/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTBuiltinSymbolProvider;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTPointer;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CBasicType;
import org.eclipse.cdt.internal.core.dom.parser.c.CFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.c.CImplicitFunction;
import org.eclipse.cdt.internal.core.dom.parser.c.CImplicitTypedef;
import org.eclipse.cdt.internal.core.dom.parser.c.CPointerType;
import org.eclipse.cdt.internal.core.dom.parser.c.CQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitTypedef;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GPPBasicType;

/**
 * This is the IASTBuiltinSymbolProvider used to implement the "Other" built-in GCC symbols defined:
 * http://gcc.gnu.org/onlinedocs/gcc/Other-Builtins.html#Other-Builtins
 * 
 * @author dsteffle
 */
public class GCCBuiltinSymbolProvider implements IASTBuiltinSymbolProvider {
	/**
	 * <code>BUILTIN_GCC_SYMBOL</code> is a built-in GCC symbol.
	 */
	public static final ASTNodeProperty BUILTIN_GCC_SYMBOL = new ASTNodeProperty(
		"GCCBuiltinSymbolProvider.BUILTIN_GCC_SYMBOL - built-in GCC symbol"); //$NON-NLS-1$
	
	private static final char[] __BUILTIN_VA_LIST = "__builtin_va_list".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_EXPECT  = "__builtin_expect".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_PREFETCH  = "__builtin_prefetch".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_HUGE_VAL  = "__builtin_huge_val".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_HUGE_VALF = "__builtin_huge_valf".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_HUGE_VALL = "__builtin_huge_vall".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_INF  = "__builtin_inf".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_INFF = "__builtin_inff".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_INFL = "__builtin_infl".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_NAN  = "__builtin_nan".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_NANF  = "__builtin_nanf".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_NANL  = "__builtin_nanl".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_NANS  = "__builtin_nans".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_NANSF  = "__builtin_nansf".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_NANSL  = "__builtin_nansl".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_FFS    = "__builtin_ffs".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_CLZ    = "__builtin_clz".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_CTZ    = "__builtin_ctz".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_POPCOUNT = "__builtin_popcount".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_PARITY = "__builtin_parity".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_FFSL   = "__builtin_ffsl".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_CLZL   = "__builtin_clzl".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_CTZL   = "__builtin_ctzl".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_POPCOUNTL = "__builtin_popcountl".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_PARITYL   = "__builtin_parityl".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_FFSLL   = "__builtin_ffsll".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_CLZLL   = "__builtin_clzll".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_CTZLL   = "__builtin_ctzll".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_POPCOUNTLL = "__builtin_popcountll".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_PARITYLL   = "__builtin_parityll".toCharArray(); //$NON-NLS-1$	
	private static final char[] __BUILTIN_TYPES_COMPATIBLE_P = "__builtin_types_compatible_p".toCharArray(); //$NON-NLS-1$
	private static final char[] __BUILTIN_POWI   = "__builtin_powi".toCharArray(); //$NON-NLS-1$	
	private static final char[] __BUILTIN_POWIF   = "__builtin_powif".toCharArray(); //$NON-NLS-1$	
	private static final char[] __BUILTIN_POWIL   = "__builtin_powil".toCharArray(); //$NON-NLS-1$	
	private static final int NUM_OTHER_GCC_BUILTINS = 34; // the total number of builtin functions listed above
	
	private IBinding[] bindings=new IBinding[NUM_OTHER_GCC_BUILTINS];
	private IScope scope=null;
	private ParserLanguage lang=null;
	public GCCBuiltinSymbolProvider(IScope scope, ParserLanguage lang) {
		this.scope = scope;
		this.lang = lang;
	}
	
	public void initialize() {
		__builtin_va_list();
		__builtin_expect();
        __builtin_prefetch();
        __builtin_huge_val();
        __builtin_inf();
        __builtin_nan();
        __builtin_unsigned_int();
        __builtin_unsigned_long();
        __builtin_unsigned_long_long();
        __builtin_types_compatible_p();
		__builtin_powi();
	}
	
