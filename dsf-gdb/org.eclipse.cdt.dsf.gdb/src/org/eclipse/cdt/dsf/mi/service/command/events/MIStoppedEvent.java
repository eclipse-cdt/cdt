/*******************************************************************************
 * Copyright (c) 2009, 2015 QNX Software Systems and others.
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
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
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
    
    /* 
     * If a method returned a value it could have different stopped event reasons
     * but the event will contain the fields we need.
     */
    final private String gdbResult;
    final private String returnValue;
    final private String returnType;

    protected MIStoppedEvent(IExecutionDMContext ctx, int token, MIResult[] results, MIFrame frame) {
    	this(ctx, token, results, frame, null, null, null);
    }
    
    /** @since 4.7 */
    protected MIStoppedEvent(IExecutionDMContext ctx, int token, MIResult[] results, MIFrame frame,
    		                 String gdbResult, String returnValue, String returnType) {
    	super(ctx, token, results);
    	this.frame = frame;
        this.gdbResult = gdbResult;
        this.returnValue = returnValue;
        this.returnType = returnType;
    }

    public MIFrame getFrame() {
    	return frame;
    }
    
    /** @since 4.7 */
    public String getGDBResultVar() {
    	return gdbResult;
    }

    /** @since 4.7 */
    public String getReturnValue() {
    	return returnValue;
    }

    /** @since 4.7 */
    public String getReturnType() {
    	return returnType;
    }

    /**
     * @since 1.1
     */
    public static MIStoppedEvent parse(IExecutionDMContext dmc, int token, MIResult[] results) 
    {
    	MIFrame frame = null;
        String  gdbResult = null;
        String  returnValue = null;
        String  returnType = null;

    	for (int i = 0; i < results.length; i++) {
    		String var = results[i].getVariable();
    		MIValue value = results[i].getMIValue();

    		if (var.equals("frame")) { //$NON-NLS-1$
    			if (value instanceof MITuple) {
    				frame = new MIFrame((MITuple)value);
    			}
    		} else if (var.equals("gdb-result-var")) { //$NON-NLS-1$
    			if (value instanceof MIConst) {
    				gdbResult = ((MIConst)value).getString();
    			}
    		} else if (var.equals("return-value")) { //$NON-NLS-1$
    			if (value instanceof MIConst) {
    				returnValue = ((MIConst)value).getString();
    			}
    		} else if (var.equals("return-type")) { //$NON-NLS-1$
    			if (value instanceof MIConst) {
    				returnType = ((MIConst)value).getString();
    			}
    		} 
    	}
    	return new MIStoppedEvent(dmc, token, results, frame, gdbResult, returnValue, returnType);
    }
}
