/*******************************************************************************
 * Copyright (c) 2012, 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *	   Mathias Kunter
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import static org.eclipse.cdt.core.index.IndexLocationFactory.getAbsolutePath;
import static org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit.getEndingLineNumber;
import static org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit.getNodeEndOffset;
import static org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit.getNodeOffset;
import static org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit.getStartingLineNumber;
import static org.eclipse.cdt.internal.ui.refactoring.includes.IncludeUtil.isContainedInRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import com.ibm.icu.text.Collator;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorPragmaStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.CodeGeneration;

import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.cdt.internal.core.dom.rewrite.util.ASTNodes;
import org.eclipse.cdt.internal.core.dom.rewrite.util.TextUtil;
import org.eclipse.cdt.internal.core.parser.scanner.CharArray;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeGuardDetection;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;
import org.eclipse.cdt.internal.corext.codemanipulation.IncludeInfo;
import org.eclipse.cdt.internal.corext.codemanipulation.StyledInclude;
import org.eclipse.cdt.internal.formatter.ChangeFormatter;

/**
 * Organizes the include directives and forward declarations of a source or header file.
 */
public class IncludeOrganizer {
	private static boolean DEBUG_HEADER_SUBSTITUTION =
			"true".equalsIgnoreCase(Platform.getDebugOption(CUIPlugin.PLUGIN_ID + "/debug/includeOrganizer/headerSubstitution")); //$NON-NLS-1$ //$NON-NLS-2$

	private static final Collator COLLATOR = Collator.getInstance();

	/**
	 * Represents a new or an existing include statement.
	 */
	private static class IncludePrototype extends StyledInclude {
		private final boolean required; // true if the header has to be included

		/** Initializes an include prototype object for a new include */
		IncludePrototype(IPath header, IncludeInfo includeInfo, IncludeGroupStyle style) {
			super(header, includeInfo, style);
			this.required = true;
		}

		/**
		 * Initializes an include prototype object for an existing include. {@code header} may be
		 * {@code null} if the include was not resolved.
		 */
		IncludePrototype(IPath header, IncludeInfo includeInfo,	IncludeGroupStyle style,
				IASTPreprocessorIncludeStatement existingInclude) {
			super(header, includeInfo, style, existingInclude);
			this.required = false;
		}

		public boolean isRequired() {
			return required;
		}
	}

	private static enum DeclarationType { TYPE, FUNCTION, VARIABLE, NAMESPACE }

	private static class ForwardDeclarationNode implements Comparable<ForwardDeclarationNode> {
		final String name;
		final String declaration;
		final DeclarationType type;
		final List<ForwardDeclarationNode> children;

		/**
		 * Creates a namespace node.
		 */
		ForwardDeclarationNode(String name) {
			this.name = name;
			this.declaration = null;
			this.type = DeclarationType.NAMESPACE;
			this.children = new ArrayList<ForwardDeclarationNode>();
		}

		/**
		 * Creates a declaration node.
		 */
		ForwardDeclarationNode(String name, String declaration, DeclarationType type) {
			this.name = name;
			this.declaration = declaration;
			this.type = type;
			this.children = null;
		}

		ForwardDeclarationNode findOrAddChild(ForwardDeclarationNode node) {
			int i = Collections.binarySearch(children, node);
			if (i >= 0)
				return children.get(i);
			children.add(-(i + 1), node);
			return node;
		}

		@Override
		public int compareTo(ForwardDeclarationNode other) {
			int c = type.ordinal() - other.type.ordinal();
			if (c != 0)
				return c;
			c = COLLATOR.compare(name, other.name);
			if (declaration == null || c != 0)
				return c;
			return COLLATOR.compare(declaration, other.declaration);
		}
	}

	private final IHeaderChooser fHeaderChooser;
	private final IncludeCreationContext fContext;
	private final String fLineDelimiter;

	public IncludeOrganizer(ITranslationUnit tu, IIndex index, String lineDelimiter,
			IHeaderChooser headerChooser) {
		fLineDelimiter = lineDelimiter;
		fHeaderChooser = headerChooser;
		fContext = new IncludeCreationContext(tu, index);
	}

