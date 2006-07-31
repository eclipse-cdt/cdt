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

package org.eclipse.rse.core.internal.subsystems;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.core.subsystems.SubSystemConfiguration;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.osgi.framework.Bundle;


/**
 * Represents a registered subsystem factory extension.
 */
public class SubSystemConfigurationProxy implements ISubSystemConfigurationProxy
{
    private String name,description,id,types,vendor, category, systemClassName;
    private String[] systemTypes;
    private List typesArray;
    private boolean allTypes = false;
    private ImageDescriptor image, liveImage;
    private IConfigurationElement element = null;
    private ISubSystemConfiguration object = null;
    private boolean firstSubSystemQuery = true;
    //private SystemLogFile logFile = null;
    
       
    /**
	 * Constructor
	 * @param element The IConfigurationElement for this factory's plugin
	 */
	public SubSystemConfigurationProxy(IConfigurationElement element) {
		this.element = element;
		this.id = element.getAttribute("id");
		this.name = element.getAttribute("name").trim();
		this.description = element.getAttribute("description").trim();
		this.types = element.getAttribute("systemTypes");
		this.vendor = element.getAttribute("vendor");
		this.category = element.getAttribute("category");
		this.systemClassName = element.getAttribute("systemClass");
		String className = element.getAttribute("class");
		if (vendor == null) vendor = "Unknown";
		if (category == null) category = "Unknown";
		if (types == null) types = "*";
		this.allTypes = types.equals("*");
		this.image = getPluginImage(element, element.getAttribute("icon"));
		if (this.image == null) this.image = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CONNECTION_ID);
		this.liveImage = getPluginImage(element, element.getAttribute("iconlive"));
		if (this.liveImage == null) this.liveImage = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CONNECTIONLIVE_ID);
		//createFolderTree();
	}
	/**
	 * Return the value of the "vendor" attribute
	 */    
    public String getVendor()
    {
    	return vendor;
    }
	/**
	 * Return the value of the "name" attribute
	 */
    public String getName()
    {
    	return name;
    }
	/**
	 * Return the value of the "description" attribute
	 */
    public String getDescription()
    {
    	return description;
    }
	/**
	 * Return the value of the "id" attribute
	 */
    public String getId()
    {
    	return id;
    }    
	/**
	 * Return all defined system types
	 */
    public String[] getSystemTypes()
    {
    	if (systemTypes == null)
    	{
    	  if (allTypes)
            systemTypes = RSECorePlugin.getDefault().getRegistry().getSystemTypeNames();
          else
          {
          	StringTokenizer tokens = new StringTokenizer(types,";");
            Vector v = new Vector();
            while (tokens.hasMoreTokens())
              v.addElement(tokens.nextToken());
            systemTypes = new String[v.size()];
            for (int idx=0; idx<v.size(); idx++)
               systemTypes[idx] = (String)v.elementAt(idx);
          }
    	}
    	return systemTypes;
    }        
    /**
     * Return true if this factory supports all system types
     */
    public boolean supportsAllSystemTypes()
    {
    	return allTypes;
    }
    
	/**
	 * Return the value of the "category" attribute
	 */
    public String getCategory()
    {
    	return category;
    }        
    
    public ImageDescriptor getImage()
    {
    	return image;
    }
    
    /**
     * Return image to use when this susystem is connection. Comes from icon attribute in extension point xml
     */    
    public ImageDescriptor getLiveImage()
    {
    	if (liveImage != null)
    	  return liveImage;
        else
          return image;
    }
    /**
     * Return true if this extension's systemTypes attribute matches the given system type
     */        
    public boolean appliesToSystemType(String type)
    {
    	if (allTypes)
    	  return true;
    	else
    	{
    		List typesArray = getTypesArray();	
    		return typesArray.contains(type);
    	}
    }
    
    private List getTypesArray()
    {
    	if (typesArray == null)
    	{
    		typesArray = new ArrayList();
    		StringTokenizer tokenizer = new StringTokenizer(types,";");
			while (tokenizer.hasMoreTokens())
			{
				String type = tokenizer.nextToken();
				typesArray.add(type);
			}
    	}
    	return typesArray;
    }
    
    /**
     * Retrieve image in given plugin's directory tree, given its file name.
     * The file name should be relatively qualified with the subdir containing it.
     */
    protected ImageDescriptor getPluginImage(IConfigurationElement element, String fileName)
    {
	   URL path = getBundle().getEntry("/");
	   URL fullPathString = null;
	   try {
		   fullPathString = new URL(path,fileName);
		   return ImageDescriptor.createFromURL(fullPathString);
	   } catch (MalformedURLException e) {}
       return null;
    }

    /**
     * Return true if this subsystem factory has been instantiated yet.
     * Use this when you want to avoid the side effect of starting the subsystem factory object.
     */    
    public boolean isSubSystemConfigurationActive()
    {
    	return (object != null);
    }

    /**
     * Return the subsystem factory's object, which is an instance of the class
     * specified in the class attribute of the extender's xml for the factory extension point.
     * The object is only instantiated once, and returned on each call to this.
     */
    public ISubSystemConfiguration getSubSystemConfiguration()
    {
    	if ( firstSubSystemQuery == true && object == null )
    	{
   	       try
   	       {
//   	    	 get the name space of the declaring extension
			    String nameSpace = element.getDeclaringExtension().getNamespace();
			    String extensionType = element.getAttribute("class");
			    
				// use the name space to get the bundle
			    Bundle bundle = Platform.getBundle(nameSpace);
   	    	
			    // if the bundle has not been uninstalled, then load the handler referred to in the
			    // extension, and load it using the bundle
			    // then register the handler
			    if (bundle.getState() != Bundle.UNINSTALLED) 
			    {
			        Class menuExtension = bundle.loadClass(extensionType);
					
			        object = (ISubSystemConfiguration)menuExtension.getConstructors()[0].newInstance(null);
			    }
   	    	   
   	   	     object.setSubSystemConfigurationProxy(this); // side effect: restores filter pools
   	   	     //System.out.println("*** STARTED SSFACTORY: " + id + " ***");
   	       } catch (Exception exc)
   	       {
   	    	   exc.printStackTrace();
   	         SystemBasePlugin.logError("Unable to start subsystem factory "+id,exc);
	         org.eclipse.swt.widgets.MessageBox mb = new org.eclipse.swt.widgets.MessageBox(SystemBasePlugin.getActiveWorkbenchShell());
	         mb.setText("Unexpected Error");
	         String errmsg = "Unable to start subsystem factory "+getName()+". See log file for details";
	         mb.setMessage(errmsg);	
	         mb.open();   	         
   	       }    		
   	       if (object != null)
   	       {
   	         try
   	         {
    	   	   if (object instanceof SubSystemConfiguration) // hoaky but works
    	   	   {
    	   	   	 SubSystemConfiguration ssFactory = (SubSystemConfiguration)object;
                 ssFactory.restoreAllFilterPoolManagersForAllProfiles();
    	   	   }
   	         } catch (Exception exc)
   	         {
   	           SystemBasePlugin.logError("Error restoring subsystem for factory "+getName(),exc);
   	         }    		
   	       }
   	       firstSubSystemQuery = false;
    	}
    	return object;
    }            
	/**
	 * Return an instance of the ISystem class identified by the "systemClass" attribute
	 * of this subsystemFactory extension point. Note each call to this method returns a
	 * new instance of the class, or null if no "systemClass" attribute was specified. 
	 */
	public IConnectorService getSystemObject()
	{
		if (systemClassName == null)
			return null;
		Object object = null;
		try
		{
			 object = (IConnectorService)element.createExecutableExtension("systemClass");
		} catch (Exception exc)
		{
			 SystemBasePlugin.logError("Unable to instantiate ISystem class "+ systemClassName + " for extension point " + id,exc);
			 org.eclipse.swt.widgets.MessageBox mb = new org.eclipse.swt.widgets.MessageBox(SystemBasePlugin.getActiveWorkbenchShell());
			 mb.setText("Unexpected Error");
			 String errmsg = "Unable to instantiate ISystem class " + systemClassName + " for extension point " + id +": " + exc.getClass().getName()+" - " + exc.getMessage();
			 mb.setMessage(errmsg);	
			 mb.open();   	         
		}    		
		return (IConnectorService)object;
	}            
    
	/**
	 * Reset for a full refresh from disk, such as after a team synch. 
	 */
	public void reset()
	{
		if (object != null)
		  object.reset();
	}    

	/**
	 * After a reset, restore from disk
	 */
	public void restore()
	{
		if (object != null)
   	         try
   	         {
    	   	   if (object instanceof SubSystemConfiguration) // hoaky but works
    	   	   {
    	   	   	 SubSystemConfiguration ssFactory = (SubSystemConfiguration)object;
                 ssFactory.restoreAllFilterPoolManagersForAllProfiles();
    	   	   }
   	         } catch (Exception exc)
   	         {
   	           SystemBasePlugin.logError("Error restoring subsystem for factory "+getName(),exc);
   	         }    				
	}

    // PRIVATE METHODS USED BY THIS CLASS AND THE FACTORY OBJECT CLASS IT WRAPPERS.
    
    protected IConfigurationElement getConfigurationElement()    
    {
    	return element;
    }
    
    protected Bundle getBundle()    
    {
    	String nameSpace = element.getDeclaringExtension().getNamespace();
    	return Platform.getBundle(nameSpace);
    }        
    
    // -----------------
    // COMMON METHODS...
    // -----------------
        
    public boolean equals(Object o)
    {
    	if (o instanceof String)
    	  return ((String)o).equals(id);
    	else if (o instanceof SubSystemConfigurationProxy)
    	  return ((SubSystemConfigurationProxy)o).getId().equals(id);
    	else
    	  return false;
    }
    
    public int hashCode()
    {
    	return id.hashCode();
    }    
    
    public String toString()
    {
    	return id+"."+name;
    }
        
}