/*******************************************************************************
 * Copyright (c) 2007, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.control;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;

/**
 * Interface to create a command input control.
 *
 */
public interface ICommandInputField {
	/**
	 * @param parent
	 * @param terminal
	 */
	void createControl(Composite parent, ITerminalViewControl terminal);

	void dispose();

	/**
	 * Sets the font of a control created with {@link #createControl(Composite, ITerminalViewControl)}
	 * @param control
	 * @param font the new text font
	 */
	void setFont(Font font);

}