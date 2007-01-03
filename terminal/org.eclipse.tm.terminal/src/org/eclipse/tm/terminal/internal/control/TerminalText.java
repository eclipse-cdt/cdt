/*******************************************************************************
 * Copyright (c) 2003, 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - split into core, view and connector plugins 
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.terminal.internal.control;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.terminal.ITerminalConnector;
import org.eclipse.tm.terminal.Logger;
import org.eclipse.tm.terminal.TerminalState;
import org.eclipse.tm.terminal.internal.telnet.TelnetConnection;

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
public class TerminalText implements Runnable, ControlListener {
	/** This is a character processing state: Initial state. */
	protected static final int ANSISTATE_INITIAL = 0;

	/** This is a character processing state: We've seen an escape character. */
	protected static final int ANSISTATE_ESCAPE = 1;

	/**
	 * This is a character processing state: We've seen a '[' after an escape
	 * character. Expecting a parameter character or a command character next.
	 */
	protected static final int ANSISTATE_EXPECTING_PARAMETER_OR_COMMAND = 2;

	/**
	 * This is a character processing state: We've seen a ']' after an escape
	 * character. We are now expecting an operating system command that
	 * reprograms an intelligent terminal.
	 */
	protected static final int ANSISTATE_EXPECTING_OS_COMMAND = 3;

	/**
	 * This field holds the current state of the Finite TerminalState Automaton (FSA)
	 * that recognizes ANSI escape sequences.
	 * 
	 * @see #processNewText()
	 */
	protected int ansiState = ANSISTATE_INITIAL;

	/**
	 * This field holds a reference to the {@link TerminalControl} object that
	 * instantiates this class.
	 */
	protected ITerminalControlForText terminal;

	/**
	 * This field holds a reference to the StyledText widget that is used to
	 * display text to the user.
	 */
	protected StyledText text;

	/**
	 * This field holds the characters received from the remote host before they
	 * are displayed to the user. Method {@link #processNewText()} scans this
	 * text looking for ANSI control characters and escape sequences.
	 */
	protected StringBuffer newText;

	/**
	 * This field holds the index of the current character while the text stored
	 * in field {@link #newText} is being processed.
	 */
	protected int characterIndex = 0;

	/**
	 * This field holds the width of a character (in pixels) for the font used
	 * to display text.
	 */
	protected int characterPixelWidth = 0;

	/**
	 * This field holds the width of the terminal screen in columns.
	 */
	protected int widthInColumns = 0;

	/**
	 * This field holds the height of the terminal screen in visible lines. The
	 * StyledText widget can contain more lines than are visible.
	 */
	protected int heightInLines = 0;

	/**
	 * This field holds the number of the column in which the cursor is
	 * logically positioned. The leftmost column on the screen is column 0, and
	 * column numbers increase to the right. The maximum value of this field is
	 * {@link #widthInColumns} - 1. We track the cursor column using this field
	 * to avoid having to recompute it repeatly using StyledText method calls.
	 * <p>
	 * 
	 * The StyledText widget that displays text has a vertical bar (called the
	 * "caret") that appears _between_ character cells, but ANSI terminals have
	 * the concept of a cursor that appears _in_ a character cell, so we need a
	 * convention for which character cell the cursor logically occupies when
	 * the caret is physically between two cells. The convention used in this
	 * class is that the cursor is logically in column N when the caret is
	 * physically positioned immediately to the _left_ of column N.
	 * <p>
	 * 
	 * When cursorColumn is N, the next character output to the terminal appears
	 * in column N. When a character is output to the rightmost column on a
	 * given line (column widthInColumns - 1), the cursor moves to column 0 on
	 * the next line after the character is drawn (this is how line wrapping is
	 * implemented). If the cursor is in the bottommost line when line wrapping
	 * occurs, the topmost visible line is scrolled off the top edge of the
	 * screen.
	 * <p>
	 */
	protected int cursorColumn = 0;

	/**
	 * This field holds the caret offset when we last moved it or wrote text to
	 * the terminal. The reason we need to remember this value is because,
	 * unlike with a normal terminal emulator, the user can move the caret by
	 * clicking anywhere in the terminal view. In a normal terminal emulator,
	 * the cursor only moves as the result of character output (i.e., escape
	 * sequences or normal characters). We use the value stored in this field to
	 * restore the position of the caret immediately before processing each
	 * chunk of output from the remote endpoint.
	 */
	protected int caretOffset = 0;

	/**
	 * This field hold the saved absolute line number of the cursor when
	 * processing the "ESC 7" and "ESC 8" command sequences.
	 */
	protected int savedCursorLine = 0;

	/**
	 * This field hold the saved column number of the cursor when processing the
	 * "ESC 7" and "ESC 8" command sequences.
	 */
	protected int savedCursorColumn = 0;

	/**
	 * This field holds an array of StringBuffer objects, each of which is one
	 * parameter from the current ANSI escape sequence. For example, when
	 * parsing the escape sequence "\e[20;10H", this array holds the strings
	 * "20" and "10".
	 */
	protected StringBuffer[] ansiParameters = new StringBuffer[16];

	/**
	 * This field holds the OS-specific command found in an escape sequence of
	 * the form "\e]...\u0007".
	 */
	protected StringBuffer ansiOsCommand = new StringBuffer(128);

	/**
	 * This field holds the index of the next unused element of the array stored
	 * in field {@link #ansiParameters}.
	 */
	protected int nextAnsiParameter = 0;

	/**
	 * This field holds the Color object representing the current foreground
	 * color as set by the ANSI escape sequence "\e[m".
	 */
	protected Color currentForegroundColor;

	/**
	 * This field holds the Color object representing the current background
	 * color as set by the ANSI escape sequence "\e[m".
	 */
	protected Color currentBackgroundColor;

	/**
	 * This field holds an integer representing the current font style as set by
	 * the ANSI escape sequence "\e[m".
	 */
	protected int currentFontStyle = SWT.NORMAL;

	/**
	 * This field is true if we are currently outputing text in reverse video
	 * mode, false otherwise.
	 */
	protected boolean reverseVideo = false;

	/**
	 * This field holds the time (in milliseconds) of the previous call to
	 * method {@link #SetNewText()}.
	 */
	static long LastNewOutputTime = 0;

	/**
	 * Color object representing the color black. The Color class requires us to
	 * call dispose() on this object when we no longer need it. We do that in
	 * method {@link #dispose()}.
	 */
	protected final Color BLACK = new Color(Display.getCurrent(), 0, 0, 0);

	/**
	 * Color object representing the color red. The Color class requires us to
	 * call dispose() on this object when we no longer need it. We do that in
	 * method {@link #dispose()}.
	 */
	protected final Color RED = new Color(Display.getCurrent(), 255, 0, 0);

	/**
	 * Color object representing the color green. The Color class requires us to
	 * call dispose() on this object when we no longer need it. We do that in
	 * method {@link #dispose()}.
	 */
	protected final Color GREEN = new Color(Display.getCurrent(), 0, 255, 0);

	/**
	 * Color object representing the color yellow. The Color class requires us
	 * to call dispose() on this object when we no longer need it. We do that in
	 * method {@link #dispose()}.
	 */
	protected final Color YELLOW = new Color(Display.getCurrent(), 255, 255, 0);

	/**
	 * Color object representing the color blue. The Color class requires us to
	 * call dispose() on this object when we no longer need it. We do that in
	 * method {@link #dispose()}.
	 */
	protected final Color BLUE = new Color(Display.getCurrent(), 0, 0, 255);

	/**
	 * Color object representing the color magenta. The Color class requires us
	 * to call dispose() on this object when we no longer need it. We do that in
	 * method {@link #dispose()}.
	 */
	protected final Color MAGENTA = new Color(Display.getCurrent(), 255, 0, 255);

	/**
	 * Color object representing the color cyan. The Color class requires us to
	 * call dispose() on this object when we no longer need it. We do that in
	 * method {@link #dispose()}.
	 */
	protected final Color CYAN = new Color(Display.getCurrent(), 0, 255, 255);

	/**
	 * Color object representing the color white. The Color class requires us to
	 * call dispose() on this object when we no longer need it. We do that in
	 * method {@link #dispose()}.
	 */
	protected final Color WHITE = new Color(Display.getCurrent(), 255, 255, 255);

	protected boolean fLimitOutput;
	protected int fBufferLineLimit;
	/**
	 * The constructor.
	 */
	public TerminalText(ITerminalControlForText terminal) {
		super();

		Logger.log("entered"); //$NON-NLS-1$

		this.terminal = terminal;

		for (int i = 0; i < ansiParameters.length; ++i) {
			ansiParameters[i] = new StringBuffer();
		}
	}

	/**
	 * @param ctlText Sets the styled text. 
	 * 
	 * <p><b>Note:</b>This method can only be called once.
	 */
	public void setStyledText(StyledText ctlText) {
		if(text!=null)
			throw new java.lang.IllegalStateException("Text can be set only once"); //$NON-NLS-1$
		text=ctlText;

		// Register this class instance as a ControlListener so we can learn
		// when the StyledText widget is resized.

		text.addControlListener(this);

		currentForegroundColor = text.getForeground();
		currentBackgroundColor = text.getBackground();
		currentFontStyle = SWT.NORMAL;
		reverseVideo = false;
	}


	/**
	 * This method performs clean up when this TerminalText object is no longer
	 * needed. After calling this method, no other method on this object should
	 * be called.
	 */
	public void dispose() {
		Logger.log("entered"); //$NON-NLS-1$

		// Call dispose() on the Color objects we created.

		BLACK.dispose();
		RED.dispose();
		GREEN.dispose();
		YELLOW.dispose();
		BLUE.dispose();
		MAGENTA.dispose();
		CYAN.dispose();
		WHITE.dispose();
	}

	/**
	 * This method is required by interface ControlListener. It allows us to
	 * know when the StyledText widget is moved.
	 */
	public void controlMoved(ControlEvent event) {
		Logger.log("entered"); //$NON-NLS-1$
		// Empty.
	}

	/**
	 * This method is required by interface ControlListener. It allows us to
	 * know when the StyledText widget is resized. This method must be
	 * synchronized to prevent it from executing at the same time as run(),
	 * which displays new text. We can't have the fields that represent the
	 * dimensions of the terminal changing while we are rendering text.
	 */
	public synchronized void controlResized(ControlEvent event) {
		Logger.log("entered"); //$NON-NLS-1$
		adjustTerminalDimensions();
	}

	/**
	 * This method sets field {@link #newText} to a new value. This method must
	 * not execute at the same time as methods {@link #run()} and {@link
	 * #clearTerminal()}.
	 * <p>
	 * 
	 * IMPORTANT: This method must be called in strict alternation with method
	 * {@link #run()}.
	 * <p>
	 * 
	 * @param newBuffer
	 *            The new buffer containing characters received from the remote
	 *            host.
	 */
	public synchronized void setNewText(StringBuffer newBuffer) {
		if (Logger.isLogEnabled()) {
			Logger.log("new text: '" + Logger.encode(newBuffer.toString()) + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		newText = newBuffer;

		// When continuous output is being processed by the Terminal view code, it
		// consumes nearly 100% of the CPU. This fixes that. If this method is called
		// too frequently, we explicitly sleep for a short time so that the thread
		// executing this function (which is the thread reading from the socket or
		// serial port) doesn't consume 100% of the CPU. Without this code, the
		// Workbench GUI is practically hung when there is continuous output in the
		// Terminal view.

		long CurrentTime = System.currentTimeMillis();

		if (CurrentTime - LastNewOutputTime < 250 && newBuffer.length() > 10) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException ex) {
				// Ignore.
			}
		}

		LastNewOutputTime = CurrentTime;
	}

	/**
	 * This method erases all text from the Terminal view. This method is called
	 * when the user chooses "Clear all" from the Terminal view context menu, so
	 * we need to serialize this method with methods {@link #run()} and {@link
	 * #setNewText(StringBuffer)}.
	 */
	public synchronized void clearTerminal() {
		Logger.log("entered"); //$NON-NLS-1$
		text.setText(""); //$NON-NLS-1$
		cursorColumn = 0;
	}

	/**
	 * This method is called when the user changes the Terminal view's font. We
	 * attempt to recompute the pixel width of the new font's characters and fix
	 * the terminal's dimensions. This method must be synchronized to prevent it
	 * from executing at the same time as run(), which displays new text. We
	 * can't have the fields that represent the dimensions of the terminal
	 * changing while we are rendering text.
	 */
	public synchronized void fontChanged() {
		Logger.log("entered"); //$NON-NLS-1$

		characterPixelWidth = 0;

		if (text != null)
			adjustTerminalDimensions();
	}

	/**
	 * This method executes in the Display thread to process data received from
	 * the remote host by classes {@link TelnetConnection} and {@link
	 * SerialPortHandler}. This method must not execute at the same time
	 * as methods {@link #setNewText(StringBuffer)} and {@link #clearTerminal()}.
	 * <p>
	 * 
	 * IMPORTANT: This method must be called in strict alternation with method
	 * {@link #setNewText(StringBuffer)}.
	 * <p>
	 */
	public synchronized void run() {
		Logger.log("entered"); //$NON-NLS-1$

		try {
			// This method can be called just after the user closes the view, so we
			// make sure not to cause a widget-disposed exception.

			if (text != null && text.isDisposed())
				return;

			// If the status bar is showing "OPENED", change it to "CONNECTED".

			if (terminal.getState()==TerminalState.OPENED) {
				// TODO Why????
				terminal.setState(TerminalState.CONNECTED);
			}

			// Find the width and height of the terminal, and resize it to display an
			// integral number of lines and columns.

			adjustTerminalDimensions();

			// Restore the caret offset, process and display the new text, then save
			// the caret offset. See the documentation for field caretOffset for
			// details.

			// ISSUE: Is this causing the scroll-to-bottom-on-output behavior?

			text.setCaretOffset(caretOffset);

			processNewText();

			caretOffset = text.getCaretOffset();
		} catch (Exception ex) {
			Logger.logException(ex);
		}
	}

	/**
	 * This method scans the newly received text, processing ANSI control
	 * characters and escape sequences and displaying normal text.
	 */
	protected void processNewText() {
		Logger.log("entered"); //$NON-NLS-1$

		// Stop the StyledText widget from redrawing while we manipulate its contents.
		// This helps display performance.

		text.setRedraw(false);

		// Scan the newly received text.

		characterIndex = 0;

		while (characterIndex < newText.length()) {
			char character = newText.charAt(characterIndex);

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
					break;

				case '\r':
					processCarriageReturn(); // Carriage Return (Control-M)
					break;

				case '\u001b':
					ansiState = ANSISTATE_ESCAPE; // Escape.
					break;

				default:
					processNonControlCharacters();
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

				case '7':
					// Save cursor position and character attributes

					ansiState = ANSISTATE_INITIAL;
					savedCursorLine = absoluteCursorLine();
					savedCursorColumn = cursorColumn;
					break;

				case '8':
					// Restore cursor and attributes to previously saved
					// position

					ansiState = ANSISTATE_INITIAL;
					moveCursor(savedCursorLine, savedCursorColumn);
					break;

				default:
					Logger
							.log("Unsupported escape sequence: escape '" + character + "'"); //$NON-NLS-1$ //$NON-NLS-2$
					ansiState = ANSISTATE_INITIAL;
					break;
				}
				break;

			case ANSISTATE_EXPECTING_PARAMETER_OR_COMMAND:
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

			default:
				// This should never happen! If it does happen, it means there is a
				// bug in the FSA. For robustness, we return to the initial
				// state.

				Logger.log("INVALID ANSI FSA STATE: " + ansiState); //$NON-NLS-1$
				ansiState = ANSISTATE_INITIAL;
				break;
			}

			++characterIndex;
		}

		// Allow the StyledText widget to redraw itself.

		text.setRedraw(true);
	}

	/**
	 * This method is called when we have parsed an OS Command escape sequence.
	 * The only one we support is "\e]0;...\u0007", which sets the terminal
	 * title.
	 */
	protected void processAnsiOsCommand() {
		if (ansiOsCommand.charAt(0) != '0' || ansiOsCommand.charAt(1) != ';') {
			Logger
					.log("Ignoring unsupported ANSI OSC sequence: '" + ansiOsCommand + "'"); //$NON-NLS-1$ //$NON-NLS-2$
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
	protected void processAnsiCommandCharacter(char ansiCommandCharacter) {
		// If the width or height of the terminal is ridiculously small (one line or
		// column or less), don't even try to process the escape sequence. This avoids
		// throwing an exception (SPR 107450). The display will be messed up, but what
		// did you user expect by making the terminal so small?

		if (heightInLines <= 1 || widthInColumns <= 1)
			return;

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

		case 'S':
			// Scroll up.
			// Emacs, vi, and GNU readline don't seem to use this command, so we ignore
			// it for now.
			break;

		case 'T':
			// Scroll down.
			// Emacs, vi, and GNU readline don't seem to use this command, so we ignore
			// it for now.
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
	 * This method makes room for N characters on the current line at the cursor
	 * position. Text under the cursor moves right without wrapping at the end
	 * of hte line.
	 */
	protected void processAnsiCommand_atsign() {
		int charactersToInsert = getAnsiParameter(0);
		int caretOffset = text.getCaretOffset();

		text.replaceTextRange(caretOffset, 0, generateString(' ',
				charactersToInsert));

		// If the current line extends past the right edge of the screen, delete the
		// characters beyond the rightmost visible column.

		int currentLineAbsolute = absoluteCursorLine();
		int currentLineStartOffset = text.getOffsetAtLine(currentLineAbsolute);
		int currentLineEndOffset;

		if (currentLineAbsolute == text.getLineCount() - 1) {
			// The cursor is on the bottommost line of text.

			currentLineEndOffset = text.getCharCount();
		} else {
			// The cursor is not on the bottommost line of text.

			currentLineEndOffset = text
					.getOffsetAtLine(currentLineAbsolute + 1) - 1;
		}

		if (currentLineEndOffset - currentLineStartOffset > widthInColumns) {
			int charactersToDelete = currentLineEndOffset
					- currentLineStartOffset - widthInColumns;

			text.replaceTextRange(currentLineStartOffset + widthInColumns,
					charactersToDelete, ""); //$NON-NLS-1$
		}

		// Is this necessary?

		text.setCaretOffset(caretOffset);
	}

	/**
	 * This method moves the cursor up by the number of lines specified by the
	 * escape sequence parameter (default 1).
	 */
	protected void processAnsiCommand_A() {
		moveCursorUp(getAnsiParameter(0));
	}

	/**
	 * This method moves the cursor down by the number of lines specified by the
	 * escape sequence parameter (default 1).
	 */
	protected void processAnsiCommand_B() {
		moveCursorDown(getAnsiParameter(0));
	}

	/**
	 * This method moves the cursor forward by the number of columns specified
	 * by the escape sequence parameter (default 1).
	 */
	protected void processAnsiCommand_C() {
		moveCursorForward(getAnsiParameter(0));
	}

	/**
	 * This method moves the cursor backward by the number of columns specified
	 * by the escape sequence parameter (default 1).
	 */
	protected void processAnsiCommand_D() {
		moveCursorBackward(getAnsiParameter(0));
	}

	/**
	 * This method moves the cursor to the first column of the Nth next line,
	 * where N is specified by the ANSI parameter (default 1).
	 */
	protected void processAnsiCommand_E() {
		int linesToMove = getAnsiParameter(0);

		moveCursor(relativeCursorLine() + linesToMove, 0);
	}

	/**
	 * This method moves the cursor to the first column of the Nth previous
	 * line, where N is specified by the ANSI parameter (default 1).
	 */
	protected void processAnsiCommand_F() {
		int linesToMove = getAnsiParameter(0);

		moveCursor(relativeCursorLine() - linesToMove, 0);
	}

	/**
	 * This method moves the cursor within the current line to the column
	 * specified by the ANSI parameter (default is column 1).
	 */
	protected void processAnsiCommand_G() {
		int targetColumn = 1;

		if (ansiParameters[0].length() > 0)
			targetColumn = getAnsiParameter(0) - 1;

		moveCursor(relativeCursorLine(), targetColumn);
	}

	/**
	 * This method sets the cursor to a position specified by the escape
	 * sequence parameters (default is the upper left corner of the screen).
	 */
	protected void processAnsiCommand_H() {
		moveCursor(getAnsiParameter(0) - 1, getAnsiParameter(1) - 1);
	}

	/**
	 * This method deletes some (or all) of the text on the screen without
	 * moving the cursor.
	 */
	protected void processAnsiCommand_J() {
		int ansiParameter;

		if (ansiParameters[0].length() == 0)
			ansiParameter = 0;
		else
			ansiParameter = getAnsiParameter(0);

		switch (ansiParameter) {
		case 0:
			// Erase from current position to end of screen (inclusive).

			int caretOffset = text.getCaretOffset();

			text.replaceTextRange(caretOffset, text.getCharCount()
					- caretOffset, generateString('\n', heightInLines
					- relativeCursorLine() - 1));

			// The above call moves the caret to the end of the text, so restore its
			// position.

			text.setCaretOffset(caretOffset);
			break;

		case 1:
			// Erase from beginning to current position (inclusive).

			int currentRelativeLineNumber = relativeCursorLine();
			int topmostScreenLineStartOffset = text
					.getOffsetAtLine(absoluteLine(0));

			text.replaceTextRange(topmostScreenLineStartOffset, text
					.getCaretOffset()
					- topmostScreenLineStartOffset, generateString('\n',
					currentRelativeLineNumber)
					+ generateString(' ', cursorColumn));

			text.setCaretOffset(topmostScreenLineStartOffset
					+ currentRelativeLineNumber + cursorColumn);
			break;

		case 2:
			// Erase entire display.

			int currentLineNumber = relativeCursorLine();
			topmostScreenLineStartOffset = text
					.getOffsetAtLine(absoluteLine(0));

			text.replaceTextRange(topmostScreenLineStartOffset, text
					.getCharCount()
					- topmostScreenLineStartOffset, generateString('\n',
					heightInLines - 1));

			moveCursor(currentLineNumber, cursorColumn);
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
	protected void processAnsiCommand_K() {
		int ansiParameter = getAnsiParameter(0);
		int originalCaretOffset = text.getCaretOffset();

		switch (ansiParameter) {
		case 0:
			// Erase from beginning to current position (inclusive).

			int currentLineStartOffset = text
					.getOffsetAtLine(absoluteCursorLine());

			text.replaceTextRange(currentLineStartOffset, cursorColumn,
					generateString(' ', cursorColumn));
			break;

		case 1:
			// Erase from current position to end (inclusive).

			int caretOffset = text.getCaretOffset();

			if (absoluteCursorLine() == text.getLineCount() - 1) {
				text.replaceTextRange(caretOffset, text.getCharCount()
						- caretOffset, ""); //$NON-NLS-1$
			} else {
				int nextLineStartOffset = text
						.getOffsetAtLine(absoluteCursorLine() + 1);

				text.replaceTextRange(caretOffset, nextLineStartOffset
						- caretOffset - 1, ""); //$NON-NLS-1$
			}
			break;

		case 2:
			// Erase entire line.

			currentLineStartOffset = text.getOffsetAtLine(absoluteCursorLine());

			if (absoluteCursorLine() == text.getLineCount() - 1) {
				// The cursor is on the bottommost line of text. Replace its contents
				// with enough spaces to leave the cursor in the current column.

				text.replaceTextRange(currentLineStartOffset, text
						.getCharCount()
						- currentLineStartOffset, generateString(' ',
						cursorColumn));
			} else {
				// The cursor is not on the bottommost line of text. Replace the
				// current line's contents with enough spaces to leave the cursor in
				// the current column.

				int nextLineStartOffset = text
						.getOffsetAtLine(absoluteCursorLine() + 1);

				text.replaceTextRange(currentLineStartOffset,
						nextLineStartOffset - currentLineStartOffset - 1,
						generateString(' ', cursorColumn));
			}
			break;

		default:
			Logger.log("Unexpected K-command parameter: " + ansiParameter); //$NON-NLS-1$
			break;
		}

		// There is some undocumented strangeness with method
		// StyledText.replaceTextRange() that requires us to manually reposition the
		// caret after calling that method. If we don't do this, the caret sometimes
		// moves to the very end of the text when deleting text within a line.

		text.setCaretOffset(originalCaretOffset);
	}

	/**
	 * Insert one or more blank lines. The current line of text moves down. Text
	 * that falls off the bottom of the screen is deleted.
	 */
	protected void processAnsiCommand_L() {
		int linesToInsert = getAnsiParameter(0);

		int currentLineStartOffset = text.getOffsetAtLine(absoluteCursorLine());

		// Compute how many of the bottommost lines of text to delete. This is
		// necessary if those lines are being pushed off the bottom of the screen by
		// the insertion of the blank lines.

		int totalLines = text.getLineCount();
		int linesToDelete = -1;

		if (heightInLines <= totalLines) {
			// There are more lines of text than are displayed, so delete as many lines
			// at the end as we insert in the middle.

			linesToDelete = linesToInsert;
		} else {
			// There are fewer lines of text than the size of the terminal window, so
			// compute how many lines will be pushed off the end of the screen by the
			// insertion. NOTE: It is possible that we may not have to delete any
			// lines at all, which will leave linesToDelete set to -1.

			if (totalLines + linesToInsert > heightInLines) {
				linesToDelete = (totalLines + linesToInsert) - heightInLines;
			}
		}

		if (linesToDelete != -1) {
			// Delete the bottomost linesToInsert lines plus the newline on the line
			// immediately above the first line to be deleted.

			int firstLineToDeleteStartOffset = text.getOffsetAtLine(totalLines
					- linesToDelete);

			text.replaceTextRange(firstLineToDeleteStartOffset - 1, text
					.getCharCount()
					- firstLineToDeleteStartOffset + 1, ""); //$NON-NLS-1$
		}

		// Insert the new blank lines, leaving the cursor on the topmost of the new
		// blank lines.

		int totalCharacters = text.getCharCount();

		if (currentLineStartOffset > totalCharacters) {
			// We are inserting the blank lines at the very end of the text, so
			// currentLineStartOffset is now out of range. It will be be in range
			// again after these newlines are appended.

			text.replaceTextRange(totalCharacters, 0, generateString('\n',
					linesToInsert));
		} else {
			// We are inserting the blank lines in the middle of the text, so
			// currentLineStartOffset is not out of range.

			text.replaceTextRange(currentLineStartOffset, 0, generateString(
					'\n', linesToInsert));
		}

		text.setCaretOffset(currentLineStartOffset);
	}

	/**
	 * Delete one or more lines of text. Any lines below the deleted lines move
	 * up, which we implmement by appending newlines to the end of the text.
	 */
	protected void processAnsiCommand_M() {
		int totalLines = text.getLineCount();
		int linesToDelete = getAnsiParameter(0);
		int currentLineAbsolute = absoluteCursorLine();
		int currentLineStartOffset = text.getOffsetAtLine(currentLineAbsolute);

		// Compute the offset of the character after the lines to be deleted. This
		// might be the end of the text.

		if (linesToDelete >= totalLines - currentLineAbsolute) {
			// We are deleting all the lines to the bottom of the text. Replace them
			// with blank lines.

			text.replaceTextRange(currentLineStartOffset, text.getCharCount()
					- currentLineStartOffset, generateString('\n', totalLines
					- currentLineAbsolute - 1));
		} else {
			// Delete the next linesToDelete lines.

			int firstUndeletedLineStartOffset = text
					.getOffsetAtLine(currentLineAbsolute + linesToDelete);

			text.replaceTextRange(currentLineStartOffset,
					firstUndeletedLineStartOffset - currentLineStartOffset, ""); //$NON-NLS-1$

			// Add an equal number of blank lines to the end of the text.

			text.replaceTextRange(text.getCharCount(), 0, generateString('\n',
					linesToDelete));
		}

		text.setCaretOffset(currentLineStartOffset);
	}

	/**
	 * This method sets a new graphics rendition mode, such as
	 * foreground/background color, bold/normal text, and reverse video.
	 */
	protected void processAnsiCommand_m() {
		if (ansiParameters[0].length() == 0) {
			// This a special case: when no ANSI parameter is specified, act like a
			// single parameter equal to 0 was specified.

			ansiParameters[0].append('0');
		}

		// There are a non-zero number of ANSI parameters. Process each one in
		// order.

		int totalParameters = ansiParameters.length;
		int parameterIndex = 0;

		while (parameterIndex < totalParameters
				&& ansiParameters[parameterIndex].length() > 0) {
			int ansiParameter = getAnsiParameter(parameterIndex);

			switch (ansiParameter) {
			case 0:
				// Reset all graphics modes.
				currentForegroundColor = text.getForeground();
				currentBackgroundColor = text.getBackground();
				currentFontStyle = SWT.NORMAL;
				reverseVideo = false;
				break;

			case 1:
				currentFontStyle = SWT.BOLD; // Turn on bold.
				break;

			case 7:
				reverseVideo = true; // Reverse video.
				break;

			case 10: // Set primary font. Ignored.
				break;

			case 22:
				currentFontStyle = SWT.NORMAL; // Cancel bold or dim attributes
												// only.
				break;

			case 27:
				reverseVideo = false; // Cancel reverse video attribute only.
				break;

			case 30:
				currentForegroundColor = BLACK; // Foreground is black.
				break;

			case 31:
				currentForegroundColor = RED; // Foreground is red.
				break;

			case 32:
				currentForegroundColor = GREEN; // Foreground is green.
				break;

			case 33:
				currentForegroundColor = YELLOW; // Foreground is yellow.
				break;

			case 34:
				currentForegroundColor = BLUE; // Foreground is blue.
				break;

			case 35:
				currentForegroundColor = MAGENTA; // Foreground is magenta.
				break;

			case 36:
				currentForegroundColor = CYAN; // Foreground is cyan.
				break;

			case 37:
				currentForegroundColor = text.getForeground(); // Foreground is
																// white.
				break;

			case 40:
				currentBackgroundColor = text.getBackground(); // Background is
																// black.
				break;

			case 41:
				currentBackgroundColor = RED; // Background is red.
				break;

			case 42:
				currentBackgroundColor = GREEN; // Background is green.
				break;

			case 43:
				currentBackgroundColor = YELLOW; // Background is yellow.
				break;

			case 44:
				currentBackgroundColor = BLUE; // Background is blue.
				break;

			case 45:
				currentBackgroundColor = MAGENTA; // Background is magenta.
				break;

			case 46:
				currentBackgroundColor = CYAN; // Background is cyan.
				break;

			case 47:
				currentBackgroundColor = WHITE; // Background is white.
				break;

			default:
				Logger
						.log("Unsupported graphics rendition parameter: " + ansiParameter); //$NON-NLS-1$
				break;
			}

			++parameterIndex;
		}
	}

	/**
	 * This method responds to an ANSI Device Status Report (DSR) command from
	 * the remote endpoint requesting the cursor position. Requests for other
	 * kinds of status are ignored.
	 */
	protected void processAnsiCommand_n() {
		// Do nothing if the numeric parameter was not 6 (which means report cursor
		// position).

		if (getAnsiParameter(0) != 6)
			return;

		// Send the ANSI cursor position (which is 1-based) to the remote endpoint.

		String positionReport = "\u001b[" + (relativeCursorLine() + 1) + ";" + //$NON-NLS-1$ //$NON-NLS-2$
				(cursorColumn + 1) + "R"; //$NON-NLS-1$

		OutputStreamWriter streamWriter = new OutputStreamWriter(terminal
				.getOutputStream(), Charset.forName("ISO-8859-1")); //$NON-NLS-1$

		try {
			streamWriter.write(positionReport, 0, positionReport.length());
			streamWriter.flush();
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
	protected void processAnsiCommand_P() {
		int currentLineEndOffset;
		int currentLineAbsolute = absoluteCursorLine();

		if (currentLineAbsolute == text.getLineCount() - 1) {
			// The cursor is on the bottommost line of text.

			currentLineEndOffset = text.getCharCount();
		} else {
			// The cursor is not on the bottommost line of text.

			currentLineEndOffset = text
					.getOffsetAtLine(currentLineAbsolute + 1) - 1;
		}

		int caretOffset = text.getCaretOffset();
		int remainingCharactersOnLine = currentLineEndOffset - caretOffset;

		if (remainingCharactersOnLine > 0) {
			// There are characters that can be deleted.

			int charactersToDelete = getAnsiParameter(0);

			if (charactersToDelete > remainingCharactersOnLine)
				charactersToDelete = remainingCharactersOnLine;

			text.replaceTextRange(caretOffset, charactersToDelete, ""); //$NON-NLS-1$
			text.setCaretOffset(caretOffset);
		}
	}

	/**
	 * This method returns one of the numeric ANSI parameters received in the
	 * most recent escape sequence.
	 * 
	 * @return The <i>parameterIndex</i>th numeric ANSI parameter or -1 if the
	 *         index is out of range.
	 */
	protected int getAnsiParameter(int parameterIndex) {
		if (parameterIndex < 0 || parameterIndex >= ansiParameters.length) {
			// This should never happen.
			return -1;
		}

		String parameter = ansiParameters[parameterIndex].toString();

		if (parameter.length() == 0)
			return 1;

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
	 * sequence. Paramters are the (optional) characters between the leading
	 * "\e[" and the command character in an escape sequence (e.g., in the
	 * escape sequence "\e[20;10H", the paramter characters are "20;10").
	 * Parameters are integers separated by one or more ';'s.
	 */
	protected void processAnsiParameterCharacter(char ch) {
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
	 */
	protected void processNonControlCharacters() {
		int firstNonControlCharacterIndex = characterIndex;
		int newTextLength = newText.length();
		char character = newText.charAt(characterIndex);

		// Identify a contiguous sequence of non-control characters, starting at
		// firstNonControlCharacterIndex in newText.

		while (character != '\u0000' && character != '\b' && character != '\t'
				&& character != '\u0007' && character != '\n'
				&& character != '\r' && character != '\u001b') {
			++characterIndex;

			if (characterIndex >= newTextLength)
				break;

			character = newText.charAt(characterIndex);
		}

		// Move characterIndex back by one character because it gets incremented at the
		// bottom of the loop in processNewText().

		--characterIndex;

		int preDisplayCaretOffset = text.getCaretOffset();

		// Now insert the sequence of non-control characters in the StyledText widget
		// at the location of the cursor.

		displayNewText(firstNonControlCharacterIndex, characterIndex);

		// If any one of the current font style, foreground color or background color
		// differs from the defaults, apply the current style to the newly displayed
		// text. Since this method is only called for a contiguous sequence of
		// non-control characters, the current style applies to the entire
		// sequence of characters.

		if (!currentForegroundColor.equals(text.getForeground())
				|| !currentBackgroundColor.equals(text.getBackground())
				|| currentFontStyle != SWT.NORMAL || reverseVideo == true) {
			StyleRange style = new StyleRange(preDisplayCaretOffset, text
					.getCaretOffset()
					- preDisplayCaretOffset,
					reverseVideo ? currentBackgroundColor
							: currentForegroundColor,
					reverseVideo ? currentForegroundColor
							: currentBackgroundColor, currentFontStyle);

			text.setStyleRange(style);
		}
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
	 * 
	 * @param first
	 *            The index (within newText) of the first character to display.
	 * @param last
	 *            The index (within newText) of the last character to display.
	 */
	protected void displayNewText(int first, int last) {
		if (text.getCaretOffset() == text.getCharCount()) {
			// The cursor is at the very end of the terminal's text, so we append the
			// new text to the StyledText widget.

			displayNewTextByAppending(first, last);
		} else {
			// The cursor is not at the end of the screen's text, so we have to
			// overwrite existing text.

			displayNewTextByOverwriting(first, last);
		}
	}

	/**
	 * This method displays new text by appending it to the end of the existing
	 * text, wrapping text that extends past the right edge of the screen.
	 * <p>
	 * 
	 * There are never any ANSI control characters or escape sequences in the
	 * text being displayed by this method (this includes newlines, carriage
	 * returns, and tabs).
	 * <p>
	 * 
	 * @param first
	 *            The index (within newText) of the first character to display.
	 * @param last
	 *            The index (within newText) of the last character to display.
	 */
	protected void displayNewTextByAppending(int first, int last) {
		int numCharsToOutput = last - first + 1;
		int availableSpaceOnLine = widthInColumns - cursorColumn;

		if (numCharsToOutput >= availableSpaceOnLine) {
			// We need to wrap the text, because it's longer than the available
			// space on the current line. First, appends as many characters as
			// will fit in the space remaining on the current line.
			//
			// NOTE: We don't line wrap the text in this method the same way we line
			// wrap the text in method displayNewTextByOverwriting(), but this is by far
			// the most common case, and it has to run the fastest.

			text.append(newText.substring(first, first + availableSpaceOnLine));
			first += availableSpaceOnLine;

			processCarriageReturn();
			processNewline();

			while (first <= last) {
				availableSpaceOnLine = widthInColumns;

				if (availableSpaceOnLine > last - first + 1) {
					text.append(newText.substring(first, last + 1));
					cursorColumn = last - first + 1;
					break;
				} else {
					text.append(newText.substring(first, first
							+ availableSpaceOnLine));
					first += availableSpaceOnLine;

					processCarriageReturn();
					processNewline();
				}
			}
		} else {
			// We don't need to wrap the text.

			text.append(newText.substring(first, last + 1));
			cursorColumn += last - first + 1;
		}
	}

	/**
	 * This method displays new text by overwriting existing text, wrapping text
	 * that extends past the right edge of the screen.
	 * <p>
	 * 
	 * There are never any ANSI control characters or escape sequences in the
	 * text being displayed by this method (this includes newlines, carriage
	 * returns, and tabs).
	 * <p>
	 * 
	 * @param first
	 *            The index (within newText) of the first character to display.
	 * @param last
	 *            The index (within newText) of the last character to display.
	 */
	protected void displayNewTextByOverwriting(int first, int last) {
		// First, break new text into segments, based on where it needs to line wrap,
		// so that each segment contains text that will appear on a separate
		// line.

		List textSegments = new ArrayList(100);

		int availableSpaceOnLine = widthInColumns - cursorColumn;

		while (first <= last) {
			String segment;

			if (last - first + 1 > availableSpaceOnLine)
				segment = newText
						.substring(first, first + availableSpaceOnLine);
			else
				segment = newText.substring(first, last + 1);

			textSegments.add(segment);

			first += availableSpaceOnLine;
			availableSpaceOnLine = widthInColumns;
		}

		// Next, for each segment, if the cursor is at the end of the text, append the
		// segment along with a newline character. If the cursor is not at the end of
		// the text, replace the next N characters starting at the cursor position with
		// the segment, where N is the minimum of the length of the segment or the
		// length of the rest of the current line.

		Iterator iter = textSegments.iterator();

		while (iter.hasNext()) {
			String segment = (String) iter.next();
			int caretOffset = text.getCaretOffset();

			if (caretOffset == text.getCharCount()) {
				// The cursor is at the end of the text, so just append the current
				// segement along with a newline.

				text.append(segment);

				// If there is another segment to display, move the cursor to a new
				// line.

				if (iter.hasNext()) {
					processCarriageReturn();
					processNewline();
				}
			} else {
				// The cursor is not at the end of the text, so replace some or all of
				// the text following the cursor on the current line with the current
				// segment.

				int numCharactersAfterCursorOnLine;

				if (absoluteCursorLine() == text.getLineCount() - 1) {
					// The cursor is on the last line of text.
					numCharactersAfterCursorOnLine = text.getCharCount()
							- caretOffset;
				} else {
					// The cursor is not on the last line of text.
					numCharactersAfterCursorOnLine = text
							.getOffsetAtLine(absoluteCursorLine() + 1)
							- caretOffset - 1;
				}

				int segmentLength = segment.length();
				int numCharactersToReplace;

				if (segmentLength < numCharactersAfterCursorOnLine)
					numCharactersToReplace = segmentLength;
				else
					numCharactersToReplace = numCharactersAfterCursorOnLine;

				text.replaceTextRange(caretOffset, numCharactersToReplace,
						segment);
				text.setCaretOffset(caretOffset + segmentLength);
				cursorColumn += segmentLength;

				// If there is another segment, move the cursor to the start of
				// the
				// next line.

				if (iter.hasNext()) {
					cursorColumn = 0;
					text.setCaretOffset(caretOffset + segmentLength + 1);
				} else {
					// We just inserted the last segment. If the current line is full,
					// wrap the cursor onto a new line.

					if (cursorColumn == widthInColumns) {
						processCarriageReturn();
						processNewline();
					}
				}
			}
		}
	}

	/**
	 * Process a BEL (Control-G) character.
	 */
	protected void processBEL() {
		// ISSUE: Is there a better way to make a sound? This is not guaranteed to
		// work on all platforms.
		// TODO
		java.awt.Toolkit.getDefaultToolkit().beep();
	}

	/**
	 * Process a backspace (Control-H) character.
	 */
	protected void processBackspace() {
		moveCursorBackward(1);
	}

	/**
	 * Process a tab (Control-I) character. We don't insert a tab character into
	 * the StyledText widget. Instead, we move the cursor forward to the next
	 * tab stop, without altering any of the text. Tab stops are every 8
	 * columns. The cursor will never move past the rightmost column.
	 */
	protected void processTab() {
		moveCursorForward(8 - (cursorColumn % 8));
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
	protected void processNewline() {
		int totalLines = text.getLineCount();
		int currentLineAbsolute = absoluteCursorLine();

		if (currentLineAbsolute < totalLines - 1) {
			// The cursor is not on the bottommost line of text, so we move the cursor
			// to the same column on the next line.

			// TODO: If we can verify that the next character is a carriage return, we
			// can optimize out the insertion of spaces that moveCursorDown()
			// will do.

			moveCursorDown(1);
		} else if (currentLineAbsolute == totalLines - 1) {
			// The cursor is on the bottommost line of text, so we append a newline
			// character to the end of the terminal's text (creating a new line on the
			// screen) and insert cursorColumn spaces.

			text.append("\n"); //$NON-NLS-1$
			text.append(generateString(' ', cursorColumn));
			text.setCaretOffset(text.getCharCount());

			// We may have scrolled a line off the top of the screen, so check
			// if we
			// need to delete some of the the oldest lines in the scroll buffer.

			deleteTopmostLines();
		} else {
			// This should _never_ happen. If it does happen, it is a bug in this
			// algorithm.

			Logger.log("SHOULD NOT BE REACHED!"); //$NON-NLS-1$
		}
	}

	/**
	 * Process a Carriage Return (Control-M).
	 */
	protected void processCarriageReturn() {
		// Move the cursor to the beginning of the current line.

		text.setCaretOffset(text.getOffsetAtLine(text.getLineAtOffset(text
				.getCaretOffset())));
		cursorColumn = 0;
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
	protected void adjustTerminalDimensions() {
		// Compute how many pixels we need to shrink the StyledText control vertically
		// to make it display an integral number of lines of text.

		int linePixelHeight = text.getLineHeight();
		Point textWindowDimensions = text.getSize();
		int verticalPixelsToShrink = textWindowDimensions.y % linePixelHeight;

		// Compute the current height of the terminal in lines.

		heightInLines = textWindowDimensions.y / linePixelHeight;

		// Compute how many pixels we need to shrink the StyledText control to make
		// it display an integral number of columns of text. We can only do this if we
		// know the pixel width of a character in the font used by the StyledText
		// widget.

		int horizontalPixelsToShrink = 0;

		if (characterPixelWidth == 0)
			computeCharacterPixelWidth();

		if (characterPixelWidth != 0) {
			horizontalPixelsToShrink = textWindowDimensions.x
					% characterPixelWidth;

			// The width of the StyledText widget that text.getSize() returns includes
			// the space occupied by the vertical scrollbar, so we have to fudge this
			// calculation (by subtracting 3 columns) to account for the presence of
			// the scrollbar. Ugh.

			widthInColumns = textWindowDimensions.x / characterPixelWidth - 3;
		}

		// If necessary, resize the text widget.

		if (verticalPixelsToShrink > 0 || horizontalPixelsToShrink > 0) {
			// Remove this class instance from being a ControlListener on the
			// StyledText widget, because we are about to resize and move the widget,
			// and we don't want this method to be recursively invoked.

			text.removeControlListener(this);

			// Shrink the StyledText control so that it displays an integral number
			// of lines of text and an integral number of columns of text.

			textWindowDimensions.y -= verticalPixelsToShrink;
			textWindowDimensions.x -= horizontalPixelsToShrink;
			text.setSize(textWindowDimensions);

			// Move the StyledText control down by the same number of pixels that
			// we just shrank it vertically and right by the same number of pixels that
			// we just shrank it horizontally. This makes the padding appear to the
			// left and top of the widget, which is more visually appealing. This is
			// only necessary because there is no way to programmatically shrink the
			// view itself.

			Point textLocation = text.getLocation();
			textLocation.y += verticalPixelsToShrink;
			textLocation.x += horizontalPixelsToShrink;
			text.setLocation(textLocation);

			// Restore this class instance as the ControlListener on the StyledText
			// widget so we know when the user resizes the Terminal view.

			text.addControlListener(this);

			// Make sure the exposed portion of the Composite canvas behind the
			// StyledText control matches the background color of the StyledText
			// control.

			Color textBackground = text.getBackground();
			text.getParent().setBackground(textBackground);

			// Scroll the StyledText widget to the bottommost position.

			text.setSelectionRange(text.getCharCount(), 0);
			text.showSelection();

			// Tell the parent object to redraw itself. This erases any partial
			// line of text that might be left visible where the parent object is
			// now exposed. This call only happens if the size needed to be changed,
			// so it should not cause any flicker.

			text.getParent().redraw();
		}

		// If we are in a TELNET connection and we know the dimensions of the terminal,
		// we give the size information to the TELNET connection object so it can
		// communicate it to the TELNET server. If we are in a serial connection,
		// there is nothing we can do to tell the remote host about the size of the
		// terminal.

		ITerminalConnector telnetConnection = terminal.getTerminalConnection();

		if (telnetConnection != null && widthInColumns != 0 && heightInLines != 0) {
			telnetConnection.setTerminalSize(widthInColumns, heightInLines);
		}
	}

	/**
	 * This method computes the the pixel width of a character in the current
	 * font. The Font object representing the font in the Terminal view doesn't
	 * provide the pixel width of the characters (even for a fixed width font).
	 * Instead, we get the pixel coordinates of the upper left corner of the
	 * bounding boxes for two adjacent characters on the same line and subtract
	 * the X coordinate of one from the X coordinate of the other. Simple, no?
	 */
	protected void computeCharacterPixelWidth() {
		// We can't assume there is any text in the terminal, so make sure there's at
		// least two characters.

		text.replaceTextRange(0, 0, "   "); //$NON-NLS-1$

		Point firstCharLocation = text.getLocationAtOffset(0);
		Point secondCharLocation = text.getLocationAtOffset(1);

		characterPixelWidth = secondCharLocation.x - firstCharLocation.x;

		text.replaceTextRange(0, 3, ""); //$NON-NLS-1$
	}

	/**
	 * This method deletes as many of the topmost lines of text as needed to
	 * keep the total number of lines of text in the Terminal view less than or
	 * equal to the limit configured in the preferences. If no limit is
	 * configured, this method does nothing.
	 */
	protected void deleteTopmostLines() {
		if (!fLimitOutput)
			return;

		// Compute the number of lines to delete, but don't do anything if there are
		// fewer lines in the terminal than the height of the terminal in lines.

		int totalLineCount = text.getLineCount();

		if (totalLineCount <= heightInLines)
			return;

		int bufferLineLimit = fBufferLineLimit;

		// Don't allow the user to set the buffer line limit to less than the height of
		// the terminal in lines.

		if (bufferLineLimit <= heightInLines)
			bufferLineLimit = heightInLines + 1;

		int linesToDelete = totalLineCount - bufferLineLimit;

		// Delete the lines. A small optimization here: don't do anything unless
		// there's at least 5 lines to delete.

		if (linesToDelete >= 5)
			text.replaceTextRange(0, text.getOffsetAtLine(linesToDelete), ""); //$NON-NLS-1$
	}

	/**
	 * This method returns the absolute line number of the line containing the
	 * cursor. The very first line of text (even if it is scrolled off the
	 * screen) is absolute line number 0.
	 * 
	 * @return The absolute line number of the line containing the cursor.
	 */
	protected int absoluteCursorLine() {
		return text.getLineAtOffset(text.getCaretOffset());
	}

	/**
	 * This method returns the relative line number of the line comtaining the
	 * cursor. The returned line number is relative to the topmost visible line,
	 * which has relative line number 0.
	 * 
	 * @return The relative line number of the line containing the cursor.
	 */
	protected int relativeCursorLine() {
		int totalLines = text.getLineCount();

		if (totalLines <= heightInLines)
			return text.getLineAtOffset(text.getCaretOffset());

		return absoluteCursorLine() - totalLines + heightInLines;
	}

	/**
	 * This method converts a visible line number (i.e., line 0 is the topmost
	 * visible line if the terminal is scrolled all the way down, and line
	 * number heightInLines - 1 is the bottommost visible line if the terminal
	 * is scrolled all the way down) to a line number as known to the StyledText
	 * widget.
	 */
	protected int absoluteLine(int visibleLineNumber) {
		int totalLines = text.getLineCount();

		if (totalLines <= heightInLines)
			return visibleLineNumber;

		return totalLines - heightInLines + visibleLineNumber;
	}

	/**
	 * This method returns a String containing <i>count</i> <i>ch</i>
	 * characters.
	 * 
	 * @return A String containing <i>count</i> <i>ch</i> characters.
	 */
	protected String generateString(char ch, int count) {
		char[] chars = new char[count];

		for (int i = 0; i < chars.length; ++i)
			chars[i] = ch;

		return new String(chars);
	}

	/**
	 * This method moves the cursor to the specified line and column. Parameter
	 * <i>targetLine</i> is the line number of a screen line, so it has a
	 * minimum value of 0 (the topmost screen line) and a maximum value of
	 * heightInLines - 1 (the bottommost screen line). A line does not have to
	 * contain any text to move the cursor to any column in that line.
	 */
	protected void moveCursor(int targetLine, int targetColumn) {
		// Don't allow out of range target line and column values.

		if (targetLine < 0)
			targetLine = 0;
		if (targetLine >= heightInLines)
			targetLine = heightInLines - 1;

		if (targetColumn < 0)
			targetColumn = 0;
		if (targetColumn >= widthInColumns)
			targetColumn = widthInColumns - 1;

		// First, find out if we need to append newlines to the end of the text. This
		// is necessary if there are fewer total lines of text than visible screen
		// lines and the target line is below the bottommost line of text.

		int totalLines = text.getLineCount();

		if (totalLines < heightInLines && targetLine >= totalLines)
			text.append(generateString('\n', heightInLines - totalLines));

		// Next, compute the offset of the start of the target line.

		int targetLineStartOffset = text
				.getOffsetAtLine(absoluteLine(targetLine));

		// Next, find how many characters are in the target line. Be careful not to
		// index off the end of the StyledText widget.

		int nextLineNumber = absoluteLine(targetLine + 1);
		int targetLineLength;

		if (nextLineNumber >= totalLines) {
			// The target line is the bottommost line of text.

			targetLineLength = text.getCharCount() - targetLineStartOffset;
		} else {
			// The target line is not the bottommost line of text, so compute its
			// length by subtracting the start offset of the target line from the start
			// offset of the following line.

			targetLineLength = text.getOffsetAtLine(nextLineNumber)
					- targetLineStartOffset - 1;
		}

		// Find out if we can just move the cursor without having to insert spaces at
		// the end of the target line.

		if (targetColumn >= targetLineLength) {
			// The target line is not long enough to just move the cursor, so we have
			// to append spaces to it before positioning the cursor.

			int spacesToAppend = targetColumn - targetLineLength;

			text.replaceTextRange(targetLineStartOffset + targetLineLength, 0,
					generateString(' ', spacesToAppend));
		}

		// Now position the cursor.

		text.setCaretOffset(targetLineStartOffset + targetColumn);

		cursorColumn = targetColumn;
	}

	/**
	 * This method moves the cursor down <i>lines</i> lines, but won't move the
	 * cursor past the bottom of the screen. This method does not cause any
	 * scrolling.
	 */
	protected void moveCursorDown(int lines) {
		moveCursor(relativeCursorLine() + lines, cursorColumn);
	}

	/**
	 * This method moves the cursor up <i>lines</i> lines, but won't move the
	 * cursor past the top of the screen. This method does not cause any
	 * scrolling.
	 */
	protected void moveCursorUp(int lines) {
		moveCursor(relativeCursorLine() - lines, cursorColumn);
	}

	/**
	 * This method moves the cursor forward <i>columns</i> columns, but won't
	 * move the cursor past the right edge of the screen, nor will it move the
	 * cursor onto the next line. This method does not cause any scrolling.
	 */
	protected void moveCursorForward(int columnsToMove) {
		moveCursor(relativeCursorLine(), cursorColumn + columnsToMove);
	}

	/**
	 * This method moves the cursor backward <i>columnsToMove</i> columns, but
	 * won't move the cursor past the left edge of the screen, nor will it move
	 * the cursor onto the previous line. This method does not cause any
	 * scrolling.
	 */
	protected void moveCursorBackward(int columnsToMove) {
		// We don't call moveCursor() here, because this is optimized for backward
		// cursor motion on a single line.

		if (columnsToMove > cursorColumn)
			columnsToMove = cursorColumn;

		text.setCaretOffset(text.getCaretOffset() - columnsToMove);

		cursorColumn -= columnsToMove;
	}

	protected int getBufferLineLimit() {
		return fBufferLineLimit;
	}

	protected void setBufferLineLimit(int bufferLineLimit) {
		fBufferLineLimit = bufferLineLimit;
	}

	protected boolean isLimitOutput() {
		return fLimitOutput;
	}

	protected void setLimitOutput(boolean limitOutput) {
		fLimitOutput = limitOutput;
	}
}
