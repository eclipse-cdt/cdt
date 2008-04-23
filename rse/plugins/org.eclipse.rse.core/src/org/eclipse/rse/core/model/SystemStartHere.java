/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty() 
 * Martin Oberhuber (Wind River) - [175680] Deprecate obsolete ISystemRegistry methods
 * Martin Oberhuber (Wind River) - [cleanup] Avoid using SystemStartHere in production code
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 ********************************************************************************/

package org.eclipse.rse.core.model;
import org.eclipse.rse.core.IRSECoreRegistry;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;


/**
 * A utility class, composed of static methods, that can be used to begin RSE processing.
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class SystemStartHere 
{
   /**
    * STEP 1. Get system registry singleton
    * <p>
    * SAME AS: <code>RSECorePlugin.getSystemRegistry</code>
    */
   public static ISystemRegistry getSystemRegistry()
   {
   	   return RSECorePlugin.getTheSystemRegistry();
   }
   
   /**
    * STEP 2a. Get connections for all system types
    * <p>
    * SAME AS: <code>getSystemRegistry().getConnections()</code>
    * @see #getConnectionsBySystemType(String)
    */
   public static IHost[] getConnections()
   {
   	   return getSystemRegistry().getHosts();
   }

   /**
    * STEP 2b. Get all connections for the given system type.
    * <p>
    * SAME AS: <code>getSystemRegistry().getHostsBySystemType(systemType)</code>
    * @param systemTypeId One of the system types IDs defined via system type extension point:
    * <ul>
    *  <li>"org.eclipse.rse.systemtype.iseries" ({@link IRSESystemType#SYSTEMTYPE_ISERIES_ID})
    *  <li>"org.eclipse.rse.systemtype.windows" ({@link IRSESystemType#SYSTEMTYPE_WINDOWS_ID})
    *  <li>"org.eclipse.rse.systemtype.zseries" ({@link IRSESystemType#SYSTEMTYPE_ZSERIES_ID})
    *  <li>"org.eclipse.rse.systemtype.unix" ({@link IRSESystemType#SYSTEMTYPE_UNIX_ID})
    *  <li>"org.eclipse.rse.systemtype.linux" ({@link IRSESystemType#SYSTEMTYPE_LINUX_ID})
    *  <li>"org.eclipse.rse.systemtype.aix" ({@link IRSESystemType#SYSTEMTYPE_AIX_ID})
    *  <li>"org.eclipse.rse.systemtype.local" ({@link IRSESystemType#SYSTEMTYPE_LOCAL_ID})
    *  <li>"org.eclipse.rse.systemtype.ftp" ({@link IRSESystemType#SYSTEMTYPE_FTP_ONLY_ID})
    *  <li>"org.eclipse.rse.systemtype.ssh" ({@link IRSESystemType#SYSTEMTYPE_SSH_ONLY_ID})
    *  <li>"org.eclipse.rse.systemtype.telnet" ({@link IRSESystemType#SYSTEMTYPE_TELNET_ONLY_ID})
    * </ul>
    * @see org.eclipse.rse.core.IRSESystemType
    * @see IRSECoreRegistry#getSystemTypeById(String)
    * @see org.eclipse.rse.core.model.ISystemRegistry#getHostsBySystemType(IRSESystemType)
    * 
    */
   public static IHost[] getConnectionsBySystemType(String systemTypeId)
   {
	   IRSESystemType systemType = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(systemTypeId);
	   return getSystemRegistry().getHostsBySystemType(systemType);
   }

   /**
    * STEP 2c. Get all connections for your subsystem configuration
    * <p>
    * SAME AS: <code>getSystemRegistry().getConnectionsBySubSystemConfiguration(subsystemConfiguration)</code>
    * @param subsystemConfiguration A subsystem configuration object. 
    * @see org.eclipse.rse.core.model.ISystemRegistry#getHostsBySubSystemConfiguration(ISubSystemConfiguration)
    * @see #getConnectionsBySubSystemConfiguration(String)
    */
   public static IHost[] getConnectionsBySubSystemConfiguration(ISubSystemConfiguration subsystemConfiguration)
   {
   	   return getSystemRegistry().getHostsBySubSystemConfiguration(subsystemConfiguration);
   }
   /**
    * STEP 2d. Get all connections for your subsystem configuration, identified by subsystemConfigurationId.
    * <p>
    * SAME AS: <code>getSystemRegistry().getConnectionsBySubSystemConfiguration(getSubSystemConfiguration(subsystemConfigurationId))</code>
    * @param subsystemConfigurationId The id of the subsystem configuration as given in its plugin.xml id attribute for the subsystemConfigurations extension point
    * @see #getSubSystemConfiguration(String)
    * @see #getConnectionsBySubSystemConfiguration(ISubSystemConfiguration)
    */
   public static IHost[] getConnectionsBySubSystemConfiguration(String subsystemConfigurationId)
   {
   	   return getSystemRegistry().getHostsBySubSystemConfiguration(getSubSystemConfiguration(subsystemConfigurationId));
   }

   /**
    * STEP 3a. Get all subsystems for all connections for your subsystem configuration, identified by subsystemConfigurationId.
    * <p>
    * SAME AS: <code>getSystemRegistry().getSubSystems(subsystemConfigurationId)</code>
    * @param subsystemConfigurationId The subsystem configuration id as given in its plugin.xml id attribute for the subsystemConfigurations extension point
    * @see ISystemRegistry#getSubSystemConfiguration(String)
    * @see ISubSystemConfiguration#getSubSystems(boolean)
    * @see ISubSystemConfiguration#getId()
    */
   public static ISubSystem[] getSubSystems(String subsystemConfigurationId)
   {
	   ISystemRegistry sr = getSystemRegistry();
	   ISubSystemConfiguration config = sr.getSubSystemConfiguration(subsystemConfigurationId);
	   if (config == null)
			return (new ISubSystem[0]);
   	   return config.getSubSystems(true);
   }
   /**
    * STEP 3b. Get all subsystems for the given connection for your subsystem configuration, identified by subsystemConfigurationId.
    * <p>
    * SAME AS: <code>getSystemRegistry().getSubSystems(subsystemConfigurationId, connection)</code>
    * @param subsystemConfigurationId The subsystem configuration id as given in its plugin.xml id attribute for the subsystemConfigurations extension point
    * @param connection The connection object you wish to get the subsystems for. Typically there is only one subsystem per object.
    * @see ISystemRegistry#getSubSystemConfiguration(String)
    * @see ISubSystemConfiguration#getSubSystems(IHost, boolean)
    */
   public static ISubSystem[] getSubSystems(String subsystemConfigurationId, IHost connection)
   {
	   ISystemRegistry sr = getSystemRegistry();
	   ISubSystemConfiguration config = sr.getSubSystemConfiguration(subsystemConfigurationId);
	   if (config == null)
			return (new ISubSystem[0]);
	   return config.getSubSystems(connection, true);
   }
   /**
    * STEP 3c. Same as {@link #getSubSystems(String,IHost)} by used when you know
    *  the subsystem configuration only supports a single subsystem per connection.
    * @param subsystemConfigurationId The subsystem configuration id as given in its plugin.xml id attribute for the subsystemConfigurations extension point
    * @param connection The connection object you wish to get the subsystems for. Typically there is only one subsystem per object.
    * @see #getSubSystems(String, IHost)
    * @see org.eclipse.rse.core.subsystems.ISubSystemConfiguration#getId()
    */
   public static ISubSystem getSubSystem(String subsystemConfigurationId, IHost connection)
   {
   	   ISubSystem[] subsystems = getSubSystems(subsystemConfigurationId, connection);
   	   if ((subsystems == null) || (subsystems.length==0))
   	     return null;
   	   else
   	     return subsystems[0];
   }



    // ----------------------------
    // MISCELLANEOUS:
    // ----------------------------           
   /**
    * Miscellaneous Helper - return the subsystem configuration object for the given subsystemConfigurationId.
    * <p>
    * SAME AS: <code>getSystemRegistry().getSubSystemConfiguration(subsystemConfigurationId)</code>
    * @param subsystemConfigurationId The id of the subsystem configuration as given in its plugin.xml id attribute for the subsystemConfigurations extension point
    */
   public static ISubSystemConfiguration getSubSystemConfiguration(String subsystemConfigurationId)
   {
   	   return getSystemRegistry().getSubSystemConfiguration(subsystemConfigurationId);
   }
    
   /**
	* Miscellaneous Helper - Return the singleton profile manager.
    * SAME AS: <code>getSystemRegistry().getSystemProfileManager()</code>
	*/
   public static ISystemProfileManager getSystemProfileManager()
   {
	   return RSECorePlugin.getTheSystemProfileManager();
   }

    /**
     * Return all active profiles.
     * <p>
     * A team might have many profiles, at least one per developer. 
     * However, typically only one or two are activated at a time, and
     *  we only return connections for those which are active.
    * <p>
    * SAME AS: <code>getSystemRegistry().getActiveSystemProfiles()</code>
     */
    public static ISystemProfile[] getActiveSystemProfiles()
    {
    	return getSystemRegistry().getActiveSystemProfiles();
    }
}