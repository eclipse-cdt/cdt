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
import java.util.ListIterator;
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
		try{
			_compilationUnit.setType( Declaration.t_namespace );
		} catch ( ParserSymbolTableException e ){
			/*shouldn't happen*/
		}
		
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

	private Declaration LookupNestedNameSpecifier(String name, Declaration inDeclaration ) throws ParserSymbolTableException{		
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
	public Declaration LookupMemberForDefinition( String name ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, -1 );
		data.qualified = true;
	
		return LookupInContained( data, (Declaration) _contextStack.peek() );
	}
	
	/**
	 * 
	 * @param name
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 * 
	 */
	public Declaration QualifiedLookup( String name ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, -1 );
		data.qualified = true;
		return Lookup( data, (Declaration) _contextStack.peek() );
	}
	
	/**
	 * 
	 * @param name
	 * @param parameters
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 */
	public Declaration QualifiedFunctionLookup( String name, LinkedList parameters ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, Declaration.t_function );
		data.qualified = true;
		data.parameters = parameters;
		
		return Lookup( data, (Declaration) _contextStack.peek() );
	}
	
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
	public Declaration MemberFunctionLookup( String name, LinkedList parameters ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, Declaration.t_function );
		data.parameters = parameters;
			
		return Lookup( data, (Declaration) _contextStack.peek() );
	}
	
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
	public Declaration UnqualifiedFunctionLookup( String name, LinkedList parameters ) throws ParserSymbolTableException{
		//figure out the set of associated scopes first, so we can remove those that are searched
		//during the normal lookup to avoid doing them twice
		HashSet associated = new HashSet();
		//collect associated namespaces & classes.
		int size = parameters.size();
		Iterator iter = parameters.iterator();
		Declaration.ParameterInfo param = null;
		for( int i = size; i > 0; i-- ){
			param = (Declaration.ParameterInfo) iter.next();
			
			getAssociatedScopes( param.typeDeclaration, associated );
			//if T is a pointer to a data member of class X, its associated namespaces and classes
			//are those associated with the member type together with those associated with X
			if( param.ptrOperator != null && 
			   (param.ptrOperator.equals("*") || param.ptrOperator.equals("[]")) &&
			 	param.typeDeclaration._containingScope.isType( Declaration.t_class, Declaration.t_union ) )
			{
				getAssociatedScopes( param.typeDeclaration._containingScope, associated );
			}
		}
		
		LookupData data = new LookupData( name, Declaration.t_function );
		data.parameters = parameters;
		data.associated = associated;
		
		Declaration found = Lookup( data, (Declaration) _contextStack.peek() );
		
		//if we haven't found anything, or what we found is not a class member, consider the 
		//associated scopes
		if( found == null || found._containingScope.getType() != Declaration.t_class ){
			LinkedList foundList = new LinkedList();
			
			if( found != null ){
				foundList.add( found );
			}
			
			iter = associated.iterator();
			
			Declaration decl;
			Declaration temp;
			
			//use while hasNext instead of forloop since the lookup might remove
			//items from the collection.  
			//Actually, I think that there will be no removals, but leave it like this anyway
			while( iter.hasNext() ){
				decl = (Declaration) iter.next();
				
				data.qualified = true;
				data.ignoreUsingDirectives = true;
				temp = Lookup( data, decl );
				if( temp != null ){
					foundList.add( temp );
				}
			}
			
			found = ResolveAmbiguities( data, foundList );
		}
		
		return found;
	}

	/**
	 * LookupForFriendship
	 * @param name
	 * @return Declaration
	 * 7.3.1.2-3 When looking for a prior declaration of a class or a function
	 * declared as a friend, scopes outside the innermost enclosing namespace
	 * scope are not considered.
	 * 11.4-9 If a friend declaration appears in a local class and the name
	 * specified is an unqualified name, a prior declaration is looked up
	 * without considering scopes that are outside the innermost enclosing non-
	 * class scope.
	 */
	private Declaration LookupForFriendship( String name ) throws ParserSymbolTableException{
		LookupData data = new LookupData( name, -1 );
		
		Declaration decl = (Declaration) _contextStack.peek();
		boolean inClass = (decl.getType() == Declaration.t_class);
		
		Declaration enclosing = decl._containingScope;
		while( enclosing != null && (inClass ? enclosing.getType() != Declaration.t_class
											  :	enclosing.getType() == Declaration.t_namespace) )
		{                                        		
			enclosing = enclosing._containingScope;
		}

		data.stopAt = enclosing;
		
		return Lookup( data, (Declaration) _contextStack.peek() );
	}
		
	public void addUsingDirective( Declaration namespace ) throws ParserSymbolTableException{
		if( namespace.getType() != Declaration.t_namespace ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTypeInfo );
		}
			
		Declaration declaration = (Declaration) _contextStack.peek();
		
		if( declaration._usingDirectives == null ){
			declaration._usingDirectives = new LinkedList(); 
		}
		
		declaration._usingDirectives.add( namespace );
	}
	
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
	public Declaration addUsingDeclaration( Declaration obj ) throws ParserSymbolTableException{
		Declaration clone = null;
		Declaration context = (Declaration) _contextStack.peek();
		boolean okToAdd = false;
		
		//7.3.3-4
		if( context.isType( Declaration.t_class, Declaration.t_union ) ){
			//a member of a base class
			if( obj.getContainingScope().getType() == context.getType() ){
				okToAdd = hasBaseClass( context, obj.getContainingScope() );		
			} 
			//TBD : a member of an _anonymous_ union
			else if ( obj.getContainingScope().getType() == Declaration.t_union ) {
				Declaration union = obj.getContainingScope();
				okToAdd = hasBaseClass( context, union.getContainingScope() ); 
			}
			//an enumerator for an enumeration
			else if ( obj.getType() == Declaration.t_enumerator ){
				Declaration enumeration = obj.getContainingScope();
				okToAdd = hasBaseClass( context, enumeration.getContainingScope() );
			}
		} else {
			okToAdd = true;
		}
		
		if( okToAdd ){
			clone = (Declaration) obj.clone(); //7.3.3-9
			addDeclaration( clone );
		} else {
			throw new ParserSymbolTableException();
		}
		return clone;
	}
	
	public void addDeclaration( Declaration obj ) throws ParserSymbolTableException{
		
		Declaration containing = (Declaration) _contextStack.peek();
			
		//handle enumerators
		if( obj.getType() == Declaration.t_enumerator ){
			//a using declaration of an enumerator will not be contained in a
			//enumeration.
			if( containing.getType() == Declaration.t_enumeration ){
				//Following the closing brace of an enum-specifier, each enumerator has the type of its 
				//enumeration
				obj.setTypeDeclaration( containing );
				//Each enumerator is declared in the scope that immediately contains the enum-specifier	
				containing = containing.getContainingScope();
			}
		}
		
		Map declarations = containing.getContainedDeclarations();
		
		boolean unnamed = obj.getName().equals( "" );
		
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
			
			if( unnamed || (origList == null) ? isValidOverload( origDecl, obj ) : isValidOverload( origList, obj ) ){
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
				throw new ParserSymbolTableException( ParserSymbolTableException.r_InvalidOverload );
			}
		} else {
			declarations.put( obj.getName(), obj );
		}
		
		//take care of the this pointer
		if( obj.getType() == Declaration.t_function && !obj.isStatic() ){
			addThis( obj );
		}
	}
	
	/**
	 * 
	 * @param name
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 * 
	 * 7.3.1.2-3 If a friend declaration in a non-local class first declares a
	 * class or function, the friend class or function is a member of the
	 * innermost enclosing namespace.
	 * 
	 * TBD: if/when the parser symbol table starts caring about visibility
	 * (public/protected/private) we will need to do more to record friendship.
	 */
	public Declaration addFriend( String name ) throws ParserSymbolTableException{
		Declaration friend = LookupForFriendship( name  );
		
		if( friend == null ){
			friend = new Declaration( name );
			friend.setNeedsDefinition( true );
			
			Declaration decl = (Declaration) _contextStack.peek();
			Declaration containing = decl._containingScope;
			//find innermost enclosing namespace
			while( containing != null && containing.getType() != Declaration.t_namespace ){
				containing = containing._containingScope;
			}
			
			Declaration namespace = (containing == null ) ? _compilationUnit : containing; 
			push( namespace );
			addDeclaration( friend );
			pop(); 
		}
			
		return friend;
	}
	
	/**
	 * 
	 * @param obj
	 * @throws ParserSymbolTableException
	 * 9.3.2-1 In the body of a nonstatic member function... the type of this of
	 * a class X is X*.  If the member function is declared const, the type of
	 * this is const X*, if the member function is declared volatile, the type
	 * of this is volatile X*....
	 */
	private void addThis( Declaration obj ) throws ParserSymbolTableException{
		if( obj.getType() != Declaration.t_function || obj.isStatic() ){
			return;
		}
		
		if( obj._containingScope.isType( Declaration.t_class, Declaration.t_union ) ){
			//check to see if there is already a this object, since using declarations
			//of function will have them from the original declaration
			LookupData data = new LookupData( "this", -1 );
			if( LookupInContained( data, obj ) == null ){
				Declaration thisObj = new Declaration("this");
				thisObj.setType( Declaration.t_type );
				thisObj.setTypeDeclaration( obj._containingScope );
				thisObj.setCVQualifier( obj.getCVQualifier() );
				thisObj.setPtrOperator("*");
				
				push( obj );
				addDeclaration( thisObj );
				pop();
			}
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
			
		if( !data.ignoreUsingDirectives ){
			//check nominated namespaces
			//the transitives list is populated in LookupInNominated, and then 
			//processed in ProcessDirectives
			
			data.visited.clear(); //each namesapce is searched at most once, so keep track
			
			tempList = LookupInNominated( data, inDeclaration, transitives );
					
			if( tempList != null ){
				foundNames.addAll( tempList );
			}
				
			//if we are doing a qualified lookup, only process using directives if
			//we haven't found the name yet (and if we aren't ignoring them). 
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
		}
		
		decl = ResolveAmbiguities( data, foundNames );
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
	
		if( data.associated != null ){
			//we are looking in lookIn, remove it from the associated scopes list
			data.associated.remove( lookIn );
		}
		
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
		
		return ResolveAmbiguities( data, found );
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
		Declaration temp = null;
		Declaration decl = null;
		
		Iterator iterator = null;
		Declaration.ParentWrapper wrapper = null;
		
		if( scopes == null )
			return null;
				
		//use data to detect circular inheritance
		if( data.inheritanceChain == null )
			data.inheritanceChain = new HashSet();
		
		data.inheritanceChain.add( lookIn );
		
		iterator = scopes.iterator();
			
		int size = scopes.size();
	
		for( int i = size; i > 0; i-- )
		{
			wrapper = (Declaration.ParentWrapper) iterator.next();
			if( !wrapper.isVirtual || !data.visited.contains( wrapper.parent ) ){
				if( wrapper.isVirtual ){
					data.visited.add( wrapper.parent );
				}
				
				//HashSet.add returns false if wrapper.parent is already in the set
				//this means we have circular inheritance
				if( data.inheritanceChain.add( wrapper.parent ) ){
				
					//is this name define in this scope?
					temp =  LookupInContained( data, wrapper.parent );
					
					if( temp == null ){
						temp = LookupInParents( data, wrapper.parent );
					}
					
					data.inheritanceChain.remove( wrapper.parent );
					
				} else {
					throw new ParserSymbolTableException( ParserSymbolTableException.r_CircularInheritance );
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
		
		if( (origType >= Declaration.t_class && origType <= Declaration.t_enumeration) && //class name or enumeration ...
			( newType == Declaration.t_type || (newType >= Declaration.t_function && newType <= Declaration.typeMask) ) ){
				
			return true;
		}
		//if the origtype is not a class-name or enumeration name, then the only other
		//allowable thing is if they are both functions.
		return isValidFunctionOverload( origDecl, newDecl );
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
			boolean valid = (( decl.getType() >= Declaration.t_class && decl.getType() <= Declaration.t_enumeration ) ||
							  isValidFunctionOverload( decl, newDecl ));
			
			while( valid && iter.hasNext() ){
				decl = (Declaration) iter.next();
				valid = isValidFunctionOverload( decl, newDecl );
			}
			
			return valid;
		}
		
		//empty list, return true
		return true;
	}
	
	private static boolean isValidFunctionOverload( Declaration origDecl, Declaration newDecl ){
		if( origDecl.getType() != Declaration.t_function || newDecl.getType() != Declaration.t_function ){
			return false;
		}
		
		if( origDecl.hasSameParameters( newDecl ) ){
			//functions with the same name and same parameter types cannot be overloaded if any of them
			//is static
			if( origDecl.isStatic() || newDecl.isStatic() ){
				return false;
			}
			
			//if none of them are static, then the function can be overloaded if they differ in the type
			//of their implicit object parameter.
			if( origDecl.getCVQualifier() != newDecl.getCVQualifier() ){
				return true;
			}
			
			return false;
		}
		
		return true;
	}
	
	static private Declaration ResolveAmbiguities( LookupData data, LinkedList items ) throws ParserSymbolTableException{
		Declaration decl = null;
	
		int size = items.size(); 
	
		if( size == 0){
			return null;
		} else if (size == 1) {
			return (Declaration) items.getFirst();
		} else	{
			Declaration first  = (Declaration)items.removeFirst();
	
			//if first one is a class-name, the next ones hide it
			if( first.getType() >= Declaration.t_class && first.getType() <= Declaration.t_enumeration ){
				return ResolveAmbiguities( data, items );
			}
		
			//else, if the first is an object (ie not a function), the rest must be the same
			//declaration.  otherwise (ie it is a function), the rest must be functions.
			boolean needSame = ( first.getType() != Declaration.t_function );
	
			Iterator iter = items.iterator();
		
			for( int i = (size - 1); i > 0; i-- ){
				decl = (Declaration) iter.next();
			
				if( needSame ){
					if( decl != first ){
						throw new ParserSymbolTableException( ParserSymbolTableException.r_AmbiguousName );
					}
				} else {
					if( decl.getType() != Declaration.t_function ){
						throw new ParserSymbolTableException( ParserSymbolTableException.r_AmbiguousName );
					}
				}
			}
		
			if( needSame ){
				return first;
			} else {
				items.addFirst( first );
				return ResolveFunction( data, items );
			}
		}
	}

	static private Declaration ResolveFunction( LookupData data, LinkedList functions ){
		
		int numParameters = ( data.parameters == null ) ? 0 : data.parameters.size();
		int num;	
			
		//Trim the list down to the set of viable functions
		Declaration function;
		Iterator iter = functions.iterator();
		while( iter.hasNext() ){
			function = (Declaration) iter.next();
			num = ( function._parameters == null ) ? 0 : function._parameters.size();
		
			//if there are m arguments in the list, all candidate functions having m parameters
			//are viable	 
			if( num == numParameters ){
			 	continue;
			} 
			//A candidate function having fewer than m parameters is viable only if it has an 
			//ellipsis in its parameter list.
			else if( num < numParameters ) {
				//TBD ellipsis
				//not enough parameters, remove it
				iter.remove();		
			} 
			//a candidate function having more than m parameters is viable only if the (m+1)-st
			//parameter has a default argument
			else {
				 ListIterator listIter = function._parameters.listIterator( num - 1 );
				 Declaration.ParameterInfo param;
				 for( int i = num; i > ( numParameters - num ); i-- ){
				 	param = (Declaration.ParameterInfo)listIter.previous();
				 	if( !param.hasDefaultValue ){
				 		iter.remove();
				 		break;
					}
				}
			}
		}
		
		//TBD, rank implicit conversion sequences to determine which one is best.
		int size = functions.size();
		if( size == 0 ){
			return null;
		}
		else if( size == 1) {
			return (Declaration) functions.getFirst();
		}
		
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
	
	/**
	 * 
	 * @param obj
	 * @param base
	 * @return boolean
	 * figure out if base is a base class of obj.
	 * 
	 * TBD: Consider rewriting iteratively for performance.
	 */
	static private boolean hasBaseClass( Declaration obj, Declaration base ){
		boolean isABaseClass = false;
		
		if( obj._parentScopes != null ){	
			Declaration decl;
			Declaration.ParentWrapper wrapper;
			
			Iterator iter = obj._parentScopes.iterator();
			int size = obj._parentScopes.size();
			
			for( int i = size; i > 0; i-- ){
				wrapper = (Declaration.ParentWrapper) iter.next();	
				decl = wrapper.parent;
				
				if( decl == base || hasBaseClass( decl, base ) ){
					return true;
				}
			}
		}
		
		return false;
	}

	static private void getAssociatedScopes( Declaration decl, HashSet associated ){
		if( decl == null ){
			return;
		}
		//if T is a class type, its associated classes are the class itself,
		//and its direct and indirect base classes. its associated Namespaces are the 
		//namespaces in which its associated classes are defined	
		if( decl.getType() == Declaration.t_class ){
			associated.add( decl );
			getBaseClassesAndContainingNamespaces( decl, associated );
		} 
		//if T is a union or enumeration type, its associated namespace is the namespace in 
		//which it is defined. if it is a class member, its associated class is the member's
		//class
		else if( decl.getType() == Declaration.t_union || decl.getType() == Declaration.t_enumeration ){
			associated.add( decl._containingScope );
		}
	}
	
	static private void getBaseClassesAndContainingNamespaces( Declaration obj, HashSet classes ){
		if( obj._parentScopes != null ){
			if( classes == null ){
				return;
			}
			
			Iterator iter = obj._parentScopes.iterator();
			int size = obj._parentScopes.size();
			Declaration base;
			
			for( int i = size; i > 0; i-- ){
				base = (Declaration) iter.next();	
				classes.add( base );
				if( base._containingScope.getType() == Declaration.t_namespace ){
					classes.add( base._containingScope );
				}
				
				getBaseClassesAndContainingNamespaces( base, classes );
			}
		}
	}
	
	private Stack _contextStack = new Stack();
	private Declaration _compilationUnit;
	
	private class LookupData
	{
		
		public String name;
		public Map usingDirectives; 
		public Set visited = new HashSet();	//used to ensure we don't visit things more than once
		
		public HashSet inheritanceChain;		//used to detect circular inheritance
		
		public LinkedList parameters;			//parameter info for resolving functions
		public HashSet associated;				//associated namespaces for argument dependant lookup
		public Declaration stopAt;				//stop looking along the stack once we hit this declaration
				 
		public int type = -1;
		public int upperType = 0;
		public boolean qualified = false;
		public boolean ignoreUsingDirectives = false;

		public LookupData( String n, int t ){
			name = n;
			type = t;
		}
	}
}
