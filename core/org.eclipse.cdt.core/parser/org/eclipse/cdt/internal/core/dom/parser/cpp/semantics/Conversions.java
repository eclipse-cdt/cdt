/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Bryan Wilkinson (QNX)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getUltimateType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getUltimateTypeViaTypedefs;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassTemplate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;

/**
 * Routines for calculating the cost of conversions
 * 
 * See
 *    [conv] 4
 *    [over.best.ics] 13.3.3.1.
 */
class Conversions {

	/**
	 * Computes the cost of using the standard conversion sequence from source to target.
	 * @param ignoreDerivedToBaseConversion handles the special case when members of different
	 *    classes are nominated via using-declarations. In such a situation the derived to
	 *    base conversion does not cause any costs.
	 * @throws DOMException
	 */
	static protected Cost checkStandardConversionSequence( IType source, IType target, boolean isImplicitThis) throws DOMException {
		Cost cost = lvalue_to_rvalue( source, target );

		if( cost.source == null || cost.target == null ){
			return cost;
		}

		if (cost.source.isSameType(cost.target) || 
				// 7.3.3.13 for overload resolution the implicit this pointer is treated as if 
				// it were a pointer to the derived class
				(isImplicitThis && cost.source instanceof ICPPClassType && cost.target instanceof ICPPClassType)) {
			cost.rank = Cost.IDENTITY_RANK;
			return cost;
		}

		qualificationConversion( cost );

		//if we can't convert the qualifications, then we can't do anything
		if( cost.qualification == Cost.NO_MATCH_RANK ){
			return cost;
		}

		//was the qualification conversion enough?
		IType s = getUltimateType( cost.source, true );
		IType t = getUltimateType( cost.target, true );

		if( s == null || t == null ){
			cost.rank = Cost.NO_MATCH_RANK;
			return cost;
		}

		if (s.isSameType(t) || 
				// 7.3.3.13 for overload resolution the implicit this pointer is treated as if 
				// it were a pointer to the derived class
				(isImplicitThis && s instanceof ICPPClassType && t instanceof ICPPClassType)) {
			return cost;
		}

		promotion( cost );
		if( cost.promotion > 0 || cost.rank > -1 ){
			return cost;
		}

		conversion( cost );

		if( cost.rank > -1 )
			return cost;

		derivedToBaseConversion( cost );

		if( cost.rank == -1 ){
			relaxTemplateParameters( cost );
		}
		return cost;	
	}

