package azure.snmc;

//import android.app.Fragment;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TabPrayerCsv extends Fragment {

    private static final String TAG = MainActivity1.class.getSimpleName();
    private static final String PATH_TO_SERVER = "http://mysafeinfo.com/api/data?list=dowjonescompanies&format=csv";//path to .csv file
    InputStream inputStream;

    private Date EndTime;
    private Date CurrentTime = null;

    private TextView txtHour;
    private TextView txtMinute;
    private Handler handler;
    private List<String> array;
    private SimpleDateFormat timeFormat;

    public TabPrayerCsv() {
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_prayer, container, false);
        return view;
    }

    public static TabPrayerCsv newInstance(String text) {

        Bundle args = new Bundle();
        args.putString("msg", text);

        TabPrayerCsv fragment = new TabPrayerCsv();
        fragment.setArguments(args);
        return fragment;
    }

    public void onStart() {
        super.onStart();
        SimpleDateFormat curFormater = new SimpleDateFormat("EEEE MMM dd, yyyy",Locale.CANADA);
        String currentDate = curFormater.format(new Date());
         TextView fajrAtv, fajrItv, zuhrItv, asarItv, meghribItv, ishaItv, zuhrAtv, asarAtv, meghribAtv, ishaAtv, datetv,prayertextJumamah1,prayertextJumamah2;

        datetv =  getActivity().findViewById(R.id.datetv);
        fajrAtv = getActivity().findViewById(R.id.fajrA);
        fajrItv = getActivity().findViewById(R.id.fajrI);
        zuhrAtv =  getActivity().findViewById(R.id.zuhrA);
        zuhrItv = getActivity().findViewById(R.id.zuhrI);
        asarAtv = getActivity().findViewById(R.id.asarA);
        asarItv = getActivity().findViewById(R.id.asarI);
        meghribAtv = getActivity().findViewById(R.id.meghribA);
        meghribItv = getActivity().findViewById(R.id.meghribI);
        ishaItv = getActivity().findViewById(R.id.ishaI);
        ishaAtv = getActivity().findViewById(R.id.ishaA);
        prayertextJumamah1 =  getActivity().findViewById(R.id.jummahPrayerTV1);
        prayertextJumamah2 =  getActivity().findViewById(R.id.jummahPrayerTV2);
        prayertextJumamah1.setTextColor(Color.WHITE);
        prayertextJumamah2.setTextColor(Color.WHITE);

        txtHour =  getActivity().findViewById(R.id.textHour);
        txtMinute =  getActivity().findViewById(R.id.textMinutes);
        TextView txtNextName = getActivity().findViewById(R.id.tvNextPrayerName);

        timeFormat = new SimpleDateFormat("hh:mm a",Locale.CANADA);

//        /*if download csv file*/
//        DownloadFilesTask downloadFilesTask = new DownloadFilesTask();
//        downloadFilesTask.execute();
        /*if from asset csv file*///TODO
//        List<String[]> csvContent = readCVSFromAssetFolder();
//        printCVSContent(csvContent);

        DbHelper myDbHelper = new DbHelper(getActivity().getApplicationContext());

        try {
            myDbHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        myDbHelper.openDataBase();


        array = myDbHelper.getAllPrayersList();//        Toast.makeText(getActivity(), "array=" + array, Toast.LENGTH_SHORT).show(); //help for debugging
        if (!array.isEmpty()) {//if database has data

            datetv.setText(currentDate);
            fajrAtv.setText(array.get(1));
            fajrItv.setText(array.get(2));
            zuhrAtv.setText(array.get(3));
            zuhrItv.setText(array.get(4));
            asarAtv.setText(array.get(5));
            asarItv.setText(array.get(6));
            meghribAtv.setText(array.get(7));
            meghribItv.setText(array.get(8));
            ishaAtv.setText(array.get(9));
            ishaItv.setText(array.get(10));
            prayertextJumamah1.setText("1st Prayer: " + array.get(11));
            prayertextJumamah2.setText("2nd Prayer: " + array.get(12));
//            jumaahSubstring = "WINTER: 1st-12:00 PM   2nd-1:15PM \n SUMMER: 1st-1:00PM   2nd-2:15PM";

        } else {// todo
        }
           nextPrayerTime();
    }





    private void nextPrayerTime() {
        boolean nextDay = false;
        List<String> arrayIqama = new ArrayList<>();
        arrayIqama.add("12:00 AM");//hack way of finding time after next day starts
        arrayIqama.add(array.get(2));//Fajr
        arrayIqama.add(array.get(4));//zuhr
        arrayIqama.add(array.get(6));//Asr
        arrayIqama.add(array.get(8));//Meghrib
        arrayIqama.add(array.get(10));//Isha
        arrayIqama.add(array.get(2));//todo hack way of finding time in between isha and next fajr

        try {
            Date StartTime;
            CurrentTime = timeFormat.parse(timeFormat.format(new Date()));

            for (int i = 0; i < arrayIqama.size()-1; i++) {

                StartTime = timeFormat.parse(arrayIqama.get(i));//fixme not able to parse the time hh:mm a
                EndTime = timeFormat.parse(arrayIqama.get(i + 1));
                if(nextDay) {//If next day (fajr) times then add day
                    StartTime = AddDays(StartTime,1);
                    EndTime = AddDays(EndTime,1);
                }
                else if(EndTime.before(StartTime)) {//If end time before start (fajr) then end next day
                    nextDay = true;
                    EndTime = AddDays(EndTime,1);
                }

                if (CurrentTime.after(StartTime) && CurrentTime.before(EndTime)) {
    //              find prayer name i0=Fajr, i1=Zuhr...etc
                    countDownStart();
                break;
               }
               else{}//todo crash if after isha
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    private Date AddDays(Date dt, int days){
        Calendar cd =  Calendar.getInstance();
        cd.setTime(dt);
        cd.add(Calendar.DATE,1);
        return cd.getTime();
    }


    private void countDownStart() {
            handler = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                handler.postDelayed(this, 1000);
                try {
//                      if (!CurrentTime.after(EndTime)) {//fixme-not working with this condition
                    CurrentTime = timeFormat.parse(timeFormat.format(new Date()));
                    long diff = EndTime.getTime() - CurrentTime.getTime();
//                        if(diff<=0){}//todo to start  again when hits zero
                    long hours = diff / (60 * 60 * 1000);
                    diff -= hours * (60 * 60 * 1000);
                    long minutes = diff / (60 * 1000);
                    txtHour.setText("" + String.format("%02d", hours));
                    txtMinute.setText("" + String.format("%02d", minutes));
//                        }
//                        } else {//// FIXME: 2018-01-16  not needed, something else maybe, also need to stop any leak if any
//                        }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }//todo resume run countdown+ when time is 0 at fajr dont display 24

        };
            handler.postDelayed(runnable, 1000);
        }




    /*when from asset csv file*/
    private List<String[]> readCVSFromAssetFolder() {
        List<String[]> csvLine = new ArrayList<>();
        String[] content = null;
        try {
            inputStream = getActivity().getAssets().open("test.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";


            String tableName ="Prayertimetable";
            String columns = "_id, Date, FajrA, FajrI, ZuhrA,ZuhrI,AsarA,AsarI,MeghribA,MegribI,IshaA,IshaI, Jumah1,Jumah";
            String str1 = "INSERT INTO " + tableName + " (" + columns + ") values(";
            String str2 = ");";


//            SQLiteDatabase db = null;
//            db.execSQL("CREATE TABLE " + tableName + "(id INTEGER PRIMARY KEY, name TEXT)");
////            db.execSQL(".mode csv");
//            db.execSQL(".import res/raw/test.csv " + tableName);


//            db.enableWriteAheadLogging();
//            db.beginTransaction();
//            while ((line = br.readLine()) != null) {
//                StringBuilder sb = new StringBuilder(str1);
//                String[] str = line.split(",");
//                sb.append("'" + str[0] + "',");
//                sb.append(str[1] + "',");
//                sb.append(str[2] + "',");
//                sb.append(str[3] + "'");
//                sb.append(str[4] + "'");
//                sb.append(str[5] + "'");
//                sb.append(str[5] + "'");
//                sb.append(str[7] + "'");
//                sb.append(str[8] + "'");
//                sb.append(str[9] + "'");
//                sb.append(str[10] + "'");
//                sb.append(str[11] + "'");
//                sb.append(str[12] + "'");
//                sb.append(str[13] + "'");
//                sb.append(str2);
//                db.execSQL(sb.toString());
//            }
//            db.setTransactionSuccessful();
//            db.endTransaction();


//OR simply if just making list and not in DB
            while((line = br.readLine()) != null){
                content = line.split(",");
                csvLine.add(content);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvLine;
    }

    private void printCVSContent(List<String[]> result){//todo in both cases of csv place. Place after DownloadFileTask etc
        String cvsColumn = "";
        for (int i = 0; i < result.size(); i++){
            String [] rows = result.get(i);

            cvsColumn += rows[0] + " " + rows[1] + "\n";
        }
        txtHour.setText(cvsColumn);//fixme ow to run query-copy it into database?
    }



    /*if from download csv file*/
    private class DownloadFilesTask extends AsyncTask<URL, Void, List<String[]>> {
        protected List<String[]> doInBackground(URL... urls) {
            return downloadRemoteTextFileContent();
        }
        protected void onPostExecute(List<String[]> result) {
            if(result != null){
                printCVSContent(result);
            }
        }
    }

    private List<String[]> downloadRemoteTextFileContent(){
        URL mUrl = null;
        List<String[]> csvLine = new ArrayList<>();
        String[] content = null;
        try {
            mUrl = new URL(PATH_TO_SERVER);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            assert mUrl != null;
            URLConnection connection = mUrl.openConnection();
            BufferedReader br = new BufferedReader(new
                    InputStreamReader(connection.getInputStream()));
            String line = "";
            while((line = br.readLine()) != null){
                content = line.split(",");
                csvLine.add(content);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvLine;
    }


}
