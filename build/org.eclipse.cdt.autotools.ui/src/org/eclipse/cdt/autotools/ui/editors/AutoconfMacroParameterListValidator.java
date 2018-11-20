/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

/**
 * This class provides the macro call parameter parsing for the Autoconf Editor hover
 * It is modified from the CDT class CParameterListValidator
 *
 * @author jjohnstn
 *
 */
public class AutoconfMacroParameterListValidator implements IContextInformationValidator, IContextInformationPresenter {
	private int fPosition;
	private ITextViewer fViewer;
	private IContextInformation fInformation;

	private int fCurrentParameter;

	public AutoconfMacroParameterListValidator() {
	}

	@Override
	public void install(IContextInformation info, ITextViewer viewer, int documentPosition) {

		fPosition = documentPosition;
		fViewer = viewer;
		fInformation = info;

		fCurrentParameter = -1;
	}

	private int getStringEnd(IDocument d, int pos, int end, char ch) throws BadLocationException {
		while (pos < end) {
			char curr = d.getChar(pos);
			pos++;
			if (curr == '\\') {
				// ignore escaped characters
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

		// There are two nesting levels to worry about.  Arguments can be
		// quoted with [] which means to treat the contents as one arg.
		// As well, macro calls can be nested within macro calls so we
		// have to handle parentheses.
		int macroQuotingLevel = 0; // Pertaining to '[' and ']' quoted args.
		int macroNestingLevel = -1; // Set to -1 to take into account first ( for function call
		int charCount = 0;
		while (start < end) {
			char curr = document.getChar(start++);
			switch (curr) {
			case 'd':
				if (start < end - 2) {
					char next = document.getChar(start);
					if (next == 'n') {
						// a comment starts, advance to the comment end
						next = document.getChar(start + 1);
						if (next == 'l')
							// dnl-comment: nothing to do anymore on this line
							start = end;
					}
				}
				break;
			case '"':
			case '\'':
				start = getStringEnd(document, start, end, curr);
				break;
			default:
				if ('[' == curr)
					++macroQuotingLevel;
				else if (']' == curr)
					--macroQuotingLevel;
				if (macroQuotingLevel != 0)
					break;
				if (considerNesting) {
					if ('(' == curr)
						++macroNestingLevel;
					else if (')' == curr) {
						--macroNestingLevel;
					}
					if (macroNestingLevel != 0)
						break;
				}
				if (increment != 0) {
					if (curr == increment) {
						++charCount;
					}
				}

				if (decrement != 0) {
					if (curr == decrement) {
						--charCount;
					}
				}
			}
		}

		return charCount;
	}

	@Override
	public boolean isContextInformationValid(int position) {

		try {
			if (position < fPosition)
				return false;

			IDocument document = fViewer.getDocument();
			IRegion line = document.getLineInformationOfOffset(fPosition);

			if (position > line.getOffset() + line.getLength())
				return false;

			return (getCharCount(document, fPosition, position, '(', ')', false) >= 0);

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

		//Don't presume what has been done to the string, rather use as is
		String s = fInformation.getInformationDisplayString();

		//@@@ This is obviously going to have problems with functions such
		//int myfunction(int (*function_argument)(void * extra, int param), void * extra)
		//int myfunction(/*A comment, indeed */int a);
		int start = 0;
		int occurrences = 0;
		while (occurrences < fCurrentParameter) {
			int found = s.indexOf(',', start);
			if (found == -1)
				break;
			start = found + 1;
			++occurrences;
		}

		if (occurrences < fCurrentParameter) {
			presentation.addStyleRange(new StyleRange(0, s.length(), null, null, SWT.NORMAL));
			return true;
		}

		if (start == -1)
			start = 0;

		int end = s.indexOf(',', start);
		if (end == -1)
			end = s.length();

		if (start > 0)
			presentation.addStyleRange(new StyleRange(0, start, null, null, SWT.NORMAL));

		if (end > start)
			presentation.addStyleRange(new StyleRange(start, end - start, null, null, SWT.BOLD));

		if (end < s.length())
			presentation.addStyleRange(new StyleRange(end, s.length() - end, null, null, SWT.NORMAL));

		return true;
	}
}
