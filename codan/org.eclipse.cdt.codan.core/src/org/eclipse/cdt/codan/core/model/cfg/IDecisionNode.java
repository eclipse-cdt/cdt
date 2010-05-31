package org.eclipse.cdt.codan.core.model.cfg;

/**
 * 
 * Interface for decision node. This node represent condition node in the graph,
 * it has one incoming arc and many outgoing, each of outgoing node should be
 * IBranchNode
 */
public interface IDecisionNode extends IBasicBlock, ISingleIncoming {
	/**
	 * Node where branches of decision node merge
	 * 
	 * @return the "merge" node
	 */
	IConnectorNode getMergeNode();
}
