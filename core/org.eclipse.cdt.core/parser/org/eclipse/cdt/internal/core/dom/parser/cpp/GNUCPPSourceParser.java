/**********************************************************************
 * Copyright (c) 2002,2003, 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

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
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblemTypeId;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisiblityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.BacktrackException;
import org.eclipse.cdt.internal.core.parser.SimpleDeclarationStrategy;
import org.eclipse.cdt.internal.core.parser.TemplateParameterManager;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;

/**
 * This is our implementation of the IParser interface, serving as a parser for
 * GNU C and C++.
 * 
 * From time to time we will make reference to the ANSI ISO specifications.
 * 
 * @author jcamelon
 */
public class GNUCPPSourceParser extends AbstractGNUSourceCodeParser {

   private static final int        DEFAULT_CATCH_HANDLER_LIST_SIZE = 4;
   private ScopeStack              templateIdScopes                = new ScopeStack();
   protected CPPASTTranslationUnit translationUnit;

   private static class ScopeStack {
      private int[] stack;

      private int   index = -1;

      public ScopeStack() {
         stack = new int[8];
      }

      private void grow() {
         int[] newStack = new int[stack.length << 1];
         System.arraycopy(stack, 0, newStack, 0, stack.length);
         stack = newStack;
      }

      final public void push(int i) {
         if (++index == stack.length)
            grow();
         stack[index] = i;
      }

      final public int pop() {
         if (index >= 0)
            return stack[index--];
         return -1;
      }

      final public int peek() {
         if (index >= 0)
            return stack[index];
         return -1;
      }

      final public int size() {
         return index + 1;
      }
   }

   /**
    * Consumes template parameters.
    * 
    * @param previousLast
    *           Previous "last" token (returned if nothing was consumed)
    * @return Last consumed token, or <code>previousLast</code> if nothing was
    *         consumed
    * @throws BacktrackException
    *            request a backtrack
    */
   protected IToken consumeTemplateParameters(IToken previousLast)
         throws EndOfFileException, BacktrackException {
      int startingOffset = previousLast == null ? LA(1).getOffset()
            : previousLast.getOffset();
      IToken last = previousLast;
      if (LT(1) == IToken.tLT) {
         last = consume(IToken.tLT);
         // until we get all the names sorted out
         ScopeStack scopes = new ScopeStack();
         scopes.push(IToken.tLT);

         while (scopes.size() > 0) {
            int top;
            last = consume();

            switch (last.getType()) {
               case IToken.tGT:
                  if (scopes.peek() == IToken.tLT) {
                     scopes.pop();
                  }
                  break;
               case IToken.tRBRACKET:
                  do {
                     top = scopes.pop();
                  } while (scopes.size() > 0
                        && (top == IToken.tGT || top == IToken.tLT));
                  if (top != IToken.tLBRACKET)
                     throwBacktrack(startingOffset, last.getEndOffset()
                           - startingOffset);

                  break;
               case IToken.tRPAREN:
                  do {
                     top = scopes.pop();
                  } while (scopes.size() > 0
                        && (top == IToken.tGT || top == IToken.tLT));
                  if (top != IToken.tLPAREN)
                     throwBacktrack(startingOffset, last.getEndOffset()
                           - startingOffset);

                  break;
               case IToken.tLT:
               case IToken.tLBRACKET:
               case IToken.tLPAREN:
                  scopes.push(last.getType());
                  break;
            }
         }
      }
      return last;
   }

   protected List templateArgumentList() throws EndOfFileException,
         BacktrackException {
      IToken start = LA(1);
      int startingOffset = start.getOffset();
      int endOffset = 0;
      start = null;
      List list = new ArrayList();

      boolean completedArg = false;
      boolean failed = false;

      templateIdScopes.push(IToken.tLT);

      while (LT(1) != IToken.tGT) {
         completedArg = false;

         IToken mark = mark();

         try {
            IASTTypeId typeId = typeId(false);
            list.add(typeId);
            completedArg = true;
         } catch (BacktrackException e) {
            backup(mark);
         } /*
            * catch (ASTSemanticException e) { backup(mark); }
            */

         if (!completedArg) {
            try {
               IASTExpression expression = assignmentExpression();
               list.add(expression);
               completedArg = true;
            } catch (BacktrackException e) {
               backup(mark);
            }
         }

         if (LT(1) == IToken.tCOMMA) {
            consume();
         } else if (LT(1) != IToken.tGT) {
            failed = true;
            endOffset = LA(1).getEndOffset();
            break;
         }
      }

      templateIdScopes.pop();

      if (failed)
         throwBacktrack(startingOffset, endOffset - startingOffset);

      return list;
   }

   /**
    * Parse a name.
    * 
    * name : ("::")? name2 ("::" name2)*
    * 
    * name2 : IDENTIFER : template-id
    * 
    * @throws BacktrackException
    *            request a backtrack
    */
   protected ITokenDuple name() throws BacktrackException, EndOfFileException {

      TemplateParameterManager argumentList = TemplateParameterManager
            .getInstance();

      try {
         IToken first = LA(1);
         IToken last = null;
         IToken mark = mark();

         boolean hasTemplateId = false;

         if (LT(1) == IToken.tCOLONCOLON) {
            argumentList.addSegment(null);
            last = consume(IToken.tCOLONCOLON);
         }

         if (LT(1) == IToken.tCOMPL)
            consume();

         switch (LT(1)) {
            case IToken.tIDENTIFIER:
               last = consume(IToken.tIDENTIFIER);
               last = consumeTemplateArguments(last, argumentList);
               if (last.getType() == IToken.tGT)
                  hasTemplateId = true;
               break;

            default:
               IToken l = LA(1);
               backup(mark);
               throwBacktrack(first.getOffset(), l.getEndOffset()
                     - first.getOffset());
         }

         while (LT(1) == IToken.tCOLONCOLON) {
            last = consume(IToken.tCOLONCOLON);

            if (LT(1) == IToken.t_template)
               consume();

            if (LT(1) == IToken.tCOMPL)
               consume();

            switch (LT(1)) {
               case IToken.t_operator:
                  IToken l = LA(1);
                  backup(mark);
                  throwBacktrack(first.getOffset(), l.getEndOffset()
                        - first.getOffset());
               case IToken.tIDENTIFIER:
                  last = consume();
                  last = consumeTemplateArguments(last, argumentList);
                  if (last.getType() == IToken.tGT)
                     hasTemplateId = true;
            }
         }

         ITokenDuple tokenDuple = TokenFactory
               .createTokenDuple(first, last, (hasTemplateId ? argumentList
                     .getTemplateArgumentsList() : null));
         return tokenDuple;
      } finally {
         TemplateParameterManager.returnInstance(argumentList);
      }

   }

   /**
    * @param last
    * @param argumentList
    * @return @throws
    *         EndOfFileException
    * @throws BacktrackException
    */
   protected IToken consumeTemplateArguments(IToken last,
         TemplateParameterManager argumentList) throws EndOfFileException,
         BacktrackException {
      //        if (language != ParserLanguage.CPP)
      //            return last;
      if (LT(1) == IToken.tLT) {
         IToken secondMark = mark();
         consume(IToken.tLT);
         try {
            List list = templateArgumentList();
            argumentList.addSegment(list);
            last = consume(IToken.tGT);
         } catch (BacktrackException bt) {
            argumentList.addSegment(null);
            backup(secondMark);
         }
      } else {
         argumentList.addSegment(null);
      }
      return last;
   }

   protected ITokenDuple operatorId(IToken originalToken,
         TemplateParameterManager templateArgs) throws BacktrackException,
         EndOfFileException {
      // we know this is an operator
      IToken operatorToken = consume(IToken.t_operator);
      IToken toSend = null;
      if (LA(1).isOperator() || LT(1) == IToken.tLPAREN
            || LT(1) == IToken.tLBRACKET) {
         if ((LT(1) == IToken.t_new || LT(1) == IToken.t_delete)
               && LT(2) == IToken.tLBRACKET && LT(3) == IToken.tRBRACKET) {
            consume();
            consume(IToken.tLBRACKET);
            toSend = consume(IToken.tRBRACKET);
            // vector new and delete operators
         } else if (LT(1) == IToken.tLPAREN && LT(2) == IToken.tRPAREN) {
            // operator ()
            consume(IToken.tLPAREN);
            toSend = consume(IToken.tRPAREN);
         } else if (LT(1) == IToken.tLBRACKET && LT(2) == IToken.tRBRACKET) {
            consume(IToken.tLBRACKET);
            toSend = consume(IToken.tRBRACKET);
         } else if (LA(1).isOperator())
            toSend = consume();
         else
            throwBacktrack(operatorToken.getOffset(), toSend != null ? toSend
                  .getEndOffset()
                  - operatorToken.getOffset() : 0);
      } else {
         // must be a conversion function
         IToken t = LA(1);
         typeId(true);
         if (t != LA(1)) {
            while (t.getNext() != LA(1)) {
               t = t.getNext();
            }
            toSend = t;
         }
      }

      boolean hasTemplateId = (templateArgs != null);
      boolean grabbedNewInstance = false;
      if (templateArgs == null) {
         templateArgs = TemplateParameterManager.getInstance();
         grabbedNewInstance = true;
      }

      try {
         toSend = consumeTemplateArguments(toSend, templateArgs);
         if (toSend.getType() == IToken.tGT) {
            hasTemplateId = true;
         }

         ITokenDuple duple = TokenFactory
               .createTokenDuple(originalToken == null ? operatorToken
                     : originalToken, toSend, (hasTemplateId ? templateArgs
                     .getTemplateArgumentsList() : null));

         return duple;
      } finally {
         if (grabbedNewInstance)
            TemplateParameterManager.returnInstance(templateArgs);
      }
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
   protected void consumePointerOperators(List collection)
         throws EndOfFileException, BacktrackException {

      for (;;) {
         if (LT(1) == IToken.tAMPER) {
            int length = LA(1).getEndOffset() - LA(1).getOffset();
            int o = consume(IToken.tAMPER).getOffset();
            ICPPASTReferenceOperator refOp = createReferenceOperator();
            ((ASTNode) refOp).setOffsetAndLength(o, length);
            collection.add(refOp);
            return;
         }
         IToken last = null;
         IToken mark = mark();
         ITokenDuple nameDuple = null;
         boolean isConst = false, isVolatile = false, isRestrict = false;
         if (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tCOLONCOLON) {
            try {
               nameDuple = name();
               last = nameDuple.getLastToken();
            } catch (BacktrackException bt) {
               backup(mark);
               return;
            }
         }
         if (LT(1) == IToken.tSTAR) {
            last = consume(IToken.tSTAR);
            int starOffset = last.getOffset();

            for (;;) {
               IToken t = LA(1);
               int startingOffset = LA(1).getOffset();
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
                     if (allowCPPRestrict) {
                        last = consume(IToken.t_restrict);
                        isRestrict = true;
                        break;
                     }
                     IToken la = LA(1);
                     throwBacktrack(startingOffset, la.getEndOffset()
                           - startingOffset);

               }
               if (t == LA(1))
                  break;
            }

            IASTPointerOperator po = null;
            if (nameDuple != null) {
               IASTName name = createName(nameDuple);
               ICPPASTPointerToMember p2m = createPointerToMember(isRestrict);
               ((ASTNode) p2m).setOffsetAndLength(starOffset, nameDuple
                     .getEndOffset()
                     - starOffset);
               p2m.setConst(isConst);
               p2m.setVolatile(isVolatile);
               p2m.setName(name);
               name.setParent(p2m);
               name.setPropertyInParent(ICPPASTPointerToMember.NAME);
               if (isRestrict) {
                  IGPPASTPointerToMember newPo = (IGPPASTPointerToMember) p2m;
                  newPo.setRestrict(isRestrict);
                  p2m = newPo;
               }
               po = p2m;

            } else {
               po = createPointer(isRestrict);
               ((ASTNode) po).setOffsetAndLength(starOffset, last
                     .getEndOffset()
                     - starOffset);
               ((IASTPointer) po).setConst(isConst);
               ((IASTPointer) po).setVolatile(isVolatile);
               if (isRestrict) {
                  IGPPASTPointer newPo = (IGPPASTPointer) po;
                  newPo.setRestrict(isRestrict);
                  po = newPo;
               }
            }
            if (po != null)
               collection.add(po);

            continue;
         }

         backup(mark);
         return;
      }
   }

   /**
    * @param isRestrict
    * @return
    */
   protected ICPPASTPointerToMember createPointerToMember(boolean gnu) {
      if (gnu)
         return new GPPASTPointerToMember();
      return new CPPASTPointerToMember();
   }

   /**
    * @param isRestrict
    * @return
    */
   protected IASTPointerOperator createPointer(boolean gnu) {
      if (gnu)
         return new GPPASTPointer();
      return new CPPASTPointer();
   }

   /**
    * @return
    */
   protected ICPPASTReferenceOperator createReferenceOperator() {
      return new CPPASTReferenceOperator();
   }

