package com.example.qmain;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.support.v4.content.FileProvider;
import android.net.Uri;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.FileOutputStream;
import java.io.File;
import android.os.Environment;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import android.support.v4.view.PagerAdapter;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

public class PVQ extends AppCompatActivity {
    public final static String DATA = "com.example.qmain.PREFERENCE_FILE_KEY"; // Data from completed questionnaire to be stored
    LinearLayout layout = null;
    List Questions = new ArrayList(); // List of all questions
    HashMap<String,List> Groups = new HashMap<>(); // Hash map mapping questions to groups
    String answers = "";
    TextView ans = null;
    static List Counter = new ArrayList();
    public static AlertDialog.Builder builder = null; // For building alert dialogs when necessary
    public Context context = this; // For accessing context of the questionnaire
    public static String LOCATION = ""; // Location stored here
    ImageView mImageView = null;
    LinearLayout camera_question = null;
    LinearLayout map_question = null;
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pvq);

        // Create and set up new questionnaire ViewPager
        final ViewPager vp = (ViewPager) findViewById(R.id.activity_pvq);
        vp.setId(View.generateViewId());
        setupUI(vp);

        MainPagerAdapter pg = new MainPagerAdapter();
        vp.setAdapter(pg);

        // Sets up an alert dialog builder for use when necessary
        builder = new AlertDialog.Builder(this);

        // Builds Questionnaire
        try{
            // Parses XML doc so code can read it
            InputStream in = getResources().openRawResource(R.raw.questions);
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(in);
            doc.getDocumentElement().normalize();

            // Builds first page
            // Each page consists of a scrollview containing a linear layout containing smaller linear layouts
            ScrollView sv1 = new ScrollView(this);
            LinearLayout p1 = new LinearLayout(this);
            p1.setOrientation(LinearLayout.VERTICAL);

            // Adds first page to ViewPager
            pg.addView(sv1,0);
            sv1.addView(p1);
            setupUI(sv1);
            setupUI(p1);

            // Adds "Sections" title to first page
            TextView title = new TextView(this);
            title.setTextSize(30);
            String sections = "Sections";
            title.setText(sections);
            p1.addView(title);


            // iterates through all groups of questions in XML doc
            NodeList groups = doc.getElementsByTagName("group");
            for(int g = 0; g<groups.getLength(); g++){
                System.out.println(g);
                Node gr = groups.item(g);
                Element eE = (Element) gr;
                final int g_button = g; // number of group, counting from 0
                String g_name = ((Element) gr).getElementsByTagName("gtext").item(0).getTextContent(); // group name

                // creates button on menu page linked to group page
                Button p1_button = new Button(this);
                p1.addView(p1_button);
                p1_button.setText(g_name);
                p1_button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        vp.setCurrentItem(g_button+1, false);
                    }
                });

                // creates group page
                ScrollView sv = new ScrollView(this);
                LinearLayout ll = new LinearLayout(this);
                ll.setOrientation(LinearLayout.VERTICAL);
                sv.addView(ll);
                pg.addView(sv,g+1);
                setupUI(sv);

                // adds name to group page
                TextView group_name = new TextView(this);
                group_name.setText(g_name);
                group_name.setTextSize(30);
                ll.addView(group_name);

                // iterates through all questions in group
                NodeList nList = eE.getElementsByTagName("question");
                List Qs = new ArrayList();
                System.out.println(nList.getLength());
                for(int j=0; j<nList.getLength();j++){
                    Node nNode = nList.item(j);
                    // makes sure question is a valid node
                    if (nNode.getNodeType() == Node.ELEMENT_NODE){
                        // Initializes question LinearLayout
                        LinearLayout q = null;
                        Element eElement = (Element) nNode;
                        // Obtains question type, hint, and text; adds * to question text if required
                        String text;
                        String type = eElement.getElementsByTagName("qtype").item(0).getTextContent();
                        String hint = eElement.getElementsByTagName("qhint").item(0).getTextContent();
                        if(eElement.getElementsByTagName("req").item(0).getTextContent().equals("T")){
                            text = eElement.getElementsByTagName("qtext").item(0).getTextContent()+"*";
                        }
                        else{
                            text = eElement.getElementsByTagName("qtext").item(0).getTextContent();
                        }
                        System.out.println("ok");

                        // Creates question LinearLayout by calling the appropriate method for the type of the question
                        if (type.equals("T")) {
                            // Text question
                            q = Questionnaire.TextQ(text, hint, context);
                        } else if (type.equals("N")) {
                            // Numeric question
                            q = Questionnaire.NumQ(text, hint, context);
                        } else if (type.equals("SC")) {
                            // Single Choice question
                            List c = new ArrayList(); // initializes then fills list of choices
                            NodeList choices = eElement.getElementsByTagName("choice");
                            for (int i = 0; i < choices.getLength(); i++) {
                                Node choice = choices.item(i);
                                Element e = (Element) choice;
                                String x = e.getElementsByTagName("ctext").item(0).getTextContent();
                                c.add(x);
                            }
                            q = Questionnaire.SingleChoice(text, c, hint, context, builder);
                        } else if (type.equals("MC")) {
                            // Multiple Choice question
                            List c = new ArrayList(); // initializes then fills list of choices
                            NodeList choices = eElement.getElementsByTagName("choice");
                            for (int i = 0; i < choices.getLength(); i++) {
                                Node choice = choices.item(i);
                                Element e = (Element) choice;
                                String x = e.getElementsByTagName("ctext").item(0).getTextContent();
                                c.add(x);
                            }
                            q = Questionnaire.MultipleChoice(text, c, hint, context, builder);
                        } else if (type.equals("M")){
                            // Map question
                            q = Map(text, context);
                            map_question = q;
                        } else if (type.equals("C")){
                            // Camera question; still being worked out
                            q = Camera(text, context);
                            camera_question = q;
                        } else if (type.equals("LC")){
                            List c = new ArrayList(); // initializes then fills list of choices
                            NodeList choices = eElement.getElementsByTagName("choice");
                            for (int i = 0; i < choices.getLength(); i++) {
                                Node choice = choices.item(i);
                                Element e = (Element) choice;
                                String x = e.getElementsByTagName("ctext").item(0).getTextContent();
                                String extra = e.getElementsByTagName("extra").item(0).getTextContent();
                                List extra_questions = new ArrayList();
                                if(extra.equals("T")){
                                    // loop through list if multiple questions
                                    NodeList questions = eElement.getElementsByTagName("equestion");
                                    for(int k=0; k<questions.getLength();k++){
                                        Node question = questions.item(k);
                                        Element quest = (Element) question;
                                        String qtext = quest.getElementsByTagName("qtext").item(0).getTextContent();
                                        String qhint = quest.getElementsByTagName("qhint").item(0).getTextContent();
                                        String qtype = quest.getElementsByTagName("qtype").item(0).getTextContent();
                                        List qparts = new ArrayList();
                                        qparts.add(qtext);
                                        qparts.add(qhint);
                                        qparts.add(qtype);
                                        extra_questions.add(qparts);
                                    }

                                }
                                List stuff = new ArrayList();
                                stuff.add(x);
                                stuff.add(extra);
                                stuff.add(extra_questions);
                                c.add(stuff);
                            }
                            q = Questionnaire.LinkedQuestion(text, c, hint, context, builder);

                        }
                        // Sets up UI (keyboard down when screen touched outside text entry box) for each question's parts
                        setupUI(q);
                        int kids;
                        try{
                            kids = q.getChildCount();}
                        catch(Exception e){
                            kids = 0;
                        }
                        for (int i = 0; i < kids; i++){
                            setupUI(q.getChildAt(i));
                        }
                        // Adds question to group LinearLayout, overall list of questions,
                        // and list of questions to be mapped to group name in hash map of questions to groups
                        ll.addView(q);
                        Questions.add(q);
                        Qs.add(q);

                    }
                }

                // Iterates through all repeatable chunks in the group
                NodeList nList2 = eE.getElementsByTagName("rchunk");
                for(int z=0; z<nList2.getLength();z++){
                    Node chunk = nList2.item(z);
                    Element chunkE = (Element) chunk;
                    // question chunk button
                    Button rbt = new Button(this);
                    rbt.setText(chunkE.getElementsByTagName("rtext").item(0).getTextContent()+" +");
                    ll.addView(rbt);
                    setupUI(rbt);

                    // XML question chunk
                    NodeList chqs = chunkE.getElementsByTagName("rquestion");

                    // when button is clicked, following code takes as arguments linear layout, XML chunk of questions, context
                    rbt.setOnClickListener(new RepeatOnClickListener(ll,chqs,this) {
                        public void onClick(View v) {
                            // new linear layout of questions in chunk
                            LinearLayout qchunk = new LinearLayout(context);
                            qchunk.setOrientation(LinearLayout.VERTICAL);
                            // iterates through questions and adds them to the linear layout
                            for(int y=0;y<nlist.getLength();y++){
                                Node question = nlist.item(y);
                                if (question.getNodeType() == Node.ELEMENT_NODE){
                                    LinearLayout q = null;
                                    Element eElement = (Element) question;
                                    String text = "";
                                    String type = eElement.getElementsByTagName("qtype").item(0).getTextContent();
                                    String hint = eElement.getElementsByTagName("qhint").item(0).getTextContent();
                                    if(eElement.getElementsByTagName("req").item(0).getTextContent().equals("T")){
                                        text = eElement.getElementsByTagName("qtext").item(0).getTextContent()+"*";
                                    }
                                    else{
                                        text = eElement.getElementsByTagName("qtext").item(0).getTextContent();
                                    }
                                    System.out.println("ok");
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
                                        q = Questionnaire.SingleChoice(text, c, hint, context, builder);
                                    } else if (type.equals("MC")) {
                                        List c = new ArrayList();
                                        NodeList choices = eElement.getElementsByTagName("choice");
                                        for (int i = 0; i < choices.getLength(); i++) {
                                            Node choice = choices.item(i);
                                            Element e = (Element) choice;
                                            String x = e.getElementsByTagName("ctext").item(0).getTextContent();
                                            c.add(x);
                                        }
                                        q = Questionnaire.MultipleChoice(text, c, hint, context, builder);
                                    } else if (type.equals("M")){
                                        q = Map(text, context);
                                        map_question = q;
                                    } else if (type.equals("C")){
                                        q = Camera(text, context);
                                        camera_question = q;
                                    }
                                    if (!type.equals("C")){
                                        setupUI(q);
                                        for (int i = 0; i < q.getChildCount(); i++){
                                            setupUI(q.getChildAt(i));
                                        }
                                        qchunk.addView(q);
                                        String g_name = (String)((TextView) view1.getChildAt(0)).getText();
                                        Groups.get(g_name).add(q);
                                        Questions.add(q);

                                    }

                                }
                            }
                            view1.addView(qchunk, view1.getChildCount()-1);
                        }
                    });

                }

                // puts list of questions for current group in Groups dictionary with group name as key
                Groups.put(g_name,Qs);

                // creates back to menu, previous, and next buttons
                Button menu_button = new Button(this);
                menu_button.setText("Menu");
                menu_button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        vp.setCurrentItem(0, false);
                    }
                });

                Button prev = new Button(this);
                prev.setText("Previous");
                prev.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                vp.setCurrentItem(g_button, false);
                                            }
                                        });

                Button next = new Button(this);
                next.setText("Next");
                next.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        vp.setCurrentItem(g_button+2, false);
                        update_answers();
                    }
                });

                // creates a navigation bar at bottom of page, adds prev/next/menu buttons as appropriate
                LinearLayout navbar = new LinearLayout(this);
                navbar.setOrientation(LinearLayout.HORIZONTAL);
                if(g_button>0){
                    navbar.addView(prev);
                }
                navbar.addView(menu_button);
                navbar.addView(next);


                ll.addView(navbar);

                setupUI(ll);
                setupUI(menu_button);
                setupUI(navbar);
                setupUI(next);
                setupUI(prev);



            }

            // review page
            ScrollView rv = new ScrollView(this);
            LinearLayout rv1 = new LinearLayout(this);
            rv1.setOrientation(LinearLayout.VERTICAL);
            pg.addView(rv,pg.getCount());
            TextView rev = new TextView(this);
            rev.setTextSize(30);
            String review = "Review";
            rev.setText(review);
            rv1.addView(rev);
            rv.addView(rv1);

            // menu button
            Button menu_button = new Button(this);
            String menu = "Menu";
            menu_button.setText(menu);
            menu_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    vp.setCurrentItem(0, false);
                }
            });

            // submit button
            Button submit = new Button(this);
            String sub = "Submit";
            submit.setText(sub);
            submit.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    submit();
                }
            });

            // blank text view to be updated
            ans = new TextView(this);
            rv1.addView(ans);
            rv1.addView(menu_button);
            rv1.addView(submit);



        }catch (Exception e){
            e.printStackTrace();
        }

    }

    int PLACE_PICKER_REQUEST = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;

    // creates map question
    public LinearLayout Map(String questiontext, Context context){
        // set up question linear layout with text and button
        LinearLayout qlayout = new LinearLayout(context);
        qlayout.setOrientation(LinearLayout.VERTICAL);
        TextView tv = new TextView(context);
        tv.setTag("text");
        tv.setText(questiontext);
        Button bt = new Button(context);
        bt.setText(questiontext);
        bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT));
        final Activity a = this;

        // on click, attempts to start place picker activity
        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideSoftKeyboard(a,v);
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

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // depending on request code (1 for place picker, 2 for image capture), performs action
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK)  {


                // Displays map and prompts user to place picker on location
                Place place = PlacePicker.getPlace(this, data);
                // Displays location in toast message after map activity is closed
                String toastMsg = String.format("Place: %s", place.getLatLng());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                System.out.println(toastMsg);
                System.out.println(PlacePicker.getLatLngBounds(data));
                // saves location
                LOCATION = toastMsg.substring(7);
                TextView update_loc = new TextView(this);
                update_loc.setText(LOCATION);
                if(map_question.getChildCount() > 2){
                    map_question.removeView(map_question.getChildAt(map_question.getChildCount()-1));
                }
                map_question.addView(update_loc);
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // opens camera, saves image as bitmap
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView = new ImageView(this);
            mImageView.setImageBitmap(imageBitmap);
            camera_question.addView(mImageView);
            FileOutputStream out = null;
            try {
                String imageFileName = "JPEG_" + timeStamp + ".jpeg";
                out = openFileOutput(imageFileName, Context.MODE_PRIVATE);
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // don't generate a picture when report has not been completed

    // writes current answers and returns them as one string; optionally writes them to file
    public static String writeAnswers(HashMap qgs, boolean toFile, FileOutputStream f, boolean incomplete) {
        String total = ""; // string with all answers
        String unanswered = "REQUIRED QUESTIONS MUST BE FILLED OUT \n"; // string with all blank required questions
        Boolean use_un = false;
        // set of group names
        Set<String> keys = qgs.keySet();
        // iterates through group names
        for(String key:keys) {
            String name = "Section: "+key + "\n";
            total += name;
            //unanswered += "\n"+name;
            List qs = (List) qgs.get(key);
            // iterates through list of questionsg
            for (int i = 0; i < qs.size(); i++) {
                // gets question linear layout
                LinearLayout q = (LinearLayout) qs.get(i);
                // gets question text
                TextView text = (TextView) q.findViewWithTag("text");
                String question = (String) text.getText();
                String line = "";
                String tag = "";
                try {
                    tag = (String) q.getTag();
                } catch (Exception e) {
                    System.out.println("there's no tag?");
                }

                // based on question tag (type), completes question line with answer in appropriate fashion
                // if for submission, returns "" if any required (*) questions don't have answers
                // otherwise adds unanswered required questions to unanswered string
                System.out.println(tag);
                if (tag.equals("T") || tag.equals("N")) {
                    EditText editText = (EditText) q.findViewWithTag("answer");
                    if (editText.getText().toString().equals("") && question.endsWith("*") && !incomplete) {
                        System.out.println("oops");
                        return "";
                    } else {
                        if (editText.getText().toString().equals("") && question.endsWith("*")) {
                            unanswered += "\n" + name+question + "\n";
                            use_un = true;
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
                            if (id == -1) {
                                unanswered += "\n" + name+question + "\n";
                                use_un = true;
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
                        // writes answers to file
                        line = line + "~~";
                        total += line;
                        f.write(line.getBytes());
                    } catch (Exception e) {
                    }
                } else {
                    // adds line to total string of answers
                    line = line + "\n";
                    total += line;
                }
            }
        }
        if(use_un){
            // returns string of unanswered questions if any are present
            return unanswered;
        }
        // returns total string of questions and answers
        return total;
    }

    public String submit(){
        // time stamp of submission -> filename for file in which data from form at time to be saved
        //String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.US).format(new Date());
        String filename = timeStamp+".txt";
        FileOutputStream fos = null;
        // opens file
        try{
            fos = openFileOutput(filename, Context.MODE_PRIVATE);
            System.out.println(getFileStreamPath(filename));
        }catch(Exception e){
            return "";
        }

        // calls writeAnswers first to check for unanswered required questions
        String answers = Questionnaire.writeAnswers(Questions, false, fos, false);
        if (answers.equals("")) {
            AlertDialog.Builder bdr = builder;
            bdr.setMessage("Answer all required questions before submitting");
            AlertDialog dialog = bdr.create();
            dialog.show();
            this.deleteFile(filename);
            return "";
        } else{
            // if all required questions answered, writes questions and answers to file
            answers = Questionnaire.writeAnswers(Questions, true, fos, false);
        }
        try {
            fos.close();
        }catch(Exception e){}

        // goes back to main page of app
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        // resets values for new questionnaire
        Counter = new ArrayList();
        LOCATION = "";

        return filename;
    }

    // updates ans TextView on submit page with current answers
    public void update_answers(){
        answers = writeAnswers(Groups, false, null, true);
        ans.setText(answers);
    }

    // hides soft keyboard
    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    // calls hideSoftKeyboard on non-EditText views
    public Activity a = this;
    public void setupUI(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

                    hideSoftKeyboard(a, v);
                    return false;
                }
            });
        }
    }
}

