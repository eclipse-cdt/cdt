/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;

/**
 * Algorithms for deducing template arguments in various contexts.
 */
public class TemplateArgumentDeduction {
	/**
	 * Deduce arguments for a template function from the template id + the template function parameters.
	 * 14.8.2.1
	 */
	static ICPPTemplateArgument[] deduceForFunctionCall(ICPPFunctionTemplate template,
			ICPPTemplateArgument[] tmplArgs, List<IType> fnArgs, List<ValueCategory> argIsLValue, CPPTemplateParameterMap map)
			throws DOMException {
		
		final ICPPTemplateParameter[] tmplParams = template.getTemplateParameters();
		
		if (!addExplicitArguments(tmplParams, tmplArgs, map))
			return null;
				
		if (!deduceFromFunctionArgs(template, fnArgs, argIsLValue, map)) 
			return null;
		
		return createArguments(map, tmplParams);
	}

	/**
	 * Deduces the mapping for the template parameters from the function parameters,
	 * returns <code>false</code> if there is no mapping.
	 */
	static boolean deduceFromFunctionArgs(ICPPFunctionTemplate template, List<IType> fnArgs, List<ValueCategory> argCats,
			CPPTemplateParameterMap map) {
		try {
			IType[] fnPars = template.getType().getParameterTypes();
			final int fnParCount = fnPars.length;
			final ICPPTemplateParameter[] tmplPars = template.getTemplateParameters();
			TemplateArgumentDeduction deduct= new TemplateArgumentDeduction(tmplPars, map, new CPPTemplateParameterMap(fnParCount), 0);
			IType fnParPack= null;
			argLoop: for (int j= 0; j < fnArgs.size(); j++) {
				IType par;
				if (fnParPack != null) {
					par= fnParPack;
					deduct.incPackOffset();
				} else if (j < fnParCount) {
					par= fnPars[j];
					if (par instanceof ICPPParameterPackType) {
						if (j != fnParCount - 1) 
							continue argLoop; 	// Non-deduced context
						
						par= fnParPack= ((ICPPParameterPackType) par).getType();
						deduct= new TemplateArgumentDeduction(deduct, fnArgs.size() - j);
					} 
				} else {
					break;
				}
				
				par= CPPTemplates.instantiateType(par, map, -1, null);
				if (!CPPTemplates.isValidType(par))
					return false;
				
				if (CPPTemplates.isDependentType(par)) {
					IType arg = fnArgs.get(j);
					par= SemanticUtil.getNestedType(par, SemanticUtil.TDEF); // adjustParameterType preserves typedefs
					
					// C++0x: 14.9.2.1-1
					if (arg instanceof InitializerListType) {
						par= SemanticUtil.getNestedType(par, TDEF | REF | CVTYPE);
	
						// Check if this is a deduced context
						IType inner= Conversions.getInitListType(par);
						if (inner != null) {
							final InitializerListType initListType = (InitializerListType) arg;
							IType[] types = initListType.getExpressionTypes();
							ValueCategory[] valueCats = initListType.getValueCategories();
							for (int i = 0; i < types.length; i++) {
								if (!deduceFromFunctionArg(inner, types[i], valueCats[i], deduct))
									return false;
							}
						}
					} else if (arg instanceof FunctionSetType) {
						// 14.8.2.1-6 Handling of overloaded function sets
						ICPPFunction[] fs= ((FunctionSetType) arg).getFunctionSet();
						for (ICPPFunction f : fs) {
							if (f instanceof ICPPFunctionTemplate)
								continue argLoop; // Non-deduced context
						}
						
						// Do trial deduction
						CPPTemplateParameterMap success= null;
						Set<String> handled= new HashSet<String>();
						for (ICPPFunction f : fs) {
							arg= f.getType();
							if (f instanceof ICPPMethod && !f.isStatic()) {
								arg= new CPPPointerToMemberType(arg, ((ICPPMethod) f).getClassOwner(), false, false, false);
							}
							if (handled.add(ASTTypeUtil.getType(arg, true))) {
								final CPPTemplateParameterMap state = deduct.saveState();
								if (deduceFromFunctionArg(par, arg, argCats.get(j), deduct)) {
									if (success != null) {
										deduct.restoreState(state);
										continue argLoop; // Non-deduced context
									}
									success= deduct.saveState();
								}
								deduct.restoreState(state);
							}
						}
						if (success == null)
							return false;
						deduct.restoreState(success);
					} else {
						if (!deduceFromFunctionArg(par, arg, argCats.get(j), deduct)) 
							return false;
					}
				}
			}
			
			if (!map.addDeducedArgs(deduct.fDeducedArgs))
				return false;
			
			return verifyDeduction(tmplPars, map, true);
		} catch (DOMException e) {
		}
		return false;
	}

