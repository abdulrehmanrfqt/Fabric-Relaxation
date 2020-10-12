package com.denimprive.fabricrelaxation;

import androidx.appcompat.app.AppCompatActivity;
import android.IntentIntegrator;
import android.IntentResult;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sdsmdg.tastytoast.TastyToast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ConnectionClass connectionClass;
    EditText edtcode;
    ProgressBar pbbar;
    ListView barcodeListView;
    DatePickerDialog datePickerDialog;
    public String barcodeId;
    public String Date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        connectionClass = new ConnectionClass();
        edtcode = findViewById(R.id.edtcode);

        pbbar = findViewById(R.id.pbbar);
        pbbar.setVisibility(View.GONE);

        barcodeListView = findViewById(R.id.barcodeListView);

        String st = getIntent().getStringExtra("Value");

// GETTING CURRENT DATE //

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("dd-MMMM-yyyy");
//        String formattedDate = df.format(c.getTime());
        Date = df.format(c.getTime());
        // formattedDate have current date/time

        TastyToast.makeText(getApplicationContext(), Date, TastyToast.LENGTH_LONG,
                TastyToast.DEFAULT);

//        Date = formattedDate;
//        Date.setTextSize(20);

        // Now we display formattedDate value in TextView
//        TextView txtView = new TextView(this);
//        txtView.setText(formattedDate);
//        txtView.setGravity(Gravity.CENTER);
//        txtView.setTextSize(20);
//        setContentView(txtView);

        edtcode.setOnClickListener(this);

        getAddedBarcodes();
    }

//OPEN SCANNER//

    public void onClick(View v) {

        if (v.getId() == R.id.edtcode) {
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            barcodeId = scanningResult.getContents();

            TastyToast.makeText(getApplicationContext(), "Barcode Scanned!", TastyToast.LENGTH_LONG,
                    TastyToast.SUCCESS);

            if (barcodeId != null) {
                MasterPostRequest();

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                getAddedBarcodes();
            } else {
                TastyToast.makeText(getApplicationContext(), "No scan data received!", TastyToast.LENGTH_LONG,
                        TastyToast.CONFUSING);
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            barcodeId = "";

        } else {
            TastyToast.makeText(getApplicationContext(), "No scan data received!", TastyToast.LENGTH_LONG,
                    TastyToast.CONFUSING);
        }

    }

// MASTER POST METHOD//

    public void MasterPostRequest() {

//        final String date = DenimPrive.getText().toString().trim();
        final String userId = getIntent().getStringExtra("Value");

        //validations

        if (TextUtils.isEmpty(barcodeId)) {
            TastyToast.makeText(getApplicationContext(), "Please Scan Barcode", TastyToast.LENGTH_LONG,
                    TastyToast.ERROR);
//            edtcode.setError("Please Scan Barcode");
//            edtcode.requestFocus();
            return;
        }


        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        String url = "http://58.27.255.29:90/api/FabricRelaxation/FabricRelaxationAdd";
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                //parse json data
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    edtcode.append(jsonObject.getString("MasterID")+"\n");
//                }
//                catch (Exception e){
//                    e.printStackTrace();
////                    post_response_text.setText("POST DATA : unable to Parse Json");
//                    Toast toast = Toast.makeText(getApplicationContext(),
//                            "Unable to Parse Json", Toast.LENGTH_SHORT);
//                    toast.show();
//                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                post_response_text.setText("Post Data : Response Failed");
                TastyToast.makeText(getApplicationContext(), "Response failed!", TastyToast.LENGTH_LONG,
                        TastyToast.ERROR);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("FabricRelaxationBarcode_ReceivingRoll_Id_Fk", barcodeId);
                params.put("FabricRelaxationBarcode_Date", Date);
                params.put("FabricRelaxationBarcode_CreatedBy", userId);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }

        };

        requestQueue.add(stringRequest);

    }

    public void getAddedBarcodes() {

        String z = "";
        List<Map<String, String>> addeddefectslist = new ArrayList<Map<String, String>>();

        try {
            Connection con = connectionClass.CONN();
            if (con == null) {
                z = "Error in connection with SQL server";
            } else {
                String query = "EXEC USP_FabricRelaxationBarcode_Get";
                PreparedStatement ps = con.prepareStatement(query);
                ResultSet rs = ps.executeQuery();

                ArrayList data1 = new ArrayList();
                while (rs.next()) {
                    Map<String, String> datanum = new HashMap<String, String>();
                    datanum.put("A", rs.getString("Fabric_FabricCode"));
                    datanum.put("B", rs.getString("FabricReceivingDetail_QtyInMeters"));
                    datanum.put("C", rs.getString("FabricReceivingDetail_RollNo"));
                    datanum.put("D", rs.getString("FabricRelaxationBarcode_CreatedDate"));
                    addeddefectslist.add(datanum);
                }
                z = "Success";

            }

        } catch (Exception ex) {
            z = "Error retrieving data from table";

        }

        final String[] from = {"A", "B", "C", "D"};
        int[] views = {R.id.lblFabricCode, R.id.lblQtyInMeters, R.id.lblRollNo, R.id.lblDate};
        final SimpleAdapter ADA = new SimpleAdapter(MainActivity.this, addeddefectslist, R.layout.barcode_detail_list_template, from, views);

        barcodeListView.setAdapter(ADA);
    }



    public void showSuccessToast(View view) {
        TastyToast.makeText(getApplicationContext(), "Download Successful !", TastyToast.LENGTH_LONG,
                TastyToast.SUCCESS);
    }

    public void showWarningToast(View view) {
        TastyToast.makeText(getApplicationContext(), "Are you sure ?", TastyToast.LENGTH_LONG,
                TastyToast.WARNING);
    }

    public void showErrorToast(View view) {
        TastyToast.makeText(getApplicationContext(), "Downloading failed ! Try again later ", TastyToast.LENGTH_LONG,
                TastyToast.ERROR);
    }
    public void showInfoToast(View view) {
        TastyToast.makeText(getApplicationContext(), "Searching for username : 'Rahul' ", TastyToast.LENGTH_LONG,
                TastyToast.INFO);
    }

    public void showDefaultToast(View view) {
        TastyToast.makeText(getApplicationContext(), "This is Default Toast", TastyToast.LENGTH_LONG,
                TastyToast.DEFAULT);
    }


    public void showConfusingToast(View view) {
        TastyToast.makeText(getApplicationContext(), "I don't Know !", TastyToast.LENGTH_LONG,
                TastyToast.CONFUSING);
    }
}