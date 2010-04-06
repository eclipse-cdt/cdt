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

	protected MICatchpointHitEvent(IExecutionDMContext ctx, int token,
			MIResult[] results, MIFrame frame, int bkptno) {
		super(ctx, token, results, frame, bkptno);
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
        	int bkptNumber = Integer.parseInt(tokenizer.nextToken()); // "1" (e.g.,)
            MIStoppedEvent stoppedEvent = MIStoppedEvent.parse(dmc, token, results); 
            return new MICatchpointHitEvent(stoppedEvent.getDMContext(), token, results, stoppedEvent.getFrame(), bkptNumber);
        }
        catch (NumberFormatException exc) {
        	assert false : "unexpected catchpoint stream record format: " + streamRecord.getString(); //$NON-NLS-1$
        	return null;
        }
    }
}
