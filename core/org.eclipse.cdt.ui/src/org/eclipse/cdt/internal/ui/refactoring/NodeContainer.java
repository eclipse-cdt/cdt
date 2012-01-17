/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;

public class NodeContainer {
	public final NameInformation NULL_NAME_INFORMATION = new NameInformation(new CPPASTName());

	private final List<IASTNode> nodes;
	private List<NameInformation> names;
	private List<NameInformation> interfaceNames;

	public class NameInformation {
		private IASTName name;
		private IASTName declaration;
		private final List<IASTName> references;
		private List<IASTName> referencesAfterCached;
		private int lastCachedReferencesHash;
		private boolean isOutput;
		private boolean isReturnValue;
		private boolean isConst;
		private boolean isWriteAccess;

		private boolean userSetIsReference;
		private boolean userSetIsReturnValue;
		private String userSetName;
		private int userOrder;

		public int getUserOrder() {
			return userOrder;
		}

		public void setUserOrder(int userOrder) {
			this.userOrder = userOrder;
		}

		public NameInformation(IASTName name) {
			super();
			this.name = name;
			references = new ArrayList<IASTName>();
		}

		public IASTName getDeclaration() {
			return declaration;
		}

		public void setDeclaration(IASTName declaration) {
			this.declaration = declaration;
		}

		public IASTName getName() {
			return name;
		}

		public void setName(IASTName name) {
			this.name = name;
		}

		public void addReference(IASTName name) {
			references.add(name);
		}

		public List<IASTName> getReferencesAfterSelection() {
			if (referencesAfterCached == null || lastCachedReferencesHash != references.hashCode()) {
				lastCachedReferencesHash = references.hashCode();
				referencesAfterCached = new ArrayList<IASTName>();
				for (IASTName ref : references) {
					IASTFileLocation loc = ref.getFileLocation();
					if (loc.getNodeOffset() >= getEndOffset()) {
						referencesAfterCached.add(ref);
					}
				}
			}
			return referencesAfterCached;
		}

		public boolean isReferencedAfterSelection() {
			return !getReferencesAfterSelection().isEmpty();
		}

		public IASTParameterDeclaration getParameterDeclaration(boolean isReference,
				INodeFactory nodeFactory) {
			IASTDeclarator sourceDeclarator = (IASTDeclarator) getDeclaration().getParent();

			IASTDeclSpecifier declSpec= null;
			IASTDeclarator declarator= null;
			
			if (sourceDeclarator.getParent() instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration decl = (IASTSimpleDeclaration) sourceDeclarator.getParent();
				declSpec = decl.getDeclSpecifier().copy(CopyStyle.withLocations);
			} else if (sourceDeclarator.getParent() instanceof IASTParameterDeclaration) {
				IASTParameterDeclaration decl = (IASTParameterDeclaration) sourceDeclarator.getParent();
				declSpec = decl.getDeclSpecifier().copy(CopyStyle.withLocations);
			}

			IASTName name= nodeFactory.newName(getDeclaration().toCharArray());
			if (sourceDeclarator instanceof IASTArrayDeclarator) {
				IASTArrayDeclarator arrDeclarator = (IASTArrayDeclarator) sourceDeclarator;
				IASTArrayDeclarator arrayDtor = nodeFactory.newArrayDeclarator(name);
				IASTArrayModifier[] arrayModifiers = arrDeclarator.getArrayModifiers();
				for (IASTArrayModifier arrayModifier : arrayModifiers) {
					arrayDtor.addArrayModifier(arrayModifier.copy(CopyStyle.withLocations));
				}
				declarator= arrayDtor;
			} else {
				declarator = nodeFactory.newDeclarator(name);
			}
			for (IASTPointerOperator pointerOp : sourceDeclarator.getPointerOperators()) {
				declarator.addPointerOperator(pointerOp.copy(CopyStyle.withLocations));
			}

			if (isReference && !hasReferenceOperartor(declarator)) {
				if (nodeFactory instanceof ICPPNodeFactory) {
					declarator.addPointerOperator(((ICPPNodeFactory) nodeFactory).newReferenceOperator(false));
				} else {
					declarator.addPointerOperator(nodeFactory.newPointer());
				}
			}

			declarator.setNestedDeclarator(sourceDeclarator.getNestedDeclarator());

			return nodeFactory.newParameterDeclaration(declSpec, declarator);
		}

		public boolean hasReferenceOperartor(IASTDeclarator declarator) {
			for (IASTPointerOperator pOp : declarator.getPointerOperators()) {
				if (pOp instanceof ICPPASTReferenceOperator) {
					return true;
				}
			}
			return false;
		}

		public String getType() {
			IASTDeclSpecifier declSpec = null;

			IASTNode node = getDeclaration().getParent();
			if (node instanceof ICPPASTSimpleTypeTemplateParameter) {
				ICPPASTSimpleTypeTemplateParameter parameter = (ICPPASTSimpleTypeTemplateParameter) node;
				return parameter.getName().toString();
			}
			IASTDeclarator sourceDeclarator = (IASTDeclarator) node;
			if (sourceDeclarator.getParent() instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration decl = (IASTSimpleDeclaration) sourceDeclarator.getParent();
				declSpec = decl.getDeclSpecifier();
			} else if (sourceDeclarator.getParent() instanceof IASTParameterDeclaration) {
				IASTParameterDeclaration decl = (IASTParameterDeclaration) sourceDeclarator.getParent();
				declSpec = decl.getDeclSpecifier();
			}

			ASTWriter writer = new ASTWriter();
			return writer.write(declSpec);
		}

