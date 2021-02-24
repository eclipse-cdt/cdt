/*******************************************************************************
 * Copyright (c) 2005, 2012 Andrew Gvozdev (Quoin Inc.) and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *******************************************************************************/
package org.eclipse.cdt.utils;

import static org.junit.Assert.assertNotEquals;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableStatus;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.cdt.utils.cdtvariables.IVariableSubstitutor;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CdtVariableResolverTest extends TestCase {
	private static String acceptedChars = "\\<>&é\"'(§è!çà|@#^¨* []?./+,;:=~)";

	public static Test suite() {
		return new TestSuite(CdtVariableResolverTest.class);
	}

	private class MockSubstitutor implements IVariableSubstitutor {

		@Override
		public String resolveToString(String macroName) throws CdtVariableException {
			if (macroName.equals("null")) {
				return null;
			}
			if (macroName.equals("op")) {
				return "op";
			}
			if (macroName.equals("ro")) {
				return "ro";
			}
			if (macroName.equals("loop")) {
				return "${LOOP}";
			}
			if (macroName.equals("LOOP")) {
				return "${loop}";
			}
			if (macroName.equals(acceptedChars)) {
				return "OK";
			}
			if (macroName.equals("throw")) {
				throw new CdtVariableException(ICdtVariableStatus.TYPE_MACRO_UNDEFINED, null, null, null);
			}
			return "#" + macroName + "#";
		}

		@Override
		public String[] resolveToStringList(String macroName) throws CdtVariableException {

			if (macroName.equals("null-to-list")) {
				return null;
			}

			if (macroName.equals("PATH")) {
				return new String[] { "path0", "path1", "path2", };
			}
			return new String[] { "@" + macroName + "@" };
		}

	}

	private MockSubstitutor mockSubstitutor = new MockSubstitutor();

	//wrapper method to make code easier to read
	private String resolveToString(String key) throws CdtVariableException {
		return CdtVariableResolver.resolveToString(key, mockSubstitutor);
	}

	public void testResolveToString() throws CdtVariableException {

		assertEquals("", resolveToString(null));
		assertEquals("", resolveToString(""));
		assertEquals("Text", resolveToString("Text"));
		assertEquals("#Macro#", resolveToString("${Macro}"));
		assertEquals("", resolveToString("${}"));
		assertEquals("${Nomacro", resolveToString("${Nomacro"));
		assertEquals("Nomacro}", resolveToString("Nomacro}"));
		assertEquals("Text/#Macro#", resolveToString("Text/${Macro}"));
		assertEquals("#Macro#/Text", resolveToString("${Macro}/Text"));
		assertEquals("#Macro1#/#Macro2#", resolveToString("${Macro1}/${Macro2}"));
		assertEquals("#=Macro#", resolveToString("${=Macro}"));
		assertEquals("#=Macro#:#Macro#", resolveToString("${=Macro}:${Macro}"));
		assertEquals("\\#Macro#", resolveToString("\\${Macro}"));
		assertEquals("\\#=Macro#", resolveToString("\\${=Macro}"));
		assertEquals("Text/#=Macro#", resolveToString("Text/${=Macro}"));
		assertEquals("Text/#=Macro#text", resolveToString("Text/${=Macro}text"));
		assertEquals("Text/#Macro#text", resolveToString("Text/${Macro}text"));
		assertEquals("Text/#Macro#text", resolveToString("Text/${Mac${ro}}text"));
		assertEquals("C:\\tmp\\", resolveToString("C:\\tmp\\"));
		assertEquals("OK", resolveToString("${" + acceptedChars + "}"));
		//resolve should only resolve 1 level deep
		assertNotEquals(resolveToString("${LOOP}"), resolveToString(resolveToString("${LOOP}")));

		assertEquals("#workspace_loc:#Macro##", resolveToString("${workspace_loc:${Macro}}"));
		assertEquals("#workspace_loc:#Macro1#/#Macro2##", resolveToString("${workspace_loc:${Macro1}/${Macro2}}"));
		assertEquals("#workspace_loc:#project_loc:/#Macro###",
				resolveToString("${workspace_loc:${project_loc:/${Macro}}}"));
		assertEquals("${ignored}\n${multiline}", resolveToString("${ignored}\n${multiline}"));

	}

	public void testExceptions() throws CdtVariableException {
		// test exceptions
		try {
			assertEquals("Unreacheable", resolveToString("${null}"));
			fail("Exception expected");
		} catch (CdtVariableException e) {
			// expected behavior
		}
		try {
			assertEquals("Unreacheable", resolveToString("${throw}"));
			fail("Exception expected");
		} catch (CdtVariableException e) {
			// expected behavior
		}

		// make sure there is no infinite loop
		assertEquals("${LOOP}", resolveToString("${loop}"));
	}

	public void testAsList() throws CdtVariableException {
		// Syntax ${var} implies using substitutor.resolveToStringList(...)
		{
			String[] list = CdtVariableResolver.resolveToStringList("${PATH}", mockSubstitutor);

			assertNotNull(list);
			assertEquals(3, list.length);
			assertEquals("path0", list[0]);
			assertEquals("path1", list[1]);
			assertEquals("path2", list[2]);
		}

		// uses substitutor.resolveToString(...)
		{
			String[] list = CdtVariableResolver.resolveToStringList("Text", mockSubstitutor);

			assertNotNull(list);
			assertEquals(1, list.length);
			assertEquals("Text", list[0]);
		}

		// uses substitutor.resolveToString(...)
		{
			String[] list = CdtVariableResolver.resolveToStringList("Text${PATH}", mockSubstitutor);

			assertNotNull(list);
			assertEquals(1, list.length);
			assertEquals("Text#PATH#", list[0]);
		}

		// uses substitutor.resolveToString(...)
		{
			String[] list = CdtVariableResolver.resolveToStringList("${PATH}${PATH}", mockSubstitutor);

			assertNotNull(list);
			assertEquals(1, list.length);
			assertEquals("#PATH##PATH#", list[0]);
		}

		// empty var delivers zero-length array
		{
			String[] list = CdtVariableResolver.resolveToStringList("${}", mockSubstitutor);

			assertNotNull(list);
			assertEquals(0, list.length);
		}

		// test exceptions
		try {
			CdtVariableResolver.resolveToStringList("${null-to-list}", mockSubstitutor);
			fail("Exception expected");
		} catch (CdtVariableException e) {
			// expected behavior
		}
	}

	// These tests are very basic not intended to be comprehensive
	public void testOtherBasic() throws CdtVariableException {
		assertEquals("${Macro}", CdtVariableResolver.createVariableReference("Macro"));

		{
			String[] list = { "1", "2", "3" };
			assertEquals("1;2;3", CdtVariableResolver.convertStringListToString(list, ";"));
		}

		{
			String[] list = { "${PATH}", "${Macro}" };
			String[] result = CdtVariableResolver.resolveStringListValues(list, mockSubstitutor, true);
			assertEquals(4, result.length);
			assertEquals("path0", result[0]);
			assertEquals("path1", result[1]);
			assertEquals("path2", result[2]);
			assertEquals("@Macro@", result[3]);
		}
	}

}
