/**********************************************************************
 * Copyright (c) 2002,2003, 2004 Rational Software Corporation and others.
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.Enum;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.ASTSemanticException;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
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
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceAlias;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTReference;
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
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter.ParamKind;
import org.eclipse.cdt.core.parser.extension.IASTFactoryExtension;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.ast.ASTAbstractDeclaration;
import org.eclipse.cdt.internal.core.parser.ast.ASTDesignator;
import org.eclipse.cdt.internal.core.parser.ast.BaseASTFactory;
import org.eclipse.cdt.internal.core.parser.problem.IProblemFactory;
import org.eclipse.cdt.internal.core.parser.pst.ExtensibleSymbolExtension;
import org.eclipse.cdt.internal.core.parser.pst.ForewardDeclaredSymbolExtension;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IDeferredTemplateInstance;
import org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IExtensibleSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolOwner;
import org.eclipse.cdt.internal.core.parser.pst.ITemplateFactory;
import org.eclipse.cdt.internal.core.parser.pst.ITemplateSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ITypeInfo;
import org.eclipse.cdt.internal.core.parser.pst.IUsingDeclarationSymbol;
import org.eclipse.cdt.internal.core.parser.pst.IUsingDirectiveSymbol;
import org.eclipse.cdt.internal.core.parser.pst.NamespaceSymbolExtension;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableError;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTableException;
import org.eclipse.cdt.internal.core.parser.pst.StandardSymbolExtension;
import org.eclipse.cdt.internal.core.parser.pst.TemplateSymbolExtension;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfoProvider;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolASTExtension.ExtensionException;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;
import org.eclipse.cdt.internal.core.parser.util.TraceUtil;


/**
 * @author jcamelon
 * 
 * The CompleteParseASTFactory class creates a complete AST 
 * for a given parsed code. 
 *
 */
public class CompleteParseASTFactory extends BaseASTFactory implements IASTFactory
{
    protected static final char[] THIS         = new char[] { 't', 'h', 'i', 's' };
	private final static ITypeInfo.OperatorExpression SUBSCRIPT;
    private final static IProblemFactory problemFactory = new ASTProblemFactory();
	private final ParserMode mode;

	private static final int BUILTIN_TYPE_SIZE = 64;
	private final Hashtable typeIdCache = new Hashtable( BUILTIN_TYPE_SIZE );
	private final Hashtable simpleTypeSpecCache = new Hashtable( BUILTIN_TYPE_SIZE );
	private static final int DEFAULT_QUALIFIEDNAME_REFERENCE_SIZE = 4;
	private char[] filename;
	
    static 
    {
    	SUBSCRIPT = ITypeInfo.OperatorExpression.subscript;
    }

    static private class LookupType extends Enum {
		public static final LookupType QUALIFIED = new LookupType( 1 );
		public static final LookupType UNQUALIFIED = new LookupType( 2 );
		public static final LookupType FORDEFINITION = new LookupType( 3 );
		public static final LookupType FORFRIENDSHIP = new LookupType( 4 );
		public static final LookupType FORPARENTSCOPE = new LookupType( 5 );

		private LookupType( int constant)
		{
			super( constant ); 
		}
    }
    
    public CompleteParseASTFactory( ParserLanguage language, ParserMode mode, IASTFactoryExtension extension )
    {
        super(extension);
		pst = new ParserSymbolTable( language, mode );
		this.mode = mode;
		filename = EMPTY_STRING;
    }

	/*
	 * Adds a reference to a reference list
	 * Overrides an existing reference if it has the same name and offset
	 */
	protected void addReference(List references, IASTReference reference){
		if( reference == null )
			return;
		if( references == null )
			return;
		
		int size = references.size();
		for( int i = 0; i < size; i++ ){
			IASTReference ref = (IASTReference)references.get(i);
			if (ref != null){
				if( (CharArrayUtils.equals( ref.getNameCharArray(), reference.getNameCharArray()))
				&& (ref.getOffset() == reference.getOffset())
				){
					references.remove(i--);
					size--;
					break; 
				}
			}
		}
		references.add(reference);
	}
	
	protected void addTemplateIdReferences( List references, List templateArgs ){
		if( templateArgs == null )
			return;
		
		int numArgs = templateArgs.size();
		for( int i = 0; i < numArgs; i++ ){
			ASTExpression exp = (ASTExpression) templateArgs.get(i);
			List refs = null;
			if( exp.getExpressionKind() == IASTExpression.Kind.POSTFIX_TYPEID_TYPEID )
				refs = ((ASTTypeId) exp.getTypeId()).getReferences();
			else
				refs = exp.getReferences();
			int numRefs = refs.size();
			for( int j = 0; j < numRefs; j++ ){
				IASTReference r = (IASTReference) refs.get(j);
				addReference( references, r );
			}
		}
	}
	/*
	 * Test if the provided list is a valid parameter list
	 * Parameters are list of TypeInfos
	 */
	protected boolean validParameterList(List parameters){
		int size = parameters.size();
		for( int i = 0; i < size; i++ ){
			ITypeInfo info = (ITypeInfo)parameters.get( i );
			if (info != null){
				if((info.getType() == ITypeInfo.t_type) 
					&& (info.getTypeSymbol() == null))
					return false;
			}else
				return false;
		}
		return true;
	}
	
	private ISymbol lookupElement (IContainerSymbol startingScope, char[] name, ITypeInfo.eType type, List parameters, LookupType lookupType ) throws ASTSemanticException {
		return lookupElement( startingScope, name, type, parameters, null, lookupType );
	}
	
	private ISymbol lookupElement (IContainerSymbol startingScope, char[] name, ITypeInfo.eType type, List parameters, List arguments, LookupType lookupType ) throws ASTSemanticException {
		ISymbol result = null;
		if( startingScope == null ) return null;
		try {
			if((type == ITypeInfo.t_function) || (type == ITypeInfo.t_constructor)){
				// looking for a function
				if(validParameterList(parameters))
					if(type == ITypeInfo.t_constructor){
						IDerivableContainerSymbol startingDerivableScope = (IDerivableContainerSymbol) startingScope;
						result = startingDerivableScope.lookupConstructor(parameters);
					}
					else {
						if( arguments != null )
							result = startingScope.lookupFunctionTemplateId( name, parameters, arguments, ( lookupType == LookupType.FORDEFINITION ) );
						else if( lookupType == LookupType.QUALIFIED )
							result = startingScope.qualifiedFunctionLookup(name, parameters);
						else if( lookupType == LookupType.UNQUALIFIED || lookupType == LookupType.FORPARENTSCOPE)
							result = startingScope.unqualifiedFunctionLookup( name, parameters );
						else if( lookupType == LookupType.FORDEFINITION )
							result = startingScope.lookupMethodForDefinition( name, parameters );
						else if( lookupType == LookupType.FORFRIENDSHIP ){
							result = ((IDerivableContainerSymbol)startingScope).lookupFunctionForFriendship( name, parameters );
						}
					}
				else
					result = null;
			}else{
				// looking for something else
				if( arguments != null )
					result = startingScope.lookupTemplateId( name, arguments );
				else if( lookupType == LookupType.QUALIFIED )
					result = startingScope.qualifiedLookup(name, type);
				else if( lookupType == LookupType.UNQUALIFIED || lookupType == LookupType.FORPARENTSCOPE )
					result = startingScope.elaboratedLookup( type, name );
				else if( lookupType == LookupType.FORDEFINITION )
					result = startingScope.lookupMemberForDefinition( name );
				else if( lookupType == LookupType.FORFRIENDSHIP )
					result = ((IDerivableContainerSymbol)startingScope).lookupForFriendship( name );
			}
		} catch (ParserSymbolTableException e) {
			if( e.reason != ParserSymbolTableException.r_UnableToResolveFunction )
				handleProblem( e.createProblemID(), name );
		} catch (ParserSymbolTableError e){
			handleProblem( IProblem.INTERNAL_RELATED, name );
		}
		return result;		
	}
	
	protected ISymbol lookupQualifiedName( IContainerSymbol startingScope, char[] name, List references, boolean throwOnError, LookupType lookup ) throws ASTSemanticException{
		return lookupQualifiedName(startingScope, name, ITypeInfo.t_any, null, 0, references, throwOnError, lookup );
	}

	protected ISymbol lookupQualifiedName( IContainerSymbol startingScope, char[] name, ITypeInfo.eType type, List parameters, int offset, List references, boolean throwOnError, LookupType lookup ) throws ASTSemanticException
	{
		ISymbol result = null;
		if( name == null && throwOnError )
			handleProblem( IProblem.SEMANTIC_NAME_NOT_PROVIDED, null );
		else if( name == null ) return null;
			
		try
		{
			result = lookupElement(startingScope, name, type, parameters, lookup);
			if( result != null ) 
				addReference(references, createReference( result, name, offset ));
			else if( throwOnError )
				handleProblem( IProblem.SEMANTIC_NAME_NOT_FOUND, name );    
		}
		catch (ASTSemanticException e)
		{
			if( throwOnError )
				throw new ASTSemanticException( e );
			
			return null;
		}
		return result;			
	}

	protected ISymbol lookupQualifiedName( IContainerSymbol startingScope, ITokenDuple name, List references, boolean throwOnError ) throws ASTSemanticException{
		return lookupQualifiedName(startingScope, name, references, throwOnError, LookupType.UNQUALIFIED);
	}
	protected ISymbol lookupQualifiedName( IContainerSymbol startingScope, ITokenDuple name, List references, boolean throwOnError, LookupType lookup ) throws ASTSemanticException{
		return lookupQualifiedName(startingScope, name, ITypeInfo.t_any, null, references, throwOnError, lookup );
	}
	protected ISymbol lookupQualifiedName( IContainerSymbol startingScope, ITokenDuple name, ITypeInfo.eType type, List parameters, List references, boolean throwOnError ) throws ASTSemanticException{
		return lookupQualifiedName( startingScope, name, type, parameters, references, throwOnError, LookupType.UNQUALIFIED );
	}
	
