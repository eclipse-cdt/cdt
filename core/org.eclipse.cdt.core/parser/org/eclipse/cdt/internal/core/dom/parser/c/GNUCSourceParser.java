/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.dom.parser.c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.BacktrackException;

/**
 * @author jcamelon
 */
public class GNUCSourceParser extends AbstractGNUSourceCodeParser {

   private final boolean    supportGCCStyleDesignators;

   private static final int DEFAULT_DECLARATOR_LIST_SIZE = 4;

   /**
    * @param scanner
    * @param parserMode
    * @param logService
    */
   public GNUCSourceParser(IScanner scanner, ParserMode parserMode,
         IParserLogService logService, ICParserExtensionConfiguration config) {
      super(scanner, logService, parserMode, config
            .supportStatementsInExpressions(), config
            .supportTypeofUnaryExpressions(), config
            .supportAlignOfUnaryExpression());
      supportGCCStyleDesignators = config.supportGCCStyleDesignators();
   }

   /**
    * @param d
    */
   protected void throwAwayMarksForInitializerClause() {
      simpleDeclarationMark = null;
   }

   protected IASTInitializer optionalCInitializer() throws EndOfFileException,
         BacktrackException {
      if (LT(1) == IToken.tASSIGN) {
         consume(IToken.tASSIGN);
         throwAwayMarksForInitializerClause();
         return cInitializerClause(Collections.EMPTY_LIST);
      }
      return null;
   }

   /**
    * @param scope
    * @return
    */
   protected IASTInitializer cInitializerClause(List designators)
         throws EndOfFileException, BacktrackException {
      IToken la = LA(1);
      int startingOffset = la.getOffset();
      la = null;
      if (LT(1) == IToken.tLBRACE) {
         consume(IToken.tLBRACE);
         IASTInitializerList result = createInitializerList();
         ((ASTNode) result).setOffset(startingOffset);
         for (;;) {
            int checkHashcode = LA(1).hashCode();
            // required at least one initializer list
            // get designator list
            List newDesignators = designatorList();
            if (newDesignators.size() != 0)
               if (LT(1) == IToken.tASSIGN)
                  consume(IToken.tASSIGN);

            IASTInitializer initializer = cInitializerClause(newDesignators);

            if (newDesignators.isEmpty()) {
               result.addInitializer(initializer);
               initializer.setParent(result);
               initializer
                     .setPropertyInParent(IASTInitializerList.NESTED_INITIALIZER);
            } else {
               ICASTDesignatedInitializer desigInitializer = createDesignatorInitializer();
               ((CASTNode) desigInitializer).setOffsetAndLength(
                     ((CASTNode) newDesignators.get(0)).getOffset(),
                     ((CASTNode) newDesignators.get(0)).getLength());
               for (int i = 0; i < newDesignators.size(); ++i) {
                  ICASTDesignator d = (ICASTDesignator) newDesignators.get(i);
                  d.setParent(desigInitializer);
                  d.setPropertyInParent(ICASTDesignatedInitializer.DESIGNATOR);
                  desigInitializer.addDesignator(d);
               }
               desigInitializer.setOperandInitializer(initializer);
               initializer.setParent(desigInitializer);
               initializer
                     .setPropertyInParent(ICASTDesignatedInitializer.OPERAND);
               result.addInitializer(desigInitializer);
               desigInitializer.setParent(result);
               desigInitializer
                     .setPropertyInParent(IASTInitializerList.NESTED_INITIALIZER);
            }
            // can end with just a '}'
            if (LT(1) == IToken.tRBRACE)
               break;
            // can end with ", }"
            if (LT(1) == IToken.tCOMMA)
               consume(IToken.tCOMMA);
            if (LT(1) == IToken.tRBRACE)
               break;
            if (checkHashcode == LA(1).hashCode()) {
               IToken l2 = LA(1);
               throwBacktrack(startingOffset, l2.getEndOffset()
                     - startingOffset);
               return null;
            }

            // otherwise, its another initializer in the list
         }
         // consume the closing brace
         int lastOffset = consume(IToken.tRBRACE).getEndOffset();
         ((ASTNode) result).setLength(lastOffset - startingOffset);
         return result;
      }
      // if we get this far, it means that we have not yet succeeded
      // try this now instead
      // assignmentExpression
      try {
         IASTExpression assignmentExpression = assignmentExpression();
         IASTInitializerExpression result = createInitializerExpression();
         result.setExpression(assignmentExpression);
         ((ASTNode) result).setOffsetAndLength(((ASTNode) assignmentExpression)
               .getOffset(), ((ASTNode) assignmentExpression).getLength());
         assignmentExpression.setParent(result);
         assignmentExpression
               .setPropertyInParent(IASTInitializerExpression.INITIALIZER_EXPRESSION);
         return result;
      } catch (BacktrackException b) {
         throwBacktrack(b);
      }
      return null;
   }

   /**
    * @return
    */
   protected ICASTDesignatedInitializer createDesignatorInitializer() {
      return new CASTDesignatedInitializer();
   }

   /**
    * @return
    */
   protected IASTInitializerList createInitializerList() {
      return new CASTInitializerList();
   }

   /**
    * @return
    */
   protected IASTInitializerExpression createInitializerExpression() {
      return new CASTInitializerExpression();
   }

   protected List designatorList() throws EndOfFileException,
         BacktrackException {
      //designated initializers for C
      List designatorList = Collections.EMPTY_LIST;

      if (LT(1) == IToken.tDOT || LT(1) == IToken.tLBRACKET) {
         while (LT(1) == IToken.tDOT || LT(1) == IToken.tLBRACKET) {
            if (LT(1) == IToken.tDOT) {
               int offset = consume(IToken.tDOT).getOffset();
               IToken id = identifier();
               ICASTFieldDesignator designator = createFieldDesignator();
               ((ASTNode) designator).setOffsetAndLength(offset, id
                     .getEndOffset()
                     - offset);
               IASTName n = createName(id);
               designator.setName(n);
               n.setParent(designator);
               n.setPropertyInParent(ICASTFieldDesignator.FIELD_NAME);
               if (designatorList == Collections.EMPTY_LIST)
                  designatorList = new ArrayList(DEFAULT_DESIGNATOR_LIST_SIZE);
               designatorList.add(designator);
            } else if (LT(1) == IToken.tLBRACKET) {
               IToken mark = consume(IToken.tLBRACKET);
               int offset = mark.getOffset();
               IASTExpression constantExpression = expression();
               if (LT(1) == IToken.tRBRACKET) {
                  int lastOffset = consume(IToken.tRBRACKET).getEndOffset();
                  ICASTArrayDesignator designator = createArrayDesignator();
                  ((ASTNode) designator).setOffsetAndLength(offset, lastOffset
                        - offset);
                  designator.setSubscriptExpression(constantExpression);
                  constantExpression.setParent(designator);
                  constantExpression
                        .setPropertyInParent(ICASTArrayDesignator.SUBSCRIPT_EXPRESSION);
                  if (designatorList == Collections.EMPTY_LIST)
                     designatorList = new ArrayList(
                           DEFAULT_DESIGNATOR_LIST_SIZE);
                  designatorList.add(designator);
                  continue;
               }
               backup(mark);
               if (supportGCCStyleDesignators) {
                  int startOffset = consume(IToken.tLBRACKET).getOffset();
                  IASTExpression constantExpression1 = expression();
                  consume(IToken.tELLIPSIS);
                  IASTExpression constantExpression2 = expression();
                  int lastOffset = consume(IToken.tRBRACKET).getEndOffset();
                  IGCCASTArrayRangeDesignator designator = createArrayRangeDesignator();
                  ((ASTNode) designator).setOffsetAndLength(startOffset,
                        lastOffset - startOffset);
                  designator.setRangeFloor(constantExpression1);
                  constantExpression1.setParent(designator);
                  constantExpression1
                        .setPropertyInParent(IGCCASTArrayRangeDesignator.SUBSCRIPT_FLOOR_EXPRESSION);
                  designator.setRangeCeiling(constantExpression2);
                  constantExpression2.setParent(designator);
                  constantExpression2
                        .setPropertyInParent(IGCCASTArrayRangeDesignator.SUBSCRIPT_CEILING_EXPRESSION);
                  if (designatorList == Collections.EMPTY_LIST)
                     designatorList = new ArrayList(
                           DEFAULT_DESIGNATOR_LIST_SIZE);
                  designatorList.add(designator);
               }
            } else if (supportGCCStyleDesignators
                  && LT(1) == IToken.tIDENTIFIER) {
               IToken identifier = identifier();
               int lastOffset = consume(IToken.tCOLON).getEndOffset();
               ICASTFieldDesignator designator = createFieldDesignator();
               ((ASTNode) designator).setOffsetAndLength(
                     identifier.getOffset(), lastOffset
                           - identifier.getOffset());
               IASTName n = createName(identifier);
               designator.setName(n);
               n.setParent(designator);
               n.setPropertyInParent(ICASTFieldDesignator.FIELD_NAME);
               if (designatorList == Collections.EMPTY_LIST)
                  designatorList = new ArrayList(DEFAULT_DESIGNATOR_LIST_SIZE);
               designatorList.add(designator);
            }
         }
      } else {
         if (supportGCCStyleDesignators
               && (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tLBRACKET)) {

            if (LT(1) == IToken.tIDENTIFIER) {
               IToken identifier = identifier();
               int lastOffset = consume(IToken.tCOLON).getEndOffset();
               ICASTFieldDesignator designator = createFieldDesignator();
               ((ASTNode) designator).setOffsetAndLength(
                     identifier.getOffset(), lastOffset
                           - identifier.getOffset());
               IASTName n = createName(identifier);
               designator.setName(n);
               n.setParent(designator);
               n.setPropertyInParent(ICASTFieldDesignator.FIELD_NAME);
               if (designatorList == Collections.EMPTY_LIST)
                  designatorList = new ArrayList(DEFAULT_DESIGNATOR_LIST_SIZE);
               designatorList.add(designator);
            } else if (LT(1) == IToken.tLBRACKET) {
               int startOffset = consume(IToken.tLBRACKET).getOffset();
               IASTExpression constantExpression1 = expression();
               consume(IToken.tELLIPSIS);
               IASTExpression constantExpression2 = expression();
               int lastOffset = consume(IToken.tRBRACKET).getEndOffset();
               IGCCASTArrayRangeDesignator designator = createArrayRangeDesignator();
               ((ASTNode) designator).setOffsetAndLength(startOffset,
                     lastOffset - startOffset);
               designator.setRangeFloor(constantExpression1);
               constantExpression1.setParent(designator);
               constantExpression1
                     .setPropertyInParent(IGCCASTArrayRangeDesignator.SUBSCRIPT_FLOOR_EXPRESSION);
               designator.setRangeCeiling(constantExpression2);
               constantExpression2.setParent(designator);
               constantExpression2
                     .setPropertyInParent(IGCCASTArrayRangeDesignator.SUBSCRIPT_CEILING_EXPRESSION);
               if (designatorList == Collections.EMPTY_LIST)
                  designatorList = new ArrayList(DEFAULT_DESIGNATOR_LIST_SIZE);
               designatorList.add(designator);
            }
         }
      }
      return designatorList;
   }