	private static boolean deduceFromFunctionArg(IType par, IType arg, ValueCategory valueCat, TemplateArgumentDeduction deduct) throws DOMException {
		boolean isReferenceTypeParameter= false;
		if (par instanceof ICPPReferenceType) {
			// If P is an rvalue reference to a cv-unqualified template parameter and the argument is an
			// lvalue, the type "lvalue reference to A" is used in place of A for type deduction.
			isReferenceTypeParameter= true;
			final ICPPReferenceType refPar = (ICPPReferenceType) par;
			if (refPar.isRValueReference() && refPar.getType() instanceof ICPPTemplateParameter && 
					valueCat == LVALUE) {
				arg= new CPPReferenceType(getSimplifiedType(arg), false);
			} else {
				arg= getArgumentTypeForDeduction(arg, true);
			}
			par= SemanticUtil.getNestedType(par, REF | TDEF);
		} else {
			arg= getArgumentTypeForDeduction(arg, false);
		}
		
		// 14.8.2.1-3
		CVQualifier cvPar= SemanticUtil.getCVQualifier(par);
		CVQualifier cvArg= SemanticUtil.getCVQualifier(arg);
		if (cvPar == cvArg || (isReferenceTypeParameter && cvPar.isAtLeastAsQualifiedAs(cvArg))) {
			IType pcheck= SemanticUtil.getNestedType(par, CVTYPE);
			if (!(pcheck instanceof ICPPTemplateParameter)) {
				par= pcheck;
				arg= SemanticUtil.getNestedType(arg, CVTYPE);
				IType argcheck= arg;
				if (par instanceof IPointerType && arg instanceof IPointerType) {
					pcheck= ((IPointerType) par).getType();
					argcheck= ((IPointerType) arg).getType();
					if (pcheck instanceof ICPPTemplateParameter) {
						pcheck= null;
					} else {
						cvPar= SemanticUtil.getCVQualifier(pcheck);
						cvArg= SemanticUtil.getCVQualifier(argcheck);
						if (cvPar.isAtLeastAsQualifiedAs(cvArg)) {
							pcheck= SemanticUtil.getNestedType(pcheck, CVTYPE);
							argcheck= SemanticUtil.getNestedType(argcheck, CVTYPE);
						} else {
							pcheck= null;
						}
					}
				}
				if (pcheck instanceof ICPPTemplateInstance && argcheck instanceof ICPPClassType) {
					ICPPTemplateInstance pInst = (ICPPTemplateInstance) pcheck;
					ICPPClassTemplate pTemplate= getPrimaryTemplate(pInst);
					if (pTemplate != null) {
						ICPPClassType aInst= findBaseInstance((ICPPClassType) argcheck, pTemplate);	
						if (aInst != null && aInst != argcheck) {
							par= pcheck;
							arg= aInst;
						}
					}
				}
			}
		}
		
		return deduct.fromType(par, arg, true);
	}

