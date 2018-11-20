/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
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
import org.eclipse.cdt.dsf.service.IDsfService;

/**
 * Place holder interface for a signals implementation.
 *
 * @since 1.0
 */
public interface ISignals extends IDsfService {
	/**
	 * Marker interface for a context for which signals can be set.
	 */
	public interface ISignalsDMContext extends IDMContext {
	}

}
