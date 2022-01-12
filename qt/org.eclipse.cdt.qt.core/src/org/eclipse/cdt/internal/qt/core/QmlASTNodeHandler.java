/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;

import org.eclipse.cdt.internal.qt.core.location.Position;
import org.eclipse.cdt.internal.qt.core.location.SourceLocation;
import org.eclipse.cdt.qt.core.IQMLAnalyzer;
import org.eclipse.cdt.qt.core.location.ISourceLocation;
import org.eclipse.cdt.qt.core.qmljs.IJSLiteral;
import org.eclipse.cdt.qt.core.qmljs.IJSRegExpLiteral;
import org.eclipse.cdt.qt.core.qmljs.IQmlASTNode;
import org.eclipse.cdt.qt.core.qmljs.IQmlObjectDefinition;
import org.eclipse.cdt.qt.core.qmljs.IQmlRootObject;

/**
 * Translates a JavaScript {@link Bindings} object into a QML AST. This class employs {@link java.lang.reflect.Proxy} in order to
 * dynamically create the AST at runtime.
 * <p>
 * To begin translation simply call the static method <code>createQmlASTProxy</code>. The AST is translated only when it needs to be
 * (i.e. when one of its 'get' methods are called).
 */
public class QmlASTNodeHandler implements InvocationHandler {
	private static final String NODE_QML_PREFIX = "QML"; //$NON-NLS-1$
	private static final String NODE_TYPE_PROPERTY = "type"; //$NON-NLS-1$
	private static final String NODE_REGEX_PROPERTY = "regex"; //$NON-NLS-1$
	private static final String CREATE_ENUM_METHOD = "fromObject"; //$NON-NLS-1$
	private static final String AST_PACKAGE = "org.eclipse.cdt.qt.core.qmljs."; //$NON-NLS-1$
	private static final String AST_QML_PREFIX = "IQml"; //$NON-NLS-1$
	private static final String AST_JS_PREFIX = "IJS"; //$NON-NLS-1$

	private static String getPropertyName(String method) {
		String name = ""; //$NON-NLS-1$
		if (method.startsWith("is")) { //$NON-NLS-1$
			name = method.substring(2, 3).toLowerCase() + method.substring(3);
		} else if (method.startsWith("get")) { //$NON-NLS-1$
			name = method.substring(3, 4).toLowerCase() + method.substring(4);
		}
		if (name.equalsIgnoreCase("identifier")) { //$NON-NLS-1$
			return "id"; //$NON-NLS-1$
		} else if (name.equalsIgnoreCase("location")) { //$NON-NLS-1$
			return "loc"; //$NON-NLS-1$
		}
		return name;
	}

	/**
	 * Constructs a new {@link IQmlASTNode} from the given {@link Bindings}. This is a helper method equivalent to
	 * <code>createQmlASTProxy(node, null)</code>
	 *
	 * @param node
	 *            the AST node as retrieved from Nashorn
	 * @return a Proxy representing the given node
	 * @throws ClassNotFoundException
	 *             if the node does not represent a valid QML AST Node
	 * @see {@link QmlASTNodeHandler#createQmlASTProxy(Bindings, Class)}
	 */
	public static IQmlASTNode createQmlASTProxy(Bindings node) throws ClassNotFoundException {
		return createQmlASTProxy(node, null);
	}

	/**
	 * Constructs a new {@link IQmlASTNode} from the given {@link Bindings}. If a return type is specified, it will take precedence
	 * over the type retrieved from the binding. This is useful for nodes that extend, but do not add functionality to, an acorn AST
	 * element. A good example of this is {@link IQmlRootObject} which extends {@link IQmlObjectDefinition}. We can easily determine
	 * the location in the AST at which we want an IQmlRootObject over an IQmlObjectDefinition and set the returnType accordingly.
	 *
	 * @param node
	 *            the node as retrieved from acorn
	 * @param returnType
	 *            the expected node to return or null
	 * @return a Proxy representing the given node
	 * @throws ClassNotFoundException
	 *             if the node does not represent a valid QML AST Node
	 */
	public static IQmlASTNode createQmlASTProxy(Bindings node, Class<?> returnType) throws ClassNotFoundException {
		String type = (String) node.getOrDefault(NODE_TYPE_PROPERTY, ""); //$NON-NLS-1$
		if (type.startsWith(NODE_QML_PREFIX)) {
			type = AST_QML_PREFIX + type.substring(3);
		} else {
			type = AST_JS_PREFIX + type;
		}
		Class<?> astClass = Class.forName(AST_PACKAGE + type);
		if (astClass.equals(IJSLiteral.class)) {
			// If this is a Literal, we have to distinguish it between a RegExp Literal using the 'regex' property
			if (node.get(NODE_REGEX_PROPERTY) != null) {
				astClass = IJSRegExpLiteral.class;
			}
		}
		if (returnType != null) {
			if (!IQmlASTNode.class.isAssignableFrom(astClass)) {
				throw new ClassCastException(astClass + " cannot be cast to " + IQmlASTNode.class); //$NON-NLS-1$
			}
			if (astClass.isAssignableFrom(returnType)) {
				astClass = returnType;
			}
		}
		return (IQmlASTNode) Proxy.newProxyInstance(QmlASTNodeHandler.class.getClassLoader(),
				new Class<?>[] { astClass }, new QmlASTNodeHandler(node));
	}

