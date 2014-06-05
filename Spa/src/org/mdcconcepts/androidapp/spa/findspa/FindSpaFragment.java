package org.mdcconcepts.androidapp.spa.findspa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;
import org.mdcconcepts.androidapp.spa.R;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class FindSpaFragment extends Fragment implements android.location.LocationListener 
{
	public GoogleMap google_map;
	LocationManager location_manager;
	Location location;
	double latitude, longitude;
	MarkerOptions marker;
	int count = 0;
	Context context;
	private static View rootView;

	private LocationManager mlocManager;
	private double mLastLatitude;
	private double mLastLongitude;
	// private MyListener mlocListener;
	
	double myCurrentLocationLat = 0.0;
	double myCurrentLocationlong = 0.0;
	private static final LatLng STREET1 = new LatLng(18.517909, 73.839365);
	private static final LatLng STREET2 = new LatLng(18.531319, 73.838199);
	private static final LatLng STREET3 = new LatLng(18.520421, 73.843716);
	private static final LatLng WALL_STREET = new LatLng(18.517829, 73.849186);
	private static View view;
	public FindSpaFragment() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		 if (rootView != null)
		 {
		        ViewGroup parent = (ViewGroup) rootView.getParent();
		        if (parent != null)
		            parent.removeView(rootView);
		 }
		 
		 try {
			  	rootView = inflater.inflate(R.layout.fragment_find_spa, container,
						false);
			  	if (rootView != null)
				{	
					context = rootView.getContext();
				
				}
			  	initilizeMap();
		 } 
    	 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		
		 return rootView;
	}

	@SuppressLint("NewApi") private void initilizeMap()
	{
		if (google_map == null)
		{
			google_map = ((MapFragment) this.getActivity().getFragmentManager()
					.findFragmentById(R.id.map)).getMap();

			// check if map is created successfully or not
			if (google_map == null)
			{
				 Toast.makeText(this.getActivity(),"Sorry! unable to create maps",
				 Toast.LENGTH_SHORT).show();
			}

			// google_map.setMyLocationEnabled(true);
			getLocation();

			marker = new MarkerOptions().position(
					new LatLng(mLastLatitude, mLastLongitude)).title(
					"Current Location");

			MarkerOptions options = new MarkerOptions();
			options.position(new LatLng(mLastLatitude, mLastLongitude));
			options.position(WALL_STREET);
			options.position(STREET1);
			options.position(STREET2);
			options.position(STREET3);

			// google_map.addMarker(options);
			String url = getMapsApiDirectionsUrl(new LatLng(mLastLatitude,
					mLastLongitude), WALL_STREET);
			ReadTask downloadTask = new ReadTask();
			downloadTask.execute(url);

			url = getMapsApiDirectionsUrl(new LatLng(mLastLatitude,
					mLastLongitude), STREET1);
			downloadTask = new ReadTask();
			downloadTask.execute(url);

			url = getMapsApiDirectionsUrl(new LatLng(mLastLatitude,
					mLastLongitude), STREET2);
			downloadTask = new ReadTask();
			downloadTask.execute(url);

			url = getMapsApiDirectionsUrl(new LatLng(mLastLatitude,
					mLastLongitude), STREET3);
			downloadTask = new ReadTask();
			downloadTask.execute(url);

			/*** ZoomIn ****/
			google_map.animateCamera(CameraUpdateFactory.newLatLngZoom(
					new LatLng(mLastLatitude, mLastLongitude), 15));
			google_map.setInfoWindowAdapter(new InfoWindowAdapter() {

				@Override
				public View getInfoWindow(Marker arg0) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public View getInfoContents(Marker arg0) {
					// TODO Auto-generated method stub
					View v = getActivity().getLayoutInflater().inflate(
							R.layout.custom_info_window, null);
					return v;
				}
			});

			// google_map.setOnMapClickListener(new OnMapClickListener() {
			//
			// public void onMapClick(LatLng arg0) {
			// // TODO Auto-generated method stub
			// google_map.clear();
			// MarkerOptions markerOptions = new MarkerOptions();
			// markerOptions.position(arg0);
			// google_map.animateCamera(CameraUpdateFactory.newLatLng(arg0));
			// Marker marker = google_map.addMarker(markerOptions);
			// marker.showInfoWindow();
			// }
			// });
			addMarkers();

		}
	}

	public void getLocation() {
		try {
			LocationManager locationManager = (LocationManager) context
					.getSystemService(this.getActivity().LOCATION_SERVICE);

			// getting GPS status
			boolean isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			boolean isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
			} else {
				// this.canGetLocation = true;
				// First get location from Network Provider
				if (isNetworkEnabled) {
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER, 3000, 10, this);
					Log.d("Network", "Network");
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							mLastLatitude = location.getLatitude();
							mLastLongitude = location.getLongitude();
							Log.v("latitude", String.valueOf(latitude));
							Log.v("Longitude", String.valueOf(longitude));
						}

					}
			}
