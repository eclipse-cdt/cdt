/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * The page for setting the editor options.
 */
public final class SmartTypingPreferencePage extends AbstractConfigurationBlockPreferencePage {
	@Override
	protected String getHelpId() {
		return ICHelpContextIds.C_EDITOR_TYPING_PAGE;
	}

	@Override
	protected void setDescription() {
		String description = PreferencesMessages.CEditorPreferencePage_typing_tabTitle;
		setDescription(description);
	}

	@Override
	protected void setPreferenceStore() {
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected Label createDescriptionLabel(Composite parent) {
		return null; // no description for new look.
	}

	@Override
	protected IPreferenceConfigurationBlock createConfigurationBlock(OverlayPreferenceStore overlayPreferenceStore) {
		return new SmartTypingConfigurationBlock(overlayPreferenceStore);
	}
}
