/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;

public class NodeContainer {
	
	public final NameInformation NULL_NAME_INFORMATION = new NameInformation(
			new CPPASTName());

	private final ArrayList<IASTNode> vec;
	private final ArrayList<NameInformation> names;

	public class NameInformation {

		private IASTName name;
		private IASTName declaration;
		private final ArrayList<IASTName> references;
		private ArrayList<IASTName> referencesAfterCached;
		private int lastCachedReferencesHash;
		private boolean isReference;
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

		public ArrayList<IASTName> getReferencesAfterSelection() {
			if (referencesAfterCached == null
					|| lastCachedReferencesHash != references.hashCode()) {

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

		public boolean isUsedAfterReferences() {
			return getReferencesAfterSelection().size() > 0;
		}

		public IASTParameterDeclaration getParameterDeclaration(boolean isReference,
				INodeFactory nodeFactory) {
			IASTDeclarator sourceDeclarator = (IASTDeclarator) getDeclaration().getParent();

			IASTDeclSpecifier declSpec= null;
			IASTDeclarator declarator= null;
			
			if (sourceDeclarator.getParent() instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration decl = (IASTSimpleDeclaration) sourceDeclarator.getParent();
				declSpec= decl.getDeclSpecifier().copy();
			} else if (sourceDeclarator.getParent() instanceof IASTParameterDeclaration) {
				IASTParameterDeclaration decl = (IASTParameterDeclaration) sourceDeclarator.getParent();
				declSpec= decl.getDeclSpecifier().copy();
			}

			IASTName name= nodeFactory.newName(getDeclaration().toCharArray());
			if (sourceDeclarator instanceof IASTArrayDeclarator) {
				IASTArrayDeclarator arrDeclarator = (IASTArrayDeclarator) sourceDeclarator;
				IASTArrayDeclarator arrayDtor = nodeFactory.newArrayDeclarator(name);
				IASTArrayModifier[] arrayModifiers = arrDeclarator.getArrayModifiers();
				for (IASTArrayModifier arrayModifier : arrayModifiers) {
					arrayDtor.addArrayModifier(arrayModifier.copy());
				}
				declarator= arrayDtor;
			} else {
				declarator = nodeFactory.newDeclarator(name);
			}
			for (IASTPointerOperator pointerOp : sourceDeclarator.getPointerOperators()) {
				declarator.addPointerOperator(pointerOp.copy());
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
				IASTSimpleDeclaration decl = (IASTSimpleDeclaration) sourceDeclarator
						.getParent();
				declSpec = decl.getDeclSpecifier();
			} else if (sourceDeclarator.getParent() instanceof IASTParameterDeclaration) {
				IASTParameterDeclaration decl = (IASTParameterDeclaration) sourceDeclarator
						.getParent();
				declSpec = decl.getDeclSpecifier();
			}

			ASTWriter writer = new ASTWriter();
			return writer.write(declSpec);
		}

		public boolean isDeclarationInScope() {
			if(declaration.toCharArray().length > 0) {
				int declOffset = declaration.getFileLocation().getNodeOffset();
				return declOffset >= getStartOffset()
				&& declOffset <= getEndOffset();
			}
			return true;
		}

		@Override
		public String toString() {
			return Messages.NodeContainer_Name + name + ' '
					+ isDeclarationInScope();
		}

		public boolean isReference() {
			return isReference;
		}

		public void setReference(boolean isReference) {
			this.isReference = isReference;
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
		vec = new ArrayList<IASTNode>();
		names = new ArrayList<NameInformation>();
	}

	public int size() {
		return vec.size();
	}

	public void add(IASTNode node) {
		vec.add(node);
	}

	public void findAllNames() {
		for (IASTNode node : vec) {
			node.accept(new CPPASTVisitor() {
				{
					shouldVisitNames = true;
				}

				@Override
				public int visit(IASTName name) {
					IBinding bind = name.resolveBinding();

					if (bind instanceof ICPPBinding
							&& !(bind instanceof ICPPTemplateTypeParameter)) {
						ICPPBinding cppBind = (ICPPBinding) bind;
						try {
							if (!cppBind.isGloballyQualified()) {
								NameInformation nameInformation = new NameInformation(
										name);

								IASTName[] refs = name.getTranslationUnit()
										.getReferences(bind);
								for (IASTName ref : refs) {
									nameInformation.addReference(ref);
								}
								names.add(nameInformation);
							}
						} catch (DOMException e) {
							ILog logger = CUIPlugin.getDefault().getLog();
							IStatus status = new Status(IStatus.WARNING,
									CUIPlugin.PLUGIN_ID, IStatus.OK, e
											.getMessage(), e);
							logger.log(status);
						}
					}else if(bind instanceof IVariable) {
						NameInformation nameInformation = new NameInformation(
								name);

						IASTName[] refs = name.getTranslationUnit()
								.getReferences(bind);
						for (IASTName ref : refs) {
							nameInformation.addReference(ref);
						}
						names.add(nameInformation);
					}
					return super.visit(name);
				}
			});
		}

		for (NameInformation nameInf : names) {
			IASTName name = nameInf.getName();

			IASTTranslationUnit unit = name.getTranslationUnit();
			IASTName[] decls = unit.getDeclarationsInAST(name.resolveBinding());
			for (IASTName declaration : decls) {
				nameInf.setDeclaration(declaration);
			}
		}
	}

	/*
	 * Returns all local names in the selection which will be used after the
	 * selection expected the ones which are pointers
	 */
	public ArrayList<NameInformation> getAllAfterUsedNames() {
		ArrayList<IASTName> declarations = new ArrayList<IASTName>();
		ArrayList<NameInformation> usedAfter = new ArrayList<NameInformation>();

		if (names.size() <= 0) {
			findAllNames();
		}

		for (NameInformation nameInf : names) {
			if (!declarations.contains(nameInf.getDeclaration())) {

				declarations.add(nameInf.getDeclaration());
				if (nameInf.isUsedAfterReferences()) {
					usedAfter.add(nameInf);
					nameInf.setReference(true);
				}
			}
		}

		return usedAfter;
	}

	public ArrayList<NameInformation> getAllAfterUsedNamesChoosenByUser() {
		ArrayList<IASTName> declarations = new ArrayList<IASTName>();
		ArrayList<NameInformation> usedAfter = new ArrayList<NameInformation>();

		for (NameInformation nameInf : names) {
			if (!declarations.contains(nameInf.getDeclaration())) {

				declarations.add(nameInf.getDeclaration());
				if (nameInf.isUserSetIsReference()
						|| nameInf.isUserSetIsReturnValue()) {
					usedAfter.add(nameInf);
				}
			}
		}

		return usedAfter;
	}

	public ArrayList<NameInformation> getUsedNamesUnique() {
		ArrayList<IASTName> declarations = new ArrayList<IASTName>();
		ArrayList<NameInformation> usedAfter = new ArrayList<NameInformation>();

		if (names.size() <= 0) {
			findAllNames();
		}

		for (NameInformation nameInf : names) {
			if (!declarations.contains(nameInf.getDeclaration())) {

				declarations.add(nameInf.getDeclaration());
				usedAfter.add(nameInf);
			} else {
				for (NameInformation nameInformation : usedAfter) {
					if (nameInf.isWriteAccess()
							&& nameInf.getDeclaration() == nameInformation
									.getDeclaration()) {
						nameInformation.setWriteAccess(true);
					}
				}
			}
		}

		return usedAfter;
	}

	/*
	 * Returns all local names in the selection which will be used after the
	 * selection expected the ones which are pointers
	 * XXX Was soll dieser Kommentar aussagen? --Mirko
	 */
	public ArrayList<NameInformation> getAllDeclaredInScope() {
		ArrayList<IASTName> declarations = new ArrayList<IASTName>();
		ArrayList<NameInformation> usedAfter = new ArrayList<NameInformation>();

		for (NameInformation nameInf : names) {
			if (nameInf.isDeclarationInScope()
					&& !declarations.contains(nameInf.getDeclaration()) && nameInf.isUsedAfterReferences()) {

				declarations.add(nameInf.getDeclaration());
				usedAfter.add(nameInf);
				// is return value candidate, set returnvalue to true and
				// reference to false
				nameInf.setReturnValue(true);
				nameInf.setReference(false);
			}
		}

		return usedAfter;
	}

	public List<IASTNode> getNodesToWrite() {
		return vec;
	}

	public int getStartOffset() {
		return getOffset(false);
	}

	public int getStartOffsetIncludingComments() {
		return getOffset(true);
	}

	private int getOffset(boolean includeComments) {
		int start = Integer.MAX_VALUE;

		for (IASTNode node : vec) {
			int nodeStart = Integer.MAX_VALUE;

			IASTNodeLocation[] nodeLocations = node.getNodeLocations();
			if (nodeLocations.length != 1) {
				for (IASTNodeLocation location : nodeLocations) {
					int nodeOffset;
					if (location instanceof IASTMacroExpansionLocation) {
						IASTMacroExpansionLocation macroLoc = (IASTMacroExpansionLocation) location;
						nodeOffset = macroLoc.asFileLocation().getNodeOffset();
					}else {
						nodeOffset = node.getFileLocation().getNodeOffset();
					}
					if(nodeOffset <  nodeStart) {
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

		for (IASTNode node : vec) {
			int fileOffset = 0;
			int length = 0;
			
			IASTNodeLocation[] nodeLocations = node.getNodeLocations();
			for (IASTNodeLocation location : nodeLocations) {
				int nodeOffset, nodeLength;
				if (location instanceof IASTMacroExpansionLocation) {
					IASTMacroExpansionLocation macroLoc = (IASTMacroExpansionLocation) location;
					nodeOffset = macroLoc.asFileLocation().getNodeOffset();
					nodeLength = macroLoc.asFileLocation().getNodeLength();
				}else {
					nodeOffset = location.getNodeOffset();
					nodeLength = location.getNodeLength();
				}
				if(fileOffset < nodeOffset) {
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
		return vec.toString();
	}

	public ArrayList<NameInformation> getNames() {
		return names;
	}
	
}
