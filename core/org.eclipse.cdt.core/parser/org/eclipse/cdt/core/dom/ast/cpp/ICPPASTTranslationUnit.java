/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTTranslationUnit extends IASTTranslationUnit {

	/**
	 * Resolve the binding for translation unit.
	 * 
	 * @return <code>IBinding</code>
	 */
	public IBinding resolveBinding();

	
	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTTranslationUnit copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTTranslationUnit copy(CopyStyle style);
}
