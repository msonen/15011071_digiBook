package com.example.digiBook;

//import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.deskode.recorddialog.RecordDialog;
import com.example.digiBook.ColorPicker.ColorPickerDialog;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.digiBook.DataManager.NOTE_AUDIO;
import static com.example.digiBook.DataManager.NOTE_DATE;
import static com.example.digiBook.DataManager.NOTE_DOC;
import static com.example.digiBook.DataManager.NOTE_PIC;

import static com.example.digiBook.ColorPicker.ColorPickerSwatch.OnColorSelectedListener;
import static com.example.digiBook.ColorPicker.ColorPickerSwatch.OnTouchListener;
import static com.example.digiBook.DataManager.NEW_NOTE_REQUEST;
import static com.example.digiBook.DataManager.NOTE_BODY;
import static com.example.digiBook.DataManager.NOTE_COLOUR;
import static com.example.digiBook.DataManager.NOTE_FONT_SIZE;
import static com.example.digiBook.DataManager.NOTE_HIDE_BODY;
import static com.example.digiBook.DataManager.NOTE_REMINDER;
import static com.example.digiBook.DataManager.NOTE_REQUEST_CODE;
import static com.example.digiBook.DataManager.NOTE_TITLE;
import static com.example.digiBook.DataManager.NOTE_VIDEO;