	/**
	 * Organizes the includes for a given translation unit.
	 * @param ast The AST translation unit to process.
	 * @throws CoreException
	 */
	public MultiTextEdit organizeIncludes(IASTTranslationUnit ast) throws CoreException {
		// Process the given translation unit with the inclusion resolver.
		BindingClassifier bindingClassifier = new BindingClassifier(fContext);
		bindingClassifier.classifyNodeContents(ast);
		Set<IBinding> bindingsToDefine = bindingClassifier.getBindingsToDefine();

		IASTPreprocessorIncludeStatement[] existingIncludes = ast.getIncludeDirectives();
		fContext.addHeadersIncludedPreviously(existingIncludes);

		HeaderSubstitutor headerSubstitutor = new HeaderSubstitutor(fContext);
		// Create the list of header files which have to be included by examining the list of
		// bindings which have to be defined.
		IIndexFileSet reachableHeaders = ast.getIndexFileSet();

		List<InclusionRequest> requests = createInclusionRequests(ast, bindingsToDefine, false, reachableHeaders);
		processInclusionRequests(requests, headerSubstitutor);

		// Use a map instead of a set to be able to retrieve existing elements using equal elements.
		// Maps each element to itself. 
		Map<IncludePrototype, IncludePrototype> includePrototypes =
				new HashMap<IncludePrototype, IncludePrototype>();
		// Put the new includes into includePrototypes.
		for (IPath header : fContext.getHeadersToInclude()) {
			IncludeGroupStyle style = fContext.getIncludeStyle(header);
			IncludeInfo includeInfo = fContext.createIncludeInfo(header, style);
			IncludePrototype prototype = new IncludePrototype(header, includeInfo, style);
			updateIncludePrototypes(includePrototypes, prototype);
		}
		// Add existing includes to includePrototypes.
		for (IASTPreprocessorIncludeStatement include : existingIncludes) {
			if (include.isPartOfTranslationUnitFile()) {
				String name = new String(include.getName().getSimpleID());
				IncludeInfo includeInfo = new IncludeInfo(name, include.isSystemInclude());
				String path = include.getPath();
				// An empty path means that the include was not resolved.
				IPath header = path.isEmpty() ? null : Path.fromOSString(path);
				IncludeGroupStyle style =
						header != null ? fContext.getIncludeStyle(header) : fContext.getIncludeStyle(includeInfo);
				IncludePrototype prototype = new IncludePrototype(header, includeInfo, style, include);
				updateIncludePrototypes(includePrototypes, prototype);
			}
		}

		NodeCommentMap commentedNodeMap = ASTCommenter.getCommentedNodeMap(ast);
		IRegion includeReplacementRegion =
				getSafeIncludeReplacementRegion(fContext.getSourceContents(), ast, commentedNodeMap);
		
		IncludePreferences preferences = fContext.getPreferences();
		boolean allowReordering = preferences.allowReordering || existingIncludes.length == 0;

		MultiTextEdit rootEdit = new MultiTextEdit();

		@SuppressWarnings("unchecked")
		List<IncludePrototype>[] groupedPrototypes =
				(List<IncludePrototype>[]) new List<?>[preferences.includeStyles.size()];
		for (IncludePrototype prototype : includePrototypes.keySet()) {
			if (prototype.getExistingInclude() == null
					|| (allowReordering && isContainedInRegion(prototype.getExistingInclude(), includeReplacementRegion))) {
				IncludeGroupStyle groupingStyle = prototype.getStyle().getGroupingStyle(preferences.includeStyles);
				// If reordering is not allowed, group everything together. 
				int position = allowReordering ? groupingStyle.getOrder() : 0;
				List<IncludePrototype> prototypes = groupedPrototypes[position];
				if (prototypes == null) {
					prototypes = new ArrayList<IncludePrototype>();
					groupedPrototypes[position] = prototypes;
				}
				prototypes.add(prototype);
			}
			if (!allowReordering && prototype.getExistingInclude() != null
					&& !prototype.isRequired() && prototype.getHeader() != null // Unused and resolved.
					&& !fContext.isPartnerFile(prototype.getHeader())
					&& isContainedInRegion(prototype.getExistingInclude(), includeReplacementRegion)) {
				switch (preferences.unusedStatementsDisposition) {
				case REMOVE:
					createDelete(prototype.getExistingInclude(), rootEdit);
					break;
				case COMMENT_OUT:
					createCommentOut(prototype.getExistingInclude(), rootEdit);
					break;
				case KEEP:
					break;
				}
			}
		}

		List<String> includeDirectives = new ArrayList<String>();
		IncludeGroupStyle previousStyle = null;
		for (List<IncludePrototype> prototypes : groupedPrototypes) {
			if (prototypes != null && !prototypes.isEmpty()) {
				Collections.sort(prototypes, preferences);
				IncludeGroupStyle style = prototypes.get(0).getStyle();
				if (!includeDirectives.isEmpty() &&
						style.isBlankLineNeededAfter(previousStyle, preferences.includeStyles)) {
					includeDirectives.add(""); // Blank line separator //$NON-NLS-1$
				}
				previousStyle = style;
				for (IncludePrototype prototype : prototypes) {
					String trailingComment = ""; //$NON-NLS-1$
					IASTPreprocessorIncludeStatement include = prototype.getExistingInclude();
					if (include == null
							|| (allowReordering && IncludeUtil.isContainedInRegion(include, includeReplacementRegion))) {
						if (include != null) {
							List<IASTComment> comments = commentedNodeMap.getTrailingCommentsForNode(include);
							StringBuilder buf = new StringBuilder();
							for (IASTComment comment : comments) {
								buf.append(getPrecedingWhitespace(comment));
								buf.append(comment.getRawSignature());
							}
							trailingComment = buf.toString();
						}
						String directive = createIncludeDirective(prototype, trailingComment);
						if (directive != null)
							includeDirectives.add(directive);
					}
				}
			}
		}

		// Create the source code to insert into the editor.

		StringBuilder buf = new StringBuilder();
		for (String include : includeDirectives) {
			buf.append(include);
			buf.append(fLineDelimiter);
		}

		int offset = includeReplacementRegion.getOffset();
		int length = includeReplacementRegion.getLength();
		if (allowReordering) {
			if (buf.length() != 0) {
				if (offset != 0 && !TextUtil.isPreviousLineBlank(fContext.getSourceContents(), offset))
					buf.insert(0, fLineDelimiter);  // Blank line before.
			}
			
			String text = buf.toString();
			// TODO(sprigogin): Add a diff algorithm and produce narrower replacements.
			if (text.length() != length ||
					!fContext.getSourceContents().regionMatches(offset, text, 0, length)) {
				rootEdit.addChild(new ReplaceEdit(offset, length, text));
			}
		} else if (buf.length() != 0) {
			offset += length;
			rootEdit.addChild(new InsertEdit(offset, buf.toString()));
		}

		createForwardDeclarations(ast, bindingClassifier,
				includeReplacementRegion.getOffset() + includeReplacementRegion.getLength(),
				buf.length() != 0, rootEdit);

		return ChangeFormatter.formatChangedCode(new String(fContext.getSourceContents()), fContext.getTranslationUnit(), rootEdit);
	}

