/*******************************************************************************
 * Copyright (c) 2005, 2012 QNX Software Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.db;

import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
public interface IBTreeComparator {
	/**
	 * Compare two records. Used for insert.
	 */
	public abstract int compare(long record1, long record2) throws CoreException;
}
