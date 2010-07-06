/*******************************************************************************
 * Copyright (c) 2010 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.utils;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.ui.refactoring.utils.NameHelper;

public class NameHelperTest extends TestCase {
	
	public void testTrimFieldName() {
		assertEquals("f", NameHelper.trimFieldName("f_"));
		assertEquals("F", NameHelper.trimFieldName("F_"));
		assertEquals("oo", NameHelper.trimFieldName("F_oo"));
		assertEquals("o", NameHelper.trimFieldName("f_o"));
		
		assertEquals("M", NameHelper.trimFieldName("a_M_"));
		assertEquals("bs", NameHelper.trimFieldName("a_bs_"));
		assertEquals("foo_bar", NameHelper.trimFieldName("foo_bar"));
		assertEquals("foo_bar", NameHelper.trimFieldName("foo_bar_"));
		
		assertEquals("foo_b", NameHelper.trimFieldName("foo_b_"));
		
		assertEquals("foo", NameHelper.trimFieldName("foo"));
		assertEquals("foo", NameHelper.trimFieldName("_foo"));
		assertEquals("bar", NameHelper.trimFieldName("_f_bar"));
		
		assertEquals("f", NameHelper.trimFieldName("f__"));
		assertEquals("f", NameHelper.trimFieldName("__f"));
		assertEquals("O__b", NameHelper.trimFieldName("fO__b"));
		assertEquals("Oo", NameHelper.trimFieldName("fOo"));
		assertEquals("O", NameHelper.trimFieldName("fO"));
		assertEquals("MyStatic", NameHelper.trimFieldName("sMyStatic"));
		assertEquals("MyMember", NameHelper.trimFieldName("mMyMember"));
		
		assertEquals("8", NameHelper.trimFieldName("_8"));
		
		assertEquals("8bar", NameHelper.trimFieldName("_8bar_"));
		assertEquals("8bar_8", NameHelper.trimFieldName("_8bar_8"));
		assertEquals("8bAr", NameHelper.trimFieldName("_8bAr"));
		assertEquals("b8Ar", NameHelper.trimFieldName("_b8Ar"));
		
		assertEquals("Id", NameHelper.trimFieldName("Id"));
		assertEquals("ID", NameHelper.trimFieldName("ID"));
		assertEquals("IDS", NameHelper.trimFieldName("IDS"));
		assertEquals("ID", NameHelper.trimFieldName("bID"));
		assertEquals("IdA", NameHelper.trimFieldName("IdA"));
	}

}
