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
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
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
	private static final int VALUE_OFFSET= Database.TYPE_SIZE;
	private static final int NODE_SIZE = VALUE_OFFSET + Database.PTR_SIZE;
	
	/**
	 * Stores the given template arguments in the database.
	 * @return the record by which the arguments can be referenced.
	 */
	public static long putArguments(PDOMNode parent, ICPPTemplateArgument[] templateArguments) throws CoreException {
		final PDOMLinkage linkage= parent.getLinkage();
		final Database db= linkage.getDB();
		final short len= (short) Math.min(templateArguments.length, (Database.MAX_MALLOC_SIZE-2)/NODE_SIZE); 
		final long block= db.malloc(2+NODE_SIZE*len);
		long p= block;

		db.putShort(p, len); p+=2;
		for (int i=0; i<len; i++, p+=NODE_SIZE) {
			final ICPPTemplateArgument arg = templateArguments[i];
			final boolean isNonType= arg.isNonTypeValue();
			if (isNonType) {
				linkage.storeType(p, arg.getTypeOfNonTypeValue());
				long valueRec= PDOMValue.store(db, linkage, arg.getNonTypeValue());
				db.putRecPtr(p+VALUE_OFFSET, valueRec); 
			} else {
				linkage.storeType(p, arg.getTypeValue());
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
		
		Assert.isTrue(len >= 0 && len <= (Database.MAX_MALLOC_SIZE-2)/NODE_SIZE);
		long p= record+2;
		for (int i=0; i<len; i++) {
			linkage.storeType(p, null);
			final long nonTypeValueRec= db.getRecPtr(p+VALUE_OFFSET);
			PDOMValue.delete(db, nonTypeValueRec);
			p+= NODE_SIZE;
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
		
		Assert.isTrue(len >= 0 && len <= (Database.MAX_MALLOC_SIZE-2)/NODE_SIZE);
		if (len == 0) {
			return ICPPTemplateArgument.EMPTY_ARGUMENTS;
		}
		
		rec+=2;
		ICPPTemplateArgument[] result= new ICPPTemplateArgument[len];
		for (int i=0; i<len; i++) {
			IType type= linkage.loadType(rec);
			if (type == null) {
				type= new ProblemType(ISemanticProblem.TYPE_NOT_PERSISTED);
			}
			final long nonTypeValRec= db.getRecPtr(rec+VALUE_OFFSET); 
			if (nonTypeValRec != 0) {
				final IValue val= PDOMValue.restore(db, linkage, nonTypeValRec);
				result[i]= new CPPTemplateArgument(val, type);
			} else {
				result[i]= new CPPTemplateArgument(type);
			}
			rec+= NODE_SIZE;
		}
		return result;
	}
}
