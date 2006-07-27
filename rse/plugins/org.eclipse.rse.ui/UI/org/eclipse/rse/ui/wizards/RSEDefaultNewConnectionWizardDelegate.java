/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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

import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPerspectiveHelpers;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.util.ISubsystemConfigurationAdapter;
import org.eclipse.rse.model.DummyHost;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.SystemStartHere;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemConnectionForm;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.messages.SystemMessageDialog;


/**
 *
 */
public class RSEDefaultNewConnectionWizardDelegate extends RSENewConnectionWizardDelegate {
	
	private RSENewConnectionWizardDefaultDelegateMainPage         mainPage;
	private SystemNewConnectionWizardRenameProfilePage rnmProfilePage;
    private ISystemNewConnectionWizardPage[]           subsystemFactorySuppliedWizardPages;
    private Hashtable                                  ssfWizardPagesPerSystemType = new Hashtable();
	private String                                     defaultUserId;
	private String                                     defaultConnectionName;
	private String                                     defaultHostName;
	private String[]                                   activeProfileNames = null;
    private int                                        privateProfileIndex = -1;
    private ISystemProfile                              privateProfile = null;
    private IHost                           currentlySelectedConnection = null;
    private String[]                                   restrictSystemTypesTo;
    private static String                              lastProfile = null;    
    private boolean									   showProfilePageInitially = true;
    private IHost _dummyHost;

	/**
	 * Constructor.
	 */
	public RSEDefaultNewConnectionWizardDelegate() {
	}

	/**
	 * @see org.eclipse.rse.ui.wizards.RSENewConnectionWizardDelegate#init(org.eclipse.rse.ui.wizards.RSENewConnectionWizard, org.eclipse.rse.core.IRSESystemType)
	 */
	public void init(RSENewConnectionWizard wizard, IRSESystemType systemType) {
		super.init(wizard, systemType);
		restrictSystemType(systemType.getName());
    	activeProfileNames = SystemStartHere.getSystemProfileManager().getActiveSystemProfileNames();
    	systemTypeSelected(systemType.getName(), true);
	}

	/**
     * Call this to restrict the system type that the user is allowed to choose
     */
    public void restrictSystemType(String systemType)
    {
    	restrictSystemTypesTo = new String[1];
    	restrictSystemTypesTo[0] = systemType;
	    if (mainPage != null)
	      mainPage.restrictSystemTypes(restrictSystemTypesTo);
    }	
    /**
     * Call this to restrict the system types that the user is allowed to choose
     */
    public void restrictSystemTypes(String[] systemTypes)
    {
    	this.restrictSystemTypesTo = systemTypes;
	    if (mainPage != null)
	      mainPage.restrictSystemTypes(systemTypes);
    }	    
    
    public IHost getDummyHost()
    {
    	if (_dummyHost == null)
    	{
    		_dummyHost = new DummyHost(mainPage.getHostName(), mainPage.getSystemType());
    	}
    	return _dummyHost;
    }
    


