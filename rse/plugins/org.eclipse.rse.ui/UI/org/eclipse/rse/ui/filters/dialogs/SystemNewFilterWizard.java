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

import java.util.Vector;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterContainer;
import org.eclipse.rse.filters.ISystemFilterContainerReference;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.filters.ISystemFilterPoolSelectionValidator;
import org.eclipse.rse.filters.ISystemFilterPoolWrapper;
import org.eclipse.rse.filters.ISystemFilterPoolWrapperInformation;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.filters.SystemFilterStringEditPane;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorFilterName;
import org.eclipse.rse.ui.wizards.AbstractSystemWizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;


/**
 * Base Wizard for users to define a new system filter.
 * While subsystem providers are free to offer their own wizards, this
 * abstracts out much of it and makes it easy to simply override and
 * supply a couple classes to offer a totally unique presentation to the 
 * user.
 * <p>
 * Some guiding design rules for this wizard:
 * <ul>
 *  <li>Users are confused about filter strings versus filters. So, we don't
 *       support the creation of multiple filter strings at the time the filter
 *       is created, only after via the change action. The wizard basically
 *       prompts for the creation of a single new filter string.
 *  <li>Users are confused about "naming" a filter, so the name is not asked
 *       for until the second page, and it is possible for subclasses to 
 *       default that name to something intelligent based on the contents of
 *       the first page, such that users can press Finish and not think about
 *       the name.
 * </ul>
 * <p>
 * While this class can be subclassed, you should find all attributes can be
 * configured via setters.
 */
