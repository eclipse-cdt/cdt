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

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown;

/*
 * @author Sergey Prigogin
 */
public interface ICPPInternalUnknownClassType extends ICPPClassType, ICPPInternalUnknown {
	/**
	 * Resolves unknown type to another unknown type that is a step closer to the final
	 * name resolution.
	 * @param parentBinding a new parent binding, usually a result of partial resolution
	 *        of the original parent binding.
	 * @param argMap template argument map.
	 * @return a partially resolved, but still unknown, binding.
	 */
    public IBinding resolvePartially(ICPPInternalUnknown parentBinding, ObjectMap argMap);
}
