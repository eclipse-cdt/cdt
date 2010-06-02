/**********************************************************************
 * Copyright (c) 2004, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
