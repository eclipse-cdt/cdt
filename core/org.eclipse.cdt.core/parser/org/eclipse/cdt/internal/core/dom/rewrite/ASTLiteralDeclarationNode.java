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

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;

public class ASTLiteralDeclarationNode extends ASTLiteralNode implements IASTDeclaration {
	public ASTLiteralDeclarationNode(String code) {
		super(code);
	}

	@Override
	public IASTDeclaration copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTDeclaration copy(CopyStyle style) {
		throw new UnsupportedOperationException();
	}
}