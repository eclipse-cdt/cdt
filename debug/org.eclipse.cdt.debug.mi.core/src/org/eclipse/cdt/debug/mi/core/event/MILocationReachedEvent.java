/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.event;

import org.eclipse.cdt.debug.mi.core.output.MIConst;
import org.eclipse.cdt.debug.mi.core.output.MIExecAsyncOutput;
import org.eclipse.cdt.debug.mi.core.output.MIFrame;
import org.eclipse.cdt.debug.mi.core.output.MIResult;
import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;
import org.eclipse.cdt.debug.mi.core.output.MITuple;
import org.eclipse.cdt.debug.mi.core.output.MIValue;

/**
 * *stopped,reason="location-reached",thread-id="0",frame={addr="0x0804858e",func="main2",args=[],file="hello.c",line="27"}
 */
public class MILocationReachedEvent extends MIEvent {

	int threadId;
	MIFrame frame;

	MIExecAsyncOutput exec;
	MIResultRecord rr;

	public MILocationReachedEvent(MIExecAsyncOutput record) {
		exec = record;
		parse();
	}

	public MILocationReachedEvent(MIResultRecord record) {
		rr = record;
		parse();
	}

	public int getThreadId() {
		return threadId;
	}

	public MIFrame getFrame() {
		return frame;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("thread-id=").append(threadId).append('\n');
		buffer.append(frame.toString());
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

				if (var.equals("thread-id")) {
					try {
						threadId = Integer.parseInt(str.trim());
					} catch (NumberFormatException e) {
					}
				} else if (var.equals("frame")) {
					if (value instanceof MITuple) {
						frame = new MIFrame((MITuple)value);
					}
				}
			}
		}
	}
}
