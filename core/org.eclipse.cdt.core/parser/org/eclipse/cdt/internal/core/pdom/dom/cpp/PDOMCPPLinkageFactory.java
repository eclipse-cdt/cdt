/*******************************************************************************
 * Copyright (c) 2005, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMCPPLinkageFactory implements IPDOMLinkageFactory {

	@Override
	public PDOMLinkage getLinkage(PDOM pdom, long record) {
		return new PDOMCPPLinkage(pdom, record);
	}

	@Override
	public PDOMLinkage createLinkage(PDOM pdom) throws CoreException {
		return new PDOMCPPLinkage(pdom);
	}
	
}
