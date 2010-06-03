/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
