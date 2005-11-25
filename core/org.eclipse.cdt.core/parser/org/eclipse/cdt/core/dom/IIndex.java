/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.dom;

/**
 * @author Doug Schaefer
 *
 */
public interface IIndex {

	/**
	 * Get the index reader. This is used by clients to get at the
	 * contents of the index.
	 * 
	 * @return index reader
	 */
	public IIndexReader getReader();

	/**
	 * Get the index writer. This is used by indexers to populate
	 * the index.
	 * 
	 * @return index writer
	 */
	public IIndexWriter getWriter();
}
