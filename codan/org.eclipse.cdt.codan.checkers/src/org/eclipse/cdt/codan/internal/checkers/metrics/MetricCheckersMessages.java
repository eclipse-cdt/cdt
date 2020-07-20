/*******************************************************************************
 * Copyright (c) 2020 Sergey Vladimirov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sergey Vladimirov - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.metrics;

import org.eclipse.osgi.util.NLS;

/**
 * Messages
 */
public class MetricCheckersMessages extends NLS {

	public static String CyclomaticComplexityChecker_countCase;
	public static String CyclomaticComplexityChecker_countConditionalExpression;
	public static String CyclomaticComplexityChecker_countDo;
	public static String CyclomaticComplexityChecker_countFor;
	public static String CyclomaticComplexityChecker_countIf;
	public static String CyclomaticComplexityChecker_countLogicalAnd;
	public static String CyclomaticComplexityChecker_countLogicalOr;
	public static String CyclomaticComplexityChecker_countSwitch;
	public static String CyclomaticComplexityChecker_countWhile;
	public static String CyclomaticComplexityChecker_maxCyclomaticComplexity;

	public static String LinesOfCodeChecker_countFuncBodyTokens;
	public static String LinesOfCodeChecker_maxLines;

	public static String NestedBlockDepthChecker_countCase;
	public static String NestedBlockDepthChecker_countDo;
	public static String NestedBlockDepthChecker_countFor;
	public static String NestedBlockDepthChecker_countIf;
	public static String NestedBlockDepthChecker_countSwitch;
	public static String NestedBlockDepthChecker_countWhile;
	public static String NestedBlockDepthChecker_maxDepth;

	static {
		NLS.initializeMessages(MetricCheckersMessages.class.getName(), MetricCheckersMessages.class);
	}

	// Do not instantiate
	private MetricCheckersMessages() {
	}
}
