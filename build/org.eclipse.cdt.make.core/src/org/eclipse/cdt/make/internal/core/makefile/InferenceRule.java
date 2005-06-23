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

import org.eclipse.cdt.make.core.makefile.ICommand;

public class InferenceRule extends Rule {

	public InferenceRule(Directive parent, Target target) {
		this(parent, target, new Command[0]);
	}

	public InferenceRule(Directive parent, String tgt, Command[] cmds) {
		this(parent, new Target(tgt), cmds);
	}

	public InferenceRule(Directive parent, Target target, Command[] cmds) {
		super(parent, target, cmds);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getTarget().toString()).append(":\n"); //$NON-NLS-1$
		ICommand[] cmds = getCommands();
		for (int i = 0; i < cmds.length; i++) {
			buffer.append(cmds[i].toString());
		}
		return buffer.toString();
	}

}
