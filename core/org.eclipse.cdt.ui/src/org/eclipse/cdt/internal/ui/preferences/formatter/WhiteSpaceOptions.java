/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.internal.ui.preferences.formatter.SnippetPreview.PreviewSnippet;

/**
 * Manage code formatter white space options on a higher level.
 */
public final class WhiteSpaceOptions {

	/**
	 * Represents a node in the options tree.
	 */
	public abstract static class Node {

		private final InnerNode fParent;
		private final String fName;

		public int index;

		protected final Map<String, String> fWorkingValues;
		protected final ArrayList<Node> fChildren;

		public Node(InnerNode parent, Map<String, String> workingValues, String message) {
			if (workingValues == null || message == null)
				throw new IllegalArgumentException();
			fParent = parent;
			fWorkingValues = workingValues;
			fName = message;
			fChildren = new ArrayList<>();
			if (fParent != null)
				fParent.add(this);
		}

		public abstract void setChecked(boolean checked);

		public boolean hasChildren() {
			return !fChildren.isEmpty();
		}

		public List<Node> getChildren() {
			return Collections.unmodifiableList(fChildren);
		}

		public InnerNode getParent() {
			return fParent;
		}

		@Override
		public final String toString() {
			return fName;
		}

		public abstract List<PreviewSnippet> getSnippets();

		public abstract void getCheckedLeafs(List<OptionNode> list);
	}

	/**
	 * A node representing a group of options in the tree.
	 */
	public static class InnerNode extends Node {

		public InnerNode(InnerNode parent, Map<String, String> workingValues, String messageKey) {
			super(parent, workingValues, messageKey);
		}

		@Override
		public void setChecked(boolean checked) {
			for (Object element : fChildren)
				((Node) element).setChecked(checked);
		}

		public void add(Node child) {
			fChildren.add(child);
		}

		@Override
		public List<PreviewSnippet> getSnippets() {
			final ArrayList<PreviewSnippet> snippets = new ArrayList<>(fChildren.size());
			for (Object element : fChildren) {
				final List<PreviewSnippet> childSnippets = ((Node) element).getSnippets();
				for (PreviewSnippet snippet : childSnippets) {
					if (!snippets.contains(snippet))
						snippets.add(snippet);
				}
			}
			return snippets;
		}

		@Override
		public void getCheckedLeafs(List<OptionNode> list) {
			for (Node element : fChildren) {
				element.getCheckedLeafs(list);
			}
		}
	}

	/**
	 * A node representing a concrete white space option in the tree.
	 */
	public static class OptionNode extends Node {
		private final String fKey;
		private final ArrayList<PreviewSnippet> fSnippets;

		public OptionNode(InnerNode parent, Map<String, String> workingValues, String messageKey, String key,
				PreviewSnippet snippet) {
			super(parent, workingValues, messageKey);
			fKey = key;
			fSnippets = new ArrayList<>(1);
			fSnippets.add(snippet);
		}

		@Override
		public void setChecked(boolean checked) {
			fWorkingValues.put(fKey, checked ? CCorePlugin.INSERT : CCorePlugin.DO_NOT_INSERT);
		}

		public boolean getChecked() {
			return CCorePlugin.INSERT.equals(fWorkingValues.get(fKey));
		}

		@Override
		public List<PreviewSnippet> getSnippets() {
			return fSnippets;
		}

		@Override
		public void getCheckedLeafs(List<OptionNode> list) {
			if (getChecked())
				list.add(this);
		}
	}

	/**
	 * Preview snippets.
	 */

