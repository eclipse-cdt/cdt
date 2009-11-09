/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

/**
 * Collects methods to store an argument list in the database
 */
public class PDOMCPPTemplateParameterMap {
	private static final int PARAMPOS_OFFSET= 0;
	private static final int TYPE_OFFSET= PARAMPOS_OFFSET + 4;
	private static final int VALUE_OFFSET= TYPE_OFFSET + Database.TYPE_SIZE;
	private static final int NODE_SIZE = VALUE_OFFSET + Database.PTR_SIZE;

	/**
	 * Stores the given template parameter map in the database.
	 * @return the record by which the arguments can be referenced.
	 */
	public static long putMap(PDOMNode parent, ICPPTemplateParameterMap map) throws CoreException {
		final PDOMLinkage linkage= parent.getLinkage();
		final Database db= linkage.getDB();
		Integer[] keys= map.getAllParameterPositions();
		final short len= (short) Math.min(keys.length, (Database.MAX_MALLOC_SIZE-2)/NODE_SIZE); 
		final long block= db.malloc(2+NODE_SIZE*len);
		long p= block;

		db.putShort(p, len); p+=2;
		for (int i=0; i<len; i++) {
			final Integer paramPos = keys[i];
			db.putInt(p + PARAMPOS_OFFSET, paramPos); 
			final ICPPTemplateArgument arg = map.getArgument(paramPos);
			if (arg.isNonTypeValue()) {
				linkage.storeType(p + TYPE_OFFSET, arg.getTypeOfNonTypeValue());
				db.putRecPtr(p+VALUE_OFFSET, PDOMValue.store(db, linkage, arg.getNonTypeValue())); 
			} else {
				linkage.storeType(p + TYPE_OFFSET, arg.getTypeValue());
			}
			p+=NODE_SIZE; 
		}
		return block;
	}


	/**
	 * Clears the map in the database.
	 */
	public static void clearMap(PDOMNode parent, int rec) throws CoreException {
		final PDOMLinkage linkage= parent.getLinkage();
		final Database db= linkage.getDB();
		final short len= db.getShort(rec);
		
		Assert.isTrue(len >= 0 && len <= (Database.MAX_MALLOC_SIZE-2)/NODE_SIZE);
		rec+=2;
		for (int i=0; i<len; i++) {
			linkage.storeType(rec+TYPE_OFFSET, null);
			PDOMValue.delete(db, db.getRecPtr(rec+VALUE_OFFSET));
			rec+= NODE_SIZE;
		}
		db.free(rec);
	}

	/**
	 * Restores the map from from the database.
	 */
	public static CPPTemplateParameterMap getMap(PDOMNode parent, long rec) throws CoreException {
		final PDOMLinkage linkage= parent.getLinkage();
		final Database db= linkage.getDB();
		final short len= db.getShort(rec);
		
		Assert.isTrue(len >= 0 && len <= (Database.MAX_MALLOC_SIZE-2)/NODE_SIZE);
		if (len == 0) {
			return CPPTemplateParameterMap.EMPTY;
		}
		
		rec+=2;
		CPPTemplateParameterMap result= new CPPTemplateParameterMap(len);
		for (int i=0; i<len; i++) {
			final int parPos= db.getInt(rec + PARAMPOS_OFFSET);
			IType type= linkage.loadType(rec + TYPE_OFFSET);
			if (type == null) {
				type= new CPPBasicType(Kind.eUnspecified, CPPBasicType.UNIQUE_TYPE_QUALIFIER);
			}
			final long nonTypeValRec= db.getRecPtr(rec+VALUE_OFFSET); 
			ICPPTemplateArgument arg;
			if (nonTypeValRec != 0) {
				IValue val= PDOMValue.restore(db, linkage, nonTypeValRec);
				arg= new CPPTemplateArgument(val, type);
			} else {
				arg= new CPPTemplateArgument(type);
			}
			result.put(parPos, arg);
			rec+= NODE_SIZE;
		}
		return result;
	}
}
