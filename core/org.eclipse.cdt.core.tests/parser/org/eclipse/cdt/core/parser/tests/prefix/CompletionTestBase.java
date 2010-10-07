/**********************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.prefix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.ast2.AST2BaseTest;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class CompletionTestBase extends BaseTestCase {

    private static final IParserLogService NULL_LOG = new NullLogService();

	protected IASTCompletionNode getCompletionNode(String code, ParserLanguage lang, boolean useGNUExtensions) throws ParserException {
        FileContent codeReader = FileContent.create("<test-code>", code.trim().toCharArray());
        ScannerInfo scannerInfo = new ScannerInfo();
        IScanner scanner= AST2BaseTest.createScanner(codeReader, lang, ParserMode.COMPLETE_PARSE, scannerInfo);
        
        ISourceCodeParser parser = null;
        if( lang == ParserLanguage.CPP )
        {
            ICPPParserExtensionConfiguration config = null;
            if (useGNUExtensions)
            	config = new GPPParserExtensionConfiguration();
            else
            	config = new ANSICPPParserExtensionConfiguration();
            parser = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE,
                NULL_LOG,
                config );
        }
        else
        {
            ICParserExtensionConfiguration config = null;

            if (useGNUExtensions)
            	config = new GCCParserExtensionConfiguration();
            else
            	config = new ANSICParserExtensionConfiguration();
            
            parser = new GNUCSourceParser( scanner, ParserMode.COMPLETE_PARSE, 
                NULL_LOG, config );
        }
        
		scanner.setContentAssistMode(code.length());
        parser.parse();
		return parser.getCompletionNode();
    }

	protected IASTCompletionNode getGPPCompletionNode(String code) throws ParserException {
		return getCompletionNode(code, ParserLanguage.CPP, true);
	}
	
	protected IASTCompletionNode getGCCCompletionNode(String code) throws ParserException {
		return getCompletionNode(code, ParserLanguage.C, true);
	}
	
	protected void checkCompletion(String code, boolean isCpp, String[] expected) throws ParserException {
		checkCompletion(code, true, isCpp, expected);
	}

	protected void checkNonPrefixCompletion(String code, boolean isCpp, String[] expected) throws ParserException {
		checkCompletion(code, false, isCpp, expected);
	}

	private void checkCompletion(String code, boolean isPrefix, boolean isCpp, String[] expected) throws ParserException {
				IASTCompletionNode node = isCpp ? getGPPCompletionNode(code) : getGCCCompletionNode(code);
		assertNotNull(node);
		List<IBinding> bindings= proposeBindings(node, isPrefix);
		String[] names= getSortedNames(bindings);
		int len= Math.min(expected.length, names.length);
		for (int i = 0; i < len; i++) {
			assertEquals(expected[i], names[i]);
		}
		assertEquals(expected.length, names.length);
	}
	
	private static class BindingsComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			IBinding b1 = (IBinding)o1;
			IBinding b2 = (IBinding)o2;
			return b1.getName().compareTo(b2.getName());
		}
	}
	
	private static BindingsComparator bindingsComparator  = new BindingsComparator();
	
	protected IBinding[] sortBindings(IBinding[] bindings) {
		Arrays.sort(bindings, bindingsComparator);
		return bindings;
	}
	
	protected String getAboveComment() throws IOException {
		return getContents(1)[0].toString();
	}
	
	protected StringBuffer[] getContents(int sections) throws IOException {
		CTestPlugin plugin = CTestPlugin.getDefault();
		if (plugin == null)
			throw new AssertionFailedError("This test must be run as a JUnit plugin test");
		return TestSourceReader.getContentsForTest(plugin.getBundle(), "parser", getClass(), getName(), sections);
	}
	
	protected List<IBinding> proposeBindings(IASTCompletionNode completionNode, boolean isPrefix) {
		List<IBinding> proposals = new ArrayList<IBinding>();
		boolean handleMacros= false;
		IASTName[] names = completionNode.getNames();

		for (int i = 0; i < names.length; ++i) {
			if (names[i].getTranslationUnit() == null)
				// The node isn't properly hooked up, must have backtracked out of this node
				continue;

			IASTCompletionContext astContext = names[i].getCompletionContext();
			if (astContext == null) {
				continue;
			} 
			IBinding[] bindings = astContext.findBindings(names[i], isPrefix);
			if (bindings != null)
				for (int j = 0; j < bindings.length; ++j)
					proposals.add(bindings[j]);
		}
		return proposals;
	}
	
	protected String[] getSortedNames(List<IBinding> bindings) {
		String[] result= new String[bindings.size()];
		Iterator<IBinding> it= bindings.iterator();
		for (int i = 0; i < result.length; i++) {
			result[i]= it.next().getName();
		}
		Arrays.sort(result);
		return result;
	}
}
