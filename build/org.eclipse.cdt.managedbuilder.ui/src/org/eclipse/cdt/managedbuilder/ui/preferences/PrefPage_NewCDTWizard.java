/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.ui.preferences;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
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
		return Messages.PrefPage_NewCDTWizard_0;
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
	public ICResourceDescription getResDesc() {
		return null;
	}

	@Override
	protected boolean isSingle() {
		return false;
	}

}
