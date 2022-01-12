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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;

/**
 * Extracts required information from FunctionCallExpressions that call
 * QObject::connect.  This implementation handles all overloads of QObject::connect
 * except the QMetaMethod related ones.  QMetaMethods cannot be statically analyzed
 * so they are ignored.
 * <p>
 * The binding is found by identifying the overload and then looking at the appropriate
 * parameters.
 */
public class QtFunctionCall {

	private QtFunctionCall() {
	}

	/**
	 * Returns a collection of all Qt method references within the given function call.  Returns
	 * null if there are no Qt method references.
	 */
	public static Collection<QtMethodReference> getReferences(IASTFunctionCallExpression call) {
		ICPPFunction function = ASTUtil.resolveFunctionBinding(ICPPFunction.class, call);
		if (function == null)
			return null;

		if (QtKeywords.is_QObject_connect(function))
			return getReferencesInConnect(function, call);
		if (QtKeywords.is_QObject_disconnect(function))
			return getReferencesInDisconnect(function, call);
		return null;
	}

	private static Collection<QtMethodReference> getReferencesInConnect(ICPPFunction function,
			IASTFunctionCallExpression call) {
		if (function == null)
			return null;

		// There are 3 overloads of QObject::connect (Qt 4.8.4). They can be
		// distinguished by examining
		// the type of the forth parameter.
		// connect( , , , const char *, )
		// connect( , , , QMetaMethod&, )
		// connect( , , , Qt::ConnectionType = )
		ICPPParameter[] params = function.getParameters();
		if (params.length < 4)
			return null;

		IASTInitializerClause[] args = call.getArguments();
		IType type3 = ASTUtil.getBaseType(params[3].getType());

		// static bool connect( const QObject *sender, const QMetaMethod &signal,
		//                      const QObject *receiver, const QMetaMethod &method,
		//                      Qt::ConnectionType type = Qt::AutoConnection )
		if (QtKeywords.isQMetaMethod(type3)) {
			// QMetaMethod cannot be statically analyzed.
			return null;
		}

		// Otherwise find the signal and member parameters based on the overload.
		QtMethodReference signal = null;
		QtMethodReference member = null;

		// static bool connect( const QObject *sender, const char *signal,
		//                      const QObject *receiver, const char *member,
		//                      Qt::ConnectionType = Qt::AutoConnection );
		if (type3 instanceof IBasicType && ((IBasicType) type3).getKind() == IBasicType.Kind.eChar) {
			signal = QtMethodReference.parse(call, ASTUtil.getBaseType(safeArgsAt(args, 0)), safeArgsAt(args, 1));
			member = QtMethodReference.parse(call, ASTUtil.getBaseType(safeArgsAt(args, 2)), safeArgsAt(args, 3));
		}

		// inline bool connect( const QObject *sender, const char *signal,
		//                      const char *member,
		//                      Qt::ConnectionType type = Qt::AutoConnection ) const;
		else if (type3 instanceof IEnumeration) {
			signal = QtMethodReference.parse(call, ASTUtil.getBaseType(safeArgsAt(args, 0)), safeArgsAt(args, 1));
			member = QtMethodReference.parse(call, ASTUtil.getReceiverType(call), safeArgsAt(args, 2));
		}

		return mergeNonNull(signal, member);
	}

	private static Collection<QtMethodReference> getReferencesInDisconnect(ICPPFunction function,
			IASTFunctionCallExpression call) {
		if (function == null)
			return null;

		// There are 4 overloads of QObject::disconnect (Qt 4.8.4).  They can be distinguished by examining
		// the type of the second parameter.  The number of parameters is used to disambiguate one conflict.
		// disconnect( , const char *, , )  && 4 params
		// disconnect( , QMetaMethod&, , )
		// disconnect( , const QObject *, )
		// disconnect( , const char * )     && 2 params
		ICPPParameter[] params = function.getParameters();
		if (params.length < 2)
			return null;

		IASTInitializerClause[] args = call.getArguments();
		IType type1 = ASTUtil.getBaseType(params[1].getType());

		// static bool disconnect( const QObject *sender, const QMetaMethod &signal,
		//						   const QObject *receiver, const QMetaMethod &member );
		if (QtKeywords.isQMetaMethod(type1)) {
			// QMetaMethod cannot be statically analyzed.
			return Collections.emptyList();
		}

		// Otherwise find the signal and member parameters based on the overload.
		QtMethodReference signal = null;
		QtMethodReference member = null;

		if (type1 instanceof IBasicType && ((IBasicType) type1).getKind() == IBasicType.Kind.eChar) {
			switch (params.length) {
			// static bool disconnect( const QObject *sender, const char *signal,
			//                         const QObject *receiver, const char *member );
			case 4:
				signal = QtMethodReference.parse(call, ASTUtil.getBaseType(safeArgsAt(args, 0)), safeArgsAt(args, 1));
				member = QtMethodReference.parse(call, ASTUtil.getBaseType(safeArgsAt(args, 2)), safeArgsAt(args, 3));
				break;

			// inline bool disconnect( const QObject *receiver, const char *member = 0 );
			case 2:
				member = QtMethodReference.parse(call, ASTUtil.getBaseType(safeArgsAt(args, 0)), safeArgsAt(args, 1));
				break;
			}
		}

		// inline bool disconnect( const char *signal = 0,
		//                         const QObject *receiver = 0, const char *member = 0 );
		else if (QtKeywords.isQObject(type1)) {
			ICPPClassType recvr = ASTUtil.getReceiverType(call);
			signal = QtMethodReference.parse(call, recvr, safeArgsAt(args, 0));
			member = QtMethodReference.parse(call, ASTUtil.getBaseType(safeArgsAt(args, 1)), safeArgsAt(args, 2));
		}

		return mergeNonNull(signal, member);
	}

	private static IASTNode safeArgsAt(IASTNode[] args, int index) {
		return args.length > index ? args[index] : null;
	}

	private static <T> Collection<T> mergeNonNull(T... withNulls) {
		T firstNonNull = null;
		ArrayList<T> list = null;
		for (T t : withNulls) {
			if (t == null)
				continue;
			else if (list != null)
				list.add(t);
			else if (firstNonNull == null)
				firstNonNull = t;
			else {
				list = new ArrayList<>(withNulls.length);
				list.add(firstNonNull);
				list.add(t);
			}
		}

		if (list != null)
			return list;
		if (firstNonNull != null)
			return Collections.singletonList(firstNonNull);
		return null;
	}
}
