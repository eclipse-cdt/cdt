/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

/**
 * Interface for checkers that can be run when user is typing, checker has to be
 * very quick to run in this mode
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 * </p>
 *
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IRunnableInEditorChecker {
	/**
	 * @param model the model to check.
	 * @param context container object for sharing data between different checkers
	 * 		operating on the model.
	 * @since 2.0
	 */
	void processModel(Object model, ICheckerInvocationContext context);
}
