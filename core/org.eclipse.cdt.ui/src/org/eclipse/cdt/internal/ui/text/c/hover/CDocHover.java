/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.c.hover;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.text.HTMLPrinter;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IFunctionSummary.IFunctionPrototypeSummary;
import org.eclipse.cdt.ui.IRequiredInclude;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.cdt.ui.text.IHoverHelpInvocationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorInput;

public class CDocHover extends AbstractCEditorTextHover {

	public CDocHover() {
	}

	/* (non-Javadoc)
	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
	 */
	@Override
	public String getHoverInfo(ITextViewer viewer, IRegion region) {
		String expression = null;

		if (getEditor() == null)
			return null;
		try {
			IDocument document = viewer.getDocument();
			if (document == null) {
				return null;
			}
			expression = document.get(region.getOffset(), region.getLength());
			expression = expression.trim();
			if (expression.isEmpty())
				return null;

			StringBuilder buffer = new StringBuilder();
			final IRegion hoverRegion = region;

			// call the Help to get info

			ICHelpInvocationContext context = new IHoverHelpInvocationContext() {
				@Override
				public IProject getProject() {
					ITranslationUnit unit = getTranslationUnit();
					if (unit != null) {
						return unit.getCProject().getProject();
					}
					return null;
				}

				@Override
				public ITranslationUnit getTranslationUnit() {
					IEditorInput editorInput = getEditor().getEditorInput();
					return CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
				}

				@Override
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
				if (fs.getDescription() != null) {
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
						buffer.append("<br>"); //$NON-NLS-1$
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
		} catch (Exception e) {
			/* Ignore */
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see ITextHover#getHoverRegion(ITextViewer, int)
	 */
	@Override
	public IRegion getHoverRegion(ITextViewer viewer, int offset) {
		if (viewer != null) {
			Point selectedRange = viewer.getSelectedRange();
			if (selectedRange.x >= 0 && selectedRange.y > 0 && offset >= selectedRange.x
					&& offset <= selectedRange.x + selectedRange.y) {
				return new Region(selectedRange.x, selectedRange.y);
			}

			return CWordFinder.findWord(viewer.getDocument(), offset);
		}
		return null;
	}
}
