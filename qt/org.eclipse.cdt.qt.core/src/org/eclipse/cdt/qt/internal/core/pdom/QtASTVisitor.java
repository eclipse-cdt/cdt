/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.internal.core.pdom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.index.IIndexSymbols;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.parser.scanner.LocationMap;
import org.eclipse.cdt.qt.core.QtKeywords;

@SuppressWarnings("restriction")
public class QtASTVisitor extends ASTVisitor {

	private final IIndexSymbols symbols;
	private final LocationMap locationMap;

	private static final Pattern expansionParamRegex = Pattern.compile("^" + "(?:Q_ENUMS|Q_FLAGS)" + "\\s*\\((.*)\\)$");
	private static final Pattern qualNameRegex = Pattern.compile("\\s*((?:[^\\s:]+\\s*::\\s*)*[^\\s:]+).*");

	private static final Pattern declareFlagsRegex = Pattern.compile("^Q_DECLARE_FLAGS\\s*\\(\\s*([^\\s]+),\\s*([^\\s]+)\\s*\\)$");

	public QtASTVisitor(IIndexSymbols symbols, LocationMap locationMap) {
		shouldVisitDeclSpecifiers = true;

		this.symbols = symbols;
		this.locationMap = locationMap;
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

	private boolean isQObject(ICPPASTCompositeTypeSpecifier spec, IASTPreprocessorMacroExpansion[] expansions) {

		// The class definition must contain a Q_OBJECT expansion.
		for (IASTPreprocessorMacroExpansion expansion : expansions) {
			IASTPreprocessorMacroDefinition macro = expansion.getMacroDefinition();
			if (QtKeywords.Q_OBJECT.equals(String.valueOf(macro.getName())))
				return true;
		}

		return false;
	}

	private class EnumDecl {
		private final String name;
		private final boolean isFlag;
		private final IASTName refName;
		private final QtASTImageLocation location;

		public EnumDecl(String name, boolean isFlag, IASTName refName, int offset, int length) {
			this.name = name;
			this.isFlag = isFlag;
			this.refName = refName;
			this.location = new QtASTImageLocation(refName.getFileLocation(), offset, length);
		}

		public void handle(IASTPreprocessorIncludeStatement owner, ICPPASTCompositeTypeSpecifier spec, QObjectName qobjName, Map<String, String> aliases) {

			String alias = aliases.get(name);

			IBinding[] bindings = CPPSemantics.findBindingsForQualifiedName(spec.getScope(), alias == null ? name : alias);
			for(IBinding binding : bindings) {
				// Create a reference from this Qt name to the target enum's definition.
				IASTName cppName = null;
				if (binding instanceof ICPPInternalBinding) {
					IASTNode node = ((ICPPInternalBinding) binding).getDefinition();
					cppName = node instanceof IASTName ? (IASTName) node : null;
				}

				QtEnumName astName = new QtEnumName(qobjName, refName, name, cppName, location, isFlag);
				symbols.add(owner, astName, qobjName);

				if (cppName != null)
					symbols.add(owner, new ASTDelegatedName.Reference(cppName, location), astName);
			}
		}
	}

	private void handleQObject(IASTPreprocessorIncludeStatement owner, ICPPASTCompositeTypeSpecifier spec, IASTPreprocessorMacroExpansion[] expansions) {

		// Put the QObject into the symbol map.
		QObjectName qobjName = new QObjectName(spec);
		symbols.add(owner, qobjName, null);

		// There are three macros that are significant to QEnums, Q_ENUMS, Q_FLAGS, and Q_DECLARE_FLAGS.
		// All macro expansions in the QObject class definition are examined to find instances of these
		// three.  Two lists are created during this processing.  Then the those lists are uses to create
		// the QEnum instances.

		List<EnumDecl> enumDecls = new ArrayList<QtASTVisitor.EnumDecl>();
		Map<String, String> flagAliases = new HashMap<String, String>();

		for (IASTPreprocessorMacroExpansion expansion : expansions) {
			String macroName = String.valueOf(expansion.getMacroReference());
			if (QtKeywords.Q_OBJECT.equals(macroName))
				continue;
			if (QtKeywords.Q_ENUMS.equals(macroName))
				extractEnumDecls(expansion, false, enumDecls);
			else if (QtKeywords.Q_FLAGS.equals(macroName))
				extractEnumDecls(expansion, true, enumDecls);
			else if (QtKeywords.Q_DECLARE_FLAGS.equals(macroName)) {
				Matcher m = declareFlagsRegex.matcher(expansion.getRawSignature());
				if (m.matches()) {
					String flagName = m.group(1);
					String enumName = m.group(2);
					flagAliases.put(flagName, enumName);
				}
			}
		}

		for(EnumDecl decl : enumDecls)
			decl.handle(owner, spec, qobjName, flagAliases);
	}

	private void extractEnumDecls(IASTPreprocessorMacroExpansion expansion, boolean isFlag, List<EnumDecl> decls) {
		String signature = expansion.getRawSignature();
		Matcher m = expansionParamRegex.matcher(signature);
		if (!m.matches())
			return;

		IASTName refName = expansion.getMacroReference();
		String param = m.group(1);
		for(int offset = m.start(1), end = param.length(); !param.isEmpty(); offset += end, param = param.substring(end)) {
			m = qualNameRegex.matcher(param);
			if (!m.matches())
				break;

			int start = m.start(1);
			end = m.end(1);

			String enumName = m.group(1);
			decls.add(new EnumDecl(enumName, isFlag, refName, offset + start, end - start));
		}
	}
}
