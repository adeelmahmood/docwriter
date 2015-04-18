package com.att.utils.dw.writer;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;

public class AbstractWriterTest {

	DocWriter docWriter;
	Map<String, String> attrs;
	String out;
	
	@Before
	public void setUp() throws Exception {
		attrs = new HashMap<String, String>();
	}

	@After
	public void tearDown() throws Exception {
		//grab the output file name and create a file from output stream of doc writer
		ByteArrayOutputStream baos = docWriter.getOutputStream();
		OutputStream outStream = new FileOutputStream(out);
		baos.writeTo(outStream);		
	}
}
