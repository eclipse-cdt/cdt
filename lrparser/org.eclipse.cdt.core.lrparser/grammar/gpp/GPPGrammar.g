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

%options la=2
%options package=org.eclipse.cdt.internal.core.dom.lrparser.gpp
%options template=FixedBtParserTemplateD.g


$Terminals

   -- GCC allows these keywords to be used in C++
   
   _Complex  
   _Imaginary
   restrict
   
$End


-- For this to work the environment variable LPG_INCLUDE must be set up
-- to point at the directory where the CPPParser.g file is located.
$Import
	../cpp/CPPGrammar.g
	
$DropRules

-- will be replaced by extended asm syntax
asm_definition
    ::= 'asm' '(' 'stringlit' ')' ';'

$End


$Import
    ../gnu/GNUExtensions.g
$End

$Globals
/.
	import org.eclipse.cdt.core.dom.lrparser.action.gnu.GPPBuildASTParserAction;
	import org.eclipse.cdt.core.dom.lrparser.action.gnu.GPPSecondaryParserFactory;
	import org.eclipse.cdt.core.dom.ast.gnu.cpp.*;
./
$End

$Define

	$build_action_class /. GPPBuildASTParserAction ./
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
    
namespace_definition_hook
    ::= attribute_or_decl_specifier_seq
    
    
simple_type_specifier
    ::= '_Complex'
          /. $Build  consumeToken(); $EndBuild ./
      | '_Imaginary'
          /. $Build  consumeToken(); $EndBuild ./

cv_qualifier
    ::= 'restrict'
          /. $Build  consumeToken(); $EndBuild ./
          
          
explicit_instantiation
    ::= 'extern' 'template' declaration
            /. $Build  consumeTemplateExplicitInstantiationGCC(IGPPASTExplicitTemplateInstantiation.ti_extern);  $EndBuild ./
      | 'static' 'template' declaration
            /. $Build  consumeTemplateExplicitInstantiationGCC(IGPPASTExplicitTemplateInstantiation.ti_static);  $EndBuild ./
      | 'inline' 'template' declaration    
            /. $Build  consumeTemplateExplicitInstantiationGCC(IGPPASTExplicitTemplateInstantiation.ti_inline);  $EndBuild ./
            
            
postfix_expression
    ::= '(' type_id ')' initializer_list
          /. $Build  consumeExpressionTypeIdInitializer();  $EndBuild ./

          
$End