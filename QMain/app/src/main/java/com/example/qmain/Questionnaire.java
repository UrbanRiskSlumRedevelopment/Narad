package com.example.qmain;

import android.content.Context;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Space;

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

import android.text.InputFilter;
import android.text.Spanned;

import java.lang.reflect.Array;


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
        String hint;
        String parent;
        TextView rqt = null;
        String req;
        String qid = eElement.getElementsByTagName("q").item(0).getTextContent();
        try{
            req = eElement.getElementsByTagName("req").item(0).getTextContent();
        }catch(Exception e){
            req = "F";
        }

        try{
            hint = eElement.getElementsByTagName("qhint").item(0).getTextContent();
        }catch(Exception e){
            hint = "";
        }

        if(req.equals("T")){
            text = eElement.getElementsByTagName("qtext").item(0).getTextContent()+"*";
        }
        else if(req.equals("C")){
            text = eElement.getElementsByTagName("qtext").item(0).getTextContent();
            String req_text = eElement.getElementsByTagName("qtext").item(0).getTextContent()+"*";
            rqt = new TextView(context);
            rqt.setText(req_text);
            rqt.setVisibility(View.GONE);
        }else{
            text = eElement.getElementsByTagName("qtext").item(0).getTextContent();
        }
        try{
            parent = eElement.getElementsByTagName("parent").item(0).getTextContent();
        }catch(Exception e){
            parent = null;
        }
        String qlimit;
        try{
            qlimit = eElement.getElementsByTagName("qlimit").item(0).getTextContent();
        }catch(Exception e){
            qlimit = "";
        }
        List extras = new ArrayList();
        if (type.equals("T")) {
            q = TextQ(text, hint, context, parent, qns);
        } else if (type.equals("N")) {
            q = NumQ(text, hint, context, parent, qns, qlimit);
        } else if (type.equals("SC")) {
            List c = new ArrayList();
            NodeList choices = eElement.getElementsByTagName("choice");
            HashMap<String, ArrayList> dependencies = new HashMap<>();
            for (int i = 0; i < choices.getLength(); i++) {
                Node choice = choices.item(i);
                Element e = (Element) choice;
                String x = e.getElementsByTagName("ctext").item(0).getTextContent();
                String tag = e.getElementsByTagName("ccode").item(0).getTextContent();
                x = x+"~~"+tag;
                c.add(x);
                try {
                    ArrayList dps = new ArrayList();
                    NodeList dependents = e.getElementsByTagName("dependents");

                    for(int j = 0; j<dependents.getLength(); j++){
                        String dep_string = dependents.item(j).getTextContent();
                        dps.add(dep_string);
                    }
                    dependencies.put(x, dps);
                }catch(Exception ex){
                    //System.out.println("no dependencies");
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
                String code = e.getElementsByTagName("ccode").item(0).getTextContent();
                x = x + "~~"+code;
                c.add(x);
                try {
                    ArrayList dps = new ArrayList();
                    NodeList dependents = e.getElementsByTagName("dependents");

                    for(int j = 0; j<dependents.getLength(); j++){
                        String dep_string = dependents.item(j).getTextContent();
                        dps.add(dep_string);
                    }
                    dependencies.put(x, dps);
                }catch(Exception ex){
                    //System.out.println("no dependencies");
                }
                /*
                try{
                    ArrayList dps = new ArrayList();
                    String dependents = e.getElementsByTagName("dependents").item(0).getTextContent();
                    dependents.replace(" ","");
                    String[] deps = dependents.split(",");

                    for(int d = 0; d < deps.length; d++){
                        dps.add(deps[d]);
                    }

                    dependencies.put(x, dps);
                }catch(Exception exc){
                    //System.out.println("no dependencies");
                }
                */

            }
            builder = PVQ.builder;
            q = MultipleChoice(text, c, hint, context, builder, qns, ds, dependencies, parent);
        } else if (type.equals("M")){
            return null;
        } else if (type.equals("C")){
            return null;
        } else if (type.equals("S")){
            NodeList factors = eElement.getElementsByTagName("factor");
            q = SumQ(text,hint,context,factors, parent, qns, qlimit);
        } else if (type.equals("P")){
            q = ParentQ(text, hint, context, parent, qns);
        }

        if(rqt != null){
            q.addView(rqt);
            rqt.setTag("required");
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
                //System.out.println("child question");
            }
        }
        list2.add(q);

        for(int o=0;o<extras.size();o++){
            // Adds question to group LinearLayout, overall list of questions,
            // and list of questions to be mapped to group name in hash map of questions to groups
            list2.add((LinearLayout)extras.get(o));
        }

        if(qns != null){
            String num = qid;
            qns.put(num, q);
            ds.put(num, 0);
        }

        TextView qt = new TextView(context);
        qt.setText(qid);
        qt.setVisibility(View.GONE);
        qt.setTag("qid");
        q.addView(qt);


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
        text.setPadding(0,0,0,5);
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
                                    String parent, HashMap qns, String limit){
        // sets up question text
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);
        text.setPadding(0,0,0,5);

        // sets up box for answer text entry (numerical)
        EditText edittext = new EditText(context);
        edittext.setHint(hint);
        edittext.setInputType(2);
        text.setTag("text");
        edittext.setTag("answer");

        if(!limit.equals("")) {
            edittext.addTextChangedListener(new NumQWatcher(edittext, limit, qns, context));
        }

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
                                    String parent, HashMap qns, String qlimit){
        // store only factors and not total;
        // if total is a restrictive total should be set as restriction on further ones

        // sets up question text
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);
        text.setTag("text");
        text.setPadding(0,0,0,5);
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
            //System.out.println(factor.getTextContent());
            String ftext = factor.getElementsByTagName("ftext").item(0).getTextContent();
            EditText et = new EditText(context);
            et.setTextSize(15);
            et.setHint("            ");
            if(qlimit.equals("")){
                et.addTextChangedListener(new SumWatcher(tv, to_sum, "", qns, null, context));
            }else {
                et.addTextChangedListener(new SumWatcher(tv, to_sum, qlimit, qns, ftext+" ", context));
            }
            et.setInputType(2);
            to_sum.add(et);
            LinearLayout hbar = new LinearLayout(context);
            hbar.setOrientation(LinearLayout.HORIZONTAL);
            TextView ft = new TextView(context);
            ft.setTextSize(15);
            ft.setText(ftext+ " ");
            ft.setTag("ftext");
            et.setTag("fanswer");

            hbar.addView(ft);
            hbar.addView(et);
            hbar.setTag("factor");
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
            rb.setText(btext.substring(0, btext.indexOf("~~")));
            rg.addView(rb);
            rb.setTag(btext.substring(btext.indexOf("~~")+2));
            ArrayList<String> options = (ArrayList<String>)lds.get(btext);
            if(options == null){
                options = new ArrayList<String>();
            }
            rb.setOnCheckedChangeListener(new onCheckedChangedB(qns, ds, options));
        }
        text.setTag("text");
        text.setPadding(0,0,0,5);
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
        text.setPadding(0,0,0,5);

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
            cb.setText(ctext.substring(0, ctext.indexOf("~~")));
            String tag = "choice" + ctext.substring(ctext.indexOf("~~"));
            cb.setTag(tag);
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
        text.setPadding(0,0,0,5);

        qlayout.setTag("P");
        //qlayout.addView(text);

        TextView parent_text = new TextView(context);
        String pp = questiontext+" - ";
        parent_text.setText(pp);
        parent_text.setTag("parent text");
        parent_text.setVisibility(View.GONE);
        qlayout.addView(parent_text);

        if(parent != null){
            LinearLayout pl = (LinearLayout)qns.get(parent);
            pl.addView(qlayout);
            parent_text.setText(((TextView)pl.findViewWithTag("parent text")).getText()+questiontext+" - ");
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

    public static int NumLimit(String qnum, HashMap<String, LinearLayout> numqs, String factor){
        qnum = qnum.replace(" ","");
        if(qnum.contains(",")){
            String[] qnums = qnum.split(",");
            int total = 0;
            for(String qn : qnums){
                try{
                    LinearLayout qll = numqs.get(qn);
                    total += NumAnswer(qll, factor);
                }catch(Exception e){}
            }
            return total;
        }else{
            LinearLayout qll = numqs.get(qnum);
            return NumAnswer(qll, factor);
        }
    }

    public static int NumAnswer(LinearLayout qll, String factor){
        if(factor!=null){
            System.out.println(factor);
            System.out.println("in num answer");
            for(int v = 0; v<qll.getChildCount(); v++){
                System.out.println("looping");
                if(qll.getChildAt(v).getTag().equals("factor")){
                    System.out.println("found a factor");
                    try{
                        TextView ftext = (TextView) qll.getChildAt(v).findViewWithTag("ftext");
                        if(ftext.getText().toString().equals(factor)){
                            EditText et = (EditText) qll.getChildAt(v).findViewWithTag("fanswer");
                            return Integer.parseInt(et.getText().toString());
                        }else{
                            System.out.println(ftext.getText().toString());
                        }
                    }catch(Exception e1){
                        System.out.println("error caught");
                        e1.printStackTrace();
                    }
                }else{
                    System.out.println(qll.getChildAt(v).getTag());
                }
            }
        }
        try{
            TextView answ = (TextView) qll.findViewWithTag("answer");
            String s = answ.getText().toString();
            s = s.substring(s.indexOf(" ")+1);
            if(s.equals("")){
                return 0;
            }
            s = s.replace(" ","");
            return Integer.parseInt(s);
        }catch(Exception e){
            e.printStackTrace();
            EditText answ = (EditText) qll.findViewWithTag("answer");
            String s = answ.getText().toString();
            s = s.replace(" ","");
            if(s.equals("")){
                return 0;
            }
            return Integer.parseInt(s);
        }
    }

}

