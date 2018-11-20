/*******************************************************************************
 * Copyright (c) 2008 ARM Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.disassembly;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;

/**
 * A context sensitive document update request.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 *
 * Use the element path instead of this interface?
 *
 * This interface is experimental
 */
public interface IDocumentUpdate extends IViewerUpdate {

	/**
	 * Returns the root element associated with this request.
	 *
	 * @return the root element
	 */
	public Object getRootElement();

	/**
	 * Returns the base element associated with this request.
	 *
	 * @return the base element
	 */
	public Object getBaseElement();
}