	private final QMLAnalyzer analyzer;
	private final Bindings node;
	private final Map<String, Object> methodResults;

	private QmlASTNodeHandler(Bindings node) {
		this.analyzer = (QMLAnalyzer) Activator.getService(IQMLAnalyzer.class);
		this.node = node;
		this.methodResults = new HashMap<>();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String mName = method.getName();
		if (!methodResults.containsKey(method.getName())) {
			// Invoke the default implementation of the method if possible
			if (method.isDefault()) {
				final Class<?> declaringClass = method.getDeclaringClass();
				Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
						.getDeclaredConstructor(Class.class, int.class);
				constructor.setAccessible(true);
				methodResults.put(mName, constructor.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE)
						.unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args));
			} else {
				// Use the return type of the method as well as its contents of the node to get the Object to return
				String pName = getPropertyName(mName);
				methodResults.put(mName, handleObject(node.get(pName), method.getReturnType()));
			}
		}
		return methodResults.get(mName);
	}

	private Object handleObject(Object value, Class<?> expectedType) throws Throwable {
		if (expectedType.isArray()) {
			Object arr = Array.newInstance(expectedType.getComponentType(), ((Bindings) value).size());
			int ctr = 0;
			for (Object obj : ((Bindings) value).values()) {
				Array.set(arr, ctr++, handleObject(obj, expectedType.getComponentType()));
			}
			return arr;
		} else if (expectedType.equals(Object.class)) {
			return value;
		} else if (expectedType.isAssignableFrom(ISourceLocation.class)) {
			// ISourceLocation doesn't correspond to an AST Node and needs to be created manually from
			// the given Bindings.
			if (value instanceof Bindings) {
				Bindings bind = (Bindings) value;
				SourceLocation loc = new SourceLocation();
				loc.setSource((String) bind.get("source")); //$NON-NLS-1$
				Bindings start = (Bindings) bind.get("start"); //$NON-NLS-1$
				loc.setStart(new Position(((Number) start.get("line")).intValue(), //$NON-NLS-1$
						((Number) start.get("column")).intValue())); //$NON-NLS-1$
				Bindings end = (Bindings) bind.get("end"); //$NON-NLS-1$
				loc.setEnd(new Position(((Number) end.get("line")).intValue(), //$NON-NLS-1$
						((Number) end.get("column")).intValue())); //$NON-NLS-1$
				return loc;
			}
			return new SourceLocation();
		} else if (expectedType.isAssignableFrom(List.class)) {
			if (value instanceof Bindings) {
				List<Object> list = new ArrayList<>();
				for (Bindings object : analyzer.toJavaArray((Bindings) value, Bindings[].class)) {
					list.add(QmlASTNodeHandler.createQmlASTProxy(object));
				}
				return list;
			}
			return null;
		} else if (expectedType.isPrimitive()) {
			return handlePrimitive(value, expectedType);
		} else if (expectedType.isAssignableFrom(Number.class)) {
			if (value instanceof Number) {
				return value;
			}
			return 0;
		} else if (expectedType.isEnum()) {
			return expectedType.getMethod(CREATE_ENUM_METHOD, Object.class).invoke(null, value);
		} else if (value instanceof Bindings) {
			return QmlASTNodeHandler.createQmlASTProxy((Bindings) value, expectedType);
		}
		return value;
	}

	private Object handlePrimitive(Object value, Class<?> expectedType) throws Throwable {
		if (expectedType.isPrimitive()) {
			if (expectedType.equals(Boolean.TYPE)) {
				if (value instanceof Boolean) {
					return value;
				}
				return false;
			} else if (expectedType.equals(Character.TYPE)) {
				if (value instanceof Character) {
					return value;
				}
				return '\0';
			} else if (expectedType.equals(Byte.TYPE)) {
				if (value instanceof Number) {
					return ((Number) value).byteValue();
				}
				return (byte) 0;
			} else if (expectedType.equals(Short.TYPE)) {
				if (value instanceof Number) {
					return ((Number) value).shortValue();
				}
				return (short) 0;
			} else if (expectedType.equals(Integer.TYPE)) {
				if (value instanceof Number) {
					return ((Number) value).intValue();
				}
				return 0;
			} else if (expectedType.equals(Long.TYPE)) {
				if (value instanceof Number) {
					return ((Number) value).longValue();
				}
				return 0l;
			} else if (expectedType.equals(Float.TYPE)) {
				if (value instanceof Number) {
					return ((Number) value).floatValue();
				}
				return 0.0f;
			} else if (expectedType.equals(Double.TYPE)) {
				if (value instanceof Number) {
					return ((Number) value).doubleValue();
				}
				return 0.0d;
			}
		}
		throw new IllegalArgumentException("expectedType was not a primitive type"); //$NON-NLS-1$
	}
}
