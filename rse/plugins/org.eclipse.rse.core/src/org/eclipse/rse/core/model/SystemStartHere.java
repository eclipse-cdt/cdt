/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 ********************************************************************************/

package org.eclipse.rse.core.model;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.core.model.SystemProfileManager;


/**
 * It all begins RIGHT HERE!
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
   	   return RSECorePlugin.getDefault().getSystemRegistry();
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
    * SAME AS: <code>getSystemRegistry().getConnectionsBySystemType(systemType)</code>
    * @param systemType One of the system types defined via system type extension point:
    * <ul>
    *  <li>"iSeries"
    *  <li>"Windows"
    *  <li>"z/OS"
    *  <li>"Unix"
    *  <li>"Linux"
    *  <li>"Local"
    * </ul>
    * @see org.eclipse.rse.core.IRSESystemType
    * @see org.eclipse.rse.core.model.ISystemRegistry#getHostsBySystemType(String)
    */
   public static IHost[] getConnectionsBySystemType(String systemType)
   {
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
    * @see org.eclipse.rse.core.model.ISystemRegistry#getSubSystems(String)
    * @see org.eclipse.rse.core.subsystems.ISubSystemConfiguration#getId()
    */
   public static ISubSystem[] getSubSystems(String subsystemConfigurationId)
   {
   	   return getSystemRegistry().getSubSystems(subsystemConfigurationId);
   }
   /**
    * STEP 3b. Get all subsystems for the given connection for your subsystem configuration, identified by subsystemConfigurationId.
    * <p>
    * SAME AS: <code>getSystemRegistry().getSubSystems(subsystemConfigurationId, connection)</code>
    * @param subsystemConfigurationId The subsystem configuration id as given in its plugin.xml id attribute for the subsystemConfigurations extension point
    * @param connection The connection object you wish to get the subsystems for. Typically there is only one subsystem per object.
    * @see org.eclipse.rse.core.model.ISystemRegistry#getSubSystems(String, IHost)
    * @see org.eclipse.rse.core.subsystems.ISubSystemConfiguration#getId()
    */
   public static ISubSystem[] getSubSystems(String subsystemConfigurationId, IHost connection)
   {
   	   return getSystemRegistry().getSubSystems(subsystemConfigurationId, connection);
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
   	   ISubSystem[] subsystems = getSystemRegistry().getSubSystems(subsystemConfigurationId, connection);
   	   if ((subsystems == null) || (subsystems.length==0))
   	     return null;
   	   else
   	     return subsystems[0];
   }



    // ----------------------------
    // MISCELLANEOUS:
    // ----------------------------           
   /**
    * Miscallenous Helper. Return the subsystem configuration object for the given subsystemConfigurationId.
    * <p>
    * SAME AS: <code>getSystemRegistry().getSubSystemConfiguration(subsystemConfigurationId)</code>
    * @param subsystemConfigurationId The id of the subsystem configuration as given in its plugin.xml id attribute for the subsystemConfigurations extension point
    */
   public static ISubSystemConfiguration getSubSystemConfiguration(String subsystemConfigurationId)
   {
   	   return getSystemRegistry().getSubSystemConfiguration(subsystemConfigurationId);
   }
    
   /**
	* Miscallenous Helper. Return singleton profile manager
    * SAME AS: <code>getSystemRegistry().getSystemProfileManager()</code>
	*/
   public static ISystemProfileManager getSystemProfileManager()
   {
	   return SystemProfileManager.getDefault();
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