package it.epocaricerca.standalone.continuityChange.model;

import java.util.List;

public class FileLine {

	private String firm;
	
	private String year;
	
	private List<String> citations;

	public String getFirm() {
		return firm;
	}

	public void setFirm(String firm) {
		this.firm = firm;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public List<String> getCitations() {
		return citations;
	}

	public void setCitations(List<String> citations) {
		this.citations = citations;
	}
}
