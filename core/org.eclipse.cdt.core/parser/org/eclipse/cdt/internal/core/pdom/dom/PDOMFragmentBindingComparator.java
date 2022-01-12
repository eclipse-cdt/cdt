/*******************************************************************************
 * Copyright (c) 2007, 2012 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBindingComparator;

/**
 * {@link IIndexFragmentBindingComparator} suitable for comparing two PDOMBindings
 *
 */
public class PDOMFragmentBindingComparator implements IIndexFragmentBindingComparator {
	@Override
	public int compare(IIndexFragmentBinding a, IIndexFragmentBinding b) {
		if (a instanceof PDOMBinding && b instanceof PDOMBinding) {
			return ((PDOMBinding) a).pdomCompareTo((PDOMBinding) b);
		}
		return Integer.MIN_VALUE;
	}
}
