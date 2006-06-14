/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Viewer filter used in selection dialogs.
 */
public class TypedViewerFilter extends ViewerFilter {

	private Class[] fAcceptedTypes;
	private Object[] fRejectedElements;

	/**
	 * Creates a filter that only allows elements of gives types.
	 * @param acceptedTypes The types of accepted elements
	 */
	public TypedViewerFilter(Class[] acceptedTypes) {
		this(acceptedTypes, null);
	}

	/**
	 * Creates a filter that only allows elements of gives types, but not from a
	 * list of rejected elements.
	 * @param acceptedTypes Accepted elements must be of this types
	 * @param rejectedElements Element equals to the rejected elements are
	 * filtered out
	 */	
	public TypedViewerFilter(Class[] acceptedTypes, Object[] rejectedElements) {
		Assert.isNotNull(acceptedTypes);
		fAcceptedTypes= acceptedTypes;
		fRejectedElements= rejectedElements;
	}	

	/**
	 * @see ViewerFilter#select
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (fRejectedElements != null) {
			for (int i= 0; i < fRejectedElements.length; i++) {
				if (element.equals(fRejectedElements[i])) {
					return false;
				}
			}
		}
		for (int i= 0; i < fAcceptedTypes.length; i++) {
			if (fAcceptedTypes[i].isInstance(element)) {
				return true;
			}
		}
		return false;
	}

}
