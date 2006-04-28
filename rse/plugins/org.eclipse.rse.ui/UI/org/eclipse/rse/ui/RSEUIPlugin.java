/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui;

import java.net.InetAddress;
import java.net.URL;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.ISystemViewSupplier;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPropertyPageExtension;
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.core.comm.ISystemKeystoreProvider;
import org.eclipse.rse.core.comm.SystemCommunicationsDaemon;
import org.eclipse.rse.core.comm.SystemKeystoreProviderManager;
import org.eclipse.rse.core.internal.subsystems.SubSystemConfigurationProxy;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.internal.model.SystemProfileManager;
import org.eclipse.rse.internal.model.SystemRegistry;
import org.eclipse.rse.internal.persistence.RSEPersistenceManager;
import org.eclipse.rse.model.ISystemProfileManager;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.model.SystemStartHere;
import org.eclipse.rse.persistence.IRSEPersistenceManager;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageFile;
import org.eclipse.rse.ui.actions.ISystemDynamicPopupMenuExtension;
import org.eclipse.rse.ui.actions.SystemDynamicPopupMenuExtensionManager;
import org.eclipse.rse.ui.actions.SystemShowPreferencesPageAction;
import org.eclipse.rse.ui.internal.RSESystemTypeAdapterFactory;
import org.eclipse.rse.ui.internal.RSEUIRegistry;
import org.eclipse.rse.ui.propertypages.RemoteSystemsPreferencePage;
import org.eclipse.rse.ui.propertypages.SystemCommunicationsPreferencePage;
import org.eclipse.rse.ui.view.SubsystemFactoryAdapterFactory;
import org.eclipse.rse.ui.view.SystemViewAdapterFactory;
import org.eclipse.rse.ui.view.team.SystemTeamViewResourceAdapterFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;


/**
 * Plugin for the core remote systems support.
 */
public class RSEUIPlugin extends SystemBasePlugin 
{
	public static final String PLUGIN_ID  = "org.eclipse.rse.ui"; 	
	public static final String HELPPREFIX = "org.eclipse.rse.ui.";
	 
    public static final boolean INCLUDE_LOCAL_YES = true;
    public static final boolean INCLUDE_LOCAL_NO = false;
	private static RSEUIPlugin         inst = null;
	
	private static SystemMessageFile 	messageFile = null;    
    private static SystemMessageFile    defaultMessageFile = null;    
    
//    private SystemType[]	            allSystemTypes = null;
    private String                      enabledSystemTypes;
    private ISystemRegistry              _systemRegistry = null;
    
    
    private IRSEPersistenceManager         _persistenceManager = null;
 
	private ISubSystemConfigurationProxy[]    subsystemFactories = null;
 

    private static String machineName = null;		
    private static String machineIP = null;
    private Vector viewSuppliers = new Vector();
    private SystemViewAdapterFactory svaf; // for fastpath access
    private SystemTeamViewResourceAdapterFactory svraf; // for fastpath
	private SystemShowPreferencesPageAction[] showPrefPageActions = null;
	private boolean dontShowLocalConnection, dontShowProfilePageInitially;
	    		
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
	 * Initializes default preferences.
	 */
	public void initializeDefaultPreferences() {
		
		String productId = null;
		
		// get product
		IProduct product = Platform.getProduct();
		
		// get product ID if there is a product
		if (product != null) {
			productId = product.getId();
		}
		else {
			// DKM - why would we return just because there's no product id?
			// letting this continue
			//return;
		}
	    
		// Get reference to the plug-in registry
	    IExtensionRegistry registry = Platform.getExtensionRegistry();
	    
	    // Get configured extenders
	    IConfigurationElement[] extensions = registry.getConfigurationElementsFor("org.eclipse.rse.ui", "rseConfigDefaults");

	    boolean showNewConnPromptPref = ISystemPreferencesConstants.DEFAULT_SHOWNEWCONNECTIONPROMPT;
	    dontShowLocalConnection = false;
	    dontShowProfilePageInitially = false;
	    
	    if ( (extensions != null) && (extensions.length > 0))
	    {
	    	String showNewConn;
	    	String showProfileInitially;
	    	
	    	for (int idx = 0; idx < extensions.length; idx++)
	    	{
	    		IConfigurationElement extension = extensions[idx];
	    		
	    		String configProductId = extension.getAttribute("productId");
	    		
	    		if (productId.equals(configProductId)) {
	    		
	    			showNewConn = extension.getAttribute("showNewConnectionPrompt");
	    			
	    			if (showNewConn != null)
	    				showNewConnPromptPref = showNewConn.equals("true");
	    			
	    			showNewConn = extension.getAttribute("showLocalConnection");
	    			
	    			if (showNewConn != null)
	    				dontShowLocalConnection = showNewConn.equals("false");
	    			
	    			enabledSystemTypes = extension.getAttribute("enableSystemTypes");
	    			
	    			if ((enabledSystemTypes != null) && (enabledSystemTypes.length() == 0))
	    				enabledSystemTypes = null;
	    			
	    			showProfileInitially = extension.getAttribute("showProfilePage");
	    			
	    			if (showProfileInitially != null)
	    				dontShowProfilePageInitially = showProfileInitially.equals("false");
	    		}
	    	}
	    }
	    
		RemoteSystemsPreferencePage.initDefaults(getPreferenceStore(), showNewConnPromptPref);
		SystemCommunicationsPreferencePage.initDefaults(getPreferenceStore());
	}
	
