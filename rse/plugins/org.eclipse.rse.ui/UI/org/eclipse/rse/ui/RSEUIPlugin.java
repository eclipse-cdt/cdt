/********************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - moved methods to SystemPreferencesManager.
 * Uwe Stieber (Wind River) - bugfixing.
 * David Dykstal (IBM) - 168870: move core function from UI to core
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [185554] Remove dynamicPopupMenuExtensions extension point
 * Martin Oberhuber (Wind River) - [174945] Remove obsolete icons from rse.shells.ui
 * Martin Oberhuber (Wind River) - [186525] Move keystoreProviders to core
 * Martin Oberhuber (Wind River) - [186523] Move subsystemConfigurations from UI to core
 * Martin Oberhuber (Wind River) - [185552] Remove remoteSystemsViewPreferencesActions extension point
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [186779] Fix IRSESystemType.getAdapter()
 * David Dykstal (IBM) - [189858] Delay the creation of the remote systems project
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 * David Dykstal (IBM) - [191038] initialize SystemRegistryUI without a log file, it was not used
 * David McKnight   (IBM)        - [196838] Don't recreate local after it has been deleted
 * David Dykstal (IBM) - [197036] formatted the initialize job to be able to read it
 * Martin Oberhuber (Wind River) - [215820] Move SystemRegistry implementation to Core
 * David Dykstal (IBM) - [197167] adding notification and waiting for RSE model
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 * David McKnight   (IBM)        - [236505] Remote systems dialog not working
 * David McKnight   (IBM)        - [238288] use ImageRegistry to store/retrieve images for RSE label providers
 ********************************************************************************/

package org.eclipse.rse.ui;

import java.net.URL;
import java.util.Vector;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.model.ISystemProfileManager;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.internal.core.model.SystemProfileManager;
import org.eclipse.rse.internal.core.model.SystemRegistry;
import org.eclipse.rse.internal.ui.RSESystemTypeAdapterFactory;
import org.eclipse.rse.internal.ui.RSEUIInitJob;
import org.eclipse.rse.internal.ui.subsystems.SubSystemConfigurationProxyAdapterFactory;
import org.eclipse.rse.internal.ui.view.SubSystemConfigurationAdapterFactory;
import org.eclipse.rse.internal.ui.view.SystemViewAdapterFactory;
import org.eclipse.rse.internal.ui.view.team.SystemTeamViewResourceAdapterFactory;
import org.eclipse.rse.persistence.IRSEPersistenceManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageFile;
import org.eclipse.rse.ui.internal.model.SystemRegistryUI;
import org.eclipse.rse.ui.model.ISystemRegistryUI;
import org.osgi.framework.BundleContext;


/**
 * Plugin for the core remote systems support.
 */
public class RSEUIPlugin extends SystemBasePlugin
{

	public static final String PLUGIN_ID  = "org.eclipse.rse.ui"; //$NON-NLS-1$
	public static final String HELPPREFIX = "org.eclipse.rse.ui."; //$NON-NLS-1$

    public static final boolean INCLUDE_LOCAL_YES = true;
    public static final boolean INCLUDE_LOCAL_NO = false;
	private static RSEUIPlugin         inst = null;

	private static SystemMessageFile 	messageFile = null;
    private static SystemMessageFile    defaultMessageFile = null;

    private Vector viewSuppliers = new Vector();
    private SystemViewAdapterFactory svaf; // for fastpath access
    private SystemTeamViewResourceAdapterFactory svraf; // for fastpath
	private boolean loggingSystemMessageLine = false;

	/**
	 * Constructor for SystemsPlugin
	 */
	public RSEUIPlugin()
	{
		super();

   	    if (inst == null)
   	    {
   	        inst = this;
   	    }
	}

    /**
     * Return singleton. Same as inherited getBaseDefault but returned object
     *  is typed as RSEUIPlugin versus SystemBasePlugin.
     */
    public static RSEUIPlugin getDefault()
    {
	    return inst;
    }

	/**
	 * Initializes preferences.
	 */
	public void initializeDefaultPreferences() {
		SystemPreferencesManager.initDefaults();
	}

