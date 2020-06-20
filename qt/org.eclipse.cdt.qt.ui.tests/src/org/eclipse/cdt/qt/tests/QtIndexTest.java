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
package org.eclipse.cdt.qt.tests;

import org.eclipse.cdt.internal.qt.core.index.IQObject;
import org.eclipse.cdt.internal.qt.core.index.QtIndex;

public class QtIndexTests extends BaseQtTestCase {

	private static final String Filename_testCache = "testCache.hh";

	// #include "junit-QObject.hh"
	// class B : public QObject
	// {
	// Q_OBJECT
	// Q_PROPERTY(bool allowed READ isAllowed)
	// };
	public void changeBDecl() throws Exception {
		loadComment(Filename_testCache);
	}

	// #include "junit-QObject.hh"
	// class B : public QObject
	// {
	// Q_OBJECT
	// };
	public void testLookup() throws Exception {
		loadComment(Filename_testCache);

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		// make sure the instance can be found
		IQObject qobj1 = qtIndex.findQObject(new String[] { "B" });
		assertNotNull(qobj1);
		assertEquals("B", qobj1.getName());

		// make sure the instance is still found after the content changes
		changeBDecl();
		IQObject qobj2 = qtIndex.findQObject(new String[] { "B" });
		assertNotNull(qobj2);
		assertEquals("B", qobj2.getName());
	}
}
