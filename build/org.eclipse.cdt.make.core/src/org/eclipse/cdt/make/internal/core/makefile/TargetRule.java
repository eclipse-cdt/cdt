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
package org.eclipse.cdt.make.internal.core.makefile;

/**
 * Makefile : ( statement ) *
 * statement :   rule | macro_definition | comments | empty
 * rule :  inference_rule | target_rule
 * inference_rule : target ':' <nl> ( <tab> command <nl> ) +
 * target_rule : target [ ( target ) * ] ':' [ ( prerequisite ) * ] [ ';' command ] <nl> 
                 [ ( <tab> prefix_command command ) * ]
 * macro_definition : string '=' (string)* 
 * comments : '#' (string) *
 * empty : <nl>
 * command : string <nl>
 * target : string
 * prefix_command : '-' | '@' | '+'
 * internal_macro :  "$<" | "$*" | "$@" | "$?" | "$%" 
 */

public class TargetRule extends Rule {

	String[] dependencies;

	public TargetRule(String target) {
		this(target, new String[0]);
	}

	public TargetRule(String target, String[] pres) {
		this(target, pres, new Command[0]);
	}

	public TargetRule(String target, String[] deps, Command[] commands) {
		super(target, commands);
		dependencies = deps;
	}

	public String[] getDependencies() {
		return dependencies;
	}

	public void setDependecies(String[] reqs) {
		dependencies = reqs;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(target);
		if (buffer.length() > 0) {
			buffer.append(": ");
			for (int i = 0; i < dependencies.length; i++) {
				buffer.append(dependencies[i]).append(' ');
			}
			buffer.append('\n');
			Command[] cmds = getCommands();
			for (int i = 0; i < cmds.length; i++) {
				buffer.append(cmds[i].toString());
			}
		}
		return buffer.toString();
	}
}
