/*******************************************************************************
 * Copyright (c) 2005, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launch.ui;

import org.eclipse.debug.core.ILaunchConfiguration;


public class CMainAttachTab extends CMainTab {

	public CMainAttachTab() {
		super(false);
	}
	
	@Override
	public boolean isValid(ILaunchConfiguration config) {
		if (super.isValid(config) == false) {
			String name = fProgText.getText().trim();
			if (name.length() == 0) {  // allow no program for attach config.
				setErrorMessage(null);
				return true; 
			}
			return false;
		}
		return true;
	}
}
