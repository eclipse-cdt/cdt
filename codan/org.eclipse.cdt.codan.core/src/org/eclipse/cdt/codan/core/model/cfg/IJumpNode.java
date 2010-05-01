package org.eclipse.cdt.codan.core.model.cfg;

/**
 * Node that changes the control of the graph, i.e. passes control to non-next
 * statement. Can be used to implement gotos, break, continue, end of branches
 */
public interface IJumpNode extends IBasicBlock, ISingleOutgoing {
	/**
	 * True of outgoing arc is backward one
	 */
	boolean isBackwardArc();

	IConnectorNode getJumpNode();
}
