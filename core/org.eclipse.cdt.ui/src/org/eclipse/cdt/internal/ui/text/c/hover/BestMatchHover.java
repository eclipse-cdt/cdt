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
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.ui.IEditorPart;

/**
 * BestMatchHover
 */
public class BestMatchHover extends AbstractCEditorTextHover {

	private List<CEditorTextHoverDescriptor> fTextHoverSpecifications;
	private List<ITextHover> fInstantiatedTextHovers;
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
		fTextHoverSpecifications= new ArrayList<CEditorTextHoverDescriptor>(2);
		fInstantiatedTextHovers= new ArrayList<ITextHover>(2);

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

		for (Iterator<CEditorTextHoverDescriptor> iterator= new ArrayList<CEditorTextHoverDescriptor>(fTextHoverSpecifications).iterator(); iterator.hasNext(); ) {
			CEditorTextHoverDescriptor spec= iterator.next();

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
	@SuppressWarnings("deprecation")
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {

		checkTextHovers();
		fBestHover= null;

		if (fInstantiatedTextHovers == null)
			return null;

		for (Iterator<ITextHover> iterator= fInstantiatedTextHovers.iterator(); iterator.hasNext(); ) {
			ITextHover hover= iterator.next();

			String s= hover.getHoverInfo(textViewer, hoverRegion);
			if (s != null && s.trim().length() > 0) {
				fBestHover= hover;
				return s;
			}
		}

		return null;
	}

	/*
	 * @see org.eclipse.jface.text.ITextHoverExtension2#getHoverInfo2(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		
		checkTextHovers();
		fBestHover= null;
		
		if (fInstantiatedTextHovers == null)
			return null;
		
		for (Iterator<ITextHover> iterator= fInstantiatedTextHovers.iterator(); iterator.hasNext(); ) {
			ITextHover hover= iterator.next();
			
			if (hover instanceof ITextHoverExtension2) {
				Object info= ((ITextHoverExtension2) hover).getHoverInfo2(textViewer, hoverRegion);
				if (info != null) {
					fBestHover= hover;
					return info;
				}
			} else {
				String s= hover.getHoverInfo(textViewer, hoverRegion);
				if (s != null && s.trim().length() > 0) {
					fBestHover= hover;
					return s;
				}
			}
		}
		
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
	 * @since 3.0
	 */
	@Override
	public IInformationControlCreator getHoverControlCreator() {
		if (fBestHover instanceof ITextHoverExtension)
			return ((ITextHoverExtension)fBestHover).getHoverControlCreator();

		return null;
	}

	/*
	 * @see org.eclipse.jface.text.information.IInformationProviderExtension2#getInformationPresenterControlCreator()
	 * @since 3.0
	 */
	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		// this is wrong, but left here for backwards compatibility
		if (fBestHover instanceof IInformationProviderExtension2)
			return ((IInformationProviderExtension2)fBestHover).getInformationPresenterControlCreator();

		return null;
	}

}
