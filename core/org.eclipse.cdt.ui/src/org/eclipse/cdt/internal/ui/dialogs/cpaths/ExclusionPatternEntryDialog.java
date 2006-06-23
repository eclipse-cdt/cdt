/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.List;

import org.eclipse.cdt.internal.ui.dialogs.StatusDialog;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.TypedElementSelectionValidator;
import org.eclipse.cdt.internal.ui.dialogs.TypedViewerFilter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;

public class ExclusionPatternEntryDialog extends StatusDialog {

	private StringButtonDialogField fExclusionPatternDialog;
	private StatusInfo fExclusionPatternStatus;

	private IContainer fCurrSourceFolder;
	private String fExclusionPattern;
	private List fExistingPatterns;

	public ExclusionPatternEntryDialog(Shell parent, String patternToEdit, List existingPatterns, CPElement entryToEdit) {
		super(parent);
		fExistingPatterns = existingPatterns;
		if (patternToEdit == null) {
			setTitle(CPathEntryMessages.getString("ExclusionPatternEntryDialog.add.title")); //$NON-NLS-1$
		} else {
			setTitle(CPathEntryMessages.getString("ExclusionPatternEntryDialog.edit.title")); //$NON-NLS-1$
			fExistingPatterns.remove(patternToEdit);
		}

		IWorkspaceRoot root = entryToEdit.getCProject().getProject().getWorkspace().getRoot();
		IResource res = root.findMember(entryToEdit.getPath());
		if (res instanceof IContainer) {
			fCurrSourceFolder = (IContainer) res;
		}

		fExclusionPatternStatus = new StatusInfo();

		String label = CPathEntryMessages.getFormattedString("ExclusionPatternEntryDialog.pattern.label", //$NON-NLS-1$
				entryToEdit.getPath().makeRelative().toString());

		ExclusionPatternAdapter adapter = new ExclusionPatternAdapter();
		fExclusionPatternDialog = new StringButtonDialogField(adapter);
		fExclusionPatternDialog.setLabelText(label);
		fExclusionPatternDialog.setButtonLabel(CPathEntryMessages.getString("ExclusionPatternEntryDialog.pattern.button")); //$NON-NLS-1$
		fExclusionPatternDialog.setDialogFieldListener(adapter);
		fExclusionPatternDialog.enableButton(fCurrSourceFolder != null);

		if (patternToEdit == null) {
			fExclusionPatternDialog.setText(""); //$NON-NLS-1$
		} else {
			fExclusionPatternDialog.setText(patternToEdit.toString());
		}
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		int widthHint = convertWidthInCharsToPixels(60);

		Composite inner = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		inner.setLayout(layout);

		Label description = new Label(inner, SWT.WRAP);
		description.setText(CPathEntryMessages.getString("ExclusionPatternEntryDialog.description")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.widthHint = convertWidthInCharsToPixels(80);
		description.setLayoutData(gd);

		fExclusionPatternDialog.doFillIntoGrid(inner, 3);

		LayoutUtil.setWidthHint(fExclusionPatternDialog.getLabelControl(null), widthHint);
		LayoutUtil.setHorizontalSpan(fExclusionPatternDialog.getLabelControl(null), 2);

		LayoutUtil.setWidthHint(fExclusionPatternDialog.getTextControl(null), widthHint);
		LayoutUtil.setHorizontalGrabbing(fExclusionPatternDialog.getTextControl(null));

		fExclusionPatternDialog.postSetFocusOnDialogField(parent.getDisplay());
		applyDialogFont(composite);
		return composite;
	}

	// -------- ExclusionPatternAdapter --------

	private class ExclusionPatternAdapter implements IDialogFieldListener, IStringButtonAdapter {

		// -------- IDialogFieldListener

		public void dialogFieldChanged(DialogField field) {
			doStatusLineUpdate();
		}

		public void changeControlPressed(DialogField field) {
			doChangeControlPressed();
		}
	}

	protected void doChangeControlPressed() {
		IPath pattern = chooseExclusionPattern();
		if (pattern != null) {
			fExclusionPatternDialog.setText(pattern.toString());
		}
	}

	protected void doStatusLineUpdate() {
		checkIfPatternValid();
		updateStatus(fExclusionPatternStatus);
	}

	protected void checkIfPatternValid() {
		String pattern = fExclusionPatternDialog.getText().trim();
		if (pattern.length() == 0) {
			fExclusionPatternStatus.setError(CPathEntryMessages.getString("ExclusionPatternEntryDialog.error.empty")); //$NON-NLS-1$
			return;
		}
		IPath path = new Path(pattern);
		if (path.isAbsolute() || path.getDevice() != null) {
			fExclusionPatternStatus.setError(CPathEntryMessages.getString("ExclusionPatternEntryDialog.error.notrelative")); //$NON-NLS-1$
			return;
		}
		if (fExistingPatterns.contains(pattern)) {
			fExclusionPatternStatus.setError(CPathEntryMessages.getString("ExclusionPatternEntryDialog.error.exists")); //$NON-NLS-1$
			return;
		}

		fExclusionPattern = pattern;
		fExclusionPatternStatus.setOK();
	}

	public String getExclusionPattern() {
		return fExclusionPattern;
	}

	/*
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		//		WorkbenchHelp.setHelp(newShell,
		// ICHelpContextIds.EXCLUSION_PATTERN_DIALOG);
	}

	// ---------- util method ------------

	private IPath chooseExclusionPattern() {
		Class[] acceptedClasses = new Class[] { IFolder.class, IFile.class};
		ISelectionStatusValidator validator = new TypedElementSelectionValidator(acceptedClasses, false);
		ViewerFilter filter = new TypedViewerFilter(acceptedClasses);

		ILabelProvider lp = new WorkbenchLabelProvider();
		ITreeContentProvider cp = new WorkbenchContentProvider();

		IPath initialPath = new Path(fExclusionPatternDialog.getText());
		IResource initialElement = null;
		IContainer curr = fCurrSourceFolder;
		int nSegments = initialPath.segmentCount();
		for (int i = 0; i < nSegments; i++) {
			IResource elem = curr.findMember(initialPath.segment(i));
			if (elem != null) {
				initialElement = elem;
			}
			if (elem instanceof IContainer) {
				curr = (IContainer) elem;
			} else {
				break;
			}
		}

		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), lp, cp);
		dialog.setTitle(CPathEntryMessages.getString("ExclusionPatternEntryDialog.ChooseExclusionPattern.title")); //$NON-NLS-1$
		dialog.setValidator(validator);
		dialog.setMessage(CPathEntryMessages.getString("ExclusionPatternEntryDialog.ChooseExclusionPattern.description")); //$NON-NLS-1$
		dialog.addFilter(filter);
		dialog.setInput(fCurrSourceFolder);
		dialog.setInitialSelection(initialElement);
		dialog.setSorter(new ResourceSorter(ResourceSorter.NAME));

		if (dialog.open() == Window.OK) {
			IResource res = (IResource) dialog.getFirstResult();
			IPath path = res.getFullPath().removeFirstSegments(fCurrSourceFolder.getFullPath().segmentCount()).makeRelative();
			if (res instanceof IContainer) {
				return path.addTrailingSeparator();
			}
			return path;
		}
		return null;
	}

}
