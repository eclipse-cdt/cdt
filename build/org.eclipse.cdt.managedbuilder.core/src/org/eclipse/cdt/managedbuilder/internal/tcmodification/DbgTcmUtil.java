/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.tcmodification;

import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.internal.core.IRealBuildObjectAssociation;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.extension.MatchObjectElement;
import org.eclipse.core.runtime.IPath;

public class DbgTcmUtil {
	private static final PrintStream OUT = System.out;
	public static boolean DEBUG = false;
	
	private DbgTcmUtil(){
	}
	public static final class DbgException extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private DbgException() {
			super();
		}

		private DbgException(String message, Throwable cause) {
			super(message, cause);
		}

		private DbgException(String message) {
			super(message);
		}

		private DbgException(Throwable cause) {
			super(cause);
		}
	}
	
	public static void print(String str){
		OUT.print(str);
	}

	public static void println(String str){
		OUT.println(str);
	}
	
	public static void fail(String msg){
		println(msg);
		throw new DbgException(msg);
	}

	public static void fail(){
		DbgException e = new DbgException();
		e.printStackTrace(OUT);
		throw e;
	}
	
	public static void dumpStorage(PerTypeMapStorage<? extends IRealBuildObjectAssociation, Set<IPath>> storage){
		println("starting storage dump.."); //$NON-NLS-1$
		int[] types = ObjectTypeBasedStorage.getSupportedObjectTypes();
		for(int i = 0; i < types.length; i++){
			int type = types[i];
			MatchObjectElement.TypeToStringAssociation assoc = MatchObjectElement.TypeToStringAssociation.getAssociation(type);
			if(assoc == null)
				continue;
			
			println(" dumping for type " + assoc.getString()); //$NON-NLS-1$
			
			@SuppressWarnings("unchecked")
			Map<IRealBuildObjectAssociation, Set<IPath>> map = (Map<IRealBuildObjectAssociation, Set<IPath>>) storage.getMap(type, false);
			if(map != null){
				Set<Entry<IRealBuildObjectAssociation, Set<IPath>>> entrySet = map.entrySet();
				for (Entry<IRealBuildObjectAssociation, Set<IPath>> entry : entrySet) {
					IRealBuildObjectAssociation obj = entry.getKey();
					println("  dumping " + assoc.getString() + " " + obj.getUniqueRealName()); //$NON-NLS-1$ //$NON-NLS-2$
					Set<IPath> set = entry.getValue();
					if(set != null){
						for (IPath path : set) {
							println("   path \"" + path + "\""); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
					println("  end dumping " + obj.getUniqueRealName()); //$NON-NLS-1$
				}
			}
			
			println(" end type " + assoc.getString()); //$NON-NLS-1$
		}
		println("end storage dump"); //$NON-NLS-1$
	}

}
