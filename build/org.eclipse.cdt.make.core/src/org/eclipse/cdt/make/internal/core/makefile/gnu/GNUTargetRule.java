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
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import org.eclipse.cdt.make.core.makefile.ICommand;
import org.eclipse.cdt.make.core.makefile.ITarget;
import org.eclipse.cdt.make.internal.core.makefile.TargetRule;

/**
 */

public class GNUTargetRule extends TargetRule {

	String[] orderOnlyPrerequisites;
	boolean doubleColon;

	public GNUTargetRule(ITarget target, boolean double_colon, String[] normal_prereqs, String[] order_prereqs, ICommand[] commands) {
		super(target, normal_prereqs, commands);
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
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getTarget().toString());
		buffer.append(':');
		String[] reqs = getNormalPrerequisites();
		for (int i = 0; i < reqs.length; i++) {
			buffer.append(' ').append(reqs[i]);
		}
		reqs = getOrderOnlyPrerequisites();
		if (reqs.length > 0) {
			buffer.append(" |");
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
