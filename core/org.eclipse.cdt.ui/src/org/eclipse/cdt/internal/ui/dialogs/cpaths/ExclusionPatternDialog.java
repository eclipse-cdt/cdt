/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.dialogs.StatusDialog;
import org.eclipse.cdt.internal.ui.dialogs.TypedElementSelectionValidator;
import org.eclipse.cdt.internal.ui.dialogs.TypedViewerFilter;
import org.eclipse.cdt.internal.ui.util.ImageDescriptorRegistry;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
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
import org.eclipse.ui.views.navigator.ResourceSorter;

public class ExclusionPatternDialog extends StatusDialog {
	
	private static class ExclusionPatternLabelProvider extends LabelProvider {
				
		public Image getImage(Object element) {
			ImageDescriptorRegistry registry= CUIPlugin.getImageDescriptorRegistry();
			return registry.get(CPluginImages.DESC_OBJS_EXCLUSION_FILTER_ATTRIB);
		}

		public String getText(Object element) {
			return (String) element;
		}

	}
	
	
	private ListDialogField fExclusionPatternList;
	private CPElement fCurrElement;
	private IProject fCurrProject;
	
	private IContainer fCurrSourceFolder;
	
	private static final int IDX_ADD= 0;
	private static final int IDX_ADD_MULTIPLE= 1;
	private static final int IDX_EDIT= 2;
	private static final int IDX_REMOVE= 4;
	
		
	public ExclusionPatternDialog(Shell parent, CPElement entryToEdit) {
		super(parent);
		fCurrElement= entryToEdit;
		setTitle(CPathEntryMessages.getString("ExclusionPatternDialog.title")); //$NON-NLS-1$

		String label= CPathEntryMessages.getFormattedString("ExclusionPatternDialog.pattern.label", entryToEdit.getPath().makeRelative().toString()); //$NON-NLS-1$
		
		String[] buttonLabels= new String[] {
			/* IDX_ADD */ CPathEntryMessages.getString("ExclusionPatternDialog.pattern.add"), //$NON-NLS-1$
			/* IDX_ADD_MULTIPLE */ CPathEntryMessages.getString("ExclusionPatternDialog.pattern.add.multiple"), //$NON-NLS-1$
			/* IDX_EDIT */ CPathEntryMessages.getString("ExclusionPatternDialog.pattern.edit"), //$NON-NLS-1$
			null,
			/* IDX_REMOVE */ CPathEntryMessages.getString("ExclusionPatternDialog.pattern.remove") //$NON-NLS-1$
		};

		ExclusionPatternAdapter adapter= new ExclusionPatternAdapter();

		fExclusionPatternList= new ListDialogField(adapter, buttonLabels, new ExclusionPatternLabelProvider());
		fExclusionPatternList.setDialogFieldListener(adapter);
		fExclusionPatternList.setLabelText(label);
		fExclusionPatternList.setRemoveButtonIndex(IDX_REMOVE);
		fExclusionPatternList.enableButton(IDX_EDIT, false);
	
		fCurrProject= entryToEdit.getCProject().getProject();
		IWorkspaceRoot root= fCurrProject.getWorkspace().getRoot();
		IResource res= root.findMember(entryToEdit.getPath());
		if (res instanceof IContainer) {
			fCurrSourceFolder= (IContainer) res;
		}				
		
		IPath[] pattern= (IPath[]) entryToEdit.getAttribute(CPElement.EXCLUSION);
		
		ArrayList elements= new ArrayList(pattern.length);
		for (int i= 0; i < pattern.length; i++) {
			elements.add(pattern[i].toString());
		}
		fExclusionPatternList.setElements(elements);
		fExclusionPatternList.selectFirstElement();
		fExclusionPatternList.enableButton(IDX_ADD_MULTIPLE, fCurrSourceFolder != null);
	}
	
	
	protected Control createDialogArea(Composite parent) {
		Composite composite= (Composite)super.createDialogArea(parent);

		Composite inner= new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		inner.setLayout(layout);
		
		fExclusionPatternList.doFillIntoGrid(inner, 3);
		LayoutUtil.setHorizontalSpan(fExclusionPatternList.getLabelControl(null), 2);
		
		applyDialogFont(composite);		
		return composite;
	}
	
