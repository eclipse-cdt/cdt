/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
 *
 *******************************************************************************/
package org.eclipse.cdt.ui.text;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface to be implemented by contributors to the extension point
 * <code>org.eclipse.cdt.ui.quickFixProcessors</code>.
 *
 * @since 5.0
 */
public interface IQuickFixProcessor {
	/**
	 * Returns <code>true</code> if the processor has proposals for the given problem. This test should be an
	 * optimistic guess and be very cheap.
	 *
	 * @param unit the compilation unit
	 * @param problemId the problem Id. The id is of a problem of the problem type(s) this processor specified in
	 * the extension point.
	 * @return <code>true</code> if the processor has proposals for the given problem
	 */
	boolean hasCorrections(ITranslationUnit unit, int problemId);

	/**
	 * Collects corrections or code manipulations for the given context.
	 *
	 * @param context Defines current compilation unit, position and a shared AST
	 * @param locations Problems are the current location.
	 * @return the corrections applicable at the location or <code>null</code> if no proposals
	 * 			can be offered
	 * @throws CoreException CoreException can be thrown if the operation fails
	 */
	ICCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations)
			throws CoreException;
}
