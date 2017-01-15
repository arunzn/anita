package com.mbrdi.anita.basic.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mbrdi.anita.basic.database.MongoDB;
import com.mbrdi.anita.basic.service.GridSupport;
import org.mongojack.JacksonDBCollection;
import org.mongojack.ObjectId;
import org.mongojack.WriteResult;

import javax.persistence.Id;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class ReportBug {

	public static JacksonDBCollection<ReportBug, String> reportBugCollection = MongoDB.getDBCollection(ReportBug.class);

	@Id
	@ObjectId
	public String _id;

	public String crash_api;
	public String crash_log;
	public Long time;


	public ReportBug save() {
		WriteResult<ReportBug, String> result = reportBugCollection.save(this);
		return result.getSavedObject();
	}

	public static GridResponse<ReportBug> fetch(GridRequest request) {
		return GridSupport.fetch(ReportBug.class, reportBugCollection, request);
	}

}
