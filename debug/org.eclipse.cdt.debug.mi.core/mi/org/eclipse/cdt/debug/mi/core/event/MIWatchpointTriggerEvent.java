/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.event;

import org.eclipse.cdt.debug.mi.core.MISession;
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
	String exp = ""; //$NON-NLS-1$
	String oldValue = ""; //$NON-NLS-1$
	String newValue = ""; //$NON-NLS-1$

	public MIWatchpointTriggerEvent(MISession source, MIExecAsyncOutput async) {
		super(source, async);
		parse();
	}

	public MIWatchpointTriggerEvent(MISession source, MIResultRecord record) {
		super(source, record);
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
		buffer.append("number=").append(number).append('\n'); //$NON-NLS-1$
		buffer.append("expression=" + exp + "\n");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append("old=" + oldValue + "\n");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append("new=" + newValue + "\n");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append("thread-id=").append(getThreadId()).append('\n'); //$NON-NLS-1$
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

				if (var.equals("wpt") || var.equals("hw-awpt") || var.equals("hw-rwpt")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					if (value instanceof MITuple) {
						parseWPT((MITuple) value);
					}
				} else if (var.equals("value")) { //$NON-NLS-1$
					if (value instanceof MITuple) {
						parseValue((MITuple) value);
					}
				} else if (var.equals("thread-id")) { //$NON-NLS-1$
					if (value instanceof MIConst) {
						String str = ((MIConst) value).getString();
						try {
							int id = Integer.parseInt(str.trim());
							setThreadId(id);
						} catch (NumberFormatException e) {
						}
					}
				} else if (var.equals("frame")) { //$NON-NLS-1$
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

			if (var.equals("number")) { //$NON-NLS-1$
				if (value instanceof MIConst) {
					String str = ((MIConst) value).getString();
					try {
						number = Integer.parseInt(str);
					} catch (NumberFormatException e) {
					}
				}
			} else if (var.equals("exp")) { //$NON-NLS-1$
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
			String str = ""; //$NON-NLS-1$
			if (value instanceof MIConst) {
				str = ((MIConst) value).getString();
			}

			if (var.equals("old")) { //$NON-NLS-1$
				oldValue = str;
			} else if (var.equals("new")) { //$NON-NLS-1$
				newValue = str;
			} else if (var.equals("value")) { //$NON-NLS-1$
				oldValue = newValue = str;
			}
		}
	}
}
