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

import static org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit.getEndingLineNumber;
import static org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit.getNodeEndOffset;
import static org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit.getNodeOffset;
import static org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit.getStartingLineNumber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

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
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.PathUtil;

import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;
import org.eclipse.cdt.internal.core.parser.scanner.CharArray;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeGuardDetection;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;

import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle.IncludeKind;

/**
 * Organizes the include directives and forward declarations of a source or header file.
 */
public class IncludeOrganizer {
	private static boolean DEBUG_HEADER_SUBSTITUTION = "true".equalsIgnoreCase(Platform.getDebugOption(CUIPlugin.PLUGIN_ID + "/debug/includeOrganizer/headerSubstitution")); //$NON-NLS-1$ //$NON-NLS-2$

	private static class IncludePrototype implements Comparable<IncludePrototype> {
		final IPath header;  // null for existing unresolved includes 
		final IncludeInfo includeInfo; // never null
		IASTPreprocessorIncludeStatement existingInclude; // null for newly added includes
		final boolean required; // true if the header has to be included
		final IncludeGroupStyle style;

		/** Initializes an include prototype for a new include */
		IncludePrototype(IPath header, IncludeInfo includeInfo, IncludeGroupStyle style) {
			if (includeInfo == null)
				throw new NullPointerException();
			this.header = header;
			this.includeInfo = includeInfo;
			this.style = style;
			this.required = true;
		}

		/**
		 * Initializes an include prototype for an existing include. {@code header} may be
		 * {@code null} if the include was not resolved.
		 */
		IncludePrototype(IASTPreprocessorIncludeStatement include, IPath header,
				IncludeInfo includeInfo, IncludeGroupStyle style) {
			if (includeInfo == null)
				throw new NullPointerException();
			this.existingInclude = include;
			this.header = header;
			this.includeInfo = includeInfo;
			this.style = style;
			this.required = false;
		}

		public void updateFrom(IncludePrototype other) {
			this.existingInclude = other.existingInclude;
		}

		@Override
		public int hashCode() {
			if (header != null)
				return header.hashCode();  // includeInfo is ignored if header is not null
			return includeInfo.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IncludePrototype other = (IncludePrototype) obj;
			if (header != null)
				return header.equals(other.header);  // includeInfo is ignored if header is not null
			if (other.header != null)
				return false;
			return includeInfo.equals(other.includeInfo);
		}

		/** For debugging only */
		@Override
		public String toString() {
			return header != null ? header.toPortableString() : includeInfo.toString();
		}

		@Override
		public int compareTo(IncludePrototype other) {
			return includeInfo.compareTo(other.includeInfo);
		}
	}

	private static final Collator COLLATOR = Collator.getInstance();

	private final IHeaderChooser fHeaderChooser;
	private final InclusionContext fContext;
	private final String fLineDelimiter;

	public IncludeOrganizer(ITranslationUnit tu, IIndex index, String lineDelimiter,
			IHeaderChooser headerChooser) {
		fLineDelimiter = lineDelimiter;
		fHeaderChooser = headerChooser;
		fContext = new InclusionContext(tu, index);
	}

