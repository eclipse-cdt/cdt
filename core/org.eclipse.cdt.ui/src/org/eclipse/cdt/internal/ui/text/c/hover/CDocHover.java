/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
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


import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorInput;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IRequiredInclude;
import org.eclipse.cdt.ui.IFunctionSummary.IFunctionPrototypeSummary;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.cdt.ui.text.IHoverHelpInvocationContext;

import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.text.HTMLPrinter;

public class CDocHover extends AbstractCEditorTextHover {
	
	/**
	 * Constructor for DefaultCEditorTextHover
	 */
	public CDocHover() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	@Override
	public String getHoverInfo(ITextViewer viewer, IRegion region) {
		String expression = null;
		
		if (getEditor() == null) 
			return null;
		try {
			expression = viewer.getDocument().get(region.getOffset(), region.getLength());
			expression = expression.trim();
			if (expression.isEmpty())
				return null; 

			StringBuilder buffer = new StringBuilder();
			final IRegion hoverRegion = region;

			// call the Help to get info

			ICHelpInvocationContext context = new IHoverHelpInvocationContext() {

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

				public IRegion getHoverRegion() {
					return hoverRegion; 
				}
				
			};

			IFunctionSummary fs = CHelpProviderManager.getDefault().getFunctionInfo(context, expression);
			if (fs != null) {
				buffer.append(CEditorMessages.DefaultCEditorTextHover_html_name); 
				buffer.append(HTMLPrinter.convertToHTMLContent(fs.getName()));
				final IFunctionPrototypeSummary prototype = fs.getPrototype();
				if (prototype != null) {
					buffer.append(CEditorMessages.DefaultCEditorTextHover_html_prototype); 
					buffer.append(HTMLPrinter.convertToHTMLContent(prototype.getPrototypeString(false)));
				}
				if(fs.getDescription() != null) {
					buffer.append(CEditorMessages.DefaultCEditorTextHover_html_description); 
					//Don't convert this description since it could already be formatted
					buffer.append(fs.getDescription());
				}
				IRequiredInclude[] incs = fs.getIncludes();
				if (incs != null && incs.length > 0) {
					buffer.append(CEditorMessages.DefaultCEditorTextHover_html_includes); 
					int count = 0;
					for (IRequiredInclude inc : incs) {
						buffer.append(inc.getIncludeName());
						buffer.append("<br>");        //$NON-NLS-1$
						if (count++ > 4) {
							buffer.append("...<br>"); //$NON-NLS-1$
							break; // too long list: do not display all 
						}
					}
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
	@Override
	public IRegion getHoverRegion(ITextViewer viewer, int offset) {
		if (viewer != null) {
			Point selectedRange = viewer.getSelectedRange();
			if (selectedRange.x >= 0 && 
					selectedRange.y > 0 &&
					offset >= selectedRange.x &&
					offset <= selectedRange.x + selectedRange.y)
				return new Region( selectedRange.x, selectedRange.y );
			
			return CWordFinder.findWord(viewer.getDocument(), offset);
		}
		return null;
	}
	
}
