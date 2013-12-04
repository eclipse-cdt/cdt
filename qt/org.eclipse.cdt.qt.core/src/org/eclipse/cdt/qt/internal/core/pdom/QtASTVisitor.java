/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.internal.core.pdom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
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
	// A regular expression for matching qualified names.  This allows for optional global qualification
	// (leading ::) and then separates the first part of the name from the rest (if present).  There are
	// three capture groups:
	//   (1) If the input name specifies the global namespace (leading ::) then capture group 1 will
	//       be ::.  Group 1 will be null otherwise.
	//   (2) The text of the first component of the qualified name, including leading :: if present in
	//       the input string.  Leading and trailing whitespace is trimmed.  There is no effort to check
	//       that the name contains valid C++ identifier characters.
	//   (3) The text of everything after the first component of the qualified name.
	//
	// E.g., -- Input Name --   ---- Capture Groups ----
	//       "::nsA::nsB::b" => { "::", "nsA", "nsB::b" }
	//       "a"             => { null, "a",   null     }
	//       "::  i"         => { "::", "i",   null     }
	private static final Pattern QUALNAME_REGEX = Pattern.compile("^\\s*(::)?\\s*([^\\s:]+)\\s*(?:::(.*))?$"); //$NON-NLS-1$

	private static final Pattern qualNameRegex = Pattern.compile("\\s*((?:[^\\s:]+\\s*::\\s*)*[^\\s:]+).*");

	private static final Pattern declareFlagsRegex = Pattern.compile("^Q_DECLARE_FLAGS\\s*\\(\\s*([^\\s]+),\\s*([^\\s]+)\\s*\\)$");

	/**
	 * A regular expression for scanning the Q_CLASSINFO expansion and extracting the
	 * expansion parameter key and value.  It provides the following capture groups:
	 * <br>1 - the key
	 * <br>2 - the value
	 * <p>
	 * The key must not have embedded quotes.
	 */
	private static final Pattern classInfoRegex = Pattern.compile("^Q_CLASSINFO\\s*\\(\\s*\"([^\"]+)\"\\s*,\\s*\"(.*)\"\\s*\\)$");

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

			IBinding[] bindings = findBindingsForQualifiedName(spec.getScope(), alias == null ? name : alias);
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
			} else if(QtKeywords.Q_CLASSINFO.equals(macroName)) {
				Matcher m = classInfoRegex.matcher(expansion.getRawSignature());
				if (m.matches()) {
					String key = m.group(1);
					String value = m.group(2);
					qobjName.addClassInfo(key, value);
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

	private static IScope getLookupScope(IASTNode node) {
		if (node == null)
			return null;

		if (node instanceof IASTCompositeTypeSpecifier)
			return ((IASTCompositeTypeSpecifier) node).getScope();

		if (node instanceof ICPPASTNamespaceDefinition)
			return ((ICPPASTNamespaceDefinition) node).getScope();

		if (!(node instanceof ICPPInternalBinding))
			return null;

		IASTNode defn = ((ICPPInternalBinding) node).getDefinition();
		if (defn == null)
			return null;

		return getLookupScope(defn.getParent());
	}

	private static IScope getLookupScope(IBinding binding) {
		if (binding == null)
			return null;

		if (binding instanceof IASTCompositeTypeSpecifier)
			return ((IASTCompositeTypeSpecifier) binding).getScope();

		if (!(binding instanceof ICPPInternalBinding))
			return null;

		IASTNode defn = ((ICPPInternalBinding) binding).getDefinition();
		if (defn == null)
			return null;

		return getLookupScope(defn.getParent());
	}

	/**
	 * Use C++ lookup semantics to find the possible bindings for the given qualified name starting
	 * in the given scope.
	 */
	public static IBinding[] findBindingsForQualifiedName(IScope scope, String qualifiedName) {
		// Return immediately if the qualifiedName does not match a known format.
		Matcher m = QUALNAME_REGEX.matcher(qualifiedName);
		if (!m.matches())
			return IBinding.EMPTY_BINDING_ARRAY;

		// If the qualified name is rooted in the global namespace, then navigate to that scope.
		boolean isGlobal = m.group(1) != null;
		if (isGlobal) {
			IScope global = scope;
			try {
				while(global.getParent() != null)
					global = global.getParent();
			} catch(DOMException e) {
				CCorePlugin.log(e);
			}
			scope = global;
		}

		Set<IBinding> bindings = new HashSet<IBinding>();

		// Look for the name in the given scope.
		findBindingsForQualifiedName(scope, qualifiedName, bindings);

		// If the qualified name is not rooted in the global namespace (with a leading ::), then
		// look at all parent scopes.
		if (!isGlobal)
			try {
				while(scope != null) {
					scope = scope.getParent();
					if (scope != null)
						findBindingsForQualifiedName(scope, qualifiedName, bindings);
				}
			} catch (DOMException e) {
				CCorePlugin.log(e);
			}

		return bindings.size() <= 0 ? IBinding.EMPTY_BINDING_ARRAY : bindings.toArray(new IBinding[bindings.size()]);
	}

	private static void findBindingsForQualifiedName(IScope scope, String qualifiedName, Collection<IBinding> bindings) {
		// Split the qualified name into the first part (before the first :: qualifier) and the rest. All
		// bindings for the first part are found and their scope is used to find the rest of the name.  When
		// the call tree gets to a leaf (non-qualified name) then a simple lookup happens and all matching
		// bindings are added to the result.

		Matcher m = QUALNAME_REGEX.matcher(qualifiedName);
		if (!m.matches())
			return;

		String part1 = m.group(2);
		String part2 = m.group(3);

		// When we're down to a single component name, then use the normal lookup method.
		if (part2 == null || part2.isEmpty()) {
			bindings.addAll(Arrays.asList(CPPSemantics.findBindings(scope, part1, false)));
			return;
		}

		// Find all bindings that match the first part of the name.  For each such binding,
		// lookup the second part of the name.
		for(IBinding binding : CPPSemantics.findBindings(scope, part1, false))
			findBindingsForQualifiedName(getLookupScope(binding), part2, bindings);
	}
}
