----------------------------------------------------------------------------------
-- Copyright (c) 2006, 2010 IBM Corporation and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl_v10.html
--
-- Contributors:
--     IBM Corporation - initial API and implementation
----------------------------------------------------------------------------------


$Terminals
	
	-- Keywords

	asm  auto  bool  break  case  catch  char  class  
	const  const_cast  continue  default  delete  do    
	double  dynamic_cast  else  enum  explicit  export  
	extern  false  float  for  friend  goto  if  inline  
	int  long  mutable  namespace  new  operator  private
	protected  public  register  reinterpret_cast  return
	short  signed  sizeof  static  static_cast  struct
	switch  template  this  throw   try  true  typedef
	typeid  typename  union  unsigned  using  virtual
	void  volatile  wchar_t  while
	
	-- Literals
	
	integer  floating  charconst  stringlit
	
	-- Identifiers
	
	identifier 

	-- Special tokens used in content assist
	
	Completion
	EndOfCompletion
	
	-- Unrecognized token, not actually used anywhere in the grammar, always leads to syntax error
	
	Invalid
	
    -- Punctuation (with aliases to make grammar more readable)

	LeftBracket      ::= '['
	LeftParen        ::= '('
	Dot              ::= '.'
	DotStar          ::= '.*'
	Arrow            ::= '->'
	ArrowStar        ::= '->*'
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
	ColonColon       ::= '::'
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
    LeftBrace
    
$End


$Globals
/.	
    import org.eclipse.cdt.core.dom.ast.cpp.*;
	import org.eclipse.cdt.core.dom.lrparser.action.cpp.CPPNodeFactory;
	import org.eclipse.cdt.core.dom.lrparser.action.cpp.CPPBuildASTParserAction;
	import org.eclipse.cdt.core.dom.lrparser.action.cpp.CPPSecondaryParserFactory;
./
$End


$Define
	$build_action_class /. CPPBuildASTParserAction ./
	$node_factory_create_expression /. CPPNodeFactory.getDefault() ./
	$parser_factory_create_expression /. CPPSecondaryParserFactory.getDefault() ./
$End


$Rules

------------------------------------------------------------------------------------------
-- AST scoping
------------------------------------------------------------------------------------------


<openscope-ast> 
    ::= $empty
          /. $Build  openASTScope();  $EndBuild ./ 

<empty>
    ::= $empty 
          /. $Build  consumeEmpty();  $EndBuild ./ 
           
------------------------------------------------------------------------------------------
-- Content assist
------------------------------------------------------------------------------------------

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

'{' ::=? 'LeftBrace'
       | 'EndOfCompletion'



------------------------------------------------------------------------------------------
-- Basic Concepts
------------------------------------------------------------------------------------------

-- The extra external declaration rules are there just so that ERROR_TOKEN can be
-- caught at the top level.

translation_unit
    ::= declaration_seq_opt
          /. $Build  consumeTranslationUnit(); $EndBuild ./
      

------------------------------------------------------------------------------------------
-- Expressions
------------------------------------------------------------------------------------------

identifier_token
    ::= 'identifier'
      | 'Completion'
      

literal
    ::= 'integer'
           /. $Build  consumeExpressionLiteral(ICPPASTLiteralExpression.lk_integer_constant); $EndBuild ./
      | 'floating'
           /. $Build  consumeExpressionLiteral(ICPPASTLiteralExpression.lk_float_constant); $EndBuild ./
      | 'charconst'
           /. $Build  consumeExpressionLiteral(ICPPASTLiteralExpression.lk_char_constant); $EndBuild ./
      | 'stringlit'
           /. $Build  consumeExpressionLiteral(ICPPASTLiteralExpression.lk_string_literal); $EndBuild ./  
      | 'true'
           /. $Build  consumeExpressionLiteral(ICPPASTLiteralExpression.lk_true); $EndBuild ./
      | 'false'
           /. $Build  consumeExpressionLiteral(ICPPASTLiteralExpression.lk_false); $EndBuild ./
      | 'this'
           /. $Build  consumeExpressionLiteral(ICPPASTLiteralExpression.lk_this); $EndBuild ./
           
           
primary_expression
    ::= literal
      | '(' expression ')'
           /. $Build  consumeExpressionBracketed();  $EndBuild ./
      | id_expression
      
      
id_expression 
    ::= qualified_or_unqualified_name
           /. $Build  consumeExpressionName();  $EndBuild ./
      
      
-- Pushes an IASTName on the stack, if you want an id expression then wrap the name
qualified_or_unqualified_name
    ::= unqualified_id_name
      | qualified_id_name


unqualified_id_name
    ::= identifier_name
      | operator_function_id_name
      | conversion_function_id_name
      | template_id_name
      | '~' identifier_token
          /. $Build  consumeDestructorName();  $EndBuild ./
      | '~' template_id_name
          /. $Build  consumeDestructorNameTemplateId();  $EndBuild ./
     -- | '~' class_name
    


-- wrap an identifier in a name node
identifier_name
    ::= identifier_token
           /. $Build  consumeIdentifierName();  $EndBuild ./
           

template_opt
    ::= 'template'
          /. $Build  consumePlaceHolder();  $EndBuild ./
      | $empty
          /. $Build  consumeEmpty();  $EndBuild ./


-- the ::=? is necessary for example 8.2.1 in the C++ spec to parse correctly
dcolon_opt
    ::=? '::'
          /. $Build  consumeToken();  $EndBuild ./  -- need the actual token to compute offsets
      | $empty
          /. $Build  consumeEmpty();  $EndBuild ./



qualified_id_name
    ::= dcolon_opt nested_name_specifier template_opt unqualified_id_name
          /. $Build  consumeQualifiedId(true);  $EndBuild ./
      | '::' unqualified_id_name
          /. $Build  consumeGlobalQualifiedId();  $EndBuild ./
      
      --| '::' identifier_name
      --    /. $Build  consumeGlobalQualifiedId();  $EndBuild ./
      --| '::' operator_function_id_name
      --    /. $Build  consumeGlobalQualifiedId();  $EndBuild ./
      --| '::' template_id_name
      --    /. $Build  consumeGlobalQualifiedId();  $EndBuild ./



--unqualified_id_with_template_name
--    ::= template_opt unqualified_id_name
--          /. $Build  consumeNameWithTemplateKeyword();  $EndBuild ./
    
    
-- puts a list of names in reverse order on the stack
-- always ends with ::
-- this is the part of a qualified id that comes before the last ::
-- but does not include a :: at the front
nested_name_specifier
    ::= class_or_namespace_name '::' nested_name_specifier_with_template
          /. $Build  consumeNestedNameSpecifier(true);  $EndBuild ./
      | class_or_namespace_name '::' 
          /. $Build  consumeNestedNameSpecifier(false);  $EndBuild ./
      

