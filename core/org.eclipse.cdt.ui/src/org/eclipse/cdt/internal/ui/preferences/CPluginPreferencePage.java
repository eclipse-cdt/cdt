/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.OptionalMessageDialog;
import org.eclipse.cdt.internal.ui.util.SWTUtil;

/**
 * The page for general C/C++ preferences.
 */
public class CPluginPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	private static final String USE_STRUCTURAL_PARSE_MODE_LABEL= PreferencesMessages.CPluginPreferencePage_structuralParseMode_label;
	private static final int GROUP_VINDENT = 5;
	private static final int GROUP_HINDENT = 20;
	private Button b1, b2, b3;
	
	public CPluginPreferencePage() {
		super(GRID);
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.C_PREF_PAGE);
	}	

	/**
	 * @see FieldEditorPreferencePage#createControl(Composite)
	 */	
	@Override
	protected void createFieldEditors() {
		Composite parent= getFieldEditorParent();

		Label caption= new Label(parent, SWT.NULL);
		caption.setText(PreferencesMessages.CPluginPreferencePage_caption);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 1;
		caption.setLayoutData(gd);

		addFiller(parent);
		
		BooleanFieldEditor useStructuralParseMode= new BooleanFieldEditor(PreferenceConstants.PREF_USE_STRUCTURAL_PARSE_MODE, USE_STRUCTURAL_PARSE_MODE_LABEL, parent);
		addField(useStructuralParseMode);
		
		String noteTitle= PreferencesMessages.CPluginPreferencePage_note; 
		String noteMessage= PreferencesMessages.CPluginPreferencePage_performanceHint; 
		Composite noteControl= createNoteComposite(JFaceResources.getDialogFont(), parent, noteTitle, noteMessage);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 1;
		noteControl.setLayoutData(gd);
		
		// Build either default cfg or all. 
		Group gr = new Group(parent, SWT.NONE);
		gr.setText(PreferencesMessages.CPluginPreferencePage_0);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalIndent = GROUP_VINDENT;
		gr.setLayoutData(gd);
		gr.setLayout(new GridLayout());

		Label l1 = new Label(gr, SWT.NONE);
		l1.setText(PreferencesMessages.CPluginPreferencePage_1);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalIndent = GROUP_VINDENT;
		l1.setLayoutData(gd);

		boolean b = ACBuilder.needAllConfigBuild();

		b1 = new Button(gr, SWT.RADIO);
		b1.setText(PreferencesMessages.CPluginPreferencePage_2);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalIndent = GROUP_VINDENT;
		gd.horizontalIndent = GROUP_HINDENT;
		b1.setLayoutData(gd);
		b1.setSelection(!b);

		b2 = new Button(gr, SWT.RADIO);
		b2.setText(PreferencesMessages.CPluginPreferencePage_3);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = GROUP_HINDENT;
		b2.setLayoutData(gd);
		b2.setSelection(b);
		
		noteControl= createNoteComposite(
				JFaceResources.getDialogFont(), 
				gr, 
				PreferencesMessages.CPluginPreferencePage_note, 
				PreferencesMessages.CPluginPreferencePage_4);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.verticalIndent = GROUP_VINDENT;
		noteControl.setLayoutData(gd);

		// Building project dependencies 
		Group gr2 = new Group(parent, SWT.NONE);
		gr2.setText(PreferencesMessages.CPluginPreferencePage_5);
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		gd2.verticalIndent = GROUP_VINDENT;
		gr2.setLayoutData(gd2);
		gr2.setLayout(new GridLayout());

		boolean b2 = ACBuilder.buildConfigResourceChanges();
		
		b3 = new Button(gr2, SWT.CHECK);
		b3.setText(PreferencesMessages.CPluginPreferencePage_7);
		gd2 = new GridData(GridData.FILL_HORIZONTAL);
		gd2.verticalIndent = GROUP_VINDENT;
		b3.setLayoutData(gd2);
		b3.setSelection(b2);
		
		GridLayout layout = new GridLayout();
		layout.numColumns= 2;

		Group dontAskGroup= new Group(parent, SWT.NONE);
		dontAskGroup.setLayout(layout);
		dontAskGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dontAskGroup.setText(PreferencesMessages.CPluginPreferencePage_cdtDialogs_group);

		Label label= new Label(dontAskGroup, SWT.WRAP);
		label.setText(PreferencesMessages.CPluginPreferencePage_clearDoNotShowAgainSettings_label);
		GridData data= new GridData(GridData.FILL, GridData.CENTER, true, false);
		data.widthHint= convertVerticalDLUsToPixels(50);
		label.setLayoutData(data);

		Button clearButton= new Button(dontAskGroup, SWT.PUSH);
		clearButton.setText(PreferencesMessages.CPluginPreferencePage_clear_button);
		clearButton.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false));
		clearButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				OptionalMessageDialog.clearAllRememberedStates();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				OptionalMessageDialog.clearAllRememberedStates();
			}
		});
		SWTUtil.setButtonDimensionHint(clearButton);
		Dialog.applyDialogFont(parent);
	}
	@Override
	protected Composite createNoteComposite(Font font, Composite composite,
			String title, String message) {
		Composite messageComposite = super.createNoteComposite(font, composite,
				title, message);
		Control[] children = messageComposite.getChildren();
		if (children.length == 2 && (children[1] instanceof Label)) {
			// this is temporary fix for problem that 3 line note does not displayed properly within the group
			Label messageLabel = (Label) children[1];
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.widthHint=400;
			messageLabel.setLayoutData(gd);
		}
		return messageComposite;
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
	
	public static void setBuildAllMode3(boolean enable) {
	}
	
	/**
	 * @see IWorkbenchPreferencePage#init
	 */
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
		// tell the Core Plugin about this preference
		CCorePlugin.getDefault().setStructuralParseMode(useStructuralParseMode());
		ACBuilder.setAllConfigBuild(b2.getSelection());
		ACBuilder.setBuildConfigResourceChanges(b3.getSelection());
		return true;
	}

    @Override
	protected void performDefaults() {
    	super.performDefaults();
		ACBuilder.setAllConfigBuild(false);
		ACBuilder.setBuildConfigResourceChanges(false);
		b1.setSelection(true);
		b2.setSelection(false);
		b3.setSelection(false);
    }
}
