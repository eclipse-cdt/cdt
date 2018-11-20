/*******************************************************************************
 * Copyright (c) 2011, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.resources;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.resources.ExclusionInstance;
import org.eclipse.cdt.core.resources.RefreshExclusion;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;

/**
 * @author vkong
 */
public class ResourceExclusion extends RefreshExclusion {
	@Override
	public synchronized String getName() {
		return Messages.ResourceExclusion_name;
	}

	@Override
	public boolean supportsExclusionInstances() {
		return true;
	}

	@Override
	public synchronized boolean testExclusion(IResource resource) {
		// Populate the resources to be excluded by this exclusion.
		List<IResource> excludedResources = new LinkedList<>();
		List<ExclusionInstance> exclusionInstances = getExclusionInstances();

		for (ExclusionInstance instance : exclusionInstances) {
			excludedResources.add(instance.getResource());
		}

		if (excludedResources.contains(resource)) {
			return true;
		}

		// Check to see if the given resource is part of this exclusion.
		for (IResource excludedResource : excludedResources) {
			// TODO: need to update this for Phase 2 implementation
			if (excludedResource instanceof IContainer) {
				IContainer container = (IContainer) excludedResource;
				if (container.getFullPath().isPrefixOf(resource.getFullPath())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Object clone() {
		ResourceExclusion clone = new ResourceExclusion();
		copyTo(clone);
		return clone;
	}
}
