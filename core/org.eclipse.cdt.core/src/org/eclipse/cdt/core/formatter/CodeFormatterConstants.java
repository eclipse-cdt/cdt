/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Sergey Prigogin, Google
 *******************************************************************************/
package org.eclipse.cdt.core.formatter;

import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.cdt.internal.formatter.align.Alignment;

/**
 */
public class CodeFormatterConstants {

	/**
	 * <pre>
	 * FORMATTER / Option for alignment of arguments in allocation expression
	 *     - option id:         "org.eclipse.cdt.core.formatter.language"
	 *     - possible values:   values proposed in class <code>ParserLanguage</code> 
	 *     - default:           ParserLanguage.CPP
	 * </pre>
	 */
	public static final String FORMATTER_LANGUAGE = CCorePlugin.PLUGIN_ID + ".formatter.language";	 //$NON-NLS-1$
	
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of arguments in allocation expression
	 *     - option id:         "org.eclipse.cdt.core.formatter.current_file"
	 *     - possible values:   object of class <code>IFile</code> or <code>null</code> 
	 *     - default:           null
	 * </pre>
	 */
	public static final String FORMATTER_CURRENT_FILE = CCorePlugin.PLUGIN_ID + ".formatter.current_file";	 //$NON-NLS-1$
	
	/**
	 * <pre>
	 * FORMATTER / Value to set a brace location at the end of a line.
	 * </pre>
	 * @see #FORMATTER_BRACE_POSITION_FOR_ANONYMOUS_TYPE_DECLARATION
	 * @see #FORMATTER_BRACE_POSITION_FOR_ARRAY_INITIALIZER
	 * @see #FORMATTER_BRACE_POSITION_FOR_BLOCK
	 * @see #FORMATTER_BRACE_POSITION_FOR_CONSTRUCTOR_DECLARATION
 	 * @see #FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION
 	 * @see #FORMATTER_BRACE_POSITION_FOR_SWITCH
	 * @see #FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION
	 */
	public static final String END_OF_LINE = "end_of_line";	//$NON-NLS-1$

	/**
	 * <pre>
	 * FORMATTER / Value to set an option to false.
	 * </pre>
	 */
	public static final String FALSE = "false"; //$NON-NLS-1$
	
	/**
	 * <pre>
	 * FORMATTER / Option to align type members of a type declaration on column
	 *     - option id:         "org.eclipse.cdt.core.formatter.formatter.align_type_members_on_columns"
	 *     - possible values:   { TRUE, FALSE }
	 *     - default:           FALSE
	 * </pre>
	 * @see #TRUE
	 * @see #FALSE
	 */
	public static final String FORMATTER_ALIGN_TYPE_MEMBERS_ON_COLUMNS = CCorePlugin.PLUGIN_ID + ".formatter.align_type_members_on_columns";	 //$NON-NLS-1$

	/**
	 * <pre>
	 * FORMATTER / Option for alignment of arguments in allocation expression
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_arguments_in_allocation_expression"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_ALLOCATION_EXPRESSION = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_arguments_in_allocation_expression";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of arguments in enum constant
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_arguments_in_enum_constant"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_ENUM_CONSTANT = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_arguments_in_enum_constant";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of arguments in explicit constructor call
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_arguments_in_explicit_constructor_call"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_EXPLICIT_CONSTRUCTOR_CALL = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_arguments_in_explicit_constructor_call";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of arguments in method invocation
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_arguments_in_method_invocation"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_arguments_in_method_invocation";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of arguments in qualified allocation expression
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_arguments_in_qualified_allocation_expression"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_QUALIFIED_ALLOCATION_EXPRESSION = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_arguments_in_qualified_allocation_expression";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of assignment
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_assignment"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, M_NO_ALIGNMENT, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_ASSIGNMENT  = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_assignment";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of binary expression
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_binary_expression"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_BINARY_EXPRESSION = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_binary_expression";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of compact if
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_compact_if"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_ONE_PER_LINE, INDENT_BY_ONE)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_COMPACT_IF = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_compact_if";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of conditional expression
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_conditional_expression"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_ONE_PER_LINE, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_CONDITIONAL_EXPRESSION = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_conditional_expression";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of enum constants
	 *     - option id:        "org.eclipse.cdt.core.formatter.alignment_for_enum_constants"
	 *     - possible values:  values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:          createAlignmentValue(false, WRAP_NO_SPLIT, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_enum_constants";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of expressions in array initializer
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_expressions_in_array_initializer"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_EXPRESSIONS_IN_ARRAY_INITIALIZER = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_expressions_in_array_initializer";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of multiple fields
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_multiple_fields"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_MULTIPLE_FIELDS = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_multiple_fields";//$NON-NLS-1$	
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of parameters in constructor declaration
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_parameters_in_constructor_declaration"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_CONSTRUCTOR_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_parameters_in_constructor_declaration";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of parameters in method declaration
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_parameters_in_method_declaration"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_PARAMETERS_IN_METHOD_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_parameters_in_method_declaration";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of selector in method invocation
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_selector_in_method_invocation"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_SELECTOR_IN_METHOD_INVOCATION = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_selector_in_method_invocation";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of superclass in type declaration
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_superclass_in_type_declaration"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_NEXT_SHIFTED, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_SUPERCLASS_IN_TYPE_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_superclass_in_type_declaration";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of superinterfaces in enum declaration
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_superinterfaces_in_enum_declaration"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_SUPERINTERFACES_IN_ENUM_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_superinterfaces_in_enum_declaration";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of superinterfaces in type declaration
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_superinterfaces_in_type_declaration"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_SUPERINTERFACES_IN_TYPE_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_superinterfaces_in_type_declaration";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of throws clause in constructor declaration
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_throws_clause_in_constructor_declaration"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_THROWS_CLAUSE_IN_CONSTRUCTOR_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_throws_clause_in_constructor_declaration";	 //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option for alignment of throws clause in method declaration
	 *     - option id:         "org.eclipse.cdt.core.formatter.alignment_for_throws_clause_in_method_declaration"
	 *     - possible values:   values returned by <code>createAlignmentValue(boolean, int, int)</code> call
	 *     - default:           createAlignmentValue(false, WRAP_COMPACT, INDENT_DEFAULT)
	 * </pre>
	 * @see #createAlignmentValue(boolean, int, int)
	 */
	public static final String FORMATTER_ALIGNMENT_FOR_THROWS_CLAUSE_IN_METHOD_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.alignment_for_throws_clause_in_method_declaration";	 //$NON-NLS-1$

//	/**
//	 * <pre>
//	 * FORMATTER / Option to add blank lines after the imports declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.blank_lines_after_imports"
//	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
//	 *     - default:           "0"
//	 * </pre>
//	 */
//	public static final String FORMATTER_BLANK_LINES_AFTER_IMPORTS = CCorePlugin.PLUGIN_ID + ".formatter.blank_lines_after_imports";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to add blank lines after the package declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.blank_lines_after_package"
//	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
//	 *     - default:           "0"
//	 * </pre>
//	 */
//	public static final String FORMATTER_BLANK_LINES_AFTER_PACKAGE = CCorePlugin.PLUGIN_ID + ".formatter.blank_lines_after_package";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to add blank lines at the beginning of the method body
//	 *     - option id:         "org.eclipse.cdt.core.formatter.number_of_blank_lines_at_beginning_of_method_body"
//	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
//	 *     - default:           "0"
//	 * </pre>
//	 */
//	public static final String FORMATTER_BLANK_LINES_AT_BEGINNING_OF_METHOD_BODY = CCorePlugin.PLUGIN_ID + ".formatter.number_of_blank_lines_at_beginning_of_method_body"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to add blank lines before a field declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.blank_lines_before_field"
//	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
//	 *     - default:           "0"
//	 * </pre>
//	 */
//	public static final String FORMATTER_BLANK_LINES_BEFORE_FIELD = CCorePlugin.PLUGIN_ID + ".formatter.blank_lines_before_field";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to add blank lines before the first class body declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.blank_lines_before_first_class_body_declaration"
//	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
//	 *     - default:           "0"
//	 * </pre>
//	 */
//	public static final String FORMATTER_BLANK_LINES_BEFORE_FIRST_CLASS_BODY_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.blank_lines_before_first_class_body_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to add blank lines before the imports declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.blank_lines_before_imports"
//	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
//	 *     - default:           "0"
//	 * </pre>
//	 */
//	public static final String FORMATTER_BLANK_LINES_BEFORE_IMPORTS = CCorePlugin.PLUGIN_ID + ".formatter.blank_lines_before_imports";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to add blank lines before a member type declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.blank_lines_before_member_type"
//	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
//	 *     - default:           "0"
//	 * </pre>
//	 */
//	public static final String FORMATTER_BLANK_LINES_BEFORE_MEMBER_TYPE = CCorePlugin.PLUGIN_ID + ".formatter.blank_lines_before_member_type";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to add blank lines before a method declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.blank_lines_before_method"
//	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
//	 *     - default:           "0"
//	 * </pre>
//	 */
//	public static final String FORMATTER_BLANK_LINES_BEFORE_METHOD = CCorePlugin.PLUGIN_ID + ".formatter.blank_lines_before_method";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to add blank lines before a new chunk
//	 *     - option id:         "org.eclipse.cdt.core.formatter.blank_lines_before_new_chunk"
//	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
//	 *     - default:           "0"
//	 * </pre>
//	 */
//	public static final String FORMATTER_BLANK_LINES_BEFORE_NEW_CHUNK = CCorePlugin.PLUGIN_ID + ".formatter.blank_lines_before_new_chunk";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to add blank lines before the package declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.blank_lines_before_package"
//	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
//	 *     - default:           "0"
//	 * </pre>
//	 */
//	public static final String FORMATTER_BLANK_LINES_BEFORE_PACKAGE = CCorePlugin.PLUGIN_ID + ".formatter.blank_lines_before_package";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to add blank lines between type declarations
//	 *     - option id:         "org.eclipse.cdt.core.formatter.blank_lines_between_type_declarations"
//	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
//	 *     - default:           "0"
//	 * </pre>
//	 */
//	public static final String FORMATTER_BLANK_LINES_BETWEEN_TYPE_DECLARATIONS = CCorePlugin.PLUGIN_ID + ".formatter.blank_lines_between_type_declarations";	//$NON-NLS-1$

