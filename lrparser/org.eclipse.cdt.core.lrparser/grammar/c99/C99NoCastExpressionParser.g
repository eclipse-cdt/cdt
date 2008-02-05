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
%options package=org.eclipse.cdt.internal.core.dom.lrparser.c99
%options template=btParserTemplateD.g


$Define
	$sym_class /. C99NoCastExpressionParsersym ./
$End

$Import
	C99Grammar.g

$DropRules

cast_expression
    ::= '(' type_name ')' cast_expression

-- The following rule remains in the grammar:
--    cast_expression ::= unary_expression 

$End


$Start
    no_cast_start
$End



$Headers
/.
	public IASTExpression getParseResult() {
		return (IASTExpression) action.getSecondaryParseResult();
	}
./
$End


$Rules 

no_cast_start
    ::= expression
      | ERROR_TOKEN
          /. $Build  consumeExpressionProblem();  $EndBuild ./
          
$End