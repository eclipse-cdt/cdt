package org.eclipse.cdt.core.parser;
import java.util.List;
/**
 * @author jcamelon
 *
 * To change this generated comment edit the template variable 
"typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface IMacroDescriptor {
	void initialize(String name, List identifiers, List tokens, String sig);
	List getParameters();
	List getTokenizedExpansion();
	String getName();
	String getSignature();
	boolean compatible(IMacroDescriptor descriptor);
}