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
package org.eclipse.cdt.internal.core.parser.scanner2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;

/**
 * @author jcamelon
 */
public class LocationMap implements ILocationResolver, IScannerPreprocessorLog {

   private static final char [] EMPTY_CHAR_ARRAY = "".toCharArray(); //$NON-NLS-1$
   private List                       problems             = Collections.EMPTY_LIST;
   private static final IASTProblem[] EMPTY_PROBLEMS_ARRAY = new IASTProblem[0];
   private static final IASTNodeLocation [] EMPTY_LOCATION_ARRAY = new IASTNodeLocation[0];
   private char[]                     tu_filename = EMPTY_CHAR_ARRAY ;
//   private int finalOffset  = 0;

   /**
    *  
    */
   public LocationMap() {
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getMacroDefinitions()
    */
   public IASTMacroDefinition[] getMacroDefinitions(IASTNode parent) {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getIncludeDirectives()
    */
   public IASTPreprocessorIncludeStatement[] getIncludeDirectives(
         IASTNode newParam) {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getAllPreprocessorStatements()
    */
   public IASTPreprocessorStatement[] getAllPreprocessorStatements(
         IASTNode newParam) {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getLocations(int,
    *      int)
    */
   public IASTNodeLocation[] getLocations(int offset, int length) {
      if( tu_filename == EMPTY_CHAR_ARRAY ) return EMPTY_LOCATION_ARRAY;
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getLocation(int)
    */
   public IASTNodeLocation getLocation(int offset) {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#startTranslationUnit()
    */
   public void startTranslationUnit(char[] filename) {
      this.tu_filename = filename;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#endTranslationUnit(int)
    */
   public void endTranslationUnit(int offset) {
//      this.finalOffset  = offset;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#startInclusion(char[],
    *      int)
    */
   public void startInclusion(char[] includePath, int offset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#endInclusion(char[],
    *      int)
    */
   public void endInclusion(char[] includePath, int offset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#enterObjectStyleMacroExpansion(char[],
    *      char[], int)
    */
   public void enterObjectStyleMacroExpansion(char[] name, char[] expansion,
         int offset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#exitObjectStyleMacroExpansion(char[],
    *      int)
    */
   public void exitObjectStyleMacroExpansion(char[] name, int offset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#enterFunctionStyleExpansion(char[],
    *      char[][], char[], int)
    */
   public void enterFunctionStyleExpansion(char[] name, char[][] parameters,
         char[] expansion, int offset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#exitFunctionStyleExpansion(char[],
    *      int)
    */
   public void exitFunctionStyleExpansion(char[] name, int offset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#defineObjectStyleMacro(org.eclipse.cdt.internal.core.parser.scanner2.ObjectStyleMacro,
    *      int, int, int, int)
    */
   public void defineObjectStyleMacro(ObjectStyleMacro m, int startOffset,
         int nameOffset, int nameEndOffset, int endOffset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#defineFunctionStyleMacro(org.eclipse.cdt.internal.core.parser.scanner2.FunctionStyleMacro,
    *      int, int, int, int)
    */
   public void defineFunctionStyleMacro(FunctionStyleMacro m, int startOffset,
         int nameOffset, int nameEndOffset, int endOffset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundIf(int,
    *      int)
    */
   public void encounterPoundIf(int startOffset, int endOffset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundPragma(int,
    *      int)
    */
   public void encounterPoundPragma(int startOffset, int endOffset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundError(int,
    *      int)
    */
   public void encounterPoundError(int startOffset, int endOffset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundIfdef(int,
    *      int)
    */
   public void encounterPoundIfdef(int startOffset, int endOffset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundUndef(int,
    *      int)
    */
   public void encounterPoundUndef(int startOffset, int endOffset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundElse(int,
    *      int)
    */
   public void encounterPoundElse(int startOffset, int endOffset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundElif(int,
    *      int)
    */
   public void encounterPoundElif(int startOffset, int endOffset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterPoundEndIf(int,
    *      int)
    */
   public void encounterPoundEndIf(int startOffset, int endOffset) {
      // TODO Auto-generated method stub

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getTranslationUnitPath()
    */
   public String getTranslationUnitPath() {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getInclusionsPaths()
    */
   public String[] getInclusionsPaths() {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#cleanup()
    */
   public void cleanup() {
      // TODO Auto-generated method stub
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver#getScannerProblems()
    */
   public IASTProblem[] getScannerProblems() {
      if (problems == Collections.EMPTY_LIST)
         return EMPTY_PROBLEMS_ARRAY;
      return (IASTProblem[]) problems.toArray(new IASTProblem[problems.size()]);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterIProblem(org.eclipse.cdt.core.parser.IProblem)
    */
   public void encounterProblem(IASTProblem problem) {
      if (problems == Collections.EMPTY_LIST)
         problems = new ArrayList(4);
      problems.add(problem);
   }

}