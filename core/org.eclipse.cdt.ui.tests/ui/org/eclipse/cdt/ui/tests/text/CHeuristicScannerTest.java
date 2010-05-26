/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.util.HashMap;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.FastPartitioner;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.ui.text.CHeuristicScanner;
import org.eclipse.cdt.internal.ui.text.CIndenter;
import org.eclipse.cdt.internal.ui.text.FastCPartitionScanner;

/**
 * CHeuristicScannerTest.
 * Derived from JDT.
 */
public class CHeuristicScannerTest extends TestCase {
	private static boolean BUG_65463_IS_FIXED = false;
	private FastPartitioner fPartitioner;
	private Document fDocument;
	private CIndenter fScanner;
	private CHeuristicScanner fHeuristicScanner;

	public static Test suite() {
		return new TestSuite(CHeuristicScannerTest.class);
	}

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() {
		if (CCorePlugin.getDefault() != null) {
			HashMap<String, String> options= CCorePlugin.getDefaultOptions();
			options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, CCorePlugin.TAB);
			options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");

			final String indentOnColumn= DefaultCodeFormatterConstants.createAlignmentValue(false, DefaultCodeFormatterConstants.WRAP_NO_SPLIT, DefaultCodeFormatterConstants.INDENT_ON_COLUMN);
			options.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION, indentOnColumn);
			options.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_EXPRESSIONS_IN_INITIALIZER_LIST, indentOnColumn);
			options.put(DefaultCodeFormatterConstants.FORMATTER_CONTINUATION_INDENTATION, "1");
			CCorePlugin.setOptions(options);
		}

		fDocument= new Document();
		String[] types= new String[] {
			ICPartitions.C_MULTI_LINE_COMMENT,
			ICPartitions.C_SINGLE_LINE_COMMENT,
			ICPartitions.C_STRING,
			ICPartitions.C_CHARACTER,
			ICPartitions.C_PREPROCESSOR,
			IDocument.DEFAULT_CONTENT_TYPE
		};
		fPartitioner= new FastPartitioner(new FastCPartitionScanner(), types);
		fPartitioner.connect(fDocument); 
		fDocument.setDocumentPartitioner(ICPartitions.C_PARTITIONING, fPartitioner);
		
		fHeuristicScanner= new CHeuristicScanner(fDocument);
		fScanner= new CIndenter(fDocument, fHeuristicScanner);
	}
	
	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		fDocument.setDocumentPartitioner(ICPartitions.C_PARTITIONING, null);
		fPartitioner.disconnect();
		fPartitioner= null;
		fDocument= null;

		if (CCorePlugin.getDefault() != null) {
			CCorePlugin.setOptions(CCorePlugin.getDefaultOptions());
		}
}

	public void testPrevIndentationUnit1() {
		fDocument.set("\tint a;\n" +
			"\tif (true)\n" +
			"");
		
		int pos= fScanner.findReferencePosition(18);
		Assert.assertEquals(9, pos);
	}
	
	public void testPrevIndentationUnit2() {
		fDocument.set("\tint a;\n" +
			"\tif (true)\n" +
			"\t\treturn a");
		
		int pos= fScanner.findReferencePosition(28);
		Assert.assertEquals(21, pos);
	}
	
	public void testPrevIndentationUnit4() {
		fDocument.set("\tint a;\n" +
			"\tif (true)\n" +
			"\t\treturn a\n" +
			"");
		
		int pos= fScanner.findReferencePosition(29);
		Assert.assertEquals(21, pos);
	}
	
	public void testPrevIndentationUnit5() {
		fDocument.set("\tint a;\n" +
			"\tif (true)\n" +
			"\t\treturn a;\n" +
			"");
		
		int pos= fScanner.findReferencePosition(30);
		Assert.assertEquals(9, pos);
	}
	
	public void testPrevIndentationUnit6() {
		// method definition
		fDocument.set("\tvoid proc (int par1, int par2\n");
		
		int pos= fScanner.findReferencePosition(30);
		Assert.assertEquals(12, pos);
	}
	
	public void testPrevIndentationUnit7() {
		// for with semis 
		fDocument.set("\tvoid proc (int par1, int par2) {\n" +
			"\t\t\n" +
			"\t\tfor (int i= 4; i < 33; i++) \n" +
			"");
		
		int pos= fScanner.findReferencePosition(fDocument.getLength());
		Assert.assertEquals(39, pos);
	}
	
	public void testPrevIndentationUnit8() {
		// TODO this is mean - comment at indentation spot
		fDocument.set("\t/* comment */ void proc (int par1, int par2) {\n");

		int pos= fScanner.findReferencePosition(fDocument.getLength());
//		Assert.assertEquals(1, pos);
		Assert.assertEquals(15, pos);
	}
	
	public void testPrevIndentationUnit9() {
		// block
		fDocument.set("\tvoid proc (int par1, int par2) {\n" +
			"\t\t\n" +
			"\t\tfor (int i= 4; i < 33; i++) {\n" +
			"\t\t}\n" +
			"\t\t\n" +
			"\t\tint i;\n");
		
		int pos= fScanner.findReferencePosition(fDocument.getLength());
		Assert.assertEquals(fDocument.getLength() - 7, pos);
	}

	public void testPrevIndentationUnit10() {
		// if else 
		fDocument.set("\tvoid proc (int par1, int par2) {\n" +
			"\t\t\n" +
			"\t\tif (condition()) {\n" +
			"\t\t\tcode();\n" +
			"\t\t} else {\n" +
			"\t\t\totherCode();\n" +
			"\t\t}\n" +
			"");
		
		int pos= fScanner.findReferencePosition(fDocument.getLength());
		Assert.assertEquals(39, pos);
	}

	public void testPrevIndentationUnit11() {
		// inside else block
		fDocument.set("\tvoid proc (int par1, int par2) {\n" +
			"\t\t\n" +
			"\t\tif (condition()) {\n" +
			"\t\t\tcode();\n" +
			"\t\t} else {\n" +
			"\t\t\totherCode();\n" +
			"\t\t" +
			"");
		
		int pos= fScanner.findReferencePosition(fDocument.getLength());
		Assert.assertEquals(83, pos);
	}

	public void testPrevIndentation1() {
		fDocument.set("\tint a;\n" +
			"\tif (true)\n" +
			"");
		
		String indent= fScanner.getReferenceIndentation(18).toString();
		Assert.assertEquals("\t", indent);
	}
	
	public void testPrevIndentation2() {
		fDocument.set("\tint a;\n" +
			"\tif (true)\n" +
			"\t\treturn a");
		
		String indent= fScanner.getReferenceIndentation(28).toString();
		Assert.assertEquals("\t\t", indent);
	}
	
	public void testPrevIndentation3() {
		fDocument.set("\tint a;\n" +
			"\tif (true)\n" +
			"\t\treturn a;");
		
		String indent= fScanner.getReferenceIndentation(29).toString();
		Assert.assertEquals("\t\t", indent);
	}
	
	public void testPrevIndentation4() {
		fDocument.set("\tint a;\n" +
			"\tif (true)\n" +
			"\t\treturn a\n" +
			"");
		
		String indent= fScanner.getReferenceIndentation(29).toString();
		Assert.assertEquals("\t\t", indent);
	}
	
	public void testPrevIndentation5() {
		fDocument.set("\tint a;\n" +
			"\tif (true)\n" +
			"\t\treturn a;\n" +
			"");
		
		String indent= fScanner.getReferenceIndentation(30).toString();
		Assert.assertEquals("\t", indent);
	}
	
	public void testPrevIndentation6() {
		fDocument.set("\tvoid proc (int par1, int par2\n");
		
		String indent= fScanner.getReferenceIndentation(30).toString();
		Assert.assertEquals("\t", indent);
	}
	
	public void testPrevIndentation7() {
		// for with semis 
		fDocument.set("\tvoid proc (int par1, int par2) {\n" +
			"\t\t\n" +
			"\t\tfor (int i= 4; i < 33; i++) \n" +
			"");
		
		String indent= fScanner.getReferenceIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t", indent);
	}
	
	public void testPrevIndentation8() {
		fDocument.set("\t/* comment */ void proc (int par1, int par2) {\n");
		
		String indent= fScanner.getReferenceIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t", indent);
	}
	
	public void testPrevIndentation9() {
		// block
		fDocument.set("\tvoid proc (int par1, int par2) {\n" +
			"\t\t\n" +
			"\t\tfor (int i= 4; i < 33; i++) {\n" +
			"\t\t}\n" +
			"\t\t\n" +
			"\t\tint i;\n");
		
		String indent= fScanner.getReferenceIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t", indent);
	}

	public void testPrevIndentation10() {
		// else
		fDocument.set("\tvoid proc (int par1, int par2) {\n" +
			"\t\t\n" +
			"\t\tif (condition()) {\n" +
			"\t\t\tcode();\n" +
			"\t\t} else {\n" +
			"\t\t\totherCode();\n" +
			"\t\t}\n" +
			"");
		
		String indent= fScanner.getReferenceIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t", indent);
	}

	public void testPrevIndentation11() {
		// else
		fDocument.set("\tvoid proc (int par1, int par2) {\n" +
			"\t\t\n" +
			"\t\tif (condition()) {\n" +
			"\t\t\tcode();\n" +
			"\t\t} else {\n" +
			"\t\t\totherCode();\n" +
			"\t\t" +
			"");
		
		String indent= fScanner.getReferenceIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t\t", indent);
	}

	public void testIndentation1() {
		fDocument.set("\tint a;\n" +
			"\tif (true)\n" +
			"");
		
		String indent= fScanner.computeIndentation(18).toString();
		Assert.assertEquals("\t\t", indent);
	}
	
	public void testIndentation2() {
		fDocument.set("\tint a;\n" +
			"\tif (true)\n" +
			"\t\treturn a");
		
		String indent= fScanner.computeIndentation(28).toString();
		Assert.assertEquals("\t\t\t", indent);
	}
	
	public void testIndentation3() {
		fDocument.set("\tint a;\n" +
			"\tif (true)\n" +
			"\t\treturn a;");
		
		String indent= fScanner.computeIndentation(29).toString();
		Assert.assertEquals("\t\t\t", indent);
	}
	
	public void testIndentation4() {
		fDocument.set("\tint a;\n" +
			"\tif (true)\n" +
			"\t\treturn a\n" +
			"");
		
		String indent= fScanner.computeIndentation(29).toString();
		Assert.assertEquals("\t\t\t", indent);
	}
	
	public void testIndentation5() {
		fDocument.set("\tint a;\n" +
			"\tif (true)\n" +
			"\t\treturn a;\n" +
			"");
		
		String indent= fScanner.computeIndentation(30).toString();
		Assert.assertEquals("\t", indent);
	}
	
	public void testIndentation6() {
		// parameter declaration - alignment with parenthesis 
		fDocument.set("\tvoid proc (int par1, int par2\n");
		
		String indent= fScanner.computeIndentation(30).toString();
		Assert.assertEquals("\t           ", indent);
	}
	
	public void testIndentation6a() {
		// parameter declaration - alignment with parenthesis 
		fDocument.set("\tvoid proc (  int par1, int par2\n");
		
		String indent= fScanner.computeIndentation(30).toString();
		Assert.assertEquals("\t             ", indent);
	}
	
	public void testIndentation7() {
		// for with semis 
		fDocument.set("\tvoid proc (int par1, int par2) {\n" +
			"\t\t\n" +
			"\t\tfor (int i= 4; i < 33; i++) \n" +
			"");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t\t", indent);
	}
	
	public void testIndentation8() {
		// method definition
		fDocument.set("\t/* package */ void proc (int par1, int par2) {\n");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t", indent);
	}
	
	public void testIndentation9() {
		// block
		fDocument.set("\tvoid proc (int par1, int par2) {\n" +
			"\t\t\n" +
			"\t\tfor (int i= 4; i < 33; i++) {\n" +
			"\t\t}\n" +
			"\t\t\n" +
			"\t\tint i;\n");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t", indent);
	}

	public void testIndentation10() {
		// else
		fDocument.set("\tvoid proc (int par1, int par2) {\n" +
			"\t\t\n" +
			"\t\tif (condition()) {\n" +
			"\t\t\tcode();\n" +
			"\t\t} else {\n" +
			"\t\t\totherCode();\n" +
			"\t\t}\n" +
			"");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t", indent);
	}

	public void testIndentation11() {
		// else
		fDocument.set("\tvoid proc (int par1, int par2) {\n" +
			"\t\t\n" +
			"\t\tif (condition()) {\n" +
			"\t\t\tcode();\n" +
			"\t\t} else {\n" +
			"\t\t\totherCode();\n" +
			"\t\t" +
			"");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t\t", indent);
	}

	public void testIndentation12() {
		// multi-line condition
		fDocument.set("\tvoid proc (int par1, int par2) {\n" +
			"\t\t\n" +
			"\t\tif (condition1()\n" +
			"");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t\t", indent);
	}

	public void testIndentation13() {
		// multi-line call
		fDocument.set("\tvoid proc (int par1, int par2) {\n" +
			"\t\t\n" +
			"\t\tthis->doStuff(param1, param2,\n" +
			"");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t\t", indent);
	}

	public void testIndentation14() {
		// multi-line array initializer
		fDocument.set("\tvoid proc (int par1, int par2) {\n" +
			"\t\t\n" +
			"\t\tString[] arr= new String[] { a1, a2,\n" +
			"");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("		                             ", indent);
	}

	public void testIndentation15() {
		// for
		fDocument.set("\tfor (int i= 0; i < 10; i++) {\n" +
			"\t\tbar(); bar(); // foo\n" +
			"\t}\n");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t", indent);
	}

	public void testIndentation16() {
		// if
		fDocument.set("\tif (true)\n" +
			"\t\t;");
		
		String indent= fScanner.computeIndentation(fDocument.getLength() - 1).toString();
		Assert.assertEquals("\t\t", indent);
	}

	public void testIndentation17() {
		// if
		fDocument.set("\tif (true)\n" +
			";");
		
		String indent= fScanner.computeIndentation(fDocument.getLength() - 1).toString();
		Assert.assertEquals("\t\t", indent);
	}

	public void testIndentation18() {
		// if
		fDocument.set("\tif (true)\n" +
			"");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t", indent);
	}

	public void testIndentation19() {
		// if w/ brace right after }
		fDocument.set("\tif (true) {\n" +
			"\t\t}");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t", indent);
	}

	public void testIndentation20() {
		// if w/ brace right before }
		fDocument.set("\tif (true) {\n" +
			"\t\t}");
		
		String indent= fScanner.computeIndentation(fDocument.getLength() - 1).toString();
		Assert.assertEquals("\t", indent);
	}

	public void testIndentation21() {
		// double if w/ brace
		fDocument.set("\tif (true)\n" +
			"\t\tif (true) {\n" +
			"");	
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t\t", indent);
	}

	public void testIndentation22() {
		// after double if w/ brace 
		fDocument.set("\tif (true)\n" +
			"\t\tif (true) {\n" +
			"\t\t\tstuff();" +
			"\t\t}\n" +
			"");	
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t", indent); // because of possible dangling else
	}

	public void testIndentation22a() {
		// after double if w/ brace 
		fDocument.set("\tif (true)\n" +
			"\t\tif (true) {\n" +
			"\t\t\tstuff();\n" +
			"\t\t}\n" +
			"");
		
		String indent= fScanner.computeIndentation(fDocument.getLength() - 2).toString();
		Assert.assertEquals("\t\t", indent);
	}

	public void testIndentation22b() {
		// after double if w/ brace 
		fDocument.set("\tif (true)\n" +
			"\t\tif (true) {\n" +
			"\t\t\tstuff();" +
			"\t\t}\n" +
			"a");	
		
		String indent= fScanner.computeIndentation(fDocument.getLength() - 1).toString();
		Assert.assertEquals("\t", indent); // no dangling else possible
	}

	public void testIndentation23() {
		// do
		fDocument.set("\tdo\n" +
			"");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t", indent);
	}

	public void testIndentation24() {
		// braceless else 
		fDocument.set("\tif (true) {\n" +
			"\t\tstuff();\n" +
			"\t} else\n" +
			"\t\tnoStuff");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t", indent);
	}

	public void testIndentation25() {
		// braceless else 
		fDocument.set("\tif (true) {\r\n" +
			"\t\tstuff();\r\n" +
			"\t} else\r\n" +
			"\t\tnoStuff;\r\n");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t", indent);
	}

	public void testIndentation26() {
		// do while
		fDocument.set("\tdo\n" +
			"\t\t\n" +
			"\twhile (true);" +
			"");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t", indent);
	}

	public void testIndentation27() {
		// do while
		fDocument.set("\tdo\n" +
			"\t\t;\n" +
			"\twhile (true);" +
			"");
		
		int i= fScanner.findReferencePosition(8);
		Assert.assertEquals(1, i);
		String indent= fScanner.computeIndentation(8).toString();
		Assert.assertEquals("\t", indent);
	}

	public void testIndentation28() {
		// TODO do while - how to we distinguish from while {} loop?
		fDocument.set("\tdo\n" +
			"\t\t;\n" +
			"\twhile (true);" +
			"");
		
		int i= fScanner.findReferencePosition(fDocument.getLength());
		Assert.assertEquals(1, i);
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t", indent);
	}

	public void testIndentation29() {
		fDocument.set("\t\twhile (condition)\n" +
				"\t\t\twhile (condition)\n" + 
				"\t\t\t\tfoo();\n");
		
		int i= fScanner.findReferencePosition(fDocument.getLength());
		Assert.assertEquals(2, i);
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t", indent);
	}

	public void testIndentation30() {
		// braceless else 
		fDocument.set("\tif (true)\n" +
			"\t{");
		
		String indent= fScanner.computeIndentation(fDocument.getLength() - 1).toString();
		Assert.assertEquals("\t", indent);
	}

	public void testIndentation31() {
		// braceless else 
		fDocument.set("\tif (true)\n" +
			"{\t\n" +
			"\t\tstuff();\n" +
			"\t} else\n" +
			"\t\tnoStuff");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t", indent);
	}

	public void testIndentation32() {
		// braceless else 
		fDocument.set("\tswitch(ch) {\n" +
			"\t\tcase one:\n");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("			", indent);
	}

	public void testListAlignmentMethodDeclaration() {
		// parameter declaration - alignment with parenthesis 
		fDocument.set(	"\tvoid proc (  int par1, int par2,\n" +
				"	   int par3, int par4,\n");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("	   ", indent);
	}
	
	public void testListAlignmentMethodCall() {
		// parameter declaration - alignment with parenthesis 
		fDocument.set(	"\this->proc (par1, par2,\n" +
				"	   par3, par4,\n");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("	   ", indent);
	}
	
	public void testListAlignmentArray() {
		// parameter declaration - alignment with parenthesis 
		fDocument.set(	"\tint[]= new int[] { 1, two,\n" +
				"	   three, four,\n");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("	   ", indent);
	}
	
	public void testListAlignmentArray2() {
		// no prior art - probe system settings. 
		fDocument.set(	"\tint[]= new int[] { 1, two,\n");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t                   ", indent);
		
	}
	
	public void testBraceAlignmentOfMultilineDeclaration() {
		fDocument.set(	"	int foobar(int one, int two,\n" + 
						"						 int three, int four,\n" + 
						"						 int five) {\n" + 
						"		\n" + 
						"		return 0;\n" + 
						"	}");
		
		String indent= fScanner.computeIndentation(fDocument.getLength() - 1).toString();
		Assert.assertEquals("	", indent);
	}
	
	public void testBlocksInCaseStatements() {
		fDocument.set(
				"		switch (i) {\n" + 
				"			case 1:\n" + 
				"				do {\n" + 
				"");
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("					", indent);
	}
	
	public void testClassInstanceCreationHeuristic() throws Exception {
		fDocument.set("   method(new std::vector<std::string>(10), foo, new int[])");
	    
	    for (int offset= 0; offset < 15; offset++)
	    	assertFalse(fHeuristicScanner.looksLikeClassInstanceCreationBackward(offset, CHeuristicScanner.UNBOUND));
	    for (int offset= 15; offset < 18; offset++)
	    	assertTrue(fHeuristicScanner.looksLikeClassInstanceCreationBackward(offset, CHeuristicScanner.UNBOUND));
	    for (int offset= 18; offset < 20; offset++)
	    	assertFalse(fHeuristicScanner.looksLikeClassInstanceCreationBackward(offset, CHeuristicScanner.UNBOUND));
	    for (int offset= 20; offset < 26; offset++)
	    	assertTrue(fHeuristicScanner.looksLikeClassInstanceCreationBackward(offset, CHeuristicScanner.UNBOUND));
	    for (int offset= 26; offset < 48; offset++)
	    	assertFalse(fHeuristicScanner.looksLikeClassInstanceCreationBackward(offset, CHeuristicScanner.UNBOUND));
	    for (int offset= 48; offset < 54; offset++)
	    	assertFalse(fHeuristicScanner.looksLikeClassInstanceCreationBackward(offset, CHeuristicScanner.UNBOUND));
	    for (int offset= 54; offset < 57; offset++)
	    	assertTrue(fHeuristicScanner.looksLikeClassInstanceCreationBackward(offset, CHeuristicScanner.UNBOUND));
	    for (int offset= 57; offset < 59; offset++)
	    	assertFalse(fHeuristicScanner.looksLikeClassInstanceCreationBackward(offset, CHeuristicScanner.UNBOUND));
    }

	public void testFieldReferenceHeuristic() throws Exception {
		fDocument.set("t.f=tp->f-T::f;");
	    for (int offset= 0; offset < 2; offset++)
	    	assertFalse(fHeuristicScanner.looksLikeFieldReferenceBackward(offset, CHeuristicScanner.UNBOUND));
	    for (int offset= 2; offset < 4; offset++)
	    	assertTrue(fHeuristicScanner.looksLikeFieldReferenceBackward(offset, CHeuristicScanner.UNBOUND));
	    for (int offset= 4; offset < 8; offset++)
	    	assertFalse(fHeuristicScanner.looksLikeFieldReferenceBackward(offset, CHeuristicScanner.UNBOUND));
	    for (int offset= 8; offset < 10; offset++)
	    	assertTrue(fHeuristicScanner.looksLikeFieldReferenceBackward(offset, CHeuristicScanner.UNBOUND));
	    for (int offset= 10; offset < 13; offset++)
	    	assertFalse(fHeuristicScanner.looksLikeFieldReferenceBackward(offset, CHeuristicScanner.UNBOUND));
	    for (int offset= 13; offset < 14; offset++)
	    	assertTrue(fHeuristicScanner.looksLikeFieldReferenceBackward(offset, CHeuristicScanner.UNBOUND));
	    for (int offset= 15; offset < 15; offset++)
	    	assertFalse(fHeuristicScanner.looksLikeFieldReferenceBackward(offset, CHeuristicScanner.UNBOUND));
	}
	
	public void testCompositeTypeDefinitionHeuristic() throws Exception {
		int offset;
		fDocument.set("class A {");
		offset= fDocument.get().indexOf("{");
    	assertTrue(fHeuristicScanner.looksLikeCompositeTypeDefinitionBackward(offset, CHeuristicScanner.UNBOUND));
		fDocument.set("class A : B {");
		offset= fDocument.get().indexOf("{");
    	assertTrue(fHeuristicScanner.looksLikeCompositeTypeDefinitionBackward(offset, CHeuristicScanner.UNBOUND));
		fDocument.set("struct A : B {");
		offset= fDocument.get().indexOf("{");
    	assertTrue(fHeuristicScanner.looksLikeCompositeTypeDefinitionBackward(offset, CHeuristicScanner.UNBOUND));
		fDocument.set("class A : virtual public B {");
		offset= fDocument.get().indexOf("{");
    	assertTrue(fHeuristicScanner.looksLikeCompositeTypeDefinitionBackward(offset, CHeuristicScanner.UNBOUND));
		fDocument.set("class A : public B, protected virtual C {");
		offset= fDocument.get().indexOf("{");
    	assertTrue(fHeuristicScanner.looksLikeCompositeTypeDefinitionBackward(offset, CHeuristicScanner.UNBOUND));
		fDocument.set("template <class T> class A : public B<int,float>, protected C<T> {");
		offset= fDocument.get().indexOf("{");
    	assertTrue(fHeuristicScanner.looksLikeCompositeTypeDefinitionBackward(offset, CHeuristicScanner.UNBOUND));
	}
	
	public void testShiftOperator() throws Exception {
		fDocument.set(
				"		for (int j = 0; j == 0; j ++) {\n" + 
				"			j = 3 >> 1;\n" 
		);
		
		String indent= fScanner.computeIndentation(fDocument.getLength()).toString();
		Assert.assertEquals("\t\t\t", indent);
	}

	public void testConditional1() throws Exception {
		if (!BUG_65463_IS_FIXED) // Enable when http://bugs.eclipse.org/bugs/show_bug.cgi?id=65463 is fixed
			return;
    	fDocument.set(
    			"		boolean isPrime() {\n" +
    			"			return fPrime == true ? true\n" +
    			"			                      : false;"
    	);
    	
    	String indent= fScanner.computeIndentation(fDocument.getLength() - 8).toString();
    	Assert.assertEquals("			                      ", indent);
    }

	public void testConditional2() throws Exception {
		if (!BUG_65463_IS_FIXED) // Enable when http://bugs.eclipse.org/bugs/show_bug.cgi?id=65463 is fixed
			return;
    	fDocument.set(
    			"		boolean isPrime() {\n" +
    			"			return fPrime == true" +
    			"					? true\n" +
    			"					: false;"
    	);
    	
    	String indent= fScanner.computeIndentation(fDocument.getLength() - 8).toString();
    	Assert.assertEquals("					", indent);
    }
}