	/**
	 * <pre>
	 * FORMATTER / Option to position the braces of an annotation type declaration
	 *     - option id:         "org.eclipse.cdt.core.formatter.brace_position_for_annotation_type_declaration"
	 *     - possible values:   { END_OF_LINE, NEXT_LINE, NEXT_LINE_SHIFTED, NEXT_LINE_ON_WRAP }
	 *     - default:           END_OF_LINE
	 * </pre>
	 * @see #END_OF_LINE
	 * @see #NEXT_LINE
	 * @see #NEXT_LINE_SHIFTED
	 * @see #NEXT_LINE_ON_WRAP
	 */
	public static final String FORMATTER_BRACE_POSITION_FOR_ARRAY_INITIALIZER = CCorePlugin.PLUGIN_ID + ".formatter.brace_position_for_array_initializer";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to position the braces of a block
	 *     - option id:         "org.eclipse.cdt.core.formatter.brace_position_for_block"
	 *     - possible values:   { END_OF_LINE, NEXT_LINE, NEXT_LINE_SHIFTED, NEXT_LINE_ON_WRAP }
	 *     - default:           END_OF_LINE
	 * </pre>
	 * @see #END_OF_LINE
	 * @see #NEXT_LINE
	 * @see #NEXT_LINE_SHIFTED
	 * @see #NEXT_LINE_ON_WRAP
	 */
	public static final String FORMATTER_BRACE_POSITION_FOR_BLOCK = CCorePlugin.PLUGIN_ID + ".formatter.brace_position_for_block";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to position the braces of a block in a case statement when the block is the first statement following
	 *             the case
	 *     - option id:         "org.eclipse.cdt.core.formatter.brace_position_for_block_in_case"
	 *     - possible values:   { END_OF_LINE, NEXT_LINE, NEXT_LINE_SHIFTED, NEXT_LINE_ON_WRAP }
	 *     - default:           END_OF_LINE
	 * </pre>
	 * @see #END_OF_LINE
	 * @see #NEXT_LINE
	 * @see #NEXT_LINE_SHIFTED
	 * @see #NEXT_LINE_ON_WRAP
	 */
	public static final String FORMATTER_BRACE_POSITION_FOR_BLOCK_IN_CASE = CCorePlugin.PLUGIN_ID + ".formatter.brace_position_for_block_in_case";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to position the braces of a constructor declaration
	 *     - option id:         "org.eclipse.cdt.core.formatter.brace_position_for_constructor_declaration"
	 *     - possible values:   { END_OF_LINE, NEXT_LINE, NEXT_LINE_SHIFTED, NEXT_LINE_ON_WRAP }
	 *     - default:           END_OF_LINE
	 * </pre>
	 * @see #END_OF_LINE
	 * @see #NEXT_LINE
	 * @see #NEXT_LINE_SHIFTED
	 * @see #NEXT_LINE_ON_WRAP
	 */
	public static final String FORMATTER_BRACE_POSITION_FOR_CONSTRUCTOR_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.brace_position_for_constructor_declaration";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to position the braces of an enum constant
	 *     - option id:         "org.eclipse.cdt.core.formatter.brace_position_for_enum_constant"
	 *     - possible values:   { END_OF_LINE, NEXT_LINE, NEXT_LINE_SHIFTED, NEXT_LINE_ON_WRAP }
	 *     - default:           END_OF_LINE
	 * </pre>
	 * @see #END_OF_LINE
	 * @see #NEXT_LINE
	 * @see #NEXT_LINE_SHIFTED
	 * @see #NEXT_LINE_ON_WRAP
	 */
	public static final String FORMATTER_BRACE_POSITION_FOR_ENUM_CONSTANT = CCorePlugin.PLUGIN_ID + ".formatter.brace_position_for_enum_constant";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to position the braces of an enum declaration
	 *     - option id:         "org.eclipse.cdt.core.formatter.brace_position_for_enum_declaration"
	 *     - possible values:   { END_OF_LINE, NEXT_LINE, NEXT_LINE_SHIFTED, NEXT_LINE_ON_WRAP }
	 *     - default:           END_OF_LINE
	 * </pre>
	 * @see #END_OF_LINE
	 * @see #NEXT_LINE
	 * @see #NEXT_LINE_SHIFTED
	 * @see #NEXT_LINE_ON_WRAP
	 */
	public static final String FORMATTER_BRACE_POSITION_FOR_ENUM_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.brace_position_for_enum_declaration";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to position the braces of a method declaration
	 *     - option id:         "org.eclipse.cdt.core.formatter.brace_position_for_method_declaration"
	 *     - possible values:   { END_OF_LINE, NEXT_LINE, NEXT_LINE_SHIFTED, NEXT_LINE_ON_WRAP }
	 *     - default:           END_OF_LINE
	 * </pre>
	 * @see #END_OF_LINE
	 * @see #NEXT_LINE
	 * @see #NEXT_LINE_SHIFTED
	 * @see #NEXT_LINE_ON_WRAP
	 */
	public static final String FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.brace_position_for_method_declaration";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to position the braces of a switch statement
	 *     - option id:         "org.eclipse.cdt.core.formatter.brace_position_for_switch"
	 *     - possible values:   { END_OF_LINE, NEXT_LINE, NEXT_LINE_SHIFTED, NEXT_LINE_ON_WRAP }
	 *     - default:           END_OF_LINE
	 * </pre>
	 * @see #END_OF_LINE
	 * @see #NEXT_LINE
	 * @see #NEXT_LINE_SHIFTED
	 * @see #NEXT_LINE_ON_WRAP
	 */
	public static final String FORMATTER_BRACE_POSITION_FOR_SWITCH = CCorePlugin.PLUGIN_ID + ".formatter.brace_position_for_switch";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to position the braces of a type declaration
	 *     - option id:         "org.eclipse.cdt.core.formatter.brace_position_for_type_declaration"
	 *     - possible values:   { END_OF_LINE, NEXT_LINE, NEXT_LINE_SHIFTED, NEXT_LINE_ON_WRAP }
	 *     - default:           END_OF_LINE
	 * </pre>
	 * @see #END_OF_LINE
	 * @see #NEXT_LINE
	 * @see #NEXT_LINE_SHIFTED
	 * @see #NEXT_LINE_ON_WRAP
	 */
	public static final String FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.brace_position_for_type_declaration";	//$NON-NLS-1$

//	/**
//	 * <pre>
//	 * FORMATTER / Option to control whether blank lines are cleared inside comments
//	 *     - option id:         "org.eclipse.cdt.core.formatter.comment.clear_blank_lines"
//	 *     - possible values:   { TRUE, FALSE }
//	 *     - default:           FALSE
//	 * </pre>
//	 * @see #TRUE
//	 * @see #FALSE
//	 */	
//	public final static String FORMATTER_COMMENT_CLEAR_BLANK_LINES = CCorePlugin.PLUGIN_ID + ".formatter.comment.clear_blank_lines"; //$NON-NLS-1$
	
//	/**
//	 * <pre>
//	 * FORMATTER / Option to control whether comments are formatted
//	 *     - option id:         "org.eclipse.cdt.core.formatter.comment.format_comments"
//	 *     - possible values:   { TRUE, FALSE }
//	 *     - default:           TRUE
//	 * </pre>
//	 * @see #TRUE
//	 * @see #FALSE
//	 */	
//	public final static String FORMATTER_COMMENT_FORMAT = CCorePlugin.PLUGIN_ID + ".formatter.comment.format_comments"; //$NON-NLS-1$

//	/**
//	 * <pre>
//	 * FORMATTER / Option to control whether the header comment of a C/C++ source file is formatted
//	 *     - option id:         "org.eclipse.cdt.core.formatter.comment.format_header"
//	 *     - possible values:   { TRUE, FALSE }
//	 *     - default:           FALSE
//	 * </pre>
//	 * @see #TRUE
//	 * @see #FALSE
//	 */	
//	public final static String FORMATTER_COMMENT_FORMAT_HEADER = CCorePlugin.PLUGIN_ID + ".formatter.comment.format_header"; //$NON-NLS-1$

//	/**
//	 * <pre>
//	 * FORMATTER / Option to control whether HTML tags are formatted.
//	 *     - option id:         "org.eclipse.cdt.core.formatter.comment.format_html"
//	 *     - possible values:   { TRUE, FALSE }
//	 *     - default:           TRUE
//	 * </pre>
//	 * @see #TRUE
//	 * @see #FALSE
//	 */	
//	public final static String FORMATTER_COMMENT_FORMAT_HTML = CCorePlugin.PLUGIN_ID + ".formatter.comment.format_html"; //$NON-NLS-1$

//	/**
//	 * <pre>
//	 * FORMATTER / Option to control whether code snippets are formatted in comments
//	 *     - option id:         "org.eclipse.cdt.core.formatter.comment.format_source_code"
//	 *     - possible values:   { TRUE, FALSE }
//	 *     - default:           TRUE
//	 * </pre>
//	 * @see #TRUE
//	 * @see #FALSE
//	 */	
//	public final static String FORMATTER_COMMENT_FORMAT_SOURCE = CCorePlugin.PLUGIN_ID + ".formatter.comment.format_source_code"; //$NON-NLS-1$
	
//	/**
//	 * <pre>
//	 * FORMATTER / Option to specify the line length for comments.
//	 *     - option id:         "org.eclipse.cdt.core.formatter.comment.line_length"
//	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
//	 *     - default:           "80"
//	 * </pre>
//	 */	
//	public final static String FORMATTER_COMMENT_LINE_LENGTH = CCorePlugin.PLUGIN_ID + ".formatter.comment.line_length"; //$NON-NLS-1$

//	/**
//	 * <pre>
//	 * FORMATTER / Option to compact else/if
//	 *     - option id:         "org.eclipse.cdt.core.formatter.compact_else_if"
//	 *     - possible values:   { TRUE, FALSE }
//	 *     - default:           TRUE
//	 * </pre>
//	 * @see #TRUE
//	 * @see #FALSE
//	 */
//	public static final String FORMATTER_COMPACT_ELSE_IF = CCorePlugin.PLUGIN_ID + ".formatter.compact_else_if";	//$NON-NLS-1$

