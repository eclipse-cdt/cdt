/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Kushal Munir (IBM) - Update javadoc for class.
 *******************************************************************************/

package org.eclipse.rse.services.clientserver.java;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.internal.services.clientserver.java.AbstractAttributeInfo;
import org.eclipse.rse.internal.services.clientserver.java.AbstractCPInfo;
import org.eclipse.rse.internal.services.clientserver.java.ClassFileUTF8Reader;
import org.eclipse.rse.internal.services.clientserver.java.ClassInfo;
import org.eclipse.rse.internal.services.clientserver.java.DoubleInfo;
import org.eclipse.rse.internal.services.clientserver.java.EnhancedDataInputStream;
import org.eclipse.rse.internal.services.clientserver.java.FieldInfo;
import org.eclipse.rse.internal.services.clientserver.java.FieldRefInfo;
import org.eclipse.rse.internal.services.clientserver.java.FloatInfo;
import org.eclipse.rse.internal.services.clientserver.java.IClassFileConstants;
import org.eclipse.rse.internal.services.clientserver.java.IntegerInfo;
import org.eclipse.rse.internal.services.clientserver.java.InterfaceMethodRefInfo;
import org.eclipse.rse.internal.services.clientserver.java.LongInfo;
import org.eclipse.rse.internal.services.clientserver.java.MethodInfo;
import org.eclipse.rse.internal.services.clientserver.java.MethodRefInfo;
import org.eclipse.rse.internal.services.clientserver.java.NameAndTypeInfo;
import org.eclipse.rse.internal.services.clientserver.java.StringInfo;
import org.eclipse.rse.internal.services.clientserver.java.UTF8Info;

