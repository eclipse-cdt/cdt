/*******************************************************************************
 * Copyright (c) 2001, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.dialogfields;

/**
 * Change listener used by <code>ListDialogField</code> and <code>CheckedListDialogField</code>
 */
public interface IListAdapter<T> {

	/**
	 * A button from the button bar has been pressed.
	 */
	void customButtonPressed(ListDialogField<T> field, int index);

	/**
	 * The selection of the list has changed.
	 */
	void selectionChanged(ListDialogField<T> field);

	/**
	 * En entry in the list has been double clicked
	 */
	void doubleClicked(ListDialogField<T> field);

}
