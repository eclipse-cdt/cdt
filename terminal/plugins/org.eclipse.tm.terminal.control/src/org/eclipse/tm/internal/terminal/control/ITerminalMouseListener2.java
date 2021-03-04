/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.control;

import org.eclipse.tm.terminal.model.ITerminalTextDataReadOnly;

/**
 * Extension of {@link ITerminalMouseListener} for consumers that need the stateMask for a button mouse action.
 *
 * If ITerminalMouseListener2 is used, the methods in ITerminalMouseListener will not be called.
 *
 * @since 5.2
 * @see ITerminalMouseListener
 */
public interface ITerminalMouseListener2 extends ITerminalMouseListener {
	/**
	 * Invoked when a double-click has happend inside the terminal control.<br>
	 * <br>
	 * <strong>Important:</strong> the event fires for every click, even outside the text region.
	 * @param terminalText a read-only view of the current terminal text
	 * @param button see {@link org.eclipse.swt.events.MouseEvent#button} for the meaning of the button values
	 * @param stateMask see {@link org.eclipse.swt.events.MouseEvent#stateMask} for the meaning of the values
	 */
	default void mouseDoubleClick(ITerminalTextDataReadOnly terminalText, int line, int column, int button,
			int stateMask) {
		// do nothing by default so that implementors only need to implement methods they care about
	}

	@Override
	default void mouseDoubleClick(ITerminalTextDataReadOnly terminalText, int line, int column, int button) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Invoked when a mouse button is pushed down inside the terminal control.<br>
	 * <br>
	 * <strong>Important:</strong> the event fires for every mouse down, even outside the text region.
	 * @param terminalText a read-only view of the current terminal text
	 * @param button see {@link org.eclipse.swt.events.MouseEvent#button} for the meaning of the button values
	 * @param stateMask see {@link org.eclipse.swt.events.MouseEvent#stateMask} for the meaning of the values
	 */
	default void mouseDown(ITerminalTextDataReadOnly terminalText, int line, int column, int button, int stateMask) {
		// do nothing by default so that implementors only need to implement methods they care about
	}

	@Override
	default void mouseDown(ITerminalTextDataReadOnly terminalText, int line, int column, int button) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Invoked when a mouse button is released inside the terminal control.<br>
	 * <br>
	 * <strong>Important:</strong> the event fires for every mouse up, even outside the text region.
	 * @param terminalText a read-only view of the current terminal text
	 * @param button see {@link org.eclipse.swt.events.MouseEvent#button} for the meaning of the button values
	 * @param stateMask see {@link org.eclipse.swt.events.MouseEvent#stateMask} for the meaning of the values
	 */
	default void mouseUp(ITerminalTextDataReadOnly terminalText, int line, int column, int button, int stateMask) {
		// do nothing by default so that implementors only need to implement methods they care about
	}

	@Override
	default void mouseUp(ITerminalTextDataReadOnly terminalText, int line, int column, int button) {
		throw new UnsupportedOperationException();
	}

}