nested_name_specifier_with_template
    ::= class_or_namespace_name_with_template '::' nested_name_specifier_with_template
          /. $Build  consumeNestedNameSpecifier(true);  $EndBuild ./
      | class_or_namespace_name_with_template '::' 
          /. $Build  consumeNestedNameSpecifier(false);  $EndBuild ./

      
class_or_namespace_name_with_template
    ::= template_opt class_or_namespace_name
          /. $Build  consumeNameWithTemplateKeyword();  $EndBuild ./
      
      
      
nested_name_specifier_opt
    ::= nested_name_specifier
      | $empty
           /. $Build  consumeNestedNameSpecifierEmpty();  $EndBuild ./

      
class_or_namespace_name -- just identifiers
    ::= class_name
      --| namespace_name -- namespace_name name can only be an identifier token, which is already accepted by class_name


postfix_expression
    ::= primary_expression
      | postfix_expression '[' expression ']'
          /. $Build  consumeExpressionArraySubscript();  $EndBuild ./
      | postfix_expression '(' expression_list_opt ')'    
          /. $Build  consumeExpressionFunctionCall();  $EndBuild ./
      | simple_type_specifier '(' expression_list_opt ')' -- explicit type conversion operator
          /. $Build  consumeExpressionSimpleTypeConstructor(); $EndBuild ./
      | 'typename' dcolon_opt nested_name_specifier <empty>  identifier_name '(' expression_list_opt ')'
          /. $Build  consumeExpressionTypeName(); $EndBuild ./
      | 'typename' dcolon_opt nested_name_specifier template_opt template_id_name '(' expression_list_opt ')'
          /. $Build  consumeExpressionTypeName(); $EndBuild ./
      | postfix_expression '.' qualified_or_unqualified_name
          /. $Build  consumeExpressionFieldReference(false, false);  $EndBuild ./
      | postfix_expression '->' qualified_or_unqualified_name
          /. $Build  consumeExpressionFieldReference(true, false);  $EndBuild ./
      | postfix_expression '.' 'template' qualified_or_unqualified_name
          /. $Build  consumeExpressionFieldReference(false, true);  $EndBuild ./
      | postfix_expression '->' 'template' qualified_or_unqualified_name
          /. $Build  consumeExpressionFieldReference(true, true);  $EndBuild ./
      | postfix_expression '.' pseudo_destructor_name
          /. $Build  consumeExpressionFieldReference(false, false);  $EndBuild ./
      | postfix_expression '->' pseudo_destructor_name
          /. $Build  consumeExpressionFieldReference(true, false);  $EndBuild ./
      | postfix_expression '++'
          /. $Build  consumeExpressionUnaryOperator(IASTUnaryExpression.op_postFixIncr);  $EndBuild ./
      | postfix_expression '--'
          /. $Build  consumeExpressionUnaryOperator(IASTUnaryExpression.op_postFixDecr);  $EndBuild ./
      | 'dynamic_cast' '<' type_id '>' '(' expression ')'
          /. $Build  consumeExpressionCast(ICPPASTCastExpression.op_dynamic_cast);  $EndBuild ./
      | 'static_cast' '<' type_id '>' '(' expression ')'
          /. $Build  consumeExpressionCast(ICPPASTCastExpression.op_static_cast);  $EndBuild ./
      | 'reinterpret_cast' '<' type_id '>' '(' expression ')'
          /. $Build  consumeExpressionCast(ICPPASTCastExpression.op_reinterpret_cast);  $EndBuild ./
      | 'const_cast' '<' type_id '>' '(' expression ')'
          /. $Build  consumeExpressionCast(ICPPASTCastExpression.op_const_cast);  $EndBuild ./
      | 'typeid' '(' expression ')'
          /. $Build  consumeExpressionUnaryOperator(ICPPASTUnaryExpression.op_typeid);  $EndBuild ./
      | 'typeid' '(' type_id ')'
          /. $Build  consumeExpressionTypeId(ICPPASTTypeIdExpression.op_typeid); $EndBuild ./

      
      
-- just names
-- Don't even know if I really need this rule, the DOM parser just parses qualified_or_unqualified_name
-- instead of pseudo_destructor_name. But the difference is I have different
-- token types, so maybe I do need this rule.
pseudo_destructor_name
    ::= dcolon_opt nested_name_specifier_opt type_name '::' destructor_type_name
          /. $Build  consumePsudoDestructorName(true);  $EndBuild ./
      | dcolon_opt nested_name_specifier 'template' template_id_name '::' destructor_type_name
          /. $Build  consumePsudoDestructorName(true);  $EndBuild ./
      | dcolon_opt nested_name_specifier_opt destructor_type_name
          /. $Build  consumePsudoDestructorName(false);  $EndBuild ./


destructor_type_name
    ::= '~' identifier_token
          /. $Build  consumeDestructorName();  $EndBuild ./
      | '~' template_id_name
          /. $Build  consumeDestructorNameTemplateId();  $EndBuild ./
          
          
--destructor_type_name
--    ::= '~' type_name
--          /. $Build  consumeDestructorName();  $EndBuild ./
     
          

unary_expression
    ::= postfix_expression
      | new_expression
      | delete_expression
      | '++' cast_expression
          /. $Build  consumeExpressionUnaryOperator(IASTUnaryExpression.op_prefixIncr);  $EndBuild ./
      | '--' cast_expression
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
          /. $Build  consumeExpressionTypeId(ICPPASTTypeIdExpression.op_sizeof); $EndBuild ./


new_expression
    ::= dcolon_opt 'new' new_placement_opt new_type_id <openscope-ast> new_array_expressions_opt new_initializer_opt
          /. $Build  consumeExpressionNew(true);  $EndBuild ./
      | dcolon_opt 'new' new_placement_opt '(' type_id ')' <openscope-ast> new_array_expressions_opt new_initializer_opt
          /. $Build  consumeExpressionNew(false);  $EndBuild ./


new_placement_opt
    ::= '(' expression_list ')'
      | $empty
          /. $Build  consumeEmpty();  $EndBuild ./


new_type_id
    ::= type_specifier_seq
          /. $Build  consumeTypeId(false);  $EndBuild ./
      | type_specifier_seq new_declarator
          /. $Build  consumeTypeId(true);  $EndBuild ./


new_declarator -- pointer operators are part of the type id, held in an empty declarator
    ::= <openscope-ast> new_pointer_operators
          /. $Build  consumeNewDeclarator(); $EndBuild ./


