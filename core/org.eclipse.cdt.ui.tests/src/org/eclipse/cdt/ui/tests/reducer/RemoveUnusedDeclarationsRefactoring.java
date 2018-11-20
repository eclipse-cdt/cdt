/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.reducer;

import static org.eclipse.cdt.internal.core.dom.parser.ASTQueries.findInnermostDeclarator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationListOwner;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAliasDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPartialSpecialization;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterOptions;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.rewrite.util.ASTNodes;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.changes.CCompositeChange;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;
import org.eclipse.cdt.ui.refactoring.CTextFileChange;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

public class RemoveUnusedDeclarationsRefactoring extends CRefactoring {
	private static final String PROTECTION_TOKEN = "PRESERVE";
	private static final IASTName UNUSED_NAME = new CPPASTName(null);
	private static final ProblemFinder problemFinder = new ProblemFinder();

	private INodeFactory nodeFactory;
	private final DefaultCodeFormatterOptions formattingOptions;

	private IIndex index;
	private IASTTranslationUnit ast;
	private IRegion region;

	public RemoveUnusedDeclarationsRefactoring(ICElement element, ISelection selection, ICProject project) {
		super(element, selection, project);
		name = Messages.RemoveUnusedDeclarationsRefactoring_RemoveUnusedDeclarations;
		formattingOptions = new DefaultCodeFormatterOptions(project.getOptions(true));
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		SubMonitor progress = SubMonitor.convert(pm, 10);

		RefactoringStatus status = super.checkInitialConditions(progress.newChild(8));
		if (status.hasError()) {
			return status;
		}

		ast = getAST(tu, progress.newChild(1));
		index = getIndex();
		nodeFactory = ast.getASTNodeFactory();
		region = selectedRegion.getLength() == 0 ? new Region(0, ast.getFileLocation().getNodeLength())
				: selectedRegion;

		if (isProgressMonitorCanceled(progress, initStatus))
			return initStatus;

		return initStatus;
	}

	private ICPPASTFunctionDeclarator getDeclaration(IASTNode node) {
		while (node != null && !(node instanceof IASTFunctionDefinition)) {
			node = node.getParent();
		}
		if (node != null) {
			IASTFunctionDeclarator declarator = ((IASTFunctionDefinition) node).getDeclarator();
			if (declarator instanceof ICPPASTFunctionDeclarator) {
				return (ICPPASTFunctionDeclarator) declarator;
			}
		}
		return null;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext checkContext) {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		// This method bypasses the standard refactoring framework involving ModificationCollector and ASTRewrite since
		// it is too slow for the gigantic changes this refactoring has to deal with.
		NavigableSet<IASTName> names = NameCollector.getContainedNames(ast);

		String code = ast.getRawSignature();

		SortedNodeSet<IASTNode> nodesToDelete = new SortedNodeSet<>();
		IASTPreprocessorMacroExpansion[] macroExpansions = ast.getMacroExpansions();
		for (IASTPreprocessorMacroExpansion macroExpansion : macroExpansions) {
			IASTName name = macroExpansion.getMacroReference();
			if (SelectionHelper.isNodeInsideRegion(name, region) && !containsProtectionToken(name, code)
					&& macroExpansion.getMacroDefinition().getExpansion().isEmpty()) {
				nodesToDelete.add(macroExpansion);
			}
		}

		CandidateDeclarationFinder finder = new CandidateDeclarationFinder();
		ast.accept(finder);
		List<IASTDeclaration> declarations = finder.declarations;

		for (int i = declarations.size(); --i >= 0;) {
			IASTDeclaration declaration = declarations.get(i);
			if (SelectionHelper.isNodeInsideRegion(declaration, region) && !containsProtectionToken(declaration, code)
					&& !problemFinder.containsProblemBinding(declaration)
					&& !isPossiblyUsed(declaration, names, nodesToDelete)) {
				nodesToDelete.add(declaration);
				removeContainedNames(declaration, names);
			}
		}

		CTextFileChange fileChange = new CTextFileChange(tu.getElementName(), tu);
		fileChange.setEdit(new MultiTextEdit());

		int maxOffset = 0;
		TextEdit lastEdit = null;
		IASTNode lastNode = null;
		for (IASTNode node : nodesToDelete) {
			int offset = ASTNodes.offset(node);
			int endOffset = ASTNodes.endOffset(node);
			offset = skipWhitespaceBefore(offset, code);
			if (offset < region.getOffset())
				offset = region.getOffset();
			// Do not attempt to delete nodes inside a deleted region.
			if (endOffset > maxOffset) {
				DeleteEdit edit = new DeleteEdit(offset, endOffset - offset);
				fileChange.addEdit(edit);
				if (maxOffset < endOffset)
					maxOffset = endOffset;
				lastEdit = edit;
				lastNode = node;
			}
		}

		CCompositeChange change = new CCompositeChange(""); //$NON-NLS-1$
		change.markAsSynthetic();
		change.add(fileChange);
		change.setDescription(new RefactoringChangeDescriptor(getRefactoringDescriptor()));
		return change;
	}

