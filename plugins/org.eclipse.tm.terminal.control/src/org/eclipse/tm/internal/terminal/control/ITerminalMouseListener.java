/*******************************************************************************
 * Copyright (c) 2015 CWI. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Davy Landman (CWI) - [475267][api] Initial definition of interface
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.control;

import org.eclipse.tm.terminal.model.ITerminalTextDataReadOnly;

/**
 * Terminal specific version of {@link org.eclipse.swt.events.MouseListener}
 */
public interface ITerminalMouseListener  {
	/**
	 * Invoked when a double-click has happend inside the terminal control.<br>
	 * <br>
	 * <strong>Important:</strong> the event fires for every click, even outside the text region.
	 * @param terminalText a read-only view of the current terminal text
	 * @param button see {@link org.eclipse.swt.events.MouseEvent#button} for the meaning of the button values
	 */
	void mouseDoubleClick(ITerminalTextDataReadOnly terminalText, int line, int column, int button);
	/**
	 * Invoked when a mouse button is pushed down inside the terminal control.<br>
	 * <br>
	 * <strong>Important:</strong> the event fires for every mouse down, even outside the text region.
	 * @param terminalText a read-only view of the current terminal text
	 * @param button see {@link org.eclipse.swt.events.MouseEvent#button} for the meaning of the button values
	 */
	void mouseDown(ITerminalTextDataReadOnly terminalText, int line, int column, int button);
	/**
	 * Invoked when a mouse button is released inside the terminal control.<br>
	 * <br>
	 * <strong>Important:</strong> the event fires for every mouse up, even outside the text region.
	 * @param terminalText a read-only view of the current terminal text
	 * @param button see {@link org.eclipse.swt.events.MouseEvent#button} for the meaning of the button values
	 */
	void mouseUp(ITerminalTextDataReadOnly terminalText, int line, int column, int button);
}
