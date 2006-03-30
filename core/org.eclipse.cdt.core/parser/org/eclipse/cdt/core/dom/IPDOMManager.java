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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public interface IPDOMManager {

	// Getting and deleting a PDOM for a project
	public IPDOM getPDOM(IProject project);
	public void deletePDOM(IProject project) throws CoreException;

	// Getting and setting indexer Ids
	public String getDefaultIndexerId();
	public void setDefaultIndexerId(String indexerId);
	
	public String getIndexerId(IProject project);
	public void setIndexerId(IProject project, String indexerId);
}
