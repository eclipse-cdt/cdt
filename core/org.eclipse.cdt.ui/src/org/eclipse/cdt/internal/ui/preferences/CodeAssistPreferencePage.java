/*******************************************************************************
 * Copyright (c) 2002, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;

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

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, ContentAssistPreference.AUTOACTIVATION_DELAY));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.AUTOINSERT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.PREFIX_COMPLETION));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, ContentAssistPreference.TIMEOUT_DELAY));		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_ARROW));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOUBLECOLON));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.SHOW_DOCUMENTED_PROPOSALS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.ORDER_PROPOSALS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.ADD_INCLUDE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.CURRENT_FILE_SEARCH_SCOPE));        
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.PROJECT_SEARCH_SCOPE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, ContentAssistPreference.PROPOSALS_FILTER));

        OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return keys;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		fOverlayStore.load();
		fOverlayStore.start();

		Composite contentAssistComposite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		contentAssistComposite.setLayout(layout);

		String label;
		//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		// search scope (no longer supported)
		// The following three radio buttons are grouped together
//		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_searchGroupTitle; 
//		Group searchGroup = addGroupBox(contentAssistComposite, label, 2);
//		
//		label= PreferencesMessages.CEditorPreferencePage_ContentAssistPage_searchGroupCurrentFileOption; 
//		addRadioButton(searchGroup, label, ContentAssistPreference.CURRENT_FILE_SEARCH_SCOPE, 0);
//		
//		label= PreferencesMessages.CEditorPreferencePage_ContentAssistPage_searchGroupCurrentProjectOption; 
//		addRadioButton(searchGroup, label, ContentAssistPreference.PROJECT_SEARCH_SCOPE, 0);
		
		//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_insertionGroupTitle; 
		Group insertionGroup = addGroupBox(contentAssistComposite, label, 2);
		
		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_insertSingleProposalAutomatically; 
		addCheckBox(insertionGroup, label, ContentAssistPreference.AUTOINSERT, 0);

		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_insertCommonProposalAutomatically; 
		addCheckBox(insertionGroup, label, ContentAssistPreference.PREFIX_COMPLETION, 0);
		
		// parsing timeout (no longer supported)
//		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_timeoutDelay; 
//		addTextField(insertionGroup, label, ContentAssistPreference.TIMEOUT_DELAY, 6, 0, true);

		//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		// sorting and filtering	
		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_sortingSection_title; 
		Group sortingGroup = addGroupBox(contentAssistComposite, label, 2);

		label= PreferencesMessages.CEditorPreferencePage_ContentAssistPage_showProposalsInAlphabeticalOrder; 
		addCheckBox(sortingGroup, label, ContentAssistPreference.ORDER_PROPOSALS, 0);

		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_proposalFilterSelect ; 
		addComboBox(sortingGroup, label, ContentAssistPreference.PROPOSALS_FILTER, 20, 0);
    	
		//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		// The following items are grouped for Auto Activation
		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_autoActivationGroupTitle; 
		Group enableGroup = addGroupBox(contentAssistComposite, label, 2);
		
		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_autoActivationEnableDot; 
		addCheckBox(enableGroup, label, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOT, 0);

		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_autoActivationEnableArrow; 
		addCheckBox(enableGroup, label, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_ARROW, 0);
		
		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_autoActivationEnableDoubleColon; 
		addCheckBox(enableGroup, label, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOUBLECOLON, 0);
		
		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_autoActivationDelay; 
		addTextField(enableGroup, label, ContentAssistPreference.AUTOACTIVATION_DELAY, 4, 0, true);

		initializeFields();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(contentAssistComposite, ICHelpContextIds.C_EDITOR_CONTENT_ASSIST_PREF_PAGE);

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
		store.setDefault(ContentAssistPreference.PREFIX_COMPLETION, true);
		store.setDefault(ContentAssistPreference.ORDER_PROPOSALS, false);
		store.setDefault(ContentAssistPreference.ADD_INCLUDE, true);
		store.setDefault(ContentAssistPreference.PROPOSALS_FILTER, ProposalFilterPreferencesUtil.getProposalFilternamesAsString());  // $NON_NLS 1$

	}
	
}