	/**
	 * <pre>
	 * FORMATTER / Option to set the continuation indentation
	 *     - option id:         "org.eclipse.cdt.core.formatter.continuation_indentation"
	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
	 *     - default:           "2"
	 * </pre>
	 */
	public static final String FORMATTER_CONTINUATION_INDENTATION = CCorePlugin.PLUGIN_ID + ".formatter.continuation_indentation";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to set the continuation indentation inside array initializer
	 *     - option id:         "org.eclipse.cdt.core.formatter.continuation_indentation_for_array_initializer"
	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
	 *     - default:           "2"
	 * </pre>
	 */
	public static final String FORMATTER_CONTINUATION_INDENTATION_FOR_ARRAY_INITIALIZER = CCorePlugin.PLUGIN_ID + ".formatter.continuation_indentation_for_array_initializer";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to indent body declarations compare to its enclosing annotation declaration header
	 *     - option id:         "org.eclipse.cdt.core.formatter.indent_body_declarations_compare_to_annotation_declaration_header"
	 *     - possible values:   { TRUE, FALSE }
	 *     - default:           TRUE
	 * </pre>
	 * @see #TRUE
	 * @see #FALSE
	 */
	public static final String FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ANNOTATION_DECLARATION_HEADER = CCorePlugin.PLUGIN_ID + ".formatter.indent_body_declarations_compare_to_annotation_declaration_header";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to indent body declarations compare to its enclosing enum constant header
	 *     - option id:         "org.eclipse.cdt.core.formatter.indent_body_declarations_compare_to_enum_constant_header"
	 *     - possible values:   { TRUE, FALSE }
	 *     - default:           TRUE
	 * </pre>
	 * @see #TRUE
	 * @see #FALSE
	 */
	public static final String FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ENUM_CONSTANT_HEADER = CCorePlugin.PLUGIN_ID + ".formatter.indent_body_declarations_compare_to_enum_constant_header";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to indent body declarations compare to its enclosing enum declaration header
	 *     - option id:         "org.eclipse.cdt.core.formatter.indent_body_declarations_compare_to_enum_declaration_header"
	 *     - possible values:   { TRUE, FALSE }
	 *     - default:           TRUE
	 * </pre>
	 * @see #TRUE
	 * @see #FALSE
	 */
	public static final String FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_ENUM_DECLARATION_HEADER = CCorePlugin.PLUGIN_ID + ".formatter.indent_body_declarations_compare_to_enum_declaration_header";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to indent body declarations compare to its enclosing type header
	 *     - option id:         "org.eclipse.cdt.core.formatter.indent_body_declarations_compare_to_type_header"
	 *     - possible values:   { TRUE, FALSE }
	 *     - default:           TRUE
	 * </pre>
	 * @see #TRUE
	 * @see #FALSE
	 */
	public static final String FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER = CCorePlugin.PLUGIN_ID + ".formatter.indent_body_declarations_compare_to_type_header";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to indent breaks compare to cases
	 *     - option id:         "org.eclipse.cdt.core.formatter.indent_breaks_compare_to_cases"
	 *     - possible values:   { TRUE, FALSE }
	 *     - default:           TRUE
	 * </pre>
	 * @see #TRUE
	 * @see #FALSE
	 */
	public static final String FORMATTER_INDENT_BREAKS_COMPARE_TO_CASES = CCorePlugin.PLUGIN_ID + ".formatter.indent_breaks_compare_to_cases";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to indent empty lines
	 *     - option id:         "org.eclipse.cdt.core.formatter.indent_empty_lines"
	 *     - possible values:   { TRUE, FALSE }
	 *     - default:           FALSE
	 * </pre>
	 * @see #TRUE
	 * @see #FALSE
	 */
	public static final String FORMATTER_INDENT_EMPTY_LINES = CCorePlugin.PLUGIN_ID + ".formatter.indent_empty_lines"; //$NON-NLS-1$	
	/**
	 * <pre>
	 * FORMATTER / Option to indent statements inside a block
	 *     - option id:         "org.eclipse.cdt.core.formatter.indent_statements_compare_to_block"
	 *     - possible values:   { TRUE, FALSE }
	 *     - default:           TRUE
	 * </pre>
	 * @see #TRUE
	 * @see #FALSE
	 */
	public static final String FORMATTER_INDENT_STATEMENTS_COMPARE_TO_BLOCK = CCorePlugin.PLUGIN_ID + ".formatter.indent_statements_compare_to_block"; //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to indent statements inside the body of a method or a constructor
	 *     - option id:         "org.eclipse.cdt.core.formatter.indent_statements_compare_to_body"
	 *     - possible values:   { TRUE, FALSE }
	 *     - default:           TRUE
	 * </pre>
	 * @see #TRUE
	 * @see #FALSE
	 */
	public static final String FORMATTER_INDENT_STATEMENTS_COMPARE_TO_BODY = CCorePlugin.PLUGIN_ID + ".formatter.indent_statements_compare_to_body"; //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to indent switch statements compare to cases
	 *     - option id:         "org.eclipse.cdt.core.formatter.indent_switchstatements_compare_to_cases"
	 *     - possible values:   { TRUE, FALSE }
	 *     - default:           TRUE
	 * </pre>
	 * @see #TRUE
	 * @see #FALSE
	 */
	public static final String FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES = CCorePlugin.PLUGIN_ID + ".formatter.indent_switchstatements_compare_to_cases";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to indent switch statements compare to switch
	 *     - option id:         "org.eclipse.cdt.core.formatter.indent_switchstatements_compare_to_switch"
	 *     - possible values:   { TRUE, FALSE }
	 *     - default:           TRUE
	 * </pre>
	 * @see #TRUE
	 * @see #FALSE
	 */
	public static final String FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH = CCorePlugin.PLUGIN_ID + ".formatter.indent_switchstatements_compare_to_switch";	//$NON-NLS-1$

