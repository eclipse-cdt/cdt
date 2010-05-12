-----------------------------------------------------------------------------------
-- Copyright (c) 2006, 2010 IBM Corporation and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     IBM Corporation - initial API and implementation
-----------------------------------------------------------------------------------

$Terminals
	
	-- Keywords
	
	auto      break     case      char      const       continue  default   do       
	double    else      enum      extern    float       for       goto      if        
	inline    int       long      register  restrict    return    short     signed     
	sizeof    static    struct    switch    typedef     union     unsigned  void 
	volatile  while     _Bool     _Complex  _Imaginary
	
	-- Literals
	
	integer  floating  charconst  stringlit
	
	-- identifiers
	
	identifier

	-- Special tokens used in content assist
	
	Completion
	EndOfCompletion
	
	-- Unrecognized token
	
	Invalid
	
    -- Punctuation (with aliases to make grammar more readable)

	LeftBracket      ::= '['
	LeftParen        ::= '('
	LeftBrace        ::= '{'
	Dot              ::= '.'
	Arrow            ::= '->'
	PlusPlus         ::= '++'
	MinusMinus       ::= '--'
	And              ::= '&'
	Star             ::= '*'
	Plus             ::= '+'
	Minus            ::= '-'
	Tilde            ::= '~'
	Bang             ::= '!'
	Slash            ::= '/'
	Percent          ::= '%'
	RightShift       ::= '>>'
	LeftShift        ::= '<<'
	LT               ::= '<'
	GT               ::= '>'
	LE               ::= '<='
	GE               ::= '>='
	EQ               ::= '=='
	NE               ::= '!='
	Caret            ::= '^'
	Or               ::= '|'
	AndAnd           ::= '&&'
	OrOr             ::= '||'
	Question         ::= '?'
	Colon            ::= ':'
	DotDotDot        ::= '...'
	Assign           ::= '='
	StarAssign       ::= '*='
	SlashAssign      ::= '/='
	PercentAssign    ::= '%='
	PlusAssign       ::= '+='
	MinusAssign      ::= '-='
	RightShiftAssign ::= '>>='
	LeftShiftAssign  ::= '<<='
	AndAssign        ::= '&='
	CaretAssign      ::= '^='
	OrAssign         ::= '|='
	Comma            ::= ','

    RightBracket     -- these four have special rules for content assist
    RightParen     
    RightBrace    
    SemiColon
    
      
$End


$Globals
/.	
	import org.eclipse.cdt.internal.core.dom.parser.c.CNodeFactory;
	import org.eclipse.cdt.core.dom.lrparser.action.c99.C99BuildASTParserAction;
	import org.eclipse.cdt.core.dom.lrparser.action.c99.C99SecondaryParserFactory;
./
$End

-- TODO move this code into a common template

$Define
	$build_action_class /. C99BuildASTParserAction ./
	$node_factory_create_expression /. CNodeFactory.getDefault() ./
	$parser_factory_create_expression /. C99SecondaryParserFactory.getDefault() ./
$End



$Rules

-------------------------------------------------------------------------------------------
-- AST scopes
-------------------------------------------------------------------------------------------


<openscope-ast> 
    ::= $empty
          /. $Build  openASTScope();  $EndBuild ./ 

<empty>
    ::= $empty 
          /. $Build  consumeEmpty();  $EndBuild ./ 
          
          
-------------------------------------------------------------------------------------------
-- Content assist
-------------------------------------------------------------------------------------------

-- The EndOfCompletion token is a special token that matches some punctuation.
-- These tokens allow the parse to complete successfully after a Completion token
-- is encountered.


']' ::=? 'RightBracket'
       | 'EndOfCompletion'
      
')' ::=? 'RightParen'
       | 'EndOfCompletion'
      
'}' ::=? 'RightBrace'
       | 'EndOfCompletion'
      
';' ::=? 'SemiColon'
       | 'EndOfCompletion'



-------------------------------------------------------------------------------------------
-- Expressions
-------------------------------------------------------------------------------------------

