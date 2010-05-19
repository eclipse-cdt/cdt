/*******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * David Dykstal (IBM) - [160403] filters should be connection private by default
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 * David McKnight   (IBM)        - [226948] [api][regression] SystemNewFilterWizard.createNamePage() is no longer available
 * David McKnight   (IBM)        - [249482] Duplicate Filters can be created if changing the filter pool
 * David Dykstal    (IBM)        - [148977] New Filter dialog should propose a default filter name on the 2nd page
 *******************************************************************************/

package org.eclipse.rse.ui.filters.dialogs;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolSelectionValidator;
import org.eclipse.rse.core.filters.ISystemFilterPoolWrapper;
import org.eclipse.rse.core.filters.ISystemFilterPoolWrapperInformation;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.wizards.AbstractSystemWizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;




/**
 * Second page of the New Filter wizard that prompts for the name of the filter.
 * 
 * @since 3.0 moved from internal to API
 */
public class SystemNewFilterWizardNamePage
	   extends AbstractSystemWizardPage
	   implements SelectionListener
{

	protected Text  nameText;
	protected Button uniqueCB;
	protected Label poolVerbiage;
	protected Label poolComboLabel;
	protected Combo poolWrapperCombo;
	protected Combo poolCombo;
	protected SystemMessage errorMessage;
	protected String inputFilterName;
	protected boolean contentsCreated;
	protected boolean userEditedName;
	protected boolean ignoreChanges;
	protected ISystemValidator nameValidator;
	protected ISystemValidator[] nameValidators;
	protected ISystemFilterPoolSelectionValidator filterPoolSelectionValidator;
	protected ISystemFilterPool[] poolsToSelectFrom = null;
    protected ISystemFilterPoolWrapper[] poolWrappers = null;
    protected ISystemFilterPoolWrapperInformation poolWrapperInformation;
	protected ISystemFilterPool parentPool = null;
  	private ISystemNewFilterWizardConfigurator configurator;

	/**
	 * Constructor.
	 */
	public SystemNewFilterWizardNamePage(SystemNewFilterWizard wizard, ISystemFilterPool parentPool, ISystemNewFilterWizardConfigurator data)
	{
		super(wizard, "SetNewFilterName", data.getPage2Title(), data.getPage2Description());	           //$NON-NLS-1$
	    this.parentPool = parentPool;
	    this.configurator = data;
		setHelp(data.getPage2HelpID());
	}

	// ---------------------------------
	// INPUT METHODS...
	// ---------------------------------
	/**
	 * Set the filter name to default the entry field to
	 */
	public void setFilterName(String filterName)
	{
		this.inputFilterName = filterName;
		if (nameText != null)
		  nameText.setText(inputFilterName);
	}
	/**
	 * Set the validator to use to verify the filter name is correct
	 */
	public void setFilterNameValidator(ISystemValidator nameValidator)
	{
		this.nameValidator = nameValidator;
	}
    /**
     * Call if you want to allow the user to select the filter pool to create this filter in.
     */
    public void setAllowFilterPoolSelection(ISystemFilterPool[] poolsToSelectFrom,
                                             ISystemValidator[] nameValidators)
    {
    	this.poolsToSelectFrom = poolsToSelectFrom;
    	this.nameValidators = nameValidators;
    	if ((poolsToSelectFrom != null) && (poolsToSelectFrom.length>0))
    	{
    	  if (parentPool == null)
    	     parentPool = poolsToSelectFrom[0];
    	}
    }
    /**
     * This is an alternative to {@link #setAllowFilterPoolSelection(ISystemFilterPool[], ISystemValidator[])}
     * <p>
     * If you want to prompt the user for the parent filter pool to create this filter in,
     *  but want to not use the term "pool" say, you can use an array of euphamisms. That is,
     *  you can pass an array of objects that map to filter pools, but have a different
     *  display name that is shown in the dropdown.
     * <p>
     * Of course, if you want to do this, then you will likely want to offer a different
     *  label and tooltip for the prompt, and different verbiage above the prompt. The
     *  object this method accepts as a parameter encapsulates all that information, and
     *  there is a default class you can use for this.
     */
    public void setAllowFilterPoolSelection(ISystemFilterPoolWrapperInformation poolWrappersToSelectFrom,
                                             ISystemValidator[] nameValidators)
    {
    	this.poolWrapperInformation = poolWrappersToSelectFrom;
    	this.nameValidators = nameValidators;
    	if (parentPool == null)
    	  parentPool = poolWrappersToSelectFrom.getPreSelectWrapper().getSystemFilterPool();
    }
	/**
	 * Set the validator to call when the user selects a filter pool. Optional.
	 */
	public void setFilterPoolSelectionValidator(ISystemFilterPoolSelectionValidator validator)
	{
	     filterPoolSelectionValidator = validator;
	     //System.out.println("Inside setFilterPoolSelectionValidator. Non null? " + (validator != null));
	}

	// ---------------------------------
	// LIFECYCLE METHODS...
	// ---------------------------------

	/**
	 * Populate the dialog area with our widgets. Return the composite they are in.
	 */
	public Control createContents(Composite parent)
	{

		int nbrColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);

		SystemWidgetHelpers.createVerbiage(composite_prompts, configurator.getPage2NameVerbiage(), nbrColumns, false, 200);
		nameText = SystemWidgetHelpers.createLabeledTextField(composite_prompts, null, configurator.getPage2NamePromptLabel(), configurator.getPage2NamePromptTooltip());
		
        addSeparatorLine(composite_prompts, nbrColumns);       
        addFillerLine(composite_prompts, nbrColumns);

        // allow the user to create this filter uniquely for this connection, which means putting it in a
        //  special filter pool we will create, just for this connection. This option is not shown if we are
        //  already told which filter pool to create the filter in, such as in Show Filter Pools mode, when
        //  the user selects New Filter to create a filter in the selected pool. We assume in this case the
        //  will go in whatever filter is selected.
        if ((poolsToSelectFrom!=null) || (poolWrapperInformation!=null))
        {
			uniqueCB = SystemWidgetHelpers.createCheckBox(composite_prompts, nbrColumns, configurator.getPage2UniqueToConnectionLabel(), null);
			uniqueCB.setToolTipText(configurator.getPage2UniqueToConnectionToolTip());
			uniqueCB.addSelectionListener(this);
			uniqueCB.setSelection(true); // [160403] filters should be connection private by default
        }

        addFillerLine(composite_prompts, nbrColumns);

        if (poolsToSelectFrom != null)
        {
		   	poolVerbiage = SystemWidgetHelpers.createVerbiage(composite_prompts, configurator.getPage2PoolVerbiage(), nbrColumns, false, 200);
		   	poolVerbiage.setToolTipText(configurator.getPage2PoolVerbiageTip());
           	poolCombo = SystemWidgetHelpers.createLabeledReadonlyCombo(composite_prompts, null, configurator.getPage2PoolPromptLabel(), configurator.getPage2PoolPromptTooltip());
			poolComboLabel = SystemWidgetHelpers.getLastLabel();
           	String[] poolNames = new String[poolsToSelectFrom.length];
		   	int filterPoolSelectionIndex = 0;
           	for (int idx=0; idx<poolNames.length; idx++)
           	{
           	  	ISystemFilterPool pool = poolsToSelectFrom[idx];
           	  	if (pool == parentPool)
           	    	filterPoolSelectionIndex = idx;
              	poolNames[idx] = pool.getSystemFilterPoolManager().getName()+"."+pool.getName(); //$NON-NLS-1$
           	}
           	if ((nameValidator == null) && (nameValidators!=null))
             	nameValidator = nameValidators[filterPoolSelectionIndex];
           	poolCombo.setItems(poolNames);
           	poolCombo.select(filterPoolSelectionIndex);
           	poolCombo.addSelectionListener(this);
           	if ((uniqueCB!=null) && uniqueCB.getSelection())
           	{
				poolVerbiage.setEnabled(false);
				poolComboLabel.setEnabled(false);
           		poolCombo.setEnabled(false);
           	}
        }
        else if (poolWrapperInformation != null)
        {
		 	poolVerbiage = SystemWidgetHelpers.createVerbiage(composite_prompts, poolWrapperInformation.getVerbiageLabel(), nbrColumns, false, 200);
		   	poolWrapperCombo = SystemWidgetHelpers.createLabeledReadonlyCombo(composite_prompts, null, poolWrapperInformation.getPromptLabel(), poolWrapperInformation.getPromptTooltip());
			poolComboLabel = SystemWidgetHelpers.getLastLabel();
		   	poolWrappers = poolWrapperInformation.getWrappers();
           	String[] poolNames = new String[poolWrappers.length];
		   	int filterPoolSelectionIndex = 0;
           	for (int idx=0; idx<poolNames.length; idx++)
           	{
           	  	if (poolWrapperInformation.getPreSelectWrapper() == poolWrappers[idx])
           	    	filterPoolSelectionIndex = idx;
              	poolNames[idx] = poolWrappers[idx].getDisplayName();
           	}
           	if ((nameValidator == null) && (nameValidators!=null))
             	nameValidator = nameValidators[filterPoolSelectionIndex];
           	poolWrapperCombo.setItems(poolNames);
           	poolWrapperCombo.select(filterPoolSelectionIndex);
           	poolWrapperCombo.addSelectionListener(this);
			if ((uniqueCB!=null) && uniqueCB.getSelection())
			{
				poolVerbiage.setEnabled(false);
				poolComboLabel.setEnabled(false);
				poolWrapperCombo.setEnabled(false);
			}
        }

		// initialize inputs
        if (nameValidator != null)
        {
        	int maxNameLength = nameValidator.getMaximumNameLength();
        	if (maxNameLength >= 0)
        	  nameText.setTextLimit(maxNameLength);
        }
		if (inputFilterName != null)
		  nameText.setText(inputFilterName);

		// add keystroke listeners...
		nameText.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (ignoreChanges)
					  return;
					userEditedName = true;
					validateNameInput();
				}
			}
		);

		setPageComplete();
		contentsCreated = true;
		return composite_prompts;
	}
	/**
	 * Return the Control to be given initial focus.
	 * Override from parent. Return control to be given initial focus.
	 */
	protected Control getInitialFocusControl()
	{
        return nameText;
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
		if (!contentsCreated)
		  return true;
		return (verify() == null);
	}

	/**
	 * Return true if the page is complete, so to enable Finish.
	 * Called by wizard framework.
	 */
	public boolean isPageComplete()
	{
		boolean pageComplete = (errorMessage == null) && (nameText!=null);
		if (pageComplete)
		  pageComplete = (nameText.getText().trim().length() > 0);
		return pageComplete;
	}

	/**
	 * Inform caller of page-complete status of this page
	 */
	public void setPageComplete()
	{
		setPageComplete(isPageComplete());
	}
	/**
	 * User has selected something
	 */
	public void widgetSelected(SelectionEvent e)
	{
		Object src = e.getSource();
		if (src == poolCombo)
		{
		  	int selection = poolCombo.getSelectionIndex();
 			
		  	if ((selection >= 0) && (nameValidators!=null)){
		    	nameValidator = nameValidators[selection];
		    	
	 			ISystemFilterPool currentPool = poolsToSelectFrom[selection];
	 			if  (currentPool == parentPool){ // if this is the connection-unique filter pool, set the uniqueCB
	 				uniqueCB.setSelection(true); 			
	 				poolCombo.setEnabled(false);
	 			} 			
		  	}
		}
		else if (src == poolWrapperCombo)
		{
		  	int selection = poolWrapperCombo.getSelectionIndex();
 			
		  	if ((selection >= 0) && (nameValidators!=null)){
		    	nameValidator = nameValidators[selection];
		    				  	
	 			ISystemFilterPoolWrapper currentPool = poolWrappers[selection];
	 			if  (currentPool == poolWrapperInformation.getPreSelectWrapper()){ // if this is the connection-unique filter pool, set the uniqueCB
	 				uniqueCB.setSelection(true); 				
	 				poolWrapperCombo.setEnabled(false);
	 			} 	
		  	}
		}
		else if (src == uniqueCB)
		{
			boolean selected = uniqueCB.getSelection();
			if (poolVerbiage != null)
			  	poolVerbiage.setEnabled(!selected);
			if (poolCombo != null){
			 	poolCombo.setEnabled(!selected);
			 	
			 				 				 	
			 	// if unique to connection then only connection-private filter pool can be used			 		
			 	// if it's not unique to connection, then the connection-private filter pool should not be selected
		 		boolean foundPool = false;
			 	int filterPoolSelectionIndex = 0;
		 		if (poolsToSelectFrom != null){
		 			
		 			int currentIndex = poolCombo.getSelectionIndex();
		 			ISystemFilterPool currentPool = poolsToSelectFrom[currentIndex];
		 			if (currentPool != parentPool && !selected){ 
		 				// then the current pool is okay
		 				filterPoolSelectionIndex = currentIndex;
		 			}
		 			else {
			 			for (int idx=0; idx<poolsToSelectFrom.length && !foundPool; idx++){
			 				ISystemFilterPool pool = poolsToSelectFrom[idx];
			 		
			 				boolean isConnectionUnique = pool == parentPool;
			 				
			 				if ((isConnectionUnique && selected) || (!isConnectionUnique && !selected)){			 				
			 					filterPoolSelectionIndex = idx;
		           	  			foundPool = true;		           	  			
		           	  		}
			 			}
			 			poolCombo.select(filterPoolSelectionIndex);
		 			}
		 		}
		 		else if (poolWrappers != null){
		 			int currentIndex = poolWrapperCombo.getSelectionIndex();
		 			ISystemFilterPoolWrapper currentPoolWrapper = poolWrappers[currentIndex];
		 			if (currentPoolWrapper == poolWrapperInformation.getPreSelectWrapper()){
		 				// then the current pool is okay
		 				filterPoolSelectionIndex = currentIndex;
		 			}
		 			else {
			 			for (int idx=0; idx<poolWrappers.length && !foundPool; idx++){
			 				
			 				boolean isConnectionUnique = poolWrapperInformation.getPreSelectWrapper() == poolWrappers[idx];
			 				if ((isConnectionUnique && selected) || (!isConnectionUnique && !selected)){			 				
			 					filterPoolSelectionIndex = idx;
		           	  			foundPool = true;		           	  			
		           	  		}
			 			}
			 			poolWrapperCombo.select(filterPoolSelectionIndex);
		 			}
		 		}

		 		if (nameValidators != null){
		 			nameValidator = nameValidators[filterPoolSelectionIndex];
		 		}
			 	
			}
			if (poolWrapperCombo != null)
				poolWrapperCombo.setEnabled(!selected);
			if (poolComboLabel != null)
				poolComboLabel.setEnabled(!selected);
		}
		verify();
		setPageComplete();
	}
	/**
	 * User has selected something and pressed Enter
	 */
	public void widgetDefaultSelected(SelectionEvent e)
	{
	}
	// ---------------------------------
	// VERIFICATION METHODS...
	// ---------------------------------
	/**
	 * Verify all contents
	 */
	public SystemMessage verify()
	{
		errorMessage = null;
		Control controlInError = null;

		if ((errorMessage == null) && (filterPoolSelectionValidator != null))
		{
			errorMessage = filterPoolSelectionValidator.validate(getParentSystemFilterPool());
			if (poolCombo != null)
			  controlInError = poolCombo;
			else if (poolWrapperCombo != null)
			  controlInError = poolCombo;
		}
		if ((errorMessage == null) && (nameValidator != null))
		{
	        errorMessage = nameValidator.validate(nameText.getText().trim());
			controlInError = nameText;
		}

		if (errorMessage != null)
		{
		  	if (controlInError != null)
		  	  controlInError.setFocus();
		  	setErrorMessage(errorMessage);
		}
		else
		    clearErrorMessage();
		return errorMessage;
	}
  	/**
	 * This hook method is called whenever the text changes in the filter name input field.
	 */
	protected SystemMessage validateNameInput()
	{
	    errorMessage= null;
		if (nameValidator != null)
	      errorMessage = nameValidator.validate(nameText.getText().trim());
		if ((errorMessage == null) && (filterPoolSelectionValidator != null))
			errorMessage = filterPoolSelectionValidator.validate(getParentSystemFilterPool());
		setPageComplete();
		if (errorMessage != null)
		  setErrorMessage(errorMessage);
		else
		  clearErrorMessage();
		return errorMessage;
	}


	// ---------------------------------
	// METHODS FOR EXTRACTING USER DATA
	// ---------------------------------
	/**
	 * Return name of filter
	 * Call this after finish ends successfully.
	 */
	public String getFilterName()
	{
		if (nameText != null)
		  return nameText.getText().trim();
		else
		  return inputFilterName;
	}
	/**
	 * Return the filter pool that was explicitly chosen by the user,
	 *  or implicitly set by the caller.
	 */
	public ISystemFilterPool getParentSystemFilterPool()
	{
		ISystemFilterPool pool = null;
		// do we prompt with a list of filter pools? Yes, just return selected...
		if (poolCombo != null)
		{
		  int selection = poolCombo.getSelectionIndex();
		  if (selection < 0)
		    selection = 0;
		  pool = poolsToSelectFrom[selection];
		}
		// do we prompt using a wrapper of some kind, such a profile or a command set,
		// from which we deduce the pool? If so, deduce pool from selected wrapper....
		else if (poolWrapperCombo != null)
		{
		  int selection = poolWrapperCombo.getSelectionIndex();
		  if (selection < 0)
		    selection = 0;
		  pool = poolWrappers[selection].getSystemFilterPool();
		}
		// else no prompt so we must have been given the explicit filter pool in which
		// to create this filter. Eg, in Show Filter Pools mode and the user selects a
		// filter pool and choose New Filter from it.
		else
		  pool = parentPool;
		//System.out.println("Inside getParentSystemFilterPool. returning " + pool.getName());
	    return pool;
	}

	/**
	 * Return the user's decision whether to create this filter uniquely
	 *  for this connection, or for all applicable connections.
	 */
	public boolean getUniqueToThisConnection()
	{
		if (uniqueCB != null)
			return uniqueCB.getSelection();
		else
		 	return false;
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
			if (!userEditedName && (nameText!=null))
			{
				String defaultName = ((SystemNewFilterWizard)getWizard()).getDefaultFilterName();
				if (defaultName != null) {
					if (defaultName.length() > 0) {
						if (nameValidator != null) {
							String nameProposal = defaultName;
							boolean invalid = true;
							int times = 0;
							while (invalid && times < 20) { // try only 20 times
								if (nameValidator.validate(nameProposal) != null) {
									times++;
									nameProposal = defaultName + " " + times; //$NON-NLS-1$
								} else {
									invalid = false;
									defaultName = nameProposal;
								}
							}
						}				
					}
					ignoreChanges = true;
				    nameText.setText(defaultName);
				    nameText.selectAll();
				    ignoreChanges = false;
				}
			}
		    verify();
		    //System.out.println("Wizard size = " + ((SystemNewFilterWizard)getWizard()).getShell().getSize());
		}
	}

	// --------------------------------------------------------------
	// ALL THE MRI ON THIS PAGE IS CONFIGURABLE. CALL HERE TO SET IT.
	// --------------------------------------------------------------

}
