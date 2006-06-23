/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.c.hover;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.ui.IEditorPart;

/**
 * BestMatchHover
 */
public class BestMatchHover extends AbstractCEditorTextHover implements ITextHoverExtension, IInformationProviderExtension2 {

	private List fTextHoverSpecifications;
	private List fInstantiatedTextHovers;
	private ITextHover fBestHover;

	public BestMatchHover() {
		installTextHovers();
	}

	public BestMatchHover(IEditorPart editor) {
		this();
		setEditor(editor);
	}
	
	/**
	 * Installs all text hovers.
	 */
	private void installTextHovers() {
		
		// initialize lists - indicates that the initialization happened
		fTextHoverSpecifications= new ArrayList(2);
		fInstantiatedTextHovers= new ArrayList(2);

		// populate list
		CEditorTextHoverDescriptor[] hoverDescs= CUIPlugin.getDefault().getCEditorTextHoverDescriptors();
		for (int i= 0; i < hoverDescs.length; i++) {
			// ensure that we don't add ourselves to the list
			if (!PreferenceConstants.ID_BESTMATCH_HOVER.equals(hoverDescs[i].getId()))
				fTextHoverSpecifications.add(hoverDescs[i]);
		}
	}	

	private void checkTextHovers() {
		if (fTextHoverSpecifications.size() == 0)
			return;

		for (Iterator iterator= new ArrayList(fTextHoverSpecifications).iterator(); iterator.hasNext(); ) {
			CEditorTextHoverDescriptor spec= (CEditorTextHoverDescriptor) iterator.next();

			ICEditorTextHover hover= spec.createTextHover();
			if (hover != null) {
				hover.setEditor(getEditor());
				addTextHover(hover);
				fTextHoverSpecifications.remove(spec);
			}
		}
	}

	protected void addTextHover(ITextHover hover) {
		if (!fInstantiatedTextHovers.contains(hover))
			fInstantiatedTextHovers.add(hover);
	}

	/*
	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {

		checkTextHovers();
		fBestHover= null;

		if (fInstantiatedTextHovers == null)
			return null;

		for (Iterator iterator= fInstantiatedTextHovers.iterator(); iterator.hasNext(); ) {
			ITextHover hover= (ITextHover)iterator.next();

			String s= hover.getHoverInfo(textViewer, hoverRegion);
			if (s != null && s.trim().length() > 0) {
				fBestHover= hover;
				return s;
			}
		}

		return null;
	}

	/*
	 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
	 * @since 3.0
	 */
	public IInformationControlCreator getHoverControlCreator() {
		if (fBestHover instanceof ITextHoverExtension)
			return ((ITextHoverExtension)fBestHover).getHoverControlCreator();

		return null;
	}

	/*
	 * @see org.eclipse.jface.text.information.IInformationProviderExtension2#getInformationPresenterControlCreator()
	 * @since 3.0
	 */
	public IInformationControlCreator getInformationPresenterControlCreator() {
		if (fBestHover instanceof IInformationProviderExtension2)
			return ((IInformationProviderExtension2)fBestHover).getInformationPresenterControlCreator();

		return null;
	}

}
