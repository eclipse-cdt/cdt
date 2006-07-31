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

package org.eclipse.rse.files.ui.widgets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemRemoteObjectMatcher;
import org.eclipse.rse.files.ui.ISystemAddFileListener;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.SystemFilterSimple;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.subsystems.files.core.model.ISystemFileRemoteTypes;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.util.SystemRemoteFileMatcher;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.IValidatorRemoteSelection;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.SystemPropertySheetForm;
import org.eclipse.rse.ui.view.SystemSelectRemoteObjectAPIProviderImpl;
import org.eclipse.rse.ui.view.SystemViewForm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;



/**
 * A reusable form for prompting for a remote file system folder or file.
 * <p>
 * This form may be used to populate a dialog or a wizard page.
 * <p>
 * To configure the functionality, call these methods:
 * <ul>
 *   <li>{@link #setShowNewConnectionPrompt(boolean)}
 *   <li>{@link #setSystemConnection(IHost) or #setDefaultConnection(SystemConnection)}
 *   <li>{@link #setSystemTypes(String[])}
 *   <li>{@link #setRootFolder(IHost, String)} or {@link #setRootFolder(IRemoteFile)}
 *   <li>{@link #setPreSelection(IRemoteFile)}
 *   <li>{@link #setFileTypes(String[])} or {@link #setFileTypes(String)} 
 *   <li>{@link #setAutoExpandDepth(int)}
 *   <li>{@link #setShowPropertySheet(boolean)}
 *   <li>{@link #enableAddMode(org.eclipse.rse.files.ui.ISystemAddFileListener)}
 *   <li>{@link #setMultipleSelectionMode(boolean)}
 *   <li>{@link #setSelectionValidator(org.eclipse.rse.ui.IValidatorRemoteSelection)}
 * </ul>
 * <p>
 * To configure the text on the dialog, call these methods:
 * <ul>
 *   <li>{@link #setMessage(String)}
 *   <li>{@link #setSelectionTreeToolTipText(String)}
 * </ul>
 * <p>
 * After running, call these methods to get the output:
 * <ul>
 *   <li>{@link #getSelectedObject()}
 *   <li>{@link #getSelectedConnection()}
 * </ul>
 */
