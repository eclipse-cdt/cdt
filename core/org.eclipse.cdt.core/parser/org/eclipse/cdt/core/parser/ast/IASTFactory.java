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

import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier.ClassNameType;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
/**
 * @author jcamelon
 *
 */
public interface IASTFactory
{
    public IASTMacro createMacro(
        String name,
        int startingOffset,
        int nameOffset,
        int nameEndOffset, int endingOffset) throws Exception;
    public IASTInclusion createInclusion(
        String name,
        String fileName,
        boolean local,
        int startingOffset,
        int nameOffset,
        int nameEndOffset, int endingOffset) throws Exception;
    public IASTUsingDirective createUsingDirective(
        IASTScope scope,
        ITokenDuple duple, int startingOffset, int endingOffset)
        throws ASTSemanticException, Exception;
    public IASTUsingDeclaration createUsingDeclaration(
        IASTScope scope,
        boolean isTypeName,
        ITokenDuple name, int startingOffset, int endingOffset) throws ASTSemanticException, Exception;
    public IASTASMDefinition createASMDefinition(
        IASTScope scope,
        String assembly,
        int first,
        int last)throws Exception;
    public IASTNamespaceDefinition createNamespaceDefinition(
        IASTScope scope,
        String identifier,
        int startingOffset,
        int nameOffset, int nameEndOffset) throws ASTSemanticException, Exception;
        
    public IASTNamespaceAlias    createNamespaceAlias( 
    	IASTScope scope, 
    	String identifier, 
    	ITokenDuple alias, 
    	int startingOffset, 
    	int nameOffset, 
    	int nameEndOffset, int endOffset ) throws ASTSemanticException, Exception;
        
    public IASTCompilationUnit createCompilationUnit() throws Exception;
    public IASTLinkageSpecification createLinkageSpecification(
        IASTScope scope,
        String spec, int startingOffset) throws Exception;
    public IASTClassSpecifier createClassSpecifier(
        IASTScope scope,
        ITokenDuple name,
        ASTClassKind kind,
        ClassNameType type,
        ASTAccessVisibility access,
        int startingOffset,
        int nameOffset, int nameEndOffset) throws ASTSemanticException, Exception;
    /**
     * @param astClassSpec
     * @param isVirtual
     * @param visibility
     * @param string
     */
    public void addBaseSpecifier(
        IASTClassSpecifier astClassSpec,
        boolean isVirtual,
        ASTAccessVisibility visibility,
        ITokenDuple parentClassName) throws ASTSemanticException, Exception;
    public IASTElaboratedTypeSpecifier createElaboratedTypeSpecifier(
        IASTScope scope,
        ASTClassKind elaboratedClassKind,
        ITokenDuple typeName,
        int startingOffset, int endOffset, boolean isForewardDecl) throws ASTSemanticException, Exception;
    public IASTEnumerationSpecifier createEnumerationSpecifier(
        IASTScope scope,
        String name,
        int startingOffset, int nameOffset, int nameEndOffset) throws ASTSemanticException, Exception;
    public void addEnumerator(
        IASTEnumerationSpecifier enumeration,
        String string,
        int startingOffset,
        int nameOffset, int nameEndOffset, int endingOffset, IASTExpression initialValue)throws ASTSemanticException, Exception;
    public IASTExpression createExpression(
        IASTScope scope,
        IASTExpression.Kind kind,
        IASTExpression lhs,
        IASTExpression rhs,
        IASTExpression thirdExpression,
        IASTTypeId typeId,
        ITokenDuple idExpression, String literal, IASTNewExpressionDescriptor newDescriptor) throws ASTSemanticException, Exception;
    public IASTExpression.IASTNewExpressionDescriptor createNewDescriptor(List newPlacementExpressions,List newTypeIdExpressions,List newInitializerExpressions)throws Exception;
    public IASTInitializerClause createInitializerClause(
        IASTInitializerClause.Kind kind,
        IASTExpression assignmentExpression,
        List initializerClauses) throws Exception;
    public IASTExceptionSpecification createExceptionSpecification(IASTScope scope, List typeIds) throws ASTSemanticException, Exception;
    /**
     * @param exp
     */
    public IASTArrayModifier createArrayModifier(IASTExpression exp) throws Exception;
    /**
     * @param duple
     * @param expressionList
     * @return
     */
    public IASTConstructorMemberInitializer createConstructorMemberInitializer(
        IASTScope scope,
        ITokenDuple duple, IASTExpression expressionList) throws ASTSemanticException, Exception;
    public IASTSimpleTypeSpecifier createSimpleTypeSpecifier(
        IASTScope scope,
        IASTSimpleTypeSpecifier.Type kind,
        ITokenDuple typeName,
        boolean isShort,
        boolean isLong,
        boolean isSigned,
        boolean isUnsigned, boolean isTypename) throws ASTSemanticException, Exception;
    public IASTFunction createFunction(
        IASTScope scope,
        ITokenDuple name,
        List parameters,
        IASTAbstractDeclaration returnType,
        IASTExceptionSpecification exception,
        boolean isInline,
        boolean isFriend,
        boolean isStatic,
        int startOffset,
        int nameOffset,
        int nameEndOffset,
		IASTTemplate ownerTemplate,
		boolean isConst,
		boolean isVolatile,
		boolean isVirtual,
		boolean isExplicit,
		boolean isPureVirtual, List constructorChain, boolean isDefinition ) throws ASTSemanticException, Exception;
    public IASTAbstractDeclaration createAbstractDeclaration(
        boolean isConst,
        boolean isVolatile,
        IASTTypeSpecifier typeSpecifier,
        List pointerOperators, List arrayModifiers, List parameters, ASTPointerOperator pointerOperator)throws Exception;
    public IASTMethod createMethod(
        IASTScope scope,
        String name,
        List parameters,
        IASTAbstractDeclaration returnType,
        IASTExceptionSpecification exception,
        boolean isInline,
        boolean isFriend,
        boolean isStatic,
        int startOffset,
        int nameOffset,
        int nameEndOffset,
        IASTTemplate ownerTemplate,
        boolean isConst,
        boolean isVolatile,
        boolean isVirtual,
        boolean isExplicit,
        boolean isPureVirtual, ASTAccessVisibility visibility, List constructorChain, boolean isDefinition) throws ASTSemanticException, Exception;
        
