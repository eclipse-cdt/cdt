/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.cdt.core.model.Flags;
import org.eclipse.cdt.internal.core.model.IConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Peter Graves
 *
 * This is a very simple set of sanity tests for the flags class to make sure
 * there are no very silly problems in the class. It also verifies that there
 * is no overlap in the IConstants.
 */
public class FlagTests {

	int flags[];

	/**
	 * Sets up the test fixture.
	 *
	 * Called before every test case method.
	 *
	 * Example code test the packages in the project
	 *  "com.qnx.tools.ide.cdt.core"
	 */
	@BeforeEach
	protected void setUp() {
		flags = new int[15];
		flags[0] = IConstants.AccPublic;
		flags[1] = IConstants.AccPrivate;
		flags[2] = IConstants.AccProtected;
		flags[3] = IConstants.AccStatic;
		flags[4] = IConstants.AccExtern;
		flags[5] = IConstants.AccInline;
		flags[6] = IConstants.AccVolatile;
		flags[7] = IConstants.AccRegister;
		flags[8] = IConstants.AccExplicit;
		flags[9] = IConstants.AccExport;
		flags[10] = IConstants.AccAbstract;
		flags[11] = IConstants.AccMutable;
		flags[12] = IConstants.AccAuto;
		flags[13] = IConstants.AccVirtual;
		flags[14] = IConstants.AccTypename;

	}

	@Test
	public void testIsStatic() {
		int x;
		assertTrue(Flags.isStatic(IConstants.AccStatic), "isStatic with a static");
		for (x = 0; x < flags.length; x++) {
			if (flags[x] != IConstants.AccStatic)
				assertTrue(!Flags.isStatic(flags[x]), "isStatic with a non-static");
		}
	}

	@Test
	public void testIsAbstract() {
		int x;
		assertTrue(Flags.isAbstract(IConstants.AccAbstract), "isAbstract with a abstract");
		for (x = 0; x < flags.length; x++) {
			if (flags[x] != IConstants.AccAbstract)
				assertTrue(!Flags.isAbstract(flags[x]), "isAbstract with a non-abstract");
		}
	}

	@Test
	public void testIsExplicit() {
		int x;
		assertTrue(Flags.isExplicit(IConstants.AccExplicit), "isExplicit with a explicit");
		for (x = 0; x < flags.length; x++) {
			if (flags[x] != IConstants.AccExplicit)
				assertTrue(!Flags.isExplicit(flags[x]), "isExplicit with a non-explicit");
		}
	}

	@Test
	public void testIsExport() {
		int x;
		assertTrue(Flags.isExport(IConstants.AccExport), "isExport with a Export");
		for (x = 0; x < flags.length; x++) {
			if (flags[x] != IConstants.AccExport)
				assertTrue(!Flags.isExport(flags[x]), "isExport with a non-Export");
		}
	}

	@Test
	public void testIsExtern() {
		int x;
		assertTrue(Flags.isExtern(IConstants.AccExtern), "isExtern with a Extern");
		for (x = 0; x < flags.length; x++) {
			if (flags[x] != IConstants.AccExtern)
				assertTrue(!Flags.isExtern(flags[x]), "isExtern with a non-Extern");
		}
	}

	@Test
	public void testIsInline() {
		int x;
		assertTrue(Flags.isInline(IConstants.AccInline), "isInline with a Inline");
		for (x = 0; x < flags.length; x++) {
			if (flags[x] != IConstants.AccInline)
				assertTrue(!Flags.isInline(flags[x]), "isInline with a non-Inline");
		}
	}

	@Test
	public void testIsMutable() {
		int x;
		assertTrue(Flags.isMutable(IConstants.AccMutable), "isMutable with a Mutable");
		for (x = 0; x < flags.length; x++) {
			if (flags[x] != IConstants.AccMutable)
				assertTrue(!Flags.isMutable(flags[x]), "isMutable with a non-Mutable");
		}
	}

	@Test
	public void testIsPrivate() {
		int x;
		assertTrue(Flags.isPrivate(IConstants.AccPrivate), "isPrivate with a Private");
		for (x = 0; x < flags.length; x++) {
			if (flags[x] != IConstants.AccPrivate)
				assertTrue(!Flags.isPrivate(flags[x]), "isPrivate with a non-Private");
		}
	}

	@Test
	public void testIsPublic() {
		int x;
		assertTrue(Flags.isPublic(IConstants.AccPublic), "isPublic with a Public");
		for (x = 0; x < flags.length; x++) {
			if (flags[x] != IConstants.AccPublic)
				assertTrue(!Flags.isPublic(flags[x]), "isPublic with a non-Public");
		}
	}

	@Test
	public void testIsProtected() {
		int x;
		assertTrue(Flags.isProtected(IConstants.AccProtected), "isProtected with a Protected");
		for (x = 0; x < flags.length; x++) {
			if (flags[x] != IConstants.AccProtected)
				assertTrue(!Flags.isProtected(flags[x]), "isProtected with a non-Protected");
		}
	}

	@Test
	public void testIsRegister() {
		int x;
		assertTrue(Flags.isRegister(IConstants.AccRegister), "isRegister with a Register");
		for (x = 0; x < flags.length; x++) {
			if (flags[x] != IConstants.AccRegister)
				assertTrue(!Flags.isRegister(flags[x]), "isRegister with a non-Register");
		}
	}

	@Test
	public void testIsVirtual() {
		int x;
		assertTrue(Flags.isVirtual(IConstants.AccVirtual), "isVirtual with a Virtual");
		for (x = 0; x < flags.length; x++) {
			if (flags[x] != IConstants.AccVirtual)
				assertTrue(!Flags.isVirtual(flags[x]), "isVirtual with a non-Virtual");
		}
	}

	@Test
	public void testIsVolatile() {
		int x;
		assertTrue(Flags.isVolatile(IConstants.AccVolatile), "isVolatile with a Volatile");
		for (x = 0; x < flags.length; x++) {
			if (flags[x] != IConstants.AccVolatile)
				assertTrue(!Flags.isVolatile(flags[x]), "isVolatile with a non-Volatile");
		}
	}

}
