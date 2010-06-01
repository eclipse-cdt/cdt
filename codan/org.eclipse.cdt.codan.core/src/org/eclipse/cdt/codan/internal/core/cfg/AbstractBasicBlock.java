package org.eclipse.cdt.codan.internal.core.cfg;

import org.eclipse.cdt.codan.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.core.model.cfg.ICfgData;

/**
 * Abstract Basic Block for control flow graph.
 */
public abstract class AbstractBasicBlock implements IBasicBlock, ICfgData {
	/**
	 * Empty array of basic blocks
	 */
	public final static IBasicBlock[] EMPTY_LIST = new IBasicBlock[0];
	private Object data;

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * Add a node to list of outgoing nodes of this node
	 * 
	 * @param node - node to add
	 */
	public abstract void addOutgoing(IBasicBlock node);

	/**
	 * Add a node to list of incoming nodes of this node
	 * 
	 * @param node - node to add
	 */
	public abstract void addIncoming(IBasicBlock node);

	/**
	 * @return toString for data object
	 */
	public String toStringData() {
		if (getData() == null)
			return "0x" + Integer.toHexString(System.identityHashCode(this)); //$NON-NLS-1$
		return getData().toString();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + toStringData(); //$NON-NLS-1$
	}
}
