/*******************************************************************************
 * Copyright (c) 2010 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.utils;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.ui.refactoring.gettersandsetters.GetterSetterNameGenerator;

public class AccessorNameGeneratorTest extends TestCase {
	
	public void testTrimFieldName() {
		assertEquals("f", GetterSetterNameGenerator.trimFieldName("f_"));
		assertEquals("F", GetterSetterNameGenerator.trimFieldName("F_"));
		assertEquals("oo", GetterSetterNameGenerator.trimFieldName("F_oo"));
		assertEquals("o", GetterSetterNameGenerator.trimFieldName("f_o"));
		
		assertEquals("M", GetterSetterNameGenerator.trimFieldName("a_M_"));
		assertEquals("bs", GetterSetterNameGenerator.trimFieldName("a_bs_"));
		assertEquals("foo_bar", GetterSetterNameGenerator.trimFieldName("foo_bar"));
		assertEquals("foo_bar", GetterSetterNameGenerator.trimFieldName("foo_bar_"));
		
		assertEquals("foo_b", GetterSetterNameGenerator.trimFieldName("foo_b_"));
		
		assertEquals("foo", GetterSetterNameGenerator.trimFieldName("foo"));
		assertEquals("foo", GetterSetterNameGenerator.trimFieldName("_foo"));
		assertEquals("bar", GetterSetterNameGenerator.trimFieldName("_f_bar"));
		
		assertEquals("f", GetterSetterNameGenerator.trimFieldName("f__"));
		assertEquals("f", GetterSetterNameGenerator.trimFieldName("__f"));
		assertEquals("O__b", GetterSetterNameGenerator.trimFieldName("fO__b"));
		assertEquals("Oo", GetterSetterNameGenerator.trimFieldName("fOo"));
		assertEquals("O", GetterSetterNameGenerator.trimFieldName("fO"));
		assertEquals("MyStatic", GetterSetterNameGenerator.trimFieldName("sMyStatic"));
		assertEquals("MyMember", GetterSetterNameGenerator.trimFieldName("mMyMember"));
		
		assertEquals("8", GetterSetterNameGenerator.trimFieldName("_8"));
		
		assertEquals("8bar", GetterSetterNameGenerator.trimFieldName("_8bar_"));
		assertEquals("8bar_8", GetterSetterNameGenerator.trimFieldName("_8bar_8"));
		assertEquals("8bAr", GetterSetterNameGenerator.trimFieldName("_8bAr"));
		assertEquals("b8Ar", GetterSetterNameGenerator.trimFieldName("_b8Ar"));
		
		assertEquals("Id", GetterSetterNameGenerator.trimFieldName("Id"));
		assertEquals("ID", GetterSetterNameGenerator.trimFieldName("ID"));
		assertEquals("IDS", GetterSetterNameGenerator.trimFieldName("IDS"));
		assertEquals("ID", GetterSetterNameGenerator.trimFieldName("bID"));
		assertEquals("Id", GetterSetterNameGenerator.trimFieldName("MId"));
		assertEquals("IdA", GetterSetterNameGenerator.trimFieldName("IdA"));
	}
}
