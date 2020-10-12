package com.denimprive.fabricrelaxation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sdsmdg.tastytoast.TastyToast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoginActivity extends AppCompatActivity {
    EditText edtusername,edtpass;
    Button btnlogin;
    ProgressBar pbbar;
    public String userid;

    ConnectionClass connectionClass;
    String ip, db, un, passwords;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        connectionClass = new ConnectionClass();
        ip = "10.100.1.5";
        un = "sa";
        passwords = "Compaq786";
        db = "ERPMS";

        edtusername = findViewById(R.id.edtusername);
        edtpass = findViewById(R.id.edtpass);
        btnlogin = findViewById(R.id.btnlogin);
        pbbar = findViewById(R.id.pbbar);
        pbbar.setVisibility(View.GONE);

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoLogin doLogin = new DoLogin();
                doLogin.execute("");

            }
        });
    }

    public class DoLogin extends AsyncTask<String,String,String>
    {
        String z = "";
        Boolean isSuccess = false;

        String username = edtusername.getText().toString();
        String password = edtpass.getText().toString();


        @Override
        protected void onPreExecute() {
            pbbar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String r) {
            pbbar.setVisibility(View.GONE);

            Toast.makeText(LoginActivity.this,r,Toast.LENGTH_SHORT).show();

            if(isSuccess) {
                getMasterId();
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
//                userid = edtuserid.getText().toString();
                i.putExtra("Value", userid);
                startActivity(i);
                finish();
            }

        }

        @Override
        protected String doInBackground(String... params) {
            if(username.trim().equals("")|| password.trim().equals(""))
                z = "Please enter User Id and Password";
//                TastyToast.makeText(getApplicationContext(), "Please enter User Id and Password", TastyToast.LENGTH_LONG,
//                        TastyToast.INFO);
            else
            {
                try {
                    Connection con = connectionClass.CONN();
                    if (con == null) {
                        z = "Error in connection with SQL server";
//                        TastyToast.makeText(getApplicationContext(), "Error in connection with SQL server", TastyToast.LENGTH_LONG,
//                                TastyToast.ERROR);
                    } else {
                        String query = "select * from Users where Users_Name='" + username + "' and Users_Password='" + password + "'";
                        Statement stmt = con.createStatement();
                        ResultSet rs = stmt.executeQuery(query);

                        if(rs.next())
                        {
                            z = "Login Successfull";
//                            TastyToast.makeText(getApplicationContext(), "Login Successfull", TastyToast.LENGTH_LONG,
//                                    TastyToast.SUCCESS);
                            isSuccess=true;
                        }
                        else
                        {
                            z = "Invalid Credentials";
//                            TastyToast.makeText(getApplicationContext(), "Invalid Credentials", TastyToast.LENGTH_LONG,
//                                    TastyToast.INFO);
//                            isSuccess = false;
                        }

                    }
                }
                catch (Exception ex)
                {
                    isSuccess = false;
                    z = "Exceptions";
//                    TastyToast.makeText(getApplicationContext(), "Exceptions", TastyToast.LENGTH_LONG,
//                            TastyToast.CONFUSING);
                }
            }
            return z;
        }
    }

    public void getMasterId() {

        Connection con = CONN(un, passwords, db, ip);
        String query1 = "select Users_Id from Users where Users_Inactive = 0 and Users_Name = '"+edtusername.getText().toString()+"'";

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query1);

            while (rs.next()) {
                userid = rs.getString("Users_Id");
//                edtuserid.setText(id);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

//CONNECTION API

    @SuppressLint("NewApi")
    private Connection CONN(String _user, String _pass, String _DB,
                            String _server) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String ConnURL = null;
        try {

            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnURL = "jdbc:jtds:sqlserver://" + _server + ";"
                    + "databaseName=" + _DB + ";user=" + _user + ";password="
                    + _pass + ";";
            conn = DriverManager.getConnection(ConnURL);
        } catch (SQLException se) {
            Log.e("ERRO", se.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("ERRO", e.getMessage());
        } catch (Exception e) {
            Log.e("ERRO", e.getMessage());
        }
        return conn;
    }
}