identifier_token
    ::= 'identifier'
      | 'Completion'
      

literal
    ::= 'integer'                    
          /. $Build  consumeExpressionLiteral(IASTLiteralExpression.lk_integer_constant); $EndBuild ./
      | 'floating'
          /. $Build  consumeExpressionLiteral(IASTLiteralExpression.lk_float_constant);  $EndBuild ./
      | 'charconst'                  
          /. $Build  consumeExpressionLiteral(IASTLiteralExpression.lk_char_constant);   $EndBuild ./
      | 'stringlit'
          /. $Build  consumeExpressionLiteral(IASTLiteralExpression.lk_string_literal);  $EndBuild ./


primary_expression 
    ::= literal 
      | primary_expression_id
          /. $Build  consumeExpressionID();  $EndBuild ./
      | '(' expression ')'         
          /. $Build  consumeExpressionBracketed();  $EndBuild ./


primary_expression_id
    ::= identifier_token

          
postfix_expression
    ::= primary_expression
      | postfix_expression '[' expression ']'
          /. $Build  consumeExpressionArraySubscript();  $EndBuild ./
      | postfix_expression '(' expression_list_opt ')'
          /. $Build  consumeExpressionFunctionCall();  $EndBuild ./
      | postfix_expression '.'  member_name
          /. $Build  consumeExpressionFieldReference(false);  $EndBuild ./
      | postfix_expression '->' member_name
          /. $Build  consumeExpressionFieldReference(true);  $EndBuild ./
      | postfix_expression '++'
         /. $Build   consumeExpressionUnaryOperator(IASTUnaryExpression.op_postFixIncr);  $EndBuild ./
      | postfix_expression '--'
          /. $Build  consumeExpressionUnaryOperator(IASTUnaryExpression.op_postFixDecr);  $EndBuild ./
      | '(' type_id ')' initializer_list
          /. $Build  consumeExpressionTypeIdInitializer();  $EndBuild ./
 
 
comma_opt
    ::= ',' | $empty


member_name
    ::= identifier_token


unary_expression
    ::= postfix_expression
      | '++' unary_expression
          /. $Build  consumeExpressionUnaryOperator(IASTUnaryExpression.op_prefixIncr);  $EndBuild ./
      | '--' unary_expression
          /. $Build  consumeExpressionUnaryOperator(IASTUnaryExpression.op_prefixDecr);  $EndBuild ./
      | '&' cast_expression
          /. $Build  consumeExpressionUnaryOperator(IASTUnaryExpression.op_amper);  $EndBuild ./
      | '*' cast_expression
          /. $Build  consumeExpressionUnaryOperator(IASTUnaryExpression.op_star);  $EndBuild ./
      | '+' cast_expression
          /. $Build  consumeExpressionUnaryOperator(IASTUnaryExpression.op_plus);  $EndBuild ./
      | '-' cast_expression
          /. $Build  consumeExpressionUnaryOperator(IASTUnaryExpression.op_minus);  $EndBuild ./
      | '~' cast_expression
          /. $Build  consumeExpressionUnaryOperator(IASTUnaryExpression.op_tilde);  $EndBuild ./
      | '!' cast_expression
          /. $Build  consumeExpressionUnaryOperator(IASTUnaryExpression.op_not);  $EndBuild ./
      | 'sizeof' unary_expression
          /. $Build  consumeExpressionUnaryOperator(IASTUnaryExpression.op_sizeof);  $EndBuild ./
      | 'sizeof' '(' type_id ')'
          /. $Build  consumeExpressionTypeId(IASTTypeIdExpression.op_sizeof);  $EndBuild ./  
          

cast_expression
    ::= unary_expression
      | '(' type_id ')' cast_expression
          /. $Build  consumeExpressionCast(IASTCastExpression.op_cast);  $EndBuild ./


multiplicative_expression
    ::= cast_expression
      | multiplicative_expression '*' cast_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_multiply);  $EndBuild ./
      | multiplicative_expression '/' cast_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_divide);  $EndBuild ./
      | multiplicative_expression '%' cast_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_modulo);  $EndBuild ./