public class SystemSelectRemoteFileOrFolderForm 
 	   implements ISelectionChangedListener, ISystemIconConstants, ISystemMessages
{	
	protected static final int PROMPT_WIDTH = 400; // The maximum width of the dialog's prompt, in pixels.
	
	// GUI widgets
    protected Label                   verbageLabel, spacer1, spacer2;
	protected Text                    nameEntryValue;
	protected SystemViewForm          tree;
    protected SystemPropertySheetForm ps;
	protected ISystemMessageLine      msgLine;	
	protected Composite               outerParent, ps_composite;	
	// inputs
	protected ISystemRegistry sr = null;
	protected String    verbage = null;
	protected String    treeTip = null;	
	protected String 	locationPrompt = "";
	protected String    fileTypes;
	protected boolean   fileMode;
	protected boolean   valid = true;
	protected boolean   filesOnlyMode;
	protected boolean   showRootFilter = true;	
	protected boolean   alwaysEnableOK = false;
	protected boolean   multipleSelectionMode;
	protected boolean   allowForMultipleParents = false;
	protected boolean   showPropertySheet = false;
	protected boolean	showLocationPrompt = false;
	protected boolean	allowFolderSelection = true;
	protected SystemRemoteObjectMatcher objectMatcher = null;
	protected ISystemAddFileListener    addButtonCallback = null;
	protected Vector    listeners = new Vector();
	protected IValidatorRemoteSelection selectionValidator;
	// outputs 
	protected Object[]         outputObjects = null;	
	protected IHost outputConnection = null;
	// state
	//protected ResourceBundle rb;
    protected SystemSelectRemoteObjectAPIProviderImpl inputProvider = null;
    protected ISystemFilter preSelectFilter;
    protected String       preSelectFilterChild;
    protected boolean      preSelectRoot;
	protected boolean      initDone;
	protected boolean      contentsCreated;
	
	//protected String  errorMessage;
	protected Object  caller;
	protected boolean callerInstanceOfWizardPage, callerInstanceOfSystemPromptDialog;	
	protected int     autoExpandDepth = 0;

	protected Object previousSelection = null;
	protected List viewerFilters = new ArrayList();

   /**
	 * Constructor
	 * @param msgLine A GUI widget capable of writing error messages to.
	 * @param caller The wizardpage or dialog hosting this form.
	 * @param fileMode true if in select-file mode, false if in select-folder mode

	 * @see #setSystemConnection(IHost)
	 * @see #setShowNewConnectionPrompt(boolean)
	 * @see #setSystemTypes(String[])
     * @see #setSelectionTreeToolTipText(String)
	 */
	public SystemSelectRemoteFileOrFolderForm(ISystemMessageLine msgLine, Object caller, boolean fileMode) 
	{
		this.msgLine = msgLine;
		this.caller = caller;
		this.fileMode = fileMode;
		callerInstanceOfWizardPage = (caller instanceof WizardPage);
		callerInstanceOfSystemPromptDialog = (caller instanceof SystemPromptDialog);				
		//rb = RSEUIPlugin.getResourceBundle();
		sr = RSEUIPlugin.getTheSystemRegistry();

		// set default GUI
		verbage = fileMode ? SystemFileResources.RESID_SELECTFILE_VERBAGE: SystemFileResources.RESID_SELECTDIRECTORY_VERBAGE;
        treeTip = fileMode ? SystemFileResources.RESID_SELECTFILE_SELECT_TOOLTIP : SystemFileResources.RESID_SELECTDIRECTORY_SELECT_TOOLTIP;

        // create the input provider that drives the contents of the tree
        inputProvider = getInputProvider();
        
         String initialFilterString = "*"; // change to "*" for defect 43492
        inputProvider.setFilterString(fileMode ? initialFilterString : initialFilterString+" /nf");
        
        // create object matcher
        if (fileMode)
          objectMatcher = SystemRemoteFileMatcher.getFileOnlyMatcher();
        else 
          objectMatcher = SystemRemoteFileMatcher.getFolderOnlyMatcher();
	}

    // ---------------------------------
    // INPUT OR CONFIGURATION METHODS...
    // ---------------------------------
	/**
	 * Returns the input provider that drives the contents of the tree
	 * Subclasses can override to provide custom tree contents
	 */
	protected SystemSelectRemoteObjectAPIProviderImpl getInputProvider()
	{
		if (inputProvider == null)
		{
		    // create the input provider that drives the contents of the tree
			inputProvider = new SystemSelectRemoteObjectAPIProviderImpl(null, ISystemFileRemoteTypes.TYPECATEGORY, 
					true, null); // show new connection prompt, no system type restrictions
	        
		}
		return inputProvider;
	}
	
	/**
	 * Indicate whether the form should allow selection of objects from different parents
	 */
    public void setAllowForMultipleParents(boolean flag)
    {
        allowForMultipleParents = flag;
    }
	
    /**
     * Set the connection to restrict the user to seeing
     */
    public void setSystemConnection(IHost conn)
    {
    	// Dave, you can't just change true to false. This causes the side effect of all selection dialogs
    	//  allowing users to see more then just their current connection, which makes no sense. It needs
    	//  to be selective based on the particular action. So, I added setDefaultConnection for those cases
    	//  where we want to see other connections. Phil
        //inputProvider.setSystemConnection(conn, /* DKM - now we support cross system copy true */ false); // true means only this connection
        inputProvider.setSystemConnection(conn,true); // true means only this connection
    }
    /**
     * Set the connection to default the selection to
     */
    public void setDefaultConnection(IHost conn)
    {
        inputProvider.setSystemConnection(conn, /* DKM - now we support cross system copy true */ false); // true means only this connection
    }

    /**
     * Set to true if a "New Connection..." special connection is to be shown for creating new connections
     */
    public void setShowNewConnectionPrompt(boolean show)
    {
    	inputProvider.setShowNewConnectionPrompt(show);
    }
    /**
     * Restrict to certain system types
     * @param systemTypes the system types to restrict what connections are shown and what types of connections
     *  the user can create
     * @see org.eclipse.rse.core.IRSESystemType
     */
    public void setSystemTypes(String[] systemTypes)
    {
    	inputProvider.setSystemTypes(systemTypes);
    }
    /**
     * Set the message shown as the text at the top of the form. Eg, "Select a file"
     */
    public void setMessage(String message)
    {
    	this.verbage = message;
    	if (verbageLabel != null)
    	  verbageLabel.setText(message);
    }
    /**
     * Set the tooltip text for the remote systems tree from which an item is selected.
     */
    public void setSelectionTreeToolTipText(String tip)
    {
    	this.treeTip = tip;
    	if (tree != null)
    	  tree.setToolTipText(tip);
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
        setSystemConnection(connection);
        setShowNewConnectionPrompt(false);
        setAutoExpandDepth(1);        
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
	/**
     * Set the root folder from which to start listing folders.
     * This version identifies the folder via an IRemoteFile object.
     * There is another overload that identifies the folder via a connection and folder path.
     * 
     * @param rootFolder The IRemoteFile object representing the remote folder to start the list from
     * 
     * @see org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString
	 */
	public void setRootFolder(IRemoteFile rootFolder)
	{
		setRootFolder(rootFolder.getSystemConnection(),rootFolder.getAbsolutePath());
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
		SystemBasePlugin.logInfo("given: '" + selection.getAbsolutePath()+"'");
		IRemoteFile parentFolder = selection.getParentRemoteFile();
		/**/
		if (parentFolder != null)
		  SystemBasePlugin.logInfo("parent of given: '" + parentFolder.getAbsolutePath() + "'");
		else
		  SystemBasePlugin.logInfo("parent of given is null");
		/**/
		// it might be a bug, bug when asking for the parent of '/', I get back '/'!!!
		if ((parentFolder != null) && 
		    (selection.getAbsolutePath().equals("/") &&
		     (parentFolder.getAbsolutePath()!=null) &&
		     parentFolder.getAbsolutePath().equals("/")))
          parentFolder = null;		
		if (parentFolder != null)
		{
		   IRemoteFileSubSystemConfiguration ssf = selection.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration();
		   boolean isUnix = ssf.isUnixStyle();
		   if (isUnix)  
		     setRestrictFolders(parentFolder.isRoot());
		   setRootFolder(parentFolder);
		   preSelectFilterChild = selection.getName();
		   //RSEUIPlugin.logInfo("Setting preSelectFilterChild to '"+preSelectFilterChild+"'");
		}
		else
		{
		   SystemBasePlugin.logInfo("preSelectRoot is true");
		   preSelectRoot = true;
		   setRestrictFolders(false);
		   setRootFolder(selection);
		}
		inputProvider.setPreSelectFilterChild(preSelectFilterChild);		
	}
	
	/**
	 * For files mode, restrict the files list by an array of file types
	 * <p>
	 * This must be called BEFORE setRootFolder!
	 */
	public void setFileTypes(String[] fileTypes)
	{
		String fts = null;
		if (fileTypes != null)
          fts = RemoteFileFilterString.getTypesString(fileTypes);
        setFileTypes(fts);
	}
	/**
	 * For files mode, restrict the files list by a comman-delimited array of file types.
	 * The last type must also end in a comma. Eg "java, class," or "class,".
	 * <p>
	 * This must be called BEFORE setRootFolder!
	 */
	public void setFileTypes(String fileTypes)
	{
		this.fileTypes = fileTypes;
        inputProvider.setFilterString("/"+ fileTypes);
	}
    /**
     * Specify the zero-based auto-expand level for the tree. The default is zero, meaning
     *   only show the connections.
     */
    public void setAutoExpandDepth(int depth)
    {
    	this.autoExpandDepth = depth+1;
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
    	//this.filesOnlyMode = restrict;
    	this.showRootFilter = !restrict;
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
    }
    /**
     * Show the property sheet on the right hand side, to show the properties of the
     * selected object.
     * <p>
     * Default is false
     */
    public void setShowPropertySheet(boolean show)
    {
    	this.showPropertySheet = show;
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
    	if (multiple)
    	  objectMatcher = null;
    }
    
    /**
     * Add a listener to selection change events in the tree
     */
    public void addSelectionChangedListener(ISelectionChangedListener l)
    {
    	if (tree != null)
    	  tree.addSelectionChangedListener(l);
    	else
    	  listeners.addElement(l);
    }
    /**
     * Remove a listener for selection change events
     */
    public void removeSelectionChangedListener(ISelectionChangedListener l)
    {
    	if (tree != null)
    	  tree.removeSelectionChangedListener(l);
    	else
    	  listeners.removeElement(l);
    }
    
    /**
     * Specify a validator to use when the user selects a remote file or folder.
     * This allows you to decide if OK should be enabled or not for that remote file or folder.
     */
    public void setSelectionValidator(IValidatorRemoteSelection selectionValidator)
    {
    	this.selectionValidator = selectionValidator;
    }
    

    // ---------------------------------
    // OUTPUT METHODS...
    // ---------------------------------
    /**
     * Return first selected object
     */	
    public Object getSelectedObject()
    {
    	if ((outputObjects != null) && (outputObjects.length>=1))
    	  return outputObjects[0];
    	else
    	  return null;
    }
    /**
     * Return all selected objects. 
     * @see #setMultipleSelectionMode(boolean)
     */	
    public Object[] getSelectedObjects()
    {
    	return outputObjects;
    }
    /**
     * Return selected connection
     */	
    public IHost getSelectedConnection()
    {
    	return outputConnection;
    }

    /**
     * Return the embedded System Tree object.
     * Will be null until createContents is called.
     */
    public SystemViewForm getSystemViewForm()
    {
    	return tree;
    }
	
    /**
     * Return the multiple selection mode current setting
     */
    public boolean getMultipleSelectionMode()
    {
    	return multipleSelectionMode;
    }

    // -----------------------------------------------------
    // SEMI-PRIVATE METHODS USED BY CALLING DIALOG/WIZARD...
    // -----------------------------------------------------
	/**
	 * Often the message line is null at the time of instantiation, so we have to call this after
	 *  it is created.
	 */
	public void setMessageLine(ISystemMessageLine msgLine)
	{
		this.msgLine = msgLine;
	}

	/**
	 * Return control to recieve initial focus
	 */
	public Control getInitialFocusControl()
	{
		return tree.getTreeControl();
	}	
	
	/**
	 * Show or hide the property sheet. This is called after the contents are created when the user
	 *  toggles the Details button.
	 * @param shell Use getShell() in your dialog or wizard page
	 * @param contents Use getContents() in your dialog or wizard page
	 * @return new state -> true if showing, false if hiding
	 */
	public boolean toggleShowPropertySheet(Shell shell, Control contents) 
	{
	    Point windowSize = shell.getSize();
	    Point oldSize = contents.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		if (showPropertySheet) // hiding?
		{
          ps.dispose();
          spacer1.dispose();
          spacer2.dispose();
          ps_composite.dispose();
          ps = null; spacer1 = spacer2 = null; ps_composite = null;
          ((GridLayout)outerParent.getLayout()).numColumns = 1;
		}
		else // showing?
		{
		  //createPropertySheet((Composite)contents, shell);
          ((GridLayout)outerParent.getLayout()).numColumns = 2;
		  createPropertySheet(outerParent, shell);
		}

	    Point newSize = contents.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	    shell.setSize(new Point(windowSize.x + (newSize.x - oldSize.x), windowSize.y));
	    
		if (ps != null)
		{
		  ISelection s = tree.getSelection();
		  if (s != null)
		    ps.selectionChanged(s);		  
		}
	    
		showPropertySheet = !showPropertySheet;
		return showPropertySheet;
	}
	
	/**
	 * Create the property sheet viewer
	 */
	private void createPropertySheet(Composite outerParent, Shell shell)
	{
		ps_composite = SystemWidgetHelpers.createFlushComposite(outerParent, 1);	
		((GridData)ps_composite.getLayoutData()).grabExcessVerticalSpace = true;
		((GridData)ps_composite.getLayoutData()).verticalAlignment = GridData.FILL;

        // SPACER LINES
        spacer1 = SystemWidgetHelpers.createLabel(ps_composite, "", 1);
        spacer2 = SystemWidgetHelpers.createLabel(ps_composite, "", 1);
        // PROPERTY SHEET VIEWER
        ps = new SystemPropertySheetForm(shell, ps_composite, SWT.BORDER, msgLine);			
	}
		
	public void dispose()
	{
		if (tree != null)
		{
			tree.removeSelectionChangedListener(this);	
			for (int i = 0; i < listeners.size(); i++)
			{
				tree.removeSelectionChangedListener((ISelectionChangedListener)listeners.get(i));	
			}
		}	
	}	
	/**
	 * In this method, we populate the given SWT container with widgets and return the container
	 *  to the caller. 
	 * @param parent The parent composite
	 */
	public Control createContents(Shell shell, Composite parent)
	{
		contentsCreated = true;
		
		outerParent = parent;		
		// OUTER COMPOSITE
		//if (showPropertySheet)
        {
        	outerParent = SystemWidgetHelpers.createComposite(parent, showPropertySheet ? 2 : 1);        	
        }

		// INNER COMPOSITE
		int gridColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createFlushComposite(outerParent, gridColumns);	

        // PROPERTY SHEET COMPOSITE
		if (showPropertySheet)
		{
			createPropertySheet(outerParent, shell);
		}
		else
		{
            //((GridLayout)composite_prompts.getLayout()).margin...
		}

        // MESSAGE/VERBAGE TEXT AT TOP
        verbageLabel = (Label) SystemWidgetHelpers.createVerbiage(composite_prompts, verbage, gridColumns, false, PROMPT_WIDTH);
        //verbageLabel = SystemWidgetHelpers.createLabel(composite_prompts, verbage, gridColumns);

        // SPACER LINE
        SystemWidgetHelpers.createLabel(composite_prompts, "", gridColumns);
       
        // LOCATION PROMPT
        if (showLocationPrompt)
        {
        	SystemWidgetHelpers.createLabel(composite_prompts, locationPrompt, gridColumns);
        }
        
        // SELECT OBJECT READONLY TEXT FIELD
        Composite nameComposite = composite_prompts;
        int       nameSpan = gridColumns;
	    nameEntryValue = SystemWidgetHelpers.createReadonlyTextField(nameComposite);
		((GridData)nameEntryValue.getLayoutData()).horizontalSpan = nameSpan;
		
		// create an array of viewer filters from our list of viewer filters
		ViewerFilter[] initViewerFilters = null;
		
		// if we don't have a viewer filter list, then create an empty array
		if (viewerFilters == null) {
			initViewerFilters = new ViewerFilter[0];
		}
		// otherwise copy from list to array
		else {
			initViewerFilters = new ViewerFilter[viewerFilters.size()];
			
			Iterator iter = viewerFilters.iterator();
			
			int idx = 0;
			
			// copy from list to array
			while (iter.hasNext()) {
				ViewerFilter filter = (ViewerFilter)(iter.next());
				initViewerFilters[idx] = filter;
				idx++;
			}
		}

		// TREE        
		tree = new SystemViewForm(shell, composite_prompts, SWT.NULL, inputProvider, !multipleSelectionMode, msgLine, gridColumns, 1, initViewerFilters);
		
		if (treeTip != null)
		  //tree.setToolTipText(treeTip); //EXTREMELY ANNOYING!
		if (autoExpandDepth != 0)
		{
		  tree.getSystemView().setAutoExpandLevel(autoExpandDepth);
		  tree.reset(inputProvider);
		}

        // initialize fields
	    if (!initDone)
	      doInitializeFields();		  		

	    // add selection listeners
		tree.addSelectionChangedListener(this);				  
		if (listeners.size() > 0)
		  for (int idx=0; idx<listeners.size(); idx++)
		     tree.addSelectionChangedListener((ISelectionChangedListener)listeners.elementAt(idx));

		// pre-select default root folder filter...
		if (preSelectFilter != null)
		{
		  tree.select(preSelectFilter, true);
		  Object preSelectFilterChildObject = inputProvider.getPreSelectFilterChildObject();
		  if (preSelectFilterChildObject != null)
		    tree.select(preSelectFilterChildObject, false);
		}		  
        
		return composite_prompts;
	}

	/**
	 * Completes processing of the wizard page or dialog. If this 
	 * method returns true, the wizard/dialog will close; 
	 * otherwise, it will stay active.
	 *
	 * @return true if no errors
	 */
	public boolean verify() 
	{
		msgLine.clearErrorMessage();    		
        outputConnection = internalGetConnection();
		return true;
	}


    // -----------------------------------------------------
    // PRIVATE METHODS USED BY US...
    // -----------------------------------------------------
    
    /**
     * Return the current connection
     */
    protected IHost internalGetConnection()
    {
    	Object parent = tree.getSystemView().getRootParent();
    	if (parent instanceof IHost)
    	{
    		return (IHost)parent;
    	}
    	return null;
    }
    
    protected void setNameText(String text)
    {
    	nameEntryValue.setText(text);
    }
    private String getNameText()
    {
    	return nameEntryValue.getText();
    }
    private void doInitializeFields()
	{
		  setPageComplete();
		  initDone = true;
		  return; 
	}
	
    /**
     * Returns the implementation of ISystemRemoteElement for the given
     * object.  Returns null if this object does not adaptable to this.
     */
    private ISystemRemoteElementAdapter getRemoteAdapter(Object o) 
    {
    	return SystemAdapterHelpers.getRemoteAdapter(o);
    }
		
	// ---------------------------------------------------
	// METHODS FOR SELECTION CHANGED LISTENER INTERFACE... 
	// ---------------------------------------------------
	/**
	 * User selected something in the tree.
	 */
	public void selectionChanged(SelectionChangedEvent e)
	{
		valid = true;
		ISelection selection = e.getSelection();
			
		if (ps != null)
		  ps.selectionChanged(selection);
		  				
		outputObjects = null;
		int selectionSize = ((IStructuredSelection)selection).size();
		if ((selectionSize > 1) && !tree.sameParent() && !allowForMultipleParents)
		{
			clearErrorMessage();
			setNameText("");
			setPageComplete();
		    return; // don't enable OK/Add if selections from different parents
		}
		
        Object errMsg = null;
		Object selectedObject = getFirstSelection(selection);
		if (selectedObject == previousSelection && selectionSize == 1)
		{
			// DKM we null set this before, so we need to reset it
			outputObjects = getSelections(selection);
			return;	
		}
		clearErrorMessage();
		setNameText("");
		setPageComplete();
		previousSelection = selectedObject;  
		if (selectedObject != null)
		{

		  ISystemRemoteElementAdapter remoteAdapter = getRemoteAdapter(selectedObject);
		  if (remoteAdapter != null)
		  {
			setNameTextFromSelection(selection, selectedObject, remoteAdapter);
			
		  	outputConnection = internalGetConnection();
		  	if ((addButtonCallback != null) && (selectedObject instanceof IRemoteFile))
		  	{
		  	  errMsg = addButtonCallback.okToEnableAddButton(outputConnection, (IRemoteFile[])getSelections(selection));

		  	  if (errMsg != null)
		  	  {
		  	  	if (errMsg instanceof String)
		  		  setErrorMessage((String)errMsg);
		  		else
		  		  setErrorMessage((SystemMessage)errMsg);
		  	  }		     
		  	}
		  	else if ((objectMatcher == null) || objectMatcher.appliesTo(remoteAdapter, selectedObject))
		  	{
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
		  	// if we're in file mode and folder selection is not allowed, then mark as invlaid selection
		  	else if (fileMode && !allowFolderSelection) {
		  	    
		  	    if (remoteAdapter.getRemoteType(selectedObject).equals(ISystemFileRemoteTypes.TYPE_FOLDER)) {
		  	        valid = false;
		  	        setPageComplete();
		  	    }
		  	}
		  }	
		}
	}

	protected ISystemRemoteElementAdapter[] getRemoteAdapters(ISelection selection)
	{
		Object[] selectedObjects = getSelections(selection);
		ISystemRemoteElementAdapter[] adapters = new ISystemRemoteElementAdapter[selectedObjects.length];
		for (int idx=0; idx<adapters.length; idx++)
		{
			adapters[idx] = getRemoteAdapter(selectedObjects[idx]);
		}
		return adapters;
	}
	
	
	private void setNameTextFromSelection(ISelection selection, Object selectedObject,ISystemRemoteElementAdapter remoteAdapter)
	{
		setNameText(remoteAdapter.getAbsoluteName(selectedObject));
		outputObjects = getSelections(selection);
		setPageComplete();
	}

	/**
	 * Return first item currently selected.
	 */
	protected Object getFirstSelection(ISelection selection)
	{
		IStructuredSelection sSelection = (IStructuredSelection)selection;
		if (sSelection != null)
		{
	      Iterator selectionIterator = sSelection.iterator();
	      if (selectionIterator.hasNext())
	        return selectionIterator.next();
	      else
	        return null;
		}		
		return null;
	}	
	/**
	 * Return all items currently selected.
	 */
	protected Object[] getSelections(ISelection selection)
	{
		IStructuredSelection sSelection = (IStructuredSelection)selection;
		if (sSelection != null)
		{
		  Object[] selectedObjects = new Object[sSelection.size()]; 
	      Iterator selectionIterator = sSelection.iterator();
	      int idx = 0;
	      while (selectionIterator.hasNext())
	      	selectedObjects[idx++] = selectionIterator.next();
	      return selectedObjects;
		}		
		return null;
	}	
	
	/**
	 * This method can be called by the dialog or wizard page host, to decide whether to enable
	 * or disable the next, final or ok buttons. It returns true if the minimal information is
	 * available and is correct.
	 */
	public boolean isPageComplete()
	{
		return ( (getNameText().length() > 0) ) && valid;
	}
	
	/**
	 * Inform caller of page-complete status of this form
	 */
	public void setPageComplete()
	{
		if (callerInstanceOfWizardPage)
		{
		  ((WizardPage)caller).setPageComplete(isPageComplete());
		}
		else if (callerInstanceOfSystemPromptDialog)
		{
		  ((SystemPromptDialog)caller).setPageComplete(isPageComplete());
		}		
	}


    protected void clearErrorMessage()
    {
    	if (msgLine != null)
    	  msgLine.clearErrorMessage();
    }
    protected void setErrorMessage(String msg)
    {
    	if (msgLine != null)
    	  if (msg != null)
    	    msgLine.setErrorMessage(msg);
    	  else
    	    msgLine.clearErrorMessage();
    }
    protected void setErrorMessage(SystemMessage msg)
    {
    	if (msgLine != null)
    	  if (msg != null)
    	    msgLine.setErrorMessage(msg);
    	  else
    	    msgLine.clearErrorMessage();
    }

	/**
	 * Return shell of parent dialog or wizard
	 */
	protected Shell getShell()
	{
		if (callerInstanceOfWizardPage)
		  return ((WizardPage)caller).getShell();
		else if (callerInstanceOfSystemPromptDialog)
		  return ((SystemPromptDialog)caller).getShell();
		else
		  return null;
	}
	
	public void setShowLocationPrompt(boolean show)
	{
		showLocationPrompt = show;
	}
	
	public void setLocationPrompt(String prompt)
	{
		locationPrompt = prompt;
	}
	
	/**
	 * Add viewer filter.
	 * @param filter a viewer filter.
	 * @see SystemActionViewerFilter.
	 */
	public void addViewerFilter(ViewerFilter filter) {
		viewerFilters.add(filter);
	}
	
	/**
	 * Sets whether to allow folder selection. The default selection validator will use this to
	 * determine whether the OK button will be enabled when a folder is selected. The default
	 * is <code>true</code>. This call only makes sense if the form is in file selection mode.
	 * @param allow <code>true</code> to allow folder selection, <code>false</code> otherwise.
	 */
	public void setAllowFolderSelection(boolean allow) {
	    allowFolderSelection = allow;
	}
}