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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;

/**
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * 
 * @author vkong
 * @since 5.3
 *
 */
public class ResourceExclusion extends RefreshExclusion {
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.RefreshExclusion#getName()
	 */
	@Override
	public String getName() {
		return Messages.ResourceExclusion_name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.RefreshExclusion#testExclusion(org.eclipse.core.resources.IResource)
	 */
	@Override
	public boolean testExclusion(IResource resource) {
		//TODO: will need to change this for Phase 2 implementation
		List<IResource> excludedResources = new LinkedList<IResource>();
		List<ExclusionInstance> exclusionInstances = getExclusionInstances();
		Iterator<ExclusionInstance> iterator = exclusionInstances.iterator();
		while (iterator.hasNext()) {
			ExclusionInstance instance = iterator.next();
			excludedResources.add(instance.getResource());
		}
		
		if (resource instanceof IFolder) {
			return excludedResources.contains(resource);
		} else {
			Iterator<IResource> resources = excludedResources.iterator();
			while (resources.hasNext()) {
				IFolder excludedResource = (IFolder) resources.next();
				if (excludedResource.exists(resource.getFullPath()))
					return true;
			}			
		}
		return false;
	}

	@Override
	public boolean supportsExclusionInstances() {
		return true;
	}

}
