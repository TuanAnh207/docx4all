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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.apache.log4j.Logger;
import org.docx4all.xml.ObjectFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STJc;
import org.docx4j.wml.Underline;
import org.docx4j.wml.PPr.PStyle;
import org.docx4j.wml.RPr.RStyle;
import org.docx4j.wml.Styles.LatentStyles.LsdException;

/**
 *	@author Jojada Tirtowidjojo - 19/02/2008
 */
public class StyleSheet extends StyleContext {
	private static Logger log = Logger.getLogger(StyleSheet.class);

	public final static String LATENT_STYLES = "latentStyles";
	public final static String LSD_EXCEPTIONS = "lsdExceptions";
	public final static String UI_STYLES = "uiStyles";
	public final static String ID_STYLES = "idStyles";
	
	public final static String PARAGRAPH_ATTR_VALUE = "paragraph";
	public final static String CHARACTER_ATTR_VALUE = "character";
	
    private static StyleSheet defaultStyleSheet;
    
    public static final StyleSheet getDefaultStyleSheet() {
        if (defaultStyleSheet == null) {
        	defaultStyleSheet = new StyleSheet();
        	defaultStyleSheet.setWordprocessingMLPackage(ObjectFactory.createEmptyDocumentPackage());
        }
        return defaultStyleSheet;
    }

	public final static void addAttributes(MutableAttributeSet attrs, PPr pPr) {
		//ALIGNMENT attribute
		Jc jc = pPr.getJc();
		if (jc != null) {
			if (jc.getVal() == STJc.LEFT) {
				StyleConstants.setAlignment(
						attrs,
						StyleConstants.ALIGN_LEFT);
			} else if (jc.getVal() == STJc.RIGHT) {
				StyleConstants.setAlignment(
						attrs,
						StyleConstants.ALIGN_RIGHT);
			} else if (jc.getVal() == STJc.CENTER) {
				StyleConstants.setAlignment(
						attrs,
						StyleConstants.ALIGN_CENTER);
			} else if (jc.getVal() == STJc.BOTH) {
				StyleConstants.setAlignment(
						attrs,
						StyleConstants.ALIGN_JUSTIFIED);
			}
		}
		
		PStyle pStyle = pPr.getPStyle();
		if (pStyle != null) {
			attrs.addAttribute(WordMLStyleConstants.PStyleAttribute, pStyle.getVal());
		}
	}
	
	public final static void addAttributes(MutableAttributeSet attrs, RPr rPr) {
		//BOLD Attribute
		BooleanDefaultTrue bdt = rPr.getB();
		if (bdt != null && bdt.isVal()) {
			StyleConstants.setBold(attrs, Boolean.TRUE);
		}

		//ITALIC Attribute
		bdt = rPr.getI();
		if (bdt != null && bdt.isVal()) {
			StyleConstants.setItalic(attrs, Boolean.TRUE);
		}
		
		//UNDERLINE Attribute
		//TODO: To support underline style and color
		if (hasUnderlineSet(rPr)) {
			StyleConstants.setUnderline(attrs, Boolean.TRUE);
		}
		
		//FONT FAMILY Attribute
		RPr.RFonts rfonts = rPr.getRFonts();
		if (rfonts != null) {
			String strValue = rfonts.getAscii();
			if (strValue != null) {
				StyleConstants.setFontFamily(attrs, strValue);
			}
		}
		
		//FONT SIZE Attribute
		HpsMeasure sz = rPr.getSz();
		if (sz != null && sz.getVal() != null) {
			StyleConstants.setFontSize(attrs, sz.getVal().intValue());
		}
		
		RStyle rStyle = rPr.getRStyle();
		if (rStyle != null) {
			attrs.addAttribute(WordMLStyleConstants.RStyleAttribute, rStyle.getVal());
		}
	}
	
	public final static boolean hasUnderlineSet(RPr rPr) {
		boolean hasUnderlineSet = false;
		
		Underline u = rPr.getU();
		if (u != null) {
			String none = null;
			for (String s : u.getVal()) {
				if (s.equalsIgnoreCase("none")) {
					none = s;
				}
			}
			hasUnderlineSet = (none == null && !u.getVal().isEmpty());
		}
		
		return hasUnderlineSet;
	}

	private WordprocessingMLPackage docPackage;
	
	public StyleSheet() {
		super();
	}
	
	public void setWordprocessingMLPackage(WordprocessingMLPackage docPackage) {
		this.docPackage = docPackage;
		org.docx4j.wml.Styles docxStyles = 
			(org.docx4j.wml.Styles)
				docPackage.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement();
		initDefaultStyle(docxStyles);
		initLatentStyles(docxStyles);
		initStyles(docxStyles);
	}
	
	public WordprocessingMLPackage getWordprocessingMLPackage() {
		return this.docPackage;
	}
	
