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

package org.eclipse.cdt.internal.core.resources;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.resources.ExclusionInstance;
import org.eclipse.cdt.core.resources.Messages;
import org.eclipse.cdt.core.resources.RefreshExclusion;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;

/**
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There
 * is no guarantee that this API will work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 * 
 * @author vkong
 * @since 5.3
 * 
 */
public class ResourceExclusion extends RefreshExclusion {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.resources.RefreshExclusion#getName()
	 */
	@Override
	public synchronized String getName() {
		return Messages.ResourceExclusion_name;
	}

	@Override
	public boolean supportsExclusionInstances() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.core.resources.RefreshExclusion#testExclusion(org.eclipse.core.resources.IResource)
	 */
	@Override
	public synchronized boolean testExclusion(IResource resource) {

		// Populate the resources to be excluded by this exclusion
		List<IResource> excludedResources = new LinkedList<IResource>();
		List<ExclusionInstance> exclusionInstances = getExclusionInstances();

		for (ExclusionInstance instance : exclusionInstances) {
			excludedResources.add(instance.getResource());
		}

		if (excludedResources.contains(resource)) {
			return true;
		} else { // check to see if the given resource is part of this exclusion

			for (IResource excludedResource : excludedResources) {
				// TODO: need to update this for Phase 2 implementation
				if (excludedResource instanceof IContainer) {
					IContainer container = (IContainer) excludedResource;
					if (container.getFullPath().isPrefixOf(resource.getFullPath())) {
						return true;
					}
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
