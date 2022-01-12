/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.sourcelookup;

import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;

/**
* A source lookup change listener is notified of changes in the source lookup path.
*/
public interface ISourceLookupChangeListener {

	/**
	 * Notification that the source lookup containers have changed.
	 */
	public void sourceContainersChanged(ISourceLookupDirector director);
}
