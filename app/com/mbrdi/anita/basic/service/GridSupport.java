package com.mbrdi.anita.basic.service;

import com.mbrdi.anita.basic.model.GridRequest;
import com.mbrdi.anita.basic.model.GridResponse;
import com.mbrdi.anita.basic.util.Util;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.DBSort;
import org.mongojack.JacksonDBCollection;
import play.Logger;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public abstract class GridSupport {
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    private static List<String> like_fields = Arrays.asList("name", "address", "route_name", "reg_no", "corp_name", "vehicle_type", "email");

	public static <T> GridResponse<T> fetch(Class klass, JacksonDBCollection<T, ?> dbCollection, GridRequest gridRequest) {

		int offset = gridRequest.page < 2 ? 0 : gridRequest.rows * (gridRequest.page - 1);

		GridResponse<T> response = new GridResponse<>();
		Query q = DBQuery.empty();

		try {
			for (int i = 0; i < gridRequest.filterColumns.size(); i++) {
				
				Class fieldClass = getField(klass, gridRequest.filterColumns.get(i));
				
				if (fieldClass == String.class) {
					if(gridRequest.filterColumns.get(i).endsWith("_id")) {
                        q.is(gridRequest.filterColumns.get(i), gridRequest.filterValues.get(i));
					}
					else {
                        // Dont use regular expression for like compare fields
                        if(like_fields.contains(gridRequest.filterColumns.get(i))) {
                            q.regex(gridRequest.filterColumns.get(i), Pattern.compile(gridRequest.filterValues.get(i), Pattern.CASE_INSENSITIVE));
                        } else {
                            q.is(gridRequest.filterColumns.get(i), gridRequest.filterValues.get(i));
                        }
					}
				} else if (fieldClass == Integer.class || fieldClass.getName().equals("int")) {
					if (q == null)
						q = DBQuery.is(gridRequest.filterColumns.get(i), Integer.parseInt(gridRequest.filterValues.get(i).toString().trim()));
					else {
                        Integer val = Util.toInteger(gridRequest.filterValues.get(i));
                        if(val != null)
                            q.is(gridRequest.filterColumns.get(i), val);
                        else {
                            Logger.error("Incorrect Value for " +  gridRequest.filterColumns.get(i) + " value:" + gridRequest.filterValues.get(i));
                        }
                    }
				} else if (fieldClass == Long.class || fieldClass.getName().equals("long")) {
                    if (q == null)
                        q = DBQuery.is(gridRequest.filterColumns.get(i), Long.parseLong(gridRequest.filterValues.get(i).toString().trim()));
                    else
                        q.is(gridRequest.filterColumns.get(i), Long.parseLong(gridRequest.filterValues.get(i).toString().trim()));
				} else if (fieldClass == Double.class || fieldClass.getName().equals("double")) {
                    if (q == null)
                        q = DBQuery.is(gridRequest.filterColumns.get(i), Double.parseDouble(gridRequest.filterValues.get(i).toString().trim()));
                    else
                        q.is(gridRequest.filterColumns.get(i), Double.parseDouble(gridRequest.filterValues.get(i).toString().trim()));
                }
				else if (fieldClass == Boolean.class || fieldClass.getName().equals("boolean")) {
                    if (q == null)
                        q = DBQuery.is(gridRequest.filterColumns.get(i), Boolean.parseBoolean(gridRequest.filterValues.get(i).toString().trim()));
                    else
                        q.is(gridRequest.filterColumns.get(i), Boolean.parseBoolean(gridRequest.filterValues.get(i).toString().trim()));
				}
				
			}

			if (gridRequest.infield != null && gridRequest.invalues != null) {
				q.in(gridRequest.infield, Util.csvToSet(gridRequest.invalues));
			}

			if (gridRequest.notinfield != null && gridRequest.notinvalues != null) {
				q.notIn(gridRequest.notinfield, Util.csvToSet(gridRequest.notinvalues));
			}

			if (gridRequest.btwfield != null && gridRequest.btwvalues != null) {
				Object from = gridRequest.btwvalues.get(0);
				Object to = gridRequest.btwvalues.get(1);
				Class fieldClass = getField(klass, gridRequest.btwfield);
				if(fieldClass.equals(Date.class)) {
					to = dateFormat.parse(to.toString());
					from = dateFormat.parse(from.toString());
				}
				else if (fieldClass.isPrimitive() && fieldClass.getName().equals("int")){
					to = new Integer(to.toString());
					from = new Integer(from.toString());
				}
				else if (fieldClass.isAssignableFrom(Number.class)){
					to = new Long(to.toString());
					from = new Long(from.toString());
				}
				q.lessThan(gridRequest.btwfield, to).greaterThanEquals(gridRequest.btwfield, from);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		if(gridRequest.query != null && !q.conditions().isEmpty())
			q.and(gridRequest.query);
		else if(gridRequest.query != null)
			q = gridRequest.query;
		
		
		response.total = dbCollection.find(q).count();

		if (gridRequest.sidx != null) {
			if ("desc".equalsIgnoreCase(gridRequest.sord))
				response.rows = dbCollection.find(q).sort(DBSort.desc(gridRequest.sidx)).skip(offset).limit(gridRequest.rows).toArray();
			else
				response.rows = dbCollection.find(q).sort(DBSort.asc(gridRequest.sidx)).skip(offset).limit(gridRequest.rows).toArray();
		} else {
			response.rows = dbCollection.find(q).skip(offset).limit(gridRequest.rows).toArray();
		}

		response.page = gridRequest.page;
		response.records = response.total;
		response.total = new Double(Math.ceil(response.records / new Double(gridRequest.rows))).intValue();

		return response;
	}
	
	private static Class getField(Class klass, String fieldName) throws Exception {
		while(fieldName.contains(".")){
			Field field = klass.getField(fieldName.substring(0,fieldName.indexOf('.')));
			fieldName = fieldName.substring(fieldName.indexOf('.')+1);
			klass = field.getType();
		}
		return klass.getField(fieldName).getType();
	}

}
