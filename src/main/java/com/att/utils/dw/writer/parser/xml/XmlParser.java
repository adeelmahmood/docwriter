package com.att.utils.dw.writer.parser.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.att.utils.dw.exceptions.DocumentCreationException;
import com.att.utils.dw.exceptions.DocumentDataEntryException;
import com.att.utils.dw.writer.DocFactory;
import com.att.utils.dw.writer.DocType;
import com.att.utils.dw.writer.DocWriter;

/*
 * Parses given project xml to given output stream
 * xml format: <project><components><component><rows><row><col> </> ...
 */
public class XmlParser extends DefaultHandler {
	private final static Log log = LogFactory.getLog(XmlParser.class);
	private static SAXParserFactory parserFactory;
	private SAXParser parser;
	
	private boolean traversing = false;
	private boolean traversingInnerTbl = false;
	private boolean traversingInnerTblCol = false;
	private boolean traversingHeaderRows = false;
	private boolean tableCreated = false;
	
	private String nodeValue = "";
	
	private ByteArrayOutputStream baos;
	
	private DocWriter docWriter;
	private Map<String, String> fieldAttributes;
	
	//regexp
	Pattern currencyPattern = Pattern.compile("^\\$[0-9]+(,[0-9]{3})*(\\.[0-9]{2})?$");
	Pattern numberPattern = Pattern.compile("^[0-9|\\.]+$");
	Pattern percentPattern = Pattern.compile("\\d.*%$");
	
	//member variables
	private String projectType;
	
	//constructor
	public XmlParser(DocType type, Map<String, String> attrs){
		String fileType = attrs.get("fileType");
		//create new writer
		docWriter = DocFactory.create(type);
		log.debug("doc writer " + fileType + " created");
		fieldAttributes = new HashMap<String, String>();
		fieldAttributes.putAll(attrs);
		
		//initialize member variables
		projectType = "";
		
		try{
			//instantiate the parser factory
			parserFactory = SAXParserFactory.newInstance();			
			//initialize the parser
			parser = parserFactory.newSAXParser();
		} catch(Throwable ex){
			throw new ExceptionInInitializerError(ex);
		}
		log.debug("report stream parser initialized");
	}
	
	//parses given xml string
	public void parse(String xmlString) throws SAXException, IOException{
		try{
			//create input stream from given xml
			InputSource is = new InputSource();		
		    is.setCharacterStream(new StringReader(xmlString));
		    
			//parse given input stream and register the handler
			parser.parse(is, this);
		} catch(IllegalArgumentException e){
			log.error("error in parsing input stream: " + e);
			throw e;
		} catch (SAXException e) {
			e.printStackTrace();
			log.error("error in parsing input stream: " + e);
			throw e;
		} catch (IOException e) {
			log.error("error in parsing input stream: " + e);
			throw e;
		}
	}
	
	//parses given xml reader
	public void parse(Reader xmlReader) throws SAXException, IOException{
		try{			
			//parse given input stream and register the handler
			parser.parse(new InputSource(xmlReader), this);
		} catch(IllegalArgumentException e){
			log.error("error in parsing input stream: " + e);
			throw e;
		} catch (SAXException e) {
			log.error("error in parsing input stream: " + e);
			throw e;
		} catch (IOException e) {
			log.error("error in parsing input stream: " + e);
			throw e;
		}
	}
	
	//start xml document event handler
	public void startDocument() throws SAXException {
		try{			
			//create new excel document
			docWriter.init(fieldAttributes);
			fieldAttributes.clear();
			log.debug("starting processing report document");			
		} 
		catch(Exception e){
			log.error("error in creating pdf file: " + e);
			throw new SAXException(e);
		}
	}
	
