/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.tests;

import org.eclipse.core.runtime.NullProgressMonitor;

public class SysoutProgressMonitor extends NullProgressMonitor {

	@Override
	public void beginTask(String name, int totalWork) {
		if (name.length() > 0) {
			System.out.println(name);
			System.out.flush();
		}
	}

	@Override
	public void subTask(String name) {
		if (name.length() > 0) {
			System.out.println(name);
			System.out.flush();
		}
	}

	@Override
	public void setTaskName(String name) {
		if (name.length() > 0) {
			System.out.println(name);
			System.out.flush();
		}
	}

}