new_pointer_operators  -- presumably this will not need an action as ptr_operator will have one
    ::= ptr_operator 
      | ptr_operator new_pointer_operators
      

new_array_expressions 
    ::= '[' expression ']'
      | new_array_expressions '[' constant_expression ']'


new_array_expressions_opt
    ::= new_array_expressions
      | $empty


new_initializer
    ::= '(' expression_list_opt ')'  -- even if the parens are there we get null in the AST
          /. $Build  consumeNewInitializer();  $EndBuild ./
    
    
new_initializer_opt
    ::= new_initializer
      | $empty
           /. $Build  consumeEmpty();  $EndBuild ./


delete_expression
    ::= dcolon_opt 'delete' cast_expression
          /. $Build  consumeExpressionDelete(false);  $EndBuild ./
      | dcolon_opt 'delete' '[' ']' cast_expression
          /. $Build  consumeExpressionDelete(true);  $EndBuild ./


cast_expression
     ::= unary_expression
       | '(' type_id ')' cast_expression
           /. $Build  consumeExpressionCast(ICPPASTCastExpression.op_cast);  $EndBuild ./


pm_expression
    ::= cast_expression
      | pm_expression '.*' cast_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_pmdot);  $EndBuild ./
      | pm_expression '->*' cast_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_pmarrow);  $EndBuild ./
          

multiplicative_expression
    ::= pm_expression
      | multiplicative_expression '*' pm_expression
         /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_multiply);  $EndBuild ./
      | multiplicative_expression '/' pm_expression
         /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_divide);  $EndBuild ./
      | multiplicative_expression '%' pm_expression
         /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_modulo);  $EndBuild ./


additive_expression
    ::= multiplicative_expression
      | additive_expression '+' multiplicative_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_plus);  $EndBuild ./
      | additive_expression '-' multiplicative_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_minus);  $EndBuild ./
      
      
shift_expression
    ::= additive_expression
      | shift_expression '<<' additive_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_shiftLeft);  $EndBuild ./
      | shift_expression '>>' additive_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_shiftRight);  $EndBuild ./
      
      
relational_expression
    ::= shift_expression
      | relational_expression '<' shift_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_lessThan);  $EndBuild ./
      | relational_expression '>' shift_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_greaterThan);  $EndBuild ./
      | relational_expression '<=' shift_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_lessEqual);  $EndBuild ./
      | relational_expression '>=' shift_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_greaterEqual);  $EndBuild ./


equality_expression
    ::= relational_expression
      | equality_expression '==' relational_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_equals);  $EndBuild ./
      | equality_expression '!=' relational_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_notequals);  $EndBuild ./


and_expression
    ::= equality_expression
      | and_expression '&' equality_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_binaryAnd);  $EndBuild ./


exclusive_or_expression
    ::= and_expression
      | exclusive_or_expression '^' and_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_binaryXor);  $EndBuild ./


inclusive_or_expression
    ::= exclusive_or_expression
      | inclusive_or_expression '|' exclusive_or_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_binaryOr);  $EndBuild ./


logical_and_expression
    ::= inclusive_or_expression
      | logical_and_expression '&&' inclusive_or_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_logicalAnd);  $EndBuild ./


logical_or_expression
    ::= logical_and_expression
      | logical_or_expression '||' logical_and_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_logicalOr);  $EndBuild ./
      

conditional_expression
    ::= logical_or_expression
      | logical_or_expression '?' expression ':' assignment_expression
           /. $Build  consumeExpressionConditional();  $EndBuild ./


throw_expression
    ::= 'throw'
          /. $Build  consumeExpressionThrow(false);  $EndBuild ./
      | 'throw' assignment_expression
          /. $Build  consumeExpressionThrow(true);  $EndBuild ./


assignment_expression
    ::= conditional_expression
      | throw_expression
      | logical_or_expression '=' assignment_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_assign);  $EndBuild ./
      | logical_or_expression '*=' assignment_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_multiplyAssign);  $EndBuild ./
      | logical_or_expression '/=' assignment_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_divideAssign);  $EndBuild ./
      | logical_or_expression '%=' assignment_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_moduloAssign);  $EndBuild ./
      | logical_or_expression '+=' assignment_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_plusAssign);  $EndBuild ./
      | logical_or_expression '-=' assignment_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_minusAssign);  $EndBuild ./
      | logical_or_expression '>>=' assignment_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_shiftRightAssign);  $EndBuild ./
      | logical_or_expression '<<=' assignment_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_shiftLeftAssign);  $EndBuild ./
      | logical_or_expression '&=' assignment_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_binaryAndAssign);  $EndBuild ./
      | logical_or_expression '^=' assignment_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_binaryXorAssign);  $EndBuild ./
      | logical_or_expression '|=' assignment_expression
          /. $Build  consumeExpressionBinaryOperator(ICPPASTBinaryExpression.op_binaryOrAssign);  $EndBuild ./


expression
    ::= expression_list
     -- | ERROR_TOKEN
     --     /. $Build  consumeExpressionProblem(); $EndBuild ./
      
-- expression_list and expression_list_opt always result in a single element on the stack
-- the element might be an expression, an expression list or null

expression_list
    ::= <openscope-ast> expression_list_actual
           /. $Build  consumeExpressionList();  $EndBuild ./


expression_list_actual
    ::= assignment_expression
      | expression_list_actual ',' assignment_expression

      
expression_list_opt
   ::= expression_list
     | $empty
          /. $Build  consumeEmpty();  $EndBuild ./
          
          
expression_opt
    ::= expression
      | $empty
          /. $Build  consumeEmpty();  $EndBuild ./
      
      
constant_expression
    ::= conditional_expression


constant_expression_opt
    ::= constant_expression
      | $empty
          /. $Build  consumeEmpty();  $EndBuild ./
      

------------------------------------------------------------------------------------------
-- Statements
------------------------------------------------------------------------------------------

statement
    ::= labeled_statement
      | expression_statement
      | compound_statement
      | selection_statement
      | iteration_statement
      | jump_statement
      | declaration_statement
      | try_block
      | ERROR_TOKEN
          /. $Build  consumeStatementProblem();  $EndBuild ./


labeled_statement
    ::= 'identifier' ':' statement
          /. $Build  consumeStatementLabeled();  $EndBuild ./
      | 'case' constant_expression ':' statement
          /. $Build  consumeStatementCase();  $EndBuild ./
      | 'default' ':' statement
          /. $Build  consumeStatementDefault();  $EndBuild ./
      
      
expression_statement
    ::= expression ';'
           /. $Build  consumeStatementExpression();  $EndBuild ./
      | ';'
           /. $Build  consumeStatementNull();  $EndBuild ./
    
    
