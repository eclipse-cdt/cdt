/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
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

/**
 * An element changed listener receives notification of changes to C elements
 * maintained by the C model.
 */
public interface IElementChangedListener {

	/**
	 * Notifies that one or more attributes of one or more C elements have changed.
	 * The specific details of the change are described by the given event.
	 *
	 * @param event the change event
	 */
	public void elementChanged(ElementChangedEvent event);
}
