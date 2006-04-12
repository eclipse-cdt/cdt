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

package org.eclipse.rse.ui.wizards;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.dialogs.SystemWizardDialog;
import org.eclipse.rse.ui.view.ISystemTree;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;


/**
 * Base class for all RSE wizards. This class is more beneficial when using in conjunction with
 *  {@link org.eclipse.rse.ui.wizards.AbstractSystemWizardPage}, and 
 *  {@link org.eclipse.rse.ui.actions.SystemBaseWizardAction}.
 * <p>A wizard is a multi-page UI, that prompts users for information and then uses that information
 *  to create something (typically). The wizard has an overall title that shows for each page, and
 *  a wizard page title that can be unique per page, but typically is not. Typically, the overall title
 *  is a verb, such as "New", while the page title expands on the verb, as in "File". There is also a 
 *  description per page, which is unique and explains the purpose of that page. Further, there is a 
 *  wizard image that is always the same per wizard page.
 * </p>
 * <p> Using this base class for wizards offers the following advantages over just using the 
 * eclipse Wizard class:
 * </p>
 * <ul>
 *  <li>Designed to work in conjunction with the {@link org.eclipse.rse.ui.actions.SystemBaseWizardAction},
 *      and {@link org.eclipse.rse.ui.dialogs.SystemWizardDialog} classes, propogating settings 
 *      from these to the wizard, and further propogating those to the individual wizard pages.
 *  <li>The overall wizard title and image can be set via the constructor or setter methods.
 *  <li>Supports setting an input object, and getting an output object. This is exploited by the 
 *      {@link org.eclipse.rse.ui.actions.SystemBaseWizardAction} class, when
 *      this wizard is launched from a subclass of that action class.
 *  <li>Supports setting a wizard page title via {@link #setWizardPageTitle(String)}, that all 
 *      {@link org.eclipse.rse.ui.wizards.AbstractSystemWizardPage} pages will use by default for 
 *      their title, if the non-title constructor is used for that page.
 *  <li>If being hosted by a {@link org.eclipse.rse.ui.dialogs.SystemWizardDialog}, supports dynamically
 *      updating the wizard size via {@link #updateSize()}, if dynamic content is added to a wizard page, causing the page to grow beyond its initial size.
 *  <li>Supports a {@link #setHelp(String)} method to set the overall wizard's contextual help. This is propogated to each
 *      {@link org.eclipse.rse.ui.wizards.AbstractSystemWizardPage} as it is added.
 *  <li>Supports setting the viewer that launched this wizard, as wizards often need to know this so they can update the UI upon Finish.
 *  <li>Supports a {@link #wasCancelled()} method so callers can easily test if the wizard was cancelled/dismissed by the user. 
 *  <li>Supports a {@link #setMinimumPageSize(int, int)} method to set the minimum width and height of the wizard.
 *  <li>Supports a {@link #setPageError(IWizardPage)} method that can be called in performFinish when an error is detected on a 
 *      non-current page. This issues a message telling the user there is an error on another page.
 *  <li>Supports a simple {@link #setBusyCursor(boolean)} method to toggle the cursor between busy and normal
 * </ul>
 * <p>To use this class, :</p>
 * <ol>
 *  <li>Subclass it.
 *  <li>In your constructor, call {@link #setHelp(String)} to set the overall help, if desired.
 *  <li>In your constructor, call {@link #setWizardPageTitle(String)} to set the title to use for all pages, if desired.
 *  <li>Override {@link #addPages()} to add your {@link org.eclipse.rse.ui.wizards.AbstractSystemWizardPage pages} via calls to addPage(IWizardPage).
 *  <li>Override {@link #performFinish()} to do the wizard's main task when Finish is pressed. Cycle through each of your pages calling
 *       performFinish() on each of them. If performFinish() returns false from any page, call {@link #setPageError(IWizardPage)} and 
 *       return false from your performFinish() method to cancel the Finish operation. 
 * </ol>
 * 
 * @see org.eclipse.rse.ui.wizards.AbstractSystemWizardPage
 * @see org.eclipse.rse.ui.dialogs.SystemWizardDialog
 * @see org.eclipse.rse.ui.actions.SystemBaseWizardAction
 */
