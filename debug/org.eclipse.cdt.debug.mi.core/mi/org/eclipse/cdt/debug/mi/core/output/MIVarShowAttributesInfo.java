/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
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
 * GDB/MI var-show-attributes
 */
public class MIVarShowAttributesInfo extends MIInfo {

	String attr = ""; //$NON-NLS-1$

	public MIVarShowAttributesInfo(MIOutput record) {
		super(record);
		parse();
	}

	public String getAttributes () {
		return attr;
	}

	public boolean isEditable() {
		return attr.equals("editable"); //$NON-NLS-1$
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("attr")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIConst) {
							attr = ((MIConst)value).getString();
						}
					}
				}
			}
		}
	}
}
