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
import java.util.Map;

import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTConstructorMemberInitializer;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceAlias;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTemplate;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier.ClassNameType;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type;
import org.eclipse.cdt.core.parser.extension.IASTFactoryExtension;
import org.eclipse.cdt.internal.core.parser.ast.BaseASTFactory;

/**

 * @author jcamelon
 *
 */
public class QuickParseASTFactory extends BaseASTFactory implements IASTFactory {

	private boolean temporarilyDisableNodeConstruction = true;

	public QuickParseASTFactory( IASTFactoryExtension extension )
	{
		super(extension);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createUsingDirective(org.eclipse.cdt.internal.core.parser.ast.IASTScope, org.eclipse.cdt.internal.core.parser.TokenDuple)
	 */
	public IASTUsingDirective createUsingDirective(IASTScope scope, ITokenDuple duple, int startingOffset, int startingLine, int endingOffset, int endingLine) {
		return new ASTUsingDirective( scope, duple.toString(), startingOffset, startingLine, endingOffset, endingLine, duple.getFilename(), duple.getStartOffset(), duple.getEndOffset(), duple.getLineNumber() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createASMDefinition(org.eclipse.cdt.internal.core.parser.ast.IASTScope, java.lang.String, int, int)
	 */
	public IASTASMDefinition createASMDefinition(IASTScope scope, char[] assembly, int startingOffset, int startingLine, int endingOffset, int endingLine, char[] fn) {		
		IASTASMDefinition definition = new ASTASMDefinition( scope, assembly, fn ); 
		definition.setStartingOffsetAndLineNumber( startingOffset, startingLine ); 
		definition.setEndingOffsetAndLineNumber( endingOffset, endingLine );
		return definition; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createNamespaceDefinition(int, java.lang.String, int)
	 */
	public IASTNamespaceDefinition createNamespaceDefinition(IASTScope scope, char[] identifier, int first, int startingLine, int nameOffset, int nameEndOffset, int nameLineNumber, char[] fn) {
		IASTNamespaceDefinition definition = new ASTNamespaceDefinition( scope, identifier, first, startingLine, nameOffset, nameEndOffset, nameLineNumber, fn );
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
	public IASTLinkageSpecification createLinkageSpecification(IASTScope scope, char[] spec, int startingOffset, int startingLine, char[] fn) {
		return new ASTLinkageSpecification( scope, spec, startingOffset, startingLine, fn );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createUsingDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope, boolean, org.eclipse.cdt.internal.core.parser.TokenDuple)
	 */
	public IASTUsingDeclaration createUsingDeclaration(IASTScope scope, boolean isTypeName, ITokenDuple name, int startingOffset, int startingLine, int endingOffset, int endingLine) {
		return new ASTUsingDeclaration( scope, isTypeName, name.toCharArray(), startingOffset, startingLine, endingOffset, endingLine, name.getFilename(), name.getStartOffset(), name.getEndOffset(), name.getLineNumber() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, org.eclipse.cdt.core.parser.ast.ClassKind, org.eclipse.cdt.core.parser.ast.ClassNameType, org.eclipse.cdt.core.parser.ast.AccessVisibility, org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
	 */
	public IASTClassSpecifier createClassSpecifier(IASTScope scope, ITokenDuple name, ASTClassKind kind, ClassNameType type, ASTAccessVisibility access, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, char[] fn ) {
		return new ASTClassSpecifier( scope, name == null ? EMPTY_STRING : name.toCharArray() , kind, type, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, access, fn );  //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#addBaseSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier, boolean, org.eclipse.cdt.core.parser.ast.AccessVisibility, java.lang.String)
	 */
	public void addBaseSpecifier(IASTClassSpecifier astClassSpec, boolean isVirtual, ASTAccessVisibility visibility, ITokenDuple parentClassName) {
		IASTBaseSpecifier baseSpecifier = new ASTBaseSpecifier( parentClassName.toString(), isVirtual, visibility, parentClassName.getFirstToken().getOffset() );
		((IASTQClassSpecifier)astClassSpec).addBaseClass(baseSpecifier);
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createEnumerationSpecifier(java.lang.String, int)
     */
    public IASTEnumerationSpecifier createEnumerationSpecifier(IASTScope scope, char[] name, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, char[] fn)
    {
        return new ASTEnumerationSpecifier( scope, name, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, fn );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#addEnumerator(org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier, java.lang.String, int, int)
     */
    public IASTEnumerator addEnumerator(IASTEnumerationSpecifier enumeration, char[] string, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endLine, IASTExpression initialValue, char[] fn)
    {
     	IASTEnumerator enumerator = new ASTEnumerator( enumeration, string, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, endingOffset, endLine, initialValue, fn );
     	((ASTEnumerationSpecifier)enumeration).addEnumerator( enumerator );
     	return enumerator;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createExpression(org.eclipse.cdt.core.parser.ast.IASTExpression.ExpressionKind, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, java.lang.String, java.lang.String, java.lang.String)
	 */
	public IASTExpression createExpression(IASTScope scope, Kind kind, IASTExpression lhs, IASTExpression rhs, IASTExpression thirdExpression, IASTTypeId typeId, ITokenDuple idExpression, char[] literal, IASTNewExpressionDescriptor newDescriptor) {
		return temporarilyDisableNodeConstruction ? ExpressionFactory.createExpression( kind, lhs, rhs, thirdExpression, typeId, idExpression == null ? EMPTY_STRING : idExpression.toCharArray(), literal, newDescriptor ) : null;  //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNewDescriptor()
	 */
	public IASTNewExpressionDescriptor createNewDescriptor(List newPlacementEpressions, List newTypeIdExpressions, List newInitializerExpressions) {
		return new ASTNewDescriptor();
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createExceptionSpecification(java.util.List)
     */
    public IASTExceptionSpecification createExceptionSpecification(IASTScope scope, List typeIds)
    {
        return new ASTExceptionSpecification( typeIds );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createConstructorMemberInitializer(org.eclipse.cdt.core.parser.ITokenDuple, org.eclipse.cdt.core.parser.ast.IASTExpression)
     */
    public IASTConstructorMemberInitializer createConstructorMemberInitializer(IASTScope scope, ITokenDuple duple, IASTExpression expressionList )
    {
//        return new ASTConstructorMemberInitializer( duple.toString(), expressionList );
    	return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createSimpleTypeSpecifier(org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.SimpleType, org.eclipse.cdt.core.parser.ITokenDuple)
     */
    public IASTSimpleTypeSpecifier createSimpleTypeSpecifier(IASTScope scope, Type kind, ITokenDuple typeName, boolean isShort, boolean isLong, boolean isSigned, boolean isUnsigned, boolean isTypename, boolean isComplex, boolean isImaginary, boolean isGlobal, Map extensionParms)
    {
        return new ASTSimpleTypeSpecifier( kind, typeName, isShort, isLong, isSigned, isUnsigned, isTypename, isComplex, isImaginary);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createFunction(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, java.util.List, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
     */
    public IASTFunction createFunction(IASTScope scope, ITokenDuple name, List parameters, IASTAbstractDeclaration returnType, IASTExceptionSpecification exception, boolean isInline, boolean isFriend, boolean isStatic, int startOffset, int startLine, int nameOffset, int nameEndOffset, int nameLine, IASTTemplate ownerTemplate, boolean isConst, boolean isVolatile, boolean isVirtual, boolean isExplicit, boolean isPureVirtual, List constructorChain, boolean isFunctionDefinition, boolean hasFunctionTryBlock, boolean hasVariableArguments )
    {
    	ASTFunction function = new ASTFunction(scope, name.toCharArray(), parameters, returnType, exception, isInline, isFriend, isStatic, startOffset, startLine, nameOffset, nameEndOffset, ownerTemplate, hasFunctionTryBlock, hasVariableArguments, nameLine, name.getFilename() ); 
    	if( isFriend && scope instanceof IASTQClassSpecifier ){
    		((IASTQClassSpecifier)scope).addFriendDeclaration( function );
    	}
        return function;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createMethod(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, java.util.List, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration, boolean, boolean, boolean, boolean, boolean, boolean, boolean, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility)
     */
	public IASTMethod createMethod(IASTScope scope, ITokenDuple name, List parameters, IASTAbstractDeclaration returnType, IASTExceptionSpecification exception, boolean isInline, boolean isFriend, boolean isStatic, int startOffset, int startLine, int nameOffset, int nameEndOffset, int nameLine, IASTTemplate ownerTemplate, boolean isConst, boolean isVolatile, boolean isVirtual, boolean isExplicit, boolean isPureVirtual, ASTAccessVisibility visibility, List constructorChain, boolean isFunctionDefinition, boolean hasFunctionTryBlock, boolean hasVariableArguments )
	{
		ASTMethod method = new ASTMethod(scope, name.toCharArray(), parameters, returnType, exception, isInline, isFriend, isStatic, startOffset, startLine, nameOffset, nameEndOffset, nameLine, ownerTemplate, isConst, isVolatile, false, false, isVirtual, isExplicit, isPureVirtual, visibility, constructorChain, hasFunctionTryBlock, hasVariableArguments, name.getFilename());
    	if( isFriend && scope instanceof IASTQClassSpecifier ){
    		((IASTQClassSpecifier)scope).addFriendDeclaration( method );
    	}
        return method;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createVariable(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, boolean, org.eclipse.cdt.core.parser.ast.IASTInitializerClause, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, boolean, boolean, boolean, boolean)
     */
    public IASTVariable createVariable(IASTScope scope, ITokenDuple name, boolean isAuto, IASTInitializerClause initializerClause, IASTExpression bitfieldExpression, IASTAbstractDeclaration abstractDeclaration, boolean isMutable, boolean isExtern, boolean isRegister, boolean isStatic, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, IASTExpression constructorExpression, char[] fn)
    {
        return new ASTVariable(scope, ( name != null ? name.toCharArray() : EMPTY_STRING ), isAuto, initializerClause, bitfieldExpression, abstractDeclaration, isMutable, isExtern, isRegister, isStatic, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, constructorExpression, fn); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createField(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, boolean, org.eclipse.cdt.core.parser.ast.IASTInitializerClause, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, boolean, boolean, boolean, boolean, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility)
     */
    public IASTField createField(IASTScope scope, ITokenDuple name, boolean isAuto, IASTInitializerClause initializerClause, IASTExpression bitfieldExpression, IASTAbstractDeclaration abstractDeclaration, boolean isMutable, boolean isExtern, boolean isRegister, boolean isStatic, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, IASTExpression constructorExpression, ASTAccessVisibility visibility, char[] fn)
    {
        return new ASTField(scope, ( name != null ? name.toCharArray() : EMPTY_STRING ), isAuto, initializerClause, bitfieldExpression, abstractDeclaration, isMutable, isExtern, isRegister, isStatic, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, constructorExpression, visibility, fn);  //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateDeclaration(java.util.List)
     */
    public IASTTemplateDeclaration createTemplateDeclaration(IASTScope scope, List templateParameters, boolean exported, int startingOffset, int startingLine, char[] fn)
    {
        return new ASTTemplateDeclaration( scope, templateParameters, startingOffset, startingLine, exported, fn );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateParameter(org.eclipse.cdt.core.parser.ast.IASTTemplateParameter.ParameterKind, org.eclipse.cdt.core.parser.IToken, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration)
     */
    public IASTTemplateParameter createTemplateParameter(IASTTemplateParameter.ParamKind kind, char[] identifier, IASTTypeId defaultValue, IASTParameterDeclaration parameter, List parms, IASTCodeScope parameterScope, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endingLine, char[] fn)
    {
        return new ASTTemplateParameter( kind, identifier, defaultValue != null ? defaultValue.getTypeOrClassNameCharArray() : EMPTY_STRING, parameter, parms, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, endingOffset, endingLine, fn ); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateInstantiation()
     */
    public IASTTemplateInstantiation createTemplateInstantiation(IASTScope scope, int startingOffset, int startingLine, char[] fn)
    {
        return new ASTTemplateInstantiation(scope, startingOffset, startingLine, fn);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateSpecialization()
     */
    public IASTTemplateSpecialization createTemplateSpecialization(IASTScope scope, int startingOffset, int startingLine, char[] fn)
    {
        return new ASTTemplateSpecialization(scope, startingOffset, startingLine, fn );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTypedef(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration)
     */
    public IASTTypedefDeclaration createTypedef(IASTScope scope, char[] name, IASTAbstractDeclaration mapping, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, char[] fn)
    {
        return new ASTTypedefDeclaration( scope, name, mapping, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, fn);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTypeSpecDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope, boolean, org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier, java.util.List, java.util.List)
     */
    public IASTAbstractTypeSpecifierDeclaration createTypeSpecDeclaration(IASTScope scope, IASTTypeSpecifier typeSpecifier, IASTTemplate template, int startingOffset, int startingLine, int endingOffset, int endingLine, boolean isFriend, char[] fn)
    {
    	ASTAbstractTypeSpecifierDeclaration abs = new ASTAbstractTypeSpecifierDeclaration( scope, typeSpecifier, template, startingOffset, endingOffset, startingLine, endingLine, isFriend, fn );
    	if( isFriend && scope instanceof IASTQClassSpecifier ){
    		((IASTQClassSpecifier)scope).addFriendDeclaration( abs );
    	}
        return abs; 
    }

    public IASTElaboratedTypeSpecifier createElaboratedTypeSpecifier(IASTScope scope, ASTClassKind elaboratedClassKind, ITokenDuple typeName, int startingOffset, int startingLine, int endOffset, int endingLine, boolean isForewardDecl, boolean isFriend)
    {
        return new ASTElaboratedTypeSpecifier( scope, elaboratedClassKind, typeName.toCharArray(), startingOffset, startingLine, typeName.getFirstToken().getOffset(), typeName.getLastToken().getEndOffset(), typeName.getLastToken().getLineNumber(), endOffset, endingLine, typeName.getFilename() );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNamespaceAlias(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, org.eclipse.cdt.core.parser.ITokenDuple, int, int, int)
     */
    public IASTNamespaceAlias createNamespaceAlias(IASTScope scope, char[] identifier, ITokenDuple alias, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endOffset, int endingLine)
    {
    	return new ASTNamespaceAlias( scope, identifier, alias.toCharArray(), startingOffset, nameOffset, nameEndOffset, endOffset, startingLine, nameLine, endingLine, alias.getFilename());
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNewCodeBlock(org.eclipse.cdt.core.parser.ast.IASTScope)
	 */
	public IASTCodeScope createNewCodeBlock(IASTScope scope) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#queryIsTypeName(org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public boolean queryIsTypeName(IASTScope scope, ITokenDuple nameInQuestion) {
		return true;  // we have no information to say that it is not
	}

    public IASTParameterDeclaration createParameterDeclaration(boolean isConst, boolean isVolatile, IASTTypeSpecifier typeSpecifier, List pointerOperators, List arrayModifiers, List parameters, ASTPointerOperator pointerOp, char[] parameterName, IASTInitializerClause initializerClause, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endingLine, char[] fn)
    {
        return new ASTParameterDeclaration( isConst, isVolatile, typeSpecifier, pointerOperators, arrayModifiers, parameters, pointerOp, parameterName, initializerClause, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, endingOffset, endingLine, fn );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTypeId(org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type, org.eclipse.cdt.core.parser.ITokenDuple, java.util.List, java.util.List)
     */
    public IASTTypeId createTypeId(IASTScope scope, Type kind, boolean isConst, boolean isVolatile, boolean isShort, 
	boolean isLong, boolean isSigned, boolean isUnsigned, boolean isTypename, ITokenDuple name, List pointerOps, List arrayMods, char[] completeSignature)
    {
        return ( temporarilyDisableNodeConstruction ?  new ASTTypeId( kind, name == null ? EMPTY_STRING : name.toCharArray(), pointerOps, arrayMods, isConst,  //$NON-NLS-1$
        	isVolatile, isUnsigned, isSigned, isShort, isLong, isTypename, completeSignature ) : null );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#signalEndOfClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
     */
    public void signalEndOfClassSpecifier(IASTClassSpecifier astClassSpecifier)
    {
    }

    public IASTInitializerClause createInitializerClause(IASTScope scope, IASTInitializerClause.Kind kind, IASTExpression assignmentExpression, List initializerClauses, List designators)
    {
    	if( kind == IASTInitializerClause.Kind.ASSIGNMENT_EXPRESSION )
    		return new ASTExpressionInitializerClause( kind, assignmentExpression );
    	else if( kind == IASTInitializerClause.Kind.INITIALIZER_LIST )
    		return new ASTInitializerListInitializerClause( kind, initializerClauses );
		else if ( kind == IASTInitializerClause.Kind.DESIGNATED_INITIALIZER_LIST )
			return new ASTDesignatedInitializerListInitializerClause( kind, initializerClauses, designators );
		else if( kind == IASTInitializerClause.Kind.DESIGNATED_ASSIGNMENT_EXPRESSION )
			return new ASTDesignatedExpressionInitializerClause( kind, assignmentExpression, designators );
  		return new ASTInitializerClause( kind );
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#lookupSymbolInContext(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public IASTNode lookupSymbolInContext(IASTScope scope, ITokenDuple duple, IASTNode reference) throws ASTNotImplementedException {
		throw new ASTNotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#getDeclaratorScope(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public IASTScope getDeclaratorScope(IASTScope scope, ITokenDuple duple) {
		return scope;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#getNodeForThisExpression(org.eclipse.cdt.core.parser.ast.IASTExpression)
	 */
	public IASTNode expressionToMostPreciseASTNode(IASTScope scope, IASTExpression expression) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#validateIndirectMemberOperation(org.eclipse.cdt.core.parser.ast.IASTNode)
	 */
	public boolean validateIndirectMemberOperation(IASTNode node) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#validateDirectMemberOperation(org.eclipse.cdt.core.parser.ast.IASTNode)
	 */
	public boolean validateDirectMemberOperation(IASTNode node) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#constructExpressions(boolean)
	 */
	public void constructExpressions(boolean flag) {
		temporarilyDisableNodeConstruction = flag;
	}
}
