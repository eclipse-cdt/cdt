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
package org.eclipse.cdt.internal.qt.core.index;

/**
 * Represents a specific QML type registration.
 * <p>
 * Qt allows types to be registered with QML by calling the qmlRegisterType function,
 * e.g.,
 * <pre>
 * class Q : public QObject { Q_OBJECT };
 * qmlRegisterType&lt;Q&gt;( "uri", 1, 0, "Q" );
 * </pre>
 * Registers Q in the QML system with the name "Q", in the library imported from "uri"
 * having the version number 1.0.
 */
public interface IQmlRegistration extends IQObject.IMember {
	/**
	 * Identifies the kind of qmlRegister* function that was used to register the
	 * type.  Qt 4.8 only defines two kinds, but in 5.0 there several more.
	 * <p>
	 * If a type has been registered more than once, then there will be several
	 * entries for it in the collection returned by {@link QtIndex#getQmlRegistrations()}.
	 */
	public enum Kind {
		/**
		 * Indicates that the type has been registered with a function call to
		 * qmlRegisterType.
		 */
		Type,

		/**
		 * Indicates that the type has been registered with a function call to
		 * qmlRegisterUncreatableType.
		 */
		Uncreatable
	}

	/**
	 * Returns the kind of function that was used for this registration.  In Qt 4.8,
	 * there are two variations of the qmlRegister* function; qmlRegisterType and
	 * qmlRegisterUncreatableType.  In Qt 5.0 there are several more.
	 * <p>
	 * It is possible for the same type to be registered in different ways, although
	 * this generally indicates a problem in the client code.
	 */
	public IQmlRegistration.Kind getKind();

	/**
	 * Returns QObject to which this registration applies.  In the sample at {@link IQmlRegistration}
	 * this would return the IQObject for Q.
	 */
	public IQObject getQObject();

	/**
	 * Returns the specific revision of the IQObject that was registered.  Returns null if no
	 * revision was specified.
	 * <p>
	 * E.g.,
	 * <code>
	 * class Q : public QObject
	 * {
	 * Q_OBJECT
	 * signals:
	 * Q_REVISION(2) void sig();
	 * };
	 *
	 * qmlRegisterType<Q>(    "uri", 1, 0, "Q1" );
	 * qmlRegisterType<Q, 2>( "uri", 1, 0, "Q2" );
	 * </code>
	 *
	 * The QML type "Q2" would have access to the "sig" signal, while "Q1" would not.
	 *
	 * @see IQMethod#getRevision()
	 * @see IQProperty#getRevision()
	 */
	public Long getVersion();

	/**
	 * Returns the literal value of the first argument to the function if it can be
	 * resolved and null otherwise.
	 */
	public String getURI();

	/**
	 * Returns the literal value of the second argument to the function if it can be
	 * resolved and null otherwise.
	 */
	public Long getMajor();

	/**
	 * Returns the literal value of the third argument to the function if it can be
	 * resolved and null otherwise.
	 */
	public Long getMinor();

	/**
	 * Returns the literal value of the fourth argument to the function if it can be
	 * resolved and null otherwise.
	 */
	public String getQmlName();

	/**
	 * Returns the literal value of the fifth argument to qmlRegisterUncreatableType if it
	 * can be resolved and null otherwise.
	 */
	public String getReason();
}
