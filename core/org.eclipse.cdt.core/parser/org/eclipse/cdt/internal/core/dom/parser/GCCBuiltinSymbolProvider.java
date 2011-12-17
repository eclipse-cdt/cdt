/*******************************************************************************
 *  Copyright (c) 2005, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM - Initial API and implementation
 *      Markus Schorn (Wind River Systems)
 *      Anton Leherbauer (Wind River Systems)
 *      Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.c.CBasicType;
import org.eclipse.cdt.internal.core.dom.parser.c.CBuiltinParameter;
import org.eclipse.cdt.internal.core.dom.parser.c.CBuiltinVariable;
import org.eclipse.cdt.internal.core.dom.parser.c.CFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.c.CImplicitFunction;
import org.eclipse.cdt.internal.core.dom.parser.c.CImplicitTypedef;
import org.eclipse.cdt.internal.core.dom.parser.c.CPointerType;
import org.eclipse.cdt.internal.core.dom.parser.c.CQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBuiltinParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBuiltinVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitTypedef;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;

/**
 * This is the IBuiltinBindingsProvider used to implement the "Other" built-in GCC symbols defined:
 * http://gcc.gnu.org/onlinedocs/gcc/Other-Builtins.html#Other-Builtins
 */
@SuppressWarnings("nls")
public class GCCBuiltinSymbolProvider implements IBuiltinBindingsProvider {
	/**
	 * <code>BUILTIN_GCC_SYMBOL</code> is a built-in GCC symbol.
	 */
	public static final ASTNodeProperty BUILTIN_GCC_SYMBOL = new ASTNodeProperty(
		"GCCBuiltinSymbolProvider.BUILTIN_GCC_SYMBOL - built-in GCC symbol"); //$NON-NLS-1$
	
	private static final Map<String, char[]> CHAR_ARRAYS = new HashMap<String, char[]>();

	private IBinding[] fBindings;
	private IScope fScope;
	private final boolean fCpp;
	private final boolean fGnu;

	private Map<String, IType> fTypeMap;
	private List<IBinding> fBindingList;
	
	public GCCBuiltinSymbolProvider(ParserLanguage lang, boolean supportGnuSymbols) {
		fCpp= lang == ParserLanguage.CPP;
		fGnu= supportGnuSymbols;
	}
	
	@Override
	public IBinding[] getBuiltinBindings(IScope scope) {
		fScope= scope;
		initialize();
		return fBindings;
	}
	
	private void initialize() {
		// Symbols for all parsers
		fTypeMap= new HashMap<String, IType>();
		fBindingList= new ArrayList<IBinding>();
		addStdBuiltins();
		if (fGnu) {
			addGnuBuiltins();
		}
		
		fBindings= fBindingList.toArray(new IBinding[fBindingList.size()]);
		fTypeMap= null;
		fBindingList= null;
	}
	
	private void addStdBuiltins() {
		variable("const char*", "__func__");
		variable("const char*", "__FUNCTION__");
		variable("const char*", "__PRETTY_FUNCTION__");
	}

