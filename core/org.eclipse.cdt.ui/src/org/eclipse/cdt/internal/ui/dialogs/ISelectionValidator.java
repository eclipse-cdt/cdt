package org.eclipse.cdt.internal.ui.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface ISelectionValidator {
	void isValid(Object[] selection, StatusInfo res);
}