	/**
	 * Creates forward declarations by examining the list of bindings which have to be declared.
	 * @param pendingBlankLine 
	 */
	private void createForwardDeclarations(IASTTranslationUnit ast, BindingClassifier classifier,
			int offset, boolean pendingBlankLine, MultiTextEdit rootEdit)	throws CoreException {
		ForwardDeclarationNode typeDeclarationsRoot = new ForwardDeclarationNode(""); //$NON-NLS-1$
		ForwardDeclarationNode nonTypeDeclarationsRoot = new ForwardDeclarationNode(""); //$NON-NLS-1$

		IIndexFileSet reachableHeaders = ast.getIndexFileSet();
		Set<IBinding> bindings =
				removeBindingsDefinedInIncludedHeaders(ast, classifier.getBindingsToDeclare(), reachableHeaders);
		for (IBinding binding : bindings) {
			// Create the text of the forward declaration of this binding.
			StringBuilder declarationText = new StringBuilder();

			DeclarationType declarationType;
			// Check the type of the binding and create a corresponding forward declaration text.
			if (binding instanceof ICompositeType) {
				declarationType = DeclarationType.TYPE;
				// Forward declare a composite type.
				ICompositeType compositeType = (ICompositeType) binding;

				// Check whether this is a template type.
				ICPPTemplateDefinition templateDefinition = null;
				if (compositeType instanceof ICPPTemplateDefinition) {
					templateDefinition = (ICPPTemplateDefinition) compositeType;
				} else if (compositeType instanceof ICPPTemplateInstance) {
					templateDefinition = ((ICPPTemplateInstance) compositeType).getTemplateDefinition();
				}
				if (templateDefinition != null) {
					// Create the template text.
					declarationText.append("template "); //$NON-NLS-1$
					ICPPTemplateParameter[] templateParameters = templateDefinition.getTemplateParameters();
					for (int i = 0; i < templateParameters.length; i++) {
						ICPPTemplateParameter templateParameter = templateParameters[i];
						if (i == 0) {
							declarationText.append("<"); //$NON-NLS-1$
						}
						declarationText.append("typename "); //$NON-NLS-1$
						declarationText.append(templateParameter.getName());
						if (i != templateParameters.length - 1) {
							declarationText.append(", "); //$NON-NLS-1$
						}
					}
					if (templateParameters.length > 0) {
						declarationText.append("> "); //$NON-NLS-1$
					}
				}

				// Append the corresponding keyword.
				switch (compositeType.getKey()) {
				case ICPPClassType.k_class:
					declarationText.append("class"); //$NON-NLS-1$
					break;
				case ICompositeType.k_struct:
					declarationText.append("struct"); //$NON-NLS-1$
					break;
				case ICompositeType.k_union:
					declarationText.append("union"); //$NON-NLS-1$
					break;
				}

				// Append the name of the composite type.
				declarationText.append(' ');
				declarationText.append(binding.getName());

				// Append the semicolon.
				declarationText.append(';');
			} else if (binding instanceof IEnumeration) {
				declarationType = DeclarationType.TYPE;
				// Forward declare an enumeration class (C++11 syntax).
				declarationText.append("enum class "); //$NON-NLS-1$
				declarationText.append(binding.getName());
				declarationText.append(';');
			} else if (binding instanceof IFunction && !(binding instanceof ICPPMethod)) {
				declarationType = DeclarationType.FUNCTION;
				// Forward declare a C-style function.
				IFunction function = (IFunction) binding;

				// Append return type and function name.
				IFunctionType functionType = function.getType();
				// TODO(sprigogin): Switch to ASTWriter since ASTTypeUtil doesn't properly handle namespaces.  
				declarationText.append(ASTTypeUtil.getType(functionType.getReturnType(), false));
				declarationText.append(' ');
				declarationText.append(function.getName());
				declarationText.append('(');

				// Append parameter types and names.
				IType[] parameterTypes = functionType.getParameterTypes();
				IParameter[] parameters = function.getParameters();
				for (int i = 0; i < parameterTypes.length && i < parameters.length; i++) {
					if (i != 0) {
						declarationText.append(", "); //$NON-NLS-1$
					}
					declarationText.append(ASTTypeUtil.getType(parameterTypes[i], false));
					char lastChar = declarationText.charAt(declarationText.length() - 1);
					if (lastChar != '*' && lastChar != '&') {
						// Append a space to separate the type name from the parameter name.
						declarationText.append(' ');
					}
					declarationText.append(parameters[i].getName());
				}

				declarationText.append(");"); //$NON-NLS-1$
			} else if (binding instanceof IVariable) {
				declarationType = DeclarationType.VARIABLE;
				IVariable variable = (IVariable) binding;
				IType variableType = variable.getType();
				declarationText.append("extern "); //$NON-NLS-1$
				declarationText.append(ASTTypeUtil.getType(variableType, false));
				declarationText.append(' ');
				declarationText.append(variable.getName());
				declarationText.append(';');
			} else {
				CUIPlugin.log(new IllegalArgumentException(
						"Unexpected type of binding " + binding.getName() + //$NON-NLS-1$
						" - " + binding.getClass().getSimpleName())); //$NON-NLS-1$
				continue;
			}

			// Consider the namespace(s) of the binding.
			List<String> namespaces = new ArrayList<String>();
			try {
				IScope scope = binding.getScope();
				while (scope != null && scope.getKind() == EScopeKind.eNamespace) {
					IName scopeName = scope.getScopeName();
					if (scopeName != null) {
						namespaces.add(new String(scopeName.getSimpleID()));
					}
					scope = scope.getParent();
				}
			} catch (DOMException e) {
			}

			ForwardDeclarationNode parentNode = declarationType == DeclarationType.TYPE ?
					typeDeclarationsRoot : nonTypeDeclarationsRoot;

			Collections.reverse(namespaces);
			for (String ns : namespaces) {
				ForwardDeclarationNode node = new ForwardDeclarationNode(ns);
				parentNode = parentNode.findOrAddChild(node);
			}
			
			ForwardDeclarationNode node =
					new ForwardDeclarationNode(binding.getName(), declarationText.toString(), declarationType);
			parentNode.findOrAddChild(node);
		}

		StringBuilder buf = new StringBuilder();

		for (ForwardDeclarationNode node : typeDeclarationsRoot.children) {
			if (pendingBlankLine) {
				buf.append(fLineDelimiter);
				pendingBlankLine = false;
			}
			printNode(node, buf);
		}

		for (ForwardDeclarationNode node : nonTypeDeclarationsRoot.children) {
			if (pendingBlankLine) {
				buf.append(fLineDelimiter);
				pendingBlankLine = false;
			}
			printNode(node, buf);
		}

		if ((pendingBlankLine || buf.length() != 0) && !isBlankLineOrEndOfFile(offset))
			buf.append(fLineDelimiter);

		if (buf.length() != 0)
			rootEdit.addChild(new InsertEdit(offset, buf.toString()));
	}

