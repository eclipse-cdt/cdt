/*******************************************************************************
 * Copyright (c) 2008 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.executables;

import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.core.executables.IExecutablesChangeEvent;
import org.eclipse.core.runtime.PlatformObject;

public class ExecutablesChangeEvent extends PlatformObject implements IExecutablesChangeEvent {

	private Executable[] oldExecutables;
	private Executable[] newExecutables;

	public ExecutablesChangeEvent(Executable[] oldList, Executable[] newList) {
		oldExecutables = oldList;
		newExecutables = newList;
	}

	public Executable[] getCurrentExecutables() {
		return newExecutables;
	}

	public Executable[] getPreviousExecutables() {
		return oldExecutables;
	}

}
