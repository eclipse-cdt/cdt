/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public interface IPDOMManager {

	// Getting the PDOM
	public IPDOM getPDOM(ICProject project) throws CoreException;
	
	// Get the indexer for a given project
	public IPDOMIndexer getIndexer(ICProject project);

	// Getting and setting indexer Ids
	public String getDefaultIndexerId();
	public void setDefaultIndexerId(String indexerId);
	
	public String getIndexerId(ICProject project) throws CoreException;
	public void setIndexerId(ICProject project, String indexerId) throws CoreException;

	// Enqueue and indexer sub job
	public void enqueue(IPDOMIndexerTask subjob);

}
