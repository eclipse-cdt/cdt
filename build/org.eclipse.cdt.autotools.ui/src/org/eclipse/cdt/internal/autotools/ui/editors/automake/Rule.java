/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.util.ArrayList;

import org.eclipse.cdt.make.core.makefile.ICommand;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IRule;
import org.eclipse.cdt.make.core.makefile.ITarget;

public abstract class Rule extends Parent implements IRule {

	Target target;

	public Rule(Directive parent, Target tgt) {
		this(parent, tgt, new Command[0]);
	}

	public Rule(Directive parent, Target tgt, Command[] cmds) {
		super(parent);
		target = tgt;
		addDirectives(cmds);
	}

	@Override
	public ICommand[] getCommands() {
		IDirective[] directives = getDirectives();
		ArrayList<IDirective> cmds = new ArrayList<>(directives.length);
		for (int i = 0; i < directives.length; i++) {
			if (directives[i] instanceof ICommand) {
				cmds.add(directives[i]);
			}
		}
		return cmds.toArray(new ICommand[0]);
	}

	@Override
	public ITarget getTarget() {
		return target;
	}

	public void setTarget(Target tgt) {
		target = tgt;
	}

	@Override
	public boolean equals(Object r) {
		if (r instanceof Rule)
			return ((Rule) r).getTarget().equals(getTarget());
		return false;
	}

	@Override
	public int hashCode() {
		return getTarget().hashCode();
	}

}
