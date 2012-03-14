/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla <bmuskalla@eclipsesource.com> - [extract method] missing return type when code can throw exception - https://bugs.eclipse.org/bugs/show_bug.cgi?id=97413
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.code.flow;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IVariable;

import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;

public abstract class FlowInfo {
	// Return statement handling.
	protected static final int NOT_POSSIBLE=	0;
	protected static final int UNDEFINED= 		1;
	protected static final int NO_RETURN= 		2;
	protected static final int PARTIAL_RETURN= 	3;
	protected static final int VOID_RETURN= 	4;
	protected static final int VALUE_RETURN=	5;
	protected static final int THROW=			6;

	// Local access handling.
	public static final int READ= 				PDOMName.READ_ACCESS; // 1 << 9
	public static final int WRITE= 				PDOMName.WRITE_ACCESS; // 1 << 10
	public static final int UNUSED=				1 << 0;
	public static final int READ_POTENTIAL=		1 << 1;
	public static final int WRITE_POTENTIAL=    1 << 2;
	public static final int UNKNOWN= 			1 << 3;

	// Table to merge access modes for condition statements (e.g branch[x] || branch[y]).
	private static final int[][] ACCESS_MODE_CONDITIONAL_TABLE= {
	/*	  					  UNUSED		   READ			    READ_POTENTIAL   WRITE			  WRITE_POTENTIAL  UNKNOWN */
	/* UNUSED */			{ UNUSED,		   READ_POTENTIAL,  READ_POTENTIAL,  WRITE_POTENTIAL, WRITE_POTENTIAL, UNKNOWN },
	/* READ */				{ READ_POTENTIAL,  READ,			READ_POTENTIAL,  UNKNOWN,		  UNKNOWN,         UNKNOWN },
	/* READ_POTENTIAL */	{ READ_POTENTIAL,  READ_POTENTIAL,  READ_POTENTIAL,  UNKNOWN,		  UNKNOWN,         UNKNOWN },
	/* WRITE */				{ WRITE_POTENTIAL, UNKNOWN,			UNKNOWN,		 WRITE,			  WRITE_POTENTIAL, UNKNOWN },
	/* WRITE_POTENTIAL */   { WRITE_POTENTIAL, UNKNOWN,			UNKNOWN,		 WRITE_POTENTIAL, WRITE_POTENTIAL, UNKNOWN },
	/* UNKNOWN */ 			{ UNKNOWN, 		   UNKNOWN,			UNKNOWN,		 UNKNOWN, 		  UNKNOWN,		   UNKNOWN }
	};

	// Table to change access mode if there is an open branch statement
	private static final int[] ACCESS_MODE_OPEN_BRANCH_TABLE= {
	/*	UNUSED	READ			READ_POTENTIAL  WRITE				WRITE_POTENTIAL  UNKNOWN */
		UNUSED,	READ_POTENTIAL,	READ_POTENTIAL,	WRITE_POTENTIAL,	WRITE_POTENTIAL, UNKNOWN
	};

	// Table to merge return modes for condition statements (y: fReturnKind, x: other.fReturnKind)
	private static final int[][] RETURN_KIND_CONDITIONAL_TABLE = {
	/* 						  NOT_POSSIBLE		UNDEFINED		NO_RETURN		PARTIAL_RETURN	VOID_RETURN		VALUE_RETURN	THROW */
	/* NOT_POSSIBLE */		{ NOT_POSSIBLE,		NOT_POSSIBLE,	NOT_POSSIBLE, 	NOT_POSSIBLE,	NOT_POSSIBLE,	NOT_POSSIBLE,	NOT_POSSIBLE	},
	/* UNDEFINED */			{ NOT_POSSIBLE,		UNDEFINED,		NO_RETURN,		PARTIAL_RETURN, VOID_RETURN,	VALUE_RETURN,	THROW 			},
	/* NO_RETURN */			{ NOT_POSSIBLE,		NO_RETURN,		NO_RETURN,		PARTIAL_RETURN,	PARTIAL_RETURN, PARTIAL_RETURN, NO_RETURN 		},
	/* PARTIAL_RETURN */	{ NOT_POSSIBLE,		PARTIAL_RETURN,	PARTIAL_RETURN,	PARTIAL_RETURN, PARTIAL_RETURN, PARTIAL_RETURN, PARTIAL_RETURN	},
	/* VOID_RETURN */		{ NOT_POSSIBLE,		VOID_RETURN,	PARTIAL_RETURN,	PARTIAL_RETURN, VOID_RETURN,	NOT_POSSIBLE,	VOID_RETURN		},
	/* VALUE_RETURN */		{ NOT_POSSIBLE,		VALUE_RETURN,	PARTIAL_RETURN, PARTIAL_RETURN, NOT_POSSIBLE,	VALUE_RETURN,	VALUE_RETURN	},
	/* THROW */				{ NOT_POSSIBLE,		THROW,			NO_RETURN,		PARTIAL_RETURN, VOID_RETURN,	VALUE_RETURN,	THROW			}
	};

