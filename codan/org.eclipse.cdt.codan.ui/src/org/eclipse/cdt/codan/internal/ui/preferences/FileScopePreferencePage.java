/*******************************************************************************
 * Copyright (c) 2010, 2012 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.codan.core.param.FileScopeProblemPreference;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.cdt.codan.internal.ui.dialogs.ExclusionInclusionEntryDialog;
import org.eclipse.cdt.codan.internal.ui.widgets.BasicElementLabels;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class FileScopePreferencePage extends PreferencePage {
	private ListDialogField<String> fInclusionPatternList;
	private ListDialogField<String> fExclusionPatternList;
	private FileScopeProblemPreference fCurrElement;
	private IProject fCurrProject;
	private IContainer fCurrSourceFolder;
	private static final int IDX_ADD = 0;
	private static final int IDX_ADD_MULTIPLE = 1;
	private static final int IDX_EDIT = 2;
	private static final int IDX_REMOVE = 4;

	public FileScopePreferencePage(FileScopeProblemPreference entryToEdit) {
		setTitle(CodanUIMessages.ExclusionInclusionDialog_title);
		setDescription(CodanUIMessages.ExclusionInclusionDialog_description2);
		fCurrElement = entryToEdit;
		fCurrProject = entryToEdit.getProject();
		IWorkspaceRoot root = fCurrProject != null ? fCurrProject.getWorkspace().getRoot()
				: ResourcesPlugin.getWorkspace().getRoot();
		IResource res = root.findMember(entryToEdit.getPath());
		if (res instanceof IContainer) {
			fCurrSourceFolder = (IContainer) res;
		}
		if (res == null)
			fCurrSourceFolder = root;
		String excLabel = CodanUIMessages.ExclusionInclusionDialog_exclusion_pattern_label;
		//		ImageDescriptor excDescriptor = null; //JavaPluginImages.DESC_OBJS_EXCLUSION_FILTER_ATTRIB;
		String[] excButtonLabels = new String[] { CodanUIMessages.ExclusionInclusionDialog_exclusion_pattern_add,
				CodanUIMessages.ExclusionInclusionDialog_exclusion_pattern_add_multiple,
				CodanUIMessages.ExclusionInclusionDialog_exclusion_pattern_edit, null,
				CodanUIMessages.ExclusionInclusionDialog_exclusion_pattern_remove };
		String incLabel = CodanUIMessages.ExclusionInclusionDialog_inclusion_pattern_label;
		//		ImageDescriptor incDescriptor = null; //JavaPluginImages.DESC_OBJS_INCLUSION_FILTER_ATTRIB;
		String[] incButtonLabels = new String[] { CodanUIMessages.ExclusionInclusionDialog_inclusion_pattern_add,
				CodanUIMessages.ExclusionInclusionDialog_inclusion_pattern_add_multiple,
				CodanUIMessages.ExclusionInclusionDialog_inclusion_pattern_edit, null,
				CodanUIMessages.ExclusionInclusionDialog_inclusion_pattern_remove };
		fExclusionPatternList = createListContents(entryToEdit, FileScopeProblemPreference.EXCLUSION, excLabel, null,
				excButtonLabels);
		fInclusionPatternList = createListContents(entryToEdit, FileScopeProblemPreference.INCLUSION, incLabel, null,
				incButtonLabels);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite inner = new Composite(parent, SWT.NONE);
		inner.setFont(parent.getFont());
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		inner.setLayout(layout);
		inner.setLayoutData(new GridData(GridData.FILL_BOTH));
		fInclusionPatternList.doFillIntoGrid(inner, 3);
		LayoutUtil.setHorizontalSpan(fInclusionPatternList.getLabelControl(null), 2);
		LayoutUtil.setHorizontalGrabbing(fInclusionPatternList.getListControl(null), true);
		fExclusionPatternList.doFillIntoGrid(inner, 3);
		LayoutUtil.setHorizontalSpan(fExclusionPatternList.getLabelControl(null), 2);
		LayoutUtil.setHorizontalGrabbing(fExclusionPatternList.getListControl(null), true);
		setControl(inner);
		Dialog.applyDialogFont(inner);
		return inner;
	}

	private static class ExclusionInclusionLabelProvider extends LabelProvider {

		public ExclusionInclusionLabelProvider(String descriptorPath) {
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}

		@Override
		public String getText(Object element) {
			return BasicElementLabels.getFilePattern((String) element);
		}
	}

	private ListDialogField<String> createListContents(FileScopeProblemPreference entryToEdit, String key, String label,
			String descriptor, String[] buttonLabels) {
		ExclusionPatternAdapter adapter = new ExclusionPatternAdapter();
		ListDialogField<String> patternList = new ListDialogField<>(adapter, buttonLabels,
				new ExclusionInclusionLabelProvider(descriptor));
		patternList.setDialogFieldListener(adapter);
		patternList.setLabelText(label);
		patternList.enableButton(IDX_EDIT, false);
		IPath[] pattern = entryToEdit.getAttribute(key);
		ArrayList<String> elements = new ArrayList<>(pattern.length);
		for (int i = 0; i < pattern.length; i++) {
			String patternName = pattern[i].toString();
			if (patternName.length() > 0)
				elements.add(patternName);
		}
		patternList.setElements(elements);
		patternList.selectFirstElement();
		patternList.enableButton(IDX_ADD_MULTIPLE, fCurrSourceFolder != null);
		patternList.setViewerComparator(new ViewerComparator());
		return patternList;
	}

	protected void doCustomButtonPressed(ListDialogField<String> field, int index) {
		if (index == IDX_ADD) {
			addEntry(field);
		} else if (index == IDX_EDIT) {
			editEntry(field);
		} else if (index == IDX_ADD_MULTIPLE) {
			addMultipleEntries(field);
		} else if (index == IDX_REMOVE) {
			field.removeElements(field.getSelectedElements());
		}
		updateStatus();
	}

	private void updateStatus() {
		fCurrElement.setAttribute(FileScopeProblemPreference.INCLUSION, getInclusionPattern());
		fCurrElement.setAttribute(FileScopeProblemPreference.EXCLUSION, getExclusionPattern());
	}

	protected void doDoubleClicked(ListDialogField<String> field) {
		editEntry(field);
		updateStatus();
	}

	protected void doSelectionChanged(ListDialogField<String> field) {
		List<String> selected = field.getSelectedElements();
		field.enableButton(IDX_EDIT, canEdit(selected));
	}

	private boolean canEdit(List<String> selected) {
		return selected.size() == 1;
	}

	private void editEntry(ListDialogField<String> field) {
		List<String> selElements = field.getSelectedElements();
		if (selElements.size() != 1) {
			return;
		}
		List<String> existing = field.getElements();
		String entry = selElements.get(0);
		ExclusionInclusionEntryDialog dialog = new ExclusionInclusionEntryDialog(getShell(), isExclusion(field), entry,
				existing, fCurrElement);
		if (dialog.open() == Window.OK) {
			field.replaceElement(entry, dialog.getExclusionPattern());
		}
	}

	private boolean isExclusion(ListDialogField<String> field) {
		return field == fExclusionPatternList;
	}

	private void addEntry(ListDialogField<String> field) {
		List<String> existing = field.getElements();
		ExclusionInclusionEntryDialog dialog = new ExclusionInclusionEntryDialog(getShell(), isExclusion(field), null,
				existing, fCurrElement);
		if (dialog.open() == Window.OK) {
			field.addElement(dialog.getExclusionPattern());
		}
	}

	// -------- ExclusionPatternAdapter --------
	private class ExclusionPatternAdapter implements IListAdapter<String>, IDialogFieldListener {
		/**
		 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter#customButtonPressed(org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField,
		 *      int)
		 */
		@Override
		public void customButtonPressed(ListDialogField<String> field, int index) {
			doCustomButtonPressed(field, index);
		}

		/**
		 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter#selectionChanged(org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField)
		 */
		@Override
		public void selectionChanged(ListDialogField<String> field) {
			doSelectionChanged(field);
		}

		/**
		 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter#doubleClicked(org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField)
		 */
		@Override
		public void doubleClicked(ListDialogField<String> field) {
			doDoubleClicked(field);
		}

		/**
		 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener#dialogFieldChanged(org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField)
		 */
		@Override
		public void dialogFieldChanged(DialogField field) {
		}
	}

	protected void doStatusLineUpdate() {
	}

	protected void checkIfPatternValid() {
	}

	private IPath[] getPattern(ListDialogField<String> field) {
		Object[] arr = field.getElements().toArray();
		Arrays.sort(arr);
		IPath[] res = new IPath[arr.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = new Path((String) arr[i]);
		}
		return res;
	}

	public IPath[] getExclusionPattern() {
		return getPattern(fExclusionPatternList);
	}

	public IPath[] getInclusionPattern() {
		return getPattern(fInclusionPatternList);
	}

	private void addMultipleEntries(ListDialogField<String> field) {
		String title, message;
		if (isExclusion(field)) {
			title = CodanUIMessages.ExclusionInclusionDialog_ChooseExclusionPattern_title;
			message = CodanUIMessages.ExclusionInclusionDialog_ChooseExclusionPattern_description;
		} else {
			title = CodanUIMessages.ExclusionInclusionDialog_ChooseInclusionPattern_title;
			message = CodanUIMessages.ExclusionInclusionDialog_ChooseInclusionPattern_description;
		}
		IPath[] res = ExclusionInclusionEntryDialog.chooseExclusionPattern(getShell(), fCurrSourceFolder, title,
				message, null, true);
		if (res != null) {
			for (int i = 0; i < res.length; i++) {
				field.addElement(res[i].toString());
			}
		}
	}

	@Override
	public void noDefaultAndApplyButton() {
		super.noDefaultAndApplyButton();
	}
}