/**
 * This is a basic class file parser that returns a package name from a class
 * file.
 * <p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class BasicClassFileParser {

	//private long magic;
	//private int minor_version;
	//private int major_version;
	private int constant_pool_count;
	private List constant_pool;
	//private int access_flags;
	private int this_class;
	//private int super_class;
	private int interfaces_count;
	private int[] interfaces;
	private int fields_count;
	private int methods_count;
	private MethodInfo[] methods;
	private int attributes_count;
	private InputStream stream;

	/**
	 * Constructor.
	 * 
	 * @param stream the input stream to parse.
	 */
	public BasicClassFileParser(InputStream stream) {
		this.stream = stream;
	}

	/**
	 * Returns the package.
	 * @return the package.
	 */
	public String getQualifiedClassName() {

		ClassInfo info = (ClassInfo)(getCPInfo(this_class));

		int nameIndex = info.getNameIndex();
		String name = getString(nameIndex);

		return name.replace('/', '.');
	}

	/**
	 * Returns whether there is a <code>public static void main(String[])</code> method.
	 * @return <code>true</code> if there is, otherwise <code>false</code>.
	 */
	public boolean isExecutable() {

		for (int i = 0; i < methods_count; i++) {
			MethodInfo info = methods[i];

			// first ensure method name is "main"
			int nameIndex = info.getNameIndex();
			String name = getString(nameIndex);

			if (name.equals("main")) { //$NON-NLS-1$

				// check access flags for public and static
				int accessFlags = info.getAccessFlags();

				if ((accessFlags & 0x000F) == 0x0009) {

					// now check descriptor for parameter and return value
					int descriptorIndex = info.getDescriptorIndex();
					String descriptor = getString(descriptorIndex);

					if (descriptor.equals("([Ljava/lang/String;)V")) { //$NON-NLS-1$
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Parses a class file.
	 * @throws IOException if an I/O error occurs.
	 */
	public void parse() throws IOException {

		EnhancedDataInputStream dataStream = new EnhancedDataInputStream(stream);

		/*magic = */ dataStream.readUnsignedInt();
		/*minor_version =*/ dataStream.readUnsignedShort();
		/*major_version =*/ dataStream.readUnsignedShort();
		constant_pool_count = dataStream.readUnsignedShort();

		readConstantPool(dataStream);

		/*access_flags =*/ dataStream.readUnsignedShort();

		this_class = dataStream.readUnsignedShort();

		/*super_class =*/ dataStream.readUnsignedShort();

		interfaces_count = dataStream.readUnsignedShort();

		readInterfaces(dataStream);

		fields_count = dataStream.readUnsignedShort();

		readFields(dataStream);

		methods_count = dataStream.readUnsignedShort();

		readMethods(dataStream);

		dataStream.close();
	}

	/**
	 * Reads the constant pool.
	 * @param dataStream the data stream.
	 * @throws IOException if an I/O error occurs.
	 */
	protected void readConstantPool(EnhancedDataInputStream dataStream) throws IOException {

		constant_pool = new ArrayList();

		for (int i = 0; i < constant_pool_count-1; i++) {
			AbstractCPInfo info = readConstantInfo(dataStream);
			constant_pool.add(i, info);

			// each long or double info takes two spaces, so fill in the next entry with an empty object
			// this entry is not usable according to the VM Specification
			if (info instanceof LongInfo || info instanceof DoubleInfo) {
				i++;
				constant_pool.add(i, new Object());
			}
		}
	}

	/**
	 * Gets the constant info.
	 * @param dataStream the data stream.
	 * @return the constant info.
	 * @throws IOException if an I/O error occurs.
	 */
	protected AbstractCPInfo readConstantInfo(EnhancedDataInputStream dataStream) throws IOException {
		short tag = (short)(dataStream.readUnsignedByte());

		switch (tag) {

		case IClassFileConstants.CONSTANT_CLASS: {
			int nameIndex = dataStream.readUnsignedShort();
			return new ClassInfo(tag, nameIndex);
		}
		case IClassFileConstants.CONSTANT_FIELD_REF: {
			int classIndex = dataStream.readUnsignedShort();
			int nameAndTypeIndex = dataStream.readUnsignedShort();
			return new FieldRefInfo(tag, classIndex, nameAndTypeIndex);
		}
		case IClassFileConstants.CONSTANT_METHOD_REF: {
			int classIndex = dataStream.readUnsignedShort();
			int nameAndTypeIndex = dataStream.readUnsignedShort();
			return new MethodRefInfo(tag, classIndex, nameAndTypeIndex);
		}
		case IClassFileConstants.CONSTANT_INTERFACE_METHOD_REF: {
			int classIndex = dataStream.readUnsignedShort();
			int nameAndTypeIndex = dataStream.readUnsignedShort();
			return new InterfaceMethodRefInfo(tag, classIndex, nameAndTypeIndex);
		}
		case IClassFileConstants.CONSTANT_STRING: {
			int stringIndex = dataStream.readUnsignedShort();
			return new StringInfo(tag, stringIndex);
		}
		case IClassFileConstants.CONSTANT_INTEGER: {
			long bytes = dataStream.readUnsignedInt();
			return new IntegerInfo(tag, bytes);
		}
		case IClassFileConstants.CONSTANT_FLOAT: {
			long bytes = dataStream.readUnsignedInt();
			return new FloatInfo(tag, bytes);
		}
		case IClassFileConstants.CONSTANT_LONG: {
			long highBytes = dataStream.readUnsignedInt();
			long lowBytes = dataStream.readUnsignedInt();
			return new LongInfo(tag, highBytes, lowBytes);
		}
		case IClassFileConstants.CONSTANT_DOUBLE: {
			long highBytes = dataStream.readUnsignedInt();
			long lowBytes = dataStream.readUnsignedInt();
			return new DoubleInfo(tag, highBytes, lowBytes);
		}
		case IClassFileConstants.CONSTANT_NAME_AND_TYPE: {
			int nameIndex = dataStream.readUnsignedShort();
			int descriptorIndex = dataStream.readUnsignedShort();
			return new NameAndTypeInfo(tag, nameIndex, descriptorIndex);
		}
		case IClassFileConstants.CONSTANT_UTF8: {
			int length = dataStream.readUnsignedShort();
			short[] bytes = new short[length];

			for (int i = 0; i < length; i++) {
				bytes[i] = (short)(dataStream.readUnsignedByte());
			}

			return new UTF8Info(tag, length, bytes);
		}
		default: {
			return null;
		}
		}
	}

	/**
	 * Reads the interfaces.
	 * @param dataStream the data stream.
	 * @throws IOException if an I/O error occurs.
	 */
	protected void readInterfaces(EnhancedDataInputStream dataStream) throws IOException {
		interfaces = new int[interfaces_count];

		for (int i = 0; i < interfaces_count; i++) {
			interfaces[i] = dataStream.readUnsignedShort();
		}
	}

	/**
	 * Reads the fields.
	 * @param dataStream the data stream.
	 * @throws IOException if an I/O error occurs.
	 */
	protected void readFields(EnhancedDataInputStream dataStream) throws IOException {

		for (int i = 0; i < fields_count; i++) {
			readField(dataStream);
		}
	}

	/**
	 * Reads a field.
	 * @param dataStream the data stream.
	 * @throws IOException if an I/O error occurs.
	 */
	protected FieldInfo readField(EnhancedDataInputStream dataStream) throws IOException {
		/*int accessFlags =    */ dataStream.readUnsignedShort();
		/*int nameIndex =      */ dataStream.readUnsignedShort();
		/*int descriptorIndex =*/ dataStream.readUnsignedShort();
		int attributesCount = dataStream.readUnsignedShort();

		for (int i = 0; i < attributesCount; i++) {
			readAttribute(dataStream);
		}

		return null;
	}

	/**
	 * Reads the methods.
	 * @param dataStream the data stream.
	 * @throws IOException if an I/O error occurs.
	 */
	protected void readMethods(EnhancedDataInputStream dataStream) throws IOException {

		methods = new MethodInfo[methods_count];

		for (int i = 0; i < methods_count; i++) {
			methods[i] = readMethod(dataStream);
		}
	}

	/**
	 * Reads a method.
	 * @param dataStream the data stream.
	 * @throws IOException if an I/O error occurs.
	 */
	protected MethodInfo readMethod(EnhancedDataInputStream dataStream) throws IOException {

		int accessFlags = dataStream.readUnsignedShort();
		int nameIndex = dataStream.readUnsignedShort();
		int descriptorIndex = dataStream.readUnsignedShort();
		int attributesCount = dataStream.readUnsignedShort();

		for (int i = 0; i < attributesCount; i++) {
			readAttribute(dataStream);
		}

		return new MethodInfo(accessFlags, nameIndex, descriptorIndex, attributes_count, null);
	}

	/**
	 * Reads an attribute.
	 * @param dataStream the data stream.
	 * @throws IOException if an I/O error occurs.
	 */
	protected AbstractAttributeInfo readAttribute(EnhancedDataInputStream dataStream) throws IOException {
		dataStream.skip(2);
		long length = dataStream.readUnsignedInt();

		if (length > 0) {
			dataStream.skip(length);
		}

		return null;
	}

	/**
	 * Returns the entry at the constant pool index.
	 * @param index the index.
	 * @return the constant pool table entry.
	 */
	protected AbstractCPInfo getCPInfo(int index) {
		return (AbstractCPInfo)(constant_pool.get(index-1));
	}

	/**
	 * Returns the name given an index to the constant pool table.
	 * The entry at the index must be a UTF8 string entry
	 */
	protected String getString(int index) {
		UTF8Info nameInfo = (UTF8Info)(getCPInfo(index));
		String name = ClassFileUTF8Reader.getInstance().getString(nameInfo.getBytes());
		return name;
	}
}
