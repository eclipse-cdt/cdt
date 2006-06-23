/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
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
 * GDB/MI stack info depth parsing.
 */
public class MIStackInfoDepthInfo extends MIInfo {

	int depth;

	public MIStackInfoDepthInfo(MIOutput out) {
		super(out);
		parse();
	}

	public int getDepth() {
		return depth;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("depth")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							String str = ((MIConst)val).getCString();
							try {
								depth = Integer.parseInt(str.trim());
							} catch (NumberFormatException e) {
							}
						}
					}
				}
			}
		}
	}
}
