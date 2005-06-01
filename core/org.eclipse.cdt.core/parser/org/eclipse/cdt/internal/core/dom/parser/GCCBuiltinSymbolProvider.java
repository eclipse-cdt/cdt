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
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.GPPPointerType;

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
    private static final char[] __BUILTIN_ABORT   = "__builtin_abort".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_EXIT1   = "__builtin_exit".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_EXIT2   = "__builtin__Exit".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_EXIT3   = "__builtin__exit".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_CONJ = "__builtin_conj".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_CONJF = "__builtin_conjf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_CONJL = "__builtin_conjl".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_CREAL = "__builtin_creal".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_CREALF = "__builtin_crealf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_CREALL = "__builtin_creall".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_CIMAG = "__builtin_cimag".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_CIMAGF = "__builtin_cimagf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_CIMAGL = "__builtin_cimagl".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_IMAXABS = "__builtin_imaxabs".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_LLABS = "__builtin_llabs".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_SNPRINTF = "__builtin_snprintf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_VSCANF = "__builtin_vscanf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_VSNPRINTF = "__builtin_vsnprintf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_VSSCANF = "__builtin_vsscanf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_COSF = "__builtin_cosf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_COSL = "__builtin_cosl".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_EXPF = "__builtin_expf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_EXPL = "__builtin_expl".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_FABSF = "__builtin_fabsf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_FABSL = "__builtin_fabsl".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_LOGF = "__builtin_logf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_LOGL = "__builtin_logl".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_SINF = "__builtin_sinf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_SINL = "__builtin_sinl".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_SQRTF = "__builtin_sqrtf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_SQRTL = "__builtin_sqrtl".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_ABS = "__builtin_abs".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_COS = "__builtin_cos".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_EXP = "__builtin_exp".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_FABS = "__builtin_fabs".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_FPRINTF = "__builtin_fprintf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_FPUTS = "__builtin_fputs".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_LABS = "__builtin_labs".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_LOG = "__builtin_log".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_MEMCMP = "__builtin_memcmp".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_MEMCPY = "__builtin_memcpy".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_MEMSET = "__builtin_memset".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_PRINTF = "__builtin_printf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_PUTCHAR = "__builtin_putchar".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_PUTS = "__builtin_puts".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_SCANF = "__builtin_scanf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_SIN = "__builtin_sin".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_SPRINTF = "__builtin_sprintf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_SQRT = "__builtin_sqrt".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_SSCANF = "__builtin_sscanf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_STRCAT = "__builtin_strcat".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_STRCHR = "__builtin_strchr".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_STRCMP = "__builtin_strcmp".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_STRCPY = "__builtin_strcpy".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_STRCSPN = "__builtin_strcspn".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_STRLEN = "__builtin_strlen".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_STRNCAT = "__builtin_strncat".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_STRNCMP = "__builtin_strncmp".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_STRNCPY = "__builtin_strncpy".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_STRPBRK = "__builtin_strpbrk".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_STRRCHR = "__builtin_strrchr".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_STRSPN = "__builtin_strspn".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_STRSTR = "__builtin_strstr".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_VPRINTF = "__builtin_vprintf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_VSPRINTF = "__builtin_vsprintf".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_ISGREATER = "__builtin_isgreater".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_ISGREATEREQUAL = "__builtin_isgreaterequal".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_ISLESS = "__builtin_isless".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_ISLESSEQUAL = "__builtin_islessequal".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_ISLESSGREATER = "__builtin_islessgreater".toCharArray(); //$NON-NLS-1$
    private static final char[] __BUILTIN_ISUNORDERED = "__builtin_isunordered".toCharArray(); //$NON-NLS-1$

    private static final int NUM_OTHER_GCC_BUILTINS = 105; // the total number of builtin functions listed above
	
    static final private  IType c_unspecified;
    static final private  IType c_char;
    static final private  IType c_char_p;
    static final private  IType c_char_p_r;
    static final private  IType c_const_char_p;
    static final private  IType c_const_char_p_r;
    static final private  IType c_const_void_p;
    static final private  IType c_const_void_p_r;
    static final private  IType c_double;
    static final private  IType c_double_complex;
    static final private  IType c_float;
    static final private  IType c_float_complex;
    static final private  IType c_int;
    //static final private  IType c_long;
    static final private  IType c_long_double;
    static final private  IType c_long_double_complex;
    static final private  IType c_long_int;
    static final private  IType c_long_long_int;
    static final private  IType c_signed_long_int;
    static final private  IType c_size_t;
    static final private  IType c_unsigned_int;
    static final private  IType c_unsigned_long;
    static final private  IType c_unsigned_long_long;
    static final private  IFunctionType c_va_list;
    static final private  IType c_void;
    static final private  IType c_void_p;
    static final private  IType c_void_p_r;
    static final private  IType c_FILE_p_r; // implemented as void * restrict
    static final private  IType cpp_unspecified;
    static final private  IType cpp_char;
    static final private  IType cpp_char_p;
    static final private  IType cpp_char_p_r;
    static final private  IType cpp_const_char_p;
    static final private  IType cpp_const_char_p_r;
    static final private  IType cpp_const_void_p;
    static final private  IType cpp_const_void_p_r;
    static final private  IType cpp_double;
    static final private  IType cpp_double_complex;
    static final private  IType cpp_float;
    static final private  IType cpp_float_complex;
    static final private  IType cpp_int;
    //static final private  IType cpp_long;
    static final private  IType cpp_long_double;
    static final private  IType cpp_long_double_complex;
    static final private  IType cpp_long_int;
    static final private  IType cpp_long_long_int;
    static final private  IType cpp_signed_long_int;
    static final private  IType cpp_size_t;
    static final private  IType cpp_unsigned_int;
    static final private  IType cpp_unsigned_long;
    static final private  IType cpp_unsigned_long_long;
    static final private  IFunctionType cpp_va_list;
    static final private  IType cpp_void;
    static final private  IType cpp_void_p;
    static final private  IType cpp_void_p_r;
    static final private  IType cpp_FILE_p_r; // implemented as void * restrict

	static {
		c_unspecified = new CBasicType(IBasicType.t_unspecified, 0);
		c_char = new CBasicType( IBasicType.t_char, 0 );
		c_char_p = new CPointerType( c_char, 0);
		c_char_p_r = new CPointerType( c_char, CPointerType.IS_RESTRICT);
		c_const_char_p = new CPointerType(new CQualifierType( c_char, true, false, false), 0);
		c_const_char_p_r = new CPointerType(new CQualifierType( c_char, true, false, false), CPointerType.IS_RESTRICT);
		
		c_double = new CBasicType(IBasicType.t_double, 0);
		c_double_complex = new CBasicType(IBasicType.t_double, CBasicType.IS_COMPLEX);
		c_float = new CBasicType(IBasicType.t_float, 0);
		c_float_complex = new CBasicType(IBasicType.t_float, CBasicType.IS_COMPLEX);
		c_int = new CBasicType(IBasicType.t_int, 0);

		c_long_double = new CBasicType(IBasicType.t_double, CBasicType.IS_LONG);
		c_long_double_complex = new CBasicType(IBasicType.t_double, CBasicType.IS_LONG | CBasicType.IS_COMPLEX);
		c_long_int = new CBasicType(IBasicType.t_int, CBasicType.IS_LONG);
		c_long_long_int = new CBasicType(IBasicType.t_int, CBasicType.IS_LONGLONG);
		c_signed_long_int = new CBasicType(IBasicType.t_int, CBasicType.IS_LONG | CBasicType.IS_SIGNED);
		c_unsigned_int = new CBasicType(IBasicType.t_int, CBasicType.IS_UNSIGNED);
		c_unsigned_long = new CBasicType(IBasicType.t_int, CBasicType.IS_LONG | CBasicType.IS_UNSIGNED);
		c_unsigned_long_long = new CBasicType(IBasicType.t_int, CBasicType.IS_LONGLONG | CBasicType.IS_UNSIGNED);

		c_va_list = new CFunctionType( c_char_p, new IType[0]); // assumed: char * va_list();
		c_size_t = c_unsigned_long; // assumed unsigned long int
		
		c_void = new CBasicType(IBasicType.t_void, 0);
		c_void_p = new CPointerType( c_void, 0);
		c_void_p_r = new CPointerType( c_void, CPointerType.IS_RESTRICT);
		c_const_void_p = new CPointerType(new CQualifierType( c_void, true, false, false), 0);
		c_const_void_p_r = new CPointerType(new CQualifierType( c_void, true, false, false), CPointerType.IS_RESTRICT);
		
		c_FILE_p_r = c_void_p_r; // implemented as void * restrict
		
		cpp_unspecified = new CPPBasicType(IBasicType.t_unspecified, 0);
		cpp_char = new CPPBasicType(IBasicType.t_char, 0);
		cpp_char_p = new CPPPointerType( cpp_char );
		cpp_char_p_r = new GPPPointerType( cpp_char, false, false, true);
		cpp_const_char_p = new CPPPointerType(new CPPQualifierType(cpp_char, true, false));
		cpp_const_char_p_r = new GPPPointerType(new CPPQualifierType(cpp_char, true, false), false, false, true);
		
		cpp_double = new CPPBasicType(IBasicType.t_double, 0);
		cpp_double_complex = new GPPBasicType(IBasicType.t_double, GPPBasicType.IS_COMPLEX, null);
		cpp_float = new CPPBasicType(IBasicType.t_float, 0);
		cpp_float_complex = new GPPBasicType(IBasicType.t_float, GPPBasicType.IS_COMPLEX, null);
		cpp_int = new CPPBasicType(IBasicType.t_int, 0);
		cpp_long_int = new CPPBasicType(IBasicType.t_int, CPPBasicType.IS_LONG);
		cpp_long_double = new CPPBasicType(IBasicType.t_double, CPPBasicType.IS_LONG);
		cpp_long_double_complex = new GPPBasicType(IBasicType.t_double, CPPBasicType.IS_LONG | GPPBasicType.IS_COMPLEX, null);
		cpp_long_long_int = new CPPBasicType(IBasicType.t_int, GPPBasicType.IS_LONGLONG);
		cpp_signed_long_int = new CPPBasicType(IBasicType.t_int, CPPBasicType.IS_LONG | CPPBasicType.IS_SIGNED);
		
		cpp_unsigned_int = new CPPBasicType(IBasicType.t_int, CPPBasicType.IS_UNSIGNED);
		cpp_unsigned_long = new CPPBasicType(IBasicType.t_int, CPPBasicType.IS_UNSIGNED | CPPBasicType.IS_LONG);
		cpp_unsigned_long_long = new GPPBasicType(IBasicType.t_int, CPPBasicType.IS_UNSIGNED | GPPBasicType.IS_LONGLONG, null);
		
		cpp_size_t = cpp_unsigned_long; // assumed unsigned long int
		cpp_va_list = new CPPFunctionType( cpp_char_p, new IType[0]); // assumed: char * va_list();
		
		cpp_void = new CPPBasicType(IBasicType.t_void, 0);
		cpp_void_p = new CPPPointerType( cpp_void );
		cpp_void_p_r = new GPPPointerType( cpp_void, false, false, true);
		cpp_const_void_p = new CPPPointerType(new CPPQualifierType(cpp_void, true, false));
		cpp_const_void_p_r = new GPPPointerType(new CPPQualifierType(cpp_void, true, false), false, false, true);
		
		cpp_FILE_p_r = cpp_void_p_r; // implemented as void * restrict
	}
    
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
        __builtin_exit();
        __builtin_conj();
        __builtin_creal_cimag();
        __builtin_abs();
        __builtin_printf();
        __builtin_scanf();
        __builtin_math();
        __builtin_put();
        __builtin_mem();
        __builtin_str_strn();
        __builtin_less_greater();
	}
	
	private void __builtin_va_list() {
		// char * __builtin_va_list();
		IBinding temp = null;
        if (lang == ParserLanguage.C) {
            temp = new CImplicitTypedef(c_va_list, __BUILTIN_VA_LIST, scope);
        } else {
            temp = new CPPImplicitTypedef(cpp_va_list, __BUILTIN_VA_LIST, scope);
        }
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
	}
	
	private void __builtin_expect() {
        //long __builtin_expect( long exp, long c )
		IBinding temp = null;
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[2];
			parms[0] = c_long_int;
			parms[1] = c_long_int;
			functionType = new CFunctionType(c_long_int, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new CBuiltinParameter(parms[0]);
			theParms[1] = theParms[0];
			temp = new CImplicitFunction(__BUILTIN_EXPECT, scope, functionType, theParms, false);
		} else {
			IFunctionType functionType = null;
			IType[] parms = new IType[2];
			parms[0] = cpp_long_int;
			parms[1] = cpp_long_int;
			functionType = new CPPFunctionType(cpp_long_int, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			theParms[1] = theParms[0];
			temp = new CPPImplicitFunction(__BUILTIN_EXPECT, scope, functionType, theParms, false);
		}
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
	}
	
	private void __builtin_prefetch() {
		// void __builtin_prefetch (const void *addr, ...)
		IBinding temp = null;
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_const_void_p;
			functionType = new CFunctionType(c_void, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CBuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_PREFETCH, scope, functionType, theParms, true);
		} else {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_const_void_p;
			functionType = new CPPFunctionType(cpp_void, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_PREFETCH, scope, functionType, theParms, true);
		}
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
	}
	
	private void __builtin_huge_val() {
        //double __builtin_huge_val (void)
		IBinding temp = null;
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_void;
			functionType = new CFunctionType(c_double, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CBuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_HUGE_VAL, scope, functionType, theParms, false);
		} else {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_void;
			functionType = new CPPFunctionType(cpp_double, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_HUGE_VAL, scope, functionType, theParms, false);
		}
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
        //float __builtin_huge_valf (void)
		temp = null;
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_void;
			functionType = new CFunctionType(c_float, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CBuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_HUGE_VALF, scope, functionType, theParms, false);
		} else {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_void;
			functionType = new CPPFunctionType(cpp_float, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_HUGE_VALF, scope, functionType, theParms, false);
		}
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
        //long double __builtin_huge_vall (void)
		temp = null;
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_void;
			functionType = new CFunctionType(c_long_double, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CBuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_HUGE_VALL, scope, functionType, theParms, false);
		} else {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_void;
			functionType = new CPPFunctionType(cpp_long_double, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_HUGE_VALL, scope, functionType, theParms, false);
		}
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
	}
	
	private void __builtin_inf() {
        //double __builtin_inf (void)
		IBinding temp = null;
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_void;
			functionType = new CFunctionType(c_double, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CBuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_INF, scope, functionType, theParms, false);
		} else {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_void;
			functionType = new CPPFunctionType(cpp_double, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_INF, scope, functionType, theParms, false);
		}
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
        //float __builtin_inff (void)
		temp = null;
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_void;
			functionType = new CFunctionType(c_float, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CBuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_INFF, scope, functionType, theParms, false);
		} else {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_void;
			functionType = new CPPFunctionType(cpp_float, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_INFF, scope, functionType, theParms, false);
		}
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
        //long double __builtin_infl (void)
		temp = null;
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_void;
			functionType = new CFunctionType(c_long_double, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CBuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_INFL, scope, functionType, theParms, false);
		} else {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_void;
			functionType = new CPPFunctionType(cpp_long_double, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_INFL, scope, functionType, theParms, false);
		}
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
	}
	
	private void __builtin_nan() {
		//double __builtin_nan (const char * str)
		IBinding temp = null;
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_const_char_p;
			functionType = new CFunctionType(c_double, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CBuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_NAN, scope, functionType, theParms, false);
		} else {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_const_char_p;
			functionType = new CPPFunctionType(cpp_double, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_NAN, scope, functionType, theParms, false);
		}
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//float __builtin_nanf (const char * str)
		temp = null;
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_const_char_p;
			functionType = new CFunctionType(c_float, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CBuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_NANF, scope, functionType, theParms, false);
		} else {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_const_char_p;
			functionType = new CPPFunctionType(cpp_float, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_NANF, scope, functionType, theParms, false);
		}
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//long double __builtin_nanl (const char * str)
		temp = null;
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_const_char_p;
			functionType = new CFunctionType(c_long_double, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CBuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_NANL, scope, functionType, theParms, false);
		} else {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_const_char_p;
			functionType = new CPPFunctionType(cpp_long_double, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_NANL, scope, functionType, theParms, false);
		}
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//double __builtin_nans (const char * str)
		temp = null;
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_const_char_p;
			functionType = new CFunctionType(c_double, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CBuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_NANS, scope, functionType, theParms, false);
		} else {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_const_char_p;
			functionType = new CPPFunctionType(cpp_double, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_NANS, scope, functionType, theParms, false);
		}
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//float __builtin_nansf (const char * str)
		temp = null;
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_const_char_p;
			functionType = new CFunctionType(cpp_float, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CBuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_NANSF, scope, functionType, theParms, false);
		} else {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_const_char_p;
			functionType = new CPPFunctionType(cpp_float, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_NANSF, scope, functionType, theParms, false);
		}
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
		
		//long double __builtin_nansl (const char * str)
		temp = null;
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = c_const_char_p;
			functionType = new CFunctionType(c_long_double, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CBuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_NANSL, scope, functionType, theParms, false);
		} else {
			IFunctionType functionType = null;
			IType[] parms = new IType[1];
			parms[0] = cpp_const_char_p;
			functionType = new CPPFunctionType(cpp_long_double, parms);
			IParameter[] theParms = new IParameter[1];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			temp = new CPPImplicitFunction(__BUILTIN_NANSL, scope, functionType, theParms, false);
		}
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
	}
	
	private void __builtin_unsigned_int() {
		//int __builtin_ffs(unsigned int x)
		IBinding temp = null;
		IFunctionType functionType = null;
		IParameter[] theParms = new IParameter[1];
		if (lang == ParserLanguage.C) {
			IType[] parms = new IType[1];
			parms[0] = c_unsigned_int;
			functionType = new CFunctionType(c_int, parms);
			theParms[0] = new CBuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_FFS, scope, functionType, theParms, false);
		} else {
			IType[] parms = new IType[1];
			parms[0] = cpp_unsigned_int;
			functionType = new CPPFunctionType(cpp_int, parms);
			theParms[0] = new CPPBuiltinParameter(parms[0]);
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
		//int __builtin_ffsl(unsigned long x)
		IBinding temp = null;
		IFunctionType functionType = null;
		IParameter[] theParms = new IParameter[1];
		if (lang == ParserLanguage.C) {
			IType[] parms = new IType[1];
			parms[0] = c_unsigned_long;
			functionType = new CFunctionType(c_int, parms);
			theParms[0] = new CBuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_FFSL, scope, functionType, theParms, false);
		} else {
			IType[] parms = new IType[1];
			parms[0] = cpp_unsigned_long;
			functionType = new CPPFunctionType(cpp_int, parms);
			theParms[0] = new CPPBuiltinParameter(parms[0]);
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
		//int __builtin_ffsll(unsigned long long x)
		IBinding temp = null;
		IFunctionType functionType = null;
		IParameter[] theParms = new IParameter[1];
		if (lang == ParserLanguage.C) {
			IType[] parms = new IType[1];
			parms[0] = c_unsigned_long_long;
			functionType = new CFunctionType(c_int, parms);
			theParms[0] = new CBuiltinParameter(parms[0]);
			temp = new CImplicitFunction(__BUILTIN_FFSLL, scope, functionType, theParms, false);
		} else {
			IType[] parms = new IType[1];
			parms[0] = cpp_unsigned_long_long;
			functionType = new CPPFunctionType(cpp_int, parms);
			theParms[0] = new CPPBuiltinParameter(parms[0]);
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
			IFunctionType functionType = null;
			IType[] parms = new IType[2];
			parms[0] = c_unspecified;
			parms[1] = c_unspecified;
			functionType = new CFunctionType(c_int, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new CBuiltinParameter(parms[0]);
			theParms[1] = theParms[0];
			temp = new CImplicitFunction(__BUILTIN_TYPES_COMPATIBLE_P, scope, functionType, theParms, true);
		} else {
			IFunctionType functionType = null;
			IType[] parms = new IType[2];
			parms[0] = cpp_unspecified;
			parms[1] = cpp_unspecified;
			functionType = new CPPFunctionType(cpp_int, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			theParms[1] = theParms[0];
			temp = new CPPImplicitFunction(__BUILTIN_TYPES_COMPATIBLE_P, scope, functionType, theParms, true);
		}
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
	}
	
	private void __builtin_powi() {
		// double __builtin_powi (double, int)
		IBinding temp = null;
		if (lang == ParserLanguage.C) {
			IFunctionType functionType = null;
			IType[] parms = new IType[2];
			parms[0] = c_double;
			parms[1] = c_int;
			functionType = new CFunctionType(c_double, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new CBuiltinParameter(parms[0]);
			theParms[1] = new CBuiltinParameter(parms[1]);
			temp = new CImplicitFunction(__BUILTIN_POWI, scope, functionType, theParms, false);
		} else {
			IType[] parms = new IType[2];
			parms[0] = cpp_double;
			parms[1] = cpp_int;
			IFunctionType functionType = new CPPFunctionType(cpp_double, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			theParms[1] = new CPPBuiltinParameter(parms[1]);
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
			theParms[0] = new CBuiltinParameter(parms[0]);
			theParms[1] = new CBuiltinParameter(parms[1]);
			temp = new CImplicitFunction(__BUILTIN_POWIF, scope, functionType, theParms, false);
		} else {
			IType[] parms = new IType[2];
			parms[0] = cpp_float;
			parms[1] = cpp_int;
			IFunctionType functionType = new CPPFunctionType(cpp_float, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			theParms[1] = new CPPBuiltinParameter(parms[1]);
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
			theParms[0] = new CBuiltinParameter(parms[0]);
			theParms[1] = new CBuiltinParameter(parms[1]);
			temp = new CImplicitFunction(__BUILTIN_POWIL, scope, functionType, theParms, false);
		} else {
			IType[] parms = new IType[2];
			parms[0] = cpp_long_double;
			parms[1] = cpp_int;
			IFunctionType functionType = new CPPFunctionType(cpp_long_double, parms);
			IParameter[] theParms = new IParameter[2];
			theParms[0] = new CPPBuiltinParameter(parms[0]);
			theParms[1] = new CPPBuiltinParameter(parms[1]);
			temp = new CPPImplicitFunction(__BUILTIN_POWIL, scope, functionType, theParms, false);
		}
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
	}
    
    public void __builtin_exit() {
        // void __builtin_abort(void)
        IBinding temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_void;
            functionType = new CFunctionType(c_void, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_ABORT, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_void;
            IFunctionType functionType = new CPPFunctionType(cpp_void, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_ABORT, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // void __builtin_exit(int)
        // void __builtin__Exit(int)
        // void __builtin__exit(int)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_int;
            functionType = new CFunctionType(c_void, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_EXIT1, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CImplicitFunction(__BUILTIN_EXIT2, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CImplicitFunction(__BUILTIN_EXIT3, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_int;
            IFunctionType functionType = new CPPFunctionType(cpp_void, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_EXIT1, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CPPImplicitFunction(__BUILTIN_EXIT2, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CPPImplicitFunction(__BUILTIN_EXIT3, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        }
    }
    
    private void __builtin_conj() {
        IBinding temp = null;
        // double complex __builtin_conj(double complex)
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_double_complex;
            functionType = new CFunctionType(c_double_complex, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_CONJ, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_double_complex;
            IFunctionType functionType = new CPPFunctionType(cpp_double_complex, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_CONJ, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // float complex __builtin_conjf(float complex)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_float_complex;
            functionType = new CFunctionType(c_float_complex, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_CONJF, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_float_complex;
            IFunctionType functionType = new CPPFunctionType(cpp_float_complex, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_CONJF, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);

        // long double complex __builtin_conjl(long double complex)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_long_double_complex;
            functionType = new CFunctionType(c_long_double_complex, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_CONJL, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_long_double_complex;
            IFunctionType functionType = new CPPFunctionType(cpp_long_double_complex, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_CONJL, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
    }
    
    private void __builtin_creal_cimag() {
        IBinding temp = null;
        // double __builtin_creal(double complex)
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_double_complex;
            functionType = new CFunctionType(c_double, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_CREAL, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_double_complex;
            IFunctionType functionType = new CPPFunctionType(cpp_double, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_CREAL, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);

        // float __builtin_crealf(float complex)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_float_complex;
            functionType = new CFunctionType(c_float, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_CREALF, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_float_complex;
            IFunctionType functionType = new CPPFunctionType(cpp_float, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_CREALF, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);

        // long double __builtin_creall(long double complex)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_long_double_complex;
            functionType = new CFunctionType(c_long_double, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_CREALL, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_long_double_complex;
            IFunctionType functionType = new CPPFunctionType(cpp_long_double, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_CREALL, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // double __builtin_cimag(double complex)
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_double_complex;
            functionType = new CFunctionType(c_double, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_CIMAG, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_double_complex;
            IFunctionType functionType = new CPPFunctionType(cpp_double, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_CIMAG, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // float __builtin_cimagf(float complex)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_float_complex;
            functionType = new CFunctionType(c_float, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_CIMAGF, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_float_complex;
            IFunctionType functionType = new CPPFunctionType(cpp_float, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_CIMAGF, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // long double __builtin_cimagl(long double complex)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_long_double_complex;
            functionType = new CFunctionType(c_long_double, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_CIMAGL, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_long_double_complex;
            IFunctionType functionType = new CPPFunctionType(cpp_long_double, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_CIMAGL, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);      
    }
    
    private void __builtin_abs() {
        IBinding temp = null;
        // int __builtin_abs(int)
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_int;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_ABS, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_int;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_ABS, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);

        // double __builtin_fabs(double)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_double;
            functionType = new CFunctionType(c_double, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_FABS, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_double;
            IFunctionType functionType = new CPPFunctionType(cpp_double, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_FABS, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);

        // long int __builtin_labs(long int)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_long_int;
            functionType = new CFunctionType(c_long_int, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_LABS, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_long_int;
            IFunctionType functionType = new CPPFunctionType(cpp_long_int, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_LABS, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // intmax_t __builtin_imaxabs(intmax_t) // C99: 7.18.1.5- intmax_t = signed long int (any signed int)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_signed_long_int;
            functionType = new CFunctionType(c_signed_long_int, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_IMAXABS, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_signed_long_int;
            IFunctionType functionType = new CPPFunctionType(cpp_signed_long_int, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_IMAXABS, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // long long int __builtin_llabs(long long int)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_long_long_int;
            functionType = new CFunctionType(c_long_long_int, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_LLABS, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_long_long_int;
            IFunctionType functionType = new CPPFunctionType(cpp_long_long_int, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_LLABS, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // float __builtin_fabsf(float)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_float;
            functionType = new CFunctionType(c_float, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_FABSF, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_float;
            IFunctionType functionType = new CPPFunctionType(cpp_float, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_FABSF, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // long double __builtin_fabsl(long double)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_long_double;
            functionType = new CFunctionType(c_long_double, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_FABSL, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_long_double;
            IFunctionType functionType = new CPPFunctionType(cpp_long_double, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_FABSL, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
    }
    
    private void __builtin_printf() {
        IBinding temp = null;
        // int __builtin_printf(const char * restrict, ...)
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_const_char_p_r;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_PRINTF, scope, functionType, theParms, true);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_const_char_p_r;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_PRINTF, scope, functionType, theParms, true);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // int __builtin_sprintf(char * restrict, const char * restrict, ...)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_char_p_r;
            parms[1] = c_const_char_p_r;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_SPRINTF, scope, functionType, theParms, true);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_char_p_r;
            parms[1] = cpp_const_char_p_r;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_SPRINTF, scope, functionType, theParms, true);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // int __builtin_snprintf(char * restrict, size_t, const char * restrict, ...) // use unsigned long int for size_t
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[3];
            parms[0] = c_char_p_r;
            parms[1] = c_size_t;
            parms[2] = c_const_char_p_r;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[3];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            theParms[2] = new CBuiltinParameter(parms[2]);
            temp = new CImplicitFunction(__BUILTIN_SNPRINTF, scope, functionType, theParms, true);
        } else {
            IType[] parms = new IType[3];
            parms[0] = cpp_char_p_r;
            parms[1] = cpp_size_t;
            parms[2] = cpp_const_char_p_r;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[3];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            theParms[2] = new CPPBuiltinParameter(parms[2]);
            temp = new CPPImplicitFunction(__BUILTIN_SNPRINTF, scope, functionType, theParms, true);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // int __builtin_fprintf(FILE * restrict, const char * restrict) // use void * restrict for FILE
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_FILE_p_r;
            parms[1] = c_const_char_p_r;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_FPRINTF, scope, functionType, theParms, true);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_FILE_p_r;
            parms[1] = cpp_const_char_p_r;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_FPRINTF, scope, functionType, theParms, true);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // int __builtin_vprintf(const char * restrict, va_list)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_const_char_p_r;
            parms[1] = c_va_list;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_VPRINTF, scope, functionType, theParms, true);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_const_char_p_r;
            parms[1] = cpp_va_list;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_VPRINTF, scope, functionType, theParms, true);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // int __builtin_vsprintf(char * restrict, size_t, const char * restrict, va_list)
        // int __builtin_vsnprintf(char * restrict, size_t, const char * restrict, va_list)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[4];
            parms[0] = c_char_p_r;
            parms[1] = c_size_t;
            parms[2] = c_const_char_p_r;
            parms[3] = c_va_list;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[4];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            theParms[2] = new CBuiltinParameter(parms[2]);
            theParms[3] = new CBuiltinParameter(parms[3]);
            temp = new CImplicitFunction(__BUILTIN_VSPRINTF, scope, functionType, theParms, true);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CImplicitFunction(__BUILTIN_VSNPRINTF, scope, functionType, theParms, true);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        } else {
            IType[] parms = new IType[4];
            parms[0] = cpp_char_p_r;
            parms[1] = cpp_size_t;
            parms[2] = cpp_const_char_p_r;
            parms[3] = cpp_va_list;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[4];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            theParms[2] = new CPPBuiltinParameter(parms[2]);
            theParms[3] = new CPPBuiltinParameter(parms[3]);
            temp = new CPPImplicitFunction(__BUILTIN_VSPRINTF, scope, functionType, theParms, true);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CPPImplicitFunction(__BUILTIN_VSNPRINTF, scope, functionType, theParms, true);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        }
    }
    
    private void __builtin_scanf() {
        IBinding temp = null;
        // int __builtin_vscanf(const char * restrict, va_list)
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_const_char_p_r;
            parms[1] = c_va_list;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_VSCANF, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_const_char_p_r;
            parms[1] = cpp_va_list;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_VSCANF, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // int __builtin_vsscanf(const char * restrict, const char * restrict, va_list)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[3];
            parms[0] = c_const_char_p_r;
            parms[1] = c_const_char_p_r;
            parms[2] = c_va_list;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[3];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            theParms[2] = new CBuiltinParameter(parms[2]);
            temp = new CImplicitFunction(__BUILTIN_VSSCANF, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[3];
            parms[0] = cpp_const_char_p_r;
            parms[1] = cpp_const_char_p_r;
            parms[2] = cpp_va_list;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[3];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            theParms[2] = new CPPBuiltinParameter(parms[2]);
            temp = new CPPImplicitFunction(__BUILTIN_VSSCANF, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // int __builtin_scanf(const char * restrict, ...)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_const_char_p_r;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_SCANF, scope, functionType, theParms, true);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_const_char_p_r;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_SCANF, scope, functionType, theParms, true);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // int __builtin_sscanf(const char * restrict, const char * restrict, ...)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_const_char_p_r;
            parms[1] = c_const_char_p_r;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_SSCANF, scope, functionType, theParms, true);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_const_char_p_r;
            parms[1] = cpp_const_char_p_r;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_SSCANF, scope, functionType, theParms, true);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
    }
    
    private void __builtin_math() {
        IBinding temp = null;
        // double __builtin_cos(double)
        // double __builtin_exp(double)
        // double __builtin_log(double)
        // double __builtin_sin(double)
        // double __builtin_sqrt(double)
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_double;
            functionType = new CFunctionType(c_double, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_COS, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CImplicitFunction(__BUILTIN_EXP, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CImplicitFunction(__BUILTIN_LOG, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CImplicitFunction(__BUILTIN_SIN, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CImplicitFunction(__BUILTIN_SQRT, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_double;
            IFunctionType functionType = new CPPFunctionType(cpp_double, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_COS, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CPPImplicitFunction(__BUILTIN_EXP, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CPPImplicitFunction(__BUILTIN_LOG, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CPPImplicitFunction(__BUILTIN_SIN, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CPPImplicitFunction(__BUILTIN_SQRT, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        }        
        
        // float __builtin_cosf(float)
        // float __builtin_expf(float)        
        // float __builtin_logf(float)
        // float __builtin_sinf(float)
        // float __builtin_sqrtf(float)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_float;
            functionType = new CFunctionType(c_float, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_COSF, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CImplicitFunction(__BUILTIN_EXPF, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CImplicitFunction(__BUILTIN_LOGF, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CImplicitFunction(__BUILTIN_SINF, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CImplicitFunction(__BUILTIN_SQRTF, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_float;
            IFunctionType functionType = new CPPFunctionType(cpp_float, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_COSF, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CPPImplicitFunction(__BUILTIN_EXPF, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CPPImplicitFunction(__BUILTIN_LOGF, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CPPImplicitFunction(__BUILTIN_SINF, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CPPImplicitFunction(__BUILTIN_SQRTF, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        }
        
        // long double __builtin_cosl(long double)
        // long double __builtin_expl(long double)
        // long double __builtin_logl(long double)
        // long double __builtin_sinl(long double)
        // long double __builtin_sqrtl(long double)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_long_double;
            functionType = new CFunctionType(c_long_double, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_COSL, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CImplicitFunction(__BUILTIN_EXPL, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CImplicitFunction(__BUILTIN_LOGL, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CImplicitFunction(__BUILTIN_SINL, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CImplicitFunction(__BUILTIN_SQRTL, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_long_double;
            IFunctionType functionType = new CPPFunctionType(cpp_long_double, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_COSL, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CPPImplicitFunction(__BUILTIN_EXPL, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CPPImplicitFunction(__BUILTIN_LOGL, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CPPImplicitFunction(__BUILTIN_SINL, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
            temp = new CPPImplicitFunction(__BUILTIN_SQRTL, scope, functionType, theParms, false);
            bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        }
    }
    
    private void __builtin_put() {
        IBinding temp = null;
        // int __builtin_fputs(const char * restrict, FILE * restrict)
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_const_char_p_r;
            parms[1] = c_FILE_p_r;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_FPUTS, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_const_char_p_r;
            parms[1] = cpp_FILE_p_r;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_FPUTS, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);

        // int __builtin_putchar(int)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_int;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_PUTCHAR, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_int;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_PUTCHAR, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // int __builtin_puts(const char *)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_const_char_p;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_PUTS, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_const_char_p;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_PUTS, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
    }
    
    private void __builtin_mem() {
        IBinding temp = null;
        // int __builtin_memcmp(const void *, const void *, size_t)
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[3];
            parms[0] = c_const_void_p;
            parms[1] = c_const_void_p;
            parms[2] = c_size_t;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[3];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            theParms[2] = new CBuiltinParameter(parms[2]);
            temp = new CImplicitFunction(__BUILTIN_MEMCMP, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[3];
            parms[0] = cpp_const_void_p;
            parms[1] = cpp_const_void_p;
            parms[2] = cpp_size_t;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[3];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            theParms[2] = new CPPBuiltinParameter(parms[2]);
            temp = new CPPImplicitFunction(__BUILTIN_MEMCMP, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);

        // void * __builtin_memcpy(void * restrict, const void * restrict, size_t)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[3];
            parms[0] = c_void_p_r;
            parms[1] = c_const_void_p_r;
            parms[2] = c_size_t;
            functionType = new CFunctionType(c_void_p, parms);
            IParameter[] theParms = new IParameter[3];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            theParms[2] = new CBuiltinParameter(parms[2]);
            temp = new CImplicitFunction(__BUILTIN_MEMCPY, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[3];
            parms[0] = cpp_void_p_r;
            parms[1] = cpp_const_void_p_r;
            parms[2] = cpp_size_t;
            IFunctionType functionType = new CPPFunctionType(cpp_void_p, parms);
            IParameter[] theParms = new IParameter[3];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            theParms[2] = new CPPBuiltinParameter(parms[2]);
            temp = new CPPImplicitFunction(__BUILTIN_MEMCPY, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // void * __builtin_memset(void *, int, size_t)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[3];
            parms[0] = c_void_p;
            parms[1] = c_int;
            parms[2] = c_size_t;
            functionType = new CFunctionType(c_void_p, parms);
            IParameter[] theParms = new IParameter[3];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            theParms[2] = new CBuiltinParameter(parms[2]);
            temp = new CImplicitFunction(__BUILTIN_MEMSET, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[3];
            parms[0] = cpp_void_p;
            parms[1] = cpp_int;
            parms[2] = cpp_size_t;
            IFunctionType functionType = new CPPFunctionType(cpp_void_p, parms);
            IParameter[] theParms = new IParameter[3];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            theParms[2] = new CPPBuiltinParameter(parms[2]);
            temp = new CPPImplicitFunction(__BUILTIN_MEMSET, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
    }
    
    private void __builtin_str_strn() {
        IBinding temp = null;
        // char * __builtin_strcat(char * restrict, const char * restrict)
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_char_p_r;
            parms[1] = c_const_char_p_r;
            functionType = new CFunctionType(c_char_p, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_STRCAT, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_char_p_r;
            parms[1] = cpp_const_char_p_r;
            IFunctionType functionType = new CPPFunctionType(cpp_char_p, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_STRCAT, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // char * __builtin_strchr(const char *, int)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_const_char_p;
            parms[1] = c_int;
            functionType = new CFunctionType(c_char_p, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_STRCHR, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_const_char_p;
            parms[1] = cpp_int;
            IFunctionType functionType = new CPPFunctionType(cpp_char_p, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_STRCHR, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // int __builtin_strcmp(const char *, const char *)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_const_char_p;
            parms[1] = c_const_char_p;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_STRCMP, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_const_char_p;
            parms[1] = cpp_const_char_p;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_STRCMP, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // char * __builtin_strcpy(char * restrict, const char * restrict)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_char_p_r;
            parms[1] = c_const_char_p_r;
            functionType = new CFunctionType(c_char_p, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_STRCPY, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_char_p_r;
            parms[1] = cpp_const_char_p_r;
            IFunctionType functionType = new CPPFunctionType(cpp_char_p, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_STRCPY, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // size_t __builtin_strcspn(const char *, const char *)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_const_char_p;
            parms[1] = c_const_char_p;
            functionType = new CFunctionType(c_size_t, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_STRCSPN, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_const_char_p;
            parms[1] = cpp_const_char_p;
            IFunctionType functionType = new CPPFunctionType(cpp_size_t, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_STRCSPN, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // size_t __builtin_strlen(const char *)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[1];
            parms[0] = c_const_char_p;
            functionType = new CFunctionType(c_size_t, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CBuiltinParameter(parms[0]);
            temp = new CImplicitFunction(__BUILTIN_STRLEN, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[1];
            parms[0] = cpp_const_char_p;
            IFunctionType functionType = new CPPFunctionType(cpp_size_t, parms);
            IParameter[] theParms = new IParameter[1];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            temp = new CPPImplicitFunction(__BUILTIN_STRLEN, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // char * __builtin_strpbrk(const char *, const char *)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_const_char_p;
            parms[1] = c_const_char_p;
            functionType = new CFunctionType(c_char_p, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_STRPBRK, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_const_char_p;
            parms[1] = cpp_const_char_p;
            IFunctionType functionType = new CPPFunctionType(cpp_char_p, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_STRPBRK, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // char * __builtin_strrchr(const char *, int)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_const_char_p;
            parms[1] = c_int;
            functionType = new CFunctionType(c_char_p, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_STRRCHR, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_const_char_p;
            parms[1] = cpp_int;
            IFunctionType functionType = new CPPFunctionType(cpp_char_p, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_STRRCHR, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // size_t __builtin_strspn(const char *, const char *)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_const_char_p;
            parms[1] = c_const_char_p;
            functionType = new CFunctionType(c_size_t, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_STRSPN, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_const_char_p;
            parms[1] = cpp_const_char_p;
            IFunctionType functionType = new CPPFunctionType(cpp_size_t, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_STRSPN, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // char * __builtin_strstr(const char *, const char *)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_const_char_p;
            parms[1] = c_const_char_p;
            functionType = new CFunctionType(c_char_p, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_STRSTR, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_const_char_p;
            parms[1] = cpp_const_char_p;
            IFunctionType functionType = new CPPFunctionType(cpp_char_p, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_STRSTR, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // char * __builtin_strncat(char * restrict, const char * restrict, size_t)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[3];
            parms[0] = c_char_p_r;
            parms[1] = c_const_char_p_r;
            parms[2] = c_size_t;
            functionType = new CFunctionType(c_char_p, parms);
            IParameter[] theParms = new IParameter[3];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            theParms[2] = new CBuiltinParameter(parms[2]);
            temp = new CImplicitFunction(__BUILTIN_STRNCAT, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[3];
            parms[0] = cpp_char_p_r;
            parms[1] = cpp_const_char_p_r;
            parms[2] = cpp_size_t;
            IFunctionType functionType = new CPPFunctionType(cpp_char_p, parms);
            IParameter[] theParms = new IParameter[3];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            theParms[2] = new CPPBuiltinParameter(parms[2]);
            temp = new CPPImplicitFunction(__BUILTIN_STRNCAT, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // int __builtin_strncmp(const char *, const char *, size_t)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[3];
            parms[0] = c_const_char_p;
            parms[1] = c_const_char_p;
            parms[2] = c_size_t;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[3];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            theParms[2] = new CBuiltinParameter(parms[2]);
            temp = new CImplicitFunction(__BUILTIN_STRNCMP, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[3];
            parms[0] = cpp_const_char_p;
            parms[1] = cpp_const_char_p;
            parms[2] = cpp_size_t;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[3];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            theParms[2] = new CPPBuiltinParameter(parms[2]);
            temp = new CPPImplicitFunction(__BUILTIN_STRNCMP, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);

        // char * __builtin_strncpy(char * restrict, const char * restrict, size_t)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[3];
            parms[0] = c_char_p_r;
            parms[1] = c_const_char_p_r;
            parms[2] = c_size_t;
            functionType = new CFunctionType(c_char_p, parms);
            IParameter[] theParms = new IParameter[3];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            theParms[2] = new CBuiltinParameter(parms[2]);
            temp = new CImplicitFunction(__BUILTIN_STRNCPY, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[3];
            parms[0] = cpp_char_p_r;
            parms[1] = cpp_const_char_p_r;
            parms[2] = cpp_size_t;
            IFunctionType functionType = new CPPFunctionType(cpp_char_p, parms);
            IParameter[] theParms = new IParameter[3];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            theParms[2] = new CPPBuiltinParameter(parms[2]);
            temp = new CPPImplicitFunction(__BUILTIN_STRNCPY, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
    }
    
    public void __builtin_less_greater() {
        IBinding temp = null;
        // int __builtin_isgreater(real-floating, real-floating)
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_float;
            parms[1] = c_float;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_ISGREATER, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_float;
            parms[1] = cpp_float;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_ISGREATER, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // int __builtin_isgreaterequal(real-floating, real-floating)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_float;
            parms[1] = c_float;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_ISGREATEREQUAL, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_float;
            parms[1] = cpp_float;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_ISGREATEREQUAL, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // int __builtin_isless(real-floating, real-floating)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_float;
            parms[1] = c_float;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_ISLESS, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_float;
            parms[1] = cpp_float;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_ISLESS, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // int __builtin_islessequal(real-floating, real-floating)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_float;
            parms[1] = c_float;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_ISLESSEQUAL, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_float;
            parms[1] = cpp_float;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_ISLESSEQUAL, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // int __builtin_islessgreater(real-floating, real-floating)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_float;
            parms[1] = c_float;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_ISLESSGREATER, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_float;
            parms[1] = cpp_float;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_ISLESSGREATER, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
        
        // int __builtin_isunordered(real-floating, real-floating)
        temp = null;
        if (lang == ParserLanguage.C) {
            IFunctionType functionType = null;
            IType[] parms = new IType[2];
            parms[0] = c_float;
            parms[1] = c_float;
            functionType = new CFunctionType(c_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CBuiltinParameter(parms[0]);
            theParms[1] = new CBuiltinParameter(parms[1]);
            temp = new CImplicitFunction(__BUILTIN_ISUNORDERED, scope, functionType, theParms, false);
        } else {
            IType[] parms = new IType[2];
            parms[0] = cpp_float;
            parms[1] = cpp_float;
            IFunctionType functionType = new CPPFunctionType(cpp_int, parms);
            IParameter[] theParms = new IParameter[2];
            theParms[0] = new CPPBuiltinParameter(parms[0]);
            theParms[1] = new CPPBuiltinParameter(parms[1]);
            temp = new CPPImplicitFunction(__BUILTIN_ISUNORDERED, scope, functionType, theParms, false);
        }
        bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
    }
    
	public IBinding[] getBuiltinBindings() {
		initialize();
		return (IBinding[])ArrayUtil.trim(IBinding.class, bindings);
	}
	
	public static class CBuiltinParameter implements IParameter {

		private static final String BLANK_STRING = ""; //$NON-NLS-1$
		private IType type=null;
		
		public CBuiltinParameter(IType type) {
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
    
	static public class CPPBuiltinParameter implements ICPPParameter {

        private static final String BLANK_STRING = ""; //$NON-NLS-1$
        private IType type=null;
        
        public CPPBuiltinParameter(IType type) {
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

        /**
         * return false
         */
        public IASTInitializer getDefaultValue() {
            return null;
        }

        /**
         * return false
         */
        public boolean isMutable() {
            return false;
        }

        public String[] getQualifiedName() {
            return new String[0];
        }

        public char[][] getQualifiedNameCharArray() {
            return new char[0][];
        }

        public boolean isGloballyQualified() {
            return false;
        }
        
    }
}
