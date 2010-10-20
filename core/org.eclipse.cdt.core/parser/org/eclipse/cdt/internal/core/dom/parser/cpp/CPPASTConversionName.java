/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Devin Steffler (IBM) - Initial API and implementation
 *    Emanuel Graf IFS - Fix for #198259
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Implementation of conversion function ids
 */
public class CPPASTConversionName extends CPPASTNameBase implements ICPPASTConversionName {
	private IASTTypeId typeId = null;
	private char[] fName;
	
	public CPPASTConversionName() {
	}
	
	public CPPASTConversionName(IASTTypeId typeId) {
		setTypeId(typeId);
	}
	
	public CPPASTConversionName copy() {
		CPPASTConversionName copy = new CPPASTConversionName();
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

	@Override
	protected IBinding createIntermediateBinding() {
		return CPPVisitor.createBinding(this);
	}

	public char[] toCharArray() {
		if (fName == null) {
			IType t= null;
			if (typeId != null) {
				t= CPPVisitor.createType(typeId);
			}
			fName= createName(t, typeId);
		}
		return fName;
	}

	public static char[] createName(IType t, IASTNode typeId) {
		StringBuilder buf= new StringBuilder();
		buf.append(Keywords.cOPERATOR);
		buf.append(' ');
		if (t != null) {
			ASTTypeUtil.appendType(t, true, buf);
		} else {
			buf.append(typeId.getRawSignature());
			WHITESPACE_SEQ.matcher(buf).replaceAll(" "); //$NON-NLS-1$
		}
		final int len= buf.length();
		char[] name= new char[len];
		buf.getChars(0, len, name, 0);
		return name;
	}

	public char[] getSimpleID() {
		return toCharArray();
	}
	
	public char[] getLookupKey() {
		return Keywords.cOPERATOR;
	}
}
