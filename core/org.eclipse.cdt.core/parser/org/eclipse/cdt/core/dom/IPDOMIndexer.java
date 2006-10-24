/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;


/**
 * @author Doug Schaefer
 *
 */
public interface IPDOMIndexer {

	public void setProject(ICProject project);
	public ICProject getProject();
	
	public void handleDelta(ICElementDelta delta) throws CoreException;
	
	public void reindex() throws CoreException;
	
	/**
	 * Return the unique ID of type of this indexer
	 * @return the unique ID of type of this indexer
	 */
	public String getID();
	
	/**
	 * Returns whether to index headers that are not actually included by any source.
	 * @since 4.0
	 */
	public boolean getIndexAllHeaders();
	
	/**
	 * Clients are not allowed to call this method, it is called by the framework. 
	 * @since 4.0
	 */
	public void setIndexAllHeaders(boolean value);
}
