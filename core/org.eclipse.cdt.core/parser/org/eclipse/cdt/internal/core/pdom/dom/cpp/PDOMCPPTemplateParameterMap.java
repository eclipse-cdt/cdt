/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateArgument;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

/**
 * Collects methods to store an argument list in the database
 */
public class PDOMCPPTemplateParameterMap {
	/**
	 * Stores the given template parameter map in the database.
	 * @return the record by which the arguments can be referenced.
	 */
	public static int putMap(PDOMNode parent, ICPPTemplateParameterMap map) throws CoreException {
		final PDOMLinkage linkage= parent.getLinkage();
		final Database db= linkage.getPDOM().getDB();
		Integer[] keys= map.getAllParameterPositions();
		final short len= (short) Math.min(keys.length, (Database.MAX_MALLOC_SIZE-2)/12); 
		final int block= db.malloc(2+12*len);
		int p= block;

		db.putShort(p, len); p+=2;
		for (int i=0; i<len; i++) {
			final Integer paramPos = keys[i];
			db.putInt(p, paramPos); 
			p+=4;
			final ICPPTemplateArgument arg = map.getArgument(paramPos);
			if (arg.isNonTypeValue()) {
				final PDOMNode type= linkage.addType(parent, arg.getTypeOfNonTypeValue());
				// type can be null, if it is local
				db.putInt(p, type == null ? 0 : type.getRecord());
				final IString s= db.newString(arg.getNonTypeValue().getCanonicalRepresentation());
				db.putInt(p+4, s.getRecord()); 
			} else {
				final PDOMNode type= linkage.addType(parent, arg.getTypeValue());
				// type can be null, if it is local
				db.putInt(p, type == null ? 0 : type.getRecord()); 
			}
			p+=8;
		}
		return block;
	}


	/**
	 * Clears the map in the database.
	 */
	public static void clearMap(PDOMNode parent, int rec) throws CoreException {
		final PDOMLinkage linkage= parent.getLinkage();
		final Database db= linkage.getPDOM().getDB();
		final short len= db.getShort(rec);
		
		Assert.isTrue(len >= 0 && len <= (Database.MAX_MALLOC_SIZE-2)/12);
		rec+=2;
		for (int i=0; i<len; i++) {
			rec+=4;
			final int typeRec= db.getInt(rec);
			if (typeRec != 0) {
				final IType t= (IType) linkage.getNode(typeRec);
				linkage.deleteType(t, parent.getRecord());
			}			
			final int nonTypeValueRec= db.getInt(rec+4);
			if (nonTypeValueRec != 0) {
				db.getString(nonTypeValueRec).delete();
			} 
			rec+= 8;
		}
		db.free(rec);
	}

	/**
	 * Restores the map from from the database.
	 */
	public static CPPTemplateParameterMap getMap(PDOMNode parent, int rec) throws CoreException {
		final PDOMLinkage linkage= parent.getLinkage();
		final Database db= linkage.getPDOM().getDB();
		final short len= db.getShort(rec);
		
		Assert.isTrue(len >= 0 && len <= (Database.MAX_MALLOC_SIZE-2)/12);
		if (len == 0) {
			return new CPPTemplateParameterMap();
		}
		
		rec+=2;
		CPPTemplateParameterMap result= new CPPTemplateParameterMap();
		for (int i=0; i<len; i++) {
			final int parPos= db.getInt(rec);
			final int typeRec= db.getInt(rec+4);
			final IType type= typeRec == 0 ? new CPPBasicType(-1, 0) : (IType) linkage.getNode(typeRec);
			final int nonTypeValRec= db.getInt(rec+8); 
			ICPPTemplateArgument arg;
			if (nonTypeValRec != 0) {
				final IString s= db.getString(nonTypeValRec);
				arg= new CPPTemplateArgument(Value.fromCanonicalRepresentation(s.getString()), type);
			} else {
				arg= new CPPTemplateArgument(type);
			}
			result.put(parPos, arg);
			rec+= 12;
		}
		return result;
	}
}
