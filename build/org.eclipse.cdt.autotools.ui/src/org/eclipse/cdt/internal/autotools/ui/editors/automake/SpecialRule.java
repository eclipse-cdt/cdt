/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

/**
 * Targets that have special meaning for Make.
 */
public abstract class SpecialRule extends Rule implements ISpecialRule {

	String[] prerequisites;

	public SpecialRule(Directive parent, Target target, String[] reqs, Command[] cmds) {
		super(parent, target, cmds);
		prerequisites = reqs.clone();
	}

	public String[] getPrerequisites() {
		return prerequisites.clone();
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
