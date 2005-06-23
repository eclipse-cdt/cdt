/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
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

	String sigName = ""; //$NON-NLS-1$
	String sigMeaning = ""; //$NON-NLS-1$

	MIExecAsyncOutput exec = null;
	MIResultRecord rr = null;

	public MIInferiorSignalExitEvent(MISession source, MIExecAsyncOutput async) {
		super(source, async.getToken());
		exec = async;
		parse();
	}

	public MIInferiorSignalExitEvent(MISession source, MIResultRecord record) {
		super(source, record.getToken());
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
		buffer.append("signal-name=" + sigName + "\n");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append("signal-meaning=" + sigMeaning + "\n");  //$NON-NLS-1$//$NON-NLS-2$
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
				String str = ""; //$NON-NLS-1$
				if (value instanceof MIConst) {
					str = ((MIConst)value).getString();
				}

				if (var.equals("signal-name")) { //$NON-NLS-1$
					sigName = str;
				} else if (var.equals("signal-meaning")) { //$NON-NLS-1$
					sigMeaning = str;
				}
			}
		}
	}
}
