/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.upc;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTForallStatement;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTKeywordExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSynchronizationStatement;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTTypedefNameSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CNodeFactory;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTEnumerationSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTForallStatement;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTKeywordExpression;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTSynchronizationStatement;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTTypeIdSizeofExpression;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTTypedefNameSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.upc.ast.UPCASTUnarySizeofExpression;


/**
 * Creates AST nodes that are specific to the UPC parser.
 *
 * The methods in ASTNodeFactory that build nodes for declaration
 * specifiers are overridden here to replace those nodes with the UPC nodes for
 * declaration specifiers. These UPC specific nodes add support
 * for 'strict', 'relaxed' and 'shared'.
 */
@SuppressWarnings("restriction")
public class UPCASTNodeFactory extends CNodeFactory implements IUPCNodeFactory {


	private boolean useUPCSizeofExpressions = false;
	private int currentUPCSizofExpressionOperator = 0;


	@Override
	public void setUseUPCSizeofExpressions(int op) {
		useUPCSizeofExpressions = true;
		currentUPCSizofExpressionOperator = op;
	}

	@Override
	public void setUseC99SizeofExpressions() {
		useUPCSizeofExpressions = false;
	}



	@Override
	public IASTTypeIdExpression newTypeIdExpression(int operator, IASTTypeId typeId) {
		if(useUPCSizeofExpressions) {
			assert operator == IASTTypeIdExpression.op_sizeof;
			return new UPCASTTypeIdSizeofExpression(currentUPCSizofExpressionOperator, typeId);
		}

		return super.newTypeIdExpression(operator, typeId);
	}


	@Override
	public IASTUnaryExpression newUnaryExpression(int operator, IASTExpression operand) {
		if(useUPCSizeofExpressions) {
			assert operator == IASTUnaryExpression.op_sizeof;
			return new UPCASTUnarySizeofExpression(currentUPCSizofExpressionOperator, operand);
		}

		return super.newUnaryExpression(operator, operand);
	}


	@Override
	public IUPCASTKeywordExpression newKeywordExpression(int keywordKind) {
		return new UPCASTKeywordExpression(keywordKind);
	}


	@Override
	public IUPCASTSynchronizationStatement newSyncronizationStatment(IASTExpression barrierExpression, int statmentKind) {
		return new UPCASTSynchronizationStatement(barrierExpression, statmentKind);
	}


	@Override
	public IUPCASTForallStatement newForallStatement(IASTStatement init, IASTExpression condition,
			IASTExpression iterationExpression, IASTStatement body, IASTExpression affinity) {
		return new UPCASTForallStatement(init, condition, iterationExpression, body, affinity);
	}


	@Override
	public IUPCASTSimpleDeclSpecifier newSimpleDeclSpecifier() {
		return new UPCASTSimpleDeclSpecifier();
	}


	@Override
	public IUPCASTCompositeTypeSpecifier newCompositeTypeSpecifier(int key, IASTName name) {
		return new UPCASTCompositeTypeSpecifier(key, name);
	}


	@Override
	public IUPCASTElaboratedTypeSpecifier newElaboratedTypeSpecifier(int kind, IASTName name) {
		return new UPCASTElaboratedTypeSpecifier(kind, name);
	}


	@Override
	public IUPCASTEnumerationSpecifier newEnumerationSpecifier(IASTName name) {
		return new UPCASTEnumerationSpecifier(name);
	}


	@Override
	public IUPCASTTypedefNameSpecifier newTypedefNameSpecifier(IASTName name) {
		return new UPCASTTypedefNameSpecifier(name);
	}

}
