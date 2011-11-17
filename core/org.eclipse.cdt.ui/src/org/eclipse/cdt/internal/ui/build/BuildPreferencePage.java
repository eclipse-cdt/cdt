/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.build;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;

/**
 * The page for top-level build preferences
 */
public class BuildPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private static final int GROUP_VINDENT = 5;
	private static final int GROUP_HINDENT = 20;
	private Button buildActive, buildAll, buildOnlyOnRefChange;

	public BuildPreferencePage() {
		super();
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
		setDescription(PreferencesMessages.CBuildPreferencePage_description);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.C_PREF_PAGE);
	}

	@Override
	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);

		Composite container= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth= 0;
		layout.verticalSpacing= convertVerticalDLUsToPixels(10);
		layout.horizontalSpacing= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		container.setLayout(layout);

		// Build either default configuration or all.
		Group gr = addGroup(container, PreferencesMessages.CPluginPreferencePage_build_scope);
		Label l1 = new Label(gr, SWT.NONE);
		l1.setText(PreferencesMessages.CPluginPreferencePage_1);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalIndent = GROUP_VINDENT;
		l1.setLayoutData(gd);

		boolean needAllConfigBuild = ACBuilder.needAllConfigBuild();

		buildActive = new Button(gr, SWT.RADIO);
		buildActive.setText(PreferencesMessages.CPluginPreferencePage_2);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalIndent = GROUP_VINDENT;
		gd.horizontalIndent = GROUP_HINDENT;
		buildActive.setLayoutData(gd);
		buildActive.setSelection(!needAllConfigBuild);

		buildAll = new Button(gr, SWT.RADIO);
		buildAll.setText(PreferencesMessages.CPluginPreferencePage_3);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = GROUP_HINDENT;
		buildAll.setLayoutData(gd);
		buildAll.setSelection(needAllConfigBuild);

		addNote(gr, PreferencesMessages.CPluginPreferencePage_4);

		// Building project dependencies.
		Group gr2 = addGroup(container, PreferencesMessages.CPluginPreferencePage_building_configurations);
		buildOnlyOnRefChange = new Button(gr2, SWT.CHECK);
		buildOnlyOnRefChange.setText(PreferencesMessages.CPluginPreferencePage_7);
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		gd2.verticalIndent = GROUP_VINDENT;
		buildOnlyOnRefChange.setLayoutData(gd2);
		buildOnlyOnRefChange.setSelection(ACBuilder.buildConfigResourceChanges());

		Dialog.applyDialogFont(container);
		return container;
	}

	private void addNote(Group parent, String noteMessage) {
		Composite noteControl= createNoteComposite(JFaceResources.getDialogFont(), parent,
				PreferencesMessages.CPluginPreferencePage_note, noteMessage);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.verticalIndent = GROUP_VINDENT;
		noteControl.setLayoutData(gd);
	}

	@Override
	protected Composite createNoteComposite(Font font, Composite composite, String title, String message) {
		Composite messageComposite = super.createNoteComposite(font, composite, title, message);
		Control[] children = messageComposite.getChildren();
		if (children.length == 2 && (children[1] instanceof Label)) {
			// this is temporary fix for problem that 3 line note does not displayed properly within the group
			Label messageLabel = (Label) children[1];
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.widthHint=500;
			messageLabel.setLayoutData(gd);
		}
		return messageComposite;
	}

	private Group addGroup(Composite parent, String label) {
		return addGroup(parent, label, 1);
	}

	private Group addGroup(Composite parent, String label, int numColumns) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(label);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setLayout(new GridLayout(numColumns, false));
		return group;
	}

	/**
	 * @see IWorkbenchPreferencePage#init
	 */
	@Override
	public void init(IWorkbench workbench) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		if (!super.performOk())
			return false;
		// tell the Core Plugin about this preference
		ACBuilder.setAllConfigBuild(buildAll.getSelection());
		ACBuilder.setBuildConfigResourceChanges(buildOnlyOnRefChange.getSelection());
		return true;
	}

    @Override
	protected void performDefaults() {
		ACBuilder.setAllConfigBuild(false);
		ACBuilder.setBuildConfigResourceChanges(false);
		buildActive.setSelection(true);
		buildAll.setSelection(false);
		buildOnlyOnRefChange.setSelection(false);
    	super.performDefaults();
    }
}
