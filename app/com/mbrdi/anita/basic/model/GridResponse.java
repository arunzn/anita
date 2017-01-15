package com.mbrdi.anita.basic.model;

import java.util.ArrayList;
import java.util.List;

public class GridResponse<T> {
	
	public GridResponse(){
	}
	
	public GridResponse(List<T> list) {
		rows = list;
		page = 1;
		total = rows.size();
		records = rows.size();
	}

	//current page
	public int page;
	
	//total records
	public int total;
	
	//page size
	public int records;
	
	//records for current page
	public List<T> rows = new ArrayList<>();
	
}