	/**
	 * Organizes the includes for a given translation unit.
	 * @param ast The AST translation unit to process.
	 * @throws CoreException
	 */
	public List<TextEdit> organizeIncludes(IASTTranslationUnit ast) throws CoreException {
		// Process the given translation unit with the inclusion resolver.
		BindingClassifier bindingClassifier = new BindingClassifier(fContext);
		bindingClassifier.classifyNodeContents(ast);
		Set<IBinding> bindingsToDefine = bindingClassifier.getBindingsToDefine();

		// Stores the forward declarations for composite types and enumerations as text.
		List<String> typeForwardDeclarations = new ArrayList<String>();
		// Stores the forward declarations for C-style functions as text.
		List<String> functionForwardDeclarations = new ArrayList<String>();

		createForwardDeclarations(ast, bindingClassifier, typeForwardDeclarations, functionForwardDeclarations,
				bindingsToDefine);

		HeaderSubstitutor headerSubstitutor = new HeaderSubstitutor(fContext);
		// Create the list of header files which have to be included by examining the list of
		// bindings which have to be defined.
		IIndexFileSet reachableHeaders = ast.getIndexFileSet();

		List<InclusionRequest> requests = createInclusionRequests(ast, bindingsToDefine, reachableHeaders);
		processInclusionRequests(requests, headerSubstitutor);

		// Use a map instead of a set to be able to retrieve existing elements using equal elements.
		// Maps each element to itself. 
		Map<IncludePrototype, IncludePrototype> includePrototypes =
				new HashMap<IncludePrototype, IncludePrototype>();
		// Put the new includes into includePrototypes.
		for (IPath header : fContext.getHeadersToInclude()) {
			IncludeGroupStyle style = getIncludeStyle(header);
			IncludeInfo includeInfo = createIncludeInfo(header, style);
			IncludePrototype prototype = new IncludePrototype(header, includeInfo, style);
			updateIncludePrototypes(includePrototypes, prototype);
		}
		// Put the existing includes into includePrototypes.
		IASTPreprocessorIncludeStatement[] existingIncludes = ast.getIncludeDirectives();
		for (IASTPreprocessorIncludeStatement include : existingIncludes) {
			if (include.isPartOfTranslationUnitFile()) {
				String name = new String(include.getName().getSimpleID());
				IncludeInfo includeInfo = new IncludeInfo(name, include.isSystemInclude());
				String path = include.getPath();
				IPath header = path.isEmpty() ? null : Path.fromOSString(path);
				IncludeGroupStyle style =
						header != null ? getIncludeStyle(header) : getIncludeStyle(includeInfo);
				IncludePrototype prototype = new IncludePrototype(include, header, includeInfo, style);
				updateIncludePrototypes(includePrototypes, prototype);
			}
		}

		NodeCommentMap commentedNodeMap = ASTCommenter.getCommentedNodeMap(ast);
		IRegion includeReplacementRegion = getSafeIncludeReplacementRegion(ast, commentedNodeMap);
		
		IncludePreferences preferences = fContext.getPreferences();
		boolean allowReordering = preferences.allowReordering || existingIncludes.length == 0;

		List<TextEdit> edits = new ArrayList<TextEdit>();

		@SuppressWarnings("unchecked")
		List<IncludePrototype>[] groupedPrototypes =
				(List<IncludePrototype>[]) new List<?>[preferences.includeStyles.size()];
		for (IncludePrototype prototype : includePrototypes.keySet()) {
			if (prototype.existingInclude == null
					|| (allowReordering && isContainedInRegion(prototype.existingInclude, includeReplacementRegion))) {
				IncludeGroupStyle groupingStyle = getGroupingStyle(prototype.style);
				// If reordering is not allowed, group everything together. 
				int position = allowReordering ? groupingStyle.getOrder() : 0;
				List<IncludePrototype> prototypes = groupedPrototypes[position];
				if (prototypes == null) {
					prototypes = new ArrayList<IncludePrototype>();
					groupedPrototypes[position] = prototypes;
				}
				prototypes.add(prototype);
			}
			if (!allowReordering && prototype.existingInclude != null
					&& !prototype.required && prototype.header != null // Unused and resolved. 
					&& isContainedInRegion(prototype.existingInclude, includeReplacementRegion)) {
				switch (preferences.unusedStatementsDisposition) {
				case REMOVE:
					createDelete(prototype.existingInclude, edits);
					break;
				case COMMENT_OUT:
					createCommentOut(prototype.existingInclude, edits);
					break;
				case KEEP:
					break;
				}
			}
		}

		List<String> includeDirectives = new ArrayList<String>();
		IncludeGroupStyle previousParentStyle = null;
		for (List<IncludePrototype> prototypes : groupedPrototypes) {
			if (prototypes != null && !prototypes.isEmpty()) {
				Collections.sort(prototypes);
				IncludeGroupStyle style = prototypes.get(0).style;
				IncludeGroupStyle groupingStyle = getGroupingStyle(style);
				IncludeGroupStyle parentStyle = getParentStyle(groupingStyle);
				boolean blankLineBefore = groupingStyle.isBlankLineBefore() ||
						(parentStyle != null && parentStyle != previousParentStyle &&
						parentStyle.isKeepTogether() && parentStyle.isBlankLineBefore());
				previousParentStyle = parentStyle;
				if (!includeDirectives.isEmpty() && blankLineBefore)
					includeDirectives.add(""); // Blank line separator //$NON-NLS-1$
				for (IncludePrototype prototype : prototypes) {
					String trailingComment = ""; //$NON-NLS-1$
					IASTPreprocessorIncludeStatement include = prototype.existingInclude;
					if (include == null
							|| (allowReordering && isContainedInRegion(include, includeReplacementRegion))) {
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

		if (buf.length() != 0 && !typeForwardDeclarations.isEmpty())
			buf.append(fLineDelimiter);
		for (String declaration : typeForwardDeclarations) {
			buf.append(declaration);
			buf.append(fLineDelimiter);
		}

		if (buf.length() != 0 && !functionForwardDeclarations.isEmpty())
			buf.append(fLineDelimiter);
		for (String declaration : functionForwardDeclarations) {
			buf.append(declaration);
			buf.append(fLineDelimiter);
		}

		int offset = includeReplacementRegion.getOffset();
		int length = includeReplacementRegion.getLength();
		if (allowReordering) {
			if (buf.length() != 0) {
				if (offset != 0 && !isPreviousLineBlank(offset))
					buf.insert(0, fLineDelimiter);  // Blank line before.
				if (!isBlankLineOrEndOfFile(offset + length))
					buf.append(fLineDelimiter);  // Blank line after.
			}
			
			String text = buf.toString();
			// TODO(sprigogin): Add a diff algorithm and produce more narrow replacements.
			if (!CharArrayUtils.equals(fContext.getTranslationUnit().getContents(), offset, length, text)) {
				edits.add(new ReplaceEdit(offset, length, text));
			}
		} else if (buf.length() != 0) {
			offset += length;
			if (!isBlankLineOrEndOfFile(offset))
				buf.append(fLineDelimiter);  // Blank line after.
			edits.add(new InsertEdit(offset, buf.toString()));
		}

		return edits;
	}

	/**
	 * Creates forward declarations by examining the list of bindings which have to be declared.
	 * Bindings that cannot be safely declared for whatever reason are added to
	 * {@code bindingsToDefine} set.
	 */
	private void createForwardDeclarations(IASTTranslationUnit ast, BindingClassifier classifier,
			List<String> forwardDeclarations, List<String> functionForwardDeclarations,
			Set<IBinding> bindingsToDefine) throws CoreException {
		IIndexFileSet reachableHeaders = ast.getIndexFileSet();
		Set<IBinding> bindings =
				removeBindingsDefinedInIncludedHeaders(ast, classifier.getBindingsToDeclare(), reachableHeaders);
		for (IBinding binding : bindings) {
			// Create the text of the forward declaration of this binding.
			StringBuilder declarationText = new StringBuilder();

			// Consider the namespace(s) of the binding.
			List<IName> scopeNames = new ArrayList<IName>();
			try {
				IScope scope = binding.getScope();
				while (scope != null && scope.getKind() == EScopeKind.eNamespace) {
					IName scopeName = scope.getScopeName();
					if (scopeName != null) {
						scopeNames.add(scopeName);
					}
					scope = scope.getParent();
				}
			} catch (DOMException e) {
			}

			Collections.reverse(scopeNames);
			for (IName scopeName : scopeNames) {
				declarationText.append("namespace "); //$NON-NLS-1$
				declarationText.append(scopeName.toString());
				declarationText.append(" { "); //$NON-NLS-1$
			}

			// Initialize the list which should be used to store the declaration.
			List<String> forwardDeclarationListToUse = forwardDeclarations;

			// Check the type of the binding and create a corresponding forward declaration text.
			if (binding instanceof ICompositeType) {
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
				// Forward declare an enumeration class (C++11 syntax).
				declarationText.append("enum class "); //$NON-NLS-1$
				declarationText.append(binding.getName());
				declarationText.append(';');
			} else if (binding instanceof IFunction && !(binding instanceof ICPPMethod)) {
				// Forward declare a C-style function.
				IFunction function = (IFunction) binding;

				// Append return type and function name.
				IFunctionType functionType = function.getType();
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

				// Add this forward declaration to the separate function forward declaration list.
				forwardDeclarationListToUse = functionForwardDeclarations;
			} else {
				// We can't create a forward declaration for this binding. The binding will have
				// to be defined.
				bindingsToDefine.add(binding);
				continue;
			}

			// Append the closing curly brackets from the namespaces (if any).
			for (int i = 0; i < scopeNames.size(); i++) {
				declarationText.append(" }"); //$NON-NLS-1$
			}

			// Add the forward declaration to the corresponding list.
			forwardDeclarationListToUse.add(declarationText.toString());
		}

		Collections.sort(forwardDeclarations, COLLATOR);
		Collections.sort(functionForwardDeclarations, COLLATOR);
	}

	private void createCommentOut(IASTPreprocessorIncludeStatement include, List<TextEdit> edits) {
		IASTFileLocation location = include.getFileLocation();
		int offset = location.getNodeOffset();
		if (fContext.getTranslationUnit().isCXXLanguage()) {
			offset = getLineStart(offset);
			edits.add(new InsertEdit(offset, "//")); //$NON-NLS-1$
		} else {
			edits.add(new InsertEdit(offset, "/*")); //$NON-NLS-1$
			int endOffset = offset + location.getNodeLength();
			edits.add(new InsertEdit(endOffset, "*/")); //$NON-NLS-1$
		}
	}

	private void createDelete(IASTPreprocessorIncludeStatement include, List<TextEdit> edits) {
		IASTFileLocation location = include.getFileLocation();
		int offset = location.getNodeOffset();
		int endOffset = offset + location.getNodeLength();
		offset = getLineStart(offset);
		endOffset = skipToNextLine(endOffset);
		edits.add(new DeleteEdit(offset, endOffset - offset));
	}

	private void updateIncludePrototypes(Map<IncludePrototype, IncludePrototype> includePrototypes,
			IncludePrototype prototype) {
		IncludePrototype existing = includePrototypes.get(prototype);
		if (existing == null) {
			includePrototypes.put(prototype, prototype);
		} else {
			existing.updateFrom(prototype);
		}
	}

	private boolean isContainedInRegion(IASTNode node, IRegion region) {
		return getNodeOffset(node) >= region.getOffset()
				&& getNodeEndOffset(node) <= region.getOffset() + region.getLength();
	}

	private IRegion getSafeIncludeReplacementRegion(IASTTranslationUnit ast, NodeCommentMap commentMap) {
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
				includeOffset = skipToNextLine(includeGuardEndOffset);
			} else {
				includeOffset = 0;
			}
			if (!topCommentSkipped) {
				// Skip the first comment block near the top of the file. 
				includeOffset = skipStandaloneCommentBlock(includeOffset, maxSafeOffset, ast.getComments(), commentMap);
			}
			includeEndOffset = includeOffset;
		} else {
			includeEndOffset = skipToNextLine(includeEndOffset);
		}
		return new Region(includeOffset, includeEndOffset - includeOffset);
	}

	private int getNumberOfIncludeGuardStatementsToSkip(IASTTranslationUnit ast) {
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
			Arrays.copyOfRange(contents, offset, contents.length);
		CharArrayIntMap ppKeywords= new CharArrayIntMap(40, -1);
		Keywords.addKeywordsPreprocessor(ppKeywords);
		if (IncludeGuardDetection.detectIncludeGuard(new CharArray(contents), new LexerOptions(), ppKeywords) != null) {
			num += 2;
		}
		return num;
	}

	private IASTPreprocessorStatement findFirstPreprocessorStatement(IASTTranslationUnit ast) {
		for (IASTPreprocessorStatement statement : ast.getAllPreprocessorStatements()) {
			if (statement.isPartOfTranslationUnitFile())
				return statement;
		}
		return null;
	}
	private boolean isPragmaOnce(IASTPreprocessorStatement statement) {
		if (!(statement instanceof IASTPreprocessorPragmaStatement))
			return false;
		return CharArrayUtils.equals(((IASTPreprocessorPragmaStatement) statement).getMessage(), "once"); //$NON-NLS-1$
	}

	private int skipToNextLine(int offset) {
		char[] contents = fContext.getTranslationUnit().getContents();
		while (offset < contents.length) {
			if (contents[offset++] == '\n')
				break;
		}
		return offset;
	}

	private int getLineStart(int offset) {
		char[] contents = fContext.getTranslationUnit().getContents();
		while (--offset >= 0) {
			if (contents[offset] == '\n')
				break;
		}
		return offset + 1;
	}

	private int skipToNextLineAfterNode(IASTNode node) {
		return skipToNextLine(getNodeEndOffset(node));
	}

	/**
	 * Returns {@code true} if there are no non-whitespace characters between the given
	 * {@code offset} and the end of the line.
	 */
	private boolean isBlankLineOrEndOfFile(int offset) {
		char[] contents = fContext.getTranslationUnit().getContents();
		while (offset < contents.length) {
			char c = contents[offset++];
			if (c == '\n')
				return true;
			if (!Character.isWhitespace(c))
				return false;
		}
		return true;
	}

	/**
	 * Returns {@code true} the line prior to the line corresponding to the given {@code offset} 
	 * does not contain non-whitespace characters.
	 */
	private boolean isPreviousLineBlank(int offset) {
		char[] contents = fContext.getTranslationUnit().getContents();
		while (--offset >= 0) {
			if (contents[offset] == '\n')
				break;
		}
		while (--offset >= 0) {
			char c = contents[offset];
			if (c == '\n')
				return true;
			if (!Character.isWhitespace(c))
				return false;
		}
		return false;
	}

	/**
	 * Returns the whitespace preceding the given node. The newline character in not considered
	 * whitespace for the purpose of this method.
	 */
	private String getPrecedingWhitespace(IASTNode node) {
		int offset = getNodeOffset(node);
		if (offset >= 0) {
			char[] contents = fContext.getTranslationUnit().getContents();
			int i = offset;
			while (--i >= 0) {
				char c = contents[i];
				if (c == '\n' || !Character.isWhitespace(c))
					break;
			}
			i++;
			return new String(contents, i, offset - i);
		}
		return ""; //$NON-NLS-1$
	}

	private int skipStandaloneCommentBlock(int offset, int endOffset, IASTComment[] comments, NodeCommentMap commentMap) {
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
							return skipToNextLineAfterNode(previous);
						previous = comment;
					}
					if (getStartingLineNumber(node) > getEndingLineNumber(previous) + 1)
						return skipToNextLineAfterNode(previous);
				}
				node = inverseFreestandingMap.get(comment);
				if (node != null) {
					List<IASTComment> freestandingComments = commentMap.getFreestandingMap().get(node);
					IASTComment previous = freestandingComments.get(0);
					for (int j = 1; j < freestandingComments.size(); j++) {
						comment = freestandingComments.get(j);
						if (getStartingLineNumber(comment) > getEndingLineNumber(previous) + 1)
							return skipToNextLineAfterNode(previous);
						previous = comment;
					}
				}
			}
		}
		return offset;
	}

	private IncludeGroupStyle getGroupingStyle(IncludeGroupStyle style) {
		if (style.isKeepTogether())
			return style;
		IncludeGroupStyle parent = getParentStyle(style);
		if (parent != null && (parent.isKeepTogether() || parent.getIncludeKind() == IncludeKind.OTHER))
			return parent;
		return fContext.getPreferences().includeStyles.get(IncludeKind.OTHER);
	}

	private IncludeGroupStyle getParentStyle(IncludeGroupStyle style) {
		IncludeKind kind = style.getIncludeKind().parent;
		if (kind == null)
			return null;
		return fContext.getPreferences().includeStyles.get(kind);
	}

	private IncludeGroupStyle getIncludeStyle(IPath headerPath) {
		IncludeKind includeKind;
		IncludeInfo includeInfo = fContext.getIncludeForHeaderFile(headerPath);
		if (includeInfo != null && includeInfo.isSystem()) {
			if (headerPath.getFileExtension() == null) {
				includeKind = IncludeKind.SYSTEM_WITHOUT_EXTENSION;
			} else {
				includeKind = IncludeKind.SYSTEM_WITH_EXTENSION;
			}
		} else if (isPartnerFile(headerPath)) {
			includeKind = IncludeKind.PARTNER;
		} else {
			IPath dir = fContext.getCurrentDirectory();
			if (dir.isPrefixOf(headerPath)) {
				if (headerPath.segmentCount() == dir.segmentCount() + 1) {
					includeKind = IncludeKind.IN_SAME_FOLDER;
				} else {
					includeKind = IncludeKind.IN_SUBFOLDER;
				}
			} else {
				IFile[] files = ResourceLookup.findFilesForLocation(headerPath);
				if (files.length == 0) {
					includeKind = IncludeKind.EXTERNAL;
				} else {
					IProject project = fContext.getProject();
					includeKind = IncludeKind.IN_OTHER_PROJECT;
					for (IFile file : files) {
						if (file.getProject().equals(project)) {
							includeKind = IncludeKind.IN_SAME_PROJECT;
							break;
						}
					}
				}
			}
		}
		return fContext.getPreferences().includeStyles.get(includeKind);
	}

	private IncludeGroupStyle getIncludeStyle(IncludeInfo includeInfo) {
		IncludeKind includeKind;
		IPath path = Path.fromPortableString(includeInfo.getName());
		if (includeInfo.isSystem()) {
			if (path.getFileExtension() == null) {
				includeKind = IncludeKind.SYSTEM_WITHOUT_EXTENSION;
			} else {
				includeKind = IncludeKind.SYSTEM_WITH_EXTENSION;
			}
		} else if (isPartnerFile(path)) {
			includeKind = IncludeKind.PARTNER;
		} else {
			includeKind = IncludeKind.EXTERNAL;
		}
		return fContext.getPreferences().includeStyles.get(includeKind);
	}

	private Set<IBinding> removeBindingsDefinedInIncludedHeaders(IASTTranslationUnit ast,
			Set<IBinding> bindings, IIndexFileSet reachableHeaders) throws CoreException {
		Set<IBinding> filteredBindings = new HashSet<IBinding>(bindings);

		List<InclusionRequest> requests = createInclusionRequests(ast, bindings, reachableHeaders);
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
			IIndexInclude[] includedBy = fContext.getIndex().findIncludedBy(file, IIndex.DEPTH_INFINITE);
			for (IIndexInclude include : includedBy) {
				IPath path = getPath(include.getIncludedByLocation());
				if (includedHeaders.contains(path)) {
					return true;
				}
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
				if (isPartnerFile(path)) {
					request.resolve(path);
					fContext.addHeaderToInclude(path);
					if (includedByPartner != null) {
						try {
							IIndexFile indexFile = request.getDeclaringFiles().keySet().iterator().next();
							if (!includedByPartner.contains(indexFile)) {
								for (IIndexInclude include : indexFile.getIncludes()) {
									fContext.addHeaderAlreadyIncluded(getPath(include.getIncludesLocation()));
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
			if (!request.isResolved()) {
				List<IPath> candidatePaths = request.getCandidatePaths();
				Set<IPath> representativeHeaders = new HashSet<IPath>();
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
				}
			}
		}

		// Process remaining unambiguous inclusion requests.
		for (InclusionRequest request : requests) {
			if (!request.isResolved()) {
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
					}
				}
			}
		}

		// Resolve ambiguous inclusion requests.
		for (InclusionRequest request : requests) {
			if (!request.isResolved()) {
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

		// Remove headers that are exported by other headers.
		fContext.removeExportedHeaders();
	}

	private static IPath getPath(IIndexFileLocation location) {
		return IndexLocationFactory.getAbsolutePath(location);
	}

	/**
	 * Checks if the given path points to a partner header of the current translation unit.
	 * A header is considered a partner if its name without extension is the same as the name of
	 * the translation unit, or the name of the translation unit differs by one of the suffixes
	 * used for test files.
	 */
	private boolean isPartnerFile(IPath path) {
		String headerName = path.removeFileExtension().lastSegment();
		String sourceName = fContext.getTranslationUnit().getLocation().removeFileExtension().lastSegment();
		if (headerName.equals(sourceName))
			return true;
		if (sourceName.startsWith(headerName)) {
			int pos = headerName.length();
			while (pos < sourceName.length() && !Character.isLetterOrDigit(sourceName.charAt(pos))) {
				pos++;
			}
			if (pos == sourceName.length())
				return true;
			String suffix = sourceName.substring(pos);
			for (String s : fContext.getPreferences().partnerFileSuffixes) {
				if (suffix.equalsIgnoreCase(s))
					return true;
			}
		}
		return false;
	}

	private List<InclusionRequest> createInclusionRequests(IASTTranslationUnit ast,
			Set<IBinding> bindingsToDefine, IIndexFileSet reachableHeaders) throws CoreException {
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
	    				indexNames = Arrays.copyOf(indexNames, indexNames.length + 1);
	    				indexNames[indexNames.length - 1] = indexName;
	    			}
	    		}
			} else if (binding instanceof IFunction) {
				// For functions we need to include the declaration.
				indexNames = index.findDeclarations(binding);
			} else {
				// For all other bindings we need to include the definition.
				indexNames = index.findDefinitions(binding);
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
					IPath path = getPath(indexFile.getLocation());
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

	private IncludeInfo createIncludeInfo(IPath header, IncludeGroupStyle style) {
		String name = null;
		if (style.isRelativePath()) {
			name = getRelativePath(header);
		}
		if (name == null) {
			IncludeInfo includeInfo = fContext.getIncludeForHeaderFile(header);
			if (includeInfo != null) {
				name = includeInfo.getName();
			} else {
				name = getRelativePath(header);
			}
			if (name == null) {
				name = header.toPortableString();  // Last resort. 
			}
		}
		return new IncludeInfo(name, style.isAngleBrackets());
	}

	private String getRelativePath(IPath header) {
		IPath relativePath = PathUtil.makeRelativePath(header, fContext.getCurrentDirectory());
		if (relativePath == null)
			return null;
		return relativePath.toPortableString();
	}

	private String createIncludeDirective(IncludePrototype include, String lineComment) {
		StringBuilder buf = new StringBuilder();
		// Unresolved includes are preserved out of caution.
		if (!include.required && include.header != null) {
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
		buf.append("#include "); //$NON-NLS-1$
		buf.append(include.includeInfo.toString());
		buf.append(lineComment);
		return buf.toString();
	}
}