	public IASTVariable createVariable(IASTScope scope, String name, boolean isAuto, IASTInitializerClause initializerClause, IASTExpression bitfieldExpression, 
		   IASTAbstractDeclaration abstractDeclaration, boolean isMutable, boolean isExtern, boolean isRegister, boolean isStatic, int startingOffset, int nameOffset, int nameEndOffset, IASTExpression constructorExpression ) throws ASTSemanticException, Exception;
		   
	public IASTField createField( IASTScope scope, String name, boolean isAuto, IASTInitializerClause initializerClause, IASTExpression bitfieldExpression, IASTAbstractDeclaration abstractDeclaration, boolean isMutable, boolean isExtern, boolean isRegister, boolean isStatic, int startingOffset, int nameOffset, int nameEndOffset, IASTExpression constructorExpression, ASTAccessVisibility visibility) throws ASTSemanticException, Exception;
	
	public IASTParameterDeclaration createParameterDeclaration( boolean isConst, boolean isVolatile, IASTTypeSpecifier getTypeSpecifier, List pointerOperators, List arrayModifiers, List parameters, ASTPointerOperator pointerOp, String parameterName, IASTInitializerClause initializerClause, int startingOffset, int nameOffset, int nameEndOffset, int endingOffset ) throws Exception;
	
	public IASTTemplateDeclaration createTemplateDeclaration( IASTScope scope, List templateParameters, boolean exported, int startingOffset ) throws Exception; 

	public IASTTemplateParameter createTemplateParameter( IASTTemplateParameter.ParamKind kind, String identifier, String defaultValue, IASTParameterDeclaration parameter, List parms ) throws Exception; 

	public IASTTemplateInstantiation createTemplateInstantiation(IASTScope scope, int startingOffset)throws Exception; 
	
	public IASTTemplateSpecialization createTemplateSpecialization(IASTScope scope, int startingOffset)throws Exception; 
	
	public IASTTypedefDeclaration createTypedef( IASTScope scope, String name, IASTAbstractDeclaration mapping, int startingOffset, int nameOffset, int nameEndOffset ) throws ASTSemanticException, Exception;

	public IASTAbstractTypeSpecifierDeclaration createTypeSpecDeclaration( IASTScope scope, IASTTypeSpecifier typeSpecifier, IASTTemplate template, int startingOffset, int endingOffset)throws Exception;
	
	public boolean queryIsTypeName( IASTScope scope, ITokenDuple nameInQuestion ) throws Exception;

	static final String DOUBLE_COLON = "::";
	static final String TELTA = "~";
	/**
	 * @param scope
	 * @return
	 */
	public IASTCodeScope createNewCodeBlock(IASTScope scope)throws Exception;
	
	public IASTTypeId    createTypeId( IASTScope scope, IASTSimpleTypeSpecifier.Type kind, boolean isConst, boolean isVolatile, boolean isShort, 
			boolean isLong, boolean isSigned, boolean isUnsigned, boolean isTypename, ITokenDuple name, List pointerOps, List arrayMods ) throws ASTSemanticException, Exception;
    /**
     * @param astClassSpecifier
     */
    public void signalEndOfClassSpecifier(IASTClassSpecifier astClassSpecifier) throws Exception; 
						

}