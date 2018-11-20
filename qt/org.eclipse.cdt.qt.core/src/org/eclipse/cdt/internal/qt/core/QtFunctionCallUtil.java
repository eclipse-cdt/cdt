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
package org.eclipse.cdt.internal.qt.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * Utility for managing interaction with QObject::connect and QObject::disconnect function
 * calls.  These function calls can contain two expansions.  The first is always SIGNAL,
 * the second is either SIGNAL (which will cause the second signal to be emitted when the
 * first is received) or SLOT (which will cause the slot to be evaluate when the signal
 * is received).  This class follows the Qt convention of calling the first SIGNAL expansion
 * the sender and the second (which could be SIGNAL or SLOT) the receiver.
 *
 * In the following examples, the type of the signal is the type of the q_sender variable.
 * The type of the method is the type of the q_receiver variable.  The variable q_unrelated is
 * some instance that is not needed for either case.
 * <pre>
 * QObject::connect( q_sender, SIGNAL(destroyed()), q_receiver, SIGNAL() );
 * QObject::connect( q_sender, SIGNAL(destroyed()), q_receiver, SLOT(deleteLater()) );
 * QObject::connect( q_sender, SIGNAL(destroyed()), q_receiver, SIGNAL(), Qt::AutoConnection );
 * QObject::connect( q_sender, SIGNAL(destroyed()), q_receiver, SLOT(deleteLater()), Qt::AutoConnection );
 * q_unrelated->connect( q_sender, SIGNAL(destroyed()), q_receiver, SIGNAL() );
 * q_unrelated->connect( q_sender, SIGNAL(destroyed()), q_receiver, SLOT(deleteLater()) );
 * q_unrelated->connect( q_sender, SIGNAL(destroyed()), q_receiver, SIGNAL(), Qt::AutoConnection );
 * q_unrelated->connect( q_sender, SIGNAL(destroyed()), q_receiver, SLOT(deleteLater()), Qt::AutoConnection );
 *
 * q_receiver->connect( q_sender, SIGNAL(destroyed()), SIGNAL() );
 * q_receiver->connect( q_sender, SIGNAL(destroyed()), SLOT() );
 * q_receiver->connect( q_sender, SIGNAL(destroyed()), SIGNAL(), Qt::AutoConnection );
 * q_receiver->connect( q_sender, SIGNAL(destroyed()), SLOT(), Qt::AutoConnection );
 *
 * QObject::disconnect( q_sender, SIGNAL(), q_receiver, SIGNAL() );
 * QObject::disconnect( q_sender, SIGNAL(), q_receiver, SLOT() );
 * q_unrelated->disconnect( q_sender, SIGNAL(), q_receiver, SIGNAL() );
 * q_unrelated->disconnect( q_sender, SIGNAL(), q_receiver, SLOT() );
 *
 * q_sender->disconnect( SIGNAL(), q_receiver, SIGNAL() );
 * q_sender->disconnect( SIGNAL(), q_receiver, SLOT() );
 * q_sender->disconnect( SIGNAL(), q_receiver );
 * q_sender->disconnect( SIGNAL() );
 * q_sender->disconnect();
 * </pre>
 */
public class QtFunctionCallUtil {

	private static final Pattern SignalRegex = Pattern.compile("^\\s*" + QtKeywords.SIGNAL + ".*");
	private static final Pattern MethodRegex = Pattern
			.compile("^\\s*(?:" + QtKeywords.SIGNAL + '|' + QtKeywords.SLOT + ").*");

	/**
	 * Return true if the specified name is a QObject::connect or QObject::disconnect function
	 * and false otherwise.
	 */
	public static boolean isQObjectFunctionCall(IASTCompletionContext astContext, boolean isPrefix, IASTName name) {
		if (name == null || name.getSimpleID() == null || name.getSimpleID().length <= 0)
			return false;

		// Bug332201: Qt content assist should always be applied to the most specific part of
		//            the target name.
		IBinding[] funcBindings = astContext.findBindings(name.getLastName(), isPrefix);
		for (IBinding funcBinding : funcBindings)
			if (QtKeywords.is_QObject_connect(funcBinding) || QtKeywords.is_QObject_disconnect(funcBinding))
				return true;

		return false;
	}

	/**
	 * If the given argument is a SIGNAL or SLOT expansion then find and return the node in the AST
	 * that will be used for this method.  Returns null if the argument is not a Qt method call or
	 * if the associated node cannot be found.
	 */
	public static IType getTargetType(IASTFunctionCallExpression call, IASTInitializerClause[] args, int argIndex) {
		int sigExpIndex = getExpansionArgIndex(args, 0, SignalRegex);
		if (argIndex == sigExpIndex)
			return getSignalTargetType(sigExpIndex, call, args);

		int methodExpIndex = getExpansionArgIndex(args, sigExpIndex + 1, MethodRegex);
		if (argIndex == methodExpIndex)
			return getMethodTargetType(methodExpIndex, sigExpIndex, call, args);

		// Otherwise the given argument is not a SIGNAL or SLOT expansion.
		return null;
	}

	private static IType getSignalTargetType(int sigExpIndex, IASTFunctionCallExpression call,
			IASTInitializerClause[] args) {
		// When the SIGNAL expansion is first, the type is based on the receiver of
		// the function call.  Otherwise the type is the previous argument.
		return ASTUtil.getBaseType(sigExpIndex == 0 ? call : args[sigExpIndex - 1]);
	}

	private static IType getMethodTargetType(int methodExpIndex, int sigExpIndex, IASTFunctionCallExpression call,
			IASTInitializerClause[] args) {
		// If the method is right after the signal, then the type is based on the receiver
		// of the function call.  Otherwise the method type is based on the parameter right
		// before the expansion.
		if (methodExpIndex == (sigExpIndex + 1))
			return ASTUtil.getReceiverType(call);
		return ASTUtil.getBaseType(args[methodExpIndex - 1]);
	}

	private static int getExpansionArgIndex(IASTInitializerClause[] args, int begin, Pattern macroNameRegex) {
		for (int i = begin; i < args.length; ++i) {
			IASTInitializerClause arg = args[i];
			String raw = arg.getRawSignature();
			Matcher m = macroNameRegex.matcher(raw);
			if (m.matches())
				return i;
		}
		return -1;
	}
}
