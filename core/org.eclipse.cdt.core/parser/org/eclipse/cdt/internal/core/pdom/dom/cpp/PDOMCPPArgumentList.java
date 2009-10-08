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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateArgument;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

/**
 * Collects methods to store an argument list in the database
 */
public class PDOMCPPArgumentList {
	/**
	 * Stores the given template arguments in the database.
	 * @return the record by which the arguments can be referenced.
	 */
	public static long putArguments(PDOMNode parent, ICPPTemplateArgument[] templateArguments) throws CoreException {
		final PDOMLinkage linkage= parent.getLinkage();
		final Database db= linkage.getDB();
		final short len= (short) Math.min(templateArguments.length, (Database.MAX_MALLOC_SIZE-2)/8); 
		final long block= db.malloc(2+8*len);
		long p= block;

		db.putShort(p, len); p+=2;
		for (int i=0; i<len; i++, p+=8) {
			final ICPPTemplateArgument arg = templateArguments[i];
			final boolean isNonType= arg.isNonTypeValue();
			if (isNonType) {
				final PDOMNode type= linkage.addType(parent, arg.getTypeOfNonTypeValue());
				// type can be null, if it is a local type
				db.putRecPtr(p, type == null ? 0 : type.getRecord()); 
				long valueRec= PDOMValue.store(db, linkage, arg.getNonTypeValue());
				db.putRecPtr(p+4, valueRec); 
			} else {
				final PDOMNode type= linkage.addType(parent, arg.getTypeValue());
				// type can be null, if it is a local type.
				db.putRecPtr(p, type == null ? 0 : type.getRecord()); 
			}
		}
		return block;
	}


	/**
	 * Restores an array of template arguments from the database.
	 */
	public static void clearArguments(PDOMNode parent, final long record) throws CoreException {
		final PDOMLinkage linkage= parent.getLinkage();
		final Database db= linkage.getDB();
		final short len= db.getShort(record);
		
		Assert.isTrue(len >= 0 && len <= (Database.MAX_MALLOC_SIZE-2)/8);
		long p= record+2;
		for (int i=0; i<len; i++) {
			final long typeRec= db.getRecPtr(p);
			if (typeRec != 0) {
				final IType t= (IType) linkage.getNode(typeRec);
				linkage.deleteType(t, parent.getRecord());
			}			
			final long nonTypeValueRec= db.getRecPtr(p+4);
			PDOMValue.delete(db, nonTypeValueRec);
			p+= 8;
		}
		db.free(record);
	}

	/**
	 * Restores an array of template arguments from the database.
	 */
	public static ICPPTemplateArgument[] getArguments(PDOMNode parent, long rec) throws CoreException {
		final PDOMLinkage linkage= parent.getLinkage();
		final Database db= linkage.getDB();
		final short len= db.getShort(rec);
		
		Assert.isTrue(len >= 0 && len <= (Database.MAX_MALLOC_SIZE-2)/8);
		if (len == 0) {
			return ICPPTemplateArgument.EMPTY_ARGUMENTS;
		}
		
		rec+=2;
		ICPPTemplateArgument[] result= new ICPPTemplateArgument[len];
		for (int i=0; i<len; i++) {
			final long typeRec= db.getRecPtr(rec);
			final IType type= typeRec == 0 ? new CPPBasicType(Kind.eUnspecified,CPPBasicType.UNIQUE_TYPE_QUALIFIER) : (IType) linkage.getNode(typeRec);
			final long nonTypeValRec= db.getRecPtr(rec+4); 
			if (nonTypeValRec != 0) {
				final IValue val= PDOMValue.restore(db, linkage, nonTypeValRec);
				result[i]= new CPPTemplateArgument(val, type);
			} else {
				result[i]= new CPPTemplateArgument(type);
			}
			rec+= 8;
		}
		return result;
	}
}
