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

package org.eclipse.rse.ui.actions;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.ui.dialogs.ISystemPromptDialog;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.dialogs.SystemWizardDialog;
import org.eclipse.swt.widgets.Shell;



/**
 * A specialization for Action for actions that put up dialogs.
 * These actions have some common requirements:
 * <sl>
 *  <li>They need to take the parent Shell object in the constructor or later perhaps via setParent
 *  <li>They need to be able to set the input value for the dialog. This is typically related to 
 *       what is currently selected. 
 *  <li>They need to be able to get the output value from the dialog. This can be queried by 
 *       calling getValue after the action runs.
 * </sl>
 * <p>This subclass of SystemBaseAction implements the run() method in a way optimized for the processing
 *  of dialogs: it calls an abstract method to create the dialog, then sets the input from the action's
 *  value (if set) or selection (otherwise) and opens the dialog. After, it calls an abstract method to
 *  extract the dialog's output object which is used to set this action's value, for the benefit of the
 *  caller.  
 * </p>
 * <p>To use this dialog, subclass it and <b>override</b> the following methods</p>:
 * <sl>
 *  <li>{@link #createDialog(Shell)}
 *  <li>{@link #getDialogValue(Dialog)}
 *  <li>{@link #run()} but ONLY if you don't want the default implementation of this method.
 * </sl>
 * <p>In addition to the methods you must override, you can optionally call various methods to configure
 * this action. In addition to those in the parent class, this class offers these configuration methods:</p>
 * <sl>
 *  <li>{@link #setDialogHelp(String)} to set the context help ID for the dialog, for cases where the dialog is generic
 *  and its help depends on context and hence cannot be hardcoded in the dialog class.
 *  <li>{@link #setValue(Object)} to the set the input object to pass to the dialog, for cases where the current selection
 *  is not what you want to pass to the dialog.
 *  <li>{@link #setNeedsProgressMonitor(boolean)} to specify if the dialog is to display a progress monitor or not, for cases
 *  where this decision depends on context and hence cannot be hardcoded in the dialog class.
 *  <li>{@link #setProcessAllSelections(boolean)} to specify the behaviour when there are multiple objects selected. By 
 *  default, the dialog will be created and processed once for each selected object, but you can specify that you instead
 *  want to only invoke the dialog once and pass in all selected objects as a single ISelection object.
 * </sl>
 * 
 */
