/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;

/**
 * This operation renames resources (Package fragments and compilation units).
 *
 * <p>Notes:<ul>
 * <li>When a compilation unit is renamed, its main type and the constructors of the
 * 		main type are renamed.
 * </ul>
 */
public class RenameResourceElementsOperation extends MoveResourceElementsOperation {
	/**
	 * When executed, this operation will rename the specified elements with the given names in the
	 * corresponding destinations.
	 */
	public RenameResourceElementsOperation(ICElement[] elements, ICElement[] destinations, String[] newNames,
			boolean force) {
		//a rename is a move to the same parent with a new name specified
		//these elements are from different parents
		super(elements, destinations, force);
		setRenamings(newNames);
	}

	/**
	 * @see MultiOperation
	 */
	@Override
	protected String getMainTaskName() {
		return CoreModelMessages.getString("operation.renameResourceProgress"); //$NON-NLS-1$
	}

	/**
	 * @see CopyResourceElementsOperation#isRename()
	 */
	@Override
	protected boolean isRename() {
		return true;
	}

	/**
	 * @see MultiOperation
	 */
	@Override
	protected void verify(ICElement element) throws CModelException {
		super.verify(element);
		verifyRenaming(element);
	}
}
