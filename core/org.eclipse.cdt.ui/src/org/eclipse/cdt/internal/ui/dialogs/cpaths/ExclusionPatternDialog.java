/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.ui.dialogs.TypedElementSelectionValidator;
import org.eclipse.cdt.internal.ui.dialogs.TypedViewerFilter;
import org.eclipse.cdt.internal.ui.util.ImageDescriptorRegistry;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

public class ExclusionPatternDialog extends StatusDialog {

	private static class ExclusionPatternLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			ImageDescriptorRegistry registry = CUIPlugin.getImageDescriptorRegistry();
			return registry.get(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_EXCLUSION_FILTER_ATTRIB));
		}

		@Override
		public String getText(Object element) {
			return (String) element;
		}

	}

	private ListDialogField<String> fExclusionPatternList;
	private CPElement fCurrElement;
	private IProject fCurrProject;

	private IContainer fCurrSourceFolder;

	private static final int IDX_ADD = 0;
	private static final int IDX_ADD_MULTIPLE = 1;
	private static final int IDX_EDIT = 2;
	private static final int IDX_REMOVE = 4;

	public ExclusionPatternDialog(Shell parent, CPElement entryToEdit) {
		super(parent);
		fCurrElement = entryToEdit;
		setTitle(CPathEntryMessages.ExclusionPatternDialog_title);

		String label = NLS.bind(CPathEntryMessages.ExclusionPatternDialog_pattern_label,
				entryToEdit.getPath().makeRelative().toString());

		String[] buttonLabels = new String[] { CPathEntryMessages.ExclusionPatternDialog_pattern_add,
				CPathEntryMessages.ExclusionPatternDialog_pattern_add_multiple,
				CPathEntryMessages.ExclusionPatternDialog_pattern_edit, null,
				CPathEntryMessages.ExclusionPatternDialog_pattern_remove };

		ExclusionPatternAdapter adapter = new ExclusionPatternAdapter();

		fExclusionPatternList = new ListDialogField<>(adapter, buttonLabels, new ExclusionPatternLabelProvider());
		fExclusionPatternList.setDialogFieldListener(adapter);
		fExclusionPatternList.setLabelText(label);
		fExclusionPatternList.setRemoveButtonIndex(IDX_REMOVE);
		fExclusionPatternList.enableButton(IDX_EDIT, false);

		fCurrProject = entryToEdit.getCProject().getProject();
		IWorkspaceRoot root = fCurrProject.getWorkspace().getRoot();
		IResource res = root.findMember(entryToEdit.getPath());
		if (res instanceof IContainer) {
			fCurrSourceFolder = (IContainer) res;
		}

		IPath[] pattern = (IPath[]) entryToEdit.getAttribute(CPElement.EXCLUSION);

		ArrayList<String> elements = new ArrayList<>(pattern.length);
		for (IPath element : pattern) {
			elements.add(element.toString());
		}
		fExclusionPatternList.setElements(elements);
		fExclusionPatternList.selectFirstElement();
		fExclusionPatternList.enableButton(IDX_ADD_MULTIPLE, fCurrSourceFolder != null);

		setHelpAvailable(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		Composite inner = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		inner.setLayout(layout);

		fExclusionPatternList.doFillIntoGrid(inner, 3);
		LayoutUtil.setHorizontalSpan(fExclusionPatternList.getLabelControl(null), 2);

		applyDialogFont(composite);
		return composite;
	}

	protected void doCustomButtonPressed(ListDialogField<String> field, int index) {
		if (index == IDX_ADD) {
			addEntry();
		} else if (index == IDX_EDIT) {
			editEntry();
		} else if (index == IDX_ADD_MULTIPLE) {
			addMultipleEntries();
		}
	}

	protected void doDoubleClicked(ListDialogField<String> field) {
		editEntry();
	}

	protected void doSelectionChanged(ListDialogField<String> field) {
		List<String> selected = field.getSelectedElements();
		fExclusionPatternList.enableButton(IDX_EDIT, canEdit(selected));
	}

	private boolean canEdit(List<String> selected) {
		return selected.size() == 1;
	}

	private void editEntry() {

		List<String> selElements = fExclusionPatternList.getSelectedElements();
		if (selElements.size() != 1) {
			return;
		}
		List<String> existing = fExclusionPatternList.getElements();
		String entry = selElements.get(0);
		ExclusionPatternEntryDialog dialog = new ExclusionPatternEntryDialog(getShell(), entry, existing, fCurrElement);
		if (dialog.open() == Window.OK) {
			fExclusionPatternList.replaceElement(entry, dialog.getExclusionPattern());
		}
	}

	private void addEntry() {
		List<String> existing = fExclusionPatternList.getElements();
		ExclusionPatternEntryDialog dialog = new ExclusionPatternEntryDialog(getShell(), null, existing, fCurrElement);
		if (dialog.open() == Window.OK) {
			fExclusionPatternList.addElement(dialog.getExclusionPattern());
		}
	}

	// -------- ExclusionPatternAdapter --------

	private class ExclusionPatternAdapter implements IListAdapter<String>, IDialogFieldListener {
		@Override
		public void customButtonPressed(ListDialogField<String> field, int index) {
			doCustomButtonPressed(field, index);
		}

		@Override
		public void selectionChanged(ListDialogField<String> field) {
			doSelectionChanged(field);
		}

		@Override
		public void doubleClicked(ListDialogField<String> field) {
			doDoubleClicked(field);
		}

		@Override
		public void dialogFieldChanged(DialogField field) {
		}

	}

	protected void doStatusLineUpdate() {
	}

	protected void checkIfPatternValid() {
	}

	public IPath[] getExclusionPattern() {
		IPath[] res = new IPath[fExclusionPatternList.getSize()];
		for (int i = 0; i < res.length; i++) {
			String entry = fExclusionPatternList.getElement(i);
			res[i] = new Path(entry);
		}
		return res;
	}

	/*
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		//		WorkbenchHelp.setHelp(newShell, ICHelpContextIds.EXCLUSION_PATTERN_DIALOG);
	}

	private void addMultipleEntries() {
		Class<?>[] acceptedClasses = new Class<?>[] { IFolder.class, IFile.class };
		ISelectionStatusValidator validator = new TypedElementSelectionValidator(acceptedClasses, true);
		ViewerFilter filter = new TypedViewerFilter(acceptedClasses);

		ILabelProvider lp = new WorkbenchLabelProvider();
		ITreeContentProvider cp = new WorkbenchContentProvider();

		IResource initialElement = null;

		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), lp, cp);
		dialog.setTitle(CPathEntryMessages.ExclusionPatternDialog_ChooseExclusionPattern_title);
		dialog.setValidator(validator);
		dialog.setMessage(CPathEntryMessages.ExclusionPatternDialog_ChooseExclusionPattern_description);
		dialog.addFilter(filter);
		dialog.setInput(fCurrSourceFolder);
		dialog.setInitialSelection(initialElement);
		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));

		if (dialog.open() == Window.OK) {
			Object[] objects = dialog.getResult();
			int existingSegments = fCurrSourceFolder.getFullPath().segmentCount();

			for (Object object : objects) {
				IResource curr = (IResource) object;
				IPath path = curr.getFullPath().removeFirstSegments(existingSegments).makeRelative();
				String res;
				if (curr instanceof IContainer) {
					res = path.addTrailingSeparator().toString();
				} else {
					res = path.toString();
				}
				fExclusionPatternList.addElement(res);
			}
		}
	}

}
