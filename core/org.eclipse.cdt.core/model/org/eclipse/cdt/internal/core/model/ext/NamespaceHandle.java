/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model.ext;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.INamespace;

public class NamespaceHandle extends CElementHandle implements INamespace {

	public NamespaceHandle(ICElement parent, ICPPNamespace ns) {
		super(parent, ICElement.C_NAMESPACE, ns.getName());
	}

	public NamespaceHandle(ICElement parent, String name) {
		super(parent, ICElement.C_NAMESPACE, name);
	}
}
