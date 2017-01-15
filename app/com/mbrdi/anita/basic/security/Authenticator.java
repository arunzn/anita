package com.mbrdi.anita.basic.security;

import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

public class Authenticator extends Security.Authenticator {

	@Override
	public String getUsername(Context ctx) {
		return ctx.session().get("id");
	}

	/**
	 * If user not authorized return him to login page. For data request return
	 * "forbidden"
	 */
	@Override
	public Result onUnauthorized(Context ctx) {
		return redirect("/login");
	}

}
