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

package org.eclipse.cdt.internal.ui.cview;

/**
 * This action toggles whether this navigator links its selection to the active
 * editor.
 * 
 * @since 2.0
 */
public class ToggleLinkingAction extends CViewAction {

	/**
	 * Constructs a new action.
	 */
	public ToggleLinkingAction(CView cview, String label) {
		super(cview, label);
		setChecked(cview.isLinkingEnabled());
	}

	/**
	 * Runs the action.
	 */
	public void run() {
		getCView().setLinkingEnabled(isChecked());
	}

}