additive_expression
    ::= multiplicative_expression
      | additive_expression '+' multiplicative_expression
      	  /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_plus);  $EndBuild ./
      | additive_expression '-' multiplicative_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_minus);  $EndBuild ./


shift_expression
    ::= additive_expression
      | shift_expression '<<' additive_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_shiftLeft);  $EndBuild ./
      | shift_expression '>>' additive_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_shiftRight);  $EndBuild ./


relational_expression
    ::= shift_expression
      | relational_expression '<' shift_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_lessThan);  $EndBuild ./
      | relational_expression '>' shift_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_greaterThan);  $EndBuild ./
      | relational_expression '<=' shift_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_lessEqual);  $EndBuild ./
      | relational_expression '>=' shift_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_greaterEqual);  $EndBuild ./


equality_expression
    ::= relational_expression
      | equality_expression '==' relational_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_equals);  $EndBuild ./
      | equality_expression '!=' relational_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_notequals);  $EndBuild ./


and_expression
    ::= equality_expression
      | and_expression '&' equality_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryAnd);  $EndBuild ./


exclusive_or_expression
    ::= and_expression
      | exclusive_or_expression '^' and_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryXor);  $EndBuild ./


inclusive_or_expression
    ::= exclusive_or_expression
      | inclusive_or_expression '|' exclusive_or_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryOr);  $EndBuild ./


logical_and_expression
    ::= inclusive_or_expression
      | logical_and_expression '&&' inclusive_or_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_logicalAnd);  $EndBuild ./


logical_or_expression
    ::= logical_and_expression
      | logical_or_expression '||' logical_and_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_logicalOr);  $EndBuild ./


conditional_expression
    ::= logical_or_expression
      | logical_or_expression '?' expression ':' assignment_expression
           /. $Build  consumeExpressionConditional();  $EndBuild ./


assignment_expression
    ::= conditional_expression
      | unary_expression '='   assignment_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_assign);  $EndBuild ./
      | unary_expression '*='  assignment_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_multiplyAssign);  $EndBuild ./
      | unary_expression '/='  assignment_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_divideAssign);  $EndBuild ./
      | unary_expression '%='  assignment_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_moduloAssign);  $EndBuild ./
      | unary_expression '+='  assignment_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_plusAssign);  $EndBuild ./
      | unary_expression '-='  assignment_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_minusAssign);  $EndBuild ./
      | unary_expression '<<=' assignment_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_shiftLeftAssign);  $EndBuild ./
      | unary_expression '>>=' assignment_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_shiftRightAssign);  $EndBuild ./
      | unary_expression '&='  assignment_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryAndAssign);  $EndBuild ./
      | unary_expression '^='  assignment_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryXorAssign);  $EndBuild ./
      | unary_expression '|='  assignment_expression
          /. $Build  consumeExpressionBinaryOperator(IASTBinaryExpression.op_binaryOrAssign);  $EndBuild ./

	
-- special rule to avoid conflict between problem statements and problem expressions
expression_in_statement
    ::= expression_list


expression
    ::= expression_list
    

expression_list
    ::= <openscope-ast> expression_list_actual
          /. $Build  consumeExpressionList();  $EndBuild ./


expression_list_opt
   ::= expression_list
     | $empty
          /. $Build  consumeEmpty();  $EndBuild ./

          
expression_list_actual
    ::= assignment_expression
      | expression_list_actual ',' assignment_expression 


constant_expression
    ::= conditional_expression
    

-------------------------------------------------------------------------------------------
-- Statements
-------------------------------------------------------------------------------------------
      

      
statement
    ::= labeled_statement
      | compound_statement
      | expression_statement
      | selection_statement
      | iteration_statement
      | jump_statement
      | ERROR_TOKEN
          /. $Build  consumeStatementProblem();  $EndBuild ./


labeled_statement
    ::= identifier_token ':' statement
    	  /. $Build  consumeStatementLabeled();  $EndBuild ./
      | 'case' constant_expression ':' statement
          /. $Build  consumeStatementCase();  $EndBuild ./
      | 'default' ':' statement
          /. $Build  consumeStatementDefault();  $EndBuild ./
         
         
