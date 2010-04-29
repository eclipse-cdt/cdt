/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Sergey Prigogin, Google
 *     Andrew Ferguson (Symbian)
 *     Andrew Gvozdev
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TabsToSpacesConverter;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.doctools.DefaultMultilineCommentAutoEditStrategy;

import org.eclipse.cdt.internal.formatter.DefaultCodeFormatterOptions;

import org.eclipse.cdt.internal.ui.text.CAutoIndentStrategy;
import org.eclipse.cdt.internal.ui.text.CTextTools;

/**
 * Testing the auto indent strategies.
 */
public class CAutoIndentTest extends AbstractAutoEditTest {

	private HashMap<String, String> fOptions;
	private List<IStatus> fStatusLog;
	private ILogListener fLogListener;

	
	/**
	 * @param name
	 */
	public CAutoIndentTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(CAutoIndentTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
//		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();  
//		shell.forceActive();
//		shell.forceFocus();
		fOptions= CCorePlugin.getOptions();

		fStatusLog= Collections.synchronizedList(new ArrayList<IStatus>());
		fLogListener= new ILogListener() {
			public void logging(IStatus status, String plugin) {
				if(!status.isOK()) {
					fStatusLog.add(status);
				}
			}
		};
		final Plugin plugin = CUIPlugin.getDefault();
		if (plugin != null) {
			plugin.getLog().addLogListener(fLogListener);
		}
}
	
	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		final Plugin plugin = CUIPlugin.getDefault();
		if (plugin != null) {
			plugin.getLog().removeLogListener(fLogListener);
		}
		CCorePlugin.setOptions(fOptions);
		super.tearDown();
	}

	private AutoEditTester createAutoEditTester() {
		CTextTools textTools = CUIPlugin.getDefault().getTextTools();
		IDocument doc = new Document();
		textTools.setupCDocument(doc);
		AutoEditTester tester = new AutoEditTester(doc, ICPartitions.C_PARTITIONING);
		
		tester.setAutoEditStrategy(IDocument.DEFAULT_CONTENT_TYPE, new CAutoIndentStrategy(ICPartitions.C_PARTITIONING, null));
		tester.setAutoEditStrategy(ICPartitions.C_MULTI_LINE_COMMENT, new DefaultMultilineCommentAutoEditStrategy());
		tester.setAutoEditStrategy(ICPartitions.C_PREPROCESSOR, new CAutoIndentStrategy(ICPartitions.C_PARTITIONING, null));
		return tester;
	}

	public void testCAutoIndent() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		tester.type("void main() {\n"); //$NON-NLS-1$
		assertEquals(1, tester.getCaretLine());
		// Nested statement is indented by one.
		assertEquals(1, tester.getCaretColumn());
		// The brace was closed automatically.
		assertEquals("}", tester.getLine(1)); //$NON-NLS-1$
		tester.type("if (expression1 &&\n"); //$NON-NLS-1$
		assertEquals(2, tester.getCaretLine());
		// Continuation line is indented by two relative to the statement.
		assertEquals(3, tester.getCaretColumn());
		tester.type("expression2 &&\n"); //$NON-NLS-1$
		assertEquals(3, tester.getCaretLine());
		// Second continuation line is also indented by two relative to the statement.
		assertEquals(3, tester.getCaretColumn());
		tester.type("expression3) {"); //$NON-NLS-1$
		// Remember caret position.
		int offset = tester.getCaretOffset();
		// Press Enter
        tester.type("\n");  //$NON-NLS-1$
		assertEquals(4, tester.getCaretLine());
		// Nested statement is indented by one relative to the containing statement.
		assertEquals(2, tester.getCaretColumn());
		// The brace was closed automatically.
		assertEquals("\t}", tester.getLine(1)); //$NON-NLS-1$
		tester.type("int x = 5;"); //$NON-NLS-1$
		// Move caret back after the opening brace.
		tester.setCaretOffset(offset);
		// Press Enter
        tester.type("\n"); //$NON-NLS-1$
		assertEquals(4, tester.getCaretLine());
		// Nested statement is indented by one relative to the containing statement.
		assertEquals(2, tester.getCaretColumn());
        // No auto closing brace since the braces are already balanced.
		assertEquals("\t\tint x = 5;", tester.getLine(1)); //$NON-NLS-1$
	}

	public void testPasteAutoIndent() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		tester.type("class A {\n"); //$NON-NLS-1$
		tester.goTo(1, 0);
		tester.paste("class B {\n" +
				     "protected:\n" +
				     "\tB();\n" +
				     "public:\n" +
				     "\tint getX() const {\n" +
				     "\t\treturn x_;\n" +
				     "\t}\n" +
				     "private:\n" +
				     "\tint x_;\n" +
				     "};\n"); //$NON-NLS-1$
		tester.goTo(1, 0);
		assertEquals("\tclass B {", tester.getLine(0)); //$NON-NLS-1$
		assertEquals("\tprotected:", tester.getLine(1)); //$NON-NLS-1$
		assertEquals("\t\tB();", tester.getLine(2)); //$NON-NLS-1$
		assertEquals("\tpublic:", tester.getLine(3)); //$NON-NLS-1$
		assertEquals("\t\tint getX() const {", tester.getLine(4)); //$NON-NLS-1$
		assertEquals("\t\t\treturn x_;", tester.getLine(5)); //$NON-NLS-1$
		assertEquals("\t\t}", tester.getLine(6)); //$NON-NLS-1$
		assertEquals("\tprivate:", tester.getLine(7)); //$NON-NLS-1$
		assertEquals("\t\tint x_;", tester.getLine(8)); //$NON-NLS-1$
		assertEquals("\t};", tester.getLine(9)); //$NON-NLS-1$
	}

	public void testDefaultAutoIndent() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		tester.type("   initial indent=5\n"); //$NON-NLS-1$
		assertEquals(1, tester.getCaretLine());
		assertEquals(5, tester.getCaretColumn());
		tester.type("indent=5\n"); //$NON-NLS-1$
		assertEquals(2, tester.getCaretLine());
		assertEquals(5, tester.getCaretColumn());
		tester.backspace();
		tester.type("indent=4\n"); //$NON-NLS-1$
		assertEquals(3, tester.getCaretLine());
		assertEquals(4, tester.getCaretColumn());
		tester.backspace();
		tester.backspace();
		tester.type("indent=2\n"); //$NON-NLS-1$
		assertEquals(4, tester.getCaretLine());
		assertEquals(2, tester.getCaretColumn());
		tester.type("\n"); //$NON-NLS-1$
		assertEquals(5, tester.getCaretLine());
		assertEquals(2, tester.getCaretColumn());
	}

	public void testCCommentAutoIndent() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		tester.type("/*\n"); //$NON-NLS-1$
		assertEquals(ICPartitions.C_MULTI_LINE_COMMENT, tester.getContentType(tester.getCaretOffset()-1));
		assertEquals(1, tester.getCaretLine());
		assertEquals(3, tester.getCaretColumn());
		assertEquals(" * ", tester.getLine()); //$NON-NLS-1$
		tester.type('\n');
		assertEquals(" * ", tester.getLine()); //$NON-NLS-1$
		tester.type('/');
		assertEquals(" */", tester.getLine()); //$NON-NLS-1$
		tester.type('\n');
		assertEquals(3, tester.getCaretLine());
		assertEquals("", tester.getLine()); //$NON-NLS-1$
		assertEquals(0, tester.getCaretColumn());
	}

	public void testPreprocessorAutoIndent() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		tester.type("void main() {\n"); //$NON-NLS-1$
		assertEquals(1, tester.getCaretLine());
		// Nested statement is indented by one.
		assertEquals(1, tester.getCaretColumn());
		// The brace was closed automatically.
		assertEquals("}", tester.getLine(1)); //$NON-NLS-1$
		tester.type("#define"); //$NON-NLS-1$
		assertEquals("#define", tester.getLine()); //$NON-NLS-1$
		tester.type(" FOREVER \\\n");
		assertEquals(1, tester.getCaretColumn());
		tester.type("for(;;) \\\n");
		assertEquals(1, tester.getCaretColumn());
		tester.type("\t{");
		assertEquals(2, tester.getCaretColumn());
		assertEquals("\t{", tester.getLine());
		tester.type("\\\n");
		assertEquals(2, tester.getCaretColumn());
		assertEquals("\t}", tester.getLine(1));
	}

	public void testPasteBlockCommentAutoIndent() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		tester.type("class A {\n};"); //$NON-NLS-1$
		tester.goTo(1, 0);
		tester.paste("/*\n" +
				     " * block comment\n" +
				     " */\n");
		tester.goTo(1, 0);
		assertEquals("\t/*", tester.getLine(0)); //$NON-NLS-1$
		assertEquals("\t * block comment", tester.getLine(1)); //$NON-NLS-1$
		assertEquals("\t */", tester.getLine(2)); //$NON-NLS-1$
	}

	public void testPasteLineCommentAutoIndent() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		tester.type("class A {\n};"); //$NON-NLS-1$
		tester.goTo(1, 0);
		tester.paste("// int f;\n");
		tester.goTo(1, 0);
		assertEquals("\t// int f;", tester.getLine(0)); //$NON-NLS-1$
	}

	/**
	 * Tests brackets with semi-colons are inserted in the appropriate
	 * contexts
	 * @throws BadLocationException
	 */
	public void testBracketWithSemiColonInsertion() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester(); 
		String[] kw= new String[] {"class", "union", "struct", "enum"};
		String[] kw_inh= new String[] {"class", "union", "struct"};
		String[] kw_anon= new String[] {"union", "struct", "enum"};

		for(int i=0; i<kw.length; i++) {
			tester.reset();

			tester.type("\n\n\n "+kw[i]+" A {\n"); //$NON-NLS-1$
			assertEquals("\n\n\n "+kw[i]+" A {\n\t \n };", tester.fDoc.get()); //$NON-NLS-1$
		}
		
		for(int i=0; i<kw.length; i++) {
			tester.reset();

			tester.type("\n\n\n"+kw[i]+" A {\n"); //$NON-NLS-1$
			assertEquals("\n\n\n"+kw[i]+" A {\n\t\n};", tester.fDoc.get()); //$NON-NLS-1$
		}
		
		for(int i=0; i<kw.length; i++) {		
			tester.reset();

			tester.type("\n\n\n "+kw[i]+" A {\n"); //$NON-NLS-1$
			assertEquals("\n\n\n "+kw[i]+" A {\n\t \n };", tester.fDoc.get()); //$NON-NLS-1$
		}
		
		for(int i=0; i<kw.length; i++) {		
			tester.reset();

			tester.type("\n// foo\n\n\n//bar\n\n"); //$NON-NLS-1$
			tester.goTo(2,0);
			tester.type(kw[i]+" A {\n"); //$NON-NLS-1$
			assertEquals("\n// foo\n"+kw[i]+" A {\n\t\n};\n\n//bar\n\n", tester.fDoc.get()); //$NON-NLS-1$
		}

		// this tests for a sensible behaviour for enums, although the
		// code generated is invalid, its the user entered part that is
		// the problem
		for(int i=0; i<kw_inh.length; i++) {		
			tester.reset();

			tester.type("\n\n\n"+kw_inh[i]+" A\n:\npublic B\n,\npublic C\n{\n"); //$NON-NLS-1$
			assertEquals("\n\n\n"+kw_inh[i]+" A\n:\n\tpublic B\n\t,\n\tpublic C\n\t{\n\t\t\n\t};", tester.fDoc.get()); //$NON-NLS-1$
		}
		
		for(int i=0; i<kw.length; i++) {		
			tester.reset();

			tester.type("\n// foo\n\n\n//bar\n\n"); //$NON-NLS-1$
			tester.goTo(2,0);
			tester.type(kw[i]+" /* for(int i=0; i<100; i++) {} */\nA \n{\n"); //$NON-NLS-1$
			assertEquals("\n// foo\n"+kw[i]+" /* for(int i=0; i<100; i++) {} */\nA \n{\n\t\n};\n\n//bar\n\n", tester.fDoc.get()); //$NON-NLS-1$
		}		

		for(int i=0; i<kw_anon.length; i++) {		
			tester.reset();

			tester.type("\n\n\n"+kw_anon[i]+" {\n"); //$NON-NLS-1$
			assertEquals("\n\n\n"+kw_anon[i]+" {\n\t\n};", tester.fDoc.get()); //$NON-NLS-1$
		}
}
	
	/**
	 * Tests that brackets are inserted (without semi-colons) in appropriate
	 * contexts
	 * @throws BadLocationException
	 */
	public void testBracketInsertion() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester();
		
		tester.type("\nfor (;;) {\n");
		assertEquals("\nfor (;;) {\n\t\n}", tester.fDoc.get()); //$NON-NLS-1$
		
		tester.reset();
		tester.type("\nfor /*class*/ (;;) {\n"); //$NON-NLS-1$
		assertEquals("\nfor /*class*/ (;;) {\n\t\n}", tester.fDoc.get()); //$NON-NLS-1$	
		
		tester.reset();
		tester.type("\nfor (;;) /*class*/ {\n"); //$NON-NLS-1$
		assertEquals("\nfor (;;) /*class*/ {\n\t\n}", tester.fDoc.get()); //$NON-NLS-1$

		tester.reset();
		tester.type("\nint i[5]={\n"); //$NON-NLS-1$
		assertEquals("\nint i[5]={\n\t\t\n};", tester.fDoc.get()); //$NON-NLS-1$
	}

	public void testBracketIndentForConstructorDefinition_Bug183814() throws BadLocationException {
		DefaultCodeFormatterOptions whitesmiths= DefaultCodeFormatterOptions.getWhitesmithsSettings();
		CCorePlugin.setOptions(new HashMap<String, String>(whitesmiths.getMap()));
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		
		tester.type("Foo::Foo()\n{");
		assertEquals("Foo::Foo()\n    {", tester.fDoc.get());
	}
	
	public void testSmartPasteWhitesmiths_Bug180531() throws Exception {
		DefaultCodeFormatterOptions whitesmiths= DefaultCodeFormatterOptions.getWhitesmithsSettings();
		CCorePlugin.setOptions(new HashMap<String, String>(whitesmiths.getMap()));
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		
		tester.type("A::~A()\n{");
		assertEquals("A::~A()\n    {", tester.fDoc.get());
		tester.type("\ndelete x;");
		assertEquals("A::~A()\n    {\n    delete x;\n    }", tester.fDoc.get());
		
		tester.setCaretOffset(tester.fDoc.getLength());
		tester.type('\n');
		String copy= tester.fDoc.get();
		tester.paste(copy);
		assertEquals(copy+copy, tester.fDoc.get());
	}
	
	public void testIndentInsideNamespaceDefinition_Bug188007() throws Exception {
		AutoEditTester tester = createAutoEditTester();
		
		tester.type("namespace ns {\n");
		assertEquals("", tester.getLine());
		assertEquals(0, tester.getCaretColumn());
		
		DefaultCodeFormatterOptions defaultOptions= DefaultCodeFormatterOptions.getDefaultSettings();
		defaultOptions.indent_body_declarations_compare_to_namespace_header= true;
		CCorePlugin.setOptions(new HashMap<String, String>(defaultOptions.getMap()));
		tester = createAutoEditTester();
		
		tester.type("namespace ns {\n");
		assertEquals("\t", tester.getLine());
		assertEquals(1, tester.getCaretColumn());
	}
	
	public void testSmartPaste_Bug215310() throws Exception  {
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		
		tester.type("#define S \\ \n");
		tester.type("d\n");
		tester.paste(
			"class B : private A \n" + 
			"{\n" + 
			"};\n"
		);
		
		assertNoError();
	}

	public void testAutoIndentDisabled_Bug219923() throws Exception  {
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		IPreferenceStore store= PreferenceConstants.getPreferenceStore();
		try {
			store.setValue(PreferenceConstants.EDITOR_AUTO_INDENT, false);
			tester.type("void main() {\n"); //$NON-NLS-1$
			assertEquals(1, tester.getCaretLine());
			// Nested statement is not indented
			assertEquals(0, tester.getCaretColumn());
			// The brace was closed automatically.
			assertEquals("}", tester.getLine(1)); //$NON-NLS-1$
			tester.type('\t');
			tester.type('\n');
			// indent from previous line
			assertEquals(1, tester.getCaretColumn());
			tester.type('{');
			tester.type('\n');
			// indent from previous line
			assertEquals(1, tester.getCaretColumn());
			tester.type('}');
			tester.type('\n');
			// indent from previous line
			assertEquals(1, tester.getCaretColumn());
			tester.backspace();
			tester.type('\n');
			// indent from previous line
			assertEquals(0, tester.getCaretColumn());
		} finally {
			store.setToDefault(PreferenceConstants.EDITOR_AUTO_INDENT);
		}
	}

	public void testTabsAsSpaces_SmartIndentDisabled_Bug242707() throws Exception  {
		HashMap<String, String> options = new HashMap<String, String>();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, CCorePlugin.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "3");
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "3");
		DefaultCodeFormatterOptions defaultOptions= DefaultCodeFormatterOptions.getDefaultSettings();
		defaultOptions.set(options);
		CCorePlugin.setOptions(new HashMap<String, String>(defaultOptions.getMap()));
		
		IPreferenceStore store= PreferenceConstants.getPreferenceStore();
		store.setValue(PreferenceConstants.EDITOR_SMART_TAB, false);
		
		AutoEditTester tester = createAutoEditTester();
		
		TabsToSpacesConverter tabToSpacesConverter = new TabsToSpacesConverter();
		tabToSpacesConverter.setNumberOfSpacesPerTab(3);
		tabToSpacesConverter.setLineTracker(new DefaultLineTracker());
		tester.setTabsToSpacesConverter(tabToSpacesConverter);
		
		
		try {
			tester.type("void main() {\n"); //$NON-NLS-1$
			assertEquals(1, tester.getCaretLine());
			// Nested statement is indented
			assertEquals(3, tester.getCaretColumn());
			assertEquals("   ", tester.getLine(0)); //$NON-NLS-1$
			// The brace was closed automatically.
			assertEquals("}", tester.getLine(1)); //$NON-NLS-1$
			tester.type('\t');
			// Indent from previous line + expanded tab
			assertEquals("      ", tester.getLine(0)); //$NON-NLS-1$
			// Return normal indentation 
			tester.backspace(3);
			assertEquals("   ", tester.getLine(0)); //$NON-NLS-1$
			tester.type("for (;;)\n");
			// Check indentation under "for" operator
			assertEquals("      ", tester.getLine(0)); //$NON-NLS-1$
			// Remove all symbols on the line
			tester.backspace(6);
			assertEquals("", tester.getLine(0)); //$NON-NLS-1$
			// Tabulation should not trigger autoindent, just 1 tab filled with spaces
			tester.type("\t");
			assertEquals("   ", tester.getLine(0)); //$NON-NLS-1$
			tester.type("\t");
			// Check one more tab
			assertEquals("      ", tester.getLine(0)); //$NON-NLS-1$
			// Clean the line to repeat 2 last entries but with spaces
			tester.backspace(6);
			assertEquals("", tester.getLine(0)); //$NON-NLS-1$
			// 1-st sequence of spaces
			tester.type("     ");
			assertEquals("     ", tester.getLine(0)); //$NON-NLS-1$
			// 2-nd sequence of spaces
			tester.type("     ");
			assertEquals("          ", tester.getLine(0)); //$NON-NLS-1$
			
		} finally {
			store.setToDefault(PreferenceConstants.EDITOR_SMART_TAB);
		}
	}

	public void testSmartIndentAfterArrayIndexOperator_Bug291821() throws Exception  {
		AutoEditTester tester = createAutoEditTester();
		
		tester.type("int &Array::operator [](int subindex)\n"); //$NON-NLS-1$
		assertEquals(1, tester.getCaretLine());
		tester.type('{');
		// Brace is not indented
		assertEquals(1, tester.getCaretColumn());
		tester.type('\n');
		// The brace was closed automatically.
		assertEquals("}", tester.getLine(1)); //$NON-NLS-1$
	}
	
	public void testSkipToStatementStartWhitesmiths_Bug311018() throws Exception {
		DefaultCodeFormatterOptions whitesmiths= DefaultCodeFormatterOptions.getWhitesmithsSettings();
		CCorePlugin.setOptions(new HashMap<String, String>(whitesmiths.getMap()));
		AutoEditTester tester = createAutoEditTester();
		tester.type("if (i > 0)\n"); //$NON-NLS-1$
		tester.type("{\n"); //$NON-NLS-1$
		// start is indented to the brace
		assertEquals("if (i > 0)\n    {\n    \n    }", tester.fDoc.get());
	}

	private void assertNoError() {
		if (!fStatusLog.isEmpty()) {
			fail(fStatusLog.get(0).toString());
		}
	}
}

