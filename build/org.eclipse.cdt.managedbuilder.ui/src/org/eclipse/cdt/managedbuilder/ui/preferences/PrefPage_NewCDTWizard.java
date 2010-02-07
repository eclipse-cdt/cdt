/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.ui.preferences;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.ui.properties.Messages;
import org.eclipse.cdt.ui.newui.AbstractPrefPage;
import org.eclipse.cdt.ui.newui.ICPropertyTab;

/**
 * @since 5.1
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class PrefPage_NewCDTWizard extends AbstractPrefPage {

	@Override
	protected String getHeader() {
		return Messages.getString("PrefPage_NewCDTWizard.0"); //$NON-NLS-1$
	}

	/*
	 * All affected settings are stored in preferences.
	 * Tabs are responsible for saving, after OK signal.
	 * No need to affect Project Description somehow.
	 */
	@Override
	public boolean performOk() {
		forEach(ICPropertyTab.OK, null);
		return true;
	}
	@Override
	public ICResourceDescription getResDesc() { return null; } 
	@Override
	protected boolean isSingle() { return false; }

}
