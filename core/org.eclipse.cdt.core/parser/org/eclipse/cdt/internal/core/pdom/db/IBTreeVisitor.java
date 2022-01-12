/*******************************************************************************
 * Copyright (c) 2005, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
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
	 * @param record the offset of the record to compare with the key
	 * @return -1 if record < key, 0 if record == key, 1 if record > key
	 */
	public int compare(long record) throws CoreException;

	/**
	 * Visits a given record and returns whether to continue or not.
	 *
	 * @param record the offset of the record being visited
	 * @return {@code true} to continue the visit, {@code false} to abort it.
	 */
	public boolean visit(long record) throws CoreException;

	/**
	 * Called before visiting a record.
	 *
	 * @param record the offset of the record being visited
	 */
	public default void preVisit(long record) throws CoreException {
	}

	/**
	 * Called after visiting a record.
	 *
	 * @param record the offset of the record being visited
	 */
	public default void postVisit(long record) throws CoreException {
	}
}