	/**
	 * Creates the wizard pages.
	 * This method is an override from the parent Wizard class.
	 */
	public void addPages()
	{
	   try {
	      mainPage = createMainPage(restrictSystemTypesTo);
	      mainPage.setConnectionNameValidators(SystemConnectionForm.getConnectionNameValidators());
	      mainPage.setCurrentlySelectedConnection(currentlySelectedConnection);	      
	      if (defaultUserId != null)
	        mainPage.setUserId(defaultUserId);
	      if (defaultConnectionName != null)
	        mainPage.setConnectionName(defaultConnectionName);
	      if (defaultHostName != null)
	        mainPage.setHostName(defaultHostName);

	      if (restrictSystemTypesTo != null)
	        mainPage.restrictSystemTypes(restrictSystemTypesTo);
	      
	      ISystemProfile defaultProfile = SystemStartHere.getSystemProfileManager().getDefaultPrivateSystemProfile();
	      
	      showProfilePageInitially = RSEUIPlugin.getDefault().getShowProfilePageInitially();
	      /* DKM - I don't think we should force profiles into the faces of users
	       *     we no longer default to "private" so hopefully this would never be
	       *     desirable
	       * 
	      // if there is a default private profile, we might want to show the rename profile page
	      if (defaultProfile != null)
	      {
	      	// make private profile equal to default profile
	      	privateProfile = defaultProfile;
	      	
	      	// get the private profile index in the list of active profiles
	      	for (int idx=0; (privateProfileIndex<0) && (idx<activeProfileNames.length); idx++)
	      	   if (activeProfileNames[idx].equals(defaultProfile.getName()))
	      	     privateProfileIndex = idx;
	      	
	      	// if profile page is to be shown initially, then add the page
	      	if (showProfilePageInitially) {
	      		rnmProfilePage = new SystemNewConnectionWizardRenameProfilePage(this) ;
	      		addPage(rnmProfilePage);
	      	}
	      	// otherwise, do not add profile page
	      	// and set the new private profile name to be the local machine name 
	      	else {
	      		rnmProfilePage = null;
	      		
	      		String initProfileName = RSEUIPlugin.getLocalMachineName();
	      		int dotIndex = initProfileName.indexOf('.');
	      		
	      		if (dotIndex != -1) {
	      			initProfileName = initProfileName.substring(0, dotIndex);
	      		}
	      		
	      		setNewPrivateProfileName(initProfileName);
	      	}
	      }
	      else 
	      */
	      {	      	
            mainPage.setProfileNames(activeProfileNames);
            // if there is no connection currently selected, default the profile to
            // place the new connection into to be the first of:
            //   1. the profile the last connection was created in, in this session
            //   2. the team profile.
            if (currentlySelectedConnection == null)
            {
              if ((lastProfile == null) && (activeProfileNames!=null))
              {
                String defaultTeamName = ISystemPreferencesConstants.DEFAULT_TEAMPROFILE;
                for (int idx=0; (lastProfile==null)&&(idx<activeProfileNames.length); idx++)
                {
              	 if (!activeProfileNames[idx].equals(defaultTeamName))
              	   lastProfile = activeProfileNames[idx];
                }  
                if ((lastProfile == null) && (activeProfileNames.length>0))
                  lastProfile = activeProfileNames[0];
              }
              if (lastProfile != null)
                mainPage.setProfileNamePreSelection(lastProfile);            
            }
	      }
	        
	      // getWizard().addPage((WizardPage)mainPage);
	      
	   } catch (Exception exc)
	   {
	   	 SystemBasePlugin.logError("New connection: Error in createPages: ",exc);
	   }
	}

	/**
	 * Creates the wizard's main page. 
	 * This method is an override from the parent class.
	 */
	protected RSENewConnectionWizardDefaultDelegateMainPage createMainPage(String[] restrictSystemTypesTo)
	{
		String pageTitle = getPageTitle();
	    mainPage = new RSENewConnectionWizardDefaultDelegateMainPage(getWizard(),
	            pageTitle,
	           SystemResources.RESID_NEWCONN_PAGE1_DESCRIPTION);
		getWizard().setOutputObject(null);
	    return mainPage;
	}
	
	public String getPageTitle() {
		
		String pageTitle = null;
		
		if ((restrictSystemTypesTo == null) || (restrictSystemTypesTo.length != 1)) {
			pageTitle = SystemResources.RESID_NEWCONN_PAGE1_TITLE;
		}
		else {
			String onlySystemType = restrictSystemTypesTo[0];
			
			if (onlySystemType.equals(IRSESystemType.SYSTEMTYPE_LOCAL)) {
				pageTitle = SystemResources.RESID_NEWCONN_PAGE1_LOCAL_TITLE;
			}
			else {
				pageTitle = SystemResources.RESID_NEWCONN_PAGE1_REMOTE_TITLE;
				pageTitle = SystemMessage.sub(pageTitle, "&1", onlySystemType);
			}
		}
		
		return pageTitle;
	}
	
    /**
     * Set the currently selected connection. Used to better default entry fields.
     */
    public void setCurrentlySelectedConnection(IHost conn)
    {
    	this.currentlySelectedConnection = conn;
    }    

    /**
     * For "new" mode, allows setting of the initial user Id. Sometimes subsystems
     *  like to have their own default userId preference page option. If so, query
     *  it and set it here by calling this.
     */
    public void setUserId(String userId)
    {    	
    	defaultUserId = userId;
    	if (mainPage != null)
    	  mainPage.setUserId(userId);
    }	

