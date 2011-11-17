/**********************************************************************
 * Copyright (c) 2007, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 * Miwako Tokugawa (Intel Corporation) - bug 222817 (OptionCategoryApplicability)
**********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.enablement;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class HasNatureExpression implements IBooleanExpression {

	public static final String NAME = "hasNature"; //$NON-NLS-1$

	private static final String NATURE_ID = "natureId"; //$NON-NLS-1$

	private String natureId;

	public HasNatureExpression(IManagedConfigElement element) {
		natureId = element.getAttribute(NATURE_ID);
	}

	@Override
	public boolean evaluate(IResourceInfo rcInfo, IHoldsOptions holder,
			IOption option) {
		return evaluate(rcInfo);
	}

	@Override
	public boolean evaluate(IResourceInfo rcInfo, IHoldsOptions holder,
			IOptionCategory category) {
		return evaluate(rcInfo);
	}

	private boolean evaluate(IResourceInfo rcInfo) {
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
			if (project.isAccessible())
				return project.hasNature(natureId);
		} catch (CoreException e) {
			// Project close concurrently => Nature not available.
		}
		return false;
	}
}
