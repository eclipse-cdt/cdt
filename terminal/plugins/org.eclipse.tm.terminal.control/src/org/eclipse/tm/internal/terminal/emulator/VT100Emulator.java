/*******************************************************************************
 * Copyright (c) 2003, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - split into core, view and connector plugins
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Michael Scharf (Wind River) - [209746] There are cases where some colors not displayed correctly
 * Martin Oberhuber (Wind River) - [168197] Fix Terminal for CDC-1.1/Foundation-1.1
 * Michael Scharf (Wind River) - [262996] get rid of TerminalState.OPENED
 * Martin Oberhuber (Wind River) - [334969] Fix multi-command SGR sequence
 * Kris De Volder (VMWare) - [392107] Switched interpretation for ESC[0K and ESC[1K sequences
 * Martin Oberhuber (Wind River) - [401386] Regression: No header on top due to incorrect ESC[K interpretation
 * Martin Oberhuber (Wind River) - [401480] Handle ESC[39;49m and ESC[G
 * Anton Leherbauer (Wind River) - [433751] Add option to enable VT100 line wrapping mode
 * Anton Leherbauer (Wind River) - [458218] Add support for ANSI insert mode
 * Anton Leherbauer (Wind River) - [458398] Add support for normal/application cursor keys mode
 * Anton Leherbauer (Wind River) - [458402] Add support for scroll up/down and scroll region
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.emulator;

import static org.eclipse.tm.terminal.model.TerminalColor.BLACK;
import static org.eclipse.tm.terminal.model.TerminalColor.BLUE;
import static org.eclipse.tm.terminal.model.TerminalColor.CYAN;
import static org.eclipse.tm.terminal.model.TerminalColor.GREEN;
import static org.eclipse.tm.terminal.model.TerminalColor.MAGENTA;
import static org.eclipse.tm.terminal.model.TerminalColor.RED;
import static org.eclipse.tm.terminal.model.TerminalColor.WHITE;
import static org.eclipse.tm.terminal.model.TerminalColor.YELLOW;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.function.Function;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tm.internal.terminal.control.impl.ITerminalControlForText;
import org.eclipse.tm.internal.terminal.control.impl.TerminalPlugin;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.terminal.model.ITerminalTextData;
import org.eclipse.tm.terminal.model.TerminalStyle;

/**
 * This class processes character data received from the remote host and
 * displays it to the user using the Terminal view's StyledText widget. This
 * class processes ANSI control characters, including NUL, backspace, carriage
 * return, linefeed, and a subset of ANSI escape sequences sufficient to allow
 * use of screen-oriented applications, such as vi, Emacs, and any GNU
 * readline-enabled application (Bash, bc, ncftp, etc.).
 * <p>
 *
 * @author Fran Litterio <francis.litterio@windriver.com>
 * @author Chris Thew <chris.thew@windriver.com>
 */
public class VT100Emulator implements ControlListener {
	/** This is a character processing state: Initial state. */
	private static final int ANSISTATE_INITIAL = 0;

	/** This is a character processing state: We've seen an escape character. */
	private static final int ANSISTATE_ESCAPE = 1;

	/**
	 * This is a character processing state: We've seen a '[' after an escape
	 * character. Expecting a parameter character or a command character next.
	 */
	private static final int ANSISTATE_EXPECTING_PARAMETER_OR_COMMAND = 2;

	/**
	 * This is a character processing state: We've seen a ']' after an escape
	 * character. We are now expecting an operating system command that
	 * reprograms an intelligent terminal.
	 */
	private static final int ANSISTATE_EXPECTING_OS_COMMAND = 3;

	/**
	 * This is a character processing state: We've seen a '[?' after an escape
	 * character. Expecting a parameter character or a command character next.
	 */
	private static final int ANSISTATE_EXPECTING_DEC_PRIVATE_COMMAND = 4;

	/**
	 * This is a character processing state: We've seen one of ()*+-./ after an escape
	 * character. Expecting a character set designation character.
	 */
	private static final int ANSISTATE_EXPECTING_CHARSET_DESIGNATION = 5;

	/**
	 * This field holds the current state of the Finite TerminalState Automaton (FSA)
	 * that recognizes ANSI escape sequences.
	 *
	 * @see #processNewText()
	 */
	private int ansiState = ANSISTATE_INITIAL;

	/**
	 * This field holds a reference to the {@link TerminalControl} object that
	 * instantiates this class.
	 */
	private final ITerminalControlForText terminal;

	/**
	 * This field holds a reference to the StyledText widget that is used to
	 * display text to the user.
	 */
	final private IVT100EmulatorBackend text;
	/**
	 * This field hold the saved absolute line number of the cursor when
	 * processing the "ESC 7" and "ESC 8" command sequences.
	 */
	private int savedCursorLine = 0;

	/**
	 * This field hold the saved column number of the cursor when processing the
	 * "ESC 7" and "ESC 8" command sequences.
	 */
	private int savedCursorColumn = 0;

	/**
	 * This field holds an array of StringBuffer objects, each of which is one
	 * parameter from the current ANSI escape sequence. For example, when
	 * parsing the escape sequence "\e[20;10H", this array holds the strings
	 * "20" and "10".
	 */
	private final StringBuffer[] ansiParameters = new StringBuffer[16];

	/**
	 * This field holds the OS-specific command found in an escape sequence of
	 * the form "\e]...\u0007".
	 */
	private final StringBuffer ansiOsCommand = new StringBuffer(128);

	/**
	 * This field holds the index of the next unused element of the array stored
	 * in field {@link #ansiParameters}.
	 */
	private int nextAnsiParameter = 0;

