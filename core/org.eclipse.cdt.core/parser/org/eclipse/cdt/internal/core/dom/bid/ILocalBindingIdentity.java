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
 * An ILocalBindingIdentity instance uniquely defines a binding within a scope.
 * <p>
 * All LocalBindingIdentity instances are required to order by name as the most significant
 * component, and then by any other information. This is for indexing purposes.
 */
public interface ILocalBindingIdentity {
	/**
	 * Get the name of the binding this identity represents
	 * @return the name of the binding this identity represents
	 * @throws CoreException
	 */
	public char[] getNameCharArray() throws CoreException;
}