	static Cost checkUserDefinedConversionSequence( IType source, IType target ) throws DOMException {
		Cost cost = null;
		Cost constructorCost = null;
		Cost conversionCost = null;

		IType s = getUltimateType( source, true );
		IType t = getUltimateType( target, true );

		ICPPConstructor constructor = null;
		ICPPMethod conversion = null;

		//constructors
		if( t instanceof ICPPClassType ){
			ICPPConstructor [] constructors = ((ICPPClassType)t).getConstructors();
			if( constructors.length > 0 ){
				if( constructors.length == 1 && constructors[0] instanceof IProblemBinding )
					constructor = null;
				else {
					LookupData data = new LookupData();
					data.forUserDefinedConversion = true;
					data.functionParameters = new IType [] { source };
					IBinding binding = CPPSemantics.resolveFunction( data, constructors );
					if( binding instanceof ICPPConstructor )
						constructor = (ICPPConstructor) binding;
				}
			}
			if( constructor != null && !constructor.isExplicit() ){
				constructorCost = checkStandardConversionSequence( t, target, false );
			}
		}

		boolean checkConversionOperators=
			(SemanticUtil.ENABLE_224364 && s instanceof ICPPClassType)
			|| (s instanceof CPPClassType
			|| s instanceof CPPClassTemplate
			|| s instanceof CPPClassSpecialization
			|| s instanceof CPPClassInstance);
		
		//conversion operators
		if (checkConversionOperators) {
			ICPPMethod [] ops = SemanticUtil.getConversionOperators((ICPPClassType)s); 
			if( ops.length > 0 && !(ops[0] instanceof IProblemBinding) ){
				Cost [] costs = null;
				for (int i = 0; i < ops.length; i++) {
					cost = checkStandardConversionSequence( ops[i].getType().getReturnType(), target, false );
					if( cost.rank != Cost.NO_MATCH_RANK )
						costs = (Cost[]) ArrayUtil.append( Cost.class, costs, cost );
				}
				if( costs != null ){
					Cost best = costs[0];
					boolean bestIsBest = true;
					int bestIdx = 0;
					for (int i = 1; i < costs.length && costs[i] != null; i++) {
						int comp = best.compare( costs[i] );
						if( comp == 0 )
							bestIsBest = false;
						else if( comp > 0 ){
							best = costs[ bestIdx = i ];
							bestIsBest = true;
						}
					}
					if( bestIsBest ){
						conversion = ops[ bestIdx ]; 
						conversionCost = best;
					}
				}
			}
		}

		//if both are valid, then the conversion is ambiguous
		if( constructorCost != null && constructorCost.rank != Cost.NO_MATCH_RANK && 
				conversionCost != null && conversionCost.rank != Cost.NO_MATCH_RANK )
		{
			cost = constructorCost;
			cost.userDefined = Cost.AMBIGUOUS_USERDEFINED_CONVERSION;	
			cost.rank = Cost.USERDEFINED_CONVERSION_RANK;
		} else {
			if( constructorCost != null && constructorCost.rank != Cost.NO_MATCH_RANK ){
				cost = constructorCost;
				cost.userDefined = constructor.hashCode();
				cost.rank = Cost.USERDEFINED_CONVERSION_RANK;
			} else if( conversionCost != null && conversionCost.rank != Cost.NO_MATCH_RANK ){
				cost = conversionCost;
				cost.userDefined = conversion.hashCode();
				cost.rank = Cost.USERDEFINED_CONVERSION_RANK;
			} 			
		}
		return cost;
	}

	/**
	 * Calculates the number of edges in the inheritance path of <code>clazz</code> to
	 * <code>ancestorToFind</code>, returning -1 if no inheritance relationship is found.
	 * @param clazz the class to search upwards from
	 * @param ancestorToFind the class to find in the inheritance graph
	 * @return the number of edges in the inheritance graph, or -1 if the specifide classes have
	 * no inheritance relation
	 * @throws DOMException
	 */
	private static int calculateInheritanceDepth(int maxdepth, ICPPClassType clazz, ICPPClassType ancestorToFind) throws DOMException {
		if(clazz == ancestorToFind || clazz.isSameType(ancestorToFind))
			return 0;

		if(maxdepth>0) {
			ICPPBase [] bases = clazz.getBases();
			for(int i=0; i<bases.length; i++) {
				IBinding base = bases[i].getBaseClass();
				if(base instanceof IType) {
					IType tbase= (IType) base;
					if( tbase.isSameType(ancestorToFind) || 
							(ancestorToFind instanceof ICPPSpecialization &&  /*allow some flexibility with templates*/ 
									((IType)((ICPPSpecialization)ancestorToFind).getSpecializedBinding()).isSameType(tbase) ) ) 
					{
						return 1;
					}

					tbase= getUltimateTypeViaTypedefs(tbase);
					if(tbase instanceof ICPPClassType) {
						int n= calculateInheritanceDepth(maxdepth-1, (ICPPClassType) tbase, ancestorToFind );
						if(n>0)
							return n+1;
					}
				}
			}
		}

		return -1;
	}

