/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public class QuickFixUseDotOperator extends AbstractCodanCMarkerResolution {

	@Override
	public String getLabel() {
		return QuickFixMessages.QuickFixUseDotOperator_replace_ptr;
	}

	@Override
	public void apply(IMarker marker, IDocument document) {
		int lineNum = marker.getAttribute(IMarker.LINE_NUMBER, -1);
		try {
			if (lineNum >= 0) {
				FindReplaceDocumentAdapter dad = new FindReplaceDocumentAdapter(document);
				int lineOffset = document.getLineOffset(lineNum - 1);
				int columnOffset = getColumnOffset(marker);
				IRegion region;
				try {
					region = dad.find(lineOffset + columnOffset, "->", //$NON-NLS-1$
							/* forwardSearch */true, /* caseSensitive */true, /* wholeWord */true,
							/* regExSearch */false);
					if (region == null) {
						CheckersUiActivator.log("QuickFixUseDotOperator failed to find '->'"); //$NON-NLS-1$
						return;
					}
					document.replace(region.getOffset(), 2, "."); //$NON-NLS-1$
				} catch (BadLocationException e) {
					CheckersUiActivator.log(e);
					return;
				}
			}
			marker.delete();
		} catch (BadLocationException | CoreException e) {
			CheckersUiActivator.log(e);
		}
	}

	private int getColumnOffset(IMarker marker) {
		// Get the column offset from the problem.variable attribute which is set for
		// the generic C/C++ error message in cdt.core.
		String offset = marker.getAttribute("problem.variable", "1:"); //$NON-NLS-1$ //$NON-NLS-2$
		if (offset.charAt(offset.length() - 1) == ':') {
			String strToParse = offset.substring(0, offset.length() - 1);
			return Integer.parseInt(strToParse) - 1;
		}
		return 0;
	}

}
