package org.eclipse.cdt.codan.core.model.cfg;

/**
 * 
 * Interface for decision node. This node represent condition node in the graph,
 * it has one incoming arc and many outgoing, each of outgoing node should be
 * IBranchNode
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IDecisionNode extends IBasicBlock, ISingleIncoming {
	/**
	 * Node where branches of decision node merge
	 * 
	 * @return the "merge" node
	 */
	IConnectorNode getMergeNode();
}
