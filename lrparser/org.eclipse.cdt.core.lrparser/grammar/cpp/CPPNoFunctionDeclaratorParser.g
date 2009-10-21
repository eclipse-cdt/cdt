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
%options package=org.eclipse.cdt.internal.core.dom.lrparser.cpp
%options template=LRSecondaryParserTemplate.g

$Import
	CPPGrammar.g
$DropRules

	direct_declarator
        ::= function_direct_declarator
        
    init_declarator_complete
    	::= init_declarator

$End

$Define
    $ast_class /. IASTDeclarator ./
$End

$Start
    no_function_declarator_start
$End

$Rules 

	no_function_declarator_start
	    ::= init_declarator_complete
	      | ERROR_TOKEN
	          /. $Build  consumeEmpty();  $EndBuild ./
	
	-- redeclare this rule with no semantic action, prevents recursion
	init_declarator_complete
    	::= init_declarator
              
$End