	public List<Style> getUIStyles() {
		Style uiStyles = getStyle(UI_STYLES);
		
		List<Style> theList = new ArrayList<Style>(uiStyles.getAttributeCount());
		
		Enumeration<?> tenum = uiStyles.getAttributeNames();
		while (tenum.hasMoreElements()) {
			Object obj = tenum.nextElement();
			
			obj = uiStyles.getAttribute(obj.toString());
			if (obj instanceof Style) {
				theList.add((Style) obj);
			}
		}
		
		Collections.sort(theList, new UIStyleComparator());
		return theList;
	}
	
	public String[] getUIStyleNames() {
		List<Style> uiStyles = getUIStyles();
		String[] theNames = new String[uiStyles.size()];
		int i=0;
		for (Style s: uiStyles) {
			theNames[i++] = s.getName();
		}
		return theNames;
	}
	
	public Style getUIStyle(String styleUiName) {
		Style s = getStyle(UI_STYLES);
		return getChildStyle(s, styleUiName);
	}
	
	public Style getIDStyle(String styleId) {
		Style s = getStyle(ID_STYLES);
		return getChildStyle(s, styleId);
	}
	
	public Style getReferredStyle(String name) {
		Style s = getUIStyle(name);
		if (s == null) {
			s = getIDStyle(name);
		}
		return s;
	}
	