	protected ISymbol lookupQualifiedName( IContainerSymbol startingScope, ITokenDuple name, ITypeInfo.eType type, List parameters, List references, boolean throwOnError, LookupType lookup ) throws ASTSemanticException
	{
		ISymbol result = null;
		if( name == null && throwOnError ) handleProblem( IProblem.SEMANTIC_NAME_NOT_PROVIDED, null );
		else if( name == null ) return null;
		
		List [] templateArgLists = name.getTemplateIdArgLists();
		List args = null;
		int idx = 0;
		char[] image = null;
		switch( name.getSegmentCount() )
		{
			case 0: 
				if( throwOnError )
					handleProblem( IProblem.SEMANTIC_NAME_NOT_PROVIDED, null );
				else
					return null;
			case 1:
				image = name.extractNameFromTemplateId();
				args = ( templateArgLists != null ) ? getTemplateArgList( templateArgLists[ 0 ] ) : null;
				result = lookupElement(startingScope, image, type, parameters, args, lookup );
				
                if( result != null )
                {
                	if( lookup == LookupType.FORPARENTSCOPE && startingScope instanceof ITemplateFactory ){
                		if( args != null )
                			((ITemplateFactory)startingScope).pushTemplateId( result, args );
                		else 
                			((ITemplateFactory)startingScope).pushSymbol( result );
                	}
                	if( references != null )
                		addReference( references, createReference( result, image, name.getStartOffset() ));
					if( args != null && references != null )
					{
						addTemplateIdReferences( references, templateArgLists[0] );
						name.freeReferences( );
					}
                }
				else
				{	
					if( startingScope.getASTExtension().getPrimaryDeclaration() instanceof IASTCodeScope )
					{
						if( ((IASTCodeScope) startingScope.getASTExtension().getPrimaryDeclaration()).getContainingFunction() instanceof IASTMethod )
						{
							IASTClassSpecifier classSpecifier = ((IASTMethod) ((IASTCodeScope) startingScope.getASTExtension().getPrimaryDeclaration()).getContainingFunction()).getOwnerClassSpecifier();
							if( classSpecifier != null )
								((ASTClassSpecifier)classSpecifier).addUnresolvedReference( new UnresolvedReferenceDuple( startingScope, name ));
							break;
						}
					}
					if ( throwOnError )
						handleProblem( IProblem.SEMANTIC_NAME_NOT_FOUND, image, name.getStartOffset(), name.getEndOffset(), name.getLineNumber(), true );
					return null;
				}
                break;
			default:
			    IToken t = null;
				IToken last = name.getLastToken();
				result = startingScope;
				if( name.getFirstToken().getType() == IToken.tCOLONCOLON )
					result = pst.getCompilationUnit();
				
				for( ; ; )
				{
				    if( t == last)
				        break;
					t = ( t != null ) ? t.getNext() : name.getFirstToken();
					if( t.getType() == IToken.tCOLONCOLON ){
						idx++;
						continue;
					} else if( t.getType() == IToken.t_template ){
						continue;
 					}
					if( t.isPointer() ) break;
					
					image = t.getCharImage();
					int offset = t.getOffset();
					
					if( templateArgLists != null && templateArgLists[ idx ] != null ){
						if( t != last && t.getNext().getType() == IToken.tLT )
							t = TokenFactory.consumeTemplateIdArguments( t.getNext(), last );
					}
					
					try
					{
						if( result instanceof IDeferredTemplateInstance ){
							result = ((IDeferredTemplateInstance)result).getTemplate().getTemplatedSymbol();
						}
						args = ( templateArgLists != null ) ? getTemplateArgList( templateArgLists[ idx ] ) : null;
						if( t == last ) 
							result = lookupElement((IContainerSymbol)result, image, type, parameters, args, ( lookup == LookupType.FORDEFINITION ) ? lookup : LookupType.QUALIFIED );
						else
							if( templateArgLists != null && templateArgLists[idx] != null )
								result = ((IContainerSymbol)result).lookupTemplateId( image, args );
							else
								result = ((IContainerSymbol)result).lookupNestedNameSpecifier( image );
						
						if( result != null ){
		                	if( lookup == LookupType.FORPARENTSCOPE && startingScope instanceof ITemplateFactory ){
		                		if( templateArgLists != null && templateArgLists[idx] != null )
		                			((ITemplateFactory)startingScope).pushTemplateId( result, args );
		                		else 
		                			((ITemplateFactory)startingScope).pushSymbol( result );
		                	}
		                	if( references != null )
		                		addReference( references, createReference( result, image, offset ));
							if( references != null && templateArgLists != null && templateArgLists[idx] != null )
							{
								addTemplateIdReferences( references, templateArgLists[idx] );
								name.freeReferences();
							}
						}
						else
							break;
					}
					catch( ParserSymbolTableException pste )
					{
						if ( throwOnError )
							handleProblem( pste.createProblemID(), image );
						return null;
					}
					catch( ParserSymbolTableError e )
					{
						if( throwOnError )
							handleProblem( IProblem.INTERNAL_RELATED, image );
						return null;
					}
				}
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
        int startingLine, int endingOffset, int endingLine)
        throws ASTSemanticException
    {		
    	setFilename( duple );
		List references = new ArrayList();	
		ISymbol symbol = lookupQualifiedName( 
			scopeToSymbol( scope), duple, references, true ); 

		
		IUsingDirectiveSymbol usingDirective = null;
		if( symbol != null )
			try {
				usingDirective = ((ASTScope)scope).getContainerSymbol().addUsingDirective( (IContainerSymbol)symbol );
			} catch (ParserSymbolTableException pste) {
				handleProblem( pste.createProblemID(), duple.toCharArray(), startingOffset, endingOffset, startingLine, true );
			}
		
		ASTUsingDirective using = new ASTUsingDirective( scopeToSymbol(scope), usingDirective, startingOffset, startingLine, endingOffset, endingLine, references, filename, duple.getStartOffset(), duple.getEndOffset(), duple.getLineNumber() );
		attachSymbolExtension( usingDirective, using );
		
		return using;
    }
    

    /**
	 * @param duple
	 */
	private void setFilename(ITokenDuple duple) {
		filename = ( duple == null ) ? EMPTY_STRING : duple.getFilename();
	}

	protected IContainerSymbol getScopeToSearchUpon(
        IASTScope currentScope,
        IToken firstToken ) 
    {
		if( firstToken.getType() == IToken.tCOLONCOLON )  
		{ 
			return pst.getCompilationUnit();
		}
		return scopeToSymbol(currentScope);
    }
    protected IContainerSymbol scopeToSymbol(IASTScope currentScope)
    {
    	if( currentScope instanceof ASTScope )
        	return ((ASTScope)currentScope).getContainerSymbol();
    	else if ( currentScope instanceof ASTTemplateDeclaration )
    		return ((ASTTemplateDeclaration)currentScope).getContainerSymbol();
    	else if ( currentScope instanceof ASTTemplateInstantiation )
    		return ((ASTTemplateInstantiation)currentScope).getContainerSymbol();
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
        int startingLine, int endingOffset, int endingLine) throws ASTSemanticException
    {
    	setFilename( name );
        List references = new ArrayList(); 
        
        IUsingDeclarationSymbol endResult = null;
        
        if(name.getSegmentCount() > 1)
        {
        	ITokenDuple duple = name.getLeadingSegments();
        	IContainerSymbol containerSymbol = null;
        	if( duple == null ){
        		//null leading segment means globally qualified
        		containerSymbol = scopeToSymbol( scope ).getSymbolTable().getCompilationUnit();
        	} else {
	        	ISymbol symbol = lookupQualifiedName( scopeToSymbol(scope), duple, references, true );
	        	
	        	if( symbol instanceof IContainerSymbol )
	        		containerSymbol = (IContainerSymbol) symbol;
	        	else if ( symbol instanceof IDeferredTemplateInstance )
	        		containerSymbol = ((IDeferredTemplateInstance)symbol).getTemplate().getTemplatedSymbol();
        	}
	        try
	        {
	            endResult = scopeToSymbol(scope).addUsingDeclaration( name.getLastToken().getCharImage(), containerSymbol );
	        }
	        catch (ParserSymbolTableException e)
	        {
	        	handleProblem(e.createProblemID(), name.getLastToken().getCharImage(), startingOffset, endingOffset, startingLine, true );
	        }
        } else
			try {
				endResult = scopeToSymbol(scope).addUsingDeclaration(name.getLastToken().getCharImage());
			} catch (ParserSymbolTableException e) {
				handleProblem(e.createProblemID(), name.getLastToken().getCharImage(), startingOffset, endingOffset, startingLine, true );
	        }
        
			if( endResult != null )
			{
				List refs = endResult.getReferencedSymbols();
				int numRefs = refs.size();
				for( int i = 0; i < numRefs; i++ )
					addReference( references, createReference( (ISymbol) refs.get(i), name.getLastToken().getCharImage(), name.getLastToken().getOffset() ) );

			}
		ASTUsingDeclaration using = new ASTUsingDeclaration( scope, name.getLastToken().getCharImage(),
	        	endResult.getReferencedSymbols(), isTypeName, startingOffset, startingLine, endingOffset, endingLine, references, filename, name.getStartOffset(), name.getEndOffset(), name.getLineNumber() );
		attachSymbolExtension( endResult, using );
		
        return using; 
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createASMDefinition(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, int, int)
     */
    public IASTASMDefinition createASMDefinition(
        IASTScope scope,
        char[] assembly,
        int startingOffset,
        int startingLine, int endingOffset, int endingLine, char[] fn)
    {
        return new ASTASMDefinition( scopeToSymbol(scope), assembly, startingOffset, startingLine, endingOffset, endingLine, fn);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNamespaceDefinition(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, int, int)
     */
    public IASTNamespaceDefinition createNamespaceDefinition(
        IASTScope scope,
        char[] identifier,
        int startingOffset,
        int startingLine, int nameOffset, int nameEndOffset, int nameLineNumber, char[] fn) throws ASTSemanticException
    {
    	//TODO - set filename
    	IContainerSymbol pstScope = scopeToSymbol(scope);
    	ISymbol namespaceSymbol  = null; 
    	
    	if( ! identifier.equals( EMPTY_STRING ) ) 
    	{
	    	try
	        {
	            namespaceSymbol = pstScope.qualifiedLookup( identifier );
	        }
	        catch (ParserSymbolTableException e)
	        {
	        	handleProblem( e.createProblemID(), identifier, nameOffset, nameEndOffset, nameLineNumber, true );
	        }
    	}
        
        if( namespaceSymbol != null )
        {
        	if( namespaceSymbol.getType() != ITypeInfo.t_namespace )
        		handleProblem( IProblem.SEMANTIC_INVALID_OVERLOAD, identifier, nameOffset, nameEndOffset, nameLineNumber, true );
        }
        else
        {
        	namespaceSymbol = pst.newContainerSymbol( identifier, ITypeInfo.t_namespace );
        	if( identifier.equals( EMPTY_STRING ) ) 
        		namespaceSymbol.setContainingSymbol( pstScope );	
        	else
        	{
	        	
	        	try
	            {
	                pstScope.addSymbol( namespaceSymbol );
	            }
	            catch (ParserSymbolTableException e1)
	            {
//	            	assert false : e1;
	            }
        	}
        }
        
        ASTNamespaceDefinition namespaceDef = new ASTNamespaceDefinition( namespaceSymbol, startingOffset, startingLine, nameOffset, nameEndOffset, nameLineNumber, fn);
        attachSymbolExtension( namespaceSymbol, namespaceDef, true );
        return namespaceDef;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createCompilationUnit()
     */
    public IASTCompilationUnit createCompilationUnit()
    {
    	ISymbol symbol = pst.getCompilationUnit();
    	ASTCompilationUnit compilationUnit = new ASTCompilationUnit( symbol );
        attachSymbolExtension(symbol, compilationUnit, true );
    	return compilationUnit; 
    }
    
    protected void attachSymbolExtension( IExtensibleSymbol symbol, ASTNode astNode )
    {
    	ISymbolASTExtension symbolExtension = new ExtensibleSymbolExtension( symbol, astNode );
    	symbol.setASTExtension( symbolExtension );
    }
    
	protected void attachSymbolExtension(
		ISymbol symbol,
		ASTSymbol astSymbol, boolean asDefinition )
	{
		ISymbolASTExtension symbolExtension = symbol.getASTExtension();
		if( symbolExtension == null )
		{
			if( astSymbol instanceof IASTNamespaceDefinition ) 
				symbolExtension = new NamespaceSymbolExtension( symbol, astSymbol );
			else if( astSymbol instanceof IASTFunction || astSymbol instanceof IASTMethod || 
					astSymbol instanceof IASTEnumerationSpecifier || 
					astSymbol instanceof IASTClassSpecifier || 
					astSymbol instanceof IASTElaboratedTypeSpecifier )
			{
				symbolExtension = new ForewardDeclaredSymbolExtension( symbol, astSymbol );
			}
			else if( astSymbol instanceof IASTTemplateDeclaration ){
				symbolExtension = new TemplateSymbolExtension( symbol, astSymbol );
			}
			else 
			{
				symbolExtension = new StandardSymbolExtension( symbol, astSymbol );
			}
			symbol.setASTExtension( symbolExtension );
		}
		else
		{
			if( asDefinition )
				try {
					symbolExtension.addDefinition( astSymbol );
				} catch (ExtensionException e) {
//					assert false : ExtensionException.class;
				}
		}
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createLinkageSpecification(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, int)
     */
    public IASTLinkageSpecification createLinkageSpecification(
        IASTScope scope,
        char[] spec,
        int startingOffset, int startingLine, char[] fn)
    {
        return new ASTLinkageSpecification( scopeToSymbol( scope ), spec, startingOffset, startingLine, fn );
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
        int startingLine, int nameOffset, int nameEndOffset, int nameLine, char[] fn) throws ASTSemanticException
    {
    	setFilename( fn );
        IContainerSymbol currentScopeSymbol = scopeToSymbol(scope);
        ITypeInfo.eType pstType = classKindToTypeInfo(kind);		
		List references = new ArrayList();

		char[] newSymbolName = EMPTY_STRING; 
		List templateIdArgList = null;
		boolean isTemplateId = false;
		
		if( name != null ){
			IToken nameToken = null;
			if( name.getSegmentCount() != 1 ) // qualified name 
			{
				ITokenDuple containerSymbolName = name.getLeadingSegments(); 
				ISymbol temp = lookupQualifiedName( currentScopeSymbol, containerSymbolName, references, true);
				if( temp instanceof IDeferredTemplateInstance )
					currentScopeSymbol = ((IDeferredTemplateInstance)temp).getTemplate().getTemplatedSymbol();
				else
					currentScopeSymbol = (IContainerSymbol) temp;
				if( currentScopeSymbol == null )
				 	handleProblem( IProblem.SEMANTIC_NAME_NOT_FOUND, containerSymbolName.toCharArray(), containerSymbolName.getFirstToken().getOffset(), containerSymbolName.getLastToken().getEndOffset(), containerSymbolName.getLastToken().getLineNumber(), true );	
				 
				nameToken = name.getLastSegment().getFirstToken();
			} else {
				nameToken = name.getFirstToken();
			}
			//template-id
			List [] array = name.getTemplateIdArgLists();
			if( array != null ){
				templateIdArgList = array[ array.length - 1 ];
				isTemplateId = (templateIdArgList != null);
			}
				 
			newSymbolName = nameToken.getCharImage();
		}
		ISymbol classSymbol = null;
		if( !CharArrayUtils.equals(newSymbolName, EMPTY_STRING) && !isTemplateId ){ 
			try
			{
				classSymbol = currentScopeSymbol.lookupMemberForDefinition(newSymbolName);
			}
			catch (ParserSymbolTableException e)
			{
				handleProblem(IProblem.SEMANTIC_UNIQUE_NAME_PREDEFINED, name.toCharArray(), nameOffset, nameEndOffset, nameLine, true);
			}
	        
			if( classSymbol != null && ! classSymbol.isForwardDeclaration() )
				handleProblem( IProblem.SEMANTIC_UNIQUE_NAME_PREDEFINED, newSymbolName, nameOffset, nameEndOffset, nameLine, true );  
			
			
			if( classSymbol != null && classSymbol.getType() != pstType )
			{
				boolean isError = true;
				if( classSymbol.isType( ITypeInfo.t_class, ITypeInfo.t_union ) )
				{
					if ( ( pstType == ITypeInfo.t_class || pstType == ITypeInfo.t_struct || pstType == ITypeInfo.t_union ) )
						isError = false;
								
				}				
				handleProblem( IProblem.SEMANTIC_INVALID_OVERLOAD, newSymbolName, nameOffset, nameEndOffset, nameLine, isError );
			}
		}

		IDerivableContainerSymbol newSymbol = pst.newDerivableContainerSymbol( newSymbolName, pstType );
		
		if( classSymbol != null )
			classSymbol.setForwardSymbol( newSymbol );
			
		List args = null;
		if( isTemplateId ){
			args = getTemplateArgList( templateIdArgList );
		}
		try
        {
			if( !isTemplateId )
				currentScopeSymbol.addSymbol( newSymbol );
			else 
				currentScopeSymbol.addTemplateId( newSymbol, args );
        }
        catch (ParserSymbolTableException e2)
        {
        	handleProblem( e2.createProblemID(), newSymbolName );
        }
		
        if( name != null && name.getTemplateIdArgLists() != null  )
        {
        	for( int i = 0; i < name.getTemplateIdArgLists().length; ++i )
        		addTemplateIdReferences( references, name.getTemplateIdArgLists()[i]);
        	name.freeReferences( );
        }
        ASTClassSpecifier classSpecifier = new ASTClassSpecifier( newSymbol, kind, type, access, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, references, filename );
        attachSymbolExtension(newSymbol, classSpecifier, true );
        
        return classSpecifier;
    }
    
	private List getTemplateArgList( List args ){
		if( args == null )
			return null;
		
		int size = args.size();
		List list = new ArrayList( size );
		
		ASTExpression exp;
		for( int i = 0; i < size; i++ )
		{
			exp = (ASTExpression) args.get( i );
			
			ITypeInfo info = exp.getResultType().getResult();
			
			list.add( info );
		}
			
		return list;
	}
	
    protected void handleProblem( int id, char[] attribute ) throws ASTSemanticException
	{
    	handleProblem( null, id, attribute, -1, -1, -1, true );  //TODO make this right
    }

    protected void handleProblem( IASTScope scope, int id, char[] attribute ) throws ASTSemanticException
	{
    	handleProblem( scope, id, attribute, -1, -1, -1, true);
	}
    
    protected void handleProblem( int id, char[] attribute, int startOffset, int endOffset, int lineNumber, boolean isError ) throws ASTSemanticException {
    	handleProblem( null, id, attribute, startOffset, endOffset, lineNumber, isError );
    }
    
	/**
	 * @param id
	 * @param attribute
	 * @param startOffset
	 * @param endOffset
	 * @param lineNumber
	 * @param isError TODO
	 * @param filename
	 * @throws ASTSemanticException
	 */		
	protected void handleProblem( IASTScope scope, int id, char[] attribute, int startOffset, int endOffset, int lineNumber, boolean isError ) throws ASTSemanticException {			
		IProblem p = problemFactory.createProblem( id, 
				startOffset, endOffset, lineNumber, filename, attribute, !isError, isError );
		
		TraceUtil.outputTrace(logService, "CompleteParseASTFactory - IProblem : ", p ); //$NON-NLS-1$
		
		if( shouldThrowException( scope, id, !isError ) ) 
			throw new ASTSemanticException(p);
	}

	protected boolean shouldThrowException( IASTScope scope, int id, boolean isWarning ){
		if( isWarning ) return false;
		if( scope != null ){
			IContainerSymbol symbol = scopeToSymbol( scope );
			if( symbol.isTemplateMember() ){
				if( id == IProblem.SEMANTIC_INVALID_CONVERSION_TYPE ||
					id == IProblem.SEMANTIC_INVALID_TYPE )
				{
					return false;
				}
			}
		}
		return true;
	}
	protected ITypeInfo.eType classKindToTypeInfo(ASTClassKind kind)
    {
        ITypeInfo.eType pstType = null;
        
        if( kind == ASTClassKind.CLASS )
        	pstType = ITypeInfo.t_class;
        else if( kind == ASTClassKind.STRUCT )
        	pstType = ITypeInfo.t_struct;
        else if( kind == ASTClassKind.UNION )
        	pstType = ITypeInfo.t_union;
        else if( kind == ASTClassKind.ENUM )
            pstType = ITypeInfo.t_enumeration;
//        else
//        	assert false : kind ;
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
    	setFilename( parentClassName );
    	IDerivableContainerSymbol classSymbol = (IDerivableContainerSymbol)scopeToSymbol( astClassSpec);
        List references = new ArrayList(); 
        
        if( parentClassName == null || parentClassName.getFirstToken() == null )
           	handleProblem( IProblem.SEMANTIC_NAME_NOT_PROVIDED, null );
        	 
        //Its possible that the parent is not an IContainerSymbol if its a template parameter or some kinds of template instances
		ISymbol symbol = lookupQualifiedName( classSymbol, parentClassName, references, true );
		
		if( symbol instanceof ITemplateSymbol )
			handleProblem( IProblem.SEMANTIC_INVALID_TEMPLATE_ARGUMENT, parentClassName.toCharArray(), parentClassName.getStartOffset(), parentClassName.getEndOffset(), parentClassName.getLineNumber(), true);
			
		List [] templateArgumentLists = parentClassName.getTemplateIdArgLists();
		if( templateArgumentLists != null )
		{
			for( int i = 0; i < templateArgumentLists.length; ++i )
				addTemplateIdReferences( references, templateArgumentLists[i]);
		}
		parentClassName.freeReferences();
		classSymbol.addParent( symbol, isVirtual, visibility, parentClassName.getFirstToken().getOffset(), references );
		 
    }
    /**
     * @param symbol
     * @param referenceElementName
     * @return
     */
    protected IASTReference createReference(ISymbol symbol, char[] referenceElementName, int offset ) throws ASTSemanticException 
    {
    	if( mode != ParserMode.COMPLETE_PARSE )
    		return null;
		//referenced symbol doesn't have an attached AST node, could happen say for the copy constructor added 
		//by the symbol table.
    	if( symbol.getASTExtension() == null )
    		return null;
    	
    	
    	Iterator i = symbol.getASTExtension().getAllDefinitions();
    	ASTSymbol declaration = i.hasNext() ? (ASTSymbol) i.next() : null;
    	ASTSymbol definition  = i.hasNext() ? (ASTSymbol) i.next() : null;
    			
//    	assert (symbol != null ) : "createReference cannot be called on null symbol ";
    	if( symbol.getTypeInfo().checkBit( ITypeInfo.isTypedef ) ||
		    symbol.getASTExtension().getPrimaryDeclaration() instanceof IASTTypedefDeclaration )
			return new ASTTypedefReference ( offset, (IASTTypedefDeclaration) declaration );
        else if( symbol.getType() == ITypeInfo.t_namespace )
        	return new ASTNamespaceReference( offset, (IASTNamespaceDefinition) declaration);
        else if( symbol.getType() == ITypeInfo.t_class || 
				 symbol.getType() == ITypeInfo.t_struct || 
				 symbol.getType() == ITypeInfo.t_union ) 
			return new ASTClassReference( offset, (IASTTypeSpecifier)symbol.getASTExtension().getPrimaryDeclaration() );
		else if( symbol.getType() == ITypeInfo.t_enumeration )
			return new ASTEnumerationReference( offset, (IASTEnumerationSpecifier)symbol.getASTExtension().getPrimaryDeclaration() );
		else if( symbol.getType() == ITypeInfo.t_enumerator )
			return new ASTEnumeratorReference( offset, (IASTEnumerator) declaration );
		else if(( symbol.getType() == ITypeInfo.t_function ) || (symbol.getType() == ITypeInfo.t_constructor))
		{
			ASTNode referenced = (definition != null) ? definition : declaration;
			if( referenced instanceof IASTMethod )
			
				return new ASTMethodReference( offset, (IASTMethod)referenced ); 
			return new ASTFunctionReference( offset, (IASTFunction)referenced );
		}
		else if( ( symbol.getType() == ITypeInfo.t_type ) || 
				( symbol.getType() == ITypeInfo.t_bool )||
				( symbol.getType() == ITypeInfo.t_char  ) ||     
				( symbol.getType() == ITypeInfo.t_wchar_t )||
				( symbol.getType() == ITypeInfo.t_int )   ||
				( symbol.getType() == ITypeInfo.t_float )||
				( symbol.getType() == ITypeInfo.t_double ) ||    
				( symbol.getType() == ITypeInfo.t_void ) ||
				( symbol.getType() == ITypeInfo.t__Bool) ||
				( symbol.getType() == ITypeInfo.t_templateParameter ) )
			
		{
			if( symbol.getContainingSymbol().getType() == ITypeInfo.t_class || 
				symbol.getContainingSymbol().getType() == ITypeInfo.t_struct || 
				symbol.getContainingSymbol().getType() == ITypeInfo.t_union )
			{
				return new ASTFieldReference( offset, (IASTField) (definition != null ? definition : declaration ));
			}
			else if( ( 	symbol.getContainingSymbol().getType() == ITypeInfo.t_function || 
						symbol.getContainingSymbol().getType() == ITypeInfo.t_constructor ) && 
				symbol.getContainingSymbol() instanceof IParameterizedSymbol && 
				((IParameterizedSymbol)symbol.getContainingSymbol()).getParameterList() != null && 
				((IParameterizedSymbol)symbol.getContainingSymbol()).getParameterList().contains( symbol ) )
			{
				return new ASTParameterReference( offset, (IASTParameterDeclaration) declaration );
			}
			else
			{
				ASTNode s = (definition != null) ? definition : declaration;
				if(s instanceof IASTVariable)
					return new ASTVariableReference( offset, (IASTVariable)s);
				else if (s instanceof IASTParameterDeclaration)
					return new ASTParameterReference( offset, (IASTParameterDeclaration)s);
				else if (s instanceof IASTTemplateParameter )
					return new ASTTemplateParameterReference( offset, (IASTTemplateParameter)s );
			}
		}
//		assert false : "Unreachable code : createReference()";
		return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createEnumerationSpecifier(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, int, int)
     */
    public IASTEnumerationSpecifier createEnumerationSpecifier(
        IASTScope scope,
        char[] name,
        int startingOffset,
        int startingLine, int nameOffset, int nameEndOffset, int nameLine, char[] fn) throws ASTSemanticException
    {
    	setFilename(fn);
		IContainerSymbol containerSymbol = scopeToSymbol(scope);
		ITypeInfo.eType pstType = ITypeInfo.t_enumeration;
			
		IDerivableContainerSymbol classSymbol = pst.newDerivableContainerSymbol( name, pstType );
		try
		{
			containerSymbol.addSymbol( classSymbol );
		}
		catch (ParserSymbolTableException e)
		{
			handleProblem( e.createProblemID(), name );
		}
        
        ASTEnumerationSpecifier enumSpecifier = new ASTEnumerationSpecifier( classSymbol, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, fn  );
		
		attachSymbolExtension(classSymbol, enumSpecifier, true );
		return enumSpecifier;
    }
    /**
	 * @param fn
	 */
	private void setFilename(char[] fn) {
		filename = fn;
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#addEnumerator(org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier, java.lang.String, int, int, org.eclipse.cdt.core.parser.ast.IASTExpression)
     */
    public IASTEnumerator addEnumerator(
        IASTEnumerationSpecifier enumeration,
        char[] name,
        int startingOffset,
        int startingLine,
        int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endLine, IASTExpression initialValue, char[] fn) throws ASTSemanticException
    {
    	setFilename(fn);
        IContainerSymbol enumerationSymbol = (IContainerSymbol)((ISymbolOwner)enumeration).getSymbol();
        
        ISymbol enumeratorSymbol = pst.newSymbol( name, ITypeInfo.t_enumerator );
        try
        {
            enumerationSymbol.addSymbol( enumeratorSymbol );
        }
        catch (ParserSymbolTableException e1)
        {
        	if( e1.reason == ParserSymbolTableException.r_InvalidOverload )
        		handleProblem( IProblem.SEMANTIC_INVALID_OVERLOAD, name, startingOffset, endingOffset, startingLine, true );
//			assert false : e1;
        }
        ASTEnumerator enumerator = new ASTEnumerator( enumeratorSymbol, enumeration, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, endingOffset, endLine, initialValue, fn ); 
        ((ASTEnumerationSpecifier)enumeration).addEnumerator( enumerator );
        attachSymbolExtension( enumeratorSymbol, enumerator, true );
        return enumerator;
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
        IASTTypeId typeId,
        ITokenDuple idExpression, char[] literal, IASTNewExpressionDescriptor newDescriptor) throws ASTSemanticException
    {
    	setFilename( idExpression );
    	if( idExpression != null && logService.isTracing() )
    	{
    		TraceUtil.outputTrace(
    				logService,
    				"Entering createExpression with Kind=", //$NON-NLS-1$
					null,
					kind.getKindName(),
					" idexpression=", //$NON-NLS-1$
					idExpression.toString()
    				);
    	}
    	else if( literal != null && literal.length > 0 && logService.isTracing() ) 
    	{
       		TraceUtil.outputTrace(
       				logService,
    				"Entering createExpression with Kind=", //$NON-NLS-1$
					null,
					kind.getKindName(),
					" literal=", //$NON-NLS-1$
					String.valueOf(literal)
    				);
    	}
    	
    	List references = new ArrayList(); 
		ISymbol symbol = getExpressionSymbol(scope, kind, lhs, rhs, idExpression, references );
        
        // Try to figure out the result that this expression evaluates to
		ExpressionResult expressionResult = getExpressionResultType(scope, kind, lhs, rhs, thirdExpression, typeId, literal, symbol);

		if( newDescriptor != null ){
			createConstructorReference( newDescriptor, typeId, references );
		}
		
		if( symbol == null )
			purgeBadReferences( kind, rhs );
		
		
		// expression results could be empty, but should not be null
//		assert expressionResult != null  : expressionResult; //throw new ASTSemanticException();
			
		// create the ASTExpression
		ASTExpression expression = null;
		if( extension.overrideCreateExpressionMethod() )
			expression = (ASTExpression) extension.createExpression( scope, kind, lhs, rhs, thirdExpression, typeId, idExpression, literal, newDescriptor, references );
		else 
			expression =  ExpressionFactory.createExpression( kind, lhs, rhs, thirdExpression, typeId, idExpression, literal, newDescriptor, references );
		// Assign the result to the created expression										
		expression.setResultType (expressionResult);

		return expression;			
    }
    
	private void createConstructorReference( IASTNewExpressionDescriptor descriptor, IASTTypeId typeId, List references ){
		ISymbol symbol = null;
		try {
			symbol = typeId.getTypeSymbol();
			
			if( symbol.isType( ITypeInfo.t_type ) )
				symbol = symbol.getTypeSymbol();
		} catch (ASTNotImplementedException e) {
			return;
		}
		if( symbol == null || !( symbol instanceof IDerivableContainerSymbol ) )
			return;
		
		List initializers = ((ASTNewDescriptor)descriptor).getNewInitializerExpressionsList();
		
		ASTExpression exp = ( initializers.size() > 0 ) ? (ASTExpression) initializers.get(0) : null;
		
		ITokenDuple duple = ((ASTTypeId)typeId).getTokenDuple().getLastSegment();
		
		if( createConstructorReference( symbol, exp, duple, references ) ){
			//if we have a constructor reference, get rid of the class reference.
			List refs = ((ASTTypeId)typeId).getReferences();
			int size = refs.size();
			for( int i = 0; i < size; i++ )
			{
				ASTReference ref = (ASTReference) refs.get(i);
				if( CharArrayUtils.equals( ref.getNameCharArray(), duple.toCharArray() ) &&
					ref.getOffset() == duple.getStartOffset() )
				{
					refs.remove( i-- );
					size--;
				}
			}
		}
	}
	
	private boolean createConstructorReference( ISymbol classSymbol, ASTExpression expressionList, ITokenDuple duple, List references ){
		if( classSymbol != null && classSymbol.getTypeInfo().checkBit( ITypeInfo.isTypedef ) ){
			TypeInfoProvider provider = pst.getTypeInfoProvider();
			ITypeInfo info = classSymbol.getTypeInfo().getFinalType( provider );
			classSymbol = info.getTypeSymbol();
			provider.returnTypeInfo( info );
		}
		if( classSymbol == null || ! (classSymbol instanceof IDerivableContainerSymbol ) ){
			return false;
		}
		
		List parameters = new ArrayList();
		while( expressionList != null ){
			parameters.add( expressionList.getResultType().getResult() );
			expressionList = (ASTExpression) expressionList.getRHSExpression();
		}
		
		IParameterizedSymbol constructor = null;
		try {
			constructor = ((IDerivableContainerSymbol)classSymbol).lookupConstructor( parameters );
		} catch (ParserSymbolTableException e1) {
			return false;
		}
		
		if( constructor != null ){
			IASTReference reference = null;
			try {
				reference = createReference( constructor, duple.toCharArray(), duple.getStartOffset() );
			} catch (ASTSemanticException e2) {
				return false;
			}
			if( reference != null ){
				addReference( references, reference );
				return true;
			}
		}
		return false;
	}
    /**
	 * @param kind
     * @param rhs
	 */
	private void purgeBadReferences(Kind kind, IASTExpression rhs) {
		if( rhs == null ) return;
		if( kind == Kind.POSTFIX_ARROW_IDEXPRESSION || kind == Kind.POSTFIX_ARROW_TEMPL_IDEXP ||
			kind == Kind.POSTFIX_DOT_IDEXPRESSION || kind == Kind.POSTFIX_DOT_TEMPL_IDEXPRESS )
		{
			ASTExpression astExpression = (ASTExpression) rhs;
			char[] idExpression = astExpression.getIdExpressionCharArray();
			if( idExpression.length > 0 ) //$NON-NLS-1$
			{
				List refs = astExpression.getReferences();
				int size = refs.size();
				for( int i = 0; i < size; i++ )
				{
					IASTReference r = (IASTReference) refs.get(i);
					if( CharArrayUtils.equals( r.getNameCharArray(), idExpression ) )
					{
						refs.remove(i--);
						size--;
					}
				}
			}
		}
		
	}

	/*
     * Try and dereference the symbol in the expression
     */
    private ISymbol getExpressionSymbol(
		    IASTScope scope, 
		    Kind kind, 
		    IASTExpression lhs,
			IASTExpression rhs,
			ITokenDuple idExpression, 
			List references )throws ASTSemanticException
	{
    	ISymbol symbol = null;
		IContainerSymbol startingScope = scopeToSymbol( scope );
	    	    	
		//If the expression has an id, look up id and add it to references	
		if( idExpression != null )
			symbol = lookupQualifiedName( startingScope, idExpression, references, false );
	        
		// If the expression is lookup symbol if it is in the scope of a type after a "." or an "->"
		IContainerSymbol searchScope = getSearchScope(kind, lhs, startingScope);
		if ( searchScope != null && !searchScope.equals(startingScope))
			symbol = lookupQualifiedName(searchScope, ((ASTIdExpression)rhs).getIdExpressionTokenDuple(), references, false, LookupType.QUALIFIED );
			    			
		// get symbol if it is the "this" pointer
		// go up the scope until you hit a class
		if (kind == IASTExpression.Kind.PRIMARY_THIS){
			try{
				symbol = startingScope.lookup( THIS ); //$NON-NLS-1$
			}catch (ParserSymbolTableException e){
				handleProblem( e.createProblemID(), THIS ); //$NON-NLS-1$
			}
		}
		// lookup symbol if it is a function call
		if (kind == IASTExpression.Kind.POSTFIX_FUNCTIONCALL){ 
			ITokenDuple functionId =  getFunctionId(lhs); 
			IContainerSymbol functionScope = getSearchScope(lhs.getExpressionKind(), lhs.getLHSExpression(), startingScope);    

			if( functionScope == null )
				return null;
			
			ExpressionResult expResult = ((ASTExpression)rhs).getResultType();
			List parameters = null;
			if(expResult instanceof ExpressionResultList){
				ExpressionResultList expResultList = (ExpressionResultList) expResult;
				parameters = expResultList.getResultList();
								 
			}else {
				parameters = new ArrayList(1);
				parameters.add(expResult.getResult());
			}
			if( functionScope.equals( startingScope ) )
				symbol = lookupQualifiedName(functionScope, functionId, ITypeInfo.t_function, parameters, references, false);
			else
				symbol = lookupQualifiedName(functionScope, functionId, ITypeInfo.t_function, parameters, references, false, LookupType.QUALIFIED );
		}
		
    	return symbol;
    }
    /*
     * Returns the function ID token
     */
	private ITokenDuple getFunctionId (IASTExpression expression){
		if(expression.getExpressionKind().isPostfixMemberReference() && expression.getRHSExpression() instanceof ASTIdExpression ) 
			return ((ASTIdExpression)expression.getRHSExpression()).getIdExpressionTokenDuple();
		else if( expression instanceof ASTIdExpression ) 
			return ((ASTIdExpression)expression).getIdExpressionTokenDuple();
		return null;
	}

    private IContainerSymbol getSearchScope (Kind kind, IASTExpression lhs, IContainerSymbol startingScope) throws ASTSemanticException{
		if( kind.isPostfixMemberReference() )
		{
			ITypeInfo lhsInfo = ((ASTExpression)lhs).getResultType().getResult();
			if(lhsInfo != null){
				TypeInfoProvider provider = pst.getTypeInfoProvider();
				ITypeInfo info = null;
				try{
					info = lhsInfo.getFinalType( provider );
				} catch ( ParserSymbolTableError e ){
					return null;
				}
				ISymbol containingScope = info.getTypeSymbol();
				provider.returnTypeInfo( info );
//				assert containingScope != null : "Malformed Expression";	
				if( containingScope instanceof IDeferredTemplateInstance )
					return ((IDeferredTemplateInstance) containingScope).getTemplate().getTemplatedSymbol();
				return ( containingScope instanceof IContainerSymbol ) ? (IContainerSymbol)containingScope : null;
			} 
//			assert lhsInfo != null : "Malformed Expression";
			return null;
		}
		return startingScope;		
    }
    	
    /*
     * Conditional Expression conversion
     */
     protected ITypeInfo conditionalExpressionConversions(ITypeInfo second, ITypeInfo third){
     	ITypeInfo info = null;
     	if(second.equals(third)){
     		info = second;
     		return info;
     	}
     	if((second.getType() == ITypeInfo.t_void) && (third.getType() != ITypeInfo.t_void)){
     		info = third;
     		return info;	
     	}
		if((second.getType() != ITypeInfo.t_void) && (third.getType() == ITypeInfo.t_void)){
			info = second;
			return info;	
		}
		if((second.getType() == ITypeInfo.t_void) && (third.getType() == ITypeInfo.t_void)){
			info = second;
			return info;	
		}
		try{
	     	info = pst.getConditionalOperand(second, third);
	     	return info;
		} catch(ParserSymbolTableException e){
			// empty info
			return info;
		}
     }
	/*
	 * Apply the usual arithmetic conversions to find out the result of an expression 
	 * that has a lhs and a rhs as indicated in the specs (section 5.Expressions, page 64)
	 */
	protected ITypeInfo usualArithmeticConversions( IASTScope scope, ITypeInfo lhs, ITypeInfo rhs) throws ASTSemanticException{
		if( lhs == null ) return null;
		if( rhs == null ) return null;
		// if you have a variable of type basic type, then we need to go to the basic type first
		while( (lhs.getType() == ITypeInfo.t_type) && (lhs.getTypeSymbol() != null)){
			lhs = lhs.getTypeSymbol().getTypeInfo();  
		}
		while( (rhs.getType() == ITypeInfo.t_type) && (rhs.getTypeSymbol() != null)){
			rhs = rhs.getTypeSymbol().getTypeInfo();  
		}
		
		if( !lhs.isType(ITypeInfo.t__Bool, ITypeInfo.t_enumerator ) && 
			!rhs.isType(ITypeInfo.t__Bool, ITypeInfo.t_enumerator ) ) 
		{
			handleProblem( scope, IProblem.SEMANTIC_INVALID_CONVERSION_TYPE, null ); 
		}

		ITypeInfo info = TypeInfoProvider.newTypeInfo( );
		if( 
		   ( lhs.checkBit(ITypeInfo.isLong)  && lhs.getType() == ITypeInfo.t_double)
		|| ( rhs.checkBit(ITypeInfo.isLong)  && rhs.getType() == ITypeInfo.t_double)
		){
			info.setType(ITypeInfo.t_double);
			info.setBit(true, ITypeInfo.isLong);		
			return info; 
		}
		else if(
		   ( lhs.getType() == ITypeInfo.t_double )
		|| ( rhs.getType() == ITypeInfo.t_double )
		){
			info.setType(ITypeInfo.t_double);
			return info; 			
		}
		else if (
		   ( lhs.getType() == ITypeInfo.t_float )
		|| ( rhs.getType() == ITypeInfo.t_float )
		){
			info.setType(ITypeInfo.t_float);
			return info; 						
		} else {
			// perform intergral promotions (Specs section 4.5)
			info.setType(ITypeInfo.t_int);
		}
		
		if(
		   ( lhs.checkBit(ITypeInfo.isUnsigned) && lhs.checkBit(ITypeInfo.isLong)) 
		|| ( rhs.checkBit(ITypeInfo.isUnsigned) && rhs.checkBit(ITypeInfo.isLong))
		){
			info.setBit(true, ITypeInfo.isUnsigned);
			info.setBit(true, ITypeInfo.isLong);
			return info;
		} 
		else if(
			( lhs.checkBit(ITypeInfo.isUnsigned) && rhs.checkBit(ITypeInfo.isLong) ) 
		 || ( rhs.checkBit(ITypeInfo.isUnsigned) && lhs.checkBit(ITypeInfo.isLong) )
		){
			info.setBit(true, ITypeInfo.isUnsigned);
			info.setBit(true, ITypeInfo.isLong);
			return info;
		}
		else if (		
			( lhs.checkBit(ITypeInfo.isLong)) 
		 || ( rhs.checkBit(ITypeInfo.isLong))
		){
			info.setBit(true, ITypeInfo.isLong);
			return info;			
		}
		else if (
			( lhs.checkBit(ITypeInfo.isUnsigned) ) 
		 || ( rhs.checkBit(ITypeInfo.isUnsigned) )		
		){
			info.setBit(true, ITypeInfo.isUnsigned);
			return info;			
		} else {
			// it should be both = int
			return info;
		}		
	}
		
	private ITypeInfo modifyTypeInfo(ASTExpression exp, Kind kind)
	{
//		assert exp != null : exp;
		ITypeInfo info = exp.getResultType().getResult();
		
		//short added to a type
		if (kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_SHORT ){
			info.setBit( true, ITypeInfo.isShort);
		}
		// long added to a type
		if (kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_LONG ){
		    info.setBit( true, ITypeInfo.isLong);
		}
		// signed added to a type
		if (kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_SIGNED ){
		    info.setBit( true, ITypeInfo.isUnsigned);
		}
		// unsigned added to a type
		if (kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_UNSIGNED ){
		    info.setBit( true, ITypeInfo.isUnsigned);
		}
		
		return info;
	}
	
	private void constructBasicType( TypeInfoProvider provider, Kind kind ){
		// types that resolve to void
		if ((kind == IASTExpression.Kind.PRIMARY_EMPTY)
		|| (kind == IASTExpression.Kind.THROWEXPRESSION) 
		|| (kind == IASTExpression.Kind.POSTFIX_DOT_DESTRUCTOR) 
		|| (kind == IASTExpression.Kind.POSTFIX_ARROW_DESTRUCTOR)
		|| (kind == IASTExpression.Kind.DELETE_CASTEXPRESSION)
		|| (kind == IASTExpression.Kind.DELETE_VECTORCASTEXPRESSION)
		){
			provider.setType( ITypeInfo.t_void );
			return;
		}
		// types that resolve to int
		if ((kind == IASTExpression.Kind.PRIMARY_INTEGER_LITERAL)
		|| (kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_INT)
		){
		    provider.setType( ITypeInfo.t_int );
			return;
		}
		
		// size of is always unsigned int
		if((kind == IASTExpression.Kind.UNARY_SIZEOF_TYPEID) 		
		|| (kind == IASTExpression.Kind.UNARY_SIZEOF_UNARYEXPRESSION) 		
		){
		    provider.setType( ITypeInfo.t_int );
		    provider.setTypeBits( ITypeInfo.isUnsigned );
			return;
		}
		
		// types that resolve to char
		if((kind == IASTExpression.Kind.PRIMARY_CHAR_LITERAL)
		|| (kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_CHAR)
		|| (kind == IASTExpression.Kind.PRIMARY_STRING_LITERAL)
		){
		    provider.setType( ITypeInfo.t_char );
			return;
		}	
		// types that resolve to float
		if((kind == IASTExpression.Kind.PRIMARY_FLOAT_LITERAL)
		|| (kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_FLOAT)){
		    provider.setType( ITypeInfo.t_float );
			return;
		}
		//types that resolve to double
		if( kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_DOUBLE){
		    provider.setType( ITypeInfo.t_double );
			return;		
		}	
		//types that resolve to wchar
		if(kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_WCHART){
		    provider.setType( ITypeInfo.t_wchar_t );
			return;
		}		
		// types that resolve to bool
		if((kind == IASTExpression.Kind.PRIMARY_BOOLEAN_LITERAL)
		|| (kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_BOOL)
		|| (kind == IASTExpression.Kind.RELATIONAL_GREATERTHAN)
		|| (kind == IASTExpression.Kind.RELATIONAL_GREATERTHANEQUALTO)
		|| (kind == IASTExpression.Kind.RELATIONAL_LESSTHAN)
		|| (kind == IASTExpression.Kind.RELATIONAL_LESSTHANEQUALTO) 
		|| (kind == IASTExpression.Kind.EQUALITY_EQUALS) 
		|| (kind == IASTExpression.Kind.EQUALITY_NOTEQUALS) 
		|| (kind == IASTExpression.Kind.LOGICALANDEXPRESSION) 
		|| (kind == IASTExpression.Kind.LOGICALOREXPRESSION) 				
		)
		{
		    provider.setType(ITypeInfo.t_bool);
			return;
		}
	}
	protected ExpressionResult getExpressionResultType(
			IASTScope scope, 
			Kind kind, IASTExpression lhs,
			IASTExpression rhs,
			IASTExpression thirdExpression,
			IASTTypeId typeId,
			char[] literal,
			ISymbol symbol)	throws ASTSemanticException
	{
	    ITypeInfo info = null;
	    ExpressionResult result = null;
	    
		if( extension.canHandleExpressionKind( kind ))
		{
			info = extension.getExpressionResultType( kind, lhs, rhs, typeId );
			return new ExpressionResult( info );
		}
		
		//basic types
		if( kind.isBasicType() ){
			TypeInfoProvider provider = pst.getTypeInfoProvider();
			provider.beginTypeConstruction();
			
			if( literal != null && !literal.equals(EMPTY_STRING) && kind.isLiteral() ){ 
				provider.setDefaultObj( literal );
			}
			
			constructBasicType( provider, kind );
			
			info = provider.completeConstruction();
			
			//types that need a pointer
			
			if( kind == IASTExpression.Kind.PRIMARY_STRING_LITERAL ||
			  (literal.length > 3 && ( kind == IASTExpression.Kind.PRIMARY_CHAR_LITERAL || 
				                             kind == IASTExpression.Kind.POSTFIX_SIMPLETYPE_CHAR )) )
			{
				info.addPtrOperator(new ITypeInfo.PtrOp(ITypeInfo.PtrOp.t_pointer));					
			}
			
			return new ExpressionResult( info );
		}

		// modifications to existing types		
		if( kind.isPostfixSimpleType() ){
		    info = modifyTypeInfo( (ASTExpression)lhs, kind );
		    return new ExpressionResult( info );
		}
		
		// Id expressions resolve to t_type, symbol already looked up
		if( kind == IASTExpression.Kind.ID_EXPRESSION )
		{
		    info = TypeInfoProvider.newTypeInfo(ITypeInfo.t_type);
			info.setTypeSymbol(symbol);								
			result = new ExpressionResult(info);
			if (symbol == null)
				result.setFailedToDereference(true);
			return result;
		}
		
		// an ampersand implies a pointer operation of type reference
		if (kind == IASTExpression.Kind.UNARY_AMPSND_CASTEXPRESSION){
			ASTExpression left =(ASTExpression)lhs;
			if(left == null)
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			
			info = left.getResultType().getResult();
			if (info != null){
				info = info.getFinalType( null );
				info.applyOperatorExpression( ITypeInfo.OperatorExpression.addressof );
			} else 
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			
			result = new ExpressionResult(info);
			return result;
		}
		
		// a star implies a pointer operation of type pointer
		if (kind == IASTExpression.Kind.UNARY_STAR_CASTEXPRESSION){
			ASTExpression left =(ASTExpression)lhs;
			if(left == null)
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null ); 
			info = left.getResultType().getResult();
			if (info != null){
				info = info.getFinalType( null );
				info.applyOperatorExpression( ITypeInfo.OperatorExpression.indirection );
			}else 
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			
			result = new ExpressionResult(info);
			return result;
		}
		// subscript
		if (kind == IASTExpression.Kind.POSTFIX_SUBSCRIPT){
			ASTExpression left =(ASTExpression)lhs;
			if(left == null)
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			info = left.getResultType().getResult();
			if ((info != null))
			{
				info = info.getFinalType( null );
				info.applyOperatorExpression( ITypeInfo.OperatorExpression.subscript );
			}else {
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			}
			result = new ExpressionResult(info);
			return result;
		}
		// the dot and the arrow resolves to the type of the member
		if ((kind == IASTExpression.Kind.POSTFIX_DOT_IDEXPRESSION)
		|| (kind == IASTExpression.Kind.POSTFIX_ARROW_IDEXPRESSION)
		|| (kind == IASTExpression.Kind.POSTFIX_DOT_TEMPL_IDEXPRESS)
		|| (kind == IASTExpression.Kind.POSTFIX_ARROW_TEMPL_IDEXP)
		){
			if(symbol != null){
				info = TypeInfoProvider.newTypeInfo( symbol.getTypeInfo() );			
			}
			result = new ExpressionResult(info);
			return result;
		}
		// the dot* and the arrow* are the same as dot/arrow + unary star
		if ((kind == IASTExpression.Kind.PM_DOTSTAR)
		|| (kind == IASTExpression.Kind.PM_ARROWSTAR)
		){
			ASTExpression right =(ASTExpression)rhs;
			if (right == null)
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			info = right.getResultType().getResult();
			if ((info != null) && (symbol != null)){
				info.setTypeSymbol(symbol);
				info = info.getFinalType( null );
				info.applyOperatorExpression( ITypeInfo.OperatorExpression.indirection );
				
			} else 
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			
			result = new ExpressionResult(info);
			return result;
		}
		// this
		if (kind == IASTExpression.Kind.PRIMARY_THIS){
			if(symbol != null)
			{
			    info = TypeInfoProvider.newTypeInfo(ITypeInfo.t_type);
				info.setTypeSymbol(symbol);	
			} else 
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			
			result = new ExpressionResult(info);
			return result;
		}
		
		// conditional
		if (kind == IASTExpression.Kind.CONDITIONALEXPRESSION){
			ASTExpression right = (ASTExpression)rhs;  
			ASTExpression third = (ASTExpression)thirdExpression;
			if((right != null ) && (third != null)){
				ITypeInfo rightType =right.getResultType().getResult();
				ITypeInfo thirdType =third.getResultType().getResult();
				if((rightType != null) && (thirdType != null)){
					info = conditionalExpressionConversions(rightType, thirdType);   
				} else 
					handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
				
			} else 
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			
			result = new ExpressionResult(info);
			return result;
		}		
		// new 
		if( ( kind == IASTExpression.Kind.NEW_TYPEID )
		|| ( kind == IASTExpression.Kind.NEW_NEWTYPEID ) )
		{
			try
            {
                info = typeId.getTypeSymbol().getTypeInfo();
				info.addPtrOperator( new ITypeInfo.PtrOp(ITypeInfo.PtrOp.t_pointer));
            }
            catch (ASTNotImplementedException e)
            {
            	// will never happen
            }
			result = new ExpressionResult(info);
			return result;
		}
		// types that use the usual arithmetic conversions
		if((kind == IASTExpression.Kind.MULTIPLICATIVE_MULTIPLY) 
		|| (kind == IASTExpression.Kind.MULTIPLICATIVE_DIVIDE) 
		|| (kind == IASTExpression.Kind.MULTIPLICATIVE_MODULUS) 
		|| (kind == IASTExpression.Kind.ADDITIVE_PLUS) 
		|| (kind == IASTExpression.Kind.ADDITIVE_MINUS) 
		|| (kind == IASTExpression.Kind.ANDEXPRESSION) 
		|| (kind == IASTExpression.Kind.EXCLUSIVEOREXPRESSION)
		|| (kind == IASTExpression.Kind.INCLUSIVEOREXPRESSION)
		){
			ASTExpression left = (ASTExpression)lhs;
			ASTExpression right = (ASTExpression)rhs;  
			if((left != null ) && (right != null)){
				ITypeInfo leftType =left.getResultType().getResult();
				ITypeInfo rightType =right.getResultType().getResult();
				info = usualArithmeticConversions( scope, leftType, rightType);
			}
			else 
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null ); 
			
			result = new ExpressionResult(info);
			return result;
		}
		
		// types that resolve to LHS types 
		if ((kind == IASTExpression.Kind.PRIMARY_BRACKETED_EXPRESSION)
		|| (kind == IASTExpression.Kind.POSTFIX_INCREMENT) 
		|| (kind == IASTExpression.Kind.POSTFIX_DECREMENT)
		|| (kind == IASTExpression.Kind.POSTFIX_TYPEID_EXPRESSION)		 
		|| (kind == IASTExpression.Kind.UNARY_INCREMENT) 
		|| (kind == IASTExpression.Kind.UNARY_DECREMENT) 
		|| (kind == IASTExpression.Kind.UNARY_PLUS_CASTEXPRESSION) 
		|| (kind == IASTExpression.Kind.UNARY_MINUS_CASTEXPRESSION) 
		|| (kind == IASTExpression.Kind.UNARY_NOT_CASTEXPRESSION)
		|| (kind == IASTExpression.Kind.UNARY_TILDE_CASTEXPRESSION) 
		|| (kind == IASTExpression.Kind.SHIFT_LEFT) 
		|| (kind == IASTExpression.Kind.SHIFT_RIGHT) 
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_NORMAL) 
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_PLUS) 
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_MINUS)
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_MULT)
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_DIV)
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_MOD)
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_LSHIFT)
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_RSHIFT)
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_AND)
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_OR)
		|| (kind == IASTExpression.Kind.ASSIGNMENTEXPRESSION_XOR) 
		){
			ASTExpression left = (ASTExpression)lhs;  
			if(left != null){
				info =left.getResultType().getResult();   
			} else 
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			
			result = new ExpressionResult(info);
			return result;
		}		
		// the cast changes the types to the type looked up in typeId = symbol
		if(( kind == IASTExpression.Kind.CASTEXPRESSION )
		|| ( kind == IASTExpression.Kind.POSTFIX_DYNAMIC_CAST )
		|| ( kind == IASTExpression.Kind.POSTFIX_STATIC_CAST )
		|| ( kind == IASTExpression.Kind.POSTFIX_REINTERPRET_CAST )
		|| ( kind == IASTExpression.Kind.POSTFIX_CONST_CAST )		
		){
			try{
				info = TypeInfoProvider.newTypeInfo(typeId.getTypeSymbol().getTypeInfo()); 
			}catch (ASTNotImplementedException e)
			{
				// will never happen
			}
			result = new ExpressionResult(info);
			return result;
		}				
		// a list collects all types of left and right hand sides
		if(kind == IASTExpression.Kind.EXPRESSIONLIST){
			result = new ExpressionResultList();
			if(lhs != null){
			    ExpressionResult resultType = ((ASTExpression)lhs).getResultType();
			    if( resultType instanceof ExpressionResultList )
			        ((ExpressionResultList)result).setResult( (ExpressionResultList) resultType );
			    else
			        result.setResult( resultType.getResult() );	
			}
			if(rhs != null){
			    ITypeInfo rightType = ((ASTExpression)rhs).getResultType().getResult();
				result.setResult(rightType);
			}
			return result;			
		}
		// a function call type is the return type of the function
		if(kind == IASTExpression.Kind.POSTFIX_FUNCTIONCALL){
			if(symbol != null){
				IParameterizedSymbol psymbol = (IParameterizedSymbol) symbol;
				ISymbol returnTypeSymbol = psymbol.getReturnType();
				if(returnTypeSymbol != null){
				    info = TypeInfoProvider.newTypeInfo(returnTypeSymbol.getTypeInfo());  
					//info.setTypeSymbol(returnTypeSymbol);
				}else {
					// this is call to a constructor
				}
			} else {
			    info = TypeInfoProvider.newTypeInfo();
			}
			result = new ExpressionResult(info);
			if(symbol == null)
				result.setFailedToDereference(true);
			return result;
		}
		// typeid
		if( kind == IASTExpression.Kind.POSTFIX_TYPEID_TYPEID )
		{
			try
            {
                info = typeId.getTypeSymbol().getTypeInfo();
            }
            catch (ASTNotImplementedException e)
            {
            	// will not ever happen from within CompleteParseASTFactory
            }
			result = new ExpressionResult(info);
			return result;
		}
		// typename
		if ( ( kind == IASTExpression.Kind.POSTFIX_TYPENAME_IDENTIFIER )
		|| ( kind == IASTExpression.Kind.POSTFIX_TYPENAME_TEMPLATEID ) )
		{
			if(symbol != null){
			    info = TypeInfoProvider.newTypeInfo(ITypeInfo.t_type);
				info.setTypeSymbol(symbol);
			} else 
				handleProblem( scope, IProblem.SEMANTIC_MALFORMED_EXPRESSION, null );
			
			result = new ExpressionResult(info);
			return result;
		}
