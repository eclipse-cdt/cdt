/*******************************************************************************
 * Copyright (c) 2009, 2014 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

/**
 * IProblemReporter - interface to report problems.
 *
 * It is recommended to extend {@link AbstractProblemReporter} instead
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same.
 * </p>
 */
public interface IProblemReporter {
	/**
	 * id of generic codan problem marker
	 */
	public static final String GENERIC_CODE_ANALYSIS_MARKER_TYPE = "org.eclipse.cdt.codan.core.codanProblem"; //$NON-NLS-1$

	/**
	 * Report a problem with "problemId" id on the location determined by "loc",
	 * using problem specific error message customized by args.
	 *
	 * @param problemId - id of the problem registered with a checker
	 * @param loc - location object, can be created using
	 *        getRuntime().getProblemLocationFactory().createProblemLocation
	 *        methods
	 * @param args - custom arguments, can be none, in this case default message
	 *        is reported
	 */
	public void reportProblem(String problemId, IProblemLocation loc, Object... args);
}