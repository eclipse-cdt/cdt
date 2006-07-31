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
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.files.ui.actions.SystemSelectRemoteFolderAction;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
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
 * Unlike SystemQualifiedRemoteFolderCombo, this widget does not combine the 
 *  connection name with the folder name. That means if you want to restrict
 *  this to a particular connection, you must call setSystemConnection. If 
 *  you want to restrict to any connections of a particular system type, call
 *  setSystemType.
 * <p>
 * Because this combo only deals with strings, versus IRemoteFile, it is 
 *  left to the caller to interpret that string as desired.
 * <p>
 * The composite is layed as follows:
 * <pre><code>
 *   Folder: ______________v  Browse...
 * </code></pre>
 */
public class SystemRemoteFolderCombo extends Composite implements ISystemCombo
{
	private Label              folderLabel = null;
	private SystemHistoryCombo folderCombo = null;
	private Button             browseButton = null;	
	//private RemoteFileSubSystem subsystem = null;
	//private RemoteFileSubSystemConfiguration subsystemFactory = null;
	private String[]           systemTypes = null;	
	private IHost   connection = null;
	private boolean            showNewConnectionPrompt = true;
	//private static final int DEFAULT_COMBO_WIDTH = 300;
	//private static final int DEFAULT_BUTTON_WIDTH = 80;
    private SystemSelectRemoteFolderAction browseAction = null;
    private IRemoteFileSubSystem            fileSubSystem = null;
    
    // list of listeners that are notified when the browse action is run and completed
    // note that the listener
    private Vector listeners;
    	
	/**
	 * Constructor for SystemFileFolderCombo. Requires a history key used to store/restore the
	 *  dropdown history for this. Pass null to use the overall default (ISystemPreferencesConstants.HISTORY_FOLDER)
	 * @param parent Parent composite
	 * @param style SWT style flags for overall composite widget
	 * @param historyKey A string identifying the key into the user preferences where this combo's history will be stored.
	 * @param readOnly True if the combo box is not to allow user editing.
	 * @see #setSystemConnection(IHost)
	 */
	public SystemRemoteFolderCombo(Composite parent, int style, String historyKey, boolean readOnly)
	{
		super(parent, style);		
		prepareComposite(3);
	    folderLabel = SystemWidgetHelpers.createLabel(this,SystemFileResources.WIDGET_FOLDER_LABEL, SystemFileResources.WIDGET_FOLDER_TOOLTIP);
        if (historyKey == null)
          historyKey = ISystemPreferencesConstants.HISTORY_FOLDER;
	    folderCombo = SystemWidgetHelpers.createHistoryCombo(this,null,historyKey,readOnly,SystemFileResources.WIDGET_FOLDER_TOOLTIP);
	    Object folderData = folderCombo.getLayoutData();
	    if (folderData instanceof GridData)
	      ((GridData)folderData).widthHint = 160; 
	    browseButton = createPushButton(this,SystemFileResources.WIDGET_BROWSE_LABEL);
	    addOurButtonSelectionListener();
	    
	    listeners = new Vector();
	}

	/**
	 * Set auto-uppercase. When enabled, all non-quoted values are uppercases when appropriate.
	 * This has no effect in readonly mode!
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
    }

    /**
     * Set the input system connection to restrict the browse button to this connection only.
     * Either call this or setSubSystemConfigurationId.
     */
    public void setSystemConnection(IHost connection)
    {
    	this.connection = connection;
    }
    /**
     * Get the system connection as specified in setSystemConnection.
     */
    public IHost getSystemConnection()
    {
    	return connection;
    }

    /**
     * Set the input remote file subsystem. If set, this will allow the browse button to better
     *  pre-fill the selection dialog when browse is pressed.
     */
    public void setSubSystem(IRemoteFileSubSystem subsystem)
    {
    	this.fileSubSystem = subsystem;
    }
    
    /**
     * Set whether to allow users to create new connections when Browse is pressed
     */
    public void setShowNewConnectionPrompt(boolean show)
    {
    	this.showNewConnectionPrompt = show;
    }
    	
	/**
	 * Return the combo box widget as a System
	 * Same as {@link #getCombo()}
	 */
	public Combo getFolderCombo()
	{
		return folderCombo.getCombo();
	}
	
	/**
	 * Get folder label.
	 * @return the folder label.
	 */
	public Label getFolderLabel() {
		return folderLabel;
	}
	
	/**
	 * Return the combo box historical widget
	 * Same as {@link #getCombo()} but returns the combo reference as a SystemHistoryCombo reference
	 */
	public SystemHistoryCombo getHistoryCombo()
	{
		return folderCombo;
	}
	/**
	 * Return the embedded combo box widget
	 * Same as {@link #getFolderCombo()}
	 */
	public Combo getCombo()
	{
		return folderCombo.getCombo();
	}

	/**
	 * Set the width hint for this whole composite
	 * Default is computed from children.
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
     * Set the items in the combo field
     */
    public void setItems(String[] items)
    {
    	folderCombo.setItems(items);
    }
    /**
     * Get the items in the combo field
     */
    public String[] getItems()
    {
    	return folderCombo.getItems();
    }


