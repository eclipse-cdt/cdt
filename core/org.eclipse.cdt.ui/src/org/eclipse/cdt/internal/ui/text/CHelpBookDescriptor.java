/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.ICHelpBook;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class represents the CHelpBook settings
 * 
 * @since 2.1
 */
public class CHelpBookDescriptor {
	final private static String ELEMENT_BOOK = "book"; //$NON-NLS-1$
	final private static String ATTRIBUTE_TITLE = "title"; //$NON-NLS-1$
	final private static String ATTRIBUTE_ENABLED = "enabled"; //$NON-NLS-1$
	final private static String VALUE_TRUE = "true"; //$NON-NLS-1$
	final private static String VALUE_FALSE = "false"; //$NON-NLS-1$

	private boolean fEnabled = true;
	private ICHelpBook fCHelpBook;
	
	CHelpBookDescriptor(ICHelpBook book){
		this(book,null);
	}

	CHelpBookDescriptor(ICHelpBook book, Element parentElement){
		fCHelpBook = book;
		
		if(parentElement == null)
			return;
		
		NodeList bookElements = parentElement.getElementsByTagName(ELEMENT_BOOK);
		if(bookElements.getLength() == 0)
			return;
		
		String title = book.getTitle();
		for(int i = 0; i < bookElements.getLength(); i++){
			Element bookElement = (Element)bookElements.item(i);
			if(title.equals(bookElement.getAttribute(ATTRIBUTE_TITLE))){
				fEnabled = VALUE_TRUE.equalsIgnoreCase(bookElement.getAttribute(ATTRIBUTE_ENABLED));
				break;
			}
		}
	}
	
	public boolean isEnabled(){
		return fEnabled;
	}
	
	public boolean matches(ICHelpInvocationContext context){
		ITranslationUnit unit = context.getTranslationUnit();
		if(unit != null)
			return matches(unit);
		IProject project = context.getProject();
		if(project != null)
			return matches(project);
		return true;
	}
	
	public boolean matches(IProject project){
		ICHelpBook book = getCHelpBook();
		boolean bMatches = false;
		switch(book.getCHelpType()){
			case ICHelpBook.HELP_TYPE_CPP:
				try{
					bMatches = project.hasNature(CCProjectNature.CC_NATURE_ID);
				}catch(CoreException e){
				}
				break;
			case ICHelpBook.HELP_TYPE_C:
			case ICHelpBook.HELP_TYPE_ASM:
				try{
					bMatches = project.hasNature(CProjectNature.C_NATURE_ID);
				}catch(CoreException e){
				}
				break;
			default:
				bMatches = true;
		}
		return bMatches;
	}
	
	public boolean matches(ITranslationUnit unit){
		ICHelpBook book = getCHelpBook();
		boolean bMatches = false;
		switch(book.getCHelpType()){
			case ICHelpBook.HELP_TYPE_CPP:
				bMatches = unit.isCXXLanguage();
				break;
			case ICHelpBook.HELP_TYPE_C:
				bMatches = unit.isCLanguage() || unit.isCXXLanguage();
				break;
			case ICHelpBook.HELP_TYPE_ASM:
				bMatches = unit.isASMLanguage();
				break;
			default:
				bMatches = true;
		}
		return bMatches;
	}
	
	public void enable(boolean enable){
		fEnabled = enable;
	}
	
	public ICHelpBook getCHelpBook(){
		return fCHelpBook;
	}
	
	public void serialize(Document doc, Element parentElement){
		Element bookElement = doc.createElement(ELEMENT_BOOK);
		bookElement.setAttribute(ATTRIBUTE_TITLE,getCHelpBook().getTitle());
		bookElement.setAttribute(ATTRIBUTE_ENABLED,fEnabled ? VALUE_TRUE : VALUE_FALSE);
		parentElement.appendChild(bookElement);
	}
}
