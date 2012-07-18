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
package com.netbout.inf.notices;

import com.netbout.inf.Notice;
import com.netbout.spi.Bout;
import com.netbout.spi.Identity;
import com.netbout.spi.Profile;
import com.netbout.spi.Urn;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Notice is related to identity.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface IdentityNotice extends Notice {

    /**
     * The identity this notice is related to.
     * @return The identity
     */
    Identity identity();

    /**
     * Serializer.
     */
    class Serial implements Serializer<IdentityNotice> {
        /**
         * {@inheritDoc}
         */
        @Override
        public String nameOf(final IdentityNotice notice) {
            return String.format(
                "w/%s",
                notice.identity().name()
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Set<Urn> deps(final IdentityNotice notice) {
            final Set<Urn> deps = new HashSet<Urn>();
            deps.add(notice.identity().name());
            return deps;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final IdentityNotice notice,
            final DataOutputStream stream) throws IOException {
            stream.writeUTF(notice.identity().name().toString());
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public IdentityNotice read(final DataInputStream stream)
            throws IOException {
            final Urn name = Urn.create(stream.readUTF());
            return new IdentityNotice() {
                @Override
                public Identity identity() {
                    return Serial.toIdentity(name);
                }
            };
        }
        /**
         * Create an identity from name.
         * @param name The name of it
         * @return The identity
         */
        static Identity toIdentity(final Urn name) {
            return new Identity() {
                @Override
                public int hashCode() {
                    return this.name().hashCode();
                }
                @Override
                public boolean equals(final Object dude) {
                    return this == dude || (dude instanceof Identity
                        && this.name().equals(
                            Identity.class.cast(dude).name()
                        )
                    );
                }
                @Override
                public Urn name() {
                    return name;
                }
                @Override
                public Profile profile() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Set<Identity> friends(final String query) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Identity friend(final Urn name) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Bout bout(final Long number) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Iterable<Bout> inbox(final String query) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Bout start() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public URL authority() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public Long eta() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public int compareTo(final Identity identity) {
                    return name.compareTo(identity.name());
                }
            };
        }
    }

}
