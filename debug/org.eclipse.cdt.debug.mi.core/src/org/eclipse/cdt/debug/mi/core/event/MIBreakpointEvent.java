package org.eclipse.cdt.debug.mi.core.event;

import org.eclipse.cdt.debug.mi.core.output.MIConst;
import org.eclipse.cdt.debug.mi.core.output.MIExecAsyncOutput;
import org.eclipse.cdt.debug.mi.core.output.MIFrame;
import org.eclipse.cdt.debug.mi.core.output.MIResult;
import org.eclipse.cdt.debug.mi.core.output.MIResultRecord;
import org.eclipse.cdt.debug.mi.core.output.MITuple;
import org.eclipse.cdt.debug.mi.core.output.MIValue;

/**
 * ^done,reason="breakpoint-hit",bkptno="1",thread-id="0",frame={addr="0x08048468",func="main",args=[{name="argc",value="1"},{name="argv",value="0xbffff18c"}],file="hello.c",line="4"}
 *
 */
public class MIBreakpointEvent extends MIEvent {

	int bkptno;
	int threadId;
	MIFrame frame;

	MIExecAsyncOutput exec;
	MIResultRecord rr;

	public MIBreakpointEvent(MIExecAsyncOutput record) {
		exec = record;
		parse();
	}

	public MIBreakpointEvent(MIResultRecord record) {
		rr = record;
		parse();
	}

	public int getBreakNumber() {
		return bkptno;
	}

	public int getThreadId() {
		return threadId;
	}

	public MIFrame getMIFrame() {
		return frame;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("number=").append(bkptno).append('\n');
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
				if (value != null && value instanceof MIConst) {
					str = ((MIConst)value).getString();
				}

				if (var.equals("bkptno")) {
					try {
						bkptno = Integer.parseInt(str.trim());
					} catch (NumberFormatException e) {
					}
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