compound_statement
    ::= '{' '}' 
          /. $Build  consumeStatementCompoundStatement(false);  $EndBuild ./
      | '{' <openscope-ast> block_item_list '}'
          /. $Build  consumeStatementCompoundStatement(true);  $EndBuild ./
         
         
block_item_list
    ::= block_item
      | block_item_list block_item
      
      
block_item
    ::= statement
      | declaration
          /. $Build  consumeStatementDeclarationWithDisambiguation();  $EndBuild ./
         
         
expression_statement
    ::= ';'
          /. $Build  consumeStatementNull();  $EndBuild ./
      | expression_in_statement ';'
          /. $Build  consumeStatementExpression();  $EndBuild ./
         
         
selection_statement
    ::= 'if' '(' expression ')' statement
          /. $Build  consumeStatementIf(false);  $EndBuild ./
      | 'if' '(' expression ')' statement 'else' statement
          /. $Build  consumeStatementIf(true);  $EndBuild ./
      | 'switch' '(' expression ')' statement
          /. $Build  consumeStatementSwitch();  $EndBuild ./
  
  
expression_opt
    ::= expression
      | $empty
          /. $Build  consumeEmpty();  $EndBuild ./


iteration_statement
    ::= 'do' statement 'while' '(' expression ')' ';'
          /. $Build  consumeStatementDoLoop(true);  $EndBuild ./
	  | 'do' statement
          /. $Build  consumeStatementDoLoop(false);  $EndBuild ./
      | 'while' '(' expression ')' statement
          /. $Build  consumeStatementWhileLoop();  $EndBuild ./
      | 'for' '(' expression_opt ';' expression_opt ';' expression_opt ')' statement
          /. $Build  consumeStatementForLoop();  $EndBuild ./
      | 'for' '(' declaration expression_opt ';' expression_opt ')' statement
          /. $Build  consumeStatementForLoop();  $EndBuild ./
          

jump_statement
    ::= 'goto' identifier_token ';'
          /. $Build  consumeStatementGoto();  $EndBuild ./
      | 'continue' ';'
          /. $Build  consumeStatementContinue();  $EndBuild ./
      | 'break' ';'
          /. $Build  consumeStatementBreak();  $EndBuild ./
      | 'return' ';'
          /. $Build  consumeStatementReturn(false);  $EndBuild ./
      | 'return' expression ';'
          /. $Build  consumeStatementReturn(true);  $EndBuild ./
    
    
    
-------------------------------------------------------------------------------------------
-- Declarations
-------------------------------------------------------------------------------------------

      
      
declaration 
    ::= declaration_specifiers  ';'
          /. $Build  consumeDeclarationSimple(false);  $EndBuild ./
	  | declaration_specifiers <openscope-ast> init_declarator_list ';'
	      /. $Build  consumeDeclarationSimple(true);  $EndBuild ./
         

declaration_specifiers
    ::= <openscope-ast> simple_declaration_specifiers
          /. $Build  consumeDeclarationSpecifiersSimple();  $EndBuild ./
      | <openscope-ast> struct_or_union_declaration_specifiers
          /. $Build  consumeDeclarationSpecifiersStructUnionEnum();  $EndBuild ./
      | <openscope-ast> elaborated_declaration_specifiers
          /. $Build  consumeDeclarationSpecifiersStructUnionEnum();  $EndBuild ./
      | <openscope-ast> enum_declaration_specifiers
          /. $Build  consumeDeclarationSpecifiersStructUnionEnum();  $EndBuild ./
      | <openscope-ast> typdef_name_declaration_specifiers
          /. $Build  consumeDeclarationSpecifiersTypedefName();  $EndBuild ./


no_type_declaration_specifier
    ::= storage_class_specifier
      | type_qualifier
      | function_specifier
    
    
no_type_declaration_specifiers
    ::= no_type_declaration_specifier
      | no_type_declaration_specifiers no_type_declaration_specifier
  
      
