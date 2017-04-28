package com.example.qmain;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.content.DialogInterface;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.graphics.Color;
import android.os.Environment;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.widget.ImageButton;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.io.File;
import java.io.FileNotFoundException;
import android.util.TypedValue;
import android.view.Gravity;
import android.util.AttributeSet;
import android.support.v4.view.GravityCompat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.InputFilter;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.PagerAdapter;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import android.support.design.widget.NavigationView;
import android.view.Menu;

public class PVQ extends AppCompatActivity {
    List Questions = new ArrayList(); // List of all questions
    HashMap<String,List> Groups = new HashMap<>(); // Hash map mapping questions to groups
    List Image_Tags = new ArrayList();
    String answers = "";
    TextView ans = null;
    LinearLayout req_buttons;
    static List Counter = new ArrayList();
    public static AlertDialog.Builder builder = null; // For building alert dialogs when necessary
    public Context context = this; // For accessing context of the questionnaire
    public static String LOCATION = ""; // Location stored here
    ImageView mImageView = null;
    LinearLayout camera_question = null;
    LinearLayout map_question = null;
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
    String author = "";
    List pages = new ArrayList();
    ViewPager vp = null;
    DrawerLayout dl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pvq);

        // Create and set up new questionnaire ViewPager
        //final ViewPager vp = (ViewPager) findViewById(R.id.activity_pvq);
        //NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        NavigationView nv = (NavigationView) findViewById(R.id.navigation);
        dl = (DrawerLayout) findViewById(R.id.drawer_layout);

        LinearLayout form = (LinearLayout) findViewById(R.id.activity_pvq);
        author = getIntent().getStringExtra("author");
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Questionnaire");
            ImageButton hamburger = new ImageButton(this);
            hamburger.setImageResource(R.drawable.hamburger);
            int co = ContextCompat.getColor(context, R.color.colorPrimary);
            hamburger.setBackgroundColor(co);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            ActionBar.LayoutParams hl = new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT, Gravity.END);
            getSupportActionBar().setCustomView(hamburger, hl);
            hamburger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dl.openDrawer(GravityCompat.END);
                }
            });
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        vp = new ViewPager(this);
        vp.setId(View.generateViewId());
        setupUI(vp);
        form.addView(vp);

        final MainPagerAdapter pg = new MainPagerAdapter();
        vp.setAdapter(pg);

        LinearLayout navbar = new LinearLayout(this);
        navbar.setOrientation(LinearLayout.HORIZONTAL);
        navbar.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));


        // creates back to menu, previous, and next buttons
        Button menu_button = new Button(this);
        String menu = "Menu";
        menu_button.setText(menu);
        menu_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            try {
                vp.setCurrentItem(0, false);
                if(vp.getCurrentItem() != vp.getChildCount()-1){
                    ans.setText("");
                }
            }catch(Exception e){
                System.out.println("no menu");
            }
            }
        });

        Button scroll_up = new Button(this);
        String btt = "Back to Top";
        scroll_up.setText(btt);
        scroll_up.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    ScrollView view = (ScrollView) vp.findViewWithTag("myview" + vp.getCurrentItem());
                    view.fullScroll(ScrollView.FOCUS_UP);
                }catch(Exception e){
                    System.out.println("scroll up error");
                }
            }
        });

        Button prev = new Button(this);
        String pr = "Previous";
        prev.setText(pr);
        prev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    int gb = vp.getCurrentItem();
                    vp.setCurrentItem(gb -1, false);
                    if(vp.getCurrentItem() != vp.getChildCount()-1){
                        ans.setText("");
                    }
                }catch(Exception e){
                    System.out.println("prev button error");
                }
            }
        });

        Button next = new Button(this);
        String nxt = "Next";
        next.setText(nxt);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    int gb = vp.getCurrentItem();
                    vp.setCurrentItem(gb + 1, false);
                }catch(Exception e){
                    System.out.println("next button error");
                }
            }
        });

        setupUI(prev);
        setupUI(menu_button);
        setupUI(next);
        setupUI(scroll_up);

        navbar.addView(prev);
        navbar.addView(scroll_up);
        navbar.addView(next);
        navbar.setGravity(Gravity.CENTER);
        setupUI(navbar);

        form.addView(navbar);

        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                1.0f
        );
        LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                12.0f
        );

        vp.setLayoutParams(param1);
        navbar.setLayoutParams(param2);

        // Sets up an alert dialog builder for use when necessary
        builder = new AlertDialog.Builder(this);

        // Builds Questionnaire
        try{
            // Parses XML doc so code can read it
            InputStream in = getResources().openRawResource(R.raw.questionnaire);
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(in);
            doc.getDocumentElement().normalize();
            in.close();

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
            pages.add(sections);


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
                pages.add(g_name);
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
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        LinearLayout qu = Questionnaire.build_question(nNode, Questions, Qs, ll, this);
                        if(null == qu){
                            Element eElement = (Element) nNode;
                            // Obtains question type, hint, and text; adds * to question text if required
                            String text = eElement.getElementsByTagName("qtext").item(0).getTextContent();
                            String type = eElement.getElementsByTagName("qtype").item(0).getTextContent();
                            if (type.equals("M")){
                                // Map question
                                qu = Map(text, context);
                                map_question = qu;
                                Questions.add(qu);
                                Qs.add(qu);
                                ll.addView(qu);
                            } else if (type.equals("C")) {
                                // Camera question; still being worked out
                                qu = Camera(text, context);
                                camera_question = qu;
                                Questions.add(qu);
                                Qs.add(qu);
                                ll.addView(qu);
                            }
                        }
                    }
                }
                for(int qn = 0; qn < Questions.size(); qn++){
                    LinearLayout q = (LinearLayout) Questions.get(qn);
                    setupUI(q);
                    int x = q.getChildCount();
                    for(int qi = 0; qi < x; qi++){
                        setupUI(q.getChildAt(qi));
                    }
                }

                // Iterates through all repeatable chunks in the group
                NodeList nList2 = eE.getElementsByTagName("rchunk");
                String num = "";
                try{
                    num = eE.getElementsByTagName("rsize").item(0).getTextContent();
                }catch(Exception e){
                    num="";
                }
                for(int z=0; z<nList2.getLength();z++){
                    Node chunk = nList2.item(z);
                    Element chunkE = (Element) chunk;
                    if(num.equals("")) {
                        // question chunk button
                        Button rbt = new Button(this);
                        String rtext = chunkE.getElementsByTagName("rtext").item(0).getTextContent() + " +";
                        rbt.setText(rtext);
                        ll.addView(rbt);
                        setupUI(rbt);

                        // XML question chunk
                        NodeList chqs = chunkE.getElementsByTagName("rquestion");

                        // when button is clicked, following code takes as arguments linear layout, XML chunk of questions, context
                        rbt.setOnClickListener(new RepeatOnClickListener(ll, chqs, this) {
                            public void onClick(View v) {
                                // new linear layout of questions in chunk
                                LinearLayout qchunk = new LinearLayout(context);
                                qchunk.setOrientation(LinearLayout.VERTICAL);
                                // iterates through questions and adds them to the linear layout
                                for (int y = 0; y < nlist.getLength(); y++) {
                                    Node question = nlist.item(y);
                                    if (question.getNodeType() == Node.ELEMENT_NODE) {
                                        String g_name = (String) ((TextView) view1.getChildAt(0)).getText();
                                        LinearLayout qu = Questionnaire.build_question(question, Questions, Groups.get(g_name), qchunk, context);
                                    }
                                }
                                view1.addView(qchunk, view1.getChildCount() - 1);
                            }
                        });
                    }else{
                        EditText num_times = new EditText(this);
                        num_times.setHint(num);
                        num_times.setInputType(2);
                        String limit = (chunkE.getElementsByTagName("rlimit").item(0).getTextContent());
                        int max = Integer.parseInt(limit);
                        NodeList chqs = chunkE.getElementsByTagName("rquestion");
                        LinearLayout questions_here = new LinearLayout(this);
                        questions_here.setOrientation(LinearLayout.VERTICAL);
                        num_times.addTextChangedListener(new NumWatcher(max, questions_here, chqs, this, Questions, Qs));
                        ll.addView(num_times);
                        ll.addView(questions_here);
                    }

                }

                // puts list of questions for current group in Groups dictionary with group name as key
                Groups.put(g_name,Qs);

                setupUI(ll);
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

            // review button
            Button review_button = new Button(this);
            review_button.setText(review);
            review_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    update_answers();
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
            req_buttons = new LinearLayout(this);
            req_buttons.setOrientation(LinearLayout.VERTICAL);
            rv1.addView(review_button);
            rv1.addView(ans);
            rv1.addView(req_buttons);
            rv1.addView(submit);

            vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if(vp.getCurrentItem() != vp.getChildCount()-1) {
                        req_buttons.removeAllViews();
                        req_buttons.setBackgroundColor(Color.TRANSPARENT);
                        ans.setText("");
                    }
                }
                @Override
                public void onPageSelected(int position) {}
                @Override
                public void onPageScrollStateChanged(int state) {}
            });

            vp.setCurrentItem(1);
            Menu menu1 = nv.getMenu();
            for(int i = 1; i < pages.size(); i++){
                menu1.add((String)pages.get(i));
            }
            for(int i = 0; i < menu1.size(); i++){
                menu1.getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        vp.setCurrentItem(pages.indexOf(item.getTitle()));
                        dl.closeDrawer(GravityCompat.END);
                        return false;
                    }
                });
            }


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
        qlayout.setOrientation(LinearLayout.VERTICAL);
        TextView tv = new TextView(context);
        tv.setTag("text");
        tv.setText(questiontext);
        Button bt = new Button(context);
        bt.setText(questiontext);
        bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT));

        final AlertDialog.Builder bdr = new AlertDialog.Builder(this);
        final Context context1 = this;

        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final EditText input_tag = new EditText(context1);
                input_tag.setInputType(InputType.TYPE_CLASS_TEXT);
                input_tag.setHint("Enter a description (max 30 characters)");
                int maxLength = 30;
                input_tag.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
                bdr.setView(input_tag);
                // Set up the buttons
                bdr.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //tag = input_tag.getText().toString();
                        dispatchTakePictureIntent(input_tag.getText().toString());
                    }
                });
                bdr.setNegativeButton("No description", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dispatchTakePictureIntent("");
                        dialog.cancel();
                    }
                });
                AlertDialog alert = bdr.create();
                alert.show();
            }
        });
        qlayout.addView(tv);
        qlayout.addView(bt);
        qlayout.setTag("C");
        return qlayout;
    }

    String mCurrentPhotoPath = "";

    private File createImageFile(String tag) throws IOException {
        // Create an image file name
        Image_Tags.add(tag);
        System.out.println(Image_Tags);
        tag = tag.replaceAll(" ", "_");
        System.out.println(tag);
        String imageFileName = timeStamp+"_t__"+tag+"__t_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpeg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        System.out.println(imageFileName);
        System.out.println(mCurrentPhotoPath);
        return image;
    }

    private void dispatchTakePictureIntent(String tag) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(tag);
            } catch (IOException ex) {
                System.out.println("filename creation error");
            }
            // Continue only if the File was successfully created
            System.out.println(getFilesDir());

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.qmain.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            }
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
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView = new ImageView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            mImageView.setLayoutParams(lp);
            File file = new File(mCurrentPhotoPath);
            Uri uri = Uri.fromFile(file);
            Bitmap imageBitmap;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                float w = imageBitmap.getWidth();
                float h = imageBitmap.getHeight();
                int width = 480;
                float hw_ratio = width/w;
                float new_h = hw_ratio*h;
                int nh = (int) new_h;
                Bitmap scaled = Bitmap.createScaledBitmap(imageBitmap, width, nh, true);

                mImageView.setImageBitmap(scaled);
                System.out.println("bitmap set");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.out.println("NO FILE");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("SOMETHING ELSE");
            }
            mImageView.setBackgroundColor(Color.CYAN);
            TextView itag = new TextView(this);
            itag.setText((String)Image_Tags.get(Image_Tags.size()-1));
            camera_question.addView(itag);
            camera_question.addView(mImageView);
        }
    }
    // don't generate a picture when report has not been completed

    // writes current answers and returns them as one string; optionally writes them to file
    public String writeAnswers(HashMap qgs, boolean toFile, FileOutputStream f, boolean incomplete, FileOutputStream jf) {
        String total = ""; // string with all answers
        //String unanswered = "REQUIRED QUESTIONS MUST BE FILLED OUT \n"; // string with all blank required questions
        String unanswered = "";
        Boolean use_un = false;
        HashMap json = new HashMap();
        HashMap groups = new HashMap();
        json.put("Groups", groups);
        if (toFile) {
            try {
                // writes answers to file
                json.put("Author", author);
                String line = "Author: " + author + "~~";
                total += line;
                f.write(line.getBytes());
            } catch (Exception e) {
                System.out.println("problem writing line");
            }
        }
        // set of group names
        Set<String> keys = qgs.keySet();
        // iterates through group names
        for(String key:keys) {
            String name = "Section: "+key + "\n";
            total += name;
            //unanswered += "\n"+name;
            List qs = (List) qgs.get(key);
            boolean in_list = false;
            // iterates through list of questions

            HashMap questions = new HashMap();
            groups.put(key, questions);

            for (int i = 0; i < qs.size(); i++) {
                // gets question linear layout
                LinearLayout q = (LinearLayout) qs.get(i);
                // gets question text
                TextView text = (TextView) q.findViewWithTag("text");
                text.setTextColor(Color.GRAY);
                String question = (String) text.getText();
                String line = "";
                String tag = "";
                String qans = "";
                try {
                    tag = (String) q.getTag();
                } catch (Exception e) {
                    System.out.println("there's no tag?");
                }

                // based on question tag (type), completes question line with answer in appropriate fashion
                // if for submission, returns "" if any required (*) questions don't have answers
                // otherwise adds unanswered required questions to unanswered string
                if (tag.equals("T") || tag.equals("N")) {
                    EditText editText = (EditText) q.findViewWithTag("answer");
                    if (editText.getText().toString().equals("") && question.endsWith("*") && !incomplete) {
                        System.out.println("oops");
                        return "";
                    } else {
                        if (editText.getText().toString().equals("") && question.endsWith("*")) {
                            //unanswered += "\n" + name+question + "\n";
                            if(! in_list){
                                unanswered += key + ",, ";
                                in_list = true;
                            }
                            use_un = true;
                            text.setTextColor(Color.RED);
                        }
                        line = question + ": " + editText.getText();
                        qans = line.substring(line.indexOf(": ") + 2);
                    }
                } else if (tag.equals("S")){
                    TextView answer_total = (TextView) q.findViewWithTag("answer");
                    String at = answer_total.getText().toString();
                    if(question.endsWith("*") && !incomplete && at.equals("Total: ")){
                        System.out.println("oops");
                        return "";
                    }else if(question.endsWith("*")&& at.equals("Total: ")){
                        //unanswered += "\n" + name+question + "\n";
                        if(! in_list){
                            unanswered += key + ",, ";
                            in_list = true;
                        }
                        use_un = true;
                        text.setTextColor(Color.RED);
                    } else{
                        try {
                            if(at.equals("Total: ")){
                                at = " 0";
                            }
                            line = question + ": " + at.substring(at.indexOf(" ") + 1);
                            qans = at.substring(at.indexOf(" ") + 1);
                        }catch(Exception e){
                            line = question + ": ";
                        }
                    }
                } else if (tag.equals("SC")) {
                    line = question + ": ";
                    RadioGroup rg = (RadioGroup) q.findViewWithTag("choices");
                    int id = rg.getCheckedRadioButtonId();
                    if (id == -1 && !incomplete && question.endsWith("*")) {
                        return "";
                    } else {
                        RadioButton rb = (RadioButton) rg.getChildAt(id);
                        try {
                            if (id == -1  && question.endsWith("*")) {
                                //unanswered += "\n" + name+question + "\n";
                                if(! in_list){
                                    unanswered += key + ",, ";
                                    in_list = true;
                                }
                                use_un = true;
                                text.setTextColor(Color.RED);
                                line = line.toUpperCase();
                                continue;
                            } else if(id == -1){
                                continue;
                            }
                            line += rb.getText();
                            qans = (String) rb.getText();
                        } catch (Exception e) {
                            System.out.println("something went wrong");
                            System.out.println(question);
                        }
                    }
                } else if (tag.equals("MC")) {
                    line = question + ": ";
                    for (int j = 0; j < q.getChildCount(); j++) {
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
                    qans = line.substring(line.indexOf(": ") + 2);
                } else if (tag.equals("M")) {
                    line = question + ": " + LOCATION;
                    qans = LOCATION;
                } else if (tag.equals("C")) {
                    String imtags = "";
                    for(int t = 0; t<Image_Tags.size()-1; t++){
                        imtags += Image_Tags.get(t) + ", ";
                    }
                    if(Image_Tags.size() > 0) {
                        imtags += Image_Tags.get(Image_Tags.size() - 1);
                    }
                    line = question + ": ";
                    line += imtags;
                    qans = imtags;
                }
                String jquestion = question;
                if(question.endsWith("*")){
                    jquestion = question.substring(0,question.length()-1);
                }
                questions.put(jquestion,qans);
                if (toFile) {
                    try {
                        // writes answers to file
                        line = line + "~~";
                        total += line;
                        f.write(line.getBytes());
                    } catch (Exception e) {
                        System.out.println("problem writing line");
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
            return "!<!," + unanswered;
        }

        if(jf != null) {
            JSONObject j = new JSONObject(json);
            String jstring = j.toString();
            try {
                jf.write(jstring.getBytes());
            }catch(Exception e){
                System.out.println("problem writing json");
            }
        }

        // returns total string of questions and answers
        return total;
    }

    public String submit(){
        // time stamp of submission -> filename for file in which data from form at time to be saved
        //String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.US).format(new Date());
        String filename = timeStamp+".txt";
        String jsonfilename = timeStamp + ".json";
        FileOutputStream fos = null;
        FileOutputStream jfos = null;
        // opens file
        try{
            fos = openFileOutput(filename, Context.MODE_PRIVATE);
            jfos = openFileOutput(jsonfilename, Context.MODE_PRIVATE);
        }catch(Exception e){
            return "";
        }

        // calls writeAnswers first to check for unanswered required questions
        String answers = writeAnswers(Groups, false, fos, false, null);
        if (answers.equals("")) {
            AlertDialog.Builder bdr = builder;
            bdr.setMessage("Answer all required questions before submitting");
            AlertDialog dialog = bdr.create();
            dialog.show();
            this.deleteFile(filename);
            return "";
        } else{
            // if all required questions answered, writes questions and answers to file
            System.out.println(writeAnswers(Groups, true, fos, false, jfos));
        }
        try {
            fos.close();
            jfos.close();
        }catch(Exception e){}

        // goes back to main page of app
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);

        // resets values for new questionnaire
        Counter = new ArrayList();
        LOCATION = "";
        mImageView = null;
        timeStamp = "";

        return filename;
    }

    // updates ans TextView on submit page with current answers
    public void update_answers(){
        answers = writeAnswers(Groups, false, null, true, null);
        //ans.setText(answers);
        if(answers.substring(0,4).equals("!<!,")){
            String req_msg = "The following sections have required questions that need to be answered:";
            ans.setText(req_msg);
            List rsects = new ArrayList();
            String scts = answers.substring(4);
            req_buttons.setBackgroundColor(Color.MAGENTA);
            while(scts.length() > 3){
                System.out.println(pages);
                String sct = scts.substring(0, scts.indexOf(",, "));
                scts = scts.substring(scts.indexOf(",, ") + 3);
                Button br = new Button(this);
                br.setText(sct);
                br.setOnClickListener(new StringOnClickListener(sct) {
                    public void onClick(View v) {
                        vp.setCurrentItem(pages.indexOf(s));
                        System.out.println(s+"   " + Integer.toString(pages.indexOf(s)));
                    }
                });
                req_buttons.addView(br);
            }
            System.out.println(scts);
        }else{
            ans.setText(answers);
        }
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit? Your answers will not be saved.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteFile("JPEG_" + timeStamp + ".jpeg");
                        PVQ.this.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                onBackPressed();
                break;

        }

        return true;
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
        v.setTag("myview" + position);
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

}

class StringOnClickListener implements View.OnClickListener
{
    String s;
    // can take a linear layout, nodelist, and context as parameters
    public StringOnClickListener(String str) {
        this.s = str;
    }

    @Override
    public void onClick(View v)
    {
    }

}

class NumWatcher implements TextWatcher {
    private LinearLayout view1;
    private NodeList nodes;
    private Context context;
    private List list1;
    private List list2;
    private int max;
    NumWatcher(int max, LinearLayout ll, NodeList nodes, Context context, List list1, List list2){
        this.view1 = ll;
        this.nodes = nodes;
        this.context = context;
        this.list1 = list1;
        this.list2 = list2;
        this.max = max;
    }

    public void afterTextChanged(Editable s) {
        String value = s.toString();
        for(int i = 0; i < view1.getChildCount(); i++){
            for(int j = 0; j < ((LinearLayout)view1.getChildAt(i)).getChildCount(); j++){
                System.out.println(list1.remove(((LinearLayout)view1.getChildAt(i)).getChildAt(j)));
                System.out.println(list2.remove(((LinearLayout)view1.getChildAt(i)).getChildAt(j)));
            }
        }
        view1.removeAllViews();
        if(value.equals("")){
            return;
        }
        int times = Integer.parseInt(value);
        if(times > max){
            System.out.println("in here");
            return;
        }
        for(int i = 0;i<times;i++){
            LinearLayout qchunk = new LinearLayout(context);
            qchunk.setOrientation(LinearLayout.VERTICAL);
            for (int y = 0; y < nodes.getLength(); y++) {
                Node question = nodes.item(y);
                if (question.getNodeType() == Node.ELEMENT_NODE) {
                    LinearLayout qu = Questionnaire.build_question(question, list1, list2, qchunk, context);
                }
            }
            view1.addView(qchunk, view1.getChildCount() - 1);
        }
    }
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
}

class CustomDrawerLayout extends DrawerLayout {

    public CustomDrawerLayout(Context context) {
        super(context);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}

