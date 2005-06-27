/**********************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.prefix;

import java.util.Arrays;
import java.util.Comparator;

import junit.framework.TestCase;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.scanner2.DOMScanner;
import org.eclipse.cdt.internal.core.parser.scanner2.FileCodeReaderFactory;
import org.eclipse.cdt.internal.core.parser.scanner2.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.IScannerExtensionConfiguration;

public class CompletionTestBase extends TestCase {

    private static final IParserLogService NULL_LOG = new NullLogService();

	protected ASTCompletionNode getCompletionNode(String code, ParserLanguage lang, boolean useGNUExtensions) throws ParserException {
        CodeReader codeReader = new CodeReader(code.toCharArray());
        ScannerInfo scannerInfo = new ScannerInfo();
        IScannerExtensionConfiguration configuration = null;
        if( lang == ParserLanguage.C )
            configuration = new GCCScannerExtensionConfiguration();
        else
            configuration = new GPPScannerExtensionConfiguration();
        IScanner scanner = new DOMScanner( codeReader, scannerInfo, ParserMode.COMPLETE_PARSE, lang, NULL_LOG, configuration, FileCodeReaderFactory.getInstance() );
        
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

	protected ASTCompletionNode getGPPCompletionNode(String code) throws ParserException {
		return getCompletionNode(code, ParserLanguage.CPP, true);
	}
	
	protected ASTCompletionNode getGCCCompletionNode(String code) throws ParserException {
		return getCompletionNode(code, ParserLanguage.C, true);
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
}
