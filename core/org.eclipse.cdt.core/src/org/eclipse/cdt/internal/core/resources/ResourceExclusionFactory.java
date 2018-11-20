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

	@Override
	public RefreshExclusion createNewExclusion() {
		return new ResourceExclusion();
	}

	@Override
	public ExclusionInstance createNewExclusionInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getExclusionClassname() {
		return ResourceExclusion.class.getName();
	}

	@Override
	public String getInstanceClassname() {
		// TODO Auto-generated method stub
		return null;
	}

}
