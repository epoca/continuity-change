package it.epocaricerca.standalone.continuityChange.controller;
import org.springframework.stereotype.Component;

@Component
public class ProgressState {
	
	private int exportCounter = 0;
	
	private int exportValue;
	
	private int importCounter = 0;
	
	private int importValue;
	
	private int totalImportLines;
	
	public void incrementExportCounter() {
		exportCounter++;
	}
	
	public int getExportCounter() {
		return this.exportCounter;
	}

	public int getExportValue() {
		return exportValue;
	}

	public void setExportValue(int value) {
		this.exportValue = value;
	}
	
	public void incrementImportCounter() {
		importCounter++;
	}
	
	public int getImportCounter() {
		return this.importCounter;
	}

	public int getImportValue() {
		return importValue;
	}

	public void setImportValue(int value) {
		this.importValue = value;
	}
	
	public void resetExportValues() {
		this.exportCounter = 0;
		this.exportValue = 0;
	}
	
	public void resetImportValues() {
		this.importCounter = 0;
		this.importValue = 0;
		this.totalImportLines = 0;
	}

	public int getTotalImportLines() {
		return totalImportLines;
	}

	public void setTotalImportLines(int totalImportLines) {
		this.totalImportLines = totalImportLines;
	}
	
}
