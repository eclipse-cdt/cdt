/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.resources;

import java.util.EventListener;

/**
 * An interface to be implemented by objects interested in path variable
 * creation, removal and value change events.
 *
 * <p>Clients may implement this interface.</p>
 *
 * @since 3.0
 */
public interface IPathEntryVariableChangeListener extends EventListener {
	/**
	 * Notification that a path variable has changed.
	 * <p>
	 * This method is called when a path variable is added, removed or has its value
	 * changed in the observed <code>IPathVariableManager</code> object.
	 * </p>
	 *
	 * @param event the path variable change event object describing which variable
	 *    changed and how
	 * @see IPathEntryVariableManager#addChangeListener(IPathEntryVariableChangeListener)
	 * @see IPathEntryVariableManager#removeChangeListener(IPathEntryVariableChangeListener)
	 * @see PathEntryVariableChangeEvent
	 */
	public void pathVariableChanged(PathEntryVariableChangeEvent event);

}
