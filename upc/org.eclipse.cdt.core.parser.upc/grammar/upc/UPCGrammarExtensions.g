-----------------------------------------------------------------------------------
-- Copyright (c) 2006, 2009 IBM Corporation and others.
-- This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License 2.0
-- which accompanies this distribution, and is available at
-- https://www.eclipse.org/legal/epl-2.0/
--
-- SPDX-License-Identifier: EPL-2.0
--
-- Contributors:
--     IBM Corporation - initial API and implementation
-----------------------------------------------------------------------------------


$Define
	$build_action_class /. UPCParserAction ./
	$node_factory_create_expression /. new UPCASTNodeFactory() ./
	$parser_factory_create_expression /. UPCSecondaryParserFactory.getDefault() ./
$End


$Globals
/.
import org.eclipse.cdt.core.dom.parser.upc.UPCASTNodeFactory;
import org.eclipse.cdt.core.dom.parser.upc.UPCSecondaryParserFactory;
import org.eclipse.cdt.core.dom.parser.upc.UPCParserAction;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTKeywordExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSynchronizationStatement;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTUnarySizeofExpression;
import org.eclipse.cdt.core.dom.lrparser.lpgextensions.FixedBacktrackingParser;
./
$End


$Terminals  -- Additional keywords defined by UPC
	MYTHREAD
	THREADS
	UPC_MAX_BLOCKSIZE
	relaxed
	shared
	strict
	upc_barrier 
	upc_localsizeof
	upc_blocksizeof 
	upc_elemsizeof 
	upc_notify
	upc_fence 
	upc_wait
	upc_forall
$End



$Rules  -- UPC grammar extensions to C99

-----------------------------------------------------------------------------------
-- Expressions
-----------------------------------------------------------------------------------


literal
    ::= 'MYTHREAD'
            /. $Build  consumeKeywordExpression(IUPCASTKeywordExpression.kw_mythread); $EndBuild ./
      | 'THREADS'
            /. $Build  consumeKeywordExpression(IUPCASTKeywordExpression.kw_threads); $EndBuild ./
      | 'UPC_MAX_BLOCKSIZE'
            /. $Build  consumeKeywordExpression(IUPCASTKeywordExpression.kw_upc_max_block_size); $EndBuild ./
            

-- causes ambiguities because of no type information, solution is SGLR
unary_expression
    ::= 'upc_localsizeof' unary_expression
          /. $Build  consumeExpressionUnarySizeofOperator(IUPCASTUnarySizeofExpression.upc_localsizeof); $EndBuild ./
      | 'upc_localsizeof' '(' type_id ')'
          /. $Build  consumeExpressionSizeofTypeId(IUPCASTUnarySizeofExpression.upc_localsizeof); $EndBuild ./
      | 'upc_blocksizeof' unary_expression
          /. $Build  consumeExpressionUnarySizeofOperator(IUPCASTUnarySizeofExpression.upc_blocksizeof); $EndBuild ./
      | 'upc_blocksizeof' '(' type_id ')'
          /. $Build  consumeExpressionSizeofTypeId(IUPCASTUnarySizeofExpression.upc_blocksizeof); $EndBuild ./
      | 'upc_elemsizeof'  unary_expression
          /. $Build  consumeExpressionUnarySizeofOperator(IUPCASTUnarySizeofExpression.upc_elemsizeof); $EndBuild ./
      | 'upc_elemsizeof'  '(' type_id ')'
          /. $Build  consumeExpressionSizeofTypeId(IUPCASTUnarySizeofExpression.upc_elemsizeof); $EndBuild ./
      
      
-----------------------------------------------------------------------------------
-- Declarations
-----------------------------------------------------------------------------------


type_qualifier
    ::= shared_type_qualifier
      | reference_type_qualifier

-- causes ambiguities in parameter declarations, inherant in grammar
-- for example: int foo(int shared []);
-- does the [] bind to shared or is it shared with infinite block size array?
-- TODO: probably just resolved in the same way as dangling else

shared_type_qualifier
    ::= 'shared' layout_qualifier  -- don't consume anything, the presense of the 
                                   -- layout_qualifier will determine that 'shared' token was encountered
      | 'shared'   /. $Build  consumeToken();  $EndBuild ./

reference_type_qualifier
    ::= 'relaxed'  /. $Build  consumeToken();  $EndBuild ./
      | 'strict'   /. $Build  consumeToken();  $EndBuild ./

layout_qualifier
    ::= '[' constant_expression ']'
         /. $Build  consumeLayoutQualifier(true, false);  $EndBuild ./
      | '[' '*' ']'
         /. $Build  consumeLayoutQualifier(false, true);  $EndBuild ./
      | '[' ']'
         /. $Build  consumeLayoutQualifier(false, false);  $EndBuild ./



-----------------------------------------------------------------------------------
-- Statements
-----------------------------------------------------------------------------------

statement
     ::= synchronization_statement

