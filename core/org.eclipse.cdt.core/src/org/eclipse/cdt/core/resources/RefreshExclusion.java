/*******************************************************************************
 *  Copyright (c) 2011, 2012 IBM Corporation and others.
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.ibm.icu.text.MessageFormat;

/**
 * A RefreshExclusion represents a rule for excluding certain resources from being refreshed.
 * 
 * Clients should extend this class to provide support for their own custom exclusions.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There
 * is no guarantee that this API will work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 * 
 * @author crecoskie
 * @since 5.3
 * 
 */
public abstract class RefreshExclusion implements Cloneable{

	public static final String CLASS_ATTRIBUTE_NAME = "class"; //$NON-NLS-1$
	public static final String CONTRIBUTOR_ID_ATTRIBUTE_NAME = "contributorId"; //$NON-NLS-1$
	public static final String DISPLAY_STRING_ATTRIBUTE_NAME = "displayString"; //$NON-NLS-1$
	public static final String EXCLUSION_ELEMENT_NAME = "exclusion"; //$NON-NLS-1$
	public static final String EXCLUSION_TYPE_ATTRIBUTE_NAME = "exclusionType"; //$NON-NLS-1$
	public static final String EXTENSION_DATA_ELEMENT_NAME = "extensionData"; //$NON-NLS-1$
	public static final String FILE_VALUE = "FILE"; //$NON-NLS-1$
	public static final String FOLDER_VALUE = "FOLDER"; //$NON-NLS-1$
	public static final String INSTANCE_ELEMENT_NAME = "instance"; //$NON-NLS-1$
	public static final String RESOURCE_VALUE = "RESOURCE"; //$NON-NLS-1$
	public static final String WORKSPACE_PATH_ATTRIBUTE_NAME = "workspacePath"; //$NON-NLS-1$

	public synchronized static List<RefreshExclusion> loadData(ICStorageElement parentElement,
			RefreshExclusion parentExclusion, IResource parentResource, RefreshScopeManager manager)
			throws CoreException {

		List<RefreshExclusion> exclusions = new LinkedList<RefreshExclusion>();

		// the parent element might contain any number of exclusions... iterate through the list
		ICStorageElement[] children = parentElement.getChildren();

		for (ICStorageElement child : children) {

			if (child.getName().equals(EXCLUSION_ELEMENT_NAME)) {

				// create an object of the proper type
				String className = child.getAttribute(CLASS_ATTRIBUTE_NAME);
				RefreshExclusion newExclusion = manager.getExclusionForClassName(className);

				if (newExclusion == null) {
					throw new CoreException(CCorePlugin.createStatus(MessageFormat.format(
							Messages.RefreshExclusion_0, className)));
				}

				// load the exclusion type
				String exclusionTypeString = child.getAttribute(EXCLUSION_TYPE_ATTRIBUTE_NAME);
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

				// set parent if nested
				newExclusion.fParentExclusion = parentExclusion;

				// set parent resource if there is one
				newExclusion.fParentResource = parentResource;

				newExclusion.fContributorId = child.getAttribute(CONTRIBUTOR_ID_ATTRIBUTE_NAME);

				// get the extension element
				ICStorageElement[] grandchildren = child.getChildren();

				for (ICStorageElement grandchild : grandchildren) {

					if (grandchild.getName().equals(RefreshExclusion.EXTENSION_DATA_ELEMENT_NAME)) {

						// load the extension's data
						newExclusion.loadExtendedData(grandchild);
					}

					else if (grandchild.getName().equals(INSTANCE_ELEMENT_NAME)) {

						// load the instance data
						ExclusionInstance instance = ExclusionInstance.loadInstanceData(grandchild,
								manager);
						newExclusion.fExclusionInstanceList.add(instance);
					}
				}

				// load nested exclusions
				List<RefreshExclusion> nestedExclusions = loadData(child, newExclusion, null,
						manager);

				// add to parent
				for (RefreshExclusion nestedExclusion : nestedExclusions) {
					newExclusion.addNestedExclusion(nestedExclusion);
				}

				// add the new exclusion to the list of exclusions to return
				exclusions.add(newExclusion);
			}
		}

		return exclusions;
	}

