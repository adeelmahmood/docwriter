package com.att.utils.dw.writer.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.utils.dw.exceptions.DocumentCreationException;
import com.att.utils.dw.exceptions.DocumentDataEntryException;
import com.att.utils.dw.writer.DocWriter;

/**
 * The Class ExcelDocWriter.
 * Implements the doc writer to provide excel document creation functionality
 */
public class ExcelDocWriter implements DocWriter {
	
	/** The log. */
	private Logger log = LoggerFactory.getLogger(ExcelDocWriter.class);
	
	//excel params
	/** The workbook. */
	private WritableWorkbook workbook;
	
	/** The sheet. */
	private WritableSheet sheet;
	
	/** The col index. */
	private int colIndex = 0;
	
	/** The row index. */
	private int rowIndex = 0;
	
	/** The current row index. */
	private int currentRowIndex = 0;
	
	/** The num cols. */
	private int numCols = 0;
	
	/** The table indexes override. */
	private boolean tableIndexesOverride = false;
	
	/** The cell view. */
	private CellView cellView;
	
	/** The using inner table. */
	private boolean usingInnerTable = false;
	
	/** The inner table contents. */
	private String[] innerTableContents;
	
	/** The inner table index. */
	private int innerTableIndex;
	
	//cell formats
	/** The currency number format. */
	private NumberFormat currencyNumberFormat = new NumberFormat(NumberFormat.CURRENCY_DOLLAR + "#,###", NumberFormat.COMPLEX_FORMAT);	
	
	/** The cell format. */
	private WritableCellFormat cellFormat;
	
	/** The cell font. */
	private WritableFont cellFont;
	
	/** The baos. */
	private ByteArrayOutputStream baos;
	
	/**
	 * Instantiates a new excel doc writer.
	 */
	public ExcelDocWriter(){			
	}
	
	public void init(Map<String, String> reportAttrs) throws DocumentCreationException {
		log.debug("creating new excel document");
		//reset local vars
		colIndex = 0;
		rowIndex = 0;
		numCols = 0;
		tableIndexesOverride = false;
		
		//initialize the output stream
		baos = new ByteArrayOutputStream();
		
		//set workbook settings to help with memory usage
		WorkbookSettings ws = new WorkbookSettings();
		ws.setDrawingsDisabled(true);
		ws.setNamesDisabled(true);
		ws.setMergedCellChecking(false);
		ws.setFormulaAdjust(false);
		ws.setCellValidationDisabled(true);
		
		try {
			workbook = Workbook.createWorkbook(baos);
		} catch (IOException e) {
			log.error("error in creating report. " + e);
			throw new DocumentCreationException(e.toString());
		}
		//add new sheet (in excel a sheet functions as a table)
		sheet = workbook.createSheet("Sheet1", 0);		
		log.debug("new workbook and sheet initialized");
	}

	public void createTable(int cols, String type) {
		if(!tableIndexesOverride){
			//rowIndex = 0;	rowIndex needs to keep incrementing because multiple tables can be added
			currentRowIndex = 0;	//for current table
			colIndex = 0;
		}
		numCols = cols;
		tableIndexesOverride = false;
		log.debug("create table called for excel with " + cols + " cols, just did the reset for colIndex");
	}
	
	public void createTable(float[] widths) {
		createTable(widths.length, null);
	}
	
	public void addPageHeader(String[] headers) throws DocumentDataEntryException{
		for(int i=0; i<headers.length; i++){
			try {
				cellFont = new WritableFont(WritableFont.ARIAL, 11, WritableFont.BOLD);				
				//header
				if(i==0)
					cellFont.setUnderlineStyle(UnderlineStyle.SINGLE);
				cellFormat = new WritableCellFormat(cellFont);
				//create label
				Label label = new Label(colIndex, rowIndex, headers[i], cellFormat);
				//add to sheet
				sheet.addCell(label);
				//after spacing
				colIndex=0;
				if(i==0 && i<headers.length-1)
					rowIndex+=1;
				else
					rowIndex+=2;
			} catch (WriteException e) {
				log.error("error in adding page header. " + e);
				throw new DocumentDataEntryException(e.toString());
			}
			//indicate table indexes are already set - dont reinitialize
			tableIndexesOverride = true; 
		}
	}
		
