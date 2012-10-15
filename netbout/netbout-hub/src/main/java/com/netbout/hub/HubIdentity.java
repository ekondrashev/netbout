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
package com.netbout.hub;

import com.jcabi.log.Logger;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import com.netbout.spi.Identity;
import com.netbout.spi.NetboutUtils;
import com.netbout.spi.Profile;
import com.netbout.spi.Urn;
import java.net.URL;
import java.util.Set;

/**
 * Identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class HubIdentity implements Identity {

    /**
     * The hub.
     */
    private final transient PowerHub hub;

    /**
     * The name.
     */
    private final transient Urn iname;

    /**
     * The profile.
     */
    private final transient Profile iprofile;

    /**
     * Public ctor.
     * @param ihub The hub
     * @param name The identity's name
     */
    public HubIdentity(final PowerHub ihub, final Urn name) {
        this.hub = ihub;
        this.iname = name;
        this.iprofile = new HubProfile(ihub, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.iname.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Friend friend) {
        return this.iname.compareTo(identity.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return obj == this || ((obj instanceof Identity)
            && this.name().equals(((Identity) obj).name()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.name().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long eta() {
        return ((DefaultHub) this.hub).eta(this.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL authority() {
        try {
            return this.hub.resolver().authority(this.name());
        } catch (Identity.UnreachableUrnException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Urn name() {
        return this.iname;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bout start() {
        Bout bout;
        try {
            bout = this.bout(this.hub.manager().create(this.name()));
        } catch (Identity.BoutNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
        Logger.info(
            this,
            "Bout #%d started successfully by '%s'",
            bout.number(),
            this.name()
        );
        return bout;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Bout bout(final Long number) throws Identity.BoutNotFoundException {
        return new HubBout(this.hub, this, this.hub.manager().find(number));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Bout> inbox(final String query) {
        try {
            return new LazyBouts(
                this.hub.manager(),
                this.hub.infinity().messages(
                    String.format(
                        "(and (talks-with '%s') %s (unique $bout.number))",
                        this.name(),
                        NetboutUtils.normalize(query)
                    )
                ),
                this
            );
        } catch (com.netbout.inf.InvalidSyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Profile profile() {
        return this.iprofile;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (4 lines)
     */
    @Override
    public Friend friend(final Urn name)
        throws Identity.UnreachableUrnException {
        final Identity identity = this.hub.identity(name);
        Logger.debug(
            this,
            "#friend('%s'): found",
            name
        );
        return identity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Friend> friends(final String keyword) {
        final Set<Friend> friends = this.hub.findByKeyword(this, keyword);
        if (friends.contains(this)) {
            friends.remove(this);
        }
        Logger.debug(
            this,
            "#friends('%s'): found %d friends",
            keyword,
            friends.size()
        );
        return friends;
    }

}
