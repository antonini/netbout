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

import java.net.HttpURLConnection;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link NbPage}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id: BoutRsTest.java 2482 2012-05-22 10:18:34Z guard $
 */
public final class NbPageTest {

    /**
     * NbPage can build anonymous response.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void buildsAnonymousResponse() throws Exception {
        final NbPage page = new NbPage();
        final BaseRs rest = new NbResourceMocker().mock(BaseRs.class);
        page.init(rest);
        MatcherAssert.assertThat(
            page.render().anonymous().build(),
            Matchers.allOf(
                Matchers.hasProperty(
                    "status",
                    Matchers.equalTo(HttpURLConnection.HTTP_OK)
                ),
                Matchers.hasProperty(
                    "metadata",
                    Matchers.allOf(
                        Matchers.hasKey("Netbout-Version"),
                        Matchers.hasKey(HttpHeaders.SET_COOKIE),
                        Matchers.hasEntry(
                            Matchers.equalTo(HttpHeaders.CONTENT_TYPE),
                            Matchers.hasItem(MediaType.TEXT_XML_TYPE)
                        )
                    )
                ),
                Matchers.hasProperty(
                    "entity",
                    Matchers.equalTo(page)
                )
            )
        );
    }

}