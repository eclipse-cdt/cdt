/*******************************************************************************
 * Copyright (c) 2006 Symbian Software and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.bid;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * A comparator for ILocalBindingIdentity objects
 */
public interface ILocalBindingIdentityComparator {
	/**
	 * 
	 * @param a
	 * @param b
	 * @return -1 if a&lt;b, 0 if a==b and 1 if a&gt;b
	 * @throws CoreException
	 */
	public int compare(IBinding a, IBinding b) throws CoreException;
	/**
	 * 
	 * @param a
	 * @param b
	 * @return -1 if a&lt;b, 0 if a==b and 1 if a&gt;b
	 * @throws CoreException
	 */
	public int compare(ILocalBindingIdentity a, IBinding b) throws CoreException;
	/**
	 * 
	 * @param a
	 * @param b
	 * @return -1 if a&lt;b, 0 if a==b and 1 if a&gt;b
	 * @throws CoreException
	 */
	public int compare(IBinding a, ILocalBindingIdentity b) throws CoreException;
	/**
	 * 
	 * @param a
	 * @param b
	 * @return -1 if a&lt;b, 0 if a==b and 1 if a&gt;b
	 * @throws CoreException
	 */
	public int compare(ILocalBindingIdentity a, ILocalBindingIdentity b) throws CoreException;
}
