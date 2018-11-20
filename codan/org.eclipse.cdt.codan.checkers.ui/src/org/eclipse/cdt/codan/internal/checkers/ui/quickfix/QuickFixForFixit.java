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
import org.eclipse.cdt.internal.errorparsers.Fixit;
import org.eclipse.cdt.internal.errorparsers.FixitManager;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class QuickFixForFixit extends AbstractCodanCMarkerResolution {

	@Override
	public String getLabel() {
		return QuickFixMessages.QuickFixForFixit_apply_fixit;
	}

	@Override
	public void apply(IMarker marker, IDocument document) {
		int lineNum = marker.getAttribute(IMarker.LINE_NUMBER, -1);
		try {
			if (lineNum >= 0) {
				Fixit f = FixitManager.getInstance().findFixit(marker);
				int lineOffset = document.getLineOffset(f.getLineNumber() - 1);
				int columnOffset = f.getColumnNumber() - 1;
				try {
					document.replace(lineOffset + columnOffset, f.getLength(), f.getChange());
				} catch (BadLocationException e) {
					return;
				}
			}
			FixitManager.getInstance().deleteMarker(marker);
			marker.delete();
		} catch (BadLocationException | CoreException e) {
			CheckersUiActivator.log(e);
		}
	}

	@Override
	public boolean isApplicable(IMarker marker) {
		return FixitManager.getInstance().hasFixit(marker);
	}
}
