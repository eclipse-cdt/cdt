/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeTraitSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTypeTraitType.TypeTraitOperator;

/**
 * Implementation of ICPPASTTypeTraitSpecifier.
 */
public class CPPASTTypeTraitSpecifier extends CPPASTBaseDeclSpecifier implements ICPPASTTypeTraitSpecifier {
	private TypeTraitOperator fOperator;
	private ICPPASTTypeId fOperand;
	
	public CPPASTTypeTraitSpecifier(TypeTraitOperator operator, ICPPASTTypeId operand) {
		fOperator = operator;
		fOperand = operand;
		fOperand.setParent(this);
		fOperand.setPropertyInParent(OPERAND);
	}
	
	@Override
	public ICPPASTDeclSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTTypeTraitSpecifier copy(CopyStyle style) {
		CPPASTTypeTraitSpecifier copy = new CPPASTTypeTraitSpecifier(fOperator, fOperand.copy(style));
		return super.copy(copy, style);
	}

	@Override
	public TypeTraitOperator getOperator() {
		return fOperator;
	}

	@Override
	public ICPPASTTypeId getOperand() {
		return fOperand;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclSpecifiers) {
			switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default: break;
			}
		}
		
        if (!fOperand.accept(action)) 
        	return false;
        
        if (action.shouldVisitDeclSpecifiers) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default: break;
	        }
		}
        
        return true;
	}
}
