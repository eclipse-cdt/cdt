/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.StyledString.Styler;

public class ColoringLabelProvider extends DecoratingStyledCellLabelProvider implements ILabelProvider {
	public static final Styler HIGHLIGHT_STYLE= StyledString.createColorRegistryStyler(null, ColoredViewersManager.HIGHLIGHT_BG_COLOR_NAME);
	public static final Styler HIGHLIGHT_WRITE_STYLE= StyledString.createColorRegistryStyler(null, ColoredViewersManager.HIGHLIGHT_WRITE_BG_COLOR_NAME);
	
	public ColoringLabelProvider(IStyledLabelProvider labelProvider) {
		this(labelProvider, null, null);
	}
	
	public ColoringLabelProvider(IStyledLabelProvider labelProvider, ILabelDecorator decorator, IDecorationContext decorationContext) {
		super(labelProvider, decorator, decorationContext);
	}

	@Override
	public void initialize(ColumnViewer viewer, ViewerColumn column) {
		ColoredViewersManager.install(this);
		setOwnerDrawEnabled(ColoredViewersManager.showColoredLabels());
		
		super.initialize(viewer, column);
	}
		
	@Override
	public void dispose() {
		super.dispose();
		ColoredViewersManager.uninstall(this);
	}
	
	public void refresh() {
		ColumnViewer viewer= getViewer();
		
		if (viewer == null) {
			return;
		}
		boolean showColoredLabels= ColoredViewersManager.showColoredLabels();
		if (showColoredLabels != isOwnerDrawEnabled()) {
			setOwnerDrawEnabled(showColoredLabels);
			viewer.refresh();
		} else if (showColoredLabels) {
			viewer.refresh();
		}
	}
	
	@Override
	protected StyleRange prepareStyleRange(StyleRange styleRange, boolean applyColors) {
		if (!applyColors && styleRange.background != null) {
			styleRange= super.prepareStyleRange(styleRange, applyColors);
			styleRange.borderStyle= SWT.BORDER_DOT;
			return styleRange;
		}
		return super.prepareStyleRange(styleRange, applyColors);
	}
	
	@Override
	public String getText(Object element) {
		return getStyledText(element).getString();
	}

	public static StyledString decorateStyledString(StyledString string, String decorated, Styler color) {
		String label= string.getString();
		int originalStart= decorated.indexOf(label);
		if (originalStart == -1) {
			return new StyledString(decorated); // the decorator did something wild
		}
		if (originalStart > 0) {
			StyledString newString= new StyledString(decorated.substring(0, originalStart), color);
			newString.append(string);
			string= newString;
		}
		if (decorated.length() > originalStart + label.length()) { // decorator appended something
			return string.append(decorated.substring(originalStart + label.length()), color);
		}
		return string; // no change
	}
}
