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
%options package=org.eclipse.cdt.internal.core.dom.lrparser.c99
%options template=LRSecondaryParserTemplate.g

-- All we need to do is import the main parser and redefine the start symbol.


$Import
	C99Grammar.g
$End

$Define
    $ast_class /. IASTExpression ./
$End

$Start
    expression_parser_start
$End

$Rules

	expression_parser_start
	    ::= expression
	      | ERROR_TOKEN
	          /. $Build  consumeEmpty();  $EndBuild ./
          
$End