compound_statement
    ::= '{' <openscope-ast> statement_seq '}'
          /. $Build  consumeStatementCompoundStatement(true);  $EndBuild ./
      | '{' '}'
          /. $Build  consumeStatementCompoundStatement(false);  $EndBuild ./
    
    
statement_seq
    ::= statement
      | statement_seq statement


selection_statement
    ::= 'if' '(' condition ')' statement
          /. $Build  consumeStatementIf(false);  $EndBuild ./
      | 'if' '(' condition ')' statement 'else' statement
          /. $Build  consumeStatementIf(true);  $EndBuild ./
      | 'switch' '(' condition ')' statement
          /. $Build  consumeStatementSwitch();  $EndBuild ./
      


condition
    ::= expression
      | type_specifier_seq declarator '=' assignment_expression
          /. $Build  consumeConditionDeclaration();  $EndBuild ./

condition_opt
    ::= condition
      | $empty
          /. $Build  consumeEmpty(); $EndBuild ./


iteration_statement
    ::= 'while' '(' condition ')' statement
          /. $Build  consumeStatementWhileLoop();  $EndBuild ./
      | 'do' statement 'while' '(' expression ')' ';'
          /. $Build  consumeStatementDoLoop(true);  $EndBuild ./
      | 'do' statement
          /. $Build  consumeStatementDoLoop(false);  $EndBuild ./
      | 'for' '(' for_init_statement condition_opt ';' expression_opt ')' statement
          /. $Build consumeStatementForLoop(); $EndBuild ./


-- I'm sure there are ambiguities here but we won't worry about it
for_init_statement
    ::= expression_statement
      | simple_declaration_with_declspec
          /. $Build  consumeStatementDeclaration();  $EndBuild ./


jump_statement
    ::= 'break' ';'
          /. $Build  consumeStatementBreak();  $EndBuild ./
      | 'continue' ';'
          /. $Build  consumeStatementContinue();  $EndBuild ./
      | 'return' expression ';'
          /. $Build  consumeStatementReturn(true);  $EndBuild ./
      | 'return' ';'
          /. $Build  consumeStatementReturn(false);  $EndBuild ./
      | 'goto' identifier_token ';'
          /. $Build  consumeStatementGoto();  $EndBuild ./


-- Nested functions are not part of the C++ spec, but several
-- of the parser test cases expect them to work.
declaration_statement
    ::= block_declaration
          /. $Build  consumeStatementDeclarationWithDisambiguation();  $EndBuild ./
      | function_definition  -- not spec
          /. $Build  consumeStatementDeclaration();  $EndBuild ./



------------------------------------------------------------------------------------------
-- Declarations
------------------------------------------------------------------------------------------


declaration
    ::= block_declaration
      | function_definition
      | template_declaration
      | explicit_instantiation
      | explicit_specialization
      | linkage_specification
      | namespace_definition
      | ERROR_TOKEN
          /. $Build  consumeDeclarationProblem();  $EndBuild ./


block_declaration
    ::= simple_declaration
      | asm_definition
      | namespace_alias_definition
      | using_declaration
      | using_directive


declaration_seq
    ::= declaration
      | declaration_seq declaration



declaration_seq_opt
    ::= declaration_seq
      | $empty
      
    
    
simple_declaration
    ::= declaration_specifiers_opt <openscope-ast> init_declarator_list_opt ';'
          /. $Build  consumeDeclarationSimple(true);  $EndBuild ./


simple_declaration_with_declspec 
    ::= declaration_specifiers <openscope-ast> init_declarator_list_opt ';'
          /. $Build  consumeDeclarationSimple(true);  $EndBuild ./


-- declaration specifier nodes not created here, they are created by sub-rules 
-- these rules add IToken modifiers to the declspec nodes
declaration_specifiers
    ::= <openscope-ast> simple_declaration_specifiers
          /. $Build  consumeDeclarationSpecifiersSimple();  $EndBuild ./
      | <openscope-ast> class_declaration_specifiers
          /. $Build  consumeDeclarationSpecifiersComposite();  $EndBuild ./
      | <openscope-ast> elaborated_declaration_specifiers
          /. $Build  consumeDeclarationSpecifiersComposite();  $EndBuild ./
      | <openscope-ast> enum_declaration_specifiers
          /. $Build  consumeDeclarationSpecifiersComposite();  $EndBuild ./
      | <openscope-ast> type_name_declaration_specifiers
          /. $Build  consumeDeclarationSpecifiersTypeName();  $EndBuild ./


declaration_specifiers_opt
    ::= declaration_specifiers
      | $empty 
          /. $Build  consumeEmpty();  $EndBuild ./

          

-- what about type qualifiers... cv_qualifier
no_type_declaration_specifier
    ::= storage_class_specifier
      | function_specifier
      | cv_qualifier
      | 'friend'
          /. $Build  consumeToken(); $EndBuild ./
      | 'typedef'
          /. $Build  consumeToken(); $EndBuild ./
      
      
no_type_declaration_specifiers
    ::= no_type_declaration_specifier
      | no_type_declaration_specifiers no_type_declaration_specifier
  
      
-- now also includes qualified names
-- if there is no long long then this may be simplified
simple_declaration_specifiers
    ::= simple_type_specifier
      | no_type_declaration_specifiers simple_type_specifier
      | simple_declaration_specifiers simple_type_specifier
      | simple_declaration_specifiers no_type_declaration_specifier
      | no_type_declaration_specifiers
      
      
-- struct, union or class!
class_declaration_specifiers
    ::= class_specifier
      | no_type_declaration_specifiers class_specifier
      | class_declaration_specifiers no_type_declaration_specifier
      

-- elaborated means something different, but how different?
elaborated_declaration_specifiers
    ::= elaborated_type_specifier
      | no_type_declaration_specifiers elaborated_type_specifier
      | elaborated_declaration_specifiers no_type_declaration_specifier


-- Think this is the same
enum_declaration_specifiers
    ::= enum_specifier
      | no_type_declaration_specifiers  enum_specifier
      | enum_declaration_specifiers no_type_declaration_specifier


-- just typedefs in C99, but expanded to type names in C++ (no tagging needed)
type_name_declaration_specifiers
    ::= type_name_specifier
      | no_type_declaration_specifiers  type_name_specifier
      | type_name_declaration_specifiers no_type_declaration_specifier
    
    
  -- TODO comment this out    
--decl_specifier
--    ::= storage_class_specifier -- just keywords
--      | type_specifier  -- this is where the fun is
 --     | function_specifier -- just keywords
 --     | 'friend'
 --     | 'typedef'


