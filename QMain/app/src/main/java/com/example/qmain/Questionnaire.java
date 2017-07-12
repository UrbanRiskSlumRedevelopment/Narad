package com.example.qmain;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
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
import java.util.ArrayList;
import java.util.List;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.LinearLayout;
import android.app.AlertDialog;
import android.widget.CheckBox;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar.LayoutParams;

/**
 * Builds questions
 */
public class Questionnaire extends AppCompatActivity {
    public static AlertDialog.Builder builder = null;

    /**
     * Builds new question LinearLayout given information and updates passed data structures with question
     *
     * @param nNode XML node containing question and its parts
     * @param list2 list that needs to be updated with question (question must be added to list)
     * @param layout1 layout to add question to
     * @param context context of current activity
     * @param qns hash map of questions mapped to their numbers to be updated with new question being built
     * @param ds hash map mapping number of choices a question is dependent on that are positively answered to the question's id
     * @return new question LinearLayout containing all component views of question
     */
    public static LinearLayout build_question(Node nNode, List<LinearLayout> list2, LinearLayout layout1, Context context,
                                              HashMap<String, LinearLayout> qns, HashMap<String, Integer> ds){
        LinearLayout q = null;
        Element eElement = (Element) nNode;

        String text;
        String type = eElement.getElementsByTagName("qtype").item(0).getTextContent();  // type of question
        String hint;
        String parent;
        TextView reqQuestionTextView = null;  // TextView for conditionally required questions with version of question text as required
        String req;
        String qid = eElement.getElementsByTagName("q").item(0).getTextContent(); // question number
        // see if question is required
        try{
            req = eElement.getElementsByTagName("req").item(0).getTextContent();
        }catch(Exception e){
            req = "F";
        }
        // question hint
        try{
            hint = eElement.getElementsByTagName("qhint").item(0).getTextContent();
        }catch(Exception e){
            hint = "";
        }

        if(req.equals("T")){
            // adds "*" to required question's question text
            text = eElement.getElementsByTagName("qtext").item(0).getTextContent()+"*";
        }
        else if(req.equals("C")){
            // sets up required question version of question text
            text = eElement.getElementsByTagName("qtext").item(0).getTextContent();
            String req_text = eElement.getElementsByTagName("qtext").item(0).getTextContent()+"*";
            reqQuestionTextView = new TextView(context);
            reqQuestionTextView.setText(req_text);
            reqQuestionTextView.setVisibility(View.GONE);
        }else{
            // reads and saves question text unchanged from XML node
            text = eElement.getElementsByTagName("qtext").item(0).getTextContent();
        }
        try{
            // check for parent, save if it exists
            parent = eElement.getElementsByTagName("parent").item(0).getTextContent();
        }catch(Exception e){
            parent = null;
        }
        String qlimit;
        try{
            // check for limiting questions, save if exists
            qlimit = eElement.getElementsByTagName("qlimit").item(0).getTextContent();
        }catch(Exception e){
            qlimit = "";
        }
        if (type.equals("T")) {
            // if text entry, build question with TextQ()
            q = TextQ(text, hint, context, parent, qns);
        } else if (type.equals("N")) {
            // if numerical entry, build question with NumQ()
            q = NumQ(text, hint, context, parent, qns, qlimit);
        } else if (type.equals("SC")) {
            // if single choice question, prepare list of choices and build question with SingleChoice()
            List<String> c = new ArrayList<>();  // array list of choices
            NodeList choices = eElement.getElementsByTagName("choice");  // all choice nodes
            HashMap<String, ArrayList<String>> dependencies = new HashMap<>();
            // iterate through choice nodes
            for (int i = 0; i < choices.getLength(); i++) {
                Node choice = choices.item(i);
                Element e = (Element) choice;
                String x = e.getElementsByTagName("ctext").item(0).getTextContent();  // choice text
                String tag = e.getElementsByTagName("ccode").item(0).getTextContent();  // choice code
                x = x+"~~"+tag;
                c.add(x);  // adds choice text and code to list of choices
                try {
                    // keeps track of choice's dependents
                    ArrayList<String> dps = new ArrayList<>();
                    NodeList dependents = e.getElementsByTagName("dependents");
                    // iterates through choice's dependents, if any
                    for(int j = 0; j<dependents.getLength(); j++){
                        String dep_string = dependents.item(j).getTextContent();
                        dps.add(dep_string);
                    }
                    // maps choice's dependents to choice
                    dependencies.put(x, dps);
                }catch(Exception ex){
                    //System.out.println("no dependencies");
                }
            }
            builder = PVQ.builder;  // dialog builder
            // build question with SingleChoice()
            q = SingleChoice(text, c, hint, context, builder, qns, ds, dependencies, parent);
        } else if (type.equals("MC")) {
            // if multiple choice question, prepare list of choices and build question with MultipleChoice()
            List<String> c = new ArrayList<>();  // array list of choices
            NodeList choices = eElement.getElementsByTagName("choice");
            HashMap<String, ArrayList<String>> dependencies = new HashMap<>();
            // iterates through choice nodes
            for (int i = 0; i < choices.getLength(); i++) {
                Node choice = choices.item(i);
                Element e = (Element) choice;
                String x = e.getElementsByTagName("ctext").item(0).getTextContent();  // choice text
                String code = e.getElementsByTagName("ccode").item(0).getTextContent();  // choice code
                x = x + "~~"+code;
                c.add(x);  // adds choice text and code to list of choices
                try {
                    ArrayList<String> dps = new ArrayList<>();  // list of choice's dependents
                    NodeList dependents = e.getElementsByTagName("dependents");
                    // iterates through dependents, if any
                    for(int j = 0; j<dependents.getLength(); j++){
                        String dep_string = dependents.item(j).getTextContent();
                        dps.add(dep_string);
                    }
                    // maps choice's dependents to choice
                    dependencies.put(x, dps);
                }catch(Exception ex){
                    //System.out.println("no dependencies");
                }
            }
            builder = PVQ.builder;  // dialog builder
            // build question with MultipleChoice()
            q = MultipleChoice(text, c, hint, context, builder, qns, ds, dependencies, parent);
        } else if (type.equals("M")){
            // method doesn't build map questions
            return null;
        } else if (type.equals("C")){
            // method doesn't build camera questions
            return null;
        } else if (type.equals("S")){
            // if sum question, builds question using SumQ() given list of factor nodes
            NodeList factors = eElement.getElementsByTagName("factor");
            q = SumQ(text,hint,context,factors, parent, qns, qlimit);
        } else if (type.equals("P")){
            // if parent question, builds question using ParentQ()
            q = ParentQ(text, hint, context, parent, qns);
        }

        // if question is conditionally required, adds TextView with required question text
        // so it may be shown if question becomes required
        if(reqQuestionTextView != null && q != null){
            q.addView(reqQuestionTextView);
            reqQuestionTextView.setTag("required");
        }

        // determines whether question is initially invisible or not
        String inv;
        try{
            inv = eElement.getElementsByTagName("inv").item(0).getTextContent();
        }catch(Exception e){
            inv = "F";
        }
        if(inv.equals("T") && q != null){
            q.setVisibility(View.GONE);
        }

        // adds question to LinearLayout specified in parameters
        if(layout1 != null){
            try {
                layout1.addView(q);
            }catch(IllegalStateException e){
                //System.out.println("child question");
            }
        }
        list2.add(q);

        if(qns != null){
            qns.put(qid, q);  // maps question to its number in hash map specified in parameters
            ds.put(qid, 0);  // number of questions question qid depends on currently answered positively (0 initially for all)
        }

        // add invisible TextView to question containing its qid for future access
        TextView qt = new TextView(context);
        qt.setText(qid);
        qt.setVisibility(View.GONE);
        qt.setTag("qid");
        if(q!=null) {
            q.addView(qt);
        }

        return q;
    }