	/**
	 * 14.8.2.2 [temp.deduct.funcaddr]
	 * Deducing template arguments taking the address of a function template 
	 * @throws DOMException 
	 */
	static ICPPTemplateArgument[] deduceForAddressOf(ICPPFunctionTemplate template,
			ICPPTemplateArgument[] tmplArgs, IFunctionType arg, CPPTemplateParameterMap map) throws DOMException {
		final ICPPTemplateParameter[] tmplParams = template.getTemplateParameters();
		if (!addExplicitArguments(tmplParams, tmplArgs, map))
			return null;
				
		IType par= template.getType();
		par= CPPTemplates.instantiateType(par, map, -1, null);
		if (!CPPTemplates.isValidType(par))
			return null;

		boolean isDependentPar= CPPTemplates.isDependentType(par);
		if (isDependentPar) {
			TemplateArgumentDeduction deduct= new TemplateArgumentDeduction(tmplParams, map, new CPPTemplateParameterMap(tmplParams.length), 0);
			par= SemanticUtil.getNestedType(par, SemanticUtil.TDEF); 
			if (arg != null && !deduct.fromType(par, arg, false))
				return null;
			if (!map.addDeducedArgs(deduct.fDeducedArgs))
				return null;
		}

		if (!verifyDeduction(tmplParams, map, true))
			return null;

		if (isDependentPar)
			par= CPPTemplates.instantiateType(par, map, -1, null);
		
		if (arg == null || arg.isSameType(par)) {
			return createArguments(map, tmplParams);
		}
		return null;
	}

	/**
	 * Deduce arguments for a user defined conversion template 
	 * 14.8.2.3
	 */
	static ICPPTemplateArgument[] deduceForConversion(ICPPFunctionTemplate template,
			IType conversionType, CPPTemplateParameterMap map) throws DOMException {
		final ICPPTemplateParameter[] tmplParams = template.getTemplateParameters();
		final int length = tmplParams.length;
		
		ICPPTemplateArgument[] result = new ICPPTemplateArgument[length];
		IType a= SemanticUtil.getSimplifiedType(conversionType);
		IType p= template.getType().getReturnType();
		p= getArgumentTypeForDeduction(p, a instanceof ICPPReferenceType);
		a= SemanticUtil.getNestedType(a, SemanticUtil.REF | SemanticUtil.TDEF);
		TemplateArgumentDeduction deduct= new TemplateArgumentDeduction(tmplParams, null, map, 0);
		if (!deduct.fromType(p, a, false)) {
			return null;
		}
		
		for (int i = 0; i < length; i++) {
			if (result[i] == null) {
				final ICPPTemplateParameter tpar = tmplParams[i];
				ICPPTemplateArgument deducedArg= map.getArgument(tpar);
				if (deducedArg == null) {
					deducedArg= tpar.getDefaultValue();
					if (deducedArg == null)
						return null;
				}			
				result[i]= deducedArg;
			}
		}
		return result;
	}

	/**
	 * Deduce arguments for a function declaration
	 * 14.8.2.6
	 */
	static ICPPTemplateArgument[] deduceForDeclaration(ICPPFunctionTemplate template,
			ICPPTemplateArgument[] args, ICPPFunctionType ftype, CPPTemplateParameterMap map) throws DOMException {
		final ICPPTemplateParameter[] tmplParams = template.getTemplateParameters();
		
		if (!addExplicitArguments(tmplParams, args, map))
			return null;

		IType a= SemanticUtil.getSimplifiedType(ftype);
		IType p= CPPTemplates.instantiateType(template.getType(), map, -1, null);
		if (!CPPTemplates.isValidType(p))
			return null;

		TemplateArgumentDeduction deduct= new TemplateArgumentDeduction(tmplParams, map, new CPPTemplateParameterMap(tmplParams.length), 0);
		if (!deduct.fromType(p, a, false)) {
			return null;
		}
		
		if (!map.addDeducedArgs(deduct.fDeducedArgs))
			return null;

		if (!verifyDeduction(tmplParams, map, true))
			return null;
		
		IType type= CPPTemplates.instantiateType(p, map, -1, null);
		if (!ftype.isSameType(type))
			return null;
		
		return createArguments(map, tmplParams);
	}

