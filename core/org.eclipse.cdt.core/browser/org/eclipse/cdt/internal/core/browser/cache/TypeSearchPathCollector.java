/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser.cache;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.core.runtime.Path;

/**
 * Collects type and dependency paths from search results.
 */
public class TypeSearchPathCollector implements IIndexSearchRequestor {
		
	public Set typePaths= new HashSet(5);
	public Set dependencyPaths= new HashSet(5);

	public void acceptClassDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames) {
		typePaths.add(resourcePath);
	}

	public void acceptNamespaceDeclaration(String resourcePath, char[] typeName, char[][] enclosingTypeNames) {
		typePaths.add(resourcePath);
	}

	public void acceptIncludeDeclaration(String resourcePath, char[] decodedSimpleName) {
		dependencyPaths.add(resourcePath);
	}

	public void acceptConstructorDeclaration(String resourcePath, char[] typeName, int parameterCount) { }
	public void acceptConstructorReference(String resourcePath, char[] typeName, int parameterCount) { }
	public void acceptFieldDeclaration(String resourcePath, char[] fieldName) { }
	public void acceptFieldReference(String resourcePath, char[] fieldName) { }
	public void acceptInterfaceDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName) { }
	public void acceptFunctionDeclaration(String resourcePath, char[] methodName, int parameterCount) { }
	public void acceptMethodDeclaration(String resourcePath, char[] methodName, int parameterCount, char[][] enclosingTypeNames) { }
	public void acceptMethodReference(String resourcePath, char[] methodName, int parameterCount) { }
	public void acceptPackageReference(String resourcePath, char[] packageName) { }
	public void acceptVariableDeclaration(String resourcePath, char[] simpleTypeName) { }
	public void acceptFieldDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames) { }
	public void acceptMacroDeclaration(String resourcePath, char[] decodedSimpleName) { }
	public void acceptSuperTypeReference(String resourcePath, char[] qualification, char[] typeName, char[] enclosingTypeName, char classOrInterface, char[] superQualification, char[] superTypeName, char superClassOrInterface, int modifiers) { }
	public void acceptSuperTypeReference(String resourcePath, char[] qualification, char[] typeName, char classOrInterface, char[] superQualification, char[] superTypeName, char superClassOrInterface, int modifiers) { }
	public void acceptTypeReference(String resourcePath, char[] typeName) { }
	
	/**
	 * Returns the paths that have been collected.
	 */
	public Set getPaths() {
		Set pathSet= new HashSet(typePaths.size());
		for (Iterator i= typePaths.iterator(); i.hasNext(); ) {
			String path= (String) i.next();
			pathSet.add(new Path(path));
		}
		return pathSet;
	}

	/**
	 * Returns the dependency paths that have been collected.
	 */
	public Set getDependencyPaths() {
		Set pathSet= new HashSet(dependencyPaths.size());
		for (Iterator i= dependencyPaths.iterator(); i.hasNext(); ) {
			String path= (String) i.next();
			pathSet.add(new Path(path));
		}
		return pathSet;
	}
}