   /**
    * @return
    */
   protected IGCCASTArrayRangeDesignator createArrayRangeDesignator() {
      return new CASTArrayRangeDesignator();
   }

   /**
    * @return
    */
   protected ICASTArrayDesignator createArrayDesignator() {
      return new CASTArrayDesignator();
   }

   /**
    * @return
    */
   protected ICASTFieldDesignator createFieldDesignator() {
      return new CASTFieldDesignator();
   }

   protected IASTDeclaration declaration() throws EndOfFileException,
         BacktrackException {
      switch (LT(1)) {
         case IToken.t_asm:
            return asmDeclaration();
         default:
            IASTDeclaration d = simpleDeclaration();
            return d;
      }

   }

   /**
    * @throws BacktrackException
    * @throws EndOfFileException
    */
   protected IASTDeclaration simpleDeclaration() throws BacktrackException,
         EndOfFileException {
      IToken firstToken = LA(1);
      int firstOffset = firstToken.getOffset();
      if (firstToken.getType() == IToken.tLBRACE)
         throwBacktrack(firstToken.getOffset(), firstToken.getLength());

      firstToken = null; // necessary for scalability

      IASTDeclSpecifier declSpec = declSpecifierSeq(false);

      List declarators = Collections.EMPTY_LIST;
      if (LT(1) != IToken.tSEMI) {
         declarators = new ArrayList(DEFAULT_DECLARATOR_LIST_SIZE);
         declarators.add(initDeclarator());

         while (LT(1) == IToken.tCOMMA) {
            consume(IToken.tCOMMA);
            declarators.add(initDeclarator());
         }
      }

      boolean hasFunctionBody = false;
      boolean hasFunctionTryBlock = false;
      boolean consumedSemi = false;
      int semiOffset = 0;

      switch (LT(1)) {
         case IToken.tSEMI:
            semiOffset = consume(IToken.tSEMI).getEndOffset();
            consumedSemi = true;
            break;
         case IToken.tLBRACE:
            break;
         default:
            throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);
      }

