package org.eclipse.cdt.codan.core.model.cfg;

public interface IJumpNode extends IBasicBlock, ISingleOutgoing {
	boolean isBackwardArc();

	IConnectorNode getJumpNode();
}
