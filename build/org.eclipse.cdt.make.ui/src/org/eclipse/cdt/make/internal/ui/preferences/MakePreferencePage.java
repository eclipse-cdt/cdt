/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.preferences;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeProjectOptionBlock;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

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

public class MakePreferencePage extends PreferencePage implements IWorkbenchPreferencePage, ICOptionContainer {
	
	private MakeProjectOptionBlock fOptionBlock;

	public MakePreferencePage() {
		setPreferenceStore(MakeUIPlugin.getDefault().getPreferenceStore());
		setDescription(MakeUIPlugin.getResourceString("MakePreferencePage.description")); //$NON-NLS-1$
		fOptionBlock = new MakeProjectOptionBlock(this);
	}

	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		//		WorkbenchHelp.setHelp(parent, ICMakeHelpContextIds.PROJECT_PROPERTY_PAGE);
	}

	protected Control createContents(Composite parent) {
		return fOptionBlock.createContents(parent);
	}

	
	public void init(IWorkbench workbench) {
	}

	public boolean performOk() {
		boolean ok = fOptionBlock.performApply(null);
		MakeCorePlugin.getDefault().savePluginPreferences();
		return ok;
	}

	/**
	 * @see DialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		fOptionBlock.setVisible(visible);
	}

	public void updateContainer() {
		fOptionBlock.update();
		boolean ok = fOptionBlock.isValid();
		if (!ok) {
			setErrorMessage(fOptionBlock.getErrorMessage());
		}
		if (ok) {
			setErrorMessage(null);
		}
		setValid(ok);
	}

	public IProject getProject() {
		return null;
	}

	public boolean isValid() {
		updateContainer();
		return super.isValid();
	}

	protected void performDefaults() {
		fOptionBlock.performDefaults();
		super.performDefaults();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getPreferences()
	 */
	public Preferences getPreferences() {
		return MakeUIPlugin.getDefault().getPluginPreferences();
	}

}