/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.event;

import java.util.EventListener;

/**
 * PatternEventListener will be implemented by UIPage. When PatternEvent is
 * fired by InputUIElement due to a mismatch of input entered by the user and
 * the expected pattern of input.
 * 
 * @since 4.0
 */

public interface PatternEventListener extends EventListener {

	/**
	 * This methods is implemented by calsses handling PatternEvent.
	 * PatternEvent instance is the parameter to this method.
	 * 
	 * @param aPet
     * 
     * @since 4.0
	 */
	public void patternPerformed(PatternEvent aPet);

}
