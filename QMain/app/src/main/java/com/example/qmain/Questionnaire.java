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

import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.text.Editable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.content.Intent;
import android.app.AlertDialog;
import android.widget.CheckBox;
import android.view.ViewGroup;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.app.ActivityManager;

import android.content.SharedPreferences;

import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import android.support.v7.app.ActionBar.LayoutParams;


public class Questionnaire extends AppCompatActivity {
    public final static String DATA = "com.example.qmain.PREFERENCE_FILE_KEY";
    LinearLayout layout = null;
    public static AlertDialog.Builder builder = null;
    public Context context = this;

    public static LinearLayout build_question(Node nNode, List list2,
                                              LinearLayout layout1, Context context, HashMap qns, HashMap ds){
        LinearLayout q = null;
        Element eElement = (Element) nNode;

        String text;
        String type = eElement.getElementsByTagName("qtype").item(0).getTextContent();
        String hint = eElement.getElementsByTagName("qhint").item(0).getTextContent();
        String parent;
        if(eElement.getElementsByTagName("req").item(0).getTextContent().equals("T")){
            text = eElement.getElementsByTagName("qtext").item(0).getTextContent()+"*";
        }
        else{
            text = eElement.getElementsByTagName("qtext").item(0).getTextContent();
        }
        try{
            parent = eElement.getElementsByTagName("parent").item(0).getTextContent();
        }catch(Exception e){
            parent = null;
        }
        List extras = new ArrayList();
        if (type.equals("T")) {
            q = TextQ(text, hint, context, parent, qns);
        } else if (type.equals("N")) {
            q = NumQ(text, hint, context, parent, qns);
        } else if (type.equals("SC")) {
            List c = new ArrayList();
            NodeList choices = eElement.getElementsByTagName("choice");
            HashMap<String, ArrayList> dependencies = new HashMap<>();
            for (int i = 0; i < choices.getLength(); i++) {
                Node choice = choices.item(i);
                Element e = (Element) choice;
                String x = e.getElementsByTagName("ctext").item(0).getTextContent();
                c.add(x);
                try {
                    String dependents = e.getElementsByTagName("dependents").item(0).getTextContent();
                    dependents.replace(" ","");
                    String[] deps = dependents.split(",");
                    ArrayList dps = new ArrayList();
                    for(int d = 0; d < deps.length; d++){
                        dps.add(deps[d]);
                    }
                    dependencies.put(x, dps);
                }catch(Exception ex){
                    System.out.println("no dependencies");
                }
            }
            builder = PVQ.builder;
            q = SingleChoice(text, c, hint, context, builder, qns, ds, dependencies, parent);
        } else if (type.equals("MC")) {
            List c = new ArrayList();
            NodeList choices = eElement.getElementsByTagName("choice");
            HashMap<String, ArrayList> dependencies = new HashMap<>();
            for (int i = 0; i < choices.getLength(); i++) {
                Node choice = choices.item(i);
                Element e = (Element) choice;
                String x = e.getElementsByTagName("ctext").item(0).getTextContent();
                c.add(x);
                try {
                    String dependents = e.getElementsByTagName("dependents").item(0).getTextContent();
                    dependents.replace(" ","");
                    String[] deps = dependents.split(",");
                    ArrayList dps = new ArrayList();
                    for(int d = 0; d < deps.length; d++){
                        dps.add(deps[d]);
                    }
                    dependencies.put(x, dps);
                }catch(Exception ex){
                    System.out.println("no dependencies");
                }

            }
            builder = PVQ.builder;
            q = MultipleChoice(text, c, hint, context, builder, qns, ds, dependencies, parent);
        } else if (type.equals("M")){
            return null;
        } else if (type.equals("C")){
            return null;
        } else if (type.equals("S")){
            NodeList factors = eElement.getElementsByTagName("factor");
            q = SumQ(text,hint,context,factors, parent, qns);
        } else if (type.equals("P")){
            q = ParentQ(text, hint, context, parent, qns);
        }

        String inv;
        try{
            inv = eElement.getElementsByTagName("inv").item(0).getTextContent();
        }catch(Exception e){
            inv = "F";
        }

        if(inv.equals("T")){
            q.setVisibility(View.GONE);
        }

        if(layout1 != null){
            try {
                layout1.addView(q);
            }catch(IllegalStateException e){
                System.out.println("child question");
            }
        }
        list2.add(q);

        for(int o=0;o<extras.size();o++){
            // Adds question to group LinearLayout, overall list of questions,
            // and list of questions to be mapped to group name in hash map of questions to groups
            list2.add((LinearLayout)extras.get(o));
        }

        if(qns != null){
            String num = eElement.getElementsByTagName("q").item(0).getTextContent();
            qns.put(num, q);
            ds.put(num, 0);
        }

        return q;
    }

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

    }

    // Methods for each specific question type that make question linear layout for specific question type

    public static LinearLayout TextQ(String questiontext, String hint, Context context,
                                     String parent, HashMap qns){
        // sets up question text
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);
        // sets up box for answer text entry
        final EditText edittext = new EditText(context);
        edittext.setHint(hint);
        // sets up linear layout for question, adds question text and answer text box
        LinearLayout qlayout = new LinearLayout(context);
        qlayout.setOrientation(LinearLayout.VERTICAL);
        text.setTag("text");
        edittext.setTag("answer");
        qlayout.addView(text);
        qlayout.addView(edittext);
        qlayout.setTag("T");

        if(parent != null){
            LinearLayout pl = (LinearLayout)qns.get(parent);
            pl.addView(qlayout);
            TextView parent_text = new TextView(context);
            parent_text.setText(((TextView)pl.findViewWithTag("parent text")).getText());
            parent_text.setTag("parent text");
            parent_text.setVisibility(View.GONE);
            qlayout.addView(parent_text);
        }

        return qlayout;
    }

    public static LinearLayout NumQ(String questiontext, String hint, Context context,
                                    String parent, HashMap qns){
        // sets up question text
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);
        // sets up box for answer text entry (numerical)
        EditText edittext = new EditText(context);
        edittext.setHint(hint);
        edittext.setInputType(2);
        text.setTag("text");
        edittext.setTag("answer");
        // sets up linear layout for question, adds question text and answer text box
        LinearLayout qlayout = new LinearLayout(context);
        qlayout.setOrientation(LinearLayout.VERTICAL);
        qlayout.addView(text);
        qlayout.addView(edittext);
        qlayout.setTag("N");

        if(parent != null){
            LinearLayout pl = (LinearLayout)qns.get(parent);
            pl.addView(qlayout);
            TextView parent_text = new TextView(context);
            parent_text.setText(((TextView)pl.findViewWithTag("parent text")).getText());
            parent_text.setTag("parent text");
            parent_text.setVisibility(View.GONE);
            qlayout.addView(parent_text);
        }

        return qlayout;
    }

    //
    public static LinearLayout SumQ(String questiontext, final String hint,Context context,NodeList factors,
                                    String parent, HashMap qns){
        // store only factors and not total;
        // if total is a restrictive total should be set as restriction on further ones

        // sets up question text
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);
        text.setTag("text");
        // sets up linear layout for question, adds question text
        LinearLayout qlayout = new LinearLayout(context);
        qlayout.setOrientation(LinearLayout.VERTICAL);

        if(!hint.equals("")){
            Button bt = new Button(context);
            Drawable help = ContextCompat.getDrawable(context, R.drawable.help_circle_outline);
            bt.setBackground(help);
            bt.setLayoutParams(new LinearLayout.LayoutParams(50, 50));

            final AlertDialog.Builder bdr = builder;

            bt.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    bdr.setMessage(hint);
                    AlertDialog dialog = bdr.create();
                    dialog.show();
                }
            });

            LinearLayout qh = new LinearLayout(context);
            qh.setOrientation(LinearLayout.HORIZONTAL);
            qh.addView(text);
            qh.addView(bt);
            qlayout.addView(qh);
        }else{
            qlayout.addView(text);
        }

        // sets up boxes for answer text entry (numerical)
        TextView tv = new TextView(context);
        List to_sum = new ArrayList();
        for(int i = 0;i<factors.getLength();i++){
            Element factor = (Element) factors.item(i);
            System.out.println(factor.getTextContent());
            String ftext = factor.getElementsByTagName("ftext").item(0).getTextContent();
            EditText et = new EditText(context);
            et.setTextSize(15);
            et.setHint("            ");
            et.addTextChangedListener(new SumWatcher(tv,to_sum));
            et.setInputType(2);
            to_sum.add(et);
            LinearLayout hbar = new LinearLayout(context);
            hbar.setOrientation(LinearLayout.HORIZONTAL);
            TextView ft = new TextView(context);
            ft.setTextSize(15);
            ft.setText(ftext+ " ");

            hbar.addView(ft);
            hbar.addView(et);
            qlayout.addView(hbar);
        }
        qlayout.addView(tv);
        tv.setTag("answer");
        qlayout.setTag("S");

        if(parent != null){
            LinearLayout pl = (LinearLayout)qns.get(parent);
            pl.addView(qlayout);
            TextView parent_text = new TextView(context);
            parent_text.setText(((TextView)pl.findViewWithTag("parent text")).getText());
            parent_text.setTag("parent text");
            parent_text.setVisibility(View.GONE);
            qlayout.addView(parent_text);
        }

        return qlayout;
    }

    public static LinearLayout SingleChoice(String questiontext, List choices,
                                            final String hint, Context context, AlertDialog.Builder builder,
                                            HashMap qns, HashMap ds, HashMap lds, String parent){
        // sets up question text
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);

        // creates group of radio buttons, each button being a choice from choices
        RadioGroup rg = new RadioGroup(context);
        for (int i=0; i<choices.size(); i++) {
            RadioButton rb = new RadioButton(rg.getContext());
            String btext = choices.get(i).toString();
            rb.setId(i);
            rb.setText(btext);
            rg.addView(rb);
            ArrayList<String> options = (ArrayList<String>)lds.get(btext);
            if(options == null){
                options = new ArrayList<String>();
            }
            rb.setOnCheckedChangeListener(new onCheckedChangedB(qns, ds, options));
        }
        text.setTag("text");
        rg.setTag("choices");

        // sets up info button, builds dialog with instructions when clicked on
        Button bt = new Button(context);
        Drawable help = ContextCompat.getDrawable(context, R.drawable.help_circle_outline);
        bt.setBackground(help);
        bt.setLayoutParams(new LinearLayout.LayoutParams(50, 50));

        final AlertDialog.Builder bdr = builder;

        bt.setOnClickListener(new View.OnClickListener() {
                                  public void onClick(View v) {
                                      bdr.setMessage(hint);
                                      AlertDialog dialog = bdr.create();
                                      dialog.show();
                                  }
                              });

        // sets up question linear layout and adds all component views
        LinearLayout qlayout = new LinearLayout(context);
        qlayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout qh = new LinearLayout(context);
        qh.setOrientation(LinearLayout.HORIZONTAL);
        qh.addView(text);
        if(!hint.equals("")){
            qh.addView(bt);
        }
        qlayout.addView(qh);
        qlayout.addView(rg);
        bt.setTag("button");
        qlayout.setTag("SC");

        if(parent != null){
            LinearLayout pl = (LinearLayout)qns.get(parent);
            pl.addView(qlayout);
            TextView parent_text = new TextView(context);
            parent_text.setText(((TextView)pl.findViewWithTag("parent text")).getText());
            parent_text.setTag("parent text");
            parent_text.setVisibility(View.GONE);
            qlayout.addView(parent_text);
        }

        return qlayout;

    }

    public static LinearLayout MultipleChoice(String questiontext, List choices,
                                              final String hint, Context context, AlertDialog.Builder builder,
                                              HashMap qnums, HashMap ds, HashMap localds, String parent){
        // sets up question text
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);

        System.out.println(localds.keySet());

        // sets up question linear layout, adds question text
        LinearLayout qlayout = new LinearLayout(context);
        qlayout.setOrientation(LinearLayout.VERTICAL);
        //qlayout.addView(text);
        LinearLayout qh = new LinearLayout(context);
        qh.setOrientation(LinearLayout.HORIZONTAL);
        qlayout.addView(qh);


        // for each question choice, adds checkbox to question linear layout
        for (int i=0; i<choices.size(); i++) {
            CheckBox cb = new CheckBox(context);
            String ctext = choices.get(i).toString();
            cb.setId(i);
            cb.setText(ctext);
            cb.setTag("choice");
            cb.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            ArrayList<String> options = (ArrayList<String>)localds.get(ctext);
            if(options == null){
                options = new ArrayList<String>();
            }
            cb.setOnCheckedChangeListener(new onCheckedChangedB(qnums, ds, options));
            qlayout.addView(cb);
        }

        text.setTag("text");

        // sets up info button
        Button bt = new Button(context);
        Drawable help = ContextCompat.getDrawable(context, R.drawable.help_circle_outline);
        bt.setBackground(help);
        bt.setLayoutParams(new LinearLayout.LayoutParams(50, 50));

        final AlertDialog.Builder bdr = builder;

        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bdr.setMessage(hint);
                AlertDialog dialog = bdr.create();
                dialog.show();
            }
        });
        bt.setTag("button");

        //qlayout.addView(text);
        qh.addView(text);
        if(!hint.equals("")){
            //qlayout.addView(bt);
            qh.addView(bt);
        }
        qh.setTag("qh");

        qlayout.setTag("MC");

        if(parent != null){
            LinearLayout pl = (LinearLayout)qnums.get(parent);
            pl.addView(qlayout);
            TextView parent_text = new TextView(context);
            parent_text.setText(((TextView)pl.findViewWithTag("parent text")).getText());
            parent_text.setTag("parent text");
            parent_text.setVisibility(View.GONE);
            qlayout.addView(parent_text);
        }

        return qlayout;
    }

    public static LinearLayout ParentQ(String questiontext, final String hint, Context context,
                                       String parent, HashMap qns){
        LinearLayout qlayout = new LinearLayout(context);
        qlayout.setOrientation(LinearLayout.VERTICAL);

        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);
        text.setTag("text");

        qlayout.setTag("P");
        //qlayout.addView(text);

        TextView parent_text = new TextView(context);
        String pp = questiontext+": ";
        parent_text.setText(pp);
        parent_text.setTag("parent text");
        parent_text.setVisibility(View.GONE);
        qlayout.addView(parent_text);

        if(parent != null){
            LinearLayout pl = (LinearLayout)qns.get(parent);
            pl.addView(qlayout);
            System.out.println(questiontext);
            System.out.println("!!!!!!!!!!!!!!!!!!");
            parent_text.setText(((TextView)pl.findViewWithTag("parent text")).getText()+questiontext+": ");
            System.out.println(parent_text.getText());
        }

        if(!hint.equals("")) {
            // sets up info button, builds dialog with instructions when clicked on
            Button bt = new Button(context);
            Drawable help = ContextCompat.getDrawable(context, R.drawable.help_circle_outline);
            bt.setBackground(help);
            bt.setLayoutParams(new LinearLayout.LayoutParams(50, 50));

            final AlertDialog.Builder bdr = builder;

            bt.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    bdr.setMessage(hint);
                    AlertDialog dialog = bdr.create();
                    dialog.show();
                }
            });
            /*
            System.out.println("jhgjhgj");
            qlayout.removeAllViews();
            LinearLayout qh = new LinearLayout(context);
            qh.setOrientation(LinearLayout.HORIZONTAL);
            qh.addView(text);
            qh.addView(bt);
            System.out.println("here");
            System.out.println(text.getVisibility());
            System.out.println(bt.getVisibility());
            qlayout.addView(qh);
            qlayout.addView(parent_text);
            System.out.println(qlayout.getChildCount());
            */
            LinearLayout qh = new LinearLayout(context);
            qh.setOrientation(LinearLayout.HORIZONTAL);
            qh.addView(text);
            qh.addView(bt);
            qlayout.addView(qh);
        }else{
            qlayout.addView(text);
        }

        return qlayout;
    }

}

