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


package org.eclipse.cdt.internal.core.pst;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;


/**
 * @author aniefer
 */

public class ParserSymbolTable {

	/**
	 * Constructor for ParserSymbolTable.
	 */
	public ParserSymbolTable() {
		super();
		_compilationUnit = new Declaration("");
		_compilationUnit.setType( TypeInfo.t_namespace );
	}

	public IContainerSymbol getCompilationUnit(){
		return _compilationUnit;
	}
	
	public IContainerSymbol newContainerSymbol( String name ){
		return new Declaration( name );
	}
	public IContainerSymbol newContainerSymbol( String name, TypeInfo.eType type ){
		return new Declaration( name, type );
	}
	
	public ISymbol newSymbol( String name ){
		return new BasicSymbol( name );
	}
	public ISymbol newSymbol( String name, TypeInfo.eType type ){
		return new BasicSymbol( name, type );
	}
	
	public IDerivableContainerSymbol newDerivableContainerSymbol( String name ){
		return new Declaration( name );
	}
	public IDerivableContainerSymbol newDerivableContainerSymbol( String name, TypeInfo.eType type ){
		return new Declaration( name, type );
	}
	public IParameterizedSymbol newParameterizedSymbol( String name ){
		return new Declaration( name );
	}
	public IParameterizedSymbol newParameterizedSymbol( String name, TypeInfo.eType type ){
		return new Declaration( name, type );
	}
	public ISpecializedSymbol newSpecializedSymbol( String name ){
		return new Declaration( name );
	}
	public ISpecializedSymbol newSpecializedSymbol( String name, TypeInfo.eType type ){
		return new Declaration( name, type );
	}		
	/**
	 * Lookup the name from LookupData starting in the inDeclaration
	 * @param data
	 * @param inDeclaration
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 */
	static private void Lookup( LookupData data, IContainerSymbol inSymbol ) throws ParserSymbolTableException
	{
		if( data.type != TypeInfo.t_any && data.type.compareTo(TypeInfo.t_class) < 0 && data.upperType.compareTo(TypeInfo.t_union) > 0 ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTypeInfo );
		}
		
		ISymbol symbol = null;					//the return value
		LinkedList transitives = new LinkedList();	//list of transitive using directives
		
		//if this name define in this scope?
		LookupInContained( data, inSymbol );
		
		if( !data.ignoreUsingDirectives ){
			//check nominated namespaces
			//the transitives list is populated in LookupInNominated, and then 
			//processed in ProcessDirectives
			
			data.visited.clear(); //each namesapce is searched at most once, so keep track
			
			LookupInNominated( data, inSymbol, transitives );

			//if we are doing a qualified lookup, only process using directives if
			//we haven't found the name yet (and if we aren't ignoring them). 
			if( !data.qualified || data.foundItems == null ){
				ProcessDirectives( inSymbol, data, transitives );
				
				if( inSymbol.hasUsingDirectives() ){
					ProcessDirectives( inSymbol, data, inSymbol.getUsingDirectives() );
				}
							
				while( data.usingDirectives != null && data.usingDirectives.get( inSymbol ) != null ){
					transitives.clear();
					
					LookupInNominated( data, inSymbol, transitives );
	
					if( !data.qualified || data.foundItems == null ){
						ProcessDirectives( inSymbol, data, transitives );
					}
				}
			}
		}
		
		if( data.foundItems != null || data.stopAt == inSymbol ){
			return;
		}
			
		if( inSymbol instanceof IDerivableContainerSymbol ){
			//if we still havn't found it, check any parents we have
			data.visited.clear();	//each virtual base class is searched at most once	
			symbol = LookupInParents( data, (IDerivableContainerSymbol)inSymbol );
					
			//there is a resolveAmbiguities inside LookupInParents, which means if we found
			//something the foundItems set will be non-null, but empty.  So, add the decl into
			//the foundItems set
			if( symbol != null ){
				data.foundItems.add( symbol );	
			}
		}
					
		//if still not found, check our containing scope.			
		if( data.foundItems == null && inSymbol.getContainingSymbol() != null ){ 
			Lookup( data, inSymbol.getContainingSymbol() );
		}

		return;
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
	static private void LookupInNominated( LookupData data, IContainerSymbol symbol, LinkedList transitiveDirectives ){
		//if the data.usingDirectives is empty, there is nothing to do.
		if( data.usingDirectives == null ){
			return;
		}
			
		//local variables
		LinkedList  directives = null; //using directives association with declaration
		Iterator    iter = null;
		IContainerSymbol temp = null;
		
		boolean foundSomething = false;
		int size = 0;
		
		directives = (LinkedList) data.usingDirectives.remove( symbol );
		
		if( directives == null ){
			return;
		}
		
		iter = directives.iterator();
		size = directives.size();
		for( int i = size; i > 0; i-- ){
			temp = (IContainerSymbol) iter.next();

			//namespaces are searched at most once
			if( !data.visited.contains( temp ) ){
				data.visited.add( temp );
				
				foundSomething = LookupInContained( data, temp );
													
				//only consider the transitive using directives if we are an unqualified
				//lookup, or we didn't find the name in decl
				if( (!data.qualified || !foundSomething ) && temp.getUsingDirectives() != null ){
					//name wasn't found, add transitive using directives for later consideration
					transitiveDirectives.addAll( temp.getUsingDirectives() );
				}
			}
		}
		
		return;
	}
	
	/**
	 * function LookupInContained
	 * @param data
	 * @return List
	 * 
	 * Look for data.name in our collection _containedDeclarations
	 */
	private static boolean LookupInContained( LookupData data, IContainerSymbol lookIn ){
	
		boolean foundSomething = false;
		ISymbol temp  = null;
		Object obj = null;
	
		if( data.associated != null ){
			//we are looking in lookIn, remove it from the associated scopes list
			data.associated.remove( lookIn );
		}
		
		Map declarations = lookIn.getContainedSymbols();
		
		obj = ( declarations != null ) ? declarations.get( data.name ) : null;
	
		if( obj != null ){
		 	//the contained declarations map either to a Declaration object, or to a list
		 	//of declaration objects.
			if( obj instanceof ISymbol ){
				temp = (ISymbol) obj;
				//if( ((ISymbol)obj).isType( data.type, data.upperType ) ){
				if( checkType( data, temp, data.type, data.upperType ) ){ 
					if( data.foundItems == null ){
						data.foundItems = new HashSet();
					}
					if( temp.isTemplateMember() )
						data.foundItems.add( temp.getSymbolTable().new TemplateInstance( temp, data.templateInstance.getArgumentMap() ) );
					else
						data.foundItems.add( temp );
						
					foundSomething = true;
				}
			} else {
				//we have to filter on type so can't just add the list whole to the fount set
				LinkedList objList = (LinkedList)obj;
				Iterator iter  = objList.iterator();
				int size = objList.size();
						
				for( int i = 0; i < size; i++ ){
					temp = (ISymbol) iter.next();
			
					//if( temp.isType( data.type, data.upperType ) ){
					if( checkType( data, temp, data.type, data.upperType ) ){
						if( data.foundItems == null ){
							data.foundItems = new HashSet();
						}
						if( temp.isTemplateMember() )
							data.foundItems.add( temp.getSymbolTable().new TemplateInstance( temp, data.templateInstance.getArgumentMap() ) );
						else
							data.foundItems.add(temp);
						foundSomething = true;
					} 
				}
			}
		}

		if( foundSomething ){
			return foundSomething;
		}
		
		if( lookIn instanceof IParameterizedSymbol ){
			Map parameters = ((IParameterizedSymbol)lookIn).getParameterMap();
			if( parameters != null ){
				obj = parameters.get( data.name );
				//if( obj != null && ((ISymbol)obj).isType( data.type, data.upperType ) ){
				if( obj != null && checkType( data, (ISymbol)obj, data.type, data.upperType ) ){
					if( data.foundItems == null ){
						data.foundItems = new HashSet();
					}
					ISymbol symbol = (ISymbol) obj;
					
					if( symbol.isTemplateMember() && data.templateInstance != null ){
						data.foundItems.add( symbol.getSymbolTable().new TemplateInstance( symbol, data.templateInstance.getArgumentMap() ) );
					} else {
						data.foundItems.add( symbol );
					}
					
					foundSomething = true;
				}
			}
		}
		
		return foundSomething;
	}
	
	private static boolean checkType( LookupData data, ISymbol symbol, TypeInfo.eType type, TypeInfo.eType upperType ){
		if( data.templateInstance != null && symbol.isTemplateMember() ){
			if( symbol.isType( TypeInfo.t_type ) ){
				symbol = symbol.getTypeSymbol();
			}
			if( symbol.isType( TypeInfo.t_undef ) && symbol.getContainingSymbol().isType( TypeInfo.t_template ) ){
				TypeInfo info = (TypeInfo) data.templateInstance.getArgumentMap().get( symbol );
				return info.isType( type, upperType );
			}	
		} 
		return symbol.isType( type, upperType );
	}
	
	/**
	 * 
	 * @param data
	 * @param lookIn
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 */
	private static ISymbol LookupInParents( LookupData data, ISymbol lookIn ) throws ParserSymbolTableException{
		IDerivableContainerSymbol container = null;
		if( lookIn instanceof TemplateInstance ){
			
		} else if( lookIn instanceof IDerivableContainerSymbol ){
			container = (IDerivableContainerSymbol) lookIn;
		} else{
			throw new ParserSymbolTableException();
		}
		
		List scopes = container.getParents();
		boolean foundSomething = false;
		ISymbol temp = null;
		ISymbol symbol = null;
		
		Iterator iterator = null;
		IDerivableContainerSymbol.IParentSymbol wrapper = null;
		
		if( scopes == null )
			return null;
				
		//use data to detect circular inheritance
		if( data.inheritanceChain == null )
			data.inheritanceChain = new HashSet();
		
		data.inheritanceChain.add( container );
		
		iterator = scopes.iterator();
			
		int size = scopes.size();
	
		for( int i = size; i > 0; i-- )
		{
			wrapper = (IDerivableContainerSymbol.IParentSymbol) iterator.next();
			ISymbol parent = wrapper.getParent();
			if( parent.isType( TypeInfo.t_undef ) && parent.getContainingSymbol().isType( TypeInfo.t_template ) ){
				TypeInfo info = (TypeInfo) data.templateInstance.getArgumentMap().get( parent );
				if( info.getTypeSymbol() instanceof IDerivableContainerSymbol ){
					parent = (IDerivableContainerSymbol) info.getTypeSymbol();
				} else {
					throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
				}
			}
			if( !wrapper.isVirtual() || !data.visited.contains( parent ) ){
				if( wrapper.isVirtual() ){
					data.visited.add( parent );
				}
				
				//if the inheritanceChain already contains the parent, then that 
				//is circular inheritance
				if( ! data.inheritanceChain.contains( parent ) ){
					//is this name define in this scope?
					if( parent instanceof TemplateInstance ){
						ISymbol tempInstance = data.templateInstance;
						data.templateInstance = (TemplateInstance) parent;
						ISymbol instance = ((TemplateInstance)parent).getInstantiatedSymbol();
						if( instance instanceof IContainerSymbol )
							LookupInContained( data, (IContainerSymbol)instance );
						else 
							throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
						data.templateInstance = tempInstance;
					} else if( parent instanceof IDerivableContainerSymbol ){
						LookupInContained( data, (IDerivableContainerSymbol) parent );
					} else {
						throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTypeInfo );
					}
					temp = ResolveAmbiguities( data );
					if( temp == null ){
						temp = LookupInParents( data, parent );
					}
				} else {
					throw new ParserSymbolTableException( ParserSymbolTableException.r_CircularInheritance );
				}
			}	
			
			if( temp != null && temp.isType( data.type ) ){

				if( symbol == null  ){
					symbol = temp;
				} else if ( temp != null ) {
					//it is not ambiguous if temp & decl are the same thing and it is static
					//or an enumerator
					TypeInfo type = temp.getTypeInfo();
					
					if( symbol == temp && ( type.checkBit( TypeInfo.isStatic ) || type.isType( TypeInfo.t_enumerator ) ) ){
						temp = null;
					} else {
						throw( new ParserSymbolTableException( ParserSymbolTableException.r_Ambiguous ) );
					}
	
				}
			} else {
				temp = null;	//reset temp for next iteration
			}
		}
	
