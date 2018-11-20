/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.core.runtime.PlatformObject;

@SuppressWarnings("restriction")
public class C99Structure extends PlatformObject implements IC99Binding, ICompositeType, ITypeable {

	private Map<String, IField> fields = new LinkedHashMap<>();
	private IField[] fieldArray = null;

	/** either t_union or t_structure defined in IASTCompositeTypeSpecifier */
	private int key;
	private String name;

	private IScope scope;

	public C99Structure() {
	}

	public C99Structure(String name) {
		this.name = name;
	}

	public C99Structure(int key) {
		this.key = key;
	}

	public C99Structure(String name, int key) {
		this.name = name;
		this.key = key;
	}

	public void addField(IField field) {
		fields.put(field.getName(), field);
		fieldArray = null;
	}

	@Override
	public IField findField(String name) {
		return fields.get(name);
	}

	@Override
	public IScope getCompositeScope() {
		//		C99Scope scope = new C99CompositeTypeScope(this);
		//		scope.setScopeName(nameNode);
		//		return scope;
		return null;
	}

	@Override
	public IField[] getFields() {
		if (fieldArray == null)
			fieldArray = fields.values().toArray(new IField[fields.size()]);
		return fieldArray;
	}

	public void setKey(int key) {
		this.key = key;
	}

	@Override
	public int getKey() {
		return key;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public char[] getNameCharArray() {
		if (name == null)
			return new char[0];

		return name.toCharArray();
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}

	@Override
	public IType getType() {
		return this;
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef)
			return type.isSameType(this);
		return false;
	}

	//private Map<String,IField> fields = new LinkedHashMap<String,IField>();
	//private IField[] fieldArray = null;

	//private IScope scope;

	@Override
	public C99Structure clone() {
		try {
			C99Structure clone = (C99Structure) super.clone();
			//clone.scope = (IScope) scope.clone(); TODO
			clone.fieldArray = null;
			clone.fields = new LinkedHashMap<>();
			for (IField field : fields.values())
				clone.addField(field); // TODO not a deep clone
			return clone;
		} catch (CloneNotSupportedException e) {
			assert false;
			return null;
		}
	}

	@Override
	public IScope getScope() {
		return scope;
	}

	@Override
	public void setScope(IScope scope) {
		this.scope = scope;
	}

	@Override
	public IBinding getOwner() {
		if (scope != null) {
			return CVisitor.findEnclosingFunction((IASTNode) scope.getScopeName()); // local or global
		}
		return null;
	}

	@Override
	public boolean isAnonymous() {
		return name == null || name.length() == 0;
	}
}