	/**
	 * <pre>
	 * FORMATTER / Option to specify the equivalent number of spaces that represents one indentation 
	 *     - option id:         "org.eclipse.cdt.core.formatter.indentation.size"
	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
	 *     - default:           "4"
	 * </pre>
	 * <p>This option is used only if the tab char is set to MIXED.
	 * </p>
	 * @see #FORMATTER_TAB_CHAR
	 */
	public static final String FORMATTER_INDENTATION_SIZE = CCorePlugin.PLUGIN_ID + ".formatter.indentation.size"; //$NON-NLS-1$

//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a new line after an annotation
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_new_line_after_annotation"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_new_line_after_annotation";//$NON-NLS-1$
//
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a new line after the opening brace in an array initializer
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_new_line_after_opening_brace_in_array_initializer"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_NEW_LINE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER = CCorePlugin.PLUGIN_ID + ".formatter.insert_new_line_after_opening_brace_in_array_initializer";//$NON-NLS-1$
//
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a new line at the end of the current file if missing
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_new_line_at_end_of_file_if_missing"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_NEW_LINE_AT_END_OF_FILE_IF_MISSING = CCorePlugin.PLUGIN_ID + ".formatter.insert_new_line_at_end_of_file_if_missing";//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a new line before the catch keyword in try statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_new_line_before_catch_in_try_statement"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_NEW_LINE_BEFORE_CATCH_IN_TRY_STATEMENT = CCorePlugin.PLUGIN_ID + ".formatter.insert_new_line_before_catch_in_try_statement";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a new line before the closing brace in an array initializer
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_new_line_before_closing_brace_in_array_initializer"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER = CCorePlugin.PLUGIN_ID + ".formatter.insert_new_line_before_closing_brace_in_array_initializer";//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a new line before the else keyword in if statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_new_line_before_else_in_if_statement"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_NEW_LINE_BEFORE_ELSE_IN_IF_STATEMENT = CCorePlugin.PLUGIN_ID + ".formatter.insert_new_line_before_else_in_if_statement";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a new line before the finally keyword in try statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_new_line_before_finally_in_try_statement"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_NEW_LINE_BEFORE_FINALLY_IN_TRY_STATEMENT = CCorePlugin.PLUGIN_ID + ".formatter.insert_new_line_before_finally_in_try_statement";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a new line before while in do statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_new_line_before_while_in_do_statement"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_NEW_LINE_BEFORE_WHILE_IN_DO_STATEMENT = CCorePlugin.PLUGIN_ID + ".formatter.insert_new_line_before_while_in_do_statement";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a new line in an empty annotation declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_new_line_in_empty_annotation_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ANNOTATION_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_new_line_in_empty_annotation_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a new line in an empty anonymous type declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_new_line_in_empty_anonymous_type_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_new_line_in_empty_anonymous_type_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a new line in an empty block
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_new_line_in_empty_block"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_NEW_LINE_IN_EMPTY_BLOCK = CCorePlugin.PLUGIN_ID + ".formatter.insert_new_line_in_empty_block";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a new line in an empty enum constant
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_new_line_in_empty_enum_constant"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ENUM_CONSTANT = CCorePlugin.PLUGIN_ID + ".formatter.insert_new_line_in_empty_enum_constant";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a new line in an empty enum declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_new_line_in_empty_enum_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ENUM_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_new_line_in_empty_enum_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a new line in an empty method body
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_new_line_in_empty_method_body"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY = CCorePlugin.PLUGIN_ID + ".formatter.insert_new_line_in_empty_method_body";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a new line in an empty type declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_new_line_in_empty_type_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_new_line_in_empty_type_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after and in wilcard
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_and_in_type_parameter"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_AND_IN_TYPE_PARAMETER = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_and_in_type_parameter"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after an assignment operator
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_assignment_operator"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_assignment_operator"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after at in annotation
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_at_in_annotation"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_AT_IN_ANNOTATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_at_in_annotation"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after at in annotation type declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_at_in_annotation_type_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_AT_IN_ANNOTATION_TYPE_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_at_in_annotation_type_declaration"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after a binary operator
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_binary_operator"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_BINARY_OPERATOR = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_binary_operator"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the closing angle bracket in type arguments
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_closing_angle_bracket_in_type_arguments"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_CLOSING_ANGLE_BRACKET_IN_TYPE_ARGUMENTS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_closing_angle_bracket_in_type_arguments"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the closing angle bracket in type parameters
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_closing_angle_bracket_in_type_parameters"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_CLOSING_ANGLE_BRACKET_IN_TYPE_PARAMETERS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_closing_angle_bracket_in_type_parameters"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the closing brace of a block
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_closing_brace_in_block"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_CLOSING_BRACE_IN_BLOCK = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_closing_brace_in_block"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the closing parenthesis of a cast expression
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_closing_paren_in_cast"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_closing_paren_in_cast"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the colon in an assert statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_colon_in_assert"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COLON_IN_ASSERT = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_colon_in_assert"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after colon in a case statement when a opening brace follows the colon
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_colon_in_case"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COLON_IN_CASE = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_colon_in_case";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the colon in a conditional expression
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_colon_in_conditional"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_colon_in_conditional"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after colon in a for statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_colon_in_for"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COLON_IN_FOR = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_colon_in_for";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the colon in a labeled statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_colon_in_labeled_statement"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_colon_in_labeled_statement"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in an allocation expression
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_allocation_expression"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ALLOCATION_EXPRESSION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_allocation_expression"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in annotation
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_annotation"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ANNOTATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_annotation"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in an array initializer
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_array_initializer"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_array_initializer"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in the parameters of a constructor declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_constructor_declaration_parameters"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_DECLARATION_PARAMETERS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_constructor_declaration_parameters"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in the exception names in a throws clause of a constructor declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_constructor_declaration_throws"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_CONSTRUCTOR_DECLARATION_THROWS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_constructor_declaration_throws"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in the arguments of an enum constant
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_enum_constant_arguments"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ENUM_CONSTANT_ARGUMENTS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_enum_constant_arguments"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in enum declarations
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_enum_declarations"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ENUM_DECLARATIONS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_enum_declarations"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in the arguments of an explicit constructor call
//	 *     - option id:         "org.eclipse.jdt.core.formatter.insert_space_after_comma_in_explicitconstructorcall_arguments"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_EXPLICIT_CONSTRUCTOR_CALL_ARGUMENTS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_explicitconstructorcall_arguments"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in the increments of a for statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_for_increments"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_for_increments"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in the initializations of a for statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_for_inits"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_for_inits"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in the parameters of a method declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_method_declaration_parameters"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_DECLARATION_PARAMETERS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_method_declaration_parameters"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in the exception names in a throws clause of a method declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_method_declaration_throws"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_DECLARATION_THROWS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_method_declaration_throws"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in the arguments of a method invocation
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_method_invocation_arguments"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_INVOCATION_ARGUMENTS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_method_invocation_arguments"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in multiple field declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_multiple_field_declarations"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_multiple_field_declarations"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in multiple local declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_multiple_local_declarations"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_multiple_local_declarations"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in parameterized type reference
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_parameterized_type_reference"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_PARAMETERIZED_TYPE_REFERENCE = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_parameterized_type_reference"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in superinterfaces names of a type header
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_superinterfaces"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_SUPERINTERFACES = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_superinterfaces"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in type arguments
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_type_arguments"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_TYPE_ARGUMENTS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_type_arguments"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the comma in type parameters
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_comma_in_type_parameters"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_TYPE_PARAMETERS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_comma_in_type_parameters"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after ellipsis
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_ellipsis"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_ELLIPSIS  = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_ellipsis";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening angle bracket in parameterized type reference
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_angle_bracket_in_parameterized_type_reference"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_ANGLE_BRACKET_IN_PARAMETERIZED_TYPE_REFERENCE = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_angle_bracket_in_parameterized_type_reference";//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening angle bracket in type arguments
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_angle_bracket_in_type_arguments"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_ANGLE_BRACKET_IN_TYPE_ARGUMENTS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_angle_bracket_in_type_arguments";//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening angle bracket in type parameters
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_angle_bracket_in_type_parameters"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_ANGLE_BRACKET_IN_TYPE_PARAMETERS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_angle_bracket_in_type_parameters";//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening brace in an array initializer
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_brace_in_array_initializer"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_brace_in_array_initializer";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening bracket inside an array allocation expression
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_bracket_in_array_allocation_expression"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACKET_IN_ARRAY_ALLOCATION_EXPRESSION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_bracket_in_array_allocation_expression";//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening bracket inside an array reference
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_bracket_in_array_reference"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACKET_IN_ARRAY_REFERENCE = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_bracket_in_array_reference";//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening parenthesis in annotation
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_paren_in_annotation"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_ANNOTATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_annotation"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening parenthesis in a cast expression
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_paren_in_cast"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_cast"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening parenthesis in a catch
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_paren_in_catch"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CATCH = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_catch"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening parenthesis in a constructor declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_paren_in_constructor_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CONSTRUCTOR_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_constructor_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening parenthesis in enum constant
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_paren_in_enum_constant"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_ENUM_CONSTANT = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_enum_constant"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening parenthesis in a for statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_paren_in_for"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_FOR = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_for"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening parenthesis in an if statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_paren_in_if"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_IF = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_if"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening parenthesis in a method declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_paren_in_method_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_method_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening parenthesis in a method invocation
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_paren_in_method_invocation"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_INVOCATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_method_invocation"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening parenthesis in a parenthesized expression
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_paren_in_parenthesized_expression"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_PARENTHESIZED_EXPRESSION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_parenthesized_expression"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening parenthesis in a switch statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_paren_in_switch"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_SWITCH = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_switch"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening parenthesis in a synchronized statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_paren_in_synchronized"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_SYNCHRONIZED = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_synchronized"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after the opening parenthesis in a while statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_opening_paren_in_while"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_WHILE = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_opening_paren_in_while"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after a postfix operator
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_postfix_operator"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_POSTFIX_OPERATOR = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_postfix_operator"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after a prefix operator
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_prefix_operator"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_PREFIX_OPERATOR = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_prefix_operator"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after question mark in a conditional expression
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_question_in_conditional"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_question_in_conditional"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after question mark in a wildcard
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_question_in_wildcard"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_QUESTION_IN_WILDCARD = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_question_in_wildcard"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after semicolon in a for statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_semicolon_in_for"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_semicolon_in_for"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space after an unary operator
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_after_unary_operator"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_AFTER_UNARY_OPERATOR = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_after_unary_operator"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before and in wildcard
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_and_in_type_parameter"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_AND_IN_TYPE_PARAMETER = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_and_in_type_parameter";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before an assignment operator
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_assignment_operator"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_assignment_operator";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before at in annotation type declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_at_in_annotation_type_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_AT_IN_ANNOTATION_TYPE_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_at_in_annotation_type_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before an binary operator
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_binary_operator"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_BINARY_OPERATOR = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_binary_operator";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing angle bracket in parameterized type reference
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_angle_bracket_in_parameterized_type_reference"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_ANGLE_BRACKET_IN_PARAMETERIZED_TYPE_REFERENCE = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_angle_bracket_in_parameterized_type_reference";		//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing angle bracket in type arguments
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_angle_bracket_in_type_arguments"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_ANGLE_BRACKET_IN_TYPE_ARGUMENTS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_angle_bracket_in_type_arguments";		//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing angle bracket in type parameters
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_angle_bracket_in_type_parameters"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_ANGLE_BRACKET_IN_TYPE_PARAMETERS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_angle_bracket_in_type_parameters";		//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing brace in an array initializer
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_brace_in_array_initializer"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_brace_in_array_initializer";		//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing bracket in an array allocation expression
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_bracket_in_array_allocation_expression"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACKET_IN_ARRAY_ALLOCATION_EXPRESSION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_bracket_in_array_allocation_expression";//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing bracket in an array reference
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_bracket_in_array_reference"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACKET_IN_ARRAY_REFERENCE = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_bracket_in_array_reference";//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing parenthesis in annotation
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_paren_in_annotation"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_ANNOTATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_annotation";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing parenthesis in a cast expression
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_paren_in_cast"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_cast";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing parenthesis in a catch
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_paren_in_catch"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CATCH = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_catch";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing parenthesis in a constructor declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_paren_in_constructor_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CONSTRUCTOR_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_constructor_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing parenthesis in enum constant
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_paren_in_enum_constant"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_ENUM_CONSTANT = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_enum_constant";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing parenthesis in a for statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_paren_in_for"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_FOR = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_for";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing parenthesis in an if statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_paren_in_if"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_IF = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_if";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing parenthesis in a method declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_paren_in_method_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_method_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing parenthesis in a method invocation
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_paren_in_method_invocation"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_INVOCATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_method_invocation"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing parenthesis in a parenthesized expression
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_paren_in_parenthesized_expression"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHESIZED_EXPRESSION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_parenthesized_expression"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing parenthesis in a switch statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_paren_in_switch"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_SWITCH = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_switch";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing parenthesis in a synchronized statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_paren_in_synchronized"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_SYNCHRONIZED = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_synchronized";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the closing parenthesis in a while statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_closing_paren_in_while"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_WHILE = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_closing_paren_in_while";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before colon in an assert statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_colon_in_assert"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_ASSERT = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_colon_in_assert";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before colon in a case statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_colon_in_case"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CASE = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_colon_in_case";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before colon in a conditional expression
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_colon_in_conditional"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_colon_in_conditional";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before colon in a default statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_colon_in_default"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_DEFAULT = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_colon_in_default";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before colon in a for statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_colon_in_for"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_FOR = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_colon_in_for";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before colon in a labeled statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_colon_in_labeled_statement"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_colon_in_labeled_statement";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in an allocation expression
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_allocation_expression"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ALLOCATION_EXPRESSION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_allocation_expression";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in annotation
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_annotation"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ANNOTATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_annotation";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in an array initializer
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_array_initializer"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ARRAY_INITIALIZER = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_array_initializer";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in the parameters of a constructor declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_constructor_declaration_parameters"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_DECLARATION_PARAMETERS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_constructor_declaration_parameters";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in the exception names of the throws clause of a constructor declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_constructor_declaration_throws"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_CONSTRUCTOR_DECLARATION_THROWS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_constructor_declaration_throws";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in the arguments of enum constant
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_enum_constant_arguments"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ENUM_CONSTANT_ARGUMENTS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_enum_constant_arguments";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in enum declarations
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_enum_declarations"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ENUM_DECLARATIONS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_enum_declarations";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in the arguments of an explicit constructor call
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_explicitconstructorcall_arguments"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_EXPLICIT_CONSTRUCTOR_CALL_ARGUMENTS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_explicitconstructorcall_arguments";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in the increments of a for statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_for_increments"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_for_increments";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in the initializations of a for statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_for_inits"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_for_inits";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in the parameters of a method declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_method_declaration_parameters"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_DECLARATION_PARAMETERS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_method_declaration_parameters";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in the exception names of the throws clause of a method declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_method_declaration_throws"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_DECLARATION_THROWS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_method_declaration_throws";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in the arguments of a method invocation
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_method_invocation_arguments"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_INVOCATION_ARGUMENTS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_method_invocation_arguments";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in a multiple field declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_multiple_field_declarations"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_FIELD_DECLARATIONS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_multiple_field_declarations";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in a multiple local declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_multiple_local_declarations"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_MULTIPLE_LOCAL_DECLARATIONS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_multiple_local_declarations";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in parameterized type reference
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_parameterized_type_reference"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_PARAMETERIZED_TYPE_REFERENCE = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_parameterized_type_reference";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in the superinterfaces names in a type header
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_superinterfaces"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_SUPERINTERFACES = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_superinterfaces";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in type arguments
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_type_arguments"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_TYPE_ARGUMENTS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_type_arguments";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before comma in type parameters
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_comma_in_type_parameters"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_TYPE_PARAMETERS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_comma_in_type_parameters";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before ellipsis
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_ellipsis"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_ELLIPSIS  = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_ellipsis";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening angle bracket in parameterized type reference
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_angle_bracket_in_parameterized_type_reference"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_ANGLE_BRACKET_IN_PARAMETERIZED_TYPE_REFERENCE  = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_angle_bracket_in_parameterized_type_reference";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening angle bracket in type arguments
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_angle_bracket_in_type_arguments"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_ANGLE_BRACKET_IN_TYPE_ARGUMENTS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_angle_bracket_in_type_arguments";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening angle bracket in type parameters
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_angle_bracket_in_type_parameters"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_ANGLE_BRACKET_IN_TYPE_PARAMETERS = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_angle_bracket_in_type_parameters";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening brace in an annotation type declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_brace_in_annotation_type_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ANNOTATION_TYPE_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_annotation_type_declaration"; 	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening brace in an anonymous type declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_brace_in_anonymous_type_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ANONYMOUS_TYPE_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_anonymous_type_declaration"; 	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening brace in an array initializer
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_brace_in_array_initializer"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ARRAY_INITIALIZER = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_array_initializer"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening brace in a block
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_brace_in_block"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_BLOCK = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_block";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening brace in a constructor declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_brace_in_constructor_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_CONSTRUCTOR_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_constructor_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening brace in an enum constant
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_brace_in_enum_constant"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ENUM_CONSTANT = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_enum_constant";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening brace in an enum declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_brace_in_enum_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_ENUM_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_enum_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening brace in a method declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_brace_in_method_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_METHOD_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_method_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening brace in a switch statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_brace_in_switch"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_SWITCH = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_switch";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening brace in a type declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_brace_in_type_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_TYPE_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_brace_in_type_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening bracket in an array allocation expression
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_bracket_in_array_allocation_expression"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACKET_IN_ARRAY_ALLOCATION_EXPRESSION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_bracket_in_array_allocation_expression";//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening bracket in an array reference
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_bracket_in_array_reference"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACKET_IN_ARRAY_REFERENCE = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_bracket_in_array_reference";//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening bracket in an array type reference
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_bracket_in_array_type_reference"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACKET_IN_ARRAY_TYPE_REFERENCE = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_bracket_in_array_type_reference";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening parenthesis in annotation
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_paren_in_annotation"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_ANNOTATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_annotation";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening parenthesis in annotation type member declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_paren_in_annotation_type_member_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_ANNOTATION_TYPE_MEMBER_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_annotation_type_member_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening parenthesis in a catch
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_paren_in_catch"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_CATCH = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_catch";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening parenthesis in a constructor declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_paren_in_constructor_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_CONSTRUCTOR_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_constructor_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening parenthesis in enum constant
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_paren_in_enum_constant"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_ENUM_CONSTANT = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_enum_constant";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening parenthesis in a for statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_paren_in_for"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_FOR = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_for";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening parenthesis in an if statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_paren_in_if"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_IF = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_if";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening parenthesis in a method declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_paren_in_method_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_method_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening parenthesis in a method invocation
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_paren_in_method_invocation"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_INVOCATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_method_invocation";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening parenthesis in a parenthesized expression
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_paren_in_parenthesized_expression"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_PARENTHESIZED_EXPRESSION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_parenthesized_expression"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening parenthesis in a switch statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_paren_in_switch"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_SWITCH = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_switch";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening parenthesis in a synchronized statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_paren_in_synchronized"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_SYNCHRONIZED = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_synchronized";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before the opening parenthesis in a while statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_opening_paren_in_while"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_WHILE = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_opening_paren_in_while";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before parenthesized expression in return statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_parenthesized_expression_in_return"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * 
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_PARENTHESIZED_EXPRESSION_IN_RETURN  = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_parenthesized_expression_in_return";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before a postfix operator
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_postfix_operator"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_POSTFIX_OPERATOR = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_postfix_operator";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before a prefix operator
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_prefix_operator"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_PREFIX_OPERATOR = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_prefix_operator";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before question mark in a conditional expression
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_question_in_conditional"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_question_in_conditional";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before question mark in a wildcard
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_question_in_wildcard"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_QUESTION_IN_WILDCARD = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_question_in_wildcard"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before semicolon
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_semicolon"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_semicolon";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before semicolon in for statement
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_semicolon_in_for"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON_IN_FOR = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_semicolon_in_for";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space before unary operator
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_before_unary_operator"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BEFORE_UNARY_OPERATOR = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_before_unary_operator";	//$NON-NLS-1$
//
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space between brackets in an array type reference
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_between_brackets_in_array_type_reference"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BETWEEN_BRACKETS_IN_ARRAY_TYPE_REFERENCE = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_between_brackets_in_array_type_reference";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space between empty braces in an array initializer
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_between_empty_braces_in_array_initializer"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_BRACES_IN_ARRAY_INITIALIZER = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_between_empty_braces_in_array_initializer";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space between empty brackets in an array allocation expression
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_between_empty_brackets_in_array_allocation_expression"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_BRACKETS_IN_ARRAY_ALLOCATION_EXPRESSION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_between_empty_brackets_in_array_allocation_expression";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space between empty parenthesis in an annotation type member declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_between_empty_parens_in_annotation_type_member_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_ANNOTATION_TYPE_MEMBER_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_between_empty_parens_in_annotation_type_member_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space between empty parenthesis in a constructor declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_between_empty_parens_in_constructor_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_CONSTRUCTOR_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_between_empty_parens_in_constructor_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space between empty parenthesis in enum constant
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_between_empty_parens_in_enum_constant"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_ENUM_CONSTANT = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_between_empty_parens_in_enum_constant";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space between empty parenthesis in a method declaration
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_between_empty_parens_in_method_declaration"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_DECLARATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_between_empty_parens_in_method_declaration";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to insert a space between empty parenthesis in a method invocation
//	 *     - option id:         "org.eclipse.cdt.core.formatter.insert_space_between_empty_parens_in_method_invocation"
//	 *     - possible values:   { INSERT, DO_NOT_INSERT }
//	 *     - default:           DO_NOT_INSERT
//	 * </pre>
//	 * @see CCorePlugin#INSERT
//	 * @see CCorePlugin#DO_NOT_INSERT
//	 */
//	public static final String FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_INVOCATION = CCorePlugin.PLUGIN_ID + ".formatter.insert_space_between_empty_parens_in_method_invocation";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to keep else statement on the same line
//	 *     - option id:         "org.eclipse.cdt.core.formatter.keep_else_statement_on_same_line"
//	 *     - possible values:   { TRUE, FALSE }
//	 *     - default:           FALSE
//	 * </pre>
//	 * @see #TRUE
//	 * @see #FALSE
//	 */
//	public static final String FORMATTER_KEEP_ELSE_STATEMENT_ON_SAME_LINE = CCorePlugin.PLUGIN_ID + ".formatter.keep_else_statement_on_same_line"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to keep empty array initializer one one line
//	 *     - option id:         "org.eclipse.cdt.core.formatter.keep_empty_array_initializer_on_one_line"
//	 *     - possible values:   { TRUE, FALSE }
//	 *     - default:           FALSE
//	 * </pre>
//	 * @see #TRUE
//	 * @see #FALSE
//	 */
//	public static final String FORMATTER_KEEP_EMPTY_ARRAY_INITIALIZER_ON_ONE_LINE = CCorePlugin.PLUGIN_ID + ".formatter.keep_empty_array_initializer_on_one_line"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to keep guardian clause on one line
//	 *     - option id:         "org.eclipse.cdt.core.formatter.format_guardian_clause_on_one_line"
//	 *     - possible values:   { TRUE, FALSE }
//	 *     - default:           FALSE
//	 * </pre>
//	 * @see #TRUE
//	 * @see #FALSE
//	 */
//	public static final String FORMATTER_KEEP_GUARDIAN_CLAUSE_ON_ONE_LINE = CCorePlugin.PLUGIN_ID + ".formatter.format_guardian_clause_on_one_line";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to keep simple if statement on the one line
//	 *     - option id:         "org.eclipse.cdt.core.formatter.keep_imple_if_on_one_line"
//	 *     - possible values:   { TRUE, FALSE }
//	 *     - default:           FALSE
//	 * </pre>
//	 * @see #TRUE
//	 * @see #FALSE
//	 */
//	public static final String FORMATTER_KEEP_SIMPLE_IF_ON_ONE_LINE = CCorePlugin.PLUGIN_ID + ".formatter.keep_imple_if_on_one_line"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to keep then statement on the same line
//	 *     - option id:         "org.eclipse.cdt.core.formatter.keep_then_statement_on_same_line"
//	 *     - possible values:   { TRUE, FALSE }
//	 *     - default:           FALSE
//	 * </pre>
//	 * @see #TRUE
//	 * @see #FALSE
//	 */
//	public static final String FORMATTER_KEEP_THEN_STATEMENT_ON_SAME_LINE = CCorePlugin.PLUGIN_ID + ".formatter.keep_then_statement_on_same_line";//$NON-NLS-1$

