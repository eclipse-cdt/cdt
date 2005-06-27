/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * import org.eclipse.cdt.core.parser.Enum;
 * /**
 * @author bgheorgh
 *
 * /
 *******************************************************************************/
public interface IDebugLogConstants {
	public class DebugLogConstant extends Enum {
			protected DebugLogConstant( int value )
			{
				super( value );
			}
			
		}
		
	public static final DebugLogConstant PARSER = new DebugLogConstant( 1 );
	public static final DebugLogConstant MODEL = new DebugLogConstant ( 2 );
	public static final DebugLogConstant SCANNER = new DebugLogConstant( 3 );
}