	private void printNode(ForwardDeclarationNode node, StringBuilder buf) throws CoreException {
		if (node.declaration == null) {
			buf.append(CodeGeneration.getNamespaceBeginContent(fContext.getTranslationUnit(), node.name, fLineDelimiter));
			for (ForwardDeclarationNode child : node.children) {
				printNode(child, buf);
			}
			buf.append(CodeGeneration.getNamespaceEndContent(fContext.getTranslationUnit(), node.name, fLineDelimiter));
		} else {
			buf.append(node.declaration);
		}
		buf.append(fLineDelimiter);
	}

	private void createCommentOut(IASTPreprocessorIncludeStatement include, MultiTextEdit rootEdit) {
		IASTFileLocation location = include.getFileLocation();
		int offset = location.getNodeOffset();
		if (fContext.getTranslationUnit().isCXXLanguage()) {
			offset = TextUtil.getLineStart(fContext.getSourceContents(), offset);
			rootEdit.addChild(new InsertEdit(offset, "//")); //$NON-NLS-1$
		} else {
			rootEdit.addChild(new InsertEdit(offset, "/*")); //$NON-NLS-1$
			int endOffset = offset + location.getNodeLength();
			rootEdit.addChild(new InsertEdit(endOffset, "*/")); //$NON-NLS-1$
		}
	}

	private void createDelete(IASTPreprocessorIncludeStatement include, MultiTextEdit rootEdit) {
		IASTFileLocation location = include.getFileLocation();
		int offset = location.getNodeOffset();
		int endOffset = offset + location.getNodeLength();
		offset = TextUtil.getLineStart(fContext.getSourceContents(), offset);
		endOffset = TextUtil.skipToNextLine(fContext.getSourceContents(), endOffset);
		rootEdit.addChild(new DeleteEdit(offset, endOffset - offset));
	}

	private void updateIncludePrototypes(Map<IncludePrototype, IncludePrototype> includePrototypes,
			IncludePrototype prototype) {
		IncludePrototype existing = includePrototypes.get(prototype);
		if (existing == null) {
			includePrototypes.put(prototype, prototype);
		} else {
			existing.setExistingInclude(prototype.getExistingInclude());
		}
	}