	/**
	 * Returns whether to show profile page initially, i.e. during the first new connection creation.
	 * @return <code>true</code> to show profile page initially, <code>false</code> otherwise.
	 */
	public boolean getShowProfilePageInitially() {
		return !dontShowProfilePageInitially;
	}
	
    /**
 	 *	Initialize the image registry by declaring all of the required
	 *	graphics.
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
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_USERACTION_NEW_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_USERACTION_NEW);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_USERACTION_USR_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_USERACTION_USR);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_USERACTION_IBM_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_USERACTION_IBM);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_USERACTION_IBMUSR_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_USERACTION_IBMUSR);

	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_USERTYPE_NEW_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_USERTYPE_NEW);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_USERTYPE_USR_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_USERTYPE_USR);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_USERTYPE_IBM_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_USERTYPE_IBM);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_USERTYPE_IBMUSR_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_USERTYPE_IBMUSR);

		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_COMPILE_NEW_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_COMPILE_NEW);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_COMPILE_USR_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_COMPILE_USR);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_COMPILE_IBM_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_COMPILE_IBM);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_COMPILE_IBMUSR_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_COMPILE_IBMUSR);
						   
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
						
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_ENVVAR_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_ENVVAR);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_ENVVAR_LIBPATH_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_ENVVAR_LIBPATH);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_ENVVAR_PATH_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_ENVVAR_PATH);
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
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_COMPILE_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_COMPILE);
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
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_WORKWITHUSERACTIONS_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_WORKWITHUSERACTIONS);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_WORKWITHNAMEDTYPES_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_WORKWITHNAMEDTYPES);
	    putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_WORKWITHCOMPILECMDS_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_WORKWITHCOMPILECMDS);

	    // putImageInRegistry(ISystemConstants.ICON_SYSTEM_REFRESH_ID,
		//				   path+ISystemConstants.ICON_SYSTEM_REFRESH); D54577
		
        putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_NEWFILE_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_NEWFILE);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_NEWFOLDER_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_NEWFOLDER);
	    //putImageInRegistry(ISystemConstants.ICON_SYSTEM_COLLAPSEALL_ID,
		//				   path+ISystemConstants.ICON_SYSTEM_COLLAPSEALL); // defect 41203 D54577
		
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_EXTRACT_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_EXTRACT); 
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_EXTRACTTO_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_EXTRACTTO);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_COMBINE_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_COMBINE);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_CONVERT_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_CONVERT); 
 
 

				


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
						
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_SHELL_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_SHELL);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_SHELLLIVE_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_SHELLLIVE);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_REMOVE_SHELL_ID,
						   path+ISystemIconConstants.ICON_SYSTEM_REMOVE_SHELL);
		
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_EXPORT_SHELL_OUTPUT_ID,
				   			path+ISystemIconConstants.ICON_SYSTEM_EXPORT_SHELL_OUTPUT);
		putImageInRegistry(ISystemIconConstants.ICON_SYSTEM_EXPORT_SHELL_HISTORY_ID,
				   			path+ISystemIconConstants.ICON_SYSTEM_EXPORT_SHELL_HISTORY);
		
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
        
        // call super first
        super.start(context);
        
	   	messageFile = getMessageFile("systemmessages.xml");
	   	defaultMessageFile = getDefaultMessageFile("systemmessages.xml");
        
    	SystemResourceManager.getRemoteSystemsProject(); // create core folder tree    	
    	try
    	{
    		SystemStartHere.getSystemProfileManager(); // create folders per profile
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}

	    IAdapterManager manager = Platform.getAdapterManager();

	    // DKM
	    // for subsystem factories
	    SubsystemFactoryAdapterFactory ssfaf = new SubsystemFactoryAdapterFactory();
	    ssfaf.registerWithManager(manager);
	    
	    RSESystemTypeAdapterFactory rseSysTypeFactory = new RSESystemTypeAdapterFactory();
	    manager.registerAdapters(rseSysTypeFactory, IRSESystemType.class);
	    
	    svaf = new SystemViewAdapterFactory();
	    svaf.registerWithManager(manager);

	    svraf = new SystemTeamViewResourceAdapterFactory();
	    svraf.registerWithManager(manager);

	
//		getInstallLocation();
	
	    //org.eclipse.rse.core.ui.uda.UserDefinedActionAdapterFactory udaaf = new org.eclipse.rse.core.ui.uda.UserDefinedActionAdapterFactory();	
	    //udaaf.registerWithManager(manager);	
	
		// DKM - 49648 - need to make sure that this is first called on the main thread so
		// we don't hit an SWT invalid thread exception later when getting the shell
		ISystemRegistry registry = getSystemRegistry();
	
	   

	    // add workspace listener for our project
	    SystemResourceManager.startResourceEventListening();
	
	    // DKM - moved to files.ui plugin
	    // refresh the remote edit project at plugin startup, to ensure
	    // it's never closed
		// SystemRemoteEditManager.getDefault().refreshRemoteEditProject();
		
		// Auto-start RSE communications daemon if required
		SystemCommunicationsDaemon daemon = SystemCommunicationsDaemon.getInstance();
		
		if (SystemCommunicationsDaemon.isAutoStart()) {
			daemon.startDaemon(false);
		}
		
		registerArchiveHandlers();
		registerDynamicPopupMenuExtensions();
		registerKeystoreProviders();

		 // if first time creating the remote systems project, add some default connections...
	    if (SystemResourceManager.isFirstTime() 
	    		&& !dontShowLocalConnection) // new support to allow products to not pre-create a local connection
	    {	    
	      //try
	      //{
				registry.createLocalHost(null, SystemResources.TERM_LOCAL, getLocalMachineName()); // profile, name, userId
				/* replaced with re-usable method by Phil, in v5.1.2	      		
	      	SystemConnection localConn = registry.createConnection(
	      	    //SystemResourceConstants.RESOURCE_TEAMPROFILE_NAME, IRSESystemType.SYSTEMTYPE_LOCAL,
	      	    SystemResourceConstants.RESOURCE_PRIVATEPROFILE_NAME, IRSESystemType.SYSTEMTYPE_LOCAL,
	      	    getString(ISystemConstants.TERM_LOCAL, "Local"), // connection name
	      	    "localhost", // hostname
	      	    "", // description
	      	    // DY:  defect 42101, description cannot be null
	      	    // null, // description
	      	    getLocalMachineName(), // userId
	      	    ISystemUserIdConstants.USERID_LOCATION_DEFAULT_SYSTEMTYPE, null);
	      	    */
	      //}
	      //catch (Exception exc)
	      //{
	      	//logError("Error creating default Local connection", exc);
	      //}
	    }
    }

    /**
     * For pathpath access to our adapters for non-local objects in our model. Exploits the knowledge we use singleton adapters.
     */
    public SystemViewAdapterFactory getSystemViewAdapterFactory()
    {
    	return svaf;
    }
  
	 
    /**
     * Restart the whole thing after a team synchronization
     */
    public void restart()
    {
        if (_systemRegistry != null)
        {
    	  // disconnect all active connections
    	  disconnectAll(false); // don't save ?
    	  // collapse and flush all nodes in all views
    	  _systemRegistry.fireEvent(new SystemResourceChangeEvent("dummy", ISystemResourceChangeEvents.EVENT_COLLAPSE_ALL, null));

          // allow child classes to override
          closeViews();
              	
          // clear in-memory settings for all filter pools and subsystems
    	  ISubSystemConfigurationProxy[] proxies = getSystemRegistry().getSubSystemConfigurationProxies();
    	  if (proxies != null)
    	  	for (int idx=0; idx < proxies.length; idx++)
    	  	   proxies[idx].reset();
          // clear in-memory settings for all profiles
    	  SystemProfileManager.clearDefault();

          // rebuild profiles
    	  SystemStartHere.getSystemProfileManager(); // create folders per profile
          // clear in-memory settings for all connections, then restore from disk
          ((SystemRegistry)_systemRegistry).reset();
          // restore in-memory settings for all filter pools and subsystems
    	  if (proxies != null)
    	  	for (int idx=0; idx < proxies.length; idx++)
    	  	   proxies[idx].restore();
    	  	
    	  // refresh GUIs
    	  _systemRegistry.fireEvent(new SystemResourceChangeEvent(_systemRegistry, ISystemResourceChangeEvents.EVENT_REFRESH, null));    	

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
     * Return the project used to hold all the Remote System Framework files
     */
    public IProject getRemoteSystemsProject()
    {
    	return SystemResourceManager.getRemoteSystemsProject();
    }
    
    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        

		
    	// disconnect all active connections
    	disconnectAll(true);
    	
	    // remove workspace listener for our project
	    SystemResourceManager.endResourceEventListening();
	
		// Stop RSE communications daemon if required, the stopDaemon method checks if the daemon is running or not
		SystemCommunicationsDaemon daemon = SystemCommunicationsDaemon.getInstance();
		daemon.stopDaemon();
    	
		
		
        // call this last
        super.stop(context);
    }

    /**
     * Disconnect all subsystems
     */
    protected void disconnectAll(boolean doSave)
    {
    	if (isSystemRegistryActive())
    	{
    	  ISubSystemConfigurationProxy[] proxies = getSystemRegistry().getSubSystemConfigurationProxies();
    	  if (proxies != null)
    	  {
    	  	for (int idx=0; idx < proxies.length; idx++)
    	  	{
    	  	   //System.out.println("In shutdown. proxy " + proxies[idx].getId() + " active? " + proxies[idx].isSubSystemFactoryActive());
    	  	   if (proxies[idx].isSubSystemConfigurationActive())
    	  	   {
    	  	   	 ISubSystemConfiguration ssf = proxies[idx].getSubSystemConfiguration();
    	  	   	 try
    	  	   	 {
    	  	        ssf.disconnectAllSubSystems();
    	  	   	 } catch(Exception exc)
    	  	   	 {
    	  	   	 	logError("Error disconnecting for "+ssf.getId(),exc);
    	  	   	 }
    	  	   	 if (doSave)
    	  	   	 {
    	  	   	   try
    	  	   	   {
    	  	         // TODO on shutdown classloader might not be able to do this properly
    	  	   		 // ssf.commit();
    	  	   	   } catch(Exception exc)
    	  	   	   {
    	  	   	 	  logError("Error saving subsystems for "+ssf.getId(),exc);
    	  	   	   }
    	  	   	 }
    	  	   }
    	  	}
    	  }
    	}
    }
    
    
    /**
     * Reset the system types objects to the given array. Called by preferences
     *   page when Defaults is pressed, and then OK/Apply.
     */
/*    public void setSystemTypes(SystemType[] systemTypes)
    {
    	allSystemTypes = systemTypes;
    }*/
    
    /**
     * Return all system type objects. Unlike {@link #getSystemTypes()}, this method
     *  returns all registered system types, not just those currently enabled.
     * @param refresh - true if to force a re-read from disk. If specified, then the read 
     *   is not effective, but rather is only returned. To affectively revert back, you
     *   must call setSystemTypes. This is used by the preferences page.
     * @return array of SystemType objects, one for every systemtype extension
     */
 /*   public SystemType[] getAllSystemTypes(boolean refresh)
    {

    	SystemType[] tempAllSystemTypes = null;
    	if (refresh || (allSystemTypes == null))
    	{
    		IConfigurationElement[] typePlugins = getSystemTypePlugins();  	    	  
    		
    		if (typePlugins != null)
    		{
    			Vector v = new Vector(); 
    			List typeStrs = new ArrayList();
    			ISubSystemConfigurationProxy[] proxies = getSubSystemConfigurationProxies();
    			if (proxies == null)
    				proxies = new ISubSystemConfigurationProxy[0];
				
    			for (int idx=0; idx<typePlugins.length; idx++)
    			{
    				String name = getSystemTypeName(typePlugins[idx]);
               
    				// PSC - added this to allow all system types to be declared "up front"
    				//       and then just used later by subsystems. The system types do not
    				//       appear until there is a subsystem factory that is registered for
    				//       this type.
    				boolean usedBySubSystemFactory = false;
    				for (int proxyIdx=0; !usedBySubSystemFactory && (proxyIdx<proxies.length); proxyIdx++)
    					if (proxies[proxyIdx].appliesToSystemType(name))
    						usedBySubSystemFactory = true;
          
    				//DKM v doesn't contain names - it contains SystemTypes
    				// changing this to use typeStrs - an array of names
    				boolean alreadyDeclared = typeStrs.contains(name);
 
    				if (usedBySubSystemFactory && !alreadyDeclared)
    				{
    					String icon = getSystemTypeIcon(typePlugins[idx]);
    					String iconLive = getSystemTypeLiveIcon(typePlugins[idx]);
    					
    					// first get the namespace of the extension (i.e. the id of the declaring plugin)
    					String nameSpace = typePlugins[idx].getDeclaringExtension().getNamespace();
    					
    					// now get bundle which has id equal to the namespace
    					Bundle bundle = Platform.getBundle(nameSpace);
    					
    					// get image
    					ImageDescriptor image = getPluginImage(bundle,icon);
    					
    					// get image to use when connected
    					ImageDescriptor connectedImage = getPluginImage(bundle,iconLive);
    					
    					String desc = typePlugins[idx].getAttribute("description");
    					boolean enableOffline = getSystemTypeEnableOffline(typePlugins[idx]);
    					SystemType st = new SystemType(name, image,connectedImage, enableOffline, desc);
    					v.addElement(st);
    					typeStrs.add(name);
    				}

    				// TODO: need to figure out a way to give preference to a particular declaration
    				// of a system type (among more than one such declaration). One idea is to tie
    				// this to a product branding, i.e. product ID.
    				else if (usedBySubSystemFactory && alreadyDeclared)
    				{
    					// KM: if a system type is redeclared, how do we know which one
    					// to give preference to? Currently, we don't, so for now
    					// we enable offline param if a declaration wants to enable offline,
    					// i.e. we give preference to positive value for enable offline.
    					boolean enableOffline = getSystemTypeEnableOffline(typePlugins[idx]);
    					
    					if (enableOffline) {
    						SystemType st = null;
    						for (int jdx=0; (st==null) && (jdx<v.size()); jdx++)
    						{
    							st = (SystemType)v.elementAt(jdx);
    							if (!st.equals(name))
    								st = null;
    						}
    						if (st!=null)
    						{
    							st.setEnableOffline(enableOffline);
    						}
    					}
    				}
    			}    	 
    			if (v.size() != 0)
    			{
    				tempAllSystemTypes = new SystemType[v.size()];
    				for (int idx=0; idx<v.size(); idx++)
    				{
    					tempAllSystemTypes[idx] = (SystemType)v.elementAt(idx);
    					if (enabledSystemTypes!=null)
    						tempAllSystemTypes[idx].setEnabled(false);
    				}
    				if (enabledSystemTypes!=null)
    				{
    					StringTokenizer tokens = new StringTokenizer(enabledSystemTypes,";");
    					while (tokens.hasMoreTokens())
    					{
    						String name = tokens.nextToken();
    						SystemType type = SystemType.getSystemType(tempAllSystemTypes,name);
    						if (type != null)
    							type.setEnabled(true);
    					}    					
    				}
    			}

    			if (!refresh) // if not preference page processing load-defaults, update global var
    			{
    				allSystemTypes = tempAllSystemTypes;
    				// now we need to update what we read from disk, with what are in preferences..
    				SystemTypeFieldEditor.initSystemTypePreferences(getPreferenceStore(), allSystemTypes);
    			}
    		}
    		
    		return tempAllSystemTypes;
    	}
    	
    	return allSystemTypes;
    }*/
    
    /**
     * Return the system type that matches the name.  Don't worry about enabled state.
     * @param name the system type name
     */
/*    public SystemType getSystemType(String name)
    {
    	if (allSystemTypes == null)
    		getAllSystemTypes(false);

    	for (int idx=0; idx<allSystemTypes.length; idx++)
    	{
    		if (name.equals(allSystemTypes[idx].getName()))
    		{
    			return allSystemTypes[idx];
    		} 	
    	}    
    	return null;
    }*/
    
    /**
     * Return an array of SystemType objects. These are name and image pairs registered
     *  via plugin.xml extension points.
     */
/*    public SystemType[] getSystemTypes()
    {    	
    	if (allSystemTypes == null)
    		getAllSystemTypes(false);
    	int enabledTypes=0;
    	for (int idx=0; idx<allSystemTypes.length; idx++)
    		if (allSystemTypes[idx].isEnabled())
    			enabledTypes++;    	
    	SystemType[] systemTypes = new SystemType[enabledTypes];
    	for (int idx=0, jdx=0; idx<allSystemTypes.length; idx++)
    		if (allSystemTypes[idx].isEnabled())
    			systemTypes[jdx++] = allSystemTypes[idx];
    	return systemTypes;
    }*/

    /**
     * Returns a qualified hostname given a potentially unqualified hostname
     */
    public static String getQualifiedHostName(String hostName)
	{
	 try
     {	       	    
		 InetAddress address = InetAddress.getByName(hostName);
		 return address.getCanonicalHostName();
     } 
     catch (java.net.UnknownHostException exc)
     {
       	return hostName;
     }
	}
    /**
     * A static version for convenience. 
     */
/*    public static SystemType[] getTheSystemTypes(boolean includeLocal)
    {
    	SystemType[] allTypes = getDefault().getSystemTypes();
    	if (!includeLocal)
    	{
    	  SystemType[] types = new SystemType[allTypes.length-1];
    	  int typeIdx = 0;
    	  for (int idx=0; idx<allTypes.length; idx++)
    	  {
    	  	 if (!allTypes[idx].getName().equals(IRSESystemType.SYSTEMTYPE_LOCAL))
    	  	   types[typeIdx++] = allTypes[idx];
    	  }
    	  return types;
    	}
    	return allTypes;
    }*/
	    
    /**
     * Convenience method to return an array of names-only for registered SystemType 
     *  extension point implementers.
     */
/*    public String[] getSystemTypeNames()
    {
    	return getSystemTypeNames(INCLUDE_LOCAL_NO);
    }*/
    
    /**
     * Convenience method to return an array of names-only for registered SystemType
     *  extension point implementers.
     * @param includeLocal true if returned names should include "local"
     */
/*    public String[] getSystemTypeNames(boolean includeLocal)
    {
    	IRSESystemType[] types = RSECorePlugin.getDefault().getRegistry().getSystemTypes();

    	String[] names = null;
    	if (types != null)
    	{
    		int len = types.length;
    		if (!includeLocal && (SystemType.getSystemType(types, "Local")!=null))
    			--len;
    		names = new String[len];
    		int nameIdx = 0;
    		for (int idx=0; idx<types.length; idx++)
    		{
    			String name = types[idx].getName();
    			if (includeLocal || !name.equals("Local"))
    				names[nameIdx++] = name;
    		}
    	}
    	return names;
    } */

    /**
     * Return the image for a given system type name.
     */
/*    public ImageDescriptor getSystemTypeImage(String name, boolean connected)
    {
    	SystemType match = getSystemType(name);
 
    	if (match != null)
    	{
    	  if (connected)
    	    return match.getConnectedImage();
    	  else
    	    return match.getImage();
    	}
    	else
    	  return null;
    }*/
        
    /**
     * Return whether the system type is enabled for offline support. 
     * 
     * @since RSE 6.0
     */
/*    public boolean getSystemTypeEnableOffline(String name)
    {
		SystemType[] types = getSystemTypes();
    	
		for (int idx=0; idx<types.length; idx++)
		{
			if (name.equals(types[idx].getName()))
			{
				return types[idx].isEnableOffline();
			}
		}

		// An unrecognized type was passed in
		logError("RSEUIPlugin.getSystemTypeEnableOffline:  invalid systemtype = " + name);
		return false;    	
    }*/
    
    /**
     *  Return all elements that extend the org.eclipse.rse.ui.systemtype extension point
     */
/*    private IConfigurationElement[] getSystemTypePlugins()
    {
   	    // Get reference to the plug-in registry
	    IExtensionRegistry registry = Platform.getExtensionRegistry();
	    // Get configured extenders
	    IConfigurationElement[] systemTypeExtensions =
		  registry.getConfigurationElementsFor("org.eclipse.rse.ui","systemtype");   	
	    return systemTypeExtensions;
    }*/
    /**
     *  Return the value of the "name=" attribute of the "type" tag in a given extension of the 
     *   org.eclipse.rse.ui.systemtype extension point
     */
/*    private String getSystemTypeName(IConfigurationElement element)
    {
        //logMessage("...in getSystemTypeName: "+element.getAttribute("name"));
   	    return element.getAttribute("name");
    }*/
	/**
     *  Return the value of the "icon=" attribute of the "type" tag in a given extension of the 
     *   org.eclipse.rse.ui.systemtype extension point
     */
/*    private String getSystemTypeIcon(IConfigurationElement element)
    {   	
   	    return element.getAttribute("icon");
    } */   
    /**
     * Return the value of the "enableoffline" attribute of the "type" tag in a given extension of
     * the org.eclipse.rse.ui.systemtype extension point
     * 
     * @since RSE 6.0
     */
/*    private boolean getSystemTypeEnableOffline(IConfigurationElement element)
    {
    	String enableOffline = element.getAttribute("enableoffline");
    	if (enableOffline != null)
    	{
    		return enableOffline.equals("true");
    	}
    	
    	return false;
    }*/
    
    /**
     *  Return the value of the "iconlive=" attribute of the "type" tag in a given extension of the
     *   org.eclipse.rse.ui.systemtype extension point
     */
/*    private String getSystemTypeLiveIcon(IConfigurationElement element)
    {
    	String attribute = element.getAttribute("iconlive");  	
   	    return attribute;
    }*/
    
    /**
     * Return an array of SubSystemFactoryProxy objects.
     * These represent all extensions to our subsystemconfiguration extension point.
     */
    public ISubSystemConfigurationProxy[] getSubSystemConfigurationProxies()
    {
    	if (subsystemFactories != null) // added by PSC
    		return subsystemFactories;

    	IConfigurationElement[] factoryPlugins = getSubSystemConfigurationPlugins();
    	if (factoryPlugins != null)
    	{
          Vector v = new Vector();
          for (int idx=0; idx<factoryPlugins.length; idx++)
          {
             SubSystemConfigurationProxy ssf =
               new SubSystemConfigurationProxy(factoryPlugins[idx]);           	
          	
             v.addElement(ssf);
          }    	  	
          if (v.size() != 0)
          {
            subsystemFactories = new ISubSystemConfigurationProxy[v.size()];
            for (int idx=0; idx<v.size(); idx++)
               subsystemFactories[idx] = (ISubSystemConfigurationProxy)v.elementAt(idx);
          }
    	}
    	return subsystemFactories;
    }

    /**
     *  Return all elements that extend the org.eclipse.rse.ui.subsystemconfiguration extension point
     */
    private IConfigurationElement[] getSubSystemConfigurationPlugins()
    {
   	    // Get reference to the plug-in registry
	    IExtensionRegistry registry = Platform.getExtensionRegistry();
	    // Get configured extenders
	    IConfigurationElement[] subsystemFactoryExtensions =
		  registry.getConfigurationElementsFor("org.eclipse.rse.ui","subsystemConfiguration");   	

	    return subsystemFactoryExtensions;
    }

    /**
     * Returns true if the SystemRegistry has been instantiated already.
     * Use this when you don't want to start the system registry as a side effect of retrieving it.
     */
    public boolean isSystemRegistryActive()
    {
    	return (_systemRegistry != null);
    }
    
    /**
     * Returns the persistence manager used for persisting RSE profiles
     * @return
     */
    public IRSEPersistenceManager getPersistenceManager()
    {
    	if (_persistenceManager == null)
    	{
    		_persistenceManager = new RSEPersistenceManager();
    	}
    	return _persistenceManager;
    }
    
  
    
    /**
     * Return the SystemRegistry singleton
     */
    public ISystemRegistry getSystemRegistry()
    {
    	if (_systemRegistry == null)
        {
    	  String logfilePath = getStateLocation().toOSString();    	

    	  _systemRegistry = SystemRegistry.getSystemRegistry(logfilePath);

          ISubSystemConfigurationProxy[] proxies = getSubSystemConfigurationProxies();
          if (proxies != null)
          {
            _systemRegistry.setSubSystemConfigurationProxies(proxies);
          }
        }
    	return _systemRegistry;
    }

    /**
     * A static version for convenience
	 * Returns the master registry singleton.
     */
    public static ISystemRegistry getTheSystemRegistry()
    {
    	return getDefault().getSystemRegistry();
    }
    
    public static IRSEPersistenceManager getThePersistenceManager()
    {
    	return getDefault().getPersistenceManager();
    }
    
  
    
	/**
	 * A static version for convenience
	 * Returns the master profile manager singleton.
	 */
	public static ISystemProfileManager getTheSystemProfileManager()
	{
		return SystemProfileManager.getSystemProfileManager();
	}

    /**
     * A static version for convenience
     */
    public static boolean isTheSystemRegistryActive()
    {
    	if (inst == null)
    	  return false;
    	else
    	  return getDefault().isSystemRegistryActive();
    }

  

    /**
     * Return an array of SystemPropertyPageExtension objects.
     * These represent all extensions to our propertyPage extension point.
     */
    public SystemPropertyPageExtension[] getPropertyPageExtensions()
    {
    	SystemPropertyPageExtension[] propertyPageExtensions = null;
    	IConfigurationElement[] propertyPagePlugins = getPropertyPagePlugins();
    	if (propertyPagePlugins != null)
    	{
          Vector v = new Vector();
          for (int idx=0; idx<propertyPagePlugins.length; idx++)
          {
             SystemPropertyPageExtension sppe =
               new SystemPropertyPageExtension(propertyPagePlugins[idx]);           	
             v.addElement(sppe);
          }    	  	
          
          propertyPageExtensions = new SystemPropertyPageExtension[v.size()];
          
          // prescan for first
          boolean foundFirst = false;
          
          for (int idx=0; !foundFirst && (idx<v.size()); idx++)
              if (((SystemPropertyPageExtension)v.elementAt(idx)).isAtTop())
              {
                  propertyPageExtensions[0] = (SystemPropertyPageExtension)v.elementAt(idx);
                  foundFirst = true;
              }
          
          int ppIdx = foundFirst ? 1: 0;
          
          for (int idx=0; idx<v.size(); idx++)
              if (!((SystemPropertyPageExtension)v.elementAt(idx)).isAtTop())
                  propertyPageExtensions[ppIdx++] = (SystemPropertyPageExtension)v.elementAt(idx);
    	}
    	
    	return propertyPageExtensions;
    }

    /**
     *  Return all elements that extend the org.eclipse.rse.ui.propertyPage extension point
     */
    private IConfigurationElement[] getPropertyPagePlugins()
    {
   	    // Get reference to the plug-in registry
	    IExtensionRegistry registry = Platform.getExtensionRegistry();
	    // Get configured extenders
	    IConfigurationElement[] propertyPageExtensions =
		  registry.getConfigurationElementsFor(PLUGIN_ID,"propertyPages");   	

	    return propertyPageExtensions;
    }
	/**
	 *  Return all elements that extend the org.eclipse.rse.ui.remoteSystemsViewPreferencesActions extension point
	 */
	private IConfigurationElement[] getPreferencePageActionPlugins()
	{
		// Get reference to the plug-in registry
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		// Get configured extenders
		IConfigurationElement[] prefPageExtensions =
		  registry.getConfigurationElementsFor(PLUGIN_ID,"remoteSystemsViewPreferencesActions");   	

		return prefPageExtensions;
	}
	
	/**
	 * Return an array of action objects registered via our org.eclipse.rse.ui.remoteSystemsViewPreferencesActions
	 *  extension point. <br>
	 * This may return null if there are no extenders.
	 */
	public SystemShowPreferencesPageAction[] getShowPreferencePageActions()
	{
		if (showPrefPageActions == null)
		{
			IConfigurationElement[] showPrefPagePlugins = getPreferencePageActionPlugins();
			if (showPrefPagePlugins != null)
			{
		  		Vector v = new Vector();
		  		for (int idx=0; idx<showPrefPagePlugins.length; idx++)
		  		{
					SystemShowPreferencesPageAction action = new SystemShowPreferencesPageAction();

					String prefPageId = showPrefPagePlugins[idx].getAttribute("preferencePageId");
					if ((prefPageId!=null)&&(prefPageId.length()>0))
					{
						action.setPreferencePageID(prefPageId);
					}
					String prefPageCategory = showPrefPagePlugins[idx].getAttribute("preferencePageCategory");
					if ((prefPageCategory!=null)&&(prefPageCategory.length()>0))
					{
						action.setPreferencePageCategory(prefPageCategory);
					}
					String iconFile = showPrefPagePlugins[idx].getAttribute("icon");
					
					if ((iconFile!=null)&&(iconFile.length()>0))
					{
					    // get namespace of extension (i.e. the id of the declaring plugin)
					    String nameSpace = showPrefPagePlugins[idx].getDeclaringExtension().getNamespace();
					    
					    // now get the associated bundle
					    Bundle bundle = Platform.getBundle(nameSpace);
					        
						ImageDescriptor id = getPluginImage(bundle, iconFile);
						
					    if (id != null) {
							action.setImageDescriptor(id);
					    }
					}
					String label = showPrefPagePlugins[idx].getAttribute("label");
					if ((label!=null)&&(label.length()>0))
					{
						action.setText(label);				
					}
					String tooltip = showPrefPagePlugins[idx].getAttribute("tooltip");
					if ((tooltip!=null)&&(tooltip.length()>0))
					{
						action.setToolTipText(tooltip);				
					}
					String heldId = showPrefPagePlugins[idx].getAttribute("helpContextId");
					if ((heldId!=null)&&(heldId.length()>0))
					{
						action.setHelp(heldId);				
					}
					v.addElement(action);
		  		} // end for all plugins loop
				showPrefPageActions = new SystemShowPreferencesPageAction[v.size()];
				for (int idx=0; idx<v.size(); idx++)
					showPrefPageActions[idx] = (SystemShowPreferencesPageAction)v.elementAt(idx);
			}
		}
		return showPrefPageActions;
	}
	
	/**
	 * @return The URL to the message file DTD. Null if it is not found.
	 */
	public URL getMessageFileDTD() {
		URL result = getBundle().getEntry("/messageFile.dtd");
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
	 * @param subsVars - an array of objects to substitute in for %1, %2, etc
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
     * Return the local user's machine name
     */
    public static String getLocalMachineName()
    {
    	if (machineName == null)
    	{
  	      try
	      { 
	         //InetAddress ip = InetAddress.getByName("localhost");
	         machineName = InetAddress.getLocalHost().getHostName();
	      }catch(Exception exc)
	      {
	      	machineName = ""; // ? what else ? no point in trying again.
	     	logWarning("Exception getting local hostname: " + exc.getClass().getName() + ": " + exc.getMessage());
	      }
    	}
    	return machineName;
    }
    /**
     * Return the local user's IP address
     */
    public static String getLocalMachineIPAddress()
    {
    	if (machineIP == null)
    	{
  	      try
	      {
	         //InetAddress ip = InetAddress.getByName("localhost");
	         machineIP = InetAddress.getLocalHost().getHostAddress();
	      }catch(Exception exc)
	      {
	      	machineIP = ""; // ? what else ? no point in trying again.
	     	logWarning("Exception getting local hostname: " + exc.getClass().getName() + ": " + exc.getMessage());
	      }
    	}
    	return machineIP;
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

	/**
	 * Initializes the Archive Handler Manager, by registering archive \
	 * file types with their handlers.
	 * @author mjberger
	 */
	protected void registerArchiveHandlers()
	{
		// Get reference to the plug-in registry
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		
		// Get configured extenders
		IConfigurationElement[] systemTypeExtensions = registry.getConfigurationElementsFor("org.eclipse.rse.ui", "archivehandlers");
		     	
		for (int i = 0; i < systemTypeExtensions.length; i++) {
			String ext = systemTypeExtensions[i].getAttribute("fileNameExtension");
			if (ext.startsWith(".")) ext = ext.substring(1);
			String handlerType = systemTypeExtensions[i].getAttribute("class");
			try
			{	
				// get the name space of the declaring extension
			    String nameSpace = systemTypeExtensions[i].getDeclaringExtension().getNamespace();
				
				// use the name space to get the bundle
			    Bundle bundle = Platform.getBundle(nameSpace);
			    
			    // if the bundle has not been uninstalled, then load the handler referred to in the
			    // extension, and load it using the bundle
			    // then register the handler
			    if (bundle.getState() != Bundle.UNINSTALLED) {
			        Class handler = bundle.loadClass(handlerType);
			        ArchiveHandlerManager.getInstance().setRegisteredHandler(ext, handler);
			    }
			}
			catch (ClassNotFoundException e)
			{
				logError("Cound not find archive handler class", e);
			}
		}
	}
	
	
	/**
	 * Initializes the System View Adapter Menu Extension Manager, by registering menu extensions
	 */
	protected void registerDynamicPopupMenuExtensions()
	{
		// Get reference to the plug-in registry
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		
		// Get configured extenders
		IConfigurationElement[] systemTypeExtensions = registry.getConfigurationElementsFor("org.eclipse.rse.ui", "dynamicPopupMenuExtensions");
		     	
		for (int i = 0; i < systemTypeExtensions.length; i++) 
		{
			try
			{
				// get the name space of the declaring extension
			    String nameSpace = systemTypeExtensions[i].getDeclaringExtension().getNamespace();
				
			    String menuExtensionType = systemTypeExtensions[i].getAttribute("class");
			    
				// use the name space to get the bundle
			    Bundle bundle = Platform.getBundle(nameSpace);
			    
			    // if the bundle has not been uninstalled, then load the handler referred to in the
			    // extension, and load it using the bundle
			    // then register the handler
			    if (bundle.getState() != Bundle.UNINSTALLED) 
			    {
			        Class menuExtension = bundle.loadClass(menuExtensionType);
					
			        ISystemDynamicPopupMenuExtension extension = (ISystemDynamicPopupMenuExtension)menuExtension.getConstructors()[0].newInstance(null);
			        SystemDynamicPopupMenuExtensionManager.getInstance().registerMenuExtension(extension);
			    }
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	protected void registerKeystoreProviders()
	{
		// Get reference to the plug-in registry
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		
		// Get configured extenders
		IConfigurationElement[] systemTypeExtensions = registry.getConfigurationElementsFor("org.eclipse.rse.ui", "keystoreProviders");
		     	
		for (int i = 0; i < systemTypeExtensions.length; i++) 
		{
			try
			{
				// get the name space of the declaring extension
			    String nameSpace = systemTypeExtensions[i].getDeclaringExtension().getNamespace();
				
			    String keystoreProviderType = systemTypeExtensions[i].getAttribute("class");
			    
				// use the name space to get the bundle
			    Bundle bundle = Platform.getBundle(nameSpace);
			    
				
			    if (bundle.getState() != Bundle.UNINSTALLED) 
			    {
			        Class keystoreProvider = bundle.loadClass(keystoreProviderType);
					
			        ISystemKeystoreProvider extension = (ISystemKeystoreProvider)keystoreProvider.getConstructors()[0].newInstance(null);
			        SystemKeystoreProviderManager.getInstance().registerKeystoreProvider(extension);
			    }
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns the RSE UI registry. Clients should use this method to get the registry which
	 * is the starting point for working with UI elements in the RSE framework.
	 * @return the RSE UI registry.
	 */
	public IRSEUIRegistry getRegistry() {
		return RSEUIRegistry.getDefault();
	}
}