/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.files.ui.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorFileName;
import org.eclipse.rse.ui.wizards.AbstractSystemWizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;


/**
 * Default main page of the "New File" wizard.
 * This page asks for the following information:
 * <ul>
 *   <li>New File name
 * </ul> 
 */

public class SystemNewFileWizardMainPage 
 	   extends AbstractSystemWizardPage
 	   implements  ISystemMessages	             
{  

	protected String fileNameStr; 
	protected Text folderName, connectionName, fileName;
	protected Combo folderNames;
	protected SystemMessage errorMessage;
	protected ISystemValidator nameValidator;
	protected IRemoteFile[] parentFolders;
	protected ISystemMessageLine msgLine;
	public String [] allnames;
		  
	/**
	 * Constructor.
	 */
	public SystemNewFileWizardMainPage(Wizard wizard, IRemoteFile[] parentFolders)
	{
		super(wizard, "NewFile", 
  		      FileResources.RESID_NEWFILE_PAGE1_TITLE,  FileResources.RESID_NEWFILE_PAGE1_DESCRIPTION);
	    this.parentFolders = parentFolders; 
	}

	/**
	 * CreateContents is the one method that must be overridden from the parent class.
	 * In this method, we populate an SWT container with widgets and return the container
	 *  to the caller (JFace). This is used as the contents of this page.
	 */
	public Control createContents(Composite parent)
	{

		int nbrColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);	

        // Connection name
		connectionName = SystemWidgetHelpers.createLabeledTextField(composite_prompts, null, FileResources.RESID_NEWFILE_CONNECTIONNAME_LABEL, FileResources.RESID_NEWFILE_CONNECTIONNAME_TIP);
		
		//labelConnectionName.	

        // FolderName		
        if ((parentFolders == null) || (parentFolders.length == 1))
	      folderName = SystemWidgetHelpers.createLabeledTextField(composite_prompts,null, FileResources.RESID_NEWFILE_FOLDER_LABEL,  FileResources.RESID_NEWFILE_FOLDER_TIP);		
	    else     
	      folderNames = SystemWidgetHelpers.createLabeledReadonlyCombo(composite_prompts, null, FileResources.RESID_NEWFILE_FOLDER_LABEL, FileResources.RESID_NEWFILE_FOLDER_TIP);
	
		// File Name
		fileName = SystemWidgetHelpers.createLabeledTextField(composite_prompts, null, FileResources.RESID_NEWFILE_NAME_LABEL, FileResources.RESID_NEWFILE_NAME_TOOLTIP);
		
		initializeInput();
		
		fileName.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateNameInput();
				}
			}
		);			
    		
		SystemWidgetHelpers.setCompositeHelp(composite_prompts, SystemPlugin.HELPPREFIX+NEW_FILE_WIZARD);	
		
		return composite_prompts;		

	}
	
	/**
	 * Return the Control to be given initial focus.
	 * Override from parent. Return control to be given initial focus.
	 */
	protected Control getInitialFocusControl()
	{
        return fileName;
	}
	
	/**
	 * Init values using input data
	 */
	protected void initializeInput()
	{
		connectionName.setEditable(false);
        nameValidator = new ValidatorFileName();
        		
		if ((parentFolders == null) || (parentFolders.length == 0))
		{
		    folderName.setEditable(false);
		    fileName.setEditable(false); // why do we do this??
		    setPageComplete(false);
		    return;
		}
		IRemoteFileSubSystem rfss = parentFolders[0].getParentRemoteFileSubSystem(); 
        connectionName.setText(rfss.getHostAliasName());
        connectionName.setToolTipText((rfss.getHost()).getHostName());

		if (folderName != null)
		{
		    folderName.setText(parentFolders[0].getAbsolutePath());	
		    folderName.setEditable(false);
		}
		else
		{
			String[] names = new String[parentFolders.length];
			for (int idx=0; idx<names.length; idx++)
			   names[idx] = parentFolders[idx].getAbsolutePath();
			folderNames.setItems(names);
			folderNames.select(0);
		}		
	}
	
  	/**
	 * This hook method is called whenever the text changes in the input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 */	
	protected SystemMessage validateNameInput() 
	{	
		errorMessage = null;
		this.clearErrorMessage();
	//	this.setDescription(SystemResources.RESID_NEWFILE_PAGE1_DESCRIPTION));		
	    if (nameValidator != null)
	      errorMessage= nameValidator.validate(fileName.getText());
	    if (errorMessage != null)
		  setErrorMessage(errorMessage);		
		setPageComplete(errorMessage==null);
		return errorMessage;		
	}
	
	/**
	 * Completes processing of the wizard. If this 
	 * method returns true, the wizard will close; 
	 * otherwise, it will stay active.
	 * This method is an override from the parent Wizard class. 
	 *
	 * @return whether the wizard finished successfully
	 */
	public boolean performFinish() 
	{
		
	    return true;
	}
    
	// --------------------------------- //
	// METHODS FOR EXTRACTING USER DATA ... 
	// --------------------------------- //
	/**
	 * Return user-entered new file name.
	 * Call this after finish ends successfully.
	 */
	public String getfileName()
	{
		return fileName.getText();
	}    
	/**
	 * Return the parent folder selected by the user
	 */
	public IRemoteFile getParentFolder()
	{
		if (folderName != null)
		  return parentFolders[0];
		else
		{
			int selIdx = folderNames.getSelectionIndex();
			if (selIdx == -1)
			  selIdx = 0;
			return parentFolders[selIdx];
		}
	}	
	/**
	 * Return true if the page is complete, so to enable Finish.
	 * Called by wizard framework.
	 */
	public boolean isPageComplete()
	{
		return (errorMessage==null) && (fileName.getText().trim().length()>0);
	}
	

}