package it.epocaricerca.standalone.continuityChange.transfer;

public class Notification {

	private int percentageProgress;
	
	private String message;

	public Notification(int percentageProgress, String message) {
		super();
		this.percentageProgress = percentageProgress;
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
	
}
