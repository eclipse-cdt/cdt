/*******************************************************************************
 * Copyright (c) 2015 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors: 
 *     Thomas Corbat (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite;

import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class ASTLiteralStatementNode extends ASTLiteralNode implements IASTStatement {
	public ASTLiteralStatementNode(String code) {
		super(code);
	}

	@Override
	public IASTAttributeSpecifier[] getAttributeSpecifiers() {
		return IASTAttributeSpecifier.EMPTY_ATTRIBUTE_SPECIFIER_ARRAY;
	}

	@Override
	public void addAttributeSpecifier(IASTAttributeSpecifier attributeSpecifier) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public IASTAttribute[] getAttributes() {
		return IASTAttribute.EMPTY_ATTRIBUTE_ARRAY;
	}

	@Override
	public void addAttribute(IASTAttribute attribute) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTStatement copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTStatement copy(CopyStyle style) {
		throw new UnsupportedOperationException();
	}
}