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

package org.eclipse.rse.core;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.ui.propertypages.SystemTypeFieldEditor;


/**
 * Represents a registered system type, as registered by the systemtype extension point.
 * Currently, only two pieces of information per type is recorded:<br>
 * <ol>
 * 	<li>name. eg "Windows"
 * 	<li>image. eg "windows.gif"
 * </ol>
 */
public class SystemType 
{
	
    private String name, description;
    private ImageDescriptor image, connectedImage;
    private boolean enabled=true;
    private String userId;
    
    // yantzi: artemis 6.0 added offline attribute and extension point attribute to enable offline support
    private boolean enableOffline;
    
    /**
     * Constructor
     * @param name - display name for this system type
     * @param image - image for this system type, not connected
     * @param connectedImage - image for this system type, when connected
     * @param enableOffline - whether to enable offline
     * @param description - translatable description of the system type
     */
    public SystemType(String name, ImageDescriptor image, ImageDescriptor connectedImage, boolean enableOffline,
    		          String description)
    {
    	this.name = name;
    	this.description = description;
    	this.image = image;
    	this.connectedImage = connectedImage;
    	this.enableOffline = enableOffline;
    }
    
    /**
     * Convert to a string. Same as {@link #getName()}
     */
    public String toString()
    {
    	return name;
    }
    
    /**
     * Return the display name for this system type
     * @return the name shown in the New Connection wizard for this system type.
     */
    public String getName()
    {
    	return name;
    }
    
    /**
     * Return the translatable description of this system type
     * @return a description of this system type. Might be null.
     */
    public String getDescription()
    {
    	return description;
    }
    
    /**
     * Return true if this system type supports offline mode or not
     * @return true if this system type supports offline mode
     */
    public boolean isEnableOffline()
    {
    	return enableOffline;
    }
    
    /**
     * Configuration method.
     * Set whether this system type supports offline mode or not.
     */
    public void setEnableOffline(boolean enable)
    {
    	enableOffline = enable;
    }
    
    /**
     * Return true if this system type is currently enabled, meaning it shows
     *  up in the New Connection wizard. User can affect this via preferences page.
     * @return true if this type is enabled
     */
    public boolean isEnabled()
    {
    	return enabled;
    }
    
    /**
     * Specify if this system type is currently enabled, affecting if it shows up in
     *  the New Connection wizard. User can affect this via preferences page.  
     */
    public void setEnabled(boolean enabled)
    {
    	this.enabled = enabled;
    }
    
    /**
     * Return the image for connections of this system type, when not connected
     */
    public ImageDescriptor getImage()
    {
    	return image;
    }
    
    /**
     * Return the image for connections of this system type, when connected
     */
    public ImageDescriptor getConnectedImage()
    {
    	if (connectedImage == null)
    	  return image;
    	else
    	  return connectedImage;
    }
    
    /**
     * Return the default user ID preferences setting for this system type.
     */
    public String getDefaultUserID()
    {
    	return userId;
    }
    
    /**
     * Set the default user ID for this system type. This is a preferences value
     */
    public void setDefaultUserID(String id)
    {
    	this.userId = id;
    }
    
    /**
     * Compare this system type to another one. Compare by name.
     */
    public boolean equals(Object o)
    {
    	if (o instanceof String)
    	  return ((String)o).equals(name);
    	else if (o instanceof SystemType)
    	  return ((SystemType)o).getName().equals(name);
    	else
    	  return false;
    }
    
    /**
     * Hash by name
     */
    public int hashCode()
    {
    	return name.hashCode();
    }
    
    /**
     * Helper method... given a system type name, return its system type object
     */
    public static SystemType getSystemType(SystemType[] allTypes, String name)
    {
    	SystemType type = null;
    	for (int idx=0; (type==null)&&(idx<allTypes.length); idx++)
    		if (allTypes[idx].getName().equalsIgnoreCase(name))
    			type = allTypes[idx];
    	return type;
    }
    /**
     * Helper method to produce a preferences string to save the settable information to disk
     */
    public static String getPreferenceStoreString(SystemType type)
    {
		String userId = type.getDefaultUserID();
		if (userId == null)
			userId = "null";
		return Boolean.toString(type.isEnabled()) + SystemTypeFieldEditor.EACHVALUE_DELIMITER + userId;		    	
    }
}