/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.events;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;


/**
 *
 *  ^running
 */
@Immutable
public class MIRunningEvent extends MIEvent<IExecutionDMContext> {
    public static final int CONTINUE = 0;
    public static final int NEXT = 1;
    public static final int NEXTI = 2;
    public static final int STEP = 3;
    public static final int STEPI = 4;
    public static final int FINISH = 5;
    public static final int UNTIL = 6;
    public static final int RETURN = 7;

    final private int type;
    final private int threadId;

    public MIRunningEvent(IExecutionDMContext ctx, int token, int t) {
        this(ctx, token, t, -1);
    }

    public MIRunningEvent(IExecutionDMContext ctx, int token, int t, int threadId) {
    	super(ctx, token, null);
    	type = t;
    	this.threadId = threadId;
    }

    public int getType() {
    	return type;
    }

    public int getThreadId() {
        return threadId;
    }

    @Override
    public String toString() {
    	return "Running"; //$NON-NLS-1$
    }
}
