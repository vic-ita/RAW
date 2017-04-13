/*******************************************************************************
 *  Copyright 2017 Vincenzo-Maria Cappelleri <vincenzo.cappelleri@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package raw.dht.webGui;

import java.util.LinkedList;

/**
 * Build a /(simple) html
 * page.
 * 
 * @author vic
 *
 */
public class HtmlBuilder {
	
	private LinkedList<String> elements;
	
	private String footer;
	
	/**
	 * Builds an (empty) page with the given title
	 * 
	 * @param title the page title
	 */
	public HtmlBuilder(String title) {
		elements = new LinkedList<>();
		String header = "<html></head><title>"+title+"</title></head><body>";
		elements.add(header);
		
		footer = "</body></html>";
	}
	
	/**
	 * @return a string with all elements inserted in this {@link HtmlBuilder}
	 */
	public String build(){
		elements.add(footer);
		StringBuilder sb = new StringBuilder();
		while (!elements.isEmpty()) {
			sb.append(elements.removeFirst());
		}
		return sb.toString();
	}
	
	public HtmlBuilder br() {
		elements.add("<br>");
		return this;
	}
	
	public HtmlBuilder hr() {
		elements.add("<hr>");
		return this;
	}
	
	public HtmlBuilder openBold() {
		elements.add("<b>");
		return this;
	}
	
	public HtmlBuilder closeBold() {
		elements.add("</b>");
		return this;
	}
	
	public HtmlBuilder openItalic() {
		elements.add("<i>");
		return this;
	}
	
	public HtmlBuilder closeItalic() {
		elements.add("</i>");
		return this;
	}
	
	public HtmlBuilder openPre() {
		elements.add("<pre>");
		return this;
	}
	
	public HtmlBuilder closePre() {
		elements.add("</pre>");
		return this;
	}
	
	public HtmlBuilder openH1() {
		elements.add("<h1>");
		return this;
	}
	
	public HtmlBuilder closeH1() {
		elements.add("</h1>");
		return this;
	}
	
	public HtmlBuilder openH2() {
		elements.add("<h2>");
		return this;
	}
	
	public HtmlBuilder closeH2() {
		elements.add("</h2>");
		return this;
	}
	
	public HtmlBuilder openH3() {
		elements.add("<h3>");
		return this;
	}
	
	public HtmlBuilder closeH3() {
		elements.add("</h3>");
		return this;
	}

	public HtmlBuilder openH4() {
		elements.add("<h4>");
		return this;
	}
	
	public HtmlBuilder closeH4() {
		elements.add("</h4>");
		return this;
	}
	
	public HtmlBuilder openH5() {
		elements.add("<h5>");
		return this;
	}
	
	public HtmlBuilder closeH5() {
		elements.add("</h5>");
		return this;
	}
	
	public HtmlBuilder openH6() {
		elements.add("<h6>");
		return this;
	}
	
	public HtmlBuilder closeH6() {
		elements.add("</h6>");
		return this;
	}
	
	public HtmlBuilder text(String text) {
		elements.add(text);
		return this;
	}
	
	public HtmlBuilder link(String destination, String text) {
		return linkBuilder(destination, text, false);
	}
	
	public HtmlBuilder linkToNewTab(String destination, String text) {
		return linkBuilder(destination, text, true);
	}
	
	private HtmlBuilder linkBuilder(String destination, String text, boolean toNewTab) {
		String a ="<a href=\""+destination+"\"";
		if(toNewTab){
			a = a+" target=\"_blank\"";
		}
		a = a+">"+text+"</a>";
		elements.add(a);
		return this;
	}
	
	public HtmlBuilder openForm(String action, boolean methodPost) {
		String method;
		if(methodPost){
			method = "POST";
		} else {
			method = "GET";
		}
		elements.add("<form action=\""+action+"\" method=\""+method+"\">");
		return this;
	}
	
	public HtmlBuilder closeForm() {
		elements.add("</form>");
		return this;
	}
	
	public HtmlBuilder inputTextForm(String name, String label){
		return inputTextFormBuilder(name, label, null);
	}
	
	public HtmlBuilder inputTextForm(String name, String label, String value){
		return inputTextFormBuilder(name, label, value);
	}
	
	public HtmlBuilder inputRadioForm(String name, String label, String value){
		return inputRadioForm(name, label, value, false);
	}
	
	public HtmlBuilder inputRadioForm(String name, String label, String value, boolean checked){
		String input = "<input type=\"radio\" name=\""+name+"\" value=\""+value+"\" ";
		if(checked){
			input = input+"checked";
		}
		input = input+"> "+label;
		elements.add(input);
		return this;
	}
	
	private HtmlBuilder inputTextFormBuilder(String name, String label, String value){
		String input = label+"<br><input type=\"text\" name=\""+name+"\"";
		if(value != null){
			input = input + " value\""+value+"\"";
		}
		input = input+">";
		elements.add(input);
		return this;
	}
	
	public HtmlBuilder submitButton(String label) {
		elements.add("<input type=\"submit\" value=\""+label+"\">");
		return this;
	}
	
	public HtmlBuilder space() {
		elements.add("&nbsp");
		return this;
	}
	
}
