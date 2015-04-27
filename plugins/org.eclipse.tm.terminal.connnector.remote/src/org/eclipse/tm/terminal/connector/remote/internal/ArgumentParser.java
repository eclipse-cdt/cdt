/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.remote.internal;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for managing command line arguments.
 */
public class ArgumentParser {
	private final List<String> tokens;

	/**
	 * Create a command line representation from the string with a shell command
	 * line. The command line is parsed and split on spaces. Quoted or escaped
	 * spaces are preserved..
	 */
	public ArgumentParser(String commandline) {
		this.tokens = parseCommandline(commandline);
	}

	/**
	 * Create a command line representation from an array of strings. The first
	 * element of the array is assumed to be the command, the remaining, the
	 * arguments. The elements are not parsed not (un)escaped., but taked as the
	 * are.
	 */
	public ArgumentParser(String tokenArray[]) {
		this(Arrays.asList(tokenArray));
	}

	/**
	 * Create a command line representation from an array of strings. The first
	 * element of the list is assumed to be the command, the remaining, the
	 * arguments. The elements are not parsed not (un)escaped., but taked as the
	 * are.
	 */
	public ArgumentParser(List<String> tokenList) {
		this.tokens = new ArrayList<String>(tokenList);
	}

	/**
	 * Create a command line representation from the command and an array of
	 * parameters. The elements are not parsed not (un)escaped., but taked as
	 * the are.
	 */
	public ArgumentParser(String command, String parameterArray[]) {
		this(command, Arrays.asList(parameterArray));
	}

	/**
	 * Create a command line representation from the command and an list of
	 * parameters. The elements are not parsed not (un)escaped., but taked as
	 * the are.
	 */
	public ArgumentParser(String command, List<String> parameterList) {
		this.tokens = new ArrayList<String>();
		this.tokens.add(command);
		this.tokens.addAll(parameterList);
	}

	private static List<String> parseCommandline(String commandline) {
		ArrayList<String> result = new ArrayList<String>();
		StringCharacterIterator iterator = new StringCharacterIterator(commandline);

		for (iterator.first(); iterator.current() != CharacterIterator.DONE; iterator.next()) {

			// Restart to skip white space
			if (Character.isWhitespace(iterator.current())) {
				continue;
			}

			// Read token
			StringBuffer buffer = new StringBuffer();
			token_reader: for (; iterator.current() != CharacterIterator.DONE; iterator.next()) {
				char tokenChar = iterator.current();

				// A white space terminates the token
				if (Character.isWhitespace(tokenChar)) {
					break token_reader;
				}

				// Handle character that composes the token
				switch (tokenChar) {
				case '"': {
					/*
					 * Read all text within double quotes or until end of
					 * string. Allows escaping.
					 */
					iterator.next(); // Skip quote
					quoted_reader: while ((iterator.current() != CharacterIterator.DONE) && (iterator.current() != '"')) {
						char innerChar = iterator.current();
						switch (innerChar) {
						case '\\':
							char nextChar = iterator.next();
							switch (nextChar) {
							case CharacterIterator.DONE:
								break quoted_reader;
							case '"':
								// Add the character, but remove the escape
								buffer.append(nextChar);
								iterator.next();
								continue quoted_reader;
							default:
								// Add the character and keep escape
								buffer.append(innerChar);
								buffer.append(nextChar);
								iterator.next();
								continue quoted_reader;
							}
						default:
							buffer.append(innerChar);
							iterator.next();
							continue quoted_reader;
						}
					}
					continue token_reader;
				}
				case '\'': {
					/*
					 * Read all text within single quotes or until end of
					 * string. No escaping.
					 */
					iterator.next(); // Skip the quote
					while ((iterator.current() != CharacterIterator.DONE) && (iterator.current() != '\'')) {
						buffer.append(iterator.current());
						iterator.next();
					}
					continue token_reader;
				}
				case '\\': {
					/*
					 * Read escaped char.
					 */
					char nextChar = iterator.next();
					switch (nextChar) {
					case CharacterIterator.DONE:
						break token_reader;
					case '\n':
						// Ignore newline. Both lines are concatenated.
						continue token_reader;
					default:
						// Add the character, but remove the escape
						buffer.append(nextChar);
						continue token_reader;
					}
				}
				default:
					/*
					 * Any other char, add to the buffer.
					 */
					buffer.append(tokenChar);
					continue token_reader;
				}
			}
			result.add(buffer.toString());
		}

		return result;
	}