   /**
    * @param expression
    * @throws BacktrackException
    */
   protected IASTExpression assignmentExpression() throws EndOfFileException,
         BacktrackException {
      if (LT(1) == IToken.t_throw) {
         return throwExpression();
      }

      if (LT(1) == IToken.tLPAREN && LT(2) == IToken.tLBRACE
            && supportStatementsInExpressions) {
         IASTExpression resultExpression = compoundStatementExpression();
         if (resultExpression != null)
            return resultExpression;
      }

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
   protected IASTExpression throwExpression() throws EndOfFileException,
         BacktrackException {
      IToken throwToken = consume(IToken.t_throw);
      IASTExpression throwExpression = null;
      try {
         throwExpression = expression();
      } catch (BacktrackException bte) {
      }
      int o = throwExpression != null ? calculateEndOffset(throwExpression)
            : throwToken.getEndOffset();
      return buildUnaryExpression(ICPPASTUnaryExpression.op_throw,
            throwExpression, throwToken.getOffset(), o - throwToken.getOffset());
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
               if (templateIdScopes.size() > 0
                     && templateIdScopes.peek() == IToken.tLT) {
                  return firstExpression;
               }
            case IToken.tLT:
            case IToken.tLTEQUAL:
            case IToken.tGTEQUAL:
               IToken m = mark();
               int t = consume().getType();

               IASTExpression secondExpression = null;
               try {
                  secondExpression = shiftExpression();
               } catch (BacktrackException bte) {
                  backup(m);
                  return firstExpression;
               }
               int expressionKind = 0;
               switch (t) {
                  case IToken.tGT:
                     expressionKind = IASTBinaryExpression.op_greaterThan;
                     break;
                  case IToken.tLT:
                     expressionKind = IASTBinaryExpression.op_lessThan;
                     break;
                  case IToken.tLTEQUAL:
                     expressionKind = IASTBinaryExpression.op_lessEqual;
                     break;
                  case IToken.tGTEQUAL:
                     expressionKind = IASTBinaryExpression.op_greaterEqual;
                     break;
               }
               firstExpression = buildBinaryExpression(expressionKind,
                     firstExpression, secondExpression,
                     calculateEndOffset(secondExpression));
               break;
            default:
               if (supportMinAndMaxOperators
                     && (LT(1) == IGCCToken.tMIN || LT(1) == IGCCToken.tMAX)) {
                  int new_operator = 0;
                  switch (LT(1)) {
                     case IGCCToken.tMAX:
                        consume();
                        new_operator = IGPPASTBinaryExpression.op_max;
                        break;
                     case IGCCToken.tMIN:
                        consume();
                        new_operator = IGPPASTBinaryExpression.op_min;
                  }

                  secondExpression = shiftExpression();

                  firstExpression = buildBinaryExpression(new_operator,
                        firstExpression, secondExpression,
                        calculateEndOffset(secondExpression));
                  break;
               }
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
      IASTExpression firstExpression = pmExpression();
      for (;;) {
         switch (LT(1)) {
            case IToken.tSTAR:
            case IToken.tDIV:
            case IToken.tMOD:
               IToken t = consume();
               IASTExpression secondExpression = pmExpression();
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
    * @param expression
    * @throws BacktrackException
    */
   protected IASTExpression pmExpression() throws EndOfFileException,
         BacktrackException {

      IASTExpression firstExpression = castExpression();
      for (;;) {
         switch (LT(1)) {
            case IToken.tDOTSTAR:
            case IToken.tARROWSTAR:
               IToken t = consume();
               IASTExpression secondExpression = castExpression();
               int operator = 0;
               switch (t.getType()) {
                  case IToken.tDOTSTAR:
                     operator = ICPPASTBinaryExpression.op_pmdot;
                     break;
                  case IToken.tARROWSTAR:
                     operator = ICPPASTBinaryExpression.op_pmarrow;
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
         IToken la = LA(1);
         int startingOffset = la.getOffset();
         IToken mark = mark();
         consume();
         if (templateIdScopes.size() > 0) {
            templateIdScopes.push(IToken.tLPAREN);
         }
         boolean popped = false;
         IASTTypeId typeId = null;
         // If this isn't a type name, then we shouldn't be here
         try {
            try {
               typeId = typeId(false);
               consume(IToken.tRPAREN);
            } catch (BacktrackException bte) {
               backup(mark);
               throwBacktrack(bte);
            }

            if (templateIdScopes.size() > 0) {
               templateIdScopes.pop();
               popped = true;
            }
            IASTExpression castExpression = castExpression();
            mark = null; // clean up mark so that we can garbage collect
            return buildTypeIdUnaryExpression(IASTCastExpression.op_cast,
                  typeId, castExpression, startingOffset,
                  calculateEndOffset(castExpression));
         } catch (BacktrackException b) {
            if (templateIdScopes.size() > 0 && !popped) {
               templateIdScopes.pop();
            }
         }
      }
      return unaryExpression();

   }

   /**
    * @throws BacktrackException
    */
   protected IASTTypeId typeId(boolean skipArrayModifiers)
         throws EndOfFileException, BacktrackException {
      IToken mark = mark();
      int startingOffset = mark.getOffset();
      IASTDeclSpecifier declSpecifier = null;
      IASTDeclarator declarator = null;

      try {
         declSpecifier = declSpecifierSeq(false, false);
         declarator = declarator(SimpleDeclarationStrategy.TRY_CONSTRUCTOR,
               true);
      } catch (BacktrackException bt) {
         backup(mark);
         throwBacktrack(startingOffset, figureEndOffset(declSpecifier,
               declarator)
               - startingOffset);
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
            declSpecifier, declarator));

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
      return new CPPASTTypeId();
   }

   /**
    * @param expression
    * @throws BacktrackException
    */
   protected IASTExpression deleteExpression() throws EndOfFileException,
         BacktrackException {
      int startingOffset = LA(1).getOffset();
      boolean global = false;
      if (LT(1) == IToken.tCOLONCOLON) {
         // global scope
         consume(IToken.tCOLONCOLON);
         global = true;
      }

      consume(IToken.t_delete);

      boolean vectored = false;
      if (LT(1) == IToken.tLBRACKET) {
         // array delete
         consume();
         consume(IToken.tRBRACKET);
         vectored = true;
      }
      IASTExpression castExpression = castExpression();
      ICPPASTDeleteExpression deleteExpression = createDeleteExpression();
      ((ASTNode) deleteExpression).setOffsetAndLength(startingOffset,
            calculateEndOffset(castExpression) - startingOffset);
      deleteExpression.setIsGlobal(global);
      deleteExpression.setIsVectored(vectored);
      deleteExpression.setOperand(castExpression);
      castExpression.setParent(deleteExpression);
      castExpression.setPropertyInParent(ICPPASTDeleteExpression.OPERAND);
      return deleteExpression;
   }

   /**
    * @return
    */
   protected ICPPASTDeleteExpression createDeleteExpression() {
      return new CPPASTDeleteExpression();
   }

   /**
    * Pazse a new-expression.
    * 
    * @param expression
    * 
    * @throws BacktrackException
    * 
    * 
    * newexpression: ::? new newplacement? newtypeid newinitializer? ::? new
    * newplacement? ( typeid ) newinitializer? newplacement: ( expressionlist )
    * newtypeid: typespecifierseq newdeclarator? newdeclarator: ptroperator
    * newdeclarator? | directnewdeclarator directnewdeclarator: [ expression ]
    * directnewdeclarator [ constantexpression ] newinitializer: (
    * expressionlist? )
    */
   protected IASTExpression newExpression() throws BacktrackException,
         EndOfFileException {
      IToken la = LA(1);
      int startingOffset = la.getOffset();
      int lastOffset = 0;

      boolean isGlobal = false;
      if (LT(1) == IToken.tCOLONCOLON) {
         lastOffset = consume(IToken.tCOLONCOLON).getEndOffset();
         isGlobal = true;
      }
      lastOffset = consume(IToken.t_new).getEndOffset();
      boolean typeIdInParen = false;
      boolean placementParseFailure = true;
      IToken beforeSecondParen = null;
      IToken backtrackMarker = null;
      IASTTypeId typeId = null;
      IASTExpression newPlacementExpressions = null;
      IASTExpression newInitializerExpressions = null;
      boolean isNewTypeId = false;

      if (LT(1) == IToken.tLPAREN) {
         lastOffset = consume(IToken.tLPAREN).getEndOffset();
         if (templateIdScopes.size() > 0) {
            templateIdScopes.push(IToken.tLPAREN);
         }
         try {
            // Try to consume placement list
            // Note: since expressionList and expression are the same...
            backtrackMarker = mark();
            newPlacementExpressions = expression();
            lastOffset = consume(IToken.tRPAREN).getEndOffset();
            if (LT(1) == IToken.tLBRACKET) {
               backup(backtrackMarker);
               if (templateIdScopes.size() > 0) {
                  templateIdScopes.pop();
               } //pop 1st Parent
               placementParseFailure = true;
               throwBacktrack(backtrackMarker.getOffset(), backtrackMarker
                     .getLength());
            } else
               placementParseFailure = false;
            if (LT(1) == IToken.tLPAREN) {
               beforeSecondParen = mark();
               lastOffset = consume(IToken.tLPAREN).getEndOffset();
               if (templateIdScopes.size() > 0) {
                  templateIdScopes.push(IToken.tLPAREN);
               } //push 2nd Paren
               typeIdInParen = true;
            }
         } catch (BacktrackException e) {
            backup(backtrackMarker);
         }
         if (placementParseFailure) {
            // CASE: new (typeid-not-looking-as-placement) ...
            // the first expression in () is not a placement
            // - then it has to be typeId
            typeId = typeId(true);
            lastOffset = consume(IToken.tRPAREN).getEndOffset();
            if (templateIdScopes.size() > 0) {
               templateIdScopes.pop();
            } //pop 1st Paren
         } else {
            if (!typeIdInParen) {
               if (LT(1) == IToken.tLBRACKET) {
                  // CASE: new (typeid-looking-as-placement) [expr]...
                  // the first expression in () has been parsed as a
                  // placement;
                  // however, we assume that it was in fact typeId, and
                  // this
                  // new statement creates an array.
                  // Do nothing, fallback to array/initializer processing
               } else {
                  // CASE: new (placement) typeid ...
                  // the first expression in () is parsed as a placement,
                  // and the next expression doesn't start with '(' or '['
                  // - then it has to be typeId
                  try {
                     backtrackMarker = mark();
                     typeId = typeId(true);
                     lastOffset = calculateEndOffset(typeId);
                  } catch (BacktrackException e) {
                     // Hmmm, so it wasn't typeId after all... Then it is
                     // CASE: new (typeid-looking-as-placement)
                     backup(backtrackMarker);
                     // TODO fix this
                     return null;
                  }
               }
            } else {
               // Tricky cases: first expression in () is parsed as a
               // placement,
               // and the next expression starts with '('.
               // The problem is, the first expression might as well be a
               // typeid
               try {
                  typeId = typeId(true);
                  lastOffset = consume(IToken.tRPAREN).getEndOffset();
                  if (templateIdScopes.size() > 0) {
                     templateIdScopes.pop();
                  } //popping the 2nd Paren

                  if (LT(1) == IToken.tLPAREN || LT(1) == IToken.tLBRACKET) {
                     // CASE: new (placement)(typeid)(initializer)
                     // CASE: new (placement)(typeid)[] ...
                     // Great, so far all our assumptions have been
                     // correct
                     // Do nothing, fallback to array/initializer
                     // processing
                  } else {
                     // CASE: new (placement)(typeid)
                     // CASE: new
                     // (typeid-looking-as-placement)(initializer-looking-as-typeid)
                     // Worst-case scenario - this cannot be resolved w/o
                     // more semantic information.
                     // Luckily, we don't need to know what was that - we
                     // only know that
                     // new-expression ends here.
                     ICPPASTNewExpression result = createNewExpression();
                     ((ASTNode) result).setOffsetAndLength(startingOffset,
                           lastOffset - startingOffset);
                     result.setIsGlobal(isGlobal);
                     result.setIsNewTypeId(isNewTypeId);
                     result.setTypeId(typeId);
                     typeId.setParent(result);
                     typeId.setPropertyInParent(ICPPASTNewExpression.TYPE_ID);
                     if (newPlacementExpressions != null) {
                        result.setNewPlacement(newPlacementExpressions);
                        newPlacementExpressions.setParent(result);
                        newPlacementExpressions
                              .setPropertyInParent(ICPPASTNewExpression.NEW_PLACEMENT);
                     }
                     return result;
                  }
               } catch (BacktrackException e) {
                  // CASE: new
                  // (typeid-looking-as-placement)(initializer-not-looking-as-typeid)
                  // Fallback to initializer processing
                  backup(beforeSecondParen);
                  if (templateIdScopes.size() > 0) {
                     templateIdScopes.pop();
                  }//pop that 2nd paren
               }
            }
         }
      } else {
         // CASE: new typeid ...
         // new parameters do not start with '('
         // i.e it has to be a plain typeId
         typeId = typeId(true);
         isNewTypeId = true;
      }
      ICPPASTNewExpression result = createNewExpression();
      ((ASTNode) result).setOffset(startingOffset);
      result.setIsGlobal(isGlobal);
      result.setIsNewTypeId(isNewTypeId);
      result.setTypeId(typeId);
      typeId.setParent(result);
      typeId.setPropertyInParent(ICPPASTNewExpression.TYPE_ID);
      if (newPlacementExpressions != null) {
         result.setNewPlacement(newPlacementExpressions);
         newPlacementExpressions.setParent(result);
         newPlacementExpressions
               .setPropertyInParent(ICPPASTNewExpression.NEW_PLACEMENT);
      }

      while (LT(1) == IToken.tLBRACKET) {
         // array new
         consume();

         if (templateIdScopes.size() > 0) {
            templateIdScopes.push(IToken.tLBRACKET);
         }

         IASTExpression a = assignmentExpression();
         consume(IToken.tRBRACKET);
         result.addNewTypeIdArrayExpression(a);
         a.setParent(result);
         a
               .setPropertyInParent(ICPPASTNewExpression.NEW_TYPEID_ARRAY_EXPRESSION);

         if (templateIdScopes.size() > 0) {
            templateIdScopes.pop();
         }
      }
      // newinitializer
      if (LT(1) == IToken.tLPAREN) {
         consume(IToken.tLPAREN);
         if (templateIdScopes.size() > 0) {
            templateIdScopes.push(IToken.tLPAREN);
         }

         //we want to know the difference between no newInitializer and an
         // empty new Initializer
         //if the next token is the RPAREN, then we have an Empty expression
         // in our list.
         newInitializerExpressions = expression();

         int lo = consume(IToken.tRPAREN).getEndOffset();
         if (templateIdScopes.size() > 0) {
            templateIdScopes.pop();
         }
         result.setNewInitializer(newInitializerExpressions);
         newInitializerExpressions.setParent(result);
         newInitializerExpressions
               .setPropertyInParent(ICPPASTNewExpression.NEW_INITIALIZER);
         ((CPPASTNode) result).setLength(lo - startingOffset);
      }

      return result;
   }

   /**
    * @return
    */
   protected ICPPASTNewExpression createNewExpression() {
      return new CPPASTNewExpression();
   }

   /**
    * @param expression
    * @throws BacktrackException
    */
   protected IASTExpression unaryExpression() throws EndOfFileException,
         BacktrackException {
      IToken la = LA(1);
      int startingOffset = la.getOffset();
      switch (LT(1)) {
         case IToken.tSTAR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_star);//IASTExpression.Kind.UNARY_STAR_CASTEXPRESSION);
         case IToken.tAMPER:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_amper);//IASTExpression.Kind.UNARY_AMPSND_CASTEXPRESSION);
         case IToken.tPLUS:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_plus);//IASTExpression.Kind.UNARY_PLUS_CASTEXPRESSION);
         case IToken.tMINUS:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_minus);//IASTExpression.Kind.UNARY_MINUS_CASTEXPRESSION);
         case IToken.tNOT:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_not);//IASTExpression.Kind.UNARY_NOT_CASTEXPRESSION);
         case IToken.tCOMPL:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_tilde);//IASTExpression.Kind.UNARY_TILDE_CASTEXPRESSION);
         case IToken.tINCR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_prefixIncr);//IASTExpression.Kind.UNARY_INCREMENT);
         case IToken.tDECR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_prefixDecr);//IASTExpression.Kind.UNARY_DECREMENT);
         case IToken.t_sizeof:
            consume(IToken.t_sizeof);
            IToken mark = LA(1);
            IASTTypeId typeId = null;
            int lastOffset = 0;
            IASTExpression unaryExpression = null;
            if (LT(1) == IToken.tLPAREN) {
               try {
                  consume(IToken.tLPAREN);
                  typeId = typeId(false);
                  lastOffset = consume(IToken.tRPAREN).getEndOffset();
               } catch (BacktrackException bt) {
                  backup(mark);
                  unaryExpression = unaryExpression();
                  lastOffset = calculateEndOffset(unaryExpression);
               }
            } else {
               unaryExpression = unaryExpression();
               lastOffset = calculateEndOffset(unaryExpression);
            }

            if (typeId == null && unaryExpression != null)
               return buildUnaryExpression(IASTUnaryExpression.op_sizeof,
                     unaryExpression, startingOffset, lastOffset);
            return buildTypeIdExpression(IASTTypeIdExpression.op_sizeof,
                  typeId, startingOffset, lastOffset);
         case IToken.t_new:
            return newExpression();
         case IToken.t_delete:
            return deleteExpression();
         case IToken.tCOLONCOLON:
            switch (LT(2)) {
               case IToken.t_new:
                  return newExpression();
               case IToken.t_delete:
                  return deleteExpression();
               default:
                  return postfixExpression();
            }
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
    * @param expression
    * @throws BacktrackException
    */
   protected IASTExpression postfixExpression() throws EndOfFileException,
         BacktrackException {
      IASTExpression firstExpression = null;
      boolean isTemplate = false;

      switch (LT(1)) {
         case IToken.t_typename:
            int o = consume(IToken.t_typename).getOffset();

            boolean templateTokenConsumed = false;
            if (LT(1) == IToken.t_template) {
               consume(IToken.t_template);
               templateTokenConsumed = true;
            }
            ITokenDuple nestedName = name();
            IASTName name = createName(nestedName);

            consume(IToken.tLPAREN);
            if (templateIdScopes.size() > 0) {
               templateIdScopes.push(IToken.tLPAREN);
            }
            IASTExpression expressionList = expression();
            int lastOffset = consume(IToken.tRPAREN).getEndOffset();
            if (templateIdScopes.size() > 0) {
               templateIdScopes.pop();
            }

            ICPPASTTypenameExpression result = createTypenameExpression();
            ((ASTNode) result).setOffsetAndLength(o, lastOffset - o);
            result.setIsTemplate(templateTokenConsumed);
            result.setName(name);
            name.setParent(result);
            name.setPropertyInParent(ICPPASTTypenameExpression.TYPENAME);
            result.setInitialValue(expressionList);
            expressionList.setParent(result);
            expressionList
                  .setPropertyInParent(ICPPASTTypenameExpression.INITIAL_VALUE);
            firstExpression = result;
            break;
         // simple-type-specifier ( assignment-expression , .. )
         case IToken.t_char:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_char);
            break;
         case IToken.t_wchar_t:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_wchar_t);
            break;
         case IToken.t_bool:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_bool);
            break;
         case IToken.t_short:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_short);
            break;
         case IToken.t_int:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_int);
            break;
         case IToken.t_long:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_long);
            break;
         case IToken.t_signed:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_signed);
            break;
         case IToken.t_unsigned:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_unsigned);
            break;
         case IToken.t_float:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_float);
            break;
         case IToken.t_double:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_double);
            break;
         case IToken.t_dynamic_cast:
            firstExpression = specialCastExpression(ICPPASTCastExpression.op_dynamic_cast);
            break;
         case IToken.t_static_cast:
            firstExpression = specialCastExpression(ICPPASTCastExpression.op_static_cast);
            break;
         case IToken.t_reinterpret_cast:
            firstExpression = specialCastExpression(ICPPASTCastExpression.op_reinterpret_cast);
            break;
         case IToken.t_const_cast:
            firstExpression = specialCastExpression(ICPPASTCastExpression.op_const_cast);
            break;
         case IToken.t_typeid:
            int so = consume().getOffset();
            consume(IToken.tLPAREN);
            if (templateIdScopes.size() > 0) {
               templateIdScopes.push(IToken.tLPAREN);
            }
            boolean isTypeId = true;
            IASTExpression lhs = null;
            IASTTypeId typeId = null;

            try {
               typeId = typeId(false);
            } catch (BacktrackException b) {
               isTypeId = false;
               lhs = expression();
            }
            lastOffset = consume(IToken.tRPAREN).getOffset();
            if (templateIdScopes.size() > 0) {
               templateIdScopes.pop();
            }
            if (isTypeId && typeId != null)
               firstExpression = buildTypeIdExpression(
                     ICPPASTTypeIdExpression.op_typeid, typeId, so, lastOffset);
            else
               firstExpression = buildUnaryExpression(
                     ICPPASTUnaryExpression.op_typeid, lhs, so, lastOffset);
            break;
         default:
            firstExpression = primaryExpression();
      }
      IASTExpression secondExpression = null;
      for (;;) {
         switch (LT(1)) {
            case IToken.tLBRACKET:
               // array access
               consume(IToken.tLBRACKET);
               if (templateIdScopes.size() > 0) {
                  templateIdScopes.push(IToken.tLBRACKET);
               }
               secondExpression = expression();
               int lastOffset = consume(IToken.tRBRACKET).getEndOffset();
               if (templateIdScopes.size() > 0) {
                  templateIdScopes.pop();
               }

               IASTArraySubscriptExpression s = createArraySubscriptExpression();
               ((ASTNode) s).setOffsetAndLength(((ASTNode) firstExpression)
                     .getOffset(), lastOffset
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

               if (templateIdScopes.size() > 0) {
                  templateIdScopes.push(IToken.tLPAREN);
               }
               if (LT(1) != IToken.tRPAREN)
                  secondExpression = expression();
               else
                  secondExpression = null;
               lastOffset = consume(IToken.tRPAREN).getEndOffset();

               if (templateIdScopes.size() > 0) {
                  templateIdScopes.pop();
               }

               IASTFunctionCallExpression fce = createFunctionCallExpression();
               ((ASTNode) fce).setOffsetAndLength(((ASTNode) firstExpression)
                     .getOffset(), lastOffset
                     - ((ASTNode) firstExpression).getOffset());
               fce.setFunctionNameExpression(firstExpression);
               firstExpression.setParent(fce);
               firstExpression
                     .setPropertyInParent(IASTFunctionCallExpression.FUNCTION_NAME);
               if (secondExpression != null) {
                  fce.setParameterExpression(secondExpression);
                  secondExpression.setParent(fce);
                  secondExpression
                        .setPropertyInParent(IASTFunctionCallExpression.PARAMETERS);
               }
               firstExpression = fce;
               break;
            case IToken.tINCR:
               int offset = consume(IToken.tINCR).getEndOffset();
               firstExpression = buildUnaryExpression(
                     IASTUnaryExpression.op_postFixIncr, firstExpression,
                     ((ASTNode) firstExpression).getOffset(), offset);
               break;
            case IToken.tDECR:
               offset = consume().getEndOffset();
               firstExpression = buildUnaryExpression(
                     IASTUnaryExpression.op_postFixDecr, firstExpression,
                     ((ASTNode) firstExpression).getOffset(), offset);
               break;
            case IToken.tDOT:
               // member access
               consume(IToken.tDOT);
               if (LT(1) == IToken.t_template) {
                  consume(IToken.t_template);
                  isTemplate = true;
               }

               IASTName name = createName(idExpression());

               ICPPASTFieldReference fieldReference = createFieldReference();
               ((ASTNode) fieldReference).setOffsetAndLength(
                     ((ASTNode) firstExpression).getOffset(),
                     calculateEndOffset(name)
                           - ((ASTNode) firstExpression).getOffset());
               fieldReference.setIsTemplate(isTemplate);
               fieldReference.setIsPointerDereference(false);
               fieldReference.setFieldName(name);
               name.setParent(fieldReference);
               name.setPropertyInParent(IASTFieldReference.FIELD_NAME);

               fieldReference.setFieldOwner(firstExpression);
               firstExpression.setParent(fieldReference);
               firstExpression
                     .setPropertyInParent(IASTFieldReference.FIELD_OWNER);
               firstExpression = fieldReference;
               break;
            case IToken.tARROW:
               // member access
               consume(IToken.tARROW);

               if (LT(1) == IToken.t_template) {
                  consume(IToken.t_template);
                  isTemplate = true;
               }

               name = createName(idExpression());

               fieldReference = createFieldReference();
               ((ASTNode) fieldReference).setOffsetAndLength(
                     ((ASTNode) firstExpression).getOffset(),
                     calculateEndOffset(name)
                           - ((ASTNode) firstExpression).getOffset());
               fieldReference.setIsTemplate(isTemplate);
               fieldReference.setIsPointerDereference(true);
               fieldReference.setFieldName(name);
               name.setParent(fieldReference);
               name.setPropertyInParent(IASTFieldReference.FIELD_NAME);

               fieldReference.setFieldOwner(firstExpression);
               firstExpression.setParent(fieldReference);
               firstExpression
                     .setPropertyInParent(IASTFieldReference.FIELD_OWNER);
               firstExpression = fieldReference;
               break;
            default:
               return firstExpression;
         }
      }
   }

   /**
    * @return
    */
   protected IASTArraySubscriptExpression createArraySubscriptExpression() {
      return new CPPASTArraySubscriptExpression();
   }

   /**
    * @return
    */
   protected ICPPASTTypenameExpression createTypenameExpression() {
      return new CPPASTTypenameExpression();
   }

   /**
    * @return
    */
   protected IASTFunctionCallExpression createFunctionCallExpression() {
      return new CPPASTFunctionCallExpression();
   }

   /**
    * @return
    */
   protected ICPPASTFieldReference createFieldReference() {
      return new CPPASTFieldReference();
   }

   protected IASTExpression simpleTypeConstructorExpression(int operator)
         throws EndOfFileException, BacktrackException {
      int startingOffset = LA(1).getOffset();
      consume();
      consume(IToken.tLPAREN);
      IASTExpression operand = expression();
      int l = consume(IToken.tRPAREN).getEndOffset();
      ICPPASTSimpleTypeConstructorExpression result = createSimpleTypeConstructorExpression();
      ((ASTNode) result).setOffsetAndLength(startingOffset, l - startingOffset);
      result.setSimpleType(operator);
      result.setInitialValue(operand);
      operand.setParent(result);
      operand
            .setPropertyInParent(ICPPASTSimpleTypeConstructorExpression.INITIALIZER_VALUE);
      return result;
   }

   /**
    * @return
    */
   protected ICPPASTSimpleTypeConstructorExpression createSimpleTypeConstructorExpression() {
      return new CPPASTSimpleTypeConstructorExpression();
   }

   /**
    * @param expression
    * @throws BacktrackException
    */
   protected IASTExpression primaryExpression() throws EndOfFileException,
         BacktrackException {
      IToken t = null;
      ICPPASTLiteralExpression literalExpression = null;
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
         case IToken.t_false:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(ICPPASTLiteralExpression.lk_false);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t
                  .getEndOffset()
                  - t.getOffset());
            return literalExpression;
         case IToken.t_true:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(ICPPASTLiteralExpression.lk_true);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t
                  .getEndOffset()
                  - t.getOffset());
            return literalExpression;

         case IToken.t_this:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(ICPPASTLiteralExpression.lk_this);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t
                  .getEndOffset()
                  - t.getOffset());
            return literalExpression;
         case IToken.tLPAREN:
            t = consume();
            if (templateIdScopes.size() > 0) {
               templateIdScopes.push(IToken.tLPAREN);
            }
            //TODO We need to wrap this
            IASTExpression lhs = expression();
            consume(IToken.tRPAREN).getEndOffset();
            if (templateIdScopes.size() > 0) {
               templateIdScopes.pop();
            }
            return lhs;
         case IToken.tIDENTIFIER:
         case IToken.tCOLONCOLON:
         case IToken.t_operator:
         case IToken.tCOMPL:
            ITokenDuple duple = idExpression();
            IASTName name = createName(duple);
            IASTIdExpression idExpression = createIdExpression();
            ((ASTNode) idExpression).setOffsetAndLength(duple.getStartOffset(),
                  duple.getEndOffset() - duple.getStartOffset());
            idExpression.setName(name);
            name.setParent(idExpression);
            name.setPropertyInParent(IASTIdExpression.ID_NAME);
            return idExpression;
         default:
            IToken la = LA(1);
            int startingOffset = la.getOffset();
            throwBacktrack(startingOffset, la.getLength());
            return null;
      }

   }

   /**
    * @return
    */
   protected ICPPASTLiteralExpression createLiteralExpression() {
      return new CPPASTLiteralExpression();
   }

   /**
    * @return
    */
   protected IASTIdExpression createIdExpression() {
      return new CPPASTIdExpression();
   }

   protected ITokenDuple idExpression() throws EndOfFileException,
         BacktrackException {
      ITokenDuple duple = null;
      try {
         duple = name();
      } catch (BacktrackException bt) {
         IToken mark = mark();
         if (LT(1) == IToken.tCOLONCOLON || LT(1) == IToken.tIDENTIFIER) {
            IToken start = consume();
            IToken end = null;
            if (start.getType() == IToken.tIDENTIFIER)
               end = consumeTemplateParameters(null);
            while (LT(1) == IToken.tCOLONCOLON || LT(1) == IToken.tIDENTIFIER) {
               end = consume();
               if (end.getType() == IToken.tIDENTIFIER)
                  end = consumeTemplateParameters(end);
            }

            if (LT(1) == IToken.t_operator)
               duple = operatorId(start, null);
            else {
               backup(mark);
               throwBacktrack(start.getOffset(), end.getEndOffset()
                     - start.getOffset());
            }
         } else if (LT(1) == IToken.t_operator)
            duple = operatorId(null, null);
      }
      return duple;

   }

   protected IASTExpression specialCastExpression(int kind)
         throws EndOfFileException, BacktrackException {
      int startingOffset = LA(1).getOffset();
      consume();
      consume(IToken.tLT);
      IASTTypeId typeID = typeId(false);
      consume(IToken.tGT);
      consume(IToken.tLPAREN);
      IASTExpression lhs = expression();
      int l = consume(IToken.tRPAREN).getEndOffset();
      IASTCastExpression result = createCastExpression();
      ((ASTNode) result).setOffsetAndLength(startingOffset, l - startingOffset);
      result.setTypeId(typeID);
      typeID.setParent(result);
      typeID.setPropertyInParent(IASTCastExpression.TYPE_ID);
      result.setOperand(lhs);
      lhs.setParent(result);
      lhs.setPropertyInParent(IASTCastExpression.OPERAND);
      return result;
   }

   private final boolean    allowCPPRestrict;

   private final boolean    supportExtendedTemplateSyntax;
   private final boolean    supportMinAndMaxOperators;
   private final boolean    supportComplex;
   private final boolean    supportRestrict;
   private final boolean    supportLongLong;

   private static final int DEFAULT_PARM_LIST_SIZE              = 4;
   private static final int DEFAULT_DECLARATOR_LIST_SIZE        = 4;
   private static final int DEFAULT_POINTEROPS_LIST_SIZE        = 4;
   private static final int DEFAULT_SIZE_EXCEPTIONS_LIST        = 2;
   private static final int DEFAULT_CONSTRUCTOR_CHAIN_LIST_SIZE = 4;

   /**
    * This is the standard cosntructor that we expect the Parser to be
    * instantiated with.
    * 
    * @param mode
    *           TODO
    *  
    */
   public GNUCPPSourceParser(IScanner scanner, ParserMode mode,
         IParserLogService log, ICPPParserExtensionConfiguration config) {
      super(scanner, log, mode, config.supportStatementsInExpressions(), config
            .supportTypeofUnaryExpressions(), config
            .supportAlignOfUnaryExpression());
      allowCPPRestrict = config.allowRestrictPointerOperators();
      supportExtendedTemplateSyntax = config.supportExtendedTemplateSyntax();
      supportMinAndMaxOperators = config.supportMinAndMaxOperators();
      supportRestrict = config.supportRestrictKeyword();
      supportComplex = config.supportComplexNumbers();
      supportLongLong = config.supportLongLongs();
   }

   /**
    * The merger of using-declaration and using-directive in ANSI C++ grammar.
    * 
    * using-declaration: using typename? ::? nested-name-specifier
    * unqualified-id ; using :: unqualified-id ; using-directive: using
    * namespace ::? nested-name-specifier? namespace-name ;
    * 
    * @param container
    *           Callback object representing the scope these definitions fall
    *           into.
    * @return TODO
    * @throws BacktrackException
    *            request for a backtrack
    */
   protected IASTDeclaration usingClause() throws EndOfFileException,
         BacktrackException {
      IToken firstToken = consume(IToken.t_using);

      if (LT(1) == IToken.t_namespace) {
         // using-directive
         int endOffset = consume(IToken.t_namespace).getEndOffset();
         IASTName name = null;
         if (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tCOLONCOLON)
            name = createName(name());
         else
            throwBacktrack(firstToken.getOffset(), endOffset
                  - firstToken.getOffset());

         endOffset = consume(IToken.tSEMI).getEndOffset();
         ICPPASTUsingDirective astUD = createUsingDirective();
         ((ASTNode) astUD).setOffsetAndLength(firstToken.getOffset(), endOffset
               - firstToken.getOffset());
         astUD.setQualifiedName(name);
         name.setParent(astUD);
         name.setPropertyInParent(ICPPASTUsingDirective.QUALIFIED_NAME);
         return astUD;
      }

      boolean typeName = false;

      if (LT(1) == IToken.t_typename) {
         typeName = true;
         consume(IToken.t_typename);
      }

      IASTName name = createName(name());
      int end = consume(IToken.tSEMI).getEndOffset();
      ICPPASTUsingDeclaration result = createUsingDeclaration();
      ((ASTNode) result).setOffsetAndLength(firstToken.getOffset(), end
            - firstToken.getOffset());
      result.setIsTypename(typeName);
      result.setName(name);
      name.setPropertyInParent(ICPPASTUsingDeclaration.NAME);
      name.setParent(result);
      return result;
   }

   /**
    * @return
    */
   protected ICPPASTUsingDeclaration createUsingDeclaration() {
      return new CPPASTUsingDeclaration();
   }

   /**
    * @return
    */
   protected ICPPASTUsingDirective createUsingDirective() {
      return new CPPASTUsingDirective();
   }

   /**
    * Implements Linkage specification in the ANSI C++ grammar.
    * 
    * linkageSpecification : extern "string literal" declaration | extern
    * "string literal" { declaration-seq }
    * 
    * @param container
    *           Callback object representing the scope these definitions fall
    *           into.
    * @return TODO
    * @throws BacktrackException
    *            request for a backtrack
    */
   protected ICPPASTLinkageSpecification linkageSpecification()
         throws EndOfFileException, BacktrackException {
      IToken firstToken = consume(IToken.t_extern);
      IToken spec = consume(IToken.tSTRING);
      ICPPASTLinkageSpecification linkage = createLinkageSpecification();
      ((ASTNode) linkage).setOffset(firstToken.getOffset());
      linkage.setLiteral(spec.getImage());

      if (LT(1) == IToken.tLBRACE) {
         consume(IToken.tLBRACE);

         linkageDeclarationLoop: while (LT(1) != IToken.tRBRACE) {
            int checkToken = LA(1).hashCode();
            switch (LT(1)) {
               case IToken.tRBRACE:
                  break linkageDeclarationLoop;
               default:
                  try {
                     IASTDeclaration d = declaration();
                     linkage.addDeclaration(d);
                     d.setParent(linkage);
                     d
                           .setPropertyInParent(ICPPASTLinkageSpecification.OWNED_DECLARATION);
                  } catch (BacktrackException bt) {
                     IASTProblem p = failParse(bt);
                     IASTProblemDeclaration pd = createProblemDeclaration();
                     p.setParent(pd);
                     pd.setProblem(p);
                     ((CPPASTNode) pd).setOffsetAndLength(((CPPASTNode) p));
                     p.setPropertyInParent(IASTProblemHolder.PROBLEM);
                     linkage.addDeclaration(pd);
                     pd.setParent(linkage);
                     pd
                           .setPropertyInParent(ICPPASTLinkageSpecification.OWNED_DECLARATION);
                     errorHandling();
                     if (checkToken == LA(1).hashCode())
                        errorHandling();
                  }
            }
            if (checkToken == LA(1).hashCode())
               failParseWithErrorHandling();
         }
         // consume the }
         int endOffset = consume(IToken.tRBRACE).getEndOffset();
         ((CPPASTNode) linkage).setOffset(endOffset - firstToken.getOffset());
         return linkage;
      }
      // single declaration

      IASTDeclaration d = declaration();
      linkage.addDeclaration(d);
      d.setParent(linkage);
      d.setPropertyInParent(ICPPASTLinkageSpecification.OWNED_DECLARATION);
      return linkage;
   }

   /**
    * @return
    */
   protected ICPPASTLinkageSpecification createLinkageSpecification() {
      return new CPPASTLinkageSpecification();
   }

   /**
    * 
    * Represents the emalgamation of template declarations, template
    * instantiations and specializations in the ANSI C++ grammar.
    * 
    * template-declaration: export? template < template-parameter-list >
    * declaration explicit-instantiation: template declaration
    * explicit-specialization: template <>declaration
    * 
    * @param container
    *           Callback object representing the scope these definitions fall
    *           into.
    * @return TODO
    * @throws BacktrackException
    *            request for a backtrack
    */
   protected IASTDeclaration templateDeclaration() throws EndOfFileException,
         BacktrackException {
      IToken mark = mark();
      IToken firstToken = null;
      boolean exported = false;
      boolean encounteredExtraMod = false;
      if (LT(1) == IToken.t_export) {
         exported = true;
         firstToken = consume(IToken.t_export);
         consume(IToken.t_template);
      } else {
         if (supportExtendedTemplateSyntax) {
            switch (LT(1)) {
               case IToken.t_static:
               case IToken.t_extern:
               case IToken.t_inline:
                  firstToken = consume();
                  consume(IToken.t_template);
                  encounteredExtraMod = true;
                  break;
               default:
                  firstToken = consume(IToken.t_template);
                  break;
            }
         } else
            firstToken = consume(IToken.t_template);
      }
      if (LT(1) != IToken.tLT) {
         // explicit-instantiation
         ICPPASTExplicitTemplateInstantiation templateInstantiation = null;
         if (encounteredExtraMod && supportExtendedTemplateSyntax) {
            IGPPASTExplicitTemplateInstantiation temp = createGnuTemplateInstantiation();
            switch (firstToken.getType()) {
               case IToken.t_static:
                  temp
                        .setModifier(IGPPASTExplicitTemplateInstantiation.ti_static);
                  break;
               case IToken.t_extern:
                  temp
                        .setModifier(IGPPASTExplicitTemplateInstantiation.ti_extern);
                  break;
               case IToken.t_inline:
                  temp
                        .setModifier(IGPPASTExplicitTemplateInstantiation.ti_inline);
                  break;
            }
            templateInstantiation = temp;
         } else
            templateInstantiation = createTemplateInstantiation();
         IASTDeclaration d = declaration();
         ((ASTNode) templateInstantiation).setOffsetAndLength(firstToken
               .getOffset(), calculateEndOffset(d) - firstToken.getOffset());
         templateInstantiation.setDeclaration(d);
         d.setParent(templateInstantiation);
         d
               .setPropertyInParent(ICPPASTExplicitTemplateInstantiation.OWNED_DECLARATION);
         return templateInstantiation;
      }
      consume(IToken.tLT);
      if (LT(1) == IToken.tGT) {
         // explicit-specialization
         consume(IToken.tGT);

         ICPPASTTemplateSpecialization templateSpecialization = createTemplateSpecialization();
         IASTDeclaration d = declaration();
         ((ASTNode) templateSpecialization).setOffsetAndLength(firstToken
               .getOffset(), calculateEndOffset(d) - firstToken.getOffset());
         templateSpecialization.setDeclaration(d);
         d.setParent(templateSpecialization);
         d.setPropertyInParent(ICPPASTTemplateSpecialization.OWNED_DECLARATION);
         return templateSpecialization;
      }

      try {
         List parms = templateParameterList();
         consume(IToken.tGT);
         IASTDeclaration d = declaration();
         ICPPASTTemplateDeclaration templateDecl = createTemplateDeclaration();
         ((ASTNode) templateDecl).setOffsetAndLength(firstToken.getOffset(),
               calculateEndOffset(d) - firstToken.getOffset());
         templateDecl.setExported(exported);
         templateDecl.setDeclaration(d);
         d.setParent(templateDecl);
         d.setPropertyInParent(ICPPASTTemplateDeclaration.OWNED_DECLARATION);
         for (int i = 0; i < parms.size(); ++i) {
            ICPPASTTemplateParameter parm = (ICPPASTTemplateParameter) parms
                  .get(i);
            templateDecl.addTemplateParamter(parm);
            parm.setParent(templateDecl);
            parm.setPropertyInParent(ICPPASTTemplateDeclaration.PARAMETER);
         }

         return templateDecl;
      } catch (BacktrackException bt) {
         backup(mark);
         throw bt;
      }
   }

   /**
    * @return
    */
   protected ICPPASTTemplateDeclaration createTemplateDeclaration() {
      return new CPPASTTemplateDeclaration();
   }

   /**
    * @return
    */
   protected ICPPASTTemplateSpecialization createTemplateSpecialization() {
      return new CPPASTTemplateSpecialization();
   }

   /**
    * @return
    */
   protected IGPPASTExplicitTemplateInstantiation createGnuTemplateInstantiation() {
      return new GPPASTExplicitTemplateInstantiation();
   }

   /**
    * @return
    */
   protected ICPPASTExplicitTemplateInstantiation createTemplateInstantiation() {
      return new CPPASTExplicitTemplateInstantiation();
   }

   /**
    * 
    * 
    * 
    * template-parameter-list: template-parameter template-parameter-list ,
    * template-parameter template-parameter: type-parameter
    * parameter-declaration type-parameter: class identifier? class identifier? =
    * type-id typename identifier? typename identifier? = type-id template <
    * template-parameter-list > class identifier? template <
    * template-parameter-list > class identifier? = id-expression template-id:
    * template-name < template-argument-list?> template-name: identifier
    * template-argument-list: template-argument template-argument-list ,
    * template-argument template-argument: assignment-expression type-id
    * id-expression
    * 
    * @param templateDeclaration
    *           Callback's templateDeclaration which serves as a scope to this
    *           list.
    * @throws BacktrackException
    *            request for a backtrack
    */
   protected List templateParameterList() throws BacktrackException,
         EndOfFileException {
      // if we have gotten this far then we have a true template-declaration
      // iterate through the template parameter list
      List returnValue = new ArrayList(DEFAULT_PARM_LIST_SIZE);

      for (;;) {
         if (LT(1) == IToken.tGT)
            return returnValue;
         if (LT(1) == IToken.t_class || LT(1) == IToken.t_typename) {
            IToken startingToken = LA(1);
            int lastOffset = 0;
            int type = (LT(1) == IToken.t_class ? ICPPASTSimpleTypeTemplateParameter.st_class
                  : ICPPASTSimpleTypeTemplateParameter.st_typename);
            lastOffset = consume().getEndOffset();
            IASTName identifierName = null;
            IASTTypeId typeId = null;

            if (LT(1) == IToken.tIDENTIFIER) // optional identifier
            {
               identifierName = createName(identifier());
               lastOffset = calculateEndOffset(identifierName);
               if (LT(1) == IToken.tASSIGN) // optional = type-id
               {
                  consume(IToken.tASSIGN);
                  typeId = typeId(false); // type-id
                  lastOffset = calculateEndOffset(typeId);
               }
            } else {
               identifierName = createName();
            }

            ICPPASTSimpleTypeTemplateParameter parm = createSimpleTemplateParameter();
            ((ASTNode) parm).setOffsetAndLength(startingToken.getOffset(),
                  lastOffset - startingToken.getOffset());
            parm.setParameterType(type);
            parm.setName(identifierName);
            identifierName.setParent(parm);
            identifierName
                  .setPropertyInParent(ICPPASTSimpleTypeTemplateParameter.PARAMETER_NAME);
            if (typeId != null) {
               parm.setDefaultType(typeId);
               typeId.setParent(parm);
               typeId
                     .setPropertyInParent(ICPPASTSimpleTypeTemplateParameter.DEFAULT_TYPE);
            }
            returnValue.add(parm);

         } else if (LT(1) == IToken.t_template) {
            IToken firstToken = consume(IToken.t_template);
            consume(IToken.tLT);

            List subResult = templateParameterList();
            consume(IToken.tGT);
            int last = consume(IToken.t_class).getEndOffset();
            IASTName identifierName = null;
            IASTExpression optionalExpression = null;

            if (LT(1) == IToken.tIDENTIFIER) // optional identifier
            {
               identifierName = createName(identifier());
               last = calculateEndOffset(identifierName);
               if (LT(1) == IToken.tASSIGN) // optional = type-id
               {
                  consume(IToken.tASSIGN);
                  optionalExpression = primaryExpression();
                  last = calculateEndOffset(optionalExpression);
               }
            } else
               identifierName = createName();

            ICPPASTTemplatedTypeTemplateParameter parm = createTemplatedTemplateParameter();
            ((ASTNode) parm).setOffsetAndLength(firstToken.getOffset(), last
                  - firstToken.getOffset());
            parm.setName(identifierName);
            identifierName.setParent(parm);
            identifierName
                  .setPropertyInParent(ICPPASTTemplatedTypeTemplateParameter.PARAMETER_NAME);
            if (optionalExpression != null) {
               parm.setDefaultValue(optionalExpression);
               optionalExpression.setParent(parm);
               optionalExpression
                     .setPropertyInParent(ICPPASTTemplatedTypeTemplateParameter.DEFAULT_VALUE);
            }

            for (int i = 0; i < subResult.size(); ++i) {
               ICPPASTTemplateParameter p = (ICPPASTTemplateParameter) subResult
                     .get(i);
               parm.addTemplateParamter(p);
               p.setParent(parm);
               p
                     .setPropertyInParent(ICPPASTTemplatedTypeTemplateParameter.PARAMETER);
            }
            returnValue.add(parm);

         } else if (LT(1) == IToken.tCOMMA) {
            consume(IToken.tCOMMA);
            continue;
         } else {
            ICPPASTParameterDeclaration parm = parameterDeclaration();
            returnValue.add(parm);
         }
      }
   }

   /**
    * @return
    */
   protected ICPPASTTemplatedTypeTemplateParameter createTemplatedTemplateParameter() {
      return new CPPASTTemplatedTypeTemplateParameter();
   }

   /**
    * @return
    */
   protected ICPPASTSimpleTypeTemplateParameter createSimpleTemplateParameter() {
      return new CPPASTSimpleTypeTemplateParameter();
   }

   /**
    * The most abstract construct within a translationUnit : a declaration.
    * 
    * declaration : {"asm"} asmDefinition | {"namespace"} namespaceDefinition |
    * {"using"} usingDeclaration | {"export"|"template"} templateDeclaration |
    * {"extern"} linkageSpecification | simpleDeclaration
    * 
    * Notes: - folded in blockDeclaration - merged alternatives that required
    * same LA - functionDefinition into simpleDeclaration -
    * namespaceAliasDefinition into namespaceDefinition - usingDirective into
    * usingDeclaration - explicitInstantiation and explicitSpecialization into
    * templateDeclaration
    * 
    * @param container
    *           IParserCallback object which serves as the owner scope for this
    *           declaration.
    * 
    * @throws BacktrackException
    *            request a backtrack
    */
   protected IASTDeclaration declaration() throws EndOfFileException,
         BacktrackException {
      switch (LT(1)) {
         case IToken.t_asm:
            return asmDeclaration();
         case IToken.t_namespace:
            return namespaceDefinitionOrAlias();
         case IToken.t_using:
            return usingClause();
         case IToken.t_export:
         case IToken.t_template:
            return templateDeclaration();
         case IToken.t_extern:
            if (LT(2) == IToken.tSTRING)
               return linkageSpecification();
         default:
            if (supportExtendedTemplateSyntax
                  && (LT(1) == IToken.t_static || LT(1) == IToken.t_inline || LT(1) == IToken.t_extern)
                  && LT(2) == IToken.t_template)
               return templateDeclaration();
            return simpleDeclarationStrategyUnion();
      }
   }

   protected IASTDeclaration simpleDeclarationStrategyUnion()
         throws EndOfFileException, BacktrackException {
      simpleDeclarationMark = mark();
      IASTProblem firstFailure = null;
      IASTProblem secondFailure = null;
      try {
         IASTDeclaration d = simpleDeclaration(SimpleDeclarationStrategy.TRY_CONSTRUCTOR,
               false);
         throwAwayMarksForInitializerClause();
         return d;
      } catch (BacktrackException bt) {
         if (simpleDeclarationMark == null)
            throwBacktrack(bt);
         firstFailure = bt.getProblem();
         // did not work
         backup(simpleDeclarationMark);

         try {
            IASTDeclaration d = simpleDeclaration(SimpleDeclarationStrategy.TRY_FUNCTION,
                  false);
            throwAwayMarksForInitializerClause();
            return d;            
         } catch (BacktrackException bt2) {
            if (simpleDeclarationMark == null) {
               if (firstFailure != null && (bt2.getProblem() == null))
                  throwBacktrack(firstFailure);
               else
                  throwBacktrack(bt2);
            }

            secondFailure = bt2.getProblem();
            backup(simpleDeclarationMark);
            throwAwayMarksForInitializerClause();
            try {
               return simpleDeclaration(SimpleDeclarationStrategy.TRY_VARIABLE,
                     false);
            } catch (BacktrackException b3) {
               backup(simpleDeclarationMark); //TODO - necessary?

               if (firstFailure != null)
                  throwBacktrack(firstFailure);
               else if (secondFailure != null)
                  throwBacktrack(secondFailure);
               else
                  throwBacktrack(b3);
               return null;
            }
         }

      }
   }

   /**
    * Serves as the namespace declaration portion of the ANSI C++ grammar.
    * 
    * namespace-definition: namespace identifier { namespace-body } | namespace {
    * namespace-body } namespace-body: declaration-seq?
    * 
    * @param container
    *           IParserCallback object which serves as the owner scope for this
    *           declaration.
    * @return TODO
    * @throws BacktrackException
    *            request a backtrack
    *  
    */
   protected IASTDeclaration namespaceDefinitionOrAlias()
         throws BacktrackException, EndOfFileException {

      IToken first = consume(IToken.t_namespace);
      int last = first.getEndOffset();
      IASTName name = null;
      // optional name
      if (LT(1) == IToken.tIDENTIFIER) {
         name = createName(identifier());
         last = calculateEndOffset(name);
      } else
         name = createName();

      if (LT(1) == IToken.tLBRACE) {
         consume();
         ICPPASTNamespaceDefinition namespaceDefinition = createNamespaceDefinition();
         ((ASTNode) namespaceDefinition).setOffset(first.getOffset());
         namespaceDefinition.setName(name);
         name.setParent(namespaceDefinition);
         name.setPropertyInParent(ICPPASTNamespaceDefinition.NAMESPACE_NAME);
         namespaceDeclarationLoop: while (LT(1) != IToken.tRBRACE) {
            int checkToken = LA(1).hashCode();
            switch (LT(1)) {
               case IToken.tRBRACE:
                  break namespaceDeclarationLoop;
               default:
                  try {
                     IASTDeclaration d = declaration();
                     d.setParent(namespaceDefinition);
                     d
                           .setPropertyInParent(ICPPASTNamespaceDefinition.OWNED_DECLARATION);
                     namespaceDefinition.addDeclaration(d);
                  } catch (BacktrackException bt) {
                     IASTProblem p = failParse(bt);
                     IASTProblemDeclaration pd = createProblemDeclaration();
                     p.setParent(pd);
                     pd.setProblem(p);
                     ((CPPASTNode) pd).setOffsetAndLength((CPPASTNode) p);
                     p.setPropertyInParent(IASTProblemHolder.PROBLEM);
                     namespaceDefinition.addDeclaration(pd);
                     pd.setParent(namespaceDefinition);
                     pd
                           .setPropertyInParent(ICPPASTNamespaceDefinition.OWNED_DECLARATION);
                     errorHandling();
                     if (checkToken == LA(1).hashCode())
                        errorHandling();
                  }
            }
            if (checkToken == LA(1).hashCode())
               failParseWithErrorHandling();
         }
         // consume the }
         int end = consume(IToken.tRBRACE).getEndOffset();
         ((CPPASTNode) namespaceDefinition).setLength(end - first.getOffset());
         return namespaceDefinition;
      } else if (LT(1) == IToken.tASSIGN) {
         IToken assign = consume(IToken.tASSIGN);

         if (name.toString() == null) {
            throwBacktrack(first.getOffset(), assign.getEndOffset()
                  - first.getOffset());
            return null;
         }

         ITokenDuple duple = name();
         IASTName qualifiedName = createName(duple);
         int end = consume(IToken.tSEMI).getEndOffset();

         ICPPASTNamespaceAlias alias = createNamespaceAlias();
         ((ASTNode) alias).setOffsetAndLength(first.getOffset(), end
               - first.getOffset());
         alias.setAlias(name);
         name.setParent(alias);
         name.setPropertyInParent(ICPPASTNamespaceAlias.ALIAS_NAME);
         alias.setQualifiedName(qualifiedName);
         qualifiedName.setParent(alias);
         qualifiedName.setPropertyInParent(ICPPASTNamespaceAlias.MAPPING_NAME);
         return alias;
      } else {
         throwBacktrack(first.getOffset(), last - first.getOffset());
         return null;
      }
   }

   /**
    * @return
    */
   protected ICPPASTNamespaceAlias createNamespaceAlias() {
      return new CPPASTNamespaceAlias();
   }

   /**
    * @param duple
    * @return
    */
   protected ICPPASTQualifiedName createQualifiedName(ITokenDuple duple) {
      CPPASTQualifiedName result = new CPPASTQualifiedName();
      result.setOffsetAndLength(duple.getStartOffset(), duple.getEndOffset()
            - duple.getStartOffset());
      ITokenDuple[] segments = duple.getSegments();
      for (int i = 0; i < segments.length; ++i) {
         IASTName subName = null;
         // take each name and add it to the result
         if (segments[i] instanceof IToken)
            subName = createName((IToken) segments[i]);
         else if (segments[i].getTemplateIdArgLists() == null)
            subName = createName(segments[i]);
         else
            // templateID
            subName = createTemplateID(segments[i]);
         subName.setParent(result);
         subName.setPropertyInParent(ICPPASTQualifiedName.SEGMENT_NAME);
         ((ASTNode) subName).setOffsetAndLength(segments[i].getStartOffset(),
               segments[i].getEndOffset() - segments[i].getStartOffset());
         result.addName(subName);
      }

      return result;
   }

   /**
    * @param duple
    * @return
    */
   protected ICPPASTTemplateId createTemplateID(ITokenDuple duple) {
      ICPPASTTemplateId result = new CPPASTTemplateId();
      ((ASTNode) result).setOffsetAndLength(duple.getStartOffset(), duple
            .getEndOffset()
            - duple.getStartOffset());
      char[] image = duple.extractNameFromTemplateId();
      CPPASTName templateIdName = (CPPASTName) createName();
      templateIdName.setOffsetAndLength(duple.getStartOffset(), image.length);
      templateIdName.setName(image);
      result.setTemplateName(templateIdName);
      templateIdName.setParent(result);
      templateIdName.setPropertyInParent(ICPPASTTemplateId.TEMPLATE_NAME);
      if (duple.getTemplateIdArgLists() != null) {
         List args = duple.getTemplateIdArgLists()[0];
         if (args != null)
            for (int i = 0; i < args.size(); ++i) {
               IASTNode n = (IASTNode) args.get(i);
               if (n instanceof IASTTypeId || n instanceof IASTExpression) {
                  n.setParent(result);
                  n.setPropertyInParent(ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT);
                  if (n instanceof IASTTypeId)
                     result.addTemplateArgument((IASTTypeId) n);
                  else
                     result.addTemplateArgument((IASTExpression) n);
               }
            }
      }
      return result;
   }

   /**
    * @param duple
    * @return
    */
   protected IASTName createName(ITokenDuple duple) {
      if (duple == null)
         return createName();
      if (duple.getSegmentCount() != 1)
         return createQualifiedName(duple);
      CPPASTName name = new CPPASTName(duple.toCharArray());
      name.setOffsetAndLength(duple.getStartOffset(), duple.getEndOffset()
            - duple.getStartOffset());
      return name;
   }

   /**
    * @return
    */
   protected ICPPASTNamespaceDefinition createNamespaceDefinition() {
      return new CPPASTNamespaceDefinition();
   }

   /**
    * Serves as the catch-all for all complicated declarations, including
    * function-definitions.
    * 
    * simpleDeclaration : (declSpecifier)* (initDeclarator (","
    * initDeclarator)*)? (";" | { functionBody }
    * 
    * Notes: - append functionDefinition stuff to end of this rule
    * 
    * To do: - work in functionTryBlock
    * 
    * @param container
    *           IParserCallback object which serves as the owner scope for this
    *           declaration.
    * @param tryConstructor
    *           true == take strategy1 (constructor ) : false == take strategy 2 (
    *           pointer to function)
    * @return TODO
    * @throws BacktrackException
    *            request a backtrack
    */
   protected IASTDeclaration simpleDeclaration(
         SimpleDeclarationStrategy strategy, boolean fromCatchHandler)
         throws BacktrackException, EndOfFileException {
      IToken firstToken = LA(1);
      int firstOffset = firstToken.getOffset();
      if (firstToken.getType() == IToken.tLBRACE)
         throwBacktrack(firstOffset, firstToken.getLength());
      firstToken = null; // necessary for scalability

      ICPPASTDeclSpecifier declSpec = declSpecifierSeq(false,
            strategy == SimpleDeclarationStrategy.TRY_CONSTRUCTOR);
      List declarators = Collections.EMPTY_LIST;
      if (LT(1) != IToken.tSEMI) {
         declarators = new ArrayList(DEFAULT_DECLARATOR_LIST_SIZE);
         declarators.add(initDeclarator(strategy));
         while (LT(1) == IToken.tCOMMA) {
            consume(IToken.tCOMMA);
            declarators.add(initDeclarator(strategy));
         }
      }

      boolean hasFunctionBody = false;
      boolean hasFunctionTryBlock = false;
      boolean consumedSemi = false;
      List constructorChain = Collections.EMPTY_LIST;

      switch (LT(1)) {
         case IToken.tSEMI:
            consume(IToken.tSEMI);
            consumedSemi = true;
            break;
         case IToken.t_try:
            consume(IToken.t_try);
            if (LT(1) == IToken.tCOLON) {
               constructorChain = new ArrayList(
                     DEFAULT_CONSTRUCTOR_CHAIN_LIST_SIZE);
               ctorInitializer(constructorChain);
            }
            hasFunctionTryBlock = true;
            break;
         case IToken.tCOLON:
            constructorChain = new ArrayList(
                  DEFAULT_CONSTRUCTOR_CHAIN_LIST_SIZE);
            ctorInitializer(constructorChain);
            break;
         case IToken.tLBRACE:
            break;
         case IToken.tRPAREN:
            if (!fromCatchHandler)
               throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);
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
            throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);

         IASTDeclarator declarator = (IASTDeclarator) declarators.get(0);
         if (!(declarator instanceof IASTFunctionDeclarator))
            throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);

         if (!constructorChain.isEmpty()
               && declarator instanceof ICPPASTFunctionDeclarator) {
            ICPPASTFunctionDeclarator fd = (ICPPASTFunctionDeclarator) declarator;
            for (int i = 0; i < constructorChain.size(); ++i) {
               ICPPASTConstructorChainInitializer initializer = (ICPPASTConstructorChainInitializer) constructorChain
                     .get(i);
               fd.addConstructorToChain(initializer);
               initializer.setParent(fd);
               initializer
                     .setPropertyInParent(ICPPASTFunctionDeclarator.CONSTRUCTOR_CHAIN_MEMBER);
            }
         }

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
         ((CPPASTNode) funcDefinition).setLength(calculateEndOffset(s)
               - firstOffset);

         if (hasFunctionTryBlock
               && declarator instanceof ICPPASTFunctionTryBlockDeclarator) {
            List handlers = new ArrayList(DEFAULT_CATCH_HANDLER_LIST_SIZE);
            catchHandlerSequence(handlers);
            for (int i = 0; i < handlers.size(); ++i) {
               ICPPASTCatchHandler handler = (ICPPASTCatchHandler) handlers
                     .get(i);
               ((ICPPASTFunctionTryBlockDeclarator) declarator)
                     .addCatchHandler(handler);
               handler.setParent(declarator);
               handler
                     .setPropertyInParent(ICPPASTFunctionTryBlockDeclarator.CATCH_HANDLER);
               ((CPPASTNode) funcDefinition)
                     .setLength(calculateEndOffset(handler) - firstOffset);

            }

         }
         return funcDefinition;
      }

      IASTSimpleDeclaration simpleDeclaration = createSimpleDeclaration();
      ((ASTNode) simpleDeclaration).setOffsetAndLength(firstOffset,
            figureEndOffset(declSpec, declarators) - firstOffset);
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
      return new CPPASTFunctionDefinition();
   }

   /**
    * @return
    */
   protected IASTSimpleDeclaration createSimpleDeclaration() {
      return new CPPASTSimpleDeclaration();
   }

   /**
    * This method parses a constructor chain ctorinitializer: :
    * meminitializerlist meminitializerlist: meminitializer | meminitializer ,
    * meminitializerlist meminitializer: meminitializerid | ( expressionlist? )
    * meminitializerid: ::? nestednamespecifier? classname identifier
    * 
    * @param declarator
    *           IParserCallback object that represents the declarator
    *           (constructor) that owns this initializer
    * 
    * @throws BacktrackException
    *            request a backtrack
    */
   protected void ctorInitializer(List collection) throws EndOfFileException,
         BacktrackException {
      consume(IToken.tCOLON);
      for (;;) {
         if (LT(1) == IToken.tLBRACE)
            break;
         ITokenDuple duple = name();
         IASTName name = createName(duple);

         consume(IToken.tLPAREN);
         IASTExpression expressionList = null;

         if (LT(1) != IToken.tRPAREN)
            expressionList = expression();

         int end = consume(IToken.tRPAREN).getEndOffset();
         ICPPASTConstructorChainInitializer ctorInitializer = createConstructorChainInitializer();
         ((ASTNode) ctorInitializer).setOffsetAndLength(duple.getStartOffset(),
               end - duple.getStartOffset());
         ctorInitializer.setMemberInitializerId(name);
         name.setParent(ctorInitializer);
         name.setPropertyInParent(ICPPASTConstructorChainInitializer.MEMBER_ID);

         if (expressionList != null) {
            ctorInitializer.setInitializerValue(expressionList);
            expressionList.setParent(ctorInitializer);
            expressionList
                  .setPropertyInParent(ICPPASTConstructorChainInitializer.INITIALIZER);
         }
         collection.add(ctorInitializer);
         if (LT(1) == IToken.tLBRACE)
            break;
         consume(IToken.tCOMMA);
      }

   }

   /**
    * @return
    */
   protected ICPPASTConstructorChainInitializer createConstructorChainInitializer() {
      return new CPPASTConstructorChainInitializer();
   }

   /**
    * This routine parses a parameter declaration
    * 
    * @param containerObject
    *           The IParserCallback object representing the
    *           parameterDeclarationClause owning the parm.
    * 
    * @return TODO
    * @throws BacktrackException
    *            request a backtrack
    */
   protected ICPPASTParameterDeclaration parameterDeclaration()
         throws BacktrackException, EndOfFileException {
      IToken current = LA(1);
      IASTDeclSpecifier declSpec = declSpecifierSeq(true, false);
      IASTDeclarator declarator = null;
      if (LT(1) != IToken.tSEMI)
         declarator = initDeclarator(SimpleDeclarationStrategy.TRY_FUNCTION);

      if (current == LA(1)) {
         throwBacktrack(current.getOffset(), figureEndOffset(declSpec,
               declarator)
               - current.getOffset());
      }
      ICPPASTParameterDeclaration parm = createParameterDeclaration();
      ((ASTNode) parm).setOffsetAndLength(current.getOffset(), figureEndOffset(
            declSpec, declarator)
            - current.getOffset());
      parm.setDeclSpecifier(declSpec);
      declSpec.setParent(parm);
      declSpec.setPropertyInParent(IASTParameterDeclaration.DECL_SPECIFIER);
      if (declarator != null) {
         parm.setDeclarator(declarator);
         declarator.setParent(parm);
         declarator.setPropertyInParent(IASTParameterDeclaration.DECLARATOR);
      }
      return parm;
   }

   /**
    * @return
    */
   protected ICPPASTParameterDeclaration createParameterDeclaration() {
      return new CPPASTParameterDeclaration();
   }

   /**
    * @param flags
    *           input flags that are used to make our decision
    * @return whether or not this looks like a constructor (true or false)
    * @throws EndOfFileException
    *            we could encounter EOF while looking ahead
    */
   protected boolean lookAheadForConstructorOrConversion(Flags flags)
         throws EndOfFileException {
      if (flags.isForParameterDeclaration())
         return false;
      if (LT(2) == IToken.tLPAREN && flags.isForConstructor())
         return true;

      IToken mark = mark();
      ITokenDuple duple = null;
      try {
         duple = consumeTemplatedOperatorName();
      } catch (BacktrackException e) {
         backup(mark);
         return false;
      } catch (EndOfFileException eof) {
         backup(mark);
         return false;
      }

      if (duple == null) {
         backup(mark);
         return false;
      }

      ITokenDuple leadingSegments = duple.getLeadingSegments();
      if (leadingSegments == null) {
         backup(mark);
         return false;
      }
      ITokenDuple lastSegment = duple.getLastSegment();
      char[] className = lastSegment.extractNameFromTemplateId();
      if (className == null || CharArrayUtils.equals(className, EMPTY_STRING)) {
         backup(mark);
         return false;
      }

      ITokenDuple secondlastSegment = leadingSegments.getLastSegment();
      if (secondlastSegment == null) {
         backup(mark);
         return false;
      }
      char[] otherName = secondlastSegment.extractNameFromTemplateId();
      if (otherName == null || CharArrayUtils.equals(otherName, EMPTY_STRING)) {
         backup(mark);
         return false;
      }

      if (lastSegment.isConversion()) {
         backup(mark);
         return true;
      }

      if (CharArrayUtils.equals(className, otherName)) {
         backup(mark);
         return true;
      }
      char[] destructorName = CharArrayUtils.concat(
            "~".toCharArray(), otherName); //$NON-NLS-1$
      if (CharArrayUtils.equals(destructorName, className)) {
         backup(mark);
         return true;
      }

      backup(mark);
      return false;
   }

   /**
    * This function parses a declaration specifier sequence, as according to the
    * ANSI C++ spec.
    * 
    * declSpecifier : "auto" | "register" | "static" | "extern" | "mutable" |
    * "inline" | "virtual" | "explicit" | "char" | "wchar_t" | "bool" | "short" |
    * "int" | "long" | "signed" | "unsigned" | "float" | "double" | "void" |
    * "const" | "volatile" | "friend" | "typedef" | ("typename")? name |
    * {"class"|"struct"|"union"} classSpecifier | {"enum"} enumSpecifier
    * 
    * Notes: - folded in storageClassSpecifier, typeSpecifier, functionSpecifier -
    * folded elaboratedTypeSpecifier into classSpecifier and enumSpecifier -
    * find template names in name
    * 
    * @param parm
    *           Is this for a parameter declaration (true) or simple declaration
    *           (false)
    * @param tryConstructor
    *           true for constructor, false for pointer to function strategy
    * 
    * @return TODO
    * @throws BacktrackException
    *            request a backtrack
    */
   protected ICPPASTDeclSpecifier declSpecifierSeq(boolean parm,
         boolean tryConstructor) throws BacktrackException, EndOfFileException {
      IToken firstToken = LA(1);
      Flags flags = new Flags(parm, tryConstructor);
      IToken last = null;

      boolean isInline = false, isVirtual = false, isExplicit = false, isFriend = false;
      boolean isConst = false, isVolatile = false, isRestrict = false;
      boolean isLong = false, isShort = false, isUnsigned = false, isSigned = false, isLongLong = false;
      boolean isTypename = false;

      int storageClass = IASTDeclSpecifier.sc_unspecified;
      int simpleType = IASTSimpleDeclSpecifier.t_unspecified;
      ITokenDuple duple = null;

      ICPPASTCompositeTypeSpecifier classSpec = null;
      ICPPASTElaboratedTypeSpecifier elabSpec = null;
      IASTEnumerationSpecifier enumSpec = null;
      IASTExpression typeofExpression = null;
      declSpecifiers: for (;;) {
         switch (LT(1)) {
            case IToken.t_inline:
               last = consume();
               isInline = true;
               break;
            case IToken.t_typedef:
               storageClass = IASTDeclSpecifier.sc_typedef;
               last = consume();
               break;
            case IToken.t_auto:
               last = consume();
               storageClass = IASTDeclSpecifier.sc_auto;
               break;
            case IToken.t_register:
               last = consume();
               storageClass = IASTDeclSpecifier.sc_register;
               break;
            case IToken.t_static:
               storageClass = IASTDeclSpecifier.sc_static;
               last = consume();
               break;
            case IToken.t_extern:
               storageClass = IASTDeclSpecifier.sc_extern;
               last = consume();
               break;
            case IToken.t_mutable:
               storageClass = ICPPASTDeclSpecifier.sc_mutable;
               last = consume();
               break;
            case IToken.t_virtual:
               isVirtual = true;
               last = consume();
               break;
            case IToken.t_explicit:
               isExplicit = true;
               last = consume();
               break;
            case IToken.t_friend:
               isFriend = true;
               last = consume();
               break;
            case IToken.t_const:
               isConst = true;
               last = consume();
               break;
            case IToken.t_volatile:
               isVolatile = true;
               last = consume();
               break;
            case IToken.t_restrict:
               if (!supportRestrict) {
                  IToken la = LA(1);
                  throwBacktrack(la.getOffset(), la.getEndOffset()
                        - la.getOffset());
               }
               isRestrict = true;
               last = consume();
               break;
            case IToken.t_signed:
               isSigned = true;
               flags.setEncounteredRawType(true);
               last = consume();
               break;
            case IToken.t_unsigned:
               isUnsigned = true;
               flags.setEncounteredRawType(true);
               last = consume();
               break;
            case IToken.t_short:
               isShort = true;
               flags.setEncounteredRawType(true);
               last = consume();
               break;
            case IToken.t_long:
               if (isLong && supportLongLong) {
                  isLong = false;
                  isLongLong = true;
               } else
                  isLong = true;
               flags.setEncounteredRawType(true);
               last = consume();
               break;
            case IToken.t__Complex:
               if (!supportComplex) {
                  IToken la = LA(1);
                  throwBacktrack(la.getOffset(), la.getEndOffset()
                        - la.getOffset());
               }
               last = consume(IToken.t__Complex);
               simpleType = IGPPASTSimpleDeclSpecifier.t_Complex;
               break;
            case IToken.t__Imaginary:
               if (!supportComplex) {
                  IToken la = LA(1);
                  throwBacktrack(la.getOffset(), la.getLength());
               }
               last = consume(IToken.t__Imaginary);
               simpleType = IGPPASTSimpleDeclSpecifier.t_Imaginary;
               break;
            case IToken.t_char:
               simpleType = IASTSimpleDeclSpecifier.t_char;
               flags.setEncounteredRawType(true);
               last = consume();
               break;
            case IToken.t_wchar_t:
               simpleType = ICPPASTSimpleDeclSpecifier.t_wchar_t;
               flags.setEncounteredRawType(true);
               last = consume();
               break;
            case IToken.t_bool:
               simpleType = ICPPASTSimpleDeclSpecifier.t_bool;
               flags.setEncounteredRawType(true);
               last = consume();
               break;
            case IToken.t_int:
               flags.setEncounteredRawType(true);
               last = consume();
               simpleType = IASTSimpleDeclSpecifier.t_int;
               break;
            case IToken.t_float:
               simpleType = IASTSimpleDeclSpecifier.t_float;
               flags.setEncounteredRawType(true);
               last = consume();
               break;
            case IToken.t_double:
               simpleType = IASTSimpleDeclSpecifier.t_double;
               flags.setEncounteredRawType(true);
               last = consume();
               break;
            case IToken.t_void:
               simpleType = IASTSimpleDeclSpecifier.t_void;
               flags.setEncounteredRawType(true);
               last = consume();
               break;
            case IToken.t_typename:
               isTypename = true;
               last = consume(IToken.t_typename);
               duple = name();
               flags.setEncounteredTypename(true);
               break;
            case IToken.tCOLONCOLON:
            case IToken.tIDENTIFIER:
               // TODO - Kludgy way to handle constructors/destructors
               if (flags.haveEncounteredRawType())
                  break declSpecifiers;

               if (parm && flags.haveEncounteredTypename())
                  break declSpecifiers;

               if (lookAheadForConstructorOrConversion(flags))
                  break declSpecifiers;

               if (lookAheadForDeclarator(flags))
                  break declSpecifiers;

               duple = name();
               last = duple.getLastToken();
               flags.setEncounteredTypename(true);
               break;
            case IToken.t_class:
            case IToken.t_struct:
            case IToken.t_union:
               try {
                  classSpec = classSpecifier();
                  flags.setEncounteredTypename(true);
                  break;
               } catch (BacktrackException bt) {
                  elabSpec = elaboratedTypeSpecifier();
                  flags.setEncounteredTypename(true);
                  break;
               }
            case IToken.t_enum:
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

      if (elabSpec != null) {
         elabSpec.setConst(isConst);
         elabSpec.setVolatile(isVolatile);
         if (elabSpec instanceof IGPPASTDeclSpecifier)
            ((IGPPASTDeclSpecifier) elabSpec).setRestrict(isRestrict);
         elabSpec.setFriend(isFriend);
         elabSpec.setInline(isInline);
         elabSpec.setStorageClass(storageClass);
         elabSpec.setVirtual(isVirtual);
         elabSpec.setExplicit(isExplicit);
         return elabSpec;
      }
      if (enumSpec != null) {
         enumSpec.setConst(isConst);
         enumSpec.setVolatile(isVolatile);
         if (enumSpec instanceof IGPPASTDeclSpecifier)
            ((IGPPASTDeclSpecifier) enumSpec).setRestrict(isRestrict);
         ((ICPPASTDeclSpecifier) enumSpec).setFriend(isFriend);
         ((ICPPASTDeclSpecifier) enumSpec).setVirtual(isVirtual);
         ((ICPPASTDeclSpecifier) enumSpec).setExplicit(isExplicit);
         enumSpec.setInline(isInline);
         enumSpec.setStorageClass(storageClass);
         return (ICPPASTDeclSpecifier) enumSpec;
      }
      if (classSpec != null) {
         classSpec.setConst(isConst);
         classSpec.setVolatile(isVolatile);
         if (classSpec instanceof IGPPASTDeclSpecifier)
            ((IGPPASTDeclSpecifier) classSpec).setRestrict(isRestrict);
         classSpec.setFriend(isFriend);
         classSpec.setInline(isInline);
         classSpec.setStorageClass(storageClass);
         classSpec.setVirtual(isVirtual);
         classSpec.setExplicit(isExplicit);
         return classSpec;
      }
      if (duple != null) {
         ICPPASTNamedTypeSpecifier nameSpec = createNamedTypeSpecifier();
         nameSpec.setIsTypename(isTypename);
         IASTName name = createName(duple);
         nameSpec.setName(name);
         name.setParent(nameSpec);
         name.setPropertyInParent(IASTNamedTypeSpecifier.NAME);

         nameSpec.setConst(isConst);
         nameSpec.setVolatile(isVolatile);
         if (nameSpec instanceof IGPPASTDeclSpecifier)
            ((IGPPASTDeclSpecifier) nameSpec).setRestrict(isRestrict);
         nameSpec.setFriend(isFriend);
         nameSpec.setInline(isInline);
         nameSpec.setStorageClass(storageClass);
         nameSpec.setVirtual(isVirtual);
         nameSpec.setExplicit(isExplicit);
         return nameSpec;
      }
      ICPPASTSimpleDeclSpecifier simpleDeclSpec = null;
      if (isLongLong || typeofExpression != null) {
         simpleDeclSpec = createGPPSimpleDeclSpecifier();
         ((IGPPASTSimpleDeclSpecifier) simpleDeclSpec).setLongLong(isLongLong);
         if (typeofExpression != null) {
            ((IGPPASTSimpleDeclSpecifier) simpleDeclSpec)
                  .setTypeofExpression(typeofExpression);
            typeofExpression.setParent(simpleDeclSpec);
            typeofExpression
                  .setPropertyInParent(IGPPASTSimpleDeclSpecifier.TYPEOF_EXPRESSION);
         }
      } else
         simpleDeclSpec = createSimpleDeclSpecifier();

      int l = last != null ? last.getEndOffset() - firstToken.getOffset() : 0;
      ((ASTNode) simpleDeclSpec).setOffsetAndLength(firstToken.getOffset(), l);
      simpleDeclSpec.setConst(isConst);
      simpleDeclSpec.setVolatile(isVolatile);
      if (simpleDeclSpec instanceof IGPPASTDeclSpecifier)
         ((IGPPASTDeclSpecifier) simpleDeclSpec).setRestrict(isRestrict);

      simpleDeclSpec.setFriend(isFriend);
      simpleDeclSpec.setInline(isInline);
      simpleDeclSpec.setStorageClass(storageClass);
      simpleDeclSpec.setVirtual(isVirtual);
      simpleDeclSpec.setExplicit(isExplicit);

      simpleDeclSpec.setType(simpleType);
      simpleDeclSpec.setLong(isLong);
      simpleDeclSpec.setShort(isShort);
      simpleDeclSpec.setUnsigned(isUnsigned);
      simpleDeclSpec.setSigned(isSigned);

      return simpleDeclSpec;
   }

   /**
    * @return
    */
   protected ICPPASTSimpleDeclSpecifier createGPPSimpleDeclSpecifier() {
      return new GPPASTSimpleDeclSpecifier();
   }

   /**
    * @return
    */
   protected ICPPASTSimpleDeclSpecifier createSimpleDeclSpecifier() {
      return new CPPASTSimpleDeclSpecifier();
   }

   /**
    * @return
    */
   protected ICPPASTNamedTypeSpecifier createNamedTypeSpecifier() {
      return new CPPASTNamedTypeSpecifier();
   }

   /**
    * Parse an elaborated type specifier.
    * 
    * @param decl
    *           Declaration which owns the elaborated type
    * @return TODO
    * 
    * @throws BacktrackException
    *            request a backtrack
    */
   protected ICPPASTElaboratedTypeSpecifier elaboratedTypeSpecifier()
         throws BacktrackException, EndOfFileException {
      // this is an elaborated class specifier
      IToken t = consume();
      int eck = 0;

      switch (t.getType()) {
         case IToken.t_class:
            eck = ICPPASTElaboratedTypeSpecifier.k_class;
            break;
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

      IASTName name = createName(name());

      ICPPASTElaboratedTypeSpecifier elaboratedTypeSpec = createElaboratedTypeSpecifier();
      ((ASTNode) elaboratedTypeSpec).setOffsetAndLength(t.getOffset(),
            calculateEndOffset(name) - t.getOffset());
      elaboratedTypeSpec.setKind(eck);
      elaboratedTypeSpec.setName(name);
      name.setParent(elaboratedTypeSpec);
      name.setPropertyInParent(IASTElaboratedTypeSpecifier.TYPE_NAME);
      return elaboratedTypeSpec;
   }

   /**
    * @return
    */
   protected ICPPASTElaboratedTypeSpecifier createElaboratedTypeSpecifier() {
      return new CPPASTElaboratedTypeSpecifier();
   }

   /**
    * Parses the initDeclarator construct of the ANSI C++ spec.
    * 
    * initDeclarator : declarator ("=" initializerClause | "(" expressionList
    * ")")?
    * 
    * @param constructInitializers
    *           TODO
    * @param owner
    *           IParserCallback object that represents the owner declaration
    *           object.
    * 
    * @return declarator that this parsing produced.
    * @throws BacktrackException
    *            request a backtrack
    */
   protected IASTDeclarator initDeclarator(SimpleDeclarationStrategy strategy)
         throws EndOfFileException, BacktrackException {
      IASTDeclarator d = declarator(strategy, false);

      IASTInitializer initializer = optionalCPPInitializer();
      if (initializer != null) {
         d.setInitializer(initializer);
         initializer.setParent(d);
         initializer.setPropertyInParent(IASTDeclarator.INITIALIZER);
      }

      return d;
   }

   protected IASTInitializer optionalCPPInitializer()
         throws EndOfFileException, BacktrackException {
      // handle initializer

      if (LT(1) == IToken.tASSIGN) {
         consume(IToken.tASSIGN);
         throwAwayMarksForInitializerClause();
         try {
            return initializerClause();
         } catch (EndOfFileException eof) {
            failParse();
            throw eof;
         }
      } else if (LT(1) == IToken.tLPAREN) {
         // initializer in constructor
         int o = consume(IToken.tLPAREN).getOffset(); // EAT IT!
         IASTExpression astExpression = expression();
         int l = consume(IToken.tRPAREN).getEndOffset();
         ICPPASTConstructorInitializer result = createConstructorInitializer();
         ((ASTNode) result).setOffsetAndLength(o, l - o);
         result.setExpression(astExpression);
         astExpression.setParent(result);
         astExpression
               .setPropertyInParent(ICPPASTConstructorInitializer.EXPRESSION);
         return result;
      }
      return null;
   }

   /**
    * @return
    */
   protected ICPPASTConstructorInitializer createConstructorInitializer() {
      return new CPPASTConstructorInitializer();
   }

   /**
    *  
    */
   protected IASTInitializer initializerClause() throws EndOfFileException,
         BacktrackException {
      if (LT(1) == IToken.tLBRACE) {
         int startingOffset = consume(IToken.tLBRACE).getOffset();

         IASTInitializerList result = createInitializerList();
         ((ASTNode) result).setOffset(startingOffset);

         if (LT(1) == (IToken.tRBRACE)) {
            int l = consume(IToken.tRBRACE).getEndOffset();
            ((ASTNode) result).setLength(l - startingOffset);
            return result;
         }

         // otherwise it is a list of initializer clauses

         for (;;) {
            if (LT(1) == IToken.tRBRACE)
               break;

            IASTInitializer clause = initializerClause();
            if (clause != null) {
               result.addInitializer(clause);
               clause.setParent(result);
               clause
                     .setPropertyInParent(IASTInitializerList.NESTED_INITIALIZER);
            }
            if (LT(1) == IToken.tRBRACE)
               break;
            consume(IToken.tCOMMA);
         }
         int l = consume(IToken.tRBRACE).getEndOffset();
         ((ASTNode) result).setLength(l - startingOffset);
         return result;
      }

      // if we get this far, it means that we did not
      // try this now instead
      // assignmentExpression
      IASTExpression assignmentExpression = assignmentExpression();
      IASTInitializerExpression result = createInitializerExpression();
      ((ASTNode) result).setOffsetAndLength(((ASTNode) assignmentExpression));
      result.setExpression(assignmentExpression);
      assignmentExpression.setParent(result);
      assignmentExpression
            .setPropertyInParent(IASTInitializerExpression.INITIALIZER_EXPRESSION);
      return result;
   }

   /**
    * @return
    */
   protected IASTInitializerList createInitializerList() {
      return new CPPASTInitializerList();
   }

   /**
    * @return
    */
   protected IASTInitializerExpression createInitializerExpression() {
      return new CPPASTInitializerExpression();
   }

   /**
    * Parse a declarator, as according to the ANSI C++ specification.
    * 
    * declarator : (ptrOperator)* directDeclarator
    * 
    * directDeclarator : declaratorId | directDeclarator "("
    * parameterDeclarationClause ")" (cvQualifier)* (exceptionSpecification)* |
    * directDeclarator "[" (constantExpression)? "]" | "(" declarator")" |
    * directDeclarator "(" parameterDeclarationClause ")"
    * (oldKRParameterDeclaration)*
    * 
    * declaratorId : name
    * 
    * @param forTypeID
    *           TODO
    * @param container
    *           IParserCallback object that represents the owner declaration.
    * @return declarator that this parsing produced.
    * @throws BacktrackException
    *            request a backtrack
    */
   protected IASTDeclarator declarator(SimpleDeclarationStrategy strategy,
         boolean forTypeID) throws EndOfFileException, BacktrackException {

      IToken la = LA(1);
      int startingOffset = la.getOffset();
      la = null;
      IASTDeclarator innerDecl = null;
      IASTName declaratorName = null;
      List pointerOps = new ArrayList(DEFAULT_POINTEROPS_LIST_SIZE);
      List parameters = Collections.EMPTY_LIST;
      List arrayMods = Collections.EMPTY_LIST;
      List exceptionSpecIds = Collections.EMPTY_LIST;
      boolean encounteredVarArgs = false;
      boolean tryEncountered = false;
      IASTExpression bitField = null;
      boolean isFunction = false;
      boolean isPureVirtual = false, isConst = false, isVolatile = false;

      overallLoop: do {

         consumePointerOperators(pointerOps);

         if (!forTypeID && LT(1) == IToken.tLPAREN) {
            IToken mark = mark();
            try {
               consume();
               innerDecl = declarator(strategy, forTypeID);
               consume(IToken.tRPAREN);
            } catch (BacktrackException bte) {
               backup(mark);
            }
            declaratorName = createName();
         } else {
            try {
               ITokenDuple d = consumeTemplatedOperatorName();
               declaratorName = createName(d);
               if (d.isConversion())
                  isFunction = true;
            } catch (BacktrackException bt) {
               declaratorName = createName();
            }
         }

         for (;;) {
            switch (LT(1)) {
               case IToken.tLPAREN:

                  boolean failed = false;

                  // temporary fix for initializer/function declaration
                  // ambiguity
                  if (!LA(2).looksLikeExpression()
                        && strategy != SimpleDeclarationStrategy.TRY_VARIABLE) {
                     if (LT(2) == IToken.tIDENTIFIER) {
                        IToken newMark = mark();
                        consume(IToken.tLPAREN);
                        try {
                           name();
                           //TODO - we need to lookup/resolve this name
                           //see if its a type ...
                           //if it is a type, failed = false
                           //else failed = true
                           failed = false;
                        } catch (BacktrackException b) {
                           failed = true;
                        }

                        backup(newMark);
                     }
                  }
                  if ((!LA(2).looksLikeExpression()
                        && strategy != SimpleDeclarationStrategy.TRY_VARIABLE && !failed)) {
                     // parameterDeclarationClause
                     isFunction = true;
                     // TODO need to create a temporary scope object here
                     IToken last = consume(IToken.tLPAREN);
                     boolean seenParameter = false;
                     parameterDeclarationLoop: for (;;) {
                        switch (LT(1)) {
                           case IToken.tRPAREN:
                              last = consume();
                              break parameterDeclarationLoop;
                           case IToken.tELLIPSIS:
                              last = consume();
                              encounteredVarArgs = true;
                              break;
                           case IToken.tCOMMA:
                              last = consume();
                              seenParameter = false;
                              break;
                           default:
                              int endOffset = (last != null) ? last
                                    .getEndOffset() : LA(1).getEndOffset();
                              if (seenParameter)
                                 throwBacktrack(startingOffset, endOffset
                                       - startingOffset);
                              IASTParameterDeclaration p = parameterDeclaration();
                              if (parameters == Collections.EMPTY_LIST)
                                 parameters = new ArrayList(
                                       DEFAULT_PARM_LIST_SIZE);
                              parameters.add(p);
                              seenParameter = true;
                        }
                     }
                  }

                  if (LT(1) == IToken.tCOLON)
                     break overallLoop;

                  if (LT(1) == IToken.t_try) {
                     tryEncountered = true;
                     break overallLoop;
                  }

                  IToken beforeCVModifier = mark();
                  IToken[] cvModifiers = new IToken[2];
                  int numCVModifiers = 0;
                  IToken afterCVModifier = beforeCVModifier;
                  // const-volatile
                  // 2 options: either this is a marker for the method,
                  // or it might be the beginning of old K&R style
                  // parameter declaration, see
                  //      void getenv(name) const char * name; {}
                  // This will be determined further below
                  while ((LT(1) == IToken.t_const || LT(1) == IToken.t_volatile)
                        && numCVModifiers < 2) {
                     cvModifiers[numCVModifiers++] = consume();
                     afterCVModifier = mark();
                  }
                  //check for throws clause here

                  if (LT(1) == IToken.t_throw) {
                     exceptionSpecIds = new ArrayList(
                           DEFAULT_SIZE_EXCEPTIONS_LIST);
                     consume(IToken.t_throw); // throw
                     consume(IToken.tLPAREN); // (
                     boolean done = false;
                     while (!done) {
                        switch (LT(1)) {
                           case IToken.tRPAREN:
                              consume();
                              done = true;
                              break;
                           case IToken.tCOMMA:
                              consume();
                              break;
                           default:
                              try {
                                 exceptionSpecIds.add(typeId(false));
                              } catch (BacktrackException e) {
                                 IASTProblem p = failParse(e);
                                 IASTProblemTypeId typeIdProblem = createTypeIDProblem();
                                 typeIdProblem.setProblem(p);
                                 ((CPPASTNode) typeIdProblem)
                                       .setOffsetAndLength(((CPPASTNode) p));
                                 p.setParent(typeIdProblem);
                                 p
                                       .setPropertyInParent(IASTProblemHolder.PROBLEM);
                                 exceptionSpecIds.add(typeIdProblem);
                              }
                              break;
                        }
                     }
                  }
                  // check for optional pure virtual
                  if (LT(1) == IToken.tASSIGN && LT(2) == IToken.tINTEGER) {
                     char[] image = LA(2).getCharImage();
                     if (image.length == 1 && image[0] == '0') {
                        consume(IToken.tASSIGN);
                        consume(IToken.tINTEGER);
                        isPureVirtual = true;
                     }
                  }
                  if (afterCVModifier != LA(1) || LT(1) == IToken.tSEMI) {
                     // There were C++-specific clauses after
                     // const/volatile modifier
                     // Then it is a marker for the method
                     if (numCVModifiers > 0) {
                        for (int i = 0; i < numCVModifiers; i++) {
                           if (cvModifiers[i].getType() == IToken.t_const)
                              isConst = true;
                           if (cvModifiers[i].getType() == IToken.t_volatile)
                              isVolatile = true;
                        }
                     }
                     afterCVModifier = mark();
                     // In this case (method) we can't expect K&R
                     // parameter declarations,
                     // but we'll check anyway, for errorhandling
                  }
                  break;
               case IToken.tLBRACKET:
                  arrayMods = new ArrayList(DEFAULT_POINTEROPS_LIST_SIZE);
                  consumeArrayModifiers(arrayMods);
                  continue;
               case IToken.tCOLON:
                  consume(IToken.tCOLON);
                  bitField = constantExpression();
                  break;
               default:
                  break;
            }
            break;
         }
         if (LA(1).getType() != IToken.tIDENTIFIER)
            break;

      } while (true);

      IASTDeclarator d = null;
      if (isFunction) {

         ICPPASTFunctionDeclarator fc = null;
         if (tryEncountered)
            fc = createTryBlockDeclarator();
         else
            fc = createFunctionDeclarator();
         fc.setVarArgs(encounteredVarArgs);
         for (int i = 0; i < parameters.size(); ++i) {
            IASTParameterDeclaration p = (IASTParameterDeclaration) parameters
                  .get(i);
            p.setParent(fc);
            p.setPropertyInParent(IASTFunctionDeclarator.FUNCTION_PARAMETER);
            fc.addParameterDeclaration(p);
         }
         fc.setConst(isConst);
         fc.setVolatile(isVolatile);
         fc.setPureVirtual(isPureVirtual);
         for (int i = 0; i < exceptionSpecIds.size(); ++i) {
            IASTTypeId typeId = (IASTTypeId) exceptionSpecIds.get(i);
            fc.addExceptionSpecificationTypeId(typeId);
            typeId.setParent(fc);
            typeId
                  .setPropertyInParent(ICPPASTFunctionDeclarator.EXCEPTION_TYPEID);
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

      return d;

   }

   /**
    * @return
    */
   protected IASTProblemTypeId createTypeIDProblem() {
      return new CPPASTProblemTypeId();
   }

   /**
    * @return
    */
   protected ICPPASTFunctionTryBlockDeclarator createTryBlockDeclarator() {
      return new CPPASTFunctionTryBlockDeclarator();
   }

   /**
    * @return
    */
   protected ICPPASTFunctionDeclarator createFunctionDeclarator() {
      return new CPPASTFunctionDeclarator();
   }

   /**
    * @return
    */
   protected IASTFieldDeclarator createFieldDeclarator() {
      return new CPPASTFieldDeclarator();
   }

   /**
    * @return
    */
   protected IASTDeclarator createArrayDeclarator() {
      return new CPPASTArrayDeclarator();
   }

   /**
    * @return
    */
   protected IASTDeclarator createDeclarator() {
      return new CPPASTDeclarator();
   }

   protected ITokenDuple consumeTemplatedOperatorName()
         throws EndOfFileException, BacktrackException {
      TemplateParameterManager argumentList = TemplateParameterManager
            .getInstance();
      try {
         if (LT(1) == IToken.t_operator)
            return operatorId(null, null);

         try {
            return name();
         } catch (BacktrackException bt) {
         }
         IToken start = null;

         boolean hasTemplateId = false;

         IToken mark = mark();
         if (LT(1) == IToken.tCOLONCOLON || LT(1) == IToken.tIDENTIFIER) {
            start = consume();
            IToken end = null;

            if (start.getType() == IToken.tIDENTIFIER) {
               end = consumeTemplateArguments(end, argumentList);
               if (end != null && end.getType() == IToken.tGT)
                  hasTemplateId = true;
            }

            while (LT(1) == IToken.tCOLONCOLON || LT(1) == IToken.tIDENTIFIER) {
               end = consume();
               if (end.getType() == IToken.tIDENTIFIER) {
                  end = consumeTemplateArguments(end, argumentList);
                  if (end.getType() == IToken.tGT)
                     hasTemplateId = true;
               }
            }
            if (LT(1) == IToken.t_operator)
               end = operatorId(start, (hasTemplateId ? argumentList : null))
                     .getLastToken();
            else {
               int endOffset = (end != null) ? end.getEndOffset() : 0;
               backup(mark);
               throwBacktrack(mark.getOffset(), endOffset - mark.getOffset());
            }
            return TokenFactory.createTokenDuple(start, end, argumentList
                  .getTemplateArgumentsList());
         }
         int endOffset = (mark != null) ? mark.getEndOffset() : 0;
         backup(mark);
         throwBacktrack(mark.getOffset(), endOffset - mark.getOffset());

         return null;
      } finally {
         TemplateParameterManager.returnInstance(argumentList);
      }
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
   protected ICPPASTCompositeTypeSpecifier classSpecifier()
         throws BacktrackException, EndOfFileException {
      int classKind = 0;
      IToken classKey = null;
      IToken mark = mark();

      // class key
      switch (LT(1)) {
         case IToken.t_class:
            classKey = consume();
            classKind = ICPPASTCompositeTypeSpecifier.k_class;
            break;
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

      IASTName name = null;

      // class name
      if (LT(1) == IToken.tIDENTIFIER)
         name = createName(name());
      else
         name = createName();

      if (LT(1) != IToken.tCOLON && LT(1) != IToken.tLBRACE) {
         IToken errorPoint = LA(1);
         backup(mark);
         throwBacktrack(errorPoint.getOffset(), errorPoint.getLength());
      }

      ICPPASTCompositeTypeSpecifier astClassSpecifier = createClassSpecifier();
      ((ASTNode) astClassSpecifier).setOffset(classKey.getOffset());
      astClassSpecifier.setKey(classKind);
      astClassSpecifier.setName(name);
      name.setParent(astClassSpecifier);
      name.setPropertyInParent(IASTCompositeTypeSpecifier.TYPE_NAME);

      // base clause
      if (LT(1) == IToken.tCOLON) {
         baseSpecifier(astClassSpecifier);
      }

      if (LT(1) == IToken.tLBRACE) {
         consume(IToken.tLBRACE);

         memberDeclarationLoop: while (LT(1) != IToken.tRBRACE) {
            int checkToken = LA(1).hashCode();
            switch (LT(1)) {
               case IToken.t_public:
               case IToken.t_protected:
               case IToken.t_private:
                  IToken key = consume();
                  int l = consume(IToken.tCOLON).getEndOffset();
                  ICPPASTVisiblityLabel label = createVisibilityLabel();
                  ((ASTNode) label).setOffsetAndLength(key.getOffset(), l
                        - key.getOffset());
                  label.setVisibility(token2Visibility(key.getType()));
                  astClassSpecifier.addMemberDeclaration(label);
                  label.setParent(astClassSpecifier);
                  label
                        .setPropertyInParent(ICPPASTCompositeTypeSpecifier.VISIBILITY_LABEL);
                  break;
               case IToken.tRBRACE:
                  consume(IToken.tRBRACE);
                  break memberDeclarationLoop;
               default:
                  try {
                     IASTDeclaration d = declaration();
                     astClassSpecifier.addMemberDeclaration(d);
                     d.setParent(astClassSpecifier);
                     d
                           .setPropertyInParent(IASTCompositeTypeSpecifier.MEMBER_DECLARATION);
                  } catch (BacktrackException bt) {
                     IASTProblem p = failParse(bt);
                     IASTProblemDeclaration pd = createProblemDeclaration();
                     pd.setProblem(p);
                     ((CPPASTNode) pd).setOffsetAndLength(((CPPASTNode) p));
                     p.setParent(pd);
                     p.setPropertyInParent(IASTProblemHolder.PROBLEM);
                     astClassSpecifier.addMemberDeclaration(pd);
                     pd.setParent(astClassSpecifier);
                     pd
                           .setPropertyInParent(IASTCompositeTypeSpecifier.MEMBER_DECLARATION);
                     if (checkToken == LA(1).hashCode())
                        errorHandling();
                  }
            }
            if (checkToken == LA(1).hashCode())
               failParseWithErrorHandling();
         }
         // consume the }
         int l = consume(IToken.tRBRACE).getEndOffset();
         ((ASTNode) astClassSpecifier).setLength(l - classKey.getOffset());

      }
      return astClassSpecifier;
   }

   /**
    * @return
    */
   protected ICPPASTCompositeTypeSpecifier createClassSpecifier() {
      return new CPPASTCompositeTypeSpecifier();
   }

   /**
    * @return
    */
   protected ICPPASTVisiblityLabel createVisibilityLabel() {
      return new CPPASTVisibilityLabel();
   }

   /**
    * @param type
    * @return
    */
   protected int token2Visibility(int type) {
      switch (type) {
         case IToken.t_public:
            return ICPPASTVisiblityLabel.v_public;
         case IToken.t_protected:
            return ICPPASTVisiblityLabel.v_protected;
         case IToken.t_private:
            return ICPPASTVisiblityLabel.v_private;
      }
      return 0;
   }

   /**
    * Parse the subclass-baseclauses for a class specification.
    * 
    * baseclause: : basespecifierlist basespecifierlist: basespecifier
    * basespecifierlist, basespecifier basespecifier: ::? nestednamespecifier?
    * classname virtual accessspecifier? ::? nestednamespecifier? classname
    * accessspecifier virtual? ::? nestednamespecifier? classname
    * accessspecifier: private | protected | public
    * 
    * @param classSpecOwner
    * @throws BacktrackException
    */
   protected void baseSpecifier(ICPPASTCompositeTypeSpecifier astClassSpec)
         throws EndOfFileException, BacktrackException {

      IToken last = consume(IToken.tCOLON);

      boolean isVirtual = false;
      int visibility = 0; //ASTAccessVisibility.PUBLIC;
      IASTName name = null;
      IToken firstToken = null;
      baseSpecifierLoop: for (;;) {
         switch (LT(1)) {
            case IToken.t_virtual:
               if (firstToken == null)
                  firstToken = consume(IToken.t_virtual);
               else
                  last = consume(IToken.t_virtual);
               isVirtual = true;
               break;
            case IToken.t_public:
               visibility = ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.v_public;
               if (firstToken == null)
                  firstToken = consume();
               else
                  last = consume();
               break;
            case IToken.t_protected:
               visibility = ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.v_protected;
               if (firstToken == null)
                  firstToken = consume();
               else
                  last = consume();
               break;
            case IToken.t_private:
               visibility = ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.v_private;
               if (firstToken == null)
                  firstToken = consume();
               else
                  last = consume();
               break;
            case IToken.tCOLONCOLON:
            case IToken.tIDENTIFIER:
               //to get templates right we need to use the class as the scope
               ITokenDuple d = name();
               name = createName(d);
               last = d.getLastToken();
               break;
            case IToken.tCOMMA:
               if (name == null)
                  name = createName();
               consume();
               ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier baseSpec = createBaseSpecifier();
               if (firstToken != null)
                  ((ASTNode) baseSpec).setOffsetAndLength(firstToken
                        .getOffset(), last.getEndOffset()
                        - firstToken.getOffset());
               baseSpec.setVirtual(isVirtual);
               baseSpec.setVisibility(visibility);
               baseSpec.setName(name);
               name.setParent(baseSpec);
               name
                     .setPropertyInParent(ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.NAME);

               astClassSpec.addBaseSpecifier(baseSpec);
               baseSpec.setParent(astClassSpec);
               baseSpec
                     .setPropertyInParent(ICPPASTCompositeTypeSpecifier.BASE_SPECIFIER);

               isVirtual = false;
               visibility = 0;
               name = null;
               firstToken = null;

               continue baseSpecifierLoop;
            case IToken.tLBRACE:
               if (name == null)
                  name = createName();
               baseSpec = createBaseSpecifier();
               if (firstToken != null)
                  ((ASTNode) baseSpec)
                        .setOffsetAndLength(firstToken.getOffset(), last
                              .getOffset()
                              - firstToken.getOffset());
               baseSpec.setVirtual(isVirtual);
               baseSpec.setVisibility(visibility);
               baseSpec.setName(name);
               name.setParent(baseSpec);
               name
                     .setPropertyInParent(ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.NAME);

               astClassSpec.addBaseSpecifier(baseSpec);
               baseSpec.setParent(astClassSpec);
               baseSpec
                     .setPropertyInParent(ICPPASTCompositeTypeSpecifier.BASE_SPECIFIER);
            //fall through
            default:
               break baseSpecifierLoop;
         }
      }
   }

   /**
    * @return
    */
   protected ICPPASTBaseSpecifier createBaseSpecifier() {
      return new CPPASTBaseSpecifier();
   }

   protected void catchHandlerSequence(List collection)
         throws EndOfFileException, BacktrackException {
      if (LT(1) != IToken.t_catch) {
         IToken la = LA(1);
         throwBacktrack(la.getOffset(), la.getLength()); // error, need at least
         // one of these
      }
      while (LT(1) == IToken.t_catch) {
         int startOffset = consume(IToken.t_catch).getOffset();
         consume(IToken.tLPAREN);
         boolean isEllipsis = false;
         IASTDeclaration decl = null;
         try {
            if (LT(1) == IToken.tELLIPSIS) {
               consume(IToken.tELLIPSIS);
               isEllipsis = true;
            } else {
               decl = simpleDeclaration(SimpleDeclarationStrategy.TRY_VARIABLE,
                     true);
            }
            consume(IToken.tRPAREN);
         } catch (BacktrackException bte) {
            IASTProblem p = failParse(bte);
            IASTProblemDeclaration pd = createProblemDeclaration();
            pd.setProblem(p);
            ((CPPASTNode) pd).setOffsetAndLength(((CPPASTNode) p));
            p.setParent(pd);
            p.setPropertyInParent(IASTProblemHolder.PROBLEM);
            decl = pd;
         }

         IASTStatement compoundStatement = catchBlockCompoundStatement();
         ICPPASTCatchHandler handler = createCatchHandler();
         ((ASTNode) handler).setOffsetAndLength(startOffset,
               calculateEndOffset(compoundStatement) - startOffset);
         handler.setIsCatchAll(isEllipsis);
         if (decl != null) {
            handler.setDeclaration(decl);
            decl.setParent(handler);
            decl.setPropertyInParent(ICPPASTCatchHandler.DECLARATION);
         }
         if (compoundStatement != null) {
            handler.setCatchBody(compoundStatement);
            compoundStatement.setParent(handler);
            compoundStatement
                  .setPropertyInParent(ICPPASTCatchHandler.CATCH_BODY);
         }
         collection.add(handler);
      }
   }

   /**
    * @return
    */
   protected ICPPASTCatchHandler createCatchHandler() {
      return new CPPASTCatchHandler();
   }

   protected IASTStatement catchBlockCompoundStatement()
         throws BacktrackException, EndOfFileException {
      if (mode == ParserMode.QUICK_PARSE || mode == ParserMode.STRUCTURAL_PARSE) {
         IToken curr = LA(1);
         IToken last = skipOverCompoundStatement();
         IASTCompoundStatement cs = createCompoundStatement();
         ((ASTNode) cs).setOffsetAndLength(curr.getOffset(), last
               .getEndOffset()
               - curr.getOffset());
         return cs;
      } else if (mode == ParserMode.COMPLETION_PARSE
            || mode == ParserMode.SELECTION_PARSE) {
         if (scanner.isOnTopContext())
            return compoundStatement();
         IToken curr = LA(1);
         IToken last = skipOverCompoundStatement();
         IASTCompoundStatement cs = createCompoundStatement();
         ((ASTNode) cs).setOffsetAndLength(curr.getOffset(), last
               .getEndOffset()
               - curr.getOffset());
         return cs;
      }
      return compoundStatement();
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
         return e;

      } catch (BacktrackException bt) {
         backup(mark);
         try {
            return simpleDeclarationStrategyUnion();
         } catch (BacktrackException b) {
            failParse();
            throwBacktrack(b);
            return null;
         }
      }

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

      while (true) {
         try {
            int checkOffset = LA(1).hashCode();
            IASTDeclaration declaration = declaration();
            translationUnit.addDeclaration(declaration);
            declaration.setParent(translationUnit);
            declaration
                  .setPropertyInParent(IASTTranslationUnit.OWNED_DECLARATION);

            if (LA(1).hashCode() == checkOffset)
               failParseWithErrorHandling();
         } catch (EndOfFileException e) {
            if (translationUnit.getDeclarations().length != 0) {
               CPPASTNode d = (CPPASTNode) translationUnit.getDeclarations()[translationUnit
                     .getDeclarations().length - 1];
               ((CPPASTNode) translationUnit).setLength(d.getOffset()
                     + d.getLength());
            } else
               ((CPPASTNode) translationUnit).setLength(0);
            break;
         } catch (BacktrackException b) {
            try {
               // Mark as failure and try to reach a recovery point
               IASTProblem p = failParse(b);
               IASTProblemDeclaration pd = createProblemDeclaration();
               p.setParent(pd);
               pd.setProblem(p);
               ((CPPASTNode) pd).setOffsetAndLength(((CPPASTNode) p));
               p.setPropertyInParent(IASTProblemHolder.PROBLEM);
               translationUnit.addDeclaration(pd);
               pd.setParent(translationUnit);
               pd.setPropertyInParent(IASTTranslationUnit.OWNED_DECLARATION);
               errorHandling();
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
   }

   /**
    * @return
    */
   protected IASTProblemDeclaration createProblemDeclaration() {
      return new CPPASTProblemDeclaration();
   }

   /**
    * @return
    */
   protected CPPASTTranslationUnit createTranslationUnit() {
      return new CPPASTTranslationUnit();
   }

   protected void consumeArrayModifiers(List collection)
         throws EndOfFileException, BacktrackException {
      while (LT(1) == IToken.tLBRACKET) {
         int o = consume(IToken.tLBRACKET).getOffset(); // eat the '['

         IASTExpression exp = null;
         if (LT(1) != IToken.tRBRACKET) {
            exp = constantExpression();
         }
         int l = consume(IToken.tRBRACKET).getEndOffset();
         IASTArrayModifier arrayMod = createArrayModifier();
         ((ASTNode) arrayMod).setOffsetAndLength(o, l - o);
         if (exp != null) {
            arrayMod.setConstantExpression(exp);
            exp.setParent(arrayMod);
            exp.setPropertyInParent(IASTArrayModifier.CONSTANT_EXPRESSION);
         }
         collection.add(arrayMod);
      }
      return;
   }

   /**
    * @return
    */
   protected IASTArrayModifier createArrayModifier() {
      return new CPPASTArrayModifier();
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
      return new CPPASTCompoundStatement();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createBinaryExpression()
    */
   protected IASTBinaryExpression createBinaryExpression() {
      return new CPPASTBinaryExpression();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createConditionalExpression()
    */
   protected IASTConditionalExpression createConditionalExpression() {
      return new CPPASTConditionalExpression();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createUnaryExpression()
    */
   protected IASTUnaryExpression createUnaryExpression() {
      return new CPPASTUnaryExpression();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createCompoundStatementExpression()
    */
   protected IGNUASTCompoundStatementExpression createCompoundStatementExpression() {
      return new CPPASTCompoundStatementExpression();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createExpressionList()
    */
   protected IASTExpressionList createExpressionList() {
      return new CPPASTExpressionList();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createName(org.eclipse.cdt.core.parser.IToken)
    */
   protected IASTName createName(IToken token) {
      return new CPPASTName(token.getCharImage());
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createName()
    */
   protected IASTName createName() {
      return new CPPASTName();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createEnumerator()
    */
   protected IASTEnumerator createEnumerator() {
      return new CPPASTEnumerator();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#buildTypeIdExpression(int,
    *      org.eclipse.cdt.core.dom.ast.IASTTypeId, int)
    */
   protected IASTExpression buildTypeIdExpression(int op, IASTTypeId typeId,
         int startingOffset, int endingOffset) {
      IASTTypeIdExpression typeIdExpression = createTypeIdExpression();
      ((ASTNode) typeIdExpression).setOffsetAndLength(startingOffset,
            endingOffset - startingOffset);
      ((ASTNode) typeIdExpression).setLength(endingOffset - startingOffset);
      typeIdExpression.setOperator(op);
      typeIdExpression.setTypeId(typeId);
      typeId.setParent(typeIdExpression);
      typeId.setPropertyInParent(IASTTypeIdExpression.TYPE_ID);
      return typeIdExpression;
   }

   /**
    * @return
    */
   protected IASTTypeIdExpression createTypeIdExpression() {
      return new CPPASTTypeIdExpression();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createEnumerationSpecifier()
    */
   protected IASTEnumerationSpecifier createEnumerationSpecifier() {
      return new CPPASTEnumerationSpecifier();
   }

   /**
    * @return
    */
   protected IASTLabelStatement createLabelStatement() {
      return new CPPASTLabelStatement();
   }

   /**
    * @return
    */
   protected IASTGotoStatement createGoToStatement() {
      return new CPPASTGotoStatement();
   }

   /**
    * @return
    */
   protected IASTReturnStatement createReturnStatement() {
      return new CPPASTReturnStatement();
   }

   /**
    * @return
    */
   protected IASTForStatement createForStatement() {
      return new CPPASTForStatement();
   }

   /**
    * @return
    */
   protected IASTContinueStatement createContinueStatement() {
      return new CPPASTContinueStatement();
   }

   /**
    * @return
    */
   protected IASTDoStatement createDoStatement() {
      return new CPPASTDoStatement();
   }

   /**
    * @return
    */
   protected IASTBreakStatement createBreakStatement() {
      return new CPPASTBreakStatement();
   }

   /**
    * @return
    */
   protected IASTWhileStatement createWhileStatement() {
      return new CPPASTWhileStatement();
   }

   /**
    * @return
    */
   protected IASTNullStatement createNullStatement() {
      return new CPPASTNullStatement();
   }

   /**
    * @return
    */
   protected IASTSwitchStatement createSwitchStatement() {
      return new CPPASTSwitchStatement();
   }

   /**
    * @return
    */
   protected IASTIfStatement createIfStatement() {
      return new CPPASTIfStatement();
   }

   /**
    * @return
    */
   protected IASTDefaultStatement createDefaultStatement() {
      return new CPPASTDefaultStatement();
   }

   /**
    * @return
    */
   protected IASTCaseStatement createCaseStatement() {
      return new CPPASTCaseStatement();
   }

   /**
    * @return
    */
   protected IASTExpressionStatement createExpressionStatement() {
      return new CPPASTExpressionStatement();
   }

   /**
    * @return
    */
   protected IASTDeclarationStatement createDeclarationStatement() {
      return new CPPASTDeclarationStatement();
   }

   /**
    * @return
    */
   protected IASTASMDeclaration createASMDirective() {
      return new CPPASTASMDeclaration();
   }

   /**
    * @return
    */
   protected IASTCastExpression createCastExpression() {
      return new CPPASTCastExpression();
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
         case IToken.t_try:
            return parseTryStatement();
         default:
            // can be many things:
            // label
            if (LT(1) == IToken.tIDENTIFIER && LT(2) == IToken.tCOLON) {
               return parseLabelStatement();
            }

            return parseDeclarationOrExpressionStatement();
      }
   }

   /**
    * @return @throws
    *         EndOfFileException
    * @throws BacktrackException
    */
   protected IASTStatement parseTryStatement() throws EndOfFileException,
         BacktrackException {
      int startO = consume().getOffset();
      IASTStatement tryBlock = compoundStatement();
      List catchHandlers = new ArrayList(DEFAULT_CATCH_HANDLER_LIST_SIZE);
      catchHandlerSequence(catchHandlers);
      ICPPASTTryBlockStatement tryStatement = createTryBlockStatement();
      ((ASTNode) tryStatement).setOffset(startO);
      tryStatement.setTryBody(tryBlock);
      tryBlock.setParent(tryStatement);
      tryBlock.setPropertyInParent(ICPPASTTryBlockStatement.BODY);

      for (int i = 0; i < catchHandlers.size(); ++i) {
         ICPPASTCatchHandler handler = (ICPPASTCatchHandler) catchHandlers
               .get(i);
         tryStatement.addCatchHandler(handler);
         handler.setParent(tryStatement);
         handler.setPropertyInParent(ICPPASTTryBlockStatement.CATCH_HANDLER);
         ((ASTNode) tryStatement).setLength(calculateEndOffset(handler)
               - startO);
      }
      return tryStatement;
   }

   /**
    * @return
    */
   protected ICPPASTTryBlockStatement createTryBlockStatement() {
      return new CPPASTTryBlockStatement();
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
      return new CPPASTProblemStatement();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser#createProblemExpression()
    */
   protected IASTProblemExpression createProblemExpression() {
      return new CPPASTProblemExpression();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser#createProblem(int,
    *      int, int)
    */
   protected IASTProblem createProblem(int signal, int offset, int length) {
      IASTProblem result = new CPPASTProblem(signal, EMPTY_STRING, false, true);
      ((ASTNode) result).setOffsetAndLength(offset, length);
      ((ASTNode) result).setLength(length);
      return result;
   }

}