simple_declaration_specifiers
    ::= simple_type_specifier
      | no_type_declaration_specifiers simple_type_specifier
      | simple_declaration_specifiers simple_type_specifier
      | simple_declaration_specifiers no_type_declaration_specifier
      
      
struct_or_union_declaration_specifiers
    ::= struct_or_union_specifier
      | no_type_declaration_specifiers struct_or_union_specifier
      | struct_or_union_declaration_specifiers no_type_declaration_specifier
      
      
elaborated_declaration_specifiers
    ::= elaborated_specifier
      | no_type_declaration_specifiers elaborated_specifier
      | elaborated_declaration_specifiers no_type_declaration_specifier


enum_declaration_specifiers
    ::= enum_specifier
      | no_type_declaration_specifiers  enum_specifier
      | enum_declaration_specifiers no_type_declaration_specifier


typdef_name_declaration_specifiers
    ::= type_name_specifier
      | no_type_declaration_specifiers  type_name_specifier
      | typdef_name_declaration_specifiers no_type_declaration_specifier
    
    
init_declarator_list
    ::= init_declarator
      | init_declarator_list ',' init_declarator
					  
					  
init_declarator 
    ::= complete_declarator
      | complete_declarator '=' initializer
          /. $Build  consumeDeclaratorWithInitializer(true);  $EndBuild ./


complete_declarator
    ::= declarator


storage_class_specifier
    ::= storage_class_specifier_token
          /. $Build  consumeToken();  $EndBuild ./


storage_class_specifier_token 
    ::= 'typedef'
      | 'extern'
      | 'static'
      | 'auto'
      | 'register'


simple_type_specifier
    ::= simple_type_specifier_token
          /. $Build  consumeToken();  $EndBuild ./
				
simple_type_specifier_token
    ::= 'void'        
      | 'char'
      | 'short'
      | 'int'
      | 'long'
      | 'float'
      | 'double'
      | 'signed'
      | 'unsigned'
      | '_Bool'
      | '_Complex'
      | '_Imaginary'
		
		
type_name_specifier
    ::= identifier_token
          /. $Build  consumeToken();  $EndBuild ./
       

      
struct_or_union_specifier
    ::= struct_or_union struct_or_union_specifier_hook '{' <openscope-ast> struct_declaration_list_opt '}'
          /. $Build  consumeTypeSpecifierComposite(false); $EndBuild ./           
      | struct_or_union struct_or_union_specifier_hook identifier_token struct_or_union_specifier_suffix_hook '{' <openscope-ast> struct_declaration_list_opt '}'
          /. $Build  consumeTypeSpecifierComposite(true); $EndBuild ./ 
          
struct_or_union_specifier_hook
    ::= $empty

struct_or_union_specifier_suffix_hook
    ::= $empty

struct_or_union
    ::= 'struct'
      | 'union'
      
      
elaborated_specifier          
    ::= 'struct' elaborated_specifier_hook identifier_token
          /. $Build  consumeTypeSpecifierElaborated(IASTCompositeTypeSpecifier.k_struct); $EndBuild ./
      | 'union'  elaborated_specifier_hook identifier_token
          /. $Build  consumeTypeSpecifierElaborated(IASTCompositeTypeSpecifier.k_union); $EndBuild ./
      | 'enum' elaborated_specifier_hook identifier_token
          /. $Build  consumeTypeSpecifierElaborated(IASTElaboratedTypeSpecifier.k_enum); $EndBuild ./
          
elaborated_specifier_hook
    ::= $empty
    
    
struct_declaration_list_opt
    ::= struct_declaration_list
      | $empty

struct_declaration_list
    ::= struct_declaration
      | struct_declaration_list struct_declaration
      

struct_declaration
    ::= specifier_qualifier_list <openscope-ast> struct_declarator_list ';' -- regular declarators plus bit fields
          /. $Build  consumeStructDeclaration(true);  $EndBuild ./
      | specifier_qualifier_list ';'
          /. $Build  consumeStructDeclaration(false);  $EndBuild ./
      | ERROR_TOKEN
          /. $Build  consumeDeclarationProblem();  $EndBuild ./


