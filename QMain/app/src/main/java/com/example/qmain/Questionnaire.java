package com.example.qmain;

import android.content.Context;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.List;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.content.Intent;
import android.app.AlertDialog;
import android.widget.CheckBox;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.app.ActivityManager;


import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.app.Dialog;

import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;


public class Questionnaire extends AppCompatActivity {
    public final static String DATA = "com.example.qmain.PREFERENCE_FILE_KEY";
    LinearLayout layout = null;
    public static NodeList nodes = null;
    List Questions = null;
    static List Questions2 = null;
    static List Counter = new ArrayList();
    public static AlertDialog.Builder builder = null;
    public Context context = this;
    public String LOCATION = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        ScrollView scroll_layout = (ScrollView) findViewById(R.id.activity_questionnaire);
        layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        scroll_layout.addView(layout);

        AlertDialog.Builder newbuilder = new AlertDialog.Builder(this);
        builder = newbuilder;

        final Context context = getApplicationContext();

        // parsing question
        try {
            InputStream in = getResources().openRawResource(R.raw.questions);
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(in);
            doc.getDocumentElement().normalize();
            NodeList groups = doc.getElementsByTagName("group");
            Questions = new ArrayList();
            Questions2 = new ArrayList();
            for(int g = 0; g<groups.getLength();g++){
                LinearLayout layout1 = new LinearLayout(context);
                Node gr = groups.item(g);
                Element eE = (Element) gr;
                String name = eE.getNodeName();
                NodeList nList = eE.getElementsByTagName("question");
                System.out.println(name);
                NodeList rList = eE.getElementsByTagName("repeatable");
                System.out.println(rList.item(0).getTextContent());
                final ArrayList xx = new ArrayList();
                for(int i = 0; i<100; i++){
                    xx.add(i);
                }
                if(eE.getElementsByTagName("repeatable").item(0).getTextContent().equals("T")){
                    LinearLayout chunk = new LinearLayout(context);
                    final NodeList nrList = nList;
                    nodes = nrList;
                    Button bt = new Button(this);
                    bt.setText("Add new " + ((Element) gr).getElementsByTagName("gtext").item(0).getTextContent());
                    bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.WRAP_CONTENT));

                    bt.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            /*
                            Intent intent = new Intent(context, Repeat.class);
                            startActivity(intent);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("nodes", (Serializable) nrList);
                            intent.putExtras(bundle);
                            Counter.add(1);
                            */
                        }
                    }
                    );
                    chunk.addView(bt);
                    layout.addView(chunk);

                }
                else {
                    for (int temp = 0; temp < nList.getLength(); temp++) {
                        Node nNode = nList.item(temp);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            LinearLayout q = null;
                            Element eElement = (Element) nNode;
                            String text = "";
                            String type = eElement.getElementsByTagName("qtype").item(0).getTextContent();
                            String hint = eElement.getElementsByTagName("qhint").item(0).getTextContent();
                            if(eElement.getElementsByTagName("req").item(0).getTextContent().equals("T")){
                                text = eElement.getElementsByTagName("qtext").item(0).getTextContent()+"*";
                            }
                            else{
                                text = eElement.getElementsByTagName("qtext").item(0).getTextContent();
                            }
                            if (type.equals("T")) {
                                q = TextQ(text, hint, context);
                            } else if (type.equals("N")) {
                                q = NumQ(text, hint, context);
                            } else if (type.equals("SC")) {
                                List c = new ArrayList();
                                NodeList choices = eElement.getElementsByTagName("choice");
                                for (int i = 0; i < choices.getLength(); i++) {
                                    Node choice = choices.item(i);
                                    Element e = (Element) choice;
                                    String x = e.getElementsByTagName("ctext").item(0).getTextContent().toString();
                                    c.add(x);
                                }
                                q = SingleChoice(text, c, hint, context, builder);
                            } else if (type.equals("MC")) {
                                List c = new ArrayList();
                                NodeList choices = eElement.getElementsByTagName("choice");
                                for (int i = 0; i < choices.getLength(); i++) {
                                    Node choice = choices.item(i);
                                    Element e = (Element) choice;
                                    String x = e.getElementsByTagName("ctext").item(0).getTextContent().toString();
                                    c.add(x);
                                }
                                q = MultipleChoice(text, c, hint, context, builder);
                            } else if (type.equals("M")){
                                q = Map(text, context);
                            } else if (type.equals("C")){
                                q = Camera(text, context);
                            }
                            setupUI(q);
                            layout.addView(q);
                            Questions.add(q);
                        }
                    }
                }
            }

            Button bt = new Button(this);
            bt.setText("Submit");
            bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.WRAP_CONTENT));

            bt.setOnClickListener(new View.OnClickListener() {
                                      public void onClick(View v) {
                                          Button b = (Button) v;
                                          submit();
                                      }
                                  }
            );

            layout.addView(bt);
        } catch (Exception e) {
            e.printStackTrace();}


    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    public Activity a = this;

    public void setupUI(View view) {
        // lose focus on text
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

                    hideSoftKeyboard(a);
                    return false;
                }
            });
        }
    }

    public static LinearLayout TextQ(String questiontext, String hint, Context context){
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);
        final EditText edittext = new EditText(context);
        edittext.setHint(hint);
        LinearLayout qlayout = new LinearLayout(context);
        qlayout.setOrientation(LinearLayout.VERTICAL);
        text.setTag("text");
        edittext.setTag("answer");
        qlayout.addView(text);
        qlayout.addView(edittext);
        qlayout.setTag("T");
        return qlayout;
    }

    public static LinearLayout NumQ(String questiontext, String hint, Context context){
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);
        EditText edittext = new EditText(context);
        edittext.setHint(hint);
        edittext.setInputType(2);
        text.setTag("text");
        edittext.setTag("answer");
        LinearLayout qlayout = new LinearLayout(context);
        qlayout.setOrientation(LinearLayout.VERTICAL);
        qlayout.addView(text);
        qlayout.addView(edittext);
        qlayout.setTag("N");
        return qlayout;
    }

    public static LinearLayout SingleChoice(String questiontext, List choices,
                                            final String hint, Context context, AlertDialog.Builder builder){
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);
        RadioGroup rg = new RadioGroup(context);
        for (int i=0; i<choices.size(); i++) {
            RadioButton rb = new RadioButton(rg.getContext());
            String btext = choices.get(i).toString();
            rb.setId(i);
            rb.setText(btext);
            rg.addView(rb);
            System.out.println(rb.getId());
        }
        text.setTag("text");
        rg.setTag("choices");

        Button bt = new Button(context);
        bt.setText("?");
        bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT));

        final AlertDialog.Builder bdr = builder;

        bt.setOnClickListener(new View.OnClickListener() {
                                  public void onClick(View v) {
                                      bdr.setMessage(hint);
                                      AlertDialog dialog = bdr.create();
                                      dialog.show();
                                  }
                              });

        LinearLayout qlayout = new LinearLayout(context);
        qlayout.setOrientation(LinearLayout.VERTICAL);
        qlayout.addView(text);
        qlayout.addView(rg);
        qlayout.addView(bt);
        bt.setTag("button");
        qlayout.setTag("SC");
        return qlayout;
    }

    int PLACE_PICKER_REQUEST = 1;

    public LinearLayout Map(String questiontext, Context context){
        // save location
        LinearLayout qlayout = new LinearLayout(context);
        TextView tv = new TextView(context);
        tv.setTag("text");
        tv.setText(questiontext);
        Button bt = new Button(context);
        bt.setText(questiontext);
        bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT));
        final Context c = context;
        final Activity a = this;

        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(a), PLACE_PICKER_REQUEST);
                }catch(GooglePlayServicesNotAvailableException e){
                    System.out.println("didn't work");
                }catch(GooglePlayServicesRepairableException e){
                    System.out.println(":(");
                }

            }
        });

        qlayout.addView(tv);
        qlayout.addView(bt);
        qlayout.setTag("M");
        return qlayout;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String toastMsg = String.format("Place: %s", place.getLatLng());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                System.out.println(toastMsg);
                System.out.println(PlacePicker.getLatLngBounds(data));
                LOCATION = toastMsg.substring(7);
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public LinearLayout Camera(String questiontext, Context context){
        LinearLayout qlayout = new LinearLayout(context);
        TextView tv = new TextView(context);
        tv.setTag("text");
        tv.setText(questiontext);
        Button bt = new Button(context);
        bt.setText(questiontext);
        bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT));
        final Context c = context;
        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        qlayout.addView(tv);
        qlayout.addView(bt);
        qlayout.setTag("C");
        return qlayout;
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    public static LinearLayout MultipleChoice(String questiontext, List choices,
                                              final String hint, Context context, AlertDialog.Builder builder){
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);

        LinearLayout qlayout = new LinearLayout(context);
        qlayout.setOrientation(LinearLayout.VERTICAL);
        qlayout.addView(text);

        for (int i=0; i<choices.size(); i++) {
            CheckBox cb = new CheckBox(context);
            String ctext = choices.get(i).toString();
            cb.setId(i);
            cb.setText(ctext);
            cb.setTag("choice");
            qlayout.addView(cb);
        }

        text.setTag("text");

        Button bt = new Button(context);
        bt.setText("?");
        bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT));

        final AlertDialog.Builder bdr = builder;

        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bdr.setMessage(hint);
                AlertDialog dialog = bdr.create();
                dialog.show();
            }
        });

        qlayout.addView(bt);
        bt.setTag("button");
        qlayout.setTag("MC");
        return qlayout;
    }

    public String submit(){
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String filename = timeStamp+".txt";
        System.out.println("we here");
        FileOutputStream fos = null;
        try{
            fos = openFileOutput(filename, Context.MODE_PRIVATE);
            System.out.println("adkgjaldkfjladkjglkadfjklda");
            System.out.println(getFileStreamPath(filename));
        }catch(Exception e){
            return "";
        }

        for(int i = 0; i<Questions2.size();i++){
            Questions.add(Questions2.get(i));
        }

        for(int i = 0; i<Questions.size();i++){
            LinearLayout q = (LinearLayout) Questions.get(i);
            TextView text = (TextView) q.findViewWithTag("text");
            String question = (String) text.getText();
            String line = "";
            String tag = "";
            try{
                tag = (String) q.getTag();
            }catch(Exception e){
                System.out.println("there's no tag?");
            }

            System.out.println(tag);
            if(tag.equals("T") || tag.equals("N")){
                EditText editText = (EditText) q.findViewWithTag("answer");
                if(editText.getText().toString().equals("") && question.endsWith("*")){
                    AlertDialog.Builder bdr = builder;
                    bdr.setMessage("Answer all required questions before submitting");
                    AlertDialog dialog = bdr.create();
                    dialog.show();
                    return "";
                }
                else {
                    line = question + ": " + editText.getText();
                }
            }
            else if(tag.equals("SC")){
                line = question + ": ";
                RadioGroup rg = (RadioGroup) q.findViewWithTag("choices");
                int id = rg.getCheckedRadioButtonId();
                if(id == -1){
                    AlertDialog.Builder bdr = builder;
                    bdr.setMessage("Answer all required questions before submitting");
                    AlertDialog dialog = bdr.create();
                    dialog.show();
                    return "";
                }else{
                    RadioButton rb = (RadioButton) rg.getChildAt(id);
                    System.out.println(rg.getChildAt(id));
                    System.out.println(rg.getChildCount());
                    System.out.println(id);
                    try{
                        line += rb.getText();
                        System.out.println(line);
                    }catch(Exception e){}
                }

            }
            else if(tag.equals("MC")){
                line = question + ": ";
                System.out.println(q.getChildCount());
                for (int j=0; j<q.getChildCount(); j++){
                    System.out.println(q.getChildAt(j).getTag());
                    String ctag = (String) q.getChildAt(j).getTag();
                    if(ctag.equals("choice")){
                        CheckBox cb = (CheckBox) q.getChildAt(j);
                        if(cb.isChecked()){
                            line += cb.getText() + ", ";
                        }
                    }
                }
                if(line.length()>20){
                    line = line.substring(0, line.length()-2);
                }
                System.out.println(line);
            }
            else if(tag.equals("M")){
                line = question + ": " + LOCATION;
            }
            else if(tag.equals("C")){
                line = question + ": ";
            }
            try{
                line = line + "~~";
                fos.write(line.getBytes());
            }catch(Exception e){}
        }

        try {
            fos.close();
        }catch(Exception e){}

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        return filename;

        //hexcode, title from question field, device id, account

    }



}