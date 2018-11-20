-----------------------------------------------------------------------------------
-- Copyright (c) 2006, 2008 IBM Corporation and others.
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
%options package=org.eclipse.cdt.internal.core.dom.lrparser.cpp
%options template=LRSecondaryParserTemplate.g

-- This parser is a bit of a hack.

-- There are ambiguities between type_parameter and parameter_declaration
-- when parsing a template_parameter.

-- I believe the correct disambiguation is to simply favor type_parameter
-- over parameter_declaration.

-- I have tried to resolve this by refactoring the grammar file so that
-- the parser will give precedence to type_parameter, but I have failed.

-- So the hacky solution is to reparse the tokens as a type_parameter and if
-- it succeeds, throw away the paramter_declaration and use the type_parameter
-- in its place.


$Import
	CPPGrammar.g
$End

$Globals
/. 
	import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
./
$End

$Define
    $ast_class /. ICPPASTTemplateParameter ./
$End

$Start
    type_parameter_start
$End

$Rules

	type_parameter_start
	    ::= type_parameter
	      | ERROR_TOKEN
	          /. $Build  consumeEmpty();  $EndBuild ./
          
$End