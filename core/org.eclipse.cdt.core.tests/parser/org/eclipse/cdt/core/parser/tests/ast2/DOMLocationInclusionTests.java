/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.io.File;
import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IParserConfiguration;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.SavedCodeReaderFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.content.IContentType;

/**
 * @author jcamelon
 */
public class DOMLocationInclusionTests extends AST2FileBasePluginTest {

    public DOMLocationInclusionTests() {
	}

	public DOMLocationInclusionTests(String name, Class className) {
		super(name, className);
	}

	public void testBug97967() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("#ifndef _INCLUDE_H_\n"); //$NON-NLS-1$
        buffer.append("#define _INCLUDE_H_\n"); //$NON-NLS-1$
        buffer.append("typedef void (*vfp)();\n"); //$NON-NLS-1$
        buffer.append("typedef int (*ifp)();\n"); //$NON-NLS-1$
        buffer.append("struct Include {\n"); //$NON-NLS-1$
        buffer.append("int i;\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("#endif /*_INCLUDE_H_*/\n"); //$NON-NLS-1$
        String[] macros = { importFile(
                "macro.h", "#define JEDEN 1\n#define DVA 2\n#define TRI 3\n").getLocation().toOSString() }; //$NON-NLS-1$ //$NON-NLS-2$
        String[] includes = { importFile("include.h", buffer.toString()).getLocation().toOSString() }; //$NON-NLS-1$
        IExtendedScannerInfo scannerInfo = new ExtendedScannerInfo(
                Collections.EMPTY_MAP, EMPTY_STRING_ARRAY, macros, includes);
        String code = "int main() { return BEAST * sizeof( Include ); } "; //$NON-NLS-1$
        for (ParserLanguage p : ParserLanguage.values()) {
            String filename = (p == ParserLanguage.CPP) ? "main.cc" : "main.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile c = importFile(filename, code); 

            IASTTranslationUnit tu = parse(c, scannerInfo); 
            IASTFunctionDefinition fd = (IASTFunctionDefinition) tu.getDeclarations()[3];
            IASTFileLocation floc = fd.getFileLocation();
            assertEquals(floc.getNodeOffset(),
                    code.indexOf("int main() { return BEAST * sizeof( Include ); }")); //$NON-NLS-1$
            assertEquals(floc.getNodeLength(),
                    "int main() { return BEAST * sizeof( Include ); }".length()); //$NON-NLS-1$
        }
    }