-- just reuse declaration_specifiers, makes grammar a bit more lenient but thats OK
specifier_qualifier_list
    ::= declaration_specifiers
           

struct_declarator_list
    ::= complete_struct_declarator
      | struct_declarator_list ',' complete_struct_declarator


complete_struct_declarator
    ::= struct_declarator
    

struct_declarator
    ::= declarator
      | ':' constant_expression  
          /. $Build  consumeBitField(false);  $EndBuild ./
      | declarator ':' constant_expression		
          /. $Build  consumeBitField(true);  $EndBuild ./
		      
            
enum_specifier
    ::= 'enum' enum_specifier_hook '{' <openscope-ast> enumerator_list_opt comma_opt '}'
          /. $Build  consumeTypeSpecifierEnumeration(false); $EndBuild ./
      | 'enum' enum_specifier_hook identifier_token '{' <openscope-ast> enumerator_list_opt comma_opt '}'
          /. $Build  consumeTypeSpecifierEnumeration(true); $EndBuild ./
      
enum_specifier_hook
    ::= $empty


enumerator_list_opt
    ::= enumerator_list
      | $empty


enumerator_list
    ::= enumerator
      | enumerator_list ',' enumerator
      
      
enumerator
    ::= identifier_token
          /. $Build  consumeEnumerator(false); $EndBuild ./
      | identifier_token '=' constant_expression
          /. $Build  consumeEnumerator(true); $EndBuild ./
      
      
type_qualifier
    ::= type_qualifier_token
          /. $Build  consumeToken();  $EndBuild ./
     
     
type_qualifier_token
    ::= 'const'
      | 'restrict'
      | 'volatile'


function_specifier
    ::= 'inline'    
          /. $Build  consumeToken();  $EndBuild ./


declarator
    ::= direct_declarator
      | <openscope-ast> pointer_seq direct_declarator
          /. $Build  consumeDeclaratorWithPointer(true);  $EndBuild ./


direct_declarator
    ::= array_direct_declarator
      | function_prototype_direct_declarator
      | basic_direct_declarator
      | knr_direct_declarator
      

basic_direct_declarator
    ::= declarator_id_name
          /. $Build  consumeDirectDeclaratorIdentifier();  $EndBuild ./
      | '(' declarator ')'
          /. $Build  consumeDirectDeclaratorBracketed();  $EndBuild ./


declarator_id_name
    ::= 'identifier'
          /. $Build  consumeIdentifierName();  $EndBuild ./
         
         
array_direct_declarator
    ::= basic_direct_declarator array_modifier
          /. $Build  consumeDirectDeclaratorArrayDeclarator(true);  $EndBuild ./
      | array_direct_declarator array_modifier
          /. $Build  consumeDirectDeclaratorArrayDeclarator(true);  $EndBuild ./


function_prototype_direct_declarator
    ::= function_direct_declarator
         

function_direct_declarator
    ::= basic_direct_declarator '(' <openscope-ast> parameter_type_list ')'
          /. $Build  consumeDirectDeclaratorFunctionDeclarator(true, true);  $EndBuild ./
      | basic_direct_declarator '(' ')'
          /. $Build  consumeDirectDeclaratorFunctionDeclarator(true, false);  $EndBuild ./


function_declarator
    ::= function_direct_declarator
      | <openscope-ast> pointer_seq function_direct_declarator
          /. $Build  consumeDeclaratorWithPointer(true);  $EndBuild ./


-- This is a hack because the parser cannot tell the difference between 
-- plain identifiers and types. Because of this an identifier_list would
-- always be parsed as a parameter_type_list instead. In a KnR funciton
-- definition we can use the extra list of declarators to disambiguate.
-- This rule should be merged back into direct_declarator if type info is
-- added to the parser. 

knr_direct_declarator 
    ::= basic_direct_declarator '(' <openscope-ast> identifier_list ')'
          /. $Build  consumeDirectDeclaratorFunctionDeclaratorKnR();  $EndBuild ./


