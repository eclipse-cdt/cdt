/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import org.eclipse.cdt.make.internal.core.makefile.Parent;

public abstract class Conditional extends Parent {

	String cond;
	String arg1;
	String arg2;

	public Conditional(String conditional) {
		cond = conditional;
		parse();
	}

	public Conditional() {
		this("", "", "");
	}

	public Conditional(String conditional, String argument1, String argument2) {
		arg1 = argument1;
		arg2 = argument2;
		cond = conditional;
	}


	public String getConditional() {
		return cond;
	}

	public String getArg1() {
		return arg1;
	}

	public String getArg2() {
		return arg2;
	}

	/**
	 * Formats of the contional.
	 * ifeq (ARG1, ARG2)
	 * ifeq 'ARG1' 'ARG2'
	 * ifeq "ARG1" "ARG2"
	 * ifeq "ARG1" 'ARG2'
	 * ifeq 'ARG1' "ARG2"
	 */
	void parse() {
		String line = getConditional().trim();

		char terminal = line.charAt(0) == '(' ? ',' : line.charAt(0);
 
		if (line.length() < 5 && terminal != ',' && terminal != '"' && terminal != '\'') {
			arg1 = arg2 = "";
			return;
		}
 
		// Find the end of the first string.
		int count = 0;
		// For the (ARG1, ARG2) format.
		if (terminal == ',') {
			int paren = 0;
			for (count = 0; count < line.length(); count++) {
				char ch = line.charAt(count);
				if (ch == '(') {
					paren++;
				} else if (ch == ')') {
					paren--;
				} else if (ch == terminal && paren <= 0) {
					break;
				}
			}
		} else {
			for (count = 1; count < line.length(); count++) {
				if (line.charAt(count) == terminal) {
					break;
				}
			}
		}

		arg1 = line.substring(1, count);
 
		/* Find the start of the second string.  */
		line = line.substring(count + 1).trim();
 
		terminal = terminal == ',' ? ')' : line.charAt(0);
		if (terminal != ')' && terminal != '"' && terminal != '\'') {
			arg2 = "";
			return;
		}
 
		count = 0;
		/* Find the end of the second string.  */
		if (terminal == ')') {
			int paren = 0;
			for (count = 0; count < line.length(); count++) {
				char ch = line.charAt(count);
				if (ch == '(') {
					paren++;
				} else if (ch == ')') {
					paren--;
				} else if (ch == terminal && paren <= 0) {
					break;
				}
			}
		} else {
			for (count = 1; count < line.length(); count++) {
				if (line.charAt(count) == terminal) {
					break;
				}
			}
		}
		arg2 = line.substring(1, count);
	}
}
