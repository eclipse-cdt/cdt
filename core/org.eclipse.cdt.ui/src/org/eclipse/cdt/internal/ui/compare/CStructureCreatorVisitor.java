/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.compare;

import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;
import org.eclipse.cdt.internal.core.model.CoreModelMessages;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.compare.structuremergeviewer.DocumentRangeNode;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

/**
 * AST visitor to create compare structure.
 *
 * @since 5.0
 */
class CStructureCreatorVisitor extends ASTVisitor {

	private static final String TRANSLATION_UNIT_NAME = CUIMessages.CStructureCreatorVisitor_translationUnitName;
	private static final String ANONYMOUS_NAME = CoreModelMessages.getString("CElementLabels.anonymous"); //$NON-NLS-1$

	private Stack<DocumentRangeNode> fStack = new Stack<>();
	private IDocument fDocument;
	private String fTranslationUnitFileName;

	/**
	 * Create visitor adding nodes to given root.
	 *
	 * @param root
	 */
	public CStructureCreatorVisitor(DocumentRangeNode root) {
		fDocument = root.getDocument();
		fStack.clear();
		fStack.push(root);
		// visitor options
		shouldVisitTranslationUnit = true;
		shouldVisitDeclarations = true;
		shouldVisitEnumerators = true;
		shouldVisitNamespaces = true;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTTranslationUnit)
	 */
	@Override
	public int visit(IASTTranslationUnit tu) {
		fTranslationUnitFileName = tu.getFilePath();

		push(ICElement.C_UNIT, TRANSLATION_UNIT_NAME, 0);

		// TODO fix ordering of includes and macros
		// includes
		final IASTPreprocessorIncludeStatement[] includeDirectives = tu.getIncludeDirectives();
		for (int i = 0; i < includeDirectives.length; i++) {
			IASTPreprocessorIncludeStatement includeDirective = includeDirectives[i];
			if (isLocalToFile(includeDirective)) {
				push(ICElement.C_INCLUDE, new String(includeDirective.getName().toCharArray()),
						getStartOffset(includeDirective));
				pop(getEndOffset(includeDirective));
			}
		}
		// macros
		final IASTPreprocessorMacroDefinition[] macroDefinitions = tu.getMacroDefinitions();
		for (int i = 0; i < macroDefinitions.length; i++) {
			IASTPreprocessorMacroDefinition macroDefinition = macroDefinitions[i];
			if (isLocalToFile(macroDefinition)) {
				push(ICElement.C_MACRO, new String(macroDefinition.getName().toCharArray()),
						getStartOffset(macroDefinition));
				pop(getEndOffset(macroDefinition));
			}
		}

		return super.visit(tu);
	}

	/**
	 * Test whether given AST node is local to the source file
	 * and not part of an inclusion.
	 *
	 * @param node
	 * @return <code>true</code> if the node is part of the source file.
	 */
	private boolean isLocalToFile(IASTNode node) {
		return fTranslationUnitFileName.equals(node.getContainingFilename());
	}

	/**
	 * Compute the start offset of given AST node.
	 *
	 * @param node
	 * @return
	 */
	private int getStartOffset(IASTNode node) {
		IASTFileLocation fileLocation = getMinFileLocation(node.getNodeLocations());
		if (fileLocation != null) {
			return fileLocation.getNodeOffset();
		}
		DocumentRangeNode container = getCurrentContainer();
		Object[] children = container.getChildren();
		if (children != null && children.length > 0) {
			Position prevRange = ((DocumentRangeNode) children[children.length - 1]).getRange();
			return prevRange.getOffset() + prevRange.getLength();
		}
		// fallback: use container range start
		Position containerRange = container.getRange();
		return containerRange.getOffset();
	}