storage_class_specifier
    ::= 'auto'
          /. $Build  consumeToken(); $EndBuild ./
      | 'register'
          /. $Build  consumeToken(); $EndBuild ./
      | 'static'
          /. $Build  consumeToken(); $EndBuild ./
      | 'extern'
          /. $Build  consumeToken(); $EndBuild ./
      | 'mutable'
          /. $Build  consumeToken(); $EndBuild ./


function_specifier
    ::= 'inline'
          /. $Build  consumeToken(); $EndBuild ./
      | 'virtual'
          /. $Build  consumeToken(); $EndBuild ./
      | 'explicit'
          /. $Build  consumeToken(); $EndBuild ./


-- We have no way to disambiguate token types
--typedef_name
--    ::= identifier_token


--type_specifier
--    ::= simple_type_specifier  -- int, void etc...
--      | class_specifier  -- structs, unions, classes
--      | enum_specifier   -- enums
--      | elaborated_type_specifier  -- its elaborated, but this is different than c, includes typename
--      | cv_qualifier  -- the const and volatile keywords (separated because they can be applied to pointer modifiers)


--simple_type_specifier
--    ::= dcolon_opt nested_name_specifier_opt type_name
--          /. $Build  consumeQualifiedId(false);  $EndBuild ./
--      | dcolon_opt nested_name_specifier 'template' template_id_name
--          /. $Build  consumeQualifiedId(false);  $EndBuild ./
--      | simple_type_primitive_specifier
      
   

simple_type_specifier
    ::= simple_type_specifier_token
          /. $Build  consumeToken(); $EndBuild ./

simple_type_specifier_token
    ::= 'char'
      | 'wchar_t'
      | 'bool'
      | 'short'
      | 'int'
      | 'long'
      | 'signed'
      | 'unsigned'
      | 'float'
      | 'double'
      | 'void'


-- last two rules moved here from simple_type_specifier
type_name  -- all identifiers of some kind
    ::= class_name
     -- | enum_name 
     -- | typedef_name


-- last two rules moved here from simple_type_specifier
type_name_specifier  -- all identifiers of some kind
    ::= type_name
      | dcolon_opt nested_name_specifier_opt type_name
          /. $Build  consumeQualifiedId(false);  $EndBuild ./
      | dcolon_opt nested_name_specifier 'template' template_id_name
          /. $Build  consumeQualifiedId(false);  $EndBuild ./
      | 'typename' dcolon_opt nested_name_specifier identifier_name
          /. $Build  consumeQualifiedId(false);  $EndBuild ./
      | 'typename' dcolon_opt nested_name_specifier template_opt template_id_name
          /. $Build  consumeQualifiedId(true);  $EndBuild ./
      | 'typename' identifier_name


-- used for forward declaration and incomplete types
elaborated_type_specifier
    ::= class_keyword elaborated_specifier_hook dcolon_opt nested_name_specifier_opt identifier_name
          /. $Build  consumeTypeSpecifierElaborated(false);  $EndBuild ./
      | class_keyword elaborated_specifier_hook dcolon_opt nested_name_specifier_opt template_opt template_id_name
          /. $Build  consumeTypeSpecifierElaborated(true);   $EndBuild ./
      | 'enum' elaborated_specifier_hook dcolon_opt nested_name_specifier_opt identifier_name      
          /. $Build  consumeTypeSpecifierElaborated(false);  $EndBuild ./


elaborated_specifier_hook
    ::= $empty
    

-- there is currently no way to disambiguate identifier tokens
--enum_name
--   ::= identifier_token


comma_opt
    ::= ',' | $empty
    
    
enum_specifier
    ::= 'enum' enum_specifier_hook '{' <openscope-ast> enumerator_list_opt comma_opt '}'
          /. $Build  consumeTypeSpecifierEnumeration(false); $EndBuild ./
      | 'enum' enum_specifier_hook identifier_token '{' <openscope-ast> enumerator_list_opt comma_opt '}'
          /. $Build  consumeTypeSpecifierEnumeration(true); $EndBuild ./

enum_specifier_hook
    ::= $empty
    
enumerator_list
    ::= enumerator_definition
      | enumerator_list ',' enumerator_definition


enumerator_list_opt
    ::= enumerator_list
      | $empty


enumerator_definition
    ::= identifier_token
          /. $Build  consumeEnumerator(false); $EndBuild ./
      | identifier_token '=' constant_expression
          /. $Build  consumeEnumerator(true); $EndBuild ./


namespace_name
    ::= identifier_name
    
    
-- In the spec grammar this is broken down into original_namespace_definition and extension_namespace_definition.
-- But since we are not tracking identifiers it becomes the same thing, so its simplified here.
namespace_definition
    ::= 'namespace' namespace_name namespace_definition_hook '{' <openscope-ast> declaration_seq_opt '}'
          /. $Build  consumeNamespaceDefinition(true);  $EndBuild ./
      | 'namespace' namespace_definition_hook '{' <openscope-ast> declaration_seq_opt '}'
           /. $Build  consumeNamespaceDefinition(false);  $EndBuild ./

namespace_definition_hook
    ::= $empty
    
              
namespace_alias_definition
    ::= 'namespace' identifier_token '=' dcolon_opt nested_name_specifier_opt namespace_name ';'
           /. $Build  consumeNamespaceAliasDefinition(); $EndBuild ./
           

-- make more lenient!
-- using_declaration
--     ::= 'using' typename_opt dcolon_opt nested_name_specifier unqualified_id_name ';'
--       | 'using' '::' unqualified_id_name ';'
      
      
-- TODO why not just check if the second token is 'typename'?
using_declaration
    ::= 'using' typename_opt dcolon_opt nested_name_specifier_opt unqualified_id_name ';'
          /. $Build  consumeUsingDeclaration();  $EndBuild ./


typename_opt
    ::= 'typename'
          /. $Build  consumePlaceHolder();  $EndBuild ./
      | $empty
          /. $Build  consumeEmpty();  $EndBuild ./


using_directive
    ::= 'using' 'namespace' dcolon_opt nested_name_specifier_opt namespace_name ';'
           /. $Build  consumeUsingDirective();  $EndBuild ./


asm_definition
    ::= 'asm' '(' 'stringlit' ')' ';'
          /. $Build  consumeDeclarationASM(); $EndBuild ./


linkage_specification
    ::= 'extern' 'stringlit' '{' <openscope-ast> declaration_seq_opt '}'
           /. $Build  consumeLinkageSpecification();  $EndBuild ./
      | 'extern' 'stringlit' <openscope-ast> declaration
           /. $Build  consumeLinkageSpecification();  $EndBuild ./


