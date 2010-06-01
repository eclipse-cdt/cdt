package org.eclipse.cdt.codan.core.model.cfg;

/**
 * Control flow graph's basic block node - super interface of all nodes.
 * It has set on incoming nodes and outgoing nodes.
 * <p/>
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBasicBlock {
	/**
	 * Incoming nodes - nodes that executed immediately before this one
	 * 
	 * @return array of incoming nodes, empty array of none
	 */
	IBasicBlock[] getIncomingNodes();

	/**
	 * Outgoing nodes - where control would be passed. Can be more than one if
	 * node is condition.
	 * 
	 * @return array of outgoing nodes, empty of none
	 */
	IBasicBlock[] getOutgoingNodes();

	/**
	 * @return size of array of incoming nodes
	 */
	int getIncomingSize();

	/**
	 * @return size of array of outgoing nodes
	 */
	int getOutgoingSize();
}
