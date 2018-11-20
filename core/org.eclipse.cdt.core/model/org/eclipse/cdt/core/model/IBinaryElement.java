/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.cdt.core.IAddress;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBinaryElement extends ICElement {

	/**
	 * Returns the address of the function. This method will return,
	 * the address of a symbol for children of IBinaryObject.
	 *
	 * @exception CModelException if this element does not have address
	 * information.
	 */
	IAddress getAddress() throws CModelException;

	/**
	 * Returns the binary object the element belongs to.
	 *
	 */
	IBinary getBinary();
}
