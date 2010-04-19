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
	 * set the catchpoint (to be done for gdb >= 7.0)
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
	 *            the stream record that reveals that a catchpoint was hit
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
}