	private void addGnuBuiltins() {
		// Used in stdtypes.h, mentioned in the manual but not defined in there.
		typedef("va_list", 		"__builtin_va_list");
		function("void*", 		"__builtin_va_start", "va_list", "...");
		function("void", 		"__builtin_va_end",  "va_list"); 
		function("void", 		"__builtin_va_copy",  "va_list", "va_list");

		// Gcc 4.6.0, Section 6.48
		function("void*", 		"__builtin_return_address",  "unsigned int");
		function("void*", 		"__builtin_extract_return_address",  "void*");
		function("void*", 		"__builtin_frob_return_address",  "void*");
		function("void*", 		"__builtin_frame_address",  "unsigned int"); 
		
		// Gcc 4.6.0, Section 6.51
		String[] types= {"int", "long", "long long", "unsigned int", "unsigned long", "unsigned long long"};
		for (String type : types) {
			// Manual does not mention volatile, however functions can be used for ptr to volatile
			String typePtr= "volatile " + type+"*"; 
			function(type, 	"__sync_fetch_and_add", typePtr, type, "...");
			function(type, 	"__sync_fetch_and_sub", typePtr, type, "...");
			function(type, 	"__sync_fetch_and_or", typePtr, type, "...");
			function(type, 	"__sync_fetch_and_and", typePtr, type, "...");
			function(type, 	"__sync_fetch_and_xor", typePtr, type, "...");
			function(type, 	"__sync_fetch_and_nand", typePtr, type, "...");
			function(type, 	"__sync_add_and_fetch", typePtr, type, "...");
			function(type, 	"__sync_sub_and_fetch", typePtr, type, "...");
			function(type, 	"__sync_or_and_fetch", typePtr, type, "...");
			function(type, 	"__sync_and_and_fetch", typePtr, type, "...");
			function(type, 	"__sync_xor_and_fetch", typePtr, type, "...");
			function(type, 	"__sync_nand_and_fetch", typePtr, type, "...");
			function(type, 	"__sync_lock_test_and_set", typePtr, type, "..."); 
			function(type, 	"__sync_lock_release", typePtr, "..."); 
		}
		function("void", 	"__sync_synchronize"); 
		
		// Gcc 4.6.0, Section 6.53 (incomplete)
		function("void", 		"__builtin_abort", "void");
		function("int", 		"__builtin_abs", "int");
		function("double", 		"__builtin_acos", "double");
		function("float", 		"__builtin_acosf", "float");
		function("long double", "__builtin_acosl", "long double");
		function("double", 		"__builtin_asin", "double");
		function("float", 		"__builtin_asinf", "float");
		function("long double", "__builtin_asinl", "long double");
		function("double", 		"__builtin_atan", "double");
		function("long double", "__builtin_atanl", "long double");
		function("float", 		"__builtin_atanf", "float");
		function("double", 		"__builtin_atan2", "double", "double"); 
		function("float", 		"__builtin_atan2f", "float", "float"); 
		function("long double", "__builtin_atan2l", "long double", "long double"); 
		function("void*", 		"__builtin_alloca", "size_t");
		cfunction("", 			"__builtin_choose_expr", "", "", "");
		function("double", 		"__builtin_ceil", "double");
		function("float", 		"__builtin_ceilf", "float");
		function("long double", "__builtin_ceill", "long double");
		function("double", 		"__builtin_cimag", "complex double");
		function("float", 		"__builtin_cimagf", "complex float");
		function("long double", "__builtin_cimagl", "complex long double");
		function("int", 		"__builtin_clz", "unsigned int");
		function("int", 		"__builtin_clzl", "unsigned long");
		function("int", 		"__builtin_clzll", "unsigned long long");
		function("complex double", 		"__builtin_conj", "complex double");
		function("complex float", 		"__builtin_conjf", "complex float");
		function("complex long double", "__builtin_conjl", "complex long double");
		function("double", 		"__builtin_cos", "double");
		function("float", 		"__builtin_cosf", "float");
		function("double", 		"__builtin_cosh", "double");
		function("float", 		"__builtin_coshf", "float");
		function("long double", "__builtin_coshl", "long double");
		function("long double", "__builtin_cosl", "long double");
		function("double", 		"__builtin_creal", "complex double");
		function("float", 		"__builtin_crealf", "complex float");
		function("long double", "__builtin_creall", "complex long double");
		function("int", 		"__builtin_ctz", "unsigned int");
		function("int", 		"__builtin_ctzl", "unsigned long");
		function("int", 		"__builtin_ctzll", "unsigned long long");
		function("void", 		"__builtin__Exit", "int");
		function("void", 		"__builtin__exit", "int");
		function("void", 		"__builtin_exit", "int");
		function("double", 		"__builtin_exp", "double");
		function("float", 		"__builtin_expf", "float");
		function("long double", "__builtin_expl", "long double");
		function("long", 		"__builtin_expect",  "long", "long"); 
		function("double", 		"__builtin_fabs", "double");
		function("float", 		"__builtin_fabsf", "float");
		function("long double", "__builtin_fabsl", "long double");
		function("int", 		"__builtin_ffs", "unsigned int");
		function("int", 		"__builtin_ffsl", "unsigned long");
		function("int", 		"__builtin_ffsll", "unsigned long long");
		function("double", 		"__builtin_floor", "double");
		function("float", 		"__builtin_floorf", "float");
		function("long double", "__builtin_floorl", "long double");
		function("double", 		"__builtin_fmod", "double", "double"); 
		function("float", 		"__builtin_fmodf", "float", "float"); 
		function("long double", "__builtin_fmodl", "long double", "long double"); 
		function("int", 		"__builtin_fprintf", "FILE*", "const char*");
		function("int", 		"__builtin_fputs", "const char*", "FILE*");
		function("double", 		"__builtin_frexp", "double", "int*");
		function("float", 		"__builtin_frexpf", "float", "int*");
		function("long double", "__builtin_frexpl", "long double", "int*");
		function("double", 		"__builtin_huge_val", "void"); 
		function("float", 		"__builtin_huge_valf", "void"); 
		function("long double", "__builtin_huge_vall", "void"); 
		function("long long", 	"__builtin_imaxabs", "long long");
		function("double", 		"__builtin_inf", "void"); 
		function("float", 		"__builtin_inff", "void"); 
		function("long double", "__builtin_infl", "void"); 
		function("int", 		"__builtin_isgreater", "float", "float");
		function("int", 		"__builtin_isgreaterequal", "float", "float");
		function("int", 		"__builtin_isless", "float", "float");
		function("int", 		"__builtin_islessequal", "float", "float");
		function("int", 		"__builtin_islessgreater", "float", "float");
		function("int", 		"__builtin_isunordered", "float", "float");
		function("long", 		"__builtin_labs", "long");
		function("double", 		"__builtin_ldexp", "double", "int");
		function("float", 		"__builtin_ldexpf", "float", "int");
		function("long double", "__builtin_ldexpl", "long double", "int");
		function("long long", 	"__builtin_llabs", "long long");
		function("double", 		"__builtin_log", "double");
		function("float", 		"__builtin_logf", "float");
		function("long double", "__builtin_logl", "long double");
		function("double", 		"__builtin_log10", "double");
		function("float", 		"__builtin_log10f", "float");
		function("long double", "__builtin_log10l", "long double");
		function("float", 		"__builtin_modff", "float", "float*"); 
		function("long double", "__builtin_modfl", "long double", "long double*"); 
		function("void*", 		"__builtin_memchr", "const void*", "int", "size_t");        // not in the manual
		function("int", 		"__builtin_memcmp", "const void*", "const void*", "size_t");
		function("void*", 		"__builtin_memcpy", "void*", "const void*", "size_t");
		function("void*", 		"__builtin_memmove", "void*", "const void*", "size_t");     // not in the manual
		function("void*", 		"__builtin_memset", "void*", "int", "size_t");
		function("double", 		"__builtin_nan", "const char*"); 
		function("float", 		"__builtin_nanf", "const char*"); 
		function("long double", "__builtin_nanl", "const char*"); 
		function("double", 		"__builtin_nans", "const char*"); 
		function("float", 		"__builtin_nansf", "const char*"); 
		function("long double", "__builtin_nansl", "const char*"); 
		function("int", 		"__builtin_parity", "unsigned int");
		function("int", 		"__builtin_parityl", "unsigned long");
		function("int", 		"__builtin_parityll", "unsigned long long");
		function("int", 		"__builtin_popcount", "unsigned int");
		function("int", 		"__builtin_popcountl", "unsigned long");
		function("int", 		"__builtin_popcountll", "unsigned long long");
		function("float", 		"__builtin_powf", "float");
		function("long", 		"__builtin_pow", "long", "long");
		function("long double", "__builtin_powl", "long double", "long double");
		function("double", 		"__builtin_powi", "double", "int");
		function("float", 		"__builtin_powif", "float", "int");
		function("long double", "__builtin_powil", "long double", "int");
		function("void", 		"__builtin_prefetch", "const void*", "...");
		function("int", 		"__builtin_printf", "const char*", "...");
		function("int", 		"__builtin_putchar", "int");
		function("int", 		"__builtin_puts", "const char*");
		function("int", 		"__builtin_scanf", "const char*", "...");
		function("double", 		"__builtin_sin", "double");
		function("float", 		"__builtin_sinf", "float");
		function("double", 		"__builtin_sinh", "double");
		function("float", 		"__builtin_sinhf", "float");
		function("long double", "__builtin_sinhl", "long double");
		function("long double", "__builtin_sinl", "long double");
		function("int", 		"__builtin_snprintf", "char*", "size_t", "const char*", "...");
		function("int", 		"__builtin_sprintf", "char*", "const char*", "...");
		function("double", 		"__builtin_sqrt", "double");
		function("float", 		"__builtin_sqrtf", "float");
		function("long double", "__builtin_sqrtl", "long double");
		function("int", 		"__builtin_sscanf", "const char*", "const char*", "...");
		function("char*", 		"__builtin_strcat", "char*", "const char*");
		function("char*", 		"__builtin_strchr", "const char*", "int");
		function("int", 		"__builtin_strcmp", "const char*", "const char*");
		function("char*", 		"__builtin_strcpy", "char*", "const char*");
		function("size_t", 		"__builtin_strcspn", "const char*", "const char*");
		function("size_t", 		"__builtin_strlen", "const char*");
		function("char*", 		"__builtin_strncat", "char*", "const char*", "size_t");
		function("int", 		"__builtin_strncmp", "const char*", "const char*", "size_t");
		function("char*", 		"__builtin_strncpy", "char*", "const char*", "size_t");
		function("char*", 		"__builtin_strpbrk", "const char*", "const char*");
		function("char*", 		"__builtin_strrchr", "const char*", "int");
		function("size_t", 		"__builtin_strspn", "const char*", "const char*");
		function("char*", 		"__builtin_strstr", "const char*", "const char*");
		function("double", 		"__builtin_tan", "double");
		function("float", 		"__builtin_tanf", "float");
		function("double", 		"__builtin_tanh", "double");
		function("float", 		"__builtin_tanhf", "float");
		function("long double", "__builtin_tanhl", "long double");
		function("long double", "__builtin_tanl", "long double");
		function("int", 		"__builtin_types_compatible_p", "", "");
		function("int", 		"__builtin_vprintf", "const char*", "va_list");
		function("int", 		"__builtin_vscanf", "const char*", "va_list");
		function("int", 		"__builtin_vsnprintf", "char*", "size_t", "const char*", "va_list");
		function("int", 		"__builtin_vsprintf", "char*", "const char*", "va_list");
		function("int", 		"__builtin_vsscanf", "const char*", "const char*", "va_list");
    }

