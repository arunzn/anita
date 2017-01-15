package com.mbrdi.anita.basic.util;


import com.mbrdi.anita.location.model.Location;

import java.util.List;

public class PolylineEncoding {

	  /**
	   * Encodes a sequence of LatLngs into an encoded path string.
	   */
	  public static String encode(final List<Location> path) {
	    long lastLat = 0;
	    long lastLng = 0;
	    
//	    List<Location> path1s = path.subList(0, 10);

	    final StringBuffer result = new StringBuffer();

	    for (final Location	 point : path) {
	      long lat = Math.round(point.latitude * 1e5);
	      long lng = Math.round(point.longitude * 1e5);

	      long dLat = lat - lastLat;
	      long dLng = lng - lastLng;

	      encode(dLat, result);
	      encode(dLng, result);

	      lastLat = lat;
	      lastLng = lng;
	    }
	    return result.toString();
	  }

	  private static void encode(long v, StringBuffer result) {
	    v = v < 0 ? ~(v << 1) : v << 1;
	    while (v >= 0x20) {
	      result.append(Character.toChars((int) ((0x20 | (v & 0x1f)) + 63)));
	      v >>= 5;
	    }
	    result.append(Character.toChars((int) (v + 63)));
	  }
	}
