/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Doug Schaefer (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPDOMManager {
	public static final String ID_NO_INDEXER = "org.eclipse.cdt.core.nullindexer"; //$NON-NLS-1$
	public static final String ID_FAST_INDEXER = "org.eclipse.cdt.core.fastIndexer"; //$NON-NLS-1$
	/**
	 * @deprecated The full indexer has been removed.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final String ID_FULL_INDEXER = "org.eclipse.cdt.core.domsourceindexer"; //$NON-NLS-1$

	// Getting and setting indexer Ids
	public String getDefaultIndexerId();

	public void setDefaultIndexerId(String indexerId);

	public String getIndexerId(ICProject project) throws CoreException;

	public void setIndexerId(ICProject project, String indexerId) throws CoreException;
}
