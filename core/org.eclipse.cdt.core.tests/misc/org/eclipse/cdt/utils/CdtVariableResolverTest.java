/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrew Gvozdev (Quoin Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *******************************************************************************/
package org.eclipse.cdt.utils;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableStatus;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.cdt.utils.cdtvariables.IVariableSubstitutor;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CdtVariableResolverTest extends TestCase {

	public static Test suite() {
		return new TestSuite(CdtVariableResolverTest.class);
	}

	private class MockSubstitutor implements IVariableSubstitutor {

		@Override
		public String resolveToString(String macroName)
				throws CdtVariableException {
			if (macroName.equals("null")) {
				return null;
			}
			if (macroName.equals("loop")) {
				return "${LOOP}";
			}
			if (macroName.equals("LOOP")) {
				return "${loop}";
			}
			if (macroName.equals("throw")) {
				throw new CdtVariableException(ICdtVariableStatus.TYPE_MACRO_UNDEFINED,null,null,null);
			}
			return "#"+macroName+"#";
		}

		@Override
		public String[] resolveToStringList(String macroName)
				throws CdtVariableException {
			
			if (macroName.equals("null-to-list")) {
				return null;
			}

			if (macroName.equals("PATH")) {
				return new String[] {
						"path0",
						"path1",
						"path2",
				};
			}
			return new String[] {"@"+macroName+"@"};
		}
		
	}
	private MockSubstitutor mockSubstitutor = new MockSubstitutor();
	
	public void testResolveToString() throws CdtVariableException {
		
		assertEquals("",CdtVariableResolver.resolveToString(null, mockSubstitutor));
		assertEquals("",CdtVariableResolver.resolveToString("", mockSubstitutor));
		assertEquals("Text",CdtVariableResolver.resolveToString("Text", mockSubstitutor));
		assertEquals("#Macro#",CdtVariableResolver.resolveToString("${Macro}", mockSubstitutor));
		assertEquals("",CdtVariableResolver.resolveToString("${}", mockSubstitutor));
		assertEquals("${Nomacro",CdtVariableResolver.resolveToString("${Nomacro", mockSubstitutor));
		assertEquals("Nomacro}",CdtVariableResolver.resolveToString("Nomacro}", mockSubstitutor));
		assertEquals("Text/#Macro#",CdtVariableResolver.resolveToString("Text/${Macro}", mockSubstitutor));
		assertEquals("#Macro#/Text",CdtVariableResolver.resolveToString("${Macro}/Text", mockSubstitutor));
		assertEquals("#Macro1#/#Macro2#",CdtVariableResolver.resolveToString("${Macro1}/${Macro2}", mockSubstitutor));
		assertEquals("${Macro}",CdtVariableResolver.resolveToString("\\${Macro}", mockSubstitutor));
		assertEquals("${Macro}:#Macro#",CdtVariableResolver.resolveToString("\\${Macro}:${Macro}", mockSubstitutor));
		assertEquals("\\#Macro#",CdtVariableResolver.resolveToString("\\\\${Macro}", mockSubstitutor));
		assertEquals("\\${Macro}",CdtVariableResolver.resolveToString("\\\\\\${Macro}", mockSubstitutor));
		assertEquals("C:\\tmp\\",CdtVariableResolver.resolveToString("C:\\tmp\\", mockSubstitutor));
		
		assertEquals("#workspace_loc:#Macro##",CdtVariableResolver.resolveToString("${workspace_loc:${Macro}}", mockSubstitutor));
		assertEquals("#workspace_loc:#Macro1#/#Macro2##",CdtVariableResolver.resolveToString("${workspace_loc:${Macro1}/${Macro2}}", mockSubstitutor));
		assertEquals("#workspace_loc:#project_loc:/#Macro###",CdtVariableResolver.resolveToString("${workspace_loc:${project_loc:/${Macro}}}", mockSubstitutor));

	}

	public void testExceptions() throws CdtVariableException {
		// test exceptions
		try {
			assertEquals("Unreacheable",CdtVariableResolver.resolveToString("${null}", mockSubstitutor));
			fail("Exception expected");
		} catch (CdtVariableException e) {
			// expected behavior
		}
		try {
			assertEquals("Unreacheable",CdtVariableResolver.resolveToString("${throw}", mockSubstitutor));
			fail("Exception expected");
		} catch (CdtVariableException e) {
			// expected behavior
		}
		
		// make sure there is no infinite loop
		assertEquals("${LOOP}",CdtVariableResolver.resolveToString("${loop}", mockSubstitutor));
	}

	public void testAsList() throws CdtVariableException {
		// Syntax ${var} implies using substitutor.resolveToStringList(...)
		{
			String[] list = CdtVariableResolver.resolveToStringList("${PATH}", mockSubstitutor);
			
			assertNotNull(list);
			assertEquals(3,list.length);
			assertEquals("path0",list[0]);
			assertEquals("path1",list[1]);
			assertEquals("path2",list[2]);
		}
		
		// uses substitutor.resolveToString(...)
		{
			String[] list = CdtVariableResolver.resolveToStringList("Text", mockSubstitutor);
			
			assertNotNull(list);
			assertEquals(1,list.length);
			assertEquals("Text",list[0]);
		}
		
		// uses substitutor.resolveToString(...)
		{
			String[] list = CdtVariableResolver.resolveToStringList("Text${PATH}", mockSubstitutor);
			
			assertNotNull(list);
			assertEquals(1,list.length);
			assertEquals("Text#PATH#",list[0]);
		}
		
		// uses substitutor.resolveToString(...)
		{
			String[] list = CdtVariableResolver.resolveToStringList("${PATH}${PATH}", mockSubstitutor);
			
			assertNotNull(list);
			assertEquals(1,list.length);
			assertEquals("#PATH##PATH#",list[0]);
		}
		
		// empty var delivers zero-length array
		{
			String[] list = CdtVariableResolver.resolveToStringList("${}", mockSubstitutor);
			
			assertNotNull(list);
			assertEquals(0,list.length);
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
		assertEquals("${Macro}",CdtVariableResolver.createVariableReference("Macro"));

		{
			String[] list = { "1","2","3" };
			assertEquals("1;2;3",CdtVariableResolver.convertStringListToString(list,";"));
		}

		{
			String[] list = { "${PATH}", "${Macro}" };
			String[] result = CdtVariableResolver.resolveStringListValues(list, mockSubstitutor, true);
			assertEquals(4,result.length);
			assertEquals("path0",result[0]);
			assertEquals("path1",result[1]);
			assertEquals("path2",result[2]);
			assertEquals("@Macro@",result[3]);
		}
	}

}
