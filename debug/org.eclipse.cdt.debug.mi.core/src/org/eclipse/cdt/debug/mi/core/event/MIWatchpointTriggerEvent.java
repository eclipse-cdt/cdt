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
 *  *stopped,reason="watchpoint-trigger",wpt={number="2",exp="i"},value={old="0",new="1"},thread-id="0",frame={addr="0x08048534",func="main",args=[{name="argc",value="1"},{name="argv",value="0xbffff18c"}],file="hello.c",line="10"}
 *
 */
public class MIWatchpointTriggerEvent extends MIStoppedEvent {

	int number;
	String exp = "";
	String oldValue = "";
	String newValue = "";

	public MIWatchpointTriggerEvent(MIExecAsyncOutput async) {
		super(async);
		parse();
	}

	public MIWatchpointTriggerEvent(MIResultRecord record) {
		super(record);
		parse();
	}

	public int getNumber() {
		return number;
	}

	public String getExpression() {
		return exp;
	}

	public String getOldValue() {
		return oldValue;
	}

	public String getNewValue() {
		return newValue;
	}


	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("number=").append(number).append('\n');
		buffer.append("expression=" + exp + "\n");
		;
		buffer.append("old=" + oldValue + "\n");
		buffer.append("new=" + newValue + "\n");
		buffer.append("thread-id=").append(getThreadId()).append('\n');
		MIFrame f = getFrame();
		if (f != null) {
			buffer.append(f.toString());
		}
		return buffer.toString();
	}

	void parse() {
		MIResult[] results = null;
		MIExecAsyncOutput exec = getMIExecAsyncOutput();
		MIResultRecord rr = getMIResultRecord();
		if (exec != null) {
			results = exec.getMIResults();
		} else if (rr != null) {
			results = rr.getMIResults();
		}
		if (results != null) {
			for (int i = 0; i < results.length; i++) {
				String var = results[i].getVariable();
				MIValue value = results[i].getMIValue();

				if (var.equals("wpt")) {
					if (value instanceof MITuple) {
						parseWPT((MITuple) value);
					}
				} else if (var.equals("value")) {
					if (value instanceof MITuple) {
						parseValue((MITuple) value);
					}
				} else if (var.equals("thread-id")) {
					if (value instanceof MIConst) {
						String str = ((MIConst) value).getString();
						try {
							int id = Integer.parseInt(str.trim());
							setThreadId(id);
						} catch (NumberFormatException e) {
						}
					}
				} else if (var.equals("frame")) {
					if (value instanceof MITuple) {
						MIFrame f = new MIFrame((MITuple) value);
						setFrame(f);
					}
				}
			}
		}
	}

	void parseWPT(MITuple tuple) {
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();

			if (var.equals("number")) {
				if (value instanceof MIConst) {
					String str = ((MIConst) value).getString();
					try {
						number = Integer.parseInt(str);
					} catch (NumberFormatException e) {
					}
				}
			} else if (var.equals("exp")) {
				if (value instanceof MIConst) {
					exp = ((MIConst) value).getString();
				}
			}
		}
	}

	void parseValue(MITuple tuple) {
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = "";
			if (value instanceof MIConst) {
				str = ((MIConst) value).getString();
			}

			if (var.equals("old")) {
				oldValue = str;
			} else if (var.equals("new")) {
				newValue = str;
			}
		}
	}
}
