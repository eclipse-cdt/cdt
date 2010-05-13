/*******************************************************************************
 * Copyright (c) 2009, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrey Eremchenko, kamre@ngs.ru - 222495 C/C++ search should show line matches and line numbers	
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.parser.scanner.AbstractCharArray;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;

/**
 * Element representing a line with one ore more matches.
 */
public class LineSearchElement extends PDOMSearchElement {
	public static final class Match {
		private final int fOffset;
		private final int fLength;
		private final boolean fIsPolymorphicCall;
		private final ICElement fEnclosingElement;

		public Match(int offset, int length, boolean isPolymorphicCall, ICElement enclosingElement) {
			fOffset = offset;
			fLength = length;
			fIsPolymorphicCall = isPolymorphicCall;
			fEnclosingElement = enclosingElement;
		}

		public int getOffset() {
			return fOffset;
		}

		public int getLength() {
			return fLength;
		}

		public boolean isPolymorphicCall() {
			return fIsPolymorphicCall;
		}
		
		public ICElement getEnclosingElement() {
			return fEnclosingElement;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Match))
				return false;
			Match m = (Match) obj;
			return (fOffset == m.fOffset) && (fLength == m.fLength);
		}

		@Override
		public int hashCode() {
			return 31 * fOffset + fLength;
		}
	}

	private static final class MatchesComparator implements Comparator<Match> {
		public int compare(Match m1, Match m2) {
			return m1.getOffset() - m2.getOffset();
		}
	}

	private final int fOffset;
	private final int fNumber;
	private final String fContent;
	private final Match[] fMatches;
	private final static MatchesComparator MATCHES_COMPARATOR = new MatchesComparator();

	private LineSearchElement(IIndexFileLocation file, Match[] matches, int number, String content,
			int offset) {
		super(file);
		fMatches = matches;
		fNumber = number;
		// Skip whitespace at the beginning.
		int index = 0;
		int length = content.length();
		int firstMatchOffset = matches[0].getOffset();
		while (offset < firstMatchOffset && length > 0) {
			if (!Character.isWhitespace(content.charAt(index)))
				break;
			index++;
			offset++;
			length--;
		}
		fOffset = offset;
		fContent = content.substring(index).trim();
	}

	public int getOffset() {
		return fOffset;
	}

	public int getLineNumber() {
		return fNumber;
	}

	public String getContent() {
		return fContent;
	}

	public Match[] getMatches() {
		return fMatches;
	}

	@Override
	public String toString() {
		return fNumber + ": " + fContent; //$NON-NLS-1$
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LineSearchElement))
			return false;
		LineSearchElement other = (LineSearchElement) obj;
		return (fOffset == other.fOffset) && (super.equals(obj)) && (fMatches.equals(other.fMatches));
	}

	@Override
	public int hashCode() {
		return fOffset + 31 * (super.hashCode() + 31 * fMatches.hashCode());
	}

	public static LineSearchElement[] createElements(IIndexFileLocation fileLocation, Match[] matches) {
		// sort matches according to their offsets
		Arrays.sort(matches, MATCHES_COMPARATOR);
		LineSearchElement[] result = {};
		
		// read the content of file
		FileContent content = FileContent.create(fileLocation);
		if (content != null) {
			AbstractCharArray buf = ((InternalFileContent) content).getSource();
			if (buf != null)
				result = collectLineElements(buf, matches, fileLocation);
		}
		return result;
	}

	public static LineSearchElement[] createElements(IIndexFileLocation fileLocation, Match[] matches,
			IDocument document) {
		// sort matches according to their offsets
		Arrays.sort(matches, MATCHES_COMPARATOR);
		// group all matches by lines and create LineSearchElements
		List<LineSearchElement> result = new ArrayList<LineSearchElement>();
		int firstMatch = 0;
		while (firstMatch < matches.length) {
			try {
				int lineNumber = document.getLineOfOffset(matches[firstMatch].getOffset());
				int lineOffset = document.getLineOffset(lineNumber);
				int lineLength = document.getLineLength(lineNumber);
				int nextlineOffset = lineOffset + lineLength;
				int nextMatch = firstMatch;
				int nextMatchOffset = matches[nextMatch].getOffset();
				while (nextMatch < matches.length && nextMatchOffset < nextlineOffset) {
					nextMatch++;
					if (nextMatch < matches.length)
						nextMatchOffset = matches[nextMatch].getOffset();
				}
				int lineMatchesCount = nextMatch - firstMatch;
				Match[] lineMatches = new Match[lineMatchesCount];
				System.arraycopy(matches, firstMatch, lineMatches, 0, lineMatchesCount);
				String content = document.get(lineOffset, lineLength);
				result.add(new LineSearchElement(fileLocation, lineMatches, lineNumber + 1, content, lineOffset));
				firstMatch = nextMatch;
			} catch (BadLocationException e) {
				CUIPlugin.log(e);
			}
		}
		return result.toArray(new LineSearchElement[result.size()]);
	}

	private static LineSearchElement[] collectLineElements(AbstractCharArray buf, Match[] matches,
			IIndexFileLocation fileLocation) {
		List<LineSearchElement> result = new ArrayList<LineSearchElement>();
		boolean skipLF = false;
		int lineNumber = 1;
		int lineOffset = 0;
		int lineFirstMatch = -1; // not matched
		int nextMatch = 0;
		int nextMatchOffset = matches[nextMatch].getOffset();
		for (int pos = 0; buf.isValidOffset(pos); pos++) {
			char c = buf.get(pos);
			// consider '\n' and '\r'
			if (skipLF) {
				skipLF = false;
				if (c == '\n') {
					lineOffset = pos + 1;
					continue;
				}
			}
			if (c == '\n' || c == '\r') {
				// create new LineElement if there were matches
				if (lineFirstMatch != -1) {
					int lineLength = pos - lineOffset;
					int lineMatchesCount = nextMatch - lineFirstMatch;
					Match[] lineMatches = new Match[lineMatchesCount];
					System.arraycopy(matches, lineFirstMatch, lineMatches, 0, lineMatchesCount);
					char[] lineChars= new char[lineLength];
					buf.arraycopy(lineOffset, lineChars, 0, lineLength);
					String lineContent = new String(lineChars);
					result.add(new LineSearchElement(fileLocation, lineMatches, lineNumber, lineContent,
							lineOffset));
					lineFirstMatch = -1;
					if (nextMatch >= matches.length)
						break;
					if (matches[nextMatch].getOffset() < pos)
						lineFirstMatch = nextMatch;
				}
				lineNumber++;
				lineOffset = pos + 1;
				if (c == '\r')
					skipLF = true;
				continue;
			}
			// compare offset of next match with current position
			if (nextMatchOffset > pos || nextMatch >= matches.length)
				continue;
			// next match was reached
			// check if this match is the first for current line
			if (lineFirstMatch == -1)
				lineFirstMatch = nextMatch;
			// goto to next match
			nextMatch++;
			if (nextMatch < matches.length) {
				// update offset of next match
				nextMatchOffset = matches[nextMatch].getOffset();
			}
		}
		// check if there were matches on the last line
		if (lineFirstMatch != -1) {
			int lineLength = buf.getLength() - lineOffset;
			int lineMatchesCount = nextMatch - lineFirstMatch;
			Match[] lineMatches = new Match[lineMatchesCount];
			System.arraycopy(matches, lineFirstMatch, lineMatches, 0, lineMatchesCount);

			char[] lineChars= new char[lineLength];
			buf.arraycopy(lineOffset, lineChars, 0, lineLength);
			String lineContent = new String(lineChars);
			result.add(new LineSearchElement(fileLocation, lineMatches, lineNumber, lineContent, lineOffset));
		}
		return result.toArray(new LineSearchElement[result.size()]);
	}
}
