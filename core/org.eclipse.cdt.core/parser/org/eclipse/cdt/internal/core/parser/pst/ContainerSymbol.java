/*******************************************************************************
 * Copyright (c) 2003,2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Nov 4, 2003
 */
 
package org.eclipse.cdt.internal.core.parser.pst;

import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.IASTMember;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable.Command;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable.LookupData;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ContainerSymbol extends BasicSymbol implements IContainerSymbol {

	protected ContainerSymbol( ParserSymbolTable table, String name ){
		super( table, name );
	}
	
	protected ContainerSymbol( ParserSymbolTable table, String name, ISymbolASTExtension obj ){
		super( table, name, obj );
	}
	
	protected ContainerSymbol( ParserSymbolTable table, String name, TypeInfo.eType typeInfo ){
		super( table, name, typeInfo );
	}
	
	public Object clone(){
		ContainerSymbol copy = (ContainerSymbol)super.clone();
			
		copy._usingDirectives  =  (_usingDirectives != ParserSymbolTable.EMPTY_LIST) ? (LinkedList) _usingDirectives.clone() : _usingDirectives;
		
		if( getSymbolTable().getParserMode() == ParserMode.COMPLETION_PARSE )
			copy._containedSymbols = ( _containedSymbols != ParserSymbolTable.EMPTY_MAP )? (Map)((TreeMap) _containedSymbols).clone() : _containedSymbols;
		else 
			copy._containedSymbols = ( _containedSymbols != ParserSymbolTable.EMPTY_MAP )? (Map)((HashMap) _containedSymbols).clone() : _containedSymbols;
		
		copy._contents = (_contents != ParserSymbolTable.EMPTY_LIST) ? (LinkedList) _contents.clone() : _contents;
		
		return copy;	
	}
	
	public ISymbol instantiate( ITemplateSymbol template, Map argMap ) throws ParserSymbolTableException{
		if( !isTemplateMember() || template == null ){
			return null;
		}
		
		ContainerSymbol newContainer = (ContainerSymbol) super.instantiate( template, argMap );

		Iterator iter = getContentsIterator();
	
		newContainer.getContainedSymbols().clear();
		if( !newContainer._contents.isEmpty()  ){
			newContainer._contents.clear();
			
			IExtensibleSymbol containedSymbol = null;
			ISymbol newSymbol = null;
			
			while( iter.hasNext() ){
				containedSymbol = (IExtensibleSymbol) iter.next();
				
				if( containedSymbol instanceof IUsingDirectiveSymbol ){
					newContainer._contents.add( containedSymbol );
				} else {
					ISymbol symbol = (ISymbol) containedSymbol;
					if( symbol.isForwardDeclaration() && symbol.getTypeSymbol() != null ){
						continue;
					}
					
					if( !template.getExplicitSpecializations().isEmpty() ){
						List argList = new LinkedList();
						boolean hasAllParams = true;
						Iterator templateParams = template.getParameterList().iterator();
						while( templateParams.hasNext() ){
							Object obj = argMap.get( templateParams.next() );
							if( obj == null ){
								hasAllParams = false;
								break;
							}
							argList.add( obj );
						}
						if( hasAllParams){
							ISymbol temp = TemplateEngine.checkForTemplateExplicitSpecialization( template, symbol, argList );
							if( temp != null )
								containedSymbol = temp;
						}
					}
					
					Map instanceMap = argMap;
					if( !template.getDefinitionParameterMap().isEmpty() && 
						template.getDefinitionParameterMap().containsKey( containedSymbol ) )
					{
						Map defMap = (Map) template.getDefinitionParameterMap().get( containedSymbol );
						instanceMap = new HashMap();
						Iterator i = defMap.keySet().iterator();
						while( i.hasNext() ){
							ISymbol p = (ISymbol) i.next();
							instanceMap.put( p, argMap.get( defMap.get( p ) ) );
						}
					}
					
					newSymbol = ((ISymbol)containedSymbol).instantiate( template, instanceMap );
					newSymbol.setContainingSymbol( newContainer );
					newContainer._contents.add( newSymbol );
					
					if( newSymbol instanceof IParameterizedSymbol && newSymbol.isType( TypeInfo.t_constructor ) ){
						collectInstantiatedConstructor( (IParameterizedSymbol) containedSymbol );	
					} else {
						if( newContainer.getContainedSymbols().containsKey( newSymbol.getName() ) ){
							Object obj = newContainer.getContainedSymbols().get( newSymbol.getName() );
							if( obj instanceof List ){
								((List) obj).add( newSymbol );
							} else {
								List list = new LinkedList();
								list.add( obj );
								list.add( newSymbol );
								newContainer.putInContainedSymbols( newSymbol.getName(), list );
							}
						} else {
							newContainer.putInContainedSymbols( newSymbol.getName(), newSymbol );
						}
					}
				}
			}
		}
		
		return newContainer;	
	}
	
	protected void collectInstantiatedConstructor( IParameterizedSymbol constructor ){
		throw new ParserSymbolTableError();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addSymbol(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void addSymbol( ISymbol obj ) throws ParserSymbolTableException{
		IContainerSymbol containing = this;
		
		//handle enumerators
		if( obj.getType() == TypeInfo.t_enumerator ){
			//a using declaration of an enumerator will not be contained in a
			//enumeration.
			if( containing.getType() == TypeInfo.t_enumeration ){
				//Following the closing brace of an enum-specifier, each enumerator has the type of its 
				//enumeration
				obj.setTypeSymbol( containing );
				//Each enumerator is declared in the scope that immediately contains the enum-specifier	
				containing = containing.getContainingSymbol();
			}
		}
	
		if( obj.isType( TypeInfo.t_template ) ){
			if( ! TemplateEngine.canAddTemplate( containing, (ITemplateSymbol) obj ) ) {
				throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
			}
		}
		
		//in C, structs, unions, enums don't nest
		if( getSymbolTable().getLanguage() == ParserLanguage.C ){
			if( obj.isType( TypeInfo.t_struct, TypeInfo.t_enumeration ) ){
				containing = getScopeForCTag( containing );
			}
		}
		
		//14.6.1-4 A Template parameter shall not be redeclared within its scope.
		if( isTemplateMember() || isType( TypeInfo.t_template ) ){
			if( TemplateEngine.alreadyHasTemplateParameter( this, obj.getName() ) ){
				throw new ParserSymbolTableException( ParserSymbolTableException.r_RedeclaredTemplateParam );	
			}
		}
	
		boolean unnamed = obj.getName().equals( ParserSymbolTable.EMPTY_NAME );
	
		Object origObj = null;
	
		obj.setContainingSymbol( containing );

		//does this name exist already?
		origObj = containing.getContainedSymbols().get( obj.getName() );
	
		if( origObj != null )
		{
			ISymbol origDecl = null;
			LinkedList  origList = null;
	
			if( origObj instanceof ISymbol ){
				origDecl = (ISymbol)origObj;
			} else if( origObj.getClass() == LinkedList.class ){
				origList = (LinkedList)origObj;
			} else {
				throw new ParserSymbolTableError( ParserSymbolTableError.r_InternalError );
			}
		
			boolean validOverride =  ( !unnamed ? ( (origList == null) ? ParserSymbolTable.isValidOverload( origDecl, obj ) 
																	   : ParserSymbolTable.isValidOverload( origList, obj ) )
											    : true );
			
			if( unnamed || validOverride )
			{	
				if( origList == null ){
					origList = new LinkedList();
					origList.add( origDecl );
					origList.add( obj );
			
					((ContainerSymbol)containing).putInContainedSymbols( obj.getName(), origList );
				} else	{
					origList.add( obj );
					//origList is already in _containedDeclarations
				}
			} else {
				throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidOverload );
			}
		} else {
			((ContainerSymbol)containing).putInContainedSymbols( obj.getName(), obj );
		}
	
		obj.setIsTemplateMember( isTemplateMember() || getType() == TypeInfo.t_template );
		
		addToContents( obj );
		
		Command command = new AddSymbolCommand( obj, containing );
		getSymbolTable().pushCommand( command );
	}

	public boolean removeSymbol( ISymbol symbol ){
		boolean removed = false;
		
		Map contained = getContainedSymbols();
		
		if( symbol != null && contained.containsKey( symbol.getName() ) ){
			Object obj = contained.get( symbol.getName() );
			if( obj instanceof ISymbol ){
				if( obj == symbol ){
					contained.remove( symbol.getName() );
					removed = true;
				}
			} else if ( obj instanceof List ){
				List list = (List) obj;
				if( list.remove( symbol ) ){
					if( list.size() == 1 ){
						contained.put( symbol.getName(), list.get( 0 ) );
					}
					removed = true;
				}
			}
		}
		
		if( removed ){
			ListIterator iter = getContents().listIterator( getContents().size() );
			while( iter.hasPrevious() ){
				if( iter.previous() == symbol ){
					iter.remove();
					break;
				}
			}
		}
		
		return removed;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#hasUsingDirectives()
	 */
	public boolean hasUsingDirectives(){
		return !_usingDirectives.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#getUsingDirectives()
	 */
	public List getUsingDirectives(){
		return _usingDirectives;
	}
	
	protected void addToUsingDirectives( IExtensibleSymbol symbol ){
		if( _usingDirectives == ParserSymbolTable.EMPTY_LIST )
			_usingDirectives = new LinkedList();
		_usingDirectives.add( symbol );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addUsingDirective(org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol)
	 */
	public IUsingDirectiveSymbol addUsingDirective( IContainerSymbol namespace ) throws ParserSymbolTableException{
		if( namespace.getType() != TypeInfo.t_namespace ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidUsing );
		}
		//7.3.4 A using-directive shall not appear in class scope
		if( isType( TypeInfo.t_class, TypeInfo.t_union ) ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidUsing );
		}
		
		//handle namespace aliasing
		ISymbol alias = namespace.getTypeSymbol();
		if( alias != null && alias.isType( TypeInfo.t_namespace ) ){
			namespace = (IContainerSymbol) alias;
		}
		
		IUsingDirectiveSymbol usingDirective = new UsingDirectiveSymbol( getSymbolTable(), namespace );
		
		addToUsingDirectives( usingDirective );
		addToContents( usingDirective );
		
		Command command = new AddUsingDirectiveCommand( this, usingDirective );
		getSymbolTable().pushCommand( command );
		
		return usingDirective;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addUsingDeclaration(java.lang.String)
	 */
	/**
	 * addUsingDeclaration
	 * @param obj
	 * @throws ParserSymbolTableException
	 * 
	 * 7.3.3-9  The entity declared by a using-declaration shall be known in the
	 * context using it according to its definition at the point of the using-
	 * declaration.  Definitions added to the namespace after the using-
	 * declaration are not considered when a use of the name is made.
	 * 
	 * 7.3.3-4 A using-declaration used as a member-declaration shall refer to a
	 * member of a base class of the class being defined, shall refer to a
	 * member of an anonymous union that is a member of a base class of the
	 * class being defined, or shall refer to an enumerator for an enumeration
	 * type that is a member of a base class of the class being defined.
	 */
	public IUsingDeclarationSymbol addUsingDeclaration( String name ) throws ParserSymbolTableException {
		return addUsingDeclaration( name, null );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addUsingDeclaration(java.lang.String, org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol)
	 */
	public IUsingDeclarationSymbol addUsingDeclaration( String name, IContainerSymbol declContext ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, TypeInfo.t_any );

		if( declContext != null ){				
			data.qualified = true;
			ParserSymbolTable.lookup( data, declContext );
		} else {
			ParserSymbolTable.lookup( data, this );
		}

		//figure out which declaration we are talking about, if it is a set of functions,
		//then they will be in data.foundItems (since we provided no parameter info);
		ISymbol symbol = null;
		ISymbol clone = null;
		Iterator iter = null;
		
		try{
			symbol = ParserSymbolTable.resolveAmbiguities( data );
		} catch ( ParserSymbolTableException e ) {
			if( e.reason != ParserSymbolTableException.r_UnableToResolveFunction ){
				throw e;
			}
		}

		if( symbol == null && (data.foundItems == null || data.foundItems.isEmpty()) ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidUsing );				
		}

		if( symbol == null ){
			Object object = data.foundItems.get( data.name );
			iter = ( object instanceof List ) ? ((List) object).iterator() : null;
			symbol = ( iter != null && iter.hasNext() ) ? (ISymbol)iter.next() : null;
		}

		List usingDecs = new LinkedList();
		List usingRefs = new LinkedList();
		
		UsingDeclarationSymbol usingDeclaration = new UsingDeclarationSymbol( getSymbolTable(), usingRefs, usingDecs );
		boolean addedUsingToContained = false;
		
		while( symbol != null ){
			if( ParserSymbolTable.okToAddUsingDeclaration( symbol, this ) ){
				if( ! addedUsingToContained ){
					addToContents( usingDeclaration );
					addedUsingToContained = true;
				}
				clone = (ISymbol) symbol.clone(); //7.3.3-9
				addSymbol( clone );
			} else {
				throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidUsing );
			}
			
			usingDecs.add( clone );
			usingRefs.add( symbol );
			
			if( iter != null && iter.hasNext() ){
				symbol = (ISymbol) iter.next();
			} else {
				symbol = null;
			}
		}
		
		return usingDeclaration;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#getContainedSymbols()
	 */
	public Map getContainedSymbols(){
		return _containedSymbols;
	}
	
	protected void putInContainedSymbols( String key, Object obj ){
		if( _containedSymbols == ParserSymbolTable.EMPTY_MAP ){
			if( getSymbolTable().getParserMode() == ParserMode.COMPLETION_PARSE ){
				_containedSymbols = new TreeMap( new SymbolTableComparator() );
			} else {
				_containedSymbols = new HashMap( );
			}
		}
		_containedSymbols.put( key, obj );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#elaboratedLookup(org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType, java.lang.String)
	 */
	public ISymbol elaboratedLookup( TypeInfo.eType type, String name ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, type );
	
		ParserSymbolTable.lookup( data, this );
	
		ISymbol found = ParserSymbolTable.resolveAmbiguities( data );
		
		if( isTemplateMember() && found instanceof ITemplateSymbol ) {
			boolean areWithinTemplate = false;
			IContainerSymbol container = getContainingSymbol();
			while( container != null ){
				if( container == found ){
					areWithinTemplate = true;
					break;
				}
				container = container.getContainingSymbol();
			}
			if( areWithinTemplate )
				return TemplateEngine.instantiateWithinTemplateScope( this, (ITemplateSymbol) found );
		}
		
		return found;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookup(java.lang.String)
	 */
	public ISymbol lookup( String name ) throws ParserSymbolTableException {
		LookupData data = new LookupData( name, TypeInfo.t_any );
	
		ParserSymbolTable.lookup( data, this );
	
		ISymbol found = ParserSymbolTable.resolveAmbiguities( data );
		
		if( isTemplateMember() && found instanceof ITemplateSymbol ) {
			return TemplateEngine.instantiateWithinTemplateScope( this, (ITemplateSymbol) found );
		}
		
		return found;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupMemberForDefinition(java.lang.String)
	 */
	/**
	 * LookupMemberForDefinition
	 * @param name
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 * 
	 * In a definition for a namespace member in which the declarator-id is a
	 * qualified-id, given that the qualified-id for the namespace member has
	 * the form "nested-name-specifier unqualified-id", the unqualified-id shall
	 * name a member of the namespace designated by the nested-name-specifier.
	 * 
	 * ie:
	 * you have this:
	 * namespace A{    
	 *    namespace B{       
	 *       void  f1(int);    
	 *    }  
	 *    using  namespace B; 
	 * }
	 * 
	 * if you then do this 
	 * void A::f1(int) { ... } //ill-formed, f1 is not a member of A
	 * but, you can do this (Assuming f1 has been defined elsewhere)
	 * A::f1( 1 );  //ok, finds B::f1
	 * 
	 * ie, We need a seperate lookup function for looking up the member names
	 * for a definition.
	 */
	public ISymbol lookupMemberForDefinition( String name ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, TypeInfo.t_any );
		data.qualified = true;
		
		IContainerSymbol container = this;
		
		//handle namespace aliases
		if( container.isType( TypeInfo.t_namespace ) ){
			ISymbol symbol = container.getTypeSymbol();
			if( symbol != null && symbol.isType( TypeInfo.t_namespace ) ){
				container = (IContainerSymbol) symbol;
			}
		}
		
		data.foundItems = ParserSymbolTable.lookupInContained( data, container );
		if( data.foundItems != null )
			return ParserSymbolTable.resolveAmbiguities( data );
		return null;
	}

	public IParameterizedSymbol lookupMethodForDefinition( String name, List parameters ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, TypeInfo.t_any );
		data.qualified = true;
		data.parameters = ( parameters == null ) ? ParserSymbolTable.EMPTY_LIST : parameters;
		data.exactFunctionsOnly = true;
		
		IContainerSymbol container = this;
		
		//handle namespace aliases
		if( container.isType( TypeInfo.t_namespace ) ){
			ISymbol symbol = container.getTypeSymbol();
			if( symbol != null && symbol.isType( TypeInfo.t_namespace ) ){
				container = (IContainerSymbol) symbol;
			}
		}
		
		data.foundItems = ParserSymbolTable.lookupInContained( data, container );
		
		if( data.foundItems != null ){
			ISymbol symbol = ParserSymbolTable.resolveAmbiguities( data ); 
			return (IParameterizedSymbol) (( symbol instanceof IParameterizedSymbol ) ? symbol : null);
		} 
		
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupNestedNameSpecifier(java.lang.String)
	 */
	/**
	 * Method LookupNestedNameSpecifier.
	 * @param name
	 * @return Declaration
	 * The name of a class or namespace member can be referred to after the ::
	 * scope resolution operator applied to a nested-name-specifier that
	 * nominates its class or namespace.  During the lookup for a name preceding
	 * the ::, object, function and enumerator names are ignored.  If the name
	 * is not a class-name or namespace-name, the program is ill-formed
	 */
	public IContainerSymbol lookupNestedNameSpecifier( String name ) throws ParserSymbolTableException {
		return lookupNestedNameSpecifier( name, this );
	}
	private IContainerSymbol lookupNestedNameSpecifier(String name, IContainerSymbol inSymbol ) throws ParserSymbolTableException{		
		ISymbol foundSymbol = null;
	
		LookupData data = new LookupData( name, TypeInfo.t_namespace );
		data.filter.addAcceptedType( TypeInfo.t_class );
		data.filter.addAcceptedType( TypeInfo.t_struct );
		data.filter.addAcceptedType( TypeInfo.t_union );
		
		data.foundItems = ParserSymbolTable.lookupInContained( data, inSymbol );
	
		if( data.foundItems != null ){
			foundSymbol = ParserSymbolTable.resolveAmbiguities( data );
		}
			
		if( foundSymbol == null && inSymbol.getContainingSymbol() != null ){
			foundSymbol = lookupNestedNameSpecifier( name, inSymbol.getContainingSymbol() );
		}
		
		if( foundSymbol instanceof IContainerSymbol )
			return (IContainerSymbol) foundSymbol;
 
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#qualifiedLookup(java.lang.String)
	 */
	public ISymbol qualifiedLookup( String name ) throws ParserSymbolTableException{
	
		return qualifiedLookup(name, TypeInfo.t_any); 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#qualifiedLookup(java.lang.String, org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType)
	 */
	public ISymbol qualifiedLookup( String name, TypeInfo.eType t ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, t );
		data.qualified = true;
		ParserSymbolTable.lookup( data, this );
	
		return ParserSymbolTable.resolveAmbiguities( data ); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#unqualifiedFunctionLookup(java.lang.String, java.util.List)
	 */
	/**
	 * UnqualifiedFunctionLookup
	 * @param name
	 * @param parameters
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 * 
	 * 3.4.2-1 When an unqualified name is used as the post-fix expression in a
	 * function call, other namespaces not consdiered during the usual
	 * unqualified lookup may be searched.
	 * 
	 * 3.4.2-2 For each argument type T in the function call, there is a set of
	 * zero or more associated namespaces and a set of zero or more associated
	 * classes to be considered.
	 * 
	 * If the ordinary unqualified lookup of the name find the declaration of a
	 * class member function, the associated namespaces and classes are not
	 * considered.  Otherwise, the set of declarations found by the lookup of
	 * the function name is the union of the set of declarations found using
	 * ordinary unqualified lookup and the set of declarations found in the
	 * namespaces and classes associated with the argument types.
	 */
	public IParameterizedSymbol unqualifiedFunctionLookup( String name, List parameters ) throws ParserSymbolTableException{
		//figure out the set of associated scopes first, so we can remove those that are searched
		//during the normal lookup to avoid doing them twice
		HashSet associated = new HashSet();
	
		//collect associated namespaces & classes.
		int size = ( parameters == null ) ? 0 : parameters.size();
		Iterator iter = ( parameters == null ) ? null : parameters.iterator();
	
		TypeInfo param = null;
		ISymbol paramType = null;
		for( int i = size; i > 0; i-- ){
			param = (TypeInfo) iter.next();
			paramType = ParserSymbolTable.getFlatTypeInfo( param ).getTypeSymbol();
		
			if( paramType == null ){
				continue;
			}
				
			ParserSymbolTable.getAssociatedScopes( paramType, associated );
		
			//if T is a pointer to a data member of class X, its associated namespaces and classes
			//are those associated with the member type together with those associated with X
			if( param.hasPtrOperators() && param.getPtrOperators().size() == 1 ){
				TypeInfo.PtrOp op = (TypeInfo.PtrOp)param.getPtrOperators().iterator().next();
				if( op.getType() == TypeInfo.PtrOp.t_pointer && 
					paramType.getContainingSymbol().isType( TypeInfo.t_class, TypeInfo.t_union ) )
				{
					ParserSymbolTable.getAssociatedScopes( paramType.getContainingSymbol(), associated );	
				}
			}
		}
	
		LookupData data = new LookupData( name, TypeInfo.t_function );
		//if parameters == null, thats no parameters, but we need to distinguish that from
		//no parameter information at all, so make an empty list.
		data.parameters = ( parameters == null ) ? ParserSymbolTable.EMPTY_LIST : parameters;
		data.associated = associated;
	
		ParserSymbolTable.lookup( data, this );
	
		ISymbol found = ParserSymbolTable.resolveAmbiguities( data );
	
		//if we haven't found anything, or what we found is not a class member, consider the 
		//associated scopes
		if( found == null || found.getContainingSymbol().getType() != TypeInfo.t_class ){
//			if( found != null ){
//				data.foundItems.add( found );
//			}
								
			IContainerSymbol associatedScope;
			//dump the hash to an array and iterate over the array because we
			//could be removing items from the collection as we go and we don't
			//want to get ConcurrentModificationExceptions			
			Object [] scopes = associated.toArray();
		
			size = associated.size();

			for( int i = 0; i < size; i++ ){
				associatedScope  = (IContainerSymbol) scopes[ i ];
				if( associated.contains( associatedScope ) ){
					data.qualified = true;
					data.ignoreUsingDirectives = true;
					data.usingDirectivesOnly = false;
					ParserSymbolTable.lookup( data, associatedScope );
				}
			}
		
			found = ParserSymbolTable.resolveAmbiguities( data );
		}
	
		if( found instanceof IParameterizedSymbol )
			return (IParameterizedSymbol) found;
		else 
			return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#memberFunctionLookup(java.lang.String, java.util.List)
	 */
	/**
	 * MemberFunctionLookup
	 * @param name
	 * @param parameters
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 * 
	 * Member lookup really proceeds as an unqualified lookup, but doesn't
	 * include argument dependant scopes
	 */
	public IParameterizedSymbol memberFunctionLookup( String name, List parameters ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, TypeInfo.t_function );
		//if parameters == null, thats no parameters, but we need to distinguish that from
		//no parameter information at all, so make an empty list.
		data.parameters = ( parameters == null ) ? ParserSymbolTable.EMPTY_LIST : parameters;
		
		ParserSymbolTable.lookup( data, this );
		return (IParameterizedSymbol) ParserSymbolTable.resolveAmbiguities( data ); 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#qualifiedFunctionLookup(java.lang.String, java.util.List)
	 */
	public IParameterizedSymbol qualifiedFunctionLookup( String name, List parameters ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, TypeInfo.t_function );
		data.qualified = true;
		//if parameters == null, thats no parameters, but we need to distinguish that from
		//no parameter information at all, so make an empty list.
		data.parameters = ( parameters == null ) ? ParserSymbolTable.EMPTY_LIST : parameters;
	
		ParserSymbolTable.lookup( data, this );
	
		return (IParameterizedSymbol) ParserSymbolTable.resolveAmbiguities( data ); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#templateLookup(java.lang.String, java.util.List)
	 */
	public ISymbol lookupTemplateId( String name, List arguments ) throws ParserSymbolTableException
	{
		LookupData data = new LookupData( name, TypeInfo.t_any );
		
		ParserSymbolTable.lookup( data, this );
		ISymbol found = ParserSymbolTable.resolveAmbiguities( data );
		if( found != null ){
			if( (found.isType( TypeInfo.t_templateParameter ) && found.getTypeInfo().getTemplateParameterType() == TypeInfo.t_template) ||
				     found.isType( TypeInfo.t_template ) )
			{
				found = ((ITemplateSymbol) found).instantiate( arguments );
			} else if( found.getContainingSymbol().isType( TypeInfo.t_template ) ){
				found = ((ITemplateSymbol) found.getContainingSymbol()).instantiate( arguments );
			}	
		}
		
		return found;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupFunctionTemplateId(java.lang.String, java.util.List, java.util.List)
	 */
	public ISymbol lookupFunctionTemplateId(String name, List parameters, List arguments, boolean forDefinition) throws ParserSymbolTableException {
		LookupData data = new LookupData( name, TypeInfo.t_function );
		data.exactFunctionsOnly = forDefinition;
		data.parameters = parameters;
		data.templateParameters = arguments;
		
		ParserSymbolTable.lookup( data, this );
		ISymbol found = ParserSymbolTable.resolveAmbiguities( data );

		return found;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookupTemplateIdForDefinition(java.lang.String, java.util.List)
	 */
	public IContainerSymbol lookupTemplateIdForDefinition(String name, List arguments) throws ParserSymbolTableException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public List prefixLookup( TypeFilter filter, String prefix, boolean qualified ) throws ParserSymbolTableException{
		LookupData data = new LookupData( prefix, filter );
		data.qualified = qualified;
		data.mode = ParserSymbolTable.LookupMode.PREFIX;
		
		ParserSymbolTable.lookup( data, this );
		
		List constructors = null;
		if( filter != null && filter.willAccept( TypeInfo.t_constructor ) && (this instanceof IDerivableContainerSymbol) ){
			if( getName().startsWith( prefix ) )
				constructors = ((IDerivableContainerSymbol)this).getConstructors();
		}
		
		if( data.foundItems == null || data.foundItems.isEmpty() ){
			if( constructors != null )
				return new LinkedList( constructors );
			else
				return null;
		} else {
			//remove any ambiguous symbols
			if( data.ambiguities != null && !data.ambiguities.isEmpty() ){
				Iterator iter = data.ambiguities.iterator();
				while( iter.hasNext() ){
					data.foundItems.remove( iter.next() );
				}
			}
			
			List list = new LinkedList();
			
			Iterator iter = data.foundItems.keySet().iterator();
			Object obj = null;
			while( iter.hasNext() ){
				obj = data.foundItems.get( iter.next() );
				
				if( obj instanceof List ){
					list.addAll( (List) obj );
				} else{
					list.add( obj );
				}
			}

			if( constructors != null )
				list.addAll( constructors );
			
			return list;
		}
	}
	
	public boolean isVisible( ISymbol symbol, IContainerSymbol qualifyingSymbol ){
		ISymbolASTExtension extension = symbol.getASTExtension();
		if(extension == null)
			return true;
		IASTNode node = extension.getPrimaryDeclaration();
		if(node == null)
			return true;
		
		if( node instanceof IASTMember ){
			ASTAccessVisibility visibility;
			
			visibility = ParserSymbolTable.getVisibility( symbol, qualifyingSymbol );
			if( visibility == null )
				return false;
			
			if( visibility == ASTAccessVisibility.PUBLIC ){
				return true;
			}
			
			IContainerSymbol container = getContainingSymbol();
			IContainerSymbol symbolContainer = symbol.getContainingSymbol();
			
			if( !symbolContainer.isType( TypeInfo.t_class, TypeInfo.t_union ) ||
				symbolContainer.equals( container ) )
			{
				return true;
			}

			//if this is a friend of the symbolContainer, then we are good
			if( isFriendOf( ( qualifyingSymbol != null ) ? qualifyingSymbol : symbolContainer ) ){
				return true;
			}
			
			if( visibility == ASTAccessVisibility.PROTECTED )
			{
				try {
					return ( ParserSymbolTable.hasBaseClass( container, symbolContainer ) >= 0 );
				} catch (ParserSymbolTableException e) {
					return false;
				}
			} else { //PRIVATE
				return false; 
			}
		}
		return true;
	}
	
	protected boolean isFriendOf( IContainerSymbol symbol ){
		if( symbol instanceof IDerivableContainerSymbol ){
			IContainerSymbol container = this.getContainingSymbol();
			
			while( container != null && container.isType( TypeInfo.t_block ) ){
				container = container.getContainingSymbol();
			}
			if( container != null && !container.isType( TypeInfo.t_class, TypeInfo.t_union ) ){
				container = null;
			}
			
			IDerivableContainerSymbol derivable = (IDerivableContainerSymbol) symbol;
			
			Iterator iter = derivable.getFriends().iterator();
			while( iter.hasNext() ){
				ISymbol friend = (ISymbol) iter.next();
				ISymbol typeSymbol = friend.getTypeSymbol();
				if( friend == this      || typeSymbol == this ||
					friend == container || ( container != null && typeSymbol == container ) )
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private IContainerSymbol getScopeForCTag( IContainerSymbol container ){
		while( !container.isType( TypeInfo.t_namespace ) && 
			   !container.isType( TypeInfo.t_function )  &&
			   !container.isType( TypeInfo.t_block ) )
		{
			container = container.getContainingSymbol();
		} 
		return container;
	}
	
	protected void addToContents( IExtensibleSymbol symbol ){
		if( _contents == ParserSymbolTable.EMPTY_LIST )
			_contents = new LinkedList();
		_contents.add( symbol );
	}
	
	protected List getContents(){
		return _contents;
	}
	
	public Iterator getContentsIterator(){
		return new ContentsIterator( getContents().iterator() );
	}
	
	protected class ContentsIterator implements Iterator {
		final Iterator internalIterator;
	
		Set alreadyReturned = new HashSet();
		
		public ContentsIterator( Iterator iter ){
			internalIterator = iter;
		}
		
		IExtensibleSymbol next = null;
		public boolean hasNext() {
			if( next != null ){
				return true;
			}
			if( !internalIterator.hasNext() )
				return false;
			while( internalIterator.hasNext() ){
				IExtensibleSymbol extensible = (IExtensibleSymbol) internalIterator.next();
				if( !alreadyReturned.contains( extensible ) ){
					if( extensible instanceof ISymbol ){
						ISymbol symbol = (ISymbol) extensible;
						if( symbol.isForwardDeclaration() && symbol.getTypeSymbol() != null &&
							symbol.getTypeSymbol().getContainingSymbol() == ContainerSymbol.this )
						{
							alreadyReturned.add( symbol.getTypeSymbol() );
							next = symbol.getTypeSymbol();
							return true;
						}
					} else if( extensible instanceof IUsingDeclarationSymbol ){
						IUsingDeclarationSymbol using = (IUsingDeclarationSymbol) extensible;
						alreadyReturned.addAll( using.getDeclaredSymbols() );
					}
					next = extensible;
					return true;
				}
			}
			return false;
		}

		public Object next() {
			IExtensibleSymbol extensible = next;
			if( next != null ){
				next = null;
				return extensible;
			}
			
			while( internalIterator.hasNext() ){
				extensible = (IExtensibleSymbol) internalIterator.next();
				if( !alreadyReturned.contains( extensible ) ){
					if( extensible instanceof ISymbol ){
						ISymbol symbol = (ISymbol) extensible;
						if( symbol.isForwardDeclaration() && symbol.getTypeSymbol() != null &&
							symbol.getTypeSymbol().getContainingSymbol() == ContainerSymbol.this )
						{
							alreadyReturned.add( symbol.getTypeSymbol() );
							return symbol.getTypeSymbol();
						}
					} else if( extensible instanceof IUsingDeclarationSymbol ){
						IUsingDeclarationSymbol using = (IUsingDeclarationSymbol) extensible;
						alreadyReturned.addAll( using.getDeclaredSymbols() );
					}
					return extensible;
				}
			}
			throw new NoSuchElementException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		protected void removeSymbol(){
			internalIterator.remove();
		}
		
	}
	static private class AddSymbolCommand extends Command{
		AddSymbolCommand( ISymbol newDecl, IContainerSymbol context ){
			_symbol = newDecl;
			_context = context;
		}
		
		public void undoIt(){
			Object obj = _context.getContainedSymbols().get( _symbol.getName() );
			
			if( obj instanceof LinkedList ){
				LinkedList list = (LinkedList)obj;
				ListIterator iter = list.listIterator();
				int size = list.size();
				ISymbol item = null;
				for( int i = 0; i < size; i++ ){
					item = (ISymbol)iter.next();
					if( item == _symbol ){
						iter.remove();
						break;
					}
				}
				if( list.size() == 1 ){
					_context.getContainedSymbols().put( _symbol.getName(), list.getFirst() );
				}
			} else if( obj instanceof BasicSymbol ){
				_context.getContainedSymbols().remove( _symbol.getName() );
			}
			
			//this is an inefficient way of doing this, we can modify the interfaces if the undo starts
			//being used often.
			ContentsIterator iter = (ContentsIterator) _context.getContentsIterator();
			while( iter.hasNext() ){
				IExtensibleSymbol ext = (IExtensibleSymbol) iter.next();
				if( ext == _symbol ){
					iter.removeSymbol();
					break;
				}
			}
		}
		
		private final ISymbol          _symbol;
		private final IContainerSymbol _context; 
	}
	
	static private class AddUsingDirectiveCommand extends Command{
		public AddUsingDirectiveCommand( IContainerSymbol container, IUsingDirectiveSymbol directive ){
			_decl = container;
			_directive = directive;
		}
		public void undoIt(){
			_decl.getUsingDirectives().remove( _directive );
			
			//this is an inefficient way of doing this, we can modify the interfaces if the undo starts
			//being used often.
			ContentsIterator iter = (ContentsIterator) _decl.getContentsIterator();
			while( iter.hasNext() ){
				IExtensibleSymbol ext = (IExtensibleSymbol) iter.next();
				if( ext == _directive ){
					iter.removeSymbol();
					break;
				}
			}
		}
		private final IContainerSymbol _decl;
		private final IUsingDirectiveSymbol _directive;
	}

	static protected class SymbolTableComparator implements Comparator{
		static final private Collator collator = Collator.getInstance();
		static { collator.setStrength( Collator.PRIMARY ); }
		public int compare( Object o1, Object o2 ){
			int result = collator.compare( o1, o2 );
			if( result == 0 ){
				collator.setStrength( Collator.IDENTICAL );
				result = collator.compare( o1, o2 );
				collator.setStrength( Collator.PRIMARY );
			}
			return result;
		}
		
		public boolean equals( Object obj ){
			return ( obj instanceof SymbolTableComparator );
		}
	}

	private 	LinkedList	_contents = ParserSymbolTable.EMPTY_LIST;				//ordered list of all contents of this symbol
	private		LinkedList	_usingDirectives = ParserSymbolTable.EMPTY_LIST;		//collection of nominated namespaces
	private		Map 		_containedSymbols = ParserSymbolTable.EMPTY_MAP;		//declarations contained by us.
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addTemplateId(org.eclipse.cdt.internal.core.parser.pst.ISymbol, java.util.List)
	 */
	public void addTemplateId(ISymbol symbol, List args) throws ParserSymbolTableException {
		throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
	}
}
