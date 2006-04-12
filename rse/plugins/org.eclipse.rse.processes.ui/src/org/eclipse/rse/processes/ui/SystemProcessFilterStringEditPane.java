/********************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.processes.ui;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.processes.ui.view.SystemProcessStatesContentProvider;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.processes.HostProcessFilterImpl;
import org.eclipse.rse.services.clientserver.processes.ISystemProcessRemoteConstants;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessSubSystemConfiguration;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.actions.SystemTestFilterStringAction;
import org.eclipse.rse.ui.filters.SystemFilterStringEditPane;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.validators.ValidatorIntegerInput;
import org.eclipse.rse.ui.validators.ValidatorIntegerRangeInput;
import org.eclipse.rse.ui.validators.ValidatorLongRangeInput;
import org.eclipse.rse.ui.validators.ValidatorSpecialChar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class SystemProcessFilterStringEditPane extends
		SystemFilterStringEditPane implements ISystemMessages, ISystemProcessRemoteConstants
{
	
	// GUI widgets
	
	protected Label lblStatus;
	protected CheckboxTableViewer chkStatus;	
	protected Label  lblExeName, lblUserName, lblGid;
	protected Text txtExeName, txtUserName, txtGid;
	
	protected Label lblMinVM, lblMaxVM;
	protected Text txtMinVM, txtMaxVM;
	protected Button chkBoxUnlimitedVM;
	
	// limits
	protected int gidLimit = Integer.MAX_VALUE;
	protected long vmMaxValue = Long.MAX_VALUE;
	protected int exeNameLength = 256;
	protected int userNameLength = 256;
	
	// validators
	protected ValidatorLongRangeInput vmRangeValidator = new ValidatorLongRangeInput(0, vmMaxValue);	
	protected ValidatorIntegerRangeInput gidValidator = new ValidatorIntegerRangeInput(0, gidLimit);
	protected ValidatorSpecialChar nameValidator = new ValidatorSpecialChar(" \t|", true);

	// inputs
	protected String[] inputFilterStrings;

	// state
	protected boolean noValidation = false;
	protected boolean callerInstanceOfWizardPage, callerInstanceOfSystemPromptDialog;	
    protected boolean skipUniquenessChecking;
    protected boolean calledFromVerify;    	
    protected boolean dontStealFocus;
	protected IRemoteProcessSubSystemConfiguration inputSubsystemFactory = null;
	
	// actions
	protected SystemTestFilterStringAction testAction = null;

	// constants
	protected final static int SIZING_SELECTION_WIDGET_HEIGHT = 90;
	protected final static int SIZING_SELECTION_WIDGET_WIDTH = 145;

	/**
	 * Constructor for SystemProcessFilterStringEditPane.
	 * @param shell
	 */
	public SystemProcessFilterStringEditPane(Shell shell) 
	{
		super(shell);
		((ValidatorIntegerInput)gidValidator).setBlankAllowed(true);
	}
	
	// ------------------------------
	// INPUT/CONFIGURATION METHODS...
	// ------------------------------
	    
	/**
	 * Call this to override the text limit for the filter name, from the default of 256.
	 */
	public void setExeNameLength(int max)
	{
		exeNameLength = max;
		if (txtExeName != null)
		  txtExeName.setTextLimit(max);
	}
	/**
	 * Call this to override the text limit for the filter name, from the default of 256.
	 */
	public void setUserNameLength(int max)
	{
		userNameLength = max;
		if (txtUserName != null)
		  txtUserName.setTextLimit(max);
	}
	/**
	 * Existing strings are used to aid in uniqueness validation.
	 */	
	public void setExistingStrings(String[] existingStrings)
	{
		this.inputFilterStrings = existingStrings;
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
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, 1);	
		int gridColumns = 2;
		Composite sub_prompts1 = SystemWidgetHelpers.createComposite(composite_prompts, gridColumns);	
		        
   		// Exe name prompt    
		lblExeName = SystemWidgetHelpers.createLabel(sub_prompts1, SystemProcessesResources.RESID_PROCESSFILTERSTRING_EXENAME_LABEL);
		lblExeName.setToolTipText(SystemProcessesResources.RESID_PROCESSFILTERSTRING_EXENAME_TOOLTIP);
		txtExeName = SystemWidgetHelpers.createTextField(sub_prompts1, null);
		txtExeName.setToolTipText(SystemProcessesResources.RESID_PROCESSFILTERSTRING_EXENAME_TOOLTIP);

		SystemWidgetHelpers.setHelp(txtExeName, ProcessesPlugin.HELPPREFIX+"pfsd0001");
        updateGridData(txtExeName, gridColumns-1);
		txtExeName.setText("*");

   		// User name prompt    
		lblUserName = SystemWidgetHelpers.createLabel(sub_prompts1, SystemProcessesResources.RESID_PROCESSFILTERSTRING_USERNAME_LABEL);
		lblUserName.setToolTipText(SystemProcessesResources.RESID_PROCESSFILTERSTRING_USERNAME_TOOLTIP);
		txtUserName = SystemWidgetHelpers.createTextField(sub_prompts1, null);
		txtUserName.setToolTipText(SystemProcessesResources.RESID_PROCESSFILTERSTRING_USERNAME_TOOLTIP);

		SystemWidgetHelpers.setHelp(txtUserName, ProcessesPlugin.HELPPREFIX+"pfsd0002");
        updateGridData(txtUserName, gridColumns-1);
		txtUserName.setText("*");
		
   		// Group ID prompt    
		lblGid = SystemWidgetHelpers.createLabel(sub_prompts1, SystemProcessesResources.RESID_PROCESSFILTERSTRING_GID_LABEL);
		lblGid.setToolTipText(SystemProcessesResources.RESID_PROCESSFILTERSTRING_GID_TOOLTIP);
		txtGid = SystemWidgetHelpers.createTextField(sub_prompts1, null);
		txtGid.setToolTipText(SystemProcessesResources.RESID_PROCESSFILTERSTRING_GID_TOOLTIP);

		SystemWidgetHelpers.setHelp(txtGid, ProcessesPlugin.HELPPREFIX+"pfsd0003");
        updateGridData(txtGid, gridColumns-1);
		txtGid.setText("*");
		
		// status checkbox table
		lblStatus = SystemWidgetHelpers.createLabel(sub_prompts1, SystemProcessesResources.RESID_PROCESSFILTERSTRING_STATUS_LABEL);
		lblStatus.setToolTipText(SystemProcessesResources.RESID_PROCESSFILTERSTRING_STATUS_TOOLTIP);
		chkStatus = CheckboxTableViewer.newCheckList(sub_prompts1, SWT.BORDER);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		chkStatus.getTable().setLayoutData(data);
		chkStatus.setLabelProvider(new LabelProvider());

		SystemWidgetHelpers.setHelp(chkStatus.getControl(), ProcessesPlugin.HELPPREFIX+"pfsd0004");
	    SystemWidgetHelpers.createLabel(sub_prompts1, "      ");
		addSelectionButtons(sub_prompts1);
		
		// Range prompt
		Composite subsub = SystemWidgetHelpers.createComposite(sub_prompts1, gridColumns, 3, false, null, -1, -1);
		lblMinVM = SystemWidgetHelpers.createLabel(subsub, SystemProcessesResources.RESID_PROCESSFILTERSTRING_MINVM_LABEL);
		lblMinVM.setToolTipText(SystemProcessesResources.RESID_PROCESSFILTERSTRING_MINVM_TOOLTIP);
		txtMinVM = SystemWidgetHelpers.createTextField(subsub, null);
		txtMinVM.setToolTipText(SystemProcessesResources.RESID_PROCESSFILTERSTRING_MINVM_TOOLTIP);

		SystemWidgetHelpers.setHelp(txtMinVM, ProcessesPlugin.HELPPREFIX+"pfsd0005");
		txtMinVM.setText("0");
		SystemWidgetHelpers.createLabel(subsub, "      ");
		
		lblMaxVM = SystemWidgetHelpers.createLabel(subsub, SystemProcessesResources.RESID_PROCESSFILTERSTRING_MAXVM_LABEL);
		lblMaxVM.setToolTipText(SystemProcessesResources.RESID_PROCESSFILTERSTRING_MAXVM_TOOLTIP);
		txtMaxVM = SystemWidgetHelpers.createTextField(subsub, null);
		txtMaxVM.setToolTipText(SystemProcessesResources.RESID_PROCESSFILTERSTRING_MAXVM_TOOLTIP);

		SystemWidgetHelpers.setHelp(txtMaxVM, ProcessesPlugin.HELPPREFIX+"pfsd0006");
        txtMaxVM.setEnabled(false);

		// Unlimited check box                     
		chkBoxUnlimitedVM = SystemWidgetHelpers.createCheckBox(subsub, 1, null,
		                                                  SystemProcessesResources.RESID_PROCESSFILTERSTRING_UNLIMITED_LABEL,  SystemProcessesResources.RESID_PROCESSFILTERSTRING_UNLIMITED_TOOLTIP);
        SystemWidgetHelpers.setHelp(chkBoxUnlimitedVM, ProcessesPlugin.HELPPREFIX+"pfsd0007");

        txtExeName.setFocus();

		if (refProvider != null)
			inputSubsystemFactory = (IRemoteProcessSubSystemConfiguration)((ISubSystem)refProvider).getSubSystemConfiguration();
		else if (provider != null)
			inputSubsystemFactory = (IRemoteProcessSubSystemConfiguration)provider;
		IStructuredContentProvider p = new SystemProcessStatesContentProvider();
		
		chkStatus.setContentProvider(p);
		chkStatus.setInput(p.getElements(null));
		
		txtExeName.setTextLimit(exeNameLength);
		txtUserName.setTextLimit(userNameLength);		  
		
		resetFields();						
	    doInitializeFields();		  
		
		txtExeName.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateNameInput(txtExeName);
				}
			}
		);
		txtUserName.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateNameInput(txtUserName);
				}
			}
		);
		txtGid.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateGidInput();
				}
			}
		);
		txtMinVM.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (validateMinVMInput() == null)
					{
						if (!chkBoxUnlimitedVM.getSelection() && !txtMaxVM.getText().trim().equals(""))
						{
							SystemMessage message = validateMinLessThanMax();
							fireChangeEvent(message);
						}
					}
				}
			}
		);
		txtMaxVM.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (validateMaxVMInput() == null)
					{
						if (!txtMinVM.getText().trim().equals(""))
						{
							SystemMessage message = validateMinLessThanMax();
							fireChangeEvent(message);
						}
					}
				}
			}
		);
		
		chkBoxUnlimitedVM.addSelectionListener(this);
		
		return composite_prompts;
	}
	
	/**
	 * Add the selection and deselection buttons to the dialog.
	 * @param composite org.eclipse.swt.widgets.Composite
	 */
	private void addSelectionButtons(Composite composite) 
	{
		Composite buttonComposite = new Composite(composite, SWT.RIGHT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);
		GridData data =
			new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		composite.setData(data);

		//Button selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID, WorkbenchMessages.getString("WizardTransferPage.selectAll"), false); //$NON-NLS-1$
		Button selectButton = SystemWidgetHelpers.createPushButton(buttonComposite, null, 
		                                   SystemResources.RESID_SELECTFILES_SELECTALL_BUTTON_ROOT_LABEL, SystemResources.RESID_SELECTFILES_SELECTALL_BUTTON_ROOT_TOOLTIP); 

		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				chkStatus.setAllChecked(true);
			}
		};
		selectButton.addSelectionListener(listener);


		//Button deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID, WorkbenchMessages.getString("WizardTransferPage.deselectAll"), false); //$NON-NLS-1$
		Button deselectButton = SystemWidgetHelpers.createPushButton(buttonComposite, null, 
		                                     SystemResources.RESID_SELECTFILES_DESELECTALL_BUTTON_ROOT_LABEL, SystemResources.RESID_SELECTFILES_DESELECTALL_BUTTON_ROOT_TOOLTIP); 
		
		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				chkStatus.setAllChecked(false);
			}
		};
		deselectButton.addSelectionListener(listener);
	}
	
    private void updateGridData(Control widget, int gridColumns)
    {
    	GridData data = (GridData)widget.getLayoutData();
        data.horizontalSpan = gridColumns;        
        data.grabExcessHorizontalSpace = false;
        data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;    	
    }
	
	/**
	 * Return the control to recieve initial focus. Should be overridden if you override createContents
	 */
	public Control getInitialFocusControl()
	{
		return txtExeName;
	}	
	
	/**
	 * Override of parent.
	 * Called after reset fields, at first create time or when input is reset to a non-null value.
	 */
	protected void doInitializeFields()
	{
		if (txtExeName == null) return;
			
		if (inputFilterString != null)
		{
			HostProcessFilterImpl rffs = new HostProcessFilterImpl(inputFilterString);
			String defaultExeName = rffs.getName();
			txtExeName.setText((defaultExeName==null) ? "" : defaultExeName);
			String defaultUserName = rffs.getUsername();		  
			txtUserName.setText((defaultUserName==null) ? "" : defaultUserName);
			
			String defaultGid = rffs.getGid();
			txtGid.setText((defaultGid==null) ? "" : defaultGid);
			String defaultMinVM = rffs.getMinVM();		  
			txtMinVM.setText((defaultMinVM==null) ? "" : defaultMinVM);
			String defaultMaxVM = rffs.getMaxVM();		  
			if (defaultMaxVM.equals("-1") || defaultMaxVM == null)
			{
				txtMaxVM.setText("");
				txtMaxVM.setEnabled(false);
				chkBoxUnlimitedVM.setEnabled(true);
				chkBoxUnlimitedVM.setSelection(true);
			}
			else
			{
				txtMaxVM.setEnabled(true);
				chkBoxUnlimitedVM.setEnabled(false);
				txtMaxVM.setText(defaultMaxVM);
			}
			  
			chkStatus.setAllChecked(rffs.getAnyStatus());
						
			String[] stateTypes = SystemProcessStatesContentProvider.getStates();
			for (int i = 0; i < ALL_STATES_STR.length; i++)
			{
				chkStatus.setChecked(stateTypes[i], rffs.getSpecificState(ALL_STATES_STR[i]));
			}
		}
	}
	/**
	 * This is called in the change filter dialog when the user selects "new", or selects another string.
	 * You must override this if you override createContents. Be sure to test if the contents have even been created yet!
	 */
	protected void resetFields()
	{
		if (txtExeName == null)
		  return;
	    txtExeName.setText("*");
		txtUserName.setText("*");
		txtGid.setText("");
		txtMinVM.setText("0");
		txtMaxVM.setText("");
		chkBoxUnlimitedVM.setSelection(true);
		txtMaxVM.setEnabled(false);
		chkBoxUnlimitedVM.setEnabled(true);
		chkStatus.setAllChecked(false);
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
		if (txtExeName == null) return false;
		else return true;
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
		
	    errorMessage = validateNameInput(txtExeName);
	    if (errorMessage != null)
		  controlInError = txtExeName;
	    
	    errorMessage = validateNameInput(txtUserName);
	    if (errorMessage != null)
		  controlInError = txtUserName;

	    errorMessage = validateGidInput();
	    if (errorMessage != null)
		  controlInError = txtGid;
	    
	    errorMessage = validateMinVMInput();
	    if (errorMessage != null)
		  controlInError = txtMinVM;
	    
		if (errorMessage == null)
		{
			if (!chkBoxUnlimitedVM.getSelection())
			{				
			  errorMessage = validateMaxVMInput();
			  if (errorMessage == null)
			  {
				  errorMessage = validateMinLessThanMax();
			  }
			}
			if (errorMessage != null)
			  controlInError = txtMaxVM;
			
		}
		if ((errorMessage == null) && (inputFilterStrings!=null) && !skipUniquenessChecking)
		{
			boolean notUnique = false;
			String currFilterString = getFilterString();
			if (containsFilterString(currFilterString))
			  notUnique = true;
			if (notUnique)
			{
			  errorMessage = RSEUIPlugin.getPluginMessage(FILEMSG_VALIDATE_FILEFILTERSTRING_NOTUNIQUE).makeSubstitution(currFilterString);
			}
			controlInError = txtExeName;
		}

		if (errorMessage != null)
		{
			if (!dontStealFocus)
		  	  controlInError.setFocus();
		}
		  		  
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
			  if (inputFilterStrings[idx].equals(newString))
			    return true;
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
	protected SystemMessage validateNameInput(Text txt) 
	{			
		if (noValidation || ignoreChanges)
		  return null;
		  
	    errorMessage = null;			
		
		if (nameValidator != null)
		{ 
	      errorMessage = nameValidator.validate(txt.getText().trim());		
		}
		
		fireChangeEvent(errorMessage);
		return errorMessage;
	}	

  	/**
	 * This hook method is called whenever the text changes in the input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 */
	protected SystemMessage validateGidInput() 
	{
		if (noValidation || ignoreChanges)
			  return null;
			  
		    errorMessage = null;			
			
			if (gidValidator != null)
			{ 
		      errorMessage = gidValidator.validate(txtGid.getText().trim());		
			}
			
			fireChangeEvent(errorMessage);
			return errorMessage;
	}
	
  	/**
	 * This hook method is called whenever the text changes in the input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 */
	protected SystemMessage validateMinVMInput() 
	{	
		if (noValidation || ignoreChanges)
			  return null;
			  
		    errorMessage = null;			
			
			if (vmRangeValidator != null)
			{
		      errorMessage = vmRangeValidator.validate(txtMinVM.getText().trim());		
			}
			
			fireChangeEvent(errorMessage);
			return errorMessage;
	}
	
  	/**
	 * This hook method is called whenever the text changes in the input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 */
	protected SystemMessage validateMaxVMInput() 
	{	
		if (noValidation || ignoreChanges || chkBoxUnlimitedVM.getSelection())
			  return null;
			  
		    errorMessage = null;			
			
			if (vmRangeValidator != null)
			{ 
		      errorMessage = vmRangeValidator.validate(txtMaxVM.getText().trim());		
			}
			
			fireChangeEvent(errorMessage);
			return errorMessage;
	}
	
  	/**
	 * This hook method is called whenever the text changes in the input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 */
	protected SystemMessage validateMinLessThanMax() 
	{
		  long minVM = 0;
		  long maxVM = 0;
		  try
		  {
			  minVM = Long.parseLong(txtMinVM.getText());
			  maxVM = Long.parseLong(txtMaxVM.getText());
		  }
		  catch (Exception e)
		  {
			  return null;
		  }
		  if (maxVM < minVM)
		  {
			  return ProcessesPlugin.getPluginMessage("RSEPG1001");
		  }
		  return null;
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
		if (txtExeName == null)
		  return inputFilterString;
		
		HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
		
		String exeName = txtExeName.getText().trim();
		if (!exeName.equals("")) rpfs.setName(exeName);

		String userName = txtUserName.getText().trim();
		if (!userName.equals("")) rpfs.setUsername(userName);

		String gid = txtGid.getText().trim();
		if (!gid.equals("")) rpfs.setGid(gid);

		String minVM = txtMinVM.getText().trim();
		if (!minVM.equals("")) rpfs.setMinVM(minVM);

		if (chkBoxUnlimitedVM.getSelection())
		{
			rpfs.setMaxVM("-1");
		}
		else
		{
			String maxVM = txtMaxVM.getText().trim();
			if (!maxVM.equals("")) rpfs.setMaxVM(maxVM);
		}
		
		String[] stateStrings = SystemProcessStatesContentProvider.getStates();
		for (int i = 0; i < ALL_STATES_STR.length; i++)
		{
			if (chkStatus.getChecked(stateStrings[i])) rpfs.setSpecificState(ALL_STATES_STR[i]);
		}
		return rpfs.toString();
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
		if (src == chkBoxUnlimitedVM)
		{
		  txtMaxVM.setEnabled(!chkBoxUnlimitedVM.getSelection());
		  if (!chkBoxUnlimitedVM.getSelection())
		  {
			  txtMaxVM.setFocus();
			  if (validateMaxVMInput() == null)
				{
					if (!txtMinVM.getText().trim().equals(""))
					{
						SystemMessage message = validateMinLessThanMax();
						fireChangeEvent(message);
					}
				}
		  }
		  else verify();
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
		  SystemBasePlugin.logWarning("Programming Error: input subsystem is not set");		  
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
}