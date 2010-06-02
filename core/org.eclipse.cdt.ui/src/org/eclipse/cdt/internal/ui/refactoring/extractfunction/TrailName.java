/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;

import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

import org.eclipse.cdt.internal.ui.refactoring.utils.ASTHelper;

class TrailName extends ASTNode{

	private int nameNumber;
	private IASTNode declaration = null;
	private IASTName realName = null;
	
	public TrailName(IASTName realName) {
		super();
		this.realName = realName;
	}

	public int getNameNumber() {
		return nameNumber;
	}

	public void setNameNumber(int nameNumber) {
		this.nameNumber = nameNumber;
	}

	public IASTDeclSpecifier getDeclSpecifier() {
		return ASTHelper.getDeclarationSpecifier(declaration);
	}

	public IASTName getRealName() {
		return realName;
	}

	public boolean isGloballyQualified() {
		IBinding bind = realName.resolveBinding();
		try {
			if (bind instanceof ICPPBinding) {
				ICPPBinding cppBind = (ICPPBinding) bind;
				return cppBind.isGloballyQualified();
			}
		} catch (DOMException e) {
		}			
		return false;
	}

	public IASTNode copy() {
		throw new UnsupportedOperationException();
	}
}
