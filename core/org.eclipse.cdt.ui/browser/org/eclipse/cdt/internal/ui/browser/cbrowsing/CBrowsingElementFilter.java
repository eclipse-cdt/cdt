/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.cbrowsing;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.internal.ui.filters.NonCElementFilter;
import org.eclipse.jface.viewers.Viewer;

public class CBrowsingElementFilter extends NonCElementFilter {

	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof ITypeInfo)
			return true;
		return super.select(viewer, parent, element);
	}
}