	// Table to merge return modes for sequential statements (y: fReturnKind, x: other.fReturnKind)
	private static final int[][] RETURN_KIND_SEQUENTIAL_TABLE = {
	/* 						  NOT_POSSIBLE		UNDEFINED		NO_RETURN		PARTIAL_RETURN	VOID_RETURN		VALUE_RETURN	THROW */
	/* NOT_POSSIBLE */		{ NOT_POSSIBLE,		NOT_POSSIBLE,	NOT_POSSIBLE, 	NOT_POSSIBLE,	NOT_POSSIBLE,	NOT_POSSIBLE,	NOT_POSSIBLE	},
	/* UNDEFINED */			{ NOT_POSSIBLE,		UNDEFINED,		NO_RETURN,		PARTIAL_RETURN,	VOID_RETURN,	VALUE_RETURN,	THROW			},
	/* NO_RETURN */			{ NOT_POSSIBLE,		NO_RETURN,		NO_RETURN,		PARTIAL_RETURN,	VOID_RETURN,	VALUE_RETURN,	THROW			},
	/* PARTIAL_RETURN */	{ NOT_POSSIBLE,		PARTIAL_RETURN,	PARTIAL_RETURN,	PARTIAL_RETURN,	VOID_RETURN,	VALUE_RETURN,	VALUE_RETURN	},
	/* VOID_RETURN */		{ NOT_POSSIBLE,		VOID_RETURN,	VOID_RETURN,	PARTIAL_RETURN,	VOID_RETURN,	NOT_POSSIBLE,	NOT_POSSIBLE	},
	/* VALUE_RETURN */		{ NOT_POSSIBLE,		VALUE_RETURN,	VALUE_RETURN,	PARTIAL_RETURN,	NOT_POSSIBLE,	VALUE_RETURN,	NOT_POSSIBLE	},
	/* THROW */				{ NOT_POSSIBLE,		THROW,			THROW,			VALUE_RETURN,	VOID_RETURN,	VALUE_RETURN,	THROW			}
	};

	protected static final String UNLABELED = "@unlabeled"; //$NON-NLS-1$

	protected int fReturnKind;
	protected int[] fAccessModes;
	protected Set<String> fBranches;

	protected FlowInfo() {
		this(UNDEFINED);
	}

	protected FlowInfo(int returnKind) {
		fReturnKind= returnKind;
	}

	//---- General Helpers ----------------------------------------------------------

	protected void assignExecutionFlow(FlowInfo right) {
		fReturnKind= right.fReturnKind;
		fBranches= right.fBranches;
	}

	protected void assignAccessMode(FlowInfo right) {
		fAccessModes= right.fAccessModes;
	}

	protected void assign(FlowInfo right) {
		assignExecutionFlow(right);
		assignAccessMode(right);
	}

	protected void mergeConditional(FlowInfo info, FlowContext context) {
		mergeAccessModeConditional(info, context);
		mergeExecutionFlowConditional(info);
	}

	protected void mergeSequential(FlowInfo info, FlowContext context) {
		mergeAccessModeSequential(info, context);
		mergeExecutionFlowSequential(info);
	}

	//---- Return Kind ------------------------------------------------------------------

	public void setNoReturn() {
		fReturnKind= NO_RETURN;
	}

	public boolean isUndefined() {
		return fReturnKind == UNDEFINED;
	}

	public boolean isNoReturn() {
		return fReturnKind == NO_RETURN;
	}

	public boolean isPartialReturn() {
		return fReturnKind == PARTIAL_RETURN;
	}

	public boolean isVoidReturn() {
		return fReturnKind == VOID_RETURN;
	}

	public boolean isValueReturn() {
		return fReturnKind == VALUE_RETURN;
	}

