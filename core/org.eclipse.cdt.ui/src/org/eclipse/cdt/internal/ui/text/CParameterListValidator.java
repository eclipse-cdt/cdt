/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.ui.text.contentassist.CProposalContextInformation;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

/**
 * This class provides the function parameter parsing for the C/C++ Editor hover.
 * It is based heavily on the Java class JavaParameterListValidator.
 *
 * @author thomasf
 */
public class CParameterListValidator implements IContextInformationValidator, IContextInformationPresenter {
	private int fPosition;
	private ITextViewer fViewer;
	private IContextInformation fInformation;

	private int fCurrentParameter;

	public CParameterListValidator() {
	}

	/**
	 * @see IContextInformationValidator#install(IContextInformation, ITextViewer, int)
	 * @see IContextInformationPresenter#install(IContextInformation, ITextViewer, int)
	 */
	@Override
	public void install(IContextInformation info, ITextViewer viewer, int documentPosition) {
		fPosition = documentPosition;
		fViewer = viewer;
		fInformation = info;

		fCurrentParameter = -1;
	}

	private int getCommentEnd(IDocument d, int pos, int end) throws BadLocationException {
		while (pos < end) {
			char curr = d.getChar(pos);
			pos++;
			if (curr == '*') {
				if (pos < end && d.getChar(pos) == '/') {
					return pos + 1;
				}
			}
		}
		return end;
	}

	private int getStringEnd(IDocument d, int pos, int end, char ch) throws BadLocationException {
		while (pos < end) {
			char curr = d.getChar(pos);
			pos++;
			if (curr == '\\') {
				// Ignore escaped characters.
				pos++;
			} else if (curr == ch) {
				return pos;
			}
		}
		return end;
	}

	private int getCharCount(IDocument document, int start, int end, char increment, char decrement,
			boolean considerNesting) throws BadLocationException {
		Assert.isTrue((increment != 0 || decrement != 0) && increment != decrement);

		int parenNestingLevel = 0;
		int braceNestingLevel = 0;
		int charCount = 0;
		while (start < end) {
			char curr = document.getChar(start++);
			switch (curr) {
			case '/':
				if (start < end) {
					char next = document.getChar(start);
					if (next == '*') {
						// A comment starts, advance to the comment end.
						start = getCommentEnd(document, start + 1, end);
					} else if (next == '/') {
						// '//'-comment: nothing to do anymore on this line
						start = end;
					}
				}
				break;

			case '*':
				if (start < end) {
					char next = document.getChar(start);
					if (next == '/') {
						// We have been in a comment: forget what we read before.
						charCount = 0;
						++start;
					}
				}
				break;

			case '"':
			case '\'':
				start = getStringEnd(document, start, end, curr);
				break;

			default:
				if (considerNesting) {
					if ('(' == curr) {
						++parenNestingLevel;
					} else if (')' == curr) {
						--parenNestingLevel;
					}

					if (parenNestingLevel != 0)
						break;

					if ('{' == curr) {
						++braceNestingLevel;
					} else if ('}' == curr) {
						--braceNestingLevel;
					}

					if (braceNestingLevel != 0)
						break;
				}

				if (increment != 0) {
					if (curr == increment)
						++charCount;
				}

				if (decrement != 0) {
					if (curr == decrement)
						--charCount;
				}
			}
		}

		return charCount;
	}

	/**
	 * @see IContextInformationValidator#isContextInformationValid(int)
	 */
	@Override
	public boolean isContextInformationValid(int position) {
		try {
			if (position < fPosition)
				return false;

			IDocument document = fViewer.getDocument();
			return getCharCount(document, fPosition, position, '(', ')', false) >= 0;
		} catch (BadLocationException x) {
			return false;
		}
	}

	@Override
	public boolean updatePresentation(int position, TextPresentation presentation) {
		int currentParameter = -1;

		try {
			currentParameter = getCharCount(fViewer.getDocument(), fPosition, position, ',', (char) 0, true);
		} catch (BadLocationException x) {
			return false;
		}

		if (fCurrentParameter != -1) {
			if (currentParameter == fCurrentParameter)
				return false;
		}

		presentation.clear();
		fCurrentParameter = currentParameter;

		// Don't presume what has been done to the string, rather use as is.
		String s = fInformation.getInformationDisplayString();
		String params = s;

		// Context information objects of type CProposalContextInformation can have
		// an optional prefix before and suffix after the parameter list.
		// In such a case, query the indices that bound the parameter list part of
		// the string, so we can parse the comma positions accurately.
		int paramlistStartIndex = 0;
		int paramlistEndIndex = s.length();
		if (fInformation instanceof CProposalContextInformation) {
			CProposalContextInformation info = (CProposalContextInformation) fInformation;
			if (info.hasPrefixSuffix()) {
				paramlistStartIndex = info.getArglistStartIndex();
				paramlistEndIndex = info.getArglistEndIndex();
				params = s.substring(paramlistStartIndex, paramlistEndIndex);
			}
		}

		int[] commas = computeCommaPositions(params);
		if (commas.length - 2 < fCurrentParameter) {
			presentation.addStyleRange(new StyleRange(0, s.length(), null, null, SWT.NORMAL));
			return true;
		}

		int start = commas[fCurrentParameter] + 1;
		int end = commas[fCurrentParameter + 1];
		if (start > 0)
			presentation.addStyleRange(new StyleRange(paramlistStartIndex, start, null, null, SWT.NORMAL));

		if (end > start)
			presentation.addStyleRange(new StyleRange(paramlistStartIndex + start, end - start, null, null, SWT.BOLD));

		if (end < s.length())
			presentation.addStyleRange(
					new StyleRange(paramlistStartIndex + end, params.length() - end, null, null, SWT.NORMAL));

		return true;
	}

	private int[] computeCommaPositions(String code) {
		final int length = code.length();
		int pos = 0;
		List<Integer> positions = new ArrayList<>();
		positions.add(Integer.valueOf(-1));
		while (pos < length && pos != -1) {
			char ch = code.charAt(pos);
			switch (ch) {
			case ',':
				positions.add(Integer.valueOf(pos));
				break;
			case '(':
				pos = indexOfClosingPeer(code, '(', ')', pos);
				break;
			case '<':
				pos = indexOfClosingPeer(code, '<', '>', pos);
				break;
			case '[':
				pos = indexOfClosingPeer(code, '[', ']', pos);
				break;
			case '{':
				pos = indexOfClosingPeer(code, '{', '}', pos);
				break;
			default:
				break;
			}
			if (pos != -1)
				pos++;
		}
		positions.add(Integer.valueOf(length));

		int[] fields = new int[positions.size()];
		for (int i = 0; i < fields.length; i++)
			fields[i] = positions.get(i).intValue();
		return fields;
	}

	private int indexOfClosingPeer(String code, char left, char right, int pos) {
		int level = 0;
		final int length = code.length();
		while (pos < length) {
			char ch = code.charAt(pos);
			if (ch == left) {
				++level;
			} else if (ch == right) {
				if (--level == 0) {
					return pos;
				}
			}
			++pos;
		}
		return -1;
	}
}
