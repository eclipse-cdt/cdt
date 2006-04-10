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

package org.eclipse.rse.files.ui.propertypages;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IVirtualRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileIOException;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSecurityException;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.propertypages.SystemBasePropertyPage;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;


/**
 * The remote property page for remote file properties.
 * This is an output-only page.
 * The plugin.xml file registers this for remote file system objects.
 */
public class SystemFilePropertyPage extends SystemBasePropertyPage
       implements ISystemMessages, SelectionListener
{
	
	protected Label labelNamePrompt, labelTypePrompt, labelPathPrompt, labelSizePrompt, 
	                labelModifiedPrompt;
	//protected Button cbReadablePrompt, cbWritablePrompt;
	protected Button cbReadonlyPrompt, cbHiddenPrompt;
	protected Label labelName, labelType, labelPath, labelSize, labelModified, labelReadable, labelWritable, labelHidden;
	protected String errorMessage;	
    protected boolean initDone = false;
    protected boolean wasReadOnly = false;
       	
	/**
	 * Constructor for SystemFilterPropertyPage
	 */
	public SystemFilePropertyPage()
	{
		super();
		SystemPlugin sp = SystemPlugin.getDefault();
	}
	/**
	 * Create the page's GUI contents.
	 */
	protected Control createContentArea(Composite parent)
	{
		IRemoteFile file = getRemoteFile();

		// Inner composite
		int nbrColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);	

		// Name display
		labelNamePrompt = SystemWidgetHelpers.createLabel(
			composite_prompts, SystemFileResources.RESID_PP_FILE_NAME_LABEL, SystemFileResources.RESID_PP_FILE_NAME_TOOLTIP);
		labelName = SystemWidgetHelpers.createLabel(
			composite_prompts, "");
	    labelName.setToolTipText(SystemFileResources.RESID_PP_FILE_NAME_TOOLTIP);

		// Type display
		labelTypePrompt = SystemWidgetHelpers.createLabel(
			composite_prompts, SystemFileResources.RESID_PP_FILE_TYPE_LABEL, SystemFileResources.RESID_PP_FILE_TYPE_TOOLTIP);
		labelType = SystemWidgetHelpers.createLabel(
			composite_prompts, "");
	    labelType.setToolTipText(SystemFileResources.RESID_PP_FILE_TYPE_TOOLTIP);

		// Path display
		if (!file.isRoot())
		{
		  labelPathPrompt = SystemWidgetHelpers.createLabel(
			composite_prompts, SystemFileResources.RESID_PP_FILE_PATH_LABEL, SystemFileResources.RESID_PP_FILE_PATH_TOOLTIP);
		  labelPath = SystemWidgetHelpers.createLabel(
			composite_prompts, "");
	      labelPath.setToolTipText(SystemFileResources.RESID_PP_FILE_PATH_TOOLTIP);
		}

		// Size display
		if (!file.isDirectory())
		{
		  labelSizePrompt = SystemWidgetHelpers.createLabel(
			composite_prompts, SystemFileResources.RESID_PP_FILE_SIZE_LABEL, SystemFileResources.RESID_PP_FILE_SIZE_TOOLTIP);
		  labelSize = SystemWidgetHelpers.createLabel(
			composite_prompts, "");
	      labelSize.setToolTipText(SystemFileResources.RESID_PP_FILE_SIZE_TOOLTIP);
		}

		// Modified display
		if (!file.isRoot())
		{
		  labelModifiedPrompt = SystemWidgetHelpers.createLabel(
			composite_prompts, SystemFileResources.RESID_PP_FILE_MODIFIED_LABEL, SystemFileResources.RESID_PP_FILE_MODIFIED_TOOLTIP);
		  labelModified = SystemWidgetHelpers.createLabel(
			composite_prompts, "");
	      labelModified.setToolTipText(SystemFileResources.RESID_PP_FILE_MODIFIED_TOOLTIP);
		}

		// Readonly display
		if (!file.isRoot())
		{
		  if (file.showReadOnlyProperty())		   	  
		  {
		    cbReadonlyPrompt = SystemWidgetHelpers.createCheckBox(
			  composite_prompts, null, SystemFileResources.RESID_PP_FILE_READONLY_LABEL, SystemFileResources.RESID_PP_FILE_READONLY_TOOLTIP);
		  }
		}  

        /*
		// Readable display
		if (!file.isRoot())
		{
		  cbReadablePrompt = SystemWidgetHelpers.createCheckBox(
			composite_prompts, null, SystemFileResources.RESID_PP_FILE_READABLE_ROOT);
		}

		// Writable display
		if (!file.isRoot())
		{
		  cbWritablePrompt = SystemWidgetHelpers.createCheckBox(
			composite_prompts, null, SystemFileResources.RESID_PP_FILE_WRITABLE_ROOT);
		}
		*/

		// Hidden display
		if (!file.isRoot())
		{
		  cbHiddenPrompt = SystemWidgetHelpers.createCheckBox(
			composite_prompts, null, SystemFileResources.RESID_PP_FILE_HIDDEN_LABEL, SystemFileResources.RESID_PP_FILE_HIDDEN_TOOLTIP);
	      //((GridData)cbHiddenPrompt.getLayoutData()).horizontalSpan = nbrColumns;
		}
        
	    if (!initDone)	
	      doInitializeFields();		  
	      
	    if (!file.isRoot() && file.showReadOnlyProperty())	    
		  cbReadonlyPrompt.addSelectionListener(this);
        
		return composite_prompts;
	}
	/**
	 * Get the input remote file object
	 */
	protected IRemoteFile getRemoteFile()
	{
		Object element = getElement();
		return ((IRemoteFile)element);
	}

	/**
	 * Initialize values of input fields based on input
	 */
	protected void doInitializeFields()
	{
		initDone = true;
		IRemoteFile file = getRemoteFile();
		// name
		String name = file.getName();
		if (name.length() > 100) 
		{
			String shortName = name.substring(0, 97).concat("...");				
			labelName.setText(shortName);			
		}	
		else
		{
			labelName.setText(name);
		}
					
		// type
		if (file.isRoot())
		  labelType.setText(SystemFileResources.RESID_PP_FILE_TYPE_ROOT_VALUE);
		else if (file.isDirectory())
		  labelType.setText(SystemFileResources.RESID_PP_FILE_TYPE_FOLDER_VALUE);
		else
		  labelType.setText(SystemFileResources.RESID_PP_FILE_TYPE_FILE_VALUE);
	    // path
	    if (labelPath != null)
	    {
	      String path = file.getParentPath();
	      if (path != null)
	        labelPath.setText(file.getParentPath());
	    }
	    // size
	    if (labelSize != null)
	      labelSize.setText(Long.toString(file.getLength()));
	    // modified
	    if (labelModified != null)
	    {
	      Date date = file.getLastModifiedDate();
	      if (date != null)
	      {
	        SimpleDateFormat datefmt = (SimpleDateFormat)DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
	        labelModified.setText(datefmt.format(date));
	      }
	    }
	    // readonly
	    if (file.showReadOnlyProperty()) {
	      if (cbReadonlyPrompt != null)
	      {
	        cbReadonlyPrompt.setSelection(!file.canWrite());
	        wasReadOnly = !file.canWrite();
	        if (wasReadOnly || file instanceof IVirtualRemoteFile)
              cbReadonlyPrompt.setEnabled(false);
	      }
	    }  
        /*
	    // readable
	    if (cbReadablePrompt != null)
	    {
	      cbReadablePrompt.setSelection(file.canRead());
          cbReadablePrompt.setEnabled(false);
	    }
	    // writable
	    if (cbWritablePrompt != null)
	    {
 	      cbWritablePrompt.setSelection(file.canWrite());
          cbWritablePrompt.setEnabled(false); 
	    }
	    */
	    // hidden
	    if (cbHiddenPrompt != null)
	    {
	      cbHiddenPrompt.setSelection(file.isHidden());
          cbHiddenPrompt.setEnabled(false); 
	    }
	}
	
	/**
	 * Called by parent when user presses OK
	 */
	public boolean performOk()
	{
		boolean ok = super.performOk();
		if (ok && (cbReadonlyPrompt!=null) && cbReadonlyPrompt.getSelection() && !wasReadOnly)
		{
   		  try
    	  {
    	     getRemoteFile().getParentRemoteFileSubSystem().setReadOnly(getRemoteFile());
		     SystemPlugin.getTheSystemRegistry().fireEvent(
                   new org.eclipse.rse.model.SystemResourceChangeEvent(
                   getRemoteFile(),ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE,null)); 
    	     
    	  } catch (RemoteFileIOException exc) 
    	  {
             setMessage(SystemPlugin.getPluginMessage(ISystemMessages.FILEMSG_IO_ERROR));
    	  } catch (RemoteFileSecurityException exc)
    	  {
             setMessage(SystemPlugin.getPluginMessage(ISystemMessages.FILEMSG_SECURITY_ERROR));
    	  }
    	  //catch (RemoteFileException e)
    	  //{ 	
    	  //}
		}    
		return ok;
	}
    /**
     * Validate all the widgets on the page
	 * <p>
	 * Subclasses should override to do full error checking on all
	 *  the widgets on the page.
     */
    protected boolean verifyPageContents()
    {
    	return true;
    }

    public void widgetDefaultSelected(SelectionEvent event)
    {
    	
    }
    public void widgetSelected(SelectionEvent event)
    {
    	
    }

}