/*
 * Created on Aug 25, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.internal.core.model;

/**
 * @author bgheorgh
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface IDebugLogConstants {
	public class DebugLogConstant {
			private DebugLogConstant( int value )
			{
				this.value = value;
			}
			private final int value;
		}
		
	public static final DebugLogConstant PARSER = new DebugLogConstant( 1 );
	public static final DebugLogConstant MODEL = new DebugLogConstant ( 2 );

}
