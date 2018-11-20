/*******************************************************************************
 * Copyright (c) 2007, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom;

public class IndexerStatistics {
	public int fResolutionTime;
	public int fParsingTime;
	public int fAddToIndexTime;
	public int fErrorCount;
	public int fReferenceCount = 0;
	public int fDeclarationCount = 0;
	public int fProblemBindingCount = 0;
	public int fUnresolvedIncludesCount = 0;
	public int fPreprocessorProblemCount = 0;
	public int fSyntaxProblemsCount = 0;
	public int fTooManyTokensCount = 0;
}
