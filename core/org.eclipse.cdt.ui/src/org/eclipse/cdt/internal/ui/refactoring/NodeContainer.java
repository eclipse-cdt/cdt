/*******************************************************************************
 * Copyright (c) 2008, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.dom.rewrite.util.ASTNodes;
import org.eclipse.cdt.internal.corext.refactoring.code.flow.FlowContext;
import org.eclipse.cdt.internal.corext.refactoring.code.flow.FlowInfo;
import org.eclipse.cdt.internal.corext.refactoring.code.flow.InputFlowAnalyzer;
import org.eclipse.cdt.internal.corext.refactoring.code.flow.Selection;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IPreferencesService;

public class NodeContainer {
	private final List<IASTNode> nodes;
	private List<NameInformation> names;
	private List<NameInformation> interfaceNames;

	public NodeContainer() {
		super();
		nodes = new ArrayList<>();
	}

	public final int size() {
		return nodes.size();
	}

	public final boolean isEmpty() {
		return nodes.isEmpty();
	}

	public void add(IASTNode node) {
		nodes.add(node);
	}

	private void findAllNames() {
		if (names != null) {
			return;
		}
		names = new ArrayList<>();
		final int startOffset = getStartOffset();
		final int endOffset = getEndOffset();

		IPreferencesService preferences = Platform.getPreferencesService();
		final boolean passOutputByPointer = preferences.getBoolean(CUIPlugin.PLUGIN_ID,
				PreferenceConstants.FUNCTION_PASS_OUTPUT_PARAMETERS_BY_POINTER, false,
				PreferenceConstants.getPreferenceScopes(getProject()));

		for (IASTNode node : nodes) {
			node.accept(new ASTVisitor() {
				{
					shouldVisitNames = true;
				}

				@Override
				public int visit(IASTName name) {
					if (name.getPropertyInParent() != IASTFieldReference.FIELD_NAME) {
						IBinding binding = name.resolveBinding();

						if (binding instanceof ICPPBinding && !(binding instanceof ICPPTemplateTypeParameter)) {
							ICPPBinding cppBinding = (ICPPBinding) binding;
							try {
								if (!cppBinding.isGloballyQualified()) {
									NameInformation nameInfo = new NameInformation(name);
									nameInfo.setPassOutputByPointer(passOutputByPointer);
									IASTName[] refs = name.getTranslationUnit().getReferences(binding);
									for (IASTName ref : refs) {
										nameInfo.addReference(ref, startOffset, endOffset);
									}
									names.add(nameInfo);
								}
							} catch (DOMException e) {
								IStatus status = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, e.getMessage(), e);
								CUIPlugin.log(status);
							}
						} else if (binding instanceof IVariable) {
							NameInformation nameInformation = new NameInformation(name);

							IASTName[] refs = name.getTranslationUnit().getReferences(binding);
							for (IASTName ref : refs) {
								nameInformation.addReference(ref, startOffset, endOffset);
							}
							names.add(nameInformation);
						}
					}
					return super.visit(name);
				}
			});
		}

		for (NameInformation nameInfo : names) {
			IASTName name = nameInfo.getName();

			IASTTranslationUnit ast = name.getTranslationUnit();
			IASTName[] nameDeclarations = ast.getDeclarationsInAST(name.resolveBinding());
			if (nameDeclarations.length != 0) {
				nameInfo.setDeclarationName(nameDeclarations[nameDeclarations.length - 1]);
			}
		}
	}

	private IProject getProject() {
		IProject project = null;
		if (nodes.isEmpty()) {
			ITranslationUnit tu = nodes.get(0).getTranslationUnit().getOriginatingTranslationUnit();
			if (tu != null)
				project = tu.getCProject().getProject();
		}
		return project;
	}

	/**
	 * Returns names that are either parameter or return value candidates.
	 */
	private List<NameInformation> getInterfaceNames() {
		if (interfaceNames == null) {
			findAllNames();

			Set<IVariable> externalReads = getVariablesReadOutside();

			Set<IASTName> declarations = new HashSet<>();
			interfaceNames = new ArrayList<>();

			for (NameInformation nameInfo : names) {
				IASTName declarationName = nameInfo.getDeclarationName();
				if (declarations.add(declarationName)) {
					if (isDeclaredInSelection(nameInfo)) {
						if (externalReads.contains(nameInfo.getName().resolveBinding())) {
							nameInfo.setMustBeReturnValue(true);
							interfaceNames.add(nameInfo);
						}
					} else {
						IASTDeclarator declarator = (IASTDeclarator) declarationName.getParent();
						if (!hasReferenceOperator(declarator)) {
							for (NameInformation n2 : names) {
								if (n2.getDeclarationName() == declarationName) {
									Optional<Integer> flags = CPPVariableReadWriteFlags.getReadWriteFlags(n2.getName());
									if (CPPVariableReadWriteFlags.mayBeWriteAccess(flags)) {
										nameInfo.setWriteAccess(true);
										break;
									}
								}
							}
							if (nameInfo.isWriteAccess()
									&& externalReads.contains(nameInfo.getName().resolveBinding())) {
								nameInfo.setOutput(true);
							}
						}
						interfaceNames.add(nameInfo);
					}
				}
			}
		}

		return interfaceNames;
	}

	private Set<IVariable> getVariablesReadOutside() {
		if (nodes.isEmpty())
			return Collections.emptySet();

		IASTNode firstNode = nodes.get(0);
		IASTFunctionDefinition enclosingFunction = ASTQueries.findAncestorWithType(firstNode,
				IASTFunctionDefinition.class);
		FlowContext flowContext = new FlowContext(enclosingFunction);
		flowContext.setConsiderAccessMode(true);
		flowContext.setComputeMode(FlowContext.ARGUMENTS);
		// Compute a selection that exactly covers the selected nodes
		Selection selection = Selection.createFromStartEnd(ASTNodes.offset(firstNode),
				ASTNodes.endOffset(nodes.get(nodes.size() - 1)));

		InputFlowAnalyzer analyzer = new InputFlowAnalyzer(flowContext, selection, true);
		FlowInfo argInfo = analyzer.perform(enclosingFunction);
		Set<IVariable> variables = argInfo.get(flowContext, FlowInfo.READ | FlowInfo.READ_POTENTIAL | FlowInfo.UNKNOWN);

		// Remove variables with scopes limited to the selection.
		for (Iterator<IVariable> iter = variables.iterator(); iter.hasNext();) {
			IVariable var = iter.next();
			try {
				IASTNode scopeNode = ASTInternal.getPhysicalNodeOfScope(var.getScope());
				if (selection.covers(scopeNode))
					iter.remove();
			} catch (DOMException e) {
			}
		}
		return variables;
	}

	public static boolean hasReferenceOperator(IASTDeclarator declarator) {
		IASTPointerOperator[] operators = declarator.getPointerOperators();
		return operators.length != 0 && operators[operators.length - 1] instanceof ICPPASTReferenceOperator;
	}

	public boolean isDeclaredInSelection(NameInformation nameInfo) {
		IASTName declaration = nameInfo.getDeclarationName();
		if (declaration != null && declaration.toCharArray().length > 0) {
			int declOffset = declaration.getFileLocation().getNodeOffset();
			return declOffset >= getStartOffset() && declOffset <= getEndOffset();
		}
		return true;
	}

	private List<NameInformation> getInterfaceNames(boolean isReturnValue) {
		List<NameInformation> selectedNames = null;

		for (NameInformation nameInfo : getInterfaceNames()) {
			if (nameInfo.mustBeReturnValue() == isReturnValue) {
				if (selectedNames == null) {
					selectedNames = new ArrayList<>();
				}
				selectedNames.add(nameInfo);
			}
		}
		if (selectedNames == null) {
			selectedNames = Collections.emptyList();
		}
		return selectedNames;
	}

	/**
	 * Returns names that are candidates to be used as function parameters.
	 */
	public List<NameInformation> getParameterCandidates() {
		return getInterfaceNames(false);
	}

	/**
	 * Returns names that are candidates for being used as the function return value. Multiple
	 * return value candidates mean that the function cannot be extracted.
	 */
	public List<NameInformation> getReturnValueCandidates() {
		return getInterfaceNames(true);
	}

	public List<IASTNode> getNodesToWrite() {
		return nodes;
	}

	public int getStartOffset() {
		return getOffset(false);
	}

	public int getStartOffsetIncludingComments() {
		return getOffset(true);
	}

	private int getOffset(boolean includeComments) {
		int start = Integer.MAX_VALUE;

		for (IASTNode node : nodes) {
			int nodeStart = Integer.MAX_VALUE;

			IASTNodeLocation[] nodeLocations = node.getNodeLocations();
			if (nodeLocations.length != 1) {
				for (IASTNodeLocation location : nodeLocations) {
					int nodeOffset;
					if (location instanceof IASTMacroExpansionLocation) {
						IASTMacroExpansionLocation macroLoc = (IASTMacroExpansionLocation) location;
						nodeOffset = macroLoc.asFileLocation().getNodeOffset();
					} else {
						nodeOffset = node.getFileLocation().getNodeOffset();
					}
					if (nodeOffset < nodeStart) {
						nodeStart = nodeOffset;
					}
				}
			} else {
				nodeStart = node.getFileLocation().getNodeOffset();
			}
			if (nodeStart < start) {
				start = nodeStart;
			}
		}

		return start;
	}

	public int getEndOffset() {
		return getEndOffset(false);
	}

	public int getEndOffsetIncludingComments() {
		return getEndOffset(true);
	}

	private int getEndOffset(boolean includeComments) {
		int end = 0;

		for (IASTNode node : nodes) {
			int fileOffset = 0;
			int length = 0;

			IASTNodeLocation[] nodeLocations = node.getNodeLocations();
			for (IASTNodeLocation location : nodeLocations) {
				int nodeOffset, nodeLength;
				if (location instanceof IASTMacroExpansionLocation) {
					IASTMacroExpansionLocation macroLoc = (IASTMacroExpansionLocation) location;
					nodeOffset = macroLoc.asFileLocation().getNodeOffset();
					nodeLength = macroLoc.asFileLocation().getNodeLength();
				} else {
					nodeOffset = location.getNodeOffset();
					nodeLength = location.getNodeLength();
				}
				if (fileOffset < nodeOffset) {
					fileOffset = nodeOffset;
					length = nodeLength;
				}
			}
			int endNode = fileOffset + length;
			if (endNode > end) {
				end = endNode;
			}
		}

		return end;
	}

	@Override
	public String toString() {
		return nodes.toString();
	}

	public List<NameInformation> getNames() {
		findAllNames();
		return names;
	}
}
