package com.mbrdi.anita.basic.util;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import play.Play;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

public class ExcelUtil {

    private static String macro_xlsm_folder = Play.application().configuration().getString("macro_xlsm_folder", null);


	private static boolean isNotEmptyRow(XSSFRow row, int size) {
		if (row == null) {
	        return false;
	    }
	    if (row.getLastCellNum() <= 0) {
	        return false;
	    }
	    for (int cellNum = 0; cellNum < size; cellNum++) {
	        Cell cell = row.getCell(cellNum);
	        if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK && StringUtils.isNotBlank(cell.toString())) {
	            return true;
	        }
	    }
	    return false;
	}



	/**
	 * This provides values in our required format.
	 */
	private static Object convertValueType(String pojoValue, Class klass, String pojoField) throws NoSuchFieldException, SecurityException {

		Field field = klass.getField(pojoField);

		if (field.getType().equals(Boolean.class)) {
			return Boolean.parseBoolean(pojoValue);
		} else if (field.getType().equals(Integer.class)) {
			return Integer.parseInt(pojoValue);
		} else if (field.getType().equals(Double.class)) {
			// for time fields replace : with .
			return Double.parseDouble(pojoValue.replace(":", "."));
		} else if (field.getType().equals(Long.class)) {
			return Long.parseLong(pojoValue);
		}
		// else if(field.getType().equals(Date.class)){
		// return Util.(pojoValue);
		// }

		return null;
	}

	@SuppressWarnings({ "resource", "rawtypes" })
	public static List readXLS(byte[] data, Class klass, String[] fields) {

		try {
			XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(data));

			// Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);

			Map<Integer, Class> datatypeMap = new HashMap<Integer, Class>();

			// Map<Integer, Field> columnFieldMap = new HashMap<Integer,
			// Field>();
			for (int i = 0; i < fields.length; i++) {
				try {
					// Field field = klass.getDeclaredField(fields[i]);
					// if(field != null){
					// columnFieldMap.put(i, field);
					// datatypeMap.put(i, field.getType());
					// }
					datatypeMap.put(i, getDataType(klass, fields[i]));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			List list = new LinkedList<>();
			// skip header row
			Iterator rows = sheet.rowIterator();
			rows.next();
			while (rows.hasNext()) {

				try {
					Object returnObject = klass.newInstance();

					XSSFRow row = (XSSFRow) rows.next();
					for (int i = 0; i < fields.length; i++) {
						XSSFCell cell = row.getCell(i);
						// columnFieldMap.get(i).set(returnObject,getCellValue(cell,datatypeMap.get(i),fields[i]));
						setFieldValue(klass, returnObject, getCellValue(cell, datatypeMap.get(i), fields[i]), fields[i]);
					}
					list.add(returnObject);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return list;

		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
	}

	private static void setFieldValue(Class klass, Object returnObject, Object cellValue, String fieldName) throws Exception {
		if(cellValue == null)
			return;

		if (!fieldName.contains(".")) {
			Field field = klass.getField(fieldName);// getDeclaredField(fieldName);
			if("addressLine".equals(fieldName) && field.get(returnObject) != null){
				cellValue = field.get(returnObject).toString() +"," + cellValue;
			}
			if (!field.getType().equals(cellValue.getClass())){
				if(field.getType() == Integer.class){
					cellValue = Util.intValue(cellValue.toString());
				}
				else if(field.getType() == Long.class){
					cellValue = Util.longValue(cellValue.toString());
				}
				else if(field.getType() == Double.class){
					cellValue = Util.doubleValue(cellValue.toString());
				}
			}
			field.set(returnObject, cellValue);
		} else {
			Field field = klass.getDeclaredField(fieldName.substring(0, fieldName.indexOf(".")));
			Object midObj = field.get(returnObject);
			if (midObj == null || field.getType().equals(Set.class)) {
				if(field.getType().equals(Set.class)) {
					Set m = null;
					if(midObj == null)
						m = new HashSet<>();
					else
						m = ((Set)midObj);
					
				}
				else {
					midObj = field.getType().newInstance();
					field.set(returnObject, midObj);
				}
			}
			
			if(field.getType().equals(Set.class)) {
				if(!((Set)midObj).isEmpty()) 
				midObj = ((Set)midObj).iterator().next();
			} 
			
			setFieldValue(field.getType(), midObj, cellValue, fieldName.substring(fieldName.indexOf(".") + 1));
		}
	}

	private static Class getDataType(Class klass, String fieldName) throws NoSuchFieldException, SecurityException {
		if (!fieldName.contains(".")) {
			Field field = klass.getDeclaredField(fieldName);
			return field.getType();
		} else {
			Field field = klass.getDeclaredField(fieldName.substring(0, fieldName.indexOf(".")));
			return getDataType(field.getType(), fieldName.substring(fieldName.indexOf(".") + 1));
		}
	}

    /**
     * @param list
     *            : List of POJOs
     * @param documentHeaders
     *            : List of header lines in excel sheet
     * @param dataColumnHeaders
     *            : Data Column Headers
     * @param fields
     *            : name of field property in POJO
     * @param sheetName
     *            : Work sheet name in excel sheet
     * @return
     */
    public static byte[] writeXLSWithMacro(List list, String[] documentHeaders, String[] dataColumnHeaders, String[] fields, String sheetName, String file_name) {
        // Blank workbook
        XSSFWorkbook workbook = null;
        try {
            if(macro_xlsm_folder != null) {
                workbook = new XSSFWorkbook(
                        OPCPackage.open(new BufferedInputStream(new FileInputStream(new File(macro_xlsm_folder + "/" + file_name))))
                );//
            } else {
                workbook = new XSSFWorkbook();
            }

            writeWorkBook(list, documentHeaders, dataColumnHeaders, fields, sheetName, workbook);
            return workbookToByteArray(workbook);
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try {
				if (workbook != null)
                	workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

	/**
	 * @param list
	 *            : List of POJOs
	 * @param documentHeaders
	 *            : List of header lines in excel sheet
	 * @param dataColumnHeaders
	 *            : Data Column Headers
	 * @param fields
	 *            : name of field property in POJO
	 * @param sheetName
	 *            : Work sheet name in excel sheet
	 * @return
	 */
	public static byte[] writeXLS(List list, String[] documentHeaders, String[] dataColumnHeaders, String[] fields, String sheetName) {
        // Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
        try {

            writeWorkBook(list, documentHeaders, dataColumnHeaders, fields, sheetName, workbook);
            return workbookToByteArray(workbook);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

	public static byte[] workbookToByteArray(XSSFWorkbook workbook) {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			workbook.write(bout);
			return bout.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param list
	 *            : List of POJOs
	 * @param documentHeaders
	 *            : List of header lines in excel sheet
	 * @param dataColumnHeaders
	 *            : Data Column Headers
	 * @param fields
	 *            : name of field property in POJO
	 * @param sheetName
	 *            : Work sheet name in excel sheet
	 * @return
	 */
	public static XSSFWorkbook writeXLS(XSSFWorkbook workbook, List list, String[] documentHeaders, String[] dataColumnHeaders, String[] fields,
			String sheetName) {
		writeWorkBook(list, documentHeaders, dataColumnHeaders, fields, sheetName, workbook);
		return workbook;
	}

	private static void writeWorkBook(List list, String[] documentHeaders, String[] dataColumnHeaders, String[] fields, String sheetName,
			XSSFWorkbook workbook) {
		// This data needs to be written (Object[])
		Map<Long, Object[]> data = new TreeMap<Long, Object[]>();
		data.put(1l, dataColumnHeaders);

		long count = 2;

		for (Object object : list) {
			Object[] objs = new Object[dataColumnHeaders.length];
			for (int i = 0; i < dataColumnHeaders.length; i++) {
				objs[i] = getFieldValue(fields[i], object);
				// columnFieldMap.get(i) != null ? columnFieldMap.get(object) :
				// null;
			}
			data.put(count++, objs);
		}

		// Create a blank sheet
		XSSFSheet sheet = workbook.createSheet(sheetName);
        if(workbook.getNumberOfSheets() > 1)
            workbook.setActiveSheet(1);

		int rownum = 0;
		// add documentHeaders
		if (documentHeaders != null) {
			for (String header : documentHeaders) {
				Row row = sheet.createRow(rownum++);
				Cell cell = row.createCell(0);
				cell.setCellValue(header);
			}
		}

		// Iterate over data and write to sheet
		Set<Long> keyset = data.keySet();
		for (Long key : keyset) {
			Row row = sheet.createRow(rownum++);
			Object[] objArr = data.get(key);
			int cellnum = 0;
			for (Object obj : objArr) {
				Cell cell = row.createCell(cellnum++);
				if (obj == null)
					continue;
				if (obj instanceof String)
					cell.setCellValue((String) obj);
				else if (obj instanceof Long)
					cell.setCellValue((Long) obj);
				else if (obj instanceof Double)
					cell.setCellValue((Double) obj);
				else if (obj instanceof Integer)
					cell.setCellValue((Integer) obj);
				else if (obj instanceof Boolean)
					cell.setCellValue((Boolean) obj);
                else
                    cell.setCellValue(obj.toString());
			}
		}
	}

	private static Object getFieldValue(String fieldName, Object object) {
		try {
			if (!fieldName.contains(".")) {
				Field field = object.getClass().getDeclaredField(fieldName);
				return field.get(object);
			}

			Field field = object.getClass().getDeclaredField(fieldName.substring(0, fieldName.indexOf(".")));

			return getFieldValue(fieldName.substring(fieldName.indexOf(".") + 1), field.get(object));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	private static Object getCellValue(XSSFCell cell) {
		if (XSSFCell.CELL_TYPE_NUMERIC == cell.getCellType())
			return new Long(new Double(cell.getNumericCellValue()).longValue());
		else if (XSSFCell.CELL_TYPE_STRING == cell.getCellType())
			return cell.getStringCellValue();
		else
			return null;
	}

	private static Object getCellValue(XSSFCell cell, Class klass, String fieldName) {

		if ("gender".equalsIgnoreCase(fieldName)) {
			if (cell != null && "M".equalsIgnoreCase(cell.getStringCellValue()))
				return 1;
			else if (cell != null && "F".equalsIgnoreCase(cell.getStringCellValue()))
				return 0;
			else
				return 2;
		}

		else if (cell == null)
			return null;

		else if (klass == Long.class){
			if(cell != null && cell.getCellType() == XSSFCell.CELL_TYPE_STRING){
				return Util.getLongValue(cell.getStringCellValue().trim());
			}
			else if(cell != null && cell.getCellType() != XSSFCell.CELL_TYPE_NUMERIC){
				return null;
			} else {
				return cell != null ? new Long(new Double(cell.getNumericCellValue()).longValue()) : null;
			}
		}
		else if (klass == Double.class) {
			if (cell != null && cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
				if (fieldName.endsWith("time")) {
					try {
						Date d = cell.getDateCellValue();
						return d.getHours() + d.getMinutes()/100d;
					} catch (Exception e) {
						return new Double(cell.getRawValue());
					}
				} else {
					return cell != null ? new Double(cell.getRawValue()) : null;
				}
			}
			return cell != null ? new Double(cell.getRawValue()) : null;
		}
		else if (klass == Float.class){
			if(cell != null && cell.getCellType() != XSSFCell.CELL_TYPE_NUMERIC){
				return null;
			} else {
				return cell != null ? new Double(cell.getNumericCellValue()).floatValue() : null;
			}
		}
		else if (klass == Integer.class) {
			if (fieldName.endsWith("date")) {
				if(cell != null && cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
					return Util.getIntDate(cell.getDateCellValue());
				}
				else if(cell != null && cell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
					return getIntDate(cell.getStringCellValue());
				}
			} else {
				if(cell != null && cell.getCellType() != XSSFCell.CELL_TYPE_NUMERIC){
					return null;
				} else {
					return cell != null ? new Double(cell.getNumericCellValue()).intValue() : 0;
				}
			}
		}
		else if (klass == String.class) {
			if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				if ("route_name".equals(fieldName) || "vehicle_uniqueNo".equals(fieldName) || "emp_no".equals(fieldName))
					return ("" + cell.getNumericCellValue()).substring(0, ("" + cell.getNumericCellValue()).indexOf("."));
				else
					return "" + cell.getRawValue();
			}
			if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING)
				return cell.getStringCellValue();
			if (cell != null && cell.getCellType() == Cell.CELL_TYPE_BLANK)
				return null;
		}
		else if (klass == Boolean.class) {
			if (cell != null && ("1".equals(cell.getStringCellValue()) || "Y".equalsIgnoreCase(cell.getStringCellValue())))
				return true;
			else
				return false;
		}
		else if (klass == null) {
			if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
				return cell.getRawValue();
			else
				return cell.getStringCellValue();
		}

			return null;
	}

    private static Integer getIntDate(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
