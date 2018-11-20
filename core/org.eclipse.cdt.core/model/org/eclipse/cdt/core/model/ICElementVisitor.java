/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.core.runtime.CoreException;

/**
 * This interface is implemented by clients that walk the ICElement tree.
 */
public interface ICElementVisitor {

	/**
	 * Visited a member if the ICElement tree. Returns whether to visit the children
	 * of this element.
	 */
	public boolean visit(ICElement element) throws CoreException;

}
