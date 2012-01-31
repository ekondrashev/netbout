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
package com.netbout.inf.predicates.math;

import com.netbout.inf.Predicate;
import com.netbout.inf.predicates.AbstractVarargPred;
import com.netbout.spi.Message;
import com.ymock.util.Logger;
import java.util.List;

/**
 * First argument is less than the second.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class LessThanPred extends AbstractVarargPred {

    /**
     * Public ctor.
     * @param args The arguments
     */
    public LessThanPred(final List<Predicate> args) {
        super("less-than", args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object evaluate(final Message msg, final int pos) {
        final boolean equal = (Boolean) new EqualPred(this.args())
            .evaluate(msg, pos);
        final boolean greater = (Boolean) new GreaterThanPred(this.args())
            .evaluate(msg, pos);
        final boolean less = !equal && !greater;
        Logger.debug(
            this,
            "#evaluate(): %B",
            less
        );
        return less;
    }

}