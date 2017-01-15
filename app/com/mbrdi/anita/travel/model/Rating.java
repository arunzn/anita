package com.mbrdi.anita.travel.model;

import java.util.List;

public class Rating {

	public static enum Reason { UNCLEAN, LATE, IMPOLITE, NO_AIRCONDITIONING, FAST}

	public Integer value;
	public String message;
	public List<Reason> reasons;
}
