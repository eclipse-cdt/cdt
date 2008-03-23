/*******************************************************************************
 * Copyright (c) 2008 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;

/*
 * Represents a partially instantiated C++ class template, declaration of which is not yet available.
 *
 * @author Sergey Prigogin
 */
public interface ICPPInternalUnknownClassInstance extends ICPPClassTemplate, ICPPInternalUnknown,
		ICPPInternalClassTemplate {
	public IType[] getArguments();
}