knr_function_declarator
    ::= knr_direct_declarator
      | <openscope-ast> pointer_seq knr_direct_declarator
          /. $Build  consumeDeclaratorWithPointer(true);  $EndBuild ./


identifier_list
    ::= 'identifier'
          /. $Build  consumeIdentifierKnR();  $EndBuild ./
      | identifier_list ',' 'identifier'
          /. $Build  consumeIdentifierKnR();  $EndBuild ./
                    

array_modifier 
    ::= '[' ']'
          /. $Build  consumeDirectDeclaratorArrayModifier(false);  $EndBuild ./
      | '[' <openscope-ast> array_modifier_type_qualifiers ']'
         /. $Build  consumeDirectDeclaratorModifiedArrayModifier(false, false, true, false);  $EndBuild ./
      | '[' assignment_expression ']'
          /. $Build  consumeDirectDeclaratorArrayModifier(true);  $EndBuild ./
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


pointer_seq
    ::= pointer_hook '*' pointer_hook
          /. $Build  consumePointer();  $EndBuild ./
      | pointer_seq pointer_hook '*' pointer_hook
          /. $Build  consumePointer();  $EndBuild ./
      | pointer_hook '*' pointer_hook <openscope-ast> type_qualifier_list
          /. $Build  consumePointerTypeQualifierList();  $EndBuild ./
      | pointer_seq pointer_hook '*' pointer_hook <openscope-ast> type_qualifier_list
          /. $Build  consumePointerTypeQualifierList();  $EndBuild ./

pointer_hook
    ::= $empty


type_qualifier_list
    ::= type_qualifier
      | type_qualifier_list type_qualifier


parameter_type_list
    ::= parameter_list
          /. $Build  consumeEmpty();  $EndBuild ./
      | parameter_list ',' '...'
          /. $Build  consumePlaceHolder();  $EndBuild ./
      | '...'  -- not spec
          /. $Build  consumePlaceHolder();  $EndBuild ./


parameter_list
    ::= parameter_declaration
      | parameter_list ',' parameter_declaration


parameter_declaration
    ::= declaration_specifiers complete_parameter_declarator
          /. $Build  consumeParameterDeclaration();  $EndBuild ./
      | declaration_specifiers
          /. $Build  consumeParameterDeclarationWithoutDeclarator();  $EndBuild ./


complete_parameter_declarator
    ::= declarator
      | abstract_declarator



-- only used in expressions, eg) sizeof, casts etc...
type_id
    ::= specifier_qualifier_list
          /. $Build  consumeTypeId(false);  $EndBuild ./
      | specifier_qualifier_list abstract_declarator
          /. $Build  consumeTypeId(true);  $EndBuild ./


abstract_declarator  -- a declarator that does not include an identifier
    ::= direct_abstract_declarator
      | <openscope-ast> pointer_seq
          /. $Build  consumeDeclaratorWithPointer(false);  $EndBuild ./
      | <openscope-ast> pointer_seq direct_abstract_declarator
          /. $Build  consumeDeclaratorWithPointer(true);  $EndBuild ./


direct_abstract_declarator
    ::= basic_direct_abstract_declarator
      | array_direct_abstract_declarator
      | function_direct_abstract_declarator


basic_direct_abstract_declarator
    ::= '(' abstract_declarator ')'
          /. $Build  consumeDirectDeclaratorBracketed();  $EndBuild ./
          
          
array_direct_abstract_declarator
    ::= array_modifier
          /. $Build  consumeDirectDeclaratorArrayDeclarator(false);  $EndBuild ./
      | array_direct_abstract_declarator array_modifier
          /. $Build  consumeDirectDeclaratorArrayDeclarator(true);  $EndBuild ./
      | basic_direct_abstract_declarator array_modifier
          /. $Build  consumeDirectDeclaratorArrayDeclarator(true);  $EndBuild ./    
          
          
