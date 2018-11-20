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
package org.eclipse.cdt.internal.qt.ui.assist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameter;
import org.eclipse.cdt.internal.qt.core.index.IQMethod;
import org.eclipse.cdt.internal.qt.core.index.IQObject;
import org.eclipse.cdt.internal.qt.core.index.IQProperty;
import org.eclipse.cdt.internal.qt.core.index.QtIndex;
import org.eclipse.cdt.internal.qt.ui.Activator;
import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProposal;
import org.eclipse.cdt.ui.text.contentassist.ICEditorContentAssistInvocationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * An attribute-based proposal depends on the both the attribute (the previous identifier) and the
 * containing class definition.  The class definition is not needed for all attribute types, but
 * is used to build the list of proposals for attributes like READ, WRITE, etc.
 */
@SuppressWarnings("restriction")
public class QPropertyAttributeProposal {
	private final int relevance;
	private final String identifier;
	private final String display;

	public QPropertyAttributeProposal(String identifier, int relevance) {
		this(identifier, identifier, relevance);
	}

	public ICompletionProposal createProposal(String prefix, int offset) {
		int prefixLen = prefix == null ? 0 : prefix.length();

		String disp = identifier.equals(display) ? display : (identifier + " - " + display);
		return new CCompletionProposal(identifier.substring(prefixLen), offset, prefixLen, Activator.getQtLogo(), disp,
				relevance);
	}

	private QPropertyAttributeProposal(String identifier, String display, int relevance) {
		this.identifier = identifier;
		this.display = display;
		this.relevance = relevance;
	}

	public String getIdentifier() {
		return identifier;
	}

	public static Collection<QPropertyAttributeProposal> buildProposals(IQProperty.Attribute attr,
			ICEditorContentAssistInvocationContext context, IType type, String name) {
		switch (attr) {
		// propose true/false for bool Attributes
		case DESIGNABLE:
		case SCRIPTABLE:
		case STORED:
		case USER:
			return Arrays.asList(new QPropertyAttributeProposal("true", IMethodAttribute.BaseRelevance + 11),
					new QPropertyAttributeProposal("false", IMethodAttribute.BaseRelevance + 10));

		// propose appropriate methods for method-based attributes
		case READ:
		case WRITE:
		case RESET:
			return getMethodProposals(context, get(attr, type, name));

		// propose appropriate signals for NOTIFY
		case NOTIFY:
			return getSignalProposals(context, get(attr, type, name));

		default:
			break;
		}

		return Collections.emptyList();
	}

	private static Collection<QPropertyAttributeProposal> getMethodProposals(
			ICEditorContentAssistInvocationContext context, IMethodAttribute methodAttribute) {

		ICPPClassType cls = getEnclosingClassDefinition(context);
		if (cls == null)
			return Collections.emptyList();

		// Return all the methods, including inherited and non-visible ones.
		ICPPMethod[] methods = cls.getMethods();
		List<ICPPMethod> filtered = new ArrayList<>(methods.length);
		for (ICPPMethod method : methods)
			if (methodAttribute.keep(method))
				filtered.add(method);

		// TODO Choose the overload that is the best match -- closest parameter type and fewest
		//      parameters with default values.

		List<QPropertyAttributeProposal> proposals = new ArrayList<>();
		for (ICPPMethod method : getMethods(context, methodAttribute))
			proposals.add(new QPropertyAttributeProposal(method.getName(), getDisplay(cls, method),
					methodAttribute.getRelevance(method)));

		return proposals;
	}

	private static Collection<QPropertyAttributeProposal> getSignalProposals(
			ICEditorContentAssistInvocationContext context, IMethodAttribute methodAttribute) {
		ICPPClassType cls = getEnclosingClassDefinition(context);
		if (cls == null)
			return Collections.emptyList();

		ICProject cProject = context.getProject();
		if (cProject == null)
			return Collections.emptyList();

		QtIndex qtIndex = QtIndex.getIndex(cProject.getProject());
		if (qtIndex == null)
			return Collections.emptyList();

		IQObject qobj = null;
		try {
			qobj = qtIndex.findQObject(cls.getQualifiedName());
		} catch (DOMException e) {
			Activator.log(e);
		}

		if (qobj == null)
			return Collections.emptyList();

		List<QPropertyAttributeProposal> proposals = new ArrayList<>();
		for (IQMethod qMethod : qobj.getSignals().all())
			proposals.add(new QPropertyAttributeProposal(qMethod.getName(), IMethodAttribute.BaseRelevance));

		return proposals;
	}

	private static boolean isSameClass(ICPPClassType cls1, ICPPClassType cls2) {

		// IType.isSameType doesn't work in this case.  Given an instance of ICPPClassType, cls,
		// the following returns false:
		//     cls.isSameType( cls.getMethods()[0].getOwner() )
		//
		// Instead we check the fully qualified names.

		try {
			String[] qn1 = cls1.getQualifiedName();
			String[] qn2 = cls2.getQualifiedName();

			if (qn1.length != qn2.length)
				return false;

			for (int i = 0; i < qn1.length; ++i)
				if (!qn1[i].equals(qn2[i]))
					return false;
			return true;
		} catch (DOMException e) {
			return false;
		}
	}

