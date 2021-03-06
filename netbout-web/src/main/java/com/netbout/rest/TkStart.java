/**
 * Copyright (c) 2009-2015, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
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

import com.netbout.spi.Base;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Friends;
import com.netbout.spi.Inbox;
import com.netbout.spi.Messages;
import java.io.IOException;
import java.util.Iterator;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.forward.RsForward;
import org.takes.misc.Href;
import org.takes.rq.RqHref;

/**
 * Start.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 2.14
 */
public final class TkStart implements Take {

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Ctor.
     * @param bse Base
     */
    public TkStart(final Base bse) {
        this.base = bse;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final Inbox inbox = new RqAlias(this.base, req).alias().inbox();
        final long number = inbox.start();
        final Bout bout = inbox.bout(number);
        final StringBuilder msg = new StringBuilder(
            String.format("new bout #%d started", number)
        );
        final Href href = new RqHref.Base(req).href();
        final Iterator<String> invite = href.param("invite").iterator();
        while (invite.hasNext()) {
            final String friend = invite.next();
            try {
                bout.friends().invite(friend);
            } catch (final Friends.UnknownAliasException ex) {
                this.discard(bout);
                throw new RsFailure(ex);
            }
            msg.append(String.format(", \"%s\" invited", friend));
        }
        final Iterator<String> post = href.param("post").iterator();
        if (post.hasNext()) {
            try {
                bout.messages().post(post.next());
            } catch (final Messages.BrokenPostException ex) {
                this.discard(bout);
                throw new RsFailure(ex);
            }
            msg.append(", message posted");
        }
        throw new RsForward(
            new RsFlash(msg.toString()),
            new RqHref.Smart(new RqHref.Base(req)).home().path("b").path(
                Long.toString(number)
            )
        );
    }

    /**
     * Remove all friends from a bout.
     * @param bout Bout
     * @throws IOException If there is some problem inside
     */
    private void discard(final Bout bout) throws IOException {
        for (final Friend member : bout.friends().iterate()) {
            bout.friends().kick(member.alias());
        }
    }
}
