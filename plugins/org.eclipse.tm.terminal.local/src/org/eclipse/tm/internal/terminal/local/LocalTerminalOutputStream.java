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

import java.io.IOException;
import java.io.OutputStream;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.tm.internal.terminal.local.process.LocalTerminalProcess;

/**
 * The class {@link LocalTerminalOutputStream} is an {@link OutputStream} that copies data that is
 * typed into the terminal to the standard input of the active process. Data that is written to the
 * stream is directly forwarded to the {@link IStreamsProxy} of the process. CRLF line separators
 * that are received from the terminal will be automatically converted to the line separator that is
 * specified in the {@link ILocalTerminalSettings}. The Terminal Control generally sends CR line
 * separators if the local echo is disabled and CRLF if enabled. The reason for this idiosyncrasy
 * is not entirely clear right now and the line separator behavior might change in the future.
 *
 * TODO: research as to whether the CR/CRLF distinction in VT100TerminalControl.TerminalKeyHandler
 *       (based on the local echo setting) is really necessary
 *
 * @author Mirko Raner
 * @version $Revision: 1.4 $
 */
public class LocalTerminalOutputStream extends OutputStream {

	private final static String NOTHING = ""; //$NON-NLS-1$
	private final static String CRLF = LocalTerminalUtilities.CRLF;
	private final static char CR = '\r';
	private final static char LF = '\n';
	private final static char CTRL_C = '\03';
	private final static int TERMINAL_SENDS_CR = 0;
	private final static int TERMINAL_SENDS_CRLF = 1;
	private final static int PROGRAM_EXPECTS_LF = 0;
	private final static int PROGRAM_EXPECTS_CRLF = 1;
	private final static int PROGRAM_EXPECTS_CR = 2;
	private final static int NO_CHANGE = 0;
	private final static int CHANGE_CR_TO_LF = 1;
	private final static int INSERT_LF_AFTER_CR = 2;
	private final static int REMOVE_CR = 3;
	private final static int REMOVE_LF = 4;

	// CRLF conversion table:
	//
	// Expected line separator -->         |       LF        |        CRLF        |       CR       |
	// ------------------------------------+-----------------+--------------------+----------------+
	// Local echo off - control sends CR   | change CR to LF | insert LF after CR | no change      |
	// ------------------------------------+-----------------+--------------------+----------------+
	// Local echo on - control sends CRLF  | remove CR       | no change          | remove LF      |
	//
	private final static int[][] CRLF_REPLACEMENT = {

		{CHANGE_CR_TO_LF, INSERT_LF_AFTER_CR, NO_CHANGE},
		{REMOVE_CR, NO_CHANGE, REMOVE_LF}
	};

	private final boolean sendSIGINTOnCtrlC;
	private IStreamsProxy streamsProxy;
	private IProcess process;
	private int replacement;

	/**
	 * Creates a new {@link LocalTerminalOutputStream}.
	 *
	 * @param process the {@link IProcess} object of the terminal process
	 * @param settings the {@link ILocalTerminalSettings} (currently only used for the line
	 * separator settings)
	 */
	public LocalTerminalOutputStream(IProcess process, ILocalTerminalSettings settings) {

		this.process = process;
		streamsProxy = process.getStreamsProxy();
		sendSIGINTOnCtrlC = LocalTerminalUtilities.getCtrlC(settings);
		boolean localEcho = LocalTerminalUtilities.getLocalEcho(settings);
		int terminalSends = localEcho? TERMINAL_SENDS_CRLF:TERMINAL_SENDS_CR;
		int programExpects;
		String lineSeparator = LocalTerminalUtilities.getLineSeparator(settings);
		if (lineSeparator == null) {

			lineSeparator = System.getProperty(LocalTerminalUtilities.LINE_SEPARATOR_PROPERTY);
			if (LocalTerminalUtilities.CR.equals(lineSeparator)) {

				programExpects = PROGRAM_EXPECTS_CR;
			}
			else if (LocalTerminalUtilities.LF.equals(lineSeparator)) {

				programExpects = PROGRAM_EXPECTS_LF;
			}
			else {

				programExpects = PROGRAM_EXPECTS_CRLF;
			}
		}
		else if (lineSeparator.equals(ILocalTerminalSettings.LINE_SEPARATOR_LF)) {

			programExpects = PROGRAM_EXPECTS_LF;
		}
		else if (lineSeparator.equals(ILocalTerminalSettings.LINE_SEPARATOR_CR)) {

			programExpects = PROGRAM_EXPECTS_CR;
		}
		else {

			programExpects = PROGRAM_EXPECTS_CRLF;
		}
		replacement = CRLF_REPLACEMENT[terminalSends][programExpects];
	}

	/**
	 * Writes the specified byte to this output stream.
	 *
	 * @param data the byte
	 * @throws IOException if an I/O error occurs
	 */
	public void write(int data) throws IOException {

		write(new byte[] {(byte)data}, 0, 1);
	}

	/**
	 * Writes a specified number of bytes from the specified byte array starting at a given offset.
	 *
	 * @param data the array containing the data
	 * @param offset the offset into the array
	 * @param length the number of bytes to be written
	 * @throws IOException of an I/O error occurs
	 */
	public void write(byte[] data, int offset, int length) throws IOException {

		String text = new String(data, offset, length);
		//
		// TODO: check whether this is correct! new String(byte[], int, int) always uses the default
		//       encoding!

		if (replacement == CHANGE_CR_TO_LF) {

			text = text.replace(CR, LF);
		}
		else if (replacement == INSERT_LF_AFTER_CR) {

			text = text.replaceAll(ILocalTerminalSettings.LINE_SEPARATOR_CR, CRLF);
		}
		else if (replacement == REMOVE_CR) {

			text = text.replaceAll(ILocalTerminalSettings.LINE_SEPARATOR_CR, NOTHING);
		}
		else if (replacement == REMOVE_LF) {

			text = text.replaceAll(ILocalTerminalSettings.LINE_SEPARATOR_LF, NOTHING);
		}

		// Process Ctrl-C in the proper order:
		//
		int positionOfCtrlC = -1;
		while (sendSIGINTOnCtrlC && (positionOfCtrlC = text.indexOf(CTRL_C)) != -1) {

			// Send text up to (and including) the Ctrl-C to the process, then send a SIGINT:
			//
			streamsProxy.write(text.substring(0, positionOfCtrlC+1));
			if (process instanceof LocalTerminalProcess) {

				((LocalTerminalProcess)process).interrupt();
			}

			// Remove the part of the text that was already sent:
			//
			text = text.substring(positionOfCtrlC+1);
		}
		if (text.length() > 0) {

			streamsProxy.write(text);
		}
	}
}
