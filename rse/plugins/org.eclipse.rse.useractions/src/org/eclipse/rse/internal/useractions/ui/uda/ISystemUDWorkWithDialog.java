package org.eclipse.rse.internal.useractions.ui.uda;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.eclipse.rse.ui.messages.ISystemMessageLine;

/**
 * A common interface that the action, and types, edit panes for user actions
 * implement.
 */
public interface ISystemUDWorkWithDialog {
	/**
	 * Decide if we can do the delete or not.
	 * Will decide the enabled state of the delete action.
	 */
	public boolean canDelete(Object selectedObject);

	/**
	 * Decide if we can do the move up or not.
	 * Will decide the enabled state of the move up action.
	 */
	public boolean canMoveUp(Object selectedObject);

	/**
	 * Decide if we can do the move down or not.
	 * Will decide the enabled state of the move down action.
	 */
	public boolean canMoveDown(Object selectedObject);

	/**
	 * Decide if we can do the copy or not.
	 * Will decide the enabled state of the copy action.
	 */
	public boolean canCopy(Object selectedObject);

	/**
	 * Return the message line 
	 */
	public ISystemMessageLine getMessageLine();

	/**
	 * Return true if changes are pending in the edit pane
	 */
	public boolean areChangesPending();

	/**
	 * Process the apply button
	 */
	public void processApply();

	/**
	 * Process the revert button
	 */
	public void processRevert();
}
