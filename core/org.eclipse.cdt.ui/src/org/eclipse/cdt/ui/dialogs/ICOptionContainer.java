package org.eclipse.cdt.ui.dialogs;

/***********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Preferences;

public interface ICOptionContainer {

	void updateContainer();

	IProject getProject();

	/**
	 * Returns the preference store.
	 *
	 * @return the preference store, or <code>null</code> if none
	 */
	public Preferences getPreferences();

}
