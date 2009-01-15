-----------------------------------------------------------------------------------
-- Copyright (c) 2008 IBM Corporation and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     IBM Corporation - initial API and implementation
-----------------------------------------------------------------------------------

%options la=2
%options package=org.eclipse.cdt.internal.core.dom.lrparser.gpp
%options template=FixedBtParserTemplateD.g


-- For this to work the environment variable LPG_INCLUDE must be set up
-- to point at the directory where the CPPParser.g file is located.
$Import
	../cpp/CPPParser.g
	
$DropRules

-- will be replaced by extended asm syntax
asm_definition
    ::= 'asm' '(' 'stringlit' ')' ';'


$End


$Import
    ../gnu/GNUExtensions.g
$End


$Rules


asm_definition
    ::= extended_asm_declaration


no_type_declaration_specifier
   ::= attribute_or_decl_specifier

complete_declarator
   ::= attribute_or_decl_specifier_seq declarator
     | declarator attribute_or_decl_specifier_seq
     | attribute_or_decl_specifier_seq declarator attribute_or_decl_specifier_seq
      
member_declarator_complete
   ::= attribute_or_decl_specifier_seq member_declarator_complete
     | member_declarator_complete attribute_or_decl_specifier_seq
     | attribute_or_decl_specifier_seq member_declarator_complete attribute_or_decl_specifier_seq
      
enum_specifier_hook
    ::= attribute_or_decl_specifier_seq

composite_specifier_hook
    ::= attribute_or_decl_specifier_seq
    
class_name_suffix_hook
    ::= attribute_or_decl_specifier_seq

pointer_hook
    ::= attribute_or_decl_specifier_seq 

elaborated_specifier_hook
    ::= attribute_or_decl_specifier_seq

$End