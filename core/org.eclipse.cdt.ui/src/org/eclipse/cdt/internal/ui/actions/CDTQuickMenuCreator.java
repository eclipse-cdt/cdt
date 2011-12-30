/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Ported to CDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.actions.QuickMenuCreator;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.CWordFinder;

/**
 * C/C++ editor aware quick menu creator. In the given editor, the menu will be aligned with the word
 * at the current offset.
 *
 * @since 5.2
 */
public abstract class CDTQuickMenuCreator extends QuickMenuCreator {

	private final CEditor fEditor;

	/**
	 * Create a CDT quick menu creator
	 * @param editor a Java editor, or <code>null</code> if none
	 */
	public CDTQuickMenuCreator(CEditor editor) {
		fEditor= editor;
	}

	@Override
	protected Point computeMenuLocation(StyledText text) {
		if (fEditor == null || text != fEditor.getViewer().getTextWidget())
			return super.computeMenuLocation(text);
		return computeWordStart();
	}

	private Point computeWordStart() {
		ITextSelection selection= (ITextSelection)fEditor.getSelectionProvider().getSelection();
		IRegion textRegion= CWordFinder.findWord(fEditor.getViewer().getDocument(), selection.getOffset());
		if (textRegion == null)
			return null;

		IRegion widgetRegion= modelRange2WidgetRange(textRegion);
		if (widgetRegion == null)
			return null;

		int start= widgetRegion.getOffset();

		StyledText styledText= fEditor.getViewer().getTextWidget();
		Point result= styledText.getLocationAtOffset(start);
		result.y+= styledText.getLineHeight(start);

		if (!styledText.getClientArea().contains(result))
			return null;
		return result;
	}

	private IRegion modelRange2WidgetRange(IRegion region) {
		ISourceViewer viewer= fEditor.getViewer();
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5)viewer;
			return extension.modelRange2WidgetRange(region);
		}

		IRegion visibleRegion= viewer.getVisibleRegion();
		int start= region.getOffset() - visibleRegion.getOffset();
		int end= start + region.getLength();
		if (end > visibleRegion.getLength())
			end= visibleRegion.getLength();

		return new Region(start, end - start);
	}

	/**
	 * Returns a handler that can create and open the quick menu.
	 *
	 * @return a handler that can create and open the quick menu
	 */
	public IHandler createHandler() {
		return new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) throws ExecutionException {
				createMenu();
				return null;
			}
		};
	}

}
