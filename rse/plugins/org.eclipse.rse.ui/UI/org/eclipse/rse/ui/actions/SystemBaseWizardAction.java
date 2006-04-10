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
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rse.ui.dialogs.SystemWizardDialog;
import org.eclipse.rse.ui.wizards.ISystemWizard;
import org.eclipse.swt.widgets.Shell;



/**
 * A specialization for the eclipse Action method, for actions that put up wizards.
 * <p> This class is most effective when used with actions that extend {@link org.eclipse.rse.ui.wizards.AbstractSystemWizard},
 * or implement {@link org.eclipse.rse.ui.wizards.ISystemWizard}. It supports 
 * settings that are propogated to the wizard, and wizard pages if they extend {@link org.eclipse.rse.ui.wizards.AbstractSystemWizardPage},
 * or implement {@link org.eclipse.rse.ui.wizards.ISystemWizardPage}.
 * </p>
 * The advantages to using this class over the base action class are:
 * <ul>
 *  <li>Supports setting the label, description, image and tooltip text for this action, via constructors.
 *  <li>Sets {@link org.eclipse.rse.ui.SystemBaseAction#allowOnMultipleSelection(boolean)} to false, since most wizard actions are not permitted on multiple selection.
 *  <li>Supports setting whether the wizard requires a {@link org.eclipse.rse.ui.actions.SystemBaseDialogAction#setNeedsProgressMonitor() progress-monitor} or not, which is propogated to the wizard.
 *  <li>Supports setting the overall wizard {@link #setWizardTitle(String) title}, which is propogated to the wizard.
 *  <li>Supports setting the overall wizard {@link #setWizardImage(ImageDescriptor) image}, which is propogated to the wizard.
 *  <li>Supports setting the default wizard {@link #setWizardPageTitle(String) page-title}, which is propogated to the wizard and the wizard pages.
 *  <li>Supports setting the wizard;s default {@link #setHelp(String) contextual-help}, which is propogated to the wizard and the wizard pages.
 *  <li>Supports setting the wizard's {@link #setMinimumPageSize(int,int) minimum-size}, which is propogated and applied to the wizard.
 *  <li>Supports setting an {@link org.eclipse.rse.ui.SystemBaseAction#setInputObject(Object) input-object}, which is propogated to the wizard and wizard pages. By default, this 
 *       is set the current StructuredSelection.
 *  <li>Supports querying a {@link org.eclipse.rse.ui.SystemBaseAction#getOutputObject() output-object} which is set by the wizard class.
 *  <li>Supports a {@link #wasCancelled()} method so the caller can easily determine if the wizard was dismissed or cancelled by the user.
 *  <li>Supports propogation of the {@link org.eclipse.rse.ui.SystemBaseAction#getViewer() current-viewer}.
 * </ul>
 * 
 * <p>To use this class:</p>
 * <ol>
 *  <li>Create your wizard class that extends {@link org.eclipse.rse.ui.wizards.AbstractSystemWizard}, and is populated with
 *       pages that extend {@link org.eclipse.rse.ui.wizards.AbstractSystemWizardPage}.
 *  <li>Define your action's label and tooltip translatable strings in a resource bundle, where the former's key ends with "label" and the latter's
 *       key ends with "tooltip".
 *  <li>Define your subclass of this wizard. Decide if you want to set the wizard's title, page-title, image, help and optionally minimum size
 *       in your wizard subclass, or here in your action subclass. For re-use it is best to set it in the wizard, if possible.
 *  <li>Override {@link #createWizard()} to instantiate, configure and return your wizard.
 *  <li>Decide if you will do the work of the wizard in the wizard's performFinish method, or here in this action subclass. If here,
 *       override {@link #postProcessWizard(IWizard)}. You might also override this if you need to set output variables in the action,
 *       after the sucessful completion of the wizard.
 * </ol>
 */
