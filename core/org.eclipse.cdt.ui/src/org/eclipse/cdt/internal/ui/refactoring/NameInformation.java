/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
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
import java.util.List;

import org.eclipse.core.runtime.Assert;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.TypeHelper;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriterVisitor;

/**
 * Additional information about an IASTName in code being refactored. 
 */
public class NameInformation {
	public static final int INDEX_FOR_ADDED = -1;

	private final IASTName name;
	private IASTName declarationName;
	private final List<IASTName> references;
	private List<IASTName> referencesAfterCached;
	private int lastCachedReferencesHash;
	private boolean isOutput;
	private boolean mustBeReturnValue;
	private boolean isWriteAccess;
	private boolean passOutputByPointer;
	private boolean isReturnValue;
	private String newName;
	private int newOrder;
	private boolean isDeleted;
	private String defaultValue;
	private String newTypeName;

	public NameInformation(IASTName name) {
		this.name = name;
		this.newName = String.valueOf(name.getSimpleID());
		references = new ArrayList<IASTName>();
	}

	public static NameInformation createInfoForAddedParameter(String type, String name,
			String defaultValue) {
		NameInformation info= new NameInformation(null);
		info.setTypeName(type);
		info.setNewName(name);
		info.setDefaultValue(defaultValue);
		info.setNewOrder(INDEX_FOR_ADDED);
		return info;
	}

	/**
	 * For debugging only.
	 */
	@Override
	public String toString() {
		return name.toString();
	}

	public int getNewOrder() {
		return newOrder;
	}

	public void setNewOrder(int newOrder) {
		this.newOrder = newOrder;
	}

	/**
	 * Returns <code>true</code> if the value of the variable has to propagate to the outside world.
	 */
	public boolean isOutput() {
		return isOutput;
	}

	void setOutput(boolean isOutput) {
		this.isOutput = isOutput;
	}

	public boolean isOutputParameter() {
		return isOutput() && !isReturnValue();
	}

	public boolean mustBeReturnValue() {
		return mustBeReturnValue;
	}

	public void setMustBeReturnValue(boolean mustBeReturnValue) {
		this.mustBeReturnValue = mustBeReturnValue;
	}

	public boolean isReturnValue() {
		return mustBeReturnValue || isReturnValue;
	}

