/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.dialogfields;

/**
 * Change listener used by <code>ListDialogField</code> and <code>CheckedListDialogField</code>
 */
public interface IListAdapter {
	
	/**
	 * A button from the button bar has been pressed.
	 */
	void customButtonPressed(ListDialogField field, int index);
	
	/**
	 * The selection of the list has changed.
	 */	
	void selectionChanged(ListDialogField field);
	
	/**
	 * En entry in the list has been double clicked
	 */
	void doubleClicked(ListDialogField field);	

}