	//start element event handler
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if(qName.equalsIgnoreCase("project")){
			//compare project id here to make given and one in xml matches
			traversing = true;
			projectType = attributes.getValue("type");
			log.debug("now traversing project " + projectType);
		}
		//component tag
		else if(qName.equalsIgnoreCase("component")){
			//get component attributes
			String name = attributes.getValue("name");
			if(name.indexOf("_") != -1){
				name = name.substring(0, name.indexOf("_"));
			}
			String cols = attributes.getValue("cols");
			String type = attributes.getValue("type");
			log.debug("processing component: " + name + " of type: " + type);
			
			try{
				//parse col 
				int col = 0;
				if(cols != null && cols.length() > 0){
					col = Integer.parseInt(cols);
				}
				
				//create new table
				if(type.equalsIgnoreCase("info")){
					float[] widths = {1f, 3f};
					//start new sheet as table for report data
					docWriter.createTable(widths);
					tableCreated = true;
				}
				else if(type.equalsIgnoreCase("data") || type.equalsIgnoreCase("totals")){
					float[] widths = new float[col];
					for(int i=0; i<col; i++){
						if(i==0)
							widths[i] = 1.15f;
						else if(!projectType.equals("_FundingProposal") && i==1)
							widths[i] = 1.25f;					
						else
							widths[i] = 0.75f;
					}
					//start new sheet as table for report data
					docWriter.createTable(widths);
					tableCreated = true;
				}
				else if(!type.equalsIgnoreCase("notes")){ 
					//start new sheet as table for report data
					docWriter.createTable(col, null);
					tableCreated = true;
				}
	
				if(tableCreated){
					//header cell
					fieldAttributes.put("header", "true");
					fieldAttributes.put("colspan", col+"");
					fieldAttributes.put("fontSize", "11");
					fieldAttributes.put("grey", "true");
					//set table heading - exluding totals table
					if(name != null && name.length() > 0 && !name.equalsIgnoreCase("totals")){
						docWriter.addCell(name, fieldAttributes);
					}
					fieldAttributes.clear();
					log.debug("new table created and header cells added");
				}
			} catch (DocumentDataEntryException e1) {
				log.error("error in data entry", e1);
			} catch (DocumentCreationException e2) {
				log.error("error in creation", e2);
			}
		}
		//header tag
		else if(qName.equalsIgnoreCase("header")){
			//header tag is optional - table can wrap some rows in header tag to make them the header of that table
			traversingHeaderRows = true;			
		}
		//rows
		else if(qName.equalsIgnoreCase("row")){
			//nothing to do
		}
		//column
		else if(qName.equalsIgnoreCase("col")){
			nodeValue = "";
			//grab all field attributes
			for(int i=0; i<attributes.getLength(); i++){
				String key = attributes.getQName(i).toString();
				String value = attributes.getValue(i).toString();
				//add to field attributes map
				fieldAttributes.put(key, value);
			}
		}
		//inner tables
		else if(qName.equalsIgnoreCase("innertable")){
			traversingInnerTbl = true;
			
			int col = 0;
			String cols = attributes.getValue("cols");
			//parse col
			if(cols != null && cols.length() > 0){
				col = Integer.parseInt(cols);
			}
			
			//create new inner table
			docWriter.startInnerTable(col);
		}
		//paragraph notes
		else if(qName.equalsIgnoreCase("para")){
			nodeValue = "";
		}
	}
	
	//node value event handler
	public void characters(char[] ch, int start, int length) throws SAXException {
		//grab current node value
		nodeValue += new String(ch, start, length);
	}
	
	//end element event handler
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(traversing){
			//end col (skip repeated headers rows and cols)
			if(qName.equalsIgnoreCase("col")){
				//add cell
				try {
					if(traversingInnerTblCol){
						traversingInnerTblCol = false;
					}
					else{
						if(traversingHeaderRows){
							fieldAttributes.put("header", "true");
							fieldAttributes.put("lightgrey", "true");
						}
						
						if(numberPattern.matcher(nodeValue).matches()){
							fieldAttributes.put("dataType", "number");
						}
						else if(currencyPattern.matcher(nodeValue).matches()){
							fieldAttributes.put("dataType", "currency");
						}
						else if(percentPattern.matcher(nodeValue).matches()){
							fieldAttributes.put("dataType", "percent");	
						}
						//add to cell
						docWriter.addCell(nodeValue, fieldAttributes);
					}
				} catch (DocumentDataEntryException e) {
					log.error("errir in end element", e);
				}
				fieldAttributes.clear();
			} 
			//end para 
			else if(qName.equalsIgnoreCase("para")){
				if(nodeValue.length() > 0){
					log.debug("paragraph content: " + nodeValue);
					//extract title for paragrah
					String[] paraData = nodeValue.split("___");
					
					//add paragraph
					try {
						docWriter.addParagraph(paraData[0], nodeValue.replaceFirst(paraData[0] + "___", ""), fieldAttributes);
					} catch (DocumentDataEntryException e) {
						log.error("error in adding paragraph", e);
					}					
				}
			}
			//end row
			else if(qName.equalsIgnoreCase("row")){
				//nothing to do 				
			}
			//end header rows
			else if(qName.equalsIgnoreCase("header")){				
				traversingHeaderRows = false;
			}
			//end table
			else if(qName.equalsIgnoreCase("component")){
				//table completed - add to dcoument
				try {
					if(tableCreated){
						docWriter.endTable();
						tableCreated = false;
						log.debug("completed component.");						
					}
				} catch (DocumentCreationException e) {
					log.error("error ing close component", e);
				}				
			}
			//end inner table
			else if(qName.equalsIgnoreCase("innertable")){
				//inner table completed - add to main table
				if(traversingInnerTbl){
					docWriter.endInnerTable();
				}
				traversingInnerTbl = false;
				traversingInnerTblCol = true;	//inner table is contained within a col tag
			}
			//end budget tag
			else if(qName.equalsIgnoreCase("project")){
				traversing = false;
				docWriter.newPage();
				log.debug("completed project");
			}
		}
	}	
	
	//end document event handler		
	public void endDocument() throws SAXException {
		try {
			docWriter.end();
			log.debug("parsing completed");
		} catch (IOException e) {
			log.error("error in closing document", e);
		}		
		this.setBaos(docWriter.getOutputStream());
	}	

	public void setBaos(ByteArrayOutputStream baos) {
		this.baos = baos;
	}

	public ByteArrayOutputStream getBaos() {
		return baos;
	}
}