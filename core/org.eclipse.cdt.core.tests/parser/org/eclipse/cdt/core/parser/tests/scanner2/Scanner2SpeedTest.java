/*
 * Created on Jun 8, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.core.parser.tests.scanner2;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collections;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.parser.scanner2.Scanner2;

/**
 * @author Doug Schaefer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Scanner2SpeedTest extends SpeedTest2 {
	
	private static final ISourceElementRequestor CALLBACK = new NullSourceElementRequestor();
	private PrintStream stream;

	public static void main(String[] args) {
		try {
			PrintStream stream = null;
			if (args.length > 0)
				stream = new PrintStream(new FileOutputStream(args[0]));

			new Scanner2SpeedTest().runTest(stream, 1);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void test() throws Exception {
		runTest(10);
	}

	public void runTest(PrintStream stream, int n) throws Exception {
		this.stream = stream;
		runTest(n);
	}
	
	private void runTest(int n) throws Exception {
		String code = 
			"#include <windows.h>\n" +
			"#include <stdio.h>\n" +
			"#include <iostream>\n";
		
		CodeReader reader = new CodeReader(code.toCharArray());
		IScannerInfo info = getScannerInfo(false);
		long totalTime = 0;
		for (int i = 0; i < n; ++i) {
			long time = testScan(reader, false, info, ParserLanguage.CPP);
			if (i > 0)
				totalTime += time;
		}
		
		if (n > 1) {
			System.out.println("Average Time: " + (totalTime / (n - 1)) + " millisecs");
		}
	}

	/**
	 * @param path
	 * @param quick TODO
	 */
	protected long testScan(CodeReader reader, boolean quick, IScannerInfo info, ParserLanguage lang) throws Exception {
		ParserMode mode = quick ? ParserMode.QUICK_PARSE : ParserMode.COMPLETE_PARSE;
		Scanner2 scanner = createScanner(reader, info, mode, lang, CALLBACK, null, Collections.EMPTY_LIST ); 
		long startTime = System.currentTimeMillis();
		int count = 0;
		try {
			while (true) {
					IToken t = scanner.nextToken();
					
					if (stream != null)
						stream.println(t.getImage());
					
					if (t == null)
						break;
					++count;
				
			}
		} catch (EndOfFileException e2) {
		}
		long totalTime = System.currentTimeMillis() - startTime;
		System.out.println( "Resulting scan took " + totalTime + " millisecs " +
				count + " tokens");
		return totalTime;
	}


}
