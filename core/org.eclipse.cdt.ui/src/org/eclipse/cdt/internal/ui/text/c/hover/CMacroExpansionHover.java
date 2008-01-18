/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.c.hover;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

/**
 * A hover to explore macro expansion.
 *
 * @since 5.0
 */
public class CMacroExpansionHover extends AbstractCEditorTextHover implements IInformationProviderExtension2 {

	private Reference fCache;

	/*
	 * @see org.eclipse.cdt.internal.ui.text.c.hover.AbstractCEditorTextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		CMacroExpansionInput input= CMacroExpansionInput.create(getEditor(), hoverRegion, false);
		if (input == null) {
			return null;
		}
		input.fStartWithFullExpansion= true;
		fCache= new SoftReference(input);
		String result= input.fExplorer.getFullExpansion().getCodeAfterStep();
		if (result.length() == 0) {
			// expansion is empty - hover should show empty string
			result= "/* EMPTY */"; //$NON-NLS-1$
		}
		return result;
	}
	
	/*
	 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
	 */
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new CMacroExpansionControl(parent, getTooltipAffordanceString());
			}
		};
	}

	/*
	 * @see IInformationProviderExtension2#getInformationPresenterControlCreator()
	 */
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				int shellStyle= SWT.RESIZE;
				int style= SWT.V_SCROLL | SWT.H_SCROLL;
				return new CMacroExpansionExplorationControl(parent, shellStyle, style, getCachedMacroExpansionInput());
			}
		};
	}

	protected CMacroExpansionInput getCachedMacroExpansionInput() {
		if (fCache == null) {
			return null;
		}
		CMacroExpansionInput input= (CMacroExpansionInput) fCache.get();
		fCache= null;
		if (input == null) {
			IEditorPart editor= getEditor();
			if (editor != null) {
				ISelectionProvider provider= editor.getSite().getSelectionProvider();
				ISelection selection= provider.getSelection();
				if (selection instanceof ITextSelection) {
					ITextSelection textSelection= (ITextSelection) selection;
					IRegion region= new Region(textSelection.getOffset(), textSelection.getLength());
					input= CMacroExpansionInput.create(editor, region, true);
					input.fStartWithFullExpansion= true;
				}
			}
		}
		return input;
	}

}
