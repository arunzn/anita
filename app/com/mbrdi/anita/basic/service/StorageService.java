package com.mbrdi.anita.basic.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.mbrdi.anita.basic.model.Attachment;
import com.mbrdi.anita.basic.util.DateUtil;
import play.Play;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.*;

public class StorageService {

	private static String bucket = Play.application().configuration().getString("aws-bucket");
	public static String BUCKET_URL = Play.application().configuration().getString("aws-bucket-url");

	private static LinkedList<Integer> listSurveillancePics(String prefix) {
		LinkedList<Integer> picIds = new LinkedList<Integer>();
		AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());
		
		try {

			ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(prefix);
			ObjectListing objectListing;
			do {
				objectListing = s3client.listObjects(listObjectsRequest);
				for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
					String keyStr = objectSummary.getKey();
					if (keyStr != null && !"".equals(keyStr)) {
						keyStr = keyStr.replace(prefix, "");
						keyStr = keyStr.replace(".jpg", "");
						try {
							Integer key = Integer.valueOf(keyStr);
							picIds.add(key);
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}
				}
				listObjectsRequest.setMarker(objectListing.getNextMarker());
			} while (objectListing.isTruncated());
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return picIds;
	}
	
	public static boolean exists(String prefix) {
		AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());

		boolean isValidFile = true;
	    try {
	        ObjectMetadata objectMetadata = s3client.getObjectMetadata(bucket, prefix);
	    } catch (AmazonS3Exception s3e) {
	        if (s3e.getStatusCode() == 404) {
	        // i.e. 404: NoSuchKey - The specified key does not exist
	            isValidFile = false;
	        }
	        else {
	            s3e.printStackTrace();    // rethrow all S3 exceptions other than 404   
	        }
	    }
	    return isValidFile;
	}
	
	private static Map<String, String> listFiles(String prefix) {
		Map<String, String> files = new HashMap<>();
		AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());
		
		try {

			ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(prefix);
			ObjectListing objectListing;
			do {
				objectListing = s3client.listObjects(listObjectsRequest);
				for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
					String keyStr = objectSummary.getKey();
					files.put(keyStr, objectSummary.getLastModified().toString() + " | " + objectSummary.getSize() + " KB");
				}
				listObjectsRequest.setMarker(objectListing.getNextMarker());
			} while (objectListing.isTruncated());
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return files;
	}

	public static LinkedList<String> listPics(String prefix) {
		LinkedList<String> pics = new LinkedList<String>();
		AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());
		
		try {

			ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(prefix);
			ObjectListing objectListing;
			do {
				objectListing = s3client.listObjects(listObjectsRequest);
				for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
					String keyStr = objectSummary.getKey();
					if (keyStr != null && !"".equals(keyStr)) {
						pics.add(BUCKET_URL + keyStr);
					}
				}
				listObjectsRequest.setMarker(objectListing.getNextMarker());
			} while (objectListing.isTruncated());
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return pics;
	}
	
	public static LinkedList<Attachment> listAttachments(String prefix) {
		LinkedList<Attachment> Attachments = new LinkedList<Attachment>();
		AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());

		try {

			ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(prefix);
			ObjectListing objectListing;
			do {
				objectListing = s3client.listObjects(listObjectsRequest);
				for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
					Attachment attachment = new Attachment();
					String keyStr = objectSummary.getKey();
					if (keyStr != null && !"".equals(keyStr)) {
						try {
							attachment.url = BUCKET_URL + keyStr;
							keyStr = keyStr.replace(prefix, "");
							keyStr = keyStr.replace(".jpg", "");
							attachment.name = keyStr;
							Attachments.add(attachment);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				listObjectsRequest.setMarker(objectListing.getNextMarker());
			} while (objectListing.isTruncated());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return Attachments;
	}

	/**
	 * Use this method to upload User/Driver/Corporate photos.
	 * 
	 * @return
	 */
	public static String upload(String path, File file) {
        new TransferManager(new ProfileCredentialsProvider()).upload(bucket, path, file);
		return BUCKET_URL + path;
	}
	
	public static String uploadCorpPics(String corp_id, String field_name, String field_id, String name, String date, String type, File file) {
		/**
		 * url format is "/files/corp/:corp_id/veh/:veh_id/current_time_long/file.jpg"
		 * */
		String path = "files/corp/" + corp_id + "/" + field_name+ "/" + field_id + "/" + date + "/"+ name + "/" + type;
        new TransferManager(new ProfileCredentialsProvider()).upload(bucket, path, file);
		return BUCKET_URL + path;
	}
	
	public static String uploadInvoices(String path, File file) {
        new TransferManager(new ProfileCredentialsProvider()).upload(bucket, path, file);
		return BUCKET_URL + path;
	}

	public static String uploadVehiclePic(String vehicle_id, String country_code, String type, File file) {
		String path = country_code + "/VEHICLE/" + vehicle_id + "/" + type;
		new TransferManager(new ProfileCredentialsProvider()).upload(bucket, path, file);
		return BUCKET_URL + path;
	}

	public static String uploadDriverPic(String driver_id, String country_code, String type, File file) {
		String path = country_code + "/DRIVER/" + driver_id + "/" + type;
		new TransferManager(new ProfileCredentialsProvider()).upload(bucket, path, file);
		return BUCKET_URL + path;
	}

	public static String uploadTripMap(int country_code, String corp_id, String trip_id, int date, byte[] input) {
		
		String path = country_code + "/CORP/" + corp_id + "/TRIP/" + date + "/" + trip_id + "/journey_map.jpeg";
		
	    Long contentLength = input != null ? Long.valueOf(input.length) : 0;

	    ObjectMetadata metadata = new ObjectMetadata();
	    metadata.setContentLength(contentLength);

        new TransferManager(new ProfileCredentialsProvider()).upload(bucket, path, new ByteArrayInputStream(input), metadata);
		return path;
	}
	
	public static String uploadTripAttachment(Integer country_code, String corp_id, String trip_id, Integer date, String file_name, File file) {
		String path = country_code + "/CORP/" + corp_id + "/TRIP/" + date + "/" + trip_id + "/" + file_name;
        new TransferManager(new ProfileCredentialsProvider()).upload(bucket, path, file);
		return BUCKET_URL + path;
	}
	
	public static String uploadEmployeeAttachment(Integer country_code, String corp_id, String user_id, String file_name, File file) {
		String path = country_code + "/CORP/" + corp_id + "/EMPLOYEE/" + user_id + "/" + file_name;
        new TransferManager(new ProfileCredentialsProvider()).upload(bucket, path, file);
		return BUCKET_URL + path;
	}
	
	public static String uploadRouteAttachment(Integer country_code, String corp_id,  String route_id, Integer date, String file_name, File file)
	{
		//date is route start date.
		String path = country_code + "/CORP/" + corp_id + "/ROUTE/" + date + "/" + route_id + "/" + file_name;
        new TransferManager(new ProfileCredentialsProvider()).upload(bucket, path, file);
		return BUCKET_URL + path;
	}
	
	public static void moveRouteAttachmentToTrip(Integer country_code, String corp_id,  String route_id, String trip_id, Integer date){
		try {
			String route_path = country_code + "/CORP/" + corp_id + "/ROUTE/" + date + "/" + route_id + "/";
			String trip_path = country_code + "/CORP/" + corp_id + "/TRIP/" + date + "/" + trip_id + "/";
			
			Map<String, String> files = listFiles(BUCKET_URL + route_path);
			for(String file : files.keySet())
                new TransferManager(new ProfileCredentialsProvider()).copy(BUCKET_URL, route_path + file, route_path, trip_path + file);
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	public static String uploadTicketAttachment(String country_code, String pojo_name, String corp_id, String ticket_id, String field_name, File file) {
		/**
		 * url format is "/country_code/TICKET/corp_id/ticket_id/file"
		 * */
		String path = country_code + "/TICKET/" + corp_id + "/" + ticket_id + "/" + field_name;
		new TransferManager(new ProfileCredentialsProvider()).upload(bucket, path, file);
		return BUCKET_URL + path;
	}


	public static String uploadEmployeeProfilePic(Integer country_code, String corp_id, String user_id, String file_name, File file) {
		String path = country_code + "/CORP/" + corp_id + "/EMPLOYEE/" + user_id + "/" + file_name + ".jpeg";
        new TransferManager(new ProfileCredentialsProvider()).upload(bucket, path, file);
		return BUCKET_URL + path;
	}
	

	public static String uploadRoutePics(String corp_id, String field_name, String route_id, String type, File file) {
		String path = "PICS/CORP" + "/" + corp_id + "/" + field_name+ "/" + route_id + "/" + type;
        new TransferManager(new ProfileCredentialsProvider()).upload(bucket, path, file);
		return BUCKET_URL + path;
	}
	
	
//	public static void copyRoutePicsToTrip(String corp_id, String source_field_name, String destination_field_name, String route_id, String trip_id) {
//		try {
//			String sourcePath = "PICS/CORP" + "/" + corp_id + "/" + source_field_name + "/" + route_id;
//			String destinationPath = "PICS/CORP" + "/" + corp_id + "/" + destination_field_name + "/" + trip_id;
//			tm.copy(bucket, sourcePath, bucket, destinationPath);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public static void browseObjectByAccType(String prefix,int case_){
		
		AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());
		try {
			
			ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(prefix);
			ObjectListing objectListing;
			do {
				objectListing = s3client.listObjects(listObjectsRequest);
				for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
					String keyStr = objectSummary.getKey();
					if (keyStr != null && !"".equals(keyStr)) {
							 // case 1 for all previous year
						if(case_ == 1)
						deleteObject(s3client, keyStr);
						else if(case_ == 2){ // for PREMIUM
							String[] arr = keyStr.substring(prefix.length()).split("/");
							if(arr.length > 0){
								if(Integer.parseInt(arr[0]) < (DateUtil.getDayOfYear() - 30))
									deleteObject(s3client, keyStr);
							}
						} else if(case_ == 3){ // for PREMIUM
							String[] arr = keyStr.substring(prefix.length()).split("/");
							if(arr.length > 0){
								if(Integer.parseInt(arr[0]) < (335 + DateUtil.getDayOfYear()))
									deleteObject(s3client, keyStr);
							}
						} else if(case_ == 4){ // for BASIC
							String[] arr = keyStr.substring(prefix.length()).split("/");
							if(arr.length > 0){
								if(Integer.parseInt(arr[0]) < (DateUtil.getDayOfYear() - 3))
									deleteObject(s3client, keyStr);
							}
						} else if(case_ == 5){ // for BASIC
							String[] arr = keyStr.substring(prefix.length()).split("/");
							if(arr.length > 0){
								if(Integer.parseInt(arr[0]) < (362 + DateUtil.getDayOfYear()))
									deleteObject(s3client, keyStr);
							}
						}
					}
				}
				listObjectsRequest.setMarker(objectListing.getNextMarker());
			} while (objectListing.isTruncated());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteByPrefix(String prefix) {

		AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());
		deleteObject(s3client, prefix);

	}
	
	public static void deleteObject(AmazonS3 s3, String key ){
		
		try{
		s3.deleteObject(bucket, key);
		} catch(AmazonServiceException ase){
			ase.printStackTrace();
		} catch(AmazonClientException ace){
			ace.printStackTrace();
		}
	}

}