	static IRegion getSafeIncludeReplacementRegion(String contents, IASTTranslationUnit ast,
			NodeCommentMap commentMap) {
		int maxSafeOffset = ast.getFileLocation().getNodeLength();
		IASTDeclaration[] declarations = ast.getDeclarations(true);
		if (declarations.length != 0)
			maxSafeOffset = declarations[0].getFileLocation().getNodeOffset();

		boolean topCommentSkipped = false;
		int includeOffset = -1;
		int includeEndOffset = -1;
		int includeGuardStatementsToSkip = getNumberOfIncludeGuardStatementsToSkip(ast);
		int includeGuardEndOffset = -1;
		for (IASTPreprocessorStatement statement : ast.getAllPreprocessorStatements()) {
			if (statement.isPartOfTranslationUnitFile()) {
				IASTFileLocation fileLocation = statement.getFileLocation();
				int offset = fileLocation.getNodeOffset();
				if (offset >= maxSafeOffset)
					break;
				int endOffset = offset + fileLocation.getNodeLength();

				if (includeGuardStatementsToSkip > 0) {
					--includeGuardStatementsToSkip;
					includeGuardEndOffset = endOffset;
					if (!commentMap.getLeadingCommentsForNode(statement).isEmpty()) {
						topCommentSkipped = true;
					}
				} else if (statement instanceof IASTPreprocessorIncludeStatement) {
					if (includeOffset < 0)
						includeOffset = offset;
					includeEndOffset = endOffset;
					includeGuardStatementsToSkip = 0;  // Just in case
				} else {
					break;
				}
			}
		}
		if (includeOffset < 0) {
			if (includeGuardEndOffset >= 0) {
				includeOffset = TextUtil.skipToNextLine(contents, includeGuardEndOffset);
			} else {
				includeOffset = 0;
			}
			if (!topCommentSkipped) {
				// Skip the first comment block near the top of the file. 
				includeOffset = skipStandaloneCommentBlock(contents, includeOffset, maxSafeOffset, ast.getComments(), commentMap);
			}
			includeEndOffset = includeOffset;
		} else {
			includeEndOffset = TextUtil.skipToNextLine(contents, includeEndOffset);
		}
		return new Region(includeOffset, includeEndOffset - includeOffset);
	}

	private static int getNumberOfIncludeGuardStatementsToSkip(IASTTranslationUnit ast) {
		IASTPreprocessorStatement statement = findFirstPreprocessorStatement(ast);
		if (statement == null)
			return 0;

		int num = 0;
		int offset = 0;
		if (isPragmaOnce(statement)) {
			num++;
			offset = getNodeEndOffset(statement); 
		}
		char[] contents = ast.getRawSignature().toCharArray();
		if (offset != 0)
			contents = Arrays.copyOfRange(contents, offset, contents.length);
		CharArrayIntMap ppKeywords= new CharArrayIntMap(40, -1);
		Keywords.addKeywordsPreprocessor(ppKeywords);
		if (IncludeGuardDetection.detectIncludeGuard(new CharArray(contents), new LexerOptions(), ppKeywords) != null) {
			num += 2;
		}
		return num;
	}

	private static IASTPreprocessorStatement findFirstPreprocessorStatement(IASTTranslationUnit ast) {
		for (IASTPreprocessorStatement statement : ast.getAllPreprocessorStatements()) {
			if (statement.isPartOfTranslationUnitFile())
				return statement;
		}
		return null;
	}

	private static boolean isPragmaOnce(IASTPreprocessorStatement statement) {
		if (!(statement instanceof IASTPreprocessorPragmaStatement))
			return false;
		return CharArrayUtils.equals(((IASTPreprocessorPragmaStatement) statement).getMessage(), "once"); //$NON-NLS-1$
	}

	/**
	 * Returns {@code true} if there are no non-whitespace characters between the given
	 * {@code offset} and the end of the line.
	 */
	private boolean isBlankLineOrEndOfFile(int offset) {
		String contents = fContext.getSourceContents();
		while (offset < contents.length()) {
			char c = contents.charAt(offset++);
			if (c == '\n')
				return true;
			if (!Character.isWhitespace(c))
				return false;
		}
		return true;
	}

	/**
	 * Returns the whitespace preceding the given node. The newline character in not considered
	 * whitespace for the purpose of this method.
	 */
	private String getPrecedingWhitespace(IASTNode node) {
		int offset = getNodeOffset(node);
		if (offset >= 0) {
			String contents = fContext.getSourceContents();
			int i = offset;
			while (--i >= 0) {
				char c = contents.charAt(i);
				if (c == '\n' || !Character.isWhitespace(c))
					break;
			}
			i++;
			return contents.substring(i, offset);
		}
		return ""; //$NON-NLS-1$
	}

