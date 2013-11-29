/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.tests;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.cdt.qt.core.index.IQObject;
import org.eclipse.cdt.qt.core.index.QtIndex;

public class QObjectTests extends BaseQtTestCase {

	// #include "junit-QObject.hh"
	// class T {};
	// class B1 : public QObject {Q_OBJECT};
	// class B2 : public QObject {Q_OBJECT};
	// class B3 : public QObject {Q_OBJECT};
	// class D1 : public B1, public B2, private B3, public T {Q_OBJECT};
	// class D2 : public T,  public QObject {};
	public void testGetBases() throws Exception {
		loadComment("bases.hh");

		QtIndex qtIndex = QtIndex.getIndex(fProject);
		assertNotNull(qtIndex);

		IQObject qobj_B1 = qtIndex.findQObject(new String[]{ "B1" });
		if (!isIndexOk("B1", qobj_B1))
			return;
		IQObject qobj_D1 = qtIndex.findQObject(new String[]{ "D1" });
		assertNotNull(qobj_B1);
		assertNotNull(qobj_D1);

		Collection<IQObject> d1_bases = qobj_D1.getBases();
		assertNotNull(d1_bases);
		assertEquals(2, d1_bases.size());
		Iterator<IQObject> iterator = d1_bases.iterator();
		assertEquals(qobj_B1.getName(), iterator.next().getName());
		assertEquals("B2", iterator.next().getName());

		// D2 is not a QObject because it doesn't expand the Q_OBJECT macro
		IQObject qobj_D2 = qtIndex.findQObject(new String[]{ "D2" });
		assertNull(qobj_D2);
	}
}
