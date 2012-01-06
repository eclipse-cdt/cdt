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
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.OptionalMessageDialog;
import org.eclipse.cdt.internal.ui.util.SWTUtil;

/**
 * The page for general C/C++ preferences.
 */
public class CPluginPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	public static final String C_BASE_PREF_PAGE_ID= "org.eclipse.cdt.ui.preferences.CPluginPreferencePage"; //$NON-NLS-1$
	
	private static final int GROUP_VINDENT = 5;
	private ArrayList<Button> fCheckBoxes;

	public CPluginPreferencePage() {
		super();
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
		setDescription(PreferencesMessages.CPluginPreferencePage_description);

		fCheckBoxes= new ArrayList<Button>();
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

		Group outlineViewGroup = addGroup(container, PreferencesMessages.CPluginPreferencePage_outline_view);
		addCheckBox(outlineViewGroup, PreferencesMessages.CPluginPreferencePage_structuralParseMode_label,
				PreferenceConstants.PREF_USE_STRUCTURAL_PARSE_MODE);
		
		addNote(outlineViewGroup, PreferencesMessages.CPluginPreferencePage_performanceHint);

		// Refactoring.
		Group refactoringGroup = addGroup(container, PreferencesMessages.CPluginPreferencePage_refactoring_title);
		addCheckBox(refactoringGroup,
				PreferencesMessages.CPluginPreferencePage_refactoring_auto_save,
				PreferenceConstants.REFACTOR_SAVE_ALL_EDITORS);
		addCheckBox(refactoringGroup,
				PreferencesMessages.CPluginPreferencePage_refactoring_lightweight,
				PreferenceConstants.REFACTOR_LIGHTWEIGHT);
		
		Group dontAskGroup= addGroup(container, PreferencesMessages.CPluginPreferencePage_cdtDialogs_group, 2);
		Label label= new Label(dontAskGroup, SWT.WRAP);
		label.setText(PreferencesMessages.CPluginPreferencePage_clearDoNotShowAgainSettings_label);
		GridData data= new GridData(GridData.FILL, GridData.CENTER, true, false);
		data.widthHint= convertVerticalDLUsToPixels(50);
		label.setLayoutData(data);

		Button clearButton= new Button(dontAskGroup, SWT.PUSH);
		clearButton.setText(PreferencesMessages.CPluginPreferencePage_clear_button);
		clearButton.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false));
		clearButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				OptionalMessageDialog.clearAllRememberedStates();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				OptionalMessageDialog.clearAllRememberedStates();
			}
		});
		SWTUtil.setButtonDimensionHint(clearButton);
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

	private Button addCheckBox(Composite parent, String label, String key) {
		Button button= new Button(parent, SWT.CHECK);
		button.setText(label);
		button.setData(key);
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		button.setSelection(getPreferenceStore().getBoolean(key));

		fCheckBoxes.add(button);
		return button;
	}

	protected void addFiller(Composite composite) {
		PixelConverter pixelConverter= new PixelConverter(composite);
		Label filler= new Label(composite, SWT.LEFT );
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 1;
		gd.heightHint= pixelConverter.convertHeightInCharsToPixels(1) / 2;
		filler.setLayoutData(gd);
	}

	public static boolean isLinkToEditor() {
		return CUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.PREF_LINK_TO_EDITOR);
	}

	public static void setLinkingEnabled(boolean enable) {
		CUIPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.PREF_LINK_TO_EDITOR, enable);
	}

	public static boolean useStructuralParseMode() {
		return CUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.PREF_USE_STRUCTURAL_PARSE_MODE);
	}

	/**
	 * @see IWorkbenchPreferencePage#init
	 */
	@Override
	public void init(IWorkbench workbench) {
	}
	
	/**
	 * Initializes the default values of this page in the preference bundle.
	 */
	public static void initDefaults(IPreferenceStore prefs) {
		prefs.setDefault(PreferenceConstants.PREF_LINK_TO_EDITOR, false);
		prefs.setDefault(PreferenceConstants.PREF_USE_STRUCTURAL_PARSE_MODE, false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		if (!super.performOk())
			return false;
		IPreferenceStore store= getPreferenceStore();
		for (int i= 0; i < fCheckBoxes.size(); i++) {
			Button button= fCheckBoxes.get(i);
			String key= (String) button.getData();
			store.setValue(key, button.getSelection());
		}
		// tell the Core Plugin about this preference
		CCorePlugin.getDefault().setStructuralParseMode(useStructuralParseMode());
		return true;
	}

    @Override
	protected void performDefaults() {
    	super.performDefaults();
		IPreferenceStore store= getPreferenceStore();
		for (int i= 0; i < fCheckBoxes.size(); i++) {
			Button button= fCheckBoxes.get(i);
			String key= (String) button.getData();
			button.setSelection(store.getDefaultBoolean(key));
		}
    }
}
