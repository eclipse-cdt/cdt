/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;

/**
 * @author jcamelon
 */
public class CPPASTEnumerationSpecifier extends CPPASTBaseDeclSpecifier
		implements IASTEnumerationSpecifier, ICPPASTDeclSpecifier {

	private IASTName name;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier#addEnumerator(org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator)
	 */
	public void addEnumerator(IASTEnumerator enumerator) {
		if (enumerators == null) {
			enumerators = new IASTEnumerator[DEFAULT_ENUMERATORS_LIST_SIZE];
			currentIndex = 0;
		}
		if (enumerators.length == currentIndex) {
			IASTEnumerator[] old = enumerators;
			enumerators = new IASTEnumerator[old.length * 2];
			for (int i = 0; i < old.length; ++i)
				enumerators[i] = old[i];
		}
		enumerators[currentIndex++] = enumerator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier#getEnumerators()
	 */
	public IASTEnumerator[] getEnumerators() {
		if (enumerators == null)
			return IASTEnumerator.EMPTY_ENUMERATOR_ARRAY;
		removeNullEnumerators();
		return enumerators;
	}

	private void removeNullEnumerators() {
		int nullCount = 0;
		for (int i = 0; i < enumerators.length; ++i)
			if (enumerators[i] == null)
				++nullCount;
		if (nullCount == 0)
			return;
		IASTEnumerator[] old = enumerators;
		int newSize = old.length - nullCount;
		enumerators = new IASTEnumerator[newSize];
		for (int i = 0; i < newSize; ++i)
			enumerators[i] = old[i];
		currentIndex = newSize;
	}

	private int currentIndex = 0;

	private IASTEnumerator[] enumerators = null;

	private static final int DEFAULT_ENUMERATORS_LIST_SIZE = 4;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier#setName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public void setName(IASTName name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier#getName()
	 */
	public IASTName getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier#getRawSignature()
	 */
	public String getRawSignature() {
		return getName().toString() == null ? "" : getName().toString(); //$NON-NLS-1$
	}

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

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTNameOwner#getRoleForName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public int getRoleForName(IASTName n) {
		if (name == n)
			return r_declaration;
		return r_unclear;
	}
}
