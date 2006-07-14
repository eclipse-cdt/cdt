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

package org.eclipse.rse.ui.dialogs;


import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.ISystemPageCompleteListener;
import org.eclipse.rse.ui.SystemBaseForm;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.IValidatorRemoteSelection;
import org.eclipse.rse.ui.widgets.SystemSelectConnectionForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


/**
 * Dialog for allowing users to select an existing connection, or optionally create a new one.
 * There are a number of methods to configure the dialog so only connections of a particular system type,
 *  or containing subsystems from a particular subsystem factory or class of subsystem factories, are shown.
 * <p>
 * Call these methods to configure the functionality of the dialog
 * <ul>
 *   <li>{@link #setHost(IHost) or #setDefaultConnection(SystemConnection)}
 *   <li>{@link #setShowNewConnectionPrompt(boolean)}
 *   <li>{@link #setSystemTypes(String[])}
 *   <li>{@link #setAutoExpandDepth(int)}
 *   <li>{@link #setRootFolder(IHost, String)} or {@link #setRootFolder(IRemoteFile)} or {@link #setPreSelection(IRemoteFile)}
 *   <li>{@link #setFileTypes(String[])} or {@link #setFileTypes(String)} 
 *   <li>{@link #setShowPropertySheet(boolean)}
 *   <li>{@link #enableAddMode(ISystemAddFileListener)}
 *   <li>{@link #setMultipleSelectionMode(boolean)}
 *   <li>{@link #setSelectionValidator(IValidatorRemoteSelection)}
 * </ul>
 * <p>
 * Call these methods to configure the text on the dialog
 * <ul>
 *   <li>{@link #setMessage(String)}
 *   <li>{@link #setSelectionTreeToolTipText(String)}
 * </ul>
 * <p>
 * After running, call these methods to get the output:
 * <ul>
 *   <li>{@link #getSelectedObject()} or {@link #getSelectedObjects()}
 *   <li>{@link #getSelectedConnection()}
 * </ul>
 * 
  */
