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

package org.docx4all.swing;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.MutableAttributeSet;

import org.apache.log4j.Logger;
import org.docx4all.swing.text.WordMLDocument;
import org.docx4all.swing.text.WordMLEditorKit;
import org.docx4all.swing.text.WordMLFragment;
import org.docx4all.ui.main.WordMLEditor;
import org.docx4all.util.DocUtil;
import org.jdesktop.application.ResourceMap;

/**
 *	@author Jojada Tirtowidjojo - 22/01/2008
 */
public class WordMLTextPane extends JEditorPane {
	private static Logger log = Logger.getLogger(WordMLTextPane.class);

	public WordMLTextPane() {
		super();
		getEditorKit().install(this);
	}
	
    public void setDocument(Document doc) {
        if (doc instanceof WordMLDocument) {
            super.setDocument(doc);
        } else {
            throw new IllegalArgumentException("Model must be WordMLDocument");
        }
    }

    /**
     * Sets the currently installed kit for handling
     * content.  This is the bound property that
     * establishes the content type of the editor.
     * 
     * @param kit the desired editor behavior
     * @exception IllegalArgumentException if kit is not a
     *		<code>WordMLEditorKit</code>
     */
    public void setEditorKit(EditorKit kit) {
        if (kit instanceof WordMLEditorKit) {
            super.setEditorKit(kit);
        } else {
            throw new IllegalArgumentException("Must be WordMLEditorKit");
        }
    }

    public void replaceSelection(String content) {
        if (!isEditable()) {
        	UIManager.getLookAndFeel().provideErrorFeedback(WordMLTextPane.this);
            return;
        }
        
        WordMLDocument doc = (WordMLDocument) getDocument();
        if (doc != null && content != null && content.length() > 0) {
        	int start = getSelectionStart();
        	int end = getSelectionEnd();           	
        	
            try {
            	int newCaretPos = start + content.length();
            	
            	AttributeSet inputAttrs = getInputAttributesML();
            	MutableAttributeSet attrs = 
            		(MutableAttributeSet) inputAttrs.copyAttributes();
            	if (inputAttrs.getResolveParent() != null) {
            		attrs.setResolveParent(inputAttrs.getResolveParent().copyAttributes());
            	}
            	
                doc.replace(start, (end - start), content, attrs);
                
                if (log.isDebugEnabled()) {
                	log.debug("replaceSelection(String): selection start=" + start
                		+ " end=" + end
                		+ " text=" + content);
                	DocUtil.displayStructure(doc);
                }
                
                setCaretPosition(newCaretPos);
                
            } catch (BadLocationException e) {
            	UIManager.getLookAndFeel().provideErrorFeedback(WordMLTextPane.this);
            	
		        WordMLEditor wmlEditor = 
		        	WordMLEditor.getInstance(WordMLEditor.class);
		        ResourceMap rm = wmlEditor.getContext().getResourceMap();
		        String title = 
		        	rm.getString("Application.edit.info.dialog.title");
		        String message =
		        	rm.getString("Application.edit.info.cannotEditMessage");
		        wmlEditor.showMessageDialog(
		        	title, message, JOptionPane.INFORMATION_MESSAGE);
		        
                if (log.isDebugEnabled()) {
                	log.debug("replaceSelection(String): selection start=" + start
                		+ " end=" + end
                		+ " text=" + content);
                	DocUtil.displayStructure(doc);
                }
            }
            
        }
    } //replaceSelection(String)

    public void replaceSelection(WordMLFragment fragment) {
        if (!isEditable()) {
        	UIManager.getLookAndFeel().provideErrorFeedback(WordMLTextPane.this);
            return;
        }
        
        WordMLDocument doc = (WordMLDocument) getDocument();
        if (doc != null && fragment != null) {
        	int start = getSelectionStart();
        	int end = getSelectionEnd();    
        	
			try {
				saveCaretText();
				
            	AttributeSet inputAttrs = getInputAttributesML();
            	MutableAttributeSet attrs = 
            		(MutableAttributeSet) inputAttrs.copyAttributes();
            	if (inputAttrs.getResolveParent() != null) {
            		attrs.setResolveParent(inputAttrs.getResolveParent().copyAttributes());
            	}
            	
				doc.replace(start, (end-start), fragment, attrs);

	            if (log.isDebugEnabled()) {
	            	log.debug("replaceSelection(WordMLFragment): selection start=" + start
	            		+ " end=" + end
	            		+ " fragment=" + fragment);
	            	DocUtil.displayStructure(doc);
	            }
	            
				end = start + fragment.getText().length();
				setCaretPosition(end);
				
			} catch (BadLocationException e) {
				e.printStackTrace();
				
            	UIManager.getLookAndFeel().provideErrorFeedback(WordMLTextPane.this);
            	
		        WordMLEditor wmlEditor = WordMLEditor.getInstance(WordMLEditor.class);            
		    	ResourceMap rm = wmlEditor.getContext().getResourceMap(getClass());

		        String title = 
		        	rm.getString("Application.edit.info.dialog.title");
				String message = 
					rm.getString("Application.edit.info.cannotPasteMessage");
				wmlEditor.showMessageDialog(title, message, JOptionPane.INFORMATION_MESSAGE);
				
	            if (log.isDebugEnabled()) {
	            	log.debug("replaceSelection(WordMLFragment): selection start=" + start
	            		+ " end=" + end
	            		+ " fragment=" + fragment);
	            	DocUtil.displayStructure(doc);
	            }
			}
			
        } //if (doc != null && fragment != null)
    } //replaceSelection(WordMLFragment)
    
    /**
     * Gets the input attributes for the pane.
     *
     * @return the attributes
     */
    public MutableAttributeSet getInputAttributesML() {
        return getWordMLEditorKit().getInputAttributesML();
    }

    public void saveCaretText() {
    	getWordMLEditorKit().saveCaretText();
    }
    
    public WordMLEditorKit getWordMLEditorKit() {
    	return (WordMLEditorKit) getEditorKit();
    }
    
    protected EditorKit createDefaultEditorKit() {
        return new WordMLEditorKit();
    }


}// WordMLTextPane class


















