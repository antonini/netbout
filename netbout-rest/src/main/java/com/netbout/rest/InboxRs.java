/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
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
package com.netbout.rest;

import com.jcabi.log.Logger;
import com.jcabi.urn.URN;
import com.netbout.client.RestSession;
import com.netbout.rest.jaxb.ShortBout;
import com.netbout.rest.period.Period;
import com.netbout.rest.period.PeriodsBuilder;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Query;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.JaxbGroup;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * RESTful front of user's inbox.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Path("/")
public final class InboxRs extends BaseRs {

    /**
     * Threshold param.
     */
    private static final String PERIOD_PARAM = "p";

    /**
     * Query to filter messages with.
     */
    private transient String query = "";

    /**
     * Use bundle?
     */
    private transient boolean bundle = true;

    /**
     * Set filtering keyword.
     * @param keyword The query
     */
    @QueryParam(RestSession.QUERY_PARAM)
    public void setQuery(final String keyword) {
        if (keyword != null) {
            this.query = keyword;
        }
    }

    /**
     * Turn bundling on/off.
     * @param flag Shall we use bundling?
     */
    @QueryParam(RestSession.BUNDLE_PARAM)
    public void setBundle(final String flag) {
        if (flag != null) {
            this.bundle = "on".equals(flag);
        }
    }

    /**
     * Get inbox.
     * @param view Which period to view
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Response inbox(@QueryParam(InboxRs.PERIOD_PARAM) final String view) {
        final Identity identity = this.identity();
        final List<ShortBout> bouts = new LinkedList<ShortBout>();
        // @checkstyle MagicNumber (1 line)
        final Period period = PeriodsBuilder.parse(view, 5L);
        final Iterable<Bout> inbox = this.fetch(period);
        final PeriodsBuilder periods = new PeriodsBuilder(
            period,
            UriBuilder.fromUri(
                this.base().clone()
                    .replaceQueryParam(RestSession.QUERY_PARAM, "{query}")
                    .build(this.query)
            )
        ).setQueryParam(InboxRs.PERIOD_PARAM);
        for (Bout bout : inbox) {
            boolean show;
            try {
                show = periods.show(new Bout.Smart(bout).updated());
            } catch (com.netbout.rest.period.PeriodViolationException ex) {
                throw new IllegalStateException(
                    Logger.format(
                        "Invalid date of bout #%d after %[list]s",
                        bout.number(),
                        bouts
                    ),
                    ex
                );
            }
            if (show) {
                bouts.add(
                    new ShortBout(
                        bout,
                        this.base().path(String.format("/%d", bout.number())),
                        identity
                    )
                );
            }
            if (!periods.more()) {
                break;
            }
        }
        return new PageBuilder()
            .schema("")
            .stylesheet("/xsl/inbox.xsl")
            .build(NbPage.class)
            .init(this)
            .searcheable(true)
            .append(new JaxbBundle("query", this.query))
            .append(new JaxbBundle("view", view))
            .append(JaxbGroup.build(bouts, "bouts"))
            .append(JaxbGroup.build(periods.links(), "periods"))
            .link(new Link("friends", "/f"))
            .render()
            .authenticated(identity)
            .build();
    }

    /**
     * Start new bout.
     * @return The JAX-RS response
     */
    @Path("/s")
    @GET
    public Response start() {
        final Identity identity = this.identity();
        final Bout bout = identity.start();
        return new PageBuilder()
            .build(NbPage.class)
            .init(this)
            .authenticated(identity)
            .entity(String.format("bout #%d created", bout.number()))
            .status(Response.Status.SEE_OTHER)
            .location(this.base().path("/{num}").build(bout.number()))
            .header("Bout-number", bout.number())
            .build();
    }

    /**
     * Start a bout with a starter.
     * @param text The text of the first message
     * @return The JAX-RS response
     * @throws Exception If something goes wrong inside (it shouldn't)
     */
    @Path("/")
    @POST
    public Response starter(@FormParam("starter") final String text)
        throws Exception {
        if (text == null) {
            throw new ForwardException(
                this,
                this.base(),
                "Form param 'starter' is mandatory"
            );
        }
        final Identity identity = this.identity();
        final Bout bout = identity.start();
        bout.rename("Welcome to Netbout!");
        bout.post(text);
        bout.invite(identity.friend(new URN("facebook", "1531296526")));
        return new PageBuilder()
            .build(NbPage.class)
            .init(this)
            .authenticated(identity)
            .status(Response.Status.SEE_OTHER)
            .location(this.base().build())
            .build();
    }

    /**
     * Fetch bouts.
     * @param period The period
     * @return The list of them
     */
    private Iterable<Bout> fetch(final Period period) {
        String pred = this.query;
        if (this.bundle) {
            pred = String.format("(and %s (bundled))", pred);
        }
        return this.identity().inbox(new Query.Textual(period.query(pred)));
    }

}