/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.spelling;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;

import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.ITokenStore;
import org.eclipse.cdt.ui.text.ITokenStoreFactory;
import org.eclipse.cdt.ui.text.doctools.IDocCommentDictionary;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.cdt.ui.text.doctools.IDocCommentSimpleDictionary;

import org.eclipse.cdt.internal.ui.text.CPreprocessorScanner;
import org.eclipse.cdt.internal.ui.text.FastCPartitioner;
import org.eclipse.cdt.internal.ui.text.doctools.DocCommentSpellDictionary;
import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellChecker;
import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellDictionary;
import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellEventListener;

/**
 * C/C++ spelling engine
 */
public class CSpellingEngine extends SpellingEngine {

	/**
	 * A dummy token store for use with a token scanner.
	 */
	private static class SimpleTokenStore implements ITokenStore {
		@Override
		public void ensureTokensInitialised() {
		}
		@Override
		public IPreferenceStore getPreferenceStore() {
			return null;
		}
		@Override
		public IToken getToken(String property) {
			return new Token(property);
		}
		@Override
		public void adaptToPreferenceChange(PropertyChangeEvent event) {
		}
		@Override
		public boolean affectsBehavior(PropertyChangeEvent event) {
			return false;
		}
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.SpellingEngine#check(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IRegion[], org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellChecker, org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void check(IDocument document, IRegion[] regions, ISpellChecker checker, ISpellingProblemCollector collector, IProgressMonitor monitor) {
		ISpellEventListener listener= new SpellEventListener(collector, document);
		boolean isIgnoringStringLiterals= SpellingPreferences.isIgnoreStringLiterals();
		
		ISpellDictionary toRemove= null;
		try {
			checker.addListener(listener);
			
			IDocCommentOwner owner= null;
			if (document instanceof IDocumentExtension3) {
				IDocumentPartitioner partitioner= ((IDocumentExtension3)document).getDocumentPartitioner(ICPartitions.C_PARTITIONING);
				if (partitioner instanceof FastCPartitioner) {
					owner= ((FastCPartitioner)partitioner).getDocCommentOwner();
				}
			}
			
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

						if (owner!=null) {
							IDocCommentDictionary dict= null;
							
							if (type.equals(ICPartitions.C_MULTI_LINE_DOC_COMMENT)) {
								dict= owner.getMultilineConfiguration().getSpellingDictionary();
							} else if (type.equals(ICPartitions.C_SINGLE_LINE_DOC_COMMENT)) {
								dict= owner.getSinglelineConfiguration().getSpellingDictionary();
							}
							
							if (dict instanceof IDocCommentSimpleDictionary) {
								ISpellDictionary sd= new DocCommentSpellDictionary((IDocCommentSimpleDictionary)dict);
								checker.addDictionary(sd);
								toRemove= sd;
							}
						}
						
						if (type.equals(ICPartitions.C_PREPROCESSOR)) {
							RuleBasedScanner scanner = new CPreprocessorScanner(new ITokenStoreFactory() {
								@Override
								public ITokenStore createTokenStore(String[] propertyColorNames) {
									return new SimpleTokenStore();
								}}, GPPLanguage.getDefault());
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
						
						if (toRemove != null) {
							checker.removeDictionary(toRemove);
							toRemove= null;
						}
					}
				}
			} catch (BadLocationException x) {
				// Ignore BadLocationException since although it does happen from time to time,
				// there seems to be not much harm from it.
//				CUIPlugin.log(x);
			}
		} finally {
			if (toRemove != null)
				checker.removeDictionary(toRemove);
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
