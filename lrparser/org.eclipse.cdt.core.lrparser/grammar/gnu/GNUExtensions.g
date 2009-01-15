-----------------------------------------------------------------------------------
-- Copyright (c) 2006, 2008 IBM Corporation and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
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
	MAX
	MIN
	__attribute__
	__declspec
	
$End



$Headers
/.
	private $gnu_action_class gnuAction;
./
$End

$Define

	$gnu_action_class /.  ./

	$action_initializations /.
	
		gnuAction = new $gnu_action_class ($node_factory_create_expression, this, tu, astStack);
		gnuAction.setParserOptions(options);
		
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


$End

