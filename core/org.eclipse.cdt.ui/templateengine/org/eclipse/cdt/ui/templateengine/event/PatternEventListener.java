/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
