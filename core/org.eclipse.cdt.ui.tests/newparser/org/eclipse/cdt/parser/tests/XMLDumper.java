package org.eclipse.cdt.parser.tests;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * This class implements a utility that will walk through an object
 * and it's children and create an XML file for it.
 */
public class XMLDumper {

	private int id = 0;
	private HashMap map = new HashMap();
	private Writer stream;
	
	public XMLDumper(Writer stream) {
		this.stream = stream;
	}
	
	public void dump(Object obj) throws IOException, IllegalAccessException {
		Class cls = obj.getClass();
		stream.write("<" + cls.getName() + ">\n");
		
		Field [] fields = cls.getFields();
		for (int i = 0; i < fields.length; ++i) {
			Field field = fields[i];
			
			// Skip over static fields
			if (Modifier.isStatic(field.getModifiers()))
				continue;
			
			// Skip fields that start with an underscore
			if (field.getName().charAt(0) == '_')
				continue;
			
			stream.write("<" + field.getName() + ">");
			
			Class type = field.getType();
			if (String.class.isAssignableFrom(type)) {
				stream.write((String)field.get(obj));				
			}
		}
	}
}