class SumWatcher implements TextWatcher{
    private TextView tv;
    private List factors;
    private String qlim;
    private HashMap<String, LinearLayout> qns;
    private String f;
    private AlertDialog dialog;
    public SumWatcher(TextView tv, List factors, String qlimit, HashMap<String, LinearLayout> qns, String factor, Context context){
        this.tv = tv;
        this.factors = factors;
        this.qlim = qlimit;
        this.qns = qns;
        this.f = factor;
        AlertDialog.Builder newbuilder = new AlertDialog.Builder(context);
        String msg = "Value too large, contradicts answer to question "+qlimit;
        newbuilder.setMessage(msg);
        this.dialog = newbuilder.create();
    }

    public void afterTextChanged(Editable s) {
        int flim = -1;
        int tlim = -1;
        if(!qlim.equals("") && f!=null) {
            flim = Questionnaire.NumLimit(qlim, qns, f);
        }
        if(!qlim.equals("")){
            tlim = Questionnaire.NumLimit(qlim, qns, null);
        }
        int sval;
        if(s.toString().equals("")){
            sval = 0;
        }else {
            sval = Integer.parseInt(s.toString());
        }

        System.out.println(f);
        System.out.println(sval);
        System.out.println(flim);
        System.out.println(tlim);

        if(sval > flim && flim >= 0){
            dialog.show();
            return;
        }

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
                if(sum > tlim && tlim > 0){
                    ((EditText)factors.get(i)).setText("");
                    dialog.show();
                    break;
                }
            }catch(Exception e){
            }
        }
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
                dependent = dependent.replace(" ","");
                int u = (int)dependents_map.get(dependent);
                dependents_map.put(dependent, u+1);
                ((LinearLayout)questions.get(dependent)).setVisibility(View.VISIBLE);
                LinearLayout q = ((LinearLayout)questions.get(dependent));
                try{
                    TextView rtv = (TextView)q.findViewWithTag("required");
                    TextView tv = (TextView) q.findViewWithTag("text");
                    String sw = tv.getText().toString();
                    tv.setText(rtv.getText());
                    rtv.setText(sw);
                }catch(Exception e){
                    System.out.println(e.getStackTrace());
                }
            }
        }else{
            for(String dependent: dependents){
                int u = (int)dependents_map.get(dependent);
                dependents_map.put(dependent, u-1);
                if(dependents_map.get(dependent).equals(0)) {
                    LinearLayout q = ((LinearLayout)questions.get(dependent));
                    q.setVisibility(View.GONE);
                    try{
                        TextView rtv = (TextView)q.findViewWithTag("required");
                        TextView tv = (TextView) q.findViewWithTag("text");
                        String sw = tv.getText().toString();
                        tv.setText(rtv.getText());
                        rtv.setText(sw);
                    }catch(Exception e){
                        System.out.println(e.getStackTrace());
                    }
                }
            }
        }
    }
}


class NumQWatcher implements TextWatcher {
    private String q;
    private EditText et;
    private HashMap qns;
    private AlertDialog dialog;
    NumQWatcher(EditText et, String l, HashMap qns, Context context){
        this.et = et;
        this.q = l;
        this.qns = qns;
        AlertDialog.Builder newbuilder = new AlertDialog.Builder(context);
        String msg = "Value too large, contradicts answer to question "+l;
        newbuilder.setMessage(msg);
        this.dialog = newbuilder.create();
    }

    public void afterTextChanged(Editable s) {
        this.setNewLimit(et, q, qns, s);
    }
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
    private void setNewLimit(EditText et, String q, HashMap qns, Editable s){
        int lim = Questionnaire.NumLimit(q, qns, null);
        int val = 0;
        if(s.toString().equals("")){
            return;
        }
        try{
            val = Integer.parseInt(s.toString());
            System.out.println("successfully parsed");
            if(val > lim){
                dialog.show();
                et.setText("");
            }
        }catch(Exception e){
            et.setText("");
        }
    }
}


// reopenable
// apk
// commenting

// bundling/deploying to phone
