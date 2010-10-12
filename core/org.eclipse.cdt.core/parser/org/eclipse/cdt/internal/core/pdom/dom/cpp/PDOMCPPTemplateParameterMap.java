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

import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMValue;
import org.eclipse.core.runtime.CoreException;

/**
 * Collects methods to store an argument list in the database
 */
public class PDOMCPPTemplateParameterMap {
	private static final int TYPE_OFFSET= 0;
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
		int keyLen= 0;
		int dataSize= 2;
		for (Integer key : keys) {
			int delta= 2+4+NODE_SIZE;
			ICPPTemplateArgument[] packExpansion= map.getPackExpansion(key);
			if (packExpansion != null) {
				delta+= (packExpansion.length-1) * NODE_SIZE;
			}
			if (dataSize+delta > Database.MAX_MALLOC_SIZE)
				break;
			dataSize += delta;
			keyLen++;
		}
		final long block= db.malloc(dataSize);
		long p= block;
		db.putShort(p, (short)keyLen); p+=2;
		for (final Integer paramId : keys) {
			if (--keyLen < 0)
				break;
			db.putInt(p, paramId); p+=4; 
			final ICPPTemplateArgument arg = map.getArgument(paramId);
			if (arg != null) {
				db.putShort(p, (short) -1); p+=2;
				storeArgument(db, linkage, p, arg); p+= NODE_SIZE;
			} else {
				final ICPPTemplateArgument[] args = map.getPackExpansion(paramId);
				db.putShort(p, (short) args.length); p+=2;
				for (ICPPTemplateArgument a : args) {
					storeArgument(db, linkage, p, a); p+= NODE_SIZE;
				}
			}
		}
		assert p == block+dataSize;
		return block;
	}

	private static void storeArgument(final Database db, final PDOMLinkage linkage, long p,
			final ICPPTemplateArgument arg) throws CoreException {
		if (arg.isNonTypeValue()) {
			linkage.storeType(p + TYPE_OFFSET, arg.getTypeOfNonTypeValue());
			db.putRecPtr(p+VALUE_OFFSET, PDOMValue.store(db, linkage, arg.getNonTypeValue())); 
		} else {
			linkage.storeType(p + TYPE_OFFSET, arg.getTypeValue());
		}
	}

	/**
	 * Clears the map in the database.
	 */
	public static void clearMap(PDOMNode parent, final int record) throws CoreException {
		final PDOMLinkage linkage= parent.getLinkage();
		final Database db= linkage.getDB();
		
		long p= record;
		final short len= db.getShort(p); p+= 2;
		
		for (int i=0; i<len; i++) {
			p+= 4; // parameter id
			short packSize= db.getShort(p); p+= 2;
			if (packSize == -1) 
				packSize= 1;
			for (int j = 0; j < packSize; j++) {
				linkage.storeType(p+TYPE_OFFSET, null);
				PDOMValue.delete(db, db.getRecPtr(p+VALUE_OFFSET));
				p+= NODE_SIZE;
			}
		}
		db.free(record);
	}

	/**
	 * Restores the map from from the database.
	 */
	public static CPPTemplateParameterMap getMap(PDOMNode parent, long rec) throws CoreException {
		final PDOMLinkage linkage= parent.getLinkage();
		final Database db= linkage.getDB();
		final short len= db.getShort(rec);
		
		if (len == 0) {
			return CPPTemplateParameterMap.EMPTY;
		}
		
		rec+=2;
		CPPTemplateParameterMap result= new CPPTemplateParameterMap(len);
		for (int i=0; i<len; i++) {
			final int parPos= db.getInt(rec); rec+= 4;
			short packSize= db.getShort(rec); rec+= 2;
			if (packSize == -1) {
				ICPPTemplateArgument arg = readArgument(rec, linkage, db); rec+= NODE_SIZE;
				result.put(parPos, arg);
			} else {
				ICPPTemplateArgument[] packExpansion= new ICPPTemplateArgument[packSize];
				for (int j = 0; j < packExpansion.length; j++) {
					packExpansion[j]= readArgument(rec, linkage, db); rec+= NODE_SIZE;
				}
				result.put(parPos, packExpansion);
			}
		}
		return result;
	}


	private static ICPPTemplateArgument readArgument(long rec, final PDOMLinkage linkage, final Database db)
			throws CoreException {
		IType type= linkage.loadType(rec + TYPE_OFFSET);
		if (type == null) {
			type= new ProblemType(ISemanticProblem.TYPE_NOT_PERSISTED);
		}
		final long nonTypeValRec= db.getRecPtr(rec+VALUE_OFFSET); 
		ICPPTemplateArgument arg;
		if (nonTypeValRec != 0) {
			IValue val= PDOMValue.restore(db, linkage, nonTypeValRec);
			arg= new CPPTemplateArgument(val, type);
		} else {
			arg= new CPPTemplateArgument(type);
		}
		return arg;
	}
}
