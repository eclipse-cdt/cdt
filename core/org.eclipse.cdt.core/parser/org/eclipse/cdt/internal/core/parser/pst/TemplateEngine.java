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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.parser.pst.IDerivableContainerSymbol.IParentSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable.Cost;

/**
 * @author aniefer
 */
public final class TemplateEngine {

	static protected ITypeInfo instantiateTypeInfo( ITypeInfo info, ITemplateSymbol template, ObjectMap  argMap ) throws ParserSymbolTableException{
		if( argMap == null )
			return info;
		ISymbol typeSymbol = info.getTypeSymbol();
		if( info.isType( ITypeInfo.t_type ) ) {
			if ( info.getTypeSymbol() == null ) {
				return info;
			}
			if( typeSymbol instanceof IDeferredTemplateInstance ){
				IDeferredTemplateInstance deferred = (IDeferredTemplateInstance) info.getTypeSymbol();
				ITypeInfo newInfo = TypeInfoProvider.newTypeInfo( info );
				//newInfo.setTypeSymbol( deferred.instantiate( template, argMap ) );
				template.registerDeferredInstatiation( newInfo, deferred, ITemplateSymbol.DeferredKind.TYPE_SYMBOL, argMap );
				newInfo.setTypeSymbol( deferred );
				return newInfo;
			} else if ( typeSymbol instanceof UndefinedTemplateSymbol &&
						( typeSymbol.isType( ITypeInfo.t_template ) ||
						  typeSymbol.isType( ITypeInfo.t_undef ) ) ) {
					ITemplateSymbol deferred = (ITemplateSymbol) info.getTypeSymbol();
					ITypeInfo newInfo = TypeInfoProvider.newTypeInfo( info );
					template.registerDeferredInstatiation( newInfo, deferred, ITemplateSymbol.DeferredKind.TYPE_SYMBOL, argMap );
					newInfo.setTypeSymbol( deferred );
					return newInfo;
			} else if( typeSymbol.isType( ITypeInfo.t_templateParameter ) &&
					   argMap.containsKey( info.getTypeSymbol() ) )
			{
				ITypeInfo targetInfo = TypeInfoProvider.newTypeInfo( (ITypeInfo) argMap.get( info.getTypeSymbol() ) );
				if( info.hasPtrOperators() ){
					targetInfo.addPtrOperator( info.getPtrOperators() );
				}
				
				if( info.checkBit( ITypeInfo.isConst ) )
					targetInfo.setBit( true, ITypeInfo.isConst );
				
				if( info.checkBit( ITypeInfo.isVolatile ) )
					targetInfo.setBit( true, ITypeInfo.isVolatile );
				
				return targetInfo;
			} else if( typeSymbol.isType( ITypeInfo.t_function ) ){
				ITypeInfo newInfo = TypeInfoProvider.newTypeInfo( info );
				newInfo.setTypeSymbol( info.getTypeSymbol().instantiate( template, argMap ) );
				return newInfo;
			}
		}
		return info;
	
	}
	
	static protected void instantiateDeferredTypeInfo( ITypeInfo info, ITemplateSymbol template, ObjectMap argMap ) throws ParserSymbolTableException {
		info.setTypeSymbol( info.getTypeSymbol().instantiate( template, argMap ) );
	}
	
