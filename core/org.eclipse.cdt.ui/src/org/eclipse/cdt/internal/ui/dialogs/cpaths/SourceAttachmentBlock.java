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

package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.corext.Assert;
import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.cdt.internal.ui.dialogs.TypedElementSelectionValidator;
import org.eclipse.cdt.internal.ui.util.PixelConverter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * UI to set the source attachment archive and root. Same implementation for both setting attachments for libraries from variable
 * entries and for normal (internal or external) jar.
 * 
 * SourceAttachmentBlock
 */
public class SourceAttachmentBlock {

	private IStatusChangeListener fContext;

	private StringButtonDialogField fFileNameField;
	private SelectionButtonDialogField fWorkspaceButton;
	private SelectionButtonDialogField fExternalFolderButton;

	private IStatus fNameStatus;

	private IWorkspaceRoot fWorkspaceRoot;

	private Control fSWTWidget;
	private CLabel fFullPathResolvedLabel;

	private ICProject fProject;
	private ILibraryEntry fEntry;

	/**
	 * @deprecated
	 */
	public SourceAttachmentBlock(IWorkspaceRoot root, IStatusChangeListener context, ILibraryEntry oldEntry) {
		this(context, oldEntry, null);
	}

	/**
	 * @param context
	 *            listeners for status updates
	 * @param entry
	 *            The entry to edit
	 * @param containerPath
	 *            Path of the container that contains the given entry or <code>null</code> if the entry does not belong to a
	 *            container.
	 * @param project
	 *            Project to which the entry belongs. Can be <code>null</code> if <code>getRunnable</code> is not run and the
	 *            entry does not belong to a container.
	 *  
	 */
	public SourceAttachmentBlock(IStatusChangeListener context, ILibraryEntry entry, ICProject project) {
		Assert.isNotNull(entry);

		fContext = context;
		fEntry = entry;
		fProject = project;

		fWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		fNameStatus = new StatusInfo();

		SourceAttachmentAdapter adapter = new SourceAttachmentAdapter();

		fFileNameField = new StringButtonDialogField(adapter);
		fFileNameField.setDialogFieldListener(adapter);
		fFileNameField.setLabelText(CPathEntryMessages.getString("SourceAttachmentBlock.filename.label")); //$NON-NLS-1$
		fFileNameField.setButtonLabel(CPathEntryMessages.getString("SourceAttachmentBlock.filename.externalfile.button")); //$NON-NLS-1$

		fWorkspaceButton = new SelectionButtonDialogField(SWT.PUSH);
		fWorkspaceButton.setDialogFieldListener(adapter);
		fWorkspaceButton.setLabelText(CPathEntryMessages.getString("SourceAttachmentBlock.filename.internal.button")); //$NON-NLS-1$

		fExternalFolderButton = new SelectionButtonDialogField(SWT.PUSH);
		fExternalFolderButton.setDialogFieldListener(adapter);
		fExternalFolderButton.setLabelText(CPathEntryMessages.getString("SourceAttachmentBlock.filename.externalfolder.button")); //$NON-NLS-1$

		// set the old settings
		setDefaults();
	}

	public void setDefaults() {
		if (fEntry.getSourceAttachmentPath() != null) {
			fFileNameField.setText(fEntry.getSourceAttachmentPath().toString());
		} else {
			fFileNameField.setText(""); //$NON-NLS-1$
		}
	}

	/**
	 * Gets the source attachment path chosen by the user
	 */
	public IPath getSourceAttachmentPath() {
		if (fFileNameField.getText().length() == 0) {
			return null;
		}
		return new Path(fFileNameField.getText());
	}

	/**
	 * Gets the source attachment root chosen by the user Returns null to let JCore automatically detect the root.
	 */
	public IPath getSourceAttachmentRootPath() {
		return null;
	}

	/**
	 * Null for now
	 * 
	 * @return
	 */
	public IPath getSourceAttachmentPrefixMapping() {
		return null;
	}

	/**
	 * Creates the control
	 */
	public Control createControl(Composite parent) {
		PixelConverter converter = new PixelConverter(parent);

		fSWTWidget = parent;

		Composite composite = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 4;
		composite.setLayout(layout);

		int widthHint = converter.convertWidthInCharsToPixels(60);

		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 3;

		Label message = new Label(composite, SWT.LEFT);
		message.setLayoutData(gd);
		message.setText(CPathEntryMessages.getFormattedString(
				"SourceAttachmentBlock.message", fEntry.getLibraryPath().lastSegment())); //$NON-NLS-1$

		fWorkspaceButton.doFillIntoGrid(composite, 1);

		// archive name field
		fFileNameField.doFillIntoGrid(composite, 4);
		LayoutUtil.setWidthHint(fFileNameField.getTextControl(null), widthHint);
		LayoutUtil.setHorizontalGrabbing(fFileNameField.getTextControl(null));

		// aditional 'browse workspace' button for normal jars
		DialogField.createEmptySpace(composite, 3);

		fExternalFolderButton.doFillIntoGrid(composite, 1);

		fFileNameField.postSetFocusOnDialogField(parent.getDisplay());

		Dialog.applyDialogFont(composite);

		//WorkbenchHelp.setHelp(composite, IJavaHelpContextIds.SOURCE_ATTACHMENT_BLOCK);
		return composite;
	}

