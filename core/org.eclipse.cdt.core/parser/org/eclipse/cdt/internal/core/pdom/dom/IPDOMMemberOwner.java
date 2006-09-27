package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface for PDOM entities that contain members
 */
public interface IPDOMMemberOwner {
	public void addMember(PDOMNode member) throws CoreException;
	public void accept(IPDOMVisitor visitor) throws CoreException;
}
