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
package org.eclipse.cdt.core.parser.ast;

import java.util.List;

import org.eclipse.cdt.core.parser.Backtrack;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier.ClassNameType;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.internal.core.parser.ast.IASTArrayModifier;

/**
 * @author jcamelon
 *
 */
public interface IASTFactory {
	
	public IASTMacro createMacro( String name, int startingOffset, int endingOffset, int nameOffset );
	public IASTInclusion createInclusion( String name, String fileName, boolean local, int startingOffset, int endingOffset, int nameOffset );

	public IASTUsingDirective createUsingDirective(
		IASTScope scope,
		ITokenDuple duple)
		throws Backtrack;
		
	public IASTUsingDeclaration createUsingDeclaration( 
		IASTScope scope, 
		boolean isTypeName, 
		ITokenDuple name );
		
		
	public IASTASMDefinition createASMDefinition(
		IASTScope scope,
		String assembly,
		int first,
		int last);
	
	public IASTNamespaceDefinition createNamespaceDefinition(
		IASTScope scope,
		String identifier,
		int startingOffset, int nameOffset);
	
	public IASTCompilationUnit createCompilationUnit();
	
	public IASTLinkageSpecification createLinkageSpecification(IASTScope scope, String spec);
	
	public IASTClassSpecifier createClassSpecifier( IASTScope scope,
		String name,  
		ASTClassKind kind, 
		ClassNameType type, 
		ASTAccessVisibility access, 
		IASTTemplateDeclaration ownerTemplateDeclaration, int startingOffset, int nameOffset );

	/**
	 * @param astClassSpec
	 * @param isVirtual
	 * @param visibility
	 * @param string
	 */
	public void addBaseSpecifier(IASTClassSpecifier astClassSpec, boolean isVirtual, ASTAccessVisibility visibility, String string);

    public IASTElaboratedTypeSpecifier createElaboratedTypeSpecifier(ASTClassKind elaboratedClassKind, String typeName, int startingOffset, int endOffset );
    public IASTEnumerationSpecifier createEnumerationSpecifier(String name, int startingOffset, int nameOffset );
    public void addEnumerator(IASTEnumerationSpecifier enumeration, String string, int startingOffset, int endingOffset);
    
    public IASTExpression createExpression( IASTExpression.Kind kind, IASTExpression lhs, IASTExpression rhs, IASTExpression thirdExpression, String id, String typeId, String literal, IASTNewExpressionDescriptor newDescriptor );
    
    public IASTExpression.IASTNewExpressionDescriptor createNewDescriptor();
    
    public IASTInitializerClause createInitializerClause(IASTInitializerClause.Kind kind, IASTExpression assignmentExpression, List initializerClauses);
    
    public IASTExceptionSpecification createExceptionSpecification( List typeIds );
    /**
     * @param exp
     */
    public IASTArrayModifier createArrayModifier(IASTExpression exp);
    /**
     * @param duple
     * @param expressionList
     * @return
     */
    public IASTConstructorMemberInitializer createConstructorMemberInitializer(ITokenDuple duple, IASTExpression expressionList );
    
    public IASTSimpleTypeSpecifier createSimpleTypeSpecifier( IASTSimpleTypeSpecifier.SimpleType kind, ITokenDuple typeName, boolean isShort, boolean isLong, boolean isSigned, boolean isUnsigned, boolean isTypename );
    

	
}