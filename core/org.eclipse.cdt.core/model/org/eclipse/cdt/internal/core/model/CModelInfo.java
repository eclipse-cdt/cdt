package org.eclipse.cdt.internal.core.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * @see ICModel
 */
public class CModelInfo extends CContainerInfo {

	/**
	 * Constructs a new C Model Info 
	 */
	protected CModelInfo(CElement element) {
		super(element);
	}

	/**
	 * Compute the non-java resources contained in this java project.
	 */
	private Object[] computeNonCResources() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		int length = projects.length;
		Object[] nonCProjects = null;
		int index = 0;
		for (int i = 0; i < length; i++) {
			IProject project = projects[i];
			if (!CProject.hasCNature(project)) {
				if (nonCProjects == null) {
					nonCProjects = new Object[length];
				}
				nonCProjects[index++] = project;
			}
		}
		if (index == 0) return NO_NON_C_RESOURCES;
		if (index < length) {
			System.arraycopy(nonCProjects, 0, nonCProjects = new Object[index], 0, index);
		}
		return nonCProjects;
	}

	/**
	 * Returns an array of non-C resources contained in the receiver.
	 */
	Object[] getNonCResources() {
		if (nonCResources == null) {
			nonCResources = computeNonCResources();
		}
		return nonCResources;
	}

}
