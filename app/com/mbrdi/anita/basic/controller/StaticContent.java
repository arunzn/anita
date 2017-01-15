package com.mbrdi.anita.basic.controller;

import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

public class StaticContent extends Controller {

//	private static String static_content_server = Play.application().configuration().getString("static.content.server","anita.zoyride.com:9090/assets");
	private static String static_content_server = Play.application().configuration().getString("static.content.server","localhost:9000/assets");
	private static String static_content_protocol = Play.application().configuration().getString("static.content.protocol","http://");

	public Result sitemapXml() {
//		response().setContentType("application/xml");
//		response().setHeader("Content-disposition", "attachment; filename=sitemap.xml");
//		return ok(new File("public/sitemap.xml"));
		return redirect(static_content_protocol + static_content_server +"/sitemap.xml");
	}
	
	public static String at(String file) {
		return (static_content_protocol + static_content_server + "/" + file);
	}

    public static String staticServer(){
        return static_content_protocol + static_content_server + "/";
    }
}
