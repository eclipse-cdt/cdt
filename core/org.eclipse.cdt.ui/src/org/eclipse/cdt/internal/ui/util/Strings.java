/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;

/**
 * Helper class to provide String manipulation functions not available in standard JDK.
 */
public class Strings {
	
	/**
	 * Indent char is a space char but not a line delimiters.
	 * <code>== Character.isWhitespace(ch) && ch != '\n' && ch != '\r'</code>
	 */
	public static boolean isIndentChar(char ch) {
		return Character.isWhitespace(ch) && !isLineDelimiterChar(ch);
	}
	
	/**
	 * tests if a char is lower case. Fix for 26529 
	 */
	public static boolean isLowerCase(char ch) {
		return Character.toLowerCase(ch) == ch;
	}	
	
	/**
	 * Line delimiter chars are  '\n' and '\r'.
	 */
	public static boolean isLineDelimiterChar(char ch) {
		return ch == '\n' || ch == '\r';
	}	

	public static String removeNewLine(String message) {
		StringBuffer result= new StringBuffer();
		int current= 0;
		int index= message.indexOf('\n', 0);
		while (index != -1) {
			result.append(message.substring(current, index));
			if (current < index && index != 0)
				result.append(' ');
			current= index + 1;
			index= message.indexOf('\n', current);
		}
		result.append(message.substring(current));
		return result.toString();
	}

	/**
	 * Converts the given string into an array of lines. The lines 
	 * don't contain any line delimiter characters.
	 *
	 * @return the string converted into an array of strings. Returns <code>
	 * 	null</code> if the input string can't be converted in an array of lines.
	 */
	public static String[] convertIntoLines(String input) {
		try {
			ILineTracker tracker= new DefaultLineTracker();
			tracker.set(input);
			int size= tracker.getNumberOfLines();
			String result[]= new String[size];
			for (int i= 0; i < size; i++) {
				IRegion region= tracker.getLineInformation(i);
				int offset= region.getOffset();
				result[i]= input.substring(offset, offset + region.getLength());
			}
			return result;
		} catch (BadLocationException e) {
			return null;
		}
	}

	/**
	 * Returns <code>true</code> if the given string only consists of
	 * white spaces according to Java. If the string is empty, <code>true
	 * </code> is returned.
	 * 
	 * @return <code>true</code> if the string only consists of white
	 * 	spaces; otherwise <code>false</code> is returned
	 * 
	 * @see java.lang.Character#isWhitespace(char)
	 */
	public static boolean containsOnlyWhitespaces(String s) {
		int size= s.length();
		for (int i= 0; i < size; i++) {
			if (!Character.isWhitespace(s.charAt(i)))
				return false;
		}
		return true;
	}
	
	/**
	 * Removes leading tabs and spaces from the given string. If the string
	 * doesn't contain any leading tabs or spaces then the string itself is 
	 * returned.
	 */
	public static String trimLeadingTabsAndSpaces(String line) {
		int size= line.length();
		int start= size;
		for (int i= 0; i < size; i++) {
			char c= line.charAt(i);
			if (!isIndentChar(c)) {
				start= i;
				break;
			}
		}
		if (start == 0)
			return line;
		else if (start == size)
			return ""; //$NON-NLS-1$
		else
			return line.substring(start);
	}
	
	public static String trimTrailingTabsAndSpaces(String line) {
		int size= line.length();
		int end= size;
		for (int i= size - 1; i >= 0; i--) {
			char c= line.charAt(i);
			if (isIndentChar(c)) {
				end= i;
			} else {
				break;
			}
		}
		if (end == size)
			return line;
		else if (end == 0)
			return ""; //$NON-NLS-1$
		else
			return line.substring(0, end);
	}
	
	/**
	 * Returns the indent of the given string.
	 * 
	 * @param line the text line
	 * @param tabWidth the width of the '\t' character.
	 */
	public static int computeIndent(String line, int tabWidth) {
		int result= 0;
		int blanks= 0;
		int size= line.length();
		for (int i= 0; i < size; i++) {
			char c= line.charAt(i);
			if (c == '\t') {
				result++;
				blanks= 0;
			} else if (isIndentChar(c)) {
				blanks++;
				if (blanks == tabWidth) {
					result++;
					blanks= 0;
				}
			} else {
				return result;
			}
		}
		return result;
	}
	
	/**
	 * Removes the given number of idents from the line. Asserts that the given line 
	 * has the requested number of indents. If <code>indentsToRemove <= 0</code>
	 * the line is returned.
	 */
	public static String trimIndent(String line, int indentsToRemove, int tabWidth) {
		if (line == null || indentsToRemove <= 0)
			return line;
			
		int start= 0;
		int indents= 0;
		int blanks= 0;
		int size= line.length();
		for (int i= 0; i < size; i++) {
			char c= line.charAt(i);
			if (c == '\t') {
				indents++;
				blanks= 0;
			} else if (isIndentChar(c)) {
					blanks++;
					if (blanks == tabWidth) {
						indents++;
						blanks= 0;
					}
			} else {
				// Assert.isTrue(false, "Line does not have requested number of indents"); //$NON-NLS-1$
				start= i + 1;
				break; 
			}
			if (indents == indentsToRemove) {
				start= i + 1;
				break;
			}	
		}
		if (start == size)
			return ""; //$NON-NLS-1$
		return line.substring(start);
	}
	
