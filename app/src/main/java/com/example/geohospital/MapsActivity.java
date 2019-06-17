package com.example.geohospital;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.renderscript.Sampler;
import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double longitud;
    private double latitud;
    private LocationManager locManager;
    private Location lc;
    private FirebaseFirestore db;
    private List<Address> adress;
    private Address cercano;
    private final Map<String, MarkerOptions> mMarkers = new ConcurrentHashMap<String, MarkerOptions>();
    private LatLng hospitalCercano=null;
    private String ciudad;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);



        mapFragment.getMapAsync(this);
        Ubicacion();



    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
   // @Override
    public void onMapReady(GoogleMap googleMap)
    {
        Ubicacion();
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(latitud,longitud);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Mi ubicación."));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mensaje("¡Da clic en el mapa para comenzar!");

    }

    private void  Ubicacion()
    {
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {

            LocationListener locationListener=new LocationListener() {
                @Override
                public void onLocationChanged(Location location)
                {
                    if(getTitle().equals("Mapa"))
                    {
                        return;
                    }

                    lc=location;
                    mMap.clear();
                    dibujarMiubicacion(location);



                   mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                       @Override
                       public void onMapClick(LatLng latLng)
                       {
                           if(getTitle().equals("Mapa"))
                           {
                               return;
                           }
                           if(lc!=null)
                           {

                               dibujarHospita();
                           }


                       }
                   });


                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker)
                        {
                            Toast.makeText(getApplicationContext(),""+marker.getTitle(),Toast.LENGTH_LONG).show();
                            return false;

                        }
                    });

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            int permiso= ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION );
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);

        }
    }


    private void dibujarHospita()
    {
        mMap.clear();

        dibujarMiubicacion(lc);




        try {

            Geocoder geo = new Geocoder(getApplicationContext());


            adress= geo.getFromLocation(lc.getLatitude(),lc.getLongitude(),1);

            String[] parts = adress.get(0).getAddressLine(0).split(",");
            ciudad=parts[ parts.length-3 ].trim();


            db= FirebaseFirestore.getInstance();//Inicializar conexion firebase

            CollectionReference cRf= db.collection("Hospital");

            Query query = cRf.whereEqualTo("ciudad", ciudad );//Filtro que hace que consulte solo los hospitales de la ciudad donde etoy.

          //  Query query = cRf;//Trae todos los hospitales pero hace mas lento el proceso.

            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot value, @javax.annotation.Nullable FirebaseFirestoreException e) {

                    float distancia=0;
                    boolean primeraVez=true;
                    String nombreHospital="";

                    for (QueryDocumentSnapshot doc:value)
                    {

                        try {

                            String nonbre= doc.getString("hospital");
                            if(nonbre==null)
                            {
                                break;
                            }

                            Geocoder geo = new Geocoder(getApplicationContext());
                            adress = geo.getFromLocationName(doc.getString("hospital")+","+doc.getString("ciudad"), 1);
                            Address loca=adress.get(0);
                            loca.getAddressLine(0);


                            double b=loca.getLongitude();
                            Location hospitalCercano = new Location("");
                            hospitalCercano.setLatitude(loca.getLatitude());
                            hospitalCercano.setLongitude(loca.getLongitude());


                            LatLng hospital = new LatLng(loca.getLatitude(),loca.getLongitude());
                            MarkerOptions options = new MarkerOptions();
                            options.position(hospital);

                            MarkerOptions marker=new MarkerOptions()
                                    .position(hospital)
                                    .title( doc.getString("hospital") +"-"+loca.getAddressLine(0))
                                    .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.hospital));


                            mMarkers.put(doc.getString("hospital"),marker);



                            if(primeraVez)
                            {
                                distancia=  lc.distanceTo(hospitalCercano);
                                primeraVez=false;
                                nombreHospital=doc.getString("hospital");
                                cercano=loca;
                                continue;
                            }

                            if(lc.distanceTo(hospitalCercano) < distancia)
                            {
                                distancia=  lc.distanceTo(hospitalCercano);
                                nombreHospital=doc.getString("hospital");
                                cercano=loca;
                            }




                        }
                        catch (IOException ee) {
                            Toast.makeText(getApplicationContext(),ee.getMessage()+"",Toast.LENGTH_LONG).show();
                        }

                    }//for

                    if(!primeraVez)
                    {
                        LatLng hospital = new LatLng(cercano.getLatitude(),cercano.getLongitude());
                        MarkerOptions options = new MarkerOptions();
                        options.position(hospital);
                        mMap.addMarker(new MarkerOptions().position(hospital).title(nombreHospital +"-"+cercano.getAddressLine(0)));
                        hospitalCercano=hospital;
                        setTitle("Mapa");
                        LatLng origin = new LatLng(latitud,longitud);

                        if(hospitalCercano!=null && origin!=null)
                        {
                            String url = getDirectionsUrl(origin, hospitalCercano);
                            DownloadTask downloadTask = new DownloadTask();
                            downloadTask.execute(url);
                        }

                        for (MarkerOptions item : mMarkers.values())
                        {

                            String a,b;
                            a=item.getTitle().trim();
                            b=nombreHospital+"-"+cercano.getAddressLine(0).trim();

                            if(a.equals(b))
                            {
                                continue;
                            }
                            mMap.addMarker(item);
                        }





                    }else
                    {
                        mensaje("No hay hospitales registrados para la ciudad :"+ ciudad );
                    }







                }
            });


        }
        catch (IOException e) {
            // TODO Auto-generated catch block e.printStackTrace();
        }


        FirebaseAuth.getInstance().signOut();



    }

    private void dibujarMiubicacion(Location location)
    {
        longitud=location.getLongitude();
        latitud=location.getLatitude();
        LatLng ubicacion = new LatLng(latitud,longitud);
        mMap.addMarker(new MarkerOptions().position(ubicacion).title("Mi ubicación."));
        float zoomLevel = 16.0f;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion,zoomLevel));
    }

    private class  DownloadTask extends AsyncTask<String, Void, String>
    {


        @Override
        protected String doInBackground(String... url) {
            String data = "";

            try
            {


                data = downloadUrl(url[0]);

            } catch (Exception e)
            {
                //Log.d("Background Task", e.toString());
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}


    }


    private class ParserTask extends AsyncTask <String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {

                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }


        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result)
        {
            if(result.size()>0)
            {
                ArrayList points = null;
                PolylineOptions lineOptions = null;
                MarkerOptions markerOptions = new MarkerOptions();

                for (int i = 0; i < result.size(); i++)
                {
                    points = new ArrayList();
                    lineOptions = new PolylineOptions();

                    List<HashMap<String, String>> path = result.get(i);

                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    lineOptions.addAll(points);
                    lineOptions.width(12);
                    lineOptions.color(Color.GREEN);
                    lineOptions.geodesic(true);

                }

                // Drawing polyline in the Google Map for the i-th route
                mMap.addPolyline(lineOptions);
            }


        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters+"&key=AIzaSyAiFYGwScHsT-yWDBfybQRpCmRIAUzH1Xs";


        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            //Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, vectorDrawableResourceId);

        background.setBounds(0, 0, 100, 100);
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(100, 100,100 , 100);

        Bitmap bitmap =
                Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void mensaje(String mensaje)
    {
        Toast toast = Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}



