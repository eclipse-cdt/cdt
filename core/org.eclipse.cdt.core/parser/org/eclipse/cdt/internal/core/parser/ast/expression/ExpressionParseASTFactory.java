/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.expression;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.ASTSemanticException;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTArrayModifier;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTConstructorMemberInitializer;
import org.eclipse.cdt.core.parser.ast.IASTDesignator;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
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
import org.eclipse.cdt.core.parser.ast.IReferenceManager;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier.ClassNameType;
import org.eclipse.cdt.core.parser.ast.IASTDesignator.DesignatorKind;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter.ParamKind;
import org.eclipse.cdt.core.parser.extension.IASTFactoryExtension;
import org.eclipse.cdt.internal.core.parser.ast.BaseASTFactory;

/**
 * @author jcamelon
 */
public class ExpressionParseASTFactory extends BaseASTFactory implements IASTFactory {

	

	/**
	 * @param factory
	 */
	public ExpressionParseASTFactory( IASTFactoryExtension extension ) {
		super( extension );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createMacro(java.lang.String,
	 *           int, int, int, int, int, int, int,
	 *           org.eclipse.cdt.core.parser.IMacroDescriptor)
	 */
	public IASTMacro createMacro(
		char[] name,
		int startingOffset,
		int startingLine,
		int nameOffset,
		int nameEndOffset,
		int nameLine,
		int endingOffset,
		int endingLine,
		char[] fn) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createInclusion(java.lang.String,
	 *           java.lang.String, boolean, int, int, int, int, int, int, int)
	 */
	public IASTInclusion createInclusion(
		char[] name,
		char[] fileName,
		boolean local,
		int startingOffset,
		int startingLine,
		int nameOffset,
		int nameEndOffset,
		int nameLine,
		int endingOffset,
		int endingLine, char[] fn) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createUsingDirective(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           org.eclipse.cdt.core.parser.ITokenDuple, int, int, int, int)
	 */
	public IASTUsingDirective createUsingDirective(
		IASTScope scope,
		ITokenDuple duple,
		int startingOffset,
		int startingLine,
		int endingOffset,
		int endingLine)
		throws ASTSemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createUsingDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           boolean, org.eclipse.cdt.core.parser.ITokenDuple, int, int, int,
	 *           int)
	 */
	public IASTUsingDeclaration createUsingDeclaration(
		IASTScope scope,
		boolean isTypeName,
		ITokenDuple name,
		int startingOffset,
		int startingLine,
		int endingOffset,
		int endingLine)
		throws ASTSemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createASMDefinition(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           java.lang.String, int, int, int, int)
	 */
	public IASTASMDefinition createASMDefinition(
		IASTScope scope,
		char[] assembly,
		int startingOffset,
		int startingLine,
		int endingOffset,
		int endingLine, char[] fn) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           java.lang.String, int, int, int, int, int)
	 */
	public IASTNamespaceDefinition createNamespaceDefinition(
		IASTScope scope,
		char[] identifier,
		int startingOffset,
		int startingLine,
		int nameOffset,
		int nameEndOffset,
		int nameLineNumber, char[] fn)
		throws ASTSemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNamespaceAlias(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           java.lang.String, org.eclipse.cdt.core.parser.ITokenDuple, int,
	 *           int, int, int, int, int, int)
	 */
	public IASTNamespaceAlias createNamespaceAlias(
		IASTScope scope,
		char[] identifier,
		ITokenDuple alias,
		int startingOffset,
		int startingLine,
		int nameOffset,
		int nameEndOffset,
		int nameLine,
		int endOffset,
		int endingLine)
		throws ASTSemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createCompilationUnit()
	 */
	public IASTCompilationUnit createCompilationUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           java.lang.String, int, int)
	 */
	public IASTLinkageSpecification createLinkageSpecification(
		IASTScope scope,
		char[] spec,
		int startingOffset,
		int startingLine, char[] fn) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           org.eclipse.cdt.core.parser.ITokenDuple,
	 *           org.eclipse.cdt.core.parser.ast.ASTClassKind,
	 *           org.eclipse.cdt.core.parser.ast.IASTClassSpecifier.ClassNameType,
	 *           org.eclipse.cdt.core.parser.ast.ASTAccessVisibility, int, int, int,
	 *           int, int)
	 */
	public IASTClassSpecifier createClassSpecifier(
		IASTScope scope,
		ITokenDuple name,
		ASTClassKind kind,
		ClassNameType type,
		ASTAccessVisibility access,
		int startingOffset,
		int startingLine,
		int nameOffset,
		int nameEndOffset,
		int nameLine, char[] fn)
		throws ASTSemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#addBaseSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier,
	 *           boolean, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility,
	 *           org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public void addBaseSpecifier(
		IASTClassSpecifier astClassSpec,
		boolean isVirtual,
		ASTAccessVisibility visibility,
		ITokenDuple parentClassName)
		throws ASTSemanticException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createElaboratedTypeSpecifier(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           org.eclipse.cdt.core.parser.ast.ASTClassKind,
	 *           org.eclipse.cdt.core.parser.ITokenDuple, int, int, int, int,
	 *           boolean, boolean)
	 */
	public IASTElaboratedTypeSpecifier createElaboratedTypeSpecifier(
		IASTScope scope,
		ASTClassKind elaboratedClassKind,
		ITokenDuple typeName,
		int startingOffset,
		int startingLine,
		int endOffset,
		int endingLine,
		boolean isForewardDecl,
		boolean isFriend)
		throws ASTSemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createEnumerationSpecifier(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           java.lang.String, int, int, int, int, int)
	 */
	public IASTEnumerationSpecifier createEnumerationSpecifier(
		IASTScope scope,
		char[] name,
		int startingOffset,
		int startingLine,
		int nameOffset,
		int nameEndOffset,
		int nameLine, char[] fn)
		throws ASTSemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#addEnumerator(org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier,
	 *           java.lang.String, int, int, int, int, int, int, int,
	 *           org.eclipse.cdt.core.parser.ast.IASTExpression)
	 */
	public IASTEnumerator addEnumerator(
		IASTEnumerationSpecifier enumeration,
		char[] string,
		int startingOffset,
		int startingLine,
		int nameOffset,
		int nameEndOffset,
		int nameLine,
		int endingOffset,
		int endLine,
		IASTExpression initialValue, char[] fn)
		throws ASTSemanticException {
		return null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createExpression(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           org.eclipse.cdt.core.parser.ast.IASTExpression.Kind,
	 *           org.eclipse.cdt.core.parser.ast.IASTExpression,
	 *           org.eclipse.cdt.core.parser.ast.IASTExpression,
	 *           org.eclipse.cdt.core.parser.ast.IASTExpression,
	 *           org.eclipse.cdt.core.parser.ast.IASTTypeId,
	 *           org.eclipse.cdt.core.parser.ITokenDuple, java.lang.String,
	 *           org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor)
	 */
	public IASTExpression createExpression(
		IASTScope scope,
		Kind kind,
		IASTExpression lhs,
		IASTExpression rhs,
		IASTExpression thirdExpression,
		IASTTypeId typeId,
		ITokenDuple idExpression,
		char[] literal,
		IASTNewExpressionDescriptor newDescriptor)
		throws ASTSemanticException {
			if( extension.overrideCreateExpressionMethod() )
				return extension.createExpression(scope, kind, lhs, rhs, thirdExpression, typeId, idExpression, literal, newDescriptor, null );
			return ExpressionFactory.createExpression( kind, lhs, rhs, thirdExpression, typeId, idExpression == null ? EMPTY_STRING : idExpression.toCharArray(), literal, newDescriptor ); //$NON-NLS-1$	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNewDescriptor(java.util.List,
	 *           java.util.List, java.util.List)
	 */
	public IASTNewExpressionDescriptor createNewDescriptor(
		List newPlacementExpressions,
		List newTypeIdExpressions,
		List newInitializerExpressions) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createInitializerClause(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           org.eclipse.cdt.core.parser.ast.IASTInitializerClause.Kind,
	 *           org.eclipse.cdt.core.parser.ast.IASTExpression, java.util.List,
	 *           java.util.List)
	 */
	public IASTInitializerClause createInitializerClause(
		IASTScope scope,
		org.eclipse.cdt.core.parser.ast.IASTInitializerClause.Kind kind,
		IASTExpression assignmentExpression,
		List initializerClauses,
		List designators) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createExceptionSpecification(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           java.util.List)
	 */
	public IASTExceptionSpecification createExceptionSpecification(
		IASTScope scope,
		List typeIds)
		throws ASTSemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createArrayModifier(org.eclipse.cdt.core.parser.ast.IASTExpression)
	 */
	public IASTArrayModifier createArrayModifier(IASTExpression exp) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createConstructorMemberInitializer(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           org.eclipse.cdt.core.parser.ITokenDuple,
	 *           org.eclipse.cdt.core.parser.ast.IASTExpression)
	 */
	public IASTConstructorMemberInitializer createConstructorMemberInitializer(
		IASTScope scope,
		ITokenDuple duple,
		IASTExpression expressionList)
		throws ASTSemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createSimpleTypeSpecifier(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type,
	 *           org.eclipse.cdt.core.parser.ITokenDuple, boolean, boolean, boolean,
	 *           boolean, boolean, boolean, boolean)
	 */
	public IASTSimpleTypeSpecifier createSimpleTypeSpecifier(
		IASTScope scope,
		Type kind,
		ITokenDuple typeName,
		boolean isShort,
		boolean isLong,
		boolean isSigned,
		boolean isUnsigned,
		boolean isTypename,
		boolean isComplex,
		boolean isImaginary,
		boolean isGlobal, Map extensionParms)
		throws ASTSemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createFunction(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           org.eclipse.cdt.core.parser.ITokenDuple, java.util.List,
	 *           org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration,
	 *           org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification,
	 *           boolean, boolean, boolean, int, int, int, int, int,
	 *           org.eclipse.cdt.core.parser.ast.IASTTemplate, boolean, boolean,
	 *           boolean, boolean, boolean, java.util.List, boolean, boolean,
	 *           boolean)
	 */
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
		int startLine,
		int nameOffset,
		int nameEndOffset,
		int nameLine,
		IASTTemplate ownerTemplate,
		boolean isConst,
		boolean isVolatile,
		boolean isVirtual,
		boolean isExplicit,
		boolean isPureVirtual,
		List constructorChain,
		boolean isDefinition,
		boolean hasFunctionTryBlock,
		boolean hasVariableArguments)
		throws ASTSemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createAbstractDeclaration(boolean,
	 *           boolean, org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier,
	 *           java.util.List, java.util.List, java.util.List,
	 *           org.eclipse.cdt.core.parser.ast.ASTPointerOperator)
	 */
	public IASTAbstractDeclaration createAbstractDeclaration(
		boolean isConst,
		boolean isVolatile,
		IASTTypeSpecifier typeSpecifier,
		List pointerOperators,
		List arrayModifiers,
		List parameters,
		ASTPointerOperator pointerOperator) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createMethod(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           org.eclipse.cdt.core.parser.ITokenDuple, java.util.List,
	 *           org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration,
	 *           org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification,
	 *           boolean, boolean, boolean, int, int, int, int, int,
	 *           org.eclipse.cdt.core.parser.ast.IASTTemplate, boolean, boolean,
	 *           boolean, boolean, boolean,
	 *           org.eclipse.cdt.core.parser.ast.ASTAccessVisibility,
	 *           java.util.List, boolean, boolean, boolean)
	 */
	public IASTMethod createMethod(
		IASTScope scope,
		ITokenDuple name,
		List parameters,
		IASTAbstractDeclaration returnType,
		IASTExceptionSpecification exception,
		boolean isInline,
		boolean isFriend,
		boolean isStatic,
		int startOffset,
		int startLine,
		int nameOffset,
		int nameEndOffset,
		int nameLine,
		IASTTemplate ownerTemplate,
		boolean isConst,
		boolean isVolatile,
		boolean isVirtual,
		boolean isExplicit,
		boolean isPureVirtual,
		ASTAccessVisibility visibility,
		List constructorChain,
		boolean isDefinition,
		boolean hasFunctionTryBlock,
		boolean hasVariableArguments)
		throws ASTSemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createVariable(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           java.lang.String, boolean,
	 *           org.eclipse.cdt.core.parser.ast.IASTInitializerClause,
	 *           org.eclipse.cdt.core.parser.ast.IASTExpression,
	 *           org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, boolean,
	 *           boolean, boolean, boolean, int, int, int, int, int,
	 *           org.eclipse.cdt.core.parser.ast.IASTExpression)
	 */
	public IASTVariable createVariable(
		IASTScope scope,
		ITokenDuple name,
		boolean isAuto,
		IASTInitializerClause initializerClause,
		IASTExpression bitfieldExpression,
		IASTAbstractDeclaration abstractDeclaration,
		boolean isMutable,
		boolean isExtern,
		boolean isRegister,
		boolean isStatic,
		int startingOffset,
		int startingLine,
		int nameOffset,
		int nameEndOffset,
		int nameLine,
		IASTExpression constructorExpression, char[] fn)
		throws ASTSemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createField(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           java.lang.String, boolean,
	 *           org.eclipse.cdt.core.parser.ast.IASTInitializerClause,
	 *           org.eclipse.cdt.core.parser.ast.IASTExpression,
	 *           org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, boolean,
	 *           boolean, boolean, boolean, int, int, int, int, int,
	 *           org.eclipse.cdt.core.parser.ast.IASTExpression,
	 *           org.eclipse.cdt.core.parser.ast.ASTAccessVisibility)
	 */
	public IASTField createField(
		IASTScope scope,
		ITokenDuple name,
		boolean isAuto,
		IASTInitializerClause initializerClause,
		IASTExpression bitfieldExpression,
		IASTAbstractDeclaration abstractDeclaration,
		boolean isMutable,
		boolean isExtern,
		boolean isRegister,
		boolean isStatic,
		int startingOffset,
		int startingLine,
		int nameOffset,
		int nameEndOffset,
		int nameLine,
		IASTExpression constructorExpression,
		ASTAccessVisibility visibility, char[] fn)
		throws ASTSemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createDesignator(org.eclipse.cdt.core.parser.ast.IASTDesignator.DesignatorKind,
	 *           org.eclipse.cdt.core.parser.ast.IASTExpression,
	 *           org.eclipse.cdt.core.parser.IToken)
	 */
	public IASTDesignator createDesignator(
		DesignatorKind kind,
		IASTExpression constantExpression,
		IToken fieldIdentifier, Map extensionParms) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createParameterDeclaration(boolean,
	 *           boolean, org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier,
	 *           java.util.List, java.util.List, java.util.List,
	 *           org.eclipse.cdt.core.parser.ast.ASTPointerOperator,
	 *           java.lang.String,
	 *           org.eclipse.cdt.core.parser.ast.IASTInitializerClause, int, int,
	 *           int, int, int, int, int)
	 */
	public IASTParameterDeclaration createParameterDeclaration(
		boolean isConst,
		boolean isVolatile,
		IASTTypeSpecifier getTypeSpecifier,
		List pointerOperators,
		List arrayModifiers,
		List parameters,
		ASTPointerOperator pointerOp,
		char[] parameterName,
		IASTInitializerClause initializerClause,
		int startingOffset,
		int startingLine,
		int nameOffset,
		int nameEndOffset,
		int nameLine,
		int endingOffset,
		int endingLine, char[] fn) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           java.util.List, boolean, int, int)
	 */
	public IASTTemplateDeclaration createTemplateDeclaration(
		IASTScope scope,
		List templateParameters,
		boolean exported,
		int startingOffset,
		int startingLine, char[] fn) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateParameter(org.eclipse.cdt.core.parser.ast.IASTTemplateParameter.ParamKind,
	 *           java.lang.String, java.lang.String,
	 *           org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration,
	 *           java.util.List)
	 */
	public IASTTemplateParameter createTemplateParameter(
		ParamKind kind,
		char[] identifier,
		IASTTypeId defaultValue,
		IASTParameterDeclaration parameter,
		List parms, IASTCodeScope parameterScope,
		int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endingLine, char[] fn) {
		// TODO Auto-generated method stub
		return null;
	}

	/*Os
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateInstantiation(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           int, int)
	 */
	public IASTTemplateInstantiation createTemplateInstantiation(
		IASTScope scope,
		int startingOffset,
		int startingLine, char[] fn) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           int, int)
	 */
	public IASTTemplateSpecialization createTemplateSpecialization(
		IASTScope scope,
		int startingOffset,
		int startingLine, char[] fn) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTypedef(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           java.lang.String,
	 *           org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, int, int,
	 *           int, int, int)
	 */
	public IASTTypedefDeclaration createTypedef(
		IASTScope scope,
		char[] name,
		IASTAbstractDeclaration mapping,
		int startingOffset,
		int startingLine,
		int nameOffset,
		int nameEndOffset,
		int nameLine, char[] fn)
		throws ASTSemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTypeSpecDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier,
	 *           org.eclipse.cdt.core.parser.ast.IASTTemplate, int, int, int, int)
	 */
	public IASTAbstractTypeSpecifierDeclaration createTypeSpecDeclaration(
		IASTScope scope,
		IASTTypeSpecifier typeSpecifier,
		IASTTemplate template,
		int startingOffset,
		int startingLine,
		int endingOffset,
		int endingLine,
		boolean isFriend, char[] fn) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#queryIsTypeName(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public boolean queryIsTypeName(
		IASTScope scope,
		ITokenDuple nameInQuestion) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNewCodeBlock(org.eclipse.cdt.core.parser.ast.IASTScope)
	 */
	public IASTCodeScope createNewCodeBlock(IASTScope scope) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTypeId(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type,
	 *           boolean, boolean, boolean, boolean, boolean, boolean, boolean,
	 *           org.eclipse.cdt.core.parser.ITokenDuple, java.util.List,
	 *           java.util.List)
	 */
	public IASTTypeId createTypeId(
		IASTScope scope,
		Type kind,
		boolean isConst,
		boolean isVolatile,
		boolean isShort,
		boolean isLong,
		boolean isSigned,
		boolean isUnsigned,
		boolean isTypename,
		ITokenDuple name,
		List pointerOps,
		List arrayMods, char[] completeSignature)
		throws ASTSemanticException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#signalEndOfClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
	 */
	public void signalEndOfClassSpecifier(IASTClassSpecifier astClassSpecifier) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#lookupSymbolInContext(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *           org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public IASTNode lookupSymbolInContext(IASTScope scope, ITokenDuple duple, IASTNode reference)
		throws ASTNotImplementedException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#getNodeForThisExpression(org.eclipse.cdt.core.parser.ast.IASTExpression)
	 */
	public IASTNode expressionToMostPreciseASTNode(IASTScope scope, IASTExpression expression) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#getDeclaratorScope(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public IASTScope getDeclaratorScope(IASTScope scope, ITokenDuple duple) {
		return scope;
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
		//ignore
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#getReferenceManager()
	 */
	public IReferenceManager getReferenceManager() {
		// TODO Auto-generated method stub
		return null;
	}
}
