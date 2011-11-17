/*******************************************************************************
 *  Copyright (c) 2006, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.upc.ast;

import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;

public interface IUPCASTElaboratedTypeSpecifier extends IUPCASTDeclSpecifier,
		ICASTElaboratedTypeSpecifier {


	@Override
	public IUPCASTElaboratedTypeSpecifier copy();
}
