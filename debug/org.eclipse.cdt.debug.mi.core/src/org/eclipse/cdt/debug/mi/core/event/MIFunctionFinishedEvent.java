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
 * *stopped,reason="function-finished",thread-id="0",frame={addr="0x0804855a",func="main",args=[{name="argc",value="1"},{name="argv",value="0xbffff18c"}],file="hello.c",line="17"},gdb-result-var="$1",return-value="10"
 */
public class MIFunctionFinishedEvent extends MIEvent {

	String gdbResult = "";
	String returnValue = "";
	int threadId;
	MIFrame frame;

	MIExecAsyncOutput exec;
	MIResultRecord rr;

	public MIFunctionFinishedEvent(MIExecAsyncOutput record) {
		exec = record;
		parse();
	}

	public MIFunctionFinishedEvent(MIResultRecord record) {
		rr = record;
		parse();
	}

	public String getGDBResultVar() {
		return gdbResult;
	}

	public String getReturnValue() {
		return returnValue;
	}

	public int getThreadId() {
		return threadId;
	}

	public MIFrame getFrame() {
		return frame;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("gdb-result-var=" + gdbResult + "\n");;
		buffer.append("return-value=" + returnValue + "\n");
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

				if (var.equals("gdb-result-var")) {
					gdbResult = str;
				} else if (var.equals("return-value")) {
					returnValue = str;
				} else if (var.equals("thread-id")) {
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
