/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

/**
 * Represents a reference to a function which cannot be resolved
 * because  an argument depends on a template parameter.
 * 
 * @since 5.6
 */
public interface ICPPDeferredFunction extends ICPPFunction {
	/**
	 * Returns the candidate functions the reference might resolve to 
	 * after template instantiation.
	 */
	public ICPPFunction[] getCandidates();
}
