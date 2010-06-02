/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.corext.template.c;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * A context type for comments.
 *
 * @since 4.0
 */
public class CommentContextType extends TranslationUnitContextType {

	public static final String ID= "org.eclipse.cdt.ui.text.templates.comment"; //$NON-NLS-1$

	/**
	 * Creates a comment context type.
	 */
	public CommentContextType() {
		super();
	}
	
	/*
	 * @see org.eclipse.cdt.internal.corext.template.c.TranslationUnitContextType#createContext(org.eclipse.jface.text.IDocument, int, int, org.eclipse.cdt.core.model.ITranslationUnit)
	 */
	@Override
	public TranslationUnitContext createContext(IDocument document, int offset,
			int length, ITranslationUnit translationUnit) {
		return new CommentContext(this, document, offset, length, translationUnit);
	}

	/*
	 * @see org.eclipse.cdt.internal.corext.template.c.TranslationUnitContextType#createContext(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.Position, org.eclipse.cdt.core.model.ITranslationUnit)
	 */
	@Override
	public TranslationUnitContext createContext(IDocument document,
			Position position, ITranslationUnit translationUnit) {
		return new CommentContext(this, document, position, translationUnit);
	}

}