	static private void conversion( Cost cost ) throws DOMException{
		IType src = cost.source;
		IType trg = cost.target;

		cost.conversion = 0;
		cost.detail = 0;

		IType[] sHolder= new IType[1], tHolder= new IType[1];
		IType s = getUltimateType( src, sHolder, true );
		IType t = getUltimateType( trg, tHolder, true );
		IType sPrev= sHolder[0], tPrev= tHolder[0];

		if( src instanceof IBasicType && trg instanceof IPointerType ){
			//4.10-1 an integral constant expression of integer type that evaluates to 0 can be converted to a pointer type
			IASTExpression exp = ((IBasicType)src).getValue();
			if( exp instanceof IASTLiteralExpression && 
					((IASTLiteralExpression)exp).getKind() == IASTLiteralExpression.lk_integer_constant  )
			{
				try { 
					String val = exp.toString().toLowerCase().replace('u', '0');
					val.replace( 'l', '0' );
					if( Integer.decode( val ).intValue() == 0 ){
						cost.rank = Cost.CONVERSION_RANK;
						cost.conversion = 1;
					}
				} catch( NumberFormatException e ) {
				}
			}
		} else if( sPrev instanceof IPointerType ){
			//4.10-2 an rvalue of type "pointer to cv T", where T is an object type can be
			//converted to an rvalue of type "pointer to cv void"
			if( tPrev instanceof IPointerType && t instanceof IBasicType && ((IBasicType)t).getType() == IBasicType.t_void ){
				cost.rank = Cost.CONVERSION_RANK;
				cost.conversion = 1;
				cost.detail = 2;
				return;
			}
			//4.10-3 An rvalue of type "pointer to cv D", where D is a class type can be converted
			//to an rvalue of type "pointer to cv B", where B is a base class of D.
			else if( s instanceof ICPPClassType && tPrev instanceof IPointerType && t instanceof ICPPClassType ){
				int depth= calculateInheritanceDepth(CPPSemantics.MAX_INHERITANCE_DEPTH, (ICPPClassType)s, (ICPPClassType) t );
				cost.rank= ( depth > -1 ) ? Cost.CONVERSION_RANK : Cost.NO_MATCH_RANK;
				cost.conversion= ( depth > -1 ) ? depth : 0;
				cost.detail= 1;
				return;
			}
			// 4.12 if the target is a bool, we can still convert
			else if(!(trg instanceof IBasicType && ((IBasicType)trg).getType() == ICPPBasicType.t_bool)) {
				return;
			}
		}

		if( t instanceof IBasicType && s instanceof IBasicType || s instanceof IEnumeration ){
			//4.7 An rvalue of an integer type can be converted to an rvalue of another integer type.  
			//An rvalue of an enumeration type can be converted to an rvalue of an integer type.
			cost.rank = Cost.CONVERSION_RANK;
			cost.conversion = 1;	
		} else if( trg instanceof IBasicType && ((IBasicType)trg).getType() == ICPPBasicType.t_bool && s instanceof IPointerType ){
			//4.12 pointer or pointer to member type can be converted to an rvalue of type bool
			cost.rank = Cost.CONVERSION_RANK;
			cost.conversion = 1;
		} else if( s instanceof ICPPPointerToMemberType && t instanceof ICPPPointerToMemberType ){
			//4.11-2 An rvalue of type "pointer to member of B of type cv T", where B is a class type, 
			//can be converted to an rvalue of type "pointer to member of D of type cv T" where D is a
			//derived class of B
			ICPPPointerToMemberType spm = (ICPPPointerToMemberType) s;
			ICPPPointerToMemberType tpm = (ICPPPointerToMemberType) t;
			IType st = spm.getType();
			IType tt = tpm.getType();
			if( st != null && tt != null && st.isSameType( tt ) ){
				int depth= calculateInheritanceDepth(CPPSemantics.MAX_INHERITANCE_DEPTH, tpm.getMemberOfClass(), spm.getMemberOfClass());
				cost.rank= ( depth > -1 ) ? Cost.CONVERSION_RANK : Cost.NO_MATCH_RANK;
				cost.conversion= ( depth > -1 ) ? depth : 0;
				cost.detail= 1;
			}
		}
	}

