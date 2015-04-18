package com.att.utils.dw.reader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

/**
 * PdfDocReader class
 * Provides pdf document reading functionality
 * 
 * @author m057188
 */
public class PdfDocReader {
	private Logger log = LoggerFactory.getLogger(PdfDocReader.class);
	
	private String fileName;
	
	public PdfDocReader(String fileName){
		this.fileName = fileName;
		log.debug("pdf doc reader created for file " + fileName);
	}
	
	@SuppressWarnings("rawtypes")
  public void readDoc(){
		log.debug("reading document " + fileName);
		try {
			//create new pdf reader
			PdfReader reader = new PdfReader(fileName);
			
			//get fields
			AcroFields form = reader.getAcroFields();			
			HashMap fields = form.getFields();
			
			String key;
			for (Iterator i = fields.keySet().iterator(); i.hasNext(); ){
				key = (String) i.next();
				log.debug("Key: " + key + " - " + form.getFieldType(key));
			}
		} catch (IOException e) {
			log.error("error in reading document", e);
		}		
	}
	
	@SuppressWarnings("rawtypes")
	public void readAndWriteDoc(){
		log.debug("reading document to write");
		try {
			//create new pdf reader
			PdfReader reader = new PdfReader(fileName);
			PdfStamper stamper = new PdfStamper(reader, 
					new FileOutputStream(fileName.replace(".pdf", "-out.pdf")), '\0', false);			
			
			//get fields
			AcroFields form = stamper.getAcroFields();			
			HashMap fields = form.getFields();
			
			String key;
			for (Iterator i = fields.keySet().iterator(); i.hasNext(); ){
				key = (String) i.next();
				if(key.endsWith("Date[0]")){
					form.setField(key, "04-13-2012");							
				}
				else if(key.indexOf("BaseSalary") != -1){
					form.setField(key, Double.toString(100));
				}else{
					form.setField(key, "test");
				}
				if(key.indexOf("FirstName") != -1 || key.indexOf("LastName") != -1 || key.indexOf("Role") != -1){
					log.debug("Key: " + key + " - " + form.getFieldType(key));
					form.setField(key, "test");					
				}
				else if(key.indexOf("subform") != -1 && form.getFieldType(key) == AcroFields.FIELD_TYPE_TEXT){
					log.debug("Key: " + key + " - " + form.getFieldType(key));
					form.setField(key, Integer.toString(100));
				}
			}			
			stamper.setFormFlattening(true);
						
			//close writer
			stamper.close();
			
		} catch (IOException e) {
			log.error("error in reading document", e);
		} catch (DocumentException e) {
			log.error("error in writing document", e);
		}
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
}
