package com.mbrdi.anita.basic.model;

import com.mbrdi.anita.basic.util.Util;
import org.mongojack.DBQuery;

import java.util.ArrayList;
import java.util.List;

public class GridRequest {
	
	//total records
	public int total;
	
	public int rows;
	
	//page size
	public int records;
	
	public String sidx;
	
	public String filterOp;
	
	public List<String> filterColumns = new ArrayList<String>();
	
	public List<String> filterValues = new ArrayList<>();

	public String sord;

	public int page;
	
	public Class klass;
	
	public String primary_key;
	
	public String infield;
	
	public String invalues;
	
	public String notinfield;

	public String notinvalues;

	public String btwfield;

	public List<String> btwvalues;
	
	public String ltfield;
	
	public String ltvalue;

	public String gtfield;
	
	public String gtvalue;
	
	public DBQuery.Query query;


	public String extractAndRemove(String field) {
		if (this.filterColumns.contains(field)) {
			int index = filterColumns.indexOf(field);
			String value = (String) filterValues.get(index);
			filterValues.remove(filterColumns.indexOf(field));
			filterColumns.remove(field);
			return Util.isNullOrEmpty(value) ? null : value;
		}
		return null;
	}

    public String valueOf(String key) {
        if(filterColumns != null && filterColumns.contains(key)) {
            return filterValues.get(filterColumns.indexOf(key));
        }
        return null;
    }

    public void removeField(String key) {
        filterValues.remove(filterColumns.indexOf(key));
        filterColumns.remove(key);
    }
}
