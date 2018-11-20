/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.lrparser.xlc.ast;

import org.eclipse.cdt.core.lrparser.xlc.ast.IXlcCASTVectorTypeSpecifier;
import org.eclipse.cdt.core.lrparser.xlc.ast.IXlcCNodeFactory;
import org.eclipse.cdt.internal.core.dom.parser.c.CNodeFactory;

@SuppressWarnings("restriction")
public class XlcCNodeFactory extends CNodeFactory implements IXlcCNodeFactory {

	private static final XlcCNodeFactory DEFAULT_INSTANCE = new XlcCNodeFactory();

	public static XlcCNodeFactory getDefault() {
		return DEFAULT_INSTANCE;
	}

	@Override
	public IXlcCASTVectorTypeSpecifier newVectorTypeSpecifier() {
		return new XlcCASTVectorTypeSpecifier();
	}
}
