/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.dnd;

import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;

/**
 * TransferDropTargetListener
 */
public interface TransferDropTargetListener extends DropTargetListener {

	/**
	 * Returns the transfer used by this drop target.
	 */
	public Transfer getTransfer();

	/**
	 * Returns whether the listener is able to handle the given
	 * drop traget event.
	 *
	 * @param event the drop target event
	 *
	 * @return <code>true</code> if the listener can handle the event;
	 *  otherwise <code>false</code>
	 */
	public boolean isEnabled(DropTargetEvent event);
}