public abstract class      AbstractSystemWizard 
				extends    Wizard implements ISystemWizard			
{
	protected boolean finishPressed = true;	// most accurate guess
	protected boolean cancelled = false;	
	protected Object  input = null;
	protected Object  output = null;	
	protected IStructuredSelection selection = null;
	protected int minPageWidth, minPageHeight;
	protected String helpId;
	protected Viewer  viewer = null;	
    protected String  pageTitle;	
    protected SystemWizardDialog owningDialog;
	private   Cursor waitCursor;

    /**
     * Default constructor.
     * 
     * @see #setWizardTitle(String)
	 * @see #setWizardImage(ImageDescriptor)
     * @see #setWizardPageTitle(String)
     */
	public AbstractSystemWizard()
	{
		super();
		
	}
	
    /**
     * Constructor when wizard title is known.
     * Alternatively, you can call {@link #setWizardTitle(String)}
     * 
	 * @see #setWizardImage(ImageDescriptor)
     * @see #setWizardPageTitle(String)
     */
	public AbstractSystemWizard(String title)
	{
		super();
		setWindowTitle(title);
	}
	
	/**
	 * Constructor when you both a title and an image for this wizard.
	 * Alternatively, you can call {@link #setWizardTitle(String)} or {@link #setWizardImage(ImageDescriptor)}
	 * 
     * @see #setWizardPageTitle(String)
	 */
	public AbstractSystemWizard(String title, ImageDescriptor wizardImage)
	{
		super();
		setWindowTitle(title);
		setDefaultPageImageDescriptor(wizardImage);
	}
	
	/**
	 * Called from {@link org.eclipse.rse.ui.dialogs.SystemWizardDialog} when it is used as the hosting dialog
	 */
	public void setSystemWizardDialog(SystemWizardDialog dlg)
	{
		this.owningDialog = dlg;
	}
	/**
	 * Return the result of {@link #setSystemWizardDialog(SystemWizardDialog)}
	 */
	public SystemWizardDialog getSystemWizardDialog()
	{
		return owningDialog;
	}
    /**
     * Exposes this nice new 2.0 capability to the public. 
     * Only does anything if being hosted by SystemWizardDialog.
     */
    public void updateSize() 
    {
    	if (owningDialog != null)
    	  owningDialog.updateSize(getContainer().getCurrentPage());
    }    
	/**
	 * Set the wizard title. Using this makes it possible to avoid subclassing.
	 * Typically the wizard title is the same for all pages... eg "New"
	 */
	public void setWizardTitle(String title)
	{
		setWindowTitle(title);
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
	 * Set the wizard image. Using this makes it possible to avoid subclassing
	 */
	public void setWizardImage(ImageDescriptor wizardImage)
	{
		super.setDefaultPageImageDescriptor(wizardImage);
	}
    /**
     * Set the help context Id (infoPop) for this wizard. This must be fully qualified by
     *  plugin ID.
     * <p>
     * Same as {@link org.eclipse.rse.ui.actions.SystemBaseAction #setHelp(String)}
     * @see org.eclipse.rse.ui.actions.SystemBaseAction #getHelpContextId()
     */
    public void setHelp(String id)
    {
    	this.helpId = id;
    }
    /**
     * Return the help Id as set in setHelp(String)
     */
    public String getHelpContextId()
    {
    	return helpId;
    }
	/**
	 * Intercept of parent method so we can percolate the help id
	 */
	public void addPage(IWizardPage page)
	{
		super.addPage(page);
		if ((helpId!=null) && (page instanceof ISystemWizardPage))
		{
			// tweak by Phil 10/19/2002 ... this was overriding page-specific help
			// on secondary pages. To reduce regression I only test if help is already
			// specified if this is not the first page... hence one-page wizards are
			// not affected...
			ISystemWizardPage swPage = (ISystemWizardPage)page;
			if ((super.getPageCount() == 1) || (swPage.getHelpContextId() == null))
		      swPage.setHelp(helpId);
		}
	}

	/**
	 * Set the Viewer that called this wizard. It is good practice for actions to call this
	 *  so wizard can directly access the originating viewer if needed.
	 * <p>
	 * This is called for you if using a subclass of {@link org.eclipse.rse.ui.actions.SystemBaseWizardAction}.
	 */
	public void setViewer(Viewer v)
	{
		this.viewer = v;
	}
	/**
	 * Get the Viewer that called this wizard. This will be null unless set by the action that started this wizard.
	 */
	public Viewer getViewer()
	{
		return viewer;
	}
	/**
	 * Return the current viewer as an ISystemTree if it is one, or null otherwise
	 */
	protected ISystemTree getCurrentTreeView()
	{
		  Viewer v = getViewer();
		  if (v instanceof ISystemTree)
            return (ISystemTree)v;
          else
            return null;
	}

	/**
	 * For explicitly setting input object
	 */
	public void setInputObject(Object inputObject)
	{
		this.input = inputObject;		
		//System.out.println("Inside AbstractSystemWizard#setInputObject: " + inputObject + ", class = " + inputObject.getClass().getName());
	}
	/**
	 * For explicitly getting input object
	 */
	public Object getInputObject()
	{
		return input;
	}
	
	/**
	 * For explicitly getting output object after wizard is dismissed. Set by the
	 * wizard's processFinish method.
	 */
	public Object getOutputObject()
	{
		return output;
	}
	
	/**
	 * For explicitly setting output object after wizard is dismissed. Called in the
	 * wizard's processFinish method, typically.
	 */
	protected void setOutputObject(Object outputObject)
	{
		output = outputObject;
	}	
	
	/**
	 * Allow caller to determine if wizard was cancelled or not.
	 */
	public boolean wasCancelled()
	{
		if (cancelled) // most reliable
		  return true;
		else
		  return !finishPressed;
	}
	/**
	 * You must call this in your performFinish method.
	 */
	protected void setWasCancelled(boolean cancelled)
	{		
		finishPressed = !cancelled;
	}	
	/**
	 * Override of parent so we can record the fact the wizard was cancelled.
	 */
    public boolean performCancel() 
    {
    	 //System.out.println("inside performCancel");
    	 cancelled = true;
    	 setWasCancelled(true);
	     return super.performCancel();
    }
	
    /**
     * Required by INewWizard interface. It is called by the framework for wizards
     *  that are launched file the File->New interface. Otherwise we don't use it.
     * If you need it, the selection is stored in protected variable "selection".
    */
    public void init(IWorkbench workbench, IStructuredSelection selection)	
    {
    	this.selection = selection;        
    }	
    
    /**
     * Set the wizard's min page width and height.
     * If you pass 0 for either one, the Eclipse default value will be used.
     */
    public void setMinimumPageSize(int width, int height)
    {
    	if (width <= 0)
    	  width = 300; // found this number in WizardDialog code
    	if (height <= 0)
    	  height = 225; // found this number in WizardDialog code
    	this.minPageWidth = width;
    	this.minPageHeight = height;
    }
    
    /**
     * Return the minimum page width. If zero, it has not been explicitly set, so the default is to be used.
     */    
    public int getMinimumPageWidth()
    {
    	return minPageWidth;
    }
    /**
     * Return the minimum page height. If zero, it has not been explicitly set, so the default is to be used.
     */    
    public int getMinimumPageHeight()
    {
    	return minPageHeight;
    }
    
    /**
     * If in the processing of performFinish an error is detected on another page of the
     *  wizard, the best we can do is tell the user this via an error message on their own
     *  page. It seems there is no way in JFace to successfully switch focus to another page.
     * <p>
     * To simplify processing, simply call this method in your wizard's performFinish if any
     *  page's performFinish returned false. Pass the failing page. If it is not the current
     *  page, this code will issue msg RSEG1240 "Error on another page" to the user.
     */
    protected void setPageError(IWizardPage pageInError)
    {
    	IWizardPage currentPage = getContainer().getCurrentPage();
    	if (currentPage != pageInError)
    	{
    		SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_WIZARD_PAGE_ERROR);
    		if (currentPage instanceof AbstractSystemWizardPage)
    		  ((AbstractSystemWizardPage)currentPage).setErrorMessage(msg);
    		else if (pageInError instanceof WizardPage)
    		  ((WizardPage)currentPage).setErrorMessage(msg.getLevelOneText());
    	}
    }
    
    /**
     * Expose inherited protected method convertWidthInCharsToPixels as a publicly
     *  excessible method
     * <p>
     * Requires setOwningDialog to have been called, else returns -1
     */
    public int publicConvertWidthInCharsToPixels(int chars) 
    {
    	if (owningDialog != null)
    	  return owningDialog.publicConvertWidthInCharsToPixels(chars);
    	else
    	  return -1;
    }
    /**
     * Expose inherited protected method convertHeightInCharsToPixels as a publicly
     *  excessible method
     * <p>
     * Requires setOwningDialog to have been called, else returns -1
     */
    public int publicConvertHeightInCharsToPixels(int chars) 
    {
    	if (owningDialog != null)
    	  return owningDialog.publicConvertHeightInCharsToPixels(chars);
    	else
    	  return -1;
    }        
	/**
	 * Set the cursor to the wait cursor (true) or restores it to the normal cursor (false).
	 */
	public void setBusyCursor(boolean setBusy)
	{
		if (setBusy)
		{
          // Set the busy cursor to all shells.
    	  Display d = getShell().getDisplay();
    	  waitCursor = new Cursor(d, SWT.CURSOR_WAIT);
		  org.eclipse.rse.ui.dialogs.SystemPromptDialog.setDisplayCursor(getShell(), waitCursor);
		}
		else
		{
		  org.eclipse.rse.ui.dialogs.SystemPromptDialog.setDisplayCursor(getShell(), null);
		  if (waitCursor != null)
		    waitCursor.dispose();
		  waitCursor = null;
		}
	}
    
    // ----------------------------
    // METHODS YOU MUST OVERRIDE...
    // ----------------------------
    
	/**
	 * Creates the wizard pages.
	 * This method is an override from the parent Wizard class, but is made abstract here to ensure child classes override it.
	 */
	public abstract void addPages();

	/**
	 * Called when finish pressed.
	 * <p>
	 * Return true if no errors, false to cancel the finish operation. 
	 * <p>
	 * Typically, you walk through each wizard page calling performFinish on it, and only return true if they all return true.
	 * If one of the pages returns false, you should call setPageError(IWizardPage), which shows a message to the user about an 
	 *  error pending on another page, if the given page is not the current page.
	 */
	public abstract boolean performFinish();	
    
}