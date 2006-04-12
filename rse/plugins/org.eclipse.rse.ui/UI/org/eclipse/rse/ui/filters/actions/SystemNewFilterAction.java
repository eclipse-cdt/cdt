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
import java.util.Vector;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterContainer;
import org.eclipse.rse.filters.ISystemFilterContainerReference;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.filters.ISystemFilterPoolSelectionValidator;
import org.eclipse.rse.filters.ISystemFilterPoolWrapperInformation;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemBaseWizardAction;
import org.eclipse.rse.ui.filters.SystemFilterStringEditPane;
import org.eclipse.rse.ui.filters.dialogs.SystemNewFilterWizard;
import org.eclipse.swt.widgets.Shell;


/**
 * The action acts as a base class for all "New Filter" wizards so we can
 *  get some common functionality.
 * <p>
 * An interesting capability of this action is to defer configuration, which might be
 *  time consuming, until the user selects to run it. That can be done by registering
 *  a callback object that implements ISystemNewFilterActionConfigurator.
 */
public class SystemNewFilterAction 
                extends SystemBaseWizardAction 
                
{
    protected ISystemFilterPool parentPool;
    protected ISystemFilterPool[] poolsToSelectFrom;
    protected ISystemFilterPoolWrapperInformation poolWrapperInformation;
    protected boolean nested = false;
    protected boolean showFilterStrings = true;
    protected boolean showNamePrompt = true;
    protected boolean showInfoPage = true;
    protected boolean fromRSE = false;
    protected String[] defaultFilterStrings;
    protected String  type = null;
    protected String  verbage = null;
    protected String  page1Description;
    protected String  namePageHelp;
    protected ISystemFilterPoolSelectionValidator filterPoolSelectionValidator; 
    protected ISystemNewFilterActionConfigurator callbackConfigurator;
    protected boolean callbackConfiguratorCalled = true;
    protected Object callbackData = null;
    protected SystemFilterStringEditPane editPane;
    
	/**
	 * Constructor for non-nested actions.
	 */
	public SystemNewFilterAction(Shell shell, ISystemFilterPool parentPool,
	                                       String label, String tooltip, ImageDescriptor image)	
	{
		this(shell, parentPool, label, tooltip, image, false);		
	}
	/**
	 * Constructor allowing nested actions. Changes the title.
	 */
	public SystemNewFilterAction(Shell shell, ISystemFilterPool parentPool, 
	                                       String label, String tooltip, ImageDescriptor image,
	                                       boolean nested) 
	{
		super(label, tooltip, image, shell);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_NEW);
		this.parentPool = parentPool;
		this.nested = nested;
		setAvailableOffline(true);		
	}		
	/**
	 * Constructor to use when you want to just use the default action name and image.
	 * Also defaults to nested filters not allowed.
	 */
	public SystemNewFilterAction(Shell shell, ISystemFilterPool parentPool) 
	{
		this(shell, parentPool, SystemResources.ACTION_NEWFILTER_LABEL, SystemResources.ACTION_NEWFILE_TOOLTIP,
		      RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWFILTER_ID), false);
	}		
	
	// ------------------------
	// CONFIGURATION METHODS...
	// ------------------------
	
	/**
	 * <i>Configuration method. Do not override.</i><br>
	 * Call this to defer expensive configuration until the user runs the action
	 * @param caller - an implementor of the callback interface
	 * @param data - any data the callback needs. It will be passed back on the callback.
	 */
	public void setCallBackConfigurator(ISystemNewFilterActionConfigurator caller, Object data)
	{
		this.callbackConfigurator = caller;
		this.callbackData = data;
		this.callbackConfiguratorCalled = false;
	}
    /**
	 * <i>Configuration method. Do not override.</i><br>
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
	/**
	 * <i>Configuration method. Do not override.</i><br>
	 * Set the parent filter pool that the new-filter actions need.
	 * Typically this is set at constructor time but it can be set later if re-using the action.
	 */
	public void setParentFilterPool(ISystemFilterPool parentPool)
	{
		this.parentPool = parentPool;
	}	
    /**
	 * <i>Configuration method. Do not override.</i><br>
     * If you want to prompt the user for the parent filter pool to create this filter in, 
     *  call this with the list of filter pools. In this case, the filter pool passed into
     *  the constructor will be used as the initial selection.
     */
    public void setAllowFilterPoolSelection(ISystemFilterPool[] poolsToSelectFrom)
    {
    	this.poolsToSelectFrom = poolsToSelectFrom;
    }
    /**
	 * <i>Configuration method. Do not override.</i><br>
     * This is an alternative to {@link #setAllowFilterPoolSelection(ISystemFilterPool[])}
     * <p>
     * If you want to prompt the user for the parent filter pool to create this filter in, 
     *  but want to not use the term "pool" say, you can use an array of euphamisms. That is,
     *  you can pass an array of objects that map to filter pools, but have a different 
     *  display name that is shown in the dropdown.
     * <p>
     * Of course, if you want to do this, then you will likely want to offer a different
     *  label and tooltip for the prompt, and different verbage above the prompt. The 
     *  object this method accepts as a parameter encapsulates all that information, and
     *  there is a default class you can use for this.
     */
    public void setAllowFilterPoolSelection(ISystemFilterPoolWrapperInformation poolsToSelectFrom)
    {
    	this.poolWrapperInformation = poolsToSelectFrom;
    }
    
    /**
	 * <i>Configuration method. Do not override.</i><br>
     * Set the type of filter we are creating. Results in a call to setType(String) on the new filter.
     * Types are not used by the base filter framework but are a way for tools to create typed
     * filters and have unique actions per filter type.
     * <p>This will also result in a call to setType(String) on the filter string edit pane, which
     * sets the <samp>type</samp> instance variable in case your edit pane subclass needs to know.
     */
    public void setType(String type)
    {
    	this.type = type;
    }
    /**
     * <i>Getter method. Do not override.</i><br>
     * Get the type of filter as set by {@link #setType(String)}
     */
    public String getType()
    {
    	return type;
    }
    /**
	 * <i>Configuration method. Do not override.</i><br>
     * Set whether to show, or hide, the first page that prompts for a filter string. Default is true.
     * @see #setDefaultFilterStrings(String[])
     */
    public void setShowFilterStrings(boolean show)
    {
    	showFilterStrings = show;
    }
	/**
	 * <i>Configuration method. Do not override.</i><br>
	 * Call this if you want the new filter to have some default filter strings.
	 */
	public void setDefaultFilterStrings(String[] defaultFilterStrings)
	{
		this.defaultFilterStrings = defaultFilterStrings;
	}
	/**
	 * <i>Configuration method. Do not override.</i><br>
	 * Call in order to not prompt the user for a filter name. This also implies we will not 
	 *  be prompting for a parent filter pool! Default is true.
	 * <p>
	 * This is used when creating temporary filters that won't be saved. In this case, on
	 *  Finish a filter is not created! Instead, call getFilterStrings() to get the filter
	 *  strings created by the user ... typically there is just one unless you also called
	 *  setDefaultFilterStrings, in which case they will also be returned.
	 * <p>
	 * For convenience, when this is called, setShowInfoPage(false) is called for you
	 */
	public void setShowNamePrompt(boolean show)
	{
		showNamePrompt = show;
	}
	/**
	 * <i>Configuration method. Do not override.</i><br>
	 * Call in order to not show the final info-only page of the wizard. Default is true.
	 * @see #setVerbage(String)
	 */
	public void setShowInfoPage(boolean show)
	{
		showInfoPage = show;
	}
	/**
	 * <i>Configuration method. Do not override.</i><br>
	 * Set the verbage to show on the final page. By default, it shows a tip about creating multiple
	 *  filter strings via the Change action. Use this method to change that default.
	 */
	public void setVerbage(String verbage)
	{
		this.verbage = verbage;
	}
	/**
	 * <i>Configuration method. Do not override.</i><br>
	 * Set the description to display on the first page of the wizard
	 */
	public void setPage1Description(String description)
	{
		this.page1Description = description;
	}
	/**
	 * <i>Configuration method. Do not override.</i><br>
	 * Set if we are creating a filter for use in the RSE or not. This affects the 
	 *  tips and help.
	 * <p>
	 * This is set to true automatically by the subsystem factory base class in the RSE,
	 *  else it defaults to false.
	 */
	public void setFromRSE(boolean rse)
	{
		this.fromRSE = true;
	}
	/**
	 * <i>Configuration method. Do not override.</i><br>
	 * Set the validator to call when the user selects a filter pool. Optional.
	 * Only valid in create mode.
	 */
	public void setFilterPoolSelectionValidator(ISystemFilterPoolSelectionValidator validator)
	{
	     this.filterPoolSelectionValidator = validator;	
	}    

	/**
	 * <i>Configuration method. Do not override.</i><br>
	 * Specify an edit pane that prompts the user for the contents of a filter string.
	 */
	public void setFilterStringEditPane(SystemFilterStringEditPane editPane)
	{
		this.editPane = editPane;
	}
	
	/**
	 * <i>Configuration method. Do not override.</i><br>
	 * Specify the help to show for the name page (page 2)
	 */
	public void setNamePageHelp(String helpId)
	{
		this.namePageHelp = helpId;
	}
			
	// ----------------------
	// OVERRIDABLE METHODS...
	// ----------------------
	/**
	 * <i><b>Overridable</b> configuration method.</i><br>
	 * Overridable extension. For those cases when you don't want to create your
	 * own wizard subclass, but prefer to simply configure the default wizard.
	 * <p>
	 * Note, at the point this is called, all the base configuration, based on the 
	 * setters for this action, have been called. There really is nothing much that
	 * can't be done via setters. The reason you may want to subclass versus use the
	 * setters is defer expensive operations until the user actually selects the New Filter
	 * action. Using the setters means this is done at time the popup menu is being 
	 * construction. Overriding this method allows you to defer the wizard configuration
	 * until the user selects the action and the wizard is actually created.
	 * <p>By default, this does nothing.
	 */
	protected void configureNewFilterWizard(SystemNewFilterWizard wizard)
	{		
	}
	
	/**
	 * <i><b>Overridable</b> configuration method.</i><br>
	 * Configure the new filter created by the wizard. This is only called after 
	 *  successful completion of the wizard
	 * <p>By default, this does nothing.
	 */ 
	protected void configureNewFilter(ISystemFilter newFilter)
	{
	}

	// --------------------	
	// LIFECYCLE METHODS...
	// --------------------

	/**
	 * <i>Lifecyle method. Do not override. Instead override {@link #createNewFilterWizard(ISystemFilterPool)}.</i><br>
	 * The default processing for the run method calls createDialog, which
	 *  in turn calls this method to return an instance of our wizard.<br>
	 * Our default implementation is to call createNewFilterWizard.
	 * <p>
	 * Note your own wizard must subclass {@link org.eclipse.rse.ui.filters.dialogs.SystemNewFilterWizard SystemNewFilterWizard}
	 */
	protected IWizard createWizard()
	{
		if ((callbackConfigurator != null) && !callbackConfiguratorCalled)
		{
			callbackConfigurator.configureNewFilterAction(((ISubSystem)callbackData).getSubSystemConfiguration(), this, callbackData);
			callbackConfiguratorCalled = true;
		}
		SystemNewFilterWizard wizard = createNewFilterWizard(parentPool);
		if (poolsToSelectFrom != null)
		  wizard.setAllowFilterPoolSelection(poolsToSelectFrom);
		else if (poolWrapperInformation != null)
		  wizard.setAllowFilterPoolSelection(poolWrapperInformation);
		if (type != null)
		  wizard.setType(type);		 
		if (defaultFilterStrings != null)
		  wizard.setDefaultFilterStrings(defaultFilterStrings);
		if (namePageHelp != null)
		  wizard.setNamePageHelp(namePageHelp);		 
		wizard.setShowFilterStrings(showFilterStrings);
		wizard.setShowNamePrompt(showNamePrompt);
		wizard.setShowInfoPage(showInfoPage);
		wizard.setFromRSE(fromRSE);
		if (verbage != null)
		  wizard.setVerbage(verbage);
		if (page1Description != null)
		  wizard.setPage1Description(page1Description);
		if (filterPoolSelectionValidator != null)
		  wizard.setFilterPoolSelectionValidator(filterPoolSelectionValidator);
		if (editPane != null)
		  wizard.setFilterStringEditPane(editPane);
		ISystemFilterPoolReferenceManagerProvider provider = getSystemFilterPoolReferenceManagerProvider();
    	//RSEUIPlugin.logDebugMessage(this.getClass().getName(),"Inside createWizard. null? " + (provider==null));		
        wizard.setSystemFilterPoolReferenceManagerProvider(provider);
        configureNewFilterWizard(wizard);
		return wizard;		
	}

	/**
	 * <i><b>Overridable</b> lifecyle method. </i><br>
	 * Create and return the actual wizard.
	 * By default this returns an instance of {@link org.eclipse.rse.filters.ui.wizards.SystemNewFilterWizard}.
	 * <p>
	 * You can avoid creating your own wizard subclass by instead overriding 
	 * {@link #configureNewFilterWizard(SystemNewFilterWizard)}
	 */
	protected SystemNewFilterWizard createNewFilterWizard(ISystemFilterPool parentPool)
	{
		return new SystemNewFilterWizard(parentPool);
	}
    /**
	 * <i>Lifecyle method. Do not override. Instead override {@link #configureNewFilter(ISystemFilter)}.</i><br>
     * Intercept of parent method so we can allow overrides opportunity to 
     *  configure the new filter.
     * This simply calls configureNewFilter.
     */
    protected void postProcessWizard(IWizard wizard)
    {
    	SystemNewFilterWizard newFilterWizard = (SystemNewFilterWizard)wizard;    	
    	ISystemFilter newFilter = newFilterWizard.getSystemFilter();
    	if (newFilter != null)
    	  configureNewFilter(newFilter);
    }
	
	/**
	 * <i>Lifecyle method. No need to override.</i><br>
	 * Decide whether to enable this action based on selected object's type.
	 * Returns false unless selected object is a filter pool or subsystem.
	 */
	public boolean checkObjectType(Object selectedObject)
	{
		return ((selectedObject instanceof ISystemFilterContainer) ||
 		        (selectedObject instanceof ISystemFilterContainerReference) ||
 		        (selectedObject instanceof ISystemFilterPoolReferenceManagerProvider)); 		      
	}
	
	// -----------------	
	// OUTPUT METHODS...
	// -----------------

    /**
     * <i>Output method. Do not override.</i><br>
     * Get the contextual system filter pool reference manager provider. Will return non-null if the
     * current selection is a reference to a filter pool or filter, or a reference manager
     * provider.
     */
    public ISystemFilterPoolReferenceManagerProvider getSystemFilterPoolReferenceManagerProvider()
    {
    	Object firstSelection = getFirstSelection();
    	if (firstSelection != null)
    	{
    		if (firstSelection instanceof ISystemFilterReference)
    		  return ((ISystemFilterReference)firstSelection).getProvider();
    		else if (firstSelection instanceof ISystemFilterPoolReference)
    		  return ((ISystemFilterPoolReference)firstSelection).getProvider();
            else if (firstSelection instanceof ISystemFilterPoolReferenceManagerProvider)
              return (ISystemFilterPoolReferenceManagerProvider)firstSelection;
            else
              return null;
    	}
        return null;
    }
    
    /**
     * <i>Output method. Do not override.</i><br>
     * Convenience method to return the newly created filter after running the action. 
     * Will be null if the user cancelled. Will also be null if you called setShowNamePrompt(false),
     *  in which case you should call getNewFilterStrings().
     * <p>
     * Be sure to call wasCancelled() first before calling this.
     */
    public ISystemFilter getNewFilter()
    {
    	Object output = getValue();
    	if (output instanceof ISystemFilter)
    	  return (ISystemFilter)getValue();
    	else
    	  return null;
    }
    
    /**
     * <i>Output method. Do not override.</i><br>
     * When prompting for an unnamed filter, no filter is created. Instead, the user is prompted
     *  for a single filter string. This method returns that string. However, if you happened to 
     *  call setDefaultFilterStrings(...) then those are also returned, hence the need for an
     *  array. If not, this will be an array of one, or null if the user cancelled the wizard.
     * <p>
     * Be sure to call wasCancelled() first before calling this.
     */
    public String[] getFilterStrings()
    {
    	Object output = getValue();
    	if (output == null)
    	  return null;
    	else if (output instanceof Vector)
    	{
    		Vector v = (Vector)output;
    		String[] strings = new String[v.size()];
    		for (int idx=0; idx<strings.length; idx++)
    		    strings[idx] = (String)v.elementAt(idx);
    		return strings;
    	}
    	else if (output instanceof ISystemFilter)
    	{
    		return ((ISystemFilter)output).getFilterStrings();
    	}
    	else
    	  return null;
    }
    /**
     * <i>Output method. Do not override.</i><br>
     * Shortcut to getFilterStrings()[0].
     */
    public String getFilterString()
    {
    	String[] strings = getFilterStrings();
    	if ((strings!=null) && (strings.length>0))
    	  return strings[0];
    	else
    	  return null;
    }
}