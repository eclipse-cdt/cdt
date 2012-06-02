/*******************************************************************************
 * Copyright (c) 2002, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Ericsson             - Fix improper hover order (Bug 294812)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.c.hover;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.ui.IEditorPart;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;

/**
 * 'Fake' hover used to choose the best available hover.
 * This hover is always the first hover used and will delegate the hover
 * request to the best of the real hovers.  The 'best' hover is the first 
 * hover that returns some text for the specified parameters.
 * 
 * Note that hovers are ordered by plugin dependency, with the most specific
 * hovers being placed before less specific ones.
 */
public class BestMatchHover extends AbstractCEditorTextHover {
	/*
	 * Note that hover ordering is very important to be preserved by this class (bug 294812).
	 */
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
		CEditorTextHoverDescriptor[] hoverDescs= CUIPlugin.getDefault().getCEditorTextHoverDescriptors();
		
		// Initialize lists - indicates that the initialization happened
		fTextHoverSpecifications= new ArrayList<CEditorTextHoverDescriptor>(hoverDescs.length-1);
		fInstantiatedTextHovers= new ArrayList<ITextHover>(hoverDescs.length-1);

		// Populate list
		for (int i= 0; i < hoverDescs.length; i++) {
			// Ensure that we don't add ourselves to the list
			if (!PreferenceConstants.ID_BESTMATCH_HOVER.equals(hoverDescs[i].getId())) {
				fTextHoverSpecifications.add(hoverDescs[i]);
				// Add place-holder for hover instance
				fInstantiatedTextHovers.add(null);
			}
		}
	}

	private void checkTextHovers() {
		if (fTextHoverSpecifications == null)
			return;

		boolean allCreated = true;
		for (int i= 0; i < fTextHoverSpecifications.size(); ++i) {
			CEditorTextHoverDescriptor spec= fTextHoverSpecifications.get(i);
			if (spec == null) continue;
			
			ICEditorTextHover hover= spec.createTextHover();
			if (hover != null) {
				hover.setEditor(getEditor());
				// Remember instance and mark as created
				fInstantiatedTextHovers.set(i, hover);
				fTextHoverSpecifications.set(i, null);
			} else {
				allCreated = false;
			}
		}
		
		if (allCreated) {
			fTextHoverSpecifications = null;
		}
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

		for (ITextHover hover : fInstantiatedTextHovers) {
			if (hover == null) continue;

			String s= hover.getHoverInfo(textViewer, hoverRegion);
			if (s != null && s.trim().length() > 0) {
				fBestHover= hover;
				return s;
			}
		}

		return null;
	}

	/*
	 * @see ITextHoverExtension2#getHoverInfo2(ITextViewer, IRegion)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		checkTextHovers();
		fBestHover= null;
		
		if (fInstantiatedTextHovers == null)
			return null;
		
		for (ITextHover hover : fInstantiatedTextHovers) {
			if (hover == null) continue;

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
	 * @see ITextHoverExtension#getHoverControlCreator()
	 * @since 3.0
	 */
	@Override
	public IInformationControlCreator getHoverControlCreator() {
		if (fBestHover instanceof ITextHoverExtension)
			return ((ITextHoverExtension)fBestHover).getHoverControlCreator();

		return null;
	}

	/*
	 * @see IInformationProviderExtension2#getInformationPresenterControlCreator()
	 * @since 3.0
	 */
	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		// This is wrong, but left here for backwards compatibility
		if (fBestHover instanceof IInformationProviderExtension2)
			return ((IInformationProviderExtension2) fBestHover).getInformationPresenterControlCreator();

		return null;
	}
}
