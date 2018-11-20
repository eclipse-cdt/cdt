/*
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core;

import java.util.regex.Matcher;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.qt.core.index.IQMethod;
import org.eclipse.cdt.internal.qt.core.index.IQObject;
import org.eclipse.cdt.internal.qt.core.index.QtIndex;
import org.eclipse.cdt.internal.qt.core.pdom.ASTNameReference;
import org.eclipse.cdt.internal.qt.core.pdom.QtASTImageLocation;
import org.eclipse.core.resources.IProject;

/**
 * Qt signals and slots are referenced using the SIGNAL and SLOT macros.  The expansion
 * parameter is the signature of the signal or slot and they are associated with a type.
 * This utility class is used to convert from these AST nodes to an IASTName that can be
 * used as a reference to the IBinding for the C++ method.
 */
@SuppressWarnings("restriction")
public class QtMethodReference extends ASTNameReference {

	public static enum Type {
		Signal("sender", "SIGNAL", "signal"), Slot("receiver", "SLOT", "member");

		public final String roleName;
		public final String macroName;
		public final String paramName;

		public boolean matches(Type other) {
			if (other == null)
				return false;

			// The signal parameter must be a SIGNAL, but the slot could be a
			// SLOT or a SIGNAL.
			return this == Signal ? other == Signal : true;
		}

		/**
		 * Return the type of method reference within the expansion of the given macro name.
		 */
		public static Type from(IASTName name) {
			String nameStr = String.valueOf(name);
			if (QtKeywords.SIGNAL.equals(nameStr))
				return Signal;
			if (QtKeywords.SLOT.equals(nameStr))
				return Slot;
			return null;
		}

		private Type(String roleName, String macroName, String paramName) {
			this.roleName = roleName;
			this.macroName = macroName;
			this.paramName = paramName;
		}
	}

	private final Type type;
	private final ICPPClassType cls;
	private final String expansionParam;

	private QtMethodReference(Type type, ICPPClassType cls, IASTName macroRefName, String expansionParam,
			IASTFileLocation location) {
		super(macroRefName, location);

		this.type = type;
		this.cls = cls;
		this.expansionParam = expansionParam;
	}

	/**
	 * Return the C++ class that defines the Qt method that is being referenced.
	 */
	public ICPPClassType getContainingType() {
		return cls;
	}

	/**
	 * Look for SIGNAL or SLOT macro expansions at the location of the given node.  Return the
	 * QMethod reference is an expansion is found and null otherwise.
	 * <p>
	 * QMetaMethod references cannot be statically resolved so null will be returned in this case.
	 */
	public static QtMethodReference parse(IASTNode parent, IType cppType, IASTNode arg) {
		if (!(cppType instanceof ICPPClassType) || arg == null)
			return null;
		ICPPClassType cls = (ICPPClassType) cppType;

		// Look for a SIGNAL or SLOT expansion as this location.
		Type type = null;
		IASTName macroReferenceName = null;
		for (IASTNodeLocation location : arg.getNodeLocations()) {
			if (!(location instanceof IASTMacroExpansionLocation))
				continue;

			IASTPreprocessorMacroExpansion expansion = ((IASTMacroExpansionLocation) location).getExpansion();
			macroReferenceName = expansion.getMacroReference();
			IASTPreprocessorMacroDefinition macroDefn = expansion.getMacroDefinition();

			type = Type.from(macroDefn.getName());
			if (type != null)
				break;
		}

		// There is nothing to do if the expected type of expansion is not found.
		if (macroReferenceName == null || type == null)
			return null;

		// This check will miss cases like:
		//     #define MY_SIG1 SIGNAL
		//     #define MY_SIG2(s) SIGNAL(s)
		//     #define MY_SIG3(s) SIGNAL(signal())
		//     connect( &a, MY_SIG1(signal()), ...
		//     connect( &a, MY_SIG2(signal()), ...
		//     connect( &a, MY_SIG2, ...
		// This could be improved by adding tests when arg represents a macro expansion.  However, I'm
		// not sure if we would be able to follow the more complicated case of macros that call functions
		// that use the SIGNAL macro.  For now I've implemented the simpler check of forcing the call to
		// use the SIGNAL/SLOT macro directly.
		String raw = arg.getRawSignature();
		Matcher m = ASTUtil.Regex_MacroExpansion.matcher(raw);
		if (!m.matches())
			return null;

		// Get the argument to the SIGNAL/SLOT macro and the offset/length of that argument within the
		// complete function argument.  E.g., with this argument to QObject::connect
		//      SIGNAL( signal(int) )
		// the values are
		//		expansionArgs:  "signal(int)"
		//		expansionOffset: 8
		//		expansionLength: 11
		String expansionArgs = m.group(2);
		int expansionOffset = m.start(2);
		int expansionLength = m.end(2) - expansionOffset;

		IASTFileLocation location = new QtASTImageLocation(macroReferenceName.getFileLocation(), expansionOffset,
				expansionLength);
		return new QtMethodReference(type, cls, macroReferenceName, expansionArgs, location);
	}

	public Type getType() {
		return type;
	}

	@Override
	public String getRawSignature() {
		return expansionParam;
	}

	@Override
	public char[] getSimpleID() {
		return expansionParam.toCharArray();
	}

	private IQObject findQObject() {
		String[] qualName = null;
		try {
			qualName = cls.getQualifiedName();
		} catch (DOMException e) {
			Activator.log(e);
		}

		IProject project = ASTUtil.getProject(delegate);
		if (project == null)
			return null;

		QtIndex qtIndex = QtIndex.getIndex(project);
		if (qtIndex == null)
			return null;

		return qtIndex.findQObject(qualName);
	}

	public IQMethod getMethod() {
		IQObject qobj = findQObject();
		if (qobj == null)
			return null;

		// Return the first matching method.
		for (IQMethod method : ASTUtil.findMethods(qobj, this))
			return method;

		return null;
	}

	@Override
	public IBinding resolveBinding() {
		if (binding != null)
			return binding;

		// Qt method references return the C++ method that is being referenced in the SIGNAL or
		// SLOT macro expansion.
		String methodName = expansionParam;
		int paren = methodName.indexOf('(');
		if (paren > 0)
			methodName = methodName.substring(0, paren);
		IBinding[] methods = CPPSemantics.findBindings(cls.getCompositeScope(), methodName.trim(), false);

		// TODO find the one binding that matches the parameter of the macro expansion
		// 1) Normalize expansionParam
		// 2) Use it to filter the matching methods
		binding = methods.length > 0 ? methods[0] : null;
		return binding;
	}
}
