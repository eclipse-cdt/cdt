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

package org.eclipse.rse.ui.filters.actions;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.rse.ui.actions.SystemBaseDialogAction;
import org.eclipse.rse.ui.filters.SystemFilterDialogInputs;
import org.eclipse.rse.ui.filters.SystemFilterDialogInterface;
import org.eclipse.swt.widgets.Shell;



/**
 * Base class capturing the attributes and operations common to dialog actions 
 * that work on system filters.
 */
public abstract class SystemFilterAbstractFilterAction 
       extends SystemBaseDialogAction
       
{


	protected SystemFilterDialogInputs dlgInputs;

	/**
	 * Constructor when given the translated action label
	 */
	public SystemFilterAbstractFilterAction(Shell parent, String title) 
	{
		super(title, null, parent);
        allowOnMultipleSelection(false);
        init();
	}	

	/**
	 * Constructor when given the translated action label and tooltip
	 */
	public SystemFilterAbstractFilterAction(Shell parent, String title, String tooltip) 
	{
		super(title, tooltip, null, parent);
        allowOnMultipleSelection(false);
        init();
	}	
	
	/**
	 * Common initialization code
	 */
	protected void init()
	{
		dlgInputs = new SystemFilterDialogInputs();
	}

    // ----------------------------
    // HELP ID SETTINGS...
    // ----------------------------

    /**
     * Set the help context Id (infoPop) for this action. This must be fully qualified by
     *  plugin ID.
     * <p>
     * Same as {@link org.eclipse.rse.ui.actions.SystemBaseAction #setHelp(String)}
     * @see org.eclipse.rse.ui.actions.SystemBaseAction #getHelpContextId()
     */
    public void setHelpContextId(String id)
    {
    	setHelp(id);
    }

    // ----------------------------
    // ATTRIBUTE GETTERS/SETTERS...
    // ----------------------------

    /**
     * Set the dialog title.
     * Either call this or override getDialogTitle()
     */
    public void setDialogTitle(String title)
    {
    	dlgInputs.title = title;
    }    
    /**
     * Get the dialog title.
     * By default, uses what was given in setDialogTitle, or an english default if nothing set.
     */
    public String getDialogTitle()
    {
    	return dlgInputs.title;
    }

    /**
     * Set the dialog prompt text.
     * Either call this or override getDialogPrompt()
     */
    public void setDialogPrompt(String prompt)
    {
    	dlgInputs.prompt = prompt;
    }    
    /**
     * Get the dialog prompt.
     * By default, uses what was given in setDialogPrompt
     */
    public String getDialogPrompt()
    {
    	return dlgInputs.prompt; 
    }

    /**
     * Set the dialog's filter name prompt text and tooltip
     * Either call this or override getDialogFilterNamePrompt/Tip() 
     */
    public void setDialogFilterNamePrompt(String prompt, String tip)
    {
    	dlgInputs.filterNamePrompt = prompt;
    	dlgInputs.filterNameTip = tip;
    }    
    /**
     * Get the dialog's filter name prompt text.
     * By default, uses what was given in setDialogFilterNamePrompt.
     */
    public String getDialogFilterFilterNamePrompt()
    {
    	return dlgInputs.filterNamePrompt; 
    }
    /**
     * Get the dialog's filter name tooltip text.
     * By default, uses what was given in setDialogFilterNamePrompt.
     */
    public String getDialogFilterNameTip()
    {
    	return dlgInputs.filterNameTip; 
    }

    /**
     * Set the dialog's pre-select information. 
     * Either call this or override getDialogPreSelectInput() 
     */
    public void setDialogPreSelectInput(Object selectData)
    {
    	dlgInputs.preSelectObject = selectData;
    }    
    /**
     * Get the dialog's pre-select information. 
     * By default, uses what was given in setDialogPreSelectInput.
     */
    public Object getDialogPreSelectInput()
    {
    	return dlgInputs.preSelectObject;
    }


    // -------------------------
    // PARENT CLASS OVERRIDES...
    // -------------------------

    /**
     * Called by SystemBaseAction when selection is set.
     * Our opportunity to verify we are allowed for this selected type.
     */
	public boolean checkObjectType(Object selectedObject)
	{
		return true;
		//return (selectedObject instanceof SystemFilterPoolReferenceManagerProvider); // override as appropriate
	}

    /**
     * Extends run in parent class to call doOKprocessing if the result of calling
     * getDialogValue() resulted in a non-null value. 
	 */
	public void run()
	{
		super.run();
		if (getValue() != null)
		  doOKprocessing(getValue());
	}
	

	/**
	 * Overrides parent method to allow creating of a dialog meeting our interface,
	 * so we can pass instance of ourselves to it for callbacks to get our data.
	 * <p>
	 * If your dialog does not implement our interface, override this method!
	 */
	protected Dialog createDialog(Shell parent)
	{
		SystemFilterDialogInterface fDlg = createFilterDialog(parent);
		fDlg.setFilterDialogActionCaller(this);
		return (Dialog)fDlg;
	}
	
	/**
	 * Where you create the dialog meeting our interface. If you override
	 * createDialog, then override this to return null
	 */
	public abstract SystemFilterDialogInterface createFilterDialog(Shell parent);

	/**
	 * If you decide to use the supplied run method as is,
	 *  then you must override this method to retrieve the data
	 *  from the dialog. For InputDialog dialogs, this is simply
	 *  a matter of return dlg.getValue();
	 * <p>
	 * This is called by the run method after the dialog returns. Callers
	 * of this object can subsequently retrieve it by calling getValue.
	 * 
	 * @param dlg The dialog object, after it has returned from open.
	 */
	protected abstract Object getDialogValue(Dialog dlg);	

	/**
	 * Method called when ok pressed on dialog and after getDialogValue has set the
	 * value attribute appropriately. 
	 * <p>
	 * Only called if user pressed OK on dialog.
	 * <p>
	 * @param dlgValue The output of getDialogValue().
	 */
	public abstract void doOKprocessing(Object dlgValue);
	
}