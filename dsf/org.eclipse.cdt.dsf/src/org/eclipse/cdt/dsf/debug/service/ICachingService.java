/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

import org.eclipse.cdt.dsf.datamodel.IDMContext;

/**
 * Interface for services which use an internal cache for data.
 *
 * @since 1.1
 */
public interface ICachingService {

	/**
	 * Clears the service cache entries which have the given context in their
	 * hierarchy.
	 * @param context Root context to flush.  May be <code>null</code> to flush
	 * the entire cache.
	 */
	public void flushCache(IDMContext context);
}
