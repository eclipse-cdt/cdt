/*******************************************************************************
 * Copyright (c) 2002, 2009 QNX Software Systems and others.
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

package org.eclipse.cdt.core.model;

import org.eclipse.core.runtime.IPath;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IIncludeReference extends IParent, ICElement {

	/**
	 * Returns the pathEntry
	 */
	IIncludeEntry getIncludeEntry();

	/**
	 * Return the affected path
	 */
	IPath getAffectedPath();

	/**
	 * Return true if the path is on the include path Entry
	 */
	boolean isOnIncludeEntry(IPath path);

}
