/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.parser.util.CharArrayMap;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;

/**
 * Interface that allows to implement a class-scope.
 */
public interface IPDOMCPPEnumType extends ICPPEnumeration, IPDOMBinding, IIndexType {
	/**
	 * Return the scope name, for use in {@link IScope#getScopeName()}
	 */
	IIndexName getScopeName();
	
	@Override
	IEnumerator[] getEnumerators();

	/**
	 * Called by the scope to access the enumerators.
	 */
	void loadEnumerators(CharArrayMap<PDOMCPPEnumerator> map);
}
