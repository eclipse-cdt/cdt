/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.c.CBasicType;
import org.eclipse.cdt.internal.core.dom.parser.c.CBuiltinVariable;
import org.eclipse.cdt.internal.core.dom.parser.c.CPointerType;
import org.eclipse.cdt.internal.core.dom.parser.c.CQualifierType;

/**
 * This is the IBuiltinBindingsProvider used to implement the standard builtin bindings:
 */
public class CBuiltinSymbolProvider implements IBuiltinBindingsProvider {
	public static final ASTNodeProperty BUILTIN_SYMBOL = new ASTNodeProperty(
		"GCCBuiltinSymbolProvider.BUILTIN_SYMBOL - built-in symbol"); //$NON-NLS-1$
	
    private static final char[] __FUNC__ = "__func__".toCharArray(); //$NON-NLS-1$
    private static final int NUM_BUILTINS = 1; // the total number of builtin functions listed above
	
    static final private  IType c_char;
    static final private  IType c_const_char_p;
	static {
		c_char = new CBasicType(Kind.eChar, 0);
		c_const_char_p = new CPointerType(new CQualifierType(c_char, true, false, false), 0);
	}
    
	private IBinding[] bindings=new IBinding[NUM_BUILTINS];
	private IScope scope=null;
	public CBuiltinSymbolProvider() {
	}
	
	public IBinding[] getBuiltinBindings(IScope scope) {
		this.scope= scope;
		initialize();
		return (IBinding[])ArrayUtil.trim(IBinding.class, bindings);
	}

	private void initialize() {
        __func__();
	}
	
	private void __func__() {
		// const char * __func__;
		IBinding temp= new CBuiltinVariable(c_const_char_p, __FUNC__, scope);
		bindings = (IBinding[])ArrayUtil.append(IBinding.class, bindings, temp);
	}
}