// class used to create a PagerAdapter to work with ViewPager
class MainPagerAdapter extends PagerAdapter
{
    // This holds all the currently displayable views, in order from left to right.
    private ArrayList<View> views = new ArrayList<View>();

    public void addView (View v, int position)
    {
        views.add (position, v);
        super.notifyDataSetChanged();
    }

    public void removeView (int position)
    {
        views.remove (position);
        super.notifyDataSetChanged();
    }

    public boolean isViewFromObject (View view, Object object)
    {
        return view == object;
    }

    public int getCount ()
    {
        return views.size();
    }

    public Object instantiateItem (ViewGroup container, int position)
    {
        View v = views.get (position);
        container.addView (v);
        return v;
    }

    @Override
    public void destroyItem (ViewGroup container, int position, Object object)
    {
        container.removeView (views.get (position));
    }


}

// onClickListener customized for repeatable chunks
class RepeatOnClickListener implements View.OnClickListener
{
    LinearLayout view1;
    NodeList nlist;
    Context context;
    // can take a linear layout, nodelist, and context as parameters
    public RepeatOnClickListener(LinearLayout view1, NodeList nlist, Context context) {
        this.view1 = view1;
        this.nlist = nlist;
        this.context = context;
    }

    @Override
    public void onClick(View v)
    {
    }

};

