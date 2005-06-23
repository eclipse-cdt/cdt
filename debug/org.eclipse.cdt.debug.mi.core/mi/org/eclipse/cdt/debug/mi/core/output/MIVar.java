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
 * GDB/MI var-list-children
 * -var-list-children var2
 *  ^done,numchild="6",children={child={name="var2.0",exp="0",numchild="0",type="char"},child={name="var2.1",exp="1",numchild="0",type="char"},child={name="var2.2",exp="2",numchild="0",type="char"},child={name="var2.3",exp="3",numchild="0",type="char"},child={name="var2.4",exp="4",numchild="0",type="char"},child={name="var2.5",exp="5",numchild="0",type="char"}}
 *
 */
public class MIVar {

	String name = ""; //$NON-NLS-1$
	String type = ""; //$NON-NLS-1$
	String exp = ""; //$NON-NLS-1$
	int numchild;


	public MIVar(String n, int num, String t) {
		name = n;
		numchild = num;
		type = t;
	}

	public MIVar(MITuple tuple) {
		parse(tuple);
	}

	public String getVarName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public int getNumChild() {
		return numchild;
	}

	public String getExp() {
		return exp;
	}

	void parse(MITuple tuple) {
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = ""; //$NON-NLS-1$
			if (value != null && value instanceof MIConst) {
				str = ((MIConst)value).getCString();
			}

			if (var.equals("numchild")) { //$NON-NLS-1$
				try {
					numchild = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("name")) { //$NON-NLS-1$
				name = str;
			} else if (var.equals("type")) { //$NON-NLS-1$
				type = str;
			} else if (var.equals("exp")) { //$NON-NLS-1$
				exp = str;
			}
		}
	}
}