	private static String getDisplay(ICPPClassType referenceContext, ICPPMethod method) {

		boolean includeClassname = !isSameClass(referenceContext, method.getClassOwner());

		StringBuilder sig = new StringBuilder();
		ICPPFunctionType type = method.getType();

		sig.append(type.getReturnType().toString());
		sig.append(' ');
		if (includeClassname) {
			sig.append(method.getOwner().getName());
			sig.append("::");
		}
		sig.append(method.getName());
		sig.append('(');
		boolean first = true;
		for (ICPPParameter param : method.getParameters()) {
			if (first)
				first = false;
			else
				sig.append(", ");

			String defValue = null;
			if (param instanceof CPPParameter) {
				CPPParameter cppParam = (CPPParameter) param;
				IASTInitializer defaultValue = cppParam.getInitializer();
				if (defaultValue instanceof IASTEqualsInitializer) {
					IASTInitializerClause clause = ((IASTEqualsInitializer) defaultValue).getInitializerClause();
					defValue = clause.toString();
				}
			}

			sig.append(defValue == null ? param.getType().toString() : defValue);
		}
		sig.append(')');
		return sig.toString();
	}

	private static interface IMethodAttribute {
		public boolean keep(ICPPMethod method);

		public static final int BaseRelevance = 2000;

		public int getRelevance(ICPPMethod method);

		public static final IMethodAttribute Null = new IMethodAttribute() {
			@Override
			public boolean keep(ICPPMethod method) {
				return false;
			}

			@Override
			public int getRelevance(ICPPMethod method) {
				return 0;
			}
		};
	}

	private static IMethodAttribute get(IQProperty.Attribute attr, IType type, String propertyName) {
		switch (attr) {
		case READ:
			return new Read(type, propertyName);
		case WRITE:
			return new Write(type, propertyName);
		case RESET:
			return new Reset(type, propertyName);
		default:
			return IMethodAttribute.Null;
		}
	}

	private static class Read implements IMethodAttribute {
		private final IType type;
		private final String propertyName;

		public Read(IType type, String propertyName) {
			this.type = type;
			this.propertyName = propertyName;
		}

		// From the Qt docs, http://qt-project.org/doc/qt-4.8/properties.html:
		// "A READ accessor function is required. It is for reading the property value. Ideally, a
		// const function is used for this purpose, and it must return either the property's type
		// or a pointer or reference to that type. e.g., QWidget::focus is a read-only property with
		// READ function, QWidget::hasFocus().
		@Override
		public boolean keep(ICPPMethod method) {
			// READ must have no params without default values
			if (method.getParameters().length > 0 && !method.getParameters()[0].hasDefaultValue())
				return false;

			// Make sure the return type of the method can be assigned to the property's type.
			IType retType = method.getType().getReturnType();
			if (!isAssignable(retType, type))
				return false;

			return true;
		}

		@Override
		public int getRelevance(ICPPMethod method) {
			String methodName = method.getName();
			if (methodName == null)
				return 0;

			// exact match is the most relevant
			if (methodName.equals(propertyName))
				return BaseRelevance + 20;

			// accessor with "get" prefix is the 2nd highest rank
			if (methodName.equalsIgnoreCase("get" + propertyName))
				return BaseRelevance + 19;

			// method names that include the property name anywhere are the next
			// most relevant
			if (methodName.matches(".*(?i)" + propertyName + ".*"))
				return BaseRelevance + 18;

			// otherwise return default relevance
			return 10;
		}
	}

	private static class Write implements IMethodAttribute {
		private final IType type;
		private final String propertyName;

		public Write(IType type, String propertyName) {
			this.type = type;
			this.propertyName = propertyName;
		}

		// From the Qt docs, http://qt-project.org/doc/qt-4.8/properties.html:
		// A WRITE accessor function is optional. It is for setting the property value. It must
		// return void and must take exactly one argument, either of the property's type or a
		// pointer or reference to that type. e.g., QWidget::enabled has the WRITE function
		// QWidget::setEnabled(). Read-only properties do not need WRITE functions. e.g., QWidget::focus
		// has no WRITE function.
		@Override
		public boolean keep(ICPPMethod method) {

			// The Qt moc doesn't seem to check that the return type is void, and I'm not sure why it
			// would need to.  This filter doesn't reject non-void methods.

			// WRITE must have at least one parameter and no more than one param without default values
			if (method.getParameters().length < 1
					|| (method.getParameters().length > 1 && !method.getParameters()[1].hasDefaultValue()))
				return false;

			// Make sure the property's type can be assigned to the type of the first parameter
			IType paramType = method.getParameters()[0].getType();
			if (!isAssignable(type, paramType))
				return false;

			return true;
		}

