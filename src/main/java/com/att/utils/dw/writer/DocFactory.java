package com.att.utils.dw.writer;

import com.att.utils.dw.writer.excel.ExcelDocWriter;
import com.att.utils.dw.writer.pdf.PdfDocWriter;

/**
 * DocFactory creates instances of DocWriter
 * 
 * @author aq728y
 *
 */
public class DocFactory {

	/**
	 * Creates a new DocWriter instance based on given type
	 * @param type doc writer typ
	 * @return DocWriter
	 */
	public static DocWriter create(DocType type){
		DocWriter docWriter = null;
		switch(type){
			case PDF:
				docWriter = new PdfDocWriter();
				break;
			case EXCEL:
				docWriter = new ExcelDocWriter();
				break;
			default:
				throw new RuntimeException("Unknown doc type " + type);
		}
		return docWriter;
	}
}
