package it.epocaricerca.standalone.continuityChange.transfer;

public class Notification {

	private int percentageProgress;
	
	private String message;
	
	private int totalImportLines;
	
	private int numOfProcessedLines;

	public Notification(int percentageProgress, int totalImportLines, int numOfProcessedLines, String message) {
		super();
		this.percentageProgress = percentageProgress;
		this.totalImportLines = totalImportLines;
		this.numOfProcessedLines = numOfProcessedLines;
		this.message = message;
	}

	public int getPercentageProgress() {
		return percentageProgress;
	}

	public void setPercentageProgress(int percentageProgress) {
		this.percentageProgress = percentageProgress;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getTotalImportLines() {
		return totalImportLines;
	}

	public void setTotalImportLines(int totalImportLines) {
		this.totalImportLines = totalImportLines;
	}

	public int getNumOfProcessedLines() {
		return numOfProcessedLines;
	}

	public void setNumOfProcessedLines(int numOfProcessedLines) {
		this.numOfProcessedLines = numOfProcessedLines;
	}
	
}
