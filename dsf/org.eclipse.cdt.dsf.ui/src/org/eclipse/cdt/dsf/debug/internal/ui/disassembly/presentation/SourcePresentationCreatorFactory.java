/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.presentation;

import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.text.ITextViewer;

/**
 * A factory for source presentation creators.
 */
public final class SourcePresentationCreatorFactory {

	public static ISourcePresentationCreator create(ILanguage language, IStorage storage, ITextViewer textViewer) {
		return new CSourcePresentationCreator(language, storage, textViewer);
	}
}
