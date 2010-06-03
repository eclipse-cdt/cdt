/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

public interface IIndexFragmentBindingComparator {
	/**
	 * Compares to index fragment bindings, or returns {@link Integer#MIN_VALUE} if the comparator is
	 * not capable of comparing the two objects because of their run-time type.
	 * <p>
	 * Compares two index fragment bindings using the following scheme:
	 * <ul>
	 * <li>Compares the fully qualified names, by pair-wise lexicographic comparison
	 * of individual name components, starting with the innermost scoped name. If
	 * all pair-wise comparisons are equal, then the comparison routine continues, otherwise returning
	 * <ul>
	 * <li> -1 if the first differing component name of <em>a</em> was &lt the pairwise equivalent from <em>b</em>
	 * <li> 1 if the first differing component name of <em>a</em> was &gt the pairwise equivalent from <em>b</em>
	 * </ul> In case binding <em>a</em> has fewer component names than binding <em>b</em> then
	 * -1 is returned, otherwise 1 is returned. 
	 * <li>Compares a course-grained binding type dependent on language. For C/C++ the
	 * type comparison is performed by comparing the bindings associated constant as defined in
	 * IIndexCNodeConstants or IIndexCPPNodeConstants. If these are equal comparison continues
	 * <li>Compares fine grained binding information dependent on the binding language. For C++ the
	 * type comparison is relevant for binding types that are further differentiated by type signatures via
	 * {@link IndexCPPSignatureUtil}
	 * </ul>
	 * @param a a non null {@link IIndexFragmentBinding}
	 * @param b a non null {@link IIndexFragmentBinding}
	 * @return -1, 0, 1 or Integer.MIN_VALUE if this comparator cannot compare the two
	 */
	public int compare(IIndexFragmentBinding a, IIndexFragmentBinding b);
}
