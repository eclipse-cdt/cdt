/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.search;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.ui.browser.typeinfo.TypeInfoLabelProvider;

import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CUILabelProvider;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchLabelProvider extends LabelProvider {

	private final AbstractTextSearchViewPage fPage;
	private final TypeInfoLabelProvider fTypeInfoLabelProvider;
	private final CUILabelProvider fCElementLabelProvider;
	
	public PDOMSearchLabelProvider(AbstractTextSearchViewPage page) {
		fTypeInfoLabelProvider= new TypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_FULLY_QUALIFIED | TypeInfoLabelProvider.SHOW_PARAMETERS);
		fCElementLabelProvider= new CUILabelProvider(0, CElementImageProvider.SMALL_ICONS);
		fPage= page;
	}
	
	public Image getImage(Object element) {
		if (element instanceof PDOMSearchElement)
			return fTypeInfoLabelProvider.getImage(((PDOMSearchElement)element).getTypeInfo());

		return fCElementLabelProvider.getImage(element);
	}

	public String getText(Object element) {
		if (element instanceof PDOMSearchElement) {
			return fTypeInfoLabelProvider.getText(((PDOMSearchElement)element).getTypeInfo());
		} 
		return fCElementLabelProvider.getText(element);
	}
	
	protected int getMatchCount(Object element) {
		return fPage.getInput().getMatchCount(element);
	}
}