    public void testBug101875() throws Exception {
        for (int i = 0; i < 4; ++i) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("#ifndef _BLAH_H_\n"); //$NON-NLS-1$
            buffer.append("#endif"); //$NON-NLS-1$
            if (i > 1)
                buffer.append(" /* _BLAH_H_ */"); //$NON-NLS-1$
            if ((i % 2) == 1)
                buffer.append("\n"); //$NON-NLS-1$
            importFile("blah.h", buffer.toString()); //$NON-NLS-1$
            buffer = new StringBuffer();
            buffer.append("#include \"blah.h\"\n"); //$NON-NLS-1$
            buffer.append("/**\n"); //$NON-NLS-1$
            buffer.append(" * A type used by test functions.\n"); //$NON-NLS-1$
            buffer.append("*/\n"); //$NON-NLS-1$
            buffer.append("int SomeStructure;\n"); //$NON-NLS-1$
            String code = buffer.toString();

            for (ParserLanguage p : ParserLanguage.values()) {
                String filename = (p == ParserLanguage.CPP) ? "blah.cc" : "blah.c"; //$NON-NLS-1$ //$NON-NLS-2$
                IFile source = importFile(filename, code); 
                IASTTranslationUnit tu = parse(source); 
                IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) tu.getDeclarations()[0];
                assertSoleFileLocation(declaration.getDeclarators()[0], filename,
                        code.indexOf("SomeStructure"), "SomeStructure".length()); //$NON-NLS-1$ //$NON-NLS-2$ 
            }
        }
    }

    public class ParserConfiguration implements IParserConfiguration {

        private final IScannerInfo info;

        private final String dialect;

        public ParserConfiguration(IScannerInfo s, IFile code) {
            this.info = s;
            String filename = code.getLocation().toOSString();
            IProject prj = code.getProject();

            // FIXME: ALAIN, for headers should we assume CPP ??
            // The problem is that it really depends on how the header was
            // included.
            String id = null;
            IContentType contentType = CCorePlugin.getContentType(prj, filename);
            if (contentType != null) {
                id = contentType.getId();
            }

            if (id != null) {
                if (CCorePlugin.CONTENT_TYPE_CSOURCE.equals(id))
                    dialect = "GNUC"; //$NON-NLS-1$
                else
                    dialect = "GNUC++"; //$NON-NLS-1$
            } else
                dialect = "GNUC++"; //$NON-NLS-1$

        }

        @Override
		public IScannerInfo getScannerInfo() {
            return info;
        }

        @Override
		public String getParserDialect() {
            return dialect;
        }

    }

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * @param name
     * @param className
     */
    public DOMLocationInclusionTests(String name) {
        super(name, DOMLocationInclusionTests.class);
    }

    protected IASTTranslationUnit parse(IFile code) throws Exception {
        SavedCodeReaderFactory.getInstance().getCodeReaderCache().flush();
        return CDOM.getInstance().getTranslationUnit(code);
    }

    protected IASTTranslationUnit parse(IFile code, IScannerInfo s)
            throws Exception {
        SavedCodeReaderFactory.getInstance().getCodeReaderCache().flush();
        return CDOM.getInstance().getTranslationUnit(
                code,
                CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES),
                new ParserConfiguration(s, code));
    }

    /**
     * @param pathEndsWith
     *            TODO
     * @param offset
     * @param length
     * @param declarator
     */
    protected void assertSoleFileLocation(IASTNode n, String pathEndsWith, int offset, int length) {
        IASTNodeLocation[] locations = n.getNodeLocations();
        assertEquals(locations.length, 1);
        IASTFileLocation nodeLocation = (IASTFileLocation) locations[0];
        assertTrue(nodeLocation.getFileName().endsWith(pathEndsWith));
        assertEquals(offset, nodeLocation.getNodeOffset());
        assertEquals(length, nodeLocation.getNodeLength());
    }

    private void assertFileLocation(IASTNode n, String pathEndsWith, int offset, int length) {
        IASTFileLocation location = n.getFileLocation();
        assertTrue(location.getFileName().endsWith(pathEndsWith));
        assertEquals(offset, location.getNodeOffset());
        assertEquals(length, location.getNodeLength());
    }

    public void testSimpleInclusion() throws Exception {
        String foo = "int FOO;"; //$NON-NLS-1$
        String code = "int bar;\n#include \"foo.h\"\n"; //$NON-NLS-1$

        importFile("foo.h", foo); //$NON-NLS-1$

        for (ParserLanguage p : ParserLanguage.values()) {
            String filename = (p == ParserLanguage.CPP) ? "code.cc" : "code.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile cpp = importFile(filename, code); 
            IASTTranslationUnit tu = parse(cpp); 
            IASTDeclaration[] declarations = tu.getDeclarations();
            assertEquals(declarations.length, 2);
            IASTSimpleDeclaration bar = (IASTSimpleDeclaration) declarations[0];
            IASTSimpleDeclaration FOO = (IASTSimpleDeclaration) declarations[1];
            assertSoleFileLocation(bar, filename, code.indexOf("int"), code.indexOf(";") + 1); //$NON-NLS-1$ //$NON-NLS-2$ 

            assertSoleFileLocation(FOO, "foo.h", foo.indexOf("int"), foo.indexOf(";") + 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            IASTPreprocessorIncludeStatement[] incs = tu.getIncludeDirectives();
            assertNotNull(incs);
            assertEquals(incs.length, 1);
            assertSoleFileLocation(incs[0], filename, code.indexOf("#inc"), "#include \"foo.h\"".length());
            // test bug 166026
            assertEquals(tu.getFilePath(), incs[0].getContainingFilename());
            
            checkInclude(incs[0], filename, code, "foo.h", false);
        }
    }

    protected void checkInclude(IASTPreprocessorIncludeStatement inc, String file, String code, String name, boolean system) {
        IASTName incName= inc.getName();
    	
        assertEquals(system, inc.isSystemInclude());
        assertEquals(name, incName.toString());
        assertSoleFileLocation(incName, file, code.indexOf(name), name.length());

	}

	public void testSimpleInclusion2() throws Exception {
        String foo = "int FOO;"; //$NON-NLS-1$
        String code = "int bar;\n#include \"foo.h\"\nfloat byob;\n"; //$NON-NLS-1$

        importFile("foo.h", foo); //$NON-NLS-1$

        for (ParserLanguage p : ParserLanguage.values()) {
            String filename = (p == ParserLanguage.CPP) ? "code.cc" : "code.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile cpp = importFile(filename, code); 
            IASTTranslationUnit tu = parse(cpp); 
            IASTDeclaration[] declarations = tu.getDeclarations();
            assertEquals(declarations.length, 3);
            IASTSimpleDeclaration bar = (IASTSimpleDeclaration) declarations[0];
            IASTSimpleDeclaration FOO = (IASTSimpleDeclaration) declarations[1];
            IASTSimpleDeclaration byob = (IASTSimpleDeclaration) declarations[2];
            assertSoleFileLocation(
                    bar,
                    filename,
                    code.indexOf("int"), code.indexOf("r;") + 2 - code.indexOf("int")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
            assertSoleFileLocation(
                    FOO,
                    "foo.h", foo.indexOf("int"), foo.indexOf(";") + 1 - foo.indexOf("int")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            assertSoleFileLocation(
                    byob,
                    filename,
                    code.indexOf("float"), code.indexOf("b;") + 2 - code.indexOf("float")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
            IASTPreprocessorIncludeStatement[] incs = tu.getIncludeDirectives();
            assertNotNull(incs);
            assertEquals(incs.length, 1);
            assertSoleFileLocation(
                    incs[0],
                    filename,
                    code.indexOf("#inc"), code.indexOf(".h\"\n") + ".h\"".length() - code.indexOf("#inc")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
        }
    }

    public void testMacrosInIncludeFile() throws Exception {
        String c_file_code = "#define X 4\n\n#include \"blarg.h\"\n\n#define POST_INCLUDE\n\n"; //$NON-NLS-1$
        String h_file_code = "#ifndef _BLARG_H_\r\n#define _BLARG_H_\r\n// macro\r\n#define PRINT(s,m)  printf(s,m)\r\n#endif //_BLARG_H_\r\n"; //$NON-NLS-1$
        importFile("blarg.h", h_file_code); //$NON-NLS-1$

        for (ParserLanguage p : ParserLanguage.values()) {
            String filename = (p == ParserLanguage.CPP) ? "blah.cc" : "blah.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile c_file = importFile(filename, c_file_code); 
            IASTTranslationUnit tu = parse(c_file); 
            assertEquals(tu.getDeclarations().length, 0);
            IASTPreprocessorMacroDefinition[] macroDefinitions = tu
                    .getMacroDefinitions();
            assertNotNull(macroDefinitions);
            assertEquals(macroDefinitions.length, 4);
            assertSoleFileLocation(
                    macroDefinitions[0],
                    filename,
                    c_file_code.indexOf("#define"), c_file_code.indexOf("4") + 1 - c_file_code.indexOf("#define")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
            assertSoleFileLocation(macroDefinitions[0].getName(), filename,
                    c_file_code.indexOf("X"), 1); //$NON-NLS-1$ 
            assertSoleFileLocation(
                    macroDefinitions[1],
                    "blarg.h", h_file_code.indexOf("#define _BLARG_H_"), "#define _BLARG_H_".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(
                    macroDefinitions[1].getName(),
                    "blarg.h", h_file_code.indexOf("e _BLARG_H_") + 2, "_BLARG_H_".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(
                    macroDefinitions[2],
                    "blarg.h", h_file_code.indexOf("#define PRINT(s,m)  printf(s,m)\r"), "#define PRINT(s,m)  printf(s,m)".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
            assertSoleFileLocation(macroDefinitions[2].getName(),
                    "blarg.h", h_file_code.indexOf("PRINT"), "PRINT".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
            assertSoleFileLocation(
                    macroDefinitions[3],
                    filename,
                    c_file_code.indexOf("#define POST_INCLUDE"), "#define POST_INCLUDE".length()); //$NON-NLS-1$ //$NON-NLS-2$ 
            assertSoleFileLocation(
                    macroDefinitions[3].getName(),
                    filename,
                    c_file_code.indexOf("POST_INCLUDE"), "POST_INCLUDE".length()); //$NON-NLS-1$ //$NON-NLS-2$ 
        }
    }

    public void testBug84451() throws Exception {
        String header1_code = "int x;\n"; //$NON-NLS-1$
        String header2_code = "int y;\n"; //$NON-NLS-1$
        String cpp_code = "#include \"header1.h\"\n#include \"header2.h\"\nint z;\n"; //$NON-NLS-1$
        importFile("header1.h", header1_code); //$NON-NLS-1$ 
        importFile("header2.h", header2_code); //$NON-NLS-1$ 

        for (ParserLanguage p : ParserLanguage.values()) {
            String filename = (p == ParserLanguage.CPP) ? "source.cc" : "source.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile f = importFile(filename, cpp_code); 
            IASTTranslationUnit tu = parse(f);
            IASTDeclaration[] declarations = tu.getDeclarations();
            IASTPreprocessorIncludeStatement[] includeDirectives = tu
                    .getIncludeDirectives();
            assertSoleFileLocation(
                    includeDirectives[0],
                    filename,
                    cpp_code.indexOf("#include \"header1.h\""), "#include \"header1.h\"".length()); //$NON-NLS-1$ //$NON-NLS-2$ 
            assertSoleFileLocation(declarations[0],
                    "header1.h", 0, "int x;".length()); //$NON-NLS-1$ //$NON-NLS-2$
            assertSoleFileLocation(declarations[1],
                    "header2.h", 0, "int y;".length()); //$NON-NLS-1$ //$NON-NLS-2$
            assertSoleFileLocation(
                    includeDirectives[1],
                    filename,
                    cpp_code.indexOf("#include \"header2.h\""), "#include \"header2.h\"".length()); //$NON-NLS-1$ //$NON-NLS-2$ 
            assertSoleFileLocation(declarations[2], filename, cpp_code
                    .indexOf("int z;"), "int z;".length()); //$NON-NLS-1$ //$NON-NLS-2$ 

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

        for (ParserLanguage p : ParserLanguage.values()) {
            String filename = (p == ParserLanguage.CPP) ? "blah.cc" : "blah.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile c_file = importFile(filename, c_file_code); 
            IASTTranslationUnit tu = parse(c_file); 
            assertEquals(tu.getDeclarations().length, 0);
            IASTPreprocessorMacroDefinition[] macroDefinitions = tu.getMacroDefinitions();
            assertNotNull(macroDefinitions);
            assertEquals(macroDefinitions.length, 6);
            assertSoleFileLocation(
                    macroDefinitions[0],
                    filename,
                    c_file_code.indexOf("#define"), c_file_code.indexOf("4") + 1 - c_file_code.indexOf("#define")); //$NON-NLS-1$ //$NON-NLS-2$    //$NON-NLS-3$ 
            assertSoleFileLocation(macroDefinitions[0].getName(), filename,
                    c_file_code.indexOf("X"), 1); //$NON-NLS-1$ 
            assertSoleFileLocation(
                    macroDefinitions[1],
                    "blarg.h", h_file_code.indexOf("#define _BLARG_H_"), "#define _BLARG_H_".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(
                    macroDefinitions[1].getName(),
                    "blarg.h", h_file_code.indexOf("e _BLARG_H_") + 2, "_BLARG_H_".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(
                    macroDefinitions[2],
                    "blarg.h", h_file_code.indexOf("#define PRINT(s,m) printf(s,m)\r"), "#define PRINT(s,m) printf(s,m)".length()); //$NON-NLS-1$ //$NON-NLS-2$    //$NON-NLS-3$ 
            assertSoleFileLocation(macroDefinitions[2].getName(),
                    "blarg.h", h_file_code.indexOf("PRINT"), "PRINT".length()); //$NON-NLS-1$    //$NON-NLS-2$ //$NON-NLS-3$ 
            assertSoleFileLocation(
                    macroDefinitions[3],
                    filename,
                    c_file_code.indexOf("#define POST_INCLUDE"), "#define POST_INCLUDE".length()); //$NON-NLS-1$ //$NON-NLS-2$ 
            assertSoleFileLocation(
                    macroDefinitions[3].getName(),
                    filename,
                    c_file_code.indexOf("POST_INCLUDE"), "POST_INCLUDE".length()); //$NON-NLS-1$ //$NON-NLS-2$ 
            assertSoleFileLocation(
                    macroDefinitions[4],
                    "second.h", h_file2_code.indexOf("#define _SECOND_H_"), "#define _SECOND_H_".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(
                    macroDefinitions[5],
                    filename,
                    c_file_code.indexOf("#define POST_SECOND"), "#define POST_SECOND".length()); //$NON-NLS-1$ //$NON-NLS-2$ 

        }
    }

    public void testBug90851() throws Exception {
        IFile imacro_file = importFile("macro.h", "#define BEAST 666\n"); //$NON-NLS-1$ //$NON-NLS-2$
        StringBuffer buffer = new StringBuffer();
        buffer.append("#ifndef _INCLUDE_H_\n"); //$NON-NLS-1$
        buffer.append("#define _INCLUDE_H_\n"); //$NON-NLS-1$
        buffer.append("typedef void (*vfp)();\n"); //$NON-NLS-1$
        buffer.append("typedef int (*ifp)();\n"); //$NON-NLS-1$
        buffer.append("struct Include {\n"); //$NON-NLS-1$
        buffer.append("int i;\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("#endif /*_INCLUDE_H_*/\n"); //$NON-NLS-1$
        final String inc_file_code = buffer.toString();
        IFile include_file = importFile("include.h", inc_file_code); //$NON-NLS-1$ 
        String[] macros = { imacro_file.getLocation().toOSString() };
        String[] includes = { include_file.getLocation().toOSString() };
        IExtendedScannerInfo scannerInfo = new ExtendedScannerInfo(Collections.EMPTY_MAP, EMPTY_STRING_ARRAY, macros, includes);
        
        for (ParserLanguage p : ParserLanguage.values()) {
            String filename = (p == ParserLanguage.CPP) ? "main.cc" : "main.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile code = importFile(filename, "int main() { return BEAST * sizeof( Include ); } "); //$NON-NLS-1$ 
            IASTTranslationUnit tu = parse(code, scannerInfo); 
            IASTPreprocessorMacroDefinition[] macro_defs = tu.getMacroDefinitions();
            assertEquals(macro_defs.length, 2);
            IASTPreprocessorMacroDefinition BEAST = macro_defs[0];
            assertEquals(BEAST.getName().toString(), "BEAST"); //$NON-NLS-1$
            IASTPreprocessorMacroDefinition INCLUDE_H = macro_defs[1];
            final IASTNodeLocation[] nodeLocations = INCLUDE_H.getName().getNodeLocations();
            assertEquals(nodeLocations.length, 1);
            final IASTFileLocation flatLoc = INCLUDE_H.getName().getFileLocation();
            assertNotNull(flatLoc);
            assertEquals(include_file.getLocation().toOSString(), flatLoc.getFileName());
            assertEquals(inc_file_code.indexOf("#define _INCLUDE_H_") + "#define ".length(), flatLoc.getNodeOffset()); //$NON-NLS-1$ //$NON-NLS-2$
            assertEquals("_INCLUDE_H_".length(), flatLoc.getNodeLength()); //$NON-NLS-1$
        }
    }

    public void testIProblemLocation() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("#include <not_found.h>\n"); //$NON-NLS-1$
        buffer.append("int x,y,z;"); //$NON-NLS-1$
        String code = buffer.toString();

        for (ParserLanguage p : ParserLanguage.values()) {
            String filename = (p == ParserLanguage.CPP) ? "blah.cc" : "blah.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile f = importFile(filename, code); 
            IASTTranslationUnit tu = parse(f); 
            IASTProblem[] prbs = tu.getPreprocessorProblems();
            assertEquals(prbs.length, 1);
            IASTNodeLocation[] locs = prbs[0].getNodeLocations();
            assertEquals(locs.length, 1);
            IASTFileLocation fileLoc = (IASTFileLocation) locs[0];
            assertEquals(code.indexOf("#include"), fileLoc.getNodeOffset()); //$NON-NLS-1$
            assertEquals("#include <not_found.h>".length(), fileLoc.getNodeLength()); //$NON-NLS-1$  
        }

    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DOMLocationInclusionTests.class);
        suite.addTest(new DOMLocationInclusionTests("cleanupProject")); //$NON-NLS-1$
        return suite;
    }

    public void testBug97603() throws Exception {
        IFile imacro_file = importFile("macro.h", "#define JEDEN 1\n#define DVA 2\n#define TRI 3\n"); //$NON-NLS-1$ //$NON-NLS-2$
        StringBuffer buffer = new StringBuffer();
        buffer.append("#ifndef _INCLUDE_H_\n"); //$NON-NLS-1$
        buffer.append("#define _INCLUDE_H_\n"); //$NON-NLS-1$
        buffer.append("typedef void (*vfp)();\n"); //$NON-NLS-1$
        buffer.append("typedef int (*ifp)();\n"); //$NON-NLS-1$
        buffer.append("struct Include {\n"); //$NON-NLS-1$
        buffer.append("int i;\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("#endif /*_INCLUDE_H_*/\n"); //$NON-NLS-1$
        final String inc_file_code = buffer.toString();
        IFile include_file = importFile("include.h", inc_file_code); //$NON-NLS-1$ 
        String[] macros = { imacro_file.getLocation().toOSString() };
        String[] includes = { include_file.getLocation().toOSString() };
        IExtendedScannerInfo scannerInfo = new ExtendedScannerInfo(
                Collections.EMPTY_MAP, EMPTY_STRING_ARRAY, macros, includes);
        for (ParserLanguage p : ParserLanguage.values()) {
            String filename = (p == ParserLanguage.CPP) ? "main.cc" : "main.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile code = importFile(filename, "int main() { return BEAST * sizeof( Include ); } "); //$NON-NLS-1$ 

            IASTTranslationUnit tu = parse(code, scannerInfo); 
            IASTPreprocessorMacroDefinition[] macro_defs = tu.getMacroDefinitions();
            assertEquals(macro_defs.length, 4);
            IASTPreprocessorMacroDefinition BEAST = macro_defs[0];
            assertEquals(BEAST.getName().toString(), "JEDEN"); //$NON-NLS-1$
            IASTPreprocessorMacroDefinition INCLUDE_H = macro_defs[3];
            final IASTNodeLocation[] nodeLocations = INCLUDE_H.getName().getNodeLocations();
            assertEquals(nodeLocations.length, 1);
            final IASTFileLocation flatLoc = INCLUDE_H.getName().getFileLocation();
            assertNotNull(flatLoc);
            assertEquals(include_file.getLocation().toOSString(), flatLoc.getFileName());
            assertEquals(inc_file_code.indexOf("#define _INCLUDE_H_") + "#define ".length(), flatLoc.getNodeOffset()); //$NON-NLS-1$ //$NON-NLS-2$
            assertEquals("_INCLUDE_H_".length(), flatLoc.getNodeLength()); //$NON-NLS-1$
            for (int j = 0; j < macro_defs.length; ++j)
                assertNotNull(macro_defs[j].getName().getFileLocation());

        }
    }

    public void testBug97603_2() throws Exception {
        IFile imacro_file1= importFile("macro1.h", "#define JEDEN 1\n");
        IFile imacro_file2= importFile("macro2.h", "#define DVA 2\n#define TRI 3\n");
        StringBuffer buffer = new StringBuffer();
        buffer.append("#ifndef _INCLUDE_H_\n"); //$NON-NLS-1$
        buffer.append("#define _INCLUDE_H_\n"); //$NON-NLS-1$
        buffer.append("typedef void (*vfp)();\n"); //$NON-NLS-1$
        buffer.append("typedef int (*ifp)();\n"); //$NON-NLS-1$
        buffer.append("struct Include {\n"); //$NON-NLS-1$
        buffer.append("int i;\n"); //$NON-NLS-1$
        buffer.append("};\n"); //$NON-NLS-1$
        buffer.append("#endif /*_INCLUDE_H_*/\n"); //$NON-NLS-1$
        final String inc_file_code = buffer.toString();
        IFile include_file = importFile("include.h", inc_file_code); //$NON-NLS-1$ 
        String[] macros = { imacro_file1.getLocation().toOSString(), imacro_file2.getLocation().toOSString() };
        String[] includes = { include_file.getLocation().toOSString() };
        IExtendedScannerInfo scannerInfo = new ExtendedScannerInfo(Collections.EMPTY_MAP, EMPTY_STRING_ARRAY, macros, includes);
        
        for (ParserLanguage p : ParserLanguage.values()) {
            String filename = (p == ParserLanguage.CPP) ? "main.cc" : "main.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile code = importFile(filename, "int main() { return BEAST * sizeof( Include ); } "); //$NON-NLS-1$ 

            IASTTranslationUnit tu = parse(code, scannerInfo); 
            IASTPreprocessorMacroDefinition[] macro_defs = tu.getMacroDefinitions();
            assertEquals(macro_defs.length, 4);
            IASTPreprocessorMacroDefinition BEAST = macro_defs[0];
            assertEquals(BEAST.getName().toString(), "JEDEN"); //$NON-NLS-1$
            IASTPreprocessorMacroDefinition INCLUDE_H = macro_defs[3];
            final IASTNodeLocation[] nodeLocations = INCLUDE_H.getName().getNodeLocations();
            assertEquals(nodeLocations.length, 1);
            final IASTFileLocation flatLoc = INCLUDE_H.getName().getFileLocation();
            assertNotNull(flatLoc);
            assertEquals(include_file.getLocation().toOSString(), flatLoc.getFileName());
            assertEquals(inc_file_code.indexOf("#define _INCLUDE_H_") + "#define ".length(), flatLoc.getNodeOffset()); //$NON-NLS-1$ //$NON-NLS-2$
            assertEquals("_INCLUDE_H_".length(), flatLoc.getNodeLength()); //$NON-NLS-1$
            for (int j = 0; j < macro_defs.length; ++j)
                assertNotNull(macro_defs[j].getName().getFileLocation());

        }
    }
    
    public void testSystemInclude() throws Exception {
        IFile incsh= importFile("incs.h", "");
        StringBuffer buffer = new StringBuffer();
        buffer.append("#include <incs.h>\n"); 
        buffer.append("#include <../AST2BasedProjectMofo/incs.h>\n"); 
        buffer.append("#define TARG <incs.h>\n");
        buffer.append("#include TARG\n"); 
        String code= buffer.toString();
        IExtendedScannerInfo scannerInfo = new ExtendedScannerInfo(Collections.EMPTY_MAP, new String[] {incsh.getLocation().removeLastSegments(1).toOSString()}, null, null);
        
        for (ParserLanguage p : ParserLanguage.values()) {
            String filename = (p == ParserLanguage.CPP) ? "main.cc" : "main.c"; 
            IFile sfile = importFile(filename, code); 
            IASTTranslationUnit tu = parse(sfile, scannerInfo); 

            IASTPreprocessorIncludeStatement[] incs = tu.getIncludeDirectives();
            assertNotNull(incs);
            assertEquals(3, incs.length);

            assertSoleFileLocation(incs[0], filename, code.indexOf("#include <incs.h>"), "#include <incs.h>".length());
            checkInclude(incs[0], filename, code, "incs.h", true);

            assertSoleFileLocation(incs[1], filename, code.indexOf("#include <../AST2BasedProjectMofo/incs.h>"), "#include <../AST2BasedProjectMofo/incs.h>".length());
            checkInclude(incs[1], filename, code, "../AST2BasedProjectMofo/incs.h", true);

            assertFileLocation(incs[2], filename, code.indexOf("#include TARG"), "#include TARG".length());
            IASTName incName= incs[2].getName();
			
			assertEquals(true, incs[2].isSystemInclude());
			assertEquals("incs.h", incName.toString());
			assertFileLocation(incName, filename, code.lastIndexOf("TARG"), "TARG".length());
        }
    }
    
    // #define NAME test.h
    // #define MAKE_INCLUDE(path, header) <path/header>
    // #include MAKE_INCLUDE(test_bug164644, NAME)
    public void testBug164644() throws Exception {
    	String tmpDir= System.getProperty("java.io.tmpdir");
    	File tmpFile= new File(tmpDir + "/test_bug164644/test.h").getCanonicalFile();
    	tmpFile.getParentFile().mkdirs();
    	tmpFile.createNewFile();
    	try {
    		String code= getContents(1)[0].toString();
    		IExtendedScannerInfo scannerInfo = new ExtendedScannerInfo(
    				Collections.EMPTY_MAP, new String[] {tmpDir}, null, null);

    		for (ParserLanguage p : ParserLanguage.values()) {
    			String filename = (p == ParserLanguage.CPP) ? "main.cc" : "main.c"; 
    			IFile sfile = importFile(filename, code); 
    			IASTTranslationUnit tu = parse(sfile, scannerInfo); 

    			IASTPreprocessorIncludeStatement[] incs = tu.getIncludeDirectives();
    			assertNotNull(incs);
    			assertEquals(1, incs.length);

    			assertEquals(tmpFile.getAbsolutePath(), incs[0].getPath());
    			assertFileLocation(incs[0], filename, code.indexOf("#include MAKE_INCLUDE(test_bug164644, NAME)"), "#include MAKE_INCLUDE(test_bug164644, NAME)".length());
    			IASTPreprocessorIncludeStatement inc = incs[0];
    			IASTName incName= inc.getName();
    			assertEquals(true, inc.isSystemInclude());
    		}
    	}
    	finally {
    		tmpFile.delete();
    		tmpFile.getParentFile().delete();
    	}
    }
    
    // // comment
    //  
    // #ifndef guard
    // #define guard
    //  bla bla
    // #if 1
    // #endif
    //  bla bla
    // #endif
    // //comment

    // // comment
    //  
    // #if !defined(guard)
    // #define guard
    //  bla bla
    // #if 1
    // #endif
    //  bla bla
    // #endif
    // //comment

    // // comment
    //  
    // #if ((!defined guard))
    // #define guard
    //  bla bla
    // #if 1
    // #endif
    //  bla bla
    // #endif
    // //comment
    
    // // comment
    // #pragma once
    
    //    // Some comment
    //
    //    #ifndef AN_UNIQUE_INCLUDE_GUARD_H_
    //    #define AN_UNIQUE_INCLUDE_GUARD_H_
    //
    //    #include <string>
    //
    //    Some code without any macro references
    //
    //    #endif  // AN_UNIQUE_INCLUDE_GUARD_H_
    public void testPragmaOnceDetection_197989a() throws Exception {    
    	CharSequence[] contents= getContents(5);
    	
    	int i= 0;
    	for (CharSequence content : contents) {
    		String headerName = i + ".h";
			IFile base = importFile("base" + headerName, "#include \"" + headerName + "\"");
    		importFile(headerName, content.toString());
			IASTTranslationUnit tu = parse(base, new ScannerInfo()); 
			IASTPreprocessorIncludeStatement[] incs = tu.getIncludeDirectives();
			assertTrue(incs.length > 0);
			assertTrue(incs[0].hasPragmaOnceSemantics());
		}
    }

    // #ifndef guard
    // #define guard2
    // #endif

    // #if !defined guard
    // #define guard2
    // #endif

    // #if !defined(guard) && !defined(guard2)
    // #define guard
    // #endif

    // #if (0)
    // #pragma once
    // #endif

    // leading
    // #ifndef guard
    // #define guard2
    // #endif

    // #ifndef guard
    // #define guard2
    // #endif
    // #ifdef xx
    // trailing
    // #endif
    public void testPragmaOnceDetection_197989b() throws Exception {    
    	CharSequence[] contents= getContents(6);
    	
    	int i= 0;
    	for (CharSequence content : contents) {
    		String headerName = i + ".h";
			IFile base = importFile("base" + headerName, "#include \"" + headerName + "\"");
    		importFile(headerName, content.toString());
			IASTTranslationUnit tu = parse(base, new ScannerInfo()); 
			IASTPreprocessorIncludeStatement[] incs = tu.getIncludeDirectives();
			assertEquals(1, incs.length);
			assertFalse(incs[0].hasPragmaOnceSemantics());
		}
    }
    
    
    // // header.h
    // #ifdef AH
    // #endif
    // #ifndef BH 
    // #endif
    // #define h
    // #if CH || DH
    // #elif EH==1
    // #endif
    
    // #define BH
    // #define DH 0
    // #define EH 1
    // #include "header.h"
    // #ifdef h // defined in header
    // #endif
    // #ifdef A 
    //    #ifdef a  // inactive
    //    #endif
    // #else
    //   #ifndef B	 
    //   #endif
    // #endif
    // #if defined C
    // #elif ((!((defined(D)))))
    // #endif
    // #define A
    // #define B
    // #define AH
    // #define h
    // #undef u
    // #ifdef h  // locally defined
    // #endif    
    // #ifndef u // locally undefined
    // #endif
    public void testSignificantMacros_197989a() throws Exception {    
    	CharSequence[] contents= getContents(2);

    	IFile h = importFile("header.h", contents[0].toString());
    	IFile c = importFile("source.c", contents[1].toString());

    	IASTTranslationUnit tu = parse(c, new ScannerInfo()); 
    	IASTPreprocessorIncludeStatement[] incs = tu.getIncludeDirectives();
    	assertEquals(1, incs.length);
    	assertEquals("{AH=null,BH=*,CH=null,DH=0,EH=1}",
    			incs[0].getSignificantMacros().toString());
    	assertEquals("{A=null,AH=null,B=null,C=null,CH=null,D=null}",
    			tu.getSignificantMacros().toString());
    }
    
    // // header.h
    // #if EQ(A,B)
    // #endif
    
    // #define EQ(x,y) x==y
    // #define A A1
    // #define B 1
    // #include "header.h"
        public void testSignificantMacros_197989b() throws Exception {    
    	CharSequence[] contents= getContents(2);

    	IFile h = importFile("header.h", contents[0].toString());
    	IFile c = importFile("source.c", contents[1].toString());

    	IASTTranslationUnit tu = parse(c, new ScannerInfo()); 
    	IASTPreprocessorIncludeStatement[] incs = tu.getIncludeDirectives();
    	assertEquals(1, incs.length);
    	assertEquals("{A=A1,A1=null,B=1,EQ=x==y}",
    			incs[0].getSignificantMacros().toString());
    	assertEquals("{A1=null}",
    			tu.getSignificantMacros().toString());
    }

}
