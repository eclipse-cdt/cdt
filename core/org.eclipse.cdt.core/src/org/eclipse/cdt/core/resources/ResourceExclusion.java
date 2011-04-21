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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsExclusionInstances() {
		return true;
	}

}
