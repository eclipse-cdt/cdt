/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others. 
 * All rights reserved.   This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v05. html
 *
 * Contributors: 
 * Rational Software - Initial API and implementation
 *
***********************************************************************/


package org.eclipse.cdt.internal.core.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * @author aniefer
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

public class ParserSymbolTable {

	/**
	 * Constructor for ParserSymbolTable.
	 */
	public ParserSymbolTable() {
		super();
		_compilationUnit = new Declaration();
		push( _compilationUnit );
	}

	public void push( Declaration obj ){
		if( _contextStack.empty() == false && obj.getContainingScope() == null ){
			obj.setContainingScope( (Declaration) _contextStack.peek() );
		}
		
		_contextStack.push( obj );
	}
	
	public Declaration pop(){
		return (Declaration) _contextStack.pop();
	}
	
	public Declaration peek(){
		return (Declaration) _contextStack.peek();
	}
	
	public Declaration getCompilationUnit(){
			return _compilationUnit;
	}
		
	public Declaration Lookup( String name ) throws ParserSymbolTableException {
		LookupData data = new LookupData( name, -1 );
		return Lookup( data, (Declaration) _contextStack.peek() );
	}
	
	public Declaration ElaboratedLookup( int type, String name ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, type );
		return Lookup( data, (Declaration) _contextStack.peek() );
	}

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
	
	public Declaration LookupNestedNameSpecifier( String name ) throws ParserSymbolTableException {
		return LookupNestedNameSpecifier( name, (Declaration) _contextStack.peek() );
	}

	private Declaration LookupNestedNameSpecifier(String name, Declaration inDeclaration ) throws ParserSymbolTableException 
	{		
		Declaration foundDeclaration = null;
		
		LookupData data = new LookupData( name, Declaration.t_namespace );
		data.upperType = Declaration.t_union;
		
		foundDeclaration = LookupInContained( data, inDeclaration );
			
		if( foundDeclaration == null && inDeclaration._containingScope != null ){
			foundDeclaration = LookupNestedNameSpecifier( name, inDeclaration._containingScope );
		}
			
		return foundDeclaration;
	}
	
	/**
	 * 
	 * @param name
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 * 
	 * During lookup for a name preceding the :: scope resolution operator,
	 * object, function, and enumerator names are ignored.
	 */
	public Declaration QualifiedLookup( String name ) throws ParserSymbolTableException
	{
		LookupData data = new LookupData( name, -1 );
		data.qualified = true;
		return Lookup( data, (Declaration) _contextStack.peek() );
	}
	
	public void addUsingDirective( Declaration namespace ) throws ParserSymbolTableException 
	{
		if( namespace.getType() != Declaration.t_namespace ){
			throw new ParserSymbolTableException();
		}
			
		Declaration declaration = (Declaration) _contextStack.peek();
		
		if( declaration._usingDirectives == null ){
			declaration._usingDirectives = new LinkedList(); 
		}
		
		declaration._usingDirectives.add( namespace );
	}
	
	public void addDeclaration( Declaration obj ) throws ParserSymbolTableException{
		Declaration containing = (Declaration) _contextStack.peek();
		Map declarations = containing.getContainedDeclarations();
		
		Object origObj = null;
		
		obj.setContainingScope( containing );

		if( declarations == null ){
			declarations = containing.createContained();
		} else {
			//does this name exist already?
			origObj = declarations.get( obj.getName() );
		}
		
		if( origObj != null )
		{
			Declaration origDecl = null;
			LinkedList  origList = null;
		
			if( origObj.getClass() == Declaration.class ){
				origDecl = (Declaration)origObj;
			} else if( origObj.getClass() == LinkedList.class ){
				origList = (LinkedList)origObj;
			} else {
				throw new ParserSymbolTableException();
			}
			
			if( (origList == null) ? isValidOverload( origDecl, obj ) : isValidOverload( origList, obj ) ){
				if( origList == null ){
					origList = new LinkedList();
					origList.add( origDecl );
					origList.add( obj );
				
					declarations.remove( obj );
					declarations.put( obj.getName(), origList );
				} else	{
					origList.add( obj );
					//origList is already in _containedDeclarations
				}
			} else {
				throw new ParserSymbolTableException();
			}
		} else {
			declarations.put( obj.getName(), obj );
		}
	}
	
	/**
	 * Lookup the name from LookupData starting in the inDeclaration
	 * @param data
	 * @param inDeclaration
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 */
	static private Declaration Lookup( LookupData data, Declaration inDeclaration ) throws ParserSymbolTableException
	{
		if( data.type != -1 && data.type < Declaration.t_class && data.upperType > Declaration.t_union ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTypeInfo );
		}
		
		Declaration decl = null;					//the return value
		LinkedList tempList = null;
		LinkedList foundNames = new LinkedList();	//list of names found
		LinkedList transitives = new LinkedList();	//list of transitive using directives
		
		
		//if this name define in this scope?
		decl = LookupInContained( data, inDeclaration );
		if( decl != null ){
			foundNames.add( decl );		 
		}
			
		//check nominated namespaces
		//the transitives list is populated in LookupInNominated, and then 
		//processed in ProcessDirectives
		
		data.visited.clear(); //each namesapce is searched at most once, so keep track
		
		tempList = LookupInNominated( data, inDeclaration, transitives );
				
		if( tempList != null ){
			foundNames.addAll( tempList );
		}
			
		//if we are doing a qualified lookup, only process using directives if
		//we haven't found the name yet. 
		if( !data.qualified || foundNames.size() == 0 ){
			ProcessDirectives( inDeclaration, data, transitives );
			
			if( inDeclaration._usingDirectives != null ){
				ProcessDirectives( inDeclaration, data, inDeclaration._usingDirectives );
			}
						
			while( data.usingDirectives != null && data.usingDirectives.get( inDeclaration ) != null ){
				transitives.clear();
				
				tempList = LookupInNominated( data, inDeclaration, transitives );
				
				if( tempList != null ){
					foundNames.addAll( tempList );
				}

				if( !data.qualified || foundNames.size() == 0 ){
					ProcessDirectives( inDeclaration, data, transitives );
				}
			}
		}
		
		decl = ResolveAmbiguities( foundNames );
		if( decl != null ){
			return decl;
		}
			
		//if we still havn't found it, check any parents we have
		data.visited.clear();	//each virtual base class is searched at most once	
		decl = LookupInParents( data, inDeclaration );	
					
		//if still not found, check our containing scope.			
		if( decl == null && inDeclaration._containingScope != null ){ 
			decl = Lookup( data, inDeclaration._containingScope );
		}

		return decl;
	}
	
	/**
	 * function LookupInNominated
	 * @param data
	 * @param transitiveDirectives
	 * @return List
	 * 
	 * for qualified:
	 *  3.4.3.2-2 "let S be the set of all declarations of m in X
	 * and in the transitive closure of all namespaces nominated by using-
	 * directives in X and its used namespaces, except that using-directives are
	 * ignored in any namespace, including X, directly containing one or more
	 * declarations of m."
	 * 
	 * for unqualified:
	 * 7.3.4-2 The using-directive is transitive: if a scope contains a using
	 * directive that nominates a second namespace that itself contains using-
	 * directives, the effect is as if the using-directives from the second
	 * namespace also appeared in the first.
	 */
	static private LinkedList LookupInNominated( LookupData data, Declaration declaration, LinkedList transitiveDirectives ) throws ParserSymbolTableException{
		//if the data.usingDirectives is empty, there is nothing to do.
		if( data.usingDirectives == null ){
			return null;
		}
			
		LinkedList found = null;	//list of found names to return
		
		//local variables
		LinkedList  list = null;
		Iterator    iter = null;
		Declaration decl = null;
		Declaration temp = null;
		int size = 0;
		
		list = (LinkedList) data.usingDirectives.remove( declaration );
		
		if( list == null ){
			return null;
		}
		
		iter = list.iterator();
		size = list.size();
		for( int i = size; i > 0; i-- ){
			decl = (Declaration) iter.next();

			//namespaces are searched at most once
			if( !data.visited.contains( decl ) ){
				data.visited.add( decl );
				
				temp = LookupInContained( data, decl );
										
				//if we found something, add it to the list of found names
				if( temp != null ){
					if( found == null ){ 
						found = new LinkedList();
					}
					found.add( temp );
				}	
				
				//only consider the transitive using directives if we are an unqualified
				//lookup, or we didn't find the name in decl
				if( (!data.qualified || temp == null) && decl._usingDirectives != null ){
					//name wasn't found, add transitive using directives for later consideration
					transitiveDirectives.addAll( decl._usingDirectives );
				}
			}
		}
		
		return found;
	}
	
	/**
	 * function LookupInContained
	 * @param data
	 * @return List
	 * @throws ParserSymbolTableException
	 * 
	 * Look for data.name in our collection _containedDeclarations
	 */
	private static Declaration LookupInContained( LookupData data, Declaration lookIn ) throws ParserSymbolTableException{
		LinkedList found = null;
		Declaration temp  = null;
		Object obj = null;
	
		Map declarations = lookIn.getContainedDeclarations();
		if( declarations == null )
			return null;
		
		obj = declarations.get( data.name );
	
		if( obj == null ){
			//not found
			return null;
		}
			
	 	//the contained declarations map either to a Declaration object, or to a list
	 	//of declaration objects.
		if( obj.getClass() == Declaration.class ){	
			if( ((Declaration)obj).isType( data.type, data.upperType ) ){
				return (Declaration) obj;
			}
		} else {
			found = new LinkedList();
			
			LinkedList objList = (LinkedList)obj;
			Iterator iter  = objList.iterator();
			int size = objList.size();
					
			for( int i = 0; i < size; i++ ){
				temp = (Declaration) iter.next();
		
				if( temp.isType( data.type, data.upperType ) ){
					found.add(temp);
				} 
			}
		}

		//if none of the found items made it through the type filtering, just
		//return null instead of an empty list.
		if( found == null || found.size() == 0 )			
			return null;
		
		return ResolveAmbiguities( found );
	}
	
	/**
	 * 
	 * @param data
	 * @param lookIn
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 */
	private static Declaration LookupInParents( LookupData data, Declaration lookIn ) throws ParserSymbolTableException{
		LinkedList scopes = lookIn.getParentScopes();
		Declaration decl = null;
		Declaration temp = null;
		Iterator iterator = null;
		Declaration.ParentWrapper wrapper = null;
		
		if( scopes == null )
			return null;
				
		iterator = scopes.iterator();
			
		int size = scopes.size();
	
		for( int i = size; i > 0; i-- )
		{
			wrapper = (Declaration.ParentWrapper) iterator.next();
			if( !wrapper.isVirtual || !data.visited.contains( wrapper.parent ) ){
				if( wrapper.isVirtual ){
					data.visited.add( wrapper.parent );
				}
				
				//is this name define in this scope?
				temp =  LookupInContained( data, wrapper.parent );
					
				if( temp == null ){
					temp = LookupInParents( data, wrapper.parent );
				}
			}	
			
			if( temp != null && temp.isType( data.type ) ){
				if( decl == null  ){
					decl = temp;
				} else if ( temp != null ) {
					//it is not ambiguous if temp & decl are the same thing and it is static
					//or an enumerator
					if( decl == temp && ( temp.isStatic() || temp.getType() == Declaration.t_enumerator) ){
						temp = null;
					} else {
						throw( new ParserSymbolTableException( ParserSymbolTableException.r_AmbiguousName ) );
					}
	
				}
			} else {
				temp = null;	//reset temp for next iteration
			}
		}
	
		return decl;	
	}
	
	/**
	 * function isValidOverload
	 * @param origDecl
	 * @param newDecl
	 * @return boolean
	 * 
	 * 3.3.7 "A class name or enumeration name can be hidden by the name of an
	 * object, function or enumerator declared in the same scope"
	 * 
	 * 3.4-1 "Name lookup may associate more than one declaration with a name if
	 * it finds the name to be a function name"
	 */
	private static boolean isValidOverload( Declaration origDecl, Declaration newDecl ){
		int origType = origDecl.getType();
		int newType  = newDecl.getType();
		
		if( (origType >= Declaration.t_class && origType <= Declaration.t_enum) && //class name or enumeration ...
			( newType == Declaration.t_type || (newType >= Declaration.t_function && newType <= Declaration.typeMask) ) ){
				
			return true;
		}
		//if the origtype is not a class-name or enumeration name, then the only other
		//allowable thing is if they are both functions.
		else if( origType == Declaration.t_function && newType == Declaration.t_function ){
			return true;
		}
		
		return false;
	}
	
	private static boolean isValidOverload( LinkedList origList, Declaration newDecl ){
		if( origList.size() == 1 ){
			return isValidOverload( (Declaration)origList.getFirst(), newDecl );
		} else if ( origList.size() > 1 ){

			//the first thing can be a class-name or enumeration name, but the rest
			//must be functions.  So make sure the newDecl is a function before even
			//considering the list
			if( newDecl.getType() != Declaration.t_function ){
				return false;
			}
			
			Iterator iter = origList.iterator();
			Declaration decl = (Declaration) iter.next();
			boolean valid = (( decl.getType() >= Declaration.t_class && decl.getType() <= Declaration.t_enum ) ||
							  decl.getType() == Declaration.t_function );
			
			while( valid && iter.hasNext() ){
				decl = (Declaration) iter.next();
				valid = ( decl.getType() == Declaration.t_function );
			}
			
			return valid;
		}
		
		//empty list, return true
		return true;
	}
	
	static private Declaration ResolveAmbiguities( LinkedList items ) throws ParserSymbolTableException{
		Declaration decl = null;
	
		int size = items.size(); 
	
		if( size == 0){
			return null;
		} else if (size == 1) {
			return (Declaration) items.getFirst();
		} else	{
			Declaration first  = (Declaration)items.removeFirst();
	
			//if first one is a class-name, the next ones hide it
			if( first.getType() >= Declaration.t_class && first.getType() <= Declaration.t_enum ){
				return ResolveAmbiguities( items );
			}
		
			//else, if the first is an object (ie not a function), the rest must be the same
			//declaration.  otherwise (ie it is a function), the rest must be functions.
			boolean needSame = ( first.getType() != Declaration.t_function );
	
			Iterator iter = items.iterator();
		
			for( int i = (size - 1); i > 0; i-- ){
				decl = (Declaration) iter.next();
			
				if( needSame ){
					if( decl != first ){
						throw new ParserSymbolTableException();
					}
				} else {
					if( decl.getType() != Declaration.t_function ){
						throw new ParserSymbolTableException();
					}
				}
			}
		
			if( needSame ){
				return first;
			} else {
				items.addFirst( first );
				return ResolveFunction( items );
			}
		}
	}

	static private Declaration ResolveFunction( LinkedList functions ){
		//TBD
		return null;
	}
	
	/**
	 * function ProcessDirectives
	 * @param Declaration decl
	 * @param LookupData  data
	 * @param LinkedList  directives
	 * 
	 * Go through the directives and for each nominated namespace find the
	 * closest enclosing declaration for that namespace and decl, then add the
	 * nominated namespace to the lookup data for consideration when we reach
	 * the enclosing declaration.
	 */
	static private void ProcessDirectives( Declaration decl, LookupData data, LinkedList directives ){
		Declaration enclosing = null;
		Declaration temp = null;
		
		int size = directives.size();
		Iterator iter = directives.iterator();
	
		for( int i = size; i > 0; i-- ){
			temp = (Declaration) iter.next();
		
			//namespaces are searched at most once
			if( !data.visited.contains( temp ) ){
				enclosing = getClosestEnclosingDeclaration( decl, temp );
						
				//the data.usingDirectives is a map from enclosing declaration to 
				//a list of namespaces to consider when we reach that enclosing
				//declaration
				LinkedList list = (data.usingDirectives == null ) 
								? null
								: (LinkedList) data.usingDirectives.get( enclosing );
				if ( list == null ){
					list = new LinkedList();
					list.add( temp );
					if( data.usingDirectives == null ){
						data.usingDirectives = new HashMap();
					}
					data.usingDirectives.put( enclosing, list );
				} else {
					list.add( temp );
				}
			}
		}
	}
	
	/**
	 * function getClosestEnclosingDeclaration
	 * @param decl1
	 * @param decl2
	 * @return Declaration
	 * 
	 * 7.3.4-1 "During unqualified lookup, the names appear as if they were
	 * declared in the nearest enclosing namespace which contains both the
	 * using-directive and the nominated namespace"
	 * 
	 * TBD: Consider rewriting this iteratively instead of recursively, for
	 * performance
	 */
	static private Declaration getClosestEnclosingDeclaration( Declaration decl1, Declaration decl2 ){
		if( decl1 == decl2 ){ 
			return decl1;
		}
				
		if( decl1._depth == decl2._depth ){
			return getClosestEnclosingDeclaration( decl1._containingScope, decl2._containingScope );
		} else if( decl1._depth > decl2._depth ) {
			return getClosestEnclosingDeclaration( decl1._containingScope, decl2 );
		} else {
			return getClosestEnclosingDeclaration( decl1, decl2._containingScope );
		}
	}
	
	private Stack _contextStack = new Stack();
	private Declaration _compilationUnit;
	
	private class LookupData
	{
		public String name;
		public Map usingDirectives; 
		public Set visited = new HashSet();	//used to ensure we don't visit things more than once
		
		public int type = -1;
		public int upperType = 0;
		public boolean qualified = false;

		public LookupData( String n, int t ){
			name = n;
			type = t;
		}
	}
}
