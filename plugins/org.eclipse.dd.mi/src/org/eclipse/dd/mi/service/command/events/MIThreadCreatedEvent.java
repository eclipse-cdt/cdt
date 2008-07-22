/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.dd.mi.service.command.events;

import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.mi.service.command.output.MIConst;
import org.eclipse.dd.mi.service.command.output.MIResult;
import org.eclipse.dd.mi.service.command.output.MIValue;


/**
 * This can not be detected yet by gdb/mi.
 *
 */
@Immutable
public class MIThreadCreatedEvent extends MIEvent<IContainerDMContext> {

    final private int tid;

    public MIThreadCreatedEvent(IContainerDMContext ctx, int id) {
        this(ctx, 0, id);
    }

    public MIThreadCreatedEvent(IContainerDMContext ctx, int token, int id) {
        super(ctx, token, null);
        tid = id;
    }

    public int getId() {
        return tid;
    }

    public static MIThreadCreatedEvent parse(IContainerDMContext ctx, int token, MIResult[] results)
    {
    	for (int i = 0; i < results.length; i++) {
    		String var = results[i].getVariable();
    		MIValue val = results[i].getMIValue();
    		if (var.equals("id")) { //$NON-NLS-1$
    			if (val instanceof MIConst) {
    				try { 
    					int thread = Integer.parseInt(((MIConst) val).getString());
    					return new MIThreadCreatedEvent(ctx, token, thread);
    				}
    				catch (NumberFormatException e) {
    					return null;
    				}
    			}
    		}
    	}

    	return null;
    }
}