	/**
	 * Deduces the mapping for the template parameters from the function parameters,
	 * returns <code>false</code> if there is no mapping.
	 */
	static int deduceForPartialOrdering(ICPPTemplateParameter[] tmplPars, IType[] fnPars, IType[] fnArgs) {
		try {
			final int fnParCount = fnPars.length;
			final int fnArgCount = fnArgs.length;
			int result= 0;
			TemplateArgumentDeduction deduct= new TemplateArgumentDeduction(tmplPars, new CPPTemplateParameterMap(0), new CPPTemplateParameterMap(fnParCount), 0);
			IType fnParPack= null;
			for (int j= 0; j < fnArgCount; j++) {
				IType par;
				if (fnParPack != null) {
					par= fnParPack;
					deduct.incPackOffset();
				} else  {
					if (j >= fnParCount) 
						return result;
					
					par= fnPars[j];
					if (par instanceof ICPPParameterPackType) {
						if (j != fnParCount - 1) 
							continue; 	// non-deduced context
						
						par= fnParPack= ((ICPPParameterPackType) par).getType();
						deduct= new TemplateArgumentDeduction(deduct, fnArgs.length - j);
					} 
				} 
				
				IType arg = fnArgs[j];
				int cmpSpecialized= deduceForPartialOrdering(par, arg, deduct);
				if (cmpSpecialized < 0)
					return cmpSpecialized;
				if (cmpSpecialized > 0)
					result= cmpSpecialized;
			}
			return result;
		} catch (DOMException e) {
		}
		return -1;
	}
	
	private static int deduceForPartialOrdering(IType par, IType arg, TemplateArgumentDeduction deduct) throws DOMException {
		par= getNestedType(par, TDEF);
		arg= getNestedType(arg, TDEF);
		boolean isMoreCVQualified= false;
		if (par instanceof ICPPReferenceType && arg instanceof ICPPReferenceType) {
			par= getNestedType(par, REF | TDEF);
			arg= getNestedType(arg, REF | TDEF);
			CVQualifier cvp= getCVQualifier(par);
			CVQualifier cva= getCVQualifier(arg);
			isMoreCVQualified= cva.isMoreQualifiedThan(cvp);
		}
		par= getNestedType(par, TDEF | REF | ALLCVQ);
		arg= getNestedType(arg, TDEF | REF | ALLCVQ);
		
		if (!deduct.fromType(par, arg, false)) 
			return -1;
		
		return isMoreCVQualified ? 1 : 0;
	}

	/**
	 * Adds the explicit arguments to the map.
	 */
	private static boolean addExplicitArguments(final ICPPTemplateParameter[] tmplParams,
			ICPPTemplateArgument[] tmplArgs, CPPTemplateParameterMap map) {
		tmplArgs= SemanticUtil.getSimplifiedArguments(tmplArgs);
		ICPPTemplateParameter tmplParam= null;
		int packOffset= -1;
		for (int i = 0; i < tmplArgs.length; i++) {
			if (packOffset < 0 || tmplParam == null) {
				if (i >= tmplParams.length) 
					return false;
				
				tmplParam= tmplParams[i];
				if (tmplParam.isParameterPack()) {
					packOffset= i;
				}
			}
			ICPPTemplateArgument tmplArg= tmplArgs[i];
			tmplArg= CPPTemplates.matchTemplateParameterAndArgument(tmplParam, tmplArg, map);
			if (tmplArg == null)
				return false;
	
			if (packOffset < 0) {
				map.put(tmplParam, tmplArg);
			}
		}
		
		if (packOffset >= 0) {
			final int packSize= tmplArgs.length- packOffset;
			ICPPTemplateArgument[] pack= new ICPPTemplateArgument[packSize];
			System.arraycopy(tmplArgs, packOffset, pack, 0, packSize);
			map.put(tmplParam, pack);
		}
		return true;
	}

	private static ICPPTemplateArgument[] createArguments(CPPTemplateParameterMap map,
			final ICPPTemplateParameter[] tmplParams) {
		List<ICPPTemplateArgument> result= new ArrayList<ICPPTemplateArgument>(tmplParams.length);
		for (ICPPTemplateParameter tpar : tmplParams) {
			if (tpar.isParameterPack()) {
				ICPPTemplateArgument[] deducedArgs= map.getPackExpansion(tpar);
				if (deducedArgs == null) 
					return null;
				result.addAll(Arrays.asList(deducedArgs));
			} else {
				ICPPTemplateArgument deducedArg= map.getArgument(tpar);
				if (deducedArg == null) {
					return null;
				}			
	
				result.add(deducedArg);
			}
		}
		return result.toArray(new ICPPTemplateArgument[result.size()]);
	}

	
	/**
	 * 14.8.2.1.3 If P is a class and has the form template-id, then A can be a derived class of the deduced A.
	 */
	private static ICPPClassType findBaseInstance(ICPPClassType a, ICPPClassTemplate pTemplate) throws DOMException {
		return findBaseInstance(a, pTemplate, CPPSemantics.MAX_INHERITANCE_DEPTH, new HashSet<Object>());
	}

