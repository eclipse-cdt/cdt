/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