init_declarator_list
    ::= init_declarator_complete
      | init_declarator_list ',' init_declarator_complete


init_declarator_list_opt
    ::= init_declarator_list
      | $empty
      
      
init_declarator_complete
    ::= init_declarator
          /. $Build  consumeInitDeclaratorComplete();  $EndBuild ./ 
      
      
init_declarator
    ::= complete_declarator 
      | complete_declarator initializer
          /. $Build  consumeDeclaratorWithInitializer(true);  $EndBuild ./

complete_declarator
    ::= declarator

declarator
    ::= direct_declarator 
      | <openscope-ast> ptr_operator_seq direct_declarator
          /. $Build  consumeDeclaratorWithPointer(true);  $EndBuild ./

function_declarator
    ::= function_direct_declarator
      | <openscope-ast> ptr_operator_seq direct_declarator
          /. $Build  consumeDeclaratorWithPointer(true);  $EndBuild ./
      

direct_declarator
    ::= basic_direct_declarator
      | function_direct_declarator
      | array_direct_declarator


basic_direct_declarator
    ::= declarator_id_name
         /. $Build  consumeDirectDeclaratorIdentifier();  $EndBuild ./
      | '(' declarator ')'
         /. $Build  consumeDirectDeclaratorBracketed();  $EndBuild ./


function_direct_declarator
    ::= basic_direct_declarator '(' <openscope-ast> parameter_declaration_clause ')' <openscope-ast> cv_qualifier_seq_opt <openscope-ast> exception_specification_opt
          /. $Build  consumeDirectDeclaratorFunctionDeclarator(true);  $EndBuild ./


array_direct_declarator
    ::= array_direct_declarator array_modifier
           /. $Build  consumeDirectDeclaratorArrayDeclarator(true);  $EndBuild ./
      | basic_direct_declarator array_modifier
           /. $Build  consumeDirectDeclaratorArrayDeclarator(true);  $EndBuild ./


array_modifier
    ::= '[' constant_expression ']'
           /. $Build  consumeDirectDeclaratorArrayModifier(true);  $EndBuild ./
      | '[' ']'
           /. $Build  consumeDirectDeclaratorArrayModifier(false);  $EndBuild ./      
      

ptr_operator
    ::= pointer_hook '*' pointer_hook <openscope-ast> cv_qualifier_seq_opt
          /. $Build  consumePointer();  $EndBuild ./
      | pointer_hook '&' pointer_hook
          /. $Build  consumeReferenceOperator();  $EndBuild ./
      | dcolon_opt nested_name_specifier pointer_hook '*' pointer_hook <openscope-ast> cv_qualifier_seq_opt
          /. $Build  consumePointerToMember();  $EndBuild ./

pointer_hook
    ::= $empty
    
ptr_operator_seq
    ::= ptr_operator
      | ptr_operator_seq ptr_operator


--ptr_operator_seq_opt
--    ::= ptr_operator_seq
--      | $empty


cv_qualifier_seq
    ::= cv_qualifier cv_qualifier_seq_opt


cv_qualifier_seq_opt
    ::= cv_qualifier_seq
      | $empty


cv_qualifier
    ::= 'const'
          /. $Build  consumeToken(); $EndBuild ./
      | 'volatile'
          /. $Build  consumeToken(); $EndBuild ./


declarator_id_name
   ::= qualified_or_unqualified_name
     | dcolon_opt nested_name_specifier_opt type_name
          /. $Build  consumeQualifiedId(false);  $EndBuild ./
      
--declarator_id_name
--   ::= unqualified_id_name
--     | <empty> nested_name_specifier template_opt unqualified_id_name
--         /. $Build  consumeQualifiedId(true);  $EndBuild ./
  

type_id
    ::= type_specifier_seq
          /. $Build  consumeTypeId(false);  $EndBuild ./
      | type_specifier_seq abstract_declarator
          /. $Build  consumeTypeId(true);  $EndBuild ./


--type_specifier_seq
--    ::= type_specifier
--      | type_specifier_seq type_specifier


-- more lenient than spec, but easier to deal with
-- TODO are conflicts resolved by using the more strict rule? 
type_specifier_seq
    ::= declaration_specifiers



abstract_declarator
    ::= direct_abstract_declarator 
      | <openscope-ast> ptr_operator_seq 
          /. $Build  consumeDeclaratorWithPointer(false);  $EndBuild ./
      | <openscope-ast> ptr_operator_seq direct_abstract_declarator
          /. $Build  consumeDeclaratorWithPointer(true);  $EndBuild ./
      
      
direct_abstract_declarator
    ::= basic_direct_abstract_declarator
      | array_direct_abstract_declarator
      | function_direct_abstract_declarator
      

basic_direct_abstract_declarator
    ::= '(' abstract_declarator ')'
          /. $Build  consumeDirectDeclaratorBracketed();  $EndBuild ./
      | '(' ')'
          /. $Build  consumeAbstractDeclaratorEmpty();  $EndBuild ./
          

array_direct_abstract_declarator
    ::= array_modifier
          /. $Build  consumeDirectDeclaratorArrayDeclarator(false);  $EndBuild ./
      | array_direct_abstract_declarator array_modifier
          /. $Build  consumeDirectDeclaratorArrayDeclarator(true);  $EndBuild ./
      | basic_direct_abstract_declarator array_modifier
          /. $Build  consumeDirectDeclaratorArrayDeclarator(true);  $EndBuild ./    
       

function_direct_abstract_declarator                 
     ::= basic_direct_abstract_declarator '(' <openscope-ast> parameter_declaration_clause ')' <openscope-ast> cv_qualifier_seq_opt <openscope-ast> exception_specification_opt
           /. $Build  consumeDirectDeclaratorFunctionDeclarator(true);  $EndBuild ./
       | '(' <openscope-ast> parameter_declaration_clause ')' <openscope-ast> cv_qualifier_seq_opt <openscope-ast> exception_specification_opt
           /. $Build  consumeDirectDeclaratorFunctionDeclarator(false);  $EndBuild ./
      

-- actions just place a marker indicating if '...' was parsed
parameter_declaration_clause
    ::= parameter_declaration_list_opt '...'
          /. $Build  consumePlaceHolder();  $EndBuild ./
      | parameter_declaration_list_opt
          /. $Build  consumeEmpty();  $EndBuild ./
      | parameter_declaration_list ',' '...'
          /. $Build  consumePlaceHolder();  $EndBuild ./


parameter_declaration_list
     ::= parameter_declaration
       | parameter_declaration_list ',' parameter_declaration


parameter_declaration_list_opt
    ::= parameter_declaration_list
      | $empty