	Reader fReader;

	boolean fCrAfterNewLine;

	/**
	 * The constructor.
	 */
	public VT100Emulator(ITerminalTextData data, ITerminalControlForText terminal, Reader reader) {
		super();

		Logger.log("entered"); //$NON-NLS-1$

		this.terminal = terminal;

		for (int i = 0; i < ansiParameters.length; ++i) {
			ansiParameters[i] = new StringBuffer();
		}
		setInputStreamReader(reader);
		if (TerminalPlugin.isOptionEnabled(Logger.TRACE_DEBUG_LOG_VT100BACKEND))
			text = new VT100BackendTraceDecorator(new VT100EmulatorBackend(data), System.out);
		else
			text = new VT100EmulatorBackend(data);

		//		text.setDimensions(24, 80);
		TerminalStyle style = TerminalStyle.getDefaultStyle();
		text.setDefaultStyle(style);
		text.setStyle(style);
	}

	/**
	 * Set the reader that this Terminal gets its input from.
	 *
	 * The reader can be changed while the Terminal is running, but a change of
	 * the reader likely loses some characters which have not yet been fully
	 * read. Changing the reader can be done in order to change the selected
	 * Encoding, though. This is typically done when the Terminal is
	 * constructed, i.e. before it really starts operation; or, when the user
	 * manually selects a different encoding and thus doesn't care about losing
	 * old characters.
	 *
	 * @param reader the new Reader
	 */
	public void setInputStreamReader(Reader reader) {
		fReader = reader;
	}

	public void setDimensions(int lines, int cols) {
		text.setDimensions(lines, cols);
		ITerminalConnector telnetConnection = getConnector();
		if (telnetConnection != null) {
			telnetConnection.setTerminalSize(text.getColumns(), text.getLines());
		}

	}

	/**
	 * This method performs clean up when this VT100Emulator object is no longer
	 * needed. After calling this method, no other method on this object should
	 * be called.
	 */
	public void dispose() {
	}

	/**
	 * This method is required by interface ControlListener. It allows us to
	 * know when the StyledText widget is moved.
	 */
	@Override
	public void controlMoved(ControlEvent event) {
		Logger.log("entered"); //$NON-NLS-1$
		// Empty.
	}

	/**
	 * This method is required by interface ControlListener. It allows us to
	 * know when the StyledText widget is resized.
	 */
	@Override
	public void controlResized(ControlEvent event) {
		Logger.log("entered"); //$NON-NLS-1$
		adjustTerminalDimensions();
	}

	/**
	 * This method erases all text from the Terminal view.
	 */
	public void clearTerminal() {
		Logger.log("entered"); //$NON-NLS-1$
		text.clearAll();
	}

	/**
	 * This method is called when the user changes the Terminal view's font. We
	 * attempt to recompute the pixel width of the new font's characters and fix
	 * the terminal's dimensions.
	 */
	public void fontChanged() {
		Logger.log("entered"); //$NON-NLS-1$

		if (text != null)
			adjustTerminalDimensions();
	}

	//	/**
	//	 * This method executes in the Display thread to process data received from
	//	 * the remote host by class {@link org.eclipse.tm.internal.terminal.telnet.TelnetConnection} and
	//	 * other implementors of {@link ITerminalConnector}, like the
	//	 * SerialPortHandler.
	//	 * <p>
	//	 * These connectors write text to the terminal's buffer through
	//	 * {@link TerminalControl#writeToTerminal(String)} and then have
	//	 * this run method executed in the display thread. This method
	//	 * must not execute at the same time as methods
	//	 * {@link #setNewText(StringBuffer)} and {@link #clearTerminal()}.
	//	 * <p>
	//	 * IMPORTANT: This method must be called in strict alternation with method
	//	 * {@link #setNewText(StringBuffer)}.
	//	 * <p>
	//	 */
	public void processText() {
		try {
			// Find the width and height of the terminal, and resize it to display an
			// integral number of lines and columns.

			adjustTerminalDimensions();

			// Restore the caret offset, process and display the new text, then save
			// the caret offset. See the documentation for field caretOffset for
			// details.

			// ISSUE: Is this causing the scroll-to-bottom-on-output behavior?

			try {
				processNewText();
			} catch (IOException e) {
				Logger.logException(e);
			}

		} catch (Exception ex) {
			Logger.logException(ex);
		}
	}

