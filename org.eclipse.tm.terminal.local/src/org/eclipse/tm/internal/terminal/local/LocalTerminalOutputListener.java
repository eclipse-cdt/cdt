/***************************************************************************************************
 * Copyright (c) 2008 Mirko Raner.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - initial implementation for Eclipse Bug 196337
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local;

import java.io.PrintStream;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;

/**
 * The class {@link LocalTerminalOutputListener} is an {@link IStreamListener} that transfers output
 * from a program's standard output and standard error streams to an {@link ITerminalControl}.
 * It does so by listening for appended text and sending it to the terminal's
 * {@link ITerminalControl#getRemoteToTerminalOutputStream()}. The class also performs line
 * separator conversions as specified by the {@link ILocalTerminalSettings}.
 *
 * @author Mirko Raner
 * @version $Revision: 1.2 $
 */
public class LocalTerminalOutputListener implements IStreamListener {

	private PrintStream printStream;
	private String lineSeparator;

	/**
	 * Creates a new {@link LocalTerminalOutputListener}.
	 *
	 * @param control the {@link ITerminalControl} to which the received output is forwarded
	 * @param settings the {@link ILocalTerminalSettings}
	 */
	public LocalTerminalOutputListener(ITerminalControl control, ILocalTerminalSettings settings) {

		printStream = new PrintStream(control.getRemoteToTerminalOutputStream(), true);
		lineSeparator = LocalTerminalUtilities.getLineSeparator(settings);
		if (lineSeparator == null) {

			String defaultLS = System.getProperty(LocalTerminalUtilities.LINE_SEPARATOR_PROPERTY);
			if (LocalTerminalUtilities.CRLF.equals(defaultLS)) {

				lineSeparator = ILocalTerminalSettings.LINE_SEPARATOR_CRLF;
			}
			else if (LocalTerminalUtilities.LF.equals(defaultLS)) {

				lineSeparator = ILocalTerminalSettings.LINE_SEPARATOR_LF;
			}
			else if (LocalTerminalUtilities.CR.equals(defaultLS)) {

				lineSeparator = ILocalTerminalSettings.LINE_SEPARATOR_CR;
			}
			else {

				Logger.log("Unknown default line separator: " + defaultLS); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Processes new output that was appended to the intercepted stream.
	 *
	 * @param text the new output
	 * @param monitor the {@link IStreamMonitor} from which the output was received (this parameter
	 * is currently not evaluated because each {@link IStreamMonitor} has its own dedicated instance
	 * of {@link LocalTerminalOutputListener} attached)
	 */
	public void streamAppended(String text, IStreamMonitor monitor) {

		// The VT100TerminalControl apparently adheres to a strict interpretation of the CR and
		// LF control codes, i.e., CR moves the caret to the beginning of the line (but does not
		// move down to the next line), and LF moves down to the next line (but not to the
		// beginning of the line). Therefore, if the program launched in the terminal does not use
		// CRLF as its line terminator the line terminators have to be converted to CRLF before
		// being passed on to the terminal control:
		//
		if (!ILocalTerminalSettings.LINE_SEPARATOR_CRLF.equals(lineSeparator)) {

			text = text.replaceAll(lineSeparator, LocalTerminalUtilities.CRLF);
		}
		printStream.print(text);
	}
}