	/**
	 * Set whether or not to log the messages shown on the system message line for dialogs
	 * and wizards. These message are typically validation messages for fields.
	 * These are logged using the RSE logging settings. The default is to not log
	 * these messages.
	 * @param flag true if logging of these messages is desired, false otherwise.
	 */
	public void setLoggingSystemMessageLine(boolean flag) {
		loggingSystemMessageLine = flag;
	}

	/**
	 * @return true if we are logging messages displayed on the system message line.
	 */
	public boolean getLoggingSystemMessageLine() {
		return loggingSystemMessageLine;
	}

    /* (non-Javadoc)
     * @see org.eclipse.rse.core.SystemBasePlugin#initializeImageRegistry()
     */
    protected void initializeImageRegistry()
    {
    	//SystemElapsedTimer timer = new SystemElapsedTimer();
    	//timer.setStartTime();
    	String path = getIconPath();
    	// Wizards...
    	/*
	    putImageInRegistry(ISystemConstants.ICON_SYSTEM_NEWWIZARD_ID,
						   path+ISystemConstants.ICON_SYSTEM_NEWWIZARD);
		*/
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_NEWPROFILEWIZARD_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_NEWPROFILEWIZARD);

	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_NEWCONNECTIONWIZARD_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_NEWCONNECTIONWIZARD);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_NEWFILTERPOOLWIZARD_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_NEWFILTERPOOLWIZARD);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_NEWFILTERWIZARD_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_NEWFILTERWIZARD);
	    //putImageInRegistry(ISystemConstants.ICON_SYSTEM_NEWFILTERSTRINGWIZARD_ID,
		//				   path+ISystemConstants.ICON_SYSTEM_NEWFILTERSTRINGWIZARD);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_NEWFILEWIZARD_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_NEWFILEWIZARD);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_NEWFOLDERWIZARD_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_NEWFOLDERWIZARD);

    	// Things...

	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_PROFILE_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_PROFILE);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_PROFILE_ACTIVE_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_PROFILE_ACTIVE);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_CONNECTION_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_CONNECTION);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_CONNECTIONLIVE_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_CONNECTIONLIVE);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_FILTERPOOL_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_FILTERPOOL);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_FILTER_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_FILTER);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_FILTERSTRING_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_FILTERSTRING);
	    //putImageInRegistry(ISystemConstants.ICON_SYSTEM_FILE_ID,
		//				   path+ISystemConstants.ICON_SYSTEM_FILE);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_FOLDER_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_FOLDER);
	    //putImageInRegistry(ISystemConstants.ICON_SYSTEM_FOLDEROPEN_ID,
		//				   path+ISystemConstants.ICON_SYSTEM_FOLDEROPEN);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_ROOTDRIVE_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_ROOTDRIVE);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_ROOTDRIVEOPEN_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_ROOTDRIVEOPEN);

		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_PROCESS_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_PROCESS);

//		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_TARGET_ID,
	//					   path+ISystemIconConstants.ICON_SYSTEM_TARGET);

		// Message icons: REDUNDANT
		/*
	    putImageInRegistry(ISystemConstants.ICON_SYSTEM_SMALLERROR_ID,
						   path+ISystemConstants.ICON_SYSTEM_SMALLERROR);
	    putImageInRegistry(ISystemConstants.ICON_SYSTEM_SMALLWARNING_ID,
						   path+ISystemConstants.ICON_SYSTEM_SMALLWARNING);
	    putImageInRegistry(ISystemConstants.ICON_SYSTEM_SMALLINFO_ID,
						   path+ISystemConstants.ICON_SYSTEM_SMALLINFO);
	    */

    	// New Actions...
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_NEW_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_NEW);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_NEWPROFILE_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_NEWPROFILE);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_NEWCONNECTION_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_NEWCONNECTION);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_NEWFILTERPOOL_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_NEWFILTERPOOL);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_NEWFILTERPOOLREF_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_NEWFILTERPOOLREF);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_NEWFILTER_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_NEWFILTER);
	    //putImageInRegistry(ISystemConstants.ICON_SYSTEM_NEWFILTERSTRING_ID,
		//				   path+ISystemConstants.ICON_SYSTEM_NEWFILTERSTRING);

    	// Other Actions...
	    //putImageInRegistry(ISystemConstants.ICON_SYSTEM_PULLDOWN_ID,
		//				   path+ISystemConstants.ICON_SYSTEM_PULLDOWN);
    	putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_LOCK_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_LOCK);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_MOVEUP_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_MOVEUP);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_MOVEDOWN_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_MOVEDOWN);
	    //putImageInRegistry(ISystemConstants.ICON_SYSTEM_COPY_ID,
		//				   path+ISystemConstants.ICON_SYSTEM_COPY);
		//putImageInRegistry(ISystemConstants.ICON_SYSTEM_PASTE_ID,
		//				   path+ISystemConstants.ICON_SYSTEM_PASTE);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_MOVE_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_MOVE);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_CLEAR_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_CLEAR);

	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_CLEAR_ALL_ID,
				   path+ISystemIconConstants.ICON_SYSTEM_CLEAR_ALL);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_CLEAR_SELECTED_ID,
				   path+ISystemIconConstants.ICON_SYSTEM_CLEAR_SELECTED);


	    //putImageInRegistry(ISystemConstants.ICON_SYSTEM_DELETE_ID,
		//				   path+ISystemConstants.ICON_SYSTEM_DELETE);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_DELETEREF_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_DELETEREF);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_RENAME_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_RENAME);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_RUN_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_RUN);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_STOP_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_STOP);
