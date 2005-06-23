/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.filewizard;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class NewSourceFileCreationWizardPage extends AbstractFileCreationWizardPage {
	private ITranslationUnit fNewFileTU = null;
	private StringDialogField fNewFileDialogField;
	
	public NewSourceFileCreationWizardPage() {
		super(NewFileWizardMessages.getString("NewSourceFileCreationWizardPage.title")); //$NON-NLS-1$
		setDescription(NewFileWizardMessages.getString("NewSourceFileCreationWizardPage.description")); //$NON-NLS-1$

		fNewFileDialogField = new StringDialogField();
		fNewFileDialogField.setDialogFieldListener(new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				handleFieldChanged(NEW_FILE_ID);
			}
		});
		fNewFileDialogField.setLabelText(NewFileWizardMessages.getString("NewSourceFileCreationWizardPage.sourceFile.label")); //$NON-NLS-1$
	}
	
	/**
	 * Sets the focus on the starting input field.
	 */		
	protected void setFocus() {
		fNewFileDialogField.setFocus();
	}

	/**
	 * Creates the controls for the file name field. Expects a <code>GridLayout</code> with at 
	 * least 3 columns.
	 * 
	 * @param composite the parent composite
	 * @param nColumns number of columns to span
	 */		
	protected void createFileControls(Composite parent, int nColumns) {
		fNewFileDialogField.doFillIntoGrid(parent, nColumns);
		Text textControl = fNewFileDialogField.getTextControl(null);
		LayoutUtil.setWidthHint(textControl, getMaxFieldWidth());
		textControl.addFocusListener(new StatusFocusListener(NEW_FILE_ID));
	}
	
	public IPath getFileFullPath() {
		String str = fNewFileDialogField.getText();
        IPath path = null;
	    if (str.length() > 0) {
	        path = new Path(str);
	        if (!path.isAbsolute()) {
	            IPath folderPath = getSourceFolderFullPath();
	        	if (folderPath != null)
	        	    path = folderPath.append(path);
	        }
	    }
	    return path;
	}
	
	protected IStatus fileNameChanged() {
		StatusInfo status = new StatusInfo();
		
		IPath filePath = getFileFullPath();
		if (filePath == null) {
			status.setError(NewFileWizardMessages.getString("NewSourceFileCreationWizardPage.error.EnterFileName")); //$NON-NLS-1$
			return status;
		}

		IPath sourceFolderPath = getSourceFolderFullPath();
		if (sourceFolderPath == null || !sourceFolderPath.isPrefixOf(filePath)) {
			status.setError(NewFileWizardMessages.getString("NewSourceFileCreationWizardPage.error.FileNotInSourceFolder")); //$NON-NLS-1$
			return status;
		}
		
		// check if file already exists
		IResource file = getWorkspaceRoot().findMember(filePath);
		if (file != null && file.exists()) {
	    	if (file.getType() == IResource.FILE) {
	    		status.setError(NewFileWizardMessages.getString("NewSourceFileCreationWizardPage.error.FileExists")); //$NON-NLS-1$
	    	} else if (file.getType() == IResource.FOLDER) {
	    		status.setError(NewFileWizardMessages.getString("NewSourceFileCreationWizardPage.error.MatchingFolderExists")); //$NON-NLS-1$
	    	} else {
	    		status.setError(NewFileWizardMessages.getString("NewSourceFileCreationWizardPage.error.MatchingResourceExists")); //$NON-NLS-1$
	    	}
			return status;
		}
		
		// check if folder exists
		IPath folderPath = filePath.removeLastSegments(1).makeRelative();
		IResource folder = getWorkspaceRoot().findMember(folderPath);
		if (folder == null || !folder.exists() || (folder.getType() != IResource.PROJECT && folder.getType() != IResource.FOLDER)) {
		    status.setError(NewFileWizardMessages.getFormattedString("NewSourceFileCreationWizardPage.error.FolderDoesNotExist", folderPath)); //$NON-NLS-1$
			return status;
		}

		IStatus convStatus = CConventions.validateSourceFileName(getCurrentProject(), filePath.lastSegment());
		if (convStatus.getSeverity() == IStatus.ERROR) {
			status.setError(NewFileWizardMessages.getFormattedString("NewSourceFileCreationWizardPage.error.InvalidFileName", convStatus.getMessage())); //$NON-NLS-1$
			return status;
		} else if (convStatus.getSeverity() == IStatus.WARNING) {
			status.setWarning(NewFileWizardMessages.getFormattedString("NewSourceFileCreationWizardPage.warning.FileNameDiscouraged", convStatus.getMessage())); //$NON-NLS-1$
		}
		return status;
	}
	
	public void createFile(IProgressMonitor monitor) throws CoreException {
        IPath filePath = getFileFullPath();
        if (filePath != null) {
            if (monitor == null)
	            monitor = new NullProgressMonitor();
            try {
	            fNewFileTU = null;
	            IFile newFile = NewSourceFileGenerator.createSourceFile(filePath, true, monitor);
	            if (newFile != null) {
	            	fNewFileTU = (ITranslationUnit) CoreModel.getDefault().create(newFile);
	            }
	        } finally {
	            monitor.done();
	        }
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.wizards.filewizard.AbstractFileCreationWizardPage#getCreatedFileTU()
	 */
	public ITranslationUnit getCreatedFileTU() {
		return fNewFileTU;
	}
}
