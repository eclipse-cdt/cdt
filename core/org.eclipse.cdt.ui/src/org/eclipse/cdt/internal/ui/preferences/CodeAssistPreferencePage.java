/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * CodeAssistPreferencePage
 */
public class CodeAssistPreferencePage extends AbstractPreferencePage {

	/**
	 * 
	 */
	public CodeAssistPreferencePage() {
		super();
		//setDescription(PreferencesMessages.getString("CodeAssistPreferencePage.description")); //$NON-NLS-1$
	}

	protected OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {
		ArrayList overlayKeys = new ArrayList();

		// temporary
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.USE_DOM));
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, ContentAssistPreference.AUTOACTIVATION_DELAY));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.AUTOINSERT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.CODEASSIST_PREFIX_COMPLETION));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, ContentAssistPreference.TIMEOUT_DELAY));		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.PROPOSALS_BACKGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.PROPOSALS_FOREGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.PARAMETERS_BACKGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.PARAMETERS_FOREGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_ARROW));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOUBLECOLON));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.SHOW_DOCUMENTED_PROPOSALS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.ORDER_PROPOSALS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.ADD_INCLUDE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.CURRENT_FILE_SEARCH_SCOPE));        
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.PROJECT_SEARCH_SCOPE));

        OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return keys;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite contentAssistComposite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		contentAssistComposite.setLayout(layout);

		// temporary use DOM
		addCheckBox(contentAssistComposite, "Use DOM (Work in progress)", ContentAssistPreference.USE_DOM, 0);
		
		//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		// The following three radio buttons are grouped together
		String label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.searchGroupTitle"); //$NON-NLS-1$
		Group searchGroup = addGroupBox(contentAssistComposite, label, 2);
		
		label= PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.searchGroupCurrentFileOption"); //$NON-NLS-1$
		addRadioButton(searchGroup, label, ContentAssistPreference.CURRENT_FILE_SEARCH_SCOPE, 0);
		
		label= PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.searchGroupCurrentProjectOption"); //$NON-NLS-1$
		addRadioButton(searchGroup, label, ContentAssistPreference.PROJECT_SEARCH_SCOPE, 0);
		
		//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.insertionGroupTitle"); //$NON-NLS-1$
		Group insertionGroup = addGroupBox(contentAssistComposite, label, 2);
		
		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.insertSingleProposalAutomatically"); //$NON-NLS-1$
		addCheckBox(insertionGroup, label, ContentAssistPreference.AUTOINSERT, 0);

		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.insertCommonProposalAutomatically"); //$NON-NLS-1$
		addCheckBox(insertionGroup, label, ContentAssistPreference.CODEASSIST_PREFIX_COMPLETION, 0);
		
		label= PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.showProposalsInAlphabeticalOrder"); //$NON-NLS-1$
		addCheckBox(insertionGroup, label, ContentAssistPreference.ORDER_PROPOSALS, 0);

		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.timeoutDelay"); //$NON-NLS-1$
		addTextField(insertionGroup, label, ContentAssistPreference.TIMEOUT_DELAY, 6, 0, true);


		//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		// The following items are grouped for Auto Activation
		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.autoActivationGroupTitle"); //$NON-NLS-1$
		Group enableGroup = addGroupBox(contentAssistComposite, label, 2);
		
		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.autoActivationEnableDot"); //$NON-NLS-1$
		addCheckBox(enableGroup, label, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOT, 0);

		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.autoActivationEnableArrow"); //$NON-NLS-1$
		addCheckBox(enableGroup, label, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_ARROW, 0);
		
		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.autoActivationEnableDoubleColon"); //$NON-NLS-1$
		addCheckBox(enableGroup, label, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOUBLECOLON, 0);
		
		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.autoActivationDelay"); //$NON-NLS-1$
		addTextField(enableGroup, label, ContentAssistPreference.AUTOACTIVATION_DELAY, 4, 0, true);

		//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&		
		label = PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.completionProposalBackgroundColor"); //$NON-NLS-1$
		addColorButton(contentAssistComposite, label, ContentAssistPreference.PROPOSALS_BACKGROUND, 0);

		label= PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.completionProposalForegroundColor"); //$NON-NLS-1$
		addColorButton(contentAssistComposite, label, ContentAssistPreference.PROPOSALS_FOREGROUND, 0);

//		label= PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.parameterBackgroundColor"); 
//		addColorButton(contentAssistComposite, label, ContentAssistPreference.PARAMETERS_BACKGROUND, 0);
//
//		label= PreferencesMessages.getString("CEditorPreferencePage.ContentAssistPage.parameterForegroundColor");
//		addColorButton(contentAssistComposite, label, ContentAssistPreference.PARAMETERS_FOREGROUND, 0);

		WorkbenchHelp.setHelp(contentAssistComposite, ICHelpContextIds.C_EDITOR_CONTENT_ASSIST_PREF_PAGE);	

		return contentAssistComposite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}

	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(ContentAssistPreference.CURRENT_FILE_SEARCH_SCOPE, true);
		store.setDefault(ContentAssistPreference.PROJECT_SEARCH_SCOPE, false);

		store.setDefault(ContentAssistPreference.TIMEOUT_DELAY, 3000);
		
		store.setDefault(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOT, true);
		store.setDefault(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_ARROW, true);
		store.setDefault(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOUBLECOLON, true);
		store.setDefault(ContentAssistPreference.AUTOACTIVATION_DELAY, 500);
		
		store.setDefault(ContentAssistPreference.AUTOINSERT, true);
		store.setDefault(ContentAssistPreference.CODEASSIST_PREFIX_COMPLETION, true);
		PreferenceConverter.setDefault(store, ContentAssistPreference.PROPOSALS_BACKGROUND, new RGB(254, 241, 233));
		PreferenceConverter.setDefault(store, ContentAssistPreference.PROPOSALS_FOREGROUND, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(store, ContentAssistPreference.PARAMETERS_BACKGROUND, new RGB(254, 241, 233));
		PreferenceConverter.setDefault(store, ContentAssistPreference.PARAMETERS_FOREGROUND, new RGB(0, 0, 0));
		store.setDefault(ContentAssistPreference.ORDER_PROPOSALS, false);
		store.setDefault(ContentAssistPreference.ADD_INCLUDE, true);

	}
}