	private static ICPPClassType findBaseInstance(ICPPClassType a, ICPPClassTemplate pTemplate, int maxdepth, HashSet<Object> handled) throws DOMException {
		if (a instanceof ICPPTemplateInstance) {
			final ICPPTemplateInstance inst = (ICPPTemplateInstance) a;
			ICPPClassTemplate tmpl= getPrimaryTemplate(inst);
			if (pTemplate.isSameType(tmpl))
				return a;
		}
		if (maxdepth-- > 0) {
			for (ICPPBase cppBase : a.getBases()) {
				IBinding base= cppBase.getBaseClass();
				if (base instanceof ICPPClassType && handled.add(base)) {
					final ICPPClassType inst= findBaseInstance((ICPPClassType) base, pTemplate, maxdepth, handled);
					if (inst != null)
						return inst;
				}
			}
		}
		return null;
	}
	
	private static ICPPClassTemplate getPrimaryTemplate(ICPPTemplateInstance inst) throws DOMException {
		ICPPTemplateDefinition template= inst.getTemplateDefinition();
		if (template instanceof ICPPClassTemplatePartialSpecialization) {
			return ((ICPPClassTemplatePartialSpecialization) template).getPrimaryClassTemplate();
		} else if (template instanceof ICPPClassTemplate) {
			return (ICPPClassTemplate) template;
		}	
		return null;
	}
	
	/**
	 * 14.8.2.1-2
	 * if P is not a reference type
	 * - If A is an array type, the pointer type produced by the array-to-pointer conversion is used instead
	 * - If A is a function type, the pointer type produced by the function-to-pointer conversion is used instead
	 * - If A is a cv-qualified type, the top level cv-qualifiers are ignored for type deduction
	 * 
	 * 	 Also 14.8.2.3-2 where the same logics is used in reverse.
	 */
	private static IType getArgumentTypeForDeduction(IType type, boolean parameterIsAReferenceType) {
		type = SemanticUtil.getSimplifiedType(type);
		if (type instanceof ICPPReferenceType) {
		    type = ((ICPPReferenceType) type).getType();
		}
		IType result = type;
		if (!parameterIsAReferenceType) {
			if (type instanceof IArrayType) {
				result = new CPPPointerType(((IArrayType) type).getType());
			} else if (type instanceof IFunctionType) {
				result = new CPPPointerType(type);
			} else {
				result = SemanticUtil.getNestedType(type, TDEF | ALLCVQ );
			}
		}
		return result;
	}

	/**
	 * Deduces the template parameter mapping from pairs of template arguments.
	 */
	public static boolean fromTemplateArguments(final ICPPTemplateParameter[] pars, final ICPPTemplateArgument[] p, final ICPPTemplateArgument[] a, CPPTemplateParameterMap map) throws DOMException {
		TemplateArgumentDeduction deduct= new TemplateArgumentDeduction(pars, null, map, 0);
		final int len= a.length;
		if (p == null || p.length != len) {
			return false;
		}
		for (int j= 0; j < len; j++) {
			if (!deduct.fromTemplateArgument(p[j], a[j])) {
				return false;
			}
		}
		return verifyDeduction(pars, map, false);
	}

