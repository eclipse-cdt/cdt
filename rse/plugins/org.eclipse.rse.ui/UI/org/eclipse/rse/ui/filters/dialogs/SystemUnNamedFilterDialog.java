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

package org.eclipse.rse.ui.filters.dialogs;

import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.filters.ISystemFilterStringEditPaneListener;
import org.eclipse.rse.ui.filters.SystemFilterStringEditPane;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;



/**
 * A dialog that prompts the user for a single filter string, but not for the
 *  purpose of creating a named filter. The output is that single filter string,
 *  and the caller can do what they want with it.
 */

public class SystemUnNamedFilterDialog extends SystemPromptDialog
                implements ISystemMessages, 
                            ISystemFilterStringEditPaneListener
{

    protected SystemFilterStringEditPane  editpane;
    protected String outputFilterString;
	// inputs
    protected ISystemFilterPoolReferenceManagerProvider provider;    
	/**
	 * Constructor
	 */
	public SystemUnNamedFilterDialog(Shell shell) 
	{
		this(shell, SystemResources.RESID_CRTFILTER_TITLE);
	}
	/**
	 * Constructor, when unique title desired
	 */
	public SystemUnNamedFilterDialog(Shell shell, String title)
	{
		super(shell, title);
		//this.parentPool = parentPool;
		//setMinimumSize(450, 350); // x, y
		//pack();
		setOutputObject(null);
        setHelp();		        
	}		

	/**
	 * Overridable extension point for setting dialog help. By default, there is no help
	 */
	protected void setHelp()
	{
	}
	// -------------------
	// INPUT/CONFIGURATION
	// -------------------
    /**
     * Set the contextual system filter pool reference manager provider. Eg, in the RSE, this
     *  will be the selected subsystem if the New Filter action is launched from there, or if
     *  launched from a filter pool reference under there.
     * <p>
     * Will be non-null if the current selection is a reference to a filter pool or filter, 
     *  or a reference manager provider. 
     * <p>
     * This is passed into the filter and filter string wizards and dialogs in case it is needed
     *  for context. 
     */
    public void setSystemFilterPoolReferenceManagerProvider(ISystemFilterPoolReferenceManagerProvider provider)
    {
    	this.provider = provider;
    	//SystemPlugin.logDebugMessage(this.getClass().getName(),"Inside setSystemFilterPoolReferenceManagerProvider. null? " + (provider==null));
    }
	/**
	 * Specify an edit pane that prompts the user for the contents of a filter string.
	 */
	public void setFilterStringEditPane(SystemFilterStringEditPane editPane)
	{
		this.editpane = editPane;
	}

	// -------------------
	// OUTPUT
	// -------------------
	/**
	 * Return the string the user configured in this dialog.
	 * Will return null if the user cancelled the dialog, so test with wasCancelled().
	 */
	public String getFilterString()
	{
		return outputFilterString;
	}


	// LIFECYCLE
			
	/**
	 * @see SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl()
	{
		return editpane.getInitialFocusControl();
	}
	/**
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent)
	{
		editpane = getFilterStringEditPane(getShell());
	    editpane.setSystemFilterPoolReferenceManagerProvider(provider);

		// Edit pane is our whole content area
		Control composite = editpane.createContents(parent);
 	    
	 	// add listeners
		editpane.addChangeListener(this);
				
		return composite;
	}
	/**
	 * Return our edit pane. Overriding this is an alternative to calling setEditPane.
	 * This is called in createContents
	 */
	protected SystemFilterStringEditPane getFilterStringEditPane(Shell shell)
	{
	    if (editpane == null)
	      editpane = new SystemFilterStringEditPane(shell);
	    return editpane;		
	}

	/**
	 * Parent override.
	 * Called when user presses OK button. 
	 * This is when we save all the changes the user made.
	 */
	protected boolean processOK() 
	{
		SystemMessage errorMessage = editpane.verify(); // should fire events back to us if there is an error
		if (errorMessage != null)
		  return false;
		outputFilterString = editpane.getFilterString();
		return super.processOK();		
	}	

	/**
	 * Parent override.
	 * Called when user presses CLOSE button. We simply blow away all their changes!
	 */
	protected boolean processCancel() 
	{
		return super.processCancel();		
	}	
	
	/**
	 * Override of parent method so we can direct it to the Apply button versus the OK button
	 */
	public void setPageComplete(boolean complete)
	{
	}
			
    // ---------------
    // HELPER METHODS
    // ---------------  

	
	
	// ----------------------------------------------
	// EDIT PANE CHANGE LISTENER INTERFACE METHODS...
	// ----------------------------------------------
    /**
     * Callback method. The user has changed the filter string. It may or may not
     *  be valid. If not, the given message is non-null. If it is, and you want it,
     *  call getSystemFilterString() in the edit pane.
     */
    public void filterStringChanged(SystemMessage message)
    {
    	if (message != null)
    	  setErrorMessage(message);
    	else
    	  clearErrorMessage();
    	setPageComplete(message == null);
    }    
    /**
     * Callback method. We are about to do a verify,the side effect of which is to
     *  change the current state of the dialog, which we don't want. This tells the
     *  dialog to back up that state so it can be restored.
     */
    public void backupChangedState()
    {
    }    
    /**
     * Callback method. After backup and change events this is called to restore state
     */
    public void restoreChangedState()
    {
    }    
}