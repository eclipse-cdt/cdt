/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.breakpointactions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Interface implemented by plug-ins that wish to contribute breakpoint actions.
 *
 * THIS INTERFACE IS PROVISIONAL AND WILL CHANGE IN THE FUTURE BREAKPOINT ACTION
 * CONTRIBUTIONS USING THIS INTERFACE WILL NEED TO BE REVISED TO WORK WITH
 * FUTURE VERSIONS OF CDT.
 *
 */
public interface IBreakpointAction {

	public IStatus execute(IBreakpoint breakpoint, IAdaptable context, IProgressMonitor monitor);

	public String getMemento();

	public void initializeFromMemento(String data);

	public String getDefaultName();

	public String getSummary();

	public String getTypeName();

	public String getIdentifier();

	public String getName();

	public void setName(String name);

}
