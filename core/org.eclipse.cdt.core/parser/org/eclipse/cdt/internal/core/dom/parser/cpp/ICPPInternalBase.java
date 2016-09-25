/*******************************************************************************
 * Copyright (c) 2007, 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * @author Bryan Wilkinson
 */
public interface ICPPInternalBase extends Cloneable {
	public Object clone();

	/**
	 * Sets the base class.
	 */
	public void setBaseClass(IBinding binding) throws DOMException;
}