      if (!consumedSemi) {
         if (LT(1) == IToken.tLBRACE) {
            hasFunctionBody = true;
         }

         if (hasFunctionTryBlock && !hasFunctionBody)
            throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);
      }

      if (hasFunctionBody) {
         if (declarators.size() != 1)
            throwBacktrack(firstOffset, LA(1).getEndOffset());

         IASTDeclarator declarator = (IASTDeclarator) declarators.get(0);
         if (!(declarator instanceof IASTFunctionDeclarator))
            throwBacktrack(firstOffset, LA(1).getEndOffset());

         IASTFunctionDefinition funcDefinition = createFunctionDefinition();
         ((ASTNode) funcDefinition).setOffset(firstOffset);
         funcDefinition.setDeclSpecifier(declSpec);
         declSpec.setParent(funcDefinition);
         declSpec.setPropertyInParent(IASTFunctionDefinition.DECL_SPECIFIER);

         funcDefinition.setDeclarator((IASTFunctionDeclarator) declarator);
         declarator.setParent(funcDefinition);
         declarator.setPropertyInParent(IASTFunctionDefinition.DECLARATOR);

         IASTStatement s = handleFunctionBody();
         if (s != null) {
            funcDefinition.setBody(s);
            s.setParent(funcDefinition);
            s.setPropertyInParent(IASTFunctionDefinition.FUNCTION_BODY);
         }
         ((ASTNode) funcDefinition).setLength(calculateEndOffset(s)
               - firstOffset);
         return funcDefinition;
      }

      IASTSimpleDeclaration simpleDeclaration = createSimpleDeclaration();

      int length = figureEndOffset(declSpec, declarators) - firstOffset;
      if (consumedSemi)
         length = semiOffset - firstOffset;
      ((ASTNode) simpleDeclaration).setOffsetAndLength(firstOffset, length);
      simpleDeclaration.setDeclSpecifier(declSpec);
      declSpec.setParent(simpleDeclaration);
      declSpec.setPropertyInParent(IASTSimpleDeclaration.DECL_SPECIFIER);

      for (int i = 0; i < declarators.size(); ++i) {
         IASTDeclarator declarator = (IASTDeclarator) declarators.get(i);
         simpleDeclaration.addDeclarator(declarator);
         declarator.setParent(simpleDeclaration);
         declarator.setPropertyInParent(IASTSimpleDeclaration.DECLARATOR);
      }
      return simpleDeclaration;
   }

   /**
    * @return
    */
   protected IASTFunctionDefinition createFunctionDefinition() {
      return new CASTFunctionDefinition();
   }

   /**
    * @return
    */
   protected CASTSimpleDeclaration createSimpleDeclaration() {
      return new CASTSimpleDeclaration();
   }

   protected CASTTranslationUnit translationUnit;

   private static final int      DEFAULT_POINTEROPS_LIST_SIZE = 4;

   private static final int      DEFAULT_PARAMETERS_LIST_SIZE = 4;

   protected CASTTranslationUnit createTranslationUnit() {
      CASTTranslationUnit t = new CASTTranslationUnit();
      t.setOffset(0);
      t.setParent(null);
      t.setPropertyInParent(null);
      return t;
   }

   /**
    * This is the top-level entry point into the ANSI C++ grammar.
    * 
    * translationUnit : (declaration)*
    */
   protected void translationUnit() {
      try {
         translationUnit = createTranslationUnit();
      } catch (Exception e2) {
         logException("translationUnit::createCompilationUnit()", e2); //$NON-NLS-1$
         return;
      }

      translationUnit.setLocationResolver(scanner.getLocationResolver());

      int lastBacktrack = -1;
      while (true) {
         try {
            int checkOffset = LA(1).hashCode();
            IASTDeclaration d = declaration();
            d.setParent(translationUnit);
            d.setPropertyInParent(IASTTranslationUnit.OWNED_DECLARATION);
            translationUnit.addDeclaration(d);
            if (LA(1).hashCode() == checkOffset)
               failParseWithErrorHandling();
         } catch (EndOfFileException e) {
            // As expected
            if (translationUnit.getDeclarations().length != 0) {
               CASTNode d = (CASTNode) translationUnit.getDeclarations()[translationUnit
                     .getDeclarations().length - 1];
               ((CASTNode) translationUnit).setLength(d.getOffset()
                     + d.getLength());
            } else
               ((CASTNode) translationUnit).setLength(0);
            break;
         } catch (BacktrackException b) {
            try {
               // Mark as failure and try to reach a recovery point
               IASTProblem p = failParse(b);
               IASTProblemDeclaration pd = createProblemDeclaration();
               pd.setProblem(p);
               ((CASTNode) pd).setOffsetAndLength(((CASTNode) p).getOffset(),
                     ((CASTNode) p).getLength());
               p.setParent(pd);
               p.setPropertyInParent(IASTProblemHolder.PROBLEM);
               pd.setParent(translationUnit);
               pd.setPropertyInParent(IASTTranslationUnit.OWNED_DECLARATION);
               translationUnit.addDeclaration(pd);
               errorHandling();
               if (lastBacktrack != -1 && lastBacktrack == LA(1).hashCode()) {
                  // we haven't progressed from the
                  // last backtrack
                  // try and find tne next definition
                  failParseWithErrorHandling();
               } else {
                  // start again from here
                  lastBacktrack = LA(1).hashCode();
               }
            } catch (EndOfFileException e) {
               break;
            }
         } catch (OutOfMemoryError oome) {
            logThrowable("translationUnit", oome); //$NON-NLS-1$
            throw oome;
         } catch (Exception e) {
            logException("translationUnit", e); //$NON-NLS-1$
            try {
               failParseWithErrorHandling();
            } catch (EndOfFileException e3) {
               //nothing
            }
         } catch (ParseError perr) {
            throw perr;
         } catch (Throwable e) {
            logThrowable("translationUnit", e); //$NON-NLS-1$
            try {
               failParseWithErrorHandling();
            } catch (EndOfFileException e3) {
               //break;
            }
         }
      }
      //        compilationUnit.exitScope( requestor );
   }

   /**
    * @return
    */
   protected IASTProblemDeclaration createProblemDeclaration() {
      return new CASTProblemDeclaration();
   }

   /**
    * @param expression
    * @throws BacktrackException
    */
   protected IASTExpression assignmentExpression() throws EndOfFileException,
         BacktrackException {
      IASTExpression conditionalExpression = conditionalExpression();
      // if the condition not taken, try assignment operators
      if (conditionalExpression != null
            && conditionalExpression instanceof IASTConditionalExpression) //&&
         return conditionalExpression;
      switch (LT(1)) {
         case IToken.tASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_assign,
                  conditionalExpression);
         case IToken.tSTARASSIGN:
            return assignmentOperatorExpression(
                  IASTBinaryExpression.op_multiplyAssign, conditionalExpression);
         case IToken.tDIVASSIGN:
            return assignmentOperatorExpression(
                  IASTBinaryExpression.op_divideAssign, conditionalExpression);
         case IToken.tMODASSIGN:
            return assignmentOperatorExpression(
                  IASTBinaryExpression.op_moduloAssign, conditionalExpression);
         case IToken.tPLUSASSIGN:
            return assignmentOperatorExpression(
                  IASTBinaryExpression.op_plusAssign, conditionalExpression);
         case IToken.tMINUSASSIGN:
            return assignmentOperatorExpression(
                  IASTBinaryExpression.op_minusAssign, conditionalExpression);
         case IToken.tSHIFTRASSIGN:
            return assignmentOperatorExpression(
                  IASTBinaryExpression.op_shiftRightAssign,
                  conditionalExpression);
         case IToken.tSHIFTLASSIGN:
            return assignmentOperatorExpression(
                  IASTBinaryExpression.op_shiftLeftAssign,
                  conditionalExpression);
         case IToken.tAMPERASSIGN:
            return assignmentOperatorExpression(
                  IASTBinaryExpression.op_binaryAndAssign,
                  conditionalExpression);
         case IToken.tXORASSIGN:
            return assignmentOperatorExpression(
                  IASTBinaryExpression.op_binaryXorAssign,
                  conditionalExpression);
         case IToken.tBITORASSIGN:
            return assignmentOperatorExpression(
                  IASTBinaryExpression.op_binaryOrAssign, conditionalExpression);
      }
      return conditionalExpression;
   }

   /**
    * @param expression
    * @throws BacktrackException
    */
   protected IASTExpression relationalExpression() throws BacktrackException,
         EndOfFileException {

      IASTExpression firstExpression = shiftExpression();
      for (;;) {
         switch (LT(1)) {
            case IToken.tGT:
            case IToken.tLT:
            case IToken.tLTEQUAL:
            case IToken.tGTEQUAL:
               int t = consume().getType();
               IASTExpression secondExpression = shiftExpression();
               int operator = 0;
               switch (t) {
                  case IToken.tGT:
                     operator = IASTBinaryExpression.op_greaterThan;
                     break;
                  case IToken.tLT:
                     operator = IASTBinaryExpression.op_lessThan;
                     break;
                  case IToken.tLTEQUAL:
                     operator = IASTBinaryExpression.op_lessEqual;
                     break;
                  case IToken.tGTEQUAL:
                     operator = IASTBinaryExpression.op_greaterEqual;
                     break;
               }
               firstExpression = buildBinaryExpression(operator,
                     firstExpression, secondExpression,
                     calculateEndOffset(secondExpression));
               break;
            default:
               return firstExpression;
         }
      }
   }

   /**
    * @param expression
    * @throws BacktrackException
    */
   protected IASTExpression multiplicativeExpression()
         throws BacktrackException, EndOfFileException {
      IASTExpression firstExpression = castExpression();
      for (;;) {
         switch (LT(1)) {
            case IToken.tSTAR:
            case IToken.tDIV:
            case IToken.tMOD:
               IToken t = consume();
               IASTExpression secondExpression = castExpression();
               int operator = 0;
               switch (t.getType()) {
                  case IToken.tSTAR:
                     operator = IASTBinaryExpression.op_multiply;
                     break;
                  case IToken.tDIV:
                     operator = IASTBinaryExpression.op_divide;
                     break;
                  case IToken.tMOD:
                     operator = IASTBinaryExpression.op_modulo;
                     break;
               }
               firstExpression = buildBinaryExpression(operator,
                     firstExpression, secondExpression,
                     calculateEndOffset(secondExpression));
               break;
            default:
               return firstExpression;
         }
      }
   }

   /**
    * castExpression : unaryExpression | "(" typeId ")" castExpression
    */
   protected IASTExpression castExpression() throws EndOfFileException,
         BacktrackException {
      // TO DO: we need proper symbol checkint to ensure type name
      if (LT(1) == IToken.tLPAREN) {
         IToken mark = mark();
         int startingOffset = mark.getOffset();
         consume();
         IASTTypeId typeId = null;
         IASTExpression castExpression = null;
         // If this isn't a type name, then we shouldn't be here
         try {
            try {
               typeId = typeId(false);
               consume(IToken.tRPAREN);
               castExpression = castExpression();
            } catch (BacktrackException bte) {
               backup(mark);
               throwBacktrack(bte);
            }

            return buildTypeIdUnaryExpression(IASTCastExpression.op_cast,
                  typeId, castExpression, startingOffset,
                  calculateEndOffset(castExpression));
         } catch (BacktrackException b) {
         }
      }
      return unaryExpression();
   }

   /**
    * @param expression
    * @throws BacktrackException
    */
   protected IASTExpression unaryExpression() throws EndOfFileException,
         BacktrackException {
      int startingOffset = LA(1).getOffset();
      switch (LT(1)) {
         case IToken.tSTAR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_star);
         case IToken.tAMPER:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_amper);
         case IToken.tPLUS:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_plus);
         case IToken.tMINUS:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_minus);
         case IToken.tNOT:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_not);
         case IToken.tCOMPL:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_tilde);
         case IToken.tINCR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_prefixIncr);
         case IToken.tDECR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_prefixDecr);
         case IToken.t_sizeof:
            startingOffset = consume(IToken.t_sizeof).getOffset();
            IToken mark = LA(1);
            IASTExpression unaryExpression = null;
            IASTTypeId typeId = null;
            int lastOffset = 0;
            if (LT(1) == IToken.tLPAREN) {
               try {
                  consume(IToken.tLPAREN);
                  typeId = typeId(false);
                  lastOffset = consume(IToken.tRPAREN).getEndOffset();
               } catch (BacktrackException bt) {
                  backup(mark);
                  typeId = null;
                  unaryExpression = unaryExpression();
                  lastOffset = calculateEndOffset(unaryExpression);
               }
            } else {
               unaryExpression = unaryExpression();
               lastOffset = calculateEndOffset(unaryExpression);
            }
            mark = null;
            if (typeId == null && unaryExpression != null)
               return buildUnaryExpression(IASTUnaryExpression.op_sizeof,
                     unaryExpression, startingOffset, lastOffset);
            return buildTypeIdExpression(IASTTypeIdExpression.op_sizeof,
                  typeId, startingOffset, lastOffset);

         default:
            if (LT(1) == IGCCToken.t_typeof && supportTypeOfUnaries) {
               IASTExpression unary = unaryTypeofExpression();
               if (unary != null)
                  return unary;
            }
            if (LT(1) == IGCCToken.t___alignof__ && supportAlignOfUnaries) {
               IASTExpression align = unaryAlignofExpression();
               if (align != null)
                  return align;
            }
            return postfixExpression();
      }
   }

   /**
    * @param typeId
    * @param startingOffset
    * @param op
    * @return
    */
   protected IASTExpression buildTypeIdExpression(int op, IASTTypeId typeId,
         int startingOffset, int endingOffset) {
      IASTTypeIdExpression result = createTypeIdExpression();
      result.setOperator(op);
      ((ASTNode) result).setOffsetAndLength(startingOffset, endingOffset
            - startingOffset);
      ((ASTNode) result).setLength(endingOffset - startingOffset);
      result.setTypeId(typeId);
      typeId.setParent(result);
      typeId.setPropertyInParent(IASTTypeIdExpression.TYPE_ID);
      return result;
   }

   /**
    * @return
    */
   protected IASTTypeIdExpression createTypeIdExpression() {
      return new CASTTypeIdExpression();
   }

   /**
    * @param expression
    * @throws BacktrackException
    */
   protected IASTExpression postfixExpression() throws EndOfFileException,
         BacktrackException {

      IASTExpression firstExpression = null;
      switch (LT(1)) {
         case IToken.tLPAREN:
            // ( type-name ) { initializer-list }
            // ( type-name ) { initializer-list , }
            IToken m = mark();
            try {
               int offset = consume(IToken.tLPAREN).getOffset();
               IASTTypeId t = typeId(false);
               int lastOffset = consume(IToken.tRPAREN).getEndOffset();
               IASTInitializer i = cInitializerClause(Collections.EMPTY_LIST);
               firstExpression = buildTypeIdInitializerExpression(t, i, offset,
                     lastOffset);
               break;
            } catch (BacktrackException bt) {
               backup(m);
            }

         default:
            firstExpression = primaryExpression();
      }

      IASTExpression secondExpression = null;
      for (;;) {
         switch (LT(1)) {
            case IToken.tLBRACKET:
               // array access
               consume(IToken.tLBRACKET);
               secondExpression = expression();
               int last = consume(IToken.tRBRACKET).getEndOffset();
               IASTArraySubscriptExpression s = createArraySubscriptExpression();
               ((ASTNode) s).setOffsetAndLength(((ASTNode) firstExpression)
                     .getOffset(), last
                     - ((ASTNode) firstExpression).getOffset());
               s.setArrayExpression(firstExpression);
               firstExpression.setParent(s);
               firstExpression
                     .setPropertyInParent(IASTArraySubscriptExpression.ARRAY);
               s.setSubscriptExpression(secondExpression);
               secondExpression.setParent(s);
               secondExpression
                     .setPropertyInParent(IASTArraySubscriptExpression.SUBSCRIPT);
               firstExpression = s;
               break;
            case IToken.tLPAREN:
               // function call
               consume(IToken.tLPAREN);
               if (LT(1) != IToken.tRPAREN)
                  secondExpression = expression();
               last = consume(IToken.tRPAREN).getEndOffset();
               IASTFunctionCallExpression f = createFunctionCallExpression();
               ((ASTNode) f).setOffsetAndLength(((ASTNode) firstExpression)
                     .getOffset(), last
                     - ((ASTNode) firstExpression).getOffset());
               f.setFunctionNameExpression(firstExpression);
               firstExpression.setParent(f);
               firstExpression
                     .setPropertyInParent(IASTFunctionCallExpression.FUNCTION_NAME);

               if (secondExpression != null) {
                  f.setParameterExpression(secondExpression);
                  secondExpression.setParent(f);
                  secondExpression
                        .setPropertyInParent(IASTFunctionCallExpression.PARAMETERS);
               }
               firstExpression = f;
               break;
            case IToken.tINCR:
               int offset = consume(IToken.tINCR).getEndOffset();
               firstExpression = buildUnaryExpression(
                     IASTUnaryExpression.op_postFixIncr, firstExpression,
                     ((CASTNode) firstExpression).getOffset(), offset);
               break;
            case IToken.tDECR:
               offset = consume().getEndOffset();
               firstExpression = buildUnaryExpression(
                     IASTUnaryExpression.op_postFixDecr, firstExpression,
                     ((CASTNode) firstExpression).getOffset(), offset);
               break;
            case IToken.tDOT:
               // member access
               consume(IToken.tDOT);
               IASTName name = createName(identifier());
               IASTFieldReference result = createFieldReference();
               ((ASTNode) result).setOffsetAndLength(
                     ((ASTNode) firstExpression).getOffset(),
                     calculateEndOffset(name)
                           - ((ASTNode) firstExpression).getOffset());
               result.setFieldOwner(firstExpression);
               result.setIsPointerDereference(false);
               firstExpression.setParent(result);
               firstExpression
                     .setPropertyInParent(IASTFieldReference.FIELD_OWNER);
               result.setFieldName(name);
               name.setParent(result);
               name.setPropertyInParent(IASTFieldReference.FIELD_NAME);
               firstExpression = result;
               break;
            case IToken.tARROW:
               // member access
               consume(IToken.tARROW);
               name = createName(identifier());
               result = createFieldReference();
               ((ASTNode) result).setOffsetAndLength(
                     ((ASTNode) firstExpression).getOffset(),
                     calculateEndOffset(name)
                           - ((ASTNode) firstExpression).getOffset());
               result.setFieldOwner(firstExpression);
               result.setIsPointerDereference(true);
               firstExpression.setParent(result);
               firstExpression
                     .setPropertyInParent(IASTFieldReference.FIELD_OWNER);
               result.setFieldName(name);
               name.setParent(result);
               name.setPropertyInParent(IASTFieldReference.FIELD_NAME);
               firstExpression = result;
               break;
            default:
               return firstExpression;
         }
      }
   }

   /**
    * @return
    */
   protected IASTFunctionCallExpression createFunctionCallExpression() {
      return new CASTFunctionCallExpression();
   }

   /**
    * @return
    */
   protected IASTArraySubscriptExpression createArraySubscriptExpression() {
      return new CASTArraySubscriptExpression();
   }

   /**
    * @param t
    * @param i
    * @param offset
    * @param lastOffset
    *           TODO
    * @return
    */
   protected ICASTTypeIdInitializerExpression buildTypeIdInitializerExpression(
         IASTTypeId t, IASTInitializer i, int offset, int lastOffset) {
      ICASTTypeIdInitializerExpression result = createTypeIdInitializerExpression();
      ((ASTNode) result).setOffsetAndLength(offset, lastOffset - offset);
      result.setTypeId(t);
      t.setParent(result);
      t.setPropertyInParent(ICASTTypeIdInitializerExpression.TYPE_ID);
      result.setInitializer(i);
      i.setParent(result);
      i.setPropertyInParent(ICASTTypeIdInitializerExpression.INITIALIZER);
      return result;
   }

   /**
    * @return
    */
   protected ICASTTypeIdInitializerExpression createTypeIdInitializerExpression() {
      return new CASTTypeIdInitializerExpression();
   }

   /**
    * @return
    */
   protected IASTFieldReference createFieldReference() {
      return new CASTFieldReference();
   }

   /**
    * @param expression
    * @throws BacktrackException
    */
   protected IASTExpression primaryExpression() throws EndOfFileException,
         BacktrackException {
      IToken t = null;
      IASTLiteralExpression literalExpression = null;
      switch (LT(1)) {
         // TO DO: we need more literals...
         case IToken.tINTEGER:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression
                  .setKind(IASTLiteralExpression.lk_integer_constant);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t
                  .getEndOffset()
                  - t.getOffset());
            return literalExpression;
         case IToken.tFLOATINGPT:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(IASTLiteralExpression.lk_float_constant);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t
                  .getEndOffset()
                  - t.getOffset());
            return literalExpression;
         case IToken.tSTRING:
         case IToken.tLSTRING:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(IASTLiteralExpression.lk_string_literal);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t
                  .getEndOffset()
                  - t.getOffset());
            return literalExpression;
         case IToken.tCHAR:
         case IToken.tLCHAR:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(IASTLiteralExpression.lk_char_constant);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t
                  .getEndOffset()
                  - t.getOffset());
            return literalExpression;
         case IToken.tLPAREN:
            t = consume();
            //TODO - do we need to return a wrapper?
            IASTExpression lhs = expression();
            int finalOffset = consume(IToken.tRPAREN).getEndOffset();
            return buildUnaryExpression(
                  IASTUnaryExpression.op_bracketedPrimary, lhs, t.getOffset(),
                  finalOffset);
         case IToken.tIDENTIFIER:

            int startingOffset = LA(1).getOffset();
            IToken t1 = identifier();
            IASTIdExpression idExpression = createIdExpression();
            IASTName name = createName(t1);
            idExpression.setName(name);
            name.setParent(idExpression);
            name.setPropertyInParent(IASTIdExpression.ID_NAME);
            ((ASTNode) idExpression).setOffsetAndLength((ASTNode) name);
            return idExpression;
         default:
            IToken la = LA(1);
            startingOffset = la.getOffset();
            throwBacktrack(startingOffset, la.getLength());
            return null;
      }

   }

   /**
    * @return
    */
   protected IASTLiteralExpression createLiteralExpression() {
      return new CASTLiteralExpression();
   }

   /**
    * @return
    */
   protected IASTIdExpression createIdExpression() {
      return new CASTIdExpression();
   }

   protected IASTTypeId typeId(boolean skipArrayModifiers)
         throws EndOfFileException, BacktrackException {
      IToken mark = mark();
      int startingOffset = mark.getOffset();
      IASTDeclSpecifier declSpecifier = null;
      IASTDeclarator declarator = null;

      try {
         declSpecifier = declSpecifierSeq(false);
         declarator = declarator();
      } catch (BacktrackException bt) {
         backup(mark);
         throwBacktrack(bt);
      }
      if (declarator == null || declarator.getName().toString() != null) //$NON-NLS-1$
      {
         backup(mark);
         throwBacktrack(startingOffset, figureEndOffset(declSpecifier,
               declarator)
               - startingOffset);
      }

      IASTTypeId result = createTypeId();
      ((ASTNode) result).setOffsetAndLength(startingOffset, figureEndOffset(
            declSpecifier, declarator)
            - startingOffset);

      result.setDeclSpecifier(declSpecifier);
      declSpecifier.setParent(result);
      declSpecifier.setPropertyInParent(IASTTypeId.DECL_SPECIFIER);

      result.setAbstractDeclarator(declarator);
      declarator.setParent(result);
      declarator.setPropertyInParent(IASTTypeId.ABSTRACT_DECLARATOR);

      return result;
   }

   /**
    * @return
    */
   protected IASTTypeId createTypeId() {
      return new CASTTypeId();
   }

   /**
    * Parse a Pointer Operator.
    * 
    * ptrOperator : "*" (cvQualifier)* | "&" | ::? nestedNameSpecifier "*"
    * (cvQualifier)*
    * 
    * @param owner
    *           Declarator that this pointer operator corresponds to.
    * @throws BacktrackException
    *            request a backtrack
    */
   protected void consumePointerOperators(List pointerOps)
         throws EndOfFileException, BacktrackException {
      for (;;) {
         IToken mark = mark();
         IToken last = null;

         boolean isConst = false, isVolatile = false, isRestrict = false;

         if (LT(1) != IToken.tSTAR) {
            backup(mark);
            break;
         }

         last = consume(IToken.tSTAR);
         int startOffset = mark.getOffset();
         for (;;) {
            IToken t = LA(1);
            switch (LT(1)) {
               case IToken.t_const:
                  last = consume(IToken.t_const);
                  isConst = true;
                  break;
               case IToken.t_volatile:
                  last = consume(IToken.t_volatile);
                  isVolatile = true;
                  break;
               case IToken.t_restrict:
                  last = consume(IToken.t_restrict);
                  isRestrict = true;
                  break;
            }

            if (t == LA(1))
               break;
         }

         IASTPointerOperator po = createPointer();
         ((ASTNode) po).setOffsetAndLength(startOffset, last.getEndOffset()
               - startOffset);
         ((ICASTPointer) po).setConst(isConst);
         ((ICASTPointer) po).setVolatile(isVolatile);
         ((ICASTPointer) po).setRestrict(isRestrict);
         pointerOps.add(po);
      }
   }

   /**
    * @return
    */
   protected ICASTPointer createPointer() {
      return new CASTPointer();
   }

   protected IASTDeclSpecifier declSpecifierSeq(boolean parm)
         throws BacktrackException, EndOfFileException {
      Flags flags = new Flags(parm);

      int startingOffset = LA(1).getOffset();
      int storageClass = IASTDeclSpecifier.sc_unspecified;
      boolean isInline = false;
      boolean isConst = false, isRestrict = false, isVolatile = false;
      boolean isShort = false, isLong = false, isUnsigned = false, isIdentifier = false, isSigned = false, isLongLong = false;
      int simpleType = IASTSimpleDeclSpecifier.t_unspecified;
      IToken identifier = null;
      IASTCompositeTypeSpecifier structSpec = null;
      IASTElaboratedTypeSpecifier elabSpec = null;
      IASTEnumerationSpecifier enumSpec = null;
      IASTExpression typeofExpression = null;
      boolean isTypedef = false;
      IToken last = null;

      declSpecifiers: for (;;) {
         switch (LT(1)) {
            //Storage Class Specifiers
            case IToken.t_auto:
               last = consume();
               storageClass = IASTDeclSpecifier.sc_auto;
               break;
            case IToken.t_register:
               storageClass = IASTDeclSpecifier.sc_register;
               last = consume();
               break;
            case IToken.t_static:
               storageClass = IASTDeclSpecifier.sc_static;
               last = consume();
               break;
            case IToken.t_extern:
               storageClass = IASTDeclSpecifier.sc_extern;
               last = consume();
               break;
            case IToken.t_typedef:
               isTypedef = true;
               storageClass = IASTDeclSpecifier.sc_typedef;
               last = consume();
               break;

            //Function Specifier
            case IToken.t_inline:
               isInline = true;
               last = consume();
               break;

            //Type Qualifiers
            case IToken.t_const:
               isConst = true;
               last = consume();
               break;
            case IToken.t_volatile:
               isVolatile = true;
               last = consume();
               break;
            case IToken.t_restrict:
               isRestrict = true;
               last = consume();
               break;

            //Type Specifiers
            case IToken.t_void:
               flags.setEncounteredRawType(true);
               last = consume();
               simpleType = IASTSimpleDeclSpecifier.t_void;
               break;
            case IToken.t_char:
               flags.setEncounteredRawType(true);
               last = consume();
               simpleType = IASTSimpleDeclSpecifier.t_char;
               break;
            case IToken.t_short:
               flags.setEncounteredRawType(true);
               last = consume();
               isShort = true;
               break;
            case IToken.t_int:
               flags.setEncounteredRawType(true);
               last = consume();
               simpleType = IASTSimpleDeclSpecifier.t_int;
               break;
            case IToken.t_long:
               flags.setEncounteredRawType(true);
               last = consume();
               if (isLong) {
                  isLongLong = true;
                  isLong = false;
               } else
                  isLong = true;
               break;
            case IToken.t_float:
               flags.setEncounteredRawType(true);
               last = consume();
               simpleType = IASTSimpleDeclSpecifier.t_float;
               break;
            case IToken.t_double:
               flags.setEncounteredRawType(true);
               last = consume();
               simpleType = IASTSimpleDeclSpecifier.t_double;
               break;
            case IToken.t_signed:
               flags.setEncounteredRawType(true);
               last = consume();
               isSigned = true;
               break;
            case IToken.t_unsigned:
               flags.setEncounteredRawType(true);
               last = consume();
               isUnsigned = true;
               break;
            case IToken.t__Bool:
               flags.setEncounteredRawType(true);
               last = consume();
               simpleType = ICASTSimpleDeclSpecifier.t_Bool;
               break;
            case IToken.t__Complex:
               last = consume(IToken.t__Complex);
               simpleType = ICASTSimpleDeclSpecifier.t_Complex;
               break;
            case IToken.t__Imaginary:
               last = consume(IToken.t__Imaginary);
               simpleType = ICASTSimpleDeclSpecifier.t_Imaginary;
               break;

            case IToken.tIDENTIFIER:
               // TODO - Kludgy way to handle constructors/destructors
               if (flags.haveEncounteredRawType()) {
                  break declSpecifiers;
               }
               if (parm && flags.haveEncounteredTypename()) {
                  break declSpecifiers;
               }
               if (lookAheadForDeclarator(flags)) {
                  break declSpecifiers;
               }
               switch (LT(2)) {
                  case IToken.tLPAREN:
                     if (isTypedef)
                        break;
                  case IToken.tSEMI:
                  case IToken.tASSIGN:
                     //TODO more
                     break declSpecifiers;
               }

               identifier = identifier();
               last = identifier;
               isIdentifier = true;
               flags.setEncounteredTypename(true);
               break;
            case IToken.t_struct:
            case IToken.t_union:
               if (flags.haveEncounteredTypename())
                  throwBacktrack(LA(1));
               try {
                  structSpec = structOrUnionSpecifier();
                  flags.setEncounteredTypename(true);
                  break;
               } catch (BacktrackException bt) {
                  elabSpec = elaboratedTypeSpecifier();
                  flags.setEncounteredTypename(true);
                  break;
               }
            case IToken.t_enum:
               if (flags.haveEncounteredTypename())
                  throwBacktrack(LA(1));
               try {
                  enumSpec = enumSpecifier();
                  flags.setEncounteredTypename(true);
                  break;
               } catch (BacktrackException bt) {
                  // this is an elaborated class specifier
                  elabSpec = elaboratedTypeSpecifier();
                  flags.setEncounteredTypename(true);
                  break;
               }
            default:
               if (supportTypeOfUnaries && LT(1) == IGCCToken.t_typeof) {
                  typeofExpression = unaryTypeofExpression();
                  if (typeofExpression != null) {
                     flags.setEncounteredTypename(true);
                  }
               }
               break declSpecifiers;
         }
      }

      if (structSpec != null) {
         ((ASTNode) structSpec).setOffsetAndLength(startingOffset,
               calculateEndOffset(structSpec) - startingOffset);
         structSpec.setConst(isConst);
         ((ICASTCompositeTypeSpecifier) structSpec).setRestrict(isRestrict);
         structSpec.setVolatile(isVolatile);
         structSpec.setInline(isInline);
         structSpec.setStorageClass(storageClass);

         return structSpec;
      }

      if (enumSpec != null) {
         ((ASTNode) enumSpec).setOffsetAndLength(startingOffset,
               calculateEndOffset(enumSpec) - startingOffset);
         enumSpec.setConst(isConst);
         ((CASTEnumerationSpecifier) enumSpec).setRestrict(isRestrict);
         enumSpec.setVolatile(isVolatile);
         enumSpec.setInline(isInline);
         enumSpec.setStorageClass(storageClass);
         return enumSpec;

      }
      if (elabSpec != null) {
         ((ASTNode) elabSpec).setOffsetAndLength(startingOffset,
               calculateEndOffset(elabSpec) - startingOffset);
         elabSpec.setConst(isConst);
         ((CASTElaboratedTypeSpecifier) elabSpec).setRestrict(isRestrict);
         elabSpec.setVolatile(isVolatile);
         elabSpec.setInline(isInline);
         elabSpec.setStorageClass(storageClass);

         return elabSpec;
      }
      if (isIdentifier) {
         ICASTTypedefNameSpecifier declSpec = createNamedTypeSpecifier();
         declSpec.setConst(isConst);
         declSpec.setRestrict(isRestrict);
         declSpec.setVolatile(isVolatile);
         declSpec.setInline(isInline);
         declSpec.setStorageClass(storageClass);

         ((ASTNode) declSpec).setOffsetAndLength(startingOffset, last
               .getEndOffset()
               - startingOffset);
         IASTName name = createName(identifier);
         declSpec.setName(name);
         name.setParent(declSpec);
         name.setPropertyInParent(IASTNamedTypeSpecifier.NAME);
         return declSpec;
      }
      ICASTSimpleDeclSpecifier declSpec = createSimpleTypeSpecifier();
      declSpec.setConst(isConst);
      declSpec.setRestrict(isRestrict);
      declSpec.setVolatile(isVolatile);
      declSpec.setInline(isInline);
      declSpec.setStorageClass(storageClass);

      declSpec.setType(simpleType);
      declSpec.setLong(isLong);
      declSpec.setLongLong(isLongLong);
      declSpec.setUnsigned(isUnsigned);
      declSpec.setSigned(isSigned);
      declSpec.setShort(isShort);
      ((ASTNode) declSpec).setOffsetAndLength(startingOffset,
            (last != null) ? last.getEndOffset() - startingOffset : 0);
      return declSpec;
   }

   /**
    * @return
    */
   protected ICASTSimpleDeclSpecifier createSimpleTypeSpecifier() {
      return new CASTSimpleDeclSpecifier();
   }

   /**
    * @return
    */
   protected ICASTTypedefNameSpecifier createNamedTypeSpecifier() {
      return new CASTTypedefNameSpecifier();
   }

   /**
    * Parse a class/struct/union definition.
    * 
    * classSpecifier : classKey name (baseClause)? "{" (memberSpecification)*
    * "}"
    * 
    * @param owner
    *           IParserCallback object that represents the declaration that owns
    *           this classSpecifier
    * 
    * @return TODO
    * @throws BacktrackException
    *            request a backtrack
    */
   protected ICASTCompositeTypeSpecifier structOrUnionSpecifier()
         throws BacktrackException, EndOfFileException {

      int classKind = 0;
      IToken classKey = null;
      IToken mark = mark();

      // class key
      switch (LT(1)) {
         case IToken.t_struct:
            classKey = consume();
            classKind = IASTCompositeTypeSpecifier.k_struct;
            break;
         case IToken.t_union:
            classKey = consume();
            classKind = IASTCompositeTypeSpecifier.k_union;
            break;
         default:
            throwBacktrack(mark.getOffset(), mark.getLength());
      }

      IToken nameToken = null;
      // class name
      if (LT(1) == IToken.tIDENTIFIER) {
         nameToken = identifier();
      }

      if (LT(1) != IToken.tLBRACE) {
         IToken errorPoint = LA(1);
         backup(mark);
         throwBacktrack(errorPoint.getOffset(), errorPoint.getLength());
      }

      consume(IToken.tLBRACE);

      IASTName name = null;
      if (nameToken != null)
         name = createName(nameToken);
      else
         name = createName();

      ICASTCompositeTypeSpecifier result = createCompositeTypeSpecifier();

      result.setKey(classKind);
      ((ASTNode) result).setOffset(classKey.getOffset());

      result.setName(name);
      if (name != null) {
         name.setParent(result);
         name.setPropertyInParent(IASTCompositeTypeSpecifier.TYPE_NAME);
      }

      memberDeclarationLoop: while (LT(1) != IToken.tRBRACE) {
         int checkToken = LA(1).hashCode();
         switch (LT(1)) {
            case IToken.tRBRACE:
               consume(IToken.tRBRACE);
               break memberDeclarationLoop;
            default:
               try {
                  IASTDeclaration d = declaration();
                  d.setParent(result);
                  d
                        .setPropertyInParent(IASTCompositeTypeSpecifier.MEMBER_DECLARATION);
                  result.addMemberDeclaration(d);
               } catch (BacktrackException bt) {
                  if (checkToken == LA(1).hashCode())
                     failParseWithErrorHandling();
               }
         }
         if (checkToken == LA(1).hashCode())
            failParseWithErrorHandling();
      }
      // consume the }
      int endOffset = consume(IToken.tRBRACE).getEndOffset();
      ((CASTNode) result).setLength(endOffset - classKey.getOffset());
      return result;
   }

   /**
    * @return
    */
   protected IASTName createName() {
      return new CASTName();
   }

   /**
    * @return
    */
   protected ICASTCompositeTypeSpecifier createCompositeTypeSpecifier() {
      return new CASTCompositeTypeSpecifier();
   }

   protected ICASTElaboratedTypeSpecifier elaboratedTypeSpecifier()
         throws BacktrackException, EndOfFileException {
      // this is an elaborated class specifier
      IToken t = consume();
      int eck = 0;

      switch (t.getType()) {
         case IToken.t_struct:
            eck = IASTElaboratedTypeSpecifier.k_struct;
            break;
         case IToken.t_union:
            eck = IASTElaboratedTypeSpecifier.k_union;
            break;
         case IToken.t_enum:
            eck = IASTElaboratedTypeSpecifier.k_enum;
            break;
         default:
            backup(t);
            throwBacktrack(t.getOffset(), t.getLength());
      }

      IToken identifier = identifier();
      IASTName name = createName(identifier);
      ICASTElaboratedTypeSpecifier result = createElaboratedTypeSpecifier();
      result.setName(name);
      name.setParent(result);
      name.setPropertyInParent(IASTElaboratedTypeSpecifier.TYPE_NAME);
      result.setKind(eck);
      ((ASTNode) result).setOffsetAndLength(t.getOffset(),
            calculateEndOffset(name) - t.getOffset());
      return result;
   }

   /**
    * @return
    */
   protected ICASTElaboratedTypeSpecifier createElaboratedTypeSpecifier() {
      return new CASTElaboratedTypeSpecifier();
   }

   protected IASTDeclarator initDeclarator() throws EndOfFileException,
         BacktrackException {
      IASTDeclarator d = declarator();

      try {
         //			astFactory.constructExpressions(constructInitializers);
         IASTInitializer i = optionalCInitializer();
         if (i != null) {
            d.setInitializer(i);
            i.setParent(d);
            i.setPropertyInParent(IASTDeclarator.INITIALIZER);
            ((ASTNode) d).setLength(calculateEndOffset(i)
                  - ((ASTNode) d).getOffset());
         }
         return d;
      } finally {
         //			astFactory.constructExpressions(true);
      }
   }

   protected IASTDeclarator declarator() throws EndOfFileException,
         BacktrackException {
      IASTDeclarator innerDecl = null;
      IASTName declaratorName = null;
      IToken la = LA(1);
      int startingOffset = la.getOffset();
      int finalOffset = startingOffset;
      la = null;
      List pointerOps = new ArrayList(DEFAULT_POINTEROPS_LIST_SIZE);
      List parameters = Collections.EMPTY_LIST;
      List arrayMods = Collections.EMPTY_LIST;
      boolean encounteredVarArgs = false;
      IASTExpression bitField = null;
      boolean isFunction = false;
      IASTName[] parmNames = null;
      IASTDeclaration[] parmDeclarations = null;
      int numKnRCParms = 0;

      overallLoop: do {

         consumePointerOperators(pointerOps);
         if (!pointerOps.isEmpty()) {
            finalOffset = calculateEndOffset((IASTPointerOperator) pointerOps
                  .get(pointerOps.size() - 1));
         }

         if (LT(1) == IToken.tLPAREN) {
            consume();
            innerDecl = declarator();
            finalOffset = consume(IToken.tRPAREN).getEndOffset();
            declaratorName = createName();
         } else if (LT(1) == IToken.tIDENTIFIER) {
            declaratorName = createName(identifier());
            finalOffset = calculateEndOffset(declaratorName);
         } else
            declaratorName = createName();

         for (;;) {
            switch (LT(1)) {
               case IToken.tLPAREN:
                  // parameterDeclarationClause
                  //                        d.setIsFunction(true);
                  // TODO need to create a temporary scope object here
                  IToken last = consume(IToken.tLPAREN);
                  finalOffset = last.getEndOffset();
                  isFunction = true;
                  boolean seenParameter = false;

                  // count the number of K&R C parameters (0 K&R C parameters
                  // essentially means it's not K&R C)
                  numKnRCParms = countKnRCParms();

                  if (numKnRCParms > 0) { // KnR C parameters were found so
                     // handle the declarator accordingly
                     parmNames = new IASTName[numKnRCParms];
                     parmDeclarations = new IASTDeclaration[numKnRCParms];

                     for (int i = 0; i <= parmNames.length; i++) {
                        switch (LT(1)) {
                           case IToken.tCOMMA:
                              last = consume();
                              seenParameter = false;
                           case IToken.tIDENTIFIER:
                              if (seenParameter)
                                 throwBacktrack(startingOffset, last
                                       .getEndOffset()
                                       - startingOffset);

                              parmNames[i] = createName(identifier());

                              seenParameter = true;
                              break;
                           case IToken.tRPAREN:
                              last = consume();
                              break;
                           default:
                              break;
                        }
                     }

                     // now that the parameter names are parsed, parse the
                     // parameter declarations
                     for (int i = 0; i < numKnRCParms
                           && LT(1) != IToken.tLBRACE; i++) { // max
                        // parameter
                        // declarations
                        // same as
                        // parameter
                        // name count
                        // (could be
                        // less)
                        try {
                           boolean hasValidDecltors = true;

                           IASTDeclaration decl = simpleDeclaration();
                           IASTSimpleDeclaration declaration = null;
                           if (decl instanceof IASTSimpleDeclaration) {
                              declaration = ((IASTSimpleDeclaration) decl);

                              IASTDeclarator[] decltors = declaration
                                    .getDeclarators();
                              for (int k = 0; k < decltors.length; k++) {
                                 boolean decltorOk = false;
                                 for (int j = 0; j < parmNames.length; j++) {
                                    if (CharArrayUtils.equals(decltors[k]
                                          .getName().toCharArray(),
                                          parmNames[j].toCharArray())) {
                                       decltorOk = true;
                                       break;
                                    }
                                 }
                                 if (!decltorOk)
                                    hasValidDecltors = false;
                              }
                           } else {
                              hasValidDecltors = false;
                           }

                           if (hasValidDecltors) {
                              parmDeclarations[i] = declaration;
                           } else {
                              parmDeclarations[i] = createKnRCProblemDeclaration(
                                    ((ASTNode) declaration).getLength(),
                                    ((ASTNode) declaration).getOffset());
                           }
                        } catch (BacktrackException b) {
                           parmDeclarations[i] = createKnRCProblemDeclaration(b
                                 .getLength(), b.getOffset());
                        }
                     }

                     break overallLoop;
                  }

                  parameterDeclarationLoop: for (;;) {
                     switch (LT(1)) {
                        case IToken.tRPAREN:
                           last = consume();
                           finalOffset = last.getEndOffset();
                           break parameterDeclarationLoop;
                        case IToken.tELLIPSIS:
                           last = consume();
                           encounteredVarArgs = true;
                           finalOffset = last.getEndOffset();
                           break;
                        case IToken.tCOMMA:
                           last = consume();
                           finalOffset = last.getEndOffset();
                           seenParameter = false;
                           break;
                        default:
                           if (seenParameter)
                              throwBacktrack(startingOffset, last
                                    .getEndOffset()
                                    - startingOffset);
                           IASTParameterDeclaration pd = parameterDeclaration();
                           finalOffset = calculateEndOffset(pd);
                           if (parameters == Collections.EMPTY_LIST)
                              parameters = new ArrayList(
                                    DEFAULT_PARAMETERS_LIST_SIZE);
                           parameters.add(pd);
                           seenParameter = true;
                     }
                  }

                  break;
               case IToken.tLBRACKET:
                  if (arrayMods == Collections.EMPTY_LIST)
                     arrayMods = new ArrayList(DEFAULT_POINTEROPS_LIST_SIZE);
                  consumeArrayModifiers(arrayMods);
                  if (!arrayMods.isEmpty())
                     finalOffset = calculateEndOffset((IASTArrayModifier) arrayMods
                           .get(arrayMods.size() - 1));
                  continue;
               case IToken.tCOLON:
                  consume(IToken.tCOLON);
                  bitField = constantExpression();
                  finalOffset = calculateEndOffset(bitField);
               default:
                  break;
            }
            break;
         }
         if (LA(1).getType() != IToken.tIDENTIFIER)
            break;

      } while (true);

      IASTDeclarator d = null;
      if (numKnRCParms > 0) {
         ICASTKnRFunctionDeclarator functionDecltor = createKnRFunctionDeclarator();
         for (int i = 0; i < parmDeclarations.length; ++i) {
            if (parmDeclarations[i] != null
                  && !(parmDeclarations[i] instanceof IASTProblemDeclaration)) {
               parmDeclarations[i].setParent(functionDecltor);
               parmDeclarations[i]
                     .setPropertyInParent(ICASTKnRFunctionDeclarator.FUNCTION_PARAMETER);
               finalOffset = calculateEndOffset(parmDeclarations[i]);
            }
         }
         functionDecltor.setParameterDeclarations(parmDeclarations);
         functionDecltor.setParameterNames(parmNames);
         if (declaratorName != null) {
            functionDecltor.setName(declaratorName);
            declaratorName.setParent(functionDecltor);
            declaratorName.setPropertyInParent(IASTDeclarator.DECLARATOR_NAME);
         }

         for (int i = 0; i < parmNames.length; ++i) {
            parmNames[i].setParent(functionDecltor);
            parmNames[i]
                  .setPropertyInParent(ICASTKnRFunctionDeclarator.PARAMETER_NAME);
         }

         d = functionDecltor;
      } else if (isFunction) {
         IASTStandardFunctionDeclarator fc = createFunctionDeclarator();
         fc.setVarArgs(encounteredVarArgs);
         for (int i = 0; i < parameters.size(); ++i) {
            IASTParameterDeclaration p = (IASTParameterDeclaration) parameters
                  .get(i);
            p.setParent(fc);
            p
                  .setPropertyInParent(IASTStandardFunctionDeclarator.FUNCTION_PARAMETER);
            fc.addParameterDeclaration(p);
         }
         d = fc;
      } else if (arrayMods != Collections.EMPTY_LIST) {
         d = createArrayDeclarator();
         for (int i = 0; i < arrayMods.size(); ++i) {
            IASTArrayModifier m = (IASTArrayModifier) arrayMods.get(i);
            m.setParent(d);
            m.setPropertyInParent(IASTArrayDeclarator.ARRAY_MODIFIER);
            ((IASTArrayDeclarator) d).addArrayModifier(m);
         }
      } else if (bitField != null) {
         IASTFieldDeclarator fl = createFieldDeclarator();
         fl.setBitFieldSize(bitField);
         d = fl;
      } else {
         d = createDeclarator();
      }
      for (int i = 0; i < pointerOps.size(); ++i) {
         IASTPointerOperator po = (IASTPointerOperator) pointerOps.get(i);
         d.addPointerOperator(po);
         po.setParent(d);
         po.setPropertyInParent(IASTDeclarator.POINTER_OPERATOR);
      }
      if (innerDecl != null) {
         d.setNestedDeclarator(innerDecl);
         innerDecl.setParent(d);
         innerDecl.setPropertyInParent(IASTDeclarator.NESTED_DECLARATOR);
      }
      if (declaratorName != null) {
         d.setName(declaratorName);
         declaratorName.setParent(d);
         declaratorName.setPropertyInParent(IASTDeclarator.DECLARATOR_NAME);
      }

      ((ASTNode) d).setOffsetAndLength(startingOffset, finalOffset
            - startingOffset);
      return d;
   }

   protected IASTArrayDeclarator createArrayDeclarator() {
      return new CASTArrayDeclarator();
   }

   /**
    * @return
    */
   protected IASTFieldDeclarator createFieldDeclarator() {
      return new CASTFieldDeclarator();
   }

   /**
    * @return
    */
   protected IASTStandardFunctionDeclarator createFunctionDeclarator() {
      return new CASTFunctionDeclarator();
   }

   /**
    * @return
    */
   protected ICASTKnRFunctionDeclarator createKnRFunctionDeclarator() {
      return new CASTKnRFunctionDeclarator();
   }

   /**
    * @param t
    * @return
    */
   protected IASTName createName(IToken t) {
      IASTName n = new CASTName(t.getCharImage());
      ((ASTNode) n).setOffsetAndLength(t.getOffset(), t.getEndOffset()
            - t.getOffset());
      return n;
   }

   /**
    * @return
    */
   protected IASTDeclarator createDeclarator() {
      return new CASTDeclarator();
   }

   protected void consumeArrayModifiers(List arrayMods)
         throws EndOfFileException, BacktrackException {

      while (LT(1) == IToken.tLBRACKET) {
         //eat the '['
         int startOffset = consume(IToken.tLBRACKET).getOffset();

         boolean isStatic = false;
         boolean isConst = false;
         boolean isRestrict = false;
         boolean isVolatile = false;
         boolean isVarSized = false;

         outerLoop: do {
            switch (LT(1)) {
               case IToken.t_static:
                  isStatic = true;
                  consume();
                  break;
               case IToken.t_const:
                  isConst = true;
                  consume();
                  break;
               case IToken.t_volatile:
                  isVolatile = true;
                  consume();
                  break;
               case IToken.t_restrict:
                  isRestrict = true;
                  consume();
                  break;
               case IToken.tSTAR:
                  isVarSized = true;
                  consume();
               //deliberate fall through
               default:
                  break outerLoop;
            }
         } while (true);

         IASTExpression exp = null;

         if (LT(1) != IToken.tRBRACKET) {
            if (!(isStatic || isRestrict || isConst || isVolatile))
               exp = assignmentExpression();
            else
               exp = constantExpression();
         }
         int lastOffset = consume(IToken.tRBRACKET).getEndOffset();

         IASTArrayModifier arrayMod = null;
         if (!(isStatic || isRestrict || isConst || isVolatile))
            arrayMod = createArrayModifier();
         else {
            ICASTArrayModifier temp = createCArrayModifier();
            temp.setStatic(isStatic);
            temp.setConst(isConst);
            temp.setVolatile(isVolatile);
            temp.setRestrict(isRestrict);
            temp.setVariableSized(isVarSized);
            arrayMod = temp;
         }
         ((ASTNode) arrayMod).setOffsetAndLength(startOffset, lastOffset
               - startOffset);
         if (exp != null) {
            arrayMod.setConstantExpression(exp);
            exp.setParent(arrayMod);
            exp.setPropertyInParent(IASTArrayModifier.CONSTANT_EXPRESSION);
         }
         arrayMods.add(arrayMod);
      }
   }

   /**
    * @return
    */
   protected ICASTArrayModifier createCArrayModifier() {
      return new CASTModifiedArrayModifier();
   }

   /**
    * @return
    */
   protected IASTArrayModifier createArrayModifier() {
      return new CASTArrayModifier();
   }

   protected IASTParameterDeclaration parameterDeclaration()
         throws BacktrackException, EndOfFileException {
      IToken current = LA(1);
      int startingOffset = current.getOffset();
      IASTDeclSpecifier declSpec = declSpecifierSeq(true);

      IASTDeclarator declarator = null;
      if (LT(1) != IToken.tSEMI)
         declarator = initDeclarator();

      if (current == LA(1))
         throwBacktrack(current.getOffset(), figureEndOffset(declSpec,
               declarator)
               - current.getOffset());

      IASTParameterDeclaration result = createParameterDeclaration();
      ((ASTNode) result).setOffsetAndLength(startingOffset, figureEndOffset(
            declSpec, declarator)
            - startingOffset);
      result.setDeclSpecifier(declSpec);
      declSpec.setParent(result);
      declSpec.setPropertyInParent(IASTParameterDeclaration.DECL_SPECIFIER);
      result.setDeclarator(declarator);
      declarator.setParent(result);
      declarator.setPropertyInParent(IASTParameterDeclaration.DECLARATOR);
      return result;
   }

   /**
    * @return
    */
   protected IASTParameterDeclaration createParameterDeclaration() {
      return new CASTParameterDeclaration();
   }

   /**
    * @throws BacktrackException
    */
   protected IASTNode forInitStatement() throws BacktrackException,
         EndOfFileException {
      IToken mark = mark();
      try {
         IASTExpression e = expression();
         consume(IToken.tSEMI);
         //TODO is this a problem? Should we wrap this in an expression
         // statement?
         return e;
      } catch (BacktrackException bt) {
         backup(mark);
         try {
            return simpleDeclaration();
         } catch (BacktrackException b) {
            IASTProblem p = failParse(b);
            IASTProblemExpression pe = createProblemExpression();
            ((CASTNode) pe).setOffsetAndLength(((CASTNode) p));
            pe.setProblem(p);
            p.setParent(pe);
            p.setPropertyInParent(IASTProblemHolder.PROBLEM);
            return pe;
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#getTranslationUnit()
    */
   protected IASTTranslationUnit getTranslationUnit() {
      return translationUnit;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createCompoundStatement()
    */
   protected IASTCompoundStatement createCompoundStatement() {
      return new CASTCompoundStatement();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createBinaryExpression()
    */
   protected IASTBinaryExpression createBinaryExpression() {
      return new CASTBinaryExpression();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createConditionalExpression()
    */
   protected IASTConditionalExpression createConditionalExpression() {
      return new CASTConditionalExpression();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createUnaryExpression()
    */
   protected IASTUnaryExpression createUnaryExpression() {
      return new CASTUnaryExpression();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createCompoundStatementExpression()
    */
   protected IGNUASTCompoundStatementExpression createCompoundStatementExpression() {
      return new CASTCompoundStatementExpression();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createExpressionList()
    */
   protected IASTExpressionList createExpressionList() {
      return new CASTExpressionList();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createEnumerator()
    */
   protected IASTEnumerator createEnumerator() {
      return new CASTEnumerator();
   }

   /**
    * @return
    */
   protected IASTLabelStatement createLabelStatement() {
      return new CASTLabelStatement();
   }

   /**
    * @return
    */
   protected IASTGotoStatement createGoToStatement() {
      return new CASTGotoStatement();
   }

   /**
    * @return
    */
   protected IASTReturnStatement createReturnStatement() {
      return new CASTReturnStatement();
   }

   /**
    * @return
    */
   protected IASTForStatement createForStatement() {
      return new CASTForStatement();
   }

   /**
    * @return
    */
   protected IASTContinueStatement createContinueStatement() {
      return new CASTContinueStatement();
   }

   /**
    * @return
    */
   protected IASTDoStatement createDoStatement() {
      return new CASTDoStatement();
   }

   /**
    * @return
    */
   protected IASTBreakStatement createBreakStatement() {
      return new CASTBreakStatement();
   }

   /**
    * @return
    */
   protected IASTWhileStatement createWhileStatement() {
      return new CASTWhileStatement();
   }

   /**
    * @return
    */
   protected IASTNullStatement createNullStatement() {
      return new CASTNullStatement();
   }

   /**
    * @return
    */
   protected IASTSwitchStatement createSwitchStatement() {
      return new CASTSwitchStatement();
   }

   /**
    * @return
    */
   protected IASTIfStatement createIfStatement() {
      return new CASTIfStatement();
   }

   /**
    * @return
    */
   protected IASTDefaultStatement createDefaultStatement() {
      return new CASTDefaultStatement();
   }

   /**
    * @return
    */
   protected IASTCaseStatement createCaseStatement() {
      return new CASTCaseStatement();
   }

   /**
    * @return
    */
   protected IASTExpressionStatement createExpressionStatement() {
      return new CASTExpressionStatement();
   }

   /**
    * @return
    */
   protected IASTDeclarationStatement createDeclarationStatement() {
      return new CASTDeclarationStatement();
   }

   /**
    * @return
    */
   protected IASTASMDeclaration createASMDirective() {
      return new CASTASMDeclaration();
   }

   protected IASTEnumerationSpecifier createEnumerationSpecifier() {
      return new CASTEnumerationSpecifier();
   }

   /**
    * @return
    */
   protected IASTCastExpression createCastExpression() {
      return new CASTCastExpression();
   }

   protected IASTStatement statement() throws EndOfFileException,
         BacktrackException {
      switch (LT(1)) {
         // labeled statements
         case IToken.t_case:
            return parseCaseStatement();
         case IToken.t_default:
            return parseDefaultStatement();
         // compound statement
         case IToken.tLBRACE:
            return parseCompoundStatement();
         // selection statement
         case IToken.t_if:
            return parseIfStatement();
         case IToken.t_switch:
            return parseSwitchStatement();
         //iteration statements
         case IToken.t_while:
            return parseWhileStatement();
         case IToken.t_do:
            return parseDoStatement();
         case IToken.t_for:
            return parseForStatement();
         //jump statement
         case IToken.t_break:
            return parseBreakStatement();
         case IToken.t_continue:
            return parseContinueStatement();
         case IToken.t_return:
            return parseReturnStatement();
         case IToken.t_goto:
            return parseGotoStatement();
         case IToken.tSEMI:
            return parseNullStatement();
         default:
            // can be many things:
            // label
            if (LT(1) == IToken.tIDENTIFIER && LT(2) == IToken.tCOLON) {
               return parseLabelStatement();
            }

            return parseDeclarationOrExpressionStatement();
      }

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser#nullifyTranslationUnit()
    */
   protected void nullifyTranslationUnit() {
      translationUnit = null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser#createProblemStatement()
    */
   protected IASTProblemStatement createProblemStatement() {
      return new CASTProblemStatement();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser#createProblemExpression()
    */
   protected IASTProblemExpression createProblemExpression() {
      return new CASTProblemExpression();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser#createProblem(int,
    *      int, int)
    */
   protected IASTProblem createProblem(int signal, int offset, int length) {
      IASTProblem result = new CASTProblem(signal, EMPTY_STRING, false, true);
      ((ASTNode) result).setOffsetAndLength(offset, length);
      return result;
   }

   private int countKnRCParms() {
      IToken mark = null;
      int parmCount = 0;
      boolean previousWasIdentifier = false;

      try {
         mark = mark();

         // starts at the beginning of the parameter list
         for (;;) {
            if (LT(1) == IToken.tCOMMA) {
               consume();
               previousWasIdentifier = false;
            } else if (LT(1) == IToken.tIDENTIFIER) {
               consume();
               if (previousWasIdentifier == true) {
                  backup(mark);
                  return 0; // i.e. KnR C won't have int f(typedef x) char
                  // x; {}
               }
               previousWasIdentifier = true;
               parmCount++;
            } else if (LT(1) == IToken.tRPAREN) {
               consume();
               break;
            } else {
               backup(mark);
               return 0; // i.e. KnR C won't have int f(char) char x; {}
            }
         }

         // if the next token is a tSEMI then the declaration was a regular
         // declaration statement i.e. int f(type_def);
         if (LT(1) == IToken.tSEMI) {
            backup(mark);
            return 0;
         }

         // look ahead for the start of the function body, if end of file is
         // found then return 0 parameters found (implies not KnR C)
         while (LT(1) != IToken.tLBRACE) {
            consume();
         }

         backup(mark);
         return parmCount;
      } catch (EndOfFileException eof) {
         if (mark != null)
            backup(mark);

         return 0;
      }
   }

   private IASTProblemDeclaration createKnRCProblemDeclaration(int length,
         int offset) throws EndOfFileException {
      IASTProblem p = createProblem(IASTProblem.SYNTAX_ERROR, offset, length);
      IASTProblemDeclaration pd = createProblemDeclaration();
      pd.setProblem(p);
      ((ASTNode) pd).setOffsetAndLength(((ASTNode) p).getOffset(),
            ((ASTNode) p).getLength());
      p.setParent(pd);
      p.setPropertyInParent(IASTProblemHolder.PROBLEM);

      // consume until LBRACE is found (to leave off at the function body and
      // continue from there)
      while (LT(1) != IToken.tLBRACE)
         consume();

      return pd;
   }

}