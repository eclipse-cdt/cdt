/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.cdt.internal.ui.actions.AbstractToggleLinkingAction;

/**
 * This action toggles whether this navigator links its selection to the active
 * editor.
 * 
 */
public class ToggleLinkingAction extends AbstractToggleLinkingAction {
	
    IndexView fCView;
    
	/**
	 * Constructs a new action.
	 */
	public ToggleLinkingAction(IndexView cView) {
		fCView = cView;
		setChecked(cView.isLinking);
	}

	/**
	 * Runs the action.
	 */
	public void run() {
	    fCView.toggleLinking();
	}

}