	private class SourceAttachmentAdapter implements IStringButtonAdapter, IDialogFieldListener {

		// -------- IStringButtonAdapter --------
		public void changeControlPressed(DialogField field) {
			attachmentChangeControlPressed(field);
		}

		// ---------- IDialogFieldListener --------
		public void dialogFieldChanged(DialogField field) {
			attachmentDialogFieldChanged(field);
		}
	}

	void attachmentChangeControlPressed(DialogField field) {
		if (field == fFileNameField) {
			IPath jarFilePath = chooseExtJarFile();
			if (jarFilePath != null) {
				fFileNameField.setText(jarFilePath.toString());
			}
		}
	}

	// ---------- IDialogFieldListener --------

	void attachmentDialogFieldChanged(DialogField field) {
		if (field == fFileNameField) {
			fNameStatus = updateFileNameStatus();
		} else if (field == fWorkspaceButton) {
			IPath jarFilePath = chooseInternalJarFile();
			if (jarFilePath != null) {
				fFileNameField.setText(jarFilePath.toString());
			}
			return;
		} else if (field == fExternalFolderButton) {
			IPath folderPath = chooseExtFolder();
			if (folderPath != null) {
				fFileNameField.setText(folderPath.toString());
			}
			return;
		}
		doStatusLineUpdate();
	}

	private void doStatusLineUpdate() {
		fFileNameField.enableButton(canBrowseFileName());

		// set the resolved path for variable jars
		if (fFullPathResolvedLabel != null) {
			fFullPathResolvedLabel.setText(getResolvedLabelString(fFileNameField.getText(), true));
		}

		IStatus status = StatusUtil.getMostSevere(new IStatus[]{fNameStatus});
		fContext.statusChanged(status);
	}

	private boolean canBrowseFileName() {
		return true;
	}

	private String getResolvedLabelString(String path, boolean osPath) {
		IPath resolvedPath = getResolvedPath(new Path(path));
		if (resolvedPath != null) {
			if (osPath) {
				return resolvedPath.toOSString();
			}
			return resolvedPath.toString();
		}
		return ""; //$NON-NLS-1$
	}

	/*
	 * Do substitution here
	 */
	private IPath getResolvedPath(IPath path) {
		return path;
	}

	private IStatus updateFileNameStatus() {
		StatusInfo status = new StatusInfo();

		String fileName = fFileNameField.getText();
		if (fileName.length() == 0) {
			// no source attachment
			return status;
		}
		if (!Path.EMPTY.isValidPath(fileName)) {
			status.setError(CPathEntryMessages.getString("SourceAttachmentBlock.filename.error.notvalid")); //$NON-NLS-1$
			return status;
		}
		IPath filePath = new Path(fileName);
		File file = filePath.toFile();
		IResource res = fWorkspaceRoot.findMember(filePath);
		if (res != null && res.getLocation() != null) {
			file = res.getLocation().toFile();
		}
		if (!file.exists()) {
			String message = CPathEntryMessages.getFormattedString(
					"SourceAttachmentBlock.filename.error.filenotexists", filePath.toString()); //$NON-NLS-1$
			status.setError(message);
			return status;
		}
		return status;
	}

	/*
	 * Opens a dialog to choose a jar from the file system.
	 */
	private IPath chooseExtJarFile() {
		IPath currPath = new Path(fFileNameField.getText());
		if (currPath.isEmpty()) {
			currPath = fEntry.getPath();
		}

		if (ArchiveFileFilter.isArchivePath(currPath)) {
			currPath = currPath.removeLastSegments(1);
		}

		FileDialog dialog = new FileDialog(getShell());
		dialog.setText(CPathEntryMessages.getString("SourceAttachmentBlock.extjardialog.text")); //$NON-NLS-1$
		dialog.setFilterExtensions(new String[]{"*.jar;*.zip"}); //$NON-NLS-1$
		dialog.setFilterPath(currPath.toOSString());
		String res = dialog.open();
		if (res != null) {
			return new Path(res).makeAbsolute();
		}
		return null;
	}

	private IPath chooseExtFolder() {
		IPath currPath = new Path(fFileNameField.getText());
		if (currPath.isEmpty()) {
			currPath = fEntry.getPath();
		}
		if (ArchiveFileFilter.isArchivePath(currPath)) {
			currPath = currPath.removeLastSegments(1);
		}

		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setText(CPathEntryMessages.getString("SourceAttachmentBlock.extfolderdialog.text")); //$NON-NLS-1$
		dialog.setFilterPath(currPath.toOSString());
		String res = dialog.open();
		if (res != null) {
			return new Path(res).makeAbsolute();
		}
		return null;
	}

