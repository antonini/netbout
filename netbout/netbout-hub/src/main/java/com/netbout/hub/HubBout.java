/**
 * Copyright (c) 2009-2011, netBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
 * incident to the author by email.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.netbout.hub;

import com.netbout.spi.Bout;
import com.netbout.spi.DuplicateInvitationException;
import com.netbout.spi.Identity;
import com.netbout.spi.Message;
import com.netbout.spi.MessageNotFoundException;
import com.netbout.spi.MessagePostException;
import com.netbout.spi.NetboutUtils;
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import com.netbout.spi.xml.DomParser;
import com.ymock.util.Logger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class HubBout implements Bout {

    /**
     * The hub.
     */
    private final transient Hub hub;

    /**
     * The viewer.
     */
    private final transient Identity viewer;

    /**
     * The data.
     */
    private final transient BoutDt data;

    /**
     * Public ctor.
     * @param ihub The hub
     * @param idnt The viewer
     * @param dat The data
     */
    public HubBout(final Hub ihub, final Identity idnt, final BoutDt dat) {
        this.hub = ihub;
        this.viewer = idnt;
        this.data = dat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Bout bout) {
        return NetboutUtils.dateOf(this).compareTo(NetboutUtils.dateOf(bout));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long number() {
        return this.data.getNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String title() {
        return this.data.getTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date date() {
        return this.data.getDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void confirm() {
        this.data.confirm(this.viewer.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void leave() {
        this.data.kickOff(this.viewer.name());
        if (this.viewer instanceof InvitationSensitive) {
            ((InvitationSensitive) this.viewer).kickedOff(this.number());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rename(final String text) {
        if (!NetboutUtils.participantOf(this.viewer, this).confirmed()) {
            throw new IllegalStateException(
                String.format(
                    "You '%s' can't rename bout #%d until you join",
                    this.viewer,
                    this.number()
                )
            );
        }
        this.data.setTitle(text);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Participant invite(final Identity friend)
        throws DuplicateInvitationException {
        if (!NetboutUtils.participantOf(this.viewer, this).confirmed()) {
            throw new IllegalStateException(
                String.format(
                    "You '%s' can't invite %s until you join bout #%d",
                    this.viewer,
                    friend,
                    this.number()
                )
            );
        }
        if (NetboutUtils.participatesIn(friend.name(), this)) {
            throw new DuplicateInvitationException(
                String.format(
                    "Identity '%s' has already been invited to bout #%d",
                    friend,
                    this.number()
                )
            );
        }
        final ParticipantDt dude = this.data.addParticipant(friend.name());
        Logger.debug(
            this,
            "#invite('%s'): success",
            friend
        );
        if (friend instanceof InvitationSensitive) {
            ((InvitationSensitive) friend).invited(this);
        }
        this.hub.make("just-invited")
            .inBout(this)
            .arg(this.number())
            .asDefault(false)
            .exec();
        return new HubParticipant(this.hub, this, dude, this.data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Collection<Participant> participants() {
        final Collection<Participant> participants
            = new ArrayList<Participant>();
        for (ParticipantDt dude : this.data.getParticipants()) {
            participants.add(
                new HubParticipant(this.hub, this, dude, this.data)
            );
        }
        Logger.debug(
            this,
            "#participants(): %d participant(s) found in bout #%d: %[list]s",
            participants.size(),
            this.number(),
            participants
        );
        return participants;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public List<Message> messages(final String query) {
        final List<MessageDt> datas =
            new ArrayList<MessageDt>(this.data.getMessages());
        final List<Message> messages = new ArrayList<Message>();
        for (MessageDt msg : datas) {
            messages.add(new HubMessage(this.hub, this.viewer, this, msg));
        }
        Collections.sort(messages, Collections.reverseOrder());
        final List<Message> result = this.filter(messages, query);
        Logger.debug(
            this,
            "#messages('%s'): %d message(s) found",
            query,
            result.size()
        );
        return result;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Message message(final Long num) throws MessageNotFoundException {
        final Message message = new HubMessage(
            this.hub,
            this.viewer,
            this,
            this.data.findMessage(num)
        );
        Logger.debug(
            this,
            "#message(#%d): found",
            num
        );
        return message;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     * @checkstyle ExecutableStatementCount (80 lines)
     */
    @Override
    public Message post(final String text) throws MessagePostException {
        if (text.isEmpty()) {
            throw new MessagePostException("some message content is required");
        }
        if (!NetboutUtils.participantOf(this.viewer, this).confirmed()) {
            throw new IllegalStateException(
                String.format(
                    "You '%s' can't post to bout #%d until you join",
                    this.viewer,
                    this.number()
                )
            );
        }
        this.validate(text);
        final Long duplicate = this.hub.make("pre-post-ignore-duplicate")
            .synchronously()
            .inBout(this)
            .arg(this.number())
            .arg(text)
            .asDefault(0L)
            .exec();
        Message message;
        if (duplicate == 0L) {
            final MessageDt msg = this.data.addMessage();
            msg.setDate(new Date());
            msg.setAuthor(this.viewer.name());
            msg.setText(text);
            Logger.debug(
                this,
                "#post('%s'): message posted",
                text
            );
            message = new HubMessage(
                this.hub,
                this.viewer,
                this,
                msg
            );
            message.text();
            this.hub.make("notify-bout-participants")
                .inBout(this)
                .arg(this.number())
                .arg(message.number())
                .asDefault(false)
                .exec();
        } else {
            try {
                message = this.message(duplicate);
            } catch (com.netbout.spi.MessageNotFoundException ex) {
                throw new MessagePostException(
                    String.format(
                        "duplicate found at msg #%d, but it's absent",
                        duplicate
                    ),
                    ex
                );
            }
        }
        return message;
    }

    /**
     * Filter list of messages with a predicate.
     * @param list The list to filter
     * @param query The query
     * @return New list of them
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public List<Message> filter(final List<Message> list,
        final String query) {
        final List<Message> result = new ArrayList<Message>();
        final Predicate predicate = new PredicateBuilder(this.hub).parse(query);
        for (Message msg : list) {
            boolean visible = true;
            if (!query.isEmpty()) {
                final Object response = predicate.evaluate(msg, result.size());
                if (response instanceof Boolean) {
                    visible = (Boolean) response;
                } else if (response instanceof String) {
                    result.add(new PlainMessage(this, (String) response));
                    break;
                } else {
                    throw new IllegalArgumentException(
                        Logger.format(
                            "Can't understand %[type]s response from '%s'",
                            response,
                            query
                        )
                    );
                }
            }
            if (visible) {
                result.add(msg);
            }
        }
        if (list.isEmpty()) {
            final Object response = predicate.evaluate(
                new PlainMessage(this, ""), 0
            );
            if (response instanceof String) {
                result.add(new PlainMessage(this, (String) response));
            }
        }
        return result;
    }

    /**
     * Validate incoming text and throw exception if not valid.
     * @param text The text to validate
     * @throws MessagePostException If failed to validate
     * @checkstyle RedundantThrows (3 lines)
     */
    private void validate(final String text) throws MessagePostException {
        final DomParser parser = new DomParser(text);
        try {
            parser.validate();
        } catch (com.netbout.spi.xml.DomValidationException ex) {
            Logger.warn(
                this,
                "#post('%s'): %[exception]s",
                text,
                ex
            );
            throw new MessagePostException(ex);
        }
        if (parser.isXml()) {
            Urn namespace;
            try {
                namespace = parser.namespace();
            } catch (com.netbout.spi.xml.DomValidationException ex) {
                throw new MessagePostException(ex);
            }
            URL def;
            try {
                def = new URL("http://localhost");
            } catch (java.net.MalformedURLException ex) {
                throw new IllegalStateException();
            }
            final URL url = this.hub.make("resolve-xml-namespace")
                .synchronously()
                .arg(namespace)
                .asDefault(def)
                .exec();
            if (url.equals(def)) {
                throw new MessagePostException(
                    String.format(
                        "Namespace '%s' is not supported by helpers",
                        namespace
                    )
                );
            }
            URL schema;
            try {
                schema = parser.schemaLocation(namespace);
            } catch (com.netbout.spi.xml.DomValidationException ex) {
                throw new MessagePostException(ex);
            }
            if (!url.equals(schema)) {
                throw new MessagePostException(
                    String.format(
                        "Schema for namespace '%s' should be '%s' (not '%s')",
                        namespace,
                        url,
                        schema
                    )
                );
            }
        }
    }

}