	public boolean isThrow() {
		return fReturnKind == THROW;
	}

	public boolean isReturn() {
		return fReturnKind == VOID_RETURN || fReturnKind == VALUE_RETURN;
	}

	//---- Branches -------------------------------------------------------------------------

	public boolean branches() {
		return fBranches != null && !fBranches.isEmpty();
	}

	protected Set<String> getBranches() {
		return fBranches;
	}

	protected void removeLabel(IASTName label) {
		if (fBranches != null) {
			fBranches.remove(makeString(label));
			if (fBranches.isEmpty())
				fBranches= null;
		}
	}

	protected static String makeString(IASTName label) {
		if (label == null) {
			return UNLABELED;
		} else {
			return String.valueOf(label.getSimpleID());
		}
	}

	//---- Execution flow -------------------------------------------------------------------

	private void mergeExecutionFlowSequential(FlowInfo otherInfo) {
		int other= otherInfo.fReturnKind;
		if (branches() && other == VALUE_RETURN)
			other= PARTIAL_RETURN;
		fReturnKind= RETURN_KIND_SEQUENTIAL_TABLE[fReturnKind][other];
		mergeBranches(otherInfo);
	}

	private void mergeExecutionFlowConditional(FlowInfo otherInfo) {
		fReturnKind= RETURN_KIND_CONDITIONAL_TABLE[fReturnKind][otherInfo.fReturnKind];
		mergeBranches(otherInfo);
	}

	private void mergeBranches(FlowInfo otherInfo) {
		fBranches= mergeSets(fBranches, otherInfo.fBranches);
	}

	private static <T> Set<T> mergeSets(Set<T> thisSet, Set<T> otherSet) {
		if (otherSet != null) {
			if (thisSet == null) {
				thisSet= otherSet;
			} else {
				for (T element : otherSet) {
					thisSet.add(element);
				}
			}
		}
		return thisSet;
	}

	//---- Local access handling --------------------------------------------------

	/**
	 * Returns a set of <code>IVariable</code> that conform to the given
	 * access mode <code>mode</code>.
	 *
	 * @param context the flow context object used to compute this flow info
	 * @param mode the access type. Valid values are <code>READ</code>, <code>WRITE</code>,
	 *     <code>UNKNOWN</code> and any combination of them.
	 * @return an array of local variable bindings conforming to the given type.
	 */
	public Set<IVariable> get(FlowContext context, int mode) {
		if (fAccessModes == null)
			return Collections.emptySet();
		Set<IVariable> result= new HashSet<IVariable>(fAccessModes.length);
		for (int i= 0; i < fAccessModes.length; i++) {
			int accessMode= fAccessModes[i];
			if ((accessMode & mode) != 0)
				result.add(context.getLocalFromIndex(i));
		}
		return result;
	}

	/**
	 * Checks whether the given local variable binding has the given access
	 * mode.
	 *
	 * @param context the flow context used during flow analysis
	 * @param local local variable of interest
	 * @param mode the access mode of the local variable
	 *
	 * @return <code>true</code> if the binding has the given access mode.
	 * 	<code>False</code> otherwise
	 */
	public boolean hasAccessMode(FlowContext context, IVariable local, int mode) {
		boolean unusedMode= (mode & UNUSED) != 0;
		if (fAccessModes == null && unusedMode)
			return true;
		int index= context.getIndexFromLocal(local);
		if (index == -1)
			return unusedMode;
		return (fAccessModes[index] & mode) != 0;
	}

	/**
	 * Returns the access mode of the local variable identified by the given binding.
	 *
	 * @param context the flow context used during flow analysis
	 * @param local the local variable of interest
	 * @return the access mode of the local variable
	 */
	public int getAccessMode(FlowContext context, IVariable local) {
		if (fAccessModes == null)
			return UNUSED;
		int index= context.getIndexFromLocal(local);
		if (index == -1)
			return UNUSED;
		return fAccessModes[index];
	}

	protected int[] getAccessModes() {
		return fAccessModes;
	}

	protected void clearAccessMode(IVariable binding, FlowContext context) {
		if (fAccessModes == null)	// All are unused
			return;
		int index = context.getIndexFromLocal(binding);
		if (index >= 0)
			fAccessModes[index]= UNUSED;
	}

