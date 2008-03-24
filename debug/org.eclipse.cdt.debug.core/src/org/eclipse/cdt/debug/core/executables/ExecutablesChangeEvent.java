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
package org.eclipse.cdt.debug.core.executables;

import java.util.ArrayList;

import org.eclipse.core.runtime.PlatformObject;

public class ExecutablesChangeEvent extends PlatformObject implements IExecutablesChangeEvent {

	private Executable[] oldExecutables;
	private Executable[] newExecutables;

	public ExecutablesChangeEvent(ArrayList<Executable> oldList, ArrayList<Executable> newList) {
		oldExecutables = oldList.toArray(new Executable[oldList.size()]);
		newExecutables = newList.toArray(new Executable[newList.size()]);
	}

	public Executable[] getCurrentExecutables() {
		return newExecutables;
	}

	public Executable[] getPreviousExecutables() {
		return oldExecutables;
	}

}