	/**
	 * <pre>
	 * FORMATTER / Option to specify the length of the page. Beyond this length, the formatter will try to split the code
	 *     - option id:         "org.eclipse.cdt.core.formatter.lineSplit"
	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
	 *     - default:           "80"
	 * </pre>
	 */
	public static final String FORMATTER_LINE_SPLIT = CCorePlugin.PLUGIN_ID + ".formatter.lineSplit"; //$NON-NLS-1$
//
//	/**
//	 * <pre>
//	 * FORMATTER / Option to specify the number of empty lines to preserve
//	 *     - option id:         "org.eclipse.cdt.core.formatter.number_of_empty_lines_to_preserve"
//	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
//	 *     - default:           "0"
//	 * </pre>
//	 */
//	public static final String FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE = CCorePlugin.PLUGIN_ID + ".formatter.number_of_empty_lines_to_preserve";	//$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Option to specify whether or not empty statement should be on a new line
//	 *     - option id:         "org.eclipse.cdt.core.formatter.put_empty_statement_on_new_line"
//	 *     - possible values:   { TRUE, FALSE }
//	 *     - default:           FALSE
//	 * </pre>
//	 * @see #TRUE
//	 * @see #FALSE
//	 */
//	public static final String FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE = CCorePlugin.PLUGIN_ID + ".formatter.put_empty_statement_on_new_line";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to specify the tabulation size
	 *     - option id:         "org.eclipse.cdt.core.formatter.tabulation.char"
	 *     - possible values:   { TAB, SPACE, MIXED }
	 *     - default:           TAB
	 * </pre>
	 * More values may be added in the future.
	 * 
	 * @see CCorePlugin#TAB
	 * @see CCorePlugin#SPACE
	 * @see #MIXED
	 */
	public static final String FORMATTER_TAB_CHAR = CCorePlugin.PLUGIN_ID + ".formatter.tabulation.char"; //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Option to specify the equivalent number of spaces that represents one tabulation 
	 *     - option id:         "org.eclipse.cdt.core.formatter.tabulation.size"
	 *     - possible values:   "&lt;n&gt;", where n is zero or a positive integer
	 *     - default:           "4"
	 * </pre>
	 */
	public static final String FORMATTER_TAB_SIZE = CCorePlugin.PLUGIN_ID + ".formatter.tabulation.size"; //$NON-NLS-1$

