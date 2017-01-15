package com.mbrdi.anita.basic.security;

import play.Play;
import play.mvc.Http.Request;
import play.mvc.Http.Session;

import java.lang.reflect.Method;

public class Authorizer {
	
	public static boolean validateAccess(Request request, Session session, Method actionMethod) {
		
		try {
			//Check Rights Annotation on Method
			String rights = session.get("RIGHTS");
			RightsRequired ann = actionMethod.getAnnotation(RightsRequired.class);
			if (ann != null && ann.value() != null && ann.value().length > 0){
				if(rights == null)
					return false;
				
				for (Integer ar : ann.value()) {
					if(rights != null && !rights.contains(ar.toString())) {
						return false;
					}
				}
			}

			//Check Rights Annotation on Class
			ann = actionMethod.getDeclaringClass().getAnnotation(RightsRequired.class);
			if (ann != null && ann.value() != null && ann.value().length > 0){
				if(rights == null)
					return false;
				int[] rightsReqired = ann.value();
				for (int ar : rightsReqired) {
					if(rights != null && !rights.contains(ar+"")) {
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}

    public static boolean hasAccess(Session session, String key) {
        String rights = session.get("RIGHTS");
        AccessRight ar = AccessRight.valueOf(key);
        if (ar != null)
            return rights != null && rights.contains(ar.value.toString());
        return false;
    }

    public static boolean hasAccess(Session session, Integer key) {
        String rights = session.get("RIGHTS");
        return rights != null && rights.contains(key.toString());
    }
}
