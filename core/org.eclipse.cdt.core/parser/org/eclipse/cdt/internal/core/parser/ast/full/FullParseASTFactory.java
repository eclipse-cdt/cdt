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
package org.eclipse.cdt.internal.core.parser.ast.full;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.Backtrack;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTConstructorMemberInitializer;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
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
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTemplate;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedef;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier.ClassNameType;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.SimpleType;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter.ParameterKind;
import org.eclipse.cdt.internal.core.parser.ast.BaseASTFactory;
import org.eclipse.cdt.internal.core.parser.ast.IASTArrayModifier;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableException;

/**
 * @author jcamelon
 *
 */
public class FullParseASTFactory extends BaseASTFactory implements IASTFactory {

	private ParserSymbolTable pst = new ParserSymbolTable(); // symbol table
	
	public IASTUsingDirective createUsingDirective(
		IASTScope scope,
		ITokenDuple duple)
		throws Backtrack {
		Iterator iter = duple.iterator();
		IToken t1 = (IToken)iter.next();
		IContainerSymbol symbol = null; 
		
		if( t1.getType() == IToken.tCOLONCOLON )
			symbol = pst.getCompilationUnit();
		else
		{
			try
			{
				symbol = (IContainerSymbol)((IASTFScope)scope).getContainerSymbol().Lookup( t1.getImage() );
			}
			catch( ParserSymbolTableException pste )
			{
				handlePSTException( pste );
			}
		}
		
		while( iter.hasNext() )
		{
			IToken t = (IToken)iter.next(); 
			if( t.getType() == IToken.tCOLONCOLON ) continue; 
			try
			{
				symbol = symbol.LookupNestedNameSpecifier( t.getImage() );
			}
			catch( ParserSymbolTableException pste )
			{
				handlePSTException( pste );
			}
		}
		
		try {
			((IASTFScope)scope).getContainerSymbol().addUsingDirective( symbol );
		} catch (ParserSymbolTableException pste) {
			handlePSTException( pste );
		}
		
		IASTUsingDirective astUD = new ASTUsingDirective( duple.toString() );
		return astUD;
	}
	
	public IASTASMDefinition createASMDefinition(
		IASTScope scope,
		String assembly,
		int first,
		int last) {
		IContainerSymbol containerSymbol = (IContainerSymbol)((IASTFScope)scope).getSymbol();
		ISymbol asmSymbol = pst.newSymbol( "", ParserSymbolTable.TypeInfo.t_asm );
		IASTFASMDefinition asmDefinition = new ASTASMDefinition( asmSymbol, assembly );
		asmSymbol.setASTNode( asmDefinition );
		
		try {
			containerSymbol.addSymbol(asmSymbol);
		} catch (ParserSymbolTableException e1) {
			//?
		}
		
		asmDefinition.setStartingOffset( first );
		asmDefinition.setEndingOffset( last );
		return asmDefinition;
	}

	public IASTNamespaceDefinition createNamespaceDefinition(
		IASTScope scope,
		String identifier, 
		int first, int nameOffset ) {
		
		IContainerSymbol namespaceSymbol = null; 
		
		pst.newContainerSymbol( identifier, ParserSymbolTable.TypeInfo.t_namespace );
		IASTFNamespaceDefinition namespaceDefinition = new ASTNamespaceDefinition( namespaceSymbol, identifier );
		namespaceDefinition.setStartingOffset( first ); 
		if( identifier != "" )
			namespaceDefinition.setNameOffset( nameOffset );
		return namespaceDefinition;
	}

	public IASTCompilationUnit createCompilationUnit() {
		IASTFCompilationUnit compilationUnit = new ASTCompilationUnit( pst.getCompilationUnit() );  
		return compilationUnit;
	}
	
	public IASTLinkageSpecification createLinkageSpecification(IASTScope scope, String spec) {
		IContainerSymbol symbol = pst.newContainerSymbol("", ParserSymbolTable.TypeInfo.t_linkage );
		IASTFLinkageSpecification linkage = new ASTLinkageSpecification( symbol, spec);
		return linkage;
	}
	
