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
import com.netbout.spi.BoutNotFoundException;
import com.netbout.spi.Helper;
import com.netbout.spi.Identity;
import com.netbout.spi.PromotionException;
import com.netbout.spi.User;
import java.net.URL;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@XmlType(name = "identity")
@XmlAccessorType(XmlAccessType.NONE)
public final class HubIdentity implements Identity {

    /**
     * {@inheritDoc}
     */
    @Override
    public User user() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout start() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Bout> inbox(final String query) {
        return null;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (3 lines)
     */
    @Override
    public Bout bout(final Long number) throws BoutNotFoundException {
        return null;
    }

    /**
     * Get name of the object, JAXB-related method.
     * @return The name
     */
    @XmlElement(name = "name", required = true)
    public String getName() {
        return this.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return "hello";
    }

    /**
     * Get photo of the object, JAXB-related method.
     * @return The name
     */
    @XmlElement(name = "photo", required = true)
    public URL getPhoto() {
        return this.photo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL photo() {
        return null;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (3 lines)
     */
    @Override
    public void promote(final Helper helper) throws PromotionException {
        //...
    }

}