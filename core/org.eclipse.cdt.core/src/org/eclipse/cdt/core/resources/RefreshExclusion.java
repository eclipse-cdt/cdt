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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

	/**
	 * Indicates the type of resources that this exclusion can exclude.  Used to determine which type of icon is displayed in
	 * the exclusion UI when this exclusion is present.
	 *
	 */
	public enum ExclusionType {
		/**
		 * Constant indicating that this exclusion only excludes folders.
		 */
		FOLDER,
		
		
		/**
		 * Constant indicating that this exclusion only excludes folders.
		 */
		FILE,
		
		
		/**
		 * Constant indicating that this exclusion can exclude any resource.
		 */
		RESOURCE
	}
	
	/**
	 * Represents a particular instance of an exclusion.  E.g., if an exclusion allowed
	 * for the exclusion of a list individual resources, there would be one exclusion instance
	 * per resource.  Each exclusion instance is presented in the user interface as a child of the exclusion.
	 * 
	 * Clients may extend this class to provide custom implementations for their exclusion type.
	 *
	 */
	public class ExclusionInstance {
		
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

		public void loadInstanceData(Element extensionElement) {
			
		}
		
		protected void loadExtendedInstanceData(Element instanceElement) {
			// override to provide extension specific behaviour if desired
		}
	}

	protected List<ExclusionInstance> fExclusionInstanceList = new LinkedList<ExclusionInstance>();
	protected List<RefreshExclusion> fNestedExclusions = new LinkedList<RefreshExclusion>();
	protected ExclusionType fExclusionType;
	protected RefreshExclusion fParent;
	protected String fContributorId;
	
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
	public RefreshExclusion getParent() {
		return fParent;
	}

	public void setParent(RefreshExclusion parent) {
		fParent = parent;
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
		exclusion.setParent(this);
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

	public void loadData(Element parentElement) {
		
	}

}
