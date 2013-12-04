package com.sff.report_performance;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;


/*
 * This class is used to filter out any non-digit input
 * */
public class MyDocumentFilter {
	public static PlainDocument createDocumentFilter() {
		PlainDocument doc = new PlainDocument();
		doc.setDocumentFilter(new DocumentFilter() {
			@Override
			public void insertString(FilterBypass fb, int off, String str, AttributeSet attr) 
					throws BadLocationException{
				fb.insertString(off, str.replaceAll("\\D++", ""), attr);
			} 
			@Override
			public void replace(FilterBypass fb, int off, int len, String str, AttributeSet attr) 
					throws BadLocationException{
				fb.replace(off, len, str.replaceAll("\\D++", ""), attr); 
			}
		});
		return doc;
	}
}
