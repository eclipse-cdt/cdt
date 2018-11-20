/*******************************************************************************
 * Copyright (c) 2017  Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Kichwa Coders - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * @since 5.4
 */
public class MIGDBShowNewConsoleInfo extends MIInfo {

	private Boolean fIsSet = null;

	public MIGDBShowNewConsoleInfo(MIOutput record) {
		super(record);
		parse();
	}

	protected void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord outr = out.getMIResultRecord();
			if (outr != null) {
				MIResult[] results = outr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("value")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIConst) {
							fIsSet = "on".equals(((MIConst) value).getString()); //$NON-NLS-1$
						}
					}
				}
			}
		}
	}

	/**
	 * Return if child will launch in a new console, or <code>null</code> if
	 * new-console unsupported.
	 */
	public Boolean isSet() {
		return fIsSet;
	}
}
