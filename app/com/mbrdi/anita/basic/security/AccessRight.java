package com.mbrdi.anita.basic.security;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public enum AccessRight {
	BASIC(100);

	public Integer value;
    public Boolean isSpecial;

    public List<ActionRight> rights;
	
	private AccessRight(int value, ActionRight...rights){
		this.value = value;
        this.isSpecial = false;
        this.rights = new LinkedList<>();

        this.rights.add(new ActionRight("Add", value+ 1));
        this.rights.add(new ActionRight("Edit", value+ 2));
        this.rights.add(new ActionRight("Delete", value+ 3));
        if(rights.length > 0)
            this.rights.addAll(Arrays.asList(rights));
    }

    private AccessRight(int value, boolean isSpecial){
        this.value = value;
        this.isSpecial = isSpecial;
    }
	
	private static String all = null;
	
	public static String allRights(){
		if(all == null){
			all = "";
			for(AccessRight ar :AccessRight.values()){
				all += ar.value + ",";
                if(ar.rights!= null) {
                    for (ActionRight r : ar.rights) {
                        all += r.value + ",";
                    }
                }
			}
		}
		return all;
	}

	public static class ActionRight {
        public String name;
        public Integer value;
        public ActionRight(String name, Integer value){
            this.name = name;
            this.value = value;
        }
    }
}
