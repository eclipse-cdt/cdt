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

package org.eclipse.rse.files.ui.dialogs;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.rse.files.ui.ISystemAddFileListener;
import org.eclipse.rse.files.ui.widgets.SystemSelectRemoteFileOrFolderForm;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.IValidatorRemoteSelection;
import org.eclipse.rse.ui.view.SystemViewForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for allowing users to select a remote file or folder. This is a thin dialog wrapper on top of 
 * the SystemSelectRemoteFileOrFolderForm widget, which you could optionally imbed directly into your own
 * dialog or wizard page.
 * <p>
 * Call these methods to configure the functionality of the dialog
 * <ul>
 *   <li>{@link #setSystemConnection(IHost) or #setDefaultConnection(SystemConnection)}
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
 * @see org.eclipse.rse.files.ui.actions.SystemSelectRemoteFileAction
 * @see org.eclipse.rse.files.ui.actions.SystemSelectRemoteFolderAction
 */
public class SystemSelectRemoteFileOrFolderDialog 
       extends SystemPromptDialog 
       //implements  ISystemFileConstants
{  
	public static final boolean FILE_MODE = true;
	public static final boolean FOLDER_MODE = false;
	protected SystemSelectRemoteFileOrFolderForm form;
	private boolean                            multipleSelectionMode;
	protected IHost                   outputConnection;
	private ISystemAddFileListener             addButtonCallback = null;
	
	/**
	 * Constructor
	 * 
	 * @param shell The shell to hang the dialog off of
	 * @param fileMode True if selecting files, false if selecting folders
	 * 
	 */
	public SystemSelectRemoteFileOrFolderDialog(Shell shell, boolean fileMode)
	{
		this(shell, 
		     fileMode ? SystemFileResources.RESID_SELECTFILE_TITLE : SystemFileResources.RESID_SELECTDIRECTORY_TITLE,
		     fileMode);
	}	
	
	/**
	 * Constructor when you want to supply your own title.
	 * 
	 * @param shell The shell to hang the dialog off of
	 * @param title The title to give the dialog
	 * @param fileMode True if selecting files, false if selecting folders
	 */
	public SystemSelectRemoteFileOrFolderDialog(Shell shell, String title, boolean fileMode)
	{
		super(shell, title);
		super.setBlockOnOpen(true); // always modal	
		form = getForm(fileMode);
		//pack();
	}	

    // ------------------
    // PUBLIC METHODS...
    // ------------------
	/**
     * indicate whether selections from different parents are allowed
     */
    public void setAllowForMultipleParents(boolean flag)
    {
    	form.setAllowForMultipleParents(flag);
    }
    
    /**
     * Set the system connection to restrict the user to selecting files or folders from
     */
    public void setSystemConnection(IHost conn)
    {
    	form.setSystemConnection(conn);
    }
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
     * Set to true if a "New Connection..." special connection is to be shown for creating new connections
     */
    public void setShowNewConnectionPrompt(boolean show)
    {
    	form.setShowNewConnectionPrompt(show);
    }
    /**
     * Set the message shown at the top of the form
     */
    public void setMessage(String message)
    {
    	form.setMessage(message);
    }
    /**
     * Set the tooltip text for the remote systems tree from which an item is selected.
     */
    public void setSelectionTreeToolTipText(String tip)
    {
    	form.setSelectionTreeToolTipText(tip);
    }
    /**
     * Specify the zero-based auto-expand level for the tree. The default is zero, meaning
     *   only show the connections.
     */
    public void setAutoExpandDepth(int depth)
    {
    	form.setAutoExpandDepth(depth);
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
        form.setRootFolder(connection, folderAbsolutePath);
	}
	/**
     * Set the root folder from which to start listing folders.
     * This version identifies the folder via an IRemoteFile object.
     * There is another overload that identifies the folder via a connection and folder path.
     * <p>
     * This call effectively transforms the select dialog by:
     * <ul>
     *  <li>Preventing the user from selecting other connections
     *  <li>Preventing the user from selecting other filter strings
     * </ul>
     * 
     * @param rootFolder The IRemoteFile object representing the remote folder to start the list from
     * 
     * @see org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString
	 */
	public void setRootFolder(IRemoteFile rootFolder)
	{
		form.setRootFolder(rootFolder);
	}
	/**
	 * Set a file or folder to preselect. This will:
	 * <ul>
	 *   <li>Set the parent folder as the root folder 
	 *   <li>Pre-expand the parent folder
	 *   <li>Pre-select the given file or folder after expansion
	 * </ul>
	 * If there is no parent, then we were given a root. In which case we will
	 * <ul>
	 *  <li>Force setRestrictFolders to false
	 *  <li>Pre-expand the root drives (Windows) or root files (Unix)
	 *  <li>Pre-select the given root drive (Windows only)
	 * </ul>
	 */
	public void setPreSelection(IRemoteFile selection)
	{
		form.setPreSelection(selection);
	}

	/**
	 * For files mode, restrict the files list by an array of file types
	 * <p>
	 * This must be called BEFORE setRootFolder!
	 */
	public void setFileTypes(String[] fileTypes)
	{
		form.setFileTypes(fileTypes);
	}
	/**
	 * For files mode, restrict the files list by a comman-delimited array of file types.
	 * The last type must also end in a comma. Eg "java, class," or "class,".
	 * <p>
	 * This must be called BEFORE setRootFolder!
	 */
	public void setFileTypes(String fileTypes)
	{
		form.setFileTypes(fileTypes);
	}
    /**
     * Specify whether setRootFolder should prevent the user from being able to see or select 
     *  any other folder. This causes two effects:
     * <ol>
     *   <li>The special filter for root/drives is not shown
     *   <li>No subfolders are listed in the target folder, if we are listing files. Of course, they are shown
     *          if we are listing folders, else it would be an empty list!
     * </ol>
     */
    public void setRestrictFolders(boolean restrict)
    {
    	form.setRestrictFolders(restrict);
    }
    /**
     * Enable Add mode. This means the OK button is replaced with an Add button, and
     * the Cancel with a Close button. When Add is pressed, the caller is called back.
     * The dialog is not exited until Close is pressed.
     * <p>
     * When a library is selected, the caller is called back to decide to enable the Add
     * button or not.
     */
    public void enableAddMode(ISystemAddFileListener caller)
    {
    	this.addButtonCallback = caller;
    	form.enableAddMode(caller);
    	setShowAddButton(true);
    	enableAddButton(false);
        setShowOkButton(false);
        setCancelButtonLabel(SystemResources.BUTTON_CLOSE);    	
    }
    /**
     * Overloaded method that allows setting the label and tooltip text of the Add button.
     * If you pass null for the label, the default is used ("Add").
     */
    public void enableAddMode(ISystemAddFileListener caller, String addLabel, String addToolTipText)
    {
    	enableAddMode(caller);
    	if (addLabel != null)
    	  setAddButtonLabel(addLabel);
    	if (addToolTipText != null)
    	  setAddButtonToolTipText(addToolTipText);
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
    	this.multipleSelectionMode = multiple;
    	form.setMultipleSelectionMode(multiple);
    }
    /**
     * Specify a validator to use when the user selects a remote file or folder.
     * This allows you to decide if OK should be enabled or not for that remote file or folder.
     */
    public void setSelectionValidator(IValidatorRemoteSelection selectionValidator)
    {
    	form.setSelectionValidator(selectionValidator);
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
    	return outputConnection;
    }

    /**
     * Return the multiple selection mode as set by setMultipleSelectionMode(boolean)
     */
    public boolean getMultipleSelectionMode()
    {
    	return multipleSelectionMode;
    }
    /**
     * Return the embedded System Tree object.
     * Will be null until createContents is called.
     */
    public SystemViewForm getSystemViewForm()
    {
    	return form.getSystemViewForm();
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
		return form.createContents(getShell(), parent);
	}

    /**
     * Private method. 
     * Get the contents.
     */
    protected SystemSelectRemoteFileOrFolderForm getForm(boolean fileMode)
    {
		//System.out.println("INSIDE GETFORM");    	
    	//if (form == null)
    	//{
    	  form = new SystemSelectRemoteFileOrFolderForm(getMessageLine(), this, fileMode);
    	  // reset output variables just to be safe
    	  setOutputObject(null);
		  outputConnection = null;
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
			outputConnection = form.getSelectedConnection();
			if (multipleSelectionMode)
			  setOutputObject(form.getSelectedObjects());
			else
			  setOutputObject(form.getSelectedObject());
		}
		else
		  setOutputObject(null);
		return closeDialog;
	}	

	/**
     * Private method. 
     * <p>
	 * Called when user presses Add button. 
	 */
	protected boolean processAdd() 
	{
        Object errMsg = addButtonCallback.addButtonPressed(form.getSelectedConnection(), (IRemoteFile[])form.getSelectedObjects());
        if (errMsg != null)
        {
          if (errMsg instanceof String)
            setErrorMessage((String)errMsg);
          else
            setErrorMessage((SystemMessage)errMsg);
        }
        else
          clearErrorMessage();
        enableAddButton(false); // presumably you won't add the same thing twice!
        return false;
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
	
	
    public void setPageComplete(boolean complete)
    {
    	if (addButtonCallback != null)
    	  enableAddButton(complete);
    	else
    	  super.setPageComplete(complete);
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
	 * Add viewer filter.
	 * @param filter a viewer filter.
	 */
	public void addViewerFilter(ViewerFilter filter) {
		
		if (form != null) {
			form.addViewerFilter(filter);
		}
	}
	
	/**
	 * Sets whether to allow folder selection. The default selection validator will use this to
	 * determine whether the OK button will be enabled when a folder is selected. The default
	 * is <code>true</code>. This call only makes sense if the dialog is in file selection mode.
	 * @param allow <code>true</code> to allow folder selection, <code>false</code> otherwise.
	 */
	public void setAllowFolderSelection(boolean allow) {
	    
	    if (form != null) {
	        form.setAllowFolderSelection(allow);
	    }
	}
}