/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.upc;

import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.parser.c99.C99ASTNodeFactory;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTForallStatement;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTKeywordExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSizeofExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSynchronizationStatement;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTEnumerationSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTForallStatement;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTKeywordExpression;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTSizeofExpression;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTSynchronizationStatement;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTTypedefNameSpecifier;


/**
 * Creates AST nodes that are specific to the UPC parser.
 * 
 * The methods in ASTNodeFactory that build nodes for declaration
 * specifiers are overridden here to replace thos nodes with the UPC nodes for
 * declaration specifiers. These UPC specific nodes add support
 * for 'strict', 'relaxed' and 'shared'.
 */
public class UPCASTNodeFactory extends C99ASTNodeFactory {

	public IUPCASTKeywordExpression newKeywordExpression() {
		return new UPCASTKeywordExpression();
	}
	
	public IUPCASTSizeofExpression newSizeofExpression() {
		return new UPCASTSizeofExpression();
	}
	
	public IUPCASTSynchronizationStatement newSyncronizationStatment() {
		return new UPCASTSynchronizationStatement();
	}
	
	public IUPCASTForallStatement newForallStatement() {
		return new UPCASTForallStatement();
	}

	/**
	 * Override to return UPC version of decl specifier.
	 */
	public ICASTSimpleDeclSpecifier newCSimpleDeclSpecifier() {
		return new UPCASTSimpleDeclSpecifier();
	}
	
	public ICASTCompositeTypeSpecifier newCCompositeTypeSpecifier() {
		return new UPCASTCompositeTypeSpecifier();
	}
	
	public ICASTElaboratedTypeSpecifier newCElaboratedTypeSpecifier() {
		return new UPCASTElaboratedTypeSpecifier();
	}
	
	public ICASTEnumerationSpecifier newCEnumerationSpecifier() {
		return new UPCASTEnumerationSpecifier();
	}
	
	public ICASTTypedefNameSpecifier newCTypedefNameSpecifier() {
		return new UPCASTTypedefNameSpecifier();
	}

}
