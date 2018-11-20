/*
 * Copyright (c) 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndexSymbols;
import org.eclipse.cdt.core.index.IPDOMASTProcessor;
import org.eclipse.cdt.internal.core.parser.scanner.LocationMap;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public class QtPDOMASTProcessor extends IPDOMASTProcessor.Abstract {
	@Override
	public int process(IASTTranslationUnit ast, IIndexSymbols symbols) throws CoreException {
		ast.accept(new QtASTVisitor(symbols, ast.getAdapter(LocationMap.class)));
		return ILinkage.QT_LINKAGE_ID;
	}
}
