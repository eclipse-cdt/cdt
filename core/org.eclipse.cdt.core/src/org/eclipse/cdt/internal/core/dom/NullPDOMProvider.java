/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom;

import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.IPDOMProvider;
import org.eclipse.core.resources.IProject;

/**
 * @author Doug Schaefer
 *
 */
public class NullPDOMProvider implements IPDOMProvider {

	public IPDOM getPDOM(IProject project) {
		// by default return null.
		return null;
	}
	
}
