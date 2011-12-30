/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IActionFilter;

import org.eclipse.cdt.core.model.ICElement;

/**
 * An implementation of the IWorkbenchAdapter for CElements.
 */
public class CActionFilter implements IActionFilter {

	public CActionFilter() {
	}

	@Override
	public boolean testAttribute(Object target, String name, String value) {
		ICElement element = (ICElement) target;
		IResource resource = element.getResource();
		if (resource != null) {
			IActionFilter filter = (IActionFilter) resource.getAdapter(IActionFilter.class);
			if (filter != null) {
				return filter.testAttribute(resource, name, value);
			}
		}
		return false;
	}
}