//		assert false : this;
		return null;						
	}

    protected void getExpressionReferences(IASTExpression expression, List references)
    {
        if( expression != null )
        {
        	List eRefs = ((ASTExpression)expression).getReferences();
        	if( eRefs != null && !eRefs.isEmpty())
        	{
        		for( int i = 0; i < eRefs.size(); ++i )
        		{
        			IASTReference r = (IASTReference)eRefs.get(i);
        			references.add( r );
        		}
        	}
        	if( expression.getLHSExpression() != null )
        		getExpressionReferences( expression.getLHSExpression(), references );
        	if( expression.getRHSExpression() != null )
        		getExpressionReferences( expression.getRHSExpression(), references );
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
    	List newTypeIds = new ArrayList(); 
        if( typeIds != null )
        {
            int size = typeIds.size();
        	for( int i = 0; i < size; i++ )
        		newTypeIds.add( ((IASTTypeId)typeIds.get(i)).toString() );
        	
        }
        return new ASTExceptionSpecification( newTypeIds );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createConstructorMemberInitializer(org.eclipse.cdt.core.parser.ITokenDuple, org.eclipse.cdt.core.parser.ast.IASTExpression)
     */
    public IASTConstructorMemberInitializer createConstructorMemberInitializer(
        IASTScope scope,
        ITokenDuple duple, IASTExpression expressionList)
    {
    	setFilename( duple );
        List references = new ArrayList(); 
        
        IContainerSymbol scopeSymbol = scopeToSymbol(scope);
        
		boolean requireReferenceResolution = false;
		ISymbol symbol = null;
        if( duple != null )
        {
        	try
        	{
        		symbol = lookupQualifiedName( scopeSymbol, duple, references, true );
        	} catch( ASTSemanticException ase )
        	{
        	    //use the class specifier's unresolved reference mechanism to resolve these references.
        	    //TODO: resolve unresolved references in the expressionList using resolveLeftoverConstructorInitializerMembers
        	    if( scope instanceof ASTClassSpecifier ){
        	        ASTClassSpecifier classSpecifier = (ASTClassSpecifier) scope;
        	        classSpecifier.addUnresolvedReference( new UnresolvedReferenceDuple(scopeSymbol, duple ) );
        	    } 
        	}
        }
        
        if( symbol != null ){
        	createConstructorReference( symbol, (ASTExpression) expressionList, duple, references );
        }
        
        getExpressionReferences( expressionList, references ); 
        return new ASTConstructorMemberInitializer( 
        	expressionList, 
        	duple == null ? EMPTY_STRING : duple.toCharArray(), 
			duple == null ? 0 : duple.getFirstToken().getOffset(),
        	references, requireReferenceResolution );
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
        boolean isUnsigned, 
		boolean isTypename, 
		boolean isComplex, 
		boolean isImaginary,
		boolean isGlobal, Map extensionParms ) throws ASTSemanticException
    {
    	setFilename( typeName );
    	if( extension.overrideCreateSimpleTypeSpecifierMethod( kind ))
    		return extension.createSimpleTypeSpecifier(pst, scope, kind, typeName, isShort, isLong, isSigned, isUnsigned, isTypename, isComplex, isImaginary, isGlobal, extensionParms );
    	char[] typeNameAsString = typeName.toCharArray();
    	if( kind != Type.CLASS_OR_TYPENAME )
    	{
    		IASTSimpleTypeSpecifier query = (IASTSimpleTypeSpecifier) simpleTypeSpecCache.get( typeNameAsString );
    		if( query != null )
    			return query;
    	}
    	
    	ITypeInfo.eType type = null;
    	
    	if( kind == IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME )
    		type = ITypeInfo.t_type;
	    else if( kind == IASTSimpleTypeSpecifier.Type.BOOL )
        	type = ITypeInfo.t_bool;
        else if( kind == IASTSimpleTypeSpecifier.Type.CHAR )
			type = ITypeInfo.t_char;
		else if( kind == IASTSimpleTypeSpecifier.Type.DOUBLE ||kind == IASTSimpleTypeSpecifier.Type.FLOAT  ) 
			type = ITypeInfo.t_double;
		else if( kind == IASTSimpleTypeSpecifier.Type.INT )
			type = ITypeInfo.t_int;
		else if( kind == IASTSimpleTypeSpecifier.Type.VOID )
			type = ITypeInfo.t_void;
		else if( kind == IASTSimpleTypeSpecifier.Type.WCHAR_T)
			type = ITypeInfo.t_wchar_t;
		else if( kind == IASTSimpleTypeSpecifier.Type._BOOL )
			type = ITypeInfo.t__Bool;
	
		List references = ( kind == Type.CLASS_OR_TYPENAME ) ? new ArrayList( DEFAULT_QUALIFIEDNAME_REFERENCE_SIZE ): null; 
		ISymbol s = pst.newSymbol( EMPTY_STRING, type ); 
		if( kind == IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME )
		{
			// lookup the duple
			IToken last = typeName.getLastToken();
			IToken current = null;
			ISymbol typeSymbol = getScopeToSearchUpon( scope, typeName.getFirstToken() );
			
			if( isGlobal )
				typeSymbol = typeSymbol.getSymbolTable().getCompilationUnit();
			
			List [] argLists = typeName.getTemplateIdArgLists();
			int idx = 0;
			
			for( ; ; )
			{
			    if( current == last )
			        break;
				current = ( current != null ) ? current.getNext() : typeName.getFirstToken(); 
				
				if( current.getType() == IToken.tCOLONCOLON ){
					idx++;
					continue;
				}

				char[] image = current.getCharImage();
				int offset = current.getOffset();
				
				if( argLists != null && argLists[ idx ] != null ){
					if( current != last && current.getNext().getType() == IToken.tLT )
						current = TokenFactory.consumeTemplateIdArguments( current.getNext(), last );
				}
				
				if( typeSymbol instanceof IDeferredTemplateInstance ){
					typeSymbol = ((IDeferredTemplateInstance)typeSymbol).getTemplate().getTemplatedSymbol();
				}
				try
                {
					if( argLists != null && argLists[ idx ] != null )
						typeSymbol = ((IContainerSymbol)typeSymbol).lookupTemplateId( image, getTemplateArgList( argLists[idx] ) );
					else if( current != last )
                		typeSymbol = ((IContainerSymbol)typeSymbol).lookupNestedNameSpecifier( image );
                    else
                    	typeSymbol = ((IContainerSymbol)typeSymbol).lookup( image );
					
					if( typeSymbol != null )
					{	
                    	addReference( references, createReference( typeSymbol, image, offset ));
                    	if( argLists != null && argLists[idx] != null )
                    	{
							addTemplateIdReferences( references, argLists[idx] );
							typeName.freeReferences();
                    	}
					}
					else
                    	handleProblem( IProblem.SEMANTIC_NAME_NOT_FOUND, image, current.getOffset(), current.getEndOffset(), current.getLineNumber(), true );
                }
                catch (ParserSymbolTableException e)
                {
                	handleProblem( e.createProblemID(), image,typeName.getStartOffset(), typeName.getEndOffset(), typeName.getLineNumber(), true );
                } 
			}
			s.setTypeSymbol( typeSymbol );
		}
		
		s.getTypeInfo().setBit( isLong, ITypeInfo.isLong );
		s.getTypeInfo().setBit( isShort, ITypeInfo.isShort);
		s.getTypeInfo().setBit( isUnsigned, ITypeInfo.isUnsigned );
		s.getTypeInfo().setBit( isComplex, ITypeInfo.isComplex );
		s.getTypeInfo().setBit( isImaginary, ITypeInfo.isImaginary );
		s.getTypeInfo().setBit( isSigned, ITypeInfo.isSigned );
			
		IASTSimpleTypeSpecifier result = new ASTSimpleTypeSpecifier( s, false, typeNameAsString, references );
		if( kind != Type.CLASS_OR_TYPENAME )
			simpleTypeSpecCache.put( typeNameAsString, result );
		return result;

    }
    /* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createFunction(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, java.util.List, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.IASTTemplate)
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
		boolean isPureVirtual, List constructorChain, boolean isFunctionDefinition, boolean hasFunctionTryBlock, boolean hasVariableArguments ) throws ASTSemanticException
	{
		setFilename( name );
		List references = new ArrayList();
		IContainerSymbol ownerScope = scopeToSymbol( scope );		
		
		// check if this is a method in a body file
		if(name.getSegmentCount() > 1){
			ISymbol symbol = lookupQualifiedName( ownerScope, 
					  name.getLeadingSegments(), 
					  references, 
					  false,
					  LookupType.FORPARENTSCOPE );
			
			IContainerSymbol parentScope = null;
			
			if( symbol instanceof IContainerSymbol )
				parentScope = (IContainerSymbol) symbol;
			else if( symbol instanceof IDeferredTemplateInstance )
				parentScope = ((IDeferredTemplateInstance) symbol).getTemplate().getTemplatedSymbol();
			
			if((parentScope != null) && 
			( (parentScope.getType() == ITypeInfo.t_class) 
			|| (parentScope.getType() == ITypeInfo.t_struct)
			|| (parentScope.getType() == ITypeInfo.t_union))
			){
				if( parentScope.getASTExtension().getPrimaryDeclaration() instanceof IASTElaboratedTypeSpecifier ){
					//we are trying to define a member of a class for which we only have a forward declaration
					handleProblem( scope, IProblem.SEMANTICS_RELATED, name.toCharArray(), startOffset, nameEndOffset, startLine, true );
				}
				IASTScope methodParentScope = (IASTScope)parentScope.getASTExtension().getPrimaryDeclaration();
				ITokenDuple newName = name.getLastSegment();
                return createMethod(
                    methodParentScope,
                    newName, 
                    parameters,
                    returnType,
                    exception,
                    isInline,
                    isFriend,
                    isStatic,
                    startOffset,
                    startLine,
                    newName.getFirstToken().getOffset(),
                    nameEndOffset,
                    nameLine,
                    ownerTemplate,
                    isConst,
                    isVolatile,
                    isVirtual,
                    isExplicit,
                    isPureVirtual,
                    ASTAccessVisibility.PRIVATE,
                    constructorChain, references, isFunctionDefinition, hasFunctionTryBlock, hasVariableArguments );
			}
		}
	
		IParameterizedSymbol symbol = pst.newParameterizedSymbol( name.extractNameFromTemplateId(), ITypeInfo.t_function );
		setFunctionTypeInfoBits(isInline, isFriend, isStatic, symbol);
		
		symbol.setHasVariableArgs( hasVariableArguments );
		
		symbol.prepareForParameters( parameters.size() );
		setParameter( symbol, returnType, false, references );
		setParameters( symbol, references, parameters );
		 
		symbol.setIsForwardDeclaration(!isFunctionDefinition);
		boolean previouslyDeclared = false;

		int size = parameters.size();
		List functionParameters = new ArrayList( size );
		// the lookup requires a list of type infos
		// instead of a list of IASTParameterDeclaration
		for( int i = 0; i < size; i++ ){
			ASTParameterDeclaration param = (ASTParameterDeclaration)parameters.get(i);
			if( param.getSymbol() == null )
				handleProblem( IProblem.SEMANTICS_RELATED, param.getNameCharArray(), param.getNameOffset(), param.getEndingOffset(), param.getStartingLine(), true );
			functionParameters.add(param.getSymbol().getTypeInfo());
		}
		
		IParameterizedSymbol functionDeclaration = null; 
		
		functionDeclaration = 
			(IParameterizedSymbol) lookupQualifiedName(ownerScope, name.getFirstToken().getCharImage(), ITypeInfo.t_function, functionParameters, 0, null, false, LookupType.FORDEFINITION );                

		if( functionDeclaration != null && symbol.isType( ITypeInfo.t_function )){
			previouslyDeclared = true;
			
			if( isFunctionDefinition ){
				functionDeclaration.setForwardSymbol( symbol );
			}
		}
		
		if( previouslyDeclared == false || isFunctionDefinition ){
			try
			{
				ownerScope.addSymbol( symbol );
			}
			catch (ParserSymbolTableException e)
			{
				handleProblem( e.createProblemID(), name.toCharArray());   
			}	
		} else {
			symbol = functionDeclaration;
		}
		
		ASTFunction function = new ASTFunction( symbol, nameEndOffset, parameters, returnType, exception, startOffset, startLine, nameOffset, nameLine, ownerTemplate, references, previouslyDeclared, hasFunctionTryBlock, isFriend, filename );        
	    attachSymbolExtension(symbol, function, isFunctionDefinition); 
	    return function;
	}
    
    protected void setFunctionTypeInfoBits(
        boolean isInline,
        boolean isFriend,
        boolean isStatic,
        IParameterizedSymbol symbol)
    {
        symbol.getTypeInfo().setBit( isInline, ITypeInfo.isInline );
        symbol.getTypeInfo().setBit( isFriend, ITypeInfo.isFriend );
        symbol.getTypeInfo().setBit( isStatic, ITypeInfo.isStatic );
    }
    
    /**
     * @param symbol
     * @param iterator
     */
    protected void setParameters(IParameterizedSymbol symbol, List references, List params) throws ASTSemanticException
    {
    	int size = params.size();
    	for( int i = 0; i < size; i++)
        {
        	setParameter( symbol, (IASTParameterDeclaration)params.get(i), true, references );	
        }
    }

	protected ITypeInfo getParameterTypeInfo( IASTAbstractDeclaration absDecl)throws ASTSemanticException{
	    TypeInfoProvider provider = pst.getTypeInfoProvider();
	    provider.beginTypeConstruction();
		if( absDecl.getTypeSpecifier() instanceof IASTSimpleTypeSpecifier ) 
		{
			IASTSimpleTypeSpecifier simpleType = ((IASTSimpleTypeSpecifier)absDecl.getTypeSpecifier());
			IASTSimpleTypeSpecifier.Type kind = simpleType.getType();
			if( kind == IASTSimpleTypeSpecifier.Type.BOOL )
				provider.setType(ITypeInfo.t_bool);
			else if( kind == IASTSimpleTypeSpecifier.Type.CHAR )
				provider.setType(ITypeInfo.t_char);
			else if( kind == IASTSimpleTypeSpecifier.Type.DOUBLE )
			    provider.setType(ITypeInfo.t_double);
			else if( kind == IASTSimpleTypeSpecifier.Type.FLOAT )
			    provider.setType(ITypeInfo.t_float); 
			else if( kind == IASTSimpleTypeSpecifier.Type.INT )
			    provider.setType(ITypeInfo.t_int);
			else if( kind == IASTSimpleTypeSpecifier.Type.VOID )
			    provider.setType(ITypeInfo.t_void);
			else if( kind == IASTSimpleTypeSpecifier.Type.WCHAR_T)
			    provider.setType(ITypeInfo.t_wchar_t);
			else if( kind == IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME )
			    provider.setType(ITypeInfo.t_type);
			else if( kind == IASTSimpleTypeSpecifier.Type._BOOL ){
			    provider.setType( ITypeInfo.t__Bool );
			}
//			else
//				assert false : "Unexpected IASTSimpleTypeSpecifier.Type";
			
			setTypeBitFlags(provider, simpleType);
			
		}
		else if( absDecl.getTypeSpecifier() instanceof IASTClassSpecifier )
		{
		    provider.setType( ITypeInfo.t_type );
		    provider.setTypeSymbol( ((ASTClassSpecifier)absDecl.getTypeSpecifier()).getSymbol() );
		}
		else if( absDecl.getTypeSpecifier() instanceof IASTEnumerationSpecifier )
		{
		    provider.setType( ITypeInfo.t_type );
		    provider.setTypeSymbol( ((ASTEnumerationSpecifier)absDecl.getTypeSpecifier()).getSymbol() );
		}
		else if( absDecl.getTypeSpecifier() instanceof IASTElaboratedTypeSpecifier )
		{
		    provider.setType( ITypeInfo.t_type );
		    provider.setTypeSymbol( ((ASTElaboratedTypeSpecifier)absDecl.getTypeSpecifier()).getSymbol() );
		}
//		else
//			assert false : this;
		
		return provider.completeConstruction();		
	}
    /**
	 * @param type
	 * @param simpleType
	 */
	private void setTypeBitFlags(TypeInfoProvider provider, IASTSimpleTypeSpecifier simpleType) {
		provider.setBit( simpleType.isLong(), ITypeInfo.isLong);
		provider.setBit( simpleType.isShort(), ITypeInfo.isShort);
		provider.setBit( simpleType.isUnsigned(), ITypeInfo.isUnsigned);
		provider.setBit( simpleType.isComplex(), ITypeInfo.isComplex);
		provider.setBit( simpleType.isImaginary(), ITypeInfo.isImaginary);
		provider.setBit( simpleType.isSigned(), ITypeInfo.isSigned);
	}

	/**
	 * @param symbol
	 * @param returnType
	 */
	protected void setParameter(IParameterizedSymbol symbol, IASTAbstractDeclaration absDecl, boolean isParameter, List references) throws ASTSemanticException
	{
		if (absDecl.getTypeSpecifier() == null)
			return;
	
		// now determined by another function    
		ITypeInfo info = getParameterTypeInfo( absDecl );
		ITypeInfo.eType type = info.getType();
		
		ISymbol xrefSymbol = info.getTypeSymbol();
		List newReferences = null; 
		int infoBits = 0;
	    if( absDecl.getTypeSpecifier() instanceof IASTSimpleTypeSpecifier ) 
	    {
	   		if( ((IASTSimpleTypeSpecifier)absDecl.getTypeSpecifier()).getType() == IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME )
	    	{
	    		xrefSymbol = ((ASTSimpleTypeSpecifier)absDecl.getTypeSpecifier()).getSymbol(); 
	    		newReferences = ((ASTSimpleTypeSpecifier)absDecl.getTypeSpecifier()).getReferences();
	    	}
	   		
	   		infoBits = ((ASTSimpleTypeSpecifier)absDecl.getTypeSpecifier()).getSymbol().getTypeInfo().getTypeBits();
	    }
	    else if( absDecl.getTypeSpecifier() instanceof ASTElaboratedTypeSpecifier )
	    {
	    	ASTElaboratedTypeSpecifier elab = (ASTElaboratedTypeSpecifier)absDecl.getTypeSpecifier();
	    	xrefSymbol = elab.getSymbol();
	    	List elabReferences = elab.getReferences();
			newReferences = new ArrayList(elabReferences.size());
			for( int i = 0; i < elabReferences.size(); ++i )
			{
				IASTReference r = (IASTReference)elabReferences.get(i);
				newReferences.add( r );
			}
	    	if( xrefSymbol != null )
	    		addReference( newReferences, createReference( xrefSymbol, elab.getNameCharArray(), elab.getNameOffset()) );  
	    }
	    
	    char[] paramName = EMPTY_STRING; 
	    if(absDecl instanceof IASTParameterDeclaration){
	    	paramName = ((ASTParameterDeclaration)absDecl).getNameCharArray();
	    }
	    
	    ISymbol paramSymbol = pst.newSymbol( paramName, type );
	    if( xrefSymbol != null ){
	    	if( absDecl.getTypeSpecifier() instanceof IASTSimpleTypeSpecifier )
	    		paramSymbol.setTypeSymbol( xrefSymbol.getTypeSymbol() );
	    	else 
	    		paramSymbol.setTypeSymbol( xrefSymbol );
	    }
	    
	    paramSymbol.getTypeInfo().setTypeBits( infoBits );
	    paramSymbol.getTypeInfo().setBit( absDecl.isConst(), ITypeInfo.isConst );
	    paramSymbol.getTypeInfo().setBit( absDecl.isVolatile(), ITypeInfo.isVolatile );
	    
	    List ptrs = null, arrayMods = null;
	    if( absDecl instanceof ASTParameterDeclaration ){
	        ptrs = ((ASTParameterDeclaration)absDecl).getPointerOperatorsList(); 
	        arrayMods = ((ASTParameterDeclaration)absDecl).getArrayModifiersList();
	    } else {
	        ptrs = ((ASTAbstractDeclaration)absDecl).getPointerOperatorsList(); 
	        arrayMods = ((ASTAbstractDeclaration)absDecl).getArrayModifiersList();
	    }
	    setPointerOperators( paramSymbol, ptrs, arrayMods );
	
	    if( isParameter)
	    	symbol.addParameter( paramSymbol );
	    else
			symbol.setReturnType( paramSymbol );
			
		if( newReferences != null && !newReferences.isEmpty())
			references.addAll( newReferences );
		
		if( absDecl instanceof ASTParameterDeclaration )
		{
			ASTParameterDeclaration parm = (ASTParameterDeclaration)absDecl;
			parm.setSymbol( paramSymbol );
			attachSymbolExtension( paramSymbol, parm, true );
		}
	}

    /**
     * @param paramSymbol
     * @param iterator
     */
    protected void setPointerOperators(ISymbol symbol, List pointerOps, List arrayMods) throws ASTSemanticException
    {
        int ptrOpsSize = pointerOps.size();
        for( int i = 0; i < ptrOpsSize; i++)
        {
        	ASTPointerOperator pointerOperator = (ASTPointerOperator)pointerOps.get(i);
        	if( pointerOperator == ASTPointerOperator.REFERENCE )
        		symbol.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_reference )); 
        	else if( pointerOperator == ASTPointerOperator.POINTER )
				symbol.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ));
			else if( pointerOperator == ASTPointerOperator.CONST_POINTER )
				symbol.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer, true, false ));
			else if( pointerOperator == ASTPointerOperator.VOLATILE_POINTER )
				symbol.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer, false, true));
			else if( pointerOperator == ASTPointerOperator.RESTRICT_POINTER )
				symbol.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ));
