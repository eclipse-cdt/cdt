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

import org.eclipse.cdt.make.core.makefile.ICommand;
import org.eclipse.cdt.make.core.makefile.IInferenceRule;
import org.eclipse.cdt.make.core.makefile.IRule;
import org.eclipse.cdt.make.core.makefile.ITarget;

public abstract class Rule extends Statement implements IRule, IInferenceRule {

	ICommand[] commands;
	ITarget target;

	public Rule(ITarget tgt) {
		this(tgt, new Command[0]);
	}

	public Rule(ITarget tgt, ICommand[] cmds) {
		target = tgt;
		commands = cmds;
	}

	public ICommand[] getCommands() {
		return commands;
	}

	public void setCommand(ICommand[] cmds) {
		commands = cmds;
	}

	public ITarget getTarget() {
		return target;
	}

	public void setTarget(ITarget tgt) {
		target = tgt;
	}

	public void addCommand(ICommand cmd) {
		ICommand[] newCmds = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCmds, 0, commands.length);
		newCmds[commands.length] = cmd;
		commands = newCmds;
	}

	public void addCommands(ICommand[] cmds) {
		ICommand[] newCmds = new ICommand[commands.length + cmds.length];
		System.arraycopy(commands, 0, newCmds, 0, commands.length);
		System.arraycopy(cmds, 0, newCmds, commands.length, cmds.length);
		commands = newCmds;
	}

	public boolean equals(Rule r) {
		return r.getTarget().equals(getTarget());
	}

}
