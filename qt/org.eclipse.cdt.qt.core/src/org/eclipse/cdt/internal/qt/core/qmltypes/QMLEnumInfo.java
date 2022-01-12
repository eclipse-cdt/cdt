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
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.qt.core.qmljs.IJSObjectExpression;
import org.eclipse.cdt.qt.core.qmljs.IJSProperty;
import org.eclipse.cdt.qt.core.qmljs.IQmlObjectDefinition;
import org.eclipse.cdt.qt.core.qmljs.IQmlObjectMember;
import org.eclipse.cdt.qt.core.qmljs.IQmlPropertyBinding;
import org.eclipse.cdt.qt.core.qmljs.IQmlScriptBinding;
import org.eclipse.cdt.qt.core.qmljs.QMLExpressionEvaluator;
import org.eclipse.cdt.qt.core.qmljs.QMLExpressionEvaluator.InvalidExpressionException;

public class QMLEnumInfo {
	public static class EnumConst {
		private final String identifier;
		private final int value;

		private EnumConst(String ident, int val) {
			this.identifier = ident;
			this.value = val;
		}

		public String getIdentifier() {
			return identifier;
		}

		public int getValue() {
			return value;
		}
	}

	static final String IDENTIFIER = "Enum"; //$NON-NLS-1$

	static final String PROPERTY_NAME = "name"; //$NON-NLS-1$
	static final String PROPERTY_VALUE = "values"; //$NON-NLS-1$

	private String name;
	private List<EnumConst> constantList = new ArrayList<>();

	QMLEnumInfo(QMLModelBuilder builder, IQmlObjectDefinition obj) {
		if (builder.ensureIdentifier(obj.getIdentifier(), IDENTIFIER)) {
			for (IQmlObjectMember member : obj.getBody().getMembers()) {
				if (builder.ensureNode(member, IQmlPropertyBinding.class)) {
					IQmlPropertyBinding prop = (IQmlPropertyBinding) member;
					switch (prop.getIdentifier().getName()) {
					case PROPERTY_NAME:
						this.name = builder.getStringBinding(prop);
						break;
					case PROPERTY_VALUE:
						if (builder.ensureNode(prop.getBinding(), IQmlScriptBinding.class)) {
							IQmlScriptBinding binding = ((IQmlScriptBinding) prop.getBinding());
							if (builder.ensureNode(binding.getScript(), IJSObjectExpression.class)) {
								IJSObjectExpression objExpr = (IJSObjectExpression) binding.getScript();
								for (IJSProperty property : objExpr.getProperties()) {
									Object value;
									try {
										value = QMLExpressionEvaluator.evaluateConstExpr(property.getValue());
										if (value instanceof Number) {
											constantList.add(
													new EnumConst(property.getType(), ((Number) value).intValue()));
										}
									} catch (InvalidExpressionException e) {
										builder.handleException(e);
									}
								}
							}
						}
						break;
					default:
					}
				}
			}
		}
		constantList = Collections.unmodifiableList(constantList);
	}

	public String getName() {
		return name;
	}

	public List<EnumConst> getConstants() {
		return constantList;
	}
}