	private void variable(String type, String name) {
		IBinding b = fCpp ? new CPPBuiltinVariable(toType(type), toCharArray(name), fScope)
				: new CBuiltinVariable(toType(type), toCharArray(name), fScope);
		fBindingList.add(b);
	}

	private void typedef(String type, String name) {
		IBinding b = fCpp ? new CPPImplicitTypedef(toType(type), toCharArray(name), fScope)
				: new CImplicitTypedef(toType(type), toCharArray(name), fScope);
		fBindingList.add(b);
	}

	private void cfunction(String returnType, String name, String... parameterTypes) {
		if (!fCpp) {
			function(returnType, name, parameterTypes);
		}
	}
	
	private void function(String returnType, String name, String... parameterTypes) {
		int len = parameterTypes.length;
		boolean varargs= len > 0 && parameterTypes[len-1].equals("...");
		if (varargs)
			len--;
		
		IType[] pTypes = new IType[len];
		IParameter[] theParms = fCpp ? new ICPPParameter[len] : new IParameter[len];
		for (int i = 0; i < len; i++) {
			IType pType = toType(parameterTypes[i]);
			pTypes[i] = pType;
			theParms[i] = fCpp ? new CPPBuiltinParameter(pType) : new CBuiltinParameter(pType);
		}
		IType rt = toType(returnType);
		IFunctionType ft = fCpp ? new CPPFunctionType(rt, pTypes) : new CFunctionType(rt, pTypes);

		IBinding b = fCpp ? new CPPImplicitFunction(toCharArray(name), fScope,
				(ICPPFunctionType) ft, (ICPPParameter[]) theParms, varargs)
				: new CImplicitFunction(toCharArray(name), fScope, ft, theParms, varargs);
		fBindingList.add(b);
	}

