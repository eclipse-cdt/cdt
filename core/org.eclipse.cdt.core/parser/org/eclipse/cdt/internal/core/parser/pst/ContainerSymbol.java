/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

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
			
		copy._usingDirectives  = ( _usingDirectives != null ) ? (LinkedList) _usingDirectives.clone() : null; 
		copy._containedSymbols = ( _containedSymbols != null )? (HashMap) _containedSymbols.clone() : null;

		return copy;	
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
	
		//Templates contain 1 declaration
		if( getType() == TypeInfo.t_template ){
			//declaration must be a class or a function
			if( ( obj.getType() != TypeInfo.t_class && obj.getType() != TypeInfo.t_function ) ||
				( getContainedSymbols() != null && getContainedSymbols().size() == 1 ) )
			{
				//throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
			}
		}
		
		Map declarations = containing.getContainedSymbols();
	
		boolean unnamed = obj.getName().equals( ParserSymbolTable.EMPTY_NAME );
	
		Object origObj = null;
	
		obj.setContainingSymbol( containing );

		//does this name exist already?
		origObj = declarations.get( obj.getName() );
	
		if( origObj != null )
		{
			ISymbol origDecl = null;
			LinkedList  origList = null;
	
			if( origObj instanceof ISymbol ){
				origDecl = (ISymbol)origObj;
			} else if( origObj.getClass() == LinkedList.class ){
				origList = (LinkedList)origObj;
			} else {
				throw new ParserSymbolTableException( ParserSymbolTableException.r_InternalError );
			}
		
			boolean validOverride = ((origList == null) ? ParserSymbolTable.isValidOverload( origDecl, obj ) : ParserSymbolTable.isValidOverload( origList, obj ) );
			if( unnamed || validOverride )
			{	
				if( origList == null ){
					origList = new LinkedList();
					origList.add( origDecl );
					origList.add( obj );
			
					declarations.remove( origDecl );
					declarations.put( obj.getName(), origList );
				} else	{
					origList.add( obj );
					//origList is already in _containedDeclarations
				}
			} else {
				throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidOverload );
			}
		} else {
			declarations.put( obj.getName(), obj );
		}
	
		obj.setIsTemplateMember( isTemplateMember() || getType() == TypeInfo.t_template );
		
		Command command = new AddSymbolCommand( (ISymbol) obj, containing );
		getSymbolTable().pushCommand( command );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#hasUsingDirectives()
	 */
	public boolean hasUsingDirectives(){
		return ( _usingDirectives != null && !_usingDirectives.isEmpty() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#getUsingDirectives()
	 */
	public List getUsingDirectives(){
		if( _usingDirectives == null ){
			_usingDirectives = new LinkedList();
		}
		
		return _usingDirectives;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addUsingDirective(org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol)
	 */
	public void addUsingDirective( IContainerSymbol namespace ) throws ParserSymbolTableException{
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
		
		List usingDirectives = getUsingDirectives();		
	
		usingDirectives.add( namespace );
		
		Command command = new AddUsingDirectiveCommand( this, namespace );
		getSymbolTable().pushCommand( command );
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
	public ISymbol addUsingDeclaration( String name ) throws ParserSymbolTableException {
		return addUsingDeclaration( name, null );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#addUsingDeclaration(java.lang.String, org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol)
	 */
	public ISymbol addUsingDeclaration( String name, IContainerSymbol declContext ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, TypeInfo.t_any, null );

		if( declContext != null ){				
			data.qualified = true;
			data.templateInstance = declContext.getTemplateInstance();
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

		while( symbol != null ){
			if( ParserSymbolTable.okToAddUsingDeclaration( symbol, this ) ){
				clone = (ISymbol) symbol.clone(); //7.3.3-9
				addSymbol( clone );
			} else {
				throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidUsing );
			}
			
			if( iter != null && iter.hasNext() ){
				symbol = (ISymbol) iter.next();
			} else {
				symbol = null;
			}
		}
		
		return clone;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#getContainedSymbols()
	 */
	public Map getContainedSymbols(){
		if( _containedSymbols == null ){
			_containedSymbols = new HashMap();
		}
		return _containedSymbols;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#elaboratedLookup(org.eclipse.cdt.internal.core.parser.pst.TypeInfo.eType, java.lang.String)
	 */
	public ISymbol elaboratedLookup( TypeInfo.eType type, String name ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, type, getTemplateInstance() );
	
		ParserSymbolTable.lookup( data, this );
	
		return ParserSymbolTable.resolveAmbiguities( data ); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#lookup(java.lang.String)
	 */
	public ISymbol lookup( String name ) throws ParserSymbolTableException {
		LookupData data = new LookupData( name, TypeInfo.t_any, getTemplateInstance() );
	
		ParserSymbolTable.lookup( data, this );
	
		return ParserSymbolTable.resolveAmbiguities( data ); 
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
		LookupData data = new LookupData( name, TypeInfo.t_any, getTemplateInstance() );
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
	
		return ParserSymbolTable.resolveAmbiguities( data );
	}

	public IParameterizedSymbol lookupMethodForDefinition( String name, List parameters ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, TypeInfo.t_any, getTemplateInstance() );
		data.qualified = true;
		data.parameters = ( parameters == null ) ? new LinkedList() : parameters;
		
		IContainerSymbol container = this;
		
		//handle namespace aliases
		if( container.isType( TypeInfo.t_namespace ) ){
			ISymbol symbol = container.getTypeSymbol();
			if( symbol != null && symbol.isType( TypeInfo.t_namespace ) ){
				container = (IContainerSymbol) symbol;
			}
		}
		
		data.foundItems = ParserSymbolTable.lookupInContained( data, container );
		
		ISymbol symbol = ParserSymbolTable.resolveAmbiguities( data ); 
		return (IParameterizedSymbol) (( symbol instanceof IParameterizedSymbol ) ? symbol : null);
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
	
		LookupData data = new LookupData( name, TypeInfo.t_namespace, getTemplateInstance() );
		data.filter.addFilteredType( TypeInfo.t_class );
		data.filter.addFilteredType( TypeInfo.t_struct );
		data.filter.addFilteredType( TypeInfo.t_union );
		
		data.foundItems = ParserSymbolTable.lookupInContained( data, inSymbol );
	
		if( data.foundItems != null ){
			foundSymbol = (ISymbol) ParserSymbolTable.resolveAmbiguities( data );//, data.foundItems );
		}
			
		if( foundSymbol == null && inSymbol.getContainingSymbol() != null ){
			foundSymbol = lookupNestedNameSpecifier( name, inSymbol.getContainingSymbol() );
		}
		
		if( foundSymbol instanceof IContainerSymbol )
			return (IContainerSymbol) foundSymbol;
		else 
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
		LookupData data = new LookupData( name, t, getTemplateInstance() );
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
	
		LookupData data = new LookupData( name, TypeInfo.t_function, getTemplateInstance() );
		//if parameters == null, thats no parameters, but we need to distinguish that from
		//no parameter information at all, so make an empty list.
		data.parameters = ( parameters == null ) ? new LinkedList() : parameters;
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
		LookupData data = new LookupData( name, TypeInfo.t_function, getTemplateInstance() );
		//if parameters == null, thats no parameters, but we need to distinguish that from
		//no parameter information at all, so make an empty list.
		data.parameters = ( parameters == null ) ? new LinkedList() : parameters;
		
		ParserSymbolTable.lookup( data, (IContainerSymbol) this );
		return (IParameterizedSymbol) ParserSymbolTable.resolveAmbiguities( data ); 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#qualifiedFunctionLookup(java.lang.String, java.util.List)
	 */
	public IParameterizedSymbol qualifiedFunctionLookup( String name, List parameters ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, TypeInfo.t_function, getTemplateInstance() );
		data.qualified = true;
		//if parameters == null, thats no parameters, but we need to distinguish that from
		//no parameter information at all, so make an empty list.
		data.parameters = ( parameters == null ) ? new LinkedList() : parameters;
	
		ParserSymbolTable.lookup( data, (IContainerSymbol)this );
	
		return (IParameterizedSymbol) ParserSymbolTable.resolveAmbiguities( data ); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#templateLookup(java.lang.String, java.util.List)
	 */
	public TemplateInstance templateLookup( String name, List arguments ) throws ParserSymbolTableException
	{
		LookupData data = new LookupData( name, TypeInfo.t_any, getTemplateInstance() );
		data.parameters = arguments;
		
		ParserSymbolTable.lookup( data, (IContainerSymbol) this );
		ISymbol found = ParserSymbolTable.resolveAmbiguities( data );
		if( found.isType( TypeInfo.t_template ) ){
			return ((IParameterizedSymbol) found).instantiate( arguments );
		} 
		return null;
	}

	public List prefixLookup( TypeFilter filter, String prefix, boolean qualified ) throws ParserSymbolTableException{
		LookupData data = new LookupData( prefix, filter, getTemplateInstance() );
		data.qualified = qualified;
		data.mode = ParserSymbolTable.LookupMode.PREFIX;
		
		ParserSymbolTable.lookup( data, this );
		
		if( data.foundItems == null || data.foundItems.isEmpty() ){
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
			
			return list;
		}
	}
	
	public boolean isVisible( ISymbol symbol, IContainerSymbol qualifyingSymbol ){
		ISymbolASTExtension extension = symbol.getASTExtension();
		IASTNode node = extension.getPrimaryDeclaration();
		
		if( node instanceof IASTMember ){
			ASTAccessVisibility visibility;
			try {
				visibility = ParserSymbolTable.getVisibility( symbol, qualifyingSymbol );
			} catch (ParserSymbolTableException e) {
				return false;
			}
			if( visibility == ASTAccessVisibility.PUBLIC ){
				return true;
			}
			
			IContainerSymbol container = getContainingSymbol();
			IContainerSymbol symbolContainer = ( qualifyingSymbol != null ) ? qualifyingSymbol : symbol.getContainingSymbol();
			
			if( !symbolContainer.isType( TypeInfo.t_class, TypeInfo.t_union ) ||
				symbolContainer.equals( container ) )
			{
				return true;
			}

			//if this is a friend of the symbolContainer, then we are good
			if( isFriendOf( symbolContainer ) ){
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol#instantiate(java.util.List)
	 */
	public TemplateInstance instantiate( List arguments ) throws ParserSymbolTableException{
		if( getType() != TypeInfo.t_template ){
			return null;
		}
			
		//TODO uncomment when template specialization matching & ordering is working
		//IParameterizedSymbol template = ParserSymbolTable.matchTemplatePartialSpecialization( this, arguments );
		IParameterizedSymbol template = null;
			
		if( template == null ){
			template = (IParameterizedSymbol) this;
		}
			
		List paramList = template.getParameterList();
		int numParams = ( paramList != null ) ? paramList.size() : 0;
			
		if( numParams == 0 ){
			return null;				
		}

		HashMap map = new HashMap();
		Iterator paramIter = paramList.iterator();
		Iterator argIter = arguments.iterator();
			
		ISymbol param = null;
		TypeInfo arg = null; 
		for( int i = 0; i < numParams; i++ ){
			param = (ISymbol) paramIter.next();
				
			if( argIter.hasNext() ){
				arg = (TypeInfo) argIter.next();
				map.put( param, arg );
			} else {
				Object obj = param.getTypeInfo().getDefault();
				if( obj != null && obj instanceof TypeInfo ){
					map.put( param, obj );
				} else {
					throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
				}
			}
		}
			
		if( template.getContainedSymbols().size() != 1 ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
		}
			
		Iterator iter = template.getContainedSymbols().keySet().iterator();
		IContainerSymbol symbol = (IContainerSymbol) template.getContainedSymbols().get( iter.next() );
			 
		TemplateInstance instance = new TemplateInstance( getSymbolTable(), symbol, map );
		return instance;
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
					_context.getContainedSymbols().remove( _symbol.getName() );
					_context.getContainedSymbols().put( _symbol.getName(), list.getFirst() );
				}
			} else if( obj instanceof BasicSymbol ){
				_context.getContainedSymbols().remove( _symbol.getName() );
			}
//			if( _removeThis && _symbol instanceof IParameterizedSymbol ){
//				((IParameterizedSymbol)_symbol).getContainedSymbols().remove( ParserSymbolTable.THIS );
//			}
		}
		
		private ISymbol 		 _symbol;
		private IContainerSymbol _context; 
	}
	
	static private class AddUsingDirectiveCommand extends Command{
		public AddUsingDirectiveCommand( IContainerSymbol container, IContainerSymbol namespace ){
			_decl = container;
			_namespace = namespace;
		}
		public void undoIt(){
			_decl.getUsingDirectives().remove( _namespace );
		}
		private IContainerSymbol _decl;
		private IContainerSymbol _namespace;
	}

	private		LinkedList	_usingDirectives;		//collection of nominated namespaces
	private		HashMap 	_containedSymbols;		//declarations contained by us.

}
