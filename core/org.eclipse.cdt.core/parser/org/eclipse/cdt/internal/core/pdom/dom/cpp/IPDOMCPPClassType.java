/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface that allows to implement a class-scope.
 */
public interface IPDOMCPPClassType extends ICPPClassType, IPDOMBinding, IIndexType {

	/**
	 * Visits the children of the class type without using the cache. This method is
	 * used to populate the cache.
	 */
	void acceptUncached(IPDOMVisitor visitor) throws CoreException;

	/**
	 * Returns the scope name, for use in {@link IScope#getScopeName()}
	 */
	IIndexName getScopeName();
}
