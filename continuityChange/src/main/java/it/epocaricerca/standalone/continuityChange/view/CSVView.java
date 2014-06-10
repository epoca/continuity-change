package it.epocaricerca.standalone.continuityChange.view;

import java.io.BufferedWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.AbstractView;

public class CSVView extends AbstractView {
	
	private static final char FIELD_SEPARATOR = ',';
	private static final char ROW_SEPARATOR = '\n';
	private static final String DOCUMENTS_EXPORT_HEADER = "Family Number, Patent Number, Kind, Publication Date, XPN, Title, Abstract, Priority Details, Applicants, Inventors, Classes, States,";
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String fileName = (String) model.get("fileName");
		
//		if (fileName.contains("documents")) {
//			writeDocumentsData(model, response);
//		}
//		else {
			writeChartData(model, response);
//		}
		
	}
	
	private void writeChartData(Map<String, Object> model, HttpServletResponse response) throws Exception {
		
		BufferedWriter writer = new BufferedWriter(response.getWriter());
		
		String fileName = (String) model.get("fileName");
		
		response.setHeader("Content-Disposition","attachment; filename=\""+fileName+"\"");
		
		List<Object[]> dataSource = (List<Object[]>) model.get("dataSource");
		
		for (Object[] row : dataSource) {
			int rowLength = row.length;
			for (int i = 0; i <= rowLength; i++) {
				if (i < rowLength) {
					try {
						writer.write( row[i].toString() );
					} catch (NullPointerException e) {
						writer.write( " " );
					}
					
					writer.write( FIELD_SEPARATOR );
				}
				else if (i == row.length) {
					//writer.write( ROW_SEPARATOR );
					writer.newLine();
				}
			}
			//writer.newLine();
		}
		
		writer.flush();
		writer.close();
	}
	
//	private void writeDocumentsData(Map<String, Object> model, HttpServletResponse response) throws Exception {
//		
//		BufferedWriter writer = new BufferedWriter(response.getWriter());
//		
//		String fileName = (String) model.get("fileName");
//		
//		response.setHeader("Content-Disposition","attachment; filename=\""+fileName+"\"");
//		
//		Map<String, List<Patent>> dataSource = (Map<String, List<Patent>>) model.get("dataSource");
//		
//		writer.write( DOCUMENTS_EXPORT_HEADER );
//		writer.newLine();
//		
//		for (Map.Entry<String, List<Patent>> entry : dataSource.entrySet()) {
//		    String familyNumber = entry.getKey();
//		    List<Patent> patents = entry.getValue();
//		    for( Patent p : patents ) {
//		    	writer.write( familyNumber ); writer.write( FIELD_SEPARATOR );
//		    	writer.write( p.getPublicationNumber() ); writer.write( FIELD_SEPARATOR );
//		    	writer.write( p.getKindCode() ); writer.write( FIELD_SEPARATOR );
//		    	writer.write( dateFormat.format(p.getPublicationDate()) ); writer.write( FIELD_SEPARATOR );
//		    	writer.write( p.getStandardizedPatentNumber() ); writer.write( FIELD_SEPARATOR );
//		    	writer.write( "\""+p.getTitle()+"\"" ); writer.write( FIELD_SEPARATOR );
//		    	writer.write( "\""+p.getEnAbstract()+"\"" ); writer.write( FIELD_SEPARATOR );
//		    	String priorityDetails = "";
//		    	for (PriorityDetail pd : p.getPriorityDetail()) {
//		    		priorityDetails = priorityDetails.concat(pd.getPriorityNumber() + " " + dateFormat.format(pd.getPriorityDate()) + "; ");
//		    	}
//		    	writer.write( priorityDetails ); writer.write( FIELD_SEPARATOR );
//		    	String applicants = "";
//		    	for (EPR epr : p.getEprs()) {
//		    		applicants = applicants.concat(epr.getAteneo() + "; ");
//		    	}
//		    	writer.write( "\""+applicants+"\"" ); writer.write( FIELD_SEPARATOR );
//		    	String inventors = "";
//		    	for (Inventor inventor : p.getInventors()) {
//		    		inventors = inventors.concat(inventor.getFullName() + "; ");
//		    	}
//		    	writer.write( "\""+inventors+"\"" ); writer.write( FIELD_SEPARATOR );
//		    	String classes = "";
//		    	for (IPC ipc : p.getInternationalPatentClasses()) {
//		    		classes = classes.concat(ipc.getCode() + " ");
//		    	}
//		    	writer.write( "\""+classes+"\"" ); writer.write( FIELD_SEPARATOR );
//		    	String states = "";
//		    	for (DesignatedState ds : p.getDesignatedStates()) {
//		    		states = states.concat(ds.getCode() + " ");
//		    	}
//		    	writer.write( states ); writer.write( FIELD_SEPARATOR );
//		    	writer.newLine();
//		    	
//		    }
//		}
//		
//		writer.flush();
//		writer.close();
//		
//	}
}
