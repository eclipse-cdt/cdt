/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Binding comparator suitable for C/C++ across index implementations. This will not be used
 * unless we ever have non-PDOM implementations of IIndexFragment, and in that case we may find
 * this implementation is too slow.
 */
public class DefaultFragmentBindingComparator implements IIndexFragmentBindingComparator {
	@Override
	public int compare(IIndexFragmentBinding a, IIndexFragmentBinding b) {
		int cmp= compareQualifiedNames(CPPVisitor.getQualifiedName(a), CPPVisitor.getQualifiedName(b));
		if (cmp == 0) {
			int ac= a.getBindingConstant(), bc= b.getBindingConstant();
			cmp= ac < bc ? -1 : (ac > bc ? 1 : 0);
			if (cmp == 0) {
				cmp= IndexCPPSignatureUtil.compareSignatures(a, b);
			}
		}
		return cmp;
	}
	
	private int compareQualifiedNames(String[] a, String[] b) {
		if (a.length < b.length)
			return -1;
		if (a.length > b.length)
			return 1;
		for (int i= 0; i < a.length; i++) {
			int cmp= a[i].compareTo(b[i]);
			if (cmp != 0)
				return cmp;
		}
		return 0;
	}
}
