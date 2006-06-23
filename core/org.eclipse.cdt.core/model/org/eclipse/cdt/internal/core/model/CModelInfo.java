/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * @see ICModel
 */
public class CModelInfo extends OpenableInfo {

	Object[] nonCResources = null;

	/**
	 * Constructs a new C Model Info 
	 */
	protected CModelInfo(CElement element) {
		super(element);
	}

	/**
	 * Compute the non-C resources contained in this C project.
	 */
	private Object[] computeNonCResources() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		int length = projects.length;
		Object[] nonCProjects = null;
		int index = 0;
		for (int i = 0; i < length; i++) {
			IProject project = projects[i];
			if (!(CoreModel.hasCNature(project) || CoreModel.hasCCNature(project))) {
				if (nonCProjects == null) {
					nonCProjects = new Object[length];
				}
				nonCProjects[index++] = project;
			}
		}
		if (index == 0) {
			return NO_NON_C_RESOURCES;
		}
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

	/**
	 * @return
	 */
	public void setNonCResources(Object[] resources) {
		nonCResources = resources;
	}

}
