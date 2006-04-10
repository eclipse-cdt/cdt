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

package org.eclipse.rse.ui.dialogs;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.ui.wizards.ISystemWizard;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


/**
 * Base wizard dialog class. Extends Eclipse WizardDialog class to add 
 * support for the ISystemPromptDialog interface methods. These make it
 * easy to pass an input object to your wizard, if your wizard implements
 * ISystemWizard.
 * <p>This class is most effective when used together with {@link org.eclipse.rse.ui.wizards.AbstractSystemWizard} and
 * with {@link org.eclipse.rse.ui.actions.SystemBaseWizardAction}. Indeed,
 * if you use SystemBaseWizardAction, this class is automatically used for the dialog. It supports
 * propogation of information from the action, to the wizard, to the wizard dialog and to the wizard pages.
 * </p>
 * <p>The advantages to using this class versus the base JFace WizardDialog class is:
 * </p>
 * <ul>
 *  <li>Supports settings of an input object, which is propogated to the wizard and its pages.
 *  <li>Supports querying of an output object, as set by the wizard.
 *  <li>Supports a {@link #wasCancelled()} method for callers to know if the wizard was dismissed or cancelled by the user.
 *  <li>Supports setting of the overall contextual help, which is propogated to each page.
 *  <li>Supports an {@link #updateSize(IWizardPage)} method that can be called by a page when it dynamically grows the page size.
 *  <li>When a progress monitor is not required, the real estate for the monitor is reclaimed, reducing the size of the wizard
 *  <li>Whan a progress monitor is required, registers this with the RSE system registry, so it is using for long-running remote communications requests.
 * </ul>
 * 
 * <p>To use this class, simply instantiate it, passing a wizard that implements {@link org.eclipse.rse.ui.wizards.ISystemWizard}, 
 *  which {@link org.eclipse.rse.ui.wizards.AbstractSystemWizard} does. If you use {@link org.eclipse.rse.ui.actions.SystemBaseWizardAction},
 *  then this is done for you.
 * 
 * @see org.eclipse.rse.ui.wizards.AbstractSystemWizard
 * @see org.eclipse.rse.ui.actions.SystemBaseWizardAction
 */
public class SystemWizardDialog
	   extends WizardDialog 
	   implements ISystemPromptDialog
{
	protected ISystemWizard wizard;
	protected String helpId;
	
	/**
	 * Constructor
	 */
	public SystemWizardDialog(Shell shell, ISystemWizard wizard)
	{
		super(shell, wizard);
		this.wizard = wizard;
		wizard.setSystemWizardDialog(this);
	}
	/**
	 * Constructor two. Use when you have an input object at instantiation time.
	 */
	public SystemWizardDialog(Shell shell, ISystemWizard wizard, Object inputObject)
	{
		super(shell,wizard);
		this.wizard = wizard;		
		setInputObject(inputObject);
		wizard.setSystemWizardDialog(this);
	}
    
	/**
	 * For explicitly setting input object. Called by SystemDialogAction
	 */
	public void setInputObject(Object inputObject)
	{
		wizard.setInputObject(inputObject);
	}
	/**
	 * For explicitly getting input object.
	 */
	public Object getInputObject()
	{
		return wizard.getInputObject();
	}
	
	/**
	 * For explicitly getting output object after wizard is dismissed. Set by the
	 * dialog's processOK method.
	 */
	public Object getOutputObject()
	{
		
		return wizard.getOutputObject();
	}
	
	/**
	 * Allow caller to determine if wizard was cancelled or not.
	 */
	public boolean wasCancelled()
	{
		//System.out.println("Inside wasCancelled of SystemWizardDialog: " + wizard.wasCancelled());
		return wizard.wasCancelled();
	}
	
    /**
     * Set the help context id for this wizard dialog
     */
    public void setHelp(String id)
    {
    	helpId = id;
    	if (wizard instanceof ISystemWizard)
    	  ((ISystemWizard)wizard).setHelp(id);
    }
    
	/**
	 * Get the help context id for this wizard dialog, as set in setHelp
	 */
	public String getHelpContextId()
	{
		return helpId;
	}
	
	/**
	 * Intercept of parent method so we can automatically register the wizard's progress monitor
	 *  with the SystemRegistry for all framework progress monitor requests, if user has specified
	 *  they need a progress monitor for this wizard.
	 */
    protected Control createDialogArea(Composite parent) 
    {
    	boolean needsMonitor = wizard.needsProgressMonitor();
    	Control ctrl = super.createDialogArea(parent);
        if (!needsMonitor)
        {
        	IProgressMonitor pm = getProgressMonitor();
        	((ProgressMonitorPart)pm).dispose();
        }
        if (needsMonitor && SystemPlugin.isTheSystemRegistryActive())
        {
        	SystemPlugin.getTheSystemRegistry().setRunnableContext(getShell(), this);
	        // add a dispose listener
	        getShell().addDisposeListener(new DisposeListener() 
	        {
		      public void widgetDisposed(DisposeEvent e) 
		      {
        	      SystemPlugin.getTheSystemRegistry().clearRunnableContext();		      	
		      }
 	        });
        }
        return ctrl;	
    }
    
    /**
     * Exposes this nice new 2.0 capability to the public. 
     */
    public void updateSize(IWizardPage page) 
    {
    	super.updateSize(page);
    }    
    
    /**
     * Expose inherited protected method convertWidthInCharsToPixels as a publicly
     *  excessible method
     */
    public int publicConvertWidthInCharsToPixels(int chars) 
    {
    	return convertWidthInCharsToPixels(chars);
    }
    /**
     * Expose inherited protected method convertHeightInCharsToPixels as a publicly
     *  excessible method
     */
    public int publicConvertHeightInCharsToPixels(int chars) 
    {
    	return convertHeightInCharsToPixels(chars);
    }    
}