/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.output;

/**
 * GDB/MI var-list-children
 * -var-list-children var2
 *  ^done,numchild="6",children={child={name="var2.0",exp="0",numchild="0",type="char"},child={name="var2.1",exp="1",numchild="0",type="char"},child={name="var2.2",exp="2",numchild="0",type="char"},child={name="var2.3",exp="3",numchild="0",type="char"},child={name="var2.4",exp="4",numchild="0",type="char"},child={name="var2.5",exp="5",numchild="0",type="char"}}
 *
 */
public class MIChild {

	String name = "";
	String type = "";
	int numchild;


	public MIChild(String n, int num, String t) {
		name = n;
		numchild = num;
		type = t;
	}

	public MIChild(MITuple tuple) {
		parse(tuple);
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public int getNumChild() {
		return numchild;
	}

	void parse(MITuple tuple) {
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = "";
			if (value != null && value instanceof MIConst) {
				str = ((MIConst)value).getCString();
			}

			if (var.equals("numchild")) {
				try {
					numchild = Integer.parseInt(str.trim());
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("name")) {
				name = str;
			} else if (var.equals("type")) {
				type = str;
			}
		}
	}
}
