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
package org.eclipse.cdt.qt.tests;

import java.util.Collection;

import org.eclipse.cdt.internal.qt.core.index.IQObject;
import org.eclipse.cdt.internal.qt.core.index.IQmlRegistration;
import org.eclipse.cdt.internal.qt.core.index.QtIndex;

public class QmlRegistrationTests extends BaseQtTestCase {

	// #include "junit-QObject.hh"
	// class B : public QObject
	// {
	// Q_OBJECT
	// };
	//
	// class D : public B
	// {
	// Q_OBJECT
	// };
	//
	// static void func()
	// {
	//     qmlRegisterType<B>( "b-uri",    1, 2, "B" );
	//     qmlRegisterType<B>( "b-uri.34", 3, 4, "B34" );
	//
	//     const char * uri = "d-uri";
	//     int maj = 2, min = 3;
	//     const char * qmlName = "D1";
	//     qmlRegisterType<D, 1>( uri, maj, min, qmlName );
	// }
	public void testQmlRegisterType() throws Exception {
		loadComment("qmlregistertype.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQObject b_qobj = qtIndex.findQObject(new String[] { "B" });
		if (!isIndexOk("B", b_qobj))
			return;
		assertNotNull(b_qobj);

		Collection<IQmlRegistration> qmlRegistrations = qtIndex.getQmlRegistrations();
		assertNotNull(qmlRegistrations);
		assertEquals(3, qmlRegistrations.size());

		for (IQmlRegistration qmlRegistration : qmlRegistrations) {
			IQObject qobj = qmlRegistration.getQObject();
			assertNotNull(qobj);

			// all values of B should be fully resolved, except for Revision, which was not provided
			if (qobj.getName().equals("B")) {
				assertNull(qmlRegistration.getVersion());
				String qmlName = qmlRegistration.getQmlName();
				assertNotNull(qmlName);
				if ("B".equals(qmlName)) {
					assertEquals(IQmlRegistration.Kind.Type, qmlRegistration.getKind());
					assertEquals("b-uri", qmlRegistration.getURI());
					assertEquals(Long.valueOf(1), qmlRegistration.getMajor());
					assertEquals(Long.valueOf(2), qmlRegistration.getMinor());
					assertNull(qmlRegistration.getReason());
				} else if ("B34".equals(qmlName)) {
					assertEquals(IQmlRegistration.Kind.Type, qmlRegistration.getKind());
					assertEquals("b-uri.34", qmlRegistration.getURI());
					assertEquals(Long.valueOf(3), qmlRegistration.getMajor());
					assertEquals(Long.valueOf(4), qmlRegistration.getMinor());
					assertNull(qmlRegistration.getReason());
				} else {
					fail("unexpected uri for B " + qmlName);
				}

				// the values for D are not expected to be resolved (yet), but it does have a Revision
			} else if (qobj.getName().equals("D")) {
				assertEquals(IQmlRegistration.Kind.Type, qmlRegistration.getKind());
				assertEquals(Long.valueOf(1), qmlRegistration.getVersion());
				assertNull(qmlRegistration.getURI());
				assertNull(qmlRegistration.getMajor());
				assertNull(qmlRegistration.getMinor());
				assertNull(qmlRegistration.getQmlName());
				assertNull(qmlRegistration.getReason());

			} else {
				fail("unexpected qmlRegistration " + qobj.getName());
			}
		}
	}

	// #include "junit-QObject.hh"
	// class T;
	//
	// static void func()
	// {
	//     qmlRegisterType<T>( "t-uri", 3, 4, "qml-T" );
	// }
	public void testQmlRegisterFwdDecl() throws Exception {
		loadComment("qmlregistertype.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		Collection<IQmlRegistration> qmlRegistrations = qtIndex.getQmlRegistrations();
		assertNotNull(qmlRegistrations);
		assertEquals(1, qmlRegistrations.size());

		IQmlRegistration qmlRegistration = qmlRegistrations.iterator().next();
		assertNotNull(qmlRegistration);
		assertEquals(IQmlRegistration.Kind.Type, qmlRegistration.getKind());
		assertEquals("t-uri", qmlRegistration.getURI());
		assertEquals(Long.valueOf(3), qmlRegistration.getMajor());
		assertEquals(Long.valueOf(4), qmlRegistration.getMinor());
		assertEquals("qml-T", qmlRegistration.getQmlName());
		assertNull(qmlRegistration.getReason());

		// The QObject has not been defined, so it cannot be found.
		assertNull(qmlRegistration.getQObject());
	}

	// #include "junit-QObject.hh"
	// class B : public QObject
	// {
	// Q_OBJECT
	// };
	//
	// class D : public B
	// {
	// Q_OBJECT
	// };
	//
	// static void func()
	// {
	//     qmlRegisterUncreatableType<B>( "b-uri",    1, 2, "B",   QString( "msg1" ) );
	//     qmlRegisterUncreatableType<B>( "b-uri.34", 3, 4, "B34", QString( "msg2" ) );
	//
	//     const char * uri = "d-uri";
	//     int maj = 2, min = 3;
	//     const char * qmlName = "D1";
	//     const QString msg( "msg3" );
	//     qmlRegisterUncreatableType<D>( uri, maj, min, qmlName, msg );
	// }
	public void testQmlRegisterUncreatableType() throws Exception {
		loadComment("qmlregisteruncreatabletype.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQObject b_qobj = qtIndex.findQObject(new String[] { "B" });
		if (!isIndexOk("B", b_qobj))
			return;
		assertNotNull(b_qobj);

		Collection<IQmlRegistration> qmlRegistrations = qtIndex.getQmlRegistrations();
		assertNotNull(qmlRegistrations);
		assertEquals(3, qmlRegistrations.size());

		for (IQmlRegistration qmlRegistration : qmlRegistrations) {
			IQObject qobj = qmlRegistration.getQObject();
			assertNotNull(qobj);

			// all values of B should be fully resolved, except for Revision, which was not provided
			if (qobj.getName().equals("B")) {
				assertNull(qmlRegistration.getVersion());
				String qmlName = qmlRegistration.getQmlName();
				assertNotNull(qmlName);
				if ("B".equals(qmlName)) {
					assertEquals(IQmlRegistration.Kind.Uncreatable, qmlRegistration.getKind());
					assertEquals("b-uri", qmlRegistration.getURI());
					assertEquals(Long.valueOf(1), qmlRegistration.getMajor());
					assertEquals(Long.valueOf(2), qmlRegistration.getMinor());
					assertEquals(null/*"msg1"*/, qmlRegistration.getReason());
				} else if ("B34".equals(qmlName)) {
					assertEquals(IQmlRegistration.Kind.Uncreatable, qmlRegistration.getKind());
					assertEquals("b-uri.34", qmlRegistration.getURI());
					assertEquals(Long.valueOf(3), qmlRegistration.getMajor());
					assertEquals(Long.valueOf(4), qmlRegistration.getMinor());
					assertEquals(null/*"msg2"*/, qmlRegistration.getReason());
				} else {
					fail("unexpected uri for B " + qmlName);
				}

				// the values for D are not expected to be resolved (yet), but it does have a Revision
			} else if (qobj.getName().equals("D")) {
				assertEquals(IQmlRegistration.Kind.Uncreatable, qmlRegistration.getKind());
				assertNull(qmlRegistration.getVersion());
				assertNull(qmlRegistration.getURI());
				assertNull(qmlRegistration.getMajor());
				assertNull(qmlRegistration.getMinor());
				assertNull(qmlRegistration.getQmlName());
				assertNull(qmlRegistration.getReason());
			} else {
				fail("unexpected qmlRegistration " + qobj.getName());
			}
		}
	}

	// #include "junit-QObject.hh"
	// class B : public QObject
	// {
	// Q_OBJECT
	// };
	//
	// class D : public B
	// {
	// Q_OBJECT
	// };
	//
	// static void func()
	// {
	//     qmlRegisterType<B>( "b-uri",    1, 2, "B" );
	//     qmlRegisterType<B>( "b-uri.34", 3, 4, "B34" );
	//     qmlRegisterType<D, 1>( "d-uri", 2, 3, "D" );
	// }
	public void testAccessFromQObject() throws Exception {
		loadComment("qmlregistertype_fromqobject.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQObject b_qobj = qtIndex.findQObject(new String[] { "B" });
		if (!isIndexOk("B", b_qobj))
			return;
		assertNotNull(b_qobj);

		Collection<IQmlRegistration> b_qmlRegistrations = b_qobj.getQmlRegistrations();
		assertNotNull(b_qmlRegistrations);
		assertEquals(2, b_qmlRegistrations.size());
		for (IQmlRegistration qmlRegistration : b_qmlRegistrations)
			if ("B".equals(qmlRegistration.getQmlName()))
				assert_checkQmlRegistration(qmlRegistration, IQmlRegistration.Kind.Type, null, "b-uri", 1L, 2L, null);
			else if ("B34".equals(qmlRegistration.getQmlName()))
				assert_checkQmlRegistration(qmlRegistration, IQmlRegistration.Kind.Type, null, "b-uri.34", 3L, 4L,
						null);
			else
				fail("unexpected QmlRegistration with qmlName '" + qmlRegistration.getQmlName() + '\'');

		IQObject d_qobj = qtIndex.findQObject(new String[] { "D" });
		assertNotNull(d_qobj);

		Collection<IQmlRegistration> d_qmlRegistrations = d_qobj.getQmlRegistrations();
		assertNotNull(d_qmlRegistrations);
		assertEquals(1, d_qmlRegistrations.size());
		for (IQmlRegistration qmlRegistration : d_qmlRegistrations)
			if ("D".equals(qmlRegistration.getQmlName()))
				assert_checkQmlRegistration(qmlRegistration, IQmlRegistration.Kind.Type, 1L, "d-uri", 2L, 3L, null);
			else
				fail("unexpected QmlRegistration with qmlName '" + qmlRegistration.getQmlName() + '\'');
	}

	private static void assert_checkQmlRegistration(IQmlRegistration qmlRegistration, IQmlRegistration.Kind eKind,
			Long eVersion, String eUri, Long eMaj, Long eMin, String reason) throws Exception {
		assertNotNull(qmlRegistration);
		assertEquals(eKind, qmlRegistration.getKind());
		assertEquals(eVersion, qmlRegistration.getVersion());
		assertEquals(eUri, qmlRegistration.getURI());
		assertEquals(eMaj, qmlRegistration.getMajor());
		assertEquals(eMin, qmlRegistration.getMinor());
		assertEquals(reason, qmlRegistration.getReason());
	}
}
