package org.eclipse.cdt.qt.internal.core.pdom;

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