	/**
	 * Checks if the node or the rest of its last line contain text matching {@link #PROTECTION_TOKEN}.
	 */
	private boolean containsProtectionToken(IASTNode node, String code) {
		int offset = ASTNodes.offset(node);
		int endOffset = ASTNodes.skipToNextLineAfterNode(code, node);
		for (int i = offset; i < endOffset - PROTECTION_TOKEN.length(); i++) {
			if (code.regionMatches(i, PROTECTION_TOKEN, 0, PROTECTION_TOKEN.length()))
				return true;
		}
		return false;
	}

	private boolean containsAncestor(Collection<IASTNode> nodes, IASTNode node) {
		while ((node = node.getParent()) != null) {
			if (nodes.contains(node))
				return true;
		}
		return false;
	}

	private IASTNode getTopMostContainer(IASTNode node) {
		while (node != null) {
			IASTNode prevNode = node;
			node = node.getParent();
			if (node instanceof IASTTranslationUnit)
				return prevNode;
		}
		return null;
	}

	private boolean isPossiblyUsed(IASTDeclaration declaration, NavigableSet<IASTName> names,
			SortedNodeSet<IASTNode> nodesToDelete) {
		if (declaration instanceof ICPPASTNamespaceDefinition) {
			// An empty namespace definition can be removed.
			IASTDeclaration[] children = ((ICPPASTNamespaceDefinition) declaration).getDeclarations(false);
			for (IASTDeclaration child : children) {
				if (!nodesToDelete.contains(child))
					return true;
			}
			return false;
		} else if (declaration instanceof ICPPASTLinkageSpecification) {
			// An empty linkage specification can be removed.
			IASTDeclaration[] children = ((ICPPASTLinkageSpecification) declaration).getDeclarations(false);
			for (IASTDeclaration child : children) {
				if (!nodesToDelete.contains(child))
					return true;
			}
			return false;
		} else if (declaration instanceof ICPPASTVisibilityLabel) {
			// A visibility label not followed by a member declaration can be removed.
			IASTNode parent = declaration.getParent();
			IASTDeclaration[] siblings = ((ICPPASTCompositeTypeSpecifier) parent).getDeclarations(false);
			boolean after = false;
			for (IASTDeclaration sibling : siblings) {
				if (after) {
					if (sibling instanceof ICPPASTVisibilityLabel)
						break;
					if (!nodesToDelete.contains(sibling))
						return true;
				} else if (sibling == declaration) {
					after = true;
				}
			}
			return false;
		}

		if (ASTQueries.findAncestorWithType(declaration, ICPPASTLambdaExpression.class) != null)
			return true; // Removing inside lambda expressions is unsafe.

		Collection<IASTName> declaredNames = getDeclaredNames(declaration);
		if (declaredNames == null)
			return true;

		for (IASTName declName : declaredNames) {
			char[] declNameChars = declName.getSimpleID();
			IASTNode startPoint = declName;
			int startOffset = ASTNodes.endOffset(declaration);
			if (declaration.getPropertyInParent() == IASTCompositeTypeSpecifier.MEMBER_DECLARATION
					&& !(declName.resolveBinding() instanceof ICPPConstructor)
					&& (declNameChars.length == 0 || declNameChars[0] != '~')) {
				// Member declarations can be referenced by other members declared before them.
				startPoint = declaration.getParent();
				startOffset = ASTNodes.offset(startPoint);
			} else {
				ASTNodeProperty property = declName.getPropertyInParent();
				if (property == IASTCompositeTypeSpecifier.TYPE_NAME
						|| property == ICPPASTEnumerationSpecifier.ENUMERATION_NAME
								&& ((ICPPASTEnumerationSpecifier) declName.getParent()).isScoped()) {
					// Start from the first forward declaration of class or scoped enumeration.
					while (declName instanceof ICPPASTTemplateId) {
						declName = ((ICPPASTTemplateId) declName).getTemplateName();
					}
					IBinding binding = declName.resolveBinding();
					if (binding instanceof IProblemBinding)
						return true;
					if (binding instanceof ICPPInternalBinding) {
						IASTNode[] declarations = ((ICPPInternalBinding) binding).getDeclarations();
						if (declarations != null && declarations.length != 0) {
							IASTNode firstDeclaration = declarations[0];
							int firstDeclarationOffset = ASTNodes.endOffset(firstDeclaration);
							if (startOffset > firstDeclarationOffset) {
								startPoint = firstDeclaration;
								startOffset = firstDeclarationOffset;
							}
						}
					}
				}
			}

			if (declNameChars.length != 0 && declNameChars[0] == '~')
				declNameChars = Arrays.copyOfRange(declNameChars, 1, declNameChars.length);

			int declOffset = ASTNodes.offset(declaration);
			int declEndOffset = ASTNodes.endOffset(declaration);

			for (IASTName name : names) {
				if (name != declName) {
					if (name.getPropertyInParent() == IASTDeclarator.DECLARATOR_NAME
							&& name.getParent().getPropertyInParent() == IASTParameterDeclaration.DECLARATOR) {
						continue; // Ignore parameter names.
					}

					char[] nameChars = name.getSimpleID();

					int offset = nameChars.length != 0 && nameChars[0] == '~' ? 1 : 0;
					if (CharArrayUtils.equals(nameChars, offset, nameChars.length - offset, declNameChars)) {
						int nameOffset = ASTNodes.offset(name);
						if (nameOffset >= declEndOffset || nameOffset >= startOffset && nameOffset < declOffset) {
							IASTDeclaration decl = findTopmostNonTemplateDeclaration(name);
							if (decl == null)
								return true;

							if (!isDeclaredBy(name, decl))
								return true;
						}
					}
				}
			}
		}
		return false;
	}

