/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Doug Schaefer
 *
 */
public interface IPDOMManager {
	public static final String ID_NO_INDEXER= "org.eclipse.cdt.core.nullindexer"; //$NON-NLS-1$
	public static final String ID_FAST_INDEXER= "org.eclipse.cdt.core.fastIndexer"; //$NON-NLS-1$
	public static final String ID_FULL_INDEXER= "org.eclipse.cdt.core.domsourceindexer"; //$NON-NLS-1$

	/**
	 * Clears the entire index of the project and schedules the indexer.
	 * @throws CoreException
	 * @since 4.0
	 */
	public void reindex(ICProject project) throws CoreException;

	/**
	 * Export index for usage within a team.
	 * @param project a project for which the pdom is to be exported.
	 * @param location the target location for the database.
	 * @param options currently none are supported.
	 * @throws CoreException
	 * @since 4.0
	 */
	public void export(ICProject project, String location, int options, IProgressMonitor monitor) throws CoreException;

	// Getting and setting indexer Ids
	public String getDefaultIndexerId();
	public void setDefaultIndexerId(String indexerId); 
	
	public String getIndexerId(ICProject project) throws CoreException;
	public void setIndexerId(ICProject project, String indexerId) throws CoreException;
}
