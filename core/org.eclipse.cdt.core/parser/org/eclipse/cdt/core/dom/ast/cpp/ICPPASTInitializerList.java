/*******************************************************************************
 * Copyright (c) 2009, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTInitializerList;

/**
 * Braced initializer list. 
 *
 * @since 5.2
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTInitializerList extends IASTInitializerList, ICPPASTPackExpandable {

	@Override
	ICPPASTInitializerList copy();

	/**
	 * @since 5.3
	 */
	@Override
	ICPPASTInitializerList copy(CopyStyle style);
}
