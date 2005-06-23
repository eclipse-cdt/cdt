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
package org.eclipse.cdt.make.internal.core.makefile;

import org.eclipse.cdt.make.core.makefile.ISpecialRule;
import org.eclipse.cdt.make.core.makefile.ICommand;

/**
 * Targets that have special meaning for Make.
 */
public abstract class SpecialRule extends Rule implements ISpecialRule {

	String[] prerequisites;

	public SpecialRule(Directive parent, Target target, String[] reqs, Command[] cmds) {
		super(parent, target, cmds);
		prerequisites = reqs;
	}

	public String[] getPrerequisites() {
		return prerequisites;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
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