	/**
	 * Compute the end offset of give AST node.
	 *
	 * @param node
	 * @return
	 */
	private int getEndOffset(IASTNode node) {
		IASTFileLocation fileLocation = getMaxFileLocation(node.getNodeLocations());
		if (fileLocation != null) {
			return fileLocation.getNodeOffset() + fileLocation.getNodeLength();
		}
		// fallback: use container range end
		DocumentRangeNode container = getCurrentContainer();
		Position containerRange = container.getRange();
		return containerRange.getOffset() + containerRange.getLength();
	}

	/*
		 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#leave(org.eclipse.cdt.core.dom.ast.IASTTranslationUnit)
		 */
	@Override
	public int leave(IASTTranslationUnit tu) {
		super.leave(tu);
		assert getCurrentContainer().getTypeCode() == ICElement.C_UNIT;
		pop(fDocument.getLength());
		return PROCESS_SKIP;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
	 */
	@Override
	public int visit(IASTDeclaration node) {
		boolean isTemplateDecl = isTemplateDecl(node);
		final int startOffset = isTemplateDecl ? getStartOffset(node.getParent()) : getStartOffset(node);
		final int endOffset = getEndOffset(node);
		if (node instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition functionDef = (IASTFunctionDefinition) node;
			final int nodeType;
			if (inClassBody()) {
				nodeType = isTemplateDecl ? ICElement.C_TEMPLATE_METHOD : ICElement.C_METHOD;
			} else {
				nodeType = isTemplateDecl ? ICElement.C_TEMPLATE_FUNCTION : ICElement.C_FUNCTION;
			}
			push(nodeType, getDeclaratorName(functionDef.getDeclarator()), startOffset);
			pop(endOffset);
			return PROCESS_SKIP;
		} else if (node instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) node;
			IASTDeclSpecifier declSpec = simpleDecl.getDeclSpecifier();
			if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
				ICPPASTCompositeTypeSpecifier compositeTypeSpec = (ICPPASTCompositeTypeSpecifier) declSpec;
				final String nodeName = getTypeName(compositeTypeSpec);
				final int nodeType;
				switch (compositeTypeSpec.getKey()) {
				case IASTCompositeTypeSpecifier.k_struct:
					nodeType = isTemplateDecl ? ICElement.C_TEMPLATE_STRUCT : ICElement.C_STRUCT;
					break;
				case IASTCompositeTypeSpecifier.k_union:
					nodeType = isTemplateDecl ? ICElement.C_TEMPLATE_UNION : ICElement.C_UNION;
					break;
				case ICPPASTCompositeTypeSpecifier.k_class:
					nodeType = isTemplateDecl ? ICElement.C_TEMPLATE_CLASS : ICElement.C_CLASS;
					break;
				default:
					assert false : "Unexpected composite type specifier"; //$NON-NLS-1$
					return PROCESS_CONTINUE;
				}
				push(nodeType, nodeName, startOffset);
			} else if (declSpec instanceof IASTEnumerationSpecifier) {
				IASTEnumerationSpecifier enumSpecifier = (IASTEnumerationSpecifier) declSpec;
				push(ICElement.C_ENUMERATION, getEnumerationName(enumSpecifier), startOffset);
			} else {
				IASTDeclarator[] declarators = simpleDecl.getDeclarators();
				for (int i = 0; i < declarators.length; i++) {
					IASTDeclarator declarator = declarators[i];
					int declStartOffset = declarators.length == 1 ? startOffset : getStartOffset(declarator);
					int declEndOffset = declarators.length == 1 ? endOffset : getEndOffset(declarator);
					final String nodeName = getDeclaratorName(declarator);
					if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_typedef) {
						push(ICElement.C_TYPEDEF, nodeName, declStartOffset);
						pop(declEndOffset);
					} else if (declarator instanceof IASTFunctionDeclarator && !hasNestedPointerOperators(declarator)) {
						final int nodeType;
						if (inClassBody()) {
							nodeType = isTemplateDecl ? ICElement.C_TEMPLATE_METHOD_DECLARATION
									: ICElement.C_METHOD_DECLARATION;
						} else {
							nodeType = isTemplateDecl ? ICElement.C_TEMPLATE_FUNCTION_DECLARATION
									: ICElement.C_FUNCTION_DECLARATION;
						}
						push(nodeType, nodeName, declStartOffset);
						pop(declEndOffset);
					} else if (declarator != null) {
						final int nodeType;
						if (inClassBody()) {
							nodeType = ICElement.C_FIELD;
						} else {
							if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_extern) {
								nodeType = ICElement.C_VARIABLE_DECLARATION;
							} else {
								nodeType = isTemplateDecl ? ICElement.C_TEMPLATE_VARIABLE : ICElement.C_VARIABLE;
							}
						}
						push(nodeType, nodeName, declStartOffset);
						pop(declEndOffset);
					}
				}
			}
		} else if (node instanceof IASTASMDeclaration) {
			// ignored
		} else if (node instanceof ICPPASTVisibilityLabel) {
			// ignored
		} else if (node instanceof ICPPASTNamespaceDefinition) {
			// handled below
		} else if (node instanceof ICPPASTNamespaceAlias) {
			// ignored
		} else if (node instanceof ICPPASTUsingDeclaration) {
			ICPPASTUsingDeclaration usingDecl = (ICPPASTUsingDeclaration) node;
			push(ICElement.C_USING, ASTStringUtil.getQualifiedName(usingDecl.getName()), startOffset);
			pop(endOffset);
		} else if (node instanceof ICPPASTUsingDirective) {
			ICPPASTUsingDirective usingDirective = (ICPPASTUsingDirective) node;
			push(ICElement.C_USING, ASTStringUtil.getQualifiedName(usingDirective.getQualifiedName()), startOffset);
			pop(endOffset);
		} else if (node instanceof ICPPASTLinkageSpecification) {
			// declarations get flattened
		} else if (node instanceof ICPPASTTemplateDeclaration) {
			// handled at child declaration level
		} else if (node instanceof ICPPASTTemplateSpecialization) {
			// ignored
		} else if (node instanceof ICPPASTExplicitTemplateInstantiation) {
			// ignored
		} else if (node instanceof IASTProblemDeclaration) {
			// ignored
		}
		return super.visit(node);
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor#visit(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition)
	 */
	@Override
	public int visit(ICPPASTNamespaceDefinition namespace) {
		push(ICElement.C_NAMESPACE, ASTStringUtil.getQualifiedName(namespace.getName()), getStartOffset(namespace));
		return super.visit(namespace);
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator)
	 */
	@Override
	public int visit(IASTEnumerator enumerator) {
		push(ICElement.C_ENUMERATOR, ASTStringUtil.getQualifiedName(enumerator.getName()), getStartOffset(enumerator));
		pop(getEndOffset(enumerator));
		return super.visit(enumerator);
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#leave(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
	 */
	@Override
	public int leave(IASTDeclaration node) {
		super.leave(node);
		boolean isTemplateDecl = isTemplateDecl(node);
		final int endOffset = isTemplateDecl ? getEndOffset(node.getParent()) : getEndOffset(node);
		if (node instanceof IASTFunctionDefinition) {
			final int nodeType;
			if (inClassBody()) {
				nodeType = isTemplateDecl ? ICElement.C_TEMPLATE_METHOD : ICElement.C_METHOD;
			} else {
				nodeType = isTemplateDecl ? ICElement.C_TEMPLATE_FUNCTION : ICElement.C_FUNCTION;
			}
			assert getCurrentContainer().getTypeCode() == nodeType;
			pop(endOffset);
		} else if (node instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) node;
			IASTDeclSpecifier declSpec = simpleDecl.getDeclSpecifier();
			boolean isCompositeType = false;
			if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
				isCompositeType = true;
				ICPPASTCompositeTypeSpecifier compositeTypeSpec = (ICPPASTCompositeTypeSpecifier) declSpec;
				final int nodeType;
				switch (compositeTypeSpec.getKey()) {
				case IASTCompositeTypeSpecifier.k_struct:
					nodeType = isTemplateDecl ? ICElement.C_TEMPLATE_STRUCT : ICElement.C_STRUCT;
					break;
				case IASTCompositeTypeSpecifier.k_union:
					nodeType = isTemplateDecl ? ICElement.C_TEMPLATE_UNION : ICElement.C_UNION;
					break;
				case ICPPASTCompositeTypeSpecifier.k_class:
					nodeType = isTemplateDecl ? ICElement.C_TEMPLATE_CLASS : ICElement.C_CLASS;
					break;
				default:
					assert false : "Unexpected composite type specifier"; //$NON-NLS-1$
					return PROCESS_CONTINUE;
				}
				assert getCurrentContainer().getTypeCode() == nodeType;
				pop(isTemplateDecl ? endOffset : getEndOffset(declSpec));
			} else if (declSpec instanceof IASTEnumerationSpecifier) {
				isCompositeType = true;
				assert getCurrentContainer().getTypeCode() == ICElement.C_ENUMERATION;
				pop(getEndOffset(declSpec));
			}
			if (isCompositeType) {
				IASTDeclarator[] declarators = simpleDecl.getDeclarators();
				for (int i = 0; i < declarators.length; i++) {
					IASTDeclarator declarator = declarators[i];
					final String nodeName = getDeclaratorName(declarator);
					final int declStartOffset = getStartOffset(declarator);
					final int declEndOffset = getEndOffset(declarator);
					if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_typedef) {
						push(ICElement.C_TYPEDEF, nodeName, declStartOffset);
						pop(declEndOffset);
					} else if (declarator instanceof IASTFunctionDeclarator && !hasNestedPointerOperators(declarator)) {
						final int nodeType;
						if (inClassBody()) {
							nodeType = isTemplateDecl ? ICElement.C_TEMPLATE_METHOD_DECLARATION
									: ICElement.C_METHOD_DECLARATION;
						} else {
							nodeType = isTemplateDecl ? ICElement.C_TEMPLATE_FUNCTION_DECLARATION
									: ICElement.C_FUNCTION_DECLARATION;
						}
						push(nodeType, nodeName, declStartOffset);
						pop(declEndOffset);
					} else if (declarator != null) {
						final int nodeType;
						if (inClassBody()) {
							nodeType = ICElement.C_FIELD;
						} else {
							if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_extern) {
								nodeType = ICElement.C_VARIABLE_DECLARATION;
							} else {
								nodeType = isTemplateDecl ? ICElement.C_TEMPLATE_VARIABLE : ICElement.C_VARIABLE;
							}
						}
						push(nodeType, nodeName, declStartOffset);
						pop(declEndOffset);
					}
				}
			}
		} else if (node instanceof IASTASMDeclaration) {
			// ignored
		} else if (node instanceof ICPPASTVisibilityLabel) {
			// ignored
		} else if (node instanceof ICPPASTNamespaceDefinition) {
			// handled below
		} else if (node instanceof ICPPASTNamespaceAlias) {
			// ignored
		} else if (node instanceof ICPPASTUsingDeclaration) {
			// handled in visit
		} else if (node instanceof ICPPASTUsingDirective) {
			// handled in visit
		} else if (node instanceof ICPPASTLinkageSpecification) {
			// declarations get flattened
		} else if (node instanceof ICPPASTTemplateDeclaration) {
			// handled at child declaration level
		} else if (node instanceof ICPPASTTemplateSpecialization) {
			// ignored
		} else if (node instanceof ICPPASTExplicitTemplateInstantiation) {
			// ignored
		} else if (node instanceof IASTProblemDeclaration) {
			// ignored
		}
		return PROCESS_CONTINUE;
	}

	/*
	 * @see org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor#leave(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition)
	 */
	@Override
	public int leave(ICPPASTNamespaceDefinition namespace) {
		assert getCurrentContainer().getTypeCode() == ICElement.C_NAMESPACE;
		pop(getEndOffset(namespace));
		return super.leave(namespace);
	}

	private DocumentRangeNode getCurrentContainer() {
		return fStack.peek();
	}

	/**
	 * Adds a new node with the given type and name to the current container.
	 */
	private void push(int type, String name, int declarationStart) {
		if (name.length() == 0) {
			name = ANONYMOUS_NAME;
		}
		fStack.push(new CNode(getCurrentContainer(), type, name, declarationStart, 0));
	}

	/**
	 * Closes the current node by setting its end position
	 * and pops it off the stack.
	 */
	private void pop(int declarationEnd) {
		DocumentRangeNode current = getCurrentContainer();
		current.setAppendPosition(declarationEnd);
		current.setLength(declarationEnd - current.getRange().getOffset());
		fStack.pop();
	}

	/**
	 * @return <code>true</code> if the current container is class-like.
	 */
	private boolean inClassBody() {
		int typeCode = getCurrentContainer().getTypeCode();
		return typeCode == ICElement.C_CLASS || typeCode == ICElement.C_TEMPLATE_CLASS || typeCode == ICElement.C_STRUCT
				|| typeCode == ICElement.C_TEMPLATE_STRUCT || typeCode == ICElement.C_UNION
				|| typeCode == ICElement.C_TEMPLATE_UNION;
	}

	/**
	 * Test whether the given declaration is a templated declaration.
	 *
	 * @param node
	 * @return <code>true</code> if the declaration is templated.
	 */
	private boolean isTemplateDecl(IASTDeclaration node) {
		return node.getParent() instanceof ICPPASTTemplateDeclaration;
	}

	private boolean hasNestedPointerOperators(IASTDeclarator declarator) {
		declarator = declarator.getNestedDeclarator();
		while (declarator != null) {
			if (declarator.getPointerOperators().length > 0) {
				return true;
			}
			declarator = declarator.getNestedDeclarator();
		}
		return false;
	}

	private String getEnumerationName(IASTEnumerationSpecifier enumSpecifier) {
		String nodeName = ASTStringUtil.getQualifiedName(enumSpecifier.getName());
		if (nodeName.length() == 0) {
			nodeName = ANONYMOUS_NAME;
		}
		return nodeName;
	}

	private String getTypeName(IASTCompositeTypeSpecifier compositeTypeSpec) {
		String nodeName = ASTStringUtil.getQualifiedName(compositeTypeSpec.getName());
		if (nodeName.length() == 0) {
			nodeName = ANONYMOUS_NAME;
		}
		return nodeName;
	}

	private String getDeclaratorName(IASTDeclarator node) {
		node = getInnermostDeclarator(node);
		IASTName name = node.getName();
		String nodeName = ASTStringUtil.getQualifiedName(name);
		if (nodeName.length() == 0) {
			nodeName = ANONYMOUS_NAME;
		}
		return nodeName;
	}

	private IASTDeclarator getInnermostDeclarator(IASTDeclarator node) {
		IASTDeclarator nested = node.getNestedDeclarator();
		while (nested != null) {
			node = nested;
			nested = node.getNestedDeclarator();
		}
		return node;
	}

	private static IASTFileLocation getMaxFileLocation(IASTNodeLocation[] locations) {
		if (locations == null || locations.length == 0) {
			return null;
		}
		final IASTNodeLocation nodeLocation = locations[locations.length - 1];
		return nodeLocation.asFileLocation();
	}

	private static IASTFileLocation getMinFileLocation(IASTNodeLocation[] locations) {
		if (locations == null || locations.length == 0) {
			return null;
		}
		final IASTNodeLocation nodeLocation = locations[0];
		return nodeLocation.asFileLocation();
	}
}
