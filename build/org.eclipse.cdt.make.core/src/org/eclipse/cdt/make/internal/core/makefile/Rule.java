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

public abstract class Rule extends Statement {

	Command[] commands;
	String target;

	public Rule(String target) {
		this(target, new Command[0]);
	}

	public Rule(String tgt, Command[] cmds) {
		target = tgt;
		commands = cmds;
	}

	public Command[] getCommands() {
		return commands;
	}

	public void setCommand(Command[] cmds) {
		commands = cmds;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String tgt) {
		target = tgt;
	}

	public void addCommand(Command cmd) {
		Command[] newCmds = new Command[commands.length + 1];
		System.arraycopy(commands, 0, newCmds, 0, commands.length);
		newCmds[commands.length] = cmd;
		commands = newCmds;
	}

	public void addCommands(Command[] cmds) {
		Command[] newCmds = new Command[commands.length + cmds.length];
		System.arraycopy(commands, 0, newCmds, 0, commands.length);
		System.arraycopy(cmds, 0, newCmds, commands.length, cmds.length);
		commands = newCmds;
	}

	public boolean equals(Rule r) {
		return r.getTarget().equals(getTarget());
	}

}
