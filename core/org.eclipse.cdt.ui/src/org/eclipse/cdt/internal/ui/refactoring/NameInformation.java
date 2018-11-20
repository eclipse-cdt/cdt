/*******************************************************************************
 * Copyright (c) 2008, 2013 Institute for Software, HSR Hochschule fuer Technik
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
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.DeclarationGenerator;
import org.eclipse.cdt.core.dom.rewrite.TypeHelper;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriterVisitor;
import org.eclipse.core.runtime.Assert;

/**
 * Additional information about an IASTName in code being refactored.
 */
public class NameInformation {
	public static enum Indirection {
		NONE, POINTER, REFERENCE
	}

	public static final int INDEX_FOR_ADDED = -1;

	private final IASTName name;
	private IASTName declarationName;
	private final List<IASTName> referencesInSelection;
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
	private Indirection indirection;

	public NameInformation(IASTName name) {
		this.name = name;
		this.newName = String.valueOf(name.getSimpleID());
		referencesInSelection = new ArrayList<>();
	}

	public static NameInformation createInfoForAddedParameter(String type, String name, String defaultValue) {
		NameInformation info = new NameInformation(null);
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
		indirection = null;
	}

	public boolean isOutputParameter() {
		return isOutput() && !isReturnValue();
	}

	public boolean mustBeReturnValue() {
		return mustBeReturnValue;
	}

	public void setMustBeReturnValue(boolean mustBeReturnValue) {
		this.mustBeReturnValue = mustBeReturnValue;
		indirection = null;
	}

	public boolean isReturnValue() {
		return mustBeReturnValue || isReturnValue;
	}