public class EditActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    private static final int VIDEO_CAPTURE_REQUEST = 178;
    // Layout components
    private EditText titleEdit, bodyEdit;
    private RelativeLayout relativeLayoutEdit;
    private Toolbar toolbar;
    private MenuItem menuHideBody;
    private InputMethodManager imm;
    private Bundle bundle;
    private final  int PICKFILE_REQUEST_CODE = 682;
    private final int TAKE_PHOTO_REQUEST_CODE = 123;
    private String[] colourArr; // Colours string array
    private int[] colourArrResId; // colourArr to resource int array
    private int[] fontSizeArr; // Font sizes int array
    private String[] fontSizeNameArr; // Font size names string array
    private String soundPath="", doc_path = "", picPath= "";
    // Defaults
    private String colour = "#FFFFFF"; // white default
    private int fontSize = 18; // Medium default
    private Boolean hideBody = false;
    private String dateString = "";
    private long timestamp;
    private long alarm_timestamp=0L;
    private AlertDialog fontDialog, saveChangesDialog;
    private ColorPickerDialog colorPickerDialog;
    private LinearLayout addonLayout, audioLayout;
    private TextView dateViewText;
    private String videoPath = "";
    private RecordDialog recordDialog;
    private boolean sometihng_changed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // Initialize colours and font sizes arrays
        colourArr = getResources().getStringArray(R.array.colours);

        colourArrResId = new int[colourArr.length];
        for (int i = 0; i < colourArr.length; i++)
            colourArrResId[i] = Color.parseColor(colourArr[i]);

        fontSizeArr = new int[] {14, 18, 22}; // 0 for small, 1 for medium, 2 for large
        fontSizeNameArr = getResources().getStringArray(R.array.fontSizeNames);

        setContentView(R.layout.activity_edit);

        // Init layout components

        toolbar = (Toolbar)findViewById(R.id.toolbarEdit);
        titleEdit = (EditText)findViewById(R.id.titleEdit);
        bodyEdit = (EditText)findViewById(R.id.bodyEdit);
        relativeLayoutEdit = (RelativeLayout)findViewById(R.id.relativeLayoutEdit);
        ScrollView scrollView = (ScrollView)findViewById(R.id.scrollView);
        addonLayout = (LinearLayout)findViewById(R.id.layout_addons);
        dateViewText = (TextView)findViewById(R.id.date_view);
        imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);

        if (toolbar != null)
            initToolbar();

        // If scrollView touched and note body doesn't have focus -> request focus and go to body end
        scrollView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!bodyEdit.isFocused()) {
                    bodyEdit.requestFocus();
                    bodyEdit.setSelection(bodyEdit.getText().length());
                    // Force show keyboard
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                            InputMethodManager.HIDE_IMPLICIT_ONLY);

                    return true;
                }

                return false;
            }
        });

        // Get data bundle from MainActivity
        bundle = getIntent().getExtras();

        if (bundle != null) {
            // If current note is not new -> initialize colour, font, hideBody and EditTexts
            if (bundle.getInt(NOTE_REQUEST_CODE) != NEW_NOTE_REQUEST) {
                colour = bundle.getString(NOTE_COLOUR);
                fontSize = bundle.getInt(NOTE_FONT_SIZE);
                hideBody = bundle.getBoolean(NOTE_HIDE_BODY);

                titleEdit.setText(bundle.getString(NOTE_TITLE));
                bodyEdit.setText(bundle.getString(NOTE_BODY));
                bodyEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

                if (hideBody)
                    menuHideBody.setTitle(R.string.action_show_body);

                soundPath = bundle.getString(NOTE_AUDIO);
                videoPath = bundle.getString(NOTE_VIDEO);

                picPath = bundle.getString(NOTE_PIC);
                doc_path = bundle.getString(NOTE_DOC);
                timestamp = Long.parseLong(bundle.getString(NOTE_DATE));
                alarm_timestamp = Long.parseLong(bundle.getString(NOTE_REMINDER));

                if (alarm_timestamp != 0L){

                    ImageView v = new ImageView(getApplicationContext());
                    v.setVisibility(View.VISIBLE);
                    v.setImageResource(R.mipmap.ic_reminder_addon);
                    //Uri tempUri = getImageUri(getApplicationContext(), imageBitmap);
                    v.setBackgroundColor(Color.TRANSPARENT);
                    //Log.v("PATH ",data.getData().getPath());
                    v.setPadding(0, 0, 0, 0);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            init_alarm(new Date(alarm_timestamp));

                        }
                    });
                    //LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_addons);
                    addonLayout.addView(v);

                }
                if (!soundPath.isEmpty()){
                    ImageView v =new ImageView(getApplicationContext());
                    v.setImageResource(R.mipmap.ic_music_addon);
                    v.setBackgroundColor(Color.TRANSPARENT);
                    v.setPadding(0, 0, 0, 0);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent myIntent = new Intent(Intent.ACTION_VIEW);
                            myIntent.setDataAndType(Uri.fromFile(new File(soundPath)),"audio/*");
                            myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Intent j = Intent.createChooser(myIntent, "Choose an application to open with:");
                            startActivity(j);
                            //Log.v("PATH",data.getData().toString());

                        }
                    });
                    addonLayout.addView(v);
                }

                if(!videoPath.isEmpty()){

                    ImageView v = new ImageView(getApplicationContext());
                    v.setVisibility(View.VISIBLE);
                    v.setImageResource(R.mipmap.ic_camera_addon);
                    //Uri tempUri = getImageUri(getApplicationContext(), imageBitmap);
                    v.setBackgroundColor(Color.TRANSPARENT);
                    //Log.v("PATH ",data.getData().getPath());
                    v.setPadding(0, 0, 0, 0);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent myIntent = new Intent(Intent.ACTION_VIEW);
                            //myIntent.setData(data.getData());
                            myIntent.setDataAndType(Uri.parse(videoPath),"video/*");
                            myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            myIntent.addFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP);
                            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Intent j = Intent.createChooser(myIntent, "Choose an application to open with:");
                            startActivity(j);
                        }
                    });
                    //LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_addons);
                    addonLayout.addView(v);

                }


                if (!doc_path.isEmpty()){

                    final File file = new File(doc_path);
                    //Log.v("INIT_SETTINGS",doc_path);
                    ImageView v = new ImageView(getApplicationContext());
                    v.setVisibility(View.VISIBLE);
                    v.setImageResource(R.mipmap.ic_doc_addon);
                    v.setBackgroundColor(Color.TRANSPARENT);
                    v.setPadding(0, 0, 0, 0);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent myIntent = new Intent(Intent.ACTION_VIEW);
                            myIntent.setDataAndType(Uri.parse(doc_path), "*/*");
                            myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            Intent j = Intent.createChooser(myIntent, "Choose an application to open with:");
                            startActivity(j);

                            Log.v("PATH",Uri.fromFile(file).getPath());
                            //Toast.makeText(getApplicationContext(),"INIT_SETTINGS" + doc_path, Toast.LENGTH_SHORT).show();
                        }
                    });
                    addonLayout.addView(v);

                }
                //Log.v("INIT_SETTINGS",picPath);
                if (!picPath.isEmpty()){
                  //  Log.v("INIT_SETTINGS",picPath);
                    final File file = new File(picPath);
                    ImageView v = new ImageView(getApplicationContext());
                    v.setVisibility(View.VISIBLE);
                    v.setImageResource(R.mipmap.ic_pic_addon);
                    v.setBackgroundColor(Color.TRANSPARENT);
                    v.setPadding(0, 0, 0, 0);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent myIntent = new Intent(Intent.ACTION_VIEW);
                            myIntent.setDataAndType(Uri.parse(picPath),"image/*");
                            myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            Intent j = Intent.createChooser(myIntent, "Choose an application to open with:");
                            startActivity(j);
                            //Toast.makeText(getApplicationContext(),"INIT_SETTINGS" + doc_path, Toast.LENGTH_SHORT).show();

                        }
                    });
                    addonLayout.addView(v);
                }

            }

            // If current note is new -> request keyboard focus to note title and show keyboard
            else if (bundle.getInt(NOTE_REQUEST_CODE) == NEW_NOTE_REQUEST) {
                titleEdit.requestFocus();
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                timestamp = new Date().getTime();


            }

            // Set background colour to note colour
            relativeLayoutEdit.setBackgroundColor(Color.parseColor(colour));
        }



        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        dateString = dateFormat.format(new Date(timestamp));
        dateViewText.setText(dateString);
        initDialogs(this);
    }


    /**
     * Initialize toolbar with required components such as
     * - title, navigation icon + listener, menu/OnMenuItemClickListener, menuHideBody -
     */
    protected void initToolbar() {
        toolbar.setTitle("");

        // Set a 'Back' navigation icon in the Toolbar and handle the click
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Inflate menu_edit to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_edit);

        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(this);

        Menu menu = toolbar.getMenu();

        if (menu != null)
            menuHideBody = menu.findItem(R.id.action_hide_show_body);
    }


    /**
     * Implementation of AlertDialogs such as
     * - colorPickerDialog, fontDialog and saveChangesDialog -
     * @param context The Activity context of the dialogs; in this case EditActivity context
     */
    protected void initDialogs(Context context) {
        // Colour picker dialog
        colorPickerDialog = ColorPickerDialog.newInstance(R.string.dialog_note_colour,
                colourArrResId, Color.parseColor(colour), 3, ColorPickerDialog.SIZE_SMALL);

        // Colour picker listener in colour picker dialog
        colorPickerDialog.setOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                // Format selected colour to string
                String selectedColourAsString = String.format("#%06X", (0xFFFFFF & color));

                // Check which colour is it and equal to main colour
                for (String aColour : colourArr)
                    if (aColour.equals(selectedColourAsString))
                        colour = aColour;

                // Re-set background colour
                relativeLayoutEdit.setBackgroundColor(Color.parseColor(colour));
            }
        });


        // Font size picker dialog
        fontDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_font_size)
                .setItems(fontSizeNameArr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Font size updated with new pick
                        fontSize = fontSizeArr[which];
                        bodyEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                    }
                })
                .setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();


        // 'Save changes?' dialog
        saveChangesDialog = new AlertDialog.Builder(context)
                .setMessage(R.string.dialog_save_changes)
                .setPositiveButton(R.string.yes_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If 'Yes' clicked -> check if title is empty
                        // If title not empty -> save and go back; Otherwise toast
                        if (!isEmpty(titleEdit) || !isEmpty(bodyEdit))
                            saveChanges();

                        else
                            toastEditTextCannotBeEmpty();
                    }
                })
                .setNegativeButton(R.string.no_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If 'No' clicked in new note -> put extra 'discard' to show toast
                        if (bundle != null && bundle.getInt(NOTE_REQUEST_CODE) ==
                                NEW_NOTE_REQUEST) {

                            Intent intent = new Intent();
                            intent.putExtra("request", "discard");

                            setResult(RESULT_CANCELED, intent);

                            imm.hideSoftInputFromWindow(titleEdit.getWindowToken(), 0);

                            dialog.dismiss();
                            finish();
                            overridePendingTransition(0, 0);
                        }
                    }
                })
                .create();
    }




    /**
     * Item clicked in Toolbar menu callback method
     * @param item Item clicked
     * @return true if click detected and logic finished, false otherwise
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();

        // Note colour menu item clicked -> show colour picker dialog
        if (id == R.id.action_note_colour) {
            colorPickerDialog.show(getFragmentManager(), "colourPicker");
            return true;
        }
        if (id == R.id.action_sound){



            recordDialog = RecordDialog.newInstance("Record Audio");
            recordDialog.setMessage("Press for record");
            recordDialog.show(this.getFragmentManager(),"TAG");
            recordDialog.setPositiveButton("Save", new RecordDialog.ClickListener() {
                @Override
                public void OnClickListener(final String path) {
                    //Toast.makeText(getApplicationContext(),"Save audio: "+path, Toast.LENGTH_LONG).show();
                   // AudioWife.getInstance().init(getApplication(), Uri.parse(path))
                     //       .useDefaultUi((ViewGroup)findViewById(R.id.layout_audioPlayer), getLayoutInflater());
                    soundPath = path;
                    sometihng_changed = true;
                    ImageView v = new ImageView(getApplicationContext());
                    v.setVisibility(View.VISIBLE);
                    v.setImageResource(R.mipmap.ic_music_addon);
                    v.setBackgroundColor(Color.TRANSPARENT);
                    v.setPadding(0, 0, 0, 0);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent myIntent = new Intent(Intent.ACTION_VIEW);
                            myIntent.setDataAndType(Uri.fromFile(new File(soundPath)),"audio/*");
                            myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Intent j = Intent.createChooser(myIntent, "Choose an application to open with:");
                            startActivity(j);
                            Log.v("PATH",Uri.fromFile(new File(soundPath)).getPath());


                        }
                    });


                    addonLayout.addView(v);
                }
            });



        }
        if (id==R.id.action_pic){


            //final Context context = EditActivity.this;


            //Dialog dialog = new Dialog(context);
            //ConstraintLayout featureLayout = (ConstraintLayout) View.inflate(this, R.layout.popup_addmenu, null);
            //dialog.setContentView(featureLayout);
            //dialog.show();
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePicture, TAKE_PHOTO_REQUEST_CODE);



        }

        if (id == R.id.action_add_video){

            Intent captureVideoIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
            startActivityForResult(captureVideoIntent,VIDEO_CAPTURE_REQUEST);

        }

        if (id == R.id.action_pickFile){


            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("*/*");
            startActivityForResult(i, PICKFILE_REQUEST_CODE);

        }

        // Font size menu item clicked -> show font picker dialog
        if (id == R.id.action_font_size) {
            fontDialog.show();
            return true;
        }
        if (id==R.id.action_add_alarm){

            init_alarm(new Date());

        }
        // If 'Hide note body in list' or 'Show note body in list' clicked
        if (id == R.id.action_hide_show_body) {
            // If hideBody false -> set to true and change menu item text to 'Show note body in list'
            if (!hideBody) {
                hideBody = true;
                menuHideBody.setTitle(R.string.action_show_body);

                // Toast note body will be hidden
                Toast toast = Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.toast_note_body_hidden),
                        Toast.LENGTH_SHORT);
                toast.show();
            }

            // If hideBody true -> set to false and change menu item text to 'Hide note body in list'
            else {
                hideBody = false;
                menuHideBody.setTitle(R.string.action_hide_body);

                // Toast note body will be shown
                Toast toast = Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.toast_note_body_showing),
                        Toast.LENGTH_SHORT);
                toast.show();
            }

            return true;
        }

        return false;
    }


    private String getMime(Uri uri){
        ContentResolver cR = this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return cR.getType(uri);
    }

    private void init_alarm(Date date){
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        final Context context = this;
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker,  int year, int monthOfYear, int dayOfMonth) {
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);

                final int a = year, b = monthOfYear, ca = dayOfMonth;
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay,int minute) {
                        c.set(a, b, ca,hourOfDay, minute);
                        long when = c.getTime().getTime();
                        Log.v("ALARM", c.getTime().toString());
                        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                        Intent intent = new Intent(context, AlarmReceiver.class);
                        packIntent(intent);
                        //intent.putExtra("myAction", "Notify");
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

                        if (alarm_timestamp == 0L) {
                            ImageView v = new ImageView(getApplicationContext());
                            v.setVisibility(View.VISIBLE);
                            v.setImageResource(R.mipmap.ic_reminder_addon);
                            //Uri tempUri = getImageUri(getApplicationContext(), imageBitmap);
                            v.setBackgroundColor(Color.TRANSPARENT);
                            //Log.v("PATH ",data.getData().getPath());
                            v.setPadding(0, 0, 0, 0);
                            v.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    init_alarm(new Date(alarm_timestamp));

                                }
                            });
                            alarm_timestamp = when;
                            //LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_addons);
                            addonLayout.addView(v);
                        }
                        else {
                            am.cancel(pendingIntent);
                        }
                        am.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
                        Toast.makeText(context, "Alarm Set Up!", Toast.LENGTH_SHORT).show();
                        sometihng_changed = true;
                    }
                }, mHour, mMinute, true);
                timePickerDialog.show();
            }
        },mYear,mMonth, mDay);
        datePickerDialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);;
        switch (requestCode) {
            case 0: {
                if (resultCode == RESULT_OK) {
                    // Great! User has recorded and saved the audio file
                } else if (resultCode == RESULT_CANCELED) {
                    // Oops! User has canceled the recording
                }

                break;
            }
            case PICKFILE_REQUEST_CODE: {
                if (resultCode != RESULT_OK)
                    break;
                final Uri ft = data.getData();
                ImageView v = new ImageView(getApplicationContext());
                v.setVisibility(View.VISIBLE);
                v.setImageResource(R.mipmap.ic_doc_addon);
                v.setBackgroundColor(Color.TRANSPARENT);
                v.setPadding(0, 0, 0, 0);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent myIntent = new Intent(Intent.ACTION_VIEW);
                        myIntent.setData(data.getData());
                        myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Intent j = Intent.createChooser(myIntent, "Choose an application to open with:");
                        startActivity(j);
                        Log.v("PATH",data.getData().toString());


                    }
                });



                addonLayout.addView(v);
                doc_path = data.getData().toString();
                sometihng_changed = true;

                break;

            }
            case TAKE_PHOTO_REQUEST_CODE: {
                if (resultCode != RESULT_OK)
                    break;
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                String[] projection = { MediaStore.Images.Media.DATA };
                Cursor cursor = managedQuery(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection, null, null, null);
                int column_index_data = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToLast();

                final String imagePath = cursor.getString(column_index_data);
                //final Uri uri = data.getData();
                ImageView v = new ImageView(getApplicationContext());
                v.setVisibility(View.VISIBLE);
                v.setImageResource(R.mipmap.ic_pic_addon);
                //Uri tempUri = getImageUri(getApplicationContext(), imageBitmap);
                v.setBackgroundColor(Color.TRANSPARENT);
                //Log.v("PATH ",data.getData().getPath());
                v.setPadding(0, 0, 0, 0);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent myIntent = new Intent(Intent.ACTION_VIEW);
                        //myIntent.setData(data.getData());
                        myIntent.setDataAndType(Uri.parse(imagePath),"image/*");
                        myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        myIntent.addFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Intent j = Intent.createChooser(myIntent, "Choose an application to open with:");
                        startActivity(j);
                    }
                });
                //LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_addons);
                addonLayout.addView(v);
                this.picPath = imagePath;
                sometihng_changed = true;

                break;
            }
            case VIDEO_CAPTURE_REQUEST: {

                if (resultCode != RESULT_OK)
                    break;
                ImageView v = new ImageView(getApplicationContext());
                v.setVisibility(View.VISIBLE);
                v.setImageResource(R.mipmap.ic_camera_addon);
                //Uri tempUri = getImageUri(getApplicationContext(), imageBitmap);
                v.setBackgroundColor(Color.TRANSPARENT);
                //Log.v("PATH ",data.getData().getPath());
                v.setPadding(0, 0, 0, 0);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent myIntent = new Intent(Intent.ACTION_VIEW);
                        //myIntent.setData(data.getData());
                        myIntent.setDataAndType(data.getData(),"video/*");
                        myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        myIntent.addFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Intent j = Intent.createChooser(myIntent, "Choose an application to open with:");
                        startActivity(j);
                    }
                });
                //LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_addons);
                addonLayout.addView(v);
                this.videoPath = data.getData().toString();
                sometihng_changed = true;
            }
        }
    }



    private void packIntent(Intent intent){


        // Package everything and send back to activity with OK
        intent.putExtra(NOTE_TITLE, titleEdit.getText().toString());
        intent.putExtra(NOTE_BODY, bodyEdit.getText().toString());
        intent.putExtra(NOTE_COLOUR, colour);
        intent.putExtra(NOTE_FONT_SIZE, fontSize);
        intent.putExtra(NOTE_HIDE_BODY, hideBody);
        intent.putExtra(NOTE_AUDIO, soundPath);
        intent.putExtra(NOTE_VIDEO, videoPath);
        intent.putExtra(NOTE_DOC, doc_path);
        intent.putExtra(NOTE_PIC, picPath);
        intent.putExtra(NOTE_DATE, String.valueOf(timestamp));
        intent.putExtra(NOTE_REMINDER, String.valueOf(alarm_timestamp));

    }

    /**
     * Create an Intent with title, body, colour, font size and hideBody extras
     * Set RESULT_OK and go back to MainActivity
     */
    protected void saveChanges() {
        Intent intent = new Intent();
        packIntent(intent);
        setResult(RESULT_OK, intent);

        imm.hideSoftInputFromWindow(titleEdit.getWindowToken(), 0);

        finish();
        overridePendingTransition(0, 0);
    }


    /**
     * Back or navigation '<-' pressed
     */
    @Override
    public void onBackPressed() {

        saveJob();
    }

    private void saveJob(){
        // New note -> show 'Save changes?' dialog
        if (bundle.getInt(NOTE_REQUEST_CODE) == NEW_NOTE_REQUEST)
            saveChangesDialog.show();

            // Existing note
        else {
            /*
             * If title is not empty -> Check if note changed
             *  If yes -> saveChanges
             *  If not -> hide keyboard if showing and finish
             */
            if (!isEmpty(titleEdit) || !isEmpty(bodyEdit)) {
                if (!(titleEdit.getText().toString().equals(bundle.getString(NOTE_TITLE))) ||
                        !(bodyEdit.getText().toString().equals(bundle.getString(NOTE_BODY))) ||
                        !(colour.equals(bundle.getString(NOTE_COLOUR))) ||
                        fontSize != bundle.getInt(NOTE_FONT_SIZE) ||
                        hideBody != bundle.getBoolean(NOTE_HIDE_BODY) || sometihng_changed) {
                    if (recordDialog !=null){
                        recordDialog.dismiss();
                        recordDialog.onDestroy();
                    }
                    saveChanges();
                }

                else {
                   imm.hideSoftInputFromWindow(titleEdit.getWindowToken(), 0);

                    finish();
                    overridePendingTransition(0, 0);
                }
            }

            // If title empty -> Toast title cannot be empty
            else
                toastEditTextCannotBeEmpty();
        }
    }
    /**
     * Check if passed EditText text is empty or not
     * @param editText The EditText widget to check
     * @return true if empty, false otherwise
     */
    protected boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

    /**
     * Show Toast for 'Title cannot be empty'
     */
    protected void toastEditTextCannotBeEmpty() {
        Toast toast = Toast.makeText(getApplicationContext(),
                getResources().getString(R.string.toast_edittext_cannot_be_empty),
                Toast.LENGTH_LONG);
        toast.show();
    }


    /**
     * If current window loses focus -> hide keyboard
     * @param hasFocus parameter passed by system; true if focus changed, false otherwise
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!hasFocus)
            if (imm != null && titleEdit != null)
                imm.hideSoftInputFromWindow(titleEdit.getWindowToken(), 0);
    }


    /**
     * Orientation changed callback method
     * If orientation changed -> If any AlertDialog is showing -> dismiss it to prevent WindowLeaks
     * @param newConfig Configuration passed by system
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (colorPickerDialog != null && colorPickerDialog.isDialogShowing())
            colorPickerDialog.dismiss();

        if (fontDialog != null && fontDialog.isShowing())
            fontDialog.dismiss();

        if (saveChangesDialog != null && saveChangesDialog.isShowing())
            saveChangesDialog.dismiss();

        super.onConfigurationChanged(newConfig);
    }



}
