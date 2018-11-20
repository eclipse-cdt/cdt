/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import org.eclipse.cdt.internal.ui.preferences.formatter.ProfileManager.CustomProfile;

public interface IProfileVersioner {

	public int getFirstVersion();

	public int getCurrentVersion();

	public String getProfileKind();

	/**
	 * Update the <code>profile</code> to the
	 * current version number
	 */
	public void update(CustomProfile profile);

}