		public boolean isDeclaredInSelection() {
			if (declaration != null && declaration.toCharArray().length > 0) {
				int declOffset = declaration.getFileLocation().getNodeOffset();
				return declOffset >= getStartOffset() && declOffset <= getEndOffset();
			}
			return true;
		}

		@Override
		public String toString() {
			return name.toString() + (isDeclaredInSelection() ? " (declared inside)" : "");  //$NON-NLS-1$//$NON-NLS-2$
		}

		public boolean isOutput() {
			return isOutput;
		}

		public void setOutput(boolean isOutput) {
			this.isOutput = isOutput;
		}

		public boolean isReturnValue() {
			return isReturnValue;
		}

		public void setReturnValue(boolean isReturnValue) {
			this.isReturnValue = isReturnValue;
		}

		public boolean isUserSetIsReference() {
			return userSetIsReference;
		}

		public void setUserSetIsReference(boolean userSetIsReference) {
			this.userSetIsReference = userSetIsReference;
		}

		public boolean isUserSetIsReturnValue() {
			return userSetIsReturnValue;
		}

		public void setUserSetIsReturnValue(boolean userSetIsReturnValue) {
			this.userSetIsReturnValue = userSetIsReturnValue;
		}

		public String getUserSetName() {
			return userSetName;
		}

		public void setUserSetName(String userSetName) {
			this.userSetName = userSetName;
		}

		public boolean isConst() {
			return isConst;
		}

		public void setConst(boolean isConst) {
			this.isConst = isConst;
		}

		public boolean isWriteAccess() {
			return isWriteAccess;
		}

		public void setWriteAccess(boolean isWriteAceess) {
			this.isWriteAccess = isWriteAceess;
		}
	}

	public NodeContainer() {
		super();
		nodes = new ArrayList<IASTNode>();
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
		names = new ArrayList<NameInformation>();
		for (IASTNode node : nodes) {
			node.accept(new ASTVisitor() {
				{
					shouldVisitNames = true;
				}

				@Override
				public int visit(IASTName name) {
					IBinding bind = name.resolveBinding();

					if (bind instanceof ICPPBinding	&& !(bind instanceof ICPPTemplateTypeParameter)) {
						ICPPBinding cppBind = (ICPPBinding) bind;
						try {
							if (!cppBind.isGloballyQualified()) {
								NameInformation nameInformation = new NameInformation(name);
								IASTName[] refs = name.getTranslationUnit().getReferences(bind);
								for (IASTName ref : refs) {
									nameInformation.addReference(ref);
								}
								names.add(nameInformation);
							}
						} catch (DOMException e) {
							ILog logger = CUIPlugin.getDefault().getLog();
							IStatus status = new Status(IStatus.WARNING,
									CUIPlugin.PLUGIN_ID, IStatus.OK, e.getMessage(), e);
							logger.log(status);
						}
					} else if (bind instanceof IVariable) {
						NameInformation nameInformation = new NameInformation(name);

						IASTName[] refs = name.getTranslationUnit().getReferences(bind);
						for (IASTName ref : refs) {
							nameInformation.addReference(ref);
						}
						names.add(nameInformation);
					}
					return super.visit(name);
				}
			});
		}

		for (NameInformation nameInfo : names) {
			IASTName name = nameInfo.getName();

			IASTTranslationUnit unit = name.getTranslationUnit();
			IASTName[] nameDeclarations = unit.getDeclarationsInAST(name.resolveBinding());
			if (nameDeclarations.length != 0) {
				nameInfo.setDeclaration(nameDeclarations[nameDeclarations.length - 1]);
			}
		}
	}

	/**
	 * Returns names that are either parameter or return value candidates.
	 */
	private List<NameInformation> getInterfaceNames() {
		if (interfaceNames == null) {
			findAllNames();
	
			Set<IASTName> declarations = new HashSet<IASTName>();
			interfaceNames = new ArrayList<NameInformation>();
	
			for (NameInformation nameInfo : names) {
				if (declarations.add(nameInfo.getDeclaration())) {
					if (nameInfo.isDeclaredInSelection()) {
						if (nameInfo.isReferencedAfterSelection()) {
							nameInfo.setReturnValue(true);
							interfaceNames.add(nameInfo);
						}
					} else {
						for (NameInformation n2 : names) {
							if (n2.getDeclaration() == nameInfo.getDeclaration()) {
								int flag = CPPVariableReadWriteFlags.getReadWriteFlags(n2.getName());
								if ((flag & PDOMName.WRITE_ACCESS) != 0) {
									nameInfo.setWriteAccess(true);
									break;
								}
							}
						}
						if (nameInfo.isWriteAccess() && nameInfo.isReferencedAfterSelection()) {
							nameInfo.setOutput(true);
						}
						interfaceNames.add(nameInfo);
					}
				}
			}
		}

		return interfaceNames;
	}

	private List<NameInformation> getInterfaceNames(boolean isReturnValue) {
		List<NameInformation> selectedNames = null;

		for (NameInformation nameInfo : getInterfaceNames()) {
			if (nameInfo.isReturnValue() == isReturnValue) {
				if (selectedNames == null) {
					selectedNames = new ArrayList<NameInformation>();
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
					if (nodeOffset <  nodeStart) {
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
