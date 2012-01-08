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
package org.eclipse.cdt.internal.ui.text.doctools;

import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;

import org.eclipse.cdt.ui.text.ICTokenScanner;
import org.eclipse.cdt.ui.text.ITokenStoreFactory;
import org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer;
import org.eclipse.cdt.ui.text.doctools.IDocCommentDictionary;
import org.eclipse.cdt.ui.text.doctools.IDocCommentViewerConfiguration;

public class NullDocCommentViewerConfiguration implements IDocCommentViewerConfiguration {
	public static final IDocCommentViewerConfiguration INSTANCE= new NullDocCommentViewerConfiguration();
	
	public NullDocCommentViewerConfiguration() {
	}
	
	@Override
	public IAutoEditStrategy createAutoEditStrategy() {		
		return null;
	}

	@Override
	public ICTokenScanner createCommentScanner(ITokenStoreFactory tokenStoreFactory) {
		return null;
	}

	@Override
	public ITextDoubleClickStrategy createDoubleClickStrategy() {
		return null;
	}

	@Override
	public ICompletionProposalComputer createProposalComputer() {
		return null;
	}

	@Override
	public boolean isDocumentationComment(IDocument doc, int offset, int length) {
		return false;
	}
	
	@Override
	public IDocCommentDictionary getSpellingDictionary() {
		return null;
	}
}
