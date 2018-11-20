/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

/**
 * Constants for supported preprocessor directive types.
 *
 * @since 4.0
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPreprocessorDirective {

	/**
	 * Special constant indicating to ignore the preprocessor directive.
	 */
	public static final int ppIgnore = -2;

	/**
	 * Special constant indicating to mark the preprocessor directive as invalid.
	 */
	public static final int ppInvalid = -1;

	/**
	 * Standard preprocessor directive <code>#if</code>.
	 */
	public static final int ppIf = 0;

	/**
	 * Standard preprocessor directive <code>#ifdef</code>.
	 */
	public static final int ppIfdef = 1;

	/**
	 * Standard preprocessor directive <code>#ifndef</code>.
	 */
	public static final int ppIfndef = 2;

	/**
	 * Standard preprocessor directive <code>#elif</code>.
	 */
	public static final int ppElif = 3;

	/**
	 * Standard preprocessor directive <code>#else</code>.
	 */
	public static final int ppElse = 4;

	/**
	 * Standard preprocessor directive <code>#endif</code>.
	 */
	public static final int ppEndif = 5;

	/**
	 * Standard preprocessor directive <code>#include</code>.
	 */
	public static final int ppInclude = 6;

	/**
	 * Standard preprocessor directive <code>#define</code>.
	 */
	public static final int ppDefine = 7;

	/**
	 * Standard preprocessor directive <code>#undef</code>.
	 */
	public static final int ppUndef = 8;

	/**
	 * Standard preprocessor directive <code>#error</code>.
	 */
	public static final int ppError = 9;

	/**
	 * Standard preprocessor directive <code>#pragma</code>.
	 */
	public static final int ppPragma = 10;

	/**
	 * GNU preprocessor extension <code>#include_next</code>.
	 * Search include file after the directory of the current file.
	 */
	public static final int ppInclude_next = 11;

	/**
	 * GNU preprocessor extension <code>#import</code>.
	 * Include only once.
	 */
	public static final int ppImport = 12;

	/**
	 * GNU preprocessor extension <code>#warning</code>.
	 * Similar to <code>#error</code>.
	 */
	public static final int ppWarning = 13;

}
