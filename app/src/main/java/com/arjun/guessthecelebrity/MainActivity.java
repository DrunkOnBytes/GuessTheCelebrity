package com.arjun.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebURLs= new ArrayList<String>();
    ArrayList<String> names= new ArrayList<String>();
    int chosenCeleb=0,correctAnswer;
    ImageView imageView;
    String[] answers=new String[4];
    Button b1,b2,b3,b4;
    DownloadTask task;
    String result;


    public class DownloadTask extends AsyncTask<String,Void,String>{


        @Override
        protected String doInBackground(String... urls) {

            String result="";
            URL url;
            HttpURLConnection urlConnection=null;

            try {
                url=new URL(urls[0]);
                urlConnection=(HttpURLConnection)url.openConnection();
                InputStream in= urlConnection.getInputStream();
                InputStreamReader reader=new InputStreamReader(in);
                int data=reader.read();
                while (data!=-1){

                    char current=(char)data;
                    result+=current;
                    data=reader.read();
                }
                return result;
            }
            catch (Exception e){

                e.printStackTrace();
                return "Failed";
            }


        }
    }

    public class ImageDownloader extends AsyncTask<String,Void, Bitmap>{


        @Override
        protected Bitmap doInBackground(String... urls) {

            try {

                URL url=new URL(urls[0]);
                HttpURLConnection connection=(HttpURLConnection)url.openConnection();
                connection.connect();
                InputStream inputStream=connection.getInputStream();
                Bitmap myBitmap= BitmapFactory.decodeStream(inputStream);

                return myBitmap;

            } catch (IOException e){

                e.printStackTrace();

            } catch (Exception e) {

                e.printStackTrace();

            }
            return null;
        }
    }


    public void celebChoose(View view){

        if(view.getTag().toString().equals(Integer.toString(correctAnswer)))
            Toast.makeText(getApplicationContext(),"Correct!",Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getApplicationContext(),"Wrong! It was "+names.get(chosenCeleb),Toast.LENGTH_LONG).show();
        nextQuestion();
    }

    public void nextQuestion(){


            Random random=new Random();
            chosenCeleb=random.nextInt(celebURLs.size());

            ImageDownloader imageTask=new ImageDownloader();
            Bitmap celebImage;
        try {
            celebImage=imageTask.execute(celebURLs.get(chosenCeleb)).get();
            imageView.setImageBitmap(celebImage);

            correctAnswer=random.nextInt(4);
            int incorrectanswerlocation;
            for (int i=0;i<4;i++){

                if(i==correctAnswer)
                    answers[i]=names.get(chosenCeleb);
                else{

                    incorrectanswerlocation=random.nextInt(celebURLs.size());
                    while (incorrectanswerlocation==correctAnswer)
                        incorrectanswerlocation=random.nextInt(celebURLs.size());
                    answers[i]=names.get(incorrectanswerlocation);
                }
            }
            b1.setText(answers[0]);
            b2.setText(answers[1]);
            b3.setText(answers[2]);
            b4.setText(answers[3]);

        } catch (ExecutionException e) {

            e.printStackTrace();
        } catch (InterruptedException e) {

            e.printStackTrace();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        task=new DownloadTask();
        imageView=(ImageView)findViewById(R.id.imageView);
        result= null;
        b1=(Button)findViewById(R.id.op1);
        b2=(Button)findViewById(R.id.op2);
        b3=(Button)findViewById(R.id.op3);
        b4=(Button)findViewById(R.id.op4);

        try {
            result = task.execute("http://www.posh24.se/kandisar").get();

            String[] splitResult = result.split("<div class=\"sidebarContainer\">");

            Pattern p = Pattern.compile("img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);
            while (m.find()) {

                celebURLs.add(m.group(1));

            }

            p = Pattern.compile("\" alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);
            while (m.find()) {

                names.add(m.group(1));

            }
        }
        catch (ExecutionException e) {

                 e.printStackTrace();

             }
        catch (InterruptedException e) {

                e.printStackTrace();

            }


            nextQuestion();
    }
}
