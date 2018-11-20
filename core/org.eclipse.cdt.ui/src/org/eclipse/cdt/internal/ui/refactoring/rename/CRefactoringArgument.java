/*******************************************************************************
 * Copyright (c) 2004, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;

/**
 * Represents the input to a refactoring. Important are file and offset, the rest
 * can be calculated from the AST.
 */
public class CRefactoringArgument {
	private int fOffset;
	private int fLength;
	private String fText = ""; //$NON-NLS-1$
	private int fKind = CRefactory.ARGUMENT_UNKNOWN;
	private IFile fFile;

	private IBinding fBinding;
	private IScope fScope;
	private IASTTranslationUnit fTranslationUnit;

	public CRefactoringArgument(IFile file, int offset, int length) {
		fKind = CRefactory.ARGUMENT_UNKNOWN;
		fFile = file;
		fOffset = offset;
		fLength = length;
	}

	public CRefactoringArgument(ICElement elem) {
		fKind = CRefactory.ARGUMENT_UNKNOWN;
		if (elem instanceof ISourceReference) {
			ISourceReference sref = (ISourceReference) elem;
			ISourceRange sr;
			try {
				sr = sref.getSourceRange();
				fFile = (IFile) sref.getTranslationUnit().getResource();
				fOffset = sr.getIdStartPos();
				fLength = sr.getIdLength();
			} catch (CModelException e) {
				CUIPlugin.log(e);
			}
		}
	}

	public String getName() {
		return fText;
	}

	public IFile getSourceFile() {
		return fFile;
	}

	public int getArgumentKind() {
		return fKind;
	}

	public int getOffset() {
		return fOffset;
	}

	public int getLength() {
		return fLength;
	}

	public void setName(String name) {
		fText = name.toString();
	}

	public void setName(IASTName name) {
		setName(name.toString());
	}

	public void setBinding(IASTTranslationUnit tu, IBinding binding, IScope scope) {
		fTranslationUnit = tu;
		fBinding = binding;
		fScope = scope;
		if (binding instanceof IVariable) {
			IVariable var = (IVariable) binding;
			if (binding instanceof IField) {
				fKind = CRefactory.ARGUMENT_FIELD;
			} else if (binding instanceof IParameter) {
				fKind = CRefactory.ARGUMENT_PARAMETER;
			} else {
				if (ASTManager.isLocalVariable(var, scope)) {
					fKind = CRefactory.ARGUMENT_LOCAL_VAR;
				} else {
					boolean isStatic = false;
					isStatic = var.isStatic();
					if (isStatic) {
						fKind = CRefactory.ARGUMENT_FILE_LOCAL_VAR;
					} else {
						fKind = CRefactory.ARGUMENT_GLOBAL_VAR;
					}
				}
			}
		} else if (binding instanceof IEnumerator) {
			fKind = CRefactory.ARGUMENT_ENUMERATOR;
		} else if (binding instanceof IFunction) {
			fKind = CRefactory.ARGUMENT_NON_VIRTUAL_METHOD;
			IFunction func = (IFunction) binding;
			if (binding instanceof ICPPMethod) {
				ICPPMethod method = (ICPPMethod) binding;
				if (ClassTypeHelper.isVirtual(method)) {
					fKind = CRefactory.ARGUMENT_VIRTUAL_METHOD;
				}
			} else {
				boolean isStatic = false;
				isStatic = func.isStatic();
				if (isStatic) {
					fKind = CRefactory.ARGUMENT_FILE_LOCAL_FUNCTION;
				} else {
					fKind = CRefactory.ARGUMENT_GLOBAL_FUNCTION;
				}
			}
		} else if (binding instanceof ICompositeType) {
			fKind = CRefactory.ARGUMENT_CLASS_TYPE;
		} else if (binding instanceof IEnumeration || binding instanceof ITypedef) {
			fKind = CRefactory.ARGUMENT_TYPE;
		} else if (binding instanceof ICPPNamespace) {
			fKind = CRefactory.ARGUMENT_NAMESPACE;
		} else if (binding instanceof IMacroBinding) {
			fKind = CRefactory.ARGUMENT_MACRO;
		}
	}

	public IScope getScope() {
		return fScope;
	}

	public IBinding getBinding() {
		return fBinding;
	}

	public IASTTranslationUnit getTranslationUnit() {
		return fTranslationUnit;
	}
}
