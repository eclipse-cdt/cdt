package org.eclipse.cdt.core.parser.tests;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.internal.core.parser.IMacroDescriptor;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.Scanner;
import org.eclipse.cdt.internal.core.parser.ScannerException;
import org.eclipse.cdt.internal.core.parser.Token;

/**
 * @author jcamelon
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ScannerTestCase extends TestCase
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
				s.append("var").append(i).append("=").append(values[i]).append(" ");
			}
			return s.toString();
		}

		public String symbolName(int index)
		{
			return "DEFINITION" + index;
		}

		public int symbolValue(int index)
		{
			return new Long(Math.round(Math.pow(index, index))).intValue();
		}

		public String generateCode()
		{
			if (length < 2)
			{
				return "Array must have at least 2 elements";
			}
			int numberOfElsifs= length - 1;
			StringBuffer buffer= new StringBuffer();
			buffer.append("#if ").append(values[0]).append("\n#\tdefine ");
			buffer.append(symbolName(0)).append(" ").append(symbolValue(0));
			for (int i= 0; i < numberOfElsifs; ++i)
				buffer
					.append("\n#elif ")
					.append(values[1 + i])
					.append("\n#\tdefine ")
					.append(symbolName(i + 1))
					.append(" ")
					.append(symbolValue(i + 1));
			buffer
				.append("\n#else \n#\tdefine ")
				.append(symbolName(length))
				.append(" ")
				.append(symbolValue(length))
				.append("\n#endif");
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

	public final static String EXCEPTION_THROWN= "Exception thrown ";
	public final static String EXPECTED_FAILURE=
		"This statement should not be reached "
			+ "as we sent in bad preprocessor input to the scanner";
	public final static boolean verbose= false;
	public final static boolean doIncludeStdio= false;
	public final static boolean doIncludeWindowsH= false;
	public final static boolean doIncludeWinUserH= false;
	
	public final static int SIZEOF_TRUTHTABLE = 10; 

	public void initializeScanner(String input)
	{
		scanner= new Scanner(); 
		scanner.initialize( new StringReader(input),"TEXT");
	}

	public static Test suite()
	{
		return new TestSuite(ScannerTestCase.class);
	}

	public int fullyTokenize() throws Exception
	{
		try
		{
			Token t= scanner.nextToken();
			while (t != null)
			{
				if (verbose)
					System.out.println("Token t = " + t);

				if ((t.type < 1) || (t.type > Token.tLAST))
					System.out.println("Unknown type for token " + t);
				t= scanner.nextToken();
			}
		}
		catch (Parser.EndOfFile e)
		{
		}
		catch (ScannerException se)
		{
			throw se;
		}
		return scanner.getCount();
	}

	public void testWeirdStrings()
	{
		try
		{
			initializeScanner( "Living Life L\"LONG\"");
			validateIdentifier( "Living" );
			validateIdentifier( "Life" );
			validateString("LONG", true);
			validateEOF();
		}
		catch( ScannerException se )
		{
			fail(EXCEPTION_THROWN + se.toString());
		}
		
	}
	
	
	public void testNumerics()
	{
		try
		{
			initializeScanner("3.0 0.9 .5 3. 4E5 2.01E-03 ...");
			validateFloatingPointLiteral( "3.0");
			validateFloatingPointLiteral( "0.9");
			validateFloatingPointLiteral( ".5");
			validateFloatingPointLiteral( "3."); 
			validateFloatingPointLiteral( "4E5");
			validateFloatingPointLiteral( "2.01E-03" );
			validateToken( Token.tELIPSE );
			validateEOF();
		}
		catch( ScannerException se )
		{
			fail(EXCEPTION_THROWN + se.toString());
		}
		
	}
	
	
	Scanner scanner;

	/**
	 * Constructor for ScannerTestCase.
	 * @param name
	 */
	public ScannerTestCase(String name)
	{
		super(name);
	}

	public void testPreprocessorDefines()
	{
		try
		{
			initializeScanner("#define SIMPLE_NUMERIC 5\nint x = SIMPLE_NUMERIC");
			validateToken(Token.t_int);
			validateDefinition("SIMPLE_NUMERIC", "5");
			validateIdentifier("x");
			validateToken(Token.tASSIGN);
			validateInteger("5");
			validateEOF();

			initializeScanner("#define SIMPLE_STRING \"This is a simple string.\"\n\nconst char * myVariable = SIMPLE_STRING;");
			validateToken(Token.t_const);
			validateDefinition("SIMPLE_STRING", "\"This is a simple string.\"");
			validateToken(Token.t_char);
			validateToken(Token.tSTAR);
			validateIdentifier("myVariable");
			validateToken(Token.tASSIGN);
			validateString("This is a simple string.");
			validateToken(Token.tSEMI);
			validateEOF();

			initializeScanner("#define FOOL 5  \n int tryAFOOL = FOOL + FOOL;");

			validateToken(Token.t_int);
			validateIdentifier("tryAFOOL");
			validateToken(Token.tASSIGN);
			validateInteger("5");
			validateToken(Token.tPLUS);
			validateInteger("5");
			validateToken(Token.tSEMI);
			validateEOF();

			initializeScanner("#define FOOL 5  \n int FOOLer = FOOL;");

			validateToken(Token.t_int);
			validateIdentifier("FOOLer");
			validateToken(Token.tASSIGN);
			validateInteger("5");
			validateToken(Token.tSEMI);
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
			validateDefinition("MULTICOMMENT", "X  + Y");

			for (int i= 0; i < 7; ++i)
			{
				switch (i)
				{
					case 0 :
						initializeScanner("#define SIMPLE_STRING This is a simple string.\n");
						break;
					case 1 :
						initializeScanner("#	define SIMPLE_NUMERIC 5\n");
						break;
					case 2 :
						initializeScanner("#	define		SIMPLE_NUMERIC   	5\n");
						break;
					case 3 :
						initializeScanner("#define 		SIMPLE_STRING \"This 	is a simple     string.\"\n");
						break;
					case 4 :
						initializeScanner("#define SIMPLE_STRING 	  	This 	is a simple 	string.\n");
						break;
					case 5 :
						initializeScanner("#define FLAKE\n\nFLAKE");
						break;
					case 6 :
						initializeScanner("#define SIMPLE_STRING 	  	This 	is a simple 	string.\\\n		Continue please.");
						break;
				}
				validateEOF();

				switch (i)
				{
					case 0 :
						validateDefinition(
							"SIMPLE_STRING",
							"This is a simple string.");
						break;
					case 1 :
						validateDefinition("SIMPLE_NUMERIC", "5");
						break;
					case 2 :
						validateDefinition("SIMPLE_NUMERIC", "5");
						break;
					case 3 :
						validateDefinition(
							"SIMPLE_STRING",
							"\"This 	is a simple     string.\"");
						break;
					case 4 :
						validateDefinition(
							"SIMPLE_STRING",
							"This 	is a simple 	string.");
						break;
					case 5 :
						validateDefinition("FLAKE", "");
						break;
					case 6 :
						validateDefinition(
							"SIMPLE_STRING",
							"This 	is a simple 	string.		Continue please.");
				}
			}
		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}
	}

	public void prepareForWindowsRH()
	{
		scanner.addIncludePath(
			"C:\\Program Files\\Microsoft Visual Studio\\VC98\\Include");
		scanner.addDefinition("_WIN32_WINNT", "0x0300");
		scanner.addDefinition("WINVER", "0x0400");
		scanner.addDefinition("_WIN32_WINDOWS", "0x0300");
		scanner.addDefinition("_MSC_VER", "1200");
	}

	public void prepareForWindowsH()
	{
		scanner.addIncludePath(
			"C:\\Program Files\\Microsoft Visual Studio\\VC98\\Include");
		scanner.addDefinition("_MSC_VER", "1200");
		scanner.addDefinition("__cplusplus", "1");
		scanner.addDefinition("__STDC__", "1");
		scanner.addDefinition("_WIN32", "");		
		scanner.addDefinition( "__midl", "1000" );
		scanner.addDefinition("_WIN32_WINNT", "0x0300");
		scanner.addDefinition("WINVER", "0x0400");
		scanner.addDefinition( "_M_IX86", "300");
		scanner.addDefinition( "_INTEGRAL_MAX_BITS", "64");
	}

	public void prepareForStdio()
	{
		scanner.addIncludePath(
			"C:\\Program Files\\Microsoft Visual Studio\\VC98\\Include");
		scanner.addDefinition("_MSC_VER", "1100");
		scanner.addDefinition("__STDC__", "1");
		scanner.addDefinition("_INTEGRAL_MAX_BITS", "64");
		scanner.addDefinition("_WIN32", "");
		scanner.addDefinition( "_M_IX86", "300");
	}

	public void testConcatenation()
	{
		try
		{
			initializeScanner("#define F1 3\n#define F2 F1##F1\nint x=F2;");
			validateToken(Token.t_int);
			validateDefinition("F1", "3");
			validateDefinition( "F2", "F1##F1");
			validateIdentifier("x");
			validateToken(Token.tASSIGN);
			validateInteger("33");
			validateToken(Token.tSEMI);
			validateEOF();
			
			initializeScanner("#define PREFIX RT_\n#define RUN PREFIX##Run"); 
			validateEOF(); 
			validateDefinition( "PREFIX", "RT_" ); 
			validateDefinition( "RUN", "PREFIX##Run" );
		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}
		
		try
		{
			initializeScanner( "#define DECLARE_HANDLE(name) struct name##__ { int unused; }; typedef struct name##__ *name\n DECLARE_HANDLE( joe )" );
			validateToken( Token.t_struct );
			validateIdentifier( "joe__"); 
			validateToken( Token.tLBRACE);  
			validateToken( Token.t_int ); 
			validateIdentifier( "unused"); 
			validateToken( Token.tSEMI ); 
			validateToken( Token.tRBRACE );
			validateToken( Token.tSEMI ); 
			validateToken( Token.t_typedef ); 
			validateToken( Token.t_struct ); 
			validateIdentifier( "joe__" ); 
			validateToken( Token.tSTAR ); 
			validateIdentifier( "joe");  
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

			initializeScanner("#define SYMBOL 5\n#ifdef SYMBOL\nint counter(SYMBOL);\n#endif");

			validateToken(Token.t_int);
			validateIdentifier("counter");
			validateToken(Token.tLPAREN);
			validateInteger("5");
			validateToken(Token.tRPAREN);
			validateToken(Token.tSEMI);
			validateEOF();

			initializeScanner("#define SYMBOL 5\n#ifndef SYMBOL\nint counter(SYMBOL);\n#endif");
			validateEOF();

			initializeScanner("#ifndef DEFINED\n#define DEFINED 100\n#endif\nint count = DEFINED;");
			validateToken(Token.t_int);
			validateDefinition("DEFINED", "100");

			validateIdentifier("count");
			validateToken(Token.tASSIGN);
			validateInteger("100");
			validateToken(Token.tSEMI);
			validateEOF();

			initializeScanner("#ifndef DEFINED\n#define DEFINED 100\n#endif\nint count = DEFINED;");
			scanner.addDefinition("DEFINED", "101");

			validateDefinition("DEFINED", "101");
			validateToken(Token.t_int);
			validateIdentifier("count");
			validateToken(Token.tASSIGN);
			validateInteger("101");
			validateToken(Token.tSEMI);
			validateEOF();
			
			initializeScanner( "/* NB: This is #if 0'd out */"); 
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
		code.write( "#define COMPLEX_MACRO 33 \\\n");
		code.write( "	+ 44\n\nCOMPLEX_MACRO");
		initializeScanner( code.toString() );
		validateInteger( "33" );
		validateToken( Token.tPLUS );
		validateInteger( "44" );
	}

	public void testSlightlyComplexIfdefStructure()
	{
		try
		{
			initializeScanner("#ifndef BASE\n#define BASE 10\n#endif\n#ifndef BASE\n#error BASE is defined\n#endif");
			validateEOF();
			validateBalance();
		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}

		try
		{
			initializeScanner("#ifndef ONE\n#define ONE 1\n#ifdef TWO\n#define THREE ONE + TWO\n#endif\n#endif\nint three(THREE);");

			validateToken(Token.t_int);
			validateDefinition("ONE", "1");
			validateAsUndefined("TWO");
			validateAsUndefined("THREE");
			validateIdentifier("three");
			validateToken(Token.tLPAREN);
			validateIdentifier("THREE");
			validateToken(Token.tRPAREN);
			validateToken(Token.tSEMI);
			validateEOF();
			validateBalance();

			initializeScanner("#ifndef ONE\n#define ONE 1\n#ifdef TWO\n#define THREE ONE + TWO\n#endif\n#endif\nint three(THREE);");
			scanner.addDefinition("TWO", "2");
			validateToken(Token.t_int);
			validateDefinition("ONE", "1");
			validateDefinition("TWO", "2");
			validateDefinition("THREE", "ONE + TWO");

			validateIdentifier("three");
			validateToken(Token.tLPAREN);
			validateInteger("1");
			validateToken(Token.tPLUS);
			validateInteger("2");
			validateToken(Token.tRPAREN);
			validateToken(Token.tSEMI);
			validateEOF();
			validateBalance();

			initializeScanner("#ifndef FOO\n#define FOO 4\n#else\n#undef FOO\n#define FOO 6\n#endif");
			validateEOF();
			validateBalance();
			validateDefinition("FOO", "4");

			initializeScanner("#ifndef FOO\n#define FOO 4\n#else\n#undef FOO\n#define FOO 6\n#endif");
			scanner.addDefinition("FOO", "2");
			validateEOF();
			validateBalance();
			validateDefinition("FOO", "6");

			initializeScanner("#ifndef ONE\n#   define ONE 1\n#   ifndef TWO\n#       define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#else\n#   ifndef TWO\n#      define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#endif\n");
			validateEOF();
			validateBalance();
			validateDefinition("ONE", "1");
			validateDefinition("TWO", "ONE + ONE");

			initializeScanner("#ifndef ONE\n#   define ONE 1\n#   ifndef TWO\n#       define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#else\n#   ifndef TWO\n#      define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#endif\n");
			scanner.addDefinition("ONE", "one");
			validateEOF();
			validateBalance();
			validateDefinition("ONE", "one");
			validateDefinition("TWO", "ONE + ONE");

			initializeScanner("#ifndef ONE\n#   define ONE 1\n#   ifndef TWO\n#       define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#else\n#   ifndef TWO\n#      define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#endif\n");
			scanner.addDefinition("ONE", "one");
			scanner.addDefinition("TWO", "two");
			validateEOF();
			validateBalance();

			validateDefinition("ONE", "one");
			validateDefinition("TWO", "2");

			initializeScanner("#ifndef ONE\n#   define ONE 1\n#   ifndef TWO\n#       define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#else\n#   ifndef TWO\n#      define TWO ONE + ONE \n#   else\n#       undef TWO\n#       define TWO 2 \n#   endif\n#endif\n");
			scanner.addDefinition("TWO", "two");
			validateEOF();
			validateBalance();

			validateDefinition("ONE", "1");
			validateDefinition("TWO", "2");

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
			initializeScanner("#if 0\n#error NEVER\n#endif\n");
			validateEOF();
			validateBalance();

			initializeScanner("#define X 5\n#define Y 7\n#if (X < Y)\n#define Z X + Y\n#endif");
			validateEOF();
			validateBalance();
			validateDefinition("X", "5");
			validateDefinition("Y", "7");
			validateDefinition("Z", "X + Y");

			initializeScanner("#if T < 20\n#define Z T + 1\n#endif");
			scanner.addDefinition("X", "5");
			scanner.addDefinition("Y", "7");
			scanner.addDefinition("T", "X + Y");
			validateEOF();
			validateBalance();
			validateDefinition("X", "5");
			validateDefinition("Y", "7");
			validateDefinition("T", "X + Y");
			validateDefinition("Z", "T + 1");

		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}

		try
		{
			initializeScanner("#if ( 10 / 5 ) != 2\n#error 10/5 seems to not equal 2 anymore\n#endif\n");
			validateEOF();
			validateBalance();
		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}

		try
		{
			initializeScanner("#ifndef FIVE \n#define FIVE 5\n#endif \n#ifndef TEN\n#define TEN 2 * FIVE\n#endif\n#if TEN != 10\n#define MISTAKE 1\n#error Five does not equal 10\n#endif\n");
			scanner.addDefinition("FIVE", "55");
			validateEOF();
			fail(EXPECTED_FAILURE);
		}
		catch (ScannerException se)
		{
			validateBalance(1);
			validateDefinition("FIVE", "55");
			validateDefinition("TEN", "2 * FIVE");
			validateDefinition("MISTAKE", "1");
		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}

		try
		{
			initializeScanner("#if ((( FOUR / TWO ) * THREE )< FIVE )\n#error 6 is not less than 5 \n#endif\n#if ( ( FIVE * ONE ) != (( (FOUR) + ONE ) * ONE ) )\n#error 5 should equal 5\n#endif \n");

			scanner.addDefinition("ONE", "1");
			scanner.addDefinition("TWO", "(ONE + ONE)");
			scanner.addDefinition("THREE", "(TWO + ONE)");
			scanner.addDefinition("FOUR", "(TWO * TWO)");
			scanner.addDefinition("FIVE", "(THREE + TWO)");

			validateEOF();
			validateBalance();
			validateDefinition("ONE", "1");
			validateDefinition("TWO", "(ONE + ONE)");
			validateDefinition("THREE", "(TWO + ONE)");
			validateDefinition("FOUR", "(TWO * TWO)");
			validateDefinition("FIVE", "(THREE + TWO)");

			TruthTable table= new TruthTable(SIZEOF_TRUTHTABLE);
			int numberOfRows= table.getNumberOfRows();
			TableRow[] rows= table.rows;

			for (int i= 0; i < numberOfRows; ++i)
			{
				TableRow row= rows[i];
				String code= row.generateCode();
				if (verbose)
					System.out.println("\n\nRow " + i + " has code\n" + code);
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
			initializeScanner("#if ! 0\n#error Correct!\n#endif");
			Token t= scanner.nextToken();
			fail(EXPECTED_FAILURE);
		}
		catch (ScannerException se)
		{
			validateBalance(1);
			assertTrue(se.getMessage().equals("#error Correct!"));
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
			initializeScanner("#define GO(x) x+1\nint y(5);\ny = GO(y);");
			validateToken(Token.t_int);
			validateIdentifier("y");
			validateToken(Token.tLPAREN);
			validateInteger("5");
			validateToken(Token.tRPAREN);
			validateToken(Token.tSEMI);

			IMacroDescriptor descriptor=
				(IMacroDescriptor) scanner.getDefinition("GO");
			List parms= descriptor.getParameters();
			assertNotNull(parms);
			assertTrue(parms.size() == 1);
			String parm1= (String) parms.get(0);
			assertTrue(parm1.equals("x"));
			List expansion= descriptor.getTokenizedExpansion();
			assertNotNull(parms);
			assertTrue(expansion.size() == 3);
			assertTrue(((Token) expansion.get(0)).type == Token.tIDENTIFIER);
			assertTrue(((Token) expansion.get(0)).image.equals("x"));
			assertTrue(((Token) expansion.get(1)).type == Token.tPLUS);
			assertTrue(((Token) expansion.get(2)).type == Token.tINTEGER);
			assertTrue(((Token) expansion.get(2)).image.equals("1"));

			validateIdentifier("y");
			validateToken(Token.tASSIGN);
			validateIdentifier("y");
			validateToken(Token.tPLUS);
			validateInteger("1");
			validateToken(Token.tSEMI);
			validateEOF();
			validateBalance();

			initializeScanner(
				"#define ONE 1\n"
					+ "#define SUM(a,b,c,d,e,f,g) ( a + b + c + d + e + f + g )\n"
					+ "int daSum = SUM(ONE,3,5,7,9,11,13);");
			validateToken(Token.t_int);
			validateIdentifier("daSum");
			validateToken(Token.tASSIGN);
			validateToken(Token.tLPAREN);
			validateInteger("1");
			validateToken(Token.tPLUS);
			validateInteger("3");
			validateToken(Token.tPLUS);
			validateInteger("5");
			validateToken(Token.tPLUS);
			validateInteger("7");
			validateToken(Token.tPLUS);
			validateInteger("9");
			validateToken(Token.tPLUS);
			validateInteger("11");
			validateToken(Token.tPLUS);
			validateInteger("13");
			validateToken(Token.tRPAREN);
			validateToken(Token.tSEMI);
			validateEOF();

			IMacroDescriptor macro= (IMacroDescriptor) scanner.getDefinition("SUM");
			List params= macro.getParameters();
			assertNotNull(params);
			assertTrue(params.size() == 7);

			List tokens= macro.getTokenizedExpansion();
			assertNotNull(tokens);
			assertTrue(tokens.size() == 15);

			initializeScanner("#define LOG( format, var1)   printf( format, var1 )\nLOG( \"My name is %s\", \"Bogdan\" );\n");
			validateIdentifier("printf");
			validateToken(Token.tLPAREN);
			validateString("My name is %s");
			validateToken(Token.tCOMMA);
			validateString("Bogdan");
			validateToken(Token.tRPAREN);
			validateToken(Token.tSEMI);
			validateEOF();

			initializeScanner("#define INCR( x )   ++x\nint y(2);\nINCR(y);");
			validateToken(Token.t_int);
			validateIdentifier("y");
			validateToken(Token.tLPAREN);
			validateInteger("2");
			validateToken(Token.tRPAREN);
			validateToken(Token.tSEMI);
			validateToken(Token.tINCR);
			validateIdentifier("y");
			validateToken(Token.tSEMI);
			validateEOF();

			initializeScanner("#define CHECK_AND_SET( x, y, z )     if( x ) { \\\n y = z; \\\n }\n\nCHECK_AND_SET( 1, balance, 5000 );\nCHECK_AND_SET( confused(), you, dumb );");
			validateToken(Token.t_if);
			validateToken(Token.tLPAREN);
			validateInteger("1");
			validateToken(Token.tRPAREN);
			validateToken(Token.tLBRACE);
			validateIdentifier("balance");
			validateToken(Token.tASSIGN);
			validateInteger("5000");
			validateToken(Token.tSEMI);
			validateToken(Token.tRBRACE);
			validateToken(Token.tSEMI);

			validateToken(Token.t_if);
			validateToken(Token.tLPAREN);
			validateIdentifier("confused");
			validateToken(Token.tLPAREN);
			validateToken(Token.tRPAREN);
			validateToken(Token.tRPAREN);
			validateToken(Token.tLBRACE);
			validateIdentifier("you");
			validateToken(Token.tASSIGN);
			validateIdentifier("dumb");
			validateToken(Token.tSEMI);
			validateToken(Token.tRBRACE);
			validateToken(Token.tSEMI);
			validateEOF();

			initializeScanner("#define ON 7\n#if defined(ON)\nint itsOn = ON;\n#endif");
			validateToken(Token.t_int);
			validateBalance(1);
			validateIdentifier("itsOn");
			validateToken(Token.tASSIGN);
			validateInteger("7");
			validateToken(Token.tSEMI);
			validateEOF();
			validateBalance();

			initializeScanner("#if defined( NOTHING ) \nint x = NOTHING;\n#endif");
			validateEOF();
			validateBalance();
			
			
				
			

		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}
	}

	public void testQuickScan() throws Parser.EndOfFile
	{
		try
		{
			initializeScanner( "#if X + 5 < 7\n  int found = 1;\n#endif" );
			scanner.setQuickScan( true );
			validateToken( Token.t_int ); 
			validateIdentifier( "found" ); 
			validateToken( Token.tASSIGN ); 
			validateInteger( "1"); 
			validateToken( Token.tSEMI );
			validateEOF(); 
			 	
		} 
		catch( ScannerException se )
		{
			fail( EXCEPTION_THROWN + se.getMessage() );
		}
		
		try
		{
			initializeScanner( "#if 0\n  int error = 666;\n#endif" ); 
			scanner.setQuickScan( true ); 
			validateEOF(); 
		}
		catch( ScannerException se )
		{
			fail( EXCEPTION_THROWN + se.getMessage() );
		}
		
	}

	public void testInclusions()
	{
		try
		{
			if (doIncludeStdio)
			{
				initializeScanner("#include <stdio.h>");
				prepareForStdio();
				int count= fullyTokenize();
				if (verbose)
					System.out.println(
						"For stdio.h, Scanner produced " + count + " tokens");
				validateBalance();

				initializeScanner("#include \\\n<\\\nstdio.h   \\\n>");
				prepareForStdio();
				count= fullyTokenize();
				if (verbose)
					System.out.println(
						"For stdio.h, Scanner produced " + count + " tokens");
			}

			if (doIncludeWindowsH)
			{
				initializeScanner("#include <Windows.h>");
				prepareForWindowsH();
				int count= fullyTokenize();
				if (verbose)
					System.out.println(
						"For Windows.h, Scanner produced "
							+ scanner.getCount()
							+ " tokens");
				validateBalance();
			}

			if (doIncludeWinUserH)
			{
				initializeScanner("#include <WinUser.rh>");
				prepareForWindowsRH();
				validateEOF();
				validateBalance();
				if (verbose)
					System.out.println(
						"For WinUser.rh, Scanner produced "
							+ scanner.getCount()
							+ " tokens");
			}
		}
		catch (Exception e)
		{
			fail(EXCEPTION_THROWN + e.toString());
		}

	}

	public void testOtherPreprocessorCommands()
	{
		try
		{
			initializeScanner("#\n#\t\n#define MAX_SIZE 1024\n#\n#  ");
			validateEOF();
			validateDefinition("MAX_SIZE", "1024");
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
					initializeScanner("#  ape");
					break;
				case 1 :
					initializeScanner("#  #");
					break;
				case 2 :
					initializeScanner("#  32");
					break;
				case 3 :
					initializeScanner("#  defines");
					break;
			}

			try
			{
				validateEOF();
				fail(EXPECTED_FAILURE);
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

	public void validateIdentifier(String expectedImage) throws ScannerException
	{
		try {
			Token t= scanner.nextToken();
			assertTrue(t.type == Token.tIDENTIFIER);
			assertTrue(t.image.equals(expectedImage));
		} catch (Parser.EndOfFile e) {
			assertTrue(false);
		}
	}

	public void validateInteger(String expectedImage) throws ScannerException
	{
		try {
			Token t= scanner.nextToken();
			assertTrue(t.type == Token.tINTEGER);
			assertTrue(t.image.equals(expectedImage));
		} catch (Parser.EndOfFile e) {
			assertTrue(false);
		}
	}
	
	public void validateFloatingPointLiteral(String expectedImage) throws ScannerException
	{
		try {
			Token t= scanner.nextToken();
			assertTrue(t.type == Token.tFLOATINGPT);
			assertTrue(t.image.equals(expectedImage));
		} catch (Parser.EndOfFile e) {
			assertTrue(false);
		}
	}
	
	public void validateChar( char expected )throws ScannerException
	{
		try {
			Token t= scanner.nextToken();
			assertTrue(t.getType() == Token.tCHAR );
			Character c = new Character( expected ); 
			assertEquals( t.getImage(), c.toString() ); 
		} catch (Parser.EndOfFile e) {
			assertTrue(false);
		}		
	}
	public void validateChar( String expected ) throws ScannerException
	{
		try {
			Token t= scanner.nextToken();
			assertTrue(t.getType() == Token.tCHAR );
			assertEquals( t.getImage(), expected ); 
		} catch (Parser.EndOfFile e) {
			assertTrue(false);
		}		
	}

	public void validateString( String expectedImage ) throws ScannerException
	{
		validateString( expectedImage, false );
	}

	public void validateString(String expectedImage, boolean lString ) throws ScannerException
	{
		try {
			Token t= scanner.nextToken();
			if( lString )
				assertTrue(t.getType() == Token.tLSTRING);
			else
				assertTrue(t.getType() == Token.tSTRING);
			assertTrue(t.getImage().equals(expectedImage));
		} catch (Parser.EndOfFile e) {
			assertTrue(false);
		}
	}

	public void validateToken(int tokenType) throws ScannerException
	{
		try {
			Token t= scanner.nextToken();
			assertTrue(t.type == tokenType);
		} catch (Parser.EndOfFile e) {
			assertTrue(false);
		}
	}

	public void validateBalance(int expected)
	{
		assertTrue(scanner.getDepth() == expected);
	}

	public void validateBalance()
	{
		assertTrue(scanner.getDepth() == 0);
	}

	public void validateEOF() throws ScannerException
	{
		try {
			assertNull(scanner.nextToken());
		} catch (Parser.EndOfFile e) {
		}
	}

	public void validateDefinition(String name, String value)
	{
		String definition= null;
		definition= (String) scanner.getDefinition(name);
		assertNotNull(definition);
		assertTrue(definition.trim().equals(value));
	}

	public void validateDefinition(String name, int value)
	{
		String definition= null;
		definition= (String) scanner.getDefinition(name);
		assertNotNull(definition);
		int intValue= (Integer.valueOf((String) definition)).intValue();
		assertEquals(value, intValue);
	}

	public void validateAsUndefined(String name)
	{
		assertNull(scanner.getDefinition(name));
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
		initializeScanner( "X::X( const X & rtg_arg ) : U( rtg_arg ) , Z( rtg_arg.Z ) , br( rtg_arg.br ){}" );
		validateIdentifier("X");
		validateToken( Token.tCOLONCOLON);
		validateIdentifier("X");
		validateToken( Token.tLPAREN );
		validateToken( Token.t_const );
		validateIdentifier("X");
		validateToken( Token.tAMPER );
		validateIdentifier( "rtg_arg");
		validateToken( Token.tRPAREN );  
		validateToken( Token.tCOLON );
		validateIdentifier( "U");
		validateToken( Token.tLPAREN );
		validateIdentifier( "rtg_arg");
		validateToken( Token.tRPAREN );
		validateToken( Token.tCOMMA );
		validateIdentifier( "Z");
		validateToken( Token.tLPAREN );
		validateIdentifier( "rtg_arg");
		validateToken( Token.tDOT );
		validateIdentifier( "Z");
		validateToken( Token.tRPAREN );
		validateToken( Token.tCOMMA );
		validateIdentifier( "br");
		validateToken( Token.tLPAREN );
		validateIdentifier( "rtg_arg");
		validateToken( Token.tDOT );
		validateIdentifier( "br");
		validateToken( Token.tRPAREN );
		validateToken( Token.tLBRACE);
		validateToken( Token.tRBRACE);
		validateEOF();
	}

	public void testBug35892()
	{
		try
		{
			initializeScanner( "'c'" ); 
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
		buffer.append( "\n\n");
		initializeScanner( buffer.toString());
		validateString( "\\\"\\\\");
	}

	public void testConditionalWithBraces()
	{
		try
		{
			for( int i = 0; i < 4; ++i )
			{
				initializeScanner( "int foobar(int a) { if(a == 0) {\n#ifdef THIS\n} else {}\n#elif THAT\n} else {}\n#endif\nreturn 0;}" );
				switch( i )
				{
					case 0:
						scanner.addDefinition( "THIS", "1");
						scanner.addDefinition( "THAT", "1" );  
						break; 
					case 1:
						scanner.addDefinition( "THIS", "1");
						scanner.addDefinition( "THAT", "0" );  
						break; 						
					case 2:
						scanner.addDefinition( "THAT", "1" );
						break; 
					case 3: 
						scanner.addDefinition( "THAT", "0" );
						break;
				}
					
				validateToken( Token.t_int ); 
				validateIdentifier( "foobar"); 
				validateToken( Token.tLPAREN ); 
				validateToken( Token.t_int ); 
				validateIdentifier( "a" ); 
				validateToken( Token.tRPAREN ); 
				validateToken( Token.tLBRACE ); 
				validateToken( Token.t_if ); 
				validateToken( Token.tLPAREN );
				validateIdentifier( "a" );
				validateToken( Token.tEQUAL );
				validateInteger( "0" );
				validateToken( Token.tRPAREN );
				validateToken( Token.tLBRACE );
				
				if( i <= 1 )
				{
					validateToken( Token.tRBRACE ); 
					validateToken( Token.t_else ); 
					validateToken( Token.tLBRACE );
					validateToken( Token.tRBRACE );
				}
					
				if( i == 2 )
				{
					validateToken( Token.tRBRACE ); 
					validateToken( Token.t_else ); 
					validateToken( Token.tLBRACE );
					validateToken( Token.tRBRACE );
				}
					
				validateToken( Token.t_return ); 
				validateInteger( "0"); 
				validateToken( Token.tSEMI ); 
				validateToken( Token.tRBRACE ); 
				validateEOF();
			}
		} catch( ScannerException se )
		{
			fail(EXCEPTION_THROWN + se.toString());			
		}
	}
	
	public void testNestedRecursiveDefines() throws Exception
	{
		initializeScanner( "#define C B A\n#define B C C\n#define A B\nA" );
		
		validateIdentifier("B");
		validateDefinition("A", "B");
		validateDefinition("B", "C C");
		validateDefinition("C", "B A");
		validateIdentifier("A");
		validateIdentifier("B");
		validateIdentifier("A");
		validateEOF();
	}
	
	public void testBug36316() throws Exception
	{
		initializeScanner( "#define A B->A\nA" );
	
		validateIdentifier("B");
		validateDefinition("A", "B->A");
		validateToken(Token.tARROW);
		validateIdentifier("A");
		validateEOF();
	}
	
	public void testBug36434() throws Exception
	{
		initializeScanner( "#define X(Y)");
		validateEOF();
		IMacroDescriptor macro = (IMacroDescriptor)scanner.getDefinition( "X" );
		assertNotNull( macro ); 
		assertEquals( macro.getParameters().size(), 1 );
		assertEquals( (String)macro.getParameters().get(0), "Y" );
		assertEquals( macro.getTokenizedExpansion().size(), 0 );
	}
	
	public void testBug36047() throws Exception
	{
		StringWriter writer = new StringWriter(); 
		writer.write( "# define MAD_VERSION_STRINGIZE(str)	#str\n" ); 
		writer.write( "# define MAD_VERSION_STRING(num)	MAD_VERSION_STRINGIZE(num)\n" ); 
		writer.write( "# define MAD_VERSION		MAD_VERSION_STRING(MAD_VERSION_MAJOR) \".\" \\\n" );
		writer.write( "                         MAD_VERSION_STRING(MAD_VERSION_MINOR) \".\" \\\n" );
		writer.write( "                         MAD_VERSION_STRING(MAD_VERSION_PATCH) \".\" \\\n" );
		writer.write( "                         MAD_VERSION_STRING(MAD_VERSION_EXTRA)\n" );
		writer.write( "# define MAD_VERSION_MAJOR 2\n" );
		writer.write( "# define MAD_VERSION_MINOR 1\n" );
		writer.write( "# define MAD_VERSION_PATCH 3\n" );
		writer.write( "# define MAD_VERSION_EXTRA boo\n" );
		writer.write( "MAD_VERSION\n" );
		initializeScanner( writer.toString() );
		  
		validateString( "2.1.3.boo" );
		
		validateEOF(); 
	}
	
	public void testBug36475() throws Exception
	{
		StringWriter writer = new StringWriter(); 
		writer.write( " \"A\" \"B\" \"C\" " ); 
		
		initializeScanner( writer.toString() );
		  
		validateString( "ABC" );
		validateEOF(); 
	}
	
	public void testBug36509() throws Exception 
	{ 
		StringWriter writer = new StringWriter(); 
		writer.write("#define debug(s, t) printf(\"x\" # s \"= %d, x\" # t \"= %s\", \\\n"); 
		writer.write("                    x ## s, x ## t) \n"); 
		writer.write("debug(1, 2);");
		   
		initializeScanner( writer.toString() ); 
		//printf("x1=%d, x2= %s", x1, x2); 
		validateIdentifier( "printf" ); 
		validateToken( Token.tLPAREN ); 
		validateString("x1= %d, x2= %s"); 
		validateToken(Token.tCOMMA); 
		validateIdentifier("x1"); 
		validateToken(Token.tCOMMA); 
		validateIdentifier("x2"); 
		validateToken(Token.tRPAREN); 
		validateToken(Token.tSEMI); 
		validateEOF();
	}
	
	public void testBug36695() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write("\'\\4\'  \'\\n\'");
		initializeScanner( writer.toString() );
		
		validateChar( "\\4" );
		validateChar( "\\n" );
		validateEOF();
	}
	
	public void testBug36521() throws Exception 
	{ 
		StringWriter writer = new StringWriter(); 
		writer.write("#define str(s)      # s\n"); 
		writer.write("fputs(str(strncmp(\"abc\\0d\", \"abc\", \'\\4\')\n"); 
		writer.write("        == 0), s);\n"); 
		                         
		initializeScanner( writer.toString() ); 
		validateIdentifier("fputs"); 
		validateToken(Token.tLPAREN); 
		validateString("strncmp ( \\\"abc\\\\0d\\\" , \\\"abc\\\" , '\\\\4' ) == 0"); 
		validateToken(Token.tCOMMA); 
		validateIdentifier("s"); 
		validateToken(Token.tRPAREN); 
		validateToken(Token.tSEMI); 
	}
	
	public void testBug36770() throws Exception
	{
		StringWriter writer = new StringWriter();
		writer.write( "#define A 0\n" );
		writer.write( "#if ( A == 1 )\n");
		writer.write( "#  define foo 1\n");
		writer.write( "#else\n");
		writer.write( "# define foo 2\n");
		writer.write( "#endif\n");
		writer.write( "foo\n");
	
		initializeScanner( writer.toString() );
		validateInteger( "2" );
		validateEOF();
	}
		
}
