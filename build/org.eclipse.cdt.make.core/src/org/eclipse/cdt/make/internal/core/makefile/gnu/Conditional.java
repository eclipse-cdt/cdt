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
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import org.eclipse.cdt.make.core.makefile.gnu.IConditional;
import org.eclipse.cdt.make.internal.core.makefile.Directive;
import org.eclipse.cdt.make.internal.core.makefile.Parent;

public abstract class Conditional extends Parent implements IConditional {

    private static final String EMPTY = ""; //$NON-NLS-1$
	String cond;
	String arg1;
	String arg2;

	public Conditional(Directive parent, String conditional) {
		super(parent);
		cond = conditional;
		parse();
	}

	public Conditional(Directive parent) {
		this(parent, EMPTY, EMPTY, EMPTY);
	}

	public Conditional(Directive parent, String conditional, String argument1, String argument2) {
		super(parent);
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

	public boolean isIfdef() {
		return false;
	}

	public boolean isIfndef() {
		return false;
	}

	public boolean isIfeq() {
		return false;
	}

	public boolean isIfneq() {
		return false;
	}

	public boolean isElse() {
		return false;
	}

	public boolean isEndif() {
		return false;
	}

	/**
	 * Formats of the conditional string.
	 * ifeq (ARG1, ARG2)
	 * ifeq 'ARG1' 'ARG2'
	 * ifeq "ARG1" "ARG2"
	 * ifeq "ARG1" 'ARG2'
	 * ifeq 'ARG1' "ARG2"
	 */
	protected void parse() {
		String line = getConditional().trim();

		char terminal = line.charAt(0) == '(' ? ',' : line.charAt(0);
 
		if (line.length() < 5 && terminal != ',' && terminal != '"' && terminal != '\'') {
			arg1 = arg2 = EMPTY;
			return;
		}
 
		// Find the end of the first string.
		int count = 0;
		// For the (ARG1, ARG2) format.

		// get the first ARG1
		if (terminal == ',') {
			int paren = 0;
			for (count = 1; count < line.length(); count++) {
				char ch = line.charAt(count);
				if (ch == '(') {
					paren++;
				} else if (ch == ')') {
					paren--;
				}
				if (ch == terminal && paren <= 0) {
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
			arg2 = EMPTY;
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
				}
				if (ch == terminal && paren <= 0) {
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
		arg2 = line.substring(0, count);
	}
}
