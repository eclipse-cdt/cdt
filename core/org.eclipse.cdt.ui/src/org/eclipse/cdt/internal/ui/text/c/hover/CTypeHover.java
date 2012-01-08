/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Marc-Andre Laperle - bug 282495
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
 * Aggregator of problem and doc hovers.
 * @since 5.0
 */
public class CTypeHover implements ICEditorTextHover, ITextHoverExtension, ITextHoverExtension2, IInformationProviderExtension2 {
	private AbstractCEditorTextHover fProblemHover;
	private AbstractCEditorTextHover fCDocHover;
	private AbstractCEditorTextHover fBestMatchHover;

	private AbstractCEditorTextHover fCurrentHover;

	public CTypeHover() {
		fProblemHover= new ProblemHover();
		fCDocHover= new CDocHover();
		fCurrentHover= null;
		fBestMatchHover = new BestMatchHover();
	}

	/*
	 * @see ICEditorTextHover#setEditor(IEditorPart)
	 */
	@Override
	public void setEditor(IEditorPart editor) {
		fProblemHover.setEditor(editor);
		fCDocHover.setEditor(editor);
		fBestMatchHover.setEditor(editor);
		fCurrentHover= null;
	}

	/*
	 * @see ITextHover#getHoverRegion(ITextViewer, int)
	 */
	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return fCDocHover.getHoverRegion(textViewer, offset);
	}

	/*
	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
	 */
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		Object info= getHoverInfo2(textViewer, hoverRegion);
		if (info != null) {
			return String.valueOf(info);
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.ITextHoverExtension2#getHoverInfo2(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		Object hoverInfo= fProblemHover.getHoverInfo2(textViewer, hoverRegion);
		if (hoverInfo != null) {
			fCurrentHover= fProblemHover;
			return hoverInfo;
		}

		hoverInfo = fCDocHover.getHoverInfo2(textViewer, hoverRegion);
		if(hoverInfo != null){
			fCurrentHover= fCDocHover;
		}
		
		hoverInfo = fBestMatchHover.getHoverInfo(textViewer, hoverRegion);
		if(hoverInfo != null){
			fCurrentHover = fBestMatchHover;
		}
		
		return hoverInfo;
	}

	/*
	 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
	 */
	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return fCurrentHover == null ? null : fCurrentHover.getHoverControlCreator();
	}

	/*
	 * @see org.eclipse.jface.text.information.IInformationProviderExtension2#getInformationPresenterControlCreator()
	 */
	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return fCurrentHover == null ? null : fCurrentHover.getInformationPresenterControlCreator();
	}
}
