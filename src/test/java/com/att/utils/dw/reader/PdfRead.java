package com.att.utils.dw.reader;

import static org.junit.Assert.*;

import org.junit.Test;

import com.att.utils.dw.reader.PdfDocReader;

public class PdfRead {
	
	@Test
	public void pdfReadFromFile(){
		//create a pdf reader
		PdfDocReader reader = new PdfDocReader("tmp/Subaward01 RR_Budget-V1.1.pdf");
		assertNotNull(reader);
		
		//check filename was set
		assertEquals("tmp/Subaward01 RR_Budget-V1.1.pdf", reader.getFileName());
		
		//try reading
		reader.readDoc();
	}

	@Test
	public void pdfReadAndWrite(){
		//create a pdf reader
		PdfDocReader reader = new PdfDocReader("tmp/Subaward01 RR_Budget-V1.1.pdf");
		assertNotNull(reader);
		
		//check filename was set
		assertEquals("tmp/Subaward01 RR_Budget-V1.1.pdf", reader.getFileName());
		
		//try reading
		reader.readAndWriteDoc();
	}
}
