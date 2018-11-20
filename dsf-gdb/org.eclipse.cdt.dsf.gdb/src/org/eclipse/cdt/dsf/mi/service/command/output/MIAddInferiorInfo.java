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
 *   -add-inferior
 *   ^done,inferior="i2"
 *
 *   @since 4.0
 */
public class MIAddInferiorInfo extends MIInfo {

	private String fGroupId;

	public MIAddInferiorInfo(MIOutput record) {
		super(record);
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					MIValue resultVal = results[i].getMIValue();
					String str = ""; //$NON-NLS-1$
					if (resultVal instanceof MIConst) {
						str = ((MIConst) resultVal).getString();
					}

					if (var.equals("inferior")) { //$NON-NLS-1$
						fGroupId = str;
					}
				}
			}
		}
	}

	public String getGroupId() {
		return fGroupId;
	}
}
