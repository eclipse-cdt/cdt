/*******************************************************************************
 * Copyright (c) 2008 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergey Prigogin (Google) - initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassInstance;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

/**
 * @author Sergey Prigogin
 */
class CompositeCPPUnknownClassInstance extends CompositeCPPUnknownClassType
		implements ICPPUnknownClassInstance {
	
	public CompositeCPPUnknownClassInstance(ICompositesFactory cf,
			ICPPUnknownClassInstance rbinding) {
		super(cf, rbinding);
	}

	public IType[] getArguments() {
		IType[] arguments = ((ICPPUnknownClassInstance) rbinding).getArguments();
		try {
			for (int i = 0; i < arguments.length; i++) {
				arguments[i] = cf.getCompositeType((IIndexType) arguments[i]);
			}
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
		return arguments;
	}
}
