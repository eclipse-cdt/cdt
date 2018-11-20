/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.c.hover;

import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.IWorkbenchPartOrientation;

/**
 * A hover to explore macro expansion.
 *
 * @since 5.0
 */
public class CMacroExpansionHover extends AbstractCEditorTextHover {
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		Object hoverInfo = getHoverInfo2(textViewer, hoverRegion);
		return hoverInfo != null ? hoverInfo.toString() : null;
	}

	/*
	 * @see AbstractCEditorTextHover#getHoverInfo2(ITextViewer, IRegion)
	 */
	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		CMacroExpansionInput input = CMacroExpansionInput.create(getEditor(), hoverRegion, false);
		return input;
	}

	/*
	 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
	 */
	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				return new CMacroExpansionControl(parent, getTooltipAffordanceString());
			}
		};
	}

	/*
	 * @see AbstractCEditorTextHover#getInformationPresenterControlCreator()
	 */
	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				IEditorPart editor = getEditor();
				int orientation = SWT.NONE;
				if (editor instanceof IWorkbenchPartOrientation)
					orientation = ((IWorkbenchPartOrientation) editor).getOrientation();
				return new SourceViewerInformationControl(parent, true, orientation, null);
			}
		};
	}
}
