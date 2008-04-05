/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 ********************************************************************************/

package org.eclipse.rse.files.ui.widgets;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.internal.files.ui.Activator;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.internal.files.ui.ISystemFileConstants;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.validators.ValidatorFileName;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SaveAsForm extends SystemSelectRemoteFileOrFolderForm {
	 protected Text fileNameText;
	 protected String fileName, initialFileName;
	 protected ValidatorFileName validator;		 
	 
	/**
	 * Constructor for SaveAsForm
	 */
	public SaveAsForm(ISystemMessageLine msgLine, Object caller, boolean fileMode) 
	{
		super(msgLine, caller, fileMode);
		
		validator = new ValidatorFileName();
	}
	
	/**
	 * In this method, we populate the given SWT container with widgets and return the container
	 *  to the caller. 
	 * @param parent The parent composite
	 */
	public Control createContents(Shell shell, Composite parent)	
	{
		Control control = super.createContents(shell, parent);
		
		Composite composite = SystemWidgetHelpers.createComposite(parent, 2);
//		SystemWidgetHelpers.createLabel(composite, SystemResources.RESID_NEWFILE_NAME_ROOT_LABEL);
//		fileNameText = SystemWidgetHelpers.createTextField(composite, null);

		fileNameText = SystemWidgetHelpers.createLabeledTextField(
			composite, null, FileResources.RESID_NEWFILE_NAME_LABEL, FileResources.RESID_NEWFILE_NAME_TOOLTIP);
			
			
		fileNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fileName = fileNameText.getText();
				setPageComplete();
			}
		});
					
		if (fileName != null)
		{
			fileNameText.setText(fileName);
		}
		
		return control;
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
		//System.out.println("Inside verify");
		boolean ok = super.verify();
		if (ok)
		{
			IRemoteFile file = (IRemoteFile) getSelectedObject();
			IRemoteFile saveasFile = null;
			try
			{
				saveasFile = file.getParentRemoteFileSubSystem().getRemoteFileObject(file, fileName, new NullProgressMonitor());
			}
			catch (Exception e)
			{
			}
			
		    //System.out.println("...saveasMbr null? "+ (saveasMbr==null));
			if (saveasFile != null && saveasFile.exists())
			{
				String msgTxt = NLS.bind(FileResources.MSG_UPLOAD_FILE_EXISTS, fileName);

				SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID, 
						ISystemFileConstants.MSG_UPLOAD_FILE_EXISTS,
						IStatus.WARNING, msgTxt);
				SystemMessageDialog dlg = new SystemMessageDialog(getShell(), msg);
				ok = dlg.openQuestionNoException();

			}
		}
		return ok;
	}

	public boolean isPageComplete()
	{
		SystemMessage errMsg = validator.validate(fileName);
		
		if (errMsg != null)
		{
			setErrorMessage(errMsg);
			return false;			
		}
		else
		{
			clearErrorMessage();
		}
		
		return fileNameText !=null && fileNameText.getText().length() > 0 && super.isPageComplete();
	}
	
	public String getFileName()
	{
		return fileName;
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
		
			fileName = selection.getName();//parentFile.getName();
			initialFileName=fileName;
		}
	}
	
	/**
		 * Returns the implementation of ISystemViewElement for the given
		 * object.  Returns null if the adapter is not defined or the
		 * object is not adaptable.
		 */
		protected ISystemViewElementAdapter getViewAdapter(Object o) 
		{
			return SystemAdapterHelpers.getViewAdapter(o);
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
			Object selectedObject = getFirstSelection(e.getSelection());
			if (selectedObject != null && selectedObject instanceof IRemoteFile)
			{
				IRemoteFile remoteFile = (IRemoteFile)selectedObject;
			
				ISystemRemoteElementAdapter remoteAdapter = getRemoteAdapter(selectedObject);			
				if ((remoteAdapter != null))
				{

					if (fileNameText != null)
					{	
						// simulate the parent file being selected...
						Object parentFile = getSelectedParent();
															    		
						if (remoteFile.isFile())
						{	
							fileName = remoteAdapter.getName(selectedObject);	    			    	
							fileNameText.setText(fileName);
						}
						else
						{
							fileName =initialFileName;
							if (fileName != null)
							{
								fileNameText.setText(fileName);
							}
							parentFile = remoteFile;
						}
		    							
						
						remoteAdapter = getRemoteAdapter(parentFile); 
						if (remoteAdapter != null)
						{
							String fullPath = remoteAdapter.getAbsoluteName(parentFile);
							setNameText(fullPath);		    	   	
							outputObjects = new Object[] {parentFile};
							setPageComplete();
						}
					}
				}
			}
		}

}