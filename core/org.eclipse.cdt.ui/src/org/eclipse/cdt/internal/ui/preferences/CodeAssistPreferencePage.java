/*******************************************************************************
 * Copyright (c) 2002, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     IBM Corporation
 *     Kirk Beitz (Nokia)
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *     Thomas Corbat
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.preferences.OverlayPreferenceStore.OverlayKey;
import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Content Assist preference page.
 */
public class CodeAssistPreferencePage extends AbstractPreferencePage {

	public CodeAssistPreferencePage() {
		super();
		//setDescription(PreferencesMessages.getString("CodeAssistPreferencePage.description")); //$NON-NLS-1$
	}

	@Override
	protected OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {
		ArrayList<OverlayKey> overlayKeys = new ArrayList<>();

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT,
				ContentAssistPreference.AUTOACTIVATION_DELAY));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				ContentAssistPreference.AUTOINSERT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				ContentAssistPreference.PREFIX_COMPLETION));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				ContentAssistPreference.GUESS_ARGUMENTS));
		//		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, ContentAssistPreference.TIMEOUT_DELAY));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING,
				ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING,
				ContentAssistPreference.AUTOACTIVATION_TRIGGERS_ARROW));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING,
				ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOUBLECOLON));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING,
				ContentAssistPreference.AUTOACTIVATION_TRIGGERS_REPLACE_DOT_WITH_ARROW));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				ContentAssistPreference.DEFAULT_ARGUMENT_DISPLAY_PARAMETERS_WITH_DEFAULT_ARGUMENT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				ContentAssistPreference.DEFAULT_ARGUMENT_DISPLAY_ARGUMENTS));
		//		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.SHOW_DOCUMENTED_PROPOSALS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				ContentAssistPreference.ORDER_PROPOSALS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				ContentAssistPreference.SHOW_CAMEL_CASE_MATCHES));
		//		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ContentAssistPreference.ADD_INCLUDE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				ContentAssistPreference.CURRENT_FILE_SEARCH_SCOPE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				ContentAssistPreference.PROJECT_SEARCH_SCOPE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING,
				ContentAssistPreference.PROPOSALS_FILTER));

		return overlayKeys.toArray(new OverlayPreferenceStore.OverlayKey[overlayKeys.size()]);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				ICHelpContextIds.C_EDITOR_CONTENT_ASSIST_PREF_PAGE);
	}

	@Override
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

		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_guessArguments;
		addCheckBox(insertionGroup, label, ContentAssistPreference.GUESS_ARGUMENTS, 0);

		//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		// sorting and filtering
		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_sortingSection_title;
		Group sortingGroup = addGroupBox(contentAssistComposite, label, 2);

		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_showProposalsInAlphabeticalOrder;
		addCheckBox(sortingGroup, label, ContentAssistPreference.ORDER_PROPOSALS, 0);

		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_proposalFilterSelect;
		addComboBox(sortingGroup, label, ContentAssistPreference.PROPOSALS_FILTER, NO_TEXT_LIMIT, 0);

		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_showCamelCaseMatches;
		addCheckBox(sortingGroup, label, ContentAssistPreference.SHOW_CAMEL_CASE_MATCHES, 0);

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

		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_autoActivationEnableReplaceDotWithArrow;
		addCheckBox(enableGroup, label, ContentAssistPreference.AUTOACTIVATION_TRIGGERS_REPLACE_DOT_WITH_ARROW, 0);

		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_autoActivationDelay;
		addTextField(enableGroup, label, ContentAssistPreference.AUTOACTIVATION_DELAY, 4, 4, true);

		//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		// The following items are grouped for Default Arguments
		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_defaultArgumentsGroupTitle;
		Group defaultArgumentsGroup = addGroupBox(contentAssistComposite, label, 2);

		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_displayParametersWithDefaultArgument;
		Button displayDefaultedParameters = addCheckBox(defaultArgumentsGroup, label,
				ContentAssistPreference.DEFAULT_ARGUMENT_DISPLAY_PARAMETERS_WITH_DEFAULT_ARGUMENT, 0);

		label = PreferencesMessages.CEditorPreferencePage_ContentAssistPage_displayDefaultArguments;
		Button displayDefaultArguments = addCheckBox(defaultArgumentsGroup, label,
				ContentAssistPreference.DEFAULT_ARGUMENT_DISPLAY_ARGUMENTS, 0);

		createDependency(displayDefaultedParameters,
				ContentAssistPreference.DEFAULT_ARGUMENT_DISPLAY_PARAMETERS_WITH_DEFAULT_ARGUMENT,
				displayDefaultArguments);

		initializeFields();

		return contentAssistComposite;
	}

	@Override
	public void init(IWorkbench workbench) {
		// Nothing to do.
	}

	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(ContentAssistPreference.CURRENT_FILE_SEARCH_SCOPE, true);
		store.setDefault(ContentAssistPreference.PROJECT_SEARCH_SCOPE, false);

		store.setDefault(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOT, true);
		store.setDefault(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_ARROW, true);
		store.setDefault(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_DOUBLECOLON, true);
		store.setDefault(ContentAssistPreference.AUTOACTIVATION_TRIGGERS_REPLACE_DOT_WITH_ARROW, true);
		store.setDefault(ContentAssistPreference.AUTOACTIVATION_DELAY, 500);
		store.setDefault(ContentAssistPreference.DEFAULT_ARGUMENT_DISPLAY_PARAMETERS_WITH_DEFAULT_ARGUMENT, true);
		store.setDefault(ContentAssistPreference.DEFAULT_ARGUMENT_DISPLAY_ARGUMENTS, true);

		store.setDefault(ContentAssistPreference.AUTOINSERT, true);
		store.setDefault(ContentAssistPreference.PREFIX_COMPLETION, true);
		store.setDefault(ContentAssistPreference.GUESS_ARGUMENTS, true);
		store.setDefault(ContentAssistPreference.ORDER_PROPOSALS, false);
		store.setDefault(ContentAssistPreference.PROPOSALS_FILTER,
				ProposalFilterPreferencesUtil.getProposalFilternamesAsString());
		store.setDefault(ContentAssistPreference.SHOW_CAMEL_CASE_MATCHES, true);
	}
}
