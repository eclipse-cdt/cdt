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
 * David Dykstal (IBM) - 168870: move core function from UI to core
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [175680] Deprecate obsolete ISystemRegistry methods
 ********************************************************************************/

package org.eclipse.rse.core.subsystems;

import java.net.URL;

import org.eclipse.rse.core.IRSESystemType;
import org.osgi.framework.Bundle;

/**
 * Interface to SubSystemConfigurationExtension class
 * Internal use, not likely you will ever need to use it or access it directly.
 */
public interface ISubSystemConfigurationProxy {
	
	/**
	 * Return value of the <samp>id</samp> xml attribute.
	 * Return unique id of this configuration.
	 */
	public String getId();

	/**
	 * Return value of the <samp>name</samp> xml attribute.
	 * Return name of this factory. Matches value in name attribute in extension point xml
	 */
	public String getName();

	/**
	 * Return value of the <samp>description</samp> xml attribute.
	 * Return description of this factory. Matches value in description attribute in extension point xml
	 */
	public String getDescription();

	/**
	 * Returns the bundle which have declared the subsystem
	 * configuration associated with this proxy.
	 *  
	 * @return The declaring bundle.
	 */
	public Bundle getDeclaringBundle();
	
	/**
	 * Return value of the <samp>systemTypeIds</samp> xml attribute.
	 * Return the system type ids this subsystem configuration supports.
	 */
	public String getDeclaredSystemTypeIds();
	
	/**
	 * Returns the list of system types the subsystem configuration is supporting.
	 * The list is combined from the list of currently registered system types cleaned
	 * up by the ones not matching the declared system type ids.
	 *  
	 * @return The list of supported system types or an empty list.
	 */
	public IRSESystemType[] getSystemTypes();

	/**
	 * Return true if this factory supports all system types
	 */
	public boolean supportsAllSystemTypes();

	/**
	 * Return value of the <samp>vendor</samp> xml attribute.
	 * Return vendor of this configuration.
	 */
	public String getVendor();

	/**
	 * Return value of the <samp>category</samp> xml attribute.
	 * Return the category this subsystem configuration subscribes to.
	 * @see org.eclipse.rse.core.model.ISubSystemConfigurationCategories
	 */
	public String getCategory();

	/**
	 * Return true if the subsystem factory has been instantiated yet
	 */
	public boolean isSubSystemConfigurationActive();

	/**
	 * Returns the priority of the subsystem configuration.
	 */
	public int getPriority();

	/**
	 * Return the subsystem factory singleton instance. Will instantiate if not already.
	 */
	public ISubSystemConfiguration getSubSystemConfiguration();

	//	/**
	//	 * Return an instance of the IConnectorService class identified by the "systemClass" attribute
	//	 * of this subsystemConfigurations extension point. Note each call to this method returns a
	//	 * new instance of the class, or null if no "systemClass" attribute was specified. 
	//	 */
	//	public IConnectorService getSystemObject();

	/**
	 * Test if the given system type matches one or more of the
	 * <samp>systemTypes</samp> attribute of this extension.
	 */
	public boolean appliesToSystemType(IRSESystemType type);

	/**
	 * Reset for a full refresh from disk, such as after a team synch. 
	 */
	public void reset();

	/**
	 * After a reset, restore from disk
	 */
	public void restore();
	
	/**
	 * @return the URL of the image associated with this subsystem in its non-connected state.
	 */
	public URL getImageLocation();
	
	/**
	 * @return the URL of the image associated with this subsystem in its connected state.
	 */
	public URL getLiveImageLocation();

}