	protected String fContributorId = ""; //$NON-NLS-1$
	protected List<ExclusionInstance> fExclusionInstanceList = new LinkedList<ExclusionInstance>();
	protected ExclusionType fExclusionType = ExclusionType.RESOURCE;
	protected List<RefreshExclusion> fNestedExclusions = new LinkedList<RefreshExclusion>();

	protected RefreshExclusion fParentExclusion;

	protected IResource fParentResource;

	/**
	 * Adds an instance to the list of instances of this exclusion.
	 * 
	 * @param exclusionInstance
	 */
	public synchronized void addExclusionInstance(ExclusionInstance exclusionInstance) {
		exclusionInstance.setParentExclusion(this);
		fExclusionInstanceList.add(exclusionInstance);
	}

	public synchronized void addNestedExclusion(RefreshExclusion exclusion) {
		fNestedExclusions.add(exclusion);
		exclusion.setParentExclusion(this);
	}

	/**
	 * @return a String corresponding to the ID of the RefreshExclusionContributor that was used to create
	 *         this exclusion.
	 */
	public synchronized String getContributorId() {
		return fContributorId;
	}

	/**
	 * @return an unmodifiable list of all the instance of this exclusion
	 */
	public synchronized List<ExclusionInstance> getExclusionInstances() {
		return Collections.unmodifiableList(fExclusionInstanceList);
	}

	public synchronized ExclusionType getExclusionType() {
		return fExclusionType;
	}

	/**
	 * @return a String corresponding to the human-readable name for this exclusion.
	 */
	public abstract String getName();

	/**
	 * 
	 * @return an unmodifiable list of exclusions to this exclusion.
	 */
	public synchronized List<RefreshExclusion> getNestedExclusions() {
		return Collections.unmodifiableList(fNestedExclusions);
	}

	/**
	 * If this is a nested exclusion, returns the exclusion which is the direct parent of this one.
	 * 
	 * @return RefreshExclusion
	 */
	public synchronized RefreshExclusion getParentExclusion() {
		return fParentExclusion;
	}

	/**
	 * If this exclusion is a direct descendant of a resource, returns that resource. Otherwise, returns null;
	 * 
	 * @return IResource
	 */
	public synchronized IResource getParentResource() {
		return fParentResource;
	}

	protected synchronized void loadExtendedData(ICStorageElement grandchild) {
		// override to provide extension specific behaviour if desired
	}

