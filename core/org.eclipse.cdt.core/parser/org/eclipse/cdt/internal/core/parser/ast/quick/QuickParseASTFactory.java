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

import java.util.List;

import org.eclipse.cdt.core.parser.Backtrack;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.AccessVisibility;
import org.eclipse.cdt.core.parser.ast.ClassKind;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier.ClassNameType;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.internal.core.parser.ast.BaseASTFactory;

/**
 * @author jcamelon
 *
 */
public class QuickParseASTFactory extends BaseASTFactory implements IASTFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createUsingDirective(org.eclipse.cdt.internal.core.parser.ast.IASTScope, org.eclipse.cdt.internal.core.parser.TokenDuple)
	 */
	public IASTUsingDirective createUsingDirective(IASTScope scope, ITokenDuple duple) throws Backtrack {
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
	public IASTUsingDeclaration createUsingDeclaration(IASTScope scope, boolean isTypeName, ITokenDuple name) {
		return new ASTUsingDeclaration( scope, isTypeName, name.toString() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, org.eclipse.cdt.core.parser.ast.ClassKind, org.eclipse.cdt.core.parser.ast.ClassNameType, org.eclipse.cdt.core.parser.ast.AccessVisibility, org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
	 */
	public IASTClassSpecifier createClassSpecifier(IASTScope scope, String name, ClassKind kind, ClassNameType type, AccessVisibility access, IASTTemplateDeclaration ownerTemplateDeclaration, int startingOffset, int nameOffset) {
		IASTClassSpecifier spec = new ASTClassSpecifier( scope, name, kind, type, access, ownerTemplateDeclaration );
		spec.setStartingOffset( startingOffset );
		spec.setNameOffset( nameOffset );
		return spec;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#addBaseSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier, boolean, org.eclipse.cdt.core.parser.ast.AccessVisibility, java.lang.String)
	 */
	public void addBaseSpecifier(IASTClassSpecifier astClassSpec, boolean isVirtual, AccessVisibility visibility, String string) {
		IASTBaseSpecifier baseSpecifier = new ASTBaseSpecifier( astClassSpec, isVirtual, visibility );
		((IASTQClassSpecifier)astClassSpec).addBaseClass(baseSpecifier);
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createElaboratedTypeSpecifier(org.eclipse.cdt.core.parser.ast.ClassKind, java.lang.String, int, int)
     */
    public IASTElaboratedTypeSpecifier createElaboratedTypeSpecifier(ClassKind elaboratedClassKind, String typeName, int startingOffset, int endOffset)
    {
        return new ASTElaboratedTypeSpecifier( elaboratedClassKind, typeName, startingOffset, endOffset );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createEnumerationSpecifier(java.lang.String, int)
     */
    public IASTEnumerationSpecifier createEnumerationSpecifier(String name, int startingOffset, int nameOffset)
    {
        return new ASTEnumerationSpecifier( name, startingOffset, nameOffset );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#addEnumerator(org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier, java.lang.String, int, int)
     */
    public void addEnumerator(IASTEnumerationSpecifier enumeration, String string, int startingOffset, int endingOffset)
    {
     	IASTEnumerator enumerator = new ASTEnumerator( enumeration, string, startingOffset, endingOffset );
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createExpression(org.eclipse.cdt.core.parser.ast.IASTExpression.ExpressionKind, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, java.lang.String, java.lang.String, java.lang.String)
	 */
	public IASTExpression createExpression(Kind kind, IASTExpression lhs, IASTExpression rhs, IASTExpression thirdExpression, String id, String typeId, String literal, IASTNewExpressionDescriptor newDescriptor) {
		return new ASTExpression( kind, lhs, rhs, thirdExpression, id, typeId, literal, newDescriptor );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNewDescriptor()
	 */
	public IASTNewExpressionDescriptor createNewDescriptor() {
		return new ASTNewDescriptor();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createIASTInitializerClause()
	 */
	public IASTInitializerClause createIASTInitializerClause(IASTInitializerClause.Kind kind, IASTExpression assignmentExpression, List initializerClauses) {
		return new ASTInitializerClause( kind, assignmentExpression, initializerClauses );
	}

}