	/**
	 * <pre>
	 * FORMATTER / Option to use tabulations only for leading indentations 
	 *     - option id:         "org.eclipse.cdt.core.formatter.use_tabs_only_for_leading_indentations"
	 *     - possible values:   { TRUE, FALSE }
	 *     - default:           FALSE
	 * </pre>
	 * @see #TRUE
	 * @see #FALSE
	 */
	public static final String FORMATTER_USE_TABS_ONLY_FOR_LEADING_INDENTATIONS = CCorePlugin.PLUGIN_ID + ".formatter.use_tabs_only_for_leading_indentations"; //$NON-NLS-1$

	/**
	 * <pre>
	 * FORMATTER / The wrapping is done by indenting by one compare to the current indentation.
	 * </pre>
	 */
	public static final int INDENT_BY_ONE= 2;
	
	/**
	 * <pre>
	 * FORMATTER / The wrapping is done by using the current indentation.
	 * </pre>
	 */
	public static final int INDENT_DEFAULT= 0;
	/**
	 * <pre>
	 * FORMATTER / The wrapping is done by indenting on column under the splitting location.
	 * </pre>
	 */
	public static final int INDENT_ON_COLUMN = 1;
	
	/**
	 * <pre>
	 * FORMATTER / Possible value for the option FORMATTER_TAB_CHAR
	 * </pre>
	 * @see CCorePlugin#TAB
	 * @see CCorePlugin#SPACE
	 * @see #FORMATTER_TAB_CHAR
	 */
	public static final String MIXED = "mixed"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Value to set a brace location at the start of the next line with
//	 *             the right indentation.
//	 * </pre>
//	 * @see #FORMATTER_BRACE_POSITION_FOR_ANONYMOUS_TYPE_DECLARATION
//	 * @see #FORMATTER_BRACE_POSITION_FOR_ARRAY_INITIALIZER
//	 * @see #FORMATTER_BRACE_POSITION_FOR_BLOCK
//	 * @see #FORMATTER_BRACE_POSITION_FOR_CONSTRUCTOR_DECLARATION
// 	 * @see #FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION
// 	 * @see #FORMATTER_BRACE_POSITION_FOR_SWITCH
//	 * @see #FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION
//	 */
//	public static final String NEXT_LINE = "next_line"; //$NON-NLS-1$
//	/**
//	 * <pre>
//	 * FORMATTER / Value to set a brace location at the start of the next line if a wrapping
//	 *             occured.
//	 * </pre>
//	 * @see #FORMATTER_BRACE_POSITION_FOR_ANONYMOUS_TYPE_DECLARATION
//	 * @see #FORMATTER_BRACE_POSITION_FOR_ARRAY_INITIALIZER
//	 * @see #FORMATTER_BRACE_POSITION_FOR_BLOCK
//	 * @see #FORMATTER_BRACE_POSITION_FOR_CONSTRUCTOR_DECLARATION
// 	 * @see #FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION
// 	 * @see #FORMATTER_BRACE_POSITION_FOR_SWITCH
//	 * @see #FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION
//	 */
//    public static final String NEXT_LINE_ON_WRAP = "next_line_on_wrap"; //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Value to set a brace location at the start of the next line with
	 *             an extra indentation.
	 * </pre>
	 * @see #FORMATTER_BRACE_POSITION_FOR_ANONYMOUS_TYPE_DECLARATION
	 * @see #FORMATTER_BRACE_POSITION_FOR_ARRAY_INITIALIZER
	 * @see #FORMATTER_BRACE_POSITION_FOR_BLOCK
	 * @see #FORMATTER_BRACE_POSITION_FOR_CONSTRUCTOR_DECLARATION
 	 * @see #FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION
 	 * @see #FORMATTER_BRACE_POSITION_FOR_SWITCH
	 * @see #FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION
	 */
	public static final String NEXT_LINE_SHIFTED = "next_line_shifted";	//$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / Value to set an option to true.
	 * </pre>
	 */
	public static final String TRUE = "true"; //$NON-NLS-1$
	/**
	 * <pre>
	 * FORMATTER / The wrapping is done using as few lines as possible.
	 * </pre>
	 */
	public static final int WRAP_COMPACT= 1;
	/**
	 * <pre>
	 * FORMATTER / The wrapping is done putting the first element on a new
	 *             line and then wrapping next elements using as few lines as possible.
	 * </pre>
	 */
	public static final int WRAP_COMPACT_FIRST_BREAK= 2;
	/**
	 * <pre>
	 * FORMATTER / The wrapping is done by putting each element on its own line
	 *             except the first element.
	 * </pre>
	 */
	public static final int WRAP_NEXT_PER_LINE= 5;
	/**
	 * <pre>
	 * FORMATTER / The wrapping is done by putting each element on its own line.
	 *             All elements are indented by one except the first element.
	 * </pre>
	 */
	public static final int WRAP_NEXT_SHIFTED= 4;

