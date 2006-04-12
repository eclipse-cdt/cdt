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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.rse.files.ui.SystemFileTreeAndListGroup;
import org.eclipse.rse.files.ui.actions.SystemSelectFileTypesAction;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileRoot;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.view.SystemViewLabelAndContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


/**
 * This re-usable composite widget is for prompting the user to select
 *  multiple files from a remote system.
 * <p>
 * The composite is layed as follows:
 * <code>
 *   Folder: ______________V  Browse...
 * 
 *   .________________.  ____________. 
 *   | + folder      |   | file1     |
 *   |   + subfolder |   | file2     |
 *   |   + subfolder |   | file3     |
 *   .---------------.   .-----------.
 * </code>
 * The tree and list boxes are checkbox widgets, standard in Eclipse.
 * <p>
 * You can also optionally decide to show the standard buttons at the
 * button of the checkbox boxes:
 * <code>
 *   .-----------------.  .------------.  .--------------.
 *   | Select Types... |  | Select All |  | Deselect All |
 *   .-----------------.  .------------.  .--------------.
 * </code> 
 *
 * <p>
 * To allow error messages to be issued on a message line versus in message dialogs,
 * pass in an ISystemMessageLine reference. This usually represents the message line
 * of a dialog or wizard. If supplied, messages are written to it.
 * To specify it, call {@link #setMessageLine(ISystemMessageLine)}.
 */
public class SystemSelectRemoteFilesForm extends Composite
{
    // widgets
    private SystemQualifiedRemoteFolderCombo dirCombo = null;
    private SystemFileTreeAndListGroup    fileSelector = null;
    //private RemoteFileEmptyImpl           emptyFileSelectorRoot = null;
    private RemoteFileRoot            rootElement = null;
    private Button                        selectTypesButton, selectAllButton, deselectAllButton;
    // state
    private String             historyKey = null;
    private boolean            showSelectTypesButton, showSelectAllButtons = false;
    private ISystemMessageLine msgLine = null;
	private java.util.List     selectedTypes = new ArrayList();
	private String             filterString = null;
    private	SystemSelectFileTypesAction typesAction = null;
    private SystemViewLabelAndContentProvider folderProvider, fileProvider;
    // constants
	private final static int SIZING_SELECTION_WIDGET_WIDTH  = 400;
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 150; 
	/**
	 * Constructor when you want to specify your own history key for the qualified folder
	 * nested widget.
	 * 
	 * @param parent The owning composite
	 * @param style The swt style to apply to the overall composite. Typically it is just SWT.NULL
	 * @param showSelectTypesButton Specify if you want to have Select Types button
	 * @param showSelectAllButtons Specify if you want to have Select All and Deselect All buttons
	 * @param key The unique string used as a preferences key to persist the qualified-folder history
	 */
	public SystemSelectRemoteFilesForm(Composite parent, int style, 
	                                   boolean showSelectTypesButton, boolean showSelectAllButtons, 
	                                   String key)
	{
		super(parent, style);			
		this.showSelectTypesButton = showSelectTypesButton;
		this.showSelectAllButtons = showSelectAllButtons;
		historyKey = key;	
		prepareComposite(1);
		createFolderPrompt(this);
		createFilesSelectorGroup(this);
		if (showSelectTypesButton || showSelectAllButtons)
		  createButtons(this, showSelectTypesButton, showSelectAllButtons);
        addOurQualifiedFolderSelectionListener();
	}
	/**
	 * Constructor when you want to use the default history key, meaning you share the
	 * qualified-folder history with other dialogs/wizards that use the same widget.
	 * 
	 * @param parent The owning composite
	 * @param style The swt style to apply to the overall composite. Typically it is just SWT.NULL
	 * @param showSelectTypesButton Specify if you want to have Select Types button
	 * @param showSelectAllButtons Specify if you want to have Select All and Deselect All buttons
	 */
	public SystemSelectRemoteFilesForm(Composite parent, int style, 
	                                   boolean showSelectTypesButton, boolean showSelectAllButtons)
	{
		this(parent, style, showSelectTypesButton, showSelectAllButtons, null);			
	}
	/**
	 * Constructor when you want to use the default history key and want to show the standard buttons.
	 * 
	 * @param parent The owning composite
	 * @param style The swt style to apply to the overall composite. Typically it is just SWT.NULL
	 */
	public SystemSelectRemoteFilesForm(Composite parent, int style) 
	{
		this(parent, style, true, true, null);			
	}
	
