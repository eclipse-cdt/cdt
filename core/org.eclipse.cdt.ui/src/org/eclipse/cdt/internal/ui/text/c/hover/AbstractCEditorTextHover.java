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

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;

import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.text.HTMLTextPresenter;

/**
 * AbstractCEditorTextHover Abstract class for providing hover information for C
 * elements.
 * 
 */
public abstract class AbstractCEditorTextHover implements ICEditorTextHover,
		ITextHoverExtension {

	private IEditorPart fEditor;

	/*
	 * @see ICEditorTextHover#setEditor(IEditorPart)
	 */
	public void setEditor(IEditorPart editor) {
		fEditor = editor;
	}

	protected IEditorPart getEditor() {
		return fEditor;
	}

	/*
	 * @see ITextHover#getHoverRegion(ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		if (textViewer != null) {
			/*
			 * If the hover offset falls within the selection range return the
			 * region for the whole selection.
			 */
			Point selectedRange = textViewer.getSelectedRange();
			if (selectedRange.x >= 0 && selectedRange.y > 0
					&& offset >= selectedRange.x
					&& offset <= selectedRange.x + selectedRange.y)
				return new Region(selectedRange.x, selectedRange.y);
			else {
				return CWordFinder.findWord(textViewer.getDocument(), offset);
			}
		}
		return null;
	}

	/*
	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
	 */
	public abstract String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion);

	/*
	 * @see ITextHoverExtension#getHoverControlCreator()
	 * @since 3.0
	 */
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, SWT.NONE,
						new HTMLTextPresenter(true),
						getTooltipAffordanceString());
			}
		};
	}

	/**
	 * Returns the tool tip affordance string.
	 * 
	 * @return the affordance string or <code>null</code> if disabled or no
	 *         key binding is defined
	 * @since 3.0
	 */
	protected String getTooltipAffordanceString() {
		//TLETODO [hover] provide affordance string
		// @see org.eclipse.jdt.internal.ui.text.java.hover.AbstractJavaEditorTextHover
		return null;
	}

}
