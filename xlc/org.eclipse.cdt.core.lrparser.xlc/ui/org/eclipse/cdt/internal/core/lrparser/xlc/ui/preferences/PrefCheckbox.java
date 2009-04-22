/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.lrparser.xlc.ui.preferences;

import org.eclipse.cdt.core.lrparser.xlc.preferences.XlcLanguagePreferences;
import org.eclipse.cdt.core.lrparser.xlc.preferences.XlcPref;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


/**
 * A simple wrapper for a checkbox.
 *
 */
class PrefCheckbox {
	
	private final XlcPref key;
	private final Button button;
	
	PrefCheckbox(Composite parent, XlcPref prefKey, String label) {
		button = ControlFactory.createCheckBox(parent, label);
		key = prefKey;
	}
	
	public XlcPref getKey() {
		return key;
	}
	
	public void setSelection(boolean selection) {
		button.setSelection(selection);
	}
	
	public boolean getSelection() {
		return button.getSelection();
	}
	
	public void setDefault() {
		setSelection(Boolean.valueOf(XlcLanguagePreferences.getDefaultPreference(key)));
	}
}