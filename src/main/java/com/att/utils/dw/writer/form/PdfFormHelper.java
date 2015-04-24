package com.att.utils.dw.writer.form;

import java.awt.Color;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfBorderDictionary;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.TextField;

public class PdfFormHelper {
	private Log log = LogFactory.getLog(PdfFormHelper.class);

	public PdfFormHelper() {

	}

	public TextField createTextField(PdfWriter writer, String name, boolean required) {
		log.debug("creating a text field");
		// create new text field
		TextField textfield = new TextField(writer, new Rectangle(0, 0, 200, 10), name);
		textfield.setBackgroundColor(Color.WHITE);
		textfield.setBorderColor(Color.BLACK);
		textfield.setBorderWidth(1);
		textfield.setBorderStyle(PdfBorderDictionary.STYLE_SOLID);
		textfield.setText("");
		textfield.setAlignment(Element.ALIGN_LEFT);
		if (required) {
			textfield.setOptions(TextField.REQUIRED);
		}
		return textfield;
	}
}
