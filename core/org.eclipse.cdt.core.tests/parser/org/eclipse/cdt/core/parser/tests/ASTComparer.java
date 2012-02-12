/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mike Kucera (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

public class ASTComparer extends Assert {

	private static Set<String> methodsToIgnore = new HashSet<String>(Arrays.asList(
		// Prevent infinite recursion
		"getParent",
		"getTranslationUnit",
		"getLastName",

		// Exponential complexity
		"getOperand2", // duplicates getInitOperand2()
		"getChildren",

		// Can be different in copy
		"isFrozen",
		"getContainingFilename",
		"getOriginalNode",
		
		// These methods are problematic
		"getProblem",
		
		// Ignore preprocessor nodes
		"getMacroDefinitions",
		"getBuiltinMacroDefinitions",
		"getIncludeDirectives",
		"getAllPreprocessorStatements",
		"getMacroExpansions",
		"getPreprocessorProblems",
		"getComments",
		
		// Avoid name resolution
		"isDeclaration",
		"isDefinition",
		"isReference",
		"isAssociatedWithLastName",
		"getNestingLevel",
		"getImplicitNames",
		"isLValue"
	));

	public static void assertCopy(IASTNode node1, IASTNode node2) {
		try {
			assertCopy(node1, node2, 0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void assertCopy(IASTNode node1, IASTNode node2, int n) throws Exception {
		if (node1 == null && node2 == null)
			return;
		assertNotNull(node1);
		assertNotNull(node2);
		assertFalse(node1 == node2); // must be distinct copy

		Class klass1 = node1.getClass();
		Class klass2 = node2.getClass();
		assertTrue(klass1.equals(klass2)); // nodes must be the same concrete type
		//System.out.println(spaces(n) + klass1.getSimpleName());
		
		BeanInfo beanInfo = Introspector.getBeanInfo(klass1);
		
		for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
			Method getter = property.getReadMethod();
			if (getter == null)
				continue;
			if (methodsToIgnore.contains(getter.getName()))
				continue;

			if (getter.getAnnotation(Deprecated.class) != null)
				continue;
			
			try {
				Class returnType = getter.getReturnType();
				
				if (IASTNode.class.isAssignableFrom(returnType)) {
					//System.out.println(spaces(n) + "Testing1: " + getter.getName());
					IASTNode result1 = (IASTNode) getter.invoke(node1);
					IASTNode result2 = (IASTNode) getter.invoke(node2);
					assertCopy(result1, result2, n + 1); // members must be same
				} else if (returnType.isArray() && IASTNode.class.isAssignableFrom(returnType.getComponentType())) {
					//System.out.println(spaces(n) + "Testing2: " + getter.getName());
					IASTNode[] result1 = (IASTNode[]) getter.invoke(node1);
					IASTNode[] result2 = (IASTNode[]) getter.invoke(node2);
					if (result1 == null && result2 == null)
						continue;
					assertNotNull(result1);
					assertNotNull(result2);
					assertEquals(result1.length, result2.length);
					for(int i = 0; i < result1.length; i++)
						assertCopy(result1[i], result2[i], n + 1);
				} else if ((returnType.isPrimitive() || returnType.equals(String.class)) && !returnType.equals(Void.class)) {
					//System.out.println(spaces(n) + "Testing3: " + getter.getName());
					Object result1 = getter.invoke(node1);
					Object result2 = getter.invoke(node2);
					assertEquals(result1, result2);
				}
			} catch (AssertionFailedError e) {
				System.out.printf("Failure when calling %s.%s() @(%d,%d)\n",
						node1.getClass().getSimpleName(),
						getter.getName(),
						((ASTNode) node1).getOffset(),
						((ASTNode) node1).getLength());
				throw e;
			}				
		}
		
	}
	
//	private static String spaces(int n) {
//		char[] spaces = new char[n*2];
//		Arrays.fill(spaces, ' ');
//		return new String(spaces);
//	}
}
