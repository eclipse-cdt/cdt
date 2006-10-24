/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/ 

package org.eclipse.cdt.core.index;

import org.eclipse.cdt.core.dom.ILinkage;

/**
 * Can be subclassed and used for queries in the index.
 * @since 4.0
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 * </p>
 */

public class IndexFilter {
	public static final IndexFilter ALL = new IndexFilter();

	/**
	 * Returns whether or not to include objects of the given linkage in the query.
	 * @see IIndex#findBindings(java.util.regex.Pattern, boolean, IndexFilter, org.eclipse.core.runtime.IProgressMonitor)
	 * @param linkage a linkage to be tested
	 * @return whether to include objects of the given linkage in the query.
	 */
	public boolean acceptLinkage(ILinkage linkage) {
		return true;
	}
	
	/**
	 * Get an IndexFilter that filters out bindings from linkages other than that
	 * specified
	 * @param target the linkage whose bindings should be retained
	 * @return an IndexFilter instance
	 */
	public static IndexFilter getFilter(final ILinkage target) {
		return new IndexFilter() {
			public boolean acceptLinkage(ILinkage linkage) {
				return linkage.getID() == target.getID();
			}
		};
	}
}