	private static boolean verifyDeduction(ICPPTemplateParameter[] pars, CPPTemplateParameterMap tpMap, boolean useDefaults) {
		for (ICPPTemplateParameter tpar : pars) {
			if (tpar.isParameterPack()) {
				ICPPTemplateArgument[] deducedArgs= tpMap.getPackExpansion(tpar);
				if (deducedArgs == null) {
					tpMap.put(tpar, ICPPTemplateArgument.EMPTY_ARGUMENTS);
				} else {
					for (ICPPTemplateArgument arg : deducedArgs) {
						if (arg == null)
							return false;
					}
				}
			} else {
				ICPPTemplateArgument deducedArg= tpMap.getArgument(tpar);
				if (deducedArg == null && useDefaults) {
					deducedArg= tpar.getDefaultValue();
					if (deducedArg != null) {
						deducedArg= CPPTemplates.instantiateArgument(deducedArg, tpMap, -1, null);
						if (deducedArg != null) {
							tpMap.put(tpar, deducedArg);
						}
					}
				}
				if (deducedArg == null) 
					return false;
			}
		}
		return true;
	}


	private CPPTemplateParameterMap fExplicitArgs;
	private CPPTemplateParameterMap fDeducedArgs;
	private Set<Integer> fTemplateParameterPacks;
	private int fPackOffset;
	private final int fPackSize;
	
	private TemplateArgumentDeduction(ICPPTemplateParameter[] tpars, CPPTemplateParameterMap explicit, CPPTemplateParameterMap result, int packSize) {
		fExplicitArgs= explicit;
		fDeducedArgs= result;
		fPackSize= packSize;
		fPackOffset= packSize > 0 ? 0 : -1;
		for (ICPPTemplateParameter tpar : tpars) {
			if (tpar.isParameterPack()) {
				if (fTemplateParameterPacks == null) {
					fTemplateParameterPacks= new HashSet<Integer>();
				}
				fTemplateParameterPacks.add(tpar.getParameterID());
			}
		}
	}

	private TemplateArgumentDeduction(TemplateArgumentDeduction base, int packSize) {
		fExplicitArgs= base.fExplicitArgs;
		fDeducedArgs= base.fDeducedArgs;
		fTemplateParameterPacks= base.fTemplateParameterPacks;
		fPackSize= packSize;
		fPackOffset= packSize > 0 ? 0 : -1;
	}
	
	private CPPTemplateParameterMap saveState() {
		return new CPPTemplateParameterMap(fDeducedArgs);
	}

	private void restoreState(CPPTemplateParameterMap saved) {
		fDeducedArgs= saved;
	}

	private void incPackOffset() {
		fPackOffset++;
		assert fPackOffset < fPackSize;
	}

	/**
	 * Deduces the template parameter mapping from one pair of template arguments.
	 */
	private boolean fromTemplateArgument(ICPPTemplateArgument p, ICPPTemplateArgument a) throws DOMException {
		if (p.isNonTypeValue() != a.isNonTypeValue()) 
			return false;
		
		if (p.isNonTypeValue()) {
			IValue tval= p.getNonTypeValue();

			if (Value.referencesTemplateParameter(tval)) {
				int parId= Value.isTemplateParameter(tval);
				if (parId >= 0) { 
					if (!p.getTypeOfNonTypeValue().isSameType(a.getTypeOfNonTypeValue()))
						return false;
					ICPPTemplateArgument old= fDeducedArgs.getArgument(parId, fPackOffset);
					if (old == null) {
						return deduce(parId, a);
					}
					return old.isSameValue(a);
				} else {
					// Non-deduced context
					return true;
				}
			}
			IValue sval= a.getNonTypeValue();
			return tval.equals(sval); 
		} 
		
		return fromType(p.getTypeValue(), a.getTypeValue(), false);
	}


