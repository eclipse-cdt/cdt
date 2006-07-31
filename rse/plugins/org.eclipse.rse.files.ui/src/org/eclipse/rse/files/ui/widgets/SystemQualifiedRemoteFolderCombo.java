/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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
import java.util.Hashtable;
import java.util.ResourceBundle;

import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.files.ui.actions.SystemSelectRemoteFolderAction;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.widgets.ISystemCombo;
import org.eclipse.rse.ui.widgets.SystemHistoryCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


/**
 * This re-usable widget is for selecting a new or previously specified
 *  folder path within a given connection.
 * <p>
 * The dropdown is historical and contains a folder path qualified by a connection name.
 * It is readonly ... the user must use the browse button.
 * <p>
 * The composite is layed as follows:
 * <code>
 *   Folder: conn\dir1_____v  Browse...
 * </code>
 * <p>
 * The browse button lists only connections that are of the specified subsystem factory.
 * It also by default has a New Connection... prompting object for creating new connections, but this can be
 * turned off.
 * <p>
 * To get the current folder object selected, use getFolder().
 * <p>
 * To listen for changes, use addSelectionListener().
 */
public class SystemQualifiedRemoteFolderCombo extends Composite 
       implements ISystemMessages, ISystemCombo
{
    public static final char CONNECTION_DELIMITER = IRemoteFile.CONNECTION_DELIMITER;
	private Label              folderLabel = null;
	private SystemHistoryCombo folderCombo = null;
	private Button             browseButton = null;	
	//private RemoteFileSubSystem subsystem = null;
	//private RemoteFileSubSystemConfiguration subsystemFactory = null;
	//private String             subsystemFactoryID = null;
	//private IRemoteFile[]      folders = null;
	private Hashtable          resolvedFolders = new Hashtable();
	//private String[]           folderStrings = null;
	private String[]           systemTypes;
	private boolean            readOnly = true;
	private boolean            showNewPrompt = true;
    private SystemSelectRemoteFolderAction browseAction = null;
	//private static final int DEFAULT_COMBO_WIDTH = 300;
	//private static final int DEFAULT_BUTTON_WIDTH = 80;
	
	/**
	 * Constructor. 
	 * Requires a history key used to store/restore the dropdown history for this. Pass null to use 
	 * the overall default (ISystemPreferencesConstants.HISTORY_FOLDER).
	 * By default, this allows users to select with any connection that has subsystems that implement
	 *  RemoteFileSubSystem. To restrict it to connections of a particular system type, say, call
	 *  setSystemType.
	 * @param parent Parent composite
	 * @param style SWT style flags for overall composite widget
	 * @param historyKey A string identifying the key into the user preferences where this combo's history will be stored.
	 * @see #setSystemType(String)
	 */
	public SystemQualifiedRemoteFolderCombo(Composite parent, int style, String historyKey)
	{
		super(parent, style);		
		prepareComposite(3);
	    folderLabel = SystemWidgetHelpers.createLabel(this,SystemFileResources.WIDGET_FOLDER_LABEL, SystemFileResources.WIDGET_FOLDER_TOOLTIP);
        if (historyKey == null)
          historyKey = ISystemPreferencesConstants.HISTORY_QUALIFIED_FOLDER;
	    folderCombo = SystemWidgetHelpers.createHistoryCombo(this,null,historyKey,readOnly,SystemFileResources.WIDGET_FOLDER_TOOLTIP);
	    Object folderData = folderCombo.getLayoutData();
	    if (folderData instanceof GridData)
	      ((GridData)folderData).widthHint = 200; 
	    browseButton = createPushButton(this,SystemFileResources.WIDGET_BROWSE_LABEL);
	    addOurButtonSelectionListener();
	    //scrubHistory(); this is too intensive as it starts all kinds of servers and stuff unnecessarily.
	}

	/**
	 * Set auto-uppercase. When enabled, all non-quoted values are uppercases when appropriate.
	 * This has no effect in readonly mode, which this combo is, so in fact this is ineffective!
	 * We include it in case we allow editing in the future, and because it is in the ISystemCombo
	 * interface we implement.
	 */
	public void setAutoUpperCase(boolean enable)
	{
		folderCombo.setAutoUpperCase(enable);
	}

    /**
     * Set the system types to restrict what connections the user sees, and what types of 
     * connections they can create.
     * @param systemTypes An array of system type names
     * 
     * @see org.eclipse.rse.core.IRSESystemType
     */
    public void setSystemTypes(String[] systemTypes)
    {
    	this.systemTypes = systemTypes;
    }
    /**
     * Convenience method to restrict to a single system type. 
     * Same as setSystemTypes(new String[] {systemType})
     *
     * @param systemType The name of the system type to restrict to
     * 
     * @see org.eclipse.rse.core.IRSESystemType
     */
    public void setSystemType(String systemType)
    {
    	if (systemType == null)
    	  setSystemTypes(null);
    	else
    	  setSystemTypes(new String[] {systemType});
    	//System.out.println("SYSTEM TYPES SET TO "+systemType+" IN SYSQUALRMTFLDRCMBO");
    }

    /**
     * Specify if the "New Connection..." object for creating connections should be shown when the user selects 
     * the Browse... button to select a remote folder. The default is true.
     */
    public void setShowNewConnectionPrompt(boolean show)
    {
    	this.showNewPrompt = show;
		if (browseAction != null)
		  browseAction.setShowNewConnectionPrompt(show);
    }
    	
	/**
	 * Return the combo box history widget
	 */
	public SystemHistoryCombo getFolderCombo()
	{
		return folderCombo;
	}
	/**
	 * Return the combo box widget
	 */
	public SystemHistoryCombo getHistoryCombo()
	{
		return folderCombo;
	}
	/**
	 * Return the raw combo box widget
	 */
	public Combo getCombo()
	{
		return folderCombo.getCombo();
	}

	/**
	 * Set the width hint for this whole composite
	 * Default is computed from the child widgets
	 */
	public void setWidthHint(int widthHint)
	{
		// after much research it was decided that it was the wrong thing to do to
		// explicitly set the widthHint of a child widget without our composite, as 
		// that could end up being a bigger number than the composites widthHint itself
		// if the caller set its it directly.
		// Rather, we just set the overall composite width and specify the combo child
		// widget is to grab all the space within that which the little button does not use.
	    ((GridData)getLayoutData()).widthHint = widthHint;
	}

	/**
	 * Return the browse button widget
	 */
	public Button getBrowseButton()
	{
		return browseButton;
	}

    /**
     * Set the folders in the combo field.
     * History is updated.
     * @param folders Array of IRemoteFile objects ... each is a remote folder
     */
    public void setFolders(IRemoteFile[] folders)
    {
    	if (folders == null)
    	{
    	  //folderStrings = null;
    	  folderCombo.setItems(null);
    	  return;
    	}
    	String[] folderStrings = new String[folders.length];
    	for (int idx=0; idx<folders.length; idx++)
    	   folderStrings[idx] = folders[idx].getAbsolutePathPlusConnection();
    	folderCombo.setItems(folderStrings);
    	folderCombo.select(0);
    	updateHistory();    	
    }
    /**
     * Append a folder to the list. It is not selected so call {@link #setFolder(IRemoteFile)} 
     *  or {@link #setSelectionIndex(int)} to select it if desired.
     * History is updated.
     * @param folder The folder to add. Caller's responsibility to precheck for existence 
     *   by calling {@link #getFolderIndex(IRemoteFile)}.
     * @return int zero-based index position of new entry in list.
     */
    public int addFolder(IRemoteFile folder)
    {
    	int pos = -1;
    	String[] folderStrings = folderCombo.getItems();
    	if (folderStrings == null)
    	{
    	  folderStrings = new String[1];
    	  folderStrings[0] = folder.getAbsolutePathPlusConnection();
    	  pos = 0;
    	}
    	else
    	{
    	  String[] newFolderStrings = new String[folderStrings.length+1];
    	  for (int idx=0; idx<folderStrings.length; idx++)
    	     newFolderStrings[idx] = folderStrings[idx];
    	  pos = newFolderStrings.length-1;
    	  newFolderStrings[pos] = folder.getAbsolutePathPlusConnection();
    	  folderStrings = newFolderStrings;
    	}
    	folderCombo.setItems(folderStrings);
    	//folderCombo.select(pos);
    	folderCombo.setHistory(folderStrings);
    	return pos;
    }
    /**
     * Remove a folder from the list. History is updated
     * @param folder The folder to remove.
     */
    public void removeFolder(IRemoteFile folder)
    {
		int idx = getFolderIndex(folder);
		if (idx > -1)
		{
		  folderCombo.getCombo().remove(idx);
		  updateHistory();
		}
    }

    /**
     * Get the items in the combo field as an array of strings.
     * @return Array of String objects
     */
    public String[] getItems()
    {
    	return folderCombo.getItems();
    }

    /**
     * Get the index of a given folder in the current list for this combo.
     * Returns -1 if it is not in the list.
     */
    public int getFolderIndex(IRemoteFile folder)
    {
    	int match = -1;
    	String[] folderStrings = folderCombo.getItems();
    	if ( (folderStrings == null) || (folderStrings.length==0) )
    	  return match;
    	IHost conn = folder.getSystemConnection();
    	String fulldir = folder.getAbsolutePathPlusConnection();
    	for (int idx=0; (idx<folderStrings.length) && (match==-1); idx++)
    	{    		
          if (folderStrings[idx].equals(fulldir))
            match = idx;
    	}
    	return match;
    }

	/**
	 * Set the folder combo field's current contents given an IRemoteFile object.
	 * If this folder is in the list, it is selected.
	 * If it is not in the list, it is added and selected.
	 */
	public void setFolder(IRemoteFile folder)
	{
		int idx = getFolderIndex(folder);
		if (idx == -1)
		  idx = addFolder(folder);
		select(idx);
	}
    /**
     * Set the value by selecting the item in the list at the given position.
     * Same as {@link #select(int)}
     */
    public void setSelectionIndex(int selIdx)
    {
    	select(selIdx);
    }
    /**
     * Select the combo dropdown list entry at the given index
     * Same as {@link #setSelectionIndex(int)}
     */
    public void select(int selIdx)
    {
		folderCombo.clearSelection();
    	folderCombo.select(selIdx);
    }

    /**
     * Clear the selection of the text in the entry field part of the combo, and the list selection
     */
    public void clearSelection()
    {
    	folderCombo.clearSelection();
    }
    /**
     * Clear the selection of the text in the entry field part of the combo
     */
    public void clearTextSelection()
    {
    	folderCombo.clearTextSelection();
    }

    /**
     * Get the index number of the currently selected item. 
     */
    public int getSelectionIndex()
    {
    	return folderCombo.getSelectionIndex();
    }

	/**
	 * Query the folder combo field's current contents
	 */
	public String getText()
	{
		return folderCombo.getText();
	}
	/**
	 * Query the folder combo field's current contents as an IRemoteFile object.
	 * It is at this time that the currently selected string is converted into an
	 *  IRemoteFile object. This can be a bit computationally intensive, as it requires
	 *  accessing the remote system, and prompting for a connection if the user is not
	 *  not yet connected to that system. 
	 * <p>
	 * Since it is possible the selected item is no longer valid, this will throw an
	 *  exception if the profile or connection does not exist or the user cancels the
	 *  connecting action. The message in the exception is translated and displayable.
	 */
	public IRemoteFile getFolder()
	 throws Exception
	{
		String fileString = folderCombo.getText().trim();
		//System.out.println("selected idx = " + idx);
		if (fileString.length() == 0)
		  return null;
		else
		{
		   IRemoteFile fileObj = (IRemoteFile)resolvedFolders.get(fileString);
		   if (fileObj == null)
		   {
		   	 fileObj = convertToRemoteFile(fileString);
		   	 if (fileObj != null)
		   	   resolvedFolders.put(fileString, fileObj);
		   }
		   return fileObj;
		}
	}
	/**
	 * Query the folder combo field's current contents and return the connection part of
	 *  it as a SystemConnection object.
     * <p>
	 * Will return null if either there is no contents currently or there is no such system!
	 */
	public IHost getSystemConnection()
	{
		String fileString = folderCombo.getText().trim();
		if (fileString.length() == 0)
		  return null;		
    	String profileName = extractProfileName(fileString);
    	String connName = extractConnectionName(fileString);
    	if ((profileName == null) || (connName == null))
    	  return null;
    	ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
    	ISystemProfile profile = sr.getSystemProfile(profileName);
    	if (profile == null)
          return null;
    	IHost conn = RSEUIPlugin.getTheSystemRegistry().getHost(profile,connName);
    	return conn;
	}
	
	/**
	 * Disable/Enable all the child controls.
	 */
	public void setEnabled(boolean enabled)
	{
		folderCombo.setEnabled(enabled);
		browseButton.setEnabled(enabled);		
	}
	/**
	 * Set the tooltip text for the folder combo field
	 */
	public void setToolTipText(String tip)
	{
	    folderLabel.setToolTipText(tip);
		folderCombo.setToolTipText(tip);
	}
	/**
	 * Set the tooltip text for the browse button
	 */
	public void setBrowseButtonToolTipText(String tip)
	{
		browseButton.setToolTipText(tip);
	}
	/**
	 * Same as {@link #setBrowseButtonToolTipText(String)}
	 */
	public void setButtonToolTipText(String tip)
	{
		setBrowseButtonToolTipText(tip);
	}

	/**
	 * Set the folder combo field's text limit
	 */
	public void setTextLimit(int limit)
	{
		folderCombo.setTextLimit(limit);
	}
	/**
	 * Set the focus to the folder combo field
	 */
	public boolean setFocus()
	{
		return folderCombo.setFocus();
	}
	/**
	 * Set the focus to the browse button
	 */
	public void setBrowseButtonFocus()
	{
		browseButton.setFocus();
	}

	/**
	 * Register a listener interested in an item is selected in the combo box
     * @see #removeSelectionListener(SelectionListener)
     */
    public void addSelectionListener(SelectionListener listener) 
    {
	    folderCombo.addSelectionListener(listener);
    }
    /** 
     * Remove a previously set combo box selection listener.
     * @see #addSelectionListener(SelectionListener)
     */
    public void removeSelectionListener(SelectionListener listener) 
    {
	    folderCombo.removeSelectionListener(listener);
    }
	/**
	 * Register a listener interested in when the browse button is selected
     * @see #removeBrowseButtonSelectionListener(SelectionListener)
     */
    public void addBrowseButtonSelectionListener(SelectionListener listener) 
    {
	    browseButton.addSelectionListener(listener);
    }
    /** 
     * Remove a previously set browse button selection listener.
     * @see #addBrowseButtonSelectionListener(SelectionListener)
     */
    public void removeBrowseButtonSelectionListener(SelectionListener listener) 
    {
	    browseButton.removeSelectionListener(listener);
    }

	/**
	 * Register a listener interested in entry field modify events
     * @see #removeModifyListener(ModifyListener)
     */
    public void addModifyListener(ModifyListener listener) 
    {
	    folderCombo.addModifyListener(listener);
    }
    /** 
     * Remove a previously set entry field listener.
     * @see #addModifyListener(ModifyListener)
     */
    public void removeModifyListener(ModifyListener listener) 
    {
	    folderCombo.removeModifyListener(listener);
    }
	
	/**
	 * Return the current history for the folder combo box
	 */
	public static String[] getHistory()
	{
		return SystemPreferencesManager.getPreferencesManager().getFolderHistory();
	}

	/**
	 * Update the history with current entry field setting.
	 * <p>
	 * This is called automatically for you whenever this list is changed. 
	 */
	public void updateHistory()
	{
        folderCombo.updateHistory();
	}	
	
	// ---------------------------------------------------------
	// METHODS TO CONVERT FROM STRINGS TO IREMOTEFILE OBJECTS...
	// ---------------------------------------------------------
	/**
	 * Given a qualified folder name, return an IRemoteFile object representing it.
	 * Will return null if there is no connection of the given name, or that connection
	 *  does not have a subsystem that implements RemoteFileSubSystem.
	 * <p>
	 * Note that if the connection contains multiple subsystems that implement RemoteFileSubSystem,
	 *  or that come from a subsystem factory of the given subsystem factory ID, 
	 *  the first such subsystem is chosen.
	 */
    public IRemoteFile convertToRemoteFile(String qualifiedFolder)
        throws Exception
    {
    	SystemMessage msg = null;
    	// parse string in constituent profile, connection and folder parts...
    	String profileName = extractProfileName(qualifiedFolder);
    	String connName = extractConnectionName(qualifiedFolder);
    	String dirName = extractFolder(qualifiedFolder);
    	if ((profileName == null) || (connName == null) || (dirName == null))
    	  return null;
    	  
    	ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
    	
    	// turn profile name into profile object...
    	ISystemProfile profile = sr.getSystemProfile(profileName);
    	if (profile == null)
    	{
    	  msg = RSEUIPlugin.getPluginMessage(MSG_ERROR_PROFILE_NOTFOUND);
    	  msg.makeSubstitution(profileName);
    	  throw new Exception(msg.getLevelOneText());
    	}
    	
		// turn connection name into connection object...
    	IHost conn = RSEUIPlugin.getTheSystemRegistry().getHost(profile,connName);
    	if (conn == null)
    	{
    	  msg = RSEUIPlugin.getPluginMessage(MSG_ERROR_CONNECTION_NOTFOUND);
    	  msg.makeSubstitution(connName);
    	  throw new Exception(msg.getLevelOneText());
    	}
    	
    	// turn folder name into folder object...
    	IRemoteFile remoteFolder = null;
    	ISubSystem[] filesubsystems = null;
    	//if (subsystemFactoryID == null)
    	  filesubsystems = RemoteFileUtility.getFileSubSystems(conn);
    	//else
    	//  filesubsystems = sr.getSubSystems(subsystemFactoryID, conn);
    	
    	if (filesubsystems.length == 0)
    	{
    	  msg = RSEUIPlugin.getPluginMessage(MSG_ERROR_CONNECTION_NOTFOUND);// hmm, what else to say?
    	  msg.makeSubstitution(connName);
    	  throw new Exception(msg.getLevelOneText());
    	}
    	IRemoteFileSubSystem ss = (IRemoteFileSubSystem)filesubsystems[0]; // what else to do?
    	if (!ss.isConnected())
    	{
    	   try 
    	   {
    	     ss.connect(getShell()); // will throw exception if fails.
    	   } catch (InterruptedException exc)
    	   {
    	     msg = RSEUIPlugin.getPluginMessage(MSG_CONNECT_CANCELLED);
    	     msg.makeSubstitution(conn.getHostName());
    	     throw new Exception(msg.getLevelOneText());    	   	 
    	   } catch (Exception exc)
    	   {
    	     msg = RSEUIPlugin.getPluginMessage(MSG_CONNECT_FAILED);
    	     msg.makeSubstitution(conn.getHostName());
    	     throw new Exception(msg.getLevelOneText());    	   	 
    	   }     	   
    	}
    	if (ss.isConnected())    	  
    	  remoteFolder = ss.getRemoteFileObject(dirName);
    	return remoteFolder;
    }
    
    /**
     * Get the profile.connection name part of a qualified folder string.
     */
    public static String extractQualifiedConnectionName(String qualifiedFolder)
    {
    	int idx = qualifiedFolder.indexOf(CONNECTION_DELIMITER);
    	if (idx == -1)
    	  return null;
    	else
    	  return qualifiedFolder.substring(0,idx);
    }
    /**
     * Get the profile name part of a profile.connection string
     */
    public static String extractProfileName(String qualifiedConnectionName)
    {
    	int idx = qualifiedConnectionName.indexOf('.');
    	if (idx == -1)
    	  return null;
    	else
    	  return qualifiedConnectionName.substring(0,idx);
    }
    /**
     * Get the connection name part of a profile.connection string.
     * Will work if given profile.connection or profile.connection\folder
     */
    public static String extractConnectionName(String qualifiedConnectionName)
    {
    	int idx = qualifiedConnectionName.indexOf('.');
    	if (idx == -1)
    	  return null;
    	else
    	{
    	  String nonProfile = qualifiedConnectionName.substring(idx+1);
    	  idx = nonProfile.indexOf(CONNECTION_DELIMITER);
    	  if (idx == -1)
    	    return nonProfile;
    	  else
    	    return nonProfile.substring(0,idx);
    	}
    }

    /**
     * Get the folder name part of a qualified folder string.
     */
    public static String extractFolder(String qualifiedFolder)
    {
    	int idx = qualifiedFolder.indexOf(CONNECTION_DELIMITER);
    	if (idx == -1)
    	  return null;
    	else
    	  return qualifiedFolder.substring(idx+1);
    }

	// -----------------------
	// INTERNAL-USE METHODS...
	// -----------------------

	/**
	 * Prepares this composite control and sets the default layout data.
	 * @param Number of columns the new group will contain.     
	 */
	protected Composite prepareComposite(int numColumns)
	{
		Composite composite = this;
		//GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		//GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.CENTER;
		data.horizontalAlignment = GridData.FILL;
	    data.grabExcessHorizontalSpace = true;        
	    data.grabExcessVerticalSpace = false;        
        //data.widthHint = 300;
		composite.setLayoutData(data);
		return composite;
	}

	protected void addOurButtonSelectionListener()
	{
	   // Add the button listener
	   SelectionListener selectionListener = new SelectionAdapter() 
	   {
		  public void widgetSelected(SelectionEvent event) 
		  {
		  	  IHost defaultConnection = null;
		  	  IRemoteFile currFolder = null;
		  	  try
		  	  {
		  	    currFolder = getFolder();
		  	  } catch (Exception exc) {}
		  	  if (currFolder != null)
		  	    defaultConnection = currFolder.getSystemConnection();
		  	    
		  	  if (browseAction == null)
		  	  {
	            browseAction = getBrowseAction(getShell(), defaultConnection);	          
	          }
	          browseAction.setShowNewConnectionPrompt(showNewPrompt);
	          //if (defaultConnection != null)
	            browseAction.setHost(defaultConnection);	            
	          //if (systemTypes != null)
	            browseAction.setSystemTypes(systemTypes);
	          //if (systemTypes != null)
	          //    System.out.println("browseAction systemsTypes set to "+systemTypes[0]);
	            

	          browseAction.run();
              IRemoteFile folder = ((SystemSelectRemoteFolderAction)browseAction).getSelectedFolder();
              if (folder != null)
                setFolder(folder);
		  };
	   };
	   browseButton.addSelectionListener(selectionListener);
	}
	/**
	 * Returns action to be called when Browse... pressed.
	 */
	protected SystemSelectRemoteFolderAction getBrowseAction(Shell shell, IHost defaultConnection)
	{		
		SystemSelectRemoteFolderAction action = new SystemSelectRemoteFolderAction(shell);
		return action;
	}


	public static Button createPushButton(Composite group, String label)
	{
	   Button button = new Button(group, SWT.PUSH);
	   button.setText(label);
	   //button.setText("THIS IS A VERY LONG LABEL. I MEAN, IT IS JUST HUGE");
	   GridData data = new GridData();
	   data.horizontalAlignment = GridData.FILL;	   
	   data.grabExcessHorizontalSpace = false;	   
	   button.setLayoutData(data);
	   return button;
	}
	protected static Button createPushButton(Composite group, ResourceBundle bundle, String key)
	{
		String label = bundle.getString(key+"label");
	    Button button = createPushButton(group,label);
	    button.setToolTipText(bundle.getString(key+"tooltip"));
	    return button;
	}

    	
}