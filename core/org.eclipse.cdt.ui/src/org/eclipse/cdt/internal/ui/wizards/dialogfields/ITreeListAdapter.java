/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.dialogfields;

import org.eclipse.swt.events.KeyEvent;

/**
 * Change listener used by <code>TreeListDialogField</code>
 */
public interface ITreeListAdapter {
	
	/**
	 * A button from the button bar has been pressed.
	 */
	void customButtonPressed(TreeListDialogField field, int index);
	
	/**
	 * The selection of the list has changed.
	 */	
	void selectionChanged(TreeListDialogField field);

	/**
	 * The list has been double clicked
	 */
	void doubleClicked(TreeListDialogField field);

	/**
	 * A key has been pressed
	 */
	void keyPressed(TreeListDialogField field, KeyEvent event);

	Object[] getChildren(TreeListDialogField field, Object element);

	Object getParent(TreeListDialogField field, Object element);

	boolean hasChildren(TreeListDialogField field, Object element);

}
