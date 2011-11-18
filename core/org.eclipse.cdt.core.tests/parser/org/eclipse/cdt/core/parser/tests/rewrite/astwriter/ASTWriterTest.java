/*******************************************************************************
 * Copyright (c) 2008, 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite.astwriter;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
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
import org.eclipse.cdt.core.parser.tests.rewrite.RewriteBaseTest;
import org.eclipse.cdt.core.parser.tests.rewrite.TestHelper;
import org.eclipse.cdt.core.parser.tests.rewrite.TestSourceFile;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationMap;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.core.resources.IFile;

/**
 * @author Guido Zgraggen
 */
public class ASTWriterTest extends RewriteBaseTest {
	private static final IParserLogService NULL_LOG = new NullLogService();
	
	private	IFile file;
	
	public ASTWriterTest(String name, ASTWriterTestSourceFile file) {
		super(name);
		fileMap.put(file.getName(), file);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		for (TestSourceFile testFile : fileMap.values()) {
			if (testFile.getSource().length() > 0) {
				file = importFile(testFile.getName(), testFile.getSource());
			}
		}
	}
	
	@Override
	protected void runTest() throws Throwable {
		file = project.getFile("ASTWritterTest.h"); //$NON-NLS-1$
		compareFiles(fileMap);
	}
	
	@Override
	protected void compareFiles(Map<String, TestSourceFile> testResourceFiles) throws Exception {
		for (String fileName : testResourceFiles.keySet()) {
			TestSourceFile testFile = testResourceFiles.get(fileName);
			String code = generateSource(testFile);
			assertEquals(TestHelper.unifyNewLines(testFile.getExpectedSource()),
					TestHelper.unifyNewLines(code + System.getProperty("line.separator"))); //$NON-NLS-1$
		}
	}
	
	public String generateSource(TestSourceFile testFile) throws Exception {
		IASTTranslationUnit unit = getParser(testFile).parse();
		NodeCommentMap commentMap = ASTCommenter.getCommentedNodeMap(unit);		
		ASTModificationMap map = new ASTModificationMap();
		map.getModificationsForNode(unit.getDeclarations()[0]);
		ASTWriter writer = new ASTWriter();
		return writer.write(unit, commentMap);
	}
	
	protected ISourceCodeParser getParser(TestSourceFile testFile) throws Exception {
        FileContent codeReader = FileContent.create(file);

        ScannerInfo scannerInfo = new ScannerInfo();
        ParserLanguage language = getLanguage(testFile);
    	boolean useGNUExtensions = getGNUExtension(testFile);
                
        IScanner scanner = AST2BaseTest.createScanner(codeReader, language, ParserMode.COMPLETE_PARSE, scannerInfo);
        
        ISourceCodeParser parser2 = null;
        if (language == ParserLanguage.CPP) {
            ICPPParserExtensionConfiguration config = null;
            if (useGNUExtensions) {
            	config = new GPPParserExtensionConfiguration();
            } else {
            	config = new ANSICPPParserExtensionConfiguration();
            }
            parser2 = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, config);
        } else {
            ICParserExtensionConfiguration config = null;

            if (useGNUExtensions) {
            	config = new GCCParserExtensionConfiguration();	
            } else {
            	config = new ANSICParserExtensionConfiguration();
            }
            
            parser2 = new GNUCSourceParser(scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, config);
        }
        return parser2;
	}
	
	private boolean getGNUExtension(TestSourceFile file) {
		if (file instanceof ASTWriterTestSourceFile)
			return ((ASTWriterTestSourceFile) file).isUseGNUExtensions();
		return false;
	}

	private ParserLanguage getLanguage(TestSourceFile file) {
		if (file instanceof ASTWriterTestSourceFile)
			return ((ASTWriterTestSourceFile) file).getParserLanguage();
		return ParserLanguage.CPP;
	}
}