	/**
	 * Preset the connection name
	 */
	public void setConnectionName(String name)
	{
		defaultConnectionName = name;
		if (mainPage != null)
		  mainPage.setConnectionName(name);
	}
	/**
	 * Preset the host name
	 */
	public void setHostName(String name)
	{
		defaultHostName = name;
		if (mainPage != null)
		  mainPage.setHostName(name);
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
		boolean ok = mainPage.performFinish();
		if (!ok)
		  getWizard().setPageError((IWizardPage)mainPage);
		else if (ok && hasAdditionalPages())
		{
    	  for (int idx=0; ok && (idx<subsystemFactorySuppliedWizardPages.length); idx++)
    	  {
    	  	   ok = subsystemFactorySuppliedWizardPages[idx].performFinish();
    	  	   if (!ok)
    	  	     getWizard().setPageError(subsystemFactorySuppliedWizardPages[idx]);
    	  }
		}
		if (ok)
		{
    		boolean cursorSet = true;
    		getWizard().setBusyCursor(true);
            ISystemRegistry sr = RSEUIPlugin.getDefault().getSystemRegistry();
            
            // if private profile is not null, then we have to rename the private profile
            // with the new profile name
            if (privateProfile != null)
            {
               try
               {
               	 String newName = activeProfileNames[privateProfileIndex];
                 sr.renameSystemProfile(privateProfile, newName);
               } 
		       catch (SystemMessageException exc)
		       {
		       	SystemMessageDialog.displayMessage(getWizard().getShell(), exc);
				   	     
		   	     ok = false;
		       }
               catch (Exception exc)
               {
               	 getWizard().setBusyCursor(false); 
               	 cursorSet = false;
               	 String msg = "Exception renaming profile ";
               	 SystemBasePlugin.logError(msg, exc);
               	 SystemMessageDialog.displayExceptionMessage(getWizard().getShell(),exc);
               	 ok = false;
               }
            }
            
            if (ok)
            {
               try
               {
                  String sysType = mainPage.getSystemType();                  
                  IHost conn = 
                    sr.createHost(mainPage.getProfileName(), sysType,
                                        mainPage.getConnectionName(), mainPage.getHostName(),
                                        mainPage.getConnectionDescription(), mainPage.getDefaultUserId(),
                                        mainPage.getDefaultUserIdLocation(), subsystemFactorySuppliedWizardPages);

               	  getWizard().setBusyCursor(false); 
              	  cursorSet = false;

                  // a tweak that is the result of UCD feedback. Phil
                  if ((conn!=null) && SystemPerspectiveHelpers.isRSEPerspectiveActive())
                  {
                   	 if (sysType.equals(IRSESystemType.SYSTEMTYPE_ISERIES))
                  	 {
                  		ISubSystem[] objSubSystems = sr.getSubSystemsBySubSystemConfigurationCategory("nativefiles", conn);
                  		if ((objSubSystems != null) 
                  		    && (objSubSystems.length>0))// might be in product that doesn't have iSeries plugins
                  		  sr.expandSubSystem(objSubSystems[0]);
                  		else
                  		  sr.expandHost(conn);
                  	 }
                  	 else
                  	   sr.expandHost(conn);
                  }

                  lastProfile = mainPage.getProfileName();
                  getWizard().setOutputObject(conn);
               } catch (Exception exc)
               {
               	 if (cursorSet)
               	   getWizard().setBusyCursor(false); 
               	 cursorSet = false;
               	 String msg = "Exception creating connection ";
               	 SystemBasePlugin.logError(msg, exc);
               	 SystemMessageDialog.displayExceptionMessage(getWizard().getShell(),exc);
               	 ok = false;
               }
            }
    		//getShell().setCursor(null);
    		//busyCursor.dispose();
    		if (cursorSet)
    		  getWizard().setBusyCursor(false);
		    return ok;
		}
	    return ok;
	}
    
    // callbacks from rename page
    
    /**
     * Set the new profile name specified on the rename profile page...
     */
    protected void setNewPrivateProfileName(String newName)
    {
    	activeProfileNames[privateProfileIndex] = newName;
    	if (mainPage != null)
    	{
    		mainPage.setProfileNames(activeProfileNames);
    		mainPage.setProfileNamePreSelection(newName);          
    	}
    }
    
    /**
     * Return the main page of this wizard
     */
    public IWizardPage getMainPage()
    {
    	if (mainPage == null) {
    		addPages();
    	}
    	
    	return mainPage;
    }
    
    /**
     * Return the form of the main page of this wizard
     */
    public SystemConnectionForm getMainPageForm()
    {
    	return ((RSENewConnectionWizardDefaultDelegateMainPage)mainPage).getForm();
    }
    
    // ----------------------------------------
    // CALLBACKS FROM SYSTEM CONNECTION PAGE...
    // ----------------------------------------
    /**
     * Event: the user has selected a system type.
     */
    public void systemTypeSelected(String systemType, boolean duringInitialization)
    {
        subsystemFactorySuppliedWizardPages = getAdditionalWizardPages(systemType);
        if (!duringInitialization)
          getWizard().getContainer().updateButtons();        
    }
    
