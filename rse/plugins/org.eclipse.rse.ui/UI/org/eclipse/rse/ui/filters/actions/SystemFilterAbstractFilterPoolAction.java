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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.filters.ISystemFilterStringReference;
import org.eclipse.rse.ui.actions.SystemBaseDialogAction;
import org.eclipse.rse.ui.dialogs.SystemSimpleContentElement;
import org.eclipse.rse.ui.filters.SystemFilterPoolDialogInputs;
import org.eclipse.rse.ui.filters.SystemFilterPoolDialogInterface;
import org.eclipse.swt.widgets.Shell;



/**
 * Base class capturing the attributes and operations common to dialog actions 
 * that work on system filter pools.
 */
public abstract class SystemFilterAbstractFilterPoolAction 
       extends SystemBaseDialogAction
       
{


	protected SystemFilterPoolDialogInputs dlgInputs;
	protected String mgrNamePreselect;

	/**
	 * Constructor when given the translated action label
	 */
	public SystemFilterAbstractFilterPoolAction(Shell parent, String title) 
	{
		super(title, null, parent);
        allowOnMultipleSelection(false);
        init();
	}	
	
	/**
	 * Constructor when given the translated action label
	 */
	public SystemFilterAbstractFilterPoolAction(Shell parent, String title, String tooltip) 
	{
		super(title, tooltip, null, parent);
        allowOnMultipleSelection(false);
        init();
	}	

	
	
	/**
	 * Constructor when given the resource bundle and key for the action label
	 */
	public SystemFilterAbstractFilterPoolAction(Shell parent, ImageDescriptor image, String label, String tooltip) 
	{
		super(label, tooltip, image, parent);
        allowOnMultipleSelection(false);
        init();
	}
	
	/**
	 * Common initialization code
	 */
	protected void init()
	{
		dlgInputs = new SystemFilterPoolDialogInputs();
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

    // --------------------------------------
    // SELECTION CHANGED INTERCEPT METHODS...
    // --------------------------------------    

	/**
	 * This is called by the UI calling the action, if that UI is not a selection provider.
	 * That is, this is an alternative to calling selectionChanged when there is no SelectionChangedEvent.
	 * @see #selectionChanged(SelectionChangedEvent event)
	 */
	public void setSelection(ISelection selection) 
	{
		super.setSelection(selection);
		Object firstSelection = getFirstSelection();
		if (isEnabled() && (firstSelection != null))
		{
		  if (firstSelection instanceof SystemSimpleContentElement)
		    firstSelection = ((SystemSimpleContentElement)firstSelection).getData();

		  if (firstSelection instanceof ISystemFilterPoolManagerProvider)
		    setFilterPoolManagerProvider((ISystemFilterPoolManagerProvider)firstSelection);
		  else if (firstSelection instanceof ISystemFilterPoolManager)
		    setFilterPoolManagerProvider(((ISystemFilterPoolManager)firstSelection).getProvider());		    
		  else if (firstSelection instanceof ISystemFilterPool)
		    setFilterPoolManagerProvider(((ISystemFilterPool)firstSelection).getProvider());		    
		  else if (firstSelection instanceof ISystemFilter)
		    setFilterPoolManagerProvider(((ISystemFilter)firstSelection).getProvider());		    
		  else if (firstSelection instanceof ISystemFilterString)
		    setFilterPoolManagerProvider(((ISystemFilterString)firstSelection).getProvider());		    

		  else if (firstSelection instanceof ISystemFilterPoolReferenceManagerProvider)
		    setFilterPoolReferenceManager(((ISystemFilterPoolReferenceManagerProvider)firstSelection).getSystemFilterPoolReferenceManager());
		  else if (firstSelection instanceof ISystemFilterPoolReferenceManager)
		    setFilterPoolReferenceManager((ISystemFilterPoolReferenceManager)firstSelection);		    
		  else if (firstSelection instanceof ISystemFilterPoolReference)
		    setFilterPoolReferenceManager(((ISystemFilterPoolReference)firstSelection).getFilterPoolReferenceManager());		    
		  else if (firstSelection instanceof ISystemFilterReference)
		    setFilterPoolReferenceManager(((ISystemFilterReference)firstSelection).getFilterPoolReferenceManager());		    
		  else if (firstSelection instanceof ISystemFilterStringReference)
		    setFilterPoolReferenceManager(((ISystemFilterStringReference)firstSelection).getFilterPoolReferenceManager());		    
		}
	}

    // ----------------------------
    // ATTRIBUTE GETTERS/SETTERS...
    // ----------------------------

    /**
     * Set the input filter pool manager provider from which to get the list of filter pool managers.
     * Either call this or call setFilterPoolManagers or override getFilterPoolManagerProvider().
     */
    public void setFilterPoolManagerProvider(ISystemFilterPoolManagerProvider provider)
    {
    	dlgInputs.poolManagerProvider = provider;
    	//setFilterPoolManagers(provider.getSystemFilterPoolManagers());
    }

    /**
     * Get the input filter pool manager provider from which to get the list of filter pool managers.
     */
    public ISystemFilterPoolManagerProvider getFilterPoolManagerProvider()
    {
    	//if (dlgInputs.poolManagerProvider != null)    	  
    	  return dlgInputs.poolManagerProvider;
    	//else if ((dlgInputs.poolManagers != null) && (dlgInputs.poolManagers.length > 0))
    	  //return dlgInputs.poolManagers[0].getProvider();
    	//else
    	  //return null;
    }

    /**
     * Set the input filter pool managers from which to allow selections of filter pools.
     * Either call this or call setFilterPoolManagerProvider or override getFilterPoolManagers().
     */
    public void setFilterPoolManagers(ISystemFilterPoolManager[] managers)
    {
    	dlgInputs.poolManagers = managers;
    }

    /**
     * Returns the filter pool managers from which to show filter pools for selection.
     * <p>
     * By default, tries the following in this order: 
     * <ol>
     *  <li>calls getFilterPoolManagerProvider if setFilterPoolManagerProvider was called
     *  <li>uses what was given in setFilterPoolManagers
     *  <li>uses what was given in setFilterPoolReferenceManager, calling its getSystemFilterPoolManagers
     *  <li>checks if the current selection is a filterPoolReferenceManager, and if so uses the managers it references from.
     * </ol>
     */
    public ISystemFilterPoolManager[] getFilterPoolManagers()
    {
    	ISystemFilterPoolManager[] mgrs = null;
        ISystemFilterPoolManagerProvider provider = getFilterPoolManagerProvider();
        if (mgrs == null)
    	  mgrs = dlgInputs.poolManagers;
		if ((mgrs==null) && (provider != null))
		  mgrs = provider.getSystemFilterPoolManagers(); // get it in real time.
    	if (mgrs == null)
    	{
    	  ISystemFilterPoolReferenceManager refmgr = getFilterPoolReferenceManager();
    	  if (refmgr != null)
    	    mgrs = refmgr.getSystemFilterPoolManagers(); 
    	}
        if (mgrs == null)
        {
          ISystemFilterPoolReferenceManagerProvider sfprmp = getReferenceManagerProviderSelection();
          if (sfprmp != null)
            mgrs = sfprmp.getSystemFilterPoolReferenceManager().getSystemFilterPoolManagers();
        }
    	return mgrs;
    }
    /**
     * Return the current selection if it implements SystemFilterPoolReferenceManagerProvider
     */
    protected ISystemFilterPoolReferenceManagerProvider getReferenceManagerProviderSelection()
    {
    	Object obj = getFirstSelection();
        if ((obj instanceof ISystemFilterPoolReferenceManagerProvider))
          return (ISystemFilterPoolReferenceManagerProvider)obj;          
        else
          return null;
    }
	/**
	 * Set the zero-based index of the manager name to preselect.
	 * The default is zero.
	 * Either call this or override getFilterPoolManagerNameSelectionIndex or call setFilterPoolManagerNamePreSelection(String)
	 */
	public void setFilterPoolManagerNameSelectionIndex(int index)
	{
       dlgInputs.mgrSelection = index;
	}
	/**
	 * Returns the zero-based index of the manager name to preselect.
	 * Returns what was set in setFilterPoolManagerNamePreSelection or setFilterPoolManagerNameSelectionIndex by default.
	 */
	public int getFilterPoolManagerNameSelectionIndex()
	{
		int pos = -1;
		if (mgrNamePreselect != null)
		{
		  ISystemFilterPoolManager[] mgrs = getFilterPoolManagers();
		  if (mgrs != null)
		  {
		  	for (int idx=0; (pos<0) && (idx<mgrs.length); idx++)
		  	   if (mgrs[idx].getName().equals(mgrNamePreselect))
		  	     pos = idx;
		  }
		}
		if (pos < 0)
          return dlgInputs.mgrSelection;
        else
        {
          //System.out.println("Found a match for mgr name '"+mgrNamePreselect+"' so preselect index is "+pos);
          return pos;
        }
	}
	/**
	 * Set the name of the filter pool manager to pre-select
	 */
	public void setFilterPoolManagerNamePreSelection(String name)
	{
        this.mgrNamePreselect = name;		
	}

    /**
     * Set the input filter pool reference manager which is holding the references this
     * dialog is allowing the user to select. 
     * <p>
     * If you call this, then this action has everything it needs to know to be fully self-contained.
     * It will totally handle updating the reference manager with the user's selections and
     * deselections. 
     */
    public void setFilterPoolReferenceManager(ISystemFilterPoolReferenceManager refManager)
    {
    	dlgInputs.refManager = refManager;
    }
    /**
     * Returns the filter pool reference manager which contains the list of selected filter pools
     * that this dialog is showing and allowing the user to change. 
     * <p>
     * If not set, then the subclass needs to override doOKprocessing.
     */
    public ISystemFilterPoolReferenceManager getFilterPoolReferenceManager()
    {
    	return dlgInputs.refManager;
    }

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
     * Set the dialog's filter pool name prompt text and tooltip
     * Either call this or override getDialogFilterPoolNamePrompt/Tip() 
     */
    public void setDialogFilterPoolNamePrompt(String prompt, String tip)
    {
    	dlgInputs.poolNamePrompt = prompt;
    	dlgInputs.poolNameTip = tip;
    }    
    /**
     * Get the dialog's filter pool name prompt text.
     * By default, uses what was given in setDialogFilterPoolNamePrompt.
     */
    public String getDialogFilterPoolNamePrompt()
    {
    	return dlgInputs.poolNamePrompt; 
    }
    /**
     * Get the dialog's filter pool name tooltip text.
     * By default, uses what was given in setDialogFilterPoolNamePrompt.
     */
    public String getDialogFilterPoolNameTip()
    {
    	return dlgInputs.poolNameTip; 
    }

    /**
     * Set the dialog's filter pool manager name prompt text and tooltip
     * Either call this or override getDialogFilterPoolManagerNamePrompt/Tip() 
     */
    public void setDialogFilterPoolManagerNamePrompt(String prompt, String tip)
    {
    	dlgInputs.poolMgrNamePrompt = prompt;
    	dlgInputs.poolMgrNameTip = tip;
    }    
    /**
     * Get the dialog's filter pool manager name prompt text.
     * By default, uses what was given in setDialogFilterPoolManagerNamePrompt.
     */
    public String getDialogFilterPoolManagerNamePrompt()
    {
    	return dlgInputs.poolMgrNamePrompt; 
    }
    /**
     * Get the dialog's filter pool manager name tooltip text.
     * By default, uses what was given in setDialogFilterPoolManagerNamePrompt.
     */
    public String getDialogFilterPoolManagerNameTip()
    {
    	return dlgInputs.poolMgrNameTip; 
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
		return (selectedObject instanceof ISystemFilterPoolReferenceManagerProvider); // override as appropriate
	}


    /**
     * Walk elements deciding pre-selection
     */
    protected void preSelect(SystemSimpleContentElement inputElement)
    {
		  SystemSimpleContentElement[] mgrElements = inputElement.getChildren();
		  for (int idx=0; idx<mgrElements.length; idx++)
		  {
		  	 //SystemFilterPoolManager mgr = (SystemFilterPoolManager)mgrElements[idx].getData();
		     SystemSimpleContentElement[] poolElements = mgrElements[idx].getChildren();
		     for (int jdx=0; jdx<poolElements.length; jdx++)
		     {
          	    //if (poolElements[jdx].isSelected())
                  poolElements[jdx].setSelected(
                     getFilterPoolPreSelection((ISystemFilterPool)poolElements[jdx].getData()));   
		     }
		  }    	
    }
	/**
	 * Decide per pool if it should be selected or not.
	 * Default behaviour is to select it if it is currently referenced.
	 */
	protected boolean getFilterPoolPreSelection(ISystemFilterPool pool)
	{
		return pool.getReferenceCount() > 0;
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
		SystemFilterPoolDialogInterface fpDlg = createFilterPoolDialog(parent);
		fpDlg.setFilterPoolDialogActionCaller(this);
		return (Dialog)fpDlg;
	}
	
	/**
	 * Where you create the dialog meeting our interface. If you override
	 * createDialog, then override this to return null
	 */
	public abstract SystemFilterPoolDialogInterface createFilterPoolDialog(Shell parent);

	/**
	 * If you decide to use the supplied run method as is,
	 *  then you must override this method to retrieve the data
	 *  from the dialog. For InputDialog dialogs, this is simply
	 *  a matter of returning dlg.getValue();
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