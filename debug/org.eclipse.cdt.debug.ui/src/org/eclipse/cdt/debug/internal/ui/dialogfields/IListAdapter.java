/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.internal.ui.dialogfields;

/**
 * Change listener used by <code>ListDialogField</code> and <code>CheckedListDialogField</code>
 */
public interface IListAdapter {
	
	/**
	 * A button from the button bar has been pressed.
	 */
	void customButtonPressed(DialogField field, int index);
	
	/**
	 * The selection of the list has changed.
	 */	
	void selectionChanged(DialogField field);

}