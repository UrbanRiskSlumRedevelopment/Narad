package com.example.qmain;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import android.view.Gravity;
import android.support.v4.view.GravityCompat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.InputFilter;

import android.support.v4.view.PagerAdapter;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import android.support.design.widget.NavigationView;
import android.view.Menu;

import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Builds questionnaire
 */
public class PVQ extends AppCompatActivity {
    HashMap<String,LinearLayout> NumQuestions = new HashMap<>(); // hash map mapping questions to their question numbers
    HashMap<String,Integer> Dependents = new HashMap<>(); // maps number of currently positive answers a question is dependent on to the question's id
    HashMap<String,List> Groups = new HashMap<>(); // hash map mapping questions to groups
    List<String> Image_Tags = new ArrayList<>(); // list of image tags
    HashMap<String, String> Images = new HashMap<>(); // hash map mapping tags to their image files
    String answers = ""; // string for storing text of answers to questionnaire
    TextView ans = null; // TextView where answers will be displayed on review page
    LinearLayout req_buttons; // LinearLayout on which buttons of unanswered required questions will go when user reviews
    public static AlertDialog.Builder builder = null; // For building alert dialogs when necessary
    public Context context = this; // For accessing context of the questionnaire activity
    public static String LOCATION = ""; // Location stored here
    ImageView mImageView = null; // Most recent taken image
    LinearLayout camera_question = null; // camera question
    LinearLayout map_question = null; // map question
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date()); // date and time at which questionnaire was created
    String ogTimeStamp = timeStamp; // another variable storing original timestamp in case timeStamp is modified
    String author = ""; // username of survey taker
    List<Object> pages = new ArrayList<>(); // list of pages (one page for each group) for navigation drawer
    ViewPager vp = null; // ViewPager whose pages will make up the questionnaire
    DrawerLayout dl = null; // Side drawer for navigation
    List<String> Identifiers = new ArrayList<>(); // questions whose answers will be used to generate the questionnaire's uid
    String uid = ""; // unique id of survey
    Boolean uid_set = false; // whether unique id has been set
    String project; // project hash

    /**
     * Builds questionnaire
     *
     * @param savedInstanceState saved instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pvq);

        // PAGE SETUP

        // navigation drawer
        NavigationView nv = (NavigationView) findViewById(R.id.navigation);
        dl = (DrawerLayout) findViewById(R.id.drawer_layout);

        // linear layout containing ViewPager and navigation bar
        LinearLayout form = (LinearLayout) findViewById(R.id.activity_pvq);
        author = getIntent().getStringExtra("author");

        // sets up support action bar if it exists
        if(getSupportActionBar() != null) {
            // titles page
            getSupportActionBar().setTitle("Questionnaire");
            // hamburger button that opens navigation drawer
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
            // displays arrow button allowing viewer to return to project home page
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        project = getIntent().getStringExtra("project"); // project hash

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // creates and sets up new questionnaire ViewPager
        vp = new ViewPager(this);
        vp.setId(View.generateViewId());
        setupUI(vp);
        form.addView(vp);

        final MainPagerAdapter pg = new MainPagerAdapter();
        vp.setAdapter(pg);

        // navigation bar
        LinearLayout navbar = new LinearLayout(this);
        navbar.setOrientation(LinearLayout.HORIZONTAL);
        navbar.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));


        // creates back to top, previous, and next buttons for navigation bar
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
        setupUI(next);
        setupUI(scroll_up);

        // adds buttons to navigation bar
        navbar.addView(prev);
        navbar.addView(scroll_up);
        navbar.addView(next);
        navbar.setGravity(Gravity.CENTER);
        setupUI(navbar);

        // adds navigation bar to page
        form.addView(navbar);

        // adjusting parameters for layout of view pager and navigation bar
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

        // QUESTIONNAIRE

        // Builds Questionnaire
        try{
            // document builder for reading xml
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            // opens project questionnaire saved at questions_from_url.xml
            FileInputStream fis;
            try{
                fis = context.openFileInput("questions_from_url.xml");
            }catch(Exception e){
                System.out.println("nope");
                fis = null;
            }
            
            /*
            // parses xml and saves as doc
            Document doc = dBuilder.parse(fis);
            if(fis!=null) {
                fis.close();
            }
            */

            // parses xml from questionnaire_with_nums and saves as doc
            // use above commented out code when getting questionnaires from the server works
            // next three lines are just for testing when getting surveys from the server doesn't work
            InputStream in = getResources().openRawResource(R.raw.questionnaire_with_nums);
            Document doc = dBuilder.parse(in);
            in.close();

            doc.getDocumentElement().normalize();

            // iterates through all question groups in XML doc
            NodeList groups = doc.getElementsByTagName("group");
            for(int g = 0; g<groups.getLength(); g++){
                System.out.println(g);
                Node gr = groups.item(g);
                Element eE = (Element) gr; // group element from xml
                String g_name = eE.getElementsByTagName("gtext").item(0).getTextContent(); // group name

                pages.add(g_name); // adds group to list of pages

                // creates group page
                ScrollView sv = new ScrollView(this);
                LinearLayout ll = new LinearLayout(this); // linear layout for group page
                ll.setOrientation(LinearLayout.VERTICAL);
                sv.addView(ll);
                pg.addView(sv,g);
                setupUI(sv);

                // adds name to group page
                TextView group_name = new TextView(this);
                group_name.setText(g_name);
                group_name.setTextSize(30);
                ll.addView(group_name);

                // iterates through all questions in group
                NodeList nList = eE.getElementsByTagName("question");
                List<LinearLayout> Qs = new ArrayList<>(); // list of question linear layouts to be mapped to group in Groups
                System.out.println(nList.getLength());
                for(int j=0; j<nList.getLength();j++){
                    Node nNode = nList.item(j);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        // builds question linear layout and adds question to Qs, NumQuestions, and Dependents
                        // does not handle map or camera questions, those built separately below
                        LinearLayout qu = Questionnaire.build_question(nNode, Qs, ll, this, NumQuestions,
                                Dependents);
                        if(null == qu){
                            // builds map or camera question
                            Element eElement = (Element) nNode;
                            // Obtains question type, hint, and text; adds * to question text if required
                            String text = eElement.getElementsByTagName("qtext").item(0).getTextContent();
                            String type = eElement.getElementsByTagName("qtype").item(0).getTextContent();
                            if (type.equals("M")){
                                // Map question
                                // creates map question and adds it to appropriate lists/linear layout
                                String qid = eElement.getElementsByTagName("q").item(0).getTextContent();
                                qu = Map(text, context);
                                map_question = qu;
                                Qs.add(qu);
                                ll.addView(qu);
                                // assigns question its question number
                                TextView tq = new TextView(this);
                                tq.setText(qid);
                                tq.setVisibility(View.GONE);
                                tq.setTag("qid");
                                qu.addView(tq);
                                NumQuestions.put(qid, qu);
                            } else if (type.equals("C")) {
                                // Camera question
                                // creates camera question and adds it to appropriate lists/linear layout
                                String qid = eElement.getElementsByTagName("q").item(0).getTextContent();
                                qu = Camera(text, context);
                                camera_question = qu;
                                Qs.add(qu);
                                ll.addView(qu);
                                // assigns question its question number
                                TextView tq = new TextView(this);
                                tq.setText(qid);
                                tq.setVisibility(View.GONE);
                                tq.setTag("qid");
                                qu.addView(tq);
                                NumQuestions.put(qid, qu);
                            }
                        }
                    }
                }
                // sets up UI (soft keyboard hidden when focus not on EditText) for each question
                // and its children in group
                for(int qn = 0; qn < Qs.size(); qn++){
                    LinearLayout q = Qs.get(qn);
                    setupUI(q);
                    int x = q.getChildCount();
                    for(int qi = 0; qi < x; qi++){
                        setupUI(q.getChildAt(qi));
                    }
                }

                NodeList nList2 = eE.getElementsByTagName("rchunk"); // list of all repeatable chunks in questionnaire

                // iterates through all repeatable chunks in the group
                for(int z=0; z<nList2.getLength();z++){
                    String num; // hint for EditText in which user specifies how many of repeatable chunks to display
                    try{
                        num = eE.getElementsByTagName("rsize").item(0).getTextContent();
                    }catch(Exception e){
                        num="";
                    }
                    Node chunk = nList2.item(z);
                    Element chunkE = (Element) chunk;
                    String qlims = eE.getElementsByTagName("rlimitq").item(0).getTextContent(); // limiting questions (if any)
                    EditText num_times = new EditText(this); // EditText in which user specifies number of repeatable chunks
                    num_times.setHint(num);
                    num_times.setInputType(2);
                    // user-set static limit on how many times set of repeatable questions can be repeated
                    String limit = (chunkE.getElementsByTagName("rlimit").item(0).getTextContent());
                    int max = Integer.parseInt(limit);
                    NodeList chqs = chunkE.getElementsByTagName("rquestion");
                    LinearLayout questions_here = new LinearLayout(this);
                    questions_here.setOrientation(LinearLayout.VERTICAL);
                    // sets up EditText so that it displays the correct (and a valid) number of repeatable chunks based on answer
                    num_times.addTextChangedListener(new NumWatcher(max, questions_here, chqs, this, Qs, NumQuestions, Dependents, qlims));
                    ll.addView(num_times);
                    ll.addView(questions_here);


                }

                // puts list of questions for current group in Groups dictionary with group name as key
                Groups.put(g_name,Qs);

                setupUI(ll);
            }

            // iterates through id fields and records numbers of identifier questions in Identifiers
            NodeList ids = doc.getElementsByTagName("idfield");
            for(int d=0; d<ids.getLength();d++){
                Node idf = ids.item(d);
                Element idfe = (Element) idf;
                String idstring = idfe.getTextContent();
                Identifiers.add(idstring);
            }
            System.out.println(Identifiers);

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

            // blank text view and blank linear layout to be updated on pressing review (through update_answers())
            // ans will be populated with text of the questions/answers of survey if all required questions are answered
            // req_buttons will be populated with buttons to sections where there are unanswered required questions
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
                        // sets required buttons ans answers text view to blank when user leaves review page
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

            pages.add("Review ");

            vp.setCurrentItem(0); // starts questionnaire on first page

            // populates navigation drawer menu with names of each page in questionnaire
            // sets up navigation drawer items so that clicking on them results in navigation to the corresponding page
            // and closing of the drawer
            Menu menu1 = nv.getMenu();
            for(int i = 0; i < pages.size(); i++){
                menu1.add((String)pages.get(i));
                System.out.println((String)pages.get(i));
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

    /**
     * Creates map question consisting of a button that opens up a map on which user picks a location,
     * after which the location gets processed in onActivityResult()
     *
     * @param questiontext text of map question
     * @param context context of current activity
     * @return qlayout, LinearLayout map question
     */
    public LinearLayout Map(String questiontext, Context context){
        LinearLayout qlayout = new LinearLayout(context); // question LinearLayout
        qlayout.setOrientation(LinearLayout.VERTICAL);
        // creates text and button that make up map question
        TextView tv = new TextView(context);
        tv.setTag("text");
        tv.setText(questiontext);
        Button bt = new Button(context);
        bt.setText(questiontext);
        bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT));
        final Activity a = this;

        // on click, attempts to start place picker activity using place picker request code
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
        // adds text and button to map question linear layout, returns linear layout
        qlayout.addView(tv);
        qlayout.addView(bt);
        qlayout.setTag("M");
        return qlayout;
    }

    /**
     * Creates camera question consisting of a button that launches image tagging and camera
     * after user takes image, the image gets processed in onActivityResult()
     *
     * @param questiontext text of camera question
     * @param context context of current activity
     * @return qlayout, LinearLayout camera question
     */
    public LinearLayout Camera(String questiontext, Context context){
        LinearLayout qlayout = new LinearLayout(context); // question LinearLayout
        qlayout.setOrientation(LinearLayout.VERTICAL);
        // creates text and button that make up camera question
        TextView tv = new TextView(context);
        tv.setTag("text");
        tv.setText(questiontext);
        Button bt = new Button(context);
        bt.setText(questiontext);
        bt.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT));
        final AlertDialog.Builder bdr = new AlertDialog.Builder(this);
        final Context context1 = context;

        // on click, prompts user for an image tag or no description, then opens camera
        // opens camera using dispatchTakePictureIntent()
        bt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final EditText input_tag = new EditText(context1);
                input_tag.setInputType(InputType.TYPE_CLASS_TEXT);
                input_tag.setHint("Enter a description (max 30 characters)");
                int maxLength = 30;
                input_tag.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
                bdr.setView(input_tag);
                // if user enters a tag, passes new tag to dispatchTakePictureIntent()
                bdr.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dispatchTakePictureIntent(input_tag.getText().toString());
                    }
                });
                // if the user opts for no tag, passes an empty string
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
        // adds text and button to camera question linear layout, returns linear layout
        qlayout.addView(tv);
        qlayout.addView(bt);
        qlayout.setTag("C");
        return qlayout;
    }

    String mCurrentPhotoPath = ""; // filepath to most recent image

    /**
     * Creates file to which image will be saved
     *
     * @param tag image tag (blank if no tag)
     * @return image, file into which the image being taken by the user will be saved
     * @throws IOException
     */
    private File createImageFile(String tag) throws IOException {
        // creates a file name from tag for image
        Image_Tags.add(tag);
        System.out.println(Image_Tags);
        tag = tag.replaceAll(" ", "_");
        System.out.println(tag);
        String imageFileName = timeStamp+"_t__"+tag+"__t_"+Integer.toString(Image_Tags.size());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpeg",         /* suffix */
                storageDir      /* directory */
        );

        // saves absolute file path to image file
        // adds image file and image tag to Images
        mCurrentPhotoPath = image.getAbsolutePath();
        Images.put(image.getAbsolutePath(), tag);
        // returns file
        return image;
    }

    /**
     * Creates file to save image in using createImageFile, launches photo taking activity
     *
     * @param tag tag for image to be taken
     */
    private void dispatchTakePictureIntent(String tag) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // creates the file where the photo will be stored
            File photoFile = null;
            try {
                photoFile = createImageFile(tag);
            } catch (IOException ex) {
                System.out.println("filename creation error");
            }
            // continues only if file was successfully created
            System.out.println(getFilesDir());
            // creates photoURI for storing image
            // launches photo taking activity using request image capture code
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.qmain.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            }
        }
    }

    /**
     * Processes result of map question or camera question
     * Saves location chosen by map question
     * Saves image tag written by and image taken by camera question, displays image
     *
     * @param requestCode integer indicating whether activity giving result is camera or map activity
     * @param resultCode integer indicating whether activity was successful
     * @param data data from activity
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // activity result from map question
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK)  {
                // saves location user chose on map
                Place place = PlacePicker.getPlace(this, data);
                // displays location in toast message after map activity is closed
                String toastMsg = String.format("Place: %s", place.getLatLng());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                System.out.println(toastMsg);
                System.out.println(PlacePicker.getLatLngBounds(data));
                // saves location
                LOCATION = toastMsg.substring(16);
                TextView update_loc = new TextView(this);
                update_loc.setText(LOCATION);
                if(map_question.getChildCount() > 2){
                    map_question.removeView(map_question.getChildAt(map_question.getChildCount()-1));
                }
                map_question.addView(update_loc);
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
        // activity result from camera question
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // opens image as bitmap and displays it in ImageView
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
            // displays image and tag as part of camera question
            mImageView.setBackgroundColor(Color.CYAN);
            TextView itag = new TextView(this);
            itag.setText(Image_Tags.get(Image_Tags.size()-1));
            camera_question.addView(itag);
            camera_question.addView(mImageView);
        }
    }

    /**
     * Writes current answers and returns them as a string (either in text or json format)
     * Optionally writes question/answers to file and json file
     *
     * @param qgs hash map of questions mapped to groups
     * @param toFile whether question/answers should be written to file
     * @param f FileOutputStream to write text question/answers to
     * @param incomplete if true, answers are being written for purposes of review
     * @param jf FileOutputStream to write json question/answers to
     * @param rj if True, function returns a json string
     * @return returns either question/answers as text or json or list of unanswered questions
     */
    public String writeAnswers(HashMap qgs, boolean toFile, FileOutputStream f, boolean incomplete, FileOutputStream jf, boolean rj) {
        String total = "";  // text with all questions and answers
        String unanswered = "";  // list of groups with unanswered required questions separated by ",, "
        uid = "";
        uid_set = false;
        timeStamp = ogTimeStamp;
        Boolean use_un = false;  // whether or not to return list of groups with unanswered questions
        HashMap<String,Object> json = new HashMap<>();  // hash map to be converted to a jsonstring to be written to file or returned
        HashMap<String,Object> groups = new HashMap<>();  // element of json hash map containing groups and their question/answers
        json.put("Groups", groups);
        if (toFile) {
            try {
                // writes metadata to json, text, and file
                // when not writing to file (review purposes only), metadata not needed
                json.put("Author", author);
                json.put("Project", getIntent().getStringExtra("project_name"));
                json.put("City", getIntent().getStringExtra("city"));
                json.put("Organization", getIntent().getStringExtra("org"));
                String line = "Author: " + author + "~~"; // "~~" serves as a delimiter between lines
                String project_line = "Project: " + getIntent().getStringExtra("project_name") + "~~";
                String city_line = "City: "+getIntent().getStringExtra("city")+"~~";
                String org_line = "Organization: "+getIntent().getStringExtra("org")+"~~";
                total += line;
                total += project_line;
                total += city_line;
                total += org_line;
                f.write(line.getBytes());
                f.write(project_line.getBytes());
                f.write(project_line.getBytes());
                f.write(project_line.getBytes());
            } catch (Exception e) {
                System.out.println("problem writing line");
            }
        }
        // set of group names
        Set keys = qgs.keySet();
        // iterates through group names
        int idp = 0;
        for(Object key:keys) {
            // adds section name to text
            String name = "Section: "+key + "\n";
            total += name;

            // iterates through list of questions in group
            List qs = (List) qgs.get(key);
            boolean in_list = false;  // keeps track of whether group has been added to unanswered

            HashMap<String, Object> questions = new HashMap<>();
            groups.put((String)key, questions);

            for (int i = 0; i < qs.size(); i++) {
                System.out.println(i);
                // gets question linear layout
                LinearLayout q = (LinearLayout) qs.get(i);
                // gets question text
                TextView text = (TextView) q.findViewWithTag("text");
                text.setTextColor(Color.GRAY);  // sets question text color to gray
                String question = (String) text.getText();  // question text
                String line = "";
                String tag = "";
                System.out.println(question);
                String qid;  // question number
                try {
                    qid = ((TextView) q.findViewWithTag("qid")).getText().toString();
                }catch(NullPointerException n){
                    qid = "";
                    n.printStackTrace();
                }
                Object qans = "";  // answer to question (could be string, list, etc.) to be mapped to question in hash map

                // if parent text or outer parent text exist, adds parent text(s) to question text
                String parent_text = "";
                try{
                    TextView pt = (TextView) q.findViewWithTag("parent text");
                    parent_text = (String) pt.getText();
                } catch(Exception e){
                    System.out.println("no parent");
                }

                String outer_parent_text = "";
                try{
                    TextView opt = (TextView) q.findViewWithTag("outer parent text");
                    outer_parent_text = (String) opt.getText();
                } catch(Exception e){
                    System.out.println("no outer parent");
                }

                question = parent_text+question + outer_parent_text;

                // identify question type by tag
                try {
                    tag = (String) q.getTag();
                } catch (Exception e) {
                    System.out.println("there's no tag?");
                }

                // skip parent questions
                if(tag.equals("P")){
                    continue;
                }

                // based on question type, completes question text line with answer in appropriate fashion
                // if for submission purposes (incomplete = False), returns "" if any required (*) questions don't have answers
                // otherwise adds unanswered required questions to unanswered string
                if(tag.equals("T") || tag.equals("N")) {
                    EditText editText = (EditText) q.findViewWithTag("answer");
                    if (editText.getText().toString().equals("") && (question.endsWith("*") || question.contains("* - ")) && !incomplete) {
                        // returns "" for incomplete required questions if writing answers for submission purposes
                        System.out.println("oops");
                        return "";
                    } else {
                        // if group contains an unanswered required question
                        // adds group to unanswered if hasn't been done already
                        // sets question text color to red
                        if (editText.getText().toString().equals("") && (question.endsWith("*") || question.contains("* - "))) {
                            if(! in_list){
                                unanswered += key + ",, ";
                                in_list = true;
                            }
                            use_un = true;
                            text.setTextColor(Color.RED);
                        }
                        // adds question answer to line and sets qans as answer
                        line = question + ": " + editText.getText();
                        qans = line.substring(line.indexOf(": ") + 2);
                    }
                } else if (tag.equals("S")){
                    // gets total answer of sum question
                    TextView answer_total = (TextView) q.findViewWithTag("answer");
                    String at = answer_total.getText().toString();

                    // gets list of sum factors (LinearLayouts with text and EditText)
                    ArrayList<View> factors = new ArrayList<>();
                    for(int v = 0; v<q.getChildCount(); v++){
                        if(q.getChildAt(v).getTag().equals("factor")){
                            factors.add(q.getChildAt(v));
                        }
                    }

                    // returns "" for incomplete required questions if writing answers for submission purposes
                    if((question.endsWith("*") || question.contains("* - ")) && !incomplete && at.equals("Total: ")){
                        System.out.println("oops");
                        return "";
                    }else if((question.endsWith("*") || question.contains("* - "))&& at.equals("Total: ")){
                        // adds group containing question to unanswered if group has not already been added
                        // sets question text color to red
                        if(! in_list){
                            unanswered += key + ",, ";
                            in_list = true;
                        }
                        use_un = true;
                        text.setTextColor(Color.RED);
                    } else{
                        try {
                            line = question + ": ";
                            HashMap<String, Integer> fqas = new HashMap<>();

                            // iterates through factors, adds factor text and answer to line (for text) and hash map (for json)
                            for(View factor : factors){
                                TextView ftext = (TextView) factor.findViewWithTag("ftext");  // factor text
                                EditText fans = (EditText) factor.findViewWithTag("fanswer");
                                String fa;  // factor answer
                                if(fans.getText().toString().equals("")){
                                    fa = "0 ";
                                }else{
                                    fa = fans.getText().toString() + " ";
                                }
                                line += ftext.getText()+": "+fa;
                                fqas.put(ftext.getText().toString(), Integer.parseInt(fa.substring(0,fa.length()-1)));
                            }
                            qans = fqas;

                        }catch(Exception e){
                            e.printStackTrace();
                            line = question + ": ";
                        }
                    }
                } else if (tag.equals("SC")) {
                    line = question + ": ";
                    RadioGroup rg = (RadioGroup) q.findViewWithTag("choices");  // list of question choice radio buttons
                    int id = rg.getCheckedRadioButtonId();  // id of checked radio button, -1 if none checked
                    // returns "" for unanswered question if writing answers for submission purposes
                    if (id == -1 && !incomplete && (question.endsWith("*") || question.contains("* - "))) {
                        return "";
                    } else {
                        RadioButton rb = (RadioButton) rg.getChildAt(id);
                        try {
                            if (id == -1  && (question.endsWith("*") || question.contains("* - "))){
                                // if group contains an unanswered required question
                                // adds group to unanswered if hasn't been done already
                                // sets question text color to red
                                if(! in_list){
                                    unanswered += key + ",, ";
                                    in_list = true;
                                }
                                use_un = true;
                                text.setTextColor(Color.RED);
                                line = line.toUpperCase();
                                continue;
                            } else if(id == -1){
                                // if not required and no answer, qans is a blank string
                                qans = "";
                            } else {
                                // adds button text to line (for text) and sets button id as qans (for json)
                                line += rb.getText();
                                qans = rb.getTag();
                            }
                        } catch (Exception e) {
                            System.out.println("something went wrong");
                            System.out.println(question);
                        }
                    }
                } else if (tag.equals("MC")) {
                    qans = new ArrayList<String>();  // answer is list of choices selected
                    line = question + ": ";
                    // iterates through all child views, checks for choice checkboxes
                    for (int j = 0; j < q.getChildCount(); j++) {
                        String ctag = (String) q.getChildAt(j).getTag();
                        if (ctag.contains("choice")) {
                            CheckBox cb = (CheckBox) q.getChildAt(j);
                            if (cb.isChecked()) {
                                // if checkbox is checked, adds checkbox text to line, code to list of choices qans
                                String tt = ctag.substring(ctag.indexOf("~~")+2);
                                line += cb.getText() + ", ";
                                ((ArrayList<String>)qans).add(tt);
                            }
                        }
                    }
                    // if more than one choice, remove ending ", " from line
                    if (line.length() > 20) {
                        line = line.substring(0, line.length() - 2);
                    }
                } else if (tag.equals("M")) {
                    // adds location to line and sets location as qans
                    line = question + ": " + LOCATION;
                    qans = LOCATION;
                } else if (tag.equals("C")) {
                    String imtags = "";  // list of image tags
                    for(int t = 0; t<Image_Tags.size()-1; t++){
                        imtags += Image_Tags.get(t) + ", ";
                    }
                    if(Image_Tags.size() > 0) {
                        imtags += Image_Tags.get(Image_Tags.size() - 1);
                    }
                    line = question + ": ";
                    line += imtags;
                    qans = Images;  // hash map of image tags to their image filenames
                }

                // jquestion will be key, qans will be value in hash map of question/answers for group
                String jquestion = question;
                if(question.endsWith("*")){
                    jquestion = question.substring(0,question.length()-1);
                }

                // if question is already in hash map for group and has multiple answers
                // if answer type is already a list of answers, appends new answer
                // if not, creates list of answers, appends old answer and new answer
                if(questions.containsKey(jquestion)){
                    if(questions.get(jquestion) instanceof ArrayList){
                        ((ArrayList<Object>)questions.get(jquestion)).add(qans);
                    }else{
                        String first_elem = (String)questions.get(jquestion);
                        questions.remove(jquestion);
                        ArrayList<Object> elems = new ArrayList<>();
                        elems.add(first_elem);
                        elems.add(qans);
                        questions.put(jquestion, elems);
                    }
                }else {
                    // puts question, answer in hash map for group
                    questions.put(jquestion, qans);
                }

                // if question is the next sequential Identifier question whose answer should be added to uid
                // adds answer to uid
                if(Identifiers.contains(qid) && !uid_set && !qans.equals("") && !qans.equals(" ")){
                    if(Identifiers.get(idp).equals(qid)){
                        String uids = ((String) qans).replace(" ", "_");
                        uids = uids.replace("/", "_");
                        uid += uids + "_";
                        System.out.println(Identifiers.get(idp) + qans);
                        idp += 1;  // keeps track of which identifier needs to be added next
                    }
                }
                System.out.println(i);


                if (toFile) {
                    try {
                        // writes answers to file
                        line = line + "~~";  // "~~" delimiter for line
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
            return "!<!," + unanswered;  // "!<!," denotes to update_answers() that there are unanswered questions
        }

        if(rj){
            return json.toString();  // returns json string
        }
        if(Identifiers.size() == idp){
            timeStamp = uid+"_"+timeStamp;
            uid_set = true;
        }

        if(jf != null) {
            // if a FileOutputStream for json is given
            // convert hash map of question/answers to groups and metadata to jsonobject
            // convert jsonobject to string, write string to file
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
        String filename;

        // calls writeAnswers first to check for unanswered required questions
        String answers = writeAnswers(Groups, false, null, false, null, false);
        if (answers.equals("")) {
            AlertDialog.Builder bdr = builder;
            bdr.setMessage("Answer all required questions before submitting");
            AlertDialog dialog = bdr.create();
            dialog.show();
            return "";
        } else{
            // if all required questions answered, writes questions and answers to file
            filename = timeStamp+"hc*"+project+".txt";
            String jsonfilename = timeStamp + "hc*"+project+".json";
            FileOutputStream fos;
            FileOutputStream jfos;
            // opens file
            try{
                fos = openFileOutput(filename, Context.MODE_PRIVATE);
                jfos = openFileOutput(jsonfilename, Context.MODE_PRIVATE);
            }catch(Exception e){
                e.printStackTrace();
                System.out.println(filename);
                System.out.println(jsonfilename);
                return "";
            }
            System.out.println(writeAnswers(Groups, true, fos, false, jfos, false));
            try {
                fos.close();
                jfos.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        // goes back to main page of project
        Intent intent = new Intent(this, Home.class);
        intent.putExtra("project", project);
        intent.putExtra("city", getIntent().getStringExtra("city"));
        intent.putExtra("org", getIntent().getStringExtra("org"));
        intent.putExtra("action_bar", getIntent().getStringExtra("project_name"));
        intent.putExtra("author", author);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        // resets values for new questionnaire
        LOCATION = "";
        mImageView = null;
        timeStamp = "";

        PVQ.this.finish();
        return filename;
    }

    // updates ans TextView on submit page with current answers
    public void update_answers(){
        answers = writeAnswers(Groups, false, null, true, null, false);
        if(answers.substring(0,4).equals("!<!,")){
            // if unanswered required questions present, set ans to message about required questions
            String req_msg = "The following sections have required questions that need to be answered:";
            ans.setText(req_msg);
            String scts = answers.substring(4);  // list of groups with unanswered required questions separated by ",, "
            req_buttons.setBackgroundColor(Color.MAGENTA);
            if(req_buttons.getChildCount() == 0) {
                // extract a group name from scts until there are none left
                while (scts.length() > 3) {
                    // make button that takes user to group
                    String sct = scts.substring(0, scts.indexOf(",, "));
                    scts = scts.substring(scts.indexOf(",, ") + 3);
                    Button br = new Button(this);
                    br.setText(sct);
                    br.setOnClickListener(new StringOnClickListener(sct) {
                        public void onClick(View v) {
                            vp.setCurrentItem(pages.indexOf(s));  // sets view pager page to group page
                        }
                    });
                    req_buttons.addView(br);  // add button to req_buttons
                }
            }
        }else{
            // display answers
            ans.setText(answers);
        }
    }

    // hides soft keyboard
    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    // calls hideSoftKeyboard for non-EditText views when they are touched
    public Activity a = this;
    public void setupUI(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(a, v);
                    return false;
                }
            });
        }
    }

    public void exitSurvey() {
        // prompts user whether they are sure if they'd like to exit questionnaire
        // exits if yes, returns user to project home page
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

    /**
     * If back pressed, calls exitSurvey()
     */
    @Override
    public void onBackPressed(){
        exitSurvey();
    }

    /**
     * If user selects back/home button in support action bar, calls exitSurvey()
     *
     * @param item MenuItem selected
     * @return boolean whether item selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                exitSurvey();
                break;
        }
        return true;
    }
}

/**
 * Class used to create a PagerAdapter to work with ViewPager
 */
class MainPagerAdapter extends PagerAdapter
{
    // This holds all the currently displayable views, in order from left to right.
    private ArrayList<View> views = new ArrayList<>();

    void addView (View v, int position)
    {
        views.add (position, v);
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

/**
 * StringOnClickListener has the same functionality as View.OnClickListener with the added ability
 * of objects to access string parameters passed during their initializations
 */
class StringOnClickListener implements View.OnClickListener
{
    String s;
    // can take string as parameter to be accessed in onClick()
    StringOnClickListener(String str) {
        this.s = str;
    }

    @Override
    public void onClick(View v)
    {
    }

}

/**
 * NumWatcher helps keep track of how many sets of repeatable questions are displayed for repeatable chunks
 */
class NumWatcher implements TextWatcher {
    private LinearLayout view1;  // LinearLayout on which all sets of questions are displayed
    private NodeList nodes;  // NodeList of questions
    private Context context;  // context of activity in which questions are being placed
    private List<LinearLayout> list2;  // list that needs to be updated with questions
    private int max;  // static maximum number of chunks to be displayed
    private HashMap<String,LinearLayout> NumQuestions;  // hash map of questions mapped to their numbers
    private HashMap ds;  // hash map mapping number of currently positively answered choices a question is dependent on to the question's id
    private String qlims;  // string of question number(s) whose answers limit the possible number of repeatable chunks
    private AlertDialog dialog;
    NumWatcher(int max, LinearLayout ll, NodeList nodes, Context context, List<LinearLayout> list2, HashMap<String,LinearLayout> nq,
               HashMap deps, String qlims){
        this.view1 = ll;
        this.nodes = nodes;
        this.context = context;
        this.list2 = list2;
        this.max = max;
        this.NumQuestions = nq;
        this.ds = deps;
        this.qlims = qlims;
        // builds error dialog in case one needs to be shown
        AlertDialog.Builder newbuilder = new AlertDialog.Builder(context);
        String msg = "Value too large, contradicts answer to question "+qlims;
        newbuilder.setMessage(msg);
        this.dialog = newbuilder.create();
    }

    /**
     * Deals with changes with input in EditText controlling number of sets of repeatable questions
     *
     * @param s Editable currently inputted in EditText
     */
    public void afterTextChanged(Editable s) {
        String value = s.toString();
        // removes all repeatable question set views from view1 and all repeatable questions from list2 for a fresh start
        for(int i = 0; i < view1.getChildCount(); i++){
            System.out.println("group");
            for(int j = 0; j < ((LinearLayout)view1.getChildAt(i)).getChildCount(); j++){
                System.out.println("q");
                System.out.println(list2.remove(((LinearLayout)view1.getChildAt(i)).getChildAt(j)));
                LinearLayout chunk = (LinearLayout)((LinearLayout)view1.getChildAt(i)).getChildAt(j);
                for(int k = 0; k < chunk.getChildCount(); k++){
                    try {
                        System.out.println(list2.remove(chunk.getChildAt(k)));
                    }catch(Exception e){
                        System.out.println(chunk.getChildAt(k));
                    }
                }
            }
        }
        view1.removeAllViews();
        if(value.equals("")){
            // no number entered equates to 0, function can exit without adding questions to view
            return;
        }
        int times = Integer.parseInt(value);  // number entered in EditText
        int lim = Questionnaire.NumLimit(qlims, NumQuestions, null);  // current answer at limiting question(s)
        if(times > max){
            // exits if times is greater than static limit
            return;
        }else if(times > lim){
            // shows error message if times is greater than user-set limit
            dialog.show();
            return;
        }
        // creates new linear layout holding repeatable set of questions for specified number of times
        for(int i = 0;i<times;i++){
            LinearLayout qchunk = new LinearLayout(context);
            qchunk.setOrientation(LinearLayout.VERTICAL);
            // iterates through question nodes, makes questions for each
            for (int y = 0; y < nodes.getLength(); y++) {
                Node question = nodes.item(y);
                if (question.getNodeType() == Node.ELEMENT_NODE) {
                    // uses Questionnaire.build_question() to create each new question
                    // function adds new question to list2 and qchunk
                    LinearLayout qu = Questionnaire.build_question(question, list2, qchunk, context, NumQuestions,
                            ds);
                    TextView pt = new TextView(context);
                    String chunk_num = " - "+Integer.toString(i+1);  // specifies which set question is part of
                    pt.setText(chunk_num);
                    pt.setTag("outer parent text");
                    pt.setVisibility(View.GONE);
                    qu.addView(pt);  // adds invisible TextView specifying chunk number of question
                }
            }
            view1.addView(qchunk, view1.getChildCount() - 1);  // adds qchunk to view holding all sets of repeatable questions
        }
    }
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
}

