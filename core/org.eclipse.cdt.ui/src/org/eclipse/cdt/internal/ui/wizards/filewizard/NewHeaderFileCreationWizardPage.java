/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.filewizard;

import java.io.File;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class NewHeaderFileCreationWizardPage extends AbstractFileCreationWizardPage {
	
	private ITranslationUnit fNewFileTU = null;
	private StringDialogField fNewFileDialogField;
	
	public NewHeaderFileCreationWizardPage() {
		super(NewFileWizardMessages.getString("NewHeaderFileCreationWizardPage.title"));
		setDescription(NewFileWizardMessages.getString("NewHeaderFileCreationWizardPage.description")); //$NON-NLS-1$

		fNewFileDialogField = new StringDialogField();
		fNewFileDialogField.setDialogFieldListener(new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				handleFieldChanged(NEW_FILE_ID);
			}
		});
		fNewFileDialogField.setLabelText(NewFileWizardMessages.getString("NewHeaderFileCreationWizardPage.headerFile.label")); //$NON-NLS-1$
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
			status.setError(NewFileWizardMessages.getString("NewHeaderFileCreationWizardPage.error.EnterFileName")); //$NON-NLS-1$
			return status;
		}
		
		// check if file already exists
		IPath workspacePath = getWorkspaceRoot().getLocation();
	    File file = workspacePath.append(filePath).toFile();
//	    if (file == null || !file.exists()) {
//			IResource res = fWorkspaceRoot.findMember(path);
//			if (res != null && res.exists()) {
//				file = res.getLocation().toFile();
//			}
//		}
	    if (file != null && file.exists()) {
	    	if (file.isFile()) {
	    		status.setError(NewFileWizardMessages.getString("NewHeaderFileCreationWizardPage.error.FileExists")); //$NON-NLS-1$
	    	} else if (file.isDirectory()) {
	    		status.setError(NewFileWizardMessages.getString("NewHeaderFileCreationWizardPage.error.MatchingFolderExists")); //$NON-NLS-1$
	    	} else {
	    		status.setError(NewFileWizardMessages.getString("NewHeaderFileCreationWizardPage.error.MatchingResourceExists")); //$NON-NLS-1$
	    	}
			return status;
		} else if (filePath.segmentCount() > 1) {
			IPath parentFolderPath = workspacePath.append(filePath).removeLastSegments(1);
			File folder = parentFolderPath.toFile();
			if (folder != null && folder.exists() && folder.isDirectory()) {
			    // folder exists
			} else {
				status.setError(NewFileWizardMessages.getFormattedString("NewHeaderFileCreationWizardPage.error.FolderDoesNotExist", PathUtil.getWorkspaceRelativePath(parentFolderPath))); //$NON-NLS-1$
				return status;
			}
		}

		// make sure file is inside source folder
		IPath folderPath = getSourceFolderFullPath();
		if (folderPath != null && !folderPath.isPrefixOf(filePath)) {
			status.setError(NewFileWizardMessages.getString("NewHeaderFileCreationWizardPage.error.FileNotInSourceFolder")); //$NON-NLS-1$
			return status;
		}

		IStatus convStatus = CConventions.validateHeaderFileName(getCurrentProject(), filePath.lastSegment());
		if (convStatus.getSeverity() == IStatus.ERROR) {
			status.setError(NewFileWizardMessages.getFormattedString("NewHeaderFileCreationWizardPage.error.InvalidFileName", convStatus.getMessage())); //$NON-NLS-1$
			return status;
		} else if (convStatus.getSeverity() == IStatus.WARNING) {
			status.setWarning(NewFileWizardMessages.getFormattedString("NewHeaderFileCreationWizardPage.warning.FileNameDiscouraged", convStatus.getMessage())); //$NON-NLS-1$
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
	            IFile newFile = NewSourceFileGenerator.createHeaderFile(filePath, true, monitor);
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
