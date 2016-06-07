/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.ReverseDebugMethod;
import org.eclipse.cdt.dsf.mi.service.command.output.MIConst;
import org.eclipse.cdt.dsf.mi.service.command.output.MINotifyAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIResult;
import org.eclipse.cdt.dsf.mi.service.command.output.MIValue;
import org.eclipse.cdt.dsf.service.DsfSession;

/** @since 5.1 */
public class GDBRunControl_7_12 extends GDBRunControl_7_10 {

	public GDBRunControl_7_12(DsfSession session) {
		super(session);
	}

	@Override
	public void eventReceived(Object output) {
		if (output instanceof MIOutput) {
			MIOOBRecord[] records = ((MIOutput)output).getMIOOBRecords();
			for (MIOOBRecord r : records) {
				if (r instanceof MINotifyAsyncOutput) {
					MINotifyAsyncOutput notifyOutput = (MINotifyAsyncOutput)r;
					String asyncClass = notifyOutput.getAsyncClass();
					// These events have been added with GDB 7.6
					if ("record-started".equals(asyncClass) || //$NON-NLS-1$
						"record-stopped".equals(asyncClass)) {	 //$NON-NLS-1$
						ReverseDebugMethod newMethod = ReverseDebugMethod.OFF;
						if ("record-started".equals(asyncClass)) { //$NON-NLS-1$
							// With GDB 7.12, we are provided with the type of record
							// that was started.
							newMethod = getTraceMethodFromOutput(notifyOutput);
						} else {
							newMethod = ReverseDebugMethod.OFF;
						}
						setReverseTraceMethod(newMethod);
					}
				}
			}
		}
	}
	
	private ReverseDebugMethod getTraceMethodFromOutput(MINotifyAsyncOutput notifyOutput) {
		// With GDB 7.12, we are provided with the type of record
		// that was started.
		//   =record-started,thread-group="i1",method="btrace",format="bts"
	    //   =record-started,thread-group="i1",method="btrace",format="pt"
		//   =record-started,thread-group="i1",method="full"
		ReverseDebugMethod method = ReverseDebugMethod.SOFTWARE;;
		String methodStr = ""; //$NON-NLS-1$
		String formatStr = ""; //$NON-NLS-1$
		MIResult[] results = notifyOutput.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue val = results[i].getMIValue();
			if (var.equals("method")) { //$NON-NLS-1$
				if (val instanceof MIConst) {
					methodStr = ((MIConst)val).getString();
				}
			} else if (var.equals("format")) { //$NON-NLS-1$
				if (val instanceof MIConst) {
					formatStr = ((MIConst)val).getString();
				}
			}
		}
		
		if (methodStr.equals("full")) { //$NON-NLS-1$
			assert formatStr.isEmpty() : "Unexpected format string for full method in =record-started: " + formatStr; //$NON-NLS-1$
			method = ReverseDebugMethod.SOFTWARE;
		} else if (methodStr.equals("btrace")){ //$NON-NLS-1$
			if (formatStr.equals("bts")) { //$NON-NLS-1$
				method = ReverseDebugMethod.BRANCH_TRACE;
			} else if (formatStr.equals("pt")) { //$NON-NLS-1$
				method = ReverseDebugMethod.PROCESSOR_TRACE;
			} else {
				assert false : "Unexpected trace format for bts in =record-started: " + formatStr; //$NON-NLS-1$
			}
		} else {
			assert false : "Unexpected trace method in =record-started: " + methodStr; //$NON-NLS-1$
		}
		return method;
	}
}
