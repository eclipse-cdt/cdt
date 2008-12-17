/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Emanuel Graf IFS - Bugfix for #198259
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;

/**
 * The implemented ICPPASTConversionName.
 *  
 * @author dsteffle
 */
public class CPPASTConversionName extends CPPASTName implements ICPPASTConversionName {
	
	private IASTTypeId typeId = null;
	
	public CPPASTConversionName() {
	}
	
	public CPPASTConversionName(char[] name) {
		super(name);
	}
	
	public CPPASTConversionName(char[] name, IASTTypeId typeId) {
		super(name);
		setTypeId(typeId);
	}
	
	@Override
	public CPPASTConversionName copy() {
		char[] name = toCharArray();
		CPPASTConversionName copy = new CPPASTConversionName(name == null ? null : name.clone());
		copy.setTypeId(typeId == null ? null : typeId.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	

	public IASTTypeId getTypeId() {
		return typeId;
	}

	public void setTypeId(IASTTypeId typeId) {
        assertNotFrozen();
		this.typeId=typeId;
		if (typeId != null) {
			typeId.setParent(this);
			typeId.setPropertyInParent(TYPE_ID);
		}
	}
	
	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitNames) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}		

		if(typeId != null )if(! typeId.accept( action )) return false;

		if (action.shouldVisitNames) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		return true;
	}
}
