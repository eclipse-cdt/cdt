package org.eclipse.cdt.codan.internal.core.cfg;

import org.eclipse.cdt.codan.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.core.model.cfg.ISingleIncoming;

/**
 * Abstrat node with one incoming arc
 * 
 */
public abstract class AbstractSingleIncomingNode extends AbstractBasicBlock
		implements ISingleIncoming {
	private IBasicBlock prev;

	public AbstractSingleIncomingNode() {
		super();
	}

	public IBasicBlock[] getIncomingNodes() {
		return new IBasicBlock[] { prev };
	}

	public int getIncomingSize() {
		return 1;
	}

	public IBasicBlock getIncoming() {
		return prev;
	}

	public void setIncoming(IBasicBlock prev) {
		this.prev = prev;
	}

	@Override
	public void addIncoming(IBasicBlock node) {
		setIncoming(node);
	}
}