    /*
     * Private method to get all the wizard pages from all the subsystem factories, given a
     *  system type.
     */
    protected ISystemNewConnectionWizardPage[] getAdditionalWizardPages(String systemType)
    {
    	// this query is expensive, so only do it once...
    	subsystemFactorySuppliedWizardPages = (ISystemNewConnectionWizardPage[])ssfWizardPagesPerSystemType.get(systemType);
    	if (subsystemFactorySuppliedWizardPages == null)
    	{
    	    // query all affected subsystems for their list of additional wizard pages...
    		Vector additionalPages = new Vector();
    		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
            ISubSystemConfiguration[] factories = sr.getSubSystemConfigurationsBySystemType(systemType, true);
            for (int idx=0; idx<factories.length; idx++)
            {
            	ISubsystemConfigurationAdapter adapter = (ISubsystemConfigurationAdapter)factories[idx].getAdapter(ISubsystemConfigurationAdapter.class);
            		
            	IWizardPage[] pages = adapter.getNewConnectionWizardPages(factories[idx], getWizard());
            	if (pages != null)
            	{
            		for (int widx=0; widx<pages.length; widx++)
            		   additionalPages.addElement(pages[widx]);
            	}
            }
            subsystemFactorySuppliedWizardPages = new ISystemNewConnectionWizardPage[additionalPages.size()];
            for (int idx=0; idx<subsystemFactorySuppliedWizardPages.length; idx++)
           	   subsystemFactorySuppliedWizardPages[idx] = (ISystemNewConnectionWizardPage)additionalPages.elementAt(idx);
            ssfWizardPagesPerSystemType.put(systemType, subsystemFactorySuppliedWizardPages);
        }    	
        return subsystemFactorySuppliedWizardPages;
    }
    
    /**
     * Return true if there are additional pages. This decides whether to enable the Next button 
     *  on the main page
     */
    protected boolean hasAdditionalPages()
    {
    	return (subsystemFactorySuppliedWizardPages != null) && (subsystemFactorySuppliedWizardPages.length>0);
    }
    
    /**
     * Return the first additional page to show when user presses Next on the main page
     */
    protected IWizardPage getFirstAdditionalPage()
    {
    	if ((subsystemFactorySuppliedWizardPages != null) && (subsystemFactorySuppliedWizardPages.length>0))
    	{
    	  IWizardPage previousPage = (IWizardPage)mainPage;
    	  for (int idx=0; idx<subsystemFactorySuppliedWizardPages.length; idx++)
    	  {
    	  	 subsystemFactorySuppliedWizardPages[idx].setPreviousPage(previousPage);
    	  	 previousPage = subsystemFactorySuppliedWizardPages[idx];
    	  }
    	  return subsystemFactorySuppliedWizardPages[0];
    	}
    	else
    	  return null;
    }
    
    // --------------------
    // PARENT INTERCEPTS...
    // --------------------
    
    /**
     * Intercept of Wizard method so we can get the Next button behaviour to work right for the
     *  dynamically managed additional wizard pages.
     */
    public IWizardPage getNextPage(IWizardPage page) 
    {
	    if (!hasAdditionalPages() || (page==rnmProfilePage))
	      return null;
	    else
	    {
	      int index = getAdditionalPageIndex(page);
	      if ((index == (subsystemFactorySuppliedWizardPages.length - 1)))
		    // last page or page not found
		    return null;
	      return subsystemFactorySuppliedWizardPages[index + 1];	
	    }
    }

    /**
	 * @see org.eclipse.rse.ui.wizards.RSENewConnectionWizardDelegate#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getPreviousPage(IWizardPage page) {
		return null;
	}

	private int getAdditionalPageIndex(IWizardPage page)
    {
    	  for (int idx=0; idx<subsystemFactorySuppliedWizardPages.length; idx++)
    	  {
    	  	 if (page == subsystemFactorySuppliedWizardPages[idx])
    	  	   return idx;
    	  }
    	  return -1;
    }

    /**
     * Intercept of Wizard method so we can take into account our additional pages
     */
    public boolean canFinish() 
    {
    	boolean ok = mainPage.isPageComplete();
    	
    	if (ok && hasAdditionalPages())
    	{
    	  for (int idx=0; ok && (idx<subsystemFactorySuppliedWizardPages.length); idx++)
    	  	 ok = subsystemFactorySuppliedWizardPages[idx].isPageComplete();
    	}
    	return ok;
    }

	/**
	 * @see org.eclipse.rse.ui.wizards.RSENewConnectionWizardDelegate#systemTypeChanged(org.eclipse.rse.core.IRSESystemType)
	 */
	public void systemTypeChanged(IRSESystemType systemType) {
		setSystemType(systemType);
		restrictSystemType(systemType.getName());
		getMainPage().setTitle(getPageTitle());
        subsystemFactorySuppliedWizardPages = getAdditionalWizardPages(systemType.getName());
	}
}