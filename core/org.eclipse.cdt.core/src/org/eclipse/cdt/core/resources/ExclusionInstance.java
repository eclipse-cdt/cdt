/*******************************************************************************
 *  Copyright (c) 2011 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.resources;

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Represents a particular instance of an exclusion. E.g., if an exclusion allowed for the exclusion of a list
 * individual resources, there would be one exclusion instance per resource. Each exclusion instance is
 * presented in the user interface as a child of the exclusion.
 *
 * Clients may extend this class to provide custom implementations for their exclusion type.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There
 * is no guarantee that this API will work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 *
 * @author crecoskie
 * @since 5.3
 *
 */
public class ExclusionInstance {

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

	public synchronized static ExclusionInstance loadInstanceData(ICStorageElement instanceElement,
			RefreshScopeManager manager) {

		String className = instanceElement.getAttribute(CLASS_ATTRIBUTE_NAME);

		ExclusionInstance newInstance = null;

		// see if there is a custom instance class
		newInstance = manager.getInstanceForClassName(className);

		if (newInstance == null) {
			newInstance = new ExclusionInstance();
		}

		// load the exclusion type
		String exclusionTypeString = instanceElement.getAttribute(EXCLUSION_TYPE_ATTRIBUTE_NAME);
		if (exclusionTypeString != null) {
			if (exclusionTypeString.equals(FILE_VALUE)) {
				newInstance.fInstanceExclusionType = org.eclipse.cdt.core.resources.ExclusionType.FILE;
			}

			else if (exclusionTypeString.equals(FOLDER_VALUE)) {
				newInstance.fInstanceExclusionType = org.eclipse.cdt.core.resources.ExclusionType.FOLDER;
			}

			else if (exclusionTypeString.equals(RESOURCE_VALUE)) {
				newInstance.fInstanceExclusionType = org.eclipse.cdt.core.resources.ExclusionType.RESOURCE;
			}

			else {
				// error
				return null;
			}
		}

		// load resource path, use it to get the corresponding resource
		String resourcePath = instanceElement.getAttribute(WORKSPACE_PATH_ATTRIBUTE_NAME);

		if (resourcePath != null) {
			newInstance.fResource = ResourcesPlugin.getWorkspace().getRoot().findMember(resourcePath);
		}

		// load display string
		newInstance.fDisplayString = instanceElement.getAttribute(DISPLAY_STRING_ATTRIBUTE_NAME);

		// load any data from extenders
		newInstance.loadExtendedInstanceData(instanceElement);

		return newInstance;
	}

	protected String fDisplayString;
	protected ExclusionType fInstanceExclusionType;
	protected RefreshExclusion fParent;

	protected IResource fResource;

	/**
	 * @return a String corresponding to the human-readable name for this exclusion instance. Examples of this
	 *         would be the resource name for a resource based exclusion, or the file extension excluded by a
	 *         file extension exclusion.
	 */
	public synchronized String getDisplayString() {
		return fDisplayString;
	}

	public synchronized ExclusionType getExclusionType() {
		return fInstanceExclusionType;
	}

	/**
	 * Returns the parent exclusion of this exclusion instance.
	 *
	 * @return RefreshExclusion
	 */
	public synchronized RefreshExclusion getParentExclusion() {
		return fParent;
	}

	/**
	 * If there is a resource directly associated with this exclusion instance, returns the resource.
	 *
	 * @return IResource
	 */
	public synchronized IResource getResource() {
		return fResource;
	}

	protected synchronized void loadExtendedInstanceData(ICStorageElement child) {
		// override to provide extension specific behaviour if desired
	}

	protected synchronized void persistExtendedInstanceData(ICStorageElement instanceElement) {
		// override to provide extension specific behaviour if desired
	}

	public synchronized void persistInstanceData(ICStorageElement exclusionElement) {

		ICStorageElement instanceElement = exclusionElement.createChild(INSTANCE_ELEMENT_NAME);

		// persist the type of the object we are
		instanceElement.setAttribute(CLASS_ATTRIBUTE_NAME, this.getClass().getName());

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
			instanceElement.setAttribute(EXCLUSION_TYPE_ATTRIBUTE_NAME, exclusionType);
		}

		// persist resource path
		if (fResource != null) {
			instanceElement.setAttribute(WORKSPACE_PATH_ATTRIBUTE_NAME, fResource.getFullPath().toString());
		}

		// persist display string
		if (fDisplayString != null) {
			instanceElement.setAttribute(DISPLAY_STRING_ATTRIBUTE_NAME, fDisplayString);
		}

		// persist any data from extenders
		persistExtendedInstanceData(instanceElement);

	}

	public synchronized void setDisplayString(String displayString) {
		fDisplayString = displayString;
	}

	public synchronized void setExclusionType(ExclusionType type) {
		fInstanceExclusionType = type;
	}

	/**
	 * @param parent
	 *            the RefreshExclusion to set as the parent.
	 */
	public synchronized void setParentExclusion(RefreshExclusion parent) {
		fParent = parent;
	}

	public synchronized void setResource(IResource resource) {
		fResource = resource;
	}
}