	private static Collection<IASTName> getDeclaredNames(IASTDeclaration declaration) {
		while (declaration instanceof ICPPASTTemplateDeclaration) {
			declaration = ((ICPPASTTemplateDeclaration) declaration).getDeclaration();
		}
		while (declaration instanceof ICPPASTTemplateSpecialization) {
			declaration = ((ICPPASTTemplateSpecialization) declaration).getDeclaration();
		}
		while (declaration instanceof ICPPASTExplicitTemplateInstantiation) {
			declaration = ((ICPPASTExplicitTemplateInstantiation) declaration).getDeclaration();
		}

		if (declaration instanceof IASTSimpleDeclaration) {
			List<IASTName> names = new ArrayList<>();
			IASTDeclarator[] declarators = ((IASTSimpleDeclaration) declaration).getDeclarators();
			for (IASTDeclarator declarator : declarators) {
				declarator = findInnermostDeclarator(declarator);
				IASTName name = declarator.getName();
				if (name instanceof ICPPASTConversionName || name instanceof ICPPASTOperatorName)
					return null; // Do not remove operators.
				addNameIfNotEmpty(name, names);
			}
			IASTDeclSpecifier declSpecifier = ((IASTSimpleDeclaration) declaration).getDeclSpecifier();
			if (declSpecifier instanceof IASTCompositeTypeSpecifier) {
				addNameIfNotEmpty(((IASTCompositeTypeSpecifier) declSpecifier).getName(), names);
			} else if (declSpecifier instanceof IASTElaboratedTypeSpecifier) {
				addNameIfNotEmpty(((IASTElaboratedTypeSpecifier) declSpecifier).getName(), names);
			} else if (declSpecifier instanceof IASTEnumerationSpecifier) {
				IASTEnumerationSpecifier enumSpecifier = (IASTEnumerationSpecifier) declSpecifier;
				IASTName name = enumSpecifier.getName();
				addNameIfNotEmpty(name, names);
				for (IASTEnumerator enumerator : enumSpecifier.getEnumerators()) {
					addNameIfNotEmpty(enumerator.getName(), names);
				}
			}
			return names;
		} else if (declaration instanceof IASTFunctionDefinition) {
			IASTDeclarator declarator = ((IASTFunctionDefinition) declaration).getDeclarator();
			declarator = findInnermostDeclarator(declarator);
			IASTName name = declarator.getName();
			if (name instanceof ICPPASTConversionName)
				return null; // Do not remove conversion operators.
			return Collections.singletonList(name);
		} else if (declaration instanceof ICPPASTUsingDirective) {
			return Collections.singletonList(((ICPPASTUsingDirective) declaration).getQualifiedName());
		} else if (declaration instanceof ICPPASTUsingDeclaration) {
			return Collections.singletonList(((ICPPASTUsingDeclaration) declaration).getName());
		} else if (declaration instanceof ICPPASTNamespaceAlias) {
			return Collections.singletonList(((ICPPASTNamespaceAlias) declaration).getAlias());
		} else if (declaration instanceof ICPPASTAliasDeclaration) {
			return Collections.singletonList(((ICPPASTAliasDeclaration) declaration).getAlias());
		}
		return null;
	}

