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

package org.eclipse.rse.subsystems.files.core.subsystems;
import java.util.Vector;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.IFileConstants;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.subsystems.files.core.util.ValidatorFileFilterString;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorFileName;
import org.eclipse.rse.ui.validators.ValidatorFolderName;
import org.eclipse.rse.ui.wizards.ISystemNewConnectionWizardPage;


/**
 * Specialization for file subsystem factories.
 * It is subclassed via use of a Rose model and MOF/EMF, or better yet 
 *  by subclassing {@link org.eclipse.rse.core.servicesubsystem.impl.FileServiceSubSystemConfiguration}.
 * 
 * @see RemoteFileSubSystem
 */

public abstract class RemoteFileSubSystemConfiguration extends SubSystemConfiguration implements IRemoteFileSubSystemConfiguration,ISubSystemConfiguration
{
	
	protected boolean unixStyle = false;
    protected String translatedType;
    	
	/**
	 * Constructor
	 */
	public RemoteFileSubSystemConfiguration() 
	{
		super();
	}
	/**
	 * Tell us if this is a unix-style file system or a windows-style file system. The
	 * default is windows.
	 * Child classes must call this, so we know how to respond to separator and path-separator requests.
	 */
	protected void setIsUnixStyle(boolean isUnixStyle)
	{
		this.unixStyle = isUnixStyle;
	}
	/**
	 * Tell us if this is a unix-style file system or a windows-style file system. The
	 * default is windows.
	 * Child classes must call this, so we know how to respond to separator and path-separator requests.
	 */
	public boolean isUnixStyle()
	{
		return unixStyle;
	}
	/**
	 * Tell us if this file system is case sensitive. The default is isUnixStyle(), and so should
	 *  rarely need to be overridden.
	 */
	public boolean isCaseSensitive()
	{
		return isUnixStyle();
	}
	/**
	 * Tell us if this subsystem factory supports targets, which are destinations for 
	 *   pushes and builds. Normally only true for file system factories.
	 * <p>We return true.
	 */
	public boolean supportsTargets()
	{
		return true;	
	}
	/**
	 * Tell us if this subsystem factory supports server launch properties, which allow the user
	 *  to configure how the server-side code for these subsystems are started. There is a Server
	 *  Launch Setting property page, with a pluggable composite, where users can configure these 
	 *  properties. 
	 * <br> By default we return false.
	 */
	public abstract boolean supportsServerLaunchProperties(IHost host);
	/**
	 * Return true if subsystems of this factory support the environment variables property.
	 * Return true to show it, return false to hide it. We return true.
	 */
	public boolean supportsEnvironmentVariablesPropertyPage()
	{
		return true;
	}
	
	/**
	 * By default this returns true.  Override if the file subsystem
	 * does not support search.
	 */
	public boolean supportsSearch()
	{
		return true;
	}
	


    // --------------------------------
    // VALIDATOR METHODS...
    // --------------------------------   	
    /**
     * Return validator used in filter string dialog for the path part of the filter string.
     * By default, returns ValidatorPathName which does very limited checking. 
     * Override as appropriate.
     */
    public ISystemValidator getPathValidator()
    {
        return new org.eclipse.rse.ui.validators.ValidatorPathName();    	
    }
    /**
     * Return validator used in filter string dialog for the file part of the filter string
     * By default, returns ValidatorFileFilterString.
     * Override as appropriate.
     */
    public ISystemValidator getFileFilterStringValidator()
    {
        return new ValidatorFileFilterString(this);    	
    }
    /**
     * Return validator used when creating or renaming files
     */
    public ValidatorFileName getFileNameValidator()
    {
    	return new ValidatorFileName();
    }
    /**
     * Return validator used when creating or renaming folders
     */
    public ValidatorFolderName getFolderNameValidator()
    {
    	return new ValidatorFolderName();
    }
    // --------------------------------
    // FILE SYSTEM ATTRIBUTE METHODS...
    // --------------------------------   	
	/**
	 * Return in string format the character used to separate folders. Eg, "\" or "/"
	 */
    public String getSeparator()
    {
    	return (unixStyle) ? IFileConstants.SEPARATOR_UNIX : IFileConstants.SEPARATOR_WINDOWS;
    }
	/**
	 * Return in character format the character used to separate folders. Eg, "\" or "/"
	 */    
    public char getSeparatorChar()
    {
    	return (unixStyle) ? IFileConstants.SEPARATOR_CHAR_UNIX : IFileConstants.SEPARATOR_CHAR_WINDOWS;
    }
	/**
	 * Return in string format the character used to separate paths. Eg, ";" or ":"
	 */    
    public String getPathSeparator()
    {
    	return (unixStyle) ? IFileConstants.PATH_SEPARATOR_UNIX : IFileConstants.PATH_SEPARATOR_WINDOWS;
    }
	/**
	 * Return in char format the character used to separate paths. Eg, ";" or ":"
	 */    
    public char getPathSeparatorChar()
    {
    	return (unixStyle) ? IFileConstants.PATH_SEPARATOR_CHAR_UNIX : IFileConstants.PATH_SEPARATOR_CHAR_WINDOWS;
    }
	/**
	 * Return as a string the line separator.
	 */
	public String getLineSeparator()
	{
		if (isUnixStyle()) {
			return "\n";
		}
		else {	
			return "\r\n";
		}
	}    
    // --------------------------------------------
    // PARENT METHODS RELATED TO WHAT WE SUPPORT...
    // --------------------------------------------
    /**
     * We return true.
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsUserId()
     */
    public boolean supportsUserId()
    {
    	return true;
    }
	/**
     * We return true.
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsSubSystemConnect()
	 */
	public boolean supportsSubSystemConnect()
	{
		return true;
	}
    /**
     * We return true.
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#isPortEditable()
     */
    public boolean isPortEditable()
    {
    	return true;    
    }	
	
