/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;

import org.eclipse.cdt.debug.mi.core.output.MIConst;
import org.eclipse.cdt.debug.mi.core.output.MIExecAsyncOutput;
import org.eclipse.cdt.debug.mi.core.output.MIResult;
import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;
import org.eclipse.cdt.debug.mi.core.output.MIValue;



/**
 * *stopped,reason="exited-normally"
 * *stopped,reason="exited",exit-code="04"
 * ^done,reason="exited",exit-code="04"
 *
 */
public class MIInferiorExitEvent extends MIEvent {

	int code = 0;

        MIExecAsyncOutput exec = null;
        MIResultRecord rr = null;

	public MIInferiorExitEvent(int token) {
		super(token);
	}

	public MIInferiorExitEvent(MIExecAsyncOutput async) {
		super(async.getToken());
		exec = async;
		parse();
	}

	public MIInferiorExitEvent(MIResultRecord record) {
		super(record.getToken());
		rr = record;
		parse();
	}

	public int getExitCode() {
		return code;
	}

	void parse () {
		MIResult[] results = null;
		if (exec != null) {
			results = exec.getMIResults();
		} else if (rr != null) {
			results = rr.getMIResults();
		}

		if (results != null) {
			for (int i = 0; i < results.length; i++) {
				String var = results[i].getVariable();
				MIValue value = results[i].getMIValue();
				String str = "";
				if (value instanceof MIConst) {
					str = ((MIConst)value).getString();
				}

				if (var.equals("exit-code")) {
					try {
						code = Integer.decode(str.trim()).intValue();
					} catch (NumberFormatException e) {
					}
				}
			}
		}
	}

}
