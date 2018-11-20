/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTImplicitDestructorNameOwner;

/**
 * Represents a block of statements in C++.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.10
 */
public interface ICPPASTCompoundStatement extends IASTCompoundStatement, IASTImplicitDestructorNameOwner {
}