	protected void mergeAccessModeSequential(FlowInfo otherInfo, FlowContext context) {
		if (!context.considerAccessMode())
			return;

		int[] others= otherInfo.fAccessModes;
		if (others == null)	// others are all unused. So nothing to do
			return;

		// Must not consider return kind since a return statement can't control execution flow
		// inside a method. It always leaves the method.
		if (branches()) {
			for (int i= 0; i < others.length; i++)
				others[i]= ACCESS_MODE_OPEN_BRANCH_TABLE[getIndex(others[i])];
		}

		if (fAccessModes == null) {	// all current variables are unused
			fAccessModes= others;
			return;
		}

		if (context.computeArguments()) {
			handleComputeArguments(others);
		} else if (context.computeReturnValues()) {
			handleComputeReturnValues(others);
		} else if (context.computeMerge()) {
			handleMergeValues(others);
		}
	}

	private void handleComputeReturnValues(int[] others) {
		for (int i= 0; i < fAccessModes.length; i++) {
			int accessmode= fAccessModes[i];
			int othermode= others[i];
			if (accessmode == WRITE)
				continue;
			if (accessmode == WRITE_POTENTIAL) {
				if (othermode == WRITE)
					fAccessModes[i]= WRITE;
				continue;
			}

			if (others[i] != UNUSED)
				fAccessModes[i]= othermode;
		}
	}

	private void handleComputeArguments(int[] others) {
		for (int i= 0; i < fAccessModes.length; i++) {
			int accessMode= fAccessModes[i];
			int otherMode= others[i];
			if (accessMode == UNUSED) {
				fAccessModes[i]= otherMode;
			} else if (accessMode == WRITE_POTENTIAL && (otherMode == READ || otherMode == READ_POTENTIAL)) {
				// Read always supersedes a potential write even if the read is potential as well
				// (we have to consider the potential read as an argument then).
				fAccessModes[i]= otherMode;
			} else if (accessMode == WRITE_POTENTIAL && otherMode == WRITE) {
				fAccessModes[i]= WRITE;
			}
		}
	}

	private void handleMergeValues(int[] others) {
		for (int i= 0; i < fAccessModes.length; i++) {
			fAccessModes[i]= ACCESS_MODE_CONDITIONAL_TABLE[getIndex(fAccessModes[i])][getIndex(others[i])];
		}
	}

	protected void createAccessModeArray(FlowContext context) {
		fAccessModes= new int[context.getArrayLength()];
		for (int i= 0; i < fAccessModes.length; i++) {
			fAccessModes[i]= UNUSED;
		}
	}

	protected void mergeAccessModeConditional(FlowInfo otherInfo, FlowContext context) {
		if (!context.considerAccessMode())
			return;

		int[] others= otherInfo.fAccessModes;
		// first access
		if (fAccessModes == null) {
			if (others != null) {
				fAccessModes= others;
			} else {
				createAccessModeArray(context);
			}
			return;
		} else {
			if (others == null) {
				for (int i= 0; i < fAccessModes.length; i++) {
					int unused_index= getIndex(UNUSED);
					fAccessModes[i]= ACCESS_MODE_CONDITIONAL_TABLE[getIndex(fAccessModes[i])][unused_index];
				}
			} else {
				for (int i= 0; i < fAccessModes.length; i++) {
					fAccessModes[i]= ACCESS_MODE_CONDITIONAL_TABLE[getIndex(fAccessModes[i])][getIndex(others[i])];
				}
			}
		}
	}

	protected void mergeEmptyCondition(FlowContext context) {
		if (fReturnKind == VALUE_RETURN || fReturnKind == VOID_RETURN)
			fReturnKind= PARTIAL_RETURN;

		if (!context.considerAccessMode())
			return;

		if (fAccessModes == null) {
			createAccessModeArray(context);
			return;
		}

		int unused_index= getIndex(UNUSED);
		for (int i= 0; i < fAccessModes.length; i++) {
			fAccessModes[i]= ACCESS_MODE_CONDITIONAL_TABLE[getIndex(fAccessModes[i])][unused_index];
		}
	}

	private static int getIndex(int accessMode) {
		 // Fast log function
		 switch (accessMode) {
		 	case UNUSED:
		 		return 0;
		 	case READ:
		 		return 1;
		 	case READ_POTENTIAL:
		 		return 2;
		 	case WRITE:
		 	case READ | WRITE:
		 		return 3;
		 	case WRITE_POTENTIAL:
		 		return 4;
		 	case UNKNOWN:
		 		return 5;
		 }
		 return -1;
	}
}