	private static void addNameIfNotEmpty(IASTName name, List<IASTName> names) {
		if (name.getSimpleID().length != 0)
			names.add(name);
	}

	/**
	 * Returns the topmost non-template declaration declaring the given name, or {@code null}
	 * if the name appears in a non-declaration context.
	 */
	private static IASTDeclaration findTopmostNonTemplateDeclaration(IASTName name) {
		IASTNode node = name;
		ASTNodeProperty property = name.getPropertyInParent();
		while (property == ICPPASTTemplateId.TEMPLATE_NAME || property == ICPPASTQualifiedName.SEGMENT_NAME
				&& node == ((ICPPASTQualifiedName) node.getParent()).getLastName()) {
			node = node.getParent();
			property = node.getPropertyInParent();
		}
		if (property == IASTDeclarator.DECLARATOR_NAME || property == IASTCompositeTypeSpecifier.TYPE_NAME
				|| property == IASTElaboratedTypeSpecifier.TYPE_NAME) {
			node = node.getParent().getParent();
		}
		for (IASTNode parent; (parent = node.getParent()) != null; node = parent) {
			if (!(parent instanceof IASTDeclaration) || parent instanceof ICPPASTTemplateDeclaration
					|| parent instanceof ICPPASTTemplateSpecialization
					|| parent instanceof ICPPASTNamespaceDefinition) {
				if (node instanceof IASTDeclaration) {
					return (IASTDeclaration) node;
				}
				break;
			}
		}

		return null;
	}