	/*
	 * Opens a dialog to choose an internal jar.
	 */
	private IPath chooseInternalJarFile() {
		String initSelection = fFileNameField.getText();

		Class[] acceptedClasses = new Class[]{IFolder.class, IFile.class};
		TypedElementSelectionValidator validator = new TypedElementSelectionValidator(acceptedClasses, false);

		ViewerFilter filter = new ArchiveFileFilter(null, false);

		ILabelProvider lp = new WorkbenchLabelProvider();
		ITreeContentProvider cp = new WorkbenchContentProvider();

		IResource initSel = null;
		if (initSelection.length() > 0) {
			initSel = fWorkspaceRoot.findMember(new Path(initSelection));
		}
		if (initSel == null) {
			initSel = fWorkspaceRoot.findMember(fEntry.getPath());
		}

		FolderSelectionDialog dialog = new FolderSelectionDialog(getShell(), lp, cp);
		dialog.setAllowMultiple(false);
		dialog.setValidator(validator);
		dialog.addFilter(filter);
		dialog.setTitle(CPathEntryMessages.getString("SourceAttachmentBlock.intjardialog.title")); //$NON-NLS-1$
		dialog.setMessage(CPathEntryMessages.getString("SourceAttachmentBlock.intjardialog.message")); //$NON-NLS-1$
		dialog.setInput(fWorkspaceRoot);
		dialog.setInitialSelection(initSel);
		if (dialog.open() == Window.OK) {
			IResource res = (IResource)dialog.getFirstResult();
			return res.getFullPath();
		}
		return null;
	}

	private Shell getShell() {
		if (fSWTWidget != null) {
			return fSWTWidget.getShell();
		}
		return CUIPlugin.getActiveWorkbenchShell();
	}

	/**
	 * Creates a runnable that sets the source attachment by modifying the project's classpath.
	 */
	public IRunnableWithProgress getRunnable(final ICProject jproject, final Shell shell) {
		fProject = jproject;
		return getRunnable(shell);
	}

	/**
	 * Creates a runnable that sets the source attachment by modifying the project's classpath or updating a container.
	 */
	public IRunnableWithProgress getRunnable(final Shell shell) {
		return new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					attachSource(shell, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		};
	}

	protected void attachSource(final Shell shell, IProgressMonitor monitor) throws CoreException {
		boolean isExported = fEntry.isExported();
		ILibraryEntry newEntry;
		newEntry = CoreModel.newLibraryEntry(fEntry.getPath(), fEntry.getBasePath(), fEntry.getLibraryPath(),
				getSourceAttachmentPath(), getSourceAttachmentRootPath(), getSourceAttachmentPrefixMapping(), isExported);
		updateProjectPathEntry(shell, fProject, newEntry, monitor);
	}

	private void updateProjectPathEntry(Shell shell, ICProject cproject, ILibraryEntry newEntry, IProgressMonitor monitor)
			throws CModelException {
		IPathEntry[] oldClasspath = cproject.getRawPathEntries();
		int nEntries = oldClasspath.length;
		ArrayList newEntries = new ArrayList(nEntries + 1);
		int entryKind = newEntry.getEntryKind();
		IPath jarPath = newEntry.getPath();
		boolean found = false;
		for (int i = 0; i < nEntries; i++) {
			IPathEntry curr = oldClasspath[i];
			if (curr.getEntryKind() == entryKind && curr.getPath().equals(jarPath)) {
				// add modified entry
				newEntries.add(newEntry);
				found = true;
			} else {
				newEntries.add(curr);
			}
		}
		if (!found) {
			if (newEntry.getSourceAttachmentPath() == null || !putJarOnClasspathDialog(shell)) {
				return;
			}
			// add new
			newEntries.add(newEntry);
		}
		IPathEntry[] newPathEntries = (IPathEntry[])newEntries.toArray(new IPathEntry[newEntries.size()]);
		cproject.setRawPathEntries(newPathEntries, monitor);
	}

	private boolean putJarOnClasspathDialog(Shell shell) {
		final boolean[] result = new boolean[1];
		shell.getDisplay().syncExec(new Runnable() {

			public void run() {
				String title = CPathEntryMessages.getString("SourceAttachmentBlock.putoncpdialog.title"); //$NON-NLS-1$
				String message = CPathEntryMessages.getString("SourceAttachmentBlock.putoncpdialog.message"); //$NON-NLS-1$
				result[0] = MessageDialog.openQuestion(CUIPlugin.getActiveWorkbenchShell(), title, message);
			}
		});
		return result[0];
	}

}