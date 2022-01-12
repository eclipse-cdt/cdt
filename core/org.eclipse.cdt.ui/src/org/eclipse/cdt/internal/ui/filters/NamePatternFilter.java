/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.filters;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.util.StringMatcher;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * The NamePatternFilter selects the elements which
 * match the given string patterns.
 * <p>
 * The following characters have special meaning:
 *   ? => any character
 *   * => any string
 * </p>
 *
 * @since 2.0
 */
public class NamePatternFilter extends ViewerFilter {
	private String[] fPatterns;
	private StringMatcher[] fMatchers;

	/**
	 * Return the currently configured StringMatchers.
	 */
	private StringMatcher[] getMatchers() {
		return fMatchers;
	}

	/**
	 * Gets the patterns for the receiver.
	 */
	public String[] getPatterns() {
		return fPatterns;
	}

	/* (non-Javadoc)
	 * Method declared on ViewerFilter.
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		String matchName = null;
		if (element instanceof ICElement) {
			matchName = ((ICElement) element).getElementName();
		} else if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			ICElement javaElement = adaptable.getAdapter(ICElement.class);
			if (javaElement != null)
				matchName = javaElement.getElementName();
			else {
				IResource resource = adaptable.getAdapter(IResource.class);
				if (resource != null)
					matchName = resource.getName();
			}
		}
		if (matchName != null) {
			StringMatcher[] testMatchers = getMatchers();
			for (int i = 0; i < testMatchers.length; i++) {
				if (testMatchers[i].match(matchName))
					return false;
			}
			return true;
		}
		return true;
	}

	/**
	 * Sets the patterns to filter out for the receiver.
	 * <p>
	 * The following characters have special meaning:
	 *   ? => any character
	 *   * => any string
	 * </p>
	 */
	public void setPatterns(String[] newPatterns) {
		fPatterns = newPatterns;
		fMatchers = new StringMatcher[newPatterns.length];
		for (int i = 0; i < newPatterns.length; i++) {
			//Reset the matchers to prevent constructor overhead
			fMatchers[i] = new StringMatcher(newPatterns[i], true, false);
		}
	}
}
