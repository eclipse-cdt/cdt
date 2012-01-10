/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Ed Swartz (Nokia)
 *     Andrey Eremchenko (LEDAS)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.ui.search;

import java.net.URI;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.browser.typeinfo.TypeInfoLabelProvider;

import org.eclipse.cdt.internal.core.dom.parser.ASTProblem;
import org.eclipse.cdt.internal.core.model.TranslationUnit;

import org.eclipse.cdt.internal.ui.search.LineSearchElement.Match;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CUILabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.ColoringLabelProvider;

/**
 * The content in the tree and list views may be either:
 * <p>
 * IStatus - warnings or errors from the search<br>
 * ICElement - for C/C++ elements, including TUs, folders, projects<br>
 * IPath - directory container, full path<br>
 * 		IIndexFileLocation - for file entries inside IPath directory containers<br>
 * {@link IPDOMSearchContentProvider#URI_CONTAINER} - container for URIs<br>
 * 		URI - for IIndexFileLocations not resolvable to the local filesystem, under URI_CONTAINER<br>
 * @author Doug Schaefer
 * @author Ed Swartz
 */
public class CSearchLabelProvider extends LabelProvider implements IStyledLabelProvider {
	protected final CSearchViewPage fPage;
	private final TypeInfoLabelProvider fTypeInfoLabelProvider;
	private final CUILabelProvider fCElementLabelProvider;
	
	public CSearchLabelProvider(CSearchViewPage page) {
		fTypeInfoLabelProvider= new TypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_FULLY_QUALIFIED | TypeInfoLabelProvider.SHOW_PARAMETERS);
		fCElementLabelProvider= new CUILabelProvider(0, CElementImageProvider.SMALL_ICONS);
		fPage= page;
	}
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof LineSearchElement) {
			LineSearchElement lineSearchElement = (LineSearchElement) element;
			ICElement enclosingElement = lineSearchElement.getMatches()[0].getEnclosingElement();
			if (!fPage.isShowEnclosingDefinitions() || enclosingElement == null)
				return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_SEARCH_LINE);
			element = enclosingElement;
		}
		
		if (element instanceof TypeInfoSearchElement)
			return fTypeInfoLabelProvider.getImage(((TypeInfoSearchElement)element).getTypeInfo());

		if (element instanceof ProblemSearchElement) {
			return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_REFACTORING_WARNING);
		}
		
		if (element instanceof IIndexFileLocation
				|| element instanceof URI) {
			return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_INCLUDE);
		}
		
		if (element == IPDOMSearchContentProvider.URI_CONTAINER) {
			// TODO: perhaps a better icon?
			return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_INCLUDES_CONTAINER);
		}

		if (element instanceof IPath) {
			return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_INCLUDES_FOLDER);
		}
		
		if (element instanceof IStatus) {
			IStatus status = (IStatus) element;
			ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
			switch (status.getSeverity()) {
				case IStatus.WARNING:
					return sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
				case IStatus.ERROR:
					return sharedImages.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
				default:
					return sharedImages.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
			}
		}
		
		return fCElementLabelProvider.getImage(element);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof LineSearchElement) {
			return element.toString();
		}

		if (element instanceof TypeInfoSearchElement) {
			return fTypeInfoLabelProvider.getText(((TypeInfoSearchElement)element).getTypeInfo());
		}
		else if (element instanceof ProblemSearchElement) {
			ProblemSearchElement pse= (ProblemSearchElement) element;
			return ASTProblem.getMessage(pse.getProblemID(), pse.getDetail()); 
		}
		
		if (element instanceof IPath) {
			return ((IPath) element).toString();
		}
		
		if (element instanceof IIndexFileLocation) {
			IPath path= IndexLocationFactory.getPath((IIndexFileLocation)element); 
			if (path != null) {
				// these are categorized into directories already
				return path.lastSegment();
			}
		}
		
		if (element instanceof URI) {
			return ((URI) element).toString();
		}
		
		if (element instanceof IStatus) {
			return ((IStatus) element).getMessage();
		}
		
		return fCElementLabelProvider.getText(element);
	}
	
	protected int getMatchCount(Object element) {
		if (element instanceof TranslationUnit) {
			TranslationUnit translationUnit = (TranslationUnit) element;
			AbstractTextSearchResult searchResult = fPage.getInput();
			if (searchResult instanceof CSearchResult) {
				CSearchResult pdomSearchResult = (CSearchResult)searchResult;
				return pdomSearchResult.computeContainedMatches(searchResult, translationUnit.getFile()).length;
			}
		}
		return fPage.getInput().getMatchCount(element);
	}

	@Override
	public StyledString getStyledText(Object element) {
		if (!(element instanceof LineSearchElement))
			return new StyledString(getText(element));
		LineSearchElement lineElement = (LineSearchElement) element;
		int lineOffset = lineElement.getOffset();
		String lineContent = lineElement.getContent();
		StyledString styled = new StyledString(lineContent);
		for (Match match : lineElement.getMatches()) {
			int offset = Math.max(0, match.getOffset() - lineOffset);
			int length = Math.min(match.getLength(), lineContent.length() - offset);
			Styler style = match.isWriteAccess() ? ColoringLabelProvider.HIGHLIGHT_WRITE_STYLE : ColoringLabelProvider.HIGHLIGHT_STYLE;
			styled.setStyle(offset, length, style);
		}
		return styled;
	}
}
