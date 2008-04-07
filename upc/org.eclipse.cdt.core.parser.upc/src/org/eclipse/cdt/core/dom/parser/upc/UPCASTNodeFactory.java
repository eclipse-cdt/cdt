/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.upc;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.lrparser.action.c99.C99ASTNodeFactory;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTForallStatement;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTKeywordExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSynchronizationStatement;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTEnumerationSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTForallStatement;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTKeywordExpression;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTSynchronizationStatement;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTTypeIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTTypedefNameSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTUnaryExpression;


/**
 * Creates AST nodes that are specific to the UPC parser.
 * 
 * The methods in ASTNodeFactory that build nodes for declaration
 * specifiers are overridden here to replace those nodes with the UPC nodes for
 * declaration specifiers. These UPC specific nodes add support
 * for 'strict', 'relaxed' and 'shared'.
 */
public class UPCASTNodeFactory extends C99ASTNodeFactory {

	@Override
	public IASTTypeIdExpression newTypeIdExpression(int operator, IASTTypeId typeId) {
		return new UPCASTTypeIdExpression(operator, typeId);
	}

	public IUPCASTKeywordExpression newKeywordExpression(int keywordKind) {
		return new UPCASTKeywordExpression(keywordKind);
	}
	
	@Override
	public IUPCASTUnaryExpression newUnaryExpression(int operator, IASTExpression operand) {
		return new UPCASTUnaryExpression(operator, operand);
	}
	
	public IUPCASTSynchronizationStatement newSyncronizationStatment(IASTExpression barrierExpression, int statmentKind) {
		return new UPCASTSynchronizationStatement(barrierExpression, statmentKind);
	}
	
	public IUPCASTForallStatement newForallStatement(IASTStatement init, IASTExpression condition,
			IASTExpression iterationExpression, IASTStatement body, IASTExpression affinity) {
		return new UPCASTForallStatement(init, condition, iterationExpression, body, affinity);
	}

	/**
	 * Override to return UPC version of decl specifier.
	 */
	@Override
	public ICASTSimpleDeclSpecifier newCSimpleDeclSpecifier() {
		return new UPCASTSimpleDeclSpecifier();
	}
	
	@Override
	public ICASTCompositeTypeSpecifier newCCompositeTypeSpecifier(int key, IASTName name) {
		return new UPCASTCompositeTypeSpecifier(key, name);
	}
	
	@Override
	public ICASTElaboratedTypeSpecifier newElaboratedTypeSpecifier(int kind, IASTName name) {
		return new UPCASTElaboratedTypeSpecifier(kind, name);
	}
	
	@Override
	public ICASTEnumerationSpecifier newEnumerationSpecifier(IASTName name) {
		return new UPCASTEnumerationSpecifier(name);
	}
	
	@Override
	public ICASTTypedefNameSpecifier newCTypedefNameSpecifier() {
		return new UPCASTTypedefNameSpecifier();
	}

}
