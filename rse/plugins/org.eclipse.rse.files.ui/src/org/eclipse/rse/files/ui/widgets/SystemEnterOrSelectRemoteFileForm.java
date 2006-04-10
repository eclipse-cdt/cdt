/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui.widgets;

import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.validators.ValidatorFileName;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class SystemEnterOrSelectRemoteFileForm extends SystemSelectRemoteFileOrFolderForm {
	
	// text field for file name
	protected Text fileNameText;
	
	// initial file name
	protected String fileName, initialFileName;
	
	// file name validator
	protected ValidatorFileName validator;	

	/**
	 * Constructor for form to enter or select a file.
	 * @param msgLine A GUI widget capable of writing error messages to.
	 * @param caller The wizardpage or dialog hosting this form.
	 * @param fileMode true if in select-file mode, false if in select-folder mode
	 */
	public SystemEnterOrSelectRemoteFileForm(ISystemMessageLine msgLine, Object caller, boolean fileMode) {
		super(msgLine, caller, fileMode);
		validator = new ValidatorFileName();
		initialFileName = "";
		fileName = initialFileName;
	}
	
	/**
	 * Calls super method and creates the text field for file name.
	 * @see org.eclipse.rse.files.ui.widgets.SystemSelectRemoteFileOrFolderForm#createContents(org.eclipse.swt.widgets.Shell, org.eclipse.swt.widgets.Composite)
	 */
	public Control createContents(Shell shell, Composite parent) {
		Control control = super.createContents(shell, parent);
		
		Composite composite = SystemWidgetHelpers.createComposite(parent, 2);

		// file name text field
		fileNameText = SystemWidgetHelpers.createLabeledTextField(
			composite, null, FileResources.RESID_NEWFILE_NAME_LABEL, FileResources.RESID_NEWFILE_NAME_TOOLTIP);
			
		// listen for file name modifications
		fileNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fileName = fileNameText.getText();
				setPageComplete();
			}
		});
		
		// set file name
		if (fileName != null) {
			fileNameText.setText(fileName);
		}
		
		return control;
	}
	
	/**
	 * @see org.eclipse.rse.files.ui.widgets.SystemSelectRemoteFileOrFolderForm#verify()
	 */
	public boolean verify() {
		
		boolean ok = super.verify();
		
		if (ok) {
			
			IRemoteFile file = (IRemoteFile)getSelectedObject();
			IRemoteFile saveasFile = null;
			
			try {
				saveasFile = file.getParentRemoteFileSubSystem().getRemoteFileObject(file, fileName);
			}
			catch (Exception e) {
			}
			
			if (saveasFile != null && saveasFile.exists()) {
				
				SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_UPLOAD_FILE_EXISTS);
				msg.makeSubstitution(fileName);
				SystemMessageDialog dlg = new SystemMessageDialog(getShell(), msg);
				ok = dlg.openQuestionNoException();
			}
		}
			
		return ok;
	}
	
	/**
	 * @see org.eclipse.rse.files.ui.widgets.SystemSelectRemoteFileOrFolderForm#isPageComplete()
	 */
	public boolean isPageComplete() {
		
		SystemMessage errMsg = null;
		
		if (fileName != null) {
			errMsg = validator.validate(fileName);
		} 
		
		if (errMsg != null) {
			setErrorMessage(errMsg);
			return false;			
		}
		else {
			clearErrorMessage();
		}
		
		return fileNameText != null && fileNameText.getText().length() > 0 && super.isPageComplete();
	}
	
	/**
	 * @see org.eclipse.rse.files.ui.widgets.SystemSelectRemoteFileOrFolderForm#setPreSelection(org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile)
	 */
	public void setPreSelection(IRemoteFile selection) {
		
		// if preselect a directory, just call super	
		if (selection.isDirectory()) {
			super.setPreSelection(selection);
		}
		// otherwise, if it's a file, select the parent and set the file name
		else if (selection.isFile()) {
			IRemoteFile parentFile = selection.getParentRemoteFile();
			
			if (parentFile.isDirectory()) {
				super.setPreSelection(parentFile);
			}
		
			initialFileName = parentFile.getName();
			fileName = initialFileName; 
		}
	}
	
	/**
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {
		super.selectionChanged(e);
		
		Object selectedObject = getFirstSelection(e.getSelection());
		
		if (selectedObject != null && selectedObject instanceof IRemoteFile) {
			
			IRemoteFile remoteFile = (IRemoteFile)selectedObject;
			
			ISystemRemoteElementAdapter remoteAdapter = getRemoteAdapter(selectedObject);			
			
			if ((remoteAdapter != null)) {
				
				if (fileNameText != null) {
						
					// simulate the parent file being selected...
					Object parentFile = tree.getSelectedParent();

					if (remoteFile.isFile()) {
						fileName = remoteAdapter.getName(selectedObject);
						fileNameText.setText(fileName);
					}
					else {
						fileName = initialFileName;
						fileNameText.setText(fileName);
						parentFile = remoteFile;
					}

					remoteAdapter = getRemoteAdapter(parentFile);

					if (remoteAdapter != null) {
						String fullPath = remoteAdapter.getAbsoluteName(parentFile);
						setNameText(fullPath);
						outputObjects = new Object[] { parentFile };
						setPageComplete();
					}
				}
			}
		}
	}
	
	/**
	  * Returns the remote element adapter.
	  * @param o the remote element.
	  * @return the remote element adapter.
	  */
	protected ISystemRemoteElementAdapter getRemoteAdapter(Object o) {
		return SystemAdapterHelpers.getRemoteAdapter(o);
	}
	 
	/**
	 * Returns the file name.
	 * @return the file name.
	 */
	public String getFileName() {
		return fileName;
	}
}