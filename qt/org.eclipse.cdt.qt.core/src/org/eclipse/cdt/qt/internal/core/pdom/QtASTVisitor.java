package org.eclipse.cdt.qt.internal.core.pdom;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.index.IIndexSymbols;
import org.eclipse.cdt.internal.core.parser.scanner.LocationMap;
import org.eclipse.cdt.qt.core.QtKeywords;

@SuppressWarnings("restriction")
public class QtASTVisitor extends ASTVisitor {

	private final IIndexSymbols symbols;
	private final LocationMap locationMap;

	public QtASTVisitor(IIndexSymbols symbols, LocationMap locationMap) {
		shouldVisitDeclSpecifiers = true;

		this.symbols = symbols;
		this.locationMap = locationMap;
	}

	private boolean isQObject(ICPPASTCompositeTypeSpecifier spec, IASTPreprocessorMacroExpansion[] expansions) {

		// The class definition must contain a Q_OBJECT expansion.
		for (IASTPreprocessorMacroExpansion expansion : expansions) {
			IASTPreprocessorMacroDefinition macro = expansion.getMacroDefinition();
			if (QtKeywords.Q_OBJECT.equals(String.valueOf(macro.getName())))
				return true;
		}

		return false;
	}

	private void handleQObject(IASTPreprocessorIncludeStatement owner, ICPPASTCompositeTypeSpecifier spec, IASTPreprocessorMacroExpansion[] expansions) {
		// Put the QObject into the symbol map.
		QObjectName qobjName = new QObjectName(spec);
		symbols.add(owner, qobjName, null);
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
			ICPPASTCompositeTypeSpecifier spec = (ICPPASTCompositeTypeSpecifier) declSpec;

			IASTFileLocation loc = spec.getFileLocation();
			IASTPreprocessorIncludeStatement owner = loc == null ? null : loc.getContextInclusionStatement();

			IASTPreprocessorMacroExpansion[] expansions = locationMap.getMacroExpansions(loc);

			if (isQObject(spec, expansions))
				handleQObject(owner, spec, expansions);
		}

		return super.visit(declSpec);
	}
}
