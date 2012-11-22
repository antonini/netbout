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
package com.netbout.inf.ray.imap.dir;

import com.jcabi.log.Logger;
import com.netbout.inf.Attribute;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Abstract sub-directory with documents.
 *
 * <p>Class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
abstract class AbstractVersion implements Closeable {

    /**
     * Lock on the directory.
     */
    private final transient Lock lock;

    /**
     * Public ctor.
     * @param lck The directory where to work
     * @throws IOException If some I/O problem inside
     */
    public AbstractVersion(@NotNull final Lock lck) throws IOException {
        this.lock = lck;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return String.format(
            "%s:%s",
            this.getClass().getSimpleName(),
            this.lock.toString()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return this.lock.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(final Object ver) {
        return this == ver || (ver instanceof AbstractVersion
            && AbstractVersion.class.cast(ver).lock.equals(this.lock));
    }

    /**
     * Expire it.
     * @throws IOException If some I/O problem inside
     */
    public final void expire() throws IOException {
        this.lock.expire();
    }

    /**
     * Get name of reverse file.
     * @param attr Attribute
     * @return File name
     * @throws IOException If some I/O problem inside
     */
    public final File reverse(final Attribute attr) throws IOException {
        final File file = new File(
            this.lock.dir(),
            String.format("/%s/reverse.inf", attr)
        );
        FileUtils.touch(file);
        return file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void close() throws IOException {
        this.closeDeps();
        this.lock.close();
        Logger.info(this, "#close(): closed with %s", this.lock);
    }

    /**
     * Get all attributes in the baseline.
     * @return The list of all of them
     * @throws IOException If some I/O problem inside
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public final Set<Attribute> attributes() throws IOException {
        final Set<Attribute> attrs = new HashSet<Attribute>();
        final File[] dirs = this.lock.dir().listFiles();
        if (dirs != null) {
            for (File file : dirs) {
                if (!file.isDirectory()) {
                    continue;
                }
                final String name = FilenameUtils.getName(file.getPath());
                if (name.charAt(0) == '_') {
                    continue;
                }
                if (!name.matches("[a-z][a-z0-9\\-]+")) {
                    throw new IOException(
                        String.format(
                            "invalid name of attribute '%s' in %s",
                            name,
                            file
                        )
                    );
                }
                attrs.add(new Attribute(name));
            }
        }
        return attrs;
    }

    /**
     * Get directory.
     * @return The directory
     * @throws IOException If some I/O problem inside
     */
    protected final File dir() throws IOException {
        return this.lock.dir();
    }

    /**
     * Close all dependencies.
     * @throws IOException If some I/O problem inside
     */
    protected abstract void closeDeps() throws IOException;

}