	public void setReturnValue(boolean isReturnValue) {
		Assert.isTrue(isReturnValue || !mustBeReturnValue);
		this.isReturnValue = isReturnValue;
		indirection = null;
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
		indirection = null;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void markAsDeleted() {
		Assert.isTrue(!isAdded()); // Added parameters should be simply removed from the list
		isDeleted = true;
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
		defaultValue = value;
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
		Assert.isTrue(declarationName.getPropertyInParent() == IASTDeclarator.DECLARATOR_NAME
				|| declarationName.getPropertyInParent() == IASTLabelStatement.NAME);
		this.declarationName = declarationName;
		indirection = null;
	}

	public IASTName getName() {
		return name;
	}

	public boolean isRenamed() {
		return name == null ? newName != null : !String.valueOf(name.getSimpleID()).equals(newName);
	}

	void addReference(IASTName name, int startOffset, int endOffset) {
		int nodeOffset = name.getFileLocation().getNodeOffset();
		if (nodeOffset >= startOffset && nodeOffset < endOffset) {
			referencesInSelection.add(name);
		}
	}

	public String getTypeName() {
		if (newTypeName != null)
			return newTypeName;
		IASTTranslationUnit translationUnit = name.getTranslationUnit();
		INodeFactory nodeFactory = translationUnit.getASTNodeFactory();
		IASTParameterDeclaration declaration = getParameterDeclaration(nodeFactory, null);
		if (declaration == null)
			return null;
		ASTWriterVisitor writer = new ASTWriterVisitor(translationUnit.getOriginatingTranslationUnit());
		declaration.accept(writer);
		return writer.toString();
	}

	public void setTypeName(String type) {
		Assert.isNotNull(type);
		newTypeName = type;
	}

	public String getReturnType() {
		if (!isReturnValue())
			return null;
		IASTTranslationUnit translationUnit = name.getTranslationUnit();
		INodeFactory nodeFactory = translationUnit.getASTNodeFactory();
		IASTDeclarator sourceDeclarator = getDeclarator();
		IASTDeclSpecifier declSpec = safeCopy(getDeclSpecifier());
		IASTDeclarator declarator = createDeclarator(nodeFactory, sourceDeclarator, null);
		IASTParameterDeclaration declaration = nodeFactory.newParameterDeclaration(declSpec, declarator);
		ASTWriterVisitor writer = new ASTWriterVisitor(translationUnit.getOriginatingTranslationUnit());
		declaration.accept(writer);
		return writer.toString();
	}

	public List<IASTName> getReferencesInSelection() {
		return referencesInSelection;
	}

	public IASTParameterDeclaration getParameterDeclaration(INodeFactory nodeFactory) {
		return getParameterDeclaration(nodeFactory, newName);
	}

	private IASTParameterDeclaration getParameterDeclaration(INodeFactory nodeFactory, String paramName) {
		IASTDeclSpecifier sourceDeclSpec = getDeclSpecifier();
		IASTDeclarator sourceDeclarator = getDeclarator();
		IASTDeclSpecifier declSpec;
		IASTDeclarator declarator;
		if (sourceDeclSpec instanceof IASTSimpleDeclSpecifier
				&& ((IASTSimpleDeclSpecifier) sourceDeclSpec).getType() == IASTSimpleDeclSpecifier.t_auto) {
			IType type = CPPVisitor.createType(sourceDeclarator);
			DeclarationGenerator generator = DeclarationGenerator.create(nodeFactory);
			declSpec = generator.createDeclSpecFromType(type);
			declarator = generator.createDeclaratorFromType(type, paramName == null ? null : paramName.toCharArray());
		} else {
			declSpec = safeCopy(sourceDeclSpec);
			declarator = createDeclarator(nodeFactory, sourceDeclarator, paramName);
		}

		Indirection indirection = getIndirection();
		if (indirection == Indirection.POINTER) {
			declarator.addPointerOperator(nodeFactory.newPointer());
		} else if (indirection == Indirection.REFERENCE) {
			declarator.addPointerOperator(((ICPPNodeFactory) nodeFactory).newReferenceOperator(false));
		}

		if (indirection != Indirection.NONE && !isWriteAccess && declSpec != null) {
			declSpec.setConst(true);
		}

		declarator.setNestedDeclarator(sourceDeclarator.getNestedDeclarator());
		return nodeFactory.newParameterDeclaration(declSpec, declarator);
	}

	public Indirection getIndirection() {
		if (indirection == null) {
			indirection = Indirection.NONE;
			boolean isCpp = declarationName.getTranslationUnit() instanceof ICPPASTTranslationUnit;
			if (isOutputParameter()) {
				if (isCpp && !passOutputByPointer) {
					indirection = Indirection.REFERENCE;
				} else {
					indirection = Indirection.POINTER;
				}
			} else {
				IType type = TypeHelper.createType(getDeclarator());
				if (TypeHelper.shouldBePassedByReference(type, declarationName.getTranslationUnit())) {
					if (isCpp) {
						if (!isWriteAccess) {
							indirection = Indirection.REFERENCE;
						}
						// TODO(sprigogin): Verify availability of the copy ctor before passing by value
					} else {
						indirection = Indirection.POINTER;
					}
				}
			}
		}
		return indirection;
	}

	private IASTDeclarator createDeclarator(INodeFactory nodeFactory, IASTDeclarator sourceDeclarator, String name) {
		IASTName astName = name != null ? nodeFactory.newName(name.toCharArray()) : nodeFactory.newName();
		IASTDeclarator declarator;
		if (sourceDeclarator instanceof IASTArrayDeclarator) {
			IASTArrayDeclarator arrDeclarator = (IASTArrayDeclarator) sourceDeclarator;
			IASTArrayDeclarator arrayDeclarator = nodeFactory.newArrayDeclarator(astName);
			IASTArrayModifier[] arrayModifiers = arrDeclarator.getArrayModifiers();
			for (IASTArrayModifier arrayModifier : arrayModifiers) {
				arrayDeclarator.addArrayModifier(arrayModifier.copy(CopyStyle.withLocations));
			}
			declarator = arrayDeclarator;
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
		indirection = null;
	}
}