	private char[] toCharArray(String name) {
		synchronized(CHAR_ARRAYS) {
			char[] result= CHAR_ARRAYS.get(name);
			if (result == null) {
				result= name.toCharArray();
				CHAR_ARRAYS.put(name, result);
			}
			return result;
		}
	}

	private IType toType(String type) {
		IType t= fTypeMap.get(type);
		if (t == null) {
			t= createType(type);
			fTypeMap.put(type, t);
		}
		return t;
	}

	private IType createType(final String type) {
		String tstr= type;
		if (fCpp && tstr.endsWith("&")) {
			final String nested = tstr.substring(0,  tstr.length()-1).trim();
			return new CPPReferenceType(toType(nested), false);
		}
		if (tstr.equals("FILE*")) {
			return toType("void*");
		} else if (tstr.endsWith("*")) {
			final String nested = tstr.substring(0,  tstr.length()-1).trim();
			final IType nt= toType(nested);
			return fCpp ? new CPPPointerType(nt) : new CPointerType(nt, 0);
		}
		
		boolean isConst = false;
		boolean isVolatile = false;
		if (tstr.startsWith("const ")) {
			isConst= true;
			tstr= tstr.substring(6);
		}
		if (tstr.startsWith("volatile ")) {
			isVolatile= true;
			tstr= tstr.substring(9);
		}
		int q= 0;
		if (tstr.startsWith("signed ")) {
			q |= IBasicType.IS_SIGNED;
			tstr= tstr.substring(7);
		}
		if (tstr.startsWith("unsigned ")) {
			q |= IBasicType.IS_UNSIGNED;
			tstr= tstr.substring(9);
		}
		if (tstr.startsWith("complex ")) {
			q |= IBasicType.IS_COMPLEX;
			tstr= tstr.substring(8);
		}
		if (tstr.startsWith("long long")) {
			q |= IBasicType.IS_LONG_LONG;
			tstr= tstr.substring(9).trim();
		}
		if (tstr.startsWith("long")) {
			q |= IBasicType.IS_LONG;
			tstr= tstr.substring(4).trim();
		}

		IType t;
		if (tstr.equals("void")) {
			Kind kind = Kind.eVoid;
			t = fCpp ? new CPPBasicType(kind, q) : new CBasicType(kind, q);
		} else if (tstr.equals("")) {
			Kind kind = Kind.eUnspecified;
			t = fCpp ? new CPPBasicType(kind, q) : new CBasicType(kind, q);
		} else if (tstr.equals("char")) {
			Kind kind = Kind.eChar;
			t = fCpp ? new CPPBasicType(kind, q) : new CBasicType(kind, q);
		} else if (tstr.equals("int")) {
			Kind kind = Kind.eInt;
			t = fCpp ? new CPPBasicType(kind, q) : new CBasicType(kind, q);
		} else if (tstr.equals("float")) {
			Kind kind = Kind.eFloat;
			t = fCpp ? new CPPBasicType(kind, q) : new CBasicType(kind, q);
		} else if (tstr.equals("double")) {
			Kind kind = Kind.eDouble;
			t = fCpp ? new CPPBasicType(kind, q) : new CBasicType(kind, q);
		} else if (tstr.equals("va_list")) {
			// Use 'char*(*)()'
			IType rt= toType("char*");
			t = fCpp ? new CPPPointerType(new CPPFunctionType(rt, IType.EMPTY_TYPE_ARRAY)) :
					new CPointerType(new CFunctionType(rt, IType.EMPTY_TYPE_ARRAY), 0); 
		} else if (tstr.equals("size_t")) {
			t= toType("unsigned long");
		} else {
			throw new IllegalArgumentException(type);
		}

		if (isConst || isVolatile) 
			return fCpp ? new CPPQualifierType(t, isConst, isVolatile) 
					: new CQualifierType(t, isConst, isVolatile, false);
		return t;
	}
}
