package org.eclipse.cdt.codan.internal.core.cfg;

import org.eclipse.cdt.codan.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.core.model.cfg.IExitNode;
import org.eclipse.cdt.codan.core.model.cfg.IStartNode;

/**
 * Plain node has one prev one jump
 * 
 */
public class ExitNode extends AbstractSingleIncomingNode implements IExitNode {
	private IStartNode start;

	protected ExitNode() {
		super();
	}

	public IBasicBlock[] getOutgoingNodes() {
		return EMPTY_LIST;
	}

	public int getOutgoingSize() {
		return 0;
	}

	public IStartNode getStartNode() {
		return start;
	}

	public void setStartNode(IStartNode start) {
		this.start = start;
	}

	@Override
	public void addOutgoing(IBasicBlock node) {
		throw new UnsupportedOperationException();
	}
}
