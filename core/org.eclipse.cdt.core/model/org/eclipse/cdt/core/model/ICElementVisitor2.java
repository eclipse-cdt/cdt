/*******************************************************************************
 * Copyright (c) 2020 Marc-Andre Laperle.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.core.runtime.CoreException;

/**
 * This interface can be implemented by clients that walk the ICElement tree.
 * @since 7.0
 * @see ICElementVisitor
 */
public interface ICElementVisitor2 extends ICElementVisitor {

	/**
	 * Called when leaving a member in the ICElement tree.
	 */
	public void leave(ICElement element) throws CoreException;
}
