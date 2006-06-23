/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.c.hover;


import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.text.HTMLPrinter;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorInput;

public class CDocHover extends AbstractCEditorTextHover {
	
	/**
	 * Constructor for DefaultCEditorTextHover
	 */
	public CDocHover() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	public String getHoverInfo(ITextViewer viewer, IRegion region) {
		String expression = null;
		
		if (getEditor() == null) 
			return null;
		try {
			expression = viewer.getDocument().get(region.getOffset(), region.getLength());
			expression = expression.trim();
			if (expression.length() == 0)
				return null; 

			StringBuffer buffer = new StringBuffer();

			// call the Help to get info

			ICHelpInvocationContext context = new ICHelpInvocationContext() {

				public IProject getProject() {
					ITranslationUnit unit = getTranslationUnit();
					if (unit != null) {
						return unit.getCProject().getProject();
					}
					return null;
				}

				public ITranslationUnit getTranslationUnit() {
					IEditorInput editorInput= getEditor().getEditorInput();
					return CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
				}	
			};

			IFunctionSummary fs = CHelpProviderManager.getDefault().getFunctionInfo(context, expression);
			if (fs != null) {
				buffer.append(CEditorMessages.getString("DefaultCEditorTextHover.html.name")); //$NON-NLS-1$
				buffer.append(HTMLPrinter.convertToHTMLContent(fs.getName()));
				buffer.append(CEditorMessages.getString("DefaultCEditorTextHover.html.prototype")); //$NON-NLS-1$
				buffer.append(HTMLPrinter.convertToHTMLContent(fs.getPrototype().getPrototypeString(false)));
				if(fs.getDescription() != null) {
					buffer.append(CEditorMessages.getString("DefaultCEditorTextHover.html.description")); //$NON-NLS-1$
					//Don't convert this description since it could already be formatted
					buffer.append(fs.getDescription());
				}
			} 
			if (buffer.length() > 0) {
				HTMLPrinter.insertPageProlog(buffer, 0);
				HTMLPrinter.addPageEpilog(buffer);
				return buffer.toString();
			}
		} catch(Exception ex) {
			/* Ignore */
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer viewer, int offset) {
		Point selectedRange = viewer.getSelectedRange();
		if (selectedRange.x >= 0 && 
			 selectedRange.y > 0 &&
			 offset >= selectedRange.x &&
			 offset <= selectedRange.x + selectedRange.y)
			return new Region( selectedRange.x, selectedRange.y );
		if (viewer != null)
			return CWordFinder.findWord(viewer.getDocument(), offset);
		return null;
	}
	
}