	protected void initDefaultStyle(org.docx4j.wml.Styles docxStyles) {
		Style defaultStyle = getStyle(DEFAULT_STYLE);
		
		if (docxStyles.getDocDefaults().getPPrDefault() != null) {
			PPr pPr = docxStyles.getDocDefaults().getPPrDefault().getPPr();
			if (pPr != null) {
				StyleSheet.addAttributes(defaultStyle, pPr);
			}
		}
		
		if (docxStyles.getDocDefaults().getRPrDefault() != null) {
			RPr rPr = docxStyles.getDocDefaults().getRPrDefault().getRPr();
			if (rPr != null) {
				StyleSheet.addAttributes(defaultStyle, rPr);
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("initDefaultStyle(): defaultStyle=" + defaultStyle);
		}
	}
	
	protected void initLatentStyles(org.docx4j.wml.Styles docxStyles) {
		Style defaultStyle = getStyle(DEFAULT_STYLE);
		Style latentStyles = addStyle(LATENT_STYLES, defaultStyle);
		
		org.docx4j.wml.Styles.LatentStyles latent = docxStyles.getLatentStyles();
		
		if (latent == null) {
			return;
		}
				
		//Currently still supporting uipriority and qformat attributes.
		latentStyles.addAttribute(
			WordMLStyleConstants.DocxObjectAttribute,
			latent);
		latentStyles.addAttribute(
			WordMLStyleConstants.UiPriorityAttribute,
			(latent.getDefUIPriority() == null) 
				? new Integer(99)
				: new Integer(latent.getDefUIPriority().intValue()));
		latentStyles.addAttribute(
			WordMLStyleConstants.QFormatAttribute,
			Boolean.valueOf(latent.isDefQFormat()));
		
		List<LsdException> list = latent.getLsdException();
		if (list != null) {
			Style lsdExceptions = addChildStyle(latentStyles, LSD_EXCEPTIONS);
			
			int i=0;
			for (LsdException lsd: list) {
				//An <lsdException> specifies the style name 
				//and NOT the style Id.
				Style temp = addChildStyle(lsdExceptions, lsd.getName());
				temp.addAttribute(
					WordMLStyleConstants.DocxObjectAttribute, lsd);
				
				if (lsd.getUiPriority() != null) {
					temp.addAttribute(
						WordMLStyleConstants.UiPriorityAttribute,
						new Integer(lsd.getUiPriority().intValue()));
				}
				temp.addAttribute(
					WordMLStyleConstants.QFormatAttribute,
					Boolean.valueOf(lsd.isQFormat()));
				
				if (log.isDebugEnabled()) {
					log.debug("initLatentStyles(): LsdException Style[" + (i++) +"]=" + temp);
				}
			}
		}
	}
	
	protected void initStyles(org.docx4j.wml.Styles docxStyles) {
		List<org.docx4j.wml.Styles.Style> styleList = docxStyles.getStyle();
		if (styleList.isEmpty()) {
			return;
		}
		
		Style defaultStyle = getStyle(DEFAULT_STYLE);
		Style latentStyles = getStyle(LATENT_STYLES);
		Style lsdExceptions = getChildStyle(latentStyles, LSD_EXCEPTIONS);
		
		Style idStyles = addStyle(ID_STYLES, defaultStyle);
		Style uiStyles = addStyle(UI_STYLES, defaultStyle);
		
		int i=0;
		List<Style> stylesWithBasedOn = new ArrayList<Style>();
		for (org.docx4j.wml.Styles.Style st: styleList) {			
			//Latent styles comprises those styles known to an application.
			//Therefore, all styles in styleList are descendants of latentStyles.
			Style temp = null;
			String uiName = st.getStyleId();
			if (st.getName() != null) {
				//Check whether st is listed in lsdExceptions.
				//Remember that a <lsdException> specifies the style name 
				//and NOT the style Id.
				uiName = st.getName().getVal();
				temp = getChildStyle(lsdExceptions, uiName);
			}
			if (temp == null) {
				temp = new NamedStyle(st.getStyleId(), latentStyles);
			}
			
			temp.addAttribute(
					WordMLStyleConstants.StyleUINameAttribute, uiName);
			temp.addAttribute(
					WordMLStyleConstants.DocxObjectAttribute, st);
			temp.addAttribute(
					WordMLStyleConstants.StyleIdAttribute, st.getStyleId());
			idStyles.addAttribute(st.getStyleId(), temp);
			
			if (st.getPPr() != null) {
				StyleSheet.addAttributes(temp, st.getPPr());
			}
			if (st.getRPr() != null) {
				StyleSheet.addAttributes(temp, st.getRPr());
			}
			if (st.getQFormat() != null) {
				temp.addAttribute(
					WordMLStyleConstants.QFormatAttribute
					, Boolean.valueOf(st.getQFormat().isVal()));
			}
			
			String type = (st.getType() == null) ? PARAGRAPH_ATTR_VALUE : st.getType();
			temp.addAttribute(
				WordMLStyleConstants.StyleTypeAttribute, type);

			if (st.getLink() != null) {
				//A link value is a style id.
				//Check whether the link style has already been created.
				Style target = getChildStyle(idStyles, st.getLink().getVal());
				if (target != null) {
					org.docx4j.wml.Styles.Style targetDocxStyles =
						(org.docx4j.wml.Styles.Style)
							target.getAttribute(WordMLStyleConstants.DocxObjectAttribute);
					String targetType = 
						(String) target.getAttribute(WordMLStyleConstants.StyleTypeAttribute);
					if (PARAGRAPH_ATTR_VALUE.equalsIgnoreCase(type)
							&& CHARACTER_ATTR_VALUE.equalsIgnoreCase(targetType)
							&& targetDocxStyles.getRPr() != null) {
						StyleSheet.addAttributes(temp, targetDocxStyles.getRPr());
						
					} else if (CHARACTER_ATTR_VALUE.equalsIgnoreCase(type)
							&& PARAGRAPH_ATTR_VALUE.equalsIgnoreCase(targetType)
							&& targetDocxStyles.getPPr() != null) {
						StyleSheet.addAttributes(temp, targetDocxStyles.getPPr());
						
					} else {
						//Specs says to ignore this
					}
				}
			} //if (st.getLink() != null)
			
			if (st.getBasedOn() != null) {
				stylesWithBasedOn.add(temp);
			}
			
			Boolean qformat = (Boolean) temp.getAttribute(WordMLStyleConstants.QFormatAttribute);
			if (qformat.booleanValue()) {
				uiStyles.addAttribute(uiName, temp);
			}

			//FIXME: st.isDefault() is still buggy at this moment.
			//Therefore, we check whether uiName starts with "Normal".
			//Fix this condition when st.isDefault() has been fixed.
			if (st.isDefault() 
				&& qformat.booleanValue()
				&& uiName.startsWith("Normal")
				&& PARAGRAPH_ATTR_VALUE.equalsIgnoreCase(type)) {
				defaultStyle.addAttribute(WordMLStyleConstants.DefaultParagraphStyleNameAttribute, uiName);
			}
			
			if (log.isDebugEnabled()) {
				log.debug("initStyles(): style[" + (i++) + "]=" + temp);
			}
			
		} //for (org.docx4j.wml.Styles.Style st: styleList)
		styleList = null;
		
		for (Style style: stylesWithBasedOn) {
			
			if (log.isDebugEnabled()) {
				log.debug("initStyles(): style with BasedOn[" + (i++) + "]=" + style);
			}
			
			org.docx4j.wml.Styles.Style wmlStyle = WordMLStyleConstants.getDocxStyle(style);
			Style parent = getChildStyle(idStyles, wmlStyle.getBasedOn().getVal());
			if (parent != null) {
				String type = (String) style.getAttribute(WordMLStyleConstants.StyleTypeAttribute);
				String parentType = (String) parent.getAttribute(WordMLStyleConstants.StyleTypeAttribute);
				if (type.equals(parentType)) {
					if (PARAGRAPH_ATTR_VALUE.equals(type)
						|| CHARACTER_ATTR_VALUE.equals(type)) {
						style.setResolveParent(parent);
					}
				}
			}
		}
	}
	
	private Style addChildStyle(Style parent, String childName) {
		Style child = new NamedStyle(childName, parent);
		parent.addAttribute(childName, child);
		return child;
	}
	
	private Style getChildStyle(Style parent, String childName) {
		return (Style) parent.getAttribute(childName);
	}
	
	private class UIStyleComparator implements Comparator<Style> {
	    public int compare(Style s1, Style s2) {
	    	Integer p1 = (Integer) s1.getAttribute(WordMLStyleConstants.UiPriorityAttribute);
	    	Integer p2 = (Integer) s2.getAttribute(WordMLStyleConstants.UiPriorityAttribute);
	    	
	    	int sign = p1.compareTo(p2);
	    	if (sign == 0) {
	    		sign = s1.getName().compareToIgnoreCase(s2.getName());
	    	}
	    	
	    	return sign;
	    }
	} //StyleUIComparator inner class
	
}// StyleSheet class



















