/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndexName;

/**
 * @author Guido Zgraggen IFS
 *
 */
public class DeclarationFinderDO {
	public IASTTranslationUnit transUnit = null;
	public String filename = null;
	public IIndexName[] allNamesPDom = null;
	public IASTName name = null;
	
	public DeclarationFinderDO(IIndexName[] allNamesPDom2, IASTTranslationUnit transUnit2, String filename2, IASTName name2) {
		this.transUnit = transUnit2;
		this.filename = filename2;
		this.allNamesPDom = allNamesPDom2;
		this.name = name2;
	}
}