	/**
	 * Convert all tokens in a full command line that can be executed in a
	 * shell.
	 * 
	 * @param fullEscape
	 *            If every special character shall be escaped. If false, only
	 *            white spaces are escaped and the shell will interpret the
	 *            special chars. If true, then all special chars are quoted.
	 */
	public String getCommandLine(boolean fullEscape) {
		StringBuffer buffer = new StringBuffer();
		Iterator<String> iterator = this.tokens.iterator();
		boolean first = true;
		while (iterator.hasNext()) {
			String token = iterator.next();
			if (!first) {
				buffer.append(' ');
			} else {
				first = false;
			}
			buffer.append(escapeToken(token, fullEscape));
		}
		return buffer.toString();
	}

	private StringBuffer escapeToken(String token, boolean fullEscape) {
		StringBuffer buffer = new StringBuffer();
		StringCharacterIterator iter = new StringCharacterIterator(token);
		for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
			if (Character.isWhitespace(c)) {
				buffer.append('\\');
				buffer.append(c);
				continue;
			}
			switch (c) {
			case '(':
			case ')':
			case '[':
			case ']':
			case '{':
			case '}':
			case '|':
			case '\\':
			case '*':
			case '&':
			case '^':
			case '%':
			case '$':
			case '#':
			case '@':
			case '!':
			case '~':
			case '`':
			case '\'':
			case '"':
			case ':':
			case ';':
			case '?':
			case '>':
			case '<':
			case '\n':
				if (fullEscape) {
					buffer.append('\\');
				}
				buffer.append(c);
				continue;
			case ' ':
				buffer.append('\\');
				buffer.append(c);
				continue;
			default:
				buffer.append(c);
				continue;
			}
		}
		return buffer;
	}

	/**
	 * Returns a List of all entries of the command line.
	 * 
	 * @return The Array
	 */
	public String[] getTokenArray() {
		return this.tokens.toArray(new String[this.tokens.size()]);
	}

	/**
	 * Returns a List of all entries of the command line.
	 * 
	 * @return The List
	 */
	public List<String> getTokenList() {
		return new ArrayList<String>(this.tokens);
	}

	/**
	 * Returns the command of the command line, assuming that the first entry is
	 * always the command.
	 * 
	 * @return The command or null if the command lines has no command nor
	 *         arguments.
	 */
	public String getCommand() {
		if (this.tokens.size() == 0) {
			return null;
		}
		return this.tokens.get(0);
	}

	/**
	 * Returns the command of the command line, assuming that the first entry is
	 * always the command.
	 * 
	 * @return The command or null if the command lines has no command nor
	 *         arguments.
	 * @param fullEscape
	 *            If every special character shall be escaped. If false, only
	 *            white spaces are escaped and the shell will interpret the
	 *            special chars. If true, then all special chars are quoted.
	 */
	public String getEscapedCommand(boolean fullEscape) {
		if (this.tokens.size() == 0) {
			return null;
		}
		return escapeToken(this.tokens.get(0), fullEscape).toString();
	}

	/**
	 * Returns a list of all arguments, assuming that the first entry is the
	 * command name.
	 * 
	 * @return The Array or null if the command lines has no command nor
	 *         arguments.
	 */
	public String[] getParameterArray() {
		if (this.tokens.size() == 0) {
			return null;
		}
		return this.tokens.subList(1, this.tokens.size()).toArray(new String[this.tokens.size() - 1]);
	}

	/**
	 * Returns a list of all arguments, assuming that the first entry is the
	 * command name.
	 * 
	 * @return The List or null if the command lines has no command nor
	 *         arguments.
	 */
	public List<String> getParameterList() {
		if (this.tokens.size() == 0) {
			return null;
		}
		return new ArrayList<String>(this.tokens.subList(1, this.tokens.size()));
	}

	/**
	 * Returns the total number of entries.
	 * 
	 * @return the total number of entries
	 */
	public int getSize() {
		return this.tokens.size();
	}

	/**
	 * Returns a representation of the command line for debug purposes.
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<"); //$NON-NLS-1$
		Iterator<String> iterator = this.tokens.iterator();
		boolean first = true;
		while (iterator.hasNext()) {
			String token = iterator.next();
			if (!first) {
				buffer.append('\n');
			} else {
				first = false;
			}
			buffer.append(token);
		}
		buffer.append(">"); //$NON-NLS-1$
		return buffer.toString();
	}
}
