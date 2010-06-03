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
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;


/**
 * This can only be detected by gdb/mi after GDB 6.8.
 *
 */
@Immutable
public class MIThreadCreatedEvent extends MIEvent<IContainerDMContext> {

    final private String fThreadId;

    public MIThreadCreatedEvent(IContainerDMContext ctx, int id) {
        this(ctx, 0, id);
    }

    public MIThreadCreatedEvent(IContainerDMContext ctx, int token, int id) {
        super(ctx, token, null);
        fThreadId = Integer.toString(id);
    }

    /**
     * @since 1.1
     */
    public MIThreadCreatedEvent(IContainerDMContext ctx, String threadId) {
        this(ctx, 0, threadId);
    }

    /**
     * @since 1.1
     */
    public MIThreadCreatedEvent(IContainerDMContext ctx, int token, String threadId) {
        super(ctx, token, null);
        fThreadId = threadId;
    }

    public int getId() {
    	try { 
    		return Integer.parseInt(fThreadId);
    	}
    	catch (NumberFormatException e) {
    		return 0;
    	}
    }
    
    /**
     * @since 1.1
     */
    public String getStrId() {
        return fThreadId;
    }
}
