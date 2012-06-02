/*******************************************************************************
 * Copyright (c) 2002, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.c.hover;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.ui.IEditorPart;

import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;

/**
 * CEditorTexHoverProxy
 */
public class CEditorTextHoverProxy extends AbstractCEditorTextHover {
	private CEditorTextHoverDescriptor fHoverDescriptor;
	private ICEditorTextHover fHover;

	public CEditorTextHoverProxy(CEditorTextHoverDescriptor descriptor, IEditorPart editor) {
		fHoverDescriptor= descriptor;
		setEditor(editor);
	}

	/*
	 * @see ICEditorTextHover#setEditor(IEditorPart)
	 */
	@Override
	public void setEditor(IEditorPart editor) {
		super.setEditor(editor);

		if (fHover != null)
			fHover.setEditor(getEditor());
	}

	public boolean isEnabled() {
		return true;
	}

	/*
	 * @see ITextHover#getHoverRegion(ITextViewer, int)
	 */
	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		if (ensureHoverCreated())
			return fHover.getHoverRegion(textViewer, offset);

		return null;
	}
	
	/*
	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (ensureHoverCreated())
			return fHover.getHoverInfo(textViewer, hoverRegion);

		return null;
	}

	
	/*
	 * @see org.eclipse.jface.text.ITextHoverExtension2#getHoverInfo2(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 * @since 5.0
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		if (ensureHoverCreated()) {
			if (fHover instanceof ITextHoverExtension2)
				return ((ITextHoverExtension2) fHover).getHoverInfo2(textViewer, hoverRegion);
			
			return fHover.getHoverInfo(textViewer, hoverRegion);
		}

		return null;
	}

	private boolean ensureHoverCreated() {
		if (!isEnabled() || fHoverDescriptor == null)
			return false;
		return isCreated() || createHover();
	}

	private boolean isCreated() {
		return fHover != null;
	}

	private boolean createHover() {
		fHover= fHoverDescriptor.createTextHover();
		if (fHover != null)
			fHover.setEditor(getEditor());
		return isCreated();
	}

	/*
	 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
	 * @since 3.0
	 */
	@Override
	public IInformationControlCreator getHoverControlCreator() {
		if (ensureHoverCreated() && (fHover instanceof ITextHoverExtension))
			return ((ITextHoverExtension)fHover).getHoverControlCreator();

		return null;
	}

	/*
	 * @see org.eclipse.jface.text.information.IInformationProviderExtension2#getInformationPresenterControlCreator()
	 */
	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		if (ensureHoverCreated()) {
			// this is wrong, but left here for backwards compatibility
			if (fHover instanceof IInformationProviderExtension2)
				return ((IInformationProviderExtension2) fHover).getInformationPresenterControlCreator();
		}
		return null;
	}
}
