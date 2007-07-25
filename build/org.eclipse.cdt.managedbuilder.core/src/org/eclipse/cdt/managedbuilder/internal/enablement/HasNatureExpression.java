/**********************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.enablement;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class HasNatureExpression implements IBooleanExpression {

	public static final String NAME = "hasNature";
	
	private static final String NATURE_ID = "natureId";
	
	private String natureId;
	
	public HasNatureExpression(IManagedConfigElement element) {
		natureId = element.getAttribute(NATURE_ID);
	}
	
	public boolean evaluate(IResourceInfo rcInfo, IHoldsOptions holder,
			IOption option) {
		// All null checks returns false to keep this expression
		// from accidentally turning things on. Although
		// a 'z' value would be better to avoid having any affect.
		if (natureId == null || natureId.length() == 0)
			return false;
		
		IConfiguration config = rcInfo.getParent();
		if (config == null)
			return false;
		
		IResource resource = config.getOwner();
		if (resource == null)
			return false;
		
		IProject project = resource.getProject();
		try {
			IProjectDescription projDesc = project.getDescription();
			String[] natures = projDesc.getNatureIds();
			for (int i = 0; i < natures.length; ++i) {
				if (natureId.equals(natures[i]))
					return true;
			}
			// Not found
			return false;
		} catch (CoreException e) {
			ManagedBuilderCorePlugin.log(e);
			return false;
		}
	}

}
