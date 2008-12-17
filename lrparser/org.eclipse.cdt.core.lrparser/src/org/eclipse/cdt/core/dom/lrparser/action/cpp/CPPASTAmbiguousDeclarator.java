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
package org.eclipse.cdt.core.dom.lrparser.action.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTAmbiguity;

/**
 * TODO delete this class and use the one from the core instead
 * 
 * @author Mike Kucera
 *
 */
@SuppressWarnings("restriction")
public class CPPASTAmbiguousDeclarator extends CPPASTAmbiguity implements IASTDeclarator {

	private List<IASTDeclarator> declarators = new ArrayList<IASTDeclarator>(2); 
	
	private int defaultDeclarator = 0;
	
	
	public CPPASTAmbiguousDeclarator(IASTDeclarator ... ds) {
		for(IASTDeclarator declarator : ds)
			addDeclarator(declarator);
	}
	
	@Override
	protected IASTNode[] getNodes() {
		return declarators.toArray(new IASTDeclarator[declarators.size()]);
	}


	public void addDeclarator(IASTDeclarator declarator) {
		if(declarator != null) {
			declarators.add(declarator);
			declarator.setParent(this);
			declarator.setPropertyInParent(null); // it really doesn't matter
		}
	}

	private IASTDeclarator getDefaultDeclarator() {
		return declarators.get(defaultDeclarator);
	}
	
	public void addPointerOperator(IASTPointerOperator operator) {
		getDefaultDeclarator().addPointerOperator(operator);
	}
	
	public void setInitializer(IASTInitializer initializer) {
		getDefaultDeclarator().setInitializer(initializer);
	}

	public void setName(IASTName name) {
		getDefaultDeclarator().setName(name);
	}

	public void setNestedDeclarator(IASTDeclarator nested) {
		getDefaultDeclarator().setNestedDeclarator(nested);
	}
	
	public IASTInitializer getInitializer() {
		return getDefaultDeclarator().getInitializer();
	}

	public IASTName getName() {
		return getDefaultDeclarator().getName();
	}

	public IASTDeclarator getNestedDeclarator() {
		return getDefaultDeclarator().getNestedDeclarator();
	}

	public IASTPointerOperator[] getPointerOperators() {
		return getDefaultDeclarator().getPointerOperators();
	}

	public int getRoleForName(IASTName n) {
		return getDefaultDeclarator().getRoleForName(n);
	}

	public IASTDeclarator copy() {
		throw new UnsupportedOperationException();
	}
	
	
}
