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
import android.graphics.Color;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.graphics.drawable.ScaleDrawable;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.app.ActivityManager;

import android.content.SharedPreferences;

import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import android.content.res.Resources;

import com.google.android.gms.games.quest.Quest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import android.support.v7.app.ActionBar.LayoutParams;


public class Questionnaire extends AppCompatActivity {
    public final static String DATA = "com.example.qmain.PREFERENCE_FILE_KEY";
    LinearLayout layout = null;
    List Questions = null;
    static List Questions2 = null;
    static List Counter = new ArrayList();
    public static AlertDialog.Builder builder = null;
    public Context context = this;
    public static String LOCATION = "";
    HashMap<String, Button> RepeatButtons = new HashMap();
    static ImageView mImageView = null;

    public static LinearLayout build_question(Node nNode, List list1, List list2, LinearLayout layout1, Context context){
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
            List extras = new ArrayList();
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
                    String x = e.getElementsByTagName("ctext").item(0).getTextContent();
                    c.add(x);
                }
                builder = PVQ.builder;
                q = SingleChoice(text, c, hint, context, builder);
            } else if (type.equals("MC")) {
                List c = new ArrayList();
                NodeList choices = eElement.getElementsByTagName("choice");
                for (int i = 0; i < choices.getLength(); i++) {
                    Node choice = choices.item(i);
                    Element e = (Element) choice;
                    String x = e.getElementsByTagName("ctext").item(0).getTextContent();
                    c.add(x);
                }
                builder = PVQ.builder;
                q = MultipleChoice(text, c, hint, context, builder);
            } else if (type.equals("M")){
                return null;
            } else if (type.equals("C")){
                return null;
            } else if (type.equals("S")){
                NodeList factors = eElement.getElementsByTagName("factor");
                q = SumQ(text,hint,context,factors);
            }
            else if (type.equals("LC")){
                List c = new ArrayList(); // initializes then fills list of choices
                NodeList choices = eElement.getElementsByTagName("choice");
                Node choice = eElement.getElementsByTagName("choice").item(0);
                //for (int i = 0; i < choices.getLength(); i++) {
                    //Node choice = choices.item(i);
                while(choice != null){
                    Element e;
                    try{
                        e = (Element) choice;
                    }catch(Exception exception){
                        choice = choice.getNextSibling();
                        if(choice==null){
                            break;
                        }
                        continue;
                    }

                    //Element e = (Element) choice;
                    String x = e.getElementsByTagName("ctext").item(0).getTextContent();
                    String extra = e.getElementsByTagName("extra").item(0).getTextContent();
                    List extra_questions = new ArrayList();
                    if(extra.equals("T")){
                        // loop through list if multiple questions
                        NodeList questions = eElement.getElementsByTagName("equestion");
                        for(int k=0; k<questions.getLength();k++){
                            Node question = questions.item(k);
                            extra_questions.add(question);
                        }

                    }

                    List stuff = new ArrayList();
                    stuff.add(x);
                    stuff.add(extra);
                    stuff.add(extra_questions);
                    c.add(stuff);
                    choice = choice.getNextSibling();
                    if(choice.equals(null)){
                        break;
                    }
                }

                List q_and_eqs = LinkedQuestion(text, c, hint, context, builder);
                q = (LinearLayout) q_and_eqs.get(0);
                for(int o = 0;o<q_and_eqs.size();o++){
                    if(o>0){
                        extras.add(q_and_eqs.get(o));
                    }
                }

            }
            if(layout1 != null){
                layout1.addView(q);
            }
            list1.add(q);
            list2.add(q);

            for(int o=0;o<extras.size();o++){
                // Adds question to group LinearLayout, overall list of questions,
                // and list of questions to be mapped to group name in hash map of questions to groups
                list1.add((LinearLayout)extras.get(o));
                list2.add((LinearLayout)extras.get(o));
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

        SharedPreferences sharedPref = context.getSharedPreferences(
                DATA, Context.MODE_PRIVATE);

        AlertDialog.Builder newbuilder = new AlertDialog.Builder(this);
        builder = newbuilder;

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
                NodeList nList = eE.getElementsByTagName("question");
                final ArrayList xx = new ArrayList();
                for(int i = 0; i<100; i++){
                    xx.add(i);
                }
                if(eE.getElementsByTagName("repeatable").item(0).getTextContent().equals("T")){
                    final ViewGroup chunk = new LinearLayout(context);
                    /*
                    chunk.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    chunk.setBackgroundColor(Color.BLUE);
                    */
                    Button bt = new Button(this);
                    final String name = ((Element) gr).getElementsByTagName("gtext").item(0).getTextContent();
                    String textset = "Add new " + name;
                    bt.setText(textset);
                    bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.WRAP_CONTENT));


                    bt.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            Intent intent = new Intent(context, Repeat.class);
                            Bundle bundle = new Bundle();
                            try {
                                bundle.putString("group name", name);
                            } catch(Exception e){
                                System.out.println(name);
                            }
                            intent.putExtras(bundle);
                            hideSoftKeyboard(a);
                            startActivity(intent);
                            Counter.add(1);

                            Button b = new Button(context);
                            String bname = name + Integer.toString(Counter.size());
                            b.setText(bname);
                            System.out.println(bname);
                            /*
                            b.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                                    ActionBar.LayoutParams.WRAP_CONTENT));
                            */
                            LinearLayout.LayoutParams params =  new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            int i = layout.indexOfChild(RepeatButtons.get(name));


                            b.setOnClickListener(new View.OnClickListener(){
                                public void onClick(View v){
                                    String display = Repeat.ANSWERS;
                                    System.out.println(display);
                                    Intent in = new Intent(context, Display.class);
                                    Bundle bun = new Bundle();
                                    bun.putString("answers", display);
                                    in.putExtras(bun);
                                    startActivity(in);
                                }
                            });
                            System.out.println("here");
                            RepeatButtons.put(name, b);
                            System.out.println("and here");
                            layout.addView(b, i+1, params);



                        }
                    }
                    );
                    /*
                    final TextView tv = new TextView(this);
                    tv.setText("chunk answers");
                    tv.setTextSize(20);
                    tv.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.WRAP_CONTENT));
                            */
                    RepeatButtons.put(name, bt);
                    //chunk.addView(bt);
                    //chunk.addView(tv);
                    layout.addView(bt);

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
                                    String x = e.getElementsByTagName("ctext").item(0).getTextContent();
                                    c.add(x);
                                }
                                q = SingleChoice(text, c, hint, context, builder);
                            } else if (type.equals("MC")) {
                                List c = new ArrayList();
                                NodeList choices = eElement.getElementsByTagName("choice");
                                for (int i = 0; i < choices.getLength(); i++) {
                                    Node choice = choices.item(i);
                                    Element e = (Element) choice;
                                    String x = e.getElementsByTagName("ctext").item(0).getTextContent();
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
            String submit = "Submit";
            bt.setText(submit);
            bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.WRAP_CONTENT));

            bt.setOnClickListener(new View.OnClickListener() {
                                      public void onClick(View v) {
                                          Button b = (Button) v;
                                          hideSoftKeyboard(a);
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


    // Methods for each specific question type that make question linear layout for specific question type

    public static LinearLayout TextQ(String questiontext, String hint, Context context){
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
        return qlayout;
    }

    public static LinearLayout NumQ(String questiontext, String hint, Context context){
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
        return qlayout;
    }

    public static LinearLayout SumQ(String questiontext,String hint,Context context,NodeList factors){
        // sets up question text
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);
        text.setTag("text");
        // sets up linear layout for question, adds question text
        LinearLayout qlayout = new LinearLayout(context);
        qlayout.setOrientation(LinearLayout.VERTICAL);
        qlayout.addView(text);
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
        return qlayout;
    }

    public static List LinkedQuestion(String questiontext, List choices, final String hint,
                                       Context context, AlertDialog.Builder builder){
        LinearLayout qlayout = new LinearLayout(context);
        // sets up question text
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);
        // creates group of radio buttons, each button being a choice from choices
        RadioGroup rg = new RadioGroup(context);
        HashMap extras = new HashMap();
        List questions = new ArrayList();
        for (int i=0; i<choices.size(); i++) {
            RadioButton rb = new RadioButton(rg.getContext());
            List choice = (List) choices.get(i);
            String btext = choice.get(0).toString();
            rb.setId(i);
            rb.setText(btext);
            extras.put(i, new ArrayList());
            if(choice.get(1).equals("T")){
                for(int h=0; h<((List)choice.get(2)).size(); h++){
                    Node question = (Node)((List)choice.get(2)).get(h);
                    List filler1 = new ArrayList();
                    List filler2 = new ArrayList();
                    LinearLayout filler3 = null;
                    LinearLayout q = build_question(question, filler1, filler2, filler3, context);
                    ((List)extras.get(i)).add(q);
                    questions.add(q);

                }

            }


            rg.addView(rb);
            System.out.println(rb.getId());
        }

        rg.setOnCheckedChangeListener(new onCheckedChanged(extras, qlayout, context));
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

        qlayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout qh = new LinearLayout(context);
        qh.setOrientation(LinearLayout.HORIZONTAL);
        //qlayout.addView(text);
        qh.addView(text);
        if(!hint.equals("")){
            //qlayout.addView(bt);
            qh.addView(bt);
        }
        qlayout.addView(qh);
        qlayout.addView(rg);
        bt.setTag("button");
        qlayout.setTag("SC");
        ArrayList views = new ArrayList();
        views.add(qlayout);

        System.out.println(extras);

        for(int t = 0;t<questions.size();t++){
            System.out.println(t);
            View v = (View) questions.get(t);
            v.setVisibility(View.GONE);
            views.add(v);
            qlayout.addView(v);
        }

        return views;
    }


    public static LinearLayout SingleChoice(String questiontext, List choices,
                                            final String hint, Context context, AlertDialog.Builder builder){
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
            System.out.println(rb.getId());
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
        //qlayout.addView(text);
        qh.addView(text);
        if(!hint.equals("")){
            //qlayout.addView(bt);
            qh.addView(bt);
        }
        qlayout.addView(qh);
        qlayout.addView(rg);
        bt.setTag("button");
        qlayout.setTag("SC");

        return qlayout;

    }

    int PLACE_PICKER_REQUEST = 1;

    public LinearLayout Map(String questiontext, Context context){
        LinearLayout qlayout = new LinearLayout(context);
        TextView tv = new TextView(context);
        tv.setTag("text");
        tv.setText(questiontext);
        Button bt = new Button(context);
        bt.setText(questiontext);
        bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT));
        final Activity a = this;

        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideSoftKeyboard(a);
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
            if (resultCode == RESULT_OK)  {
                Place place = PlacePicker.getPlace(this, data);
                String toastMsg = String.format("Place: %s", place.getLatLng());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                System.out.println(toastMsg);
                System.out.println(PlacePicker.getLatLngBounds(data));
                LOCATION = toastMsg.substring(7);
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView = new ImageView(this);
            mImageView.setImageBitmap(imageBitmap);
            layout.addView(mImageView, 3);
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

    static final int REQUEST_IMAGE_CAPTURE = 2;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }



    public static LinearLayout MultipleChoice(String questiontext, List choices,
                                              final String hint, Context context, AlertDialog.Builder builder){
        // sets up question text
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);

        // sets up question linear layout, adds question text
        LinearLayout qlayout = new LinearLayout(context);
        qlayout.setOrientation(LinearLayout.VERTICAL);
        qlayout.addView(text);

        // for each question choice, adds checkbox to question linear layout
        for (int i=0; i<choices.size(); i++) {
            CheckBox cb = new CheckBox(context);
            String ctext = choices.get(i).toString();
            cb.setId(i);
            cb.setText(ctext);
            cb.setTag("choice");
            cb.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            qlayout.addView(cb);
        }

        text.setTag("text");

        // sets up info button
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

        if(!hint.equals("")){
            qlayout.addView(bt);
        }
        bt.setTag("button");
        qlayout.setTag("MC");
        return qlayout;
    }

    public String submit(){
        hideSoftKeyboard(a);
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.US).format(new Date());
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

        String answers = writeAnswers(Questions, true, fos, false);
        if (answers.equals("")){
            AlertDialog.Builder bdr = builder;
            bdr.setMessage("Answer all required questions before submitting");
            AlertDialog dialog = bdr.create();
            dialog.show();
            return "";
        }
        try {
            fos.close();
        }catch(Exception e){}

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        Counter = new ArrayList();

        return filename;

        //hexcode, title from question field, device id, account

    }

    public static String writeAnswers(List qs, boolean toFile, FileOutputStream f, boolean incomplete) {
        String total = "";
        for (int i = 0; i < qs.size(); i++) {
            LinearLayout q = (LinearLayout) qs.get(i);
            TextView text = (TextView) q.findViewWithTag("text");
            String question = (String) text.getText();
            String line = "";
            String tag = "";
            try {
                tag = (String) q.getTag();
            } catch (Exception e) {
                System.out.println("there's no tag?");
            }

            System.out.println(tag);
            if (tag.equals("T") || tag.equals("N")) {
                EditText editText = (EditText) q.findViewWithTag("answer");
                if (editText.getText().toString().equals("") && question.endsWith("*") && !incomplete) {
                    System.out.println("oops");
                    return "";
                } else {
                    if(editText.getText().toString().equals("") && question.endsWith("*")){
                        // set color of question to red
                        question = question.toUpperCase();
                    }
                    line = question + ": " + editText.getText();
                    System.out.println(line);
                }
            } else if (tag.equals("SC")) {
                line = question + ": ";
                RadioGroup rg = (RadioGroup) q.findViewWithTag("choices");
                int id = rg.getCheckedRadioButtonId();
                if (id == -1 && !incomplete) {
                    return "";
                } else {
                    RadioButton rb = (RadioButton) rg.getChildAt(id);
                    System.out.println(rg.getChildAt(id));
                    System.out.println(rg.getChildCount());
                    System.out.println(id);
                    try {
                        if(id == -1){
                            // set color of line to red
                            line = line.toUpperCase();
                        }
                        line += rb.getText();
                        System.out.println(line);
                    } catch (Exception e) {
                        System.out.println("here");
                    }
                }

            } else if (tag.equals("MC")) {
                line = question + ": ";
                System.out.println(q.getChildCount());
                for (int j = 0; j < q.getChildCount(); j++) {
                    System.out.println(q.getChildAt(j).getTag());
                    String ctag = (String) q.getChildAt(j).getTag();
                    if (ctag.equals("choice")) {
                        CheckBox cb = (CheckBox) q.getChildAt(j);
                        if (cb.isChecked()) {
                            line += cb.getText() + ", ";
                        }
                    }
                }
                if (line.length() > 20) {
                    line = line.substring(0, line.length() - 2);
                }
                System.out.println(line);
            } else if (tag.equals("M")) {
                line = question + ": " + LOCATION;
            } else if (tag.equals("C")) {
                line = question + ": "; //+ mImageView.toString();
            }
            if (toFile) {
                try {
                    line = line + "~~";
                    total += line;
                    f.write(line.getBytes());
                } catch (Exception e) {}
            } else {
                line = line + "\n";
                total += line;
            }
        }
        return total;
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
        for(int i = 0;i<factors.size();i++){
            String value = ((EditText)factors.get(i)).getText().toString();
            try {
                int v = Integer.parseInt(value);
                sum += v;
            }catch(Exception e){
            }
        }
        if(sum == 0){
            tv.setText("Total: 0");
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

class onCheckedChanged implements RadioGroup.OnCheckedChangeListener{
    private LinearLayout ll;
    private HashMap questions;
    private Context context;
    // can take a linear layout, nodelist, and context as parameters
    public onCheckedChanged(HashMap questions, LinearLayout ll, Context context) {
        this.questions = questions;
        this.context = context;
        this.ll = ll;
    }

    @Override
    public void onCheckedChanged(RadioGroup rg, int p)
    {
        for(int i=0; i<questions.size();i++){
            List qs = (List)questions.get(i);
            System.out.println(qs);
            if(i==p){
                for(int j = 0; j<qs.size(); j++){
                    System.out.println(((LinearLayout)qs.get(j)).getChildCount());
                    ((LinearLayout)qs.get(j)).setVisibility(View.VISIBLE);
                }
            }else{
                for(int j = 0; j<qs.size(); j++){
                    LinearLayout l = (LinearLayout)qs.get(j);
                    (l).setVisibility(View.GONE);
                    for(int c = 0; c<l.getChildCount();c++){
                        if(l.getChildAt(c) instanceof EditText){
                            ((EditText)l.getChildAt(c)).setText("");
                        } else if(l.getChildAt(c) instanceof RadioGroup){
                            ((RadioGroup) l.getChildAt(c)).clearCheck();
                        } else if(l.getChildAt(c) instanceof CheckBox){
                            ((CheckBox)l.getChildAt(c)).setChecked(false);
                        }
                    }
                }
            }
        }


    }
};



// readme
// bundling/deploying to phone
// backwards compatibility
