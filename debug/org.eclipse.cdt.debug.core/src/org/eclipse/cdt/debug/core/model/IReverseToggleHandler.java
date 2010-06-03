/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.commands.IDebugCommandHandler;

/**
 * Handler interface to toggle reverse debugging
 * 
 * @since 7.0
 */
public interface IReverseToggleHandler extends IDebugCommandHandler {
	/**
	 * Method to indicate if the IElementUpdate logic should be used
	 * to update the checked state of the corresponding command.
	 * {@link isReverseToggle} will only be used if this method returns true.
	 */
	boolean toggleNeedsUpdating();

	/**
	 * Method that returns if the toggle button should show as checked or not.
	 * Only used if {@link toggleNeedsUpdating} return true.
	 *
	 * @param context The currently selected context.
	 * @return if the reverse toggle button should show as checked or not.
	 */
	boolean isReverseToggled(Object context);
}
