/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalEnumerationSpecifier;

/**
 * AST node for c++ enumeration specifiers.
 */
public class CPPASTEnumerationSpecifier extends CPPASTBaseDeclSpecifier
		implements IASTInternalEnumerationSpecifier {

	private IASTName name;
	private boolean valuesComputed= false;

	
	public CPPASTEnumerationSpecifier() {
	}

	public CPPASTEnumerationSpecifier(IASTName name) {
		setName(name);
	}
	
	public CPPASTEnumerationSpecifier copy() {
		CPPASTEnumerationSpecifier copy = new CPPASTEnumerationSpecifier(name == null ? null : name.copy());
		for(IASTEnumerator enumerator : getEnumerators())
			copy.addEnumerator(enumerator == null ? null : enumerator.copy());
		copyBaseDeclSpec(copy);
		return copy;
	}
	
	
	public boolean startValueComputation() {
		if (valuesComputed)
			return false;
		
		valuesComputed= true;
		return true;
	}

	public void addEnumerator(IASTEnumerator enumerator) {
        assertNotFrozen();
		if (enumerator != null) {
			enumerator.setParent(this);
			enumerator.setPropertyInParent(ENUMERATOR);
			enumerators = (IASTEnumerator[]) ArrayUtil.append( IASTEnumerator.class, enumerators, ++enumeratorsPos, enumerator );
		}
	}

	public IASTEnumerator[] getEnumerators() {
		if (enumerators == null)
			return IASTEnumerator.EMPTY_ENUMERATOR_ARRAY;
		enumerators = (IASTEnumerator[]) ArrayUtil.removeNullsAfter( IASTEnumerator.class, enumerators, enumeratorsPos );
		return enumerators;
	}


	private IASTEnumerator[] enumerators = null;
	private int enumeratorsPos=-1;

	public void setName(IASTName name) {
        assertNotFrozen();
		this.name = name;
		if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(ENUMERATION_NAME);
		}
	}

	public IASTName getName() {
		return name;
	}

	@Override
	public String getRawSignature() {
		return getName().toString() == null ? "" : getName().toString(); //$NON-NLS-1$
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclSpecifiers) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		if (name != null)
			if (!name.accept(action))
				return false;
		IASTEnumerator[] enums = getEnumerators();
		for (int i = 0; i < enums.length; i++)
			if (!enums[i].accept(action))
				return false;
				
		if( action.shouldVisitDeclSpecifiers ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}

		return true;
	}

	public int getRoleForName(IASTName n) {
		if (name == n)
			return r_definition;
		return r_unclear;
	}
}
