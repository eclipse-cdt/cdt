/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol.IParentSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable.Cost;
import org.eclipse.cdt.internal.core.parser.pst.TypeInfo.PtrOp;

/**
 * @author aniefer
 */
public final class TemplateEngine {

	static protected TypeInfo instantiateTypeInfo( TypeInfo info, ITemplateSymbol template, Map  argMap ) throws ParserSymbolTableException{
		if( argMap == null )
			return info;

		if( info.isType( TypeInfo.t_type ) && info.getTypeSymbol() == null )
			return info;
		if( info.isType( TypeInfo.t_type ) && info.getTypeSymbol() instanceof IDeferredTemplateInstance ){
			IDeferredTemplateInstance deferred = (IDeferredTemplateInstance) info.getTypeSymbol();
			TypeInfo newInfo = new TypeInfo( info );
			//newInfo.setTypeSymbol( deferred.instantiate( template, argMap ) );
			template.registerDeferredInstatiation( newInfo, deferred, ITemplateSymbol.DeferredKind.TYPE_SYMBOL, argMap );
			newInfo.setTypeSymbol( deferred );
			return newInfo;
		} else if( info.isType( TypeInfo.t_type ) && 
				   info.getTypeSymbol().isType( TypeInfo.t_templateParameter ) &&
				   argMap.containsKey( info.getTypeSymbol() ) )
		{
			TypeInfo targetInfo = new TypeInfo( (TypeInfo) argMap.get( info.getTypeSymbol() ) );
			if( info.hasPtrOperators() ){
				List infoOperators = new LinkedList( info.getPtrOperators() );
				targetInfo.addPtrOperator( infoOperators );
			}
			
			if( info.checkBit( TypeInfo.isConst ) )
				targetInfo.setBit( true, TypeInfo.isConst );
			
			if( info.checkBit( TypeInfo.isVolatile ) )
				targetInfo.setBit( true, TypeInfo.isVolatile );
			
			return targetInfo;
		} else if( info.isType( TypeInfo.t_type ) && info.getTypeSymbol().isType( TypeInfo.t_function ) ){
			TypeInfo newInfo = new TypeInfo( info );
			newInfo.setTypeSymbol( info.getTypeSymbol().instantiate( template, argMap ) );
			return newInfo;
		}
		return info;
	
	}
	
	static protected void instantiateDeferredTypeInfo( TypeInfo info, ITemplateSymbol template, Map argMap ) throws ParserSymbolTableException {
		info.setTypeSymbol( info.getTypeSymbol().instantiate( template, argMap ) );
	}
	
