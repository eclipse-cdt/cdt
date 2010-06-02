/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

abstract public class PreferenceScopeBlock {
	private Button fUseProjectSettings;
	private Button fStoreWithProject;
	private String fPrefPageID;
	private Link fLink;
	
	public PreferenceScopeBlock(String linkedPrefPageID) {
		fPrefPageID= linkedPrefPageID;
	}
	
	public void createControl(final Composite parent) {
		Composite group= ControlFactory.createComposite(parent,2);
		GridLayout layout = (GridLayout)group.getLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		GridData gd = (GridData) group.getLayoutData();
		gd.horizontalIndent= 0; 

		fUseProjectSettings= ControlFactory.createCheckBox(group, DialogsMessages.PreferenceScopeBlock_enableProjectSettings);
		
		Composite two= ControlFactory.createComposite(group, 1);
		two.setLayout(new TabFolderLayout());
		fStoreWithProject= ControlFactory.createCheckBox(two, DialogsMessages.PreferenceScopeBlock_storeWithProject);
		
		SelectionListener sl= new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
				onPreferenceScopeChange();
			}
		};
		fUseProjectSettings.addSelectionListener(sl);
		fStoreWithProject.addSelectionListener(sl);
		
		fLink= new Link(two, SWT.NONE);
		fLink.setText(DialogsMessages.PreferenceScopeBlock_preferenceLink);
		fLink.setLayoutData(new GridData());
		sl= new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(parent.getShell(), fPrefPageID, null, null).open();
				onPreferenceScopeChange();
			}
		};
		fLink.addSelectionListener(sl);
		
		Label horizontalLine= new Label(group, SWT.SEPARATOR | SWT.HORIZONTAL);
		horizontalLine.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
		horizontalLine.setFont(group.getFont());
	}

	abstract protected void onPreferenceScopeChange();

	private void updateEnablement() {
		if (fUseProjectSettings.getSelection()) {
			fLink.setVisible(false);
			fStoreWithProject.setVisible(true);
		}
		else {
			fStoreWithProject.setVisible(false);
			fLink.setVisible(true);
		}
		fUseProjectSettings.getParent().layout(true);
	}

	public void setProjectLocalScope() {
		fUseProjectSettings.setSelection(true);
		fStoreWithProject.setSelection(false);
		updateEnablement();
	}

	public void setProjectScope() {
		fUseProjectSettings.setSelection(true);
		fStoreWithProject.setSelection(true);
		updateEnablement();
	}

	public void setInstanceScope() {
		fUseProjectSettings.setSelection(false);
		fStoreWithProject.setSelection(false);
		updateEnablement();
	}

	public boolean isProjectLocalScope() {
		return fUseProjectSettings.getSelection() && !fStoreWithProject.getSelection();
	}

	public boolean isProjectScope() {
		return fUseProjectSettings.getSelection() && fStoreWithProject.getSelection();
	}
	
	public boolean isInstanceScope() {
		return !fUseProjectSettings.getSelection();
	}

}
