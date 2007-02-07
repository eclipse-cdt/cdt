/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.c.ICInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.core.runtime.CoreException;

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

		public String getContainingFilename() {
			return fDelegate.getContainingFilename();
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

		public ASTNodeProperty getPropertyInParent() {
			return fDelegate.getPropertyInParent();
		}

		public String getRawSignature() {
			return fDelegate.getRawSignature();
		}

		public IASTTranslationUnit getTranslationUnit() {
			return fDelegate.getTranslationUnit();
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

		public IBinding[] resolvePrefix() {
			return fDelegate.resolvePrefix();
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
	}

	private static class AnonymousEnumeration implements IEnumeration {
		private IEnumeration fDelegate;
		private char[] fName;

		public AnonymousEnumeration(char[] name, IEnumeration delegate) {
			fName= name;
			fDelegate= delegate;
		}

		public Object clone() {
			throw new PDOMNotImplementedError();
		}

		public Object getAdapter(Class adapter) {
			return fDelegate.getAdapter(adapter);
		}

		public IEnumerator[] getEnumerators() throws DOMException {
			return fDelegate.getEnumerators();
		}

		public ILinkage getLinkage() throws CoreException {
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
	}

	private static class AnonymousCompositeType implements ICompositeType {
		protected ICompositeType fDelegate;
		private char[] fName;

		public AnonymousCompositeType(char[] name, ICompositeType delegate) {
			fName= name;
			fDelegate= delegate;
		}

		public Object clone() {
			throw new PDOMNotImplementedError();
		}

		public IField findField(String name) throws DOMException {
			return fDelegate.findField(name);
		}

		public Object getAdapter(Class adapter) {
			return fDelegate.getAdapter(adapter);
		}

		public IScope getCompositeScope() throws DOMException {
			return fDelegate.getCompositeScope();
		}

		public IField[] getFields() throws DOMException {
			return fDelegate.getFields();
		}

		public int getKey() throws DOMException {
			return fDelegate.getKey();
		}

		public ILinkage getLinkage() throws CoreException {
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
	}

	private static class AnonymousClassType implements ICPPClassType {
		private ICPPClassType fDelegate;
		private char[] fName;

		public AnonymousClassType(char[] name, ICPPClassType delegate) {
			fName= name;
			fDelegate= delegate;
		}
		
		public Object clone() {
			throw new PDOMNotImplementedError();
		}

		public String getName() {
			return new String(fName);
		}

		public char[] getNameCharArray() {
			return fName;
		}

		public IField findField(String name) throws DOMException {
			return fDelegate.findField(name);
		}

		public Object getAdapter(Class adapter) {
			return fDelegate.getAdapter(adapter);
		}

		public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
			return fDelegate.getAllDeclaredMethods();
		}

		public ICPPBase[] getBases() throws DOMException {
			return fDelegate.getBases();
		}

		public IScope getCompositeScope() throws DOMException {
			return fDelegate.getCompositeScope();
		}

		public ICPPConstructor[] getConstructors() throws DOMException {
			return fDelegate.getConstructors();
		}

		public ICPPField[] getDeclaredFields() throws DOMException {
			return fDelegate.getDeclaredFields();
		}

		public ICPPMethod[] getDeclaredMethods() throws DOMException {
			return fDelegate.getDeclaredMethods();
		}

		public IField[] getFields() throws DOMException {
			return fDelegate.getFields();
		}

		public IBinding[] getFriends() throws DOMException {
			return fDelegate.getFriends();
		}

		public int getKey() throws DOMException {
			return fDelegate.getKey();
		}

		public ILinkage getLinkage() throws CoreException {
			return fDelegate.getLinkage();
		}

		public ICPPMethod[] getMethods() throws DOMException {
			return fDelegate.getMethods();
		}

		public ICPPClassType[] getNestedClasses() throws DOMException {
			return fDelegate.getNestedClasses();
		}

		public String[] getQualifiedName() throws DOMException {
			return fDelegate.getQualifiedName();
		}

		public char[][] getQualifiedNameCharArray() throws DOMException {
			return fDelegate.getQualifiedNameCharArray();
		}

		public IScope getScope() throws DOMException {
			return fDelegate.getScope();
		}

		public boolean isGloballyQualified() throws DOMException {
			return fDelegate.isGloballyQualified();
		}

		public boolean isSameType(IType type) {
			return fDelegate.isSameType(type);
		}
	}


	/**
	 * If the provided binding is anonymous, either an adapter is returned 
	 * that computes a name for the binding, or <code>null</code> if that
	 * is not appropriate (e.g. binding is not a type).
	 * Otherwise, if the binding has a name it is returned unchanged.
	 */
	public static IBinding getAdapterIfAnonymous(IBinding binding) {
		if (binding != null) {
			char[] name= binding.getNameCharArray();
			if (name.length == 0) {
				if (binding instanceof IEnumeration) {
					name= createNameForAnonymous(binding);
					if (name != null) {
						return new AnonymousEnumeration(name, (IEnumeration) binding);
					}
				}
				else if (binding instanceof ICPPClassType) {
					name= createNameForAnonymous(binding);
					if (name != null) {
						return new AnonymousClassType(name, (ICPPClassType) binding);
					}
				}
				else if (binding instanceof ICompositeType) {
					name= createNameForAnonymous(binding);
					if (name != null) {
						return new AnonymousCompositeType(name, (ICompositeType) binding);
					}
				}
			}
		}
		return binding;
	}

	private static char[] createNameForAnonymous(IBinding binding) {
		IASTNode node= null;
		if (binding instanceof ICInternalBinding) {
			node= ((ICInternalBinding) binding).getPhysicalNode();
		}
		else if (binding instanceof ICPPInternalBinding) {
			node= ((ICPPInternalBinding) binding).getDefinition();
		}
		if (node != null) {
			IASTFileLocation loc= node.getFileLocation();
			if (loc == null) {
				node= node.getParent();
				if (node != null) {
					loc= node.getFileLocation();
				}
			}
			if (loc != null) {
				char[] fname= loc.getFileName().toCharArray();
				int fnamestart= findFileNameStart(fname);
				StringBuffer buf= new StringBuffer();
				buf.append('{');
				buf.append(fname, fnamestart, fname.length-fnamestart);
				buf.append(':');
				buf.append(loc.getNodeOffset());
				buf.append('}');
				return buf.toString().toCharArray();
			}
		}
		return null;
	}

	private static int findFileNameStart(char[] fname) {
		for (int i= fname.length-2; i>=0; i--) {
			switch (fname[i]) {
			case '/':
			case '\\':
				return i+1;
			}
		}
		return 0;
	}

	/**
	 * If the name is empty and has no file location, either an adapter 
	 * that has a file location is returned, or <code>null</code> if that 
	 * is not possible.
	 * Otherwise if the provided name is not empty, it is returned unchanged.
	 */
	public static IASTName getAdapterIfAnonymous(IASTName name) {
		if (name.getFileLocation() == null) {
			if (name.toCharArray().length == 0) {
				IASTNode parent= name.getParent();
				if (parent != null) {
					IASTFileLocation loc= parent.getFileLocation();
					if (loc != null) {
						return new AnonymousASTName(name, loc);
					}
				}
			}
		}
		return name;
	}
}
