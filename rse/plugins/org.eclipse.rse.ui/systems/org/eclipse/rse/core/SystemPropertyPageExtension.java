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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.ui.propertypages.SystemRemotePropertyPageNode;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.internal.dialogs.IPropertyPageContributor;
import org.eclipse.ui.internal.dialogs.PropertyPageManager;
import org.osgi.framework.Bundle;


/**
 * Represents a registered remote system property page. These are registered
 * via our propertyPages extension point.
 * <p>
 * This class encapsulates all the information supplied by the extension xml.
 * <ol>
 * <li>id. Unique identifier
 * <li>name. Displayable property page name
 * <li>class. The class which implements IWorkbenchPropertyPage
 * <li>subsystemconfigurationid. For scoping to remote objects for a given subsystem factory
 * <li>subsystemconfigurationCategory. For scoping to remote objects for a given subsystem factory category
 * <li>systemTypes. For scoping to remote objects from systems of a given type, or semicolon-separated types.
 * <li>namefilter. For scoping to remote objects of a given name
 * <li>typecategoryfilter. For scoping to remote objects for a given remote object type category
 * <li>typefilter. For scoping to remote objects of a given type
 * <li>subtypefilter. For scoping to remote objects of a given subtype
 * <li>subsubtypefilter. For scoping to remote objects of a given sub-subtype
 * </ol>
 * @see org.eclipse.rse.ui.view.ISystemRemoteElementAdapter
 */
public class SystemPropertyPageExtension implements IPropertyPageContributor
{
	
    private String name,id;
    private ImageDescriptor image = null;
    private SystemRemoteObjectMatcher matcher = null;
    private IConfigurationElement element = null;
    private IWorkbenchPropertyPage object = null;
    private boolean atTop = false;
	private HashMap filterProperties; 
	private static final String TAG_FILTER="filter";//$NON-NLS-1$
	
    /**
     * Constructor
     */
    public SystemPropertyPageExtension(IConfigurationElement element)
    {
    	this.element = element;
    	this.id = element.getAttribute("id");
    	this.name = element.getAttribute("name"); 
    	this.image = getPluginImage(element, element.getAttribute("icon"));
    	String sAtTop = element.getAttribute("first");
    	if ((sAtTop != null) && sAtTop.equals("1"))
    	  atTop = true;
        
        String subsystemfilter,subsystemCategoryFilter,systypes,categoryfilter,namefilter,typefilter,subtypefilter,subsubtypefilter;

    	categoryfilter = element.getAttribute("typecategoryfilter");
    	namefilter = element.getAttribute("namefilter");
    	typefilter = element.getAttribute("typefilter");
    	subtypefilter = element.getAttribute("subtypefilter");
    	subsubtypefilter = element.getAttribute("subsubtypefilter");
    	subsystemfilter = element.getAttribute("subsystemconfigurationid");    	
    	subsystemCategoryFilter = element.getAttribute("subsystemconfigurationCategory");    
 	    systypes = element.getAttribute("systemTypes");    
    	    	
	    filterProperties = null;
   		IConfigurationElement[] children = element.getChildren();
	    for (int i=0; i<children.length; i++) 
	    {
		   processChildElement(children[i]);
	    }
	
    	
    	matcher = new SystemRemoteObjectMatcher(subsystemfilter, subsystemCategoryFilter, categoryfilter, systypes, 
    	                                        namefilter, typefilter, subtypefilter, subsubtypefilter);
    }
    /**
     * Parses child element and processes it 
     */
    private void processChildElement(IConfigurationElement element) 
    {
    	String tag = element.getName();
    	if (tag.equals(TAG_FILTER)) 
    	{
    		String key = element.getAttribute("name");
    		String value = element.getAttribute("value");
    		if ((key == null) || (value == null))
    		   return;
    		if (filterProperties==null) 
    		   filterProperties = new HashMap();
    		filterProperties.put(key, value);
	    }
    }
    /**
     * Getter method.
     * Return what was specified for the <samp>name</samp> xml attribute.
     */    
    public String getName()
    {
    	return name;
    }    
    /**
     * Getter method.
     * Return what was specified for the <samp>id</samp> xml attribute.
     */
    public String getId()
    {
    	return id;
    }    
    /**
     * Getter method.
     * Return what was specified for the <samp>icon</samp> xml attribute.
     */
    public ImageDescriptor getImage()
    {
    	return image;
    }
    /**
     * Getter method.
     * Return what was last set via call to setAtTop(boolean)
     */
    public boolean isAtTop()
    {
    	return atTop;
    }
    /**
     * Set the at top attribute
     */
    public void setAtTop(boolean atTop)
    {
    	this.atTop = atTop;
    }
    /**
     * Getter method.
     * Return what was specified for the <samp>typecategoryfilter</samp> xml attribute.
     */    
    public String getCategoryFilter()
    {
    	return matcher.getCategoryFilter();
    }        
    /**
     * Getter method.
     * Return what was specified for the <samp>namefilter</samp> xml attribute.
     */
    public String getNameFilter()
    {
    	return matcher.getNameFilter();
    }    
    /**
     * Getter method.
     * Return what was specified for the <samp>typefilter</samp> xml attribute.
     */
    public String getTypeFilter()
    {
    	return matcher.getTypeFilter();
    }    
    /**
     * Getter method.
     * Return what was specified for the <samp>subtypefilter</samp> xml attribute.
     */
    public String getSubTypeFilter()
    {
    	return matcher.getSubTypeFilter();
    }    
    /**
     * Getter method.
     * Return what was specified for the <samp>subsubtypefilter</samp> xml attribute.
     */
    public String getSubSubTypeFilter()
    {
    	return matcher.getSubSubTypeFilter();
    }    
    /**
     * Getter method.
     * Return what was specified for the <samp>subsystemconfigurationid</samp> xml attribute.
     */
    public String getSubSystemFactoryId()
    {
    	return matcher.getSubSystemFactoryId();
    }    
    /**
     * Getter method.
     * Return what was specified for the <samp>subsystemconfigurationCategory</samp> xml attribute.
     */
    public String getSubSystemFactoryCategoryFilter()
    {
    	return matcher.getSubSystemFactoryCategoryFilter();
    }        
    /**
     * Getter method.
     * Return what was specified for the <samp>systemTypes</samp> xml attribute.
     */
    public String getSystemTypesFilter()
    {
    	return matcher.getSystemTypesFilter();
    }        

