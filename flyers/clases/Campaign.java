package com.advancehdt.flyers.clases;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.nio.channels.InterruptedByTimeoutException;
import java.util.List;

public class Campaign {

    private Integer id;
    private boolean is_polygon;
    private boolean valid_area;
    private List<LatLng> mPolygonPoints;
    private PolylineOptions mCoordenadas = new PolylineOptions();


    public Campaign(List<LatLng> mPolygonPoints) {
        this.mPolygonPoints = mPolygonPoints;
    }

    public Campaign() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isIs_polygon() {
        return is_polygon;
    }

    public void setIs_polygon(boolean is_polygon) {
        this.is_polygon = is_polygon;
    }

    public boolean isValid_area() {
        return valid_area;
    }

    public void setValid_area(boolean valid_area) {
        this.valid_area = valid_area;
    }

    public List<LatLng> getmPolygonPoints() {
        return mPolygonPoints;
    }

    public void setmPolygonPoints(List<LatLng> mPolygonPoints) {
        this.mPolygonPoints = mPolygonPoints;
    }

    public PolylineOptions getmCoordenadas() {
        return mCoordenadas;
    }

    public void setmCoordenadas(PolylineOptions mCoordenadas) {
        this.mCoordenadas = mCoordenadas;
    }
}
