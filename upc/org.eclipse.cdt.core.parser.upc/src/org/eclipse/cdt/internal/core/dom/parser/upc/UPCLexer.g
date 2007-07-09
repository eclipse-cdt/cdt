-----------------------------------------------------------------------------------
-- Copyright (c) 2006, 2007 IBM Corporation and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     IBM Corporation - initial API and implementation
-----------------------------------------------------------------------------------

%Options la=1
%options package=org.eclipse.cdt.internal.core.dom.parser.upc
%options template=LexerTemplateD.g
%options export_terminals=("UPCLexerBaseexp.java", "TK_")
%options verbose
%Options list
%options single_productions


$Import
C99Lexer.g
$End


$Globals
/.
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.Token;
./
$End