	static protected ITemplateSymbol matchTemplatePartialSpecialization( ITemplateSymbol template, List args ) throws ParserSymbolTableException{
		if( template == null ){
			return null;
		}
		
		List specs = template.getSpecializations();
		int size = ( specs != null ) ? specs.size() : 0;
		if( size == 0 ){
			return template;
		}
		
		ISpecializedSymbol bestMatch = null;
		boolean bestMatchIsBest = true;
		Iterator iter = specs.iterator();
		ISpecializedSymbol spec = null;
		List specArgs = null;
		for( int i = size; i > 0; i-- ){
			spec = (ISpecializedSymbol) iter.next();
			specArgs = spec.getArgumentList();
			if( specArgs == null || specArgs.size() != args.size() ){
				continue;
			}
			
			Iterator iter1 = specArgs.iterator();
			Iterator iter2 = args.iterator();
			
			HashMap map = new HashMap();
			TypeInfo info1 = null, info2 = null;

			boolean match = true;
			for( int j = specArgs.size(); j > 0; j-- ){
				info1 = (TypeInfo) iter1.next();
				info2 = (TypeInfo) iter2.next();
				
				ISymbol sym1 = template.getSymbolTable().newSymbol( ParserSymbolTable.EMPTY_NAME );
				sym1.setTypeInfo( info1 );
				
				if( !deduceTemplateArgument( map, sym1, info2 ) ){
					match = false;
					break;
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
		
		//14.5.4.1 If none of the specializations is more specialized than all the other matchnig
		//specializations, then the use of the class template is ambiguous and the program is ill-formed.
		if( !bestMatchIsBest ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_Ambiguous );
		}
		
		return bestMatch;
	}
	
	static protected boolean matchTemplateParameterAndArgument( ISymbol param, TypeInfo arg ){
		if( !isValidArgument(param, arg) ){
			return false;
		}
		
		if( param.getTypeInfo().getTemplateParameterType() == TypeInfo.t_typeName ){
			return true;	
		} else if( param.getTypeInfo().getTemplateParameterType() == TypeInfo.t_template ){
			
			ISymbol symbol = arg.getTypeSymbol();
			if( !arg.isType( TypeInfo.t_type ) || symbol == null || !symbol.isType( TypeInfo.t_template ) ){
				return false;
			}
			
			IParameterizedSymbol p = (IParameterizedSymbol) param;
			IParameterizedSymbol a = (IParameterizedSymbol) symbol;
			
			List pList = p.getParameterList();
			List aList = a.getParameterList();
			
			if( pList.size() != aList.size() ){
				return false;
			}
			
			Iterator pIter = pList.iterator();
			Iterator aIter = aList.iterator();
			while( pIter.hasNext() ){
				ISymbol pParam = (ISymbol) pIter.next();
				ISymbol aParam = (ISymbol) aIter.next();
				
				if( pParam.getType() != aParam.getType() || 
						pParam.getTypeInfo().getTemplateParameterType() != aParam.getTypeInfo().getTemplateParameterType() )
				{
					return false;
				}
			}
			
			return true;
		} else {
			Cost cost = null;
			try {
				TypeInfo info = new TypeInfo( param.getTypeInfo() );
				info.setType( info.getTemplateParameterType() );
				cost = ParserSymbolTable.checkStandardConversionSequence( arg, info );
			} catch (ParserSymbolTableException e) {
			}
			
			if( cost == null || cost.rank != Cost.NO_MATCH_RANK ){
				return false;
			}			
		}
		return true;
	}

	static private boolean isValidArgument(ISymbol param, TypeInfo arg) {
		if( param.getTypeInfo().getTemplateParameterType() == TypeInfo.t_typeName ){
			//14.3.1, local type, type with no name
			if( arg.isType( TypeInfo.t_type ) && arg.getTypeSymbol() != null ){
				ISymbol symbol = arg.getTypeSymbol();
				if( symbol.getName().equals( ParserSymbolTable.EMPTY_NAME ) ){
					return false;
				} else if( hasNoLinkage( arg ) ){
					return false;
				}
			}
		} else if ( param.getTypeInfo().getTemplateParameterType() == TypeInfo.t_template ){
			
		} else {
			List ptrs = param.getPtrOperators();
			PtrOp op = ( ptrs.size() > 0 ) ? (PtrOp) ptrs.get(0) : null;
			
			//if the parameter has reference type
			if( op != null && op.getType() == PtrOp.t_reference ){
				if( arg.isType( TypeInfo.t_type )  && arg.getTypeSymbol() != null ){
					if( arg.getTypeSymbol().getName().equals( ParserSymbolTable.EMPTY_NAME ) ){
						return false;
					}
				}
				return hasExternalLinkage( arg );
			}
			
			List argPtrs = arg.getPtrOperators();
			PtrOp argOp = (argPtrs.size() > 0 ) ? (PtrOp)argPtrs.get(0) : null;
			
			//address of an object with external linkage exluding nonstatic class members
			//name of an object with external linkage excluding nonstatic class members
			if( (argOp != null && argOp.getType() == PtrOp.t_pointer ) ||
					( arg.isType( TypeInfo.t_type ) ) )
			{
				ISymbol symbol = arg.getTypeSymbol();
				if ( symbol != null && symbol.getContainingSymbol().isType( TypeInfo.t_class, TypeInfo.t_union ) ){
					if( !symbol.isType( TypeInfo.t_class, TypeInfo.t_union ) ){
						if( !symbol.getTypeInfo().checkBit( TypeInfo.isStatic ) ){
							return false;
						}
					}
				}
				
				return hasExternalLinkage( arg );
			}
			
			//integral or enumeration type
			if( op == null && ( arg.isType( TypeInfo.t_bool, TypeInfo.t_int ) || 
					arg.isType( TypeInfo.t_enumerator )           )  )
			{	
				return true;
			}
			
			//name of a non-type template parameter
			if( arg.isType( TypeInfo.t_templateParameter ) && 
					arg.getTemplateParameterType() != TypeInfo.t_typeName &&
					arg.getTemplateParameterType() != TypeInfo.t_template )
			{
				return true;
			}
			return false;
		}
		return true;	
	}
	
	static protected boolean hasExternalLinkage( TypeInfo type ){
		if( ! type.isType( TypeInfo.t_type ) )
			return false;
		
		return !hasNoLinkage( type );
	}
	
	static protected boolean hasInternalLinkage( TypeInfo type ){
		return !hasNoLinkage( type );
	}
	
	static protected boolean hasNoLinkage( TypeInfo type ){
		if( type.isType( TypeInfo.t_type ) ){
			ISymbol symbol = type.getTypeSymbol();
			if( symbol.getContainingSymbol() == null ){
				return true;	//a temporary 
			}
			
			return symbol.getContainingSymbol().isType( TypeInfo.t_function );	
		}
		
		return false;
	}
	
	/**
	 * 14.8.2.1-2 If P is a cv-qualified type, the top level cv-qualifiers of P's type are ignored for type
	 * deduction.  If P is a reference type, the type referred to by P is used for Type deduction.
	 * @param pSymbol
	 * @return
	 */	
	static private TypeInfo getParameterTypeForDeduction( ISymbol pSymbol ){
		TypeInfo p = new TypeInfo( pSymbol.getTypeInfo () );
		List pPtrs = p.getPtrOperators();
		if( pPtrs.size() > 0 ){
			PtrOp pOp = (PtrOp) pPtrs.get( 0 );
			if( pOp.getType() == PtrOp.t_reference || pOp.getType() == PtrOp.t_undef_ptr ){
				pPtrs.remove( 0 );	
			} else {
				PtrOp newOp = new PtrOp( pOp.getType(), false, false );
				pPtrs.set( 0, newOp );
			}
		} else {
			p.setBit( false, TypeInfo.isConst );
			p.setBit( false, TypeInfo.isVolatile );
		}
		

		return p;
	}
	
	/**
	 * 14.8.2.1-2
	 * if P is not a reference type
	 * - If A is an array type, the pointer type produced by the array-to-pointer conversion is used instead
	 * - If A is a function type, the pointer type produced by the function-to-pointer conversion is used instead
	 * - If A is a cv-qualified type, the top level cv-qualifiers are ignored for type deduction 
	 * @param aInfo
	 * @return
	 */
	static private TypeInfo getArgumentTypeForDeduction( TypeInfo aInfo, boolean pIsAReferenceType ) throws ParserSymbolTableException{
		
		TypeInfo a = ParserSymbolTable.getFlatTypeInfo( aInfo );
		
		if( !pIsAReferenceType ){
			List aPtrs = a.getPtrOperators();
			ISymbol aSymbol = a.getTypeSymbol();
			
			if( a.getType() == TypeInfo.t_type ){
				if( aSymbol == null ){
					throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplateArgument );
				} else if( aSymbol.isType( TypeInfo.t_function ) &&  aPtrs.size() == 0 ){
					aPtrs.add( new PtrOp( PtrOp.t_pointer ) );	
				}
			}
			if( aPtrs.size() > 0 ){
				PtrOp pOp = (PtrOp) aPtrs.get( 0 );
				
				if( pOp.getType() == PtrOp.t_array ){
					aPtrs.set( 0, new PtrOp( PtrOp.t_pointer, false, false ) );
				} else {
					aPtrs.set( 0, new PtrOp( pOp.getType(), false, false ) );
				}
			} else {
				a.setBit( false, TypeInfo.isConst );
				a.setBit( false, TypeInfo.isVolatile );
			}
		}
		
		return a;
	}
	
	static private List getSourceList( ISymbol symbol ){
		ITemplateSymbol template = null;
		
		if( symbol instanceof IDeferredTemplateInstance ){
			IDeferredTemplateInstance deferred = (IDeferredTemplateInstance) symbol;
			return deferred.getArguments();
		} 
		ISymbol instantiated = symbol.getInstantiatedSymbol();
		template = (ITemplateSymbol) instantiated.getContainingSymbol();
		
		
		if( template instanceof ISpecializedSymbol ){
			return ((ISpecializedSymbol)template).getArgumentList();
		} 
		return template.getParameterList();
	}
	
	static private List getTargetList( ISymbol symbol ){
		if( symbol instanceof IDeferredTemplateInstance ){
			IDeferredTemplateInstance deferred = (IDeferredTemplateInstance) symbol;
			return deferred.getArguments();
		} 
		ISymbol instantiated = symbol.getInstantiatedSymbol();
		if( instantiated != null ){
			ITemplateSymbol template = (ITemplateSymbol) instantiated.getContainingSymbol();
			return template.findArgumentsFor( (IContainerSymbol) symbol );
		}
		return null;
	}
	
	/**
	 * @param aSymbol
	 * @param p
	 * @param derivable
	 * @return
	 */
	private static ISymbol findMatchingBaseClass( ISymbol p, IDerivableContainerSymbol a ) {
		ISymbol aSymbol = null;
		ITemplateSymbol pTemplate = null;
		ITemplateSymbol parentTemplate = null;
		
		if( p instanceof IDeferredTemplateInstance ){
			pTemplate = ((IDeferredTemplateInstance)p).getTemplate();
		} else {
			pTemplate = (ITemplateSymbol) p.getInstantiatedSymbol().getContainingSymbol();
		}
		if( pTemplate instanceof ISpecializedSymbol ){
			pTemplate = ((ISpecializedSymbol)pTemplate).getPrimaryTemplate();
		}
		Iterator iter = a.getParents().iterator();
		while( iter.hasNext() ){
			IParentSymbol wrapper = (IParentSymbol) iter.next();
			ISymbol parent = wrapper.getParent();
			if( parent instanceof IDeferredTemplateInstance ){
				IDeferredTemplateInstance parentInstance = (IDeferredTemplateInstance) parent;
				parentTemplate = parentInstance.getTemplate();
				if( parentTemplate instanceof ISpecializedSymbol ){
					parentTemplate = ((ISpecializedSymbol)parentTemplate).getPrimaryTemplate();
				}
				if( pTemplate == parentTemplate ){
					aSymbol = parent;
					break;
				}
				//In general, we don't have enough information to proceed further down this branch
			} else {
				parentTemplate = (ITemplateSymbol) parent.getInstantiatedSymbol().getContainingSymbol();
				if( parentTemplate instanceof ISpecializedSymbol ){
					parentTemplate = ((ISpecializedSymbol)parentTemplate).getPrimaryTemplate();
				}
				if( pTemplate == parentTemplate ){
					aSymbol = parent;
					break;
				}
				aSymbol = findMatchingBaseClass( p, (IDerivableContainerSymbol) parent );
			}
			if( aSymbol != null )
				return aSymbol;
		}
		
		return aSymbol;
	}
	
	static private boolean deduceTemplateArgument( Map map, ISymbol pSymbol, TypeInfo a ) throws ParserSymbolTableException{//, Map argumentMap ){
		ISymbol symbol;
		
		boolean pIsAReferenceType = false;
		
		Iterator i = pSymbol.getPtrOperators().iterator();
		if( i.hasNext() && ((PtrOp)i.next()).getType() == TypeInfo.PtrOp.t_reference ){
			pIsAReferenceType = true;
		}
		
		TypeInfo p = getParameterTypeForDeduction( pSymbol );
		
		a = getArgumentTypeForDeduction( a, pIsAReferenceType );
		
		if( p.isType( TypeInfo.t_type ) ){
			symbol = p.getTypeSymbol();
			ISymbol aSymbol = a.getTypeSymbol();
			if( symbol == null || ( a.isType( TypeInfo.t_type) && aSymbol == null ) || a.isType( TypeInfo.t_undef ))
				throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTypeInfo );
			if( symbol instanceof IDeferredTemplateInstance || symbol.isTemplateInstance() ){
				return deduceFromTemplateTemplateArguments(map, symbol, aSymbol);	
			} 
			if( symbol.isType( TypeInfo.t_templateParameter ) ){
				if( symbol.getTypeInfo().getTemplateParameterType() == TypeInfo.t_typeName ){
					//a = getFlatTypeInfo( a );
					List aPtrs = a.getPtrOperators();
					List pPtrs = p.getPtrOperators();
					
					if( pPtrs != null && pPtrs.size() > 0){
						if( aPtrs == null ){
							return false;
						}
						
						Iterator pIter = pPtrs.iterator();
						ListIterator aIter = aPtrs.listIterator();
						PtrOp pOp = null;
						PtrOp aOp = null;
						while( pIter.hasNext() ){
							pOp = (PtrOp) pIter.next();
							if( !aIter.hasNext() ){
								return false;
							} 
							aOp = (PtrOp) aIter.next();
							if( pOp.getType() == aOp.getType() ){
								if( !pOp.equals( aOp ) )
									return false;
								aIter.remove();
							} else {
								return false;
							}
						} 
					}
					//cvlist T
					if( p.checkBit( TypeInfo.isConst ) ){
						if( !a.checkBit( TypeInfo.isConst ) )
							return false;
						a.setBit( false, TypeInfo.isConst);
					}
					if( p.checkBit( TypeInfo.isVolatile ) ){
						if( !a.checkBit( TypeInfo.isVolatile ) )
							return false;
						a.setBit( false, TypeInfo.isVolatile);
					}
					
					//T
					return deduceArgument( map, symbol, a );
						
				} else if ( symbol.getTypeInfo().getTemplateParameterType() == TypeInfo.t_template ){
					
				} else {
					//non-type parameter
					if( symbol.getTypeInfo().getTemplateParameterType() == a.getType() ){
						return deduceArgument( map, symbol, a );
					} 
					return false;
				}
			} 
			//T (*) ( ), T ( T::* ) ( T ), & variations
			else if( symbol.isType( TypeInfo.t_function ) ){
				if( !(aSymbol instanceof IParameterizedSymbol)|| 
						!aSymbol.isType( TypeInfo.t_function ) )
				{
					return false;
				}
				
				IParameterizedSymbol pFunction = (IParameterizedSymbol)symbol;
				IParameterizedSymbol aFunction = (IParameterizedSymbol)aSymbol;

				if( !deduceTemplateArgument( map, pFunction.getReturnType(), aFunction.getReturnType().getTypeInfo() ) ){
					return false;
				}
				
				List pPtrs = p.getPtrOperators();
				if( pPtrs.size() != 0 ){
					PtrOp op = (PtrOp) pPtrs.iterator().next();
					if( op.getType() == PtrOp.t_memberPointer ){
						TypeInfo info = new TypeInfo( TypeInfo.t_type, 0, aFunction.getContainingSymbol() );
						if( !deduceTemplateArgument( map, op.getMemberOf(), info ) ){
							return false;
						}
					}
				}

				List pParams = pFunction.getParameterList();
				List aParams = aFunction.getParameterList();
				if( pParams.size() != aParams.size() ){
					return false;
				} 
				Iterator pIter = pParams.iterator();
				Iterator aIter = aParams.iterator();
				while( pIter.hasNext() ){
					TypeInfo info = ((ISymbol)aIter.next()).getTypeInfo();
					if( !deduceTemplateArgument( map, (ISymbol) pIter.next(), info ) ){
						return false;
					}
				}
				return true;
			} 
			 
		} 
		if( p.isType( TypeInfo.t_templateParameter ) ){
			return deduceArgument( map, pSymbol, a );
		}
		if( p.getType() == a.getType() ){
			if( p.getDefault() != null ){
				return ( p.getDefault().equals( a.getDefault() ) );
			}
			return true;
		}
		
		return false;
	}
	
