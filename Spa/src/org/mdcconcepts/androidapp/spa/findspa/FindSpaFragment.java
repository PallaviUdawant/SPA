package org.mdcconcepts.androidapp.spa.findspa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mdcconcepts.androidapp.spa.R;
import org.mdcconcepts.androidapp.spa.customitems.GPSTracker;
import org.mdcconcepts.androidapp.spa.serverhandler.JSONParser;
import org.mdcconcepts.androidapp.spa.util.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class FindSpaFragment extends Fragment implements
		android.location.LocationListener {
	public GoogleMap google_map;
	LocationManager location_manager;
	Location location;
	double latitude, longitude;
	MarkerOptions marker;
	int count = 0;
	Context context;
	private static View rootView;
	Activity rootActivity;
	GPSTracker gps;

	private double mLastLatitude;
	private double mLastLongitude;
	// private MyListener mlocListener;

	double myCurrentLocationLat = 0.0;
	double myCurrentLocationlong = 0.0;
	// private static final LatLng STREET1 = new LatLng(18.517909, 73.839365);
	// private static final LatLng STREET2 = new LatLng(18.531319, 73.838199);
	// private static final LatLng STREET3 = new LatLng(18.520421, 73.843716);
	// private static final LatLng WALL_STREET = new LatLng(18.517829,
	// 73.849186);
	private ArrayList<Spa_Data> NearestLocations = new ArrayList<Spa_Data>();

	public FindSpaFragment() {
		// TODO Auto-generated constructor stub

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if (rootView != null) {
			ViewGroup parent = (ViewGroup) rootView.getParent();
			if (container.getParent() != null) {
				// parent.removeView(rootView);
				((ViewGroup) container.getParent()).removeView(rootView);

			}

		}

		try {
			rootActivity = getActivity();

			rootView = inflater.inflate(R.layout.fragment_find_spa, container,
					false);

			if (rootView != null) {
				context = rootView.getContext();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		new FetchNearestSpa().execute();
		return rootView;
	}

	@SuppressLint("NewApi")
	private void initilizeMap() {
		if (google_map == null) {
			Fragment fragment = (getFragmentManager()
					.findFragmentById(R.id.map));
			if (fragment != null) {
				
				FragmentTransaction ft = getActivity().getFragmentManager()
						.beginTransaction();
				// fragment= new FindSpaFragment();
				// ft.add(R.id.map, fragment);
				// ft.replace(R.id.map, sp);
				// ft.commit();
				Toast.makeText(rootActivity, "fragment not null",
						Toast.LENGTH_LONG).show();
				// ft.remove(fragment);
				// ft.commit();}
			} else {
				Toast.makeText(rootActivity, "fragment is null",
						Toast.LENGTH_LONG).show();
			}
			try {
				google_map = ((MapFragment) this.getActivity()
						.getFragmentManager().findFragmentById(R.id.map))
						.getMap();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// check if map is created successfully or not
			if (google_map == null) {
				Toast.makeText(this.getActivity(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			} else {

				gps = new GPSTracker(getActivity());

				// check if GPS enabled
				if (gps.canGetLocation()) {

					mLastLatitude = gps.getLatitude();
					mLastLongitude = gps.getLongitude();

					// \n is for new line
					Toast.makeText(
							getActivity(),
							"Your Location is - \nLat: " + mLastLatitude
									+ "\nLong: " + mLastLongitude, Toast.LENGTH_LONG)
							.show();
					Log.d("latitude", "" + mLastLatitude);
					Log.d("longitude", "" + mLastLongitude);

				} else {
					// can't get location
					// GPS or Network is not enabled
					// Ask user to enable GPS/network in settings
					gps.showSettingsAlert();
				}
				// google_map.setMyLocationEnabled(true);
				// getLocation();

				marker = new MarkerOptions().position(
						new LatLng(mLastLatitude, mLastLongitude)).title(
						"Current Location");

				MarkerOptions options = new MarkerOptions();
				options.position(new LatLng(mLastLatitude, mLastLongitude));
				LatLng newLatLong;
				double newLat, newLong;
				for (int i = 0; i < NearestLocations.size(); i++) {
					newLat = Double
							.parseDouble(NearestLocations.get(i).Spa_Lat);
					newLong = Double
							.parseDouble(NearestLocations.get(i).Spa_Long);
					newLatLong = new LatLng(newLat, newLong);

					// options.position(newLatLong);

					String url = getMapsApiDirectionsUrl(new LatLng(
							mLastLatitude, mLastLongitude), newLatLong);
					ReadTask downloadTask = new ReadTask();
					downloadTask.execute(url);

				}
				// options.position(WALL_STREET);
				// options.position(STREET1);
				// options.position(STREET2);
				// options.position(STREET3);

				// google_map.addMarker(options);
				// String url = getMapsApiDirectionsUrl(new
				// LatLng(mLastLatitude,
				// mLastLongitude), WALL_STREET);
				// ReadTask downloadTask = new ReadTask();
				// downloadTask.execute(url);
				//
				// url = getMapsApiDirectionsUrl(new LatLng(mLastLatitude,
				// mLastLongitude), STREET1);
				// downloadTask = new ReadTask();
				// downloadTask.execute(url);
				//
				// url = getMapsApiDirectionsUrl(new LatLng(mLastLatitude,
				// mLastLongitude), STREET2);
				// downloadTask = new ReadTask();
				// downloadTask.execute(url);
				//
				// url = getMapsApiDirectionsUrl(new LatLng(mLastLatitude,
				// mLastLongitude), STREET3);
				// downloadTask = new ReadTask();
				// downloadTask.execute(url);

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
						View v = rootActivity.getLayoutInflater().inflate(
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
	}

	public void getLocation() {
		try {
			LocationManager locationManager = (LocationManager) context
					.getSystemService(getActivity().LOCATION_SERVICE);

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
							LocationManager.NETWORK_PROVIDER, 1000 * 60 * 1,
							10, this);
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
				// / if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER, 1000 * 60 * 1,
								10, this);
						// Log.d("GPS Enabled", "GPS Enabled");
						if (locationManager != null) {
							location = locationManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
								Log.v("latitude", String.valueOf(latitude));
								Log.v("Longitude", String.valueOf(longitude));

							}
						}
					}
				}
			}

		} catch (Exception e) {
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

	private String getMapsApiDirectionsUrl(LatLng source, LatLng destination) {

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
					"Current Location"));
			LatLng newLatLong;
			double newLat, newLong;
			for (int i = 0; i < NearestLocations.size(); i++) {
				newLat = Double.parseDouble(NearestLocations.get(i).Spa_Lat);
				newLong = Double.parseDouble(NearestLocations.get(i).Spa_Long);
				newLatLong = new LatLng(newLat, newLong);
				google_map.addMarker(new MarkerOptions().position(newLatLong));
				// options.position(newLatLong);

			}
			// google_map.addMarker(new MarkerOptions().position(WALL_STREET)
			// .title("First Point"));
			// google_map.addMarker(new MarkerOptions().position(STREET1).title(
			// "Second Point"));
			// google_map.addMarker(new MarkerOptions().position(STREET2).title(
			// "Second Point"));
			// google_map.addMarker(new MarkerOptions().position(STREET3).title(
			// "Second Point"));
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

	// public void onDestroyView()
	// {
	// super.onDestroyView();
	// Fragment fragment = (getFragmentManager().findFragmentById(R.id.map));
	// if(fragment!=null)
	// {
	// FragmentTransaction ft =
	// getActivity().getFragmentManager().beginTransaction();
	// ft.remove(fragment);
	// ft.commit();}
	// }
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		google_map.clear();
	}
	@Override
	public void onPause() {
		super.onPause();
		// google_map=((MapFragment)
		// this.getActivity().getFragmentManager().findFragmentById(R.id.map)).getMap();
		// if(google_map != null)
		// google_map = null;

	}

	private class ParserTask extends
			AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

		@Override
		protected List<List<HashMap<String, String>>> doInBackground(
				String... jsonData) {
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
		protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
			ArrayList<LatLng> points = null;
			PolylineOptions polyLineOptions = new PolylineOptions();

			// traversing through routes
			for (int i = 0; i < routes.size(); i++) {
				points = new ArrayList<LatLng>();
				// polyLineOptions = new PolylineOptions();
				List<HashMap<String, String>> path = routes.get(i);
				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);

					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);

					points.add(position);
				}

				polyLineOptions.addAll(points);
				polyLineOptions.width(4);

				switch (count) {
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
			// google_map.addPolyline(polyLineOptions);
		}
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	public class FetchNearestSpa extends AsyncTask<String, String, String> {

		private ProgressDialog pDialog;
		int success;
		JSONParser jsonParser = new JSONParser();
		private static final String TAG_SUCCESS = "success";
		private static final String TAG_MESSAGE = "message";

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pDialog = new ProgressDialog(getActivity());
			pDialog.setMessage("Fetching Data ... ");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				// Building Parameters
				List<NameValuePair> params1 = new ArrayList<NameValuePair>();

				params1.add(new BasicNameValuePair("CurrentLat", String
						.valueOf(mLastLatitude)));
				params1.add(new BasicNameValuePair("CurrentLong", String
						.valueOf(mLastLongitude)));

				Log.d("request!", "starting");

				// Posting user data to script
				JSONObject json = jsonParser.makeHttpRequest(
						Util.NearestSpa_URL, "POST", params1);

				// full json response
				Log.d("Nearest Spa", json.toString());

				// json success element
				success = json.getInt(TAG_SUCCESS);

				if (success == 1) {

					JSONArray PostJson = json.getJSONArray("posts");
					Log.d("Post Date ", PostJson.toString());
					for (int i = 0; i < PostJson.length(); i++) {

						JSONObject Temp = PostJson.getJSONObject(i);
						// NearestLocations= new ArrayList<Spa_Data> ();
						NearestLocations.add(new Spa_Data(Temp
								.getString("Spa_Name"), Temp
								.getString("Spa_Id"),
								Temp.getString("Spa_Lat"), Temp
										.getString("Spa_long"), Temp
										.getString("Addresss")));
						Log.d("data", Temp.getString("Spa_Name"));
						Log.d("data", Temp.getString("Spa_Id"));
						Log.d("data", Temp.getString("Spa_Lat"));
						Log.d("data", Temp.getString("Spa_long"));
						Log.d("data", Temp.getString("Addresss"));
					}

					return json.getString(TAG_MESSAGE);
				} else {
					Log.d("Login Failure!", json.getString(TAG_MESSAGE));
					return json.getString(TAG_MESSAGE);

				}
				// WALL_STREET.latitude=json.getDouble("Spa_Lat");

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;

		}

		/**
		 * After completing background task Dismiss the progress dialog
		 **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog once product deleted
			pDialog.dismiss();
			initilizeMap();
			// if (file_url != null) {
			// Toast.makeText(LoginActivity.this, file_url, Toast.LENGTH_LONG)
			// .show();
			// if (success == 1) {
			// Intent myIntent = new Intent(LoginActivity.this,
			// MainActivity.class);
			// finish();
			// startActivity(myIntent);
			// }
			// }

		}

	}

}
