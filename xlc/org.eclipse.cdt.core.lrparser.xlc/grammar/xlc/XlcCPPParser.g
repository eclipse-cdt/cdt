-----------------------------------------------------------------------------------
-- Copyright (c) 2009, 2010 IBM Corporation and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     IBM Corporation - initial API and implementation
-----------------------------------------------------------------------------------

%options la=2
%options package=org.eclipse.cdt.internal.core.lrparser.xlc.cpp
%options template=LRParserTemplate.g


$Import
	GPPGrammar.g
$End

$Import
	XlcGrammarExtensions.g
$End 

$Globals
/.
	import org.eclipse.cdt.core.lrparser.xlc.action.XlcCPPBuildASTParserAction;
	import org.eclipse.cdt.core.dom.lrparser.action.gnu.GPPSecondaryParserFactory;
	import org.eclipse.cdt.internal.core.lrparser.xlc.ast.XlcCPPNodeFactory;
./
$End

$Define

	$build_action_class /. XlcCPPBuildASTParserAction ./
	$parser_factory_create_expression /. GPPSecondaryParserFactory.getDefault() ./
	$node_factory_create_expression /. XlcCPPNodeFactory.getDefault() ./
	
$End



$Terminals

	restrict
	
$End


$Start
    translation_unit
$End


$Rules 

	
cv_qualifier
    ::= 'restrict'

block_declaration
    ::= vector_declaration
      | static_assert_declaration
    

identifier_token
    ::= 'vector' 
      | 'pixel'
    
    
specifier_qualifier
    ::= 'typedef'
          /. $Build  consumeToken(); $EndBuild ./
          
          
array_modifier 
    ::= '[' <openscope-ast> array_modifier_type_qualifiers ']'
          /. $Build  consumeDirectDeclaratorModifiedArrayModifier(false, false, true, false);  $EndBuild ./
      | '[' <openscope-ast> array_modifier_type_qualifiers assignment_expression ']'
          /. $Build  consumeDirectDeclaratorModifiedArrayModifier(false, false, true, true);  $EndBuild ./
      | '[' 'static' assignment_expression ']'
          /. $Build  consumeDirectDeclaratorModifiedArrayModifier(true, false, false, true);  $EndBuild ./
      | '[' 'static' <openscope-ast> array_modifier_type_qualifiers assignment_expression ']'
          /. $Build  consumeDirectDeclaratorModifiedArrayModifier(true, false, true, true);  $EndBuild ./
      | '[' <openscope-ast> array_modifier_type_qualifiers 'static' assignment_expression ']'
          /. $Build  consumeDirectDeclaratorModifiedArrayModifier(true, false, true, true);  $EndBuild ./
      | '[' '*' ']'
          /. $Build  consumeDirectDeclaratorModifiedArrayModifier(false, true, false, false);  $EndBuild ./
      | '[' <openscope-ast> array_modifier_type_qualifiers '*' ']'
          /. $Build  consumeDirectDeclaratorModifiedArrayModifier(false, true, true, false);  $EndBuild ./
          
          
array_modifier_type_qualifiers
    ::= type_qualifier_list 
    
type_qualifier_list
    ::= cv_qualifier         
      | type_qualifier_list cv_qualifier

member_declaration
    ::= static_assert_declaration
    
static_assert_declaration 
    ::= '__static_assert'  '(' expression ',' literal ')' ';'
        /. $Build  consumeCPPASTStaticAssertDeclaration();  $EndBuild ./

          
$End