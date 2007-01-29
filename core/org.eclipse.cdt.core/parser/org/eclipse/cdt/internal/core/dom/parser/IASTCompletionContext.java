/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * Interface for a code completion's context. Used for context-sensitive
 * finding of bindings with a certain prefix.
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @author Bryan Wilkinson
 * @since 4.0
 */
public interface IASTCompletionContext {
	
	/**
	 * Returns bindings that start with the given prefix, only considering those
	 * that are valid for this context.
	 * 
	 * @param n the name containing a prefix
	 * @return valid bindings in this context for the given prefix
	 */
	IBinding[] resolvePrefix(IASTName n);
}
