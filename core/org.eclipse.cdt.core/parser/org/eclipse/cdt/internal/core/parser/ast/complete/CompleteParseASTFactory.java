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
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
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
import org.eclipse.cdt.core.parser.ast.IASTNamespaceAlias;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
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
import org.eclipse.cdt.internal.core.parser.pst.ForewardDeclaredSymbolExtension;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolOwner;
import org.eclipse.cdt.internal.core.parser.pst.NamespaceSymbolExtension;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableException;
import org.eclipse.cdt.internal.core.parser.pst.StandardSymbolExtension;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfo;
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

	protected ISymbol lookupQualifiedName( IContainerSymbol startingScope, String name, TypeInfo.eType type, List parameters, int offset, List references, boolean throwOnError ) throws ASTSemanticException
	{
		ISymbol result = null;
		try
		{	
			if( name == null ) throw new ASTSemanticException();
			try
			{
				if(type == TypeInfo.t_function){
					// looking for a function
					result = startingScope.qualifiedFunctionLookup(name, new LinkedList(parameters));
				}else{
					// looking for something else
					result = startingScope.qualifiedLookup(name, type);
				}
				if( result != null ) 
					references.add( createReference( result, name, offset ));
				else
					throw new ASTSemanticException();    
			}
			catch (ParserSymbolTableException e)
			{
				throw new ASTSemanticException();    
			}
			
		}
		catch( ASTSemanticException se )
		{
			if( throwOnError )
				throw se;
			return null;
		}
		return result;			
		
	}

	protected ISymbol lookupQualifiedName( IContainerSymbol startingScope, ITokenDuple name, List references, boolean throwOnError ) throws ASTSemanticException{
		return lookupQualifiedName(startingScope, name, TypeInfo.t_any, null, references, throwOnError);
	}

	protected ISymbol lookupQualifiedName( IContainerSymbol startingScope, ITokenDuple name, TypeInfo.eType type, List parameters, List references, boolean throwOnError ) throws ASTSemanticException
	{
		ISymbol result = null;
		IToken firstSymbol = null;
		try
		{	
			if( name == null ) throw new ASTSemanticException();
			
			switch( name.length() )
			{
				case 0: 
					if( throwOnError )
						throw new ASTSemanticException();
				case 1:
					firstSymbol = name.getFirstToken();
					try
	                {
	                	if(type == TypeInfo.t_function)	                	
							result = startingScope.unqualifiedFunctionLookup( firstSymbol.getImage(), new LinkedList(parameters));
						else
	                    	result = startingScope.lookup( firstSymbol.getImage());
	                    if( result != null ) 
							references.add( createReference( result, firstSymbol.getImage(), firstSymbol.getOffset() ));
						else
							throw new ASTSemanticException();    
	                }
	                catch (ParserSymbolTableException e)
	                {
	                 	throw new ASTSemanticException();    
	                }
	                break;
				case 2: 
					firstSymbol = name.getFirstToken();
					if( firstSymbol.getType() != IToken.tCOLONCOLON )
						throw new ASTSemanticException();
					try
					{
						if(type == TypeInfo.t_function)	                	
							result = pst.getCompilationUnit().unqualifiedFunctionLookup( name.getLastToken().getImage(), new LinkedList(parameters));
						else
							result = pst.getCompilationUnit().lookup( name.getLastToken().getImage() );
						references.add( createReference( result, name.getLastToken().getImage(), name.getLastToken().getOffset() ));
					}
					catch( ParserSymbolTableException e)
					{
						throw new ASTSemanticException();
					}
					break;
				default:
					Iterator iter = name.iterator();
					firstSymbol = name.getFirstToken();
					result = startingScope;
					if( firstSymbol.getType() == IToken.tCOLONCOLON )
						result = pst.getCompilationUnit();
					while( iter.hasNext() )
					{
						IToken t = (IToken)iter.next();
						if( t.getType() == IToken.tCOLONCOLON ) continue;
						try
						{
							if( t == name.getLastToken() ) 
								if(type == TypeInfo.t_function)	                	
									result = ((IContainerSymbol)result).qualifiedFunctionLookup( t.getImage(), new LinkedList(parameters) );
								else						
									result = ((IContainerSymbol)result).qualifiedLookup( t.getImage() );
							else
								result = ((IContainerSymbol)result).lookupNestedNameSpecifier( t.getImage() );
							references.add( createReference( result, t.getImage(), t.getOffset() ));
						}
						catch( ParserSymbolTableException pste )
						{
							throw new ASTSemanticException();						
						}
					}
					 
			}
		}
		catch( ASTSemanticException se )
		{
			if( throwOnError )
				throw se;
			return null;
		}
		return result;
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
		List references = new ArrayList();	
		ISymbol symbol = lookupQualifiedName( 
			scopeToSymbol( scope), duple, references, true ); 

		try {
			((ASTScope)scope).getContainerSymbol().addUsingDirective( (IContainerSymbol)symbol );
		} catch (ParserSymbolTableException pste) {
			throw new ASTSemanticException();	
		}
		
		IASTUsingDirective astUD = new ASTUsingDirective( scopeToSymbol(scope), ((IASTNamespaceDefinition)symbol.getASTExtension().getPrimaryDeclaration()), startingOffset, endingOffset, references );
		return astUD;
    }
    

    protected IContainerSymbol getScopeToSearchUpon(
        IASTScope currentScope,
        IToken firstToken, Iterator iterator ) throws ASTSemanticException
    {
		if( firstToken.getType() == IToken.tCOLONCOLON )  
		{ 
			iterator.next();
			return pst.getCompilationUnit();
		}
		else
		{
			return (IContainerSymbol)scopeToSymbol(currentScope);
		}
        	
        
    }
    protected IContainerSymbol scopeToSymbol(IASTScope currentScope)
    {
    	if( currentScope instanceof ASTScope )
        	return ((ASTScope)currentScope).getContainerSymbol();
        else
        	return scopeToSymbol(((ASTAnonymousDeclaration)currentScope).getOwnerScope());
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createUsingDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope, boolean, org.eclipse.cdt.core.parser.ITokenDuple, int, int)
     */
    public IASTUsingDeclaration createUsingDeclaration(
        IASTScope scope,
        boolean isTypeName,
        ITokenDuple name,
        int startingOffset,
        int endingOffset) throws ASTSemanticException
    {
        List references = new ArrayList(); 
		ISymbol symbol = lookupQualifiedName( scopeToSymbol(scope), name, references, true );
        
        try
        {
            scopeToSymbol(scope).addUsingDeclaration( name.getLastToken().getImage(), symbol.getContainingSymbol() );
        }
        catch (ParserSymbolTableException e)
        {
        	throw new ASTSemanticException();
        }
        return new ASTUsingDeclaration( scope, 
        	symbol.getASTExtension().getPrimaryDeclaration(), isTypeName, startingOffset, endingOffset, references );
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
        
        return new ASTASMDefinition( scopeToSymbol(scope), assembly, first, last );
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
    	
    	IContainerSymbol pstScope = scopeToSymbol(scope);
    	ISymbol namespaceSymbol  = null; 

    	
    	if( ! identifier.equals( "" ) )
    	{
	    	try
	        {
	            namespaceSymbol = pstScope.qualifiedLookup( identifier );
	        }
	        catch (ParserSymbolTableException e)
	        {
	            throw new ASTSemanticException();
	        }
    	}
        
        if( namespaceSymbol != null )
        {
        	if( namespaceSymbol.getType() != TypeInfo.t_namespace )
        		throw new ASTSemanticException(); 
        }
        else
        {
        	namespaceSymbol = pst.newContainerSymbol( identifier, TypeInfo.t_namespace );
        	if( identifier.equals( "" ) )
        		namespaceSymbol.setContainingSymbol( pstScope );	
        	else
        	{
	        	
	        	try
	            {
	                pstScope.addSymbol( namespaceSymbol );
	            }
	            catch (ParserSymbolTableException e1)
	            {
	            	// not overloading, should never happen
	            }
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
        return new ASTLinkageSpecification( scopeToSymbol( scope ), spec, startingOffset );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, org.eclipse.cdt.core.parser.ast.ASTClassKind, org.eclipse.cdt.core.parser.ast.IASTClassSpecifier.ClassNameType, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility, org.eclipse.cdt.core.parser.ast.IASTTemplate, int, int)
     */
    public IASTClassSpecifier createClassSpecifier(
        IASTScope scope,
        ITokenDuple name,
        ASTClassKind kind,
        ClassNameType type,
        ASTAccessVisibility access,
        int startingOffset,
        int nameOffset) throws ASTSemanticException
    {
        IContainerSymbol currentScopeSymbol = scopeToSymbol(scope);
        TypeInfo.eType pstType = classKindToTypeInfo(kind);		
		List references = new ArrayList();

		String newSymbolName = "";
		
		if( name != null ){
			IToken lastToken = name.getLastToken();
			if( name.length() != 1 ) // qualified name 
			{
				 ITokenDuple containerSymbolName = 
					name.getSubrange( 0, name.length() - 3 ); // -1 for index, -2 for last hop of qualified name
				 currentScopeSymbol = (IContainerSymbol)lookupQualifiedName( currentScopeSymbol, 
					containerSymbolName, references, true);
				 if( currentScopeSymbol == null )
					throw new ASTSemanticException();
			}
			newSymbolName = lastToken.getImage();
		}
		
		ISymbol classSymbol = null;
		if( !newSymbolName.equals("") ){
			try
			{
				classSymbol = currentScopeSymbol.lookupMemberForDefinition(newSymbolName);
			}
			catch (ParserSymbolTableException e)
			{
				throw new ASTSemanticException();
			}
	        
			if( classSymbol != null && ! classSymbol.isForwardDeclaration() )
				throw new ASTSemanticException();
			
			if( classSymbol != null && classSymbol.getType() != pstType )
				throw new ASTSemanticException();
		}

		IDerivableContainerSymbol newSymbol = pst.newDerivableContainerSymbol( newSymbolName, pstType );
		
		if( classSymbol != null )
			classSymbol.setTypeSymbol( newSymbol );
			
		try
        {
            currentScopeSymbol.addSymbol( newSymbol );
        }
        catch (ParserSymbolTableException e2)
        {
        	throw new ASTSemanticException();
        }
			
        ASTClassSpecifier classSpecifier = new ASTClassSpecifier( newSymbol, kind, type, access, startingOffset, nameOffset, references );
        try
        {
            attachSymbolExtension(newSymbol, classSpecifier );
        }
        catch (ExtensionException e1)
        {
            throw new ASTSemanticException();
        }
        return classSpecifier;
    }
    
    protected TypeInfo.eType classKindToTypeInfo(ASTClassKind kind)
        throws ASTSemanticException
    {
        TypeInfo.eType pstType = null;
        
        if( kind == ASTClassKind.CLASS )
        	pstType = TypeInfo.t_class;
        else if( kind == ASTClassKind.STRUCT )
        	pstType = TypeInfo.t_struct;
        else if( kind == ASTClassKind.UNION )
        	pstType = TypeInfo.t_union;
        else
        	throw new ASTSemanticException();
        return pstType;
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
        	
		IContainerSymbol symbol = null; 
		
		symbol = getScopeToSearchUpon(astClassSpec, (IToken)parentClassName.getFirstToken(), iterator );
		
		while( iterator.hasNext() )
		{
			IToken t = (IToken)iterator.next(); 
			if( t.getType() == IToken.tCOLONCOLON ) continue; 
			try
			{
				if( t == parentClassName.getLastToken())
					symbol = (IContainerSymbol)symbol.lookup( t.getImage() );
				else
					symbol = symbol.lookupNestedNameSpecifier( t.getImage() );
				
				if( symbol != null )
					references.add( createReference( symbol, t.getImage(), t.getOffset() ));
				else
					throw new ASTSemanticException();
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
    protected IASTReference createReference(ISymbol symbol, String string, int offset ) throws ASTSemanticException 
    {
    	if( symbol == null )
    		throw new ASTSemanticException(); 
    		
        if( symbol.getType() == TypeInfo.t_namespace )
        {
        	return new ASTNamespaceReference( offset, string, (IASTNamespaceDefinition)symbol.getASTExtension().getPrimaryDeclaration());
        }
        else if( symbol.getType() == TypeInfo.t_class || 
				 symbol.getType() == TypeInfo.t_struct || 
				 symbol.getType() == TypeInfo.t_union ) 
		{  
			return new ASTClassReference( offset, string, (IASTTypeSpecifier)symbol.getASTExtension().getPrimaryDeclaration() );
		}
		else if( symbol.getTypeInfo().checkBit( TypeInfo.isTypedef ))
		{
			return new ASTTypedefReference( offset, string, (IASTTypedefDeclaration)symbol.getASTExtension().getPrimaryDeclaration());
		}
		else if( symbol.getType() == TypeInfo.t_enumeration )
			return new ASTEnumerationReference( offset, string,  (IASTEnumerationSpecifier)symbol.getASTExtension().getPrimaryDeclaration() );
		else if( symbol.getType() == TypeInfo.t_function )
		{
			if( symbol.getContainingSymbol().getTypeInfo().isType( TypeInfo.t_class, TypeInfo.t_union ) )
				return new ASTMethodReference( offset, string, (IASTMethod)symbol.getASTExtension().getPrimaryDeclaration() ); 
			else
				return new ASTFunctionReference( offset, string, (IASTFunction)symbol.getASTExtension().getPrimaryDeclaration() );
		}
		else if( ( symbol.getType() == TypeInfo.t_type ) || 
				( symbol.getType() == TypeInfo.t_bool )||
				( symbol.getType() == TypeInfo.t_char  ) ||     
				( symbol.getType() == TypeInfo.t_wchar_t )||
				( symbol.getType() == TypeInfo.t_int )   ||
				( symbol.getType() == TypeInfo.t_float )||
				( symbol.getType() == TypeInfo.t_double ) ||    
				( symbol.getType() == TypeInfo.t_void )  )
			
		{
			if( symbol.getContainingSymbol().getType() == TypeInfo.t_class || 
				symbol.getContainingSymbol().getType() == TypeInfo.t_struct || 
				symbol.getContainingSymbol().getType() == TypeInfo.t_union )
			{
				return new ASTFieldReference( offset, string, (IASTField)symbol.getASTExtension().getPrimaryDeclaration());
			}
			else
			{
				return new ASTVariableReference( offset, string, (IASTVariable)symbol.getASTExtension().getPrimaryDeclaration());
			}
		}
        throw new ASTSemanticException(); 
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createEnumerationSpecifier(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, int, int)
     */
    public IASTEnumerationSpecifier createEnumerationSpecifier(
        IASTScope scope,
        String name,
        int startingOffset,
        int nameOffset) throws ASTSemanticException
    {
		IContainerSymbol containerSymbol = scopeToSymbol(scope);
		TypeInfo.eType pstType = TypeInfo.t_enumeration;
			
		IDerivableContainerSymbol classSymbol = pst.newDerivableContainerSymbol( name, pstType );
		try
		{
			containerSymbol.addSymbol( classSymbol );
		}
		catch (ParserSymbolTableException e)
		{
			throw new ASTSemanticException();
		}
        
        ASTEnumerationSpecifier enumSpecifier = new ASTEnumerationSpecifier( classSymbol, startingOffset, nameOffset );
		
		try
		{
			attachSymbolExtension(classSymbol, enumSpecifier );
		}
		catch (ExtensionException e1)
		{
			throw new ASTSemanticException();
		}
		return enumSpecifier;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#addEnumerator(org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier, java.lang.String, int, int, org.eclipse.cdt.core.parser.ast.IASTExpression)
     */
    public void addEnumerator(
        IASTEnumerationSpecifier enumeration,
        String string,
        int startingOffset,
        int endingOffset,
        IASTExpression initialValue) throws ASTSemanticException
    {
        IContainerSymbol enumerationSymbol = (IContainerSymbol)((ISymbolOwner)enumeration).getSymbol();
        
        ISymbol enumeratorSymbol = pst.newSymbol( string, TypeInfo.t_enumerator );
        try
        {
            enumerationSymbol.addSymbol( enumeratorSymbol );
        }
        catch (ParserSymbolTableException e1)
        {
			throw new ASTSemanticException();
        }
        ASTEnumerator enumerator = new ASTEnumerator( enumeratorSymbol, enumeration, startingOffset, endingOffset, initialValue ); 
        ((ASTEnumerationSpecifier)enumeration).addEnumerator( enumerator );
        try
        {
            attachSymbolExtension( enumeratorSymbol, enumerator );
        }
        catch (ExtensionException e)
        {
            throw new ASTSemanticException();
        }
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createExpression(org.eclipse.cdt.core.parser.ast.IASTExpression.Kind, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTExpression, java.lang.String, java.lang.String, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor)
     */
    public IASTExpression createExpression(
        IASTScope scope,
        Kind kind,
        IASTExpression lhs,
        IASTExpression rhs,
        IASTExpression thirdExpression,
        ITokenDuple typeId,
        String literal, IASTNewExpressionDescriptor newDescriptor) throws ASTSemanticException
    {
    	List references = new ArrayList(); 
    	
        getExpressionReferences(lhs, references);
        getExpressionReferences(rhs, references);
        getExpressionReferences(thirdExpression,references);
    	
		// add newDescriptor's references & add to references
    	// if there is a newDescriptor, check related expressions
    	if(newDescriptor != null){
    		Iterator i  = newDescriptor.getNewPlacementExpressions();
			while (i.hasNext()){
				getExpressionReferences((IASTExpression)i.next(), references);    			
    		}
			i  = newDescriptor.getNewTypeIdExpressions();
			while (i.hasNext()){
				getExpressionReferences((IASTExpression)i.next(), references);    			
			}
			i  = newDescriptor.getNewInitializerExpressions();
			while (i.hasNext()){
				getExpressionReferences((IASTExpression)i.next(), references);    			
			}
    	}
    	
        //look up id & add to references
        IContainerSymbol startingScope = scopeToSymbol( scope );
                
        //look up typeId & add to references
        if( typeId != null )
        	lookupQualifiedName( startingScope, typeId, references, false );
		
		if (kind == IASTExpression.Kind.POSTFIX_FUNCTIONCALL){        							
			ITokenDuple functionId = ((ASTExpression)lhs).getTypeId();
			List parameters = ((ASTExpression)rhs).getResultType();
			lookupQualifiedName(startingScope, functionId, TypeInfo.t_function, parameters, references, false);	        	
		}
        
        ASTExpression expression =  new ASTExpression( kind, lhs, rhs, thirdExpression, 
        							typeId,	literal, newDescriptor, references);
		       							
		expression.setResultType (getExpressionResultType(expression));
        							
        return expression;
    }

	protected List getExpressionResultType(IASTExpression expression){
		List result = new ArrayList();
		
		if (expression.getExpressionKind() == IASTExpression.Kind.PRIMARY_EMPTY) {
			TypeInfo info = new TypeInfo();
			info.setType(TypeInfo.t_void);
			result.add(info);
			return result;
		}
		if (expression.getExpressionKind() == IASTExpression.Kind.PRIMARY_INTEGER_LITERAL) {
			TypeInfo info = new TypeInfo();
			info.setType(TypeInfo.t_int);
			result.add(info);
			return result;
		}
		if (expression.getExpressionKind() == IASTExpression.Kind.PRIMARY_CHAR_LITERAL){
			TypeInfo info = new TypeInfo();
			info.setType(TypeInfo.t_char);
			result.add(info);
			return result;				
		}		
		if (expression.getExpressionKind() == IASTExpression.Kind.PRIMARY_FLOAT_LITERAL){
			TypeInfo info = new TypeInfo();
			info.setType(TypeInfo.t_float);
			result.add(info);
			return result;
		}
		if (expression.getExpressionKind() == IASTExpression.Kind.PRIMARY_BOOLEAN_LITERAL){
			TypeInfo info = new TypeInfo();
			info.setType(TypeInfo.t_bool);
			result.add(info);
			return result;
		}
		if ((expression.getExpressionKind() == IASTExpression.Kind.ADDITIVE_PLUS) 
		|| (expression.getExpressionKind() == IASTExpression.Kind.ADDITIVE_MINUS) ){
			ASTExpression right = (ASTExpression)expression.getLHSExpression();
			ASTExpression left = (ASTExpression)expression.getRHSExpression();  
			if((right != null) && (left != null)){
				TypeInfo rightType =(TypeInfo)right.getResultType().iterator().next();
				TypeInfo leftType =(TypeInfo)left.getResultType().iterator().next();   
				if ( rightType.equals(leftType) ){
					result.add(rightType);
				} else {
					// TODO: two different types added or subtracted
				}
			}
		}
		if(expression.getExpressionKind() == IASTExpression.Kind.EXPRESSIONLIST){
			if(expression.getLHSExpression() != null){
				Iterator i = ((ASTExpression)expression.getLHSExpression()).getResultType().iterator();
				while (i.hasNext()){
					result.add(i.next());	
				}
			}
			if(expression.getRHSExpression() != null){
				Iterator i = ((ASTExpression)expression.getRHSExpression()).getResultType().iterator();
				while (i.hasNext()){
					result.add(i.next());	
				}
			}
			return result;			
		}
		if(expression.getExpressionKind() == IASTExpression.Kind.POSTFIX_FUNCTIONCALL){
			TypeInfo type = new TypeInfo();
			type.setType(TypeInfo.t_function);
			result.add(type);
			return result;
		}
		return result;
	}

    protected void getExpressionReferences(IASTExpression expression, List references)
    {
        if( expression != null )
        {
        	references.addAll( ((ASTExpression)expression).getReferences() );
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNewDescriptor()
     */
    public IASTNewExpressionDescriptor createNewDescriptor(List newPlacementExpressions,List newTypeIdExpressions,List newInitializerExpressions)
    {
		return new ASTNewDescriptor(newPlacementExpressions, newTypeIdExpressions, newInitializerExpressions);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createExceptionSpecification(java.util.List)
     */
    public IASTExceptionSpecification createExceptionSpecification(IASTScope scope, List typeIds) throws ASTSemanticException
    {
    	List references = new ArrayList(); 
    	List newTypeIds = new ArrayList(); 
        if( typeIds != null )
        {
        	Iterator iter =typeIds.iterator();
        	while( iter.hasNext() )
        	{
        		ITokenDuple duple = (ITokenDuple)iter.next();
        		if( duple != null )
        		{
        			lookupQualifiedName( scopeToSymbol( scope ), duple, references, false  );
        			newTypeIds.add( duple.toString() );
        		}
        	}
        }
        return new ASTExceptionSpecification( newTypeIds, references );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createConstructorMemberInitializer(org.eclipse.cdt.core.parser.ITokenDuple, org.eclipse.cdt.core.parser.ast.IASTExpression)
     */
    public IASTConstructorMemberInitializer createConstructorMemberInitializer(
        IASTScope scope,
        ITokenDuple duple, IASTExpression expressionList) throws ASTSemanticException
    {
        List references = new ArrayList(); 
        
        IContainerSymbol scopeSymbol = scopeToSymbol(scope);
        if( duple != null )
        	lookupQualifiedName( scopeSymbol, duple, references, false );
        
        getExpressionReferences( expressionList, references ); 
        return new ASTConstructorMemberInitializer( expressionList, duple == null ? "" : duple.toString(), references );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createSimpleTypeSpecifier(org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type, org.eclipse.cdt.core.parser.ITokenDuple, boolean, boolean, boolean, boolean, boolean)
     */
    public IASTSimpleTypeSpecifier createSimpleTypeSpecifier(
        IASTScope scope,
        Type kind,
        ITokenDuple typeName,
        boolean isShort,
        boolean isLong,
        boolean isSigned,
        boolean isUnsigned, boolean isTypename) throws ASTSemanticException
    {
    	TypeInfo.eType type = null;
    	
    	if( kind == IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME )
    	{
    		type = TypeInfo.t_type;
    	}
	    else if( kind == IASTSimpleTypeSpecifier.Type.BOOL )
        {
        	type = TypeInfo.t_bool;
        }
        else if( kind == IASTSimpleTypeSpecifier.Type.CHAR )
        {
			type = TypeInfo.t_char;
        }
		else if( kind == IASTSimpleTypeSpecifier.Type.DOUBLE )
		{
			type = TypeInfo.t_double;
		}
		else if( kind == IASTSimpleTypeSpecifier.Type.FLOAT )
		{
			type = TypeInfo.t_double;
		}
		else if( kind == IASTSimpleTypeSpecifier.Type.INT )
		{
			type = TypeInfo.t_int;
		}
		else if( kind == IASTSimpleTypeSpecifier.Type.VOID )
		{
			type = TypeInfo.t_void;
		}
		else if( kind == IASTSimpleTypeSpecifier.Type.WCHAR_T)
		{
			type = TypeInfo.t_wchar_t;
		}
	
		List references = new ArrayList(); 
		ISymbol s = pst.newSymbol( "", type );
		if( kind == IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME )
		{
			// lookup the duple
			Iterator i = typeName.iterator();
			IToken first = typeName.getFirstToken();
			
			ISymbol typeSymbol = getScopeToSearchUpon( scope, first, i );
						
			while( i.hasNext() )
			{
				IToken current = (IToken)i.next(); 
				if( current.getType() == IToken.tCOLONCOLON ) continue;
				
				try
                {
                	if( current != typeName.getLastToken() )
                    	typeSymbol = ((IContainerSymbol)typeSymbol).lookupNestedNameSpecifier( current.getImage());
                    else
						typeSymbol = ((IContainerSymbol)typeSymbol).lookup( current.getImage());
					
					if( typeSymbol != null )	
                    	references.add( createReference( typeSymbol, current.getImage(), current.getOffset() ));
                    else
                    	throw new ASTSemanticException();
                }
                catch (ParserSymbolTableException e)
                {
                	throw new ASTSemanticException();    
                } 
			}
			s.setTypeSymbol( typeSymbol );
		}
		
		
		s.getTypeInfo().setBit( isLong, TypeInfo.isLong );
		s.getTypeInfo().setBit( isShort, TypeInfo.isShort);
		s.getTypeInfo().setBit( isUnsigned, TypeInfo.isUnsigned );
			
		return new ASTSimpleTypeSpecifier( s, false, typeName.toString(), references );

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
	    IASTTemplate ownerTemplate,
		boolean isConst,
		boolean isVolatile,
		boolean isVirtual,
		boolean isExplicit,
		boolean isPureVirtual,
		ASTAccessVisibility visibility, 
		List constructorChain) throws ASTSemanticException
	{
		List references = new ArrayList();
		IContainerSymbol ownerScope = scopeToSymbol( scope );		
		
		// check if this is a method in a body file
		StringTokenizer tokenizer = new StringTokenizer(name,DOUBLE_COLON);
		int tokencount = tokenizer.countTokens();
		if(tokencount > 1){
			List tokens = new ArrayList();
			String oneToken = "";
			// This is NOT a function. This is a method definition
			while (tokenizer.hasMoreTokens()){
				oneToken = tokenizer.nextToken();
				tokens.add(oneToken);
			}
				
			String functionName = oneToken;
			String parentName = name.substring(0, name.lastIndexOf(DOUBLE_COLON));
			
			int numOfTokens = 1;
			int offset = nameOffset;
			IContainerSymbol parentScope = ownerScope;
			Iterator i = tokens.iterator();
			while (i.hasNext() && (numOfTokens++) < tokens.size()){
				String token = (String) i.next();
				IContainerSymbol parentSymbol =
				(IContainerSymbol) lookupQualifiedName(parentScope, token, TypeInfo.t_class, null, offset, references, false);
				if(parentSymbol == null){
					parentSymbol = (IContainerSymbol) lookupQualifiedName(parentScope, token, TypeInfo.t_namespace, null, offset, references, false);						
				}
				if(parentSymbol == null)
					break;
				else {
					parentScope = parentSymbol;
					offset += token.length()+ DOUBLE_COLON.length();
				}
			}
			
			if((parentScope != null) && (parentScope.getType() == TypeInfo.t_class)){
				// find out the visibility of the method's declaration
				List functionReferences = new ArrayList();
				IParameterizedSymbol methodDeclaration = 
					(IParameterizedSymbol) lookupQualifiedName(parentScope, functionName, TypeInfo.t_function, parameters, 0, functionReferences, false);
				if(methodDeclaration != null){
					ASTMethodReference reference = (ASTMethodReference) functionReferences.iterator().next();
					visibility = ((IASTMethod)reference.getReferencedElement()).getVisiblity();		
				}
				return createMethod(scope, functionName, parameters, returnType,
				exception, isInline, isFriend, isStatic, startOffset, offset,
				ownerTemplate, isConst, isVolatile, isVirtual, isExplicit, isPureVirtual,
				visibility, constructorChain,parentName, references);
			}
		}
	
		IParameterizedSymbol symbol = pst.newParameterizedSymbol( name, TypeInfo.t_function );
		setFunctionTypeInfoBits(isInline, isFriend, isStatic, symbol);
		
		setParameter( symbol, returnType, false, references );
		setParameters( symbol, references, parameters.iterator() );
	
		try
		{
			ownerScope.addSymbol( symbol );
		}
		catch (ParserSymbolTableException e)
		{
			throw new ASTSemanticException();   
		}
		ASTFunction function = new ASTFunction( symbol, parameters, returnType, exception, startOffset, nameOffset, ownerTemplate, references );        
	    try
	    {
	        attachSymbolExtension(symbol, function);
	    }
	    catch (ExtensionException e1)
	    {
	        throw new ASTSemanticException();
	    } 
	    return function;
	}
    
    protected void setFunctionTypeInfoBits(
        boolean isInline,
        boolean isFriend,
        boolean isStatic,
        IParameterizedSymbol symbol)
    {
        symbol.getTypeInfo().setBit( isInline, TypeInfo.isInline );
        symbol.getTypeInfo().setBit( isFriend, TypeInfo.isFriend );
        symbol.getTypeInfo().setBit( isStatic, TypeInfo.isStatic );
    }
    
    /**
     * @param symbol
     * @param iterator
     */
    protected void setParameters(IParameterizedSymbol symbol, List references, Iterator iterator) throws ASTSemanticException
    {
        while( iterator.hasNext() )
        {
        	setParameter( symbol, (IASTParameterDeclaration)iterator.next(), true, references );	
        }
    }

    /**
     * @param symbol
     * @param returnType
     */
    protected void setParameter(IParameterizedSymbol symbol, IASTAbstractDeclaration absDecl, boolean isParameter, List references) throws ASTSemanticException
    {
    	if (absDecl.getTypeSpecifier() == null)
    		return;
    		
    	TypeInfo.eType type = null;
    	ISymbol xrefSymbol = null;
    	List newReferences = null; 
        if( absDecl.getTypeSpecifier() instanceof IASTSimpleTypeSpecifier ) 
        {
        	IASTSimpleTypeSpecifier.Type kind = ((IASTSimpleTypeSpecifier)absDecl.getTypeSpecifier()).getType();
        	if( kind == IASTSimpleTypeSpecifier.Type.BOOL )
        		type = TypeInfo.t_bool;
        	else if( kind == IASTSimpleTypeSpecifier.Type.CHAR )
        		type = TypeInfo.t_char;
        	else if( kind == IASTSimpleTypeSpecifier.Type.DOUBLE )
        		type = TypeInfo.t_double;
        	else if( kind == IASTSimpleTypeSpecifier.Type.FLOAT )
        		type = TypeInfo.t_float; 
        	else if( kind == IASTSimpleTypeSpecifier.Type.INT )
        		type = TypeInfo.t_int;
        	else if( kind == IASTSimpleTypeSpecifier.Type.VOID )
        		type = TypeInfo.t_void;
        	else if( kind == IASTSimpleTypeSpecifier.Type.WCHAR_T)
        		type = TypeInfo.t_wchar_t;
        	else if( kind == IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME )
        	{
        		type = TypeInfo.t_type;
        		xrefSymbol = ((ASTSimpleTypeSpecifier)absDecl.getTypeSpecifier()).getSymbol(); 
        		newReferences = ((ASTSimpleTypeSpecifier)absDecl.getTypeSpecifier()).getReferences();
        	}
        	else
        		throw new ASTSemanticException(); 
        }
        else if( absDecl.getTypeSpecifier() instanceof IASTClassSpecifier )
        {
        	ASTClassKind kind = ((IASTClassSpecifier)absDecl.getTypeSpecifier()).getClassKind();
        	if( kind == ASTClassKind.CLASS )
        		type = TypeInfo.t_class;
        	else if( kind == ASTClassKind.STRUCT )
				type = TypeInfo.t_struct;
			else if( kind == ASTClassKind.UNION )
				type = TypeInfo.t_union;
			else
				throw new ASTSemanticException();
        }
        else if( absDecl.getTypeSpecifier() instanceof IASTEnumerationSpecifier )
        {
        	type = TypeInfo.t_enumeration;
        }
        else if( absDecl.getTypeSpecifier() instanceof IASTElaboratedTypeSpecifier )
        {
			ASTClassKind kind = ((IASTElaboratedTypeSpecifier)absDecl.getTypeSpecifier()).getClassKind();
			if( kind == ASTClassKind.CLASS )
				type = TypeInfo.t_class;
			else if( kind == ASTClassKind.STRUCT )
				type = TypeInfo.t_struct;
			else if( kind == ASTClassKind.UNION )
				type = TypeInfo.t_union;
			else if( kind == ASTClassKind.ENUM )
				type = TypeInfo.t_enumeration;
			else
				throw new ASTSemanticException();
        }
        else
        	throw new ASTSemanticException(); 
        
        ISymbol paramSymbol = pst.newSymbol( "", type );
        if( xrefSymbol != null )
        	paramSymbol.setTypeSymbol( xrefSymbol );
        
        setPointerOperators( paramSymbol, absDecl.getPointerOperators(), absDecl.getArrayModifiers() );

        if( isParameter)
        	symbol.addParameter( paramSymbol );
        else
			symbol.setReturnType( paramSymbol );
			
		if( newReferences != null )
			references.addAll( newReferences );
		
    }

    /**
     * @param paramSymbol
     * @param iterator
     */
    protected void setPointerOperators(ISymbol symbol, Iterator pointerOpsIterator, Iterator arrayModsIterator) throws ASTSemanticException
    {
        while( pointerOpsIterator.hasNext() )
        {
        	ASTPointerOperator pointerOperator = (ASTPointerOperator)pointerOpsIterator.next();
        	if( pointerOperator == ASTPointerOperator.REFERENCE )
        		symbol.addPtrOperator( new TypeInfo.PtrOp( TypeInfo.PtrOp.t_reference )); 
        	else if( pointerOperator == ASTPointerOperator.POINTER )
				symbol.addPtrOperator( new TypeInfo.PtrOp( TypeInfo.PtrOp.t_pointer ));
			else if( pointerOperator == ASTPointerOperator.CONST_POINTER )
				symbol.addPtrOperator( new TypeInfo.PtrOp( TypeInfo.PtrOp.t_pointer, true, false ));
			else if( pointerOperator == ASTPointerOperator.VOLATILE_POINTER )
				symbol.addPtrOperator( new TypeInfo.PtrOp( TypeInfo.PtrOp.t_pointer, false, true));
			else
				throw new ASTSemanticException();
        }
        
        while( arrayModsIterator.hasNext() )
        {
        	IASTArrayModifier astArrayModifier = (IASTArrayModifier)arrayModsIterator.next();
        	symbol.addPtrOperator( new TypeInfo.PtrOp( TypeInfo.PtrOp.t_array )); 
        }
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
		boolean isVirtual,
		boolean isExplicit,
		boolean isPureVirtual,
		ASTAccessVisibility visibility, 
		List constructorChain) throws ASTSemanticException
	{
		return createMethod(scope, name, parameters, returnType,
		exception, isInline, isFriend, isStatic, startOffset, nameOffset,
		ownerTemplate, isConst, isVolatile, isVirtual, isExplicit, isPureVirtual,
		visibility, constructorChain,scopeToSymbol(scope).getName(), null);
	}   
	  
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
        boolean isVirtual,
        boolean isExplicit,
        boolean isPureVirtual,
        ASTAccessVisibility visibility, 
        List constructorChain,
        String parentName, 
        List references) throws ASTSemanticException
    {
		boolean isConstructor = false;
		boolean isDestructor = false;

		IContainerSymbol ownerScope = scopeToSymbol( scope );
		IParameterizedSymbol symbol = pst.newParameterizedSymbol( name, TypeInfo.t_function );
		setFunctionTypeInfoBits(isInline, isFriend, isStatic, symbol);
		setMethodTypeInfoBits( symbol, isConst, isVolatile, isVirtual, isExplicit );
		if(references == null)
			references = new ArrayList();
    	
    	if( returnType.getTypeSpecifier() != null )
			setParameter( symbol, returnType, false, references );
		setParameters( symbol, references, parameters.iterator() );
  
		// check constructor / destructor if no return type
		if ( returnType.getTypeSpecifier() == null ){
			if(parentName.indexOf(DOUBLE_COLON) != -1){				
				parentName = parentName.substring(parentName.lastIndexOf(DOUBLE_COLON) + DOUBLE_COLON.length());
			}    	
			if( parentName.equals(name) ){
				isConstructor = true; 
			} else if(name.startsWith(TELTA) && parentName.equals(name.substring(1))){
				isDestructor = true;
			}
		}
		
		try
		{
			if( !isConstructor )
				ownerScope.addSymbol( symbol );
			else
			{
				symbol.setType( TypeInfo.t_constructor );
				((IDerivableContainerSymbol)ownerScope).addConstructor( symbol );
			}
		}
		catch (ParserSymbolTableException e)
		{
			throw new ASTSemanticException();   
		}

  
        ASTMethod method = new ASTMethod( symbol, parameters, returnType, exception, startOffset, nameOffset, ownerTemplate, references, isConstructor, isDestructor, isPureVirtual, visibility, constructorChain );
        try
        {
            attachSymbolExtension( symbol, method );
        }
        catch (ExtensionException e1)
        {
            throw new ASTSemanticException();
        }
        return method;
    }
    /**
     * @param symbol
     * @param isConst
     * @param isVolatile
     * @param isConstructor
     * @param isDestructor
     * @param isVirtual
     * @param isExplicit
     * @param isPureVirtual
     */
    protected void setMethodTypeInfoBits(IParameterizedSymbol symbol, boolean isConst, boolean isVolatile, boolean isVirtual, boolean isExplicit)
    {
        symbol.getTypeInfo().setBit( isConst, TypeInfo.isConst );
		symbol.getTypeInfo().setBit( isVolatile, TypeInfo.isConst );
		symbol.getTypeInfo().setBit( isVirtual, TypeInfo.isVirtual );
		symbol.getTypeInfo().setBit( isExplicit, TypeInfo.isExplicit );
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
        int nameOffset, IASTExpression constructorExpression) throws ASTSemanticException
    {
		List references = new ArrayList(); 
        ISymbol newSymbol = cloneSimpleTypeSymbol(name, abstractDeclaration, references);
        setVariableTypeInfoBits(
            isAuto,
            abstractDeclaration,
            isMutable,
            isExtern,
            isRegister,
            isStatic,
            newSymbol);
		setPointerOperators( newSymbol, abstractDeclaration.getPointerOperators(), abstractDeclaration.getArrayModifiers() );
		try
		{
			scopeToSymbol(scope).addSymbol( newSymbol );
		}
		catch (ParserSymbolTableException e)
		{
			// TODO Auto-generated catch block
		}
        
        ASTVariable variable = new ASTVariable( newSymbol, abstractDeclaration, initializerClause, bitfieldExpression, startingOffset, nameOffset, references, constructorExpression );
        try
        {
            attachSymbolExtension(newSymbol, variable );
        }
        catch (ExtensionException e)
        {
            throw new ASTSemanticException();
        }
        return variable;        
    }
    protected void setVariableTypeInfoBits(
        boolean isAuto,
        IASTAbstractDeclaration abstractDeclaration,
        boolean isMutable,
        boolean isExtern,
        boolean isRegister,
        boolean isStatic,
        ISymbol newSymbol)
    {
        newSymbol.getTypeInfo().setBit( isMutable, TypeInfo.isMutable );
        newSymbol.getTypeInfo().setBit( isAuto, TypeInfo.isAuto );
        newSymbol.getTypeInfo().setBit( isExtern, TypeInfo.isExplicit );
        newSymbol.getTypeInfo().setBit( isRegister, TypeInfo.isRegister );
        newSymbol.getTypeInfo().setBit( isStatic, TypeInfo.isStatic );
        newSymbol.getTypeInfo().setBit( abstractDeclaration.isConst(), TypeInfo.isConst );
    }
    
    protected ISymbol cloneSimpleTypeSymbol(
        String name,
        IASTAbstractDeclaration abstractDeclaration,
        List references) throws ASTSemanticException
    {
    	if( abstractDeclaration.getTypeSpecifier() == null )
    		throw new ASTSemanticException();
        ISymbol newSymbol = null;
		ISymbol symbolToBeCloned = null;		
        if( abstractDeclaration.getTypeSpecifier() instanceof ASTSimpleTypeSpecifier ) 
        {
        	symbolToBeCloned = ((ASTSimpleTypeSpecifier)abstractDeclaration.getTypeSpecifier()).getSymbol();
            references.addAll( ((ASTSimpleTypeSpecifier)abstractDeclaration.getTypeSpecifier()).getReferences() );
        }
        else if( abstractDeclaration.getTypeSpecifier() instanceof ASTClassSpecifier )
        {
        	symbolToBeCloned = ((ASTClassSpecifier)abstractDeclaration.getTypeSpecifier()).getSymbol();
        }
		else if( abstractDeclaration.getTypeSpecifier() instanceof ASTElaboratedTypeSpecifier )
		{
			symbolToBeCloned = ((ASTElaboratedTypeSpecifier)abstractDeclaration.getTypeSpecifier()).getSymbol();
		}
		newSymbol = (ISymbol) symbolToBeCloned.clone(); 
		newSymbol.setName( name );

        return newSymbol;
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
        IASTExpression constructorExpression, ASTAccessVisibility visibility) throws ASTSemanticException
    {
		List references = new ArrayList(); 
		ISymbol newSymbol = cloneSimpleTypeSymbol(name, abstractDeclaration, references);
		setVariableTypeInfoBits(
			isAuto,
			abstractDeclaration,
			isMutable,
			isExtern,
			isRegister,
			isStatic,
			newSymbol);
		setPointerOperators( newSymbol, abstractDeclaration.getPointerOperators(), abstractDeclaration.getArrayModifiers() );
		
		try
		{
			scopeToSymbol(scope).addSymbol( newSymbol );
		}
		catch (ParserSymbolTableException e)
		{
			throw new ASTSemanticException();
		}
		
		ASTField field = new ASTField( newSymbol, abstractDeclaration, initializerClause, bitfieldExpression, startingOffset, nameOffset, references, constructorExpression, visibility );
		try
		{
			attachSymbolExtension(newSymbol, field );
		}
		catch (ExtensionException e)
		{
			throw new ASTSemanticException();
		}
		return field;        


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
        return new ASTTemplateDeclaration();
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
        return new ASTTemplateInstantiation();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTScope, int)
     */
    public IASTTemplateSpecialization createTemplateSpecialization(
        IASTScope scope,
        int startingOffset)
    {
        // TODO Auto-generated method stub
        return new ASTTemplateSpecialization();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTypedef(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, int, int)
     */
    public IASTTypedefDeclaration createTypedef(
        IASTScope scope,
        String name,
        IASTAbstractDeclaration mapping,
        int startingOffset,
        int nameOffset) throws ASTSemanticException
    {
    	IContainerSymbol containerSymbol = scopeToSymbol(scope);
    	ISymbol newSymbol = pst.newSymbol( name, TypeInfo.t_type);
    	newSymbol.getTypeInfo().setBit( true,TypeInfo.isTypedef );
    	
    	
    	List references = new ArrayList();
		if( mapping.getTypeSpecifier() instanceof ASTSimpleTypeSpecifier ) 
	    {
			references.addAll( ((ASTSimpleTypeSpecifier)mapping.getTypeSpecifier()).getReferences() );
		}
    	
    	try
        {
            containerSymbol.addSymbol( newSymbol );
        }
        catch (ParserSymbolTableException e)
        {
            throw new ASTSemanticException(); 
        }
        ASTTypedef d = new ASTTypedef( newSymbol, mapping, startingOffset, nameOffset, references );
        try
        {
            attachSymbolExtension(newSymbol, d );
        }
        catch (ExtensionException e1)
        {
            throw new ASTSemanticException();
        }
        return d; 
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

    
    public IASTElaboratedTypeSpecifier createElaboratedTypeSpecifier(IASTScope scope, ASTClassKind kind, ITokenDuple name, int startingOffset, int endOffset, boolean isForewardDecl) throws ASTSemanticException
    {
		IContainerSymbol currentScopeSymbol = scopeToSymbol(scope);
		TypeInfo.eType pstType = classKindToTypeInfo(kind);		
		List references = new ArrayList();
		IToken lastToken = name.getLastToken();
		if( name.length() != 1 ) // qualified name 
		{
			 ITokenDuple containerSymbolName = 
				name.getSubrange( 0, name.length() - 3 ); // -1 for index, -2 for last hop of qualified name
			 currentScopeSymbol = (IContainerSymbol)lookupQualifiedName( currentScopeSymbol, 
				containerSymbolName, references, true);
			 if( currentScopeSymbol == null )
				throw new ASTSemanticException();
		}
		
		ISymbol checkSymbol = null;
		try
		{
			checkSymbol = currentScopeSymbol.qualifiedLookup(lastToken.getImage());
		}
		catch (ParserSymbolTableException e)
		{
			throw new ASTSemanticException();
		}
        

 		if( isForewardDecl )
 		{
			if( checkSymbol == null ) 
			{ 
				checkSymbol  = pst.newDerivableContainerSymbol( lastToken.getImage(), pstType );
				checkSymbol.setIsForwardDeclaration( true );
				try
	            {
	                currentScopeSymbol.addSymbol( checkSymbol  );
	            }
	            catch (ParserSymbolTableException e1)
	            {
	                throw new ASTSemanticException();
	            }
	            
	            ASTElaboratedTypeSpecifier elab = 
	            	new ASTElaboratedTypeSpecifier( checkSymbol, kind, startingOffset, name.getFirstToken().getOffset(), endOffset, references, isForewardDecl );
	            	
	            try
                {
                    attachSymbolExtension( checkSymbol, elab );
                }
                catch (ExtensionException e2)
                {
                	throw new ASTSemanticException();
                }
			}
 		}


		return (IASTElaboratedTypeSpecifier)checkSymbol.getASTExtension().getPrimaryDeclaration();
    }

    protected ParserSymbolTable pst = new ParserSymbolTable();


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNamespaceAlias(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, org.eclipse.cdt.core.parser.ITokenDuple, int, int, int)
     */
    public IASTNamespaceAlias createNamespaceAlias(IASTScope scope, String identifier, ITokenDuple alias, int startingOffset, int nameOffset, int endOffset) throws ASTSemanticException
    {
        IContainerSymbol startingSymbol = scopeToSymbol(scope);
        List references = new ArrayList();
        
        ISymbol namespaceSymbol = lookupQualifiedName( startingSymbol, alias, references, true );
        
        if( namespaceSymbol.getType() != TypeInfo.t_namespace )
        	throw new ASTSemanticException();
        
        ISymbol newSymbol = pst.newContainerSymbol( identifier, TypeInfo.t_namespace );
        newSymbol.setTypeSymbol( namespaceSymbol );
        
        try
        {
            startingSymbol.addSymbol( newSymbol );
        }
        catch (ParserSymbolTableException e)
        {
        	throw new ASTSemanticException();
        }
        
        ASTNamespaceAlias astAlias = new ASTNamespaceAlias(
        	newSymbol, alias.toString(), (IASTNamespaceDefinition)namespaceSymbol.getASTExtension().getPrimaryDeclaration(), 
        	startingOffset, nameOffset, endOffset, references ); 
        try
        {
            attachSymbolExtension( newSymbol, astAlias );
        }
        catch (ExtensionException e1)
        {
			throw new ASTSemanticException();
        }
        return astAlias;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNewCodeBlock(org.eclipse.cdt.core.parser.ast.IASTScope)
	 */
	public IASTCodeScope createNewCodeBlock(IASTScope scope) {
		IContainerSymbol symbol = scopeToSymbol( scope );
		
		IContainerSymbol newScope = pst.newContainerSymbol("");
		newScope.setContainingSymbol(symbol);
		
		ASTCodeScope codeScope = new ASTCodeScope( newScope );
		try
        {
            attachSymbolExtension( newScope, codeScope );
        }
        catch (ExtensionException e)
        {
        }
		return codeScope;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#queryIsTypeName(org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public boolean queryIsTypeName(IASTScope scope, ITokenDuple nameInQuestion) {
		ISymbol lookupSymbol = null;
		try {
			lookupSymbol =
				lookupQualifiedName(
					scopeToSymbol(scope),
					nameInQuestion,
					new ArrayList(),
					false);
		} catch (ASTSemanticException e) {
			// won't get thrown
		} 
		if( lookupSymbol == null ) return false;
		if( lookupSymbol.isType( TypeInfo.t_type, TypeInfo.t_enumeration ) ) return true;
		return false;
	}
}