	static private void derivedToBaseConversion( Cost cost ) throws DOMException {
		IType s = getUltimateType( cost.source, true );
		IType t = getUltimateType( cost.target, true );

		if( cost.targetHadReference && s instanceof ICPPClassType && t instanceof ICPPClassType ){
			int depth= calculateInheritanceDepth(CPPSemantics.MAX_INHERITANCE_DEPTH, (ICPPClassType) s, (ICPPClassType) t);
			if(depth > -1){
				cost.rank= Cost.DERIVED_TO_BASE_CONVERSION;
				cost.conversion= depth;
			}	
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
	 * @throws DOMException
	 */
	static private void promotion( Cost cost ) throws DOMException{
		IType src = cost.source;
		IType trg = cost.target;

		if( src.isSameType( trg ) )
			return;

		if( src instanceof IBasicType && trg instanceof IBasicType ){
			int sType = ((IBasicType)src).getType();
			int tType = ((IBasicType)trg).getType();
			if( ( tType == IBasicType.t_int && ( sType == IBasicType.t_int ||   //short, long , unsigned etc
					sType == IBasicType.t_char    || 
					sType == ICPPBasicType.t_bool || 
					sType == ICPPBasicType.t_wchar_t ||
					sType == IBasicType.t_unspecified ) ) || //treat unspecified as int
					( tType == IBasicType.t_double && sType == IBasicType.t_float ) )
			{
				cost.promotion = 1; 
			}
		} else if( src instanceof IEnumeration && trg instanceof IBasicType &&
				( ((IBasicType)trg).getType() == IBasicType.t_int || 
						((IBasicType)trg).getType() == IBasicType.t_unspecified ) )
		{
			cost.promotion = 1; 
		}

		cost.rank = (cost.promotion > 0 ) ? Cost.PROMOTION_RANK : Cost.NO_MATCH_RANK;
	}

	static private void qualificationConversion( Cost cost ) throws DOMException{
		boolean canConvert = true;
		int requiredConversion = Cost.IDENTITY_RANK;  

		IType s = cost.source, t = cost.target;
		boolean constInEveryCV2k = true;
		boolean firstPointer= true;
		while( true ){
			s= getUltimateTypeViaTypedefs(s);
			t= getUltimateTypeViaTypedefs(t);
			IPointerType op1= s instanceof IPointerType ? (IPointerType) s : null;
			IPointerType op2= t instanceof IPointerType ? (IPointerType) t : null;

			if( op1 == null && op2 == null )
				break;
			else if( op1 == null ^ op2 == null) {
				// 4.12 - pointer types can be converted to bool
				if(t instanceof ICPPBasicType) {
					if(((ICPPBasicType)t).getType() == ICPPBasicType.t_bool) {
						canConvert= true;
						requiredConversion = Cost.CONVERSION_RANK;
						break;
					}
				}
				canConvert = false; 
				break;
			} else if( op1 instanceof ICPPPointerToMemberType ^ op2 instanceof ICPPPointerToMemberType ){
				canConvert = false;
				break;
			} 

			//if const is in cv1,j then const is in cv2,j.  Similary for volatile
			if( ( op1.isConst() && !op2.isConst() ) || ( op1.isVolatile() && !op2.isVolatile() ) ) {
				canConvert = false;
				requiredConversion = Cost.NO_MATCH_RANK;
				break;
			}
			//if cv1,j and cv2,j are different then const is in every cv2,k for 0<k<j
			if( !constInEveryCV2k && ( op1.isConst() != op2.isConst() ||
					op1.isVolatile() != op2.isVolatile() ) )
			{
				canConvert = false;
				requiredConversion = Cost.NO_MATCH_RANK;
				break; 
			}
			constInEveryCV2k &= (firstPointer || op2.isConst());
			s = op1.getType();
			t = op2.getType();
			firstPointer= false;
		}

		if( s instanceof IQualifierType ^ t instanceof IQualifierType ){
			if( t instanceof IQualifierType ){
				if (!constInEveryCV2k) {
					canConvert= false;
					requiredConversion= Cost.NO_MATCH_RANK;
				}
				else {
					canConvert = true;
					requiredConversion = Cost.CONVERSION_RANK;
				}
			} else {
				//4.2-2 a string literal can be converted to pointer to char
				if( t instanceof IBasicType && ((IBasicType)t).getType() == IBasicType.t_char &&
						s instanceof IQualifierType )
				{
					IType qt = ((IQualifierType)s).getType();
					if( qt instanceof IBasicType ){
						IASTExpression val = ((IBasicType)qt).getValue();
						canConvert = (val != null && 
								val instanceof IASTLiteralExpression && 
								((IASTLiteralExpression)val).getKind() == IASTLiteralExpression.lk_string_literal );
					} else {
						canConvert = false;
						requiredConversion = Cost.NO_MATCH_RANK;
					}
				} else {
					canConvert = false;
					requiredConversion = Cost.NO_MATCH_RANK;
				}
			}
		} else if( s instanceof IQualifierType && t instanceof IQualifierType ){
			IQualifierType qs = (IQualifierType) s, qt = (IQualifierType) t;
			if( qs.isConst() == qt.isConst() && qs.isVolatile() == qt.isVolatile() ) {
				requiredConversion = Cost.IDENTITY_RANK;
			}
			else if( (qs.isConst() && !qt.isConst()) || (qs.isVolatile() && !qt.isVolatile()) || !constInEveryCV2k ) {
				requiredConversion = Cost.NO_MATCH_RANK;
				canConvert= false;
			}
			else
				requiredConversion = Cost.CONVERSION_RANK;
		} else if( constInEveryCV2k && !canConvert ){
			canConvert = true;
			requiredConversion = Cost.CONVERSION_RANK;
			int i = 1;
			for( IType type = s; canConvert == true && i == 1; type = t, i++  ){
				while( type instanceof ITypeContainer ){
					if( type instanceof IQualifierType )
						canConvert = false;
					else if( type instanceof IPointerType ){
						canConvert = !((IPointerType)type).isConst() && !((IPointerType)type).isVolatile();
					}
					if( !canConvert ){
						requiredConversion = Cost.NO_MATCH_RANK;
						break;
					}
					type = ((ITypeContainer)type).getType();
				}
			}
		}

		cost.qualification = requiredConversion;
		if( canConvert == true ){
			cost.rank = Cost.LVALUE_OR_QUALIFICATION_RANK;
		}
	}

	static private Cost lvalue_to_rvalue( IType source, IType target ) throws DOMException{
		Cost cost = new Cost( source, target );

		if( ! isCompleteType( source ) ){
			cost.rank = Cost.NO_MATCH_RANK;
			return cost;
		}

		if( source instanceof ICPPReferenceType ){
			source = ((ICPPReferenceType) source).getType();
		}
		if( target instanceof ICPPReferenceType ){
			target = ((ICPPReferenceType) target).getType();
			cost.targetHadReference = true;
		}

		//4.3 function to pointer conversion
		if( target instanceof IPointerType && ((IPointerType)target).getType() instanceof IFunctionType &&
				source instanceof IFunctionType )
		{
			source = new CPPPointerType( source );
		}
		//4.2 Array-To-Pointer conversion
		else if( target instanceof IPointerType && source instanceof IArrayType ){
			source = new CPPPointerType( ((IArrayType)source).getType() );
		}

		//4.1 if T is a non-class type, the type of the rvalue is the cv-unqualified version of T
		if( source instanceof IQualifierType ){
			IType t = ((IQualifierType)source).getType();
			while( t instanceof ITypedef )
				t = ((ITypedef)t).getType();
			if( !(t instanceof ICPPClassType) ){
				source = t;
			}
		} else if( source instanceof IPointerType && 
				( ((IPointerType)source).isConst() || ((IPointerType)source).isVolatile() ) )
		{
			IType t = ((IPointerType)source).getType();
			while( t instanceof ITypedef )
				t = ((ITypedef)t).getType();
			if( !(t instanceof ICPPClassType) ){
				source = new CPPPointerType( t );
			}
		}

		cost.source = source;
		cost.target = target;

		return cost;
	}

	static private boolean isCompleteType( IType type ){
		type = getUltimateType( type, false );
		if( type instanceof ICPPClassType && type instanceof ICPPInternalBinding )
			return (((ICPPInternalBinding)type).getDefinition() != null );
		return true;
	}

	static private void relaxTemplateParameters( Cost cost ){
		IType s = getUltimateType( cost.source, false );
		IType t = getUltimateType( cost.target, false );

		if( (s instanceof ICPPTemplateTypeParameter && t instanceof ICPPTemplateTypeParameter) ||
				(s instanceof ICPPTemplateTemplateParameter && t instanceof ICPPTemplateTemplateParameter ) )
		{
			cost.rank = Cost.FUZZY_TEMPLATE_PARAMETERS;
		}
	}
}
