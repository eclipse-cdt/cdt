/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IIndexSymbols;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.parser.scanner.LocationMap;
import org.eclipse.cdt.internal.qt.core.ASTUtil;
import org.eclipse.cdt.internal.qt.core.QtFunctionCall;
import org.eclipse.cdt.internal.qt.core.QtKeywords;
import org.eclipse.cdt.internal.qt.core.QtMethodReference;
import org.eclipse.cdt.internal.qt.core.QtMethodUtil;
import org.eclipse.cdt.internal.qt.core.index.IQMethod;
import org.eclipse.cdt.internal.qt.core.index.IQProperty;
import org.eclipse.cdt.internal.qt.core.index.QProperty;

@SuppressWarnings("restriction")
public class QtASTVisitor extends ASTVisitor {

	private final IIndexSymbols symbols;
	private final LocationMap locationMap;

	private static final Pattern expansionParamRegex = Pattern.compile("^(?:Q_ENUMS|Q_FLAGS)\\s*\\((.*)\\)$",
			Pattern.DOTALL);
	private static final Pattern qualNameRegex = Pattern.compile("\\s*((?:[^\\s:]+\\s*::\\s*)*[^\\s:]+).*");

	private static final Pattern declareFlagsRegex = Pattern
			.compile("^Q_DECLARE_FLAGS\\s*\\(\\s*([^\\s]+),\\s*([^\\s]+)\\s*\\)$", Pattern.DOTALL);

	/**
	 * A regular expression for scanning the Q_CLASSINFO expansion and extracting the
	 * expansion parameter key and value.  It provides the following capture groups:
	 * <br>1 - the key
	 * <br>2 - the value
	 * <p>
	 * The key must not have embedded quotes.
	 */
	private static final Pattern classInfoRegex = Pattern
			.compile("^Q_CLASSINFO\\s*\\(\\s*\"([^\"]+)\"\\s*,\\s*\"(.*)\"\\s*\\)$", Pattern.DOTALL);

	private static final Pattern leadingWhitespaceRegex = Pattern.compile("^\\s*([^\\s].*)$");

	private static final Pattern qPropertyRegex = Pattern.compile(
			"^Q_PROPERTY\\s*\\(\\s*(.+?)\\s*([a-zA-Z_][\\w]*+)(?:(?:\\s+(READ\\s+.*))|\\s*)\\s*\\)$", Pattern.DOTALL);

	/**
	 * A regular expression for scanning Q_PROPERTY attributes.  The regular expression is built
	 * from the values defined in IQProperty#Attribute.  It looks like:
	 * <pre>
	 * (:?READ)|(?:WRITE)|(:?RESET)|...
	 * </pre>
	 * This regular expression is used to recognize valid attributes while scanning the
	 * Q_PROPERTY macro expansion.
	 *
	 * @see QProperty#scanAttributes(String)
	 */
	private static final Pattern qPropertyAttributeRegex;
	static {
		StringBuilder regexBuilder = new StringBuilder();
		for (IQProperty.Attribute attr : IQProperty.Attribute.values()) {
			if (attr.ordinal() > 0)
				regexBuilder.append('|');
			regexBuilder.append("(:?");
			regexBuilder.append(attr.identifier);
			regexBuilder.append(")");
		}
		qPropertyAttributeRegex = Pattern.compile(regexBuilder.toString());
	}

	public QtASTVisitor(IIndexSymbols symbols, LocationMap locationMap) {
		shouldVisitDeclSpecifiers = true;
		shouldVisitExpressions = true;

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
			if (isQGadget(spec, expansions))
				handleQClass(owner, spec, new QGadgetName(spec), expansions);
		}

