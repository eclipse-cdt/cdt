/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Ed Swartz (Nokia)
 *    Anton Leherbauer (Wind River Systems)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.c;

import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;

/**
 * C parser extension configuration interface.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * Clients can subclass {@link AbstractCParserExtensionConfiguration} instead.
 * @noextend This interface is not intended to be extended by clients.
 *
 * @see "http://gcc.gnu.org/onlinedocs/gcc/C-Extensions.html"
 * @since 4.0
 */
public interface ICParserExtensionConfiguration {

	/**
	 * Support for GNU extension "Statements and Declarations in Expressions".
	 *
	 * @see "http://gcc.gnu.org/onlinedocs/gcc/Statement-Exprs.html"
	 * @return <code>true</code> if support for the extension should be
	 *         enabled
	 */
	public boolean supportStatementsInExpressions();

	/**
	 * Support for GNU extension "Designated Initializers".
	 *
	 * @see "http://gcc.gnu.org/onlinedocs/gcc/Designated-Inits.html"
	 * @return <code>true</code> if support for the extension should be
	 *         enabled
	 */
	public boolean supportGCCStyleDesignators();

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
	 * @deprecated use {@link #getBuiltinBindingsProvider()} instead.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public boolean supportGCCOtherBuiltinSymbols();
}
