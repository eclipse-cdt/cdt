/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.correction;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.text.IInvocationContext;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.TextInvocationContext;

public class CorrectionContext extends TextInvocationContext implements IInvocationContext {
	private ITranslationUnit fTranslationUnit;

	/*
	 * Constructor for CorrectionContext.
	 */
	public CorrectionContext(ITranslationUnit tu, ISourceViewer sourceViewer, int offset, int length) {
		super(sourceViewer, offset, length);
		fTranslationUnit = tu;
	}

	/*
	 * Constructor for CorrectionContext.
	 */
	public CorrectionContext(ITranslationUnit tu, int offset, int length) {
		this(tu, null, offset, length);
	}

	/**
	 * Returns the translation unit.
	 * @return an <code>ITranslationUnit</code>
	 */
	@Override
	public ITranslationUnit getTranslationUnit() {
		return fTranslationUnit;
	}

	/**
	 * Returns the length.
	 * @return int
	 */
	@Override
	public int getSelectionLength() {
		return Math.max(getLength(), 0);
	}

	/**
	 * Returns the offset.
	 * @return int
	 */
	@Override
	public int getSelectionOffset() {
		return getOffset();
	}
}
