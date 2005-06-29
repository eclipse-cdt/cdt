/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IParserConfiguration;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
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
import org.eclipse.cdt.internal.core.dom.SavedCodeReaderFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.content.IContentType;

/**
 * @author jcamelon
 */
public class DOMLocationInclusionTests extends AST2FileBasePluginTest {

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
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            String filename = (p == ParserLanguage.CPP) ? "main.cc" : "main.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile c = importFile(filename, code); //$NON-NLS-1$ //$NON-NLS-2$

            IASTTranslationUnit tu = parse(c, scannerInfo); //$NON-NLS-1$
            IASTFunctionDefinition fd = (IASTFunctionDefinition) tu
                    .getDeclarations()[3];
            IASTFileLocation floc = fd.getFileLocation();
            assertEquals(
                    floc.getNodeOffset(),
                    code
                            .indexOf("int main() { return BEAST * sizeof( Include ); }")); //$NON-NLS-1$
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

            for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                    : null) {
                String filename = (p == ParserLanguage.CPP) ? "blah.cc" : "blah.c"; //$NON-NLS-1$ //$NON-NLS-2$
                IFile source = importFile(filename, code); //$NON-NLS-1$
                IASTTranslationUnit tu = parse(source); //$NON-NLS-1$
                IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) tu
                        .getDeclarations()[0];
                assertSoleFileLocation(declaration.getDeclarators()[0],
                        filename,
                        code.indexOf("SomeStructure"), "SomeStructure".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
            IContentType contentType = CCorePlugin
                    .getContentType(prj, filename);
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

        public IScannerInfo getScannerInfo() {
            return info;
        }

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
                CDOM.getInstance().getCodeReaderFactory(
                        CDOM.PARSE_SAVED_RESOURCES),
                new ParserConfiguration(s, code));
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

        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            String filename = (p == ParserLanguage.CPP) ? "code.cc" : "code.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile cpp = importFile(filename, code); //$NON-NLS-1$
            IASTTranslationUnit tu = parse(cpp); //$NON-NLS-1$
            IASTDeclaration[] declarations = tu.getDeclarations();
            assertEquals(declarations.length, 2);
            IASTSimpleDeclaration bar = (IASTSimpleDeclaration) declarations[0];
            IASTSimpleDeclaration FOO = (IASTSimpleDeclaration) declarations[1];
            assertSoleFileLocation(bar, filename,
                    code.indexOf("int"), code.indexOf(";") + 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            assertSoleFileLocation(FOO,
                    "foo.h", foo.indexOf("int"), foo.indexOf(";") + 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            IASTPreprocessorIncludeStatement[] incs = tu.getIncludeDirectives();
            assertNotNull(incs);
            assertEquals(incs.length, 1);
            assertSoleFileLocation(
                    incs[0],
                    filename,
                    code.indexOf("#inc"), code.indexOf(".h\"\n") + ".h\"".length() - code.indexOf("#inc")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

        }
    }

    public void testSimpleInclusion2() throws Exception {
        String foo = "int FOO;"; //$NON-NLS-1$
        String code = "int bar;\n#include \"foo.h\"\nfloat byob;\n"; //$NON-NLS-1$

        importFile("foo.h", foo); //$NON-NLS-1$

        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            String filename = (p == ParserLanguage.CPP) ? "code.cc" : "code.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile cpp = importFile(filename, code); //$NON-NLS-1$
            IASTTranslationUnit tu = parse(cpp); //$NON-NLS-1$
            IASTDeclaration[] declarations = tu.getDeclarations();
            assertEquals(declarations.length, 3);
            IASTSimpleDeclaration bar = (IASTSimpleDeclaration) declarations[0];
            IASTSimpleDeclaration FOO = (IASTSimpleDeclaration) declarations[1];
            IASTSimpleDeclaration byob = (IASTSimpleDeclaration) declarations[2];
            assertSoleFileLocation(
                    bar,
                    filename,
                    code.indexOf("int"), code.indexOf("r;") + 2 - code.indexOf("int")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            assertSoleFileLocation(
                    FOO,
                    "foo.h", foo.indexOf("int"), foo.indexOf(";") + 1 - foo.indexOf("int")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            assertSoleFileLocation(
                    byob,
                    filename,
                    code.indexOf("float"), code.indexOf("b;") + 2 - code.indexOf("float")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            IASTPreprocessorIncludeStatement[] incs = tu.getIncludeDirectives();
            assertNotNull(incs);
            assertEquals(incs.length, 1);
            assertSoleFileLocation(
                    incs[0],
                    filename,
                    code.indexOf("#inc"), code.indexOf(".h\"\n") + ".h\"".length() - code.indexOf("#inc")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }
    }

    public void testMacrosInIncludeFile() throws Exception {
        String c_file_code = "#define X 4\n\n#include \"blarg.h\"\n\n#define POST_INCLUDE\n\n"; //$NON-NLS-1$
        String h_file_code = "#ifndef _BLARG_H_\r\n#define _BLARG_H_\r\n// macro\r\n#define PRINT(s,m)  printf(s,m)\r\n#endif //_BLARG_H_\r\n"; //$NON-NLS-1$
        importFile("blarg.h", h_file_code); //$NON-NLS-1$

        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            String filename = (p == ParserLanguage.CPP) ? "blah.cc" : "blah.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile c_file = importFile(filename, c_file_code); //$NON-NLS-1$    
            IASTTranslationUnit tu = parse(c_file); //$NON-NLS-1$
            assertEquals(tu.getDeclarations().length, 0);
            IASTPreprocessorMacroDefinition[] macroDefinitions = tu
                    .getMacroDefinitions();
            assertNotNull(macroDefinitions);
            assertEquals(macroDefinitions.length, 4);
            assertSoleFileLocation(
                    macroDefinitions[0],
                    filename,
                    c_file_code.indexOf("#define"), c_file_code.indexOf("4") + 1 - c_file_code.indexOf("#define")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            assertSoleFileLocation(macroDefinitions[0].getName(), filename,
                    c_file_code.indexOf("X"), 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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
                    filename,
                    c_file_code.indexOf("#define POST_INCLUDE"), "#define POST_INCLUDE".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(
                    macroDefinitions[3].getName(),
                    filename,
                    c_file_code.indexOf("POST_INCLUDE"), "POST_INCLUDE".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    public void testBug84451() throws Exception {
        String header1_code = "int x;\n"; //$NON-NLS-1$
        String header2_code = "int y;\n"; //$NON-NLS-1$
        String cpp_code = "#include \"header1.h\"\n#include \"header2.h\"\nint z;\n"; //$NON-NLS-1$
        importFile("header1.h", header1_code); //$NON-NLS-1$ 
        importFile("header2.h", header2_code); //$NON-NLS-1$ 

        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            String filename = (p == ParserLanguage.CPP) ? "source.cc" : "source.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile f = importFile(filename, cpp_code); //$NON-NLS-1$
            IASTTranslationUnit tu = parse(f);
            IASTDeclaration[] declarations = tu.getDeclarations();
            IASTPreprocessorIncludeStatement[] includeDirectives = tu
                    .getIncludeDirectives();
            assertSoleFileLocation(
                    includeDirectives[0],
                    filename,
                    cpp_code.indexOf("#include \"header1.h\""), "#include \"header1.h\"".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(declarations[0],
                    "header1.h", 0, "int x;".length()); //$NON-NLS-1$ //$NON-NLS-2$
            assertSoleFileLocation(declarations[1],
                    "header2.h", 0, "int y;".length()); //$NON-NLS-1$ //$NON-NLS-2$
            assertSoleFileLocation(
                    includeDirectives[1],
                    filename,
                    cpp_code.indexOf("#include \"header2.h\""), "#include \"header2.h\"".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(declarations[2], filename, cpp_code
                    .indexOf("int z;"), "int z;".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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

        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            String filename = (p == ParserLanguage.CPP) ? "blah.cc" : "blah.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile c_file = importFile(filename, c_file_code); //$NON-NLS-1$
            IASTTranslationUnit tu = parse(c_file); //$NON-NLS-1$
            assertEquals(tu.getDeclarations().length, 0);
            IASTPreprocessorMacroDefinition[] macroDefinitions = tu
                    .getMacroDefinitions();
            assertNotNull(macroDefinitions);
            assertEquals(macroDefinitions.length, 6);
            assertSoleFileLocation(
                    macroDefinitions[0],
                    filename,
                    c_file_code.indexOf("#define"), c_file_code.indexOf("4") + 1 - c_file_code.indexOf("#define")); //$NON-NLS-1$ //$NON-NLS-2$    //$NON-NLS-3$ //$NON-NLS-4$
            assertSoleFileLocation(macroDefinitions[0].getName(), filename,
                    c_file_code.indexOf("X"), 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$    //$NON-NLS-4$
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
                    filename,
                    c_file_code.indexOf("#define POST_INCLUDE"), "#define POST_INCLUDE".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(
                    macroDefinitions[3].getName(),
                    filename,
                    c_file_code.indexOf("POST_INCLUDE"), "POST_INCLUDE".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(
                    macroDefinitions[4],
                    "second.h", h_file2_code.indexOf("#define _SECOND_H_"), "#define _SECOND_H_".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            assertSoleFileLocation(
                    macroDefinitions[5],
                    filename,
                    c_file_code.indexOf("#define POST_SECOND"), "#define POST_SECOND".length()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$    //$NON-NLS-4$

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
        IFile include_file = importFile("include.h", inc_file_code); //$NON-NLS-1$ //$NON-NLS-2$
        String[] macros = { imacro_file.getLocation().toOSString() };
        String[] includes = { include_file.getLocation().toOSString() };
        IExtendedScannerInfo scannerInfo = new ExtendedScannerInfo(
                Collections.EMPTY_MAP, EMPTY_STRING_ARRAY, macros, includes);
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            String filename = (p == ParserLanguage.CPP) ? "main.cc" : "main.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile code = importFile(filename,
                    "int main() { return BEAST * sizeof( Include ); } "); //$NON-NLS-1$ //$NON-NLS-2$

            IASTTranslationUnit tu = parse(code, scannerInfo); //$NON-NLS-1$
            IASTPreprocessorMacroDefinition[] macro_defs = tu
                    .getMacroDefinitions();
            assertEquals(macro_defs.length, 2);
            IASTPreprocessorMacroDefinition BEAST = macro_defs[0];
            assertEquals(BEAST.getName().toString(), "BEAST"); //$NON-NLS-1$
            IASTPreprocessorMacroDefinition INCLUDE_H = macro_defs[1];
            final IASTNodeLocation[] nodeLocations = INCLUDE_H.getName()
                    .getNodeLocations();
            assertEquals(nodeLocations.length, 1);
            final IASTFileLocation flatLoc = INCLUDE_H.getName()
                    .getFileLocation();
            assertNotNull(flatLoc);
            assertEquals(include_file.getLocation().toOSString(), flatLoc
                    .getFileName());
            assertEquals(
                    inc_file_code.indexOf("#define _INCLUDE_H_") + "#define ".length(), flatLoc.getNodeOffset()); //$NON-NLS-1$ //$NON-NLS-2$
            assertEquals("_INCLUDE_H_".length(), flatLoc.getNodeLength()); //$NON-NLS-1$
        }
    }

    public void testIProblemLocation() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("#include <not_found.h>\n"); //$NON-NLS-1$
        buffer.append("int x,y,z;"); //$NON-NLS-1$
        String code = buffer.toString();

        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            String filename = (p == ParserLanguage.CPP) ? "blah.cc" : "blah.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile f = importFile(filename, code); //$NON-NLS-1$
            IASTTranslationUnit tu = parse(f); //$NON-NLS-1$
            IASTProblem[] prbs = tu.getPreprocessorProblems();
            assertEquals(prbs.length, 1);
            IASTNodeLocation[] locs = prbs[0].getNodeLocations();
            assertEquals(locs.length, 1);
            IASTFileLocation fileLoc = (IASTFileLocation) locs[0];
            assertEquals(code.indexOf("#include"), fileLoc.getNodeOffset()); //$NON-NLS-1$
            assertEquals(
                    "#include <not_found.h>\n".length(), fileLoc.getNodeLength()); //$NON-NLS-1$  
        }

    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DOMLocationInclusionTests.class);
        suite.addTest(new DOMLocationInclusionTests("cleanupProject")); //$NON-NLS-1$
        return suite;
    }

    public void testBug97603() throws Exception {
        IFile imacro_file = importFile(
                "macro.h", "#define JEDEN 1\n#define DVA 2\n#define TRI 3\n"); //$NON-NLS-1$ //$NON-NLS-2$
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
        IFile include_file = importFile("include.h", inc_file_code); //$NON-NLS-1$ //$NON-NLS-2$
        String[] macros = { imacro_file.getLocation().toOSString() };
        String[] includes = { include_file.getLocation().toOSString() };
        IExtendedScannerInfo scannerInfo = new ExtendedScannerInfo(
                Collections.EMPTY_MAP, EMPTY_STRING_ARRAY, macros, includes);
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            String filename = (p == ParserLanguage.CPP) ? "main.cc" : "main.c"; //$NON-NLS-1$ //$NON-NLS-2$
            IFile code = importFile(filename,
                    "int main() { return BEAST * sizeof( Include ); } "); //$NON-NLS-1$ //$NON-NLS-2$

            IASTTranslationUnit tu = parse(code, scannerInfo); //$NON-NLS-1$
            IASTPreprocessorMacroDefinition[] macro_defs = tu
                    .getMacroDefinitions();
            assertEquals(macro_defs.length, 4);
            IASTPreprocessorMacroDefinition BEAST = macro_defs[0];
            assertEquals(BEAST.getName().toString(), "JEDEN"); //$NON-NLS-1$
            IASTPreprocessorMacroDefinition INCLUDE_H = macro_defs[3];
            final IASTNodeLocation[] nodeLocations = INCLUDE_H.getName()
                    .getNodeLocations();
            assertEquals(nodeLocations.length, 1);
            final IASTFileLocation flatLoc = INCLUDE_H.getName()
                    .getFileLocation();
            assertNotNull(flatLoc);
            assertEquals(include_file.getLocation().toOSString(), flatLoc
                    .getFileName());
            assertEquals(
                    inc_file_code.indexOf("#define _INCLUDE_H_") + "#define ".length(), flatLoc.getNodeOffset()); //$NON-NLS-1$ //$NON-NLS-2$
            assertEquals("_INCLUDE_H_".length(), flatLoc.getNodeLength()); //$NON-NLS-1$
            for (int j = 0; j < macro_defs.length; ++j)
                assertNotNull(macro_defs[j].getName().getFileLocation());

        }
    }
}
