package hbs.sff.no;

import javax.swing.DefaultListSelectionModel;

public class PartialSelectionModel extends DefaultListSelectionModel {
	private static final long serialVersionUID = 1L;
	public int getSelectionMode() { return SINGLE_INTERVAL_SELECTION; } 
//	public void setSelectionInterval(int start, int end){
//		for(int i = start; i <= end; i++){
//			if(start == 0){}
//			else super.setSelectionInterval(start, end);
//		}
//	}
//	public void addSelectionInterval(int start, int end){
//		for(int i = start; i <= end; i++){
//			if(start == 0){} 
//			else super.addSelectionInterval(start, end);
//		}
//	}
}
