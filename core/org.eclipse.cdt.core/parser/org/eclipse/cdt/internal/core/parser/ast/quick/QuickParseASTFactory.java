/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.quick;

import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.internal.core.parser.TokenDuple;
import org.eclipse.cdt.internal.core.parser.Parser.Backtrack;
import org.eclipse.cdt.internal.core.parser.ast.BaseASTFactory;
import org.eclipse.cdt.internal.core.parser.ast.IASTFactory;
import org.eclipse.cdt.internal.core.parser.ast.full.IASTASMDefinition;
import org.eclipse.cdt.internal.core.parser.ast.full.IASTCompilationUnit;
import org.eclipse.cdt.internal.core.parser.ast.full.IASTLinkageSpecification;
import org.eclipse.cdt.internal.core.parser.ast.full.IASTNamespaceDefinition;
import org.eclipse.cdt.internal.core.parser.ast.full.IASTScope;

/**
 * @author jcamelon
 *
 */
public class QuickParseASTFactory extends BaseASTFactory implements IASTFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createUsingDirective(org.eclipse.cdt.internal.core.parser.ast.IASTScope, org.eclipse.cdt.internal.core.parser.TokenDuple)
	 */
	public IASTUsingDirective createUsingDirective(IASTScope scope, TokenDuple duple) throws Backtrack {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createASMDefinition(org.eclipse.cdt.internal.core.parser.ast.IASTScope, java.lang.String, int, int)
	 */
	public IASTASMDefinition createASMDefinition(IASTScope scope, String assembly, int first, int last) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createNamespaceDefinition(int, java.lang.String, int)
	 */
	public IASTNamespaceDefinition createNamespaceDefinition(int first, String identifier, int nameOffset) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createCompilationUnit()
	 */
	public IASTCompilationUnit createCompilationUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createLinkageSpecification(java.lang.String)
	 */
	public IASTLinkageSpecification createLinkageSpecification(String spec) {
		// TODO Auto-generated method stub
		return null;
	}

}