	/**
	 * We return false.
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsCommands()
	 */
	public boolean supportsCommands()
	{
		return false;
	}
	/**
	 * We return false.
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsProperties()
	 */
	public boolean supportsProperties()
	{
		return false;
	}
    /**
     * We return true.
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsFilters()
     */
    public boolean supportsFilters()
    {
    	return true;
    }
    
	/** 
	 * Return true if filters of this subsystem factory support dropping into.
	 */
	public boolean supportsDropInFilters()
	{
	    return true;
	}
	
	/**
	 * Indicates that a drop on a filter will be handled as a copy by the file subsystem
	 * rather than having a filter update.
	 */
	public boolean providesCustomDropInFilters()
	{
		return true;
	}

    /**
     * We return supportsFilters()
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsNestedFilters()
     */
    public boolean supportsNestedFilters()
    {
    	return supportsFilters();
    }

	/**
	 * Return true if you support user-defined actions for the remote system objects returned from expansion of
	 *  subsystems created by this subsystem factory.
	 * <p>We return true.
	 * @see #createActionSubSystem()
	 */
	public boolean supportsUserDefinedActions()
	{
		return true;
	}
	
	

	/**
	 * Return true if you support user-defined/managed named file types
	 * <p>We return true
	 */
	public boolean supportsFileTypes()
	{
		return true;
	}    

	/**
	 * Return true if you support compile actions for the remote system objects returned from expansion of
	 *  subsystems created by this subsystem factory.
	 * <p>
	 * By returning true, user sees a "Work with->Compile Commands..." action item in the popup menu for this
	 *  subsystem. The action is supplied by the framework, but is populated using overridable methods in this subsystem.
	 * <p>We return false, but really we expect subclasses to return true
	 */
	public boolean supportsCompileActions()
	{
		return false;
	}
    
    // -------------------------------------------------------
    // PARENT METHODS RELATED TO FILTERS...
    // ... ONLY INTERESTING IF supportsFilters() returns true!
    // -------------------------------------------------------

	/**
	 * Override from parent.
	 * <p>
	 * Here we create the default filter pool for this subsystem factory, and populate it
	 *  with default filters. 
	 * <p>
	 * This is overridden for local, windows and ifs file subsystem factories, so what we
	 *  default here applies to Unix and Linux only.
	 */
	protected ISystemFilterPool createDefaultFilterPool(ISystemFilterPoolManager mgr)
	{
		//SystemPlugin.logDebugMessage(this.getClass().getName(),"in createDefaultFilterPool for remote file subsystem factory");
		ISystemFilterPool pool = null;
		try {
		  // -----------------------------------------------------
		  // create a pool named filters
		  // -----------------------------------------------------      			  
		  pool = mgr.createSystemFilterPool(getDefaultFilterPoolName(mgr.getName(), getId()), true); // true=>is deletable by user
		  //System.out.println("Pool created");
		  // ---------------------------------------------------------------------------------------------
		  // create default filters in that pool iff this is the user's private profile we are creating...
		  // ---------------------------------------------------------------------------------------------
		  if (isUserPrivateProfile(mgr))
		  {
				    
		      Vector filterStrings = new Vector();
		      /*
		      RemoteFileFilterString rootsFilterString = new RemoteFileFilterString(this);
		      filterStrings.add(rootsFilterString.toString());
		      mgr.createSystemFilter(pool, rb.getString(ISystemFileConstants.RESID_FILTER_ROOTS),filterStrings);    		    	
		      */
	

		      // ----------------------
		      // "My Home" filter...
		      // ----------------------
		      filterStrings = new Vector();
		      RemoteFileFilterString myHomeFilterString = new RemoteFileFilterString(this);
		      myHomeFilterString.setPath(getSeparator());
		      filterStrings.add("./*");
		      ISystemFilter filter = mgr.createSystemFilter(pool, SystemFileResources.RESID_FILTER_MYHOME,filterStrings);
		      filter.setNonChangable(true);
		      filter.setSingleFilterStringOnly(true);
		      
		      // ----------------------
		      // "Home" filter...
		      // ----------------------		  
		      String name = null;
		      String path = null;
		      if (mgr.getName().equals("Team"))
		      {
		  	    path = "/home";
		        name = SystemFileResources.RESID_FILTER_HOME;
		      }
		      else
		      {
		  	    path = "/home"; //"/home/username";
		        name = SystemFileResources.RESID_FILTER_USERHOME;
		      }
		      filterStrings.clear();
		      RemoteFileFilterString homeFilterString = new RemoteFileFilterString(this);
		      homeFilterString.setPath(path);
		      filterStrings.add(homeFilterString.toString());
	          mgr.createSystemFilter(pool, name, filterStrings);	
	          	          	          
	        
		      // ----------------------
		      // "Root Files" filter...
		      // ----------------------
		      filterStrings = new Vector();
		      RemoteFileFilterString rootFilesFilterString = new RemoteFileFilterString(this);
		      rootFilesFilterString.setPath(getSeparator());
		      filterStrings.add(rootFilesFilterString.toString());		      
		      mgr.createSystemFilter(pool, SystemFileResources.RESID_FILTER_ROOTFILES,filterStrings);		      
		  }
		  //else
		    //System.out.println("Did not create default filters because this is not the default private profile: " + mgr.getName());
		} catch (Exception exc)
		{
			SystemBasePlugin.logError("Error creating default filter pool",exc);
		}
		return pool;
	}

