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

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
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

   public static class Location implements IASTNodeLocation {
      private final int nodeOffset;
      private final int nodeLength;

      /**
       * @param offset
       * @param length
       */
      public Location(int offset, int length) {
         nodeOffset = offset;
         nodeLength = length;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTNodeLocation#getNodeOffset()
       */
      public int getNodeOffset() {
         return nodeOffset;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTNodeLocation#getNodeLength()
       */
      public int getNodeLength() {
         return nodeLength;
      }

   }

   /**
    * @author jcamelon
    */
   public static class FileLocation extends Location implements
         IASTFileLocation {

      private String fileName;

      /**
       * @param length
       * @param offset
       * @param tu_filename
       *  
       */
      public FileLocation(char[] tu_filename, int offset, int length) {
         super(offset, length);
         fileName = new String(tu_filename);
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.cdt.core.dom.ast.IASTFileLocation#getFileName()
       */
      public String getFileName() {
         return fileName;
      }

   }

   private static final IASTProblem[]      EMPTY_PROBLEMS_ARRAY = new IASTProblem[0];
   private static final IASTNodeLocation[] EMPTY_LOCATION_ARRAY = new IASTNodeLocation[0];

   public static class Context {
      /**
       * @param startOffset
       * @param endOffset
       */
      public Context(CompositeContext parent, int startOffset, int endOffset) {
         this.context_directive_start = startOffset;
         this.context_directive_end = endOffset;
         this.parent = parent;
      }

      public final int               context_directive_start;
      public final int               context_directive_end;
      public int                     context_ends = -1;
      private final CompositeContext parent;

      public CompositeContext getParent() {
         return parent;
      }
   }

   public static class CompositeContext extends Context {

      public CompositeContext(CompositeContext parent, int startOffset,
            int endOffset) {
         super(parent, startOffset, endOffset);
      }

      private static final int DEFAULT_SUBCONTEXT_ARRAY_SIZE = 8;

      protected List           subContexts                   = Collections.EMPTY_LIST;

      public List getSubContexts() {
         return subContexts;
      }

      public void addSubContext(Context c) {
         if (subContexts == Collections.EMPTY_LIST)
            subContexts = new ArrayList(DEFAULT_SUBCONTEXT_ARRAY_SIZE);
         subContexts.add(c);
      }

      /**
       * @return
       */
      public boolean hasSubContexts() {
         return subContexts != Collections.EMPTY_LIST;
      }

   }

   public static class Inclusion extends CompositeContext {
      public final char[] path;

      public Inclusion(CompositeContext parent, char[] path, int startOffset,
            int endOffset) {
         super(parent, startOffset, endOffset);
         this.path = path;
      }
   }

   public static class TranslationUnit extends CompositeContext {
      public final char[] path;

      /**
       * @param startOffset
       * @param endOffset
       */
      public TranslationUnit(char[] path) {
         super(null, 0, 0);
         this.path = path;
      }
   }

   public static class Problem extends Context {
      public final IASTProblem problem;

      /**
       * @param parent
       * @param startOffset
       * @param endOffset
       */
      public Problem(CompositeContext parent, int startOffset, int endOffset,
            IASTProblem problem) {
         super(parent, startOffset, endOffset);
         this.problem = problem;
      }

   }

   protected TranslationUnit  tu;
   protected CompositeContext currentContext;

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
      if (tu == null)
         return EMPTY_LOCATION_ARRAY;
      Context c = findContextForOffset(offset);
      if (c.context_ends >= offset + length)
         return createSoleLocation(c, offset, length);

      return EMPTY_LOCATION_ARRAY;
   }

   /**
    * @param c
    * @param offset
    * @param length
    * @return
    */
   protected IASTNodeLocation[] createSoleLocation(Context c, int offset,
         int length) {
      IASTNodeLocation[] result = new IASTNodeLocation[1];
      if (c instanceof TranslationUnit) {
         result[0] = new FileLocation(((TranslationUnit) c).path, reconcileOffset( c, offset ),
               length);
         return result;
      }
      if( c instanceof Inclusion )
      {
         result[0] = new FileLocation(((Inclusion) c).path, reconcileOffset( c, offset ),
               length);
         return result;
      }
      return EMPTY_LOCATION_ARRAY;
   }

   /**
    * @param c
    * @param offset
    * @return
    */
   protected static int reconcileOffset(Context c, int offset) {
      int subtractOff = 0;
      if( c instanceof CompositeContext )
      {
         List subs = ((CompositeContext)c).getSubContexts();
         for( int i = 0; i < subs.size(); ++i )
         {
            Context subC = (Context) subs.get(i);
            if( subC.context_ends < offset )
               subtractOff += subC.context_ends - subC.context_directive_end;
            else
               break;
         }
      }
      return offset - c.context_directive_end - subtractOff;
   }

   /**
    * @param offset
    * @return
    */
   protected Context findContextForOffset(int offset) {
      return findContextForOffset(tu, offset);
   }

   protected static Context findContextForOffset(CompositeContext context,
         int offset) {
      if (!context.hasSubContexts() )
      {
         if( context.context_ends >= offset)
            return context;
         return null;
      }
      List subContexts = context.getSubContexts();
      //check first
      Context bestContext = (Context) subContexts.get(0);
      if (bestContext.context_directive_end > offset)
         return context;
      
      for (int i = 1; i < subContexts.size(); ++i) {
         Context nextContext = (Context) subContexts.get(i);
         if (nextContext.context_directive_end < offset)
            bestContext = nextContext;
         else
            break;
      }
      
      if ( bestContext.context_ends < offset )
         return context;
      
      if (bestContext instanceof CompositeContext)
         return findContextForOffset((CompositeContext) bestContext, offset);
      return bestContext;
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
      tu = new TranslationUnit(filename);
      currentContext = tu;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#endTranslationUnit(int)
    */
   public void endTranslationUnit(int offset) {
      tu.context_ends = offset;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#startInclusion(char[],
    *      int)
    */
   public void startInclusion(char[] includePath, int offset, int endOffset) {
      Inclusion i = new Inclusion(currentContext, includePath, offset,
            endOffset);
      currentContext.addSubContext(i);
      currentContext = i;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#endInclusion(char[],
    *      int)
    */
   public void endInclusion(int offset) {
      ((Inclusion) currentContext).context_ends = offset;
      currentContext = currentContext.getParent();
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
      return new String(tu.path);
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
      List contexts = new ArrayList(8);
      LocationMap.collectContexts(V_PROBLEMS, tu, contexts);
      if (contexts.isEmpty())
         return EMPTY_PROBLEMS_ARRAY;
      IASTProblem[] result = new IASTProblem[contexts.size()];
      for (int i = 0; i < contexts.size(); ++i)
         result[i] = ((Problem) contexts.get(i)).problem;

      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser.scanner2.IScannerPreprocessorLog#encounterIProblem(org.eclipse.cdt.core.parser.IProblem)
    */
   public void encounterProblem(IASTProblem problem) {
      ScannerASTProblem p = (ScannerASTProblem) problem;
      Problem pr = new Problem(currentContext, p.getOffset(), p.getOffset()
            + p.getLength(), problem);
      pr.context_ends = p.getOffset() + p.getLength();
   }

   protected static final int V_ALL        = 1;
   protected static final int V_INCLUSIONS = 2;
   protected static final int V_PROBLEMS   = 3;

   protected static void collectContexts(int key, Context source, List result) {
      switch (key) {
         case V_ALL:
            result.add(source);
            break;
         case V_INCLUSIONS:
            if (source instanceof Inclusion)
               result.add(source);
            break;
         case V_PROBLEMS:
            if (source instanceof Problem)
               result.add(source);
            break;
      }
      if (source instanceof CompositeContext) {
         List l = ((CompositeContext) source).getSubContexts();
         for (int i = 0; i < l.size(); ++i)
            collectContexts(key, (Context) l.get(i), result);
      }
   }

}