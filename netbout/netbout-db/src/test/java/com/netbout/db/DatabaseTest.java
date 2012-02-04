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
package com.netbout.db;

import com.rexsl.core.Manifests;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case of {@link Database}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class DatabaseTest {

    /**
     * Snapshot of Manifests.
     */
    private transient byte[] snapshot;

    /**
     * Prepare manifests.
     * @throws Exception If there is some problem inside
     */
    @Before
    public void prepare() throws Exception {
        this.snapshot = Manifests.snapshot();
        Manifests.inject("Netbout-JdbcDriver", new DriverMocker("foo").mock());
        Manifests.inject("Netbout-JdbcUrl", "jdbc:foo:");
    }

    /**
     * Prepare manifests.
     * @throws Exception If there is some problem inside
     */
    @After
    public void revert() throws Exception {
        Manifests.revert(this.snapshot);
        Database.drop();
    }

    /**
     * Database can reconnect if connection is lost.
     * @throws Exception If there is some problem inside
     * @todo #127 This test doesn't reproduce the problem still. I don't know
     *  what to do here exactly. Looks like the problem is bigger than it looks.
     */
    @Test
    public void canReconnectOnAlreadyClosedConnection() throws Exception {
        final Database database = new Database();
        // @checkstyle MagicNumber (1 line)
        for (int step = 0; step < 100; step += 1) {
            final Connection conn = database.connect();
            try {
                final PreparedStatement stmt = conn.prepareStatement(
                    "SELECT name FROM identity"
                );
                try {
                    stmt.execute();
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        }
    }

    /**
     * Database can handle many simultaneous connections.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void canHandleSimultaneousConnections() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(20);
        final Collection<Callable> tasks = new ArrayList<Callable>();
        for (int num = 0; num < 3; num += 1) {
            tasks.add(
                new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        new IdentityRowMocker().mock();
                        return true;
                    }
                }
            );
        }
        executor.invokeAll((Collection) tasks);
    }

}
