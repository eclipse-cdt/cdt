/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson               - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * GDB/MI var-set-format
 */
public class MIVarSetFormatInfo extends MIInfo {

	String value = null;

	public MIVarSetFormatInfo(MIOutput record) {
		super(record);
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("value")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							value = ((MIConst) val).getCString();
						}
					}
				}
			}
		}
	}

	public String getValue() {
		return value;
	}
}