	/**
	 * <pre>
	 * FORMATTER / Value to disable alignment.
	 * </pre>
	 */
	public static final int WRAP_NO_SPLIT= 0;
	/**
	 * <pre>
	 * FORMATTER / The wrapping is done by putting each element on its own line.
	 * </pre>
	 */
	public static final int WRAP_ONE_PER_LINE= 3;

	/*
	 * Private constants.
	 */
	private static final IllegalArgumentException WRONG_ARGUMENT = new IllegalArgumentException();
	
	/**
	 * Create a new alignment value according to the given values. This must be used to set up
	 * the alignment options.
	 * 
	 * @param forceSplit the given force value
	 * @param wrapStyle the given wrapping style
	 * @param indentStyle the given indent style
	 * 
	 * @return the new alignement value
	 */
	public static String createAlignmentValue(boolean forceSplit, int wrapStyle, int indentStyle) {
		int alignmentValue = 0; 
		switch(wrapStyle) {
			case WRAP_COMPACT :
				alignmentValue |= Alignment.M_COMPACT_SPLIT;
				break;
			case WRAP_COMPACT_FIRST_BREAK :
				alignmentValue |= Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
				break;
			case WRAP_NEXT_PER_LINE :
				alignmentValue |= Alignment.M_NEXT_PER_LINE_SPLIT;
				break;
			case WRAP_NEXT_SHIFTED :
				alignmentValue |= Alignment.M_NEXT_SHIFTED_SPLIT;
				break;
			case WRAP_ONE_PER_LINE :
				alignmentValue |= Alignment.M_ONE_PER_LINE_SPLIT;
				break;
		}		
		if (forceSplit) {
			alignmentValue |= Alignment.M_FORCE;
		}
		switch(indentStyle) {
			case INDENT_BY_ONE :
				alignmentValue |= Alignment.M_INDENT_BY_ONE;
				break;
			case INDENT_ON_COLUMN :
				alignmentValue |= Alignment.M_INDENT_ON_COLUMN;
		}
		return String.valueOf(alignmentValue);
	}