synchronization_statement
     ::= 'upc_notify' expression ';'
           /. $Build  consumeStatementSynchronizationStatement(IUPCASTSynchronizationStatement.st_upc_notify, true); $EndBuild ./
       | 'upc_notify' ';'
           /. $Build  consumeStatementSynchronizationStatement(IUPCASTSynchronizationStatement.st_upc_notify, false); $EndBuild ./
       | 'upc_wait' expression ';'
           /. $Build  consumeStatementSynchronizationStatement(IUPCASTSynchronizationStatement.st_upc_wait, true); $EndBuild ./
       | 'upc_wait' ';'
           /. $Build  consumeStatementSynchronizationStatement(IUPCASTSynchronizationStatement.st_upc_wait, false); $EndBuild ./
       | 'upc_barrier' expression ';'
           /. $Build  consumeStatementSynchronizationStatement(IUPCASTSynchronizationStatement.st_upc_barrier, true); $EndBuild ./
       | 'upc_barrier' ';'
           /. $Build  consumeStatementSynchronizationStatement(IUPCASTSynchronizationStatement.st_upc_barrier, false); $EndBuild ./
       | 'upc_fence' ';'
           /. $Build  consumeStatementSynchronizationStatement(IUPCASTSynchronizationStatement.st_upc_fence, false); $EndBuild ./
       
       
iteration_statement
    ::= 'upc_forall' '(' expression ';' expression ';' expression ';' affinity ')' statement
            /. $Build  consumeStatementUPCForallLoop(true, true, true, true); $EndBuild ./
            
      | 'upc_forall' '(' expression ';' expression ';' expression ';'          ')' statement
            /. $Build  consumeStatementUPCForallLoop(true, true, true, false); $EndBuild ./
            
      | 'upc_forall' '(' expression ';' expression ';'            ';' affinity ')' statement
            /. $Build  consumeStatementUPCForallLoop(true, true, false, true); $EndBuild ./
            
      | 'upc_forall' '(' expression ';' expression ';'            ';'          ')' statement
            /. $Build  consumeStatementUPCForallLoop(true, true, false, false); $EndBuild ./
            
      | 'upc_forall' '(' expression ';'            ';' expression ';' affinity ')' statement
            /. $Build  consumeStatementUPCForallLoop(true, false, true, true); $EndBuild ./
            
      | 'upc_forall' '(' expression ';'            ';' expression ';'          ')' statement
            /. $Build  consumeStatementUPCForallLoop(true, false, true, false); $EndBuild ./
            
      | 'upc_forall' '(' expression ';'            ';'            ';' affinity ')' statement
            /. $Build  consumeStatementUPCForallLoop(true, false, false, true); $EndBuild ./
            
      | 'upc_forall' '(' expression ';'            ';'            ';'          ')' statement
            /. $Build  consumeStatementUPCForallLoop(true, false, false, false); $EndBuild ./
            
      | 'upc_forall' '('            ';' expression ';' expression ';' affinity ')' statement
            /. $Build  consumeStatementUPCForallLoop(false, true, true, true); $EndBuild ./
            
      | 'upc_forall' '('            ';' expression ';' expression ';'          ')' statement
            /. $Build  consumeStatementUPCForallLoop(false, true, true, false); $EndBuild ./
            
      | 'upc_forall' '('            ';' expression ';'            ';' affinity ')' statement
            /. $Build  consumeStatementUPCForallLoop(false, true, false, true); $EndBuild ./
            
      | 'upc_forall' '('            ';' expression ';'            ';'          ')' statement
            /. $Build  consumeStatementUPCForallLoop(false, true, false, false); $EndBuild ./
            
      | 'upc_forall' '('            ';'            ';' expression ';' affinity ')' statement
            /. $Build  consumeStatementUPCForallLoop(false, false, true, true); $EndBuild ./
            
      | 'upc_forall' '('            ';'            ';' expression ';'          ')' statement
            /. $Build  consumeStatementUPCForallLoop(false, false, true, false); $EndBuild ./
            
      | 'upc_forall' '('            ';'            ';'            ';' affinity ')' statement
            /. $Build  consumeStatementUPCForallLoop(false, false, false, true); $EndBuild ./
            
      | 'upc_forall' '('            ';'            ';'            ';'          ')' statement
            /. $Build  consumeStatementUPCForallLoop(false, false, false, false); $EndBuild ./
      
      | 'upc_forall' '(' declaration expression ';' expression ';' affinity ')' statement
            /. $Build  consumeStatementUPCForallLoop(true, true, true, true); $EndBuild ./
            
      | 'upc_forall' '(' declaration expression ';' expression ';'          ')' statement
            /. $Build  consumeStatementUPCForallLoop(true, true, true, false); $EndBuild ./
            
      | 'upc_forall' '(' declaration expression ';'            ';' affinity ')' statement
            /. $Build  consumeStatementUPCForallLoop(true, true, false, true); $EndBuild ./
            
      | 'upc_forall' '(' declaration expression ';'            ';'          ')' statement
            /. $Build  consumeStatementUPCForallLoop(true, true, false, false); $EndBuild ./
            
      | 'upc_forall' '(' declaration            ';' expression ';' affinity ')' statement
            /. $Build  consumeStatementUPCForallLoop(true, false, true, true); $EndBuild ./
            
      | 'upc_forall' '(' declaration            ';' expression ';'          ')' statement
            /. $Build  consumeStatementUPCForallLoop(true, false, true, false); $EndBuild ./
            
      | 'upc_forall' '(' declaration            ';'            ';' affinity ')' statement
            /. $Build  consumeStatementUPCForallLoop(true, false, false, true); $EndBuild ./
            
      | 'upc_forall' '(' declaration            ';'            ';'          ')' statement
            /. $Build  consumeStatementUPCForallLoop(true, false, false, false); $EndBuild ./

affinity
    ::= expression
      | 'continue'
          /. $Build  consumeToken();  $EndBuild ./

$End


