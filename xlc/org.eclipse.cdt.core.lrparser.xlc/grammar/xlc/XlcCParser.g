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
%options package=org.eclipse.cdt.internal.core.lrparser.xlc.c
%options template=LRParserTemplate.g


$Import
	GCCGrammar.g
$End

$Import
	XlcGrammarExtensions.g
$End


$Globals
/.
	import org.eclipse.cdt.core.lrparser.xlc.action.XlcCBuildASTParserAction;
	import org.eclipse.cdt.core.dom.lrparser.action.gnu.GCCSecondaryParserFactory;
	import org.eclipse.cdt.internal.core.lrparser.xlc.ast.XlcCNodeFactory;
./
$End


$Define

	$build_action_class /. XlcCBuildASTParserAction ./
	$parser_factory_create_expression /. GCCSecondaryParserFactory.getDefault() ./
	$node_factory_create_expression /. XlcCNodeFactory.getDefault() ./
	
$End


$Start
    translation_unit
$End


$Rules 

declaration
    ::= vector_declaration
    
identifier_token
    ::= 'pixel'
      | 'vector'
      | 'bool'
      
declarator_id_name
    ::= 'pixel'
           /. $Build  consumeIdentifierName();  $EndBuild ./
      | 'vector'
           /. $Build  consumeIdentifierName();  $EndBuild ./
      | 'bool'
           /. $Build  consumeIdentifierName();  $EndBuild ./
    
$End