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

public class InferenceRule extends Rule {

	public InferenceRule(String target) {
		this(target, new Command[0]);
	}

	public InferenceRule(String target, Command[] commands) {
		super(target, commands);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(target).append(':');
		buffer.append('\n');
		Command[] cmds = getCommands();
		for (int i = 0; i < cmds.length; i++) {
			buffer.append(cmds[i].toString());
		}
		return buffer.toString();
	}

}