	/**
	 * @param map
	 * @param pSymbol
	 * @param transformationMap
	 * @param symbol
	 * @param aSymbol
	 * @return
	 */
	private static boolean deduceFromTemplateTemplateArguments(Map map, ISymbol pSymbol, ISymbol aSymbol) {
		//template-name<T> or template-name<i>, where template-name is a class template
		ITemplateSymbol p = ( pSymbol instanceof IDeferredTemplateInstance ) ? 
							(ITemplateSymbol) ((IDeferredTemplateInstance) pSymbol ).getTemplate() :
							(ITemplateSymbol) pSymbol.getInstantiatedSymbol().getContainingSymbol();	
		
		if( p instanceof ISpecializedSymbol )
			p = ((ISpecializedSymbol)p).getPrimaryTemplate();
		
		ISymbol a = ( aSymbol.isTemplateInstance() ) ? aSymbol.getInstantiatedSymbol().getContainingSymbol() :
					                                   aSymbol.getContainingSymbol();
			
		if( a instanceof ISpecializedSymbol )
			a = ((ISpecializedSymbol)a).getPrimaryTemplate();
		
		if( p != a ){
			if( aSymbol instanceof IDerivableContainerSymbol ){
				aSymbol = findMatchingBaseClass( pSymbol, (IDerivableContainerSymbol) aSymbol );
			} else {
				aSymbol = null;
			}
			if( aSymbol == null ) {
				return false;
			}
		}
	
		List pList = getSourceList( pSymbol );
		List aList = getTargetList( aSymbol );
		
		//TODO: default args?
		if( pList == null || aList == null || pList.size() != aList.size()){
			return false;				
		}
		Iterator pIter = pList.iterator();
		Iterator aIter = aList.iterator();
		while( pIter.hasNext() ){
			Object obj = pIter.next();
			ISymbol sym = null;
			if( obj instanceof ISymbol ){
				sym = (ISymbol) obj;
			} else {
				sym = pSymbol.getSymbolTable().newSymbol( ParserSymbolTable.EMPTY_NAME );
				sym.setTypeInfo( (TypeInfo) obj );
			}
			
			TypeInfo arg = transformTypeInfo( aIter.next(), null );
			
			try {
				if( !deduceTemplateArgument( map, sym, arg ) ){
					return false;
				}
			} catch (ParserSymbolTableException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param template
	 * @param argList - the arguments passed in the function call
	 * @return
	 * 
	 * A type that is specified in terms of template parameters (P) is compared with an actual 
	 * type (A), and an attempt is made to find template argument vaules that will make P, 
	 * after substitution of the deduced values, compatible with A.
	 */
	static private Map deduceTemplateArgumentsUsingParameterList( ITemplateSymbol template, IParameterizedSymbol function ){

		List aList = function.getParameterList();
		LinkedList args = new LinkedList();
		 
		Iterator iter = aList.iterator();
		while( iter.hasNext() ){
			ISymbol symbol = (ISymbol) iter.next();
			args.add( symbol.getTypeInfo() );
		}
		
		return deduceTemplateArguments( template, args );
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
	static private Map deduceTemplateArguments( ITemplateSymbol template, List arguments ){
		if( template.getContainedSymbols() == ParserSymbolTable.EMPTY_MAP || template.getContainedSymbols().size() != 1 ){
			return null;
		}

		ISymbol templateSymbol = template.getTemplatedSymbol();
		if( !templateSymbol.isType( TypeInfo.t_function ) ){
			return null;
		}

		IParameterizedSymbol templateFunction = (IParameterizedSymbol) templateSymbol;
		
		List pList = templateFunction.getParameterList();
//		TODO: ellipses?
		if( pList == null || arguments == null || pList.size() != arguments.size() ){
			return null;
		}
		
		HashMap map = new HashMap();
		
		Iterator pIter = pList.iterator();
		Iterator aIter = arguments.iterator();
		
		while( pIter.hasNext() ){
			try {
				if( !deduceTemplateArgument( map, (ISymbol) pIter.next(), (TypeInfo) aIter.next() ) ){
					return null;
				}
			} catch (ParserSymbolTableException e) {
				return null;
			}
		}
		
		return map;			
	}

	static private boolean deduceArgument( Map map, ISymbol p, TypeInfo a ){
		
		a = ParserSymbolTable.getFlatTypeInfo( a );
		
		if( map.containsKey( p ) ){
			TypeInfo current = (TypeInfo)map.get( p );
			return current.equals( a );
		} 
		map.put( p, a );
		return true;
	}
	/**
	 * Compare spec1 to spec2.  Return > 0 if spec1 is more specialized, < 0 if spec2
	 * is more specialized, = 0 otherwise.
	 * @param spec1
	 * @param spec2
	 * @return
	 */
	static private int orderSpecializations( ISpecializedSymbol spec1, ISpecializedSymbol spec2 ) throws ParserSymbolTableException{
		if( spec1 == null ){
			return -1;	
		}
		
		ISymbol decl = spec1.getTemplatedSymbol();
		
		//to order class template specializations, we need to transform them into function templates
		ITemplateSymbol template1 = spec1;
		ITemplateSymbol template2 = spec2;
		
		if( decl.isType( TypeInfo.t_class, TypeInfo.t_union ) ) {
			template1 = classTemplateSpecializationToFunctionTemplate( spec1 );
			template2 = classTemplateSpecializationToFunctionTemplate( spec2 );	
		}
		
		return orderTemplateFunctions( template1, template2);
	}
	
	/**
	 * Compare spec1 to spec2.  Return > 0 if spec1 is more specialized, < 0 if spec2
	 * is more specialized, = 0 otherwise.
	 * 
	 * Both spec1 and spec2 are expected to be template functions
	 * 
	 */
	static protected int orderTemplateFunctions( ITemplateSymbol spec1, ITemplateSymbol spec2 ) throws ParserSymbolTableException{
		//Using the transformed parameter list, perform argument deduction against the other
		//function template
		Map map = createMapForFunctionTemplateOrdering( spec1 );
		
		IContainerSymbol templatedSymbol = spec1.getTemplatedSymbol();
		if( !( templatedSymbol instanceof IParameterizedSymbol ) )
			throw new ParserSymbolTableError( ParserSymbolTableError.r_InternalError );
		
		IParameterizedSymbol function = (IParameterizedSymbol)templatedSymbol;
		function = (IParameterizedSymbol) function.instantiate( spec1, map );
		((TemplateSymbol)spec1).processDeferredInstantiations();
		
		Map m1 = deduceTemplateArgumentsUsingParameterList( spec2, function);
		
		map = createMapForFunctionTemplateOrdering( spec2 );
		
		templatedSymbol = spec2.getTemplatedSymbol();
		if( !( templatedSymbol instanceof IParameterizedSymbol ) )
			throw new ParserSymbolTableError( ParserSymbolTableError.r_InternalError );
		
		function = (IParameterizedSymbol)templatedSymbol;
		function = (IParameterizedSymbol) function.instantiate( spec2, map );
		((TemplateSymbol)spec2).processDeferredInstantiations();
		
		Map m2 = deduceTemplateArgumentsUsingParameterList( spec1, function );
		
		//The transformed  template is at least as specialized as the other iff the deduction
		//succeeds and the deduced parameter types are an exact match
		//A template is more specialized than another iff it is at least as specialized as the
		//other template and that template is not at least as specialized as the first.
		boolean d1 = ( m1 != null );
		boolean d2 = ( m2 != null );
		
		if( d1 && d2 || !d1 && !d2 )
			return 0;
		else if( d1 && !d2 )
			return 1;
		else 
			return -1;
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

	static private Map createMapForFunctionTemplateOrdering( ITemplateSymbol template ){
		HashMap map = new HashMap();
		TypeInfo val = null;
		List paramList = template.getParameterList();
		for( Iterator iterator = paramList.iterator(); iterator.hasNext(); ) {
			ISymbol param = (ISymbol) iterator.next();
			//template type parameter
			if( param.getTypeInfo().getTemplateParameterType() == TypeInfo.t_typeName ){
				val = new TypeInfo( TypeInfo.t_type, 0, template.getSymbolTable().newSymbol( "", TypeInfo.t_class ) ); //$NON-NLS-1$
			} 
			//template parameter
			else if ( param.getTypeInfo().getTemplateParameterType() == TypeInfo.t_template ) {
				
			}
			//non type parameter 
			else {
				val = new TypeInfo( param.getTypeInfo().getTemplateParameterType(), 0, null );
			}
			map.put( param, val );
		}
		return map;
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
	static private ITemplateSymbol classTemplateSpecializationToFunctionTemplate( ISpecializedSymbol specialization ) throws ParserSymbolTableException{
		ISpecializedSymbol transformed = (ISpecializedSymbol) specialization.clone();
		transformed.getArgumentList().clear();
		transformed.getContainedSymbols().clear();
		//TODO clean up this
		((ContainerSymbol)transformed).getContents().clear();
		
		IParameterizedSymbol function = specialization.getSymbolTable().newParameterizedSymbol( transformed.getName(), TypeInfo.t_function );
		try{
			transformed.addSymbol( function );
		} catch ( ParserSymbolTableException e ){
			//we shouldn't get this because there aren't any other symbols in the template
		}
		ISymbol param = specialization.getSymbolTable().newSymbol( "", TypeInfo.t_type ); //$NON-NLS-1$
		
		param.setTypeSymbol( specialization.instantiate( specialization.getArgumentList() ) );
				
		function.addParameter( param );
		
		return transformed;
	}
	
	static private TypeInfo transformTypeInfo( Object obj, Map argumentMap ){
		TypeInfo info = null;
		if( obj instanceof ISymbol ){
			info = new TypeInfo( TypeInfo.t_type, 0, (ISymbol) obj );
		} else {
			info = (TypeInfo) obj;
		}
		
		if( argumentMap == null )
			return info;
		
		if( info.isType( TypeInfo.t_type ) && 
				info.getTypeSymbol().isType( TypeInfo.t_templateParameter ) &&
				argumentMap.containsKey( info.getTypeSymbol() ) )
		{
			TypeInfo newType = new TypeInfo( (TypeInfo) argumentMap.get( info.getTypeSymbol() ) );
			if( info.hasPtrOperators() )
				newType.addPtrOperator( info.getPtrOperators() );
			
			return newType;
		}
		
		return info;
	}
	
	static protected List selectTemplateFunctions( Set templates, List functionArguments, List templateArguments ) throws ParserSymbolTableException{
		if( templates == null || templates.size() == 0 )
			return null;
		
		List instances = null;
		
		Iterator iter = templates.iterator();
		
		outer: while( iter.hasNext() ){
			IParameterizedSymbol fn = (IParameterizedSymbol) iter.next();
			ITemplateSymbol template = (ITemplateSymbol) fn.getContainingSymbol();
			
			Map map = deduceTemplateArguments( template, functionArguments );
			
			if( map == null )
				continue;
			
			Iterator paramIter = template.getParameterList().iterator();
			Iterator argsIter = (templateArguments != null ) ? templateArguments.iterator() : null;
			List instanceArgs = new LinkedList();
			while( paramIter.hasNext() ){
				ISymbol param = (ISymbol) paramIter.next();
				TypeInfo arg = (TypeInfo) (( argsIter != null && argsIter.hasNext() )? argsIter.next() : null);
				TypeInfo mapped = (TypeInfo) map.get( param );
				
				if( arg != null && mapped != null )
					if( arg.equals( mapped ) )
						instanceArgs.add( arg );
					else
						continue outer;
				else if( arg == null && mapped == null )
					continue outer;
				else 
					instanceArgs.add( (arg != null) ? arg : mapped );
			}
			
			IContainerSymbol instance = (IContainerSymbol) template.instantiate( instanceArgs );
			
			if( instance != null ){
				if( instances == null )
					instances = new LinkedList();
				instances.add( instance );		
			}
		}
		
		return instances;
	}
	
	/**
	 * @param look
	 * @param parameters
	 * @param arguments
	 * @throws ParserSymbolTableException
	 */
	static protected ITemplateSymbol selectTemplateOrSpecialization( ITemplateSymbol template, List parameters, List arguments ) throws ParserSymbolTableException {
		if( template != null  ){
			//primary definition or specialization?
			boolean forPrimary = true;
			
			if( parameters.size() == 0 ){
				forPrimary = false;
			} else if( arguments != null ){
				if( arguments.size() != parameters.size() ){
					forPrimary = false;
				} else if( !parameters.isEmpty() ){
					Iterator pIter = parameters.iterator();
					Iterator aIter = arguments.iterator();
					while( pIter.hasNext() ){
						if( pIter.next() != ((TypeInfo) aIter.next()).getTypeSymbol() ){
							forPrimary = false;
							break;
						}
					}
				}
			}
			
			ITemplateSymbol primary = template;
			
			if( forPrimary ){
				//make sure parameters match up with found template
				if( checkTemplateParameterListsAreEquivalent( primary.getParameterList(), parameters ) ){
					return template;
				} 
				throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplateParameter );
			}
			
			//specialization 
			if( parameters.isEmpty() ){
				return primary;
			}
			
			//partial specialization
			ISpecializedSymbol spec = findPartialSpecialization( template, parameters, arguments );
			
			if( spec != null )
				return spec;
			
			//TODO			
			throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplate );	
			
		}
		return null;
	}
	
	static private boolean checkTemplateParameterListsAreEquivalent( List p1, List p2 ){
		if( p1.size() != p2.size() ){
			return false;
		}
		
		Iterator iter1 = p1.iterator();
		Iterator iter2 = p2.iterator();
		while( iter1.hasNext() ){
			ISymbol param1 = (ISymbol) iter1.next();
			ISymbol param2 = (ISymbol) iter2.next();
			if( param1.getTypeInfo().getTemplateParameterType() != param2.getTypeInfo().getTemplateParameterType() ){
				return false;
			}
		}
		return true;
	}
	
	static private boolean checkTemplateArgumentListsAreEquivalent( List p1, List p2, List a1, List a2 ){
		if( a1.size() !=  a2.size() || p1.size() != p2.size() ){
			return false;
		}
		
		Map m [] = { new HashMap(), new HashMap() };
		
		for( List list = p1; list != null; list = p2 ){
			Iterator i = list.iterator();
			int index = 0;
			while( i.hasNext() ){
				m[ ( list == p2 )? 1 : 0 ].put( i.next(), new Integer( index++ ) );
			}
			
			if( list == p2 ){
				break;
			}
		}
		
		Iterator i1 = a1.iterator();
		Iterator i2 = a2.iterator();
		while( i1.hasNext() ){
			TypeInfo t1 = (TypeInfo) i1.next();
			TypeInfo t2 = (TypeInfo) i2.next();
			
			if( t1.equals( t2 ) ){
				continue;
			} else if( t1.isType( TypeInfo.t_type ) && t2.isType( TypeInfo.t_type ) ) {
				ISymbol s1 = t1.getTypeSymbol(), s2 = t2.getTypeSymbol();
				if( m[0].containsKey( s1 ) && m[1].containsKey( s2 ) && m[0].get( s1 ).equals( m[1].get( s2 ) ) )
					continue;
			} 
			
			return false;
		}
		return true;
	}
	
	static private ISpecializedSymbol findPartialSpecialization( ITemplateSymbol template, List parameters, List arguments ){
		
		Iterator iter = template.getSpecializations().iterator();
		ISpecializedSymbol spec = null;
		while( iter.hasNext() ){
			spec = (ISpecializedSymbol) iter.next();
			
			if( ! checkTemplateParameterListsAreEquivalent( spec.getParameterList(), parameters ) ){
				continue;
			}
			
			if( checkTemplateArgumentListsAreEquivalent( spec.getParameterList(), parameters, 
			                                             spec.getArgumentList(),  arguments  ) )
			{
				return spec;
			}
		}
		return null;
	}
	
	static protected ISymbol translateParameterForDefinition ( ISymbol templatedSymbol, ISymbol param, Map defnMap ){
		if( defnMap == ParserSymbolTable.EMPTY_MAP ){
			return param;
		}
		
		ISymbol mappedParam = param;
		while( mappedParam.isTemplateInstance() ){
			mappedParam = mappedParam.getInstantiatedSymbol();
		}
			
		if( defnMap.containsKey( templatedSymbol ) ){
			Map map = (Map) defnMap.get( templatedSymbol );
			
			Iterator i = map.keySet().iterator();
			while( i.hasNext() ){
				ISymbol key = (ISymbol) i.next();
				if( map.get( key ) == mappedParam ){
					return key;
				}
			}
		}
		
		return param;
	}

	/**
	 * 14.6.1-1 (2) Within the scope of a class template (class template specialization or partial specialization), when 
	 * the name of the template is neither qualified nor followed by <. it is equivalent to the name of the template 
	 * followed by the template-parameters (template-arguments) enclosed in <>
	 * 
	 * @param symbol
	 * @return
	 */
	static protected ISymbol instantiateWithinTemplateScope( IContainerSymbol container, ITemplateSymbol symbol )
	{
		if( symbol.getTemplatedSymbol().isType( TypeInfo.t_function ) ){
			return symbol;
		}
		
		IDeferredTemplateInstance instance = null;
		
		IContainerSymbol containing = container.getContainingSymbol();
		boolean instantiate = false;
		while( containing != null ){
			if( containing == symbol ){
				instantiate = true;
				break;
			}
			
			containing = containing.getContainingSymbol();
			if( containing != null && !containing.isTemplateMember() || !containing.isType( TypeInfo.t_template ) ){
				containing = null;
			}
		}
		
		if( instantiate ){
			if( symbol instanceof ISpecializedSymbol ){
				ISpecializedSymbol spec = (ISpecializedSymbol) symbol;
				instance = spec.deferredInstance( spec.getArgumentList() );
			} else {
				ITemplateSymbol template = symbol;
				Iterator iter = template.getParameterList().iterator();
				List args = new LinkedList();
				while( iter.hasNext() ){
					args.add( new TypeInfo( TypeInfo.t_type, 0, (ISymbol) iter.next() ) );
				}
				
				instance = template.deferredInstance( args );
			}
		}
		
		return ( instance != null ) ? instance : (ISymbol) symbol;
	}
	
	static protected boolean alreadyHasTemplateParameter( IContainerSymbol container, String name ){
		while( container != null ){
			if( container instanceof ITemplateSymbol ){
				ITemplateSymbol template = (ITemplateSymbol) container;
				if( template.getParameterMap().containsKey( name ) ){
					return true;
				}
			}
			container = container.getContainingSymbol();
		}
		return false;
	}
	
	static protected boolean canAddTemplate( IContainerSymbol containing, ITemplateSymbol template ){
		//14-2 A template-declaration can appear only as a namespace scope or class scope declaration
		if( !containing.isType( TypeInfo.t_namespace ) && !containing.isType( TypeInfo.t_class, TypeInfo.t_union ) ){
			return false;
		}	
		
		//14.5.2-3  A member function template shall not be virtual
		if( containing.isTemplateMember() && containing.getContainingSymbol().isType( TypeInfo.t_template ) ){
			ISymbol symbol = template.getTemplatedSymbol();
			if( symbol != null && symbol.isType( TypeInfo.t_function ) && symbol.getTypeInfo().checkBit( TypeInfo.isVirtual ) ){
				return false;
			}
		}
		
		return true;
	}
	
	static protected List verifyExplicitArguments( ITemplateSymbol template, List arguments, ISymbol symbol ) throws ParserSymbolTableException{
		List actualArgs = new LinkedList();
		
		Iterator params = template.getParameterList().iterator();
		Iterator args   = arguments.iterator();
		
		while( params.hasNext() ){
			ISymbol param = (ISymbol) params.next();
			if( args.hasNext() ){
				TypeInfo arg = (TypeInfo) args.next();
				if( matchTemplateParameterAndArgument( param, arg ) ){
					actualArgs.add( arg );
				} else {
					throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplateArgument );
				}
			} else {
				//14.7.3-11 a trailing template-argument can be left unspecified in the template-id naming an explicit
				//function template specialization provided it can be deduced from the function argument type
				if( template.getTemplatedSymbol() instanceof IParameterizedSymbol &&
					symbol instanceof IParameterizedSymbol && template.getTemplatedSymbol().getName().equals( symbol.getName() ) )
				{
					Map map = deduceTemplateArgumentsUsingParameterList( template, (IParameterizedSymbol) symbol ); 
					if( map != null && map.containsKey( param ) ){
						actualArgs.add( map.get( param ) );
					} else {
						throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplateArgument );
					}
				}
			}
		}
		return actualArgs;
	}
	
	static protected ITemplateSymbol resolveTemplateFunctions( Set functions, List args, ISymbol symbol ) throws ParserSymbolTableException{
		ITemplateSymbol template = null;
		
		Iterator iter = functions.iterator();
		
		outer: while( iter.hasNext() ){
			IParameterizedSymbol fn = (IParameterizedSymbol) iter.next();
			ITemplateSymbol tmpl = (ITemplateSymbol) fn.getContainingSymbol();
			
			Map map = deduceTemplateArgumentsUsingParameterList( tmpl, (IParameterizedSymbol) symbol );
			
			if( map == null )
				continue;			 
			Iterator pIter = tmpl.getParameterList().iterator();
			Iterator aIter = args.iterator();
			while( pIter.hasNext() && aIter.hasNext() ){
				ISymbol param = (ISymbol) pIter.next();
				TypeInfo arg = (TypeInfo) aIter.next();
				if( map.containsKey( param ) ) {
					if( !map.get( param ).equals( arg )){
						continue outer;
					}
				} else if( !matchTemplateParameterAndArgument( param, arg )){
					continue outer;
				}
			}
			//made it this far, its a match
			if( template != null ){
				throw new ParserSymbolTableException(ParserSymbolTableException.r_Ambiguous );
			} 
			template = tmpl;
		}
		
		return template;
	}
	
	static protected List resolveTemplateFunctionArguments( List args, ITemplateSymbol template, IParameterizedSymbol fn )
	{
		List resultList = new LinkedList();
		
		List params = template.getParameterList();
		Map map = null;
		
		Iterator pIter = params.iterator();
		Iterator aIter = ( args != null ) ? args.iterator() : null;
		while( pIter.hasNext() ){
			ISymbol param = (ISymbol) pIter.next();
			TypeInfo arg = null;
			if( aIter != null && aIter.hasNext() ){
				arg = (TypeInfo) aIter.next();
			} else {
				if( map == null ){
					map = deduceTemplateArgumentsUsingParameterList( template, fn );
					if(map == null )
						return null;
				}
				if( map.containsKey( param ) ){
					arg = (TypeInfo) map.get( param );
				}
			}
			
			if( arg == null || !matchTemplateParameterAndArgument( param, arg ) )
				return null;
			
			resultList.add( arg );
		}
		
		return resultList;
	}
	
	static protected ISymbol checkForTemplateExplicitSpecialization( ITemplateSymbol template, ISymbol symbol, List arguments ){
		if( !template.getExplicitSpecializations().isEmpty() ){
			//TODO: could optimize this if we had a TypeInfo.hashCode()
			Iterator iter = template.getExplicitSpecializations().keySet().iterator();
			List args = null;
			while( iter.hasNext() ){
				args = (List) iter.next();
				
				if( args.equals( arguments ) ){
					Map explicitMap = (Map) template.getExplicitSpecializations().get( args );
					if( explicitMap.containsKey( symbol ) ){
						return (ISymbol) explicitMap.get( symbol );
					}
				}
			}
		}
		
		return null;
	}
	
	static protected boolean templateParametersAreEquivalent( ISymbol p1, ISymbol p2 ){
		if( !p1.isType( TypeInfo.t_templateParameter ) || !p2.isType( TypeInfo.t_templateParameter ) ||
			 p1.getTypeInfo().getTemplateParameterType() != p2.getTypeInfo().getTemplateParameterType() )
		{
			return false;
		}
		
		ITemplateSymbol t1 = getContainingTemplate( p1 );
		ITemplateSymbol t2 = getContainingTemplate( p2 );
		
		if( p1.getTypeInfo().getTemplateParameterType() == TypeInfo.t_typeName )
		{
			List l1 = t1.getParameterList(), l2 = t2.getParameterList();
			return ( l1 != null && l2 != null &&  l1.indexOf( p1 ) == l2.indexOf( p2 ) ); 
		} else if( p1.getTypeInfo().getTemplateParameterType() == TypeInfo.t_template ){
			ITemplateSymbol pt1 = (ITemplateSymbol)p1.getTypeSymbol();
			ITemplateSymbol pt2 = (ITemplateSymbol)p2.getTypeSymbol();
			return checkTemplateParameterListsAreEquivalent( pt1.getParameterList(), pt2.getParameterList() );
		} else {
			return p1.getTypeInfo().equals( p2.getTypeInfo() );
		}
	}
	
	static protected ITemplateSymbol getContainingTemplate( ISymbol symbol ){
		if( ! symbol.isTemplateMember() ){
			return null;
		}
		
		while( !( symbol.getContainingSymbol() instanceof ITemplateSymbol ) ){
			symbol = symbol.getContainingSymbol();
		}
		return (ITemplateSymbol) symbol.getContainingSymbol();
	}

	/**
	 * @param instance
	 * @param instance2
	 * @return
	 */
	protected static boolean deferedInstancesAreEquivalent(IDeferredTemplateInstance instance, IDeferredTemplateInstance instance2) {
		if( instance.getTemplate() != instance2.getTemplate() )
			return false;
		
		List args = instance.getArguments();
		List args2 = instance2.getArguments();
		
		if( args.size() != args2.size() )
			return false;
		
		Iterator iter1 = args.iterator(), iter2 = args2.iterator();
		while( iter1.hasNext() ){
			TypeInfo info1 = (TypeInfo) iter1.next();
			TypeInfo info2 = (TypeInfo) iter2.next();
			
			if( ! info1.equals( info2 ) )
				return false;
		}
		return true;
	}
}
