/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.IToken;

public class PDOMASTAdapter {
	private static class AnonymousASTName implements IASTName {
		private IASTName fDelegate;
		private IASTFileLocation fLocation;

		public AnonymousASTName(IASTName name, final IASTFileLocation loc) {
			fDelegate= name;
			fLocation= new IASTFileLocation() {
				public int getEndingLineNumber() {
					return loc.getStartingLineNumber();
				}

				public String getFileName() {
					return loc.getFileName();
				}

				public int getStartingLineNumber() {
					return loc.getStartingLineNumber();
				}

				public IASTFileLocation asFileLocation() {
					return loc.asFileLocation();
				}

				public int getNodeLength() {
					return 0;
				}

				public int getNodeOffset() {
					return loc.getNodeOffset();
				}

				public IASTPreprocessorIncludeStatement getContextInclusionStatement() {
					return loc.getContextInclusionStatement();
				}
			};
		}

		public boolean accept(ASTVisitor visitor) {
			return fDelegate.accept(visitor);
		}

		public boolean contains(IASTNode node) {
			return fDelegate.contains(node);
		}

		public IBinding getBinding() {
			return fDelegate.getBinding();
		}

		public IBinding getPreBinding() {
			return fDelegate.getPreBinding();
		}

		public String getContainingFilename() {
			return fLocation.getFileName();
		}

		public IASTFileLocation getFileLocation() {
			return fLocation;
		}

		public ILinkage getLinkage() {
			return fDelegate.getLinkage();
		}

		public IASTNodeLocation[] getNodeLocations() {
			return fDelegate.getNodeLocations();
		}

		public IASTNode getParent() {
			return fDelegate.getParent();
		}

		public IASTNode[] getChildren() {
			return fDelegate.getChildren();
		}

		public ASTNodeProperty getPropertyInParent() {
			return fDelegate.getPropertyInParent();
		}

		public String getRawSignature() {
			return fDelegate.getRawSignature();
		}

		public IASTTranslationUnit getTranslationUnit() {
			return fDelegate.getTranslationUnit();
		}

		public int getRoleOfName(boolean allowResolution) {
			return fDelegate.getRoleOfName(allowResolution);
		}

		public boolean isDeclaration() {
			return fDelegate.isDeclaration();
		}

		public boolean isDefinition() {
			return fDelegate.isDefinition();
		}

		public boolean isReference() {
			return fDelegate.isReference();
		}

		public IBinding resolveBinding() {
			return fDelegate.resolveBinding();
		}

		public IBinding resolvePreBinding() {
			return fDelegate.resolvePreBinding();
		}

		public IASTCompletionContext getCompletionContext() {
			return fDelegate.getCompletionContext();
		}

		public void setBinding(IBinding binding) {
			fDelegate.setBinding(binding);
		}

		public void setParent(IASTNode node) {
			fDelegate.setParent(node);
		}

		public void setPropertyInParent(ASTNodeProperty property) {
			fDelegate.setPropertyInParent(property);
		}

		public char[] toCharArray() {
			return fDelegate.toCharArray();
		}

		public char[] getSimpleID() {
			return fDelegate.getSimpleID();
		}
		
		public char[] getLookupKey() {
			return fDelegate.getLookupKey();
		}

		public IASTImageLocation getImageLocation() {
			return null;
		}

		public boolean isPartOfTranslationUnitFile() {
			return fLocation.getFileName().equals(fDelegate.getTranslationUnit().getFilePath());
		}
		
		@Override
		public String toString() {
			return fDelegate.toString();
		}

		public IASTName getLastName() {
			return this;
		}

		public IToken getSyntax() throws ExpansionOverlapsBoundaryException,
				UnsupportedOperationException {
			return fDelegate.getSyntax();
		}

		public IToken getLeadingSyntax() throws ExpansionOverlapsBoundaryException,
				UnsupportedOperationException {
			return fDelegate.getLeadingSyntax();
		}

		public IToken getTrailingSyntax() throws ExpansionOverlapsBoundaryException,
				UnsupportedOperationException {
			return fDelegate.getTrailingSyntax();
		}
		
		public boolean isFrozen() {
			return fDelegate.isFrozen();
		}
			
		public boolean isActive() {
			return fDelegate.isFrozen();
		}

		public IASTName copy() {
			throw new UnsupportedOperationException();
		}

