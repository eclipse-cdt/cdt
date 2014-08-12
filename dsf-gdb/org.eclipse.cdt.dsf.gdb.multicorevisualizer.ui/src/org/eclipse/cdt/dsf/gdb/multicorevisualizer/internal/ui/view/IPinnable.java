/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation (Bug 441713)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

/** Interface for pinnable objects */
public interface IPinnable {

	/**
	 * Pins to the current context
	 */
	public void pin();
	
	/**
	 * Unpins
	 */
	public void unpin();
	
	/** Returns whether currently pinned */
	public boolean isPinned();
}
