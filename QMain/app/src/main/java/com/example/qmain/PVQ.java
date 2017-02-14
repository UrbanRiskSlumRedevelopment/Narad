package com.example.qmain;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.FileOutputStream;
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
    public final static String DATA = "com.example.qmain.PREFERENCE_FILE_KEY";
    LinearLayout layout = null;
    List Questions = new ArrayList();
    HashMap<String,List> Groups = new HashMap<>();
    String answers = "";
    TextView ans = null;
    static List Questions2 = null;
    static List Counter = new ArrayList();
    public static AlertDialog.Builder builder = null;
    public Context context = this;
    public static String LOCATION = "";
    HashMap<String, Button> RepeatButtons = new HashMap();
    static ImageView mImageView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pvq);

        final ViewPager vp = (ViewPager) findViewById(R.id.activity_pvq);
        vp.setId(View.generateViewId());
        setupUI(vp);

        MainPagerAdapter pg = new MainPagerAdapter();
        vp.setAdapter(pg);

        SharedPreferences sharedPref = context.getSharedPreferences(
                DATA, Context.MODE_PRIVATE);

        AlertDialog.Builder newbuilder = new AlertDialog.Builder(this);
        builder = newbuilder;

        try{
            InputStream in = getResources().openRawResource(R.raw.questions);
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(in);
            doc.getDocumentElement().normalize();

            // first page
            ScrollView sv1 = new ScrollView(this);
            LinearLayout p1 = new LinearLayout(this);
            p1.setOrientation(LinearLayout.VERTICAL);

            pg.addView(sv1,0);
            sv1.addView(p1);
            setupUI(sv1);
            setupUI(p1);

            TextView title = new TextView(this);
            title.setTextSize(30);
            title.setText("Sections");
            p1.addView(title);


            // iterates through all groups
            NodeList groups = doc.getElementsByTagName("group");
            for(int g = 0; g<groups.getLength(); g++){
                System.out.println(g);
                Node gr = groups.item(g);
                Element eE = (Element) gr;
                final int g_button = g;
                String g_name = ((Element) gr).getElementsByTagName("gtext").item(0).getTextContent();

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

                // iterates through all question nodes
                NodeList nList = eE.getElementsByTagName("question");
                List Qs = new ArrayList();
                for(int j=0; j<nList.getLength();j++){
                    Node nNode = nList.item(j);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE){
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
                            System.out.println("lols");
                            q = Map(text, context);
                        } else if (type.equals("C")){
                            System.out.print("lol");
                            //q = Camera(text, context);
                        }
                        if (!type.equals("C")){
                            setupUI(q);
                            for (int i = 0; i < q.getChildCount(); i++){
                                setupUI(q.getChildAt(i));
                            }
                            ll.addView(q);
                            Questions.add(q);
                            Qs.add(q);

                        }
                    }
                }

                // repeatable chunks
                NodeList nList2 = eE.getElementsByTagName("rchunk");
                for(int z=0; z<nList2.getLength();z++){
                    Node chunk = nList2.item(z);
                    Element chunkE = (Element) chunk;
                    Button rbt = new Button(this);
                    rbt.setText(chunkE.getElementsByTagName("rtext").item(0).getTextContent()+" +");
                    ll.addView(rbt);
                    setupUI(rbt);

                    // question chunk
                    NodeList chqs = chunkE.getElementsByTagName("rquestion");



                    rbt.setOnClickListener(new RepeatOnClickListener(ll,chqs,this) {
                        public void onClick(View v) {
                            LinearLayout qchunk = new LinearLayout(context);
                            qchunk.setOrientation(LinearLayout.VERTICAL);
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
                                        System.out.println("lols");
                                        q = Map(text, context);
                                    } else if (type.equals("C")){
                                        System.out.print("lol");
                                        //q = Camera(text, context);
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

                    //ll.addView(qchunk);


                }

                Groups.put(g_name,Qs);

                // creates back to menu button

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
            rev.setText("Review");
            rv1.addView(rev);
            rv.addView(rv1);

            Button menu_button = new Button(this);
            menu_button.setText("Menuuuuu");
            menu_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    vp.setCurrentItem(0, false);
                }
            });

            Button submit = new Button(this);
            submit.setText("Submit");
            submit.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    submit();
                }
            });

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
        final Activity a = this;

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

    public static String writeAnswers(HashMap qgs, boolean toFile, FileOutputStream f, boolean incomplete) {
        String total = "";
        Set<String> keys = qgs.keySet();
        for(String key:keys) {
            String name = "Group: "+key + "\n";
            total += name;
            List qs = (List) qgs.get(key);
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
                        if (editText.getText().toString().equals("") && question.endsWith("*")) {
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
                    } catch (Exception e) {
                    }
                } else {
                    line = line + "\n";
                    total += line;
                }
            }
        }
        return total;
    }

    public String submit(){
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

        String answers = Questionnaire.writeAnswers(Questions, false, fos, false);
        if (answers.equals("")) {
            AlertDialog.Builder bdr = builder;
            bdr.setMessage("Answer all required questions before submitting");
            AlertDialog dialog = bdr.create();
            dialog.show();
            this.deleteFile(filename);
            return "";
        } else{
            answers = Questionnaire.writeAnswers(Questions, true, fos, false);
        }
        try {
            fos.close();
        }catch(Exception e){}

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        Counter = new ArrayList();
        LOCATION = "";

        return filename;

        //hexcode, title from question field, device id, account

    }

    public void update_answers(){
        answers = writeAnswers(Groups, false, null, true);
        ans.setText(answers);
    }

    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    public Activity a = this;
    // call set up on all child views of a view etc
    public void setupUI(View view) {
        // lose focus on text
        // Set up touch listener for non-text box views to hide keyboard.
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

class RepeatOnClickListener implements View.OnClickListener
{
    LinearLayout view1;
    NodeList nlist;
    Context context;
    public RepeatOnClickListener(LinearLayout view1, NodeList nlist, Context context) {
        this.view1 = view1;
        this.nlist = nlist;
        this.context = context;
    }

    @Override
    public void onClick(View v)
    {
        //read your lovely variable
    }

};