		public IASTName copy(CopyStyle style) {
			throw new UnsupportedOperationException();
		}
	}

	private static class AnonymousEnumeration implements IEnumeration {
		private IEnumeration fDelegate;
		private char[] fName;

		public AnonymousEnumeration(char[] name, IEnumeration delegate) {
			fName= name;
			fDelegate= delegate;
		}

		@Override
		public Object clone() {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			return fDelegate.getAdapter(adapter);
		}

		public IEnumerator[] getEnumerators() throws DOMException {
			return fDelegate.getEnumerators();
		}

		public ILinkage getLinkage() {
			return fDelegate.getLinkage();
		}

		public String getName() {
			return new String(fName);
		}

		public char[] getNameCharArray() {
			return fName;
		}

		public IScope getScope() throws DOMException {
			return fDelegate.getScope();
		}

		public boolean isSameType(IType type) {
			return fDelegate.isSameType(type);
		}

		public IBinding getOwner() {
			return fDelegate.getOwner();
		}
		
		public long getMinValue() {
			return fDelegate.getMinValue();
		}

		public long getMaxValue() {
			return fDelegate.getMaxValue();
		}
	}

	private static class AnonymousCompositeType implements ICompositeType {
		protected ICompositeType fDelegate;
		private char[] fName;

		public AnonymousCompositeType(char[] name, ICompositeType delegate) {
			fName= name;
			fDelegate= delegate;
		}

		@Override
		public Object clone() {
			throw new UnsupportedOperationException();
		}

		public IField findField(String name) {
			return fDelegate.findField(name);
		}

		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			return fDelegate.getAdapter(adapter);
		}

		public IScope getCompositeScope() {
			return fDelegate.getCompositeScope();
		}

		public IField[] getFields() {
			return fDelegate.getFields();
		}

		public int getKey() {
			return fDelegate.getKey();
		}

		public ILinkage getLinkage() {
			return fDelegate.getLinkage();
		}

		public String getName() {
			return new String(fName);
		}

		public char[] getNameCharArray() {
			return fName;
		}

		public IScope getScope() throws DOMException {
			return fDelegate.getScope();
		}

		public boolean isSameType(IType type) {
			return fDelegate.isSameType(type);
		}
		
		public IBinding getOwner() {
			return fDelegate.getOwner();
		}

