package it.epocaricerca.standalone.continuityChange.controller;
import org.springframework.stereotype.Component;

@Component
public class ExportState {
	
	private int counter = 0;
	
	private int value;
	
	public void increment() {
		counter++;
	}
	
	public int getCounter() {
		return this.counter;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public void reset() {
		this.counter = 0;
		this.value = 0;
	}
	
}
