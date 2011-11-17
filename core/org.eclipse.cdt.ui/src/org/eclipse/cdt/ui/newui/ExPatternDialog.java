/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.dialogs.TypedElementSelectionValidator;
import org.eclipse.cdt.internal.ui.dialogs.TypedViewerFilter;
import org.eclipse.cdt.internal.ui.dialogs.cpaths.CPathEntryMessages;
import org.eclipse.cdt.internal.ui.util.ImageDescriptorRegistry;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ExPatternDialog extends StatusDialog {

	private ListDialogField<String> fExclusionPatternList;
	private IProject fCurrProject;
	private IPath[] pattern;
	private IPath path;
	private IContainer fCurrSourceFolder;

	private static final int IDX_ADD= 0;
	private static final int IDX_ADD_MULTIPLE= 1;
	private static final int IDX_EDIT= 2;
	private static final int IDX_REMOVE= 4;

	public ExPatternDialog(Shell parent, IPath[] _data, IPath _path, IProject proj) {
		super(parent);
		fCurrProject = proj;
		pattern = _data;
		path = _path;
		setTitle(CPathEntryMessages.ExclusionPatternDialog_title);

		String label= NLS.bind(CPathEntryMessages.ExclusionPatternDialog_pattern_label,
				path.makeRelative().toString());

		String[] buttonLabels= new String[] {
			CPathEntryMessages.ExclusionPatternDialog_pattern_add,
			CPathEntryMessages.ExclusionPatternDialog_pattern_add_multiple,
			CPathEntryMessages.ExclusionPatternDialog_pattern_edit,
			null,
			CPathEntryMessages.ExclusionPatternDialog_pattern_remove
		};

		ExclusionPatternAdapter adapter= new ExclusionPatternAdapter();

		fExclusionPatternList= new ListDialogField<String>(adapter, buttonLabels, new ExPatternLabelProvider());
		fExclusionPatternList.setDialogFieldListener(adapter);
		fExclusionPatternList.setLabelText(label);
		fExclusionPatternList.setRemoveButtonIndex(IDX_REMOVE);
		fExclusionPatternList.enableButton(IDX_EDIT, false);

		IWorkspaceRoot root= fCurrProject.getWorkspace().getRoot();
		IResource res= root.findMember(path);
		if (res instanceof IContainer) {
			fCurrSourceFolder= (IContainer) res;
		}

		ArrayList<String> elements= new ArrayList<String>(pattern.length);
		for (IPath p : pattern)
			elements.add(p.toString());
		fExclusionPatternList.setElements(elements);
		fExclusionPatternList.selectFirstElement();
		fExclusionPatternList.enableButton(IDX_ADD_MULTIPLE, fCurrSourceFolder != null);

		setHelpAvailable(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite= (Composite) super.createDialogArea(parent);

		Composite inner= new Composite(composite, SWT.NONE);
		inner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout= new GridLayout(2, false);
		layout.marginHeight= 0;
		layout.marginWidth= 0;
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
		List<String> selected= field.getSelectedElements();
		fExclusionPatternList.enableButton(IDX_EDIT, canEdit(selected));
	}

	private boolean canEdit(List<?> selected) {
		return selected.size() == 1;
	}

	private void editEntry() {

		List<String> selElements= fExclusionPatternList.getSelectedElements();
		if (selElements.size() != 1) {
			return;
		}
		List<String> existing= fExclusionPatternList.getElements();
		String entry= selElements.get(0);
		ExPatternEntryDialog dialog= new ExPatternEntryDialog(getShell(), entry, existing, fCurrProject, path);
		if (dialog.open() == Window.OK) {
			fExclusionPatternList.replaceElement(entry, dialog.getExclusionPattern());
		}
	}

	private void addEntry() {
		List<String> existing= fExclusionPatternList.getElements();
		ExPatternEntryDialog dialog= new ExPatternEntryDialog(getShell(), null, existing, fCurrProject, path);
		if (dialog.open() == Window.OK) {
			fExclusionPatternList.addElement(dialog.getExclusionPattern());
		}
	}

	protected void doStatusLineUpdate() {
	}

	protected void checkIfPatternValid() {
	}

	public IPath[] getExclusionPattern() {
		IPath[] res= new IPath[fExclusionPatternList.getSize()];
		for (int i= 0; i < res.length; i++) {
			String entry= fExclusionPatternList.getElement(i);
			res[i]= new Path(entry);
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

    @Override
	protected boolean isResizable() {
    	return true;
    }

	private void addMultipleEntries() {
		Class<?>[] acceptedClasses= new Class<?>[] { IFolder.class, IFile.class };
		ISelectionStatusValidator validator= new TypedElementSelectionValidator(acceptedClasses, true);
		ViewerFilter filter= new TypedViewerFilter(acceptedClasses);

		ILabelProvider lp= new WorkbenchLabelProvider();
		ITreeContentProvider cp= new WorkbenchContentProvider();

		IResource initialElement= null;

		ElementTreeSelectionDialog dialog= new ElementTreeSelectionDialog(getShell(), lp, cp);
		dialog.setTitle(CPathEntryMessages.ExclusionPatternDialog_ChooseExclusionPattern_title);
		dialog.setValidator(validator);
		dialog.setMessage(CPathEntryMessages.ExclusionPatternDialog_ChooseExclusionPattern_description);
		dialog.addFilter(filter);
		dialog.setInput(fCurrSourceFolder);
		dialog.setInitialSelection(initialElement);
		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));

		if (dialog.open() == Window.OK) {
			Object[] objects= dialog.getResult();
			int existingSegments= fCurrSourceFolder.getFullPath().segmentCount();

			for (Object object : objects) {
				IResource curr= (IResource) object;
				IPath path= curr.getFullPath().removeFirstSegments(existingSegments).makeRelative();
				String res;
				if (curr instanceof IContainer) {
					res= path.addTrailingSeparator().toString();
				} else {
					res= path.toString();
				}
				fExclusionPatternList.addElement(res);
			}
		}
	}

	private static class ExPatternLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			ImageDescriptorRegistry registry= CUIPlugin.getImageDescriptorRegistry();
			return registry.get(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_EXCLUSION_FILTER_ATTRIB));
		}

		@Override
		public String getText(Object element) {
			return (String) element;
		}
	}

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
}