class SumWatcher implements TextWatcher{
    private TextView tv;
    private List factors;
    public SumWatcher(TextView tv, List factors){
        this.tv = tv;
        this.factors = factors;
    }

    public void afterTextChanged(Editable s) {
        int sum = 0;
        boolean zero = false;
        for(int i = 0;i<factors.size();i++){
            String value = ((EditText)factors.get(i)).getText().toString();
            if(value.equals("0")){
                zero = true;
            }
            try {
                int v = Integer.parseInt(value);
                sum += v;
            }catch(Exception e){
            }
        }
        System.out.println(zero);
        if(sum == 0 && !zero){
            tv.setText("Total: ");
            tv.setTextSize(17);
        } else{
            String total = Integer.toString(sum);
            tv.setText("Total: " + total);
            tv.setTextSize(17);
        }
    }
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
}

class onCheckedChangedB implements RadioButton.OnCheckedChangeListener{
    private HashMap questions;
    private HashMap dependents_map;
    private ArrayList<String> dependents;

    public onCheckedChangedB(HashMap qns, HashMap deps, ArrayList dependents){
        this.questions = qns;
        this.dependents_map = deps;
        this.dependents = dependents;
    }

    @Override
    public void onCheckedChanged(CompoundButton b, boolean isChecked){
        if(isChecked){
            for(String dependent: dependents){
                System.out.println(dependents_map.keySet());
                System.out.println(dependent);
                System.out.println(dependents_map.containsKey(dependent));
                int u = (int)dependents_map.get(dependent);
                dependents_map.put(dependent, u+1);
                ((LinearLayout)questions.get(dependent)).setVisibility(View.VISIBLE);
            }
        }else{
            for(String dependent: dependents){
                int u = (int)dependents_map.get(dependent);
                dependents_map.put(dependent, u-1);
                if(dependents_map.get(dependent).equals(0)) {
                    ((LinearLayout) questions.get(dependent)).setVisibility(View.GONE);
                }
            }
        }
    }
}

class onCheckedChanged implements RadioGroup.OnCheckedChangeListener{
    private LinearLayout ll;
    private HashMap questions;
    private Context context;
    private HashMap qnums;
    // can take a linear layout, nodelist, and context as parameters
    public onCheckedChanged(HashMap questions, LinearLayout ll, Context context, HashMap qnums) {
        this.questions = questions;
        this.context = context;
        this.ll = ll;
        this.qnums = qnums;
    }

    @Override
    public void onCheckedChanged(RadioGroup rg, int p)
    {
        for(int i=0;i<questions.size();i++){
            List qs = (List)questions.get(i);
            if(i==p){
                for(int h = 0; h<qs.size(); h++) {
                    String key = (String) qs.get(h);
                    ((LinearLayout) qnums.get(key)).setVisibility(View.VISIBLE);
                }
            }else{
                for(int h = 0; h<qs.size(); h++) {
                    String key = (String) qs.get(h);
                    ((LinearLayout) qnums.get(key)).setVisibility(View.GONE);
                }
            }
        }

    }
};



// readme
// bundling/deploying to phone
// backwards compatibility
