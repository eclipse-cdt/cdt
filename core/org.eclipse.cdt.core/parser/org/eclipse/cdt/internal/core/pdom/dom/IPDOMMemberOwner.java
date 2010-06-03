/*******************************************************************************
 * Copyright (c) 2006, 2008 Symbian Software and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface for PDOM entities that contain members. Note this is not a generic
 */
public interface IPDOMMemberOwner {
	public void accept(IPDOMVisitor visitor) throws CoreException;
	public void addChild(PDOMNode member) throws CoreException;
}
