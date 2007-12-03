/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.spelling;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellChecker;
import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellEventListener;

/**
 * C/C++ spelling engine
 */
public class CSpellingEngine extends SpellingEngine {
	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.SpellingEngine#check(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IRegion[], org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellChecker, org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void check(IDocument document, IRegion[] regions, ISpellChecker checker, ISpellingProblemCollector collector, IProgressMonitor monitor) {
		ISpellEventListener listener= new SpellEventListener(collector, document);
		boolean isIgnoringStringLiterals= SpellingPreferences.isIgnoreStringLiterals();
		try {
			checker.addListener(listener);
			try {
				for (int i= 0; i < regions.length; i++) {
					IRegion region= regions[i];
					ITypedRegion[] partitions= TextUtilities.computePartitioning(document,
							ICPartitions.C_PARTITIONING, region.getOffset(), region.getLength(), false);
					for (int index= 0; index < partitions.length; index++) {
						if (monitor != null && monitor.isCanceled())
							return;

						ITypedRegion partition= partitions[index];
						final String type= partition.getType();
						
						if (isIgnoringStringLiterals && type.equals(ICPartitions.C_STRING))
							continue;

						if (type.equals(ICPartitions.C_PREPROCESSOR)) {
							CTextTools textTools = CUIPlugin.getDefault().getTextTools();
							RuleBasedScanner scanner = textTools.getCppPreprocessorScanner();
							scanner.setRange(document, partition.getOffset(), partition.getLength());
							int firstTokenOffset = -1;
							int firstTokenLength = -1;
							while (true) {
								IToken token = scanner.nextToken();
								if (token.isEOF()) {
									break;
								}
								if (token.isOther()) {
									int offset = scanner.getTokenOffset();
									int length = scanner.getTokenLength();
									if (firstTokenOffset < 0) {
										firstTokenOffset = offset;
										firstTokenLength = length;
									}
									String subregionType = null;
									char c = document.getChar(offset);
									if (c == '"') {
										if (!isIgnoringStringLiterals &&
												!isIncludeDirective(document, firstTokenOffset, firstTokenLength)) {
											subregionType = ICPartitions.C_STRING;
										}
									} else if (c == '/' && length >= 2) {
										c = document.getChar(offset + 1);
										if (c == '/') {
											subregionType = ICPartitions.C_SINGLE_LINE_COMMENT;
										} else if (c == '*') {
											subregionType = ICPartitions.C_MULTI_LINE_COMMENT;
										}
									}
									if (subregionType != null) {
										TypedRegion subregion = new TypedRegion(offset, length, subregionType);
										checker.execute(new SpellCheckIterator(document, subregion,
												checker.getLocale()));
									}
								}
							}
						} else if (!type.equals(IDocument.DEFAULT_CONTENT_TYPE) &&
								!type.equals(ICPartitions.C_CHARACTER)) {
							checker.execute(new SpellCheckIterator(document, partition, checker.getLocale()));
						}
					}
				}
			} catch (BadLocationException x) {
				CUIPlugin.getDefault().log(x);
			}
		} finally {
			checker.removeListener(listener);
		}
	}

	/**
	 * Returns <code>true</code> if the token at the given offset and length is an include directive. 
	 * @param document
	 * @param offset
	 * @param length
	 * @return
	 * @throws BadLocationException
	 */
	private boolean isIncludeDirective(IDocument document, int offset, int length) throws BadLocationException {
		while (length > 0) {
			char c = document.getChar(offset);
			if (c == '#' || Character.isWhitespace(c)) {
				offset++;
				length--;
			} else if (c == 'i') {
				return document.get(offset, length).startsWith("include"); //$NON-NLS-1$
			} else {
				break;
			}
		}
		return false;
	}
}