//			/ if GPS Enabled get lat/long using GPS Services
			if (isGPSEnabled)
			{
				if (location == null) 
				{
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10, this);
//						Log.d("GPS Enabled", "GPS Enabled");
					if (locationManager != null)
					{
							location = locationManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null)
							{
								latitude = location.getLatitude();
								longitude = location.getLongitude();
								Log.v("latitude", String.valueOf(latitude));
								Log.v("Longitude", String.valueOf(longitude));

							}
						}
					}
				}
			}

		} 
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	private String getMapsApiDirectionsUrl(LatLng source, LatLng destination)
	{

		String waypoints = "waypoints=optimize:true|" + +source.latitude + ","
				+ source.longitude + "|" + destination.latitude + ","
				+ destination.longitude;
		// + "|"+STREET1.latitude + ","+ STREET1.longitude;
		String sensor = "sensor=false";
		String params = waypoints + "&" + sensor;
		String output = "json";
		String url = "https://maps.googleapis.com/maps/api/directions/"
				+ output + "?" + params;
		return url;
	}

	private void addMarkers() {
		if (google_map != null) {
			google_map.addMarker(new MarkerOptions().position(
					new LatLng(mLastLatitude, mLastLongitude)).title(
					"First Point"));
			google_map.addMarker(new MarkerOptions().position(WALL_STREET)
					.title("First Point"));
			google_map.addMarker(new MarkerOptions().position(STREET1).title(
					"Second Point"));
			google_map.addMarker(new MarkerOptions().position(STREET2).title(
					"Second Point"));
			google_map.addMarker(new MarkerOptions().position(STREET3).title(
					"Second Point"));
		}
	}

	private class ReadTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... url) {
			String data = "";
			try {
				HttpConnection http = new HttpConnection();
				data = http.readUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			new ParserTask().execute(result);
		}
	}

	private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> 
	{

		@Override
		protected List<List<HashMap<String, String>>> doInBackground(String... jsonData)
		{
			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try {
				jObject = new JSONObject(jsonData[0]);
				PathJSONParser parser = new PathJSONParser();
				routes = parser.parse(jObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return routes;
		}

		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> routes) 
		{
			ArrayList<LatLng> points = null;
			PolylineOptions polyLineOptions = new PolylineOptions();

			// traversing through routes
			for (int i = 0; i < routes.size(); i++) 
			{
				points = new ArrayList<LatLng>();
				// polyLineOptions = new PolylineOptions();
				List<HashMap<String, String>> path = routes.get(i);
				for (int j = 0; j < path.size(); j++) 
				{
					HashMap<String, String> point = path.get(j);

					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);

					points.add(position);
				}

				polyLineOptions.addAll(points);
				polyLineOptions.width(4);

				switch (count) 
				{
					case 0:
						polyLineOptions.color(Color.BLUE);
						count++;
						break;
					case 1:
						polyLineOptions.color(Color.RED);
						count++;
						break;
					case 2:
						polyLineOptions.color(Color.GREEN);
						count++;
						break;
					case 3:
						polyLineOptions.color(Color.BLACK);
						count = 0;
						break;

				}

			}
			google_map.addPolyline(polyLineOptions);
		}
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

}
