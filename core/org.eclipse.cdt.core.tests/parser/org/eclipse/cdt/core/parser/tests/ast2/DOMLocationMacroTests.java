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

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorObjectStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorUndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class DOMLocationMacroTests extends AST2BaseTest {

    public void testObjectStyleMacroExpansionSimpleDeclarator() throws Exception
    {
        StringBuffer buffer = new StringBuffer( "#define ABC D\n" ); //$NON-NLS-1$
        buffer.append( "int ABC;"); //$NON-NLS-1$
        String code = buffer.toString();
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            IASTTranslationUnit tu = parse(code, p);
            IASTPreprocessorObjectStyleMacroDefinition ABC = (IASTPreprocessorObjectStyleMacroDefinition) tu.getMacroDefinitions()[0];
            IASTSimpleDeclaration var = (IASTSimpleDeclaration) tu.getDeclarations()[0];
            IASTDeclarator d = var.getDeclarators()[0];
            assertEquals( d.getName().toString(), "D"); //$NON-NLS-1$
            IASTNodeLocation [] declaratorLocations = d.getNodeLocations();
            assertEquals( declaratorLocations.length, 1 );
            IASTMacroExpansion expansion = (IASTMacroExpansion) declaratorLocations[0];
            IASTPreprocessorObjectStyleMacroDefinition fromExpansion = (IASTPreprocessorObjectStyleMacroDefinition) expansion.getMacroDefinition();
            assertEqualsMacros( fromExpansion, ABC );
            assertEquals( expansion.getNodeOffset(), 0 );
            assertEquals( expansion.getNodeLength(), 1 );
            IASTNodeLocation [] macroLocation = expansion.getExpansionLocations();
            assertEquals( macroLocation.length, 1 );
            assertTrue( macroLocation[0] instanceof IASTFileLocation );
            assertEquals( macroLocation[0].getNodeOffset(), code.indexOf( "int ABC;") + "int ".length() ); //$NON-NLS-1$ //$NON-NLS-2$
            assertEquals( macroLocation[0].getNodeLength(), "ABC".length() ); //$NON-NLS-1$
        }
    }
    
    public void testObjectMacroExpansionModestDeclarator() throws Exception
    {
        StringBuffer buffer = new StringBuffer( "#define ABC * D\n" ); //$NON-NLS-1$
        buffer.append( "int ABC;"); //$NON-NLS-1$
        String code = buffer.toString();
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            IASTTranslationUnit tu = parse(code, p);
            IASTPreprocessorObjectStyleMacroDefinition ABC = (IASTPreprocessorObjectStyleMacroDefinition) tu.getMacroDefinitions()[0];
            IASTSimpleDeclaration var = (IASTSimpleDeclaration) tu.getDeclarations()[0];
            IASTDeclarator d = var.getDeclarators()[0];
            assertEquals( d.getName().toString(), "D"); //$NON-NLS-1$
            assertEquals( d.getPointerOperators().length, 1 );
            IASTNodeLocation [] declaratorLocations = d.getNodeLocations();
            assertEquals( declaratorLocations.length, 1 );
            IASTMacroExpansion expansion = (IASTMacroExpansion) declaratorLocations[0];
            IASTPreprocessorObjectStyleMacroDefinition fromExpansion = (IASTPreprocessorObjectStyleMacroDefinition) expansion.getMacroDefinition();
            assertEqualsMacros( fromExpansion, ABC );
            assertEquals( expansion.getNodeOffset(), 0 );
            assertEquals( expansion.getNodeLength(), 3 );
            IASTNodeLocation [] macroLocation = expansion.getExpansionLocations();
            assertEquals( macroLocation.length, 1 );
            assertTrue( macroLocation[0] instanceof IASTFileLocation );
            assertEquals( macroLocation[0].getNodeOffset(), code.indexOf( "int ABC;") + "int ".length() ); //$NON-NLS-1$ //$NON-NLS-2$
            assertEquals( macroLocation[0].getNodeLength(), "ABC".length() ); //$NON-NLS-1$
            
            IASTName n = d.getName();
            IASTNodeLocation [] nameLocations = n.getNodeLocations();
            assertEquals( nameLocations.length, 1 );
            final IASTMacroExpansion nodeLocation = (IASTMacroExpansion) nameLocations[0];
            assertEquals( nodeLocation.getNodeOffset(), 2 );
            assertEquals( nodeLocation.getNodeLength(), 1 );
            
            assertEquals( nodeLocation.getExpansionLocations()[0].getNodeOffset(), macroLocation[0].getNodeOffset() );
            assertEquals( nodeLocation.getExpansionLocations()[0].getNodeLength(), macroLocation[0].getNodeLength() );
            
            IASTPointer po = (IASTPointer) d.getPointerOperators()[0];
            assertFalse( po.isConst() );
            assertFalse( po.isVolatile() );
            IASTMacroExpansion pointerLocation = (IASTMacroExpansion) po.getNodeLocations()[0];
            assertEquals( pointerLocation.getNodeOffset(), 0 );
            assertEquals( pointerLocation.getNodeLength(), 1 );
            assertEquals( pointerLocation.getExpansionLocations()[0].getNodeOffset(), macroLocation[0].getNodeOffset() );
            assertEquals( pointerLocation.getExpansionLocations()[0].getNodeLength(), macroLocation[0].getNodeLength() );
            assertEqualsMacros( pointerLocation.getMacroDefinition(), nodeLocation.getMacroDefinition() );
        }
    }    
    
    public void testObjectMacroExpansionPartialDeclSpec() throws Exception
    {
        StringBuffer buffer = new StringBuffer( "#define XYZ const\n"); //$NON-NLS-1$
        buffer.append( "XYZ int var;"); //$NON-NLS-1$
        String code = buffer.toString();
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            IASTTranslationUnit tu = parse(code, p);
            IASTPreprocessorObjectStyleMacroDefinition XYZ = (IASTPreprocessorObjectStyleMacroDefinition) tu.getMacroDefinitions()[0];
            IASTSimpleDeclaration var = (IASTSimpleDeclaration) tu.getDeclarations()[0];
            IASTSimpleDeclSpecifier declSpec = (IASTSimpleDeclSpecifier) var.getDeclSpecifier();
            IASTNodeLocation [] declSpecLocations = declSpec.getNodeLocations();
            assertEquals( declSpecLocations.length, 2 );
            IASTMacroExpansion expansion = (IASTMacroExpansion) declSpecLocations[0];
            assertEqualsMacros( XYZ, expansion.getMacroDefinition() );
            assertEquals( expansion.getNodeOffset(), 0 );
            assertEquals( expansion.getNodeLength(), 6 );
            IASTNodeLocation [] expansionLocations = expansion.getExpansionLocations();
            assertEquals( expansionLocations.length, 1 );
            assertTrue( expansionLocations[0] instanceof IASTFileLocation );
            assertEquals( expansionLocations[0].getNodeOffset(), code.indexOf( "XYZ int")); //$NON-NLS-1$
            assertEquals( expansionLocations[0].getNodeLength(), "XYZ".length()); //$NON-NLS-1$
            IASTFileLocation second = (IASTFileLocation) declSpecLocations[1];
            assertEquals( second.getNodeOffset(), code.indexOf( " int") ); //$NON-NLS-1$
            assertEquals( second.getNodeLength(), " int".length() ); //$NON-NLS-1$
        }        
    }
    
    public void testObjectMacroExpansionNested() throws Exception
    {
        StringBuffer buffer = new StringBuffer( "#define XYZ const\n"); //$NON-NLS-1$
        buffer.append( "#define PO *\n"); //$NON-NLS-1$
        buffer.append( "#define C_PO PO XYZ\n"); //$NON-NLS-1$
        buffer.append( "int C_PO var;"); //$NON-NLS-1$
        String code = buffer.toString();
        
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            IASTTranslationUnit tu = parse(code, p);
            final IASTPreprocessorMacroDefinition[] macroDefinitions = tu.getMacroDefinitions();
            IASTPreprocessorMacroDefinition XYZ = macroDefinitions[0];
            IASTPreprocessorMacroDefinition PO = macroDefinitions[1];
            IASTPreprocessorMacroDefinition C_PO = macroDefinitions[2];
            IASTSimpleDeclaration var = (IASTSimpleDeclaration) tu.getDeclarations()[0];
            assertTrue( var.getDeclarators()[0].getPointerOperators().length > 0 );
            IASTNodeLocation [] locations = var.getNodeLocations();
            assertEquals( 3, locations.length);
            IASTFileLocation start_loc = (IASTFileLocation) locations[0];
            assertEquals( start_loc.getNodeOffset(), code.indexOf( "int") ); //$NON-NLS-1$
            assertEquals( start_loc.getNodeLength(), "int ".length()); //$NON-NLS-1$
            IASTMacroExpansion mac_loc = (IASTMacroExpansion) locations[1];
            final IASTPreprocessorMacroDefinition C_PO2 = mac_loc.getMacroDefinition();
            assertEqualsMacros( C_PO, C_PO2 );
            assertEquals( 0, mac_loc.getNodeOffset());
            assertEquals( 4+ C_PO.getExpansion().length() + XYZ.getExpansion().length() + PO.getExpansion().length(), mac_loc.getNodeLength() );
            IASTFileLocation end_loc = (IASTFileLocation) locations[2];
            assertEquals( code.indexOf( " var"), end_loc.getNodeOffset() );  //$NON-NLS-1$
            assertEquals( " var;".length(), end_loc.getNodeLength() ); //$NON-NLS-1$
        }
    }

    public void testObjectMacroExpansionComplex() throws Exception
    {
        StringBuffer buffer = new StringBuffer( "#define XYZ const\n"); //$NON-NLS-1$
        buffer.append( "#define PO *\n"); //$NON-NLS-1$
        buffer.append( "#define C_PO PO XYZ\n"); //$NON-NLS-1$
        buffer.append( "#define IT int\n"); //$NON-NLS-1$
        buffer.append( "#define V var\n"); //$NON-NLS-1$
        buffer.append( "XYZ IT C_PO C_PO V;"); //$NON-NLS-1$
        String code = buffer.toString();
        
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            IASTTranslationUnit tu = parse(code, p);
            IASTPreprocessorObjectStyleMacroDefinition XYZ = (IASTPreprocessorObjectStyleMacroDefinition) tu.getMacroDefinitions()[0];
//            IASTPreprocessorObjectStyleMacroDefinition PO = (IASTPreprocessorObjectStyleMacroDefinition) tu.getMacroDefinitions()[1];
            IASTPreprocessorObjectStyleMacroDefinition C_PO = (IASTPreprocessorObjectStyleMacroDefinition) tu.getMacroDefinitions()[2];
            IASTPreprocessorObjectStyleMacroDefinition IT = (IASTPreprocessorObjectStyleMacroDefinition) tu.getMacroDefinitions()[3];
            IASTPreprocessorObjectStyleMacroDefinition V = (IASTPreprocessorObjectStyleMacroDefinition) tu.getMacroDefinitions()[4];
            
            IASTSimpleDeclaration var = (IASTSimpleDeclaration) tu.getDeclarations()[0];
            final IASTNodeLocation[] nodeLocations = var.getNodeLocations();
            
            assertEquals( 10, nodeLocations.length );
            IASTMacroExpansion first_loc = (IASTMacroExpansion) nodeLocations[0];
            assertEqualsMacros( first_loc.getMacroDefinition(), XYZ );
            IASTFileLocation second_loc = (IASTFileLocation) nodeLocations[1];
            assertEquals( 1, second_loc.getNodeLength() );
            IASTMacroExpansion third_loc = (IASTMacroExpansion) nodeLocations[2];
            assertEqualsMacros( third_loc.getMacroDefinition(), IT );
            IASTFileLocation fourth_loc = (IASTFileLocation) nodeLocations[3];
            assertEquals( 1, fourth_loc.getNodeLength() );
            IASTMacroExpansion fifth_loc = (IASTMacroExpansion) nodeLocations[4];
            assertEqualsMacros( fifth_loc.getMacroDefinition(), C_PO );
            IASTFileLocation sixth_loc = (IASTFileLocation) nodeLocations[5];
            assertEquals( 1, sixth_loc.getNodeLength() );
            IASTMacroExpansion seventh_loc = (IASTMacroExpansion) nodeLocations[6];
            assertEqualsMacros( seventh_loc.getMacroDefinition(), C_PO );
            IASTFileLocation eighth_loc = (IASTFileLocation) nodeLocations[7];
            assertEquals( 1, eighth_loc.getNodeLength() );
            IASTMacroExpansion ninth_loc = (IASTMacroExpansion) nodeLocations[8];
            assertEqualsMacros( ninth_loc.getMacroDefinition(), V );
            IASTFileLocation tenth_loc = (IASTFileLocation) nodeLocations[9];
            assertEquals( 1, tenth_loc.getNodeLength() );

            final IASTFileLocation flatLocation = var.getFileLocation();
            assertNotNull( flatLocation);
            assertEquals( code.indexOf("XYZ IT C_PO C_PO V;"), flatLocation.getNodeOffset() ); //$NON-NLS-1$
            assertEquals( "XYZ IT C_PO C_PO V;".length(), flatLocation.getNodeLength() ); //$NON-NLS-1$

            
        }        
    }
    
    public void testStdioBug() throws ParserException
    {
        StringBuffer buffer = new StringBuffer( "#define    _PTR        void *\n"); //$NON-NLS-1$
        buffer.append( "#define _EXFUN(name, proto)     __cdecl name proto\n"); //$NON-NLS-1$
        buffer.append( "_PTR     _EXFUN(memchr,(const _PTR, int, size_t));\n"); //$NON-NLS-1$
        String code = buffer.toString();
        
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            IASTTranslationUnit tu = parse(code, p);
            final IASTPreprocessorMacroDefinition[] macroDefinitions = tu.getMacroDefinitions();
            IASTPreprocessorObjectStyleMacroDefinition _PTR = (IASTPreprocessorObjectStyleMacroDefinition) macroDefinitions[0];
            IASTPreprocessorFunctionStyleMacroDefinition _EXFUN = (IASTPreprocessorFunctionStyleMacroDefinition) macroDefinitions[1];
            IASTSimpleDeclaration memchr = (IASTSimpleDeclaration) tu.getDeclarations()[0];
            IASTNodeLocation [] locations = memchr.getNodeLocations();
            assertEquals( locations.length, 4 );
            IASTMacroExpansion loc_1 = (IASTMacroExpansion) locations[0];
            assertEqualsMacros( _PTR, loc_1.getMacroDefinition() );
            IASTFileLocation loc_2 = (IASTFileLocation) locations[1];
            assertEquals( loc_2.getNodeOffset(), code.indexOf( "     _EXFUN(")); //$NON-NLS-1$
            assertEquals( loc_2.getNodeLength(), "     ".length() ); //$NON-NLS-1$
            IASTMacroExpansion loc_3 = (IASTMacroExpansion) locations[2];
            assertEqualsMacros( _EXFUN, loc_3.getMacroDefinition() );
            IASTFileLocation loc_4 = (IASTFileLocation) locations[3];
            assertEquals( loc_4.getNodeOffset(), code.indexOf( ";")); //$NON-NLS-1$
            assertEquals( loc_4.getNodeLength(), 1 );
            IASTFileLocation flat = memchr.getFileLocation();
            assertEquals( flat.getNodeOffset() , code.indexOf( "_PTR     _EXFUN(memchr,(const _PTR, int, size_t));")); //$NON-NLS-1$
            assertEquals( flat.getNodeLength(), "_PTR     _EXFUN(memchr,(const _PTR, int, size_t));".length() ); //$NON-NLS-1$
            
            IASTDeclarator d = memchr.getDeclarators()[0];
            final IASTNodeLocation[] declaratorLocations = d.getNodeLocations();
            IASTFileLocation f = d.getFileLocation();
            assertEquals( code.indexOf( "_PTR     _EXFUN(memchr,(const _PTR, int, size_t))"), f.getNodeOffset() ); //$NON-NLS-1$
            assertEquals( "_PTR     _EXFUN(memchr,(const _PTR, int, size_t))".length(), f.getNodeLength() ); //$NON-NLS-1$
        }        
    }
    
    private void assertEqualsMacros(IASTPreprocessorMacroDefinition fromExpansion, IASTPreprocessorMacroDefinition source) {
        assertEquals( fromExpansion.getExpansion(), source.getExpansion() );
        assertEquals( fromExpansion.getName().toString(), source.getName().toString() );
    }
    
    public void testMacroBindings() throws Exception
    {
        StringBuffer buffer = new StringBuffer( "#define ABC def\n"); //$NON-NLS-1$
        buffer.append( "int ABC;\n"); //$NON-NLS-1$
        buffer.append( "#undef ABC\n"); //$NON-NLS-1$
        buffer.append( "#define ABC ghi\n"); //$NON-NLS-1$
        buffer.append( "int ABC;\n"); //$NON-NLS-1$
        String code = buffer.toString();
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            IASTTranslationUnit tu = parse(code, p);
            IASTPreprocessorMacroDefinition [] macros = tu.getMacroDefinitions();
            assertEquals( macros.length, 2 );
            IASTPreprocessorObjectStyleMacroDefinition ABC1 = (IASTPreprocessorObjectStyleMacroDefinition) macros[0];
            IASTPreprocessorObjectStyleMacroDefinition ABC2 = (IASTPreprocessorObjectStyleMacroDefinition) macros[1];
            IMacroBinding binding1 = (IMacroBinding) ABC1.getName().resolveBinding();
            assertNotNull( binding1 );
            IMacroBinding binding2 = (IMacroBinding) ABC2.getName().resolveBinding();
            assertNotNull( binding2 );
            assertNotSame( binding1, binding2 );
            IASTName [] firstReferences = tu.getReferences( binding1 );
            IASTName [] firstDeclarations = tu.getDeclarations( binding1 );
            assertEquals( firstReferences.length, 2 );
            assertEquals( firstReferences[0].getPropertyInParent(), IASTTranslationUnit.EXPANSION_NAME );
            assertEquals( firstReferences[0].getParent(), tu );
            assertEquals( firstReferences[1].getPropertyInParent(), IASTPreprocessorUndefStatement.MACRO_NAME );
            assertTrue( firstReferences[1].getParent() instanceof IASTPreprocessorUndefStatement );
            assertEquals( firstDeclarations.length, 1 );
            assertSame( ABC1.getName(), firstDeclarations[0] );
            IASTName [] secondReferences = tu.getReferences(binding2);
            IASTName [] secondDeclarations = tu.getDeclarations( binding2 );
            assertEquals( 1, secondReferences.length );
            assertEquals( secondReferences[0].getPropertyInParent(), IASTTranslationUnit.EXPANSION_NAME );
            assertEquals( secondReferences[0].getParent(), tu );
            assertSame( ABC2.getName(), secondDeclarations[0]);
            
        }
    }
    
    
    public void testBug90978() throws Exception {
        StringBuffer buffer = new StringBuffer( "#define MACRO mm\n"); //$NON-NLS-1$
        buffer.append( "int MACRO;\n"); //$NON-NLS-1$
        String code = buffer.toString();
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            IASTTranslationUnit tu = parse(code, p);
            IASTPreprocessorObjectStyleMacroDefinition MACRO = (IASTPreprocessorObjectStyleMacroDefinition) tu.getMacroDefinitions()[0];
            IASTName macro_name = MACRO.getName();
            IMacroBinding binding = (IMacroBinding) macro_name.resolveBinding();
            IASTName [] references = tu.getReferences( binding );
            assertEquals( references.length, 1 );
            IASTName reference = references[0];
            IASTNodeLocation [] nodeLocations = reference.getNodeLocations();
            assertEquals( nodeLocations.length, 1 );
            assertTrue( nodeLocations[0] instanceof IASTFileLocation );
            IASTFileLocation loc = (IASTFileLocation) nodeLocations[0];
            assertEquals( code.indexOf( "int MACRO") + "int ".length(), loc.getNodeOffset() ); //$NON-NLS-1$ //$NON-NLS-2$
            assertEquals( "MACRO".length(), loc.getNodeLength() ); //$NON-NLS-1$
        }
    }
    
    public void testBug94933() throws Exception {
        StringBuffer buffer = new StringBuffer( "#define API extern\n" );
        buffer.append( "#define MYAPI API\n");
        buffer.append( "MYAPI void func() {}" );
        String code = buffer.toString();
        for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
                : null) {
            IASTTranslationUnit tu = parse(code, p);
            IASTFunctionDefinition f = (IASTFunctionDefinition) tu.getDeclarations()[0];
            assertNotNull( f.getFileLocation() ); 
        }
    }
}