    // Methods for each specific question type that make question linear layout for specific question type

    /**
     * Builds a text entry question with question text and an EditText for answer entry
     *
     * @param questiontext text of question
     * @param hint hint or prompt to go in the answer entry box
     * @param context context of current activity
     * @param parent number (in string format) of parent question, if any
     * @param qns hash map mapping question to question number
     * @return LinearLayout of text entry question with text and answer entry box
     */
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

        // adds question to parent question LinearLayout, if one exists
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

    /**
     * Builds a number entry question with question text and an EditText for answer entry
     *
     * @param questiontext text of question
     * @param hint hint or prompt to go in the answer entry box
     * @param context context of current activity
     * @param parent number (in string format) of parent question, if any
     * @param qns hash map mapping question to question number
     * @param limit number (in string format) of question whose answer is an upper limit
     * @return LinearLayout of number entry question with text and answer entry box
     */
    public static LinearLayout NumQ(String questiontext, String hint, Context context,
                                    String parent, HashMap<String, LinearLayout> qns, String limit){
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

        // sets up NumQWatcher that verifies answer to this question is less than or equal to
        // answer of limiting question, if one exists
        if(!limit.equals("")) {
            edittext.addTextChangedListener(new NumQWatcher(edittext, limit, qns, context));
        }

        // sets up linear layout for question, adds question text and answer text box
        LinearLayout qlayout = new LinearLayout(context);
        qlayout.setOrientation(LinearLayout.VERTICAL);
        qlayout.addView(text);
        qlayout.addView(edittext);
        qlayout.setTag("N");

        // adds question to parent question LinearLayout, if one exists
        if(parent != null){
            LinearLayout pl = qns.get(parent);
            pl.addView(qlayout);
            TextView parent_text = new TextView(context);
            parent_text.setText(((TextView)pl.findViewWithTag("parent text")).getText());
            parent_text.setTag("parent text");
            parent_text.setVisibility(View.GONE);
            qlayout.addView(parent_text);
        }

        return qlayout;
    }

