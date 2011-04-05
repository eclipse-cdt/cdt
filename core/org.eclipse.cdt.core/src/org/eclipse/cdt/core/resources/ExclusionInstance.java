/*******************************************************************************
 *  Copyright (c) 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.resources;

import java.lang.reflect.Constructor;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a particular instance of an exclusion.  E.g., if an exclusion allowed
 * for the exclusion of a list individual resources, there would be one exclusion instance
 * per resource.  Each exclusion instance is presented in the user interface as a child of the exclusion.
 * 
 * Clients may extend this class to provide custom implementations for their exclusion type.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * 
 * @author crecoskie
 * @since 5.3
 *
 */
public class ExclusionInstance {
	
	public static final String CLASS_ATTRIBUTE_NAME = "class"; //$NON-NLS-1$
	public static final String EXTENSION_DATA_ELEMENT_NAME = "extensionData"; //$NON-NLS-1$
	public static final String CONTRIBUTOR_ID_ATTRIBUTE_NAME = "contributorId"; //$NON-NLS-1$
	public static final String INSTANCE_ELEMENT_NAME = "instance"; //$NON-NLS-1$
	public static final String WORKSPACE_PATH_ATTRIBUTE_NAME = "workspacePath"; //$NON-NLS-1$
	public static final String EXCLUSION_TYPE_ATTRIBUTE_NAME = "exclusionType"; //$NON-NLS-1$
	public static final String EXCLUSION_ELEMENT_NAME = "exclusion"; //$NON-NLS-1$
	public static final String RESOURCE_VALUE = "RESOURCE"; //$NON-NLS-1$
	public static final String FOLDER_VALUE = "FOLDER"; //$NON-NLS-1$
	public static final String FILE_VALUE = "FILE"; //$NON-NLS-1$
	public static final String DISPLAY_STRING_ATTRIBUTE_NAME = "displayString"; //$NON-NLS-1$
	
	private ExclusionType fInstanceExclusionType;
	private IResource fResource;
	private String fDisplayString;

	public ExclusionType getExclusionType() {
		return fInstanceExclusionType;
	}
	
	public void setExclusionType(ExclusionType type) {
		fInstanceExclusionType = type;
	}
	
	/**
	 * If there is a resource directly associated with this exclusion instance, returns the resource.
	 * 
	 * @return IResource
	 */
	public IResource getResource() {
		return fResource;
	}
	
	public void setResource(IResource resource) {
		fResource = resource;
	}
	
	/**
	 * @return a String corresponding to the human-readable name for this exclusion instance.
	 * Examples of this would be the resource name for a resource based exclusion, or the file extension
	 * excluded by a file extension exclusion.
	 */
	public String getDisplayString() {
		return fDisplayString;
	}
	
	public void setDisplayString(String displayString) {
		fDisplayString = displayString;
	}

	public void persistInstanceData(Document doc, Element extensionElement) {
		
		// persist the type of the object we are
		extensionElement.setAttribute(CLASS_ATTRIBUTE_NAME, this.getClass().getName());
		
		Element instanceElement = doc.createElement(INSTANCE_ELEMENT_NAME);
		
		// persist the exclusion type
		String exclusionType = null;
		switch(getExclusionType()) {
		case FILE:
			exclusionType = FILE_VALUE;
			break;
			
		case FOLDER:
			exclusionType = FOLDER_VALUE;
			break;
			
		case RESOURCE:
			exclusionType = RESOURCE_VALUE;
			break;
		}
		
		if(exclusionType != null) {
			instanceElement.setAttribute(EXCLUSION_TYPE_ATTRIBUTE_NAME, exclusionType);
		}
		
		// persist resource path
		if(fResource != null) {
			instanceElement.setAttribute(WORKSPACE_PATH_ATTRIBUTE_NAME, fResource.getFullPath().toString());
		}
		
		// persist display string
		if(fDisplayString != null) {
			instanceElement.setAttribute(DISPLAY_STRING_ATTRIBUTE_NAME, fDisplayString);
		}
		
		// persist any data from extenders
		persistExtendedInstanceData(doc, instanceElement);
		
	}
	
	protected void persistExtendedInstanceData(Document doc, Element instanceElement) {
		// override to provide extension specific behaviour if desired	
	}

	@SuppressWarnings("rawtypes")
	public static ExclusionInstance loadInstanceData(Element instanceElement) {
		
		String classname = instanceElement.getAttribute(CLASS_ATTRIBUTE_NAME);
		
		ExclusionInstance newInstance = null;
		Class instanceClass;
		try {
			instanceClass = Class.forName(classname);

			Class[] parameterTypes = new Class[0];
			Constructor constructor = instanceClass.getConstructor(parameterTypes);
			newInstance = (ExclusionInstance) constructor.newInstance((Object[]) null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		// load the exclusion type
		String exclusionTypeString = instanceElement.getAttribute(EXCLUSION_TYPE_ATTRIBUTE_NAME);
		if(exclusionTypeString != null) {
			if(exclusionTypeString.equals(FILE_VALUE)) {
				newInstance.fInstanceExclusionType = org.eclipse.cdt.core.resources.ExclusionType.FILE;
			}
			
			else if(exclusionTypeString.equals(FOLDER_VALUE)) {
				newInstance.fInstanceExclusionType = org.eclipse.cdt.core.resources.ExclusionType.FOLDER;
			}
			
			else if(exclusionTypeString.equals(RESOURCE_VALUE)) {
				newInstance.fInstanceExclusionType = org.eclipse.cdt.core.resources.ExclusionType.RESOURCE;
			}
			
			else {
				// error
				return null;
			}
		}		
		
		// load resource path, use it to get the corresponding resource
		String resourcePath = instanceElement.getAttribute(WORKSPACE_PATH_ATTRIBUTE_NAME);
		
		if(resourcePath != null) {
			newInstance.fResource = ResourcesPlugin.getWorkspace().getRoot().findMember(resourcePath);
		}
		
		// load display string
		newInstance.fDisplayString = instanceElement.getAttribute(DISPLAY_STRING_ATTRIBUTE_NAME);
		
		
		// load any data from extenders
		newInstance.loadExtendedInstanceData(instanceElement);
		
		return newInstance;
	}
	
	protected void loadExtendedInstanceData(Element instanceElement) {
		// override to provide extension specific behaviour if desired
	}
}