		data.inheritanceChain.remove( container );

		return symbol;	
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
	private static boolean isValidOverload( ISymbol origSymbol, ISymbol newSymbol ){
		TypeInfo.eType origType = origSymbol.getType();
		TypeInfo.eType newType  = newSymbol.getType();
		
		if( (origType.compareTo(TypeInfo.t_class) >= 0 && origType.compareTo(TypeInfo.t_enumeration) <= 0) && //class name or enumeration ...
			( newType == TypeInfo.t_type || (newType.compareTo( TypeInfo.t_function ) >= 0 /*&& newType <= TypeInfo.typeMask*/) ) ){
				
			return true;
		}
		//if the origtype is not a class-name or enumeration name, then the only other
		//allowable thing is if they are both functions.
		if( origSymbol instanceof IParameterizedSymbol && newSymbol instanceof IParameterizedSymbol )
			return isValidFunctionOverload( (IParameterizedSymbol) origSymbol, (IParameterizedSymbol) newSymbol );
		else 
		return false;
	}
	
	private static boolean isValidOverload( LinkedList origList, ISymbol newSymbol ){
		if( origList.size() == 1 ){
			return isValidOverload( (ISymbol)origList.getFirst(), newSymbol );
		} else if ( origList.size() > 1 ){

			//the first thing can be a class-name or enumeration name, but the rest
			//must be functions.  So make sure the newDecl is a function before even
			//considering the list
			if( newSymbol.getType() != TypeInfo.t_function ){
				return false;
			}
			
			Iterator iter = origList.iterator();
			ISymbol symbol = (ISymbol) iter.next();
			boolean valid = ( (symbol.getType().compareTo( TypeInfo.t_class ) >= 0 ) && (symbol.getType().compareTo( TypeInfo.t_enumeration ) <= 0 ) );
			if( !valid && (symbol instanceof IParameterizedSymbol) )
				valid = isValidFunctionOverload( (IParameterizedSymbol)symbol, (IParameterizedSymbol)newSymbol );
			
			while( valid && iter.hasNext() ){
				symbol = (ISymbol) iter.next();
				valid = ( symbol instanceof IParameterizedSymbol) && isValidFunctionOverload( (IParameterizedSymbol)symbol, (IParameterizedSymbol)newSymbol );
			}
			
			return valid;
		}
		
		//empty list, return true
		return true;
	}
	
	private static boolean isValidFunctionOverload( IParameterizedSymbol origSymbol, IParameterizedSymbol newSymbol ){
		if( origSymbol.getType() != TypeInfo.t_function || newSymbol.getType() != TypeInfo.t_function ){
			return false;
		}
		
		if( origSymbol.hasSameParameters( newSymbol ) ){
			//functions with the same name and same parameter types cannot be overloaded if any of them
			//is static
			if( origSymbol.getTypeInfo().checkBit( TypeInfo.isStatic ) || newSymbol.getTypeInfo().checkBit( TypeInfo.isStatic ) ){
				return false;
			}
			
			//if none of them are static, then the function can be overloaded if they differ in the type
			//of their implicit object parameter.
			if( origSymbol.compareCVQualifiersTo( newSymbol ) != 0 ){
				return true;
			}
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param data
	 * @return Declaration
	 * @throws ParserSymbolTableException
	 * 
	 * Resolve the foundItems set down to one declaration and return that
	 * declaration.  
	 * If we successfully resolve, then the data.foundItems list will be
	 * cleared.  If however, we were not able to completely resolve the set,
	 * then the data.foundItems set will be left with those items that
	 * survived the partial resolution and we will return null.  (currently,
	 * this case applies to when we have overloaded functions and no parameter
	 * information)
	 * 
	 * NOTE: data.parameters == null means there is no parameter information at
	 * all, when looking for functions with no parameters, an empty list must be
	 * provided in data.parameters.
	 */
	static private ISymbol ResolveAmbiguities( LookupData data ) throws ParserSymbolTableException{
		ISymbol decl = null;
		ISymbol obj	= null;
		IContainerSymbol cls = null;
		
		if( data.foundItems == null ){
			return null;
		}
		
		int size = data.foundItems.size(); 
		Iterator iter = data.foundItems.iterator();
		
		boolean needDecl = true;
		
		if( size == 0){
			return null;
		} else if (size == 1) {
			decl = (ISymbol) iter.next();
			//if it is a function we need to check its parameters
			if( !decl.isType( TypeInfo.t_function ) ){
				data.foundItems.clear();
				return decl;
			}
			needDecl = false;
		} 
		
		LinkedList functionList = null;	

		for( int i = size; i > 0; i-- ){
			//if we
			if( needDecl ){
				decl = (ISymbol) iter.next();
			} else {
				needDecl = true;
			}
			
			if( decl.isType( TypeInfo.t_function ) ){
				if( functionList == null){
					functionList = new LinkedList();
				}
				functionList.add( decl );
			} else {
				//if this is a class-name, other stuff hides it
				if( decl.isType( TypeInfo.t_class, TypeInfo.t_enumeration ) ){
					if( cls == null ) {
						cls = (IContainerSymbol) decl;
					} else {
						throw new ParserSymbolTableException( ParserSymbolTableException.r_Ambiguous ); 
					}
				} else {
					//an object, can only have one of these
					if( obj == null ){
						obj = decl;	
					} else {
						throw new ParserSymbolTableException( ParserSymbolTableException.r_Ambiguous ); 
					}
				}
			}
			
			decl = null;
		}
	
		data.foundItems.clear();
		
		int numFunctions = ( functionList == null ) ? 0 : functionList.size();
		
		boolean ambiguous = false;
		
		if( cls != null ){
			//the class is only hidden by other stuff if they are from the same scope
			if( obj != null && cls.getContainingSymbol() != obj.getContainingSymbol()){
				ambiguous = true;	
			}
			if( functionList != null ){
				Iterator fnIter = functionList.iterator();
				IParameterizedSymbol fn = null;
				for( int i = numFunctions; i > 0; i-- ){
					fn = (IParameterizedSymbol) fnIter.next();
					if( cls.getContainingSymbol()!= fn.getContainingSymbol()){
						ambiguous = true;
						break;
					}
				}
			}
		}
		
		if( obj != null && !ambiguous ){
			if( numFunctions > 0 ){
				ambiguous = true;
			} else {
				return obj;
			}
		} else if( numFunctions > 0 ) {
			if( data.parameters == null ){
				//we have no parameter information, if we only have one function, return
				//that, otherwise we can't decide between them
				if( numFunctions == 1){
					return (ISymbol) functionList.getFirst();
				} else {
					data.foundItems.addAll( functionList );
					return null;
				}
			} else {
				return ResolveFunction( data, functionList );
			}
		}
		
		if( ambiguous ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_Ambiguous );
		} else {
			return cls;
		}
	}

	static private IParameterizedSymbol ResolveFunction( LookupData data, LinkedList functions ) throws ParserSymbolTableException{
		 
		ReduceToViable( data, functions );
		
		int numSourceParams = ( data.parameters == null ) ? 0 : data.parameters.size();
		int numFns = functions.size();
		
		if( numSourceParams == 0 ){
			//no parameters
			//if there is only 1 viable function, return it, if more than one, its ambiguous
			if( numFns == 0 ){
				return null;
			} else if ( numFns == 1 ){
				return (IParameterizedSymbol)functions.getFirst();
			} else{
				throw new ParserSymbolTableException( ParserSymbolTableException.r_Ambiguous );
			}
		}
		
		IParameterizedSymbol bestFn = null;				//the best function
		IParameterizedSymbol currFn = null;				//the function currently under consideration
		Cost [] bestFnCost = null;				//the cost of the best function
		Cost [] currFnCost = null;				//the cost for the current function
				
		Iterator iterFns = functions.iterator();
		Iterator sourceParams = null;
		Iterator targetParams = null;
		
		int numTargetParams = 0;
		int numParams = 0;
		int comparison;
		Cost cost = null;
		Cost temp = null;
		
		TypeInfo source = null;
		TypeInfo target = null;
		 
		boolean hasWorse = false;
		boolean hasBetter = false;
		boolean ambiguous = false;
		boolean currHasAmbiguousParam = false;
		boolean bestHasAmbiguousParam = false;

		for( int i = numFns; i > 0; i-- ){
			currFn = (IParameterizedSymbol) iterFns.next();
			
			sourceParams = data.parameters.iterator();
			targetParams = currFn.getParameterList().iterator();
			
			//number of parameters in the current function
			numTargetParams = currFn.getParameterList().size();
			
			//we only need to look at the smaller number of parameters
			//(a larger number in the Target means default parameters, a larger
			//number in the source means ellipses.)
			numParams = ( numTargetParams < numSourceParams ) ? numTargetParams : numSourceParams;
			
			if( currFnCost == null ){
				currFnCost = new Cost [ numParams ];	
			}
			
			comparison = 0;
			
			for( int j = 0; j < numParams; j++ ){
				source = (TypeInfo) sourceParams.next();
				target = ((ISymbol)targetParams.next()).getTypeInfo();
				if( source.equals( target ) ){
					cost = new Cost( source, target );
					cost.rank = 0;	//exact match, no cost
				} else {
					cost = checkStandardConversionSequence( source, target );
					
					if( cost.rank == -1){
						temp = checkUserDefinedConversionSequence( source, target );
						if( temp != null ){
							cost = temp;
						}
					}
				}
				
				currFnCost[ j ] = cost;
			}
			
			
			hasWorse = false;
			hasBetter = false;
			
			for( int j = 0; j < numParams; j++ ){ 
				if( currFnCost[ j ].rank < 0 ){
					hasWorse = true;
					hasBetter = false;
					break;
				}
				
				currHasAmbiguousParam = ( currFnCost[ j ].userDefined == 1 );
				
				if( bestFnCost != null ){
					comparison = currFnCost[ j ].compare( bestFnCost[ j ] );
					hasWorse |= ( comparison < 0 );
					hasBetter |= ( comparison > 0 );
				} else {
					hasBetter = true;
				}
			}
				
			ambiguous |= ( hasWorse && hasBetter ) || ( !hasWorse && !hasBetter );
			
			if( !hasWorse ){
				if( hasBetter ){
					ambiguous = false;
					bestFnCost = currFnCost;
					bestHasAmbiguousParam = currHasAmbiguousParam;
					currFnCost = null;
					bestFn = currFn;
				}				
			}
		}

		if( ambiguous || bestHasAmbiguousParam ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_Ambiguous );
		}
						
		return bestFn;
	}
	
