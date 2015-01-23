/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Ed Swartz (Nokia)
 *    Anton Leherbauer (Wind River Systems)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.cpp;

import java.util.Map;

import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.IToken.ContextSensitiveTokenType;

/**
 * C++ parser extension configuration interface.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * Clients can subclass {@link AbstractCPPParserExtensionConfiguration} instead.
 * @noextend This interface is not intended to be extended by clients.
 * 
 * @see "http://gcc.gnu.org/onlinedocs/gcc/C-Extensions.html"
 * @see "http://gcc.gnu.org/onlinedocs/gcc/C_002b_002b-Extensions.html"
 * @since 4.0
 */
public interface ICPPParserExtensionConfiguration {

	/**
	 * Support for GNU extension "Restricting Pointer Aliasing".
	 * 
	 * @see "http://gcc.gnu.org/onlinedocs/gcc/Restricted-Pointers.html"
	 * @return <code>true</code> if support for the extension should be
	 *         enabled
	 */
	public boolean allowRestrictPointerOperators();

	/**
	 * Support for GNU extension "Extended Syntax for Template Instantiation".
	 * 
	 * @see "http://gcc.gnu.org/onlinedocs/gcc/Template-Instantiation.html"
	 * @return <code>true</code> if support for the extension should be
	 *         enabled
	 */
	public boolean supportExtendedTemplateSyntax();

	/**
	 * Support for GNU extension "Data types for complex numbers".
	 * 
	 * @see "http://gcc.gnu.org/onlinedocs/gcc/Complex.html#Complex"
	 * @return <code>true</code> if support for the extension should be
	 *         enabled
	 */
	public boolean supportComplexNumbers();

	/**
	 * Support for GNU long long types.
	 * 
	 * @see "http://gcc.gnu.org/onlinedocs/gcc/Long-Long.html"
	 * @return <code>true</code> if support for the extension should be
	 *         enabled
	 */
	public boolean supportLongLongs();

	/**
	 * Support for GNU extension "Statements and Declarations in Expressions".
	 * 
	 * @see "http://gcc.gnu.org/onlinedocs/gcc/Statement-Exprs.html"
	 * @return <code>true</code> if support for the extension should be
	 *         enabled
	 */
	public boolean supportStatementsInExpressions();

	/**
	 * Support for GNU extension "Referring to a Type with typeof".
	 * 
	 * @see "http://gcc.gnu.org/onlinedocs/gcc/Typeof.html"
	 * @return <code>true</code> if support for the extension should be
	 *         enabled
	 */
	public boolean supportTypeofUnaryExpressions();

	/**
	 * Support for GNU extension "Inquiring on Alignment of Types or Variables".
	 * 
	 * @see "http://gcc.gnu.org/onlinedocs/gcc/Alignment.html"
	 * @return <code>true</code> if support for the extension should be
	 *         enabled
	 */
	public boolean supportAlignOfUnaryExpression();

	/**
	 * Support for Kernighan and Richie (K&R) C.
	 * 
	 * @return <code>true</code> if support for K&R C should be enabled
	 */
	public boolean supportKnRC();

	/**
	 * See http://gcc.gnu.org/onlinedocs/gcc/Attribute-Syntax.html for more
	 * information on GCC's Attribute Specifiers.
	 * 
	 * @return <code>true</code> if support for the extension should be
	 *         enabled
	 */
	public boolean supportAttributeSpecifiers();

	/**
	 * Win32 compiler extensions also supported by GCC on Win32
	 * 
	 * @return <code>true</code> if support for the extension should be
	 *         enabled
	 */
	public boolean supportDeclspecSpecifiers();

	/**
	 * Provide additional built-in bindings.
	 * 
	 * @return an instance of {@link IBuiltinBindingsProvider} or
	 *         <code>null</code>
	 */
	public IBuiltinBindingsProvider getBuiltinBindingsProvider();

	/**
	 * Skips information in brackets provided at the beginning of a parameter declaration:
	 * <br>
	 * void accelerate([proc=marsh] const Speed &data);
	 * @since 5.1
	 */
	public boolean supportParameterInfoBlock();
	
	/**
	 * Support additional parameters for the sizeof operator:
	 * 'sizeof' '(' typeid ',' expression-list ')'
	 * @since 5.1
	 */
	public boolean supportExtendedSizeofOperator();
	
	/**
	 * Support function style assembler definitions:
	 * 'asm' ['volatile'] [return-type] name '(' parameter-list ')' '{' assembler-code '}'
	 * @since 5.1
	 */
	public boolean supportFunctionStyleAssembler();
	
	/**
	 * Support user-defined literal expressions:
	 * (char_expr | string_expr | int_expr | float_expr) ud-suffix
	 * @since 5.10
	 */
	public boolean supportUserDefinedLiterals();

	/**
	 * Additional variants of context-sensitive keywords.
	 * @since 5.9
	 */
	public Map<String, ContextSensitiveTokenType> getAdditionalContextSensitiveKeywords();
	
	/**
	 * @deprecated use {@link #getBuiltinBindingsProvider()} instead.
	 */
	@Deprecated
	public boolean supportGCCOtherBuiltinSymbols();
	
	/**
	 * @deprecated use {@link IScannerExtensionConfiguration#supportMinAndMaxOperators()}, instead.     
	 */
	@Deprecated
	public boolean supportMinAndMaxOperators();
	
	/**
	 * @deprecated configure extra keywords, via {@link IScannerExtensionConfiguration#getAdditionalKeywords()}      
	 */
	@Deprecated
	public boolean supportRestrictKeyword();
}
