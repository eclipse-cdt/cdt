/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
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
package org.eclipse.cdt.internal.core.linux;

import org.eclipse.cdt.core.IProcessInfo;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ProcessInfo implements IProcessInfo {

	int pid;
	String name;

	public ProcessInfo(String pidString, String name) {
		try {
			pid = Integer.parseInt(pidString);
		} catch (NumberFormatException e) {
		}
		this.name = name;
	}

	public ProcessInfo(int pid, String name) {
		this.pid = pid;
		this.name = name;
	}

	/**
	 * @see org.eclipse.cdt.core.IProcessInfo#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see org.eclipse.cdt.core.IProcessInfo#getPid()
	 */
	@Override
	public int getPid() {
		return pid;
	}

}
