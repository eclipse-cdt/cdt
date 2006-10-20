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

import org.eclipse.core.runtime.CoreException;

/**
 * An IBindingIdentity instance uniquely defines a binding within a scope. Instances
 * are provided by an IBindingIdentityFactory and used by datastructures within the
 * PDOM to order binding records.
 */
public interface ICLocalBindingIdentity extends ILocalBindingIdentity {
	/**
	 * Returns the constant associated with the coarse-grained type of the
	 * associated IBinding
	 * @return the constant associated with the coarse-grained type of the
	 * associated IBinding
	 * @throws CoreException
	 */
	public int getTypeConstant() throws CoreException;

	/**
	 * Returns a String of unspecified format which uniquely identifies this
	 * binding within its scope
	 * @return
	 * @throws CoreException
	 */
	public String getExtendedType() throws CoreException;
}
