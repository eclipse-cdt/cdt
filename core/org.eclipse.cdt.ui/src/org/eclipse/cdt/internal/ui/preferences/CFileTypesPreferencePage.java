/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.filetype.ICFileTypeResolver;
import org.eclipse.cdt.core.filetype.IResolverModel;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/*
 * The preference page used for displaying/editing CDT file
 * type associations for the workspace
 */
public class CFileTypesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private CFileTypesPreferenceBlock fPrefsBlock;

	public CFileTypesPreferencePage() {
		setDescription(PreferencesMessages.getString("CFileTypesPreferencePage.description")); //$NON-NLS-1$
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite topPane = new Composite(parent, SWT.NONE);

		topPane.setLayout(new GridLayout());
		topPane.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		ICFileTypeResolver resolver = CCorePlugin.getDefault().getFileTypeResolver(null); 
		fPrefsBlock = new CFileTypesPreferenceBlock(resolver);

		WorkbenchHelp.setHelp( topPane, ICHelpContextIds.FILE_TYPES_PREF_PAGE );
		return fPrefsBlock.createControl(topPane);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		IResolverModel model = getResolverModel();
		
		model.setResolver(null);
		fPrefsBlock.setResolver(model.getResolver());
		
		super.performDefaults();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		IResolverModel model = getResolverModel();
		
		if (fPrefsBlock.performOk()) {
			model.setResolver(fPrefsBlock.getResolver());
		}
		
		return super.performOk();
	}

	private IResolverModel getResolverModel() {
		return CCorePlugin.getDefault().getResolverModel();
	}
}