public class SystemNewFilterWizard 
	    extends AbstractSystemWizard 
{
	protected SystemNewFilterWizardMainPage mainPage;	
	protected SystemNewFilterWizardNamePage namePage;	
	protected SystemNewFilterWizardInfoPage infoPage;	
	protected ISystemFilterContainer         filterContainer;
	protected ISystemFilterPool              parentPool;
	protected ISystemFilterPool[]            poolsToSelectFrom;
	protected String                        type;
	protected String[]                      defaultFilterStrings;
	//protected String                        verbage;
	//protected String                        page1Description;
	protected boolean                      showFilterStrings = true;
	protected boolean                      showNamePrompt = true;
	protected boolean                      showInfoPage = true;
	protected boolean                      fromRSE = false;
	protected boolean                      page1DescriptionSet = false;
	protected ISystemFilter                  newFilter = null;
	protected SystemFilterStringEditPane    editPane;	
    protected ISystemFilterPoolReferenceManagerProvider provider;
    protected ISystemFilterPoolWrapperInformation      poolWrapperInformation;
	protected ISystemFilterPoolSelectionValidator      filterPoolSelectionValidator; 
	protected ISystemNewFilterWizardConfigurator       configurator;

	    
	/**
	 * Constructor when you want to supply your own title and image
	 * @param title - title to show for this wizard. This is used as the page title! The title is always "New"!
	 * @param wizardImage - title bar image for this wizard
	 * @param parentPool - the filter pool we are to create this filter in.
	 */
	public SystemNewFilterWizard(String title, ImageDescriptor wizardImage, ISystemFilterPool parentPool)
	{
		this(new SystemNewFilterWizardConfigurator(title), wizardImage, parentPool);
	}
    /**
     * Constructor when you want to use the default page title and image, or want to 
     *  supply it via setWizardTitle and setWizardImage.
	 * @param parentPool - the filter pool we are to create this filter in.
     */	
	public SystemNewFilterWizard(ISystemFilterPool parentPool)
	{
		this(new SystemNewFilterWizardConfigurator(),
	  	      RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWFILTERWIZARD_ID),
	  	      parentPool);		      
	}	
	/**
	 * Constructor when you want to supply all your own configuration data
	 * @param data - configuration data
	 * @param wizardImage - title bar image for this wizard
	 * @param parentPool - the filter pool we are to create this filter in.
	 */
	public SystemNewFilterWizard(ISystemNewFilterWizardConfigurator data, ImageDescriptor wizardImage, ISystemFilterPool parentPool)
	{
		super(SystemResources.RESID_NEWFILTER_TITLE, wizardImage);
		super.setWizardPageTitle(data.getPageTitle());
    	super.setForcePreviousAndNextButtons(true);
    	this.configurator = data;
		this.parentPool = parentPool;
		setOutputObject(null);
	}

	// -----------------------------------	
	// INPUT/CONFIGURATION METHODS...
	// -----------------------------------

    /**
     * If you want to prompt the user for the parent filter pool to create this filter in, 
     *  call this with the list of filter pools. In this case, the filter pool passed into
     *  the constructor will be used as the initial selection.
     */
    public void setAllowFilterPoolSelection(ISystemFilterPool[] poolsToSelectFrom)
    {
    	this.poolsToSelectFrom = poolsToSelectFrom;
    }    
    /**
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
     * Set the type of filter we are creating. Results in a call to setType on the new filter.
     * Types are not used by the base filter framework but are a way for tools to create typed
     * filters and have unique actions per filter type.
     */
    public void setType(String type)
    {
    	this.type = type;
    }
    /**
     * Get the type of filter as set by {@link #setType(String)}
     */
    public String getType()
    {
    	return type;
    }
	/**
	 * Call in order to not have the first page, but instead the name-prompt page. Default is true.
     * @see #setDefaultFilterStrings(String[])
	 */
	public void setShowFilterStrings(boolean show)
	{
		showFilterStrings = show;
	}
	/**
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
		if (!show)
		  setShowInfoPage(false);
	}
	/**
	 * Specify the help to show for the name page (page 2)
	 */
	public void setNamePageHelp(String helpId)
	{
		if (configurator instanceof SystemNewFilterWizardConfigurator)
		  ((SystemNewFilterWizardConfigurator)configurator).setPage2HelpID(helpId);
	}
	/**
	 * Call in order to not show the final info-only page of the wizard. Default is true.
	 */
	public void setShowInfoPage(boolean show)
	{
		showInfoPage = show;
	}
	/**
	 * Call this if you want the filter to auto-include some default filter strings.
	 */
	public void setDefaultFilterStrings(String[] defaultFilterStrings)
	{
		this.defaultFilterStrings = defaultFilterStrings;
	}
	/**
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
	 * Set the validator to call when the user selects a filter pool. Optional.
	 * Only valid in create mode.
	 */
	public void setFilterPoolSelectionValidator(ISystemFilterPoolSelectionValidator validator)
	{
	     this.filterPoolSelectionValidator = validator;	
	}    

    /**
     * Set the contextual system filter pool reference manager provider. Eg, in the RSE, this
     *  will be the selected subsystem if the New Filter action is launched from there, or if
     *  launched from a filter pool reference under there.
     * <p>
     * Will be non-null if the current selection is a reference to a filter pool or filter, 
     *  or a reference manager provider. 
     * <p>
     * This is passed into the filter and filter string wizards and dialogs in case it is needed
     *  for context. 
     */
    public void setSystemFilterPoolReferenceManagerProvider(ISystemFilterPoolReferenceManagerProvider provider)
    {
    	this.provider = provider;
    	//RSEUIPlugin.logDebugMessage(this.getClass().getName(),"Inside setSystemFilterPoolReferenceManagerProvider. null? " + (provider==null));
    }
	/**
	 * Set the verbage to show on the final page. By default, it shows a tip about creating multiple
	 *  filter strings via the Change action. Use this method to change that default.
	 */
	public void setVerbage(String verbage)
	{
		if (configurator instanceof SystemNewFilterWizardConfigurator)
		  ((SystemNewFilterWizardConfigurator)configurator).setPage3Tip1(verbage);
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
		super.setWizardPageTitle(pageTitle);
		if (configurator instanceof SystemNewFilterWizardConfigurator)
		  ((SystemNewFilterWizardConfigurator)configurator).setPageTitle(pageTitle);		
	}
	/**
	 * Set the description to display on the first page of the wizard
	 */
	public void setPage1Description(String description)
	{
		if (configurator instanceof SystemNewFilterWizardConfigurator)
		  ((SystemNewFilterWizardConfigurator)configurator).setPage1Description(description);
		page1DescriptionSet = true;
	}
	
	/**
	 * Specify an edit pane that prompts the user for the contents of a filter string.
	 */
	public void setFilterStringEditPane(SystemFilterStringEditPane editPane)
	{
		this.editPane = editPane;
	}

	// -----------------------------------	
	// INTERNAL BUT OVERRIDABLE METHODS...
	// -----------------------------------
	/**
	 * Extendable point for child classes. You don't need to override typically though... rather
	 *  you can simply supply your own filter string edit pane. 
	 * <p>
	 * By default, this page uses the wizard page title as set in setWizardPageTitle(...) or the constructor.
	 * @return the primary page prompting for a single filter string. 
	 */
	protected SystemNewFilterWizardMainPage createMainPage()
	{
		mainPage = null;
		if (editPane == null)
		  mainPage = new SystemNewFilterWizardMainPage(this, configurator);
		else
		  mainPage = new SystemNewFilterWizardMainPage(this, editPane, configurator);
	    return mainPage;
	}
	/**
	 * Extendable point for child classes. You don't need to override typically though.
	 * <p>
	 * By default, this page uses the wizard page title as set in setWizardPageTitle(...) or the constructor.
	 * @return the wizard page prompting for the filter name and parent filter pool
	 */
	protected SystemNewFilterWizardNamePage createNamePage()
	{
		namePage = new SystemNewFilterWizardNamePage(this, parentPool, configurator);
	    return namePage;
	}
	/**
	 * Extendable point for child classes. You don't need to override typically though.
	 * <p>
	 * By default, this page uses the wizard page title as set in setWizardPageTitle(...) or the constructor.
	 * @return the final wizard page with additional readonly information
	 */
	protected SystemNewFilterWizardInfoPage createInfoPage()
	{
		boolean showFilterPoolsTip = ((poolsToSelectFrom != null) || (poolWrapperInformation != null));
		infoPage = new SystemNewFilterWizardInfoPage(this, showFilterPoolsTip, configurator);
	    return infoPage;
	}	
	/**
	 * Override of parent to do nothing
	 */
	public void addPages() {}
	
	/**
	 * Creates the wizard pages. 
	 * This method is an override from the parent Wizard class.
	 */
	public void createPageControls(Composite c) 
	{
		try {
		   // MAIN PAGE...
	       mainPage = createMainPage();
		   mainPage.setSystemFilterPoolReferenceManagerProvider(provider);
		   mainPage.setType(type);
		   if (defaultFilterStrings != null)
		     mainPage.setDefaultFilterStrings(defaultFilterStrings);
		   if (showFilterStrings)
		   {
	         addPage((WizardPage)mainPage);
		   }

		   // NAME PAGE...
	       namePage = createNamePage();
		   if (showNamePrompt && (namePage!=null))
		   {
		      if (filterPoolSelectionValidator!=null)
		        namePage.setFilterPoolSelectionValidator(filterPoolSelectionValidator);
		      if (poolsToSelectFrom != null)
		      {
		   	      ISystemValidator[] validators = new ISystemValidator[poolsToSelectFrom.length];
		   	      for (int idx=0; idx<validators.length; idx++)
		   	 	     validators[idx] = getFilterNameValidator(poolsToSelectFrom[idx]);
		          namePage.setAllowFilterPoolSelection(poolsToSelectFrom, validators);		     
		      }
		      else if (poolWrapperInformation != null)
		      {
		      	  ISystemFilterPoolWrapper[] wrappers = poolWrapperInformation.getWrappers();
		   	      ISystemValidator[] validators = new ISystemValidator[wrappers.length];
		   	      for (int idx=0; idx<validators.length; idx++)
		   	 	     validators[idx] = getFilterNameValidator(wrappers[idx].getSystemFilterPool());
		          namePage.setAllowFilterPoolSelection(poolWrapperInformation, validators);		     
		      }
		      else
		      {
	              ISystemValidator validator = getFilterNameValidator(getFilterContainer());
		          namePage.setFilterNameValidator(validator);		   		     
		      }
	          if (!showFilterStrings && page1DescriptionSet)
	            	namePage.setDescription(configurator.getPage1Description());
	          addPage((WizardPage)namePage);
		   }
	         		   	       
		   // INFO PAGE...		   
		   if (showInfoPage)
		   {
	          infoPage = createInfoPage();
		      if (infoPage!=null)
		      {
	            addPage((WizardPage)infoPage);
		      }
		   }
		} catch (Exception exc)
		{
	   	   SystemBasePlugin.logError("Error in createPageControls of SystemNewFilterWizard", exc);
	   	   //System.out.println("Error in createPageControls of SystemNewFilterWizard"); // temp
	   	   //exc.printStackTrace(); // temp
		}
	}
	/**
	 * Extendable point for child classes.
	 * Override to change the validator used for the filter name given the master object.
	 * By default, uses FilterNameValidator.
	 */
	protected ISystemValidator getFilterNameValidator(ISystemFilterContainer container)
	{
		return getFilterNameValidator(container, null);
	}
	/**
	 * Reusable method to return a name validator for renaming a filter.
	 * @param the current filter object on updates. Can be null for new names. Used
	 *  to remove from the existing name list the current filter's name.
	 */
	public static ISystemValidator getFilterNameValidator(ISystemFilterContainer container,ISystemFilter filter)
	{
    	Vector v = container.getSystemFilterNames();
    	if (filter != null)
    	  v.removeElement(filter.getName());
	    ValidatorFilterName filterNameValidator = new ValidatorFilterName(v);		
	    return filterNameValidator;
	}	

	/**
	 * Override if necessary.
	 * Returns true if filter strings are case-sensitive in this filter.
	 * <p>
	 * By default, returns the value in the selected filter container. If this is null, returns false.
	 */
	public boolean areStringsCaseSensitive()
	{
		ISystemFilterContainer fc = getFilterContainer();
		if (fc != null)
		  return fc.areStringsCaseSensitive();
		return false;
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
		//System.out.println("inside performFinish(): mainPage.performFinish() = " + mainPage.performFinish());
        boolean ok = false;
	    newFilter = null;
		setOutputObject(null);
		if (!mainPage.performFinish())
           	setPageError(mainPage);
		else if (!namePage.performFinish())
           	setPageError(namePage);
		else
		{
		    Vector filterStrings = mainPage.getFilterStrings();
		    String filterName = null;
		    if (showNamePrompt)
		    {
		        filterName = namePage.getFilterName();
	            ISystemFilterContainer filterParent = null;
	            if ((poolsToSelectFrom!=null) || (poolWrapperInformation != null))
	            {
	              	filterParent = namePage.getParentSystemFilterPool();
	              	if (namePage.getUniqueToThisConnection())
	              	{
	              		// this means the user selected to create this filter in the 
	              		//  filter pool that is unique to this connection. So now we 
	              		//  must find, or create, that filter pool:
						filterParent = provider.getUniqueOwningSystemFilterPool(true); // true -> create if not found
	              	}
	              	else
						filterParent = namePage.getParentSystemFilterPool();
				}
	            else
	              	filterParent = getFilterContainer();
	      
	            String type = mainPage.getType(); // query just in case it is changable by user
	            try 
	            {
	              	newFilter = createNewFilter(getShell(), filterParent, filterName, filterStrings, type);
	              	if (newFilter == null)
	                	return false;
	              	// Because we allow new users to select a profile to create their filter in, from
	              	//  which we choose the profile's default filter pool, it is possible the user
	              	//  will choose a filter pool that this subsystem does not yet reference. To solve
	              	//  this we need to add a reference for them. This is only a possibility when called
	              	//  from the subsystem New Filter action, versus from a filter pool say.
	              	if ((provider != null) && (filterParent instanceof ISystemFilterPool))
	              	{
	              	 	ISystemFilterPool parentPool = (ISystemFilterPool)filterParent;
	              	 	if (provider.getSystemFilterPoolReferenceManager().getReferenceToSystemFilterPool(parentPool) == null)
	              	 	{
	              	 		provider.getSystemFilterPoolReferenceManager().addReferenceToSystemFilterPool(parentPool);
	              	 	}
	              	}
	              
	              	/* Hmm, after much thought I have decided to leave this up to the 
	               	 * caller. They can do this themselves by overriding createNewFilter in
	               	 * their own wizard.
		          	if (!showFilterStrings && (newFilter!=null))
		          	{
		             	newFilter.setNonChangable(true);
		             	newFilter.setStringsNonChangable(true);
		          	}
		          	*/
	            } catch (Exception exc)
	            {
		        	SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXCEPTION_OCCURRED);
		    	    msg.makeSubstitution(exc);
		    	    SystemMessageDialog msgDlg = new SystemMessageDialog(getShell(), msg);
		    	    msgDlg.openWithDetails();
                    return false;
	            }
	            setOutputObject(newFilter);            
		        // special handling to expand the currently selected parent node and reveal the new filter
		        if ((newFilter != null) && (getInputObject()!=null))
		        {
		  	      	Object selectedObject = getInputObject();
		  	      	if ((selectedObject instanceof ISystemFilterPoolReference) ||
		  	       	   (selectedObject instanceof ISystemFilterPoolReferenceManagerProvider))
		          	{
		  		    	ISystemFilterPoolReferenceManagerProvider provider = null;
		  		    	if (selectedObject instanceof ISystemFilterPoolReference) 
		  		    	{
		  		      		ISystemFilterPoolReferenceManager sfprm = ((ISystemFilterPoolReference)selectedObject).getFilterPoolReferenceManager();
		  		      		if (sfprm != null)
		  		        		provider = sfprm.getProvider();
		  		    	}
		  		    	else
		  		      	provider = (ISystemFilterPoolReferenceManagerProvider)selectedObject;
		  		    	if (provider != null)
		  		      		provider.filterEventFilterCreated(selectedObject, newFilter);
		  	      	}
		  	      	else if (selectedObject instanceof ISystemFilterReference)
		  	      	{
		  	      		ISystemFilterReference ref = (ISystemFilterReference)selectedObject;
		  	      		ISystemFilterPoolReferenceManagerProvider provider = ref.getProvider();
		  	      		provider.filterEventFilterCreated(selectedObject, newFilter);
		  	      	}
		        }
		        ok = (newFilter != null);
		    } // end if showNamePrompt	
		    else
		    {
		        ok = true;	    
	            setOutputObject(filterStrings);		        
		    }
		    return ok;
	    }
	    return false;
	}
	
	/**
	 * Return the parent into which we are creating a filter. If filterContainer has been set,
	 *  returns that, else returns getInputObject() which is set by the calling action.
	 */
	protected Object getParent()
	{
		if (filterContainer != null)
		  return filterContainer;
		else
		  return getInputObject();
	}
	/**
	 * Return parent filter container to contain this filter
	 */
	protected ISystemFilterContainer getFilterContainer()
	{
		//System.out.println("Old. inside getFilterContainer. " + filterContainer + ", " + getInputObject());				

		if (filterContainer == null)
		{
		  Object input = getInputObject();
		  //if (input != null)
		  //  System.out.println("... input instanceof SystemFilterContainer? " + (input instanceof SystemFilterContainer));
		  //else
		  //  System.out.println("... input is null");

		  if (input != null)
		  {
			if (input instanceof ISystemFilter)
				return ((ISystemFilter)input).getParentFilterContainer();
			else if (input instanceof ISystemFilterReference)
				return ((ISystemFilterReference)input).getReferencedFilter().getParentFilterContainer();
		    else if (input instanceof ISystemFilterContainer)
		  	  return (ISystemFilterContainer)input;
		  	else if (input instanceof ISystemFilterContainerReference)
		  	  return ((ISystemFilterContainerReference)input).getReferencedSystemFilterContainer();  
		  	else if (input instanceof ISystemFilterPoolReference)
              return ((ISystemFilterPoolReference)input).getReferencedFilterPool();		  		
		  	else if (parentPool != null)
		  	  return parentPool;
		  	else if ((poolsToSelectFrom != null) && (poolsToSelectFrom.length>0))
		  	  return poolsToSelectFrom[0];
		  	else if (poolWrapperInformation != null)
		  	  return poolWrapperInformation.getPreSelectWrapper().getSystemFilterPool();
		  	else
		  	  return null;
		  }
		  else
		    return null;
		}
		else
		  return filterContainer;
	}
	/**
	 * Set parent filter container to contain this filter
	 */
	public void setFilterContainer(ISystemFilterContainer container)
	{
		this.filterContainer = container;
	}
	/**
	 * Set parent filter container to contain this filter
	 */
	public void setFilterContainer(ISystemFilterContainerReference containerRef)
	{
		this.filterContainer = containerRef.getReferencedSystemFilterContainer();
	}
	
	/**
	 * Extendable point for child classes.
	 * Override to create unique SystemFilter object.
	 * By default calls createSystemFilter in subsystem factory.
	 */
	public ISystemFilter createNewFilter(Shell shell, ISystemFilterContainer filterParent, String aliasName, Vector filterStrings, String type)
	   throws Exception
	{
		ISystemFilter newFilter = null;
		ISystemFilterPoolManager fpMgr = filterParent.getSystemFilterPoolManager();
		//try {
		  // create filter
		  if (type == null)
		    newFilter = fpMgr.createSystemFilter(filterParent,aliasName,filterStrings);
		  else
		    newFilter = fpMgr.createSystemFilter(filterParent,aliasName,filterStrings,type);		  
		//} catch (Exception exc)
		//{
		  //RSEUIPlugin.logError("Exception in createNewFilter in SystemFilterAbstractNewFilterWizard. ",);
		  //System.out.println("Exception in createNewFilter in SystemFilterAbstractNewFilterWizard: "+exc.getMessage());
		  //exc.printStackTrace();
		//}
		return newFilter;    
	}
	
	// -----------------------
	// CALLBACKS FROM PAGES...
	// -----------------------
	/**
	 * Return true if this filter is an RSE filter or not
	 */
	protected boolean isFromRSE()
	{
		return fromRSE;
	}
	/**
	 * For page 2 of the New Filter wizard, if it is possible to 
	 *  deduce a reasonable default name from the user input here,
	 *  then return it here. Else, just return null.
	 * <b>
	 * By default this calls getDefaultFilterName on the edit pane.
	 */
	public String getDefaultFilterName()
	{
		return mainPage.getEditPane(null).getDefaultFilterName();
	}	
	// ------
	// OUTPUT
	// ------
	/**
	 * Return the filter created upon successful finish
	 */
	public ISystemFilter getSystemFilter()
	{
		return newFilter;
	}
} // end class