public abstract class SystemBaseWizardAction extends SystemBaseDialogAction 
                                             implements ISystemWizardAction
{
	
    private IWizard newWizard;
    private String wizardTitle, pageTitle;
    private ImageDescriptor wizardImage;
    private int minPageWidth=-1, minPageHeight=-1;
    
	/**
	 * Constructor for SystemBaseWizardAction when translated label is known. You must separately
	 *  call setToolTipText and setDescription to enable these if desired.
	 * @param text string to display in menu or toolbar
	 * @param image icon to display in menu or toolbar. Can be null.
	 * @param parent Shell of parent window. Can be null if you don't know it, but call setShell when you do.
	 */
	protected SystemBaseWizardAction(String text, ImageDescriptor image, Shell parent) 
	{
		super(text, image, parent);
		allowOnMultipleSelection(false);
	}
	/**
	 * Constructor for SystemBaseWizardAction when translated label and tooltip are known. You must
	 *  separately call setDescription to enable this if desired.
	 * @param text string to display in menu or toolbar
	 * @param tooltip string to display when user hovers mouse over action.
     * @param image icon to display in menu or toolbar. Can be null.
	 * @param parent Shell of parent window. Can be null if you don't know it, but call setShell when you do.
	 */
	protected SystemBaseWizardAction(String text, String tooltip, ImageDescriptor image, Shell parent) 
	{
		super(text, tooltip, image, parent);
		allowOnMultipleSelection(false);		
	}
	/**
	 * Constructor for SystemBaseWizardAction when translated label and tooltip and description are
	 *  all known. 
	 * @param text string to display in menu or toolbar
	 * @param tooltip string to display when user hovers mouse over action.
	 * @param description string displayed in status bar of some displays. Longer than tooltip.
     * @param image icon to display in menu or toolbar. Can be null.
	 * @param parent Shell of parent window. Can be null if you don't know it, but call setShell when you do.
	 */
	protected SystemBaseWizardAction(String text, String tooltip, String description, ImageDescriptor image, Shell parent) 
	{
		super(text, tooltip, description, image, parent);
		allowOnMultipleSelection(false);		
	}	


	
	/**
	 * Set the wizard title. Using this makes it possible to avoid subclassing a wizard
	 */
	public void setWizardTitle(String title)
	{
		this.wizardTitle = title;
	}
	/**
	 * Set the wizard image. Using this makes it possible to avoid subclassing a wizard
	 */
	public void setWizardImage(ImageDescriptor wizardImage)
	{
		this.wizardImage = wizardImage;
	}
	/**
	 * Set the wizard page title. Using this makes it possible to avoid subclassing.
	 * The page title goes below the wizard title, and can be unique per page. However,
	 * typically the wizard page title is the same for all pages... eg "Filter".
	 * <p>
	 * This is not used by default, but can be queried via getPageTitle() when constructing
	 *  pages.
	 */
	public void setWizardPageTitle(String pageTitle)
	{
		this.pageTitle = pageTitle;
	}
	/**
	 * Return the page title as set via setWizardPageTitle
	 */
	public String getWizardPageTitle()
	{
		return pageTitle;
	}		
	/**
	 * Call this method to set the wizard's dimensions without having to subclass the wizard.
	 * If you pass zero for either value, then the default will be used for that.
	 */
	public void setMinimumPageSize(int width, int height)
	{
    	//if (width <= 0)
    	//  width = 300; // found this number in WizardDialog code
    	//if (height<= 0)
    	//  height = 225; // found this number in WizardDialog code	        
		this.minPageWidth = width;
		this.minPageHeight = height;
	}
	/**
	 * Override of parent's method. Does the following:
	 * <ul>
	 *   <li>Calls abstract createWizard() method to get wizard instance
	 *   <li>If wizard implements ISystemWizard, calls setInputValue(...), passing in
	 *        getValue() if not null, else passing in getFirstSelection() if not null.
	 *   <li>Instantiates a WizardDialog object with the wizard object from createWizard().
	 *        Returns this to run() in our parent class which then opens this dialog.
	 * </ul>
	 * 
	 */
	protected Dialog createDialog(Shell shell)
	{
		newWizard = createWizard();
		    
		if ((newWizard instanceof Wizard) && wasNeedsProgressMonitorSet())
		  ((Wizard)newWizard).setNeedsProgressMonitor(getNeedsProgressMonitor());
		  
		if (newWizard instanceof Wizard)
		{
          if (wizardTitle != null)
		    ((Wizard)newWizard).setWindowTitle(wizardTitle);
	  	  if (wizardImage != null)
		    ((Wizard)newWizard).setDefaultPageImageDescriptor(wizardImage);
		}

		
	    WizardDialog dialog = null;	    
	    
	    if (newWizard instanceof ISystemWizard)
	    {	    		      	
	      ISystemWizard swizard = (ISystemWizard)newWizard;
	      if (pageTitle != null)
	        swizard.setWizardPageTitle(pageTitle);
	      swizard.setViewer(getViewer());
	      dialog = new SystemWizardDialog(shell,swizard);		
	      int w = swizard.getMinimumPageWidth();
	      int h = swizard.getMinimumPageHeight();	      
	      if (minPageWidth > 0)
	        w = minPageWidth;
	      if (minPageHeight > 0)
	        h = minPageHeight;
	      //System.out.println("In SystemBaseWizardAction. minPageWidth = " + w + ", minPageHeight = " + h);
	      if ((w>0) && (h>0))
	        dialog.setMinimumPageSize(w,h);
		  
		  /*
		   * Don't do the following here as it is redundant! The run method in the parent SystemBaseDialogAction
		   *  does this already
		  Object wizardInputValue = null;		  	
		  if (getValue() != null)
		    wizardInputValue = getValue();		      		  
		  else
		    wizardInputValue = getFirstSelection();		    	      
		  if (wizardInputValue != null)
		    ((SystemWizardDialog)dialog).setInputObject(wizardInputValue);
		  */
	    }
	    else
	      dialog = new WizardDialog(shell,newWizard);	      
	    
	    return dialog;
	}
	
	/**
	 * The default processing for the run method calls createDialog, which
	 *  we override in this class. The implementation of createDialog calls
	 *  this method that you must override, to create the wizard. The result
	 *  goes into a WizardDialog which is opened and hence displayed to the 
	 *  user.
	 */
	protected abstract IWizard createWizard();	
	
	/**
	 * By default, we try to get the wizard's value by calling getOutputObject()
	 */
	protected Object getDialogValue(Dialog dlg)
	{
	    postProcessWizard(newWizard);
		if (newWizard instanceof ISystemWizard)
		{
			ISystemWizard ourWizard = (ISystemWizard)newWizard;
		    return ourWizard.getOutputObject();
		}
		else
		    return null;
	}

    /**
     * Typically, the wizard's performFinish method does the work required by
     *  a successful finish of the wizard. However, often we also want to be
     *  able to extract user-entered data from the wizard, by calling getters
     *  in this action. To enable this, override this method to populate your
     *  output instance variables from the completed wizard, which is passed
     *  as a parameter. This is only called after successful completion of the
     *  wizard.
     */
    protected void postProcessWizard(IWizard wizard)
    {
    }
    
	/**
	 * Returns true if the user cancelled the wizard.
	 * This is an override of the parent method, since we can be more
	 *  accurate with wizards than we can with dialogs.
	 */
	public boolean wasCancelled()
	{
		if (newWizard instanceof ISystemWizard)
		{
			ISystemWizard ourWizard = (ISystemWizard)newWizard;
		    return ourWizard.wasCancelled();
		}
		else
		  return super.wasCancelled();
	}    
}