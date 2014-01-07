/*
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.tests;

import java.util.Collection;

import org.eclipse.cdt.qt.core.index.IQObject;
import org.eclipse.cdt.qt.core.index.IQmlRegistered;
import org.eclipse.cdt.qt.core.index.QtIndex;

public class QmlRegisteredTests extends BaseQtTestCase {

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
	public void testQMLRegisterType() throws Exception {
		loadComment("qmlregistertype.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQObject b_qobj = qtIndex.findQObject(new String[]{ "B" });
		if (!isIndexOk("B", b_qobj))
			return;
		assertNotNull(b_qobj);

		Collection<IQmlRegistered> qmlRegistereds = qtIndex.getQmlRegistered();
		assertNotNull(qmlRegistereds);
		assertEquals(3, qmlRegistereds.size());

		for(IQmlRegistered qmlRegistered : qmlRegistereds) {
			IQObject qobj = qmlRegistered.getQObject();
			assertNotNull(qobj);

			// all values of B should be fully resolved, except for Revision, which was not provided
			if (qobj.getName().equals("B")) {
				assertNull(qmlRegistered.getVersion());
				String qmlName = qmlRegistered.getQmlName();
				assertNotNull(qmlName);
				if ("B".equals(qmlName)) {
					assertEquals(IQmlRegistered.Kind.Type, qmlRegistered.getKind());
					assertEquals("b-uri",         qmlRegistered.getURI());
					assertEquals(Long.valueOf(1), qmlRegistered.getMajor());
					assertEquals(Long.valueOf(2), qmlRegistered.getMinor());
					assertNull(qmlRegistered.getReason());
				} else if ("B34".equals(qmlName)) {
					assertEquals(IQmlRegistered.Kind.Type, qmlRegistered.getKind());
					assertEquals("b-uri.34",      qmlRegistered.getURI());
					assertEquals(Long.valueOf(3), qmlRegistered.getMajor());
					assertEquals(Long.valueOf(4), qmlRegistered.getMinor());
					assertNull(qmlRegistered.getReason());
				} else {
					fail("unexpected uri for B " + qmlName);
				}

			// the values for D are not expected to be resolved (yet), but it does have a Revision
			} else if (qobj.getName().equals("D")) {
				assertEquals(IQmlRegistered.Kind.Type, qmlRegistered.getKind());
				assertEquals(Long.valueOf(1), qmlRegistered.getVersion());
				assertNull(qmlRegistered.getURI());
				assertNull(qmlRegistered.getMajor());
				assertNull(qmlRegistered.getMinor());
				assertNull(qmlRegistered.getQmlName());
				assertNull(qmlRegistered.getReason());

			} else {
				fail("unexpected qmlRegistered " + qobj.getName());
			}
		}
	}

	// class T;
	//
	// static void func()
	// {
	//     qmlRegisterType<T>( "t-uri", 3, 4, "qml-T" );
	// }
	public void testQMLRegisterFwdDecl() throws Exception {
		loadComment("qmlregistertype.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		Collection<IQmlRegistered> qmlRegistereds = qtIndex.getQmlRegistered();
		assertNotNull(qmlRegistereds);
		assertEquals(1, qmlRegistereds.size());

		IQmlRegistered qml = qmlRegistereds.iterator().next();
		assertNotNull(qml);
		assertEquals(IQmlRegistered.Kind.Type, qml.getKind());
		assertEquals("t-uri", qml.getURI());
		assertEquals(Long.valueOf(3), qml.getMajor());
		assertEquals(Long.valueOf(4), qml.getMinor());
		assertEquals("qml-T", qml.getQmlName());
		assertNull(qml.getReason());

		// The QObject has not been defined, so it cannot be found.
		assertNull(qml.getQObject());
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
	public void testQMLRegisterUncreatableType() throws Exception {
		loadComment("qmlregistereduncreatabletype.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQObject b_qobj = qtIndex.findQObject(new String[]{ "B" });
		if (!isIndexOk("B", b_qobj))
			return;
		assertNotNull(b_qobj);

		Collection<IQmlRegistered> qmlRegistereds = qtIndex.getQmlRegistered();
		assertNotNull(qmlRegistereds);
		assertEquals(3, qmlRegistereds.size());

		for(IQmlRegistered qmlRegistered : qmlRegistereds) {
			IQObject qobj = qmlRegistered.getQObject();
			assertNotNull(qobj);

			// all values of B should be fully resolved, except for Revision, which was not provided
			if (qobj.getName().equals("B")) {
				assertNull(qmlRegistered.getVersion());
				String qmlName = qmlRegistered.getQmlName();
				assertNotNull(qmlName);
				if ("B".equals(qmlName)) {
					assertEquals(IQmlRegistered.Kind.Uncreatable, qmlRegistered.getKind());
					assertEquals("b-uri",         qmlRegistered.getURI());
					assertEquals(Long.valueOf(1), qmlRegistered.getMajor());
					assertEquals(Long.valueOf(2), qmlRegistered.getMinor());
					assertEquals(null/*"msg1"*/,          qmlRegistered.getReason());
				} else if ("B34".equals(qmlName)) {
					assertEquals(IQmlRegistered.Kind.Uncreatable, qmlRegistered.getKind());
					assertEquals("b-uri.34",      qmlRegistered.getURI());
					assertEquals(Long.valueOf(3), qmlRegistered.getMajor());
					assertEquals(Long.valueOf(4), qmlRegistered.getMinor());
					assertEquals(null/*"msg2"*/,          qmlRegistered.getReason());
				} else {
					fail("unexpected uri for B " + qmlName);
				}

			// the values for D are not expected to be resolved (yet), but it does have a Revision
			} else if (qobj.getName().equals("D")) {
				assertEquals(IQmlRegistered.Kind.Uncreatable, qmlRegistered.getKind());
				assertNull(qmlRegistered.getVersion());
				assertNull(qmlRegistered.getURI());
				assertNull(qmlRegistered.getMajor());
				assertNull(qmlRegistered.getMinor());
				assertNull(qmlRegistered.getQmlName());
				assertNull(qmlRegistered.getReason());

			} else {
				fail("unexpected qmlRegistered " + qobj.getName());
			}
		}
	}
}