function_direct_abstract_declarator
    ::= '(' ')'
          /. $Build  consumeDirectDeclaratorFunctionDeclarator(false, false);  $EndBuild  ./
      | basic_direct_abstract_declarator '(' ')'
          /. $Build  consumeDirectDeclaratorFunctionDeclarator(true, false);  $EndBuild ./
      | '(' <openscope-ast> parameter_type_list ')'
          /. $Build  consumeDirectDeclaratorFunctionDeclarator(false, true);  $EndBuild ./
      | basic_direct_abstract_declarator '(' <openscope-ast> parameter_type_list ')'
          /. $Build  consumeDirectDeclaratorFunctionDeclarator(true, true);  $EndBuild ./


initializer
    ::= assignment_expression
          /. $Build  consumeInitializer();  $EndBuild ./
      | initializer_list  
          
          
initializer_list
    ::= start_initializer_list '{' <openscope-ast> initializer_seq comma_opt '}' end_initializer_list
          /. $Build  consumeInitializerList();  $EndBuild ./
      | '{' <openscope-ast> '}'
          /. $Build  consumeInitializerList();  $EndBuild ./


start_initializer_list
    ::= $empty
          /. $Build  initializerListStart(); $EndBuild ./
          
end_initializer_list
    ::= $empty
          /. $Build  initializerListEnd(); $EndBuild ./
          
          
initializer_seq
    ::= initializer
      | designated_initializer
      | initializer_seq ',' initializer
      | initializer_seq ',' designated_initializer
            

designated_initializer
    ::= <openscope-ast> designation '=' initializer
          /. $Build  consumeInitializerDesignated();  $EndBuild ./
          
    
designation
    ::= designator_list


designator_list
    ::= designator_base
      | designator_list designator


designator_base
    ::= '[' constant_expression ']'
          /. $Build  consumeDesignatorArray();  $EndBuild ./
      | '.' identifier_token		
          /. $Build  consumeDesignatorField();  $EndBuild ./

designator
    ::= '[' constant_expression ']'
         /. $Build  consumeDesignatorArray();  $EndBuild ./
      | '.' identifier_token		
         /. $Build  consumeDesignatorField();  $EndBuild ./
		
		
-------------------------------------------------------------------------------------------
-- External Definitions
-------------------------------------------------------------------------------------------

translation_unit
    ::= external_declaration_list
          /. $Build  consumeTranslationUnit();  $EndBuild  ./
      | $empty
          /. $Build  consumeTranslationUnit();  $EndBuild ./
          
          
external_declaration_list
    ::= external_declaration
      | external_declaration_list external_declaration


external_declaration
    ::= function_definition
      | declaration
      | ';'
          /. $Build  consumeDeclarationEmpty();  $EndBuild ./
      | ERROR_TOKEN
          /. $Build  consumeDeclarationProblem();  $EndBuild ./


-- Used by KnR
declaration_list
    ::= declaration
      | declaration_list declaration

      
-- The extra <openscope-ast> nonterminal before declarator in this rule is only there
-- to avoid a shift/reduce error with the rule for declaration. 
-- The symbol table scoped is opened in the rule for function_direct_declarator
function_definition
    ::= normal_function_definition
         -- this rule is here as a special case (its not C99 spec) just to support implicit int in function definitions
      | <openscope-ast>  function_declarator function_body
          /. $Build  consumeFunctionDefinition(false);  $EndBuild ./
      | declaration_specifiers <openscope-ast>  knr_function_declarator <openscope-ast> declaration_list compound_statement
          /. $Build  consumeFunctionDefinitionKnR();  $EndBuild ./

normal_function_definition
    ::= declaration_specifiers <openscope-ast>  function_declarator function_body
          /. $Build  consumeFunctionDefinition(true);  $EndBuild ./
          
-- same syntax as compound_statement but a symbol table scope isn't opened
function_body
    ::= '{' '}' 
          /. $Build  consumeStatementCompoundStatement(false);  $EndBuild ./
      | '{' <openscope-ast> block_item_list '}'
          /. $Build  consumeStatementCompoundStatement(true);  $EndBuild ./
          
$End
