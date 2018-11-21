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
package org.eclipse.cdt.dsf.ui.viewmodel.datamodel;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;

/**
 * Interface for a view model context based on a DSF data model context.
 *
 * @since 1.0
 */
public interface IDMVMContext extends IVMContext {

	public static Object REFRESH_EVENT = new Object();

	/**
	 * returns the data model context that this view model context wraps.
	 */
	public IDMContext getDMContext();
}
