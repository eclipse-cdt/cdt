/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
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
import org.eclipse.cdt.core.internal.filetype.ResolverModel;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.dialogs.PropertyPage;

/*
 * The preference page used for displaying/editing CDT file
 * type associations for a project
 */
public class CFileTypesPropertyPage extends PropertyPage {
	
	private Button fUseWorkspace;
	private Button fUseProject;
	private CFileTypesPreferenceBlock fPrefsBlock;
	
	public CFileTypesPropertyPage(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite topPane = new Composite(parent, SWT.NONE);

		topPane.setLayout(new GridLayout());
		topPane.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Workspace radio buttons
		
		Composite radioPane = new Composite(topPane, SWT.NONE);

		radioPane.setLayout(new GridLayout());
		radioPane.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fUseWorkspace = new Button(radioPane, SWT.RADIO);
		fUseWorkspace.setText(PreferencesMessages.getString("CFileTypesPropertyPage.useWorkspaceSettings")); //$NON-NLS-1$
		fUseWorkspace.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				fPrefsBlock.setEnabled(false);
			}
		});
		
		fUseProject = new Button(radioPane, SWT.RADIO);
		fUseProject.setText(PreferencesMessages.getString("CFileTypesPropertyPage.useProjectSettings")); //$NON-NLS-1$
		fUseProject.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				fPrefsBlock.setEnabled(true);
			}
		});
		
		// Resolver block
		// TODO: get rid of ResolverModel cast

		IProject			project		= getProject(); 
		ICFileTypeResolver	resolver	= CCorePlugin.getDefault().getFileTypeResolver(project); 
		IResolverModel		model		= CCorePlugin.getDefault().getResolverModel();
		boolean				custom		= ((ResolverModel) model).customProjectResolverExists(project);
		
		Composite blockPane = new Composite(topPane, SWT.NONE);

		blockPane.setLayout(new GridLayout());
		blockPane.setLayoutData(new GridData(GridData.FILL_BOTH));

		fPrefsBlock = new CFileTypesPreferenceBlock(resolver);
		
		fPrefsBlock.createControl(blockPane);
		
		fUseWorkspace.setSelection(!custom);
		fUseProject.setSelection(custom);
		fPrefsBlock.setEnabled(custom);
		
		return topPane;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		fUseWorkspace.setSelection(true);
		fUseProject.setSelection(false);
		
		fPrefsBlock.setEnabled(false);
		
		super.performDefaults();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		IResolverModel model = getResolverModel();
		
		fPrefsBlock.performApply();
		
		if (fUseProject.getSelection()) {
			model.setResolver(getProject(), null);
			model.setResolver(getProject(), fPrefsBlock.getResolver());
		} else {
			model.setResolver(getProject(), null);
		}

		return super.performOk();
	}
	
	private IProject getProject(){
		Object		element	= getElement();
		IProject 	project	= null;
		
		if ((null != element) && (element instanceof IProject)) {
			project = (IProject) element;
		}
		
		return project;
	}

	private IResolverModel getResolverModel() {
		return CCorePlugin.getDefault().getResolverModel();
	}
	
}