	public synchronized void persistData(ICStorageElement parentElement) {
		// persist the common data that all RefreshExclusions have
		ICStorageElement exclusionElement = parentElement.createChild(EXCLUSION_ELEMENT_NAME);

		// persist the type of the object we are
		exclusionElement.setAttribute(CLASS_ATTRIBUTE_NAME, this.getClass().getName());

		// persist the exclusion type
		String exclusionType = null;
		switch (getExclusionType()) {
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

		if (exclusionType != null) {
			exclusionElement.setAttribute(EXCLUSION_TYPE_ATTRIBUTE_NAME, exclusionType);
		}

		// note: no need to persist parent, the parent relationship will be determined on load by
		// the structure of the XML tree

		exclusionElement.setAttribute(CONTRIBUTOR_ID_ATTRIBUTE_NAME, getContributorId());

		// persist instances
		for (ExclusionInstance instance : fExclusionInstanceList) {
			instance.persistInstanceData(exclusionElement);
		}

		// provide a place for extenders to store their own data
		ICStorageElement extensionElement = exclusionElement
				.createChild(EXTENSION_DATA_ELEMENT_NAME);

		// call extender to store any extender-specific data
		persistExtendedData(extensionElement);

		// persist nested exclusions
		for (RefreshExclusion exclusion : fNestedExclusions) {
			exclusion.persistData(exclusionElement);
		}
	}

	protected synchronized void persistExtendedData(ICStorageElement extensionElement) {
		// override to provide extension specific behaviour if desired
	}

	/**
	 * Removes an exclusion instance from the list of instances of this exclusion.
	 * 
	 * @param exclusionInstance
	 */
	public synchronized void removeExclusionInstance(ExclusionInstance exclusionInstance) {
		fExclusionInstanceList.remove(exclusionInstance);
	}

	/**
	 * Removes the given nested exclusion. The exclusion must be a direct child of this exclusion.
	 * 
	 * @param exclusion
	 */
	public synchronized void removeNestedExclusion(RefreshExclusion exclusion) {
		fNestedExclusions.remove(exclusion);
	}

	public synchronized void setContributorId(String id) {
		fContributorId = id;
	}

	public synchronized void setExclusionType(ExclusionType exclusionType) {
		fExclusionType = exclusionType;
	}

	public synchronized void setParentExclusion(RefreshExclusion parent) {
		fParentExclusion = parent;
	}

	/**
	 * Sets the parent resource of this exclusion.
	 * 
	 * @param parentResource
	 *            the parent resource to set
	 */
	public synchronized void setParentResource(IResource parentResource) {
		this.fParentResource = parentResource;
	}

	/**
	 * @return true if this exclusion supports exclusion instances
	 */
	public abstract boolean supportsExclusionInstances();

	/**
	 * Tests a given resource to see if this exclusion applies to it.
	 * 
	 * @param resource
	 *            the resource to be tested.
	 * @return true if the resource triggers the exclusion, false otherwise (including if this exclusion does
	 *         not apply).
	 */
	public abstract boolean testExclusion(IResource resource);

	/**
	 * Tests this exclusion and recursively test all of its nested exclusions to determine whether this
	 * exclusion should be triggered or not.
	 * 
	 * @param resource
	 *            the resource to be tested
	 * @return true if the exclusion is triggered, false otherwise (including if this exclusion does not
	 *         apply)
	 */
	public synchronized boolean testExclusionChain(IResource resource) {
		// first check and see if this exclusion would be triggered in the first place
		boolean currentValue = testExclusion(resource);

		if (currentValue) {
			List<RefreshExclusion> nestedExclusions = getNestedExclusions();
			for (RefreshExclusion exclusion : nestedExclusions) {

				boolean nestedValue = exclusion.testExclusionChain(resource);

				if (nestedValue) {
					// the nested exclusion says to do the opposite of what we originally thought, so negate
					// the current value
					currentValue = (!currentValue);

					// since the first exclusion chain to trump us wins, then, break out of the loop
					break;
				}

			}
		}

		return currentValue;

	}
	
	/**
	 * Duplicate this refresh exclusion to the given one.
	 * @param destination - the refresh exclusion to be modified
	 * @since 5.4
	 */
	protected void copyTo (RefreshExclusion destination) {
		destination.setContributorId(getContributorId());
		destination.setExclusionType(getExclusionType());
		destination.setParentResource(getParentResource());
		
		Iterator<RefreshExclusion> iterator = getNestedExclusions().iterator();
		while (iterator.hasNext()) {
			RefreshExclusion nestedExclusion = iterator.next();
			RefreshExclusion clone;
			clone = (RefreshExclusion) nestedExclusion.clone();
			clone.setParentExclusion(destination);
			destination.addNestedExclusion(clone);
		}
		
		Iterator<ExclusionInstance> exclusionInstances = getExclusionInstances().iterator();
		
		while(exclusionInstances.hasNext()) {
			ExclusionInstance next = exclusionInstances.next();
			ExclusionInstance newInstance = new ExclusionInstance();
			newInstance.setDisplayString(next.getDisplayString());
			newInstance.setExclusionType(next.getExclusionType());
			newInstance.setParentExclusion(destination);
			newInstance.setResource(next.getResource());
			destination.addExclusionInstance(newInstance);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public abstract Object clone();
}
