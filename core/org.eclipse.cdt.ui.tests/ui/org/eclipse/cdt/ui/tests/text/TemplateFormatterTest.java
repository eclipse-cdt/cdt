/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.util.HashMap;

import junit.framework.TestSuite;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.ui.tests.BaseUITestCase;

import org.eclipse.cdt.internal.corext.template.c.CFormatter;

/**
 * Tests for the template formatter (CFormatter).
 *
 * @since 5.0
 */
public class TemplateFormatterTest extends BaseUITestCase {

	private static class TestTemplateContextType extends TemplateContextType {
		TestTemplateContextType() {
			super("test");
			addResolver(new GlobalTemplateVariables.Cursor());
			addResolver(new GlobalTemplateVariables.WordSelection());
			addResolver(new GlobalTemplateVariables.LineSelection());
			addResolver(new GlobalTemplateVariables.Dollar());
			addResolver(new GlobalTemplateVariables.Date());
			addResolver(new GlobalTemplateVariables.Year());
			addResolver(new GlobalTemplateVariables.Time());
			addResolver(new GlobalTemplateVariables.User());
		}
	}

	public static TestSuite suite() {
		return suite(TemplateFormatterTest.class, "_");
	}

	private TemplateContextType fTemplateContextType;
	private String fSelection;
	private HashMap<String, String> fDefaultOptions;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fTemplateContextType= new TestTemplateContextType();
		fSelection= "while (true) {\n\tdoSomething();\n}";
		fDefaultOptions= CCorePlugin.getDefaultOptions();
	}

	@Override
	protected void tearDown() throws Exception {
		CCorePlugin.setOptions(fDefaultOptions);
		super.tearDown();
	}
	
	private void setOption(String key, String value) {
		HashMap<String, String> options= new HashMap<String, String>(1);
		options.put(key, value);
		CCorePlugin.setOptions(options);
	}

	protected void assertFormatterResult() throws Exception {
		assertFormatterResult(false);
	}
	protected void assertFormatterResult(boolean useFormatter) throws Exception {
		StringBuffer[] contents= getContentsForTest(2);
		String before= contents[0].toString().replaceAll("\\r\\n", "\n");
		String expected= contents[1].toString();
		final Document document = new Document(before);
		TemplateContext context= new DocumentTemplateContext(fTemplateContextType, document, 0, document.getLength());
		context.setVariable(GlobalTemplateVariables.SELECTION, fSelection);
		Template template= new Template("test", "descr", fTemplateContextType.getId(), before, false);
		TemplateBuffer buffer= context.evaluate(template);
		CFormatter formatter= new CFormatter("\n", 0, useFormatter, null);
		formatter.format(buffer, context);
		assertEquals(expected, buffer.getString());
	}
	
	//for(int var=0; var<max; var++) {
	//	${cursor}
	//}
	
	//for(int var=0; var<max; var++) {
	//	
	//}
	public void testForLoopTemplateDefault() throws Exception {
		assertFormatterResult();
	}

	//for(int var=0; var<max; var++) {
	//	${cursor}
	//}
	
	//for(int var=0; var<max; var++) {
	//    
	//}
	public void testForLoopTemplateMixedIndent() throws Exception {
		setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, DefaultCodeFormatterConstants.MIXED);
		setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, Integer.toString(8));
		assertFormatterResult();
	}

	//for(int var=0; var<max; var++) {
	//	${line_selection}
	//}
	
	//for(int var=0; var<max; var++) {
	//	while (true) {
	//		doSomething();
	//	}
	//}
	public void testSourroundWithForLoopTemplateDefault() throws Exception {
		assertFormatterResult();
	}

	//for(int var=0; var<max; var++) {
	//	${line_selection}
	//}
	
	//for(int var=0; var<max; var++) {
	//    while (true) {
	//	doSomething();
	//    }
	//}
	public void testSourroundWithForLoopTemplateMixedIndent() throws Exception {
		setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, DefaultCodeFormatterConstants.MIXED);
		setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, Integer.toString(8));
		fSelection= "while (true) {\n    doSomething();\n}";
		assertFormatterResult();
	}
	
	///*!
	// * \brief ShortFunctionDescription${cursor}.
	// *
	// * \return ReturnedValueDescription.
	// */

	///*!
	// * \brief ShortFunctionDescription.
	// *
	// * \return ReturnedValueDescription.
	// */
	public void _testIndentationProblemWithBackslashInComment_Bug274973() throws Exception {
		assertFormatterResult(true);
	}
}
