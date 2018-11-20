/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.cpp;

import java.util.HashMap;
import java.util.Map;

public class CPPRules {
	static Map fRules = new HashMap();

	static {
		fRules.put(Integer.valueOf(3), "] ::= RightBracket");
		fRules.put(Integer.valueOf(4), "] ::= EndOfCompletion");
		fRules.put(Integer.valueOf(5), ") ::= RightParen");
		fRules.put(Integer.valueOf(6), ") ::= EndOfCompletion");
		fRules.put(Integer.valueOf(7), "} ::= RightBrace");
		fRules.put(Integer.valueOf(8), "} ::= EndOfCompletion");
		fRules.put(Integer.valueOf(9), "; ::= SemiColon");
		fRules.put(Integer.valueOf(10), "; ::= EndOfCompletion");
		fRules.put(Integer.valueOf(41), "dcolon_opt ::= ColonColon");
		fRules.put(Integer.valueOf(42), "dcolon_opt ::=");
		fRules.put(Integer.valueOf(228), "declaration_specifiers_opt ::=");
		fRules.put(Integer.valueOf(229), "declaration_specifiers_opt ::= declaration_specifiers");
		fRules.put(Integer.valueOf(383), "class_name ::= identifier_name");
		fRules.put(Integer.valueOf(384), "class_name ::= template_id_name");
		fRules.put(Integer.valueOf(493), "template_parameter ::= type_parameter");
		fRules.put(Integer.valueOf(1), "<openscope-ast> ::=");
		fRules.put(Integer.valueOf(17), "identifier_token ::= identifier");
		fRules.put(Integer.valueOf(18), "identifier_token ::= Completion");
		fRules.put(Integer.valueOf(38), "identifier_name ::= identifier_token");
		fRules.put(Integer.valueOf(445), "operator_id_name ::= operator overloadable_operator");
		fRules.put(Integer.valueOf(443), "operator_function_id_name ::= operator_id_name");
		fRules.put(Integer.valueOf(444),
				"operator_function_id_name ::= operator_id_name LT <openscope-ast> template_argument_list_opt GT");
		fRules.put(Integer.valueOf(431), "conversion_function_id_name ::= operator conversion_type_id");
		fRules.put(Integer.valueOf(502), "template_identifier ::= identifier_name");
		fRules.put(Integer.valueOf(501),
				"template_id_name ::= template_identifier LT <openscope-ast> template_argument_list_opt GT");
		fRules.put(Integer.valueOf(33), "unqualified_id_name ::= identifier_name");
		fRules.put(Integer.valueOf(34), "unqualified_id_name ::= operator_function_id_name");
		fRules.put(Integer.valueOf(35), "unqualified_id_name ::= conversion_function_id_name");
		fRules.put(Integer.valueOf(36), "unqualified_id_name ::= template_id_name");
		fRules.put(Integer.valueOf(37), "unqualified_id_name ::= Tilde class_name");
		fRules.put(Integer.valueOf(54), "class_or_namespace_name ::= class_name");
		fRules.put(Integer.valueOf(47),
				"nested_name_specifier ::= class_or_namespace_name ColonColon nested_name_specifier_with_template");
		fRules.put(Integer.valueOf(48), "nested_name_specifier ::= class_or_namespace_name ColonColon");
		fRules.put(Integer.valueOf(43),
				"qualified_id_name ::= dcolon_opt nested_name_specifier template_opt unqualified_id_name");
		fRules.put(Integer.valueOf(44), "qualified_id_name ::= ColonColon identifier_name");
		fRules.put(Integer.valueOf(45), "qualified_id_name ::= ColonColon operator_function_id_name");
		fRules.put(Integer.valueOf(46), "qualified_id_name ::= ColonColon template_id_name");
		fRules.put(Integer.valueOf(31), "qualified_or_unqualified_name ::= unqualified_id_name");
		fRules.put(Integer.valueOf(32), "qualified_or_unqualified_name ::= qualified_id_name");
		fRules.put(Integer.valueOf(52), "nested_name_specifier_opt ::= nested_name_specifier");
		fRules.put(Integer.valueOf(53), "nested_name_specifier_opt ::=");
		fRules.put(Integer.valueOf(274), "type_name ::= class_name");
		fRules.put(Integer.valueOf(338), "declarator_id_name ::= qualified_or_unqualified_name");
		fRules.put(Integer.valueOf(339), "declarator_id_name ::= dcolon_opt nested_name_specifier_opt type_name");
		fRules.put(Integer.valueOf(321), "basic_direct_declarator ::= declarator_id_name");
		fRules.put(Integer.valueOf(322), "basic_direct_declarator ::= LeftParen declarator )");
		fRules.put(Integer.valueOf(323),
				"function_direct_declarator ::= basic_direct_declarator LeftParen <openscope-ast> parameter_declaration_clause ) <openscope-ast> cv_qualifier_seq_opt <openscope-ast> exception_specification_opt");
		fRules.put(Integer.valueOf(324), "array_direct_declarator ::= array_direct_declarator array_modifier");
		fRules.put(Integer.valueOf(325), "array_direct_declarator ::= basic_direct_declarator array_modifier");
		fRules.put(Integer.valueOf(318), "direct_declarator ::= basic_direct_declarator");
		fRules.put(Integer.valueOf(319), "direct_declarator ::= function_direct_declarator");
		fRules.put(Integer.valueOf(320), "direct_declarator ::= array_direct_declarator");
		fRules.put(Integer.valueOf(328), "ptr_operator ::= Star <openscope-ast> cv_qualifier_seq_opt");
		fRules.put(Integer.valueOf(329), "ptr_operator ::= And");
		fRules.put(Integer.valueOf(330),
				"ptr_operator ::= dcolon_opt nested_name_specifier Star <openscope-ast> cv_qualifier_seq_opt");
		fRules.put(Integer.valueOf(331), "ptr_operator_seq ::= ptr_operator");
		fRules.put(Integer.valueOf(332), "ptr_operator_seq ::= ptr_operator_seq ptr_operator");
		fRules.put(Integer.valueOf(314), "declarator ::= direct_declarator");
		fRules.put(Integer.valueOf(315), "declarator ::= <openscope-ast> ptr_operator_seq direct_declarator");
		fRules.put(Integer.valueOf(312), "init_declarator ::= declarator");
		fRules.put(Integer.valueOf(313), "init_declarator ::= declarator initializer");
		fRules.put(Integer.valueOf(311), "init_declarator_complete ::= init_declarator");
		fRules.put(Integer.valueOf(307), "init_declarator_list ::= init_declarator_complete");
		fRules.put(Integer.valueOf(308),
				"init_declarator_list ::= init_declarator_list Comma init_declarator_complete");
		fRules.put(Integer.valueOf(309), "init_declarator_list_opt ::= init_declarator_list");
		fRules.put(Integer.valueOf(310), "init_declarator_list_opt ::=");
		fRules.put(Integer.valueOf(221),
				"simple_declaration ::= declaration_specifiers_opt <openscope-ast> init_declarator_list_opt ;");
		fRules.put(Integer.valueOf(304), "asm_definition ::= asm LeftParen stringlit ) ;");
		fRules.put(Integer.valueOf(299),
				"namespace_alias_definition ::= namespace identifier_token Assign dcolon_opt nested_name_specifier_opt namespace_name ;");
		fRules.put(Integer.valueOf(300),
				"using_declaration ::= using typename_opt dcolon_opt nested_name_specifier_opt unqualified_id_name ;");
		fRules.put(Integer.valueOf(303),
				"using_directive ::= using namespace dcolon_opt nested_name_specifier_opt namespace_name ;");
		fRules.put(Integer.valueOf(212), "block_declaration ::= simple_declaration");
		fRules.put(Integer.valueOf(213), "block_declaration ::= asm_definition");
		fRules.put(Integer.valueOf(214), "block_declaration ::= namespace_alias_definition");
		fRules.put(Integer.valueOf(215), "block_declaration ::= using_declaration");
		fRules.put(Integer.valueOf(216), "block_declaration ::= using_directive");
		fRules.put(Integer.valueOf(316), "function_declarator ::= function_direct_declarator");
		fRules.put(Integer.valueOf(317), "function_declarator ::= <openscope-ast> ptr_operator_seq direct_declarator");
		fRules.put(Integer.valueOf(372),
				"function_definition ::= declaration_specifiers_opt function_declarator <openscope-ast> ctor_initializer_list_opt function_body");
		fRules.put(Integer.valueOf(373),
				"function_definition ::= declaration_specifiers_opt function_declarator try <openscope-ast> ctor_initializer_list_opt function_body <openscope-ast> handler_seq");
		fRules.put(Integer.valueOf(489), "export_opt ::= export");
		fRules.put(Integer.valueOf(490), "export_opt ::=");
		fRules.put(Integer.valueOf(488),
				"template_declaration ::= export_opt template LT <openscope-ast> template_parameter_list GT declaration");
		fRules.put(Integer.valueOf(510), "explicit_instantiation ::= template declaration");
		fRules.put(Integer.valueOf(511), "explicit_specialization ::= template LT GT declaration");
		fRules.put(Integer.valueOf(305),
				"linkage_specification ::= extern stringlit LeftBrace <openscope-ast> declaration_seq_opt }");
		fRules.put(Integer.valueOf(306), "linkage_specification ::= extern stringlit <openscope-ast> declaration");
		fRules.put(Integer.valueOf(296),
				"original_namespace_definition ::= namespace identifier_name LeftBrace <openscope-ast> declaration_seq_opt }");
		fRules.put(Integer.valueOf(297),
				"extension_namespace_definition ::= namespace original_namespace_name LeftBrace <openscope-ast> declaration_seq_opt }");
		fRules.put(Integer.valueOf(294), "named_namespace_definition ::= original_namespace_definition");
		fRules.put(Integer.valueOf(295), "named_namespace_definition ::= extension_namespace_definition");
		fRules.put(Integer.valueOf(298),
				"unnamed_namespace_definition ::= namespace LeftBrace <openscope-ast> declaration_seq_opt }");
		fRules.put(Integer.valueOf(292), "namespace_definition ::= named_namespace_definition");
		fRules.put(Integer.valueOf(293), "namespace_definition ::= unnamed_namespace_definition");
		fRules.put(Integer.valueOf(205), "declaration ::= block_declaration");
		fRules.put(Integer.valueOf(206), "declaration ::= function_definition");
		fRules.put(Integer.valueOf(207), "declaration ::= template_declaration");
		fRules.put(Integer.valueOf(208), "declaration ::= explicit_instantiation");
		fRules.put(Integer.valueOf(209), "declaration ::= explicit_specialization");
		fRules.put(Integer.valueOf(210), "declaration ::= linkage_specification");
		fRules.put(Integer.valueOf(211), "declaration ::= namespace_definition");
		fRules.put(Integer.valueOf(15), "external_declaration ::= declaration");
		fRules.put(Integer.valueOf(16), "external_declaration ::= ERROR_TOKEN");
		fRules.put(Integer.valueOf(13), "external_declaration_list ::= external_declaration");
		fRules.put(Integer.valueOf(14), "external_declaration_list ::= external_declaration_list external_declaration");
		fRules.put(Integer.valueOf(11), "translation_unit ::= external_declaration_list");
		fRules.put(Integer.valueOf(12), "translation_unit ::=");
		fRules.put(Integer.valueOf(0), "$accept ::= translation_unit");
		fRules.put(Integer.valueOf(2), "<empty> ::=");
		fRules.put(Integer.valueOf(19), "literal ::= integer");
		fRules.put(Integer.valueOf(20), "literal ::= zero");
		fRules.put(Integer.valueOf(21), "literal ::= floating");
		fRules.put(Integer.valueOf(22), "literal ::= charconst");
		fRules.put(Integer.valueOf(23), "literal ::= stringlit");
		fRules.put(Integer.valueOf(24), "literal ::= true");
		fRules.put(Integer.valueOf(25), "literal ::= false");
		fRules.put(Integer.valueOf(26), "literal ::= this");
		fRules.put(Integer.valueOf(30), "id_expression ::= qualified_or_unqualified_name");
		fRules.put(Integer.valueOf(27), "primary_expression ::= literal");
		fRules.put(Integer.valueOf(28), "primary_expression ::= LeftParen expression )");
		fRules.put(Integer.valueOf(29), "primary_expression ::= id_expression");
		fRules.put(Integer.valueOf(263), "simple_type_specifier_token ::= char");
		fRules.put(Integer.valueOf(264), "simple_type_specifier_token ::= wchar_t");
		fRules.put(Integer.valueOf(265), "simple_type_specifier_token ::= bool");
		fRules.put(Integer.valueOf(266), "simple_type_specifier_token ::= short");
		fRules.put(Integer.valueOf(267), "simple_type_specifier_token ::= int");
		fRules.put(Integer.valueOf(268), "simple_type_specifier_token ::= long");
		fRules.put(Integer.valueOf(269), "simple_type_specifier_token ::= signed");
		fRules.put(Integer.valueOf(270), "simple_type_specifier_token ::= unsigned");
		fRules.put(Integer.valueOf(271), "simple_type_specifier_token ::= float");
		fRules.put(Integer.valueOf(272), "simple_type_specifier_token ::= double");
		fRules.put(Integer.valueOf(273), "simple_type_specifier_token ::= void");
		fRules.put(Integer.valueOf(262), "simple_type_specifier ::= simple_type_specifier_token");
		fRules.put(Integer.valueOf(55), "postfix_expression ::= primary_expression");
		fRules.put(Integer.valueOf(56), "postfix_expression ::= postfix_expression LeftBracket expression ]");
		fRules.put(Integer.valueOf(57), "postfix_expression ::= postfix_expression LeftParen expression_list_opt )");
		fRules.put(Integer.valueOf(58), "postfix_expression ::= simple_type_specifier LeftParen expression_list_opt )");
		fRules.put(Integer.valueOf(59),
				"postfix_expression ::= typename dcolon_opt nested_name_specifier <empty> identifier_name LeftParen expression_list_opt )");
		fRules.put(Integer.valueOf(60),
				"postfix_expression ::= typename dcolon_opt nested_name_specifier template_opt template_id_name LeftParen expression_list_opt )");
		fRules.put(Integer.valueOf(61), "postfix_expression ::= postfix_expression Dot qualified_or_unqualified_name");
		fRules.put(Integer.valueOf(62),
				"postfix_expression ::= postfix_expression Arrow qualified_or_unqualified_name");
		fRules.put(Integer.valueOf(63),
				"postfix_expression ::= postfix_expression Dot template qualified_or_unqualified_name");
		fRules.put(Integer.valueOf(64),
				"postfix_expression ::= postfix_expression Arrow template qualified_or_unqualified_name");
		fRules.put(Integer.valueOf(65), "postfix_expression ::= postfix_expression Dot pseudo_destructor_name");
		fRules.put(Integer.valueOf(66), "postfix_expression ::= postfix_expression Arrow pseudo_destructor_name");
		fRules.put(Integer.valueOf(67), "postfix_expression ::= postfix_expression PlusPlus");
		fRules.put(Integer.valueOf(68), "postfix_expression ::= postfix_expression MinusMinus");
		fRules.put(Integer.valueOf(69), "postfix_expression ::= dynamic_cast LT type_id GT LeftParen expression )");
		fRules.put(Integer.valueOf(70), "postfix_expression ::= static_cast LT type_id GT LeftParen expression )");
		fRules.put(Integer.valueOf(71), "postfix_expression ::= reinterpret_cast LT type_id GT LeftParen expression )");
		fRules.put(Integer.valueOf(72), "postfix_expression ::= const_cast LT type_id GT LeftParen expression )");
		fRules.put(Integer.valueOf(73), "postfix_expression ::= typeid LeftParen expression )");
		fRules.put(Integer.valueOf(74), "postfix_expression ::= typeid LeftParen type_id )");
		fRules.put(Integer.valueOf(92),
				"new_expression ::= dcolon_opt new new_placement_opt new_type_id <openscope-ast> new_array_expressions_opt new_initializer_opt");
		fRules.put(Integer.valueOf(93),
				"new_expression ::= dcolon_opt new new_placement_opt LeftParen type_id ) <openscope-ast> new_array_expressions_opt new_initializer_opt");
		fRules.put(Integer.valueOf(108), "delete_expression ::= dcolon_opt delete cast_expression");
		fRules.put(Integer.valueOf(109), "delete_expression ::= dcolon_opt delete LeftBracket ] cast_expression");
		fRules.put(Integer.valueOf(79), "unary_expression ::= postfix_expression");
		fRules.put(Integer.valueOf(80), "unary_expression ::= new_expression");
		fRules.put(Integer.valueOf(81), "unary_expression ::= delete_expression");
		fRules.put(Integer.valueOf(82), "unary_expression ::= PlusPlus cast_expression");
		fRules.put(Integer.valueOf(83), "unary_expression ::= MinusMinus cast_expression");
		fRules.put(Integer.valueOf(84), "unary_expression ::= And cast_expression");
		fRules.put(Integer.valueOf(85), "unary_expression ::= Star cast_expression");
		fRules.put(Integer.valueOf(86), "unary_expression ::= Plus cast_expression");
		fRules.put(Integer.valueOf(87), "unary_expression ::= Minus cast_expression");
		fRules.put(Integer.valueOf(88), "unary_expression ::= Tilde cast_expression");
		fRules.put(Integer.valueOf(89), "unary_expression ::= Bang cast_expression");
		fRules.put(Integer.valueOf(90), "unary_expression ::= sizeof unary_expression");
		fRules.put(Integer.valueOf(91), "unary_expression ::= sizeof LeftParen type_id )");
		fRules.put(Integer.valueOf(110), "cast_expression ::= unary_expression");
		fRules.put(Integer.valueOf(111), "cast_expression ::= LeftParen type_id ) cast_expression");
		fRules.put(Integer.valueOf(112), "pm_expression ::= cast_expression");
		fRules.put(Integer.valueOf(113), "pm_expression ::= pm_expression DotStar cast_expression");
		fRules.put(Integer.valueOf(114), "pm_expression ::= pm_expression ArrowStar cast_expression");
		fRules.put(Integer.valueOf(115), "multiplicative_expression ::= pm_expression");
		fRules.put(Integer.valueOf(116), "multiplicative_expression ::= multiplicative_expression Star pm_expression");
		fRules.put(Integer.valueOf(117), "multiplicative_expression ::= multiplicative_expression Slash pm_expression");
		fRules.put(Integer.valueOf(118),
				"multiplicative_expression ::= multiplicative_expression Percent pm_expression");
		fRules.put(Integer.valueOf(119), "additive_expression ::= multiplicative_expression");
		fRules.put(Integer.valueOf(120), "additive_expression ::= additive_expression Plus multiplicative_expression");
		fRules.put(Integer.valueOf(121), "additive_expression ::= additive_expression Minus multiplicative_expression");
		fRules.put(Integer.valueOf(122), "shift_expression ::= additive_expression");
		fRules.put(Integer.valueOf(123), "shift_expression ::= shift_expression LeftShift additive_expression");
		fRules.put(Integer.valueOf(124), "shift_expression ::= shift_expression RightShift additive_expression");
		fRules.put(Integer.valueOf(125), "relational_expression ::= shift_expression");
		fRules.put(Integer.valueOf(126), "relational_expression ::= relational_expression LT shift_expression");
		fRules.put(Integer.valueOf(127), "relational_expression ::= relational_expression GT shift_expression");
		fRules.put(Integer.valueOf(128), "relational_expression ::= relational_expression LE shift_expression");
		fRules.put(Integer.valueOf(129), "relational_expression ::= relational_expression GE shift_expression");
		fRules.put(Integer.valueOf(130), "equality_expression ::= relational_expression");
		fRules.put(Integer.valueOf(131), "equality_expression ::= equality_expression EQ relational_expression");
		fRules.put(Integer.valueOf(132), "equality_expression ::= equality_expression NE relational_expression");
		fRules.put(Integer.valueOf(133), "and_expression ::= equality_expression");
		fRules.put(Integer.valueOf(134), "and_expression ::= and_expression And equality_expression");
		fRules.put(Integer.valueOf(135), "exclusive_or_expression ::= and_expression");
		fRules.put(Integer.valueOf(136), "exclusive_or_expression ::= exclusive_or_expression Caret and_expression");
		fRules.put(Integer.valueOf(137), "inclusive_or_expression ::= exclusive_or_expression");
		fRules.put(Integer.valueOf(138),
				"inclusive_or_expression ::= inclusive_or_expression Or exclusive_or_expression");
		fRules.put(Integer.valueOf(139), "logical_and_expression ::= inclusive_or_expression");
		fRules.put(Integer.valueOf(140),
				"logical_and_expression ::= logical_and_expression AndAnd inclusive_or_expression");
		fRules.put(Integer.valueOf(141), "logical_or_expression ::= logical_and_expression");
		fRules.put(Integer.valueOf(142), "logical_or_expression ::= logical_or_expression OrOr logical_and_expression");
		fRules.put(Integer.valueOf(143), "conditional_expression ::= logical_or_expression");
		fRules.put(Integer.valueOf(144),
				"conditional_expression ::= logical_or_expression Question expression Colon assignment_expression");
		fRules.put(Integer.valueOf(145), "throw_expression ::= throw");
		fRules.put(Integer.valueOf(146), "throw_expression ::= throw assignment_expression");
		fRules.put(Integer.valueOf(147), "assignment_expression ::= conditional_expression");
		fRules.put(Integer.valueOf(148), "assignment_expression ::= throw_expression");
		fRules.put(Integer.valueOf(149),
				"assignment_expression ::= logical_or_expression Assign assignment_expression");
		fRules.put(Integer.valueOf(150),
				"assignment_expression ::= logical_or_expression StarAssign assignment_expression");
		fRules.put(Integer.valueOf(151),
				"assignment_expression ::= logical_or_expression SlashAssign assignment_expression");
		fRules.put(Integer.valueOf(152),
				"assignment_expression ::= logical_or_expression PercentAssign assignment_expression");
		fRules.put(Integer.valueOf(153),
				"assignment_expression ::= logical_or_expression PlusAssign assignment_expression");
		fRules.put(Integer.valueOf(154),
				"assignment_expression ::= logical_or_expression MinusAssign assignment_expression");
		fRules.put(Integer.valueOf(155),
				"assignment_expression ::= logical_or_expression RightShiftAssign assignment_expression");
		fRules.put(Integer.valueOf(156),
				"assignment_expression ::= logical_or_expression LeftShiftAssign assignment_expression");
		fRules.put(Integer.valueOf(157),
				"assignment_expression ::= logical_or_expression AndAssign assignment_expression");
		fRules.put(Integer.valueOf(158),
				"assignment_expression ::= logical_or_expression CaretAssign assignment_expression");
		fRules.put(Integer.valueOf(159),
				"assignment_expression ::= logical_or_expression OrAssign assignment_expression");
		fRules.put(Integer.valueOf(162), "expression_list_actual ::= assignment_expression");
		fRules.put(Integer.valueOf(163),
				"expression_list_actual ::= expression_list_actual Comma assignment_expression");
		fRules.put(Integer.valueOf(161), "expression_list ::= <openscope-ast> expression_list_actual");
		fRules.put(Integer.valueOf(160), "expression ::= expression_list");
		fRules.put(Integer.valueOf(39), "template_opt ::= template");
		fRules.put(Integer.valueOf(40), "template_opt ::=");
		fRules.put(Integer.valueOf(51),
				"class_or_namespace_name_with_template ::= template_opt class_or_namespace_name");
		fRules.put(Integer.valueOf(49),
				"nested_name_specifier_with_template ::= class_or_namespace_name_with_template ColonColon nested_name_specifier_with_template");
		fRules.put(Integer.valueOf(50),
				"nested_name_specifier_with_template ::= class_or_namespace_name_with_template ColonColon");
		fRules.put(Integer.valueOf(164), "expression_list_opt ::= expression_list");
		fRules.put(Integer.valueOf(165), "expression_list_opt ::=");
		fRules.put(Integer.valueOf(78), "destructor_type_name ::= Tilde type_name");
		fRules.put(Integer.valueOf(75),
				"pseudo_destructor_name ::= dcolon_opt nested_name_specifier_opt type_name ColonColon destructor_type_name");
		fRules.put(Integer.valueOf(76),
				"pseudo_destructor_name ::= dcolon_opt nested_name_specifier template template_id_name ColonColon destructor_type_name");
		fRules.put(Integer.valueOf(77),
				"pseudo_destructor_name ::= dcolon_opt nested_name_specifier_opt destructor_type_name");
		fRules.put(Integer.valueOf(254), "storage_class_specifier ::= auto");
		fRules.put(Integer.valueOf(255), "storage_class_specifier ::= register");
		fRules.put(Integer.valueOf(256), "storage_class_specifier ::= static");
		fRules.put(Integer.valueOf(257), "storage_class_specifier ::= extern");
		fRules.put(Integer.valueOf(258), "storage_class_specifier ::= mutable");
		fRules.put(Integer.valueOf(259), "function_specifier ::= inline");
		fRules.put(Integer.valueOf(260), "function_specifier ::= virtual");
		fRules.put(Integer.valueOf(261), "function_specifier ::= explicit");
		fRules.put(Integer.valueOf(336), "cv_qualifier ::= const");
		fRules.put(Integer.valueOf(337), "cv_qualifier ::= volatile");
		fRules.put(Integer.valueOf(230), "no_type_declaration_specifier ::= storage_class_specifier");
		fRules.put(Integer.valueOf(231), "no_type_declaration_specifier ::= function_specifier");
		fRules.put(Integer.valueOf(232), "no_type_declaration_specifier ::= cv_qualifier");
		fRules.put(Integer.valueOf(233), "no_type_declaration_specifier ::= friend");
		fRules.put(Integer.valueOf(234), "no_type_declaration_specifier ::= typedef");
		fRules.put(Integer.valueOf(235), "no_type_declaration_specifiers ::= no_type_declaration_specifier");
		fRules.put(Integer.valueOf(236),
				"no_type_declaration_specifiers ::= no_type_declaration_specifiers no_type_declaration_specifier");
		fRules.put(Integer.valueOf(237), "simple_declaration_specifiers ::= simple_type_specifier");
		fRules.put(Integer.valueOf(238),
				"simple_declaration_specifiers ::= no_type_declaration_specifiers simple_type_specifier");
		fRules.put(Integer.valueOf(239),
				"simple_declaration_specifiers ::= simple_declaration_specifiers simple_type_specifier");
		fRules.put(Integer.valueOf(240),
				"simple_declaration_specifiers ::= simple_declaration_specifiers no_type_declaration_specifier");
		fRules.put(Integer.valueOf(241), "simple_declaration_specifiers ::= no_type_declaration_specifiers");
		fRules.put(Integer.valueOf(392), "class_keyword ::= class");
		fRules.put(Integer.valueOf(393), "class_keyword ::= struct");
		fRules.put(Integer.valueOf(394), "class_keyword ::= union");
		fRules.put(Integer.valueOf(386),
				"class_head ::= class_keyword identifier_name_opt <openscope-ast> base_clause_opt");
		fRules.put(Integer.valueOf(387),
				"class_head ::= class_keyword template_id_name <openscope-ast> base_clause_opt");
		fRules.put(Integer.valueOf(388),
				"class_head ::= class_keyword nested_name_specifier identifier_name <openscope-ast> base_clause_opt");
		fRules.put(Integer.valueOf(389),
				"class_head ::= class_keyword nested_name_specifier template_id_name <openscope-ast> base_clause_opt");
		fRules.put(Integer.valueOf(385),
				"class_specifier ::= class_head LeftBrace <openscope-ast> member_declaration_list_opt }");
		fRules.put(Integer.valueOf(242), "class_declaration_specifiers ::= class_specifier");
		fRules.put(Integer.valueOf(243),
				"class_declaration_specifiers ::= no_type_declaration_specifiers class_specifier");
		fRules.put(Integer.valueOf(244),
				"class_declaration_specifiers ::= class_declaration_specifiers no_type_declaration_specifier");
		fRules.put(Integer.valueOf(280),
				"elaborated_type_specifier ::= class_keyword dcolon_opt nested_name_specifier_opt identifier_name");
		fRules.put(Integer.valueOf(281),
				"elaborated_type_specifier ::= class_keyword dcolon_opt nested_name_specifier_opt template_opt template_id_name");
		fRules.put(Integer.valueOf(282),
				"elaborated_type_specifier ::= enum dcolon_opt nested_name_specifier_opt identifier_name");
		fRules.put(Integer.valueOf(245), "elaborated_declaration_specifiers ::= elaborated_type_specifier");
		fRules.put(Integer.valueOf(246),
				"elaborated_declaration_specifiers ::= no_type_declaration_specifiers elaborated_type_specifier");
		fRules.put(Integer.valueOf(247),
				"elaborated_declaration_specifiers ::= elaborated_declaration_specifiers no_type_declaration_specifier");
		fRules.put(Integer.valueOf(283), "enum_specifier ::= enum LeftBrace <openscope-ast> enumerator_list_opt }");
		fRules.put(Integer.valueOf(284),
				"enum_specifier ::= enum identifier_token LeftBrace <openscope-ast> enumerator_list_opt }");
		fRules.put(Integer.valueOf(248), "enum_declaration_specifiers ::= enum_specifier");
		fRules.put(Integer.valueOf(249),
				"enum_declaration_specifiers ::= no_type_declaration_specifiers enum_specifier");
		fRules.put(Integer.valueOf(250),
				"enum_declaration_specifiers ::= enum_declaration_specifiers no_type_declaration_specifier");
		fRules.put(Integer.valueOf(275), "type_name_specifier ::= type_name");
		fRules.put(Integer.valueOf(276), "type_name_specifier ::= dcolon_opt nested_name_specifier_opt type_name");
		fRules.put(Integer.valueOf(277),
				"type_name_specifier ::= dcolon_opt nested_name_specifier template template_id_name");
		fRules.put(Integer.valueOf(278),
				"type_name_specifier ::= typename dcolon_opt nested_name_specifier identifier_name");
		fRules.put(Integer.valueOf(279),
				"type_name_specifier ::= typename dcolon_opt nested_name_specifier template_opt template_id_name");
		fRules.put(Integer.valueOf(251), "type_name_declaration_specifiers ::= type_name_specifier");
		fRules.put(Integer.valueOf(252),
				"type_name_declaration_specifiers ::= no_type_declaration_specifiers type_name_specifier");
		fRules.put(Integer.valueOf(253),
				"type_name_declaration_specifiers ::= type_name_declaration_specifiers no_type_declaration_specifier");
		fRules.put(Integer.valueOf(223), "declaration_specifiers ::= <openscope-ast> simple_declaration_specifiers");
		fRules.put(Integer.valueOf(224), "declaration_specifiers ::= <openscope-ast> class_declaration_specifiers");
		fRules.put(Integer.valueOf(225),
				"declaration_specifiers ::= <openscope-ast> elaborated_declaration_specifiers");
		fRules.put(Integer.valueOf(226), "declaration_specifiers ::= <openscope-ast> enum_declaration_specifiers");
		fRules.put(Integer.valueOf(227), "declaration_specifiers ::= <openscope-ast> type_name_declaration_specifiers");
		fRules.put(Integer.valueOf(342), "type_specifier_seq ::= declaration_specifiers");
		fRules.put(Integer.valueOf(340), "type_id ::= type_specifier_seq");
		fRules.put(Integer.valueOf(341), "type_id ::= type_specifier_seq abstract_declarator");
		fRules.put(Integer.valueOf(94), "new_placement_opt ::= LeftParen expression_list )");
		fRules.put(Integer.valueOf(95), "new_placement_opt ::=");
		fRules.put(Integer.valueOf(96), "new_type_id ::= type_specifier_seq");
		fRules.put(Integer.valueOf(97), "new_type_id ::= type_specifier_seq new_declarator");
		fRules.put(Integer.valueOf(101), "new_array_expressions ::= LeftBracket expression ]");
		fRules.put(Integer.valueOf(102),
				"new_array_expressions ::= new_array_expressions LeftBracket constant_expression ]");
		fRules.put(Integer.valueOf(103), "new_array_expressions_opt ::= new_array_expressions");
		fRules.put(Integer.valueOf(104), "new_array_expressions_opt ::=");
		fRules.put(Integer.valueOf(105), "new_initializer ::= LeftParen expression_list_opt )");
		fRules.put(Integer.valueOf(106), "new_initializer_opt ::= new_initializer");
		fRules.put(Integer.valueOf(107), "new_initializer_opt ::=");
		fRules.put(Integer.valueOf(99), "new_pointer_operators ::= ptr_operator");
		fRules.put(Integer.valueOf(100), "new_pointer_operators ::= ptr_operator new_pointer_operators");
		fRules.put(Integer.valueOf(98), "new_declarator ::= <openscope-ast> new_pointer_operators");
		fRules.put(Integer.valueOf(168), "constant_expression ::= conditional_expression");
		fRules.put(Integer.valueOf(166), "expression_opt ::= expression");
		fRules.put(Integer.valueOf(167), "expression_opt ::=");
		fRules.put(Integer.valueOf(169), "constant_expression_opt ::= constant_expression");
		fRules.put(Integer.valueOf(170), "constant_expression_opt ::=");
		fRules.put(Integer.valueOf(180), "labeled_statement ::= identifier Colon statement");
		fRules.put(Integer.valueOf(181), "labeled_statement ::= case constant_expression Colon");
		fRules.put(Integer.valueOf(182), "labeled_statement ::= default Colon");
		fRules.put(Integer.valueOf(183), "expression_statement ::= expression ;");
		fRules.put(Integer.valueOf(184), "expression_statement ::= ;");
		fRules.put(Integer.valueOf(185), "compound_statement ::= LeftBrace <openscope-ast> statement_seq }");
		fRules.put(Integer.valueOf(186), "compound_statement ::= LeftBrace }");
		fRules.put(Integer.valueOf(189), "selection_statement ::= if LeftParen condition ) statement");
		fRules.put(Integer.valueOf(190), "selection_statement ::= if LeftParen condition ) statement else statement");
		fRules.put(Integer.valueOf(191), "selection_statement ::= switch LeftParen condition ) statement");
		fRules.put(Integer.valueOf(194), "iteration_statement ::= while LeftParen condition ) statement");
		fRules.put(Integer.valueOf(195), "iteration_statement ::= do statement while LeftParen expression ) ;");
		fRules.put(Integer.valueOf(196),
				"iteration_statement ::= for LeftParen expression_opt ; expression_opt ; expression_opt ) statement");
		fRules.put(Integer.valueOf(197),
				"iteration_statement ::= for LeftParen simple_declaration_with_declspec expression_opt ; expression_opt ) statement");
		fRules.put(Integer.valueOf(198), "jump_statement ::= break ;");
		fRules.put(Integer.valueOf(199), "jump_statement ::= continue ;");
		fRules.put(Integer.valueOf(200), "jump_statement ::= return expression ;");
		fRules.put(Integer.valueOf(201), "jump_statement ::= return ;");
		fRules.put(Integer.valueOf(202), "jump_statement ::= goto identifier_token ;");
		fRules.put(Integer.valueOf(203), "declaration_statement ::= block_declaration");
		fRules.put(Integer.valueOf(204), "declaration_statement ::= function_definition");
		fRules.put(Integer.valueOf(512), "try_block ::= try compound_statement <openscope-ast> handler_seq");
		fRules.put(Integer.valueOf(171), "statement ::= labeled_statement");
		fRules.put(Integer.valueOf(172), "statement ::= expression_statement");
		fRules.put(Integer.valueOf(173), "statement ::= compound_statement");
		fRules.put(Integer.valueOf(174), "statement ::= selection_statement");
		fRules.put(Integer.valueOf(175), "statement ::= iteration_statement");
		fRules.put(Integer.valueOf(176), "statement ::= jump_statement");
		fRules.put(Integer.valueOf(177), "statement ::= declaration_statement");
		fRules.put(Integer.valueOf(178), "statement ::= try_block");
		fRules.put(Integer.valueOf(179), "statement ::= ERROR_TOKEN");
		fRules.put(Integer.valueOf(187), "statement_seq ::= statement");
		fRules.put(Integer.valueOf(188), "statement_seq ::= statement_seq statement");
		fRules.put(Integer.valueOf(192), "condition ::= expression");
		fRules.put(Integer.valueOf(193), "condition ::= type_specifier_seq declarator Assign assignment_expression");
		fRules.put(Integer.valueOf(222),
				"simple_declaration_with_declspec ::= declaration_specifiers <openscope-ast> init_declarator_list_opt ;");
		fRules.put(Integer.valueOf(217), "declaration_seq ::= declaration");
		fRules.put(Integer.valueOf(218), "declaration_seq ::= declaration_seq declaration");
		fRules.put(Integer.valueOf(219), "declaration_seq_opt ::= declaration_seq");
		fRules.put(Integer.valueOf(220), "declaration_seq_opt ::=");
		fRules.put(Integer.valueOf(289), "enumerator_definition ::= identifier_token");
		fRules.put(Integer.valueOf(290), "enumerator_definition ::= identifier_token Assign constant_expression");
		fRules.put(Integer.valueOf(285), "enumerator_list ::= enumerator_definition");
		fRules.put(Integer.valueOf(286), "enumerator_list ::= enumerator_list Comma enumerator_definition");
		fRules.put(Integer.valueOf(287), "enumerator_list_opt ::= enumerator_list");
		fRules.put(Integer.valueOf(288), "enumerator_list_opt ::=");
		fRules.put(Integer.valueOf(291), "namespace_name ::= identifier_name");
		fRules.put(Integer.valueOf(301), "typename_opt ::= typename");
		fRules.put(Integer.valueOf(302), "typename_opt ::=");
		fRules.put(Integer.valueOf(375), "initializer ::= Assign initializer_clause");
		fRules.put(Integer.valueOf(376), "initializer ::= LeftParen expression_list )");
		fRules.put(Integer.valueOf(364), "parameter_declaration ::= declaration_specifiers parameter_init_declarator");
		fRules.put(Integer.valueOf(365), "parameter_declaration ::= declaration_specifiers");
		fRules.put(Integer.valueOf(358), "parameter_declaration_list ::= parameter_declaration");
		fRules.put(Integer.valueOf(359),
				"parameter_declaration_list ::= parameter_declaration_list Comma parameter_declaration");
		fRules.put(Integer.valueOf(360), "parameter_declaration_list_opt ::= parameter_declaration_list");
		fRules.put(Integer.valueOf(361), "parameter_declaration_list_opt ::=");
		fRules.put(Integer.valueOf(355), "parameter_declaration_clause ::= parameter_declaration_list_opt DotDotDot");
		fRules.put(Integer.valueOf(357), "parameter_declaration_clause ::= parameter_declaration_list Comma DotDotDot");
		fRules.put(Integer.valueOf(356), "parameter_declaration_clause ::= parameter_declaration_list_opt");
		fRules.put(Integer.valueOf(333), "cv_qualifier_seq ::= cv_qualifier cv_qualifier_seq_opt");
		fRules.put(Integer.valueOf(334), "cv_qualifier_seq_opt ::= cv_qualifier_seq");
		fRules.put(Integer.valueOf(335), "cv_qualifier_seq_opt ::=");
		fRules.put(Integer.valueOf(520), "exception_specification ::= throw LeftParen type_id_list )");
		fRules.put(Integer.valueOf(521), "exception_specification ::= throw LeftParen )");
		fRules.put(Integer.valueOf(522), "exception_specification_opt ::= exception_specification");
		fRules.put(Integer.valueOf(523), "exception_specification_opt ::=");
		fRules.put(Integer.valueOf(326), "array_modifier ::= LeftBracket constant_expression ]");
		fRules.put(Integer.valueOf(327), "array_modifier ::= LeftBracket ]");
		fRules.put(Integer.valueOf(349), "basic_direct_abstract_declarator ::= LeftParen abstract_declarator )");
		fRules.put(Integer.valueOf(350), "array_direct_abstract_declarator ::= array_modifier");
		fRules.put(Integer.valueOf(351),
				"array_direct_abstract_declarator ::= array_direct_abstract_declarator array_modifier");
		fRules.put(Integer.valueOf(352),
				"array_direct_abstract_declarator ::= basic_direct_abstract_declarator array_modifier");
		fRules.put(Integer.valueOf(353),
				"function_direct_abstract_declarator ::= basic_direct_abstract_declarator LeftParen <openscope-ast> parameter_declaration_clause ) <openscope-ast> cv_qualifier_seq_opt <openscope-ast> exception_specification_opt");
		fRules.put(Integer.valueOf(354),
				"function_direct_abstract_declarator ::= LeftParen <openscope-ast> parameter_declaration_clause ) <openscope-ast> cv_qualifier_seq_opt <openscope-ast> exception_specification_opt");
		fRules.put(Integer.valueOf(346), "direct_abstract_declarator ::= basic_direct_abstract_declarator");
		fRules.put(Integer.valueOf(347), "direct_abstract_declarator ::= array_direct_abstract_declarator");
		fRules.put(Integer.valueOf(348), "direct_abstract_declarator ::= function_direct_abstract_declarator");
		fRules.put(Integer.valueOf(343), "abstract_declarator ::= direct_abstract_declarator");
		fRules.put(Integer.valueOf(344), "abstract_declarator ::= <openscope-ast> ptr_operator_seq");
		fRules.put(Integer.valueOf(345),
				"abstract_declarator ::= <openscope-ast> ptr_operator_seq direct_abstract_declarator");
		fRules.put(Integer.valueOf(362), "abstract_declarator_opt ::= abstract_declarator");
		fRules.put(Integer.valueOf(363), "abstract_declarator_opt ::=");
		fRules.put(Integer.valueOf(366), "parameter_init_declarator ::= declarator");
		fRules.put(Integer.valueOf(367), "parameter_init_declarator ::= declarator Assign parameter_initializer");
		fRules.put(Integer.valueOf(368), "parameter_init_declarator ::= abstract_declarator");
		fRules.put(Integer.valueOf(369),
				"parameter_init_declarator ::= abstract_declarator Assign parameter_initializer");
		fRules.put(Integer.valueOf(370), "parameter_init_declarator ::= Assign parameter_initializer");
		fRules.put(Integer.valueOf(371), "parameter_initializer ::= assignment_expression");
		fRules.put(Integer.valueOf(435), "ctor_initializer_list ::= Colon mem_initializer_list");
		fRules.put(Integer.valueOf(436), "ctor_initializer_list_opt ::= ctor_initializer_list");
		fRules.put(Integer.valueOf(437), "ctor_initializer_list_opt ::=");
		fRules.put(Integer.valueOf(374), "function_body ::= compound_statement");
		fRules.put(Integer.valueOf(515), "handler ::= catch LeftParen exception_declaration ) compound_statement");
		fRules.put(Integer.valueOf(516), "handler ::= catch LeftParen DotDotDot ) compound_statement");
		fRules.put(Integer.valueOf(513), "handler_seq ::= handler");
		fRules.put(Integer.valueOf(514), "handler_seq ::= handler_seq handler");
		fRules.put(Integer.valueOf(377), "initializer_clause ::= assignment_expression");
		fRules.put(Integer.valueOf(378), "initializer_clause ::= LeftBrace <openscope-ast> initializer_list Comma }");
		fRules.put(Integer.valueOf(379), "initializer_clause ::= LeftBrace <openscope-ast> initializer_list }");
		fRules.put(Integer.valueOf(380), "initializer_clause ::= LeftBrace <openscope-ast> }");
		fRules.put(Integer.valueOf(381), "initializer_list ::= initializer_clause");
		fRules.put(Integer.valueOf(382), "initializer_list ::= initializer_list Comma initializer_clause");
		fRules.put(Integer.valueOf(415), "bit_field_declarator ::= identifier_name");
		fRules.put(Integer.valueOf(411), "member_declarator ::= declarator");
		fRules.put(Integer.valueOf(412), "member_declarator ::= declarator constant_initializer");
		fRules.put(Integer.valueOf(413), "member_declarator ::= bit_field_declarator Colon constant_expression");
		fRules.put(Integer.valueOf(414), "member_declarator ::= Colon constant_expression");
		fRules.put(Integer.valueOf(409), "member_declarator_list ::= member_declarator");
		fRules.put(Integer.valueOf(410), "member_declarator_list ::= member_declarator_list Comma member_declarator");
		fRules.put(Integer.valueOf(426), "access_specifier_keyword ::= private");
		fRules.put(Integer.valueOf(427), "access_specifier_keyword ::= protected");
		fRules.put(Integer.valueOf(428), "access_specifier_keyword ::= public");
		fRules.put(Integer.valueOf(395), "visibility_label ::= access_specifier_keyword Colon");
		fRules.put(Integer.valueOf(396),
				"member_declaration ::= declaration_specifiers_opt <openscope-ast> member_declarator_list ;");
		fRules.put(Integer.valueOf(397), "member_declaration ::= declaration_specifiers_opt ;");
		fRules.put(Integer.valueOf(398), "member_declaration ::= function_definition ;");
		fRules.put(Integer.valueOf(399), "member_declaration ::= function_definition");
		fRules.put(Integer.valueOf(400),
				"member_declaration ::= dcolon_opt nested_name_specifier template_opt unqualified_id_name ;");
		fRules.put(Integer.valueOf(401), "member_declaration ::= using_declaration");
		fRules.put(Integer.valueOf(402), "member_declaration ::= template_declaration");
		fRules.put(Integer.valueOf(403), "member_declaration ::= visibility_label");
		fRules.put(Integer.valueOf(404), "member_declaration ::= ERROR_TOKEN");
		fRules.put(Integer.valueOf(405), "member_declaration_list ::= member_declaration");
		fRules.put(Integer.valueOf(406), "member_declaration_list ::= member_declaration_list member_declaration");
		fRules.put(Integer.valueOf(407), "member_declaration_list_opt ::= member_declaration_list");
		fRules.put(Integer.valueOf(408), "member_declaration_list_opt ::=");
		fRules.put(Integer.valueOf(390), "identifier_name_opt ::= identifier_name");
		fRules.put(Integer.valueOf(391), "identifier_name_opt ::=");
		fRules.put(Integer.valueOf(417), "base_clause ::= Colon base_specifier_list");
		fRules.put(Integer.valueOf(418), "base_clause_opt ::= base_clause");
		fRules.put(Integer.valueOf(419), "base_clause_opt ::=");
		fRules.put(Integer.valueOf(416), "constant_initializer ::= Assign constant_expression");
		fRules.put(Integer.valueOf(422), "base_specifier ::= dcolon_opt nested_name_specifier_opt class_name");
		fRules.put(Integer.valueOf(423),
				"base_specifier ::= virtual access_specifier_keyword_opt dcolon_opt nested_name_specifier_opt class_name");
		fRules.put(Integer.valueOf(424),
				"base_specifier ::= access_specifier_keyword virtual dcolon_opt nested_name_specifier_opt class_name");
		fRules.put(Integer.valueOf(425),
				"base_specifier ::= access_specifier_keyword dcolon_opt nested_name_specifier_opt class_name");
		fRules.put(Integer.valueOf(420), "base_specifier_list ::= base_specifier");
		fRules.put(Integer.valueOf(421), "base_specifier_list ::= base_specifier_list Comma base_specifier");
		fRules.put(Integer.valueOf(429), "access_specifier_keyword_opt ::= access_specifier_keyword");
		fRules.put(Integer.valueOf(430), "access_specifier_keyword_opt ::=");
		fRules.put(Integer.valueOf(432), "conversion_type_id ::= type_specifier_seq conversion_declarator");
		fRules.put(Integer.valueOf(433), "conversion_type_id ::= type_specifier_seq");
		fRules.put(Integer.valueOf(434), "conversion_declarator ::= <openscope-ast> ptr_operator_seq");
		fRules.put(Integer.valueOf(441), "mem_initializer_name ::= dcolon_opt nested_name_specifier_opt class_name");
		fRules.put(Integer.valueOf(442), "mem_initializer_name ::= identifier_name");
		fRules.put(Integer.valueOf(440), "mem_initializer ::= mem_initializer_name LeftParen expression_list_opt )");
		fRules.put(Integer.valueOf(438), "mem_initializer_list ::= mem_initializer");
		fRules.put(Integer.valueOf(439), "mem_initializer_list ::= mem_initializer Comma mem_initializer_list");
		fRules.put(Integer.valueOf(507), "template_argument ::= assignment_expression");
		fRules.put(Integer.valueOf(508), "template_argument ::= type_id");
		fRules.put(Integer.valueOf(509), "template_argument ::= qualified_or_unqualified_name");
		fRules.put(Integer.valueOf(503), "template_argument_list ::= template_argument");
		fRules.put(Integer.valueOf(504), "template_argument_list ::= template_argument_list Comma template_argument");
		fRules.put(Integer.valueOf(505), "template_argument_list_opt ::= template_argument_list");
		fRules.put(Integer.valueOf(506), "template_argument_list_opt ::=");
		fRules.put(Integer.valueOf(446), "overloadable_operator ::= new");
		fRules.put(Integer.valueOf(447), "overloadable_operator ::= delete");
		fRules.put(Integer.valueOf(448), "overloadable_operator ::= new LeftBracket ]");
		fRules.put(Integer.valueOf(449), "overloadable_operator ::= delete LeftBracket ]");
		fRules.put(Integer.valueOf(450), "overloadable_operator ::= Plus");
		fRules.put(Integer.valueOf(451), "overloadable_operator ::= Minus");
		fRules.put(Integer.valueOf(452), "overloadable_operator ::= Star");
		fRules.put(Integer.valueOf(453), "overloadable_operator ::= Slash");
		fRules.put(Integer.valueOf(454), "overloadable_operator ::= Percent");
		fRules.put(Integer.valueOf(455), "overloadable_operator ::= Caret");
		fRules.put(Integer.valueOf(456), "overloadable_operator ::= And");
		fRules.put(Integer.valueOf(457), "overloadable_operator ::= Or");
		fRules.put(Integer.valueOf(458), "overloadable_operator ::= Tilde");
		fRules.put(Integer.valueOf(459), "overloadable_operator ::= Bang");
		fRules.put(Integer.valueOf(460), "overloadable_operator ::= Assign");
		fRules.put(Integer.valueOf(461), "overloadable_operator ::= LT");
		fRules.put(Integer.valueOf(462), "overloadable_operator ::= GT");
		fRules.put(Integer.valueOf(463), "overloadable_operator ::= PlusAssign");
		fRules.put(Integer.valueOf(464), "overloadable_operator ::= MinusAssign");
		fRules.put(Integer.valueOf(465), "overloadable_operator ::= StarAssign");
		fRules.put(Integer.valueOf(466), "overloadable_operator ::= SlashAssign");
		fRules.put(Integer.valueOf(467), "overloadable_operator ::= PercentAssign");
		fRules.put(Integer.valueOf(468), "overloadable_operator ::= CaretAssign");
		fRules.put(Integer.valueOf(469), "overloadable_operator ::= AndAssign");
		fRules.put(Integer.valueOf(470), "overloadable_operator ::= OrAssign");
		fRules.put(Integer.valueOf(471), "overloadable_operator ::= LeftShift");
		fRules.put(Integer.valueOf(472), "overloadable_operator ::= RightShift");
		fRules.put(Integer.valueOf(473), "overloadable_operator ::= RightShiftAssign");
		fRules.put(Integer.valueOf(474), "overloadable_operator ::= LeftShiftAssign");
		fRules.put(Integer.valueOf(475), "overloadable_operator ::= EQ");
		fRules.put(Integer.valueOf(476), "overloadable_operator ::= NE");
		fRules.put(Integer.valueOf(477), "overloadable_operator ::= LE");
		fRules.put(Integer.valueOf(478), "overloadable_operator ::= GE");
		fRules.put(Integer.valueOf(479), "overloadable_operator ::= AndAnd");
		fRules.put(Integer.valueOf(480), "overloadable_operator ::= OrOr");
		fRules.put(Integer.valueOf(481), "overloadable_operator ::= PlusPlus");
		fRules.put(Integer.valueOf(482), "overloadable_operator ::= MinusMinus");
		fRules.put(Integer.valueOf(483), "overloadable_operator ::= Comma");
		fRules.put(Integer.valueOf(484), "overloadable_operator ::= ArrowStar");
		fRules.put(Integer.valueOf(485), "overloadable_operator ::= Arrow");
		fRules.put(Integer.valueOf(486), "overloadable_operator ::= LeftParen )");
		fRules.put(Integer.valueOf(487), "overloadable_operator ::= LeftBracket ]");
		fRules.put(Integer.valueOf(494), "template_parameter ::= parameter_declaration");
		fRules.put(Integer.valueOf(491), "template_parameter_list ::= template_parameter");
		fRules.put(Integer.valueOf(492),
				"template_parameter_list ::= template_parameter_list Comma template_parameter");
		fRules.put(Integer.valueOf(495), "type_parameter ::= class identifier_name_opt");
		fRules.put(Integer.valueOf(496), "type_parameter ::= class identifier_name_opt Assign type_id");
		fRules.put(Integer.valueOf(497), "type_parameter ::= typename identifier_name_opt");
		fRules.put(Integer.valueOf(498), "type_parameter ::= typename identifier_name_opt Assign type_id");
		fRules.put(Integer.valueOf(499),
				"type_parameter ::= template LT <openscope-ast> template_parameter_list GT class identifier_name_opt");
		fRules.put(Integer.valueOf(500),
				"type_parameter ::= template LT <openscope-ast> template_parameter_list GT class identifier_name_opt Assign id_expression");
		fRules.put(Integer.valueOf(517), "exception_declaration ::= type_specifier_seq <openscope-ast> declarator");
		fRules.put(Integer.valueOf(518),
				"exception_declaration ::= type_specifier_seq <openscope-ast> abstract_declarator");
		fRules.put(Integer.valueOf(519), "exception_declaration ::= type_specifier_seq");
		fRules.put(Integer.valueOf(524), "type_id_list ::= type_id");
		fRules.put(Integer.valueOf(525), "type_id_list ::= type_id_list Comma type_id");

	}

	public static String lookup(int ruleNumber) {
		return (String) fRules.get(Integer.valueOf(ruleNumber));
	}
}
