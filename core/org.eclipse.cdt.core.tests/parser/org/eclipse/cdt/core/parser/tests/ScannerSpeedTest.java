/*
 * Created on Jun 8, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.core.parser.tests;

import java.util.Collections;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerException;

/**
 * @author Doug Schaefer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ScannerSpeedTest extends SpeedTest {
	
	private static final ISourceElementRequestor CALLBACK = new NullSourceElementRequestor();

	public static void main(String[] args) {
		try {
			new ScannerSpeedTest().runTest(1);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void test() throws Exception {
		runTest(10);
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
		
		if (n > 0) {
			System.out.println("Average Time: " + (totalTime / (n - 1)) + " millisecs");
		}
	}

	/**
	 * @param path
	 * @param quick TODO
	 */
	protected long testScan(CodeReader reader, boolean quick, IScannerInfo info, ParserLanguage lang) throws Exception {
		ParserMode mode = quick ? ParserMode.QUICK_PARSE : ParserMode.COMPLETE_PARSE;
		IScanner scanner = ParserFactory.createScanner(reader, info, mode, lang, CALLBACK, null, Collections.EMPTY_LIST ); 
		long startTime = System.currentTimeMillis();
		try {
			while (true) {
				try {
					if (scanner.nextToken() == null)
						break;
				} catch (ScannerException e) {
				}
			}
		} catch (EndOfFileException e2) {
		}
		long totalTime = System.currentTimeMillis() - startTime;
		System.out.println( "Resulting scan took " + totalTime + " millisecs " +
				scanner.getCount() + " tokens");
		return totalTime;
	}

}
