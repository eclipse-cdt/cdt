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
import org.eclipse.cdt.make.core.makefile.IRule;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.ITarget;

public abstract class Rule extends Parent implements IRule {

	ITarget target;

	public Rule(ITarget tgt) {
		this(tgt, new Command[0]);
	}

	public Rule(ITarget tgt, ICommand[] cmds) {
		target = tgt;
		addStatements(cmds);
	}

	public ICommand[] getCommands() {
		IDirective[] stmts = getStatements();
		ICommand[] cmds = new ICommand[stmts.length];
		System.arraycopy(stmts, 0, cmds, 0, stmts.length);
		return cmds;
	}

	public ITarget getTarget() {
		return target;
	}

	public void setTarget(ITarget tgt) {
		target = tgt;
	}

	public boolean equals(Rule r) {
		return r.getTarget().equals(getTarget());
	}

}
