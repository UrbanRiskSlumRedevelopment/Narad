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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class Repeat extends AppCompatActivity {
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeat);
        LinearLayout chunk = new LinearLayout(getApplicationContext());
        chunk.setOrientation(LinearLayout.VERTICAL);

        Bundle bundle = getIntent().getExtras();
        NodeList nodes = (NodeList) bundle.getSerializable("nodes");

        System.out.println(nodes.getLength());

        for (int temp = 0; temp < nodes.getLength(); temp++) {
            System.out.println("add a question");
            LinearLayout repeat = new LinearLayout(context);
            Node nNode = nodes.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                LinearLayout q = null;
                Element eElement = (Element) nNode;
                String a = " #" + Integer.toString((int) Questionnaire.Counter.size()+1);
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
                        String x = e.getElementsByTagName("ctext").item(0).getTextContent().toString();
                        c.add(x);
                    }
                    q = Questionnaire.SingleChoice(text, c, hint, context, Questionnaire.builder);
                } else if (type.equals("MC")) {
                    List c = new ArrayList();
                    NodeList choices = eElement.getElementsByTagName("choice");
                    for (int i = 0; i < choices.getLength(); i++) {
                        Node choice = choices.item(i);
                        Element e = (Element) choice;
                        String x = e.getElementsByTagName("ctext").item(0).getTextContent().toString();
                        c.add(x);
                    }
                    q = Questionnaire.MultipleChoice(text, c, hint, context, Questionnaire.builder);
                }
                setupUI(q);
                Questionnaire.Questions2.add(q);
                repeat.addView(q);

            }
            chunk.addView(repeat);
        }

        Button bt = new Button(context);
        bt.setText("Done");
        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
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

    public Activity a = this;

    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

                    hideSoftKeyboard(a);
                    return false;
                }
            });
        }
    }
}