	public void addCell(String value, Map<String, String> attrs) throws DocumentDataEntryException {
		String dataType = "string";
		
		//if inside an inner table simply keep buffering the additional values
		//and add them all in a single cell at the end
		if(this.isUsingInnerTable()){
			innerTableContents[innerTableIndex++] = value;
			return;
		}
		
		//create cell font
		cellFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);		
		
		//bold
		if(attrs.containsKey("bold") || "bold".equals(attrs.get("format"))){
			try {
				cellFont.setBoldStyle(WritableFont.BOLD);
			} catch (WriteException e) {
				log.error("error in setting bold font: " + e);
			}
		}
		//underline
		if(attrs.containsKey("underline") || "underline".equals(attrs.get("format"))){
			try {
				cellFont.setUnderlineStyle(UnderlineStyle.SINGLE);
			} catch (WriteException e) {
				log.error("error in setting underline: " + e);
			}
		}
		
		//create cell format based on given data type
		if(attrs.containsKey("dataType")){
			dataType = attrs.get("dataType");
			//dataType can be overridden by a manul data type
			if(attrs.containsKey("overrideDataType")){
				dataType = attrs.get("overrideDataType");
			}
			
			//based on data type create the cell
			if(dataType.equals("number")){
				cellFormat = new WritableCellFormat(cellFont, NumberFormats.INTEGER);				
			}
			else if(dataType.equals("double")){
				cellFormat = new WritableCellFormat(cellFont, NumberFormats.FLOAT);				
			}
			else if(dataType.equals("currency")){
				cellFormat = new WritableCellFormat(cellFont, currencyNumberFormat);
			}
			else if(dataType.equals("percent")){
				cellFormat = new WritableCellFormat(cellFont, NumberFormats.PERCENT_FLOAT);
			}
			else{
				cellFormat = new WritableCellFormat(cellFont);
			}
		}
		//use default cell format
		else{
			cellFormat = new WritableCellFormat(cellFont);
		}

		try{
			//background color
			if(attrs.containsKey("grey")){
				cellFormat.setBackground(Colour.GRAY_50);
				//override font color 
				cellFont.setColour(Colour.WHITE);				
			}
			else if(attrs.containsKey("lightgrey")){
				cellFormat.setBackground(Colour.GRAY_25);
			}
			//highlighting 
			if(attrs.containsKey("highlight") || attrs.containsKey("highlighted")){
				cellFormat.setBackground(Colour.YELLOW);
			}
			
			//aligment
			if(attrs.containsKey("align")){
				if(attrs.get("align").equals("center")){
					cellFormat.setAlignment(Alignment.CENTRE);
				}
				else if(attrs.get("align").equals("right")){
					cellFormat.setAlignment(Alignment.RIGHT);
				}
			}		
			
			//wrapping
			if(attrs.containsKey("wrap")){
				cellFormat.setWrap(true);
			}
		} catch (WriteException e) {
			log.error("error in adding cell. " + e);
			throw new DocumentDataEntryException(e.toString());
		}
		
