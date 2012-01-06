/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
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
				@Override
				public int getEndingLineNumber() {
					return loc.getStartingLineNumber();
				}

				@Override
				public String getFileName() {
					return loc.getFileName();
				}

				@Override
				public int getStartingLineNumber() {
					return loc.getStartingLineNumber();
				}

				@Override
				public IASTFileLocation asFileLocation() {
					return loc.asFileLocation();
				}

				@Override
				public int getNodeLength() {
					return 0;
				}

				@Override
				public int getNodeOffset() {
					return loc.getNodeOffset();
				}

				@Override
				public IASTPreprocessorIncludeStatement getContextInclusionStatement() {
					return loc.getContextInclusionStatement();
				}
			};
		}

		@Override
		public boolean accept(ASTVisitor visitor) {
			return fDelegate.accept(visitor);
		}

		@Override
		public boolean contains(IASTNode node) {
			return fDelegate.contains(node);
		}

		@Override
		public IBinding getBinding() {
			return fDelegate.getBinding();
		}

		@Override
		public IBinding getPreBinding() {
			return fDelegate.getPreBinding();
		}

		@Override
		public String getContainingFilename() {
			return fLocation.getFileName();
		}

		@Override
		public IASTFileLocation getFileLocation() {
			return fLocation;
		}

		@Override
		public ILinkage getLinkage() {
			return fDelegate.getLinkage();
		}

		@Override
		public IASTNodeLocation[] getNodeLocations() {
			return fDelegate.getNodeLocations();
		}

		@Override
		public IASTNode getParent() {
			return fDelegate.getParent();
		}

		@Override
		public IASTNode[] getChildren() {
			return fDelegate.getChildren();
		}

		@Override
		public ASTNodeProperty getPropertyInParent() {
			return fDelegate.getPropertyInParent();
		}

		@Override
		public String getRawSignature() {
			return fDelegate.getRawSignature();
		}

		@Override
		public IASTTranslationUnit getTranslationUnit() {
			return fDelegate.getTranslationUnit();
		}

		@Override
		public int getRoleOfName(boolean allowResolution) {
			return fDelegate.getRoleOfName(allowResolution);
		}

		@Override
		public boolean isDeclaration() {
			return fDelegate.isDeclaration();
		}

		@Override
		public boolean isDefinition() {
			return fDelegate.isDefinition();
		}

		@Override
		public boolean isReference() {
			return fDelegate.isReference();
		}

		@Override
		public IBinding resolveBinding() {
			return fDelegate.resolveBinding();
		}

		@Override
		public IBinding resolvePreBinding() {
			return fDelegate.resolvePreBinding();
		}

		@Override
		public IASTCompletionContext getCompletionContext() {
			return fDelegate.getCompletionContext();
		}

		@Override
		public void setBinding(IBinding binding) {
			fDelegate.setBinding(binding);
		}

		@Override
		public void setParent(IASTNode node) {
			fDelegate.setParent(node);
		}

		@Override
		public void setPropertyInParent(ASTNodeProperty property) {
			fDelegate.setPropertyInParent(property);
		}

		@Override
		public char[] toCharArray() {
			return fDelegate.toCharArray();
		}

		@Override
		public char[] getSimpleID() {
			return fDelegate.getSimpleID();
		}
		
		@Override
		public char[] getLookupKey() {
			return fDelegate.getLookupKey();
		}

		@Override
		public IASTImageLocation getImageLocation() {
			return null;
		}

		@Override
		public boolean isPartOfTranslationUnitFile() {
			return fLocation.getFileName().equals(fDelegate.getTranslationUnit().getFilePath());
		}
		
		@Override
		public String toString() {
			return fDelegate.toString();
		}

		@Override
		public IASTName getLastName() {
			return this;
		}

		@Override
		public IToken getSyntax() throws ExpansionOverlapsBoundaryException,
				UnsupportedOperationException {
			return fDelegate.getSyntax();
		}

		@Override
		public IToken getLeadingSyntax() throws ExpansionOverlapsBoundaryException,
				UnsupportedOperationException {
			return fDelegate.getLeadingSyntax();
		}

		@Override
		public IToken getTrailingSyntax() throws ExpansionOverlapsBoundaryException,
				UnsupportedOperationException {
			return fDelegate.getTrailingSyntax();
		}
		
		@Override
		public boolean isFrozen() {
			return fDelegate.isFrozen();
		}
			
		@Override
		public boolean isActive() {
			return fDelegate.isFrozen();
		}

		@Override
		public IASTName copy() {
			throw new UnsupportedOperationException();
		}

		@Override
		public IASTName copy(CopyStyle style) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isQualified() {
			return fDelegate.isQualified();
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

		@Override
		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			return fDelegate.getAdapter(adapter);
		}

		@Override
		public IEnumerator[] getEnumerators() throws DOMException {
			return fDelegate.getEnumerators();
		}

		@Override
		public ILinkage getLinkage() {
			return fDelegate.getLinkage();
		}

		@Override
		public String getName() {
			return new String(fName);
		}

		@Override
		public char[] getNameCharArray() {
			return fName;
		}

		@Override
		public IScope getScope() throws DOMException {
			return fDelegate.getScope();
		}

		@Override
		public boolean isSameType(IType type) {
			return fDelegate.isSameType(type);
		}

		@Override
		public IBinding getOwner() {
			return fDelegate.getOwner();
		}
		
		@Override
		public long getMinValue() {
			return fDelegate.getMinValue();
		}

		@Override
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

		@Override
		public IField findField(String name) {
			return fDelegate.findField(name);
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			return fDelegate.getAdapter(adapter);
		}

		@Override
		public IScope getCompositeScope() {
			return fDelegate.getCompositeScope();
		}

		@Override
		public IField[] getFields() {
			return fDelegate.getFields();
		}

		@Override
		public int getKey() {
			return fDelegate.getKey();
		}

		@Override
		public ILinkage getLinkage() {
			return fDelegate.getLinkage();
		}

		@Override
		public String getName() {
			return new String(fName);
		}

		@Override
		public char[] getNameCharArray() {
			return fName;
		}

		@Override
		public IScope getScope() throws DOMException {
			return fDelegate.getScope();
		}

		@Override
		public boolean isSameType(IType type) {
			return fDelegate.isSameType(type);
		}
		
		@Override
		public IBinding getOwner() {
			return fDelegate.getOwner();
		}

		@Override
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

		@Override
		public String getName() {
			return new String(fName);
		}

		@Override
		public char[] getNameCharArray() {
			return fName;
		}

		@Override
		public String[] getQualifiedName() throws DOMException {
			String[] qn= fDelegate.getQualifiedName();
			if (qn.length < 1) {
				qn= new String[]{null};
			}
			qn[qn.length - 1]= new String(fName);
			return qn;
		}

		@Override
		public char[][] getQualifiedNameCharArray() throws DOMException {
			char[][] qn= fDelegate.getQualifiedNameCharArray();
			if (qn.length < 1) {
				qn= new char[][]{null};
			}
			qn[qn.length - 1]= fName;
			return qn;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			return fDelegate.getAdapter(adapter);
		}

		@Override
		public ILinkage getLinkage() {
			return fDelegate.getLinkage();
		}

		@Override
		public IScope getScope() throws DOMException {
			return fDelegate.getScope();
		}

		@Override
		public boolean isGloballyQualified() throws DOMException {
			return fDelegate.isGloballyQualified();
		}

		@Override
		public IBinding getOwner() {
			return fDelegate.getOwner();
		}
	}

	private static class AnonymousCPPEnumeration extends AnonymousCPPBinding implements ICPPEnumeration {
		public AnonymousCPPEnumeration(char[] name, IEnumeration delegate) {
			super(name, (ICPPBinding) delegate);
		}

		@Override
		public IEnumerator[] getEnumerators() throws DOMException {
			return ((IEnumeration) fDelegate).getEnumerators();
		}

		@Override
		public boolean isSameType(IType type) {
			return ((IEnumeration) fDelegate).isSameType(type);
		}
		
		@Override
		public long getMinValue() {
			return ((IEnumeration)fDelegate).getMinValue();
		}

		@Override
		public long getMaxValue() {
			return ((IEnumeration)fDelegate).getMaxValue();
		}

		@Override
		public boolean isScoped() {
			return ((ICPPEnumeration)fDelegate).isScoped();
		}

		@Override
		public IType getFixedType() {
			return ((ICPPEnumeration)fDelegate).getFixedType();
		}

		@Override
		public ICPPScope asScope() {
			return ((ICPPEnumeration)fDelegate).asScope();
		}
	}

	private static class AnonymousClassType extends AnonymousCPPBinding implements ICPPClassType {
		public AnonymousClassType(char[] name, ICPPClassType delegate) {
			super(name, delegate);
		}
		
		@Override
		public IField findField(String name) {
			return ((ICPPClassType) fDelegate).findField(name);
		}

		@Override
		public ICPPMethod[] getAllDeclaredMethods() {
			return ((ICPPClassType) fDelegate).getAllDeclaredMethods();
		}

		@Override
		public ICPPBase[] getBases() {
			return ((ICPPClassType) fDelegate).getBases();
		}

		@Override
		public IScope getCompositeScope() {
			return ((ICPPClassType) fDelegate).getCompositeScope();
		}

		@Override
		public ICPPConstructor[] getConstructors() {
			return ((ICPPClassType) fDelegate).getConstructors();
		}

		@Override
		public ICPPField[] getDeclaredFields() {
			return ((ICPPClassType) fDelegate).getDeclaredFields();
		}

		@Override
		public ICPPMethod[] getDeclaredMethods() {
			return ((ICPPClassType) fDelegate).getDeclaredMethods();
		}

		@Override
		public IField[] getFields() {
			return ((ICPPClassType) fDelegate).getFields();
		}

		@Override
		public IBinding[] getFriends() {
			return ((ICPPClassType) fDelegate).getFriends();
		}

		@Override
		public int getKey() {
			return ((ICPPClassType) fDelegate).getKey();
		}

		@Override
		public ICPPMethod[] getMethods() {
			return ((ICPPClassType) fDelegate).getMethods();
		}

		@Override
		public ICPPClassType[] getNestedClasses() {
			return ((ICPPClassType) fDelegate).getNestedClasses();
		}

		@Override
		public boolean isSameType(IType type) {
			return ((ICPPClassType) fDelegate).isSameType(type);
		}

		@Override
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