public abstract class SystemBaseDialogAction extends SystemBaseAction 
                                             implements ISystemDialogAction
{
	protected Object value;	
	protected boolean processAll;
	protected boolean needsProgressMonitor, needsProgressMonitorSet;
	protected boolean cancelled;
	protected String  dlgHelpId;

	/**
	 * Constructor for SystemBaseDialogAction when translated label is known. You must separately
	 *  call setToolTipText and setDescription to enable these if desired.
	 * @param text string to display in menu or toolbar
	 * @param image icon to display in menu or toolbar. Can be null.
	 * @param shell Shell of parent window. Can be null if you don't know it, but call setShell when you do.
	 */
	protected SystemBaseDialogAction(String text, ImageDescriptor image, Shell shell) 
	{
		super(text, image, shell);
	}
	/**
	 * Constructor for SystemBaseDialogAction when translated label and tooltip are known. You must
	 *  separately call setDescription to enable this if desired.
	 * @param text string to display in menu or toolbar
	 * @param tooltip string to display when user hovers mouse over action.
     * @param image icon to display in menu or toolbar. Can be null.
	 * @param shell Shell of parent window. Can be null if you don't know it, but call setShell when you do.
	 */
	protected SystemBaseDialogAction(String text, String tooltip, ImageDescriptor image, Shell shell) 
	{
		super(text, tooltip, image, shell);
	}
	
	
	/**
	 * Constructor for SystemBaseDialogAction when translated label and tooltip and description are
	 *  all known. 
	 * @param text string to display in menu or toolbar
	 * @param tooltip string to display when user hovers mouse over action.
	 * @param description string displayed in status bar of some displays. Longer than tooltip.
     * @param image icon to display in menu or toolbar. Can be null.
	 * @param shell Shell of parent window. Can be null if you don't know it, but call setShell when you do.
	 */
	protected SystemBaseDialogAction(String text, String tooltip, String description, ImageDescriptor image, Shell shell) 
	{
		super(text, tooltip, description, image, shell);
	}	



    
    // ------------------------
    // HELPER/GETTER METHODS...
    // ------------------------
    /**
     * Return the help Id destined for the dialog this action brings up
     */
    public String getDialogHelpContextId()
    {
    	return dlgHelpId;
    }
	/**
	 * Get the value, typically set in actionPerformed
	 *  after putting up the dialog, and holds the output
	 *  from the dialog.
	 */
	public Object getValue()
	{
	    return value;
	}
    /**
     * Return true if the action's dialog/wizard is to include a progress monitor
     */
    public boolean getNeedsProgressMonitor()
    {
    	return needsProgressMonitor;
    }
    /**
     * Return true if the caller explicitly called setNeedsProgressMonitor
     */
    protected boolean wasNeedsProgressMonitorSet()
    {
    	return needsProgressMonitorSet;
    }
    /**
     * Return setting of setProcessAllSelections.
     * @see #setProcessAllSelections(boolean)
     */
    protected boolean getProcessAllSelections()
    {
    	return this.processAll;
    }    
	/**
	 * Returns true if the user cancelled the dialog.
	 * The default way to guess at this is to test if the output from
	 *  getDialogValue was null or not. Override if you need to refine this.
	 */
	public boolean wasCancelled()
	{
		if (cancelled) // most accurate
		  return true;
		else
		  return (value == null);
	}

    // -----------------------------------------------------------
    // CONFIGURATION METHODS...
    // -----------------------------------------------------------
   
    /**
     * When using generic dialogs, it is nice to offer non-generic help.
     * If desired, set the help context ID here, and it will be passed on
     *  to the generic dialog after instantiation of it.
     */
    public void setDialogHelp(String id)
    {
    	this.dlgHelpId = id;
    }
	/**
	 * Set the value used as input to the dialog
	 */
	public void setValue(Object value)
	{
	    this.value = value;
	}
    /**
     * If desired, specify if you want to include a progress monitor in your 
     * dialog or wizard. If the dialog is a SystemPromptDialog or the Wizard
     * is a SystemWizardDialog, it will be passed on after instantiating the
     * dialog or wizard.
     */
    public void setNeedsProgressMonitor(boolean needs)
    {
    	this.needsProgressMonitor = needs;
    	this.needsProgressMonitorSet = true;
    }	
    /**
     * If this action supports being enabled for multiple selections (the default,
     *  but changable by calling allowOnMultipleSelections(false)), then by default
     *  the default run() implementation will create and invoke the dialog once for each
     *  item selected. Call this with true to change that behaviour so that the dialog
     *  is only created and processed once.
     * <p>
     * Use this when the dialog itself will process all selected items at once.
     * <p>
     * The default is false.
     */
    public void setProcessAllSelections(boolean all)
    {
    	this.processAll = all;
    }    

    // -----------------------------------------------------------
    // OVERRIDABLE METHODS...
    // -----------------------------------------------------------

	/**
	 * This is the method called by the system when the user
	 *  selects this action. This is a default implementation
	 *  which:
	 * <ul>
	 *  <li>calls abstract method createDialog() to get the dialog
	 *      object. Child classes must implement this method.
	 *  <li>if the returned dialog implements ISystemPromptDialog, then
	 *      it will call setInputObject passing getValue() as the paramter. 
	 *      If getValue returns null, then instead the currently selected objects
	 *      are passed as the parameter. If setProcessAllSelections has been called
	 *      then the current ISelection is passed, else the each object in the selection
	 *      is passed, and the dialog is displayed once per selected object. 
	 *      Your dialog can then cast this input as necessary to initialize its input 
	 *      fields, say. Presumably it knows what to cast it to. Note: code that 
	 *      creates actions does not typically call setValue unless
	 *      the action is used in a UI that has no concept of ISelection, such as a 
	 *      raw swt widget. ISelection is a JFace viewer concept.
	 *  <li>calls dlg.open(getShell()) on the dialog to display it.
	 *  <li>calls setValue(getDialogValue(dlg)) so callers can get the dialog output
	 *      via a call to getValue() on this action object. Again, this is typically only
	 *      used when launching actions from non-viewers, such as launching one dialog from
	 *      another dialog. When launching from popup-menus of viewers, use selectionChanged
	 *      instead. Either way, the object set or selected is passed on to the dialog,
	 *      if not null, by way of a call to the dialog's setInputObject method.
	 * </ul>
	 * If this action is to be enabled when multiple items are selected
	 * (the default) then the processing above is repeated once for every object
	 * selected. If your dialog actually processes all the selected items, then
	 * call setProcessAllSelections(true) to change the behaviour to only do all
	 * of this once. In this case setInputObject will be called with the 
	 * entire IStructuredSelection object, and your dialog code can process each
	 * of the objects in it. 
	 * <p>
	 * Please note that if NO ITEMS are selected, we will still call createDialog
	 * but not call setInput.
	 * <p>
	 * To use this default implementation you must implement
	 *  the createDialog method. Note we will also call
	 *  dlg.setBlockOnOpen(true) on the returned dialog to
	 *  force it to be modal.
	 */
	public void run()
	{		
        Shell shell = getShell();
        if (shell == null)
          SystemBasePlugin.logDebugMessage(this.getClass().getName(),"Warning: shell is null!");
		Object currentSelection = null;
		if (!getProcessAllSelections())
		  currentSelection = getFirstSelection();		
		else
		  currentSelection = getSelection();
        boolean cancelled = false;
      		  
		do
		{
		  Dialog dlg = createDialog(getShell());
		  if (dlg == null)
		    return;
		  dlg.setBlockOnOpen(true);
		  Object dialogInputValue = currentSelection;		  	
		  if (getValue() != null)
		    dialogInputValue = getValue();		      		  
		  if ((dialogInputValue != null) && (dlg instanceof ISystemPromptDialog))
		  {
		    ((ISystemPromptDialog)dlg).setInputObject(dialogInputValue);
		  }
		  if (dlgHelpId!=null) 
		  {
		      if (dlg instanceof SystemPromptDialog)
		        ((SystemPromptDialog)dlg).setHelp(dlgHelpId);
		      else if (dlg instanceof SystemWizardDialog)
		        ((SystemWizardDialog)dlg).setHelp(dlgHelpId);
		  }
		  if (dlg instanceof SystemPromptDialog)
		  {
		     if (needsProgressMonitorSet)
		       ((SystemPromptDialog)dlg).setNeedsProgressMonitor(needsProgressMonitor);
		  }
		  
		  int rc = dlg.open();
		  
		  // if (rc != 0) NOT RELIABLE!
		  if (dlg instanceof SystemWizardDialog)
		  {
		    if (((SystemWizardDialog)dlg).wasCancelled())
		       cancelled = true;
		  	//System.out.println("Testing cancelled state of SystemWizardDialog: " + cancelled);
		  }
		  else if (dlg instanceof SystemPromptDialog)
		  {
		    if (((SystemPromptDialog)dlg).wasCancelled())
		       cancelled = true;
		  	//System.out.println("Testing cancelled state of SystemPromptDialog: " + cancelled);
		  }
		       		       
		  if (!cancelled)
		  {
		    setValue(getDialogValue(dlg));
		  
		    if ((currentSelection != null) && !getProcessAllSelections())
		      currentSelection = getNextSelection();
		    else if (currentSelection != null)
		      currentSelection = null;
		  }
		  else
		    setValue(null);
		} while (!cancelled && (currentSelection != null));
	}

    // -----------------------------------------------------------
    // ABSTRACT METHODS...
    // -----------------------------------------------------------
	/**
	 * If you decide to use the supplied run method as is,
	 *  then you must override this method to create and return
	 *  the dialog that is displayed by the default run method
	 *  implementation.
	 * <p>
	 * If you override actionPerformed with your own, then
	 *  simply implement this to return null as it won't be used.
	 * @see #run()
	 */
	protected abstract Dialog createDialog(Shell parent);
	/**
	 * If you decide to use the supplied run method as is,
	 *  then you must override this method to retrieve the data
	 *  from the dialog. For SystemPromptDialog dialogs, this is simply
	 *  a matter of returning dlg.getOutputObject();
	 * <p>
	 * This is called by the run method after the dialog returns, and
	 * wasCancelled() is false. Callers of this object can subsequently 
	 * retrieve this returned value by calling getValue. If you don't need
	 * to pass a value back to the caller of this action, simply return null
	 * from this method.
	 * 
	 * @param dlg The dialog object, after it has returned from open.
	 */
	protected abstract Object getDialogValue(Dialog dlg);
}