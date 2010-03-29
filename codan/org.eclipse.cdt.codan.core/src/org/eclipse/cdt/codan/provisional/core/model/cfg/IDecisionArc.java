package org.eclipse.cdt.codan.provisional.core.model.cfg;

/**
 * Arc that has a condition upon it, for example if branches have true/false
 * condition, and switch branches have case condition
 */
public interface IDecisionArc extends ISingleOutgoing {
	/**
	 * Index represent branch index in decision node, as well semantics: <br>
	 * 0 - always a false (else) branch of if, and default branch for switch,
	 * even if no code exists <br>
	 * 1 - is true (then) branch for if
	 * 
	 * @return
	 */
	int getIndex();

	/**
	 * Basic block that is at the end of the arc
	 * 
	 * @return
	 */
	IBasicBlock getOutgoing();

	/**
	 * Return parent node (decision node) or the decision arc
	 * 
	 * @return
	 */
	IDecisionNode getDecisionNode();
}
