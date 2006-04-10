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

import java.util.Vector;

import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.filters.ISystemFilterStringEditPaneListener;
import org.eclipse.rse.ui.filters.SystemFilterStringEditPane;
import org.eclipse.rse.ui.wizards.AbstractSystemWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


/**
 * Main page of the abstract "New Filter" wizard.
 * This page's content is supplyable in the form of an "edit pane" which 
 *   essentially is reponsible for the content area of the wizard, and 
 *   which implements necessary minimal methods for this wizard to 
 *   interact with it.
 * <p>
 * As per the design goals of the filter wizard, this page effectively 
 *  only prompts to create a single new filter string. Thus, the 
 *  edit pane needed is in fact the "new filter string" edit pane. 
 */

public class SystemNewFilterWizardMainPage 
       extends AbstractSystemWizardPage
       implements ISystemMessages, ISystemFilterStringEditPaneListener 
	               //,SystemFilterNewFilterWizardMainPageInterface, ISystemMessageLine
{
	protected SystemFilterStringEditPane editPane;	
	protected String                     type;
	protected String[]                   defaultFilterStrings;
	protected boolean                   firstVisit = true;
	private   Control			          clientArea;
	/**
	 * Constructor. 
	 * Uses the wizard page title as set in the overall wizard. 
	 * Uses a default wizard page description. Change later via setDescription if desired.
	 * @param wizard - the parent new filter wizard
	 * @param data - configurable mri data
	 */
	public SystemNewFilterWizardMainPage(SystemNewFilterWizard wizard, ISystemNewFilterWizardConfigurator data)

	{
		super(wizard,"NewFilterPage1", data.getPage1Title(), data.getPage1Description()); 	
	  	editPane = getEditPane(wizard.getShell());	
		//setHelp(data.getPage1HelpID()); not used as it comes from wizard help
	}
	/**
	 * Constructor when unique edit pane supplied
	 * Uses the wizard page title as set in the overall wizard. 
	 * Uses a default wizard page description. Change later via setDescription if desired.
	 * @param wizard - the parent new filter wizard
	 * @param editPane - the edit pane that prompts the user for a single filter string
	 * @param data - configurable mri data
	 */
	public SystemNewFilterWizardMainPage(SystemNewFilterWizard wizard, SystemFilterStringEditPane editPane, ISystemNewFilterWizardConfigurator data)

	{
		super(wizard,"NewFilterPage1", data.getPage1Title(), data.getPage1Description()); 	
	  	this.editPane = editPane;
		editPane.addChangeListener(this);
		//setHelp(data.getPage1HelpID()); not used as it comes from wizard help
	}
    /**
     * Set the contextual system filter pool reference manager provider. Ie, in the RSE this
     *  is the currently selected subsystem if this wizard was launched from a subsystem. 
     * <p>
     * Will be non-null if the current selection is a reference to a filter pool or filter, 
     * or a reference manager provider.
     * <p>
     * This is not used by default but made available for subclasses.
     */
    public void setSystemFilterPoolReferenceManagerProvider(ISystemFilterPoolReferenceManagerProvider provider)
    {
    	editPane.setSystemFilterPoolReferenceManagerProvider(provider);
    }
    /**
     * Overrride this if you want to supply your own edit pane for the filter string. 
     */
    protected SystemFilterStringEditPane getEditPane(Shell shell)
    {
    	if (editPane == null)
    	  editPane = new SystemFilterStringEditPane(shell);
    	return editPane;
    }

	/**
	 * CreateContents is the one method that must be overridden from the parent class.
	 * In this method, we populate an SWT container with widgets and return the container
	 *  to the caller (JFace). This is used as the contents of this page.
	 */
	public Control createContents(Composite parent)
	{
		clientArea =  editPane.createContents(parent); 
		editPane.addChangeListener(this);
		return clientArea;
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
		SystemMessage errorMessage = editPane.verify();
		if (errorMessage != null)
		  setErrorMessage(errorMessage);
		return (errorMessage == null);		
	}

	/**
	 * Return the Control to be given initial focus.
	 * Override from parent. Return control to be given initial focus.
	 */
	protected Control getInitialFocusControl()
	{
        return editPane.getInitialFocusControl();
	}
	// ------------------------------------
	// METHODS FOR EXTRACTING USER DATA ... 
	// ------------------------------------
	/**
	 * Return the user-specified filter strings
	 */
	public Vector getFilterStrings()
	{
		Vector v = new Vector();
		
		String userAddedString = editPane.getFilterString();
		if ((userAddedString !=null) && (userAddedString.length()>0))
		{
			if (!v.contains(userAddedString))
		  		v.add(userAddedString);
		}
		else if (defaultFilterStrings != null)
		{
			for (int idx=0; idx<defaultFilterStrings.length; idx++)
			{
				v.add(defaultFilterStrings[idx]);
			}
		}
		return v;
		//return editPane.getFilterStrings();
	}
    /**
     * Get the type of filter as set by {@link #setType(String)}
     */
    public String getType()
    {
    	return type;
    }
	// -----------------------------------------
	// METHODS FOR SETTING/CONFIGURING INPUT ... 
	// -----------------------------------------
    /**
     * Set the type of filter we are creating. Results in a call to setType on the new filter.
     * Types are not used by the base filter framework but are a way for tools to create typed
     * filters and have unique actions per filter type.
     */
    public void setType(String type)
    {
    	this.type = type;
    	editPane.setType(type);
    }
	
	/**
	 * Supply the default set of filter strings this filter is to have.
	 */
	public void setDefaultFilterStrings(String[] defaultFilterStrings)
	{
		this.defaultFilterStrings = defaultFilterStrings;
		if (editPane != null)
		{
			for (int i = 0; i < defaultFilterStrings.length; i++)
			{
				editPane.setFilterString(defaultFilterStrings[i], i);
			}
		}
	}
	
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

	// -------------------------------	
	// INTERCEPT OF WIZARDPAGE METHODS
	// -------------------------------
	/**
	 * This is called when a page is given focus or loses focus
	 */
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		if (visible)
		{
			// ok, we don't want to issue a msg in the user's face the first time they see this page, but
			//   after that when we get focus we verify the contents.
			// It is the job of that verify method in the edit pane to fire an event back to us as the result
			//   of that verify. This results in a call to our filterStringChanged method above.
			if (!firstVisit)
		      editPane.verify();
		    else
		      setPageComplete(editPane.isComplete());
		    firstVisit = false;
		    //System.out.println("Edit pane size = " + clientArea.getSize());
		}
	}	
}