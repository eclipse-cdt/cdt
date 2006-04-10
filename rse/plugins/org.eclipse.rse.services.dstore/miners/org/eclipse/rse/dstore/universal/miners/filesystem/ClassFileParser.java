/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.dstore.universal.miners.filesystem;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class ClassFileParser {


   	private static final int CONSTANT_Class = 7;
   	private static final int CONSTANT_Fieldref = 9;
   	private static final int CONSTANT_Methodref = 10;
   	private static final int CONSTANT_InterfaceMethodref = 11;
   	private static final int CONSTANT_String = 8;
   	private static final int CONSTANT_Integer = 3;
   	private static final int CONSTANT_Float = 4;
   	private static final int CONSTANT_Long = 5;
   	private static final int CONSTANT_Double = 6;
   	private static final int CONSTANT_NameAndType = 12;
   	private static final int CONSTANT_Utf8 = 1;

	private DataInputStream in;
	
   	private ArrayList classes = new ArrayList();
   	private ArrayList utf8s = new ArrayList();

   	private class _Class {
      	public int pool_index;
      	public int name_index;

      	public _Class(int pIndx, int nIndx) {
         	pool_index = pIndx;
         	name_index = nIndx;
      	}
   	}

   	private class _Utf8 {
      	public int pool_index;
      	public byte[] bytes;

      	public _Utf8(int pIndx, byte[] bytes) {
         	pool_index = pIndx;
         	this.bytes = bytes;
      	}
   	}
	
	
	public ClassFileParser(InputStream stream)
	{
		in = new DataInputStream(stream);
	}

	public String getPackageName() 
	{
		String packageName = null;
      	try {
         	// Skip magic / miner / major
         	in.skipBytes(8);

         	int constPoolCount = in.readUnsignedShort();

         	// Read in the constant_pool storing Class and Utf8 entries
         	int tag;
         	int index;
         	int len;
         	byte[] data;
         	for (int loop = 1; loop < constPoolCount; loop++) {
            	tag = in.readUnsignedByte();
            	switch (tag) {
               		case CONSTANT_Class:
                    	index = in.readUnsignedShort();
                     	classes.add(new _Class(loop, index));
                  		break;

               		case CONSTANT_Utf8:
                  		len = in.readUnsignedShort();
                  		data = new byte[len];
                  		in.read(data, 0, len);
                  		utf8s.add(new _Utf8(loop, data));
                  		break;

               		case CONSTANT_Fieldref:
               		case CONSTANT_Methodref:
               		case CONSTANT_InterfaceMethodref:
               		case CONSTANT_Integer:
               		case CONSTANT_Float:
               		case CONSTANT_NameAndType:
                  		in.skipBytes(4);
                  		break;

               		case CONSTANT_String:
                  		in.skipBytes(2);
                  		break;

               		case CONSTANT_Long:
               		case CONSTANT_Double:
                  		in.skipBytes(8);
                  		// these take two entries in constant pool
						loop++;
                  		break;

               		default:
                  		break;
            	}
         	}

      		// Skip access flags
      		in.skipBytes(2);
      		int classNameIndex = in.readUnsignedShort();

      		boolean found = false;
      		int utf8Index = -1;
      		for(int loop = 0; loop < classes.size() && !found; loop++) {
         		if (((_Class)classes.get(loop)).pool_index == classNameIndex) {
            		found = true;
            		utf8Index = ((_Class)classes.get(loop)).name_index;
         		}
	  		}

      		found = false;
      		for (int loop = 0; loop < utf8s.size() && !found; loop++) {
         		if (((_Utf8)utf8s.get(loop)).pool_index == utf8Index) {
            		packageName = new String(((_Utf8)utf8s.get(loop)).bytes);
            		packageName = packageName.replace('/', '.');
            		found = true;
         		}
      		}

      		in.close();


      	} catch (Exception e) {
         	e.printStackTrace();
      	}
		
		return packageName;		
	}
	
}