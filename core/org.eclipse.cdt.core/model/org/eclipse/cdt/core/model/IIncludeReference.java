/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model;

import org.eclipse.core.runtime.IPath;

/**
 * IIncludeReference
 */
public interface IIncludeReference extends IParent, ICElement {

	/**
	 * Returns the pathEntry
	 * 
	 * @return
	 */
	IIncludeEntry getIncludeEntry();

	/**
	 * Return the affected path
	 * @return
	 */
	IPath getAffectedPath();

	/**
	 * Return true if the path is on the include path Entry
	 * 
	 * @param path
	 * @return
	 */
	boolean isOnIncludeEntry(IPath path);

}
