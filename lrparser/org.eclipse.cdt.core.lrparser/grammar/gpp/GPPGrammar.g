-----------------------------------------------------------------------------------
-- Copyright (c) 2009 IBM Corporation and others.
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


$Terminals

   -- GCC allows these C99 keywords to be used in C++
   
   _Complex  _Imaginary
   
$End


-- For this to work the environment variable LPG_INCLUDE must be set up
-- to point at the directory where the CPPParser.g file is located.
$Import
	../cpp/CPPGrammar.g
	
$DropRules

-- will be replaced by extended asm syntax
asm_definition
    ::= 'asm' '(' 'stringlit' ')' ';'

-- need to replace the action associated with this rule with one that supports _Complex and _Imaginary
declaration_specifiers
    ::= <openscope-ast> simple_declaration_specifiers


$End


$Import
    ../gnu/GNUExtensions.g
$End

$Globals
/.
	import org.eclipse.cdt.core.dom.lrparser.action.gnu.GPPBuildASTParserAction;
	import org.eclipse.cdt.core.dom.lrparser.action.gnu.GPPSecondaryParserFactory;
./
$End

$Define

    $gnu_action_class /. GPPBuildASTParserAction ./
	$parser_factory_create_expression /. GPPSecondaryParserFactory.getDefault() ./

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
    
declarator
    ::= <openscope-ast> ptr_operator_seq attribute_or_decl_specifier_seq direct_declarator
          /. $Build  consumeDeclaratorWithPointer(true);  $EndBuild ./

elaborated_specifier_hook
    ::= attribute_or_decl_specifier_seq
    
    
simple_type_specifier
    ::= '_Complex'
          /. $Build  consumeToken(); $EndBuild ./
      | '_Imaginary'
          /. $Build  consumeToken(); $EndBuild ./

declaration_specifiers
    ::= <openscope-ast> simple_declaration_specifiers
          /. $BeginAction  gnuAction.consumeDeclarationSpecifiersSimple();  $EndAction ./
          
$End