/*******************************************************************************
 *  Copyright (c) 2004, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.scanner;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;

/**
 * Scanner2Tests ported to use the CPreprocessor
 */
public class PortedScannerTests extends PreprocessorTestsBase {
    public static TestSuite suite() {
		return suite(PortedScannerTests.class);
	}

	public PortedScannerTests() {
		super();
	}

	public PortedScannerTests(String name) {
		super(name);
	}

	public void testBug102825_1() throws Exception {
		StringBuffer buffer = new StringBuffer(
				"#define CURLOPTTYPE_OBJECTPOINT   10000\n"); 
		buffer.append("#define CINIT = CURLOPTTYPE_##OBJECTPOINT + 1\n"); 
		buffer.append("CINIT\n"); 
		initializeScanner(buffer.toString());
		validateToken(IToken.tASSIGN);
		validateInteger("10000"); 
	}

	public void testBug102825_2() throws Exception {
		StringBuffer buffer = new StringBuffer(
				"#define CURLOPTTYPE_OBJECTPOINT   10000\n"); 
		buffer
				.append("#define CINIT(name,type,number) = CURLOPTTYPE_##type + number\n"); 
		buffer.append("CINIT(FILE, OBJECTPOINT, 1)\n"); 
		initializeScanner(buffer.toString());
		validateToken(IToken.tASSIGN);
		validateInteger("10000"); 
	}

	public void testBug102825_3() throws Exception {
		StringBuffer buffer = new StringBuffer(
				"#define CURLOPTTYPE_OBJECTPOINT   10000\n"); 
		buffer
				.append("#define CINIT(name,type,number) CURLOPT_ ## name = CURLOPTTYPE_	## type + number\n"); 
		buffer.append("CINIT(FILE, OBJECTPOINT, 1)\n"); 
		initializeScanner(buffer.toString());
		validateIdentifier("CURLOPT_FILE"); 
		validateToken(IToken.tASSIGN);
		validateInteger("10000"); 
	}

	public void testBug102825_4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define glue( a, b ) a ## b\n"); 
		buffer.append("#define HIGHLOW \"hello\"\n"); 
		buffer.append("glue( HIGH, LOW )\n"); 

