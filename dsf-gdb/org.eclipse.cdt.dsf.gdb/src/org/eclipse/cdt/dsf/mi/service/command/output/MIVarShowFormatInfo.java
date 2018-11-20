/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import org.eclipse.cdt.dsf.mi.service.MIFormat;

/**
 * GDB/MI var-show-format
 */
public class MIVarShowFormatInfo extends MIInfo {

	int format = MIFormat.NATURAL;

	public MIVarShowFormatInfo(MIOutput record) {
		super(record);
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("name")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIConst) {
							String str = ((MIConst) value).getString();
							if ("binary".equals(str)) { //$NON-NLS-1$
								format = MIFormat.BINARY;
							} else if ("decimal".equals(str)) { //$NON-NLS-1$
								format = MIFormat.DECIMAL;
							} else if ("hexadecimal".equals(str)) { //$NON-NLS-1$
								format = MIFormat.HEXADECIMAL;
							} else if ("octal".equals(str)) { //$NON-NLS-1$
								format = MIFormat.OCTAL;
							} else if ("natural".equals(str)) { //$NON-NLS-1$
								format = MIFormat.NATURAL;
							}
						}
					}
				}
			}
		}
	}

	public int getFormat() {
		return format;
	}
}
