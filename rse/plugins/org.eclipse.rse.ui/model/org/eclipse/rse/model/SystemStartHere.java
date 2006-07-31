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

package org.eclipse.rse.model;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.model.SystemProfileManager;
import org.eclipse.rse.ui.RSEUIPlugin;


/**
 * It all begins RIGHT HERE!
 */
public class SystemStartHere 
{
   /**
    * STEP 1. Get system registry singleton
    * <p>
    * SAME AS: <code>RSEUIPlugin.getTheSystemRegistry</code>
    */
   public static ISystemRegistry getSystemRegistry()
   {
   	   return RSEUIPlugin.getTheSystemRegistry();
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
    * @see org.eclipse.rse.model.ISystemRegistry#getHostsBySystemType(String)
    */
   public static IHost[] getConnectionsBySystemType(String systemType)
   {
   	   return getSystemRegistry().getHostsBySystemType(systemType);
   }

   /**
    * STEP 2c. Get all connections for your subsystem factory
    * <p>
    * SAME AS: <code>getSystemRegistry().getConnectionsBySubSystemConfiguration(factory)</code>
    * @param factory A subsystem factory object. 
    * @see org.eclipse.rse.model.ISystemRegistry#getHostsBySubSystemConfiguration(ISubSystemConfiguration)
    * @see #getConnectionsBySubSystemConfiguration(String)
    */
   public static IHost[] getConnectionsBySubSystemConfiguration(ISubSystemConfiguration factory)
   {
   	   return getSystemRegistry().getHostsBySubSystemConfiguration(factory);
   }
   /**
    * STEP 2d. Get all connections for your subsystem factory, identified by factory Id.
    * <p>
    * SAME AS: <code>getSystemRegistry().getConnectionsBySubSystemConfiguration(getSubSystemConfiguration(factoryId))</code>
    * @param factoryId The id of the subsystem factory as given in its plugin.xml id attribute for the subsystemconfiguration extension point
    * @see #getSubSystemConfiguration(String)
    * @see #getConnectionsBySubSystemConfiguration(ISubSystemConfiguration)
    */
   public static IHost[] getConnectionsBySubSystemConfiguration(String factoryId)
   {
   	   return getSystemRegistry().getHostsBySubSystemConfiguration(getSubSystemConfiguration(factoryId));
   }

   /**
    * STEP 3a. Get all subsystems for all connections for your subsystem factory, identified by factory Id.
    * <p>
    * SAME AS: <code>getSystemRegistry().getSubSystems(factoryId)</code>
    * @param factoryId The subsystem factory id as given in its plugin.xml id attribute for the subsystemconfiguration extension point
    * @see org.eclipse.rse.model.ISystemRegistry#getSubSystems(String)
    * @see org.eclipse.rse.core.subsystems.ISubSystemConfiguration#getId()
    */
   public static ISubSystem[] getSubSystems(String factoryId)
   {
   	   return getSystemRegistry().getSubSystems(factoryId);
   }
   /**
    * STEP 3b. Get all subsystems for the given connection for your subsystem factory, identified by factory Id.
    * <p>
    * SAME AS: <code>getSystemRegistry().getSubSystems(factoryId, connection)</code>
    * @param factoryId The subsystem factory id as given in its plugin.xml id attribute for the subsystemconfiguration extension point
    * @param connection The connection object you wish to get the subsystems for. Typically there is only one subsystem per object.
    * @see org.eclipse.rse.model.ISystemRegistry#getSubSystems(String, IHost)
    * @see org.eclipse.rse.core.subsystems.ISubSystemConfiguration#getId()
    */
   public static ISubSystem[] getSubSystems(String factoryId, IHost connection)
   {
   	   return getSystemRegistry().getSubSystems(factoryId, connection);
   }
   /**
    * STEP 3c. Same as {@link #getSubSystems(String,IHost)} by used when you know
    *  the subsystem factory only supports a single subsystem per connection.
    * @param factoryId The subsystem factory id as given in its plugin.xml id attribute for the subsystemconfiguration extension point
    * @param connection The connection object you wish to get the subsystems for. Typically there is only one subsystem per object.
    * @see #getSubSystems(String, IHost)
    * @see org.eclipse.rse.core.subsystems.ISubSystemConfiguration#getId()
    */
   public static ISubSystem getSubSystem(String factoryId, IHost connection)
   {
   	   ISubSystem[] subsystems = getSystemRegistry().getSubSystems(factoryId, connection);
   	   if ((subsystems == null) || (subsystems.length==0))
   	     return null;
   	   else
   	     return subsystems[0];
   }



    // ----------------------------
    // MISCELLANEOUS:
    // ----------------------------           
   /**
    * Miscallenous Helper. Return the subsystem factory object for the given subsystem factory Id
    * <p>
    * SAME AS: <code>getSystemRegistry().getSubSystemConfiguration(factoryId)</code>
    * @param factoryId The id of the subsystem factory as given in its plugin.xml id attribute for the subsystemconfiguration extension point
    */
   public static ISubSystemConfiguration getSubSystemConfiguration(String factoryId)
   {
   	   return getSystemRegistry().getSubSystemConfiguration(factoryId);
   }
    
   /**
	* Miscallenous Helper. Return singleton profile manager
    * SAME AS: <code>getSystemRegistry().getSystemProfileManager()</code>
	*/
   public static ISystemProfileManager getSystemProfileManager()
   {
	   return SystemProfileManager.getSystemProfileManager();
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