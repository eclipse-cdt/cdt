/*******************************************************************************
 * Copyright (c) 2005, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 * 
 * The visitor visits all records where compare returns 0. 
 */
public interface IBTreeVisitor {
	/**
	 * Compares the record against an internally held key. The comparison must be
	 * compatible with the one used for the B-tree.
	 * Used for visiting.
	 * 
	 * @param record
	 * @return -1 if record < key, 0 if record == key, 1 if record > key
	 */
	public int compare(long record) throws CoreException;

	/**
	 * Visits a given record and returns whether to continue or not.
	 *
	 * @return {@code true} to continue the visit, {@code false} to abort it.
	 */
	public boolean visit(long record) throws CoreException;	

	/**
	 * Called before visiting a node.
	 *
	 * @param record the node being visited
	 */
	public default void preNode(long record) throws CoreException {}

	/**
	 * Called after visiting a node.
	 *
	 * @param record the node being visited
	 */
	public default void postNode(long record) throws CoreException {}
}
