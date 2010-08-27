/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	
	private Map<String,IField> fields = new LinkedHashMap<String,IField>();
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
	
	public IField findField(String name) {
		return fields.get(name);
	}

	public IScope getCompositeScope() {
//		C99Scope scope = new C99CompositeTypeScope(this);
//		scope.setScopeName(nameNode);
//		return scope;
		return null;
	}

	public IField[] getFields() {
		if(fieldArray == null)
			fieldArray = fields.values().toArray(new IField[fields.size()]);
		return fieldArray;
	}
	
	public void setKey(int key) {
		this.key = key;
	}
	
	public int getKey() {
		return key;
	}


	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public char[] getNameCharArray() {
		if(name == null)
			return new char[0];
		
		return name.toCharArray();
	}


	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}
	
	public IType getType() {
		return this;
	}
	
	public boolean isSameType(IType type) {
		if(type == this)
			return true;
		if( type instanceof ITypedef)
            return type.isSameType( this );
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
			clone.fields = new LinkedHashMap<String,IField>();
			for(IField field : fields.values())
				clone.addField(field); // TODO not a deep clone
			return clone;
		} catch (CloneNotSupportedException e) {
			assert false;
			return null;
		}
	}

	public IScope getScope() {
		return scope;
	}

	public void setScope(IScope scope) {
		this.scope = scope;
	}

	public IBinding getOwner() {
		if (scope != null) {
			return CVisitor.findEnclosingFunction((IASTNode) scope.getScopeName()); // local or global
		}
		return null;
	}

	public boolean isAnonymous() {
		return name == null || name.length() == 0;
	}
}
