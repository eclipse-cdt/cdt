/*******************************************************************************
 * Copyright (c) 2008, 2012 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.doctools;

import org.eclipse.cdt.ui.text.ICTokenScanner;
import org.eclipse.cdt.ui.text.ITokenStoreFactory;
import org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer;
import org.eclipse.cdt.ui.text.doctools.IDocCommentDictionary;
import org.eclipse.cdt.ui.text.doctools.IDocCommentViewerConfiguration;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;

public class NullDocCommentViewerConfiguration implements IDocCommentViewerConfiguration {
	public static final IDocCommentViewerConfiguration INSTANCE = new NullDocCommentViewerConfiguration();

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