	private void __builtin_va_list() {
		// char * __builtin_va_list();
		IBinding temp = null;
		
        if (lang == ParserLanguage.C) {
            ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
            sds.setType(IASTSimpleDeclSpecifier.t_char);
            IType type = new CBasicType(sds);
            CPointerType returnType = new CPointerType(type);
            returnType.setPointer(new CASTPointer());
            IFunctionType functionType = null;
            functionType = new CFunctionType(returnType, new IType[0]);
            temp = new CImplicitTypedef(functionType, __BUILTIN_VA_LIST, scope);
        } else {
            IType type = new CPPBasicType( IBasicType.t_char, 0 );
            IType returnType = new CPPPointerType(type);
            IFunctionType functionType = null;
            functionType = new CPPFunctionType(returnType, new IType[0]);
            temp = new CPPImplicitTypedef(functionType, __BUILTIN_VA_LIST, scope);
        }
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
	}
	
	private void __builtin_expect() {
        //long __builtin_expect( long exp, long c )
		IBinding temp = null;
		
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setLong(true);
			IType returnType = new CBasicType(sds);
			IFunctionType functionType = null;
			IType[] parms = new IType[2];
			parms[0] = new CBasicType(sds);
			parms[1] = parms[0];
			functionType = new CFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new BuiltinParameter(parms[0]);
			theParms[1] = theParms[0];
			temp = new CImplicitFunction(__BUILTIN_EXPECT, scope, functionType, theParms, false);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_unspecified, CPPBasicType.IS_LONG );
			IFunctionType functionType = null;
			IType[] parms = new IType[2];
			parms[0] = returnType;
			parms[1] = parms[0];
			functionType = new CPPFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new BuiltinParameter(parms[0]);
			theParms[1] = theParms[0];
			temp = new CPPImplicitFunction(__BUILTIN_EXPECT, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
	}
	
