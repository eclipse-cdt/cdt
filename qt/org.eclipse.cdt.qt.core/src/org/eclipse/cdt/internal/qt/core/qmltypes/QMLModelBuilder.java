/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
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
package org.eclipse.cdt.internal.qt.core.qmltypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.qt.core.qmljs.IJSArrayExpression;
import org.eclipse.cdt.qt.core.qmljs.IJSExpression;
import org.eclipse.cdt.qt.core.qmljs.IQmlASTNode;
import org.eclipse.cdt.qt.core.qmljs.IQmlBinding;
import org.eclipse.cdt.qt.core.qmljs.IQmlProgram;
import org.eclipse.cdt.qt.core.qmljs.IQmlPropertyBinding;
import org.eclipse.cdt.qt.core.qmljs.IQmlQualifiedID;
import org.eclipse.cdt.qt.core.qmljs.IQmlRootObject;
import org.eclipse.cdt.qt.core.qmljs.IQmlScriptBinding;
import org.eclipse.cdt.qt.core.qmljs.QMLExpressionEvaluator;
import org.eclipse.cdt.qt.core.qmljs.QMLExpressionEvaluator.InvalidExpressionException;

public class QMLModelBuilder {

	private final Map<String, QMLModuleInfo> moduleMap = new HashMap<>();

	public QMLModelBuilder() {
	}

	public QMLModuleInfo addModule(String module, IQmlASTNode ast) {
		QMLModuleInfo info = moduleMap.get(module);
		if (!moduleMap.containsKey(module)) {
			if (ensureNode(ast, IQmlProgram.class)) {
				IQmlRootObject obj = ((IQmlProgram) ast).getRootObject();
				if (ensureNode(obj, IQmlRootObject.class)) {
					info = new QMLModuleInfo(this, obj);
					moduleMap.put(module, info);
				}
			}
		}
		return info;
	}

	public QMLModuleInfo getModule(String module) {
		return moduleMap.get(module);
	}

	public boolean hasModule(String module) {
		return moduleMap.get(module) != null;
	}

	boolean ensureIdentifier(IQmlQualifiedID actual, String expected) {
		if (!actual.getName().equals(expected)) {
			Activator.log("[QmlTypes] Unexpected node identifier: expected '" + expected + "', but was '" //$NON-NLS-1$ //$NON-NLS-2$
					+ actual.getName() + "'"); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	boolean ensureNode(IQmlASTNode actual, Class<? extends IQmlASTNode> expected) {
		if (!expected.isInstance(actual)) {
			Activator.log("[QmlTypes] Expected node '" + expected + "', but was '" //$NON-NLS-1$//$NON-NLS-2$
					+ actual.getClass().getInterfaces()[0] + "'"); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	boolean ensureValue(Object actual, Class<?> expected) {
		if (!expected.isInstance(actual)) {
			Activator.log("[QmlTypes] Unexpected value: expected '" + expected + "', but was '" //$NON-NLS-1$ //$NON-NLS-2$
					+ actual.getClass().getInterfaces()[0] + "'"); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	void unexpectedNode(IQmlASTNode node) {
		Activator.log("[QmlTypes] Unexpected node '" + node.getClass().getInterfaces()[0] + "'"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	String getStringBinding(IQmlPropertyBinding prop) {
		IQmlBinding b = prop.getBinding();
		if (ensureNode(b, IQmlScriptBinding.class)) {
			IQmlScriptBinding sb = (IQmlScriptBinding) b;
			if (ensureNode(sb.getScript(), IJSExpression.class)) {
				try {
					Object value = QMLExpressionEvaluator.evaluateConstExpr((IJSExpression) sb.getScript());
					if (value instanceof String) {
						return (String) value;
					}
				} catch (InvalidExpressionException e) {
					handleException(e);
				}
			}
		}
		return null;
	}

	String[] getStringArrayBinding(IQmlPropertyBinding prop) {
		ArrayList<String> result = new ArrayList<>();
		IQmlBinding b = prop.getBinding();
		if (ensureNode(b, IQmlScriptBinding.class)) {
			IQmlScriptBinding sb = (IQmlScriptBinding) b;
			if (ensureNode(sb.getScript(), IJSArrayExpression.class)) {
				IJSArrayExpression arrExpr = (IJSArrayExpression) sb.getScript();
				for (IJSExpression expr : arrExpr.getElements()) {
					try {
						Object value = QMLExpressionEvaluator.evaluateConstExpr(expr);
						if (value instanceof String) {
							result.add((String) value);
						}
					} catch (InvalidExpressionException e) {
						handleException(e);
					}
				}
			}
		}
		return result.toArray(new String[result.size()]);
	}

	public Integer[] getIntegerArrayBinding(IQmlPropertyBinding prop) {
		ArrayList<Integer> result = new ArrayList<>();
		IQmlBinding b = prop.getBinding();
		if (ensureNode(b, IQmlScriptBinding.class)) {
			IQmlScriptBinding sb = (IQmlScriptBinding) b;
			if (ensureNode(sb.getScript(), IJSArrayExpression.class)) {
				IJSArrayExpression arrExpr = (IJSArrayExpression) sb.getScript();
				for (IJSExpression expr : arrExpr.getElements()) {
					try {
						Object value = QMLExpressionEvaluator.evaluateConstExpr(expr);
						if (value instanceof Number) {
							result.add(((Number) value).intValue());
						}
					} catch (InvalidExpressionException e) {
						handleException(e);
					}
				}
			}
		}
		return result.toArray(new Integer[result.size()]);
	}

	boolean getBooleanBinding(IQmlPropertyBinding prop) {
		IQmlBinding b = prop.getBinding();
		if (ensureNode(b, IQmlScriptBinding.class)) {
			IQmlScriptBinding sb = (IQmlScriptBinding) b;
			if (ensureNode(sb.getScript(), IJSExpression.class)) {
				try {
					Object value = QMLExpressionEvaluator.evaluateConstExpr((IJSExpression) sb.getScript());
					if (value instanceof Number) {
						return (Boolean) value;
					}
				} catch (InvalidExpressionException e) {
					handleException(e);
				}
			}
		}
		return false;
	}

	public Integer getIntegerBinding(IQmlPropertyBinding prop) {
		IQmlBinding b = prop.getBinding();
		if (ensureNode(b, IQmlScriptBinding.class)) {
			IQmlScriptBinding sb = (IQmlScriptBinding) b;
			if (ensureNode(sb.getScript(), IJSExpression.class)) {
				try {
					Object value = QMLExpressionEvaluator.evaluateConstExpr((IJSExpression) sb.getScript());
					if (value instanceof Number) {
						return ((Number) value).intValue();
					}
				} catch (InvalidExpressionException e) {
					handleException(e);
				}
			}
		}
		return 0;
	}

	public void handleException(Throwable t) {
		Activator.log("[QmlTypes] " + t.getMessage()); //$NON-NLS-1$
	}

}
