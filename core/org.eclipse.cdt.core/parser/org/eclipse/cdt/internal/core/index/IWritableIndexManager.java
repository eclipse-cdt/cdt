/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;

public interface IWritableIndexManager extends IIndexManager {
	/**
	 * Returns a writable index or <code>null</code> if the project does not exist or is not yet
	 * registered with the pdom manager.
	 */
	IWritableIndex getWritableIndex(ICProject project) throws CoreException;
}