	public void setReturnValue(boolean isReturnValue) {
		Assert.isTrue(isReturnValue || !mustBeReturnValue);
		this.isReturnValue = isReturnValue;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public boolean isWriteAccess() {
		return isWriteAccess;
	}

	void setWriteAccess(boolean isWriteAceess) {
		this.isWriteAccess = isWriteAceess;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void markAsDeleted() {
		Assert.isTrue(!isAdded()); // Added parameters should be simply removed from the list
		isDeleted= true;
	}

	public boolean isAdded() {
		// TODO(sprigogin): Adding parameters is not supported yet.
		return false;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String value) {
		Assert.isNotNull(value);
		defaultValue= value;
	}

	public IASTName getDeclarationName() {
		return declarationName;
	}

	public IASTDeclarator getDeclarator() {
		return (IASTDeclarator) declarationName.getParent();
	}

	public IASTDeclSpecifier getDeclSpecifier() {
		IASTNode parent = getDeclarator().getParent();
		if (parent instanceof IASTSimpleDeclaration) {
			return ((IASTSimpleDeclaration) parent).getDeclSpecifier();
		} else if (parent instanceof IASTParameterDeclaration) {
			return ((IASTParameterDeclaration) parent).getDeclSpecifier();
		}
		return null;
	}

	void setDeclarationName(IASTName declarationName) {
		Assert.isTrue(declarationName.getParent() instanceof IASTDeclarator);
		this.declarationName = declarationName;
	}

	public IASTName getName() {
		return name;
	}

	public boolean isRenamed() {
		return name == null ? newName != null : String.valueOf(name.getSimpleID()).equals(name); 
	}

	void addReference(IASTName name) {
		references.add(name);
	}

	public String getTypeName() {
		if (newTypeName != null)
			return newTypeName;
		INodeFactory nodeFactory = name.getTranslationUnit().getASTNodeFactory();
		IASTParameterDeclaration declaration = getParameterDeclaration(nodeFactory, null);
		ASTWriterVisitor writer = new ASTWriterVisitor();
		declaration.accept(writer);
		return writer.toString();
	}

	public void setTypeName(String type) {
		Assert.isNotNull(type);
		newTypeName= type;
	}

	public String getReturnType() {
		if (!isReturnValue())
			return null;
		INodeFactory nodeFactory = name.getTranslationUnit().getASTNodeFactory();
		IASTDeclarator sourceDeclarator = getDeclarator();
		IASTDeclSpecifier declSpec = safeCopy(getDeclSpecifier());
		IASTDeclarator declarator = createDeclarator(nodeFactory, sourceDeclarator, null);
		IASTParameterDeclaration declaration = nodeFactory.newParameterDeclaration(declSpec, declarator);
		ASTWriterVisitor writer = new ASTWriterVisitor();
		declaration.accept(writer);
		return writer.toString();
	}

	public List<IASTName> getReferencesAfterSelection(int endOffset) {
		if (referencesAfterCached == null || lastCachedReferencesHash != references.hashCode()) {
			lastCachedReferencesHash = references.hashCode();
			referencesAfterCached = new ArrayList<IASTName>();
			for (IASTName ref : references) {
				IASTFileLocation loc = ref.getFileLocation();
				if (loc.getNodeOffset() >= endOffset) {
					referencesAfterCached.add(ref);
				}
			}
		}
		return referencesAfterCached;
	}

	public boolean isReferencedAfterSelection(int endOffset) {
		return !getReferencesAfterSelection(endOffset).isEmpty();
	}

	public IASTParameterDeclaration getParameterDeclaration(INodeFactory nodeFactory) {
		return getParameterDeclaration(nodeFactory, newName);
	}

	private IASTParameterDeclaration getParameterDeclaration(INodeFactory nodeFactory, String paramName) {
		IASTDeclarator sourceDeclarator = getDeclarator();
		IASTDeclSpecifier declSpec = safeCopy(getDeclSpecifier());
		IASTDeclarator declarator = createDeclarator(nodeFactory, sourceDeclarator, paramName);

		if (isOutputParameter()) {
			if (nodeFactory instanceof ICPPNodeFactory && !passOutputByPointer) {
				declarator.addPointerOperator(((ICPPNodeFactory) nodeFactory).newReferenceOperator(false));
			} else {
				declarator.addPointerOperator(nodeFactory.newPointer());
			}
		} else if (declSpec != null && !isWriteAccess) {
			IType type = TypeHelper.createType(sourceDeclarator);
			if (TypeHelper.shouldBePassedByReference(type, declarationName.getTranslationUnit())) {
				if (nodeFactory instanceof ICPPNodeFactory) {
					declarator.addPointerOperator(((ICPPNodeFactory) nodeFactory).newReferenceOperator(false));
				} else {
					declarator.addPointerOperator(nodeFactory.newPointer());
				}
				declSpec.setConst(true);
			}
		}

		declarator.setNestedDeclarator(sourceDeclarator.getNestedDeclarator());

		return nodeFactory.newParameterDeclaration(declSpec, declarator);
	}

	private IASTDeclarator createDeclarator(INodeFactory nodeFactory, IASTDeclarator sourceDeclarator,
			String name) {
		IASTName astName = name != null ?
				nodeFactory.newName(name.toCharArray()) : nodeFactory.newName();
		IASTDeclarator declarator;
		if (sourceDeclarator instanceof IASTArrayDeclarator) {
			IASTArrayDeclarator arrDeclarator = (IASTArrayDeclarator) sourceDeclarator;
			IASTArrayDeclarator arrayDeclarator = nodeFactory.newArrayDeclarator(astName);
			IASTArrayModifier[] arrayModifiers = arrDeclarator.getArrayModifiers();
			for (IASTArrayModifier arrayModifier : arrayModifiers) {
				arrayDeclarator.addArrayModifier(arrayModifier.copy(CopyStyle.withLocations));
			}
			declarator= arrayDeclarator;
		} else {
			declarator = nodeFactory.newDeclarator(astName);
		}
		for (IASTPointerOperator pointerOp : sourceDeclarator.getPointerOperators()) {
			declarator.addPointerOperator(pointerOp.copy(CopyStyle.withLocations));
		}
		return declarator;
	}

	@SuppressWarnings("unchecked")
	private static <T extends IASTNode> T safeCopy(T node) {
		return node == null ? null : (T) node.copy(CopyStyle.withLocations);
	}

	public ITranslationUnit getTranslationUnit() {
		return name != null ? name.getTranslationUnit().getOriginatingTranslationUnit() : null;
	}

	public boolean isPassOutputByPointer() {
		return passOutputByPointer;
	}

	public void setPassOutputByPointer(boolean passOutputByPointer) {
		this.passOutputByPointer = passOutputByPointer;
	}
}