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

import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.ASTSemanticException;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
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
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTPointerToFunction;
import org.eclipse.cdt.core.parser.ast.IASTPointerToMethod;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTemplate;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier.ClassNameType;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type;
import org.eclipse.cdt.internal.core.parser.ast.BaseASTFactory;
import org.eclipse.cdt.internal.core.parser.ast.IASTArrayModifier;

/**
 * @author jcamelon
 *
 */
public class QuickParseASTFactory extends BaseASTFactory implements IASTFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IASTFactory#createUsingDirective(org.eclipse.cdt.internal.core.parser.ast.IASTScope, org.eclipse.cdt.internal.core.parser.TokenDuple)
	 */
	public IASTUsingDirective createUsingDirective(IASTScope scope, ITokenDuple duple, int startingOffset, int endingOffset) throws ASTSemanticException {
		return new ASTUsingDirective( scope, duple.toString(), startingOffset, endingOffset );
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
	public IASTLinkageSpecification createLinkageSpecification(IASTScope scope, String spec, int startingOffset) {
		return new ASTLinkageSpecification( scope, spec, startingOffset );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createUsingDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope, boolean, org.eclipse.cdt.internal.core.parser.TokenDuple)
	 */
	public IASTUsingDeclaration createUsingDeclaration(IASTScope scope, boolean isTypeName, ITokenDuple name, int startingOffset, int endingOffset) {
		return new ASTUsingDeclaration( scope, isTypeName, name.toString(), startingOffset, endingOffset );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, org.eclipse.cdt.core.parser.ast.ClassKind, org.eclipse.cdt.core.parser.ast.ClassNameType, org.eclipse.cdt.core.parser.ast.AccessVisibility, org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
	 */
	public IASTClassSpecifier createClassSpecifier(IASTScope scope, String name, ASTClassKind kind, ClassNameType type, ASTAccessVisibility access, int startingOffset, int nameOffset)  throws ASTSemanticException {
		IASTClassSpecifier spec = new ASTClassSpecifier( scope, name, kind, type, access );
		spec.setStartingOffset( startingOffset );
		spec.setNameOffset( nameOffset );
		return spec;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#addBaseSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier, boolean, org.eclipse.cdt.core.parser.ast.AccessVisibility, java.lang.String)
	 */
	public void addBaseSpecifier(IASTClassSpecifier astClassSpec, boolean isVirtual, ASTAccessVisibility visibility, ITokenDuple parentClassName) {
		IASTBaseSpecifier baseSpecifier = new ASTBaseSpecifier( parentClassName.toString(), isVirtual, visibility, parentClassName.getFirstToken().getOffset() );
		((IASTQClassSpecifier)astClassSpec).addBaseClass(baseSpecifier);
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createElaboratedTypeSpecifier(org.eclipse.cdt.core.parser.ast.ClassKind, java.lang.String, int, int)
     */
    public IASTElaboratedTypeSpecifier createElaboratedTypeSpecifier(ASTClassKind elaboratedClassKind, String typeName, int startingOffset, int endOffset)
    {
        return new ASTElaboratedTypeSpecifier( elaboratedClassKind, typeName, startingOffset, endOffset );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createEnumerationSpecifier(java.lang.String, int)
     */
    public IASTEnumerationSpecifier createEnumerationSpecifier(IASTScope scope, String name, int startingOffset, int nameOffset)
    {
        return new ASTEnumerationSpecifier( scope, name, startingOffset, nameOffset );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#addEnumerator(org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier, java.lang.String, int, int)
     */
    public void addEnumerator(IASTEnumerationSpecifier enumeration, String string, int startingOffset, int endingOffset, IASTExpression initialValue)
    {
     	IASTEnumerator enumerator = new ASTEnumerator( enumeration, string, startingOffset, endingOffset, initialValue );
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
	public IASTInitializerClause createInitializerClause(IASTInitializerClause.Kind kind, IASTExpression assignmentExpression, List initializerClauses) {
		return new ASTInitializerClause( kind, assignmentExpression, initializerClauses );
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createExceptionSpecification(java.util.List)
     */
    public IASTExceptionSpecification createExceptionSpecification(List typeIds)
    {
        return new ASTExceptionSpecification( typeIds );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createArrayModifier(org.eclipse.cdt.core.parser.ast.IASTExpression)
     */
    public IASTArrayModifier createArrayModifier(IASTExpression exp)
    {
        return new ASTArrayModifier( exp );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createConstructorMemberInitializer(org.eclipse.cdt.core.parser.ITokenDuple, org.eclipse.cdt.core.parser.ast.IASTExpression)
     */
    public IASTConstructorMemberInitializer createConstructorMemberInitializer(ITokenDuple duple, IASTExpression expressionList )
    {
        return new ASTConstructorMemberInitializer( duple.toString(), expressionList );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createSimpleTypeSpecifier(org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.SimpleType, org.eclipse.cdt.core.parser.ITokenDuple)
     */
    public IASTSimpleTypeSpecifier createSimpleTypeSpecifier(Type kind, ITokenDuple typeName, boolean isShort, boolean isLong, boolean isSigned, boolean isUnsigned, boolean isTypename )
    {
        return new ASTSimpleTypeSpecifier( kind, typeName, isShort, isLong, isSigned, isUnsigned, isTypename );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createFunction(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, java.util.List, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
     */
    public IASTFunction createFunction(IASTScope scope, String name, List parameters, IASTAbstractDeclaration returnType, IASTExceptionSpecification exception, boolean isInline, boolean isFriend, boolean isStatic, int startOffset, int nameOffset, IASTTemplate ownerTemplate)
    {
        return new ASTFunction(scope, name, parameters, returnType, exception, isInline, isFriend, isStatic, startOffset, nameOffset, ownerTemplate );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createAbstractDeclaration(boolean, org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier, java.util.List, java.util.List)
     */
    public IASTAbstractDeclaration createAbstractDeclaration(boolean isConst, IASTTypeSpecifier typeSpecifier, List pointerOperators, List arrayModifiers)
    {
        return new ASTAbstractDeclaration( isConst, typeSpecifier, pointerOperators, arrayModifiers );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createMethod(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, java.util.List, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration, boolean, boolean, boolean, boolean, boolean, boolean, boolean, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility)
     */
    public IASTMethod createMethod(IASTScope scope, String name, List parameters, IASTAbstractDeclaration returnType, IASTExceptionSpecification exception, boolean isInline, boolean isFriend, boolean isStatic, int startOffset, int nameOffset, IASTTemplate ownerTemplate, boolean isConst, boolean isVolatile, boolean isConstructor, boolean isDestructor, boolean isVirtual, boolean isExplicit, boolean isPureVirtual, ASTAccessVisibility visibility)
    {
        return new ASTMethod(scope, name, parameters, returnType, exception, isInline, isFriend, isStatic, startOffset, nameOffset, ownerTemplate, isConst, isVolatile, isConstructor, isDestructor, isVirtual, isExplicit, isPureVirtual, visibility);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createVariable(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, boolean, org.eclipse.cdt.core.parser.ast.IASTInitializerClause, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, boolean, boolean, boolean, boolean)
     */
    public IASTVariable createVariable(IASTScope scope, String name, boolean isAuto, IASTInitializerClause initializerClause, IASTExpression bitfieldExpression, IASTAbstractDeclaration abstractDeclaration, boolean isMutable, boolean isExtern, boolean isRegister, boolean isStatic, int startingOffset, int nameOffset)
    {
        return new ASTVariable(scope, name, isAuto, initializerClause, bitfieldExpression, abstractDeclaration, isMutable, isExtern, isRegister, isStatic, startingOffset, nameOffset);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createField(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, boolean, org.eclipse.cdt.core.parser.ast.IASTInitializerClause, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, boolean, boolean, boolean, boolean, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility)
     */
    public IASTField createField(IASTScope scope, String name, boolean isAuto, IASTInitializerClause initializerClause, IASTExpression bitfieldExpression, IASTAbstractDeclaration abstractDeclaration, boolean isMutable, boolean isExtern, boolean isRegister, boolean isStatic, int startingOffset, int nameOffset, ASTAccessVisibility visibility)
    {
        return new ASTField(scope, name, isAuto, initializerClause, bitfieldExpression, abstractDeclaration, isMutable, isExtern, isRegister, isStatic, startingOffset, nameOffset, visibility);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createParameterDeclaration(boolean, org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier, java.util.List, java.util.List, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTInitializerClause)
     */
    public IASTParameterDeclaration createParameterDeclaration(boolean isConst, IASTTypeSpecifier typeSpecifier, List pointerOperators, List arrayModifiers, String parameterName, IASTInitializerClause initializerClause)
    {
        return new ASTParameterDeclaration( isConst, typeSpecifier, pointerOperators, arrayModifiers, parameterName, initializerClause );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateDeclaration(java.util.List)
     */
    public IASTTemplateDeclaration createTemplateDeclaration(IASTScope scope, List templateParameters, boolean exported, int startingOffset)
    {
        return new ASTTemplateDeclaration( scope, templateParameters, startingOffset, exported );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateParameter(org.eclipse.cdt.core.parser.ast.IASTTemplateParameter.ParameterKind, org.eclipse.cdt.core.parser.IToken, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration)
     */
    public IASTTemplateParameter createTemplateParameter(IASTTemplateParameter.ParamKind kind, String identifier, String defaultValue, IASTParameterDeclaration parameter, List parms)
    {
        return new ASTTemplateParameter( kind, identifier, defaultValue, parameter, parms );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateInstantiation()
     */
    public IASTTemplateInstantiation createTemplateInstantiation(IASTScope scope, int startingOffset)
    {
        return new ASTTemplateInstantiation(scope, startingOffset);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateSpecialization()
     */
    public IASTTemplateSpecialization createTemplateSpecialization(IASTScope scope, int startingOffset)
    {
        return new ASTTemplateSpecialization(scope, startingOffset );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTypedef(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration)
     */
    public IASTTypedefDeclaration createTypedef(IASTScope scope, String name, IASTAbstractDeclaration mapping, int startingOffset, int nameOffset)
    {
        return new ASTTypedefDeclaration( scope, name, mapping, startingOffset, nameOffset );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTypeSpecDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope, boolean, org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier, java.util.List, java.util.List)
     */
    public IASTAbstractTypeSpecifierDeclaration createTypeSpecDeclaration(IASTScope scope, IASTTypeSpecifier typeSpecifier, IASTTemplate template, int startingOffset, int endingOffset)
    {
        return new ASTAbstractTypeSpecifierDeclaration( scope, typeSpecifier, template, startingOffset, endingOffset );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createPointerToFunction(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, java.util.List, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.IASTTemplate)
     */
    public IASTPointerToFunction createPointerToFunction(IASTScope scope, String name, List parameters, IASTAbstractDeclaration returnType, IASTExceptionSpecification exception, boolean isInline, boolean isFriend, boolean isStatic, int startOffset, int nameOffset, IASTTemplate ownerTemplate, ASTPointerOperator pointerOperator)
    {
        return new ASTPointerToFunction( scope, name, parameters, returnType, exception, isInline, isFriend, isStatic, startOffset, nameOffset, ownerTemplate, pointerOperator); 
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createPointerToMethod(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, java.util.List, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.IASTTemplate, boolean, boolean, boolean, boolean, boolean, boolean, boolean, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility)
     */
    public IASTPointerToMethod createPointerToMethod(IASTScope scope, String name, List parameters, IASTAbstractDeclaration returnType, IASTExceptionSpecification exception, boolean isInline, boolean isFriend, boolean isStatic, int startOffset, int nameOffset, IASTTemplate ownerTemplate, boolean isConst, boolean isVolatile, boolean isConstructor, boolean isDestructor, boolean isVirtual, boolean isExplicit, boolean isPureVirtual, ASTAccessVisibility visibility, ASTPointerOperator pointerOperator)
    {
        return new ASTPointerToMethod(scope, name, parameters, returnType, exception, isInline, isFriend, isStatic, startOffset, nameOffset, ownerTemplate, isConst, isVolatile, isConstructor, isDestructor, isVirtual, isExplicit, isPureVirtual, visibility, pointerOperator);
    }
}
