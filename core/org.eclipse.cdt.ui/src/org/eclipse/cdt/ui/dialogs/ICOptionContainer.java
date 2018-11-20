/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

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
