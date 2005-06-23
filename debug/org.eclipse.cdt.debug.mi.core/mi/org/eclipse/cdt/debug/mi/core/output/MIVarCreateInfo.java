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
 * GDB/MI var-create.
 * -var-create "-" * buf3
 * ^done,name="var1",numchild="6",type="char [6]"
 */
public class MIVarCreateInfo extends MIInfo {

	String name = ""; //$NON-NLS-1$
	int numChild;
	String type = ""; //$NON-NLS-1$
	MIVar child;

	public MIVarCreateInfo(MIOutput record) {
		super(record);
		parse();
	}

	public MIVar getMIVar() {
		if (child == null) {
			child = new MIVar(name, numChild, type);
		}
		return child;
	}

	void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					MIValue value = results[i].getMIValue();
					String str = ""; //$NON-NLS-1$
					if (value instanceof MIConst) {
						str = ((MIConst)value).getString();
					}

					if (var.equals("name")) { //$NON-NLS-1$
						name = str;
					} else if (var.equals("numchild")) { //$NON-NLS-1$
						try {
							numChild = Integer.parseInt(str.trim());
						} catch (NumberFormatException e) {
						}
					} else if (var.equals("type")) { //$NON-NLS-1$
						type = str;
					}
				}
			}
		}
	}
}