	/**
	 * @param info
	 * @param symbol
	 * @param map
	 */
	public static void discardDeferredTypeInfo(ITypeInfo info, ITemplateSymbol template, ObjectMap map) {
		ISymbol instance = info.getTypeSymbol();
		if( !(instance instanceof IDeferredTemplateInstance ) )
			template.removeInstantiation( (IContainerSymbol) instance );
		info.setTypeSymbol( null );
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
		ISpecializedSymbol spec = null;
		List specArgs = null;
		for( int i = 0; i < size; i++ ){
			spec = (ISpecializedSymbol) specs.get(i);
			specArgs = spec.getArgumentList();
			if( specArgs == null || specArgs.size() != args.size() ){
				continue;
			}
			
			int specArgsSize = specArgs.size();			
			ObjectMap map = new ObjectMap(specArgsSize);
			ITypeInfo info1 = null, info2 = null;

			boolean match = true;
			for( int j = 0; j < specArgsSize; j++ ){
				info1 = (ITypeInfo) specArgs.get(j);
				info2 = (ITypeInfo) args.get(j);
				
				ISymbol sym1 = template.getSymbolTable().newSymbol( ParserSymbolTable.EMPTY_NAME_ARRAY );
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
	
	static protected boolean matchTemplateParameterAndArgument( ISymbol param, ITypeInfo arg ){
		if( !isValidArgument(param, arg) ){
			return false;
		}
		
		if( param.getTypeInfo().getTemplateParameterType() == ITypeInfo.t_typeName ){
			return true;	
		} else if( param.getTypeInfo().getTemplateParameterType() == ITypeInfo.t_template ){
			
			ISymbol symbol = arg.getTypeSymbol();
			if( !arg.isType( ITypeInfo.t_type ) || symbol == null || !symbol.isType( ITypeInfo.t_template ) ){
				return false;
			}
			
			IParameterizedSymbol p = (IParameterizedSymbol) param;
			IParameterizedSymbol a = (IParameterizedSymbol) symbol;
			
			List pList = p.getParameterList();
			List aList = a.getParameterList();
			int size = pList.size();
			if( aList.size() != size){
				return false;
			}
			
			for( int i = 0; i < size; i++){
				ISymbol pParam = (ISymbol) pList.get(i);
				ISymbol aParam = (ISymbol) aList.get(i);
				
				if( pParam.getType() != aParam.getType() || 
						pParam.getTypeInfo().getTemplateParameterType() != aParam.getTypeInfo().getTemplateParameterType() )
				{
					return false;
				}
			}
			
			return true;
		} else {
			Cost cost = null;
		    TypeInfoProvider provider = param.getSymbolTable().getTypeInfoProvider();
			try{
				ITypeInfo info = provider.getTypeInfo( param.getTypeInfo().getTemplateParameterType() );
				try {
					info.copy( param.getTypeInfo() );
					info.setType( info.getTemplateParameterType() );
					cost = param.getSymbolTable().checkStandardConversionSequence( arg, info );
					provider.returnTypeInfo( info );
				} catch (ParserSymbolTableException e) {
				    //nothing
				} finally {
				    provider.returnTypeInfo( info );
				}
				
				if( cost == null || cost.rank != Cost.NO_MATCH_RANK ){
					return false;
				}
			} finally{
				if( cost != null )
					cost.release( provider );
			}
		}
		return true;
	}

	static private boolean isValidArgument(ISymbol param, ITypeInfo arg) {
		if( param.getTypeInfo().getTemplateParameterType() == ITypeInfo.t_typeName ){
			//14.3.1, local type, type with no name
			if( arg.isType( ITypeInfo.t_type ) && arg.getTypeSymbol() != null ){
				ISymbol symbol = arg.getTypeSymbol();
				if( CharArrayUtils.equals( symbol.getName(), ParserSymbolTable.EMPTY_NAME_ARRAY ) ){
					return false;
				} else if( hasNoLinkage( arg ) ){
					return false;
				}
			}
		} else if ( param.getTypeInfo().getTemplateParameterType() == ITypeInfo.t_template ){
			//TODO
		} else {
			List ptrs = param.getPtrOperators();
			ITypeInfo.PtrOp op = ( ptrs.size() > 0 ) ? (ITypeInfo.PtrOp) ptrs.get(0) : null;
			
			//if the parameter has reference type
			if( op != null && op.getType() == ITypeInfo.PtrOp.t_reference ){
				if( arg.isType( ITypeInfo.t_type )  && arg.getTypeSymbol() != null ){
				    if( CharArrayUtils.equals( arg.getTypeSymbol().getName(), ParserSymbolTable.EMPTY_NAME_ARRAY )){
						return false;
					}
				}
				return hasExternalLinkage( arg );
			}
			
			List argPtrs = arg.getPtrOperators();
			ITypeInfo.PtrOp argOp = (argPtrs.size() > 0 ) ? (ITypeInfo.PtrOp)argPtrs.get(0) : null;
			
			//address of an object with external linkage exluding nonstatic class members
			//name of an object with external linkage excluding nonstatic class members
			if( (argOp != null && argOp.getType() == ITypeInfo.PtrOp.t_pointer ) ||
					( arg.isType( ITypeInfo.t_type ) ) )
			{
				ISymbol symbol = arg.getTypeSymbol();
				if ( symbol != null && symbol.getContainingSymbol().isType( ITypeInfo.t_class, ITypeInfo.t_union ) ){
					if( !symbol.isType( ITypeInfo.t_class, ITypeInfo.t_union ) ){
						if( !symbol.getTypeInfo().checkBit( ITypeInfo.isStatic ) ){
							return false;
						}
					}
				}
				
				return hasExternalLinkage( arg );
			}
			
			//integral or enumeration type
			if( op == null && ( arg.isType( ITypeInfo.t_bool, ITypeInfo.t_int ) || 
					arg.isType( ITypeInfo.t_enumerator )           )  )
			{	
				return true;
			}
			
			//name of a non-type template parameter
			if( arg.isType( ITypeInfo.t_templateParameter ) && 
					arg.getTemplateParameterType() != ITypeInfo.t_typeName &&
					arg.getTemplateParameterType() != ITypeInfo.t_template )
			{
				return true;
			}
			return false;
		}
		return true;	
	}
	
	static protected boolean hasExternalLinkage( ITypeInfo type ){
		if( ! type.isType( ITypeInfo.t_type ) )
			return false;
		
		return !hasNoLinkage( type );
	}
	
	static protected boolean hasInternalLinkage( ITypeInfo type ){
		return !hasNoLinkage( type );
	}
	
	static protected boolean hasNoLinkage( ITypeInfo type ){
		if( type.isType( ITypeInfo.t_type ) ){
			ISymbol symbol = type.getTypeSymbol();
			if( symbol.getContainingSymbol() == null ){
				return true;	//a temporary 
			}
			
			return symbol.getContainingSymbol().isType( ITypeInfo.t_function );	
		}
		
		return false;
	}
	
	/**
	 * 14.8.2.1-2 If P is a cv-qualified type, the top level cv-qualifiers of P's type are ignored for type
	 * deduction.  If P is a reference type, the type referred to by P is used for Type deduction.
	 * @param pSymbol
	 * @return
	 */	
	static private ITypeInfo getParameterTypeForDeduction( ISymbol pSymbol ){
		ITypeInfo p = TypeInfoProvider.newTypeInfo( pSymbol.getTypeInfo () );
		List pPtrs = p.getPtrOperators();
		if( pPtrs.size() > 0 ){
			ITypeInfo.PtrOp pOp = (ITypeInfo.PtrOp) pPtrs.get( 0 );
			if( pOp.getType() == ITypeInfo.PtrOp.t_reference || pOp.getType() == ITypeInfo.PtrOp.t_undef_ptr ){
				pPtrs.remove( 0 );	
			} else {
				ITypeInfo.PtrOp newOp = new ITypeInfo.PtrOp( pOp.getType(), false, false );
				pPtrs.set( 0, newOp );
			}
		} else {
			p.setBit( false, ITypeInfo.isConst );
			p.setBit( false, ITypeInfo.isVolatile );
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
	static private ITypeInfo getArgumentTypeForDeduction( ITypeInfo aInfo, boolean pIsAReferenceType ) throws ParserSymbolTableException{
		
		ITypeInfo a = ParserSymbolTable.getFlatTypeInfo( aInfo, null );
		
		if( !pIsAReferenceType ){
			ISymbol aSymbol = a.getTypeSymbol();
			
			if( a.getType() == ITypeInfo.t_type ){
				if( aSymbol == null ){
					throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplateArgument );
				} else if( aSymbol.isType( ITypeInfo.t_function ) &&  a.getPtrOperators().size() == 0 ){
					a.addPtrOperator( new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer ) );	
				}
			}
			List aPtrs = a.getPtrOperators();
			if( aPtrs.size() > 0 ){
				ITypeInfo.PtrOp pOp = (ITypeInfo.PtrOp) aPtrs.get( 0 );
				
				if( pOp.getType() == ITypeInfo.PtrOp.t_array ){
					aPtrs.set( 0, new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer, false, false ) );
				} else {
					aPtrs.set( 0, new ITypeInfo.PtrOp( pOp.getType(), false, false ) );
				}
			} else {
				a.setBit( false, ITypeInfo.isConst );
				a.setBit( false, ITypeInfo.isVolatile );
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
		List parents = a.getParents();
		int size = parents.size();
		for( int i = 0; i < size; i++ ){
			IParentSymbol wrapper = (IParentSymbol) parents.get(i);
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
	
	static private boolean deduceTemplateArgument( ObjectMap map, ISymbol pSymbol, ITypeInfo a ) throws ParserSymbolTableException{
		ISymbol symbol;
		
		boolean pIsAReferenceType = false;
		
		List ptrOps = pSymbol.getPtrOperators();
		if( ptrOps.size() > 0 && ((ITypeInfo.PtrOp)ptrOps.get(0)).getType() == ITypeInfo.PtrOp.t_reference ){
			pIsAReferenceType = true;
		}
		
		ITypeInfo p = getParameterTypeForDeduction( pSymbol );
		
		a = getArgumentTypeForDeduction( a, pIsAReferenceType );
		
		if( p.isType( ITypeInfo.t_type ) ){
			symbol = p.getTypeSymbol();
			ISymbol aSymbol = a.getTypeSymbol();
			if( symbol == null || ( a.isType( ITypeInfo.t_type) && aSymbol == null ) || a.isType( ITypeInfo.t_undef ))
				throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTypeInfo );
			if( symbol instanceof IDeferredTemplateInstance || symbol.isTemplateInstance() ){
			    if( aSymbol == null )
			        return false;
				return deduceFromTemplateTemplateArguments(map, symbol, aSymbol);	
			} 
			if( symbol.isType( ITypeInfo.t_templateParameter ) ){
				if( symbol.getTypeInfo().getTemplateParameterType() == ITypeInfo.t_typeName ){
					//a = getFlatTypeInfo( a );
					List aPtrs = a.getPtrOperators();
					List pPtrs = p.getPtrOperators();
					
					if( pPtrs != null && pPtrs.size() > 0){
						if( aPtrs == null ){
							return false;
						}

						int pSize = pPtrs.size();
						int aSize = aPtrs.size();
						if( pSize != aSize )
							return false;
						
						ITypeInfo.PtrOp pOp = null;
						ITypeInfo.PtrOp aOp = null;

						int aIdx = 0;
						for( int i = 0; i < pSize; i++ ){
							pOp = (ITypeInfo.PtrOp) pPtrs.get(i);
							aOp = (ITypeInfo.PtrOp) aPtrs.get(aIdx++);
							if( pOp.getType() == aOp.getType() ){
								if( !pOp.equals( aOp ) )
									return false;
								aPtrs.remove( --aIdx );
							} else {
								return false;
							}
						} 
					}
					//cvlist T
					if( p.checkBit( ITypeInfo.isConst ) ){
						if( !a.checkBit( ITypeInfo.isConst ) )
							return false;
						a.setBit( false, ITypeInfo.isConst);
					}
					if( p.checkBit( ITypeInfo.isVolatile ) ){
						if( !a.checkBit( ITypeInfo.isVolatile ) )
							return false;
						a.setBit( false, ITypeInfo.isVolatile);
					}
					
					//T
					return deduceArgument( map, symbol, a );
						
				} else if ( symbol.getTypeInfo().getTemplateParameterType() == ITypeInfo.t_template ){
					//TODO
				} else {
					//non-type parameter
					if( symbol.getTypeInfo().getTemplateParameterType() == a.getType() ){
						return deduceArgument( map, symbol, a );
					} 
					return false;
				}
			} 
			//T (*) ( ), T ( T::* ) ( T ), & variations
			else if( symbol.isType( ITypeInfo.t_function ) ){
				if( !(aSymbol instanceof IParameterizedSymbol)|| 
						!aSymbol.isType( ITypeInfo.t_function ) )
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
					ITypeInfo.PtrOp op = (ITypeInfo.PtrOp) pPtrs.get(0);
					if( op.getType() == ITypeInfo.PtrOp.t_memberPointer ){
						ITypeInfo info = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, aFunction.getContainingSymbol() );
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
				int size = pParams.size();
				for( int i = 0; i < size; i++ ){
					ITypeInfo info = ((ISymbol)aParams.get( i )).getTypeInfo();
					if( !deduceTemplateArgument( map, (ISymbol)pParams.get(i), info ) ){
						return false;
					}
				}
				return true;
			} 
			 
		} 
		if( p.isType( ITypeInfo.t_templateParameter ) ){
			return deduceArgument( map, pSymbol, a );
		}
		if( p.getType() == a.getType() ){
			if( p.getDefault() != null ){
			    if( p.getDefault() instanceof char[] && a.getDefault() instanceof char[] )
			        return CharArrayUtils.equals( (char[])p.getDefault(), (char[])a.getDefault() );
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
	private static boolean deduceFromTemplateTemplateArguments(ObjectMap map, ISymbol pSymbol, ISymbol aSymbol) {
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

		int size = pList.size();
		for( int i = 0; i < size; i++ ){
			Object obj = pList.get( i );
			ISymbol sym = null;
			if( obj instanceof ISymbol ){
				sym = (ISymbol) obj;
			} else {
				sym = pSymbol.getSymbolTable().newSymbol( ParserSymbolTable.EMPTY_NAME_ARRAY );
				sym.setTypeInfo( (ITypeInfo) obj );
			}
			
			ITypeInfo arg = transformTypeInfo( aList.get( i ), null );
			
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
	static private ObjectMap deduceTemplateArgumentsUsingParameterList( ITemplateSymbol template, IParameterizedSymbol function ){

		List aList = function.getParameterList();
		int size = aList.size();
		ArrayList args = new ArrayList( size );
		 
		for( int i = 0; i < size; i++ ){
			ISymbol symbol = (ISymbol) aList.get(i);
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
	static private ObjectMap deduceTemplateArguments( ITemplateSymbol template, List arguments ){
		if( template.getContainedSymbols() == CharArrayObjectMap.EMPTY_MAP || template.getContainedSymbols().size() != 1 ){
			return null;
		}

		ISymbol templateSymbol = template.getTemplatedSymbol();
		if( !templateSymbol.isType( ITypeInfo.t_function ) ){
			return null;
		}

		IParameterizedSymbol templateFunction = (IParameterizedSymbol) templateSymbol;
		
		List pList = templateFunction.getParameterList();
//		TODO: ellipses?
		if( pList == null || arguments == null || pList.size() != arguments.size() ){
			return null;
		}
		
		int size = pList.size();
		ObjectMap map = new ObjectMap(size);
		for( int i = 0; i < size; i++ ){
			try {
				if( !deduceTemplateArgument( map, (ISymbol) pList.get(i), (ITypeInfo) arguments.get(i) ) ){
					return null;
				}
			} catch (ParserSymbolTableException e) {
				return null;
			}
		}
		
		return map;			
	}

	static private boolean deduceArgument( ObjectMap map, ISymbol p, ITypeInfo a ){
		
		a = ParserSymbolTable.getFlatTypeInfo( a, null );
		
		if( map.containsKey( p ) ){
			ITypeInfo current = (ITypeInfo)map.get( p );
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
		
		if( decl.isType( ITypeInfo.t_class, ITypeInfo.t_union ) ) {
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
		ObjectMap map = createMapForFunctionTemplateOrdering( spec1 );
		
		IContainerSymbol templatedSymbol = spec1.getTemplatedSymbol();
		if( !( templatedSymbol instanceof IParameterizedSymbol ) )
			throw new ParserSymbolTableError( ParserSymbolTableError.r_InternalError );
		
		IParameterizedSymbol function = (IParameterizedSymbol)templatedSymbol;
		function = (IParameterizedSymbol) function.instantiate( spec1, map );
		((TemplateSymbol)spec1).processDeferredInstantiations();
		
		ObjectMap m1 = deduceTemplateArgumentsUsingParameterList( spec2, function);
		
		map = createMapForFunctionTemplateOrdering( spec2 );
		
		templatedSymbol = spec2.getTemplatedSymbol();
		if( !( templatedSymbol instanceof IParameterizedSymbol ) )
			throw new ParserSymbolTableError( ParserSymbolTableError.r_InternalError );
		
		function = (IParameterizedSymbol)templatedSymbol;
		function = (IParameterizedSymbol) function.instantiate( spec2, map );
		((TemplateSymbol)spec2).processDeferredInstantiations();
		
		ObjectMap m2 = deduceTemplateArgumentsUsingParameterList( spec1, function );
		
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

	static private ObjectMap createMapForFunctionTemplateOrdering( ITemplateSymbol template ){
		ITypeInfo val = null;
		List paramList = template.getParameterList();
		int size = paramList.size();
		ObjectMap map = new ObjectMap(size);
		for( int i = 0; i < size; i++ ){
			ISymbol param = (ISymbol) paramList.get( i );
			//template type parameter
			if( param.getTypeInfo().getTemplateParameterType() == ITypeInfo.t_typeName ){
				val = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, template.getSymbolTable().newSymbol( ParserSymbolTable.EMPTY_NAME_ARRAY, ITypeInfo.t_class ) ); //$NON-NLS-1$
			} 
			//template parameter
			else if ( param.getTypeInfo().getTemplateParameterType() == ITypeInfo.t_template ) {
				//TODO
			}
			//non type parameter 
			else {
				val = TypeInfoProvider.newTypeInfo( param.getTypeInfo().getTemplateParameterType() );
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
		
		IParameterizedSymbol function = specialization.getSymbolTable().newParameterizedSymbol( transformed.getName(), ITypeInfo.t_function );
		try{
			transformed.addSymbol( function );
		} catch ( ParserSymbolTableException e ){
			//we shouldn't get this because there aren't any other symbols in the template
		}
		ISymbol param = specialization.getSymbolTable().newSymbol( ParserSymbolTable.EMPTY_NAME_ARRAY, ITypeInfo.t_type ); //$NON-NLS-1$
		
		param.setTypeSymbol( specialization.instantiate( specialization.getArgumentList() ) );
				
		function.addParameter( param );
		
		return transformed;
	}
	
	static private ITypeInfo transformTypeInfo( Object obj, ObjectMap argumentMap ){
		ITypeInfo info = null;
		if( obj instanceof ISymbol ){
			info = TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, (ISymbol) obj );
		} else {
			info = (ITypeInfo) obj;
		}
		
		if( argumentMap == null )
			return info;
		
		if( info.isType( ITypeInfo.t_type ) && 
			info.getTypeSymbol().isType( ITypeInfo.t_templateParameter ) &&
			argumentMap.containsKey( info.getTypeSymbol() ) )
		{
			ITypeInfo newType = TypeInfoProvider.newTypeInfo( (ITypeInfo) argumentMap.get( info.getTypeSymbol() ) );
			if( info.hasPtrOperators() )
				newType.addPtrOperator( info.getPtrOperators() );
			
			return newType;
		}
		
		return info;
	}
	
	static protected List selectTemplateFunctions( ObjectSet templates, List functionArguments, List templateArguments ) throws ParserSymbolTableException{
		if( templates == null || templates.size() == 0 )
			return null;
		
		List instances = null;
		
		//Iterator iter = templates.iterator();
		int size = templates.size();
		
		outer: for( int idx = 0; idx < size; idx++ ){
			IParameterizedSymbol fn = (IParameterizedSymbol) templates.keyAt( idx );
			ITemplateSymbol template = (ITemplateSymbol) fn.getContainingSymbol();
			
			ObjectMap map = deduceTemplateArguments( template, functionArguments );
			
			if( map == null )
				continue;
			
			List templateParams = template.getParameterList();
			int numTemplateParams = templateParams.size();
			int numTemplateArgs = ( templateArguments != null ) ? templateArguments.size() : 0;
			List instanceArgs = new ArrayList( templateParams.size() );
			for( int i = 0; i < numTemplateParams; i++ ){
				ISymbol param = (ISymbol) templateParams.get(i);
				ITypeInfo arg = (ITypeInfo) (  i < numTemplateArgs ? templateArguments.get(i) : null);
				ITypeInfo mapped = (ITypeInfo) map.get( param );
				
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
					instances = new ArrayList(4);
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
					int size = parameters.size();
					for( int i = 0; i < size; i++ ){
						if( parameters.get(i) != ((ITypeInfo) arguments.get(i)).getTypeSymbol() ){
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
		
		int size = p1.size();
		for( int i = 0; i < size; i++ ){
			ISymbol param1 = (ISymbol) p1.get( i );
			ISymbol param2 = (ISymbol) p2.get( i );
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
		
		ObjectMap m [] = { new ObjectMap(p1.size()), new ObjectMap(p1.size()) };
		
		for( List list = p1; list != null; list = p2 ){
			int size = list.size();
			int index = 0;
			for( int i = 0; i < size; i++ ) {
				m[ ( list == p2 )? 1 : 0 ].put( list.get( i ), new Integer( index++ ) );
			}
			
			if( list == p2 ){
				break;
			}
		}
		
		int a1Size = a1.size();
		for( int i = 0; i < a1Size; i++ ){
			ITypeInfo t1 = (ITypeInfo) a1.get( i );
			ITypeInfo t2 = (ITypeInfo) a2.get( i );
			
			if( t1.equals( t2 ) ){
				continue;
			} else if( t1.isType( ITypeInfo.t_type ) && t2.isType( ITypeInfo.t_type ) ) {
				ISymbol s1 = t1.getTypeSymbol(), s2 = t2.getTypeSymbol();
				if( m[0].containsKey( s1 ) && m[1].containsKey( s2 ) && m[0].get( s1 ).equals( m[1].get( s2 ) ) )
					continue;
			} 
			
			return false;
		}
		return true;
	}
	
	static private ISpecializedSymbol findPartialSpecialization( ITemplateSymbol template, List parameters, List arguments ){
		List specs = template.getSpecializations();
		int size = specs.size();
		ISpecializedSymbol spec = null;
		for( int i = 0; i < size; i++ ){
			spec = (ISpecializedSymbol) specs.get(i);
			
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
	
	static protected ISymbol translateParameterForDefinition ( ISymbol templatedSymbol, ISymbol param, ObjectMap defnMap ){
		if( defnMap == ObjectMap.EMPTY_MAP || templatedSymbol == null ){
			return param;
		}
		
		ISymbol mappedParam = param;
		while( mappedParam.isTemplateInstance() ){
			mappedParam = mappedParam.getInstantiatedSymbol();
		}
			
		if( defnMap.containsKey( templatedSymbol ) ){
		    ObjectMap map = (ObjectMap) defnMap.get( templatedSymbol );
			
			for( int i = 0; i < map.size(); i++){
				ISymbol key = (ISymbol) map.keyAt(i);
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
	 * @throws ParserSymbolTableException
	 */
	static protected ISymbol instantiateWithinTemplateScope( IContainerSymbol container, ITemplateSymbol symbol ) throws ParserSymbolTableException
	{
		if( symbol.getTemplatedSymbol() == null || symbol.getTemplatedSymbol().isType( ITypeInfo.t_function ) ){
			return symbol;
		}
		
		ISymbol instance = null;
		ITemplateSymbol template = null;
		IContainerSymbol containing = container.getContainingSymbol();
		boolean instantiate = false;
		while( containing != null ){
			if( containing == symbol  || 
				( containing instanceof ISpecializedSymbol && ((ISpecializedSymbol)containing).getPrimaryTemplate() == symbol ) )
			{
				instantiate = true;
				template = (ITemplateSymbol) containing;
				break;
			}
			containing = containing.getContainingSymbol();
			
			if( containing != null && !containing.isTemplateMember() || !containing.isType( ITypeInfo.t_template ) ){
				break;
			}
		}
		
		if( instantiate ){
			if( template instanceof ISpecializedSymbol ){
				ISpecializedSymbol spec = (ISpecializedSymbol) template;
				instance = spec.instantiate( spec.getArgumentList() );
			} else {
				List params = template.getParameterList();
				int size = params.size();
				List args = new ArrayList( size );
				for( int i = 0; i < size; i++ ){
					args.add( TypeInfoProvider.newTypeInfo( ITypeInfo.t_type, 0, (ISymbol) params.get(i) ) );
				}
				
				instance = template.instantiate( args );
			}
		}
		
		return ( instance != null ) ? instance : (ISymbol) symbol;
	}
	
	static protected boolean alreadyHasTemplateParameter( IContainerSymbol container, char[] name ){
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
		if( !containing.isType( ITypeInfo.t_namespace ) && !containing.isType( ITypeInfo.t_class, ITypeInfo.t_union ) ){
			return false;
		}	
		
		//14.5.2-3  A member function template shall not be virtual
		if( containing.isTemplateMember() && containing.getContainingSymbol().isType( ITypeInfo.t_template ) ){
			ISymbol symbol = template.getTemplatedSymbol();
			if( symbol != null && symbol.isType( ITypeInfo.t_function ) && symbol.getTypeInfo().checkBit( ITypeInfo.isVirtual ) ){
				return false;
			}
		}
		
		return true;
	}
	
	static protected List verifyExplicitArguments( ITemplateSymbol template, List arguments, ISymbol symbol ) throws ParserSymbolTableException{
		List params = template.getParameterList();
		
		int numParams = params.size();
		int numArgs = arguments.size();
		List actualArgs = new ArrayList( numParams );
		for( int i = 0; i < numParams; i++ ){
			ISymbol param = (ISymbol) params.get(i);
			if( i < numArgs ){
				ITypeInfo arg = (ITypeInfo) arguments.get(i);
				if( matchTemplateParameterAndArgument( param, arg ) ){
					actualArgs.add( arg );
				} else {
					throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTemplateArgument );
				}
			} else {
				//14.7.3-11 a trailing template-argument can be left unspecified in the template-id naming an explicit
				//function template specialization provided it can be deduced from the function argument type
				if( template.getTemplatedSymbol() instanceof IParameterizedSymbol &&
					symbol instanceof IParameterizedSymbol && CharArrayUtils.equals( template.getTemplatedSymbol().getName(), symbol.getName() ) )
				{
					ObjectMap map = deduceTemplateArgumentsUsingParameterList( template, (IParameterizedSymbol) symbol ); 
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
	
	static protected ITemplateSymbol resolveTemplateFunctions( ObjectSet functions, List args, ISymbol symbol ) throws ParserSymbolTableException{
		ITemplateSymbol template = null;
		
		outer: for( int i = 0; i < functions.size(); i++ ){
		    IParameterizedSymbol fn = (IParameterizedSymbol) functions.keyAt(i);
			ITemplateSymbol tmpl = (ITemplateSymbol) fn.getContainingSymbol();
			
			ObjectMap map = deduceTemplateArgumentsUsingParameterList( tmpl, (IParameterizedSymbol) symbol );
			
			if( map == null )
				continue;			 
			List params = tmpl.getParameterList();
			int numParams = params.size();
			int numArgs = args.size();
			for( int j = 0; j < numParams && j < numArgs; j++ ){
				ISymbol param = (ISymbol) params.get(j);
				ITypeInfo arg = (ITypeInfo) args.get(j);
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
		List resultList = new ArrayList();
		
		List params = template.getParameterList();
		ObjectMap map = null;
		
		int numParams = params.size();
		int numArgs = ( args != null ) ? args.size() : 0;
		for( int i = 0; i < numParams; i++ ){
			ISymbol param = (ISymbol) params.get(i);
			ITypeInfo arg = null;
			if( i < numArgs ){
				arg = (ITypeInfo) args.get(i);
			} else {
				if( map == null ){
					map = deduceTemplateArgumentsUsingParameterList( template, fn );
					if(map == null )
						return null;
				}
				if( map.containsKey( param ) ){
					arg = (ITypeInfo) map.get( param );
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
			ObjectMap specs = template.getExplicitSpecializations();
			List args = null;
			for( int i = 0; i < specs.size(); i++ ){
				args = (List) specs.keyAt(i);
				
				if( args.equals( arguments ) ){
				    ObjectMap explicitMap = (ObjectMap) template.getExplicitSpecializations().get( args );
					if( explicitMap.containsKey( symbol ) ){
						return (ISymbol) explicitMap.get( symbol );
					}
				}
			}
		}
		
		return null;
	}
	
	static protected boolean templateParametersAreEquivalent( ISymbol p1, ISymbol p2 ){
		if( !p1.isType( ITypeInfo.t_templateParameter ) || !p2.isType( ITypeInfo.t_templateParameter ) ||
			 p1.getTypeInfo().getTemplateParameterType() != p2.getTypeInfo().getTemplateParameterType() )
		{
			return false;
		}
		
		ITemplateSymbol t1 = getContainingTemplate( p1 );
		ITemplateSymbol t2 = getContainingTemplate( p2 );
		
		if( t1 == null || t2 == null )
		    return false;
		
		if( p1.getTypeInfo().getTemplateParameterType() == ITypeInfo.t_typeName )
		{
			List l1 = t1.getParameterList(), l2 = t2.getParameterList();
			return ( l1 != null && l2 != null &&  l1.indexOf( p1 ) == l2.indexOf( p2 ) ); 
		} else if( p1.getTypeInfo().getTemplateParameterType() == ITypeInfo.t_template ){
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
		int size = args.size();
		if( size != args2.size() )
			return false;
		
		for( int i = 0; i < size; i++ ){
			ITypeInfo info1 = (ITypeInfo) args.get(i);
			ITypeInfo info2 = (ITypeInfo) args2.get(i);
			
			if( ! info1.equals( info2 ) )
				return false;
		}
		return true;
	}
}