    /**
     * Builds a sum question with question text, text and answer box for each factor, and text displaying total
     *
     * @param questiontext text of question
     * @param hint hint or prompt to go in the answer entry box
     * @param context context of current activity
     * @param factors list of factors as XML nodes
     * @param parent number (in string format) of parent question, if any
     * @param qns hash map mapping question to question number
     * @param qlimit number (in string format) of question whose answer is an upper limit
     * @return LinearLayout of sum question with text and sum factor texts and answer entry boxes
     */
    public static LinearLayout SumQ(String questiontext, final String hint,Context context,NodeList factors,
                                    String parent, HashMap<String, LinearLayout> qns, String qlimit){
        // sets up question text
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);
        text.setTag("text");
        text.setPadding(0,0,0,5);

        // sets up linear layout for question
        LinearLayout qlayout = new LinearLayout(context);
        qlayout.setOrientation(LinearLayout.VERTICAL);

        // sets up info button with hint if hint provided
        if(!hint.equals("")){
            Button bt = new Button(context);
            // info button layout
            Drawable help = ContextCompat.getDrawable(context, R.drawable.help_circle_outline);
            bt.setBackground(help);
            bt.setLayoutParams(new LinearLayout.LayoutParams(50, 50));

            // creates dialog with hint to be shown when info button clicked
            final AlertDialog.Builder bdr = builder;
            bt.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    bdr.setMessage(hint);
                    AlertDialog dialog = bdr.create();
                    dialog.show();
                }
            });

            // formats info button and text to be aligned, adds to layout
            LinearLayout qh = new LinearLayout(context);
            qh.setOrientation(LinearLayout.HORIZONTAL);
            qh.addView(text);
            qh.addView(bt);
            qlayout.addView(qh);
        }else{
            // if no info button, just adds text to layout
            qlayout.addView(text);
        }

        // sets up boxes for answer text entry (numerical)
        TextView tv = new TextView(context);
        List<EditText> to_sum = new ArrayList<>();  // list of EditTexts whose inputs should be summed to get the question total
        // iterates through factor XML nodes
        for(int i = 0;i<factors.getLength();i++){
            Element factor = (Element) factors.item(i);
            String ftext = factor.getElementsByTagName("ftext").item(0).getTextContent();  // text of factor
            EditText et = new EditText(context);  // answer entry box for factor
            et.setTextSize(15);
            et.setHint("            ");
            // sets SumWatcher for factor answer entry box
            if(qlimit.equals("")){
                // if no other question serves as a limit
                et.addTextChangedListener(new SumWatcher(et, tv, to_sum, "", qns, null, context));
            }else {
                // if another question serves as a limit
                et.addTextChangedListener(new SumWatcher(et, tv, to_sum, qlimit, qns, ftext+" ", context));
            }
            et.setInputType(2);
            to_sum.add(et);

            // formatting factor text and answer box to be in a horizontal bar across from each other
            LinearLayout hbar = new LinearLayout(context);
            hbar.setOrientation(LinearLayout.HORIZONTAL);
            TextView ft = new TextView(context);
            ft.setTextSize(15);
            String ftext_space = ftext+" ";
            ft.setText(ftext_space);
            ft.setTag("ftext");
            et.setTag("fanswer");

            hbar.addView(ft);
            hbar.addView(et);
            hbar.setTag("factor");
            qlayout.addView(hbar);
        }

        // TextView for displaying question total
        qlayout.addView(tv);
        tv.setTag("answer");
        qlayout.setTag("S");

        // adds question to parent question LinearLayout, if one exists
        if(parent != null){
            LinearLayout pl = qns.get(parent);
            pl.addView(qlayout);
            TextView parent_text = new TextView(context);
            parent_text.setText(((TextView)pl.findViewWithTag("parent text")).getText());
            parent_text.setTag("parent text");
            parent_text.setVisibility(View.GONE);
            qlayout.addView(parent_text);
        }

        return qlayout;
    }

    /**
     * Builds a single choice question with question text and radio button choices
     *
     * @param questiontext text of question
     * @param choices list of choices, each a string of choice text and tag separated by a delimiter
     * @param hint hint or prompt to be shown when info button is clicked
     * @param context context of current activity
     * @param builder dialog builder used to display dialog when info button is clicked
     * @param qns hash map mapping question to question number
     * @param ds hash map mapping number of choices a question is dependent on that are positively answered to the question's id
     * @param lds hash map mapping list of dependent question numbers to choice texts
     * @param parent number (in string format) of parent question, if any
     * @return LinearLayout of single choice question with text and choices
     */
    public static LinearLayout SingleChoice(String questiontext, List choices,
                                            final String hint, Context context, AlertDialog.Builder builder, HashMap qns,
                                            HashMap<String, Integer> ds, HashMap<String, ArrayList<String>> lds, String parent){
        // sets up question text
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);

        // creates group of radio buttons, each button being a choice from choices
        // each button tagged with choice code
        RadioGroup rg = new RadioGroup(context);
        for (int i=0; i<choices.size(); i++) {
            RadioButton rb = new RadioButton(rg.getContext());
            String btext = choices.get(i).toString();
            rb.setId(i);
            rb.setText(btext.substring(0, btext.indexOf("~~")));  // choice text in string before "~~"
            rg.addView(rb);
            rb.setTag(btext.substring(btext.indexOf("~~")+2));  // choice code in string after "~~"
            ArrayList<String> options = lds.get(btext);  // list of questions (by qid) to set to visible if choice checked
            if(options == null){
                options = new ArrayList<>();
            }
            rb.setOnCheckedChangeListener(new onCheckedChangedB(qns, ds, options));  // sets dependent questions visible when rb checked
        }
        text.setTag("text");
        text.setPadding(0,0,0,5);
        rg.setTag("choices");

        // sets up info button, builds dialog with hint when clicked on
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
        // adds info button if hint exists
        if(!hint.equals("")){
            qh.addView(bt);
        }
        qlayout.addView(qh);
        qlayout.addView(rg);
        bt.setTag("button");
        qlayout.setTag("SC");

        // adds question to parent question LinearLayout, if one exists
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

    /**
     * Builds a multiple choice question with question text and checkbox choices
     *
     * @param questiontext text of question
     * @param choices list of choices, each a string of choice text and tag separated by a delimiter
     * @param hint hint or prompt to be shown when info button is clicked
     * @param context context of current activity
     * @param builder dialog builder used to display dialog when info button is clicked
     * @param qnums hash map mapping question to question number
     * @param ds hash map mapping number of currently positive answers question is dependent on to the question's id
     * @param localds hash map mapping list of dependent question numbers to choice texts
     * @param parent number (in string format) of parent question, if any
     * @return LinearLayout of multiple choice question with text and choices
     */
    public static LinearLayout MultipleChoice(String questiontext, List choices,
                                              final String hint, Context context, AlertDialog.Builder builder, HashMap qnums,
                                              HashMap<String, Integer> ds, HashMap<String, ArrayList<String>> localds, String parent){
        // sets up question text
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);
        text.setPadding(0,0,0,5);

        // sets up question LinearLayout
        // adds horizontal LinearLayout where text and possibly info button will be added
        LinearLayout qlayout = new LinearLayout(context);
        qlayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout qh = new LinearLayout(context);
        qh.setOrientation(LinearLayout.HORIZONTAL);
        qlayout.addView(qh);

        // for each question choice, adds checkbox to question linear layout
        for (int i=0; i<choices.size(); i++) {
            CheckBox cb = new CheckBox(context);
            String ctext = choices.get(i).toString();
            cb.setId(i);
            cb.setText(ctext.substring(0, ctext.indexOf("~~")));  // choice text in string before "~~"
            String tag = "choice" + ctext.substring(ctext.indexOf("~~"));  // choice code in string after "~~", add code to tag
            cb.setTag(tag);
            cb.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            ArrayList<String> options = localds.get(ctext);  // list of questions (by qid) to set to visible if choice checked
            if(options == null){
                options = new ArrayList<>();
            }
            cb.setOnCheckedChangeListener(new onCheckedChangedB(qnums, ds, options));  // sets dependent questions visible when cb checked
            qlayout.addView(cb);
        }

        text.setTag("text");

        // sets up info button, builds dialog with hint when clicked on
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

        // adds info button if hint exists
        qh.addView(text);
        if(!hint.equals("")){
            qh.addView(bt);
        }
        qh.setTag("qh");

        qlayout.setTag("MC");

        // adds question to parent question LinearLayout, if one exists
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

    /**
     * Builds a parent text LinearLayout with parent question text
     *
     * @param questiontext text of question
     * @param hint hint or prompt to be shown when info button is clicked
     * @param context context of current activity
     * @param parent number (in string format) of parent question, if any
     * @param qns hash map mapping question to question number
     * @return LinearLayout with parent question text that child questions can be added to
     */
    public static LinearLayout ParentQ(String questiontext, final String hint, Context context,
                                       String parent, HashMap qns){
        // sets up question LinearLayout
        LinearLayout qlayout = new LinearLayout(context);
        qlayout.setOrientation(LinearLayout.VERTICAL);

        // sets up question text
        TextView text = new TextView(context);
        text.setTextSize(20);
        text.setText(questiontext);
        text.setTag("text");
        text.setPadding(0,0,0,5);

        qlayout.setTag("P");

        // formats question text as parent text for use in writing questions/answers
        // adds as invisible TextView to layout so it can be accessed by other functions
        TextView parent_text = new TextView(context);
        String pp = questiontext+" - ";
        parent_text.setText(pp);
        parent_text.setTag("parent text");
        parent_text.setVisibility(View.GONE);
        qlayout.addView(parent_text);

        // adds question to parent question LinearLayout, if one exists
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
            // add text and button to layout in horizontal bar
            LinearLayout qh = new LinearLayout(context);
            qh.setOrientation(LinearLayout.HORIZONTAL);
            qh.addView(text);
            qh.addView(bt);
            qlayout.addView(qh);
        }else{
            // if no hint, just add text to layout
            qlayout.addView(text);
        }

        return qlayout;
    }

    /**
     * Returns Integer total of answer(s) from given limiting question(s)
     *
     * @param qnum number(s) (in string format) of limiting question(s)
     * @param numqs hash map mapping question to question number
     * @param factor text of factor whose limit is being queried (null if none)
     * @return current Integer answer or total answer of limiting question(s)
     */
    public static int NumLimit(String qnum, HashMap<String, LinearLayout> numqs, String factor){
        qnum = qnum.replace(" ","");
        // if qnum contains multiple question numbers split by ",", lists them and iterates through them
        if(qnum.contains(",")){
            String[] qnums = qnum.split(",");
            int total = 0;  // total of limiting questions' answers
            for(String qn : qnums){
                try{
                    LinearLayout qll = numqs.get(qn);  // limiting question
                    total += NumAnswer(qll, factor);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            return total;
        }else{
            LinearLayout qll = numqs.get(qnum);  // limiting question
            return NumAnswer(qll, factor);
        }
    }

    /**
     * Returns Integer answer of given question/question factor
     *
     * @param qll LinearLayout of question
     * @param factor String factor text of question factor (null if none)
     * @return current Integer answer of given question or factor
     */
    public static int NumAnswer(LinearLayout qll, String factor){
        if(factor!=null){
            // looks for matching factor if factor is specified
            // iterates through all child views of question LinearLayout
            for(int v = 0; v<qll.getChildCount(); v++){
                // checks for factors by checking tags (all factors are tagged "factor")
                if(qll.getChildAt(v).getTag().equals("factor")){
                    try{
                        // checks whether factor is factor specified by comparing factor texts
                        TextView ftext = (TextView) qll.getChildAt(v).findViewWithTag("ftext");
                        if(ftext.getText().toString().equals(factor)){
                            // returns Integer version of current factor answer
                            EditText et = (EditText) qll.getChildAt(v).findViewWithTag("fanswer");
                            if(et.getText().toString().equals("") || et.getText().toString().equals(" ")){
                                return 0;
                            }else{
                                System.out.println(et.getText().toString());
                            }
                            return Integer.parseInt(et.getText().toString());
                        }
                    }catch(Exception e1){
                        e1.printStackTrace();
                    }
                }
            }
        }
        // if a factor is specified by not found in the limiting question, function moves on
        // returns limit on total question answer as limit on factor
        try{
            // tries finding answer in a TextView (answers are in TextViews for sum questions)
            TextView answ = (TextView) qll.findViewWithTag("answer");
            // sum questions answers are in format "Total: "+answer
            String s = answ.getText().toString();
            s = s.substring(s.indexOf(" ")+1);  // retrieves answer portion of string
            // parses and returns answer
            if(s.equals("")){
                return 0;
            }
            s = s.replace(" ","");
            return Integer.parseInt(s);
        }catch(Exception e){
            e.printStackTrace();
            // if answer not in TextView, other option is EditText
            // checks for EditText answer, parses and returns it
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

/**
 * SumWatcher helps keep track of sum question factor EditTexts, whether they are within their limits, and their totals
 */
class SumWatcher implements TextWatcher{
    private TextView tv;  // TextView of sum question displaying total
    private List factors;  // list of sum question's factor EditTexts
    private String qlim;  // number(s) of limiting question(s) in string format
    private HashMap<String, LinearLayout> qns;  // hash map mapping questions to question numbers
    private String f;  // text of specific question factor to be monitored (null if none)
    private AlertDialog dialog;  // dialog to show in case of error
    private EditText et;  // EditText of factor in question (null if none)

    /**
     * Initializes SumWatcher variables
     *
     * @param et EditText of factor in question (null if none)
     * @param tv TextView of sum question displaying total
     * @param factors list of sum question's factor EditTexts
     * @param qlimit number(s) of limiting question(s) in string format
     * @param qns hash map mapping questions to question numbers
     * @param factor text of specific question factor to be monitored (null if none)
     * @param context context of current activity
     */
    SumWatcher(EditText et, TextView tv, List factors, String qlimit, HashMap<String, LinearLayout> qns, String factor, Context context){
        this.tv = tv;
        this.factors = factors;
        this.qlim = qlimit;
        this.qns = qns;
        this.f = factor;
        this.et = et;
        // sets up error message dialog
        AlertDialog.Builder newbuilder = new AlertDialog.Builder(context);
        String msg = "Value too large, contradicts answer to question "+qlimit;
        newbuilder.setMessage(msg);
        this.dialog = newbuilder.create();
    }

    /**
     * On changed answer in EditText, checks to see whether new value is valid,
     * updates sum question total if valid
     *
     * @param s Editable answer in EditText that has just changed
     */
    public void afterTextChanged(Editable s) {
        int flim = -1;  // limit of factor
        int tlim = -1;  // limit of sum question total
        if(!qlim.equals("") && f!=null) {
            // gets factor limit if limiting questions and a factor were specified
            flim = Questionnaire.NumLimit(qlim, qns, f);
        }
        if(!qlim.equals("")){
            // gets total limit if limiting questions were specified
            tlim = Questionnaire.NumLimit(qlim, qns, null);
        }
        // parses integer value of current EditText answer
        int sval;
        if(s.toString().equals("")){
            sval = 0;
        }else {
            sval = Integer.parseInt(s.toString());
        }
        // checks value against factor limit if factor limit exists
        if(sval > flim && flim >= 0){
            dialog.show();
            if(et!=null) {
                et.setText("");
            }
            return;
        }

        // iterates through factor EditTexts and sums their answers
        int sum = 0;
        boolean zero = false;  // if 0 is from user entry (true) or default and no answer (false)
        for(int i = 0;i<factors.size();i++){
            String value = ((EditText)factors.get(i)).getText().toString();
            if(value.equals("0")){
                // zero is true if a customer entered a 0 and didn't just miss the question
                zero = true;
            }
            try {
                // adds factor answer to sum of sum question parts
                int v = Integer.parseInt(value);
                sum += v;
                if(sum > tlim && tlim > 0){
                    // checks to make sure sum does not exceed limit on question answer total
                    ((EditText)factors.get(i)).setText("");
                    dialog.show();
                    break;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        // updates answer TextView display either with blank answer of updated sum
        if(sum == 0 && !zero){
            String settext = "Total: ";
            tv.setText(settext);
            tv.setTextSize(17);
        } else{
            String total = Integer.toString(sum);
            String settext = "Total: "+total;
            tv.setText(settext);
            tv.setTextSize(17);
        }
    }
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
}


/**
 * onCheckedChangedB manages showing in and removing from view questions dependent on specific answer choices
 */
class onCheckedChangedB implements RadioButton.OnCheckedChangeListener{
    private HashMap questions;  // hash map mapping questions to question numbers
    private HashMap<String, Integer> dependents_map;  // maps number of currently positive answers question is dependent on to the question's id
    private ArrayList<String> dependents;  // list of qids of questions dependent on choice instance of onCheckedChangedB is monitoring

    /**
     * Initializes onCheckedChangedB variables
     *
     * @param qns hash map mapping questions to question numbers
     * @param deps mapping current number of choices a question is dependent on that are answered positively to the question's id
     * @param dependents list of qids of questions dependent on choice button instance of onCheckedChangedB is monitoring
     */
    onCheckedChangedB(HashMap qns, HashMap<String, Integer> deps, ArrayList<String> dependents){
        this.questions = qns;
        this.dependents_map = deps;
        this.dependents = dependents;
    }

    /**
     * Adds or removes questions dependent on an answer choice when choice answer changes
     *
     * @param b choice button instance of onCheckedChangedB is monitoring
     * @param isChecked whether or not choice button is checked
     */
    @Override
    public void onCheckedChanged(CompoundButton b, boolean isChecked){
        if(isChecked){
            // when button is checked
            // iterates through choice's dependents
            for(String dependent: dependents){
                // each dependent increases by one the number of answers it depends on answered positively
                dependent = dependent.replace(" ","");
                int u = dependents_map.get(dependent);
                dependents_map.put(dependent, u+1);
                // dependent is set visible
                ((LinearLayout)questions.get(dependent)).setVisibility(View.VISIBLE);
                LinearLayout q = ((LinearLayout)questions.get(dependent));
                try{
                    // if dependent is conditionally required
                    // sets required question text as main question text, making question now required
                    TextView rtv = (TextView)q.findViewWithTag("required");
                    TextView tv = (TextView) q.findViewWithTag("text");
                    String sw = tv.getText().toString();
                    tv.setText(rtv.getText());
                    rtv.setText(sw);
                }catch(Exception e){
                    System.out.println(e.getStackTrace()[0]);
                }
            }
        }else{
            for(String dependent: dependents){
                // each dependent increases by one the number of answers it depends on answered positively
                int u = dependents_map.get(dependent);
                dependents_map.put(dependent, u-1);
                if(dependents_map.get(dependent).equals(0)) {
                    // if a dependent has 0 answers it depends on answered positively
                    // it is removed from view and it is made not required (switching required text for main text)
                    LinearLayout q = ((LinearLayout)questions.get(dependent));
                    q.setVisibility(View.GONE);
                    try{
                        TextView rtv = (TextView)q.findViewWithTag("required");
                        TextView tv = (TextView) q.findViewWithTag("text");
                        String sw = tv.getText().toString();
                        tv.setText(rtv.getText());
                        rtv.setText(sw);
                    }catch(Exception e){
                        System.out.println(e.getStackTrace()[0]);
                    }
                }
            }
        }
    }
}


/**
 * NumQWatcher checks whether a number question's answer is within specified limits every time it changes
 */
class NumQWatcher implements TextWatcher {
    private String q;  // number(s) (in string format) of limiting question(s)
    private EditText et;  // EditText whose answers are to be monitored
    private HashMap<String, LinearLayout> qns;  // hash map mapping questions to question numbers
    private AlertDialog dialog;  // dialog to show in case of error

    /**
     * Initializes NumQWatcher variables
     *
     * @param et EditText whose answers are to be monitored
     * @param l number(s) (in string format) of limiting question(s)
     * @param qns hash map mapping questions to question numbers
     * @param context context of current activity
     */
    NumQWatcher(EditText et, String l, HashMap<String, LinearLayout> qns, Context context){
        this.et = et;
        this.q = l;
        this.qns = qns;
        // sets up error message dialog
        AlertDialog.Builder newbuilder = new AlertDialog.Builder(context);
        String msg = "Value too large, contradicts answer to question "+l;
        newbuilder.setMessage(msg);
        this.dialog = newbuilder.create();
    }

    /**
     * Checks whether current answer in EditText is within specified limits
     *
     * @param s Editable answer in EditText that was changed
     */
    public void afterTextChanged(Editable s) {
        this.checkValid(et, q, qns, s);
    }
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    /**
     * Checks whether current answer in EditText is less than or equal to limit set by limiting question(s)
     *
     * @param et EditText being monitored
     * @param q number(s) (in string format) of limiting question(s)
     * @param qns hash map mapping questions to question numbers
     * @param s current Editable answer in EditText that was changed
     */
    private void checkValid(EditText et, String q, HashMap<String, LinearLayout> qns, Editable s){
        int lim = Questionnaire.NumLimit(q, qns, null);  // gets limit from Questionnaire.NumLimit()
        int val;  // Integer value of EditText answer
        if(s.toString().equals("")){
            // if no answer, trivially does not exceed the limit
            return;
        }
        try{
            // parses Integer value of answer
            val = Integer.parseInt(s.toString());
            System.out.println("successfully parsed");
            if(val > lim){
                // if greater than limit, shows error message and resets EditText to blank
                dialog.show();
                et.setText("");
            }
        }catch(Exception e){
            et.setText("");
        }
    }
}

