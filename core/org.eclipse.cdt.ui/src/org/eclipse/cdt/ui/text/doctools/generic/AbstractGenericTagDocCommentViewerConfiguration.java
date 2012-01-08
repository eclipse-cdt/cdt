/*******************************************************************************
 * Copyright (c) 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools.generic;

import org.eclipse.jface.text.ITextDoubleClickStrategy;

import org.eclipse.cdt.ui.text.ICTokenScanner;
import org.eclipse.cdt.ui.text.ITokenStoreFactory;
import org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer;
import org.eclipse.cdt.ui.text.doctools.IDocCommentDictionary;
import org.eclipse.cdt.ui.text.doctools.IDocCommentSimpleDictionary;
import org.eclipse.cdt.ui.text.doctools.IDocCommentViewerConfiguration;

/**
 * An abstract base-class for documentation tool contributions using the 'generic tag' framework
 * @since 5.0
 */
public abstract class AbstractGenericTagDocCommentViewerConfiguration implements IDocCommentViewerConfiguration {
	protected GenericDocTag[] fTags;
	protected char[] fTagMarkers;
	
	protected ITextDoubleClickStrategy fDCStrategy;
	protected ICompletionProposalComputer fCPComputer;
	protected String fDefaultToken;
	protected String fTagToken;
	protected IDocCommentSimpleDictionary fDictionary;
	
	/**
	 * 
	 * @param tags a non-null array of tags this configuration should recognize
	 * @param tagMarkers a non-null array of characters used to prefix the tags (e.g. @ or \)
	 * @param defaultToken the default scanner token id
	 * @param tagToken the scanner token to use to mark used by this configuration
	 */
	public AbstractGenericTagDocCommentViewerConfiguration(GenericDocTag[] tags, char[] tagMarkers, String defaultToken, String tagToken) {
		fTags= tags;
		fTagMarkers= tagMarkers;
		fDCStrategy= new GenericTagDoubleClickStrategy(tagMarkers);
		fCPComputer= new GenericTagCompletionProposalComputer(fTags, tagMarkers);
		fDefaultToken= defaultToken;
		fTagToken= tagToken;
		fDictionary= new GenericTagSimpleDictionary(fTags, fTagMarkers);
	}

	/*
	 * @see org.eclipse.cdt.ui.text.doctools.IDocCommentViewerConfiguration#createCommentScanner(org.eclipse.cdt.ui.text.ITokenStoreFactory, java.lang.String)
	 */
	@Override
	public ICTokenScanner createCommentScanner(ITokenStoreFactory tokenStoreFactory) {
		return new GenericTagCommentScanner(fTags, fTagMarkers, tokenStoreFactory, fDefaultToken, fTagToken);
	}

	/*
	 * @see org.eclipse.cdt.ui.text.doctools.IDocCommentViewerConfiguration#createDoubleClickStrategy()
	 */
	@Override
	public ITextDoubleClickStrategy createDoubleClickStrategy() {
		return fDCStrategy;
	}

	/*
	 * @see org.eclipse.cdt.ui.text.doctools.IDocCommentViewerConfiguration#createProposalComputer()
	 */
	@Override
	public ICompletionProposalComputer createProposalComputer() {
		return fCPComputer;
	}
	
	/*
	 * @see org.eclipse.cdt.ui.text.doctools.IDocCommentViewerConfiguration#getSpellingDictionary()
	 */
	@Override
	public IDocCommentDictionary getSpellingDictionary() {
		return fDictionary;
	}
}
