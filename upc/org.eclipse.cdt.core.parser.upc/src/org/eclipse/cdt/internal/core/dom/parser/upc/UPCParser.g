-----------------------------------------------------------------------------------
-- Copyright (c) 2006, 2007 IBM Corporation and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     IBM Corporation - initial API and implementation
-----------------------------------------------------------------------------------

%options la=2
%options package=org.eclipse.cdt.internal.core.dom.parser.upc
%options template=btParserTemplateD.g
%options import_terminals=D:\workspaces\cdt-head2\org.eclipse.cdt.core.parser.c99\src\org\eclipse\cdt\internal\core\dom\parser\c99\C99Lexer.g


-- Unified Parallel C (UPC) is an extension of C99
$Import
D:\workspaces\cdt-head2\org.eclipse.cdt.core.parser.c99\src\org\eclipse\cdt\internal\core\dom\parser\c99\C99Parser.g
$End

$Globals
/.
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTKeywordExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSizeofExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSynchronizationStatement;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Lexer;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parsersym;
import org.eclipse.cdt.core.dom.parser.upc.UPCKeywordMap;
import org.eclipse.cdt.core.dom.parser.upc.UPCParserAction;
./
$End

$Define	
	$action_class /. UPCParserAction ./
	$keyword_map_class /. UPCKeywordMap ./
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

-- also need to be a pre-defined macro
-- THREADS and UPC_MAX_BLOCKSIZE are macros, but I want to be able to recognize
-- them in the AST, how do I do this? (getRawSignature() to get the source code and then string compare to THREADS?)
-- just use the paths and symbols dialog to add them, good for testing they can be added later by a upc toolchain

-- map them to integers in the UPCKeywordMap
-- override consumeExpressionConstant(IASTLiteralExpression.lk_integer_constant), call toString() onthe token 
-- and check if its THREADS or UPC_MAX_BLOCKSIZE and create appropriate AST node

-- but then the value would be lost, its no good, they need to be builtin macros

constant
    ::= 'MYTHREAD'
            /.$ba  consumeKeywordExpression(IUPCASTKeywordExpression.kw_mythread); $ea./
      | 'THREADS'
            /.$ba  consumeKeywordExpression(IUPCASTKeywordExpression.kw_threads); $ea./
      | 'UPC_MAX_BLOCKSIZE'
            /.$ba  consumeKeywordExpression(IUPCASTKeywordExpression.kw_upc_max_block_size); $ea./
            

-- causes ambiguities because of no type information, solution is SGLR
unary_expression
    ::= 'upc_localsizeof' unary_expression
          /.$ba  consumeExpressionUpcSizeofOperator(IUPCASTSizeofExpression.op_upc_localsizeof); $ea./
      | 'upc_localsizeof' '(' type_name ')'
          /.$ba  consumeExpressionUpcSizeofTypeName(IUPCASTSizeofExpression.op_upc_localsizeof); $ea./
      | 'upc_blocksizeof' unary_expression
          /.$ba  consumeExpressionUpcSizeofOperator(IUPCASTSizeofExpression.op_upc_blocksizeof); $ea./
      | 'upc_blocksizeof' '(' type_name ')'
          /.$ba  consumeExpressionUpcSizeofTypeName(IUPCASTSizeofExpression.op_upc_blocksizeof); $ea./
      | 'upc_elemsizeof'  unary_expression
          /.$ba  consumeExpressionUpcSizeofOperator(IUPCASTSizeofExpression.op_upc_elemsizeof); $ea./
      | 'upc_elemsizeof'  '(' type_name ')'
          /.$ba  consumeExpressionUpcSizeofTypeName(IUPCASTSizeofExpression.op_upc_elemsizeof); $ea./
      
      
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
      | 'shared'   /.$ba  consumeToken();  $ea./

reference_type_qualifier
    ::= 'relaxed'  /.$ba  consumeToken();  $ea./
      | 'strict'   /.$ba  consumeToken();  $ea./

layout_qualifier
    ::= '[' constant_expression ']'
         /.$ba  consumeLayoutQualifier(true, false);  $ea./
      | '[' '*' ']'
         /.$ba  consumeLayoutQualifier(false, true);  $ea./
      | '[' ']'
         /.$ba  consumeLayoutQualifier(false, false);  $ea./



-----------------------------------------------------------------------------------
-- Statements
-----------------------------------------------------------------------------------

statement
     ::= synchronization_statement

