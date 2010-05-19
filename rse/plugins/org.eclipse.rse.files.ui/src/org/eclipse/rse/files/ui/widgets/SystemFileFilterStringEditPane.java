/********************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others. All rights reserved.
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
 * David Dykstal    (IBM)        - [148977] New Filter dialog should propose a default filter name on the 2nd page
 ********************************************************************************/

package org.eclipse.rse.files.ui.widgets;

import java.util.Arrays;
import java.util.Vector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.files.ui.Activator;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.internal.files.ui.ISystemFileConstants;
import org.eclipse.rse.internal.files.ui.actions.SystemSelectFileTypesAction;
import org.eclipse.rse.internal.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.internal.ui.actions.SystemTestFilterStringAction;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.util.ValidatorFileFilterString;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.filters.SystemFilterStringEditPane;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorPathName;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * The edit pane for page 1 of the New File Filter wizard.
 * Prompts for the details of a single file filter string.
 */
public class SystemFileFilterStringEditPane
	extends SystemFilterStringEditPane
{
	// GUI widgets
	protected Button filesOnlyCheckBox;
	protected Button subsetByFileNameRadioButton;
	protected Button subsetByFileTypesRadioButton;
	protected Button selectTypesButton;
	protected Label labelFile;
	protected Label labelTypes;
	protected Text textFile;
	protected Text textTypes;
    protected SystemRemoteFolderCombo folderCombo;
	// limits
	protected int filterFileLength    = 256;
	protected int filterPathLength    = 256;
	// validators
	protected ISystemValidator pathValidator = new ValidatorPathName();	
	protected ISystemValidator fileValidator;
	// inputs
	protected boolean  caseSensitive = false;
	//protected boolean  showTestButton = true;
	protected String[] inputFilterStrings;
	// state
	protected boolean noValidation = false;
	protected boolean callerInstanceOfWizardPage, callerInstanceOfSystemPromptDialog;	
    protected boolean skipUniquenessChecking;
    protected boolean calledFromVerify;    	
    protected boolean dontStealFocus;
	protected RemoteFileSubSystemConfiguration inputSubsystemConfiguration = null;
	
	// actions
	private SystemTestFilterStringAction testAction = null;
    private SystemSelectFileTypesAction typesAction = null;

	
	/**
	 * Constructor for SystemFileFilterStringEditPane.
	 * @param shell
	 */
	public SystemFileFilterStringEditPane(Shell shell) 
	{
		super(shell);
	}
	
	// ------------------------------
	// INPUT/CONFIGURATION METHODS...
	// ------------------------------
    /**
     * Set the contextual system filter pool reference manager provider. Will be non-null if the
     * current selection is a reference to a filter pool or filter, or a reference manager
     * provider (eg subsystem).
     * <p>
     * Intercept of parent so we can extract the isCaseSensitive() value.
     */
    public void setSystemFilterPoolReferenceManagerProvider(ISystemFilterPoolReferenceManagerProvider provider)
    {
    	super.setSystemFilterPoolReferenceManagerProvider(provider);
    	if (provider != null)
        	caseSensitive = ((ISubSystem)provider).getSubSystemConfiguration().isCaseSensitive();
    }
	/**
	 * Set the contextual system filter pool manager provider. Will be non-null if the
	 * current selection is a filter pool or filter or reference to either, or a manager
	 * provider itself (eg, subsystem factory).
	 * <p>
	 * Intercept of parent so we can extract the isCaseSensitive() value.
	 */
	public void setSystemFilterPoolManagerProvider(ISystemFilterPoolManagerProvider provider)
	{
		super.setSystemFilterPoolManagerProvider(provider);
		if (provider != null)
			caseSensitive = ((ISubSystemConfiguration)provider).isCaseSensitive();
	}
	    
	/**
	 * Call this to override the text limit for the filter name, from the default of 40.
	 */
	public void setFilterFileLength(int max)
	{
		filterFileLength = max;
		if (textFile != null)
		  textFile.setTextLimit(max);
	}
	/**
	 * Call this to override the text limit for the filter name, from the default of 40.
	 */
	public void setFilterPathLength(int max)
	{
		filterPathLength = max;
		if (folderCombo != null)
		  folderCombo.setTextLimit(max);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.filters.SystemFilterStringEditPane#setEditable(boolean)
	 */
	public void setEditable(boolean editable) {
		super.setEditable(editable);
		enable(filesOnlyCheckBox, editable);
		enable(subsetByFileNameRadioButton, editable);
		enable(selectTypesButton, editable);
		enable(labelFile, editable);
		enable(textFile, editable);
		enable(folderCombo, editable);		
		enable(subsetByFileTypesRadioButton, editable);
		enable(labelTypes, editable);
		enable(textTypes, editable);
	}
	
	/**
	 * Existing strings are used to aid in uniqueness validation.
	 */	
	public void setExistingStrings(String[] existingStrings, boolean caseSensitive)
	{
		this.inputFilterStrings = existingStrings;
		this.caseSensitive = caseSensitive;
	}		

	// ------------------------------	
	// LIFECYCLE METHODS...
	// ------------------------------

	/**
	 * Populate the pane with the GUI widgets
	 * @param parent
	 * @return Control
	 */
	public Control createContents(Composite parent) 
	{		
		int gridColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, gridColumns);	
		
        // directory prompt                     
        String historyKey = null;
        if (refProvider != null)
          historyKey = ((ISubSystem)refProvider).getSubSystemConfiguration().getId()+".filterStringDialog"; // unique to us //$NON-NLS-1$
        else
          historyKey = "files.filterStringDialog"; // unique to us //$NON-NLS-1$
          
        boolean readonly = false;
        folderCombo = SystemFileWidgetHelpers.createFolderCombo(composite_prompts, null, gridColumns, historyKey, readonly);        
        folderCombo.setShowNewConnectionPrompt(false);
        SystemWidgetHelpers.setHelp(folderCombo, RSEUIPlugin.HELPPREFIX+"ffsd0001"); //$NON-NLS-1$
        SystemWidgetHelpers.createLabel(composite_prompts," ",gridColumns); // FILLER //$NON-NLS-1$
                			
		// parent folder prompt    
		//textFolder = SystemWidgetHelpers.createLabeledTextField(composite_prompts, null, rb, RESID_FILEFILTERSTRING_FOLDER_ROOT);

        // "Subset by file name filter" radiobutton
        subsetByFileNameRadioButton = SystemWidgetHelpers.createRadioButton(composite_prompts, null, SystemFileResources.RESID_FILEFILTERSTRING_BYFILENAME_LABEL, SystemFileResources.RESID_FILEFILTERSTRING_BYFILENAME_TOOLTIP);
        //SystemWidgetHelpers.setHelp(subsetByFileNameRadioButton, RSEUIPlugin.HELPPREFIX+"ffsd0002", RSEUIPlugin.HELPPREFIX+"ffsd0003");
        SystemWidgetHelpers.setHelp(subsetByFileNameRadioButton, RSEUIPlugin.HELPPREFIX+"ffsd0002"); //$NON-NLS-1$
        updateGridData(subsetByFileNameRadioButton, gridColumns);
                
		// File name prompt    
		//textFile = SystemWidgetHelpers.createLabeledTextField(composite_prompts, null, rb, RESID_FILEFILTERSTRING_FILE_ROOT);
		String indent = "       "; //$NON-NLS-1$
		String temp = SystemWidgetHelpers.appendColon(SystemFileResources.RESID_FILEFILTERSTRING_FILE_LABEL);
		labelFile = SystemWidgetHelpers.createLabel(composite_prompts, indent+temp);
		labelFile.setToolTipText(SystemFileResources.RESID_FILEFILTERSTRING_FILE_TOOLTIP);
		textFile = SystemWidgetHelpers.createTextField(composite_prompts, null);
		textFile.setToolTipText(SystemFileResources.RESID_FILEFILTERSTRING_FILE_TOOLTIP);
        //SystemWidgetHelpers.setHelp(textFile, RSEUIPlugin.HELPPREFIX+"ffsd0003",RSEUIPlugin.HELPPREFIX+"ffsd0002");
        SystemWidgetHelpers.setHelp(textFile, RSEUIPlugin.HELPPREFIX+"ffsd0003"); //$NON-NLS-1$
        updateGridData(textFile, gridColumns-1);
		textFile.setText("*"); //$NON-NLS-1$


        // "Subset by file types filter" radiobutton
        subsetByFileTypesRadioButton = SystemWidgetHelpers.createRadioButton(composite_prompts, null, SystemFileResources.RESID_FILEFILTERSTRING_BYFILETYPES_LABEL,  SystemFileResources.RESID_FILEFILTERSTRING_BYFILETYPES_TOOLTIP);
        //SystemWidgetHelpers.setHelp(subsetByFileTypesRadioButton, RSEUIPlugin.HELPPREFIX+"ffsd0004", RSEUIPlugin.HELPPREFIX+"ffsd0005");
        SystemWidgetHelpers.setHelp(subsetByFileTypesRadioButton, RSEUIPlugin.HELPPREFIX+"ffsd0004"); //$NON-NLS-1$
        updateGridData(subsetByFileTypesRadioButton, gridColumns);

		// File types prompt 
		Composite typesGroup = SystemWidgetHelpers.createComposite(composite_prompts, 3);   
        //SystemWidgetHelpers.setHelp(typesGroup, RSEUIPlugin.HELPPREFIX+"ffsd0005",RSEUIPlugin.HELPPREFIX+"ffsd0004");
        SystemWidgetHelpers.setHelp(typesGroup, RSEUIPlugin.HELPPREFIX+"ffsd0005"); //$NON-NLS-1$
		GridLayout layout = (GridLayout)typesGroup.getLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;		
		updateGridData(typesGroup, gridColumns);
		temp = SystemWidgetHelpers.appendColon(SystemFileResources.RESID_FILEFILTERSTRING_TYPES_LABEL);
		labelTypes = SystemWidgetHelpers.createLabel(typesGroup, indent+temp);
		labelTypes.setToolTipText(SystemFileResources.RESID_FILEFILTERSTRING_TYPES_TOOLTIP);
		textTypes = SystemWidgetHelpers.createReadonlyTextField(typesGroup);
		textTypes.setToolTipText(SystemFileResources.RESID_FILEFILTERSTRING_TYPES_TOOLTIP);
        updateGridData(textTypes, 1);
		selectTypesButton = SystemWidgetHelpers.createPushButton(typesGroup, null, SystemFileResources.RESID_FILEFILTERSTRING_SELECTTYPES_LABEL, SystemFileResources.RESID_FILEFILTERSTRING_SELECTTYPES_TOOLTIP);
    	GridData data = (GridData)selectTypesButton.getLayoutData();
        data.grabExcessHorizontalSpace = false;
        data.horizontalAlignment = GridData.FILL;    	

		                				  
        /*        				  
		// Include Folders check box                     
		subdirCheckBox = SystemWidgetHelpers.createCheckBox(composite_prompts, gridColumns, null,
		                                                    rb, RESID_FILEFILTERSTRING_INCFOLDERS_ROOT);
		subdirCheckBox.addSelectionListener(this);

		// Include Files check box                     
		fileCheckBox = SystemWidgetHelpers.createCheckBox(composite_prompts, gridColumns, null,
		                                                  rb, RESID_FILEFILTERSTRING_INCFILES_ROOT);
		fileCheckBox.addSelectionListener(this);
		*/

		// Show Files Only check box                     
        SystemWidgetHelpers.createLabel(composite_prompts," ",gridColumns); // FILLER //$NON-NLS-1$
		filesOnlyCheckBox = SystemWidgetHelpers.createCheckBox(composite_prompts, gridColumns, null,
		                                                  SystemFileResources.RESID_FILEFILTERSTRING_INCFILESONLY_LABEL,  SystemFileResources.RESID_FILEFILTERSTRING_INCFILESONLY_TOOLTIP);
        SystemWidgetHelpers.setHelp(filesOnlyCheckBox, RSEUIPlugin.HELPPREFIX+"ffsd0006"); //$NON-NLS-1$

        // Test button
        /*
        if (showTestButton)
        {
          SystemWidgetHelpers.createLabel(composite_prompts," ",gridColumns); // FILLER
          SystemWidgetHelpers.createLabel(composite_prompts," ",gridColumns); // FILLER
          createTestButton(composite_prompts, RESID_FILEFILTERSTRING_TEST_ROOT);
          SystemWidgetHelpers.setHelp(testButton, RSEUIPlugin.HELPPREFIX+"ffsd0007");
          updateGridData(testButton, gridColumns);
        }
        */
		
		folderCombo.setFocus();

		if (refProvider != null)
			inputSubsystemConfiguration = (RemoteFileSubSystemConfiguration)((ISubSystem)refProvider).getSubSystemConfiguration();
		else if (provider != null)
			inputSubsystemConfiguration = (RemoteFileSubSystemConfiguration)provider;
        pathValidator = inputSubsystemConfiguration.getPathValidator();
        fileValidator = inputSubsystemConfiguration.getFileFilterStringValidator();
        if (refProvider != null)
        	folderCombo.setSystemConnection(((ISubSystem)refProvider).getHost());
        else if (inputSubsystemConfiguration != null)
        	folderCombo.setSystemTypes(inputSubsystemConfiguration.getSystemTypes());
		folderCombo.setSubSystem((IRemoteFileSubSystem)refProvider);		
		folderCombo.setTextLimit(filterPathLength);
		textFile.setTextLimit(filterFileLength);		  
		
		resetFields();						
	    doInitializeFields();		  
		
		folderCombo.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateFolderInput();
				}
			}
		);
		textFile.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateFileInput();
				}
			}
		);

        subsetByFileNameRadioButton.addSelectionListener(this);      
        subsetByFileTypesRadioButton.addSelectionListener(this);   
        selectTypesButton.addSelectionListener(this);   
		filesOnlyCheckBox.addSelectionListener(this);
		
		setEditable(editable);
		return composite_prompts;
	}
    private void updateGridData(Control widget, int gridColumns)
    {
    	GridData data = (GridData)widget.getLayoutData();
        data.horizontalSpan = gridColumns;        
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;    	
    }
	
	/**
	 * Return the control to recieve initial focus. Should be overridden if you override createContents
	 */
	public Control getInitialFocusControl()
	{
		return folderCombo.getCombo();
	}	
	
	/**
	 * Override of parent.
	 * Called after reset fields, at first create time or when input is reset to a non-null value.
	 */
	protected void doInitializeFields()
	{
		if (folderCombo == null)
		  return;
		//if (refProvider == null)
		  //RSEUIPlugin.logError("Programming Error: input subsystem is not set for SystemFileFilterStringEditPane",null);
			
		if (inputFilterString != null)
		{
		  RemoteFileFilterString rffs = new RemoteFileFilterString(inputSubsystemConfiguration, inputFilterString);
		  String defaultPath = rffs.getPath();
		  folderCombo.setText((defaultPath==null) ? "" : defaultPath); //$NON-NLS-1$
		  String defaultFile = rffs.getFile();		  
		  textFile.setText((defaultFile==null) ? "" : defaultFile); //$NON-NLS-1$
		  String defaultTypes = rffs.getTypesAsString();
		  textTypes.setText((defaultTypes==null) ? "" : defaultTypes); //$NON-NLS-1$
		  boolean defaultIncludeFilesOnly = rffs.getShowFiles() && !rffs.getShowSubDirs();
          boolean defaultSubsetByFileName = !rffs.getFilterByTypes();
          // set appropriate radio button for subset type
          subsetByFileNameRadioButton.setSelection(defaultSubsetByFileName);
          subsetByFileTypesRadioButton.setSelection(!defaultSubsetByFileName);
		  filesOnlyCheckBox.setSelection(defaultIncludeFilesOnly);
          enableFields(defaultSubsetByFileName);        
		}
	}
	/**
	 * This is called in the change filter dialog when the user selects "new", or selects another string.
	 * You must override this if you override createContents. Be sure to test if the contents have even been created yet!
	 */
	protected void resetFields()
	{
		if (folderCombo == null)
		  return;
	    folderCombo.setText(""); //$NON-NLS-1$
		textFile.setText("*"); //$NON-NLS-1$
		textTypes.setText(""); //$NON-NLS-1$
        subsetByFileNameRadioButton.setSelection(true);
        subsetByFileTypesRadioButton.setSelection(false);
		filesOnlyCheckBox.setSelection(false);
        enableFields(true);        
	}

	/**
	 * Must be overridden if createContents is overridden.
	 * <p>
	 * This is called by the isComplete, to decide if the default information
	 *  is complete enough to enable finish. It doesn't do validation, that will be done when
	 *  finish is pressed.
	 */
	protected boolean areFieldsComplete()
	{
		if (folderCombo == null)
		  return false;
		else
		{
		  boolean filterGiven = false;
		  boolean subsetByFileName = false;
		  String fileNameText = null;
		  
		  if (subsetByFileNameRadioButton.getSelection()) {
		  	fileNameText = textFile.getText().trim();
		  	filterGiven = fileNameText.length() > 0;
		  	subsetByFileName = true;
		  }
		  else {
		    filterGiven = textTypes.getText().trim().length() > 0;
		    subsetByFileName = false;
		  }

		  String folderText = folderCombo.getText().trim();
		  
		  if (inputSubsystemConfiguration != null) {
		  
			// KM: defect 53009.
			// if input subsystem factory is Unix, then we can not allow empty path	
		  	if (inputSubsystemConfiguration.isUnixStyle()) {
		  		return folderText.length() > 0 && filterGiven;
		  	}
		  	// otherwise, if it is Windows
		  	else {
		  		
		  		// check if folder path is empty
		  		if (folderText.length() == 0) {
		  			
		  			// KM: defect 53210
		  			// if folder path empty, only valid filter is subset by file name and it
		  			// must be wild card
		  			if (subsetByFileName && fileNameText != null) {
		  				return fileNameText.equals("*"); //$NON-NLS-1$
		  			}
		  			// if we are not subsetting by file name, it is not valid
		  			else {
		  				return false;
		  			}
		  		}
		  		// if folder path is not empty, we just make sure a subset is given
		  		else {
		  			return filterGiven;
		  		}
		  	}
		  }
		  // otherwise don't care about folder path
		  else {
		  	return filterGiven;
		  }
		}
	}
	/**
	 * Enable/disable fields dependent on radiobuttons
	 */
	private void enableFields(boolean byFileName)
	{
		labelTypes.setEnabled(!byFileName);
		//textTypes.setEnabled(!byFileName);
		selectTypesButton.setEnabled(!byFileName);
		labelFile.setEnabled(byFileName);
		textFile.setEnabled(byFileName);
	}

	/**
	 * Get the action to run when "Select Types..." is pressed by the user
	 */
	private SystemSelectFileTypesAction getSelectTypesAction()
	{
		if (typesAction == null)
		  typesAction = new SystemSelectFileTypesAction(selectTypesButton.getShell());
		return typesAction;
	}
		
		
	/**
	 * Completes processing of the wizard page or dialog. If this 
	 * method returns true, the wizard/dialog will close; 
	 * otherwise, it will stay active.
	 *
	 * @return error, if there is one
	 */
	public SystemMessage verify() 
	{
		errorMessage = null;
		Control controlInError = null;
		calledFromVerify = true;
		skipEventFiring = true;
		
	    errorMessage = validateFolderInput();
	    if (errorMessage != null)
	    {
	    	controlInError = folderCombo;
	    }
	    else
	    {
			if (subsetByFileNameRadioButton.getSelection()) {
			  errorMessage = validateFileInput();
			} 
			else 
			{
				if (textTypes.getText().trim().length() == 0)
				{
					errorMessage = new SimpleSystemMessage(Activator.PLUGIN_ID, 
							ISystemFileConstants.FILEMSG_ERROR_NOFILETYPES,
							IStatus.ERROR, FileResources.FILEMSG_ERROR_NOFILETYPES, FileResources.FILEMSG_ERROR_NOFILETYPES_DETAILS);
				}
			}
			controlInError = textFile;
		}
		if ((errorMessage == null) && (inputFilterStrings!=null) && !skipUniquenessChecking)
		{
			boolean notUnique = false;
			String currFilterString = getFilterString();
			if (containsFilterString(currFilterString))
			  notUnique = true;
			if (notUnique)
			{
				String msgTxt = NLS.bind(FileResources.FILEMSG_VALIDATE_FILEFILTERSTRING_NOTUNIQUE, currFilterString);
				errorMessage = new SimpleSystemMessage(Activator.PLUGIN_ID, 
						ISystemFileConstants.FILEMSG_VALIDATE_FILEFILTERSTRING_NOTUNIQUE,
						IStatus.ERROR, msgTxt);
			}
			controlInError = textFile;
		}		  

		if (errorMessage != null)
		{
			if (!dontStealFocus)
		  	  controlInError.setFocus();
		}
		else
		  folderCombo.updateHistory(true);
		  		  
		calledFromVerify = false;
		skipEventFiring = false;
		fireChangeEvent(errorMessage);
		return errorMessage;
	}
	
	/*
	 * 
	 */
	private boolean containsFilterString(String newString)
	{
		if (inputFilterStrings == null)
		  return false;
		else
		{
			for (int idx=0; idx<inputFilterStrings.length; idx++)
			{
			  if (caseSensitive)
			  {
			    if (inputFilterStrings[idx].equals(newString))
			      return true;
			  }
			  else
			  {
			    if (inputFilterStrings[idx].equalsIgnoreCase(newString))
			      return true;			  	
			  }
			}   
		}
		return false;
	}

	// ---------------------------------------------
	// METHODS FOR VERIFYING INPUT PER KEYSTROKE ...
	// ---------------------------------------------
  	/**
	 * This hook method is called whenever the text changes in the input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 */
	protected SystemMessage validateFileInput() 
	{			
		if (noValidation || ignoreChanges)
		  return null;
		  
	    errorMessage = null;			
		
		// first validate file name
		if (fileValidator != null)
		{
		  if (fileValidator instanceof ValidatorFileFilterString)
		  {
		  	ValidatorFileFilterString fv = (ValidatorFileFilterString)fileValidator;
		    fv.setIsFileName(true);
		  }
		  
	      errorMessage = fileValidator.validate(textFile.getText().trim());		
		}
		
		// if there is no error message, then validate that folder name is correct
		// this fires a change event
		if (errorMessage == null) {
			errorMessage = validateFolderInput();	
		}
		// otherwise, simply fire change event
		// Bug 142185: fire an event with null to erase any previously shown and saved error message
		else {
			fireChangeEvent(null);
			fireChangeEvent(errorMessage);
		}
		
		return errorMessage;
	}
  	/**
	 * This hook method is called whenever the text changes in the input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 */
	protected SystemMessage validateFolderInput() 
	{			
		if (noValidation || ignoreChanges)
		  return null;
	    errorMessage= null;
	    
	    String folderComboText = folderCombo.getText().trim();
	    
		// first check if folder path is empty 
	    if (folderComboText.length() == 0) {
	    	
			// KM: defect 53009.
			// If the input subsystem factory is Unix, we do not allow empty folder path.
			// Note that for Windows, it is perfectly valid to have an empty folder path,
			// which indicates that the filter will resolve to show all the drives
			if (inputSubsystemConfiguration != null) {
			
				if (inputSubsystemConfiguration.isUnixStyle()) {
				
					// let error message come from path validator
					if (pathValidator != null) {
						errorMessage = pathValidator.validate(folderComboText);
					}
					// no path validator, so just use default path empty message
					else {
						errorMessage = new SimpleSystemMessage(Activator.PLUGIN_ID, 
								ISystemFileConstants.MSG_VALIDATE_PATH_EMPTY,
								IStatus.ERROR, FileResources.MSG_VALIDATE_PATH_EMPTY, FileResources.MSG_VALIDATE_PATH_EMPTY_DETAILS);
					}
				}
				// KM: defect 53210
				// for Windows, check that subset by file name is selected
				// and that it is wild card character
				else {
					
					if (!subsetByFileNameRadioButton.getSelection() || !textFile.getText().trim().equals("*")) { //$NON-NLS-1$
						
						// let error message come from path validator
						if (pathValidator != null) {
							errorMessage = pathValidator.validate(folderComboText);
						}
						// no path validator, so just use default path empty message
						else {
							errorMessage = new SimpleSystemMessage(Activator.PLUGIN_ID, 
									ISystemFileConstants.MSG_VALIDATE_PATH_EMPTY,
									IStatus.ERROR, FileResources.MSG_VALIDATE_PATH_EMPTY, FileResources.MSG_VALIDATE_PATH_EMPTY_DETAILS);
						}						
					}
				}
			}	
	    }
		// otherwise go through path validator
		else if (pathValidator != null) {
	    	errorMessage = pathValidator.validate(folderComboText);
		}

		fireChangeEvent(errorMessage);
		return errorMessage;
	}

	// ------------------------------	
	// DATA EXTRACTION METHODS
	// ------------------------------
	
	/**
	 * Get the filter string in its current form. 
	 * This should be overridden if createContents is overridden.
	 */
	public String getFilterString()
	{
		if (folderCombo == null)
		  return inputFilterString;
		String folder = folderCombo.getText().trim();
		boolean subsetByFileName = subsetByFileNameRadioButton.getSelection();
		String file = null;
		if (subsetByFileName)
		  file = textFile.getText().trim();
		else
		  file = textTypes.getText().trim();
		boolean showFilesOnly = filesOnlyCheckBox.getSelection();
		boolean showSubDirs = !showFilesOnly; //subdirCheckBox.getSelection();
        boolean showFiles = true; //fileCheckBox.getSelection();        
        RemoteFileFilterString rffs =  new RemoteFileFilterString(inputSubsystemConfiguration, folder, file);
        rffs.setShowSubDirs(showSubDirs);
        rffs.setShowFiles(showFiles);
        //System.out.println("internalGetFilterString: showSubDirs = " + showSubDirs + ", showFiles = " + showFiles);
        //System.out.println("... resulting string: " + rffs.toString());
		return rffs.toString();
	}

	// ------------------	
	// EVENT LISTENERS...
	// ------------------
	
	/**
	 * User has selected something
	 */
	public void widgetSelected(SelectionEvent event)
	{		
		Object src = event.getSource();	
		dontStealFocus = true;
		/*
		else if (src == subdirCheckBox)
	      validateFileInput();
		else if (src == fileCheckBox)
		  validateFileInput();	
		*/		
		if (src == filesOnlyCheckBox)
		{
		  verify();
		}
		else if (src == subsetByFileNameRadioButton)
		{
		  verify();
		  enableFields(true);
		  textFile.setFocus();
		}
		else if (src == subsetByFileTypesRadioButton)
		{
		  verify();
		  enableFields(false);
		  selectTypesButton.setFocus();
		}
		else if (src == selectTypesButton)
		{
    	  SystemSelectFileTypesAction typesAction = getSelectTypesAction();
          String typesString = textTypes.getText().trim();    	  
          typesAction.setTypes(typesString);
    	  typesAction.run();
    	  if (!typesAction.wasCancelled())
    	  {
    	  	 typesString = typesAction.getTypesString();
    	     textTypes.setText(typesString);
		     dontStealFocus = false;
    	     verify();
    	  }			
		}
		dontStealFocus = false;
	}

	/**
	 * Called by us or by owning dialog when common Test button is pressed
	 */
	public void processTest(Shell shell)
	{
		if (refProvider == null)
	    {
		  SystemBasePlugin.logWarning("Programming Error: input subsystem is not set");		   //$NON-NLS-1$
		  return;
		}
        skipUniquenessChecking = true;            
		if (verify() == null)
		{
		  SystemTestFilterStringAction testAction = new SystemTestFilterStringAction(getShell());
		  testAction.setSubSystem((ISubSystem)refProvider);
		  testAction.setFilterString(getFilterString());
		  try 
		  {
		      testAction.run();
		  }
		  catch (Exception exc)
		  {
		  	
		  	  SystemMessage msg = SystemMessageDialog.getExceptionMessage(getShell(), exc);
		  	  fireChangeEvent(msg);		  	  
		  }
		}
        skipUniquenessChecking = false;		
	}	
	
	// ------------------------	
	// STATIC HELPER METHODS...
	// ------------------------
		
	/**
	 * Reusable method to return a name validator for creating/update a file system filter string
	 * @param filter The system filter to which we are adding/updating a filter string.
	 * @param filterString The current filter object on updates. Can be null for new string. Used
	 *  to remove from the existing string list the current filter string.
	 */
	public static ValidatorFileFilterString getFileFilterStringValidator(ISystemFilter filter, String filterString)
	{
		String[] strings = filter.getFilterStrings();
    	Vector v = new Vector(strings.length);
    	v.addAll(Arrays.asList(strings));
    	if (filterString != null)
    	  v.removeElement(filterString);
    	IRemoteFileSubSystemConfiguration ssFactory = (IRemoteFileSubSystemConfiguration)filter.getProvider();
	    ValidatorFileFilterString validator = new ValidatorFileFilterString(ssFactory,v);		
	    return validator;
	}	
	
	/**
	 * Reusable method to return a name validator for creating/update a file system filter string.
	 * No unique checking.
	 */
	public static ValidatorFileFilterString getFileFilterStringValidator(IRemoteFileSubSystemConfiguration ssFactory)
	{
	    return new ValidatorFileFilterString(ssFactory);		
	}	
	
	/**
	 * If the file filter string is in new mode, and we have a non-Unix system, then if
	 * the folder name is empty and other fields are at default, we do not allow an implicit
	 * save through changing of filter string or pressing Ok button in the change filter pane.
	 * If in new mode for non-Unix system, and the user hasn't changed the other fields,
	 * we force user to use the Create button to create the filter string explicitly.
	 * @see org.eclipse.rse.ui.filters.SystemFilterStringEditPane#canSaveImplicitly()
	 */
	public boolean canSaveImplicitly() {
		
		// KM: defect 53009.
		// check if subsystem factory is non Unix and we're in new mode
		if (newMode && inputSubsystemConfiguration != null && !inputSubsystemConfiguration.isUnixStyle()) {
			
			// check that folder combo is empty
			String folderComboText = folderCombo.getText().trim();
			
			// if so, return false if other fields haven't changed
			// So a user is not able to save with an empty folder path without
			// explicitly pressing Create.
			// Note that if we're changing an existing filter string, a user can save
			// implicitly (i.e. without pressing Apply), by changing the filter
			// string selection in the filter string list in SystemChangeFilterPane
			// or by pressing Ok with pending changes. Same goes for a new filter
			// string for which the user has changed the subset by file name
			// or subset by file type fields. KM: defect 53210
			if (folderComboText.length() == 0 &&
				subsetByFileNameRadioButton.getSelection() &&
				textFile.getText().trim().equals("*")) { //$NON-NLS-1$
				return false;
			}
			else {
				return super.canSaveImplicitly();
			}
		}
		else {
			return super.canSaveImplicitly();
		}
	}
	
	public String getDefaultFilterName()
	{
		String defaultFilterName = ""; //$NON-NLS-1$
		if (folderCombo != null) {
			String folderName = folderCombo.getText().trim();
			if (folderName.length() > 0) {
				String separator = inputSubsystemConfiguration.getSeparator();
				if (folderName.endsWith(separator)) {
					folderName = folderName.substring(0, folderName.length() - separator.length());
				}
				int p = folderName.lastIndexOf(separator);
				p += separator.length();
				if (p < folderName.length()) {
					defaultFilterName = folderName.substring(p);					
				}
			}
		}
		return defaultFilterName;
	}
}