	/**
	 * Checks if {@code name} is declared by the {@code declaration}.
	 */
	private static boolean isDeclaredBy(IASTName name, IASTDeclaration declaration) {
		if (declaration.getPropertyInParent() == ICPPASTTemplateSpecialization.OWNED_DECLARATION)
			return false;
		if (name.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_NAME
				&& ((ICPPASTTemplateId) name.getParent()).resolveBinding() instanceof ICPPPartialSpecialization)
			return false;
		if (declaration instanceof IASTSimpleDeclaration) {
			IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration) declaration).getDeclSpecifier();
			if (declSpec instanceof ICPPASTDeclSpecifier && ((ICPPASTDeclSpecifier) declSpec).isFriend())
				return false;
		}

		while (name instanceof ICPPASTTemplateId) {
			name = ((ICPPASTTemplateId) name).getTemplateName();
		}
		char[] nameChars = name.getSimpleID();
		Collection<IASTName> declaredNames = getDeclaredNames(declaration);
		if (declaredNames != null) {
			for (IASTName declaredName : declaredNames) {
				while (declaredName instanceof ICPPASTTemplateId) {
					declaredName = ((ICPPASTTemplateId) declaredName).getTemplateName();
				}
				char[] declaredNameChars = declaredName.getSimpleID();
				if (Arrays.equals(nameChars, declaredNameChars))
					return true;
			}
		}

		return false;
	}

	private static void removeContainedNames(IASTNode node, Set<IASTName> names) {
		NavigableSet<IASTName> containedNames = NameCollector.getContainedNames(node);
		names.removeAll(containedNames);
	}

	private static int skipWhitespaceBefore(int offset, String text) {
		while (--offset >= 0) {
			char c = text.charAt(offset);
			if (!Character.isWhitespace(c))
				break;
		}
		return offset + 1;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
			throws CoreException, OperationCanceledException {
		// This method is no-op for this refactoring. The change is created in the createChange method.
	}

	private class CandidateDeclarationFinder extends ASTVisitor {
		final List<IASTDeclaration> declarations = new ArrayList<>();

		CandidateDeclarationFinder() {
			shouldVisitDeclarations = true;
			shouldVisitNamespaces = true;
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			if (!declaration.isPartOfTranslationUnitFile())
				return PROCESS_SKIP;
			if (declaration.getParent() instanceof IASTDeclarationListOwner)
				declarations.add(declaration);
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
			if (!namespaceDefinition.isPartOfTranslationUnitFile())
				return PROCESS_SKIP;
			declarations.add(namespaceDefinition);
			return PROCESS_CONTINUE;
		}
	}

	/**
	 * Collects all simple names.
	 */
	private static class NameCollector extends ASTVisitor {
		NavigableSet<IASTName> names = new SortedNodeSet<>();

		static NavigableSet<IASTName> getContainedNames(IASTNode node) {
			NameCollector collector = new NameCollector();
			node.accept(collector);
			return collector.names;
		}

		NameCollector() {
			this.shouldVisitNames = true;
			this.shouldVisitImplicitNames = true;
		}

		@Override
		public int visit(IASTName name) {
			if (name instanceof ICPPASTQualifiedName || name instanceof ICPPASTTemplateId
					|| name instanceof ICPPASTConversionName) {
				return PROCESS_CONTINUE;
			}
			names.add(name);
			return PROCESS_CONTINUE;
		}
	}

	/**
	 * A set of AST nodes sorted by their offsets, or, if the offsets are equal, by the end offsets
	 * in the reverse order.
	 */
	private static class SortedNodeSet<T extends IASTNode> extends TreeSet<T> {
		private static final Comparator<IASTNode> COMPARATOR = new Comparator<IASTNode>() {
			@Override
			public int compare(IASTNode node1, IASTNode node2) {
				int c = Integer.compare(ASTNodes.offset(node1), ASTNodes.offset(node2));
				if (c != 0)
					return c;
				return -Integer.compare(ASTNodes.endOffset(node1), ASTNodes.endOffset(node2));
			}
		};

		public SortedNodeSet() {
			super(COMPARATOR);
		}
	}

	private static IASTDeclarationStatement getDeclarationStatement(IASTDeclaration declaration) {
		while (true) {
			IASTNode parent = declaration.getParent();
			if (parent instanceof IASTDeclarationStatement)
				return (IASTDeclarationStatement) parent;
			if (!(parent instanceof ICPPASTTemplateDeclaration))
				return null;
			declaration = (IASTDeclaration) parent;
		}
	}

	private static IASTName getAstName(IASTDeclarator decl) {
		IASTName astName = null;
		do {
			astName = decl.getName();
			if (astName != null && astName.getSimpleID().length != 0)
				return astName;

			// Resolve parenthesis if need to.
			decl = decl.getNestedDeclarator();
		} while (decl != null);

		return astName;
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		return null;
	}
}
