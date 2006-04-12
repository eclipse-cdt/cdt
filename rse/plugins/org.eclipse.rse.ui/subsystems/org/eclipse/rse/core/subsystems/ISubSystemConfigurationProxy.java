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

package org.eclipse.rse.core.subsystems;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Interface to SubSystemFactoryExtension class
 * Internal use, not likely you will ever need to use it or access it directly.
 */
public interface ISubSystemConfigurationProxy 
{
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
     * Return value of the <samp>id</samp> xml attribute.
     * Return unique id of this configuration.
     */
    public String getId();
    /**
     * Return value of the <samp>systemTypes</samp> xml attribute.
     * Return the system types this subsystem configuration supports.
     */
    public String[] getSystemTypes();
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
     * @see org.eclipse.rse.model.ISubSystemFactoryCategories
     */
    public String getCategory();
    /**
     * Return value of the <samp>icon</samp> xml attribute.
     * Return actual graphics image used for subsystems when there is no live connection.
     */
    public ImageDescriptor getImage();
    /**
     * Return value of the <samp>iconlive</samp> xml attribute.
     * Return actual graphics image used for subsystems when there is a live connection.
     */
    public ImageDescriptor getLiveImage();  
    
    /**
     * Return true if the subsystem factory has been instantiated yet
     */
    public boolean isSubSystemConfigurationActive();
    /**
     * Return the subsystem factory singleton instance. Will instantiate if not already.
     */
    public ISubSystemConfiguration getSubSystemConfiguration();
	/**
	 * Return an instance of the ISystem class identified by the "systemClass" attribute
	 * of this subsystemFactory extension point. Note each call to this method returns a
	 * new instance of the class, or null if no "systemClass" attribute was specified. 
	 */
	public IConnectorService getSystemObject();
	
    /**
     * Test if the given system type matches one or more of the type names declared in the
     *  <samp>systemTypes</samp> attribute of this extension.
     */
    public boolean appliesToSystemType(String type);    
    
	/**
	 * Reset for a full refresh from disk, such as after a team synch. 
	 */
	public void reset();

	/**
	 * After a reset, restore from disk
	 */
	public void restore();
}