/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.debug.internal.core.sourcelookup.MapEntrySourceContainer;
import org.eclipse.core.runtime.IPath;

@SuppressWarnings("restriction")
public class MapEntrySourceContainerTests extends TestCase {

    public static Test suite() {
		return new TestSuite(MapEntrySourceContainerTests.class);
	}

	public MapEntrySourceContainerTests(String name) {
		super(name);
	}
	
	public void testUNCPath() {
		String uncPath = "//server/path/on/server";
		IPath path = MapEntrySourceContainer.createPath(uncPath);
		assertEquals(uncPath, path.toString());

		uncPath = "\\\\server\\path\\on\\server";
		path = MapEntrySourceContainer.createPath(uncPath);
		assertEquals(uncPath, path.toOSString());
	}
}
