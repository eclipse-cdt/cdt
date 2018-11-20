/*******************************************************************************
 *  Copyright (c) 2006, 2011 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.upc.ast;

import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;

public interface IUPCASTElaboratedTypeSpecifier extends IUPCASTDeclSpecifier, ICASTElaboratedTypeSpecifier {

	@Override
	public IUPCASTElaboratedTypeSpecifier copy();
}