	/**
	 * @param pste
	 */
	private void handlePSTException(ParserSymbolTableException pste) throws Backtrack {
		throw new Backtrack(); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createUsingDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope, boolean, org.eclipse.cdt.internal.core.parser.TokenDuple)
	 */
	public IASTUsingDeclaration createUsingDeclaration(IASTScope scope, boolean isTypeName, ITokenDuple name) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, org.eclipse.cdt.core.parser.ast.ClassKind, org.eclipse.cdt.core.parser.ast.ClassNameType, org.eclipse.cdt.core.parser.ast.AccessVisibility, org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
	 */
	public IASTClassSpecifier createClassSpecifier(IASTScope scope, String name, ASTClassKind kind, ClassNameType type, ASTAccessVisibility access, IASTTemplate ownerTemplateDeclaration, int startingOffset, int nameOffset) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#addBaseSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier, boolean, org.eclipse.cdt.core.parser.ast.AccessVisibility, java.lang.String)
	 */
	public void addBaseSpecifier(IASTClassSpecifier astClassSpec, boolean isVirtual, ASTAccessVisibility visibility, String string) {
		// TODO Auto-generated method stub
		
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createElaboratedTypeSpecifier(org.eclipse.cdt.core.parser.ast.ClassKind, java.lang.String, int, int)
     */
    public IASTElaboratedTypeSpecifier createElaboratedTypeSpecifier(ASTClassKind elaboratedClassKind, String typeName, int startingOffset, int endOffset )
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createEnumerationSpecifier(java.lang.String, int)
     */
    public IASTEnumerationSpecifier createEnumerationSpecifier(String name, int startingOffset, int nameOffset)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#addEnumerator(org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier, java.lang.String, int, int)
     */
    public void addEnumerator(IASTEnumerationSpecifier enumeration, String string, int startingOffset, int endingOffset, IASTExpression initialValue)
    {
        // TODO Auto-generated method stub
        
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createExpression(org.eclipse.cdt.core.parser.ast.IASTExpression.ExpressionKind, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, java.lang.String, java.lang.String, java.lang.String)
	 */
	public IASTExpression createExpression(Kind kind, IASTExpression lhs, IASTExpression rhs, IASTExpression thirdExpression, String id, String typeId, String literal, IASTNewExpressionDescriptor newDescriptor) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNewDescriptor()
	 */
	public IASTNewExpressionDescriptor createNewDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createIASTInitializerClause()
	 */
	public IASTInitializerClause createInitializerClause(IASTInitializerClause.Kind kind, IASTExpression assignmentExpression, List initializerClauses) {
		// TODO Auto-generated method stub
		return null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createExceptionSpecification(java.util.List)
     */
    public IASTExceptionSpecification createExceptionSpecification(List typeIds)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createArrayModifier(org.eclipse.cdt.core.parser.ast.IASTExpression)
     */
    public IASTArrayModifier createArrayModifier(IASTExpression exp)
    {
    	// TODO
        return null; 
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createConstructorMemberInitializer(org.eclipse.cdt.core.parser.ITokenDuple, org.eclipse.cdt.core.parser.ast.IASTExpression)
     */
    public IASTConstructorMemberInitializer createConstructorMemberInitializer(ITokenDuple duple, IASTExpression expressionList )
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createSimpleTypeSpecifier(org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.SimpleType, org.eclipse.cdt.core.parser.ITokenDuple, boolean, boolean, boolean, boolean)
     */
    public IASTSimpleTypeSpecifier createSimpleTypeSpecifier(SimpleType kind, ITokenDuple typeName, boolean isShort, boolean isLong, boolean isSigned, boolean isUnsigned, boolean isTypename)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createFunction(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, java.util.List, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration)
     */
    public IASTFunction createFunction(IASTScope scope, String name, List parameters, IASTAbstractDeclaration returnType, IASTExceptionSpecification exception, boolean isInline, boolean isFriend, boolean isStatic, int startOffset, int nameOffset, IASTTemplate ownerTemplate)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createAbstractDeclaration(boolean, org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier, java.util.List, java.util.List)
     */
    public IASTAbstractDeclaration createAbstractDeclaration(boolean isConst, IASTTypeSpecifier typeSpecifier, List pointerOperators, List arrayModifiers)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createMethod(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, java.util.List, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration, boolean, boolean, boolean, boolean, boolean, boolean, boolean, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility)
     */
    public IASTMethod createMethod(IASTScope scope, String name, List parameters, IASTAbstractDeclaration returnType, IASTExceptionSpecification exception, boolean isInline, boolean isFriend, boolean isStatic, int startOffset, int nameOffset, IASTTemplate ownerTemplate, boolean isConst, boolean isVolatile, boolean isConstructor, boolean isDestructor, boolean isVirtual, boolean isExplicit, boolean isPureVirtual, ASTAccessVisibility visibility)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createVariable(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, boolean, org.eclipse.cdt.core.parser.ast.IASTInitializerClause, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, boolean, boolean, boolean, boolean)
     */
    public IASTVariable createVariable(IASTScope scope, String name, boolean isAuto, IASTInitializerClause initializerClause, IASTExpression bitfieldExpression, IASTAbstractDeclaration abstractDeclaration, boolean isMutable, boolean isExtern, boolean isRegister, boolean isStatic, int startingOffset, int nameOffset)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createField(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, boolean, org.eclipse.cdt.core.parser.ast.IASTInitializerClause, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, boolean, boolean, boolean, boolean, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility)
     */
    public IASTField createField(IASTScope scope, String name, boolean isAuto, IASTInitializerClause initializerClause, IASTExpression bitfieldExpression, IASTAbstractDeclaration abstractDeclaration, boolean isMutable, boolean isExtern, boolean isRegister, boolean isStatic, int startingOffset, int nameOffset, ASTAccessVisibility visibility)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createParameterDeclaration(boolean, org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier, java.util.List, java.util.List, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTInitializerClause)
     */
    public IASTParameterDeclaration createParameterDeclaration(boolean isConst, IASTTypeSpecifier getTypeSpecifier, List pointerOperators, List arrayModifiers, String parameterName, IASTInitializerClause initializerClause)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateDeclaration(java.util.List)
     */
    public IASTTemplateDeclaration createTemplateDeclaration(IASTScope scope, List templateParameters, boolean exported, int startingOffset)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateParameter(org.eclipse.cdt.core.parser.ast.IASTTemplateParameter.ParameterKind, org.eclipse.cdt.core.parser.IToken, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration)
     */
    public IASTTemplateParameter createTemplateParameter(ParameterKind kind, String identifier, String defaultValue, IASTParameterDeclaration parameter, List parms)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateInstantiation()
     */
    public IASTTemplateInstantiation createTemplateInstantiation(IASTScope scope, int startingOffset)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateSpecialization()
     */
    public IASTTemplateSpecialization createTemplateSpecialization(IASTScope scope, int startingOffset)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTypedef(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration)
     */
    public IASTTypedef createTypedef(IASTScope scope, String name, IASTAbstractDeclaration mapping, int startingOffset, int nameOffset)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
