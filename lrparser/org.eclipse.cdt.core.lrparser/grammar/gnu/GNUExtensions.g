-----------------------------------------------------------------------------------
-- Copyright (c) 2008, 2009 IBM Corporation and others.
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


--  These are additional rules that allow for parsing of GNU extensions.
--  This file is intended to be mixed-in with C99Parser.g or GPPParser.g


$Terminals
	-- additional GCC only tokens as defined in IGCCToken

	typeof
	__alignof__
	__attribute__
	__declspec
	MAX
	MIN
	
	MAX ::= '>?'
	MIN ::= '<?'
	
$End


$Headers
/.
	private $gnu_action_class gnuAction;
./
$End


$Globals
/.
	import org.eclipse.cdt.core.dom.lrparser.action.gnu.GNUBuildASTParserAction;
./
$End


$Define

	$gnu_action_class /. GNUBuildASTParserAction ./  -- overridable

	$action_initializations /.
	
		gnuAction = new $gnu_action_class (this, astStack, $node_factory_create_expression);
		gnuAction.setParserProperties(properties);
	./
	
$End

$Rules

------------------------------------------------------------------------------------
-- Support for __attribute__ and __declspec
------------------------------------------------------------------------------------

attribute_or_decl_specifier
    ::= attribute_specifier
      | decl_specifier
      | asm_label

attribute_or_decl_specifier_seq
    ::= attribute_or_decl_specifier
      | attribute_or_decl_specifier_seq attribute_or_decl_specifier



attribute_specifier
    ::= '__attribute__' '(' '(' attribute_list ')' ')'
      | '__attribute__' '(' ')'
    
attribute_list
    ::= attribute
      | attribute_list ',' attribute

attribute
    ::= word
      | word '(' attribute_parameter_list ')'
      | $empty
      
word
    ::= 'identifier'
      | 'const'

attribute_parameter_list
    ::= attribute_parameter
      | attribute_parameter_list ',' attribute_parameter

attribute_parameter
    ::= assignment_expression
          /. $Build  consumeIgnore(); $EndBuild ./
      | $empty



decl_specifier
    ::= '__declspec' '(' extended_decl_modifier_seq ')'
      | '__declspec' '(' ')'    
      
extended_decl_modifier_seq
    ::= extended_decl_modifier
      | extended_decl_modifier_seq extended_decl_modifier
    
extended_decl_modifier
    ::= 'identifier'
      | 'identifier' '(' ')'
      | 'identifier' '(' 'identifier' ')'
      | 'identifier' '(' 'stringlit' ')'


------------------------------------------------------------------------------------
-- Other stuff
------------------------------------------------------------------------------------

asm_label
    ::= 'asm' '(' 'stringlit' ')'


extended_asm_declaration
    ::= 'asm' volatile_opt '(' extended_asm_param_seq ')' ';'
           /. $BeginAction  gnuAction.consumeDeclarationASM(); $EndAction ./

volatile_opt ::= 'volatile' | $empty

extended_asm_param_seq
    ::= extended_asm_param_with_operand
      | extended_asm_param_seq ':' extended_asm_param_with_operand

extended_asm_param_with_operand
    ::= extended_asm_param
      | extended_asm_param ',' extended_asm_param
      | $empty
      
extended_asm_param
    ::= 'stringlit'
      | 'stringlit' '(' 'identifier' ')'
      | 'stringlit' '(' '*' 'identifier' ')'



unary_expression
    ::= '__alignof__' unary_expression
          /. $Build  consumeExpressionUnaryOperator(IASTUnaryExpression.op_alignOf);  $EndBuild ./
      | '__alignof__' '(' type_id ')'
          /. $Build  consumeExpressionTypeId(IASTTypeIdExpression.op_alignof);  $EndBuild ./  
      | 'typeof' unary_expression
          /. $Build  consumeExpressionUnaryOperator(IASTUnaryExpression.op_typeof);  $EndBuild ./
      | 'typeof' '(' type_id ')'
          /. $Build  consumeExpressionTypeId(IASTTypeIdExpression.op_typeof);  $EndBuild ./  
          
          
relational_expression
    ::= relational_expression '>?' shift_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_max);  $EndBuild ./
      | relational_expression '<?' shift_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_min);  $EndBuild ./
          
          
conditional_expression
    ::= logical_or_expression '?' <empty> ':' assignment_expression
           /. $Build  consumeExpressionConditional();  $EndBuild ./


primary_expression
    ::= '(' compound_statement ')'
           /. $BeginAction  gnuAction.consumeCompoundStatementExpression();  $EndAction ./
           
           
labeled_statement
    ::= 'case' case_range_expression ':' statement
          /. $Build  consumeStatementCase();  $EndBuild ./
          
          
case_range_expression
    ::= constant_expression '...' constant_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_assign);  $EndBuild ./
          
          
typeof_declaration_specifiers
    ::= typeof_type_specifier
      | no_type_declaration_specifiers  typeof_type_specifier
      | typeof_declaration_specifiers no_type_declaration_specifier


typeof_type_specifier
      ::= 'typeof' unary_expression
            /. $Build  consumeExpressionUnaryOperator(IASTUnaryExpression.op_typeof);  $EndBuild ./
        | 'typeof' '(' type_id ')'
            /. $Build  consumeExpressionTypeId(IASTTypeIdExpression.op_typeof);  $EndBuild ./  
        

declaration_specifiers
    ::= <openscope-ast> typeof_declaration_specifiers
          /. $Build  consumeDeclarationSpecifiersTypeof();  $EndBuild ./
        
$End

