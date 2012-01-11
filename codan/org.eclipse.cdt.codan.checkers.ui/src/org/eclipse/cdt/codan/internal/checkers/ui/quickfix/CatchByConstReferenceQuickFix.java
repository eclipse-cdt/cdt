/*******************************************************************************
 * Copyright (c) 2010, 2011 Alena Laskavaia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - original API and implementation in CatchByReferenceQuickFix
 *    Tomasz Wesolowski - modified for const &
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ui.Messages;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;

/**
 * Quick fix for catch by value
 */
public class CatchByConstReferenceQuickFix extends AbstractCodanCMarkerResolution {
	@Override
	public String getLabel() {
		return Messages.CatchByConstReferenceQuickFix_Message;
	}

	@Override
	public void apply(IMarker marker, IDocument document) {
		CatchByReferenceQuickFix.applyCatchByReferenceQuickFix(marker, document, true);
	}
}