		public boolean isAnonymous() {
			return fDelegate.isAnonymous();
		}
	}

	private static class AnonymousCPPBinding implements ICPPBinding {
		protected ICPPBinding fDelegate;
		private char[] fName;

		public AnonymousCPPBinding(char[] name, ICPPBinding delegate) {
			fName= name;
			fDelegate= delegate;
		}
		
		@Override
		public Object clone() {
			throw new UnsupportedOperationException();
		}

		public String getName() {
			return new String(fName);
		}

		public char[] getNameCharArray() {
			return fName;
		}

		public String[] getQualifiedName() throws DOMException {
			String[] qn= fDelegate.getQualifiedName();
			if (qn.length < 1) {
				qn= new String[]{null};
			}
			qn[qn.length - 1]= new String(fName);
			return qn;
		}

		public char[][] getQualifiedNameCharArray() throws DOMException {
			char[][] qn= fDelegate.getQualifiedNameCharArray();
			if (qn.length < 1) {
				qn= new char[][]{null};
			}
			qn[qn.length - 1]= fName;
			return qn;
		}

		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			return fDelegate.getAdapter(adapter);
		}

		public ILinkage getLinkage() {
			return fDelegate.getLinkage();
		}

		public IScope getScope() throws DOMException {
			return fDelegate.getScope();
		}

		public boolean isGloballyQualified() throws DOMException {
			return fDelegate.isGloballyQualified();
		}

		public IBinding getOwner() {
			return fDelegate.getOwner();
		}
	}

	private static class AnonymousCPPEnumeration extends AnonymousCPPBinding implements ICPPEnumeration {
		public AnonymousCPPEnumeration(char[] name, IEnumeration delegate) {
			super(name, (ICPPBinding) delegate);
		}

		public IEnumerator[] getEnumerators() throws DOMException {
			return ((IEnumeration) fDelegate).getEnumerators();
		}

		public boolean isSameType(IType type) {
			return ((IEnumeration) fDelegate).isSameType(type);
		}
		
		public long getMinValue() {
			return ((IEnumeration)fDelegate).getMinValue();
		}

		public long getMaxValue() {
			return ((IEnumeration)fDelegate).getMaxValue();
		}

		public boolean isScoped() {
			return ((ICPPEnumeration)fDelegate).isScoped();
		}

		public IType getFixedType() {
			return ((ICPPEnumeration)fDelegate).getFixedType();
		}

		public ICPPScope asScope() {
			return ((ICPPEnumeration)fDelegate).asScope();
		}
	}

	private static class AnonymousClassType extends AnonymousCPPBinding implements ICPPClassType {
		public AnonymousClassType(char[] name, ICPPClassType delegate) {
			super(name, delegate);
		}
		
		public IField findField(String name) {
			return ((ICPPClassType) fDelegate).findField(name);
		}

		public ICPPMethod[] getAllDeclaredMethods() {
			return ((ICPPClassType) fDelegate).getAllDeclaredMethods();
		}

		public ICPPBase[] getBases() {
			return ((ICPPClassType) fDelegate).getBases();
		}

		public IScope getCompositeScope() {
			return ((ICPPClassType) fDelegate).getCompositeScope();
		}

		public ICPPConstructor[] getConstructors() {
			return ((ICPPClassType) fDelegate).getConstructors();
		}

		public ICPPField[] getDeclaredFields() {
			return ((ICPPClassType) fDelegate).getDeclaredFields();
		}

		public ICPPMethod[] getDeclaredMethods() {
			return ((ICPPClassType) fDelegate).getDeclaredMethods();
		}

		public IField[] getFields() {
			return ((ICPPClassType) fDelegate).getFields();
		}

		public IBinding[] getFriends() {
			return ((ICPPClassType) fDelegate).getFriends();
		}

		public int getKey() {
			return ((ICPPClassType) fDelegate).getKey();
		}

		public ICPPMethod[] getMethods() {
			return ((ICPPClassType) fDelegate).getMethods();
		}

		public ICPPClassType[] getNestedClasses() {
			return ((ICPPClassType) fDelegate).getNestedClasses();
		}

		public boolean isSameType(IType type) {
			return ((ICPPClassType) fDelegate).isSameType(type);
		}

		public boolean isAnonymous() {
			return ((ICPPClassType) fDelegate).isAnonymous();
		}
	}


	/**
	 * If the provided binding is anonymous, either an adapter is returned 
	 * that computes a name for the binding, or <code>null</code> if that
	 * is not appropriate (e.g. binding is not a type).
	 * Otherwise, if the binding has a name it is returned unchanged.
	 */
	public static IBinding getAdapterForAnonymousASTBinding(IBinding binding) {
		if (binding != null && !(binding instanceof IIndexBinding)) {
			char[] name= binding.getNameCharArray();
			if (name.length == 0) {
				if (binding instanceof IEnumeration) {
					name = ASTTypeUtil.createNameForAnonymous(binding);
					if (name != null) {
						if (binding instanceof ICPPBinding) {
							return new AnonymousCPPEnumeration(name, (IEnumeration) binding);
						}
						return new AnonymousEnumeration(name, (IEnumeration) binding);
					}
				} else if (binding instanceof ICPPClassType) {
					name = ASTTypeUtil.createNameForAnonymous(binding);
					if (name != null) {
						return new AnonymousClassType(name, (ICPPClassType) binding);
					}
				} else if (binding instanceof ICompositeType) {
					name = ASTTypeUtil.createNameForAnonymous(binding);
					if (name != null) {
						return new AnonymousCompositeType(name, (ICompositeType) binding);
					}
				} else if (binding instanceof ICPPTemplateParameter) {
					return binding;
				} else if (binding instanceof ICPPConstructor) {
					return binding;
				}
				return null;
			}
		}
		return binding;
	}

	/**
	 * If the name is empty and has no file location, either an adapter 
	 * that has a file location is returned, or <code>null</code> if that 
	 * is not possible (no parent with a file location).
	 * Otherwise if the provided name is not empty or has a file location, 
	 * it is returned unchanged.
	 */
	public static IASTName getAdapterIfAnonymous(IASTName name) {
		if (name.getLookupKey().length == 0) {
			if (name.getFileLocation() == null) {
				IASTNode parent= name.getParent();
				if (parent != null) {
					IASTFileLocation loc= parent.getFileLocation();
					if (loc != null) {
						return new AnonymousASTName(name, loc);
					}
				}
				return null;
			}
		}
		return name;
	}
}
