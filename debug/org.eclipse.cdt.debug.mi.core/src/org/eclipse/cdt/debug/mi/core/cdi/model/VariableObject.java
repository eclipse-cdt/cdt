/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFunctionType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIVoidType;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SourceManager;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.IncompleteType;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.Type;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIDataEvaluateExpression;
import org.eclipse.cdt.debug.mi.core.command.MIWhatis;
import org.eclipse.cdt.debug.mi.core.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.debug.mi.core.output.MIWhatisInfo;

/**
 */
public class VariableObject extends CObject implements ICDIVariableObject {

	// Casting info.
	public String casting_type;
	public int casting_index;
	public int casting_length;

	Type type = null;
	String typename = null;
	String sizeof = null;

	String name;
	int position;
	ICDIStackFrame frame;
	int stackdepth;

	public VariableObject(VariableObject obj, String n) {
		super(obj.getTarget());
		name = n;
		try {
			frame = obj.getStackFrame();
		} catch (CDIException e) {
		}
		position = obj.getPosition();
		stackdepth = obj.getStackDepth();
	}

	public VariableObject(ICDITarget target, String n, ICDIStackFrame stack, int pos, int depth) {
		super(target);
		name = n;
		frame = stack;
		position = pos;
		stackdepth = depth;
	}

	public int getPosition() {
		return position;
	}

	public int getStackDepth() {
		return stackdepth;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableObject#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getType()
	 */
	public ICDIType getType() throws CDIException {
		if (type == null) {
			ICDITarget target = getTarget();
			Session session = (Session) (target.getSession());
			SourceManager sourceMgr = (SourceManager) session.getSourceManager();
			String typename = getTypeName();
			try {
				type = sourceMgr.getType(target, typename);
			} catch (CDIException e) {
				// Try with ptype.
				try {
					String ptype = sourceMgr.getDetailTypeName(typename);
					type = sourceMgr.getType(target, ptype);
				} catch (CDIException ex) {
				}
			}
			if (type == null) {
				type = new IncompleteType(target, typename);
			}
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#sizeof()
	 */
	public int sizeof() throws CDIException {
		if (sizeof == null) {
			ICDITarget target = getTarget();
			Session session = (Session) (target.getSession());
			MISession mi = session.getMISession();
			CommandFactory factory = mi.getCommandFactory();
			String exp = "sizeof(" + getTypeName() + ")";
			MIDataEvaluateExpression evaluate = factory.createMIDataEvaluateExpression(exp);
			try {
				mi.postCommand(evaluate);
				MIDataEvaluateExpressionInfo info = evaluate.getMIDataEvaluateExpressionInfo();
				if (info == null) {
					throw new CDIException("Target is not responding");
				}
				sizeof = info.getExpression();
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
		}

		if (sizeof != null) {
			try {
				return Integer.parseInt(sizeof);
			} catch (NumberFormatException e) {
				throw new CDIException(e.getMessage());
			}
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#isEdiTable()
	 */
	public boolean isEditable() throws CDIException {
		ICDIType t = getType();
		if (t instanceof  ICDIArrayType ||
		    t instanceof ICDIStructType ||
		    t instanceof ICDIVoidType ||
		    t instanceof ICDIFunctionType) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getStackFrame()
	 */
	public ICDIStackFrame getStackFrame() throws CDIException {
		return frame;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getTypeName()
	 */
	public String getTypeName() throws CDIException {
		if (typename == null) {
			try {
				ICDITarget target = getTarget();
				Session session = (Session) (target.getSession());
				MISession mi = session.getMISession();
				CommandFactory factory = mi.getCommandFactory();
				MIWhatis whatis = factory.createMIWhatis(getName());
				mi.postCommand(whatis);
				MIWhatisInfo info = whatis.getMIWhatisInfo();
				if (info == null) {
					throw new CDIException("No answer");
				}
				typename = info.getType();
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
		}
		return typename;
	}

}