	/**
	 * Removes all leading indents from the given line. If the line doesn't contain
	 * any indents the line itself is returned.
	 */
	public static String trimIndents(String s, int tabWidth) {
		int indent= computeIndent(s, tabWidth);
		if (indent == 0)
			return s;
		return trimIndent(s, indent, tabWidth);
	}
	
	/**
	 * Removes the common number of indents from all lines. If a line
	 * only consists out of white space it is ignored.
	 */
	public static void trimIndentation(String[] lines, int tabWidth) {
		trimIndentation(lines, tabWidth, true);
	}
	
	/**
	 * Removes the common number of indents from all lines. If a line
	 * only consists out of white space it is ignored. If <code>
	 * considerFirstLine</code> is false the first line will be ignored.
	 */
	public static void trimIndentation(String[] lines, int tabWidth, boolean considerFirstLine) {
		String[] toDo= new String[lines.length];
		// find indentation common to all lines
		int minIndent= Integer.MAX_VALUE; // very large
		for (int i= considerFirstLine ? 0 : 1; i < lines.length; i++) {
			String line= lines[i];
			if (containsOnlyWhitespaces(line))
				continue;
			toDo[i]= line;
			int indent= computeIndent(line, tabWidth);
			if (indent < minIndent) {
				minIndent= indent;
			}
		}
		
		if (minIndent > 0) {
			// remove this indent from all lines
			for (int i= considerFirstLine ? 0 : 1; i < toDo.length; i++) {
				String s= toDo[i];
				if (s != null)
					lines[i]= trimIndent(s, minIndent, tabWidth);
				else {
					String line= lines[i];
					int indent= computeIndent(line, tabWidth);
					if (indent > minIndent)
						lines[i]= trimIndent(line, minIndent, tabWidth);
					else
						lines[i]= trimLeadingTabsAndSpaces(line);
				}
			}
		}
	}
	
	public static String getIndentString(String line, int tabWidth) {
		int size= line.length();
		int end= 0;
		int blanks= 0;
		for (int i= 0; i < size; i++) {
			char c= line.charAt(i);
			if (c == '\t') {
				end= i + 1;
				blanks= 0;
			} else if (isIndentChar(c)) {
				blanks++;
				if (blanks == tabWidth) {
					end= i + 1;
					blanks= 0;
				}
			} else {
				break;
			}
		}
		if (end == 0)
			return ""; //$NON-NLS-1$
		else if (end == size)
			return line;
		else
			return line.substring(0, end);
	}
	
	public static String[] removeTrailingEmptyLines(String[] sourceLines) {
		int lastNonEmpty= findLastNonEmptyLineIndex(sourceLines);
		String[] result= new String[lastNonEmpty + 1];
		for (int i= 0; i < result.length; i++) {
			result[i]= sourceLines[i];
		}
		return result;
	}

	private static int findLastNonEmptyLineIndex(String[] sourceLines) {
		for (int i= sourceLines.length - 1; i >= 0; i--) {
			if (! sourceLines[i].trim().equals(""))//$NON-NLS-1$
				return i;
		}
		return -1;
	}
	
	/**
	 * Change the indent of, possible muti-line, code range. The current indent is removed, a new indent added.
	 * The first line of the code will not be changed. (It is considered to have no indent as it might start in
	 * the middle of a line)
	 */
	public static String changeIndent(String code, int codeIndentLevel, int tabWidth, String newIndent, String lineDelim) {
		try {
			ILineTracker tracker= new DefaultLineTracker();
			tracker.set(code);
			int nLines= tracker.getNumberOfLines();
			if (nLines == 1) {
				return code;
			}
			
			StringBuffer buf= new StringBuffer();
			
			for (int i= 0; i < nLines; i++) {
				IRegion region= tracker.getLineInformation(i);
				int start= region.getOffset();
				int end= start + region.getLength();
				String line= code.substring(start, end);
				
				if (i == 0) {  // no indent for first line (contained in the formatted string)
					buf.append(line);
				} else { // no new line after last line
					buf.append(lineDelim);
					buf.append(newIndent); 
					buf.append(trimIndent(line, codeIndentLevel, tabWidth));
				}
			}
			return buf.toString();
		} catch (BadLocationException e) {
			// can not happen
			return code;
		}
	}	
	
	/**
	 * Concatenate the given strings into one strings using the passed line delimiter as a
	 * delimiter. No delimiter is added to the last line.
	 */
	public static String concatenate(String[] lines, String delimiter) {
		StringBuffer buffer= new StringBuffer();
		for (int i= 0; i < lines.length; i++) {
			if (i > 0)
				buffer.append(delimiter);
			buffer.append(lines[i]);
		}
		return buffer.toString();
	}
	
	public static boolean equals(String s, char[] c) {
		if (s.length() != c.length)
			return false;

		for (int i = c.length; --i >= 0;)
			if (s.charAt(i) != c[i])
				return false;
		return true;
	}

	/**
	 * Creates a string that consists of the given number of tab characters.
	 * @param indentLevel
	 */
	public static String createIndentString(int indentLevel) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < indentLevel; i++) {
			result.append('\t');
		}
		return result.toString();
	}

}