	/**
	 * Specify a message line so error messages are written to it versus in message dialogs.
	 * @param msgLine An object implementing ISystemMessageLine. Typically pass "this" for 
	 *  your dialog or wizard page, and implement the interface there.
	 */
	public void setMessageLine(ISystemMessageLine msgLine)
	{
		this.msgLine = msgLine;
	}
	/**
	 * Get the message line specified in setMessageLine.
	 */
	public ISystemMessageLine getMessageLine()
	{
		return msgLine;
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
    	dirCombo.setSystemTypes(systemTypes);
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
     * Set a filter string to subset the list by. For example, "A*.java" or "java,class,"
     */
    public void setFilterString(String filterString)
    {
    	fileProvider.setFilterString(filterString);
    	folderProvider.setFilterString(filterString);
    	refresh();
    }
    /**
     * Get the current filter string being used to subset the list by.
     * Will be null unless setFilterString has previously been called.
     */
    public String getFilterString()
    {
    	return filterString;
    }
    
    /**
     * Return the nested qualified-folder combo widget
     */
    public SystemQualifiedRemoteFolderCombo getFolderCombo()
    {
    	return dirCombo;
    }
	
	/**
	 * Disable/Enable all the child controls.
	 */
	public void setEnabled(boolean enabled)
	{
		dirCombo.setEnabled(enabled);
		fileSelector.getListTable().setEnabled(enabled);
	}
	/**
	 * Set the focus to the combo field
	 */
	public boolean setFocus()
	{
		return dirCombo.setFocus();
	}

    /**
     * Clear the contents of the file selector widgets
     */
    public void clearAll()
    {
    	fileSelector.clearAll();
    	enableButtonGroup(false);
    }
    /**
     * Set the root folder from which to populate the widgets
     * @param rootFolder The root folder from which to start the tree
     */
    public void setRootFolder(IRemoteFile rootFolder)
    {
    	rootElement.setRootFile(rootFolder);
    	fileSelector.setRoot(rootElement);
    	enableButtonGroup(true);
    }    
    
    /**
     * Refesh the contents of the folder and file-selection checkbox viewers
     */
    public void refresh()
    {
        folderProvider.flushCache();
        fileProvider.flushCache();
        fileSelector.refresh();
    }
    
    /**
     * Add a checkstate listener to be called whenever the checked state of
     *  a remote file is changed.
     */
    public void addCheckStateListener(ICheckStateListener l)
    {
    	fileSelector.addCheckStateListener(l);
    }
    /**
     * Remove a checkstate listener 
     */
    public void removeCheckStateListener(ICheckStateListener l)
    {
    	fileSelector.removeCheckStateListener(l);
    }

    /**
     * Get the list of selected IRemoteFile objects.
     * Will be length 0 if nothing selected
     */
    public IRemoteFile[] getSelectedFiles()
    {
        java.util.List list = fileSelector.getAllCheckedListItems();
    	IRemoteFile[] files = new IRemoteFile[list.size()];
        Iterator i = list.iterator();
        int idx = 0;
        while (i.hasNext())
        {
        	files[idx++] = (IRemoteFile)i.next();
        }   	
    	return files;
    }	

	// -----------------------
	// INTERNAL-USE METHODS...
	// -----------------------
	/**
	 * Prepares this composite control and sets the default layout data.
	 * @param numColumns Number of columns the new group will contain.     
	 */
	protected Composite prepareComposite(int numColumns)
	{
		Composite composite = this;
		//GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;		
		composite.setLayout(layout);
		//GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
	    data.grabExcessHorizontalSpace = true;        
	    data.grabExcessVerticalSpace = false;	    
	    //data.widthHint = SIZING_SELECTION_WIDGET_WIDTH + 20;
		composite.setLayoutData(data);
		return composite;
	}	
	
	/**
	 * Create qualified-folder prompt
	 */
	protected void createFolderPrompt(Composite parent)
	{
        // connection\folder prompt
        dirCombo = new SystemQualifiedRemoteFolderCombo(parent, SWT.NULL, historyKey);        
	}
	
	/**
	 * Create folder and files selection checkbox viewers
	 */
	protected void createFilesSelectorGroup(Composite parent)
	{
        // file selector prompt (checkbox tree and checkbox list)
		folderProvider = new SystemViewLabelAndContentProvider(true,false);        
		fileProvider = new SystemViewLabelAndContentProvider(false,true);        
        rootElement = new RemoteFileRoot();
        fileSelector = new SystemFileTreeAndListGroup(parent, 
                                                    folderProvider, folderProvider, // tree content and label providers
                                                    fileProvider, fileProvider, // list content and label providers
                                                    SWT.NULL, SIZING_SELECTION_WIDGET_WIDTH, 
                                                    SIZING_SELECTION_WIDGET_HEIGHT);		
	}
	/**
	 * Create three buttons under checkbox viewers
	 */
	protected void createButtons(Composite parent, boolean showSelectTypes, boolean showSelectAll)
	{
	    Composite buttonComposite = new Composite(parent, SWT.NONE);
  	    GridLayout layout = new GridLayout();
	    if (showSelectTypes && showSelectAll)
	      layout.numColumns = 3;
	    else if (showSelectTypes)
	      layout.numColumns = 1;
	    else
	      layout.numColumns = 2;
	    layout.makeColumnsEqualWidth = true;
	    buttonComposite.setLayout(layout);
	    buttonComposite.setLayoutData(
		new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

        if (showSelectTypes)
        {
          selectTypesButton = createPushButton(buttonComposite, SystemResources.RESID_SELECTFILES_SELECTTYPES_BUTTON_ROOT_LABEL, SystemResources.RESID_SELECTFILES_SELECTTYPES_BUTTON_ROOT_TOOLTIP);
          addSelectTypesButtonSelectionListener();
        }
        if (showSelectAll)
        {
          selectAllButton = createPushButton(buttonComposite, SystemResources.RESID_SELECTFILES_SELECTALL_BUTTON_ROOT_LABEL);
          deselectAllButton = createPushButton(buttonComposite, SystemResources.RESID_SELECTFILES_DESELECTALL_BUTTON_ROOT_TOOLTIP);
          addSelectAllButtonSelectionListener();
          addDeselectAllButtonSelectionListener();
        }
        
        enableButtonGroup(false);
	}
	protected static Button createPushButton(Composite group, String label, String tooltip)
	{
	    Button button = createPushButton(group,label);
	    button.setToolTipText(tooltip);
	    return button;
	}
	public static Button createPushButton(Composite group, String label)
	{
	   Button button = new Button(group, SWT.PUSH);
	   button.setText(label);
	   //button.setText("THIS IS A LONG LABEL. I MEAN, IT IS JUST HUGE!");
	   GridData data = new GridData(GridData.FILL_HORIZONTAL);
	   button.setLayoutData(data);
	   return button;
	}

	
	protected void addOurQualifiedFolderSelectionListener()
	{
	   // Add the button listener
	   SelectionListener selectionListener = new SelectionAdapter() 
	   {
		  public void widgetSelected(SelectionEvent event) 
		  {
    	    clearErrorMessage();
    	    //clearMessage();
    		IRemoteFile selectedFolder = null;   		
    		try 
    		{
    		  	selectedFolder = dirCombo.getFolder();
    		  	if (selectedFolder == null)
    		  	{
    		  	  //displayErrorMessage("Nothing selected?");
    		  	  clearAll();
    		  	}
    		  	else
    		  	{
    		  	  //displayMessage("Selected folder exist? " + selectedFolder.exists());
    		  	  setRootFolder(selectedFolder);
    		  	}
    		}
    		catch (Exception exc)
    		{
    			String msg = exc.getMessage();
    			if ((msg == null) || (msg.length()==0))
    			{
    			  msg = "Exception: " + exc.getClass().getName();
    			  displayExceptionMessage(exc);
    			}
    			else
    			  displayErrorMessage(msg);
    			clearAll();
    		}
		  };
	   };
	   dirCombo.addSelectionListener(selectionListener);
	}

	protected void addSelectTypesButtonSelectionListener()
	{
	   // Add the button listener
	   SelectionListener selectionListener = new SelectionAdapter() 
	   {
		  public void widgetSelected(SelectionEvent event) 
		  {
              handleTypesEditButtonPressed();
		  };
	   };
	   selectTypesButton.addSelectionListener(selectionListener);
	}
	protected void addSelectAllButtonSelectionListener()
	{
	   // Add the button listener
	   SelectionListener selectionListener = new SelectionAdapter() 
	   {
		  public void widgetSelected(SelectionEvent event) 
		  {
              fileSelector.setAllSelections(true);
		  };
	   };
	   selectAllButton.addSelectionListener(selectionListener);
	}
	protected void addDeselectAllButtonSelectionListener()
	{
	   // Add the button listener
	   SelectionListener selectionListener = new SelectionAdapter() 
	   {
		  public void widgetSelected(SelectionEvent event) 
		  {
              fileSelector.setAllSelections(false);
		  };
	   };
	   deselectAllButton.addSelectionListener(selectionListener);
	}

	/**
	 *	Open a registered type selection dialog and note the selections
	 *	in the receivers types-to-export field
	 */
	protected void handleTypesEditButtonPressed() 
	{
		SystemSelectFileTypesAction typesAction = getSelectTypesAction();
		typesAction.setTypes(selectedTypes);
		typesAction.run();
		if (!typesAction.wasCancelled())
		{
		  selectedTypes = typesAction.getTypes();
		  setupSelectionsBasedOnSelectedTypes();
		}
		/*
		TypeFilteringDialog dialog =
			new TypeFilteringDialog(getShell(), selectedTypes);
		dialog.open();
		Object[] newSelectedTypes = dialog.getResult();
		if (newSelectedTypes != null) // ie.- did not press Cancel
		{
			this.selectedTypes = new ArrayList(newSelectedTypes.length);
			for (int i = 0; i < newSelectedTypes.length; i++)
			{
				//System.out.println(newSelectedTypes[i]);
				this.selectedTypes.add(newSelectedTypes[i]);
			}
			setupSelectionsBasedOnSelectedTypes();
		}
		*/
	}
	/**
	 * Get the action to run when "Select Types..." is pressed by the user
	 */
	protected SystemSelectFileTypesAction getSelectTypesAction()
	{
		if (typesAction == null)
		  typesAction = new SystemSelectFileTypesAction(getShell());
		return typesAction;
	}
	/**
	 * Update the tree to only select those elements that match the selected types
	 */
	protected void setupSelectionsBasedOnSelectedTypes() 
	{
		//BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() 
		//{
			//public void run() 
			//{
				Map selectionMap = new Hashtable();
				IRemoteFile[] filesList = getSelectedFiles();
				for (int idx=0;idx<filesList.length; idx++)
				{
                    IRemoteFile file = filesList[idx];
                    String extension = file.getExtension();
                    //System.out.println("File extension for " + file.getName() + ": " + extension);
					if (isExportableExtension(file.getExtension()))
					{
						java.util.List elements = new ArrayList();
						IRemoteFile parent = file.getParentRemoteFile();
						if (selectionMap.containsKey(parent))
							elements = (java.util.List)selectionMap.get(parent);
						elements.add(file);
						selectionMap.put(parent, elements);
					}
				}
				fileSelector.updateSelections(selectionMap);
			//}
		//});
	}
	/**
	 * Returns whether the extension provided is an extension that
	 * has been specified for export by the user.
	 *
	 * @param extension the resource name
	 * @return <code>true</code> if the resource name is suitable for export based 
	 *   upon its extension
	 */
	protected boolean isExportableExtension(String extension) 
	{
		if (selectedTypes == null)	// ie.- all extensions are acceptable
			return true;
		Iterator enumer = selectedTypes.iterator();
		while (enumer.hasNext()) 
		{
			if (extension.equalsIgnoreCase((String)enumer.next()))
				return true;
		}	
		return false;
	}


	/**
	 * Enable or disable the button group.
	 */
	protected void enableButtonGroup(boolean enable) 
	{
		if (!showSelectTypesButton && !showSelectAllButtons)
		  return;
		if (selectTypesButton != null)
		  selectTypesButton.setEnabled(enable);
		if (selectAllButton != null)
		  selectAllButton.setEnabled(enable);
		if (deselectAllButton != null)
		  deselectAllButton.setEnabled(enable);
	}

	
	protected void clearErrorMessage()
	{
		if (msgLine != null)
		  msgLine.clearErrorMessage();
	}
	
	protected void displayErrorMessage(String msgText)
	{
		if (msgLine != null)
		  msgLine.setErrorMessage(msgText);
		else
		  SystemMessageDialog.displayErrorMessage(getShell(),msgText);
	}
	protected void displayErrorMessage(SystemMessage msg)
	{
		if (msgLine != null)
		  msgLine.setErrorMessage(msg);
		else
		  SystemMessageDialog.displayErrorMessage(getShell(),msg);
	}
	protected void displayExceptionMessage(Exception exc)
	{				
		if (msgLine != null)
		{
    	  SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXCEPTION_OCCURRED);
    	  msg.makeSubstitution(exc);
		  msgLine.setErrorMessage(msg);
		}
		else
		  SystemMessageDialog.displayExceptionMessage(getShell(), exc);
	}

}