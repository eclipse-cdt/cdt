/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.files.ui.resources;
import java.util.ArrayList;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.files.ui.widgets.SystemSelectRemoteFileOrFolderForm;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.SystemFilterSimple;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.ValidatorArchiveName;
import org.eclipse.rse.ui.validators.ValidatorFileName;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * A dialog to select or enter archive files.
 */
public class CombineForm extends SystemSelectRemoteFileOrFolderForm 
{

	protected Text fileNameText;
	protected Combo fileTypeCombo;
	protected Label nameAndTypeLabel;
	protected String fileName, fileType, initialFileName;
	protected ValidatorFileName validator;
	protected ValidatorArchiveName arcvalidator;
	protected boolean prePop;	
	protected String nameAndTypePrompt = "";
	protected String[] disallowedExtensions;
	 
   /**
	* Constructor for ExtractToForm
	*/
   public CombineForm(ISystemMessageLine msgLine, Object caller, boolean fileMode) 
   {
	   super(msgLine, caller, fileMode);
	   validator = new ValidatorFileName();
	   arcvalidator = new ValidatorArchiveName();
	   prePop = false;
   }
   
   public CombineForm(ISystemMessageLine msgLine, Object caller, boolean fileMode, boolean prePopSelection) 
   {
	   super(msgLine, caller, fileMode);
		
	   validator = new ValidatorFileName();
	   arcvalidator = new ValidatorArchiveName();
	   prePop = prePopSelection;
   }
	
   /**
	* In this method, we populate the given SWT container with widgets and return the container
	*  to the caller. 
	* @param parent The parent composite
	*/
   public Control createContents(Shell shell, Composite parent)	
   {
	   Control control = super.createContents(shell, parent);
		
	   Composite composite = SystemWidgetHelpers.createComposite(parent, 1);
	   
		nameAndTypeLabel = SystemWidgetHelpers.createLabel(composite, nameAndTypePrompt);
	   Composite subcomp1 = SystemWidgetHelpers.createComposite(composite, 2);
	   fileNameText = SystemWidgetHelpers.createLabeledTextField(
		   subcomp1, null, FileResources.RESID_COMBINE_NAME_LABEL, FileResources.RESID_COMBINE_NAME_TOOLTIP);
	   fileTypeCombo = SystemWidgetHelpers.createLabeledReadonlyCombo(subcomp1, null, FileResources.RESID_COMBINE_TYPE_LABEL, FileResources.RESID_COMBINE_TYPE_TOOLTIP);
	   
	   String[] allowedExtensions = getAllowedArchiveExtensions();
	   
	   if (allowedExtensions != null && allowedExtensions.length != 0) {
	   		fileTypeCombo.setItems(allowedExtensions);
	   		fileTypeCombo.select(0);
	   		fileType = fileTypeCombo.getText();
	   }

	   fileNameText.addModifyListener(new ModifyListener() {
		
		public void modifyText(ModifyEvent e) {
			   fileName = fileNameText.getText();
			   setPageComplete();
			   if (fileName.indexOf(".") != -1)
			   {
			   		SystemMessage isValidMsg = arcvalidator.validate(fileName);
			   		if (isValidMsg == null)
			   		{
			   			int i = fileName.lastIndexOf(".");
						fileType = fileName.substring(i+1);
			   			fileTypeCombo.setText(fileType);
			   		}
			   }
		   }
	   });
		
	   fileTypeCombo.addModifyListener(new ModifyListener() {
		
		public void modifyText(ModifyEvent e) {
				fileType = fileTypeCombo.getText();
				setPageComplete();
			}
		});
					
	   if (fileName != null)
	   {
		   fileNameText.setText(fileName);
	   }
		
	   return control;
   }
   
   protected String[] getAllowedArchiveExtensions() {
   	
   		String[] allExtensions = ArchiveHandlerManager.getInstance().getRegisteredExtensions();
   		
   		ArrayList list = new ArrayList();
   		
   		for (int i = 0; i < allExtensions.length; i++) {
   			
   			String extension = allExtensions[i];
   			
   			boolean keep = true;
   			
   			for (int j = 0; j < disallowedExtensions.length; j++) {
   				
   				if (disallowedExtensions[j].toLowerCase().equals(extension)) {
   					
   					keep = false;
   					break;
   				}
   			}
   			
   			if (keep) {
   				list.add(extension);
   			}
   		}
   		
   		String[] allowedExtensions = new String[list.size()];
   		
   		for (int i = 0; i < list.size(); i++) {
   			allowedExtensions[i] = (String)(list.get(i));
   		}
   		
   		return allowedExtensions;
   }
   
