/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * *stopped,reason="function-finished",thread-id="0",frame={addr="0x0804855a",func="main",args=[{name="argc",value="1"},{name="argv",value="0xbffff18c"}],file="hello.c",line="17"},gdb-result-var="$1",return-value="10"
 */
public class MIFunctionFinishedEvent extends MIStoppedEvent {

	String gdbResult = ""; //$NON-NLS-1$
	String returnValue = ""; //$NON-NLS-1$
	String returnType = ""; //$NON-NLS-1$

	public MIFunctionFinishedEvent(MISession source, MIExecAsyncOutput async) {
		super(source, async);
		parse();
	}

	public MIFunctionFinishedEvent(MISession source, MIResultRecord record) {
		super(source, record);
		parse();
	}

	public String getGDBResultVar() {
		return gdbResult;
	}

	public String getReturnValue() {
		return returnValue;
	}

	public String getReturnType() {
		return returnType;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("gdb-result-var=" + gdbResult + "\n");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append("return-value=" + returnValue + "\n");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append("return-type=" + returnType + "\n");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append("thread-id=").append(getThreadId()).append('\n'); //$NON-NLS-1$
		MIFrame f = getFrame();
		if (f != null) {
			buffer.append(f.toString());
		}
		return buffer.toString();
	}

	void parse () {
		MIExecAsyncOutput exec = getMIExecAsyncOutput();
		MIResultRecord rr = getMIResultRecord();

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
				String str = ""; //$NON-NLS-1$
				if (value instanceof MIConst) {
					str = ((MIConst)value).getString();
				}

				if (var.equals("gdb-result-var")) { //$NON-NLS-1$
					gdbResult = str;
				} else if (var.equals("return-value")) { //$NON-NLS-1$
					returnValue = str;
				} else if (var.equals("return-type")) { //$NON-NLS-1$
					returnType = str;
				} else if (var.equals("thread-id")) { //$NON-NLS-1$
					try {
						int id = Integer.parseInt(str.trim());
						setThreadId(id);
					} catch (NumberFormatException e) {
					}
				} else if (var.equals("frame")) { //$NON-NLS-1$
					if (value instanceof MITuple) {
						MIFrame f = new MIFrame((MITuple)value);
						setFrame(f);
					}
				}
			}
		}
	}
}
