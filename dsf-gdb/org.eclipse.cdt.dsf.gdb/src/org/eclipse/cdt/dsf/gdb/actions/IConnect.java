/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.actions;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;

/**
 * @since 1.1
 */
public interface IConnect {
	/**
	 * Returns whether this element can currently attempt to 
	 * connect to a new process.
	 */
	public boolean canConnect();

	/**
	 * Causes this element to attempt to connect to a new process.
	 */
	public void connect(RequestMonitor rm);
}
