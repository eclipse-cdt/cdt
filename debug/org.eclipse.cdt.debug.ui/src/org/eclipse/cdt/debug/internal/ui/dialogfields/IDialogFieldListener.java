/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.internal.ui.dialogfields;

/**
 * Change listener used by <code>DialogField</code>
 */
public interface IDialogFieldListener {
	
	/**
	 * The dialog field has changed.
	 */
	void dialogFieldChanged(DialogField field);

}