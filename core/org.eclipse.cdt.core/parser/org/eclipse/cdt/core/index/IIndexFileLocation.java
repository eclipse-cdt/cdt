/*******************************************************************************
 * Copyright (c) 2006, 2014 Symbian Software Ltd. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.index;

import java.net.URI;

/**
 * Files in the index are (conceptually) partitioned into workspace and non-workspace (external)
 * files. Clients can obtain instances of IIndexFileLocation implementations from
 * {@link IndexLocationFactory}. Two index file locations are considered equal if their URIs are
 * equal.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 4.0
 */
public interface IIndexFileLocation {
	/**
	 * Returns the URI of the indexed file (non-{@code null}).
	 */
	public URI getURI();

	/**
	 * Returns the workspace relative path of the file in the index or {@code null} if the file
	 * is not in the workspace.
	 */
	public String getFullPath();
}
