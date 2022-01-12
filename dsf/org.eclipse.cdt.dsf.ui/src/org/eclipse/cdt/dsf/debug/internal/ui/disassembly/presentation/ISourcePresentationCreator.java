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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextPresentation;

/**
 * A source presentation creator is used to create a {@link TextPresentation} of a document range.
 */
public interface ISourcePresentationCreator {

	/**
	 * Dispose of this presentation creator.
	 */
	public abstract void dispose();

	/**
	 * Get a text presentation for the given region and document.
	 * @param region
	 * @param document
	 * @return a text presentation
	 */
	public abstract TextPresentation getPresentation(IRegion region, IDocument document);

}