	private boolean fromType(IType p, IType a, boolean allowCVQConversion) throws DOMException {
		while (p != null) {
			while (a instanceof ITypedef)
				a = ((ITypedef) a).getType();
			if (p instanceof IBasicType) {
				return p.isSameType(a);
			} else if (p instanceof ICPPPointerToMemberType) {
				if (!(a instanceof ICPPPointerToMemberType))
					return false;
				if (!fromType(((ICPPPointerToMemberType) p).getMemberOfClass(),
						((ICPPPointerToMemberType) a).getMemberOfClass(), false)) {
					return false;
				}
				p = ((ICPPPointerToMemberType) p).getType();
				a = ((ICPPPointerToMemberType) a).getType();
			} else if (p instanceof IPointerType) {
				if (!(a instanceof IPointerType)) {
					return false;
				}
				p = ((IPointerType) p).getType();
				a = ((IPointerType) a).getType();
			} else if (p instanceof ICPPReferenceType) {
				if (!(a instanceof ICPPReferenceType)) {
					return false;
				}
				final ICPPReferenceType rp = (ICPPReferenceType) p;
				final ICPPReferenceType ra = (ICPPReferenceType) a;
				if (ra.isRValueReference() != rp.isRValueReference())
					return false;
				p = rp.getType();
				a = ra.getType();
			} else if (p instanceof IArrayType) {
				if (!(a instanceof IArrayType)) {
					return false;
				}
				IArrayType aa= (IArrayType) a;
				IArrayType pa= (IArrayType) p;
				IValue as= aa.getSize();
				IValue ps= pa.getSize();
				if (as != ps) {
					if (as == null || ps == null)
						return false;
					
					int parID= Value.isTemplateParameter(ps);
					if (parID >= 0) { 
						ICPPTemplateArgument old= fDeducedArgs.getArgument(parID, fPackOffset);
						if (old == null) {
							if (!deduce(parID, new CPPTemplateArgument(as, new CPPBasicType(ICPPBasicType.Kind.eInt, 0)))) {
								return false;
							} 
						} else if (!as.equals(old.getNonTypeValue())) {
							return false;
						}
					} else if (!as.equals(as)) {
						return false;
					}
				}
				p = pa.getType();
				a = aa.getType();
			} else if (p instanceof IQualifierType) {
				final CVQualifier cvqP = SemanticUtil.getCVQualifier(p);
				final CVQualifier cvqA = SemanticUtil.getCVQualifier(a);
				CVQualifier remaining= CVQualifier.NONE;
				if (cvqP != cvqA) {
					if (!allowCVQConversion && !cvqA.isAtLeastAsQualifiedAs(cvqP))
						return false;
					remaining= cvqA.remove(cvqP);
				}
				p = SemanticUtil.getNestedType(p, ALLCVQ); 
				a = SemanticUtil.getNestedType(a, ALLCVQ);
				if (p instanceof IQualifierType)
					return false;
				if (remaining != CVQualifier.NONE) {
					a= SemanticUtil.addQualifiers(a, remaining.isConst(), remaining.isVolatile(), remaining.isRestrict());
				}
			} else if (p instanceof IFunctionType) {
				if (!(a instanceof IFunctionType))
					return false;
				return fromFunctionType((IFunctionType) p, (IFunctionType) a);
			} else if (p instanceof ICPPTemplateParameter) {
				ICPPTemplateArgument current= fDeducedArgs.getArgument(((ICPPTemplateParameter) p).getParameterID(), fPackOffset);
				if (current != null) {
					if (current.isNonTypeValue())
						return false;
					return current.getTypeValue().isSameType(a); 
				}
				if (a == null)
					return false;
				return deduce(((ICPPTemplateParameter)p).getParameterID(), new CPPTemplateArgument(a));
			} else if (p instanceof ICPPTemplateInstance) {
				if (!(a instanceof ICPPTemplateInstance))
					return false;
				return fromTemplateInstance((ICPPTemplateInstance) p, (ICPPTemplateInstance) a);
			} else if (p instanceof ICPPUnknownBinding) {
				return true;  // An unknown type may match anything.
			} else {
				return p.isSameType(a);
			}
		}

		return false;
	}

