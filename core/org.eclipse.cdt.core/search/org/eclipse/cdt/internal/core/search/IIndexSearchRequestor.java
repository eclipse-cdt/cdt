/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.search;

import org.eclipse.cdt.core.search.BasicSearchMatch;

public interface IIndexSearchRequestor {
 
void acceptSearchMatch(BasicSearchMatch match);
/**
 * Accepts the declaration of a class in the compilation unit with the given resource path.
 * The class is declared in the given package and with the given type name. 
 * <p>
 * Note that the resource path can be null if the search query doesn't require it (eg. get all class names).
 */
void acceptClassDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames);
/**
 * Accepts the declaration of a constructor in the compilation unit with the given resource path.
 * The constructor is declared with a given name and number of arguments.
 */
void acceptConstructorDeclaration(String resourcePath, char[] typeName, int parameterCount);
/**
 * Accepts the reference to a constructor in the compilation unit with the given resource path.
 * The constructor is referenced using the given name and a number of arguments.
 *
 * Note that the resource path can be null if the search query doesn't require it.
 */
void acceptConstructorReference(String resourcePath, char[] typeName, int parameterCount);
/**
 * Accepts the declaration of a field in the compilation unit with the given resource path.
 * <p>
 * Note that the resource path can be null if the search query doesn't require it (eg. get all class names).
 * Likewise, the declaring package name and the declaring type names if the query doesn't require them.
 */
void acceptFieldDeclaration(String resourcePath, char[] fieldName);
/**
 * Accepts the reference to a field in the compilation unit with the given resource path.
 * The field is referenced using the given name 
 */
void acceptFieldReference(String resourcePath, char[] fieldName);
/**
 * Accepts the declaration of a method in the compilation unit with the given resource path.
 * The method is declared with a given method name and number of arguments.
 */
void acceptMethodDeclaration(String resourcePath, char[] methodName, int parameterCount, char[][] enclosingTypeNames);
/**
 * Accepts the reference to a method in the compilation unit with the given resource path.
 * The method is referenced using the given selector and a number of arguments.
 *
 * Note that the resource path can be null if the search query doesn't require it.
 */
void acceptMethodReference(String resourcePath, char[] methodName, int parameterCount);
/**
 * Accepts the reference to a supertype in the compilation unit with the given resource path.
 * Note that the resource path and/or the package name can be null.
 */
void acceptSuperTypeReference(String resourcePath, char[] qualification, char[] typeName, char[] enclosingTypeName, char classOrInterface, char[] superQualification, char[] superTypeName, char superClassOrInterface, int modifiers);
/**
 * Accepts the reference to a class in the compilation unit with the given resource path.
 * The class is referenced using the given type name.
 * <p>
 * Note that the resource path can be null if the search query doesn't require it.
 */
void acceptTypeReference(String resourcePath, char[] typeName);
/**
 * Accepts the declaration of a namespace in the compilation unit with the given resource path.
 */
void acceptNamespaceDeclaration(String resourcePath, char[] typeName, char[][] enclosingTypeNames);
/**
 * Accepts the declaration of a function in the compilation unit with the given resource path.
 * The function is declared with a given function name and number of arguments.
 */
void acceptFunctionDeclaration(String resourcePath, char[] methodName, int parameterCount);

void acceptVariableDeclaration(String resourcePath, char[] simpleTypeName);

void acceptFieldDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames);

void acceptMacroDeclaration(String resourcePath, char[] decodedSimpleName);

void acceptIncludeDeclaration(String resourcePath, char[] decodedSimpleName);

}