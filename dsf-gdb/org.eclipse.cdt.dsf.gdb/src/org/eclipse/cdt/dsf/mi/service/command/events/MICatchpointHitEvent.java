/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.events;

import java.util.StringTokenizer;

import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIFrame;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStreamRecord;

/**
 * @since 3.0
 */
public class MICatchpointHitEvent extends MIBreakpointHitEvent {

	/**
	 * See {@link #getReason()}
	 */
	private String fReason;
	
	protected MICatchpointHitEvent(IExecutionDMContext ctx, int token,
			MIResult[] results, MIFrame frame, int bkptno, String reason) {
		super(ctx, token, results, frame, bkptno);
		fReason = reason;
	}

	/**
	 * Returns the event the cachpoint caught. E.g., a C++ exception was thrown.
	 * This string comes either from gdb when the catchpoint is hit, or it's the
	 * gdb catchpoint keyword ('catch', 'throw', 'fork', etc) that was used to
	 * set the catchpoint
	 */
	public String getReason() {
		return fReason;
	}

	/**
	 * This variant is for catchpoint-hit in gdb < 7.0. For those versions, gdb
	 * sends us a stopped event, but it doesn't include a reason in it.
	 * Fortunately, it does output a stream record that tells us not only that a
	 * catchpoint was hit, but what its breakpoint number is.
	 * 
	 * @param streamRecord
	 *            the stream record that reveals that a catchpoint was hit and
	 *            what the event was
	 */
    public static MIBreakpointHitEvent parse(IExecutionDMContext dmc, int token, MIResult[] results, MIStreamRecord streamRecord) {
        // stream record example: "Catchpoint 1 (exception caught)"
        StringTokenizer tokenizer = new StringTokenizer(streamRecord.getString());
        tokenizer.nextToken(); // "Catchpoint"
        try {
        	int bkptNumber = Integer.parseInt(tokenizer.nextToken()); // "1"
        	StringBuilder reason = new StringBuilder();
        	boolean first = true;
        	while (tokenizer.hasMoreElements()) {
        		if (!first) {
        			reason.append(" "); //$NON-NLS-1$ ok; technically, the delim could be any whitespace, but we know it's s a space char
        		}
        		reason.append(tokenizer.nextElement());
    			first = false;
        	}

        	// remove the parentheses
        	if (reason.charAt(0) == '(') {
        		reason.deleteCharAt(0);
        	}
        	if (reason.charAt(reason.length()-1) == ')') {
        		reason.deleteCharAt(reason.length()-1);
        	}
        	
            MIStoppedEvent stoppedEvent = MIStoppedEvent.parse(dmc, token, results); 
            return new MICatchpointHitEvent(stoppedEvent.getDMContext(), token, results, stoppedEvent.getFrame(), bkptNumber, reason.toString());
        }
        catch (NumberFormatException exc) {
        	assert false : "unexpected catchpoint stream record format: " + streamRecord.getString(); //$NON-NLS-1$
        	return null;
        }
    }

	/**
	 * This variant is for a catchpoint-hit in gdb >= 7.0.
	 * {@link MIBreakpointHitEvent#parse(IExecutionDMContext, int, MIResult[], MIStreamRecord[])
	 * delegates to us if it determines that the breakpoint hit was actually
	 * caused by catchpoint. In this case, we use the event keyword used to set
	 * the catchpoint as the reason (e.g., "catch", "throw"), whereas in the gdb
	 * < 7.0 case we use the reason provided in the stream record (e.g.,
	 * "exception caught"). The inconsistency is fine. The user will get the
	 * insight he needs either way.
	 */
    public static MICatchpointHitEvent parse(IExecutionDMContext dmc, int token, MIResult[] results, int bkptNumber, String gdbKeyword) {
    	MIStoppedEvent stoppedEvent = MIStoppedEvent.parse(dmc, token, results); 
    	return new MICatchpointHitEvent(stoppedEvent.getDMContext(), token, results, stoppedEvent.getFrame(), bkptNumber, gdbKeyword);
    }
}