	static private void ReduceToViable( LookupData data, LinkedList functions ){
		int numParameters = ( data.parameters == null ) ? 0 : data.parameters.size();
		int num;	
			
		//Trim the list down to the set of viable functions
		IParameterizedSymbol function;
		Iterator iter = functions.iterator();
		while( iter.hasNext() ){
			function = (IParameterizedSymbol) iter.next();
			num = ( function.getParameterList() == null ) ? 0 : function.getParameterList().size();
		
			//if there are m arguments in the list, all candidate functions having m parameters
			//are viable	 
			if( num == numParameters ){
				continue;
			} 
			//A candidate function having fewer than m parameters is viable only if it has an 
			//ellipsis in its parameter list.
			else if( num < numParameters ) {
				//TODO ellipsis
				//not enough parameters, remove it
				iter.remove();		
			} 
			//a candidate function having more than m parameters is viable only if the (m+1)-st
			//parameter has a default argument
			else {
				ListIterator listIter = function.getParameterList().listIterator( num );
				TypeInfo param;
				for( int i = num; i > ( numParameters - num + 1); i-- ){
					param = ((ISymbol)listIter.previous()).getTypeInfo();
					if( !param.getHasDefault() ){
						iter.remove();
						break;
					}
				}
			}
		}
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
	static private void ProcessDirectives( IContainerSymbol symbol, LookupData data, List directives ){
		IContainerSymbol enclosing = null;
		IContainerSymbol temp = null;
		
		int size = directives.size();
		Iterator iter = directives.iterator();
	
		for( int i = size; i > 0; i-- ){
			temp = (IContainerSymbol) iter.next();
		
			//namespaces are searched at most once
			if( !data.visited.contains( temp ) ){
				enclosing = getClosestEnclosingDeclaration( symbol, temp );
						
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
	static private IContainerSymbol getClosestEnclosingDeclaration( ISymbol symbol1, ISymbol symbol2 ){
		if( symbol1 == symbol2 ){ 
			return ( symbol1 instanceof IContainerSymbol ) ? (IContainerSymbol) symbol1 : symbol1.getContainingSymbol();
		}
				
		if( symbol1.getDepth() == symbol2.getDepth() ){
			return getClosestEnclosingDeclaration( symbol1.getContainingSymbol(), symbol2.getContainingSymbol() );
		} else if( symbol1.getDepth() > symbol2.getDepth() ) {
			return getClosestEnclosingDeclaration( symbol1.getContainingSymbol(), symbol2 );
		} else {
			return getClosestEnclosingDeclaration( symbol1, symbol2.getContainingSymbol() );
		}
	}
	
	/**
	 * 
	 * @param obj
	 * @param base
	 * @return int
	 * figure out if base is a base class of obj, and return the "distance" to
	 * the base class.
	 * ie:
	 *     A -> B -> C
	 * the distance from A to B is 1 and from A to C is 2. This distance is used
	 * to rank standard pointer conversions.
	 * 
	 * TBD: Consider rewriting iteratively for performance.
	 */
	static private int hasBaseClass( ISymbol obj, ISymbol base ){
		if( obj == base ){
			return 0;
		}
		IDerivableContainerSymbol symbol = null;
		TemplateInstance instance = null;
		if( obj instanceof TemplateInstance ){
			instance = (TemplateInstance) obj;
			ISymbol temp = instance.getInstantiatedSymbol();
			if( temp instanceof IDerivableContainerSymbol ){
				symbol = (IDerivableContainerSymbol) temp;
			} else {
				return -1;
			}
		} else if( obj instanceof IDerivableContainerSymbol ){
			symbol = (IDerivableContainerSymbol) obj;
		} else {
			return -1;
		}
		
		if( symbol.hasParents() ){	
			ISymbol temp = null;
			IDerivableContainerSymbol parent = null;
			IDerivableContainerSymbol.IParentSymbol wrapper;
			
			Iterator iter = symbol.getParents().iterator();
			int size = symbol.getParents().size();
			
			for( int i = size; i > 0; i-- ){
				wrapper = (IDerivableContainerSymbol.IParentSymbol) iter.next();	
				temp = wrapper.getParent();
				
				if( temp instanceof TemplateInstance ){
					instance = (TemplateInstance) temp;
					if( instance.getInstantiatedSymbol() instanceof IDerivableContainerSymbol ){
						if( instance.getInstantiatedSymbol() == base ){
							return 1;
						} else {
							int n = hasBaseClass( instance, base );
							if( n > 0 ){
								return n + 1;
							}	
						}
					}
				} else {
					if( temp.isType( TypeInfo.t_undef ) && temp.getContainingSymbol().isType( TypeInfo.t_template ) ){
						if( instance == null ) continue;
						TypeInfo info = (TypeInfo) instance.getArgumentMap().get( temp );
						if( info == null || !info.isType( TypeInfo.t_class, TypeInfo.t_struct ) ){
							continue; 
						}
						parent = (IDerivableContainerSymbol) info.getTypeSymbol();
					}
					else if ( temp instanceof IDerivableContainerSymbol ){
						parent = (IDerivableContainerSymbol)temp;
					} else {
						continue; 
					}
					if( parent == base ){
						return 1;
					} else {
						int n = hasBaseClass( parent, base );
						if( n > 0 ){
							return n + 1;
						}
					}	 
				}
			}
		}
		
		return -1;
	}

	static private void getAssociatedScopes( ISymbol symbol, HashSet associated ){
		if( symbol == null ){
			return;
		}
		//if T is a class type, its associated classes are the class itself,
		//and its direct and indirect base classes. its associated Namespaces are the 
		//namespaces in which its associated classes are defined	
		//if( symbol.getType() == TypeInfo.t_class ){
		if( symbol instanceof IDerivableContainerSymbol ){
			associated.add( symbol );
			getBaseClassesAndContainingNamespaces( (IDerivableContainerSymbol) symbol, associated );
		} 
		//if T is a union or enumeration type, its associated namespace is the namespace in 
		//which it is defined. if it is a class member, its associated class is the member's
		//class
		else if( symbol.getType() == TypeInfo.t_union || symbol.getType() == TypeInfo.t_enumeration ){
			associated.add( symbol.getContainingSymbol() );
		}
	}
	
	static private void getBaseClassesAndContainingNamespaces( IDerivableContainerSymbol obj, HashSet classes ){
		if( obj.getParents() != null ){
			if( classes == null ){
				return;
			}
			
			Iterator iter = obj.getParents().iterator();
			int size = obj.getParents().size();
			Declaration.ParentWrapper wrapper;
			IDerivableContainerSymbol base;
			
			for( int i = size; i > 0; i-- ){
				wrapper = (Declaration.ParentWrapper) iter.next();	
				base = (Declaration) wrapper.parent;	
				classes.add( base );
				if( base.getContainingSymbol().getType() == TypeInfo.t_namespace ){
					classes.add( base.getContainingSymbol());
				}
				
				getBaseClassesAndContainingNamespaces( base, classes );
			}
		}
	}
	
	static private boolean okToAddUsingDeclaration( ISymbol obj, IContainerSymbol context ){
		boolean okToAdd = false;
			
		//7.3.3-5  A using-declaration shall not name a template-id
		if( obj.isTemplateMember() && obj.getContainingSymbol().isType( TypeInfo.t_template ) ){
			okToAdd = false;
		}
		//7.3.3-4
		else if( context.isType( TypeInfo.t_class, TypeInfo.t_struct ) ){
			IContainerSymbol container = obj.getContainingSymbol();
			//a member of a base class
			if( obj.getContainingSymbol().getType() == context.getType() ){
				okToAdd = ( hasBaseClass( (IDerivableContainerSymbol) context, (IDerivableContainerSymbol) container ) > 0 );		
			} 
			else if ( obj.getContainingSymbol().getType() == TypeInfo.t_union ) {
				// TODO : must be an _anonymous_ union
				container = container.getContainingSymbol();
				okToAdd = ( container instanceof IDerivableContainerSymbol ) 
						  ? ( hasBaseClass( (IDerivableContainerSymbol)context, (IDerivableContainerSymbol) container ) > 0 )
						  : false; 
			}
			//an enumerator for an enumeration
			else if ( obj.getType() == TypeInfo.t_enumerator ){
				container = container.getContainingSymbol();
				okToAdd = ( container instanceof IDerivableContainerSymbol ) 
						  ? ( hasBaseClass( (IDerivableContainerSymbol)context, (IDerivableContainerSymbol) container ) > 0 )
						  : false; 
			}
		} else {
			okToAdd = true;
		}	
		
		return okToAdd;
	}

	static private Cost lvalue_to_rvalue( TypeInfo source, TypeInfo target ){

		//lvalues will have type t_type
		if( source.isType( TypeInfo.t_type ) ){
			source = getFlatTypeInfo( source );
		}
		
		Cost cost = new Cost( source, target );
		TypeInfo.PtrOp op = null;
		
		if( cost.source.hasPtrOperators() ){
			LinkedList sourcePtrs = cost.source.getPtrOperators();
			TypeInfo.PtrOp ptr = (TypeInfo.PtrOp)sourcePtrs.getFirst();
			
			if( ptr.getType() == TypeInfo.PtrOp.t_reference ){
				sourcePtrs.removeFirst();
			}
			int size = sourcePtrs.size();
			Iterator iter = sourcePtrs.iterator();
			
			for( int i = size; i > 0; i-- ){
				op = (TypeInfo.PtrOp) iter.next();
				if( op.getType() == TypeInfo.PtrOp.t_array ){
					op.setType( TypeInfo.PtrOp.t_pointer );		
				}
			}
		}
		
		if( cost.target.hasPtrOperators() ){
			LinkedList targetPtrs = cost.target.getPtrOperators();
			TypeInfo.PtrOp ptr = (TypeInfo.PtrOp)targetPtrs.getFirst();
			
			if( ptr.getType() == TypeInfo.PtrOp.t_reference ){
				targetPtrs.removeFirst();
			}
			int size = targetPtrs.size();
			Iterator iter = targetPtrs.iterator();
			
			for( int i = size; i > 0; i-- ){
				op = (TypeInfo.PtrOp) iter.next();
				if( op.getType() == TypeInfo.PtrOp.t_array ){
					op.setType( TypeInfo.PtrOp.t_pointer );		
				}
			}
		}
		
		return cost;
	}
	
	static private void qualificationConversion( Cost cost ){
		int size = cost.source.hasPtrOperators() ? cost.source.getPtrOperators().size() : 0;
		int size2 = cost.target.hasPtrOperators() ? cost.target.getPtrOperators().size() : 0;
		
		TypeInfo.PtrOp op1 = null, op2 = null;
		boolean canConvert = true;
		
		Iterator iter1 = cost.source.hasPtrOperators() ? cost.source.getPtrOperators().iterator() : null;
		Iterator iter2 = cost.target.hasPtrOperators() ? cost.target.getPtrOperators().iterator() : null;
		
		if( size != size2 ){
			cost.qualification = 0;
			return;
		} else if ( size == 1 ){
			op1 = (TypeInfo.PtrOp) iter1.next();
			op2 = (TypeInfo.PtrOp) iter2.next();
			
			if( ( op1.isConst()    && !op2.isConst() ) ||
				( op1.isVolatile() && !op2.isVolatile() ) )
			{
				cost.qualification = 0;
				return;
			}
			canConvert = true;
		} else if( size > 0 ){
			op1 = (TypeInfo.PtrOp) iter1.next();
			op2 = (TypeInfo.PtrOp) iter2.next();

			boolean constInEveryCV2k = true;
			
			for( int j= 1; j < size; j++ ){
				op1 = (TypeInfo.PtrOp) iter1.next();
				op2 = (TypeInfo.PtrOp) iter2.next();
				
				//pointer types are similar
				if( op1.getType() != op2.getType() ){
					canConvert = false;
					break;
				}
				//if const is in cv1,j then const is in cv2,j.  Similary for volatile
				if( ( op1.isConst()    && !op2.isConst()    ) ||
				    ( op1.isVolatile() && !op2.isVolatile() )  )
				{
					canConvert = false;
					break;
				}
				
				//if cv1,j and cv2,j are different then const is in every cv2,k for 0<k<j
				if( ( op1.compareCVTo( op2 ) != 0 ) && !constInEveryCV2k ){
					canConvert = false;
					break; 
				}
				
				constInEveryCV2k &= op2.isConst();
			}
		}
		
		if( canConvert == true ){
			cost.qualification = 1;
			cost.rank = 0;
		} else {
			cost.qualification = 0;
		}
	}
		
	/**
	 * 
	 * @param source
	 * @param target
	 * @return int
	 * 
	 * 4.5-1 char, signed char, unsigned char, short int or unsigned short int
	 * can be converted to int if int can represent all the values of the source
	 * type, otherwise they can be converted to unsigned int.
	 * 4.5-2 wchar_t or an enumeration can be converted to the first of the
	 * following that can hold it: int, unsigned int, long unsigned long.
	 * 4.5-4 bool can be promoted to int 
	 * 4.6 float can be promoted to double
	 */
	static private void promotion( Cost cost ){
		TypeInfo src = cost.source;
		TypeInfo trg = cost.target;
		 
		int mask = TypeInfo.isShort | TypeInfo.isLong | TypeInfo.isUnsigned;
		
		if( (src.isType( TypeInfo.t_bool, TypeInfo.t_float ) || src.isType( TypeInfo.t_enumeration )) &&
			(trg.isType( TypeInfo.t_int ) || trg.isType( TypeInfo.t_double )) )
		{
			if( src.getType() == trg.getType() && (( src.getTypeInfo() & mask) == (trg.getTypeInfo() & mask)) ){
				//same, no promotion needed
				return;	
			}
			
			if( src.isType( TypeInfo.t_float ) ){ 
				cost.promotion = trg.isType( TypeInfo.t_double ) ? 1 : 0;
			} else {
				cost.promotion = ( trg.isType( TypeInfo.t_int ) && trg.canHold( src ) ) ? 1 : 0;
			}
			
		} else {
			cost.promotion = 0;
		}
		
		cost.rank = (cost.promotion > 0 ) ? 1 : -1;
	}
	
	/**
	 * 
	 * @param source
	 * @param target
	 * @return int
	 * 
	 */
	static private void conversion( Cost cost ){
		TypeInfo src = cost.source;
		TypeInfo trg = cost.target;
		
		int temp;
		
		cost.conversion = 0;
		cost.detail = 0;
		
		if( !src.hasSamePtrs( trg ) ){
			return;
		} 
		if( src.hasPtrOperators() && src.getPtrOperators().size() == 1 ){
			TypeInfo.PtrOp ptr = (TypeInfo.PtrOp)src.getPtrOperators().getFirst();
			if( ptr.getType() == TypeInfo.PtrOp.t_pointer ){
				ISymbol srcDecl = src.isType( TypeInfo.t_type ) ? src.getTypeSymbol() : null;
				ISymbol trgDecl = trg.isType( TypeInfo.t_type ) ? trg.getTypeSymbol() : null;
	
				if( srcDecl == null || (trgDecl == null && !trg.isType( TypeInfo.t_void )) ){
					return;	
				}
				
				//4.10-2 an rvalue of type "pointer to cv T", where T is an object type can be
				//converted to an rvalue of type "pointer to cv void"
				if( trg.isType( TypeInfo.t_void ) ){
					cost.rank = 2;
					cost.conversion = 1;
					cost.detail = 2;
					return;	
				}
				
				cost.detail = 1;
				
				//4.10-3 An rvalue of type "pointer to cv D", where D is a class type can be converted
				// to an rvalue of type "pointer to cv B", where B is a base class of D.
				if( (srcDecl instanceof IDerivableContainerSymbol) && trgDecl.isType( srcDecl.getType() ) ){
					temp = hasBaseClass( (IDerivableContainerSymbol) srcDecl, (IDerivableContainerSymbol) trgDecl );
					cost.rank = 2;
					cost.conversion = ( temp > -1 ) ? temp : 0;
					cost.detail = 1;
					return;
				}
				
				//4.11-2 An rvalue of type "pointer to member of B of type cv T", where B is a class type, 
				//can be converted to an rvalue of type "pointer to member of D of type cv T" where D is a
				//derived class of B
				if( srcDecl.getContainingSymbol().isType( TypeInfo.t_class ) && trgDecl.getContainingSymbol().isType( TypeInfo.t_class ) ){
					temp = hasBaseClass( (IDerivableContainerSymbol)trgDecl.getContainingSymbol(), (IDerivableContainerSymbol)srcDecl.getContainingSymbol() );
					cost.rank = 2;
					cost.conversion = ( temp > -1 ) ? temp : 0;
					return;
				}
			}
		} else if( !src.hasPtrOperators() ) {
			//4.7 An rvalue of an integer type can be converted to an rvalue of another integer type.  
			//An rvalue of an enumeration type can be converted to an rvalue of an integer type.
			if( src.isType( TypeInfo.t_bool, TypeInfo.t_int ) ||
				src.isType( TypeInfo.t_float, TypeInfo.t_double ) ||
				src.isType( TypeInfo.t_enumeration ) )
			{
				if( trg.isType( TypeInfo.t_bool, TypeInfo.t_int ) ||
					trg.isType( TypeInfo.t_float, TypeInfo.t_double ) )
				{
					cost.rank = 2;
					cost.conversion = 1;	
				}
			}
		}
	}
	
	static private Cost checkStandardConversionSequence( TypeInfo source, TypeInfo target ){
		Cost cost = lvalue_to_rvalue( source, target );
		
		if( cost.source.equals( cost.target ) ){
			cost.rank = 0;
			return cost;
		}
	
		qualificationConversion( cost );
		
		//if we can't convert the qualifications, then we can't do anything
		if( cost.qualification == 0 ){
			return cost;
		}
		
		promotion( cost );
		if( cost.promotion > 0 || cost.rank > -1 ){
			return cost;
		}
		
		conversion( cost );
		
		return cost;	
	}
	
	static private Cost checkUserDefinedConversionSequence( TypeInfo source, TypeInfo target ) throws ParserSymbolTableException {
		Cost cost = null;
		Cost constructorCost = null;
		Cost conversionCost = null;

		ISymbol targetDecl = null;
		ISymbol sourceDecl = null;
		IParameterizedSymbol constructor = null;
		IParameterizedSymbol conversion = null;
		
		//constructors
		if( target.getType() == TypeInfo.t_type ){
			targetDecl = target.getTypeSymbol();
			if( targetDecl.isType( TypeInfo.t_class, TypeInfo.t_union ) ){
				LookupData data = new LookupData( "", TypeInfo.t_function, null );
				LinkedList params = new LinkedList();
				params.add( source );
				data.parameters = params;
				if( targetDecl instanceof TemplateInstance ){
					data.templateInstance = targetDecl;
					TemplateInstance instance = (TemplateInstance) targetDecl;
					LookupInContained( data, (IContainerSymbol) instance.getInstantiatedSymbol() );
				} else {
					LookupInContained( data, (IContainerSymbol) targetDecl );
				}
				
				constructor = (IParameterizedSymbol)ResolveAmbiguities( data );
			}
		}
		
		//conversion operators
		if( source.getType() == TypeInfo.t_type ){
			source = getFlatTypeInfo( source );
			sourceDecl = source.getTypeSymbol();
			
			if( sourceDecl != null && (sourceDecl instanceof IContainerSymbol) ){
				String name = target.toString();
				
				if( !name.equals("") ){
					LookupData data = new LookupData( "operator " + name, TypeInfo.t_function, null );
					LinkedList params = new LinkedList();
					data.parameters = params;
					
					LookupInContained( data, (IContainerSymbol) sourceDecl );
					conversion = (IParameterizedSymbol)ResolveAmbiguities( data );	
				}
			}
		}
		
		if( constructor != null ){
			constructorCost = checkStandardConversionSequence( (TypeInfo) new TypeInfo( TypeInfo.t_type, 0, constructor.getContainingSymbol() ), target );
		}
		if( conversion != null ){
			conversionCost = checkStandardConversionSequence( (TypeInfo) new TypeInfo( target.getType(), 0, target.getTypeSymbol() ), target );
		}
		
		//if both are valid, then the conversion is ambiguous
		if( constructorCost != null && constructorCost.rank != -1 && 
			conversionCost != null && conversionCost.rank != -1 )
		{
			cost = constructorCost;
			cost.userDefined = 1;
			cost.rank = 3;
		} else {
			if( constructorCost != null && constructorCost.rank != -1 ){
				cost = constructorCost;
				cost.userDefined = constructor.hashCode();
				cost.rank = 3;
			} else if( conversionCost != null && conversionCost.rank != -1 ){
				cost = conversionCost;
				cost.userDefined = conversion.hashCode();
				cost.rank = 3;
			} 			
		}
		
		return cost;
	}

	/**
	 * 
	 * @param decl
	 * @return TypeInfo
	 * The top level TypeInfo represents modifications to the object and the
	 * remaining TypeInfo's represent the object.
	 */
	// TODO move this to ITypeInfo ?
	static private TypeInfo getFlatTypeInfo( TypeInfo topInfo ){
		TypeInfo returnInfo = topInfo;
		TypeInfo info = null;
		
		if( topInfo.getType() == TypeInfo.t_type ){
			returnInfo = (TypeInfo)new TypeInfo();
			
			ISymbol typeSymbol = topInfo.getTypeSymbol();
			
			info = topInfo.getTypeSymbol().getTypeInfo();
			
			while( info.getType() == TypeInfo.t_type ){
				typeSymbol = info.getTypeSymbol();
				
				//returnInfo.addCVQualifier( info.getCVQualifier() );
				returnInfo.addPtrOperator( info.getPtrOperators() );	
				
				info = info.getTypeSymbol().getTypeInfo();
			}
			
			if( info.isType( TypeInfo.t_class, TypeInfo.t_enumeration ) ){
				returnInfo.setType( TypeInfo.t_type );
				returnInfo.setTypeSymbol( typeSymbol );
			} else {
				returnInfo.setTypeInfo( info.getTypeInfo() );
				returnInfo.setType( info.getType() );
				returnInfo.setTypeSymbol( null );
			}
			
			returnInfo.applyPtrsAsUnaryOperators( topInfo.getPtrOperators() );
		}
		
		return returnInfo;	
	}

	static private IParameterizedSymbol matchTemplatePartialSpecialization( IParameterizedSymbol template, LinkedList args ){
		if( template == null ){
			return null;
		}
		
		LinkedList specs = template.getSpecializations();
		int size = specs.size();
		if( specs == null || size == 0 ){
			return template;
		}
		 
		IParameterizedSymbol bestMatch = null;
		boolean bestMatchIsBest = true;
		Iterator iter = specs.iterator();
		IParameterizedSymbol spec = null;
		LinkedList specArgs = null;
		for( int i = size; i > 0; i-- ){
			spec = (IParameterizedSymbol) iter.next();
			specArgs = spec.getArgumentList();
			if( specArgs == null || specArgs.size() != args.size() ){
				continue;
			}
			
			ISymbol sym1 = null, sym2 = null;
			Iterator iter1 = specArgs.iterator();
			Iterator iter2 = args.iterator();
			
			HashMap map = new HashMap();
			String name = null;
			boolean match = true;
			for( int j = specArgs.size(); j > 0; j-- ){
				sym1 = (ISymbol)iter1.next();
				sym2 = (ISymbol)iter2.next();
				
				name = sym1.getName();
				if( name.equals( "" ) ){
					//no name, only type
				} else if( map.containsKey( name ) ) {
					ISymbol val = (ISymbol) map.get( name );
					if( val.getType() != sym2.getType() ){
						match = false;
						break;
					}
				} else {
					map.put( name, sym2 );
				}
			}
			if( match ){
				int compare = orderSpecializations( bestMatch, spec );
				if( compare == 0 ){
					bestMatchIsBest = false; 
				} else if( compare < 0 ) {
					bestMatch = spec;
					bestMatchIsBest = true;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Compare spec1 to spec2.  Return > 0 if spec1 is more specialized, < 0 if spec2
	 * is more specialized, = 0 otherwise.
	 * @param spec1
	 * @param spec2
	 * @return
	 */
	static private int orderSpecializations( IParameterizedSymbol spec1, IParameterizedSymbol spec2 ){
		
		Iterator iter = spec1.getContainedSymbols().keySet().iterator();
		ISymbol decl = (ISymbol) spec1.getContainedSymbols().get( iter.next() );
		
		//to order class template specializations, we need to transform them into function templates
		if( decl.isType( TypeInfo.t_class ) ) {
			spec1 = transformClassTemplateToFunctionTemplate( spec1 );
			spec2 = transformClassTemplateToFunctionTemplate( spec2 );	
		}
		
		IParameterizedSymbol transformed1 = transformFunctionTemplateForOrdering( spec1 );
		IParameterizedSymbol transformed2 = transformFunctionTemplateForOrdering( spec2 );

		//Using the transformed parameter list, perform argument deduction against the other
		//function template		
		boolean d1 = deduceTemplateArguments( spec2, transformed1.getParameterList() );
		boolean d2 = deduceTemplateArguments( spec1, transformed2.getParameterList() );
		 
		//The transformed  template is at least as specialized as the other iff the deduction
		//succeeds and the deduced parameter types are an exact match
		//A template is more specialized than another iff it is at least as specialized as the
		//other template and that template is not at least as specialized as the first.
		if( d1 && d2 || !d1 && !d2 )
			return 0;
		else if( d1 && !d2 )
			return 1;
		else 
			return -1;
	}
	
	/**
	 * 
	 * @param template
	 * @param args
	 * @return
	 * 
	 * A type that is specified in terms of template parameters (P) is compared with an actual 
	 * type (A), and an attempt is made to find template argument vaules that will make P, 
	 * after substitution of the deduced values, compatible with A.
	 */
	static private boolean deduceTemplateArguments( IParameterizedSymbol template, LinkedList args ){
		return true;	
	}
	
	/**
	 * transform the class template to a function template as described in the spec
	 * 14.5.4.2-1
	 * @param template
	 * @return IParameterizedSymbol
	 * the function template has the same template parameters as the partial specialization and
	 * has a single function parameter whose type is a class template specialization with the template 
	 * arguments of the partial specialization
	 */
	static private IParameterizedSymbol transformClassTemplateToFunctionTemplate( IParameterizedSymbol template ){
		return null;
	}
	
	/**
	 * transform a function template for use in partial ordering, as described in the
	 * spec 14.5.5.2-3 
	 * @param template
	 * @return
	 * -for each type template parameter, synthesize a unique type and substitute that for each
	 * occurence of that parameter in the function parameter list
	 * -for each non-type template parameter, synthesize a unique value of the appropriate type and
	 * susbstitute that for each occurence of that parameter in the function parameter list
	 * for each template template parameter, synthesize a unique class template and substitute that
	 * for each occurence of that parameter in the function parameter list
	 */
	static private IParameterizedSymbol transformFunctionTemplateForOrdering( IParameterizedSymbol template ){
		return null;		
	}

	//private Stack _contextStack = new Stack();
	private Declaration _compilationUnit;
	private LinkedList undoList = new LinkedList();
	private HashSet markSet = new HashSet();
	
	private void pushCommand( Command command ){
		undoList.addFirst( command );
	}
	
	public Mark setMark(){
		Mark mark = new Mark();
		undoList.addFirst( mark );
		markSet.add( mark );
		return mark;
	}
	
	public boolean rollBack( Mark toMark ){
		if( markSet.contains( toMark ) ){
			markSet.remove( toMark );
			Command command = ( Command )undoList.removeFirst();
			while( command != toMark ){
				command.undoIt();
				command = ( Command ) undoList.removeFirst();
			}
			
			return true;
		} 
		
		return false;
	}
	
	public boolean commit( Mark toMark ){
		if( markSet.contains( toMark ) ){
			markSet.remove( toMark );
			Command command = ( Command )undoList.removeLast();
			while( command != toMark ){
				command = (Command) undoList.removeLast();
			}
			return true;
		}
		
		return false;
	}
	
	static abstract private class Command{
		abstract public void undoIt();
	}
	
	static public class Mark extends Command{
		public void undoIt(){ };
	}
	
	static private class AddDeclarationCommand extends Command{
		AddDeclarationCommand( BasicSymbol newDecl, Declaration context, boolean removeThis ){
			_decl = newDecl;
			_context = context;
			_removeThis = removeThis;
		}
		public void undoIt(){
			Object obj = _context.getContainedSymbols().get( _decl.getName() );
			
			if( obj instanceof LinkedList ){
				LinkedList list = (LinkedList)obj;
				ListIterator iter = list.listIterator();
				int size = list.size();
				Declaration item = null;
				for( int i = 0; i < size; i++ ){
					item = (Declaration)iter.next();
					if( item == _decl ){
						iter.remove();
						break;
					}
				}
				if( list.size() == 1 ){
					_context.getContainedSymbols().remove( _decl.getName() );
					_context.getContainedSymbols().put( _decl.getName(), list.getFirst() );
				}
			} else if( obj instanceof BasicSymbol ){
				_context.getContainedSymbols().remove( _decl.getName() );
			}
			if( _removeThis ){
				_context.getContainedSymbols().remove( "this" );
			}
		}
		
		private BasicSymbol _decl;
		private Declaration _context; 
		private boolean 	_removeThis;
	}
	
	static private class AddParentCommand extends Command{
		public AddParentCommand( Declaration container, Declaration.ParentWrapper wrapper ){
			_decl = container;
			_wrapper = wrapper;
		}
		
		public void undoIt(){
			List parents = _decl.getParents();
			parents.remove( _wrapper );
		}
		
		private Declaration _decl;
		private Declaration.ParentWrapper _wrapper;
	}
	
	static private class AddParameterCommand extends Command{
		public AddParameterCommand( Declaration container, BasicSymbol parameter ){
			_decl = container;
			_param = parameter;
		}
		
		public void undoIt(){
			_decl.getParameterList().remove( _param );
			
			String name = _param.getName();
			if( name != null && !name.equals("") )
			{	
				_decl.getParameterMap().remove( name );
			}
		}
		
		private Declaration _decl;
		private BasicSymbol _param;
	}
	
	static private class AddArgumentCommand extends Command{
		public AddArgumentCommand( Declaration container, BasicSymbol arg ){
			_decl = container;
			_arg = arg;
		}
		public void undoIt(){
			_decl.getArgumentList().remove( _arg );
		}

		private Declaration _decl;
		private BasicSymbol _arg;
	}
	
	static private class AddUsingDirectiveCommand extends Command{
		public AddUsingDirectiveCommand( Declaration container, Declaration namespace ){
			_decl = container;
			_namespace = namespace;
		}
		public void undoIt(){
			_decl.getUsingDirectives().remove( _namespace );
		}
		private Declaration _decl;
		private Declaration _namespace;
	}
	
	static private class LookupData
	{
		
		public String name;
		public Map usingDirectives; 
		public Set visited = new HashSet();	//used to ensure we don't visit things more than once
		
		public HashSet inheritanceChain;		//used to detect circular inheritance
		
		public LinkedList parameters;			//parameter info for resolving functions
		public HashSet associated;				//associated namespaces for argument dependant lookup
		public ISymbol stopAt;					//stop looking along the stack once we hit this declaration
				 
		public TypeInfo.eType type = TypeInfo.t_any;
		public TypeInfo.eType upperType = TypeInfo.t_undef;
		public boolean qualified = false;
		public boolean ignoreUsingDirectives = false;

		public HashSet foundItems = null;
		
		public ISymbol templateInstance = null;
		
		public LookupData( String n, TypeInfo.eType t, ISymbol i ){
			name = n;
			type = t;
			templateInstance = i;
		}
	}
	
	static private class Cost
	{
		public Cost( TypeInfo s, TypeInfo t ){
			source = s;
			target = t;
		}
		
		public TypeInfo source;
		public TypeInfo target;
		
		public int lvalue;
		public int promotion;
		public int conversion;
		public int qualification;
		public int userDefined;
		public int rank = -1;
		public int detail;
		
		public int compare( Cost cost ){
			int result = 0;
			
			if( rank != cost.rank ){
				return cost.rank - rank;
			}
			
			if( userDefined != 0 || cost.userDefined != 0 ){
				if( userDefined == 0 || cost.userDefined == 0 ){
					return cost.userDefined - userDefined;
				} else {
					if( (userDefined == 1 || cost.userDefined == 1) ||
						(userDefined != cost.userDefined ) )
					{
						return 0;
					} 
					// else they are the same constructor/conversion operator and are ranked
					//on the standard conversion sequence
				}
			}
			
			if( promotion > 0 || cost.promotion > 0 ){
				result = cost.promotion - promotion;
			}
			if( conversion > 0 || cost.conversion > 0 ){
				if( detail == cost.detail ){
					result = cost.conversion - conversion;
				} else {
					result = cost.detail - detail;
				}
			}
			
			if( result == 0 ){
				if( cost.qualification != qualification ){
					return cost.qualification - qualification;
				} else if( (cost.qualification == qualification) && qualification == 0 ){
					return 0;
				} else {
					int size = cost.target.hasPtrOperators() ? cost.target.getPtrOperators().size() : 0;
					int size2 = target.hasPtrOperators() ? target.getPtrOperators().size() : 0;
					
					ListIterator iter1 = cost.target.getPtrOperators().listIterator( size );
					ListIterator iter2 = target.getPtrOperators().listIterator( size2 );
					
					TypeInfo.PtrOp op1 = null, op2 = null;
					
					int subOrSuper = 0;
					for( int i = ( size < size2 ) ? size : size2; i > 0; i-- ){
						op1 = (TypeInfo.PtrOp)iter1.previous();
						op2 = (TypeInfo.PtrOp)iter2.previous();
						
						if( subOrSuper == 0)
							subOrSuper = op1.compareCVTo( op2 );
						else if( ( subOrSuper > 0 && ( op1.compareCVTo( op2 ) < 0 )) ||
								 ( subOrSuper < 0 && ( op1.compareCVTo( op2 ) > 0 )) )
						{
							result = -1;
							break;	
						}
					}
					if( result == -1 ){
						result = 0;
					} else {
						if( size == size2 ){
							result = subOrSuper;
						} else {
							result = size - size2; 
						}
					}
				}
			}
			 
			return result;
		}
	}

	public class BasicSymbol implements Cloneable, ISymbol
	{
		public BasicSymbol( String name ){
			super();
			_name = name;
			_typeInfo = new TypeInfo();
		}
		
		public BasicSymbol( String name, Object obj ){
			super();
			_name   = name;
			_object = obj;
			_typeInfo = new TypeInfo();
		}
		
		public BasicSymbol( String name, TypeInfo.eType typeInfo )
		{
			super();
			_name = name;
			_typeInfo = new TypeInfo( typeInfo, 0, this );
		}
		
		public ParserSymbolTable getSymbolTable(){
			return ParserSymbolTable.this;
		}
		
		public Object clone(){
			BasicSymbol copy = null;
			try{
				copy = (BasicSymbol)super.clone();
			} catch ( CloneNotSupportedException e ){
				//should not happen
				return null;
			}
			copy._object = null;
			return copy;	
		}
		
		public String getName() { return _name; }
		public void setName(String name) { _name = name; }

		public Object getCallbackExtension() { return _object; }
		public void setCallbackExtension( Object obj ) { _object = obj; }
			
		public IContainerSymbol getContainingSymbol() { return _containingScope; }
		public void setContainingSymbol( IContainerSymbol scope ){ 
			_containingScope = ( Declaration )scope;
			_depth = scope.getDepth() + 1; 
		}
	
		public void setType(TypeInfo.eType t){
			_typeInfo.setType( t );	 
		}
	
		public TypeInfo.eType getType(){ 
			return _typeInfo.getType(); 
		}
	
		public boolean isType( TypeInfo.eType type ){
			return _typeInfo.isType( type, TypeInfo.t_undef ); 
		}

		public boolean isType( TypeInfo.eType type, TypeInfo.eType upperType ){
			return _typeInfo.isType( type, upperType );
		}
		
		public ISymbol getTypeSymbol(){	
			return _typeInfo.getTypeSymbol(); 
		}
	
		public void setTypeSymbol( ISymbol type ){
			_typeInfo.setTypeSymbol( type ); 
		}

		public TypeInfo getTypeInfo(){
			return _typeInfo;
		}
		
		public void setTypeInfo( TypeInfo info ) {
			_typeInfo = info;
		}
		
		/**
		 * returns 0 if same, non zero otherwise
		 */
		public int compareCVQualifiersTo( ISymbol symbol ){
			int size = symbol.getTypeInfo().hasPtrOperators() ? symbol.getTypeInfo().getPtrOperators().size() : 0;
			int size2 = getTypeInfo().hasPtrOperators() ? getTypeInfo().getPtrOperators().size() : 0;
				
			if( size != size2 ){
				return size2 - size;
			} else {
				Iterator iter1 = symbol.getTypeInfo().getPtrOperators().iterator();
				Iterator iter2 = getTypeInfo().getPtrOperators().iterator();
	
				TypeInfo.PtrOp op1 = null, op2 = null;
	
				int subOrSuper = 0;
				for( int i = size; i > 0; i-- ){
					op1 = (TypeInfo.PtrOp)iter1.next();
					op2 = (TypeInfo.PtrOp)iter2.next();
		
					if( op1.compareCVTo( op2 ) != 0 ){
						return -1;
					}
				}
			}
			
			return 0;
		}
		
		public LinkedList getPtrOperators(){
			return _typeInfo.getPtrOperators();
		}
		public void addPtrOperator( TypeInfo.PtrOp ptrOp ){
			_typeInfo.addPtrOperator( ptrOp );
		}	
		
		public int getDepth(){
			return _depth;
		}
		
		public boolean isTemplateMember(){
			return _isTemplateMember;
		}
		public void setIsTemplateMember( boolean isMember ){
			_isTemplateMember = isMember;
		}
		public ISymbol getTemplateInstance(){
			return _templateInstance;
		}
		public void setTemplateInstance( TemplateInstance instance ){
			_templateInstance = instance;
		}
		public HashMap getArgumentMap(){
			return null;
		}
		private 	String 		_name;					//our name
		private		Object 		_object;				//the object associated with us
		private		TypeInfo	_typeInfo;				//our type info
		private		Declaration	_containingScope;		//the scope that contains us
		private		int 		_depth;					//how far down the scope stack we are
		
		private		boolean		_isTemplateMember = false;		
		private		TemplateInstance	_templateInstance;		
	}
	
	public class TemplateInstance extends BasicSymbol
	{
		protected TemplateInstance( ISymbol symbol, HashMap argMap ){
			super("");
			_instantiatedSymbol = symbol;
			symbol.setTemplateInstance( this );
			_argumentMap = argMap;
		}
		
		public boolean equals( Object t ){
			if( t == null || !( t instanceof TemplateInstance ) ){ 
				return false;
			}
			
			TemplateInstance instance = (TemplateInstance) t;
			
			if( _instantiatedSymbol != instance._instantiatedSymbol ){
				return false;
			}
			
			//check arg map
			Iterator iter1 = _argumentMap.keySet().iterator();
			Iterator iter2 = instance._argumentMap.keySet().iterator();
			int size = _argumentMap.size();
			int size2 = instance._argumentMap.size();
			ISymbol t1 = null, t2 = null;
			if( size == size2 ){
				for( int i = size; i > 0; i-- ){
					t1 = (ISymbol)iter1.next();
					t2 = (ISymbol)iter2.next();
					if( t1 != t2 || !_argumentMap.get(t1).equals( instance._argumentMap.get(t2) ) ){
						return false;								
					}
				}
			}
			
			return true;
		}
		
		public ISymbol getInstantiatedSymbol(){
			_instantiatedSymbol.setTemplateInstance( this );
			return _instantiatedSymbol;
		}
		
		public TypeInfo.eType getType(){
			ISymbol symbol = _instantiatedSymbol;
			TypeInfo.eType returnType = _instantiatedSymbol.getType();
			if( returnType == TypeInfo.t_type ){
				symbol = symbol.getTypeSymbol();
				TypeInfo info = null;	
				while( symbol != null && symbol.getType() == TypeInfo.t_undef && symbol.getContainingSymbol().getType() == TypeInfo.t_template ){
					info = (TypeInfo) _argumentMap.get( symbol );
					if( !info.isType( TypeInfo.t_type ) ){
						break;
					}
					symbol = info.getTypeSymbol();
				}
				
				return ( info != null ) ? info.getType() : TypeInfo.t_type;
			}
			
			return returnType; 
		}
	
		public boolean isType( TypeInfo.eType type ){
			return ( type == TypeInfo.t_any || getType() == type );
		}

		public boolean isType( TypeInfo.eType type, TypeInfo.eType upperType ){
			if( type == TypeInfo.t_any )
				return true;
	
			if( upperType == TypeInfo.t_undef ){
				return ( getType() == type );
			} else {
				return ( getType().compareTo( type ) >= 0 && getType().compareTo( upperType ) <= 0 );
			}
		}
		
		public ISymbol getTypeSymbol(){
			ISymbol symbol = _instantiatedSymbol.getTypeSymbol();
			if( symbol != null && symbol.getType() == TypeInfo.t_undef && 
								  symbol.getContainingSymbol().getType() == TypeInfo.t_template )
			{
				TypeInfo info = (TypeInfo) _argumentMap.get( symbol );
				return ( info != null ) ? info.getTypeSymbol() : null;	
			}
			
			return symbol; 
		}
	
		public TypeInfo getTypeInfo(){
			ISymbol symbol = _instantiatedSymbol.getTypeSymbol();
			if( symbol != null && symbol.getType() == TypeInfo.t_undef && 
								  symbol.getContainingSymbol().getType() == TypeInfo.t_template )
			{
				TypeInfo info = (TypeInfo) _argumentMap.get( symbol );
				return info;
			}
			
			return _instantiatedSymbol.getTypeInfo();
		}
			
		public HashMap getArgumentMap(){
			return _argumentMap;
		}

		
		private ISymbol			 _instantiatedSymbol;
		//private LinkedList		 _arguments;
		private HashMap			 _argumentMap;
		private Object			 _callbackExtension;
		
	}
	
	public class Declaration extends BasicSymbol implements Cloneable, 
												   			IContainerSymbol, 
												   			IDerivableContainerSymbol, 
												   			IParameterizedSymbol, 
												   			ISpecializedSymbol
	{

		public Declaration( String name ){
			super( name );
		}
	
		public Declaration( String name, Object obj ){
			super( name, obj );
		}
		
		public Declaration( String name, TypeInfo.eType typeInfo )
		{
			super( name, typeInfo );
		}

		/**
		 * clone
		 * @see java.lang.Object#clone()
		 * 
		 * implement clone for the purposes of using declarations.
		 * int   		_typeInfo;				//by assignment
		 * String 		_name;					//by assignment
		 * Object 		_object;				//null this out
		 * Declaration	_typeDeclaration;		//by assignment
		 * Declaration	_containingScope;		//by assignment
		 * LinkedList 	_parentScopes;			//shallow copy
		 * LinkedList 	_usingDirectives;		//shallow copy
		 * HashMap		_containedDeclarations;	//shallow copy
		 * int 			_depth;					//by assignment
		 */
		public Object clone(){
			Declaration copy = (Declaration)super.clone();
			
			copy._parentScopes          = ( _parentScopes != null ) ? (LinkedList) _parentScopes.clone() : null;
			copy._usingDirectives       = ( _usingDirectives != null ) ? (LinkedList) _usingDirectives.clone() : null; 
			copy._containedDeclarations = ( _containedDeclarations != null ) ? (HashMap) _containedDeclarations.clone() : null;
			copy._parameterList         = ( _parameterList != null ) ? (LinkedList) _parameterList.clone() : null;
			copy._parameterHash 		= ( _parameterHash != null ) ? (HashMap) _parameterHash.clone() : null;
		
			return copy;	
		}
	
		public void addParent( ISymbol parent ){
			addParent( parent, false );
		}
		public void addParent( ISymbol parent, boolean virtual ){
			if( _parentScopes == null ){
				_parentScopes = new LinkedList();
			}
			
			ParentWrapper wrapper = new ParentWrapper( parent, virtual );
			_parentScopes.add( wrapper );
			
			Command command = new AddParentCommand( this, wrapper );
			pushCommand( command );
		}
	
		public void addParent( IDerivableContainerSymbol.IParentSymbol wrapper ){
			if( _parentScopes == null ){
				_parentScopes = new LinkedList();
			}
			
			//ParentWrapper wrapper = new ParentWrapper( parent, virtual );
			_parentScopes.add( wrapper );
			
			Command command = new AddParentCommand( this, (ParentWrapper) wrapper );
			pushCommand( command );			
		}
		
		public Map getContainedSymbols(){
			return _containedDeclarations;
		}
	
		public Map createContained(){
			if( _containedDeclarations == null )
				_containedDeclarations = new HashMap();
		
			return _containedDeclarations;
		}

		public boolean hasParents(){
			return ( _parentScopes != null && !_parentScopes.isEmpty() );
		}
		
		public List getParents(){
			return _parentScopes;
		}
	
		public boolean needsDefinition(){
			return _needsDefinition;
		}
		public void setNeedsDefinition( boolean need ) {
			_needsDefinition = need;
		}
	
		

		//public void addPtrOperator( String ptrStr, boolean isConst, boolean isVolatile ){
		//	_typeInfo.addPtrOperator( ptrStr, isConst, isVolatile );
		//}
	
		public TypeInfo.eType getReturnType(){
			return _returnType;
		}
	
		public void setReturnType( TypeInfo.eType type ){
			_returnType = type;
		}
	
		public LinkedList getParameterList(){
			return _parameterList;
		}
		
		public void setParameterList( LinkedList list ){
			_parameterList = list;	
		}
		
		public HashMap getParameterMap(){
			return _parameterHash;
		}
		
		public LinkedList getArgumentList(){
			return _argumentList;
		}
		public void addArgument( ISymbol arg ){
			if( _argumentList == null ){
				_argumentList = new LinkedList();	
			}
			_argumentList.addLast( arg );
			
			Command command = new AddArgumentCommand( this, (BasicSymbol) arg );
			pushCommand( command );
		}
		
		public void addParameter( ISymbol param ){
			if( _parameterList == null )
				_parameterList = new LinkedList();
			
			_parameterList.addLast( param );
			String name = param.getName();
			if( name != null && !name.equals("") )
			{
				if( _parameterHash == null )
					_parameterHash = new HashMap();

				if( !_parameterHash.containsKey( name ) )
					_parameterHash.put( name, param );
			}
			param.setContainingSymbol( this );
			param.setIsTemplateMember( isTemplateMember() || getType() == TypeInfo.t_template );
			
			Command command = new AddParameterCommand( this, (BasicSymbol)param );
			pushCommand( command );
		}
		
		public void addParameter( ISymbol typeSymbol, TypeInfo.PtrOp ptrOp, boolean hasDefault ){
			BasicSymbol param = new BasicSymbol("");
			
			TypeInfo info = param.getTypeInfo();
			info.setType( TypeInfo.t_type );
			info.setTypeSymbol( typeSymbol );
			info.addPtrOperator( ptrOp );
			info.setHasDefault( hasDefault );
				
			addParameter( param );
		}
	
		public void addParameter( TypeInfo.eType type, int info, TypeInfo.PtrOp ptrOp, boolean hasDefault ){
			BasicSymbol param = new BasicSymbol("");
					
			TypeInfo t = param.getTypeInfo();
			t.setTypeInfo( info );
			t.setType( type );
			t.addPtrOperator( ptrOp );
			t.setHasDefault( hasDefault );
				
			addParameter( param );
		}
	
		public boolean hasSameParameters( IParameterizedSymbol function ){
			if( function.getType() != getType() ){
				return false;	
			}
		
			int size = getParameterList().size();
			if( function.getParameterList().size() != size ){
				return false;
			}
		
			Iterator iter = getParameterList().iterator();
			Iterator fIter = function.getParameterList().iterator();
		
			TypeInfo info = null;
			TypeInfo fInfo = null;
		
			for( int i = size; i > 0; i-- ){
				info = ((BasicSymbol)iter.next()).getTypeInfo();
				fInfo = ((BasicSymbol) fIter.next()).getTypeInfo();
			
				if( !info.equals( fInfo ) ){
					return false;
				}
			}
		
			
			return true;
		}
	
		public void addSymbol( ISymbol obj ) throws ParserSymbolTableException{
			Declaration containing = this;
			
			//handle enumerators
			if( obj.getType() == TypeInfo.t_enumerator ){
				//a using declaration of an enumerator will not be contained in a
				//enumeration.
				if( containing.getType() == TypeInfo.t_enumeration ){
					//Following the closing brace of an enum-specifier, each enumerator has the type of its 
					//enumeration
					obj.setTypeSymbol( containing );
					//Each enumerator is declared in the scope that immediately contains the enum-specifier	
					containing = (Declaration) containing.getContainingSymbol();
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
		
			boolean unnamed = obj.getName().equals( "" );
		
			Object origObj = null;
		
			obj.setContainingSymbol( containing );

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
			
				if( unnamed || ((origList == null) ? isValidOverload( origDecl, obj ) : isValidOverload( origList, obj ) )){					
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
		
			obj.setIsTemplateMember( isTemplateMember() || getType() == TypeInfo.t_template );
			
			//take care of the this pointer
			TypeInfo type = obj.getTypeInfo();
			boolean addedThis = false;
			if( type.isType( TypeInfo.t_function ) && !type.checkBit( TypeInfo.isStatic ) ){
				addThis( (Declaration) obj );
				addedThis = true;
			}
			
			Command command = new AddDeclarationCommand( (BasicSymbol) obj, containing, addedThis );
			pushCommand( command );
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
		private void addThis( Declaration obj ){
			TypeInfo type = obj.getTypeInfo();
			if( !type.isType( TypeInfo.t_function ) || type.checkBit( TypeInfo.isStatic ) ){
				return;
			}
	
			if( obj.getContainingSymbol().isType( TypeInfo.t_class, TypeInfo.t_union ) ){
				//check to see if there is already a this object, since using declarations
				//of function will have them from the original declaration
				LookupData data = new LookupData( "this", TypeInfo.t_any, null );
				LookupInContained( data, obj );
				//if we didn't find "this" then foundItems will still be null, no need to actually
				//check its contents 
				if( data.foundItems == null ){
					Declaration thisObj = new Declaration("this");
					thisObj.setType( TypeInfo.t_type );
					thisObj.setTypeSymbol( obj.getContainingSymbol() );
					//thisObj.setCVQualifier( obj.getCVQualifier() );
					TypeInfo.PtrOp ptr = new TypeInfo.PtrOp();
					ptr.setType( TypeInfo.PtrOp.t_pointer );
					if( obj.getTypeInfo().hasPtrOperators() ){
						ptr.setConst( ((TypeInfo.PtrOp) obj.getPtrOperators().getFirst()).isConst() );
						ptr.setVolatile( ((TypeInfo.PtrOp) obj.getPtrOperators().getFirst()).isVolatile() );
					}
					
					thisObj.addPtrOperator(ptr);
					
					try{
						obj.addSymbol( thisObj );
					} catch ( ParserSymbolTableException e ) {
						//won't happen because we checked that "this" didn't exist already
					}
					
				}
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
		 * TODO: if/when the parser symbol table starts caring about visibility
		 * (public/protected/private) we will need to do more to record friendship.
		 */
		public Declaration addFriend( String name ) throws ParserSymbolTableException{
			Declaration friend = LookupForFriendship( name  );
		
			if( friend == null ){
				friend = new Declaration( name );
				friend.setNeedsDefinition( true );
			
				Declaration containing = (Declaration)getContainingSymbol();
				//find innermost enclosing namespace
				while( containing != null && containing.getType() != TypeInfo.t_namespace ){
					containing = (Declaration)containing.getContainingSymbol();
				}
			
				Declaration namespace = ( containing == null ) ? (Declaration)ParserSymbolTable.this.getCompilationUnit() : containing;
				namespace.addSymbol( friend );
			}
			
			return friend;
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
			LookupData data = new LookupData( name, TypeInfo.t_any, getTemplateInstance() );
		
			boolean inClass = ( getType() == TypeInfo.t_class);
		
			Declaration enclosing = (Declaration) getContainingSymbol();
			while( enclosing != null && (inClass ? enclosing.getType() != TypeInfo.t_class
												  :	enclosing.getType() == TypeInfo.t_namespace) )
			{                                        		
				enclosing = (Declaration) enclosing.getContainingSymbol();
			}

			data.stopAt = enclosing;
		
			ParserSymbolTable.Lookup( data, this );
			return (Declaration)ParserSymbolTable.ResolveAmbiguities( data ); 
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
		public ISymbol addUsingDeclaration( String name ) throws ParserSymbolTableException {
			return addUsingDeclaration( name, null );
		}

		public ISymbol addUsingDeclaration( String name, IContainerSymbol declContext ) throws ParserSymbolTableException{
			LookupData data = new LookupData( name, TypeInfo.t_any, null );
	
			if( declContext != null ){				
				data.qualified = true;
				data.templateInstance = declContext.getTemplateInstance();
				ParserSymbolTable.Lookup( data, declContext );
			} else {
				ParserSymbolTable.Lookup( data, this );
			}
	
			//figure out which declaration we are talking about, if it is a set of functions,
			//then they will be in data.foundItems (since we provided no parameter info);
			BasicSymbol obj = (BasicSymbol)ParserSymbolTable.ResolveAmbiguities( data );
	
			if( data.foundItems == null ){
				throw new ParserSymbolTableException();				
			}

			BasicSymbol clone = null;

			//if obj != null, then that is the only object to consider, so size is 1,
			//otherwise we consider the foundItems set				
			int size = ( obj == null ) ? data.foundItems.size() : 1;
			Iterator iter = data.foundItems.iterator();
			for( int i = size; i > 0; i-- ){
				obj = ( obj != null && size == 1 ) ? obj : (Declaration) iter.next();
		
				if( ParserSymbolTable.okToAddUsingDeclaration( obj, this ) ){
					clone = (BasicSymbol) obj.clone(); //7.3.3-9
					addSymbol( clone );
				} else {
					throw new ParserSymbolTableException();
				}
			}
	
			return ( size == 1 ) ? clone : null;
		}
		
		public void addUsingDirective( IContainerSymbol namespace ) throws ParserSymbolTableException{
			if( namespace.getType() != TypeInfo.t_namespace ){
				throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTypeInfo );
			}
					
			if( _usingDirectives == null ){
				_usingDirectives = new LinkedList(); 
			}
		
			_usingDirectives.add( namespace );
			
			Command command = new AddUsingDirectiveCommand( this, (Declaration)namespace );
			pushCommand( command );
		}
		
		public boolean hasUsingDirectives(){
			return ( _usingDirectives != null && !_usingDirectives.isEmpty() );
		}
		
		public List getUsingDirectives(){
			return _usingDirectives;
		}
		
		public ISymbol ElaboratedLookup( TypeInfo.eType type, String name ) throws ParserSymbolTableException{
			LookupData data = new LookupData( name, type, getTemplateInstance() );
		
			ParserSymbolTable.Lookup( data, this );
		
			return ParserSymbolTable.ResolveAmbiguities( data ); 
		}
		
		public ISymbol Lookup( String name ) throws ParserSymbolTableException {
			LookupData data = new LookupData( name, TypeInfo.t_any, getTemplateInstance() );
		
			ParserSymbolTable.Lookup( data, this );
		
			return ParserSymbolTable.ResolveAmbiguities( data ); 
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
		public ISymbol LookupMemberForDefinition( String name ) throws ParserSymbolTableException{
			LookupData data = new LookupData( name, TypeInfo.t_any, getTemplateInstance() );
			data.qualified = true;
	
			ParserSymbolTable.LookupInContained( data, this );
		
			return ParserSymbolTable.ResolveAmbiguities( data );
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
		public IContainerSymbol LookupNestedNameSpecifier( String name ) throws ParserSymbolTableException {
			return LookupNestedNameSpecifier( name, this );
		}
		private Declaration LookupNestedNameSpecifier(String name, Declaration inDeclaration ) throws ParserSymbolTableException{		
			Declaration foundDeclaration = null;
		
			LookupData data = new LookupData( name, TypeInfo.t_namespace, getTemplateInstance() );
			data.upperType = TypeInfo.t_union;
		
			ParserSymbolTable.LookupInContained( data, inDeclaration );
		
			if( data.foundItems != null ){
				foundDeclaration = (Declaration) ParserSymbolTable.ResolveAmbiguities( data );//, data.foundItems );
			}
				
			if( foundDeclaration == null && inDeclaration.getContainingSymbol() != null ){
				foundDeclaration = LookupNestedNameSpecifier( name, (Declaration)inDeclaration.getContainingSymbol() );
			}
			
			return foundDeclaration;
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
		public IParameterizedSymbol MemberFunctionLookup( String name, LinkedList parameters ) throws ParserSymbolTableException{
			LookupData data = new LookupData( name, TypeInfo.t_function, getTemplateInstance() );
			//if parameters == null, thats no parameters, but we need to distinguish that from
			//no parameter information at all, so make an empty list.
			data.parameters = ( parameters == null ) ? new LinkedList() : parameters;
			
			ParserSymbolTable.Lookup( data, (IContainerSymbol) this );
			return (IParameterizedSymbol) ParserSymbolTable.ResolveAmbiguities( data ); 
		}
		
		public IParameterizedSymbol QualifiedFunctionLookup( String name, LinkedList parameters ) throws ParserSymbolTableException{
			LookupData data = new LookupData( name, TypeInfo.t_function, getTemplateInstance() );
			data.qualified = true;
			//if parameters == null, thats no parameters, but we need to distinguish that from
			//no parameter information at all, so make an empty list.
			data.parameters = ( parameters == null ) ? new LinkedList() : parameters;
		
			ParserSymbolTable.Lookup( data, (IContainerSymbol)this );
		
			return (IParameterizedSymbol) ParserSymbolTable.ResolveAmbiguities( data ); 
		}
		
		public ISymbol QualifiedLookup( String name ) throws ParserSymbolTableException{
			LookupData data = new LookupData( name, TypeInfo.t_any, getTemplateInstance() );
			data.qualified = true;
			ParserSymbolTable.Lookup( data, this );
		
			return ParserSymbolTable.ResolveAmbiguities( data ); 
		}
		
		public TemplateInstance TemplateLookup( String name, LinkedList arguments ) throws ParserSymbolTableException
		{
			LookupData data = new LookupData( name, TypeInfo.t_any, getTemplateInstance() );
			data.parameters = arguments;
			
			ParserSymbolTable.Lookup( data, (IContainerSymbol) this );
			ISymbol found = ParserSymbolTable.ResolveAmbiguities( data );
			if( found.isType( TypeInfo.t_template ) ){
				return ((IParameterizedSymbol) found).instantiate( arguments );
			} 
			return null;
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
		public IParameterizedSymbol UnqualifiedFunctionLookup( String name, LinkedList parameters ) throws ParserSymbolTableException{
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
			
				ParserSymbolTable.getAssociatedScopes( paramType, associated );
			
				//if T is a pointer to a data member of class X, its associated namespaces and classes
				//are those associated with the member type together with those associated with X
				if( param.hasPtrOperators() && param.getPtrOperators().size() == 1 ){
					TypeInfo.PtrOp op = (TypeInfo.PtrOp)param.getPtrOperators().getFirst();
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
		
			ParserSymbolTable.Lookup( data, this );
		
			Declaration found = (Declaration)ResolveAmbiguities( data );
		
			//if we haven't found anything, or what we found is not a class member, consider the 
			//associated scopes
			if( found == null || found.getContainingSymbol().getType() != TypeInfo.t_class ){
				if( found != null ){
					data.foundItems.add( found );
				}
									
				Declaration decl;
				Declaration temp;

				//dump the hash to an array and iterate over the array because we
				//could be removing items from the collection as we go and we don't
				//want to get ConcurrentModificationExceptions			
				Object [] scopes = associated.toArray();
			
				size = associated.size();

				for( int i = 0; i < size; i++ ){
					decl  = (Declaration) scopes[ i ];
					if( associated.contains( decl ) ){
						data.qualified = true;
						data.ignoreUsingDirectives = true;
						ParserSymbolTable.Lookup( data, decl );
					}
				}
			
				found = (Declaration)ParserSymbolTable.ResolveAmbiguities( data );
			}
		
			return found;
		}
		
		public boolean hasSpecializations(){
			return ( _specializations != null && !_specializations.isEmpty() );
		}
		
		public LinkedList	getSpecializations(){
			return _specializations;
		}
		
		public void addSpecialization( IParameterizedSymbol spec ){
			if( _specializations == null ){
				_specializations = new LinkedList();
			}
			_specializations.add( spec );	
		}

		public TemplateInstance instantiate( LinkedList arguments ) throws ParserSymbolTableException{
			if( getType() != TypeInfo.t_template ){
				return null;
			}
			List paramList = getParameterList();
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
			
			if( getContainedSymbols().size() != 1 ){
				throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );
			}
			
			Iterator iter = getContainedSymbols().keySet().iterator();
			IContainerSymbol symbol = (IContainerSymbol) getContainedSymbols().get( iter.next() );
			 
			TemplateInstance instance = new TemplateInstance( symbol, map );
			return instance;
		}
		
		
		private		boolean		_needsDefinition;		//this name still needs to be defined
		

		private		LinkedList 	_parentScopes;			//inherited scopes (is base classes)
		private		LinkedList 	_usingDirectives;		//collection of nominated namespaces
		private		HashMap 	_containedDeclarations;	//declarations contained by us.
	
		private		LinkedList	_specializations;		//template specializations
		private		LinkedList	_argumentList;				//template specialization arguments
		
		private 	LinkedList	_parameterList;			//have my cake
		private 	HashMap		_parameterHash;			//and eat it too
		
		private 	TypeInfo.eType	_returnType;			
	
		
		
		protected class ParentWrapper implements IDerivableContainerSymbol.IParentSymbol
		{
			public ParentWrapper( ISymbol p, boolean v ){
				parent    = p;
				isVirtual = v;
			}
		
			public void setParent( ISymbol parent ){
				this.parent = (Declaration) parent;
			}
			
			public ISymbol getParent(){
				return parent;
			}
			
			public boolean isVirtual(){
				return isVirtual;
			}
			
			public void setVirtual( boolean virtual ){
				isVirtual = virtual;
			}
			
			private boolean isVirtual = false;
			private ISymbol parent = null;
		}
	}
	
	static public class TypeInfo {
		public TypeInfo(){
			super();	
		}
	
		public TypeInfo( eType type, int info, ISymbol symbol ){
			super();
			_typeInfo = info;
			_type = type;
			_typeDeclaration = symbol;	
		}
	
		public TypeInfo( eType type, int info, ISymbol symbol, PtrOp op, boolean hasDefault ){
			super();
			_typeInfo = info;
			_type = type;
			_typeDeclaration = symbol;
			if( op != null ){
				_ptrOperators = new LinkedList();
				_ptrOperators.add( op );
			} else {
				_ptrOperators = null;
			}
			_hasDefaultValue = hasDefault;
		}
		
		public TypeInfo( eType type, int info, ISymbol symbol, PtrOp op, Object def ){
			super();
			_typeInfo = info;
			_type = type;
			_typeDeclaration = symbol;
			if( op != null ){
				_ptrOperators = new LinkedList();
				_ptrOperators.add( op );
			} else {
				_ptrOperators = null;
			}
			_hasDefaultValue = true;
			setDefault( def );
		}
	
		public TypeInfo( TypeInfo info ){
			super();
		
			_typeInfo = info._typeInfo;
			_type = info._type;
			_typeDeclaration = info._typeDeclaration;
			_ptrOperators = ( info._ptrOperators == null ) ? null : (LinkedList)info._ptrOperators.clone();
			_hasDefaultValue = info._hasDefaultValue;
		}
	
		public static final int typeMask   = 0x001f;
		public static final int isAuto     = 0x0020;
		public static final int isRegister = 0x0040;
		public static final int isStatic   = 0x0080;
		public static final int isExtern   = 0x0100;
		public static final int isMutable  = 0x0200;
		public static final int isInline   = 0x0400;
		public static final int isVirtual  = 0x0800;
		public static final int isExplicit = 0x1000;
		public static final int isTypedef  = 0x2000;
		public static final int isFriend   = 0x4000;
		public static final int isConst    = 0x8000;
		public static final int isVolatile = 0x10000;
		public static final int isUnsigned = 0x20000;
		public static final int isShort    = 0x40000;
		public static final int isLong     = 0x80000;
		
		// Types (maximum type is typeMask
		// Note that these should be considered ordered and if you change
		// the order, you should consider the ParserSymbolTable uses
		public static final eType t_any         = new eType( -1 ); //don't care
		public static final eType t_undef       = new eType(  0 ); //not specified
		public static final eType t_type        = new eType(  1 ); //Type Specifier
		public static final eType t_namespace   = new eType(  2 );
		public static final eType t_class       = new eType(  3 );
		public static final eType t_struct      = new eType(  4 );
		public static final eType t_union       = new eType(  5 );
		public static final eType t_enumeration = new eType(  6 );
		public static final eType t_function    = new eType(  7 );
		public static final eType t_bool        = new eType(  8 );
		public static final eType t_char        = new eType(  9 );
		public static final eType t_wchar_t     = new eType( 10 );
		public static final eType t_int         = new eType( 11 );
		public static final eType t_float       = new eType( 12 );
		public static final eType t_double      = new eType( 13 );
		public static final eType t_void        = new eType( 14 );
		public static final eType t_enumerator  = new eType( 15 );
		public static final eType t_block       = new eType( 16 );
		public static final eType t_template    = new eType( 17 );
		
		public static class eType implements Comparable{
			private eType( int v ){
				_val = v;
			}
			
			public int compareTo( Object o ){
				eType t = (eType) o;
				return _val - t._val;
			}
			
			public int toInt(){
				return _val;
			}
			
			private int _val;
		}
		
		public static class PtrOp {
			public PtrOp( eType type ){
				this.type = type;
			}
			public PtrOp( eType type, boolean isConst, boolean isVolatile ){
				this.type = type;
				this.isConst = isConst;
				this.isVolatile = isVolatile;
			}
			public PtrOp(){
				super();
			}
			
			public static final eType t_undef	  = new eType( 0 );
			public static final eType t_pointer   = new eType( 1 );
			public static final eType t_reference = new eType( 2 );
			public static final eType t_array = new eType( 3 );
			
			
			private eType type = t_undef;
			private boolean isConst = false;
			private boolean isVolatile = false;
			
			public eType getType()			 { return type; }
			public void setType( eType type ){ this.type = type; }
			
			public boolean isConst()	{ return isConst; }
			public boolean isVolatile()	{ return isVolatile; }
			public void setConst( boolean isConst ) 	 { this.isConst = isConst; }
			public void setVolatile( boolean isVolatile ){ this.isVolatile = isVolatile; }
			
			public int compareCVTo( PtrOp ptr ){
				int cv1 = ( isConst() ? 1 : 0 ) + ( isVolatile() ? 1 : 0 );
				int cv2 = ( ptr.isConst() ? 1 : 0 ) + ( ptr.isVolatile() ? 1 : 0 );
				
				return cv1 - cv2;
			}
			public boolean equals( Object o ){
				if( o == null || !(o instanceof PtrOp) ){
					return false;
				}	
				PtrOp op = (PtrOp)o;
				
				return ( isConst() == op.isConst() &&
						 isVolatile() == op.isVolatile() &&
						 getType() == op.getType() );
			}
			
		}

		private static final String _image[] = {	"", 
													"", 
													"namespace", 
													"template",
													"class", 
													"struct", 
													"union", 
													"enum",
													"",
													"bool",
													"char",
													"wchar_t",
													"int",
													"float",
													"double",
													"void",
													""
												 };
		//Partial ordering :
		// none		< const
		// none     < volatile
		// none		< const volatile
		// const	< const volatile
		// volatile < const volatile
		public static final int cvConst 		= 2;
		public static final int cvVolatile 		= 3;
		public static final int cvConstVolatile = 5;
	
			// Convenience methods
		public void setBit(boolean b, int mask){
			if( b ){
				_typeInfo = _typeInfo | mask; 
			} else {
				_typeInfo = _typeInfo & ~mask; 
			} 
		}
		
		public boolean checkBit(int mask){
			return (_typeInfo & mask) != 0;
		}	
		
		public void setType( eType t){
			_type = t; 
		}
		
		public eType getType(){ 
			return _type; 
		}
	
		public boolean isType( eType type ){
			return isType( type, t_undef ); 
		}
	
		public int getTypeInfo(){
			return _typeInfo;
		}
	
		public void setTypeInfo( int typeInfo ){
			_typeInfo = typeInfo;
		}
	
		/**
		 * 
		 * @param type
		 * @param upperType
		 * @return boolean
		 * 
		 * type checking, check that this declaration's type is between type and
		 * upperType (inclusive).  upperType of 0 means no range and our type must
		 * be type.
		 */
		public boolean isType( eType type, eType upperType ){
			//type of -1 means we don't care
			if( type == t_any )
				return true;
		
			//upperType of 0 means no range
			if( upperType == t_undef ){
				return ( getType() == type );
			} else {
				return ( getType().compareTo( type ) >= 0 && getType().compareTo( upperType ) <= 0 );
			}
		}
		
		public ISymbol getTypeSymbol(){	
			return _typeDeclaration; 
		}
	
		public void setTypeSymbol( ISymbol type ){
			_typeDeclaration = type; 
		}

		public boolean hasPtrOperators(){
			return ( _ptrOperators != null && _ptrOperators.size() > 0 );	
		}
		
		public LinkedList getPtrOperators(){
			return _ptrOperators;
		}
		
		public boolean hasSamePtrs( TypeInfo type ){
			int size = hasPtrOperators() ? getPtrOperators().size() : 0;
			int size2 = type.hasPtrOperators() ? type.getPtrOperators().size() : 0;
			if( size == size2 ){
				if( size > 0 ){
					Iterator iter1 = getPtrOperators().iterator();
					Iterator iter2 = type.getPtrOperators().iterator();
					PtrOp ptr1 = null, ptr2 = null;
					for( int i = size; i > 0; i-- ){
						ptr1 = (PtrOp)iter1.next();
						ptr2 = (PtrOp)iter2.next();
						if( ptr1.getType() != ptr2.getType() ){
							return false;
						}
					}
				}
				return true;
			}
			return false;
		}

		public void applyPtrsAsUnaryOperators( LinkedList ptrs ){
			if( ptrs == null || ptrs.isEmpty() )
				return;
				
			int size = ptrs.size();
			Iterator iter = ptrs.iterator();
			PtrOp op = null;
			for( int i = size; i > 0; i-- ){
				op = (PtrOp)iter.next();
				if( op.getType() == PtrOp.t_pointer ){
					//indirection operator, can only be applied to a pointer
					if( hasPtrOperators() ){
						PtrOp first = (PtrOp)getPtrOperators().getFirst();
						if( first.getType() == PtrOp.t_pointer )
						{
							getPtrOperators().removeFirst();
							if( op.isConst() || op.isVolatile() ){
								
								if( hasPtrOperators() ){
									((PtrOp)getPtrOperators().getFirst()).setConst( op.isConst() );
									((PtrOp)getPtrOperators().getFirst()).setVolatile( op.isVolatile() );
								} else {
									PtrOp newOp = new PtrOp( PtrOp.t_undef, op.isConst(), op.isVolatile() );
									addPtrOperator( newOp );
								}
							}
						}
					} else {
						//???
					}
				} else if( op.getType() == PtrOp.t_reference ){
					//Address-of unary operator, results in pointer to T
					//TODO or pointer to member
					PtrOp newOp = new PtrOp( PtrOp.t_pointer, op.isConst(), op.isVolatile() );
					addPtrOperator( newOp );
				}
			}
		}
	
		public void addPtrOperator( PtrOp ptr ){
			if( _ptrOperators == null ){
				_ptrOperators = new LinkedList();
			}
			if( ptr != null )
				_ptrOperators.add( ptr );	
		}
		
		public void addPtrOperator( List ptrs ){
			if( _ptrOperators == null ){
				_ptrOperators = new LinkedList();
			}
			if( ptrs != null )
				_ptrOperators.addAll( ptrs );
		}
		
		public boolean getHasDefault(){
			return _hasDefaultValue;
		}

		public void setHasDefault( boolean def ){
			_hasDefaultValue = def;
		}
		public void setDefault( Object t ){
			_defaultValue = t;
		}
		public Object getDefault(){
			return _defaultValue;
		}

		/**
		 * canHold
		 * @param type
		 * @return boolean
		 * return true is the our type can hold all the values of the passed in
		 * type.
		 * TODO, for now return true if our type is "larger" (based on ordering of
		 * the type values)
		 */
		public boolean canHold( TypeInfo type ){
			return getType().compareTo( type.getType() ) >= 0;	
		}
	
		public boolean equals( Object t ){
			if( t == null || !(t instanceof TypeInfo) ){
				return false;
			}
		
			TypeInfo type = (TypeInfo)t;
		
			boolean result = ( _typeInfo == type._typeInfo );
			result &= ( _type == type._type );
			
			if( _typeDeclaration instanceof TemplateInstance ){
				result &= _typeDeclaration.equals( type._typeDeclaration );
			} else {
				result &= ( _typeDeclaration == type._typeDeclaration );
			}
			
		
			int size1 = (_ptrOperators == null) ? 0 : _ptrOperators.size();
			int size2 = (type._ptrOperators == null) ? 0 : type._ptrOperators.size();
			if( size1 == size2 ){
				if( size1 != 0 ){
					Iterator iter1 = _ptrOperators.iterator();
					Iterator iter2 = type._ptrOperators.iterator();
					
					PtrOp op1 = null, op2 = null;
					for( int i = size1; i > 0; i-- ){
						op1 = (PtrOp)iter1.next();
						op2 = (PtrOp)iter2.next();
						
						if( !op1.equals(op2) ){
							return false;
						}
					}
				}
			} else {
				return false;
			}
			
			return result;
		}
	
		public String toString(){
			if( isType( t_type ) ){
				return _typeDeclaration.getName();
			} else {
				return _image[ getType().toInt() ];
			}
		}

		private int 	_typeInfo = 0;
		private eType   _type = t_undef;
		private ISymbol _typeDeclaration;	
	
		private boolean	_hasDefaultValue = false;
		private Object _defaultValue = null;
		private LinkedList _ptrOperators;	
	}
}
