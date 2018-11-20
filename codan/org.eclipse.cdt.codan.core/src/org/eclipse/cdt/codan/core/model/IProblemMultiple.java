/*******************************************************************************
 * Copyright (c) 2009,2011 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

/**
 * Additional interface to the problem kind to quiry either it supports multiple
 * instances or not
 *
 * @since 2.0
 */
public interface IProblemMultiple {
	/**
	 *
	 * @return true if problem can be replicated by the user, i.e. multiple is
	 *         true in the extension
	 */
	public boolean isMultiple();

	/**
	 * @return true if this is original problem, false if it replica
	 */
	public boolean isOriginal();
}