	/**
	 * This method scans the newly received text, processing ANSI control
	 * characters and escape sequences and displaying normal text.
	 * @throws IOException
	 */
	private void processNewText() throws IOException {
		Logger.log("entered"); //$NON-NLS-1$

		// Scan the newly received text.

		while (hasNextChar()) {
			char character = getNextChar();

			switch (ansiState) {
			case ANSISTATE_INITIAL:
				switch (character) {
				case '\u0000':
					break; // NUL character. Ignore it.

				case '\u0007':
					processBEL(); // BEL (Control-G)
					break;

				case '\b':
					processBackspace(); // Backspace
					break;

				case '\t':
					processTab(); // Tab.
					break;

				case '\n':
					processNewline(); // Newline (Control-J)
					if (fCrAfterNewLine)
						processCarriageReturn(); // Carriage Return (Control-M)
					break;

				case '\r':
					processCarriageReturn(); // Carriage Return (Control-M)
					break;

				case '\u001b':
					ansiState = ANSISTATE_ESCAPE; // Escape.
					break;

				default:
					processNonControlCharacters(character);
					break;
				}
				break;

			case ANSISTATE_ESCAPE:
				// We've seen an escape character. Here, we process the character
				// immediately following the escape.

				switch (character) {
				case '[':
					ansiState = ANSISTATE_EXPECTING_PARAMETER_OR_COMMAND;
					nextAnsiParameter = 0;

					// Erase the parameter strings in preparation for optional
					// parameter characters.

					for (int i = 0; i < ansiParameters.length; ++i) {
						ansiParameters[i].delete(0, ansiParameters[i].length());
					}
					break;

				case ']':
					ansiState = ANSISTATE_EXPECTING_OS_COMMAND;
					ansiOsCommand.delete(0, ansiOsCommand.length());
					break;

				case ')':
				case '(':
				case '*':
				case '+':
				case '-':
				case '.':
				case '/':
					ansiState = ANSISTATE_EXPECTING_CHARSET_DESIGNATION;
					break;

				case '7':
					// Save cursor position and character attributes

					ansiState = ANSISTATE_INITIAL;
					savedCursorLine = relativeCursorLine();
					savedCursorColumn = getCursorColumn();
					break;

				case '8':
					// Restore cursor and attributes to previously saved
					// position

					ansiState = ANSISTATE_INITIAL;
					moveCursor(savedCursorLine, savedCursorColumn);
					break;

				case 'c':
					// Reset the terminal
					ansiState = ANSISTATE_INITIAL;
					resetTerminal();
					break;

				default:
					Logger.log("Unsupported escape sequence: escape '" + character + "'"); //$NON-NLS-1$ //$NON-NLS-2$
					ansiState = ANSISTATE_INITIAL;
					break;
				}
				break;

			case ANSISTATE_EXPECTING_PARAMETER_OR_COMMAND:
				if (character == '?') {
					ansiState = ANSISTATE_EXPECTING_DEC_PRIVATE_COMMAND;
					break;
				}

				// Parameters can appear after the '[' in an escape sequence, but they
				// are optional.

				if (character == '@' || (character >= 'A' && character <= 'Z')
						|| (character >= 'a' && character <= 'z')) {
					ansiState = ANSISTATE_INITIAL;
					processAnsiCommandCharacter(character);
				} else {
					processAnsiParameterCharacter(character);
				}
				break;

			case ANSISTATE_EXPECTING_OS_COMMAND:
				// A BEL (\u0007) character marks the end of the OSC sequence.

				if (character == '\u0007') {
					ansiState = ANSISTATE_INITIAL;
					processAnsiOsCommand();
				} else {
					ansiOsCommand.append(character);
				}
				break;

			case ANSISTATE_EXPECTING_DEC_PRIVATE_COMMAND:
				// Parameters can appear after the '[?' in an escape sequence, but they
				// are optional.

				if (character == '@' || (character >= 'A' && character <= 'Z')
						|| (character >= 'a' && character <= 'z')) {
					ansiState = ANSISTATE_INITIAL;
					processDecPrivateCommandCharacter(character);
				} else {
					processAnsiParameterCharacter(character);
				}
				break;

			case ANSISTATE_EXPECTING_CHARSET_DESIGNATION:
				if (character != '%')
					ansiState = ANSISTATE_INITIAL;
				// Character set designation commands are ignored
				break;

			default:
				// This should never happen! If it does happen, it means there is a
				// bug in the FSA. For robustness, we return to the initial
				// state.

				Logger.log("INVALID ANSI FSA STATE: " + ansiState); //$NON-NLS-1$
				ansiState = ANSISTATE_INITIAL;
				break;
			}
		}
	}

	private void resetTerminal() {
		text.eraseAll();
		text.setCursor(0, 0);
		text.setStyle(text.getDefaultStyle());
		text.setScrollRegion(-1, -1);
		text.setInsertMode(false);
		terminal.enableApplicationCursorKeys(false);
	}

