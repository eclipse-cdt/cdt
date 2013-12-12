/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.qt.core;

import java.util.regex.Matcher;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.qt.core.QtKeywords;

public class QtSignalSlotReference
{
	public static enum Type
	{
		Signal( "sender",   "SIGNAL", "signal" ),
		Slot  ( "receiver", "SLOT",   "slot"   );

		public final String roleName;
		public final String macroName;
		public final String paramName;

		public boolean matches( Type other )
		{
			if( other == null )
				return false;

			// The signal parameter must be a SIGNAL, but the slot could be a
			// SLOT or a SIGNAL.
			return this == Signal ? other == Signal : true;
		}

		private Type( String roleName, String macroName, String paramName )
		{
			this.roleName = roleName;
			this.macroName = macroName;
			this.paramName = paramName;
		}
	}

	public final IASTNode parent;
	public final IASTNode node;
	public final Type type;
	public final String signature;
	public final int offset;
	public final int length;

	private QtSignalSlotReference( IASTNode parent, IASTNode node, Type type, String signature, int offset, int length )
	{
		this.parent = parent;
		this.node = node;
		this.type = type;
		this.signature = signature;
		this.offset = offset;
		this.length = length;
	}

	public IASTName createName( IBinding binding )
	{
		return new QtSignalSlotReferenceName( parent, node, signature, offset, length, binding );
	}

	public static QtSignalSlotReference parse( IASTNode parent, IASTNode arg )
	{
		// This check will miss cases like:
		//     #define MY_SIG1 SIGNAL
		//     #define MY_SIG2(s) SIGNAL(s)
		//     #define MY_SIG3(s) SIGNAL(signal())
		//     connect( &a, MY_SIG1(signal()), ...
		//     connect( &a, MY_SIG2(signal()), ...
		//     connect( &a, MY_SIG2, ...
		// This could be improved by adding tests when arg represents a macro expansion.  However, I'm
		// not sure if we would be able to follow the more complicated case of macros that call functions
		// that use the SIGNAL macro.  For now I've implemented the simpler check of forcing the call to
		// use the SIGNAL/SLOT macro directly.
		String raw = arg.getRawSignature();
		Matcher m = ASTUtil.Regex_SignalSlotExpansion.matcher( raw );
		if( ! m.matches() )
			return null;

		Type type;
		String macroName = m.group( 1 );
		if( QtKeywords.SIGNAL.equals( macroName ) )
			type = Type.Signal;
		else if( QtKeywords.SLOT.equals( macroName ) )
			type = Type.Slot;
		else
			return null;

		// Get the argument to the SIGNAL/SLOT macro and the offset/length of that argument within the
		// complete function argument.  E.g., with this argument to QObject::connect
		//      SIGNAL( signal(int) )
		// the values are
		//		expansionArgs:   "signal(int)"
		//		expansionOffset: 8
		//		expansionLength: 11
		String expansionArgs = m.group( 2 );
		int expansionOffset = m.start( 2 );
		int expansionLength = m.end( 2 ) - expansionOffset;

		return new QtSignalSlotReference( parent, arg, type, expansionArgs, expansionOffset, expansionLength );
	}
}
