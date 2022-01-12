/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik
* Rapperswil, University of applied sciences and others
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation.ConstexprEvaluationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ActivationRecord;
import org.eclipse.core.runtime.CoreException;

/**
 * Assists in executing statements for constexpr evaluation
 */
public interface ICPPExecution {
	/**
	 * Instantiates the execution with the provided template parameter map and pack offset.
	 * The context is used to replace templates with their specialization, where appropriate.
	 *
	 * @return a fully or partially instantiated execution, or the original execution
	 */
	ICPPExecution instantiate(InstantiationContext context, int maxDepth);

	/**
	 * Computes the execution produced by substituting function parameters by their values.
	 *
	 * @param record maps function parameters and local variables to their values
	 * @param context the context for the current constexpr evaluation
	 * @return the computed execution
	 */
	ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context);

	/**
	 * Marshals an ICPPExecution object for storage in the index.
	 *
	 * @param  buffer The buffer that will hold the marshalled ICPPExecution object.
	 * @param  includeValue Specifies whether nested IValue objects should be marshalled as well.
	 * */
	void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException;
}
