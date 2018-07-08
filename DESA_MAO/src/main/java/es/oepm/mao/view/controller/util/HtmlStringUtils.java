package es.oepm.mao.view.controller.util;

import java.io.Serializable;

import org.springframework.web.util.HtmlUtils;


public class HtmlStringUtils implements Serializable {

	private static final long serialVersionUID = -2783457818110460738L;
	
	public static String ERRemoveHtmlTags = "\\<[^\\>]*\\>";
	
	public static String getContentBetweenHtmlTags(String htmlContent){
		String result = null;
		
	     String[] splitStr = htmlContent.split(ERRemoveHtmlTags);
	     for (String string : splitStr) {
			if(string.contains("<") || string.contains(">") || string.isEmpty()){}
			else{
				if(result == null)
					result = string;
				else
					result += " " + string;
			}
		}
	    result = HtmlUtils.htmlUnescape(result);
		return result;
	}
}
