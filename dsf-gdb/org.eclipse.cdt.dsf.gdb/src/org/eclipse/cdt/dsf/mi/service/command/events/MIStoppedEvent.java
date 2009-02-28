/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
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
import org.eclipse.cdt.dsf.mi.service.command.output.MIFrame;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MITuple;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;

/**
 *  *stopped
 *
 */
@Immutable
public class MIStoppedEvent extends MIEvent<IExecutionDMContext> {

    final private MIFrame frame;

    protected MIStoppedEvent(IExecutionDMContext ctx, int token, MIResult[] results, MIFrame frame) {
    	super(ctx, token, results);
    	this.frame = frame;
    }

    public MIFrame getFrame() {
    	return frame;
    }
    
    /**
     * @since 1.1
     */
    public static MIStoppedEvent parse(IExecutionDMContext dmc, int token, MIResult[] results) 
    {
    	MIFrame frame = null;

    	for (int i = 0; i < results.length; i++) {
    		String var = results[i].getVariable();
    		MIValue value = results[i].getMIValue();

    		if (var.equals("frame")) { //$NON-NLS-1$
    			if (value instanceof MITuple) {
    				frame = new MIFrame((MITuple)value);
    			}
    		}
    	}
    	return new MIStoppedEvent(dmc, token, results, frame);
    }
}
