/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.Separator;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.prefs.BackingStoreException;

public class AppearancePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private SelectionButtonDialogField fShowTUChildren;
	private SelectionButtonDialogField fOutlineGroupIncludes;
	private SelectionButtonDialogField fOutlineGroupNamespaces;
	private SelectionButtonDialogField fCViewGroupIncludes;
	private SelectionButtonDialogField fCViewSeparateHeaderAndSource;
	private SelectionButtonDialogField fCViewGroupMacros;
	private SelectionButtonDialogField fOutlineGroupMembers;
	private SelectionButtonDialogField fOutlineGroupMacros;
	private SelectionButtonDialogField fShowSourceRootsAtTopOfProject;
	private SelectionButtonDialogField fCViewSortOrderOfExcludedFiles;
	private SelectionButtonDialogField fOutlineHidePragmaMarks;

	public AppearancePreferencePage() {
		setPreferenceStore(PreferenceConstants.getPreferenceStore());
		setDescription(PreferencesMessages.AppearancePreferencePage_description);

		IDialogFieldListener listener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(DialogField field) {
				doDialogFieldChanged(field);
			}
		};

		fShowTUChildren = new SelectionButtonDialogField(SWT.CHECK);
		fShowTUChildren.setDialogFieldListener(listener);
		fShowTUChildren.setLabelText(PreferencesMessages.AppearancePreferencePage_showTUChildren_label);

		fOutlineGroupIncludes = new SelectionButtonDialogField(SWT.CHECK);
		fOutlineGroupIncludes.setDialogFieldListener(listener);
		fOutlineGroupIncludes.setLabelText(PreferencesMessages.AppearancePreferencePage_outlineGroupIncludes_label);

		fOutlineGroupNamespaces = new SelectionButtonDialogField(SWT.CHECK);
		fOutlineGroupNamespaces.setDialogFieldListener(listener);
		fOutlineGroupNamespaces.setLabelText(PreferencesMessages.AppearancePreferencePage_outlineGroupNamespaces_label);

		fOutlineGroupMembers = new SelectionButtonDialogField(SWT.CHECK);
		fOutlineGroupMembers.setDialogFieldListener(listener);
		fOutlineGroupMembers.setLabelText(PreferencesMessages.AppearancePreferencePage_outlineGroupMethods_label);

		fCViewGroupIncludes = new SelectionButtonDialogField(SWT.CHECK);
		fCViewGroupIncludes.setDialogFieldListener(listener);
		fCViewGroupIncludes.setLabelText(PreferencesMessages.AppearancePreferencePage_cviewGroupIncludes_label);

		fCViewSeparateHeaderAndSource = new SelectionButtonDialogField(SWT.CHECK);
		fCViewSeparateHeaderAndSource.setDialogFieldListener(listener);
		fCViewSeparateHeaderAndSource
				.setLabelText(PreferencesMessages.AppearancePreferencePage_cviewSeparateHeaderAndSource_label);

		fShowSourceRootsAtTopOfProject = new SelectionButtonDialogField(SWT.CHECK);
		fShowSourceRootsAtTopOfProject.setDialogFieldListener(listener);
		fShowSourceRootsAtTopOfProject
				.setLabelText(PreferencesMessages.AppearancePreferencePage_showSourceRootsAtTopOfProject_label);

		fOutlineGroupMacros = new SelectionButtonDialogField(SWT.CHECK);
		fOutlineGroupMacros.setDialogFieldListener(listener);
		fOutlineGroupMacros.setLabelText(PreferencesMessages.AppearancePreferencePage_outlineGroupMacros_label);

		fOutlineHidePragmaMarks = new SelectionButtonDialogField(SWT.CHECK);
		fOutlineHidePragmaMarks.setDialogFieldListener(listener);
		fOutlineHidePragmaMarks.setLabelText(PreferencesMessages.AppearancePreferencePage_HidePragmaMarks_label);

		fCViewGroupMacros = new SelectionButtonDialogField(SWT.CHECK);
		fCViewGroupMacros.setDialogFieldListener(listener);
		fCViewGroupMacros.setLabelText(PreferencesMessages.AppearancePreferencePage_cviewGroupMacros_label);

		fCViewSortOrderOfExcludedFiles = new SelectionButtonDialogField(SWT.CHECK);
		fCViewSortOrderOfExcludedFiles.setDialogFieldListener(listener);
		fCViewSortOrderOfExcludedFiles
				.setLabelText(PreferencesMessages.AppearancePreferencePage_cviewKeepSortOrderOfExcludedFiles_label);
	}

	private void initFields() {
		IPreferenceStore prefs = getPreferenceStore();
		fShowTUChildren.setSelection(prefs.getBoolean(PreferenceConstants.PREF_SHOW_CU_CHILDREN));
		fCViewGroupIncludes.setSelection(prefs.getBoolean(PreferenceConstants.CVIEW_GROUP_INCLUDES));
		fCViewSeparateHeaderAndSource
				.setSelection(prefs.getBoolean(PreferenceConstants.CVIEW_SEPARATE_HEADER_AND_SOURCE));
		fCViewGroupMacros.setSelection(prefs.getBoolean(PreferenceConstants.CVIEW_GROUP_MACROS));
		fOutlineGroupIncludes.setSelection(prefs.getBoolean(PreferenceConstants.OUTLINE_GROUP_INCLUDES));
		fOutlineGroupNamespaces.setSelection(prefs.getBoolean(PreferenceConstants.OUTLINE_GROUP_NAMESPACES));
		fOutlineGroupMembers.setSelection(prefs.getBoolean(PreferenceConstants.OUTLINE_GROUP_MEMBERS));
		fOutlineGroupMacros.setSelection(prefs.getBoolean(PreferenceConstants.OUTLINE_GROUP_MACROS));
		fOutlineHidePragmaMarks.setSelection(prefs.getBoolean(PreferenceConstants.OUTLINE_HIDE_PRAGMA_MARK));
		boolean showSourceRootsAtTopOfProject = CCorePlugin.showSourceRootsAtTopOfProject();
		fShowSourceRootsAtTopOfProject.setSelection(showSourceRootsAtTopOfProject);
		fCViewSortOrderOfExcludedFiles.setSelection(prefs.getBoolean(PreferenceConstants.SORT_ORDER_OF_EXCLUDED_FILES));
	}

	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.APPEARANCE_PREFERENCE_PAGE);
	}

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);
		int nColumns = 1;

		Composite result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = 0;
		layout.numColumns = nColumns;
		result.setLayout(layout);

		fShowTUChildren.doFillIntoGrid(result, nColumns);
		fCViewGroupIncludes.doFillIntoGrid(result, nColumns);
		fOutlineGroupIncludes.doFillIntoGrid(result, nColumns);
		fOutlineGroupNamespaces.doFillIntoGrid(result, nColumns);
		fOutlineGroupMembers.doFillIntoGrid(result, nColumns);
		fCViewGroupMacros.doFillIntoGrid(result, nColumns);
		fOutlineGroupMacros.doFillIntoGrid(result, nColumns);
		fOutlineHidePragmaMarks.doFillIntoGrid(result, nColumns);

		new Separator().doFillIntoGrid(result, nColumns);

		fCViewSeparateHeaderAndSource.doFillIntoGrid(result, nColumns);
		fCViewSortOrderOfExcludedFiles.doFillIntoGrid(result, nColumns);

		String noteTitle = PreferencesMessages.AppearancePreferencePage_note;
		String noteMessage = PreferencesMessages.AppearancePreferencePage_preferenceOnlyForNewViews;
		Composite noteControl = createNoteComposite(JFaceResources.getDialogFont(), result, noteTitle, noteMessage);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		noteControl.setLayoutData(gd);

		new Separator().doFillIntoGrid(result, nColumns);
		fShowSourceRootsAtTopOfProject.doFillIntoGrid(result, nColumns);

		initFields();

		Dialog.applyDialogFont(result);
		return result;
	}

	void doDialogFieldChanged(DialogField field) {
		updateStatus(getValidationStatus());
	}

	private IStatus getValidationStatus() {
		return new StatusInfo();
	}

	private void updateStatus(IStatus status) {
		setValid(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);
	}

	/*
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}

	/*
	 * @see IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		IPreferenceStore prefs = getPreferenceStore();
		prefs.setValue(PreferenceConstants.PREF_SHOW_CU_CHILDREN, fShowTUChildren.isSelected());
		prefs.setValue(PreferenceConstants.CVIEW_GROUP_INCLUDES, fCViewGroupIncludes.isSelected());
		prefs.setValue(PreferenceConstants.CVIEW_SEPARATE_HEADER_AND_SOURCE,
				fCViewSeparateHeaderAndSource.isSelected());
		prefs.setValue(PreferenceConstants.CVIEW_GROUP_MACROS, fCViewGroupMacros.isSelected());
		prefs.setValue(PreferenceConstants.OUTLINE_GROUP_INCLUDES, fOutlineGroupIncludes.isSelected());
		prefs.setValue(PreferenceConstants.OUTLINE_GROUP_NAMESPACES, fOutlineGroupNamespaces.isSelected());
		prefs.setValue(PreferenceConstants.OUTLINE_GROUP_MEMBERS, fOutlineGroupMembers.isSelected());
		prefs.setValue(PreferenceConstants.OUTLINE_GROUP_MACROS, fOutlineGroupMacros.isSelected());
		prefs.setValue(PreferenceConstants.OUTLINE_HIDE_PRAGMA_MARK, fOutlineHidePragmaMarks.isSelected());
		prefs.setValue(PreferenceConstants.SORT_ORDER_OF_EXCLUDED_FILES, fCViewSortOrderOfExcludedFiles.isSelected());
		try {
			InstanceScope.INSTANCE.getNode(CUIPlugin.PLUGIN_ID).flush();
			IEclipsePreferences corePluginNode = InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID);
			corePluginNode.putBoolean(CCorePreferenceConstants.SHOW_SOURCE_ROOTS_AT_TOP_LEVEL_OF_PROJECT,
					fShowSourceRootsAtTopOfProject.isSelected());
			corePluginNode.flush();
		} catch (BackingStoreException exc) {
			CUIPlugin.log(exc);
		}
		return super.performOk();
	}

	/*
	 * @see PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		IPreferenceStore prefs = getPreferenceStore();
		fShowTUChildren.setSelection(prefs.getDefaultBoolean(PreferenceConstants.PREF_SHOW_CU_CHILDREN));
		fCViewGroupIncludes.setSelection(prefs.getDefaultBoolean(PreferenceConstants.CVIEW_GROUP_INCLUDES));
		fCViewSeparateHeaderAndSource
				.setSelection(prefs.getDefaultBoolean(PreferenceConstants.CVIEW_SEPARATE_HEADER_AND_SOURCE));
		fCViewGroupMacros.setSelection(prefs.getDefaultBoolean(PreferenceConstants.CVIEW_GROUP_MACROS));
		fOutlineGroupIncludes.setSelection(prefs.getDefaultBoolean(PreferenceConstants.OUTLINE_GROUP_INCLUDES));
		fOutlineGroupNamespaces.setSelection(prefs.getDefaultBoolean(PreferenceConstants.OUTLINE_GROUP_NAMESPACES));
		fOutlineGroupMembers.setSelection(prefs.getDefaultBoolean(PreferenceConstants.OUTLINE_GROUP_MEMBERS));
		fOutlineGroupMacros.setSelection(prefs.getDefaultBoolean(PreferenceConstants.OUTLINE_GROUP_MACROS));
		fOutlineHidePragmaMarks.setSelection(prefs.getDefaultBoolean(PreferenceConstants.OUTLINE_HIDE_PRAGMA_MARK));
		boolean showSourceRootsPref = DefaultScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID)
				.getBoolean(CCorePreferenceConstants.SHOW_SOURCE_ROOTS_AT_TOP_LEVEL_OF_PROJECT, true);
		fShowSourceRootsAtTopOfProject.setSelection(showSourceRootsPref);
		fCViewSortOrderOfExcludedFiles
				.setSelection(prefs.getDefaultBoolean(PreferenceConstants.SORT_ORDER_OF_EXCLUDED_FILES));
		super.performDefaults();
	}
}
