package com.att.utils.dw.writer;

import java.text.DecimalFormat;
import java.util.Random;

import org.junit.Test;

import com.att.utils.dw.exceptions.DocumentCreationException;
import com.att.utils.dw.utils.Constants;

/**
 * DirectWriter uses DocWriter functionality
 * 
 * @author aq728y
 *
 */
public class DirectWriterTest extends AbstractWriterTest{

	Random random = new Random();
	DecimalFormat df = new DecimalFormat("##.00");
	
	@Test
	public void testDiffTablesCreation() throws Exception {
		startDocument();
		
		//first long table
		docWriter.addPageHeader(new String[]{ "First Table", "Listing of Records" });		
		createSimpleTable(10, null, 300, 0, true);
		docWriter.newPage();		
		
		//second long table
		docWriter.addPageHeader(new String[]{ "Second Table", "Listing of Records" });
		createSimpleTable(10, null, 300, 0, true);
		docWriter.newPage();		
		
		//third table with variable column width
		docWriter.addPageHeader(new String[]{ "Table With First Big Column And Small Last Column", "Listing of Records" });		
		createSimpleTable(5, Constants.FIRST_COL_BIG_LAST_COL_SMALL, 30, 0, true);
		
		//fourth table with variable column width
		docWriter.addPageHeader(new String[]{ "Table With First Big Column", "Listing of Records" });
		createSimpleTable(6, Constants.FIRST_COL_BIG, 36, 0, true);

		//add table
		docWriter.addPageHeader(new String[]{ "Table With Inner Table" });
		createSimpleTable(5, Constants.FIRST_COL_BIG, 25, 2, false);
		
		//close document
		docWriter.end();
		//indicate output file name
		out = "output/tables.pdf";
	}
	
	@Test
	public void testSecuredLandspaceAndTextWatermark() throws Exception {
		attrs.put("landscape", "true");
		attrs.put("secured", "true");
		attrs.put("watermark", "text:ADEEL TEST,SOMETHING ELSE ANOTHER,FINAL DRAFT");
		startDocument();
		
		//first long table
		docWriter.addPageHeader(new String[]{ "First Table", "Listing of Records" });		
		createSimpleTable(10, null, 300, 0, true);
		
		//close document
		docWriter.end();
		//indicate output file name
		out = "output/tables-text-watermark-secured-landspace.pdf";
	}
	
	@Test
	public void testSecuredLandspaceAndImageWatermark() throws Exception {
		attrs.put("landscape", "true");
		attrs.put("secured", "true");
		attrs.put("watermark", "image:src/test/resources/draft.png");
		startDocument();
		
		//first long table
		docWriter.addPageHeader(new String[]{ "First Table", "Listing of Records" });		
		createSimpleTable(10, null, 300, 0, true);
		
		//close document
		docWriter.end();
		//indicate output file name
		out = "output/tables-image-watermark-secured-landspace.pdf";
	}
	
	private void startDocument() throws DocumentCreationException{
		//pdf document
		docWriter = DocFactory.create(DocType.PDF);
		//create new document
		docWriter.init(attrs);		
	}
	
	private void createSimpleTable(int cols, String colWidthType, int vals, int innerIndex, boolean simple) throws Exception{
		//start table
		docWriter.createTable(cols, colWidthType);
		
		//header
		attrs.put("header", "true");
		attrs.put("fontSize", "11");
		attrs.put("grey", "true");
		
		//add header row
		for(int i=1; i<=cols; i++){
			docWriter.addCell("Header " + i, attrs);
		}
		attrs.clear();
		
		//add content
		for(int i=1; i<=vals; i++){
			if(innerIndex > 0 && i%cols==innerIndex){
				attrs.clear();
				docWriter.startInnerTable(2);
				docWriter.addCell("Inner " + i + "-1", attrs);
				docWriter.addCell("Inner " + i + "-2", attrs);
				docWriter.endInnerTable();
			}
			else{
				docWriter.addCell(simple ? "Column " + i : getValue(i, cols), attrs);
			}
		}
		
		//footer
		attrs.put("highlight", "true");
		attrs.put("bold", "true");
		for(int i=1; i<=cols; i++){
			docWriter.addCell("Footer " + i, attrs);
		}
		attrs.clear();
		
		//close table
		docWriter.endTable();
	}
	
	private String getValue(int col, int cols){
		String val = "Column " + col;
		attrs.clear();
		if(col%cols == 1){
			attrs.put("fontSize", "13");
			attrs.put("bold", "true");
		}
		else if(col%cols == 2){
			attrs.put("underline", "true");
			attrs.put("align", "center");
		}
		else if(col%cols == 3){
			val = df.format(random.nextDouble() * (random.nextInt(5) + 1)) + "%";
			attrs.put("grey", "true");
			attrs.put("align", "right");
		}
		else if(col%cols == 4){
			val = df.format(random.nextDouble() * (random.nextInt(5) + 1)) + "%";
			attrs.put("lightgrey", "true");
			attrs.put("align", "right");
		}
		else if(col%cols == 5){
			val = "Total " + col;
		}
		return val;
	}
}