public class SystemSelectConnectionDialog 
       extends SystemPromptDialog implements ISystemPageCompleteListener
{  
	public static final boolean FILE_MODE = true;
	public static final boolean FOLDER_MODE = false;
	private SystemSelectConnectionForm 		form;


	/**
	 * Constructor
	 * 
	 * @param shell The shell to hang the dialog off of
	 * 
	 */
	public SystemSelectConnectionDialog(Shell shell)
	{
		this(shell, SystemResources.RESID_SELECTCONNECTION_TITLE);
	}	
	/**
	 * Constructor when you want to supply your own title.
	 * 
	 * @param shell The shell to hang the dialog off of
	 * @param title The title to give the dialog
	 */
	public SystemSelectConnectionDialog(Shell shell, String title)
	{
		super(shell, title);
		super.setBlockOnOpen(true); // always modal						
		form = getForm(shell);
		setShowPropertySheet(true, false); // default
	}	

    // ------------------
    // PUBLIC METHODS...
    // ------------------
    /**
     * Set the connection to default the selection to
     */
    public void setDefaultConnection(IHost conn)
    {
        form.setDefaultConnection(conn);
    }
    /**
     * Restrict to certain system types
     * @param systemTypes the system types to restrict what connections are shown and what types of connections
     *  the user can create
     * @see org.eclipse.rse.core.IRSESystemType
     */
    public void setSystemTypes(String[] systemTypes)
    {
    	form.setSystemTypes(systemTypes);
    }
	/**
	 * Restrict to a certain system type
	 * @param systemType the system type to restrict what connections are shown and what types of connections
	 *  the user can create
	 * @see org.eclipse.rse.core.IRSESystemType
	 */
	public void setSystemType(String systemType)
	{
		form.setSystemType(systemType);
	}
    /**
     * Set to true if a "New Connection..." special connection is to be shown for creating new connections
     */
    public void setShowNewConnectionPrompt(boolean show)
    {
    	form.setShowNewConnectionPrompt(show);
    }
    /**
     * Set the instruction label shown at the top of the dialog
     */
    public void setInstructionLabel(String message)
    {
    	form.setMessage(message);
    }

    /**
     * Show the property sheet on the right hand side, to show the properties of the
     * selected object.
     * <p>
     * This overload always shows the property sheet
     * <p>
     * Default is false
     */
    public void setShowPropertySheet(boolean show)
    {
    	form.setShowPropertySheet(show);
    }
    /**
     * Show the property sheet on the right hand side, to show the properties of the
     * selected object.
     * <p>
     * This overload shows a Details>>> button so the user can decide if they want to see the
     * property sheet. 
     * <p>
     * Default is true, false
     * <p>
     * @param show True if show the property sheet within the dialog
     * @param initialState True if the property is to be initially displayed, false if it is not
     *  to be displayed until the user presses the Details button.
     */
    public void setShowPropertySheet(boolean show, boolean initialState)
    {
    	if (show)
    	{
    	  form.setShowPropertySheet(initialState);
    	  setShowDetailsButton(true, !initialState);
    	}
    }

    /**
     * Set multiple selection mode. Default is single selection mode
     * <p>
     * If you turn on multiple selection mode, you must use the getSelectedObjects()
     *  method to retrieve the list of selected objects.
     * <p>
     * Further, if you turn this on, it has the side effect of allowing the user
     *  to select any remote object. The assumption being if you are prompting for
     *  files, you also want to allow the user to select a folder, with the meaning
     *  being that all files within the folder are implicitly selected. 
     *
     * @see #getSelectedObjects()
     */
    public void setMultipleSelectionMode(boolean multiple)
    {
    	form.setMultipleSelectionMode(multiple);
    }

    // ------------------
    // OUTPUT METHODS...
    // ------------------

    /**
     * Return selected file or folder
     */	
    public Object getSelectedObject()
    {
    	if (getOutputObject() instanceof Object[])
    	  return ((Object[])getOutputObject())[0];
    	else
    	  return getOutputObject();
    }
    /**
     * Return all selected objects. This method will return an array of one
     *  unless you have called setMultipleSelectionMode(true)!
     * @see #setMultipleSelectionMode(boolean)
     */	
    public Object[] getSelectedObjects()
    {
    	if (getOutputObject() instanceof Object[])
    	  return (Object[])getOutputObject();
    	else if (getOutputObject() instanceof Object)
    	  return new Object[] {getOutputObject()};
    	else
    	  return null;
    }

    /**
     * Return selected connection
     */	
    public IHost getSelectedConnection()
    {
    	return form.getSelectedConnection();
    }
	/**
	 * Return selected connections in multiple selection mode
	 */	
	public IHost[] getSelectedConnections()
	{
		return form.getSelectedConnections();
	}
	
    /**
     * Return the multiple selection mode as set by setMultipleSelectionMode(boolean)
     */
    public boolean getMultipleSelectionMode()
    {
    	return form.getMultipleSelectionMode();
    }

    // ------------------
    // PRIVATE METHODS...
    // ------------------
	/**
     * Private method. 
	 * @see SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() 
	{
		return form.getInitialFocusControl();
	}

	/**
     * Private method. 
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent) 
	{
		return form.createContents(parent);
	}

    /**
     * Private method. 
     * Get the contents.
     */
    protected SystemSelectConnectionForm getForm(Shell shell)
    {
		//System.out.println("INSIDE GETFORM");    	
    	//if (form == null)
    	//{
    	  form = new SystemSelectConnectionForm(shell,getMessageLine());
    	  form.addPageCompleteListener(this);
    	  // reset output variables just to be safe
    	  setOutputObject(null);
    	//}
    	return form;
    }

	/**
	 * Create message line. Intercept so we can set msg line of form.
	 */
	protected ISystemMessageLine createMessageLine(Composite c)
	{
		ISystemMessageLine msgLine = super.createMessageLine(c);
		if (form != null)
		  form.setMessageLine(msgLine);
		return msgLine;
	}


	/**
     * Private method. 
     * <p>
	 * Called when user presses OK button. 
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	protected boolean processOK() 
	{
		boolean closeDialog = form.verify();
		if (closeDialog)
		{
			if (getMultipleSelectionMode())
			  setOutputObject(form.getSelectedConnections());
			else
			  setOutputObject(form.getSelectedConnection());
		}
		else
		  setOutputObject(null);
		return closeDialog;
	}	

	/**
	 * Private method.
	 * <p>
	 * Called when user presses DETAILS button. 
	 * <p>
	 * Note the text is automatically toggled for us! We need only
	 * do whatever the functionality is that we desire
	 * 
	 * @param hideMode the current state of the details toggle, prior to this request. If we return true from
	 *   this method, this state and the button text will be toggled.
	 * 
	 * @return true if the details state toggle was successful, false if it failed.
	 */
	protected boolean processDetails(boolean hideMode) 
	{
		form.toggleShowPropertySheet(getShell(), getContents());
		return true;
	}		
	
	
    /**
     * We have to override close to ensure that we reset the form to null
     */
    public boolean close() 
    {
    	if (super.close())
    	{
			if (form != null)
			{
				form.dispose();		
			}
    	  form = null;
    	  return true;
    	}    
    	return false;
    }

	/**
	 * The callback method. 
	 * This is called whenever setPageComplete is called by the form code.
	 * @see {@link SystemBaseForm#addPageCompleteListener(ISystemPageCompleteListener)} 
	 */
	public void setPageComplete(boolean complete)
	{
		super.setPageComplete(complete);    
	}
}