   public String[] getDisallowedArchiveExtensions() {
   		return disallowedExtensions;
   }
   
   /**
    * Sets the extensions to disallow.
    * @param extensions the archive extensions that will not be allowed.
    */
   public void setDisallowedArchiveExtensions(String[] extensions) {
   		this.disallowedExtensions = extensions;
   }
	
   /**
	* Completes processing of the dialog.
	* Intercept of parent method.
	* 
	* @return true if no errors
	*/
   public boolean verify() 
   {
	   // This method added by Phil to issue warning msg for existing member
	   boolean ok = super.verify();
	   
	   if (ok)
	   {
		   IRemoteFile file = (IRemoteFile)getSelectedObject();
		   IRemoteFile saveasFile = null;
		   
		   try
		   {
			   saveasFile = file.getParentRemoteFileSubSystem().getRemoteFileObject(file, getFileName());
		   }
		   catch (Exception e)
		   {
		   }
	   }
	   return ok;
   }

   public boolean isPageComplete()
   {   	
   	String nameToCheck = null;
   	
   	if (fileName == null) {
   		nameToCheck = "";
   	}
   	else {
   		nameToCheck = fileName;
   	}
   	
   	SystemMessage errMsg = validator.validate(nameToCheck);
   	
   	if (errMsg != null)
   	{
   		setErrorMessage(errMsg);
   		return false;			
   	}
   	else
   	{
   		clearErrorMessage();
   	}
   	
   	return fileTypeCombo != null && 
	fileTypeCombo.getText().length() > 0 &&
	fileNameText != null && 
	fileNameText.getText().length() > 0 && 
	super.isPageComplete();
   }
	
   public String getFileName() {
   	
   		if (fileName.endsWith("." + fileType)) {
   			return fileName;
   		}
   		else {
	   		return fileName + "." + fileType;
   		}
   }
	
   /**
		* Returns the implementation of ISystemViewElement for the given
		* object.  Returns null if the adapter is not defined or the
		* object is not adaptable.
		*/
	   protected ISystemViewElementAdapter getAdapter(Object o) 
	   {
		   return SystemAdapterHelpers.getAdapter(o);
	   }
	
	  /**
		* Returns the implementation of ISystemRemoteElement for the given
		* object.  Returns null if this object does not adaptable to this.
		*/
	   protected ISystemRemoteElementAdapter getRemoteAdapter(Object o) 
	   {
		   return SystemAdapterHelpers.getRemoteAdapter(o);
	   }
		
   /**
	* User selected something in the tree.
	* This is an intercept of the parent's method so we can process a member selection, and
	*   copy the selected member's name to the entry field.
	*/
   public void selectionChanged(SelectionChangedEvent e)
   {
	   super.selectionChanged(e);
	   clearErrorMessage();
	   ISelection selection = e.getSelection();
	   Object selectedObject = getFirstSelection(e.getSelection());
	   if (selectedObject != null && selectedObject instanceof IRemoteFile)
	   {
		   IRemoteFile remoteFile = (IRemoteFile)selectedObject;
		
		   ISystemRemoteElementAdapter remoteAdapter = getRemoteAdapter(selectedObject);			
		   if ((remoteAdapter != null))
		   {
				Object parentFile = remoteFile;
				   		    		   				
				remoteAdapter = getRemoteAdapter(parentFile); 
				if (remoteAdapter != null)
				{
					String fullPath = remoteAdapter.getAbsoluteName(parentFile);
					setNameText(fullPath);		    	   	
					outputObjects = new Object[] {parentFile};
					setPageComplete();
					SystemMessage selectionMsg = null;
				  if (selectionValidator != null) 
					selectionMsg = selectionValidator.isValid(outputConnection, getSelections(selection), getRemoteAdapters(selection));

				  if (selectionMsg != null)
				  {
					valid = false;
					setErrorMessage(selectionMsg);
					setPageComplete();
				  }
			    }
			}
		}
	}

