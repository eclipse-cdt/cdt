/*******************************************************************************
 * Copyright (c) 2008, 2011 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;

/**
 * An container context object.  In the GDB/MI protocol, thread groups
 * are used as containers of threads, and are represented by a string
 * identifier.  These thread groups are the basis for this context.
 * @since 1.1
 */
public interface IMIContainerDMContext extends IContainerDMContext {
	/**
	 * Returns the GDB/MI thread group identifier of this context.
	 */
	public String getGroupId();
}
