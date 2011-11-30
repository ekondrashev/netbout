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

import com.netbout.bus.Bus;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Holder of all identities.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class Identities {

    /**
     * All identities known for us at the moment, and their objects.
     */
    private final ConcurrentMap<String, HubIdentity> all =
        new ConcurrentHashMap<String, HubIdentity>();

    /**
     * Statistics in plain text.
     * @return Stats in plain text
     */
    public String stats() {
        final StringBuilder builder = new StringBuilder();
        builder.append(
            String.format(
                "Total identities: %d",
                this.all.size()
            )
        );
        return builder.toString();
    }

    /**
     * Make new identity for the specified user, or find existing one and
     * assign to this user.
     * @param name The name of identity
     * @param user Name of the user
     * @return Identity found or created
     */
    protected HubIdentity make(final String name, final HubUser user) {
        final HubIdentity identity = Identities.make(name);
        if (identity.isAssigned() && !identity.belongsTo(user)) {
            throw new IllegalArgumentException(
                String.format(
                    "Identity '%s' is already taken by someone else",
                    name
                )
            );
        }
        if (!identity.isAssigned()) {
            identity.assignTo(user);
        }
        return identity;
    }

    /**
     * Make new identity or find existing one.
     * @param name The name of identity
     * @return Identity found
     */
    protected HubIdentity make(final String name) {
        HubIdentity identity;
        if (this.all.containsKey(name)) {
            identity = this.all.get(name);
        } else {
            if (Identities.needsNotifier(name)
                && !Identities.canNotify(name)) {
                throw new IllegalArgumentException(
                    String.format(
                        "Can't reach '%s' identity",
                        name
                    )
                );
            }
            identity = new HubIdentity(name);
            this.all.put(name, identity);
            Bus.make("identity-mentioned")
                .synchronously()
                .arg(name)
                .asDefault(true)
                .exec();
            Logger.debug(
                Identities.class,
                "#make('%s'): created just by name (%d total)",
                name,
                this.all.size()
            );
        }
        return identity;
    }

    /**
     * Find identities by name (including aliases).
     * @param keyword The keyword to find by
     * @return Identities found
     */
    protected Set<HubIdentity> findByKeyword(final String keyword) {
        final Set<HubIdentity> found = new HashSet<HubIdentity>();
        for (HubIdentity identity : this.all.values()) {
            if (identity.matchesKeyword(keyword)) {
                found.add(identity);
            }
        }
        final List<String> external = Bus.make("find-identities-by-keyword")
            .synchronously()
            .arg(keyword)
            .asDefault(new ArrayList<String>())
            .exec();
        for (String name : external) {
            found.add(Identities.make(name));
        }
        return found;
    }

    /**
     * This identity needs notifier?
     * @param identity The identity
     * @return It needs it?
     */
    protected Boolean needsNotifier(final String identity) {
        return !identity.matches("\\d+") && !identity.startsWith("nb:");
    }

    /**
     * We can notify this identity?
     * @param identity The identity
     * @return Can we?
     */
    private Boolean canNotify(final String identity) {
        return Bus.make("can-notify-identity")
            .synchronously()
            .arg(identity)
            .asDefault(false)
            .exec();
    }

}