abstract_declarator_opt
    ::= abstract_declarator
      | $empty
          /. $Build  consumeEmpty();  $EndBuild ./
          
          
parameter_declaration
    ::= declaration_specifiers parameter_init_declarator
          /. $Build  consumeParameterDeclaration();  $EndBuild ./
      | declaration_specifiers
          /. $Build  consumeParameterDeclarationWithoutDeclarator();  $EndBuild ./


parameter_init_declarator
	::= declarator
	  | declarator '=' parameter_initializer
	      /. $Build  consumeDeclaratorWithInitializer(true);  $EndBuild ./
	  | abstract_declarator
	  | abstract_declarator '=' parameter_initializer
	      /. $Build  consumeDeclaratorWithInitializer(true);  $EndBuild ./
	  | '=' parameter_initializer
	      /. $Build  consumeDeclaratorWithInitializer(false);  $EndBuild ./
	  
	  
parameter_initializer
    ::= assignment_expression
          /. $Build  consumeInitializer();  $EndBuild ./


function_definition
    ::= declaration_specifiers_opt function_declarator <openscope-ast> ctor_initializer_list_opt function_body
           /. $Build  consumeFunctionDefinition(false);  $EndBuild ./
      | declaration_specifiers_opt function_declarator 'try' <openscope-ast> ctor_initializer_list_opt function_body <openscope-ast> handler_seq
           /. $Build  consumeFunctionDefinition(true);  $EndBuild ./

    
function_body
    ::= compound_statement


initializer
    ::= '=' initializer_clause
      | '(' expression_list ')'
          /. $Build  consumeInitializerConstructor();  $EndBuild ./


initializer_clause
    ::= assignment_expression
         /. $Build  consumeInitializer();  $EndBuild ./
      | initializer_list
      
         
initializer_list
    ::= start_initializer_list '{' <openscope-ast> initializer_seq ',' '}' end_initializer_list
         /. $Build  consumeInitializerList();  $EndBuild ./
      | start_initializer_list '{' <openscope-ast> initializer_seq '}' end_initializer_list
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
    ::= initializer_clause
      | initializer_seq ',' initializer_clause


      
------------------------------------------------------------------------------------------
-- Classes
------------------------------------------------------------------------------------------


class_name
    ::= identifier_name
      | template_id_name


class_specifier
    ::= class_head '{'   <openscope-ast> member_declaration_list_opt '}'
          /.  $Build  consumeClassSpecifier();  $EndBuild ./
       
       
class_head
    ::= class_keyword composite_specifier_hook identifier_name_opt class_name_suffix_hook <openscope-ast> base_clause_opt
          /. $Build  consumeClassHead(false);  $EndBuild ./
      | class_keyword composite_specifier_hook template_id_name class_name_suffix_hook <openscope-ast> base_clause_opt
          /. $Build  consumeClassHead(false);  $EndBuild ./
      | class_keyword composite_specifier_hook nested_name_specifier identifier_name class_name_suffix_hook <openscope-ast> base_clause_opt
          /. $Build  consumeClassHead(true);  $EndBuild ./
      | class_keyword composite_specifier_hook nested_name_specifier template_id_name class_name_suffix_hook <openscope-ast> base_clause_opt
          /. $Build  consumeClassHead(true);  $EndBuild ./

composite_specifier_hook
    ::= $empty
    
class_name_suffix_hook
    ::= $empty

identifier_name_opt
    ::= identifier_name
      | $empty
          /. $Build  consumeEmpty();  $EndBuild./      
      
      
class_keyword
    ::= 'class'
      | 'struct'
      | 'union'


visibility_label
    ::= access_specifier_keyword ':'
          /. $Build  consumeVisibilityLabel();  $EndBuild ./



member_declaration
    ::= declaration_specifiers_opt <openscope-ast> member_declarator_list ';'
          /. $Build  consumeDeclarationSimple(true);  $EndBuild ./
      | declaration_specifiers_opt ';'
          /. $Build  consumeDeclarationSimple(false);  $EndBuild ./
      | function_definition ';' 
      | function_definition      
      | dcolon_opt nested_name_specifier template_opt unqualified_id_name ';'
          /. $Build  consumeMemberDeclarationQualifiedId();  $EndBuild ./ 
      | using_declaration  
      | template_declaration
      | explicit_specialization  -- not spec
      | namespace_definition     -- not spec
      | visibility_label 
      | ERROR_TOKEN
          /. $Build  consumeDeclarationProblem();  $EndBuild ./


member_declaration_list
    ::= member_declaration
      | member_declaration_list member_declaration


member_declaration_list_opt
    ::= member_declaration_list
      | $empty


member_declarator_list
    ::= member_declarator_complete
      | member_declarator_list ',' member_declarator_complete


member_declarator_complete
    ::= member_declarator

member_declarator
    ::= declarator
    -- parse pure specifier as a constant_initializer, reduces conflicts
    -- | declarator pure_specifier  
      | declarator constant_initializer
          /. $Build  consumeMemberDeclaratorWithInitializer();  $EndBuild ./
      | bit_field_declarator ':' constant_expression
          /. $Build  consumeBitField(true);  $EndBuild ./
      | ':' constant_expression
          /. $Build  consumeBitField(false);  $EndBuild ./


bit_field_declarator
    ::= identifier_name
          /. $Build  consumeDirectDeclaratorIdentifier();  $EndBuild ./


constant_initializer
    ::= '=' constant_expression
            /. $Build  consumeInitializer();  $EndBuild ./


base_clause
    ::= ':' base_specifier_list


base_clause_opt
    ::= base_clause
      | $empty 
    

base_specifier_list
    ::= base_specifier
      | base_specifier_list ',' base_specifier



base_specifier
    ::= dcolon_opt nested_name_specifier_opt class_name
          /. $Build  consumeBaseSpecifier(false, false);  $EndBuild ./
      | 'virtual' access_specifier_keyword_opt dcolon_opt nested_name_specifier_opt class_name
          /. $Build  consumeBaseSpecifier(true, true);  $EndBuild ./
      | access_specifier_keyword 'virtual' dcolon_opt nested_name_specifier_opt class_name
          /. $Build  consumeBaseSpecifier(true, true);  $EndBuild ./
      | access_specifier_keyword dcolon_opt nested_name_specifier_opt class_name
          /. $Build  consumeBaseSpecifier(true, false);  $EndBuild ./

      
access_specifier_keyword
    ::= 'private'
          /. $Build  consumeToken();  $EndBuild ./
      | 'protected'
          /. $Build  consumeToken();  $EndBuild ./
      | 'public'
          /. $Build  consumeToken();  $EndBuild ./