		//add to table
		try {			
			//numbers
			if((dataType.equals("number") || dataType.equals("percent")) && !"".equals(value)){
				double val = dataType.equals("number") ? Double.parseDouble(value) : Double.parseDouble(value.replaceAll("%", ""))/100;
				//add number to cell
				sheet.addCell(new Number(colIndex, rowIndex, val, cellFormat));
			}
			else if(dataType.equals("currency") && !"".equals(value)){
				//extract the number
				java.lang.Number num = java.text.NumberFormat.getCurrencyInstance().parse(value);
				if(num instanceof Long){
					//add number to cell
					sheet.addCell(new Number(colIndex, rowIndex, num.longValue(), cellFormat));
				}
				else {
					//add number to cell
					sheet.addCell(new Number(colIndex, rowIndex, num.doubleValue(), cellFormat));
				}
			}
			else {
				//add label to cell
				sheet.addCell(new Label(colIndex, rowIndex, value, cellFormat));
			}
			
			//colspan (default to 1 to increment atleast on column, as we have added one)
			int colspan = 1;
			if(attrs.containsKey("colspan")){
				colspan = Integer.parseInt(attrs.get("colspan"));
				//merge cells for colspan
				sheet.mergeCells(colIndex, rowIndex, colIndex+colspan-1, rowIndex);
			}
			
			//header rows
			if(attrs.containsKey("header")){
				if(cellView == null)
					cellView = new CellView();			
				//header column width
				if(attrs.containsKey("colWidth")){
					String widthStr = attrs.get("colWidth");				
					int width = Integer.parseInt(widthStr);
					cellView.setSize(width);
					cellView.setAutosize(false);
					log.debug("applied colWidth " + width + " to header: " + value);
				}
				//by default set the header column width auto
				else{
					cellView.setSize(1);
					cellView.setAutosize(true);
				}
				sheet.setColumnView(colIndex, cellView);
			}
			colIndex+=colspan;			
		} catch (RowsExceededException e) {
			log.error("error in adding cell", e);
			throw new DocumentDataEntryException(e.toString());
		} catch (WriteException e) {
			log.error("error in adding cell", e);
			throw new DocumentDataEntryException(e.toString());
		} catch (ParseException e) {
			log.error("error in parsing number", e);
			throw new DocumentDataEntryException(e.toString());
		}		
		
		//increment row and col indexes
		if(colIndex >= numCols){
			colIndex = 0;
			rowIndex++;
			currentRowIndex++;
		}
	}

	public void endTable() throws DocumentCreationException {
		try {
			//apply row grouping to the table
			sheet.setRowGroup(rowIndex-currentRowIndex+1, rowIndex-1, false);
			log.debug("setting row group for " + (rowIndex-currentRowIndex+1) + " to " + (rowIndex-1));
		} catch (RowsExceededException e) {
			log.error("error in end table, row exceeded exception", e);
			throw new DocumentCreationException(e.toString());
		} catch (WriteException e) {
			log.error("error in end table, write exception", e);
			throw new DocumentCreationException(e.toString());
		}
		log.debug("completed table");		
	}

	public void end() throws IOException {
		workbook.write();
		try {
			workbook.close();
		} catch (WriteException e) {
			log.error("error in ending document. " + e);
			throw new IOException(e.toString());
		}
		baos.close();
		baos.flush();
		log.debug("completed document");
	}
	
	public void removeRow(int count) {
		sheet.removeColumn(count);
		log.debug("removed rows (cols) " + count);
	}

	public ByteArrayOutputStream getOutputStream() {
		return baos;
	}

	public void newPage() {
		log.debug("@@@ new page for excel writer is not implemented yet");
	}

	public void addParagraph(String titleStr, String contentStr, Map<String, String> attrs) throws DocumentDataEntryException {
		attrs.put("bold", "true");
		//add the title
		addCell(titleStr, attrs);
		attrs.remove("bold");
		attrs.put("wrap", "true");
		//add content
		addCell(contentStr, attrs);
	}

	public void startInnerTable(int col) {
		this.setUsingInnerTable(true);
		innerTableContents = new String[col];
		innerTableIndex = 0;
		log.debug("started inner table");
	}

	public void endInnerTable() {
		if(this.isUsingInnerTable()){
			this.setUsingInnerTable(false);	
			//create attributes map
			Map<String, String> attrs = new HashMap<String, String>();
			//add everything in inner table buffer to a single cell
			try {
				addCell(StringUtils.join(innerTableContents, " | "), attrs);
			} catch (DocumentDataEntryException e) {
				log.error("error in adding inner table contents", e);
			}
			log.debug("completed inner table");
		}
	}

	/**
	 * Sets the using inner table.
	 *
	 * @param usingInnerTable the new using inner table
	 */
	private void setUsingInnerTable(boolean usingInnerTable) {
		this.usingInnerTable = usingInnerTable;
	}

	/**
	 * Checks if is using inner table.
	 *
	 * @return true, if is using inner table
	 */
	private boolean isUsingInnerTable() {
		return usingInnerTable;
	}
}