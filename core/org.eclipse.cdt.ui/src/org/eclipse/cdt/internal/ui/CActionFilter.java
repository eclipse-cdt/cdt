/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *     QNX Software System
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IActionFilter;

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
			IActionFilter filter = resource.getAdapter(IActionFilter.class);
			if (filter != null) {
				return filter.testAttribute(resource, name, value);
			}
		}
		return false;
	}
}
