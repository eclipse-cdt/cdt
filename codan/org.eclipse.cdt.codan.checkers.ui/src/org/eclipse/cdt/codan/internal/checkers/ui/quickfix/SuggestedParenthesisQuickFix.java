/*******************************************************************************
 * Copyright (c) 2010, 2012 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.internal.checkers.ui.Messages;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class SuggestedParenthesisQuickFix extends AbstractCodanCMarkerResolution {
	@Override
	public String getLabel() {
		return Messages.SuggestedParenthesisQuickFix_Message;
	}

	@Override
	public boolean isApplicable(IMarker marker) {
		int charEnd = marker.getAttribute(IMarker.CHAR_END, -1);
		if (charEnd == -1)
			return false;
		return true;
	}

	@Override
	public void apply(IMarker marker, IDocument document) {
		int charStart = marker.getAttribute(IMarker.CHAR_START, -1);
		int charEnd = marker.getAttribute(IMarker.CHAR_END, -1);
		if (charEnd == -1)
			return;
		try {
			document.replace(charStart, 0, "("); //$NON-NLS-1$
			document.replace(charEnd + 1, 0, ")"); //$NON-NLS-1$
		} catch (BadLocationException e) {
			CheckersUiActivator.log(e);
		}
	}
}