	private void __builtin_prefetch() {
		// void __builtin_prefetch (const void *addr, ...)
		IBinding temp = null;
		
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setType(IASTSimpleDeclSpecifier.t_void);
			IType returnType = new CBasicType(sds);
			ICASTSimpleDeclSpecifier parmSds = new CASTSimpleDeclSpecifier();
			parmSds.setType(IASTSimpleDeclSpecifier.t_void);
			parmSds.setConst(true);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = new CPointerType(new CQualifierType(parmSds));
			((CPointerType)parms[0]).setPointer(new CASTPointer());
			functionType = new CFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_PREFETCH, scope, functionType, theParms, true);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_void, 0 );
			IType parmType = new CPPPointerType( new CPPQualifierType(new CPPBasicType(IBasicType.t_void, 0), true, false) );
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = parmType;
			functionType = new CPPFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_PREFETCH, scope, functionType, theParms, true);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
	}
	
	private void __builtin_huge_val() {
        //double __builtin_huge_val (void)
		IBinding temp = null;
		
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setType(IASTSimpleDeclSpecifier.t_double);
			IType returnType = new CBasicType(sds);
			ICASTSimpleDeclSpecifier parmSds = new CASTSimpleDeclSpecifier();
			parmSds.setType(IASTSimpleDeclSpecifier.t_void);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = new CPointerType(new CQualifierType(parmSds));
			functionType = new CFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_HUGE_VAL, scope, functionType, theParms, false);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_double, 0 );
			IType parmType = new CPPBasicType(IBasicType.t_void, 0);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = parmType;
			functionType = new CPPFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_HUGE_VAL, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
        //float __builtin_huge_valf (void)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setType(IASTSimpleDeclSpecifier.t_float);
			IType returnType = new CBasicType(sds);
			ICASTSimpleDeclSpecifier parmSds = new CASTSimpleDeclSpecifier();
			parmSds.setType(IASTSimpleDeclSpecifier.t_void);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = new CPointerType(new CQualifierType(parmSds));
			functionType = new CFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_HUGE_VALF, scope, functionType, theParms, false);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_float, 0 );
			IType parmType = new CPPBasicType(IBasicType.t_void, 0);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = parmType;
			functionType = new CPPFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_HUGE_VALF, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
        //long double __builtin_huge_vall (void)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setType(IASTSimpleDeclSpecifier.t_double);
			sds.setLong(true);
			IType returnType = new CBasicType(sds);
			ICASTSimpleDeclSpecifier parmSds = new CASTSimpleDeclSpecifier();
			parmSds.setType(IASTSimpleDeclSpecifier.t_void);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = new CPointerType(new CQualifierType(parmSds));
			functionType = new CFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_HUGE_VALL, scope, functionType, theParms, false);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_double, CPPBasicType.IS_LONG );
			IType parmType = new CPPBasicType(IBasicType.t_void, 0);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = parmType;
			functionType = new CPPFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_HUGE_VALL, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
	}
	
	private void __builtin_inf() {
        //double __builtin_inf (void)
		IBinding temp = null;
		
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setType(IASTSimpleDeclSpecifier.t_double);
			IType returnType = new CBasicType(sds);
			ICASTSimpleDeclSpecifier parmSds = new CASTSimpleDeclSpecifier();
			parmSds.setType(IASTSimpleDeclSpecifier.t_void);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = new CPointerType(new CQualifierType(parmSds));
			functionType = new CFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_INF, scope, functionType, theParms, false);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_double, 0 );
			IType parmType = new CPPBasicType(IBasicType.t_void, 0);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = parmType;
			functionType = new CPPFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_INF, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
        //float __builtin_inff (void)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setType(IASTSimpleDeclSpecifier.t_float);
			IType returnType = new CBasicType(sds);
			ICASTSimpleDeclSpecifier parmSds = new CASTSimpleDeclSpecifier();
			parmSds.setType(IASTSimpleDeclSpecifier.t_void);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = new CPointerType(new CQualifierType(parmSds));
			functionType = new CFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_INFF, scope, functionType, theParms, false);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_float, 0 );
			IType parmType = new CPPBasicType(IBasicType.t_void, 0);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = parmType;
			functionType = new CPPFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_INFF, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
        //long double __builtin_infl (void)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setType(IASTSimpleDeclSpecifier.t_double);
			sds.setLong(true);
			IType returnType = new CBasicType(sds);
			ICASTSimpleDeclSpecifier parmSds = new CASTSimpleDeclSpecifier();
			parmSds.setType(IASTSimpleDeclSpecifier.t_void);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = new CPointerType(new CQualifierType(parmSds));
			functionType = new CFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_INFL, scope, functionType, theParms, false);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_double, CPPBasicType.IS_LONG );
			IType parmType = new CPPBasicType(IBasicType.t_void, 0);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = parmType;
			functionType = new CPPFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_INFL, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
	}
	
	private void __builtin_nan() {
		// const char *
		ICASTSimpleDeclSpecifier parmSds = new CASTSimpleDeclSpecifier();
		parmSds.setType(IASTSimpleDeclSpecifier.t_char);
		parmSds.setConst(true);
		CPointerType c_const_char_p = new CPointerType(new CQualifierType(parmSds));
		c_const_char_p.setPointer(new CASTPointer());
		IType cpp_const_char_p = new CPPPointerType(new CPPQualifierType(new CPPBasicType(IBasicType.t_char, 0), true, false));

		//double __builtin_nan (const char * str)
		IBinding temp = null;
		
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setType(IASTSimpleDeclSpecifier.t_double);
			IType returnType = new CBasicType(sds);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_const_char_p;
			functionType = new CFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_NAN, scope, functionType, theParms, false);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_double, 0 );
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_const_char_p;
			functionType = new CPPFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_NAN, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//float __builtin_nanf (const char * str)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setType(IASTSimpleDeclSpecifier.t_float);
			IType returnType = new CBasicType(sds);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_const_char_p;
			functionType = new CFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_NANF, scope, functionType, theParms, false);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_float, 0 );
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_const_char_p;
			functionType = new CPPFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_NANF, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//long double __builtin_nanl (const char * str)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setType(IASTSimpleDeclSpecifier.t_double);
			sds.setLong(true);
			IType returnType = new CBasicType(sds);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_const_char_p;
			functionType = new CFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_NANL, scope, functionType, theParms, false);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_double, CPPBasicType.IS_LONG );
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_const_char_p;
			functionType = new CPPFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_NANL, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//double __builtin_nans (const char * str)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setType(IASTSimpleDeclSpecifier.t_double);
			IType returnType = new CBasicType(sds);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_const_char_p;
			functionType = new CFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_NANS, scope, functionType, theParms, false);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_double, 0 );
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_const_char_p;
			functionType = new CPPFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_NANS, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//float __builtin_nansf (const char * str)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setType(IASTSimpleDeclSpecifier.t_float);
			IType returnType = new CBasicType(sds);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_const_char_p;
			functionType = new CFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_NANSF, scope, functionType, theParms, false);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_float, 0 );
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_const_char_p;
			functionType = new CPPFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_NANSF, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//long double __builtin_nansl (const char * str)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setType(IASTSimpleDeclSpecifier.t_double);
			sds.setLong(true);
			IType returnType = new CBasicType(sds);
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_const_char_p;
			functionType = new CFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_NANSL, scope, functionType, theParms, false);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_double, CPPBasicType.IS_LONG );
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_const_char_p;
			functionType = new CPPFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_NANSL, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
	}
	
	private void __builtin_unsigned_int() {
		//unsigned int
		ICASTSimpleDeclSpecifier parmSds = new CASTSimpleDeclSpecifier();
		parmSds.setType(IASTSimpleDeclSpecifier.t_int);
		parmSds.setUnsigned(true);
		IType c_unsigned_int = new CQualifierType(parmSds);
		IType cpp_unsigned_int = new CPPBasicType(IBasicType.t_int, CPPBasicType.IS_UNSIGNED);
		
		//int __builtin_ffs(unsigned int x)
		IBinding temp = null;

		IFunctionType functionType = null;
		IParameter[] theParms = new IParameter[1];
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setType(IASTSimpleDeclSpecifier.t_int);
			IType returnType = new CBasicType(sds);
			IType[] parms = new IType[1];
			parms[0] = c_unsigned_int;
			functionType = new CFunctionType(returnType, parms);
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_FFS, scope, functionType, theParms, false);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_int, 0 );
			IType[] parms = new IType[1];
			parms[0] = cpp_unsigned_int;
			functionType = new CPPFunctionType(returnType, parms);
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_FFS, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//int __builtin_clz(unsigned int x)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			temp = new CImplicitFunction(__BUILTIN_CLZ, scope, functionType, theParms, false);
		} else {
			temp = new CPPImplicitFunction(__BUILTIN_CLZ, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//int __builtin_ctz(unsigned int x)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			temp = new CImplicitFunction(__BUILTIN_CTZ, scope, functionType, theParms, false);
		} else {
			temp = new CPPImplicitFunction(__BUILTIN_CTZ, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//int __builtin_popcount(unsigned int x)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			temp = new CImplicitFunction(__BUILTIN_POPCOUNT, scope, functionType, theParms, false);
		} else {
			temp = new CPPImplicitFunction(__BUILTIN_POPCOUNT, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//int __builtin_parity(unsigned int x)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			temp = new CImplicitFunction(__BUILTIN_PARITY, scope, functionType, theParms, false);
		} else {
			temp = new CPPImplicitFunction(__BUILTIN_PARITY, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
	}
	
	private void __builtin_unsigned_long() {
        //unsigned long
		ICASTSimpleDeclSpecifier parmSds = new CASTSimpleDeclSpecifier();
		parmSds.setType(IASTSimpleDeclSpecifier.t_unspecified);
		parmSds.setUnsigned(true);
		parmSds.setLong(true);
		IType c_unsigned_long = new CQualifierType(parmSds);
		IType cpp_unsigned_long = new CPPBasicType(IBasicType.t_unspecified, CPPBasicType.IS_UNSIGNED & CPPBasicType.IS_LONG);
		
		//int __builtin_ffsl(unsigned int x)
		IBinding temp = null;

		IFunctionType functionType = null;
		IParameter[] theParms = new IParameter[1];
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setType(IASTSimpleDeclSpecifier.t_int);
			IType returnType = new CBasicType(sds);
			IType[] parms = new IType[1];
			parms[0] = c_unsigned_long;
			functionType = new CFunctionType(returnType, parms);
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_FFSL, scope, functionType, theParms, false);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_int, 0 );
			IType[] parms = new IType[1];
			parms[0] = cpp_unsigned_long;
			functionType = new CPPFunctionType(returnType, parms);
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_FFSL, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//int __builtin_clzl(unsigned int x)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			temp = new CImplicitFunction(__BUILTIN_CLZL, scope, functionType, theParms, false);
		} else {
			temp = new CPPImplicitFunction(__BUILTIN_CLZL, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//int __builtin_ctzl(unsigned int x)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			temp = new CImplicitFunction(__BUILTIN_CTZL, scope, functionType, theParms, false);
		} else {
			temp = new CPPImplicitFunction(__BUILTIN_CTZL, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//int __builtin_popcountl(unsigned int x)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			temp = new CImplicitFunction(__BUILTIN_POPCOUNTL, scope, functionType, theParms, false);
		} else {
			temp = new CPPImplicitFunction(__BUILTIN_POPCOUNTL, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//int __builtin_parityl(unsigned int x)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			temp = new CImplicitFunction(__BUILTIN_PARITYL, scope, functionType, theParms, false);
		} else {
			temp = new CPPImplicitFunction(__BUILTIN_PARITYL, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
	}
	
	private void __builtin_unsigned_long_long() {
        //unsigned long long
		ICASTSimpleDeclSpecifier parmSds = new CASTSimpleDeclSpecifier();
		parmSds.setType(IASTSimpleDeclSpecifier.t_unspecified);
		parmSds.setUnsigned(true);
		parmSds.setLongLong(true);
		IType c_unsigned_long_long = new CQualifierType(parmSds);
		IType cpp_unsigned_long_long = new GPPBasicType(IBasicType.t_unspecified, CPPBasicType.IS_UNSIGNED & GPPBasicType.IS_LONGLONG, null);
		
		//int __builtin_ffsll(unsigned long long x)
		IBinding temp = null;

		IFunctionType functionType = null;
		IParameter[] theParms = new IParameter[1];
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setType(IASTSimpleDeclSpecifier.t_int);
			IType returnType = new CBasicType(sds);
			IType[] parms = new IType[1];
			parms[0] = c_unsigned_long_long;
			functionType = new CFunctionType(returnType, parms);
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_FFSLL, scope, functionType, theParms, false);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_int, 0 );
			IType[] parms = new IType[1];
			parms[0] = cpp_unsigned_long_long;
			functionType = new CPPFunctionType(returnType, parms);
			theParms[0] = new BuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_FFSLL, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//int __builtin_clzll(unsigned long long x)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			temp = new CImplicitFunction(__BUILTIN_CLZLL, scope, functionType, theParms, false);
		} else {
			temp = new CPPImplicitFunction(__BUILTIN_CLZLL, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//int __builtin_ctzll(unsigned long long x)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			temp = new CImplicitFunction(__BUILTIN_CTZLL, scope, functionType, theParms, false);
		} else {
			temp = new CPPImplicitFunction(__BUILTIN_CTZLL, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//int __builtin_popcountll(unsigned long long x)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			temp = new CImplicitFunction(__BUILTIN_POPCOUNTLL, scope, functionType, theParms, false);
		} else {
			temp = new CPPImplicitFunction(__BUILTIN_POPCOUNTLL, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//int __builtin_parityll(unsigned long long x)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			temp = new CImplicitFunction(__BUILTIN_PARITYLL, scope, functionType, theParms, false);
		} else {
			temp = new CPPImplicitFunction(__BUILTIN_PARITYLL, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);

	}
	
	private void __builtin_types_compatible_p() {
		// int __builtin_types_compatible_p( type1, type2 ) implemented via ( ... )
		IBinding temp = null;
		
		if (lang == ParserLanguage.C) {
			ICASTSimpleDeclSpecifier sds = new CASTSimpleDeclSpecifier();
			sds.setType(IASTSimpleDeclSpecifier.t_int);
			IType returnType = new CBasicType(sds);
			ICASTSimpleDeclSpecifier parmSds = new CASTSimpleDeclSpecifier();
			parmSds.setType(IASTSimpleDeclSpecifier.t_unspecified);
			IFunctionType functionType = null;
			IType[] parms = new IType[2];
			parms[0] = new CPointerType(new CQualifierType(parmSds));
			parms[1] = parms[0];
			functionType = new CFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new BuiltinParameter(parms[0]);
			theParms[1] = theParms[0];
			temp = new CImplicitFunction(__BUILTIN_TYPES_COMPATIBLE_P, scope, functionType, theParms, true);
		} else {
			IType returnType = new CPPBasicType( IBasicType.t_int, 0 );
			IType parmType = new CPPBasicType(IBasicType.t_unspecified, 0);
			IFunctionType functionType = null;
			IType[] parms = new IType[2];
			parms[0] = parmType;
			parms[1] = parms[0];
			functionType = new CPPFunctionType(returnType, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new BuiltinParameter(parms[0]);
			theParms[1] = theParms[0];
			temp = new CPPImplicitFunction(__BUILTIN_TYPES_COMPATIBLE_P, scope, functionType, theParms, true);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
	}
	
	private void __builtin_powi() {
		// double and int
		ICASTSimpleDeclSpecifier doubleSds = new CASTSimpleDeclSpecifier();
		doubleSds.setType(IASTSimpleDeclSpecifier.t_double);
		ICASTSimpleDeclSpecifier intSds = new CASTSimpleDeclSpecifier();
		intSds.setType(IASTSimpleDeclSpecifier.t_int);
		ICASTSimpleDeclSpecifier floatSds = new CASTSimpleDeclSpecifier();
		floatSds.setType(IASTSimpleDeclSpecifier.t_float);
		ICASTSimpleDeclSpecifier longDoubleSds = new CASTSimpleDeclSpecifier();
		longDoubleSds.setType(IASTSimpleDeclSpecifier.t_double);
		longDoubleSds.setLong(true);
		IType c_double = new CBasicType(doubleSds);
		IType cpp_double = new CPPBasicType(IBasicType.t_double, 0);
		IType c_int = new CBasicType(intSds);
		IType cpp_int = new CPPBasicType(IBasicType.t_int, 0);
		IType c_float = new CBasicType(floatSds);
		IType cpp_float = new CPPBasicType(IBasicType.t_float, 0);
		IType c_long_double = new CBasicType(longDoubleSds);
		IType cpp_long_double = new CPPBasicType(IBasicType.t_double, CPPBasicType.IS_LONG);

		// double __builtin_powi (double, int)
		IBinding temp = null;
		
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[2];
			parms[0] = c_double;
			parms[1] = c_int;
			functionType = new CFunctionType(c_double, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new BuiltinParameter(parms[0]);
			theParms[1] = new BuiltinParameter(parms[1]);
			temp = new CImplicitFunction(__BUILTIN_POWI, scope, functionType, theParms, false);
		} else {
			IType[] parms = new IType[2];
			parms[0] = cpp_double;
			parms[1] = cpp_int;
			IFunctionType functionType = new CPPFunctionType(cpp_double, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new BuiltinParameter(parms[0]);
			theParms[1] = new BuiltinParameter(parms[1]);
			temp = new CPPImplicitFunction(__BUILTIN_POWI, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		// float __builtin_powif (float, int)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[2];
			parms[0] = c_float;
			parms[1] = c_int;
			functionType = new CFunctionType(c_float, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new BuiltinParameter(parms[0]);
			theParms[1] = new BuiltinParameter(parms[1]);
			temp = new CImplicitFunction(__BUILTIN_POWIF, scope, functionType, theParms, false);
		} else {
			IType[] parms = new IType[2];
			parms[0] = cpp_float;
			parms[1] = cpp_int;
			IFunctionType functionType = new CPPFunctionType(cpp_float, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new BuiltinParameter(parms[0]);
			theParms[1] = new BuiltinParameter(parms[1]);
			temp = new CPPImplicitFunction(__BUILTIN_POWIF, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		// long double __builtin_powil (long double, int)
		temp = null;
		
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[2];
			parms[0] = c_long_double;
			parms[1] = c_int;
			functionType = new CFunctionType(c_long_double, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new BuiltinParameter(parms[0]);
			theParms[1] = new BuiltinParameter(parms[1]);
			temp = new CImplicitFunction(__BUILTIN_POWIL, scope, functionType, theParms, false);
		} else {
			IType[] parms = new IType[2];
			parms[0] = cpp_long_double;
			parms[1] = cpp_int;
			IFunctionType functionType = new CPPFunctionType(cpp_long_double, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new BuiltinParameter(parms[0]);
			theParms[1] = new BuiltinParameter(parms[1]);
			temp = new CPPImplicitFunction(__BUILTIN_POWIL, scope, functionType, theParms, false);
		}
		
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
	}
	
	public IBinding[] getBuiltinBindings() {
		initialize();
		return (IBinding[])ArrayUtil.trim(IBinding.class, bindings);
	}
	
	private class BuiltinParameter implements IParameter {

		private static final String BLANK_STRING = ""; //$NON-NLS-1$
		private IType type=null;
		
		public BuiltinParameter(IType type) {
			this.type = type;
		}
		
		public IType getType() {
			return type;
		}

		/**
		 * returns false
		 */
		public boolean isStatic() {
			return false;
		}

		/**
		 * returns false
		 */
		public boolean isExtern() {
			return false;
		}

		/**
		 * returns false
		 */
		public boolean isAuto() {
			return false;
		}

		/**
		 * returns false
		 */
		public boolean isRegister() {
			return false;
		}

		public String getName() {
			return BLANK_STRING;
		}

		public char[] getNameCharArray() {
			return BLANK_STRING.toCharArray();
		}

		/**
		 * returns false
		 */
		public IScope getScope() {
			return null;
		}
		
	}
}