	private boolean fromTemplateInstance(ICPPTemplateInstance pInst, ICPPTemplateInstance aInst)
			throws DOMException {
		ICPPClassTemplate pTemplate= getPrimaryTemplate(pInst);
		ICPPClassTemplate aTemplate= getPrimaryTemplate(aInst);
		if (pTemplate == null || aTemplate == null)
			return false;
		
		if (pTemplate instanceof ICPPTemplateTemplateParameter) {
			final int tparId = ((ICPPTemplateTemplateParameter) pTemplate).getParameterID();
			ICPPTemplateArgument current= fDeducedArgs.getArgument(tparId, fPackOffset);
			if (current != null) {
				if (current.isNonTypeValue() || !current.getTypeValue().isSameType(aTemplate))
					return false;
			} else if (!deduce(tparId, new CPPTemplateArgument(aTemplate))) {
				return false;
			}
		} else if (!aTemplate.isSameType(pTemplate)) {
			return false;
		}
		
		// Check for being a non-deduced context.
		final ICPPTemplateArgument[] pArgs = pInst.getTemplateArguments();
		for (int i = 0; i < pArgs.length - 1; i++) {
			if (pArgs[i].isPackExpansion()) 
				return true; // non-deduced context
		}
		
		final ICPPTemplateArgument[] aArgs = aInst.getTemplateArguments();
		if (pArgs.length != aArgs.length) {
			if (pArgs.length == 0 || pArgs.length > aArgs.length + 1)
				return false;
			ICPPTemplateArgument lastPParam= pArgs[pArgs.length - 1];
			if (!lastPParam.isPackExpansion())
				return false;
		}

		ICPPTemplateArgument expansionPattern= null;
		TemplateArgumentDeduction deduct= this;
		for (int i = 0; i < aArgs.length; i++) {
			ICPPTemplateArgument p;
			if (expansionPattern != null) {
				p= expansionPattern;
				deduct.incPackOffset();
				p= CPPTemplates.instantiateArgument(p, fExplicitArgs, deduct.fPackOffset, null);
				if (!CPPTemplates.isValidArgument(p))
					return false;
			} else {
				p= pArgs[i];
				if (p.isPackExpansion()) {
					p= expansionPattern= p.getExpansionPattern();
					deduct= new TemplateArgumentDeduction(this, aArgs.length-i);
					p= CPPTemplates.instantiateArgument(p, fExplicitArgs, deduct.fPackOffset, null);
					if (!CPPTemplates.isValidArgument(p))
						return false;
				}
			}
			if (!deduct.fromTemplateArgument(p, aArgs[i]))
				return false;
		}
		return true;
	}

	private boolean fromFunctionType(IFunctionType ftp, IFunctionType fta) throws DOMException {
		if (!fromType(ftp.getReturnType(), fta.getReturnType(), false)) 
			return false;
		
		IType[] pParams = ftp.getParameterTypes();
		IType[] aParams = fta.getParameterTypes();
		if (pParams.length != aParams.length) {
			if (SemanticUtil.isEmptyParameterList(pParams) && SemanticUtil.isEmptyParameterList(aParams))
				return true;
			if (pParams.length == 0 || pParams.length > aParams.length + 1)
				return false;
			IType lastPParam= pParams[pParams.length - 1];
			if (!(lastPParam instanceof ICPPParameterPackType))
				return false;
		}
		IType parameterPack= null;
		TemplateArgumentDeduction deduct= this;
		for (int i = 0; i < aParams.length; i++) {
			IType p;
			if (parameterPack != null) {
				p= parameterPack;
				deduct.incPackOffset();
				p= CPPTemplates.instantiateType(p, fExplicitArgs, deduct.fPackOffset, null);
				if (!CPPTemplates.isValidType(p))
					return false;
			} else {
				p= pParams[i];
				if (p instanceof ICPPParameterPackType) {
					p= parameterPack= ((ICPPParameterPackType) p).getType();
					deduct= new TemplateArgumentDeduction(this, aParams.length - i);
					p= CPPTemplates.instantiateType(p, fExplicitArgs, deduct.fPackOffset, null);
					if (!CPPTemplates.isValidType(p))
						return false;
				}
			}
			if (!deduct.fromType(p, aParams[i], false))
				return false;
		}
		return true;
	}

	private boolean deduce(int parID, ICPPTemplateArgument arg) {
		if (fTemplateParameterPacks != null && fTemplateParameterPacks.contains(parID)) {
			if (fPackSize == 0)
				return false;
			return fDeducedArgs.putPackElement(parID, fPackOffset, arg, fPackSize);
		}
		if (SemanticUtil.containsUniqueTypeForParameterPack(arg.getTypeValue()))
			return false;
		fDeducedArgs.put(parID, arg);
		return true;
	}
}
