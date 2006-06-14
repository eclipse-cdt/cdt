/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author jcamelon
 */
public class CPPASTQualifiedName extends CPPASTNode implements
		ICPPASTQualifiedName {

	/**
	 * @param duple
	 */
	public CPPASTQualifiedName() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTName#resolveBinding()
	 */
	public IBinding resolveBinding() {
		// The full qualified name resolves to the same thing as the last name
		removeNullNames();
		IASTName lastName = getLastName();
		return lastName != null ? lastName.resolveBinding() : null;
	}

	public IBinding[] resolvePrefix() {
		removeNullNames();
		IASTName lastName = getLastName();
		return lastName != null ? lastName.resolvePrefix() : new IBinding[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (signature == null)
			return ""; //$NON-NLS-1$
		return signature;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName#addName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public void addName(IASTName name) {
		if (name != null) {
			namesPos++;
			names = (IASTName[]) ArrayUtil.append( IASTName.class, names, name );
		}
	}

	/**
	 * @param decls2
	 */
	private void removeNullNames() {
        names = (IASTName[]) ArrayUtil.removeNullsAfter( IASTName.class, names, namesPos );
	}

	private IASTName[] names = null;
	private int namesPos=-1;
	private boolean value;
	private String signature;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName#getNames()
	 */
	public IASTName[] getNames() {
		if (names == null)
			return IASTName.EMPTY_NAME_ARRAY;
		removeNullNames();
		return names;
	}

	public IASTName getLastName() {
		if (names == null || names.length == 0)
			return null;
		
		return names[names.length - 1];
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTName#toCharArray()
	 */
	public char[] toCharArray() {
		if (names == null)
			return "".toCharArray(); //$NON-NLS-1$
		removeNullNames();

		// count first
		int len = 0;
		for (int i = 0; i < names.length; ++i) {
			char[] n = names[i].toCharArray();
			if (n == null)
				return null;
			len += n.length;
			if (i != names.length - 1)
				len += 2;
		}

		char[] nameArray = new char[len];
		int pos = 0;
		for (int i = 0; i < names.length; i++) {
			char[] n = names[i].toCharArray();
			System.arraycopy(n, 0, nameArray, pos, n.length);
			pos += n.length;
			if (i != names.length - 1) {
				nameArray[pos++] = ':';
				nameArray[pos++] = ':';
			}
		}
		return nameArray;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName#isFullyQualified()
	 */
	public boolean isFullyQualified() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName#setFullyQualified(boolean)
	 */
	public void setFullyQualified(boolean value) {
		this.value = value;
	}

	/**
	 * @param string
	 */
	public void setValue(String string) {
		this.signature = string;

	}

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
		IASTName[] ns = getNames();
		for (int i = 0; i < ns.length; i++) {
			if (i == names.length - 1) {
				if (names[i].toCharArray().length > 0
						&& !names[i].accept(action))
					return false;
			} else if (!names[i].accept(action))
				return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTName#isDeclaration()
	 */
	public boolean isDeclaration() {
		IASTNode parent = getParent();
		if (parent instanceof IASTNameOwner) {
			int role = ((IASTNameOwner) parent).getRoleForName(this);
			if( role == IASTNameOwner.r_reference ) return false;
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTName#isReference()
	 */
	public boolean isReference() {
		IASTNode parent = getParent();
		if (parent instanceof IASTNameOwner) {
			int role = ((IASTNameOwner) parent).getRoleForName(this);
			if( role == IASTNameOwner.r_reference ) return true;
			return false;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNameOwner#getRoleForName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public int getRoleForName(IASTName n) {
		IASTName [] namez = getNames();
		for( int i = 0; i < names.length; ++i )
			if( namez[i] == n )
			{
				if( i < names.length - 1 )
					return r_reference;
				IASTNode p = getParent();
				if( i == names.length - 1 && p instanceof IASTNameOwner )
					return ((IASTNameOwner)p).getRoleForName(this);
				return r_unclear;
			}
		return r_unclear;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTName#getBinding()
	 */
	public IBinding getBinding() {
		removeNullNames();
		return names[names.length - 1].getBinding();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTName#setBinding(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public void setBinding(IBinding binding) {
		removeNullNames();
		names[names.length - 1].setBinding( binding );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName#isConversionOrOperator()
	 */
	public boolean isConversionOrOperator() {
		IASTName[] nonNullNames = getNames(); // ensure no null names
		
		int len=nonNullNames.length;
		if (nonNullNames[len-1] instanceof ICPPASTConversionName || nonNullNames[len-1] instanceof ICPPASTOperatorName) return true;
		
		// check templateId's name
		if (nonNullNames[len-1] instanceof ICPPASTTemplateId) {
			IASTName tempName = ((ICPPASTTemplateId)nonNullNames[len-1]).getTemplateName();
			if (tempName instanceof ICPPASTConversionName || tempName instanceof ICPPASTOperatorName) return true;
		}
		
		return false;
	}
    
    public boolean isDefinition() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            if( role == IASTNameOwner.r_definition ) return true;
            return false;
        }
        return false;
    }


}
