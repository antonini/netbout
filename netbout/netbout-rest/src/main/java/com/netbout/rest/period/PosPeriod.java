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
package com.netbout.rest.period;

import com.netbout.inf.PredicateBuilder;
import com.ymock.util.Logger;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Period, calculating position.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class PosPeriod implements Period {

    /**
     * Default limit, in elements.
     */
    public static final long DEFAULT_LIMIT = 5;

    /**
     * Start of the period, position of element.
     */
    private final transient Long start;

    /**
     * Maximum number of elements.
     */
    private final transient Long limit;

    /**
     * Add dates in it.
     */
    private final transient SortedSet<Date> dates = new TreeSet<Date>();

    /**
     * Public ctor.
     */
    public PosPeriod() {
        this(0L, PosPeriod.DEFAULT_LIMIT);
    }

    /**
     * Public ctor.
     * @param from When to start
     * @param lmt The limit
     */
    public PosPeriod(final Long from, final Long lmt) {
        this.start = from;
        this.limit = Math.min(lmt, Period.MAX);
    }

    /**
     * Build it back from text.
     * @param text The text
     * @return The period discovered
     */
    public static PosPeriod valueOf(final Object text) {
        PosPeriod period;
        if (text != null && text.toString().matches("\\d+\\-\\d+")) {
            final String[] parts = text.toString().split("-");
            period = new PosPeriod(
                Long.valueOf(parts[0]),
                Long.valueOf(parts[1])
            );
        } else {
            period = new PosPeriod();
        }
        return period;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%d-%d", this.start, this.limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof PosPeriod)
            && ((PosPeriod) obj).start.equals(this.start)
            && ((PosPeriod) obj).limit.equals(this.limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.start.hashCode() + this.limit.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean fits(final Date date) {
        return this.dates.size() < this.limit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final Date date) {
        this.dates.add(date);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Period next(final Date date) {
        return new PosPeriod(this.dates.size() + this.start, this.limit * 2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String query(final String query) {
        String original = "";
        if (!query.isEmpty() && query.charAt(0) == '(') {
            original = query;
        } else {
            if (!query.isEmpty()) {
                original = PredicateBuilder.normalize(query);
            }
        }
        final String text = String.format(
            "(and %s (from %d))",
            original,
            this.start
        );
        Logger.debug(
            this,
            "#format(%s): '%s'",
            query,
            text
        );
        return text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String title() {
        return PeriodsBuilder.when(this.dates.last());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String explain() {
        return Logger.format(
            "%s as (start=%d, limit=%d, %d dates: %[list]s)",
            this,
            this.start,
            this.limit,
            this.dates.size(),
            this.dates
        );
    }

}