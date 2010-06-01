package org.eclipse.cdt.codan.core.model.cfg;

/**
 * Has one incoming, one outgoing connection. Usually expression statement or
 * declaration.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPlainNode extends IBasicBlock, ISingleOutgoing,
		ISingleIncoming {
}
