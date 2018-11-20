/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
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
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import java.util.ArrayList;
import java.util.List;

/**
 * GDB/MI data list regiter names response extraction.
 */
public class MIDataListRegisterNamesInfo extends MIInfo {

	String[] names;
	protected int realNameCount = 0;

	public MIDataListRegisterNamesInfo(MIOutput rr) {
		super(rr);
		names = null;
		List<String> aList = new ArrayList<>();
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord outr = out.getMIResultRecord();
			if (outr != null) {
				MIResult[] results = outr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("register-names")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIList) {
							parseRegisters((MIList) value, aList);
						}
					}
				}
			}
		}
		names = aList.toArray(new String[aList.size()]);
	}

	/*
	 * Returns the register names.
	 */
	public String[] getRegisterNames() {

		/*
		 * The expectation is that we return an empty list. The
		 * constructor quarantees this so we are good here.
		 */
		return names;
	}

	private void parseRegisters(MIList list, List<String> aList) {
		MIValue[] values = list.getMIValues();
		for (int i = 0; i < values.length; i++) {
			if (values[i] instanceof MIConst) {
				String str = ((MIConst) values[i]).getCString();

				/* this cannot filter nulls because index is critical in retreival
				 * and index is assigned in the layers above. The MI spec allows
				 * empty returns, for some register names. */
				if (str != null && !str.isEmpty()) {
					realNameCount++;
					aList.add(str);
				} else {
					aList.add(""); //$NON-NLS-1$
				}
			}
		}
	}
}
