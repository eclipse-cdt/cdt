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
package org.eclipse.cdt.internal.ui.browser.cbrowsing;

import org.eclipse.cdt.internal.ui.actions.AbstractToggleLinkingAction;

/**
 * This action toggles whether this package explorer links its selection to the active
 * editor.
 * 
 * @since 2.1
 */
public class ToggleLinkingAction extends AbstractToggleLinkingAction {
	
	CBrowsingPart fCBrowsingPart;
	
	/**
	 * Constructs a new action.
	 */
	public ToggleLinkingAction(CBrowsingPart part) {
		setChecked(part.isLinkingEnabled());
		fCBrowsingPart= part;
	}

	/**
	 * Runs the action.
	 */
	public void run() {
	    fCBrowsingPart.setLinkingEnabled(isChecked());
	}

}