    /**
     * Retrieve image in given plugin's directory tree, given its file name.
     * The file name should be relatively qualified with the subdir containing it.
     */
    protected ImageDescriptor getPluginImage(IConfigurationElement element, String fileName)
    {
	   URL path = getBundle(element).getEntry("/");
	   URL fullPathString = null;
	   try {
		   fullPathString = new URL(path,fileName);
		   return ImageDescriptor.createFromURL(fullPathString);
	   } catch (MalformedURLException e) {}
       return null;
    }
    
    protected Bundle getBundle(IConfigurationElement element)    
    {
    	String nameSpace = element.getDeclaringExtension().getNamespace();
    	return Platform.getBundle(nameSpace);
    }        
    
    /**
     * Given an ISystemRemoteElement, return true if that element
     * should show this property page. Looks at the filter criteria.
     */
    public boolean appliesTo(ISystemRemoteElementAdapter adapter, Object element)
    {
    	boolean matches = matcher.appliesTo(adapter, element);
    	if (!matches)
    	  return false;
    	// Test custom filter	
    	if (filterProperties == null)
    		return true;
    	IActionFilter filter = null;
    	// If this is a resource contributor and the object is not a resource but
    	// is an adaptable then get the object's resource via the adaptable mechanism.    	
    	Object testObject = element;
    	/*
    	if (isResourceContributor 
    		&& !(object instanceof IResource)
    		&& (object instanceof IAdaptable)) 
    	{ 
			Object result = ((IAdaptable)object).getAdapter(IResource.class);
			if (result != null) 
				testObject = result;
	    }*/
	    if (testObject instanceof IActionFilter)
		  filter = (IActionFilter)testObject;
		else if (testObject instanceof IAdaptable)
		 filter = (IActionFilter)((IAdaptable)testObject).getAdapter(IActionFilter.class);
		if (filter != null)
			return testCustom(testObject, filter);
	    else
	    	return true;
	}
	/**
	 * Returns whether the object passes a custom key value filter
	 * implemented by a matcher.
	 */
	private boolean testCustom(Object object, IActionFilter filter) 
	{
		if (filterProperties == null)
			return false;
	    Iterator iter = filterProperties.keySet().iterator();
	    while (iter.hasNext()) 
	    {
		   String key = (String)iter.next();
		   String value = (String)filterProperties.get(key);
		   if (!filter.testAttribute(object, key, value))
 			   return false;
 	    }
	    return true;
    }

    
    /**
     * Instantiate and return the class that implements IWorkbenchPropertyPage
     */
    public IWorkbenchPropertyPage getPropertyPage()
    {
    	//if (object == null)
    	//{
   	       try
   	       {
   	   	     object = (IWorkbenchPropertyPage)element.createExecutableExtension("class");
   	       } catch (Exception exc)
   	       {
   	         SystemBasePlugin.logError("Unable to start remote property page extension "+id,exc);
   	       }
    	//}
    	return object;    		
    }
    	
    public String toString()
    {
    	return id;
    }    


	// -----------------------------------	
	// IPropertyPageContributor methods...
	// -----------------------------------
	
	/**
	 * Implement this method to add instances of PropertyPage class to the
	 * property page manager.
	 * @return true if pages were added, false if not.
	 */	
	public boolean contributePropertyPages(PropertyPageManager manager, Object object)
	{
		boolean added = false;
	    SystemRemotePropertyPageNode node = new SystemRemotePropertyPageNode(this, object);
	    manager.addToRoot(node);		
		return added;
	}   
	/**
	 * Returns true if this contributor should be considered
	 * for the given object.
	 */
	public boolean isApplicableTo(Object object)
	{
		return true;
	}
	/**
	 * Creates the page based on the information in the configuration element.
	 */
	public IWorkbenchPropertyPage createPage(Object element) // throws CoreException
	{
		IWorkbenchPropertyPage ppage = getPropertyPage();
		if (ppage != null)
		{
		  if (element instanceof IAdaptable) {
			  ppage.setElement((IAdaptable)element);
		  }
		  
		  ppage.setTitle(name);
		}
		return ppage;
	}
    /**
     * see IObjectContributor#canAdapt()
     */
    public boolean canAdapt() 
    {
	    return false;
    }

}