	public void setPreSelection(IRemoteFile selection)
	{
		if (selection.isDirectory())	
		{
			super.setPreSelection(selection);
		}
		else if (selection.isFile())
		{
			IRemoteFile parentFile = selection.getParentRemoteFile();
			
			if (parentFile.isDirectory())
			{
				super.setPreSelection(parentFile);
			}
		}
		if (prePop)
		{
			String file = selection.getName();
			int i = file.lastIndexOf(".");
			if (i == -1)
			{
				fileName = file;
				fileType = "";
			}
			else
			{
				fileName = file.substring(0,i);
				fileType = selection.getExtension();
			}
		}
		else
		{
			fileName = "";
		}		
	}

	/**
	 * Set the root folder from which to start listing folders or files.
	 * This version identifies the folder via a connection object and absolute path.
	 * There is another overload that identifies the folder via a single IRemoteFile object.
	 * 
	 * @param connection The connection to the remote system containing the root folder
	 * @param folderAbsolutePath The fully qualified folder to start listing from (eg: "\folder1\folder2")
	 * 
	 * @see org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString
	 */
	public void setRootFolder(IHost connection, String folderAbsolutePath)
	{
		setDefaultConnection(connection);
		setShowNewConnectionPrompt(true);
		setAutoExpandDepth(0);        
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		IRemoteFileSubSystem ss = RemoteFileUtility.getFileSubSystem(connection);
		IRemoteFileSubSystemConfiguration ssf = ss.getParentRemoteFileSubSystemConfiguration();
		RemoteFileFilterString rffs = new RemoteFileFilterString(ssf);
		rffs.setShowFiles(fileMode);  // no files if in folders mode
		rffs.setShowSubDirs(!fileMode || !filesOnlyMode); // yes folders, always, for now
		if (fileTypes != null)
		  rffs.setFile(fileTypes);
				
		// set the default filters we will show when the user expands a connection...
		String filterName = null;
		SystemFilterSimple filter = null;
		int filterCount = showRootFilter ? 2 : 1;
		if (preSelectRoot)
		  filterCount = 1;
		ISystemFilter[] filters = new ISystemFilter[filterCount];
		int idx = 0;
        
		// filter one: "Root files"/"Root folders" or "Drives"
		if (showRootFilter)
		{
		  if (ssf.isUnixStyle())
		  {
			if (!preSelectRoot)
			{
			  // "Root files" or "Folders"
			  filterName = fileMode ? SystemFileResources.RESID_FILTER_ROOTFILES : SystemFileResources.RESID_FILTER_ROOTFOLDERS;
			  //rffs.setPath(ssf.getSeparator()); // defect 43492. Show the root not the contents of the root
			}
			else
			{
			  filterName = SystemFileResources.RESID_FILTER_ROOTS; // "Roots"
			}
		  }
		  else
			filterName = fileMode ? SystemFileResources.RESID_FILTER_DRIVES : SystemFileResources.RESID_FILTER_DRIVES;
		  filter = new SystemFilterSimple(filterName);       
		  filter.setParent(ss);
		  filter.setFilterString(rffs.toString());
		  filters[idx++] = filter;
		  //System.out.println("FILTER 1: " + filter.getFilterString());
		  if (preSelectRoot)
		  {
			preSelectFilter = filter;
			preSelectFilterChild = folderAbsolutePath;
			//RSEUIPlugin.logInfo("in setRootFolder. Given: " + folderAbsolutePath);
		  }
		}
        
		if (!preSelectRoot)
		{
        
		  // filter two: "\folder1\folder2"
		  rffs.setPath(folderAbsolutePath); 

		  filter = new SystemFilterSimple(rffs.toStringNoSwitches());
		  filter.setParent(ss);
		  filter.setFilterString(rffs.toString());
		  filters[idx] = filter;
        
		  preSelectFilter = filter;
		  //RSEUIPlugin.logInfo("FILTER 2: " + filter.getFilterString());        
		}
		inputProvider.setFilterString(null); // undo what ctor did
		inputProvider.setQuickFilters(filters);
	}
	
	public void setPrePopSelection(boolean prePopSelection)
	{
		prePop = prePopSelection;
	}
	
	public void setNameAndTypePrompt(String prompt)
	{
		nameAndTypePrompt = prompt;
	}
}