		return super.visit(declSpec);
	}

	@Override
	public int visit(IASTExpression expr) {
		if (expr instanceof IASTFunctionCallExpression) {
			IASTFunctionCallExpression call = (IASTFunctionCallExpression) expr;

			// See if this is a QObject::connect or disconnect function call.
			Collection<QtMethodReference> refs = QtFunctionCall.getReferences(call);
			if (refs != null)
				for (IASTName ref : refs) {
					IASTFileLocation nameLoc = ref.getFileLocation();
					if (nameLoc != null) {
						IASTPreprocessorIncludeStatement owner = nameLoc.getContextInclusionStatement();
						symbols.add(owner, ref, null);
					}
				}

			// See if this is a qmlRegisterType or qmlRegisterUncreatableType function call.
			ICPPTemplateInstance templateFn = ASTUtil.resolveFunctionBinding(ICPPTemplateInstance.class, call);
			if (QtKeywords.is_QmlType(templateFn)) {
				IASTName fnName = null;
				IASTExpression fnNameExpr = call.getFunctionNameExpression();
				if (fnNameExpr instanceof IASTIdExpression) {
					fnName = ((IASTIdExpression) fnNameExpr).getName();
				}
				IASTFileLocation nameLoc = call.getFileLocation();
				if (nameLoc != null) {
					QmlTypeRegistration qmlTypeReg = new QmlTypeRegistration(fnName, templateFn, call);

					IASTPreprocessorIncludeStatement owner = nameLoc.getContextInclusionStatement();
					symbols.add(owner, qmlTypeReg, null);

					// the Qt data references the C++ function template instance specialization
					if (fnName != null)
						symbols.add(owner, new ASTNameReference(fnName), qmlTypeReg);
				}
			}
		}

		return super.visit(expr);
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

	private boolean isQGadget(ICPPASTCompositeTypeSpecifier spec, IASTPreprocessorMacroExpansion[] expansions) {

		// The class definition must contain a Q_GADGET expansion.
		for (IASTPreprocessorMacroExpansion expansion : expansions) {
			IASTPreprocessorMacroDefinition macro = expansion.getMacroDefinition();
			if (QtKeywords.Q_GADGET.equals(String.valueOf(macro.getName())))
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

		public void handle(IASTPreprocessorIncludeStatement owner, ICPPASTCompositeTypeSpecifier spec,
				IQtASTName qobjName, Map<String, String> aliases) {

			String alias = aliases.get(name);

			IBinding[] bindings = CPPSemantics.findBindingsForQualifiedName(spec.getScope(),
					alias == null ? name : alias);
			for (IBinding binding : bindings) {
				// Create a reference from this Qt name to the target enum's definition.
				IASTName cppName = findASTName(binding);
				QtEnumName astName = new QtEnumName(qobjName, refName, name, cppName, location, isFlag);
				symbols.add(owner, astName, qobjName);

				if (cppName != null)
					symbols.add(owner, new ASTNameReference(cppName, location), astName);
			}
		}
	}

	private void handleQObject(IASTPreprocessorIncludeStatement owner, ICPPASTCompositeTypeSpecifier spec,
			IASTPreprocessorMacroExpansion[] expansions) {

		// Put the QObject into the symbol map.
		QObjectName qobjName = new QObjectName(spec);
		handleQClass(owner, spec, qobjName, expansions);

		for (IASTPreprocessorMacroExpansion expansion : expansions) {

			IASTName name = expansion.getMacroReference();
			String macroName = name == null ? null : name.toString();
			if (QtKeywords.Q_OBJECT.equals(macroName))
				continue;

			if (QtKeywords.Q_CLASSINFO.equals(macroName)) {
				Matcher m = classInfoRegex.matcher(expansion.getRawSignature());
				if (m.matches()) {
					String key = m.group(1);
					String value = m.group(2);
					qobjName.addClassInfo(key, value);
				}
			} else if (QtKeywords.Q_PROPERTY.equals(macroName))
				handleQPropertyDefn(owner, qobjName, expansion);
		}

		// Process the slot, signal, and invokable method declarations.
		extractQMethods(owner, spec, qobjName);
	}

	private void handleQClass(IASTPreprocessorIncludeStatement owner, ICPPASTCompositeTypeSpecifier spec,
			IQtASTName qtName, IASTPreprocessorMacroExpansion[] expansions) {

		// Put the Qt name into the symbol map.
		symbols.add(owner, qtName, null);

		// The QClass contains a reference to the C++ class that it annotates.
		symbols.add(owner, new ASTNameReference(spec.getName()), qtName);

		// There are three macros that are significant to QEnums, Q_ENUMS, Q_FLAGS, and Q_DECLARE_FLAGS.
		// All macro expansions in the QObject class definition are examined to find instances of these
		// three.  Two lists are created during this processing.  Then those lists are uses to create
		// the QEnum instances.

		List<EnumDecl> enumDecls = new ArrayList<>();
		Map<String, String> flagAliases = new HashMap<>();

		for (IASTPreprocessorMacroExpansion expansion : expansions) {

			IASTName name = expansion.getMacroReference();
			String macroName = name == null ? null : name.toString();
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

		for (EnumDecl decl : enumDecls)
			decl.handle(owner, spec, qtName, flagAliases);
	}

	private void extractEnumDecls(IASTPreprocessorMacroExpansion expansion, boolean isFlag, List<EnumDecl> decls) {
		String signature = expansion.getRawSignature();
		Matcher m = expansionParamRegex.matcher(signature);
		if (!m.matches())
			return;

		IASTName refName = expansion.getMacroReference();
		String param = m.group(1);
		for (int offset = m.start(1), end = param.length(); !param.isEmpty(); offset += end, param = param
				.substring(end)) {
			m = qualNameRegex.matcher(param);
			if (!m.matches())
				break;

			int start = m.start(1);
			end = m.end(1);

			String enumName = m.group(1);
			decls.add(new EnumDecl(enumName, isFlag, refName, offset + start, end - start));
		}
	}

	private void handleQPropertyDefn(IASTPreprocessorIncludeStatement owner, QObjectName qobjName,
			IASTPreprocessorMacroExpansion expansion) {
		Matcher m = qPropertyRegex.matcher(expansion.getRawSignature());
		if (!m.matches())
			return;

		String type = m.group(1);
		String name = m.group(2);

		int nameStart = m.start(2);
		int nameEnd = m.end(2);

		IASTName refName = expansion.getMacroReference();
		QtASTImageLocation location = new QtASTImageLocation(refName.getFileLocation(), nameStart, nameEnd - nameStart);

		QtPropertyName propertyName = new QtPropertyName(qobjName, refName, name, location);
		propertyName.setType(type);
		qobjName.addProperty(propertyName);
		symbols.add(owner, propertyName, qobjName);

		// Create nodes for all the attributes.
		AttrValue[] values = new AttrValue[IQProperty.Attribute.values().length];
		String attributes = m.group(3);
		if (attributes == null)
			return;

		int attrOffset = m.start(3);

		int lastEnd = 0;
		IQProperty.Attribute lastAttr = null;
		for (Matcher attributeMatcher = qPropertyAttributeRegex.matcher(attributes); attributeMatcher
				.find(); lastEnd = attributeMatcher.end()) {
			// set the value of attribute found in the previous iteration to the substring between
			// the end of that attribute and the start of this one
			if (lastAttr != null) {
				String value = attributes.substring(lastEnd, attributeMatcher.start());
				int wsOffset = 0;
				Matcher ws = leadingWhitespaceRegex.matcher(value);
				if (ws.matches()) {
					value = ws.group(1);
					wsOffset = ws.start(1);
				}

				values[lastAttr.ordinal()] = new AttrValue(attrOffset + lastEnd + wsOffset, value.trim());
			}

			// the regex is built from the definition of the enum, so none of the strings that it
			// finds will throw an exception
			lastAttr = IQProperty.Attribute.valueOf(IQProperty.Attribute.class, attributeMatcher.group(0));

			// if this attribute doesn't have a value, then put it into the value map immediately
			// and make sure it is not used later in this scan
			if (!lastAttr.hasValue) {
				values[lastAttr.ordinal()] = AttrValue.None;
				lastAttr = null;
			}
		}

		// the value of the last attribute in the expansion is the substring between the end of
		// the attribute identifier and the end of the string
		if (lastAttr != null) {
			String value = attributes.substring(lastEnd);
			int wsOffset = 0;
			Matcher ws = leadingWhitespaceRegex.matcher(value);
			if (ws.matches()) {
				value = ws.group(1);
				wsOffset = ws.start(1);
			}

			values[lastAttr.ordinal()] = new AttrValue(attrOffset + lastEnd + wsOffset, value.trim());
		}

		// Put all values into the property name.
		for (int i = 0; i < values.length; ++i) {
			IQProperty.Attribute attr = IQProperty.Attribute.values()[i];
			AttrValue value = values[i];
			if (value == null)
				continue;

			// If the attribute is not expected to have a C++ binding as the value, then it can
			// be immediately added to the Q_PROPERTY.
			if (!couldHaveBinding(attr)) {
				propertyName.addAttribute(attr, value.value);
				continue;
			}

			// Otherwise see if one or more bindings can be found for the value of the attribute.
			// TODO Check whether the Qt moc allows for inherited methods.
			IBinding[] bindings = null;
			IASTNode specNode = qobjName.getParent();
			if (specNode instanceof IASTCompositeTypeSpecifier) {
				IScope scope = ((IASTCompositeTypeSpecifier) specNode).getScope();
				bindings = CPPSemantics.findBindings(scope, value.value, false);
			}

			// If no bindings are found, then the attribute can be immediately added to the Q_PROPERTY.
			if (bindings == null || bindings.length <= 0) {
				propertyName.addAttribute(attr, value.value);
				continue;
			}

			// Otherwise create a new attribute for each binding.
			for (IBinding foundBinding : bindings) {
				propertyName.addAttribute(attr, value.value, foundBinding);

				IASTName cppName = findASTName(foundBinding);
				if (cppName != null) {
					QtASTImageLocation attrLoc = new QtASTImageLocation(refName.getFileLocation(), value.offset,
							value.value.length());
					symbols.add(owner, new ASTNameReference(cppName, attrLoc), propertyName);
				}
			}
		}
	}

	private static boolean couldHaveBinding(IQProperty.Attribute attr) {
		switch (attr) {
		case READ:
		case WRITE:
		case RESET:
		case NOTIFY:
		case DESIGNABLE:
		case SCRIPTABLE:
			return true;

		case REVISION:
		case STORED:
		case USER:
		case CONSTANT:
		case FINAL:
		default:
			return false;
		}
	}

	private static IASTName findASTName(IBinding binding) {
		IASTNode node = null;
		if (binding instanceof ICPPInternalBinding) {
			node = ((ICPPInternalBinding) binding).getDefinition();
			if (node == null)
				node = ((ICPPInternalBinding) binding).getDeclarations()[0];
		}

		if (node == null)
			return null;

		IASTName astName = node instanceof IASTName ? (IASTName) node : null;
		if (astName != null)
			return astName;

		if (node instanceof IASTDeclarator)
			return ((IASTDeclarator) node).getName();

		return null;
	}

	private static class AttrValue {
		public final int offset;
		public final String value;

		public AttrValue(int offset, String value) {
			this.offset = offset;
			this.value = value;
		}

		public static final AttrValue None = new AttrValue(0, null);
	}

	private void extractQMethods(IASTPreprocessorIncludeStatement owner, ICPPASTCompositeTypeSpecifier spec,
			QObjectName qobjName) {
		QtASTClass qtASTClass = QtASTClass.create(spec);
		for (IASTDeclaration decl : spec.getMembers()) {

			// We only care about this node if it is within a signal/slot region or if it
			// has been tagged with a Qt annotating tag.
			int offset = decl.getFileLocation().getNodeOffset();
			IQMethod.Kind kind = qtASTClass.getKindFor(offset);
			Long revision = qtASTClass.getRevisionFor(offset);
			if (kind == IQMethod.Kind.Unspecified)
				continue;

			// Only named methods are processed, so skip this node if it is not a function or
			// if it does not have a name.
			IASTSimpleDeclaration simpleDecl = getSimpleDecl(decl);
			if (simpleDecl == null)
				continue;

			ICPPASTFunctionDeclarator decltor = null;
			for (IASTDeclarator d : simpleDecl.getDeclarators())
				if (d instanceof ICPPASTFunctionDeclarator) {
					decltor = (ICPPASTFunctionDeclarator) d;
					break;
				}
			if (decltor == null)
				continue;

			IASTName cppName = decltor.getName();
			if (cppName == null)
				continue;

			String qtEncSignatures = QtMethodUtil.getEncodedQtMethodSignatures(decltor);
			symbols.add(owner, new QMethodName(qobjName, cppName, kind, qtEncSignatures, revision), qobjName);
		}
	}

	private static IASTSimpleDeclaration getSimpleDecl(IASTNode node) {
		while (node != null && !(node instanceof IASTSimpleDeclaration))
			node = node.getParent();
		return node instanceof IASTSimpleDeclaration ? (IASTSimpleDeclaration) node : null;
	}
}
