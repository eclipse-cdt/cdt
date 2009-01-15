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

%options la=2
%options package=org.eclipse.cdt.internal.core.dom.lrparser.gcc
%options template=FixedBtParserTemplateD.g


-- For this to work the environment variable LPG_INCLUDE must be set up
-- to point at the directory where the C99Parser.g file is located.
$Import
	../c99/C99Parser.g
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
./
$End

$Define

    $gnu_action_class  /. GCCBuildASTParserAction ./

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







designator_base
    ::= identifier_token ':'		
          /. $BeginAction  gnuAction.consumeDesignatorField();  $EndAction ./
      | '[' constant_expression '...' constant_expression ']'
          /. $BeginAction  gnuAction.consumeDesignatorArray();  $EndAction ./







$End