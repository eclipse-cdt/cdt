/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation */
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

/**
 * @author jcamelon
 */
public interface ICParserExtensionConfiguration {

    public boolean supportStatementsInExpressions();
    public boolean supportGCCStyleDesignators();
	public boolean supportTypeofUnaryExpressions();
	public boolean supportAlignOfUnaryExpression();
	public boolean supportKnRC();
	
	/**
	 * See http://gcc.gnu.org/onlinedocs/gcc/Other-Builtins.html#Other-Builtins
	 * for more information on GCC's Other Built-in Symbols.
	 * @return
	 */
	public boolean supportGCCOtherBuiltinSymbols();

}
