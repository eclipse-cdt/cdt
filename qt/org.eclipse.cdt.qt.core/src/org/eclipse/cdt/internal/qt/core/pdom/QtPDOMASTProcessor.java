/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		ast.accept(new QtASTVisitor(symbols, (LocationMap) ast.getAdapter(LocationMap.class)));
		return ILinkage.QT_LINKAGE_ID;
	}
}
