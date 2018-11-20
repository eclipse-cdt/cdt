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
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import org.eclipse.cdt.make.core.makefile.ICommand;
import org.eclipse.cdt.make.internal.core.makefile.Command;
import org.eclipse.cdt.make.internal.core.makefile.Directive;
import org.eclipse.cdt.make.internal.core.makefile.Target;
import org.eclipse.cdt.make.internal.core.makefile.TargetRule;

/**
 */
public class GNUTargetRule extends TargetRule {

	String[] orderOnlyPrerequisites;
	boolean doubleColon;

	public GNUTargetRule(Directive parent, Target target, boolean double_colon, String[] normal_prereqs,
			String[] order_prereqs, Command[] commands) {
		super(parent, target, normal_prereqs, commands);
		orderOnlyPrerequisites = order_prereqs;
		doubleColon = double_colon;
	}

	public boolean isDoubleColon() {
		return doubleColon;
	}

	public String[] getNormalPrerequisites() {
		return getPrerequisites();
	}

	public String[] getOrderOnlyPrerequisites() {
		return orderOnlyPrerequisites;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
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
