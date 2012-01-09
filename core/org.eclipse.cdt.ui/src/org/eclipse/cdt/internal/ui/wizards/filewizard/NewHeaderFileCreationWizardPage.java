/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.filewizard;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CodeGeneration;

import org.eclipse.cdt.internal.core.model.CProject;
import org.eclipse.cdt.internal.corext.codemanipulation.StubUtility;

import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class NewHeaderFileCreationWizardPage extends AbstractFileCreationWizardPage {
	private final String KEY_LAST_USED_TEMPLATE = "LastUsedHeaderTemplate"; //$NON-NLS-1$

	private ITranslationUnit fNewFileTU = null;
	private StringDialogField fNewFileDialogField;
	
	public NewHeaderFileCreationWizardPage() {
		super(NewFileWizardMessages.NewHeaderFileCreationWizard_title);
		setTitle(NewFileWizardMessages.NewHeaderFileCreationWizardPage_title);
		setDescription(NewFileWizardMessages.NewHeaderFileCreationWizardPage_description); 

		fNewFileDialogField = new StringDialogField();
		fNewFileDialogField.setDialogFieldListener(new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(DialogField field) {
				handleFieldChanged(NEW_FILE_ID);
			}
		});
		fNewFileDialogField.setLabelText(NewFileWizardMessages.NewHeaderFileCreationWizardPage_headerFile_label); 
	}
	
	/**
	 * Sets the focus on the starting input field.
	 */		
	@Override
	protected void setFocus() {
		fNewFileDialogField.setFocus();
	}

	/**
	 * Creates the controls for the file name field. Expects a <code>GridLayout</code> with at 
	 * least 2 columns.
	 * 
	 * @param parent the parent composite
	 * @param nColumns number of columns to span
	 */		
	@Override
	protected void createFileControls(Composite parent, int nColumns) {
		fNewFileDialogField.doFillIntoGrid(parent, nColumns);
		Text textControl = fNewFileDialogField.getTextControl(null);
		LayoutUtil.setWidthHint(textControl, getMaxFieldWidth());
		textControl.addFocusListener(new StatusFocusListener(NEW_FILE_ID));
	}
	
	@Override
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

	@Override
	protected IStatus fileNameChanged() {
		StatusInfo status = new StatusInfo();
		
		IPath filePath = getFileFullPath();
		if (filePath == null) {
			status.setError(NewFileWizardMessages.NewHeaderFileCreationWizardPage_error_EnterFileName); 
			return status;
		}

		IPath sourceFolderPath = getSourceFolderFullPath();
		if (sourceFolderPath == null || !sourceFolderPath.isPrefixOf(filePath)) {
			status.setError(NewFileWizardMessages.NewHeaderFileCreationWizardPage_error_FileNotInSourceFolder); 
			return status;
		}
		
		// check if file already exists
		IResource file = getWorkspaceRoot().findMember(filePath);
		if (file != null && file.exists()) {
	    	if (file.getType() == IResource.FILE) {
	    		status.setError(NewFileWizardMessages.NewHeaderFileCreationWizardPage_error_FileExists); 
	    	} else if (file.getType() == IResource.FOLDER) {
	    		status.setError(NewFileWizardMessages.NewHeaderFileCreationWizardPage_error_MatchingFolderExists); 
	    	} else {
	    		status.setError(NewFileWizardMessages.NewHeaderFileCreationWizardPage_error_MatchingResourceExists); 
	    	}
			return status;
		}
		
		// check if folder exists
		IPath folderPath = filePath.removeLastSegments(1).makeRelative();
		IResource folder = getWorkspaceRoot().findMember(folderPath);
		if (folder == null || !folder.exists() || (folder.getType() != IResource.PROJECT && folder.getType() != IResource.FOLDER)) {
		    status.setError(NLS.bind(NewFileWizardMessages.NewHeaderFileCreationWizardPage_error_FolderDoesNotExist, folderPath)); 
			return status;
		}

		IStatus convStatus = CConventions.validateHeaderFileName(getCurrentProject(), filePath.lastSegment());
		if (convStatus.getSeverity() == IStatus.ERROR) {
			status.setError(NLS.bind(NewFileWizardMessages.NewHeaderFileCreationWizardPage_error_InvalidFileName, convStatus.getMessage())); 
			return status;
		} else if (convStatus.getSeverity() == IStatus.WARNING) {
			status.setWarning(NLS.bind(NewFileWizardMessages.NewHeaderFileCreationWizardPage_warning_FileNameDiscouraged, convStatus.getMessage())); 
		}
		return status;
	}
	
	@Override
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
	            	if (fNewFileTU != null) {
	            		String lineDelimiter= StubUtility.getLineDelimiterUsed(fNewFileTU);
						String content= CodeGeneration.getHeaderFileContent(getTemplate(),
								fNewFileTU, lineDelimiter);
						if (content != null) {
							fNewFileTU.getBuffer().setContents(content.toCharArray());
							fNewFileTU.save(monitor, true);
						}
	            	}
	            }
	        } finally {
	            monitor.done();
	        }
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.wizards.filewizard.AbstractFileCreationWizardPage#getCreatedFileTU()
	 */
	@Override
	public ITranslationUnit getCreatedFileTU() {
		return fNewFileTU;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.wizards.filewizard.AbstractFileCreationWizardPage#getApplicableTemplates()
	 */
	@Override
	protected Template[] getApplicableTemplates() {
		return StubUtility.getFileTemplatesForContentTypes(
				new String[] { CCorePlugin.CONTENT_TYPE_CXXHEADER, CCorePlugin.CONTENT_TYPE_CHEADER }, null);
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.wizards.filewizard.AbstractFileCreationWizardPage#getDefaultTemplateName()
	 */
	@Override
	public String getDefaultTemplateName() {
		String name = getDialogSettings().get(KEY_LAST_USED_TEMPLATE);
		if (name == null) {
			IProject project = getCurrentProject();
			if (project != null) {
				String contentType = CProject.hasCCNature(project) ?
						CCorePlugin.CONTENT_TYPE_CXXHEADER : CCorePlugin.CONTENT_TYPE_CHEADER;
				Template[] templates =
						StubUtility.getFileTemplatesForContentTypes(new String[] { contentType }, null);
				if (templates.length != 0) {
					name = templates[0].getName();
				}
			}
		}
		return name;
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.wizards.filewizard.AbstractFileCreationWizardPage#savePreferredTemplateName(String)
	 */
	@Override
	public void saveLastUsedTemplateName(String name) {
		getDialogSettings().put(KEY_LAST_USED_TEMPLATE, name);
	}
}
