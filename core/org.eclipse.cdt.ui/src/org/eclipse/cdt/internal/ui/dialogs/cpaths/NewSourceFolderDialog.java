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

import java.util.List;

import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @deprecated as of CDT 4.0. This class was used for property pages
 * for 3.X style projects.
 */
@Deprecated
public class NewSourceFolderDialog extends StatusDialog {

	private SelectionButtonDialogField fUseProjectButton;
	private SelectionButtonDialogField fUseFolderButton;

	private StringDialogField fContainerDialogField;
	private StatusInfo fContainerFieldStatus;

	private IContainer fFolder;
	private List<IContainer> fExistingFolders;
	private IProject fCurrProject;

	public NewSourceFolderDialog(Shell parent, String title, IProject project, List<IContainer> existingFolders,
			CPElement entryToEdit) {
		super(parent);
		setTitle(title);

		fContainerFieldStatus = new StatusInfo();

		SourceContainerAdapter adapter = new SourceContainerAdapter();

		fUseProjectButton = new SelectionButtonDialogField(SWT.RADIO);
		fUseProjectButton.setLabelText(CPathEntryMessages.NewSourceFolderDialog_useproject_button);
		fUseProjectButton.setDialogFieldListener(adapter);

		fUseFolderButton = new SelectionButtonDialogField(SWT.RADIO);
		fUseFolderButton.setLabelText(CPathEntryMessages.NewSourceFolderDialog_usefolder_button);
		fUseFolderButton.setDialogFieldListener(adapter);

		fContainerDialogField = new StringDialogField();
		fContainerDialogField.setDialogFieldListener(adapter);
		fContainerDialogField.setLabelText(CPathEntryMessages.NewSourceFolderDialog_sourcefolder_label);

		fUseFolderButton.attachDialogField(fContainerDialogField);

		fFolder = null;
		fExistingFolders = existingFolders;
		fCurrProject = project;

		boolean useFolders = true;
		if (entryToEdit == null) {
			fContainerDialogField.setText(""); //$NON-NLS-1$
		} else {
			IPath editPath = entryToEdit.getPath().removeFirstSegments(1);
			fContainerDialogField.setText(editPath.toString());
			useFolders = !editPath.isEmpty();
		}
		fUseFolderButton.setSelection(useFolders);
		fUseProjectButton.setSelection(!useFolders);

		setHelpAvailable(false);
	}

	public void setMessage(String message) {
		fContainerDialogField.setLabelText(message);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		Composite inner = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		inner.setLayout(layout);

		int widthHint = convertWidthInCharsToPixels(50);

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = widthHint;

		if (fExistingFolders.contains(fCurrProject)) {
			fContainerDialogField.doFillIntoGrid(inner, 2);
		} else {
			fUseProjectButton.doFillIntoGrid(inner, 1);
			fUseFolderButton.doFillIntoGrid(inner, 1);
			fContainerDialogField.getTextControl(inner);

			int horizontalIndent = convertWidthInCharsToPixels(3);
			data.horizontalIndent = horizontalIndent;
		}
		Control control = fContainerDialogField.getTextControl(null);
		control.setLayoutData(data);

		fContainerDialogField.postSetFocusOnDialogField(parent.getDisplay());
		applyDialogFont(composite);
		return composite;
	}

	// -------- SourceContainerAdapter --------

	private class SourceContainerAdapter implements IDialogFieldListener {

		// -------- IDialogFieldListener

		@Override
		public void dialogFieldChanged(DialogField field) {
			doStatusLineUpdate();
		}
	}

	protected void doStatusLineUpdate() {
		checkIfPathValid();
		updateStatus(fContainerFieldStatus);
	}

	protected void checkIfPathValid() {
		fFolder = null;
		IContainer folder = null;
		if (fUseFolderButton.isSelected()) {
			String pathStr = fContainerDialogField.getText();
			if (pathStr.length() == 0) {
				fContainerFieldStatus.setError(CPathEntryMessages.NewSourceFolderDialog_error_enterpath);
				return;
			}
			IPath path = fCurrProject.getFullPath().append(pathStr);
			IWorkspace workspace = fCurrProject.getWorkspace();

			IStatus pathValidation = workspace.validatePath(path.toString(), IResource.FOLDER);
			if (!pathValidation.isOK()) {
				fContainerFieldStatus.setError(NLS.bind(CPathEntryMessages.NewSourceFolderDialog_error_invalidpath,
						pathValidation.getMessage()));
				return;
			}
			folder = fCurrProject.getFolder(pathStr);
		} else {
			folder = fCurrProject;
		}
		if (isExisting(folder)) {
			fContainerFieldStatus.setError(CPathEntryMessages.NewSourceFolderDialog_error_pathexists);
			return;
		}
		fContainerFieldStatus.setOK();
		fFolder = folder;
	}

	private boolean isExisting(IContainer folder) {
		return fExistingFolders.contains(folder);
	}

	public IContainer getSourceFolder() {
		return fFolder;
	}

	/*
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		//		WorkbenchHelp.setHelp(newShell,
		// ICHelpContextIds.NEW_CONTAINER_DIALOG);
	}

}
