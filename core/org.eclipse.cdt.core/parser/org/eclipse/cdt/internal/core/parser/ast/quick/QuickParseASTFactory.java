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

import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.internal.core.parser.TokenDuple;
import org.eclipse.cdt.internal.core.parser.Parser.Backtrack;
import org.eclipse.cdt.internal.core.parser.ast.BaseASTFactory;

/**
 * @author jcamelon
 *
 */
public class QuickParseASTFactory extends BaseASTFactory implements IASTFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createUsingDirective(org.eclipse.cdt.internal.core.parser.ast.IASTScope, org.eclipse.cdt.internal.core.parser.TokenDuple)
	 */
	public IASTUsingDirective createUsingDirective(IASTScope scope, TokenDuple duple) throws Backtrack {
		return new ASTUsingDirective( scope, duple.toString() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createASMDefinition(org.eclipse.cdt.internal.core.parser.ast.IASTScope, java.lang.String, int, int)
	 */
	public IASTASMDefinition createASMDefinition(IASTScope scope, String assembly, int first, int last) {		
		IASTASMDefinition definition = new ASTASMDefinition( scope, assembly ); 
		definition.setStartingOffset( first ); 
		definition.setEndingOffset( last );
		return definition; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createNamespaceDefinition(int, java.lang.String, int)
	 */
	public IASTNamespaceDefinition createNamespaceDefinition(IASTScope scope, String identifier, int first, int nameOffset) {
		IASTNamespaceDefinition definition = new ASTNamespaceDefinition( scope, identifier );
		definition.setStartingOffset( first );
		definition.setNameOffset( nameOffset );
		return definition;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createCompilationUnit()
	 */
	public IASTCompilationUnit createCompilationUnit() {
		return new ASTCompilationUnit();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createLinkageSpecification(java.lang.String)
	 */
	public IASTLinkageSpecification createLinkageSpecification(IASTScope scope, String spec) {
		return new ASTLinkageSpecification( scope, spec );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createUsingDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope, boolean, org.eclipse.cdt.internal.core.parser.TokenDuple)
	 */
	public IASTUsingDeclaration createUsingDeclaration(IASTScope scope, boolean isTypeName, TokenDuple name) {
		return new ASTUsingDeclaration( scope, isTypeName, name.toString() );
	}

}
