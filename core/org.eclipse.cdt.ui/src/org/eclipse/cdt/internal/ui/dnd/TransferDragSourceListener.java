/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dnd;

import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;

/**
 * A special drag source listener which is typed with a <code>TransferData</code>.
 */
public interface TransferDragSourceListener extends DragSourceListener {
	/**
	 * Returns the transfer used by this drag source.
	 */
	public Transfer getTransfer();
}
