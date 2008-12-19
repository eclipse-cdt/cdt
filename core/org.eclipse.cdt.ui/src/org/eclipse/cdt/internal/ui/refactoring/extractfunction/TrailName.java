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
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;

import org.eclipse.cdt.internal.ui.refactoring.utils.ASTHelper;

class TrailName extends CPPASTName {

	private int nameNumber;
	private IASTNode declaration = null;
	private IASTName realName = null;
	private boolean isGloballyQualified = false;

	@Override
	public String getRawSignature() {
		return realName.getRawSignature();
	}
	
	public int getNameNumber() {
		return nameNumber;
	}

	public void setNameNumber(int nameNumber) {
		this.nameNumber = nameNumber;
	}

	public IASTNode getDeclaration() {
		return declaration;
	}

	public void setDeclaration(IASTNode declaration) {
		this.declaration = declaration;
	}

	public IASTDeclSpecifier getDeclSpecifier() {
		return ASTHelper.getDeclarationSpecifier(declaration);
	}

	public IASTName getRealName() {
		return realName;
	}

	public void setRealName(IASTName realName) {
		this.realName = realName;
	}

	public boolean isGloballyQualified() {
		return isGloballyQualified;
	}

	public void setGloballyQualified(boolean isGloballyQualified) {
		this.isGloballyQualified = isGloballyQualified;
	}
	
	@Override
	public char[] toCharArray() {
		return realName.toCharArray();
	}
}
