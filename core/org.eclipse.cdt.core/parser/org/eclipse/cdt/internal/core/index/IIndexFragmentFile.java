/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.core.runtime.CoreException;

public interface IIndexFragmentFile extends IIndexFile {

	/**
	 * Returns the fragment that owns this file.
	 */
	IIndexFragment getIndexFragment();

	/**
	 * Sets the timestamp of the file
	 * @throws CoreException 
	 */
	void setTimestamp(long timestamp) throws CoreException;

	/**
	 * Returns whether any names are associated with this file object
	 * in this fragment - i.e. whether this file contains content in its
	 * associated fragment
	 * @return whether any names are associated with this file object
	 * in this fragment
	 */
	boolean hasNames() throws CoreException;
}
