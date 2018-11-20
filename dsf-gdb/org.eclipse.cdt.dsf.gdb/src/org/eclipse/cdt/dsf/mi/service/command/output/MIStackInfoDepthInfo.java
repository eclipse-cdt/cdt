/*******************************************************************************
 * Copyright (c) 2007, 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *     Vladimir Prus (Mentor Graphics) - Use MITuple.getMIValue.
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * -stack-info-depth [max-depth]
 * ^done,depth="12"
 *
 */
public class MIStackInfoDepthInfo extends MIInfo {

	int depth = 0;

	public MIStackInfoDepthInfo(MIOutput record) {
		super(record);
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIValue value = rr.getField("depth"); //$NON-NLS-1$
				if (value instanceof MIConst) {
					String str = ((MIConst) value).getString();
					try {
						depth = Integer.parseInt(str.trim());
					} catch (NumberFormatException e) {
					}
				}
			}
		}
	}

	public int getDepth() {
		return depth;
	}
}