//			else
//				assert false : pointerOperator;
        }
        int arrayModsSize = arrayMods.size();        
        for( int i = 0; i < arrayModsSize; i++)
        {
        	symbol.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_array )); 
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createMethod(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, java.util.List, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, org.eclipse.cdt.core.parser.ast.IASTExceptionSpecification, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.IASTTemplate, boolean, boolean, boolean, boolean, boolean, boolean, boolean, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility)
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
		boolean isExplicit, boolean isPureVirtual, ASTAccessVisibility visibility, List constructorChain, boolean isFunctionDefinition, boolean hasFunctionTryBlock, boolean hasVariableArguments ) throws ASTSemanticException
	{
		return createMethod(scope, name, parameters, returnType, exception,
		isInline, isFriend, isStatic, startOffset, startLine, nameOffset,
		nameEndOffset, nameLine, ownerTemplate, isConst, isVolatile, isVirtual,
		isExplicit, isPureVirtual, visibility, constructorChain, null, isFunctionDefinition, hasFunctionTryBlock, hasVariableArguments );
	}   
	  
    public IASTMethod createMethod(
        IASTScope scope,
        ITokenDuple nameDuple,
        List parameters, 
        IASTAbstractDeclaration returnType,
        IASTExceptionSpecification exception,
        boolean isInline,
        boolean isFriend,
        boolean isStatic,
        int startOffset,
        int startingLine,
        int nameOffset,
        int nameEndOffset,
        int nameLine,
        IASTTemplate ownerTemplate,
        boolean isConst,
        boolean isVolatile,
        boolean isVirtual,
        boolean isExplicit, 
        boolean isPureVirtual,
        ASTAccessVisibility visibility, List constructorChain, List references, boolean isFunctionDefinition, boolean hasFunctionTryBlock, boolean hasVariableArguments ) throws ASTSemanticException
    {
    	setFilename( nameDuple );
		boolean isConstructor = false;
		boolean isDestructor = false;

		IContainerSymbol ownerScope = scopeToSymbol( ownerTemplate != null ? (IASTScope) ownerTemplate : scope );

		IParameterizedSymbol symbol = null;

		if( references == null )
		{
			references = new ArrayList();
			if( nameDuple.length() > 2  ) // destructor
			{
				ITokenDuple leadingSegments = nameDuple.getLeadingSegments();
				ISymbol test = lookupQualifiedName( ownerScope, leadingSegments, references, false );
				if( test == ownerScope )
					nameDuple = nameDuple.getLastSegment();
			}
		}
		char[] methodName = null;
		List templateIdArgList = null;
		//template-id?
		if( nameDuple.getTemplateIdArgLists() != null ){
			templateIdArgList = nameDuple.getTemplateIdArgLists()[ 0 ];
			methodName = nameDuple.extractNameFromTemplateId();
		} else {
			methodName = nameDuple.toCharArray();
		}
		
		symbol = pst.newParameterizedSymbol( methodName, ITypeInfo.t_function );
		setFunctionTypeInfoBits(isInline, isFriend, isStatic, symbol);
		setMethodTypeInfoBits( symbol, isConst, isVolatile, isVirtual, isExplicit );
		symbol.setHasVariableArgs( hasVariableArguments );
		    	
		symbol.prepareForParameters( parameters.size() );
    	if( returnType.getTypeSpecifier() != null )
			setParameter( symbol, returnType, false, references );
		setParameters( symbol, references, parameters );
		
		IASTClassSpecifier classifier = null; 
		if( scope instanceof IASTTemplateDeclaration ){
			classifier = (IASTClassSpecifier) ((IASTTemplateDeclaration)scope).getOwnerScope();
		} else {
			classifier = (IASTClassSpecifier) scope; 
		}
		char[] parentName = ((ASTClassSpecifier)classifier).getNameCharArray(); 
  
		// check constructor / destructor if no return type
		if ( returnType.getTypeSpecifier() == null ){
			if(CharArrayUtils.indexOf( DOUBLE_COLON, parentName ) != -1){
			    parentName = CharArrayUtils.lastSegment( parentName, DOUBLE_COLON );
			}    	
			if( CharArrayUtils.equals( parentName, methodName) ){
				isConstructor = true; 
			} else if( methodName[0] == '~' && CharArrayUtils.equals( methodName, 1, methodName.length - 1, parentName )){ //$NON-NLS-1$
				isDestructor = true;
			}
		}

		symbol.setIsForwardDeclaration(!isFunctionDefinition);
		boolean previouslyDeclared = false; 
		
		IParameterizedSymbol functionDeclaration = null;
		
		if( isFunctionDefinition || isFriend )
		{
		    int size = parameters.size();
			List functionParameters = new ArrayList( size );
			// the lookup requires a list of type infos
			// instead of a list of IASTParameterDeclaration
			for( int i = 0; i < size; i++ ){
				ASTParameterDeclaration param = (ASTParameterDeclaration)parameters.get(i);
				if( param.getSymbol() == null )
					handleProblem( IProblem.SEMANTICS_RELATED, param.getNameCharArray(), param.getNameOffset(), param.getEndingOffset(), param.getNameLineNumber(), true );
				functionParameters.add(param.getSymbol().getTypeInfo());
			}
			
			functionDeclaration = (IParameterizedSymbol) lookupQualifiedName( ownerScope, nameDuple, 
																			  isConstructor ? ITypeInfo.t_constructor : ITypeInfo.t_function, 
																			  functionParameters, null, false, 
																			  isFriend ? LookupType.FORFRIENDSHIP : LookupType.FORDEFINITION );
			
			previouslyDeclared = ( functionDeclaration != null ) && functionDeclaration.isType( isConstructor ? ITypeInfo.t_constructor : ITypeInfo.t_function );
			
			if( isFriend )
			{
				if( functionDeclaration != null && functionDeclaration.isType( isConstructor ? ITypeInfo.t_constructor : ITypeInfo.t_function ))
				{
					symbol.setForwardSymbol( functionDeclaration );
					// friend declaration, has no real visibility, set private
					visibility = ASTAccessVisibility.PRIVATE;		
				} else if( ownerScope.getContainingSymbol().isType( ITypeInfo.t_constructor ) ||
						   ownerScope.getContainingSymbol().isType( ITypeInfo.t_function )    ||
						   ownerScope.getContainingSymbol().isType( ITypeInfo.t_block ) )
				{	
					//only needs to be previously declared if we are in a local class
					handleProblem( IProblem.SEMANTIC_ILLFORMED_FRIEND, nameDuple.toCharArray(), nameDuple.getStartOffset(), nameDuple.getEndOffset(), nameDuple.getLineNumber(), true );
				}
				 
			} else if( functionDeclaration != null && functionDeclaration.isType( isConstructor ? ITypeInfo.t_constructor : ITypeInfo.t_function ) )
			{
				functionDeclaration.setForwardSymbol( symbol );
				// set the definition visibility = declaration visibility
				visibility = ((IASTMethod)(functionDeclaration.getASTExtension().getPrimaryDeclaration())).getVisiblity();		
			}
		}
		
		try
		{
			if( isFriend )
			{
				if( functionDeclaration != null )
					((IDerivableContainerSymbol)ownerScope).addFriend( functionDeclaration );
				else
					((IDerivableContainerSymbol)ownerScope).addFriend( symbol );
			} else if( !isConstructor ){
				if( templateIdArgList == null )
					ownerScope.addSymbol( symbol );
				else
					ownerScope.addTemplateId( symbol, getTemplateArgList( templateIdArgList ) );
			}
			else
			{
				symbol.setType( ITypeInfo.t_constructor );
				((IDerivableContainerSymbol)ownerScope).addConstructor( symbol );
			}
		}
		catch (ParserSymbolTableException e)
		{
			handleProblem(e.createProblemID(), nameDuple.toCharArray(), nameDuple.getStartOffset(), nameDuple.getEndOffset(), nameDuple.getLineNumber(), true );   
		}

		resolveLeftoverConstructorInitializerMembers( symbol, constructorChain );
  
        ASTMethod method = new ASTMethod( symbol, parameters, returnType, exception, startOffset, startingLine, nameOffset, nameEndOffset, nameLine, ownerTemplate, references, previouslyDeclared, isConstructor, isDestructor, isPureVirtual, visibility, constructorChain, hasFunctionTryBlock, isFriend, filename );
        if( functionDeclaration != null && isFunctionDefinition )
        	attachSymbolExtension( symbol, (ASTSymbol) functionDeclaration.getASTExtension().getPrimaryDeclaration(), false );
        attachSymbolExtension( symbol, method, isFunctionDefinition );
        return method;
    }
    
   
    
    /**
	 * @param symbol
	 * @param constructorChain
	 */
	protected void resolveLeftoverConstructorInitializerMembers(IParameterizedSymbol symbol, List constructorChain) throws ASTSemanticException
	{
		if( constructorChain != null )
		{
			int size = constructorChain.size();
			for( int i = 0; i < size; i++ )
			{
				IASTConstructorMemberInitializer initializer = (IASTConstructorMemberInitializer)constructorChain.get(i);
				if( initializer.getNameCharArray().length > 0 && 
					initializer instanceof ASTConstructorMemberInitializer && 
					((ASTConstructorMemberInitializer)initializer).requiresNameResolution() ) 
				{
					ASTConstructorMemberInitializer realInitializer = ((ASTConstructorMemberInitializer)initializer);
					IDerivableContainerSymbol container = (IDerivableContainerSymbol) symbol.getContainingSymbol();
					lookupQualifiedName(container, realInitializer.getNameCharArray(), ITypeInfo.t_any, null, realInitializer.getNameOffset(), realInitializer.getReferences(), false, LookupType.QUALIFIED);
					// TODO try and resolve parameter references now in the expression list
				}
			}
		}
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
        symbol.getTypeInfo().setBit( isConst, ITypeInfo.isConst );
		symbol.getTypeInfo().setBit( isVolatile, ITypeInfo.isVolatile );
		symbol.getTypeInfo().setBit( isVirtual, ITypeInfo.isVirtual );
		symbol.getTypeInfo().setBit( isExplicit, ITypeInfo.isExplicit );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createVariable(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, boolean, org.eclipse.cdt.core.parser.ast.IASTInitializerClause, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, boolean, boolean, boolean, boolean, int, int)
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
        int startingLine, int nameOffset, int nameEndOffset, int nameLine, IASTExpression constructorExpression, char[] fn) throws ASTSemanticException
    {
    	setFilename( fn );
		List references = new ArrayList(); 
		IContainerSymbol ownerScope = scopeToSymbol( scope );		

		if( name == null )
			handleProblem( IProblem.SEMANTIC_NAME_NOT_PROVIDED, null, startingOffset, nameEndOffset, nameLine, true );
		
		if(name.getSegmentCount() > 1)
		{
			ISymbol symbol = lookupQualifiedName( ownerScope, 
												  name.getLeadingSegments(), 
												  references, 
												  false,
												  LookupType.FORPARENTSCOPE );
			IContainerSymbol parentScope = null;
			
			if( symbol instanceof IContainerSymbol )
				parentScope = (IContainerSymbol) symbol;
			else if( symbol instanceof IDeferredTemplateInstance )
				parentScope = ((IDeferredTemplateInstance) symbol).getTemplate().getTemplatedSymbol();
			
			if( (parentScope != null) && ( (parentScope.getType() == ITypeInfo.t_class) ||
					                       (parentScope.getType() == ITypeInfo.t_struct)|| 
										   (parentScope.getType() == ITypeInfo.t_union) ) )
			{
				IASTScope fieldParentScope = (IASTScope)parentScope.getASTExtension().getPrimaryDeclaration();

				ITokenDuple newName = name.getLastSegment();

				return createField(fieldParentScope, newName,isAuto, initializerClause, bitfieldExpression, abstractDeclaration, isMutable, isExtern, 
						isRegister, isStatic, startingOffset, startingLine, newName.getStartOffset(), nameEndOffset, nameLine, constructorExpression, ASTAccessVisibility.PRIVATE, references, fn);
			}
		}

		ISymbol newSymbol = cloneSimpleTypeSymbol(name.getFirstToken().getCharImage(), abstractDeclaration, references);
        if( newSymbol == null )
        	handleProblem( IProblem.SEMANTICS_RELATED, name.toCharArray() );
        
        setVariableTypeInfoBits(
            isAuto,
            abstractDeclaration,
            isMutable,
            isExtern,
            isRegister,
            isStatic,
            newSymbol);
        
        int numPtrOps  = ((ASTAbstractDeclaration)abstractDeclaration).getNumArrayModifiers() +
        				 ((ASTAbstractDeclaration)abstractDeclaration).getNumPointerOperators(); 
        newSymbol.preparePtrOperatros( numPtrOps );
		setPointerOperators( newSymbol, ((ASTAbstractDeclaration)abstractDeclaration).getPointerOperatorsList(), 
		        						((ASTAbstractDeclaration)abstractDeclaration).getArrayModifiersList() );
		
		newSymbol.setIsForwardDeclaration( isStatic || isExtern );
		boolean previouslyDeclared = false;
		if(!isStatic){
			ISymbol variableDeclaration = lookupQualifiedName(ownerScope, name.toCharArray(), null, false, LookupType.UNQUALIFIED);                
	
			if( variableDeclaration != null && newSymbol.getType() == variableDeclaration.getType() )
			{
			    TypeInfoProvider provider = pst.getTypeInfoProvider();
			    
			    ITypeInfo newInfo = newSymbol.getTypeInfo().getFinalType( provider );
			    ITypeInfo varInfo = variableDeclaration.getTypeInfo().getFinalType( provider );
			    
				if( newInfo.equals( varInfo ) )
				{
					variableDeclaration.setForwardSymbol( newSymbol );
					previouslyDeclared = true;
				}
				provider.returnTypeInfo( newInfo );
				provider.returnTypeInfo( varInfo );
			}
		}
		try
		{
			ownerScope.addSymbol( newSymbol );
		}
		catch (ParserSymbolTableException e)
		{
			handleProblem(e.createProblemID(), name.getFirstToken().getCharImage() );
		}
        
        ASTVariable variable = new ASTVariable( newSymbol, abstractDeclaration, initializerClause, bitfieldExpression, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, references, constructorExpression, previouslyDeclared, filename );
        if( variable.getInitializerClause() != null )
        {
        	variable.getInitializerClause().setOwnerVariableDeclaration(variable);
        	addDesignatorReferences( (ASTInitializerClause)variable.getInitializerClause() );
        }
        
        attachSymbolExtension(newSymbol, variable, !isStatic );
        return variable;        
    }
    
    
    /**
     * @param clause
     */
    protected void addDesignatorReferences( ASTInitializerClause clause )
    {
        if( clause.getKind() == IASTInitializerClause.Kind.DESIGNATED_INITIALIZER_LIST || 
        	clause.getKind() == IASTInitializerClause.Kind.DESIGNATED_ASSIGNMENT_EXPRESSION )
        {
			ISymbol variableSymbol = ((ASTVariable)clause.getOwnerVariableDeclaration()).getSymbol();
			ISymbol currentSymbol = variableSymbol.getTypeSymbol();
			if( currentSymbol == null )
				return;
			
			ITypeInfo currentTypeInfo = TypeInfoProvider.newTypeInfo( currentSymbol.getTypeInfo() ); 
			List designators = clause.getDesignatorList();
			int size = designators.size();
			for( int i = 0; i < size; i++ )
			{
				ASTDesignator designator = (ASTDesignator)designators.get(i);
				if( designator.getKind() == IASTDesignator.DesignatorKind.FIELD )
				{
					ISymbol lookup = null;
					if( ! ( currentSymbol instanceof IContainerSymbol ) ) 
						break;
					
					try
                    {
                        lookup = ((IContainerSymbol)currentSymbol).lookup( designator.fieldNameCharArray() );
                    }
                    catch (ParserSymbolTableException e){
                        break;
                    }
                    
                    if( lookup == null || lookup.getContainingSymbol() != currentSymbol )  
                        break;
                        
                    try
                    {
                    	if( lookup != null )
                    		addReference( clause.getReferences(), createReference( lookup, designator.fieldNameCharArray(), designator.fieldOffset() ));
                    }
                    catch (ASTSemanticException e1)
                    {
                        // error
                    }
                    
					// we have found the correct field
					currentTypeInfo = TypeInfoProvider.newTypeInfo( lookup.getTypeInfo() );
					if( lookup.getTypeInfo() == null )
						break;
					currentSymbol = lookup.getTypeSymbol();

				}
				else if( designator.getKind() == IASTDesignator.DesignatorKind.SUBSCRIPT )
					currentTypeInfo.applyOperatorExpression( SUBSCRIPT );		
			}
			
        }			
        
        if( clause.getKind() == IASTInitializerClause.Kind.DESIGNATED_INITIALIZER_LIST || 
        	clause.getKind() == IASTInitializerClause.Kind.INITIALIZER_LIST )
        {	
        	List subInitializers = clause.getInitializersList();
        	int size = subInitializers.size();
        	for( int i = 0; i < size; i++ )
        		addDesignatorReferences( (ASTInitializerClause)subInitializers.get(i) );
        }
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
        newSymbol.getTypeInfo().setBit( isMutable, ITypeInfo.isMutable );
        newSymbol.getTypeInfo().setBit( isAuto, ITypeInfo.isAuto );
        newSymbol.getTypeInfo().setBit( isExtern, ITypeInfo.isExtern );
        newSymbol.getTypeInfo().setBit( isRegister, ITypeInfo.isRegister );
        newSymbol.getTypeInfo().setBit( isStatic, ITypeInfo.isStatic );
        newSymbol.getTypeInfo().setBit( abstractDeclaration.isConst(), ITypeInfo.isConst );
        newSymbol.getTypeInfo().setBit( abstractDeclaration.isVolatile(), ITypeInfo.isVolatile );
    }
    
    protected ISymbol cloneSimpleTypeSymbol(
        char[] name,
        IASTAbstractDeclaration abstractDeclaration,
        List references) throws ASTSemanticException
    {
//    	assert abstractDeclaration.getTypeSpecifier() != null : this;
        ISymbol newSymbol = null;
		ISymbol symbolToBeCloned = null;		
        if( abstractDeclaration.getTypeSpecifier() instanceof ASTSimpleTypeSpecifier ) 
        {
        	symbolToBeCloned = ((ASTSimpleTypeSpecifier)abstractDeclaration.getTypeSpecifier()).getSymbol();
        	if( references != null )
        	{
        		List absRefs = ((ASTSimpleTypeSpecifier)abstractDeclaration.getTypeSpecifier()).getReferences();
        		for( int i = 0; i < absRefs.size(); ++i )
        		{
        			IASTReference r = (IASTReference) absRefs.get(i);
        			references.add( r );
        		}
        	}
        }
        else if( abstractDeclaration.getTypeSpecifier() instanceof ASTClassSpecifier )  
        {
            symbolToBeCloned = pst.newSymbol(name, ITypeInfo.t_type);
            symbolToBeCloned.setTypeSymbol(((ASTClassSpecifier)abstractDeclaration.getTypeSpecifier()).getSymbol());
		}
		else if( abstractDeclaration.getTypeSpecifier() instanceof ASTElaboratedTypeSpecifier ) 
		{
			ASTElaboratedTypeSpecifier elab = ((ASTElaboratedTypeSpecifier)abstractDeclaration.getTypeSpecifier());
			symbolToBeCloned = pst.newSymbol(name, ITypeInfo.t_type);
			symbolToBeCloned.setTypeSymbol(elab.getSymbol());
			if( elab.getSymbol() != null && references != null )
				addReference( references, createReference( elab.getSymbol(), elab.getNameCharArray(), elab.getNameOffset()) );
		} 
		else if ( abstractDeclaration.getTypeSpecifier() instanceof ASTEnumerationSpecifier )
		{
			symbolToBeCloned = pst.newSymbol( name, ITypeInfo.t_type );
			symbolToBeCloned.setTypeSymbol(((ASTEnumerationSpecifier)abstractDeclaration.getTypeSpecifier()).getSymbol());
		}
		if( symbolToBeCloned != null ){
			newSymbol = (ISymbol) symbolToBeCloned.clone(); 
			newSymbol.setName( name );
		}

        return newSymbol;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createField(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, boolean, org.eclipse.cdt.core.parser.ast.IASTInitializerClause, org.eclipse.cdt.core.parser.ast.IASTExpression, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, boolean, boolean, boolean, boolean, int, int, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility)
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
		int nameOffset, int nameEndOffset, int nameLine, IASTExpression constructorExpression, ASTAccessVisibility visibility, char[] fn) throws ASTSemanticException
	{
		return createField(scope, name,isAuto, initializerClause, bitfieldExpression, abstractDeclaration, isMutable, isExtern, 
		isRegister, isStatic, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, constructorExpression, visibility, null, fn);		
	}
	
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
        IASTExpression constructorExpression, ASTAccessVisibility visibility, List references, char[] fn) throws ASTSemanticException
    {
    	setFilename( fn );
		IContainerSymbol ownerScope = scopeToSymbol( scope );		

		char[] image = ( name != null ) ? name.toCharArray() : EMPTY_STRING;
		
		if(references == null)
			references = new ArrayList();
		ISymbol newSymbol = cloneSimpleTypeSymbol(image, abstractDeclaration, references);
		if( newSymbol == null )
			handleProblem( IProblem.SEMANTICS_RELATED, image );
			
		
		setVariableTypeInfoBits(
			isAuto,
			abstractDeclaration,
			isMutable,
			isExtern,
			isRegister,
			isStatic,
			newSymbol);
		setPointerOperators( newSymbol, ((ASTAbstractDeclaration)abstractDeclaration).getPointerOperatorsList(), 
		        						((ASTAbstractDeclaration)abstractDeclaration).getArrayModifiersList() );
		
		newSymbol.setIsForwardDeclaration(isStatic);
		boolean previouslyDeclared = false;
		if( !isStatic && !image.equals( EMPTY_STRING ) ){
			ISymbol fieldDeclaration = lookupQualifiedName(ownerScope, image, null, false, LookupType.FORDEFINITION);                
			
			if( fieldDeclaration != null && newSymbol.getType() == fieldDeclaration.getType() )
			{
			    TypeInfoProvider provider = pst.getTypeInfoProvider();
			    
			    ITypeInfo newInfo = newSymbol.getTypeInfo().getFinalType( provider );
			    ITypeInfo fieldInfo = fieldDeclaration.getTypeInfo().getFinalType( provider );
			    
				if( newInfo.equals( fieldInfo ) )
				{
					previouslyDeclared = true;
					fieldDeclaration.setForwardSymbol( newSymbol );
					// set the definition visibility = declaration visibility
					visibility = ((IASTField)fieldDeclaration.getASTExtension().getPrimaryDeclaration()).getVisiblity();
				}
				provider.returnTypeInfo( newInfo );
				provider.returnTypeInfo( fieldInfo );
			}
		}
		
		try
		{
			ownerScope.addSymbol( newSymbol );
		}
		catch (ParserSymbolTableException e)
		{
			handleProblem(e.createProblemID(), image );
		}
		
		ASTField field = new ASTField( newSymbol, abstractDeclaration, initializerClause, bitfieldExpression, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, references, previouslyDeclared, constructorExpression, visibility, filename );
		attachSymbolExtension(newSymbol, field, !isStatic );
		return field;        


    }
 
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope, java.util.List, boolean, int)
     */
    public IASTTemplateDeclaration createTemplateDeclaration(
        IASTScope scope,
        List templateParameters,
        boolean exported,
        int startingOffset, int startingLine, char[] fn) throws ASTSemanticException
    {
    	setFilename(fn);
    	ITemplateSymbol template = pst.newTemplateSymbol( ParserSymbolTable.EMPTY_NAME_ARRAY );

		// the lookup requires a list of type infos
		// instead of a list of IASTParameterDeclaration
		int size = templateParameters.size();
		for( int i = 0; i < size; i++ ){
			ASTTemplateParameter param = (ASTTemplateParameter)templateParameters.get(i);
			try {
				template.addTemplateParameter( param.getSymbol() );
			} catch (ParserSymbolTableException e) {
				handleProblem( e.createProblemID(), param.getNameCharArray(), startingOffset, -1, startingLine, true );
			}
		}
		
		ASTTemplateDeclaration ast = new ASTTemplateDeclaration( template, scope, templateParameters, filename);
		ast.setStartingOffsetAndLineNumber( startingOffset, startingLine );
        attachSymbolExtension( template, ast, false );

        return ast; 
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateParameter(org.eclipse.cdt.core.parser.ast.IASTTemplateParameter.ParamKind, java.lang.String, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration, java.util.List)
     */
    public IASTTemplateParameter createTemplateParameter(
        ParamKind kind,
        char[] identifier,
        IASTTypeId defaultValue,
        IASTParameterDeclaration parameter,
        List parms, 
		IASTCodeScope parameterScope,
		int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endingLine, char[] fn ) throws ASTSemanticException
    {
    	//TODO filename
    	ISymbol symbol = null;
    	
    	TypeInfoProvider provider = pst.getTypeInfoProvider();
    	provider.beginTypeConstruction();
    	
    	if( defaultValue != null ){
    	    try {
                provider.setDefaultObj( defaultValue.getTypeSymbol().getTypeInfo() );
            } catch ( ASTNotImplementedException e1 ) {
            }
    	}
    	if( kind == ParamKind.TEMPLATE_LIST ){
    		ITemplateSymbol template = pst.newTemplateSymbol( identifier );
    		provider.setType( ITypeInfo.t_templateParameter );
    		provider.setTemplateParameterType( ITypeInfo.t_template );
    		template.setTypeInfo( provider.completeConstruction() );
    		int size = parms.size();
    		for( int i = 0; i < size; i++ ){
    			ASTTemplateParameter param = (ASTTemplateParameter)parms.get(i);
    			try {
    				template.addTemplateParameter( param.getSymbol() );
    			} catch (ParserSymbolTableException e) {
    				handleProblem( e.createProblemID(), param.getNameCharArray(), param.getStartingOffset(), param.getEndingOffset(), param.getStartingLine(), true );  //$NON-NLS-1$
    			}
    		}
    		symbol = template;
     	} else {
    		
    		if(  kind == ParamKind.CLASS || kind == ParamKind.TYPENAME ){
    			symbol = pst.newSymbol( identifier );
    			provider.setType( ITypeInfo.t_templateParameter );
        		provider.setTemplateParameterType( ITypeInfo.t_typeName );
        		symbol.setTypeInfo( provider.completeConstruction() );
        	} else /*ParamKind.PARAMETER*/ {
        		symbol = cloneSimpleTypeSymbol( ((ASTParameterDeclaration)parameter).getNameCharArray(), parameter, null );
        		provider.setTemplateParameterType( symbol.getType() );
        		provider.setType( ITypeInfo.t_templateParameter );
        		provider.setTypeSymbol( symbol.getTypeSymbol() );
        		ITypeInfo info = provider.completeConstruction();
        		info.addPtrOperator( symbol.getPtrOperators() );
        		info.setTypeBits( symbol.getTypeInfo().getTypeBits() );
        		symbol.setTypeInfo( info );
        	}
    	}

    	IContainerSymbol codeScope = ((ASTCodeScope)parameterScope).getContainerSymbol();
    	try {
			codeScope.addSymbol( symbol );
		} catch (ParserSymbolTableException e) {
		}
				
    	ASTTemplateParameter ast = new ASTTemplateParameter( symbol, defaultValue,  parameter, parms, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, endingOffset, endingLine, fn );
    	
   	    attachSymbolExtension( symbol, ast, false );
        
        return ast; 
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateInstantiation(org.eclipse.cdt.core.parser.ast.IASTScope, int)
     */
    public IASTTemplateInstantiation createTemplateInstantiation(
        IASTScope scope,
        int startingOffset, int startingLine, char[] fn)
    {
    	ASTTemplateInstantiation inst = new ASTTemplateInstantiation( scope, fn );
    	inst.setStartingOffsetAndLineNumber( startingOffset, startingLine );
        return inst; 
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTemplateSpecialization(org.eclipse.cdt.core.parser.ast.IASTScope, int)
     */
    public IASTTemplateSpecialization createTemplateSpecialization(
        IASTScope scope,
        int startingOffset, int startingLine, char[] fn)
    {
        ITemplateSymbol template = pst.newTemplateSymbol( ParserSymbolTable.EMPTY_NAME_ARRAY );

        ASTTemplateSpecialization ast = new ASTTemplateSpecialization( template, scope, fn );
		ast.setStartingOffsetAndLineNumber( startingOffset, startingLine );
        attachSymbolExtension( template, ast, false );

        return ast; 
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTypedef(org.eclipse.cdt.core.parser.ast.IASTScope, java.lang.String, org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration, int, int)
     */
    public IASTTypedefDeclaration createTypedef(
        IASTScope scope,
        char[] name,
        IASTAbstractDeclaration mapping,
        int startingOffset,
        int startingLine, int nameOffset, int nameEndOffset, int nameLine, char[] fn) throws ASTSemanticException
    {
    	IContainerSymbol containerSymbol = scopeToSymbol(scope);
		ISymbol typeSymbol = cloneSimpleTypeSymbol( name, mapping, null );
		
		if( typeSymbol == null )
			handleProblem( scope, IProblem.SEMANTICS_RELATED, name, nameOffset, nameEndOffset, nameLine, true );
		
		setPointerOperators( typeSymbol, ((ASTAbstractDeclaration)mapping).getPointerOperatorsList(), 
		        						 ((ASTAbstractDeclaration)mapping).getArrayModifiersList() );
		
		if( typeSymbol.getType() != ITypeInfo.t_type ){
			ISymbol newSymbol = pst.newSymbol( name, ITypeInfo.t_type);
	    	newSymbol.getTypeInfo().setBit( true,ITypeInfo.isTypedef );
	    	newSymbol.setTypeSymbol( typeSymbol );
	    	typeSymbol = newSymbol;
		} else {
			typeSymbol.getTypeInfo().setBit( true,ITypeInfo.isTypedef );
		}
	
    	List references = new ArrayList();
		if( mapping.getTypeSpecifier() instanceof ASTSimpleTypeSpecifier ) 
	    {
			List mappingReferences = ((ASTSimpleTypeSpecifier)mapping.getTypeSpecifier()).getReferences();
			if( mappingReferences != null && !mappingReferences.isEmpty() )
			{
				for( int i = 0; i < mappingReferences.size(); ++i )
				{
					IASTReference r = (IASTReference) mappingReferences.get(i);
					references.add( r );
				}
			}
		}
    	
    	try
        {
            containerSymbol.addSymbol( typeSymbol );
        }
        catch (ParserSymbolTableException e)
        {
        	handleProblem(e.createProblemID(), name );
        }
        ASTTypedef d = new ASTTypedef( typeSymbol, mapping, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, references, filename );
        attachSymbolExtension(typeSymbol, d, true );
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
        int startingLine, int endingOffset, int endingLine, boolean isFriend, char[] fn)
    {
        return new ASTAbstractTypeSpecifierDeclaration( scopeToSymbol(scope), typeSpecifier, template, startingOffset, startingLine, endingOffset, endingLine, isFriend, fn );
    }

    
    public IASTElaboratedTypeSpecifier createElaboratedTypeSpecifier(IASTScope scope, ASTClassKind kind, ITokenDuple name, int startingOffset, int startingLine, int endOffset, int endingLine, boolean isForewardDecl, boolean isFriend) throws ASTSemanticException
    {
    	setFilename( name );
		IContainerSymbol currentScopeSymbol = scopeToSymbol(scope);
		IContainerSymbol originalScope = currentScopeSymbol;
		
		ITypeInfo.eType pstType = classKindToTypeInfo(kind);
		List references = new ArrayList();
		IToken nameToken = name.getFirstToken();
		char[] newSymbolName = EMPTY_STRING; 
		List templateIdArgList = null;
		boolean isTemplateId = false;
		if (name.getSegmentCount() != 1) // qualified name
		{
			ITokenDuple containerSymbolName = name.getLeadingSegments();
			if( containerSymbolName == null ){
				//null means globally qualified
				currentScopeSymbol = currentScopeSymbol.getSymbolTable().getCompilationUnit();
			} else {
				currentScopeSymbol = (IContainerSymbol) lookupQualifiedName(
						currentScopeSymbol, containerSymbolName, references, true);
			}
			if (currentScopeSymbol == null)
				handleProblem(IProblem.SEMANTIC_NAME_NOT_FOUND,
						containerSymbolName.toCharArray(), containerSymbolName
								.getFirstToken().getOffset(),
						containerSymbolName.getLastToken().getEndOffset(),
						containerSymbolName.getLastToken().getLineNumber(), true);
			nameToken = name.getLastSegment().getFirstToken();
		}
		//template-id
		List[] array = name.getTemplateIdArgLists();
		if (array != null) {
			isTemplateId = true;
			templateIdArgList = array[array.length - 1];
		}
		newSymbolName = nameToken.getCharImage();
		ISymbol checkSymbol = null;
		if (!isTemplateId) {
			try {
				if (isFriend) {
					checkSymbol = ((IDerivableContainerSymbol) currentScopeSymbol)
							.lookupForFriendship(newSymbolName);
				} else {
					checkSymbol = currentScopeSymbol.elaboratedLookup(pstType,
							newSymbolName);
				}
			} catch (ParserSymbolTableException e) {
				handleProblem(e.createProblemID(), nameToken.getCharImage(),
						nameToken.getOffset(), nameToken.getEndOffset(),
						nameToken.getLineNumber(), true);
			}
		}
		List args = null;
		if (isTemplateId) {
			args = getTemplateArgList(templateIdArgList);
		}
		if (scope instanceof IASTTemplateInstantiation) {
			if (isTemplateId) {
				checkSymbol = pst.newDerivableContainerSymbol(newSymbolName,
						pstType);
				try {
					currentScopeSymbol.addTemplateId(checkSymbol, args);
				} catch (ParserSymbolTableException e) {
					handleProblem(e.createProblemID(), nameToken.getCharImage(),
							nameToken.getOffset(), nameToken.getEndOffset(),
							nameToken.getLineNumber(), true);
				}
			} else {
				handleProblem(IProblem.SEMANTIC_INVALID_TEMPLATE, nameToken
						.getCharImage());
			}
			checkSymbol = ((ASTTemplateInstantiation) scope)
					.getInstanceSymbol();
		} else if (checkSymbol == null) {
			checkSymbol = pst.newDerivableContainerSymbol(newSymbolName,
					pstType);
			checkSymbol.setIsForwardDeclaration(true);
			try {
				if (isFriend) {
					((IDerivableContainerSymbol) originalScope).addFriend(checkSymbol);
				} else {
					if (!isTemplateId)
						currentScopeSymbol.addSymbol(checkSymbol);
					else
						currentScopeSymbol.addTemplateId(checkSymbol, args);
				}
			} catch (ParserSymbolTableException e1) {
				handleProblem(e1.createProblemID(), nameToken.getCharImage(),
						nameToken.getOffset(), nameToken.getEndOffset(),
						nameToken.getLineNumber(), true);
			}
			ASTElaboratedTypeSpecifier elab = new ASTElaboratedTypeSpecifier(
					checkSymbol, kind, startingOffset, startingLine, name
							.getFirstToken().getOffset(), name.getLastToken()
							.getEndOffset(), name.getLastToken()
							.getLineNumber(), endOffset, endingLine,
					references, isForewardDecl, filename);
			attachSymbolExtension(checkSymbol, elab, !isForewardDecl);
			return elab;
		} else if (isFriend) {
			((IDerivableContainerSymbol) originalScope).addFriend(checkSymbol);
		}
		if (checkSymbol != null) {
			if (scope instanceof IASTTemplateInstantiation) {
				addReference(references, createReference(checkSymbol,
						newSymbolName, nameToken.getOffset()));
			}
			if( checkSymbol instanceof ITemplateSymbol ){
				checkSymbol = ((ITemplateSymbol)checkSymbol).getTemplatedSymbol();
			}
			if (checkSymbol.getASTExtension().getPrimaryDeclaration() instanceof IASTClassSpecifier
					|| checkSymbol.getASTExtension().getPrimaryDeclaration() instanceof IASTEnumerationSpecifier || 
					checkSymbol.getASTExtension().getPrimaryDeclaration() instanceof IASTElaboratedTypeSpecifier ) {
				ASTElaboratedTypeSpecifier elab = new ASTElaboratedTypeSpecifier(
						checkSymbol, kind, startingOffset, startingLine, name
								.getFirstToken().getOffset(), name
								.getLastToken().getEndOffset(), name
								.getLastToken().getLineNumber(), endOffset,
						endingLine, references, isForewardDecl, filename);
				attachSymbolExtension(checkSymbol, elab, !isForewardDecl);
				return elab;
			}
		} else {
			handleProblem(IProblem.SEMANTIC_NAME_NOT_FOUND, newSymbolName,
					nameToken.getOffset(), nameToken.getEndOffset(), nameToken
							.getLineNumber(), true);
		}
	
		
//		assert false : this;
		return null;
    }

    protected ParserSymbolTable pst;


    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNamespaceAlias(org.eclipse.cdt.core.parser.ast.IASTScope,
	 *      java.lang.String, org.eclipse.cdt.core.parser.ITokenDuple, int, int,
	 *      int)
	 */
    public IASTNamespaceAlias createNamespaceAlias(IASTScope scope, char[] identifier, ITokenDuple alias, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endOffset, int endingLine) throws ASTSemanticException
    {
    	setFilename( alias );
        IContainerSymbol startingSymbol = scopeToSymbol(scope);
        List references = new ArrayList();
        
        ISymbol namespaceSymbol = lookupQualifiedName( startingSymbol, alias, references, true );
        
        if( namespaceSymbol.getType() != ITypeInfo.t_namespace )
        	handleProblem( IProblem.SEMANTIC_INVALID_OVERLOAD, alias.toCharArray(), startingOffset, endOffset, startingLine, true );
        
        ISymbol newSymbol = pst.newContainerSymbol( identifier, ITypeInfo.t_namespace );
        newSymbol.setForwardSymbol( namespaceSymbol );
        
        try
        {
            startingSymbol.addSymbol( newSymbol );
        }
        catch (ParserSymbolTableException e)
        {
        	handleProblem( e.createProblemID(), identifier, startingOffset, endOffset, startingLine, true );
        }
        
        ASTNamespaceAlias astAlias = new ASTNamespaceAlias(
        	newSymbol, alias.toCharArray(), (IASTNamespaceDefinition)namespaceSymbol.getASTExtension().getPrimaryDeclaration(), 
        	startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, endOffset, endingLine, references, filename ); 
        attachSymbolExtension( newSymbol, astAlias, true );
        return astAlias;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createNewCodeBlock(org.eclipse.cdt.core.parser.ast.IASTScope)
	 */
	public IASTCodeScope createNewCodeBlock(IASTScope scope) {
		IContainerSymbol symbol = scopeToSymbol( scope );
		
		IContainerSymbol newScope = pst.newContainerSymbol(EMPTY_STRING, ITypeInfo.t_block); 
		newScope.setContainingSymbol(symbol);
		newScope.setIsTemplateMember( symbol.isTemplateMember() );
		
		ASTCodeScope codeScope = new ASTCodeScope( newScope );
		attachSymbolExtension( newScope, codeScope, true );
		return codeScope;
	}

	public IASTScope getDeclaratorScope(IASTScope scope, ITokenDuple duple){
		if( duple != null && duple.getSegmentCount() > 1){
			
			IContainerSymbol ownerScope = scopeToSymbol( scope );
			ISymbol symbol;
			
			try {
				symbol = lookupQualifiedName( ownerScope, duple.getLeadingSegments(), null, false, LookupType.FORDEFINITION );
			} catch (ASTSemanticException e) {
				return scope;
			}
			
			IContainerSymbol parentScope = null;
			
			if( symbol instanceof IContainerSymbol )
				parentScope = (IContainerSymbol) symbol;
			else if( symbol instanceof IDeferredTemplateInstance )
				parentScope = ((IDeferredTemplateInstance) symbol).getTemplate().getTemplatedSymbol();
			
			if( parentScope != null && parentScope.getASTExtension() != null ){
				if( scope instanceof IASTTemplateDeclaration || scope instanceof IASTTemplateSpecialization ){
					symbol = scopeToSymbol( scope );
					if( symbol instanceof ITemplateFactory ){
						symbol.setContainingSymbol( parentScope );
					}
					return scope;
				}
				return (IASTScope)parentScope.getASTExtension().getPrimaryDeclaration();
			}
		}
		
		return scope;
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
					null,
					false);
		} catch (ASTSemanticException e) {
			// won't get thrown
		} 
		if( lookupSymbol == null ) return false;
		if( lookupSymbol.isType( ITypeInfo.t_type, ITypeInfo.t_enumeration ) ||
			(lookupSymbol.isType( ITypeInfo.t_templateParameter ) && lookupSymbol.getTypeInfo().getTemplateParameterType() == ITypeInfo.t_typeName ) ||
			(lookupSymbol.getASTExtension() != null && lookupSymbol.getASTExtension().getPrimaryDeclaration() instanceof IASTTypedefDeclaration ) )
		{
			return true;
		}
		return false;
	}

    public IASTParameterDeclaration createParameterDeclaration(boolean isConst, boolean isVolatile, IASTTypeSpecifier typeSpecifier, List pointerOperators, List arrayModifiers, List parameters, ASTPointerOperator pointerOp, char[] parameterName, IASTInitializerClause initializerClause, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endingLine, char[] fn)
    {
    	setFilename(fn);
        return new ASTParameterDeclaration( null, isConst, isVolatile, typeSpecifier, pointerOperators, arrayModifiers, parameters, pointerOp, parameterName, initializerClause, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, endingOffset, endingLine, filename );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#createTypeId(org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type, org.eclipse.cdt.core.parser.ITokenDuple, java.util.List, java.util.List)
     */
    public IASTTypeId createTypeId(IASTScope scope, Type kind, boolean isConst, boolean isVolatile, boolean isShort, 
	boolean isLong, boolean isSigned, boolean isUnsigned, boolean isTypename, ITokenDuple name, List pointerOps, List arrayMods, char[] completeSignature) throws ASTSemanticException
    {
    	if( kind != Type.CLASS_OR_TYPENAME )
    	{
    		IASTTypeId check = (IASTTypeId) typeIdCache.get( completeSignature );
    		if( check != null )
    			return check;
    	}
        ASTTypeId result = 
        	new ASTTypeId( kind, name, pointerOps, arrayMods, completeSignature,   
        	isConst, isVolatile, isUnsigned, isSigned, isShort, isLong, isTypename );
        result.setTypeSymbol( createSymbolForTypeId( scope, result ) );
        if( kind != Type.CLASS_OR_TYPENAME )
        	typeIdCache.put( completeSignature, result );
        return result;
    }

	 /**
	  * @param id
	  * @return
	  */
	 public static ITypeInfo.eType getTypeKind(IASTTypeId id)
	 {
		 IASTSimpleTypeSpecifier.Type type = id.getKind();
		 if( type == IASTSimpleTypeSpecifier.Type.BOOL )
			 return ITypeInfo.t_bool;
		 else if( type == IASTSimpleTypeSpecifier.Type._BOOL )
		     return ITypeInfo.t__Bool;
		 else if( type == IASTSimpleTypeSpecifier.Type.CHAR )
			 return ITypeInfo.t_char;
		 else if ( type == IASTSimpleTypeSpecifier.Type.WCHAR_T )
		 	 return ITypeInfo.t_wchar_t;
		 else if( type == IASTSimpleTypeSpecifier.Type.DOUBLE )
			 return ITypeInfo.t_double;
		 else if( type == IASTSimpleTypeSpecifier.Type.FLOAT )
			 return ITypeInfo.t_float;
		 else if( type == IASTSimpleTypeSpecifier.Type.INT )
			 return ITypeInfo.t_int;
		 else if( type == IASTSimpleTypeSpecifier.Type.VOID )
			 return ITypeInfo.t_void;
		 else if( id.isShort() || id.isLong() || id.isUnsigned() || id.isSigned() )
		 	return ITypeInfo.t_int;
		 else 
			 return ITypeInfo.t_type;
	 }
	 
	protected ISymbol createSymbolForTypeId( IASTScope scope, IASTTypeId id ) throws ASTSemanticException
	{
		if( id == null ) return null;
    	
    	ASTTypeId typeId = (ASTTypeId)id;
		ISymbol result = pst.newSymbol( EMPTY_STRING, CompleteParseASTFactory.getTypeKind(id)); 
    	
    	result.getTypeInfo().setBit( id.isConst(), ITypeInfo.isConst );
		result.getTypeInfo().setBit( id.isVolatile(), ITypeInfo.isVolatile );
		
		result.getTypeInfo().setBit( id.isShort(), ITypeInfo.isShort);
		result.getTypeInfo().setBit( id.isLong(), ITypeInfo.isLong);
		result.getTypeInfo().setBit( id.isUnsigned(), ITypeInfo.isUnsigned);
		result.getTypeInfo().setBit( id.isSigned(), ITypeInfo.isSigned );
		
		List refs = new ArrayList();
		if( result.getType() == ITypeInfo.t_type )
		{
			ISymbol typeSymbol = lookupQualifiedName( scopeToSymbol(scope), typeId.getTokenDuple(), refs, true );
			if( typeSymbol == null /*|| typeSymbol.getType() == TypeInfo.t_type*/ )
			{
				freeReferences( refs );
				handleProblem( scope, IProblem.SEMANTIC_INVALID_TYPE, typeId.getTypeOrClassNameCharArray() );
			}
            result.setTypeSymbol( typeSymbol );
            typeId.addReferences( refs );
		}		
		
		setPointerOperators( result, ((ASTTypeId)id).getPointerOperatorsList(), ((ASTTypeId)id).getArrayModifiersList() );
		return result;
	}

    /**
	 * @param refs
	 */
	private void freeReferences(List refs) {
		if( refs == null || refs.isEmpty() ) return;
		refs.clear();
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTFactory#signalEndOfClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
     */
    public void signalEndOfClassSpecifier(IASTClassSpecifier astClassSpecifier)
    {
    	ASTClassSpecifier astImplementation = (ASTClassSpecifier)astClassSpecifier;
		try
        { 
			((IDerivableContainerSymbol)(astImplementation).getSymbol()).addCopyConstructor();
        }
        catch (ParserSymbolTableException e)
        {
        	// do nothing, this is best effort
        }
        
        astImplementation.setProcessingUnresolvedReferences( true );
        List unresolved = astImplementation.getUnresolvedReferences();
        List references = new ArrayList();
        int size = unresolved.size();
        for( int i = 0; i < size; i++ )
        {	
        	UnresolvedReferenceDuple duple = (UnresolvedReferenceDuple) unresolved.get(i);
        	
        	try
			{
        		lookupQualifiedName( duple.getScope(), duple.getName(), references, false );
        	}
        	catch( ASTSemanticException ase )
			{
        	}
        }
        
        astImplementation.setProcessingUnresolvedReferences( false );
        
        if( ! references.isEmpty() )
        	astImplementation.setExtraReferences( references );
        
    }

    public IASTInitializerClause createInitializerClause(IASTScope scope, IASTInitializerClause.Kind kind, IASTExpression assignmentExpression, List initializerClauses, List designators)
    {
    	return new ASTInitializerClause( kind, assignmentExpression, initializerClauses, designators );
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#lookupSymbolInContext(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public IASTNode lookupSymbolInContext(IASTScope scope, ITokenDuple duple, IASTNode reference) throws ASTNotImplementedException {
		ISymbol s = null;
		if( reference == null ) {
			try {
				s = lookupQualifiedName( scopeToSymbol( scope ), duple, null, false );
			} catch (ASTSemanticException e) {
			}
		}
		else
		{
			if( reference instanceof ASTExpression )
			{
				ASTExpression expression = (ASTExpression) reference;
				final char[] dupleAsCharArray = duple.toCharArray();
				if( expression.getExpressionKind() == IASTExpression.Kind.ID_EXPRESSION &&
					CharArrayUtils.equals( expression.getIdExpressionCharArray(), dupleAsCharArray ))
				{
					try {
						s = lookupQualifiedName( scopeToSymbol( scope ), duple, null, false );
					} catch (ASTSemanticException e1) {
					}
				}
				else if( expression.getExpressionKind() == IASTExpression.Kind.NEW_NEWTYPEID || 
						expression.getExpressionKind() == IASTExpression.Kind.NEW_TYPEID )
				{
					IContainerSymbol classSymbol = null;
					try {
						classSymbol = (IContainerSymbol) lookupQualifiedName(scopeToSymbol( scope ), duple, null, false );
					} catch (ASTSemanticException e) {
					}
					if( classSymbol != null && classSymbol.getTypeInfo().checkBit( ITypeInfo.isTypedef ) ){
						ITypeInfo info = classSymbol.getTypeInfo().getFinalType( pst.getTypeInfoProvider() );
						classSymbol = (IContainerSymbol) info.getTypeSymbol();
						pst.getTypeInfoProvider().returnTypeInfo( info );
					}
					if( classSymbol == null || ! (classSymbol instanceof IDerivableContainerSymbol ) ){
						return null;
					}
					
					List parameters = new ArrayList();
					ASTNewDescriptor newDescriptor = (ASTNewDescriptor) expression.getNewExpressionDescriptor();
					List newInitializerExpressions = newDescriptor.getNewInitializerExpressionsList();
					int size = newInitializerExpressions.size();
					for( int i = 0; i < size; i++ )
					{
						ASTExpression expressionList = (ASTExpression) newInitializerExpressions.get(i);
						while( expressionList != null ){
							parameters.add( expressionList.getResultType().getResult() );
							expressionList = (ASTExpression) expressionList.getRHSExpression();
						}
					}
					
					try {
						s = ((IDerivableContainerSymbol)classSymbol).lookupConstructor( parameters );
					} catch (ParserSymbolTableException e1) {
						return null;
					}

				}
				else if( expression.getExpressionKind() == Kind.POSTFIX_FUNCTIONCALL && 
						 CharArrayUtils.equals( expression.getLHSExpression().getIdExpressionCharArray(), dupleAsCharArray ))
				{
					try {
						ISymbol symbol = getExpressionSymbol( scope, expression.getExpressionKind(), expression.getLHSExpression(), expression.getRHSExpression(), null, null );
						if( symbol == null) return null;
						return symbol.getASTExtension().getPrimaryDeclaration();
					} catch (ASTSemanticException e) {
						return null;
					}
					
					
				}
				else
				{
					ASTExpression ownerExpression = expression.findOwnerExpressionForIDExpression( duple );
					if( ownerExpression == null ) return null;
					if( ownerExpression.getExpressionKind().isPostfixMemberReference() )
					{
						try {
							s = lookupQualifiedName( getSearchScope(ownerExpression.getExpressionKind(), ownerExpression.getLHSExpression(), scopeToSymbol(scope)), duple, null, false );
						} catch (ASTSemanticException e) {
							return null;
						}
					}
					else
					{
						try {
							s = lookupQualifiedName( scopeToSymbol( scope ), duple, null, false );
						} catch (ASTSemanticException e1) {
						}						
					}
				}
			}
		}
		if ( s == null ) return null;
		return s.getASTExtension().getPrimaryDeclaration();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#getNodeForThisExpression(org.eclipse.cdt.core.parser.ast.IASTExpression)
	 */
	public IASTNode expressionToMostPreciseASTNode(IASTScope scope, IASTExpression expression) {
		if( expression == null ) return null;
		if( expression.getExpressionKind() == IASTExpression.Kind.ID_EXPRESSION )
		{
			if( expression instanceof ASTExpression)
			{
				try {
					return lookupSymbolInContext(scope, ((ASTIdExpression)expression).getIdExpressionTokenDuple(), null);
				} catch (ASTNotImplementedException e) {
//	            	assert false : e;
				}
			}
		}
	
		return expression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#validateIndirectMemberOperation(org.eclipse.cdt.core.parser.ast.IASTNode)
	 */
	public boolean validateIndirectMemberOperation(IASTNode node) {
		List pointerOps = null;
		TypeInfoProvider provider = pst.getTypeInfoProvider();
		ITypeInfo typeInfo = null;
		if( ( node instanceof ISymbolOwner ) )
		{
			ISymbol symbol = ((ISymbolOwner) node).getSymbol();
			typeInfo = symbol.getTypeInfo().getFinalType( provider );
			pointerOps = typeInfo.getPtrOperators();
			provider.returnTypeInfo( typeInfo );
		}
		else if( node instanceof ASTExpression )
		{
			ITypeInfo info = ((ASTExpression)node).getResultType().getResult();
			if( info != null ){
				typeInfo = info.getFinalType( provider );
				pointerOps = typeInfo.getPtrOperators();
				provider.returnTypeInfo( typeInfo );
			}
		}
		else
			return false;
		
		if( pointerOps == null || pointerOps.isEmpty() ) return false;
		ITypeInfo.PtrOp lastOperator = (ITypeInfo.PtrOp) pointerOps.get( pointerOps.size() - 1 );
		if( lastOperator.getType() == ITypeInfo.PtrOp.t_array || lastOperator.getType() == ITypeInfo.PtrOp.t_pointer ) return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#validateDirectMemberOperation(org.eclipse.cdt.core.parser.ast.IASTNode)
	 */
	public boolean validateDirectMemberOperation(IASTNode node) {
		List pointerOps = null;
		if( ( node instanceof ISymbolOwner ) )
		{
			ISymbol symbol = ((ISymbolOwner) node).getSymbol();
			TypeInfoProvider provider = pst.getTypeInfoProvider();
			ITypeInfo info = symbol.getTypeInfo().getFinalType( provider );
			pointerOps = info.getPtrOperators();
			provider.returnTypeInfo( info );
		}
		else if( node instanceof ASTExpression )
		{
		    ITypeInfo info = ((ASTExpression)node).getResultType().getResult();
			if( info != null ){
			    TypeInfoProvider provider = pst.getTypeInfoProvider();
				info = info.getFinalType( provider );
				pointerOps = info.getPtrOperators();
				provider.returnTypeInfo( info );
			}
		}
		else
			return false;
		
		if( pointerOps == null || pointerOps.isEmpty() ) return true;
		ITypeInfo.PtrOp lastOperator = (ITypeInfo.PtrOp) pointerOps.get( pointerOps.size() - 1 );
		if( lastOperator.getType() == ITypeInfo.PtrOp.t_reference ) return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTFactory#constructExpressions(boolean)
	 */
	public void constructExpressions(boolean flag) {
		//ignore
	}


	/**
	 * @return
	 */
	public boolean validateCaches() {
		return (pst.getTypeInfoProvider().numAllocated() == 0);
	}

}
