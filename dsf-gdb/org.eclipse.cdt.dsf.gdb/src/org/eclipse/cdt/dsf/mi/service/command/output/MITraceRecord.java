/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * -trace-find result.
 * @since 3.0
 */
public class MITraceRecord extends MIInfo {

	private MIFrame fStackFrame = null;
	private Integer fTracepoint = null;
	private Integer fRecordIndex = null;

	public MITraceRecord(MIOutput out) {
		super(out);
		parse();
	}

	public MIFrame getStackFrame() {
		return fStackFrame;
	}

	public Integer getTracepointId() {
		return fTracepoint;
	}

	public Integer getRecordIndex() {
		return fRecordIndex;
	}

	private void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("traceframe")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							try {
								fRecordIndex = Integer.parseInt(((MIConst) val).getString());
							} catch (NumberFormatException e) {
							}
						}
					} else if (var.equals("tracepoint")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							try {
								fTracepoint = Integer.parseInt(((MIConst) val).getString());
							} catch (NumberFormatException e) {
							}
						}
					} else if (var.equals("frame")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MITuple) {
							fStackFrame = new MIFrame((MITuple) value);
						}
					}
				}
			}
		}
	}
}
