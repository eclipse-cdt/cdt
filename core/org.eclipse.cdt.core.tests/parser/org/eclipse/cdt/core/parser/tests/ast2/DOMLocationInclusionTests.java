/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.io.InputStream;
import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.FileBasePluginTest;
import org.eclipse.cdt.internal.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.scanner2.DOMScanner;
import org.eclipse.cdt.internal.core.parser.scanner2.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.IScannerExtensionConfiguration;
import org.eclipse.core.resources.IFile;

/**
 * @author jcamelon
 */
public class DOMLocationInclusionTests extends FileBasePluginTest {

    private static final IScannerInfo SCANNER_INFO = new ScannerInfo();

    private static final IParserLogService NULL_LOG = new NullLogService();

    private static final ICodeReaderFactory factory = CDOM.getInstance()
            .getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES);

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * @param name
     * @param className
     */
    public DOMLocationInclusionTests(String name) {
        super(name, DOMLocationInclusionTests.class);
    }

    protected IASTTranslationUnit parse(IFile code, ParserLanguage language)
            throws Exception {
        return parse(code, language, SCANNER_INFO);
    }

    protected IASTTranslationUnit parse(IFile code, ParserLanguage language,
            IScannerInfo s) throws Exception {
        InputStream stream = code.getContents();
        IScanner scanner = new DOMScanner(new CodeReader(code.getLocation()
                .toOSString(), stream), s, ParserMode.COMPLETE_PARSE, language,
                NULL_LOG, getScannerConfig(language), factory);
        ISourceCodeParser parser = null;
        if (language == ParserLanguage.CPP) {
            parser = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE,
                    NULL_LOG, new GPPParserExtensionConfiguration());
        } else {
            parser = new GNUCSourceParser(scanner, ParserMode.COMPLETE_PARSE,
                    NULL_LOG, new GCCParserExtensionConfiguration());
        }
        stream.close();
        IASTTranslationUnit parseResult = parser.parse();

        if (parser.encounteredError())
            throw new ParserException("FAILURE"); //$NON-NLS-1$

        if (language == ParserLanguage.C) {
            IASTProblem[] problems = CVisitor.getProblems(parseResult);
            assertEquals(problems.length, 0);
        } else if (language == ParserLanguage.CPP) {
            IASTProblem[] problems = CPPVisitor.getProblems(parseResult);
            assertEquals(problems.length, 0);
        }

        return parseResult;
    }

    /**
     * @param language
     * @return
     */
    private IScannerExtensionConfiguration getScannerConfig(
            ParserLanguage language) {
        if (language == ParserLanguage.CPP)
            return new GPPScannerExtensionConfiguration();
        return new GCCScannerExtensionConfiguration();
    }

    /**
     * @param pathEndsWith
     *            TODO
     * @param offset
     * @param length
     * @param declarator
     */
    private void assertSoleFileLocation(IASTNode n, String pathEndsWith,
            int offset, int length) {
        IASTNodeLocation[] locations = n.getNodeLocations();
        assertEquals(locations.length, 1);
        IASTFileLocation nodeLocation = (IASTFileLocation) locations[0];
        assertTrue(nodeLocation.getFileName().endsWith(pathEndsWith));
        assertEquals(offset, nodeLocation.getNodeOffset());
        assertEquals(length, nodeLocation.getNodeLength());
    }

    public void testSimpleInclusion() throws Exception {
        String foo = "int FOO;"; //$NON-NLS-1$
        String code = "int bar;\n#include \"foo.h\"\n"; //$NON-NLS-1$

        importFile("foo.h", foo); //$NON-NLS-1$
        IFile cpp = importFile("code.cpp", code); //$NON-NLS-1$

        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            IASTTranslationUnit tu = parse(cpp, p); //$NON-NLS-1$
            IASTDeclaration[] declarations = tu.getDeclarations();
            assertEquals(declarations.length, 2);
            IASTSimpleDeclaration bar = (IASTSimpleDeclaration) declarations[0];
            IASTSimpleDeclaration FOO = (IASTSimpleDeclaration) declarations[1];
            assertSoleFileLocation(bar,
                    "code.cpp", code.indexOf("int"), code.indexOf(";") + 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            assertSoleFileLocation(FOO,
                    "foo.h", foo.indexOf("int"), foo.indexOf(";") + 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            IASTPreprocessorIncludeStatement[] incs = tu.getIncludeDirectives();
            assertNotNull(incs);
            assertEquals(incs.length, 1);
            assertSoleFileLocation(
                    incs[0],
                    "code.cpp", code.indexOf("#inc"), code.indexOf(".h\"\n") + ".h\"".length() - code.indexOf("#inc")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

        }
    }

    public void testSimpleInclusion2() throws Exception {
        String foo = "int FOO;"; //$NON-NLS-1$
        String code = "int bar;\n#include \"foo.h\"\nfloat byob;\n"; //$NON-NLS-1$

        importFile("foo.h", foo); //$NON-NLS-1$
        IFile cpp = importFile("code.cpp", code); //$NON-NLS-1$

        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            IASTTranslationUnit tu = parse(cpp, p); //$NON-NLS-1$
            IASTDeclaration[] declarations = tu.getDeclarations();
            assertEquals(declarations.length, 3);
            IASTSimpleDeclaration bar = (IASTSimpleDeclaration) declarations[0];
            IASTSimpleDeclaration FOO = (IASTSimpleDeclaration) declarations[1];
            IASTSimpleDeclaration byob = (IASTSimpleDeclaration) declarations[2];
            assertSoleFileLocation(
                    bar,
                    "code.cpp", code.indexOf("int"), code.indexOf("r;") + 2 - code.indexOf("int")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            assertSoleFileLocation(
                    FOO,
                    "foo.h", foo.indexOf("int"), foo.indexOf(";") + 1 - foo.indexOf("int")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            assertSoleFileLocation(
                    byob,
                    "code.cpp", code.indexOf("float"), code.indexOf("b;") + 2 - code.indexOf("float")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            IASTPreprocessorIncludeStatement[] incs = tu.getIncludeDirectives();
            assertNotNull(incs);
            assertEquals(incs.length, 1);
            assertSoleFileLocation(
                    incs[0],
                    "code.cpp", code.indexOf("#inc"), code.indexOf(".h\"\n") + ".h\"".length() - code.indexOf("#inc")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }
    }

    public void testMacrosInIncludeFile() throws Exception {
        String c_file_code = "#define X 4\n\n#include \"blarg.h\"\n\n#define POST_INCLUDE\n\n"; //$NON-NLS-1$
        String h_file_code = "#ifndef _BLARG_H_\r\n#define _BLARG_H_\r\n// macro\r\n#define PRINT(s,m)  printf(s,m)\r\n#endif //_BLARG_H_\r\n"; //$NON-NLS-1$
        importFile("blarg.h", h_file_code); //$NON-NLS-1$
        IFile c_file = importFile("blarg.c", c_file_code); //$NON-NLS-1$
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            IASTTranslationUnit tu = parse(c_file, p); //$NON-NLS-1$
            assertEquals(tu.getDeclarations().length, 0);
            IASTPreprocessorMacroDefinition[] macroDefinitions = tu
                    .getMacroDefinitions();
            assertNotNull(macroDefinitions);
            assertEquals(macroDefinitions.length, 4);
            assertSoleFileLocation(
                    macroDefinitions[0],
                    "blarg.c", c_file_code.indexOf("#define"), c_file_code.indexOf("4") + 1 - c_file_code.indexOf("#define")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            assertSoleFileLocation(macroDefinitions[0].getName(),
                    "blarg.c", c_file_code.indexOf("X"), 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            assertSoleFileLocation(
                    macroDefinitions[1],
                    "blarg.h", h_file_code.indexOf("#define _BLARG_H_"), "#define _BLARG_H_\r".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(
                    macroDefinitions[1].getName(),
                    "blarg.h", h_file_code.indexOf("e _BLARG_H_") + 2, "_BLARG_H_".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(
                    macroDefinitions[2],
                    "blarg.h", h_file_code.indexOf("#define PRINT(s,m)  printf(s,m)\r"), "#define PRINT(s,m)  printf(s,m)".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            assertSoleFileLocation(macroDefinitions[2].getName(),
                    "blarg.h", h_file_code.indexOf("PRINT"), "PRINT".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            assertSoleFileLocation(
                    macroDefinitions[3],
                    "blarg.c", c_file_code.indexOf("#define POST_INCLUDE"), "#define POST_INCLUDE".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(
                    macroDefinitions[3].getName(),
                    "blarg.c", c_file_code.indexOf("POST_INCLUDE"), "POST_INCLUDE".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    public void testBug84451() throws Exception {
        String header1_code = "int x;\n"; //$NON-NLS-1$
        String header2_code = "int y;\n"; //$NON-NLS-1$
        String cpp_code = "#include \"header1.h\"\n#include \"header2.h\"\nint z;\n"; //$NON-NLS-1$
        importFile("header1.h", header1_code); //$NON-NLS-1$ 
        importFile("header2.h", header2_code); //$NON-NLS-1$ 
        IFile f = importFile("source.c", cpp_code); //$NON-NLS-1$
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            IASTTranslationUnit tu = parse(f, p);
            IASTDeclaration[] declarations = tu.getDeclarations();
            IASTPreprocessorIncludeStatement[] includeDirectives = tu
                    .getIncludeDirectives();
            assertSoleFileLocation(
                    includeDirectives[0],
                    "source.c", cpp_code.indexOf("#include \"header1.h\""), "#include \"header1.h\"".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(declarations[0],
                    "header1.h", 0, "int x;".length()); //$NON-NLS-1$ //$NON-NLS-2$
            assertSoleFileLocation(declarations[1],
                    "header2.h", 0, "int y;".length()); //$NON-NLS-1$ //$NON-NLS-2$
            assertSoleFileLocation(
                    includeDirectives[1],
                    "source.c", cpp_code.indexOf("#include \"header2.h\""), "#include \"header2.h\"".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(declarations[2],
                    "source.c", cpp_code.indexOf("int z;"), "int z;".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            IASTTranslationUnit.IDependencyTree tree = tu.getDependencyTree();
            assertEquals(tree.getInclusions().length, 2);

        }
    }

    public void testMacrosInIncludeFile2() throws Exception {
        String c_file_code = "#define X 4\n\n#include \"blarg.h\"\n\n#define POST_INCLUDE\n#include \"second.h\"\n#define POST_SECOND\n"; //$NON-NLS-1$
        String h_file_code = "#ifndef _BLARG_H_\r\n#define _BLARG_H_\r\n//macro\r\n#define PRINT(s,m) printf(s,m)\r\n#endif //_BLARG_H_\r\n"; //$NON-NLS-1$
        String h_file2_code = "#ifndef _SECOND_H_ \n#define _SECOND_H_\n#endif\n"; //$NON-NLS-1$
        importFile("blarg.h", h_file_code); //$NON-NLS-1$
        importFile("second.h", h_file2_code); //$NON-NLS-1$
        IFile c_file = importFile("blarg.c", c_file_code); //$NON-NLS-1$
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            IASTTranslationUnit tu = parse(c_file, p); //$NON-NLS-1$
            assertEquals(tu.getDeclarations().length, 0);
            IASTPreprocessorMacroDefinition[] macroDefinitions = tu
                    .getMacroDefinitions();
            assertNotNull(macroDefinitions);
            assertEquals(macroDefinitions.length, 6);
            assertSoleFileLocation(
                    macroDefinitions[0],
                    "blarg.c", c_file_code.indexOf("#define"), c_file_code.indexOf("4") + 1 - c_file_code.indexOf("#define")); //$NON-NLS-1$ //$NON-NLS-2$    //$NON-NLS-3$ //$NON-NLS-4$
            assertSoleFileLocation(macroDefinitions[0].getName(),
                    "blarg.c", c_file_code.indexOf("X"), 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$    //$NON-NLS-4$
            assertSoleFileLocation(
                    macroDefinitions[1],
                    "blarg.h", h_file_code.indexOf("#define _BLARG_H_"), "#define _BLARG_H_\r".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(
                    macroDefinitions[1].getName(),
                    "blarg.h", h_file_code.indexOf("e _BLARG_H_") + 2, "_BLARG_H_".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(
                    macroDefinitions[2],
                    "blarg.h", h_file_code.indexOf("#define PRINT(s,m) printf(s,m)\r"), "#define PRINT(s,m) printf(s,m)".length()); //$NON-NLS-1$ //$NON-NLS-2$    //$NON-NLS-3$ //$NON-NLS-4$
            assertSoleFileLocation(macroDefinitions[2].getName(),
                    "blarg.h", h_file_code.indexOf("PRINT"), "PRINT".length()); //$NON-NLS-1$    //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            assertSoleFileLocation(
                    macroDefinitions[3],
                    "blarg.c", c_file_code.indexOf("#define POST_INCLUDE"), "#define POST_INCLUDE".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(
                    macroDefinitions[3].getName(),
                    "blarg.c", c_file_code.indexOf("POST_INCLUDE"), "POST_INCLUDE".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(
                    macroDefinitions[4],
                    "second.h", h_file2_code.indexOf("#define _SECOND_H_"), "#define _SECOND_H_".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(
                    macroDefinitions[5],
                    "blarg.c", c_file_code.indexOf("#define POST_SECOND"), "#define POST_SECOND".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$    //$NON-NLS-4$

        }
    }

    public void testBug90851() throws Exception {
        IFile imacro_file = importFile( "macro.h", "#define BEAST 666\n"); //$NON-NLS-1$ //$NON-NLS-2$
        StringBuffer buffer = new StringBuffer();
        buffer.append( "#ifndef _INCLUDE_H_\n" ); //$NON-NLS-1$
        buffer.append( "#define _INCLUDE_H_\n" ); //$NON-NLS-1$
        buffer.append( "typedef void (*vfp)();\n" ); //$NON-NLS-1$
        buffer.append( "typedef int (*ifp)();\n" ); //$NON-NLS-1$
        buffer.append( "struct Include {\n" ); //$NON-NLS-1$
        buffer.append( "int i;\n" ); //$NON-NLS-1$
        buffer.append( "};\n" ); //$NON-NLS-1$
        buffer.append( "#endif /*_INCLUDE_H_*/\n" ); //$NON-NLS-1$
        final String inc_file_code = buffer.toString();
        IFile include_file = importFile( "include.h", inc_file_code ); //$NON-NLS-1$ //$NON-NLS-2$
        String [] macros =  { imacro_file.getLocation().toOSString() };
        String [] includes = { include_file.getLocation().toOSString() }; 
        IExtendedScannerInfo scannerInfo = new ExtendedScannerInfo( Collections.EMPTY_MAP, EMPTY_STRING_ARRAY,  macros, includes  );
        IFile code = importFile( "main.c", "int main() { return BEAST * sizeof( Include ); } "); //$NON-NLS-1$ //$NON-NLS-2$
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            IASTTranslationUnit tu = parse(code, p, scannerInfo ); //$NON-NLS-1$
            IASTPreprocessorMacroDefinition [] macro_defs = tu.getMacroDefinitions();
            assertEquals( macro_defs.length, 2 );
            IASTPreprocessorMacroDefinition BEAST = macro_defs[0];
            assertEquals( BEAST.getName().toString(), "BEAST"); //$NON-NLS-1$
            IASTPreprocessorMacroDefinition INCLUDE_H = macro_defs[1];
            final IASTNodeLocation[] nodeLocations = INCLUDE_H.getName().getNodeLocations();
            assertEquals( nodeLocations.length, 1 );
            final IASTFileLocation flatLoc = INCLUDE_H.getName().getFileLocation();
            assertNotNull( flatLoc );
            assertEquals( include_file.getLocation().toOSString(), flatLoc.getFileName() );
            assertEquals( inc_file_code.indexOf( "#define _INCLUDE_H_") + "#define ".length(), flatLoc.getNodeOffset() ); //$NON-NLS-1$ //$NON-NLS-2$
            assertEquals( "_INCLUDE_H_".length(), flatLoc.getNodeLength() ); //$NON-NLS-1$
        }        
    }
    
    public void testIProblemLocation() throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "#include <not_found.h>\n"); //$NON-NLS-1$
        buffer.append( "int x,y,z;"); //$NON-NLS-1$
        String code = buffer.toString();
        IFile f = importFile( "blah.c", code ); //$NON-NLS-1$
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            IASTTranslationUnit tu = parse( f, p ); //$NON-NLS-1$
            IASTProblem [] prbs = tu.getPreprocessorProblems();
            assertEquals( prbs.length, 1 );
            IASTNodeLocation [] locs = prbs[0].getNodeLocations();
            assertEquals( locs.length, 1 );
            IASTFileLocation fileLoc = (IASTFileLocation) locs[0];
            assertEquals( code.indexOf( "#include" ), fileLoc.getNodeOffset() ); //$NON-NLS-1$
            assertEquals( "#include <not_found.h>\n".length(), fileLoc.getNodeLength() ); //$NON-NLS-1$  
        }
        
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DOMLocationInclusionTests.class);
        suite.addTest(new DOMLocationInclusionTests("cleanupProject")); //$NON-NLS-1$
        return suite;
    }
}