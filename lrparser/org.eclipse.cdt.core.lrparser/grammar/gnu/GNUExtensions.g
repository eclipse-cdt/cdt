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
	__attribute__
	__declspec
$End


$Rules

------------------------------------------------------------------------------------
-- Support for __attribute__ and __declspec
------------------------------------------------------------------------------------

attribute_or_decl_specifier
    ::= attribute_specifier
      | decl_specifier

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
    ::= '__declspec' '(' extended_decl_modifier_seq_opt ')'  
    
extended_decl_modifier_seq_opt
    ::= extended_decl_modifier_seq
      | $empty
      
extended_decl_modifier_seq
    ::= extended_decl_modifier
      | extended_decl_modifier_seq extended_decl_modifier
    
extended_decl_modifier
    ::= 'identifier'
      | 'identifier' '(' ')'
      | 'identifier' '(' 'identifier' ')'

$End