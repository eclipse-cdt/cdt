/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik 
* Rapperswil, University of applied sciences and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.internal.core.dom.parser.ISerializableExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation.ConstexprEvaluationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ActivationRecord;

/**
 * Assists in executing statements for constexpr evaluation
 */
public interface ICPPExecution extends ISerializableExecution {
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
}
