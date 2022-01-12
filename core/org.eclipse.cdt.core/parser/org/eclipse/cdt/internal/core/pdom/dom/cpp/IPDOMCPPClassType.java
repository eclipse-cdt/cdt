/*******************************************************************************
 * Copyright (c) 2008, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface that allows to implement a class-scope.
 */
public interface IPDOMCPPClassType extends ICPPClassType, IPDOMBinding, IIndexType {
	/**
	 * Visits the children of the class type without using the cache. This method is
	 * used to populate the cache.
	 */
	void acceptUncached(IPDOMVisitor visitor) throws CoreException;

	/**
	 * Returns the scope name, for use in {@link IScope#getScopeName()}
	 */
	IIndexName getScopeName();

	/**
	 * Sets the visibility specifier of a given member.
	 * @param member The binding specifying the member.
	 * @param visibility The visibility of the <code>member</code>.
	 */
	void addMember(PDOMNode member, int visibility) throws CoreException;

	/**
	 * Returns true if this class type is visible to ADL only.
	 * A class type is visible to ADL only if it's only declaration so far
	 * is a friend declaration inside another class.
	 */
	boolean isVisibleToAdlOnly();

	/**
	 * Set whether this class type is visible to ADL only.
	 * See isVisibleToAdlOnly().
	 */
	void setVisibleToAdlOnly(boolean visibleToAdlOnly) throws CoreException;
}