	private final PreviewSnippet FOR_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"for (int i= 0, j= argc; i < argc; i++, j--) {}" //$NON-NLS-1$
	);

	private final PreviewSnippet WHILE_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"while (condition) {} do {} while (condition);" //$NON-NLS-1$
	);

	private final PreviewSnippet CATCH_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"try { number= Math::parseInt(value); } catch (Math::Exception e) {}"); //$NON-NLS-1$

	private final PreviewSnippet IF_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"if (condition) { return foo; } else {return bar;}"); //$NON-NLS-1$

	private final PreviewSnippet SWITCH_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"switch (number) { case RED: return GREEN; case GREEN: return BLUE; case BLUE: return RED; default: return BLACK;}"); //$NON-NLS-1$

	private final PreviewSnippet METHOD_DECL_PREVIEW = new PreviewSnippet(CodeFormatter.K_CLASS_BODY_DECLARATIONS,
			"void foo() throw(E0, E1) {}" + //$NON-NLS-1$
					"void bar(int x, int y) throw() {}" + //$NON-NLS-1$
					"void* baz(int* x, int& y) {return 0;}"); //$NON-NLS-1$

	private final PreviewSnippet LAMBDA_PREVIEW = new PreviewSnippet(CodeFormatter.K_CLASS_BODY_DECLARATIONS,
			"void foo() { auto f = []()->int{return 0;};}"); //$NON-NLS-1$

	private final PreviewSnippet INITIALIZER_LIST_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"int array[]= {1, 2, 3};"); //$NON-NLS-1$

	private final PreviewSnippet ARRAY_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"int array[]= {1, 2, 3};\n" + //$NON-NLS-1$
					"array [2] = 0;\n" + //$NON-NLS-1$
					"int * parray= new int[3];" + //$NON-NLS-1$
					"delete[] parray;"); //$NON-NLS-1$

	private final PreviewSnippet METHOD_CALL_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS, "foo();\n" + //$NON-NLS-1$
			"bar(x, y);"); //$NON-NLS-1$

	private final PreviewSnippet LABEL_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"label: for (int i= 0; i<argc; i++) goto label;"); //$NON-NLS-1$

	private final PreviewSnippet SEMICOLON_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"int a= 4; foo(); bar(x, y);"); //$NON-NLS-1$

	private final PreviewSnippet CONDITIONAL_PREVIEW = new PreviewSnippet(CodeFormatter.K_TRANSLATION_UNIT,
			"bool value= condition ? true : false;"); //$NON-NLS-1$

	private final PreviewSnippet CLASS_DECL_PREVIEW = new PreviewSnippet(CodeFormatter.K_TRANSLATION_UNIT,
			"class MyClass : Base1, Base2 {};"); //$NON-NLS-1$

	private final PreviewSnippet OPERATOR_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"int a= -4 + -9; b= a++ / --number; c += 4; bool value= true && false;"); //$NON-NLS-1$

	private final PreviewSnippet CAST_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"char * s= ((char *)object);"); //$NON-NLS-1$

	private final PreviewSnippet EXPRESSION_LIST_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"a= 0, b= 1, c= 2, d= 3;"); //$NON-NLS-1$

	private final PreviewSnippet DECLARATOR_LIST_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"int a=0,b=1,c=2,d=3;\nint *e, *f;"); //$NON-NLS-1$

	private final PreviewSnippet BLOCK_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"if (true) { return 1; } else { return 2; }"); //$NON-NLS-1$

	private final PreviewSnippet NAMESPACE_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"namespace FOO { int n1; }"); //$NON-NLS-1$

	private final PreviewSnippet LINKAGE_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"extern \"C\" { void func(); }"); //$NON-NLS-1$

	private final PreviewSnippet PAREN_EXPR_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"result= (a *( b +  c + d) * (e + f));"); //$NON-NLS-1$

	private final PreviewSnippet TEMPLATES_PREVIEW = new PreviewSnippet(CodeFormatter.K_TRANSLATION_UNIT,
			"template<typename T1,typename T2> class map {};\n" + //$NON-NLS-1$
					"map<int,int> m;" //$NON-NLS-1$
	);

	private final PreviewSnippet STRUCTURED_BINDING_PREVIEW = new PreviewSnippet(CodeFormatter.K_STATEMENTS,
			"auto & [first, second, third] = init;" //$NON-NLS-1$
	);

	/**
	 * Create the tree, in this order: syntax element - position - abstract element
	 * @param workingValues
	 * @return returns roots (type <code>Node</code>)
	 */
	public List<InnerNode> createTreeBySyntaxElem(Map<String, String> workingValues) {
		final ArrayList<InnerNode> roots = new ArrayList<>();

		InnerNode element;

		element = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceOptions_pointer);
		createBeforePointerTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_before));
		createAfterPointerTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_after));
		roots.add(element);

		element = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceOptions_opening_paren);
		createBeforeOpenParenTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_before));
		createAfterOpenParenTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_after));
		roots.add(element);

		element = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceOptions_closing_paren);
		createBeforeClosingParenTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_before));
		createAfterCloseParenTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_after));
		roots.add(element);

		element = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceOptions_opening_brace);
		createBeforeOpenBraceTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_before));
		createAfterOpenBraceTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_after));
		roots.add(element);

		element = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceOptions_closing_brace);
		createBeforeClosingBraceTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_before));
		createAfterCloseBraceTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_after));
		roots.add(element);

		element = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceOptions_opening_bracket);
		createBeforeOpenBracketTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_before));
		createAfterOpenBracketTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_after));
		roots.add(element);

		element = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceOptions_closing_bracket);
		createBeforeClosingBracketTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_before));
		roots.add(element);

		element = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceOptions_operator);
		createBeforeOperatorTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_before));
		createAfterOperatorTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_after));
		roots.add(element);

		element = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceOptions_comma);
		createBeforeCommaTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_before));
		createAfterCommaTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_after));
		roots.add(element);

		element = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceOptions_colon);
		createBeforeColonTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_before));
		createAfterColonTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_after));
		roots.add(element);

		element = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceOptions_semicolon);
		createBeforeSemicolonTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_before));
		createAfterSemicolonTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_after));
		roots.add(element);

		element = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceOptions_question_mark);
		createBeforeQuestionTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_before));
		createAfterQuestionTree(workingValues,
				createChild(element, workingValues, FormatterMessages.WhiteSpaceOptions_after));
		roots.add(element);

		element = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceOptions_between_empty_parens);
		createBetweenEmptyParenTree(workingValues, element);
		roots.add(element);

		element = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceOptions_between_empty_braces);
		createBetweenEmptyBracesTree(workingValues, element);
		roots.add(element);

		element = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceOptions_between_empty_brackets);
		createBetweenEmptyBracketsTree(workingValues, element);
		roots.add(element);

		return roots;
	}

	/**
	 * Create the tree, in this order: position - syntax element - abstract
	 * element
	 * @param workingValues
	 * @return returns roots (type <code>Node</code>)
	 */
	public List<InnerNode> createAltTree(Map<String, String> workingValues) {

		final ArrayList<InnerNode> roots = new ArrayList<>();

		InnerNode parent;

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_before_pointer);
		createBeforePointerTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_after_pointer);
		createAfterPointerTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_before_opening_paren);
		createBeforeOpenParenTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_after_opening_paren);
		createAfterOpenParenTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_before_closing_paren);
		createBeforeClosingParenTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_after_closing_paren);
		createAfterCloseParenTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_between_empty_parens);
		createBetweenEmptyParenTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_before_opening_brace);
		createBeforeOpenBraceTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_after_opening_brace);
		createAfterOpenBraceTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_before_closing_brace);
		createBeforeClosingBraceTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_after_closing_brace);
		createAfterCloseBraceTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_between_empty_braces);
		createBetweenEmptyBracesTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_before_opening_bracket);
		createBeforeOpenBracketTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_after_opening_bracket);
		createAfterOpenBracketTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_before_closing_bracket);
		createBeforeClosingBracketTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_between_empty_brackets);
		createBetweenEmptyBracketsTree(workingValues, parent);

		parent = createParentNode(roots, workingValues,
				FormatterMessages.WhiteSpaceOptions_before_opening_angle_bracket);
		createBeforeOpenAngleBracketTree(workingValues, parent);

		parent = createParentNode(roots, workingValues,
				FormatterMessages.WhiteSpaceOptions_after_opening_angle_bracket);
		createAfterOpenAngleBracketTree(workingValues, parent);

		parent = createParentNode(roots, workingValues,
				FormatterMessages.WhiteSpaceOptions_before_closing_angle_bracket);
		createBeforeClosingAngleBracketTree(workingValues, parent);

		parent = createParentNode(roots, workingValues,
				FormatterMessages.WhiteSpaceOptions_after_closing_angle_bracket);
		createAfterClosingAngleBracketTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_before_operator);
		createBeforeOperatorTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_after_operator);
		createAfterOperatorTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_before_comma);
		createBeforeCommaTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_after_comma);
		createAfterCommaTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_after_colon);
		createAfterColonTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_before_colon);
		createBeforeColonTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_before_semicolon);
		createBeforeSemicolonTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_after_semicolon);
		createAfterSemicolonTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_before_question_mark);
		createBeforeQuestionTree(workingValues, parent);

		parent = createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_after_question_mark);
		createAfterQuestionTree(workingValues, parent);

		//        parent= createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_before_ellipsis);
		//        createBeforeEllipsis(workingValues, parent);
		//
		//        parent= createParentNode(roots, workingValues, FormatterMessages.WhiteSpaceOptions_after_ellipsis);
		//        createAfterEllipsis(workingValues, parent);

		return roots;
	}

	private InnerNode createParentNode(List<InnerNode> roots, Map<String, String> workingValues, String text) {
		final InnerNode parent = new InnerNode(null, workingValues, text);
		roots.add(parent);
		return parent;
	}

	public ArrayList<InnerNode> createTreeByCElement(Map<String, String> workingValues) {

		final InnerNode declarations = new InnerNode(null, workingValues,
				FormatterMessages.WhiteSpaceTabPage_declarations);
		createClassTree(workingValues, declarations);
		createDeclaratorListTree(workingValues, declarations);
		createNamespaceTree(workingValues, declarations);
		createLinkageTree(workingValues, declarations);
		//        createConstructorTree(workingValues, declarations);
		createLambdaDeclTree(workingValues, declarations);
		createMethodDeclTree(workingValues, declarations);
		createExceptionSpecificationTree(workingValues, declarations);
		createLabelTree(workingValues, declarations);
		createStructuredBindingTree(workingValues, declarations);

		final InnerNode statements = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceTabPage_statements);
		createOption(statements, workingValues, FormatterMessages.WhiteSpaceOptions_before_semicolon,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON, SEMICOLON_PREVIEW);
		createBlockTree(workingValues, statements);
		createIfStatementTree(workingValues, statements);
		createForStatementTree(workingValues, statements);
		createSwitchStatementTree(workingValues, statements);
		createDoWhileTree(workingValues, statements);
		createTryStatementTree(workingValues, statements);
		//        createReturnTree(workingValues, statements);
		//        createThrowTree(workingValues, statements);

		final InnerNode expressions = new InnerNode(null, workingValues,
				FormatterMessages.WhiteSpaceTabPage_expressions);
		createFunctionCallTree(workingValues, expressions);
		createAssignmentTree(workingValues, expressions);
		createInitializerListTree(workingValues, expressions);
		createOperatorTree(workingValues, expressions);
		createParenthesizedExpressionTree(workingValues, expressions);
		createTypecastTree(workingValues, expressions);
		createConditionalTree(workingValues, expressions);
		createExpressionListTree(workingValues, expressions);

		final InnerNode arrays = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceTabPage_arrays);
		createArrayTree(workingValues, arrays);

		final InnerNode templates = new InnerNode(null, workingValues, FormatterMessages.WhiteSpaceTabPage_templates);
		createTemplateArgumentTree(workingValues, templates);
		createTemplateParameterTree(workingValues, templates);

		final ArrayList<InnerNode> roots = new ArrayList<>();
		roots.add(declarations);
		roots.add(statements);
		roots.add(expressions);
		roots.add(arrays);
		roots.add(templates);
		return roots;
	}

	private void createBeforeQuestionTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_conditional,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL,
				CONDITIONAL_PREVIEW);
	}

	private void createBeforeSemicolonTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_for,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON_IN_FOR, FOR_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_statements,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON, SEMICOLON_PREVIEW);
	}

	private void createBeforeColonTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_conditional,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL, CONDITIONAL_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_label,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT, LABEL_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_base_clause,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_BASE_CLAUSE, CLASS_DECL_PREVIEW);

		final InnerNode switchStatement = createChild(parent, workingValues,
				FormatterMessages.WhiteSpaceOptions_switch);
		createOption(switchStatement, workingValues, FormatterMessages.WhiteSpaceOptions_case,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CASE, SWITCH_PREVIEW);
		createOption(switchStatement, workingValues, FormatterMessages.WhiteSpaceOptions_default,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_DEFAULT, SWITCH_PREVIEW);
	}

	private void createBeforeCommaTree(Map<String, String> workingValues, final InnerNode parent) {

		//        final InnerNode forStatement= createChild(parent, workingValues, FormatterMessages.WhiteSpaceOptions_for);
		//        createOption(forStatement, workingValues, FormatterMessages.WhiteSpaceOptions_initialization, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS, FOR_PREVIEW);
		//        createOption(forStatement, workingValues, FormatterMessages.WhiteSpaceOptions_incrementation, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS, FOR_PREVIEW);

		final InnerNode invocation = createChild(parent, workingValues, FormatterMessages.WhiteSpaceOptions_arguments);
		createOption(invocation, workingValues, FormatterMessages.WhiteSpaceOptions_function_call,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_INVOCATION_ARGUMENTS,
				METHOD_CALL_PREVIEW);
		createOption(invocation, workingValues, FormatterMessages.WhiteSpaceOptions_template_arguments,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_TEMPLATE_ARGUMENTS,
				TEMPLATES_PREVIEW);

		final InnerNode decl = createChild(parent, workingValues, FormatterMessages.WhiteSpaceOptions_parameters);
		createOption(decl, workingValues, FormatterMessages.WhiteSpaceOptions_function,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_DECLARATION_PARAMETERS,
				METHOD_DECL_PREVIEW);
		createOption(decl, workingValues, FormatterMessages.WhiteSpaceOptions_template_parameters,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_TEMPLATE_PARAMETERS,
				TEMPLATES_PREVIEW);

		final InnerNode lists = createChild(parent, workingValues, FormatterMessages.WhiteSpaceOptions_lists);
		createOption(lists, workingValues, FormatterMessages.WhiteSpaceOptions_declarator_list,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_DECLARATOR_LIST,
				DECLARATOR_LIST_PREVIEW);
		createOption(lists, workingValues, FormatterMessages.WhiteSpaceOptions_expression_list,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_EXPRESSION_LIST,
				EXPRESSION_LIST_PREVIEW);
		createOption(lists, workingValues, FormatterMessages.WhiteSpaceOptions_initializer_list,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_INITIALIZER_LIST,
				INITIALIZER_LIST_PREVIEW);

		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_exception_specification,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_DECLARATION_THROWS,
				METHOD_DECL_PREVIEW);

		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_structured_binding_declarations,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_STRUCTURED_BINDING_NAME_LIST,
				STRUCTURED_BINDING_PREVIEW);
	}

	private void createBeforeOperatorTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_assignment_operator,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR, OPERATOR_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_unary_operator,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_UNARY_OPERATOR, OPERATOR_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_binary_operator,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BINARY_OPERATOR, OPERATOR_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_prefix_operator,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_PREFIX_OPERATOR, OPERATOR_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_postfix_operator,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_POSTFIX_OPERATOR, OPERATOR_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_lambda_arrow_operator,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_LAMBDA_RETURN, LAMBDA_PREVIEW);
	}

	private void createBeforeClosingBracketTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_arrays,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACKET, ARRAY_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_structured_binding_declarations,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_STRUCTURED_BINDING_NAME_LIST,
				STRUCTURED_BINDING_PREVIEW);
	}

	private void createBeforeClosingAngleBracketTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_template_parameters,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_ANGLE_BRACKET_IN_TEMPLATE_PARAMETERS,
				TEMPLATES_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_template_arguments,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_ANGLE_BRACKET_IN_TEMPLATE_ARGUMENTS,
				TEMPLATES_PREVIEW);
	}

	private void createBeforeOpenBracketTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_arrays,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACKET, ARRAY_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_structured_binding_declarations,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_STRUCTURED_BINDING_NAME_LIST,
				STRUCTURED_BINDING_PREVIEW);
	}

	private void createBeforeOpenAngleBracketTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_template_parameters,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_ANGLE_BRACKET_IN_TEMPLATE_PARAMETERS,
				TEMPLATES_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_template_arguments,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_ANGLE_BRACKET_IN_TEMPLATE_ARGUMENTS,
				TEMPLATES_PREVIEW);
	}

	private void createBeforeClosingBraceTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_initializer_list,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_INITIALIZER_LIST,
				CLASS_DECL_PREVIEW);
	}

	private void createBeforeOpenBraceTree(Map<String, String> workingValues, final InnerNode parent) {

		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_class_decl,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_TYPE_DECLARATION,
				CLASS_DECL_PREVIEW);

		final InnerNode functionDecl = createChild(parent, workingValues,
				FormatterMessages.WhiteSpaceOptions_function_declaration);
		{
			createOption(functionDecl, workingValues, FormatterMessages.WhiteSpaceOptions_function,
					DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_METHOD_DECLARATION,
					METHOD_DECL_PREVIEW);
		}

		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_initializer_list,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_INITIALIZER_LIST,
				INITIALIZER_LIST_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_block,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_BLOCK, BLOCK_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_switch,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_SWITCH, SWITCH_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_namespace,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_NAMESPACE_DECLARATION,
				NAMESPACE_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_linkage,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_LINKAGE_DECLARATION,
				LINKAGE_PREVIEW);
	}

	private void createBeforeClosingParenTree(Map<String, String> workingValues, final InnerNode parent) {

		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_catch,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CATCH, CATCH_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_for,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_FOR, FOR_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_if,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_IF, IF_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_switch,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_SWITCH, SWITCH_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_while,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_WHILE, WHILE_PREVIEW);

		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_type_cast,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST, CAST_PREVIEW);

		final InnerNode decl = createChild(parent, workingValues,
				FormatterMessages.WhiteSpaceOptions_function_declaration);
		createOption(decl, workingValues, FormatterMessages.WhiteSpaceOptions_function,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_DECLARATION,
				METHOD_DECL_PREVIEW);
		createOption(decl, workingValues, FormatterMessages.WhiteSpaceOptions_exception_specification,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_EXCEPTION_SPECIFICATION,
				METHOD_DECL_PREVIEW);

		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_function_call,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_INVOCATION,
				METHOD_CALL_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_paren_expr,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHESIZED_EXPRESSION,
				PAREN_EXPR_PREVIEW);
	}

	private void createBeforePointerTree(Map<String, String> workingValues, final InnerNode parent) {

		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_function,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_POINTER_IN_METHOD_DECLARATION,
				METHOD_DECL_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_declarator_list,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_POINTER_IN_DECLARATOR_LIST,
				DECLARATOR_LIST_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_structured_binding_declarations,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_REF_QUALIFIER_IN_STRUCTURED_BINDING,
				STRUCTURED_BINDING_PREVIEW);
	}

	private void createAfterPointerTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_function,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_POINTER_IN_METHOD_DECLARATION,
				METHOD_DECL_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_declarator_list,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_POINTER_IN_DECLARATOR_LIST,
				DECLARATOR_LIST_PREVIEW);
	}

	private void createBeforeOpenParenTree(Map<String, String> workingValues, final InnerNode parent) {

		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_catch,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_CATCH, CATCH_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_for,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_FOR, FOR_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_if,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_IF, IF_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_switch,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_SWITCH, SWITCH_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_while,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_WHILE, WHILE_PREVIEW);
		//        createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_return_with_parenthesized_expression, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_PARENTHESIZED_EXPRESSION_IN_RETURN, RETURN_PREVIEW);
		//        createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_throw_with_parenthesized_expression, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_PARENTHESIZED_EXPRESSION_IN_THROW, THROW_PREVIEW);

		final InnerNode decls = createChild(parent, workingValues,
				FormatterMessages.WhiteSpaceOptions_function_declaration);
		createOption(decls, workingValues, FormatterMessages.WhiteSpaceOptions_function,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_DECLARATION,
				METHOD_DECL_PREVIEW);
		createOption(decls, workingValues, FormatterMessages.WhiteSpaceOptions_exception_specification,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_EXCEPTION_SPECIFICATION,
				METHOD_DECL_PREVIEW);

		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_function_call,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_INVOCATION,
				METHOD_CALL_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_paren_expr,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_PARENTHESIZED_EXPRESSION,
				PAREN_EXPR_PREVIEW);
	}

	private void createAfterQuestionTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_conditional,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL,
				CONDITIONAL_PREVIEW);
	}

	//	private void createBeforeEllipsis(Map workingValues, InnerNode parent) {
	//		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_vararg_parameter, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ELLIPSIS, VARARG_PARAMETER_PREVIEW);
	//	}
	//
	//	private void createAfterEllipsis(Map workingValues, InnerNode parent) {
	//		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_vararg_parameter, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ELLIPSIS, VARARG_PARAMETER_PREVIEW);
	//	}

	private void createAfterSemicolonTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_for,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR, FOR_PREVIEW);
	}

	private void createAfterColonTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_conditional,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL, CONDITIONAL_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_label,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT, LABEL_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_base_clause,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_BASE_CLAUSE, CLASS_DECL_PREVIEW);
	}

	private void createAfterCommaTree(Map<String, String> workingValues, final InnerNode parent) {

		//        final InnerNode forStatement= createChild(parent, workingValues, FormatterMessages.WhiteSpaceOptions_for); {
		//            createOption(forStatement, workingValues, FormatterMessages.WhiteSpaceOptions_initialization, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS, FOR_PREVIEW);
		//            createOption(forStatement, workingValues, FormatterMessages.WhiteSpaceOptions_incrementation, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS, FOR_PREVIEW);
		//        }
		final InnerNode invocation = createChild(parent, workingValues, FormatterMessages.WhiteSpaceOptions_arguments);
		{
			createOption(invocation, workingValues, FormatterMessages.WhiteSpaceOptions_function,
					DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_INVOCATION_ARGUMENTS,
					METHOD_CALL_PREVIEW);
			createOption(invocation, workingValues, FormatterMessages.WhiteSpaceOptions_template_arguments,
					DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_TEMPLATE_ARGUMENTS,
					TEMPLATES_PREVIEW);
		}
		final InnerNode decl = createChild(parent, workingValues, FormatterMessages.WhiteSpaceOptions_parameters);
		{
			createOption(decl, workingValues, FormatterMessages.WhiteSpaceOptions_function,
					DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_DECLARATION_PARAMETERS,
					METHOD_DECL_PREVIEW);
			createOption(decl, workingValues, FormatterMessages.WhiteSpaceOptions_template_parameters,
					DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_TEMPLATE_PARAMETERS,
					TEMPLATES_PREVIEW);
		}
		final InnerNode lists = createChild(parent, workingValues, FormatterMessages.WhiteSpaceOptions_lists);
		{
			createOption(lists, workingValues, FormatterMessages.WhiteSpaceOptions_declarator_list,
					DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_DECLARATOR_LIST,
					DECLARATOR_LIST_PREVIEW);
			createOption(lists, workingValues, FormatterMessages.WhiteSpaceOptions_expression_list,
					DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_EXPRESSION_LIST,
					EXPRESSION_LIST_PREVIEW);
			createOption(lists, workingValues, FormatterMessages.WhiteSpaceOptions_initializer_list,
					DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_INITIALIZER_LIST,
					INITIALIZER_LIST_PREVIEW);
		}

		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_exception_specification,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_DECLARATION_THROWS,
				METHOD_DECL_PREVIEW);

		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_structured_binding_declarations,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_STRUCTURED_BINDING_NAME_LIST,
				STRUCTURED_BINDING_PREVIEW);
	}

	private void createAfterOperatorTree(Map<String, String> workingValues, final InnerNode parent) {

		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_assignment_operator,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR, OPERATOR_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_unary_operator,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_UNARY_OPERATOR, OPERATOR_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_binary_operator,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_BINARY_OPERATOR, OPERATOR_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_prefix_operator,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_PREFIX_OPERATOR, OPERATOR_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_postfix_operator,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_POSTFIX_OPERATOR, OPERATOR_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_lambda_arrow_operator,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_LAMBDA_RETURN, LAMBDA_PREVIEW);
	}

	private void createAfterOpenBracketTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_arrays,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACKET, ARRAY_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_structured_binding_declarations,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_STRUCTURED_BINDING_NAME_LIST,
				STRUCTURED_BINDING_PREVIEW);
	}

	private void createAfterOpenAngleBracketTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_template_parameters,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_ANGLE_BRACKET_IN_TEMPLATE_PARAMETERS,
				TEMPLATES_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_template_arguments,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_ANGLE_BRACKET_IN_TEMPLATE_ARGUMENTS,
				TEMPLATES_PREVIEW);
	}

	private void createAfterOpenBraceTree(Map<String, String> workingValues, final InnerNode parent) {

		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_initializer_list,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACE_IN_INITIALIZER_LIST,
				INITIALIZER_LIST_PREVIEW);
	}

	private void createAfterCloseBraceTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_block,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_BRACE_IN_BLOCK, BLOCK_PREVIEW);
	}

	private void createAfterCloseParenTree(Map<String, String> workingValues, final InnerNode parent) {

		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_type_cast,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST, CAST_PREVIEW);
	}

	private void createAfterClosingAngleBracketTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_template_parameters,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_ANGLE_BRACKET_IN_TEMPLATE_PARAMETERS,
				TEMPLATES_PREVIEW);
		//createOption(parent, workingValues, "WhiteSpaceOptions.parameterized_type", DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_ANGLE_BRACKET_IN_PARAMETERIZED_TYPE_REFERENCE, TYPE_ARGUMENTS_PREVIEW); //$NON-NLS-1$
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_template_arguments,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_ANGLE_BRACKET_IN_TEMPLATE_ARGUMENTS,
				TEMPLATES_PREVIEW);
	}

	private void createAfterOpenParenTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_catch,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CATCH, CATCH_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_for,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_FOR, FOR_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_if,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_IF, IF_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_switch,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_SWITCH, SWITCH_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_while,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_WHILE, WHILE_PREVIEW);

		final InnerNode decls = createChild(parent, workingValues,
				FormatterMessages.WhiteSpaceOptions_function_declaration);
		{
			createOption(decls, workingValues, FormatterMessages.WhiteSpaceOptions_function,
					DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_DECLARATION,
					METHOD_DECL_PREVIEW);
			createOption(decls, workingValues, FormatterMessages.WhiteSpaceOptions_exception_specification,
					DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_EXCEPTION_SPECIFICATION,
					METHOD_DECL_PREVIEW);
		}
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_type_cast,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST, CAST_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_function_call,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_INVOCATION,
				METHOD_CALL_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_paren_expr,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_PARENTHESIZED_EXPRESSION,
				PAREN_EXPR_PREVIEW);
	}

	private void createBetweenEmptyParenTree(Map<String, String> workingValues, final InnerNode parent) {

		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_function_decl,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_DECLARATION,
				METHOD_DECL_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_function_call,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_INVOCATION,
				METHOD_CALL_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_exception_specification,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_EXCEPTION_SPECIFICATION,
				METHOD_CALL_PREVIEW);
	}

	private void createBetweenEmptyBracketsTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_arrays,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_BRACKETS, ARRAY_PREVIEW);
	}

	private void createBetweenEmptyBracesTree(Map<String, String> workingValues, final InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceOptions_initializer_list,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_BRACES_IN_INITIALIZER_LIST,
				INITIALIZER_LIST_PREVIEW);
	}

	// syntax element tree

	private InnerNode createClassTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_classes);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_classes_before_opening_brace_of_a_class,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_TYPE_DECLARATION,
				CLASS_DECL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_classes_before_colon_of_base_clause,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_BASE_CLAUSE, CLASS_DECL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_classes_after_colon_of_base_clause,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_BASE_CLAUSE, CLASS_DECL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_classes_before_comma_base_types,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_BASE_TYPES, CLASS_DECL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_classes_after_comma_base_types,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_BASE_TYPES, CLASS_DECL_PREVIEW);
		return root;
	}

	private InnerNode createAssignmentTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_assignments);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_assignments_before_assignment_operator,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR, OPERATOR_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_assignments_after_assignment_operator,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR, OPERATOR_PREVIEW);
		return root;
	}

	private InnerNode createOperatorTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_operators);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_operators_before_binary_operators,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_BINARY_OPERATOR, OPERATOR_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_operators_after_binary_operators,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_BINARY_OPERATOR, OPERATOR_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_operators_before_unary_operators,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_UNARY_OPERATOR, OPERATOR_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_operators_after_unary_operators,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_UNARY_OPERATOR, OPERATOR_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_operators_before_prefix_operators,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_PREFIX_OPERATOR, OPERATOR_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_operators_after_prefix_operators,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_PREFIX_OPERATOR, OPERATOR_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_operators_before_postfix_operators,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_POSTFIX_OPERATOR, OPERATOR_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_operators_after_postfix_operators,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_POSTFIX_OPERATOR, OPERATOR_PREVIEW);
		return root;
	}

	private InnerNode createLambdaDeclTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues,
				FormatterMessages.WhiteSpaceTabPage_lambda_expressions);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_lambda_before_return,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_LAMBDA_RETURN, LAMBDA_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_lambda_after_return,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_LAMBDA_RETURN, LAMBDA_PREVIEW);
		return root;
	}

	private InnerNode createMethodDeclTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_functions);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_DECLARATION,
				METHOD_DECL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_DECLARATION,
				METHOD_DECL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_closing_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_DECLARATION,
				METHOD_DECL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_between_empty_parens,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_DECLARATION,
				METHOD_DECL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_opening_brace,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_METHOD_DECLARATION,
				METHOD_DECL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_pointer,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_POINTER_IN_METHOD_DECLARATION,
				METHOD_DECL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_pointer,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_POINTER_IN_METHOD_DECLARATION,
				METHOD_DECL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_comma_in_params,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_DECLARATION_PARAMETERS,
				METHOD_DECL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_comma_in_params,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_DECLARATION_PARAMETERS,
				METHOD_DECL_PREVIEW);
		return root;
	}

	private InnerNode createExceptionSpecificationTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues,
				FormatterMessages.WhiteSpaceTabPage_exception_specifications);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_EXCEPTION_SPECIFICATION,
				METHOD_DECL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_EXCEPTION_SPECIFICATION,
				METHOD_DECL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_closing_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_EXCEPTION_SPECIFICATION,
				METHOD_DECL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_between_empty_parens,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_EXCEPTION_SPECIFICATION,
				METHOD_DECL_PREVIEW);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_comma_in_params,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_DECLARATION_THROWS,
				METHOD_DECL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_comma_in_params,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_DECLARATION_THROWS,
				METHOD_DECL_PREVIEW);
		return root;
	}

	private InnerNode createNamespaceTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_namespace);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_namespace_before_brace,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_NAMESPACE_DECLARATION,
				NAMESPACE_PREVIEW);
		return root;
	}

	private InnerNode createLinkageTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_linkage);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_linkage_before_brace,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_LINKAGE_DECLARATION,
				LINKAGE_PREVIEW);
		return root;
	}

	private InnerNode createDeclaratorListTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues,
				FormatterMessages.WhiteSpaceTabPage_declarator_list);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_declarator_list_before_comma,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_DECLARATOR_LIST,
				DECLARATOR_LIST_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_declarator_list_after_comma,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_DECLARATOR_LIST,
				DECLARATOR_LIST_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_pointer,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_POINTER_IN_DECLARATOR_LIST,
				DECLARATOR_LIST_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_pointer,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_POINTER_IN_DECLARATOR_LIST,
				DECLARATOR_LIST_PREVIEW);
		return root;
	}

	private InnerNode createExpressionListTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues,
				FormatterMessages.WhiteSpaceTabPage_expression_list);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_expression_list_before_comma,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_EXPRESSION_LIST,
				EXPRESSION_LIST_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_expression_list_after_comma,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_EXPRESSION_LIST,
				EXPRESSION_LIST_PREVIEW);
		return root;
	}

	private InnerNode createInitializerListTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues,
				FormatterMessages.WhiteSpaceTabPage_initializer_list);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_opening_brace,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_INITIALIZER_LIST,
				INITIALIZER_LIST_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_opening_brace,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACE_IN_INITIALIZER_LIST,
				INITIALIZER_LIST_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_closing_brace,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACE_IN_INITIALIZER_LIST,
				INITIALIZER_LIST_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_comma,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_INITIALIZER_LIST,
				INITIALIZER_LIST_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_comma,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_INITIALIZER_LIST,
				INITIALIZER_LIST_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_between_empty_braces,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_BRACES_IN_INITIALIZER_LIST,
				INITIALIZER_LIST_PREVIEW);
		return root;
	}

	private InnerNode createArrayTree(Map<String, String> workingValues, InnerNode parent) {
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_before_opening_bracket,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACKET, ARRAY_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_after_opening_bracket,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_BRACKET, ARRAY_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_before_closing_bracket,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_BRACKET, ARRAY_PREVIEW);
		createOption(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_between_empty_brackets,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_BRACKETS, ARRAY_PREVIEW);
		return parent;
	}

	private InnerNode createFunctionCallTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_calls);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_METHOD_INVOCATION,
				METHOD_CALL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_METHOD_INVOCATION,
				METHOD_CALL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_closing_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_METHOD_INVOCATION,
				METHOD_CALL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_between_empty_parens,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_PARENS_IN_METHOD_INVOCATION,
				METHOD_CALL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_calls_before_comma_in_function_args,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_METHOD_INVOCATION_ARGUMENTS,
				METHOD_CALL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_calls_after_comma_in_function_args,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_METHOD_INVOCATION_ARGUMENTS,
				METHOD_CALL_PREVIEW);
		//        createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_calls_before_comma_in_alloc, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_ALLOCATION_EXPRESSION, ALLOC_PREVIEW);
		//        createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_calls_after_comma_in_alloc, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ALLOCATION_EXPRESSION, ALLOC_PREVIEW);
		return root;
	}

	private InnerNode createBlockTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_blocks);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_opening_brace,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_BLOCK, BLOCK_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_closing_brace,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_BRACE_IN_BLOCK, BLOCK_PREVIEW);
		return root;
	}

	private InnerNode createSwitchStatementTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_switch);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_switch_before_case_colon,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CASE, SWITCH_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_switch_before_default_colon,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_DEFAULT, SWITCH_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_opening_brace,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_BRACE_IN_SWITCH, SWITCH_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_SWITCH, SWITCH_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_SWITCH, SWITCH_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_closing_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_SWITCH, SWITCH_PREVIEW);
		return root;
	}

	private InnerNode createDoWhileTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_do);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_WHILE, WHILE_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_WHILE, WHILE_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_closing_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_WHILE, WHILE_PREVIEW);

		return root;
	}

	private InnerNode createTryStatementTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_try);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_CATCH, CATCH_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CATCH, CATCH_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_closing_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CATCH, CATCH_PREVIEW);
		return root;
	}

	private InnerNode createIfStatementTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_if);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_IF, IF_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_IF, IF_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_closing_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_IF, IF_PREVIEW);
		return root;
	}

	private InnerNode createForStatementTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_for);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_FOR, FOR_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_FOR, FOR_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_closing_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_FOR, FOR_PREVIEW);
		//        createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_for_before_comma_init, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INITS, FOR_PREVIEW);
		//        createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_for_after_comma_init, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INITS, FOR_PREVIEW);
		//        createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_for_before_comma_inc, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_FOR_INCREMENTS, FOR_PREVIEW);
		//        createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_for_after_comma_inc, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_FOR_INCREMENTS, FOR_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_semicolon,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON_IN_FOR, FOR_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_semicolon,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR, FOR_PREVIEW);

		return root;
	}

	//    private InnerNode createReturnTree(Map workingValues, InnerNode parent) {
	//    	final InnerNode root= new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceOptions_return);
	//    	createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_parenthesized_expressions, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_PARENTHESIZED_EXPRESSION_IN_RETURN, RETURN_PREVIEW);
	//    	return root;
	//    }

	//    private InnerNode createThrowTree(Map workingValues, InnerNode parent) {
	//    	final InnerNode root= new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceOptions_throw);
	//    	createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_parenthesized_expressions, DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_PARENTHESIZED_EXPRESSION_IN_THROW, THROW_PREVIEW);
	//    	return root;
	//    }

	private InnerNode createLabelTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_labels);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_colon,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_LABELED_STATEMENT, LABEL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_colon,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_LABELED_STATEMENT, LABEL_PREVIEW);
		return root;
	}

	private void createStructuredBindingTree(Map<String, String> workingValues, InnerNode parent) {
		InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_structured_bindings);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceOptions_structured_binding_before_ref_qualifier,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_REF_QUALIFIER_IN_STRUCTURED_BINDING,
				STRUCTURED_BINDING_PREVIEW);
		createOption(root, workingValues,
				FormatterMessages.WhiteSpaceOptions_structured_binding_before_name_list_opening_bracket,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_STRUCTURED_BINDING_NAME_LIST,
				STRUCTURED_BINDING_PREVIEW);
		createOption(root, workingValues,
				FormatterMessages.WhiteSpaceOptions_structured_binding_before_name_list_closing_bracket,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_STRUCTURED_BINDING_NAME_LIST,
				STRUCTURED_BINDING_PREVIEW);
		createOption(root, workingValues,
				FormatterMessages.WhiteSpaceOptions_structured_binding_before_first_name_in_list,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_STRUCTURED_BINDING_NAME_LIST,
				STRUCTURED_BINDING_PREVIEW);
		createOption(root, workingValues,
				FormatterMessages.WhiteSpaceOptions_structured_binding_before_comma_in_name_list,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_STRUCTURED_BINDING_NAME_LIST,
				STRUCTURED_BINDING_PREVIEW);
		createOption(root, workingValues,
				FormatterMessages.WhiteSpaceOptions_structured_binding_after_comma_in_name_list,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_STRUCTURED_BINDING_NAME_LIST,
				STRUCTURED_BINDING_PREVIEW);
	}

	private InnerNode createTemplateArgumentTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues,
				FormatterMessages.WhiteSpaceTabPage_template_arguments);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_opening_angle_bracket,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_ANGLE_BRACKET_IN_TEMPLATE_ARGUMENTS,
				TEMPLATES_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_opening_angle_bracket,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_ANGLE_BRACKET_IN_TEMPLATE_ARGUMENTS,
				TEMPLATES_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_comma,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_TEMPLATE_ARGUMENTS,
				TEMPLATES_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_comma,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_TEMPLATE_ARGUMENTS,
				TEMPLATES_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_closing_angle_bracket,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_ANGLE_BRACKET_IN_TEMPLATE_ARGUMENTS,
				TEMPLATES_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_closing_angle_bracket,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_ANGLE_BRACKET_IN_TEMPLATE_ARGUMENTS,
				TEMPLATES_PREVIEW);
		return root;
	}

	private InnerNode createTemplateParameterTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues,
				FormatterMessages.WhiteSpaceTabPage_template_parameters);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_opening_angle_bracket,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_ANGLE_BRACKET_IN_TEMPLATE_PARAMETERS,
				TEMPLATES_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_opening_angle_bracket,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_ANGLE_BRACKET_IN_TEMPLATE_PARAMETERS,
				TEMPLATES_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_comma,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COMMA_IN_TEMPLATE_PARAMETERS,
				TEMPLATES_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_comma,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_TEMPLATE_PARAMETERS,
				TEMPLATES_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_closing_angle_bracket,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_ANGLE_BRACKET_IN_TEMPLATE_PARAMETERS,
				TEMPLATES_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_closing_angle_bracket,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_ANGLE_BRACKET_IN_TEMPLATE_PARAMETERS,
				TEMPLATES_PREVIEW);
		return root;
	}

	private InnerNode createConditionalTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_conditionals);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_question,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_QUESTION_IN_CONDITIONAL,
				CONDITIONAL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_question,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_QUESTION_IN_CONDITIONAL,
				CONDITIONAL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_colon,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_COLON_IN_CONDITIONAL, CONDITIONAL_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_colon,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COLON_IN_CONDITIONAL, CONDITIONAL_PREVIEW);
		return root;
	}

	private InnerNode createTypecastTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_typecasts);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CAST, CAST_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_closing_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_CAST, CAST_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_closing_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST, CAST_PREVIEW);
		return root;
	}

	private InnerNode createParenthesizedExpressionTree(Map<String, String> workingValues, InnerNode parent) {
		final InnerNode root = new InnerNode(parent, workingValues, FormatterMessages.WhiteSpaceTabPage_parenexpr);

		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_PARENTHESIZED_EXPRESSION,
				PAREN_EXPR_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_after_opening_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_PARENTHESIZED_EXPRESSION,
				PAREN_EXPR_PREVIEW);
		createOption(root, workingValues, FormatterMessages.WhiteSpaceTabPage_before_closing_paren,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_CLOSING_PAREN_IN_PARENTHESIZED_EXPRESSION,
				PAREN_EXPR_PREVIEW);
		return root;
	}

	private static InnerNode createChild(InnerNode root, Map<String, String> workingValues, String message) {
		return new InnerNode(root, workingValues, message);
	}

	private static OptionNode createOption(InnerNode root, Map<String, String> workingValues, String message,
			String key, PreviewSnippet snippet) {
		return new OptionNode(root, workingValues, message, key, snippet);
	}

	public static void makeIndexForNodes(List<? extends Node> tree, List<Node> flatList) {
		for (Node node : tree) {
			node.index = flatList.size();
			flatList.add(node);
			makeIndexForNodes(node.getChildren(), flatList);
		}
	}
}