	private static int skipStandaloneCommentBlock(String contents, int offset, int endOffset,
			IASTComment[] comments, NodeCommentMap commentMap) {
		Map<IASTComment, IASTNode> inverseLeadingMap = new HashMap<IASTComment, IASTNode>();
		for (Map.Entry<IASTNode, List<IASTComment>> entry : commentMap.getLeadingMap().entrySet()) {
			IASTNode node = entry.getKey();
			if (getNodeOffset(node) <= endOffset) {
				for (IASTComment comment : entry.getValue()) {
					inverseLeadingMap.put(comment, node);
				}
			}
		}
		Map<IASTComment, IASTNode> inverseFreestandingMap = new HashMap<IASTComment, IASTNode>();
		for (Map.Entry<IASTNode, List<IASTComment>> entry : commentMap.getFreestandingMap().entrySet()) {
			IASTNode node = entry.getKey();
			if (getNodeEndOffset(node) < endOffset) {
				for (IASTComment comment : entry.getValue()) {
					inverseFreestandingMap.put(comment, node);
				}
			}
		}

		for (int i = 0; i < comments.length; i++) {
			IASTComment comment = comments[i];
			int commentOffset = getNodeOffset(comment);
			if (commentOffset >= offset) {
				if (commentOffset >= endOffset)
					break;
				IASTNode node = inverseLeadingMap.get(comment);
				if (node != null) {
					List<IASTComment> leadingComments = commentMap.getLeadingMap().get(node);
					IASTComment previous = leadingComments.get(0);
					for (int j = 1; j < leadingComments.size(); j++) {
						comment = leadingComments.get(j);
						if (getStartingLineNumber(comment) > getEndingLineNumber(previous) + 1)
							return ASTNodes.skipToNextLineAfterNode(contents, previous);
						previous = comment;
					}
					if (getStartingLineNumber(node) > getEndingLineNumber(previous) + 1)
						return ASTNodes.skipToNextLineAfterNode(contents, previous);
				}
				node = inverseFreestandingMap.get(comment);
				if (node != null) {
					List<IASTComment> freestandingComments = commentMap.getFreestandingMap().get(node);
					IASTComment previous = freestandingComments.get(0);
					for (int j = 1; j < freestandingComments.size(); j++) {
						comment = freestandingComments.get(j);
						if (getStartingLineNumber(comment) > getEndingLineNumber(previous) + 1)
							return ASTNodes.skipToNextLineAfterNode(contents, previous);
						previous = comment;
					}
				}
			}
		}
		return offset;
	}

	private Set<IBinding> removeBindingsDefinedInIncludedHeaders(IASTTranslationUnit ast,
			Set<IBinding> bindings, IIndexFileSet reachableHeaders) throws CoreException {
		Set<IBinding> filteredBindings = new HashSet<IBinding>(bindings);

		List<InclusionRequest> requests = createInclusionRequests(ast, bindings, true, reachableHeaders);
		Set<IPath> allIncludedHeaders = new HashSet<IPath>();
		allIncludedHeaders.addAll(fContext.getHeadersAlreadyIncluded());
		allIncludedHeaders.addAll(fContext.getHeadersToInclude());

		for (InclusionRequest request : requests) {
			if (isSatisfiedByIncludedHeaders(request, allIncludedHeaders))
				filteredBindings.remove(request.getBinding());
		}
		return filteredBindings;
	}

	protected boolean isSatisfiedByIncludedHeaders(InclusionRequest request, Set<IPath> includedHeaders)
			throws CoreException {
		for (IIndexFile file : request.getDeclaringFiles().keySet()) {
			IPath path = getAbsolutePath(file.getLocation());
			if (includedHeaders.contains(path))
				return true;

			IIndexInclude[] includedBy = fContext.getIndex().findIncludedBy(file, IIndex.DEPTH_INFINITE);
			for (IIndexInclude include : includedBy) {
				path = getAbsolutePath(include.getIncludedByLocation());
				if (includedHeaders.contains(path))
					return true;
			}
		}
		return false;
	}