//	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_COMPILE_ID,
//						   path+ISystemIconConstants.ICON_SYSTEM_COMPILE);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_MAKEPROFILEACTIVE_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_MAKEPROFILEACTIVE);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_MAKEPROFILEINACTIVE_ID,
				   path+ISystemIconConstants.ICON_SYSTEM_MAKEPROFILEINACTIVE);

	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_CHANGEFILTER_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_CHANGEFILTER);
	    //putImageInRegistry(ISystemConstants.ICON_SYSTEM_CHANGEFILTERSTRING_ID,
		//				   path+ISystemConstants.ICON_SYSTEM_CHANGEFILTERSTRING);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_SELECTPROFILE_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_SELECTPROFILE);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_SELECTFILTERPOOLS_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_SELECTFILTERPOOLS);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_WORKWITHFILTERPOOLS_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_WORKWITHFILTERPOOLS);
//	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_WORKWITHUSERACTIONS_ID,
//						   path+ISystemIconConstants.ICON_SYSTEM_WORKWITHUSERACTIONS);
//	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_WORKWITHNAMEDTYPES_ID,
//						   path+ISystemIconConstants.ICON_SYSTEM_WORKWITHNAMEDTYPES);
//	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_WORKWITHCOMPILECMDS_ID,
//						   path+ISystemIconConstants.ICON_SYSTEM_WORKWITHCOMPILECMDS);

	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_REFRESH_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_REFRESH);

        putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_NEWFILE_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_NEWFILE);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_NEWFOLDER_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_NEWFOLDER);
	    //putImageInRegistry(ISystemConstants.ICON_SYSTEM_COLLAPSEALL_ID,
		//				   path+ISystemConstants.ICON_SYSTEM_COLLAPSEALL); // defect 41203 D54577







        // System view icons...
	    //putImageInRegistry(ISystemConstants.ICON_SYSTEM_VIEW_ID, // only needed from plugin.xml
					//	   path+ISystemConstants.ICON_SYSTEM_VIEW);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_ERROR_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_ERROR);

	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_INFO_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_INFO);

	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_INFO_TREE_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_INFO_TREE);

	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_CANCEL_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_CANCEL);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_HELP_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_HELP);

	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_EMPTY_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_EMPTY);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_OK_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_OK);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_WARNING_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_WARNING);

		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_BLANK_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_BLANK);

		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_SEARCH_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_SEARCH);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_SEARCH_RESULT_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_SEARCH_RESULT);

		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_SHOW_TABLE_ID,
							path + ISystemIconConstants.ICON_SYSTEM_SHOW_TABLE);

		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_SHOW_MONITOR_ID,
				path + ISystemIconConstants.ICON_SYSTEM_SHOW_MONITOR);

		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_PERSPECTIVE_ID,
							path + ISystemIconConstants.ICON_SYSTEM_PERSPECTIVE);

		putImageInRegistry(ISystemIconConstants.ICON_SEARCH_REMOVE_SELECTED_MATCHES_ID,
							path + ISystemIconConstants.ICON_SEARCH_REMOVE_SELECTED_MATCHES);

		putImageInRegistry(ISystemIconConstants.ICON_SEARCH_REMOVE_ALL_MATCHES_ID,
							path + ISystemIconConstants.ICON_SEARCH_REMOVE_ALL_MATCHES);

        /**
	    putImageInRegistry(ISystemConstants.ICON_INHERITWIDGET_LOCAL_ID,
						   path+ISystemConstants.ICON_INHERITWIDGET_LOCAL);
	    putImageInRegistry(ISystemConstants.ICON_INHERITWIDGET_INHERIT_ID,
						   path+ISystemConstants.ICON_INHERITWIDGET_INHERIT);
	    putImageInRegistry(ISystemConstants.ICON_INHERITWIDGET_INTERIM_ID,
						   path+ISystemConstants.ICON_INHERITWIDGET_INTERIM);
        */

		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_ARROW_UP_ID,
				   path+ISystemIconConstants.ICON_SYSTEM_ARROW_UP);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_ARROW_DOWN_ID,
				   path+ISystemIconConstants.ICON_SYSTEM_ARROW_DOWN);

		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_CONNECTOR_SERVICE_ID,
				   path+ISystemIconConstants.ICON_SYSTEM_CONNECTOR_SERVICE);

		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_SERVICE_ID,
				   path+ISystemIconConstants.ICON_SYSTEM_SERVICE);

		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_LAUNCHER_CONFIGURATION_ID,
				   path+ISystemIconConstants.ICON_SYSTEM_LAUNCHER_CONFIGURATION);

		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_PROPERTIES_ID,
				   path+ISystemIconConstants.ICON_SYSTEM_PROPERTIES);

        // close to 1 second...
        //timer.setEndTime();
        //System.out.println("Time to load images: "+timer);
    }



    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception
	{
        super.start(context);

	   	messageFile = getMessageFile("systemmessages.xml"); //$NON-NLS-1$
	   	defaultMessageFile = getDefaultMessageFile("systemmessages.xml"); //$NON-NLS-1$

	   	//Force load the SystemRegistry - TODO Is this really necessary?
	   	RSECorePlugin.getTheSystemRegistry();

    	IAdapterManager manager = Platform.getAdapterManager();

	    // DKM
	    // for subsystem factories
	    SubSystemConfigurationAdapterFactory ssfaf = new SubSystemConfigurationAdapterFactory();
	    ssfaf.registerWithManager(manager);

	    RSESystemTypeAdapterFactory rseSysTypeFactory = new RSESystemTypeAdapterFactory();
	    rseSysTypeFactory.registerWithManager(manager);

	    manager.registerAdapters(new SubSystemConfigurationProxyAdapterFactory(), ISubSystemConfigurationProxy.class);

	    svaf = new SystemViewAdapterFactory();
	    svaf.registerWithManager(manager);

	    svraf = new SystemTeamViewResourceAdapterFactory();
	    svraf.registerWithManager(manager);

	    Job initJob = new RSEUIInitJob();
	    initJob.schedule();
	}

    /**
	 * For fastpath access to our adapters for non-local objects in our model.
	 * Exploits the knowledge we use singleton adapters.
	 *
	 * @since 3.0 returning IAdapterFactory rather than SystemViewAdapterFactory
	 */
    public IAdapterFactory getSystemViewAdapterFactory()
    {
    	return svaf;
    }

    /**
     * Restart the whole thing after a team synchronization
     */
    public void restart()
    {
    	if (RSECorePlugin.isTheSystemRegistryActive()) {
        	ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();

        	// disconnect all active connections
        	disconnectAll(false); // don't save ?
        	// collapse and flush all nodes in all views
        	sr.fireEvent(new SystemResourceChangeEvent("dummy", ISystemResourceChangeEvents.EVENT_COLLAPSE_ALL, null)); //$NON-NLS-1$

        	// allow child classes to override
        	closeViews();

        	// clear in-memory settings for all filter pools and subsystems
        	ISubSystemConfigurationProxy[] proxies = sr.getSubSystemConfigurationProxies();
        	if (proxies != null) {
          	  	for (int idx=0; idx < proxies.length; idx++)
           	  	   proxies[idx].reset();
        	}
        	// clear in-memory settings for all profiles
        	SystemProfileManager.clearDefault();

        	// rebuild profiles
        	RSECorePlugin.getTheSystemProfileManager(); // create folders per profile
        	// clear in-memory settings for all connections, then restore from disk
        	((SystemRegistry)sr).reset();
        	// restore in-memory settings for all filter pools and subsystems
        	if (proxies != null) {
          	  	for (int idx=0; idx < proxies.length; idx++)
           	  	   proxies[idx].restore();
        	}

        	// refresh GUIs
        	sr.fireEvent(new SystemResourceChangeEvent(sr, ISystemResourceChangeEvents.EVENT_REFRESH, null));

            // allow child classes to override
            openViews();
    	}
    }

    /**
     * Close or reset views prior to full refresh after team synch
     */
    public void closeViews()
    {
    	for (int idx=0; idx<viewSuppliers.size(); idx++)
    	{
    	   try {
    	     ((ISystemViewSupplier)viewSuppliers.elementAt(idx)).closeViews();
    	   } catch (Exception exc)
    	   {
    	   }
    	}
    }

    /**
     * Restore views prior to full refresh after team synch
     */
    public void openViews()
    {
    	for (int idx=0; idx<viewSuppliers.size(); idx++)
    	{
    	   try {
    	     ((ISystemViewSupplier)viewSuppliers.elementAt(idx)).openViews();
    	   } catch (Exception exc)
    	   {
    	   }
    	}
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
    	// disconnect all active connections
    	disconnectAll(true);

	    // remove workspace listener for our project
	    SystemResourceManager.endResourceEventListening();

        // call this last
        super.stop(context);
    }

    /**
     * Disconnect all subsystems
     */
    protected void disconnectAll(boolean doSave)
    {
    	if (RSECorePlugin.isTheSystemRegistryActive())
    	{
    	  ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
    	  ISubSystemConfigurationProxy[] proxies = sr.getSubSystemConfigurationProxies();
    	  if (proxies != null)
    	  {
    	  	for (int idx=0; idx < proxies.length; idx++)
    	  	{
    	  	   //System.out.println("In shutdown. proxy " + proxies[idx].getId() + " active? " + proxies[idx].isSubSystemConfigurationActive());
    	  	   if (proxies[idx].isSubSystemConfigurationActive())
    	  	   {
    	  	   	 ISubSystemConfiguration ssf = proxies[idx].getSubSystemConfiguration();
    	  	   	 try
    	  	   	 {
    	  	        ssf.disconnectAllSubSystems();
    	  	   	 } catch(Exception exc)
    	  	   	 {
    	  	   	 	logError("Error disconnecting for "+ssf.getId(),exc); //$NON-NLS-1$
    	  	   	 }
    	  	   	 if (doSave)
    	  	   	 {
    	  	   	   try
    	  	   	   {
    	  	         // TODO on shutdown classloader might not be able to do this properly
    	  	   		 // ssf.commit();
    	  	   	   } catch(Exception exc)
    	  	   	   {
    	  	   	 	  logError("Error saving subsystems for "+ssf.getId(),exc); //$NON-NLS-1$
    	  	   	   }
    	  	   	 }
    	  	   }
    	  	}
    	  }
    	}
    }

    /**
     * Test if the SystemRegistry has been instantiated already.
     * Use this when you don't want to start the system registry as a side effect of retrieving it.
     * @return <code>true</code> if the System Registry has been instantiated already.
     * @deprecated use {@link RSECorePlugin#isTheSystemRegistryActive()}
     */
    public boolean isSystemRegistryActive()
    {
    	return RSECorePlugin.isTheSystemRegistryActive();
    }

    /**
     * Return the persistence manager used for persisting RSE profiles.
     * @return the persistence manager used for persisting RSE profiles
     * @deprecated use {@link RSECorePlugin#getThePersistenceManager()}
     */
    public IRSEPersistenceManager getPersistenceManager()
    {
    	return RSECorePlugin.getThePersistenceManager();
    }

    /**
     * Return the SystemRegistryUI singleton.
	 * @return the SystemRegistryUI singleton.
     */
    public static ISystemRegistryUI getTheSystemRegistryUI()
    {
    	return SystemRegistryUI.getInstance();
    }

	/**
	 * Return the master profile manager singleton.
	 * @return the RSE Profile Manager Singleton.
	 * @deprecated use {@link RSECorePlugin#getTheSystemProfileManager()}
	 */
	public static ISystemProfileManager getTheSystemProfileManager()
	{
		return RSECorePlugin.getTheSystemProfileManager();
	}

    /**
     * Check if the SystemRegistry has been instantiated already.
     * Use this when you don't want to start the system registry as a side effect
     * of retrieving it.
     * @return <code>true</code> if the System Registry has been instantiated already.
     * @deprecated use {@link RSECorePlugin#isTheSystemRegistryActive()}
     */
    public static boolean isTheSystemRegistryActive() {
    	return RSECorePlugin.isTheSystemRegistryActive();
    }

	/**
	 * @return The URL to the message file DTD. Null if it is not found.
	 */
	public URL getMessageFileDTD() {
		URL result = getBundle().getEntry("/messageFile.dtd"); //$NON-NLS-1$
		return result;
	}

	/**
	 * Load a message file for this plugin.
	 * @param messageFileName - the name of the message xml file. Will look for it in this plugin's install folder.
	 * @return a message file object containing the parsed contents of the message file, or null if not found.
	 */
    public SystemMessageFile getMessageFile(String messageFileName)
    {
       return loadMessageFile(getBundle(), messageFileName);
    }

	/**
	 * Load a default message file for this plugin for cases where messages haven't been translated.
	 * @param messageFileName - the name of the message xml file. Will look for it in this plugin's install folder.
	 * @return a message file object containing the parsed contents of the message file, or null if not found.
	 */
	public SystemMessageFile getDefaultMessageFile(String messageFileName)
	{
	   return loadDefaultMessageFile(getBundle(), messageFileName);
	}

    /**
     * Return this plugin's message file. Assumes it has already been loaded via a call to getMessageFile.
     */
    public static SystemMessageFile getPluginMessageFile()
    {
    	return messageFile;
    }

    public SystemMessage getMessage(String msgId)
    {
    	return getPluginMessage(msgId);
    }

	/**
	 * Retrieve a message from this plugin's message file
	 * @param msgId - the ID of the message to retrieve. This is the concatenation of the
	 *   message's component abbreviation, subcomponent abbreviation, and message ID as declared
	 *   in the message xml file.
	 */
    public static SystemMessage getPluginMessage(String msgId)
    {
    	SystemMessage msg = getMessage(messageFile, msgId);
    	if (msg == null)
    	{
    		msg = getMessage(defaultMessageFile, msgId);
    	}
    	return msg;
    }
	/**
	 * Retrieve a message from this plugin's message file and do multiple substitution on it.
	 * @param msgId - the ID of the message to retrieve. This is the concatenation of the
	 *   message's component abbreviation, subcomponent abbreviation, and message ID as declared
	 *   in the message xml file.
	 * @param subsVars - an array of objects to substitute in for %1, %2, etc
	 */
	public static SystemMessage getPluginMessage(String msgId, Object[] subsVars)
	{
		SystemMessage msg = getMessage(messageFile, msgId);
		if (msg == null)
		{
			msg = getMessage(defaultMessageFile, msgId);
		}
		if ((msg != null) && (subsVars!=null) && (subsVars.length>0) && (msg.getNumSubstitutionVariables()>0))
		{
			msg.makeSubstitution(subsVars);
		}
		return msg;
	}
	/**
	 * Retrieve a message from this plugin's message file and do single substitution on it.
	 * @param msgId - the ID of the message to retrieve. This is the concatenation of the
	 *   message's component abbreviation, subcomponent abbreviation, and message ID as declared
	 *   in the message xml file.
	 * @param subsVar - an array of objects to substitute in for %1, %2, etc.
	 * @return the message.
	 */
	public static SystemMessage getPluginMessage(String msgId, Object subsVar)
	{
		SystemMessage msg = getMessage(messageFile, msgId);
		if (msg == null)
		{
			msg = getMessage(defaultMessageFile, msgId);
		}
		if ((msg != null) && (subsVar!=null) && (msg.getNumSubstitutionVariables()>0))
		{
			msg.makeSubstitution(subsVar);
		}
		return msg;
	}

    /**
     * Register a view supplier so we can ask them to participate in team synchs
     */
    public void registerViewSupplier(ISystemViewSupplier vs)
    {
    	viewSuppliers.add(vs);
    }
    /**
     * UnRegister a previously registered view supplier
     */
    public void unRegisterViewSupplier(ISystemViewSupplier vs)
    {
    	if (viewSuppliers.contains(vs))
    	  viewSuppliers.remove(vs);
    }

}