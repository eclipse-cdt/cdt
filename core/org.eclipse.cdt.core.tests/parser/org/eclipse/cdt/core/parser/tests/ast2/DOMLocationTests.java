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

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author jcamelon
 */
public class DOMLocationTests extends AST2BaseTest {

   private static final String _TEXT_ = "<text>"; //$NON-NLS-1$
   public void testBaseCase() throws ParserException {
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse("int x;", p); //$NON-NLS-1$
         IASTDeclaration declaration = tu.getDeclarations()[0];
         IASTNodeLocation[] nodeLocations = declaration.getNodeLocations();
         assertNotNull(nodeLocations);
         assertEquals(nodeLocations.length, 1);
         assertTrue(nodeLocations[0] instanceof IASTFileLocation);
         IASTFileLocation fileLocation = ((IASTFileLocation) nodeLocations[0]);
         assertEquals(fileLocation.getFileName(), _TEXT_); //$NON-NLS-1$
         assertEquals(fileLocation.getNodeOffset(), 0);
         assertEquals(fileLocation.getNodeLength(), 6);
         IASTNodeLocation[] tuLocations = tu.getNodeLocations();
         assertEquals(tuLocations.length, nodeLocations.length);
         assertEquals(fileLocation.getFileName(),
               ((IASTFileLocation) tuLocations[0]).getFileName()); //$NON-NLS-1$
         assertEquals(fileLocation.getNodeOffset(), tuLocations[0]
               .getNodeOffset());
         assertEquals(fileLocation.getNodeLength(), tuLocations[0]
               .getNodeLength());
      }
   }

   public void testSimpleDeclaration() throws ParserException {
      String code ="int xLen5, * yLength8, zLength16( int );"; //$NON-NLS-1$
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(code, p); 
         IASTDeclaration[] declarations = tu.getDeclarations();
         assertEquals(declarations.length, 1);
         IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) declarations[0];
         IASTNodeLocation[] nodeLocations = declaration.getNodeLocations();
         assertNotNull(nodeLocations);
         assertEquals(nodeLocations.length, 1);
         assertTrue(nodeLocations[0] instanceof IASTFileLocation);
         IASTFileLocation fileLocation = ((IASTFileLocation) nodeLocations[0]);
         assertEquals(fileLocation.getFileName(), _TEXT_); //$NON-NLS-1$
         assertEquals(fileLocation.getNodeOffset(), 0);
         assertEquals(fileLocation.getNodeLength(), code.indexOf( ";") + 1); //$NON-NLS-1$
         IASTDeclarator[] declarators = declaration.getDeclarators();
         assertEquals( declarators.length, 3 );
         for( int i = 0; i < 3; ++i )
         {
            IASTDeclarator declarator = declarators[i];
            switch( i )
            {
               case 0:
                  assertSoleLocation( declarator, code.indexOf( "xLen5"), "xLen5".length() ); //$NON-NLS-1$ //$NON-NLS-2$
                  break;
               case 1:
                  assertSoleLocation( declarator, code.indexOf( "* yLength8"), "* yLength8".length()); //$NON-NLS-1$ //$NON-NLS-2$
                  break;
               case 2:
                  assertSoleLocation( declarator, code.indexOf( "zLength16( int )"), "zLength16( int )".length() ); //$NON-NLS-1$ //$NON-NLS-2$
                  break;
            }
         }
         
      }
   }

   
   public void testSimpleMacroDefinition() throws Exception {
      String code ="/* hi */\n#define FOOT 0x01\n\n"; //$NON-NLS-1$
      for (ParserLanguage p = ParserLanguage.C; p != null; p = (p == ParserLanguage.C) ? ParserLanguage.CPP
            : null) {
         IASTTranslationUnit tu = parse(code, p); 
         IASTDeclaration[] declarations = tu.getDeclarations();
         assertEquals(declarations.length, 0);
         IASTPreprocessorMacroDefinition [] macros = tu.getMacroDefinitions();
         assertNotNull( macros );
         assertEquals( macros.length, 1 );
         assertSoleLocation( macros[0], code.indexOf( "#"), code.indexOf( "0x01") + 4 - code.indexOf( "#")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         assertEquals( macros[0].getName().toString(), "FOOT" ); //$NON-NLS-1$
         assertEquals( macros[0].getExpansion(), "0x01"); //$NON-NLS-1$
      }
   }
   
   
   /**
    * @param declarator
    * @param offset
    * @param length
    */
   private void assertSoleLocation(IASTNode n, int offset, int length) {
      IASTNodeLocation [] locations = n.getNodeLocations();
      assertEquals( locations.length, 1 );
      IASTNodeLocation nodeLocation = locations[0];
      assertEquals( nodeLocation.getNodeOffset(), offset );
      assertEquals( nodeLocation.getNodeLength(), length );
   }

}