-----------------------------------------------------------------------------------
-- Copyright (c) 2009 IBM Corporation and others.
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


-- For this to work the environment variable LPG_INCLUDE must be set up
-- to point at the directory where the C99Parser.g file is located.
$Import
	../c99/C99Grammar.g
$End

$Import
    ../gnu/GNUExtensions.g
$End


-- Tokens used by GCC but not part of the C99 spec
$Terminals

    asm
    
$End


$Globals
/.
	import org.eclipse.cdt.core.dom.lrparser.action.gnu.GCCBuildASTParserAction;
	import org.eclipse.cdt.core.dom.lrparser.action.gnu.GCCSecondaryParserFactory;
./
$End

$Define

    $build_action_class  /. GCCBuildASTParserAction ./
	$parser_factory_create_expression /. GCCSecondaryParserFactory.getDefault() ./
	
$End


$Rules


declaration
    ::= extended_asm_declaration
 
 
no_type_declaration_specifier
    ::= attribute_or_decl_specifier

complete_declarator
    ::= attribute_or_decl_specifier_seq declarator
      | declarator attribute_or_decl_specifier_seq
      | attribute_or_decl_specifier_seq declarator attribute_or_decl_specifier_seq

complete_struct_declarator
    ::= attribute_or_decl_specifier_seq struct_declarator
      | struct_declarator attribute_or_decl_specifier_seq
      | attribute_or_decl_specifier_seq struct_declarator attribute_or_decl_specifier_seq
    
enum_specifier_hook
    ::= attribute_or_decl_specifier_seq

struct_or_union_specifier_hook
    ::= attribute_or_decl_specifier_seq
    
struct_or_union_specifier_suffix_hook
    ::= attribute_or_decl_specifier_seq

pointer_hook
    ::= attribute_or_decl_specifier_seq 

elaborated_specifier_hook
    ::= attribute_or_decl_specifier_seq





-- GCC extensions to designated initializers

designator_base
    ::= field_name_designator
      | array_range_designator

field_name_designator
    ::= identifier_token ':'		
          /. $Build  consumeDesignatorFieldGCC();  $EndBuild ./
          
array_range_designator
    ::=  '[' constant_expression '...' constant_expression ']'
          /. $Build  consumeDesignatorArrayRange();  $EndBuild ./

designated_initializer
    ::= <openscope-ast> field_name_designator initializer
          /. $Build  consumeInitializerDesignated();  $EndBuild ./
          


-- Nested functions

block_item
    ::= normal_function_definition
          /. $Build  consumeStatementDeclaration();  $EndBuild ./
          
$End