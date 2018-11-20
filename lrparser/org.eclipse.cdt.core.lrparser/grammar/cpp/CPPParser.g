----------------------------------------------------------------------------------
-- Copyright (c) 2006, 2008 IBM Corporation and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     IBM Corporation - initial API and implementation
----------------------------------------------------------------------------------

%options la=2
%options package=org.eclipse.cdt.internal.core.dom.lrparser.cpp
%options template=LRParserTemplate.g


-- This file is needed because LPG won't allow redefinition of the 
-- start symbol, so CPPGrammar.g cannot define a start symbol.

$Import
	CPPGrammar.g
$End

$Start
    translation_unit
$End