	private void processInclusionRequests(List<InclusionRequest> requests,
			HeaderSubstitutor headerSubstitutor) throws CoreException {
		// Add partner header if necessary.
		HashSet<IIndexFile> includedByPartner = fContext.getPreferences().allowPartnerIndirectInclusion ?
				new HashSet<IIndexFile>() : null;
		for (InclusionRequest request : requests) {
			List<IPath> candidatePaths = request.getCandidatePaths();
			if (candidatePaths.size() == 1) {
				IPath path = candidatePaths.iterator().next();
				if (fContext.isPartnerFile(path)) {
					request.resolve(path);
					fContext.addHeaderToInclude(path);
					if (includedByPartner != null) {
						try {
							IIndexFile indexFile = request.getDeclaringFiles().keySet().iterator().next();
							if (!includedByPartner.contains(indexFile)) {
								for (IIndexInclude include : indexFile.getIncludes()) {
									IIndexFileLocation headerLocation = include.getIncludesLocation();
									if (headerLocation != null) {
										fContext.addHeaderAlreadyIncluded(getAbsolutePath(headerLocation));
									}
								}
								includedByPartner.add(indexFile);
							}
						} catch (CoreException e) {
							CUIPlugin.log(e);
						}
					}
				}
			}
		}

		// Process headers that are either indirectly included or have unique representatives.
		for (InclusionRequest request : requests) {
			if (!request.isResolved() && !isExportedBinding(request, headerSubstitutor)) {
				List<IPath> candidatePaths = request.getCandidatePaths();
				Set<IPath> representativeHeaders = new HashSet<IPath>();
				Set<IPath> representedHeaders = new HashSet<IPath>();
				boolean allRepresented = true;
				for (IPath path : candidatePaths) {
					if (fContext.isIncluded(path)) {
						request.resolve(path);
						if (DEBUG_HEADER_SUBSTITUTION) {
							System.out.println(request.toString() +
									(fContext.isToBeIncluded(path) ? " (decided earlier)" : " (was previously included)")); //$NON-NLS-1$ //$NON-NLS-2$
						}
						break;
					} else {
						IPath header = headerSubstitutor.getUniqueRepresentativeHeader(path);
						if (header != null) {
							representativeHeaders.add(header);
							representedHeaders.add(path);
						} else {
							allRepresented = false;
						}
					}
				}

				if (!request.isResolved() && allRepresented && representativeHeaders.size() == 1) {
					IPath path = representativeHeaders.iterator().next();
					request.resolve(path);
					if (DEBUG_HEADER_SUBSTITUTION)
						System.out.println(request.toString() + " (unique representative)"); //$NON-NLS-1$
					if (!fContext.isAlreadyIncluded(path))
						fContext.addHeaderToInclude(path);
					for (IPath header : representedHeaders) {
						if (!header.equals(path))
							fContext.addHeaderAlreadyIncluded(header);
					}
				}
			}
		}

		// Process remaining unambiguous inclusion requests.
		for (InclusionRequest request : requests) {
			if (!request.isResolved() && !isExportedBinding(request, headerSubstitutor)) {
				List<IPath> candidatePaths = request.getCandidatePaths();
				if (candidatePaths.size() == 1) {
					IPath path = candidatePaths.iterator().next();
					if (fContext.isIncluded(path)) {
						request.resolve(path);
						if (DEBUG_HEADER_SUBSTITUTION) {
							System.out.println(request.toString() +
									(fContext.isToBeIncluded(path) ? " (decided earlier)" : " (was previously included)")); //$NON-NLS-1$ //$NON-NLS-2$
						}
					} else {
						IPath header = headerSubstitutor.getPreferredRepresentativeHeader(path);
						if (header.equals(path) && fContext.getPreferences().heuristicHeaderSubstitution) {
							header = headerSubstitutor.getPreferredRepresentativeHeaderByHeuristic(request);
						}
						request.resolve(header);
						if (DEBUG_HEADER_SUBSTITUTION) {
							System.out.println(request.toString() + " (preferred representative)"); //$NON-NLS-1$
						}
						if (!fContext.isAlreadyIncluded(header))
							fContext.addHeaderToInclude(header);
						if (!header.equals(path))
							fContext.addHeaderAlreadyIncluded(path);
					}
				}
			}
		}

		// Resolve ambiguous inclusion requests.
		for (InclusionRequest request : requests) {
			if (!request.isResolved() && !isExportedBinding(request, headerSubstitutor)) {
				List<IPath> candidatePaths = request.getCandidatePaths();
				for (IPath path : candidatePaths) {
					if (fContext.isIncluded(path)) {
						request.resolve(path);
						if (DEBUG_HEADER_SUBSTITUTION) {
							System.out.println(request.toString() +
									(fContext.isToBeIncluded(path) ? " (decided earlier)" : " (was previously included)")); //$NON-NLS-1$ //$NON-NLS-2$
						}
						break;
					}
				}
				if (!request.isResolved()) {
					IPath header = fHeaderChooser.chooseHeader(request.getBinding().getName(), candidatePaths);
					if (header == null)
						throw new OperationCanceledException();
	
					request.resolve(header);
					if (DEBUG_HEADER_SUBSTITUTION) {
						System.out.println(request.toString() + " (user's choice)"); //$NON-NLS-1$
					}
					if (!fContext.isAlreadyIncluded(header))
						fContext.addHeaderToInclude(header);
				}
			}
		}

		// Resolve requests for exported symbols.
		for (InclusionRequest request : requests) {
			if (!request.isResolved()) {
				IPath firstIncludedPreviously = null;
				Set<IncludeInfo> exportingHeaders = getExportingHeaders(request, headerSubstitutor);
				for (IncludeInfo header : exportingHeaders) {
					IPath path = fContext.resolveInclude(header);
					if (path != null) {
						if (fContext.isIncluded(path)) {
							request.resolve(path);
							if (DEBUG_HEADER_SUBSTITUTION) {
								System.out.println(request.toString() +
										(fContext.isToBeIncluded(path) ? " (decided earlier)" : " (was previously included)")); //$NON-NLS-1$ //$NON-NLS-2$
							}
							break;
						}
						if (firstIncludedPreviously == null && fContext.wasIncludedPreviously(path))
							firstIncludedPreviously = path;
					}
				}
				if (request.isResolved())
					continue;

				List<IPath> candidatePaths = request.getCandidatePaths();
				for (IPath path : candidatePaths) {
					if (fContext.isIncluded(path)) {
						request.resolve(path);
						if (DEBUG_HEADER_SUBSTITUTION) {
							System.out.println(request.toString() +
									(fContext.isToBeIncluded(path) ? " (decided earlier)" : " (was previously included)")); //$NON-NLS-1$ //$NON-NLS-2$
						}
						break;
					}
					if (firstIncludedPreviously == null && fContext.wasIncludedPreviously(path))
						firstIncludedPreviously = path;
				}

				if (request.isResolved())
					continue;

				if (firstIncludedPreviously != null) {
					request.resolve(firstIncludedPreviously);
					if (DEBUG_HEADER_SUBSTITUTION) {
						System.out.println(request.toString() + " (present in old includes)"); //$NON-NLS-1$
					}
					if (!fContext.isAlreadyIncluded(firstIncludedPreviously))
						fContext.addHeaderToInclude(firstIncludedPreviously);
				}

				if (!request.isResolved()) {
					IPath header = fHeaderChooser.chooseHeader(request.getBinding().getName(), candidatePaths);
					if (header == null)
						throw new OperationCanceledException();
	
					request.resolve(header);
					if (DEBUG_HEADER_SUBSTITUTION) {
						System.out.println(request.toString() +
								(candidatePaths.size() == 1 ? " (the only choice)" : " (user's choice)")); //$NON-NLS-1$ //$NON-NLS-2$
					}
					if (!fContext.isAlreadyIncluded(header))
						fContext.addHeaderToInclude(header);
				}
			}
		}

		// Remove headers that are exported by other headers.
		fContext.removeExportedHeaders();
	}

