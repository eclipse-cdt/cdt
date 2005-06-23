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

package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI data list register values extraction.
 */
public class MIDataListRegisterValuesInfo extends MIInfo {

	MIRegisterValue[] registers;

	public MIDataListRegisterValuesInfo(MIOutput rr) {
		super(rr);
	}

	public MIRegisterValue[] getMIRegisterValues() {
		if (registers == null) {
			parse();
		}
		return registers;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("register-values")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIList) {
							registers = MIRegisterValue.getMIRegisterValues((MIList)value);
						}
					}
				}
			}
		}
		if (registers == null) {
			registers = new MIRegisterValue[0];
		}
	}
}
