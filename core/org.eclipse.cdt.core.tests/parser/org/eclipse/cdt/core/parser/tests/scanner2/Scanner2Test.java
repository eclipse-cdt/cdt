/*******************************************************************************
 * Copyright (c) 2003,2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.core.parser.tests.scanner2;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerException;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.internal.core.parser.QuickParseCallback;

/**
 * @author jcamelon
 */
public class Scanner2Test extends BaseScanner2Test
{
	public class TableRow
	{
		private int[] values;
		private int length;

		public TableRow(int[] v)
		{
			length= v.length;
			values= new int[length];
			System.arraycopy(v, 0, values, 0, length);
		}

		public String toString()
		{
			StringBuffer s= new StringBuffer();
			for (int i= 0; i < length; ++i)
			{
				s.append("var").append(i).append("=").append(values[i]).append(" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			return s.toString();
		}

		public String symbolName(int index)
		{
			return "DEFINITION" + index; //$NON-NLS-1$
		}

		public int symbolValue(int index)
		{
			return new Long(Math.round(Math.pow(index, index))).intValue();
		}

		public String generateCode()
		{
			if (length < 2)
			{
				return "Array must have at least 2 elements"; //$NON-NLS-1$
			}
			int numberOfElsifs= length - 1;
			StringBuffer buffer= new StringBuffer();
			buffer.append("#if ").append(values[0]).append("\n#\tdefine "); //$NON-NLS-1$ //$NON-NLS-2$
			buffer.append(symbolName(0)).append(" ").append(symbolValue(0)); //$NON-NLS-1$
			for (int i= 0; i < numberOfElsifs; ++i)
				buffer
					.append("\n#elif ") //$NON-NLS-1$
					.append(values[1 + i])
					.append("\n#\tdefine ") //$NON-NLS-1$
					.append(symbolName(i + 1))
					.append(" ") //$NON-NLS-1$
					.append(symbolValue(i + 1));
			buffer
				.append("\n#else \n#\tdefine ") //$NON-NLS-1$
				.append(symbolName(length))
				.append(" ") //$NON-NLS-1$
				.append(symbolValue(length))
				.append("\n#endif"); //$NON-NLS-1$
			return buffer.toString();
		}

		public int selectWinner()
		{
			for (int i= 0; i < values.length; ++i)
			{
				if (values[i] != 0)
				{
					return i;
				}
			}
			return length;
		}
		/**
		 * Returns the length.
		 * @return int
		 */
		public int getLength()
		{
			return length;
		}

	}

	public class TruthTable
	{
		private int numberOfVariables;
		private int numberOfRows;
		public TableRow[] rows;

		public TruthTable(int n)
		{
			numberOfVariables= n;
			numberOfRows= new Long(Math.round(Math.pow(2, n))).intValue();

			rows= new TableRow[numberOfRows];
			for (int i= 0; i < numberOfRows; ++i)
			{
				String Z= Integer.toBinaryString(i);

				int[] input= new int[numberOfVariables];
				for (int j= 0; j < numberOfVariables; ++j)
				{
					int padding= numberOfVariables - Z.length();
					int k= 0;
					for (; k < padding; ++k)
					{
						input[k]= 0;
					}
					for (int l= 0; l < Z.length(); ++l)
					{
						char c= Z.charAt(l);
						int value= Character.digit(c, 10);
						input[k++]= value;
					}
				}
				rows[i]= new TableRow(input);
			}
		}
		/**
		 * Returns the numberOfRows.
		 * @return int
		 */
		public int getNumberOfRows()
		{
			return numberOfRows;
		}

	}
	
	public final static int SIZEOF_TRUTHTABLE = 10; 


	public void testWeirdStrings() throws Exception
	{
		try
		{
			initializeScanner( "Living Life L\"LONG\""); //$NON-NLS-1$
			validateIdentifier( "Living" ); //$NON-NLS-1$
			validateIdentifier( "Life" ); //$NON-NLS-1$
			validateString("LONG", true); //$NON-NLS-1$
			validateEOF();
		}
		catch( ScannerException se )
		{
			fail(EXCEPTION_THROWN + se.toString());
		}
		
	}
	
	
	public void testNumerics()throws Exception
	{
		try
		{
			initializeScanner("3.0 0.9 .5 3. 4E5 2.01E-03 ..."); //$NON-NLS-1$
			validateFloatingPointLiteral( "3.0"); //$NON-NLS-1$
			validateFloatingPointLiteral( "0.9"); //$NON-NLS-1$
			validateFloatingPointLiteral( ".5"); //$NON-NLS-1$
			validateFloatingPointLiteral( "3.");  //$NON-NLS-1$
			validateFloatingPointLiteral( "4E5"); //$NON-NLS-1$
			validateFloatingPointLiteral( "2.01E-03" ); //$NON-NLS-1$
			validateToken( IToken.tELLIPSIS );
			validateEOF();
		}
		catch( ScannerException se )
		{
			fail(EXCEPTION_THROWN + se.toString());
		}
		
	}
	

	/**
	 * Constructor for ScannerTestCase.
	 * @param name
	 */
	public Scanner2Test(String name)
	{
		super(name);
	}

	public void testPreprocessorDefines()throws Exception
	{
		initializeScanner("#define SIMPLE_NUMERIC 5\nint x = SIMPLE_NUMERIC"); //$NON-NLS-1$
		validateToken(IToken.t_int);
		validateDefinition("SIMPLE_NUMERIC", "5"); //$NON-NLS-1$ //$NON-NLS-2$
		validateIdentifier("x"); //$NON-NLS-1$
		validateToken(IToken.tASSIGN);
		validateInteger("5"); //$NON-NLS-1$
		validateEOF();

		initializeScanner("#define SIMPLE_STRING \"This is a simple string.\"\n\nconst char * myVariable = SIMPLE_STRING;"); //$NON-NLS-1$
		validateToken(IToken.t_const);
		validateDefinition("SIMPLE_STRING", "\"This is a simple string.\""); //$NON-NLS-1$ //$NON-NLS-2$
		validateToken(IToken.t_char);
		validateToken(IToken.tSTAR);
		validateIdentifier("myVariable"); //$NON-NLS-1$
		validateToken(IToken.tASSIGN);
		validateString("This is a simple string."); //$NON-NLS-1$
		validateToken(IToken.tSEMI);
		validateEOF();

		initializeScanner("#define FOOL 5  \n int tryAFOOL = FOOL + FOOL;"); //$NON-NLS-1$
		validateToken(IToken.t_int);
		validateIdentifier("tryAFOOL"); //$NON-NLS-1$
		validateToken(IToken.tASSIGN);
		validateInteger("5"); //$NON-NLS-1$
		validateToken(IToken.tPLUS);
		validateInteger("5"); //$NON-NLS-1$
		validateToken(IToken.tSEMI);
		validateEOF();

		initializeScanner("#define FOOL 5  \n int FOOLer = FOOL;"); //$NON-NLS-1$
		validateToken(IToken.t_int);
		validateIdentifier("FOOLer"); //$NON-NLS-1$
		validateToken(IToken.tASSIGN);
		validateInteger("5"); //$NON-NLS-1$
		validateToken(IToken.tSEMI);
		validateEOF();

		// the case we were failing against in ctype.h
		// this is a definition, not a macro!
		initializeScanner("#define _ALPHA (0x0100|_UPPER|_LOWER)"); //$NON-NLS-1$
		validateEOF();
		validateDefinition("_ALPHA", "(0x0100|_UPPER|_LOWER)"); //$NON-NLS-1$ //$NON-NLS-2$

		// test for comments after the macro
		initializeScanner("#define NO_COMMENT// ignore me"); //$NON-NLS-1$
		validateEOF();
		validateDefinition("NO_COMMENT", ""); //$NON-NLS-1$ //$NON-NLS-2$

		initializeScanner("#define NO_COMMENT/* ignore me*/"); //$NON-NLS-1$
		validateEOF();
		validateDefinition("NO_COMMENT", ""); //$NON-NLS-1$ //$NON-NLS-2$

		initializeScanner("#define ANSWER 42 // i think"); //$NON-NLS-1$
		validateEOF();
		validateDefinition("ANSWER", "42"); //$NON-NLS-1$ //$NON-NLS-2$

		initializeScanner("#define ANSWER 42 /* i think */"); //$NON-NLS-1$
		validateEOF();
		validateDefinition("ANSWER", "42"); //$NON-NLS-1$ //$NON-NLS-2$

		initializeScanner("#define MULTILINE 3 /* comment \n that goes more than one line */"); //$NON-NLS-1$
		validateEOF();
		validateDefinition("MULTILINE", "3"); //$NON-NLS-1$ //$NON-NLS-2$

		initializeScanner("#define MULTICOMMENT X /* comment1 */ + Y /* comment 2 */"); //$NON-NLS-1$
		validateEOF();
		validateDefinition("MULTICOMMENT", "X  + Y"); //$NON-NLS-1$ //$NON-NLS-2$

		initializeScanner("#define SIMPLE_STRING This is a simple string.\n"); //$NON-NLS-1$
		validateEOF();
		validateDefinition(
				"SIMPLE_STRING", //$NON-NLS-1$
				"This is a simple string."); //$NON-NLS-1$

		initializeScanner("#	define SIMPLE_NUMERIC 5\n"); //$NON-NLS-1$
		validateEOF();
		validateDefinition("SIMPLE_NUMERIC", "5"); //$NON-NLS-1$ //$NON-NLS-2$

		initializeScanner("#	define		SIMPLE_NUMERIC   	5\n"); //$NON-NLS-1$
		validateEOF();
		validateDefinition("SIMPLE_NUMERIC", "5"); //$NON-NLS-1$ //$NON-NLS-2$

		initializeScanner("#define 		SIMPLE_STRING \"This 	is a simple     string.\"\n"); //$NON-NLS-1$
		validateEOF();
		validateDefinition(
				"SIMPLE_STRING", //$NON-NLS-1$
				"\"This 	is a simple     string.\""); //$NON-NLS-1$

		initializeScanner("#define SIMPLE_STRING 	  	This 	is a simple 	string.\n"); //$NON-NLS-1$
		validateEOF();
		validateDefinition(
				"SIMPLE_STRING", //$NON-NLS-1$
				"This 	is a simple 	string."); //$NON-NLS-1$

		initializeScanner("#define FLAKE\n\nFLAKE"); //$NON-NLS-1$
		validateEOF();
		validateDefinition("FLAKE", ""); //$NON-NLS-1$ //$NON-NLS-2$

		initializeScanner("#define SIMPLE_STRING 	  	This 	is a simple 	string.\\\n		Continue please."); //$NON-NLS-1$
		validateEOF();
		validateDefinition(
				"SIMPLE_STRING", //$NON-NLS-1$
				"This 	is a simple 	string.		Continue please."); //$NON-NLS-1$
	}
	public void testBug67834() throws Exception {
		initializeScanner(
				"#if ! BAR\n" + //$NON-NLS-1$
				"foo\n" + //$NON-NLS-1$
				"#else\n" + //$NON-NLS-1$
				"bar\n" + //$NON-NLS-1$
				"#endif\n"  //$NON-NLS-1$
				); //$NON-NLS-1$
		validateIdentifier("foo"); //$NON-NLS-1$
		validateEOF();
		validateBalance();
		
		initializeScanner(
				"#if ! (BAR)\n" + //$NON-NLS-1$
				"foo\n" + //$NON-NLS-1$
				"#else\n" + //$NON-NLS-1$
				"bar\n" + //$NON-NLS-1$
				"#endif\n"  //$NON-NLS-1$
				); //$NON-NLS-1$
		validateIdentifier("foo"); //$NON-NLS-1$
		validateEOF();
		validateBalance();
	}

	public void testConcatenation()
	{
		try
		{
			initializeScanner("#define F1 3\n#define F2 F1##F1\nint x=F2;"); //$NON-NLS-1$
			validateToken(IToken.t_int);
			validateDefinition("F1", "3"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition( "F2", "F1##F1"); //$NON-NLS-1$ //$NON-NLS-2$
			validateIdentifier("x"); //$NON-NLS-1$
			validateToken(IToken.tASSIGN);
			validateInteger("33"); //$NON-NLS-1$
			validateToken(IToken.tSEMI);
			validateEOF();
			
			initializeScanner("#define PREFIX RT_\n#define RUN PREFIX##Run");  //$NON-NLS-1$
			validateEOF(); 
			validateDefinition( "PREFIX", "RT_" );  //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition( "RUN", "PREFIX##Run" ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}
		
		try
		{
			initializeScanner( "#define DECLARE_HANDLE(name) struct name##__ { int unused; }; typedef struct name##__ *name\n DECLARE_HANDLE( joe )" ); //$NON-NLS-1$
			validateToken( IToken.t_struct );
			validateIdentifier( "joe__");  //$NON-NLS-1$
			validateToken( IToken.tLBRACE);  
			validateToken( IToken.t_int ); 
			validateIdentifier( "unused");  //$NON-NLS-1$
			validateToken( IToken.tSEMI ); 
			validateToken( IToken.tRBRACE );
			validateToken( IToken.tSEMI ); 
			validateToken( IToken.t_typedef ); 
			validateToken( IToken.t_struct ); 
			validateIdentifier( "joe__" );  //$NON-NLS-1$
			validateToken( IToken.tSTAR ); 
			validateIdentifier( "joe");   //$NON-NLS-1$
			validateEOF();
		}
		catch( Exception e )
		{ 
			fail(EXCEPTION_THROWN + e.toString());			
		}
	}

	public void testSimpleIfdef()
	{
		try
		{

			initializeScanner("#define SYMBOL 5\n#ifdef SYMBOL\nint counter(SYMBOL);\n#endif"); //$NON-NLS-1$
			validateToken(IToken.t_int);
			validateIdentifier("counter"); //$NON-NLS-1$
			validateToken(IToken.tLPAREN);
			validateInteger("5"); //$NON-NLS-1$
			validateToken(IToken.tRPAREN);
			validateToken(IToken.tSEMI);
			validateEOF();

			initializeScanner("#define SYMBOL 5\n#ifndef SYMBOL\nint counter(SYMBOL);\n#endif"); //$NON-NLS-1$
			validateEOF();

			initializeScanner("#ifndef DEFINED\n#define DEFINED 100\n#endif\nint count = DEFINED;"); //$NON-NLS-1$
			validateToken(IToken.t_int);
			validateDefinition("DEFINED", "100"); //$NON-NLS-1$ //$NON-NLS-2$

			validateIdentifier("count"); //$NON-NLS-1$
			validateToken(IToken.tASSIGN);
			validateInteger("100"); //$NON-NLS-1$
			validateToken(IToken.tSEMI);
			validateEOF();

			initializeScanner("#ifndef DEFINED\n#define DEFINED 100\n#endif\nint count = DEFINED;"); //$NON-NLS-1$
			scanner.addDefinition("DEFINED", "101"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("DEFINED", "101"); //$NON-NLS-1$ //$NON-NLS-2$
			validateToken(IToken.t_int);
			validateIdentifier("count"); //$NON-NLS-1$
			validateToken(IToken.tASSIGN);
			validateInteger("101"); //$NON-NLS-1$
			validateToken(IToken.tSEMI);
			validateEOF();
			
			initializeScanner( "/* NB: This is #if 0'd out */");  //$NON-NLS-1$
			validateEOF(); 

		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}
	}

	public void testMultipleLines() throws Exception
	{
		Writer code = new StringWriter(); 
		code.write( "#define COMPLEX_MACRO 33 \\\n"); //$NON-NLS-1$
		code.write( "	+ 44\n\nCOMPLEX_MACRO"); //$NON-NLS-1$
		initializeScanner( code.toString() );
		validateInteger( "33" ); //$NON-NLS-1$
		validateToken( IToken.tPLUS );
		validateInteger( "44" ); //$NON-NLS-1$
	}

	public void testSlightlyComplexIfdefStructure()
	{
		try
		{
			initializeScanner("#ifndef BASE\n#define BASE 10\n#endif\n#ifndef BASE\n#error BASE is defined\n#endif"); //$NON-NLS-1$
			validateEOF();
			validateBalance();
		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}

		try
		{
			initializeScanner("#ifndef ONE\n#define ONE 1\n#ifdef TWO\n#define THREE ONE + TWO\n#endif\n#endif\nint three(THREE);"); //$NON-NLS-1$

			validateToken(IToken.t_int);
			validateDefinition("ONE", "1"); //$NON-NLS-1$ //$NON-NLS-2$
			validateAsUndefined("TWO"); //$NON-NLS-1$
			validateAsUndefined("THREE"); //$NON-NLS-1$
			validateIdentifier("three"); //$NON-NLS-1$
			validateToken(IToken.tLPAREN);
			validateIdentifier("THREE"); //$NON-NLS-1$
			validateToken(IToken.tRPAREN);
			validateToken(IToken.tSEMI);
			validateEOF();
			validateBalance();

			initializeScanner("#ifndef ONE\n#define ONE 1\n#ifdef TWO\n#define THREE ONE + TWO\n#endif\n#endif\nint three(THREE);"); //$NON-NLS-1$
			scanner.addDefinition("TWO", "2"); //$NON-NLS-1$ //$NON-NLS-2$
			validateToken(IToken.t_int);
			validateDefinition("ONE", "1"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("TWO", "2"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("THREE", "ONE + TWO"); //$NON-NLS-1$ //$NON-NLS-2$

			validateIdentifier("three"); //$NON-NLS-1$
			validateToken(IToken.tLPAREN);
			validateInteger("1"); //$NON-NLS-1$
			validateToken(IToken.tPLUS);
			validateInteger("2"); //$NON-NLS-1$
			validateToken(IToken.tRPAREN);
			validateToken(IToken.tSEMI);
			validateEOF();
			validateBalance();

			initializeScanner("#ifndef FOO\n#define FOO 4\n#else\n#undef FOO\n#define FOO 6\n#endif"); //$NON-NLS-1$
			validateEOF();
			validateBalance();
			validateDefinition("FOO", "4"); //$NON-NLS-1$ //$NON-NLS-2$

			initializeScanner("#ifndef FOO\n#define FOO 4\n#else\n#undef FOO\n#define FOO 6\n#endif"); //$NON-NLS-1$
			scanner.addDefinition("FOO", "2"); //$NON-NLS-1$ //$NON-NLS-2$
			validateEOF();
			validateBalance();
			validateDefinition("FOO", "6"); //$NON-NLS-1$ //$NON-NLS-2$

			initializeScanner("#ifndef ONE\n#   define ONE 1\n#   ifndef TWO\n#       define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#else\n#   ifndef TWO\n#      define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#endif\n"); //$NON-NLS-1$
			validateEOF();
			validateBalance();
			validateDefinition("ONE", "1"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("TWO", "ONE + ONE"); //$NON-NLS-1$ //$NON-NLS-2$

			initializeScanner(
					"#ifndef ONE\r\n" + //$NON-NLS-1$
					"#   define ONE 1\n" +  //$NON-NLS-1$
					"#   ifndef TWO\n" +  //$NON-NLS-1$
					"#       define TWO ONE + ONE \n" + //$NON-NLS-1$
					"#   else\n" +  //$NON-NLS-1$
					"#       undef TWO\n" + //$NON-NLS-1$
					"#       define TWO 2 \n" +  //$NON-NLS-1$
					"#   endif\n" +  //$NON-NLS-1$
					"#else\n" +  //$NON-NLS-1$
					"#   ifndef TWO\n" +  //$NON-NLS-1$
					"#      define TWO ONE + ONE \n" +  //$NON-NLS-1$
					"#   else\n" +  //$NON-NLS-1$
					"#       undef TWO\n" +  //$NON-NLS-1$
					"#       define TWO 2 \n" +  //$NON-NLS-1$
					"#   endif\n" +  //$NON-NLS-1$
					"#endif\n"); //$NON-NLS-1$" +

			scanner.addDefinition("ONE", "one"); //$NON-NLS-1$ //$NON-NLS-2$
			validateEOF();
			validateBalance();
			validateDefinition("ONE", "one"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("TWO", "ONE + ONE"); //$NON-NLS-1$ //$NON-NLS-2$

			initializeScanner("#ifndef ONE\n#   define ONE 1\n#   ifndef TWO\n#       define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#else\n#   ifndef TWO\n#      define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#endif\n"); //$NON-NLS-1$
			scanner.addDefinition("ONE", "one"); //$NON-NLS-1$ //$NON-NLS-2$
			scanner.addDefinition("TWO", "two"); //$NON-NLS-1$ //$NON-NLS-2$
			validateEOF();
			validateBalance();

			validateDefinition("ONE", "one"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("TWO", "2"); //$NON-NLS-1$ //$NON-NLS-2$

			initializeScanner("#ifndef ONE\n#   define ONE 1\n#   ifndef TWO\n#       define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#else\n#   ifndef TWO\n#      define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#endif\n"); //$NON-NLS-1$
			scanner.addDefinition("TWO", "two"); //$NON-NLS-1$ //$NON-NLS-2$
			validateEOF();
			validateBalance();

			validateDefinition("ONE", "1"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("TWO", "2"); //$NON-NLS-1$ //$NON-NLS-2$

		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}
	}

	public void testIfs()
	{
		try
		{
			initializeScanner("#if 0\n#error NEVER\n#endif\n"); //$NON-NLS-1$
			validateEOF();
			validateBalance();

			initializeScanner("#define X 5\n#define Y 7\n#if (X < Y)\n#define Z X + Y\n#endif"); //$NON-NLS-1$
			validateEOF();
			validateBalance();
			validateDefinition("X", "5"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("Y", "7"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("Z", "X + Y"); //$NON-NLS-1$ //$NON-NLS-2$

			initializeScanner("#if T < 20\n#define Z T + 1\n#endif"); //$NON-NLS-1$
			scanner.addDefinition("X", "5"); //$NON-NLS-1$ //$NON-NLS-2$
			scanner.addDefinition("Y", "7"); //$NON-NLS-1$ //$NON-NLS-2$
			scanner.addDefinition("T", "X + Y"); //$NON-NLS-1$ //$NON-NLS-2$
			validateEOF();
			validateBalance();
			validateDefinition("X", "5"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("Y", "7"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("T", "X + Y"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("Z", "T + 1"); //$NON-NLS-1$ //$NON-NLS-2$

		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}

		try
		{
			initializeScanner("#if ( 10 / 5 ) != 2\n#error 10/5 seems to not equal 2 anymore\n#endif\n"); //$NON-NLS-1$
			validateEOF();
			validateBalance();
		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}

		try
		{
			initializeScanner(
					"#ifndef FIVE \n" + //$NON-NLS-1$
					"#define FIVE 5\n" + //$NON-NLS-1$
					"#endif \n" + //$NON-NLS-1$
					"#ifndef TEN\n" + //$NON-NLS-1$
					"#define TEN 2 * FIVE\n" + //$NON-NLS-1$
					"#endif\n" + //$NON-NLS-1$
					"#if TEN != 10\n" + //$NON-NLS-1$
					"#define MISTAKE 1\n" + //$NON-NLS-1$
					"#error Five does not equal 10\n" + //$NON-NLS-1$
					"#endif\n"); //$NON-NLS-1$
			scanner.addDefinition("FIVE", "55"); //$NON-NLS-1$ //$NON-NLS-2$
			validateEOF();
			fail(EXPECTED_FAILURE);
		}
		catch (ScannerException se)
		{
			validateBalance(1);
			validateDefinition("FIVE", "55"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("TEN", "2 * FIVE"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("MISTAKE", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}

		try
		{
			initializeScanner("#if ((( FOUR / TWO ) * THREE )< FIVE )\n#error 6 is not less than 5 \n#endif\n#if ( ( FIVE * ONE ) != (( (FOUR) + ONE ) * ONE ) )\n#error 5 should equal 5\n#endif \n"); //$NON-NLS-1$

			scanner.addDefinition("ONE", "1"); //$NON-NLS-1$ //$NON-NLS-2$
			scanner.addDefinition("TWO", "(ONE + ONE)"); //$NON-NLS-1$ //$NON-NLS-2$
			scanner.addDefinition("THREE", "(TWO + ONE)"); //$NON-NLS-1$ //$NON-NLS-2$
			scanner.addDefinition("FOUR", "(TWO * TWO)"); //$NON-NLS-1$ //$NON-NLS-2$
			scanner.addDefinition("FIVE", "(THREE + TWO)"); //$NON-NLS-1$ //$NON-NLS-2$

			validateEOF();
			validateBalance();
			validateDefinition("ONE", "1"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("TWO", "(ONE + ONE)"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("THREE", "(TWO + ONE)"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("FOUR", "(TWO * TWO)"); //$NON-NLS-1$ //$NON-NLS-2$
			validateDefinition("FIVE", "(THREE + TWO)"); //$NON-NLS-1$ //$NON-NLS-2$

			TruthTable table= new TruthTable(SIZEOF_TRUTHTABLE);
			int numberOfRows= table.getNumberOfRows();
			TableRow[] rows= table.rows;

			for (int i= 0; i < numberOfRows; ++i)
			{
				TableRow row= rows[i];
				String code= row.generateCode();
				if (verbose)
					System.out.println("\n\nRow " + i + " has code\n" + code); //$NON-NLS-1$ //$NON-NLS-2$
				initializeScanner(code);
				validateEOF();
				validateBalance();
				validateAllDefinitions(row);
			}
		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}

		try
		{
			initializeScanner("#if ! 0\n#error Correct!\n#endif"); //$NON-NLS-1$
			scanner.nextToken();
			fail(EXPECTED_FAILURE);
		}
		catch (ScannerException se)
		{
			validateBalance(1);
			// TODO define problems
			//assertEquals( se.getProblem().getID(), IProblem.PREPROCESSOR_POUND_ERROR);
		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}
	}

	public void testPreprocessorMacros()
	{
		try
		{
			initializeScanner("#define GO(x) x+1\nint y(5);\ny = GO(y);"); //$NON-NLS-1$
			validateToken(IToken.t_int);
			validateIdentifier("y"); //$NON-NLS-1$
			validateToken(IToken.tLPAREN);
			validateInteger("5"); //$NON-NLS-1$
			validateToken(IToken.tRPAREN);
			validateToken(IToken.tSEMI);

			/* Macros don't work this way anymore
			IMacroDescriptor descriptor=
				scanner.getDefinition("GO"); //$NON-NLS-1$
			String [] parms= descriptor.getParameters();
			assertNotNull(parms);
			assertTrue(parms.length == 1);
			String parm1= parms[0];
			assertTrue(parm1.equals("x")); //$NON-NLS-1$
			IToken [] expansion= descriptor.getTokenizedExpansion();
			assertNotNull(parms);
			assertTrue(expansion.length == 3);
			assertTrue((expansion[0]).getType() == IToken.tIDENTIFIER);
			assertTrue((expansion[0]).getImage().equals("x")); //$NON-NLS-1$
			assertTrue((expansion[1]).getType() == IToken.tPLUS);
			assertTrue((expansion[2]).getType() == IToken.tINTEGER);
			assertTrue((expansion[2]).getImage().equals("1")); //$NON-NLS-1$ */

			validateIdentifier("y"); //$NON-NLS-1$
			validateToken(IToken.tASSIGN);
			validateIdentifier("y"); //$NON-NLS-1$
			validateToken(IToken.tPLUS);
			validateInteger("1"); //$NON-NLS-1$
			validateToken(IToken.tSEMI);
			validateEOF();
			validateBalance();

			initializeScanner(
				"#define ONE 1\n" //$NON-NLS-1$
					+ "#define SUM(a,b,c,d,e,f,g) ( a + b + c + d + e + f + g )\n" //$NON-NLS-1$
					+ "int daSum = SUM(ONE,3,5,7,9,11,13);"); //$NON-NLS-1$
			validateToken(IToken.t_int);
			validateIdentifier("daSum"); //$NON-NLS-1$
			validateToken(IToken.tASSIGN);
			validateToken(IToken.tLPAREN);
			validateInteger("1"); //$NON-NLS-1$
			validateToken(IToken.tPLUS);
			validateInteger("3"); //$NON-NLS-1$
			validateToken(IToken.tPLUS);
			validateInteger("5"); //$NON-NLS-1$
			validateToken(IToken.tPLUS);
			validateInteger("7"); //$NON-NLS-1$
			validateToken(IToken.tPLUS);
			validateInteger("9"); //$NON-NLS-1$
			validateToken(IToken.tPLUS);
			validateInteger("11"); //$NON-NLS-1$
			validateToken(IToken.tPLUS);
			validateInteger("13"); //$NON-NLS-1$
			validateToken(IToken.tRPAREN);
			validateToken(IToken.tSEMI);
			validateEOF();

			/*
			IMacroDescriptor macro= scanner.getDefinition("SUM"); //$NON-NLS-1$
			String [] params= macro.getParameters();
			assertNotNull(params);
			assertTrue(params.length == 7);

			IToken [] tokens= macro.getTokenizedExpansion();
			assertNotNull(tokens);
			assertTrue(tokens.length == 15); */

			initializeScanner("#define LOG( format, var1)   printf( format, var1 )\nLOG( \"My name is %s\", \"Bogdan\" );\n"); //$NON-NLS-1$
			validateIdentifier("printf"); //$NON-NLS-1$
			validateToken(IToken.tLPAREN);
			validateString("My name is %s"); //$NON-NLS-1$
			validateToken(IToken.tCOMMA);
			validateString("Bogdan"); //$NON-NLS-1$
			validateToken(IToken.tRPAREN);
			validateToken(IToken.tSEMI);
			validateEOF();

			initializeScanner("#define INCR( x )   ++x\nint y(2);\nINCR(y);"); //$NON-NLS-1$
			validateToken(IToken.t_int);
			validateIdentifier("y"); //$NON-NLS-1$
			validateToken(IToken.tLPAREN);
			validateInteger("2"); //$NON-NLS-1$
			validateToken(IToken.tRPAREN);
			validateToken(IToken.tSEMI);
			validateToken(IToken.tINCR);
			validateIdentifier("y"); //$NON-NLS-1$
			validateToken(IToken.tSEMI);
			validateEOF();

			initializeScanner("#define CHECK_AND_SET( x, y, z )     if( x ) { \\\n y = z; \\\n }\n\nCHECK_AND_SET( 1, balance, 5000 );\nCHECK_AND_SET( confused(), you, dumb );"); //$NON-NLS-1$
			validateToken(IToken.t_if);
			validateToken(IToken.tLPAREN);
			validateInteger("1"); //$NON-NLS-1$
			validateToken(IToken.tRPAREN);
			validateToken(IToken.tLBRACE);
			validateIdentifier("balance"); //$NON-NLS-1$
			validateToken(IToken.tASSIGN);
			validateInteger("5000"); //$NON-NLS-1$
			validateToken(IToken.tSEMI);
			validateToken(IToken.tRBRACE);
			validateToken(IToken.tSEMI);

			validateToken(IToken.t_if);
			validateToken(IToken.tLPAREN);
			validateIdentifier("confused"); //$NON-NLS-1$
			validateToken(IToken.tLPAREN);
			validateToken(IToken.tRPAREN);
			validateToken(IToken.tRPAREN);
			validateToken(IToken.tLBRACE);
			validateIdentifier("you"); //$NON-NLS-1$
			validateToken(IToken.tASSIGN);
			validateIdentifier("dumb"); //$NON-NLS-1$
			validateToken(IToken.tSEMI);
			validateToken(IToken.tRBRACE);
			validateToken(IToken.tSEMI);
			validateEOF();

			initializeScanner("#define ON 7\n#if defined(ON)\nint itsOn = ON;\n#endif"); //$NON-NLS-1$
			validateToken(IToken.t_int);
			validateBalance(1);
			validateIdentifier("itsOn"); //$NON-NLS-1$
			validateToken(IToken.tASSIGN);
			validateInteger("7"); //$NON-NLS-1$
			validateToken(IToken.tSEMI);
			validateEOF();
			validateBalance();

			initializeScanner("#if defined( NOTHING ) \nint x = NOTHING;\n#endif"); //$NON-NLS-1$
			validateEOF();
			validateBalance();
			
			
				
			

		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}
	}

	public void testQuickScan() throws ParserFactoryError
	{
		try
		{
			initializeScanner( "#if X + 5 < 7\n  int found = 1;\n#endif", ParserMode.QUICK_PARSE ); //$NON-NLS-1$
			validateToken( IToken.t_int ); 
			validateIdentifier( "found" );  //$NON-NLS-1$
			validateToken( IToken.tASSIGN ); 
			validateInteger( "1");  //$NON-NLS-1$
			validateToken( IToken.tSEMI );
			validateEOF(); 
			 	
		} 
		catch( ScannerException se )
		{
			fail( EXCEPTION_THROWN + se.getMessage() );
		}
		
		try
		{
			initializeScanner( "#if 0\n  int error = 666;\n#endif" );  //$NON-NLS-1$
			validateEOF(); 
		}
		catch( ScannerException se )
		{
			fail( EXCEPTION_THROWN + se.getMessage() );
		}
		
	}


	public void testOtherPreprocessorCommands() throws ParserFactoryError
	{
		try
		{
			initializeScanner("#\n#\t\n#define MAX_SIZE 1024\n#\n#  "); //$NON-NLS-1$
			validateEOF();
			validateDefinition("MAX_SIZE", "1024"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}

		for (int i= 0; i < 4; ++i)
		{
			switch (i)
			{
				case 0 :
					initializeScanner("#  ape"); //$NON-NLS-1$
					break;
				case 1 :
					initializeScanner("#  #"); //$NON-NLS-1$
					break;
				case 2 :
					initializeScanner("#  32"); //$NON-NLS-1$
					break;
				case 3 :
					initializeScanner("#  defines"); //$NON-NLS-1$
					break;
			}

			try
			{
				validateEOF();
				// These are no longer scanner exceptions, the are simply ignored.
				//fail(EXPECTED_FAILURE);
			}
			catch (ScannerException se)
			{
				validateBalance();
			}
			catch (Exception e)
			{
				fail(EXCEPTION_THROWN + e.toString());
			}
		}

	}

	public void validateAllDefinitions(TableRow row)
	{
		int winner= row.selectWinner();
		int rowLength= row.getLength();
		for (int i= 0; i <= rowLength; ++i)
		{
			if (i == winner)
				validateDefinition(row.symbolName(i), row.symbolValue(i));
			else
				validateAsUndefined(row.symbolName(i));
		}
	}
	
	public void testBug36287() throws Exception
	{
		initializeScanner( "X::X( const X & rtg_arg ) : U( rtg_arg ) , Z( rtg_arg.Z ) , er( rtg_arg.er ){}" ); //$NON-NLS-1$
		validateIdentifier("X"); //$NON-NLS-1$
		validateToken( IToken.tCOLONCOLON);
		validateIdentifier("X"); //$NON-NLS-1$
		validateToken( IToken.tLPAREN );
		validateToken( IToken.t_const );
		validateIdentifier("X"); //$NON-NLS-1$
		validateToken( IToken.tAMPER );
		validateIdentifier( "rtg_arg"); //$NON-NLS-1$
		validateToken( IToken.tRPAREN );  
		validateToken( IToken.tCOLON );
		validateIdentifier( "U"); //$NON-NLS-1$
		validateToken( IToken.tLPAREN );
		validateIdentifier( "rtg_arg"); //$NON-NLS-1$
		validateToken( IToken.tRPAREN );
		validateToken( IToken.tCOMMA );
		validateIdentifier( "Z"); //$NON-NLS-1$
		validateToken( IToken.tLPAREN );
		validateIdentifier( "rtg_arg"); //$NON-NLS-1$
		validateToken( IToken.tDOT );
		validateIdentifier( "Z"); //$NON-NLS-1$
		validateToken( IToken.tRPAREN );
		validateToken( IToken.tCOMMA );
		validateIdentifier( "er"); //$NON-NLS-1$
		validateToken( IToken.tLPAREN );
		validateIdentifier( "rtg_arg"); //$NON-NLS-1$
		validateToken( IToken.tDOT );
		validateIdentifier( "er"); //$NON-NLS-1$
		validateToken( IToken.tRPAREN );
		validateToken( IToken.tLBRACE);
		validateToken( IToken.tRBRACE);
		validateEOF();
		
		initializeScanner( "foo.*bar"); //$NON-NLS-1$
		validateIdentifier("foo"); //$NON-NLS-1$
		validateToken( IToken.tDOTSTAR );
		validateIdentifier("bar"); //$NON-NLS-1$
		validateEOF();
		
		initializeScanner( "foo...bar"); //$NON-NLS-1$
		validateIdentifier("foo"); //$NON-NLS-1$
		validateToken( IToken.tELLIPSIS );
		validateIdentifier("bar"); //$NON-NLS-1$
		validateEOF();
	}

	public void testBug35892() throws ParserFactoryError
	{
		try
		{
			initializeScanner( "'c'" );  //$NON-NLS-1$
			validateChar( 'c' );
			validateEOF(); 
		}
		catch( ScannerException se )
		{
			fail( EXCEPTION_THROWN  + se.getMessage() );
		}
	}

	public void testBug36045() throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append( '"' );
		buffer.append( '\\');
		buffer.append( '"'); 
		buffer.append( '"');
		
		buffer.append( '"');
		buffer.append( '\\');
		buffer.append( '\\');
		buffer.append( '"');
		buffer.append( "\n\n"); //$NON-NLS-1$
		initializeScanner( buffer.toString());
		validateString( "\\\"\\\\"); //$NON-NLS-1$
	}

	public void testConditionalWithBraces() throws ParserFactoryError
	{
		try
		{
			for( int i = 0; i < 4; ++i )
			{
				initializeScanner( "int foobar(int a) { if(a == 0) {\n#ifdef THIS\n} else {}\n#elif THAT\n} else {}\n#endif\nreturn 0;}" ); //$NON-NLS-1$
				switch( i )
				{
					case 0:
						scanner.addDefinition( "THIS", "1"); //$NON-NLS-1$ //$NON-NLS-2$
						scanner.addDefinition( "THAT", "1" );   //$NON-NLS-1$ //$NON-NLS-2$
						break; 
					case 1:
						scanner.addDefinition( "THIS", "1"); //$NON-NLS-1$ //$NON-NLS-2$
						scanner.addDefinition( "THAT", "0" );   //$NON-NLS-1$ //$NON-NLS-2$
						break; 						
					case 2:
						scanner.addDefinition( "THAT", "1" ); //$NON-NLS-1$ //$NON-NLS-2$
						break; 
					case 3: 
						scanner.addDefinition( "THAT", "0" ); //$NON-NLS-1$ //$NON-NLS-2$
						break;
				}
					
				validateToken( IToken.t_int ); 
				validateIdentifier( "foobar");  //$NON-NLS-1$
				validateToken( IToken.tLPAREN ); 
				validateToken( IToken.t_int ); 
				validateIdentifier( "a" );  //$NON-NLS-1$
				validateToken( IToken.tRPAREN ); 
				validateToken( IToken.tLBRACE ); 
				validateToken( IToken.t_if ); 
				validateToken( IToken.tLPAREN );
				validateIdentifier( "a" ); //$NON-NLS-1$
				validateToken( IToken.tEQUAL );
				validateInteger( "0" ); //$NON-NLS-1$
				validateToken( IToken.tRPAREN );
				validateToken( IToken.tLBRACE );
				
				if( i <= 1 )
				{
					validateToken( IToken.tRBRACE ); 
					validateToken( IToken.t_else ); 
					validateToken( IToken.tLBRACE );
					validateToken( IToken.tRBRACE );
				}
					
				if( i == 2 )
				{
					validateToken( IToken.tRBRACE ); 
					validateToken( IToken.t_else ); 
					validateToken( IToken.tLBRACE );
					validateToken( IToken.tRBRACE );
				}
					
				validateToken( IToken.t_return ); 
				validateInteger( "0");  //$NON-NLS-1$
				validateToken( IToken.tSEMI ); 
				validateToken( IToken.tRBRACE ); 
				validateEOF();
			}
		} catch( ScannerException se )
		{
			fail(EXCEPTION_THROWN + se.toString());			
		}
	}
	
	public void testNestedRecursiveDefines() throws Exception
	{
		initializeScanner( "#define C B A\n#define B C C\n#define A B\nA" ); //$NON-NLS-1$
		
		validateIdentifier("B"); //$NON-NLS-1$
		validateDefinition("A", "B"); //$NON-NLS-1$ //$NON-NLS-2$
		validateDefinition("B", "C C"); //$NON-NLS-1$ //$NON-NLS-2$
		validateDefinition("C", "B A"); //$NON-NLS-1$ //$NON-NLS-2$
		validateIdentifier("A"); //$NON-NLS-1$
		validateIdentifier("B"); //$NON-NLS-1$
		validateIdentifier("A"); //$NON-NLS-1$
		validateEOF();
	}
	
	public void testBug36316() throws Exception
	{
		initializeScanner( "#define A B->A\nA" ); //$NON-NLS-1$
	
		validateIdentifier("B"); //$NON-NLS-1$
		validateDefinition("A", "B->A"); //$NON-NLS-1$ //$NON-NLS-2$
		validateToken(IToken.tARROW);
		validateIdentifier("A"); //$NON-NLS-1$
		validateEOF();
	}
	
	public void testBug36434() throws Exception
	{
		initializeScanner( "#define X(Y)\nX(55)"); //$NON-NLS-1$
		validateEOF();
		/*IMacroDescriptor macro = scanner.getDefinition( "X" ); //$NON-NLS-1$
		assertNotNull( macro ); 
		assertEquals( macro.getParameters().length, 1 );
		assertEquals( macro.getParameters()[0], "Y" ); //$NON-NLS-1$
		assertEquals( macro.getTokenizedExpansion().length, 0 );*/
	}
	
	public void testBug36047() throws Exception
	{
		StringWriter writer = new StringWriter(); 
		writer.write( "# define MAD_VERSION_STRINGIZE(str)	#str\n" );  //$NON-NLS-1$
		writer.write( "# define MAD_VERSION_STRING(num)	MAD_VERSION_STRINGIZE(num)\n" );  //$NON-NLS-1$
		writer.write( "# define MAD_VERSION		MAD_VERSION_STRING(MAD_VERSION_MAJOR) \".\" \\\n" ); //$NON-NLS-1$
		writer.write( "                         MAD_VERSION_STRING(MAD_VERSION_MINOR) \".\" \\\n" ); //$NON-NLS-1$
		writer.write( "                         MAD_VERSION_STRING(MAD_VERSION_PATCH) \".\" \\\n" ); //$NON-NLS-1$
		writer.write( "                         MAD_VERSION_STRING(MAD_VERSION_EXTRA)\n" ); //$NON-NLS-1$
		writer.write( "# define MAD_VERSION_MAJOR 2\n" ); //$NON-NLS-1$
		writer.write( "# define MAD_VERSION_MINOR 1\n" ); //$NON-NLS-1$
		writer.write( "# define MAD_VERSION_PATCH 3\n" ); //$NON-NLS-1$
		writer.write( "# define MAD_VERSION_EXTRA boo\n" ); //$NON-NLS-1$
		writer.write( "MAD_VERSION\n" ); //$NON-NLS-1$
		initializeScanner( writer.toString() );
		  
		validateString( "2.1.3.boo" ); //$NON-NLS-1$
		
		validateEOF(); 
	}
	
	public void testBug36475() throws Exception
	{
		StringWriter writer = new StringWriter(); 
		writer.write( " \"A\" \"B\" \"C\" " );  //$NON-NLS-1$
		
		initializeScanner( writer.toString() );
		  
		validateString( "ABC" ); //$NON-NLS-1$
		validateEOF(); 
	}
	
	public void testBug36509() throws Exception 
	{ 
		StringWriter writer = new StringWriter(); 
		writer.write("#define debug(s, t) printf(\"x\" # s \"= %d, x\" # t \"= %s\", \\\n");  //$NON-NLS-1$
		writer.write("                    x ## s, x ## t) \n");  //$NON-NLS-1$
		writer.write("debug(1, 2);"); //$NON-NLS-1$
		   
		initializeScanner( writer.toString() ); 
		//printf("x1=%d, x2= %s", x1, x2); 
		validateIdentifier( "printf" );  //$NON-NLS-1$
		validateToken( IToken.tLPAREN ); 
		validateString("x1= %d, x2= %s");  //$NON-NLS-1$
		validateToken(IToken.tCOMMA); 
		validateIdentifier("x1");  //$NON-NLS-1$
		validateToken(IToken.tCOMMA); 
		validateIdentifier("x2");  //$NON-NLS-1$
		validateToken(IToken.tRPAREN); 
		validateToken(IToken.tSEMI); 
		validateEOF();
	}
	
	public void testBug36695() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write("\'\\4\'  \'\\n\'"); //$NON-NLS-1$
		initializeScanner( writer.toString() );
		
		validateChar( "\\4" ); //$NON-NLS-1$
		validateChar( "\\n" ); //$NON-NLS-1$
		validateEOF();
	}
	
	public void testBug36521() throws Exception 
	{ 
		StringWriter writer = new StringWriter(); 
		writer.write("#define str(s)      # s\n");  //$NON-NLS-1$
		writer.write("fputs(str(strncmp(\"abc\\0d\", \"abc\", \'\\4\')\n");  //$NON-NLS-1$
		writer.write("        == 0), s);\n");  //$NON-NLS-1$
		                         
		initializeScanner( writer.toString() ); 
		validateIdentifier("fputs");  //$NON-NLS-1$
		validateToken(IToken.tLPAREN); 
		
		//TODO as in 36701B, whitespace is not properly replaced inside the string, ok for now.
		//validateString("strncmp(\\\"abc\\\\0d\\\", \\\"abc\\\", '\\\\4') == 0");  //$NON-NLS-1$
		validateString("strncmp(\\\"abc\\\\0d\\\", \\\"abc\\\", '\\\\4')         == 0");  //$NON-NLS-1$
		
		validateToken(IToken.tCOMMA); 
		validateIdentifier("s");  //$NON-NLS-1$
		validateToken(IToken.tRPAREN); 
		validateToken(IToken.tSEMI); 
	}
	
	public void testBug36770() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "#define A 0\n" ); //$NON-NLS-1$
		writer.write( "#if ( A == 1 )\n"); //$NON-NLS-1$
		writer.write( "#  define foo 1\n"); //$NON-NLS-1$
		writer.write( "#else\n"); //$NON-NLS-1$
		writer.write( "# define foo 2\n"); //$NON-NLS-1$
		writer.write( "#endif\n"); //$NON-NLS-1$
		writer.write( "foo\n"); //$NON-NLS-1$
		initializeScanner( writer.toString() );
		validateInteger( "2" ); //$NON-NLS-1$
		validateEOF();
	}
	
	
	public void testBug36816() throws Exception
	{
		initializeScanner( "#include \"foo.h" ); //$NON-NLS-1$
		try{
			validateEOF();
		} catch ( ScannerException e ){
			assertTrue( e.getProblem().getID() == IProblem.PREPROCESSOR_INVALID_DIRECTIVE ); 
		}
	
		initializeScanner( "#include <foo.h" ); //$NON-NLS-1$
		try{
			validateEOF();
		} catch ( ScannerException e ){
			assertTrue( e.getProblem().getID() == IProblem.PREPROCESSOR_INVALID_DIRECTIVE);
		}		
		initializeScanner( "#define FOO(A" ); //$NON-NLS-1$
		try{
			validateEOF();
		} catch( ScannerException e ){
			assertTrue( e.getProblem().getID() == IProblem.PREPROCESSOR_INVALID_MACRO_DEFN );
		}
		initializeScanner( "#define FOO(A \\ B" ); //$NON-NLS-1$
		try{
			validateEOF();
		} catch( ScannerException e ){
			assertTrue( e.getProblem().getID() == IProblem.PREPROCESSOR_INVALID_MACRO_DEFN);
		}
		
	}
	
	public void testBug36255() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "#if defined ( A ) \n" ); //$NON-NLS-1$
		writer.write( "   #if defined ( B ) && ( B != 0 ) \n" ); //$NON-NLS-1$
		writer.write( "      boo\n" ); //$NON-NLS-1$
		writer.write( "   #endif /*B*/\n" ); //$NON-NLS-1$
		writer.write( "#endif /*A*/" ); //$NON-NLS-1$
		
		initializeScanner( writer.toString() );
		validateEOF();
	}
	
	public void testBug37011() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( "#define A \"//\""); //$NON-NLS-1$
		
		initializeScanner( writer.toString() );
		
		validateEOF();
		validateDefinition("A", "\"//\""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testOtherPreprocessorDefines() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( "#define A a//boo\n" ); //$NON-NLS-1$
		writer.write( "#define B a /*boo*/ a\n" ); //$NON-NLS-1$
		writer.write( "#define C a \" //boo \"\n" ); //$NON-NLS-1$
		writer.write( "#define D a \\\"//boo\n" ); //$NON-NLS-1$
		writer.write( "#define E a \\n \"\\\"\"\n" ); //$NON-NLS-1$
		writer.write( "#define F a\\\n b\n" ); //$NON-NLS-1$
		writer.write( "#define G a '\"'//boo\n" ); //$NON-NLS-1$
		writer.write( "#define H a '\\'//b'\"/*bo\\o*/\" b\n" ); //$NON-NLS-1$
		 
		initializeScanner( writer.toString() );
		
		validateEOF();
		
		validateDefinition("A", "a"); //$NON-NLS-1$ //$NON-NLS-2$
		validateDefinition("B", "a  a"); //$NON-NLS-1$ //$NON-NLS-2$
		validateDefinition("C", "a \" //boo \""); //$NON-NLS-1$ //$NON-NLS-2$
		validateDefinition("D", "a \\\""); //$NON-NLS-1$ //$NON-NLS-2$
		validateDefinition("E", "a \\n \"\\\"\""); //$NON-NLS-1$ //$NON-NLS-2$
		validateDefinition("F", "a b"); //$NON-NLS-1$ //$NON-NLS-2$
		validateDefinition("G", "a '\"'"); //$NON-NLS-1$ //$NON-NLS-2$
		validateDefinition("H", "a '\\'//b'\"/*bo\\o*/\" b"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testBug38065() throws Exception
	{
		initializeScanner( "Foo\\\nBar" ); //$NON-NLS-1$
		
		validateIdentifier("FooBar"); //$NON-NLS-1$
		validateEOF();
		
	}
    
    public void testBug36701A() throws Exception
    {
        StringWriter writer = new StringWriter();
        writer.write("#define str(s) # s\n"); //$NON-NLS-1$
        writer.write("str( @ \\n )\n"); //$NON-NLS-1$

        initializeScanner(writer.toString());
        validateString("@ \\\\n"); //$NON-NLS-1$
        validateEOF();
    }
    
    public void testBug36701B() throws Exception 
    {
        StringWriter writer = new StringWriter();
        writer.write("#define str(s) # s\n"); //$NON-NLS-1$
        writer.write("str( @ /*ff*/  \\n  hh  \"aa\"  )\n"); //$NON-NLS-1$

        initializeScanner(writer.toString());

        //TODO The correct string is the one without the comment, however,
        //we don't care right now about the contents of the string, only
        //that we got the string, so having the comment is ok.
        // see also 36521 for the same issue
        
        //validateString("@ \\\\n hh \\\"aa\\\""); //$NON-NLS-1$
        validateString( "@ /*ff*/  \\\\n  hh  \\\"aa\\\""); //$NON-NLS-1$
        validateEOF();
    }
    
    public void testBug44305() throws Exception
    {
		StringWriter writer = new StringWriter(); 
		writer.write( "#define WCHAR_MAX 0 \n"); //$NON-NLS-1$
		writer.write( "#if WCHAR_MAX <= 0xff\n" ); //$NON-NLS-1$
		writer.write( "bool\n"); //$NON-NLS-1$
		writer.write( "#endif");  //$NON-NLS-1$
		initializeScanner( writer.toString());
		validateToken( IToken.t_bool );
		validateEOF();
    }

	public void testBug45287() throws Exception
	{
		initializeScanner( "'abcdefg' L'hijklmnop'"); //$NON-NLS-1$
		validateChar( "abcdefg" ); //$NON-NLS-1$
		validateWideChar( "hijklmnop"); //$NON-NLS-1$
		validateEOF();
	}

	public void testBug45476() throws Exception
	{
		StringBuffer buffer = new StringBuffer(); 
		buffer.append( "#define X 5\n"); //$NON-NLS-1$
		buffer.append( "#if defined X\n"); //$NON-NLS-1$
		buffer.append( "#define Y 10\n"); //$NON-NLS-1$
		buffer.append( "#endif"); //$NON-NLS-1$
		initializeScanner( buffer.toString() );
		validateEOF(); 
		validateDefinition( "Y", "10"); //$NON-NLS-1$ //$NON-NLS-2$
	}
    
    public void testBug45477() throws Exception
    {
    	StringBuffer buffer = new StringBuffer(); 
		buffer.append( "#define D\n" );  //$NON-NLS-1$
		buffer.append( "#define D\n" );  //$NON-NLS-1$
		buffer.append( "#define sum(x,y) x+y\n" ); //$NON-NLS-1$
		buffer.append( "#define E 3\n" );  //$NON-NLS-1$
		buffer.append( "#define E 3\n" ); 		  //$NON-NLS-1$
		buffer.append( "#define sum(x,y) x+y\n"); //$NON-NLS-1$
		buffer.append( "#if defined(D)\n" ); //$NON-NLS-1$
		buffer.append( "printf\n" );  //$NON-NLS-1$
		buffer.append( "#endif\n" ); //$NON-NLS-1$
		buffer.append( "#if defined(sum)\n" ); //$NON-NLS-1$
		buffer.append( "scanf\n" );  //$NON-NLS-1$
		buffer.append( "#endif\n" ); //$NON-NLS-1$
		buffer.append( "#if defined(E)\n" ); //$NON-NLS-1$
		buffer.append( "sprintf\n" );  //$NON-NLS-1$
		buffer.append( "#endif\n" ); //$NON-NLS-1$
		initializeScanner( buffer.toString() );
		validateIdentifier( "printf" );  //$NON-NLS-1$
		validateIdentifier( "scanf"); //$NON-NLS-1$
		validateIdentifier( "sprintf" ); //$NON-NLS-1$
		validateEOF();

		for( int i = 0; i < 5; ++i)
		{		
		
			buffer = new StringBuffer(); 
			
			buffer.append( "#define D blah\n" ); //$NON-NLS-1$
			
			switch( i )
			{
				case 0:
					buffer.append( "#define D\n"); //$NON-NLS-1$
					break; 
				case 1:
					buffer.append( "#define D( x ) echo\n"); //$NON-NLS-1$
					break; 
				case 2: 
					buffer.append( "#define D ACDC\n"); //$NON-NLS-1$
					break; 
				case 3:
					buffer.append( "#define D defined( D )\n"); //$NON-NLS-1$
					break; 
				case 4:
					buffer.append( "#define D blahh\n"); //$NON-NLS-1$
					break; 
 
			}
				
			initializeScanner( buffer.toString() ); 
			try
			{
				validateEOF();
				// Preprocess overwrites are now allowed without correctness checking
				//fail( "Should not reach here");  //$NON-NLS-1$
			}
			catch( ScannerException se )
			{
				assertTrue( se.getProblem().getID() == IProblem.PREPROCESSOR_INVALID_MACRO_REDEFN);
			}
		}
		
		buffer = new StringBuffer(); 
		buffer.append( "#define X 5\n"); //$NON-NLS-1$
		buffer.append( "#define Y 7\n"); //$NON-NLS-1$
		buffer.append( "#define SUMXY X    _+     Y"); //$NON-NLS-1$
		buffer.append( "#define SUMXY   X + Y"); //$NON-NLS-1$
		initializeScanner(buffer.toString());
		validateEOF(); 
    }


	protected static class Callback extends NullSourceElementRequestor implements ISourceElementRequestor
	{
		public List inclusions = new ArrayList();
		public List problems = new ArrayList(); 
			/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
		 */
		public void enterInclusion(IASTInclusion inclusion)
		{
			inclusions.add( inclusion.getName() );
		}
		
		public boolean acceptProblem( IProblem p )
		{
			problems.add( p );
			return super.acceptProblem(p);
		}
		/**
		 * @param mode
		 */
		public Callback(ParserMode mode)
		{
			super( mode );
		}

	}
    
    public void testBug45551() throws Exception
    {
    	StringBuffer buffer = new StringBuffer(); 
    	buffer.append( "#define stdio someNonExistantIncludeFile\n" );  //$NON-NLS-1$
		buffer.append( "#include <stdio.h>\n" );  //$NON-NLS-1$
		
		Callback callback = new Callback( ParserMode.QUICK_PARSE );
		initializeScanner( buffer.toString(), ParserMode.QUICK_PARSE, callback );
		validateEOF();
		assertEquals( callback.problems.size(), 0 );
		assertEquals( callback.inclusions.size(), 1 );
		assertEquals( callback.inclusions.get(0), "stdio.h");  //$NON-NLS-1$
    }
    
    public void testBug46402() throws Exception
	{
    	StringBuffer buffer = new StringBuffer();
    	buffer.append( "#define X 5\n" ); //$NON-NLS-1$
    	buffer.append( "#if defined( X )\n" );  //$NON-NLS-1$
    	buffer.append( "// blah\n" ); //$NON-NLS-1$
    	buffer.append( "#elif Y > 5 \n" ); //$NON-NLS-1$
    	buffer.append( "// coo\n" ); //$NON-NLS-1$
    	buffer.append( "#endif\n" ); //$NON-NLS-1$
    	initializeScanner( buffer.toString(), ParserMode.COMPLETE_PARSE );
    	validateEOF();
    }
    
    public void testBug50821() throws Exception
	{
    	Callback callback = new Callback( ParserMode.QUICK_PARSE );
    	initializeScanner( "\'\n\n\n", ParserMode.QUICK_PARSE,  callback ); //$NON-NLS-1$
    	scanner.nextToken(); 
    	assertEquals( callback.problems.size(), 1 );
    }
   
    
    public void test54778() throws ScannerException
	{
    	initializeScanner("#if 1 || 0 < 3 \n printf \n #endif\n"); //$NON-NLS-1$
    	validateIdentifier("printf"); //$NON-NLS-1$
    	validateEOF();
    	initializeScanner("#if !defined FOO || FOO > 3\nprintf\n#endif\n"); //$NON-NLS-1$
    	validateIdentifier("printf"); //$NON-NLS-1$
    	validateEOF();
    	initializeScanner("#if !defined FOO || FOO < 3\nprintf\n#endif\n"); //$NON-NLS-1$
    	validateIdentifier("printf"); //$NON-NLS-1$
    	validateEOF();
    }
    
    public void test68229() throws Exception{
    	Writer writer = new StringWriter();
    	writer.write( "#define COUNT 0     \n" ); //$NON-NLS-1$
    	writer.write( "1                   \n" ); //$NON-NLS-1$
    	writer.write( "#if COUNT           \n" ); //$NON-NLS-1$
    	writer.write( "   2                \n" ); //$NON-NLS-1$
    	writer.write( "#endif              \n" ); //$NON-NLS-1$
    	writer.write( "3                   \n" ); //$NON-NLS-1$

    	initializeScanner( writer.toString() );
    	
    	IToken t1 = scanner.nextToken();
    	IToken t3 = scanner.nextToken();

    	assertEquals( t1.getImage(), "1" ); //$NON-NLS-1$
    	assertEquals( t3.getImage(), "3" ); //$NON-NLS-1$
    	assertEquals( t1.getNext(), t3 );
    	validateEOF();
    	
    	writer = new StringWriter();
    	writer.write( "#define FOO( x ) x   \n" ); //$NON-NLS-1$
    	writer.write( "1  FOO( 2 )  3       \n" ); //$NON-NLS-1$
    	
    	initializeScanner( writer.toString() );
    	t1 = scanner.nextToken();
    	IToken t2 = scanner.nextToken();
    	t3 = scanner.nextToken();
    	validateEOF();
    	
    	assertEquals( t1.getImage(), "1" ); //$NON-NLS-1$
    	assertEquals( t2.getImage(), "2" ); //$NON-NLS-1$
    	assertEquals( t3.getImage(), "3" ); //$NON-NLS-1$
    	
    	assertEquals( t1.getNext(), t2 );
    }
    
    
    public void testBug56517() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "#if 0 \n"); //$NON-NLS-1$
    	writer.write( "char * x = \"#boo\";\n" ); //$NON-NLS-1$
    	writer.write( "#endif\n"); //$NON-NLS-1$
    	initializeScanner( writer.toString() );
    	validateEOF();
	}
    
    public void testBug36770B() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "#define A 0\n" ); //$NON-NLS-1$
    	writer.write( "#if ( A == 1 )\n" ); //$NON-NLS-1$
    	writer.write( "#  define foo\n" ); //$NON-NLS-1$
    	writer.write( "#else\n" ); //$NON-NLS-1$
    	writer.write( "#   define bar\n" ); //$NON-NLS-1$
    	writer.write( "#endif\n" ); //$NON-NLS-1$
    	initializeScanner( writer.toString(), ParserMode.QUICK_PARSE );
    	validateEOF();
    	validateDefinition( "A", 0 ); //$NON-NLS-1$
    	validateDefinition( "bar", "" ); //$NON-NLS-1$ //$NON-NLS-2$

	}
    
    public void testBug47797() throws Exception
	{
    	initializeScanner( "\"\\uABCD\" \'\\uABCD\' \\uABCD_ident \\u001A01BC_ident ident\\U01AF ident\\u01bc00AF"); //$NON-NLS-1$
    	validateString( "\\uABCD"); //$NON-NLS-1$
    	validateChar( "\\uABCD"); //$NON-NLS-1$
    	validateIdentifier( "\\uABCD_ident"); //$NON-NLS-1$
    	validateIdentifier( "\\u001A01BC_ident"); //$NON-NLS-1$
    	validateIdentifier( "ident\\U01AF" ); //$NON-NLS-1$
    	validateIdentifier( "ident\\u01bc00AF" ); //$NON-NLS-1$
    	validateEOF();
	}
    
    
    public void testBug59768() throws Exception
	{
    	initializeScanner( "#define A A\nA"); //$NON-NLS-1$
    	validateIdentifier( "A"); //$NON-NLS-1$
    	validateEOF();
    	/*IMacroDescriptor d = scanner.getDefinition( "A"); //$NON-NLS-1$
    	assertTrue( d.isCircular() );*/
	}
    
    public void testBug60764() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "#define P   a,b\n"); //$NON-NLS-1$
    	writer.write( "#define M(x) M1(x)\n"); //$NON-NLS-1$
    	writer.write( "#define M1(x,y) #x  #y\n"); //$NON-NLS-1$
    	writer.write( "M(P)\n"); //$NON-NLS-1$
    	initializeScanner( writer.toString() );
    	validateString( "ab"); //$NON-NLS-1$
    	validateEOF();
	}
    
    public void testBug62042() throws Exception
	{
    	Callback callback = new Callback(ParserMode.QUICK_PARSE);
    	initializeScanner( "0x", ParserMode.QUICK_PARSE,  callback ); //$NON-NLS-1$
    	validateInteger("0x"); // to me this is a valid number //$NON-NLS-1$
    	validateEOF();
    	//assertFalse( callback.problems.isEmpty() );
	}
    
    public void testBug61968() throws Exception
	{
    	Writer writer = new StringWriter(); 
    	writer.write( "unsigned int ui = 2172748163; //ok \n" ); //$NON-NLS-1$
    	writer.write( "int big = 999999999999999;//ok \n" ); //$NON-NLS-1$
    	writer.write( "void main() { \n" ); //$NON-NLS-1$
    	writer.write( "caller(4);  //ok\n" );  //$NON-NLS-1$
    	writer.write( "caller(2172748163);//causes java.lang.NumberFormatException \n" ); //$NON-NLS-1$
    	writer.write( "caller(999999999999999); //also causes NumberFormatException \n" ); //$NON-NLS-1$
    	writer.write( "}\n" ); //$NON-NLS-1$
    	Callback callback = new Callback(ParserMode.QUICK_PARSE);
    	initializeScanner( writer.toString(), ParserMode.QUICK_PARSE, callback );
    	fullyTokenize();
    	assertTrue( callback.problems.isEmpty() );
	}
    
    public void testBug62378() throws Exception
	{
    	initializeScanner( "\"\\?\\?<\""); //$NON-NLS-1$
    	validateString("\\?\\?<" ); //$NON-NLS-1$
	}
    
    public void testBug62384() throws Exception
	{
    	initializeScanner( "18446744073709551615LL"); //$NON-NLS-1$
    	validateInteger( "18446744073709551615LL"); //$NON-NLS-1$
	}
    
    public void testBug62390() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "#define f(x) x\n"); //$NON-NLS-1$
    	writer.write( "#if f(\n"); //$NON-NLS-1$
    	writer.write( "5) == 5\n"); //$NON-NLS-1$
    	writer.write( "true1\n"); //$NON-NLS-1$
    	writer.write( "#endif\n"); //$NON-NLS-1$
    	writer.write( "#if A\n"); //$NON-NLS-1$
    	writer.write( "#elif f(\n"); //$NON-NLS-1$
    	writer.write( "5) == 5\n"); //$NON-NLS-1$
    	writer.write( "true2\n"); //$NON-NLS-1$
    	writer.write( "#endif\n"); //$NON-NLS-1$
		writer.write( "#undef f\n"); //$NON-NLS-1$
		writer.write( "#define f(x) \"A0I70_001.h\"\n"); //$NON-NLS-1$
		writer.write( "#include f(\n"); //$NON-NLS-1$
		writer.write( "5\n"); //$NON-NLS-1$
    	writer.write( ")\n"); //$NON-NLS-1$
    	writer.write( "#undef f\n"); //$NON-NLS-1$
    	writer.write( "#define f(x) 1467\n"); //$NON-NLS-1$
    	writer.write( "#line f(\n"); //$NON-NLS-1$
    	writer.write( "5\n"); //$NON-NLS-1$
    	writer.write( ")\n"); //$NON-NLS-1$
    	writer.write( "#pragma f(\n"); //$NON-NLS-1$
    	writer.write( "5\n"); //$NON-NLS-1$
    	writer.write( ")\n"); //$NON-NLS-1$
    	writer.write( "}\n"); //$NON-NLS-1$
    	Callback callback = new Callback( ParserMode.QUICK_PARSE );
    	initializeScanner( writer.toString(), ParserMode.QUICK_PARSE, callback );
    	fullyTokenize();
	}
    
    public void testBug62009() throws Exception
	{
    	Callback callback = new Callback( ParserMode.QUICK_PARSE );
    	initializeScanner( "#define def(x) (x#)\ndef(orange)\n", ParserMode.QUICK_PARSE, callback ); //$NON-NLS-1$
    	fullyTokenize();
    	assertFalse( callback.problems.isEmpty() );
	}
    
    public void testBug61972() throws Exception
	{
    	initializeScanner( "#define DEF1(A1) A1\n#define DEF2     DEF1(DEF2)\nDEF2;" ); //$NON-NLS-1$
    	validateIdentifier( "DEF2"); //$NON-NLS-1$
    	validateToken( IToken.tSEMI );
    	validateEOF();
	}
    
    public void testBug64268() throws Exception
	{
      	Writer writer = new StringWriter();
		writer.write("#define BODY \\\n"); //$NON-NLS-1$
		writer.write(" {	 \\\n"); //$NON-NLS-1$
		writer.write(" /* this multi-line comment messes \\\n"); //$NON-NLS-1$
		writer.write(" up the parser.  */ }\n"); //$NON-NLS-1$
		writer.write("BODY "); //$NON-NLS-1$
		initializeScanner( writer.toString() );
		validateToken( IToken.tLBRACE);
		validateToken( IToken.tRBRACE);
		validateEOF();
	}
    
    public void test__attribute__() throws Exception {
    	initializeScanner(
    			"#define __cdecl __attribute__((cdecl))\n" + //$NON-NLS-1$
				"__cdecl;"); //$NON-NLS-1$
    	validateToken(IToken.tSEMI);
    	validateEOF();
	}
    
    public void testUndef() throws Exception {
    	initializeScanner(
    			"#define A 5\n" + //$NON-NLS-1$
				"#define B 10\n" + //$NON-NLS-1$
				"#undef A\n" + //$NON-NLS-1$
				"A B"); //$NON-NLS-1$
    	validateIdentifier("A"); //$NON-NLS-1$
    	validateInteger("10"); //$NON-NLS-1$
    	validateEOF();
    }
    
    public void testWackyFunctionMacros() throws Exception {
    	initializeScanner(
    			"#define A(X) hi##X\n" + //$NON-NLS-1$
				"#define B(Y) A(Y)\n" + //$NON-NLS-1$
				"B(there)"); //$NON-NLS-1$
    	validateIdentifier("hithere"); //$NON-NLS-1$
    	validateEOF();
    }
    
    public void testSlashes() throws Exception {
    	initializeScanner("__q / __n"); //$NON-NLS-1$
    	validateIdentifier("__q"); //$NON-NLS-1$
    	validateToken(IToken.tDIV);
    	validateIdentifier("__n"); //$NON-NLS-1$
    	validateEOF();
    }
    
    public void testStringify() throws Exception {
    	initializeScanner("#define xS(s) #s\n#define S(s) xS(s)\n#define X hi\nS(X)"); //$NON-NLS-1$
    	validateString("hi"); //$NON-NLS-1$
    	validateEOF();
    }
    
    public void testWideToNarrowConcatenation() throws Exception
	{
    	initializeScanner( "\"ONE\" L\"TWO\""); //$NON-NLS-1$
    	validateString( "ONETWO", true); //$NON-NLS-1$
    	validateEOF();
	}

    public void testEmptyIncludeDirective() throws Exception
	{
    	QuickParseCallback qpc = new QuickParseCallback();
    	initializeScanner( "#include \n#include <foo.h>\n", ParserMode.QUICK_PARSE, qpc ); //$NON-NLS-1$
    	validateEOF();
    	Iterator i = qpc.getInclusions();
    	assertTrue( i.hasNext() );
    	IASTInclusion inc = (IASTInclusion) i.next();
    	assertFalse( i.hasNext() );
    	assertEquals( inc.getName(), "foo.h"); //$NON-NLS-1$
	}
    
    public void testBug69412() throws Exception
	{
    	Callback callback = new Callback( ParserMode.COMPLETE_PARSE );
    	initializeScanner( "\'\\\\\'", ParserMode.COMPLETE_PARSE, callback ); //$NON-NLS-1$
    	validateChar( "\\\\"); //$NON-NLS-1$
    	validateEOF();
    	assertTrue( callback.problems.isEmpty());
	}
    
    public void testBug70072() throws Exception
	{
    	initializeScanner( "#if 1/0\nint i;\n#elif 2/0\nint j;\n#endif\nint k;\n" ); //$NON-NLS-1$
    	fullyTokenize();
	}
    
    public void testBug70261() throws Exception
	{
    	initializeScanner( "0X0"); //$NON-NLS-1$
    	validateInteger( "0X0"); //$NON-NLS-1$
	}
    
    public void testBug62571() throws Exception
	{
    	StringBuffer buffer = new StringBuffer( "#define J(X,Y) X##Y\n"); //$NON-NLS-1$
		buffer.append( "J(A,1Xxyz)\n"); //$NON-NLS-1$
		buffer.append( "J(B,1X1X1Xxyz)\n");	 //$NON-NLS-1$
		buffer.append( "J(C,0Xxyz)\n"); //$NON-NLS-1$
		buffer.append( "J(CC,0Xxyz)\n"); //$NON-NLS-1$
		buffer.append( "J(D,0xxyz)\n"); //$NON-NLS-1$
		buffer.append( "J(E,0x0x0xxyz)\n"); //$NON-NLS-1$
		initializeScanner( buffer.toString() );
		validateIdentifier( "A1Xxyz"); //$NON-NLS-1$
		validateIdentifier( "B1X1X1Xxyz"); //$NON-NLS-1$
		validateIdentifier( "C0Xxyz"); //$NON-NLS-1$
		validateIdentifier( "CC0Xxyz"); //$NON-NLS-1$
		validateIdentifier( "D0xxyz"); //$NON-NLS-1$
		validateIdentifier( "E0x0x0xxyz"); //$NON-NLS-1$
	}
    
    public void testBug69134() throws Exception
	{
    	Writer writer = new StringWriter();
    	writer.write( "# ifdef YYDEBUG\n" ); //$NON-NLS-1$
    	writer.write( "if (yyDebug) {\n" ); //$NON-NLS-1$
    	writer.write( "(void) fprintf (yyTrace,\n" ); //$NON-NLS-1$
    	writer.write( "\"  # |Position|State|Mod|Lev|Action |Terminal and Lookahead or Rule\n\");\n" ); //$NON-NLS-1$
    	writer.write( "yyNl ();\n" ); //$NON-NLS-1$
    	writer.write( "}\n" ); //$NON-NLS-1$
    	writer.write( "# endif\n" ); //$NON-NLS-1$
    	Callback callback = new Callback( ParserMode.COMPLETE_PARSE );
    	initializeScanner( writer.toString(), ParserMode.COMPLETE_PARSE, callback );
    	fullyTokenize();
    	assertTrue( callback.problems.isEmpty() );

	}
    
    public void testBug70073() throws Exception
    {
        StringBuffer buffer = new StringBuffer( "#if CONST \n #endif \n #elif CONST \n int" ); //$NON-NLS-1$
        final List problems = new ArrayList();
        ISourceElementRequestor requestor = new NullSourceElementRequestor() {
            public boolean acceptProblem(IProblem problem)
            {
                problems.add( problem );
                return super.acceptProblem( problem );
            }
        };
        initializeScanner( buffer.toString(), ParserMode.COMPLETE_PARSE, requestor );
        validateToken( IToken.t_int );
        assertEquals( problems.size(), 1 );
    }
}
