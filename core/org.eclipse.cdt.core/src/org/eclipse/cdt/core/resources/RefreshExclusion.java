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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A RefreshExclusion represents a rule for excluding certain resources from being refreshed.
 * 
 * Clients should extend this class to provide support for their own custom exclusions.
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
public abstract class RefreshExclusion {
	
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

	protected List<ExclusionInstance> fExclusionInstanceList = new LinkedList<ExclusionInstance>();
	protected List<RefreshExclusion> fNestedExclusions = new LinkedList<RefreshExclusion>();
	protected ExclusionType fExclusionType;
	protected RefreshExclusion fParentExclusion;
	protected IResource fParentResource;
	

	protected String fContributorId;
	
	/**
	 * If this exclusion is a direct descendant of a resource, returns that resource.
	 * Otherwise, returns null;
	 * 
	 * @return IResource
	 */
	public IResource getParentResource() {
		return fParentResource;
	}

	/**
	 * Sets the parent resource of this exclusion.
	 * 
	 * @param parentResource the parent resource to set
	 */
	public void setParentResource(IResource parentResource) {
		this.fParentResource = parentResource;
	}
	
	/**
	 * @return a String corresponding to the ID of the RefreshExclusionContributor that was used to create
	 * this exclusion.
	 */
	public String getContributorId() {
		return fContributorId;
	}
	
	public void setContributorId(String id) {
		fContributorId = id;
	}
	
	/**
	 * If this is a nested exclusion, returns the exclusion which is the direct parent of this one.
	 * 
	 * @return RefreshExclusion
	 */
	public RefreshExclusion getParentExclusion() {
		return fParentExclusion;
	}

	public void setParentExclusion(RefreshExclusion parent) {
		fParentExclusion = parent;
	}

	public ExclusionType getExclusionType() {
		return fExclusionType;
	}

	public void setExclusionType(ExclusionType exclusionType) {
		fExclusionType = exclusionType;
	}

	/**
	 * @return a String corresponding to the human-readable name for this exclusion.
	 */
	public abstract String getName();
	
	/**
	 * Tests a given resource to see if this exclusion should exclude it from being refreshed.
	 * 
	 * @param resource the resource to be tested.
	 * @return true if the resource should be excluded, false otherwise.
	 */
	public abstract boolean testExclusion(IResource resource);
	
	/**
	 * @return an unmodifiable list of all the instance of this exclusion
	 */
	public List<ExclusionInstance> getExclusionInstances() {
		return Collections.unmodifiableList(fExclusionInstanceList);
	}
	
	/**
	 * Adds an instance to the list of instances of this exclusion.
	 * 
	 * @param exclusionInstance
	 */
	public void addExclusionInstance(ExclusionInstance exclusionInstance) {
		exclusionInstance.setParentExclusion(this);
		fExclusionInstanceList.add(exclusionInstance);
	}
	
	/**
	 * Removes an exclusion instance from the list of instances of this exclusion.
	 * 
	 * @param exclusionInstance
	 */
	public void removeExclusionInstance(ExclusionInstance exclusionInstance) {
		fExclusionInstanceList.remove(exclusionInstance);
	}
	
	/**
	 * 
	 * @return an unmodifiable list of exclusions to this exclusion.
	 */
	public List<RefreshExclusion> getNestedExclusions() {
		return Collections.unmodifiableList(fNestedExclusions);
	}
	
	public void addNestedExclusion(RefreshExclusion exclusion) {
		fNestedExclusions.add(exclusion);
		exclusion.setParentExclusion(this);
	}
	
	/**
	 * Removes the given nested exclusion.  The exclusion must be a direct child of this exclusion.
	 * 
	 * @param exclusion
	 */
	public void removeNestedExclusion(RefreshExclusion exclusion) {
		fNestedExclusions.remove(exclusion);
	}
	
	public void persistData(Document doc, Element parentElement) {
		// persist the common data that all RefreshExclusions have
		Element exclusionElement = doc.createElement(EXCLUSION_ELEMENT_NAME);
		
		// persist the type of the object we are
		exclusionElement.setAttribute(CLASS_ATTRIBUTE_NAME, this.getClass().getName());
		
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
			exclusionElement.setAttribute(EXCLUSION_TYPE_ATTRIBUTE_NAME, exclusionType);
		}
		
		// note: no need to persist parent, the parent relationship will be determined on load by
		// the structure of the XML tree
		
