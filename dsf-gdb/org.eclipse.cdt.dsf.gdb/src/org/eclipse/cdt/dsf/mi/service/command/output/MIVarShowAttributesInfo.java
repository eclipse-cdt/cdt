/*******************************************************************************
 * Copyright (c) 2000, 2025 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     John Dallaway - Accommodate LLDB-MI result record format (#1363)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * GDB/MI var-show-attributes
 */
public class MIVarShowAttributesInfo extends MIInfo {

	String attr = ""; //$NON-NLS-1$

	public MIVarShowAttributesInfo(MIOutput record) {
		super(record);
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("attr") || var.equals("status")) { //$NON-NLS-1$ //$NON-NLS-2$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIConst) {
							attr = ((MIConst) value).getString();
						}
					}
				}
			}
		}
	}

	public String getAttributes() {
		return attr;
	}

	public boolean isEditable() {
		return attr.equals("editable"); //$NON-NLS-1$
	}
}
