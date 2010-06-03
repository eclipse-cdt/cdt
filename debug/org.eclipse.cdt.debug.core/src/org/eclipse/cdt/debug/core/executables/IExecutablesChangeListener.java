/*******************************************************************************
 * Copyright (c) 2008, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.executables;

import java.util.EventListener;
import java.util.List;

public interface IExecutablesChangeListener extends EventListener {

	/**
	 * Called whenever the list of executables in the workspace changes, e.g. a 
	 * project was opened/closed/created/deleted
	 * @since 7.0
	 */
	public void executablesListChanged();

	/**
	 * Called whenever some executables have changed, e.g. when a project is rebuilt or
	 * cleaned.  The content may have changed for example, so the list of source files
	 * may be different.
	 * @param executables
	 * @since 7.0
	 */
	public void executablesChanged(List<Executable> executables);
}