    /**
     * Return the translated string to show in the property sheet for the type property.
     */
    public String getTranslatedFilterTypeProperty(ISystemFilter selectedFilter)
    {
    	if (translatedType == null)
          translatedType = SystemFileResources.RESID_PROPERTY_FILE_FILTER_VALUE;    	  
    	return translatedType;
    }    

    // -------------------------------------------------------
    // PARENT METHODS RELATED TO SUBSYSTEMS... VERY IMPORTANT!
    // -------------------------------------------------------


    /**
     * Instantiate and return an instance of OUR subystem. Do not populate it yet though!
     * Eg:
     * <pre><code>
     *     	SubSystem subsys = ((AcmesubsysFactoryImpl)factory).createAcmeSubSystem();
     *     	return subsys;
     * </code></pre>
     * <b>note</b>This method should be abstract but MOF doesn't allow abstract impl classes at this point
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createSubSystemInternal(IHost)
     */
    public ISubSystem createSubSystemInternal(IHost conn)
    {
    	// example code for subclasses...
    	//SubSystem subsys = factory.createRemoteCmdSubSystem();
    	//return subsys;    	
    	return null;
    }

    /**
     * Populate a new subsystem with our unique attributes, and/or create default filter references.
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#initializeSubSystem(ISubSystem,ISystemNewConnectionWizardPage[])
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#getNewConnectionWizardPages(org.eclipse.jface.wizard.IWizard)
     */
    protected void initializeSubSystem(ISubSystem ss, ISystemNewConnectionWizardPage[] yourNewConnectionWizardPages)
    {
    	super.initializeSubSystem(ss, yourNewConnectionWizardPages); // add a reference to the default filter pool
    	
    	/** FIXME - decouple wiz pages from core - do we still need this?
    	// Find the "set port" page we contributed to the New Connection wizard, reads it value, and
    	//  default the subsystem's port value to it.
	    if (yourNewConnectionWizardPages != null)
	    {
	    	SystemFileNewConnectionWizardPage ourPage = null;
	    	for (int idx=0; (ourPage==null) && (idx<yourNewConnectionWizardPages.length); idx++)
	    	{
	    	   if (yourNewConnectionWizardPages[idx] instanceof SystemFileNewConnectionWizardPage)
	    	     ourPage = (SystemFileNewConnectionWizardPage)yourNewConnectionWizardPages[idx];
	    	}
	    	if ((ourPage != null) && !ourPage.isInformationalOnly())
	    	{
	    		int port = ourPage.getPortNumber();
	    		ss.getConnectorService().setPort(port);
	    	}
	    }
	    */
    }
    
    /**
	 * Return the default remote systems editor profile ID for files on this subsystem
	 */
	public String getEditorProfileID()
	{
		return "universal";	
	}
	
	

	


    /**
	 * Determines whether this factory is responsible for the creation of subsytems of the specified type
	 * Subsystem factories should override this to indicate which subsystems they support.
	 * 
	 * @param subSystemType type of subsystem
	 * @return whether this factory is for the specified subsystemtype
	 */
	public boolean isFactoryFor(Class subSystemType)
	{
		boolean isFor = IRemoteFileSubSystem.class.equals(subSystemType);
		return isFor;
	}
	
}