	/**
	 * Set the folder combo field's current contents
	 */
	public void setText(String text)
	{
		folderCombo.setText(text);
	}

	/**
	 * Query the folder combo field's current contents
	 */
	public String getText()
	{
		return folderCombo.getText();
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
	 * Set the tooltip text for the browse button.
	 * Same as {@link #setBrowseButtonToolTipText(String)}
	 */
	public void setButtonToolTipText(String tip)
	{
		browseButton.setToolTipText(tip);
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
     * Select the combo dropdown list entry at the given index
     */
    public void select(int selIdx)
    {
    	folderCombo.select(selIdx);
    }
    /**
     * Same as {@link #select(int)}
     */
    public void setSelectionIndex(int selIdx)
    {
    	select(selIdx);
    }
    /**
     * Get the index number of the currently selected item. 
     */
    public int getSelectionIndex()
    {
    	return folderCombo.getSelectionIndex();
    }
    
    /**
     * Clear the selection of the text in the entry field part of the combo
     */
    public void clearSelection()
    {
    	folderCombo.clearSelection();
    }
    /**
     * Clear the entered/selected contents of the combo box. Clears only the text selection, not the list selection
     */
    public void clearTextSelection()
    {
    	folderCombo.clearTextSelection();
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
     * Adds a listener which will be notified each time after the browse action is run.
     * @param listener the listener to be notified.
     */
    public void addBrowseActionCompleteListener(ISystemRemoteFolderBrowseCompleteListener listener) {
    	
    	if (!listeners.contains(listener)) {
    		listeners.add(listener);
    	}
    }
    
	/**
	 * Removes the given listener. Has no effect if the listener was not added before.
	 * @param listener the listener to be removed.
	 */
	public void removeBrowseActionCompleteListener(ISystemRemoteFolderBrowseCompleteListener listener) {
    	listeners.remove(listener);
	}
	
	/**
	 * Notifies all registered listeners.
	 * @param remoteFile the remote file to use for the notification.
	 */
	protected void notifyBrowseActionCompleteListeners(IRemoteFile remoteFile) {
		
		Iterator iter = listeners.iterator();
		
		if (iter.hasNext()) {
			ISystemRemoteFolderBrowseCompleteListener listener = (ISystemRemoteFolderBrowseCompleteListener)(iter.next());
			listener.fileSelected(remoteFile);
		}
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
	 * This is called automatically for you when setText is called. However, for non-readonly
	 *   versions, you should still call this yourself when OK is successfully pressed on the
	 *   dialog box.
	 */
	public void updateHistory()
	{
        folderCombo.updateHistory();
	}
	
	/**
	 * Update the history with current entry field setting, and optionally refresh the list from the new history.
	 */
	public void updateHistory(boolean refresh) {
		folderCombo.updateHistory(refresh);
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
        //data.widthHint = 180; //CAUSES TRUNCATION IF TRANSLATED BUTTON TEXT IS LONG
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
		  	  if (browseAction == null)
		  	  {
	            browseAction = getBrowseAction(getShell());
		  	  }
	          //if (connection != null)	          
	            browseAction.setHost(connection);
	          //if (systemTypes != null)
	            browseAction.setSystemTypes(systemTypes);	          
	          browseAction.setShowNewConnectionPrompt(showNewConnectionPrompt);
	          browseAction.setShowPropertySheet(true, false);
	          String currentFolder = getText().trim();
	          if ((currentFolder.length() > 0) && (fileSubSystem!=null))
	          {
				IRemoteFile currentFolderObject = null;
	          	try {
		          	currentFolderObject = fileSubSystem.getRemoteFileObject(currentFolder);
	          	} catch(SystemMessageException e) {
	          		SystemBasePlugin.logError("SystemRemoteFolderCombo.wdigetSelected", e);
	          	}
	          	if (currentFolderObject != null)
		          browseAction.setPreSelection(currentFolderObject);	          
	          }

	          browseAction.run();
              IRemoteFile folder = ((SystemSelectRemoteFolderAction)browseAction).getSelectedFolder();
              if (folder != null)
                setText(folder.getAbsolutePath());
                
              // notify listeners with the selected folder
              // we notify even if the suer cancelled and the folder is null so listeners
              // know that the brose dialog was cancelled
              notifyBrowseActionCompleteListeners(folder);
		  };		 
	   };
	   browseButton.addSelectionListener(selectionListener);
	}
	/**
	 * Returns action to be called when Browse... pressed.
	 * Either connection or subsystemFactoryId better be set!
	 */
	protected SystemSelectRemoteFolderAction getBrowseAction(Shell shell)
	{		
		SystemSelectRemoteFolderAction action = new SystemSelectRemoteFolderAction(shell);
		return action;
	}


	public static Button createPushButton(Composite group, String label)
	{
	   Button button = new Button(group, SWT.PUSH);
	   button.setText(label);
	   //button.setText("THIS IS A VERY LONG LABEL. I MEAN IT IS JUST HUGE."); //to test mri
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