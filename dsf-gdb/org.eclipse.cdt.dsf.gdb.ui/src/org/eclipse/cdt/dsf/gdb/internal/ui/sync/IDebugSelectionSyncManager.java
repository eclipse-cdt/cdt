/*******************************************************************************
 * Copyright (c) 2016 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.sync;

import org.eclipse.debug.ui.contexts.IDebugContextListener;

/**
 * An Interface that extends IDebugContextListener in order to synchronize the selection of 
 * Debug elements contexts and propagate this selection to the back end debugger i.e. GDB
 * 
 * @since 5.1
 */
public interface IDebugSelectionSyncManager extends IDebugContextListener {

	/**
	 * An initialization step that allows the instance to e.g. register as a debug context listener
	 */
	public void startup();
	
	/**
	 * A shutdown indication to allow the instance to e.g. de-register as a debug context listener
	 */
	public void shutdown();
}
