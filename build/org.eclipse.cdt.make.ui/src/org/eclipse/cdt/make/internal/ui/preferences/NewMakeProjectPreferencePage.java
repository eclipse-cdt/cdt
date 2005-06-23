/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class NewMakeProjectPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, ICOptionContainer {
	
	private MakeProjectOptionBlock fOptionBlock;

	public NewMakeProjectPreferencePage() {
		setPreferenceStore(MakeUIPlugin.getDefault().getPreferenceStore());
		setDescription(MakeUIPlugin.getResourceString("MakePreferencePage.description")); //$NON-NLS-1$
		fOptionBlock = new MakeProjectOptionBlock();
	}

	
	public void setContainer(IPreferencePageContainer preferencePageContainer) {
		super.setContainer(preferencePageContainer);
		fOptionBlock.setOptionContainer(this);
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
		return MakeCorePlugin.getDefault().getPluginPreferences();
	}

}
