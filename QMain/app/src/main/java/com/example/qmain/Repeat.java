package com.example.qmain;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.AlertDialog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.widget.Button;
import android.widget.ScrollView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Repeat extends AppCompatActivity {
    Context context = this;
    List rqs = new ArrayList();
    EditText id = null;
    public static String ANSWERS = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeat);
        ScrollView scroll_layout = (ScrollView) findViewById(R.id.activity_repeat);
        LinearLayout chunk = new LinearLayout(getApplicationContext());
        chunk.setOrientation(LinearLayout.VERTICAL);
        scroll_layout.addView(chunk);

        Bundle bundle = getIntent().getExtras();
        NodeList nodes = null;
        String name = bundle.getString("group name");

        Boolean skip = false;

        try {
            InputStream in = getResources().openRawResource(R.raw.questions);
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(in);
            doc.getDocumentElement().normalize();
            NodeList groups = doc.getElementsByTagName("group");
            for (int g = 0; g < groups.getLength(); g++) {
                Node gr = groups.item(g);
                Element eE = (Element) gr;
                String gname = ((Element) gr).getElementsByTagName("gtext").item(0).getTextContent();
                if (gname.equals(name)){
                    nodes = eE.getElementsByTagName("question");
                }
                else{
                    System.out.println(gname);
                    System.out.println(name);
                }
            }
        } catch (Exception e){
            skip = true;
        }
        /*
        TextView tv = new TextView(this);
        String s = "Enter unique identifier for entry*";
        tv.setText(s);
        tv.setTextSize(20);
        id = new EditText(this);
        id.setHint("enter here");
        chunk.addView(tv);
        chunk.addView(id);
        */
        for (int temp = 0; temp < nodes.getLength(); temp++) {
            if(skip){
                break;
            }
            System.out.println("add a question");
            LinearLayout repeat = new LinearLayout(context);
            repeat.setOrientation(LinearLayout.VERTICAL);
            Node nNode = nodes.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                LinearLayout q = null;
                Element eElement = (Element) nNode;
                String a = " #" + Integer.toString(Questionnaire.Counter.size());
                String text = "";
                if(eElement.getElementsByTagName("req").item(0).getTextContent().equals("T")){
                    text = eElement.getElementsByTagName("qtext").item(0).getTextContent()+a+"*";
                }
                else{
                    text = eElement.getElementsByTagName("qtext").item(0).getTextContent()+a;
                }
                String type = eElement.getElementsByTagName("qtype").item(0).getTextContent();
                String hint = eElement.getElementsByTagName("qhint").item(0).getTextContent();
                if (type.equals("T")) {
                    q = Questionnaire.TextQ(text, hint, context);
                } else if (type.equals("N")) {
                    q = Questionnaire.NumQ(text, hint, context);
                } else if (type.equals("SC")) {
                    List c = new ArrayList();
                    NodeList choices = eElement.getElementsByTagName("choice");
                    for (int i = 0; i < choices.getLength(); i++) {
                        Node choice = choices.item(i);
                        Element e = (Element) choice;
                        String x = e.getElementsByTagName("ctext").item(0).getTextContent();
                        c.add(x);
                    }
                    q = Questionnaire.SingleChoice(text, c, hint, context, Questionnaire.builder);
                } else if (type.equals("MC")) {
                    List c = new ArrayList();
                    NodeList choices = eElement.getElementsByTagName("choice");
                    for (int i = 0; i < choices.getLength(); i++) {
                        Node choice = choices.item(i);
                        Element e = (Element) choice;
                        String x = e.getElementsByTagName("ctext").item(0).getTextContent();
                        c.add(x);
                    }
                    q = Questionnaire.MultipleChoice(text, c, hint, context, Questionnaire.builder);
                }
                setupUI(q);
                Questionnaire.Questions2.add(q);
                rqs.add(q);
                repeat.addView(q);

            }
            chunk.addView(repeat);
        }

        Button bt = new Button(context);
        bt.setText("Done");
        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideSoftKeyboard(act);
                String repeatAnswers = Questionnaire.writeAnswers(rqs, false, null, true);
                //String identifier = id.getText().toString();
                //if(repeatAnswers.equals("") || identifier.equals("")){
                if(repeatAnswers.equals("")){
                    AlertDialog.Builder bdr = new AlertDialog.Builder(context);
                    bdr.setMessage("Answer all required questions before submitting");
                    AlertDialog dialog = bdr.create();
                    dialog.show();
                    //return;
                }
                else{
                    ANSWERS = repeatAnswers;
                    finish();
                }

            }
        });
        chunk.addView(bt);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    public Activity act = this;

    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

                    hideSoftKeyboard(act);
                    return false;
                }
            });
        }
    }

    // group by group
}
