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
 * signal 2
 * "signal 2\n"
 * ^done,reason="exited-signalled",signal-name="SIGINT",signal-meaning="Interrupt"
 *
 */
public class MIInferiorSignalExitEvent extends MIDestroyedEvent {

	String sigName = "";
	String sigMeaning = "";

	MIExecAsyncOutput exec = null;
	MIResultRecord rr = null;

	public MIInferiorSignalExitEvent(MIExecAsyncOutput async) {
		super(async.getToken());
		exec = async;
		parse();
	}

	public MIInferiorSignalExitEvent(MIResultRecord record) {
		super(record.getToken());
		rr = record;
		parse();
	}

	public String getName() {
		return sigName;
	}

	public String getMeaning() {
		return sigMeaning;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("signal-name=" + sigName + "\n");;
		buffer.append("signal-meaning=" + sigMeaning + "\n");;
		return buffer.toString();
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

				if (var.equals("signal-name")) {
					sigName = str;
				} else if (var.equals("signal-meaning")) {
					sigMeaning = str;
				}
			}
		}
	}
}
