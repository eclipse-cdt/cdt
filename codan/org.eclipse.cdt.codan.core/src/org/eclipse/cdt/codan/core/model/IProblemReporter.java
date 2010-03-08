/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

/**
 * IProblemReporter - interface to report problems
 * 
 * Clients may implement and extend this interface
 */
public interface IProblemReporter {
	public static final String GENERIC_CODE_ANALYSIS_MARKER_TYPE = "org.eclipse.cdt.codan.core.codanProblem"; //$NON-NLS-1$

	/**
	 * Report a problem with "problemId" id on the location determined by "loc", 
	 * using problem specific error message customized by args.
	 * @param problemId - id of the problem registered with a checker
	 * @param loc - location object
	 * @param args - custom arguments, can be null, in this case default message is reported
	 */
	public void reportProblem(String problemId, IProblemLocation loc, Object... args);
}