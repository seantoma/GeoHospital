package com.example.geohospital;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Data
{

    private FirebaseFirestore db;
    private List<String> hospital;

    private  void inicializar()
    {
        db= FirebaseFirestore.getInstance();
    }


 public List<String> verificar(String direccion)
 {
     String[] parts = direccion.split(",");
     return consultarHospitalCiudad(parts[2]);

 }

    private List<String> consultarHospitalCiudad(String ciudad)
    {


        CollectionReference cRf= db.collection("Hospital");
        Query query = cRf.whereEqualTo("ciudad",ciudad);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot value, @javax.annotation.Nullable FirebaseFirestoreException e) {


                for (QueryDocumentSnapshot doc:value)
                {

                    hospital.add( doc.getString("hospital")  );

                  //  actualizar(doc.getString("hospital"),doc.getString("ciudad" ),doc.getString("hospital")+","+doc.getString("ciudad" ) ,"","",doc.getReference().getId());

                }
            }
        });


          return hospital;
    }


public void actualizar(String ciudad,String hospital,String mensaje,String latiud,String longitud,String id )
{
    inicializar();

    Map<String,Object> objHospital=new HashMap<>();
    objHospital.put("ciudad", ciudad);
    objHospital.put("nombre", hospital);
    objHospital.put("mensaje", mensaje);
    //objHospital.put("latiud",latiud );
    //objHospital.put("longitud",longitud );

    DocumentReference newCityRef = db.collection("mascota").document( id );

    newCityRef.set(objHospital);
    objHospital.clear();

}



}