	/**
	 * Returns the default Eclipse formatter settings
	 * 
	 * @return the Eclipse default settings
	 */
	public static Map getEclipseDefaultSettings() {
		return DefaultCodeFormatterOptions.getEclipseDefaultSettings().getMap();
	}

	/**
	 * <p>Return the force value of the given alignment value.
	 * The given alignment value should be created using the <code>createAlignmentValue(boolean, int, int)</code>
	 * API.
	 * </p>
	 *
	 * @param value the given alignment value
	 * @return the force value of the given alignment value
	 * @see #createAlignmentValue(boolean, int, int)
	 * @exception IllegalArgumentException if the given alignment value is null, or if it 
	 * doesn't have a valid format.
	 */
	public static boolean getForceWrapping(String value) {
		if (value == null) {
			throw WRONG_ARGUMENT;
		}
		try {
			int existingValue = Integer.parseInt(value);
			return (existingValue & Alignment.M_FORCE) != 0;
		} catch (NumberFormatException e) {
			throw WRONG_ARGUMENT;
		}
	}
	
	/**
	 * <p>Return the indentation style of the given alignment value.
	 * The given alignment value should be created using the <code>createAlignmentValue(boolean, int, int)</code>
	 * API.
	 * </p>
	 *
	 * @param value the given alignment value
	 * @return the indentation style of the given alignment value
	 * @see #createAlignmentValue(boolean, int, int)
	 * @exception IllegalArgumentException if the given alignment value is null, or if it 
	 * doesn't have a valid format.
	 */
	public static int getIndentStyle(String value) {
		if (value == null) {
			throw WRONG_ARGUMENT;
		}
		try {
			int existingValue = Integer.parseInt(value);
			if ((existingValue & Alignment.M_INDENT_BY_ONE) != 0) {
				return INDENT_BY_ONE;
			} else if ((existingValue & Alignment.M_INDENT_ON_COLUMN) != 0) {
				return INDENT_ON_COLUMN;
			} else {
				return INDENT_DEFAULT;
			}
		} catch (NumberFormatException e) {
			throw WRONG_ARGUMENT;
		}
	}

	/**
	 * <p>Return the wrapping style of the given alignment value.
	 * The given alignment value should be created using the <code>createAlignmentValue(boolean, int, int)</code>
	 * API.
	 * </p>
	 *
	 * @param value the given alignment value
	 * @return the wrapping style of the given alignment value
	 * @see #createAlignmentValue(boolean, int, int)
	 * @exception IllegalArgumentException if the given alignment value is null, or if it 
	 * doesn't have a valid format.
	 */
	public static int getWrappingStyle(String value) {
		if (value == null) {
			throw WRONG_ARGUMENT;
		}
		try {
			int existingValue = Integer.parseInt(value) & Alignment.SPLIT_MASK;
			switch(existingValue) {
				case Alignment.M_COMPACT_SPLIT :
					return WRAP_COMPACT;
				case Alignment.M_COMPACT_FIRST_BREAK_SPLIT :
					return WRAP_COMPACT_FIRST_BREAK;
				case Alignment.M_NEXT_PER_LINE_SPLIT :
					return WRAP_NEXT_PER_LINE;
				case Alignment.M_NEXT_SHIFTED_SPLIT :
					return WRAP_NEXT_SHIFTED;
				case Alignment.M_ONE_PER_LINE_SPLIT :
					return WRAP_ONE_PER_LINE;
				default:
					return WRAP_NO_SPLIT;
			}
		} catch (NumberFormatException e) {
			throw WRONG_ARGUMENT;
		}
	}
	/**
	 * <p>Set the force value of the given alignment value and return the new value.
	 * The given alignment value should be created using the <code>createAlignmentValue(boolean, int, int)</code>
	 * API.
	 * </p>
	 *
	 * @param value the given alignment value
	 * @param force the given force value
	 * @return the new alignment value
	 * @see #createAlignmentValue(boolean, int, int)
	 * @exception IllegalArgumentException if the given alignment value is null, or if it 
	 * doesn't have a valid format.
	 */
	public static String setForceWrapping(String value, boolean force) {
		if (value == null) {
			throw WRONG_ARGUMENT;
		}
		try {
			int existingValue = Integer.parseInt(value);
			// clear existing force bit
			existingValue &= ~Alignment.M_FORCE;
			if (force) {
				existingValue |= Alignment.M_FORCE;
			}
			return String.valueOf(existingValue);
		} catch (NumberFormatException e) {
			throw WRONG_ARGUMENT;
		}		
	}
	
	/**
	 * <p>Set the indentation style of the given alignment value and return the new value.
	 * The given value should be created using the <code>createAlignmentValue(boolean, int, int)</code>
	 * API.
	 * </p>
	 *
	 * @param value the given alignment value
	 * @param indentStyle the given indentation style
	 * @return the new alignment value
	 * @see #INDENT_BY_ONE
	 * @see #INDENT_DEFAULT
	 * @see #INDENT_ON_COLUMN
	 * @see #createAlignmentValue(boolean, int, int)
	 * @exception IllegalArgumentException if the given alignment value is null, if the given
	 * indentation style is not one of the possible indentation styles, or if the given
	 * alignment value doesn't have a valid format.
	 */
	public static String setIndentStyle(String value, int indentStyle) {
		if (value == null) {
			throw WRONG_ARGUMENT;
		}
		switch(indentStyle) {
			case INDENT_BY_ONE :
			case INDENT_DEFAULT :
			case INDENT_ON_COLUMN :
				break;
			default :
				throw WRONG_ARGUMENT;
		}
		try {
			int existingValue = Integer.parseInt(value);
			// clear existing indent bits
			existingValue &= ~(Alignment.M_INDENT_BY_ONE | Alignment.M_INDENT_ON_COLUMN);
			switch(indentStyle) {
				case INDENT_BY_ONE :
					existingValue |= Alignment.M_INDENT_BY_ONE;
					break;
				case INDENT_ON_COLUMN :
					existingValue |= Alignment.M_INDENT_ON_COLUMN;
			}
			return String.valueOf(existingValue);
		} catch (NumberFormatException e) {
			throw WRONG_ARGUMENT;
		}
	}
	/**
	 * <p>Set the wrapping style of the given alignment value and return the new value.
	 * The given value should be created using the <code>createAlignmentValue(boolean, int, int)</code>
	 * API.
	 * </p>
	 *
	 * @param value the given alignment value
	 * @param wrappingStyle the given wrapping style
	 * @return the new alignment value
	 * @see #WRAP_COMPACT
	 * @see #WRAP_COMPACT_FIRST_BREAK
	 * @see #WRAP_NEXT_PER_LINE
	 * @see #WRAP_NEXT_SHIFTED
	 * @see #WRAP_NO_SPLIT
	 * @see #WRAP_ONE_PER_LINE
	 * @see #createAlignmentValue(boolean, int, int)
	 * @exception IllegalArgumentException if the given alignment value is null, if the given
	 * wrapping style is not one of the possible wrapping styles, or if the given
	 * alignment value doesn't have a valid format.
	 */
	public static String setWrappingStyle(String value, int wrappingStyle) {
		if (value == null) {
			throw WRONG_ARGUMENT;
		}
		switch(wrappingStyle) {
			case WRAP_COMPACT :
			case WRAP_COMPACT_FIRST_BREAK :
			case WRAP_NEXT_PER_LINE :
			case WRAP_NEXT_SHIFTED :
			case WRAP_NO_SPLIT :
			case WRAP_ONE_PER_LINE :
				break;
			default:
				throw WRONG_ARGUMENT;
		}
		try {
			int existingValue = Integer.parseInt(value);
			// clear existing split bits
			existingValue &= ~(Alignment.SPLIT_MASK);
			switch(wrappingStyle) {
				case WRAP_COMPACT :
					existingValue |= Alignment.M_COMPACT_SPLIT;
					break;
				case WRAP_COMPACT_FIRST_BREAK :
					existingValue |= Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
					break;
				case WRAP_NEXT_PER_LINE :
					existingValue |= Alignment.M_NEXT_PER_LINE_SPLIT;
					break;
				case WRAP_NEXT_SHIFTED :
					existingValue |= Alignment.M_NEXT_SHIFTED_SPLIT;
					break;
				case WRAP_ONE_PER_LINE :
					existingValue |= Alignment.M_ONE_PER_LINE_SPLIT;
					break;
			}
			return String.valueOf(existingValue);
		} catch (NumberFormatException e) {
			throw WRONG_ARGUMENT;
		}
	}
}