		exclusionElement.setAttribute(CONTRIBUTOR_ID_ATTRIBUTE_NAME, getContributorId());
		
        parentElement.appendChild(exclusionElement);
		
		// provide a place for extenders to store their own data
        Element extensionElement = doc.createElement(EXTENSION_DATA_ELEMENT_NAME);
        exclusionElement.appendChild(extensionElement);
        
        // persist instances
        for(ExclusionInstance instance : fExclusionInstanceList) {
        	instance.persistInstanceData(doc, extensionElement);
        }
		
		// call extender to store any extender-specific data
		persistExtendedData(doc, extensionElement);
		
		// persist nested exclusions
		for(RefreshExclusion exclusion : fNestedExclusions) {
			exclusion.persistData(doc, exclusionElement);
		}
	}
	
	protected void persistExtendedData(Document doc, Element extensionElement) {
		// override to provide extension specific behaviour if desired	
	}
	
	protected void loadExtendedData(Element parentElement) {
		// override to provide extension specific behaviour if desired
	}

	@SuppressWarnings("rawtypes")
	public static RefreshExclusion loadData(Element exclusionElement, RefreshExclusion parent) {

		
		// create an object of the proper type using zero-argument constructor
		RefreshExclusion newExclusion = null;
		String classname = exclusionElement.getAttribute(CLASS_ATTRIBUTE_NAME);
		Class extensionClass;
		try {
			extensionClass = Class.forName(classname);

			Class[] parameterTypes = new Class[0];
			Constructor constructor = extensionClass.getConstructor(parameterTypes);
			newExclusion = (RefreshExclusion) constructor.newInstance((Object[]) null);
		} catch (Exception e) {
			// error
			e.printStackTrace();
			return null;
		}
		
		// load the exclusion type
		String exclusionTypeString = exclusionElement.getAttribute(EXCLUSION_TYPE_ATTRIBUTE_NAME);
		if (exclusionTypeString != null) {
			if (exclusionTypeString.equals(FILE_VALUE)) {
				newExclusion.fExclusionType = org.eclipse.cdt.core.resources.ExclusionType.FILE;
			}

			else if (exclusionTypeString.equals(FOLDER_VALUE)) {
				newExclusion.fExclusionType = org.eclipse.cdt.core.resources.ExclusionType.FOLDER;
			}

			else if (exclusionTypeString.equals(RESOURCE_VALUE)) {
				newExclusion.fExclusionType = org.eclipse.cdt.core.resources.ExclusionType.RESOURCE;
			}

			else {
				// error
			}
		}	
		
		// set parent
		newExclusion.fParentExclusion = parent;
		
		newExclusion.fContributorId  = exclusionElement.getAttribute(CONTRIBUTOR_ID_ATTRIBUTE_NAME);
		
		// get the extension element
		NodeList extensionList = exclusionElement.getElementsByTagName(EXTENSION_DATA_ELEMENT_NAME);
		
		for(int k = 0; k < extensionList.getLength(); k++) {
			Node node = extensionList.item(k);
			// the node will be an Element
			if(node instanceof Element) {
				Element extensionElement = (Element) node;
				
				// load the extension's data
				newExclusion.loadExtendedData(extensionElement);
			}
		}
		
		// load instances
		NodeList instanceList = exclusionElement.getElementsByTagName(INSTANCE_ELEMENT_NAME);
		
		for(int k = 0; k < instanceList.getLength(); k++) {
			Node node = instanceList.item(k);
			
			// the node will be an element
			if(node instanceof Element) {
				Element instanceElement = (Element) node;
				
				// load the instance data
				ExclusionInstance instance = ExclusionInstance.loadInstanceData(instanceElement);
				newExclusion.fExclusionInstanceList.add(instance);
			}
		}
		
		// load nested exclusions
		NodeList nestedExclusionsList = exclusionElement.getElementsByTagName(EXCLUSION_ELEMENT_NAME);
		
		for(int k  = 0; k < nestedExclusionsList.getLength(); k++) {
			Node node = nestedExclusionsList.item(k);
			
			// the node will be an element
			if(node instanceof Element) {
				Element nestedExclusionElement = (Element) node;
				
				// load the nested exclusion
				RefreshExclusion nestedExclusion = loadData(nestedExclusionElement, newExclusion);
				newExclusion.addNestedExclusion(nestedExclusion);
			}
		}
		
		return newExclusion;
	}

}
