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
public class MITraceFindInfo extends MIInfo {

	private boolean fFound;
	private MITraceRecord fTraceRecord;

	public MITraceFindInfo(MIOutput out) {
		super(out);
		parse();
	}

	public boolean isFound() {
		return fFound;
	}

	public MITraceRecord getTraceRecord() {
		return fTraceRecord;
	}

	private void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("found")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							fFound = ((MIConst) val).getString().equals("0") ? false : true; //$NON-NLS-1$
							if (fFound) {
								fTraceRecord = new MITraceRecord(getMIOutput());
							}
						}
					}
				}
			}
		}
	}
}
