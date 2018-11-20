/*******************************************************************************
 * Copyright (c) 2007, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.core.runtime.CoreException;

/**
 * Interface for any element in the PDOM that can have the same name as
 * a sibling, but differ in other ways (i.e. function parameters, template
 * arguments).
 *
 * @author Bryan Wilkinson
 */
public interface IPDOMOverloader {
	/**
	 * @return the signature hash for this PDOM element, which will be unique
	 * for all sibling IPDOMOverloaders with the same name.
	 * @throws CoreException
	 */
	public int getSignatureHash() throws CoreException;
}
