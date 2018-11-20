/*******************************************************************************
 * Copyright (c) 2005, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launch.ui;

import org.eclipse.debug.core.ILaunchConfiguration;

@Deprecated
public class CMainAttachTab extends CMainTab {

	public CMainAttachTab() {
		super(false);
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {
		if (super.isValid(config) == false) {
			String name = fProgText.getText().trim();
			if (name.length() == 0) { // allow no program for attach config.
				setErrorMessage(null);
				return true;
			}
			return false;
		}
		return true;
	}
}
