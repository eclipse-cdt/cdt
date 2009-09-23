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
 */
public interface IProblemReporter {
	public static final String GENERIC_CODE_ANALYSIS_MARKER_TYPE = "org.eclipse.cdt.codan.core.codanProblem";
	/**
	 * Report a problem with "problemId" id on location determined by "loc", 
	 * using problem specific error message customised by args.
	 * @param problemId - id of the problem registers with checker
	 * @param loc - location object
	 * @param args - custom args, can be null, in this case default message is reported
	 */
	public void reportProblem(String problemId,  IProblemLocation loc,
			Object ... args);
}