synchronization_statement
     ::= 'upc_notify' expression ';'
           /.$ba  consumeStatementSynchronizationStatement(IUPCASTSynchronizationStatement.st_upc_notify, true); $ea./
       | 'upc_notify' ';'
           /.$ba  consumeStatementSynchronizationStatement(IUPCASTSynchronizationStatement.st_upc_notify, false); $ea./
       | 'upc_wait' expression ';'
           /.$ba  consumeStatementSynchronizationStatement(IUPCASTSynchronizationStatement.st_upc_wait, true); $ea./
       | 'upc_wait' ';'
           /.$ba  consumeStatementSynchronizationStatement(IUPCASTSynchronizationStatement.st_upc_wait, false); $ea./
       | 'upc_barrier' expression ';'
           /.$ba  consumeStatementSynchronizationStatement(IUPCASTSynchronizationStatement.st_upc_barrier, true); $ea./
       | 'upc_barrier' ';'
           /.$ba  consumeStatementSynchronizationStatement(IUPCASTSynchronizationStatement.st_upc_barrier, false); $ea./
       | 'upc_fence' ';'
           /.$ba  consumeStatementSynchronizationStatement(IUPCASTSynchronizationStatement.st_upc_fence, false); $ea./
       
       
iteration_statement
    ::= 'upc_forall' '(' expression ';' expression ';' expression ';' affinity ')' statement
            /.$ba  consumeStatementUPCForallLoop(true, true, true, true); $ea./
            
      | 'upc_forall' '(' expression ';' expression ';' expression ';'          ')' statement
            /.$ba  consumeStatementUPCForallLoop(true, true, true, false); $ea./
            
      | 'upc_forall' '(' expression ';' expression ';'            ';' affinity ')' statement
            /.$ba  consumeStatementUPCForallLoop(true, true, false, true); $ea./
            
      | 'upc_forall' '(' expression ';' expression ';'            ';'          ')' statement
            /.$ba  consumeStatementUPCForallLoop(true, true, false, false); $ea./
            
      | 'upc_forall' '(' expression ';'            ';' expression ';' affinity ')' statement
            /.$ba  consumeStatementUPCForallLoop(true, false, true, true); $ea./
            
      | 'upc_forall' '(' expression ';'            ';' expression ';'          ')' statement
            /.$ba  consumeStatementUPCForallLoop(true, false, true, false); $ea./
            
      | 'upc_forall' '(' expression ';'            ';'            ';' affinity ')' statement
            /.$ba  consumeStatementUPCForallLoop(true, false, false, true); $ea./
            
      | 'upc_forall' '(' expression ';'            ';'            ';'          ')' statement
            /.$ba  consumeStatementUPCForallLoop(true, false, false, false); $ea./
            
      | 'upc_forall' '('            ';' expression ';' expression ';' affinity ')' statement
            /.$ba  consumeStatementUPCForallLoop(false, true, true, true); $ea./
            
      | 'upc_forall' '('            ';' expression ';' expression ';'          ')' statement
            /.$ba  consumeStatementUPCForallLoop(false, true, true, false); $ea./
            
      | 'upc_forall' '('            ';' expression ';'            ';' affinity ')' statement
            /.$ba  consumeStatementUPCForallLoop(false, true, false, true); $ea./
            
      | 'upc_forall' '('            ';' expression ';'            ';'          ')' statement
            /.$ba  consumeStatementUPCForallLoop(false, true, false, false); $ea./
            
      | 'upc_forall' '('            ';'            ';' expression ';' affinity ')' statement
            /.$ba  consumeStatementUPCForallLoop(false, false, true, true); $ea./
            
      | 'upc_forall' '('            ';'            ';' expression ';'          ')' statement
            /.$ba  consumeStatementUPCForallLoop(false, false, true, false); $ea./
            
      | 'upc_forall' '('            ';'            ';'            ';' affinity ')' statement
            /.$ba  consumeStatementUPCForallLoop(false, false, false, true); $ea./
            
      | 'upc_forall' '('            ';'            ';'            ';'          ')' statement
            /.$ba  consumeStatementUPCForallLoop(false, false, false, false); $ea./
      
      | 'upc_forall' '(' declaration expression ';' expression ';' affinity ')' statement
            /.$ba  consumeStatementUPCForallLoop(true, true, true, true); $ea./
            
      | 'upc_forall' '(' declaration expression ';' expression ';'          ')' statement
            /.$ba  consumeStatementUPCForallLoop(true, true, true, false); $ea./
            
      | 'upc_forall' '(' declaration expression ';'            ';' affinity ')' statement
            /.$ba  consumeStatementUPCForallLoop(true, true, false, true); $ea./
            
      | 'upc_forall' '(' declaration expression ';'            ';'          ')' statement
            /.$ba  consumeStatementUPCForallLoop(true, true, false, false); $ea./
            
      | 'upc_forall' '(' declaration            ';' expression ';' affinity ')' statement
            /.$ba  consumeStatementUPCForallLoop(true, false, true, true); $ea./
            
      | 'upc_forall' '(' declaration            ';' expression ';'          ')' statement
            /.$ba  consumeStatementUPCForallLoop(true, false, true, false); $ea./
            
      | 'upc_forall' '(' declaration            ';'            ';' affinity ')' statement
            /.$ba  consumeStatementUPCForallLoop(true, false, false, true); $ea./
            
      | 'upc_forall' '(' declaration            ';'            ';'          ')' statement
            /.$ba  consumeStatementUPCForallLoop(true, false, false, false); $ea./

affinity
    ::= expression
      | 'continue'
          /.$ba  consumeToken();  $ea./

$End















