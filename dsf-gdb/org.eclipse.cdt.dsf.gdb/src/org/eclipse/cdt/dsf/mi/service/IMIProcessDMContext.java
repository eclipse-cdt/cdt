/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ercisson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;

/**
 * A process context object.  In the GDB/MI protocol, processes are represented
 * by an string identifier, which is the basis for this context.
 * @since 1.1
 */
public interface IMIProcessDMContext extends IProcessDMContext {
	/**
	 * Returns the GDB/MI process identifier of this context.
	 * @return
	 */
	public String getProcId();
}