		@Override
		public int getRelevance(ICPPMethod method) {
			String methodName = method.getName();
			if (methodName == null)
				return 0;

			// exact match is the most relevant
			if (methodName.equals(propertyName))
				return BaseRelevance + 20;

			// accessor with "get" prefix is the 2nd highest rank
			if (methodName.equalsIgnoreCase("set" + propertyName))
				return BaseRelevance + 19;

			// method names that include the property name anywhere are the next
			// most relevant
			if (methodName.matches(".*(?i)" + propertyName + ".*"))
				return BaseRelevance + 18;

			// otherwise return default relevance
			return 10;
		}
	}

	private static class Reset implements IMethodAttribute {
		private final IType type;
		private final String propertyName;

		public Reset(IType type, String propertyName) {
			this.type = type;
			this.propertyName = propertyName;
		}

		// From the Qt docs, http://qt-project.org/doc/qt-4.8/properties.html:
		// A RESET function is optional. It is for setting the property back to its context
		// specific default value. e.g., QWidget::cursor has the typical READ and WRITE
		// functions, QWidget::cursor() and QWidget::setCursor(), and it also has a RESET
		// function, QWidget::unsetCursor(), since no call to QWidget::setCursor() can mean
		// reset to the context specific cursor. The RESET function must return void and take
		// no parameters.
		@Override
		public boolean keep(ICPPMethod method) {

			// RESET must have void return type
			IType retType = method.getType().getReturnType();
			if (!(retType instanceof IBasicType) || ((IBasicType) retType).getKind() != IBasicType.Kind.eVoid)
				return false;

			// RESET must have no parameters
			if (method.getParameters().length > 0)
				return false;

			return true;
		}

		@Override
		public int getRelevance(ICPPMethod method) {
			String methodName = method.getName();
			if (methodName == null)
				return 0;

			// accessor with "reet" prefix is the most relevant
			if (methodName.equalsIgnoreCase("reset" + propertyName))
				return BaseRelevance + 20;

			// method names that include the property name anywhere are the next
			// most relevant
			if (methodName.matches(".*(?i)" + propertyName + ".*"))
				return BaseRelevance + 18;

			// otherwise return default relevance
			return 10;
		}
	}

	private static ICPPClassType getEnclosingClassDefinition(ICEditorContentAssistInvocationContext context) {
		try {
			IIndex index = CCorePlugin.getIndexManager().getIndex(context.getProject());
			ITranslationUnit tu = context.getTranslationUnit();
			if (tu == null)
				return null;

			// Disable all unneeded parts of the parser.
			IASTTranslationUnit astTU = tu.getAST(index,
					ITranslationUnit.AST_SKIP_FUNCTION_BODIES | ITranslationUnit.AST_SKIP_ALL_HEADERS
							| ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT
							| ITranslationUnit.AST_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS
							| ITranslationUnit.AST_PARSE_INACTIVE_CODE);
			if (astTU == null)
				return null;

			IASTNodeSelector selector = astTU.getNodeSelector(null);

			// Macro expansions don't provide valid enclosing nodes.  Backup until we are no longer in a
			// macro expansions.  A loop is needed because consecutive expansions have no valid node
			// between them.
			int offset = context.getInvocationOffset();
			IASTNode enclosing;
			do {
				enclosing = selector.findEnclosingNode(offset, 0);
				if (enclosing == null)
					return null;

				IASTFileLocation location = enclosing.getFileLocation();
				if (location == null)
					return null;

				offset = location.getNodeOffset() - 1;
			} while (offset > 0 && !(enclosing instanceof IASTCompositeTypeSpecifier));

			if (!(enclosing instanceof IASTCompositeTypeSpecifier))
				return null;

			IASTName name = ((IASTCompositeTypeSpecifier) enclosing).getName();
			if (name == null)
				return null;

			IBinding binding = name.getBinding();
			if (binding == null)
				return null;

			return binding.getAdapter(ICPPClassType.class);
		} catch (CoreException e) {
			Activator.log(e);
		}

		return null;
	}

	/**
	 * Find and return all methods that are accessible in the class definition that encloses the argument
	 * invocation context.  Does not return null.
	 */
	private static Collection<ICPPMethod> getMethods(ICEditorContentAssistInvocationContext context,
			IMethodAttribute methodAttribute) {

		ICPPClassType cls = getEnclosingClassDefinition(context);
		if (cls == null)
			return Collections.emptyList();

		// Return all the methods, including inherited and non-visible ones.
		ICPPMethod[] methods = cls.getMethods();
		List<ICPPMethod> filtered = new ArrayList<>(methods.length);
		for (ICPPMethod method : methods)
			if (methodAttribute.keep(method))
				filtered.add(method);

		// TODO Choose the overload that is the best match -- closest parameter type and fewest
		//      parameters with default values.

		return filtered;
	}

	private static boolean isAssignable(IType lhs, IType rhs) {
		// TODO This needs a real assignment check.  If the types are different by implicitly convertible
		//      then this should return true.
		return lhs != null && rhs.isSameType(lhs);
	}
}
