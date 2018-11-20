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
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(getTarget().toString()).append(":\n"); //$NON-NLS-1$
		ICommand[] cmds = getCommands();
		for (int i = 0; i < cmds.length; i++) {
			buffer.append(cmds[i].toString());
		}
		return buffer.toString();
	}

}
