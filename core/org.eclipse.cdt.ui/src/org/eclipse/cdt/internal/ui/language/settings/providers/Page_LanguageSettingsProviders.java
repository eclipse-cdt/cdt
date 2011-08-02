/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.language.settings.providers;

import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.ui.newui.AbstractPage;
import org.eclipse.cdt.ui.newui.ICPropertyTab;


/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class Page_LanguageSettingsProviders extends AbstractPage {
	private Boolean isLanguageSettingsProvidersEnabled = null;

	@Override
	protected boolean isSingle() {
		return false;
	}
	
	public boolean isLanguageSettingsProvidersEnabled() {
		if (isLanguageSettingsProvidersEnabled==null) {
			isLanguageSettingsProvidersEnabled = LanguageSettingsManager.isLanguageSettingsProvidersEnabled(getProject());
		}
		return isLanguageSettingsProvidersEnabled;
	}

	public void setLanguageSettingsProvidersEnabled(boolean enable) {
		isLanguageSettingsProvidersEnabled = enable;
		forEach(ICPropertyTab.UPDATE,getResDesc());
	}
}
