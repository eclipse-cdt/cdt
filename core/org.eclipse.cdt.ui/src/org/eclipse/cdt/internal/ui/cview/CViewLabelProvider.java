/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.internal.ui.IAdornmentProvider;
import org.eclipse.cdt.internal.ui.StandardCElementLabelProvider;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/*
 * CViewLabelProvider 
 */
public class CViewLabelProvider extends StandardCElementLabelProvider {
	
	/**
	 * 
	 */
	public CViewLabelProvider() {
		super();
	}

	/**
	 * @param flags
	 * @param adormentProviders
	 */
	public CViewLabelProvider(int flags, IAdornmentProvider[] adormentProviders) {
		super(flags, adormentProviders);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof IIncludeReference) {
			IIncludeReference ref = (IIncludeReference)element;
			Object parent = ref.getParent();
			if (parent instanceof IIncludeReference) {
				IPath p = ref.getPath();
				IPath parentLocation = ((IIncludeReference)parent).getPath();
				if (parentLocation.isPrefixOf(p)) {
					p = p.setDevice(null);
					p = p.removeFirstSegments(parentLocation.segmentCount());
				}
				return p.toString();
			}
		}
		return super.getText(element);
	}
}
