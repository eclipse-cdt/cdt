-----------------------------------------------------------------------------------
-- Copyright (c) 2006, 2009 IBM Corporation and others.
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
%options package=org.eclipse.cdt.internal.core.dom.parser.upc
%options template=LRSecondaryParserTemplate.g


$Import
	C99SizeofExpressionParser.g
$End

$Import
    UPCGrammarExtensions.g
$DropRules

unary_expression
    ::= 'upc_localsizeof' '(' type_id ')'
      | 'upc_blocksizeof' '(' type_id ')'
      | 'upc_elemsizeof'  '(' type_id ')'
          
$End