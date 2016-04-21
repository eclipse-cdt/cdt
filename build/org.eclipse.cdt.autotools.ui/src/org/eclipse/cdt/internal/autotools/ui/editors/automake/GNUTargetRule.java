/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.make.core.makefile.ICommand;

/**
 */
public class GNUTargetRule extends TargetRule {

	String[] orderOnlyPrerequisites;
	boolean doubleColon;

	public GNUTargetRule(Directive parent, Target target, boolean doubleColon, String[] normalPrereqs, String[] orderPrereqs, Command[] commands) {
		super(parent, target, normalPrereqs, commands);
		orderOnlyPrerequisites = orderPrereqs.clone();
		this.doubleColon = doubleColon;
	}

	public boolean isDoubleColon() {
		return doubleColon;
	}

	public String[] getNormalPrerequisites() {
		return getPrerequisites();
	}

	public String[] getOrderOnlyPrerequisites() {
		return orderOnlyPrerequisites.clone();
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(getTarget().toString());
		buffer.append(':');
		String[] reqs = getNormalPrerequisites();
		for (int i = 0; i < reqs.length; i++) {
			buffer.append(' ').append(reqs[i]);
		}
		reqs = getOrderOnlyPrerequisites();
		if (reqs.length > 0) {
			buffer.append(" |"); //$NON-NLS-1$
			for (int i = 0; i < reqs.length; i++) {
				buffer.append(' ').append(reqs[i]);
			}
		}
		buffer.append('\n');
		ICommand[] cmds = getCommands();
		for (int i = 0; i < cmds.length; i++) {
			buffer.append(cmds[i].toString());
		}
		return buffer.toString();
	}
}