	private boolean isExportedBinding(InclusionRequest request, HeaderSubstitutor headerSubstitutor) {
		return !getExportingHeaders(request, headerSubstitutor).isEmpty();
	}

	private Set<IncludeInfo> getExportingHeaders(InclusionRequest request, HeaderSubstitutor headerSubstitutor) {
		String symbol = request.getBindingQualifiedName();
		if (symbol == null)
			return Collections.emptySet();
		return headerSubstitutor.getExportingHeaders(symbol);
	}

	private List<InclusionRequest> createInclusionRequests(IASTTranslationUnit ast,
			Set<IBinding> bindingsToDefine, boolean allowDeclarations,
			IIndexFileSet reachableHeaders) throws CoreException {
		List<InclusionRequest> requests = new ArrayList<InclusionRequest>(bindingsToDefine.size());
		IIndex index = fContext.getIndex();

		binding_loop: for (IBinding binding : bindingsToDefine) {
			IIndexName[] indexNames;
			if (binding instanceof IMacroBinding) {
				indexNames = IIndexName.EMPTY_ARRAY;
	    		ILocationResolver resolver = (ILocationResolver) ast.getAdapter(ILocationResolver.class);
	    		IASTName[] declarations = resolver.getDeclarations((IMacroBinding) binding);
	    		for (IASTName name : declarations) {
	    			if (name instanceof IAdaptable) {
	    				IIndexName indexName = (IIndexName) ((IAdaptable) name).getAdapter(IIndexName.class);
	    				if (indexName != null) {
		    				indexNames = Arrays.copyOf(indexNames, indexNames.length + 1);
		    				indexNames[indexNames.length - 1] = indexName;
	    				}
	    			}
	    		}
			} else if (allowDeclarations || binding instanceof IFunction || binding instanceof IVariable) {
				// For functions and variables we need to include a declaration.
				indexNames = index.findDeclarations(binding);
			} else {
				// For all other bindings we need to include the definition.
				indexNames = index.findDefinitions(binding);
				if (indexNames.length == 0) {
					// If we could not find any definitions, there is still a chance that
					// a declaration would be sufficient.
					indexNames = index.findDeclarations(binding);
				}
			}

			if (indexNames.length != 0) {
				// Check whether the index name is (also) present within the current file.
				// If yes, we don't need to include anything.
				for (IIndexName indexName : indexNames) {
					IIndexFile indexFile = indexName.getFile();
					if (indexFile.getLocation().getURI().equals(fContext.getTranslationUnit().getLocationURI())) {
						continue binding_loop;
					}
				}

				Map<IIndexFile, IPath> declaringHeaders = new HashMap<IIndexFile, IPath>();
				Map<IIndexFile, IPath> reachableDeclaringHeaders = new HashMap<IIndexFile, IPath>();
				for (IIndexName indexName : indexNames) {
					IIndexFile indexFile = indexName.getFile();
					if (IncludeUtil.isSource(indexFile, fContext.getProject()) &&
							index.findIncludedBy(indexFile, 0).length == 0) {
						// The target is a source file which isn't included by any other files.
						// Don't include it.
						continue;
					}
					IPath path = getAbsolutePath(indexFile.getLocation());
					declaringHeaders.put(indexFile, path);
					if (reachableHeaders.contains(indexFile))
						reachableDeclaringHeaders.put(indexFile, path);
				}

				if (!declaringHeaders.isEmpty()) {
					boolean reachable = false;
					if (!reachableDeclaringHeaders.isEmpty()) {
						reachable = true;
						declaringHeaders = reachableDeclaringHeaders;
					}
					requests.add(new InclusionRequest(binding, declaringHeaders, reachable));
				}
			}
		}
		return requests;
	}

	private String createIncludeDirective(IncludePrototype include, String lineComment) {
		StringBuilder buf = new StringBuilder();
		// Unresolved includes are preserved out of caution. Partner include is always preserved.
		if (!include.isRequired() && include.getHeader() != null
				&& !fContext.isPartnerFile(include.getHeader())) {
			switch (fContext.getPreferences().unusedStatementsDisposition) {
			case REMOVE:
				return null;
			case COMMENT_OUT:
				buf.append("//"); //$NON-NLS-1$
				break;
			case KEEP:
				break;
			}
		}
		buf.append(include.getIncludeInfo().composeIncludeStatement());
		buf.append(lineComment);
		return buf.toString();
	}
}
