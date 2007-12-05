/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of Docx4all.

    Docx4all is free software: you can redistribute it and/or modify
    it under the terms of version 3 of the GNU General Public License 
    as published by the Free Software Foundation.

    Docx4all is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License   
    along with Docx4all.  If not, see <http://www.gnu.org/licenses/>.
    
 */

package org.docx4all.swing.text;

import javax.swing.text.AttributeSet;
import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.View;

import org.apache.log4j.Logger;
import org.docx4all.xml.ElementML;
import org.docx4all.xml.WordML;

public class ViewFactory implements javax.swing.text.ViewFactory {
	private static Logger log = Logger.getLogger(ViewFactory.class);
	
    /**
     * Creates a view from the given structural element of a
     * document.
     *
     * @param elem  the piece of the document to build a view of
     * @return the view
     * @see View
     */
    public View create(Element elem) {
		View theView = null;

		AttributeSet attrs = elem.getAttributes();
		ElementML elementML = WordMLStyleConstants.getElementML(attrs);
		WordML.Tag tag = elementML.getTag();

		if (tag == WordML.Tag.P) {
			theView = new ParagraphView(elem);
		} else if (tag == WordML.Tag.R) {
			theView = new RunView(elem);
		} else if (tag == WordML.Tag.T) {
			theView = new LabelView(elem);
		} else {
			theView = new BoxView(elem, View.Y_AXIS);
		}
		
		return theView;
	}


}// ViewFactory class























