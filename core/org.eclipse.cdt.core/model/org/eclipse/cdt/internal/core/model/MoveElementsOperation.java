/*******************************************************************************
 * Copyright (c) 2002, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;

/**
 * MoveElementsOperation
 */
public class MoveElementsOperation extends CopyElementsOperation {
	/**
	 * When executed, this operation will move the given elements to the given containers.
	 */
	public MoveElementsOperation(ICElement[] elementsToMove, ICElement[] destContainers, boolean force) {
		super(elementsToMove, destContainers, force);
	}

	/**
	 * Returns the <code>String</code> to use as the main task name
	 * for progress monitoring.
	 */
	@Override
	protected String getMainTaskName() {
		return CoreModelMessages.getString("operation.moveElementProgress"); //$NON-NLS-1$
	}

	/**
	 * @see CopyElementsOperation#isMove()
	 */
	@Override
	protected boolean isMove() {
		return true;
	}

}
