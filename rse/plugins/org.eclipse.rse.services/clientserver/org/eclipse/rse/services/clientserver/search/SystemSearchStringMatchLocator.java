/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.services.clientserver.search;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class locates matches.
 */
public class SystemSearchStringMatchLocator implements ISystemSearchConstants {
	
	private Reader reader;
	private SystemSearchStringMatcher stringMatcher;
	private int fPushbackChar;
	private boolean fPushback;

	/**
	 * Constructor for creating a match locator.
	 * @param reader the reader from which to read and look for matches.
	 * @param stringMatcher the search string matcher.
	 */
	public SystemSearchStringMatchLocator(Reader reader, SystemSearchStringMatcher stringMatcher) {
		this.reader = reader;
		this.stringMatcher = stringMatcher;
	}
	
	/**
	 * Gets the search string matcher.
	 * @return the string matcher.
	 */
	public SystemSearchStringMatcher getStringMatcher() {
		return stringMatcher;
	}
	
	/**
	 * Gets the reader.
	 * @return the reader.
	 */
	public Reader getReader() {
		return reader;
	}
	
	/**
	 * Locates matches.
	 * @return an array of matches, or an empty array if none exists.
	 * @throws IOException if an I/O error occurs.
	 */
	public SystemSearchLineMatch[] locateMatches() throws IOException {
		
		int lineCounter = 1;
		int charCounter = 0;
		boolean eof = false;
		
		List matches = new ArrayList();
		
		try {
		
			while (!eof) {

				StringBuffer sb = new StringBuffer(200);
				int eolStrLength = readLine(reader, sb);
				
				eof = (eolStrLength == -1);
				
				int lineLength = sb.length();

				String line = sb.toString();
				
//				boolean firstMatch = false;
//				SystemSearchLineMatch match = null;

				// TODO: need to handle gettting correct positions for non-regex at least
//				while (lineLength != 0 && matcher.contains(input, pattern)) {
//					
//					int beginOffset = charCounter + input.getBeginOffset();
//					int endOffset = charCounter + input.getEndOffset();
//					
//					// if we get a match for the first time, create a new line match
//					if (!firstMatch) {
//						match = new SystemSearchLineMatch(line.trim(), lineCounter);
//						firstMatch = true;
//					}
//					
//					// now add the match to the line match so that we store all the matches in the line
//					match.addEntry(beginOffset, endOffset);
//				}

				// this just assumes one match per line, there may be more (if we use the non-regex matcher)
				if (lineLength != 0) {
					
					if (stringMatcher.matches(line)) {
					
						int beginOffset = charCounter + 0;
						int endOffset = charCounter + lineLength;
					
						SystemSearchLineMatch match = new SystemSearchLineMatch(line.trim(), lineCounter);
						match.addEntry(beginOffset, endOffset);
						matches.add(match);
					}
				}

				charCounter += lineLength + eolStrLength;
				lineCounter++;
			}
		}
		catch (IOException e) {
			// TODO: log error
		}
		finally {
			
			if (reader != null) {
				reader.close();
			}
		}
		
		SystemSearchLineMatch[] matchArray = new SystemSearchLineMatch[matches.size()];
		
		matches.toArray(matchArray);
		
		return matchArray;
	}
	
	/**
	 * Reads a line of text from the given reader.
	 * @param reader the reader.
	 * @param sb the buffer.
	 * @return the result of reading the line.
	 * @throws IOException if an I/O error occurs.
	 */
	protected int readLine(Reader reader, StringBuffer sb) throws IOException {
		int ch = -1;
		
		// if there is a character from a previous read that we needs to be added
		if (fPushback) {
			ch = fPushbackChar;
			fPushback = false;
		}
		else {
			ch = reader.read();
		}
		
		// keep reading until eof
		while (ch >= 0) {
			
			// if we get a line feed character, then we have read a line (in Unix)
			if (ch == LF_CHAR) {
				return 1;
			}
			
			// otherwise if we get a carriage return character, then we need to check the next character
			if (ch == CR_CHAR) {
				ch = reader.read();
				
				// if the character is a line feed, then we have read a line (in Windows)
				if (ch == LF_CHAR) {
					return 2;
				}
				// otherwise, we assume we are still in the same line (i.e. a carriage return does not count as a line separator)
				// we remember the character for the next read 
				else {
					fPushbackChar = ch;
					fPushback = true;
					return 1;
				}
			}
			
			sb.append((char)ch);
			ch = reader.read();
		}
		
		return -1;
	}
}