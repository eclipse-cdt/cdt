/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
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

import org.eclipse.cdt.make.core.makefile.ICommand;
import org.eclipse.cdt.make.core.makefile.ISpecialRule;

/**
 * Targets that have special meaning for Make.
 */
public abstract class SpecialRule extends Rule implements ISpecialRule {

	String[] prerequisites;

	public SpecialRule(Directive parent, Target target, String[] reqs, Command[] cmds) {
		super(parent, target, cmds);
		prerequisites = reqs.clone();
	}

	@Override
	public String[] getPrerequisites() {
		return prerequisites.clone();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(target).append(':');
		String[] reqs = getPrerequisites();
		for (int i = 0; i < reqs.length; i++) {
			sb.append(' ').append(reqs[i]);
		}
		sb.append('\n');
		ICommand[] cmds = getCommands();
		for (int i = 0; i < cmds.length; i++) {
			sb.append(cmds[i]);
		}
		return sb.toString();
	}

}