	protected void doCustomButtonPressed(ListDialogField field, int index) {
		if (index == IDX_ADD) {
			addEntry();
		} else if (index == IDX_EDIT) {
			editEntry();
		} else if (index == IDX_ADD_MULTIPLE) {
			addMultipleEntries();
		}
	}
	
	protected void doDoubleClicked(ListDialogField field) {
		editEntry();
	}
	
	protected void doSelectionChanged(ListDialogField field) {
		List selected= field.getSelectedElements();
		fExclusionPatternList.enableButton(IDX_EDIT, canEdit(selected));
	}
	
	private boolean canEdit(List selected) {
		return selected.size() == 1;
	}
	
	private void editEntry() {
		
		List selElements= fExclusionPatternList.getSelectedElements();
		if (selElements.size() != 1) {
			return;
		}
		List existing= fExclusionPatternList.getElements();
		String entry= (String) selElements.get(0);
		ExclusionPatternEntryDialog dialog= new ExclusionPatternEntryDialog(getShell(), entry, existing, fCurrElement);
		if (dialog.open() == Window.OK) {
			fExclusionPatternList.replaceElement(entry, dialog.getExclusionPattern());
		}
	}
	
	private void addEntry() {
		List existing= fExclusionPatternList.getElements();
		ExclusionPatternEntryDialog dialog= new ExclusionPatternEntryDialog(getShell(), null, existing, fCurrElement);
		if (dialog.open() == Window.OK) {
			fExclusionPatternList.addElement(dialog.getExclusionPattern());
		}
	}	
	
	
		
	// -------- ExclusionPatternAdapter --------

	private class ExclusionPatternAdapter implements IListAdapter, IDialogFieldListener {
		/**
		 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter#customButtonPressed(org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField, int)
		 */
		public void customButtonPressed(ListDialogField field, int index) {
			doCustomButtonPressed(field, index);
		}

		/**
		 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter#selectionChanged(org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField)
		 */
		public void selectionChanged(ListDialogField field) {
			doSelectionChanged(field);
		}
		/**
		 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter#doubleClicked(org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField)
		 */
		public void doubleClicked(ListDialogField field) {
			doDoubleClicked(field);
		}

		/**
		 * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener#dialogFieldChanged(org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField)
		 */
		public void dialogFieldChanged(DialogField field) {
		}
		
	}
	
	protected void doStatusLineUpdate() {
	}		
	
	protected void checkIfPatternValid() {
	}
	
		
	public IPath[] getExclusionPattern() {
		IPath[] res= new IPath[fExclusionPatternList.getSize()];
		for (int i= 0; i < res.length; i++) {
			String entry= (String) fExclusionPatternList.getElement(i);
			res[i]= new Path(entry);
		}
		return res;
	}
		
	/*
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
//		WorkbenchHelp.setHelp(newShell, ICHelpContextIds.EXCLUSION_PATTERN_DIALOG);
	}
	
	private void addMultipleEntries() {
		Class[] acceptedClasses= new Class[] { IFolder.class, IFile.class };
		ISelectionStatusValidator validator= new TypedElementSelectionValidator(acceptedClasses, true);
		ViewerFilter filter= new TypedViewerFilter(acceptedClasses);

		ILabelProvider lp= new WorkbenchLabelProvider();
		ITreeContentProvider cp= new WorkbenchContentProvider();
		
		IResource initialElement= null;	

		ElementTreeSelectionDialog dialog= new ElementTreeSelectionDialog(getShell(), lp, cp);
		dialog.setTitle(CPathEntryMessages.getString("ExclusionPatternDialog.ChooseExclusionPattern.title")); //$NON-NLS-1$
		dialog.setValidator(validator);
		dialog.setMessage(CPathEntryMessages.getString("ExclusionPatternDialog.ChooseExclusionPattern.description")); //$NON-NLS-1$
		dialog.addFilter(filter);
		dialog.setInput(fCurrSourceFolder);
		dialog.setInitialSelection(initialElement);
		dialog.setSorter(new ResourceSorter(ResourceSorter.NAME));

		if (dialog.open() == Window.OK) {
			Object[] objects= dialog.getResult();
			int existingSegments= fCurrSourceFolder.getFullPath().segmentCount();
			
			for (int i= 0; i < objects.length; i++) {
				IResource curr= (IResource) objects[i];
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
	
}
