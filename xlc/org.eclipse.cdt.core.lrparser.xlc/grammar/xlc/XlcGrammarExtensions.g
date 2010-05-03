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


--  These are additional rules that allow for parsing of GNU extensions.
--  This file is intended to be mixed-in with C99Parser.g or GPPParser.g


$Terminals

	vector
	pixel
	bool
	
	_Decimal32
	_Decimal64
	_Decimal128
	__static_assert
	
$End





$Rules



simple_type_specifier_token
    ::= '_Decimal32'
      | '_Decimal64'
      | '_Decimal128'


type_id
    ::= vector_type
          /. $Build  consumeTypeId(false);  $EndBuild ./
      | vector_type abstract_declarator
          /. $Build  consumeTypeId(true);  $EndBuild ./

vector_declaration
    ::= vector_type <openscope-ast> init_declarator_list ';'
	      /. $Build  consumeDeclarationSimple(true);  $EndBuild ./
	      
	      
vector_type
    ::= <openscope-ast> no_type_declaration_specifiers_opt 'vector' vector_type_specifier all_specifier_qualifier_list_opt
          /. $Build  consumeVectorTypeSpecifier();  $EndBuild ./
      
      
vector_type_specifier
    ::= vector_type_specifier_token
          /. $Build  consumeToken();  $EndBuild ./
      
vector_type_specifier_token
    ::= 'pixel'
      | 'float'
      | 'bool'
      | 'signed'
      | 'unsigned'
      | 'char'
      | 'short'
      | 'int'
      | 'long'



all_specifier_qualifiers
    ::= vector_type_specifier
      | no_type_declaration_specifiers
      
all_specifier_qualifier_list
    ::= all_specifier_qualifiers
      | all_specifier_qualifier_list all_specifier_qualifiers
      
all_specifier_qualifier_list_opt
    ::= all_specifier_qualifier_list
      | $empty

no_type_declaration_specifiers_opt
    ::= no_type_declaration_specifiers
      | $empty


$End

