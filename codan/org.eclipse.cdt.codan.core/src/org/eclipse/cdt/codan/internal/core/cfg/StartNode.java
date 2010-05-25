package org.eclipse.cdt.codan.internal.core.cfg;

import org.eclipse.cdt.codan.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.core.model.cfg.IStartNode;

/**
 * Start node has no incoming, one outgoing and it is connect to function exits
 * 
 */
public class StartNode extends AbstractSingleOutgoingNode implements IStartNode {
	protected StartNode() {
		super();
	}

	public IBasicBlock[] getIncomingNodes() {
		return EMPTY_LIST;
	}

	public int getIncomingSize() {
		return 0;
	}

	@Override
	public void addOutgoing(IBasicBlock node) {
		setOutgoing(node);
	}

	@Override
	public void addIncoming(IBasicBlock node) {
		throw new UnsupportedOperationException();
	}
}
