/*******************************************************************************
 *  Copyright (c) 2006, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.upc.ast;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTKeywordExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.c.CBasicType;

@SuppressWarnings("restriction")
public class UPCASTKeywordExpression extends ASTNode implements IUPCASTKeywordExpression {

	
	private int keywordKind;
	
	public UPCASTKeywordExpression() {
	}

	public UPCASTKeywordExpression(int keywordKind) {
		this.keywordKind = keywordKind;
	}
	
	public UPCASTKeywordExpression copy() {
		UPCASTKeywordExpression copy = new UPCASTKeywordExpression(keywordKind);
		copy.setOffsetAndLength(this);
		return copy;
	}

	public int getKeywordKind() {
		return keywordKind;
	}

	public void setKeywordKind(int kind) {
		this.keywordKind = kind;
		
	}

	public IType getExpressionType() {
		return new CBasicType(Kind.eInt, 0, this);
	}

	public boolean isLValue() {
		return false;
	}

	public ValueCategory getValueCategory() {
		return ValueCategory.PRVALUE;
	}
	
	@Override
	public boolean accept(ASTVisitor visitor) {
		if(visitor.shouldVisitExpressions) {
			switch(visitor.visit(this)) {
				case ASTVisitor.PROCESS_ABORT : return false;
				case ASTVisitor.PROCESS_SKIP  : return true;
			}
		}
		if(visitor.shouldVisitExpressions) {
			switch(visitor.leave(this)) {
				case ASTVisitor.PROCESS_ABORT : return false;
				case ASTVisitor.PROCESS_SKIP  : return true;
			}
		}
		return true;
	}
	
}
