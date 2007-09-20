/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.xlc.ui.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.cdt.managedbuilder.xlc.ui.Messages;
import org.eclipse.cdt.managedbuilder.xlc.ui.XLCUIPlugin;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class XLCompilerPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public XLCompilerPreferencePage() {
		super(FLAT);
		setPreferenceStore(XLCUIPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.XLCompilerPreferencePage_0);
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(new DirectoryFieldEditor(PreferenceConstants.P_XL_COMPILER_ROOT, 
				Messages.XLCompilerPreferencePage_1, getFieldEditorParent()));
		
		String[][] versionEntries = {{PreferenceConstants.P_XL_COMPILER_VERSION_8_NAME, "v8.0"},  //$NON-NLS-1$
									   {PreferenceConstants.P_XL_COMPILER_VERSION_9_NAME, "v9.0"}};  //$NON-NLS-1$
		
		addField(new ComboFieldEditor(PreferenceConstants.P_XLC_COMPILER_VERSION,
				Messages.XLCompilerPreferencePage_2, versionEntries, getFieldEditorParent()));
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}