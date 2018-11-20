/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.core.runtime.IAdaptable;

/**
 * View model element which is stored as the data object of nodes in the viewer.
 * The implementation of this interface is usually a wrapper object for an object
 * from some data model, which is then used to correctly implement the
 * {@link #equals(Object)} and {@link #hashCode()} methods of this wrapper.
 *
 * @since 1.0
 */
@Immutable
public interface IVMContext extends IAdaptable {

	/**
	 * Returns the view model node that originated this element.
	 */
	public IVMNode getVMNode();
}
