package org.eclipse.cdt.debug.mi.core.output;



/**
 * ^done,reason="breakpoint-hit",bkptno="1",thread-id="0",frame={addr="0x08048468",func="main",args=[{name="argc",value="1"},{name="argv",value="0xbffff18c"}],file="hello.c",line="4"}
 *
 */
public class MIBreakHitInfo {

	int bkptno;
	int threadId;
	MIFrame frame;
	String file = "";
	int line;
	MIExecAsyncOutput exec;
	MIResultRecord rr;

	public MIBreakHitInfo(MIExecAsyncOutput record) {
		exec = record;
	}

	public MIBreakHitInfo(MIResultRecord record) {
		rr = record;
	}

	public int getBreakNumber() {
		return bkptno;
	}

	public int getThreadId() {
		return threadId;
	}

	public MIFrame getFrame() {
		return frame;
	}

	public String getFile() {
		return file;
	}

	public int getLine() {
		return line;
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
						bkptno = Integer.parseInt(str);
					} catch (NumberFormatException e) {
					}
				} else if (var.equals("thread-id")) {
					try {
						threadId = Integer.parseInt(str);
					} catch (NumberFormatException e) {
					}
				} else if (var.equals("frame")) {
					if (value instanceof MITuple) {
						frame = new MIFrame((MITuple)value);
					}
				} else if (var.equals("file")) {
					file = str;
				} else if (var.equals("line")) {
					try {
						line = Integer.parseInt(str);
					} catch (NumberFormatException e) {
					}
				}
			}
		}
	}
}
