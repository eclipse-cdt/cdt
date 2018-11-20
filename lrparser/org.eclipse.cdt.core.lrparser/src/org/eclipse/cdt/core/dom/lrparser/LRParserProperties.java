/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser;

@SuppressWarnings("nls")
public final class LRParserProperties {

	/**
	 * The LR parsers do not actually skip the parsing of function bodies,
	 * but this option does have the effect of not generating AST nodes
	 * for function bodies.
	 *
	 * TODO this is not implemented yet in the LR parser
	 *
	 * Possible values: "true", null
	 */
	public static final String SKIP_FUNCTION_BODIES = "org.eclipse.cdt.core.dom.lrparser.skipFunctionBodies";

	/**
	 * Instructs the parser not to create AST nodes for expressions
	 * within aggregate initializers when they do not contain names.
	 *
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=253690
	 *
	 * Possible values: "true", null
	 */
	public static final String SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS = "org.eclipse.cdt.core.dom.lrparser.skipTrivialExpressionsInAggregateInitializers";

	/**
	 * The location of the translation unit as given by the CodeReader.
	 */
	public static final String TRANSLATION_UNIT_PATH = "org.eclipse.cdt.core.dom.lrparser.translationUnitPath";

}
