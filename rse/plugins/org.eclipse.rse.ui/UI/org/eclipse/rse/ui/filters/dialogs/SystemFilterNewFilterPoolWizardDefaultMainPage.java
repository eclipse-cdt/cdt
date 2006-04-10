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
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.filters.SystemFilterPoolDialogOutputs;
import org.eclipse.rse.ui.filters.actions.SystemFilterAbstractFilterPoolAction;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorFilterPoolName;
import org.eclipse.rse.ui.wizards.AbstractSystemWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;


/**
 * Default main page of the "New Filter Pool" wizard.
 * This page asks for the name of the connection pool.
 * Also allows user to select from a list of filter pool managers
 * to put the filter pool into.
 */

public class SystemFilterNewFilterPoolWizardDefaultMainPage 
	   extends AbstractSystemWizardPage
	   implements SystemFilterNewFilterPoolWizardMainPageInterface, Listener, ISystemMessages,  SelectionListener
{
	protected Label labelName, labelMgr;
	protected Text  textName;
	protected Combo mgrCombo;
	protected int   mgrSelection = 0;
    protected String[] mgrNames = null;
    protected ISystemValidator[] validatorsByManager = null;
    protected SystemFilterAbstractFilterPoolAction parentWizardAction = null;
	    
	/**
	 * Constructor. 
	 */
	public SystemFilterNewFilterPoolWizardDefaultMainPage(Wizard wizard,
													String title,
													String description)
	{
		super(wizard, "NewFilterPool", title, description);
		this.parentWizardAction = ((SystemFilterNewFilterPoolWizard)wizard).getFilterPoolDialogActionCaller();
	}
    
	/**
	 * Call this to specify a validator for the pool name. It will be called per keystroke.
	 * Only call this if you do not call setFilterPoolManagers! 
	 */
	public void setNameValidator(ISystemValidator v)
	{
		validatorsByManager = new ISystemValidator[1];
		validatorsByManager[0] = v;
	}
	/**
	 * Even if you call setFilterPoolManagers and you really want your own validators,
	 * then call this. Otherwise, FolderNameValidator will be called for you.
	 * The input must be an array of validators that is the same length as the array
	 * of filter pool managers. Call this AFTER setFilterPoolManagers!
	 */
	public void setNameValidators(ISystemValidator[] v)
	{
		validatorsByManager = v;
	}

	/**
	 * Call this to specify the list of filter pool managers to allow the user to select from.
	 * Either call this or override getFilterPoolManagerNames, or leave null and this prompt will
	 * not show.
	 */
	public void setFilterPoolManagers(ISystemFilterPoolManager[] mgrs)
	{
		mgrNames = new String[mgrs.length];
		validatorsByManager = new ISystemValidator[mgrNames.length];
		for (int idx=0; idx<mgrs.length; idx++)
		{
		   mgrNames[idx] = mgrs[idx].getName();
		   ISystemValidator iiv = new ValidatorFilterPoolName(mgrs[idx].getSystemFilterPoolNamesVector());		   
		   validatorsByManager[idx] = iiv;
		}
	}
	/**
	 * Returns array of manager names to show in combo box.
	 */
	protected String[] getFilterPoolManagerNames() 
	{
       return mgrNames;
	}
	/**
	 * Set the zero-based index of the manager name to preselect.
	 * The default is zero.
	 * Either call this or override getFilterPoolManagerNameSelectionIndex.
	 */
	public void setFilterPoolManagerNameSelectionIndex(int index)
	{
       this.mgrSelection = index;
       //System.out.println("inside setFilterPoolManagerNameSelectionIndex in main page: " + index);
	}
	/**
	 * Returns the zero-based index of the manager name to preselect.
	 * Returns what was set in setFilterPoolManagerNameSelectionIndex by default.
	 */
	protected int getFilterPoolManagerNameSelectionIndex()
	{
       return mgrSelection;
	}
	
	/**
	 * Retrieve the pool name entry field prompt text. 
	 * By default, uses what we set in setPoolNamePromptText,
	 * or uses a supplied default if that is null.
	 */
	protected String getPoolNamePromptText()
	{
		String namePromptText = parentWizardAction.getDialogFilterPoolNamePrompt();
		return (namePromptText==null) ? SystemResources.RESID_FILTERPOOLNAME_LABEL : namePromptText;
	}
	/**
	 * Retrieve the pool name entry field tooltip text. 
	 * By default, uses what we set in setPoolNameToolTip,
	 * or uses a supplied default if that is null.
	 */
	protected String getPoolNameToolTip()
	{
		String nameTip = parentWizardAction.getDialogFilterPoolNameTip();
		return (nameTip==null) ? SystemResources.RESID_FILTERPOOLNAME_TIP : nameTip;
	}
	/**
	 * Retrieve the pool manager combo prompt text. 
	 * By default, uses what we set in setPoolManagerPromptText,
	 * or uses a supplied default if that is null.
	 */
	protected String getPoolManagerPromptText()
	{
		String mgrPromptText = parentWizardAction.getDialogFilterPoolManagerNamePrompt();
		return (mgrPromptText == null) ? SystemResources.RESID_FILTERPOOLMANAGERNAME_LABEL : mgrPromptText;
	}
	/**
	 * Retrieve the pool manager combo field tooltip text. 
	 * By default, uses what we set in setPoolManagerToolTip,
	 * or uses a supplied default if that is null.
	 */
	protected String getPoolManagerToolTip()
	{
		String mgrTip = parentWizardAction.getDialogFilterPoolManagerNameTip();
		return (mgrTip == null) ? SystemResources.RESID_FILTERPOOLMANAGERNAME_TIP : mgrTip;
	}

	/**
	 * CreateContents is the one method that must be overridden from the parent class.
	 * In this method, we populate an SWT container with widgets and return the container
	 *  to the caller (JFace). This is used as the contents of this page.
	 * @param parent The parent composite
	 */
	public Control createContents(Composite parent)
	{
  	    // top level composite
		Composite composite = new Composite(parent,SWT.NONE);        
		composite.setLayout(new GridLayout());
	    composite.setLayoutData(new GridData(
		   GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		// Inner composite
		Composite composite_prompts = SystemWidgetHelpers.createComposite(
			composite, 2);	

		// POOLNAME PROMPT
		labelName = SystemWidgetHelpers.createLabel(composite_prompts, getPoolNamePromptText());
		textName  = SystemWidgetHelpers.createTextField(
			composite_prompts,this);
	    String tip = getPoolNameToolTip();
	    if (tip != null)
	      textName.setToolTipText(tip);
			   
		// POOL MANAGER PROMPT
		String[] mgrs = getFilterPoolManagerNames();
		if (mgrs != null)
		{
		  labelMgr = SystemWidgetHelpers.createLabel(composite_prompts, getPoolManagerPromptText());
		  mgrCombo  = SystemWidgetHelpers.createReadonlyCombo(composite_prompts,this);
	      tip = getPoolManagerToolTip();
	      if (tip != null)
	        mgrCombo.setToolTipText(tip);
	      mgrCombo.setItems(mgrs);
          //System.out.println("inside createContents in main page. Selecting " + mgrSelection);	      
	      mgrCombo.select(mgrSelection);
	      setPoolNameTextLimit(mgrSelection);
		}
	            
		textName.setFocus();
		  		  
		// add keystroke listeners...
		textName.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateNameInput();
				}
			}
		);
		if (mgrCombo != null)
		  mgrCombo.addSelectionListener(this);		
		  
		setPageComplete(false);
		return composite;
	}
	
	/**
	 * Return control to recieve initial focus
	 */
	public Control getInitialFocusControl()
	{
		return textName;
	}	
	
	// --------------------------------- //
	// METHODS FOR LISTENER INTERFACE... 
	// --------------------------------- //
	/**
	 * Combo selection listener method
	 */
	public void widgetDefaultSelected(SelectionEvent event)
	{
	}
	/**
	 * Combo selection listener method
	 */
	public void widgetSelected(SelectionEvent event)
	{
		Object src = event.getSource();
		if (src == mgrCombo)
		{
	      setPoolNameTextLimit(mgrCombo.getSelectionIndex());
		}
	}
	/**
	 * Handles events generated by controls on this page.
	 */
	public void handleEvent(Event e)
	{
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
		SystemMessage errMsg = null;
		Control controlInError = null;
		clearErrorMessage();
		errMsg = validateNameInput();
		if (errMsg != null)
		  controlInError = textName;
		if (errMsg != null)
		  controlInError.setFocus();
		return (errMsg == null);		
	}
 
 	// ---------------------------------------------
	// METHODS FOR VERIFYING INPUT PER KEYSTROKE ...
	// ---------------------------------------------
  	/**
	 * This hook method is called whenever the text changes in the input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 * @see #setNameValidator(ISystemValidator)
	 */
	protected SystemMessage validateNameInput() 
	{			
		int mgrIndex = 0;
		if (mgrCombo != null)
		  mgrIndex = mgrCombo.getSelectionIndex();
		if (mgrIndex < 0)
		  mgrIndex = 0;
		ISystemValidator iiv = validatorsByManager[mgrIndex];
	    SystemMessage errorMessage= null;
		if (iiv != null)
	      errorMessage= iiv.validate(textName.getText());
	    if (errorMessage != null)
          setErrorMessage(errorMessage);	      
        else
          clearErrorMessage();
    	setPageComplete(errorMessage == null);
		return errorMessage;		
	}
	
	/**
	 * Set the name length for the filter pool based on the
	 *  currently selected manager
	 */
	protected void setPoolNameTextLimit(int mgrIndex)
	{
		if (mgrIndex < 0)
		  return;
		ISystemValidator iiv = validatorsByManager[mgrIndex];
		if (iiv != null)
		{
	      int limit = -1;
	      if (iiv instanceof ISystemValidator)
	        limit = ((ISystemValidator)iiv).getMaximumNameLength();
	      if (limit == -1)
	        limit = ValidatorFilterPoolName.MAX_FILTERPOOLNAME_LENGTH; // default is 50
	      textName.setTextLimit(limit);
		}		
	}
    
	// --------------------------------- //
	// METHODS FOR EXTRACTING USER DATA ... 
	// --------------------------------- //
	/**
	 * Return user-entered pool name.
	 * Call this after finish ends successfully.
	 */
	public String getPoolName()
	{
		return textName.getText().trim();
	}    
	/**
	 * Return user-selected pool manager name.
	 * Call this after finish ends successfully.
	 */
    public String getPoolManagerName()
    {
    	if (mgrCombo!=null)
    	  return mgrCombo.getText();
    	else
    	  return null;
    }
	
    /**
     * Return an object containing user-specified information pertinent to filter pool actions
     */
    public SystemFilterPoolDialogOutputs getFilterPoolDialogOutputs()
    {
    	SystemFilterPoolDialogOutputs output = new SystemFilterPoolDialogOutputs();
    	output.filterPoolName = getPoolName();
    	output.filterPoolManagerName = getPoolManagerName();
    	return output;
    }
  
}