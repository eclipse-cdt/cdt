/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;

/**
 * Code coloring preference page.
 * <p>
 * Note: Must be public since it is referenced from plugin.xml
 * </p>
 * 
 * @since 4.0
 */
public class CEditorColoringPreferencePage extends AbstractConfigurationBlockPreferencePage {
	@Override
	protected String getHelpId() {
		return ICHelpContextIds.C_EDITOR_COLORS_PREF_PAGE;
	}

	@Override
	protected void setDescription() {
		String description= PreferencesMessages.CEditorPreferencePage_colors; 
		setDescription(description);
	}
	
	@Override
	protected Label createDescriptionLabel(Composite parent) {
		return null;
	}
	
	@Override
	protected void setPreferenceStore() {
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected IPreferenceConfigurationBlock createConfigurationBlock(OverlayPreferenceStore overlayPreferenceStore) {
		return new CEditorColoringConfigurationBlock(overlayPreferenceStore);
	}
}
