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
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.ASTSemanticException;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
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
import org.eclipse.cdt.core.parser.ast.IASTPointerToFunction;
import org.eclipse.cdt.core.parser.ast.IASTPointerToMethod;
import org.eclipse.cdt.core.parser.ast.IASTReference;
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
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter.ParamKind;
import org.eclipse.cdt.internal.core.parser.ast.BaseASTFactory;
import org.eclipse.cdt.internal.core.parser.ast.IASTArrayModifier;
import org.eclipse.cdt.internal.core.parser.pst.ForewardDeclaredSymbolExtension;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension;
import org.eclipse.cdt.internal.core.parser.pst.NamespaceSymbolExtension;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableException;
import org.eclipse.cdt.internal.core.parser.pst.StandardSymbolExtension;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension.ExtensionException;

/**
 * @author jcamelon
 *
 */
public class CompleteParseASTFactory extends BaseASTFactory implements IASTFactory
{
    /**
     * 
     */
    public CompleteParseASTFactory()
    {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createUsingDirective(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ITokenDuple, int, int)
     */
    public IASTUsingDirective createUsingDirective(
        IASTScope scope,
        ITokenDuple duple,
        int startingOffset,
        int endingOffset)
        throws ASTSemanticException
    {
		Iterator iter = duple.iterator();
		if( ! iter.hasNext() )
			throw new ASTSemanticException(); 
			
		IToken t1 = (IToken)iter.next();
		IContainerSymbol symbol = null; 
		List references = new ArrayList(); 
		
        symbol = getScopeToSearchUpon(scope, t1, references );
		
		while( iter.hasNext() )
		{
			IToken t = (IToken)iter.next(); 
			if( t.getType() == IToken.tCOLONCOLON ) continue; 
			try
			{
				symbol = symbol.LookupNestedNameSpecifier( t.getImage() );
				references.add( createReference( symbol, t.getImage(), t.getOffset() ));
			}
			catch( ParserSymbolTableException pste )
			{
				throw new ASTSemanticException();
			}
		}
		
		try {
			((ASTScope)scope).getContainerSymbol().addUsingDirective( symbol );
		} catch (ParserSymbolTableException pste) {	
		}
		
		IASTUsingDirective astUD = new ASTUsingDirective( scopeToSymbol(scope), ((IASTNamespaceDefinition)symbol.getASTExtension().getPrimaryDeclaration()), startingOffset, endingOffset, references );
		return astUD;
    }
    

    protected IContainerSymbol getScopeToSearchUpon(
        IASTScope currentScope,
        IToken firstToken, List references ) throws ASTSemanticException
    {
		IContainerSymbol symbol = null;
        if( firstToken.getType() == IToken.tCOLONCOLON )
        	symbol = pst.getCompilationUnit();
        else
        {
        	try
        	{
        		symbol = (IContainerSymbol)scopeToSymbol(currentScope).Lookup( firstToken.getImage() );
                references.add( createReference( symbol, firstToken.getImage(), firstToken.getOffset() ));
        	}
        	catch( ParserSymbolTableException pste )
        	{
        		
        	}
        }
        return symbol;
    }
    protected IContainerSymbol scopeToSymbol(IASTScope currentScope)
    {
        return ((ASTScope)currentScope).getContainerSymbol();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createUsingDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope, boolean, org.eclipse.cdt.core.parser.ITokenDuple, int, int)
     */
    public IASTUsingDeclaration createUsingDeclaration(
        IASTScope scope,
        boolean isTypeName,
        ITokenDuple name,
        int startingOffset,
        int endingOffset)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createASMDefinition(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, int, int)
     */
    public IASTASMDefinition createASMDefinition(
        IASTScope scope,
        String assembly,
        int first,
        int last)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, int, int)
     */
    public IASTNamespaceDefinition createNamespaceDefinition(
        IASTScope scope,
        String identifier,
        int startingOffset,
        int nameOffset) throws ASTSemanticException
    {
    	// first we look up the symbol in the PST see if it already exists 
    	// if not we create it 
    	// TODO : handle the anonymous case
    	
    	IContainerSymbol pstScope = scopeToSymbol(scope);
    	ISymbol namespaceSymbol  = null; 
    	try
        {
            namespaceSymbol = pstScope.Lookup( identifier );
        }
        catch (ParserSymbolTableException e)
        {
            throw new ASTSemanticException();
        }
        
        if( namespaceSymbol != null )
        {
        	if( namespaceSymbol.getType() != ParserSymbolTable.TypeInfo.t_namespace )
        		throw new ASTSemanticException(); 
        }
        else
        {
        	namespaceSymbol = pst.newContainerSymbol( identifier, ParserSymbolTable.TypeInfo.t_namespace );
        	try
            {
                pstScope.addSymbol( namespaceSymbol );
            }
            catch (ParserSymbolTableException e1)
            {
            	// not overloading, should never happen
            }
        }
        
        ASTNamespaceDefinition namespaceDef = new ASTNamespaceDefinition( namespaceSymbol, startingOffset, nameOffset );
        try
        {
            attachSymbolExtension( namespaceSymbol, namespaceDef );
        }
        catch (ExtensionException e1)
        {
        	// will not happen with namespaces
        }
        return namespaceDef;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createCompilationUnit()
     */
    public IASTCompilationUnit createCompilationUnit()
    {
    	ISymbol symbol = pst.getCompilationUnit();
    	ASTCompilationUnit compilationUnit = new ASTCompilationUnit( symbol );
        try
        {
            attachSymbolExtension(symbol, compilationUnit );
        }
        catch (ExtensionException e)
        {
			//should not happen with CompilationUnit
        }
    	return compilationUnit; 
    }
    
    
	protected void attachSymbolExtension(
		ISymbol symbol,
		ASTSymbol astSymbol ) throws ExtensionException
	{
		ISymbolASTExtension extension = symbol.getASTExtension();
		if( extension == null )
		{
			if( astSymbol instanceof IASTNamespaceDefinition )
				extension = new NamespaceSymbolExtension( symbol, astSymbol );
			else if( astSymbol instanceof IASTFunction ) // TODO : other foreward declare cases
			{
				extension = new ForewardDeclaredSymbolExtension( symbol, astSymbol );
			}
			else
			{
				extension = new StandardSymbolExtension( symbol, astSymbol );
			}
			symbol.setASTExtension( extension );
		}
		else
		{
			extension.addDefinition( astSymbol );
		}
		
		

	}
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, int)
     */
    public IASTLinkageSpecification createLinkageSpecification(
        IASTScope scope,
        String spec,
        int startingOffset)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, org.eclipse.cdt.core.parser.ast.ASTClassKind, org.eclipse.cdt.core.parser.ast.IASTClassSpecifier.ClassNameType, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility, org.eclipse.cdt.core.parser.ast.IASTTemplate, int, int)
     */
    public IASTClassSpecifier createClassSpecifier(
        IASTScope scope,
        String name,
        ASTClassKind kind,
        ClassNameType type,
        ASTAccessVisibility access,
        int startingOffset,
        int nameOffset) throws ASTSemanticException
    {
        IContainerSymbol containerSymbol = scopeToSymbol(scope);
		ParserSymbolTable.TypeInfo.eType pstType = null;
		
		if( kind == ASTClassKind.CLASS )
			pstType = ParserSymbolTable.TypeInfo.t_class;
		else if( kind == ASTClassKind.STRUCT )
			pstType = ParserSymbolTable.TypeInfo.t_struct;
		else if( kind == ASTClassKind.UNION )
			pstType = ParserSymbolTable.TypeInfo.t_union;
		else
			throw new ASTSemanticException();
			
        IDerivableContainerSymbol classSymbol = pst.newDerivableContainerSymbol( name, pstType );
        try
        {
            containerSymbol.addSymbol( classSymbol );
        }
        catch (ParserSymbolTableException e)
        {
			throw new ASTSemanticException();
        }
        
        ASTClassSpecifier classSpecifier = new ASTClassSpecifier( classSymbol, kind, type, access, startingOffset, nameOffset );
        try
        {
            attachSymbolExtension(classSymbol, classSpecifier );
        }
        catch (ExtensionException e1)
        {
            throw new ASTSemanticException();
        }
        return classSpecifier;
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#addBaseSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier, boolean, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility, java.lang.String)
     */
    public void addBaseSpecifier(
        IASTClassSpecifier astClassSpec,
        boolean isVirtual,
        ASTAccessVisibility visibility,
        ITokenDuple parentClassName) throws ASTSemanticException 
    {
    	IDerivableContainerSymbol classSymbol = (IDerivableContainerSymbol)scopeToSymbol( astClassSpec);
        Iterator iterator = parentClassName.iterator();
        List references = new ArrayList(); 
        
        if( ! iterator.hasNext() )
        	throw new ASTSemanticException();
        	
		IToken t1 = (IToken)iterator.next();
		IContainerSymbol symbol = null; 
		
		symbol = getScopeToSearchUpon(astClassSpec.getOwnerScope(), t1, references );
		
		while( iterator.hasNext() )
		{
			IToken t = (IToken)iterator.next(); 
			if( t.getType() == IToken.tCOLONCOLON ) continue; 
			try
			{
				symbol = symbol.LookupNestedNameSpecifier( t.getImage() );
				references.add( createReference( symbol, t.getImage(), t.getOffset() ));
			}
			catch( ParserSymbolTableException pste )
			{
				throw new ASTSemanticException();
			}
		}
		
		classSymbol.addParent( symbol, isVirtual, visibility, parentClassName.getFirstToken().getOffset(), references );
		 
    }
    /**
     * @param symbol
     * @param string
     * @return
     */
    private IASTReference createReference(ISymbol symbol, String string, int offset ) throws ASTSemanticException 
    {
        if( symbol.getType() == ParserSymbolTable.TypeInfo.t_namespace )
        {
        	return new ASTNamespaceReference( offset, string, (IASTNamespaceDefinition)symbol.getASTExtension().getPrimaryDeclaration());
        }
        else if( symbol.getType() == ParserSymbolTable.TypeInfo.t_class || 
				 symbol.getType() == ParserSymbolTable.TypeInfo.t_struct || 
				 symbol.getType() == ParserSymbolTable.TypeInfo.t_union ) 
		{  
			return new ASTClassReference( offset, string, (IASTClassSpecifier)symbol.getASTExtension().getPrimaryDeclaration() );
		}
        throw new ASTSemanticException(); 
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createElaboratedTypeSpecifier(org.eclipse.cdt.core.parser.ast.ASTClassKind, java.lang.String, int, int)
     */
    public IASTElaboratedTypeSpecifier createElaboratedTypeSpecifier(
        ASTClassKind elaboratedClassKind,
        String typeName,
        int startingOffset,
        int endOffset)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createEnumerationSpecifier(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, int, int)
     */
    public IASTEnumerationSpecifier createEnumerationSpecifier(
        IASTScope scope,
        String name,
        int startingOffset,
        int nameOffset)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#addEnumerator(org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier, java.lang.String, int, int, org.eclipse.cdt.core.parser.ast.IASTExpression)
     */
    public void addEnumerator(
        IASTEnumerationSpecifier enumeration,
        String string,
        int startingOffset,
        int endingOffset,
        IASTExpression initialValue)
    {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createExpression(org.eclipse.cdt.core.parser.ast.IASTExpression.Kind, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, java.lang.String, java.lang.String, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor)
     */
    public IASTExpression createExpression(
        Kind kind,
        IASTExpression lhs,
        IASTExpression rhs,
        IASTExpression thirdExpression,
        String id,
        String typeId,
        String literal,
        IASTNewExpressionDescriptor newDescriptor)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNewDescriptor()
     */
    public IASTNewExpressionDescriptor createNewDescriptor()
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createInitializerClause(org.eclipse.cdt.core.parser.ast.IASTInitializerClause.Kind, org.eclipse.cdt.core.parser.ast.IASTExpression, java.util.List)
     */
    public IASTInitializerClause createInitializerClause(
        org.eclipse.cdt.core.parser.ast.IASTInitializerClause.Kind kind,
        IASTExpression assignmentExpression,
        List initializerClauses)
    {
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
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createConstructorMemberInitializer(org.eclipse.cdt.core.parser.ITokenDuple, org.eclipse.cdt.core.parser.ast.IASTExpression)
     */
    public IASTConstructorMemberInitializer createConstructorMemberInitializer(
        ITokenDuple duple,
        IASTExpression expressionList)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createSimpleTypeSpecifier(org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type, org.eclipse.cdt.core.parser.ITokenDuple, boolean, boolean, boolean, boolean, boolean)
     */
    public IASTSimpleTypeSpecifier createSimpleTypeSpecifier(
        Type kind,
        ITokenDuple typeName,
        boolean isShort,
        boolean isLong,
        boolean isSigned,
        boolean isUnsigned,
        boolean isTypename)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createFunction(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, java.util.List, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.IASTTemplate)
     */
    public IASTFunction createFunction(
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
        IASTTemplate ownerTemplate)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createAbstractDeclaration(boolean, org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier, java.util.List, java.util.List)
     */
    public IASTAbstractDeclaration createAbstractDeclaration(
        boolean isConst,
        IASTTypeSpecifier typeSpecifier,
        List pointerOperators,
        List arrayModifiers)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createMethod(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, java.util.List, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.IASTTemplate, boolean, boolean, boolean, boolean, boolean, boolean, boolean, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility)
     */
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
        IASTTemplate ownerTemplate,
        boolean isConst,
        boolean isVolatile,
        boolean isConstructor,
        boolean isDestructor,
        boolean isVirtual,
        boolean isExplicit,
        boolean isPureVirtual,
        ASTAccessVisibility visibility)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createVariable(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, boolean, org.eclipse.cdt.core.parser.ast.IASTInitializerClause, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, boolean, boolean, boolean, boolean, int, int)
     */
    public IASTVariable createVariable(
        IASTScope scope,
        String name,
        boolean isAuto,
        IASTInitializerClause initializerClause,
        IASTExpression bitfieldExpression,
        IASTAbstractDeclaration abstractDeclaration,
        boolean isMutable,
        boolean isExtern,
        boolean isRegister,
        boolean isStatic,
        int startingOffset,
        int nameOffset)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createField(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, boolean, org.eclipse.cdt.core.parser.ast.IASTInitializerClause, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, boolean, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility)
     */
    public IASTField createField(
        IASTScope scope,
        String name,
        boolean isAuto,
        IASTInitializerClause initializerClause,
        IASTExpression bitfieldExpression,
        IASTAbstractDeclaration abstractDeclaration,
        boolean isMutable,
        boolean isExtern,
        boolean isRegister,
        boolean isStatic,
        int startingOffset,
        int nameOffset,
        ASTAccessVisibility visibility)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createParameterDeclaration(boolean, org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier, java.util.List, java.util.List, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTInitializerClause)
     */
    public IASTParameterDeclaration createParameterDeclaration(
        boolean isConst,
        IASTTypeSpecifier getTypeSpecifier,
        List pointerOperators,
        List arrayModifiers,
        String parameterName,
        IASTInitializerClause initializerClause)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope, java.util.List, boolean, int)
     */
    public IASTTemplateDeclaration createTemplateDeclaration(
        IASTScope scope,
        List templateParameters,
        boolean exported,
        int startingOffset)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateParameter(org.eclipse.cdt.core.parser.ast.IASTTemplateParameter.ParamKind, java.lang.String, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration, java.util.List)
     */
    public IASTTemplateParameter createTemplateParameter(
        ParamKind kind,
        String identifier,
        String defaultValue,
        IASTParameterDeclaration parameter,
        List parms)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateInstantiation(org.eclipse.cdt.core.parser.ast.IASTScope, int)
     */
    public IASTTemplateInstantiation createTemplateInstantiation(
        IASTScope scope,
        int startingOffset)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTScope, int)
     */
    public IASTTemplateSpecialization createTemplateSpecialization(
        IASTScope scope,
        int startingOffset)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTypedef(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, int, int)
     */
    public IASTTypedefDeclaration createTypedef(
        IASTScope scope,
        String name,
        IASTAbstractDeclaration mapping,
        int startingOffset,
        int nameOffset)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTypeSpecDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier, org.eclipse.cdt.core.parser.ast.IASTTemplate, int, int)
     */
    public IASTAbstractTypeSpecifierDeclaration createTypeSpecDeclaration(
        IASTScope scope,
        IASTTypeSpecifier typeSpecifier,
        IASTTemplate template,
        int startingOffset,
        int endingOffset)
    {
        return new ASTAbstractTypeSpecifierDeclaration( scopeToSymbol(scope), typeSpecifier, template, startingOffset, endingOffset);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createPointerToFunction(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, java.util.List, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.IASTTemplate, org.eclipse.cdt.core.parser.ast.ASTPointerOperator)
     */
    public IASTPointerToFunction createPointerToFunction(
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
        IASTTemplate ownerTemplate,
        ASTPointerOperator pointerOperator)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createPointerToMethod(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, java.util.List, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.IASTTemplate, boolean, boolean, boolean, boolean, boolean, boolean, boolean, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility, org.eclipse.cdt.core.parser.ast.ASTPointerOperator)
     */
    public IASTPointerToMethod createPointerToMethod(
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
        IASTTemplate ownerTemplate,
        boolean isConst,
        boolean isVolatile,
        boolean isConstructor,
        boolean isDestructor,
        boolean isVirtual,
        boolean isExplicit,
        boolean isPureVirtual,
        ASTAccessVisibility visibility,
        ASTPointerOperator pointerOperator)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    protected ParserSymbolTable pst = new ParserSymbolTable();
}
