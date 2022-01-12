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
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.PresentationReconciler;

/**
 * Presentation reconciler, adding functionality for operation without a viewer.
 * Cloned from JDT.
 *
 * @since 4.0
 */
public class CPresentationReconciler extends PresentationReconciler {
	/** Last used document */
	private IDocument fLastDocument;

	/**
	 * Constructs a "repair description" for the given damage and returns
	 * this description as a text presentation.
	 * <p>
	 * NOTE: Should not be used if this reconciler is installed on a viewer.
	 * </p>
	 *
	 * @param damage the damage to be repaired
	 * @param document the document whose presentation must be repaired
	 * @return the presentation repair description as text presentation
	 */
	public TextPresentation createRepairDescription(IRegion damage, IDocument document) {
		if (document != fLastDocument) {
			setDocumentToDamagers(document);
			setDocumentToRepairers(document);
			fLastDocument = document;
		}
		return createPresentation(damage, document);
	}
}
