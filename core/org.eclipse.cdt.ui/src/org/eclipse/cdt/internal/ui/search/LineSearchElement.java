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
public class LineSearchElement extends CSearchElement {
	public static final class Match {
		private final int fOffset;
		private final int fLength;
		private final boolean fIsPolymorphicCall;
		private final ICElement fEnclosingElement;
		private final boolean fIsWriteAccess;

		public Match(int offset, int length, boolean isPolymorphicCall, ICElement enclosingElement,
				boolean isWriteAccess) {
			fOffset = offset;
			fLength = length;
			fIsPolymorphicCall = isPolymorphicCall;
			fEnclosingElement = enclosingElement;
			fIsWriteAccess = isWriteAccess;
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

		public boolean isWriteAccess() {
			return fIsWriteAccess;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Match))
				return false;
			Match m = (Match) obj;
			return fOffset == m.fOffset && fLength == m.fLength;
		}

		@Override
		public int hashCode() {
			return 31 * fOffset + fLength;
		}
	}

	private static final class MatchesComparator implements Comparator<Match> {
		@Override
		public int compare(Match m1, Match m2) {
			int diff= m1.getOffset() - m2.getOffset();
			if (diff == 0)
				diff= m2.getLength() -m1.getLength();
			return diff;
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
		return fOffset == other.fOffset && super.equals(obj) && fMatches.equals(other.fMatches);
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
		// Sort matches according to their offsets
		Arrays.sort(matches, MATCHES_COMPARATOR);
		// Group all matches by lines and create LineSearchElements
		List<LineSearchElement> result = new ArrayList<LineSearchElement>();
		List<Match> matchCollector= new ArrayList<Match>();
		int minOffset = 0;
		int lineNumber = 0;
		int lineOffset = 0;
		int lineLength = 0;
		int lineEndOffset = 0;

		try {
			for (final Match match : matches) {
				final int offset= match.getOffset();
				if (offset < lineEndOffset) {
					// Match on same line
					if (offset < minOffset) {
						// Match is not overlapped by previous one.
						matchCollector.add(match);
						minOffset= offset + match.getLength();
					}
				} else {
					// Match is on a new line
					if (!matchCollector.isEmpty()) {
						// Complete a line
						String content = document.get(lineOffset, lineLength);
						Match[] lineMatches= matchCollector.toArray(new Match[matchCollector.size()]);
						result.add(new LineSearchElement(fileLocation, lineMatches, lineNumber + 1, content, lineOffset));
						matchCollector.clear();
					}
					// Setup next line
					lineNumber = document.getLineOfOffset(offset);
					lineOffset = document.getLineOffset(lineNumber);
					lineLength = document.getLineLength(lineNumber);
					lineEndOffset = lineOffset + lineLength;
					matchCollector.add(match);
				} 
			}
			if (!matchCollector.isEmpty()) {
				// Complete a line
				String content = document.get(lineOffset, lineLength);
				Match[] lineMatches= matchCollector.toArray(new Match[matchCollector.size()]);
				result.add(new LineSearchElement(fileLocation, lineMatches, lineNumber + 1, content, lineOffset));
				matchCollector.clear();
			}
		} catch (BadLocationException e) {
			CUIPlugin.log(e);
		}
		return result.toArray(new LineSearchElement[result.size()]);
	}

	private static LineSearchElement[] collectLineElements(AbstractCharArray buf, Match[] matches,
			IIndexFileLocation fileLocation) {
		List<LineSearchElement> result = new ArrayList<LineSearchElement>();
		List<Match> matchCollector= new ArrayList<Match>();

		boolean skipLF = false;
		int lineNumber = 1;
		int lineOffset = 0;
		int i = 0;
		Match match= matches[i];
		int matchOffset = match.getOffset();
		for (int pos = 0; buf.isValidOffset(pos); pos++) {
			if (matchOffset <= pos && match != null) {
				// We are on the line of the match, store it.
				matchCollector.add(match);
				final int minOffset= matchOffset + match.getLength();
				match= null;
				matchOffset= Integer.MAX_VALUE;
				for(i= i + 1; i < matches.length; i++) {
					// Advance to next match that is not overlapped
					final Match nextMatch= matches[i];
					final int nextOffset= nextMatch.getOffset();
					if (nextOffset >= minOffset) {
						match= nextMatch;
						matchOffset= nextOffset;
						break;
					}
				}
			}
				
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
				// Create new LineElement for collected matches on this line
				if (!matchCollector.isEmpty()) {
					int lineLength = pos - lineOffset;
					Match[] lineMatches= matchCollector.toArray(new Match[matchCollector.size()]);
					char[] lineChars= new char[lineLength];
					buf.arraycopy(lineOffset, lineChars, 0, lineLength);
					String lineContent = new String(lineChars);
					result.add(new LineSearchElement(fileLocation, lineMatches, lineNumber, lineContent,
							lineOffset));
					matchCollector.clear();
					if (match == null)
						break;
				}
				lineNumber++;
				lineOffset = pos + 1;
				if (c == '\r')
					skipLF = true;
				continue;
			}
		}
		// Create new LineElement for  matches on the last line
		if (!matchCollector.isEmpty()) {
			int lineLength = buf.getLength() - lineOffset;
			Match[] lineMatches= matchCollector.toArray(new Match[matchCollector.size()]);
			char[] lineChars= new char[lineLength];
			buf.arraycopy(lineOffset, lineChars, 0, lineLength);
			String lineContent = new String(lineChars);
			result.add(new LineSearchElement(fileLocation, lineMatches, lineNumber, lineContent,
					lineOffset));
		}
		return result.toArray(new LineSearchElement[result.size()]);
	}
}