access_specifier_keyword_opt
    ::= access_specifier_keyword
      | $empty
          /. $Build  consumeEmpty();  $EndBuild ./


conversion_function_id_name
    ::= conversion_function_id
      | conversion_function_id '<' <openscope-ast> template_argument_list_opt '>'
          /. $Build  consumeTemplateId();  $EndBuild ./
          
conversion_function_id
    ::= 'operator' conversion_type_id
          /. $Build  consumeConversionName(); $EndBuild ./


conversion_type_id
    ::= type_specifier_seq conversion_declarator
          /. $Build  consumeTypeId(true);  $EndBuild ./
      | type_specifier_seq
          /. $Build  consumeTypeId(false);  $EndBuild ./


conversion_declarator
    ::= <openscope-ast> ptr_operator_seq
          /. $Build  consumeDeclaratorWithPointer(false);  $EndBuild ./
      
 
 
--conversion_declarator_opt
--    ::= conversion_declarator
--      | $empty
      
    
ctor_initializer_list
    ::= ':' mem_initializer_list


ctor_initializer_list_opt
    ::= ctor_initializer_list
      | $empty
      

mem_initializer_list
    ::= mem_initializer
      | mem_initializer ',' mem_initializer_list


mem_initializer
    ::= mem_initializer_name '(' expression_list_opt ')'
          /. $Build  consumeConstructorChainInitializer();  $EndBuild ./


mem_initializer_name
    ::= dcolon_opt nested_name_specifier_opt class_name
          /. $Build  consumeQualifiedId(false);  $EndBuild ./
      | identifier_name


operator_function_id_name
    ::= operator_id_name
      | operator_id_name '<' <openscope-ast> template_argument_list_opt '>'
          /. $Build  consumeTemplateId();  $EndBuild ./


operator_id_name
    ::= 'operator' overloadable_operator
          /. $Build  consumeOperatorName();  $EndBuild ./


overloadable_operator
    ::= 'new' | 'delete' | 'new' '[' ']' | 'delete' '[' ']'
      | '+' | '-' | '*' | '/' | '%' | '^' | '&' | '|' | '~'
      | '!' | '=' | '<' | '>' | '+=' | '-=' | '*=' | '/=' | '%='
      | '^=' | '&=' | '|=' | '<<' | '>>' | '>>=' | '<<=' | '==' | '!='
      | '<=' | '>=' | '&&' | '||' | '++' | '--' | ',' | '->*' | '->'
      | '(' ')' | '[' ']'


template_declaration
    ::= export_opt 'template' '<' <openscope-ast> template_parameter_list '>' declaration
          /. $Build  consumeTemplateDeclaration();  $EndBuild ./


export_opt
    ::= 'export'
          /. $Build  consumePlaceHolder();  $EndBuild ./
      | $empty
          /. $Build  consumeEmpty();  $EndBuild ./


template_parameter_list
    ::= template_parameter
      | template_parameter_list ',' template_parameter


-- TODO There is an ambiguity in the spec grammar here, 
-- "class X" should be parsed as a type_parameter
-- and not as a parameter_declaration. Here precedence is used to disambiguate
-- but it would be better to refactor the grammar to remove the conflict.

template_parameter
    ::= type_parameter
      | parameter_declaration
          /. $Build  consumeTemplateParamterDeclaration();  $EndBuild ./ 


type_parameter
    ::= 'class' identifier_name_opt 
          /. $Build  consumeSimpleTypeTemplateParameter(false);  $EndBuild ./
      | 'class' identifier_name_opt '=' type_id
          /. $Build  consumeSimpleTypeTemplateParameter(true);  $EndBuild ./
      | 'typename' identifier_name_opt
          /. $Build  consumeSimpleTypeTemplateParameter(false);  $EndBuild ./
      | 'typename' identifier_name_opt '=' type_id
          /. $Build  consumeSimpleTypeTemplateParameter(true);  $EndBuild ./
      | 'template' '<' <openscope-ast> template_parameter_list '>' 'class' identifier_name_opt
          /. $Build  consumeTemplatedTypeTemplateParameter(false);  $EndBuild ./
      | 'template' '<' <openscope-ast> template_parameter_list '>' 'class' identifier_name_opt '=' id_expression
          /. $Build  consumeTemplatedTypeTemplateParameter(true);  $EndBuild ./



template_id_name
    ::= identifier_name '<' <openscope-ast> template_argument_list_opt '>'
          /. $Build  consumeTemplateId();  $EndBuild ./


template_argument_list
    ::= template_argument
      | template_argument_list ',' template_argument


template_argument_list_opt
    ::= template_argument_list
      | $empty
      

template_argument
    ::= assignment_expression
          /. $Build  consumeTemplateArgumentExpression();  $EndBuild ./
      | type_id
          /. $Build  consumeTemplateArgumentTypeId();  $EndBuild ./
      --| qualified_or_unqualified_name -- accessible through assignment_expression

explicit_instantiation
    ::= 'template' declaration
          /. $Build  consumeTemplateExplicitInstantiation();  $EndBuild ./


explicit_specialization
    ::= 'template' '<' '>' declaration
           /. $Build  consumeTemplateExplicitSpecialization();  $EndBuild ./


try_block
    ::= 'try' compound_statement <openscope-ast> handler_seq
          /. $Build  consumeStatementTryBlock(true);  $EndBuild ./
	  | 'try' compound_statement
          /. $Build  consumeStatementTryBlock(false);  $EndBuild ./





handler_seq
    ::= handler
      | handler_seq handler


handler
    ::= 'catch' '(' exception_declaration ')' compound_statement
          /. $Build  consumeStatementCatchHandler(false);  $EndBuild ./
      | 'catch' '(' '...' ')' compound_statement
          /. $Build  consumeStatementCatchHandler(true);  $EndBuild ./


-- open a scope just so that we can reuse consumeDeclarationSimple()
exception_declaration
    ::= type_specifier_seq <openscope-ast> declarator
          /. $Build  consumeDeclarationSimple(true);  $EndBuild ./
      | type_specifier_seq <openscope-ast> abstract_declarator
          /. $Build  consumeDeclarationSimple(true);  $EndBuild ./
      | type_specifier_seq
          /. $Build  consumeDeclarationSimple(false);  $EndBuild ./


-- puts type ids on the stack
exception_specification
    ::= 'throw' '(' type_id_list ')'
      | 'throw' '('  ')'
          /. $Build  consumePlaceHolder();  $EndBuild ./


exception_specification_opt
    ::= exception_specification
      | $empty


type_id_list
    ::= type_id
      | type_id_list ',' type_id