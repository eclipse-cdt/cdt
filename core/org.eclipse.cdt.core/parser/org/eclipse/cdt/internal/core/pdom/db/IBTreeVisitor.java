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
package org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 * 
 * The visitor walks through the tree until the compare returns
 * >= 0, i.e. we reach the first record that meets the criteria.
 * 
 * It then continues until the visit returns false.
 * 
 */
public interface IBTreeVisitor {

	/**
	 * Compare the record against an internally held key.
	 * Used for visiting.
	 * 
	 * @param record
	 * @return -1 if record < key, 0 if record == key, 1 if record > key
	 * @throws IOException
	 */
	public abstract int compare(int record) throws CoreException;

	/**
	 * Visit a given record and return whether to continue or not.
	 * 
	 * @param record
	 * @return
	 * @throws IOException
	 */
	public abstract boolean visit(int record) throws CoreException;
	
}
