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
package org.eclipse.cdt.internal.core.resources;

import org.eclipse.cdt.core.resources.ExclusionInstance;
import org.eclipse.cdt.core.resources.RefreshExclusion;
import org.eclipse.cdt.core.resources.RefreshExclusionFactory;

/**
 * @author crecoskie
 * 
 */
public class ResourceExclusionFactory extends RefreshExclusionFactory {

	/**
	 * 
	 */
	public ResourceExclusionFactory() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.resources.RefreshExclusionFactory#createNewExclusion()
	 */
	@Override
	public RefreshExclusion createNewExclusion() {
		return new ResourceExclusion();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.resources.RefreshExclusionFactory#createNewExclusionInstance()
	 */
	@Override
	public ExclusionInstance createNewExclusionInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.resources.RefreshExclusionFactory#getExclusionClassname()
	 */
	@Override
	public String getExclusionClassname() {
		return ResourceExclusion.class.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.resources.RefreshExclusionFactory#getInstanceClassname()
	 */
	@Override
	public String getInstanceClassname() {
		// TODO Auto-generated method stub
		return null;
	}

}
