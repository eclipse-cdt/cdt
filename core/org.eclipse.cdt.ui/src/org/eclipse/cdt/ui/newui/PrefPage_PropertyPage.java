/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.newui;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.newui.AbstractPrefPage;
import org.eclipse.cdt.ui.newui.ICPropertyTab;

public class PrefPage_PropertyPage extends AbstractPrefPage {

	protected String getHeader() {
		return UIMessages.getString("PrefPage_PropertyPage.0"); //$NON-NLS-1$
	}

	/*
	 * All affected settings are stored in preferences.
	 * Tabs are responsible for saving, after OK signal.
	 * No need to affect Project Description somehow.
	 */
	public boolean performOk() {
		forEach(ICPropertyTab.OK, null);
		return true;
	}
	public ICResourceDescription getResDesc() { return null; } 
	protected boolean isSingle() { return true; }

}