		initializeScanner(buffer.toString(), ParserMode.QUICK_PARSE);
		validateString("hello"); 
		validateProblemCount(0);
	}

	public void testBug195610_1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define glue(x, y, z) x ## y ## z\n"); 
		buffer.append("glue(, b, c)\n"); 

		initializeScanner(buffer.toString(), ParserMode.QUICK_PARSE);
		validateIdentifier("bc"); 
		validateProblemCount(0);
	}

	public void testBug195610_2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define glue(x, y, z) x ## y ## z\n"); 
		buffer.append("glue(a, , c)\n"); 

		initializeScanner(buffer.toString(), ParserMode.QUICK_PARSE);
		validateIdentifier("ac"); 
		validateProblemCount(0);
	}

	public void testBug195610_3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define glue(x, y, z) x ## y ## z\n"); 
		buffer.append("glue(a, b, )\n"); 

		initializeScanner(buffer.toString(), ParserMode.QUICK_PARSE);
		validateIdentifier("ab"); 
		validateProblemCount(0);
	}

	public class TableRow {
		private int[] values;
		private int length;

		public TableRow(int[] v) {
			length = v.length;
			values = new int[length];
			System.arraycopy(v, 0, values, 0, length);
		}

		@Override
		public String toString() {
			StringBuffer s = new StringBuffer();
			for (int i = 0; i < length; ++i) {
				s
						.append("var").append(i).append("=").append(values[i]).append(" ");   //$NON-NLS-3$
			}
			return s.toString();
		}

		public String symbolName(int index) {
			return "DEFINITION" + index; 
		}

		public int symbolValue(int index) {
			return new Long(Math.round(Math.pow(index, index))).intValue();
		}

		public String generateCode() {
			if (length < 2) {
				return "Array must have at least 2 elements"; 
			}
			int numberOfElsifs = length - 1;
			StringBuffer buffer = new StringBuffer();
			buffer.append("#if ").append(values[0]).append("\n#\tdefine ");  
			buffer.append(symbolName(0)).append(" ").append(symbolValue(0)); 
			for (int i = 0; i < numberOfElsifs; ++i)
				buffer.append("\n#elif ") 
						.append(values[1 + i]).append("\n#\tdefine ") 
						.append(symbolName(i + 1)).append(" ") 
						.append(symbolValue(i + 1));
			buffer.append("\n#else \n#\tdefine ") 
					.append(symbolName(length)).append(" ") 
					.append(symbolValue(length)).append("\n#endif"); 
			return buffer.toString();
		}

		public int selectWinner() {
			for (int i = 0; i < values.length; ++i) {
				if (values[i] != 0) {
					return i;
				}
			}
			return length;
		}

		/**
		 * Returns the length.
		 * @return int
		 */
		public int getLength() {
			return length;
		}

	}

	public class TruthTable {
		private int numberOfVariables;
		private int numberOfRows;
		public TableRow[] rows;

		public TruthTable(int n) {
			numberOfVariables = n;
			numberOfRows = new Long(Math.round(Math.pow(2, n))).intValue();

			rows = new TableRow[numberOfRows];
			for (int i = 0; i < numberOfRows; ++i) {
				String Z = Integer.toBinaryString(i);

				int[] input = new int[numberOfVariables];
				for (int j = 0; j < numberOfVariables; ++j) {
					int padding = numberOfVariables - Z.length();
					int k = 0;
					for (; k < padding; ++k) {
						input[k] = 0;
					}
					for (int l = 0; l < Z.length(); ++l) {
						char c = Z.charAt(l);
						int value = Character.digit(c, 10);
						input[k++] = value;
					}
				}
				rows[i] = new TableRow(input);
			}
		}

		/**
		 * Returns the numberOfRows.
		 * @return int
		 */
		public int getNumberOfRows() {
			return numberOfRows;
		}

	}

	public final static int SIZEOF_TRUTHTABLE = 10;

	public void testWeirdStrings() throws Exception {
		initializeScanner("Living Life L\"LONG\""); 
		validateIdentifier("Living"); 
		validateIdentifier("Life"); 
		validateLString("LONG"); 
		validateEOF();

	}
	
	public void testUTFStrings() throws Exception {
		IScannerExtensionConfiguration config = new GPPScannerExtensionConfiguration() {
			@Override public boolean supportUTFLiterals() { return true; }
		};
		initializeScanner("ubiquitous u\"utf16\" User U\"utf32\"", ParserLanguage.CPP, config); 
		validateIdentifier("ubiquitous"); 
		validateUTF16String("utf16"); 
		validateIdentifier("User"); 
		validateUTF32String("utf32"); 
		validateEOF();
	}
	
	public void testUTFChars() throws Exception {
		IScannerExtensionConfiguration config = new GPPScannerExtensionConfiguration() {
			@Override public boolean supportUTFLiterals() { return true; }
		};
		initializeScanner("u'asdf' U'1234'", ParserLanguage.CPP, config);
		validateUTF16Char("asdf");
		validateUTF32Char("1234");
		validateEOF();
	}

	public void testNumerics() throws Exception {
		initializeScanner("3.0 0.9 .5 3. 4E5 2.01E-03 ..."); 
		validateFloatingPointLiteral("3.0"); 
		validateFloatingPointLiteral("0.9"); 
		validateFloatingPointLiteral(".5"); 
		validateFloatingPointLiteral("3."); 
		validateFloatingPointLiteral("4E5"); 
		validateFloatingPointLiteral("2.01E-03"); 
		validateToken(IToken.tELLIPSIS);
		validateEOF();

	}

	public void testPreprocessorDefines() throws Exception {
		initializeScanner("#define SIMPLE_NUMERIC 5\nint x = SIMPLE_NUMERIC"); 
		validateToken(IToken.t_int);
		validateDefinition("SIMPLE_NUMERIC", "5");  
		validateIdentifier("x"); 
		validateToken(IToken.tASSIGN);
		validateInteger("5"); 
		validateEOF();

		initializeScanner("#define SIMPLE_STRING \"This is a simple string.\"\n\nconst char * myVariable = SIMPLE_STRING;"); 
		validateToken(IToken.t_const);
		validateDefinition("SIMPLE_STRING", "\"This is a simple string.\"");  
		validateToken(IToken.t_char);
		validateToken(IToken.tSTAR);
		validateIdentifier("myVariable"); 
		validateToken(IToken.tASSIGN);
		validateString("This is a simple string."); 
		validateToken(IToken.tSEMI);
		validateEOF();

		initializeScanner("#define FOOL 5  \n int tryAFOOL = FOOL + FOOL;"); 
		validateToken(IToken.t_int);
		validateIdentifier("tryAFOOL"); 
		validateToken(IToken.tASSIGN);
		validateInteger("5"); 
		validateToken(IToken.tPLUS);
		validateInteger("5"); 
		validateToken(IToken.tSEMI);
		validateEOF();

		initializeScanner("#define FOOL 5  \n int FOOLer = FOOL;"); 
		validateToken(IToken.t_int);
		validateIdentifier("FOOLer"); 
		validateToken(IToken.tASSIGN);
		validateInteger("5"); 
		validateToken(IToken.tSEMI);
		validateEOF();

		// the case we were failing against in ctype.h
		// this is a definition, not a macro!
		initializeScanner("#define _ALPHA (0x0100|_UPPER|_LOWER)"); 
		validateEOF();
		validateDefinition("_ALPHA", "(0x0100|_UPPER|_LOWER)");  

		// test for comments after the macro
		initializeScanner("#define NO_COMMENT// ignore me"); 
		validateEOF();
		validateDefinition("NO_COMMENT", "");  

		initializeScanner("#define NO_COMMENT/* ignore me*/"); 
		validateEOF();
		validateDefinition("NO_COMMENT", "");  

		initializeScanner("#define ANSWER 42 // i think"); 
		validateEOF();
		validateDefinition("ANSWER", "42");  

		initializeScanner("#define ANSWER 42 /* i think */"); 
		validateEOF();
		validateDefinition("ANSWER", "42");  

		initializeScanner("#define MULTILINE 3 /* comment \n that goes more than one line */"); 
		validateEOF();
		validateDefinition("MULTILINE", "3");  

		initializeScanner("#define MULTICOMMENT X /* comment1 */ + Y /* comment 2 */"); 
		validateEOF();
		validateDefinition("MULTICOMMENT", "X + Y");  

		initializeScanner("#define SIMPLE_STRING This is a simple string.\n"); 
		validateEOF();
		validateDefinition("SIMPLE_STRING", 
				"This is a simple string."); 

		initializeScanner("#	define SIMPLE_NUMERIC 5\n"); 
		validateEOF();
		validateDefinition("SIMPLE_NUMERIC", "5");  

		initializeScanner("#	define		SIMPLE_NUMERIC   	5\n"); 
		validateEOF();
		validateDefinition("SIMPLE_NUMERIC", "5");  

		initializeScanner("#define 		SIMPLE_STRING \"This 	is a simple     string.\"\n"); 
		validateEOF();
		validateDefinition("SIMPLE_STRING", 
				"\"This 	is a simple     string.\""); 

		initializeScanner("#define SIMPLE_STRING 	  	This 	is a simple 	string.\n"); 
		validateEOF();
		validateDefinition("SIMPLE_STRING", "This is a simple string."); 

		initializeScanner("#define FLAKE\n\nFLAKE"); 
		validateEOF();
		validateDefinition("FLAKE", "");  

		initializeScanner("#define SIMPLE_STRING 	  	This 	is a simple 	string.\\\n		Continue please."); 
		validateEOF();
		validateDefinition("SIMPLE_STRING", "This is a simple string. Continue please."); 
	}

	public void testBug67834() throws Exception {
		initializeScanner("#if ! BAR\n" + 
				"foo\n" + 
				"#else\n" + 
				"bar\n" + 
				"#endif\n" 
		);
		validateIdentifier("foo"); 
		validateEOF();
		initializeScanner("#if ! (BAR)\n" + 
				"foo\n" + 
				"#else\n" + 
				"bar\n" + 
				"#endif\n" 
		);
		validateIdentifier("foo"); 
		validateEOF();
	}

	public void testConcatenation() throws Exception {
		initializeScanner("#define F1 3\n#define F2 F1##F1\nint x=F2;"); 
		validateToken(IToken.t_int);
		validateDefinition("F1", "3");  
		validateDefinition("F2", "F1##F1");  
		validateIdentifier("x"); 
		validateToken(IToken.tASSIGN);
		validateIdentifier("F1F1"); 
		validateToken(IToken.tSEMI);
		validateEOF();

		initializeScanner("#define PREFIX RT_\n#define RUN PREFIX##Run"); 
		validateEOF();
		validateDefinition("PREFIX", "RT_");  
		validateDefinition("RUN", "PREFIX##Run");  

		initializeScanner("#define DECLARE_HANDLE(name) struct name##__ { int unused; }; typedef struct name##__ *name\n DECLARE_HANDLE( joe )"); 
		validateToken(IToken.t_struct);
		validateIdentifier("joe__"); 
		validateToken(IToken.tLBRACE);
		validateToken(IToken.t_int);
		validateIdentifier("unused"); 
		validateToken(IToken.tSEMI);
		validateToken(IToken.tRBRACE);
		validateToken(IToken.tSEMI);
		validateToken(IToken.t_typedef);
		validateToken(IToken.t_struct);
		validateIdentifier("joe__"); 
		validateToken(IToken.tSTAR);
		validateIdentifier("joe"); 
		validateEOF();
	}

	public void testSimpleIfdef() throws Exception {
		initializeScanner("#define SYMBOL 5\n#ifdef SYMBOL\nint counter(SYMBOL);\n#endif"); 
		validateToken(IToken.t_int);
		validateIdentifier("counter"); 
		validateToken(IToken.tLPAREN);
		validateInteger("5"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();

		initializeScanner("#define SYMBOL 5\n#ifndef SYMBOL\nint counter(SYMBOL);\n#endif"); 
		validateEOF();

		initializeScanner("#ifndef DEFINED\n#define DEFINED 100\n#endif\nint count = DEFINED;"); 
		validateToken(IToken.t_int);
		validateDefinition("DEFINED", "100");  

		validateIdentifier("count"); 
		validateToken(IToken.tASSIGN);
		validateInteger("100"); 
		validateToken(IToken.tSEMI);
		validateEOF();

		initializeScanner("#ifndef DEFINED\n#define DEFINED 100\n#endif\nint count = DEFINED;"); 
		addDefinition("DEFINED", "101");  
		validateDefinition("DEFINED", "101");  
		validateToken(IToken.t_int);
		validateIdentifier("count"); 
		validateToken(IToken.tASSIGN);
		validateInteger("101"); 
		validateToken(IToken.tSEMI);
		validateEOF();

		initializeScanner("/* NB: This is #if 0'd out */"); 
		validateEOF();
	}

	/**
	 * @param string
	 * @param string2
	 */
	private void addDefinition(String string, String string2) {
		fScanner.addMacroDefinition(string.toCharArray(), string2.toCharArray());
	}

	public void testMultipleLines() throws Exception {
		Writer code = new StringWriter();
		code.write("#define COMPLEX_MACRO 33 \\\n"); 
		code.write("	+ 44\n\nCOMPLEX_MACRO"); 
		initializeScanner(code.toString());
		validateInteger("33"); 
		validateToken(IToken.tPLUS);
		validateInteger("44"); 
	}

	public void testSlightlyComplexIfdefStructure() throws Exception {
		initializeScanner("#ifndef BASE\n#define BASE 10\n#endif\n#ifndef BASE\n#error BASE is defined\n#endif"); 
		validateEOF();

		initializeScanner("#ifndef ONE\n#define ONE 1\n#ifdef TWO\n#define THREE ONE + TWO\n#endif\n#endif\nint three(THREE);"); 

		validateToken(IToken.t_int);
		validateDefinition("ONE", "1");  
		validateAsUndefined("TWO"); 
		validateAsUndefined("THREE"); 
		validateIdentifier("three"); 
		validateToken(IToken.tLPAREN);
		validateIdentifier("THREE"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
		initializeScanner(
				"#ifndef ONE\n" +
				   "#define ONE 1\n" +
				   "#ifdef TWO\n" +
				      "#define THREE ONE + TWO\n" +
				   "#endif\n"+
				"#endif\n"+
				"int three(THREE);"); 
		addDefinition("TWO", "2");  
		validateToken(IToken.t_int);
		validateDefinition("ONE", "1");  
		validateDefinition("TWO", "2");  
		validateDefinition("THREE", "ONE + TWO");  

		validateIdentifier("three"); 
		validateToken(IToken.tLPAREN);
		validateInteger("1"); 
		validateToken(IToken.tPLUS);
		validateInteger("2"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
		initializeScanner("#ifndef FOO\n#define FOO 4\n#else\n#undef FOO\n#define FOO 6\n#endif"); 
		validateEOF();
		validateDefinition("FOO", "4");  

		initializeScanner("#ifndef FOO\n#define FOO 4\n#else\n#undef FOO\n#define FOO 6\n#endif"); 
		addDefinition("FOO", "2");  
		validateEOF();
		validateDefinition("FOO", "6");  

		initializeScanner("#ifndef ONE\n#   define ONE 1\n#   ifndef TWO\n#       define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#else\n#   ifndef TWO\n#      define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#endif\n"); 
		validateEOF();
		validateDefinition("ONE", "1");  
		validateDefinition("TWO", "ONE + ONE");  

		initializeScanner("#ifndef ONE\r\n" + 
				"#   define ONE 1\n" + 
				"#   ifndef TWO\n" + 
				"#       define TWO ONE + ONE \n" + 
				"#   else\n" + 
				"#       undef TWO\n" + 
				"#       define TWO 2 \n" + 
				"#   endif\n" + 
				"#else\n" + 
				"#   ifndef TWO\n" + 
				"#      define TWO ONE + ONE \n" + 
				"#   else\n" + 
				"#       undef TWO\n" + 
				"#       define TWO 2 \n" + 
				"#   endif\n" + 
		"#endif\n"); 

		addDefinition("ONE", "one");  
		validateEOF();
		validateDefinition("ONE", "one");  
		validateDefinition("TWO", "ONE + ONE");  

		initializeScanner("#ifndef ONE\n#   define ONE 1\n#   ifndef TWO\n#       define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#else\n#   ifndef TWO\n#      define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#endif\n"); 
		addDefinition("ONE", "one");  
		addDefinition("TWO", "two");  
		validateEOF();
		validateDefinition("ONE", "one");  
		validateDefinition("TWO", "2");  

		initializeScanner("#ifndef ONE\n#   define ONE 1\n#   ifndef TWO\n#       define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#else\n#   ifndef TWO\n#      define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#endif\n"); 
		addDefinition("TWO", "two");  
		validateEOF();
		validateDefinition("ONE", "1");  
		validateDefinition("TWO", "2");  
	}

	public void testIfs() throws Exception {
		initializeScanner("#if 0\n#error NEVER\n#endif\n"); 
		validateEOF();
		initializeScanner("#define X 5\n#define Y 7\n#if (X < Y)\n#define Z X + Y\n#endif"); 
		validateEOF();
		validateDefinition("X", "5");  
		validateDefinition("Y", "7");  
		validateDefinition("Z", "X + Y");  

		initializeScanner("#if T < 20\n#define Z T + 1\n#endif"); 
		addDefinition("X", "5");  
		addDefinition("Y", "7");  
		addDefinition("T", "X + Y");  
		validateEOF();
		validateDefinition("X", "5");  
		validateDefinition("Y", "7");  
		validateDefinition("T", "X + Y");  
		validateDefinition("Z", "T + 1");  

		initializeScanner("#if ( 10 / 5 ) != 2\n#error 10/5 seems to not equal 2 anymore\n#endif\n"); 
		validateEOF();

		initializeScanner("#ifndef FIVE \n" + 
				"#define FIVE 5\n" + 
				"#endif \n" + 
				"#ifndef TEN\n" + 
				"#define TEN 2 * FIVE\n" + 
				"#endif\n" + 
				"#if TEN != 10\n" + 
				"#define MISTAKE 1\n" + 
				"#error Five does not equal 10\n" + 
				"#endif\n", ParserMode.QUICK_PARSE); 
		addDefinition("FIVE", "55");  
		validateEOF();
		validateDefinition("FIVE", "55");  
		validateDefinition("TEN", "2 * FIVE");  
		validateDefinition("MISTAKE", "1");  
		validateProblemCount(1);

		initializeScanner("#if ((( FOUR / TWO ) * THREE )< FIVE )\n#error 6 is not less than 5 \n#endif\n#if ( ( FIVE * ONE ) != (( (FOUR) + ONE ) * ONE ) )\n#error 5 should equal 5\n#endif \n"); 

		addDefinition("ONE", "1");  
		addDefinition("TWO", "(ONE + ONE)");  
		addDefinition("THREE", "(TWO + ONE)");  
		addDefinition("FOUR", "(TWO * TWO)");  
		addDefinition("FIVE", "(THREE + TWO)");  

		validateEOF();
		validateDefinition("ONE", "1");  
		validateDefinition("TWO", "(ONE + ONE)");  
		validateDefinition("THREE", "(TWO + ONE)");  
		validateDefinition("FOUR", "(TWO * TWO)");  
		validateDefinition("FIVE", "(THREE + TWO)");  

		TruthTable table = new TruthTable(SIZEOF_TRUTHTABLE);
		int numberOfRows = table.getNumberOfRows();
		TableRow[] rows = table.rows;

		for (int i = 0; i < numberOfRows; ++i) {
			TableRow row = rows[i];
			String code = row.generateCode();
			initializeScanner(code);
			validateEOF();
			validateAllDefinitions(row);
		}

		initializeScanner("#if ! 0\n#error Correct!\n#endif"); 
		validateEOF();
	}

	public void testPreprocessorMacros() throws Exception {
		initializeScanner("#define GO(x) x+1\nint y(5);\ny = GO(y);");
		validateToken(IToken.t_int);
		validateIdentifier("y");
		validateToken(IToken.tLPAREN);
		validateInteger("5");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);

		validateIdentifier("y");
		validateToken(IToken.tASSIGN);
		validateIdentifier("y");
		validateToken(IToken.tPLUS);
		validateInteger("1");
		validateToken(IToken.tSEMI);
		validateEOF();
		initializeScanner("#define ONE 1\n"
				+ "#define SUM(a,b,c,d,e,f,g) ( a + b + c + d + e + f + g )\n"
				+ "int daSum = SUM(ONE,3,5,7,9,11,13);");
		validateToken(IToken.t_int);
		validateIdentifier("daSum");
		validateToken(IToken.tASSIGN);
		validateToken(IToken.tLPAREN);
		validateInteger("1");
		validateToken(IToken.tPLUS);
		validateInteger("3");
		validateToken(IToken.tPLUS);
		validateInteger("5");
		validateToken(IToken.tPLUS);
		validateInteger("7");
		validateToken(IToken.tPLUS);
		validateInteger("9");
		validateToken(IToken.tPLUS);
		validateInteger("11");
		validateToken(IToken.tPLUS);
		validateInteger("13");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();

		initializeScanner("#define LOG( format, var1)   printf( format, var1 )\nLOG( \"My name is %s\", \"Bogdan\" );\n");
		validateIdentifier("printf");
		validateToken(IToken.tLPAREN);
		validateString("My name is %s");
		validateToken(IToken.tCOMMA);
		validateString("Bogdan");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();

		initializeScanner("#define INCR( x )   ++x\nint y(2);\nINCR(y);");
		validateToken(IToken.t_int);
		validateIdentifier("y");
		validateToken(IToken.tLPAREN);
		validateInteger("2");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateToken(IToken.tINCR);
		validateIdentifier("y");
		validateToken(IToken.tSEMI);
		validateEOF();

		initializeScanner("#define CHECK_AND_SET( x, y, z )     if( x ) { \\\n y = z; \\\n }\n\nCHECK_AND_SET( 1, balance, 5000 );\nCHECK_AND_SET( confused(), you, dumb );");
		validateToken(IToken.t_if);
		validateToken(IToken.tLPAREN);
		validateInteger("1");
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tLBRACE);
		validateIdentifier("balance");
		validateToken(IToken.tASSIGN);
		validateInteger("5000");
		validateToken(IToken.tSEMI);
		validateToken(IToken.tRBRACE);
		validateToken(IToken.tSEMI);

		validateToken(IToken.t_if);
		validateToken(IToken.tLPAREN);
		validateIdentifier("confused");
		validateToken(IToken.tLPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tLBRACE);
		validateIdentifier("you");
		validateToken(IToken.tASSIGN);
		validateIdentifier("dumb");
		validateToken(IToken.tSEMI);
		validateToken(IToken.tRBRACE);
		validateToken(IToken.tSEMI);
		validateEOF();

		initializeScanner(
				"#define ON 7\n"+
				"#if defined(ON)\n"+
				   "int itsOn = ON;\n"+
				"#endif");
		validateToken(IToken.t_int);
		validateIdentifier("itsOn");
		validateToken(IToken.tASSIGN);
		validateInteger("7");
		validateToken(IToken.tSEMI);
		validateEOF();
		initializeScanner("#if defined( NOTHING ) \nint x = NOTHING;\n#endif");
		validateEOF();
	}

	public void testQuickScan() throws Exception {
		initializeScanner("#if X + 5 < 7\n  int found = 1;\n#endif", ParserMode.QUICK_PARSE); 
		validateToken(IToken.t_int);
		validateIdentifier("found"); 
		validateToken(IToken.tASSIGN);
		validateInteger("1"); 
		validateToken(IToken.tSEMI);
		validateEOF();

		initializeScanner("#if 0\n  int error = 666;\n#endif"); 
		validateEOF();

	}

	public void testOtherPreprocessorCommands() throws Exception {
		initializeScanner("#\n#\t\n#define MAX_SIZE 1024\n#\n#  "); 
		validateEOF();
		validateDefinition("MAX_SIZE", "1024");  

		for (int i = 0; i < 4; ++i) {
			switch (i) {
			case 0:
				initializeScanner("#  ape"); 
				break;
			case 1:
				initializeScanner("#  #"); 
				break;
			case 2:
				initializeScanner("#  32"); 
				break;
			case 3:
				initializeScanner("#  defines"); 
				break;
			}

			validateEOF();
			// These are no longer fScanner exceptions, the are simply ignored.
		}
	}

	public void validateAllDefinitions(TableRow row) {
		int winner = row.selectWinner();
		int rowLength = row.getLength();
		for (int i = 0; i <= rowLength; ++i) {
			if (i == winner)
				validateDefinition(row.symbolName(i), row.symbolValue(i));
			else
				validateAsUndefined(row.symbolName(i));
		}
	}

	public void testBug36287() throws Exception {
		initializeScanner("X::X( const X & rtg_arg ) : U( rtg_arg ) , Z( rtg_arg.Z ) , er( rtg_arg.er ){}"); 
		validateIdentifier("X"); 
		validateToken(IToken.tCOLONCOLON);
		validateIdentifier("X"); 
		validateToken(IToken.tLPAREN);
		validateToken(IToken.t_const);
		validateIdentifier("X"); 
		validateToken(IToken.tAMPER);
		validateIdentifier("rtg_arg"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tCOLON);
		validateIdentifier("U"); 
		validateToken(IToken.tLPAREN);
		validateIdentifier("rtg_arg"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tCOMMA);
		validateIdentifier("Z"); 
		validateToken(IToken.tLPAREN);
		validateIdentifier("rtg_arg"); 
		validateToken(IToken.tDOT);
		validateIdentifier("Z"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tCOMMA);
		validateIdentifier("er"); 
		validateToken(IToken.tLPAREN);
		validateIdentifier("rtg_arg"); 
		validateToken(IToken.tDOT);
		validateIdentifier("er"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tLBRACE);
		validateToken(IToken.tRBRACE);
		validateEOF();

		initializeScanner("foo.*bar"); 
		validateIdentifier("foo"); 
		validateToken(IToken.tDOTSTAR);
		validateIdentifier("bar"); 
		validateEOF();

		initializeScanner("foo...bar"); 
		validateIdentifier("foo"); 
		validateToken(IToken.tELLIPSIS);
		validateIdentifier("bar"); 
		validateEOF();
	}

	public void testBug35892() throws Exception {
		initializeScanner("'c'"); 
		validateChar("c");
		validateEOF();
	}

	public void testBug36045() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append('"');
		buffer.append('\\');
		buffer.append('"');
		buffer.append('"');

		buffer.append('"');
		buffer.append('\\');
		buffer.append('\\');
		buffer.append('"');
		buffer.append("\n\n"); 
		initializeScanner(buffer.toString());
		validateString("\\\"\\\\"); 
	}

	public void testConditionalWithBraces() throws Exception {
		for (int i = 0; i < 4; ++i) {
			initializeScanner(
					"int foobar(int a) { if(a == 0) {\n"+
					"#ifdef THIS\n"+
					   "} else {}\n"+
					"#elif THAT\n" +
					   "} else {}\n"+
					"#endif\n"+
					"return 0;}"); 
			switch (i) {
			case 0:
				addDefinition("THIS", "1");  
				addDefinition("THAT", "1");  
				break;
			case 1:
				addDefinition("THIS", "1");  
				addDefinition("THAT", "0");  
				break;
			case 2:
				addDefinition("THAT", "1");  
				break;
			case 3:
				addDefinition("THAT", "0");  
				break;
			}

			validateToken(IToken.t_int);
			validateIdentifier("foobar"); 
			validateToken(IToken.tLPAREN);
			validateToken(IToken.t_int);
			validateIdentifier("a"); 
			validateToken(IToken.tRPAREN);
			validateToken(IToken.tLBRACE);
			validateToken(IToken.t_if);
			validateToken(IToken.tLPAREN);
			validateIdentifier("a"); 
			validateToken(IToken.tEQUAL);
			validateInteger("0"); 
			validateToken(IToken.tRPAREN);
			validateToken(IToken.tLBRACE);

			if (i <= 1) {
				validateToken(IToken.tRBRACE);
				validateToken(IToken.t_else);
				validateToken(IToken.tLBRACE);
				validateToken(IToken.tRBRACE);
			}

			if (i == 2) {
				validateToken(IToken.tRBRACE);
				validateToken(IToken.t_else);
				validateToken(IToken.tLBRACE);
				validateToken(IToken.tRBRACE);
			}

			validateToken(IToken.t_return);
			validateInteger("0"); 
			validateToken(IToken.tSEMI);
			validateToken(IToken.tRBRACE);
			validateEOF();
		}

	}

	public void testNestedRecursiveDefines() throws Exception {
		initializeScanner("#define C B A\n#define B C C\n#define A B\nA"); 

		validateIdentifier("B"); 
		validateDefinition("A", "B");  
		validateDefinition("B", "C C");  
		validateDefinition("C", "B A");  
		validateIdentifier("A"); 
		validateIdentifier("B"); 
		validateIdentifier("A"); 
		validateEOF();
	}

	public void testBug36316() throws Exception {
		initializeScanner("#define A B->A\nA"); 

		validateIdentifier("B"); 
		validateDefinition("A", "B->A");  
		validateToken(IToken.tARROW);
		validateIdentifier("A"); 
		validateEOF();
	}

	public void testBug36434() throws Exception {
		initializeScanner("#define X(Y)\nX(55)"); 
		validateEOF();
		/*IMacroDescriptor macro = fScanner.getDefinition( "X" ); 
		assertNotNull( macro ); 
		assertEquals( macro.getParameters().length, 1 );
		assertEquals( macro.getParameters()[0], "Y" ); 
		assertEquals( macro.getTokenizedExpansion().length, 0 );*/
	}

	public void testBug36047() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("# define MAD_VERSION_STRINGIZE(str)	#str\n"); 
		writer
				.write("# define MAD_VERSION_STRING(num)	MAD_VERSION_STRINGIZE(num)\n"); 
		writer
				.write("# define MAD_VERSION		MAD_VERSION_STRING(MAD_VERSION_MAJOR) \".\" \\\n"); 
		writer
				.write("                         MAD_VERSION_STRING(MAD_VERSION_MINOR) \".\" \\\n"); 
		writer
				.write("                         MAD_VERSION_STRING(MAD_VERSION_PATCH) \".\" \\\n"); 
		writer
				.write("                         MAD_VERSION_STRING(MAD_VERSION_EXTRA)\n"); 
		writer.write("# define MAD_VERSION_MAJOR 2\n"); 
		writer.write("# define MAD_VERSION_MINOR 1\n"); 
		writer.write("# define MAD_VERSION_PATCH 3\n"); 
		writer.write("# define MAD_VERSION_EXTRA boo\n"); 
		writer.write("MAD_VERSION\n"); 
		initializeScanner(writer.toString());

		validateString("2.1.3.boo"); 

		validateEOF();
	}

	public void testBug36475() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write(" \"A\" \"B\" \"C\" "); 

		initializeScanner(writer.toString());

		validateString("ABC"); 
		validateEOF();
	}

	public void testBug36509() throws Exception {
		StringWriter writer = new StringWriter();
		writer
				.write("#define debug(s, t) printf(\"x\" # s \"= %d, x\" # t \"= %s\", \\\n"); 
		writer.write("                    x ## s, x ## t) \n"); 
		writer.write("debug(1, 2);"); 

		initializeScanner(writer.toString());
		//printf("x1=%d, x2= %s", x1, x2); 
		validateIdentifier("printf"); 
		validateToken(IToken.tLPAREN);
		validateString("x1= %d, x2= %s"); 
		validateToken(IToken.tCOMMA);
		validateIdentifier("x1"); 
		validateToken(IToken.tCOMMA);
		validateIdentifier("x2"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
	}

	public void testBug36695() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("\'\\4\'  \'\\n\'"); 
		initializeScanner(writer.toString());

		validateChar("\\4"); 
		validateChar("\\n"); 
		validateEOF();
	}

	public void testBug36521() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#define str(s)      # s\n"); 
		writer.write("fputs(str(strncmp(\"abc\\0d\", \"abc\", \'\\4\')\n"); 
		writer.write("        == 0), s);\n"); 

		initializeScanner(writer.toString());
		validateIdentifier("fputs"); 
		validateToken(IToken.tLPAREN);

		//TODO as in 36701B, whitespace is not properly replaced inside the string, ok for now.
		validateString("strncmp(\\\"abc\\\\0d\\\", \\\"abc\\\", '\\\\4') == 0");  
		//validateString("strncmp(\\\"abc\\\\0d\\\", \\\"abc\\\", '\\\\4')         == 0"); 

		validateToken(IToken.tCOMMA);
		validateIdentifier("s"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
	}

	public void testBug36770() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#define A 0\n"); 
		writer.write("#if ( A == 1 )\n"); 
		writer.write("#  define foo 1\n"); 
		writer.write("#else\n"); 
		writer.write("# define foo 2\n"); 
		writer.write("#endif\n"); 
		writer.write("foo\n"); 
		initializeScanner(writer.toString());
		validateInteger("2"); 
		validateEOF();
	}

	public void testBug36816() throws Exception {
		initializeScanner("#include \"foo.h", ParserMode.QUICK_PARSE); 
		validateEOF();
		validateProblemCount(2);

		initializeScanner("#include <foo.h", ParserMode.QUICK_PARSE); 
		validateEOF();
		validateProblemCount(2);

		initializeScanner("#define FOO(A", ParserMode.QUICK_PARSE); 
		validateEOF();
		validateProblemCount(1);

		initializeScanner("#define FOO(A \\ B", ParserMode.QUICK_PARSE); 
		validateEOF();
		validateProblemCount(1);
	}

	public void testBug36255() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#if defined ( A ) \n"); 
		writer.write("   #if defined ( B ) && ( B != 0 ) \n"); 
		writer.write("      boo\n"); 
		writer.write("   #endif /*B*/\n"); 
		writer.write("#endif /*A*/"); 

		initializeScanner(writer.toString());
		validateEOF();
	}

	public void testBug37011() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#define A \"//\""); 

		initializeScanner(writer.toString());

		validateEOF();
		validateDefinition("A", "\"//\"");  
	}

	public void testOtherPreprocessorDefines() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#define A a//boo\n"); 
		writer.write("#define B a /*boo*/ a\n"); 
		writer.write("#define C a \" //boo \"\n"); 
		writer.write("#define D a \\\"//boo\n"); 
		writer.write("#define E a \\n \"\\\"\"\n"); 
		writer.write("#define F a\\\n b\n"); 
		writer.write("#define G a '\"'//boo\n"); 
		writer.write("#define H a '\\'//b'\"/*bo\\o*/\" b\n"); 

		initializeScanner(writer.toString());

		validateEOF();

		validateDefinition("A", "a");  
		validateDefinition("B", "a a");  
		validateDefinition("C", "a \" //boo \"");  
		validateDefinition("D", "a \\\"//boo");  
		validateDefinition("E", "a \\n \"\\\"\"");  
		validateDefinition("F", "a b");  
		validateDefinition("G", "a '\"'");  
		validateDefinition("H", "a '\\'//b'\"/*bo\\o*/\" b");  
	}

	public void testBug38065() throws Exception {
		initializeScanner("Foo\\\nBar"); 

		validateIdentifier("FooBar"); 
		validateEOF();

	}

	public void testBug36701A() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#define str(s) # s\n"); 
		writer.write("str( @ \\n )\n"); 

		initializeScanner(writer.toString());
		validateString("@ \\n"); 
		validateEOF();
	}

	public void testBug36701B() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#define str(s) # s\n"); 
		writer.write("str( @ /*ff*/  \\n  hh  \"aa\"  )\n"); 

		initializeScanner(writer.toString());

		validateString("@ \\n hh \\\"aa\\\""); 
		validateEOF();
	}

	public void testBug44305() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#define WCHAR_MAX 0 \n"); 
		writer.write("#if WCHAR_MAX <= 0xff\n"); 
		writer.write("bool\n"); 
		writer.write("#endif"); 
		initializeScanner(writer.toString());
		validateToken(IToken.t_bool);
		validateEOF();
	}

	public void testBug45287() throws Exception {
		initializeScanner("'abcdefg' L'hijklmnop'"); 
		validateChar("abcdefg"); 
		validateWideChar("hijklmnop"); 
		validateEOF();
	}

	public void testBug45476() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define X 5\n"); 
		buffer.append("#if defined X\n"); 
		buffer.append("#define Y 10\n"); 
		buffer.append("#endif"); 
		initializeScanner(buffer.toString());
		validateEOF();
		validateDefinition("Y", "10");  
	}

	public void testBug45477() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define D\n"); 
		buffer.append("#define D\n"); 
		buffer.append("#define sum(x,y) x+y\n"); 
		buffer.append("#define E 3\n"); 
		buffer.append("#define E 3\n"); 
		buffer.append("#define sum(x,y) x+y\n"); 
		buffer.append("#if defined(D)\n"); 
		buffer.append("printf\n"); 
		buffer.append("#endif\n"); 
		buffer.append("#if defined(sum)\n"); 
		buffer.append("scanf\n"); 
		buffer.append("#endif\n"); 
		buffer.append("#if defined(E)\n"); 
		buffer.append("sprintf\n"); 
		buffer.append("#endif\n"); 
		initializeScanner(buffer.toString());
		validateIdentifier("printf"); 
		validateIdentifier("scanf"); 
		validateIdentifier("sprintf"); 
		validateEOF();

		for (int i = 0; i < 5; ++i) {

			buffer = new StringBuffer();

			buffer.append("#define D blah\n"); 

			switch (i) {
			case 0:
				buffer.append("#define D\n"); 
				break;
			case 1:
				buffer.append("#define D( x ) echo\n"); 
				break;
			case 2:
				buffer.append("#define D ACDC\n"); 
				break;
			case 3:
				buffer.append("#define D defined( D )\n"); 
				break;
			case 4:
				buffer.append("#define D blahh\n"); 
				break;

			}

			initializeScanner(buffer.toString());
			validateEOF();
		}

		buffer = new StringBuffer();
		buffer.append("#define X 5\n"); 
		buffer.append("#define Y 7\n"); 
		buffer.append("#define SUMXY X    _+     Y"); 
		buffer.append("#define SUMXY   X + Y"); 
		initializeScanner(buffer.toString());
		validateEOF();
	}

	public void testBug45551() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define stdio someNonExistantIncludeFile\n"); 
		buffer.append("#include <stdio.h>\n"); 

		initializeScanner(buffer.toString());
		validateEOF();
		validateProblemCount(1); // file does not exist
		IASTPreprocessorIncludeStatement[] includes= fLocationResolver.getIncludeDirectives();
		assertEquals(1, includes.length);
		assertEquals("stdio.h", includes[0].getName().toString()); 
	}

	public void testBug46402() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define X 5\n"); 
		buffer.append("#if defined( X )\n"); 
		buffer.append("// blah\n"); 
		buffer.append("#elif Y > 5 \n"); 
		buffer.append("// coo\n"); 
		buffer.append("#endif\n"); 
		initializeScanner(buffer.toString(), ParserMode.COMPLETE_PARSE);
		validateEOF();
	}

	public void testBug50821() throws Exception {
		initializeScanner("\'\n\n\n", ParserMode.QUICK_PARSE); 
		fScanner.nextToken();
		validateProblemCount(1);
	}

	public void test54778() throws Exception {
		initializeScanner("#if 1 || 0 < 3 \n printf \n #endif\n"); 
		validateIdentifier("printf"); 
		validateEOF();
		initializeScanner("#if !defined FOO || FOO > 3\nprintf\n#endif\n"); 
		validateIdentifier("printf"); 
		validateEOF();
		initializeScanner("#if !defined FOO || FOO < 3\nprintf\n#endif\n"); 
		validateIdentifier("printf"); 
		validateEOF();
	}

	public void test68229() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define COUNT 0     \n"); 
		writer.write("1                   \n"); 
		writer.write("#if COUNT           \n"); 
		writer.write("   2                \n"); 
		writer.write("#endif              \n"); 
		writer.write("3                   \n"); 

		initializeScanner(writer.toString());

		IToken t1 = fScanner.nextToken();
		IToken t3 = fScanner.nextToken();

		assertEquals(t1.getImage(), "1"); 
		assertEquals(t3.getImage(), "3"); 
		assertEquals(t1.getNext(), t3);
		validateEOF();

		writer = new StringWriter();
		writer.write("#define FOO( x ) x   \n"); 
		writer.write("1  FOO( 2 )  3       \n"); 

		initializeScanner(writer.toString());
		t1 = fScanner.nextToken();
		IToken t2 = fScanner.nextToken();
		t3 = fScanner.nextToken();
		validateEOF();

		assertEquals(t1.getImage(), "1"); 
		assertEquals(t2.getImage(), "2"); 
		assertEquals(t3.getImage(), "3"); 

		assertEquals(t1.getNext(), t2);
	}

	public void testBug56517() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#if 0 \n"); 
		writer.write("char * x = \"#boo\";\n"); 
		writer.write("#endif\n"); 
		initializeScanner(writer.toString());
		validateEOF();
	}

	public void testBug36770B() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define A 0\n"); 
		writer.write("#if ( A == 1 )\n"); 
		writer.write("#  define foo\n"); 
		writer.write("#else\n"); 
		writer.write("#   define bar\n"); 
		writer.write("#endif\n"); 
		initializeScanner(writer.toString(), ParserMode.QUICK_PARSE);
		validateEOF();
		validateDefinition("A", 0); 
		validateDefinition("bar", "");  

	}

	public void testBug47797() throws Exception {
		initializeScanner("\"\\uABCD\" \'\\uABCD\' \\uABCD_ident \\u001A01BC_ident ident\\U01AF ident\\u01bc00AF"); 
		validateString("\\uABCD"); 
		validateChar("\\uABCD"); 
		validateIdentifier("\\uABCD_ident"); 
		validateIdentifier("\\u001A01BC_ident"); 
		validateIdentifier("ident\\U01AF"); 
		validateIdentifier("ident\\u01bc00AF"); 
		validateEOF();
	}

	public void testBug59768() throws Exception {
		initializeScanner("#define A A\nA"); 
		validateIdentifier("A"); 
		validateEOF();
		/*IMacroDescriptor d = fScanner.getDefinition( "A"); 
		assertTrue( d.isCircular() );*/
	}

	public void testBug60764() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define P   a,b\n"); 
		writer.write("#define M(x) M1(x)\n"); 
		writer.write("#define M1(x,y) #x  #y\n"); 
		writer.write("M(P)\n"); 
		initializeScanner(writer.toString());
		validateString("ab"); 
		validateEOF();
	}

	public void testBug62042() throws Exception {
		initializeScanner("0x", ParserMode.QUICK_PARSE); 
		validateInteger("0x"); // to me this is a valid number 
		validateEOF();
	}

	public void testBug61968() throws Exception {
		Writer writer = new StringWriter();
		writer.write("unsigned int ui = 2172748163; //ok \n"); 
		writer.write("int big = 999999999999999;//ok \n"); 
		writer.write("void main() { \n"); 
		writer.write("caller(4);  //ok\n"); 
		writer
				.write("caller(2172748163);//causes java.lang.NumberFormatException \n"); 
		writer
				.write("caller(999999999999999); //also causes NumberFormatException \n"); 
		writer.write("}\n"); 
		initializeScanner(writer.toString(), ParserMode.QUICK_PARSE);
		fullyTokenize();
		validateProblemCount(0);
	}

	public void testBug62378() throws Exception {
		initializeScanner("\"\\?\\?<\""); 
		validateString("\\?\\?<"); 
	}

	public void testBug62384() throws Exception {
		initializeScanner("18446744073709551615LL"); 
		validateInteger("18446744073709551615LL"); 
	}

	public void testBug62390() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define f(x) x\n"); 
		writer.write("#if f(\n"); 
		writer.write("5) == 5\n"); 
		writer.write("true1\n"); 
		writer.write("#endif\n"); 
		writer.write("#if A\n"); 
		writer.write("#elif f(\n"); 
		writer.write("5) == 5\n"); 
		writer.write("true2\n"); 
		writer.write("#endif\n"); 
		writer.write("#undef f\n"); 
		writer.write("#define f(x) \"A0I70_001.h\"\n"); 
		writer.write("#include f(\n"); 
		writer.write("5\n"); 
		writer.write(")\n"); 
		writer.write("#undef f\n"); 
		writer.write("#define f(x) 1467\n"); 
		writer.write("#line f(\n"); 
		writer.write("5\n"); 
		writer.write(")\n"); 
		writer.write("#pragma f(\n"); 
		writer.write("5\n"); 
		writer.write(")\n"); 
		writer.write("}\n"); 
		initializeScanner(writer.toString(), ParserMode.QUICK_PARSE);
		fullyTokenize();
	}

	public void testBug62009() throws Exception {
		initializeScanner("#define def(x) (x#)\ndef(orange)\n", ParserMode.QUICK_PARSE); 
		fullyTokenize();
		validateProblemCount(1); 
	}

	public void testBug61972() throws Exception {
		initializeScanner("#define DEF1(A1) A1\n#define DEF2     DEF1(DEF2)\nDEF2;"); 
		validateIdentifier("DEF2"); 
		validateToken(IToken.tSEMI);
		validateEOF();
	}

	public void testBug64268() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define BODY \\\n"); 
		writer.write(" {	 \\\n"); 
		writer.write(" /* this multi-line comment messes \\\n"); 
		writer.write(" up the parser.  */ }\n"); 
		writer.write("BODY "); 
		initializeScanner(writer.toString());
		validateToken(IToken.tLBRACE);
		validateToken(IToken.tRBRACE);
		validateEOF();
	}

	public void testUndef() throws Exception {
		initializeScanner("#define A 5\n" + 
				"#define B 10\n" + 
				"#undef A\n" + 
				"A B"); 
		validateIdentifier("A"); 
		validateInteger("10"); 
		validateEOF();
	}

	public void testWackyFunctionMacros() throws Exception {
		initializeScanner("#define A(X) hi##X\n" + 
				"#define B(Y) A(Y)\n" + 
				"B(there)"); 
		validateIdentifier("hithere"); 
		validateEOF();
	}

	public void testSlashes() throws Exception {
		initializeScanner("__q / __n"); 
		validateIdentifier("__q"); 
		validateToken(IToken.tDIV);
		validateIdentifier("__n"); 
		validateEOF();
	}

	public void testStringify() throws Exception {
		initializeScanner("#define xS(s) #s\n#define S(s) xS(s)\n#define X hi\nS(X)"); 
		validateString("hi"); 
		validateEOF();
	}

	public void testWideToNarrowConcatenation() throws Exception {
		initializeScanner("\"ONE\" L\"TWO\""); 
		validateLString("ONETWO"); 
		validateEOF();
	}
	
	public void testUTFStringConcatenation() throws Exception {
		IScannerExtensionConfiguration config = new GPPScannerExtensionConfiguration() {
			@Override public boolean supportUTFLiterals() { return true; }
		};
		initializeScanner("u\"a\" u\"b\"", ParserLanguage.CPP, config);
		validateUTF16String("ab");
		validateEOF();
		initializeScanner("u\"a\" \"b\"", ParserLanguage.CPP, config);
		validateUTF16String("ab");
		validateEOF();
		initializeScanner("\"a\" u\"b\"", ParserLanguage.CPP, config);
		validateUTF16String("ab");
		validateEOF();
		
		initializeScanner("U\"a\" U\"b\"", ParserLanguage.CPP, config);
		validateUTF32String("ab");
		validateEOF();
		initializeScanner("U\"a\" \"b\"", ParserLanguage.CPP, config);
		validateUTF32String("ab");
		validateEOF();
		initializeScanner("\"a\" U\"b\"", ParserLanguage.CPP, config);
		validateUTF32String("ab");
		validateEOF();
	}

	public void testEmptyIncludeDirective() throws Exception {
		initializeScanner("#include \n#include <foo.h>\n"); 
		validateEOF();
		IASTPreprocessorIncludeStatement[] includes= fLocationResolver.getIncludeDirectives();
		assertEquals(1, includes.length);
		assertEquals("foo.h", includes[0].getName().toString()); 
	}

	public void testBug69412() throws Exception {
		initializeScanner("\'\\\\\'", ParserMode.COMPLETE_PARSE); 
		validateChar("\\\\"); 
		validateEOF();
		validateProblemCount(0);
	}

	public void testBug70072() throws Exception {
		initializeScanner("#if 1/0\nint i;\n#elif 2/0\nint j;\n#endif\nint k;\n"); 
		fullyTokenize();
	}

	public void testBug70261() throws Exception {
		initializeScanner("0X0"); 
		validateInteger("0X0"); 
	}

	public void testBug62571() throws Exception {
		StringBuffer buffer = new StringBuffer("#define J(X,Y) X##Y\n"); 
		buffer.append("J(A,1Xxyz)\n"); 
		buffer.append("J(B,1X1X1Xxyz)\n"); 
		buffer.append("J(C,0Xxyz)\n"); 
		buffer.append("J(CC,0Xxyz)\n"); 
		buffer.append("J(D,0xxyz)\n"); 
		buffer.append("J(E,0x0x0xxyz)\n"); 
		initializeScanner(buffer.toString());
		validateIdentifier("A1Xxyz"); 
		validateIdentifier("B1X1X1Xxyz"); 
		validateIdentifier("C0Xxyz"); 
		validateIdentifier("CC0Xxyz"); 
		validateIdentifier("D0xxyz"); 
		validateIdentifier("E0x0x0xxyz"); 
	}

	public void testBug69134() throws Exception {
		Writer writer = new StringWriter();
		writer.write("# ifdef YYDEBUG\n"); 
		writer.write("if (yyDebug) {\n"); 
		writer.write("(void) fprintf (yyTrace,\n"); 
		writer.write("\"  # |Position|State|Mod|Lev|Action |Terminal and Lookahead or Rule\\n\");\n"); 
		writer.write("yyNl ();\n"); 
		writer.write("}\n"); 
		writer.write("# endif\n"); 
		initializeScanner(writer.toString());
		fullyTokenize();
		validateProblemCount(0);
	}

	public void testBug70073() throws Exception {
		StringBuffer buffer = new StringBuffer(
				"#if CONST \n #endif \n #elif CONST \n int"); 
		final List problems = new ArrayList();
		initializeScanner(buffer.toString());
		validateToken(IToken.t_int);
		validateProblemCount(1);
	}

	public void testBug73652() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#define DoSuperMethodA IDoSuperMethodA\n"); 
		writer
				.write("#define IDoSuperMethodA(a,b,c) IIntuition->IDoSuperMethodA(a,b,c)\n"); 
		writer.write("DoSuperMethodA(0,0,0);\n"); 

		initializeScanner(writer.toString());

		validateIdentifier("IIntuition"); 
		validateToken(IToken.tARROW);
		validateIdentifier("IDoSuperMethodA"); 
		validateToken(IToken.tLPAREN);
		validateInteger("0"); 
		validateToken(IToken.tCOMMA);
		validateInteger("0"); 
		validateToken(IToken.tCOMMA);
		validateInteger("0"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();
	}

	public void testBug73652_2() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#define DoSuperMethodA DoSuperMethodB //doobalie\n"); 
		writer.write("#define DoSuperMethodB DoSuperMethodC /*oogalie*/ \n"); 
		writer.write("#define DoSuperMethodC IDoSuperMethodA \\\n\n"); 
		writer
				.write("#define IDoSuperMethodA(a,b,c) IIntuition->IDoSuperMethodA(a,b,c)\n"); 
		writer.write("DoSuperMethodA  (0,0,0);\n"); 

		initializeScanner(writer.toString());

		validateIdentifier("IIntuition"); 
		validateToken(IToken.tARROW);
		validateIdentifier("IDoSuperMethodA"); 
		validateToken(IToken.tLPAREN);
		validateInteger("0"); 
		validateToken(IToken.tCOMMA);
		validateInteger("0"); 
		validateToken(IToken.tCOMMA);
		validateInteger("0"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);
		validateEOF();

	}

	public void testBug72997() throws Exception {
		initializeScanner("'\\\\'"); 
		validateChar("\\\\"); 
		validateEOF();
	}

	public void testBug72725() throws Exception {
		for (int i = 0; i < 2; ++i) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("#define a \\"); 
			if (i == 0)
				buffer.append("\r"); 
			buffer.append("\n"); 
			buffer.append("long macro stuff"); 
			if (i == 0)
				buffer.append("\r"); 
			buffer.append("\n"); 

			initializeScanner(buffer.toString());
			validateEOF();
			validateProblemCount(0);
		}
	}

	public void testBug72506() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#define INCFILE(x) ver ## x\n"); 
		writer.write("#define xstr(x) str(x)\n"); 
		writer.write("#define str(x) #x\n"); 
		writer.write("xstr(INCFILE(2).h)\n"); 

		initializeScanner(writer.toString());
		validateString("ver2.h"); 
		validateEOF();
	}

	public void testBug72506_2() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#define str(x) #x\n"); 
		writer.write("#define A B\n"); 
		writer.write("#define B A\n"); 
		writer.write("str(B)\n"); 

		initializeScanner(writer.toString());
		validateString("B"); 
		validateEOF();
	}

	public void testMacroPastingError() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("#define m(expr) \\\r\n"); 
		writer.write("    foo( #expr )  \r\n"); 

		initializeScanner(writer.toString());
		validateEOF();
		validateProblemCount(0);
	}

	public void testBug74176() throws Exception {
		initializeScanner("#define MYSTRING \"X Y Z "); 
		validateEOF();

		initializeScanner("#define m(b) #"); 
		validateEOF();

		initializeScanner("#define m(foo,b) #b"); 
		validateEOF();
	}

	public void testBug74180() throws Exception {
		initializeScanner("true false", ParserLanguage.C); 
		validateIdentifier("true"); 
		validateIdentifier("false"); 

		initializeScanner("true false", ParserLanguage.CPP); 
		validateToken(IToken.t_true);
		validateToken(IToken.t_false);
	}

	public void testBug73492() throws Exception {
		String code = "#define PTR void *\n" + 
				"PTR;\n"; 

		int offset = code.indexOf("PTR;") + 3;  
		initializeScanner(code);

		IToken t = fScanner.nextToken();
		assertEquals(t.getType(), IToken.t_void);
		assertEquals(offset, t.getOffset());

		t = fScanner.nextToken();
		assertEquals(t.getType(), IToken.tSTAR);
		assertEquals(offset + 1, t.getOffset());

		t = fScanner.nextToken();
		assertEquals(t.getType(), IToken.tSEMI);
		assertEquals(offset + 2, t.getOffset());
	}

	public void testBug74328() throws Exception {
		initializeScanner("\"\";\n"); 
		validateString(""); 
		validateToken(IToken.tSEMI);
		validateEOF();
	}

	public void testBug72537() throws Exception {
		initializeScanner("FOO BAR(boo)"); 
		fScanner.addMacroDefinition("FOO".toCharArray(), "foo".toCharArray()); 
		fScanner.addMacroDefinition("BAR(x)".toCharArray(), "x".toCharArray()); 

		validateIdentifier("foo"); 
		validateIdentifier("boo"); 
		validateEOF();
	}

	public void testBug75083() throws Exception {
		String code = "#define blah() { extern foo\n blah()\n"; 
		initializeScanner(code);

		int idx = code.indexOf("\n blah()") + 2 + 6; 
		IToken t = fScanner.nextToken();
		assertEquals(IToken.tLBRACE, t.getType());
		assertEquals(idx, t.getOffset());
		assertEquals(idx + 1, t.getEndOffset());

		t = fScanner.nextToken();
		assertEquals(IToken.t_extern, t.getType());
		assertEquals(idx + 1, t.getOffset());
		assertEquals(idx + 2, t.getEndOffset());

		t = fScanner.nextToken();
		assertEquals(IToken.tIDENTIFIER, t.getType());
		assertEquals(idx + 2, t.getOffset());
		assertEquals(idx + 3, t.getEndOffset());
	}

	// when fixing 75532 several IProblems were added to ExpressionEvaluator and one to Scanner2, this is to test them
	// the problems currently don't get reported when using the DOMScanner
	public void testBug75532IProblems() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#if 09 == 9\n#endif\n"); // malformed octal 
		writer.write("#if 1A == 0x1A\n#endif\n"); // malformed decimal 
		writer.write("#if 0x == 0x0\n#endif\n"); // malformed hex 
		writer.write("#if 0xAX == 0xA\n#endif\n"); // malformed hex 
		writer.write("#if 1/0 == 1\n#endif\n"); // division by zero  
		writer.write("#if defined ( sadf a\n#endif\n"); // missing ')' in defined  
		writer.write("#if defined ( sadf\n#endif\n"); // missing ')' in defined  
		writer.write("#if defined ( 2sadf )\n#endif\n"); // illegal identifier in defined  
		writer.write("#if ( 1 == 1 ? 1\n#endif\n"); // bad conditional expression   
		writer.write("#if (  \n#endif\n"); // expression syntax error  
		writer.write("#if @\n#endif\n"); // expression syntax error  
		writer.write("#if \n#endif\n"); // expression syntax error  
		writer.write("#if -\n#endif\n"); // expression syntax error  
		writer.write("#if ( 1 == 1\n#endif\n"); // missing ')'  
		writer.write("#if 1 = 1\n#endif\n"); // assignment not allowed 

		writer.write("int main(int argc, char **argv) {\n"); 
		writer.write("if ( 09 == 9 )\n"); // added while fixing this bug, IProblem on invalid octal number 
		writer.write("return 1;\nreturn 0;\n}\n"); 

		initializeScanner(writer.toString());
		fullyTokenize();
		IASTProblem[] problems= fLocationResolver.getScannerProblems();
		assertEquals(17, problems.length);
		int i= 0;
		assertEquals(IProblem.SCANNER_BAD_OCTAL_FORMAT,          problems[i].getID() );  
		assertEquals(IProblem.SCANNER_BAD_DECIMAL_FORMAT,        problems[++i].getID() );  
		assertEquals(IProblem.SCANNER_BAD_HEX_FORMAT,            problems[++i].getID() );  
		assertEquals(IProblem.SCANNER_BAD_HEX_FORMAT,            problems[++i].getID() );  
		assertEquals(IProblem.SCANNER_DIVIDE_BY_ZERO,            problems[++i].getID() );  
		assertEquals(IProblem.SCANNER_MISSING_R_PAREN,           problems[++i].getID() );  
		assertEquals(IProblem.SCANNER_MISSING_R_PAREN,           problems[++i].getID() );  
		assertEquals(IProblem.SCANNER_ILLEGAL_IDENTIFIER,        problems[++i].getID() );  
		assertEquals(IProblem.SCANNER_BAD_CONDITIONAL_EXPRESSION,problems[++i].getID() );  
		assertEquals(IProblem.SCANNER_EXPRESSION_SYNTAX_ERROR,   problems[++i].getID() );  
		assertEquals(IProblem.SCANNER_BAD_CHARACTER,   			 problems[++i].getID() );  
		assertEquals(IProblem.SCANNER_EXPRESSION_SYNTAX_ERROR,   problems[++i].getID() ); 
		assertEquals(IProblem.SCANNER_EXPRESSION_SYNTAX_ERROR,   problems[++i].getID() ); 
		assertEquals(IProblem.SCANNER_EXPRESSION_SYNTAX_ERROR,   problems[++i].getID() ); 
		assertEquals(IProblem.SCANNER_MISSING_R_PAREN,           problems[++i].getID() ); 
		assertEquals(IProblem.SCANNER_ASSIGNMENT_NOT_ALLOWED,    problems[++i].getID() ); 
		assertEquals(IProblem.SCANNER_BAD_OCTAL_FORMAT,          problems[++i].getID() ); 
	}

	public void testExpressionEvalProblems() throws Exception {
		Writer writer = new StringWriter();
		writer.write(" #if 1 == 1L       \n"); 
		writer.write(" #endif            \n"); 

		initializeScanner(writer.toString());
		validateEOF();
		validateProblemCount(0);
	}

	public void testExpressionEvalProblems_2() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define FOO( a, b ) a##b    \n"); 
		writer.write("#if FOO ( 1, 0 ) == 10      \n"); 
		writer.write("1                           \n"); 
		writer.write("#endif                      \n"); 

		initializeScanner(writer.toString());
		validateInteger("1"); 
		validateEOF();
		validateProblemCount(0);
	}

	public void testUnExpandedFunctionMacro() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define foo( a ) #a         \n"); 
		writer.write("foo( 1 )   foo              \n"); 

		initializeScanner(writer.toString());
		validateString("1"); 
		validateIdentifier("foo"); 
		validateEOF();
	}

	public void testBug39688A() throws Exception { // test valid IProblems
		Writer writer = new StringWriter();
		writer.write("#define decl1(type, ...    \\\n  )   type var;\n"); 
		writer.write("decl1(int, x, y, z)\n"); 
		writer.write("#define decl2(type, args...) type args;"); 
		writer.write("decl2(int, a, b, c, x, y, z)\n"); 
		writer.write("#define decl3(type, args...) \\\n   type args;"); 
		writer.write("decl3(int, a, b, c, x, y)\n"); 
		writer.write("#define decl4(type, args... \\\n   ) type args;"); 
		writer.write("decl4(int, a, b, z)\n"); 
		writer.write("#define decl5(type, ...) type __VA_ARGS__;"); 
		writer.write("decl5(int, z)\n"); 
		writer.write("#define decl6(type, ...    \\\n) type __VA_ARGS__;"); 
		writer.write("decl6(int, a, b, c, x)\n"); 
		writer.write("#define foo(a) a __VA_ARGS__;\n");  // C99: 6.10.3.5 this should produce an IProblem
		writer.write("#define foo2(a) a #__VA_ARGS__;\n");  // C99: 6.10.3.5 this should produce an IProblem

		initializeScanner(writer.toString());
		fullyTokenize();
		IASTProblem[] problems= fLocationResolver.getScannerProblems();
		assertEquals(2, problems.length);
		assertTrue(problems[0].getID() == IProblem.PREPROCESSOR_INVALID_VA_ARGS);
		assertTrue(problems[1].getID() == IProblem.PREPROCESSOR_MACRO_PASTING_ERROR);
	}

	public void testBug39688B() throws Exception { // test C99
		Writer writer = new StringWriter();
		writer.write("#define debug(...) fprintf(stderr, __VA_ARGS__)\n"); 
		writer.write("#define showlist(...) puts(#__VA_ARGS__)\n"); 
		writer
				.write("#define report(test, ...) ((test)?puts(#test):\\\n   printf(__VA_ARGS__))\n"); 
		writer.write("int main() {\n"); 
		writer.write("debug(\"Flag\");\n"); 
		writer.write("debug(\"X = %d\\n\", x);\n"); 
		writer.write("showlist(The first, second, and third items.);\n"); 
		writer.write("report(x>y, \"x is %d but y is %d\", x, y);\n"); 
		writer.write("return 0; }\n"); 

		initializeScanner(writer.toString());
		fullyTokenize();
		validateProblemCount(0);

		Map<String, IMacroBinding> defs = fScanner.getMacroDefinitions();
		assertTrue(defs.containsKey("debug")); 
		assertTrue(defs.containsKey("showlist")); 
		assertTrue(defs.containsKey("report")); 
		IMacroBinding debug = defs.get("debug"); 
		assertTrue(new String(debug.getParameterPlaceholderList()[0]).equals("__VA_ARGS__")); 
		assertEquals("fprintf(stderr, __VA_ARGS__)", new String(debug.getExpansion())); 
		
		IMacroBinding showlist = defs.get("showlist"); 
		assertTrue(new String(showlist.getParameterPlaceholderList()[0]).equals("__VA_ARGS__")); 
		assertTrue(new String(showlist.getExpansion())
				.equals("puts(#__VA_ARGS__)")); 
		IMacroBinding report = defs.get("report"); 
		assertTrue(new String(report.getParameterPlaceholderList()[0]).equals("test")); 
		assertTrue(new String(report.getParameterPlaceholderList()[1]).equals("__VA_ARGS__")); 
		assertTrue(new String(report.getExpansion())
				.equals("((test)?puts(#test): printf(__VA_ARGS__))")); 

		check39688Tokens(writer);
	}

	public void testBug39688C() throws Exception { // test GCC
		Writer writer = new StringWriter();
		writer.write("#define debug(vars...) fprintf(stderr, vars)\n"); 
		writer.write("#define showlist(vars...) puts(#vars)\n"); 
		writer
				.write("#define report(test, vars...) ((test)?puts(#test):\\\n   printf(vars))\n"); 
		writer.write("int main() {\n"); 
		writer.write("debug(\"Flag\");\n"); 
		writer.write("debug(\"X = %d\\n\", x);\n"); 
		writer.write("showlist(The first, second, and third items.);\n"); 
		writer.write("report(x>y, \"x is %d but y is %d\", x, y);\n"); 
		writer.write("return 0; }\n"); 

		initializeScanner(writer.toString());
		fullyTokenize();
		validateProblemCount(0);

		Map defs = fScanner.getMacroDefinitions();
		assertTrue(defs.containsKey("debug")); 
		assertTrue(defs.containsKey("showlist")); 
		assertTrue(defs.containsKey("report")); 
		IMacroBinding debug = (IMacroBinding) defs.get("debug"); 
		assertTrue(new String(debug.getParameterPlaceholderList()[0]).equals("vars")); 
		assertTrue(new String(debug.getExpansion())
				.equals("fprintf(stderr, vars)")); 
		IMacroBinding showlist = (IMacroBinding) defs.get("showlist"); 
		assertTrue(new String(showlist.getParameterPlaceholderList()[0]).equals("vars")); 
		assertTrue(new String(showlist.getExpansion()).equals("puts(#vars)")); 
		IMacroBinding report = (IMacroBinding) defs.get("report"); 
		assertTrue(new String(report.getParameterPlaceholderList()[0]).equals("test")); 
		assertTrue(new String(report.getParameterPlaceholderList()[1]).equals("vars")); 
		assertTrue(new String(report.getExpansion())
				.equals("((test)?puts(#test): printf(vars))")); 

		check39688Tokens(writer);
	}

	private void check39688Tokens(Writer writer) throws Exception {
		initializeScanner(writer.toString());

		validateToken(IToken.t_int);
		validateIdentifier("main"); 
		validateToken(IToken.tLPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tLBRACE);

		validateIdentifier("fprintf"); 
		validateToken(IToken.tLPAREN);
		validateIdentifier("stderr"); 
		validateToken(IToken.tCOMMA);
		validateString("Flag"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);

		validateIdentifier("fprintf"); 
		validateToken(IToken.tLPAREN);
		validateIdentifier("stderr"); 
		validateToken(IToken.tCOMMA);
		validateString("X = %d\\n"); 
		validateToken(IToken.tCOMMA);
		validateIdentifier("x"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);

		validateIdentifier("puts"); 
		validateToken(IToken.tLPAREN);
		validateString("The first, second, and third items."); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);

		validateToken(IToken.tLPAREN);
		validateToken(IToken.tLPAREN);
		validateIdentifier("x"); 
		validateToken(IToken.tGT);
		validateIdentifier("y"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tQUESTION);
		validateIdentifier("puts"); 
		validateToken(IToken.tLPAREN);
		validateString("x>y"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tCOLON);
		validateIdentifier("printf"); 
		validateToken(IToken.tLPAREN);
		validateString("x is %d but y is %d"); 
		validateToken(IToken.tCOMMA);
		validateIdentifier("x"); 
		validateToken(IToken.tCOMMA);
		validateIdentifier("y"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tSEMI);

		validateToken(IToken.t_return);
		validateInteger("0"); 
		validateToken(IToken.tSEMI);
		validateToken(IToken.tRBRACE);

		validateEOF();
	}

	public void testMacroArgumentExpansion() throws Exception {
		Writer writer = new StringWriter();
		writer
				.write("#define g_return( expr ) ( expr )                                   \n"); 
		writer
				.write("#define ETH( obj ) ( CHECK( (obj), boo ) )                          \n"); 
		writer
				.write("#define CHECK CHECK_INSTANCE                                        \n"); 
		writer
				.write("#define CHECK_INSTANCE( instance, type ) (foo((instance), (type)))  \n"); 
		writer
				.write("g_return( ETH(ooga) )                                               \n"); 

		initializeScanner(writer.toString());

		validateToken(IToken.tLPAREN);
		validateToken(IToken.tLPAREN);
		validateToken(IToken.tLPAREN);
		validateIdentifier("foo"); 
		validateToken(IToken.tLPAREN);
		validateToken(IToken.tLPAREN);
		validateToken(IToken.tLPAREN);
		validateIdentifier("ooga"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tCOMMA);
		validateToken(IToken.tLPAREN);
		validateIdentifier("boo"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tRPAREN);
	}

	public void testBug75956() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define ROPE( name ) name##Alloc\n"); 
		writer.write("#define _C 040                  \n"); 
		writer.write("ROPE( _C )                      \n"); 

		initializeScanner(writer.toString());
		validateIdentifier("_CAlloc"); 
		validateEOF();

		writer = new StringWriter();
		writer.write("#define ROPE( name ) Alloc ## name \n"); 
		writer.write("#define _C 040                     \n"); 
		writer.write("ROPE( _C )                         \n"); 

		initializeScanner(writer.toString());
		validateIdentifier("Alloc_C"); 
		validateEOF();

		writer = new StringWriter();
		writer.write("#define ROPE( name ) name##Alloc\n"); 
		writer.write("#define _C 040                  \n"); 
		writer.write("#define _CAlloc ooga            \n"); 
		writer.write("ROPE( _C )                      \n"); 

		initializeScanner(writer.toString());
		validateIdentifier("ooga"); 
		validateEOF();
	}

	public void testUnExpandedFunctionMacros() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define ETH(x) x  \n"); 
		writer.write("#define E ETH     \n"); 
		writer.write("ETH( c ), ETH, E; \n"); 
		initializeScanner(writer.toString());
		validateIdentifier("c"); 
		validateToken(IToken.tCOMMA);
		validateIdentifier("ETH"); 
		validateToken(IToken.tCOMMA);
		validateIdentifier("ETH"); 
		validateToken(IToken.tSEMI);
		validateEOF();
	}

	public void testBug79490A() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define TEST 'n'\n"); 
		writer.write("#if TEST == 'y'\n"); 
		writer.write("#define TRUE 1\n"); 
		writer.write("#else\n"); 
		writer.write("#define FALSE 1\n"); 
		writer.write("#endif\n"); 
		initializeScanner(writer.toString());
		validateEOF();
		validateDefinition("TEST", "'n'");  
		validateDefinition("FALSE", "1");  
	}

	public void testBug79490B() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define TEST 'y'\n"); 
		writer.write("#if TEST == 'y'\n"); 
		writer.write("#define TRUE 1\n"); 
		writer.write("#else\n"); 
		writer.write("#define FALSE 1\n"); 
		writer.write("#endif\n"); 
		initializeScanner(writer.toString());
		validateEOF();
		validateDefinition("TEST", "'y'");  
		validateDefinition("TRUE", "1");  
	}

	public void testBug102568A() throws Exception {
		initializeScanner("///*\r\nint x;\r\n"); 
		validateToken(IToken.t_int);
		validateIdentifier("x"); 
		validateToken(IToken.tSEMI);
		validateEOF();
	}

	public void testBug102568B() throws Exception {
		initializeScanner("// bla some thing /* ... \r\nint x;\r\n"); 
		validateToken(IToken.t_int);
		validateIdentifier("x"); 
		validateToken(IToken.tSEMI);
		validateEOF();
	}

	public void testbug84270() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define h g( ~\n"); 
		writer.write("#define g f\n"); 
		writer.write("#define f(a) f(x * (a))\n"); 
		writer.write("h 5) \n"); 
		initializeScanner(writer.toString());
		fullyTokenize();
	}

	public void testBug107150() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define FUNC_PROTOTYPE_PARAMS(list)    list\r\n"); 
		writer.write("int func2 FUNC_PROTOTYPE_PARAMS\r\n"); 
		writer.write("((int arg1)){\r\n"); 
		writer.write("    return 0;\r\n"); 
		writer.write("}\r\n"); 
		initializeScanner(writer.toString());
		validateToken(IToken.t_int);
		validateIdentifier("func2"); 
		validateToken(IToken.tLPAREN);
		validateToken(IToken.t_int);
		validateIdentifier("arg1"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tLBRACE);
		validateToken(IToken.t_return);
		validateInteger("0"); 
		validateToken(IToken.tSEMI);
		validateToken(IToken.tRBRACE);
		validateEOF();

		writer = new StringWriter();
		writer.write("#define FUNC_PROTOTYPE_PARAMS(list)    list\n"); 
		writer.write("int func2 FUNC_PROTOTYPE_PARAMS\n"); 
		writer.write("((int arg1)){\n"); 
		writer.write("    return 0;\n"); 
		writer.write("}\n"); 
		initializeScanner(writer.toString());
		validateToken(IToken.t_int);
		validateIdentifier("func2"); 
		validateToken(IToken.tLPAREN);
		validateToken(IToken.t_int);
		validateIdentifier("arg1"); 
		validateToken(IToken.tRPAREN);
		validateToken(IToken.tLBRACE);
		validateToken(IToken.t_return);
		validateInteger("0"); 
		validateToken(IToken.tSEMI);
		validateToken(IToken.tRBRACE);
		validateEOF();
	}

	public void testBug126136() throws Exception {
		StringBuffer buffer = new StringBuffer("#define C C\n"); 
		buffer.append("#if !C\n"); 
		buffer.append("true\n"); 
		buffer.append("#endif\n"); 
		initializeScanner(buffer.toString(), ParserLanguage.CPP);
		fullyTokenize();
	}

	public void testBug156137() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#if (3 % 2 == 1)                              \n");
		buffer.append("C                                             \n");
		buffer.append("#endif                                        \n");

		initializeScanner(buffer.toString());
		validateIdentifier("C");
		validateEOF();
	}

	public void testBug162214() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#ifdef xxx  // is not defined            \n");
		buffer.append("A                                        \n");
		buffer.append("#endif                                   \n");
		buffer.append("B                                        \n");

		initializeScanner(buffer.toString());
		validateIdentifier("B");
		validateEOF();

		buffer.setLength(0);
		buffer.append("#ifdef xxx  //* is not defined           \n");
		buffer.append("A                                        \n");
		buffer.append("#endif                                   \n");
		buffer.append("B                                        \n");

		initializeScanner(buffer.toString());
		validateIdentifier("B");
		validateEOF();
	}

	public void testBug156988() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define a        			\n");
		buffer.append("#define b \"      		\n");
		buffer.append("#define c <     			\n");
		buffer.append("#define d \"\"   			\n");
		buffer.append("#define e <>      		\n");
		buffer.append("#define f f     			\n");
		buffer.append("#define g gg     			\n");
		buffer.append("#include a                \n");
		buffer.append("#include b                \n");
		buffer.append("#include c                \n");
		buffer.append("#include d                \n");
		buffer.append("#include e                \n");
		buffer.append("#include f                \n");
		buffer.append("#include g                \n");
		buffer.append("A			                \n");

		initializeScanner(buffer.toString());
		validateIdentifier("A");
		validateEOF();
	}

	public void testBug156988_1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define a(x) x  			\n");
		buffer.append("#define b \"      		\n");
		buffer.append("#define c <     			\n");
		buffer.append("#define d \"\"   			\n");
		buffer.append("#define e <>      		\n");
		buffer.append("#define f f     			\n");
		buffer.append("#define g gg     			\n");
		buffer.append("#include a()              \n");
		buffer.append("#include a(<)             \n");
		buffer.append("#include a(\"\")          \n");
		buffer.append("#include a(<>)            \n");
		buffer.append("#include a(f)             \n");
		buffer.append("#include a(gg)            \n");
		buffer.append("#include a(g\\\ng)        \n");
		buffer.append("A				            \n");

		initializeScanner(buffer.toString());
		validateIdentifier("A");
		validateEOF();
	}

	public void testBug162410() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#pragma message (\"test\") \n");
		buffer.append("a       		             \n");
		initializeScanner(buffer.toString());
		validateIdentifier("a");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=180172
	public void testBug180172() throws Exception {
		StringBuffer buffer = new StringBuffer();
		String value = "\"https://bugs.eclipse.org/bugs/show_bug.cgi?id=180172\"";
		buffer.append("#define bug180172 ").append(value).append(" // bla \n");
		initializeScanner(buffer.toString());
		fullyTokenize();
		validateDefinition("bug180172", value);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=182180
	public void testBug182180_1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#ifdef _bug_182180_\n").append(
				"printf(\"Hello World /*.ap\\n\");\n").append("#endif\n")
				.append("bug182180\n");
		initializeScanner(buffer.toString());
		validateIdentifier("bug182180");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=182180
	public void testBug182180_2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#ifdef _bug_182180_\n").append(
				"char c='\"'; printf(\"Hello World /*.ap\\n\");\n").append(
				"#endif\n").append("bug182180\n");
		initializeScanner(buffer.toString());
		validateIdentifier("bug182180");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=182180
	public void testBug182180_3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#ifdef _bug_182180_\n").append(
				"char c1='\\'',c2='\\\"'; printf(\"Hello World /*.ap\\n\");\n")
				.append("#endif\n").append("bug182180\n");
		initializeScanner(buffer.toString());
		validateIdentifier("bug182180");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=182180
	public void testBug182180_4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#ifdef _bug_182180_\n").append(
				"printf(\"Hello '\"'World /*.ap\\n\");\n").append("#endif\n")
				.append("bug182180\n");
		initializeScanner(buffer.toString());
		validateIdentifier("bug182180");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=182180
	public void testBug182180_5() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#ifdef _bug_182180_\n").append(
				"printf(\"Hello \\\"World /*.ap\\n\");\n").append("#endif\n")
				.append("bug182180\n");
		initializeScanner(buffer.toString());
		validateIdentifier("bug182180");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200830
	public void testBug200830_1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define string BROKEN\r\n");
		buffer.append("#define macro(inst) (char*)inst\r\n");
		buffer.append("macro(\"string\");\r\n");
		initializeScanner(buffer.toString());
		validateToken(IToken.tLPAREN);
		validateToken(IToken.t_char);
		validateToken(IToken.tSTAR);
		validateToken(IToken.tRPAREN);
		validateString("string");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200830
	public void testBug200830_2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define string BROKEN\r\n");
		buffer.append("#define macro(inst) (char*)inst\r\n");
		buffer.append("macro(\" string \");\r\n");
		initializeScanner(buffer.toString());
		validateToken(IToken.tLPAREN);
		validateToken(IToken.t_char);
		validateToken(IToken.tSTAR);
		validateToken(IToken.tRPAREN);
		validateString(" string ");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200830
	public void testBug200830_3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define string BROKEN\r\n");
		buffer.append("#define macro(inst) (char*)inst\r\n");
		buffer.append("macro(\"\\\"string \");\r\n");
		initializeScanner(buffer.toString());
		validateToken(IToken.tLPAREN);
		validateToken(IToken.t_char);
		validateToken(IToken.tSTAR);
		validateToken(IToken.tRPAREN);
		validateString("\\\"string ");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=200830
	public void testBug200830_4() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define s B\r\n");
		buffer.append("#define macro(inst) (char*)inst\r\n");
		buffer.append("macro('s');\r\n");
		initializeScanner(buffer.toString());
		validateToken(IToken.tLPAREN);
		validateToken(IToken.t_char);
		validateToken(IToken.tSTAR);
		validateToken(IToken.tRPAREN);
		validateChar("s");
	}

	public void testBug185120_1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define TEST_DEFINE 1UL\n");
		buffer.append("#if TEST_DEFINE != 1UL\n");
		buffer.append("-\n");
		buffer.append("#else\n");
		buffer.append("+\n");
		buffer.append("#endif\n");
		initializeScanner(buffer.toString());
		validateToken(IToken.tPLUS);
	}

	public void testBug185120_2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define TEST_DEFINE 1LLU\n");
		buffer.append("#if TEST_DEFINE != 1ULL\n");
		buffer.append("-\n");
		buffer.append("#else\n");
		buffer.append("+\n");
		buffer.append("#endif\n");
		initializeScanner(buffer.toString());
		validateToken(IToken.tPLUS);
	}
	
    public void testBug39698() throws Exception	{
    	initializeScanner( "<? >?"); 
    	validateToken( IGCCToken.tMIN );
    	validateToken( IGCCToken.tMAX );
    	validateEOF();
	}

    public void test__attribute__() throws Exception {
    	initializeScanner(
    			"#define __cdecl __attribute__((cdecl))\n" + 
				"__cdecl;"); 
    	validateToken(IGCCToken.t__attribute__);
    	validateToken(IToken.tLPAREN);
    	validateToken(IToken.tLPAREN);
    	validateToken(IToken.tIDENTIFIER);
    	validateToken(IToken.tRPAREN);
    	validateToken(IToken.tRPAREN);
    	validateToken(IToken.tSEMI);
    	validateEOF();
	}
        
    public void testImaginary() throws Exception {
        initializeScanner( "3i", ParserLanguage.C ); 
        validateInteger( "3i" ); 
        validateEOF();
    }
}
