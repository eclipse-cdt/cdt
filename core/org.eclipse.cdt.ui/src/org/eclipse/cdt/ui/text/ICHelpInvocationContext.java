/**********************************************************************
 * Copyright (c) 2004, 2009 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.ui.text;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IProject;

/**
 * Invocation context for the CHelpProviderManager.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 *
 * @see IHoverHelpInvocationContext
 * @see IContentAssistHelpInvocationContext
 */
public interface ICHelpInvocationContext {

	/**
	 * @return the project
	 */
	IProject getProject();

	/**
	 * @return ITranslationUnit or null
	 */
	ITranslationUnit getTranslationUnit();

}
