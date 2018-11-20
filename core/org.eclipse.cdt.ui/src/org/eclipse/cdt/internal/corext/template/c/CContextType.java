/*******************************************************************************
 * Copyright (c) 2002, 2012 QNX Software Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.template.c;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

/**
 * A context type for C/C++ code.
 */
public class CContextType extends TranslationUnitContextType {
	public static final String ID = "org.eclipse.cdt.ui.text.templates.c"; //$NON-NLS-1$

	public CContextType() {
		super();
	}

	@Override
	public TranslationUnitContext createContext(IDocument document, int offset, int length,
			ITranslationUnit translationUnit) {
		return new CContext(this, document, offset, length, translationUnit);
	}

	@Override
	public TranslationUnitContext createContext(IDocument document, Position position,
			ITranslationUnit translationUnit) {
		return new CContext(this, document, position, translationUnit);
	}
}
