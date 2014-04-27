/**
 * Copyright (c) 2009-2014, Netbout.com
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

import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rexsl.page.inset.FlashInset;
import java.io.IOException;
import java.util.logging.Level;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * RESTful front of user's inbox.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Path("/")
public final class InboxRs extends BaseRs {

    /**
     * Get inbox.
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response inbox() {
        this.alias();
        return new PageBuilder()
            .stylesheet("/xsl/inbox.xsl")
            .build(NbPage.class)
            .init(this)
            .link(new Link("start", "./start"))
            .render()
            .build();
    }

    /**
     * Start new bout.
     * @throws IOException If fails
     */
    @GET
    @Path("/start")
    public void start() throws IOException {
        final long number = this.alias().inbox().start();
        throw FlashInset.forward(
            this.uriInfo().getBaseUriBuilder().clone()
                .path(BoutRs.class)
                .path("/{number}")
                .build(number),
            "new bout started",
            Level.INFO
        );
    }

}