	/**
	 * This method is called when we have parsed an OS Command escape sequence.
	 * The only one we support is "\e]0;...\u0007", which sets the terminal
	 * title.
	 */
	private void processAnsiOsCommand() {
		if (ansiOsCommand.charAt(0) != '0' || ansiOsCommand.charAt(1) != ';') {
			Logger.log("Ignoring unsupported ANSI OSC sequence: '" + ansiOsCommand + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		terminal.setTerminalTitle(ansiOsCommand.substring(2));
	}

	/**
	 * This method dispatches control to various processing methods based on the
	 * command character found in the most recently received ANSI escape
	 * sequence. This method only handles command characters that follow the
	 * ANSI standard Control Sequence Introducer (CSI), which is "\e[...", where
	 * "..." is an optional ';'-separated sequence of numeric parameters.
	 * <p>
	 */
	private void processAnsiCommandCharacter(char ansiCommandCharacter) {
		// If the width or height of the terminal is ridiculously small (one line or
		// column or less), don't even try to process the escape sequence. This avoids
		// throwing an exception (SPR 107450). The display will be messed up, but what
		// did you user expect by making the terminal so small?

		switch (ansiCommandCharacter) {
		case '@':
			// Insert character(s).
			processAnsiCommand_atsign();
			break;

		case 'A':
			// Move cursor up N lines (default 1).
			processAnsiCommand_A();
			break;

		case 'B':
			// Move cursor down N lines (default 1).
			processAnsiCommand_B();
			break;

		case 'C':
			// Move cursor forward N columns (default 1).
			processAnsiCommand_C();
			break;

		case 'D':
			// Move cursor backward N columns (default 1).
			processAnsiCommand_D();
			break;

		case 'd':
			// Line Position Absolute [row] (default = [1,column]) (VPA).
			processAnsiCommand_d();
			break;

		case 'E':
			// Move cursor to first column of Nth next line (default 1).
			processAnsiCommand_E();
			break;

		case 'F':
			// Move cursor to first column of Nth previous line (default 1).
			processAnsiCommand_F();
			break;

		case 'G':
			// Move to column N of current line (default 1).
			processAnsiCommand_G();
			break;

		case 'H':
			// Set cursor Position.
			processAnsiCommand_H();
			break;

		case 'h':
			// Reset Mode.
			processAnsiCommand_h();
			break;

		case 'J':
			// Erase part or all of display. Cursor does not move.
			processAnsiCommand_J();
			break;

		case 'K':
			// Erase in line (cursor does not move).
			processAnsiCommand_K();
			break;

		case 'L':
			// Insert line(s) (current line moves down).
			processAnsiCommand_L();
			break;

		case 'l':
			// Set Mode.
			processAnsiCommand_l();
			break;

		case 'M':
			// Delete line(s).
			processAnsiCommand_M();
			break;

		case 'm':
			// Set Graphics Rendition (SGR).
			processAnsiCommand_m();
			break;

		case 'n':
			// Device Status Report (DSR).
			processAnsiCommand_n();
			break;

		case 'P':
			// Delete character(s).
			processAnsiCommand_P();
			break;

		case 'r':
			// Set Scrolling Region.
			processAnsiCommand_r();
			break;

		case 'S':
			// Scroll up.
			processAnsiCommand_S();
			break;

		case 'T':
			// Scroll down.
			processAnsiCommand_T();
			break;

		case 'X':
			// Erase character.
			// Emacs, vi, and GNU readline don't seem to use this command, so we ignore
			// it for now.
			break;

		case 'Z':
			// Cursor back tab.
			// Emacs, vi, and GNU readline don't seem to use this command, so we ignore
			// it for now.
			break;

		default:
			Logger.log("Ignoring unsupported ANSI command character: '" + //$NON-NLS-1$
					ansiCommandCharacter + "'"); //$NON-NLS-1$
			break;
		}
	}

	/**
	 * This method dispatches control to various processing methods based on the
	 * command character found in the most recently received DEC private mode escape
	 * sequence. This method only handles command characters that follow the
	 * control sequence CSI ?
	 */
	private void processDecPrivateCommandCharacter(char commandCharacter) {
		switch (commandCharacter) {
		case 'h':
			// DEC Private Mode Set (DECSET)
			processDecPrivateCommand_h();
			break;

		case 'l':
			// DEC Private Mode Reset (DECRST)
			processDecPrivateCommand_l();
			break;

		default:
			Logger.log("Ignoring unsupported DEC private command character: '" + //$NON-NLS-1$
					commandCharacter + "'"); //$NON-NLS-1$
			break;
		}
	}

	/**
	 * This method makes room for N characters on the current line at the cursor
	 * position. Text under the cursor moves right without wrapping at the end
	 * of the line.
	 */
	private void processAnsiCommand_atsign() {
		int charactersToInsert = getAnsiParameter(0);
		text.insertCharacters(charactersToInsert);
	}

	/**
	 * This method moves the cursor up by the number of lines specified by the
	 * escape sequence parameter (default 1).
	 */
	private void processAnsiCommand_A() {
		moveCursorUp(getAnsiParameter(0));
	}

	/**
	 * This method moves the cursor down by the number of lines specified by the
	 * escape sequence parameter (default 1).
	 */
	private void processAnsiCommand_B() {
		moveCursorDown(getAnsiParameter(0));
	}

	/**
	 * This method moves the cursor forward by the number of columns specified
	 * by the escape sequence parameter (default 1).
	 */
	private void processAnsiCommand_C() {
		moveCursorForward(getAnsiParameter(0));
	}

	/**
	 * This method moves the cursor backward by the number of columns specified
	 * by the escape sequence parameter (default 1).
	 */
	private void processAnsiCommand_D() {
		moveCursorBackward(getAnsiParameter(0));
	}

	/**
	 * This method moves the cursor to a specific row.
	 */
	private void processAnsiCommand_d() {
		// Line Position Absolute [row] (default = [1,column]) (VPA).
		text.setCursorLine(getAnsiParameter(0) - 1);
	}

	/**
	 * This method moves the cursor to the first column of the Nth next line,
	 * where N is specified by the ANSI parameter (default 1).
	 */
	private void processAnsiCommand_E() {
		int linesToMove = getAnsiParameter(0);

		moveCursor(relativeCursorLine() + linesToMove, 0);
	}

	/**
	 * This method moves the cursor to the first column of the Nth previous
	 * line, where N is specified by the ANSI parameter (default 1).
	 */
	private void processAnsiCommand_F() {
		int linesToMove = getAnsiParameter(0);

		moveCursor(relativeCursorLine() - linesToMove, 0);
	}

	/**
	 * This method moves the cursor within the current line to the column
	 * specified by the ANSI parameter (default is column 1).
	 */
	private void processAnsiCommand_G() {
		moveCursor(relativeCursorLine(), getAnsiParameter(0) - 1);
	}

	/**
	 * This method sets the cursor to a position specified by the escape
	 * sequence parameters (default is the upper left corner of the screen).
	 */
	private void processAnsiCommand_H() {
		moveCursor(getAnsiParameter(0) - 1, getAnsiParameter(1) - 1);
	}

	/**
	 * This method sets terminal modes.
	 */
	private void processAnsiCommand_h() {
		if (getAnsiParameter(0) == 4) {
			// set insert mode
			text.setInsertMode(true);
		}
	}

	/**
	 * This method deletes some (or all) of the text on the screen without
	 * moving the cursor.
	 */
	private void processAnsiCommand_J() {
		int ansiParameter;

		if (ansiParameters[0].length() == 0)
			ansiParameter = 0;
		else
			ansiParameter = getAnsiParameter(0);

		switch (ansiParameter) {
		case 0:
			text.eraseToEndOfScreen();
			break;

		case 1:
			// Erase from beginning to current position (inclusive).
			text.eraseToCursor();
			break;

		case 2:
			// Erase entire display.

			text.eraseAll();
			break;

		default:
			Logger.log("Unexpected J-command parameter: " + ansiParameter); //$NON-NLS-1$
			break;
		}
	}

	/**
	 * This method deletes some (or all) of the text in the current line without
	 * moving the cursor.
	 */
	private void processAnsiCommand_K() {
		//Bug 401386: missing parameter must be interpreted as 0, and not 1 like most other defaults.
		int ansiParameter = 0;
		if (ansiParameters[0].length() > 0)
			ansiParameter = getAnsiParameter(0);

		switch (ansiParameter) {
		case 0:
			// Erase from current position to end (inclusive).
			text.eraseLineToEnd();
			break;

		case 1:
			// Erase from beginning to current position (inclusive).
			text.eraseLineToCursor();
			break;

		case 2:
			// Erase entire line.
			text.eraseLine();
			break;

		default:
			Logger.log("Unexpected K-command parameter: " + ansiParameter); //$NON-NLS-1$
			break;
		}
	}

	/**
	 * Insert one or more blank lines. The current line of text moves down. Text
	 * that falls off the bottom of the screen is deleted.
	 */
	private void processAnsiCommand_L() {
		text.insertLines(getAnsiParameter(0));
	}

	/**
	 * This method resets terminal modes.
	 */
	private void processAnsiCommand_l() {
		if (getAnsiParameter(0) == 4) {
			// reset insert mode
			text.setInsertMode(false);
		}
	}

	/**
	 * Delete one or more lines of text. Any lines below the deleted lines move
	 * up, which we implement by appending newlines to the end of the text.
	 */
	private void processAnsiCommand_M() {
		text.deleteLines(getAnsiParameter(0));
	}

	/**
	 * This method sets a new graphics rendition mode, such as
	 * foreground/background color, bold/normal text, and reverse video.
	 */
	private void processAnsiCommand_m() {
		if (ansiParameters[0].length() == 0) {
			// This a special case: when no ANSI parameter is specified, act like a
			// single parameter equal to 0 was specified.

			ansiParameters[0].append('0');
		}
		TerminalStyle style = text.getStyle();
		// There are a non-zero number of ANSI parameters. Process each one in
		// order.

		int totalParameters = ansiParameters.length;
		int parameterIndex = 0;

		Function<Integer, Boolean> moreParameters = (index) -> index < totalParameters
				&& ansiParameters[index].length() > 0;
		while (moreParameters.apply(parameterIndex)) {
			int ansiParameter = getAnsiParameter(parameterIndex);
			if (ansiParameter == 1) {
				String parameter = ansiParameters[parameterIndex].toString();
				// Special case for  ITU's T.416 foreground/background color specification
				// which uses : to separate parameters instead of ;
				if (parameter.startsWith("38:") || parameter.startsWith("48:")) { //$NON-NLS-1$ //$NON-NLS-2$
					String[] split = parameter.split(":"); //$NON-NLS-1$
					ProcessExtendedColorsReturn retval = processExtendedColors(split, style, true);
					style = retval.style();
					parameterIndex++;
					continue;
				}

			}

			switch (ansiParameter) {
			case 0:
				// Reset all graphics modes.
				style = text.getDefaultStyle();
				break;

			case 1:
				style = style.setBold(true);
				break;

			case 4:
				style = style.setUnderline(true);
				break;

			case 5:
				style = style.setBlink(true);
				break;

			case 7:
				style = style.setReverse(true);
				break;

			case 10: // Set primary font. Ignored.
				break;

			case 21:
			case 22:
				style = style.setBold(false);
				break;

			case 24:
				style = style.setUnderline(false);
				break;

			case 25:
				style = style.setBlink(false);
				break;

			case 27:
				style = style.setReverse(false);
				break;

			case 30:
				style = style.setForeground(BLACK);
				break;

			case 31:
				style = style.setForeground(RED);
				break;

			case 32:
				style = style.setForeground(GREEN);
				break;

			case 33:
				style = style.setForeground(YELLOW);
				break;

			case 34:
				style = style.setForeground(BLUE);
				break;

			case 35:
				style = style.setForeground(MAGENTA);
				break;

			case 36:
				style = style.setForeground(CYAN);
				break;

			case 37:
				style = style.setForeground(WHITE);
				break;

			case 39: //Foreground: Default
				style = style.setForeground(text.getDefaultStyle());
				break;

			case 40:
				style = style.setBackground(BLACK);
				break;

			case 41:
				style = style.setBackground(RED);
				break;

			case 42:
				style = style.setBackground(GREEN);
				break;

			case 43:
				style = style.setBackground(YELLOW);
				break;

			case 44:
				style = style.setBackground(BLUE);
				break;

			case 45:
				style = style.setBackground(MAGENTA);
				break;

			case 46:
				style = style.setBackground(CYAN);
				break;

			case 47:
				style = style.setBackground(WHITE);
				break;

			case 49: //Background: Default
				style = style.setBackground(text.getDefaultStyle());
				break;

			case 38: // Foreground color defined by sequence
			case 48: // Background color defined by sequence
				CharSequence[] params = Arrays.copyOfRange(ansiParameters, parameterIndex, ansiParameters.length);
				ProcessExtendedColorsReturn retval = processExtendedColors(params, style, false);
				parameterIndex += retval.consumed() - 1;
				style = retval.style();
				break;

			default:
				Logger.log("Unsupported graphics rendition parameter: " + ansiParameter); //$NON-NLS-1$
				break;
			}

			++parameterIndex;
		}
		text.setStyle(style);
	}

	private interface ProcessExtendedColorsReturn {
		/**
		 * @return the new style
		 */
		TerminalStyle style();

		/**
		 * @return number of parameters consumed
		 */
		int consumed();
	}

	/**
	 *
	 * @param params array of parameters, starting with 38 or 48 being the command
	 * @param colorspace if a colorspace may be included (ITU T.416 mode)
	 */
	private ProcessExtendedColorsReturn processExtendedColors(CharSequence[] paramStrings, TerminalStyle style,
			boolean colorspace) {
		int params[] = new int[paramStrings.length];
		for (int i = 0; i < params.length; i++) {
			try {
				int parseInt = Integer.parseInt(paramStrings[i].toString());
				int inMagnitude = parseInt % 256;
				int inRange = inMagnitude < 0 ? inMagnitude + 256 : inMagnitude;
				params[i] = inRange;
			} catch (NumberFormatException ex) {
				params[i] = 0;
			}
		}

		boolean foreground = params[0] == 38;
		int consumed = 1;
		if (params.length > 1) {
			int colorDepth = params[1];
			switch (colorDepth) {
			case 2: // 24-bit RGB color
				int r = 0, g = 0, b = 0;
				if (colorspace) {
					if (params.length < 6) {
						Logger.log(
								"Not enough parameters for 24-bit color depth, expected 5, one for color space and one for each of RGB"); //$NON-NLS-1$
					}
				} else {
					if (params.length < 5) {
						Logger.log("Not enough parameters for 24-bit color depth, expected 3, one for each of RGB"); //$NON-NLS-1$
					}
				}
				int start = colorspace ? 3 : 2;
				if (params.length > start + 0) {
					r = params[start + 0];
				}
				if (params.length > start + 1) {
					g = params[start + 1];
				}
				if (params.length > start + 2) {
					b = params[start + 2];
				}

				RGB rgb = new RGB(r, g, b);
				if (foreground) {
					style = style.setForeground(rgb);
				} else {
					style = style.setBackground(rgb);
				}
				consumed = Math.min(6, params.length);
				break;
			case 5: // 8-bit color table lookup
				int index = 0;
				if (params.length < 3) {
					Logger.log("Missing parameter for 8-bit color depth"); //$NON-NLS-1$
				} else {
					index = params[2];
				}
				if (foreground) {
					style = style.setForeground(index);
				} else {
					style = style.setBackground(index);
				}
				consumed = Math.min(3, params.length);
				break;
			default:
				Logger.log("Unsupported color depth " + colorDepth + " for: " + params[0]); //$NON-NLS-1$ //$NON-NLS-2$
			}

		} else {
			Logger.log("Missing color depth for " + params[0]); //$NON-NLS-1$
		}

		TerminalStyle finalStyle = style;
		int finalConsumed = consumed;
		return new ProcessExtendedColorsReturn() {
			@Override
			public TerminalStyle style() {
				return finalStyle;
			}

			@Override
			public int consumed() {
				return finalConsumed;
			}
		};
	}

	/**
	 * This method responds to an ANSI Device Status Report (DSR) command from
	 * the remote endpoint requesting the ready status or the cursor position.
	 * Requests for other kinds of status are ignored.
	 */
	private void processAnsiCommand_n() {
		String reply;

		if (getAnsiParameter(0) == 5) {
			// Report that the terminal is ready and has no malfunctions.
			reply = "\u001b[0n"; //$NON-NLS-1$

		} else if (getAnsiParameter(0) == 6) {
			// Send the ANSI cursor position (which is 1-based) to the remote
			// endpoint.
			reply = "\u001b[" + (relativeCursorLine() + 1) + ";" + //$NON-NLS-1$ //$NON-NLS-2$
					(getCursorColumn() + 1) + "R"; //$NON-NLS-1$

		} else {
			// Do nothing if the numeric parameter was not 5 or 6.
			return;
		}

		try {
			terminal.getOutputStream().write(reply.getBytes("ISO-8859-1")); //$NON-NLS-1$
			terminal.getOutputStream().flush();
		} catch (IOException ex) {
			Logger.log("Caught IOException!"); //$NON-NLS-1$
		}
	}

	/**
	 * Deletes one or more characters starting at the current cursor position.
	 * Characters on the same line and to the right of the deleted characters
	 * move left. If there are no characters on the current line at or to the
	 * right of the cursor column, no text is deleted.
	 */
	private void processAnsiCommand_P() {
		text.deleteCharacters(getAnsiParameter(0));
	}

	/**
	 *  Set Scrolling Region [top;bottom] (default = full size of window) (DECSTBM).
	 */
	private void processAnsiCommand_r() {
		int top = 0;
		int bottom = 0;
		if (ansiParameters[0].length() > 0 && ansiParameters[1].length() > 0) {
			top = getAnsiParameter(0);
			bottom = getAnsiParameter(1);
		}
		text.setScrollRegion(top - 1, bottom - 1);
	}

	/**
	 * Scroll up n lines (default = 1 line).
	 */
	private void processAnsiCommand_S() {
		text.scrollUp(getAnsiParameter(0));
	}

	/**
	 * Scroll down n lines (default = 1 line).
	 */
	private void processAnsiCommand_T() {
		text.scrollDown(getAnsiParameter(0));
	}

	private void processDecPrivateCommand_h() {
		int param = getAnsiParameter(0);
		switch (param) {
		case 1:
			// Enable Application Cursor Keys (DECCKM)
			terminal.enableApplicationCursorKeys(true);
			break;
		case 47:
		case 1047:
		case 1048:
		case 1049:
			// Use Alternate Screen Buffer (ignored).
			break;
		default:
			Logger.log("Unsupported command parameter: CSI ?" + param + 'h'); //$NON-NLS-1$
			break;
		}
	}

	private void processDecPrivateCommand_l() {
		int param = getAnsiParameter(0);
		switch (param) {
		case 1:
			// Enable Normal Cursor Keys (DECCKM)
			terminal.enableApplicationCursorKeys(false);
			break;
		case 47:
		case 1047:
		case 1048:
		case 1049:
			// Use Normal Screen Buffer (ignored, but reset scroll region).
			text.setScrollRegion(-1, -1);
			break;
		default:
			Logger.log("Unsupported command parameter: CSI ?" + param + 'l'); //$NON-NLS-1$
			break;
		}
	}

	/**
	 * This method returns one of the numeric ANSI parameters received in the
	 * most recent escape sequence.
	 *
	 * @return The <i>parameterIndex</i>th numeric ANSI parameter or -1 if the
	 *         index is out of range or 1 if parse failed (1 is also a legitimate value)
	 */
	private int getAnsiParameter(int parameterIndex) {
		if (parameterIndex < 0 || parameterIndex >= ansiParameters.length) {
			// This should never happen.
			return -1;
		}

		String parameter = ansiParameters[parameterIndex].toString();

		if (parameter.length() == 0)
			return 1;

		// return 1 on failed parseInt
		int parameterValue = 1;

		// Don't trust the remote endpoint to send well formed numeric
		// parameters.

		try {
			parameterValue = Integer.parseInt(parameter);
		} catch (NumberFormatException ex) {
			parameterValue = 1;
		}

		return parameterValue;
	}

	/**
	 * This method processes a single parameter character in an ANSI escape
	 * sequence. Parameters are the (optional) characters between the leading
	 * "\e[" and the command character in an escape sequence (e.g., in the
	 * escape sequence "\e[20;10H", the parameter characters are "20;10").
	 * Parameters are integers separated by one or more ';'s.
	 */
	private void processAnsiParameterCharacter(char ch) {
		if (ch == ';') {
			++nextAnsiParameter;
		} else {
			if (nextAnsiParameter < ansiParameters.length)
				ansiParameters[nextAnsiParameter].append(ch);
		}
	}

	/**
	 * This method processes a contiguous sequence of non-control characters.
	 * This is a performance optimization, so that we don't have to insert or
	 * append each non-control character individually to the StyledText widget.
	 * A non-control character is any character that passes the condition in the
	 * below while loop.
	 * @throws IOException
	 */
	private void processNonControlCharacters(char character) throws IOException {
		StringBuffer buffer = new StringBuffer();
		buffer.append(character);
		// Identify a contiguous sequence of non-control characters, starting at
		// firstNonControlCharacterIndex in newText.
		while (hasNextChar()) {
			character = getNextChar();
			if (character == '\u0000' || character == '\b' || character == '\t' || character == '\u0007'
					|| character == '\n' || character == '\r' || character == '\u001b') {
				pushBackChar(character);
				break;
			}
			buffer.append(character);
		}

		// Now insert the sequence of non-control characters in the StyledText widget
		// at the location of the cursor.

		displayNewText(buffer.toString());
	}

	/**
	 * This method displays a subset of the newly-received text in the Terminal
	 * view, wrapping text at the right edge of the screen and overwriting text
	 * when the cursor is not at the very end of the screen's text.
	 * <p>
	 *
	 * There are never any ANSI control characters or escape sequences in the
	 * text being displayed by this method (this includes newlines, carriage
	 * returns, and tabs).
	 * <p>
	 */
	private void displayNewText(String buffer) {
		text.appendString(buffer);
	}

	/**
	 * Process a BEL (Control-G) character.
	 */
	private void processBEL() {
		// TODO
		//Display.getDefault().beep();
	}

	/**
	 * Process a backspace (Control-H) character.
	 */
	private void processBackspace() {
		moveCursorBackward(1);
	}

	/**
	 * Process a tab (Control-I) character. We don't insert a tab character into
	 * the StyledText widget. Instead, we move the cursor forward to the next
	 * tab stop, without altering any of the text. Tab stops are every 8
	 * columns. The cursor will never move past the rightmost column.
	 */
	private void processTab() {
		moveCursorForward(8 - (getCursorColumn() % 8));
	}

	/**
	 * Process a newline (Control-J) character. A newline (NL) character just
	 * moves the cursor to the same column on the next line, creating new lines
	 * when the cursor reaches the bottom edge of the terminal. This is
	 * counter-intuitive, especially to UNIX programmers who are taught that
	 * writing a single NL to a terminal is sufficient to move the cursor to the
	 * first column of the next line, as if a carriage return (CR) and a NL were
	 * written.
	 * <p>
	 *
	 * UNIX terminals typically display a NL character as a CR followed by a NL
	 * because the terminal device typically has the ONLCR attribute bit set
	 * (see the termios(4) man page for details), which causes the terminal
	 * device driver to translate NL to CR + NL on output. The terminal itself
	 * (i.e., a hardware terminal or a terminal emulator, like xterm or this
	 * code) _always_ interprets a CR to mean "move the cursor to the beginning
	 * of the current line" and a NL to mean "move the cursor to the same column
	 * on the next line".
	 * <p>
	 */
	private void processNewline() {
		text.processNewline();
	}

	/**
	 * Process a Carriage Return (Control-M).
	 */
	private void processCarriageReturn() {
		text.setCursorColumn(0);
	}

	/**
	 * This method computes the width of the terminal in columns and its height
	 * in lines, then adjusts the width and height of the view's StyledText
	 * widget so that it displays an integral number of lines and columns of
	 * text. The adjustment is always to shrink the widget vertically or
	 * horizontally, because if the control were to grow, it would be clipped by
	 * the edges of the view window (i.e., the view window does not become
	 * larger to accommodate its contents becoming larger).
	 * <p>
	 *
	 * This method must be called immediately before each time text is written
	 * to the terminal so that we can properly line wrap text. Because it is
	 * called so frequently, it must be fast when there is no resizing to be
	 * done.
	 * <p>
	 */
	private void adjustTerminalDimensions() {
		// Compute how many pixels we need to shrink the StyledText control vertically
		// to make it display an integral number of lines of text.

		// TODO
		//		if(text.getColumns()!=80 && text.getLines()!=80)
		//			text.setDimensions(24, 80);
		// If we are in a TELNET connection and we know the dimensions of the terminal,
		// we give the size information to the TELNET connection object so it can
		// communicate it to the TELNET server. If we are in a serial connection,
		// there is nothing we can do to tell the remote host about the size of the
		// terminal.
		ITerminalConnector telnetConnection = getConnector();
		// TODO MSA: send only if dimensions have really changed!
		if (telnetConnection != null) {
			telnetConnection.setTerminalSize(text.getColumns(), text.getLines());
		}

	}

	private ITerminalConnector getConnector() {
		if (terminal.getTerminalConnector() != null)
			return terminal.getTerminalConnector();
		return null;
	}

	/**
	 * This method returns the relative line number of the line containing the
	 * cursor. The returned line number is relative to the topmost visible line,
	 * which has relative line number 0.
	 *
	 * @return The relative line number of the line containing the cursor.
	 */
	private int relativeCursorLine() {
		return text.getCursorLine();
	}

	/**
	 * This method moves the cursor to the specified line and column. Parameter
	 * <i>targetLine</i> is the line number of a screen line, so it has a
	 * minimum value of 0 (the topmost screen line) and a maximum value of
	 * heightInLines - 1 (the bottommost screen line). A line does not have to
	 * contain any text to move the cursor to any column in that line.
	 */
	private void moveCursor(int targetLine, int targetColumn) {
		text.setCursor(targetLine, targetColumn);
	}

	/**
	 * This method moves the cursor down <i>lines</i> lines, but won't move the
	 * cursor past the bottom of the screen. This method does not cause any
	 * scrolling.
	 */
	private void moveCursorDown(int lines) {
		moveCursor(relativeCursorLine() + lines, getCursorColumn());
	}

	/**
	 * This method moves the cursor up <i>lines</i> lines, but won't move the
	 * cursor past the top of the screen. This method does not cause any
	 * scrolling.
	 */
	private void moveCursorUp(int lines) {
		moveCursor(relativeCursorLine() - lines, getCursorColumn());
	}

	/**
	 * This method moves the cursor forward <i>columns</i> columns, but won't
	 * move the cursor past the right edge of the screen, nor will it move the
	 * cursor onto the next line. This method does not cause any scrolling.
	 */
	private void moveCursorForward(int columnsToMove) {
		moveCursor(relativeCursorLine(), getCursorColumn() + columnsToMove);
	}

	/**
	 * This method moves the cursor backward <i>columnsToMove</i> columns, but
	 * won't move the cursor past the left edge of the screen, nor will it move
	 * the cursor onto the previous line. This method does not cause any
	 * scrolling.
	 */
	private void moveCursorBackward(int columnsToMove) {
		moveCursor(relativeCursorLine(), getCursorColumn() - columnsToMove);
	}

	/**
	 * Resets the state of the terminal text (foreground color, background color,
	 * font style and other internal state). It essentially makes it ready for new input.
	 */
	public void resetState() {
		ansiState = ANSISTATE_INITIAL;
		text.setStyle(text.getDefaultStyle());
		text.setScrollRegion(-1, -1);
		text.setInsertMode(false);
	}

	//	public OutputStream getOutputStream() {
	//		return fTerminalInputStream.getOutputStream();
	//	}

	/**
	 * Buffer for {@link #pushBackChar(char)}.
	 */
	private int fNextChar = -1;

	private char getNextChar() throws IOException {
		int c = -1;
		if (fNextChar != -1) {
			c = fNextChar;
			fNextChar = -1;
		} else {
			c = fReader.read();
		}
		// TODO: better end of file handling
		if (c == -1)
			c = 0;
		return (char) c;
	}

	private boolean hasNextChar() throws IOException {
		if (fNextChar >= 0)
			return true;
		return fReader.ready();
	}

	/**
	 * Put back one character to the stream. This method can push
	 * back exactly one character. The character is the next character
	 * returned by {@link #getNextChar}
	 * @param c the character to be pushed back.
	 */
	void pushBackChar(char c) {
		//assert fNextChar!=-1: "Already a character waiting:"+fNextChar; //$NON-NLS-1$
		fNextChar = c;
	}

	private int getCursorColumn() {
		return text.getCursorColumn();
	}

	public boolean isCrAfterNewLine() {
		return fCrAfterNewLine;
	}

	public void setCrAfterNewLine(boolean crAfterNewLine) {
		fCrAfterNewLine = crAfterNewLine;
	}

	void setVT100LineWrapping(boolean enable) {
		text.setVT100LineWrapping(enable);
	}

	boolean isVT100LineWrapping() {
		return text.isVT100LineWrapping();
	}
}
