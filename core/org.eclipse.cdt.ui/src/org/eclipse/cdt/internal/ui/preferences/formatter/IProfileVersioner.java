/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
